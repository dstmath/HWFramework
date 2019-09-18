package java.beans;

import java.util.EventObject;

public class PropertyChangeEvent extends EventObject {
    private static final long serialVersionUID = 7042693688939648123L;
    private Object newValue;
    private Object oldValue;
    private Object propagationId;
    private String propertyName;

    public PropertyChangeEvent(Object source, String propertyName2, Object oldValue2, Object newValue2) {
        super(source);
        this.propertyName = propertyName2;
        this.newValue = newValue2;
        this.oldValue = oldValue2;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public Object getNewValue() {
        return this.newValue;
    }

    public Object getOldValue() {
        return this.oldValue;
    }

    public void setPropagationId(Object propagationId2) {
        this.propagationId = propagationId2;
    }

    public Object getPropagationId() {
        return this.propagationId;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[propertyName=");
        sb.append(getPropertyName());
        appendTo(sb);
        sb.append("; oldValue=");
        sb.append(getOldValue());
        sb.append("; newValue=");
        sb.append(getNewValue());
        sb.append("; propagationId=");
        sb.append(getPropagationId());
        sb.append("; source=");
        sb.append(getSource());
        sb.append("]");
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void appendTo(StringBuilder sb) {
    }
}
