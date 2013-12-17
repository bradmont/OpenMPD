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
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONException;




public class QuickMessage extends DBModel{
    public static final String TABLE = "quick_message";

    public QuickMessage(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public QuickMessage(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }

    @Override
    public DBModel newInstance(){
        return new QuickMessage();
    }

    @Override
    public DBModel newInstance(int id){
        return new QuickMessage(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new StringField("name"));
        addField(new StringField("subject"));
        addField(new StringField("body"));
        addField(new StringField("notification_type"));
        addField(new BooleanField("customized"));
        getField("customized").setDefault(false);

        TABLE_NAME=TABLE;
        super.init();
    }
    public void createDefaults(){
        String raw_json = TntService.readRawTextFile(MPDDBHelper.get().getContext(), R.raw.quick_messages);
        try {
            JSONObject json = new JSONObject(raw_json);
            String [] fieldnames = { "subject", "body", "notification_type"};
            
            for (Iterator<String> iKeys= json.keys(); iKeys.hasNext(); ){
                String key = iKeys.next();
                JSONObject obj = json.getJSONObject(key);
                QuickMessage message = new QuickMessage();
                message.setValue("name", key);

                for (String field: fieldnames){
                    if (obj.has(field)){
                        message.setValue(field, obj.getString(field));   
                    }
                }
                message.dirtySave();
            }
        } catch (JSONException e){ 
            Log.i("net.bradmont.openmpd", "JSONException caught.");
        }
    }   

    public String [] generateUpdateSQL(int oldversion){
        if (oldversion < 15){
            String [] result = new String[1];
            result[0] = generateCreateSQL();
            return result;
        }
        return null;
    }

}
