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



public class PhoneNumber extends DBModel{
    public static final String TABLE = "phone_number";

    public PhoneNumber(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public PhoneNumber(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }
    @Override
    public DBModel newInstance(){
        return new PhoneNumber();
    }
    @Override
    public DBModel newInstance(int id){
        return new PhoneNumber(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new StringField("number"));
        addField(new DateField("added_date"));
        addField(new BooleanField("operational"));
        addField(new StringField("label"));

        addField(new ForeignKeyField("contact_id", Contact.getReferenceInstance()));

        TABLE_NAME=TABLE;
        super.init();
    }

}
