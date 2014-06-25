package net.bradmont.openmpd;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.Calendar;
import java.util.GregorianCalendar;

import java.lang.Runnable;

import net.bradmont.openmpd.BaseActivity;
import net.bradmont.openmpd.fragments.*;
import net.bradmont.openmpd.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class HomeActivity extends BaseActivity {
	
    public static HomeFragment homeFragment = null;
    public static DebugFragment debugFragment = null;
    public static ContactList contactList = null;
    public static GiftList giftList = null;
    public static ServiceAccountList serviceAccountList = null;


	public HomeActivity() {
		super(R.string.app_name);
	}
	
    /** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        

		// set the Above View
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		if (mContent == null){
            homeFragment = new HomeFragment();
            if (intent.hasExtra("net.bradmont.openmpd.SSLErrorServer")){
                Log.i("net.bradmont.openmpd", "processing SSLError");
                String host = intent.getStringExtra("net.bradmont.openmpd.SSLErrorServer");
                homeFragment.setAskSSLIgnore(host);
            }
			mContent = homeFragment;
        }

		
		// set the Above View
		setContentView(R.layout.content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
		
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new MenuFragment())
		.commit();
		
		// customize the SlidingMenu
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		getSlidingMenu().setBehindWidthRes(R.dimen.menu_width);
		getSlidingMenu().setBehindScrollScale(0);
        setSlidingActionBarEnabled(false);

        // if it hasn't been done, populate our QuickMessage table
        QuickMessage q = new QuickMessage();
        ModelList messages = q.getAll(); 
        if (messages.size() == 0){
            q.createDefaults();
        }

        // set up our alarms for automatic background updating
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        

        ModelList accounts = MPDDBHelper.getReferenceModel("service_account").getAll();
        int [] account_ids = new int [accounts.size()];
        for (int i = 0; i < accounts.size(); i++){
            account_ids[i] = accounts.get(i).getID();
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC, 
            cal.getTimeInMillis(), 
            AlarmManager.INTERVAL_HALF_DAY, 
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
