package net.bradmont.supergreen.fields;

import net.bradmont.supergreen.models.DBModel;
import net.bradmont.supergreen.fields.constraints.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Calendar;

import android.database.Cursor;
import android.widget.*;
import android.util.Log;
import android.view.View;

public abstract class DBField {

    private String name; // field name
    private String column_name = null;
    protected String extra_arguments = null;
    boolean has_default = false;
    DBModel ownerModel = null;
    Constraint [] constraints = null;


    /**
     * Creates a new, blank DBField instance.
     */
    public DBField(String name){
        this.name=name;
    }

    /**
     * Add Constraints to a field for data validation.
     */
    public void addConstraints(Constraint ... constraints){
        this.constraints = new Constraint[constraints.length];
        for (int i = 0; i < constraints.length; i++){
            this.constraints[i] = constraints[i];
        }
    }

    public boolean validate() throws ConstraintError{
        if (constraints == null){
            return true;
        }
        for (int i = 0; i < constraints.length; i++){
            constraints[i].validate(this);
        }
        return true;
    }

    public abstract void putToView(View v);
    public abstract void getFromView(View v) throws ConstraintError;

    public void setColumnName(String column_name){
        this.column_name=column_name;
        ownerModel.setFieldColumnName(getName(), column_name);
    }

    /**
     * Add extra arguments to the field's table definition SQL, such as
     * AUTOINCREMENT, NOT NULL, etc.
    */
    public void setExtraArguments(String extra_arguments){
        this.extra_arguments = extra_arguments;
    }
    public String getExtraArguments(){
        return extra_arguments;
    }

    public String getColumnName(){
        if (column_name == null){
            return name;
        } else {
            return column_name;
        }
    }

    public String getName(){
        return name;
    }

    protected String viewToString(View v){
        String value = "";
        if (v instanceof EditText){
            value = ((EditText) v).getText().toString();
        } else if (v instanceof Switch){
            value = ((Switch) v).isChecked()? "1" : "0";
        } else if (v instanceof CheckBox){
            value = ((CheckBox) v).isChecked()? "1" : "0";
        } else if (v instanceof ToggleButton){
            value = ((ToggleButton) v).isChecked()? "1" : "0";
        } else if (v instanceof Spinner){
            // we can deal with ArrayAdapter spinners easily enough,
            // but further detail is required for CursorAdapters or others
            SpinnerAdapter sa = ((Spinner) v).getAdapter();
            if (sa instanceof ArrayAdapter){
                value = ((Spinner) v).getSelectedItem().toString();
            } else {
                value = getSpinnerValue((Spinner) v);
            }
        } else if (v instanceof TextView){
            value = ((TextView) v).getText().toString();
        }
        //Log.i("net.bradmont.supergreen", "getFromView value " + value);
        return value;
    }

    protected void stringToView(View v, String value){
        if (v == null){
            // don't do anything if the voew doesn't have this field
            return;
        } else if (v instanceof EditText){
            ((EditText) v).setText(value);
        } else if (v instanceof Switch){
            ((Switch) v).setChecked(value.equals("1"));
        } else if (v instanceof CheckBox){
            ((CheckBox) v).setChecked(value.equals("1"));
        } else if (v instanceof ToggleButton){
            ((ToggleButton) v).setChecked(value.equals("1"));
        } else if (v instanceof Spinner){
            setSpinnerPosition((Spinner) v);

            SpinnerAdapter sa = ((Spinner) v).getAdapter();
            if (((Spinner)v).getSelectedItemPosition() == 0 && sa instanceof ArrayAdapter){
                // if it's an ArrayAdapter, and it looks like setSpinnerPosition
                // might not be implemented, we can try to implement some basic
                // functionality here. If setSpinnerPosition worked and selected
                // item 0, no harm done.
                int spinnerPosition = ((ArrayAdapter) sa).getPosition(value);
                ((Spinner) v).setSelection(spinnerPosition);
            } 
        } else if (v instanceof TextView){
            ((TextView) v).setText(value);
        }
    }

    protected String getSpinnerValue(Spinner s){
        // override for intelligent selection
        Object val = s.getSelectedItem();
        if (val instanceof Cursor){
            return ((Cursor)val).getString(0);
        }
        return s.getSelectedItem().toString();
    }

    /**
     * When populating a spinner from an existing DBField, this function
     * should select the appropirate Spinner item; if you're using a 
     * SpinnerAdapterother than a simpe ArrayAdapter of string values, you
     * must build your own subclass of the apropriate DBField type and 
     * override this method.
     * @param s The spinner to populate
    */
    protected void setSpinnerPosition(Spinner s){
        // override for intelligent selection
        s.setSelection(0);
    }

    public void setModel(DBModel model){
        ownerModel = model;
    }

    /**
     * Get a reference to the DBModel of which this field is a member
     */
    public DBModel getModel(){
        return ownerModel;
    }
    /**
     * Get a reference to another field of the DBModel of which this field is a member.
     */
    public DBField getField(String name){
        return ownerModel.getField(name);
    }

    public int getInt(){
        throw (new NotImplementedException("getInt"));
    }
    public String getString(){
        throw (new NotImplementedException("getString"));
    }
    public float getFloat(){
        throw (new NotImplementedException("getFloat"));
    }
    public boolean getBoolean(){
        throw (new NotImplementedException("getBoolean"));
    }
    public Calendar getCalendar(){
        throw (new NotImplementedException("getInt"));
    }

    public DBModel getRelated(){
        throw (new NotImplementedException("getRelated"));
    }

    /**
     * Generate SQL definition for the field's data, for CREATE
     * and UPDATE queries. Must be overriden for each subclass.
     */
    public String getSQLDefinition(){
        throw (new NotImplementedException("getSQLDefinition"));
        // TODO: process constraints
    }

    /**
     * Generate SQL definition for the field's related table
     * constraints, eg, to create foreign keys. Override when
     * a field uses it.
     */
    public String getSQLTableConstraint(){
        return "";
    }

    /**
     * Set a field's default value to an int, for supported fields.
     */
    public void setDefault(int value){
        setValue(value);
    }
    /**
     * Set a field's default value to a String, for supported fields.
     */
    public void setDefault(String value){
        setValue(value);
    }
    /**
     * Set a field's default value to a float, for supported fields.
     */
    public void setDefault(float value){
        setValue(value);
    }
    /**
     * Set a field's default value to a boolean, for supported fields.
     */
    public void setDefault(boolean value){
        setValue(value);
    }
    /**
     * Set a field's default value to a Date, for supported fields.
     */
    public void setDefault(Calendar value){
        setValue(value);
    }

    /**
     * Set the value of a field to an int, for supported fields
     */
    public void setValue(int value){
        throw (new NotImplementedException("setValue(int)"));
    }
    /**
     * Set the value of a field to a String, for supported fields
     */
    public void setValue(String value){
        throw (new NotImplementedException("setValue(String)"));
    }
    /**
     * Set the value of a field to a Float, for supported fields
     */
    public void setValue(float value){
        throw (new NotImplementedException("setValue(Float)"));
    }
    /**
     * Set the value of a field to a boolean, for supported fields
     */
    public void setValue(boolean value){
        throw (new NotImplementedException("setValue(boolean)"));
    }

    /**
     * Set the value of a field to a related DBModel (foreign keys only)
     */
    public void setValue(Calendar value){
        throw (new NotImplementedException("setValue(Calendar)"));
    }

    public void setValue(DBModel value){
        throw (new NotImplementedException("setValue(DBModel)"));
    }

    class NotImplementedException extends RuntimeException{
        NotImplementedException(String message){
            super(message);
        }
    }

}
