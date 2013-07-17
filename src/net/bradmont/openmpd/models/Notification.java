package net.bradmont.openmpd.models;
import net.bradmont.openmpd.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.*;
import net.bradmont.supergreen.models.DBModel;

import android.database.Cursor;
import android.database.sqlite.*;
import android.app.Activity;

import android.util.Log;
import android.widget.SimpleCursorAdapter;


public class Notification extends DBModel{
    public static final String TABLE = "notification";
            
    // notification types
    public static final int CHANGE_PARTNER_TYPE = 1;
    public static final int CHANGE_STATUS = 2;
    public static final int CHANGE_AMOUNT = 3;
    public static final int SPECIAL_GIFT = 4;
    public static final int USER_MESSAGE = 5; // not currently used

    // notification statuses
    public static final int STATUS_NEW = 1;
    public static final int STATUS_NOTIFIED = 2;
    public static final int STATUS_ACKNOWLEDGED = 3;


    public Notification(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public Notification(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }

    @Override
    public DBModel newInstance(){
        return new Notification();
    }

    @Override
    public DBModel newInstance(int id){
        return new Notification(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new ForeignKeyField("contact", Contact.getReferenceInstance()));
        getField("contact").setColumnName("contact_id");

        addField(new IntField("type"));

        addField(new IntField("status"));
        getField("status").setDefault(STATUS_NEW);

        addField(new StringField("message")); 


        TABLE_NAME=TABLE;
        super.init();
    }
    public String [] generateUpdateSQL(int oldversion){
        if (oldversion < 5){
            String [] result = new String[1];
            result[0] = generateCreateSQL();
            return result;
        }
        return null;
    }

}
