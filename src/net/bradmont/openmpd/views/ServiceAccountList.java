package net.bradmont.openmpd.views;

import net.bradmont.supergreen.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.openmpd.controllers.TntImporter;

import android.content.Context;

import android.database.sqlite.*;
import android.database.Cursor;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

import java.lang.Runnable;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class ServiceAccountList extends SherlockListFragment {
    public static final String [] columns = {"name", "username", "last_import"};
    public static final int [] fields = {R.id.name, R.id.username, R.id.last_import};

    private SQLiteDatabase db_read = MPDDBHelper.get()
            .getReadableDatabase();
    private Cursor cursor = null;

    private LinearLayout header = null;
    private SimpleCursorAdapter adapter = null;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        ListView lv = (ListView)  inflater.inflate(R.layout.list, null);
        LayoutInflater layoutInflater = (LayoutInflater)getActivity()
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        //header = (LinearLayout)layoutInflater
                //.inflate( R.layout.contact_list_header, null, false );
        //lv.addHeaderView( header );

        setHasOptionsMenu(true);
        return lv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final OpenMPD app = (OpenMPD)getActivity();


        // set up adapter
        cursor = db_read.rawQuery("select service_account._id, name, username, last_import from service_account join tnt_service on tnt_service_id=tnt_service._id", null);
        adapter = new SimpleCursorAdapter(getActivity(),
            R.layout.service_account_list_item, cursor, columns, fields);

        adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView tv = (TextView) view;
                switch(columnIndex){
                    case 3:
                        if (cursor.getString(3) == null){
                            tv.setText(R.string.never);
                            return true;
                        }
                        return false;
                }
                return false;
            }
        });

        setListAdapter(adapter);

    }
    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.serviceaccountlist, menu);
    }
    @Override
    public boolean onOptionsItemSelected (com.actionbarsherlock.view.MenuItem item){
        switch (item.getItemId() ){
            case R.id.menu_add:
                Log.i("net.bradmont.openmpd", "menu_add");
                EditServiceAccountDialog dialog = new EditServiceAccountDialog();
                dialog.setAdapter((CursorAdapter) getListAdapter());
                dialog.show(getFragmentManager(), "edit_account_dialog");
                return true;
        }
        return false;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("net.bradmont.openmpd", String.format("Item clicked: %d", id));
        EditServiceAccountDialog dialog = new EditServiceAccountDialog();
        dialog.setAdapter((CursorAdapter) getListAdapter());
        dialog.show(getFragmentManager(), "edit_account_dialog");
        dialog.setAccount(new ServiceAccount((int)id));
        //final OpenMPD app = (OpenMPD)getActivity();
        //app.moveToFragment(new ContactDetail((int) id));
    }
}
