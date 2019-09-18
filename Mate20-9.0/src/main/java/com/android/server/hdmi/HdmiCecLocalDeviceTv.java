package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiPortInfo;
import android.hardware.hdmi.HdmiRecordSources;
import android.hardware.hdmi.HdmiTimerRecordSources;
import android.hardware.hdmi.IHdmiControlCallback;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.util.NetworkConstants;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.hdmi.DeviceDiscoveryAction;
import com.android.server.hdmi.HdmiAnnotations;
import com.android.server.hdmi.HdmiCecLocalDevice;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.usb.UsbAudioDevice;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

final class HdmiCecLocalDeviceTv extends HdmiCecLocalDevice {
    private static final String TAG = "HdmiCecLocalDeviceTv";
    @HdmiAnnotations.ServiceThreadOnly
    private boolean mArcEstablished = false;
    private final SparseBooleanArray mArcFeatureEnabled = new SparseBooleanArray();
    private boolean mAutoDeviceOff = this.mService.readBooleanSetting("hdmi_control_auto_device_off_enabled", true);
    private boolean mAutoWakeup = this.mService.readBooleanSetting("hdmi_control_auto_wakeup_enabled", true);
    private final ArraySet<Integer> mCecSwitches = new ArraySet<>();
    private final DelayedMessageBuffer mDelayedMessageBuffer = new DelayedMessageBuffer(this);
    private final SparseArray<HdmiDeviceInfo> mDeviceInfos = new SparseArray<>();
    private List<Integer> mLocalDeviceAddresses;
    @GuardedBy("mLock")
    private int mPrevPortId = -1;
    @GuardedBy("mLock")
    private List<HdmiDeviceInfo> mSafeAllDeviceInfos = Collections.emptyList();
    @GuardedBy("mLock")
    private List<HdmiDeviceInfo> mSafeExternalInputs = Collections.emptyList();
    /* access modifiers changed from: private */
    public SelectRequestBuffer mSelectRequestBuffer;
    private boolean mSkipRoutingControl;
    private final HdmiCecStandbyModeHandler mStandbyHandler;
    @GuardedBy("mLock")
    private boolean mSystemAudioActivated = false;
    @GuardedBy("mLock")
    private boolean mSystemAudioControlFeatureEnabled = this.mService.readBooleanSetting("hdmi_system_audio_control_enabled", true);
    @GuardedBy("mLock")
    private boolean mSystemAudioMute = false;
    @GuardedBy("mLock")
    private int mSystemAudioVolume = -1;
    private final TvInputManager.TvInputCallback mTvInputCallback = new TvInputManager.TvInputCallback() {
        public void onInputAdded(String inputId) {
            TvInputInfo tvInfo = HdmiCecLocalDeviceTv.this.mService.getTvInputManager().getTvInputInfo(inputId);
            if (tvInfo != null) {
                HdmiDeviceInfo info = tvInfo.getHdmiDeviceInfo();
                if (info != null) {
                    HdmiCecLocalDeviceTv.this.addTvInput(inputId, info.getId());
                    if (info.isCecDevice()) {
                        HdmiCecLocalDeviceTv.this.processDelayedActiveSource(info.getLogicalAddress());
                    }
                }
            }
        }

        public void onInputRemoved(String inputId) {
            HdmiCecLocalDeviceTv.this.removeTvInput(inputId);
        }
    };
    private final HashMap<String, Integer> mTvInputs = new HashMap<>();

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void addTvInput(String inputId, int deviceId) {
        assertRunOnServiceThread();
        this.mTvInputs.put(inputId, Integer.valueOf(deviceId));
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void removeTvInput(String inputId) {
        assertRunOnServiceThread();
        this.mTvInputs.remove(inputId);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isInputReady(int deviceId) {
        assertRunOnServiceThread();
        return this.mTvInputs.containsValue(Integer.valueOf(deviceId));
    }

    HdmiCecLocalDeviceTv(HdmiControlService service) {
        super(service, 0);
        this.mStandbyHandler = new HdmiCecStandbyModeHandler(service, this);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void onAddressAllocated(int logicalAddress, int reason) {
        assertRunOnServiceThread();
        for (HdmiPortInfo port : this.mService.getPortInfo()) {
            this.mArcFeatureEnabled.put(port.getId(), port.isArcSupported());
        }
        this.mService.registerTvInputCallback(this.mTvInputCallback);
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportPhysicalAddressCommand(this.mAddress, this.mService.getPhysicalAddress(), this.mDeviceType));
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildDeviceVendorIdCommand(this.mAddress, this.mService.getVendorId()));
        this.mCecSwitches.add(Integer.valueOf(this.mService.getPhysicalAddress()));
        this.mTvInputs.clear();
        boolean z = false;
        this.mSkipRoutingControl = reason == 3;
        if (!(reason == 0 || reason == 1)) {
            z = true;
        }
        launchRoutingControl(z);
        this.mLocalDeviceAddresses = initLocalDeviceAddresses();
        resetSelectRequestBuffer();
        launchDeviceDiscovery();
    }

    @HdmiAnnotations.ServiceThreadOnly
    private List<Integer> initLocalDeviceAddresses() {
        assertRunOnServiceThread();
        List<Integer> addresses = new ArrayList<>();
        for (HdmiCecLocalDevice device : this.mService.getAllLocalDevices()) {
            addresses.add(Integer.valueOf(device.getDeviceInfo().getLogicalAddress()));
        }
        return Collections.unmodifiableList(addresses);
    }

    @HdmiAnnotations.ServiceThreadOnly
    public void setSelectRequestBuffer(SelectRequestBuffer requestBuffer) {
        assertRunOnServiceThread();
        this.mSelectRequestBuffer = requestBuffer;
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void resetSelectRequestBuffer() {
        assertRunOnServiceThread();
        setSelectRequestBuffer(SelectRequestBuffer.EMPTY_BUFFER);
    }

    /* access modifiers changed from: protected */
    public int getPreferredAddress() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setPreferredAddress(int addr) {
        Slog.w(TAG, "Preferred addres will not be stored for TV");
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean dispatchMessage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!this.mService.isPowerStandby() || this.mService.isWakeUpMessageReceived() || !this.mStandbyHandler.handleCommand(message)) {
            return super.onMessage(message);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void deviceSelect(int id, IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        HdmiDeviceInfo targetDevice = this.mDeviceInfos.get(id);
        if (targetDevice == null) {
            invokeCallback(callback, 3);
            return;
        }
        int targetAddress = targetDevice.getLogicalAddress();
        HdmiCecLocalDevice.ActiveSource active = getActiveSource();
        if (targetDevice.getDevicePowerStatus() == 0 && active.isValid() && targetAddress == active.logicalAddress) {
            invokeCallback(callback, 0);
        } else if (targetAddress == 0) {
            handleSelectInternalSource();
            setActiveSource(targetAddress, this.mService.getPhysicalAddress());
            setActivePath(this.mService.getPhysicalAddress());
            invokeCallback(callback, 0);
        } else if (!this.mService.isControlEnabled()) {
            setActiveSource(targetDevice);
            invokeCallback(callback, 6);
        } else {
            removeAction(DeviceSelectAction.class);
            addAndStartAction(new DeviceSelectAction(this, targetDevice, callback));
        }
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void handleSelectInternalSource() {
        assertRunOnServiceThread();
        if (this.mService.isControlEnabled() && this.mActiveSource.logicalAddress != this.mAddress) {
            updateActiveSource(this.mAddress, this.mService.getPhysicalAddress());
            if (this.mSkipRoutingControl) {
                this.mSkipRoutingControl = false;
            } else {
                this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(this.mAddress, this.mService.getPhysicalAddress()));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void updateActiveSource(int logicalAddress, int physicalAddress) {
        assertRunOnServiceThread();
        updateActiveSource(HdmiCecLocalDevice.ActiveSource.of(logicalAddress, physicalAddress));
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void updateActiveSource(HdmiCecLocalDevice.ActiveSource newActive) {
        assertRunOnServiceThread();
        if (!this.mActiveSource.equals(newActive)) {
            setActiveSource(newActive);
            int logicalAddress = newActive.logicalAddress;
            if (!(getCecDeviceInfo(logicalAddress) == null || logicalAddress == this.mAddress || this.mService.pathToPortId(newActive.physicalAddress) != getActivePortId())) {
                setPrevPortId(getActivePortId());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getPortId(int physicalAddress) {
        return this.mService.pathToPortId(physicalAddress);
    }

    /* access modifiers changed from: package-private */
    public int getPrevPortId() {
        int i;
        synchronized (this.mLock) {
            i = this.mPrevPortId;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void setPrevPortId(int portId) {
        synchronized (this.mLock) {
            this.mPrevPortId = portId;
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void updateActiveInput(int path, boolean notifyInputChange) {
        assertRunOnServiceThread();
        setActivePath(path);
        if (notifyInputChange) {
            HdmiDeviceInfo info = getCecDeviceInfo(getActiveSource().logicalAddress);
            if (info == null) {
                info = this.mService.getDeviceInfoByPort(getActivePortId());
                if (info == null) {
                    info = new HdmiDeviceInfo(path, getActivePortId());
                }
            }
            this.mService.invokeInputChangeListener(info);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void doManualPortSwitching(int portId, IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        if (!this.mService.isValidPortId(portId)) {
            invokeCallback(callback, 6);
        } else if (portId == getActivePortId()) {
            invokeCallback(callback, 0);
        } else {
            this.mActiveSource.invalidate();
            if (!this.mService.isControlEnabled()) {
                setActivePortId(portId);
                invokeCallback(callback, 6);
                return;
            }
            int oldPath = getActivePortId() != -1 ? this.mService.portIdToPath(getActivePortId()) : getDeviceInfo().getPhysicalAddress();
            setActivePath(oldPath);
            if (this.mSkipRoutingControl) {
                this.mSkipRoutingControl = false;
            } else {
                startRoutingControl(oldPath, this.mService.portIdToPath(portId), true, callback);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void startRoutingControl(int oldPath, int newPath, boolean queryDevicePowerStatus, IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        if (oldPath != newPath) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildRoutingChange(this.mAddress, oldPath, newPath));
            removeAction(RoutingControlAction.class);
            addAndStartAction(new RoutingControlAction(this, newPath, queryDevicePowerStatus, callback));
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int getPowerStatus() {
        assertRunOnServiceThread();
        return this.mService.getPowerStatus();
    }

    /* access modifiers changed from: protected */
    public int findKeyReceiverAddress() {
        if (getActiveSource().isValid()) {
            return getActiveSource().logicalAddress;
        }
        HdmiDeviceInfo info = getDeviceInfoByPath(getActivePath());
        if (info != null) {
            return info.getLogicalAddress();
        }
        return -1;
    }

    private static void invokeCallback(IHdmiControlCallback callback, int result) {
        if (callback != null) {
            try {
                callback.onComplete(result);
            } catch (RemoteException e) {
                Slog.e(TAG, "Invoking callback failed:" + e);
            }
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleActiveSource(HdmiCecMessage message) {
        assertRunOnServiceThread();
        int logicalAddress = message.getSource();
        int physicalAddress = HdmiUtils.twoBytesToInt(message.getParams());
        HdmiDeviceInfo info = getCecDeviceInfo(logicalAddress);
        if (info == null) {
            if (!handleNewDeviceAtTheTailOfActivePath(physicalAddress)) {
                HdmiLogger.debug("Device info %X not found; buffering the command", Integer.valueOf(logicalAddress));
                this.mDelayedMessageBuffer.add(message);
            }
        } else if (isInputReady(info.getId()) || info.getDeviceType() == 5) {
            updateDevicePowerStatus(logicalAddress, 0);
            ActiveSourceHandler.create(this, null).process(HdmiCecLocalDevice.ActiveSource.of(logicalAddress, physicalAddress), info.getDeviceType());
        } else {
            HdmiLogger.debug("Input not ready for device: %X; buffering the command", Integer.valueOf(info.getId()));
            this.mDelayedMessageBuffer.add(message);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleInactiveSource(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (getActiveSource().logicalAddress != message.getSource() || isProhibitMode()) {
            return true;
        }
        int portId = getPrevPortId();
        if (portId != -1) {
            HdmiDeviceInfo inactiveSource = getCecDeviceInfo(message.getSource());
            if (inactiveSource == null || this.mService.pathToPortId(inactiveSource.getPhysicalAddress()) == portId) {
                return true;
            }
            doManualPortSwitching(portId, null);
            setPrevPortId(-1);
        } else {
            this.mActiveSource.invalidate();
            setActivePath(NetworkConstants.ARP_HWTYPE_RESERVED_HI);
            this.mService.invokeInputChangeListener(HdmiDeviceInfo.INACTIVE_DEVICE);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleRequestActiveSource(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mAddress == getActiveSource().logicalAddress) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(this.mAddress, getActivePath()));
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleGetMenuLanguage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!broadcastMenuLanguage(this.mService.getLanguage())) {
            Slog.w(TAG, "Failed to respond to <Get Menu Language>: " + message.toString());
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean broadcastMenuLanguage(String language) {
        assertRunOnServiceThread();
        HdmiCecMessage command = HdmiCecMessageBuilder.buildSetMenuLanguageCommand(this.mAddress, language);
        if (command == null) {
            return false;
        }
        this.mService.sendCecCommand(command);
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleReportPhysicalAddress(HdmiCecMessage message) {
        assertRunOnServiceThread();
        int path = HdmiUtils.twoBytesToInt(message.getParams());
        int address = message.getSource();
        byte type = message.getParams()[2];
        if (updateCecSwitchInfo(address, type, path)) {
            return true;
        }
        if (hasAction(DeviceDiscoveryAction.class)) {
            Slog.i(TAG, "Ignored while Device Discovery Action is in progress: " + message);
            return true;
        }
        if (!isInDeviceList(address, path)) {
            handleNewDeviceAtTheTailOfActivePath(path);
        }
        HdmiDeviceInfo deviceInfo = new HdmiDeviceInfo(address, path, getPortId(path), type, UsbAudioDevice.kAudioDeviceClassMask, HdmiUtils.getDefaultDeviceName(address));
        addCecDevice(deviceInfo);
        startNewDeviceAction(HdmiCecLocalDevice.ActiveSource.of(address, path), type);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleReportPowerStatus(HdmiCecMessage command) {
        updateDevicePowerStatus(command.getSource(), command.getParams()[0] & 255);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleTimerStatus(HdmiCecMessage message) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleRecordStatus(HdmiCecMessage message) {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean updateCecSwitchInfo(int address, int type, int path) {
        if (address == 15 && type == 6) {
            this.mCecSwitches.add(Integer.valueOf(path));
            updateSafeDeviceInfoList();
            return true;
        }
        if (type == 5) {
            this.mCecSwitches.add(Integer.valueOf(path));
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void startNewDeviceAction(HdmiCecLocalDevice.ActiveSource activeSource, int deviceType) {
        for (NewDeviceAction action : getActions(NewDeviceAction.class)) {
            if (action.isActionOf(activeSource)) {
                return;
            }
        }
        addAndStartAction(new NewDeviceAction(this, activeSource.logicalAddress, activeSource.physicalAddress, deviceType));
    }

    private boolean handleNewDeviceAtTheTailOfActivePath(int path) {
        if (!isTailOfActivePath(path, getActivePath())) {
            return false;
        }
        int newPath = this.mService.portIdToPath(getActivePortId());
        setActivePath(newPath);
        startRoutingControl(getActivePath(), newPath, false, null);
        return true;
    }

    static boolean isTailOfActivePath(int path, int activePath) {
        if (activePath == 0) {
            return false;
        }
        for (int i = 12; i >= 0; i -= 4) {
            int curActivePath = (activePath >> i) & 15;
            if (curActivePath == 0) {
                return true;
            }
            if (((path >> i) & 15) != curActivePath) {
                return false;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleRoutingChange(HdmiCecMessage message) {
        assertRunOnServiceThread();
        byte[] params = message.getParams();
        if (HdmiUtils.isAffectingActiveRoutingPath(getActivePath(), HdmiUtils.twoBytesToInt(params))) {
            this.mActiveSource.invalidate();
            removeAction(RoutingControlAction.class);
            addAndStartAction(new RoutingControlAction(this, HdmiUtils.twoBytesToInt(params, 2), true, null));
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleReportAudioStatus(HdmiCecMessage message) {
        assertRunOnServiceThread();
        setAudioStatus(HdmiUtils.isAudioStatusMute(message), HdmiUtils.getAudioStatusVolume(message));
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleTextViewOn(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mService.isPowerStandbyOrTransient() && this.mAutoWakeup) {
            this.mService.wakeUp();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleImageViewOn(HdmiCecMessage message) {
        assertRunOnServiceThread();
        return handleTextViewOn(message);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleSetOsdName(HdmiCecMessage message) {
        HdmiDeviceInfo deviceInfo = getCecDeviceInfo(message.getSource());
        if (deviceInfo == null) {
            Slog.e(TAG, "No source device info for <Set Osd Name>." + message);
            return true;
        }
        try {
            String osdName = new String(message.getParams(), "US-ASCII");
            if (deviceInfo.getDisplayName().equals(osdName)) {
                Slog.i(TAG, "Ignore incoming <Set Osd Name> having same osd name:" + message);
                return true;
            }
            HdmiDeviceInfo hdmiDeviceInfo = new HdmiDeviceInfo(deviceInfo.getLogicalAddress(), deviceInfo.getPhysicalAddress(), deviceInfo.getPortId(), deviceInfo.getDeviceType(), deviceInfo.getVendorId(), osdName);
            addCecDevice(hdmiDeviceInfo);
            return true;
        } catch (UnsupportedEncodingException e) {
            Slog.e(TAG, "Invalid <Set Osd Name> request:" + message, e);
            return true;
        }
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void launchDeviceDiscovery() {
        assertRunOnServiceThread();
        clearDeviceInfoList();
        addAndStartAction(new DeviceDiscoveryAction(this, new DeviceDiscoveryAction.DeviceDiscoveryCallback() {
            public void onDeviceDiscoveryDone(List<HdmiDeviceInfo> deviceInfos) {
                for (HdmiDeviceInfo info : deviceInfos) {
                    HdmiCecLocalDeviceTv.this.addCecDevice(info);
                }
                for (HdmiCecLocalDevice device : HdmiCecLocalDeviceTv.this.mService.getAllLocalDevices()) {
                    HdmiCecLocalDeviceTv.this.addCecDevice(device.getDeviceInfo());
                }
                HdmiCecLocalDeviceTv.this.mSelectRequestBuffer.process();
                HdmiCecLocalDeviceTv.this.resetSelectRequestBuffer();
                HdmiCecLocalDeviceTv.this.addAndStartAction(new HotplugDetectionAction(HdmiCecLocalDeviceTv.this));
                HdmiCecLocalDeviceTv.this.addAndStartAction(new PowerStatusMonitorAction(HdmiCecLocalDeviceTv.this));
                HdmiDeviceInfo avr = HdmiCecLocalDeviceTv.this.getAvrDeviceInfo();
                if (avr != null) {
                    HdmiCecLocalDeviceTv.this.onNewAvrAdded(avr);
                } else {
                    HdmiCecLocalDeviceTv.this.setSystemAudioMode(false);
                }
            }
        }));
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onNewAvrAdded(HdmiDeviceInfo avr) {
        assertRunOnServiceThread();
        addAndStartAction(new SystemAudioAutoInitiationAction(this, avr.getLogicalAddress()));
        if (isConnected(avr.getPortId()) && isArcFeatureEnabled(avr.getPortId()) && !hasAction(SetArcTransmissionStateAction.class)) {
            startArcAction(true);
        }
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void clearDeviceInfoList() {
        assertRunOnServiceThread();
        for (HdmiDeviceInfo info : this.mSafeExternalInputs) {
            invokeDeviceEventListener(info, 2);
        }
        this.mDeviceInfos.clear();
        updateSafeDeviceInfoList();
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void changeSystemAudioMode(boolean enabled, IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled() || hasAction(DeviceDiscoveryAction.class)) {
            setSystemAudioMode(false);
            invokeCallback(callback, 6);
            return;
        }
        HdmiDeviceInfo avr = getAvrDeviceInfo();
        if (avr == null) {
            setSystemAudioMode(false);
            invokeCallback(callback, 3);
            return;
        }
        addAndStartAction(new SystemAudioActionFromTv(this, avr.getLogicalAddress(), enabled, callback));
    }

    /* access modifiers changed from: package-private */
    public void setSystemAudioMode(boolean on) {
        if (isSystemAudioControlFeatureEnabled() || !on) {
            HdmiLogger.debug("System Audio Mode change[old:%b new:%b]", Boolean.valueOf(this.mSystemAudioActivated), Boolean.valueOf(on));
            updateAudioManagerForSystemAudio(on);
            synchronized (this.mLock) {
                if (this.mSystemAudioActivated != on) {
                    this.mSystemAudioActivated = on;
                    this.mService.announceSystemAudioModeChange(on);
                }
                startArcAction(on);
            }
            return;
        }
        HdmiLogger.debug("Cannot turn on system audio mode because the System Audio Control feature is disabled.", new Object[0]);
    }

    private void updateAudioManagerForSystemAudio(boolean on) {
        HdmiLogger.debug("[A]UpdateSystemAudio mode[on=%b] output=[%X]", Boolean.valueOf(on), Integer.valueOf(this.mService.getAudioManager().setHdmiSystemAudioSupported(on)));
    }

    /* access modifiers changed from: package-private */
    public boolean isSystemAudioActivated() {
        boolean z;
        if (!hasSystemAudioDevice()) {
            return false;
        }
        synchronized (this.mLock) {
            z = this.mSystemAudioActivated;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setSystemAudioControlFeatureEnabled(boolean enabled) {
        assertRunOnServiceThread();
        synchronized (this.mLock) {
            this.mSystemAudioControlFeatureEnabled = enabled;
        }
        if (hasSystemAudioDevice()) {
            changeSystemAudioMode(enabled, null);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSystemAudioControlFeatureEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSystemAudioControlFeatureEnabled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean setArcStatus(boolean enabled) {
        assertRunOnServiceThread();
        HdmiLogger.debug("Set Arc Status[old:%b new:%b]", Boolean.valueOf(this.mArcEstablished), Boolean.valueOf(enabled));
        boolean oldStatus = this.mArcEstablished;
        enableAudioReturnChannel(enabled);
        notifyArcStatusToAudioService(enabled);
        this.mArcEstablished = enabled;
        return oldStatus;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void enableAudioReturnChannel(boolean enabled) {
        assertRunOnServiceThread();
        HdmiDeviceInfo avr = getAvrDeviceInfo();
        if (avr != null) {
            this.mService.enableAudioReturnChannel(avr.getPortId(), enabled);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isConnected(int portId) {
        assertRunOnServiceThread();
        return this.mService.isConnected(portId);
    }

    private void notifyArcStatusToAudioService(boolean enabled) {
        this.mService.getAudioManager().setWiredDeviceConnectionState(262144, enabled, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isArcEstablished() {
        assertRunOnServiceThread();
        if (this.mArcEstablished) {
            for (int i = 0; i < this.mArcFeatureEnabled.size(); i++) {
                if (this.mArcFeatureEnabled.valueAt(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void changeArcFeatureEnabled(int portId, boolean enabled) {
        assertRunOnServiceThread();
        if (this.mArcFeatureEnabled.get(portId) != enabled) {
            this.mArcFeatureEnabled.put(portId, enabled);
            HdmiDeviceInfo avr = getAvrDeviceInfo();
            if (avr != null && avr.getPortId() == portId) {
                if (enabled && !this.mArcEstablished) {
                    startArcAction(true);
                } else if (!enabled && this.mArcEstablished) {
                    startArcAction(false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isArcFeatureEnabled(int portId) {
        assertRunOnServiceThread();
        return this.mArcFeatureEnabled.get(portId);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void startArcAction(boolean enabled) {
        assertRunOnServiceThread();
        HdmiDeviceInfo info = getAvrDeviceInfo();
        if (info == null) {
            Slog.w(TAG, "Failed to start arc action; No AVR device.");
        } else if (!canStartArcUpdateAction(info.getLogicalAddress(), enabled)) {
            Slog.w(TAG, "Failed to start arc action; ARC configuration check failed.");
            if (enabled && !isConnectedToArcPort(info.getPhysicalAddress())) {
                displayOsd(1);
            }
        } else {
            if (enabled) {
                removeAction(RequestArcTerminationAction.class);
                if (!hasAction(RequestArcInitiationAction.class)) {
                    addAndStartAction(new RequestArcInitiationAction(this, info.getLogicalAddress()));
                }
            } else {
                removeAction(RequestArcInitiationAction.class);
                if (!hasAction(RequestArcTerminationAction.class)) {
                    addAndStartAction(new RequestArcTerminationAction(this, info.getLogicalAddress()));
                }
            }
        }
    }

    private boolean isDirectConnectAddress(int physicalAddress) {
        return (61440 & physicalAddress) == physicalAddress;
    }

    /* access modifiers changed from: package-private */
    public void setAudioStatus(boolean mute, int volume) {
        int i;
        if (isSystemAudioActivated()) {
            synchronized (this.mLock) {
                this.mSystemAudioMute = mute;
                this.mSystemAudioVolume = volume;
                this.mService.setAudioStatus(mute, VolumeControlAction.scaleToCustomVolume(volume, this.mService.getAudioManager().getStreamMaxVolume(3)));
                if (mute) {
                    i = 101;
                } else {
                    i = volume;
                }
                displayOsd(2, i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002f, code lost:
        r2 = getActions(com.android.server.hdmi.VolumeControlAction.class);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003a, code lost:
        if (r2.isEmpty() == false) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003c, code lost:
        r6 = getAvrDeviceInfo().getLogicalAddress();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0046, code lost:
        if (r9 <= 0) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        addAndStartAction(new com.android.server.hdmi.VolumeControlAction(r7, r6, r4));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0051, code lost:
        r3 = r2.get(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0057, code lost:
        if (r9 <= 0) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0059, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005b, code lost:
        r3.handleVolumeChange(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005e, code lost:
        return;
     */
    @HdmiAnnotations.ServiceThreadOnly
    public void changeVolume(int curVolume, int delta, int maxVolume) {
        assertRunOnServiceThread();
        if (getAvrDeviceInfo() != null && delta != 0 && isSystemAudioActivated()) {
            int cecVolume = VolumeControlAction.scaleToCecVolume(curVolume + delta, maxVolume);
            synchronized (this.mLock) {
                boolean z = false;
                if (cecVolume == this.mSystemAudioVolume) {
                    this.mService.setAudioStatus(false, VolumeControlAction.scaleToCustomVolume(this.mSystemAudioVolume, maxVolume));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002e, code lost:
        if (isSystemAudioActivated() != false) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0030, code lost:
        com.android.server.hdmi.HdmiLogger.debug("[A]:System audio is not activated.", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        removeAction(com.android.server.hdmi.VolumeControlAction.class);
        sendUserControlPressedAndReleased(getAvrDeviceInfo().getLogicalAddress(), com.android.server.hdmi.HdmiCecKeycode.getMuteKey(r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        return;
     */
    @HdmiAnnotations.ServiceThreadOnly
    public void changeMute(boolean mute) {
        assertRunOnServiceThread();
        if (getAvrDeviceInfo() != null) {
            HdmiLogger.debug("[A]:Change mute:%b", Boolean.valueOf(mute));
            synchronized (this.mLock) {
                if (this.mSystemAudioMute == mute) {
                    HdmiLogger.debug("No need to change mute.", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleInitiateArc(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (canStartArcUpdateAction(message.getSource(), true)) {
            removeAction(RequestArcInitiationAction.class);
            addAndStartAction(new SetArcTransmissionStateAction(this, message.getSource(), true));
            return true;
        } else if (getAvrDeviceInfo() == null) {
            this.mDelayedMessageBuffer.add(message);
            return true;
        } else {
            this.mService.maySendFeatureAbortCommand(message, 4);
            if (!isConnectedToArcPort(message.getSource())) {
                displayOsd(1);
            }
            return true;
        }
    }

    private boolean canStartArcUpdateAction(int avrAddress, boolean enabled) {
        HdmiDeviceInfo avr = getAvrDeviceInfo();
        boolean z = false;
        if (avr == null || avrAddress != avr.getLogicalAddress() || !isConnectedToArcPort(avr.getPhysicalAddress()) || !isDirectConnectAddress(avr.getPhysicalAddress())) {
            return false;
        }
        if (!enabled) {
            return true;
        }
        if (isConnected(avr.getPortId()) && isArcFeatureEnabled(avr.getPortId())) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleTerminateArc(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mService.isPowerStandbyOrTransient()) {
            setArcStatus(false);
            return true;
        }
        removeAction(RequestArcTerminationAction.class);
        addAndStartAction(new SetArcTransmissionStateAction(this, message.getSource(), false));
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleSetSystemAudioMode(HdmiCecMessage message) {
        assertRunOnServiceThread();
        boolean systemAudioStatus = HdmiUtils.parseCommandParamSystemAudioStatus(message);
        if (!isMessageForSystemAudio(message)) {
            if (getAvrDeviceInfo() == null) {
                this.mDelayedMessageBuffer.add(message);
            } else {
                HdmiLogger.warning("Invalid <Set System Audio Mode> message:" + message, new Object[0]);
                this.mService.maySendFeatureAbortCommand(message, 4);
            }
            return true;
        } else if (!systemAudioStatus || isSystemAudioControlFeatureEnabled()) {
            removeAction(SystemAudioAutoInitiationAction.class);
            addAndStartAction(new SystemAudioActionFromAvr(this, message.getSource(), systemAudioStatus, null));
            return true;
        } else {
            HdmiLogger.debug("Ignoring <Set System Audio Mode> message because the System Audio Control feature is disabled: %s", message);
            this.mService.maySendFeatureAbortCommand(message, 4);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleSystemAudioModeStatus(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!isMessageForSystemAudio(message)) {
            HdmiLogger.warning("Invalid <System Audio Mode Status> message:" + message, new Object[0]);
            return true;
        }
        setSystemAudioMode(HdmiUtils.parseCommandParamSystemAudioStatus(message));
        return true;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleRecordTvScreen(HdmiCecMessage message) {
        List<OneTouchRecordAction> actions = getActions(OneTouchRecordAction.class);
        if (!actions.isEmpty()) {
            if (actions.get(0).getRecorderAddress() != message.getSource()) {
                announceOneTouchRecordResult(message.getSource(), 48);
            }
            return super.handleRecordTvScreen(message);
        }
        int recorderAddress = message.getSource();
        int reason = startOneTouchRecord(recorderAddress, this.mService.invokeRecordRequestListener(recorderAddress));
        if (reason != -1) {
            this.mService.maySendFeatureAbortCommand(message, reason);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleTimerClearedStatus(HdmiCecMessage message) {
        announceTimerRecordingResult(message.getSource(), message.getParams()[0] & 255);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void announceOneTouchRecordResult(int recorderAddress, int result) {
        this.mService.invokeOneTouchRecordResult(recorderAddress, result);
    }

    /* access modifiers changed from: package-private */
    public void announceTimerRecordingResult(int recorderAddress, int result) {
        this.mService.invokeTimerRecordingResult(recorderAddress, result);
    }

    /* access modifiers changed from: package-private */
    public void announceClearTimerRecordingResult(int recorderAddress, int result) {
        this.mService.invokeClearTimerRecordingResult(recorderAddress, result);
    }

    private boolean isMessageForSystemAudio(HdmiCecMessage message) {
        return this.mService.isControlEnabled() && message.getSource() == 5 && (message.getDestination() == 0 || message.getDestination() == 15) && getAvrDeviceInfo() != null;
    }

    @HdmiAnnotations.ServiceThreadOnly
    private HdmiDeviceInfo addDeviceInfo(HdmiDeviceInfo deviceInfo) {
        assertRunOnServiceThread();
        HdmiDeviceInfo oldDeviceInfo = getCecDeviceInfo(deviceInfo.getLogicalAddress());
        if (oldDeviceInfo != null) {
            removeDeviceInfo(deviceInfo.getId());
        }
        this.mDeviceInfos.append(deviceInfo.getId(), deviceInfo);
        updateSafeDeviceInfoList();
        return oldDeviceInfo;
    }

    @HdmiAnnotations.ServiceThreadOnly
    private HdmiDeviceInfo removeDeviceInfo(int id) {
        assertRunOnServiceThread();
        HdmiDeviceInfo deviceInfo = this.mDeviceInfos.get(id);
        if (deviceInfo != null) {
            this.mDeviceInfos.remove(id);
        }
        updateSafeDeviceInfoList();
        return deviceInfo;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public List<HdmiDeviceInfo> getDeviceInfoList(boolean includeLocalDevice) {
        assertRunOnServiceThread();
        if (includeLocalDevice) {
            return HdmiUtils.sparseArrayToList(this.mDeviceInfos);
        }
        ArrayList<HdmiDeviceInfo> infoList = new ArrayList<>();
        for (int i = 0; i < this.mDeviceInfos.size(); i++) {
            HdmiDeviceInfo info = this.mDeviceInfos.valueAt(i);
            if (!isLocalDeviceAddress(info.getLogicalAddress())) {
                infoList.add(info);
            }
        }
        return infoList;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public List<HdmiDeviceInfo> getSafeExternalInputsLocked() {
        return this.mSafeExternalInputs;
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void updateSafeDeviceInfoList() {
        assertRunOnServiceThread();
        List<HdmiDeviceInfo> copiedDevices = HdmiUtils.sparseArrayToList(this.mDeviceInfos);
        List<HdmiDeviceInfo> externalInputs = getInputDevices();
        synchronized (this.mLock) {
            this.mSafeAllDeviceInfos = copiedDevices;
            this.mSafeExternalInputs = externalInputs;
        }
    }

    private List<HdmiDeviceInfo> getInputDevices() {
        ArrayList<HdmiDeviceInfo> infoList = new ArrayList<>();
        for (int i = 0; i < this.mDeviceInfos.size(); i++) {
            HdmiDeviceInfo info = this.mDeviceInfos.valueAt(i);
            if (!isLocalDeviceAddress(info.getLogicalAddress()) && info.isSourceType() && !hideDevicesBehindLegacySwitch(info)) {
                infoList.add(info);
            }
        }
        return infoList;
    }

    private boolean hideDevicesBehindLegacySwitch(HdmiDeviceInfo info) {
        return !isConnectedToCecSwitch(info.getPhysicalAddress(), this.mCecSwitches);
    }

    private static boolean isConnectedToCecSwitch(int path, Collection<Integer> switches) {
        for (Integer intValue : switches) {
            if (isParentPath(intValue.intValue(), path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isParentPath(int parentPath, int childPath) {
        boolean z = false;
        for (int i = 0; i <= 12; i += 4) {
            if (((childPath >> i) & 15) != 0) {
                if (((parentPath >> i) & 15) == 0 && (childPath >> (i + 4)) == (parentPath >> (i + 4))) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    private void invokeDeviceEventListener(HdmiDeviceInfo info, int status) {
        if (!hideDevicesBehindLegacySwitch(info)) {
            this.mService.invokeDeviceEventListeners(info, status);
        }
    }

    private boolean isLocalDeviceAddress(int address) {
        return this.mLocalDeviceAddresses.contains(Integer.valueOf(address));
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public HdmiDeviceInfo getAvrDeviceInfo() {
        assertRunOnServiceThread();
        return getCecDeviceInfo(5);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public HdmiDeviceInfo getCecDeviceInfo(int logicalAddress) {
        assertRunOnServiceThread();
        return this.mDeviceInfos.get(HdmiDeviceInfo.idForCecDevice(logicalAddress));
    }

    /* access modifiers changed from: package-private */
    public boolean hasSystemAudioDevice() {
        return getSafeAvrDeviceInfo() != null;
    }

    /* access modifiers changed from: package-private */
    public HdmiDeviceInfo getSafeAvrDeviceInfo() {
        return getSafeCecDeviceInfo(5);
    }

    /* access modifiers changed from: package-private */
    public HdmiDeviceInfo getSafeCecDeviceInfo(int logicalAddress) {
        synchronized (this.mLock) {
            for (HdmiDeviceInfo info : this.mSafeAllDeviceInfos) {
                if (info.isCecDevice() && info.getLogicalAddress() == logicalAddress) {
                    return info;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public List<HdmiDeviceInfo> getSafeCecDevicesLocked() {
        ArrayList<HdmiDeviceInfo> infoList = new ArrayList<>();
        for (HdmiDeviceInfo info : this.mSafeAllDeviceInfos) {
            if (!isLocalDeviceAddress(info.getLogicalAddress())) {
                infoList.add(info);
            }
        }
        return infoList;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public final void addCecDevice(HdmiDeviceInfo info) {
        assertRunOnServiceThread();
        HdmiDeviceInfo old = addDeviceInfo(info);
        if (info.getLogicalAddress() != this.mAddress) {
            if (old == null) {
                invokeDeviceEventListener(info, 1);
            } else if (!old.equals(info)) {
                invokeDeviceEventListener(old, 2);
                invokeDeviceEventListener(info, 1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public final void removeCecDevice(int address) {
        assertRunOnServiceThread();
        HdmiDeviceInfo info = removeDeviceInfo(HdmiDeviceInfo.idForCecDevice(address));
        this.mCecMessageCache.flushMessagesFrom(address);
        invokeDeviceEventListener(info, 2);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void handleRemoveActiveRoutingPath(int path) {
        assertRunOnServiceThread();
        if (isTailOfActivePath(path, getActivePath())) {
            startRoutingControl(getActivePath(), this.mService.portIdToPath(getActivePortId()), true, null);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void launchRoutingControl(boolean routingForBootup) {
        assertRunOnServiceThread();
        if (getActivePortId() == -1) {
            int activePath = this.mService.getPhysicalAddress();
            setActivePath(activePath);
            if (!routingForBootup && !this.mDelayedMessageBuffer.isBuffered(130)) {
                this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(this.mAddress, activePath));
            }
        } else if (!routingForBootup && !isProhibitMode()) {
            int newPath = this.mService.portIdToPath(getActivePortId());
            setActivePath(newPath);
            startRoutingControl(getActivePath(), newPath, routingForBootup, null);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public final HdmiDeviceInfo getDeviceInfoByPath(int path) {
        assertRunOnServiceThread();
        for (HdmiDeviceInfo info : getDeviceInfoList(false)) {
            if (info.getPhysicalAddress() == path) {
                return info;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public HdmiDeviceInfo getSafeDeviceInfoByPath(int path) {
        synchronized (this.mLock) {
            for (HdmiDeviceInfo info : this.mSafeAllDeviceInfos) {
                if (info.getPhysicalAddress() == path) {
                    return info;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isInDeviceList(int logicalAddress, int physicalAddress) {
        assertRunOnServiceThread();
        HdmiDeviceInfo device = getCecDeviceInfo(logicalAddress);
        boolean z = false;
        if (device == null) {
            return false;
        }
        if (device.getPhysicalAddress() == physicalAddress) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onHotplug(int portId, boolean connected) {
        assertRunOnServiceThread();
        if (!connected) {
            removeCecSwitches(portId);
        }
        List<HotplugDetectionAction> hotplugActions = getActions(HotplugDetectionAction.class);
        if (!hotplugActions.isEmpty()) {
            hotplugActions.get(0).pollAllDevicesNow();
        }
    }

    private void removeCecSwitches(int portId) {
        Iterator<Integer> it = this.mCecSwitches.iterator();
        while (!it.hasNext()) {
            if (pathToPortId(it.next().intValue()) == portId) {
                it.remove();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setAutoDeviceOff(boolean enabled) {
        assertRunOnServiceThread();
        this.mAutoDeviceOff = enabled;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setAutoWakeup(boolean enabled) {
        assertRunOnServiceThread();
        this.mAutoWakeup = enabled;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean getAutoWakeup() {
        assertRunOnServiceThread();
        return this.mAutoWakeup;
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void disableDevice(boolean initiatedByCec, HdmiCecLocalDevice.PendingActionClearedCallback callback) {
        assertRunOnServiceThread();
        this.mService.unregisterTvInputCallback(this.mTvInputCallback);
        removeAction(DeviceDiscoveryAction.class);
        removeAction(HotplugDetectionAction.class);
        removeAction(PowerStatusMonitorAction.class);
        removeAction(OneTouchRecordAction.class);
        removeAction(TimerRecordingAction.class);
        disableSystemAudioIfExist();
        disableArcIfExist();
        super.disableDevice(initiatedByCec, callback);
        clearDeviceInfoList();
        getActiveSource().invalidate();
        setActivePath(NetworkConstants.ARP_HWTYPE_RESERVED_HI);
        checkIfPendingActionsCleared();
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void disableSystemAudioIfExist() {
        assertRunOnServiceThread();
        if (getAvrDeviceInfo() != null) {
            removeAction(SystemAudioActionFromAvr.class);
            removeAction(SystemAudioActionFromTv.class);
            removeAction(SystemAudioAutoInitiationAction.class);
            removeAction(SystemAudioStatusAction.class);
            removeAction(VolumeControlAction.class);
        }
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void disableArcIfExist() {
        assertRunOnServiceThread();
        HdmiDeviceInfo avr = getAvrDeviceInfo();
        if (avr != null) {
            removeAction(RequestArcInitiationAction.class);
            if (!hasAction(RequestArcTerminationAction.class) && isArcEstablished()) {
                addAndStartAction(new RequestArcTerminationAction(this, avr.getLogicalAddress()));
            }
        }
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    public void onStandby(boolean initiatedByCec, int standbyAction) {
        assertRunOnServiceThread();
        if (this.mService.isControlEnabled() && !initiatedByCec && this.mAutoDeviceOff) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, 15));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isProhibitMode() {
        return this.mService.isProhibitMode();
    }

    /* access modifiers changed from: package-private */
    public boolean isPowerStandbyOrTransient() {
        return this.mService.isPowerStandbyOrTransient();
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void displayOsd(int messageId) {
        assertRunOnServiceThread();
        this.mService.displayOsd(messageId);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void displayOsd(int messageId, int extra) {
        assertRunOnServiceThread();
        this.mService.displayOsd(messageId, extra);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int startOneTouchRecord(int recorderAddress, byte[] recordSource) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not start one touch record. CEC control is disabled.");
            announceOneTouchRecordResult(recorderAddress, 51);
            return 1;
        } else if (!checkRecorder(recorderAddress)) {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceOneTouchRecordResult(recorderAddress, 49);
            return 1;
        } else if (!checkRecordSource(recordSource)) {
            Slog.w(TAG, "Invalid record source." + Arrays.toString(recordSource));
            announceOneTouchRecordResult(recorderAddress, 50);
            return 2;
        } else {
            addAndStartAction(new OneTouchRecordAction(this, recorderAddress, recordSource));
            Slog.i(TAG, "Start new [One Touch Record]-Target:" + recorderAddress + ", recordSource:" + Arrays.toString(recordSource));
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void stopOneTouchRecord(int recorderAddress) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not stop one touch record. CEC control is disabled.");
            announceOneTouchRecordResult(recorderAddress, 51);
        } else if (!checkRecorder(recorderAddress)) {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceOneTouchRecordResult(recorderAddress, 49);
        } else {
            removeAction(OneTouchRecordAction.class);
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildRecordOff(this.mAddress, recorderAddress));
            Slog.i(TAG, "Stop [One Touch Record]-Target:" + recorderAddress);
        }
    }

    private boolean checkRecorder(int recorderAddress) {
        if (getCecDeviceInfo(recorderAddress) == null || HdmiUtils.getTypeFromAddress(recorderAddress) != 1) {
            return false;
        }
        return true;
    }

    private boolean checkRecordSource(byte[] recordSource) {
        return recordSource != null && HdmiRecordSources.checkRecordSource(recordSource);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not start one touch record. CEC control is disabled.");
            announceTimerRecordingResult(recorderAddress, 3);
        } else if (!checkRecorder(recorderAddress)) {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceTimerRecordingResult(recorderAddress, 1);
        } else if (!checkTimerRecordingSource(sourceType, recordSource)) {
            Slog.w(TAG, "Invalid record source." + Arrays.toString(recordSource));
            announceTimerRecordingResult(recorderAddress, 2);
        } else {
            addAndStartAction(new TimerRecordingAction(this, recorderAddress, sourceType, recordSource));
            Slog.i(TAG, "Start [Timer Recording]-Target:" + recorderAddress + ", SourceType:" + sourceType + ", RecordSource:" + Arrays.toString(recordSource));
        }
    }

    private boolean checkTimerRecordingSource(int sourceType, byte[] recordSource) {
        return recordSource != null && HdmiTimerRecordSources.checkTimerRecordSource(sourceType, recordSource);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not start one touch record. CEC control is disabled.");
            announceClearTimerRecordingResult(recorderAddress, 162);
        } else if (!checkRecorder(recorderAddress)) {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceClearTimerRecordingResult(recorderAddress, 160);
        } else if (!checkTimerRecordingSource(sourceType, recordSource)) {
            Slog.w(TAG, "Invalid record source." + Arrays.toString(recordSource));
            announceClearTimerRecordingResult(recorderAddress, 161);
        } else {
            sendClearTimerMessage(recorderAddress, sourceType, recordSource);
        }
    }

    private void sendClearTimerMessage(final int recorderAddress, int sourceType, byte[] recordSource) {
        HdmiCecMessage message;
        switch (sourceType) {
            case 1:
                message = HdmiCecMessageBuilder.buildClearDigitalTimer(this.mAddress, recorderAddress, recordSource);
                break;
            case 2:
                message = HdmiCecMessageBuilder.buildClearAnalogueTimer(this.mAddress, recorderAddress, recordSource);
                break;
            case 3:
                message = HdmiCecMessageBuilder.buildClearExternalTimer(this.mAddress, recorderAddress, recordSource);
                break;
            default:
                Slog.w(TAG, "Invalid source type:" + recorderAddress);
                announceClearTimerRecordingResult(recorderAddress, 161);
                return;
        }
        this.mService.sendCecCommand(message, new HdmiControlService.SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    HdmiCecLocalDeviceTv.this.announceClearTimerRecordingResult(recorderAddress, 161);
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void updateDevicePowerStatus(int logicalAddress, int newPowerStatus) {
        HdmiDeviceInfo info = getCecDeviceInfo(logicalAddress);
        if (info == null) {
            Slog.w(TAG, "Can not update power status of non-existing device:" + logicalAddress);
        } else if (info.getDevicePowerStatus() != newPowerStatus) {
            HdmiDeviceInfo newInfo = HdmiUtils.cloneHdmiDeviceInfo(info, newPowerStatus);
            addDeviceInfo(newInfo);
            invokeDeviceEventListener(newInfo, 3);
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleMenuStatus(HdmiCecMessage message) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void sendStandby(int deviceId) {
        HdmiDeviceInfo targetDevice = this.mDeviceInfos.get(deviceId);
        if (targetDevice != null) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, targetDevice.getLogicalAddress()));
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void processAllDelayedMessages() {
        assertRunOnServiceThread();
        this.mDelayedMessageBuffer.processAllMessages();
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void processDelayedMessages(int address) {
        assertRunOnServiceThread();
        this.mDelayedMessageBuffer.processMessagesForDevice(address);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void processDelayedActiveSource(int address) {
        assertRunOnServiceThread();
        this.mDelayedMessageBuffer.processActiveSource(address);
    }

    /* access modifiers changed from: protected */
    public void dump(IndentingPrintWriter pw) {
        super.dump(pw);
        pw.println("mArcEstablished: " + this.mArcEstablished);
        pw.println("mArcFeatureEnabled: " + this.mArcFeatureEnabled);
        pw.println("mSystemAudioActivated: " + this.mSystemAudioActivated);
        pw.println("mSystemAudioMute: " + this.mSystemAudioMute);
        pw.println("mSystemAudioControlFeatureEnabled: " + this.mSystemAudioControlFeatureEnabled);
        pw.println("mAutoDeviceOff: " + this.mAutoDeviceOff);
        pw.println("mAutoWakeup: " + this.mAutoWakeup);
        pw.println("mSkipRoutingControl: " + this.mSkipRoutingControl);
        pw.println("mPrevPortId: " + this.mPrevPortId);
        pw.println("CEC devices:");
        pw.increaseIndent();
        for (HdmiDeviceInfo info : this.mSafeAllDeviceInfos) {
            pw.println(info);
        }
        pw.decreaseIndent();
    }
}
