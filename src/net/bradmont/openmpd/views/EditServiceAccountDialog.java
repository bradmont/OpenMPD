package net.bradmont.openmpd.views;

import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.ConstraintError;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
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
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;



class EditServiceAccountDialog extends DialogFragment{

    ServiceAccount account = null;
    CursorAdapter adapter = null;
    int [] view_ids = {R.id.tnt_service_id, R.id.username, R.id.password };
    String [] field_names = {"tnt_service_id", "username", "password"};
    View content_view = null;

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

        builder.setView(content_view)
            .setMessage(R.string.add_account)
            .setPositiveButton(R.string.add, new saveListner())
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}

            });

        return builder.create();
    }

    /**
     * Specify a cursorAdapter if dialog called from a listView. In this
     * case, we will requery the cursor after we create a new object.
     */
    public void setAdapter(CursorAdapter adapter){
        this.adapter = adapter;
    }

    private class saveListner implements DialogInterface.OnClickListener{
        public void onClick(DialogInterface dialog, int id) {
            if (account == null) {
                account = new ServiceAccount();
            }
            // run through views, assign to values
            try {
                Log.i("net.bradmont.openmpd", "Getting views.");
                for (int i = 0; i < view_ids.length; i++){
                    View v = content_view.findViewById(view_ids[i]);
                    Log.i("net.bradmont.openmpd", "Getting " +field_names[i]);
                    DBField field = account.getField(field_names[i]);
                    field.getFromView(v);
                }
                account.save();
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
