package net.bradmont.supergreen.fields.constraints;
import net.bradmont.supergreen.fields.*;

/**
 * Ensure a StringField is not left blank.
 */
public class NotEmptyConstraint extends Constraint{

    public NotEmptyConstraint(){
    }

    @Override
    public boolean validate(DBField field) throws ConstraintError{
        if (field.getString() == null || field.getString().length() == 0){
            throw new ConstraintError( String.format("%s cannot be empty", field.getName()));
        } else {
            return true;
        }

    }
}
