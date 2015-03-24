package net.bradmont.openmpd.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Toast;


import net.bradmont.openmpd.fragments.onboard.*;
import net.bradmont.openmpd.views.*;
import net.bradmont.openmpd.R;

public class OnboardActivity extends FragmentActivity {

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;

	private int mTitleRes = R.string.app_name;

    final static ExecutorService workExecutor = Executors.newSingleThreadExecutor();
    protected ProgressDialog waitDialog = null;

	public OnboardActivity() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(mTitleRes);

        setContentView(R.layout.onboard_main);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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

}
