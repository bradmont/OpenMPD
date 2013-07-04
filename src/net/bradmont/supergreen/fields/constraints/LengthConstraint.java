package net.bradmont.supergreen.fields.constraints;
import net.bradmont.supergreen.fields.*;

/**
 * Length constraints for StringField. Set max to -1 for no maximum length.
 */
public class LengthConstraint extends Constraint{

    int min;
    int max;
    public LengthConstraint(int min){
        this(min, -1);
    }

    public LengthConstraint(int min, int max){
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean validate(DBField field) throws ConstraintError{
        if (field.getString() == null && min > 0){
            throw new ConstraintError( String.format("%s cannot be empty", field.getName()));
        } else if (max != -1 && field.getString().length() > max){
            throw new ConstraintError( String.format("%s cannot be longer than %d", field.getName(), max));
        } else if (field.getString().length() < min){
            throw new ConstraintError( String.format("%s cannot be shorter than %d", field.getName(), min));
        } else {
            return true;
        }

    }
}
