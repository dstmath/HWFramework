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
    private String mCurrentOverlaySetting = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    private final ArrayList<OverlayDisplayHandle> mOverlays = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Handler mUiHandler;

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
        final /* synthetic */ OverlayDisplayAdapter this$0;

        public abstract void onModeChangedLocked(int i);

        /* JADX WARNING: Illegal instructions before constructor call */
        public OverlayDisplayDevice(OverlayDisplayAdapter overlayDisplayAdapter, IBinder displayToken, String name, List<OverlayMode> modes, int activeMode, int defaultMode, float refreshRate, long presentationDeadlineNanos, boolean secure, int state, SurfaceTexture surfaceTexture, int number) {
            super(r1, displayToken, OverlayDisplayAdapter.UNIQUE_ID_PREFIX + number);
            OverlayDisplayAdapter overlayDisplayAdapter2 = overlayDisplayAdapter;
            List<OverlayMode> list = modes;
            float f = refreshRate;
            this.this$0 = overlayDisplayAdapter2;
            this.mName = name;
            this.mRefreshRate = f;
            this.mDisplayPresentationDeadlineNanos = presentationDeadlineNanos;
            this.mSecure = secure;
            this.mState = state;
            this.mSurfaceTexture = surfaceTexture;
            this.mRawModes = list;
            this.mModes = new Display.Mode[modes.size()];
            for (int i = 0; i < modes.size(); i++) {
                OverlayMode mode = list.get(i);
                this.mModes[i] = DisplayAdapter.createMode(mode.mWidth, mode.mHeight, f);
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

        public void performTraversalLocked(SurfaceControl.Transaction t) {
            if (this.mSurfaceTexture != null) {
                if (this.mSurface == null) {
                    this.mSurface = new Surface(this.mSurfaceTexture);
                }
                setSurfaceLocked(t, this.mSurface);
            }
        }

        public void setStateLocked(int state) {
            this.mState = state;
            this.mInfo = null;
        }

        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                Display.Mode mode = this.mModes[this.mActiveMode];
                OverlayMode rawMode = this.mRawModes.get(this.mActiveMode);
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
                    this.mInfo.flags |= 4;
                }
                this.mInfo.type = 4;
                this.mInfo.touch = 0;
                this.mInfo.state = this.mState;
            }
            return this.mInfo;
        }

        public void requestDisplayModesLocked(int color, int id) {
            int index = -1;
            if (id == 0) {
                index = 0;
            } else {
                int i = 0;
                while (true) {
                    if (i >= this.mModes.length) {
                        break;
                    } else if (this.mModes[i].getModeId() == id) {
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
                this.this$0.sendDisplayDeviceEventLocked(this, 2);
                onModeChangedLocked(index);
            }
        }
    }

    private final class OverlayDisplayHandle implements OverlayDisplayWindow.Listener {
        private static final int DEFAULT_MODE_INDEX = 0;
        /* access modifiers changed from: private */
        public int mActiveMode;
        private OverlayDisplayDevice mDevice;
        private final Runnable mDismissRunnable = new Runnable() {
            public void run() {
                OverlayDisplayWindow window;
                synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                    window = OverlayDisplayHandle.this.mWindow;
                    OverlayDisplayWindow unused = OverlayDisplayHandle.this.mWindow = null;
                }
                if (window != null) {
                    window.dismiss();
                }
            }
        };
        /* access modifiers changed from: private */
        public final int mGravity;
        /* access modifiers changed from: private */
        public final List<OverlayMode> mModes;
        /* access modifiers changed from: private */
        public final String mName;
        private final int mNumber;
        private final Runnable mResizeRunnable = new Runnable() {
            public void run() {
                synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                    if (OverlayDisplayHandle.this.mWindow != null) {
                        OverlayMode mode = (OverlayMode) OverlayDisplayHandle.this.mModes.get(OverlayDisplayHandle.this.mActiveMode);
                        OverlayDisplayWindow window = OverlayDisplayHandle.this.mWindow;
                        window.resize(mode.mWidth, mode.mHeight, mode.mDensityDpi);
                    }
                }
            }
        };
        /* access modifiers changed from: private */
        public final boolean mSecure;
        private final Runnable mShowRunnable = new Runnable() {
            public void run() {
                OverlayMode mode = (OverlayMode) OverlayDisplayHandle.this.mModes.get(OverlayDisplayHandle.this.mActiveMode);
                OverlayDisplayWindow window = new OverlayDisplayWindow(OverlayDisplayAdapter.this.getContext(), OverlayDisplayHandle.this.mName, mode.mWidth, mode.mHeight, mode.mDensityDpi, OverlayDisplayHandle.this.mGravity, OverlayDisplayHandle.this.mSecure, OverlayDisplayHandle.this);
                window.show();
                synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                    OverlayDisplayWindow unused = OverlayDisplayHandle.this.mWindow = window;
                }
            }
        };
        /* access modifiers changed from: private */
        public OverlayDisplayWindow mWindow;

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
        public void onActiveModeChangedLocked(int index) {
            OverlayDisplayAdapter.this.mUiHandler.removeCallbacks(this.mResizeRunnable);
            this.mActiveMode = index;
            if (this.mWindow != null) {
                OverlayDisplayAdapter.this.mUiHandler.post(this.mResizeRunnable);
            }
        }

        public void onWindowCreated(SurfaceTexture surfaceTexture, float refreshRate, long presentationDeadlineNanos, int state) {
            synchronized (OverlayDisplayAdapter.this.getSyncRoot()) {
                AnonymousClass1 r1 = new OverlayDisplayDevice(this, SurfaceControl.createDisplay(this.mName, this.mSecure), this.mName, this.mModes, this.mActiveMode, 0, refreshRate, presentationDeadlineNanos, this.mSecure, state, surfaceTexture, this.mNumber) {
                    final /* synthetic */ OverlayDisplayHandle this$1;

                    {
                        OverlayDisplayHandle overlayDisplayHandle = this$1;
                        this.this$1 = overlayDisplayHandle;
                    }

                    public void onModeChangedLocked(int index) {
                        this.this$1.onActiveModeChangedLocked(index);
                    }
                };
                this.mDevice = r1;
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
                DumpUtils.dumpAsync(OverlayDisplayAdapter.this.mUiHandler, this.mWindow, ipw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 200);
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

    public OverlayDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, Handler uiHandler) {
        super(syncRoot, context, handler, listener, TAG);
        this.mUiHandler = uiHandler;
    }

    public void dumpLocked(PrintWriter pw) {
        super.dumpLocked(pw);
        pw.println("mCurrentOverlaySetting=" + this.mCurrentOverlaySetting);
        pw.println("mOverlays: size=" + this.mOverlays.size());
        Iterator<OverlayDisplayHandle> it = this.mOverlays.iterator();
        while (it.hasNext()) {
            it.next().dumpLocked(pw);
        }
    }

    public void registerLocked() {
        super.registerLocked();
        getHandler().post(new Runnable() {
            public void run() {
                OverlayDisplayAdapter.this.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("overlay_display_devices"), true, new ContentObserver(OverlayDisplayAdapter.this.getHandler()) {
                    public void onChange(boolean selfChange) {
                        OverlayDisplayAdapter.this.updateOverlayDisplayDevices();
                    }
                });
                OverlayDisplayAdapter.this.updateOverlayDisplayDevices();
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateOverlayDisplayDevices() {
        synchronized (getSyncRoot()) {
            updateOverlayDisplayDevicesLocked();
        }
    }

    private void updateOverlayDisplayDevicesLocked() {
        String[] strArr;
        String[] strArr2;
        String modeString;
        String value = Settings.Global.getString(getContext().getContentResolver(), "overlay_display_devices");
        if (value == null) {
            value = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        String value2 = value;
        if (!value2.equals(this.mCurrentOverlaySetting)) {
            this.mCurrentOverlaySetting = value2;
            if (!this.mOverlays.isEmpty()) {
                Slog.i(TAG, "Dismissing all overlay display devices.");
                Iterator<OverlayDisplayHandle> it = this.mOverlays.iterator();
                while (it.hasNext()) {
                    it.next().dismissLocked();
                }
                this.mOverlays.clear();
            }
            String[] split = value2.split(";");
            int length = split.length;
            int count = 0;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Matcher displayMatcher = DISPLAY_PATTERN.matcher(split[i]);
                if (!displayMatcher.matches()) {
                    strArr = split;
                } else if (count >= 4) {
                    Slog.w(TAG, "Too many overlay display devices specified: " + value2);
                    break;
                } else {
                    String modeString2 = displayMatcher.group(1);
                    String flagString = displayMatcher.group(2);
                    ArrayList<OverlayMode> modes = new ArrayList<>();
                    String[] split2 = modeString2.split("\\|");
                    int length2 = split2.length;
                    int i2 = 0;
                    while (i2 < length2) {
                        String mode = split2[i2];
                        String[] strArr3 = split2;
                        Matcher modeMatcher = MODE_PATTERN.matcher(mode);
                        if (modeMatcher.matches()) {
                            modeString = modeString2;
                            try {
                                int width = Integer.parseInt(modeMatcher.group(1), 10);
                                strArr2 = split;
                                try {
                                    int height = Integer.parseInt(modeMatcher.group(2), 10);
                                    Matcher matcher = modeMatcher;
                                    try {
                                        int densityDpi = Integer.parseInt(modeMatcher.group(3), 10);
                                        if (width < 100 || width > 4096 || height < 100 || height > 4096 || densityDpi < 120 || densityDpi > 640) {
                                            int i3 = width;
                                            StringBuilder sb = new StringBuilder();
                                            int i4 = densityDpi;
                                            sb.append("Ignoring out-of-range overlay display mode: ");
                                            sb.append(mode);
                                            Slog.w(TAG, sb.toString());
                                        } else {
                                            modes.add(new OverlayMode(width, height, densityDpi));
                                        }
                                    } catch (NumberFormatException e) {
                                    }
                                } catch (NumberFormatException e2) {
                                    Matcher matcher2 = modeMatcher;
                                }
                            } catch (NumberFormatException e3) {
                                Matcher matcher3 = modeMatcher;
                                strArr2 = split;
                            }
                        } else {
                            Matcher matcher4 = modeMatcher;
                            modeString = modeString2;
                            strArr2 = split;
                            if (mode.isEmpty()) {
                            }
                        }
                        i2++;
                        split2 = strArr3;
                        modeString2 = modeString;
                        split = strArr2;
                    }
                    String modeString3 = modeString2;
                    strArr = split;
                    if (!modes.isEmpty()) {
                        int count2 = count + 1;
                        int number = count2;
                        String name = getContext().getResources().getString(17039946, new Object[]{Integer.valueOf(number)});
                        int gravity = chooseOverlayGravity(number);
                        boolean secure = flagString != null && flagString.contains(",secure");
                        Slog.i(TAG, "Showing overlay display device #" + number + ": name=" + name + ", modes=" + Arrays.toString(modes.toArray()));
                        OverlayDisplayHandle overlayDisplayHandle = r1;
                        int count3 = count2;
                        ArrayList<OverlayDisplayHandle> arrayList = this.mOverlays;
                        ArrayList<OverlayMode> arrayList2 = modes;
                        String str = flagString;
                        String str2 = modeString3;
                        String modeString4 = name;
                        OverlayDisplayHandle overlayDisplayHandle2 = new OverlayDisplayHandle(name, modes, gravity, secure, number);
                        arrayList.add(overlayDisplayHandle);
                        count = count3;
                        i++;
                        split = strArr;
                    }
                }
                Slog.w(TAG, "Malformed overlay display devices setting: " + value2);
                i++;
                split = strArr;
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
