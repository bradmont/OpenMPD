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
                startForeground(account.getID(), builder.build());
                TntImporter importer = new TntImporter(this, account, builder);
                importer.run();
                stopForeground(true);
                newdata = true;
            } else {
                builder.setContentText("Not stale, not updating.");
                notificationManager.notify(0, builder.build());
            }
        } else if (b.containsKey("net.bradmont.openmpd.account_ids")){
            int [] ids = b.getIntArray("net.bradmont.openmpd.account_ids");
            for (int i = 0; i < ids.length; i++){
                ServiceAccount account = new ServiceAccount(ids[i]);
                if (isOld(account)){
                    startForeground(account.getID(), builder.build());
                    TntImporter importer = new TntImporter(this, account, builder);
                    importer.run();
                    stopForeground(true);
                    newdata = true;
                } else {
                    builder.setContentText("Not stale, not updating.");
                    notificationManager.notify(0, builder.build());
                }
            }
        }

        // Evaluate contacts if we have newly imported data
        if (newdata == true){
            ContactsEvaluator evaluator = new ContactsEvaluator(this, builder);
            builder.setContentTitle("Evaluating Contacts")
                .setContentText(" ");
            startForeground(evaluator.NOTIFICATION_ID, builder.build());
            evaluator.run();
            stopForeground(true);
        }

        // notify of important changes
        ModelList notifications = MPDDBHelper.filter("notification", "status", Notification.STATUS_NEW);
        for (int i = 0; i < notifications.size(); i++){
            Notification n = (Notification) notifications.get(i);
            // notify the user
            Contact contact = (Contact) n.getRelated("contact");
            ContactStatus status = (ContactStatus)MPDDBHelper
                    .getReferenceModel("contact_status")
                    .getByField("contact_id", contact.getInt("id"));
            
            boolean notify = true;
            builder.setContentText("");
            if (n.getInt("type") == Notification.CHANGE_PARTNER_TYPE){
                int partnership = status.partnership(status.getInt("partner_type"));
                if (partnership == R.string.monthly){
                    builder.setContentTitle("New monthly parner!");
                    builder.setContentText(
                        String.format("%s %s at $%.2f",
                            contact.getString("fname"),
                            contact.getString("lname"),
                            status.getFloat("giving_amount")/100f)
                    );
                } else if (partnership == R.string.regular){
                    builder.setContentTitle("New regular parner!");
                    builder.setContentText(
                        String.format("%s %s at $%.2f/%dmo",
                            contact.getString("fname"),
                            contact.getString("lname"),
                            status.getFloat("giving_amount")/100f,
                            status.getInt("gift_frequency"))
                    );
                }
            } else if (n.getInt("type") == Notification.CHANGE_STATUS){
                if (status.getInt("status") == ContactStatus.STATUS_LATE) {
                    builder.setContentTitle("Late donor");
                    builder.setContentText(
                        String.format("%s %s at $%.2f/%dmo",
                            contact.getString("fname"),
                            contact.getString("lname"),
                            status.getFloat("giving_amount")/100f,
                            status.getInt("gift_frequency"))
                    );
                } else if (status.getInt("status") == ContactStatus.STATUS_LAPSED) {
                    builder.setContentTitle("Lapsed donor");
                    builder.setContentText(
                        String.format("%s %s at $%.2f/%dmo",
                            contact.getString("fname"),
                            contact.getString("lname"),
                            status.getFloat("giving_amount")/100f,
                            status.getInt("gift_frequency"))
                    );
                } else if (status.getInt("status") == ContactStatus.STATUS_CURRENT) {
                    try {
                        int temp = Integer.parseInt(status.getString("text"));
                        if (temp == ContactStatus.STATUS_LATE || temp == ContactStatus.STATUS_LAPSED){
                            builder.setContentTitle("Restarted donor!");
                            builder.setContentText(
                                String.format("%s %s at $%.2f/%dmo",
                                    contact.getString("fname"),
                                    contact.getString("lname"),
                                    status.getFloat("giving_amount")/100f,
                                    status.getInt("gift_frequency"))
                            );
                        } else {
                            notify = false;
                        }
                    }catch (Exception e){ notify=false;}
                } else if (status.getInt("status") == ContactStatus.STATUS_DROPPED) {
                    notify=false;
                } 

            } else if (n.getInt("type") == Notification.CHANGE_AMOUNT){
                builder.setContentTitle("Donor amount changed");
                builder.setContentText(
                    String.format("%s %s at $%.2f/%dmo",
                        contact.getString("fname"),
                        contact.getString("lname"),
                        status.getFloat("giving_amount")/100f,
                        status.getInt("gift_frequency"))
                );
            } else if (n.getInt("type") == Notification.SPECIAL_GIFT){
                builder.setContentTitle("Special Gift");
                float amount = 0f;
                try { 
                    amount = Float.parseFloat(n.getString("text"));
                } catch (Exception e){}
                builder.setContentText(
                    String.format("%s %s gave $%.2f",
                        contact.getString("fname"),
                        contact.getString("lname"),
                        amount/100f,
                        status.getInt("gift_frequency"))
                );
            }


            if (notify == true){
                notificationManager.notify(n.getID(), builder.build());
            }
            n.setValue("status", Notification.STATUS_NOTIFIED);
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
