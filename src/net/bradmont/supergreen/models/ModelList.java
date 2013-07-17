package net.bradmont.supergreen.models;

import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.models.*;
import net.bradmont.supergreen.fields.constraints.*;

import android.database.sqlite.*;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import android.content.ContentValues;
import android.content.Context;

import android.util.Log;

import java.lang.Iterable;
import java.util.Iterator;

public class ModelList {
    private Cursor cur = null;
    private DBModel referenceInstance;
    private DBModel [] models = null;
    protected SQLiteDatabase db = null;

    private String orderBy = null;
    private String lookupField = null;
    private String lookupValue = null;
    private boolean is_numeric = false;
    private boolean initialised = false;

    public ModelList(DBModel referenceInstance, String lookupFieldName, int value){
        this(referenceInstance, lookupFieldName, Integer.toString(value));
        is_numeric = true;
    }

    public ModelList(DBModel referenceInstance, String lookupFieldName, float value){
        this(referenceInstance, lookupFieldName, Float.toString(value));
        is_numeric = true;
    }

    public ModelList(DBModel referenceInstance, String lookupFieldName, boolean value){
        this(referenceInstance, lookupFieldName, value?1:0);
    }

    public ModelList(DBModel referenceInstance, String lookupField, String value){
        this.referenceInstance = referenceInstance;
        this.lookupField = lookupField;
        this.lookupValue = value;
        db = referenceInstance.getDbh().getReadableDatabase();
    }
    
    public ModelList orderBy(String orderBy){
        this.orderBy = orderBy;
        setUnInitialised();
        return this;
    }

    private void setUnInitialised(){
        models = null;
        initialised = false;
        cur = null;
    }

    private void init(){
        initialised = true;
        String [] args = new String[1];
        args[0] = lookupValue;
        String table = referenceInstance.getTableName();
        if (lookupField != null){
            String lookup_column = referenceInstance.getField(lookupField).getColumnName();
            // do select & get Cursor
            if (is_numeric == true){
                // second argument is null, signifying select *
                cur = db.query(table, null, String.format("%s = ?", lookup_column),
                    args, null, null, orderBy);
            } else {
                if (lookupValue.contains("%")){
                    cur = db.query(table, null, String.format("%s like ?", lookup_column),
                        args, null, null, orderBy);
                } else {
                    cur = db.query(table, null, String.format("%s = ?", lookup_column),
                        args, null, null, orderBy);
                }
            }
        } else {
            cur = db.query(table, null, null,
                null, null, null, orderBy);
        }
        // Instantiate models, null its elements
        models = new DBModel[cur.getCount()];
        for (int i = 0; i < models.length; i++){
            if (i == 0){
                // we want a referenceInstance internal to the list to
                // not get in the way of garbage collection. If the
                // list has length 0, then we don't need a referenceInstance
                // anyway.
                this.referenceInstance = get(0);
            }
            models[i] = null;
        }
    }

    /**
     * return the DBModel at position. 
     */
    public DBModel get(int position){
        if (initialised == false){
            init();
        }
        if (models[position] == null){
            cur.moveToPosition(position);
            models[position] = referenceInstance.newInstance();
            models[position].loadFromRow(cur);
        }
        return models[position];
    }

    public int size(){
        if (initialised == false){
            init();
        }
        return models.length;
    }

    /**
      * Access the cursor directly. Helpful for populating listviews.
      */
    public Cursor getCursor(){
        if (initialised == false){
            init();
        }
        return cur;
    }
}
