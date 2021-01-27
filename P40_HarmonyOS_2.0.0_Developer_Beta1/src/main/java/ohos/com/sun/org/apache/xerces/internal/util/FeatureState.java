package ohos.com.sun.org.apache.xerces.internal.util;

public class FeatureState {
    public static final FeatureState NOT_ALLOWED = new FeatureState(Status.NOT_ALLOWED, false);
    public static final FeatureState NOT_RECOGNIZED = new FeatureState(Status.NOT_RECOGNIZED, false);
    public static final FeatureState NOT_SUPPORTED = new FeatureState(Status.NOT_SUPPORTED, false);
    public static final FeatureState RECOGNIZED = new FeatureState(Status.RECOGNIZED, false);
    public static final FeatureState SET_DISABLED = new FeatureState(Status.SET, false);
    public static final FeatureState SET_ENABLED = new FeatureState(Status.SET, true);
    public static final FeatureState UNKNOWN = new FeatureState(Status.UNKNOWN, false);
    public final boolean state;
    public final Status status;

    public FeatureState(Status status2, boolean z) {
        this.status = status2;
        this.state = z;
    }

    public static FeatureState of(Status status2) {
        return new FeatureState(status2, false);
    }

    public static FeatureState is(boolean z) {
        return new FeatureState(Status.SET, z);
    }

    public boolean isExceptional() {
        return this.status.isExceptional();
    }
}
