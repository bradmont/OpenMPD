package net.bradmont.supergreen.models;

import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.*;

import android.database.sqlite.*;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.MenuItem;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import java.io.PrintWriter;
import java.io.StringWriter;


// TODO: Modify to allow non-integer pirmary keys
    // WONTFIX: ListView requires an int pk anyway

// TODO: Add DB version tag to Field
//       Write code to auto-generate create & update SQL

public abstract class DBModel {
    /** id is kept as a convenience. Table primary keys must be integers. */
    int id = 0;
    LinkedHashMap<String, DBField> fields = new LinkedHashMap<String, DBField>(); // preserves insertion order
    HashMap<String, String> fields_by_column_name = new HashMap<String, String>(); // stores columname:fieldname pairs for Fields that have a custom columname
    DBField primaryKey;

    boolean record_exists = false;

    protected static SQLiteOpenHelper dbh = null;
    protected SQLiteDatabase dbr = null;
    protected static SQLiteDatabase dbw = null;
    protected Context context = null;

    protected String TABLE_NAME = null;

    public DBModel(SQLiteOpenHelper dbh, String table_name){
        // create a new DBModel. Won't be written to DB until save() is called.
        this.dbh = dbh;
        this.TABLE_NAME = table_name;
    }

    public DBModel(SQLiteOpenHelper dbh, String table_name, int _id){
        // loads an existing DBModel from the database by id
        this.dbh = dbh;
        this.TABLE_NAME = table_name;
        if (_id == 0){
            throw new RowDoesNotExistException(_id);
        }
        this.id = _id;
    }

    /**
     * Allows the model to return SQL statements to initialize its table,
     * for exampel to populate it with default data. Runs only after initial
     * table creation.
    */
    public String [] generateInitializeSQL(){
        return new String[0];
    }

    public String generateCreateSQL(){
        String sql = String.format("CREATE TABLE %s ( %s );", TABLE_NAME, "%s");

        // column definitions:
        int i = 0;
        String field_defs = "";
        for (Map.Entry<String, DBField> entry: fields.entrySet()){
            DBField field = entry.getValue();
            String oldArgs = field.getExtraArguments();
            if (field == primaryKey){
                field.setExtraArguments("PRIMARY KEY " + oldArgs);
            }

            // why doesn't Java have a String.join function?
            if (i == 0){
                field_defs = field.getSQLDefinition() ;
            } else {
                field_defs = field_defs + ", " + field.getSQLDefinition() ;
            }

            if (field == primaryKey){ field.setExtraArguments(oldArgs); }
            i++;
        }
        // table constraints:
        i = 0;
        String table_constraints = "";
        for (Map.Entry<String, DBField> entry: fields.entrySet()){
            DBField field = entry.getValue();
            String constraint = field.getSQLTableConstraint();
            if (constraint != ""){
                if (i == 0){
                    table_constraints = constraint;
                } else {
                    table_constraints = table_constraints + ", " + constraint;
                }
            }
            i++;
        }

        sql = String.format(sql, field_defs + " " + table_constraints);
        return sql;
    }

    public String [] generateUpdateSQL(int oldversion){
        // TODO
        return new String[0];
    }

    public void setContext(Context context){
        this.context = context;
    }

    public Context getContext() { return context;}
    protected void addField(DBField field){
        field.setModel(this);
        fields.put(field.getName(), field);
        if (field.getName() != field.getColumnName()){
            fields_by_column_name.put(field.getColumnName(), field.getName());
        }
    }
    public void setFieldColumnName(String fieldName, String columnName){
        // record that a field doesn't use the same column name as its field name
        fields_by_column_name.put(columnName, fieldName);
    }

    protected void setPrimaryKey(DBField field){
        primaryKey = field;
    }
    public DBField getPrimaryKey(){
        return primaryKey;
    }

    /**
     * Get a DBField belonging to this DBModel
     */
    public DBField getField(String name){
        return fields.get(name);
    }

    /**
     * Directly retrieve the int value of a field
     */
    public int getInt(String fieldName){
        return getField(fieldName).getInt();
    }

    /**
     * Directly retrieve the String value of a field
     */
    public String getString(String fieldName){
        return getField(fieldName).getString();
    }

    /**
     * Directly retrieve the float value of a field
     */
    public float getFloat(String fieldName){
        return getField(fieldName).getFloat();
    }

    /**
     * Directly retrieve the boolean value of a field
     */
    public boolean getBoolean(String fieldName){
        return getField(fieldName).getBoolean();
    }

    /**
     * Directly retrieve the Date value of a field
     */
    public Calendar getCalendar(String fieldName){
        return getField(fieldName).getCalendar();
    }
    /**
     * Directly retrieve a DBModel of a related field
     */
    public DBModel getRelated(String fieldName){
        return getField(fieldName).getRelated();
    }

    /**
     * Retrive an array of DBModels for a One-to-Many relationship. 
     * relatedModel is an instance of the DBModel that references this DBModel
     * fieldName is the ForeignKey of relatedModel that refers to this DBModel
     */
    public ModelList getRelatedList(DBModel relatedModel, String fieldName){
        return relatedModel.filter(fieldName, id);
    }

    /**
      * Return a list of all instances of this model.
      */
    public ModelList getAll(){
        return new ModelList(this, null, null);
    }

    /**
      * Return a list of this model, filtering where fieldName = value
      */
    public ModelList filter(String fieldName, int value){
        return new ModelList(this, fieldName, value);
    }

    /**
      * Return a list of this model, filtering where fieldName = value
      */
    public ModelList filter(String fieldName, float value){
        return new ModelList(this, fieldName, value);
    }

    /**
      * Return a list of this model, filtering where fieldName = value
      */
    public ModelList filter(String fieldName, String value){
        return new ModelList(this, fieldName, value);
    }

    /**
      * Return a list of this model, filtering where fieldName = value
      */
    public ModelList filter(String fieldName, boolean value){
        return new ModelList(this, fieldName, value);
    }

    /**
      * Get an instance by a unique value other than primary key. No
      * sanity checking is done, so verify that row is unique.
      */
    public DBModel getByField(String field, int value){
        return getByField(field, Integer.toString(value));
    }

    /**
      * Get an instance by a unique value other than primary key. No
      * sanity checking is done, so verify that row is unique.
      */
    public DBModel getByField(String field, String value){
        String [] args = new String[1];
        String [] columns = new String[1];
        args[0] = value;
        columns[0] = getPrimaryKey().getColumnName();
        Cursor c = getDbr().query(getTableName(), columns, String.format("%s = ?", field), args, null, null, null);

        if (c.getCount() == 0) { return null;}
        c.moveToFirst();
        DBModel result = newInstance(c.getInt(0));
        c.close();
        return result;
    }

    // Directly assign values to fields:

    /**
     * Set the value of a field to an int, for supported fields
     */
    public void setValue(String fieldName, int value){
        getField(fieldName).setValue(value);
    }

    /**
     * Set the value of a field to a String, for supported fields
     */
    public void setValue(String fieldName, String value){
        getField(fieldName).setValue(value);
    }

    /**
     * Set the value of a field to a Float, for supported fields
     */
    public void setValue(String fieldName, float value){
        getField(fieldName).setValue(value);
    }

    /**
     * Set the value of a field to a boolean, for supported fields
     */
    public void setValue(String fieldName, boolean value){
        getField(fieldName).setValue(value);
    }
    /**
     * Set the value of a field to a Date, for supported fields
     */
    public void setValue(String fieldName, Calendar value){
        getField(fieldName).setValue(value);
    }

    /**
     * Set the value of a ForeignKey to a related DBModel
     */
    public void setValue(String fieldName, DBModel value){
        getField(fieldName).setValue(value);
    }
 
    public Set<String> getKeys(){
        return fields.keySet();
    }

    /**
     * returns the name of the table this model is using
    */
    public String getTableName(){
        return TABLE_NAME;
    }
    protected void init(){
        if (id != 0){ // only load if it's apropriate
            String sql = String.format("select * from %s where %s=%d", 
                    TABLE_NAME, primaryKey.getColumnName(), id);
            setupDbr();
            Cursor cur = getDbr().rawQuery(sql, null);
            if (cur.getCount() == 0){
                throw new RowDoesNotExistException(id);
            } else if (cur.getCount() > 1){
                throw new RowDoesNotExistException("Record is not unique.", id);
            }
            cur.moveToFirst();
            loadRecord(cur);
            cur.close();
            record_exists = true;
        }
    }

    /**
     * Attempt to save the model. If any fields don't meet their constraints,
     * this will throw ConstraintError. Returns the new object id.
     */
    public int save() throws ConstraintError{
        // if the record exists, update it. Otherwise, save it.
        validate();
        if (record_exists){
            update();
        } else {
            create_record();
        }
        return id;
    }

    /**
     * Attempts to save the model, and discards any exceptions. Only do this
     * if you know the field values will match any constraints. 
     */
    public int dirtySave(){
        try {
            return save();
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.i("net.bradmont.supergreen",sw.toString());
            return 0;
        }
    }

    public int getID(){
        return id;
    }

    public void update(){
        setupDbw();
        dbw.update(TABLE_NAME, buildContentValues(true), String.format("%s=%d", primaryKey.getColumnName(), id), null);
        //Log.i("net.bradmont.supergreen", String.format("update: %s %s=%d", TABLE_NAME, primaryKey.getColumnName(), id));
    }

    public void delete(){
        if (record_exists == true){
            setupDbw();
            dbw.delete(TABLE_NAME, String.format("%s=%d", primaryKey.getColumnName(), id), null);
            record_exists = false;
        }
    }
    private int create_record(){
        setupDbw();
        if (primaryKey.getInt() != 0){
            id =  (int) dbw.insert(TABLE_NAME, null, buildContentValues(true)) ;
        } else {
            id =  (int) dbw.insert(TABLE_NAME, null, buildContentValues(false)) ;
        }
        ((IntField)primaryKey).setValue(id);
        record_exists = true;
        return id;
    }

    /**
        DEPRECATED: move to view handler
    */

    public String getListSQL(){
        String SQL = "SELECT * from " + TABLE_NAME ;
        return SQL;
    }

    void loadRecord(Cursor cur){
        for (Map.Entry<String, DBField> entry: fields.entrySet()){
            DBField field = entry.getValue();
            int colIndex = cur.getColumnIndex(field.getColumnName());
            if (field instanceof IntField) {
                ((IntField) field).setValue(cur.getInt(colIndex));
            } else if (field instanceof BooleanField) {
                ((BooleanField) field).setValue(cur.getInt(colIndex));

            } else if (field instanceof DateField) {
                ((DateField) field).setValue(cur.getString(colIndex));
            } else if (field instanceof StringField) {
                ((StringField) field).setValue(cur.getString(colIndex));

            } else if (field instanceof FloatField) {
                ((FloatField) field).setValue(cur.getFloat(colIndex));
            }
        }
    }


    /**
     * Instantiate a copy of this class.
     */
    public abstract DBModel newInstance();
    public abstract DBModel newInstance(int id);

    /**
     * Populate fields by directly providing values. Requires a cursor resulting from a SELECT * FROM TABLE. The cursor must have been move'd to the relevant record.
     * Added to increase performance when populating multiple DBModels at once, allowing a single query rather than requrying for every DBModel.
     */
    public DBModel loadFromRow(Cursor cur){
        String [] columns = cur.getColumnNames();
        for (int i = 0; i < cur.getColumnCount(); i++){
            DBField field = fields.get(columns[i]);
            if (field == null){
                field = fields.get(
                    fields_by_column_name.get(columns[i]));

            }
            // populate field by data type
            if (field instanceof IntField){
                field.setValue(cur.getInt(i));
                if (field == primaryKey){
                    id = cur.getInt(i);
                }
            } else if (field instanceof FloatField){
                field.setValue(cur.getFloat(i));
            } else if (field instanceof BooleanField){
                field.setValue(cur.getInt(i));
            } else { // field instanceof StringField
                field.setValue(cur.getString(i));
            }
        }
        record_exists = true;
        return this;
    }

    public boolean validate() throws ConstraintError{
        // TODO // Adjust to return all errors at once rather than just the first
        for (Map.Entry<String, DBField> entry: fields.entrySet()){
            DBField field = entry.getValue();
            field.validate();
        }
        return true;
    }

    protected ContentValues buildContentValues(){
        return buildContentValues(false);
    }
    protected ContentValues buildContentValues(boolean includePrimaryKey){
        ContentValues cv = new ContentValues();
        for (Map.Entry<String, DBField> entry: fields.entrySet()){
            DBField field = entry.getValue();
            if (field != primaryKey || includePrimaryKey){ // reference compare
                if (field instanceof IntField) {
                    cv.put(field.getColumnName(), field.getInt());
                } else if (field instanceof BooleanField) {
                    cv.put(field.getColumnName(), field.getBoolean());
                } else if (field instanceof DateField) {
                    cv.put(field.getColumnName(), field.getString());
                } else if (field instanceof StringField) {
                    cv.put(field.getColumnName(), field.getString());
                } else if (field instanceof FloatField) {
                    cv.put(field.getColumnName(), field.getFloat());
                }
            }
        }
        return cv;
    }


    protected SQLiteDatabase getDbw(){
        setupDbw();
        return dbw;
    }
    protected static void setupDbw(){
        if (dbw == null){
            dbw = dbh.getWritableDatabase();
        }
    }
    protected SQLiteDatabase getDbr(){
        setupDbr();
        return dbr;
    }
    private void setupDbr(){
        if (dbr == null){
            dbr = dbh.getReadableDatabase();
        }
    }
    public SQLiteOpenHelper getDbh(){
        return dbh;
    }
    public class RowDoesNotExistException extends RuntimeException{
        String error = "No such record. ";
        public RowDoesNotExistException(){
        }
        public RowDoesNotExistException(int row){
            error=error + String.format(" pk = %d", row);
        }
        public RowDoesNotExistException(String error, int row){
            this.error=error + String.format(" pk = %d", row);
        }
        public String getError(){
            return error;
        }
    }

    public boolean recordExists(){
        return record_exists;
    }

    public static void beginTransaction(){
        setupDbw();
        dbw.beginTransaction();
    }

    public static void endTransaction(){
        dbw.setTransactionSuccessful();
        dbw.endTransaction();
    }
}
