package com.android.server.devicepolicy;

import android.app.admin.ConnectEvent;
import android.app.admin.DnsEvent;
import android.app.admin.NetworkEvent;
import android.content.pm.PackageManagerInternal;
import android.net.IIpConnectivityMetrics;
import android.net.INetdEventCallback;
import android.net.INetdEventCallback.Stub;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.server.ServiceThread;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class NetworkLogger {
    private static final String TAG = NetworkLogger.class.getSimpleName();
    private final DevicePolicyManagerService mDpm;
    private ServiceThread mHandlerThread;
    private IIpConnectivityMetrics mIpConnectivityMetrics;
    private final AtomicBoolean mIsLoggingEnabled = new AtomicBoolean(false);
    private final INetdEventCallback mNetdEventCallback = new Stub() {
        public void onDnsEvent(String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) {
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

    boolean startNetworkLogging() {
        Log.d(TAG, "Starting network logging.");
        if (checkIpConnectivityMetricsService()) {
            try {
                if (!this.mIpConnectivityMetrics.registerNetdEventCallback(this.mNetdEventCallback)) {
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
        Slog.wtf(TAG, "Failed to register callback with IIpConnectivityMetrics.");
        return false;
    }

    boolean stopNetworkLogging() {
        Log.d(TAG, "Stopping network logging");
        this.mIsLoggingEnabled.set(false);
        discardLogs();
        try {
            if (checkIpConnectivityMetricsService()) {
                boolean unregisterNetdEventCallback = this.mIpConnectivityMetrics.unregisterNetdEventCallback();
                if (this.mHandlerThread != null) {
                    this.mHandlerThread.quitSafely();
                }
                return unregisterNetdEventCallback;
            }
            Slog.wtf(TAG, "Failed to unregister callback with IIpConnectivityMetrics.");
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
            return true;
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

    void pause() {
        if (this.mNetworkLoggingHandler != null) {
            this.mNetworkLoggingHandler.pause();
        }
    }

    void resume() {
        if (this.mNetworkLoggingHandler != null) {
            this.mNetworkLoggingHandler.resume();
        }
    }

    void discardLogs() {
        if (this.mNetworkLoggingHandler != null) {
            this.mNetworkLoggingHandler.discardLogs();
        }
    }

    List<NetworkEvent> retrieveLogs(long batchToken) {
        return this.mNetworkLoggingHandler.retrieveFullLogBatch(batchToken);
    }
}
