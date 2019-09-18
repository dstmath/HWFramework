package android.filterfw.core;

import java.lang.reflect.Field;

public class FinalPort extends FieldPort {
    public FinalPort(Filter filter, String name, Field field, boolean hasDefault) {
        super(filter, name, field, hasDefault);
    }

    /* access modifiers changed from: protected */
    public synchronized void setFieldFrame(Frame frame, boolean isAssignment) {
        assertPortIsOpen();
        checkFrameType(frame, isAssignment);
        if (this.mFilter.getStatus() == 0) {
            super.setFieldFrame(frame, isAssignment);
            super.transfer(null);
        } else {
            throw new RuntimeException("Attempting to modify " + this + "!");
        }
    }

    public String toString() {
        return "final " + super.toString();
    }
}
