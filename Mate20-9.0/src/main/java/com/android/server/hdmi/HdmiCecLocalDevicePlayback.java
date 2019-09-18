package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.app.LocalePicker;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.hdmi.HdmiAnnotations;
import com.android.server.hdmi.HdmiCecLocalDevice;
import java.io.UnsupportedEncodingException;
import java.util.List;

final class HdmiCecLocalDevicePlayback extends HdmiCecLocalDevice {
    private static final boolean SET_MENU_LANGUAGE = SystemProperties.getBoolean("ro.hdmi.set_menu_language", false);
    private static final String TAG = "HdmiCecLocalDevicePlayback";
    private static final boolean WAKE_ON_HOTPLUG = SystemProperties.getBoolean("ro.hdmi.wake_on_hotplug", true);
    private boolean mAutoTvOff = this.mService.readBooleanSetting("hdmi_control_auto_device_off_enabled", false);
    /* access modifiers changed from: private */
    public boolean mIsActiveSource = false;
    private ActiveWakeLock mWakeLock;

    private interface ActiveWakeLock {
        void acquire();

        boolean isHeld();

        void release();
    }

    private class SystemWakeLock implements ActiveWakeLock {
        private final PowerManager.WakeLock mWakeLock;

        public SystemWakeLock() {
            this.mWakeLock = HdmiCecLocalDevicePlayback.this.mService.getPowerManager().newWakeLock(1, HdmiCecLocalDevicePlayback.TAG);
            this.mWakeLock.setReferenceCounted(false);
        }

        public void acquire() {
            this.mWakeLock.acquire();
            HdmiLogger.debug("active source: %b. Wake lock acquired", Boolean.valueOf(HdmiCecLocalDevicePlayback.this.mIsActiveSource));
        }

        public void release() {
            this.mWakeLock.release();
            HdmiLogger.debug("Wake lock released", new Object[0]);
        }

        public boolean isHeld() {
            return this.mWakeLock.isHeld();
        }
    }

    HdmiCecLocalDevicePlayback(HdmiControlService service) {
        super(service, 4);
        this.mService.writeBooleanSetting("hdmi_control_auto_device_off_enabled", this.mAutoTvOff);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void onAddressAllocated(int logicalAddress, int reason) {
        assertRunOnServiceThread();
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportPhysicalAddressCommand(this.mAddress, this.mService.getPhysicalAddress(), this.mDeviceType));
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildDeviceVendorIdCommand(this.mAddress, this.mService.getVendorId()));
        startQueuedActions();
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public int getPreferredAddress() {
        assertRunOnServiceThread();
        return SystemProperties.getInt("persist.sys.hdmi.addr.playback", 15);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void setPreferredAddress(int addr) {
        assertRunOnServiceThread();
        SystemProperties.set("persist.sys.hdmi.addr.playback", String.valueOf(addr));
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void oneTouchPlay(IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        List<OneTouchPlayAction> actions = getActions(OneTouchPlayAction.class);
        if (!actions.isEmpty()) {
            Slog.i(TAG, "oneTouchPlay already in progress");
            actions.get(0).addCallback(callback);
            return;
        }
        OneTouchPlayAction action = OneTouchPlayAction.create(this, 0, callback);
        if (action == null) {
            Slog.w(TAG, "Cannot initiate oneTouchPlay");
            invokeCallback(callback, 5);
            return;
        }
        addAndStartAction(action);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void queryDisplayStatus(IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        List<DevicePowerStatusAction> actions = getActions(DevicePowerStatusAction.class);
        if (!actions.isEmpty()) {
            Slog.i(TAG, "queryDisplayStatus already in progress");
            actions.get(0).addCallback(callback);
            return;
        }
        DevicePowerStatusAction action = DevicePowerStatusAction.create(this, 0, callback);
        if (action == null) {
            Slog.w(TAG, "Cannot initiate queryDisplayStatus");
            invokeCallback(callback, 5);
            return;
        }
        addAndStartAction(action);
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void invokeCallback(IHdmiControlCallback callback, int result) {
        assertRunOnServiceThread();
        try {
            callback.onComplete(result);
        } catch (RemoteException e) {
            Slog.e(TAG, "Invoking callback failed:" + e);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onHotplug(int portId, boolean connected) {
        assertRunOnServiceThread();
        this.mCecMessageCache.flushAll();
        if (WAKE_ON_HOTPLUG && connected && this.mService.isPowerStandbyOrTransient()) {
            this.mService.wakeUp();
        }
        if (!connected) {
            getWakeLock().release();
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void onStandby(boolean initiatedByCec, int standbyAction) {
        assertRunOnServiceThread();
        if (this.mService.isControlEnabled() && !initiatedByCec && this.mAutoTvOff) {
            switch (standbyAction) {
                case 0:
                    this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, 0));
                    break;
                case 1:
                    this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, 15));
                    break;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setAutoDeviceOff(boolean enabled) {
        assertRunOnServiceThread();
        this.mAutoTvOff = enabled;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setActiveSource(boolean on) {
        assertRunOnServiceThread();
        this.mIsActiveSource = on;
        if (on) {
            getWakeLock().acquire();
        } else {
            getWakeLock().release();
        }
    }

    @HdmiAnnotations.ServiceThreadOnly
    private ActiveWakeLock getWakeLock() {
        assertRunOnServiceThread();
        if (this.mWakeLock == null) {
            if (SystemProperties.getBoolean("persist.sys.hdmi.keep_awake", true)) {
                this.mWakeLock = new SystemWakeLock();
            } else {
                this.mWakeLock = new ActiveWakeLock() {
                    public void acquire() {
                    }

                    public void release() {
                    }

                    public boolean isHeld() {
                        return false;
                    }
                };
                HdmiLogger.debug("No wakelock is used to keep the display on.", new Object[0]);
            }
        }
        return this.mWakeLock;
    }

    /* access modifiers changed from: protected */
    public boolean canGoToStandby() {
        return !getWakeLock().isHeld();
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleActiveSource(HdmiCecMessage message) {
        assertRunOnServiceThread();
        mayResetActiveSource(HdmiUtils.twoBytesToInt(message.getParams()));
        return true;
    }

    private void mayResetActiveSource(int physicalAddress) {
        if (physicalAddress != this.mService.getPhysicalAddress()) {
            setActiveSource(false);
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleUserControlPressed(HdmiCecMessage message) {
        assertRunOnServiceThread();
        wakeUpIfActiveSource();
        return super.handleUserControlPressed(message);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleSetStreamPath(HdmiCecMessage message) {
        assertRunOnServiceThread();
        maySetActiveSource(HdmiUtils.twoBytesToInt(message.getParams()));
        maySendActiveSource(message.getSource());
        wakeUpIfActiveSource();
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleRoutingChange(HdmiCecMessage message) {
        assertRunOnServiceThread();
        maySetActiveSource(HdmiUtils.twoBytesToInt(message.getParams(), 2));
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleRoutingInformation(HdmiCecMessage message) {
        assertRunOnServiceThread();
        maySetActiveSource(HdmiUtils.twoBytesToInt(message.getParams()));
        return true;
    }

    private void maySetActiveSource(int physicalAddress) {
        setActiveSource(physicalAddress == this.mService.getPhysicalAddress());
    }

    private void wakeUpIfActiveSource() {
        if (this.mIsActiveSource) {
            if (this.mService.isPowerStandbyOrTransient() || !this.mService.getPowerManager().isScreenOn()) {
                this.mService.wakeUp();
            }
        }
    }

    private void maySendActiveSource(int dest) {
        if (this.mIsActiveSource) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(this.mAddress, this.mService.getPhysicalAddress()));
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportMenuStatus(this.mAddress, dest, 0));
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleRequestActiveSource(HdmiCecMessage message) {
        assertRunOnServiceThread();
        maySendActiveSource(message.getSource());
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleSetMenuLanguage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!SET_MENU_LANGUAGE) {
            return false;
        }
        try {
            String iso3Language = new String(message.getParams(), 0, 3, "US-ASCII");
            if (this.mService.getContext().getResources().getConfiguration().locale.getISO3Language().equals(iso3Language)) {
                return true;
            }
            for (LocalePicker.LocaleInfo localeInfo : LocalePicker.getAllAssetLocales(this.mService.getContext(), false)) {
                if (localeInfo.getLocale().getISO3Language().equals(iso3Language)) {
                    LocalePicker.updateLocale(localeInfo.getLocale());
                    return true;
                }
            }
            Slog.w(TAG, "Can't handle <Set Menu Language> of " + iso3Language);
            return false;
        } catch (UnsupportedEncodingException e) {
            Slog.w(TAG, "Can't handle <Set Menu Language>", e);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public int findKeyReceiverAddress() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void sendStandby(int deviceId) {
        assertRunOnServiceThread();
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, 0));
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void disableDevice(boolean initiatedByCec, HdmiCecLocalDevice.PendingActionClearedCallback callback) {
        super.disableDevice(initiatedByCec, callback);
        assertRunOnServiceThread();
        if (!initiatedByCec && this.mIsActiveSource) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildInactiveSource(this.mAddress, this.mService.getPhysicalAddress()));
        }
        setActiveSource(false);
        checkIfPendingActionsCleared();
    }

    /* access modifiers changed from: protected */
    public void dump(IndentingPrintWriter pw) {
        super.dump(pw);
        pw.println("mIsActiveSource: " + this.mIsActiveSource);
        pw.println("mAutoTvOff:" + this.mAutoTvOff);
    }
}
