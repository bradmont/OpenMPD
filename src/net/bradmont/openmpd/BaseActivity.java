package net.bradmont.openmpd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import net.bradmont.openmpd.fragments.*;
import net.bradmont.openmpd.views.*;

public class BaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected ListFragment mFrag;
	protected Fragment mContent;
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

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			mFrag = new ContactList();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		/*case R.id.github:
			Util.goToGitHub(this);
			return true;*/
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
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
    public static int getVersion() {
        int v = 0;
        try {
            v = getInstance().getPackageManager().getPackageInfo(getInstance().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // Huh? Really?
        }
        return v;
    }
    public static BaseActivity getInstance(){
        return instance;
    }


}
