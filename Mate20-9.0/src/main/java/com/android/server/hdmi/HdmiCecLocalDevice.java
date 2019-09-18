package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.input.InputManager;
import android.net.util.NetworkConstants;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.hdmi.HdmiAnnotations;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

abstract class HdmiCecLocalDevice {
    private static final int DEVICE_CLEANUP_TIMEOUT = 5000;
    private static final int FOLLOWER_SAFETY_TIMEOUT = 550;
    private static final int MSG_DISABLE_DEVICE_TIMEOUT = 1;
    private static final int MSG_USER_CONTROL_RELEASE_TIMEOUT = 2;
    private static final String TAG = "HdmiCecLocalDevice";
    private final ArrayList<HdmiCecFeatureAction> mActions = new ArrayList<>();
    @GuardedBy("mLock")
    private int mActiveRoutingPath;
    @GuardedBy("mLock")
    protected final ActiveSource mActiveSource = new ActiveSource();
    protected int mAddress;
    protected final HdmiCecMessageCache mCecMessageCache = new HdmiCecMessageCache();
    protected HdmiDeviceInfo mDeviceInfo;
    protected final int mDeviceType;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HdmiCecLocalDevice.this.handleDisableDeviceTimeout();
                    return;
                case 2:
                    HdmiCecLocalDevice.this.handleUserControlReleased();
                    return;
                default:
                    return;
            }
        }
    };
    protected int mLastKeyRepeatCount = 0;
    protected int mLastKeycode = -1;
    protected final Object mLock;
    protected PendingActionClearedCallback mPendingActionClearedCallback;
    protected int mPreferredAddress;
    protected final HdmiControlService mService;

    static class ActiveSource {
        int logicalAddress;
        int physicalAddress;

        public ActiveSource() {
            invalidate();
        }

        public ActiveSource(int logical, int physical) {
            this.logicalAddress = logical;
            this.physicalAddress = physical;
        }

        public static ActiveSource of(ActiveSource source) {
            return new ActiveSource(source.logicalAddress, source.physicalAddress);
        }

        public static ActiveSource of(int logical, int physical) {
            return new ActiveSource(logical, physical);
        }

        public boolean isValid() {
            return HdmiUtils.isValidAddress(this.logicalAddress);
        }

        public void invalidate() {
            this.logicalAddress = -1;
            this.physicalAddress = NetworkConstants.ARP_HWTYPE_RESERVED_HI;
        }

        public boolean equals(int logical, int physical) {
            return this.logicalAddress == logical && this.physicalAddress == physical;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof ActiveSource)) {
                return false;
            }
            ActiveSource that = (ActiveSource) obj;
            if (that.logicalAddress == this.logicalAddress && that.physicalAddress == this.physicalAddress) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.logicalAddress * 29) + this.physicalAddress;
        }

        public String toString() {
            String logicalAddressString;
            String physicalAddressString;
            StringBuffer s = new StringBuffer();
            if (this.logicalAddress == -1) {
                logicalAddressString = "invalid";
            } else {
                logicalAddressString = String.format("0x%02x", new Object[]{Integer.valueOf(this.logicalAddress)});
            }
            s.append("(");
            s.append(logicalAddressString);
            if (this.physicalAddress == 65535) {
                physicalAddressString = "invalid";
            } else {
                physicalAddressString = String.format("0x%04x", new Object[]{Integer.valueOf(this.physicalAddress)});
            }
            s.append(", ");
            s.append(physicalAddressString);
            s.append(")");
            return s.toString();
        }
    }

    interface PendingActionClearedCallback {
        void onCleared(HdmiCecLocalDevice hdmiCecLocalDevice);
    }

    /* access modifiers changed from: protected */
    public abstract int getPreferredAddress();

    /* access modifiers changed from: protected */
    public abstract void onAddressAllocated(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract void setPreferredAddress(int i);

    protected HdmiCecLocalDevice(HdmiControlService service, int deviceType) {
        this.mService = service;
        this.mDeviceType = deviceType;
        this.mAddress = 15;
        this.mLock = service.getServiceLock();
    }

    static HdmiCecLocalDevice create(HdmiControlService service, int deviceType) {
        if (deviceType == 0) {
            return new HdmiCecLocalDeviceTv(service);
        }
        if (deviceType != 4) {
            return null;
        }
        return new HdmiCecLocalDevicePlayback(service);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void init() {
        assertRunOnServiceThread();
        this.mPreferredAddress = getPreferredAddress();
        this.mPendingActionClearedCallback = null;
    }

    /* access modifiers changed from: protected */
    public boolean isInputReady(int deviceId) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean canGoToStandby() {
        return true;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean dispatchMessage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        int dest = message.getDestination();
        if (dest != this.mAddress && dest != 15) {
            return false;
        }
        this.mCecMessageCache.cacheMessage(message);
        return onMessage(message);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public final boolean onMessage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (dispatchMessageToAction(message)) {
            return true;
        }
        int opcode = message.getOpcode();
        switch (opcode) {
            case 53:
                return handleTimerStatus(message);
            case 54:
                return handleStandby(message);
            default:
                switch (opcode) {
                    case 67:
                        return handleTimerClearedStatus(message);
                    case 68:
                        return handleUserControlPressed(message);
                    case HdmiCecKeycode.CEC_KEYCODE_STOP:
                        return handleUserControlReleased();
                    case HdmiCecKeycode.CEC_KEYCODE_PAUSE:
                        return handleGiveOsdName(message);
                    case HdmiCecKeycode.CEC_KEYCODE_RECORD:
                        return handleSetOsdName(message);
                    default:
                        switch (opcode) {
                            case 128:
                                return handleRoutingChange(message);
                            case NetworkConstants.ICMPV6_ECHO_REPLY_TYPE:
                                return handleRoutingInformation(message);
                            case 130:
                                return handleActiveSource(message);
                            case 131:
                                return handleGivePhysicalAddress();
                            case 132:
                                return handleReportPhysicalAddress(message);
                            case NetworkConstants.ICMPV6_ROUTER_SOLICITATION:
                                return handleRequestActiveSource(message);
                            case NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT:
                                return handleSetStreamPath(message);
                            default:
                                switch (opcode) {
                                    case 140:
                                        return handleGiveDeviceVendorId();
                                    case 141:
                                        return handleMenuRequest(message);
                                    case 142:
                                        return handleMenuStatus(message);
                                    case 143:
                                        return handleGiveDevicePowerStatus(message);
                                    case 144:
                                        return handleReportPowerStatus(message);
                                    case HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2:
                                        return handleGetMenuLanguage(message);
                                    default:
                                        switch (opcode) {
                                            case 159:
                                                return handleGetCecVersion(message);
                                            case 160:
                                                return handleVendorCommandWithId(message);
                                            default:
                                                switch (opcode) {
                                                    case 4:
                                                        return handleImageViewOn(message);
                                                    case 10:
                                                        return handleRecordStatus(message);
                                                    case 13:
                                                        return handleTextViewOn(message);
                                                    case 15:
                                                        return handleRecordTvScreen(message);
                                                    case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL:
                                                        return handleSetMenuLanguage(message);
                                                    case 114:
                                                        return handleSetSystemAudioMode(message);
                                                    case 122:
                                                        return handleReportAudioStatus(message);
                                                    case 126:
                                                        return handleSystemAudioModeStatus(message);
                                                    case 137:
                                                        return handleVendorCommand(message);
                                                    case 157:
                                                        return handleInactiveSource(message);
                                                    case 192:
                                                        return handleInitiateArc(message);
                                                    case 197:
                                                        return handleTerminateArc(message);
                                                    default:
                                                        return false;
                                                }
                                        }
                                }
                        }
                }
        }
    }

    @HdmiAnnotations.ServiceThreadOnly
    private boolean dispatchMessageToAction(HdmiCecMessage message) {
        assertRunOnServiceThread();
        boolean processed = false;
        Iterator it = new ArrayList(this.mActions).iterator();
        while (it.hasNext()) {
            processed = processed || ((HdmiCecFeatureAction) it.next()).processCommand(message);
        }
        return processed;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleGivePhysicalAddress() {
        assertRunOnServiceThread();
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportPhysicalAddressCommand(this.mAddress, this.mService.getPhysicalAddress(), this.mDeviceType));
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleGiveDeviceVendorId() {
        assertRunOnServiceThread();
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildDeviceVendorIdCommand(this.mAddress, this.mService.getVendorId()));
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleGetCecVersion(HdmiCecMessage message) {
        assertRunOnServiceThread();
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildCecVersion(message.getDestination(), message.getSource(), this.mService.getCecVersion()));
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleActiveSource(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleInactiveSource(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleRequestActiveSource(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleGetMenuLanguage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        Slog.w(TAG, "Only TV can handle <Get Menu Language>:" + message.toString());
        return false;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleSetMenuLanguage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        Slog.w(TAG, "Only Playback device can handle <Set Menu Language>:" + message.toString());
        return false;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleGiveOsdName(HdmiCecMessage message) {
        assertRunOnServiceThread();
        HdmiCecMessage cecMessage = HdmiCecMessageBuilder.buildSetOsdNameCommand(this.mAddress, message.getSource(), this.mDeviceInfo.getDisplayName());
        if (cecMessage != null) {
            this.mService.sendCecCommand(cecMessage);
        } else {
            Slog.w(TAG, "Failed to build <Get Osd Name>:" + this.mDeviceInfo.getDisplayName());
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleRoutingChange(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleRoutingInformation(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleReportPhysicalAddress(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleSystemAudioModeStatus(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleSetSystemAudioMode(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleTerminateArc(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleInitiateArc(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleReportAudioStatus(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleStandby(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled() || this.mService.isProhibitMode() || !this.mService.isPowerOnOrTransient()) {
            return false;
        }
        this.mService.standby();
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleUserControlPressed(HdmiCecMessage message) {
        assertRunOnServiceThread();
        this.mHandler.removeMessages(2);
        if (this.mService.isPowerOnOrTransient() && isPowerOffOrToggleCommand(message)) {
            this.mService.standby();
            return true;
        } else if (!this.mService.isPowerStandbyOrTransient() || !isPowerOnOrToggleCommand(message)) {
            long downTime = SystemClock.uptimeMillis();
            int keycode = HdmiCecKeycode.cecKeycodeAndParamsToAndroidKey(message.getParams());
            int keyRepeatCount = 0;
            if (this.mLastKeycode != -1) {
                if (keycode == this.mLastKeycode) {
                    keyRepeatCount = this.mLastKeyRepeatCount + 1;
                } else {
                    injectKeyEvent(downTime, 1, this.mLastKeycode, 0);
                }
            }
            this.mLastKeycode = keycode;
            this.mLastKeyRepeatCount = keyRepeatCount;
            if (keycode == -1) {
                return false;
            }
            injectKeyEvent(downTime, 0, keycode, keyRepeatCount);
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2), 550);
            return true;
        } else {
            this.mService.wakeUp();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleUserControlReleased() {
        assertRunOnServiceThread();
        this.mHandler.removeMessages(2);
        this.mLastKeyRepeatCount = 0;
        if (this.mLastKeycode == -1) {
            return false;
        }
        injectKeyEvent(SystemClock.uptimeMillis(), 1, this.mLastKeycode, 0);
        this.mLastKeycode = -1;
        return true;
    }

    static void injectKeyEvent(long time, int action, int keycode, int repeat) {
        KeyEvent keyEvent = KeyEvent.obtain(time, time, action, keycode, repeat, 0, -1, 0, 8, 33554433, null);
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
        keyEvent.recycle();
    }

    static boolean isPowerOnOrToggleCommand(HdmiCecMessage message) {
        byte[] params = message.getParams();
        if (message.getOpcode() == 68) {
            return params[0] == 64 || params[0] == 109 || params[0] == 107;
        }
        return false;
    }

    static boolean isPowerOffOrToggleCommand(HdmiCecMessage message) {
        byte[] params = message.getParams();
        if (message.getOpcode() == 68) {
            return params[0] == 64 || params[0] == 108 || params[0] == 107;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleTextViewOn(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleImageViewOn(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleSetStreamPath(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleGiveDevicePowerStatus(HdmiCecMessage message) {
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportPowerStatus(this.mAddress, message.getSource(), this.mService.getPowerStatus()));
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleMenuRequest(HdmiCecMessage message) {
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportMenuStatus(this.mAddress, message.getSource(), 0));
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleMenuStatus(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleVendorCommand(HdmiCecMessage message) {
        if (!this.mService.invokeVendorCommandListenersOnReceived(this.mDeviceType, message.getSource(), message.getDestination(), message.getParams(), false)) {
            this.mService.maySendFeatureAbortCommand(message, 1);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleVendorCommandWithId(HdmiCecMessage message) {
        byte[] params = message.getParams();
        if (HdmiUtils.threeBytesToInt(params) == this.mService.getVendorId()) {
            if (!this.mService.invokeVendorCommandListenersOnReceived(this.mDeviceType, message.getSource(), message.getDestination(), params, true)) {
                this.mService.maySendFeatureAbortCommand(message, 1);
            }
        } else if (message.getDestination() == 15 || message.getSource() == 15) {
            Slog.v(TAG, "Wrong broadcast vendor command. Ignoring");
        } else {
            Slog.v(TAG, "Wrong direct vendor command. Replying with <Feature Abort>");
            this.mService.maySendFeatureAbortCommand(message, 0);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void sendStandby(int deviceId) {
    }

    /* access modifiers changed from: protected */
    public boolean handleSetOsdName(HdmiCecMessage message) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleRecordTvScreen(HdmiCecMessage message) {
        this.mService.maySendFeatureAbortCommand(message, 2);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleTimerClearedStatus(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleReportPowerStatus(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleTimerStatus(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleRecordStatus(HdmiCecMessage message) {
        return false;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public final void handleAddressAllocated(int logicalAddress, int reason) {
        assertRunOnServiceThread();
        this.mPreferredAddress = logicalAddress;
        this.mAddress = logicalAddress;
        onAddressAllocated(logicalAddress, reason);
        setPreferredAddress(logicalAddress);
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.mDeviceType;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public HdmiDeviceInfo getDeviceInfo() {
        assertRunOnServiceThread();
        return this.mDeviceInfo;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setDeviceInfo(HdmiDeviceInfo info) {
        assertRunOnServiceThread();
        this.mDeviceInfo = info;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isAddressOf(int addr) {
        assertRunOnServiceThread();
        return addr == this.mAddress;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void clearAddress() {
        assertRunOnServiceThread();
        this.mAddress = 15;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void addAndStartAction(HdmiCecFeatureAction action) {
        assertRunOnServiceThread();
        this.mActions.add(action);
        if (this.mService.isPowerStandby() || !this.mService.isAddressAllocated()) {
            Slog.i(TAG, "Not ready to start action. Queued for deferred start:" + action);
            return;
        }
        action.start();
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void startQueuedActions() {
        assertRunOnServiceThread();
        Iterator it = new ArrayList(this.mActions).iterator();
        while (it.hasNext()) {
            HdmiCecFeatureAction action = (HdmiCecFeatureAction) it.next();
            if (!action.started()) {
                Slog.i(TAG, "Starting queued action:" + action);
                action.start();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public <T extends HdmiCecFeatureAction> boolean hasAction(Class<T> clazz) {
        assertRunOnServiceThread();
        Iterator<HdmiCecFeatureAction> it = this.mActions.iterator();
        while (it.hasNext()) {
            if (it.next().getClass().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public <T extends HdmiCecFeatureAction> List<T> getActions(Class<T> clazz) {
        assertRunOnServiceThread();
        List<T> actions = Collections.emptyList();
        Iterator<HdmiCecFeatureAction> it = this.mActions.iterator();
        while (it.hasNext()) {
            HdmiCecFeatureAction action = it.next();
            if (action.getClass().equals(clazz)) {
                if (actions.isEmpty()) {
                    actions = new ArrayList<>();
                }
                actions.add(action);
            }
        }
        return actions;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void removeAction(HdmiCecFeatureAction action) {
        assertRunOnServiceThread();
        action.finish(false);
        this.mActions.remove(action);
        checkIfPendingActionsCleared();
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public <T extends HdmiCecFeatureAction> void removeAction(Class<T> clazz) {
        assertRunOnServiceThread();
        removeActionExcept(clazz, null);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public <T extends HdmiCecFeatureAction> void removeActionExcept(Class<T> clazz, HdmiCecFeatureAction exception) {
        assertRunOnServiceThread();
        Iterator<HdmiCecFeatureAction> iter = this.mActions.iterator();
        while (iter.hasNext()) {
            HdmiCecFeatureAction action = iter.next();
            if (action != exception && action.getClass().equals(clazz)) {
                action.finish(false);
                iter.remove();
            }
        }
        checkIfPendingActionsCleared();
    }

    /* access modifiers changed from: protected */
    public void checkIfPendingActionsCleared() {
        if (this.mActions.isEmpty() && this.mPendingActionClearedCallback != null) {
            PendingActionClearedCallback callback = this.mPendingActionClearedCallback;
            this.mPendingActionClearedCallback = null;
            callback.onCleared(this);
        }
    }

    /* access modifiers changed from: protected */
    public void assertRunOnServiceThread() {
        if (Looper.myLooper() != this.mService.getServiceLooper()) {
            throw new IllegalStateException("Should run on service thread.");
        }
    }

    /* access modifiers changed from: package-private */
    public void setAutoDeviceOff(boolean enabled) {
    }

    /* access modifiers changed from: package-private */
    public void onHotplug(int portId, boolean connected) {
    }

    /* access modifiers changed from: package-private */
    public final HdmiControlService getService() {
        return this.mService;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public final boolean isConnectedToArcPort(int path) {
        assertRunOnServiceThread();
        return this.mService.isConnectedToArcPort(path);
    }

    /* access modifiers changed from: package-private */
    public ActiveSource getActiveSource() {
        ActiveSource activeSource;
        synchronized (this.mLock) {
            activeSource = this.mActiveSource;
        }
        return activeSource;
    }

    /* access modifiers changed from: package-private */
    public void setActiveSource(ActiveSource newActive) {
        setActiveSource(newActive.logicalAddress, newActive.physicalAddress);
    }

    /* access modifiers changed from: package-private */
    public void setActiveSource(HdmiDeviceInfo info) {
        setActiveSource(info.getLogicalAddress(), info.getPhysicalAddress());
    }

    /* access modifiers changed from: package-private */
    public void setActiveSource(int logicalAddress, int physicalAddress) {
        synchronized (this.mLock) {
            this.mActiveSource.logicalAddress = logicalAddress;
            this.mActiveSource.physicalAddress = physicalAddress;
        }
        this.mService.setLastInputForMhl(-1);
    }

    /* access modifiers changed from: package-private */
    public int getActivePath() {
        int i;
        synchronized (this.mLock) {
            i = this.mActiveRoutingPath;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void setActivePath(int path) {
        synchronized (this.mLock) {
            this.mActiveRoutingPath = path;
        }
        this.mService.setActivePortId(pathToPortId(path));
    }

    /* access modifiers changed from: package-private */
    public int getActivePortId() {
        int pathToPortId;
        synchronized (this.mLock) {
            pathToPortId = this.mService.pathToPortId(this.mActiveRoutingPath);
        }
        return pathToPortId;
    }

    /* access modifiers changed from: package-private */
    public void setActivePortId(int portId) {
        setActivePath(this.mService.portIdToPath(portId));
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public HdmiCecMessageCache getCecMessageCache() {
        assertRunOnServiceThread();
        return this.mCecMessageCache;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int pathToPortId(int newPath) {
        assertRunOnServiceThread();
        return this.mService.pathToPortId(newPath);
    }

    /* access modifiers changed from: protected */
    public void onStandby(boolean initiatedByCec, int standbyAction) {
    }

    /* access modifiers changed from: protected */
    public void disableDevice(boolean initiatedByCec, final PendingActionClearedCallback originalCallback) {
        this.mPendingActionClearedCallback = new PendingActionClearedCallback() {
            public void onCleared(HdmiCecLocalDevice device) {
                HdmiCecLocalDevice.this.mHandler.removeMessages(1);
                originalCallback.onCleared(device);
            }
        };
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 1), 5000);
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void handleDisableDeviceTimeout() {
        assertRunOnServiceThread();
        Iterator<HdmiCecFeatureAction> iter = this.mActions.iterator();
        while (iter.hasNext()) {
            iter.next().finish(false);
            iter.remove();
        }
        if (this.mPendingActionClearedCallback != null) {
            this.mPendingActionClearedCallback.onCleared(this);
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void sendKeyEvent(int keyCode, boolean isPressed) {
        assertRunOnServiceThread();
        if (!HdmiCecKeycode.isSupportedKeycode(keyCode)) {
            Slog.w(TAG, "Unsupported key: " + keyCode);
            return;
        }
        List<SendKeyAction> action = getActions(SendKeyAction.class);
        int logicalAddress = findKeyReceiverAddress();
        if (logicalAddress == -1 || logicalAddress == this.mAddress) {
            Slog.w(TAG, "Discard key event: " + keyCode + ", pressed:" + isPressed + ", receiverAddr=" + logicalAddress);
        } else if (!action.isEmpty()) {
            action.get(0).processKeyEvent(keyCode, isPressed);
        } else if (isPressed) {
            addAndStartAction(new SendKeyAction(this, logicalAddress, keyCode));
        }
    }

    /* access modifiers changed from: protected */
    public int findKeyReceiverAddress() {
        Slog.w(TAG, "findKeyReceiverAddress is not implemented");
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void sendUserControlPressedAndReleased(int targetAddress, int cecKeycode) {
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildUserControlPressed(this.mAddress, targetAddress, cecKeycode));
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildUserControlReleased(this.mAddress, targetAddress));
    }

    /* access modifiers changed from: protected */
    public void dump(IndentingPrintWriter pw) {
        pw.println("mDeviceType: " + this.mDeviceType);
        pw.println("mAddress: " + this.mAddress);
        pw.println("mPreferredAddress: " + this.mPreferredAddress);
        pw.println("mDeviceInfo: " + this.mDeviceInfo);
        pw.println("mActiveSource: " + this.mActiveSource);
        pw.println(String.format("mActiveRoutingPath: 0x%04x", new Object[]{Integer.valueOf(this.mActiveRoutingPath)}));
    }
}
