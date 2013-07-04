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
        getField("balance_url").setDefault("account_balance.php");
        addField(new StringField("balance_formdata"));
        getField("balance_formdata").setDefault("Action=TntBalance&Username=%s&Password=%s");

        // donations
        addField(new StringField("donations_url"));
        getField("donations_url").setDefault("donordata.php");
        addField(new StringField("donations_formdata"));
        getField("donations_formdata").setDefault("Action=TntDonList&Username=%s&Password=%s&DateFrom=%s&DateTo=%s");

        // addresses
        addField(new StringField("addresses_url"));
        getField("addresses_url").setDefault("donordata.php");
        addField(new StringField("addresses_formdata"));
        getField("addresses_formdata").setDefault("Action=TntAddrList&Username=%s&Password=%s&DateFrom=%s");

        // addresses_by_personids
        addField(new StringField("addresses_by_personids_url"));
        getField("addresses_by_personids_url").setDefault("donordata.php");
        addField(new StringField("addresses_by_personids_formdata"));
        getField("addresses_by_personids_formdata").setDefault("Action=TntAddrList&Username=%s&Password=%s&PID=%s");


        TABLE_NAME=TABLE;
        super.init();
    }

    public void createDefaults(){
        // Abusing this method to instantiate TntServices rather than return SQL.
        String raw_json = readRawTextFile(MPDDBHelper.get().getContext(), R.raw.services);
        try {
            JSONObject json = new JSONObject(raw_json);
            String [] fieldnames = { "name", "base_url", "balance_url", 
                "balance_formdata", "donations_url", "donations_formdata",
                "addresses_url", "addresses_formdata", "addresses_by_personids_url",
                "addresses_by_personids_formdata" };
            
            for (Iterator<String> iKeys= json.keys(); iKeys.hasNext(); ){
                String key = iKeys.next();
                JSONObject obj = json.getJSONObject(key);
                TntService serv = (TntService) newInstance();
                serv.setValue("name_short", key);

                if (obj.has("http_auth")){
                    serv.setValue("http_auth", obj.getBoolean("http_auth"));   
                }
                for (String field: fieldnames){
                    if (obj.has(field)){
                        serv.setValue(field, obj.getString(field));   
                    }
                }
                serv.dirtySave();
            }
        } catch (JSONException e){ 
            Log.i("net.bradmont.openmpd", "JSONException caught.");
        }
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
}
