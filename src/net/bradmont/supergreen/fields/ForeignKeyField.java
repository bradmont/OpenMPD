package net.bradmont.supergreen.fields;

import net.bradmont.supergreen.fields.constraints.ConstraintError;
import net.bradmont.supergreen.models.*;

import android.widget.*;
import android.database.Cursor;
import android.view.View;
import android.util.Log;

public class ForeignKeyField extends IntField {

    DBModel relatedModel = null;
    public ForeignKeyField(String name){
        super(name);
    }

    public ForeignKeyField(String name, DBModel references){
        super(name);
        relatedModel = references;
    }

    /**
     * Push the value to the specified view. Will select the apropriate
     * record in a Spinner fed by a CursordAdapter; assumes the field's
     * present value corresponds to column 0 in the CursorAdapter's result
     * set.
     */
    @Override
    public void putToView(View v){
        putToView(v, 0);
    }

    public void putToView(View v, int cursor_column){
        if (v instanceof Spinner){
            Spinner spinner = (Spinner) v;
            SpinnerAdapter adapter = spinner.getAdapter();
            if (adapter instanceof CursorAdapter){
                Cursor cur = ((CursorAdapter) adapter).getCursor();
                cur.moveToFirst();
                while (!cur.isAfterLast()){
                    if (cur.getInt(cursor_column) == getInt()){
                        spinner.setSelection(cur.getPosition());
                        return;
                    }
                }
            }
        } else {
            stringToView(v, Integer.toString(value));
        }
    }

    @Override
    public String getSQLTableConstraint(){
        String sql = "FOREIGN KEY (%s) REFERENCES %s (%s)";
        sql = String.format(sql, getColumnName(), relatedModel.getTableName(),
            relatedModel.getPrimaryKey().getColumnName() );
        return sql;
    }

    /**
     * Gets the model to which this key refers
    */
    @Override 
    public DBModel getRelated(){
        return relatedModel.newInstance(getInt());
    }

    @Override 
    public void setValue(DBModel model){
        setValue(model.getPrimaryKey().getInt());
    }

}
