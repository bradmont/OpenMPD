package net.bradmont.openmpd;

import net.bradmont.openmpd.models.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.models.*;

import android.content.Context;
import android.database.sqlite.*;



public class MPDDBHelper extends DBHelper{
    private static MPDDBHelper instance = null;
    private static int DATABASE_VERSION = 4;

    @Override
    protected void registerModels(){
        instance = this;
        registerModel(new Contact());
        registerModel(new Address());
        registerModel(new EmailAddress());
        registerModel(new PhoneNumber());
        registerModel(new Gift());
        registerModel(new ContactStatus());
        registerModel(new TntService());
        registerModel(new ServiceAccount());
    }

    public MPDDBHelper(Context c) {
        super(c, DATABASE_VERSION);
        if (instance == null){
            instance = this;
        }
    }
   
    public static MPDDBHelper get(){
        return instance;
    }
    public static ModelList filter(String table_name, String field_name, int value){
        return get()
            .getReferenceModel(table_name)
            .filter(field_name, value);
    }
    
    public static ModelList filter(String table_name, String field_name, float value){
        return get()
            .getReferenceModel(table_name)
            .filter(field_name, value);
    }
    public static ModelList filter(String table_name, String field_name, String value){
        return get()
            .getReferenceModel(table_name)
            .filter(field_name, value);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        super.onCreate(db);
    }
}

