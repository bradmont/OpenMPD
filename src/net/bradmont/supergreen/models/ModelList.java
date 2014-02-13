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
    private String [] lookupFields = null;
    private String [] lookupValues = null;
    private boolean initialised = false;

    public ModelList(DBModel referenceInstance, String lookupFieldName, int value){
        this(referenceInstance, lookupFieldName, Integer.toString(value));
    }

    public ModelList(DBModel referenceInstance, String lookupFieldName, float value){
        this(referenceInstance, lookupFieldName, Float.toString(value));
    }

    public ModelList(DBModel referenceInstance, String lookupFieldName, boolean value){
        this(referenceInstance, lookupFieldName, value?1:0);
    }

    public ModelList(DBModel referenceInstance){
        this(referenceInstance, null, null);
    }

    public ModelList(DBModel referenceInstance, String lookupField, String value){
        this.referenceInstance = referenceInstance;
        if (lookupField != null && value != null){
            this.lookupFields = new String[1];
            this.lookupFields[0] = lookupField ;
            this.lookupValues = new String[1];
            this.lookupValues[0] = value ;
        }
        db = referenceInstance.getDbh().getReadableDatabase();
    }
    
    /* Set a field to order the ModelList.
     */
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

    public ModelList filter(String field, int value){
        return filter(field, Integer.toString(value));
    }

    public ModelList filter(String field, float value){
        return filter(field, Float.toString(value));
    }

    /*
     * Filter the list by key, value. Filters can be chained, like so:
        myList.filter('fname', 'george')
            .filter('lname', 'costanza')
            .filter('hair', 'none');
     */
    public ModelList filter(String field, String value){
        String [] fields = new String [lookupFields.length+1];
        String [] values = new String [lookupFields.length+1];
        fields [fields.length-1] = field;
        values [fields.length-1] = value;
        for (int i = 0; i < lookupFields.length; i++){
            fields[i] = lookupFields[i];
            values[i] = lookupValues[i];
        }
        lookupFields = fields;
        lookupValues = values;
        setUnInitialised();
        return this;
    }

    private void init(){
        initialised = true;
        String [] args = new String[1];

        args = lookupValues;

        String table = referenceInstance.getTableName();
        String conditions = null;

        // build the where conditions
        if (lookupFields != null){
            conditions = "";
            for (int i = 0; i < lookupValues.length; i++){
                if (conditions != ""){
                    conditions = conditions + " and ";
                }

                String lookup_column = referenceInstance.getField(lookupFields[i]).getColumnName();
                // we can't look up a literal %...
                if (lookupValues[i].contains("%")){
                    conditions = conditions + lookup_column+" like ?";
                } else {
                    conditions = conditions + lookup_column+"=?";
                }
            }
        }
        cur = db.query(table, null, conditions, args, null, null, orderBy);

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

    /** Returns the number of elements currently in the ModelList
     */
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
