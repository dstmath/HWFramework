package ohos.com.sun.org.apache.xerces.internal.util;

public class PropertyState {
    public static final PropertyState NOT_ALLOWED = new PropertyState(Status.NOT_ALLOWED, null);
    public static final PropertyState NOT_RECOGNIZED = new PropertyState(Status.NOT_RECOGNIZED, null);
    public static final PropertyState NOT_SUPPORTED = new PropertyState(Status.NOT_SUPPORTED, null);
    public static final PropertyState RECOGNIZED = new PropertyState(Status.RECOGNIZED, null);
    public static final PropertyState UNKNOWN = new PropertyState(Status.UNKNOWN, null);
    public final Object state;
    public final Status status;

    public PropertyState(Status status2, Object obj) {
        this.status = status2;
        this.state = obj;
    }

    public static PropertyState of(Status status2) {
        return new PropertyState(status2, null);
    }

    public static PropertyState is(Object obj) {
        return new PropertyState(Status.SET, obj);
    }

    public boolean isExceptional() {
        return this.status.isExceptional();
    }
}
