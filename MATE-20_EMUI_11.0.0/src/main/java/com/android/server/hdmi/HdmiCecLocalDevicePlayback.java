package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.LocalePicker;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.hdmi.HdmiAnnotations;
import com.android.server.hdmi.HdmiCecLocalDevice;
import com.android.server.hdmi.HdmiControlService;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class HdmiCecLocalDevicePlayback extends HdmiCecLocalDeviceSource {
    private static final boolean SET_MENU_LANGUAGE = SystemProperties.getBoolean("ro.hdmi.set_menu_language", false);
    private static final String TAG = "HdmiCecLocalDevicePlayback";
    private static final boolean WAKE_ON_HOTPLUG = SystemProperties.getBoolean("ro.hdmi.wake_on_hotplug", true);
    private boolean mAutoTvOff = this.mService.readBooleanSetting("hdmi_control_auto_device_off_enabled", false);
    private int mLocalActivePath = 0;
    private ActiveWakeLock mWakeLock;

    /* access modifiers changed from: private */
    public interface ActiveWakeLock {
        void acquire();

        boolean isHeld();

        void release();
    }

    HdmiCecLocalDevicePlayback(HdmiControlService service) {
        super(service, 4);
        this.mService.writeBooleanSetting("hdmi_control_auto_device_off_enabled", this.mAutoTvOff);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    @HdmiAnnotations.ServiceThreadOnly
    public void onAddressAllocated(int logicalAddress, int reason) {
        assertRunOnServiceThread();
        HdmiControlService hdmiControlService = this.mService;
        if (reason == 0) {
            this.mService.setAndBroadcastActiveSource(this.mService.getPhysicalAddress(), getDeviceInfo().getDeviceType(), 15);
        }
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportPhysicalAddressCommand(this.mAddress, this.mService.getPhysicalAddress(), this.mDeviceType));
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildDeviceVendorIdCommand(this.mAddress, this.mService.getVendorId()));
        if (this.mService.audioSystem() == null) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildGiveSystemAudioModeStatus(this.mAddress, 5), new HdmiControlService.SendMessageCallback() {
                /* class com.android.server.hdmi.HdmiCecLocalDevicePlayback.AnonymousClass1 */

                @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
                public void onSendCompleted(int error) {
                    if (error != 0) {
                        HdmiLogger.debug("AVR did not respond to <Give System Audio Mode Status>", new Object[0]);
                        HdmiCecLocalDevicePlayback.this.mService.setSystemAudioActivated(false);
                    }
                }
            });
        }
        startQueuedActions();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    @HdmiAnnotations.ServiceThreadOnly
    public int getPreferredAddress() {
        assertRunOnServiceThread();
        return SystemProperties.getInt("persist.sys.hdmi.addr.playback", 15);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    @HdmiAnnotations.ServiceThreadOnly
    public void setPreferredAddress(int addr) {
        assertRunOnServiceThread();
        this.mService.writeStringSystemProperty("persist.sys.hdmi.addr.playback", String.valueOf(addr));
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

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecLocalDeviceSource, com.android.server.hdmi.HdmiCecLocalDevice
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
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    @HdmiAnnotations.ServiceThreadOnly
    public void onStandby(boolean initiatedByCec, int standbyAction) {
        assertRunOnServiceThread();
        if (this.mService.isControlEnabled() && !initiatedByCec && this.mAutoTvOff) {
            if (standbyAction == 0) {
                this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, 0));
            } else if (standbyAction == 1) {
                this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, 15));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    @HdmiAnnotations.ServiceThreadOnly
    public void setAutoDeviceOff(boolean enabled) {
        assertRunOnServiceThread();
        this.mAutoTvOff = enabled;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecLocalDeviceSource
    @HdmiAnnotations.ServiceThreadOnly
    @VisibleForTesting
    public void setIsActiveSource(boolean on) {
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
                    /* class com.android.server.hdmi.HdmiCecLocalDevicePlayback.AnonymousClass2 */

                    @Override // com.android.server.hdmi.HdmiCecLocalDevicePlayback.ActiveWakeLock
                    public void acquire() {
                    }

                    @Override // com.android.server.hdmi.HdmiCecLocalDevicePlayback.ActiveWakeLock
                    public void release() {
                    }

                    @Override // com.android.server.hdmi.HdmiCecLocalDevicePlayback.ActiveWakeLock
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
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    public boolean canGoToStandby() {
        return !getWakeLock().isHeld();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleUserControlPressed(HdmiCecMessage message) {
        assertRunOnServiceThread();
        wakeUpIfActiveSource();
        return super.handleUserControlPressed(message);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDeviceSource
    public void wakeUpIfActiveSource() {
        if (this.mIsActiveSource) {
            if (this.mService.isPowerStandbyOrTransient() || !this.mService.getPowerManager().isScreenOn()) {
                this.mService.wakeUp();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDeviceSource
    public void maySendActiveSource(int dest) {
        if (this.mIsActiveSource) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(this.mAddress, this.mService.getPhysicalAddress()));
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportMenuStatus(this.mAddress, dest, 0));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
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
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    public boolean handleSetSystemAudioMode(HdmiCecMessage message) {
        boolean setSystemAudioModeOn;
        if (message.getDestination() == 15 && message.getSource() == 5 && this.mService.audioSystem() == null && this.mService.isSystemAudioActivated() != (setSystemAudioModeOn = HdmiUtils.parseCommandParamSystemAudioStatus(message))) {
            this.mService.setSystemAudioActivated(setSystemAudioModeOn);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    public boolean handleSystemAudioModeStatus(HdmiCecMessage message) {
        boolean setSystemAudioModeOn;
        if (message.getDestination() != this.mAddress || message.getSource() != 5 || this.mService.isSystemAudioActivated() == (setSystemAudioModeOn = HdmiUtils.parseCommandParamSystemAudioStatus(message))) {
            return true;
        }
        this.mService.setSystemAudioActivated(setSystemAudioModeOn);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    public int findKeyReceiverAddress() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    public int findAudioReceiverAddress() {
        if (this.mService.isSystemAudioActivated()) {
            return 5;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    @HdmiAnnotations.ServiceThreadOnly
    public void disableDevice(boolean initiatedByCec, HdmiCecLocalDevice.PendingActionClearedCallback callback) {
        super.disableDevice(initiatedByCec, callback);
        assertRunOnServiceThread();
        if (!initiatedByCec && this.mIsActiveSource && this.mService.isControlEnabled()) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildInactiveSource(this.mAddress, this.mService.getPhysicalAddress()));
        }
        setIsActiveSource(false);
        checkIfPendingActionsCleared();
    }

    private void routeToPort(int portId) {
        this.mLocalActivePath = portId;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getLocalActivePath() {
        return this.mLocalActivePath;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.hdmi.HdmiCecLocalDevice
    public void dump(IndentingPrintWriter pw) {
        super.dump(pw);
        pw.println("mIsActiveSource: " + this.mIsActiveSource);
        pw.println("mAutoTvOff:" + this.mAutoTvOff);
    }

    /* access modifiers changed from: private */
    public class SystemWakeLock implements ActiveWakeLock {
        private final PowerManager.WakeLock mWakeLock;

        public SystemWakeLock() {
            this.mWakeLock = HdmiCecLocalDevicePlayback.this.mService.getPowerManager().newWakeLock(1, HdmiCecLocalDevicePlayback.TAG);
            this.mWakeLock.setReferenceCounted(false);
        }

        @Override // com.android.server.hdmi.HdmiCecLocalDevicePlayback.ActiveWakeLock
        public void acquire() {
            this.mWakeLock.acquire();
            HdmiLogger.debug("active source: %b. Wake lock acquired", Boolean.valueOf(HdmiCecLocalDevicePlayback.this.mIsActiveSource));
        }

        @Override // com.android.server.hdmi.HdmiCecLocalDevicePlayback.ActiveWakeLock
        public void release() {
            this.mWakeLock.release();
            HdmiLogger.debug("Wake lock released", new Object[0]);
        }

        @Override // com.android.server.hdmi.HdmiCecLocalDevicePlayback.ActiveWakeLock
        public boolean isHeld() {
            return this.mWakeLock.isHeld();
        }
    }
}
