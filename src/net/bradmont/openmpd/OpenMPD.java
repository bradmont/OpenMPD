package net.bradmont.openmpd;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import net.bradmont.openmpd.dao.DaoMaster;


public class OpenMPD extends Application {

    // public constants
    public static final int ONBOARD_FIRST_RUN=0;
    public static final int ONBOARD_ACCOUNT_ADDED=1; // not used, legacy value
    public static final int ONBOARD_IMPORTING=2;
    public static final int ONBOARD_FINISHED = 3;

    private static OpenMPD instance;

    private static DaoMaster mDaoMaster = null;

    public OpenMPD(){
        super();
        instance = this;
    }
    public void onCreate(){
        mDaoMaster = new DaoMaster(new DaoMaster.OpenHelper(this, "openmpd.sqlite", null));
    }

    public static OpenMPD get() {
        return instance;
    }

    public static SQLiteOpenHelper getDB(){
        /*if (db == null){
            db = new MPDDBHelper(instance);
        }*/ // DEPRECATED
        return mDaoMaster.getDatabase();;
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
