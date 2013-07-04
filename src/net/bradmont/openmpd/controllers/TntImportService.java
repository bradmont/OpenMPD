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
import net.bradmont.openmpd.models.ServiceAccount;
import net.bradmont.openmpd.controllers.TntImporter;
import net.bradmont.supergreen.models.*;


public class TntImportService extends IntentService {
    //static final int UPDATE_FREQUENCY = 3; // import values every 3 days
    static final int UPDATE_FREQUENCY = 0; // import values every 3 days

    public TntImportService(){
        super("TntImportService");
    }
    @Override
    protected void onHandleIntent(Intent intent){
        Bundle b = intent.getExtras();
        if (MPDDBHelper.get() == null){
            MPDDBHelper dbh = new MPDDBHelper(this);
        }
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Importing Data")
            .setContentText("foo");

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(0, builder.build());


        if (b.containsKey("net.bradmont.openmpd.account_id")){
            ServiceAccount account = new ServiceAccount(b.getInt("net.bradmont.openmpd.account_id"));
            if (isOld(account)){
                TntImporter importer = new TntImporter(this, account, builder);
                importer.run();
            } else {
                builder.setContentText("Not stale, not updating.");
                notificationManager.notify(0, builder.build());
            }
        } else if (b.containsKey("net.bradmont.openmpd.account_ids")){
            int [] ids = b.getIntArray("net.bradmont.openmpd.account_ids");
            for (int i = 0; i < ids.length; i++){
                ServiceAccount account = new ServiceAccount(ids[i]);
                if (isOld(account)){
                    TntImporter importer = new TntImporter(this, account, builder);
                    importer.run();
                } else {
                    builder.setContentText("Not stale, not updating.");
                    notificationManager.notify(0, builder.build());
                }
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
