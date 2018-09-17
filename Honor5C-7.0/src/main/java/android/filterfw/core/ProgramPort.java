package android.filterfw.core;

import java.lang.reflect.Field;

public class ProgramPort extends FieldPort {
    protected String mVarName;

    public ProgramPort(Filter filter, String name, String varName, Field field, boolean hasDefault) {
        super(filter, name, field, hasDefault);
        this.mVarName = varName;
    }

    public String toString() {
        return "Program " + super.toString();
    }

    public synchronized void transfer(FilterContext context) {
        if (this.mValueWaiting) {
            try {
                Object fieldValue = this.mField.get(this.mFilter);
                if (fieldValue != null) {
                    ((Program) fieldValue).setHostValue(this.mVarName, this.mValue);
                    this.mValueWaiting = false;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Access to program field '" + this.mField.getName() + "' was denied!");
            } catch (ClassCastException e2) {
                throw new RuntimeException("Non Program field '" + this.mField.getName() + "' annotated with ProgramParameter!");
            }
        }
    }
}
