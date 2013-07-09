package net.bradmont.supergreen.fields;

import net.bradmont.supergreen.fields.constraints.ConstraintError;

import android.widget.*;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

// TODO: this needs cleanup
// look at http://stackoverflow.com/questions/7363112/best-way-to-work-with-dates-in-android-sqlite for some guidelines
public class DateField extends StringField {

    public DateField(String name){
        super(name);
    }

    public void setValue(Calendar value){
        throw (new NotImplementedException("getDate"));
    }

    public void setDefault(Calendar value){
        setValue(value);
        default_value=getString();
        has_default=true;
    }

    public Calendar getCalendar(){
        Calendar result = new GregorianCalendar();
        String [] parts = getString().split("-");
        result.set(
            Integer.parseInt(parts[0]), 
            Integer.parseInt(parts[1])-1, 
            Integer.parseInt(parts[2]), 0, 0);
        return result;
    }

    /** Return date formatted according to format string
     *
     */
    public String format(String dateFormat){
        DateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(getCalendar());
    }

}
