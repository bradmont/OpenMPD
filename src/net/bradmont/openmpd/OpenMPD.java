package net.bradmont.openmpd;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.views.*;
import net.bradmont.openmpd.controllers.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.models.ModelList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;

import android.content.Intent;
import android.content.Context;

import android.database.sqlite.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.lang.Runnable;

import net.bradmont.openmpd.BaseActivity;
import net.bradmont.openmpd.fragments.*;
import net.bradmont.openmpd.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class OpenMPD extends BaseActivity {
	
	private Fragment mContent;
    private ProgressDialog waitDialog = null;
    private static OpenMPD instance = null;
    private static DBHelper db;
    public static HomeFragment homeFragment = null;
    public static DebugFragment debugFragment = null;
    public static ContactList contactList = null;
    public static GiftList giftList = null;
    public static ServiceAccountList serviceAccountList = null;

    final static ExecutorService workExecutor = Executors.newSingleThreadExecutor();

	public OpenMPD() {
		super(R.string.app_name);
	}
	
    /** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
        instance = this;
        db = new MPDDBHelper(this);
		super.onCreate(savedInstanceState);
        

		// set the Above View
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		if (mContent == null){
            homeFragment = new HomeFragment();
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

        // if it hasn't been done, populate our TntService table
        TntService t = new TntService();
        ModelList services = t.getAll(); 
        if (services.size() == 0){
            t.createDefaults();
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
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        mContent = getSupportFragmentManager().findFragmentById(R.id.content_frame);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}
	
    public void onClick(View view){
        // retrieve the fragment in R.id.content_frame (the visible main fragment)
        ((OnClickListener) getSupportFragmentManager().findFragmentById(R.id.content_frame)).onClick(view);
    }

    public void queueTask(Runnable r){
        // r.run(); // for debugging thread crashes
        workExecutor.submit(r);
    }

    class MyMessagePasser implements Runnable{
        public String message;
        public Activity activity;
        public void run() {
            //Crouton.showText(activity, message, Style.ALERT);
            Toast.makeText(activity, message, 1000).show();
        }
    }

    public void userMessage(int resourceId){
         userMessage(getResources().getString(resourceId));
    }

    public void userMessage(String message){
        MyMessagePasser m = new MyMessagePasser();
        m.activity = this;
        m.message = message;
        runOnUiThread(m);
    }

    public void showWaitDialog(final int title_id, final int body_id){
        final Context context = this;
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                waitDialog = new ProgressDialog(context);
                waitDialog.setTitle(R.string.checking_login);
                waitDialog.setMessage("Please Wait");
                waitDialog.show();
            }
        });
    }
    public void dismissWaitDialog(){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                waitDialog.dismiss();
            }
        });
    }


	public void switchContent(Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.commit();
		getSlidingMenu().showContent();
	}

    public void moveToFragment(Fragment fragment){
		mContent = fragment;
		getSupportFragmentManager()
		.beginTransaction()
        .addToBackStack(null)
		.replace(R.id.content_frame, fragment)
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		.commit();
    }
    public static OpenMPD getInstance(){
        return instance;
    }

    public void closeDB(){
        if (db != null){
            db.getWritableDatabase().close();
            db.close();
            db = null;
        }
    }

}
