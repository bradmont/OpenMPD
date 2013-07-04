package net.bradmont.supergreen.fields;
import net.bradmont.supergreen.fields.constraints.ConstraintError;

import android.widget.*;
import android.view.View;

public class IntField extends DBField {

    protected int value;
    protected int default_value;

    public IntField(String name){
        super(name);
    }


    public void putToView(View v){
        stringToView(v, Integer.toString(value));
    }
    public void getFromView(View v) throws ConstraintError{
        try {
            value = Integer.parseInt(viewToString(v));
        } catch (Exception e){
            throw new ConstraintError("Invalid value for " + getName());
        }
    }

    @Override
    public void setDefault(int value){
        default_value = value;
        has_default=true;
        setValue(value);
    }
    @Override
    public String getSQLDefinition(){
        String sql = getColumnName() + " INTEGER";
        if (has_default){
            sql += String.format(" default (%d)", default_value);
        }
        if (extra_arguments != null){
            sql += " " + extra_arguments;
        }
        return sql;
    }


    public int getInt(){
        return value;
    }

    @Override
    public void setValue(String value){
        setValue(Integer.parseInt(value));
    }

    @Override
    public void setValue(int value){
        this.value=value;
    }
}
