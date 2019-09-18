package com.android.server.display;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.hardware.sidekick.SidekickInternal;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayEventReceiver;
import android.view.SurfaceControl;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class LocalDisplayAdapter extends DisplayAdapter {
    private static final int[] BUILT_IN_DISPLAY_IDS_TO_SCAN = {0, 1};
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static final int DEFAULT_DENSITYDPI = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
    private static final boolean FRONT_FINGERPRINT_GESTURE_NAVIGATION_SUPPORTED = SystemProperties.getBoolean("ro.config.gesture_front_support", false);
    /* access modifiers changed from: private */
    public static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    /* access modifiers changed from: private */
    public static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final String GESTURE_NAVIGATION = "secure_gesture_navigation";
    private static final int PAD_DISPLAY_ID = 100000;
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    private static final String TAG = "LocalDisplayAdapter";
    private static final String UNIQUE_ID_PREFIX = "local:";
    private static final boolean isChinaArea = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    /* access modifiers changed from: private */
    public int defaultNaviMode = 0;
    /* access modifiers changed from: private */
    public int mButtonLightMode = 1;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned = true;
    private final SparseArray<LocalDisplayDevice> mDevices = new SparseArray<>();
    private HotplugDisplayEventReceiver mHotplugReceiver;
    private HwFoldScreenState mHwFoldScreenState;
    /* access modifiers changed from: private */
    public boolean mIsGestureNavEnable = false;
    /* access modifiers changed from: private */
    public ContentResolver mResolver;
    /* access modifiers changed from: private */
    public SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public int mTrikeyNaviMode = -1;

    private static final class DisplayModeRecord {
        public final Display.Mode mMode;

        public DisplayModeRecord(SurfaceControl.PhysicalDisplayInfo phys) {
            this.mMode = DisplayAdapter.createMode(phys.width, phys.height, phys.refreshRate);
        }

        public boolean hasMatchingMode(SurfaceControl.PhysicalDisplayInfo info) {
            return this.mMode.getPhysicalWidth() == info.width && this.mMode.getPhysicalHeight() == info.height && Float.floatToIntBits(this.mMode.getRefreshRate()) == Float.floatToIntBits(info.refreshRate);
        }

        public String toString() {
            return "DisplayModeRecord{mMode=" + this.mMode + "}";
        }
    }

    private final class HotplugDisplayEventReceiver extends DisplayEventReceiver {
        public HotplugDisplayEventReceiver(Looper looper) {
            super(looper, 0);
        }

        public void onHotplug(long timestampNanos, int builtInDisplayId, boolean connected) {
            synchronized (LocalDisplayAdapter.this.getSyncRoot()) {
                if (connected) {
                    try {
                        LocalDisplayAdapter.this.tryConnectDisplayLocked(builtInDisplayId);
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    LocalDisplayAdapter.this.tryDisconnectDisplayLocked(builtInDisplayId);
                }
            }
        }
    }

    private final class LocalDisplayDevice extends DisplayDevice {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private int mActiveColorMode;
        private boolean mActiveColorModeInvalid;
        private int mActiveModeId;
        private boolean mActiveModeInvalid;
        private int mActivePhysIndex;
        /* access modifiers changed from: private */
        public final Light mBacklight;
        private int mBrightness;
        private final int mBuiltInDisplayId;
        private final Light mButtonlight;
        private int mDefaultModeId;
        private SurfaceControl.PhysicalDisplayInfo[] mDisplayInfos;
        private boolean mHavePendingChanges;
        private Display.HdrCapabilities mHdrCapabilities;
        private DisplayDeviceInfo mInfo;
        public boolean mRogChange;
        /* access modifiers changed from: private */
        public boolean mSidekickActive;
        /* access modifiers changed from: private */
        public SidekickInternal mSidekickInternal;
        private int mState;
        private final ArrayList<Integer> mSupportedColorModes;
        private final SparseArray<DisplayModeRecord> mSupportedModes;

        static {
            Class<LocalDisplayAdapter> cls = LocalDisplayAdapter.class;
        }

        public LocalDisplayDevice(LocalDisplayAdapter localDisplayAdapter, IBinder displayToken, int builtInDisplayId, SurfaceControl.PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo, int[] colorModes, int activeColorMode) {
            this(displayToken, builtInDisplayId, physicalDisplayInfos, activeDisplayInfo, colorModes, activeColorMode, null);
        }

        public LocalDisplayDevice(IBinder displayToken, int builtInDisplayId, SurfaceControl.PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo, int[] colorModes, int activeColorMode, HwFoldScreenState foldScreenState) {
            super(LocalDisplayAdapter.this, displayToken, LocalDisplayAdapter.UNIQUE_ID_PREFIX + builtInDisplayId, foldScreenState);
            this.mSupportedModes = new SparseArray<>();
            this.mSupportedColorModes = new ArrayList<>();
            this.mRogChange = false;
            this.mState = 0;
            this.mBrightness = -1;
            this.mBuiltInDisplayId = builtInDisplayId;
            updatePhysicalDisplayInfoLocked(physicalDisplayInfos, activeDisplayInfo, colorModes, activeColorMode);
            updateColorModesLocked(colorModes, activeColorMode);
            this.mSidekickInternal = (SidekickInternal) LocalServices.getService(SidekickInternal.class);
            if (this.mBuiltInDisplayId == 0) {
                LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
                this.mBacklight = lights.getLight(0);
                if (!LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION || LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1) {
                    this.mButtonlight = null;
                } else {
                    this.mButtonlight = lights.getLight(2);
                }
            } else {
                this.mBacklight = null;
                this.mButtonlight = null;
            }
            this.mHdrCapabilities = SurfaceControl.getHdrCapabilities(displayToken);
        }

        public boolean hasStableUniqueId() {
            return true;
        }

        public void updateDesityforRog() {
            this.mHavePendingChanges = true;
            this.mRogChange = true;
        }

        public boolean updatePhysicalDisplayInfoLocked(SurfaceControl.PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo, int[] colorModes, int activeColorMode) {
            this.mDisplayInfos = (SurfaceControl.PhysicalDisplayInfo[]) Arrays.copyOf(physicalDisplayInfos, physicalDisplayInfos.length);
            this.mActivePhysIndex = activeDisplayInfo;
            ArrayList<DisplayModeRecord> records = new ArrayList<>();
            boolean modesAdded = false;
            for (SurfaceControl.PhysicalDisplayInfo info : physicalDisplayInfos) {
                boolean existingMode = false;
                int j = 0;
                while (true) {
                    if (j >= records.size()) {
                        break;
                    } else if (records.get(j).hasMatchingMode(info)) {
                        existingMode = true;
                        break;
                    } else {
                        j++;
                    }
                }
                if (!existingMode) {
                    DisplayModeRecord record = findDisplayModeRecord(info);
                    if (record == null) {
                        record = new DisplayModeRecord(info);
                        modesAdded = true;
                    }
                    records.add(record);
                }
            }
            DisplayModeRecord activeRecord = null;
            int i = 0;
            while (true) {
                if (i >= records.size()) {
                    break;
                }
                DisplayModeRecord record2 = records.get(i);
                if (record2.hasMatchingMode(physicalDisplayInfos[activeDisplayInfo])) {
                    activeRecord = record2;
                    break;
                }
                i++;
            }
            if (!(this.mActiveModeId == 0 || this.mActiveModeId == activeRecord.mMode.getModeId())) {
                this.mActiveModeInvalid = true;
                LocalDisplayAdapter.this.sendTraversalRequestLocked();
            }
            if (!(records.size() != this.mSupportedModes.size() || modesAdded)) {
                return false;
            }
            this.mHavePendingChanges = true;
            this.mSupportedModes.clear();
            Iterator<DisplayModeRecord> it = records.iterator();
            while (it.hasNext()) {
                DisplayModeRecord record3 = it.next();
                this.mSupportedModes.put(record3.mMode.getModeId(), record3);
            }
            if (findDisplayInfoIndexLocked(this.mDefaultModeId) < 0) {
                if (this.mDefaultModeId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Default display mode no longer available, using currently active mode as default.");
                }
                this.mDefaultModeId = activeRecord.mMode.getModeId();
            }
            if (this.mSupportedModes.indexOfKey(this.mActiveModeId) < 0) {
                if (this.mActiveModeId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Active display mode no longer available, reverting to default mode.");
                }
                this.mActiveModeId = this.mDefaultModeId;
                this.mActiveModeInvalid = true;
            }
            LocalDisplayAdapter.this.sendTraversalRequestLocked();
            return true;
        }

        public boolean isFoldable() {
            return (this.mBuiltInDisplayId == 0 || this.mBuiltInDisplayId == LocalDisplayAdapter.PAD_DISPLAY_ID) && HwFoldScreenState.isFoldScreenDevice();
        }

        public Rect getScreenDispRect(int orientation) {
            if (!isFoldable() || this.mHwFoldScreenState == null) {
                return null;
            }
            return this.mHwFoldScreenState.getScreenDispRect(orientation);
        }

        public int getDisplayState() {
            if (!isFoldable() || this.mHwFoldScreenState == null) {
                return 0;
            }
            int state = this.mHwFoldScreenState.getDisplayMode();
            Slog.d(LocalDisplayAdapter.TAG, "getDisplayState: " + state);
            return state;
        }

        public int setDisplayState(int state) {
            Slog.d(LocalDisplayAdapter.TAG, "setDisplayState=" + state);
            if (!isFoldable() || this.mHwFoldScreenState == null) {
                Slog.d(LocalDisplayAdapter.TAG, "setDisplayState: not a foldable device");
                return 0;
            }
            int displayMode = this.mHwFoldScreenState.getDisplayMode();
            return this.mHwFoldScreenState.setDisplayMode(state);
        }

        private boolean updateColorModesLocked(int[] colorModes, int activeColorMode) {
            List<Integer> pendingColorModes = new ArrayList<>();
            if (colorModes == null) {
                return false;
            }
            boolean colorModesAdded = false;
            for (int colorMode : colorModes) {
                if (!this.mSupportedColorModes.contains(Integer.valueOf(colorMode))) {
                    colorModesAdded = true;
                }
                pendingColorModes.add(Integer.valueOf(colorMode));
            }
            if (!(pendingColorModes.size() != this.mSupportedColorModes.size() || colorModesAdded)) {
                return false;
            }
            this.mHavePendingChanges = true;
            this.mSupportedColorModes.clear();
            this.mSupportedColorModes.addAll(pendingColorModes);
            Collections.sort(this.mSupportedColorModes);
            if (!this.mSupportedColorModes.contains(Integer.valueOf(this.mActiveColorMode))) {
                if (this.mActiveColorMode != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Active color mode no longer available, reverting to default mode.");
                    this.mActiveColorMode = 0;
                    this.mActiveColorModeInvalid = true;
                } else if (!this.mSupportedColorModes.isEmpty()) {
                    Slog.e(LocalDisplayAdapter.TAG, "Default and active color mode is no longer available! Reverting to first available mode.");
                    this.mActiveColorMode = this.mSupportedColorModes.get(0).intValue();
                    this.mActiveColorModeInvalid = true;
                } else {
                    Slog.e(LocalDisplayAdapter.TAG, "No color modes available!");
                }
            }
            return true;
        }

        private DisplayModeRecord findDisplayModeRecord(SurfaceControl.PhysicalDisplayInfo info) {
            for (int i = 0; i < this.mSupportedModes.size(); i++) {
                DisplayModeRecord record = this.mSupportedModes.valueAt(i);
                if (record.hasMatchingMode(info)) {
                    return record;
                }
            }
            return null;
        }

        public void applyPendingDisplayDeviceInfoChangesLocked() {
            if (this.mHavePendingChanges) {
                this.mInfo = null;
                this.mHavePendingChanges = false;
            }
        }

        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                SurfaceControl.PhysicalDisplayInfo phys = this.mDisplayInfos[this.mActivePhysIndex];
                this.mInfo = new DisplayDeviceInfo();
                this.mInfo.width = phys.width;
                this.mInfo.height = phys.height;
                this.mInfo.modeId = this.mActiveModeId;
                this.mInfo.defaultModeId = this.mDefaultModeId;
                this.mInfo.supportedModes = new Display.Mode[this.mSupportedModes.size()];
                for (int i = 0; i < this.mSupportedModes.size(); i++) {
                    this.mInfo.supportedModes[i] = this.mSupportedModes.valueAt(i).mMode;
                }
                this.mInfo.colorMode = this.mActiveColorMode;
                this.mInfo.supportedColorModes = new int[this.mSupportedColorModes.size()];
                for (int i2 = 0; i2 < this.mSupportedColorModes.size(); i2++) {
                    this.mInfo.supportedColorModes[i2] = this.mSupportedColorModes.get(i2).intValue();
                }
                this.mInfo.hdrCapabilities = this.mHdrCapabilities;
                this.mInfo.appVsyncOffsetNanos = phys.appVsyncOffsetNanos;
                this.mInfo.presentationDeadlineNanos = phys.presentationDeadlineNanos;
                this.mInfo.state = this.mState;
                this.mInfo.uniqueId = getUniqueId();
                if (phys.secure) {
                    this.mInfo.flags = 12;
                }
                Resources res = LocalDisplayAdapter.this.getOverlayContext().getResources();
                if (this.mBuiltInDisplayId == 0) {
                    this.mInfo.name = res.getString(17039944);
                    DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags = 3 | displayDeviceInfo.flags;
                    if (res.getBoolean(17956992) || (Build.IS_EMULATOR && SystemProperties.getBoolean(LocalDisplayAdapter.PROPERTY_EMULATOR_CIRCULAR, false))) {
                        this.mInfo.flags |= 256;
                    }
                    this.mInfo.displayCutout = DisplayCutout.fromResources(res, SystemProperties.getInt("persist.sys.rog.width", this.mInfo.width), SystemProperties.getInt("persist.sys.rog.height", this.mInfo.height));
                    Slog.v(LocalDisplayAdapter.TAG, "getDisplayDeviceInfoLocked called," + width + "x" + height + ", " + this.mInfo.width + "x" + this.mInfo.height + "displayCutout " + this.mInfo.displayCutout);
                    this.mInfo.type = 1;
                    this.mInfo.densityDpi = (int) ((phys.density * 160.0f) + 0.5f);
                    this.mInfo.xDpi = phys.xDpi;
                    this.mInfo.yDpi = phys.yDpi;
                    this.mInfo.touch = 1;
                } else if (this.mBuiltInDisplayId == LocalDisplayAdapter.PAD_DISPLAY_ID) {
                    this.mInfo.name = "HUAWEI PAD PC Display";
                    this.mInfo.flags |= 2;
                    this.mInfo.type = 2;
                    this.mInfo.touch = 1;
                    this.mInfo.densityDpi = LocalDisplayAdapter.DEFAULT_DENSITYDPI == 0 ? (int) ((phys.density * 160.0f) + 0.5f) : LocalDisplayAdapter.DEFAULT_DENSITYDPI;
                    HwPCUtils.log(LocalDisplayAdapter.TAG, "PAD_DISPLAY_ID densityDpi:" + this.mInfo.densityDpi);
                    this.mInfo.xDpi = phys.xDpi;
                    this.mInfo.yDpi = phys.yDpi;
                    HwPCUtils.log(LocalDisplayAdapter.TAG, "PAD_DISPLAY_ID mInfo.xDpi:" + this.mInfo.xDpi + ",mInfo.yDpi:" + this.mInfo.yDpi);
                } else {
                    this.mInfo.displayCutout = null;
                    this.mInfo.type = 2;
                    this.mInfo.flags |= 64;
                    this.mInfo.name = LocalDisplayAdapter.this.getContext().getResources().getString(17039945);
                    this.mInfo.touch = 2;
                    if (HwPCUtils.enabled()) {
                        this.mInfo.densityDpi = (int) ((phys.density * 160.0f) + 0.5f);
                        HwPCUtils.log(LocalDisplayAdapter.TAG, "densityDpi:" + this.mInfo.densityDpi);
                        this.mInfo.xDpi = (float) this.mInfo.densityDpi;
                        this.mInfo.yDpi = (float) this.mInfo.densityDpi;
                    } else {
                        this.mInfo.setAssumedDensityForExternalDisplay(phys.width, phys.height);
                    }
                    if ("portrait".equals(SystemProperties.get("persist.demo.hdmirotation"))) {
                        this.mInfo.rotation = 3;
                    }
                    if (SystemProperties.getBoolean("persist.demo.hdmirotates", false)) {
                        this.mInfo.flags |= 2;
                    }
                    if (!res.getBoolean(17956988)) {
                        this.mInfo.flags |= 128;
                    }
                    if (res.getBoolean(17956989)) {
                        this.mInfo.flags |= 16;
                    }
                }
            }
            return this.mInfo;
        }

        public Runnable requestDisplayStateLocked(int state, int brightness) {
            int i = state;
            int i2 = brightness;
            boolean z = false;
            boolean stateChanged = this.mState != i;
            if (!(this.mBrightness == i2 || this.mBacklight == null)) {
                z = true;
            }
            boolean brightnessChanged = z;
            if (!stateChanged && !brightnessChanged) {
                return null;
            }
            int displayId = this.mBuiltInDisplayId;
            IBinder token = getDisplayTokenLocked();
            int oldState = this.mState;
            if (stateChanged) {
                this.mState = i;
                updateDeviceInfoLocked();
            }
            if (brightnessChanged) {
                this.mBrightness = i2;
            }
            final int i3 = oldState;
            final int i4 = i;
            final boolean z2 = brightnessChanged;
            final int i5 = i2;
            final int i6 = displayId;
            final IBinder iBinder = token;
            AnonymousClass1 r0 = new Runnable() {
                public void run() {
                    int currentState = i3;
                    if (Display.isSuspendedState(i3) || i3 == 0) {
                        if (!Display.isSuspendedState(i4)) {
                            setDisplayState(i4);
                            currentState = i4;
                        } else if (i4 == 4 || i3 == 4) {
                            setDisplayState(3);
                            currentState = 3;
                        } else if (i4 == 6 || i3 == 6) {
                            setDisplayState(2);
                            currentState = 2;
                        } else {
                            return;
                        }
                    }
                    boolean vrModeChange = false;
                    if ((i4 == 5 || currentState == 5) && currentState != i4) {
                        setVrMode(i4 == 5);
                        vrModeChange = true;
                    }
                    if (z2 || vrModeChange) {
                        setDisplayBrightness(i5);
                    }
                    if (i4 != currentState) {
                        setDisplayState(i4);
                    }
                }

                private void setVrMode(boolean isVrEnabled) {
                    Slog.d(LocalDisplayAdapter.TAG, "setVrMode(id=" + i6 + ", state=" + Display.stateToString(i4) + ")");
                    if (LocalDisplayDevice.this.mBacklight != null) {
                        LocalDisplayDevice.this.mBacklight.setVrMode(isVrEnabled);
                    }
                }

                /* JADX INFO: finally extract failed */
                private void setDisplayState(int state) {
                    if (LocalDisplayDevice.this.mSidekickActive) {
                        Trace.traceBegin(131072, "SidekickInternal#endDisplayControl");
                        try {
                            LocalDisplayDevice.this.mSidekickInternal.endDisplayControl();
                            Trace.traceEnd(131072);
                            boolean unused = LocalDisplayDevice.this.mSidekickActive = false;
                        } catch (Throwable th) {
                            Trace.traceEnd(131072);
                            throw th;
                        }
                    }
                    int mode = LocalDisplayAdapter.getPowerModeForState(state);
                    Trace.traceBegin(131072, "setDisplayState(id=" + i6 + ", state=" + Display.stateToString(state) + ")");
                    try {
                        SurfaceControl.setDisplayPowerMode(iBinder, mode);
                        Trace.traceCounter(131072, "DisplayPowerMode", mode);
                        Trace.traceEnd(131072);
                        if (Display.isSuspendedState(state) && state != 1 && LocalDisplayDevice.this.mSidekickInternal != null && !LocalDisplayDevice.this.mSidekickActive) {
                            Trace.traceBegin(131072, "SidekickInternal#startDisplayControl");
                            try {
                                boolean unused2 = LocalDisplayDevice.this.mSidekickActive = LocalDisplayDevice.this.mSidekickInternal.startDisplayControl(state);
                            } finally {
                                Trace.traceEnd(131072);
                            }
                        }
                    } catch (Throwable th2) {
                        Trace.traceEnd(131072);
                        throw th2;
                    }
                }

                private void setDisplayBrightness(int brightness) {
                    Trace.traceBegin(131072, "setDisplayBrightness(id=" + i6 + ", brightness=" + brightness + ")");
                    try {
                        LocalDisplayDevice.this.mBacklight.setBrightness(brightness);
                        LocalDisplayDevice.this.updateButtonBrightness(brightness);
                        Trace.traceCounter(131072, "ScreenBrightness", brightness);
                    } finally {
                        Trace.traceEnd(131072);
                    }
                }
            };
            return r0;
        }

        /* access modifiers changed from: private */
        public void updateButtonBrightness(int brightness) {
            if (this.mButtonlight != null && LocalDisplayAdapter.this.mDeviceProvisioned) {
                if (LocalDisplayAdapter.this.mTrikeyNaviMode < 0 || LocalDisplayAdapter.this.isGestureNavEnable()) {
                    LocalDisplayAdapter.this.setButtonLightTimeout(false);
                    this.mButtonlight.setBrightness(0);
                    return;
                }
                if (LocalDisplayAdapter.this.mButtonLightMode != 0) {
                    LocalDisplayAdapter.this.setButtonLightTimeout(false);
                } else if (brightness == 0) {
                    LocalDisplayAdapter.this.setButtonLightTimeout(false);
                }
                if (!LocalDisplayAdapter.this.isButtonLightTimeout()) {
                    this.mButtonlight.setBrightness(brightness);
                }
            }
        }

        public void requestDisplayModesLocked(int colorMode, int modeId) {
            if (requestModeLocked(modeId) || requestColorModeLocked(colorMode)) {
                updateDeviceInfoLocked();
            }
        }

        public void onOverlayChangedLocked() {
            updateDeviceInfoLocked();
        }

        public boolean requestModeLocked(int modeId) {
            if (modeId == 0) {
                modeId = this.mDefaultModeId;
            } else if (this.mSupportedModes.indexOfKey(modeId) < 0) {
                Slog.w(LocalDisplayAdapter.TAG, "Requested mode " + modeId + " is not supported by this display, reverting to default display mode.");
                modeId = this.mDefaultModeId;
            }
            int physIndex = findDisplayInfoIndexLocked(modeId);
            if (physIndex < 0) {
                Slog.w(LocalDisplayAdapter.TAG, "Requested mode ID " + modeId + " not available, trying with default mode ID");
                modeId = this.mDefaultModeId;
                physIndex = findDisplayInfoIndexLocked(modeId);
            }
            if (this.mActivePhysIndex == physIndex) {
                return false;
            }
            SurfaceControl.setActiveConfig(getDisplayTokenLocked(), physIndex);
            this.mActivePhysIndex = physIndex;
            this.mActiveModeId = modeId;
            this.mActiveModeInvalid = false;
            return true;
        }

        public boolean requestColorModeLocked(int colorMode) {
            if (this.mActiveColorMode == colorMode) {
                return false;
            }
            if (!this.mSupportedColorModes.contains(Integer.valueOf(colorMode))) {
                Slog.w(LocalDisplayAdapter.TAG, "Unable to find color mode " + colorMode + ", ignoring request.");
                return false;
            }
            SurfaceControl.setActiveColorMode(getDisplayTokenLocked(), colorMode);
            this.mActiveColorMode = colorMode;
            this.mActiveColorModeInvalid = false;
            return true;
        }

        public void dumpLocked(PrintWriter pw) {
            super.dumpLocked(pw);
            pw.println("mBuiltInDisplayId=" + this.mBuiltInDisplayId);
            pw.println("mActivePhysIndex=" + this.mActivePhysIndex);
            pw.println("mActiveModeId=" + this.mActiveModeId);
            pw.println("mActiveColorMode=" + this.mActiveColorMode);
            pw.println("mState=" + Display.stateToString(this.mState));
            pw.println("mBrightness=" + this.mBrightness);
            pw.println("mBacklight=" + this.mBacklight);
            pw.println("mDisplayInfos=");
            for (int i = 0; i < this.mDisplayInfos.length; i++) {
                pw.println("  " + this.mDisplayInfos[i]);
            }
            pw.println("mSupportedModes=");
            for (int i2 = 0; i2 < this.mSupportedModes.size(); i2++) {
                pw.println("  " + this.mSupportedModes.valueAt(i2));
            }
            pw.print("mSupportedColorModes=[");
            for (int i3 = 0; i3 < this.mSupportedColorModes.size(); i3++) {
                if (i3 != 0) {
                    pw.print(", ");
                }
                pw.print(this.mSupportedColorModes.get(i3));
            }
            pw.println("]");
        }

        private int findDisplayInfoIndexLocked(int modeId) {
            DisplayModeRecord record = this.mSupportedModes.get(modeId);
            if (record != null) {
                for (int i = 0; i < this.mDisplayInfos.length; i++) {
                    if (record.hasMatchingMode(this.mDisplayInfos[i])) {
                        return i;
                    }
                }
            }
            return -1;
        }

        private void updateDeviceInfoLocked() {
            this.mInfo = null;
            LocalDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
        }
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            boolean z = true;
            boolean unused = LocalDisplayAdapter.this.mDeviceProvisioned = Settings.Secure.getIntForUser(LocalDisplayAdapter.this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            int unused2 = LocalDisplayAdapter.this.mTrikeyNaviMode = Settings.System.getIntForUser(LocalDisplayAdapter.this.mResolver, "swap_key_position", LocalDisplayAdapter.this.defaultNaviMode, ActivityManager.getCurrentUser());
            int unused3 = LocalDisplayAdapter.this.mButtonLightMode = Settings.System.getIntForUser(LocalDisplayAdapter.this.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            boolean unused4 = LocalDisplayAdapter.this.mIsGestureNavEnable = LocalDisplayAdapter.this.mIsGestureNavEnable = Settings.Secure.getIntForUser(LocalDisplayAdapter.this.mResolver, LocalDisplayAdapter.GESTURE_NAVIGATION, 0, ActivityManager.getCurrentUser()) == 0 ? false : z;
        }

        public void registerContentObserver(int userId) {
            LocalDisplayAdapter.this.mResolver.registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, this, userId);
            LocalDisplayAdapter.this.mResolver.registerContentObserver(Settings.System.getUriFor("swap_key_position"), false, this, userId);
            LocalDisplayAdapter.this.mResolver.registerContentObserver(Settings.System.getUriFor("button_light_mode"), false, this, userId);
            LocalDisplayAdapter.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(LocalDisplayAdapter.GESTURE_NAVIGATION), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            boolean unused = LocalDisplayAdapter.this.mDeviceProvisioned = Settings.Secure.getIntForUser(LocalDisplayAdapter.this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            int unused2 = LocalDisplayAdapter.this.mTrikeyNaviMode = Settings.System.getIntForUser(LocalDisplayAdapter.this.mResolver, "swap_key_position", LocalDisplayAdapter.this.defaultNaviMode, ActivityManager.getCurrentUser());
            int unused3 = LocalDisplayAdapter.this.mButtonLightMode = Settings.System.getIntForUser(LocalDisplayAdapter.this.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            LocalDisplayAdapter localDisplayAdapter = LocalDisplayAdapter.this;
            if (Settings.Secure.getIntForUser(LocalDisplayAdapter.this.mResolver, LocalDisplayAdapter.GESTURE_NAVIGATION, 0, ActivityManager.getCurrentUser()) == 0) {
                z = false;
            }
            boolean unused4 = localDisplayAdapter.mIsGestureNavEnable = z;
            Slog.i(LocalDisplayAdapter.TAG, "mTrikeyNaviMode:" + LocalDisplayAdapter.this.mTrikeyNaviMode + " mButtonLightMode:" + LocalDisplayAdapter.this.mButtonLightMode + " mIsGestureNavEnable:" + LocalDisplayAdapter.this.mIsGestureNavEnable);
        }
    }

    private class UserSwtichReceiver extends BroadcastReceiver {
        private UserSwtichReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !"android.intent.action.USER_SWITCHED".equals(intent.getAction()))) {
                int newUserId = intent.getIntExtra("android.intent.extra.user_handle", UserHandle.myUserId());
                Slog.i(LocalDisplayAdapter.TAG, "UserSwtichReceiver:" + newUserId);
                if (LocalDisplayAdapter.this.mSettingsObserver != null) {
                    LocalDisplayAdapter.this.mSettingsObserver.registerContentObserver(newUserId);
                    LocalDisplayAdapter.this.mSettingsObserver.onChange(true);
                }
            }
        }
    }

    public LocalDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener) {
        super(syncRoot, context, handler, listener, TAG);
    }

    public void registerLocked() {
        super.registerLocked();
        this.mHotplugReceiver = new HotplugDisplayEventReceiver(getHandler().getLooper());
        for (int builtInDisplayId : BUILT_IN_DISPLAY_IDS_TO_SCAN) {
            tryConnectDisplayLocked(builtInDisplayId);
        }
        if (HwPCUtils.enabledInPad()) {
            HwPCUtils.log(TAG, "tryConnectPadVirtualDisplayLocked");
            tryConnectDisplayLocked(PAD_DISPLAY_ID);
        }
    }

    /* access modifiers changed from: private */
    public void tryConnectDisplayLocked(int builtInDisplayId) {
        LocalDisplayDevice device;
        int i = builtInDisplayId;
        IBinder displayToken = SurfaceControl.getBuiltInDisplay(builtInDisplayId);
        if (i == PAD_DISPLAY_ID) {
            displayToken = SurfaceControl.getBuiltInDisplay(0);
        }
        IBinder displayToken2 = displayToken;
        if (displayToken2 != null) {
            SurfaceControl.PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken2);
            if (configs == null) {
                Slog.w(TAG, "No valid configs found for display device " + i);
                return;
            }
            int activeConfig = SurfaceControl.getActiveConfig(displayToken2);
            if (activeConfig < 0) {
                Slog.w(TAG, "No active config found for display device " + i);
                return;
            }
            int activeColorMode = SurfaceControl.getActiveColorMode(displayToken2);
            if (activeColorMode < 0) {
                Slog.w(TAG, "Unable to get active color mode for display device " + i);
                activeColorMode = -1;
            }
            int activeColorMode2 = activeColorMode;
            int[] colorModes = SurfaceControl.getDisplayColorModes(displayToken2);
            LocalDisplayDevice device2 = this.mDevices.get(i);
            if (device2 == null) {
                if (i != 0 && i != PAD_DISPLAY_ID) {
                    device = new LocalDisplayDevice(this, displayToken2, i, configs, activeConfig, colorModes, activeColorMode2);
                } else if (this.mHwFoldScreenState != null || !HwFoldScreenState.isFoldScreenDevice()) {
                    device = new LocalDisplayDevice(this, displayToken2, i, configs, activeConfig, colorModes, activeColorMode2);
                } else {
                    this.mHwFoldScreenState = HwServiceFactory.getHwFoldScreenState(getContext());
                    Slog.d(TAG, "new LocalDisplayDevice() builtInDisplayId=" + i);
                    LocalDisplayDevice localDisplayDevice = device2;
                    device = new LocalDisplayDevice(displayToken2, i, configs, activeConfig, colorModes, activeColorMode2, this.mHwFoldScreenState);
                }
                this.mDevices.put(i, device);
                sendDisplayDeviceEventLocked(device, 1);
            } else {
                LocalDisplayDevice device3 = device2;
                if (device3.updatePhysicalDisplayInfoLocked(configs, activeConfig, colorModes, activeColorMode2)) {
                    sendDisplayDeviceEventLocked(device3, 2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void tryDisconnectDisplayLocked(int builtInDisplayId) {
        LocalDisplayDevice device = this.mDevices.get(builtInDisplayId);
        if (device != null) {
            this.mDevices.remove(builtInDisplayId);
            sendDisplayDeviceEventLocked(device, 3);
        }
    }

    static int getPowerModeForState(int state) {
        if (state == 1) {
            return 0;
        }
        if (state == 6) {
            return 4;
        }
        switch (state) {
            case 3:
                return 1;
            case 4:
                return 3;
            default:
                return 2;
        }
    }

    public void registerContentObserver(Context context, Handler handler) {
        if (context != null && FRONT_FINGERPRINT_NAVIGATION && FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            try {
                this.mResolver = context.getContentResolver();
                this.mSettingsObserver = new SettingsObserver(handler);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.USER_SWITCHED");
                context.registerReceiver(new UserSwtichReceiver(), intentFilter);
            } catch (Exception exp) {
                Log.e(TAG, "registerContentObserver:" + exp.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isButtonLightTimeout() {
        return SystemProperties.getBoolean("sys.button.light.timeout", false);
    }

    /* access modifiers changed from: private */
    public void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }

    /* access modifiers changed from: private */
    public boolean isGestureNavEnable() {
        return FRONT_FINGERPRINT_GESTURE_NAVIGATION_SUPPORTED && this.mIsGestureNavEnable;
    }

    /* access modifiers changed from: package-private */
    public Context getOverlayContext() {
        return ActivityThread.currentActivityThread().getSystemUiContext();
    }

    public void pcDisplayChangeService(boolean connected) {
        if (HwPCUtils.enabledInPad()) {
            Slog.w(TAG, "pcDisplayChangeService connected = " + connected);
            synchronized (getSyncRoot()) {
                if (connected) {
                    try {
                        if (this.mDevices.get(PAD_DISPLAY_ID) == null) {
                            Slog.w(TAG, "pcDisplayChangeService tryDisconnectDisplayLocked");
                            tryConnectDisplayLocked(PAD_DISPLAY_ID);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    Slog.w(TAG, "pcDisplayChangeService tryDisconnectDisplayLocked");
                    tryDisconnectDisplayLocked(PAD_DISPLAY_ID);
                }
            }
        }
    }
}
