package android.filterfw.core;

import java.lang.reflect.Field;

public class FieldPort extends InputPort {
    protected Field mField;
    protected boolean mHasFrame;
    protected Object mValue;
    protected boolean mValueWaiting = false;

    public FieldPort(Filter filter, String name, Field field, boolean hasDefault) {
        super(filter, name);
        this.mField = field;
        this.mHasFrame = hasDefault;
    }

    public void clear() {
    }

    public void pushFrame(Frame frame) {
        setFieldFrame(frame, false);
    }

    public void setFrame(Frame frame) {
        setFieldFrame(frame, true);
    }

    public Object getTarget() {
        try {
            return this.mField.get(this.mFilter);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public synchronized void transfer(FilterContext context) {
        if (this.mValueWaiting) {
            try {
                this.mField.set(this.mFilter, this.mValue);
                this.mValueWaiting = false;
                if (context != null) {
                    this.mFilter.notifyFieldPortValueUpdated(this.mName, context);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Access to field '" + this.mField.getName() + "' was denied!");
            }
        }
    }

    public synchronized Frame pullFrame() {
        throw new RuntimeException("Cannot pull frame on " + this + "!");
    }

    public synchronized boolean hasFrame() {
        return this.mHasFrame;
    }

    public synchronized boolean acceptsFrame() {
        return this.mValueWaiting ^ 1;
    }

    public String toString() {
        return "field " + super.toString();
    }

    /* JADX WARNING: Missing block: B:13:0x0023, code:
            if ((r0.equals(r2.mValue) ^ 1) != 0) goto L_0x0011;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void setFieldFrame(Frame frame, boolean isAssignment) {
        assertPortIsOpen();
        checkFrameType(frame, isAssignment);
        Object value = frame.getObjectValue();
        if (value != null || this.mValue == null) {
        }
        this.mValue = value;
        this.mValueWaiting = true;
        this.mHasFrame = true;
    }
}
