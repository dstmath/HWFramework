package com.android.server.devicepolicy;

import android.app.admin.ConnectEvent;
import android.app.admin.DnsEvent;
import android.app.admin.NetworkEvent;
import android.content.pm.PackageManagerInternal;
import android.net.IIpConnectivityMetrics;
import android.net.INetdEventCallback;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.server.ServiceThread;
import com.android.server.net.BaseNetdEventCallback;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/* access modifiers changed from: package-private */
public final class NetworkLogger {
    private static final String TAG = NetworkLogger.class.getSimpleName();
    private final DevicePolicyManagerService mDpm;
    private ServiceThread mHandlerThread;
    private IIpConnectivityMetrics mIpConnectivityMetrics;
    private final AtomicBoolean mIsLoggingEnabled = new AtomicBoolean(false);
    private final INetdEventCallback mNetdEventCallback = new BaseNetdEventCallback() {
        /* class com.android.server.devicepolicy.NetworkLogger.AnonymousClass1 */

        public void onDnsEvent(int netId, int eventType, int returnCode, String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) {
            if (NetworkLogger.this.mIsLoggingEnabled.get()) {
                sendNetworkEvent(new DnsEvent(hostname, ipAddresses, ipAddressesCount, NetworkLogger.this.mPm.getNameForUid(uid), timestamp));
            }
        }

        public void onConnectEvent(String ipAddr, int port, long timestamp, int uid) {
            if (NetworkLogger.this.mIsLoggingEnabled.get()) {
                sendNetworkEvent(new ConnectEvent(ipAddr, port, NetworkLogger.this.mPm.getNameForUid(uid), timestamp));
            }
        }

        private void sendNetworkEvent(NetworkEvent event) {
            Message msg = NetworkLogger.this.mNetworkLoggingHandler.obtainMessage(1);
            Bundle bundle = new Bundle();
            bundle.putParcelable("network_event", event);
            msg.setData(bundle);
            NetworkLogger.this.mNetworkLoggingHandler.sendMessage(msg);
        }
    };
    private NetworkLoggingHandler mNetworkLoggingHandler;
    private final PackageManagerInternal mPm;

    NetworkLogger(DevicePolicyManagerService dpm, PackageManagerInternal pm) {
        this.mDpm = dpm;
        this.mPm = pm;
    }

    private boolean checkIpConnectivityMetricsService() {
        if (this.mIpConnectivityMetrics != null) {
            return true;
        }
        IIpConnectivityMetrics service = this.mDpm.mInjector.getIIpConnectivityMetrics();
        if (service == null) {
            return false;
        }
        this.mIpConnectivityMetrics = service;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean startNetworkLogging() {
        Log.d(TAG, "Starting network logging.");
        if (!checkIpConnectivityMetricsService()) {
            Slog.wtf(TAG, "Failed to register callback with IIpConnectivityMetrics.");
            return false;
        }
        try {
            if (!this.mIpConnectivityMetrics.addNetdEventCallback(1, this.mNetdEventCallback)) {
                return false;
            }
            this.mHandlerThread = new ServiceThread(TAG, 10, false);
            this.mHandlerThread.start();
            this.mNetworkLoggingHandler = new NetworkLoggingHandler(this.mHandlerThread.getLooper(), this.mDpm);
            this.mNetworkLoggingHandler.scheduleBatchFinalization();
            this.mIsLoggingEnabled.set(true);
            return true;
        } catch (RemoteException re) {
            Slog.wtf(TAG, "Failed to make remote calls to register the callback", re);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean stopNetworkLogging() {
        Log.d(TAG, "Stopping network logging");
        this.mIsLoggingEnabled.set(false);
        discardLogs();
        try {
            if (!checkIpConnectivityMetricsService()) {
                Slog.wtf(TAG, "Failed to unregister callback with IIpConnectivityMetrics.");
                ServiceThread serviceThread = this.mHandlerThread;
                if (serviceThread != null) {
                    serviceThread.quitSafely();
                }
                return true;
            }
            boolean removeNetdEventCallback = this.mIpConnectivityMetrics.removeNetdEventCallback(1);
            ServiceThread serviceThread2 = this.mHandlerThread;
            if (serviceThread2 != null) {
                serviceThread2.quitSafely();
            }
            return removeNetdEventCallback;
        } catch (RemoteException re) {
            Slog.wtf(TAG, "Failed to make remote calls to unregister the callback", re);
            ServiceThread serviceThread3 = this.mHandlerThread;
            if (serviceThread3 != null) {
                serviceThread3.quitSafely();
            }
            return true;
        } catch (Throwable th) {
            ServiceThread serviceThread4 = this.mHandlerThread;
            if (serviceThread4 != null) {
                serviceThread4.quitSafely();
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void pause() {
        NetworkLoggingHandler networkLoggingHandler = this.mNetworkLoggingHandler;
        if (networkLoggingHandler != null) {
            networkLoggingHandler.pause();
        }
    }

    /* access modifiers changed from: package-private */
    public void resume() {
        NetworkLoggingHandler networkLoggingHandler = this.mNetworkLoggingHandler;
        if (networkLoggingHandler != null) {
            networkLoggingHandler.resume();
        }
    }

    /* access modifiers changed from: package-private */
    public void discardLogs() {
        NetworkLoggingHandler networkLoggingHandler = this.mNetworkLoggingHandler;
        if (networkLoggingHandler != null) {
            networkLoggingHandler.discardLogs();
        }
    }

    /* access modifiers changed from: package-private */
    public List<NetworkEvent> retrieveLogs(long batchToken) {
        return this.mNetworkLoggingHandler.retrieveFullLogBatch(batchToken);
    }

    /* access modifiers changed from: package-private */
    public long forceBatchFinalization() {
        return this.mNetworkLoggingHandler.forceBatchFinalization();
    }
}
