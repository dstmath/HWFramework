package com.android.server.wifi.aware;

import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.HalDeviceManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiAwareNativeManager {
    private static final String TAG = "WifiAwareNativeManager";
    private static final boolean VDBG = false;
    boolean mDbg = false;
    /* access modifiers changed from: private */
    public HalDeviceManager mHalDeviceManager;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public InterfaceAvailableForRequestListener mInterfaceAvailableForRequestListener = new InterfaceAvailableForRequestListener();
    private InterfaceDestroyedListener mInterfaceDestroyedListener;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mReferenceCount = 0;
    private WifiAwareNativeCallback mWifiAwareNativeCallback;
    /* access modifiers changed from: private */
    public WifiAwareStateManager mWifiAwareStateManager;
    /* access modifiers changed from: private */
    public IWifiNanIface mWifiNanIface = null;

    private class InterfaceAvailableForRequestListener implements HalDeviceManager.InterfaceAvailableForRequestListener {
        private InterfaceAvailableForRequestListener() {
        }

        public void onAvailabilityChanged(boolean isAvailable) {
            if (WifiAwareNativeManager.this.mDbg) {
                Log.d(WifiAwareNativeManager.TAG, "Interface availability = " + isAvailable + ", mWifiNanIface=" + WifiAwareNativeManager.this.mWifiNanIface);
            }
            synchronized (WifiAwareNativeManager.this.mLock) {
                if (isAvailable) {
                    try {
                        WifiAwareNativeManager.this.mWifiAwareStateManager.enableUsage();
                    } catch (Throwable th) {
                        throw th;
                    }
                } else if (WifiAwareNativeManager.this.mWifiNanIface == null) {
                    WifiAwareNativeManager.this.mWifiAwareStateManager.disableUsage();
                }
            }
        }
    }

    private class InterfaceDestroyedListener implements HalDeviceManager.InterfaceDestroyedListener {
        public boolean active;

        private InterfaceDestroyedListener() {
            this.active = true;
        }

        public void onDestroyed(String ifaceName) {
            if (WifiAwareNativeManager.this.mDbg) {
                Log.d(WifiAwareNativeManager.TAG, "Interface was destroyed: mWifiNanIface=" + WifiAwareNativeManager.this.mWifiNanIface + ", active=" + this.active);
            }
            if (this.active && WifiAwareNativeManager.this.mWifiNanIface != null) {
                WifiAwareNativeManager.this.awareIsDown();
            }
        }
    }

    WifiAwareNativeManager(WifiAwareStateManager awareStateManager, HalDeviceManager halDeviceManager, WifiAwareNativeCallback wifiAwareNativeCallback) {
        this.mWifiAwareStateManager = awareStateManager;
        this.mHalDeviceManager = halDeviceManager;
        this.mWifiAwareNativeCallback = wifiAwareNativeCallback;
    }

    public android.hardware.wifi.V1_2.IWifiNanIface mockableCastTo_1_2(IWifiNanIface iface) {
        return android.hardware.wifi.V1_2.IWifiNanIface.castFrom(iface);
    }

    public void start(Handler handler) {
        this.mHandler = handler;
        this.mHalDeviceManager.initialize();
        this.mHalDeviceManager.registerStatusListener(new HalDeviceManager.ManagerStatusListener() {
            public void onStatusChanged() {
                if (WifiAwareNativeManager.this.mHalDeviceManager.isStarted()) {
                    WifiAwareNativeManager.this.mHalDeviceManager.registerInterfaceAvailableForRequestListener(3, WifiAwareNativeManager.this.mInterfaceAvailableForRequestListener, WifiAwareNativeManager.this.mHandler);
                } else {
                    WifiAwareNativeManager.this.awareIsDown();
                }
            }
        }, this.mHandler);
        if (this.mHalDeviceManager.isStarted()) {
            this.mHalDeviceManager.registerInterfaceAvailableForRequestListener(3, this.mInterfaceAvailableForRequestListener, this.mHandler);
        }
    }

    @VisibleForTesting
    public IWifiNanIface getWifiNanIface() {
        IWifiNanIface iWifiNanIface;
        synchronized (this.mLock) {
            iWifiNanIface = this.mWifiNanIface;
        }
        return iWifiNanIface;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b9, code lost:
        return;
     */
    public void tryToGetAware() {
        WifiStatus status;
        synchronized (this.mLock) {
            if (this.mDbg) {
                Log.d(TAG, "tryToGetAware: mWifiNanIface=" + this.mWifiNanIface + ", mReferenceCount=" + this.mReferenceCount);
            }
            if (this.mWifiNanIface != null) {
                this.mReferenceCount++;
            } else if (this.mHalDeviceManager == null) {
                Log.e(TAG, "tryToGetAware: mHalDeviceManager is null!?");
                awareIsDown();
            } else {
                this.mInterfaceDestroyedListener = new InterfaceDestroyedListener();
                IWifiNanIface iface = this.mHalDeviceManager.createNanIface(this.mInterfaceDestroyedListener, this.mHandler);
                if (iface == null) {
                    Log.e(TAG, "Was not able to obtain an IWifiNanIface (even though enabled!?)");
                    awareIsDown();
                } else {
                    if (this.mDbg) {
                        Log.v(TAG, "Obtained an IWifiNanIface");
                    }
                    try {
                        android.hardware.wifi.V1_2.IWifiNanIface iface12 = mockableCastTo_1_2(iface);
                        if (iface12 == null) {
                            this.mWifiAwareNativeCallback.mIsHal12OrLater = false;
                            status = iface.registerEventCallback(this.mWifiAwareNativeCallback);
                        } else {
                            this.mWifiAwareNativeCallback.mIsHal12OrLater = true;
                            status = iface12.registerEventCallback_1_2(this.mWifiAwareNativeCallback);
                        }
                        if (status.code != 0) {
                            Log.e(TAG, "IWifiNanIface.registerEventCallback error: " + statusString(status));
                            this.mHalDeviceManager.removeIface(iface);
                            awareIsDown();
                            return;
                        }
                        this.mWifiNanIface = iface;
                        this.mReferenceCount = 1;
                    } catch (RemoteException e) {
                        Log.e(TAG, "IWifiNanIface.registerEventCallback exception: " + e);
                        awareIsDown();
                    }
                }
            }
        }
    }

    public void releaseAware() {
        if (this.mDbg) {
            Log.d(TAG, "releaseAware: mWifiNanIface=" + this.mWifiNanIface + ", mReferenceCount=" + this.mReferenceCount);
        }
        if (this.mWifiNanIface != null) {
            if (this.mHalDeviceManager == null) {
                Log.e(TAG, "releaseAware: mHalDeviceManager is null!?");
                return;
            }
            synchronized (this.mLock) {
                this.mReferenceCount--;
                if (this.mReferenceCount == 0) {
                    this.mInterfaceDestroyedListener.active = false;
                    this.mInterfaceDestroyedListener = null;
                    this.mHalDeviceManager.removeIface(this.mWifiNanIface);
                    this.mWifiNanIface = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void awareIsDown() {
        synchronized (this.mLock) {
            if (this.mDbg) {
                Log.d(TAG, "awareIsDown: mWifiNanIface=" + this.mWifiNanIface + ", mReferenceCount =" + this.mReferenceCount);
            }
            this.mWifiNanIface = null;
            this.mReferenceCount = 0;
            this.mWifiAwareStateManager.disableUsage();
        }
    }

    private static String statusString(WifiStatus status) {
        if (status == null) {
            return "status=null";
        }
        return status.code + " (" + status.description + ")";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WifiAwareNativeManager:");
        pw.println("  mWifiNanIface: " + this.mWifiNanIface);
        pw.println("  mReferenceCount: " + this.mReferenceCount);
        this.mWifiAwareNativeCallback.dump(fd, pw, args);
        this.mHalDeviceManager.dump(fd, pw, args);
    }
}
