package net.bradmont.supergreen.fields;

import net.bradmont.supergreen.fields.constraints.ConstraintError;

import android.widget.*;
import android.view.View;

import java.util.Date;

// TODO: this needs cleanup
// look at http://stackoverflow.com/questions/7363112/best-way-to-work-with-dates-in-android-sqlite for some guidelines
public class DateField extends StringField {

    public DateField(String name){
        super(name);
    }

    public void setValue(Date value){
        throw (new NotImplementedException("getDate"));
    }

    public void setDefault(Date value){
        setValue(value);
        default_value=getString();
        has_default=true;
    }

    public Date getDate(){
        throw (new NotImplementedException("getDate"));
    }

}
