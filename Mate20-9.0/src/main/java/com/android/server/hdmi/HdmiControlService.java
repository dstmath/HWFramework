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
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.hdmi.IHdmiDeviceEventListener;
import android.hardware.hdmi.IHdmiHotplugEventListener;
import android.hardware.hdmi.IHdmiInputChangeListener;
import android.hardware.hdmi.IHdmiMhlVendorCommandListener;
import android.hardware.hdmi.IHdmiRecordListener;
import android.hardware.hdmi.IHdmiSystemAudioModeChangeListener;
import android.hardware.hdmi.IHdmiVendorCommandListener;
import android.media.AudioManager;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.SystemService;
import com.android.server.hdmi.HdmiAnnotations;
import com.android.server.hdmi.HdmiCecController;
import com.android.server.hdmi.HdmiCecLocalDevice;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
    /* access modifiers changed from: private */
    public final Locale HONG_KONG = new Locale("zh", "HK");
    /* access modifiers changed from: private */
    public final Locale MACAU = new Locale("zh", "MO");
    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public int mActivePortId = -1;
    /* access modifiers changed from: private */
    public boolean mAddressAllocated = false;
    /* access modifiers changed from: private */
    public HdmiCecController mCecController;
    /* access modifiers changed from: private */
    public final CecMessageBuffer mCecMessageBuffer = new CecMessageBuffer();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayList<DeviceEventListenerRecord> mDeviceEventListenerRecords = new ArrayList<>();
    private final Handler mHandler = new Handler();
    private final HdmiControlBroadcastReceiver mHdmiControlBroadcastReceiver = new HdmiControlBroadcastReceiver();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public boolean mHdmiControlEnabled;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayList<HotplugEventListenerRecord> mHotplugEventListenerRecords = new ArrayList<>();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public InputChangeListenerRecord mInputChangeListenerRecord;
    private final HandlerThread mIoThread = new HandlerThread("Hdmi Control Io Thread");
    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public String mLanguage = Locale.getDefault().getISO3Language();
    @HdmiAnnotations.ServiceThreadOnly
    private int mLastInputMhl = -1;
    /* access modifiers changed from: private */
    public final List<Integer> mLocalDevices = getIntList(SystemProperties.get("ro.hdmi.device_type"));
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private HdmiCecMessageValidator mMessageValidator;
    /* access modifiers changed from: private */
    public HdmiMhlControllerStub mMhlController;
    @GuardedBy("mLock")
    private List<HdmiDeviceInfo> mMhlDevices;
    @GuardedBy("mLock")
    private boolean mMhlInputChangeEnabled;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayList<HdmiMhlVendorCommandListenerRecord> mMhlVendorCommandListenerRecords = new ArrayList<>();
    private UnmodifiableSparseArray<HdmiDeviceInfo> mPortDeviceMap;
    private UnmodifiableSparseIntArray mPortIdMap;
    /* access modifiers changed from: private */
    public List<HdmiPortInfo> mPortInfo;
    private UnmodifiableSparseArray<HdmiPortInfo> mPortInfoMap;
    private PowerManager mPowerManager;
    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public int mPowerStatus = 1;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public boolean mProhibitMode;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public HdmiRecordListenerRecord mRecordListenerRecord;
    /* access modifiers changed from: private */
    public final SelectRequestBuffer mSelectRequestBuffer = new SelectRequestBuffer();
    private final SettingsObserver mSettingsObserver = new SettingsObserver(this.mHandler);
    @HdmiAnnotations.ServiceThreadOnly
    private boolean mStandbyMessageReceived = false;
    /* access modifiers changed from: private */
    public final ArrayList<SystemAudioModeChangeListenerRecord> mSystemAudioModeChangeListenerRecords = new ArrayList<>();
    private TvInputManager mTvInputManager;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayList<VendorCommandListenerRecord> mVendorCommandListenerRecords = new ArrayList<>();
    @HdmiAnnotations.ServiceThreadOnly
    private boolean mWakeUpMessageReceived = false;

    private final class BinderService extends IHdmiControlService.Stub {
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
            HdmiCecLocalDevice.ActiveSource activeSource = tv.getActiveSource();
            if (activeSource.isValid()) {
                HdmiDeviceInfo hdmiDeviceInfo = new HdmiDeviceInfo(activeSource.logicalAddress, activeSource.physicalAddress, -1, -1, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                return hdmiDeviceInfo;
            }
            int activePath = tv.getActivePath();
            if (activePath == 65535) {
                return null;
            }
            HdmiDeviceInfo info = tv.getSafeDeviceInfoByPath(activePath);
            return info != null ? info : new HdmiDeviceInfo(activePath, tv.getActivePortId());
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
                    } else if (!HdmiControlService.this.mAddressAllocated) {
                        HdmiControlService.this.mSelectRequestBuffer.set(SelectRequestBuffer.newDeviceSelect(HdmiControlService.this, deviceId, callback));
                    } else {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                        HdmiControlService.this.invokeCallback(callback, 2);
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
                    } else if (!HdmiControlService.this.mAddressAllocated) {
                        HdmiControlService.this.mSelectRequestBuffer.set(SelectRequestBuffer.newPortSelect(HdmiControlService.this, portId, callback));
                    } else {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                        HdmiControlService.this.invokeCallback(callback, 2);
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
            List<HdmiDeviceInfo> cecDevices;
            List<T> mergeToUnmodifiableList;
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            synchronized (HdmiControlService.this.mLock) {
                if (tv == null) {
                    try {
                        cecDevices = Collections.emptyList();
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    cecDevices = tv.getSafeExternalInputsLocked();
                }
                mergeToUnmodifiableList = HdmiUtils.mergeToUnmodifiableList(cecDevices, HdmiControlService.this.getMhlDevicesLocked());
            }
            return mergeToUnmodifiableList;
        }

        public List<HdmiDeviceInfo> getDeviceList() {
            List<HdmiDeviceInfo> list;
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            synchronized (HdmiControlService.this.mLock) {
                if (tv == null) {
                    try {
                        list = Collections.emptyList();
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    list = tv.getSafeCecDevicesLocked();
                }
            }
            return list;
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
            HdmiControlService hdmiControlService = HdmiControlService.this;
            final int i = deviceType;
            final boolean z = hasVendorId;
            final int i2 = targetAddress;
            final byte[] bArr = params;
            AnonymousClass10 r1 = new Runnable() {
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
            };
            hdmiControlService.runOnServiceThread(r1);
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
                    if (!HdmiControlService.this.isTvDeviceEnabled()) {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    } else {
                        HdmiControlService.this.tv().startOneTouchRecord(recorderAddress, recordSource);
                    }
                }
            });
        }

        public void stopOneTouchRecord(final int recorderAddress) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (!HdmiControlService.this.isTvDeviceEnabled()) {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    } else {
                        HdmiControlService.this.tv().stopOneTouchRecord(recorderAddress);
                    }
                }
            });
        }

        public void startTimerRecording(final int recorderAddress, final int sourceType, final byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (!HdmiControlService.this.isTvDeviceEnabled()) {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    } else {
                        HdmiControlService.this.tv().startTimerRecording(recorderAddress, sourceType, recordSource);
                    }
                }
            });
        }

        public void clearTimerRecording(final int recorderAddress, final int sourceType, final byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                public void run() {
                    if (!HdmiControlService.this.isTvDeviceEnabled()) {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    } else {
                        HdmiControlService.this.tv().clearTimerRecording(recorderAddress, sourceType, recordSource);
                    }
                }
            });
        }

        public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService hdmiControlService = HdmiControlService.this;
            final int i = portId;
            final int i2 = offset;
            final int i3 = length;
            final byte[] bArr = data;
            AnonymousClass16 r1 = new Runnable() {
                public void run() {
                    if (!HdmiControlService.this.isControlEnabled()) {
                        Slog.w(HdmiControlService.TAG, "Hdmi control is disabled.");
                    } else if (HdmiControlService.this.mMhlController.getLocalDevice(i) == null) {
                        Slog.w(HdmiControlService.TAG, "Invalid port id:" + i);
                    } else {
                        HdmiControlService.this.mMhlController.sendVendorCommand(i, i2, i3, bArr);
                    }
                }
            };
            hdmiControlService.runOnServiceThread(r1);
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

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
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
                Iterator it = HdmiControlService.this.mPortInfo.iterator();
                while (it.hasNext()) {
                    pw.println("- " + ((HdmiPortInfo) it.next()));
                }
                pw.decreaseIndent();
                pw.println("mPowerStatus: " + HdmiControlService.this.mPowerStatus);
            }
        }
    }

    private final class CecMessageBuffer {
        private List<HdmiCecMessage> mBuffer;

        private CecMessageBuffer() {
            this.mBuffer = new ArrayList();
        }

        public void bufferMessage(HdmiCecMessage message) {
            int opcode = message.getOpcode();
            if (opcode == 4 || opcode == 13) {
                bufferImageOrTextViewOn(message);
            } else if (opcode == 130) {
                bufferActiveSource(message);
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
            if (!replaceMessageIfBuffered(message, 4) && !replaceMessageIfBuffered(message, 13)) {
                this.mBuffer.add(message);
            }
        }

        private boolean replaceMessageIfBuffered(HdmiCecMessage message, int opcode) {
            for (int i = 0; i < this.mBuffer.size(); i++) {
                if (this.mBuffer.get(i).getOpcode() == opcode) {
                    this.mBuffer.set(i, message);
                    return true;
                }
            }
            return false;
        }
    }

    private final class DeviceEventListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IHdmiDeviceEventListener mListener;

        public DeviceEventListenerRecord(IHdmiDeviceEventListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mDeviceEventListenerRecords.remove(this);
            }
        }
    }

    interface DevicePollingCallback {
        void onPollingFinished(List<Integer> list);
    }

    private class HdmiControlBroadcastReceiver extends BroadcastReceiver {
        private HdmiControlBroadcastReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x0051  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x005f  */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0075  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0083  */
        /* JADX WARNING: Removed duplicated region for block: B:42:? A[RETURN, SYNTHETIC] */
        @HdmiAnnotations.ServiceThreadOnly
        public void onReceive(Context context, Intent intent) {
            char c;
            HdmiControlService.this.assertRunOnServiceThread();
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode == -2128145023) {
                if (action.equals("android.intent.action.SCREEN_OFF")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == -1454123155) {
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 158859398) {
                if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 1947666138 && action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                c = 3;
                switch (c) {
                    case 0:
                        if (HdmiControlService.this.isPowerOnOrTransient()) {
                            HdmiControlService.this.onStandby(0);
                            return;
                        }
                        return;
                    case 1:
                        if (HdmiControlService.this.isPowerStandbyOrTransient()) {
                            HdmiControlService.this.onWakeUp();
                            return;
                        }
                        return;
                    case 2:
                        String language = getMenuLanguage();
                        if (!HdmiControlService.this.mLanguage.equals(language)) {
                            HdmiControlService.this.onLanguageChanged(language);
                            return;
                        }
                        return;
                    case 3:
                        if (HdmiControlService.this.isPowerOnOrTransient()) {
                            HdmiControlService.this.onStandby(1);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
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

    private class HdmiMhlVendorCommandListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IHdmiMhlVendorCommandListener mListener;

        public HdmiMhlVendorCommandListenerRecord(IHdmiMhlVendorCommandListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            HdmiControlService.this.mMhlVendorCommandListenerRecords.remove(this);
        }
    }

    private class HdmiRecordListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IHdmiRecordListener mListener;

        public HdmiRecordListenerRecord(IHdmiRecordListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                if (HdmiControlService.this.mRecordListenerRecord == this) {
                    HdmiRecordListenerRecord unused = HdmiControlService.this.mRecordListenerRecord = null;
                }
            }
        }
    }

    private final class HotplugEventListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IHdmiHotplugEventListener mListener;

        public HotplugEventListenerRecord(IHdmiHotplugEventListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mHotplugEventListenerRecords.remove(this);
            }
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof HotplugEventListenerRecord)) {
                return false;
            }
            boolean z = true;
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

    private final class InputChangeListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IHdmiInputChangeListener mListener;

        public InputChangeListenerRecord(IHdmiInputChangeListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                if (HdmiControlService.this.mInputChangeListenerRecord == this) {
                    InputChangeListenerRecord unused = HdmiControlService.this.mInputChangeListenerRecord = null;
                }
            }
        }
    }

    interface SendMessageCallback {
        void onSendCompleted(int i);
    }

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        public void onChange(boolean selfChange, Uri uri) {
            char c;
            String option = uri.getLastPathSegment();
            boolean enabled = HdmiControlService.this.readBooleanSetting(option, true);
            switch (option.hashCode()) {
                case -2009736264:
                    if (option.equals("hdmi_control_enabled")) {
                        c = 0;
                        break;
                    }
                case -1489007315:
                    if (option.equals("hdmi_system_audio_control_enabled")) {
                        c = 3;
                        break;
                    }
                case -1262529811:
                    if (option.equals("mhl_input_switching_enabled")) {
                        c = 4;
                        break;
                    }
                case -885757826:
                    if (option.equals("mhl_power_charge_enabled")) {
                        c = 5;
                        break;
                    }
                case 726613192:
                    if (option.equals("hdmi_control_auto_wakeup_enabled")) {
                        c = 1;
                        break;
                    }
                case 1628046095:
                    if (option.equals("hdmi_control_auto_device_off_enabled")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HdmiControlService.this.setControlEnabled(enabled);
                    return;
                case 1:
                    if (HdmiControlService.this.isTvDeviceEnabled()) {
                        HdmiControlService.this.tv().setAutoWakeup(enabled);
                    }
                    HdmiControlService.this.setCecOption(1, enabled);
                    return;
                case 2:
                    for (Integer intValue : HdmiControlService.this.mLocalDevices) {
                        HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(intValue.intValue());
                        if (localDevice != null) {
                            localDevice.setAutoDeviceOff(enabled);
                        }
                    }
                    return;
                case 3:
                    if (HdmiControlService.this.isTvDeviceEnabled()) {
                        HdmiControlService.this.tv().setSystemAudioControlFeatureEnabled(enabled);
                        return;
                    }
                    return;
                case 4:
                    HdmiControlService.this.setMhlInputChangeEnabled(enabled);
                    return;
                case 5:
                    HdmiControlService.this.mMhlController.setOption(102, HdmiControlService.toInt(enabled));
                    return;
                default:
                    return;
            }
        }
    }

    private final class SystemAudioModeChangeListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IHdmiSystemAudioModeChangeListener mListener;

        public SystemAudioModeChangeListenerRecord(IHdmiSystemAudioModeChangeListener listener) {
            this.mListener = listener;
        }

        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mSystemAudioModeChangeListenerRecords.remove(this);
            }
        }
    }

    class VendorCommandListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final int mDeviceType;
        /* access modifiers changed from: private */
        public final IHdmiVendorCommandListener mListener;

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
        ArrayList<Integer> list = new ArrayList<>();
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(string);
        Iterator<String> it = splitter.iterator();
        while (it.hasNext()) {
            String item = it.next();
            try {
                list.add(Integer.valueOf(Integer.parseInt(item)));
            } catch (NumberFormatException e) {
                Slog.w(TAG, "Can't parseInt: " + item);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [android.os.IBinder, com.android.server.hdmi.HdmiControlService$BinderService] */
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
            publishBinderService("hdmi_control", new BinderService());
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

    /* access modifiers changed from: package-private */
    public TvInputManager getTvInputManager() {
        return this.mTvInputManager;
    }

    /* access modifiers changed from: package-private */
    public void registerTvInputCallback(TvInputManager.TvInputCallback callback) {
        if (this.mTvInputManager != null) {
            this.mTvInputManager.registerCallback(callback, this.mHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterTvInputCallback(TvInputManager.TvInputCallback callback) {
        if (this.mTvInputManager != null) {
            this.mTvInputManager.unregisterCallback(callback);
        }
    }

    /* access modifiers changed from: package-private */
    public PowerManager getPowerManager() {
        return this.mPowerManager;
    }

    /* access modifiers changed from: private */
    public void onInitializeCecComplete(int initiatedBy) {
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
            resolver.registerContentObserver(Settings.Global.getUriFor(s), false, this.mSettingsObserver, -1);
        }
    }

    /* access modifiers changed from: private */
    public static int toInt(boolean enabled) {
        return enabled;
    }

    /* access modifiers changed from: package-private */
    public boolean readBooleanSetting(String key, boolean defVal) {
        return Settings.Global.getInt(getContext().getContentResolver(), key, toInt(defVal)) == 1;
    }

    /* access modifiers changed from: package-private */
    public void writeBooleanSetting(String key, boolean value) {
        Settings.Global.putInt(getContext().getContentResolver(), key, toInt(value));
    }

    private void initializeCec(int initiatedBy) {
        this.mAddressAllocated = false;
        this.mCecController.setOption(3, true);
        this.mCecController.setLanguage(this.mLanguage);
        initializeLocalDevices(initiatedBy);
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void initializeLocalDevices(int initiatedBy) {
        assertRunOnServiceThread();
        ArrayList<HdmiCecLocalDevice> localDevices = new ArrayList<>();
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

    @HdmiAnnotations.ServiceThreadOnly
    private void allocateLogicalAddress(ArrayList<HdmiCecLocalDevice> allocatingDevices, int initiatedBy) {
        assertRunOnServiceThread();
        this.mCecController.clearLogicalAddress();
        final ArrayList<HdmiCecLocalDevice> allocatedDevices = new ArrayList<>();
        int[] finished = new int[1];
        this.mAddressAllocated = allocatingDevices.isEmpty();
        this.mSelectRequestBuffer.clear();
        Iterator<HdmiCecLocalDevice> it = allocatingDevices.iterator();
        while (it.hasNext()) {
            HdmiCecLocalDevice localDevice = it.next();
            HdmiCecController hdmiCecController = this.mCecController;
            int type = localDevice.getType();
            int preferredAddress = localDevice.getPreferredAddress();
            final HdmiCecLocalDevice hdmiCecLocalDevice = localDevice;
            final ArrayList<HdmiCecLocalDevice> arrayList = allocatingDevices;
            final int[] iArr = finished;
            final int i = initiatedBy;
            AnonymousClass1 r0 = new HdmiCecController.AllocateAddressCallback() {
                public void onAllocated(int deviceType, int logicalAddress) {
                    if (logicalAddress == 15) {
                        Slog.e(HdmiControlService.TAG, "Failed to allocate address:[device_type:" + deviceType + "]");
                    } else {
                        hdmiCecLocalDevice.setDeviceInfo(HdmiControlService.this.createDeviceInfo(logicalAddress, deviceType, 0));
                        HdmiControlService.this.mCecController.addLocalDevice(deviceType, hdmiCecLocalDevice);
                        HdmiControlService.this.mCecController.addLogicalAddress(logicalAddress);
                        allocatedDevices.add(hdmiCecLocalDevice);
                    }
                    int size = arrayList.size();
                    int[] iArr = iArr;
                    int i = iArr[0] + 1;
                    iArr[0] = i;
                    if (size == i) {
                        boolean unused = HdmiControlService.this.mAddressAllocated = true;
                        if (i != 4) {
                            HdmiControlService.this.onInitializeCecComplete(i);
                        }
                        HdmiControlService.this.notifyAddressAllocated(allocatedDevices, i);
                        HdmiControlService.this.mCecMessageBuffer.processMessages();
                    }
                }
            };
            hdmiCecController.allocateLogicalAddress(type, preferredAddress, r0);
        }
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void notifyAddressAllocated(ArrayList<HdmiCecLocalDevice> devices, int initiatedBy) {
        assertRunOnServiceThread();
        Iterator<HdmiCecLocalDevice> it = devices.iterator();
        while (it.hasNext()) {
            HdmiCecLocalDevice device = it.next();
            device.handleAddressAllocated(device.getDeviceInfo().getLogicalAddress(), initiatedBy);
        }
        if (isTvDeviceEnabled()) {
            tv().setSelectRequestBuffer(this.mSelectRequestBuffer);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAddressAllocated() {
        return this.mAddressAllocated;
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void initPortInfo() {
        assertRunOnServiceThread();
        HdmiPortInfo[] cecPortInfo = null;
        if (this.mCecController != null) {
            cecPortInfo = this.mCecController.getPortInfos();
        }
        if (cecPortInfo != null) {
            SparseArray<HdmiPortInfo> portInfoMap = new SparseArray<>();
            SparseIntArray portIdMap = new SparseIntArray();
            SparseArray<HdmiDeviceInfo> portDeviceMap = new SparseArray<>();
            for (HdmiPortInfo info : cecPortInfo) {
                portIdMap.put(info.getAddress(), info.getId());
                portInfoMap.put(info.getId(), info);
                portDeviceMap.put(info.getId(), new HdmiDeviceInfo(info.getAddress(), info.getId()));
            }
            this.mPortIdMap = new UnmodifiableSparseIntArray(portIdMap);
            this.mPortInfoMap = new UnmodifiableSparseArray<>(portInfoMap);
            this.mPortDeviceMap = new UnmodifiableSparseArray<>(portDeviceMap);
            HdmiPortInfo[] mhlPortInfo = this.mMhlController.getPortInfos();
            ArraySet<Integer> mhlSupportedPorts = new ArraySet<>(mhlPortInfo.length);
            for (HdmiPortInfo info2 : mhlPortInfo) {
                if (info2.isMhlSupported()) {
                    mhlSupportedPorts.add(Integer.valueOf(info2.getId()));
                }
            }
            if (mhlSupportedPorts.isEmpty()) {
                this.mPortInfo = Collections.unmodifiableList(Arrays.asList(cecPortInfo));
                return;
            }
            ArrayList<HdmiPortInfo> result = new ArrayList<>(cecPortInfo.length);
            for (HdmiPortInfo info3 : cecPortInfo) {
                if (mhlSupportedPorts.contains(Integer.valueOf(info3.getId()))) {
                    HdmiPortInfo hdmiPortInfo = new HdmiPortInfo(info3.getId(), info3.getType(), info3.getAddress(), info3.isCecSupported(), true, info3.isArcSupported());
                    result.add(hdmiPortInfo);
                } else {
                    result.add(info3);
                }
            }
            this.mPortInfo = Collections.unmodifiableList(result);
        }
    }

    /* access modifiers changed from: package-private */
    public List<HdmiPortInfo> getPortInfo() {
        return this.mPortInfo;
    }

    /* access modifiers changed from: package-private */
    public HdmiPortInfo getPortInfo(int portId) {
        return this.mPortInfoMap.get(portId, null);
    }

    /* access modifiers changed from: package-private */
    public int portIdToPath(int portId) {
        HdmiPortInfo portInfo = getPortInfo(portId);
        if (portInfo != null) {
            return portInfo.getAddress();
        }
        Slog.e(TAG, "Cannot find the port info: " + portId);
        return NetworkConstants.ARP_HWTYPE_RESERVED_HI;
    }

    /* access modifiers changed from: package-private */
    public int pathToPortId(int path) {
        return this.mPortIdMap.get(61440 & path, -1);
    }

    /* access modifiers changed from: package-private */
    public boolean isValidPortId(int portId) {
        return getPortInfo(portId) != null;
    }

    /* access modifiers changed from: package-private */
    public Looper getIoLooper() {
        return this.mIoThread.getLooper();
    }

    /* access modifiers changed from: package-private */
    public Looper getServiceLooper() {
        return this.mHandler.getLooper();
    }

    /* access modifiers changed from: package-private */
    public int getPhysicalAddress() {
        return this.mCecController.getPhysicalAddress();
    }

    /* access modifiers changed from: package-private */
    public int getVendorId() {
        return this.mCecController.getVendorId();
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public HdmiDeviceInfo getDeviceInfo(int logicalAddress) {
        assertRunOnServiceThread();
        if (tv() == null) {
            return null;
        }
        return tv().getCecDeviceInfo(logicalAddress);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public HdmiDeviceInfo getDeviceInfoByPort(int port) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub info = this.mMhlController.getLocalDevice(port);
        if (info != null) {
            return info.getInfo();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getCecVersion() {
        return this.mCecController.getVersion();
    }

    /* access modifiers changed from: package-private */
    public boolean isConnectedToArcPort(int physicalAddress) {
        int portId = pathToPortId(physicalAddress);
        if (portId != -1) {
            return this.mPortInfoMap.get(portId).isArcSupported();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isConnected(int portId) {
        assertRunOnServiceThread();
        return this.mCecController.isConnected(portId);
    }

    /* access modifiers changed from: package-private */
    public void runOnServiceThread(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    /* access modifiers changed from: package-private */
    public void runOnServiceThreadAtFrontOfQueue(Runnable runnable) {
        this.mHandler.postAtFrontOfQueue(runnable);
    }

    /* access modifiers changed from: private */
    public void assertRunOnServiceThread() {
        if (Looper.myLooper() != this.mHandler.getLooper()) {
            throw new IllegalStateException("Should run on service thread.");
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void sendCecCommand(HdmiCecMessage command, SendMessageCallback callback) {
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

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void sendCecCommand(HdmiCecMessage command) {
        assertRunOnServiceThread();
        sendCecCommand(command, null);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void maySendFeatureAbortCommand(HdmiCecMessage command, int reason) {
        assertRunOnServiceThread();
        this.mCecController.maySendFeatureAbortCommand(command, reason);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean handleCecCommand(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!this.mAddressAllocated) {
            this.mCecMessageBuffer.bufferMessage(message);
            return true;
        }
        int errorCode = this.mMessageValidator.isValid(message);
        if (errorCode == 0) {
            return dispatchMessageToLocalDevice(message);
        }
        if (errorCode == 3) {
            maySendFeatureAbortCommand(message, 3);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void enableAudioReturnChannel(int portId, boolean enabled) {
        this.mCecController.enableAudioReturnChannel(portId, enabled);
    }

    @HdmiAnnotations.ServiceThreadOnly
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

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onHotplug(int portId, boolean connected) {
        assertRunOnServiceThread();
        if (connected && !isTvDevice()) {
            ArrayList<HdmiCecLocalDevice> localDevices = new ArrayList<>();
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

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void pollDevices(DevicePollingCallback callback, int sourceAddress, int pickStrategy, int retryCount) {
        assertRunOnServiceThread();
        this.mCecController.pollDevices(callback, sourceAddress, checkPollStrategy(pickStrategy), retryCount);
    }

    private int checkPollStrategy(int pickStrategy) {
        int strategy = pickStrategy & 3;
        if (strategy != 0) {
            int iterationStrategy = 196608 & pickStrategy;
            if (iterationStrategy != 0) {
                return strategy | iterationStrategy;
            }
            throw new IllegalArgumentException("Invalid iteration strategy:" + pickStrategy);
        }
        throw new IllegalArgumentException("Invalid poll strategy:" + pickStrategy);
    }

    /* access modifiers changed from: package-private */
    public List<HdmiCecLocalDevice> getAllLocalDevices() {
        assertRunOnServiceThread();
        return this.mCecController.getLocalDeviceList();
    }

    /* access modifiers changed from: package-private */
    public Object getServiceLock() {
        return this.mLock;
    }

    /* access modifiers changed from: package-private */
    public void setAudioStatus(boolean mute, int volume) {
        if (isTvDeviceEnabled() && tv().isSystemAudioActivated()) {
            AudioManager audioManager = getAudioManager();
            boolean muted = audioManager.isStreamMute(3);
            if (!mute) {
                if (muted) {
                    audioManager.setStreamMute(3, false);
                }
                if (volume >= 0 && volume <= 100) {
                    Slog.i(TAG, "volume: " + volume);
                    audioManager.setStreamVolume(3, volume, 1 | 256);
                }
            } else if (!muted) {
                audioManager.setStreamMute(3, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void announceSystemAudioModeChange(boolean enabled) {
        synchronized (this.mLock) {
            Iterator<SystemAudioModeChangeListenerRecord> it = this.mSystemAudioModeChangeListenerRecords.iterator();
            while (it.hasNext()) {
                invokeSystemAudioModeChangeLocked(it.next().mListener, enabled);
            }
        }
    }

    /* access modifiers changed from: private */
    public HdmiDeviceInfo createDeviceInfo(int logicalAddress, int deviceType, int powerStatus) {
        int i = logicalAddress;
        int i2 = deviceType;
        HdmiDeviceInfo hdmiDeviceInfo = new HdmiDeviceInfo(i, getPhysicalAddress(), pathToPortId(getPhysicalAddress()), i2, getVendorId(), Build.MODEL);
        return hdmiDeviceInfo;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void handleMhlHotplugEvent(int portId, boolean connected) {
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

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void handleMhlBusModeChanged(int portId, int busmode) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub device = this.mMhlController.getLocalDevice(portId);
        if (device != null) {
            device.setBusMode(busmode);
            return;
        }
        Slog.w(TAG, "No mhl device exists for bus mode change[portId:" + portId + ", busmode:" + busmode + "]");
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void handleMhlBusOvercurrent(int portId, boolean on) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub device = this.mMhlController.getLocalDevice(portId);
        if (device != null) {
            device.onBusOvercurrentDetected(on);
            return;
        }
        Slog.w(TAG, "No mhl device exists for bus overcurrent event[portId:" + portId + "]");
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void handleMhlDeviceStatusChanged(int portId, int adopterId, int deviceId) {
        assertRunOnServiceThread();
        HdmiMhlLocalDeviceStub device = this.mMhlController.getLocalDevice(portId);
        if (device != null) {
            device.setDeviceStatusChange(adopterId, deviceId);
            return;
        }
        Slog.w(TAG, "No mhl device exists for device status event[portId:" + portId + ", adopterId:" + adopterId + ", deviceId:" + deviceId + "]");
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void updateSafeMhlInput() {
        assertRunOnServiceThread();
        List<HdmiDeviceInfo> inputs = Collections.emptyList();
        SparseArray<HdmiMhlLocalDeviceStub> devices = this.mMhlController.getAllLocalDevices();
        for (int i = 0; i < devices.size(); i++) {
            HdmiMhlLocalDeviceStub device = devices.valueAt(i);
            if (device.getInfo() != null) {
                if (inputs.isEmpty()) {
                    inputs = new ArrayList<>();
                }
                inputs.add(device.getInfo());
            }
        }
        synchronized (this.mLock) {
            this.mMhlDevices = inputs;
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public List<HdmiDeviceInfo> getMhlDevicesLocked() {
        return this.mMhlDevices;
    }

    /* access modifiers changed from: private */
    public void enforceAccessPermission() {
        getContext().enforceCallingOrSelfPermission(PERMISSION, TAG);
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void oneTouchPlay(IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        HdmiCecLocalDevicePlayback source = playback();
        if (source == null) {
            Slog.w(TAG, "Local playback device not available");
            invokeCallback(callback, 2);
            return;
        }
        source.oneTouchPlay(callback);
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void queryDisplayStatus(IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        HdmiCecLocalDevicePlayback source = playback();
        if (source == null) {
            Slog.w(TAG, "Local playback device not available");
            invokeCallback(callback, 2);
            return;
        }
        source.queryDisplayStatus(callback);
    }

    /* access modifiers changed from: private */
    public void addHotplugEventListener(final IHdmiHotplugEventListener listener) {
        final HotplugEventListenerRecord record = new HotplugEventListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, 0);
            synchronized (this.mLock) {
                this.mHotplugEventListenerRecords.add(record);
            }
            runOnServiceThread(new Runnable() {
                /* JADX WARNING: Code restructure failed: missing block: B:10:0x0026, code lost:
                    if (r0.hasNext() == false) goto L_0x0058;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
                    r1 = (android.hardware.hdmi.HdmiPortInfo) r0.next();
                    r2 = new android.hardware.hdmi.HdmiHotplugEvent(r1.getId(), com.android.server.hdmi.HdmiControlService.access$1100(r6.this$0).isConnected(r1.getId()));
                    r3 = com.android.server.hdmi.HdmiControlService.access$2100(r6.this$0);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:12:0x004b, code lost:
                    monitor-enter(r3);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
                    com.android.server.hdmi.HdmiControlService.access$4700(r6.this$0, r5, r2);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:15:0x0053, code lost:
                    monitor-exit(r3);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:20:0x0058, code lost:
                    return;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:8:0x0018, code lost:
                    r0 = com.android.server.hdmi.HdmiControlService.access$4500(r6.this$0).iterator();
                 */
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

    /* access modifiers changed from: private */
    public void removeHotplugEventListener(IHdmiHotplugEventListener listener) {
        synchronized (this.mLock) {
            Iterator<HotplugEventListenerRecord> it = this.mHotplugEventListenerRecords.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HotplugEventListenerRecord record = it.next();
                if (record.mListener.asBinder() == listener.asBinder()) {
                    listener.asBinder().unlinkToDeath(record, 0);
                    this.mHotplugEventListenerRecords.remove(record);
                    break;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void addDeviceEventListener(IHdmiDeviceEventListener listener) {
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

    /* access modifiers changed from: package-private */
    public void invokeDeviceEventListeners(HdmiDeviceInfo device, int status) {
        synchronized (this.mLock) {
            Iterator<DeviceEventListenerRecord> it = this.mDeviceEventListenerRecords.iterator();
            while (it.hasNext()) {
                try {
                    it.next().mListener.onStatusChanged(device, status);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to report device event:" + e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void addSystemAudioModeChangeListner(IHdmiSystemAudioModeChangeListener listener) {
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

    /* access modifiers changed from: private */
    public void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {
        synchronized (this.mLock) {
            Iterator<SystemAudioModeChangeListenerRecord> it = this.mSystemAudioModeChangeListenerRecords.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                SystemAudioModeChangeListenerRecord record = it.next();
                if (record.mListener.asBinder() == listener) {
                    listener.asBinder().unlinkToDeath(record, 0);
                    this.mSystemAudioModeChangeListenerRecords.remove(record);
                    break;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setInputChangeListener(IHdmiInputChangeListener listener) {
        synchronized (this.mLock) {
            this.mInputChangeListenerRecord = new InputChangeListenerRecord(listener);
            try {
                listener.asBinder().linkToDeath(this.mInputChangeListenerRecord, 0);
            } catch (RemoteException e) {
                Slog.w(TAG, "Listener already died");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeInputChangeListener(HdmiDeviceInfo info) {
        synchronized (this.mLock) {
            if (this.mInputChangeListenerRecord != null) {
                try {
                    this.mInputChangeListenerRecord.mListener.onChanged(info);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception thrown by IHdmiInputChangeListener: " + e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setHdmiRecordListener(IHdmiRecordListener listener) {
        synchronized (this.mLock) {
            this.mRecordListenerRecord = new HdmiRecordListenerRecord(listener);
            try {
                listener.asBinder().linkToDeath(this.mRecordListenerRecord, 0);
            } catch (RemoteException e) {
                Slog.w(TAG, "Listener already died.", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] invokeRecordRequestListener(int recorderAddress) {
        synchronized (this.mLock) {
            if (this.mRecordListenerRecord != null) {
                try {
                    byte[] oneTouchRecordSource = this.mRecordListenerRecord.mListener.getOneTouchRecordSource(recorderAddress);
                    return oneTouchRecordSource;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to start record.", e);
                }
            }
            byte[] bArr = EmptyArray.BYTE;
            return bArr;
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeOneTouchRecordResult(int recorderAddress, int result) {
        synchronized (this.mLock) {
            if (this.mRecordListenerRecord != null) {
                try {
                    this.mRecordListenerRecord.mListener.onOneTouchRecordResult(recorderAddress, result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onOneTouchRecordResult.", e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeTimerRecordingResult(int recorderAddress, int result) {
        synchronized (this.mLock) {
            if (this.mRecordListenerRecord != null) {
                try {
                    this.mRecordListenerRecord.mListener.onTimerRecordingResult(recorderAddress, result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onTimerRecordingResult.", e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeClearTimerRecordingResult(int recorderAddress, int result) {
        synchronized (this.mLock) {
            if (this.mRecordListenerRecord != null) {
                try {
                    this.mRecordListenerRecord.mListener.onClearTimerRecordingResult(recorderAddress, result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onClearTimerRecordingResult.", e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void invokeCallback(IHdmiControlCallback callback, int result) {
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
            Iterator<HotplugEventListenerRecord> it = this.mHotplugEventListenerRecords.iterator();
            while (it.hasNext()) {
                invokeHotplugEventListenerLocked(it.next().mListener, event);
            }
        }
    }

    /* access modifiers changed from: private */
    public void invokeHotplugEventListenerLocked(IHdmiHotplugEventListener listener, HdmiHotplugEvent event) {
        try {
            listener.onReceived(event);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to report hotplug event:" + event.toString(), e);
        }
    }

    public HdmiCecLocalDeviceTv tv() {
        return (HdmiCecLocalDeviceTv) this.mCecController.getLocalDevice(0);
    }

    /* access modifiers changed from: package-private */
    public boolean isTvDevice() {
        return this.mLocalDevices.contains(0);
    }

    /* access modifiers changed from: package-private */
    public boolean isTvDeviceEnabled() {
        return isTvDevice() && tv() != null;
    }

    private HdmiCecLocalDevicePlayback playback() {
        return (HdmiCecLocalDevicePlayback) this.mCecController.getLocalDevice(4);
    }

    /* access modifiers changed from: package-private */
    public AudioManager getAudioManager() {
        return (AudioManager) getContext().getSystemService("audio");
    }

    /* access modifiers changed from: package-private */
    public boolean isControlEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mHdmiControlEnabled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int getPowerStatus() {
        assertRunOnServiceThread();
        return this.mPowerStatus;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isPowerOnOrTransient() {
        assertRunOnServiceThread();
        return this.mPowerStatus == 0 || this.mPowerStatus == 2;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isPowerStandbyOrTransient() {
        assertRunOnServiceThread();
        return this.mPowerStatus == 1 || this.mPowerStatus == 3;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isPowerStandby() {
        assertRunOnServiceThread();
        return this.mPowerStatus == 1;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void wakeUp() {
        assertRunOnServiceThread();
        this.mWakeUpMessageReceived = true;
        this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server.hdmi:WAKE");
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void standby() {
        assertRunOnServiceThread();
        if (canGoToStandby()) {
            this.mStandbyMessageReceived = true;
            this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 5, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isWakeUpMessageReceived() {
        return this.mWakeUpMessageReceived;
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onWakeUp() {
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

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onStandby(final int standbyAction) {
        assertRunOnServiceThread();
        this.mPowerStatus = 3;
        invokeVendorCommandListenersOnControlStateChanged(false, 3);
        if (!canGoToStandby()) {
            this.mPowerStatus = 1;
            return;
        }
        final List<HdmiCecLocalDevice> devices = getAllLocalDevices();
        disableDevices(new HdmiCecLocalDevice.PendingActionClearedCallback() {
            public void onCleared(HdmiCecLocalDevice device) {
                Slog.v(HdmiControlService.TAG, "On standby-action cleared:" + device.mDeviceType);
                devices.remove(device);
                if (devices.isEmpty()) {
                    HdmiControlService.this.onStandbyCompleted(standbyAction);
                }
            }
        });
    }

    private boolean canGoToStandby() {
        for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
            if (!device.canGoToStandby()) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onLanguageChanged(String language) {
        assertRunOnServiceThread();
        this.mLanguage = language;
        if (isTvDeviceEnabled()) {
            tv().broadcastMenuLanguage(language);
            this.mCecController.setLanguage(language);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public String getLanguage() {
        assertRunOnServiceThread();
        return this.mLanguage;
    }

    private void disableDevices(HdmiCecLocalDevice.PendingActionClearedCallback callback) {
        if (this.mCecController != null) {
            for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
                device.disableDevice(this.mStandbyMessageReceived, callback);
            }
        }
        this.mMhlController.clearAllLocalDevices();
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void clearLocalDevices() {
        assertRunOnServiceThread();
        if (this.mCecController != null) {
            this.mCecController.clearLogicalAddress();
            this.mCecController.clearLocalDevices();
        }
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void onStandbyCompleted(int standbyAction) {
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

    /* access modifiers changed from: private */
    public void addVendorCommandListener(IHdmiVendorCommandListener listener, int deviceType) {
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

    /* access modifiers changed from: package-private */
    public boolean invokeVendorCommandListenersOnReceived(int deviceType, int srcAddress, int destAddress, byte[] params, boolean hasVendorId) {
        synchronized (this.mLock) {
            if (this.mVendorCommandListenerRecords.isEmpty()) {
                return false;
            }
            Iterator<VendorCommandListenerRecord> it = this.mVendorCommandListenerRecords.iterator();
            while (it.hasNext()) {
                VendorCommandListenerRecord record = it.next();
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

    /* access modifiers changed from: package-private */
    public boolean invokeVendorCommandListenersOnControlStateChanged(boolean enabled, int reason) {
        synchronized (this.mLock) {
            if (this.mVendorCommandListenerRecords.isEmpty()) {
                return false;
            }
            Iterator<VendorCommandListenerRecord> it = this.mVendorCommandListenerRecords.iterator();
            while (it.hasNext()) {
                try {
                    it.next().mListener.onControlStateChanged(enabled, reason);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to notify control-state-changed to vendor handler", e);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) {
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

    /* access modifiers changed from: package-private */
    public void invokeMhlVendorCommandListeners(int portId, int offest, int length, byte[] data) {
        synchronized (this.mLock) {
            Iterator<HdmiMhlVendorCommandListenerRecord> it = this.mMhlVendorCommandListenerRecords.iterator();
            while (it.hasNext()) {
                try {
                    it.next().mListener.onReceived(portId, offest, length, data);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to notify MHL vendor command", e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setStandbyMode(boolean isStandbyModeOn) {
        assertRunOnServiceThread();
        if (isPowerOnOrTransient() && isStandbyModeOn) {
            this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 5, 0);
            if (playback() != null) {
                playback().sendStandby(0);
            }
        } else if (isPowerStandbyOrTransient() && !isStandbyModeOn) {
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

    /* access modifiers changed from: package-private */
    public boolean isProhibitMode() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mProhibitMode;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void setProhibitMode(boolean enabled) {
        synchronized (this.mLock) {
            this.mProhibitMode = enabled;
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setCecOption(int key, boolean value) {
        assertRunOnServiceThread();
        this.mCecController.setOption(key, value);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setControlEnabled(boolean enabled) {
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

    @HdmiAnnotations.ServiceThreadOnly
    private void enableHdmiControlService() {
        this.mCecController.setOption(3, true);
        this.mMhlController.setOption(103, 1);
        initializeCec(0);
    }

    /* access modifiers changed from: private */
    @HdmiAnnotations.ServiceThreadOnly
    public void disableHdmiControlService() {
        disableDevices(new HdmiCecLocalDevice.PendingActionClearedCallback() {
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

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setActivePortId(int portId) {
        assertRunOnServiceThread();
        this.mActivePortId = portId;
        setLastInputForMhl(-1);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setLastInputForMhl(int portId) {
        assertRunOnServiceThread();
        this.mLastInputMhl = portId;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int getLastInputForMhl() {
        assertRunOnServiceThread();
        return this.mLastInputMhl;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void changeInputForMhl(int portId, boolean contentOn) {
        HdmiDeviceInfo info;
        assertRunOnServiceThread();
        if (tv() != null) {
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
                info = this.mPortDeviceMap.get(portId, HdmiDeviceInfo.INACTIVE_DEVICE);
            }
            invokeInputChangeListener(info);
        }
    }

    /* access modifiers changed from: package-private */
    public void setMhlInputChangeEnabled(boolean enabled) {
        this.mMhlController.setOption(101, toInt(enabled));
        synchronized (this.mLock) {
            this.mMhlInputChangeEnabled = enabled;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isMhlInputChangeEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mMhlInputChangeEnabled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void displayOsd(int messageId) {
        assertRunOnServiceThread();
        Intent intent = new Intent("android.hardware.hdmi.action.OSD_MESSAGE");
        intent.putExtra("android.hardware.hdmi.extra.MESSAGE_ID", messageId);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void displayOsd(int messageId, int extra) {
        assertRunOnServiceThread();
        Intent intent = new Intent("android.hardware.hdmi.action.OSD_MESSAGE");
        intent.putExtra("android.hardware.hdmi.extra.MESSAGE_ID", messageId);
        intent.putExtra("android.hardware.hdmi.extra.MESSAGE_EXTRA_PARAM1", extra);
        getContext().sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION);
    }
}
