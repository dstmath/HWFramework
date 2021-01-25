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

    @Override // android.filterfw.core.FilterPort
    public void clear() {
    }

    @Override // android.filterfw.core.FilterPort
    public void pushFrame(Frame frame) {
        setFieldFrame(frame, false);
    }

    @Override // android.filterfw.core.FilterPort
    public void setFrame(Frame frame) {
        setFieldFrame(frame, true);
    }

    @Override // android.filterfw.core.InputPort
    public Object getTarget() {
        try {
            return this.mField.get(this.mFilter);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Override // android.filterfw.core.InputPort
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

    @Override // android.filterfw.core.FilterPort
    public synchronized Frame pullFrame() {
        throw new RuntimeException("Cannot pull frame on " + this + "!");
    }

    @Override // android.filterfw.core.FilterPort
    public synchronized boolean hasFrame() {
        return this.mHasFrame;
    }

    @Override // android.filterfw.core.InputPort
    public synchronized boolean acceptsFrame() {
        return !this.mValueWaiting;
    }

    @Override // android.filterfw.core.FilterPort
    public String toString() {
        return "field " + super.toString();
    }

    /* access modifiers changed from: protected */
    public synchronized void setFieldFrame(Frame frame, boolean isAssignment) {
        assertPortIsOpen();
        checkFrameType(frame, isAssignment);
        Object value = frame.getObjectValue();
        if ((value == null && this.mValue != null) || !value.equals(this.mValue)) {
            this.mValue = value;
            this.mValueWaiting = true;
        }
        this.mHasFrame = true;
    }
}
