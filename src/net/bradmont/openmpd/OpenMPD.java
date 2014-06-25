package net.bradmont.openmpd;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;


public class OpenMPD extends Application {

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
