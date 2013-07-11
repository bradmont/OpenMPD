package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;

import android.content.Context;
import android.database.Cursor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class SummaryCard extends Card {

    // current or new monthly partners
    public static final String STABLE_MONTHLY_SQL = 
        "select sum(giving_amount) "+
            "from contact_status " +
            "where partner_type=6 " +
            "and (status=4 or status=5);";
            // magic numbers! 

    // current or new regular or annual partners
    public static final String STABLE_REGULAR_SQL = 
        "select sum(giving_amount/gift_frequency) " +
            "from contact_status "+
            "where (partner_type=4 or partner_type=5) "+
                "and (status=4 or status=5);";

    // late partners
    public static final String LATE_SQL = 
        "select sum(giving_amount/gift_frequency) "+
            "from contact_status "+
            "where (partner_type=6 or partner_type=5 or partner_type=4) "+
            "and status=3;";

    // lapsed partners
    public static final String LAPSED_SQL = 
        "select sum(giving_amount/gift_frequency) "+
            "from contact_status "+
            "where (partner_type=6 or partner_type=5 or partner_type=4) "+
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


	public SummaryCard(){
		super();
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_summary, null);

        int stable_monthly = getSqlInt(STABLE_MONTHLY_SQL);
        int stable_regular = getSqlInt(STABLE_REGULAR_SQL);
        int late_average = getSqlInt(LATE_SQL);
        int lapsed_average = getSqlInt(LAPSED_SQL);

		((TextView) view.findViewById(R.id.stable_monthly))
            .setText(formatMoney(stable_monthly));

		((TextView) view.findViewById(R.id.stable_regular))
            .setText(formatMoney(stable_monthly + stable_regular));
        
		((TextView) view.findViewById(R.id.late_giving))
            .setText(formatMoney(late_average));

		((TextView) view.findViewById(R.id.lapsed_giving))
            .setText(formatMoney(lapsed_average));

        String month = getThisMonth();
        String [] args = new String[2];
        args[0] = month;
        args[1] = Integer.toString(context.getSharedPreferences("openmpd", Context.MODE_PRIVATE).getInt("average_period", 12));
        int average_special = getSqlInt(SPECIAL_SQL, args);

		((TextView) view.findViewById(R.id.average_special))
            .setText(formatMoney(average_special));

        int average_monthly = getSqlInt(AVERAGE_GIVING, args);
        String title = String.format(
            context.getResources().getString(R.string.summary_card_title),
            formatMoney(average_monthly));
		((TextView) view.findViewById(R.id.title)).setText(title);
		
		return view;
	}

    private String formatMoney(int value){
        return String.format("$%.2f", ((float) value)/100f);
    }
    private int getSqlInt(String SQL){
        return getSqlInt(SQL, null);
    }

    /**
      * Helper to get a one-value int result from an SQL statement
      */
    private int getSqlInt(String SQL, String [] args){
        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery(SQL, args);
        cur.moveToFirst();
        int result = cur.getInt(0);
        cur.close();
        return result;
    }
    public String getThisMonth(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

	
}
