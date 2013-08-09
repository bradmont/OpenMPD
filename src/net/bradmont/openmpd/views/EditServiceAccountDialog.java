package net.bradmont.openmpd.views;

import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.ConstraintError;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import org.apache.http.message.*;




public class EditServiceAccountDialog extends DialogFragment{

    ServiceAccount account = null;
    CursorAdapter adapter = null;
    int [] view_ids = {R.id.tnt_service_id, R.id.username, R.id.password };
    String [] field_names = {"tnt_service_id", "username", "password"};
    View content_view = null;
    private FragmentManager manager=null;
    private String fragment_tag = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        content_view = inflater.inflate(R.layout.edit_service_account, null);

        // set up spinner
        Spinner spinner = (Spinner) content_view.findViewById(R.id.tnt_service_id);
        Cursor c = MPDDBHelper.get().getReadableDatabase()
            .rawQuery("select _id, name from tnt_service", null);
        String [] spinner_columns = {"_id", "name"};
        int [] spinner_views = {R.id._id, R.id.name};
        //String [] spinner_columns = { "name"};
        //int [] spinner_views = { R.id.name};
        SimpleCursorAdapter ca = new SimpleCursorAdapter(getActivity(),
            R.layout.service_spinner_item, c, spinner_columns, spinner_views);
        spinner.setAdapter(ca);

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
    public void onStart(){
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new saveListener());
    }

    /**
     * Specify a cursorAdapter if dialog called from a listView. In this
     * case, we will requery the cursor after we create a new object.
     */
    public void setAdapter(CursorAdapter adapter){
        this.adapter = adapter;
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
                Log.i("net.bradmont.openmpd", "Getting views.");
                for (int i = 0; i < view_ids.length; i++){
                    View field_view = content_view.findViewById(view_ids[i]);
                    Log.i("net.bradmont.openmpd", "Getting " +field_names[i]);
                    DBField field = account.getField(field_names[i]);
                    field.getFromView(field_view);
                }

                // check the account credentials

                final Context context = getActivity();
                OpenMPD.getInstance().queueTask(new Runnable(){

                    @Override
                    public void run(){
                        OpenMPD.getInstance().showWaitDialog(R.string.checking_login, R.string.please_wait);
                        TntImporter importer = new TntImporter(getActivity(), account);
                        ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);
                        arguments.add(new BasicNameValuePair( "Action", "TntBalance"));
                        arguments.add(new BasicNameValuePair( "Username", account.getString("username")));
                        arguments.add(new BasicNameValuePair( "Password", account.getString("password")));

                        TntService service = (TntService) account.getRelated("tnt_service_id");
                        ArrayList<String> content = importer.getStringsFromUrl(service.getString("base_url") + service.getString("balance_url"), arguments);
                        OpenMPD.getInstance().dismissWaitDialog();

                        if (content == null || content.get(0).contains("ERROR") || content.size() > 5){
                            // if result is > 5 lines on a balance query, it's probably because the
                            // server sent an error. TODO: we need a better way of checking this;
                            // a comprehensive list of errors would be helpful, but there doesn't
                            // seem to be one on the TNT website... (CCCi's servers don't seem to
                            // follow the expected behaviour)
                            // http://www.tntware.com/tntmpd/faqs/en/how-can-i-make-my-organization-39-s-online-donation-system-compatible-with-tntmpd.aspx
                                OpenMPD.getInstance().userMessage( R.string.login_error);
                        } else {
                            
                            for (int i = 0; i < content.size(); i++){
                                Log.i("net.bradmont.openmpd", content.get(i));
                            }
                            try {
                                OpenMPD.getInstance().userMessage( R.string.account_verified);
                                account.dirtySave();
                                dismiss();
                            } catch (Exception e){
                                Log.i("net.bradmont.openmpd", e.getMessage());
                                for (int i=0; i < e.getStackTrace().length; i++){
                                    Log.i("net.bradmont.openmpd", e.getStackTrace()[i].toString());
                                }
                            }
                        }
                    }
                });
            } catch (ConstraintError e){
                // TODO: Don't dismiss dialog on bad input
                // TODO: Croutons
                Toast.makeText(getActivity(), "Bad input, not saved", Toast.LENGTH_SHORT)
                     .show();
            }


            if (adapter != null){
                adapter.getCursor().requery();
            }
        }
    }
}
