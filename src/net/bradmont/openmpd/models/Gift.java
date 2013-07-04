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



public class Gift extends DBModel{
    public static final String TABLE = "gift";

    public Gift(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public Gift(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }

    @Override
    public DBModel newInstance(){
        return new Gift();
    }

    @Override
    public DBModel newInstance(int id){
        return new Gift(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new IntField("tnt_people_id"));
        addField(new DateField("date"));
        addField(new StringField("month"));
        addField(new MoneyField("amount"));

        addField(new StringField("motivation_code"));
        addField(new IntField("account"));

        addField(new StringField("tnt_donation_id"));
        getField("tnt_donation_id").setExtraArguments("unique");

        TABLE_NAME=TABLE;
        super.init();
    }

    public String [] generateUpdateSQL(int oldversion){
        if (oldversion < 4){
            String [] result = new String[2];
            result[0] = "alter table gift add " + getField("month").getSQLDefinition() + ";";
            result[1] = "update gift set month=substr(date, 0, 8);";
            return result;
        }
        return null;
    }

}
