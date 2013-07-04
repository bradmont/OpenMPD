package net.bradmont.supergreen.fields.constraints;
import net.bradmont.supergreen.fields.*;

/**
 * Minimum value constraints for IntField and FloatField. 
 */
public class MinValueConstraint extends Constraint{

    int int_min;
    float float_min;
    public MinValueConstraint(int min){
        this.int_min=min;
        this.float_min=(float)min;
    }
    public MinValueConstraint(float min){
        this.float_min=min;
    }


    @Override
    public boolean validate(DBField field) throws ConstraintError{
        if (field instanceof IntField){
            if (field.getInt() < int_min){
                throw new ConstraintError ("%s cannot be less than %d".format(field.getName(), int_min));
            } else {
                return true;
            }
        } else if (field instanceof FloatField){
            if (field.getFloat() < float_min){
                throw new ConstraintError( "%s cannot be less than %f".format(field.getName(), float_min));
            } else {
                return true;
            }
        } else {
            throw new ConstraintError( String.format("This constraint is only valid for FloatField and IntField; invalid for %s", field.getName()));
        }

    }
}
