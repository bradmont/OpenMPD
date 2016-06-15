package net.bradmont.openmpd.fragments;

import net.bradmont.supergreen.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.activities.ContactDetailActivity;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.openmpd.controllers.TntImportService;
import net.bradmont.openmpd.models.*;
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

public class ContactSublistFragment extends ListFragment {
    public static final String [] columns = {"fname", "partner_type", "giving_amount", "last_gift", "fname", "fname", "s_fname", "partner_type"};
    public static final int [] fields = {R.id.name, R.id.status, R.id.amount, R.id.last_gift, R.id.initials, R.id.user_icon_right, R.id.user_icon_left, R.id.type};

    private Cursor cursor = null;

    private EnhancedListView mListView = null;
    private EnhancedListAdapter mAdapter;
    private String mListName = "";

    //private SimpleCursorAdapter adapter = null;
    private static int[] icon_colors = null;
    private static final String BASE_QUERY = "select A.fname as fname, A.lname as lname, " +
                "B.fname as s_fname, A._id, C.partner_type, C.giving_amount, C.status, C.gift_frequency, C.last_gift  from " + 
                "contact A left outer join contact B on A._id = B.spouse_id " +
                "left outer join contact_status C on A._id=c.contact_id where A.primary_contact = 1 " +
                "and A._id in (select contact_id from contact_sublist where list_name = ?) "+
                "order by (status = 4) desc, status desc, partner_type desc, A.lname, A.fname";

    private static final String ALL_CONTACTS_QUERY =
                "SELECT _id, ? FROM contact WHERE primary_contact = 1";

    private static final String STATUS_QUERY =
                "SELECT contact._id, ? from contact left outer join contact_status " +
                "on contact._id = contact_status.contact_id " +
                "where contact.primary_contact = 1 and contact_status.status = ?" ;
        
    private static final String OCCASIONAL_QUERY = 
                "SELECT contact._id, ? FROM contact LEFT OUTER JOIN contact_status " +
                "ON contact._id = contact_status.contact_id " +
                "WHERE contact.primary_contact = 1 AND (partner_type = 30 OR partner_type = 20)" ;

    private static final String SEARCH_QUERY = "select A.fname as fname, A.lname as lname, " +
                "B.fname as s_fname, A._id, C.partner_type, C.giving_amount, C.status, C.gift_frequency, "+
                "C.last_gift , A.fname || ' ' || A.lname as full_name, B.fname || ' ' || B.lname as spouse_full_name " +
                "from contact A left outer join contact B on A._id = B.spouse_id " +
                "left outer join contact_status C on A._id=c.contact_id where A.primary_contact = 1 " +
                "and A._id in (select contact_id from contact_sublist where list_name = ?) "+
                "and (full_name like '%' || ? ||'%' or spouse_full_name like '%' || ? ||'%') "+
                "order by (status = 4) desc, status desc, partner_type desc, A.lname, A.fname";
        
    /*private static final String SEARCH_QUERY = "select A.fname as fname, A.lname as lname, " +
                "B.fname as s_fname, A._id, C.partner_type, C.giving_amount, C.status, C.gift_frequency, "+
                "C.last_gift, A.fname || ' ' || A.lname as full_name, B.fname || ' ' || B.lname as spouse_full_name  from " + 
                "contact A left outer join contact B on A._id = B.spouse_id " +
                "left outer join contact_status C on A._id=c.contact_id where A.primary_contact = 1 " +
                "and (full_name like '%' || ? ||'%' or spouse_full_name like '%' || ? ||'%') "+
                "order by (status = 4) desc, status desc, partner_type desc, A.lname, A.fname";
                */

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        mListView = (EnhancedListView)  inflater.inflate(R.layout.enhancedlist, null);

        return mListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        icon_colors = getActivity().getResources().getIntArray(R.array.user_icon_colors);

        // set up adapter
        String [] args = new String[1]; args[0] = mListName;
        cursor = OpenMPD.getDB().rawQuery(BASE_QUERY, args);
        mAdapter = new EnhancedListAdapter(getActivity(),
            R.layout.contact_list_item, cursor, columns, fields);
        mAdapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView tv = null;
                String value = "";
                switch(view.getId()){
                    // 0: fname, 1:lname, 2:s_name, 3:_id, 4:partner_type, 5:giving_amount, 6: status, 7: gift_frequency, 8: last_gift
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
                        int type = cursor.getInt(columnIndex);
                        int status = cursor.getInt(cursor.getColumnIndex("status"));
                        tv = (TextView) view;
                        if (cursor.getPosition() ==0){
                            view.setVisibility(View.VISIBLE);
                            if (ContactStatus.getStatusStringRes(status) != R.string.current &&
                                    ContactStatus.getStatusStringRes(status) != R.string.none){
                                value = getActivity().getResources()
                                    .getString(ContactStatus.getStatusStringRes(status)) + " "; 
                            }
                            value += getActivity().getResources()
                                .getString(ContactStatus.getTypeStringRes(type));
                            if (ContactStatus.getTypeStringRes(type) != R.string.none){
                                value += " " + getActivity().getResources().getString(R.string.partners);
                            }
                            tv.setText(value);
                        } else {
                            cursor.moveToPrevious();
                            if (type != cursor.getInt(columnIndex) ||
                                    status != cursor.getInt(cursor.getColumnIndex("status"))){
                                view.setVisibility(View.VISIBLE);
                                if (ContactStatus.getStatusStringRes(status) != R.string.current &&
                                        ContactStatus.getStatusStringRes(status) != R.string.none){
                                    value = getActivity().getResources()
                                        .getString(ContactStatus.getStatusStringRes(status)) + " "; 
                                }
                                value += getActivity().getResources()
                                    .getString(ContactStatus.getTypeStringRes(type));
                                if (ContactStatus.getTypeStringRes(type) != R.string.none){
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
                        status = cursor.getInt(cursor.getColumnIndex("status"));
                        type = cursor.getInt(cursor.getColumnIndex("partner_type"));
                        tv = (TextView) view;
                        String text = getActivity().getResources()
                            .getString(ContactStatus.partnership(type));
                        // replace ? with giving frequency (for REGULAR donors)
                        if (cursor.getString(7) != null){
                            text = text.replace("?", cursor.getString(7));
                        }
                        tv.setText(text);
                        if (status == ContactStatus.STATUS_LATE ||
                                status == ContactStatus.STATUS_LAPSED ||
                                status == ContactStatus.STATUS_DROPPED){
                            tv.setTextColor(ContactStatus.STATUS_COLORS[status]);
                        } else {
                            tv.setTextColor(getActivity().getResources()
                                    .getColor(ContactStatus.getTypeColorRes(type)));
                        }
                        return true;
                    case R.id.amount:
                        // amount
                        status = cursor.getInt(cursor.getColumnIndex("status"));
                        type = cursor.getInt(cursor.getColumnIndex("partner_type"));
                        tv = (TextView) view;
                        if (cursor.getInt(5) == 0){
                            tv.setText("");
                        } else {
                            tv.setText(" $" + Integer.toString(cursor.getInt(5)/100));
                        }
                        if (status == ContactStatus.STATUS_LATE ||
                                status == ContactStatus.STATUS_LAPSED ||
                                status == ContactStatus.STATUS_DROPPED){
                            tv.setTextColor(ContactStatus.STATUS_COLORS[status]);
                        } else {
                            tv.setTextColor(getActivity().getResources()
                                    .getColor(ContactStatus.getTypeColorRes(type)));
                        }
                        return true;
                    case R.id.last_gift:
                        tv = (TextView) view;
                        String date = cursor.getString(8);
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

        //setListAdapter(adapter);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new ContactListClickListener());
        mListView.setSwipingLayout(R.id.card);
        mListView.setDismissCallback(new ContactDismissCallback());

        mListView.enableSwipeToDismiss();
        mListView.setSwipeDirection(EnhancedListView.SwipeDirection.BOTH);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sublist, menu);
        
        // set up search
        SearchView searchView = new
                SearchView( ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext());
        //searchView.setQueryHint(getString(R.string.hint_search_bar));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            public boolean onQueryTextChange(String newText){
                String [] args = new String[2];
                args[0] = mListName;
                args[1] = newText;
                // filter on a concatenation of contacts's names, to avoid really complex SQL...
                Cursor newCursor = OpenMPD.getDB().rawQuery(SEARCH_QUERY, args);
                mAdapter.changeCursor(newCursor);
                cursor.close();
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
                    Cursor newCursor = OpenMPD.getDB().rawQuery(BASE_QUERY, new String [] {mListName} );
                    mAdapter.changeCursor(newCursor);
                    cursor.close();
                    cursor = newCursor;
                    return true;
                }
            });
    }
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

    public void addContacts(View v){
          PopupMenu popup = new PopupMenu(getActivity(), v);
          MenuInflater inflater = popup.getMenuInflater();
          inflater.inflate(R.menu.sublist_add_contacts, popup.getMenu());
          popup.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener(){
              @Override
              public boolean   onMenuItemClick(MenuItem item) {
                  String [] args = new String [] { mListName };
                  String query = STATUS_QUERY;
                  switch (item.getItemId()){
                      case R.id.menu_filter_all:
                          query = ALL_CONTACTS_QUERY ;
                          break;
                      case R.id.menu_filter_new:
                          args = new String [] { mListName , Integer.toString(ContactStatus.STATUS_NEW)};
                          break;
                      case R.id.menu_filter_current:
                          args = new String [] { mListName , Integer.toString(ContactStatus.STATUS_CURRENT)};
                          break;
                      case R.id.menu_filter_late:
                          args = new String [] { mListName , Integer.toString(ContactStatus.STATUS_LATE)};
                          break;
                      case R.id.menu_filter_lapsed:
                          args = new String [] { mListName , Integer.toString(ContactStatus.STATUS_LAPSED)};
                          break;
                      case R.id.menu_filter_dropped:
                          args = new String [] { mListName , Integer.toString(ContactStatus.STATUS_DROPPED)};
                          break;
                      case R.id.menu_filter_occasional:
                          query = OCCASIONAL_QUERY;
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
