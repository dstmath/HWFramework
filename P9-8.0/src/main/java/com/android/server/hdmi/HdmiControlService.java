package com.android.server.hdmi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiHotplugEvent;
import android.hardware.hdmi.HdmiPortInfo;
import android.hardware.hdmi.IHdmiControlCallback;
import android.hardware.hdmi.IHdmiControlService.Stub;
import android.hardware.hdmi.IHdmiDeviceEventListener;
import android.hardware.hdmi.IHdmiHotplugEventListener;
import android.hardware.hdmi.IHdmiInputChangeListener;
import android.hardware.hdmi.IHdmiMhlVendorCommandListener;
import android.hardware.hdmi.IHdmiRecordListener;
import android.hardware.hdmi.IHdmiSystemAudioModeChangeListener;
import android.hardware.hdmi.IHdmiVendorCommandListener;
import android.media.AudioManager;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputManager.TvInputCallback;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.SystemService;
import com.android.server.hdmi.HdmiAnnotations.ServiceThreadOnly;
import com.android.server.lights.LightsManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import libcore.util.EmptyArray;

public final class HdmiControlService extends SystemService {
    static final int INITIATED_BY_BOOT_UP = 1;
    static final int INITIATED_BY_ENABLE_CEC = 0;
    static final int INITIATED_BY_HOTPLUG = 4;
    static final int INITIATED_BY_SCREEN_ON = 2;
    static final int INITIATED_BY_WAKE_UP_MESSAGE = 3;
    static final String PERMISSION = "android.permission.HDMI_CEC";
    static final int STANDBY_SCREEN_OFF = 0;
    static final int STANDBY_SHUTDOWN = 1;
    private static final String TAG = "HdmiControlService";
    private final Locale HONG_KONG = new Locale("zh", "HK");
    private final Locale MACAU = new Locale("zh", "MO");
    @ServiceThreadOnly
    private int mActivePortId = -1;
    private boolean mAddressAllocated = false;
    private HdmiCecController mCecController;
    private final CecMessageBuffer mCecMessageBuffer = new CecMessageBuffer(this, null);
    @GuardedBy("mLock")
    private final ArrayList<DeviceEventListenerRecord> mDeviceEventListenerRecords = new ArrayList();
    private final Handler mHandler = new Handler();
    private final HdmiControlBroadcastReceiver mHdmiControlBroadcastReceiver = new HdmiControlBroadcastReceiver(this, null);
    @GuardedBy("mLock")
    private boolean mHdmiControlEnabled;
    @GuardedBy("mLock")
    private final ArrayList<HotplugEventListenerRecord> mHotplugEventListenerRecords = new ArrayList();
    @GuardedBy("mLock")
    private InputChangeListenerRecord mInputChangeListenerRecord;
    private final HandlerThread mIoThread = new HandlerThread("Hdmi Control Io Thread");
    @ServiceThreadOnly
    private String mLanguage = Locale.getDefault().getISO3Language();
    @ServiceThreadOnly
    private int mLastInputMhl = -1;
    private final List<Integer> mLocalDevices = getIntList(SystemProperties.get("ro.hdmi.device_type"));
    private final Object mLock = new Object();
    private HdmiCecMessageValidator mMessageValidator;
    private HdmiMhlControllerStub mMhlController;
    @GuardedBy("mLock")
    private List<HdmiDeviceInfo> mMhlDevices;
    @GuardedBy("mLock")
    private boolean mMhlInputChangeEnabled;
    @GuardedBy("mLock")
    private final ArrayList<HdmiMhlVendorCommandListenerRecord> mMhlVendorCommandListenerRecords = new ArrayList();
    private UnmodifiableSparseArray<HdmiDeviceInfo> mPortDeviceMap;
    private UnmodifiableSparseIntArray mPortIdMap;
    private List<HdmiPortInfo> mPortInfo;
    private UnmodifiableSparseArray<HdmiPortInfo> mPortInfoMap;
    private PowerManager mPowerManager;
    @ServiceThreadOnly
    private int mPowerStatus = 1;
    @GuardedBy("mLock")
    private boolean mProhibitMode;
    @GuardedBy("mLock")
    private HdmiRecordListenerRecord mRecordListenerRecord;
    private final SelectRequestBuffer mSelectRequestBuffer = new SelectRequestBuffer();
    private final SettingsObserver mSettingsObserver = new SettingsObserver(this.mHandler);
    @ServiceThreadOnly
    private boolean mStandbyMessageReceived = false;
    private final ArrayList<SystemAudioModeChangeListenerRecord> mSystemAudioModeChangeListenerRecords = new ArrayList();
    private TvInputManager mTvInputManager;
    @GuardedBy("mLock")
    private final ArrayList<VendorCommandListenerRecord> mVendorCommandListenerRecords = new ArrayList();
    @ServiceThreadOnly
    private boolean mWakeUpMessageReceived = false;

    interface DevicePollingCallback {
        void onPollingFinished(List<Integer> list);
    }

    interface SendMessageCallback {
        void onSendCompleted(int i);
    }

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(HdmiControlService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        public int[] getSupportedTypes() {
            HdmiControlService.this.enforceAccessPermission();
            int[] localDevices = new int[HdmiControlService.this.mLocalDevices.size()];
            for (int i = 0; i < localDevices.length; i++) {
                localDevices[i] = ((Integer) HdmiControlService.this.mLocalDevices.get(i)).intValue();
            }
            return localDevices;
        }

        public HdmiDeviceInfo getActiveSource() {
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            if (tv == null) {
                Slog.w(HdmiControlService.TAG, "Local tv device not available");
                return null;
            }
            ActiveSource activeSource = tv.getActiveSource();
            if (activeSource.isValid()) {
                return new HdmiDeviceInfo(activeSource.logicalAddress, activeSource.physicalAddress, -1, -1, 0, "");
            }
            int activePath = tv.getActivePath();
            if (activePath == NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
                return null;
            }
            HdmiDeviceInfo info = tv.getSafeDeviceInfoByPath(activePath);
            if (info == null) {
                info = new HdmiDeviceInfo(activePath, tv.getActivePortId());
            }
            return info;
        }

        public void deviceSelect(final int deviceId, final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (callback == null) {
                        Slog.e(HdmiControlService.TAG, "Callback cannot be null");
                        return;
                    }
                    HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                    if (tv != null) {
                        HdmiMhlLocalDeviceStub device = HdmiControlService.this.mMhlController.getLocalDeviceById(deviceId);
                        if (device == null) {
                            tv.deviceSelect(deviceId, callback);
                        } else if (device.getPortId() == tv.getActivePortId()) {
                            HdmiControlService.this.invokeCallback(callback, 0);
                        } else {
                            device.turnOn(callback);
                            tv.doManualPortSwitching(device.getPortId(), null);
                        }
                    } else if (HdmiControlService.this.mAddressAllocated) {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                        HdmiControlService.this.invokeCallback(callback, 2);
                    } else {
                        HdmiControlService.this.mSelectRequestBuffer.set(SelectRequestBuffer.newDeviceSelect(HdmiControlService.this, deviceId, callback));
                    }
                }
            });
        }

        public void portSelect(final int portId, final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (callback == null) {
                        Slog.e(HdmiControlService.TAG, "Callback cannot be null");
                        return;
                    }
                    HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                    if (tv != null) {
                        tv.doManualPortSwitching(portId, callback);
                    } else if (HdmiControlService.this.mAddressAllocated) {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                        HdmiControlService.this.invokeCallback(callback, 2);
                    } else {
                        HdmiControlService.this.mSelectRequestBuffer.set(SelectRequestBuffer.newPortSelect(HdmiControlService.this, portId, callback));
                    }
                }
            });
        }

        public void sendKeyEvent(final int deviceType, final int keyCode, final boolean isPressed) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiMhlLocalDeviceStub device = HdmiControlService.this.mMhlController.getLocalDevice(HdmiControlService.this.mActivePortId);
                    if (device != null) {
                        device.sendKeyEvent(keyCode, isPressed);
                        return;
                    }
                    if (HdmiControlService.this.mCecController != null) {
                        HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(deviceType);
                        if (localDevice == null) {
                            Slog.w(HdmiControlService.TAG, "Local device not available");
                            return;
                        }
                        localDevice.sendKeyEvent(keyCode, isPressed);
                    }
                }
            });
        }

        public void oneTouchPlay(final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiControlService.this.oneTouchPlay(callback);
                }
            });
        }

        public void queryDisplayStatus(final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiControlService.this.queryDisplayStatus(callback);
                }
            });
        }

        public void addHotplugEventListener(IHdmiHotplugEventListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.addHotplugEventListener(listener);
        }

        public void removeHotplugEventListener(IHdmiHotplugEventListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.removeHotplugEventListener(listener);
        }

        public void addDeviceEventListener(IHdmiDeviceEventListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.addDeviceEventListener(listener);
        }

        public List<HdmiPortInfo> getPortInfo() {
            HdmiControlService.this.enforceAccessPermission();
            return HdmiControlService.this.getPortInfo();
        }

        public boolean canChangeSystemAudioMode() {
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            if (tv == null) {
                return false;
            }
            return tv.hasSystemAudioDevice();
        }

        public boolean getSystemAudioMode() {
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            if (tv == null) {
                return false;
            }
            return tv.isSystemAudioActivated();
        }

        public void setSystemAudioMode(final boolean enabled, final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                    if (tv == null) {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                        HdmiControlService.this.invokeCallback(callback, 2);
                        return;
                    }
                    tv.changeSystemAudioMode(enabled, callback);
                }
            });
        }

        public void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.addSystemAudioModeChangeListner(listener);
        }

        public void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.removeSystemAudioModeChangeListener(listener);
        }

        public void setInputChangeListener(IHdmiInputChangeListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.setInputChangeListener(listener);
        }

        public List<HdmiDeviceInfo> getInputDevices() {
            List<HdmiDeviceInfo> mergeToUnmodifiableList;
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            synchronized (HdmiControlService.this.mLock) {
                List<HdmiDeviceInfo> cecDevices;
                if (tv == null) {
                    cecDevices = Collections.emptyList();
                } else {
                    cecDevices = tv.getSafeExternalInputsLocked();
                }
                mergeToUnmodifiableList = HdmiUtils.mergeToUnmodifiableList(cecDevices, HdmiControlService.this.getMhlDevicesLocked());
            }
            return mergeToUnmodifiableList;
        }

        public List<HdmiDeviceInfo> getDeviceList() {
            List<HdmiDeviceInfo> emptyList;
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            synchronized (HdmiControlService.this.mLock) {
                if (tv == null) {
                    emptyList = Collections.emptyList();
                } else {
                    emptyList = tv.getSafeCecDevicesLocked();
                }
            }
            return emptyList;
        }

        public void setSystemAudioVolume(final int oldIndex, final int newIndex, final int maxIndex) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                    if (tv == null) {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                    } else {
                        tv.changeVolume(oldIndex, newIndex - oldIndex, maxIndex);
                    }
                }
            });
        }

        public void setSystemAudioMute(final boolean mute) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                    if (tv == null) {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                    } else {
                        tv.changeMute(mute);
                    }
                }
            });
        }

        public void setArcMode(boolean enabled) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (HdmiControlService.this.tv() == null) {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available to change arc mode.");
                    }
                }
            });
        }

        public void setProhibitMode(boolean enabled) {
            HdmiControlService.this.enforceAccessPermission();
            if (HdmiControlService.this.isTvDevice()) {
                HdmiControlService.this.setProhibitMode(enabled);
            }
        }

        public void addVendorCommandListener(IHdmiVendorCommandListener listener, int deviceType) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.addVendorCommandListener(listener, deviceType);
        }

        public void sendVendorCommand(int deviceType, int targetAddress, byte[] params, boolean hasVendorId) {
            HdmiControlService.this.enforceAccessPermission();
            final int i = deviceType;
            final boolean z = hasVendorId;
            final int i2 = targetAddress;
            final byte[] bArr = params;
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiCecLocalDevice device = HdmiControlService.this.mCecController.getLocalDevice(i);
                    if (device == null) {
                        Slog.w(HdmiControlService.TAG, "Local device not available");
                        return;
                    }
                    if (z) {
                        HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildVendorCommandWithId(device.getDeviceInfo().getLogicalAddress(), i2, HdmiControlService.this.getVendorId(), bArr));
                    } else {
                        HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildVendorCommand(device.getDeviceInfo().getLogicalAddress(), i2, bArr));
                    }
                }
            });
        }

        public void sendStandby(final int deviceType, final int deviceId) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiMhlLocalDeviceStub mhlDevice = HdmiControlService.this.mMhlController.getLocalDeviceById(deviceId);
                    if (mhlDevice != null) {
                        mhlDevice.sendStandby();
                        return;
                    }
                    HdmiCecLocalDevice device = HdmiControlService.this.mCecController.getLocalDevice(deviceType);
                    if (device == null) {
                        Slog.w(HdmiControlService.TAG, "Local device not available");
                    } else {
                        device.sendStandby(deviceId);
                    }
                }
            });
        }

        public void setHdmiRecordListener(IHdmiRecordListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.setHdmiRecordListener(listener);
        }

        public void startOneTouchRecord(final int recorderAddress, final byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (HdmiControlService.this.isTvDeviceEnabled()) {
                        HdmiControlService.this.tv().startOneTouchRecord(recorderAddress, recordSource);
                    } else {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    }
                }
            });
        }

        public void stopOneTouchRecord(final int recorderAddress) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (HdmiControlService.this.isTvDeviceEnabled()) {
                        HdmiControlService.this.tv().stopOneTouchRecord(recorderAddress);
                    } else {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    }
                }
            });
        }

        public void startTimerRecording(final int recorderAddress, final int sourceType, final byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (HdmiControlService.this.isTvDeviceEnabled()) {
                        HdmiControlService.this.tv().startTimerRecording(recorderAddress, sourceType, recordSource);
                    } else {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    }
                }
            });
        }

        public void clearTimerRecording(final int recorderAddress, final int sourceType, final byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (HdmiControlService.this.isTvDeviceEnabled()) {
                        HdmiControlService.this.tv().clearTimerRecording(recorderAddress, sourceType, recordSource);
                    } else {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    }
                }
            });
        }

        public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) {
            HdmiControlService.this.enforceAccessPermission();
            final int i = portId;
            final int i2 = offset;
            final int i3 = length;
            final byte[] bArr = data;
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (!HdmiControlService.this.isControlEnabled()) {
                        Slog.w(HdmiControlService.TAG, "Hdmi control is disabled.");
                    } else if (HdmiControlService.this.mMhlController.getLocalDevice(i) == null) {
                        Slog.w(HdmiControlService.TAG, "Invalid port id:" + i);
                    } else {
                        HdmiControlService.this.mMhlController.sendVendorCommand(i, i2, i3, bArr);
                    }
                }
            });
        }

        public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.addHdmiMhlVendorCommandListener(listener);
        }

        public void setStandbyMode(final boolean isStandbyModeOn) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    HdmiControlService.this.setStandbyMode(isStandbyModeOn);
                }
            });
        }

        protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            if (DumpUtils.checkDumpPermission(HdmiControlService.this.getContext(), HdmiControlService.TAG, writer)) {
                IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
                pw.println("mHdmiControlEnabled: " + HdmiControlService.this.mHdmiControlEnabled);
                pw.println("mProhibitMode: " + HdmiControlService.this.mProhibitMode);
                if (HdmiControlService.this.mCecController != null) {
                    pw.println("mCecController: ");
                    pw.increaseIndent();
                    HdmiControlService.this.mCecController.dump(pw);
                    pw.decreaseIndent();
                }
                pw.println("mMhlController: ");
                pw.increaseIndent();
                HdmiControlService.this.mMhlController.dump(pw);
                pw.decreaseIndent();
                pw.println("mPortInfo: ");
                pw.increaseIndent();
                for (HdmiPortInfo hdmiPortInfo : HdmiControlService.this.mPortInfo) {
                    pw.println("- " + hdmiPortInfo);
                }
                pw.decreaseIndent();
                pw.println("mPowerStatus: " + HdmiControlService.this.mPowerStatus);
            }
        }
    }

    private final class CecMessageBuffer {
        private List<HdmiCecMessage> mBuffer;

        /* synthetic */ CecMessageBuffer(HdmiControlService this$0, CecMessageBuffer -this1) {
            this();
        }

        private CecMessageBuffer() {
            this.mBuffer = new ArrayList();
        }

        public void bufferMessage(HdmiCecMessage message) {
            switch (message.getOpcode()) {
                case 4:
                case 13:
                    bufferImageOrTextViewOn(message);
                    return;
                case 130:
                    bufferActiveSource(message);
                    return;
                default:
                    return;
            }
        }

        public void processMessages() {
            for (final HdmiCecMessage message : this.mBuffer) {
                HdmiControlService.this.runOnServiceThread(new Runnable() {
                    public void run() {
                        HdmiControlService.this.handleCecCommand(message);
                    }
                });
            }
            this.mBuffer.clear();
        }

        private void bufferActiveSource(HdmiCecMessage message) {
            if (!replaceMessageIfBuffered(message, 130)) {
                this.mBuffer.add(message);
            }
        }

        private void bufferImageOrTextViewOn(HdmiCecMessage message) {
            if (!replaceMessageIfBuffered(message, 4) && (replaceMessageIfBuffered(message, 13) ^ 1) != 0) {
                this.mBuffer.add(message);
            }
        }

        private boolean replaceMessageIfBuffered(HdmiCecMessage message, int opcode) {
            for (int i = 0; i < this.mBuffer.size(); i++) {
                if (((HdmiCecMessage) this.mBuffer.get(i)).getOpcode() == opcode) {
                    this.mBuffer.set(i, message);
                    return true;
                }
            }
            return false;
        }
    }

    private final class DeviceEventListenerRecord implements DeathRecipient {
        private final IHdmiDeviceEventListener mListener;

        public DeviceEventListenerRecord(IHdmiDeviceEventListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mDeviceEventListenerRecords.remove(this);
            }
        }
    }

    private class HdmiControlBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ HdmiControlBroadcastReceiver(HdmiControlService this$0, HdmiControlBroadcastReceiver -this1) {
            this();
        }

        private HdmiControlBroadcastReceiver() {
        }

        @ServiceThreadOnly
        public void onReceive(Context context, Intent intent) {
            HdmiControlService.this.assertRunOnServiceThread();
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (HdmiControlService.this.isPowerOnOrTransient()) {
                    HdmiControlService.this.onStandby(0);
                }
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                if (HdmiControlService.this.isPowerStandbyOrTransient()) {
                    HdmiControlService.this.onWakeUp();
                }
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                String language = getMenuLanguage();
                if (!HdmiControlService.this.mLanguage.equals(language)) {
                    HdmiControlService.this.onLanguageChanged(language);
                }
            } else if (action.equals("android.intent.action.ACTION_SHUTDOWN") && HdmiControlService.this.isPowerOnOrTransient()) {
                HdmiControlService.this.onStandby(1);
            }
        }

        private String getMenuLanguage() {
            Locale locale = Locale.getDefault();
            if (locale.equals(Locale.TAIWAN) || locale.equals(HdmiControlService.this.HONG_KONG) || locale.equals(HdmiControlService.this.MACAU)) {
                return "chi";
            }
            return locale.getISO3Language();
        }
    }

    private class HdmiMhlVendorCommandListenerRecord implements DeathRecipient {
        private final IHdmiMhlVendorCommandListener mListener;

        public HdmiMhlVendorCommandListenerRecord(IHdmiMhlVendorCommandListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            HdmiControlService.this.mMhlVendorCommandListenerRecords.remove(this);
        }
    }

    private class HdmiRecordListenerRecord implements DeathRecipient {
        private final IHdmiRecordListener mListener;

        public HdmiRecordListenerRecord(IHdmiRecordListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                if (HdmiControlService.this.mRecordListenerRecord == this) {
                    HdmiControlService.this.mRecordListenerRecord = null;
                }
            }
        }
    }

    private final class HotplugEventListenerRecord implements DeathRecipient {
        private final IHdmiHotplugEventListener mListener;

        public HotplugEventListenerRecord(IHdmiHotplugEventListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mHotplugEventListenerRecords.remove(this);
            }
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (!(obj instanceof HotplugEventListenerRecord)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (((HotplugEventListenerRecord) obj).mListener != this.mListener) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return this.mListener.hashCode();
        }
    }

    private final class InputChangeListenerRecord implements DeathRecipient {
        private final IHdmiInputChangeListener mListener;

        public InputChangeListenerRecord(IHdmiInputChangeListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                if (HdmiControlService.this.mInputChangeListenerRecord == this) {
                    HdmiControlService.this.mInputChangeListenerRecord = null;
                }
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            String option = uri.getLastPathSegment();
            boolean enabled = HdmiControlService.this.readBooleanSetting(option, true);
            if (option.equals("hdmi_control_enabled")) {
                HdmiControlService.this.setControlEnabled(enabled);
            } else if (option.equals("hdmi_control_auto_wakeup_enabled")) {
                if (HdmiControlService.this.isTvDeviceEnabled()) {
                    HdmiControlService.this.tv().setAutoWakeup(enabled);
                }
                HdmiControlService.this.setCecOption(1, enabled);
            } else if (option.equals("hdmi_control_auto_device_off_enabled")) {
                for (Integer intValue : HdmiControlService.this.mLocalDevices) {
                    HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(intValue.intValue());
                    if (localDevice != null) {
                        localDevice.setAutoDeviceOff(enabled);
                    }
                }
            } else if (option.equals("hdmi_system_audio_control_enabled")) {
                if (HdmiControlService.this.isTvDeviceEnabled()) {
                    HdmiControlService.this.tv().setSystemAudioControlFeatureEnabled(enabled);
                }
            } else if (option.equals("mhl_input_switching_enabled")) {
                HdmiControlService.this.setMhlInputChangeEnabled(enabled);
            } else if (option.equals("mhl_power_charge_enabled")) {
                HdmiControlService.this.mMhlController.setOption(102, HdmiControlService.toInt(enabled));
            }
        }
    }

    private final class SystemAudioModeChangeListenerRecord implements DeathRecipient {
        private final IHdmiSystemAudioModeChangeListener mListener;

        public SystemAudioModeChangeListenerRecord(IHdmiSystemAudioModeChangeListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mSystemAudioModeChangeListenerRecords.remove(this);
            }
        }
    }

    class VendorCommandListenerRecord implements DeathRecipient {
        private final int mDeviceType;
        private final IHdmiVendorCommandListener mListener;

        public VendorCommandListenerRecord(IHdmiVendorCommandListener listener, int deviceType) {
            this.mListener = listener;
            this.mDeviceType = deviceType;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mVendorCommandListenerRecords.remove(this);
            }
        }
    }

    public HdmiControlService(Context context) {
        super(context);
    }

    private static List<Integer> getIntList(String string) {
        ArrayList<Integer> list = new ArrayList();
        SimpleStringSplitter<String> splitter = new SimpleStringSplitter(',');
        splitter.setString(string);
        for (String item : splitter) {
            try {
                list.add(Integer.valueOf(Integer.parseInt(item)));
            } catch (NumberFormatException e) {
                Slog.w(TAG, "Can't parseInt: " + item);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public void onStart() {
        this.mIoThread.start();
        this.mPowerStatus = 2;
        this.mProhibitMode = false;
        this.mHdmiControlEnabled = readBooleanSetting("hdmi_control_enabled", true);
        this.mMhlInputChangeEnabled = readBooleanSetting("mhl_input_switching_enabled", true);
        this.mCecController = HdmiCecController.create(this);
        if (this.mCecController != null) {
            if (this.mHdmiControlEnabled) {
                initializeCec(1);
            }
            this.mMhlController = HdmiMhlControllerStub.create(this);
            if (!this.mMhlController.isReady()) {
                Slog.i(TAG, "Device does not support MHL-control.");
            }
            this.mMhlDevices = Collections.emptyList();
            initPortInfo();
            this.mMessageValidator = new HdmiCecMessageValidator(this);
            publishBinderService("hdmi_control", new BinderService(this, null));
            if (this.mCecController != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SCREEN_OFF");
                filter.addAction("android.intent.action.SCREEN_ON");
                filter.addAction("android.intent.action.ACTION_SHUTDOWN");
                filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
                getContext().registerReceiver(this.mHdmiControlBroadcastReceiver, filter);
                registerContentObserver();
            }
            this.mMhlController.setOption(104, 1);
            return;
        }
        Slog.i(TAG, "Device does not support HDMI-CEC.");
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mTvInputManager = (TvInputManager) getContext().getSystemService("tv_input");
            this.mPowerManager = (PowerManager) getContext().getSystemService("power");
        }
    }

    TvInputManager getTvInputManager() {
        return this.mTvInputManager;
    }

    void registerTvInputCallback(TvInputCallback callback) {
        if (this.mTvInputManager != null) {
            this.mTvInputManager.registerCallback(callback, this.mHandler);
        }
    }

    void unregisterTvInputCallback(TvInputCallback callback) {
        if (this.mTvInputManager != null) {
            this.mTvInputManager.unregisterCallback(callback);
        }
    }

    PowerManager getPowerManager() {
        return this.mPowerManager;
    }

    private void onInitializeCecComplete(int initiatedBy) {
        if (this.mPowerStatus == 2) {
            this.mPowerStatus = 0;
        }
        this.mWakeUpMessageReceived = false;
        if (isTvDeviceEnabled()) {
            this.mCecController.setOption(1, tv().getAutoWakeup());
        }
        int reason = -1;
        switch (initiatedBy) {
            case 0:
                reason = 1;
                break;
            case 1:
                reason = 0;
                break;
            case 2:
            case 3:
                reason = 2;
                break;
        }
        if (reason != -1) {
            invokeVendorCommandListenersOnControlStateChanged(true, reason);
        }
    }

    private void registerContentObserver() {
        ContentResolver resolver = getContext().getContentResolver();
        for (String s : new String[]{"hdmi_control_enabled", "hdmi_control_auto_wakeup_enabled", "hdmi_control_auto_device_off_enabled", "hdmi_system_audio_control_enabled", "mhl_input_switching_enabled", "mhl_power_charge_enabled"}) {
            resolver.registerContentObserver(Global.getUriFor(s), false, this.mSettingsObserver, -1);
        }
    }

    private static int toInt(boolean enabled) {
        return enabled ? 1 : 0;
    }

    boolean readBooleanSetting(String key, boolean defVal) {
        if (Global.getInt(getContext().getContentResolver(), key, toInt(defVal)) == 1) {
            return true;
        }
        return false;
    }

    void writeBooleanSetting(String key, boolean value) {
        Global.putInt(getContext().getContentResolver(), key, toInt(value));
    }

    private void initializeCec(int initiatedBy) {
        this.mAddressAllocated = false;
        this.mCecController.setOption(3, true);
        this.mCecController.setLanguage(this.mLanguage);
        initializeLocalDevices(initiatedBy);
    }

    @ServiceThreadOnly
    private void initializeLocalDevices(int initiatedBy) {
        assertRunOnServiceThread();
        ArrayList<HdmiCecLocalDevice> localDevices = new ArrayList();
        for (Integer intValue : this.mLocalDevices) {
            int type = intValue.intValue();
            HdmiCecLocalDevice localDevice = this.mCecController.getLocalDevice(type);
            if (localDevice == null) {
                localDevice = HdmiCecLocalDevice.create(this, type);
            }
            localDevice.init();
            localDevices.add(localDevice);
        }
        clearLocalDevices();
        allocateLogicalAddress(localDevices, initiatedBy);
    }

    @ServiceThreadOnly
    private void allocateLogicalAddress(ArrayList<HdmiCecLocalDevice> allocatingDevices, int initiatedBy) {
        assertRunOnServiceThread();
        this.mCecController.clearLogicalAddress();
        final ArrayList<HdmiCecLocalDevice> allocatedDevices = new ArrayList();
        final int[] finished = new int[1];
        this.mAddressAllocated = allocatingDevices.isEmpty();
        this.mSelectRequestBuffer.clear();
        for (final HdmiCecLocalDevice localDevice : allocatingDevices) {
            final ArrayList<HdmiCecLocalDevice> arrayList = allocatingDevices;
            final int i = initiatedBy;
            this.mCecController.allocateLogicalAddress(localDevice.getType(), localDevice.getPreferredAddress(), new AllocateAddressCallback() {
                public void onAllocated(int deviceType, int logicalAddress) {
                    if (logicalAddress == 15) {
                        Slog.e(HdmiControlService.TAG, "Failed to allocate address:[device_type:" + deviceType + "]");
                    } else {
                        localDevice.setDeviceInfo(HdmiControlService.this.createDeviceInfo(logicalAddress, deviceType, 0));
                        HdmiControlService.this.mCecController.addLocalDevice(deviceType, localDevice);
                        HdmiControlService.this.mCecController.addLogicalAddress(logicalAddress);
                        allocatedDevices.add(localDevice);
                    }
                    int size = arrayList.size();
                    int[] iArr = finished;
                    int i = iArr[0] + 1;
                    iArr[0] = i;
                    if (size == i) {
                        HdmiControlService.this.mAddressAllocated = true;
                        if (i != 4) {
                            HdmiControlService.this.onInitializeCecComplete(i);
                        }
                        HdmiControlService.this.notifyAddressAllocated(allocatedDevices, i);
                        HdmiControlService.this.mCecMessageBuffer.processMessages();
                    }
                }
            });
        }
    }

    @ServiceThreadOnly
    private void notifyAddressAllocated(ArrayList<HdmiCecLocalDevice> devices, int initiatedBy) {
        assertRunOnServiceThread();
        for (HdmiCecLocalDevice device : devices) {
            device.handleAddressAllocated(device.getDeviceInfo().getLogicalAddress(), initiatedBy);
        }
        if (isTvDeviceEnabled()) {
            tv().setSelectRequestBuffer(this.mSelectRequestBuffer);
        }
    }

    boolean isAddressAllocated() {
        return this.mAddressAllocated;
    }

    @ServiceThreadOnly
    private void initPortInfo() {
        assertRunOnServiceThread();
        Object[] cecPortInfo = null;
        if (this.mCecController != null) {
            cecPortInfo = this.mCecController.getPortInfos();
        }
        if (cecPortInfo != null) {
            int i;
            HdmiPortInfo info;
            SparseArray<HdmiPortInfo> portInfoMap = new SparseArray();
            SparseIntArray portIdMap = new SparseIntArray();
            SparseArray<HdmiDeviceInfo> portDeviceMap = new SparseArray();
            for (HdmiPortInfo info2 : cecPortInfo) {
                portIdMap.put(info2.getAddress(), info2.getId());
                portInfoMap.put(info2.getId(), info2);
                portDeviceMap.put(info2.getId(), new HdmiDeviceInfo(info2.getAddress(), info2.getId()));
            }
            this.mPortIdMap = new UnmodifiableSparseIntArray(portIdMap);
            this.mPortInfoMap = new UnmodifiableSparseArray(portInfoMap);
            this.mPortDeviceMap = new UnmodifiableSparseArray(portDeviceMap);
            HdmiPortInfo[] mhlPortInfo = this.mMhlController.getPortInfos();
            ArraySet<Integer> mhlSupportedPorts = new ArraySet(mhlPortInfo.length);
            for (HdmiPortInfo info22 : mhlPortInfo) {
                if (info22.isMhlSupported()) {
                    mhlSupportedPorts.add(Integer.valueOf(info22.getId()));
                }
            }
            if (mhlSupportedPorts.isEmpty()) {
                this.mPortInfo = Collections.unmodifiableList(Arrays.asList(cecPortInfo));
                return;
            }
            ArrayList<HdmiPortInfo> arrayList = new ArrayList(cecPortInfo.length);
            i = 0;
            int length = cecPortInfo.length;
            while (true) {
                int i2 = i;
                if (i2 < length) {
                    info22 = cecPortInfo[i2];
                    if (mhlSupportedPorts.contains(Integer.valueOf(info22.getId()))) {
                        arrayList.add(new HdmiPortInfo(info22.getId(), info22.getType(), info22.getAddress(), info22.isCecSupported(), true, info22.isArcSupported()));
                    } else {
                        arrayList.add(info22);
                    }
                    i = i2 + 1;
                } else {
                    this.mPortInfo = Collections.unmodifiableList(arrayList);
                    return;
                }
            }
        }
    }

    List<HdmiPortInfo> getPortInfo() {
        return this.mPortInfo;
    }

    HdmiPortInfo getPortInfo(int portId) {
        return (HdmiPortInfo) this.mPortInfoMap.get(portId, null);
    }

    int portIdToPath(int portId) {
        HdmiPortInfo portInfo = getPortInfo(portId);
        if (portInfo != null) {
            return portInfo.getAddress();
        }
        Slog.e(TAG, "Cannot find the port info: " + portId);
        return NetworkConstants.ARP_HWTYPE_RESERVED_HI;
    }

    int pathToPortId(int path) {
        return this.mPortIdMap.get(path & 61440, -1);
    }

    boolean isValidPortId(int portId) {
        return getPortInfo(portId) != null;
    }

    Looper getIoLooper() {
        return this.mIoThread.getLooper();
    }

    Looper getServiceLooper() {
        return this.mHandler.getLooper();
    }

    int getPhysicalAddress() {
        return this.mCecController.getPhysicalAddress();
    }

    int getVendorId() {
        return this.mCecController.getVendorId();
    }

    @ServiceThreadOnly
    HdmiDeviceInfo getDeviceInfo(int logicalAddress) {
        assertRunOnServiceThread();
        if (tv() == null) {
            return null;
        }
        return tv().getCecDeviceInfo(logicalAddress);
    }

    @ServiceThreadOnly
    HdmiDeviceInfo getDeviceInfoByPort(int port) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub info = this.mMhlController.getLocalDevice(port);
        if (info != null) {
            return info.getInfo();
        }
        return null;
    }

    int getCecVersion() {
        return this.mCecController.getVersion();
    }

    boolean isConnectedToArcPort(int physicalAddress) {
        int portId = pathToPortId(physicalAddress);
        if (portId != -1) {
            return ((HdmiPortInfo) this.mPortInfoMap.get(portId)).isArcSupported();
        }
        return false;
    }

    @ServiceThreadOnly
    boolean isConnected(int portId) {
        assertRunOnServiceThread();
        return this.mCecController.isConnected(portId);
    }

    void runOnServiceThread(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    void runOnServiceThreadAtFrontOfQueue(Runnable runnable) {
        this.mHandler.postAtFrontOfQueue(runnable);
    }

    private void assertRunOnServiceThread() {
        if (Looper.myLooper() != this.mHandler.getLooper()) {
            throw new IllegalStateException("Should run on service thread.");
        }
    }

    @ServiceThreadOnly
    void sendCecCommand(HdmiCecMessage command, SendMessageCallback callback) {
        assertRunOnServiceThread();
        if (this.mMessageValidator.isValid(command) == 0) {
            this.mCecController.sendCommand(command, callback);
            return;
        }
        HdmiLogger.error("Invalid message type:" + command, new Object[0]);
        if (callback != null) {
            callback.onSendCompleted(3);
        }
    }

    @ServiceThreadOnly
    void sendCecCommand(HdmiCecMessage command) {
        assertRunOnServiceThread();
        sendCecCommand(command, null);
    }

    @ServiceThreadOnly
    void maySendFeatureAbortCommand(HdmiCecMessage command, int reason) {
        assertRunOnServiceThread();
        this.mCecController.maySendFeatureAbortCommand(command, reason);
    }

    @ServiceThreadOnly
    boolean handleCecCommand(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (this.mAddressAllocated) {
            int errorCode = this.mMessageValidator.isValid(message);
            if (errorCode == 0) {
                return dispatchMessageToLocalDevice(message);
            }
            if (errorCode == 3) {
                maySendFeatureAbortCommand(message, 3);
            }
            return true;
        }
        this.mCecMessageBuffer.bufferMessage(message);
        return true;
    }

    void enableAudioReturnChannel(int portId, boolean enabled) {
        this.mCecController.enableAudioReturnChannel(portId, enabled);
    }

    @ServiceThreadOnly
    private boolean dispatchMessageToLocalDevice(HdmiCecMessage message) {
        assertRunOnServiceThread();
        for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
            if (device.dispatchMessage(message) && message.getDestination() != 15) {
                return true;
            }
        }
        if (message.getDestination() != 15) {
            HdmiLogger.warning("Unhandled cec command:" + message, new Object[0]);
        }
        return false;
    }

    @ServiceThreadOnly
    void onHotplug(int portId, boolean connected) {
        assertRunOnServiceThread();
        if (connected && (isTvDevice() ^ 1) != 0) {
            ArrayList<HdmiCecLocalDevice> localDevices = new ArrayList();
            for (Integer intValue : this.mLocalDevices) {
                int type = intValue.intValue();
                HdmiCecLocalDevice localDevice = this.mCecController.getLocalDevice(type);
                if (localDevice == null) {
                    localDevice = HdmiCecLocalDevice.create(this, type);
                    localDevice.init();
                }
                localDevices.add(localDevice);
            }
            allocateLogicalAddress(localDevices, 4);
        }
        for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
            device.onHotplug(portId, connected);
        }
        announceHotplugEvent(portId, connected);
    }

    @ServiceThreadOnly
    void pollDevices(DevicePollingCallback callback, int sourceAddress, int pickStrategy, int retryCount) {
        assertRunOnServiceThread();
        this.mCecController.pollDevices(callback, sourceAddress, checkPollStrategy(pickStrategy), retryCount);
    }

    private int checkPollStrategy(int pickStrategy) {
        int strategy = pickStrategy & 3;
        if (strategy == 0) {
            throw new IllegalArgumentException("Invalid poll strategy:" + pickStrategy);
        }
        int iterationStrategy = pickStrategy & 196608;
        if (iterationStrategy != 0) {
            return strategy | iterationStrategy;
        }
        throw new IllegalArgumentException("Invalid iteration strategy:" + pickStrategy);
    }

    List<HdmiCecLocalDevice> getAllLocalDevices() {
        assertRunOnServiceThread();
        return this.mCecController.getLocalDeviceList();
    }

    Object getServiceLock() {
        return this.mLock;
    }

    void setAudioStatus(boolean mute, int volume) {
        if (isTvDeviceEnabled() && (tv().isSystemAudioActivated() ^ 1) == 0) {
            AudioManager audioManager = getAudioManager();
            boolean muted = audioManager.isStreamMute(3);
            if (!mute) {
                if (muted) {
                    audioManager.setStreamMute(3, false);
                }
                audioManager.setStreamVolume(3, volume, LightsManager.LIGHT_ID_SMARTBACKLIGHT);
            } else if (!muted) {
                audioManager.setStreamMute(3, true);
            }
        }
    }

    void announceSystemAudioModeChange(boolean enabled) {
        synchronized (this.mLock) {
            for (SystemAudioModeChangeListenerRecord record : this.mSystemAudioModeChangeListenerRecords) {
                invokeSystemAudioModeChangeLocked(record.mListener, enabled);
            }
        }
    }

    private HdmiDeviceInfo createDeviceInfo(int logicalAddress, int deviceType, int powerStatus) {
        return new HdmiDeviceInfo(logicalAddress, getPhysicalAddress(), pathToPortId(getPhysicalAddress()), deviceType, getVendorId(), Build.MODEL);
    }

    @ServiceThreadOnly
    void handleMhlHotplugEvent(int portId, boolean connected) {
        assertRunOnServiceThread();
        if (connected) {
            HdmiMhlLocalDeviceStub newDevice = new HdmiMhlLocalDeviceStub(this, portId);
            HdmiMhlLocalDeviceStub oldDevice = this.mMhlController.addLocalDevice(newDevice);
            if (oldDevice != null) {
                oldDevice.onDeviceRemoved();
                Slog.i(TAG, "Old device of port " + portId + " is removed");
            }
            invokeDeviceEventListeners(newDevice.getInfo(), 1);
            updateSafeMhlInput();
        } else {
            HdmiMhlLocalDeviceStub device = this.mMhlController.removeLocalDevice(portId);
            if (device != null) {
                device.onDeviceRemoved();
                invokeDeviceEventListeners(device.getInfo(), 2);
                updateSafeMhlInput();
            } else {
                Slog.w(TAG, "No device to remove:[portId=" + portId);
            }
        }
        announceHotplugEvent(portId, connected);
    }

    @ServiceThreadOnly
    void handleMhlBusModeChanged(int portId, int busmode) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub device = this.mMhlController.getLocalDevice(portId);
        if (device != null) {
            device.setBusMode(busmode);
        } else {
            Slog.w(TAG, "No mhl device exists for bus mode change[portId:" + portId + ", busmode:" + busmode + "]");
        }
    }

    @ServiceThreadOnly
    void handleMhlBusOvercurrent(int portId, boolean on) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub device = this.mMhlController.getLocalDevice(portId);
        if (device != null) {
            device.onBusOvercurrentDetected(on);
        } else {
            Slog.w(TAG, "No mhl device exists for bus overcurrent event[portId:" + portId + "]");
        }
    }

    @ServiceThreadOnly
    void handleMhlDeviceStatusChanged(int portId, int adopterId, int deviceId) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub device = this.mMhlController.getLocalDevice(portId);
        if (device != null) {
            device.setDeviceStatusChange(adopterId, deviceId);
        } else {
            Slog.w(TAG, "No mhl device exists for device status event[portId:" + portId + ", adopterId:" + adopterId + ", deviceId:" + deviceId + "]");
        }
    }

    @ServiceThreadOnly
    private void updateSafeMhlInput() {
        assertRunOnServiceThread();
        List<HdmiDeviceInfo> inputs = Collections.emptyList();
        SparseArray<HdmiMhlLocalDeviceStub> devices = this.mMhlController.getAllLocalDevices();
        for (int i = 0; i < devices.size(); i++) {
            HdmiMhlLocalDeviceStub device = (HdmiMhlLocalDeviceStub) devices.valueAt(i);
            if (device.getInfo() != null) {
                if (inputs.isEmpty()) {
                    inputs = new ArrayList();
                }
                inputs.add(device.getInfo());
            }
        }
        synchronized (this.mLock) {
            this.mMhlDevices = inputs;
        }
    }

    private List<HdmiDeviceInfo> getMhlDevicesLocked() {
        return this.mMhlDevices;
    }

    private void enforceAccessPermission() {
        getContext().enforceCallingOrSelfPermission(PERMISSION, TAG);
    }

    @ServiceThreadOnly
    private void oneTouchPlay(IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        HdmiCecLocalDevicePlayback source = playback();
        if (source == null) {
            Slog.w(TAG, "Local playback device not available");
            invokeCallback(callback, 2);
            return;
        }
        source.oneTouchPlay(callback);
    }

    @ServiceThreadOnly
    private void queryDisplayStatus(IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        HdmiCecLocalDevicePlayback source = playback();
        if (source == null) {
            Slog.w(TAG, "Local playback device not available");
            invokeCallback(callback, 2);
            return;
        }
        source.queryDisplayStatus(callback);
    }

    private void addHotplugEventListener(final IHdmiHotplugEventListener listener) {
        final HotplugEventListenerRecord record = new HotplugEventListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, 0);
            synchronized (this.mLock) {
                this.mHotplugEventListenerRecords.add(record);
            }
            runOnServiceThread(new Runnable() {
                /* JADX WARNING: Missing block: B:8:0x0018, code:
            r2 = com.android.server.hdmi.HdmiControlService.-get15(r6.this$0).iterator();
     */
                /* JADX WARNING: Missing block: B:10:0x0026, code:
            if (r2.hasNext() == false) goto L_0x005b;
     */
                /* JADX WARNING: Missing block: B:11:0x0028, code:
            r1 = (android.hardware.hdmi.HdmiPortInfo) r2.next();
            r0 = new android.hardware.hdmi.HdmiHotplugEvent(r1.getId(), com.android.server.hdmi.HdmiControlService.-get4(r6.this$0).isConnected(r1.getId()));
            r4 = com.android.server.hdmi.HdmiControlService.-get12(r6.this$0);
     */
                /* JADX WARNING: Missing block: B:12:0x004b, code:
            monitor-enter(r4);
     */
                /* JADX WARNING: Missing block: B:14:?, code:
            com.android.server.hdmi.HdmiControlService.-wrap13(r6.this$0, r5, r0);
     */
                /* JADX WARNING: Missing block: B:15:0x0053, code:
            monitor-exit(r4);
     */
                /* JADX WARNING: Missing block: B:23:0x005b, code:
            return;
     */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    synchronized (HdmiControlService.this.mLock) {
                        if (!HdmiControlService.this.mHotplugEventListenerRecords.contains(record)) {
                        }
                    }
                }
            });
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died");
        }
    }

    private void removeHotplugEventListener(IHdmiHotplugEventListener listener) {
        synchronized (this.mLock) {
            for (HotplugEventListenerRecord record : this.mHotplugEventListenerRecords) {
                if (record.mListener.asBinder() == listener.asBinder()) {
                    listener.asBinder().unlinkToDeath(record, 0);
                    this.mHotplugEventListenerRecords.remove(record);
                    break;
                }
            }
        }
    }

    private void addDeviceEventListener(IHdmiDeviceEventListener listener) {
        DeviceEventListenerRecord record = new DeviceEventListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, 0);
            synchronized (this.mLock) {
                this.mDeviceEventListenerRecords.add(record);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died");
        }
    }

    void invokeDeviceEventListeners(HdmiDeviceInfo device, int status) {
        synchronized (this.mLock) {
            for (DeviceEventListenerRecord record : this.mDeviceEventListenerRecords) {
                try {
                    record.mListener.onStatusChanged(device, status);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to report device event:" + e);
                }
            }
        }
    }

    private void addSystemAudioModeChangeListner(IHdmiSystemAudioModeChangeListener listener) {
        SystemAudioModeChangeListenerRecord record = new SystemAudioModeChangeListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, 0);
            synchronized (this.mLock) {
                this.mSystemAudioModeChangeListenerRecords.add(record);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died");
        }
    }

    private void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {
        synchronized (this.mLock) {
            for (SystemAudioModeChangeListenerRecord record : this.mSystemAudioModeChangeListenerRecords) {
                if (record.mListener.asBinder() == listener) {
                    listener.asBinder().unlinkToDeath(record, 0);
                    this.mSystemAudioModeChangeListenerRecords.remove(record);
                    break;
                }
            }
        }
    }

    private void setInputChangeListener(IHdmiInputChangeListener listener) {
        synchronized (this.mLock) {
            this.mInputChangeListenerRecord = new InputChangeListenerRecord(listener);
            try {
                listener.asBinder().linkToDeath(this.mInputChangeListenerRecord, 0);
            } catch (RemoteException e) {
                Slog.w(TAG, "Listener already died");
            }
        }
    }

    void invokeInputChangeListener(HdmiDeviceInfo info) {
        synchronized (this.mLock) {
            if (this.mInputChangeListenerRecord != null) {
                try {
                    this.mInputChangeListenerRecord.mListener.onChanged(info);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception thrown by IHdmiInputChangeListener: " + e);
                }
            }
        }
        return;
    }

    private void setHdmiRecordListener(IHdmiRecordListener listener) {
        synchronized (this.mLock) {
            this.mRecordListenerRecord = new HdmiRecordListenerRecord(listener);
            try {
                listener.asBinder().linkToDeath(this.mRecordListenerRecord, 0);
            } catch (RemoteException e) {
                Slog.w(TAG, "Listener already died.", e);
            }
        }
        return;
    }

    byte[] invokeRecordRequestListener(int recorderAddress) {
        synchronized (this.mLock) {
            byte[] oneTouchRecordSource;
            if (this.mRecordListenerRecord != null) {
                try {
                    oneTouchRecordSource = this.mRecordListenerRecord.mListener.getOneTouchRecordSource(recorderAddress);
                    return oneTouchRecordSource;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to start record.", e);
                }
            }
            oneTouchRecordSource = EmptyArray.BYTE;
            return oneTouchRecordSource;
        }
    }

    void invokeOneTouchRecordResult(int recorderAddress, int result) {
        synchronized (this.mLock) {
            if (this.mRecordListenerRecord != null) {
                try {
                    this.mRecordListenerRecord.mListener.onOneTouchRecordResult(recorderAddress, result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onOneTouchRecordResult.", e);
                }
            }
        }
        return;
    }

    void invokeTimerRecordingResult(int recorderAddress, int result) {
        synchronized (this.mLock) {
            if (this.mRecordListenerRecord != null) {
                try {
                    this.mRecordListenerRecord.mListener.onTimerRecordingResult(recorderAddress, result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onTimerRecordingResult.", e);
                }
            }
        }
        return;
    }

    void invokeClearTimerRecordingResult(int recorderAddress, int result) {
        synchronized (this.mLock) {
            if (this.mRecordListenerRecord != null) {
                try {
                    this.mRecordListenerRecord.mListener.onClearTimerRecordingResult(recorderAddress, result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onClearTimerRecordingResult.", e);
                }
            }
        }
        return;
    }

    private void invokeCallback(IHdmiControlCallback callback, int result) {
        try {
            callback.onComplete(result);
        } catch (RemoteException e) {
            Slog.e(TAG, "Invoking callback failed:" + e);
        }
    }

    private void invokeSystemAudioModeChangeLocked(IHdmiSystemAudioModeChangeListener listener, boolean enabled) {
        try {
            listener.onStatusChanged(enabled);
        } catch (RemoteException e) {
            Slog.e(TAG, "Invoking callback failed:" + e);
        }
    }

    private void announceHotplugEvent(int portId, boolean connected) {
        HdmiHotplugEvent event = new HdmiHotplugEvent(portId, connected);
        synchronized (this.mLock) {
            for (HotplugEventListenerRecord record : this.mHotplugEventListenerRecords) {
                invokeHotplugEventListenerLocked(record.mListener, event);
            }
        }
    }

    private void invokeHotplugEventListenerLocked(IHdmiHotplugEventListener listener, HdmiHotplugEvent event) {
        try {
            listener.onReceived(event);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to report hotplug event:" + event.toString(), e);
        }
    }

    public HdmiCecLocalDeviceTv tv() {
        return (HdmiCecLocalDeviceTv) this.mCecController.getLocalDevice(0);
    }

    boolean isTvDevice() {
        return this.mLocalDevices.contains(Integer.valueOf(0));
    }

    boolean isTvDeviceEnabled() {
        return isTvDevice() && tv() != null;
    }

    private HdmiCecLocalDevicePlayback playback() {
        return (HdmiCecLocalDevicePlayback) this.mCecController.getLocalDevice(4);
    }

    AudioManager getAudioManager() {
        return (AudioManager) getContext().getSystemService("audio");
    }

    boolean isControlEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mHdmiControlEnabled;
        }
        return z;
    }

    @ServiceThreadOnly
    int getPowerStatus() {
        assertRunOnServiceThread();
        return this.mPowerStatus;
    }

    @ServiceThreadOnly
    boolean isPowerOnOrTransient() {
        assertRunOnServiceThread();
        if (this.mPowerStatus == 0 || this.mPowerStatus == 2) {
            return true;
        }
        return false;
    }

    @ServiceThreadOnly
    boolean isPowerStandbyOrTransient() {
        assertRunOnServiceThread();
        if (this.mPowerStatus == 1 || this.mPowerStatus == 3) {
            return true;
        }
        return false;
    }

    @ServiceThreadOnly
    boolean isPowerStandby() {
        assertRunOnServiceThread();
        if (this.mPowerStatus == 1) {
            return true;
        }
        return false;
    }

    @ServiceThreadOnly
    void wakeUp() {
        assertRunOnServiceThread();
        this.mWakeUpMessageReceived = true;
        this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server.hdmi:WAKE");
    }

    @ServiceThreadOnly
    void standby() {
        assertRunOnServiceThread();
        if (canGoToStandby()) {
            this.mStandbyMessageReceived = true;
            this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 5, 0);
        }
    }

    boolean isWakeUpMessageReceived() {
        return this.mWakeUpMessageReceived;
    }

    @ServiceThreadOnly
    private void onWakeUp() {
        assertRunOnServiceThread();
        this.mPowerStatus = 2;
        if (this.mCecController == null) {
            Slog.i(TAG, "Device does not support HDMI-CEC.");
        } else if (this.mHdmiControlEnabled) {
            int startReason = 2;
            if (this.mWakeUpMessageReceived) {
                startReason = 3;
            }
            initializeCec(startReason);
        }
    }

    @ServiceThreadOnly
    private void onStandby(final int standbyAction) {
        assertRunOnServiceThread();
        this.mPowerStatus = 3;
        invokeVendorCommandListenersOnControlStateChanged(false, 3);
        if (canGoToStandby()) {
            final List<HdmiCecLocalDevice> devices = getAllLocalDevices();
            disableDevices(new PendingActionClearedCallback() {
                public void onCleared(HdmiCecLocalDevice device) {
                    Slog.v(HdmiControlService.TAG, "On standby-action cleared:" + device.mDeviceType);
                    devices.remove(device);
                    if (devices.isEmpty()) {
                        HdmiControlService.this.onStandbyCompleted(standbyAction);
                    }
                }
            });
            return;
        }
        this.mPowerStatus = 1;
    }

    private boolean canGoToStandby() {
        for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
            if (!device.canGoToStandby()) {
                return false;
            }
        }
        return true;
    }

    @ServiceThreadOnly
    private void onLanguageChanged(String language) {
        assertRunOnServiceThread();
        this.mLanguage = language;
        if (isTvDeviceEnabled()) {
            tv().broadcastMenuLanguage(language);
            this.mCecController.setLanguage(language);
        }
    }

    @ServiceThreadOnly
    String getLanguage() {
        assertRunOnServiceThread();
        return this.mLanguage;
    }

    private void disableDevices(PendingActionClearedCallback callback) {
        if (this.mCecController != null) {
            for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
                device.disableDevice(this.mStandbyMessageReceived, callback);
            }
        }
        this.mMhlController.clearAllLocalDevices();
    }

    @ServiceThreadOnly
    private void clearLocalDevices() {
        assertRunOnServiceThread();
        if (this.mCecController != null) {
            this.mCecController.clearLogicalAddress();
            this.mCecController.clearLocalDevices();
        }
    }

    @ServiceThreadOnly
    private void onStandbyCompleted(int standbyAction) {
        assertRunOnServiceThread();
        Slog.v(TAG, "onStandbyCompleted");
        if (this.mPowerStatus == 3) {
            this.mPowerStatus = 1;
            for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
                device.onStandby(this.mStandbyMessageReceived, standbyAction);
            }
            this.mStandbyMessageReceived = false;
            this.mCecController.setOption(3, false);
            this.mMhlController.setOption(104, 0);
        }
    }

    private void addVendorCommandListener(IHdmiVendorCommandListener listener, int deviceType) {
        VendorCommandListenerRecord record = new VendorCommandListenerRecord(listener, deviceType);
        try {
            listener.asBinder().linkToDeath(record, 0);
            synchronized (this.mLock) {
                this.mVendorCommandListenerRecords.add(record);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died");
        }
    }

    boolean invokeVendorCommandListenersOnReceived(int deviceType, int srcAddress, int destAddress, byte[] params, boolean hasVendorId) {
        synchronized (this.mLock) {
            if (this.mVendorCommandListenerRecords.isEmpty()) {
                return false;
            }
            for (VendorCommandListenerRecord record : this.mVendorCommandListenerRecords) {
                if (record.mDeviceType == deviceType) {
                    try {
                        record.mListener.onReceived(srcAddress, destAddress, params, hasVendorId);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Failed to notify vendor command reception", e);
                    }
                }
            }
            return true;
        }
    }

    boolean invokeVendorCommandListenersOnControlStateChanged(boolean enabled, int reason) {
        synchronized (this.mLock) {
            if (this.mVendorCommandListenerRecords.isEmpty()) {
                return false;
            }
            for (VendorCommandListenerRecord record : this.mVendorCommandListenerRecords) {
                try {
                    record.mListener.onControlStateChanged(enabled, reason);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to notify control-state-changed to vendor handler", e);
                }
            }
            return true;
        }
    }

    private void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) {
        HdmiMhlVendorCommandListenerRecord record = new HdmiMhlVendorCommandListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, 0);
            synchronized (this.mLock) {
                this.mMhlVendorCommandListenerRecords.add(record);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died.");
        }
    }

    void invokeMhlVendorCommandListeners(int portId, int offest, int length, byte[] data) {
        synchronized (this.mLock) {
            for (HdmiMhlVendorCommandListenerRecord record : this.mMhlVendorCommandListenerRecords) {
                try {
                    record.mListener.onReceived(portId, offest, length, data);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to notify MHL vendor command", e);
                }
            }
        }
    }

    void setStandbyMode(boolean isStandbyModeOn) {
        assertRunOnServiceThread();
        if (isPowerOnOrTransient() && isStandbyModeOn) {
            this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 5, 0);
            if (playback() != null) {
                playback().sendStandby(0);
            }
        } else if (isPowerStandbyOrTransient() && (isStandbyModeOn ^ 1) != 0) {
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server.hdmi:WAKE");
            if (playback() != null) {
                oneTouchPlay(new IHdmiControlCallback.Stub() {
                    public void onComplete(int result) {
                        if (result != 0) {
                            Slog.w(HdmiControlService.TAG, "Failed to complete 'one touch play'. result=" + result);
                        }
                    }
                });
            }
        }
    }

    boolean isProhibitMode() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mProhibitMode;
        }
        return z;
    }

    void setProhibitMode(boolean enabled) {
        synchronized (this.mLock) {
            this.mProhibitMode = enabled;
        }
    }

    @ServiceThreadOnly
    void setCecOption(int key, boolean value) {
        assertRunOnServiceThread();
        this.mCecController.setOption(key, value);
    }

    @ServiceThreadOnly
    void setControlEnabled(boolean enabled) {
        assertRunOnServiceThread();
        synchronized (this.mLock) {
            this.mHdmiControlEnabled = enabled;
        }
        if (enabled) {
            enableHdmiControlService();
            return;
        }
        invokeVendorCommandListenersOnControlStateChanged(false, 1);
        runOnServiceThread(new Runnable() {
            public void run() {
                HdmiControlService.this.disableHdmiControlService();
            }
        });
    }

    @ServiceThreadOnly
    private void enableHdmiControlService() {
        this.mCecController.setOption(3, true);
        this.mMhlController.setOption(103, 1);
        initializeCec(0);
    }

    @ServiceThreadOnly
    private void disableHdmiControlService() {
        disableDevices(new PendingActionClearedCallback() {
            public void onCleared(HdmiCecLocalDevice device) {
                HdmiControlService.this.assertRunOnServiceThread();
                HdmiControlService.this.mCecController.flush(new Runnable() {
                    public void run() {
                        HdmiControlService.this.mCecController.setOption(2, false);
                        HdmiControlService.this.mMhlController.setOption(103, 0);
                        HdmiControlService.this.clearLocalDevices();
                    }
                });
            }
        });
    }

    @ServiceThreadOnly
    void setActivePortId(int portId) {
        assertRunOnServiceThread();
        this.mActivePortId = portId;
        setLastInputForMhl(-1);
    }

    @ServiceThreadOnly
    void setLastInputForMhl(int portId) {
        assertRunOnServiceThread();
        this.mLastInputMhl = portId;
    }

    @ServiceThreadOnly
    int getLastInputForMhl() {
        assertRunOnServiceThread();
        return this.mLastInputMhl;
    }

    @ServiceThreadOnly
    void changeInputForMhl(int portId, boolean contentOn) {
        assertRunOnServiceThread();
        if (tv() != null) {
            HdmiDeviceInfo info;
            final int lastInput = contentOn ? tv().getActivePortId() : -1;
            if (portId != -1) {
                tv().doManualPortSwitching(portId, new IHdmiControlCallback.Stub() {
                    public void onComplete(int result) throws RemoteException {
                        HdmiControlService.this.setLastInputForMhl(lastInput);
                    }
                });
            }
            tv().setActivePortId(portId);
            HdmiMhlLocalDeviceStub device = this.mMhlController.getLocalDevice(portId);
            if (device != null) {
                info = device.getInfo();
            } else {
                info = (HdmiDeviceInfo) this.mPortDeviceMap.get(portId, HdmiDeviceInfo.INACTIVE_DEVICE);
            }
            invokeInputChangeListener(info);
        }
    }

    void setMhlInputChangeEnabled(boolean enabled) {
        this.mMhlController.setOption(101, toInt(enabled));
        synchronized (this.mLock) {
            this.mMhlInputChangeEnabled = enabled;
        }
    }

    boolean isMhlInputChangeEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mMhlInputChangeEnabled;
        }
        return z;
    }

    @ServiceThreadOnly
    void displayOsd(int messageId) {
        assertRunOnServiceThread();
        Intent intent = new Intent("android.hardware.hdmi.action.OSD_MESSAGE");
        intent.putExtra("android.hardware.hdmi.extra.MESSAGE_ID", messageId);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION);
    }

    @ServiceThreadOnly
    void displayOsd(int messageId, int extra) {
        assertRunOnServiceThread();
        Intent intent = new Intent("android.hardware.hdmi.action.OSD_MESSAGE");
        intent.putExtra("android.hardware.hdmi.extra.MESSAGE_ID", messageId);
        intent.putExtra("android.hardware.hdmi.extra.MESSAGE_EXTRA_PARAM1", extra);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION);
    }
}
