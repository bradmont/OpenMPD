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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.lang.StringBuilder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONException;



public class ServiceAccount extends DBModel{
    public static final String TABLE = "service_account";

    public ServiceAccount(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public ServiceAccount(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }
    @Override
    public DBModel newInstance(){
        return new ServiceAccount();
    }
    @Override
    public DBModel newInstance(int id){
        return new ServiceAccount(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new ForeignKeyField("tnt_service_id", MPDDBHelper.getReferenceModel("tnt_service")));

        addField(new StringField("username"));
        addField(new StringField("password"));
        addField(new DateField("last_import"));

        TABLE_NAME=TABLE;
        super.init();
    }

}
