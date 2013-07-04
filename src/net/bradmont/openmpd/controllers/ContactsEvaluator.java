package net.bradmont.openmpd.controllers;

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

    private ProgressBar progressbar=null;
    private NotificationCompat.Builder builder = null;
    private NotificationManager notifyManager = null;
    private Context context;

    public final static int NOTIFICATION_ID = 0;


    public ContactsEvaluator(Context context, ProgressBar progressbar){
        this.progressbar = progressbar;
        this.context = context;
    }

    public ContactsEvaluator(Context context, NotificationCompat.Builder builder){
        this.builder = builder;
        this.context = context;
    }

    public void run(){
        ModelList contacts = new Contact().getAll(); // all!

        if (progressbar != null){
            progressbar.setIndeterminate(false);
            progressbar.setMax(contacts.size());
        }
        if (builder != null){
            builder.setProgress(contacts.size(), 0, false);
            notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.notify(NOTIFICATION_ID, builder.build());
        }

        for (int i=0; i < contacts.size(); i++){
            ((Contact) contacts.get(i)).updateStatus();
            if (progressbar != null){
                progressbar.incrementProgressBy(1);
            }
            if (builder != null){
                builder.setProgress(contacts.size(), i, false);
                notifyManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }

}
