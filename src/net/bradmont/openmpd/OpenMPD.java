package net.bradmont.openmpd;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import net.bradmont.openmpd.dao.DaoMaster;
import net.bradmont.openmpd.dao.DaoSession;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class OpenMPD extends Application {

    // public constants
    public static final int ONBOARD_FIRST_RUN=0;
    public static final int ONBOARD_ACCOUNT_ADDED=1; // not used, legacy value
    public static final int ONBOARD_IMPORTING=2;
    public static final int ONBOARD_FINISHED = 3;

    private static OpenMPD instance;

    private static SQLiteDatabase mDatabase = null;
    private static DaoMaster mDaoMaster = null;
    private static DaoSession mDaoSession = null;

    public OpenMPD(){
        super();
        instance = this;
    }
    public void onCreate(){
        mDatabase = new OpenMpdOpenHelper(this, "openmpd.sqlite", null).getWritableDatabase();
        mDaoMaster = new DaoMaster(mDatabase);
        mDaoSession = mDaoMaster.newSession();
    }

    public static OpenMPD get() {
        return instance;
    }

    public static DaoMaster getDaoMaster(){
        return mDaoMaster;
    }
    public static DaoSession getDaoSession(){
        return mDaoSession;
    }

    public static SQLiteDatabase getDB(){
        /*if (mDaoMaster == null){
            mDaoMaster = new DaoMaster(new DaoMaster.OpenHelper(this, "openmpd.sqlite", null));
        }*/ // DEPRECATED
        return mDatabase;
    }

    public static int getVersion() {
        int v = 0;
        try {
            v = get().getPackageManager().getPackageInfo(get().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // Huh? Really?
        }
        return v;
    }

}
