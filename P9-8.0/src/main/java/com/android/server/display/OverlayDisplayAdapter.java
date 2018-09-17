package com.android.server.display;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings.Global;
import android.util.Slog;
import android.view.Display.Mode;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.display.DisplayManagerService.SyncRoot;
import com.android.server.display.OverlayDisplayWindow.Listener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class OverlayDisplayAdapter extends DisplayAdapter {
    static final boolean DEBUG = false;
    private static final Pattern DISPLAY_PATTERN = Pattern.compile("([^,]+)(,[a-z]+)*");
    private static final int MAX_HEIGHT = 4096;
    private static final int MAX_WIDTH = 4096;
    private static final int MIN_HEIGHT = 100;
    private static final int MIN_WIDTH = 100;
    private static final Pattern MODE_PATTERN = Pattern.compile("(\\d+)x(\\d+)/(\\d+)");
    static final String TAG = "OverlayDisplayAdapter";
    private static final String UNIQUE_ID_PREFIX = "overlay:";
    private String mCurrentOverlaySetting = "";
    private final ArrayList<OverlayDisplayHandle> mOverlays = new ArrayList();
    private final Handler mUiHandler;

    private abstract class OverlayDisplayDevice extends DisplayDevice {
        private int mActiveMode;
        private final int mDefaultMode;
        private final long mDisplayPresentationDeadlineNanos;
        private DisplayDeviceInfo mInfo;
        private final Mode[] mModes;
        private final String mName;
        private final List<OverlayMode> mRawModes;
        private final float mRefreshRate;
        private final boolean mSecure;
        private int mState;
        private Surface mSurface;
        private SurfaceTexture mSurfaceTexture;

        public abstract void onModeChangedLocked(int i);

        public OverlayDisplayDevice(IBinder displayToken, String name, List<OverlayMode> modes, int activeMode, int defaultMode, float refreshRate, long presentationDeadlineNanos, boolean secure, int state, SurfaceTexture surfaceTexture, int number) {
            super(OverlayDisplayAdapter.this, displayToken, OverlayDisplayAdapter.UNIQUE_ID_PREFIX + number);
            this.mName = name;
            this.mRefreshRate = refreshRate;
            this.mDisplayPresentationDeadlineNanos = presentationDeadlineNanos;
            this.mSecure = secure;
            this.mState = state;
            this.mSurfaceTexture = surfaceTexture;
            this.mRawModes = modes;
            this.mModes = new Mode[modes.size()];
            for (int i = 0; i < modes.size(); i++) {
                OverlayMode mode = (OverlayMode) modes.get(i);
                this.mModes[i] = DisplayAdapter.createMode(mode.mWidth, mode.mHeight, refreshRate);
            }
            this.mActiveMode = activeMode;
            this.mDefaultMode = defaultMode;
        }

        public void destroyLocked() {
            this.mSurfaceTexture = null;
            if (this.mSurface != null) {
                this.mSurface.release();
                this.mSurface = null;
            }
            SurfaceControl.destroyDisplay(getDisplayTokenLocked());
        }

        public boolean hasStableUniqueId() {
            return false;
        }

        public void performTraversalInTransactionLocked() {
            if (this.mSurfaceTexture != null) {
                if (this.mSurface == null) {
                    this.mSurface = new Surface(this.mSurfaceTexture);
                }
                setSurfaceInTransactionLocked(this.mSurface);
            }
        }

        public void setStateLocked(int state) {
            this.mState = state;
            this.mInfo = null;
            Slog.w(OverlayDisplayAdapter.TAG, "@@@@@@ OverlayDisplayAdapter--setStateLocked--change the mInfo to null");
        }

        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            Slog.w(OverlayDisplayAdapter.TAG, "@@@@@@ OverlayDisplayAdapter--getDisplayDeviceInfoLocked--mInfo = " + this.mInfo);
            if (this.mInfo == null) {
                Mode mode = this.mModes[this.mActiveMode];
                OverlayMode rawMode = (OverlayMode) this.mRawModes.get(this.mActiveMode);
                this.mInfo = new DisplayDeviceInfo();
                this.mInfo.name = this.mName;
                this.mInfo.uniqueId = getUniqueId();
                this.mInfo.width = mode.getPhysicalWidth();
                this.mInfo.height = mode.getPhysicalHeight();
                this.mInfo.modeId = mode.getModeId();
                this.mInfo.defaultModeId = this.mModes[0].getModeId();
                this.mInfo.supportedModes = this.mModes;
                this.mInfo.densityDpi = rawMode.mDensityDpi;
                this.mInfo.xDpi = (float) rawMode.mDensityDpi;
                this.mInfo.yDpi = (float) rawMode.mDensityDpi;
                this.mInfo.presentationDeadlineNanos = this.mDisplayPresentationDeadlineNanos + (1000000000 / ((long) ((int) this.mRefreshRate)));
                this.mInfo.flags = 64;
                if (this.mSecure) {
                    DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 4;
                }
                this.mInfo.type = 4;
                this.mInfo.touch = 0;
                this.mInfo.state = this.mState;
            }
            return this.mInfo;
        }

        public void requestDisplayModesInTransactionLocked(int color, int id) {
            int index = -1;
            if (id == 0) {
                index = 0;
            } else {
                for (int i = 0; i < this.mModes.length; i++) {
                    if (this.mModes[i].getModeId() == id) {
                        index = i;
                        break;
                    }
                }
            }
            if (index == -1) {
                Slog.w(OverlayDisplayAdapter.TAG, "Unable to locate mode " + id + ", reverting to default.");
                index = this.mDefaultMode;
            }
            if (this.mActiveMode != index) {
                this.mActiveMode = index;
                this.mInfo = null;
                Slog.w(OverlayDisplayAdapter.TAG, "@@@@@@ OverlayDisplayAdapter--requestDisplayModesInTransactionLocked--change the mInfo to null");
                OverlayDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
                onModeChangedLocked(index);
            }
        }
    }

    private final class OverlayDisplayHandle implements Listener {
        private static final int DEFAULT_MODE_INDEX = 0;
        private int mActiveMode;
        private OverlayDisplayDevice mDevice;
        private final Runnable mDismissRunnable = new Runnable() {
            public void run() {
                OverlayDisplayWindow window;
                synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                    window = OverlayDisplayHandle.this.mWindow;
                    OverlayDisplayHandle.this.mWindow = null;
                }
                if (window != null) {
                    window.dismiss();
                }
            }
        };
        private final int mGravity;
        private final List<OverlayMode> mModes;
        private final String mName;
        private final int mNumber;
        private final Runnable mResizeRunnable = new Runnable() {
            public void run() {
                synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                    if (OverlayDisplayHandle.this.mWindow == null) {
                        return;
                    }
                    OverlayMode mode = (OverlayMode) OverlayDisplayHandle.this.mModes.get(OverlayDisplayHandle.this.mActiveMode);
                    OverlayDisplayWindow window = OverlayDisplayHandle.this.mWindow;
                    window.resize(mode.mWidth, mode.mHeight, mode.mDensityDpi);
                }
            }
        };
        private final boolean mSecure;
        private final Runnable mShowRunnable = new Runnable() {
            public void run() {
                OverlayMode mode = (OverlayMode) OverlayDisplayHandle.this.mModes.get(OverlayDisplayHandle.this.mActiveMode);
                OverlayDisplayWindow window = new OverlayDisplayWindow(OverlayDisplayAdapter.this.getContext(), OverlayDisplayHandle.this.mName, mode.mWidth, mode.mHeight, mode.mDensityDpi, OverlayDisplayHandle.this.mGravity, OverlayDisplayHandle.this.mSecure, OverlayDisplayHandle.this);
                window.show();
                synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                    OverlayDisplayHandle.this.mWindow = window;
                }
            }
        };
        private OverlayDisplayWindow mWindow;

        public OverlayDisplayHandle(String name, List<OverlayMode> modes, int gravity, boolean secure, int number) {
            this.mName = name;
            this.mModes = modes;
            this.mGravity = gravity;
            this.mSecure = secure;
            this.mNumber = number;
            this.mActiveMode = 0;
            showLocked();
        }

        private void showLocked() {
            OverlayDisplayAdapter.this.mUiHandler.post(this.mShowRunnable);
        }

        public void dismissLocked() {
            OverlayDisplayAdapter.this.mUiHandler.removeCallbacks(this.mShowRunnable);
            OverlayDisplayAdapter.this.mUiHandler.post(this.mDismissRunnable);
        }

        private void onActiveModeChangedLocked(int index) {
            OverlayDisplayAdapter.this.mUiHandler.removeCallbacks(this.mResizeRunnable);
            this.mActiveMode = index;
            Slog.w(OverlayDisplayAdapter.TAG, "@@@@@@ OverlayDisplayAdapter--onActiveModeChangedLocked--index = " + index);
            if (this.mWindow != null) {
                OverlayDisplayAdapter.this.mUiHandler.post(this.mResizeRunnable);
            }
        }

        public void onWindowCreated(SurfaceTexture surfaceTexture, float refreshRate, long presentationDeadlineNanos, int state) {
            synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                this.mDevice = new OverlayDisplayDevice(OverlayDisplayAdapter.this, SurfaceControl.createDisplay(this.mName, this.mSecure), this.mName, this.mModes, this.mActiveMode, 0, refreshRate, presentationDeadlineNanos, this.mSecure, state, surfaceTexture, this.mNumber) {
                    public void onModeChangedLocked(int index) {
                        OverlayDisplayHandle.this.onActiveModeChangedLocked(index);
                    }
                };
                Slog.w(OverlayDisplayAdapter.TAG, "@@@@@@ OverlayDisplayAdapter--onWindowCreated--mModes = " + this.mModes + "; mActiveMode = " + this.mActiveMode);
                OverlayDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, 1);
            }
        }

        public void onWindowDestroyed() {
            synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                if (this.mDevice != null) {
                    this.mDevice.destroyLocked();
                    OverlayDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, 3);
                }
            }
        }

        public void onStateChanged(int state) {
            synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                if (this.mDevice != null) {
                    this.mDevice.setStateLocked(state);
                    OverlayDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, 2);
                }
            }
        }

        public void dumpLocked(PrintWriter pw) {
            pw.println("  " + this.mName + ":");
            pw.println("    mModes=" + Arrays.toString(this.mModes.toArray()));
            pw.println("    mActiveMode=" + this.mActiveMode);
            pw.println("    mGravity=" + this.mGravity);
            pw.println("    mSecure=" + this.mSecure);
            pw.println("    mNumber=" + this.mNumber);
            if (this.mWindow != null) {
                IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "    ");
                ipw.increaseIndent();
                DumpUtils.dumpAsync(OverlayDisplayAdapter.this.mUiHandler, this.mWindow, ipw, "", 200);
            }
        }
    }

    private static final class OverlayMode {
        final int mDensityDpi;
        final int mHeight;
        final int mWidth;

        OverlayMode(int width, int height, int densityDpi) {
            this.mWidth = width;
            this.mHeight = height;
            this.mDensityDpi = densityDpi;
        }

        public String toString() {
            return "{" + "width=" + this.mWidth + ", height=" + this.mHeight + ", densityDpi=" + this.mDensityDpi + "}";
        }
    }

    public OverlayDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, Handler uiHandler) {
        super(syncRoot, context, handler, listener, TAG);
        this.mUiHandler = uiHandler;
    }

    public void dumpLocked(PrintWriter pw) {
        super.dumpLocked(pw);
        pw.println("mCurrentOverlaySetting=" + this.mCurrentOverlaySetting);
        pw.println("mOverlays: size=" + this.mOverlays.size());
        for (OverlayDisplayHandle overlay : this.mOverlays) {
            overlay.dumpLocked(pw);
        }
    }

    public void registerLocked() {
        super.registerLocked();
        getHandler().post(new Runnable() {
            public void run() {
                OverlayDisplayAdapter.this.getContext().getContentResolver().registerContentObserver(Global.getUriFor("overlay_display_devices"), true, new ContentObserver(OverlayDisplayAdapter.this.getHandler()) {
                    public void onChange(boolean selfChange) {
                        OverlayDisplayAdapter.this.updateOverlayDisplayDevices();
                    }
                });
                OverlayDisplayAdapter.this.updateOverlayDisplayDevices();
            }
        });
    }

    private void updateOverlayDisplayDevices() {
        synchronized (getSyncRoot()) {
            updateOverlayDisplayDevicesLocked();
        }
    }

    private void updateOverlayDisplayDevicesLocked() {
        String value = Global.getString(getContext().getContentResolver(), "overlay_display_devices");
        if (value == null) {
            value = "";
        }
        if (!value.equals(this.mCurrentOverlaySetting)) {
            this.mCurrentOverlaySetting = value;
            if (!this.mOverlays.isEmpty()) {
                Slog.i(TAG, "Dismissing all overlay display devices.");
                for (OverlayDisplayHandle overlay : this.mOverlays) {
                    overlay.dismissLocked();
                }
                this.mOverlays.clear();
            }
            int count = 0;
            String[] split = value.split(";");
            int i = 0;
            int length = split.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                Matcher displayMatcher = DISPLAY_PATTERN.matcher(split[i2]);
                if (displayMatcher.matches()) {
                    if (count >= 4) {
                        Slog.w(TAG, "Too many overlay display devices specified: " + value);
                        break;
                    }
                    String modeString = displayMatcher.group(1);
                    String flagString = displayMatcher.group(2);
                    ArrayList<OverlayMode> modes = new ArrayList();
                    for (String mode : modeString.split("\\|")) {
                        Matcher modeMatcher = MODE_PATTERN.matcher(mode);
                        if (modeMatcher.matches()) {
                            try {
                                int width = Integer.parseInt(modeMatcher.group(1), 10);
                                int height = Integer.parseInt(modeMatcher.group(2), 10);
                                int densityDpi = Integer.parseInt(modeMatcher.group(3), 10);
                                if (width < 100 || width > 4096 || height < 100 || height > 4096 || densityDpi < 120 || densityDpi > 640) {
                                    Slog.w(TAG, "Ignoring out-of-range overlay display mode: " + mode);
                                } else {
                                    modes.add(new OverlayMode(width, height, densityDpi));
                                }
                            } catch (NumberFormatException e) {
                            }
                        } else {
                            boolean isEmpty = mode.isEmpty();
                        }
                    }
                    if (!modes.isEmpty()) {
                        count++;
                        int number = count;
                        String name = getContext().getResources().getString(17039899, new Object[]{Integer.valueOf(count)});
                        int gravity = chooseOverlayGravity(count);
                        boolean secure = flagString != null ? flagString.contains(",secure") : false;
                        Slog.i(TAG, "Showing overlay display device #" + count + ": name=" + name + ", modes=" + Arrays.toString(modes.toArray()));
                        this.mOverlays.add(new OverlayDisplayHandle(name, modes, gravity, secure, count));
                        i = i2 + 1;
                    }
                }
                Slog.w(TAG, "Malformed overlay display devices setting: " + value);
                i = i2 + 1;
            }
        }
    }

    private static int chooseOverlayGravity(int overlayNumber) {
        switch (overlayNumber) {
            case 1:
                return 51;
            case 2:
                return 85;
            case 3:
                return 53;
            default:
                return 83;
        }
    }
}
