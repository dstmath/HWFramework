package android.filterfw.core;

import java.lang.reflect.Field;

public class FinalPort extends FieldPort {
    public FinalPort(Filter filter, String name, Field field, boolean hasDefault) {
        super(filter, name, field, hasDefault);
    }

    protected synchronized void setFieldFrame(Frame frame, boolean isAssignment) {
        assertPortIsOpen();
        checkFrameType(frame, isAssignment);
        if (this.mFilter.getStatus() != 0) {
            throw new RuntimeException("Attempting to modify " + this + "!");
        }
        super.setFieldFrame(frame, isAssignment);
        super.transfer(null);
    }

    public String toString() {
        return "final " + super.toString();
    }
}
