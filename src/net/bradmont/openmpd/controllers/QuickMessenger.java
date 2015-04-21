package net.bradmont.openmpd.controllers;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import net.bradmont.openmpd.helpers.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/*
 * Manage Quick Action messages, including selecting and sending messages,
 * adding and editing message templates.
 */
public class QuickMessenger {

    protected Contact contact;
    protected String messageFilter = null;
    private Activity activity=null;

	public QuickMessenger(Activity activity, Contact contact, String messageFilter){
        this.activity=activity;
        this.contact=contact;
        this.messageFilter = messageFilter;
    }
	public QuickMessenger(Activity activity, Contact contact){
        this.activity=activity;
        this.contact=contact;
    }
    public void showQuickMessageDialog(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OpenMPD.get());
        if (!prefs.getBoolean("pref_message_templates_enabled", false)){
            sendMessage(null, "", "");
            return;
        }

        String sql;
        if (messageFilter != null){
            sql = "select _id, name, subject, body from quick_message where notification_type = '" + messageFilter+ "' order by name;";
        } else {
            sql = "select _id, name, subject, body from quick_message order by name;";
        }
        AlertDialog.Builder ad = new AlertDialog.Builder(activity);
        ad.setTitle(R.string.choose_a_template);

        ad.setPositiveButton(R.string.send_blank, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendMessage(null, "", "");
            }
        });
        ad.setNegativeButton(R.string.new_template, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showEditTemplateDialog(null);
            }
        });

        // set up listview
        ListView message_list = (ListView) ((LayoutInflater) activity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.dialog_list, null);
        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery(sql, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
            R.layout.quick_message_list_item, cur,
            new String [] {"name"},
            new int [] {R.id.name});
        message_list.setAdapter(adapter);
        final AlertDialog dialog = ad.create();

        message_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                int position, long id) {
                QuickMessage message = new QuickMessage((int)id);
                showPreview(message, dialog);
            }
        });

        dialog.setView(message_list);
        dialog.show();
    }

    public void showPreview(final QuickMessage message, final Dialog parentDialog){
        // show a preview of the message, and the option to show it
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final String body = message.getString("body").replace("$name", contact.getString("fname"));
        final String subject = message.getString("subject").replace("$name", contact.getString("fname"));

        builder.setMessage(body)
                .setTitle(message.getString("subject"));

        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendMessage(message, subject, body);
                parentDialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.edit_template, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { 
                showEditTemplateDialog(message);
                parentDialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }
        });

        builder.show();

    }
    private void showEditTemplateDialog(final QuickMessage message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.edit_email_template);
        final LinearLayout layout = (LinearLayout) ((LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.quick_message_edit, null);
        builder.setView(layout);
        if (message != null){
            ((EditText) layout.findViewById(R.id.name))
                .setText(message.getString("name"));
            ((EditText) layout.findViewById(R.id.email_subject))
                .setText(message.getString("subject"));
            ((EditText) layout.findViewById(R.id.email_body))
                .setText(message.getString("body"));
        }
        builder.setPositiveButton(R.string.save_and_send, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveTemplate(message, layout);
                String body = message.getString("body").replace("$name", contact.getString("fname"));
                String subject = message.getString("subject").replace("$name", contact.getString("fname"));
                sendMessage(message, subject, body);
            }
        });
        builder.setNeutralButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveTemplate(message, layout);
                ((BaseActivity) activity).userMessage(R.string.saved);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }
        });
        builder.show();
    }
    private void saveTemplate(QuickMessage message, LinearLayout layout){
        if (message == null){
            message = new QuickMessage();
            message.setValue("notification_type", messageFilter);
        }
        message.setValue("name", 
            ((EditText) layout.findViewById(R.id.name)).getText().toString());
        message.setValue("subject", 
            ((EditText) layout.findViewById(R.id.email_subject)).getText().toString());
        message.setValue("body", 
            ((EditText) layout.findViewById(R.id.email_body)).getText().toString());
        message.setValue("customized", true);
        message.dirtySave();
    }

    private void sendMessage(QuickMessage message, String subject, String body){
        String recipient_address = "";
        try {
            EmailAddress email = (EmailAddress) MPDDBHelper
                .getModelByField("email_address", "contact_id", contact.getInt("id"));
            recipient_address = email.getString("address");
        } catch (Exception e){
            Log.i("net.bradmont.openmpd", "Problem getting email address");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String [] {recipient_address});
        if (message != null){
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
        }

        activity.startActivity(
                Intent.createChooser(intent, "Send Email"));
    }

	
}
