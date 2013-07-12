package net.bradmont.openmpd.controllers;

import android.os.Bundle;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.supergreen.models.*;


public class TntImportService extends IntentService {
    static final int UPDATE_FREQUENCY = 3; // import values every 3 days

    public TntImportService(){
        super("TntImportService");
    }
    @Override
    protected void onHandleIntent(Intent intent){
        Bundle b = intent.getExtras();
        boolean newdata = false;
        boolean initialImport = false;
        if (MPDDBHelper.get() == null){
            MPDDBHelper dbh = new MPDDBHelper(this);
        }
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Importing Contacts")
            .setContentText(" ");

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(0, builder.build());


        // import our stuff
        if (b.containsKey("net.bradmont.openmpd.account_id")){
            ServiceAccount account = new ServiceAccount(b.getInt("net.bradmont.openmpd.account_id"));
            if (isOld(account)){
                if (account.getString("last_import") == null) {initialImport = true;}
                startForeground(account.getID(), builder.build());
                TntImporter importer = new TntImporter(this, account, builder);
                importer.run();
                stopForeground(true);
                newdata = true;
            }
        } else if (b.containsKey("net.bradmont.openmpd.account_ids")){
            int [] ids = b.getIntArray("net.bradmont.openmpd.account_ids");
            for (int i = 0; i < ids.length; i++){
                ServiceAccount account = new ServiceAccount(ids[i]);
                if (isOld(account)){
                    if (account.getString("last_import") == null) {initialImport = true;}
                    startForeground(account.getID(), builder.build());
                    TntImporter importer = new TntImporter(this, account, builder);
                    importer.run();
                    stopForeground(true);
                    newdata = true;
                }
            }
        }

        // Evaluate contacts if we have newly imported data
        if (newdata == true){
            ContactsEvaluator evaluator = new ContactsEvaluator(this, builder, initialImport);
            builder.setContentTitle("Evaluating Contacts")
                .setContentText(" ");
            startForeground(evaluator.NOTIFICATION_ID, builder.build());
            evaluator.run();
            stopForeground(true);


            // notify of important changes
            ModelList notifications = MPDDBHelper.filter("notification", "status", Notification.STATUS_NEW);
            
            int new_partners, late_partners, lapsed_partners, restarted_partners, amount_changes, special_gifts;
            new_partners = late_partners = lapsed_partners = restarted_partners = amount_changes = special_gifts = 0;

            for (int i = 0; i < notifications.size(); i++){

                Notification n = (Notification) notifications.get(i);
                Contact contact = (Contact) n.getRelated("contact");
                ContactStatus status = (ContactStatus)MPDDBHelper
                        .getReferenceModel("contact_status")
                        .getByField("contact_id", contact.getInt("id"));

                switch (n.getInt("type")){
                    case Notification.CHANGE_PARTNER_TYPE:
                        int partnership = status.partnership(status.getInt("partner_type"));
                        if (partnership == R.string.monthly || partnership == R.string.regular){
                            new_partners++;
                        }
                        break;

                    case Notification.CHANGE_STATUS:
                        switch(status.getInt("status")){
                            case ContactStatus.STATUS_LATE:
                                late_partners++;
                                break;
                            case ContactStatus.STATUS_LAPSED:
                                lapsed_partners++;
                                break;
                            case ContactStatus.STATUS_CURRENT:
                                try{
                                    int temp = Integer.parseInt(status.getString("message"));
                                    if (temp == ContactStatus.STATUS_LATE || temp == ContactStatus.STATUS_LAPSED){
                                        restarted_partners++;
                                    }
                                } catch (Exception e){ }
                                break;

                        }
                        break;

                    case Notification.CHANGE_AMOUNT:
                        amount_changes++;
                        break;
                    case Notification.SPECIAL_GIFT:
                        special_gifts++;
                }
                n.setValue("status", Notification.STATUS_NOTIFIED);
                n.dirtySave();
            }

            int total = new_partners + late_partners + lapsed_partners + restarted_partners + amount_changes + special_gifts;

            if (total > 0){
                builder.setContentTitle(String.format("%d MPD notifications", total));
                String content = "";

                if (new_partners >  0){
                    content += String.format("%d new partner(s). ", new_partners);
                }
                if ( restarted_partners  > 0){
                    content += String.format("%d restarted partner(s). ", restarted_partners);
                } 
                if ( special_gifts > 0) { 
                    content += String.format("%d special gift(s). ", special_gifts);
                }
                if ( late_partners  > 0){
                    content += String.format("%d late partner(s). ", late_partners);
                } 
                if ( lapsed_partners  > 0){
                    content += String.format("%d lapsed partner(s). ", lapsed_partners);
                } 
                if ( amount_changes  > 0){
                    content += String.format("%d amount change(s). ", amount_changes);
                } 

                builder.setContentText(content);
                builder.setProgress(0, 0, false); // remove progress bar
                notificationManager.notify(1, builder.build());
            }
        }
    }

    private boolean isOld(ServiceAccount account){
        String date = account.getString("last_import");
        if (date == null){return true;}

        Calendar now = Calendar.getInstance();
        Calendar next_update = new GregorianCalendar();
        String [] parts = date.split("-");
        next_update.set(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]) - 1,
            Integer.parseInt(parts[2])
            );

        next_update.add(Calendar.DAY_OF_MONTH, UPDATE_FREQUENCY);
        // if next_update is before current time, we need to update, so return true:
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String nx = dateFormat.format(next_update.getTime());
        String nw = dateFormat.format(now.getTime());

        // it is utterly ridiculous that this should be easier than using the Calendar object...
        if (nx.compareTo(nw) > 0){
            Log.i("net.bradmont.openmpd", "Not updating; " + nx + " is after today, " + nw);
            return false;
        } else {
            Log.i("net.bradmont.openmpd", "Updating; " + nx + " is before or equal to today, " + nw + ". Last was " + date);
            return true;
        }

    }
}
