package net.bradmont.openmpd;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import java.util.Calendar;
import java.util.GregorianCalendar;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.ServiceAccount;
import net.bradmont.openmpd.controllers.TntImportService;
import net.bradmont.supergreen.models.ModelList;


/**
 * Handles the device boot system broadcast, and schedules data updates
 */
public class OnBootScheduler extends BroadcastReceiver {
    private boolean instantiatedDB = false;
    public void onReceive(Context context, Intent intent) {
        MPDDBHelper helper = MPDDBHelper.rawGet();
        if (helper == null){
            helper = new MPDDBHelper(context);
            instantiatedDB = true;
        }
        // set up our alarms for automatic background updating
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);

        ModelList accounts = helper.getReferenceModel("service_account").getAll();
        if  (accounts.size() == 0){
            return;
        }
        int [] account_ids = new int [accounts.size()];
        for (int i = 0; i < accounts.size(); i++){
            account_ids[i] = accounts.get(i).getID();
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY,
        //alarmManager.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, 
            PendingIntent.getService(context, 0, new Intent(context, TntImportService.class).putExtra("net.bradmont.openmpd.account_ids", account_ids),
            PendingIntent.FLAG_UPDATE_CURRENT)
            );
        helper.close();
    }
}
