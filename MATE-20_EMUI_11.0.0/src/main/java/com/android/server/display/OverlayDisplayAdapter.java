package com.android.server.display;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Slog;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.OverlayDisplayWindow;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* access modifiers changed from: package-private */
public final class OverlayDisplayAdapter extends DisplayAdapter {
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
    private final ArrayList<OverlayDisplayHandle> mOverlays = new ArrayList<>();
    private final Handler mUiHandler;

    public OverlayDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, Handler uiHandler) {
        super(syncRoot, context, handler, listener, TAG);
        this.mUiHandler = uiHandler;
    }

    @Override // com.android.server.display.DisplayAdapter
    public void dumpLocked(PrintWriter pw) {
        super.dumpLocked(pw);
        pw.println("mCurrentOverlaySetting=" + this.mCurrentOverlaySetting);
        pw.println("mOverlays: size=" + this.mOverlays.size());
        Iterator<OverlayDisplayHandle> it = this.mOverlays.iterator();
        while (it.hasNext()) {
            it.next().dumpLocked(pw);
        }
    }

    @Override // com.android.server.display.DisplayAdapter
    public void registerLocked() {
        super.registerLocked();
        getHandler().post(new Runnable() {
            /* class com.android.server.display.OverlayDisplayAdapter.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                OverlayDisplayAdapter.this.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("overlay_display_devices"), true, new ContentObserver(OverlayDisplayAdapter.this.getHandler()) {
                    /* class com.android.server.display.OverlayDisplayAdapter.AnonymousClass1.AnonymousClass1 */

                    @Override // android.database.ContentObserver
                    public void onChange(boolean selfChange) {
                        OverlayDisplayAdapter.this.updateOverlayDisplayDevices();
                    }
                });
                OverlayDisplayAdapter.this.updateOverlayDisplayDevices();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateOverlayDisplayDevices() {
        synchronized (getSyncRoot()) {
            updateOverlayDisplayDevicesLocked();
        }
    }

    private void updateOverlayDisplayDevicesLocked() {
        String value;
        String[] strArr;
        Matcher displayMatcher;
        String[] strArr2;
        String value2 = Settings.Global.getString(getContext().getContentResolver(), "overlay_display_devices");
        if (value2 == null) {
            value = "";
        } else {
            value = value2;
        }
        if (!value.equals(this.mCurrentOverlaySetting)) {
            this.mCurrentOverlaySetting = value;
            if (!this.mOverlays.isEmpty()) {
                Slog.i(TAG, "Dismissing all overlay display devices.");
                Iterator<OverlayDisplayHandle> it = this.mOverlays.iterator();
                while (it.hasNext()) {
                    it.next().dismissLocked();
                }
                this.mOverlays.clear();
            }
            String[] split = value.split(";");
            int length = split.length;
            int count = 0;
            int i = 0;
            while (i < length) {
                Matcher displayMatcher2 = DISPLAY_PATTERN.matcher(split[i]);
                if (!displayMatcher2.matches()) {
                    strArr = split;
                } else if (count >= 4) {
                    Slog.w(TAG, "Too many overlay display devices specified: " + value);
                    return;
                } else {
                    String modeString = displayMatcher2.group(1);
                    String flagString = displayMatcher2.group(2);
                    ArrayList<OverlayMode> modes = new ArrayList<>();
                    String[] split2 = modeString.split("\\|");
                    int length2 = split2.length;
                    int i2 = 0;
                    while (i2 < length2) {
                        String mode = split2[i2];
                        Matcher modeMatcher = MODE_PATTERN.matcher(mode);
                        if (modeMatcher.matches()) {
                            displayMatcher = displayMatcher2;
                            try {
                                int width = Integer.parseInt(modeMatcher.group(1), 10);
                                strArr2 = split;
                                try {
                                    int height = Integer.parseInt(modeMatcher.group(2), 10);
                                    try {
                                        int densityDpi = Integer.parseInt(modeMatcher.group(3), 10);
                                        if (width < 100 || width > 4096 || height < 100 || height > 4096 || densityDpi < 120 || densityDpi > 640) {
                                            Slog.w(TAG, "Ignoring out-of-range overlay display mode: " + mode);
                                        } else {
                                            modes.add(new OverlayMode(width, height, densityDpi));
                                        }
                                    } catch (NumberFormatException e) {
                                    }
                                } catch (NumberFormatException e2) {
                                }
                            } catch (NumberFormatException e3) {
                                strArr2 = split;
                            }
                        } else {
                            displayMatcher = displayMatcher2;
                            strArr2 = split;
                            if (mode.isEmpty()) {
                            }
                        }
                        i2++;
                        split = strArr2;
                        length2 = length2;
                        modeString = modeString;
                        displayMatcher2 = displayMatcher;
                    }
                    strArr = split;
                    if (!modes.isEmpty()) {
                        int count2 = count + 1;
                        boolean secure = true;
                        String name = getContext().getResources().getString(17039995, Integer.valueOf(count2));
                        int gravity = chooseOverlayGravity(count2);
                        if (flagString == null || !flagString.contains(",secure")) {
                            secure = false;
                        }
                        Slog.i(TAG, "Showing overlay display device #" + count2 + ": name=" + name + ", modes=" + Arrays.toString(modes.toArray()));
                        this.mOverlays.add(new OverlayDisplayHandle(name, modes, gravity, secure, count2));
                        count = count2;
                        i++;
                        split = strArr;
                    }
                }
                Slog.w(TAG, "Malformed overlay display devices setting: " + value);
                i++;
                split = strArr;
            }
        }
    }

    private static int chooseOverlayGravity(int overlayNumber) {
        if (overlayNumber == 1) {
            return 51;
        }
        if (overlayNumber == 2) {
            return 85;
        }
        if (overlayNumber != 3) {
            return 83;
        }
        return 53;
    }

    private abstract class OverlayDisplayDevice extends DisplayDevice {
        private int mActiveMode;
        private final int mDefaultMode;
        private final long mDisplayPresentationDeadlineNanos;
        private DisplayDeviceInfo mInfo;
        private final Display.Mode[] mModes;
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
            this.mModes = new Display.Mode[modes.size()];
            for (int i = 0; i < modes.size(); i++) {
                OverlayMode mode = modes.get(i);
                this.mModes[i] = DisplayAdapter.createMode(mode.mWidth, mode.mHeight, refreshRate);
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
            SurfaceControl.destroyDisplay(getDisplayTokenLocked());
        }

        @Override // com.android.server.display.DisplayDevice
        public boolean hasStableUniqueId() {
            return false;
        }

        @Override // com.android.server.display.DisplayDevice
        public void performTraversalLocked(SurfaceControl.Transaction t) {
            SurfaceTexture surfaceTexture = this.mSurfaceTexture;
            if (surfaceTexture != null) {
                if (this.mSurface == null) {
                    this.mSurface = new Surface(surfaceTexture);
                }
                setSurfaceLocked(t, this.mSurface);
            }
        }

        public void setStateLocked(int state) {
            this.mState = state;
            this.mInfo = null;
        }

        @Override // com.android.server.display.DisplayDevice
        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                Display.Mode[] modeArr = this.mModes;
                int i = this.mActiveMode;
                Display.Mode mode = modeArr[i];
                OverlayMode rawMode = this.mRawModes.get(i);
                this.mInfo = new DisplayDeviceInfo();
                DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                displayDeviceInfo.name = this.mName;
                displayDeviceInfo.uniqueId = getUniqueId();
                this.mInfo.width = mode.getPhysicalWidth();
                this.mInfo.height = mode.getPhysicalHeight();
                this.mInfo.modeId = mode.getModeId();
                this.mInfo.defaultModeId = this.mModes[0].getModeId();
                DisplayDeviceInfo displayDeviceInfo2 = this.mInfo;
                displayDeviceInfo2.supportedModes = this.mModes;
                displayDeviceInfo2.densityDpi = rawMode.mDensityDpi;
                this.mInfo.xDpi = (float) rawMode.mDensityDpi;
                this.mInfo.yDpi = (float) rawMode.mDensityDpi;
                DisplayDeviceInfo displayDeviceInfo3 = this.mInfo;
                displayDeviceInfo3.presentationDeadlineNanos = this.mDisplayPresentationDeadlineNanos + (1000000000 / ((long) ((int) this.mRefreshRate)));
                displayDeviceInfo3.flags = 64;
                if (this.mSecure) {
                    displayDeviceInfo3.flags |= 4;
                }
                DisplayDeviceInfo displayDeviceInfo4 = this.mInfo;
                displayDeviceInfo4.type = 4;
                displayDeviceInfo4.touch = 3;
                displayDeviceInfo4.state = this.mState;
            }
            return this.mInfo;
        }

        @Override // com.android.server.display.DisplayDevice
        public void setAllowedDisplayModesLocked(int[] modes) {
            int id;
            if (modes.length > 0) {
                id = modes[0];
            } else {
                id = 0;
            }
            int index = -1;
            if (id == 0) {
                index = 0;
            } else {
                int i = 0;
                while (true) {
                    Display.Mode[] modeArr = this.mModes;
                    if (i >= modeArr.length) {
                        break;
                    } else if (modeArr[i].getModeId() == id) {
                        index = i;
                        break;
                    } else {
                        i++;
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
                OverlayDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
                onModeChangedLocked(index);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class OverlayDisplayHandle implements OverlayDisplayWindow.Listener {
        private static final int DEFAULT_MODE_INDEX = 0;
        private int mActiveMode;
        private OverlayDisplayDevice mDevice;
        private final Runnable mDismissRunnable = new Runnable() {
            /* class com.android.server.display.OverlayDisplayAdapter.OverlayDisplayHandle.AnonymousClass3 */

            @Override // java.lang.Runnable
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
            /* class com.android.server.display.OverlayDisplayAdapter.OverlayDisplayHandle.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                    if (OverlayDisplayHandle.this.mWindow != null) {
                        OverlayMode mode = (OverlayMode) OverlayDisplayHandle.this.mModes.get(OverlayDisplayHandle.this.mActiveMode);
                        OverlayDisplayHandle.this.mWindow.resize(mode.mWidth, mode.mHeight, mode.mDensityDpi);
                    }
                }
            }
        };
        private final boolean mSecure;
        private final Runnable mShowRunnable = new Runnable() {
            /* class com.android.server.display.OverlayDisplayAdapter.OverlayDisplayHandle.AnonymousClass2 */

            @Override // java.lang.Runnable
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onActiveModeChangedLocked(int index) {
            OverlayDisplayAdapter.this.mUiHandler.removeCallbacks(this.mResizeRunnable);
            this.mActiveMode = index;
            if (this.mWindow != null) {
                OverlayDisplayAdapter.this.mUiHandler.post(this.mResizeRunnable);
            }
        }

        @Override // com.android.server.display.OverlayDisplayWindow.Listener
        public void onWindowCreated(SurfaceTexture surfaceTexture, float refreshRate, long presentationDeadlineNanos, int state) {
            synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                this.mDevice = new OverlayDisplayDevice(SurfaceControl.createDisplay(this.mName, this.mSecure), this.mName, this.mModes, this.mActiveMode, 0, refreshRate, presentationDeadlineNanos, this.mSecure, state, surfaceTexture, this.mNumber) {
                    /* class com.android.server.display.OverlayDisplayAdapter.OverlayDisplayHandle.AnonymousClass1 */

                    @Override // com.android.server.display.OverlayDisplayAdapter.OverlayDisplayDevice
                    public void onModeChangedLocked(int index) {
                        OverlayDisplayHandle.this.onActiveModeChangedLocked(index);
                    }
                };
                OverlayDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, 1);
            }
        }

        @Override // com.android.server.display.OverlayDisplayWindow.Listener
        public void onWindowDestroyed() {
            synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                if (this.mDevice != null) {
                    this.mDevice.destroyLocked();
                    OverlayDisplayAdapter.this.sendDisplayDeviceEventLocked(this.mDevice, 3);
                }
            }
        }

        @Override // com.android.server.display.OverlayDisplayWindow.Listener
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
            StringBuilder sb = new StringBuilder();
            sb.append("    mModes=");
            sb.append(Arrays.toString(this.mModes.toArray()));
            pw.println(sb.toString());
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

        public String toString() {
            return "{width=" + this.mWidth + ", height=" + this.mHeight + ", densityDpi=" + this.mDensityDpi + "}";
        }
    }
}
