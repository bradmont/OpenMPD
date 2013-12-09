package net.bradmont.openmpd.views;

import net.bradmont.supergreen.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.openmpd.controllers.TntImportService;

import android.content.Context;
import android.content.Intent;

import android.database.sqlite.*;
import android.database.Cursor;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;


import java.lang.Runnable;

public class ContactList extends SherlockListFragment implements OnClickListener{
    public static final String [] columns = {"fname", "partner_type", "giving_amount", "last_gift"};
    public static final int [] fields = {R.id.name, R.id.status, R.id.amount, R.id.last_gift};

    private SQLiteDatabase db_read = MPDDBHelper.get()
            .getReadableDatabase();
    private Cursor cursor = null;

    private SimpleCursorAdapter adapter = null;
    private static final String BASE_QUERY = "select A.fname as fname, A.lname as lname, " +
                "B.fname as s_fname, A._id, C.partner_type, C.giving_amount, C.status, C.gift_frequency, C.last_gift  from " + 
                "contact A left outer join contact B on A._id = B.spouse_id " +
                "left outer join contact_status C on A._id=c.contact_id where A.primary_contact = 1 " +
                "order by A.lname, A.fname";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        ListView lv = (ListView)  inflater.inflate(R.layout.list, null);
        LayoutInflater layoutInflater = (LayoutInflater)getActivity()
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        return lv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final OpenMPD app = (OpenMPD)getActivity();


        // set up adapter
        cursor = db_read.rawQuery(BASE_QUERY, null);
        adapter = new SimpleCursorAdapter(getActivity(),
            R.layout.contact_list_item, cursor, columns, fields);
        adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView tv = (TextView) view;
                switch(columnIndex){
                    // 0: fname, 1:lname, 2:s_name, 3:_id, 4:partner_type, 5:giving_amount, 6: status, 7: gift_frequency
                    case 0: 
                        String temp = cursor.getString(1);
                        if (cursor.getString(0) != null && !cursor.getString(0).equals("")){
                            temp = temp +", "+ cursor.getString(0);
                        }
                        if (cursor.getString(2) != null){
                            temp = temp + " " +app.getResources().getString(R.string._and_) + " " + cursor.getString(2);
                        }
                        tv.setText(temp);
                        return true;

                    case 4:
                        // partner type
                        String text = app.getResources()
                            .getString(ContactStatus.partnership(cursor.getInt(4)));
                        // replace ? with giving frequency (for REGULAR donors)
                        if (cursor.getString(7) != null){
                            text = text.replace("?", cursor.getString(7));
                        }
                        tv.setText(text);
                        tv.setTextColor(ContactStatus.STATUS_COLORS[cursor.getInt(6)]);
                        return true;
                    case 5:
                        // amount
                        if (cursor.getInt(5) == 0){
                            tv.setText("");
                        } else {
                            tv.setText(" $" + Integer.toString(cursor.getInt(5)/100));
                        }
                        tv.setTextColor(ContactStatus.STATUS_COLORS[cursor.getInt(6)]);
                        return true;
                }
                return false;
            }
        });

        setListAdapter(adapter);

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        SearchView searchView = new
                SearchView( ((OpenMPD)getActivity()).getSupportActionBar().getThemedContext());
        //searchView.setQueryHint(getString(R.string.hint_search_bar));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            public boolean onQueryTextChange(String newText){
                String [] args = new String[1];
                args[0] = newText;
                // filter on a concatenation of contacts's names, to avoid really complex SQL...
                Cursor newCursor = db_read.rawQuery("select A.fname as fname, A.lname as lname, " +
                "B.fname as s_fname, A._id, C.partner_type, C.giving_amount, C.status, C.gift_frequency, "+
                "C.last_gift, A.fname || ' ' || A.lname as full_name, B.fname || ' ' || B.lname as spouse_full_name  from " + 
                "contact A left outer join contact B on A._id = B.spouse_id " +
                "left outer join contact_status C on A._id=c.contact_id where A.primary_contact = 1 " +
                "and (full_name like '%' || ? ||'%' or spouse_full_name like '%' || ? ||'%') order by A.lname, A.fname", args);
                adapter.changeCursor(newCursor);
                cursor = newCursor;
                return true;
            }
            public boolean  onQueryTextSubmit(String query){
                return false;
            }
        });
        menu.add(R.string.Search)
                .setIcon(android.R.drawable.ic_menu_search)
                .setActionView(searchView)
                .setOnActionExpandListener(new MenuItem.OnActionExpandListener(){
                    // so we can reset the search when the searchView is closed
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        Cursor newCursor = db_read.rawQuery(BASE_QUERY, null);
                        adapter.changeCursor(newCursor);
                        cursor = newCursor;
                        return true;
                    }
                })
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM
                                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    }


    @Override
    public void onResume(){
        super.onResume();
        // set title
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar()
            .setTitle(R.string.app_name);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar()
            .setSubtitle(null);
    }

    @Override
    public void onClick(View view){
    }

    public void tntImport(){

        ModelList accounts = MPDDBHelper.getReferenceModel("service_account").getAll();
        for (int i = 0; i < accounts.size(); i++){
            getActivity().startService(new Intent(getActivity(), TntImportService.class).putExtra("net.bradmont.openmpd.account_id", accounts.get(i).getID()) );
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final OpenMPD app = (OpenMPD)getActivity();
        app.moveToFragment(new ContactDetail((int) id));
    }
}
