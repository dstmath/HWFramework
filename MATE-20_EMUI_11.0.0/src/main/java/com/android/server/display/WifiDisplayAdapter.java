package com.android.server.display;

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
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayAddress;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.HwServiceFactory;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.WifiDisplayController;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/* access modifiers changed from: package-private */
public final class WifiDisplayAdapter extends DisplayAdapter implements IWifiDisplayAdapterInner {
    private static final String ACTION_DISCONNECT = "android.server.display.wfd.DISCONNECT";
    private static final int BASE_HIGH = 1080;
    private static final boolean DEBUG = false;
    private static final String DISPLAY_NAME_PREFIX = "wifi:";
    private static final int MSG_SEND_CAST_CHANGE_BROADCAST = 2;
    private static final int MSG_SEND_DISPLAY_DATA_BROADCAST = 3;
    private static final int MSG_SEND_STATUS_CHANGE_BROADCAST = 1;
    private static final String TAG = "WifiDisplayAdapter";
    private static final String WIFI_DISPLAY_UIBC_INFO = "com.huawei.hardware.display.action.WIFI_DISPLAY_UIBC_INFO";
    private WifiDisplay mActiveDisplay;
    private int mActiveDisplayState;
    private WifiDisplay[] mAvailableDisplays = WifiDisplay.EMPTY_ARRAY;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass8 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiDisplayAdapter.ACTION_DISCONNECT)) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    WifiDisplayAdapter.this.requestDisconnectLocked();
                }
            }
        }
    };
    private WifiDisplayStatus mCurrentStatus;
    private WifiDisplayController mDisplayController;
    private WifiDisplayDevice mDisplayDevice;
    private WifiDisplay[] mDisplays = WifiDisplay.EMPTY_ARRAY;
    private int mFeatureState;
    private final WifiDisplayHandler mHandler;
    IHwWifiDisplayAdapterEx mHwAdapterEx = null;
    private boolean mPendingStatusChangeBroadcast;
    private final PersistentDataStore mPersistentDataStore;
    private WifiDisplay[] mRememberedDisplays = WifiDisplay.EMPTY_ARRAY;
    private int mScanState;
    private WifiDisplaySessionInfo mSessionInfo;
    private final boolean mSupportsProtectedBuffers;
    private int mUibcCap = 0;
    private final WifiDisplayController.Listener mWifiDisplayListener = new WifiDisplayController.Listener() {
        /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass9 */

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onFeatureStateChanged(int featureState) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (WifiDisplayAdapter.this.mFeatureState != featureState) {
                    WifiDisplayAdapter.this.mFeatureState = featureState;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onScanStarted() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (WifiDisplayAdapter.this.mScanState != 1) {
                    WifiDisplayAdapter.this.mScanState = 1;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
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
                    WifiDisplayAdapter.this.mAvailableDisplays = availableDisplays2;
                    WifiDisplayAdapter.this.fixRememberedDisplayNamesFromAvailableDisplaysLocked();
                    WifiDisplayAdapter.this.updateDisplaysLocked();
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onScanFinished() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (WifiDisplayAdapter.this.mScanState != 0) {
                    WifiDisplayAdapter.this.mScanState = 0;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplayConnecting(WifiDisplay display) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay display2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                if (WifiDisplayAdapter.this.mActiveDisplayState != 1 || WifiDisplayAdapter.this.mActiveDisplay == null || !WifiDisplayAdapter.this.mActiveDisplay.equals(display2)) {
                    WifiDisplayAdapter.this.mActiveDisplayState = 1;
                    WifiDisplayAdapter.this.mActiveDisplay = display2;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplayConnectionFailed() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (!(WifiDisplayAdapter.this.mActiveDisplayState == 0 && WifiDisplayAdapter.this.mActiveDisplay == null)) {
                    WifiDisplayAdapter.this.mActiveDisplayState = 0;
                    WifiDisplayAdapter.this.mActiveDisplay = null;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplayConnected(WifiDisplay display, Surface surface, int width, int height, int flags) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay display2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayRemembered(WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display));
                if ((flags & 256) != 0) {
                    WifiDisplayAdapter.this.mPersistentDataStore.addHdcpSupportedDevice(display2.getDeviceAddress());
                }
                WifiDisplayAdapter.this.addDisplayDeviceLocked(display2, surface, width, height, flags);
                if (WifiDisplayAdapter.this.mActiveDisplayState != 2 || WifiDisplayAdapter.this.mActiveDisplay == null || !WifiDisplayAdapter.this.mActiveDisplay.equals(display2)) {
                    WifiDisplayAdapter.this.mActiveDisplayState = 2;
                    WifiDisplayAdapter.this.mActiveDisplay = display2;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplaySessionInfo(WifiDisplaySessionInfo sessionInfo) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplayAdapter.this.mSessionInfo = sessionInfo;
                WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplayChanged(WifiDisplay display) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplay display2 = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                if (WifiDisplayAdapter.this.mActiveDisplay != null && WifiDisplayAdapter.this.mActiveDisplay.hasSameAddress(display2) && !WifiDisplayAdapter.this.mActiveDisplay.equals(display2)) {
                    WifiDisplayAdapter.this.mActiveDisplay = display2;
                    WifiDisplayAdapter.this.renameDisplayDeviceLocked(display2.getFriendlyDisplayName());
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplayDisconnected() {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplayAdapter.this.removeDisplayDeviceLocked();
                if (!(WifiDisplayAdapter.this.mActiveDisplayState == 0 && WifiDisplayAdapter.this.mActiveDisplay == null)) {
                    WifiDisplayAdapter.this.mActiveDisplayState = 0;
                    WifiDisplayAdapter.this.mActiveDisplay = null;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
                if (WifiDisplayAdapter.this.mHwAdapterEx != null) {
                    WifiDisplayAdapter.this.mHwAdapterEx.setHwWifiDisplayParameters(null);
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onSetConnectionFailedReason(int reason) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                Slog.d(WifiDisplayAdapter.TAG, "onSetConnectionFailedReason, reason=" + reason);
                if (WifiDisplayAdapter.this.mHwAdapterEx != null) {
                    WifiDisplayAdapter.this.mHwAdapterEx.setConnectionFailedReason(reason);
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplayCasting(WifiDisplay display) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                if (WifiDisplayAdapter.this.mHwAdapterEx != null) {
                    WifiDisplayAdapter.this.mHwAdapterEx.displayCasting(display);
                }
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onSetUibcInfo(int capSupport) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                WifiDisplayAdapter.this.mUibcCap = capSupport;
            }
        }

        @Override // com.android.server.display.WifiDisplayController.Listener
        public void onDisplayDataInfo(String inputDataType) {
            synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                Message msg = new Message();
                msg.what = 3;
                msg.obj = inputDataType;
                WifiDisplayAdapter.this.mHandler.sendMessage(msg);
            }
        }
    };

    public WifiDisplayAdapter(DisplayManagerService.SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, PersistentDataStore persistentDataStore) {
        super(syncRoot, context, handler, listener, TAG);
        this.mHandler = new WifiDisplayHandler(handler.getLooper());
        this.mPersistentDataStore = persistentDataStore;
        this.mSupportsProtectedBuffers = context.getResources().getBoolean(17891575);
        this.mHwAdapterEx = HwServiceFactory.getHwWifiDisplayAdapterEx(this, context);
    }

    @Override // com.android.server.display.DisplayAdapter
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
        DumpUtils.dumpAsync(getHandler(), this.mDisplayController, ipw, "", 200);
    }

    @Override // com.android.server.display.DisplayAdapter
    public void registerLocked() {
        super.registerLocked();
        updateRememberedDisplaysLocked();
        getHandler().post(new Runnable() {
            /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                WifiDisplayAdapter wifiDisplayAdapter = WifiDisplayAdapter.this;
                wifiDisplayAdapter.mDisplayController = new WifiDisplayController(wifiDisplayAdapter.getContext(), WifiDisplayAdapter.this.getHandler(), WifiDisplayAdapter.this.mWifiDisplayListener);
                WifiDisplayAdapter.this.getContext().registerReceiverAsUser(WifiDisplayAdapter.this.mBroadcastReceiver, UserHandle.ALL, new IntentFilter(WifiDisplayAdapter.ACTION_DISCONNECT), null, WifiDisplayAdapter.this.mHandler);
            }
        });
    }

    public void requestStartScanLocked() {
        getHandler().post(new Runnable() {
            /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestStartScan();
                }
            }
        });
    }

    public void requestStopScanLocked() {
        getHandler().post(new Runnable() {
            /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestStopScan();
                }
            }
        });
    }

    public void requestConnectLocked(final String address) {
        getHandler().post(new Runnable() {
            /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass4 */

            @Override // java.lang.Runnable
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
            /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestPause();
                }
            }
        });
    }

    public void requestResumeLocked() {
        getHandler().post(new Runnable() {
            /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                if (WifiDisplayAdapter.this.mDisplayController != null) {
                    WifiDisplayAdapter.this.mDisplayController.requestResume();
                }
            }
        });
    }

    public void requestDisconnectLocked() {
        getHandler().post(new Runnable() {
            /* class com.android.server.display.WifiDisplayAdapter.AnonymousClass7 */

            @Override // java.lang.Runnable
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
            if (this.mPersistentDataStore.rememberWifiDisplay(new WifiDisplay(address, display.getDeviceName(), alias, false, false, false))) {
                this.mPersistentDataStore.saveIfNeeded();
                updateRememberedDisplaysLocked();
                scheduleStatusChangedBroadcastLocked();
            }
        }
        WifiDisplay wifiDisplay = this.mActiveDisplay;
        if (wifiDisplay != null && wifiDisplay.getDeviceAddress().equals(address)) {
            renameDisplayDeviceLocked(this.mActiveDisplay.getFriendlyDisplayName());
        }
    }

    public void requestForgetLocked(String address) {
        if (this.mPersistentDataStore.forgetWifiDisplay(address)) {
            this.mPersistentDataStore.saveIfNeeded();
            updateRememberedDisplaysLocked();
            scheduleStatusChangedBroadcastLocked();
        }
        WifiDisplay wifiDisplay = this.mActiveDisplay;
        if (wifiDisplay != null && wifiDisplay.getDeviceAddress().equals(address)) {
            requestDisconnectLocked();
        }
    }

    public WifiDisplayStatus getWifiDisplayStatusLocked() {
        if (this.mCurrentStatus == null) {
            this.mCurrentStatus = new WifiDisplayStatus(this.mFeatureState, this.mScanState, this.mActiveDisplayState, this.mActiveDisplay, this.mDisplays, this.mSessionInfo);
        }
        return this.mCurrentStatus;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisplaysLocked() {
        List<WifiDisplay> displays = new ArrayList<>(this.mAvailableDisplays.length + this.mRememberedDisplays.length);
        boolean[] remembered = new boolean[this.mAvailableDisplays.length];
        WifiDisplay[] wifiDisplayArr = this.mRememberedDisplays;
        for (WifiDisplay d : wifiDisplayArr) {
            boolean available = false;
            int i = 0;
            while (true) {
                WifiDisplay[] wifiDisplayArr2 = this.mAvailableDisplays;
                if (i >= wifiDisplayArr2.length) {
                    break;
                } else if (d.equals(wifiDisplayArr2[i])) {
                    available = true;
                    remembered[i] = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!available) {
                displays.add(new WifiDisplay(d.getDeviceAddress(), d.getDeviceName(), d.getDeviceAlias(), false, false, true));
            }
        }
        int i2 = 0;
        while (true) {
            WifiDisplay[] wifiDisplayArr3 = this.mAvailableDisplays;
            if (i2 < wifiDisplayArr3.length) {
                WifiDisplay d2 = wifiDisplayArr3[i2];
                displays.add(new WifiDisplay(d2.getDeviceAddress(), d2.getDeviceName(), d2.getDeviceAlias(), true, d2.canConnect(), remembered[i2]));
                i2++;
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
    /* access modifiers changed from: public */
    private void fixRememberedDisplayNamesFromAvailableDisplaysLocked() {
        boolean changed = false;
        int i = 0;
        while (true) {
            WifiDisplay[] wifiDisplayArr = this.mRememberedDisplays;
            if (i >= wifiDisplayArr.length) {
                break;
            }
            WifiDisplay rememberedDisplay = wifiDisplayArr[i];
            WifiDisplay availableDisplay = findAvailableDisplayLocked(rememberedDisplay.getDeviceAddress());
            if (availableDisplay != null && !rememberedDisplay.equals(availableDisplay)) {
                this.mRememberedDisplays[i] = availableDisplay;
                changed |= this.mPersistentDataStore.rememberWifiDisplay(availableDisplay);
            }
            i++;
        }
        if (changed) {
            this.mPersistentDataStore.saveIfNeeded();
        }
    }

    private WifiDisplay findAvailableDisplayLocked(String address) {
        WifiDisplay[] wifiDisplayArr = this.mAvailableDisplays;
        for (WifiDisplay display : wifiDisplayArr) {
            if (display.getDeviceAddress().equals(address)) {
                return display;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addDisplayDeviceLocked(WifiDisplay display, Surface surface, int width, int height, int flags) {
        int deviceFlags;
        removeDisplayDeviceLocked();
        if (this.mPersistentDataStore.rememberWifiDisplay(display)) {
            this.mPersistentDataStore.saveIfNeeded();
            updateRememberedDisplaysLocked();
            scheduleStatusChangedBroadcastLocked();
        }
        boolean secure = (flags & 1) != 0;
        if (secure) {
            int deviceFlags2 = 64 | 4;
            if (this.mSupportsProtectedBuffers) {
                deviceFlags = deviceFlags2 | 8;
            } else {
                deviceFlags = deviceFlags2;
            }
        } else {
            deviceFlags = 64;
        }
        String name = display.getFriendlyDisplayName();
        this.mDisplayDevice = new WifiDisplayDevice(SurfaceControl.createDisplay(name, secure), name, width, height, 60.0f, deviceFlags, display.getDeviceAddress(), surface);
        sendDisplayDeviceEventLocked(this.mDisplayDevice, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeDisplayDeviceLocked() {
        WifiDisplayDevice wifiDisplayDevice = this.mDisplayDevice;
        if (wifiDisplayDevice != null) {
            wifiDisplayDevice.destroyLocked();
            sendDisplayDeviceEventLocked(this.mDisplayDevice, 3);
            this.mDisplayDevice = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void renameDisplayDeviceLocked(String name) {
        WifiDisplayDevice wifiDisplayDevice = this.mDisplayDevice;
        if (wifiDisplayDevice != null && !wifiDisplayDevice.getNameLocked().equals(name)) {
            this.mDisplayDevice.setNameLocked(name);
            sendDisplayDeviceEventLocked(this.mDisplayDevice, 2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleStatusChangedBroadcastLocked() {
        this.mCurrentStatus = null;
        if (!this.mPendingStatusChangeBroadcast) {
            this.mPendingStatusChangeBroadcast = true;
            this.mHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendStatusChangeBroadcast() {
        synchronized (getSyncRoot()) {
            if (this.mPendingStatusChangeBroadcast) {
                this.mPendingStatusChangeBroadcast = false;
                Intent intent = new Intent("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
                intent.addFlags(1073741824);
                intent.putExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS", (Parcelable) getWifiDisplayStatusLocked());
                if (this.mHwAdapterEx != null) {
                    intent.putExtra("android.hardware.display.extra.WIFI_DISPLAY_CONN_FAILED_REASON", this.mHwAdapterEx.getConnectionFailReason(true));
                }
                if (this.mActiveDisplayState == 2) {
                    intent.putExtra(WIFI_DISPLAY_UIBC_INFO, this.mUibcCap);
                }
                getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class WifiDisplayDevice extends DisplayDevice {
        private final DisplayAddress mAddress;
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
            this.mAddress = DisplayAddress.fromMacAddress(address);
            this.mSurface = surface;
            this.mMode = DisplayAdapter.createMode(width, height, refreshRate);
        }

        @Override // com.android.server.display.DisplayDevice
        public boolean hasStableUniqueId() {
            return true;
        }

        public void destroyLocked() {
            Surface surface = this.mSurface;
            if (surface != null) {
                surface.release();
                this.mSurface = null;
            }
            SurfaceControl.destroyDisplay(getDisplayTokenLocked());
        }

        public void setNameLocked(String name) {
            this.mName = name;
            this.mInfo = null;
        }

        @Override // com.android.server.display.DisplayDevice
        public void performTraversalLocked(SurfaceControl.Transaction t) {
            Surface surface = this.mSurface;
            if (surface != null) {
                setSurfaceLocked(t, surface);
            }
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

        @Override // com.android.server.display.DisplayDevice
        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (this.mInfo == null) {
                this.mInfo = new DisplayDeviceInfo();
                DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                displayDeviceInfo.name = this.mName;
                displayDeviceInfo.uniqueId = getUniqueId();
                DisplayDeviceInfo displayDeviceInfo2 = this.mInfo;
                displayDeviceInfo2.width = this.mWidth;
                displayDeviceInfo2.height = this.mHeight;
                displayDeviceInfo2.modeId = this.mMode.getModeId();
                this.mInfo.defaultModeId = this.mMode.getModeId();
                DisplayDeviceInfo displayDeviceInfo3 = this.mInfo;
                displayDeviceInfo3.supportedModes = new Display.Mode[]{this.mMode};
                displayDeviceInfo3.presentationDeadlineNanos = 1000000000 / ((long) ((int) this.mRefreshRate));
                displayDeviceInfo3.flags = this.mFlags;
                displayDeviceInfo3.type = 3;
                displayDeviceInfo3.address = this.mAddress;
                displayDeviceInfo3.touch = 2;
                displayDeviceInfo3.setAssumedDensityForExternalDisplay(this.mWidth, this.mHeight);
                if (WifiDisplayAdapter.this.mHwAdapterEx != null) {
                    WifiDisplayAdapter.this.mHwAdapterEx.updateDensityForPcMode(this.mInfo);
                }
            }
            return this.mInfo;
        }
    }

    /* access modifiers changed from: private */
    public final class WifiDisplayHandler extends Handler {
        public WifiDisplayHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                WifiDisplayAdapter.this.handleSendStatusChangeBroadcast();
            } else if (i != 2) {
                if (i == 3 && WifiDisplayAdapter.this.mHwAdapterEx != null) {
                    WifiDisplayAdapter.this.mHwAdapterEx.handleSendDisplayDataBroadcast(String.valueOf(msg.obj));
                }
            } else if (WifiDisplayAdapter.this.mHwAdapterEx != null) {
                WifiDisplayAdapter.this.mHwAdapterEx.handleSendCastingBroadcast();
            }
        }
    }

    @Override // com.android.server.display.IWifiDisplayAdapterInner
    public Handler getHandlerInner() {
        return this.mHandler;
    }

    @Override // com.android.server.display.IWifiDisplayAdapterInner
    public WifiDisplay getmActiveDisplayInner() {
        return this.mActiveDisplay;
    }

    @Override // com.android.server.display.IWifiDisplayAdapterInner
    public WifiDisplayController getmDisplayControllerInner() {
        return this.mDisplayController;
    }

    @Override // com.android.server.display.IWifiDisplayAdapterInner
    public PersistentDataStore getmPersistentDataStoreInner() {
        return this.mPersistentDataStore;
    }

    @Override // com.android.server.display.IWifiDisplayAdapterInner
    public int getmActiveDisplayStateInner() {
        return this.mActiveDisplayState;
    }

    public void requestStartScanLocked(int channelID) {
        IHwWifiDisplayAdapterEx iHwWifiDisplayAdapterEx = this.mHwAdapterEx;
        if (iHwWifiDisplayAdapterEx != null) {
            iHwWifiDisplayAdapterEx.requestStartScanLocked(channelID);
        }
    }

    public void requestConnectLocked(String address, HwWifiDisplayParameters parameters) {
        IHwWifiDisplayAdapterEx iHwWifiDisplayAdapterEx = this.mHwAdapterEx;
        if (iHwWifiDisplayAdapterEx != null) {
            iHwWifiDisplayAdapterEx.requestConnectLocked(address, parameters);
        }
    }

    public void checkVerificationResultLocked(boolean isRight) {
        IHwWifiDisplayAdapterEx iHwWifiDisplayAdapterEx = this.mHwAdapterEx;
        if (iHwWifiDisplayAdapterEx != null) {
            iHwWifiDisplayAdapterEx.checkVerificationResultLocked(isRight);
        }
    }

    public void sendWifiDisplayActionLocked(String action) {
        IHwWifiDisplayAdapterEx iHwWifiDisplayAdapterEx = this.mHwAdapterEx;
        if (iHwWifiDisplayAdapterEx != null) {
            iHwWifiDisplayAdapterEx.sendWifiDisplayActionLocked(action);
        }
    }
}
