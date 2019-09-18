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

final class NetworkLogger {
    private static final String TAG = NetworkLogger.class.getSimpleName();
    private final DevicePolicyManagerService mDpm;
    private ServiceThread mHandlerThread;
    private IIpConnectivityMetrics mIpConnectivityMetrics;
    /* access modifiers changed from: private */
    public final AtomicBoolean mIsLoggingEnabled = new AtomicBoolean(false);
    private final INetdEventCallback mNetdEventCallback = new BaseNetdEventCallback() {
        public void onDnsEvent(String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) {
            if (NetworkLogger.this.mIsLoggingEnabled.get()) {
                DnsEvent dnsEvent = new DnsEvent(hostname, ipAddresses, ipAddressesCount, NetworkLogger.this.mPm.getNameForUid(uid), timestamp);
                sendNetworkEvent(dnsEvent);
            }
        }

        public void onConnectEvent(String ipAddr, int port, long timestamp, int uid) {
            if (NetworkLogger.this.mIsLoggingEnabled.get()) {
                ConnectEvent connectEvent = new ConnectEvent(ipAddr, port, NetworkLogger.this.mPm.getNameForUid(uid), timestamp);
                sendNetworkEvent(connectEvent);
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
    /* access modifiers changed from: private */
    public NetworkLoggingHandler mNetworkLoggingHandler;
    /* access modifiers changed from: private */
    public final PackageManagerInternal mPm;

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
                if (this.mHandlerThread != null) {
                    this.mHandlerThread.quitSafely();
                }
                return true;
            }
            boolean removeNetdEventCallback = this.mIpConnectivityMetrics.removeNetdEventCallback(1);
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
            return removeNetdEventCallback;
        } catch (RemoteException re) {
            Slog.wtf(TAG, "Failed to make remote calls to unregister the callback", re);
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
            return true;
        } catch (Throwable th) {
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void pause() {
        if (this.mNetworkLoggingHandler != null) {
            this.mNetworkLoggingHandler.pause();
        }
    }

    /* access modifiers changed from: package-private */
    public void resume() {
        if (this.mNetworkLoggingHandler != null) {
            this.mNetworkLoggingHandler.resume();
        }
    }

    /* access modifiers changed from: package-private */
    public void discardLogs() {
        if (this.mNetworkLoggingHandler != null) {
            this.mNetworkLoggingHandler.discardLogs();
        }
    }

    /* access modifiers changed from: package-private */
    public List<NetworkEvent> retrieveLogs(long batchToken) {
        return this.mNetworkLoggingHandler.retrieveFullLogBatch(batchToken);
    }
}
