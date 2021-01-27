package com.android.server.wifi.aware;

import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.os.Handler;
import android.os.IHwInterface;
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
    private HalDeviceManager mHalDeviceManager;
    private Handler mHandler;
    private InterfaceAvailableForRequestListener mInterfaceAvailableForRequestListener = new InterfaceAvailableForRequestListener();
    private InterfaceDestroyedListener mInterfaceDestroyedListener;
    private final Object mLock = new Object();
    private int mReferenceCount = 0;
    private WifiAwareNativeCallback mWifiAwareNativeCallback;
    private WifiAwareStateManager mWifiAwareStateManager;
    private IWifiNanIface mWifiNanIface = null;

    WifiAwareNativeManager(WifiAwareStateManager awareStateManager, HalDeviceManager halDeviceManager, WifiAwareNativeCallback wifiAwareNativeCallback) {
        this.mWifiAwareStateManager = awareStateManager;
        this.mHalDeviceManager = halDeviceManager;
        this.mWifiAwareNativeCallback = wifiAwareNativeCallback;
    }

    public android.hardware.wifi.V1_2.IWifiNanIface mockableCastTo_1_2(IWifiNanIface iface) {
        return android.hardware.wifi.V1_2.IWifiNanIface.castFrom((IHwInterface) iface);
    }

    public void start(Handler handler) {
        this.mHandler = handler;
        this.mHalDeviceManager.initialize();
        this.mHalDeviceManager.registerStatusListener(new HalDeviceManager.ManagerStatusListener() {
            /* class com.android.server.wifi.aware.WifiAwareNativeManager.AnonymousClass1 */

            @Override // com.android.server.wifi.HalDeviceManager.ManagerStatusListener
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
    /* access modifiers changed from: public */
    private void awareIsDown() {
        synchronized (this.mLock) {
            if (this.mDbg) {
                Log.d(TAG, "awareIsDown: mWifiNanIface=" + this.mWifiNanIface + ", mReferenceCount =" + this.mReferenceCount);
            }
            this.mWifiNanIface = null;
            this.mReferenceCount = 0;
            this.mWifiAwareStateManager.disableUsage();
        }
    }

    private class InterfaceDestroyedListener implements HalDeviceManager.InterfaceDestroyedListener {
        public boolean active;

        private InterfaceDestroyedListener() {
            this.active = true;
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceDestroyedListener
        public void onDestroyed(String ifaceName) {
            if (WifiAwareNativeManager.this.mDbg) {
                Log.d(WifiAwareNativeManager.TAG, "Interface was destroyed: mWifiNanIface=" + WifiAwareNativeManager.this.mWifiNanIface + ", active=" + this.active);
            }
            if (this.active && WifiAwareNativeManager.this.mWifiNanIface != null) {
                WifiAwareNativeManager.this.awareIsDown();
            }
        }
    }

    /* access modifiers changed from: private */
    public class InterfaceAvailableForRequestListener implements HalDeviceManager.InterfaceAvailableForRequestListener {
        private InterfaceAvailableForRequestListener() {
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceAvailableForRequestListener
        public void onAvailabilityChanged(boolean isAvailable) {
            if (WifiAwareNativeManager.this.mDbg) {
                Log.d(WifiAwareNativeManager.TAG, "Interface availability = " + isAvailable + ", mWifiNanIface=" + WifiAwareNativeManager.this.mWifiNanIface);
            }
            synchronized (WifiAwareNativeManager.this.mLock) {
                if (isAvailable) {
                    WifiAwareNativeManager.this.mWifiAwareStateManager.enableUsage();
                } else if (WifiAwareNativeManager.this.mWifiNanIface == null) {
                    WifiAwareNativeManager.this.mWifiAwareStateManager.disableUsage();
                }
            }
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
