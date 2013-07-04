package net.bradmont.supergreen;

import net.bradmont.supergreen.models.*;
import net.bradmont.supergreen.fields.*;

import android.database.sqlite.*;
import android.content.Context;
import android.util.Log;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handle connecting our DBModels to the SQLite database.
 * Lifecycle:
 * DBModel() -- constructor
 * registerModel() for each model
 * getReadableDatabase or getWritableDatabase -- this is when the
 *          lifting is done; create &amp; update aren't called until now
 *          all models must be registered before this is done.
 */
public abstract class DBHelper extends SQLiteOpenHelper {

    // data members:
    protected static final String DATABASE_NAME="supergreen.sqlite";
    protected Context context;
    private static LinkedHashMap<String, DBModel> models = new LinkedHashMap<String, DBModel>();

    /**
     *  register a model
     */
    public void registerModel(DBModel model){
        models.put(model.getTableName(), model);
        Log.i("net.bradmont.supergreen.DBHelper", "Reistering model " + model.getTableName());
    }

    /**
     *  Creates tables for each registered model
     */
    public void createDatabase(SQLiteDatabase db){
        Log.i("net.bradmont.supergreen.DBHelper", "Creating Database");
        Log.i("net.bradmont.supergreen.DBHelper", "" + models.size() + " models" );
        for (Map.Entry<String, DBModel> entry: models.entrySet()){
            DBModel model = entry.getValue();
            Log.i("net.bradmont.supergreen.DBHelper", "Executing SQL:" + model.generateCreateSQL());
            db.execSQL(model.generateCreateSQL());
            String [] queries = model.generateInitializeSQL();
            for (int i=0; queries != null && i < queries.length; i++){
                String sql=queries[i];
                db.execSQL(sql);
            }
        }
    }

    public void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion){
        for (Map.Entry<String, DBModel> entry: models.entrySet()){
            DBModel model = entry.getValue();
            String [] queries = model.generateUpdateSQL(oldVersion);
            for (int i=0; queries != null && i < queries.length; i++){
                String sql=queries[i];
                db.execSQL(sql);
            }
        }
    }

    public DBHelper(Context c, int database_version) {
        super(c, DATABASE_NAME, null, database_version);
        registerModels();
        context = c;
    }

    public Context getContext(){
        return context;
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        updateDatabase(db, oldVersion, newVersion);
    }


    public void onCreate(SQLiteDatabase db){
        registerModels();
        createDatabase(db);
    }

    public static DBModel getReferenceModel(String model){
        return models.get(model);
    }
    protected abstract void registerModels();
}
