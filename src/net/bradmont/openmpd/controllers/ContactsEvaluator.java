package net.bradmont.openmpd.controllers;

import net.bradmont.openmpd.activities.ImportActivity;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.supergreen.models.*;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.message.*;

import android.app.NotificationManager;
import android.content.Context;
import android.widget.ProgressBar;

import android.support.v4.app.NotificationCompat;


import java.lang.Runnable;
import java.lang.Thread;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;

public class ContactsEvaluator implements Runnable{

    private NotificationCompat.Builder builder = null;
    private NotificationManager notifyManager = null;
    private Context context;
    private ArrayList<Boolean> initialImport = null; 
    private ArrayList<Integer> newdata = null; 

    public final static int NOTIFICATION_ID = 1;

    public ContactsEvaluator(Context context, NotificationCompat.Builder builder){
        this.builder = builder;
        this.context = context;
    }

    public ContactsEvaluator(Context context, NotificationCompat.Builder builder, ArrayList<Integer> newdata, ArrayList<Boolean> initialImport){
        this(context, builder);
        this.initialImport = initialImport;
        this.newdata = newdata;
    }

    public void run(){
        ModelList [] contact_lists = null;
        int total_contacts = 0;
        if (newdata == null){
            contact_lists = new ModelList[1];
            contact_lists[0] = new Contact().getAll(); // all!
            total_contacts = contact_lists[0].size();
        } else {
            contact_lists = new ModelList[newdata.size()];
            for (int i = 0; i < newdata.size(); i++){
                contact_lists[i] = MPDDBHelper.get().filter("contact", "account", newdata.get(i).intValue());
                total_contacts += contact_lists[i].size();
            }
        }

        notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int progress = 0;
        ContactStatus temp = new ContactStatus();
        ContactStatus.beginTransaction();
        for (int j = 0; j < contact_lists.length; j++){
            ModelList contacts = contact_lists[j];
            for (int i=0; i < contacts.size(); i++){
                try {
                    ((Contact) contacts.get(i)).updateStatus(initialImport.get(j).booleanValue());
                } catch (Exception e){
                    String contact_id = "Contact index " + i + " of " + contacts.size() + ", Contact_id: ";
                    try {
                        contact_id += ((Contact) contacts.get(i)).getInt("id");
                        contact_id += "\nName: " + ((Contact) contacts.get(i)).getInt("fname") 
                            + " " + ((Contact) contacts.get(i)).getInt("lname");
                    } catch (Exception drop ){}
                    LogItem.logError("Error evaluating contact", contact_id, e);
                }
                progress++;
                if (progress % 1000 == 0){
                    ContactStatus.endTransaction();
                    ContactStatus.beginTransaction();
                }
                if (builder != null){
                    builder.setProgress(total_contacts, progress, false);
                    notifyManager.notify(NOTIFICATION_ID, builder.build());
                }
                ImportActivity.setProgress(-1, total_contacts, progress, false);
            }
        }
        ContactStatus.endTransaction();
    }

}
