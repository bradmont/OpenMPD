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



public class TntService extends DBModel{
    public static final String TABLE = "tnt_service";

    public TntService(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public TntService(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }
    @Override
    public DBModel newInstance(){
        return new TntService();
    }
    @Override
    public DBModel newInstance(int id){
        return new TntService(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new StringField("name"));
        addField(new StringField("name_short"));
        addField(new StringField("base_url"));

        addField(new BooleanField("http_auth"));
        getField("http_auth").setDefault(false);

        // balance
        addField(new StringField("balance_url"));
        addField(new StringField("balance_formdata"));

        // donations
        addField(new StringField("donations_url"));
        addField(new StringField("donations_formdata"));

        // addresses
        addField(new StringField("addresses_url"));
        addField(new StringField("addresses_formdata"));

        // addresses_by_personids
        addField(new StringField("addresses_by_personids_url"));
        addField(new StringField("addresses_by_personids_formdata"));

        addField(new StringField("query_ini_url"));
        addField(new BooleanField("untested_service"));
        getField("untested_service").setDefault(false);

        TABLE_NAME=TABLE;
        super.init();
    }

    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
                return null;
        }
        return text.toString();
     }

     private String [] [] getArgs(String formdata){
        String [] pairs = formdata.split("&");
        String [] [] results = new String [pairs.length][];
        for (int i = 0; i < pairs.length; i++){
            results[i] = pairs[i].split("=");
        }
        return results;
     }

     private String getArgNameByValue(String formdata, String value){
        String [] [] args = getArgs(formdata);
        for (int i = 0; i < args.length; i++){
            if (args[i][1].equals(value)){
                return args[i][0];
            }
        }
        return null;
     }

     private String getValueByArgName(String formdata, String key){
        String [] [] args = getArgs(formdata);
        for (int i = 0; i < args.length; i++){
            if (args[i][0].equals(key)){
                return args[i][1];
            }
        }
        return null;
     }
     
     public String getUsernameKey(){
        return getArgNameByValue(getString("balance_formdata"), "$ACCOUNT$");
     }
     public String getPasswordKey(){
        return getArgNameByValue(getString("balance_formdata"), "$PASSWORD$");
     }

     public String getBalanceAction(){
        return getValueByArgName(getString("balance_formdata"), "Action");
     }

     public String getDonationsAction(){
        return getValueByArgName(getString("donations_formdata"), "Action");
     }

     public String getAddressesAction(){
        return getValueByArgName(getString("addresses_formdata"), "Action");
     }

     public String getAddressesByPersonidsAction(){
        return getValueByArgName(getString("addresses_by_personids_formdata"), "Action");
     }
}
