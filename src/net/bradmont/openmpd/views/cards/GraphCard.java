package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.holograph.BarGraph;

import android.content.Context;
import android.database.Cursor;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fima.cardsui.objects.Card;

/**
 * Bug (TODO): if a non-monthly regular donor gives an extra gift in a
 * month they also make a regular donation, their regular donation will
 * be included in the monthly base rather than the regular base
 */
public class GraphCard extends Card {

    // monthly giving, where a month's total gifts == donor's regular giving
    // eg, if they gave an extra gift, their entire giving will be excluded
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

    private static View content = null; // so we can invalidate the view from
                                        // clearCache. We should only ever
                                        // have one instance anyway.
	public GraphCard(){
		super();
	}

	@Override
	public View getCardContent(Context context) {
        if (content == null){
            content = buildView(context);
            return content;
        }
        if (content.getParent() != null){
            ((ViewGroup)content.getParent()).removeView(content);
        }
        return content;
	}

	public View buildView(Context context) {
        // TODO: cursors may not return same number of rows. Deal with this gracefully
		View view = LayoutInflater.from(context).inflate(R.layout.card_graph, null);

		((TextView) view.findViewById(R.id.title)).setText(R.string.giving_summary);
        BarGraph graph = (BarGraph) view.findViewById(R.id.magic_graph);

        // magic graph
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
		
		return view;
	}

    public boolean verifyCache(){
        Cursor cur1 = MPDDBHelper.get().getReadableDatabase().rawQuery(VERIFY_CACHE_SQL, null);
        boolean result = (cur1.getCount() == 1);
        cur1.close();
        return result;
    }
    public void createCache(){
        MPDDBHelper.get().getWritableDatabase().execSQL(CACHE_SQL);
        verifyCache();
    }
    public static void clearCache(){
        Log.i("net.bradmont.openmpd", "Clearing cache.");
        MPDDBHelper.get().getWritableDatabase().execSQL(CLEAR_CACHE_SQL);
        content = null;
    }
}
