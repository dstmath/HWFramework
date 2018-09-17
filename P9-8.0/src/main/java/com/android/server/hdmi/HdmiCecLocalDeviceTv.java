package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiPortInfo;
import android.hardware.hdmi.HdmiRecordSources;
import android.hardware.hdmi.HdmiTimerRecordSources;
import android.hardware.hdmi.IHdmiControlCallback;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager.TvInputCallback;
import android.net.util.NetworkConstants;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.hdmi.HdmiAnnotations.ServiceThreadOnly;
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
    @ServiceThreadOnly
    private boolean mArcEstablished = false;
    private final SparseBooleanArray mArcFeatureEnabled = new SparseBooleanArray();
    private boolean mAutoDeviceOff = this.mService.readBooleanSetting("hdmi_control_auto_device_off_enabled", true);
    private boolean mAutoWakeup = this.mService.readBooleanSetting("hdmi_control_auto_wakeup_enabled", true);
    private final ArraySet<Integer> mCecSwitches = new ArraySet();
    private final DelayedMessageBuffer mDelayedMessageBuffer = new DelayedMessageBuffer(this);
    private final SparseArray<HdmiDeviceInfo> mDeviceInfos = new SparseArray();
    private List<Integer> mLocalDeviceAddresses;
    @GuardedBy("mLock")
    private int mPrevPortId = -1;
    @GuardedBy("mLock")
    private List<HdmiDeviceInfo> mSafeAllDeviceInfos = Collections.emptyList();
    @GuardedBy("mLock")
    private List<HdmiDeviceInfo> mSafeExternalInputs = Collections.emptyList();
    private SelectRequestBuffer mSelectRequestBuffer;
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
    private final TvInputCallback mTvInputCallback = new TvInputCallback() {
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
    private final HashMap<String, Integer> mTvInputs = new HashMap();

    @ServiceThreadOnly
    private void addTvInput(String inputId, int deviceId) {
        assertRunOnServiceThread();
        this.mTvInputs.put(inputId, Integer.valueOf(deviceId));
    }

    @ServiceThreadOnly
    private void removeTvInput(String inputId) {
        assertRunOnServiceThread();
        this.mTvInputs.remove(inputId);
    }

    @ServiceThreadOnly
    protected boolean isInputReady(int deviceId) {
        assertRunOnServiceThread();
        return this.mTvInputs.containsValue(Integer.valueOf(deviceId));
    }

    HdmiCecLocalDeviceTv(HdmiControlService service) {
        super(service, 0);
        this.mStandbyHandler = new HdmiCecStandbyModeHandler(service, this);
    }

    @ServiceThreadOnly
    protected void onAddressAllocated(int logicalAddress, int reason) {
        boolean z = false;
        assertRunOnServiceThread();
        for (HdmiPortInfo port : this.mService.getPortInfo()) {
            this.mArcFeatureEnabled.put(port.getId(), port.isArcSupported());
        }
        this.mService.registerTvInputCallback(this.mTvInputCallback);
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildReportPhysicalAddressCommand(this.mAddress, this.mService.getPhysicalAddress(), this.mDeviceType));
        this.mService.sendCecCommand(HdmiCecMessageBuilder.buildDeviceVendorIdCommand(this.mAddress, this.mService.getVendorId()));
        this.mCecSwitches.add(Integer.valueOf(this.mService.getPhysicalAddress()));
        this.mTvInputs.clear();
        this.mSkipRoutingControl = reason == 3;
        if (!(reason == 0 || reason == 1)) {
            z = true;
        }
        launchRoutingControl(z);
        this.mLocalDeviceAddresses = initLocalDeviceAddresses();
        resetSelectRequestBuffer();
        launchDeviceDiscovery();
    }

    @ServiceThreadOnly
    private List<Integer> initLocalDeviceAddresses() {
        assertRunOnServiceThread();
        List<Integer> addresses = new ArrayList();
        for (HdmiCecLocalDevice device : this.mService.getAllLocalDevices()) {
            addresses.add(Integer.valueOf(device.getDeviceInfo().getLogicalAddress()));
        }
        return Collections.unmodifiableList(addresses);
    }

    @ServiceThreadOnly
    public void setSelectRequestBuffer(SelectRequestBuffer requestBuffer) {
        assertRunOnServiceThread();
        this.mSelectRequestBuffer = requestBuffer;
    }

    @ServiceThreadOnly
    private void resetSelectRequestBuffer() {
        assertRunOnServiceThread();
        setSelectRequestBuffer(SelectRequestBuffer.EMPTY_BUFFER);
    }

    protected int getPreferredAddress() {
        return 0;
    }

    protected void setPreferredAddress(int addr) {
        Slog.w(TAG, "Preferred addres will not be stored for TV");
    }

    @ServiceThreadOnly
    boolean dispatchMessage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mService.isPowerStandby() && (this.mService.isWakeUpMessageReceived() ^ 1) != 0 && this.mStandbyHandler.handleCommand(message)) {
            return true;
        }
        return super.onMessage(message);
    }

    @ServiceThreadOnly
    void deviceSelect(int id, IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        HdmiDeviceInfo targetDevice = (HdmiDeviceInfo) this.mDeviceInfos.get(id);
        if (targetDevice == null) {
            invokeCallback(callback, 3);
            return;
        }
        int targetAddress = targetDevice.getLogicalAddress();
        ActiveSource active = getActiveSource();
        if (targetDevice.getDevicePowerStatus() == 0 && active.isValid() && targetAddress == active.logicalAddress) {
            invokeCallback(callback, 0);
        } else if (targetAddress == 0) {
            handleSelectInternalSource();
            setActiveSource(targetAddress, this.mService.getPhysicalAddress());
            setActivePath(this.mService.getPhysicalAddress());
            invokeCallback(callback, 0);
        } else if (this.mService.isControlEnabled()) {
            removeAction(DeviceSelectAction.class);
            addAndStartAction(new DeviceSelectAction(this, targetDevice, callback));
        } else {
            setActiveSource(targetDevice);
            invokeCallback(callback, 6);
        }
    }

    @ServiceThreadOnly
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

    @ServiceThreadOnly
    void updateActiveSource(int logicalAddress, int physicalAddress) {
        assertRunOnServiceThread();
        updateActiveSource(ActiveSource.of(logicalAddress, physicalAddress));
    }

    @ServiceThreadOnly
    void updateActiveSource(ActiveSource newActive) {
        assertRunOnServiceThread();
        if (!this.mActiveSource.equals(newActive)) {
            setActiveSource(newActive);
            int logicalAddress = newActive.logicalAddress;
            if (!(getCecDeviceInfo(logicalAddress) == null || logicalAddress == this.mAddress || this.mService.pathToPortId(newActive.physicalAddress) != getActivePortId())) {
                setPrevPortId(getActivePortId());
            }
        }
    }

    int getPortId(int physicalAddress) {
        return this.mService.pathToPortId(physicalAddress);
    }

    int getPrevPortId() {
        int i;
        synchronized (this.mLock) {
            i = this.mPrevPortId;
        }
        return i;
    }

    void setPrevPortId(int portId) {
        synchronized (this.mLock) {
            this.mPrevPortId = portId;
        }
    }

    @ServiceThreadOnly
    void updateActiveInput(int path, boolean notifyInputChange) {
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

    @ServiceThreadOnly
    void doManualPortSwitching(int portId, IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        if (!this.mService.isValidPortId(portId)) {
            invokeCallback(callback, 6);
        } else if (portId == getActivePortId()) {
            invokeCallback(callback, 0);
        } else {
            this.mActiveSource.invalidate();
            if (this.mService.isControlEnabled()) {
                int oldPath = getActivePortId() != -1 ? this.mService.portIdToPath(getActivePortId()) : getDeviceInfo().getPhysicalAddress();
                setActivePath(oldPath);
                if (this.mSkipRoutingControl) {
                    this.mSkipRoutingControl = false;
                    return;
                } else {
                    startRoutingControl(oldPath, this.mService.portIdToPath(portId), true, callback);
                    return;
                }
            }
            setActivePortId(portId);
            invokeCallback(callback, 6);
        }
    }

    @ServiceThreadOnly
    void startRoutingControl(int oldPath, int newPath, boolean queryDevicePowerStatus, IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        if (oldPath != newPath) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildRoutingChange(this.mAddress, oldPath, newPath));
            removeAction(RoutingControlAction.class);
            addAndStartAction(new RoutingControlAction(this, newPath, queryDevicePowerStatus, callback));
        }
    }

    @ServiceThreadOnly
    int getPowerStatus() {
        assertRunOnServiceThread();
        return this.mService.getPowerStatus();
    }

    protected int findKeyReceiverAddress() {
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

    @ServiceThreadOnly
    protected boolean handleActiveSource(HdmiCecMessage message) {
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
            ActiveSourceHandler.create(this, null).process(ActiveSource.of(logicalAddress, physicalAddress), info.getDeviceType());
        } else {
            HdmiLogger.debug("Input not ready for device: %X; buffering the command", Integer.valueOf(info.getId()));
            this.mDelayedMessageBuffer.add(message);
        }
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleInactiveSource(HdmiCecMessage message) {
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

    @ServiceThreadOnly
    protected boolean handleRequestActiveSource(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mAddress == getActiveSource().logicalAddress) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(this.mAddress, getActivePath()));
        }
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleGetMenuLanguage(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!broadcastMenuLanguage(this.mService.getLanguage())) {
            Slog.w(TAG, "Failed to respond to <Get Menu Language>: " + message.toString());
        }
        return true;
    }

    @ServiceThreadOnly
    boolean broadcastMenuLanguage(String language) {
        assertRunOnServiceThread();
        HdmiCecMessage command = HdmiCecMessageBuilder.buildSetMenuLanguageCommand(this.mAddress, language);
        if (command == null) {
            return false;
        }
        this.mService.sendCecCommand(command);
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleReportPhysicalAddress(HdmiCecMessage message) {
        assertRunOnServiceThread();
        int path = HdmiUtils.twoBytesToInt(message.getParams());
        int address = message.getSource();
        int type = message.getParams()[2];
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
        addCecDevice(new HdmiDeviceInfo(address, path, getPortId(path), type, UsbAudioDevice.kAudioDeviceClassMask, HdmiUtils.getDefaultDeviceName(address)));
        startNewDeviceAction(ActiveSource.of(address, path), type);
        return true;
    }

    protected boolean handleReportPowerStatus(HdmiCecMessage command) {
        updateDevicePowerStatus(command.getSource(), command.getParams()[0] & 255);
        return true;
    }

    protected boolean handleTimerStatus(HdmiCecMessage message) {
        return true;
    }

    protected boolean handleRecordStatus(HdmiCecMessage message) {
        return true;
    }

    boolean updateCecSwitchInfo(int address, int type, int path) {
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

    void startNewDeviceAction(ActiveSource activeSource, int deviceType) {
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

    @ServiceThreadOnly
    protected boolean handleRoutingChange(HdmiCecMessage message) {
        assertRunOnServiceThread();
        byte[] params = message.getParams();
        if (HdmiUtils.isAffectingActiveRoutingPath(getActivePath(), HdmiUtils.twoBytesToInt(params))) {
            this.mActiveSource.invalidate();
            removeAction(RoutingControlAction.class);
            addAndStartAction(new RoutingControlAction(this, HdmiUtils.twoBytesToInt(params, 2), true, null));
        }
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleReportAudioStatus(HdmiCecMessage message) {
        boolean z = false;
        assertRunOnServiceThread();
        byte[] params = message.getParams();
        int volume = params[0] & 127;
        if ((params[0] & 128) == 128) {
            z = true;
        }
        setAudioStatus(z, volume);
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleTextViewOn(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mService.isPowerStandbyOrTransient() && this.mAutoWakeup) {
            this.mService.wakeUp();
        }
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleImageViewOn(HdmiCecMessage message) {
        assertRunOnServiceThread();
        return handleTextViewOn(message);
    }

    @ServiceThreadOnly
    protected boolean handleSetOsdName(HdmiCecMessage message) {
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
            addCecDevice(new HdmiDeviceInfo(deviceInfo.getLogicalAddress(), deviceInfo.getPhysicalAddress(), deviceInfo.getPortId(), deviceInfo.getDeviceType(), deviceInfo.getVendorId(), osdName));
            return true;
        } catch (UnsupportedEncodingException e) {
            Slog.e(TAG, "Invalid <Set Osd Name> request:" + message, e);
            return true;
        }
    }

    @ServiceThreadOnly
    private void launchDeviceDiscovery() {
        assertRunOnServiceThread();
        clearDeviceInfoList();
        addAndStartAction(new DeviceDiscoveryAction(this, new DeviceDiscoveryCallback() {
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

    @ServiceThreadOnly
    void onNewAvrAdded(HdmiDeviceInfo avr) {
        assertRunOnServiceThread();
        addAndStartAction(new SystemAudioAutoInitiationAction(this, avr.getLogicalAddress()));
        if (isConnected(avr.getPortId()) && isArcFeatureEnabled(avr.getPortId()) && (hasAction(SetArcTransmissionStateAction.class) ^ 1) != 0) {
            startArcAction(true);
        }
    }

    @ServiceThreadOnly
    private void clearDeviceInfoList() {
        assertRunOnServiceThread();
        for (HdmiDeviceInfo info : this.mSafeExternalInputs) {
            invokeDeviceEventListener(info, 2);
        }
        this.mDeviceInfos.clear();
        updateSafeDeviceInfoList();
    }

    @ServiceThreadOnly
    void changeSystemAudioMode(boolean enabled, IHdmiControlCallback callback) {
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

    void setSystemAudioMode(boolean on) {
        if (isSystemAudioControlFeatureEnabled() || !on) {
            HdmiLogger.debug("System Audio Mode change[old:%b new:%b]", Boolean.valueOf(this.mSystemAudioActivated), Boolean.valueOf(on));
            updateAudioManagerForSystemAudio(on);
            synchronized (this.mLock) {
                if (this.mSystemAudioActivated != on) {
                    this.mSystemAudioActivated = on;
                    this.mService.announceSystemAudioModeChange(on);
                }
            }
            return;
        }
        HdmiLogger.debug("Cannot turn on system audio mode because the System Audio Control feature is disabled.", new Object[0]);
    }

    private void updateAudioManagerForSystemAudio(boolean on) {
        int device = this.mService.getAudioManager().setHdmiSystemAudioSupported(on);
        HdmiLogger.debug("[A]UpdateSystemAudio mode[on=%b] output=[%X]", Boolean.valueOf(on), Integer.valueOf(device));
    }

    boolean isSystemAudioActivated() {
        if (!hasSystemAudioDevice()) {
            return false;
        }
        boolean z;
        synchronized (this.mLock) {
            z = this.mSystemAudioActivated;
        }
        return z;
    }

    @ServiceThreadOnly
    void setSystemAudioControlFeatureEnabled(boolean enabled) {
        assertRunOnServiceThread();
        synchronized (this.mLock) {
            this.mSystemAudioControlFeatureEnabled = enabled;
        }
        if (hasSystemAudioDevice()) {
            changeSystemAudioMode(enabled, null);
        }
    }

    boolean isSystemAudioControlFeatureEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSystemAudioControlFeatureEnabled;
        }
        return z;
    }

    @ServiceThreadOnly
    boolean setArcStatus(boolean enabled) {
        assertRunOnServiceThread();
        HdmiLogger.debug("Set Arc Status[old:%b new:%b]", Boolean.valueOf(this.mArcEstablished), Boolean.valueOf(enabled));
        boolean oldStatus = this.mArcEstablished;
        enableAudioReturnChannel(enabled);
        notifyArcStatusToAudioService(enabled);
        this.mArcEstablished = enabled;
        return oldStatus;
    }

    @ServiceThreadOnly
    void enableAudioReturnChannel(boolean enabled) {
        assertRunOnServiceThread();
        HdmiDeviceInfo avr = getAvrDeviceInfo();
        if (avr != null) {
            this.mService.enableAudioReturnChannel(avr.getPortId(), enabled);
        }
    }

    @ServiceThreadOnly
    boolean isConnected(int portId) {
        assertRunOnServiceThread();
        return this.mService.isConnected(portId);
    }

    private void notifyArcStatusToAudioService(boolean enabled) {
        this.mService.getAudioManager().setWiredDeviceConnectionState(DumpState.DUMP_DOMAIN_PREFERRED, enabled ? 1 : 0, "", "");
    }

    @ServiceThreadOnly
    boolean isArcEstablished() {
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

    @ServiceThreadOnly
    void changeArcFeatureEnabled(int portId, boolean enabled) {
        assertRunOnServiceThread();
        if (this.mArcFeatureEnabled.get(portId) != enabled) {
            this.mArcFeatureEnabled.put(portId, enabled);
            HdmiDeviceInfo avr = getAvrDeviceInfo();
            if (avr != null && avr.getPortId() == portId) {
                if (enabled && (this.mArcEstablished ^ 1) != 0) {
                    startArcAction(true);
                } else if (!enabled && this.mArcEstablished) {
                    startArcAction(false);
                }
            }
        }
    }

    @ServiceThreadOnly
    boolean isArcFeatureEnabled(int portId) {
        assertRunOnServiceThread();
        return this.mArcFeatureEnabled.get(portId);
    }

    @ServiceThreadOnly
    void startArcAction(boolean enabled) {
        assertRunOnServiceThread();
        HdmiDeviceInfo info = getAvrDeviceInfo();
        if (info == null) {
            Slog.w(TAG, "Failed to start arc action; No AVR device.");
        } else if (canStartArcUpdateAction(info.getLogicalAddress(), enabled)) {
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
        } else {
            Slog.w(TAG, "Failed to start arc action; ARC configuration check failed.");
            if (enabled && (isConnectedToArcPort(info.getPhysicalAddress()) ^ 1) != 0) {
                displayOsd(1);
            }
        }
    }

    private boolean isDirectConnectAddress(int physicalAddress) {
        return (61440 & physicalAddress) == physicalAddress;
    }

    void setAudioStatus(boolean mute, int volume) {
        synchronized (this.mLock) {
            this.mSystemAudioMute = mute;
            this.mSystemAudioVolume = volume;
            this.mService.setAudioStatus(mute, VolumeControlAction.scaleToCustomVolume(volume, this.mService.getAudioManager().getStreamMaxVolume(3)));
            if (mute) {
                volume = 101;
            }
            displayOsd(2, volume);
        }
    }

    /* JADX WARNING: Missing block: B:14:0x002c, code:
            r0 = getActions(com.android.server.hdmi.VolumeControlAction.class);
     */
    /* JADX WARNING: Missing block: B:15:0x0036, code:
            if (r0.isEmpty() == false) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:16:0x0038, code:
            r7 = getAvrDeviceInfo().getLogicalAddress();
     */
    /* JADX WARNING: Missing block: B:17:0x0042, code:
            if (r10 <= 0) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:18:0x0044, code:
            r3 = true;
     */
    /* JADX WARNING: Missing block: B:19:0x0045, code:
            addAndStartAction(new com.android.server.hdmi.VolumeControlAction(r8, r7, r3));
     */
    /* JADX WARNING: Missing block: B:20:0x004b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x004f, code:
            r3 = false;
     */
    /* JADX WARNING: Missing block: B:25:0x0051, code:
            r3 = (com.android.server.hdmi.VolumeControlAction) r0.get(0);
     */
    /* JADX WARNING: Missing block: B:26:0x0057, code:
            if (r10 <= 0) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:27:0x0059, code:
            r3.handleVolumeChange(r4);
     */
    /* JADX WARNING: Missing block: B:28:0x005d, code:
            r4 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @ServiceThreadOnly
    void changeVolume(int curVolume, int delta, int maxVolume) {
        boolean z = true;
        assertRunOnServiceThread();
        if (delta != 0 && (isSystemAudioActivated() ^ 1) == 0) {
            int cecVolume = VolumeControlAction.scaleToCecVolume(curVolume + delta, maxVolume);
            synchronized (this.mLock) {
                if (cecVolume == this.mSystemAudioVolume) {
                    this.mService.setAudioStatus(false, VolumeControlAction.scaleToCustomVolume(this.mSystemAudioVolume, maxVolume));
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x002a, code:
            if (isSystemAudioActivated() != false) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:11:0x002c, code:
            com.android.server.hdmi.HdmiLogger.debug("[A]:System audio is not activated.", new java.lang.Object[0]);
     */
    /* JADX WARNING: Missing block: B:12:0x0034, code:
            return;
     */
    /* JADX WARNING: Missing block: B:16:0x0038, code:
            removeAction(com.android.server.hdmi.VolumeControlAction.class);
            sendUserControlPressedAndReleased(getAvrDeviceInfo().getLogicalAddress(), com.android.server.hdmi.HdmiCecKeycode.getMuteKey(r5));
     */
    /* JADX WARNING: Missing block: B:17:0x004c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @ServiceThreadOnly
    void changeMute(boolean mute) {
        assertRunOnServiceThread();
        HdmiLogger.debug("[A]:Change mute:%b", Boolean.valueOf(mute));
        synchronized (this.mLock) {
            if (this.mSystemAudioMute == mute) {
                HdmiLogger.debug("No need to change mute.", new Object[0]);
            }
        }
    }

    @ServiceThreadOnly
    protected boolean handleInitiateArc(HdmiCecMessage message) {
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
        boolean z = false;
        HdmiDeviceInfo avr = getAvrDeviceInfo();
        if (avr == null || avrAddress != avr.getLogicalAddress() || !isConnectedToArcPort(avr.getPhysicalAddress()) || !isDirectConnectAddress(avr.getPhysicalAddress())) {
            return false;
        }
        if (!enabled) {
            return true;
        }
        if (isConnected(avr.getPortId())) {
            z = isArcFeatureEnabled(avr.getPortId());
        }
        return z;
    }

    @ServiceThreadOnly
    protected boolean handleTerminateArc(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mService.isPowerStandbyOrTransient()) {
            setArcStatus(false);
            return true;
        }
        removeAction(RequestArcTerminationAction.class);
        addAndStartAction(new SetArcTransmissionStateAction(this, message.getSource(), false));
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleSetSystemAudioMode(HdmiCecMessage message) {
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
        } else if (!systemAudioStatus || (isSystemAudioControlFeatureEnabled() ^ 1) == 0) {
            removeAction(SystemAudioAutoInitiationAction.class);
            addAndStartAction(new SystemAudioActionFromAvr(this, message.getSource(), systemAudioStatus, null));
            return true;
        } else {
            HdmiLogger.debug("Ignoring <Set System Audio Mode> message because the System Audio Control feature is disabled: %s", message);
            this.mService.maySendFeatureAbortCommand(message, 4);
            return true;
        }
    }

    @ServiceThreadOnly
    protected boolean handleSystemAudioModeStatus(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (isMessageForSystemAudio(message)) {
            setSystemAudioMode(HdmiUtils.parseCommandParamSystemAudioStatus(message));
            return true;
        }
        HdmiLogger.warning("Invalid <System Audio Mode Status> message:" + message, new Object[0]);
        return true;
    }

    @ServiceThreadOnly
    protected boolean handleRecordTvScreen(HdmiCecMessage message) {
        List<OneTouchRecordAction> actions = getActions(OneTouchRecordAction.class);
        if (actions.isEmpty()) {
            int recorderAddress = message.getSource();
            int reason = startOneTouchRecord(recorderAddress, this.mService.invokeRecordRequestListener(recorderAddress));
            if (reason != -1) {
                this.mService.maySendFeatureAbortCommand(message, reason);
            }
            return true;
        }
        if (((OneTouchRecordAction) actions.get(0)).getRecorderAddress() != message.getSource()) {
            announceOneTouchRecordResult(message.getSource(), 48);
        }
        return super.handleRecordTvScreen(message);
    }

    protected boolean handleTimerClearedStatus(HdmiCecMessage message) {
        announceTimerRecordingResult(message.getSource(), message.getParams()[0] & 255);
        return true;
    }

    void announceOneTouchRecordResult(int recorderAddress, int result) {
        this.mService.invokeOneTouchRecordResult(recorderAddress, result);
    }

    void announceTimerRecordingResult(int recorderAddress, int result) {
        this.mService.invokeTimerRecordingResult(recorderAddress, result);
    }

    void announceClearTimerRecordingResult(int recorderAddress, int result) {
        this.mService.invokeClearTimerRecordingResult(recorderAddress, result);
    }

    private boolean isMessageForSystemAudio(HdmiCecMessage message) {
        if (!this.mService.isControlEnabled() || message.getSource() != 5) {
            return false;
        }
        if ((message.getDestination() == 0 || message.getDestination() == 15) && getAvrDeviceInfo() != null) {
            return true;
        }
        return false;
    }

    @ServiceThreadOnly
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

    @ServiceThreadOnly
    private HdmiDeviceInfo removeDeviceInfo(int id) {
        assertRunOnServiceThread();
        HdmiDeviceInfo deviceInfo = (HdmiDeviceInfo) this.mDeviceInfos.get(id);
        if (deviceInfo != null) {
            this.mDeviceInfos.remove(id);
        }
        updateSafeDeviceInfoList();
        return deviceInfo;
    }

    @ServiceThreadOnly
    List<HdmiDeviceInfo> getDeviceInfoList(boolean includeLocalDevice) {
        assertRunOnServiceThread();
        if (includeLocalDevice) {
            return HdmiUtils.sparseArrayToList(this.mDeviceInfos);
        }
        ArrayList<HdmiDeviceInfo> infoList = new ArrayList();
        for (int i = 0; i < this.mDeviceInfos.size(); i++) {
            HdmiDeviceInfo info = (HdmiDeviceInfo) this.mDeviceInfos.valueAt(i);
            if (!isLocalDeviceAddress(info.getLogicalAddress())) {
                infoList.add(info);
            }
        }
        return infoList;
    }

    List<HdmiDeviceInfo> getSafeExternalInputsLocked() {
        return this.mSafeExternalInputs;
    }

    @ServiceThreadOnly
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
        ArrayList<HdmiDeviceInfo> infoList = new ArrayList();
        for (int i = 0; i < this.mDeviceInfos.size(); i++) {
            HdmiDeviceInfo info = (HdmiDeviceInfo) this.mDeviceInfos.valueAt(i);
            if (!(isLocalDeviceAddress(info.getLogicalAddress()) || !info.isSourceType() || (hideDevicesBehindLegacySwitch(info) ^ 1) == 0)) {
                infoList.add(info);
            }
        }
        return infoList;
    }

    private boolean hideDevicesBehindLegacySwitch(HdmiDeviceInfo info) {
        return isConnectedToCecSwitch(info.getPhysicalAddress(), this.mCecSwitches) ^ 1;
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
        int i = 0;
        while (i <= 12) {
            if (((childPath >> i) & 15) != 0) {
                if (((parentPath >> i) & 15) == 0 && (childPath >> (i + 4)) == (parentPath >> (i + 4))) {
                    z = true;
                }
                return z;
            }
            i += 4;
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

    @ServiceThreadOnly
    HdmiDeviceInfo getAvrDeviceInfo() {
        assertRunOnServiceThread();
        return getCecDeviceInfo(5);
    }

    @ServiceThreadOnly
    HdmiDeviceInfo getCecDeviceInfo(int logicalAddress) {
        assertRunOnServiceThread();
        return (HdmiDeviceInfo) this.mDeviceInfos.get(HdmiDeviceInfo.idForCecDevice(logicalAddress));
    }

    boolean hasSystemAudioDevice() {
        return getSafeAvrDeviceInfo() != null;
    }

    HdmiDeviceInfo getSafeAvrDeviceInfo() {
        return getSafeCecDeviceInfo(5);
    }

    HdmiDeviceInfo getSafeCecDeviceInfo(int logicalAddress) {
        synchronized (this.mLock) {
            for (HdmiDeviceInfo info : this.mSafeAllDeviceInfos) {
                if (info.isCecDevice() && info.getLogicalAddress() == logicalAddress) {
                    return info;
                }
            }
            return null;
        }
    }

    List<HdmiDeviceInfo> getSafeCecDevicesLocked() {
        ArrayList<HdmiDeviceInfo> infoList = new ArrayList();
        for (HdmiDeviceInfo info : this.mSafeAllDeviceInfos) {
            if (!isLocalDeviceAddress(info.getLogicalAddress())) {
                infoList.add(info);
            }
        }
        return infoList;
    }

    @ServiceThreadOnly
    final void addCecDevice(HdmiDeviceInfo info) {
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

    @ServiceThreadOnly
    final void removeCecDevice(int address) {
        assertRunOnServiceThread();
        HdmiDeviceInfo info = removeDeviceInfo(HdmiDeviceInfo.idForCecDevice(address));
        this.mCecMessageCache.flushMessagesFrom(address);
        invokeDeviceEventListener(info, 2);
    }

    @ServiceThreadOnly
    void handleRemoveActiveRoutingPath(int path) {
        assertRunOnServiceThread();
        if (isTailOfActivePath(path, getActivePath())) {
            startRoutingControl(getActivePath(), this.mService.portIdToPath(getActivePortId()), true, null);
        }
    }

    @ServiceThreadOnly
    void launchRoutingControl(boolean routingForBootup) {
        assertRunOnServiceThread();
        if (getActivePortId() == -1) {
            int activePath = this.mService.getPhysicalAddress();
            setActivePath(activePath);
            if (!routingForBootup && (this.mDelayedMessageBuffer.isBuffered(130) ^ 1) != 0) {
                this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(this.mAddress, activePath));
            }
        } else if (!routingForBootup && (isProhibitMode() ^ 1) != 0) {
            int newPath = this.mService.portIdToPath(getActivePortId());
            setActivePath(newPath);
            startRoutingControl(getActivePath(), newPath, routingForBootup, null);
        }
    }

    @ServiceThreadOnly
    final HdmiDeviceInfo getDeviceInfoByPath(int path) {
        assertRunOnServiceThread();
        for (HdmiDeviceInfo info : getDeviceInfoList(false)) {
            if (info.getPhysicalAddress() == path) {
                return info;
            }
        }
        return null;
    }

    HdmiDeviceInfo getSafeDeviceInfoByPath(int path) {
        synchronized (this.mLock) {
            for (HdmiDeviceInfo info : this.mSafeAllDeviceInfos) {
                if (info.getPhysicalAddress() == path) {
                    return info;
                }
            }
            return null;
        }
    }

    @ServiceThreadOnly
    boolean isInDeviceList(int logicalAddress, int physicalAddress) {
        boolean z = false;
        assertRunOnServiceThread();
        HdmiDeviceInfo device = getCecDeviceInfo(logicalAddress);
        if (device == null) {
            return false;
        }
        if (device.getPhysicalAddress() == physicalAddress) {
            z = true;
        }
        return z;
    }

    @ServiceThreadOnly
    void onHotplug(int portId, boolean connected) {
        assertRunOnServiceThread();
        if (!connected) {
            removeCecSwitches(portId);
        }
        List<HotplugDetectionAction> hotplugActions = getActions(HotplugDetectionAction.class);
        if (!hotplugActions.isEmpty()) {
            ((HotplugDetectionAction) hotplugActions.get(0)).pollAllDevicesNow();
        }
    }

    private void removeCecSwitches(int portId) {
        Iterator<Integer> it = this.mCecSwitches.iterator();
        while (!it.hasNext()) {
            if (pathToPortId(((Integer) it.next()).intValue()) == portId) {
                it.remove();
            }
        }
    }

    @ServiceThreadOnly
    void setAutoDeviceOff(boolean enabled) {
        assertRunOnServiceThread();
        this.mAutoDeviceOff = enabled;
    }

    @ServiceThreadOnly
    void setAutoWakeup(boolean enabled) {
        assertRunOnServiceThread();
        this.mAutoWakeup = enabled;
    }

    @ServiceThreadOnly
    boolean getAutoWakeup() {
        assertRunOnServiceThread();
        return this.mAutoWakeup;
    }

    @ServiceThreadOnly
    protected void disableDevice(boolean initiatedByCec, PendingActionClearedCallback callback) {
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

    @ServiceThreadOnly
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

    @ServiceThreadOnly
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

    @ServiceThreadOnly
    protected void onStandby(boolean initiatedByCec, int standbyAction) {
        assertRunOnServiceThread();
        if (this.mService.isControlEnabled() && !initiatedByCec && this.mAutoDeviceOff) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, 15));
        }
    }

    boolean isProhibitMode() {
        return this.mService.isProhibitMode();
    }

    boolean isPowerStandbyOrTransient() {
        return this.mService.isPowerStandbyOrTransient();
    }

    @ServiceThreadOnly
    void displayOsd(int messageId) {
        assertRunOnServiceThread();
        this.mService.displayOsd(messageId);
    }

    @ServiceThreadOnly
    void displayOsd(int messageId, int extra) {
        assertRunOnServiceThread();
        this.mService.displayOsd(messageId, extra);
    }

    @ServiceThreadOnly
    int startOneTouchRecord(int recorderAddress, byte[] recordSource) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not start one touch record. CEC control is disabled.");
            announceOneTouchRecordResult(recorderAddress, 51);
            return 1;
        } else if (!checkRecorder(recorderAddress)) {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceOneTouchRecordResult(recorderAddress, 49);
            return 1;
        } else if (checkRecordSource(recordSource)) {
            addAndStartAction(new OneTouchRecordAction(this, recorderAddress, recordSource));
            Slog.i(TAG, "Start new [One Touch Record]-Target:" + recorderAddress + ", recordSource:" + Arrays.toString(recordSource));
            return -1;
        } else {
            Slog.w(TAG, "Invalid record source." + Arrays.toString(recordSource));
            announceOneTouchRecordResult(recorderAddress, 50);
            return 2;
        }
    }

    @ServiceThreadOnly
    void stopOneTouchRecord(int recorderAddress) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not stop one touch record. CEC control is disabled.");
            announceOneTouchRecordResult(recorderAddress, 51);
        } else if (checkRecorder(recorderAddress)) {
            removeAction(OneTouchRecordAction.class);
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildRecordOff(this.mAddress, recorderAddress));
            Slog.i(TAG, "Stop [One Touch Record]-Target:" + recorderAddress);
        } else {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceOneTouchRecordResult(recorderAddress, 49);
        }
    }

    private boolean checkRecorder(int recorderAddress) {
        if (getCecDeviceInfo(recorderAddress) == null) {
            return false;
        }
        if (HdmiUtils.getTypeFromAddress(recorderAddress) == 1) {
            return true;
        }
        return false;
    }

    private boolean checkRecordSource(byte[] recordSource) {
        return recordSource != null ? HdmiRecordSources.checkRecordSource(recordSource) : false;
    }

    @ServiceThreadOnly
    void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not start one touch record. CEC control is disabled.");
            announceTimerRecordingResult(recorderAddress, 3);
        } else if (!checkRecorder(recorderAddress)) {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceTimerRecordingResult(recorderAddress, 1);
        } else if (checkTimerRecordingSource(sourceType, recordSource)) {
            addAndStartAction(new TimerRecordingAction(this, recorderAddress, sourceType, recordSource));
            Slog.i(TAG, "Start [Timer Recording]-Target:" + recorderAddress + ", SourceType:" + sourceType + ", RecordSource:" + Arrays.toString(recordSource));
        } else {
            Slog.w(TAG, "Invalid record source." + Arrays.toString(recordSource));
            announceTimerRecordingResult(recorderAddress, 2);
        }
    }

    private boolean checkTimerRecordingSource(int sourceType, byte[] recordSource) {
        if (recordSource != null) {
            return HdmiTimerRecordSources.checkTimerRecordSource(sourceType, recordSource);
        }
        return false;
    }

    @ServiceThreadOnly
    void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
        assertRunOnServiceThread();
        if (!this.mService.isControlEnabled()) {
            Slog.w(TAG, "Can not start one touch record. CEC control is disabled.");
            announceClearTimerRecordingResult(recorderAddress, 162);
        } else if (!checkRecorder(recorderAddress)) {
            Slog.w(TAG, "Invalid recorder address:" + recorderAddress);
            announceClearTimerRecordingResult(recorderAddress, 160);
        } else if (checkTimerRecordingSource(sourceType, recordSource)) {
            sendClearTimerMessage(recorderAddress, sourceType, recordSource);
        } else {
            Slog.w(TAG, "Invalid record source." + Arrays.toString(recordSource));
            announceClearTimerRecordingResult(recorderAddress, 161);
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
        this.mService.sendCecCommand(message, new SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    HdmiCecLocalDeviceTv.this.announceClearTimerRecordingResult(recorderAddress, 161);
                }
            }
        });
    }

    void updateDevicePowerStatus(int logicalAddress, int newPowerStatus) {
        HdmiDeviceInfo info = getCecDeviceInfo(logicalAddress);
        if (info == null) {
            Slog.w(TAG, "Can not update power status of non-existing device:" + logicalAddress);
        } else if (info.getDevicePowerStatus() != newPowerStatus) {
            HdmiDeviceInfo newInfo = HdmiUtils.cloneHdmiDeviceInfo(info, newPowerStatus);
            addDeviceInfo(newInfo);
            invokeDeviceEventListener(newInfo, 3);
        }
    }

    protected boolean handleMenuStatus(HdmiCecMessage message) {
        return true;
    }

    protected void sendStandby(int deviceId) {
        HdmiDeviceInfo targetDevice = (HdmiDeviceInfo) this.mDeviceInfos.get(deviceId);
        if (targetDevice != null) {
            this.mService.sendCecCommand(HdmiCecMessageBuilder.buildStandby(this.mAddress, targetDevice.getLogicalAddress()));
        }
    }

    @ServiceThreadOnly
    void processAllDelayedMessages() {
        assertRunOnServiceThread();
        this.mDelayedMessageBuffer.processAllMessages();
    }

    @ServiceThreadOnly
    void processDelayedMessages(int address) {
        assertRunOnServiceThread();
        this.mDelayedMessageBuffer.processMessagesForDevice(address);
    }

    @ServiceThreadOnly
    void processDelayedActiveSource(int address) {
        assertRunOnServiceThread();
        this.mDelayedMessageBuffer.processActiveSource(address);
    }

    protected void dump(IndentingPrintWriter pw) {
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
