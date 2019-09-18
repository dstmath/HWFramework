package com.android.server.ethernet;

import android.content.Context;
import android.net.IEthernetManager;
import android.net.IEthernetServiceListener;
import android.net.IpConfiguration;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.util.Log;
import android.util.PrintWriterPrinter;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class EthernetServiceImpl extends IEthernetManager.Stub {
    private static final String TAG = "EthernetServiceImpl";
    private final Context mContext;
    private Handler mHandler;
    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private EthernetTracker mTracker;

    public EthernetServiceImpl(Context context) {
        this.mContext = context;
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "EthernetService");
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    private void enforceUseRestrictedNetworksPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_USE_RESTRICTED_NETWORKS", "ConnectivityService");
    }

    private boolean checkUseRestrictedNetworksPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.CONNECTIVITY_USE_RESTRICTED_NETWORKS") == 0;
    }

    public void start() {
        Log.i(TAG, "Starting Ethernet service");
        HandlerThread handlerThread = new HandlerThread("EthernetServiceThread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper());
        this.mTracker = new EthernetTracker(this.mContext, this.mHandler);
        this.mTracker.start();
        this.mStarted.set(true);
    }

    public String[] getAvailableInterfaces() throws RemoteException {
        return this.mTracker.getInterfaces(checkUseRestrictedNetworksPermission());
    }

    public IpConfiguration getConfiguration(String iface) {
        enforceAccessPermission();
        if (this.mTracker.isRestrictedInterface(iface)) {
            enforceUseRestrictedNetworksPermission();
        }
        return new IpConfiguration(this.mTracker.getIpConfiguration(iface));
    }

    public void setConfiguration(String iface, IpConfiguration config) {
        if (!this.mStarted.get()) {
            Log.w(TAG, "System isn't ready enough to change ethernet configuration");
        }
        enforceConnectivityInternalPermission();
        if (this.mTracker.isRestrictedInterface(iface)) {
            enforceUseRestrictedNetworksPermission();
        }
        this.mTracker.updateIpConfiguration(iface, new IpConfiguration(config));
    }

    public boolean isAvailable(String iface) {
        enforceAccessPermission();
        if (this.mTracker.isRestrictedInterface(iface)) {
            enforceUseRestrictedNetworksPermission();
        }
        return this.mTracker.isTrackingInterface(iface);
    }

    public void addListener(IEthernetServiceListener listener) {
        if (listener != null) {
            enforceAccessPermission();
            this.mTracker.addListener(listener, checkUseRestrictedNetworksPermission());
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    public void removeListener(IEthernetServiceListener listener) {
        if (listener != null) {
            enforceAccessPermission();
            this.mTracker.removeListener(listener);
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump EthernetService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Current Ethernet state: ");
        pw.increaseIndent();
        this.mTracker.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.println("Handler:");
        pw.increaseIndent();
        this.mHandler.dump(new PrintWriterPrinter(pw), TAG);
        pw.decreaseIndent();
    }
}
