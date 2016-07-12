package net.bradmont.openmpd.fragments;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.activities.ContactDetailActivity;
import net.bradmont.openmpd.activities.ContactSublistActivity;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.openmpd.controllers.TntImportService;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.views.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.sqlite.*;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import net.bradmont.openmpd.helpers.Log;
import net.bradmont.openmpd.helpers.TextTools;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.app.ActionBar;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;


import java.lang.Runnable;

public class ContactListFragment extends ListFragment {
    public static final String [] columns = {"fname", "status", "giving_amount", "last_gift", "fname", "fname", "s_fname", "type"};
    public static final int [] fields = {R.id.name, R.id.status, R.id.amount, R.id.last_gift, R.id.initials, R.id.user_icon_right, R.id.user_icon_left, R.id.type};

    private Cursor cursor = null;

    private ListView mListView = null;

    private SimpleCursorAdapter adapter = null;
    private static int[] icon_colors = null;
    private static final String CONTACT_COUPLE_SUBQUERY =
				"select distinct A.contact_id as _contact_id, fname, lname, s_fname, s_lname from " +

 				"(select distinct * from contact join person on person.contact_id =contact._id "+
				"	where IS_CONTACT_PRIMARY = 1) A "+
 				"left outer join " +
				"(select contact_id, fname as s_fname, lname as s_lname "+
				"	from person where IS_TNT_SPOUSE = 1) B "+ 
 				"on A.contact_id = B.contact_id ";

    private static final String FIELDS = 
                "fname, lname, s_fname, s_lname, _contact_id as _id, TYPE as type, " +
                "GIVING_AMOUNT as giving_amount, STATUS as status, GIVING_FREQUENCY "+
                "as giving_frequency, LAST_GIFT as last_gift, MANUAL_SET_EXPIRES as manual_set_expires ";

    private static final String ORDER = "status != 'new', status != 'current', type !='monthly', " +
                "   type !='regular', type !='annual', status desc, type desc, lname, fname";

    public static final String BASE_QUERY = 
                "select " + FIELDS +
                "from (" + CONTACT_COUPLE_SUBQUERY + ") A "+
                "   left outer join contact_status " +
                "   on _contact_id = contact_status.contact_id "+
                "order by " + ORDER;


    private static final String STATUS_QUERY = 
                "select " + FIELDS +
                "from (" + CONTACT_COUPLE_SUBQUERY + ") A "+
                "   left outer join contact_status " +
                "   on _contact_id = contact_status.contact_id "+
                "where status=? " +
                "order by type desc, lname, fname";

    private static final String OCCASIONAL_QUERY = 
                "select " + FIELDS +
                "from (" + CONTACT_COUPLE_SUBQUERY + ") A "+
                "   left outer join contact_status " +
                "   on _contact_id = contact_status.contact_id "+
                "where type='onetime' or type='occasional' " +
                "order by (status = 'new') desc, status desc, type desc, lname, fname";


    private static final String SEARCH_QUERY = 
                "select " + FIELDS +
                ", fname || ' ' || lname as full_name, s_fname || ' ' || s_lname as spouse_full_name  "+
                "from (" + CONTACT_COUPLE_SUBQUERY + ") A "+
                "   left outer join contact_status " +
                "   on _contact_id = contact_status.contact_id "+
                "where (full_name like '%' || ? ||'%' or spouse_full_name like '%' || ? ||'%') " +
                "order by " + ORDER;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        mListView = (ListView)  inflater.inflate(R.layout.list, null);
        LayoutInflater layoutInflater = (LayoutInflater)getActivity()
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        return mListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        icon_colors = getActivity().getResources().getIntArray(R.array.user_icon_colors);

        // set up adapter
        cursor = OpenMPD.getDB().rawQuery(BASE_QUERY, null);
        TextTools.dumpCursorColumns(cursor);
        adapter = new SimpleCursorAdapter(getActivity(),
            R.layout.contact_list_item, cursor, columns, fields);
        adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView tv = null;
                String value = "";
                switch(view.getId()){
                    // 0: fname, 1:lname, 2:s_name, 3:_id, 4:type, 5:giving_amount, 6: status, 7: gift_frequency, 8: last_gift
                    // public static final int [] fields = {R.id.name, R.id.status, R.id.amount, R.id.last_gift, R.id.initials, R.id.user_icon_right, R.id.user_icon_left};
                    case R.id.name: 
                        tv = (TextView) view;
                        String temp = cursor.getString(1);
                        if (cursor.getString(0) != null && !cursor.getString(0).equals("")){
                            temp = temp +", "+ cursor.getString(0);
                        }
                        if (cursor.getString(2) != null){
                            temp = temp + " " +getActivity().getResources().getString(R.string._and_) + " " + cursor.getString(2);
                        }
                        tv.setText(temp);
                        return true;

                    case R.id.type:
                        String type = cursor.getString(columnIndex);
                        String status = cursor.getString(cursor.getColumnIndex("status"));
                        tv = (TextView) view;
                        if (cursor.getPosition() ==0){
                            view.setVisibility(View.VISIBLE);
                            if (!status.equals("current") && !status.equals("none")) {
                                value = status + " ";
                            }
                            value += type;
                            if (!type.equals("none")){
                                value += " " + getActivity().getResources().getString(R.string.partners);
                            }
                            tv.setText(value);
                        } else {
                            cursor.moveToPrevious();
                            if (!(type.equals( cursor.getString(columnIndex)) &&
                                    status.equals(cursor.getString(cursor.getColumnIndex("status"))))){
                                view.setVisibility(View.VISIBLE);
                                if (!status.equals("current") &&
                                        !status.equals("none")){
                                    value = status + " ";
                                }
                                value += type;
                                if (!type.equals("none")){
                                    value += " " + getActivity().getResources().getString(R.string.partners);
                                }
                                tv.setText(value);
                            } else {
                                view.setVisibility(View.GONE);
                            }
                            cursor.moveToNext();
                        }
                        return true;
                    case R.id.status:
                        // partner type
                        status = cursor.getString(cursor.getColumnIndex("status"));
                        type = cursor.getString(cursor.getColumnIndex("type"));
                        int gift_frequency = cursor.getInt(cursor.getColumnIndex("giving_frequency"));
                        tv = (TextView) view;
                        String text = type;
                        if (type.equals("monthly"))
                                text = getActivity().getResources().getString(R.string.per_month);
                        if (type.equals("annual"))
                                text = getActivity().getResources().getString(R.string.per_year);
                        if (type.equals("regular"))
                                text = getActivity().getResources().getString(R.string.per_n_months);
                        // TODO: change "type" to "/?mo"...
                        // replace ? with giving frequency (for REGULAR donors)
                        if (gift_frequency != 0 && gift_frequency != 1 && gift_frequency != 12){
                            text = text.replace("?",  Integer.toString(gift_frequency));
                        }
                        tv.setText(text);
                        if (status.equals("late")||
                                status.equals("lapsed")||
                                status.equals("dropped")){
                            tv.setTextColor(getStatusColour(status));
                        } else {
                            tv.setTextColor(getStatusColour(type));
                        }
                        return true;
                    case R.id.amount:
                        // amount
                        status = cursor.getString(cursor.getColumnIndex("status"));
                        type = cursor.getString(cursor.getColumnIndex("type"));
                        tv = (TextView) view;
                        int amount = cursor.getInt(cursor.getColumnIndex("giving_amount"));
                        if (type.equals("monthly") ||type.equals("regular") || type.equals("annual")){
                            tv.setText(" $" + Integer.toString(amount/100));
                        } else {
                            tv.setText("");
                        }
                        if (status.equals("late")||
                                status.equals("lapsed")||
                                status.equals("dropped")){
                            tv.setTextColor(getStatusColour(status));
                        } else {
                            tv.setTextColor(getStatusColour(type));
                        }
                        return true;
                    case R.id.last_gift:
                        tv = (TextView) view;
                        String date = cursor.getString(cursor.getColumnIndex("last_gift"));
                        if (date != null){
                            tv.setText(date.substring(0,7));
                        } else {
                            tv.setText(R.string.never);
                        }
                        return true;
                    case R.id.initials:
                        tv = (TextView) view;
                        try {
                            value += cursor.getString(cursor.getColumnIndex("fname")).substring(0,1);
                        } catch (Exception e){}
                        if (!cursor.isNull(cursor.getColumnIndex("s_fname"))){
                            try {
                                value += cursor.getString(cursor.getColumnIndex("s_fname")).substring(0,1);
                            } catch (Exception e){}
                        } else {
                            try {
                                value += cursor.getString(cursor.getColumnIndex("lname")).substring(0,1);
                            } catch (Exception e){}
                        }

                        tv.setText(value);
                        return true;
                    case R.id.user_icon_left :
                        value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                                cursor.getString(cursor.getColumnIndex("lname")) ;
                        ImageView iconView = (ImageView) view;
                        iconView.getDrawable().setColorFilter( getColor(value), Mode.MULTIPLY );

                        iconView.getDrawable().setLevel(5000);

                        return true;
                    case R.id.user_icon_right :
                        iconView = (ImageView) view;

                        if (!cursor.isNull(cursor.getColumnIndex("s_fname"))){
                            value = cursor.getString(cursor.getColumnIndex("s_fname")) + " " +
                                    cursor.getString(cursor.getColumnIndex("s_fname")) ;
                            String spouse_value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                                    cursor.getString(cursor.getColumnIndex("lname")) ;
                            iconView.getDrawable().setColorFilter( getColor(value, spouse_value), Mode.MULTIPLY );
                        } else {
                            value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                                    cursor.getString(cursor.getColumnIndex("lname")) ;
                            iconView.getDrawable().setColorFilter( getColor(value), Mode.MULTIPLY );
                        }
                        iconView.getDrawable().setLevel(5000);

                        return true;
                }
                return false;
            }
        });

        setListAdapter(adapter);
        mListView.setOnItemClickListener(new ContactListClickListener());

    }
    private int getStatusColour(String status){
        switch(status){
            case "late":
                return getActivity().getResources().getColor(R.color.late_partner);
            case "lapsed":
                return getActivity().getResources().getColor(R.color.lapsed_partner);
            case "dropped":
                return getActivity().getResources().getColor(R.color.dropped_partner);
            case "new":
                return getActivity().getResources().getColor(R.color.new_partner);
            case "monthly":
                return getActivity().getResources().getColor(R.color.monthly_partner);
            case "regular":
            case "annual":
                return getActivity().getResources().getColor(R.color.regular_partner);
            case "frequent":
                return getActivity().getResources().getColor(R.color.frequent_partner);
            case "special":
            case "onetime":
                return getActivity().getResources().getColor(R.color.special_partner);
        }
        return 0;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contact_list, menu);

        Cursor cur = OpenMPD.getDB().rawQuery("select distinct list_name from contact_sublist", null);
        cur.moveToFirst();
        while (!cur.isAfterLast()){
            String subListName = cur.getString(0);
            Menu subm = menu.findItem(R.id.menu_lists).getSubMenu();
            MenuItem item = subm.add(subListName);
            item.setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick (MenuItem item){
                    Intent intent = new Intent(getActivity(), ContactSublistActivity.class);
                    intent.putExtra("listName", item.getTitle().toString());
                    startActivity(intent);
                    return true;
                }

            });
            cur.moveToNext();
        }
        cur.close();

        // set up search
        SearchView searchView = new
                SearchView( ((BaseActivity)getActivity()).getSupportActionBar().getThemedContext());
        //searchView.setQueryHint(getString(R.string.hint_search_bar));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            public boolean onQueryTextChange(String newText){
                String [] args = new String[1];
                args[0] = newText;
                // filter on a concatenation of contacts's names, to avoid really complex SQL...
                Cursor newCursor = OpenMPD.getDB().rawQuery(SEARCH_QUERY, args);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            }
            public boolean  onQueryTextSubmit(String query){
                return false;
            }
        });
        menu.findItem(R.id.menu_search)
                .setActionView(searchView)
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setOnActionExpandListener(
                menu.findItem(R.id.menu_search),
                new MenuItemCompat.OnActionExpandListener(){
                // so we can reset the search when the searchView is closed
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    Cursor newCursor = OpenMPD.getDB().rawQuery(BASE_QUERY, null);
                    adapter.changeCursor(newCursor);
                    cursor = newCursor;
                    return true;
                }
            });
        
    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        String [] args = new String[1];
        Cursor newCursor = null;
        switch (item.getItemId() ){
            case R.id.menu_filter_all:
                newCursor = OpenMPD.getDB().rawQuery(BASE_QUERY, null);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            case R.id.menu_filter_new:
                args[0] = "new";
                newCursor = OpenMPD.getDB().rawQuery(STATUS_QUERY, args);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            case R.id.menu_filter_current:
                args[0] = "current";
                newCursor = OpenMPD.getDB().rawQuery(STATUS_QUERY, args);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            case R.id.menu_filter_late:
                args[0] = "late";
                newCursor = OpenMPD.getDB().rawQuery(STATUS_QUERY, args);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            case R.id.menu_filter_lapsed:
                args[0] = "lapsed";
                newCursor = OpenMPD.getDB().rawQuery(STATUS_QUERY, args);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            case R.id.menu_filter_dropped:
                args[0] = "dropped";
                newCursor = OpenMPD.getDB().rawQuery(STATUS_QUERY, args);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            case R.id.menu_filter_occasional:
                newCursor = OpenMPD.getDB().rawQuery(OCCASIONAL_QUERY, null);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            case R.id.menu_help:
                HelpDialog.showHelp(getActivity(), R.string.help_contact_list_title, R.string.help_contact_list);
            return true;
            case R.id.menu_list_new:
                makeNewList();
            return true;
        }
        return false;
    }

    private void makeNewList(){
        // ask for name for new list
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.name_list);
        final EditText input = new EditText(getActivity());
        builder.setView(input);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String new_list = input.getText().toString();
                // launch list
                Intent intent = new Intent(getActivity(), ContactSublistActivity.class);
                intent.putExtra("listName", new_list);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

        
    }


    @Override
    public void onResume(){
        super.onResume();
        // set title
    }
    /*
     * Color for a given string (name), ensuring it is not thhe same color
     * provided by value2
     */
    static int getColor(String value, String value2){
        int spouse_color = getColorIndex(value2, -1);
        return icon_colors[(spouse_color + (icon_colors.length/2)) % icon_colors.length];
    }

    static int getColor(String value){
        return icon_colors[getColorIndex(value, -1)];
    }

    static int getColorIndex(String value, int unwantedIndex){
        int total = 0;
        for (int i = 0; i < value.length(); i++){
            total += (int) value.charAt(i);
        }
        total = total % icon_colors.length;
        if (total == unwantedIndex){
            total = (total +1) % icon_colors.length;
        }
        return total;
    }



    private class ContactListClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i("net.bradmont.openmpd", "click");
            Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
            intent.putExtra("contactId", (int) id);
            startActivity(intent);
        }
    }
}
