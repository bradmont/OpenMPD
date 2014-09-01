package net.bradmont.openmpd;

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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Toast;


import net.bradmont.openmpd.fragments.*;
import net.bradmont.openmpd.views.*;

public class BaseActivity extends FragmentActivity {

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

	private int mTitleRes;
    private static BaseActivity instance = null;

    final static ExecutorService workExecutor = Executors.newSingleThreadExecutor();
    protected ProgressDialog waitDialog = null;

	public BaseActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        instance = this;
		setTitle(mTitleRes);

        setContentView(R.layout.viewpager);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        // set up tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new HomeTabListener();
        actionBar.addTab(
            actionBar.newTab().setText(R.string.analytics).setTabListener(tabListener));
        actionBar.addTab(
            actionBar.newTab().setText(R.string.contacts).setTabListener(tabListener));
        actionBar.addTab(
            actionBar.newTab().setText(R.string.notifications).setTabListener(tabListener));

        mPager.setOnPageChangeListener(
            new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    // swipe handler
                    getActionBar().setSelectedNavigationItem(position);
                }
            }
        );

        

	}

    private class HomeTabListener implements ActionBar.TabListener {

        public HomeTabListener(){}
        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            return;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

    }

    private class HomePagerAdapter extends FragmentPagerAdapter {

        private Fragment mAnalyticsFragment = null;
        private Fragment mContactsFragment = null;
        private Fragment mNotificationsFragment = null;

        public HomePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    if (mAnalyticsFragment == null){
                        mAnalyticsFragment = new AnalyticsFragment();
                    }
                    return mAnalyticsFragment;
                case 1:
                    if (mContactsFragment == null){
                        mContactsFragment = new ContactList();
                    }
                    return mContactsFragment;
                case 2:
                    if (mNotificationsFragment == null){
                        mNotificationsFragment = new ContactList();
                    }
                    return mNotificationsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }


    }



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return true;
		/*case R.id.github:
			Util.goToGitHub(this);
			return true;*/
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

    public static BaseActivity getInstance(){
        return instance;
    }


}
