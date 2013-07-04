package net.bradmont.supergreen.fields.constraints;
import net.bradmont.supergreen.fields.*;

/**
 * Maximim value constraints for IntField and FloatField. 
 */
public class MaxValueConstraint extends Constraint{

    int int_max;
    float float_max;
    public MaxValueConstraint(int max){
        this.int_max=max;
        this.float_max=(float)max;
    }
    public MaxValueConstraint(float max){
        this.float_max=max;
    }


    @Override
    public boolean validate(DBField field) throws ConstraintError{
        if (field instanceof IntField){
            if (field.getInt() > int_max){
                throw new ConstraintError("%s cannot be longer than %d".format(field.getName(), int_max));
            } else {
                return true;
            }
        } else {
            if (field.getFloat() > float_max){
                throw new ConstraintError( "%s cannot be longer than %f".format(field.getName(), float_max));
            } else {
                return true;
            }
        }

    }
}
