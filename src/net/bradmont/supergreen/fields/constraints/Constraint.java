package net.bradmont.supergreen.fields.constraints;
import net.bradmont.supergreen.fields.*;

/**
 * Constraints should be static to a DBModel to improve performance and save 
 * memory.
 */
public abstract class Constraint{

    /**
     * Verifies whether a given DBField's value matches this constraint.
     * Returns an empty string if the DBField conforms to the constraint,
     * otherwise returns an error message.
     */
    public abstract boolean validate(DBField field) throws ConstraintError;
}
