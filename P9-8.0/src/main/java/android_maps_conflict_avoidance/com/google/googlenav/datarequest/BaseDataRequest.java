package android_maps_conflict_avoidance.com.google.googlenav.datarequest;

public abstract class BaseDataRequest implements DataRequest {
    private volatile boolean isCancelled = false;
    private int serverFailureCount = 0;

    public boolean isImmediate() {
        return true;
    }

    public boolean isForeground() {
        return true;
    }

    public boolean isSubmission() {
        return false;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public boolean retryOnFailure() {
        return this.serverFailureCount < 3;
    }

    public void onServerFailure() {
        this.serverFailureCount++;
    }
}
