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
