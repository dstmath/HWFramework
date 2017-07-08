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
import android.os.UserHandle;
import android.view.Display.Mode;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.display.DisplayManagerService.SyncRoot;
import com.android.server.display.WifiDisplayController.Listener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.util.Objects;

final class WifiDisplayAdapter extends DisplayAdapter {
    private static final String ACTION_DISCONNECT = "android.server.display.wfd.DISCONNECT";
    private static final boolean DEBUG = false;
    private static final String DISPLAY_NAME_PREFIX = "wifi:";
    private static final int MSG_SEND_STATUS_CHANGE_BROADCAST = 1;
    private static final String TAG = "WifiDisplayAdapter";
    private WifiDisplay mActiveDisplay;
    private int mActiveDisplayState;
    private WifiDisplay[] mAvailableDisplays;
    private final BroadcastReceiver mBroadcastReceiver;
    private WifiDisplayStatus mCurrentStatus;
    private WifiDisplayController mDisplayController;
    private WifiDisplayDevice mDisplayDevice;
    private WifiDisplay[] mDisplays;
    private int mFeatureState;
    private final WifiDisplayHandler mHandler;
    private boolean mPendingStatusChangeBroadcast;
    private final PersistentDataStore mPersistentDataStore;
    private WifiDisplay[] mRememberedDisplays;
    private int mScanState;
    private WifiDisplaySessionInfo mSessionInfo;
    private final boolean mSupportsProtectedBuffers;
    private final Listener mWifiDisplayListener;

    /* renamed from: com.android.server.display.WifiDisplayAdapter.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ String val$address;

        AnonymousClass6(String val$address) {
            this.val$address = val$address;
        }

        public void run() {
            if (WifiDisplayAdapter.this.mDisplayController != null) {
                WifiDisplayAdapter.this.mDisplayController.requestConnect(this.val$address);
            }
        }
    }

    private final class WifiDisplayDevice extends DisplayDevice {
        private final String mAddress;
        private final int mFlags;
        private final int mHeight;
        private DisplayDeviceInfo mInfo;
        private final Mode mMode;
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

        public void performTraversalInTransactionLocked() {
            if (this.mSurface != null) {
                setSurfaceInTransactionLocked(this.mSurface);
            }
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
                DisplayDeviceInfo displayDeviceInfo = this.mInfo;
                Mode[] modeArr = new Mode[WifiDisplayAdapter.MSG_SEND_STATUS_CHANGE_BROADCAST];
                modeArr[0] = this.mMode;
                displayDeviceInfo.supportedModes = modeArr;
                this.mInfo.presentationDeadlineNanos = 1000000000 / ((long) ((int) this.mRefreshRate));
                this.mInfo.flags = this.mFlags;
                this.mInfo.type = 3;
                this.mInfo.address = this.mAddress;
                this.mInfo.touch = 2;
                this.mInfo.setAssumedDensityForExternalDisplay(this.mWidth, this.mHeight);
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
                case WifiDisplayAdapter.MSG_SEND_STATUS_CHANGE_BROADCAST /*1*/:
                    WifiDisplayAdapter.this.handleSendStatusChangeBroadcast();
                default:
            }
        }
    }

    public WifiDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener listener, PersistentDataStore persistentDataStore) {
        super(syncRoot, context, handler, listener, TAG);
        this.mDisplays = WifiDisplay.EMPTY_ARRAY;
        this.mAvailableDisplays = WifiDisplay.EMPTY_ARRAY;
        this.mRememberedDisplays = WifiDisplay.EMPTY_ARRAY;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WifiDisplayAdapter.ACTION_DISCONNECT)) {
                    synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                        WifiDisplayAdapter.this.requestDisconnectLocked();
                    }
                }
            }
        };
        this.mWifiDisplayListener = new Listener() {
            public void onFeatureStateChanged(int featureState) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    if (WifiDisplayAdapter.this.mFeatureState != featureState) {
                        WifiDisplayAdapter.this.mFeatureState = featureState;
                        WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                    }
                }
            }

            public void onScanStarted() {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    if (WifiDisplayAdapter.this.mScanState != WifiDisplayAdapter.MSG_SEND_STATUS_CHANGE_BROADCAST) {
                        WifiDisplayAdapter.this.mScanState = WifiDisplayAdapter.MSG_SEND_STATUS_CHANGE_BROADCAST;
                        WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                    }
                }
            }

            public void onScanResults(WifiDisplay[] availableDisplays) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    availableDisplays = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAliases(availableDisplays);
                    boolean changed = Arrays.equals(WifiDisplayAdapter.this.mAvailableDisplays, availableDisplays) ? WifiDisplayAdapter.DEBUG : true;
                    int i = 0;
                    while (!changed && i < availableDisplays.length) {
                        changed = availableDisplays[i].canConnect() != WifiDisplayAdapter.this.mAvailableDisplays[i].canConnect() ? true : WifiDisplayAdapter.DEBUG;
                        i += WifiDisplayAdapter.MSG_SEND_STATUS_CHANGE_BROADCAST;
                    }
                    if (changed) {
                        WifiDisplayAdapter.this.mAvailableDisplays = availableDisplays;
                        WifiDisplayAdapter.this.fixRememberedDisplayNamesFromAvailableDisplaysLocked();
                        WifiDisplayAdapter.this.updateDisplaysLocked();
                        WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                    }
                }
            }

            public void onScanFinished() {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    if (WifiDisplayAdapter.this.mScanState != 0) {
                        WifiDisplayAdapter.this.mScanState = 0;
                        WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                    }
                }
            }

            public void onDisplayConnecting(WifiDisplay display) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    display = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                    if (WifiDisplayAdapter.this.mActiveDisplayState == WifiDisplayAdapter.MSG_SEND_STATUS_CHANGE_BROADCAST && WifiDisplayAdapter.this.mActiveDisplay != null) {
                        if (WifiDisplayAdapter.this.mActiveDisplay.equals(display)) {
                        }
                    }
                    WifiDisplayAdapter.this.mActiveDisplayState = WifiDisplayAdapter.MSG_SEND_STATUS_CHANGE_BROADCAST;
                    WifiDisplayAdapter.this.mActiveDisplay = display;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }

            public void onDisplayConnectionFailed() {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    if (!(WifiDisplayAdapter.this.mActiveDisplayState == 0 && WifiDisplayAdapter.this.mActiveDisplay == null)) {
                        WifiDisplayAdapter.this.mActiveDisplayState = 0;
                        WifiDisplayAdapter.this.mActiveDisplay = null;
                        WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                    }
                }
            }

            public void onDisplayConnected(WifiDisplay display, Surface surface, int width, int height, int flags) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    display = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                    WifiDisplayAdapter.this.addDisplayDeviceLocked(display, surface, width, height, flags);
                    if (WifiDisplayAdapter.this.mActiveDisplayState == 2 && WifiDisplayAdapter.this.mActiveDisplay != null) {
                        if (WifiDisplayAdapter.this.mActiveDisplay.equals(display)) {
                        }
                    }
                    WifiDisplayAdapter.this.mActiveDisplayState = 2;
                    WifiDisplayAdapter.this.mActiveDisplay = display;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }

            public void onDisplaySessionInfo(WifiDisplaySessionInfo sessionInfo) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    WifiDisplayAdapter.this.mSessionInfo = sessionInfo;
                    WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                }
            }

            public void onDisplayChanged(WifiDisplay display) {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    display = WifiDisplayAdapter.this.mPersistentDataStore.applyWifiDisplayAlias(display);
                    if (!(WifiDisplayAdapter.this.mActiveDisplay == null || !WifiDisplayAdapter.this.mActiveDisplay.hasSameAddress(display) || WifiDisplayAdapter.this.mActiveDisplay.equals(display))) {
                        WifiDisplayAdapter.this.mActiveDisplay = display;
                        WifiDisplayAdapter.this.renameDisplayDeviceLocked(display.getFriendlyDisplayName());
                        WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                    }
                }
            }

            public void onDisplayDisconnected() {
                synchronized (WifiDisplayAdapter.this.getSyncRoot()) {
                    WifiDisplayAdapter.this.removeDisplayDeviceLocked();
                    if (!(WifiDisplayAdapter.this.mActiveDisplayState == 0 && WifiDisplayAdapter.this.mActiveDisplay == null)) {
                        WifiDisplayAdapter.this.mActiveDisplayState = 0;
                        WifiDisplayAdapter.this.mActiveDisplay = null;
                        WifiDisplayAdapter.this.scheduleStatusChangedBroadcastLocked();
                    }
                }
            }
        };
        this.mHandler = new WifiDisplayHandler(handler.getLooper());
        this.mPersistentDataStore = persistentDataStore;
        this.mSupportsProtectedBuffers = context.getResources().getBoolean(17956989);
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
        DumpUtils.dumpAsync(getHandler(), this.mDisplayController, ipw, "", 200);
    }

    public void registerLocked() {
        super.registerLocked();
        updateRememberedDisplaysLocked();
        getHandler().post(new Runnable() {
            public void run() {
                WifiDisplayAdapter.this.mDisplayController = new WifiDisplayController(WifiDisplayAdapter.this.getContext(), WifiDisplayAdapter.this.getHandler(), WifiDisplayAdapter.this.mWifiDisplayListener);
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

    public void requestConnectLocked(String address) {
        getHandler().post(new AnonymousClass6(address));
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
        if (display == null || Objects.equal(display.getDeviceAlias(), alias)) {
        } else {
            if (this.mPersistentDataStore.rememberWifiDisplay(new WifiDisplay(address, display.getDeviceName(), alias, DEBUG, DEBUG, DEBUG))) {
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
            this.mCurrentStatus = new WifiDisplayStatus(this.mFeatureState, this.mScanState, this.mActiveDisplayState, this.mActiveDisplay, this.mDisplays, this.mSessionInfo);
        }
        return this.mCurrentStatus;
    }

    private void updateDisplaysLocked() {
        List<WifiDisplay> displays = new ArrayList(this.mAvailableDisplays.length + this.mRememberedDisplays.length);
        boolean[] remembered = new boolean[this.mAvailableDisplays.length];
        WifiDisplay[] wifiDisplayArr = this.mRememberedDisplays;
        int length = wifiDisplayArr.length;
        for (int i = 0; i < length; i += MSG_SEND_STATUS_CHANGE_BROADCAST) {
            int i2;
            WifiDisplay d = wifiDisplayArr[i];
            boolean available = DEBUG;
            for (i2 = 0; i2 < this.mAvailableDisplays.length; i2 += MSG_SEND_STATUS_CHANGE_BROADCAST) {
                if (d.equals(this.mAvailableDisplays[i2])) {
                    available = true;
                    remembered[i2] = true;
                    break;
                }
            }
            if (!available) {
                displays.add(new WifiDisplay(d.getDeviceAddress(), d.getDeviceName(), d.getDeviceAlias(), DEBUG, DEBUG, true));
            }
        }
        for (i2 = 0; i2 < this.mAvailableDisplays.length; i2 += MSG_SEND_STATUS_CHANGE_BROADCAST) {
            d = this.mAvailableDisplays[i2];
            displays.add(new WifiDisplay(d.getDeviceAddress(), d.getDeviceName(), d.getDeviceAlias(), true, d.canConnect(), remembered[i2]));
        }
        this.mDisplays = (WifiDisplay[]) displays.toArray(WifiDisplay.EMPTY_ARRAY);
    }

    private void updateRememberedDisplaysLocked() {
        this.mRememberedDisplays = this.mPersistentDataStore.getRememberedWifiDisplays();
        this.mActiveDisplay = this.mPersistentDataStore.applyWifiDisplayAlias(this.mActiveDisplay);
        this.mAvailableDisplays = this.mPersistentDataStore.applyWifiDisplayAliases(this.mAvailableDisplays);
        updateDisplaysLocked();
    }

    private void fixRememberedDisplayNamesFromAvailableDisplaysLocked() {
        boolean changed = DEBUG;
        for (int i = 0; i < this.mRememberedDisplays.length; i += MSG_SEND_STATUS_CHANGE_BROADCAST) {
            WifiDisplay rememberedDisplay = this.mRememberedDisplays[i];
            WifiDisplay availableDisplay = findAvailableDisplayLocked(rememberedDisplay.getDeviceAddress());
            if (!(availableDisplay == null || rememberedDisplay.equals(availableDisplay))) {
                this.mRememberedDisplays[i] = availableDisplay;
                changed |= this.mPersistentDataStore.rememberWifiDisplay(availableDisplay);
            }
        }
        if (changed) {
            this.mPersistentDataStore.saveIfNeeded();
        }
    }

    private WifiDisplay findAvailableDisplayLocked(String address) {
        WifiDisplay[] wifiDisplayArr = this.mAvailableDisplays;
        int length = wifiDisplayArr.length;
        for (int i = 0; i < length; i += MSG_SEND_STATUS_CHANGE_BROADCAST) {
            WifiDisplay display = wifiDisplayArr[i];
            if (display.getDeviceAddress().equals(address)) {
                return display;
            }
        }
        return null;
    }

    private void addDisplayDeviceLocked(WifiDisplay display, Surface surface, int width, int height, int flags) {
        removeDisplayDeviceLocked();
        if (this.mPersistentDataStore.rememberWifiDisplay(display)) {
            this.mPersistentDataStore.saveIfNeeded();
            updateRememberedDisplaysLocked();
            scheduleStatusChangedBroadcastLocked();
        }
        boolean secure = (flags & MSG_SEND_STATUS_CHANGE_BROADCAST) != 0 ? true : DEBUG;
        int deviceFlags = 64;
        if (secure) {
            deviceFlags = 68;
            if (this.mSupportsProtectedBuffers) {
                deviceFlags = 68 | 8;
            }
        }
        String name = display.getFriendlyDisplayName();
        this.mDisplayDevice = new WifiDisplayDevice(SurfaceControl.createDisplay(name, secure), name, width, height, 60.0f, deviceFlags, display.getDeviceAddress(), surface);
        sendDisplayDeviceEventLocked(this.mDisplayDevice, MSG_SEND_STATUS_CHANGE_BROADCAST);
    }

    private void removeDisplayDeviceLocked() {
        if (this.mDisplayDevice != null) {
            this.mDisplayDevice.destroyLocked();
            sendDisplayDeviceEventLocked(this.mDisplayDevice, 3);
            this.mDisplayDevice = null;
        }
    }

    private void renameDisplayDeviceLocked(String name) {
        if (this.mDisplayDevice != null && !this.mDisplayDevice.getNameLocked().equals(name)) {
            this.mDisplayDevice.setNameLocked(name);
            sendDisplayDeviceEventLocked(this.mDisplayDevice, 2);
        }
    }

    private void scheduleStatusChangedBroadcastLocked() {
        this.mCurrentStatus = null;
        if (!this.mPendingStatusChangeBroadcast) {
            this.mPendingStatusChangeBroadcast = true;
            this.mHandler.sendEmptyMessage(MSG_SEND_STATUS_CHANGE_BROADCAST);
        }
    }

    private void handleSendStatusChangeBroadcast() {
        synchronized (getSyncRoot()) {
            if (this.mPendingStatusChangeBroadcast) {
                this.mPendingStatusChangeBroadcast = DEBUG;
                Intent intent = new Intent("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
                intent.addFlags(1073741824);
                intent.putExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS", getWifiDisplayStatusLocked());
                getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                return;
            }
        }
    }
}
