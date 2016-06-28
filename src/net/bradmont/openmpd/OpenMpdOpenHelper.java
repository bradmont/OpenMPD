package net.bradmont.openmpd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import java.io.IOException;

import de.greenrobot.dao.DbUtils;
import net.bradmont.openmpd.dao.*;

public class OpenMpdOpenHelper extends DaoMaster.DevOpenHelper{
    public OpenMpdOpenHelper(Context context, String name, CursorFactory factory) {
        super(context, name, factory);
    }
    public void onCreate (SQLiteDatabase db){
        super.onCreate(db);
        createAllViews(db);
    }

    private void createAllViews(SQLiteDatabase db){
        try {
            DbUtils.executeSqlScript(OpenMPD.get(), db, "views.sql");
        } catch (IOException e){
            Log.i("net.bradmont.openmpd", "UNABLE TO OPEN VIEWS SCRIPT");
        }
    }
}
