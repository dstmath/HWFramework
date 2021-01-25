package com.android.server.display;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import com.android.server.display.DisplayAdapterEx;
import com.android.server.display.DisplayManagerServiceEx;
import com.android.server.display.OverlayDisplayWindowEx;
import com.android.server.display.SurfaceControlExt;
import com.huawei.screenrecorder.activities.SurfaceControlEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HwVrDisplayAdapter extends DefaultHwVrDisplayAdapter {
    private static final int DEFAULT_DISPLAY_PARAMS_LEN = 3;
    private static final int DEFAULT_OVERLAY_NUM = 1;
    private static final long REFRESH_SCALE = 1000000000;
    private static final String TAG = "HwVrDisplayAdapter";
    private static final String UNIQUE_ID_PREFIX = "hwoverlay:";
    private final int INDEX_DENSITY = 2;
    private final int INDEX_HEIGHT = 1;
    private final int INDEX_WIDTH = 0;
    private final int LEN_OF_OVERMODE = 4;
    private final List<OverlayDisplayHandle> mOverlays = new ArrayList(16);
    private final Handler mUiHandler;

    public HwVrDisplayAdapter(DisplayManagerServiceEx.SyncRootEx syncRoot, Context context, Handler handler, DisplayAdapterEx.ListenerEx listener, Handler uiHandler) {
        super(syncRoot, context, handler, listener, uiHandler);
        this.mUiHandler = uiHandler;
    }

    public void registerLocked() {
        HwVrDisplayAdapter.super.registerLocked();
    }

    public boolean createVrDisplay(String displayName, int[] displayParams) {
        boolean createVrDisplayLocked;
        Log.i(TAG, "createVrDisplay " + displayName + " with params " + Arrays.toString(displayParams));
        if (TextUtils.isEmpty(displayName) || displayParams == null || displayParams.length != DEFAULT_DISPLAY_PARAMS_LEN) {
            Log.e(TAG, "params is invalid in createVrDisplay.");
            return false;
        }
        synchronized (getSyncRoot()) {
            createVrDisplayLocked = createVrDisplayLocked(displayName, displayParams);
        }
        return createVrDisplayLocked;
    }

    public boolean destroyVrDisplay(String displayName) {
        boolean destroyVrDisplayLocked;
        Log.i(TAG, "destroyVrDisplay " + displayName);
        if (TextUtils.isEmpty(displayName)) {
            Log.e(TAG, "params is invalid in destroyVrDisplay.");
            return false;
        }
        synchronized (getSyncRoot()) {
            destroyVrDisplayLocked = destroyVrDisplayLocked(displayName);
        }
        return destroyVrDisplayLocked;
    }

    public boolean destroyAllVrDisplay() {
        synchronized (getSyncRoot()) {
            Log.i(TAG, "destroyAllVrDisplay");
            if (this.mOverlays != null) {
                if (!this.mOverlays.isEmpty()) {
                    Iterator<OverlayDisplayHandle> iterator = this.mOverlays.iterator();
                    while (iterator.hasNext()) {
                        iterator.next().dismissLocked();
                        iterator.remove();
                    }
                    return true;
                }
            }
            Log.e(TAG, "mOverlays is invalid in destroyAllVrDisplay.");
            return false;
        }
    }

    private boolean createVrDisplayLocked(String displayName, int[] displayParams) {
        int width = displayParams[0];
        int height = displayParams[1];
        int density = displayParams[2];
        ArrayList<OverlayMode> modes = new ArrayList<>(4);
        modes.add(new OverlayMode(width, height, density));
        this.mOverlays.add(new OverlayDisplayHandle(displayName, modes, 8388659, true, 1));
        return true;
    }

    private boolean destroyVrDisplayLocked(String displayName) {
        List<OverlayDisplayHandle> list = this.mOverlays;
        if (list == null || displayName == null) {
            Log.e(TAG, "mOverlays is invalid in destroyVrDisplayLocked.");
            return false;
        } else if (list.isEmpty()) {
            return true;
        } else {
            for (OverlayDisplayHandle overlay : this.mOverlays) {
                overlay.dismissLocked();
            }
            this.mOverlays.clear();
            return true;
        }
    }

    /* access modifiers changed from: private */
    public final class OverlayDisplayHandle extends OverlayDisplayWindowEx.ListenerEx {
        private static final int DEFAULT_MODE_INDEX = 0;
        private final boolean isSecure;
        private int mActiveMode;
        private OverlayDisplayDevice mDevice;
        private final Runnable mDismissRunnable = new Runnable() {
            /* class com.android.server.display.HwVrDisplayAdapter.OverlayDisplayHandle.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                OverlayDisplayWindowEx window;
                synchronized (HwVrDisplayAdapter.this.getSyncRoot()) {
                    window = OverlayDisplayHandle.this.mWindow;
                    OverlayDisplayHandle.this.mWindow = null;
                }
                if (window != null) {
                    window.dismiss();
                }
            }
        };
        private final int mGravity;
        private final ArrayList<OverlayMode> mModes;
        private final String mName;
        private final int mNumber;
        private final Runnable mResizeRunnable = new Runnable() {
            /* class com.android.server.display.HwVrDisplayAdapter.OverlayDisplayHandle.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (HwVrDisplayAdapter.this.getSyncRoot()) {
                    if (OverlayDisplayHandle.this.mWindow == null) {
                        Log.e(HwVrDisplayAdapter.TAG, "mWindow is null.");
                        return;
                    }
                    if (OverlayDisplayHandle.this.mActiveMode >= 0) {
                        if (OverlayDisplayHandle.this.mActiveMode < OverlayDisplayHandle.this.mModes.size()) {
                            OverlayMode mode = (OverlayMode) OverlayDisplayHandle.this.mModes.get(OverlayDisplayHandle.this.mActiveMode);
                            OverlayDisplayHandle.this.mWindow.resize(mode.mWidth, mode.mHeight, mode.mDensityDpi);
                            return;
                        }
                    }
                    Log.e(HwVrDisplayAdapter.TAG, "error in resize, mode index " + OverlayDisplayHandle.this.mActiveMode + " error.");
                }
            }
        };
        private final Runnable mShowRunnable = new Runnable() {
            /* class com.android.server.display.HwVrDisplayAdapter.OverlayDisplayHandle.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                if (OverlayDisplayHandle.this.mActiveMode < 0 || OverlayDisplayHandle.this.mActiveMode >= OverlayDisplayHandle.this.mModes.size()) {
                    Log.e(HwVrDisplayAdapter.TAG, "error in show, mode index " + OverlayDisplayHandle.this.mActiveMode + " error.");
                    return;
                }
                OverlayMode mode = (OverlayMode) OverlayDisplayHandle.this.mModes.get(OverlayDisplayHandle.this.mActiveMode);
                OverlayDisplayWindowEx window = new OverlayDisplayWindowEx(HwVrDisplayAdapter.this.getContext(), OverlayDisplayHandle.this.mName, mode.mWidth, mode.mHeight, mode.mDensityDpi, OverlayDisplayHandle.this.mGravity, OverlayDisplayHandle.this.isSecure, OverlayDisplayHandle.this);
                window.show();
                synchronized (HwVrDisplayAdapter.this.getSyncRoot()) {
                    OverlayDisplayHandle.this.mWindow = window;
                }
            }
        };
        private OverlayDisplayWindowEx mWindow;

        public OverlayDisplayHandle(String name, ArrayList<OverlayMode> modes, int gravity, boolean secure, int number) {
            this.mName = name;
            this.mModes = modes;
            this.mGravity = gravity;
            this.isSecure = secure;
            this.mNumber = number;
            this.mActiveMode = 0;
            showLocked();
        }

        private void showLocked() {
            if (HwVrDisplayAdapter.this.mUiHandler != null) {
                HwVrDisplayAdapter.this.mUiHandler.post(this.mShowRunnable);
            }
        }

        public void dismissLocked() {
            if (HwVrDisplayAdapter.this.mUiHandler != null) {
                HwVrDisplayAdapter.this.mUiHandler.removeCallbacks(this.mShowRunnable);
                HwVrDisplayAdapter.this.mUiHandler.post(this.mDismissRunnable);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onActiveModeChangedLocked(int index) {
            if (HwVrDisplayAdapter.this.mUiHandler != null) {
                HwVrDisplayAdapter.this.mUiHandler.removeCallbacks(this.mResizeRunnable);
            }
            this.mActiveMode = index;
            if (this.mWindow != null) {
                HwVrDisplayAdapter.this.mUiHandler.post(this.mResizeRunnable);
            }
        }

        public void onWindowCreated(SurfaceTexture surfaceTexture, float refreshRate, long presentationDeadlineNanos, int state) {
            synchronized (HwVrDisplayAdapter.this.getSyncRoot()) {
                this.mDevice = new OverlayDisplayDevice(SurfaceControlEx.createDisplay(this.mName, this.isSecure), this.mName, this.mModes, this.mActiveMode, 0, refreshRate, presentationDeadlineNanos, this.isSecure, state, surfaceTexture, this.mNumber) {
                    /* class com.android.server.display.HwVrDisplayAdapter.OverlayDisplayHandle.AnonymousClass1 */

                    @Override // com.android.server.display.HwVrDisplayAdapter.OverlayDisplayDevice
                    public void onModeChangedLocked(int index) {
                        OverlayDisplayHandle.this.onActiveModeChangedLocked(index);
                    }
                };
                HwVrDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, DisplayAdapterEx.DISPLAY_DEVICE_EVENT_ADDED);
            }
        }

        public void onWindowDestroyed() {
            synchronized (HwVrDisplayAdapter.this.getSyncRoot()) {
                if (this.mDevice != null) {
                    this.mDevice.destroyLocked();
                    HwVrDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, DisplayAdapterEx.DISPLAY_DEVICE_EVENT_REMOVED);
                }
            }
        }

        public void onStateChanged(int state) {
            synchronized (HwVrDisplayAdapter.this.getSyncRoot()) {
                if (this.mDevice != null) {
                    this.mDevice.setStateLocked(state);
                    HwVrDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, DisplayAdapterEx.DISPLAY_DEVICE_EVENT_CHANGED);
                }
            }
        }
    }

    private abstract class OverlayDisplayDevice extends DisplayDeviceExt {
        private final boolean isSecure;
        private int mActiveMode;
        private final int mDefaultMode;
        private final long mDisplayPresentationDeadlineNanos;
        private DisplayDeviceInfoEx mInfo;
        private Display.Mode[] mModes = null;
        private final String mName;
        private final List<OverlayMode> mRawModes;
        private final float mRefreshRate;
        private int mState;
        private Surface mSurface;
        private SurfaceTexture mSurfaceTexture;
        private String mUniqueName;
        final /* synthetic */ HwVrDisplayAdapter this$0;

        public abstract void onModeChangedLocked(int i);

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public OverlayDisplayDevice(HwVrDisplayAdapter hwVrDisplayAdapter, IBinder displayToken, String name, List<OverlayMode> modes, int activeMode, int defaultMode, float refreshRate, long presentationDeadlineNanos, boolean secure, int state, SurfaceTexture surfaceTexture, int number) {
            super(hwVrDisplayAdapter, displayToken, HwVrDisplayAdapter.UNIQUE_ID_PREFIX + number);
            List<OverlayMode> list = modes;
            this.this$0 = hwVrDisplayAdapter;
            this.mName = name;
            this.mUniqueName = HwVrDisplayAdapter.UNIQUE_ID_PREFIX + number;
            this.mRefreshRate = refreshRate;
            this.mDisplayPresentationDeadlineNanos = presentationDeadlineNanos;
            this.isSecure = secure;
            this.mState = state;
            this.mSurfaceTexture = surfaceTexture;
            this.mRawModes = list;
            int modeSize = 0;
            if (list != null) {
                this.mModes = new Display.Mode[modes.size()];
                modeSize = modes.size();
            }
            int i = 0;
            while (i < modeSize) {
                OverlayMode mode = list.get(i);
                this.mModes[i] = hwVrDisplayAdapter.createMode(mode.mWidth, mode.mHeight, refreshRate);
                i++;
                list = modes;
            }
            this.mActiveMode = activeMode;
            this.mDefaultMode = defaultMode;
        }

        public void destroyLocked() {
            this.mSurfaceTexture = null;
            Surface surface = this.mSurface;
            if (surface != null) {
                surface.release();
                this.mSurface = null;
            }
            SurfaceControlEx.destroyDisplay(getDisplayTokenLocked());
        }

        public boolean hasStableUniqueId() {
            return false;
        }

        public void performTraversalLocked(SurfaceControlExt.TransactionEx transaction) {
            SurfaceTexture surfaceTexture = this.mSurfaceTexture;
            if (surfaceTexture != null) {
                if (this.mSurface == null) {
                    this.mSurface = new Surface(surfaceTexture);
                }
                setSurfaceLocked(transaction, this.mSurface);
            }
        }

        public void setStateLocked(int state) {
            this.mState = state;
            this.mInfo = null;
        }

        public DisplayDeviceInfoEx getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                int i = this.mActiveMode;
                if (i < 0 || i >= this.mRawModes.size()) {
                    Log.e(HwVrDisplayAdapter.TAG, "error in getDisplayDeviceInfoLocked, mode index " + this.mActiveMode + " error.");
                    return new DisplayDeviceInfoEx();
                }
                Display.Mode mode = this.mModes[this.mActiveMode];
                this.mInfo = new DisplayDeviceInfoEx();
                this.mInfo.setName(this.mName);
                this.mInfo.setUniqueId(this.mUniqueName);
                this.mInfo.setWidth(mode.getPhysicalWidth());
                this.mInfo.setHeight(mode.getPhysicalHeight());
                this.mInfo.setModeId(mode.getModeId());
                this.mInfo.setDefaultModeId(this.mModes[0].getModeId());
                this.mInfo.setSupportedModes(this.mModes);
                OverlayMode rawMode = this.mRawModes.get(this.mActiveMode);
                this.mInfo.setDensityDpi(rawMode.mDensityDpi);
                this.mInfo.setXdpi((float) rawMode.mDensityDpi);
                this.mInfo.setYdpi((float) rawMode.mDensityDpi);
                int rate = (int) this.mRefreshRate;
                if (rate != 0) {
                    this.mInfo.setPresentationDeadlineNanos(this.mDisplayPresentationDeadlineNanos + (HwVrDisplayAdapter.REFRESH_SCALE / ((long) rate)));
                }
                this.mInfo.setFlags(DisplayDeviceInfoEx.FLAG_PRESENTATION | DisplayDeviceInfoEx.FLAG_DESTROY_CONTENT_ON_REMOVAL);
                if (this.isSecure) {
                    DisplayDeviceInfoEx displayDeviceInfoEx = this.mInfo;
                    displayDeviceInfoEx.setFlags(displayDeviceInfoEx.getFlags() | DisplayDeviceInfoEx.FLAG_SECURE);
                }
                this.mInfo.setType(4);
                this.mInfo.setTouch(DisplayDeviceInfoEx.TOUCH_VIRTUAL);
                this.mInfo.setState(this.mState);
            }
            return this.mInfo;
        }
    }

    /* access modifiers changed from: private */
    public static final class OverlayMode {
        final int mDensityDpi;
        final int mHeight;
        final int mWidth;

        OverlayMode(int width, int height, int densityDpi) {
            this.mWidth = width;
            this.mHeight = height;
            this.mDensityDpi = densityDpi;
        }
    }
}
