package net.bradmont.openmpd.models;
import net.bradmont.openmpd.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.*;
import net.bradmont.supergreen.models.DBModel;

import android.database.Cursor;
import android.database.sqlite.*;
import android.app.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.SimpleCursorAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;



public class LogItem extends DBModel{
    public static final String TABLE = "log";

    public LogItem(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public LogItem(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }

    @Override
    public DBModel newInstance(){
        return new LogItem();
    }

    @Override
    public DBModel newInstance(int id){
        return new LogItem(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new StringField("msg1"));
        addField(new StringField("msg2"));
        addField(new StringField("msg3"));
        addField(new StringField("timestamp"));

        TABLE_NAME=TABLE;
        super.init();
    }

    public static void logError(String a, String b, Exception e){
        e.printStackTrace();
        String stackTrace = "";
        for (int i = 0; i < e.getStackTrace().length; i++){
            stackTrace = stackTrace + "\n" + e.getStackTrace()[i].toString();
        }
        stackTrace = e.toString() + "\n" + stackTrace;
        logError(a, b, stackTrace);
    }
    public static void logError(String a){
        _logError(a, null, null);
    }
    public static void logError(String a, String b){
        _logError(a, b, null);
    }
    public static void logError(String a, String b, String c){
        _logError(a, b, c);
    }
    public static void _logError(String a, String b, String c){
        LogItem i = new LogItem();
        i.setValue("msg1", a);
        i.setValue("msg2", b);
        i.setValue("msg3", c);
        i.setValue("timestamp", new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        i.dirtySave();
    }
    public String [] generateUpdateSQL(int oldversion){
        if (oldversion < 11){
            String [] result = new String[1];
            result[0] = generateCreateSQL();
            return result;
        }
        return null;
    }
}
