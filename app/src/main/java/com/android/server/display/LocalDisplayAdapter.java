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
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.Display.ColorTransform;
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
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

final class LocalDisplayAdapter extends DisplayAdapter {
    private static final int[] BUILT_IN_DISPLAY_IDS_TO_SCAN = null;
    private static final boolean DEBUG = false;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = false;
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = 0;
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    private static final String TAG = "LocalDisplayAdapter";
    private static final String UNIQUE_ID_PREFIX = "local:";
    private static final boolean isChinaArea = false;
    private int defaultNaviMode;
    private int mButtonLightMode;
    private boolean mDeviceProvisioned;
    private final SparseArray<LocalDisplayDevice> mDevices;
    private HotplugDisplayEventReceiver mHotplugReceiver;
    private ContentResolver mResolver;
    private SettingsObserver mSettingsObserver;
    private int mTrikeyNaviMode;

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
            return LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
        }

        public String toString() {
            return "DisplayModeRecord{mMode=" + this.mMode + "}";
        }
    }

    private final class HotplugDisplayEventReceiver extends DisplayEventReceiver {
        public HotplugDisplayEventReceiver(Looper looper) {
            super(looper);
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
        static final /* synthetic */ boolean -assertionsDisabled = false;
        final /* synthetic */ boolean $assertionsDisabled;
        private int mActiveColorTransformId;
        private boolean mActiveColorTransformInvalid;
        private int mActiveModeId;
        private boolean mActiveModeInvalid;
        private int mActivePhysIndex;
        private final Light mBacklight;
        private int mBrightness;
        private final int mBuiltInDisplayId;
        private final Light mButtonlight;
        private int mDefaultColorTransformId;
        private int mDefaultModeId;
        private PhysicalDisplayInfo[] mDisplayInfos;
        private boolean mHavePendingChanges;
        private HdrCapabilities mHdrCapabilities;
        private DisplayDeviceInfo mInfo;
        private int mState;
        private final SparseArray<ColorTransform> mSupportedColorTransforms;
        private final SparseArray<DisplayModeRecord> mSupportedModes;

        /* renamed from: com.android.server.display.LocalDisplayAdapter.LocalDisplayDevice.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ int val$brightness;
            final /* synthetic */ boolean val$brightnessChanged;
            final /* synthetic */ int val$displayId;
            final /* synthetic */ int val$oldState;
            final /* synthetic */ int val$state;
            final /* synthetic */ IBinder val$token;

            AnonymousClass1(int val$oldState, int val$state, boolean val$brightnessChanged, int val$brightness, int val$displayId, IBinder val$token) {
                this.val$oldState = val$oldState;
                this.val$state = val$state;
                this.val$brightnessChanged = val$brightnessChanged;
                this.val$brightness = val$brightness;
                this.val$displayId = val$displayId;
                this.val$token = val$token;
            }

            public void run() {
                int currentState = this.val$oldState;
                if (Display.isSuspendedState(this.val$oldState) || this.val$oldState == 0) {
                    if (!Display.isSuspendedState(this.val$state)) {
                        setDisplayState(this.val$state);
                        currentState = this.val$state;
                    } else if (this.val$state == 4 || this.val$oldState == 4) {
                        setDisplayState(3);
                        currentState = 3;
                    } else {
                        return;
                    }
                }
                if (this.val$brightnessChanged) {
                    setDisplayBrightness(this.val$brightness);
                }
                if (this.val$state != currentState) {
                    setDisplayState(this.val$state);
                }
            }

            private void setDisplayState(int state) {
                Trace.traceBegin(131072, "setDisplayState(id=" + this.val$displayId + ", state=" + Display.stateToString(state) + ")");
                try {
                    SurfaceControl.setDisplayPowerMode(this.val$token, LocalDisplayAdapter.getPowerModeForState(state));
                } finally {
                    Trace.traceEnd(131072);
                }
            }

            private void setDisplayBrightness(int brightness) {
                Trace.traceBegin(131072, "setDisplayBrightness(id=" + this.val$displayId + ", brightness=" + brightness + ")");
                try {
                    LocalDisplayDevice.this.mBacklight.setBrightness(brightness);
                    LocalDisplayDevice.this.updateButtonBrightness(brightness);
                } finally {
                    Trace.traceEnd(131072);
                }
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.LocalDisplayAdapter.LocalDisplayDevice.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.LocalDisplayAdapter.LocalDisplayDevice.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.LocalDisplayAdapter.LocalDisplayDevice.<clinit>():void");
        }

        public LocalDisplayDevice(IBinder displayToken, int builtInDisplayId, PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo) {
            super(LocalDisplayAdapter.this, displayToken, LocalDisplayAdapter.UNIQUE_ID_PREFIX + builtInDisplayId);
            this.mSupportedModes = new SparseArray();
            this.mSupportedColorTransforms = new SparseArray();
            this.mState = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
            this.mBrightness = -1;
            this.mBuiltInDisplayId = builtInDisplayId;
            updatePhysicalDisplayInfoLocked(physicalDisplayInfos, activeDisplayInfo);
            if (this.mBuiltInDisplayId == 0) {
                LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
                this.mBacklight = lights.getLight(LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY);
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

        public boolean updatePhysicalDisplayInfoLocked(PhysicalDisplayInfo[] physicalDisplayInfos, int activeDisplayInfo) {
            DisplayModeRecord record;
            this.mDisplayInfos = (PhysicalDisplayInfo[]) Arrays.copyOf(physicalDisplayInfos, physicalDisplayInfos.length);
            this.mActivePhysIndex = activeDisplayInfo;
            ArrayList<ColorTransform> colorTransforms = new ArrayList();
            boolean colorTransformsAdded = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
            ColorTransform activeColorTransform = null;
            int i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
            while (true) {
                int length = physicalDisplayInfos.length;
                if (i >= r0) {
                    break;
                }
                PhysicalDisplayInfo info = physicalDisplayInfos[i];
                boolean existingMode = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
                int j = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
                while (j < colorTransforms.size()) {
                    ColorTransform colorTransform;
                    if (((ColorTransform) colorTransforms.get(j)).getColorTransform() == info.colorTransform) {
                        existingMode = true;
                        if (i == activeDisplayInfo) {
                            activeColorTransform = (ColorTransform) colorTransforms.get(j);
                        }
                        if (existingMode) {
                            colorTransform = findColorTransform(info);
                            if (colorTransform == null) {
                                colorTransform = DisplayAdapter.createColorTransform(info.colorTransform);
                                colorTransformsAdded = true;
                            }
                            colorTransforms.add(colorTransform);
                            if (i == activeDisplayInfo) {
                                activeColorTransform = colorTransform;
                            }
                        }
                        i++;
                    } else {
                        j++;
                    }
                }
                if (existingMode) {
                    colorTransform = findColorTransform(info);
                    if (colorTransform == null) {
                        colorTransform = DisplayAdapter.createColorTransform(info.colorTransform);
                        colorTransformsAdded = true;
                    }
                    colorTransforms.add(colorTransform);
                    if (i == activeDisplayInfo) {
                        activeColorTransform = colorTransform;
                    }
                }
                i++;
            }
            ArrayList<DisplayModeRecord> records = new ArrayList();
            boolean modesAdded = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
            i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
            while (true) {
                length = physicalDisplayInfos.length;
                if (i >= r0) {
                    break;
                }
                info = physicalDisplayInfos[i];
                existingMode = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
                for (j = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; j < records.size(); j++) {
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
                i++;
            }
            DisplayModeRecord activeRecord = null;
            for (i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < records.size(); i++) {
                record = (DisplayModeRecord) records.get(i);
                if (record.hasMatchingMode(physicalDisplayInfos[activeDisplayInfo])) {
                    activeRecord = record;
                    break;
                }
            }
            if (this.mActiveModeId != 0) {
                if (this.mActiveModeId != activeRecord.mMode.getModeId()) {
                    this.mActiveModeInvalid = true;
                    LocalDisplayAdapter.this.sendTraversalRequestLocked();
                }
            }
            if (!(this.mActiveColorTransformId == 0 || this.mActiveColorTransformId == activeColorTransform.getId())) {
                this.mActiveColorTransformInvalid = true;
                LocalDisplayAdapter.this.sendTraversalRequestLocked();
            }
            boolean z;
            if (colorTransforms.size() == this.mSupportedColorTransforms.size()) {
                z = colorTransformsAdded;
            } else {
                z = true;
            }
            if (!(records.size() == this.mSupportedModes.size() ? modesAdded : true) && !r9) {
                return LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
            }
            this.mHavePendingChanges = true;
            this.mSupportedModes.clear();
            for (DisplayModeRecord record2 : records) {
                this.mSupportedModes.put(record2.mMode.getModeId(), record2);
            }
            this.mSupportedColorTransforms.clear();
            for (ColorTransform colorTransform2 : colorTransforms) {
                this.mSupportedColorTransforms.put(colorTransform2.getId(), colorTransform2);
            }
            if (findDisplayInfoIndexLocked(this.mDefaultColorTransformId, this.mDefaultModeId) < 0) {
                if (this.mDefaultModeId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Default display mode no longer available, using currently active mode as default.");
                }
                this.mDefaultModeId = activeRecord.mMode.getModeId();
                if (this.mDefaultColorTransformId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Default color transform no longer available, using currently active color transform as default");
                }
                this.mDefaultColorTransformId = activeColorTransform.getId();
            }
            if (this.mSupportedModes.indexOfKey(this.mActiveModeId) < 0) {
                if (this.mActiveModeId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Active display mode no longer available, reverting to default mode.");
                }
                this.mActiveModeId = this.mDefaultModeId;
                this.mActiveModeInvalid = true;
            }
            if (this.mSupportedColorTransforms.indexOfKey(this.mActiveColorTransformId) < 0) {
                if (this.mActiveColorTransformId != 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Active color transform no longer available, reverting to default transform.");
                }
                this.mActiveColorTransformId = this.mDefaultColorTransformId;
                this.mActiveColorTransformInvalid = true;
            }
            LocalDisplayAdapter.this.sendTraversalRequestLocked();
            return true;
        }

        private DisplayModeRecord findDisplayModeRecord(PhysicalDisplayInfo info) {
            for (int i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mSupportedModes.size(); i++) {
                DisplayModeRecord record = (DisplayModeRecord) this.mSupportedModes.valueAt(i);
                if (record.hasMatchingMode(info)) {
                    return record;
                }
            }
            return null;
        }

        private ColorTransform findColorTransform(PhysicalDisplayInfo info) {
            for (int i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mSupportedColorTransforms.size(); i++) {
                ColorTransform transform = (ColorTransform) this.mSupportedColorTransforms.valueAt(i);
                if (transform.getColorTransform() == info.colorTransform) {
                    return transform;
                }
            }
            return null;
        }

        public void applyPendingDisplayDeviceInfoChangesLocked() {
            if (this.mHavePendingChanges) {
                this.mInfo = null;
                this.mHavePendingChanges = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
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
                for (i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mSupportedModes.size(); i++) {
                    this.mInfo.supportedModes[i] = ((DisplayModeRecord) this.mSupportedModes.valueAt(i)).mMode;
                }
                this.mInfo.colorTransformId = this.mActiveColorTransformId;
                this.mInfo.defaultColorTransformId = this.mDefaultColorTransformId;
                this.mInfo.supportedColorTransforms = new ColorTransform[this.mSupportedColorTransforms.size()];
                for (i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mSupportedColorTransforms.size(); i++) {
                    this.mInfo.supportedColorTransforms[i] = (ColorTransform) this.mSupportedColorTransforms.valueAt(i);
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
                    this.mInfo.name = res.getString(17040622);
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 3;
                    if (res.getBoolean(17957029) || (Build.IS_EMULATOR && SystemProperties.getBoolean(LocalDisplayAdapter.PROPERTY_EMULATOR_CIRCULAR, LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION))) {
                        displayDeviceInfo = this.mInfo;
                        displayDeviceInfo.flags |= DumpState.DUMP_SHARED_USERS;
                    }
                    this.mInfo.type = 1;
                    this.mInfo.densityDpi = (int) ((phys.density * 160.0f) + TaskPositioner.RESIZING_HINT_ALPHA);
                    this.mInfo.xDpi = phys.xDpi;
                    this.mInfo.yDpi = phys.yDpi;
                    this.mInfo.touch = 1;
                } else {
                    this.mInfo.type = 2;
                    displayDeviceInfo = this.mInfo;
                    displayDeviceInfo.flags |= 64;
                    this.mInfo.name = LocalDisplayAdapter.this.getContext().getResources().getString(17040623);
                    this.mInfo.touch = 2;
                    this.mInfo.setAssumedDensityForExternalDisplay(phys.width, phys.height);
                    if ("portrait".equals(SystemProperties.get("persist.demo.hdmirotation"))) {
                        this.mInfo.rotation = 3;
                    }
                    if (SystemProperties.getBoolean("persist.demo.hdmirotates", LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION)) {
                        displayDeviceInfo = this.mInfo;
                        displayDeviceInfo.flags |= 2;
                    }
                    if (!res.getBoolean(17956986)) {
                        displayDeviceInfo = this.mInfo;
                        displayDeviceInfo.flags |= DumpState.DUMP_PACKAGES;
                    }
                }
            }
            return this.mInfo;
        }

        public Runnable requestDisplayStateLocked(int state, int brightness) {
            Object obj = 1;
            if (!-assertionsDisabled) {
                if (state == 1 && brightness != 0) {
                    obj = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            boolean stateChanged = this.mState != state ? true : LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
            boolean brightnessChanged = (this.mBrightness == brightness || this.mBacklight == null) ? LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION : true;
            if (!stateChanged && !brightnessChanged) {
                return null;
            }
            int displayId = this.mBuiltInDisplayId;
            IBinder token = getDisplayTokenLocked();
            int oldState = this.mState;
            if (stateChanged) {
                this.mState = state;
                updateDeviceInfoLocked();
            }
            if (brightnessChanged) {
                this.mBrightness = brightness;
            }
            return new AnonymousClass1(oldState, state, brightnessChanged, brightness, displayId, token);
        }

        private void updateButtonBrightness(int brightness) {
            if (this.mButtonlight != null && LocalDisplayAdapter.this.mDeviceProvisioned) {
                if (LocalDisplayAdapter.this.mTrikeyNaviMode >= 0) {
                    if (LocalDisplayAdapter.this.mButtonLightMode != 0) {
                        LocalDisplayAdapter.this.setButtonLightTimeout(LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION);
                    } else if (brightness == 0) {
                        LocalDisplayAdapter.this.setButtonLightTimeout(LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION);
                    }
                    if (!LocalDisplayAdapter.this.isButtonLightTimeout()) {
                        this.mButtonlight.setBrightness(brightness);
                        return;
                    }
                    return;
                }
                LocalDisplayAdapter.this.setButtonLightTimeout(LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION);
                this.mButtonlight.setBrightness(LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY);
            }
        }

        public void requestColorTransformAndModeInTransactionLocked(int colorTransformId, int modeId) {
            if (modeId == 0) {
                modeId = this.mDefaultModeId;
            } else if (this.mSupportedModes.indexOfKey(modeId) < 0) {
                Slog.w(LocalDisplayAdapter.TAG, "Requested mode " + modeId + " is not supported by this display," + " reverting to default display mode.");
                modeId = this.mDefaultModeId;
            }
            if (colorTransformId == 0) {
                colorTransformId = this.mDefaultColorTransformId;
            } else if (this.mSupportedColorTransforms.indexOfKey(colorTransformId) < 0) {
                Slog.w(LocalDisplayAdapter.TAG, "Requested color transform " + colorTransformId + " is not supported" + " by this display, reverting to the default color transform");
                colorTransformId = this.mDefaultColorTransformId;
            }
            int physIndex = findDisplayInfoIndexLocked(colorTransformId, modeId);
            if (physIndex < 0) {
                Slog.w(LocalDisplayAdapter.TAG, "Requested color transform, mode ID pair (" + colorTransformId + ", " + modeId + ") not available, trying color transform with default mode ID");
                modeId = this.mDefaultModeId;
                physIndex = findDisplayInfoIndexLocked(colorTransformId, modeId);
                if (physIndex < 0) {
                    Slog.w(LocalDisplayAdapter.TAG, "Requested color transform with default mode ID still not available, falling back to default color transform with default mode.");
                    colorTransformId = this.mDefaultColorTransformId;
                    physIndex = findDisplayInfoIndexLocked(colorTransformId, modeId);
                }
            }
            if (this.mActivePhysIndex != physIndex) {
                SurfaceControl.setActiveConfig(getDisplayTokenLocked(), physIndex);
                this.mActivePhysIndex = physIndex;
                this.mActiveModeId = modeId;
                this.mActiveModeInvalid = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
                this.mActiveColorTransformId = colorTransformId;
                this.mActiveColorTransformInvalid = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
                updateDeviceInfoLocked();
            }
        }

        public void dumpLocked(PrintWriter pw) {
            int i;
            super.dumpLocked(pw);
            pw.println("mBuiltInDisplayId=" + this.mBuiltInDisplayId);
            pw.println("mActivePhysIndex=" + this.mActivePhysIndex);
            pw.println("mActiveModeId=" + this.mActiveModeId);
            pw.println("mActiveColorTransformId=" + this.mActiveColorTransformId);
            pw.println("mState=" + Display.stateToString(this.mState));
            pw.println("mBrightness=" + this.mBrightness);
            pw.println("mBacklight=" + this.mBacklight);
            pw.println("mDisplayInfos=");
            for (i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mDisplayInfos.length; i++) {
                pw.println("  " + this.mDisplayInfos[i]);
            }
            pw.println("mSupportedModes=");
            for (i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mSupportedModes.size(); i++) {
                pw.println("  " + this.mSupportedModes.valueAt(i));
            }
            pw.println("mSupportedColorTransforms=[");
            for (i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mSupportedColorTransforms.size(); i++) {
                if (i != 0) {
                    pw.print(", ");
                }
                pw.print(this.mSupportedColorTransforms.valueAt(i));
            }
            pw.println("]");
        }

        private int findDisplayInfoIndexLocked(int colorTransformId, int modeId) {
            DisplayModeRecord record = (DisplayModeRecord) this.mSupportedModes.get(modeId);
            ColorTransform transform = (ColorTransform) this.mSupportedColorTransforms.get(colorTransformId);
            if (!(record == null || transform == null)) {
                for (int i = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < this.mDisplayInfos.length; i++) {
                    PhysicalDisplayInfo info = this.mDisplayInfos[i];
                    if (info.colorTransform == transform.getColorTransform() && record.hasMatchingMode(info)) {
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
        final /* synthetic */ LocalDisplayAdapter this$0;

        SettingsObserver(LocalDisplayAdapter this$0, Handler handler) {
            boolean z = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            if (Secure.getIntForUser(this$0.mResolver, "device_provisioned", LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            }
            this$0.mDeviceProvisioned = z;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mResolver, "swap_key_position", this$0.defaultNaviMode, ActivityManager.getCurrentUser());
            this$0.mButtonLightMode = System.getIntForUser(this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
        }

        public void registerContentObserver(int userId) {
            this.this$0.mResolver.registerContentObserver(System.getUriFor("device_provisioned"), LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("swap_key_position"), LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("button_light_mode"), LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION;
            LocalDisplayAdapter localDisplayAdapter = this.this$0;
            if (Secure.getIntForUser(this.this$0.mResolver, "device_provisioned", LocalDisplayAdapter.FRONT_FINGERPRINT_NAVIGATION_TRIKEY, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            }
            localDisplayAdapter.mDeviceProvisioned = z;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mResolver, "swap_key_position", this.this$0.defaultNaviMode, ActivityManager.getCurrentUser());
            this.this$0.mButtonLightMode = System.getIntForUser(this.this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            Slog.i(LocalDisplayAdapter.TAG, "mTrikeyNaviMode:" + this.this$0.mTrikeyNaviMode + " mButtonLightMode:" + this.this$0.mButtonLightMode);
        }
    }

    private class UserSwtichReceiver extends BroadcastReceiver {
        final /* synthetic */ LocalDisplayAdapter this$0;

        /* synthetic */ UserSwtichReceiver(LocalDisplayAdapter this$0, UserSwtichReceiver userSwtichReceiver) {
            this(this$0);
        }

        private UserSwtichReceiver(LocalDisplayAdapter this$0) {
            this.this$0 = this$0;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !"android.intent.action.USER_SWITCHED".equals(intent.getAction()))) {
                int newUserId = intent.getIntExtra("android.intent.extra.user_handle", UserHandle.myUserId());
                Slog.i(LocalDisplayAdapter.TAG, "UserSwtichReceiver:" + newUserId);
                if (this.this$0.mSettingsObserver != null) {
                    this.this$0.mSettingsObserver.registerContentObserver(newUserId);
                    this.this$0.mSettingsObserver.onChange(true);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.LocalDisplayAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.LocalDisplayAdapter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.LocalDisplayAdapter.<clinit>():void");
    }

    public LocalDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, Listener listener) {
        super(syncRoot, context, handler, listener, TAG);
        this.mDevices = new SparseArray();
        this.mTrikeyNaviMode = -1;
        this.mButtonLightMode = 1;
        this.defaultNaviMode = FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
        this.mDeviceProvisioned = true;
    }

    public void registerLocked() {
        super.registerLocked();
        this.mHotplugReceiver = new HotplugDisplayEventReceiver(getHandler().getLooper());
        int[] iArr = BUILT_IN_DISPLAY_IDS_TO_SCAN;
        int length = iArr.length;
        for (int i = FRONT_FINGERPRINT_NAVIGATION_TRIKEY; i < length; i++) {
            tryConnectDisplayLocked(iArr[i]);
        }
    }

    private void tryConnectDisplayLocked(int builtInDisplayId) {
        IBinder displayToken = SurfaceControl.getBuiltInDisplay(builtInDisplayId);
        if (displayToken != null) {
            PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken);
            if (configs == null) {
                Slog.w(TAG, "No valid configs found for display device " + builtInDisplayId);
                return;
            }
            int activeConfig = SurfaceControl.getActiveConfig(displayToken);
            if (activeConfig < 0) {
                Slog.w(TAG, "No active config found for display device " + builtInDisplayId);
                return;
            }
            LocalDisplayDevice device = (LocalDisplayDevice) this.mDevices.get(builtInDisplayId);
            if (device == null) {
                device = new LocalDisplayDevice(displayToken, builtInDisplayId, configs, activeConfig);
                this.mDevices.put(builtInDisplayId, device);
                sendDisplayDeviceEventLocked(device, 1);
            } else if (device.updatePhysicalDisplayInfoLocked(configs, activeConfig)) {
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
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
            case H.REPORT_LOSING_FOCUS /*3*/:
                return 1;
            case H.DO_TRAVERSAL /*4*/:
                return 3;
            default:
                return 2;
        }
    }

    public void registerContentObserver(Context context, Handler handler) {
        if (context != null && FRONT_FINGERPRINT_NAVIGATION && FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            try {
                this.mResolver = context.getContentResolver();
                this.mSettingsObserver = new SettingsObserver(this, handler);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.USER_SWITCHED");
                context.registerReceiver(new UserSwtichReceiver(), intentFilter);
            } catch (Exception exp) {
                Log.e(TAG, "registerContentObserver:" + exp.getMessage());
            }
        }
    }

    private boolean isButtonLightTimeout() {
        return SystemProperties.getBoolean("sys.button.light.timeout", FRONT_FINGERPRINT_NAVIGATION);
    }

    private void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }
}
