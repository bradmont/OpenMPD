package net.bradmont.openmpd.helpers;

import net.bradmont.openmpd.OpenMPD;

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
            "where type='monthly' " +
            "and (status='current' or status='new');";
            // magic numbers! 

    // current or new regular or annual partners
    public static final String STABLE_REGULAR_SQL =
        "select sum(giving_amount/giving_frequency) " +
            "from contact_status "+
            "where (type='annual' or type='regular') "+
                "and (status='new' or status='current');";

    // Frequent, but irregular, partners
    public static final String FREQUENT_SQL =
        "select sum(giving_amount) " +
            "from contact_status "+
            "where (type='frequent') "+
                "and (status='new' or status='current');";

    // late partners
    public static final String LATE_SQL =
        "select sum(giving_amount/giving_frequency) "+
            "from contact_status "+
            "where (type='monthly' or type='regular' or type='annual') "+
            "and status='late';";

    // lapsed partners
    public static final String LAPSED_SQL =
        "select sum(giving_amount/giving_frequency) "+
            "from contact_status "+
            "where (type='monthly' or type='current' or type='annual') "+
            "and status='lapsed';";

    // Average of special gifts
    private static final String SPECIAL_SQL =
        "select avg(total_gifts) from "+
            "(select month, sum(total_gifts) - sum(giving_amount)  as total_gifts "+
                "from  "+
                    "(select * from ( select contact._id, contact_id, giving_amount from contact join contact_status on contact_id=contact._id ) A  "+
                    "join  "+
                    "(select contact_id, month, sum(amount) as total_gifts from gift where month!=? group by contact_id,month) B  "+
                    "on A.contact_id = B.contact_id  "+
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
        String month = TextTools.getThisYearMonth();
        String [] args = new String[2];
        args[0] = month;
        args[1] = Integer.toString(OpenMPD.get().getApplicationContext()
            .getSharedPreferences("openmpd", Context.MODE_PRIVATE).getInt("average_period", 12));
        return formatMoney(getSqlInt(SPECIAL_SQL, args));

    }
    public static String getAverage (){
        String month = TextTools.getThisYearMonth();
        String [] args = new String[2];
        args[0] = month;
        args[1] = Integer.toString(OpenMPD.get().getApplicationContext()
            .getSharedPreferences("openmpd", Context.MODE_PRIVATE).getInt("average_period", 12));
        return formatMoney(getSqlInt(AVERAGE_GIVING, args));
    }

    public static String getWeightedAverage (){
        String month = TextTools.getThisYearMonth();
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
        Cursor cur = OpenMPD.getDB().rawQuery(SQL, args);
        cur.moveToFirst();
        int result = cur.getInt(0);
        cur.close();
        return result;
    }

}
