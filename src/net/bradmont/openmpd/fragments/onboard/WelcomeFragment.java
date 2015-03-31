package net.bradmont.openmpd.fragments.onboard;

import android.app.AlertDialog;

import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ServiceConnection;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ViewFlipper;


import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;

import mbanje.kurt.fabbutton.FabButton;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.activities.OnboardActivity;
import net.bradmont.openmpd.R;
import net.bradmont.openmpd.views.HelpDialog;
import net.bradmont.openmpd.helpers.Analytics;
import net.bradmont.holograph.BarGraph;
import net.bradmont.openmpd.views.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.openmpd.controllers.AccountVerifyService;



public class WelcomeFragment extends Fragment implements View.OnClickListener {

    private StringPicker mPicker = null;
    private NoScrollListView mAccountList = null;
    private SimpleCursorAdapter mAdapter = null;
    private int mSelectedService = -1;
    private static String [] service_defs = null;
    private static String [] service_names = null;
    private static String [] service_urls = null;
    private AccountVerifyService mAccountVerifyService = null;
    private boolean mServiceBound = false;

    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.onboard_flipper, null);

        ((TextView) view.findViewById(R.id.action_welcome_next))
            .setOnClickListener(this);
        ((TextView) view.findViewById(R.id.action_login_next))
            .setOnClickListener(this);
        ((TextView) view.findViewById(R.id.action_accounts_add))
            .setOnClickListener(this);
        ((TextView) view.findViewById(R.id.action_accounts_done))
            .setOnClickListener(this);
        view.findViewById(R.id.account_fab_verify)
            .setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                        verify_add_account();
                }
            });

        if (service_defs == null){
            service_defs = readServicesList(R.raw.tnt_organisations);
        }
        if (service_names == null){
            service_names = new String[service_defs.length];
            service_urls = new String[service_defs.length];
            for (int i = 0; i < service_defs.length; i++){
                String [] parts = TntImporter.csvLineSplit(service_defs[i]);
                service_names[i] = parts[0];
                service_urls[i] = parts[1];
            }
        }
        mPicker = (StringPicker) view.findViewById(R.id.string_picker);
        mPicker.setValues(service_names);

        mAccountList = (NoScrollListView) view.findViewById(R.id.account_list);
        // populate account list
        Cursor cursor = OpenMPD.getDB().getReadableDatabase().rawQuery("select service_account._id, name, balance_url, username from service_account join tnt_service on tnt_service_id=tnt_service._id", null);
         String [] columns = {"name", "username", "balance_url"};
         int [] fields = {R.id.name, R.id.username, R.id.url};

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.service_account_list_item, cursor, columns, fields);
        mAdapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
               TextView tv = (TextView) view;
                 switch(columnIndex){
                     case 2:
                         try {
                             tv.setText(new URL(cursor.getString(2)).getHost());
                             return true;
                         } catch (Exception e){
                         }
                     return false;
               }
               return false;
            }
        });
        mAccountList.setAdapter(mAdapter);


        return view;
    }

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onStart(){
        super.onStart();
        Intent intent = new Intent(getActivity(), AccountVerifyService.class);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.i("net.bradmont.openmpd", "called bindService");
    }
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mServiceBound) {
            getActivity().unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }
    private void verify_add_account(){
        if (mAccountVerifyService.isRunning() || mServiceBound == false){
            return;
        }
        // set indeterminate on progress button
        ((FabButton) getView().findViewById(R.id.account_fab_verify)).showProgress(true);
        final ServiceAccount account = new ServiceAccount();
        // get values from form fields
        account.setValue("username",
            ((EditText) getView().findViewById(R.id.username)).getText().toString());
        account.setValue("password",
            ((EditText) getView().findViewById(R.id.password)).getText().toString());
        // get selected service
        mSelectedService = mPicker.getCurrent();

        // if service not already in DB
        if (getServiceid(service_names[mSelectedService]) == -1){
            TntService service = new TntService();
            service.setValue("name", service_names[mSelectedService]);
            service.setValue("query_ini_url", service_urls[mSelectedService]);
            service.dirtySave();
            account.setValue("tnt_service_id", service.getID());
        } else {
            account.setValue("tnt_service_id", getServiceid(service_names[mSelectedService]));
        }
        mAccountVerifyService.setOnFinishHandler(new AccountVerifyService.OnFinishHandler(){
            @Override
            public void onFinish(boolean success){
                if (success == true){
                    account.dirtySave();
                    ((FabButton) getView().findViewById(R.id.account_fab_verify)).showProgress(false);
                    ((ViewFlipper) getView().findViewById(R.id.onboard_flipper)).showNext();
                } else {
                    ((OnboardActivity) getActivity()).userMessage(R.string.login_error);
                    ((FabButton) getView().findViewById(R.id.account_fab_verify)).showProgress(false);
                }
            }
        });

        mAccountVerifyService.verifyAccount((OnboardActivity) getActivity(), account);

    }

    private int getServiceid(String name){
        SQLiteDatabase db = OpenMPD.getDB().getReadableDatabase();
        Cursor c = db.rawQuery("select _id from tnt_service where name=?", new String[]{ name });
        if (c.getCount() == 0){
            return -1;
        }
        c.moveToFirst();
        return c.getInt(0);
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_welcome_next:
                // get selected org
                mSelectedService = mPicker.getCurrent();
                // set R.id.org_name
                ((TextView) getView().findViewById(R.id.org_name))
                    .setText(service_names[mSelectedService]);
                // set R.id.org_url
                try {
                ((TextView) getView().findViewById(R.id.org_url))
                    .setText( new URL( service_urls[mSelectedService])
                                .getHost()) ;
                } catch (Exception e){
                ((TextView) getView().findViewById(R.id.org_url))
                    .setText( service_urls[mSelectedService]);
                }
            case R.id.action_login_next:
            case R.id.action_accounts_add:
                ((ViewFlipper) getView().findViewById(R.id.onboard_flipper)).showNext();
                break;
        }

    }

    public String [] readServicesList(int resource_id){
        InputStream inputStream = getActivity().getResources().openRawResource(resource_id);
        InputStreamReader inputReader = new InputStreamReader (inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        ArrayList<String> lines = new ArrayList<String>();

        try {
            while (( line = buffReader.readLine()) != null) {
                lines.add(line);
            }
            buffReader.close();
            inputReader.close();
            inputStream.close();
        } catch (IOException e) {
                return null;
        }
        return lines.toArray(new String[lines.size()]);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            AccountVerifyService.AccountVerifyBinder binder = (AccountVerifyService.AccountVerifyBinder) service;
            mAccountVerifyService = binder.getService();
            mServiceBound = true;
            Log.i("net.bradmont.openmpd", "service bound.");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServiceBound = false;
        }

    };

}
