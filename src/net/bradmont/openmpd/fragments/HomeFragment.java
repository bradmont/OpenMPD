package net.bradmont.openmpd.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.bradmont.openmpd.R;
import net.bradmont.openmpd.MPDDBHelper;
import net.bradmont.holograph.BarGraph;


public class HomeFragment extends Fragment {

    // monthly giving == regular giving
    private static final String BASE_GIVING_SQL =
        "select month, sum(total_gifts) "+
        "    from "+
        "        ( select contact_id, tnt_people_id, giving_amount, partner_type from contact join contact_status on contact_id=contact._id where tnt_people_id>0) A "+
        "        join "+
        "        (select tnt_people_id, month, sum(amount) as total_gifts from gift group by tnt_people_id,month) B "+
        "        on A.tnt_people_id = B.tnt_people_id "+
        "    where total_gifts=giving_amount and partner_type=6"+
        "    group by month "+
        "    order by month; ";


    // non-monthly regular
    private static final String REGULAR_NONMONTHLY_GIVING = 
    "select * from  "+
    "   (select distinct month from gift) months left outer join (select month, sum(total_gifts) "+
    "    from "+
    "        ( select contact_id, tnt_people_id, giving_amount, partner_type from contact join contact_status on contact_id=contact._id where tnt_people_id>0) A "+
    "        join "+
    "        (select tnt_people_id, month, sum(amount) as total_gifts from gift group by tnt_people_id,month) B "+
    "        on A.tnt_people_id = B.tnt_people_id "+
    "    where total_gifts=giving_amount and (partner_type=5 or partner_type=4) "+
    "    group by month) gifts "+
    "    on months.month=gifts.month "+
    "    order by month;";

    // monthly giving where month's gifts > regular giving
    private static final String BASE_GIVING_SQL_EXTRA =
        "select month, sum(giving_amount) "+
        "    from "+
        "        ( select contact_id, tnt_people_id, giving_amount from contact join contact_status on contact_id=contact._id where tnt_people_id>0) A "+
        "        join "+
        "        (select tnt_people_id, month, sum(amount) as total_gifts from gift group by tnt_people_id,month) B "+
        "        on A.tnt_people_id = B.tnt_people_id "+
        "    where total_gifts>giving_amount "+
        "    group by month "+
        "    order by month; ";


    // gifts above monthly giving
    private static final String SPECIAL_SQL =
        "    select month, sum(total_gifts) - sum(giving_amount) "+
        "    from "+
        "        (select * from ( select contact_id, tnt_people_id, giving_amount from contact join contact_status on contact_id=contact._id where tnt_people_id>0) A "+
        "        join "+
        "        (select tnt_people_id, month, sum(amount) as total_gifts from gift group by tnt_people_id,month) B "+
        "        on A.tnt_people_id = B.tnt_people_id "+
        "    where total_gifts>giving_amount) "+
        "    group by month "+
        "    order by month; ";



    Float [] values = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // magic graph
        Float [] [] vals = null;

        Cursor cur1 = MPDDBHelper.get().getReadableDatabase().rawQuery(
            BASE_GIVING_SQL, null);
        values = new Float[cur.getCount()];
        Cursor cur2 = MPDDBHelper.get().getReadableDatabase().rawQuery(
            BASE_GIVING_SQL_EXTRA, null);
        values = new Float[cur.getCount()];
        Cursor cur3 = MPDDBHelper.get().getReadableDatabase().rawQuery(
            REGULAR_NONMONTHLY_GIVING, null);
        Cursor cur4 = MPDDBHelper.get().getReadableDatabase().rawQuery(
            SPECIAL_SQL, null);

        vals = new Float[cur1.getCount()][3];
        cur1.moveToFirst();
        cur2.moveToFirst();
        cur3.moveToFirst();
        cur4.moveToFirst();
        for (int i = 0; i < cur1.getCount(); i++){
            vals[i][0] = ((float) cur1.getInt(1) + cur2.getInt(1)) / 100f;
            vals[i][1] = ((float) cur3.getInt(2)) / 100f;
            vals[i][2] = ((float) cur4.getInt(1)) / 100f;
            cur1.moveToNext();
            cur2.moveToNext();
            cur3.moveToNext();
            cur4.moveToNext();
        }
        cur1.close(); cur2.close(); cur3.close(); cur4.close();
        graph = (BarGraph) getView().findViewById(R.id.magic_graph);
        graph.setValues(vals);
	}
}
