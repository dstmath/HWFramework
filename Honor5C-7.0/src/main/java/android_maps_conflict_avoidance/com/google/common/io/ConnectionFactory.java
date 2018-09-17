package android_maps_conflict_avoidance.com.google.common.io;

public interface ConnectionFactory {
    boolean getNetworkWorked();

    boolean getNetworkWorkedThisSession();

    void notifyFailure();

    boolean registerNetworkSuccess(boolean z);

    boolean usingMDS();
}
