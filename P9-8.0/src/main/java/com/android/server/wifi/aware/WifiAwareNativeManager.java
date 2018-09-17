package com.android.server.wifi.aware;

import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.HalDeviceManager.ManagerStatusListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;

class WifiAwareNativeManager {
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareNativeManager";
    private HalDeviceManager mHalDeviceManager;
    private InterfaceAvailableForRequestListener mInterfaceAvailableForRequestListener = new InterfaceAvailableForRequestListener(this, null);
    private InterfaceDestroyedListener mInterfaceDestroyedListener = new InterfaceDestroyedListener(this, null);
    private final Object mLock = new Object();
    private WifiAwareNativeCallback mWifiAwareNativeCallback;
    private WifiAwareStateManager mWifiAwareStateManager;
    private IWifiNanIface mWifiNanIface = null;

    private class InterfaceAvailableForRequestListener implements com.android.server.wifi.HalDeviceManager.InterfaceAvailableForRequestListener {
        /* synthetic */ InterfaceAvailableForRequestListener(WifiAwareNativeManager this$0, InterfaceAvailableForRequestListener -this1) {
            this();
        }

        private InterfaceAvailableForRequestListener() {
        }

        public void onAvailableForRequest() {
            WifiAwareNativeManager.this.tryToGetAware();
        }
    }

    private class InterfaceDestroyedListener implements com.android.server.wifi.HalDeviceManager.InterfaceDestroyedListener {
        /* synthetic */ InterfaceDestroyedListener(WifiAwareNativeManager this$0, InterfaceDestroyedListener -this1) {
            this();
        }

        private InterfaceDestroyedListener() {
        }

        public void onDestroyed() {
            WifiAwareNativeManager.this.awareIsDown();
        }
    }

    WifiAwareNativeManager(WifiAwareStateManager awareStateManager, HalDeviceManager halDeviceManager, WifiAwareNativeCallback wifiAwareNativeCallback) {
        this.mWifiAwareStateManager = awareStateManager;
        this.mHalDeviceManager = halDeviceManager;
        this.mWifiAwareNativeCallback = wifiAwareNativeCallback;
        this.mHalDeviceManager.registerStatusListener(new ManagerStatusListener() {
            public void onStatusChanged() {
                if (WifiAwareNativeManager.this.mHalDeviceManager.isStarted()) {
                    WifiAwareNativeManager.this.mHalDeviceManager.registerInterfaceAvailableForRequestListener(3, WifiAwareNativeManager.this.mInterfaceAvailableForRequestListener, null);
                } else {
                    WifiAwareNativeManager.this.awareIsDown();
                }
            }
        }, null);
        if (this.mHalDeviceManager.isStarted()) {
            tryToGetAware();
        }
    }

    IWifiNanIface getWifiNanIface() {
        IWifiNanIface iWifiNanIface;
        synchronized (this.mLock) {
            iWifiNanIface = this.mWifiNanIface;
        }
        return iWifiNanIface;
    }

    /* JADX WARNING: Missing block: B:11:0x0015, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void tryToGetAware() {
        synchronized (this.mLock) {
            if (this.mWifiNanIface != null) {
                return;
            }
            IWifiNanIface iface = this.mHalDeviceManager.createNanIface(this.mInterfaceDestroyedListener, null);
            if (iface != null) {
                try {
                    WifiStatus status = iface.registerEventCallback(this.mWifiAwareNativeCallback);
                    if (status.code != 0) {
                        Log.e(TAG, "IWifiNanIface.registerEventCallback error: " + statusString(status));
                        this.mHalDeviceManager.removeIface(iface);
                        return;
                    }
                    this.mWifiNanIface = iface;
                    this.mWifiAwareStateManager.enableUsage();
                } catch (RemoteException e) {
                    Log.e(TAG, "IWifiNanIface.registerEventCallback exception: " + e);
                    this.mHalDeviceManager.removeIface(iface);
                }
            }
        }
    }

    private void awareIsDown() {
        synchronized (this.mLock) {
            if (this.mWifiNanIface != null) {
                this.mWifiNanIface = null;
                this.mWifiAwareStateManager.disableUsage();
            }
        }
    }

    private static String statusString(WifiStatus status) {
        if (status == null) {
            return "status=null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(status.code).append(" (").append(status.description).append(")");
        return sb.toString();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WifiAwareNativeManager:");
        pw.println("  mWifiNanIface: " + this.mWifiNanIface);
        this.mHalDeviceManager.dump(fd, pw, args);
    }
}
