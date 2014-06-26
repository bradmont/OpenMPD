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



public class Address extends DBModel{
    public static final String TABLE = "address";

    public Address(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public Address(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }

    @Override
    public DBModel newInstance(){
        return new Address();
    }

    @Override
    public DBModel newInstance(int id){
        return new Address(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new StringField("addr1"));
        addField(new StringField("addr2"));
        addField(new StringField("addr3"));
        addField(new StringField("addr4"));

        addField(new StringField("city"));
        addField(new StringField("region"));
        addField(new StringField("post_code"));

        addField(new StringField("country_short"));
        addField(new StringField("country_long"));

        addField(new StringField("valid_from"));
        addField(new BooleanField("deliverable"));

        addField(new ForeignKeyField("contact_id", Contact.getReferenceInstance()));

        TABLE_NAME=TABLE;
        super.init();
    }

}
