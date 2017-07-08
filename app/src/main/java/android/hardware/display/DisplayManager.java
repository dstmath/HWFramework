package android.hardware.display;

import android.content.Context;
import android.hardware.display.VirtualDisplay.Callback;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;
import java.util.ArrayList;

public final class DisplayManager {
    public static final String ACTION_WIFI_DISPLAY_STATUS_CHANGED = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";
    private static final boolean DEBUG = false;
    public static final String DISPLAY_CATEGORY_PRESENTATION = "android.hardware.display.category.PRESENTATION";
    public static final String EXTRA_WIFI_DISPLAY_STATUS = "android.hardware.display.extra.WIFI_DISPLAY_STATUS";
    private static final String TAG = "DisplayManager";
    public static final int VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR = 16;
    public static final int VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY = 8;
    public static final int VIRTUAL_DISPLAY_FLAG_PRESENTATION = 2;
    public static final int VIRTUAL_DISPLAY_FLAG_PUBLIC = 1;
    public static final int VIRTUAL_DISPLAY_FLAG_SECURE = 4;
    private final Context mContext;
    private final SparseArray<Display> mDisplays;
    private final DisplayManagerGlobal mGlobal;
    private final Object mLock;
    private final ArrayList<Display> mTempDisplays;

    public interface DisplayListener {
        void onDisplayAdded(int i);

        void onDisplayChanged(int i);

        void onDisplayRemoved(int i);
    }

    public DisplayManager(Context context) {
        this.mLock = new Object();
        this.mDisplays = new SparseArray();
        this.mTempDisplays = new ArrayList();
        this.mContext = context;
        this.mGlobal = DisplayManagerGlobal.getInstance();
    }

    public Display getDisplay(int displayId) {
        Display orCreateDisplayLocked;
        synchronized (this.mLock) {
            orCreateDisplayLocked = getOrCreateDisplayLocked(displayId, DEBUG);
        }
        return orCreateDisplayLocked;
    }

    public Display[] getDisplays() {
        return getDisplays(null);
    }

    public Display[] getDisplays(String category) {
        Display[] displayArr;
        int[] displayIds = this.mGlobal.getDisplayIds();
        synchronized (this.mLock) {
            if (category == null) {
                try {
                    addAllDisplaysLocked(this.mTempDisplays, displayIds);
                } catch (Throwable th) {
                    this.mTempDisplays.clear();
                }
            } else if (category.equals(DISPLAY_CATEGORY_PRESENTATION)) {
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, 3);
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, VIRTUAL_DISPLAY_FLAG_PRESENTATION);
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, VIRTUAL_DISPLAY_FLAG_SECURE);
                addPresentationDisplaysLocked(this.mTempDisplays, displayIds, 5);
            }
            displayArr = (Display[]) this.mTempDisplays.toArray(new Display[this.mTempDisplays.size()]);
            this.mTempDisplays.clear();
        }
        return displayArr;
    }

    private void addAllDisplaysLocked(ArrayList<Display> displays, int[] displayIds) {
        for (int i = 0; i < displayIds.length; i += VIRTUAL_DISPLAY_FLAG_PUBLIC) {
            Display display = getOrCreateDisplayLocked(displayIds[i], true);
            if (display != null) {
                displays.add(display);
            }
        }
    }

    private void addPresentationDisplaysLocked(ArrayList<Display> displays, int[] displayIds, int matchType) {
        for (int i = 0; i < displayIds.length; i += VIRTUAL_DISPLAY_FLAG_PUBLIC) {
            Display display = getOrCreateDisplayLocked(displayIds[i], true);
            if (!(display == null || (display.getFlags() & VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY) == 0 || display.getType() != matchType)) {
                displays.add(display);
            }
        }
    }

    private Display getOrCreateDisplayLocked(int displayId, boolean assumeValid) {
        Display display = (Display) this.mDisplays.get(displayId);
        if (display == null) {
            display = this.mGlobal.getCompatibleDisplay(displayId, this.mContext.getDisplayAdjustments(displayId));
            if (display == null) {
                return display;
            }
            this.mDisplays.put(displayId, display);
            return display;
        } else if (assumeValid || display.isValid()) {
            return display;
        } else {
            return null;
        }
    }

    public void registerDisplayListener(DisplayListener listener, Handler handler) {
        this.mGlobal.registerDisplayListener(listener, handler);
    }

    public void unregisterDisplayListener(DisplayListener listener) {
        this.mGlobal.unregisterDisplayListener(listener);
    }

    public void startWifiDisplayScan() {
        Log.d(TAG, "startWifiDisplayScan, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        this.mGlobal.startWifiDisplayScan();
    }

    public void stopWifiDisplayScan() {
        Log.d(TAG, "stopWifiDisplayScan, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        this.mGlobal.stopWifiDisplayScan();
    }

    public void connectWifiDisplay(String deviceAddress) {
        this.mGlobal.connectWifiDisplay(deviceAddress);
    }

    public void pauseWifiDisplay() {
        this.mGlobal.pauseWifiDisplay();
    }

    public void resumeWifiDisplay() {
        this.mGlobal.resumeWifiDisplay();
    }

    public void disconnectWifiDisplay() {
        this.mGlobal.disconnectWifiDisplay();
    }

    public void renameWifiDisplay(String deviceAddress, String alias) {
        this.mGlobal.renameWifiDisplay(deviceAddress, alias);
    }

    public void forgetWifiDisplay(String deviceAddress) {
        this.mGlobal.forgetWifiDisplay(deviceAddress);
    }

    public WifiDisplayStatus getWifiDisplayStatus() {
        return this.mGlobal.getWifiDisplayStatus();
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int densityDpi, Surface surface, int flags) {
        return createVirtualDisplay(name, width, height, densityDpi, surface, flags, null, null);
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int densityDpi, Surface surface, int flags, Callback callback, Handler handler) {
        return createVirtualDisplay(null, name, width, height, densityDpi, surface, flags, callback, handler);
    }

    public VirtualDisplay createVirtualDisplay(MediaProjection projection, String name, int width, int height, int densityDpi, Surface surface, int flags, Callback callback, Handler handler) {
        return this.mGlobal.createVirtualDisplay(this.mContext, projection, name, width, height, densityDpi, surface, flags, callback, handler);
    }
}
