package net.bradmont.supergreen.fields;
import net.bradmont.supergreen.fields.constraints.ConstraintError;

import android.widget.*;
import android.view.View;

public class StringField extends DBField {

    protected String value;
    protected String default_value;

    public StringField(String name){
        super(name);
    }


    public void putToView(View v){
        stringToView(v, value);
    }
    public void getFromView(View v) throws ConstraintError{
        value = viewToString(v);
    }

    public String getString(){
        return value;
    }
    @Override
    public void setDefault(String value){
        default_value = value;
        has_default=true;
        setValue(value);
    }

    @Override
    public String getSQLDefinition(){
        String sql = getColumnName() + " text";
        if (has_default){
            sql += String.format(" default '%s'", default_value);
        }
        if (extra_arguments != null){
            sql += " " + extra_arguments;
        }               

        return sql;
    }


    @Override
    public void setValue(String value){
        this.value=value;
    }
}
