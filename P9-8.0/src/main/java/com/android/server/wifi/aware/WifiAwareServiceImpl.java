package com.android.server.wifi.aware;

import android.content.Context;
import android.net.wifi.RttManager.ParcelableRttParams;
import android.net.wifi.aware.Characteristics;
import android.net.wifi.aware.ConfigRequest;
import android.net.wifi.aware.ConfigRequest.Builder;
import android.net.wifi.aware.DiscoverySession;
import android.net.wifi.aware.IWifiAwareDiscoverySessionCallback;
import android.net.wifi.aware.IWifiAwareEventCallback;
import android.net.wifi.aware.IWifiAwareManager.Stub;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.SubscribeConfig;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiAwareServiceImpl extends Stub {
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareService";
    private static final boolean VDBG = false;
    private Context mContext;
    private final SparseArray<DeathRecipient> mDeathRecipientsByClientId = new SparseArray();
    private final Object mLock = new Object();
    private int mNextClientId = 1;
    private int mNextRangingId = 1;
    private WifiAwareStateManager mStateManager;
    private final SparseIntArray mUidByClientId = new SparseIntArray();

    public WifiAwareServiceImpl(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public int getMockableCallingUid() {
        return getCallingUid();
    }

    public void start(HandlerThread handlerThread, WifiAwareStateManager awareStateManager) {
        Log.i(TAG, "Starting Wi-Fi Aware service");
        this.mStateManager = awareStateManager;
        this.mStateManager.start(this.mContext, handlerThread.getLooper());
    }

    public void startLate() {
        Log.i(TAG, "Late initialization of Wi-Fi Aware service");
        this.mStateManager.startLate();
    }

    public boolean isUsageEnabled() {
        enforceAccessPermission();
        return this.mStateManager.isUsageEnabled();
    }

    public Characteristics getCharacteristics() {
        enforceAccessPermission();
        if (this.mStateManager.getCapabilities() == null) {
            return null;
        }
        return this.mStateManager.getCapabilities().toPublicCharacteristics();
    }

    public void connect(final IBinder binder, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyOnIdentityChanged) {
        enforceAccessPermission();
        enforceChangePermission();
        if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        } else if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else {
            final int clientId;
            if (notifyOnIdentityChanged) {
                enforceLocationPermission();
            }
            if (configRequest != null) {
                enforceConnectivityInternalPermission();
            } else {
                configRequest = new Builder().build();
            }
            configRequest.validate();
            int uid = getMockableCallingUid();
            int pid = getCallingPid();
            synchronized (this.mLock) {
                clientId = this.mNextClientId;
                this.mNextClientId = clientId + 1;
            }
            DeathRecipient dr = new DeathRecipient() {
                public void binderDied() {
                    binder.unlinkToDeath(this, 0);
                    synchronized (WifiAwareServiceImpl.this.mLock) {
                        WifiAwareServiceImpl.this.mDeathRecipientsByClientId.delete(clientId);
                        WifiAwareServiceImpl.this.mUidByClientId.delete(clientId);
                    }
                    WifiAwareServiceImpl.this.mStateManager.disconnect(clientId);
                }
            };
            try {
                binder.linkToDeath(dr, 0);
                synchronized (this.mLock) {
                    this.mDeathRecipientsByClientId.put(clientId, dr);
                    this.mUidByClientId.put(clientId, uid);
                }
                this.mStateManager.connect(clientId, uid, pid, callingPackage, callback, configRequest, notifyOnIdentityChanged);
            } catch (RemoteException e) {
                Log.e(TAG, "Error on linkToDeath - " + e);
                try {
                    callback.onConnectFail(1);
                } catch (RemoteException e2) {
                    Log.e(TAG, "Error on onConnectFail()");
                }
            }
        }
    }

    public void disconnect(int clientId, IBinder binder) {
        enforceAccessPermission();
        enforceChangePermission();
        enforceClientValidity(getMockableCallingUid(), clientId);
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        }
        synchronized (this.mLock) {
            DeathRecipient dr = (DeathRecipient) this.mDeathRecipientsByClientId.get(clientId);
            if (dr != null) {
                binder.unlinkToDeath(dr, 0);
                this.mDeathRecipientsByClientId.delete(clientId);
            }
            this.mUidByClientId.delete(clientId);
        }
        this.mStateManager.disconnect(clientId);
    }

    public void terminateSession(int clientId, int sessionId) {
        enforceAccessPermission();
        enforceChangePermission();
        enforceClientValidity(getMockableCallingUid(), clientId);
        this.mStateManager.terminateSession(clientId, sessionId);
    }

    public void publish(int clientId, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback callback) {
        enforceAccessPermission();
        enforceChangePermission();
        enforceLocationPermission();
        if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        } else if (publishConfig == null) {
            throw new IllegalArgumentException("PublishConfig must not be null");
        } else {
            publishConfig.assertValid(this.mStateManager.getCharacteristics());
            enforceClientValidity(getMockableCallingUid(), clientId);
            this.mStateManager.publish(clientId, publishConfig, callback);
        }
    }

    public void updatePublish(int clientId, int sessionId, PublishConfig publishConfig) {
        enforceAccessPermission();
        enforceChangePermission();
        if (publishConfig == null) {
            throw new IllegalArgumentException("PublishConfig must not be null");
        }
        publishConfig.assertValid(this.mStateManager.getCharacteristics());
        enforceClientValidity(getMockableCallingUid(), clientId);
        this.mStateManager.updatePublish(clientId, sessionId, publishConfig);
    }

    public void subscribe(int clientId, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback callback) {
        enforceAccessPermission();
        enforceChangePermission();
        enforceLocationPermission();
        if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        } else if (subscribeConfig == null) {
            throw new IllegalArgumentException("SubscribeConfig must not be null");
        } else {
            subscribeConfig.assertValid(this.mStateManager.getCharacteristics());
            enforceClientValidity(getMockableCallingUid(), clientId);
            this.mStateManager.subscribe(clientId, subscribeConfig, callback);
        }
    }

    public void updateSubscribe(int clientId, int sessionId, SubscribeConfig subscribeConfig) {
        enforceAccessPermission();
        enforceChangePermission();
        if (subscribeConfig == null) {
            throw new IllegalArgumentException("SubscribeConfig must not be null");
        }
        subscribeConfig.assertValid(this.mStateManager.getCharacteristics());
        enforceClientValidity(getMockableCallingUid(), clientId);
        this.mStateManager.updateSubscribe(clientId, sessionId, subscribeConfig);
    }

    public void sendMessage(int clientId, int sessionId, int peerId, byte[] message, int messageId, int retryCount) {
        enforceAccessPermission();
        enforceChangePermission();
        if (retryCount != 0) {
            enforceConnectivityInternalPermission();
        }
        if (message != null && message.length > this.mStateManager.getCharacteristics().getMaxServiceNameLength()) {
            throw new IllegalArgumentException("Message length longer than supported by device characteristics");
        } else if (retryCount < 0 || retryCount > DiscoverySession.getMaxSendRetryCount()) {
            throw new IllegalArgumentException("Invalid 'retryCount' must be non-negative and <= DiscoverySession.MAX_SEND_RETRY_COUNT");
        } else {
            enforceClientValidity(getMockableCallingUid(), clientId);
            this.mStateManager.sendMessage(clientId, sessionId, peerId, message, messageId, retryCount);
        }
    }

    public int startRanging(int clientId, int sessionId, ParcelableRttParams params) {
        enforceAccessPermission();
        enforceLocationPermission();
        enforceConnectivityInternalPermission();
        enforceClientValidity(getMockableCallingUid(), clientId);
        if (params.mParams.length == 0) {
            throw new IllegalArgumentException("Empty ranging parameters");
        }
        int rangingId;
        synchronized (this.mLock) {
            rangingId = this.mNextRangingId;
            this.mNextRangingId = rangingId + 1;
        }
        this.mStateManager.startRanging(clientId, sessionId, params.mParams, rangingId);
        return rangingId;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiAwareService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Wi-Fi Aware Service");
        synchronized (this.mLock) {
            pw.println("  mNextClientId: " + this.mNextClientId);
            pw.println("  mDeathRecipientsByClientId: " + this.mDeathRecipientsByClientId);
            pw.println("  mUidByClientId: " + this.mUidByClientId);
        }
        this.mStateManager.dump(fd, pw, args);
    }

    private void enforceClientValidity(int uid, int clientId) {
        synchronized (this.mLock) {
            int uidIndex = this.mUidByClientId.indexOfKey(clientId);
            if (uidIndex < 0 || this.mUidByClientId.valueAt(uidIndex) != uid) {
                throw new SecurityException("Attempting to use invalid uid+clientId mapping: uid=" + uid + ", clientId=" + clientId);
            }
        }
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
    }

    private void enforceLocationPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", TAG);
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
    }
}
