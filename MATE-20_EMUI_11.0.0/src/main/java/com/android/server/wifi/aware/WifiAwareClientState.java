package com.android.server.wifi.aware;

import android.app.AppOpsManager;
import android.content.Context;
import android.net.wifi.aware.ConfigRequest;
import android.net.wifi.aware.IWifiAwareEventCallback;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.wifi.util.WifiPermissionsUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class WifiAwareClientState {
    private static final byte[] ALL_ZERO_MAC = {0, 0, 0, 0, 0, 0};
    static final int CLUSTER_CHANGE_EVENT_JOINED = 1;
    static final int CLUSTER_CHANGE_EVENT_STARTED = 0;
    private static final String TAG = "WifiAwareClientState";
    private static final boolean VDBG = false;
    private final AppOpsManager mAppOps;
    private final IWifiAwareEventCallback mCallback;
    private final String mCallingPackage;
    private final int mClientId;
    private ConfigRequest mConfigRequest;
    private final Context mContext;
    private final long mCreationTime;
    boolean mDbg = false;
    private byte[] mLastDiscoveryInterfaceMac = ALL_ZERO_MAC;
    private final boolean mNotifyIdentityChange;
    private final int mPid;
    private final SparseArray<WifiAwareDiscoverySessionState> mSessions = new SparseArray<>();
    private final int mUid;
    private final WifiPermissionsUtil mWifiPermissionsUtil;

    public WifiAwareClientState(Context context, int clientId, int uid, int pid, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyIdentityChange, long creationTime, WifiPermissionsUtil wifiPermissionsUtil) {
        this.mContext = context;
        this.mClientId = clientId;
        this.mUid = uid;
        this.mPid = pid;
        this.mCallingPackage = callingPackage;
        this.mCallback = callback;
        this.mConfigRequest = configRequest;
        this.mNotifyIdentityChange = notifyIdentityChange;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mCreationTime = creationTime;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
    }

    public void destroy() {
        for (int i = 0; i < this.mSessions.size(); i++) {
            this.mSessions.valueAt(i).terminate();
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

    public String getCallingPackage() {
        return this.mCallingPackage;
    }

    public boolean getNotifyIdentityChange() {
        return this.mNotifyIdentityChange;
    }

    public long getCreationTime() {
        return this.mCreationTime;
    }

    public SparseArray<WifiAwareDiscoverySessionState> getSessions() {
        return this.mSessions;
    }

    public WifiAwareDiscoverySessionState getAwareSessionStateForPubSubId(int pubSubId) {
        for (int i = 0; i < this.mSessions.size(); i++) {
            WifiAwareDiscoverySessionState session = this.mSessions.valueAt(i);
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
            return;
        }
        this.mSessions.delete(sessionId);
    }

    public WifiAwareDiscoverySessionState terminateSession(int sessionId) {
        WifiAwareDiscoverySessionState session = this.mSessions.get(sessionId);
        if (session == null) {
            Log.e(TAG, "terminateSession: sessionId doesn't exist - " + sessionId);
            return null;
        }
        session.terminate();
        this.mSessions.delete(sessionId);
        return session;
    }

    public WifiAwareDiscoverySessionState getSession(int sessionId) {
        return this.mSessions.get(sessionId);
    }

    public void onInterfaceAddressChange(byte[] mac) {
        if (this.mNotifyIdentityChange && !Arrays.equals(mac, this.mLastDiscoveryInterfaceMac)) {
            try {
                this.mCallback.onIdentityChanged(this.mWifiPermissionsUtil.checkCallersLocationPermission(this.mCallingPackage, this.mUid, true) ? mac : ALL_ZERO_MAC);
            } catch (RemoteException e) {
                Log.w(TAG, "onIdentityChanged: RemoteException - ignored: " + e);
            }
        }
        this.mLastDiscoveryInterfaceMac = mac;
    }

    public void onClusterChange(int flag, byte[] mac, byte[] currentDiscoveryInterfaceMac) {
        if (this.mNotifyIdentityChange && !Arrays.equals(currentDiscoveryInterfaceMac, this.mLastDiscoveryInterfaceMac)) {
            try {
                this.mCallback.onIdentityChanged(this.mWifiPermissionsUtil.checkCallersLocationPermission(this.mCallingPackage, this.mUid, true) ? currentDiscoveryInterfaceMac : ALL_ZERO_MAC);
            } catch (RemoteException e) {
                Log.w(TAG, "onIdentityChanged: RemoteException - ignored: " + e);
            }
        }
        this.mLastDiscoveryInterfaceMac = currentDiscoveryInterfaceMac;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("AwareClientState:");
        pw.println("  mClientId: " + this.mClientId);
        pw.println("  mConfigRequest: " + this.mConfigRequest);
        pw.println("  mNotifyIdentityChange: " + this.mNotifyIdentityChange);
        pw.println("  mCallback: " + this.mCallback);
        pw.println("  mSessions: [" + this.mSessions + "]");
        for (int i = 0; i < this.mSessions.size(); i++) {
            this.mSessions.valueAt(i).dump(fd, pw, args);
        }
    }
}
