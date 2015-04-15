package net.bradmont.openmpd;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;


public class OpenMPD extends Application {

    // public constants
    public static final int ONBOARD_FIRST_RUN=0;
    public static final int ONBOARD_ACCOUNT_ADDED=1; // not used, legacy value
    public static final int ONBOARD_IMPORTING=2;
    public static final int ONBOARD_FINISHED = 3;

    private static OpenMPD instance;
    private static MPDDBHelper db;

    public OpenMPD(){
        super();
        instance = this;
    }
    public void onCreate(){
        db = new MPDDBHelper(this);
    }

    public static OpenMPD get() {
        return instance;
    }

    public static MPDDBHelper getDB(){
        if (db == null){
            db = new MPDDBHelper(instance);
        }
        return db;
    }

    public static void closeDB(){
        if (db != null){
            db.getWritableDatabase().close();
            db.close();
            db = null;
        }
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
