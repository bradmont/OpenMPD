package net.bradmont.supergreen.fields;

import net.bradmont.supergreen.fields.constraints.ConstraintError;
import android.widget.*;
import android.view.View;

public class MoneyField extends IntField {


    public MoneyField(String name){
        super(name);
    }


    @Override
    public void setValue(String value){
        setValue((int) (Float.parseFloat(value) * 100f));
    }

    @Override
    public void setValue(float value){
        setValue ((int) value*100f);
    }

    @Override
    public void putToView(View parentView){
        stringToView(parentView, String.format("%.2f", ((float) getInt())/100.0f ));
    }
    
    @Override
    public String getString(){
        return String.format("%.2f", ((float) getInt())/100.0f );
    }
    public float getFloat(){
        return (float) value;
    }
}
