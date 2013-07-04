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

import java.lang.Runnable;

public class ContactList extends ListFragment implements OnClickListener{
    public static final String [] columns = {"fname", "lname", "s_fname", "partner_type", "giving_amount"};
    public static final int [] fields = {R.id.fname, R.id.lname, R.id.spouse_fname, R.id.status, R.id.amount};

    private SQLiteDatabase db_read = MPDDBHelper.get()
            .getReadableDatabase();
    private Cursor cursor = null;

    private LinearLayout header = null;
    private SimpleCursorAdapter adapter = null;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        ListView lv = (ListView)  inflater.inflate(R.layout.list, null);
        LayoutInflater layoutInflater = (LayoutInflater)getActivity()
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        header = (LinearLayout)layoutInflater
                .inflate( R.layout.contact_list_header, null, false );

        lv.addHeaderView( header );
        return lv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final OpenMPD app = (OpenMPD)getActivity();


        // set up adapter
        cursor = db_read.rawQuery("select A.fname as fname, A.lname as lname, " +
                "B.fname as s_fname, A._id, C.partner_type, C.giving_amount, C.status, C.gift_frequency  from " + 
                "contact A left outer join contact B on A._id = B.spouse_id " +
                "left outer join contact_status C on A._id=c.contact_id where A.primary_contact = 1 " +
                "order by A.lname, A.fname", null);
        adapter = new SimpleCursorAdapter(getActivity(),
            R.layout.contact_list_item, cursor, columns, fields);
        adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView tv = (TextView) view;
                switch(columnIndex){
                    // 0: fname, 1:lname, 2:s_name, 3:_id, 4:partner_type, 5:giving_amount, 6: status, 7: gift_frequency
                    case 2:
                        if (cursor.getString(2) != null){
                            tv.setText(app.getResources().getString(R.string._and_) + " " + cursor.getString(2));
                        } else {
                            tv.setText("");
                        }
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
    public void onClick(View view){
        switch (view.getId()){
            case R.id.download_button:
                tntImport();
                break;
            case R.id.evaluate_button:
                evaluate();
                break;
        }
    }

    public void tntImport(){

        ModelList accounts = MPDDBHelper.getReferenceModel("service_account").getAll();
        for (int i = 0; i < accounts.size(); i++){
            getActivity().startService(new Intent(getActivity(), TntImportService.class).putExtra("net.bradmont.openmpd.account_id", accounts.get(i).getID()) );
        }

    }
    public void evaluate(){
        final OpenMPD app = (OpenMPD)getActivity();

        final ProgressBar pb = (ProgressBar) header.findViewById(R.id.progress_bar);
        final LinearLayout layout = (LinearLayout) header.findViewById(R.id.contacts_button_bar);
        final SimpleCursorAdapter cursorAdapter = adapter;
        // Show ProgressBar, hide buttons
        pb.setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);

        app.queueTask( new ContactsEvaluator(app, pb));

        // Queue a task to hide the progressbar once our task is done.
        app.queueTask( new Runnable(){
            public void run(){
                app.runOnUiThread(new Runnable(){
                    public void run(){
                        pb.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                        cursorAdapter.getCursor().requery();
                    }
                });
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final OpenMPD app = (OpenMPD)getActivity();
        app.moveToFragment(new ContactDetail((int) id));
    }
}
