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

public class AccountListActivity extends BaseActivity {
	
	public AccountListActivity() {
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
            mContent = new ServiceAccountList();
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

	}

}
