package net.bradmont.openmpd.fragments;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.activities.ContactDetailActivity;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.openmpd.controllers.TntImportService;
import net.bradmont.openmpd.views.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;

import android.database.sqlite.*;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import net.bradmont.openmpd.helpers.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;
import android.widget.PopupMenu;

import net.bradmont.openmpd.views.EnhancedListView;



import java.lang.Runnable;
import java.util.Arrays;

public class ContactSublistFragment extends ContactListFragment {

    private String mListName = "";
	private EnhancedListAdapter mAdapter;

    public static final String LIST_BASE_QUERY = 
                "select " + FIELDS +
                "from (" + CONTACT_COUPLE_SUBQUERY + ") A "+
                "   left outer join contact_status " +
                "   on _contact_id = contact_status.contact_id "+
				"where _contact_id in (select contact_id from contact_sublist where list_name = ?) "+
                "order by " + ORDER;

    private static final String ALL_CONTACTS_QUERY =
                "SELECT _id, ? FROM contact ";

    private static final String LOAD_CONTACTS_BY_STATUS_QUERY =
                "SELECT contact._id, ? from contact left outer join contact_status " +
                "on contact._id = contact_status.contact_id " +
                "where contact_status.status = ?" ;
        
    private static final String LOAD_CONTACTS_BY_OCCASIONAL_QUERY = 
                "SELECT contact._id, ? FROM contact LEFT OUTER JOIN contact_status " +
                "ON contact._id = contact_status.contact_id " +
                "WHERE (partner_type = 'onetime' OR partner_type = 'occasional')" ;

    protected static final String LIST_SEARCH_QUERY = 
                "select " + FIELDS +
                ", fname || ' ' || lname as full_name, s_fname || ' ' || s_lname as spouse_full_name  "+
                "from (" + CONTACT_COUPLE_SUBQUERY + ") A "+
                "   left outer join contact_status " +
                "   on _contact_id = contact_status.contact_id "+
				"where _contact_id in (select contact_id from contact_sublist where list_name = ?) "+
                "and (full_name like '%' || ? ||'%' or spouse_full_name like '%' || ? ||'%') " +
                "order by " + ORDER;
        

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId() ){
            case R.id.delete:
			new AlertDialog.Builder(getActivity())
				   .setMessage(R.string.are_you_sure)
				   .setCancelable(false)
				   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int id) {
							OpenMPD.getDB().execSQL(
									"DELETE FROM contact_sublist WHERE list_name = ?",
									new String [] {mListName});
							getActivity().finish();
					   }
				   })
				   .setNegativeButton(R.string.cancel, null)
				   .show();

			return true;
        }
        return false;
    }

    private class ContactDismissCallback implements EnhancedListView.OnDismissCallback{
        @Override
        public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
            final int deleted_id = mAdapter.remove(position);
            return new EnhancedListView.Undoable(){
                @Override
                public void undo(){
                    mAdapter.insert(deleted_id);
                }
            };
        }
    }
    private class EnhancedListAdapter extends SimpleCursorAdapter{
        public EnhancedListAdapter(Context context, int layout, Cursor c, String[] from, int[] to){
            super(context, layout, c, from, to);
        }

        public int remove(int position){
            getCursor().moveToPosition(position);
            int contactId = getCursor().getInt(3);
            OpenMPD.getDB().execSQL(
                    "DELETE FROM contact_sublist WHERE contact_id = ? AND list_name = ?",
                    new String [] {Integer.toString(contactId), mListName});

            cursor.requery();
            return contactId;
        }

        public void insert(int deleted_id){
            OpenMPD.getDB().execSQL(
                    "INSERT INTO contact_sublist VALUES (?, ?)" , 
                    new String [] { Integer.toString(deleted_id), mListName });
            cursor.requery();
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        // set title
    }


    public void setListName(String listName){
        mListName = listName;
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(
        getActivity().getResources().getString(R.string.custom_list_title) + listName);
        // TODO: proper resource
        if (mAdapter != null){
            Cursor newCursor = OpenMPD.getDB().rawQuery(BASE_QUERY, new String [] {mListName});
            mAdapter.changeCursor(newCursor);
            cursor.close();
            cursor = newCursor;
        }
    }

    public String getListName(){
        return mListName;
    }
    protected String getBaseQuery(){
        return LIST_BASE_QUERY;
    }
    protected String getSearchQuery(){
        return LIST_SEARCH_QUERY;
    }


    public void addContacts(View v){
          PopupMenu popup = new PopupMenu(getActivity(), v);
          MenuInflater inflater = popup.getMenuInflater();
          inflater.inflate(R.menu.sublist_add_contacts, popup.getMenu());
          popup.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener(){
              @Override
              public boolean   onMenuItemClick(MenuItem item) {
                  String [] args = new String [] { mListName };
                  String query = LOAD_CONTACTS_BY_STATUS_QUERY;
                  switch (item.getItemId()){
                      case R.id.menu_filter_all:
                          query = ALL_CONTACTS_QUERY ;
                          break;
                      case R.id.menu_filter_new:
                          args = new String [] { mListName , "new"};
                          break;
                      case R.id.menu_filter_current:
                          args = new String [] { mListName , "current"};
                          break;
                      case R.id.menu_filter_late:
                          args = new String [] { mListName , "late"};
                          break;
                      case R.id.menu_filter_lapsed:
                          args = new String [] { mListName , "lapsed"};
                          break;
                      case R.id.menu_filter_dropped:
                          args = new String [] { mListName , "dropped"};
                          break;
                      case R.id.menu_filter_occasional:
                          query = LOAD_CONTACTS_BY_OCCASIONAL_QUERY;
                          break;
                  }
                  query = "insert into contact_sublist " + query;
                  OpenMPD.getDB().execSQL(query, args);
                  cursor.requery();
                  return true;
              }
              });
          popup.show();


    }
}
