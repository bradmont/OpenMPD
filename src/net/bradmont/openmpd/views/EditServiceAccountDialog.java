package net.bradmont.openmpd.views;

import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.ConstraintError;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.lang.RuntimeException;
import java.util.ArrayList;
import java.net.URL;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.http.message.*;




public class EditServiceAccountDialog extends DialogFragment{

    ServiceAccount account = null;
    int [] view_ids = {R.id.username, R.id.password };
    String [] field_names = {"username", "password"};
    View content_view = null;
    private FragmentManager manager=null;
    private String fragment_tag = null;
    private Runnable saveCallback = null;

    private SimpleCursorAdapter parentAdapter = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        content_view = inflater.inflate(R.layout.edit_service_account, null);

        // set up spinner
        Spinner spinner = (Spinner) content_view.findViewById(R.id.tnt_service_id);
        String [] lines = getLines();
        ArrayAdapter<String> adapter = new ServicesAdapter(getActivity(), R.layout.service_spinner_item, R.id.name, lines);
        spinner.setAdapter(adapter);

        if (account != null){
            populateView();
        }

        builder.setView(content_view)
            .setMessage(R.string.add_account)
            .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}
            });

        return builder.create();
    }

    public void setParentAdapter(SimpleCursorAdapter adapter){
        parentAdapter = adapter;
    }

    /**
      * Callback that's executed on a successful save
      */
    public void setSaveCallback(Runnable r){
        saveCallback = r;
    }
    public void onStart(){
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new saveListener());
    }

    public String [] getLines(){
        InputStream inputStream = getActivity().getResources().openRawResource(R.raw.tnt_organisations);
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

    public void setAccount(ServiceAccount account){
        this.account = account;
    }

    private void populateView(){
        for (int i = 0; i < view_ids.length; i++){
            View v = content_view.findViewById(view_ids[i]);
            Log.i("net.bradmont.openmpd", "Getting " +field_names[i]);
            DBField field = account.getField(field_names[i]);
            field.putToView(v);
        }
    }

    private class saveListener implements View.OnClickListener{
        public void onClick(View v) {
            if (account == null) {
                account = new ServiceAccount();
            }
            // run through views, assign to values
            try {
                // get name & password
                Log.i("net.bradmont.openmpd", "Getting views.");
                for (int i = 0; i < view_ids.length; i++){
                    View field_view = content_view.findViewById(view_ids[i]);
                    Log.i("net.bradmont.openmpd", "Getting " +field_names[i]);
                    DBField field = account.getField(field_names[i]);
                    field.getFromView(field_view);
                }

                Log.i("net.bradmont.openmpd", "finding service in spinner");
                // get selected service
                Spinner spin = (Spinner) content_view.findViewById(R.id.tnt_service_id);
                int position = spin.getSelectedItemPosition();
                String item = (String) ((ArrayAdapter)spin.getAdapter()).getItem(position);
                String [] values = TntImporter.csvLineSplit(item);

                // check if service already in tnt_service
                Log.i("net.bradmont.openmpd", "checking for existing service");
                SQLiteDatabase db = OpenMPD.getDB().getReadableDatabase();
                Cursor c = db.rawQuery("select _id from tnt_service where name=?", new String[]{ values[0] });
                TntService service = null;
                if (c.getCount() == 0){
                    Log.i("net.bradmont.openmpd", "service doesn't exist");
                    // if not, create it
                    service = new TntService();
                    service.setValue("name", values[0]);
                    service.setValue("query_ini_url", values[1]);
                    service.save();
                    account.setValue("tnt_service_id", service.getID());
                    Log.i("net.bradmont.openmpd", "service created");
                } else {
                    Log.i("net.bradmont.openmpd", "service exists");
                    c.moveToFirst();
                    account.setValue("tnt_service_id", c.getInt(0));
                }
                c.close();
                    Log.i("net.bradmont.openmpd", "cursor closed");


                // get its id, assign to account.tnt_service_id
                

                // check the account credentials

                final Context context = getActivity();
                Log.i("net.bradmont.openmpd", "Queueing task");
                ((BaseActivity)getActivity()).queueTask(new Runnable(){

                    @Override
                    public void run(){
                        Log.i("net.bradmont.openmpd", "run()");
                        ((BaseActivity)getActivity()).showWaitDialog(R.string.connecting_server, R.string.please_wait);
                        TntImporter importer = new TntImporter(getActivity(), account);
                        ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);
                        TntService service = (TntService) account.getRelated("tnt_service_id");
                        if (service.getString("base_url") == null || (!service.getString("base_url").startsWith("http") && !service.getString("balance_url").startsWith("http"))){
                            Log.i("net.bradmont.openmpd", "getting query.ini");
                            if (!TntImporter.processQueryIni(service)){
                                ((BaseActivity)getActivity()).userMessage( R.string.connect_server_error);
                                LogItem.logError("Error parsing Query.ini", service.getString("query_ini_url"), "");
                                ((BaseActivity)getActivity()).dismissWaitDialog();
                                Log.i("net.bradmont.openmpd", "error getting query.ini");
                                return;
                            }
                            Log.i("net.bradmont.openmpd", "done getting query.ini");
                            service.dirtySave();
                        } else {
                            Log.i("net.bradmont.openmpd", "no need to get query.ini");
                        }
                        ((BaseActivity)getActivity()).dismissWaitDialog();
                        Log.i("net.bradmont.openmpd", "Getting balance");
                        ((BaseActivity)getActivity()).showWaitDialog(R.string.checking_login, R.string.please_wait);

                        Log.i("net.bradmont.openmpd", "getting balance");
                        arguments.add(new BasicNameValuePair( "Action", service.getBalanceAction()));
                        arguments.add(new BasicNameValuePair( service.getUsernameKey(), account.getString("username")));
                        arguments.add(new BasicNameValuePair( service.getPasswordKey(), account.getString("password")));

                        ArrayList<String> content = null;
                        try {
                            content = importer.getStringsFromUrl(service.getString("base_url") + service.getString("balance_url"), arguments, false);
                        } catch (RuntimeException e){
                            ((BaseActivity)getActivity()).dismissWaitDialog();
                            Log.i("net.bradmont.openmpd", "SSL error caught");
                            if (service.getString("base_url").startsWith("http")){
                                showSSLError(service.getString("base_url"));
                            } else {
                                showSSLError(service.getString("balance_url"));
                            }
                            return;
                        }
                        ((BaseActivity)getActivity()).dismissWaitDialog();

                        if (content == null || content.get(0).contains("ERROR") || content.size() > 5){
                            // if result is > 5 lines on a balance query, it's probably because the
                            // server sent an error. TODO: we need a better way of checking this;
                            // a comprehensive list of errors would be helpful, but there doesn't
                            // seem to be one on the TNT website... (CCCi's servers don't seem to
                            // follow the expected behaviour)
                            // http://www.tntware.com/tntmpd/faqs/en/how-can-i-make-my-organization-39-s-online-donation-system-compatible-with-tntmpd.aspx
                                ((BaseActivity)getActivity()).userMessage( R.string.login_error);
                                if (content != null){
                                    String temp = "";
                                    for (int i = 0; i < content.size(); i++){
                                        temp += content.get(i) + "\n";
                                    }
                                    LogItem.logError("Sign in error", service.getString("base_url") + service.getString("balance_url"), temp);
                                }
                        } else {
                            
                            for (int i = 0; i < content.size(); i++){
                                Log.i("net.bradmont.openmpd", content.get(i));
                            }
                            try {
                                ((BaseActivity)getActivity()).userMessage( R.string.account_verified);
                                account.dirtySave();
                                dismiss();
                                if (saveCallback != null){
                                    getActivity().runOnUiThread(saveCallback);
                                }
                            } catch (Exception e){
                                Log.i("net.bradmont.openmpd", e.getMessage());
                                for (int i=0; i < e.getStackTrace().length; i++){
                                    Log.i("net.bradmont.openmpd", e.getStackTrace()[i].toString());
                                }
                            }
                        }
                    }
                    public void showSSLError(final String url){
                        ((BaseActivity)getActivity()).runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                Log.i("net.bradmont.openmpd", "building dialog");
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage(R.string.ask_add_ssl_exception)
                                       .setTitle(R.string.ssl_cert_error);
                                builder.setPositiveButton(R.string.ignore_certificate, new DialogInterface.OnClickListener() {
                                           public void onClick(DialogInterface dialog, int id) {
                                               // User clicked OK button
                                               URL u = null;
                                               try { u = new URL(url); } catch (Exception e){}
                                               String host = u.getHost();
                                               SharedPreferences.Editor prefs = OpenMPD.get().getSharedPreferences("openmpd", Context.MODE_PRIVATE).edit();
                                               prefs.putBoolean("ignore_ssl_" + host, true);
                                               prefs.commit();
                                               ((BaseActivity)getActivity()).userMessage(R.string.ignoring_ssl);
                                           }
                                       });
                                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                           public void onClick(DialogInterface dialog, int id) { }
                                       });


                                Log.i("net.bradmont.openmpd", "showing dialog");
                                builder.show();
                            }
                        });    
                    }
                });
            } catch (ConstraintError e){
                // TODO: Don't dismiss dialog on bad input
                // TODO: Croutons
                Toast.makeText(getActivity(), "Bad input, not saved", Toast.LENGTH_SHORT)
                     .show();
            }

            if (parentAdapter != null){
                parentAdapter.getCursor().requery();
            }

        }
    }

    private class ServicesAdapter extends ArrayAdapter<String>{
        private String [] [] values = null;
        private int layoutResourceId;

        public ServicesAdapter(Context context, int resource, int textViewResourceID, String[] objects){
            super(context, resource, textViewResourceID, objects);
            values = new String[objects.length][];
            for (int i = 0; i < objects.length; i++){
                values[i]=null;
            }
            layoutResourceId = resource;
        }
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null){
                LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
                view = inflater.inflate(layoutResourceId, parent, false);
            }
            if (values[position] == null){
                values[position] = TntImporter.csvLineSplit(getItem(position));
            }

            TextView name = (TextView) view.findViewById(R.id.name);
            TextView server = (TextView) view.findViewById(R.id.server);
            name.setText(values[position][0]);
            try {
                URL url = new URL(values[position][1]);
                server.setText(url.getHost());
            } catch (Exception e){
                server.setText("-");
            }
            return view;
        }

    }
}
