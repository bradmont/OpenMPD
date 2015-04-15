package net.bradmont.openmpd.helpers;

import net.bradmont.openmpd.OpenMPD;
import net.bradmont.openmpd.MPDDBHelper;

import android.content.Context;
import android.database.Cursor;




/* Various calculations to analyse our support database
 */
public class Analytics {

    private static String mThisMonth = null;
    // current or new monthly partners
    public static final String STABLE_MONTHLY_SQL =
        "select sum(giving_amount) "+
            "from contact_status " +
            "where partner_type=60 " +
            "and (status=4 or status=5);";
            // magic numbers! 

    // current or new regular or annual partners
    public static final String STABLE_REGULAR_SQL =
        "select sum(giving_amount/gift_frequency) " +
            "from contact_status "+
            "where (partner_type=40 or partner_type=50) "+
                "and (status=4 or status=5);";

    // Frequent, but irregular, partners
    public static final String FREQUENT_SQL =
        "select sum(giving_amount) " +
            "from contact_status "+
            "where (partner_type=35) "+
                "and (status=4 or status=5);";

    // late partners
    public static final String LATE_SQL =
        "select sum(giving_amount/gift_frequency) "+
            "from contact_status "+
            "where (partner_type=60 or partner_type=50 or partner_type=40) "+
            "and status=3;";

    // lapsed partners
    public static final String LAPSED_SQL =
        "select sum(giving_amount/gift_frequency) "+
            "from contact_status "+
            "where (partner_type=60 or partner_type=50 or partner_type=40) "+
            "and status=2;";

    // Average of special gifts
    private static final String SPECIAL_SQL =
        "select avg(total_gifts) from "+
            "(select month, sum(total_gifts) - sum(giving_amount)  as total_gifts "+
                "from  "+
                    "(select * from ( select contact_id, tnt_people_id, giving_amount from contact join contact_status on contact_id=contact._id where tnt_people_id not like '-%') A  "+
                    "join  "+
                    "(select tnt_people_id, month, sum(amount) as total_gifts from gift where month!=? group by tnt_people_id,month) B  "+
                    "on A.tnt_people_id = B.tnt_people_id  "+
                "where total_gifts>giving_amount)  "+
                "group by month  "+
                "order by month desc "+
            "limit ?); ";

    // Monthly average
    private static final String AVERAGE_GIVING =
        "select avg(total) from "+
            "(select month, sum(amount) as total  "+
                "from gift  "+
                "where month !=?  "+
                "group by month  "+
                "order by month desc  "+
                "limit ?); ";

    public static String getStableMonthly (){
        return formatMoney(getSqlInt(STABLE_MONTHLY_SQL));
    }

    public static String getStableRegular (){
        return formatMoney(getSqlInt(STABLE_REGULAR_SQL));
    }

    public static String getFrequentAverage (){
        return formatMoney(getSqlInt(FREQUENT_SQL));
    }

    public static String getLateAverage (){
        return formatMoney(getSqlInt(LATE_SQL));
    }

    public static String getLapsedAverage (){
        return formatMoney(getSqlInt(LAPSED_SQL));
    }
    public static String getDroppedTotal (){
        return formatMoney(
            getSqlInt(LAPSED_SQL)
            + getSqlInt(LATE_SQL)
            );
    }
    public static String getOngoingTotal (){
        return formatMoney(
            getSqlInt(STABLE_MONTHLY_SQL)
            + getSqlInt(STABLE_REGULAR_SQL)
            + getSqlInt(FREQUENT_SQL)
            );
    }

    public static String getAverageSpecial (){
        String month = TextTools.getThisMonth();
        String [] args = new String[2];
        args[0] = month;
        args[1] = Integer.toString(OpenMPD.get().getApplicationContext()
            .getSharedPreferences("openmpd", Context.MODE_PRIVATE).getInt("average_period", 12));
        return formatMoney(getSqlInt(SPECIAL_SQL, args));

    }
    public static String getAverage (){
        String month = TextTools.getThisMonth();
        String [] args = new String[2];
        args[0] = month;
        args[1] = Integer.toString(OpenMPD.get().getApplicationContext()
            .getSharedPreferences("openmpd", Context.MODE_PRIVATE).getInt("average_period", 12));
        return formatMoney(getSqlInt(AVERAGE_GIVING, args));
    }

    public static String getWeightedAverage (){
        String month = TextTools.getThisMonth();
        String [] args = new String[2];
        args[0] = month;
        int period = OpenMPD.get().getApplicationContext()
                .getSharedPreferences("openmpd", Context.MODE_PRIVATE).getInt("average_period", 12);

        int total_giving = 0;
        int total_months = 0;
        while (period > 0){
            args[1] = Integer.toString(period);
            total_months += period;
            total_giving += getSqlInt(AVERAGE_GIVING, args) * period;
            period -= 3;
        }
        return formatMoney(total_giving / total_months);
    }

    private static String formatMoney(int value){
        return String.format("$%.0f", ((float) value)/100f);
    }
    private static int getSqlInt(String SQL){
        return getSqlInt(SQL, null);
    }

    /**
      * Helper to get a one-value int result from an SQL statement
      */
    private static int getSqlInt(String SQL, String [] args){
        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery(SQL, args);
        cur.moveToFirst();
        int result = cur.getInt(0);
        cur.close();
        return result;
    }

}
