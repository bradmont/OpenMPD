package net.bradmont.supergreen.fields;

import net.bradmont.supergreen.fields.constraints.ConstraintError;
import android.widget.*;
import android.view.View;

public class FloatField extends DBField {

    protected float value;
    protected float default_value;

    public FloatField(String name){
        super(name);
    }


    public void putToView(View v){
        stringToView(v, Float.toString(value));
    }
    public void getFromView(View v) throws ConstraintError{
        try {
            value = Float.parseFloat(viewToString(v));
        } catch (Exception e){
            throw new ConstraintError("Invalid value for " + getName());
        }

    }

    @Override
    public float getFloat(){
        return value;
    }

    @Override
    public void setValue(float value){
        this.value=value;
    }

    @Override
    public void setDefault(float value){
        default_value = value;
        has_default=true;
        setValue(value);
    }
    @Override
    public String getSQLDefinition(){
        String sql = getColumnName() + " real";
        if (has_default){
            sql += String.format(" default %f", default_value);
        }
        if (extra_arguments != null){
            sql += " " + extra_arguments;
        }               

        return sql;
    }

}
