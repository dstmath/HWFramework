package com.android.server.display;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.Display.HdrCapabilities;
import android.view.Display.Mode;
import android.view.DisplayEventReceiver;
import android.view.SurfaceControl;
import android.view.SurfaceControl.PhysicalDisplayInfo;
import com.android.server.LocalServices;
import com.android.server.display.DisplayAdapter.Listener;
import com.android.server.display.DisplayManagerService.SyncRoot;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class LocalDisplayAdapter extends DisplayAdapter {
    private static final int[] BUILT_IN_DISPLAY_IDS_TO_SCAN = new int[]{0, 1};
    private static final boolean DEBUG = false;
    private static final int DEFAULT_DENSITYDPI = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final int PAD_DISPLAY_ID = 100000;
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    private static final String TAG = "LocalDisplayAdapter";
    private static final String UNIQUE_ID_PREFIX = "local:";
    private static final boolean isChinaArea = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private int defaultNaviMode = 0;
    private int mButtonLightMode = 1;
    private boolean mDeviceProvisioned = true;
    private final SparseArray<LocalDisplayDevice> mDevices = new SparseArray();
    private HotplugDisplayEventReceiver mHotplugReceiver;
    private ContentResolver mResolver;
    private SettingsObserver mSettingsObserver;
    private int mTrikeyNaviMode = -1;

    private static final class DisplayModeRecord {
        public final Mode mMode;

        public DisplayModeRecord(PhysicalDisplayInfo phys) {
            this.mMode = DisplayAdapter.createMode(phys.width, phys.height, phys.refreshRate);
        }

        public boolean hasMatchingMode(PhysicalDisplayInfo info) {
            int modeRefreshRate = Float.floatToIntBits(this.mMode.getRefreshRate());
            int displayInfoRefreshRate = Float.floatToIntBits(info.refreshRate);
            if (this.mMode.getPhysicalWidth() == info.width && this.mMode.getPhysicalHeight() == info.height && modeRefreshRate == displayInfoRefreshRate) {
                return true;
            }
            return false;
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
                    LocalDisplayAdapter.this.tryConnectDisplayLocked(builtInDisplayId);
                } else {
                    LocalDisplayAdapter.this.tryDisconnectDisplayLocked(builtInDisplayId);
                }
            }
        }
    }

    private final class LocalDisplayDevice extends DisplayDevice {
        static final /* synthetic */ boolean -assertionsDisabled = (LocalDisplayDevice.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;
        private int mActiveColorMode;
        private boolean mActiveColorModeInvalid;
        private int mActiveModeId;
        private boolean mActiveModeInvalid;
        private int mActivePhysIndex;
        private final Light mBacklight;
        private int mBrightness = -1;
        private final int mBuiltInDisplayId;
        private final Light mButtonlight;
        private int mDefaultModeId;
        private PhysicalDisplayInfo[] mDisplayInfos;
        private boolean mHavePendingChanges;
        private HdrCapabilities mHdrCapabilities;
        private DisplayDeviceInfo mInfo;
        private int mState = 0;
        private final ArrayList<Integer> mSupportedColorModes = new ArrayList();
        private final SparseArray<DisplayModeRecord> mSupportedModes = new SparseArray();

        public LocalDisplayDevice(IBinder displayToken, int builtInDisplayId, PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo, int[] colorModes, int activeColorMode) {
            super(LocalDisplayAdapter.this, displayToken, LocalDisplayAdapter.UNIQUE_ID_PREFIX + builtInDisplayId);
            this.mBuiltInDisplayId = builtInDisplayId;
            updatePhysicalDisplayInfoLocked(physicalDisplayInfos, activeDisplayInfo, colorModes, activeColorMode);
            updateColorModesLocked(colorModes, activeColorMode);
            if (this.mBuiltInDisplayId == 0) {
                LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
                this.mBacklight = lights.getLight(0);
                if (LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION && LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                    this.mButtonlight = lights.getLight(2);
                } else {
                    this.mButtonlight = null;
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

        public boolean updatePhysicalDisplayInfoLocked(PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo, int[] colorModes, int activeColorMode) {
            int i;
            DisplayModeRecord record;
            this.mDisplayInfos = (PhysicalDisplayInfo[]) Arrays.copyOf(physicalDisplayInfos, physicalDisplayInfos.length);
            this.mActivePhysIndex = activeDisplayInfo;
            ArrayList<DisplayModeRecord> records = new ArrayList();
            boolean modesAdded = false;
            for (PhysicalDisplayInfo info : physicalDisplayInfos) {
                boolean existingMode = false;
                for (int j = 0; j < records.size(); j++) {
                    if (((DisplayModeRecord) records.get(j)).hasMatchingMode(info)) {
                        existingMode = true;
                        break;
                    }
                }
                if (!existingMode) {
                    record = findDisplayModeRecord(info);
                    if (record == null) {
                        record = new DisplayModeRecord(info);
                        modesAdded = true;
                    }
                    records.add(record);
                }
            }
            DisplayModeRecord activeRecord = null;
            for (i = 0; i < records.size(); i++) {
                record = (DisplayModeRecord) records.get(i);
                if (record.hasMatchingMode(physicalDisplayInfos[activeDisplayInfo])) {
                    activeRecord = record;
                    break;
                }
            }
            if (!(this.mActiveModeId == 0 || this.mActiveModeId == activeRecord.mMode.getModeId())) {
                this.mActiveModeInvalid = true;
                LocalDisplayAdapter.this.sendTraversalRequestLocked();
            }
            if (!(records.size() == this.mSupportedModes.size() ? modesAdded : true)) {
                return false;
            }
            this.mHavePendingChanges = true;
            this.mSupportedModes.clear();
            for (DisplayModeRecord record2 : records) {
                this.mSupportedModes.put(record2.mMode.getModeId(), record2);
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

        private boolean updateColorModesLocked(int[] colorModes, int activeColorMode) {
            List<Integer> pendingColorModes = new ArrayList();
            if (colorModes == null) {
                return false;
            }
            boolean colorModesChanged;
            boolean colorModesAdded = false;
            for (int colorMode : colorModes) {
                if (!this.mSupportedColorModes.contains(Integer.valueOf(colorMode))) {
                    colorModesAdded = true;
                }
                pendingColorModes.add(Integer.valueOf(colorMode));
            }
            if (pendingColorModes.size() == this.mSupportedColorModes.size()) {
                colorModesChanged = colorModesAdded;
            } else {
                colorModesChanged = true;
            }
            if (!colorModesChanged) {
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
                } else if (this.mSupportedColorModes.isEmpty()) {
                    Slog.e(LocalDisplayAdapter.TAG, "No color modes available!");
                } else {
                    Slog.e(LocalDisplayAdapter.TAG, "Default and active color mode is no longer available! Reverting to first available mode.");
                    this.mActiveColorMode = ((Integer) this.mSupportedColorModes.get(0)).intValue();
                    this.mActiveColorModeInvalid = true;
                }
            }
            return true;
        }

        private DisplayModeRecord findDisplayModeRecord(PhysicalDisplayInfo info) {
            for (int i = 0; i < this.mSupportedModes.size(); i++) {
                DisplayModeRecord record = (DisplayModeRecord) this.mSupportedModes.valueAt(i);
                if (record.hasMatchingMode(info)) {
                    return record;
                }
            }
            return null;
        }

        public void applyPendingDisplayDeviceInfoChangesLocked() {
            if (this.mHavePendingChanges) {
                this.mInfo = null;
                Slog.w(LocalDisplayAdapter.TAG, "@@@@@@ LocalDisplayAdapter--applyPendingDisplayDeviceInfoChangesLocked--change the mInfo to null");
                this.mHavePendingChanges = false;
            }
        }

        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                int i;
                PhysicalDisplayInfo phys = this.mDisplayInfos[this.mActivePhysIndex];
                this.mInfo = new DisplayDeviceInfo();
                this.mInfo.width = phys.width;
                this.mInfo.height = phys.height;
                this.mInfo.modeId = this.mActiveModeId;
                this.mInfo.defaultModeId = this.mDefaultModeId;
                this.mInfo.supportedModes = new Mode[this.mSupportedModes.size()];
                for (i = 0; i < this.mSupportedModes.size(); i++) {
                    this.mInfo.supportedModes[i] = ((DisplayModeRecord) this.mSupportedModes.valueAt(i)).mMode;
                }
                this.mInfo.colorMode = this.mActiveColorMode;
                this.mInfo.supportedColorModes = new int[this.mSupportedColorModes.size()];
                for (i = 0; i < this.mSupportedColorModes.size(); i++) {
                    this.mInfo.supportedColorModes[i] = ((Integer) this.mSupportedColorModes.get(i)).intValue();
                }
                this.mInfo.hdrCapabilities = this.mHdrCapabilities;
                this.mInfo.appVsyncOffsetNanos = phys.appVsyncOffsetNanos;
                this.mInfo.presentationDeadlineNanos = phys.presentationDeadlineNanos;
                this.mInfo.state = this.mState;
                this.mInfo.uniqueId = getUniqueId();
                if (phys.secure) {
                    this.mInfo.flags = 12;
                }
                Resources res = LocalDisplayAdapter.this.getContext().getResources();
                DisplayDeviceInfo displayDeviceInfo;
                if (this.mBuiltInDisplayId == 0) {
                    this.mInfo.name = res.getString(17039897);
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 3;
                    if (res.getBoolean(17956980) || (Build.IS_EMULATOR && SystemProperties.getBoolean(LocalDisplayAdapter.PROPERTY_EMULATOR_CIRCULAR, false))) {
                        displayDeviceInfo = this.mInfo;
                        displayDeviceInfo.flags |= 256;
                    }
                    this.mInfo.type = 1;
                    this.mInfo.densityDpi = (int) ((phys.density * 160.0f) + 0.5f);
                    this.mInfo.xDpi = phys.xDpi;
                    this.mInfo.yDpi = phys.yDpi;
                    this.mInfo.touch = 1;
                } else if (this.mBuiltInDisplayId == LocalDisplayAdapter.PAD_DISPLAY_ID) {
                    this.mInfo.name = "HUAWEI PAD PC Display";
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 2;
                    this.mInfo.type = 2;
                    this.mInfo.touch = 1;
                    this.mInfo.densityDpi = LocalDisplayAdapter.DEFAULT_DENSITYDPI == 0 ? (int) ((phys.density * 160.0f) + 0.5f) : LocalDisplayAdapter.DEFAULT_DENSITYDPI;
                    HwPCUtils.log(LocalDisplayAdapter.TAG, "PAD_DISPLAY_ID densityDpi:" + this.mInfo.densityDpi);
                    this.mInfo.xDpi = phys.xDpi;
                    this.mInfo.yDpi = phys.yDpi;
                    HwPCUtils.log(LocalDisplayAdapter.TAG, "PAD_DISPLAY_ID mInfo.xDpi:" + this.mInfo.xDpi + ",mInfo.yDpi:" + this.mInfo.yDpi);
                } else {
                    this.mInfo.type = 2;
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 64;
                    this.mInfo.name = LocalDisplayAdapter.this.getContext().getResources().getString(17039898);
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
                        displayDeviceInfo = this.mInfo;
                        displayDeviceInfo.flags |= 2;
                    }
                    if (!res.getBoolean(17956977)) {
                        displayDeviceInfo = this.mInfo;
                        displayDeviceInfo.flags |= 128;
                    }
                }
                Slog.w(LocalDisplayAdapter.TAG, "@@@@@@ LocalDisplayAdapter--getDisplayDeviceInfoLocked--mInfo = " + this.mInfo);
            }
            return this.mInfo;
        }

        public Runnable requestDisplayStateLocked(int state, int brightness) {
            if (-assertionsDisabled || state != 1 || brightness == 0) {
                boolean stateChanged = this.mState != state;
                final boolean brightnessChanged = (this.mBrightness == brightness || this.mBacklight == null) ? false : true;
                if (!stateChanged && !brightnessChanged) {
                    return null;
                }
                final int displayId = this.mBuiltInDisplayId;
                final IBinder token = getDisplayTokenLocked();
                final int oldState = this.mState;
                if (stateChanged) {
                    this.mState = state;
                    updateDeviceInfoLocked();
                }
                if (brightnessChanged) {
                    this.mBrightness = brightness;
                }
                final int i = state;
                final int i2 = brightness;
                return new Runnable() {
                    public void run() {
                        boolean z = false;
                        int currentState = oldState;
                        if (Display.isSuspendedState(oldState) || oldState == 0) {
                            if (!Display.isSuspendedState(i)) {
                                setDisplayState(i);
                                currentState = i;
                            } else if (i == 4 || oldState == 4) {
                                setDisplayState(3);
                                currentState = 3;
                            } else {
                                return;
                            }
                        }
                        if ((i == 5 || currentState == 5) && currentState != i) {
                            if (i == 5) {
                                z = true;
                            }
                            setVrMode(z);
                        }
                        if (brightnessChanged) {
                            setDisplayBrightness(i2);
                        }
                        if (i != currentState) {
                            setDisplayState(i);
                        }
                    }

                    private void setVrMode(boolean isVrEnabled) {
                        Slog.d(LocalDisplayAdapter.TAG, "setVrMode(id=" + displayId + ", state=" + Display.stateToString(i) + ")");
                        if (LocalDisplayDevice.this.mBacklight != null) {
                            LocalDisplayDevice.this.mBacklight.setVrMode(isVrEnabled);
                        }
                    }

                    private void setDisplayState(int state) {
                        Trace.traceBegin(131072, "setDisplayState(id=" + displayId + ", state=" + Display.stateToString(state) + ")");
                        try {
                            SurfaceControl.setDisplayPowerMode(token, LocalDisplayAdapter.getPowerModeForState(state));
                        } finally {
                            Trace.traceEnd(131072);
                        }
                    }

                    private void setDisplayBrightness(int brightness) {
                        Trace.traceBegin(131072, "setDisplayBrightness(id=" + displayId + ", brightness=" + brightness + ")");
                        try {
                            LocalDisplayDevice.this.mBacklight.setBrightness(brightness);
                            LocalDisplayDevice.this.updateButtonBrightness(brightness);
                        } finally {
                            Trace.traceEnd(131072);
                        }
                    }
                };
            }
            throw new AssertionError();
        }

        private void updateButtonBrightness(int brightness) {
            if (this.mButtonlight != null && LocalDisplayAdapter.this.mDeviceProvisioned) {
                if (LocalDisplayAdapter.this.mTrikeyNaviMode >= 0) {
                    if (LocalDisplayAdapter.this.mButtonLightMode != 0) {
                        LocalDisplayAdapter.this.setButtonLightTimeout(false);
                    } else if (brightness == 0) {
                        LocalDisplayAdapter.this.setButtonLightTimeout(false);
                    }
                    if (!LocalDisplayAdapter.this.isButtonLightTimeout()) {
                        this.mButtonlight.setBrightness(brightness);
                        return;
                    }
                    return;
                }
                LocalDisplayAdapter.this.setButtonLightTimeout(false);
                this.mButtonlight.setBrightness(0);
            }
        }

        public void requestDisplayModesInTransactionLocked(int colorMode, int modeId) {
            if (requestModeInTransactionLocked(modeId) || requestColorModeInTransactionLocked(colorMode)) {
                updateDeviceInfoLocked();
            }
        }

        public boolean requestModeInTransactionLocked(int modeId) {
            if (modeId == 0) {
                modeId = this.mDefaultModeId;
            } else if (this.mSupportedModes.indexOfKey(modeId) < 0) {
                Slog.w(LocalDisplayAdapter.TAG, "Requested mode " + modeId + " is not supported by this display," + " reverting to default display mode.");
                modeId = this.mDefaultModeId;
            }
            int physIndex = findDisplayInfoIndexLocked(modeId);
            if (physIndex < 0) {
                Slog.w(LocalDisplayAdapter.TAG, "Requested mode ID " + modeId + " not available," + " trying with default mode ID");
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

        public boolean requestColorModeInTransactionLocked(int colorMode) {
            if (this.mActiveColorMode == colorMode) {
                return false;
            }
            if (this.mSupportedColorModes.contains(Integer.valueOf(colorMode))) {
                SurfaceControl.setActiveColorMode(getDisplayTokenLocked(), colorMode);
                this.mActiveColorMode = colorMode;
                this.mActiveColorModeInvalid = false;
                return true;
            }
            Slog.w(LocalDisplayAdapter.TAG, "Unable to find color mode " + colorMode + ", ignoring request.");
            return false;
        }

        public void dumpLocked(PrintWriter pw) {
            int i;
            super.dumpLocked(pw);
            pw.println("mBuiltInDisplayId=" + this.mBuiltInDisplayId);
            pw.println("mActivePhysIndex=" + this.mActivePhysIndex);
            pw.println("mActiveModeId=" + this.mActiveModeId);
            pw.println("mActiveColorMode=" + this.mActiveColorMode);
            pw.println("mState=" + Display.stateToString(this.mState));
            pw.println("mBrightness=" + this.mBrightness);
            pw.println("mBacklight=" + this.mBacklight);
            pw.println("mDisplayInfos=");
            for (Object obj : this.mDisplayInfos) {
                pw.println("  " + obj);
            }
            pw.println("mSupportedModes=");
            for (i = 0; i < this.mSupportedModes.size(); i++) {
                pw.println("  " + this.mSupportedModes.valueAt(i));
            }
            pw.print("mSupportedColorModes=[");
            for (i = 0; i < this.mSupportedColorModes.size(); i++) {
                if (i != 0) {
                    pw.print(", ");
                }
                pw.print(this.mSupportedColorModes.get(i));
            }
            pw.println("]");
        }

        private int findDisplayInfoIndexLocked(int modeId) {
            DisplayModeRecord record = (DisplayModeRecord) this.mSupportedModes.get(modeId);
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
            Slog.w(LocalDisplayAdapter.TAG, "@@@@@@ LocalDisplayAdapter--updateDeviceInfoLocked--change the mInfo to null");
            LocalDisplayAdapter.this.sendDisplayDeviceEventLocked(this, 2);
        }
    }

    private class SettingsObserver extends ContentObserver {
        final /* synthetic */ LocalDisplayAdapter this$0;

        SettingsObserver(LocalDisplayAdapter this$0, Handler handler) {
            boolean z = false;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            if (Secure.getIntForUser(this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            }
            this$0.mDeviceProvisioned = z;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mResolver, "swap_key_position", this$0.defaultNaviMode, ActivityManager.getCurrentUser());
            this$0.mButtonLightMode = System.getIntForUser(this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
        }

        public void registerContentObserver(int userId) {
            this.this$0.mResolver.registerContentObserver(System.getUriFor("device_provisioned"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("swap_key_position"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("button_light_mode"), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = false;
            LocalDisplayAdapter localDisplayAdapter = this.this$0;
            if (Secure.getIntForUser(this.this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            }
            localDisplayAdapter.mDeviceProvisioned = z;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mResolver, "swap_key_position", this.this$0.defaultNaviMode, ActivityManager.getCurrentUser());
            this.this$0.mButtonLightMode = System.getIntForUser(this.this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            Slog.i(LocalDisplayAdapter.TAG, "mTrikeyNaviMode:" + this.this$0.mTrikeyNaviMode + " mButtonLightMode:" + this.this$0.mButtonLightMode);
        }
    }

    private class UserSwtichReceiver extends BroadcastReceiver {
        /* synthetic */ UserSwtichReceiver(LocalDisplayAdapter this$0, UserSwtichReceiver -this1) {
            this();
        }

        private UserSwtichReceiver() {
        }

        /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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

    public LocalDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, Listener listener) {
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

    private void tryConnectDisplayLocked(int builtInDisplayId) {
        IBinder displayToken = SurfaceControl.getBuiltInDisplay(builtInDisplayId);
        if (builtInDisplayId == PAD_DISPLAY_ID) {
            displayToken = SurfaceControl.getBuiltInDisplay(0);
        }
        if (displayToken != null) {
            PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken);
            if (configs == null) {
                Slog.w(TAG, "No valid configs found for display device " + builtInDisplayId);
                return;
            }
            int activeConfig = SurfaceControl.getActiveConfig(displayToken);
            for (int i = 0; i < configs.length; i++) {
                Slog.w(TAG, "@@@@@@ the display config get from surfaceflinger: width = " + configs[i].width + "; height = " + configs[i].height + "; density = " + configs[i].density + "; builtInDisplayId = " + builtInDisplayId + "; activeConfig = " + activeConfig, new Exception());
            }
            if (activeConfig < 0) {
                Slog.w(TAG, "No active config found for display device " + builtInDisplayId);
                return;
            }
            int activeColorMode = SurfaceControl.getActiveColorMode(displayToken);
            if (activeColorMode < 0) {
                Slog.w(TAG, "Unable to get active color mode for display device " + builtInDisplayId);
                activeColorMode = -1;
            }
            int[] colorModes = SurfaceControl.getDisplayColorModes(displayToken);
            LocalDisplayDevice device = (LocalDisplayDevice) this.mDevices.get(builtInDisplayId);
            if (device == null) {
                device = new LocalDisplayDevice(displayToken, builtInDisplayId, configs, activeConfig, colorModes, activeColorMode);
                this.mDevices.put(builtInDisplayId, device);
                sendDisplayDeviceEventLocked(device, 1);
            } else if (device.updatePhysicalDisplayInfoLocked(configs, activeConfig, colorModes, activeColorMode)) {
                sendDisplayDeviceEventLocked(device, 2);
            }
        }
    }

    private void tryDisconnectDisplayLocked(int builtInDisplayId) {
        LocalDisplayDevice device = (LocalDisplayDevice) this.mDevices.get(builtInDisplayId);
        if (device != null) {
            this.mDevices.remove(builtInDisplayId);
            sendDisplayDeviceEventLocked(device, 3);
        }
    }

    static int getPowerModeForState(int state) {
        switch (state) {
            case 1:
                return 0;
            case 3:
                return 1;
            case 4:
                return 3;
            default:
                return 2;
        }
    }

    public void registerContentObserver(Context context, Handler handler) {
        int i = 1;
        if (context != null) {
            if (!(FRONT_FINGERPRINT_NAVIGATION && FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1)) {
                i = 0;
            }
            if ((i ^ 1) == 0) {
                try {
                    this.mResolver = context.getContentResolver();
                    this.mSettingsObserver = new SettingsObserver(this, handler);
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.intent.action.USER_SWITCHED");
                    context.registerReceiver(new UserSwtichReceiver(this, null), intentFilter);
                } catch (Exception exp) {
                    Log.e(TAG, "registerContentObserver:" + exp.getMessage());
                }
            }
        }
    }

    private boolean isButtonLightTimeout() {
        return SystemProperties.getBoolean("sys.button.light.timeout", false);
    }

    private void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }

    public void pcDisplayChangeService(boolean connected) {
        if (HwPCUtils.enabledInPad()) {
            Slog.w(TAG, "pcDisplayChangeService connected = " + connected);
            synchronized (getSyncRoot()) {
                if (!connected) {
                    Slog.w(TAG, "pcDisplayChangeService tryDisconnectDisplayLocked");
                    tryDisconnectDisplayLocked(PAD_DISPLAY_ID);
                } else if (((LocalDisplayDevice) this.mDevices.get(PAD_DISPLAY_ID)) == null) {
                    Slog.w(TAG, "pcDisplayChangeService tryDisconnectDisplayLocked");
                    tryConnectDisplayLocked(PAD_DISPLAY_ID);
                }
            }
        }
    }
}
