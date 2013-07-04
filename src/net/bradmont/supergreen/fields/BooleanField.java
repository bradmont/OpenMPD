package net.bradmont.supergreen.fields;
import net.bradmont.supergreen.fields.constraints.ConstraintError;


import android.widget.*;
import android.view.View;

public class BooleanField extends DBField {

    protected int value;
    protected int default_value;


    public BooleanField(String name){
        super(name);
    }

    public void putToView(View v){
        stringToView(v, Integer.toString(value));
    }
    public void getFromView(View v) throws ConstraintError{
        value = Integer.parseInt(viewToString(v));
    }

    public int getInt(){
        return value;
    }
    public boolean getBoolean(){
        return (value != 0);
    }

    @Override
    public void setValue(int value){
        this.value= value==0 ? 0: 1;
    }
    public void setValue(boolean value){
        this.value= value?1:0;
    }
    @Override
    public void setDefault(boolean value){
        default_value = value?0:1;
        has_default=true;
        setValue(value);
    }

    @Override
    public String getSQLDefinition(){
        String sql = getColumnName() + " int";
        if (has_default){
            sql += String.format(" default (%d)", default_value);
        }
        if (extra_arguments != null){
            sql += " " + extra_arguments;
        }               

        return sql;
    }   


}
