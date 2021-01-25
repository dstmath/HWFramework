package android.net;

import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;

public class NetworkMonitorManager {
    private final INetworkMonitor mNetworkMonitor;
    private final String mTag;

    public NetworkMonitorManager(INetworkMonitor networkMonitorManager, String tag) {
        this.mNetworkMonitor = networkMonitorManager;
        this.mTag = tag;
    }

    public NetworkMonitorManager(INetworkMonitor networkMonitorManager) {
        this(networkMonitorManager, NetworkMonitorManager.class.getSimpleName());
    }

    private void log(String s, Throwable e) {
        Log.e(this.mTag, s, e);
    }

    public boolean start() {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.start();
            return true;
        } catch (RemoteException e) {
            log("Error in start", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean launchCaptivePortalApp() {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.launchCaptivePortalApp();
            return true;
        } catch (RemoteException e) {
            log("Error in launchCaptivePortalApp", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean notifyCaptivePortalAppFinished(int response) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.notifyCaptivePortalAppFinished(response);
            return true;
        } catch (RemoteException e) {
            log("Error in notifyCaptivePortalAppFinished", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean setAcceptPartialConnectivity() {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.setAcceptPartialConnectivity();
            return true;
        } catch (RemoteException e) {
            log("Error in setAcceptPartialConnectivity", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean forceReevaluation(int uid) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.forceReevaluation(uid);
            return true;
        } catch (RemoteException e) {
            log("Error in forceReevaluation", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean notifyPrivateDnsChanged(PrivateDnsConfigParcel config) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.notifyPrivateDnsChanged(config);
            return true;
        } catch (RemoteException e) {
            log("Error in notifyPrivateDnsChanged", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean notifyDnsResponse(int returnCode) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.notifyDnsResponse(returnCode);
            return true;
        } catch (RemoteException e) {
            log("Error in notifyDnsResponse", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean notifyNetworkConnected(LinkProperties lp, NetworkCapabilities nc) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.notifyNetworkConnected(lp, nc);
            return true;
        } catch (RemoteException e) {
            log("Error in notifyNetworkConnected", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean notifyNetworkDisconnected() {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.notifyNetworkDisconnected();
            return true;
        } catch (RemoteException e) {
            log("Error in notifyNetworkDisconnected", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean notifyLinkPropertiesChanged(LinkProperties lp) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.notifyLinkPropertiesChanged(lp);
            return true;
        } catch (RemoteException e) {
            log("Error in notifyLinkPropertiesChanged", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean notifyNetworkCapabilitiesChanged(NetworkCapabilities nc) {
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkMonitor.notifyNetworkCapabilitiesChanged(nc);
            return true;
        } catch (RemoteException e) {
            log("Error in notifyNetworkCapabilitiesChanged", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
