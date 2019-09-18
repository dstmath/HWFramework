package com.android.server.display;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplaySessionInfo;
import android.hardware.display.WifiDisplayStatus;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.HwServiceFactory;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.WifiDisplayController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class WifiDisplayAdapter extends DisplayAdapter implements IWifiDisplayAdapterInner {
    private static final String ACTION_DISCONNECT = "android.server.display.wfd.DISCONNECT";
    private static final String ACTION_WIFI_DISPLAY_CASTING = "com.huawei.hardware.display.action.WIFI_DISPLAY_CASTING";
    private static final boolean DEBUG = false;
    private static final String DISPLAY_NAME_PREFIX = "wifi:";
    private static final int MSG_SEND_CAST_CHANGE_BROADCAST = 2;
    private static final int MSG_SEND_STATUS_CHANGE_BROADCAST = 1;
    private static final String TAG = "WifiDisplayAdapter";
    private static final String WIFI_DISPLAY_CASTING_PERMISSION = "com.huawei.wfd.permission.ACCESS_WIFI_DISPLAY_CASTING";
    private static final String WIFI_DISPLAY_UIBC_INFO = "com.huawei.hardware.display.action.WIFI_DISPLAY_UIBC_INFO";
    /* access modifiers changed from: private */
    public WifiDisplay mActiveDisplay;
    /* access modifiers changed from: private */
    public int mActiveDisplayState;
    /* access modifiers changed from: private */
    public WifiDisplay[] mAvailableDisplays = WifiDisplay.EMPTY_ARRAY;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiDisplayAdapter.ACTION_DISCONNECT)) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    WifiDisplayAdapter.this.requestDisconnectLocked();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mConnectionFailedReason = -1;
    private WifiDisplayStatus mCurrentStatus;
    /* access modifiers changed from: private */
    public WifiDisplayController mDisplayController;
    private WifiDisplayDevice mDisplayDevice;
    private WifiDisplay[] mDisplays = WifiDisplay.EMPTY_ARRAY;
    /* access modifiers changed from: private */
    public int mFeatureState;
    /* access modifiers changed from: private */
    public final WifiDisplayHandler mHandler;
    IHwWifiDisplayAdapterEx mHwAdapterEx = null;
    private boolean mPendingStatusChangeBroadcast;
    /* access modifiers changed from: private */
    public final PersistentDataStore mPersistentDataStore;
    private WifiDisplay[] mRememberedDisplays = WifiDisplay.EMPTY_ARRAY;
    /* access modifiers changed from: private */
    public int mScanState;
    /* access modifiers changed from: private */
    public WifiDisplaySessionInfo mSessionInfo;
    private final boolean mSupportsProtectedBuffers;
    /* access modifiers changed from: private */
    public int mUibcCap = 0;
    /* access modifiers changed from: private */
    public IVRSystemServiceManager mVrMananger;
    /* access modifiers changed from: private */
    public final WifiDisplayController.Listener mWifiDisplayListener = new WifiDisplayController.Listener() {
        public void onFeatureStateChanged(int featureState) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (WifiDisplayAdapter.this.mFeatureState != featureState) {
                    int unused = WifiDisplayAdapter.this.mFeatureState = featureState;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onScanStarted() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (WifiDisplayAdapter.this.mScanState != 1) {
                    int unused = WifiDisplayAdapter.this.mScanState = 1;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onScanResults(WifiDisplay[] availableDisplays) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay[] availableDisplays2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAliases(availableDisplays);
                boolean changed = !Arrays.equals(WifiDisplayAdapter.this.mAvailableDisplays, availableDisplays2);
                int i = 0;
                while (!changed && i < availableDisplays2.length) {
                    changed = availableDisplays2[i].canConnect() != WifiDisplayAdapter.this.mAvailableDisplays[i].canConnect();
                    i++;
                }
                if (changed) {
                    WifiDisplay[] unused = WifiDisplayAdapter.this.mAvailableDisplays = availableDisplays2;
                    WifiDisplayAdapter.this.fixRememberedDisplayNamesFromAvailableDisplaysLocked();
                    WifiDisplayAdapter.this.updateDisplaysLocked();
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onScanFinished() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (WifiDisplayAdapter.this.mScanState != 0) {
                    int unused = WifiDisplayAdapter.this.mScanState = 0;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onDisplayConnecting(WifiDisplay display) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay display2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                if (WifiDisplayAdapter.this.mActiveDisplayState != 1 || WifiDisplayAdapter.this.mActiveDisplay == null || !WifiDisplayAdapter.this.mActiveDisplay.equals(display2)) {
                    int unused = WifiDisplayAdapter.this.mActiveDisplayState = 1;
                    WifiDisplay unused2 = WifiDisplayAdapter.this.mActiveDisplay = display2;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onDisplayConnectionFailed() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (!(WifiDisplayAdapter.this.mActiveDisplayState == 0 && WifiDisplayAdapter.this.mActiveDisplay == null)) {
                    int unused = WifiDisplayAdapter.this.mActiveDisplayState = 0;
                    WifiDisplay unused2 = WifiDisplayAdapter.this.mActiveDisplay = null;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onDisplayConnected(WifiDisplay display, Surface surface, int width, int height, int flags) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay display2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayRemembered(WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display));
                if ((flags & 256) != 0) {
                    WifiDisplayAdapter.this.mPersistentDataStore.addHdcpSupportedDevice(display2.getDeviceAddress());
                }
                WifiDisplayAdapter.this.addDisplayDeviceLocked(display2, surface, width, height, flags);
                if (WifiDisplayAdapter.this.mActiveDisplayState != 2 || WifiDisplayAdapter.this.mActiveDisplay == null || !WifiDisplayAdapter.this.mActiveDisplay.equals(display2)) {
                    int unused = WifiDisplayAdapter.this.mActiveDisplayState = 2;
                    WifiDisplay unused2 = WifiDisplayAdapter.this.mActiveDisplay = display2;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onDisplaySessionInfo(WifiDisplaySessionInfo sessionInfo) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplaySessionInfo unused = WifiDisplayAdapter.this.mSessionInfo = sessionInfo;
                WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
            }
        }

        public void onDisplayChanged(WifiDisplay display) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay display2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                if (WifiDisplayAdapter.this.mActiveDisplay != null && WifiDisplayAdapter.this.mActiveDisplay.hasSameAddress(display2) && !WifiDisplayAdapter.this.mActiveDisplay.equals(display2)) {
                    WifiDisplay unused = WifiDisplayAdapter.this.mActiveDisplay = display2;
                    WifiDisplayAdapter.this.renameDisplayDeviceLocked(display2.getFriendlyDisplayName());
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onDisplayDisconnected() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplayAdapter.this.removeDisplayDeviceLocked();
                if (!(WifiDisplayAdapter.this.mActiveDisplayState == 0 && WifiDisplayAdapter.this.mActiveDisplay == null)) {
                    int unused = WifiDisplayAdapter.this.mActiveDisplayState = 0;
                    WifiDisplay unused2 = WifiDisplayAdapter.this.mActiveDisplay = null;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        public void onSetConnectionFailedReason(int reason) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                Slog.d(WifiDisplayAdapter.TAG, "onSetConnectionFailedReason, reason=" + reason);
                int unused = WifiDisplayAdapter.this.mConnectionFailedReason = reason;
                if (WifiDisplayAdapter.this.mActiveDisplay != null && 8 == reason) {
                    WifiDisplayAdapter.this.mPersistentDataStore.addUibcExceptionDevice(WifiDisplayAdapter.this.mActiveDisplay.getDeviceAddress());
                }
            }
        }

        public void onDisplayCasting(WifiDisplay display) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay display2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                if (WifiDisplayAdapter.this.mActiveDisplayState != 2 || WifiDisplayAdapter.this.mActiveDisplay == null || !WifiDisplayAdapter.this.mActiveDisplay.equals(display2)) {
                    Slog.d(WifiDisplayAdapter.TAG, "onDisplayCasting mActiveDisplayState " + WifiDisplayAdapter.this.mActiveDisplayState);
                } else {
                    Slog.d(WifiDisplayAdapter.TAG, "onDisplayCasting .....");
                    WifiDisplayAdapter.this.mHandler.sendEmptyMessage(2);
                    WifiDisplayAdapter.this.LaunchMKForWifiMode();
                }
            }
        }

        public void onSetUibcInfo(int capSupport) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                int unused = WifiDisplayAdapter.this.mUibcCap = capSupport;
            }
        }
    };

    private final class WifiDisplayDevice extends DisplayDevice {
        private final String mAddress;
        private final int mFlags;
        private final int mHeight;
        private DisplayDeviceInfo mInfo;
        private final Display.Mode mMode;
        private String mName;
        private final float mRefreshRate;
        private Surface mSurface;
        private final int mWidth;

        public WifiDisplayDevice(IBinder displayToken, String name, int width, int height, float refreshRate, int flags, String address, Surface surface) {
            super(WifiDisplayAdapter.this, displayToken, WifiDisplayAdapter.DISPLAY_NAME_PREFIX + address);
            this.mName = name;
            this.mWidth = width;
            this.mHeight = height;
            this.mRefreshRate = refreshRate;
            this.mFlags = flags;
            this.mAddress = address;
            this.mSurface = surface;
            this.mMode = DisplayAdapter.createMode(width, height, refreshRate);
        }

        public boolean hasStableUniqueId() {
            return true;
        }

        public void destroyLocked() {
            if (this.mSurface != null) {
                this.mSurface.release();
                this.mSurface = null;
            }
            SurfaceControl.destroyDisplay(getDisplayTokenLocked());
        }

        public void setNameLocked(String name) {
            this.mName = name;
            this.mInfo = null;
        }

        public void performTraversalLocked(SurfaceControl.Transaction t) {
            if (this.mSurface != null) {
                setSurfaceLocked(t, this.mSurface);
            }
            Slog.w(WifiDisplayAdapter.TAG, "performTraversalInTransactionLocked: ");
            WifiDisplayAdapter.this.mVrMananger.mirrorVRDisplayIfNeed(getDisplayTokenLocked());
            if (!HwPCUtils.enabledInPad()) {
                return;
            }
            if (HwPCUtils.isPcCastModeInServer()) {
                int layerStack = HwPCUtils.getPCDisplayID();
                Slog.d(WifiDisplayAdapter.TAG, "performTraversalInTransactionLocked: setDisplayLayerStack layerStack = " + layerStack);
                SurfaceControl.setDisplayLayerStack(getDisplayTokenLocked(), layerStack);
                return;
            }
            SurfaceControl.setDisplayLayerStack(getDisplayTokenLocked(), 0);
        }

        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                this.mInfo = new DisplayDeviceInfo();
                this.mInfo.name = this.mName;
                this.mInfo.uniqueId = getUniqueId();
                this.mInfo.width = this.mWidth;
                this.mInfo.height = this.mHeight;
                this.mInfo.modeId = this.mMode.getModeId();
                this.mInfo.defaultModeId = this.mMode.getModeId();
                this.mInfo.supportedModes = new Display.Mode[]{this.mMode};
                this.mInfo.presentationDeadlineNanos = 1000000000 / ((long) ((int) this.mRefreshRate));
                this.mInfo.flags = this.mFlags;
                this.mInfo.type = 3;
                this.mInfo.address = this.mAddress;
                this.mInfo.touch = 2;
                if (HwPCUtils.enabled()) {
                    this.mInfo.densityDpi = ((this.mWidth < this.mHeight ? this.mWidth : this.mHeight) * 240) / 1080;
                    Slog.i(WifiDisplayAdapter.TAG, "PC mode densityDpi:" + this.mInfo.densityDpi);
                    this.mInfo.xDpi = (float) this.mInfo.densityDpi;
                    this.mInfo.yDpi = (float) this.mInfo.densityDpi;
                } else {
                    this.mInfo.setAssumedDensityForExternalDisplay(this.mWidth, this.mHeight);
                }
            }
            return this.mInfo;
        }
    }

    private final class WifiDisplayHandler extends Handler {
        public WifiDisplayHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WifiDisplayAdapter.this.handleSendStatusChangeBroadcast();
                    return;
                case 2:
                    WifiDisplayAdapter.this.handleSendCastingBroadcast();
                    return;
                default:
                    return;
            }
        }
    }

    public WifiDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, PersistentDataStore persistentDataStore) {
        super(syncRoot, context, handler, listener, TAG);
        this.mHandler = new WifiDisplayHandler(handler.getLooper());
        this.mPersistentDataStore = persistentDataStore;
        this.mSupportsProtectedBuffers = context.getResources().getBoolean(17957070);
        this.mHwAdapterEx = HwServiceFactory.getHwWifiDisplayAdapterEx(this);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    public void dumpLocked(PrintWriter pw) {
        super.dumpLocked(pw);
        pw.println("mCurrentStatus=" + getWifiDisplayStatusLocked());
        pw.println("mFeatureState=" + this.mFeatureState);
        pw.println("mScanState=" + this.mScanState);
        pw.println("mActiveDisplayState=" + this.mActiveDisplayState);
        pw.println("mActiveDisplay=" + this.mActiveDisplay);
        pw.println("mDisplays=" + Arrays.toString(this.mDisplays));
        pw.println("mAvailableDisplays=" + Arrays.toString(this.mAvailableDisplays));
        pw.println("mRememberedDisplays=" + Arrays.toString(this.mRememberedDisplays));
        pw.println("mPendingStatusChangeBroadcast=" + this.mPendingStatusChangeBroadcast);
        pw.println("mSupportsProtectedBuffers=" + this.mSupportsProtectedBuffers);
        if (this.mDisplayController == null) {
            pw.println("mDisplayController=null");
            return;
        }
        pw.println("mDisplayController:");
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.increaseIndent();
        DumpUtils.dumpAsync(getHandler(), this.mDisplayController, ipw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 200);
    }

    public void registerLocked() {
        super.registerLocked();
        updateRememberedDisplaysLocked();
        getHandler().post(new Runnable() {
            public void run() {
                WifiDisplayController unused = WifiDisplayAdapter.this.mDisplayController = new WifiDisplayController(WifiDisplayAdapter.this.getContext(), WifiDisplayAdapter.this.getHandler(), WifiDisplayAdapter.this.mWifiDisplayListener);
                WifiDisplayAdapter.this.getContext().registerReceiverAsUser(WifiDisplayAdapter.this.mBroadcastReceiver, UserHandle.ALL, new IntentFilter(WifiDisplayAdapter.ACTION_DISCONNECT), null, WifiDisplayAdapter.this.mHandler);
            }
        });
    }

    public void requestStartScanLocked() {
        getHandler().post(new Runnable() {
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestStartScan();
                }
            }
        });
    }

    public void requestStopScanLocked() {
        getHandler().post(new Runnable() {
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestStopScan();
                }
            }
        });
    }

    public void requestConnectLocked(final String address) {
        getHandler().post(new Runnable() {
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestConnect(address);
                    if (WifiDisplayAdapter.this.mHwAdapterEx != null) {
                        WifiDisplayAdapter.this.mHwAdapterEx.setConnectParameters(address);
                    }
                }
            }
        });
    }

    public void requestPauseLocked() {
        getHandler().post(new Runnable() {
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestPause();
                }
            }
        });
    }

    public void requestResumeLocked() {
        getHandler().post(new Runnable() {
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestResume();
                }
            }
        });
    }

    public void requestDisconnectLocked() {
        getHandler().post(new Runnable() {
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestDisconnect();
                }
            }
        });
    }

    public void requestRenameLocked(String address, String alias) {
        if (alias != null) {
            alias = alias.trim();
            if (alias.isEmpty() || alias.equals(address)) {
                alias = null;
            }
        }
        WifiDisplay display = this.mPersistentDataStore.getRememberedWifiDisplay(address);
        if (display != null && !Objects.equals(display.getDeviceAlias(), alias)) {
            WifiDisplay display2 = new WifiDisplay(address, display.getDeviceName(), alias, false, false, false);
            if (this.mPersistentDataStore.rememberWifiDisplay(display2)) {
                this.mPersistentDataStore.saveIfNeeded();
                updateRememberedDisplaysLocked();
                scheduleStatusChangedBroadcastLocked();
            }
        }
        if (this.mActiveDisplay != null && this.mActiveDisplay.getDeviceAddress().equals(address)) {
            renameDisplayDeviceLocked(this.mActiveDisplay.getFriendlyDisplayName());
        }
    }

    public void requestForgetLocked(String address) {
        if (this.mPersistentDataStore.forgetWifiDisplay(address)) {
            this.mPersistentDataStore.saveIfNeeded();
            updateRememberedDisplaysLocked();
            scheduleStatusChangedBroadcastLocked();
        }
        if (this.mActiveDisplay != null && this.mActiveDisplay.getDeviceAddress().equals(address)) {
            requestDisconnectLocked();
        }
    }

    public WifiDisplayStatus getWifiDisplayStatusLocked() {
        if (this.mCurrentStatus == null) {
            WifiDisplayStatus wifiDisplayStatus = new WifiDisplayStatus(this.mFeatureState, this.mScanState, this.mActiveDisplayState, this.mActiveDisplay, this.mDisplays, this.mSessionInfo);
            this.mCurrentStatus = wifiDisplayStatus;
        }
        return this.mCurrentStatus;
    }

    /* access modifiers changed from: private */
    public void updateDisplaysLocked() {
        List<WifiDisplay> displays = new ArrayList<>(this.mAvailableDisplays.length + this.mRememberedDisplays.length);
        boolean[] remembered = new boolean[this.mAvailableDisplays.length];
        for (WifiDisplay d : this.mRememberedDisplays) {
            boolean available = false;
            int i = 0;
            while (true) {
                if (i >= this.mAvailableDisplays.length) {
                    break;
                } else if (d.equals(this.mAvailableDisplays[i])) {
                    available = true;
                    remembered[i] = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!available) {
                WifiDisplay wifiDisplay = r9;
                WifiDisplay wifiDisplay2 = new WifiDisplay(d.getDeviceAddress(), d.getDeviceName(), d.getDeviceAlias(), false, false, true);
                displays.add(wifiDisplay);
            }
        }
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < this.mAvailableDisplays.length) {
                WifiDisplay d2 = this.mAvailableDisplays[i3];
                WifiDisplay wifiDisplay3 = new WifiDisplay(d2.getDeviceAddress(), d2.getDeviceName(), d2.getDeviceAlias(), true, d2.canConnect(), remembered[i3]);
                displays.add(wifiDisplay3);
                i2 = i3 + 1;
            } else {
                this.mDisplays = (WifiDisplay[]) displays.toArray(WifiDisplay.EMPTY_ARRAY);
                return;
            }
        }
    }

    private void updateRememberedDisplaysLocked() {
        this.mRememberedDisplays = this.mPersistentDataStore.getRememberedWifiDisplays();
        this.mActiveDisplay = this.mPersistentDataStore.applyWifiDisplayAlias(this.mActiveDisplay);
        this.mAvailableDisplays = this.mPersistentDataStore.applyWifiDisplayAliases(this.mAvailableDisplays);
        updateDisplaysLocked();
    }

    /* access modifiers changed from: private */
    public void fixRememberedDisplayNamesFromAvailableDisplaysLocked() {
        boolean changed = false;
        for (int i = 0; i < this.mRememberedDisplays.length; i++) {
            WifiDisplay rememberedDisplay = this.mRememberedDisplays[i];
            WifiDisplay availableDisplay = findAvailableDisplayLocked(rememberedDisplay.getDeviceAddress());
            if (availableDisplay != null && !rememberedDisplay.equals(availableDisplay)) {
                this.mRememberedDisplays[i] = availableDisplay;
                changed |= this.mPersistentDataStore.rememberWifiDisplay(availableDisplay);
            }
        }
        if (changed) {
            this.mPersistentDataStore.saveIfNeeded();
        }
    }

    private WifiDisplay findAvailableDisplayLocked(String address) {
        for (WifiDisplay display : this.mAvailableDisplays) {
            if (display.getDeviceAddress().equals(address)) {
                return display;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void addDisplayDeviceLocked(WifiDisplay display, Surface surface, int width, int height, int flags) {
        removeDisplayDeviceLocked();
        if (this.mPersistentDataStore.rememberWifiDisplay(display)) {
            this.mPersistentDataStore.saveIfNeeded();
            updateRememberedDisplaysLocked();
            scheduleStatusChangedBroadcastLocked();
        }
        boolean secure = (flags & 1) != 0;
        int deviceFlags = 64;
        if (secure) {
            deviceFlags = 64 | 4;
            if (this.mSupportsProtectedBuffers) {
                deviceFlags |= 8;
            }
        }
        String name = display.getFriendlyDisplayName();
        WifiDisplayDevice wifiDisplayDevice = r0;
        String str = name;
        WifiDisplayDevice wifiDisplayDevice2 = new WifiDisplayDevice(SurfaceControl.createDisplay(name, secure), name, width, height, 60.0f, deviceFlags, display.getDeviceAddress(), surface);
        this.mDisplayDevice = wifiDisplayDevice;
        sendDisplayDeviceEventLocked(this.mDisplayDevice, 1);
    }

    /* access modifiers changed from: private */
    public void removeDisplayDeviceLocked() {
        if (this.mDisplayDevice != null) {
            this.mDisplayDevice.destroyLocked();
            sendDisplayDeviceEventLocked(this.mDisplayDevice, 3);
            this.mDisplayDevice = null;
        }
    }

    /* access modifiers changed from: private */
    public void renameDisplayDeviceLocked(String name) {
        if (this.mDisplayDevice != null && !this.mDisplayDevice.getNameLocked().equals(name)) {
            this.mDisplayDevice.setNameLocked(name);
            sendDisplayDeviceEventLocked(this.mDisplayDevice, 2);
        }
    }

    /* access modifiers changed from: private */
    public void scheduleStatusChangedBroadcastLocked() {
        this.mCurrentStatus = null;
        if (!this.mPendingStatusChangeBroadcast) {
            this.mPendingStatusChangeBroadcast = true;
            this.mHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0056, code lost:
        getContext().sendBroadcastAsUser(r1, android.os.UserHandle.ALL);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0060, code lost:
        return;
     */
    public void handleSendStatusChangeBroadcast() {
        synchronized (getSyncRoot()) {
            if (this.mPendingStatusChangeBroadcast) {
                this.mPendingStatusChangeBroadcast = false;
                Intent intent = new Intent("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
                intent.addFlags(1073741824);
                intent.putExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS", getWifiDisplayStatusLocked());
                if (this.mConnectionFailedReason != -1) {
                    Slog.d(TAG, "handleSendStatusChangeBroadcast, connection failed reason is " + this.mConnectionFailedReason);
                    intent.putExtra("android.hardware.display.extra.WIFI_DISPLAY_CONN_FAILED_REASON", this.mConnectionFailedReason);
                    this.mConnectionFailedReason = -1;
                }
                if (this.mActiveDisplayState == 2) {
                    intent.putExtra(WIFI_DISPLAY_UIBC_INFO, this.mUibcCap);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSendCastingBroadcast() {
        Intent intent = new Intent(ACTION_WIFI_DISPLAY_CASTING);
        intent.addFlags(1073741824);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_DISPLAY_CASTING_PERMISSION);
    }

    public Handler getHandlerInner() {
        return getHandler();
    }

    public WifiDisplayController getmDisplayControllerInner() {
        return this.mDisplayController;
    }

    public PersistentDataStore getmPersistentDataStoreInner() {
        return this.mPersistentDataStore;
    }

    public void requestStartScanLocked(int channelID) {
        if (this.mHwAdapterEx != null) {
            this.mHwAdapterEx.requestStartScanLocked(channelID);
        }
    }

    public void requestConnectLocked(String address, String verificaitonCode) {
        if (this.mHwAdapterEx != null) {
            this.mHwAdapterEx.requestConnectLocked(address, verificaitonCode);
        }
    }

    public void checkVerificationResultLocked(boolean isRight) {
        if (this.mHwAdapterEx != null) {
            this.mHwAdapterEx.checkVerificationResultLocked(isRight);
        }
    }

    public void sendWifiDisplayActionLocked(String action) {
        if (this.mHwAdapterEx != null) {
            this.mHwAdapterEx.sendWifiDisplayActionLocked(action);
        }
    }

    public void LaunchMKForWifiMode() {
        if (this.mHwAdapterEx != null) {
            this.mHwAdapterEx.LaunchMKForWifiMode();
        }
    }
}
