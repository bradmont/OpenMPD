package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.holograph.BarGraph;

import android.content.Context;
import android.database.Cursor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

/**
 * Bug (TODO): if a non-monthly regular donor gives an extra gift in a
 * month they also make a regular donation, their regular donation will
 * be included in the monthly base rather than the regular base
 */
public class GraphCard extends Card {

    // monthly giving, where a month's total gifts == donor's regular giving
    // eg, if they gave an extra gift, their entire giving will be excluded
    private static final String GIVING_SQL =
        "select month.month, base_giving/100, regular_giving/100, special_gifts/100 from "+
        "    month left outer join monthly_base_giving A "+
        "        on month.month=A.month"+
        "        left outer join regular_by_month B"+
        "            on month.month=B.month"+
        "        left outer join special_gifts_by_month C"+
        "            on month.month=C.month"+
        "    order by month.month;";

    private View content = null;
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

        Cursor cur1 = MPDDBHelper.get().getReadableDatabase().rawQuery(
            GIVING_SQL, null);

        vals = new Float[cur1.getCount()][3];
        cur1.moveToFirst();
        for (int i = 0; i < cur1.getCount(); i++){
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
            cur1.moveToNext();
        }
        cur1.close(); 
        graph.setValues(vals);
		
		return view;
	}

	
	
	
}
