package android.hardware.display;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.IDisplayManager;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.IVirtualDisplayCallback;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.DisplayInfo;
import android.view.Surface;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalEx;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DisplayManagerGlobal implements IHwDisplayManagerGlobalInner {
    private static final boolean DEBUG = false;
    public static final int EVENT_DISPLAY_ADDED = 1;
    public static final int EVENT_DISPLAY_CHANGED = 2;
    public static final int EVENT_DISPLAY_REMOVED = 3;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "DisplayManager";
    private static final boolean USE_CACHE = false;
    private static DisplayManagerGlobal sInstance;
    private DisplayManagerCallback mCallback;
    private int[] mDisplayIdCache;
    private final SparseArray<DisplayInfo> mDisplayInfoCache = new SparseArray<>();
    private final ArrayList<DisplayListenerDelegate> mDisplayListeners = new ArrayList<>();
    private final IDisplayManager mDm;
    IHwDisplayManagerGlobalEx mHwDmg = null;
    private AtomicBoolean mIAwareCacheEnable = new AtomicBoolean(false);
    private final Object mLock = new Object();
    private int mWifiDisplayScanNestCount;

    private static final class DisplayListenerDelegate extends Handler {
        public final DisplayManager.DisplayListener mListener;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public DisplayListenerDelegate(DisplayManager.DisplayListener listener, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper(), null, true);
            this.mListener = listener;
        }

        public void sendDisplayEvent(int displayId, int event) {
            sendMessage(obtainMessage(event, displayId, 0));
        }

        public void clearEvents() {
            removeCallbacksAndMessages(null);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.mListener.onDisplayAdded(msg.arg1);
                    return;
                case 2:
                    this.mListener.onDisplayChanged(msg.arg1);
                    return;
                case 3:
                    this.mListener.onDisplayRemoved(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    private final class DisplayManagerCallback extends IDisplayManagerCallback.Stub {
        private DisplayManagerCallback() {
        }

        public void onDisplayEvent(int displayId, int event) {
            DisplayManagerGlobal.this.handleDisplayEvent(displayId, event);
        }
    }

    private static final class VirtualDisplayCallback extends IVirtualDisplayCallback.Stub {
        private VirtualDisplayCallbackDelegate mDelegate;

        public VirtualDisplayCallback(VirtualDisplay.Callback callback, Handler handler) {
            if (callback != null) {
                this.mDelegate = new VirtualDisplayCallbackDelegate(callback, handler);
            }
        }

        public void onPaused() {
            if (this.mDelegate != null) {
                this.mDelegate.sendEmptyMessage(0);
            }
        }

        public void onResumed() {
            if (this.mDelegate != null) {
                this.mDelegate.sendEmptyMessage(1);
            }
        }

        public void onStopped() {
            if (this.mDelegate != null) {
                this.mDelegate.sendEmptyMessage(2);
            }
        }
    }

    private static final class VirtualDisplayCallbackDelegate extends Handler {
        public static final int MSG_DISPLAY_PAUSED = 0;
        public static final int MSG_DISPLAY_RESUMED = 1;
        public static final int MSG_DISPLAY_STOPPED = 2;
        private final VirtualDisplay.Callback mCallback;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public VirtualDisplayCallbackDelegate(VirtualDisplay.Callback callback, Handler handler) {
            super(handler != null ? handler.getLooper() : Looper.myLooper(), null, true);
            this.mCallback = callback;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    this.mCallback.onPaused();
                    return;
                case 1:
                    this.mCallback.onResumed();
                    return;
                case 2:
                    this.mCallback.onStopped();
                    return;
                default:
                    return;
            }
        }
    }

    private DisplayManagerGlobal(IDisplayManager dm) {
        this.mDm = dm;
        this.mHwDmg = HwFrameworkFactory.getHwDisplayManagerGlobalEx(this);
    }

    public static DisplayManagerGlobal getInstance() {
        DisplayManagerGlobal displayManagerGlobal;
        synchronized (DisplayManagerGlobal.class) {
            if (sInstance == null) {
                IBinder b = ServiceManager.getService(Context.DISPLAY_SERVICE);
                if (b != null) {
                    sInstance = new DisplayManagerGlobal(IDisplayManager.Stub.asInterface(b));
                }
            }
            displayManagerGlobal = sInstance;
        }
        return displayManagerGlobal;
    }

    public DisplayInfo getDisplayInfo(int displayId) {
        try {
            synchronized (this.mLock) {
                if (this.mIAwareCacheEnable.get()) {
                    DisplayInfo info = this.mDisplayInfoCache.get(displayId);
                    if (info != null) {
                        return info;
                    }
                }
                DisplayInfo info2 = this.mDm.getDisplayInfo(displayId);
                if (info2 == null) {
                    return null;
                }
                if (this.mIAwareCacheEnable.get()) {
                    this.mDisplayInfoCache.put(displayId, info2);
                }
                registerCallbackIfNeededLocked();
                return info2;
            }
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public int[] getDisplayIds() {
        int[] displayIds;
        try {
            synchronized (this.mLock) {
                displayIds = this.mDm.getDisplayIds();
                registerCallbackIfNeededLocked();
            }
            return displayIds;
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public Display getCompatibleDisplay(int displayId, DisplayAdjustments daj) {
        DisplayInfo displayInfo = getDisplayInfo(displayId);
        if (displayInfo == null) {
            return null;
        }
        return new Display(this, displayId, displayInfo, daj);
    }

    public Display getCompatibleDisplay(int displayId, Resources resources) {
        DisplayInfo displayInfo = getDisplayInfo(displayId);
        if (displayInfo == null) {
            return null;
        }
        return new Display(this, displayId, displayInfo, resources);
    }

    public Display getRealDisplay(int displayId) {
        return getCompatibleDisplay(displayId, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    public void registerDisplayListener(DisplayManager.DisplayListener listener, Handler handler) {
        if (listener != null) {
            synchronized (this.mLock) {
                if (findDisplayListenerLocked(listener) < 0) {
                    this.mDisplayListeners.add(new DisplayListenerDelegate(listener, handler));
                    registerCallbackIfNeededLocked();
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    public void unregisterDisplayListener(DisplayManager.DisplayListener listener) {
        if (listener != null) {
            synchronized (this.mLock) {
                int index = findDisplayListenerLocked(listener);
                if (index >= 0) {
                    this.mDisplayListeners.get(index).clearEvents();
                    this.mDisplayListeners.remove(index);
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    private int findDisplayListenerLocked(DisplayManager.DisplayListener listener) {
        int numListeners = this.mDisplayListeners.size();
        for (int i = 0; i < numListeners; i++) {
            if (this.mDisplayListeners.get(i).mListener == listener) {
                return i;
            }
        }
        return -1;
    }

    private void registerCallbackIfNeededLocked() {
        if (this.mCallback == null) {
            this.mCallback = new DisplayManagerCallback();
            try {
                this.mDm.registerCallback(this.mCallback);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDisplayEvent(int displayId, int event) {
        synchronized (this.mLock) {
            if (this.mIAwareCacheEnable.get()) {
                this.mDisplayInfoCache.remove(displayId);
                if (event == 1 || event == 3) {
                    this.mDisplayIdCache = null;
                }
            }
            int numListeners = this.mDisplayListeners.size();
            for (int i = 0; i < numListeners; i++) {
                this.mDisplayListeners.get(i).sendDisplayEvent(displayId, event);
            }
        }
    }

    public void startWifiDisplayScan() {
        synchronized (this.mLock) {
            if (HWFLOW) {
                Log.d(TAG, "startWifiDisplayScan mWifiDisplayScanNestCount=" + this.mWifiDisplayScanNestCount);
            }
            int i = this.mWifiDisplayScanNestCount;
            this.mWifiDisplayScanNestCount = i + 1;
            if (i == 0) {
                registerCallbackIfNeededLocked();
                try {
                    this.mDm.startWifiDisplayScan();
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            }
        }
    }

    public void stopWifiDisplayScan() {
        synchronized (this.mLock) {
            int i = this.mWifiDisplayScanNestCount - 1;
            this.mWifiDisplayScanNestCount = i;
            if (i == 0) {
                try {
                    this.mDm.stopWifiDisplayScan();
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            } else if (this.mWifiDisplayScanNestCount < 0) {
                Log.wtf(TAG, "Wifi display scan nest count became negative: " + this.mWifiDisplayScanNestCount);
                this.mWifiDisplayScanNestCount = 0;
            }
            if (HWFLOW) {
                Log.d(TAG, "stopWifiDisplayScan mWifiDisplayScanNestCount=" + this.mWifiDisplayScanNestCount);
            }
        }
    }

    public void connectWifiDisplay(String deviceAddress) {
        if (deviceAddress != null) {
            try {
                this.mDm.connectWifiDisplay(deviceAddress);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("deviceAddress must not be null");
        }
    }

    public void pauseWifiDisplay() {
        try {
            this.mDm.pauseWifiDisplay();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void resumeWifiDisplay() {
        try {
            this.mDm.resumeWifiDisplay();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void disconnectWifiDisplay() {
        try {
            this.mDm.disconnectWifiDisplay();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void renameWifiDisplay(String deviceAddress, String alias) {
        if (deviceAddress != null) {
            try {
                this.mDm.renameWifiDisplay(deviceAddress, alias);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("deviceAddress must not be null");
        }
    }

    public void forgetWifiDisplay(String deviceAddress) {
        if (deviceAddress != null) {
            try {
                this.mDm.forgetWifiDisplay(deviceAddress);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("deviceAddress must not be null");
        }
    }

    public WifiDisplayStatus getWifiDisplayStatus() {
        try {
            return this.mDm.getWifiDisplayStatus();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void requestColorMode(int displayId, int colorMode) {
        try {
            this.mDm.requestColorMode(displayId, colorMode);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setSaturationLevel(float level) {
        try {
            this.mDm.setSaturationLevel(level);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public VirtualDisplay createVirtualDisplay(Context context, MediaProjection projection, String name, int width, int height, int densityDpi, Surface surface, int flags, VirtualDisplay.Callback callback, Handler handler, String uniqueId) {
        String str = name;
        if (TextUtils.isEmpty(name)) {
            Surface surface2 = surface;
            throw new IllegalArgumentException("name must be non-null and non-empty");
        } else if (width <= 0 || height <= 0 || densityDpi <= 0) {
            Surface surface3 = surface;
            throw new IllegalArgumentException("width, height, and densityDpi must be greater than 0");
        } else {
            VirtualDisplayCallback callbackWrapper = new VirtualDisplayCallback(callback, handler);
            try {
                VirtualDisplayCallback callbackWrapper2 = callbackWrapper;
                try {
                    int displayId = this.mDm.createVirtualDisplay(callbackWrapper, projection != null ? projection.getProjection() : null, context.getPackageName(), str, width, height, densityDpi, surface, flags, uniqueId);
                    if (displayId < 0) {
                        Log.e(TAG, "Could not create virtual display: " + str);
                        return null;
                    }
                    Display display = getRealDisplay(displayId);
                    if (display != null) {
                        return new VirtualDisplay(this, display, callbackWrapper2, surface);
                    }
                    Log.wtf(TAG, "Could not obtain display info for newly created virtual display: " + str);
                    try {
                        this.mDm.releaseVirtualDisplay(callbackWrapper2);
                        return null;
                    } catch (RemoteException ex) {
                        throw ex.rethrowFromSystemServer();
                    }
                } catch (RemoteException e) {
                    ex = e;
                    Surface surface4 = surface;
                    throw ex.rethrowFromSystemServer();
                }
            } catch (RemoteException e2) {
                ex = e2;
                Surface surface5 = surface;
                VirtualDisplayCallback virtualDisplayCallback = callbackWrapper;
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public void setVirtualDisplaySurface(IVirtualDisplayCallback token, Surface surface) {
        try {
            this.mDm.setVirtualDisplaySurface(token, surface);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void resizeVirtualDisplay(IVirtualDisplayCallback token, int width, int height, int densityDpi) {
        try {
            this.mDm.resizeVirtualDisplay(token, width, height, densityDpi);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void releaseVirtualDisplay(IVirtualDisplayCallback token) {
        try {
            this.mDm.releaseVirtualDisplay(token);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public Point getStableDisplaySize() {
        try {
            return this.mDm.getStableDisplaySize();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public List<BrightnessChangeEvent> getBrightnessEvents(String callingPackage) {
        try {
            ParceledListSlice<BrightnessChangeEvent> events = this.mDm.getBrightnessEvents(callingPackage);
            if (events == null) {
                return Collections.emptyList();
            }
            return events.getList();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setBrightnessConfigurationForUser(BrightnessConfiguration c, int userId, String packageName) {
        try {
            this.mDm.setBrightnessConfigurationForUser(c, userId, packageName);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public BrightnessConfiguration getBrightnessConfigurationForUser(int userId) {
        try {
            return this.mDm.getBrightnessConfigurationForUser(userId);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public BrightnessConfiguration getDefaultBrightnessConfiguration() {
        try {
            return this.mDm.getDefaultBrightnessConfiguration();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setTemporaryBrightness(int brightness) {
        try {
            this.mDm.setTemporaryBrightness(brightness);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
        try {
            this.mDm.setTemporaryAutoBrightnessAdjustment(adjustment);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public Pair<float[], float[]> getMinimumBrightnessCurve() {
        try {
            Curve curve = this.mDm.getMinimumBrightnessCurve();
            return Pair.create(curve.getX(), curve.getY());
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public List<AmbientBrightnessDayStats> getAmbientBrightnessStats() {
        try {
            ParceledListSlice<AmbientBrightnessDayStats> stats = this.mDm.getAmbientBrightnessStats();
            if (stats == null) {
                return Collections.emptyList();
            }
            return stats.getList();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setIAwareCacheEnable(boolean cache) {
        synchronized (this.mLock) {
            this.mIAwareCacheEnable.set(cache);
            if (!cache) {
                this.mDisplayInfoCache.clear();
            }
        }
    }

    public IDisplayManager getService() {
        return this.mDm;
    }

    public int getWifiDisplayScanNestCount() {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiDisplayScanNestCount;
        }
        return i;
    }

    public void addWifiDisplayScanNestCount() {
        synchronized (this.mLock) {
            this.mWifiDisplayScanNestCount++;
        }
    }

    public Object getLock() {
        return this.mLock;
    }

    public void registerCallbackIfNeededLockedInner() {
        registerCallbackIfNeededLocked();
    }

    public void startWifiDisplayScan(int channelId) {
        if (this.mHwDmg != null) {
            this.mHwDmg.startWifiDisplayScan(channelId);
        }
    }

    public void connectWifiDisplay(String deviceAddress, String verificaitonCode) {
        if (this.mHwDmg != null) {
            this.mHwDmg.connectWifiDisplay(deviceAddress, verificaitonCode);
        }
    }

    public void checkVerificationResult(boolean isRight) {
        if (this.mHwDmg != null) {
            this.mHwDmg.checkVerificationResult(isRight);
        }
    }

    public boolean sendWifiDisplayAction(String action) {
        if (this.mHwDmg != null) {
            return this.mHwDmg.sendWifiDisplayAction(action);
        }
        return false;
    }
}
