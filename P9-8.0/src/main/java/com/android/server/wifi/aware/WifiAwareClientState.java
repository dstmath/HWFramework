package com.android.server.wifi.aware;

import android.app.AppOpsManager;
import android.content.Context;
import android.net.wifi.RttManager.ParcelableRttResults;
import android.net.wifi.aware.ConfigRequest;
import android.net.wifi.aware.IWifiAwareEventCallback;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class WifiAwareClientState {
    private static final byte[] ALL_ZERO_MAC = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    static final int CLUSTER_CHANGE_EVENT_JOINED = 1;
    static final int CLUSTER_CHANGE_EVENT_STARTED = 0;
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareClientState";
    private static final boolean VDBG = false;
    private AppOpsManager mAppOps;
    private final IWifiAwareEventCallback mCallback;
    private final String mCallingPackage;
    private final int mClientId;
    private ConfigRequest mConfigRequest;
    private final Context mContext;
    private byte[] mLastDiscoveryInterfaceMac = ALL_ZERO_MAC;
    private final boolean mNotifyIdentityChange;
    private final int mPid;
    private final SparseArray<WifiAwareDiscoverySessionState> mSessions = new SparseArray();
    private final int mUid;

    public WifiAwareClientState(Context context, int clientId, int uid, int pid, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyIdentityChange) {
        this.mContext = context;
        this.mClientId = clientId;
        this.mUid = uid;
        this.mPid = pid;
        this.mCallingPackage = callingPackage;
        this.mCallback = callback;
        this.mConfigRequest = configRequest;
        this.mNotifyIdentityChange = notifyIdentityChange;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
    }

    public void destroy() {
        for (int i = 0; i < this.mSessions.size(); i++) {
            ((WifiAwareDiscoverySessionState) this.mSessions.valueAt(i)).terminate();
        }
        this.mSessions.clear();
        this.mConfigRequest = null;
    }

    public ConfigRequest getConfigRequest() {
        return this.mConfigRequest;
    }

    public int getClientId() {
        return this.mClientId;
    }

    public int getUid() {
        return this.mUid;
    }

    public boolean getNotifyIdentityChange() {
        return this.mNotifyIdentityChange;
    }

    public WifiAwareDiscoverySessionState getAwareSessionStateForPubSubId(int pubSubId) {
        for (int i = 0; i < this.mSessions.size(); i++) {
            WifiAwareDiscoverySessionState session = (WifiAwareDiscoverySessionState) this.mSessions.valueAt(i);
            if (session.isPubSubIdSession(pubSubId)) {
                return session;
            }
        }
        return null;
    }

    public void addSession(WifiAwareDiscoverySessionState session) {
        int sessionId = session.getSessionId();
        if (this.mSessions.get(sessionId) != null) {
            Log.w(TAG, "createSession: sessionId already exists (replaced) - " + sessionId);
        }
        this.mSessions.put(sessionId, session);
    }

    public void removeSession(int sessionId) {
        if (this.mSessions.get(sessionId) == null) {
            Log.e(TAG, "removeSession: sessionId doesn't exist - " + sessionId);
        } else {
            this.mSessions.delete(sessionId);
        }
    }

    public void terminateSession(int sessionId) {
        WifiAwareDiscoverySessionState session = (WifiAwareDiscoverySessionState) this.mSessions.get(sessionId);
        if (session == null) {
            Log.e(TAG, "terminateSession: sessionId doesn't exist - " + sessionId);
            return;
        }
        session.terminate();
        this.mSessions.delete(sessionId);
    }

    public WifiAwareDiscoverySessionState getSession(int sessionId) {
        return (WifiAwareDiscoverySessionState) this.mSessions.get(sessionId);
    }

    public void onInterfaceAddressChange(byte[] mac) {
        if (this.mNotifyIdentityChange && (Arrays.equals(mac, this.mLastDiscoveryInterfaceMac) ^ 1) != 0) {
            try {
                this.mCallback.onIdentityChanged(hasLocationingPermission() ? mac : ALL_ZERO_MAC);
            } catch (RemoteException e) {
                Log.w(TAG, "onIdentityChanged: RemoteException - ignored: " + e);
            }
        }
        this.mLastDiscoveryInterfaceMac = mac;
    }

    public void onClusterChange(int flag, byte[] mac, byte[] currentDiscoveryInterfaceMac) {
        if (this.mNotifyIdentityChange && (Arrays.equals(currentDiscoveryInterfaceMac, this.mLastDiscoveryInterfaceMac) ^ 1) != 0) {
            try {
                boolean hasPermission = hasLocationingPermission();
                IWifiAwareEventCallback iWifiAwareEventCallback = this.mCallback;
                if (!hasPermission) {
                    mac = ALL_ZERO_MAC;
                }
                iWifiAwareEventCallback.onIdentityChanged(mac);
            } catch (RemoteException e) {
                Log.w(TAG, "onIdentityChanged: RemoteException - ignored: " + e);
            }
        }
        this.mLastDiscoveryInterfaceMac = currentDiscoveryInterfaceMac;
    }

    private boolean hasLocationingPermission() {
        if (this.mContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", this.mPid, this.mUid) == 0 && this.mAppOps.noteOp(0, this.mUid, this.mCallingPackage) == 0) {
            return true;
        }
        return false;
    }

    public void onRangingSuccess(int rangingId, ParcelableRttResults results) {
        try {
            this.mCallback.onRangingSuccess(rangingId, results);
        } catch (RemoteException e) {
            Log.w(TAG, "onRangingSuccess: RemoteException - ignored: " + e);
        }
    }

    public void onRangingFailure(int rangingId, int reason, String description) {
        try {
            this.mCallback.onRangingFailure(rangingId, reason, description);
        } catch (RemoteException e) {
            Log.w(TAG, "onRangingFailure: RemoteException - ignored: " + e);
        }
    }

    public void onRangingAborted(int rangingId) {
        try {
            this.mCallback.onRangingAborted(rangingId);
        } catch (RemoteException e) {
            Log.w(TAG, "onRangingAborted: RemoteException - ignored: " + e);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("AwareClientState:");
        pw.println("  mClientId: " + this.mClientId);
        pw.println("  mConfigRequest: " + this.mConfigRequest);
        pw.println("  mNotifyIdentityChange: " + this.mNotifyIdentityChange);
        pw.println("  mCallback: " + this.mCallback);
        pw.println("  mSessions: [" + this.mSessions + "]");
        for (int i = 0; i < this.mSessions.size(); i++) {
            ((WifiAwareDiscoverySessionState) this.mSessions.valueAt(i)).dump(fd, pw, args);
        }
    }
}
