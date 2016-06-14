package net.bradmont.openmpd;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.dao.*;
import net.bradmont.openmpd.views.*;
import net.bradmont.openmpd.controllers.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.models.ModelList;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;


import android.database.sqlite.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import net.bradmont.openmpd.helpers.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import java.lang.Runnable;

import net.bradmont.openmpd.BaseActivity;
import net.bradmont.openmpd.fragments.*;
import net.bradmont.openmpd.activities.*;
import net.bradmont.openmpd.R;

public class HomeActivity extends BaseActivity {
	

	public HomeActivity() {
		super(R.string.app_name);
	}
	
    /** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("openmpd", Context.MODE_PRIVATE);
        switch (prefs.getInt("onboardState", OpenMPD.ONBOARD_FIRST_RUN)){
            case OpenMPD.ONBOARD_FINISHED:
                break;
            case OpenMPD.ONBOARD_FIRST_RUN:
            case OpenMPD.ONBOARD_ACCOUNT_ADDED:
                Intent switchIntent = new Intent(this, OnboardActivity.class);
                startActivity(switchIntent);
                finish();
                break;
            case OpenMPD.ONBOARD_IMPORTING:
                // TODO
                switchIntent = new Intent(this, ImportActivity.class);
                startActivity(switchIntent);
                finish();
                // todo: stop this activity
        }
		
        // if it hasn't been done, populate our QuickMessage table
        QuickMessage q = new QuickMessage();
        List<QuickMessage> messages = OpenMPD.getDaoSession().getQuickMessageDao().queryBuilder().list(); // TODO: encapsulate...
        if (messages.size() == 0){
            //q.createDefaults();
            // DEPRECATED
            // TODO: MOVE ELSEWHERE
        }

        // set up our alarms for automatic background updating
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = new GregorianCalendar();
        

        //ModelList accounts = MPDDBHelper.getReferenceModel("service_account").getAll();
        List<ServiceAccount> accounts = OpenMPD.getDaoSession().getServiceAccountDao().queryBuilder().list(); // TODO: encapsulate...
        int [] account_ids = new int [accounts.size()];
        for (int i = 0; i < accounts.size(); i++){
            account_ids[i] = accounts.get(i).getId().intValue();
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC, 
            cal.getTimeInMillis(), 
            AlarmManager.INTERVAL_HOUR, 
            PendingIntent.getService(this, 0, 
                new Intent(this, TntImportService.class).putExtra("net.bradmont.openmpd.account_ids", account_ids),
            PendingIntent.FLAG_UPDATE_CURRENT)
            );

	}

    private void verifySSLIgnore(final String host){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(host)
            .setMessage(R.string.ask_add_ssl_exception);
        builder.setPositiveButton(R.string.ignore_certificate, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences.Editor prefs = getSharedPreferences("openmpd", Context.MODE_PRIVATE).edit();
                prefs.putBoolean("ignore_ssl_" + host, true);
                prefs.commit();
                userMessage(R.string.ignoring_ssl);

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        Log.i("net.bradmont.openmpd", "Showing dialog");
        builder.show();

    }
	


}
