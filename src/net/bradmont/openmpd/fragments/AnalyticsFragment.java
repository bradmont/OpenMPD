package net.bradmont.openmpd.fragments;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.R;
import net.bradmont.openmpd.views.HelpDialog;
import net.bradmont.openmpd.views.Analytics;
import net.bradmont.holograph.BarGraph;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class AnalyticsFragment extends Fragment {

    private static final String CLEAR_CACHE_SQL =
        "drop table if exists giving_summary_cache;";
    private static final String VERIFY_CACHE_SQL =
        "SELECT name FROM sqlite_master WHERE type='table' AND name='giving_summary_cache';";
    private static final String CACHE_SQL =
        "create table giving_summary_cache as " +
        "select month.month as month, base_giving/100 as base, " +
        "regular_giving/100 as regular, frequent_giving/100 as frequent, "+
        "special_gifts/100 as special from "+
        "    month left outer join monthly_base_giving A "+
        "        on month.month=A.month"+
        "        left outer join regular_by_month B"+
        "            on month.month=B.month"+
        "        left outer join frequent_by_month C"+
        "            on month.month=C.month"+
        "        left outer join special_gifts_by_month D"+
        "            on month.month=D.month"+
        "    order by month.month;";
    private static final String GIVING_SQL =
        "select * from giving_summary_cache;";


    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.analytics, null);

        ((TextView) view.findViewById(R.id.twelve_month_average))
            .setText(Analytics.getAverage());
        ((TextView) view.findViewById(R.id.weighted_average))
            .setText(Analytics.getWeightedAverage());

        ((TextView) view.findViewById(R.id.monthly))
            .setText(Analytics.getStableMonthly());
        ((TextView) view.findViewById(R.id.quarterly_and_annual))
            .setText(Analytics.getStableRegular());

        ((TextView) view.findViewById(R.id.repeating_irregular))
            .setText(Analytics.getFrequentAverage());
        ((TextView) view.findViewById(R.id.avg_special))
            .setText(Analytics.getAverageSpecial());

        ((TextView) view.findViewById(R.id.late))
            .setText(Analytics.getLateAverage());
        ((TextView) view.findViewById(R.id.lapsed))
            .setText(Analytics.getLapsedAverage());

        ((TextView) view.findViewById(R.id.dropped_support))
            .setText(Analytics.getDroppedTotal());
        ((TextView) view.findViewById(R.id.ongoing_total))
            .setText(Analytics.getOngoingTotal());


        populateGraph(view);
        return view;
    }

    private View populateGraph(View parent){

        // set up bar grap
        BarGraph graph = (BarGraph) parent.findViewById(R.id.magic_graph);
        Float [] [] vals = null;

        if (!verifyCache()){
            createCache();
        }
        Cursor cur1 = MPDDBHelper.get().getReadableDatabase().rawQuery(
            GIVING_SQL, null);

        vals = new Float[cur1.getCount()][4];
        String [] labels = new String[cur1.getCount()];
        String [] groups = new String[cur1.getCount()];
        String [] parts = null;
        cur1.moveToFirst();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM");
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < cur1.getCount(); i++){
            parts = cur1.getString(0).split("-");
            groups[i] = parts[0];
            cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) -1);
            labels[i] = dateFormat.format(cal.getTime());
            try {
                vals[i][0] = cur1.getFloat(1);
            } catch (Exception e){
                vals[i][0]=0f;
            }
            try {
                vals[i][1] = cur1.getFloat(2);
            } catch (Exception e){
                vals[i][1]=0f;
            }
            try {
                vals[i][2] = cur1.getFloat(3);
            } catch (Exception e){
                vals[i][2]=0f;
            }
            try {
                vals[i][3] = cur1.getFloat(4);
            } catch (Exception e){
                vals[i][3]=0f;
            }
            cur1.moveToNext();
        }
        cur1.close();
        graph.setLabels(labels);
        graph.setValues(vals);
        graph.setGroups(groups);

        return parent;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        //addCards();
    }
    @Override
    public void onResume(){
        super.onResume();
        // set title
        ((FragmentActivity) getActivity()).getActionBar()
            .setTitle(R.string.app_name);
        ((FragmentActivity) getActivity()).getActionBar()
            .setSubtitle(null);
    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId() ){
            case R.id.menu_help:
                HelpDialog.showHelp(getActivity(), R.string.help_main_title, R.string.help_main);
            return true;
        }
        return false;
    }

    public void showUpdateNews(final int version){
        final SharedPreferences prefs = getActivity().getSharedPreferences("openmpd", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.update_news)
            .setTitle(R.string.whats_new);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                prefs.edit()
                    .putInt("updateNews", version)
                      .apply();
            }
        });
        builder.show();

    }
    public boolean verifyCache(){
        Cursor cur1 = MPDDBHelper.get().getReadableDatabase().rawQuery(VERIFY_CACHE_SQL, null);
        boolean result = (cur1.getCount() == 1);
        cur1.close();
        return result;
    }
    public static void createCache(){
        MPDDBHelper.get().getWritableDatabase().execSQL(CACHE_SQL);
    }
    public static void clearCache(){
        MPDDBHelper.get().getWritableDatabase().execSQL(CLEAR_CACHE_SQL);
    }


}
