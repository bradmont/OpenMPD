package net.bradmont.openmpd.controllers;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import net.bradmont.openmpd.helpers.Log;

import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.activities.*;

public class AccountVerifyService extends Service {

    private final IBinder mBinder = new AccountVerifyBinder();
    private boolean mIsRunning = false;
    OnFinishHandler mFinishCallback = null;

    public class AccountVerifyBinder extends Binder{
        public AccountVerifyService getService(){
            return AccountVerifyService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean isRunning(){
        return mIsRunning;
    }
    public void setOnFinishHandler(OnFinishHandler handler){
        mFinishCallback = handler;
    }

    public void verifyAccount(final OnboardActivity activity, final ServiceAccount account){
        activity.queueTask(new Runnable(){
            public void run(){
                mIsRunning = true;
                // try to log in
                Log.i("net.bradmont.openmpd", "getting query.ini");
                TntImporter importer = new TntImporter(AccountVerifyService.this, account);
                Log.i("net.bradmont.openmpd", "verifying account");
                importer.processQueryIni((TntService) account.getRelated("tnt_service_id"));
                if (importer.verifyAccount()){
                    activity.runOnUiThread(new Runnable(){
                        public void run(){
                            Log.i("net.bradmont.openmpd", "account verified");
                            mFinishCallback.onFinish(true);
                        }
                    });
                } else {
                    activity.runOnUiThread(new Runnable(){
                        public void run(){
                            Log.i("net.bradmont.openmpd", "account not verified");
                            mFinishCallback.onFinish(false);
                        }
                    });
                }
                mIsRunning = false;
            }
        });
    }

    public interface OnFinishHandler{
        public void onFinish(boolean success);
    }

}
