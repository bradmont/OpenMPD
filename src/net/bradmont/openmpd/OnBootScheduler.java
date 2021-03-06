package net.bradmont.openmpd;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.dao.ServiceAccount;
import net.bradmont.openmpd.controllers.TntImportService;


/**
 * Handles the device boot system broadcast, and schedules data updates
 */
public class OnBootScheduler extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("net.bradmont.openmpd", "received boot broadcast");
        // set up our alarms for automatic background updating
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar cal = new GregorianCalendar();

        List<ServiceAccount> accounts = OpenMPD.getDaoSession().getServiceAccountDao().queryBuilder().list(); // TODO: encapsulate...
        if  (accounts.size() == 0){
            Log.d("net.bradmont.openmpd", "no accounts, not setting alarm");
            return;
        }
        int [] account_ids = new int [accounts.size()];
        for (int i = 0; i < accounts.size(); i++){
            account_ids[i] = accounts.get(i).getId().intValue();
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_HOUR,
            PendingIntent.getService(context, 0, new Intent(context, TntImportService.class).putExtra("net.bradmont.openmpd.account_ids", account_ids),
            PendingIntent.FLAG_UPDATE_CURRENT)
            );
        Log.d("net.bradmont.openmpd", "alarmmanager set");
    }
}
