package net.bradmont.openmpd.fragments;

import net.bradmont.supergreen.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.TntImportService;
import net.bradmont.openmpd.controllers.TntImporter;

import android.content.Context;
import android.content.Intent;

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
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.util.ArrayList;



public class DebugFragment extends SherlockFragment implements OnClickListener{

    public static final String [] columns = {"msg1", "msg2", "msg3"};
    public static final int [] fields = {R.id.msg1, R.id.msg2, R.id.msg3};


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView( LayoutInflater inflater, 
            ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.debug_page, null);
        setHasOptionsMenu(true);
        Cursor cursor = MPDDBHelper.get()
                   .getReadableDatabase()
                   .rawQuery("select * from log order by _id desc;", null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.error_list_item, cursor, columns, fields);
        ListView lv = (ListView) view.findViewById(R.id.list);
        lv.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.debug, menu);
    }

    @Override
    public boolean onOptionsItemSelected (com.actionbarsherlock.view.MenuItem item){
        switch (item.getItemId() ){
            case R.id.menu_refresh:
                Log.i("net.bradmont.openmpd", "menu_refresh");
                ModelList accounts = MPDDBHelper
                    .get()
                    .getReferenceModel("service_account")
                    .getAll();
                int [] account_ids = new int [accounts.size()];
                for (int i = 0; i < accounts.size(); i++){
                    account_ids[i] = accounts.get(i).getID();
                }

                getActivity().startService(
                    new Intent(getActivity(), TntImportService.class)
                        .putExtra("net.bradmont.openmpd.account_ids", account_ids)
                        .putExtra("net.bradmont.openmpd.force_update", true)

                    );

                return true;
        }
        return false;
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.clear_data_button:
                String [] tables = { "address", "contact_status", "email_address", "gift", "notification", "phone_number", "contact" };
                for (String table : tables){
                    MPDDBHelper.get().getWritableDatabase()
                        .execSQL(String.format("delete from %s;", table));
                }
                MPDDBHelper.get().getWritableDatabase()
                    .execSQL(String.format("update service_account set last_import = null;"));
                break;
            case R.id.randomise_data_button:
                // Replace all contact info with fake data, for public
                // screenshots and such.

                // read sample data
                InputStream inputStream = getActivity().getResources().openRawResource(R.raw.example_names);
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                ArrayList<String> fake_data = new ArrayList<String>(500);
                try {
                    line = buffreader.readLine(); // drop first row
                    while (( line = buffreader.readLine()) != null) {
                        fake_data.add(line);
                    }
                } catch (IOException e){
                    return;
                }


                // get all contacts
                ModelList contacts = MPDDBHelper.getReferenceModel("contact").getAll();
                for (int i = 0; i < contacts.size(); i++){
                    int index = i % fake_data.size();
                    String [] info = TntImporter.csvLineSplit(fake_data.get(index));
                    Contact c = (Contact) contacts.get(index);
                    c.setValue("fname", info[0]);
                    c.setValue("lname", info[1]);
                    c.dirtySave();
                    try {
                    EmailAddress email = (EmailAddress) MPDDBHelper
                        .getModelByField("email_address", "contact_id", c.getInt("id"));
                        email.setValue("address", info[9]);
                        email.dirtySave();
                    } catch (Exception e){}
                    try {
                    PhoneNumber phone = (PhoneNumber) MPDDBHelper
                        .getModelByField("phone_number", "contact_id", c.getInt("id"));
                        phone.setValue("number", info[7]);
                        phone.dirtySave();
                    } catch (Exception e){}
                    try {
                    Address address = (Address) MPDDBHelper
                        .getModelByField("address", "contact_id", c.getInt("id"));
                        address.setValue("addr1", info[3]);
                        address.setValue("addr2", "");
                        address.setValue("addr3", "");

                        address.setValue("city", info[4]);
                        address.setValue("region", info[5]);
                        address.setValue("post_code", info[6]);

                        address.dirtySave();
                    } catch (Exception e){}
                }
                break;
        }
    }

}
