package com.android.server.ethernet;

import android.content.Context;
import android.net.IEthernetManager.Stub;
import android.net.IEthernetServiceListener;
import android.net.IpConfiguration;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteCallbackList;
import android.util.Log;
import android.util.PrintWriterPrinter;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class EthernetServiceImpl extends Stub {
    private static final String TAG = "EthernetServiceImpl";
    private final Context mContext;
    private final EthernetConfigStore mEthernetConfigStore;
    private Handler mHandler;
    private IpConfiguration mIpConfiguration;
    private final RemoteCallbackList<IEthernetServiceListener> mListeners = new RemoteCallbackList();
    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private final EthernetNetworkFactory mTracker;

    public EthernetServiceImpl(Context context) {
        this.mContext = context;
        Log.i(TAG, "Creating EthernetConfigStore");
        this.mEthernetConfigStore = new EthernetConfigStore();
        this.mIpConfiguration = this.mEthernetConfigStore.readIpAndProxyConfigurations();
        Log.i(TAG, "Read stored IP configuration: " + this.mIpConfiguration);
        this.mTracker = new EthernetNetworkFactory(this.mListeners);
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "EthernetService");
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    public void start() {
        Log.i(TAG, "Starting Ethernet service");
        HandlerThread handlerThread = new HandlerThread("EthernetServiceThread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper());
        this.mTracker.start(this.mContext, this.mHandler);
        this.mStarted.set(true);
    }

    public IpConfiguration getConfiguration() {
        IpConfiguration ipConfiguration;
        enforceAccessPermission();
        synchronized (this.mIpConfiguration) {
            ipConfiguration = new IpConfiguration(this.mIpConfiguration);
        }
        return ipConfiguration;
    }

    public void setConfiguration(IpConfiguration config) {
        if (!this.mStarted.get()) {
            Log.w(TAG, "System isn't ready enough to change ethernet configuration");
        }
        enforceConnectivityInternalPermission();
        synchronized (this.mIpConfiguration) {
            this.mEthernetConfigStore.writeIpAndProxyConfigurations(config);
            if (!config.equals(this.mIpConfiguration)) {
                this.mIpConfiguration = new IpConfiguration(config);
                this.mTracker.stop();
                this.mTracker.start(this.mContext, this.mHandler);
            }
        }
    }

    public boolean isAvailable() {
        enforceAccessPermission();
        return this.mTracker.isTrackingInterface();
    }

    public void addListener(IEthernetServiceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        enforceAccessPermission();
        this.mListeners.register(listener);
    }

    public void removeListener(IEthernetServiceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        enforceAccessPermission();
        this.mListeners.unregister(listener);
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump EthernetService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Current Ethernet state: ");
        pw.increaseIndent();
        this.mTracker.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.println();
        pw.println("Stored Ethernet configuration: ");
        pw.increaseIndent();
        pw.println(this.mIpConfiguration);
        pw.decreaseIndent();
        pw.println("Handler:");
        pw.increaseIndent();
        this.mHandler.dump(new PrintWriterPrinter(pw), TAG);
        pw.decreaseIndent();
    }
}
