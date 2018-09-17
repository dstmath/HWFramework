package android_maps_conflict_avoidance.com.google.common.io;

import android_maps_conflict_avoidance.com.google.common.Config;

public abstract class BaseConnectionFactory implements ConnectionFactory {
    private boolean hasPreviousNetworkSuccessBeenRead = false;
    private final String netAvailablePrefName;
    private boolean networkWorked = false;
    private boolean networkWorkedThisSession = false;
    protected final PersistentStore store;

    protected BaseConnectionFactory(String netAvailablePrefName) {
        this.netAvailablePrefName = netAvailablePrefName;
        this.store = Config.getInstance().getPersistentStore();
    }

    protected void checkPreviousNetworkSuccess() {
        setNetworkWorked(this.store.readPreference(this.netAvailablePrefName) != null);
    }

    public synchronized boolean registerNetworkSuccess(boolean forcePrefWrite) {
        this.networkWorkedThisSession = true;
        if (getNetworkWorked() && !forcePrefWrite) {
            return false;
        }
        this.networkWorked = true;
        this.store.setPreference(this.netAvailablePrefName, new byte[]{(byte) getNetworkPreferenceValue()});
        return true;
    }

    protected byte getNetworkPreferenceValue() {
        return (byte) 0;
    }

    public void notifyFailure() {
    }

    public boolean getNetworkWorked() {
        if (!this.hasPreviousNetworkSuccessBeenRead) {
            checkPreviousNetworkSuccess();
        }
        return this.networkWorked;
    }

    private void setNetworkWorked(boolean worked) {
        this.hasPreviousNetworkSuccessBeenRead = true;
        this.networkWorked = worked;
    }

    public boolean getNetworkWorkedThisSession() {
        return this.networkWorkedThisSession;
    }

    public boolean usingMDS() {
        return false;
    }
}
