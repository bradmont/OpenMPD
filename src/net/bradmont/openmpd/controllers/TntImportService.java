package net.bradmont.openmpd.controllers;

import android.os.Bundle;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import net.bradmont.openmpd.helpers.Log;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.activities.ImportActivity;
import net.bradmont.openmpd.dao.*;
import net.bradmont.openmpd.fragments.AnalyticsFragment;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.supergreen.models.*;


public class TntImportService extends IntentService {
    static final String UPDATE_FREQUENCY = "3"; // import values every 3 days
    public static final boolean DEBUG_TEST_EVALUATOR = false;

    public TntImportService(){
        super("TntImportService");
    }
    @Override
    protected void onHandleIntent(Intent intent){
        Log.i("net.bradmont.openmpd", "Starting updater thread");

        // check if we have a network connection
        if (!isNetworkAvailable()){
            return;
        }
        Bundle b = intent.getExtras();
        ArrayList<Long> newdata = new ArrayList<Long>();
        ArrayList<Boolean> initialImport = new ArrayList<Boolean>();
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Importing Contacts")
            .setContentText(" ");

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(0, builder.build());


        // import our stuff
        startForeground(ContactsEvaluator.NOTIFICATION_ID, builder.build());

        // DEBUG: skip importingg.
        if (DEBUG_TEST_EVALUATOR) {
            newdata = null;
        } else if (b.containsKey("net.bradmont.openmpd.account_id")){
            ServiceAccount account = OpenMPD.getDaoSession().getServiceAccountDao().load( 
                    (long) b.getInt("net.bradmont.openmpd.account_id"));
            if (isOld(account) || b.containsKey("net.bradmont.openmpd.force_update")){
                if (account.getLastImport() == null) {
                    initialImport.add(new Boolean(true));
                } else {
                    initialImport.add(new Boolean(false));
                }
                TntImporter importer = new TntImporter(this, account, builder);
                try {
                    if (importer.run() == true){
                        newdata.add(account.getId().longValue());
                        Log.i("net.bradmont.openmpd", "importer returned true.");
                    } else {
                        Log.i("net.bradmont.openmpd", "importer returned false.");
                        return;
                    }
                } catch (Exception e){
                    Log.i("net.bradmont.openmpd", "TntImporter crashed", e);
                    notifyError(notificationManager, "TntImporter Exception", e);
                }
            }
        } else if (b.containsKey("net.bradmont.openmpd.account_ids")){
            int [] ids = b.getIntArray("net.bradmont.openmpd.account_ids");
            for (int i = 0; i < ids.length; i++){
                ServiceAccount account = OpenMPD.getDaoSession().getServiceAccountDao().load((long)ids[i]);
                if (isOld(account) || b.containsKey("net.bradmont.openmpd.force_update")){
                    if (account.getLastImport() == null) {
                        initialImport.add(new Boolean(true));
                    } else {
                        initialImport.add(new Boolean(false));
                    }
                    TntImporter importer = new TntImporter(this, account, builder);
                    try {
                        if (importer.run() == true){
                            newdata.add(new Long(account.getId()));
                            Log.i("net.bradmont.openmpd", "importer returned true.");
                        } else {
                            Log.i("net.bradmont.openmpd", "importer returned false.");
                            return;
                        }
                    } catch (Exception e){
                        Log.i("net.bradmont.opemnpd", "TntImporter crashed", e);
                        notifyError(notificationManager, "TntImporter Exception", e);
                    }
                }
            }
        }
        Log.i("net.bradmont.openmpd", "import done");

        // Evaluate contacts if we have newly imported data
        if (newdata == null || newdata.size() > 0 ){
            Log.i("net.bradmont.openmpd", "starting evaluation");
            ContactsEvaluator evaluator = new ContactsEvaluator(this, builder, newdata, initialImport);
            builder.setContentTitle("Evaluating Contacts")
                .setContentText(" ");
            ImportActivity.setStatus(-1, R.string.evaluating_contacts);
            evaluator.run();

            getSharedPreferences("openmpd", Context.MODE_PRIVATE)  
                .edit()
                .putInt("onboardState", OpenMPD.ONBOARD_FINISHED)
                .apply(); // Onboarding done



            // clear the cache on our summary graph
            AnalyticsFragment.clearCache();
            AnalyticsFragment.createCache();

            notifyUser(builder, notificationManager);
        }

        ImportActivity.onFinish();
        stopForeground(true);
    }
    protected void notifyError(NotificationManager notificationManager, String message, Exception e){
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("OpenMPD import error")
            .setContentText("Please click to report error.")
            .setAutoCancel(true);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setType("text/plain");
        emailIntent.setData(Uri.parse("mailto: brad.stewart@p2c.com" ));

        emailIntent.putExtra(Intent.EXTRA_EMAIL, "brad.stewart@p2c.com");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OpenMPD crash report: " + message);

        String stackTrace = e.toString();
        for (int i = 0; i < e.getStackTrace().length; i++){
            stackTrace = stackTrace + "\n" + e.getStackTrace()[i].toString();
        }

        stackTrace = "OpenMPD import process ended with the following error:\n"
                + stackTrace;

        emailIntent.putExtra(Intent.EXTRA_TEXT, stackTrace);


        PendingIntent emailPendingIntent =
            PendingIntent.getActivity(this, 0, Intent.createChooser(emailIntent, "Send email with..."), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(emailPendingIntent);
        notificationManager.notify(ContactsEvaluator.NOTIFICATION_ID +8, builder.build());
    }

    protected void notifyUser(NotificationCompat.Builder builder, NotificationManager notificationManager){
        // TODO
        /*
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
                    if (partnership == R.string.per_month || partnership == R.string.per_n_months){
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

            Intent homeIntent = new Intent(this, HomeActivity.class);
            homeIntent.putExtra("net.bradmont.openmpd.notifications", true);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(HomeActivity.class);
            stackBuilder.addNextIntent(homeIntent);
            PendingIntent homePendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(homePendingIntent);


            notificationManager.notify(ContactsEvaluator.NOTIFICATION_ID +1, builder.build());
        }
        */
    }

    protected boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager 
            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isOld(ServiceAccount account){
        Date lastImport = account.getLastImport();
        if (lastImport == null){return true;}

        Calendar now = Calendar.getInstance();
        Calendar next_update = new GregorianCalendar();
        next_update.setTime(lastImport);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OpenMPD.get());

        // check if set for office hours only
        if (prefs.getBoolean("pref_notify_office_hours", false)){
            // is it beween 9 and 5 on a weekday?
            if  ((now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
            
            (now.get(Calendar.HOUR_OF_DAY) < 9  || now.get(Calendar.HOUR_OF_DAY) > 17)){
                Log.i("net.bradmont.openmpd", "Configured to import only during business hours, aborting.");
                return false;
            } else {
                Log.i("net.bradmont.openmpd", "Currently business hours, continuing");
            }
        }

        int update_frequency = Integer.parseInt(prefs.getString("pref_update_frequency", UPDATE_FREQUENCY));
        next_update.add(Calendar.DAY_OF_MONTH, update_frequency);
        // if next_update is before current time, we need to update, so return true:
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String nx = dateFormat.format(next_update.getTime());
        String nw = dateFormat.format(now.getTime());

        // it is utterly ridiculous that this should be easier than using the Calendar object...
        if (nx.compareTo(nw) > 0){
            Log.i("net.bradmont.openmpd", "Not updating; " + nx + " is after today, " + nw);
            return false;
        } else {
            Log.i("net.bradmont.openmpd", "Updating; " + nx + " is before or equal to today, " + nw + ". Last was " + lastImport.toString());
            return true;
        }

    }
}
