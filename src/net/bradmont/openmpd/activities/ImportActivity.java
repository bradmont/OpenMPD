package net.bradmont.openmpd.activities;

import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.widget.SimpleCursorAdapter;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;


import net.bradmont.openmpd.controllers.*;
import net.bradmont.openmpd.fragments.onboard.*;
import net.bradmont.openmpd.views.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.dao.*;

public class ImportActivity extends Activity {

	private int mTitleRes = R.string.app_name;
    private NoScrollListView mAccountList = null;
    private SimpleCursorAdapter mAdapter = null;
    private static final String ACCOUNT_QUERY = "select service_account._id, name, balance_url, username from service_account join tnt_service on tnt_service_id=tnt_service._id" ;
    private static ImportActivity mInstance = null;
    private Vector<ProgressBar> mProgressBars = new Vector<ProgressBar>(10);



	public ImportActivity() {
        mInstance = this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(mTitleRes);
        setContentView(R.layout.onboard_import);

        mAccountList = (NoScrollListView) findViewById(R.id.account_list);
        Cursor cursor = OpenMPD.getDB().rawQuery(ACCOUNT_QUERY, null);
        String [] columns = {"NAME", "USERNAME", "BALANCE_URL"};
        int [] fields = {R.id.name, R.id.username, R.id.url};

        mAdapter = new SimpleCursorAdapter(this,
                R.layout.onboard_import_list_item, cursor, columns, fields);
        mAdapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
               TextView tv = (TextView) view;
                 switch(columnIndex){
                     case 2:
                         try {
                             tv.setText(new URL(cursor.getString(2)).getHost());
                             return true;
                         } catch (Exception e){
                         }
                     return false;
               }
               return false;
            }
        });
        mAccountList.setAdapter(mAdapter);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        return ;

    }
    @Override
    public void onStart(){
        super.onStart();
        mInstance = this;
        // launch TntImportService
        List<ServiceAccount> accounts = OpenMPD.getDaoSession().getServiceAccountDao().queryBuilder().list();
        int [] account_ids = new int [accounts.size()];
        for (int i = 0; i < accounts.size(); i++){
            if (i == 0){
                // set indeterminate on first bar, as it doesn't seem to happen all the time
            }
            account_ids[i] = accounts.get(i).getId().intValue();
        }

        startService(
            new Intent(this, TntImportService.class)
                .putExtra("net.bradmont.openmpd.account_ids", account_ids)
                .putExtra("net.bradmont.openmpd.force_update", true)
            );

    }
    public void onStop(){
        mInstance = null;
        super.onStop();
    }

    public static void onFinish(){
        if (mInstance != null){
            mInstance.getSharedPreferences("openmpd", Context.MODE_PRIVATE)
                .edit()
                .putInt("onboardState", OpenMPD.ONBOARD_FINISHED)
                .commit(); // Onboarding done

            Intent switchIntent = new Intent(mInstance, HomeActivity.class);
            switchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mInstance.startActivity(switchIntent);
            mInstance.finish();

        }
    }

    public static void setProgress(int accountId, int progressMax, int progress, boolean indeterminate){
        if (mInstance == null){return;}
        ProgressBar bar = null;
        if (mInstance.mProgressBars.size() <= accountId + 1 ||
                mInstance.mProgressBars.get(accountId +1) == null){
            if (accountId == -1){
                // the "Evaluating contacts" progress bar
                bar = (ProgressBar) mInstance.findViewById(R.id.eval_progressbar);
            } else {
                View v = mInstance.findListItem(accountId);
                if (v != null){
                    bar = (ProgressBar) v.findViewById(R.id.progressbar);
                }
            }
            while (mInstance.mProgressBars.size() <= accountId + 1){
                mInstance.mProgressBars.addElement(null);
            }
            mInstance.mProgressBars.set(accountId+1, bar);
        } else {
            bar=mInstance.mProgressBars.get(accountId +1);
        }

        if (bar != null){
            bar.setMax(progressMax);
            bar.setProgress(progress);
            bar.setIndeterminate(indeterminate);
        }
    }

    public static void setStatus(int accountId, final int resId){
        if (mInstance == null){return;}
        TextView text = null;
        if (accountId == -1){
            text = (TextView) mInstance.findViewById(R.id.eval_status);
        } else {
            View v = mInstance.findListItem(accountId);
            if (v != null){
                text = (TextView) v.findViewById(R.id.import_status);
            }
        }
        if (text != null){
            final TextView tv = text;
            mInstance.runOnUiThread(new Runnable(){
                public void run(){
                    tv.setText(resId);
                }
            });
        }
    }

    public View findListItem(int accountId){
        Cursor c = mAdapter.getCursor();
        c.moveToFirst();
        // TODO: cache these values
        while (c.getInt(0) != accountId && !c.isAfterLast()){
            c.moveToNext();
        }
        int first = mAccountList.getFirstVisiblePosition() - mAccountList.getHeaderViewsCount();
        int wanted = c.getPosition();
        if (wanted < first || wanted >= first + mAccountList.getChildCount()){
            return null;
        }
        return mAccountList.getChildAt(wanted - first);
    }

}
