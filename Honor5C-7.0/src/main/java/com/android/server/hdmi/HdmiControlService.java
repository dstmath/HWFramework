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
import android.hardware.hdmi.IHdmiControlCallback.Stub;
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
import android.media.tv.TvInputManager.TvInputCallback;
import android.net.Uri;
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
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.SystemService;
import com.android.server.hdmi.HdmiAnnotations.ServiceThreadOnly;
import com.android.server.wm.WindowManagerService.H;
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
    private final Locale HONG_KONG;
    private final Locale MACAU;
    @ServiceThreadOnly
    private int mActivePortId;
    private boolean mAddressAllocated;
    private HdmiCecController mCecController;
    private final CecMessageBuffer mCecMessageBuffer;
    @GuardedBy("mLock")
    private final ArrayList<DeviceEventListenerRecord> mDeviceEventListenerRecords;
    private final Handler mHandler;
    private final HdmiControlBroadcastReceiver mHdmiControlBroadcastReceiver;
    @GuardedBy("mLock")
    private boolean mHdmiControlEnabled;
    @GuardedBy("mLock")
    private final ArrayList<HotplugEventListenerRecord> mHotplugEventListenerRecords;
    @GuardedBy("mLock")
    private InputChangeListenerRecord mInputChangeListenerRecord;
    private final HandlerThread mIoThread;
    @ServiceThreadOnly
    private String mLanguage;
    @ServiceThreadOnly
    private int mLastInputMhl;
    private final List<Integer> mLocalDevices;
    private final Object mLock;
    private HdmiCecMessageValidator mMessageValidator;
    private HdmiMhlControllerStub mMhlController;
    @GuardedBy("mLock")
    private List<HdmiDeviceInfo> mMhlDevices;
    @GuardedBy("mLock")
    private boolean mMhlInputChangeEnabled;
    @GuardedBy("mLock")
    private final ArrayList<HdmiMhlVendorCommandListenerRecord> mMhlVendorCommandListenerRecords;
    private UnmodifiableSparseArray<HdmiDeviceInfo> mPortDeviceMap;
    private UnmodifiableSparseIntArray mPortIdMap;
    private List<HdmiPortInfo> mPortInfo;
    private UnmodifiableSparseArray<HdmiPortInfo> mPortInfoMap;
    private PowerManager mPowerManager;
    @ServiceThreadOnly
    private int mPowerStatus;
    @GuardedBy("mLock")
    private boolean mProhibitMode;
    @GuardedBy("mLock")
    private HdmiRecordListenerRecord mRecordListenerRecord;
    private final SelectRequestBuffer mSelectRequestBuffer;
    private final SettingsObserver mSettingsObserver;
    @ServiceThreadOnly
    private boolean mStandbyMessageReceived;
    private final ArrayList<SystemAudioModeChangeListenerRecord> mSystemAudioModeChangeListenerRecords;
    private TvInputManager mTvInputManager;
    @GuardedBy("mLock")
    private final ArrayList<VendorCommandListenerRecord> mVendorCommandListenerRecords;
    @ServiceThreadOnly
    private boolean mWakeUpMessageReceived;

    interface DevicePollingCallback {
        void onPollingFinished(List<Integer> list);
    }

    interface SendMessageCallback {
        void onSendCompleted(int i);
    }

    /* renamed from: com.android.server.hdmi.HdmiControlService.1 */
    class AnonymousClass1 implements AllocateAddressCallback {
        final /* synthetic */ ArrayList val$allocatedDevices;
        final /* synthetic */ ArrayList val$allocatingDevices;
        final /* synthetic */ int[] val$finished;
        final /* synthetic */ int val$initiatedBy;
        final /* synthetic */ HdmiCecLocalDevice val$localDevice;

        AnonymousClass1(HdmiCecLocalDevice val$localDevice, ArrayList val$allocatedDevices, ArrayList val$allocatingDevices, int[] val$finished, int val$initiatedBy) {
            this.val$localDevice = val$localDevice;
            this.val$allocatedDevices = val$allocatedDevices;
            this.val$allocatingDevices = val$allocatingDevices;
            this.val$finished = val$finished;
            this.val$initiatedBy = val$initiatedBy;
        }

        public void onAllocated(int deviceType, int logicalAddress) {
            if (logicalAddress == 15) {
                Slog.e(HdmiControlService.TAG, "Failed to allocate address:[device_type:" + deviceType + "]");
            } else {
                this.val$localDevice.setDeviceInfo(HdmiControlService.this.createDeviceInfo(logicalAddress, deviceType, HdmiControlService.STANDBY_SCREEN_OFF));
                HdmiControlService.this.mCecController.addLocalDevice(deviceType, this.val$localDevice);
                HdmiControlService.this.mCecController.addLogicalAddress(logicalAddress);
                this.val$allocatedDevices.add(this.val$localDevice);
            }
            int size = this.val$allocatingDevices.size();
            int[] iArr = this.val$finished;
            int i = iArr[HdmiControlService.STANDBY_SCREEN_OFF] + HdmiControlService.STANDBY_SHUTDOWN;
            iArr[HdmiControlService.STANDBY_SCREEN_OFF] = i;
            if (size == i) {
                HdmiControlService.this.mAddressAllocated = true;
                if (this.val$initiatedBy != HdmiControlService.INITIATED_BY_HOTPLUG) {
                    HdmiControlService.this.onInitializeCecComplete(this.val$initiatedBy);
                }
                HdmiControlService.this.notifyAddressAllocated(this.val$allocatedDevices, this.val$initiatedBy);
                HdmiControlService.this.mCecMessageBuffer.processMessages();
            }
        }
    }

    /* renamed from: com.android.server.hdmi.HdmiControlService.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ IHdmiHotplugEventListener val$listener;
        final /* synthetic */ HotplugEventListenerRecord val$record;

        AnonymousClass2(HotplugEventListenerRecord val$record, IHdmiHotplugEventListener val$listener) {
            this.val$record = val$record;
            this.val$listener = val$listener;
        }

        public void run() {
            synchronized (HdmiControlService.this.mLock) {
                if (HdmiControlService.this.mHotplugEventListenerRecords.contains(this.val$record)) {
                    for (HdmiPortInfo port : HdmiControlService.this.mPortInfo) {
                        HdmiHotplugEvent event = new HdmiHotplugEvent(port.getId(), HdmiControlService.this.mCecController.isConnected(port.getId()));
                        synchronized (HdmiControlService.this.mLock) {
                            HdmiControlService.this.invokeHotplugEventListenerLocked(this.val$listener, event);
                        }
                    }
                    return;
                }
            }
        }
    }

    /* renamed from: com.android.server.hdmi.HdmiControlService.3 */
    class AnonymousClass3 implements PendingActionClearedCallback {
        final /* synthetic */ List val$devices;
        final /* synthetic */ int val$standbyAction;

        AnonymousClass3(List val$devices, int val$standbyAction) {
            this.val$devices = val$devices;
            this.val$standbyAction = val$standbyAction;
        }

        public void onCleared(HdmiCecLocalDevice device) {
            Slog.v(HdmiControlService.TAG, "On standby-action cleared:" + device.mDeviceType);
            this.val$devices.remove(device);
            if (this.val$devices.isEmpty()) {
                HdmiControlService.this.onStandbyCompleted(this.val$standbyAction);
            }
        }
    }

    /* renamed from: com.android.server.hdmi.HdmiControlService.6 */
    class AnonymousClass6 extends Stub {
        final /* synthetic */ int val$lastInput;

        AnonymousClass6(int val$lastInput) {
            this.val$lastInput = val$lastInput;
        }

        public void onComplete(int result) throws RemoteException {
            HdmiControlService.this.setLastInputForMhl(this.val$lastInput);
        }
    }

    private final class BinderService extends IHdmiControlService.Stub {

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.10 */
        class AnonymousClass10 implements Runnable {
            final /* synthetic */ int val$deviceType;
            final /* synthetic */ boolean val$hasVendorId;
            final /* synthetic */ byte[] val$params;
            final /* synthetic */ int val$targetAddress;

            AnonymousClass10(int val$deviceType, boolean val$hasVendorId, int val$targetAddress, byte[] val$params) {
                this.val$deviceType = val$deviceType;
                this.val$hasVendorId = val$hasVendorId;
                this.val$targetAddress = val$targetAddress;
                this.val$params = val$params;
            }

            public void run() {
                HdmiCecLocalDevice device = HdmiControlService.this.mCecController.getLocalDevice(this.val$deviceType);
                if (device == null) {
                    Slog.w(HdmiControlService.TAG, "Local device not available");
                    return;
                }
                if (this.val$hasVendorId) {
                    HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildVendorCommandWithId(device.getDeviceInfo().getLogicalAddress(), this.val$targetAddress, HdmiControlService.this.getVendorId(), this.val$params));
                } else {
                    HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildVendorCommand(device.getDeviceInfo().getLogicalAddress(), this.val$targetAddress, this.val$params));
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.11 */
        class AnonymousClass11 implements Runnable {
            final /* synthetic */ int val$deviceId;
            final /* synthetic */ int val$deviceType;

            AnonymousClass11(int val$deviceId, int val$deviceType) {
                this.val$deviceId = val$deviceId;
                this.val$deviceType = val$deviceType;
            }

            public void run() {
                HdmiMhlLocalDeviceStub mhlDevice = HdmiControlService.this.mMhlController.getLocalDeviceById(this.val$deviceId);
                if (mhlDevice != null) {
                    mhlDevice.sendStandby();
                    return;
                }
                HdmiCecLocalDevice device = HdmiControlService.this.mCecController.getLocalDevice(this.val$deviceType);
                if (device == null) {
                    Slog.w(HdmiControlService.TAG, "Local device not available");
                } else {
                    device.sendStandby(this.val$deviceId);
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.12 */
        class AnonymousClass12 implements Runnable {
            final /* synthetic */ byte[] val$recordSource;
            final /* synthetic */ int val$recorderAddress;

            AnonymousClass12(int val$recorderAddress, byte[] val$recordSource) {
                this.val$recorderAddress = val$recorderAddress;
                this.val$recordSource = val$recordSource;
            }

            public void run() {
                if (HdmiControlService.this.isTvDeviceEnabled()) {
                    HdmiControlService.this.tv().startOneTouchRecord(this.val$recorderAddress, this.val$recordSource);
                } else {
                    Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.13 */
        class AnonymousClass13 implements Runnable {
            final /* synthetic */ int val$recorderAddress;

            AnonymousClass13(int val$recorderAddress) {
                this.val$recorderAddress = val$recorderAddress;
            }

            public void run() {
                if (HdmiControlService.this.isTvDeviceEnabled()) {
                    HdmiControlService.this.tv().stopOneTouchRecord(this.val$recorderAddress);
                } else {
                    Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.14 */
        class AnonymousClass14 implements Runnable {
            final /* synthetic */ byte[] val$recordSource;
            final /* synthetic */ int val$recorderAddress;
            final /* synthetic */ int val$sourceType;

            AnonymousClass14(int val$recorderAddress, int val$sourceType, byte[] val$recordSource) {
                this.val$recorderAddress = val$recorderAddress;
                this.val$sourceType = val$sourceType;
                this.val$recordSource = val$recordSource;
            }

            public void run() {
                if (HdmiControlService.this.isTvDeviceEnabled()) {
                    HdmiControlService.this.tv().startTimerRecording(this.val$recorderAddress, this.val$sourceType, this.val$recordSource);
                } else {
                    Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.15 */
        class AnonymousClass15 implements Runnable {
            final /* synthetic */ byte[] val$recordSource;
            final /* synthetic */ int val$recorderAddress;
            final /* synthetic */ int val$sourceType;

            AnonymousClass15(int val$recorderAddress, int val$sourceType, byte[] val$recordSource) {
                this.val$recorderAddress = val$recorderAddress;
                this.val$sourceType = val$sourceType;
                this.val$recordSource = val$recordSource;
            }

            public void run() {
                if (HdmiControlService.this.isTvDeviceEnabled()) {
                    HdmiControlService.this.tv().clearTimerRecording(this.val$recorderAddress, this.val$sourceType, this.val$recordSource);
                } else {
                    Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.16 */
        class AnonymousClass16 implements Runnable {
            final /* synthetic */ byte[] val$data;
            final /* synthetic */ int val$length;
            final /* synthetic */ int val$offset;
            final /* synthetic */ int val$portId;

            AnonymousClass16(int val$portId, int val$offset, int val$length, byte[] val$data) {
                this.val$portId = val$portId;
                this.val$offset = val$offset;
                this.val$length = val$length;
                this.val$data = val$data;
            }

            public void run() {
                if (!HdmiControlService.this.isControlEnabled()) {
                    Slog.w(HdmiControlService.TAG, "Hdmi control is disabled.");
                } else if (HdmiControlService.this.mMhlController.getLocalDevice(this.val$portId) == null) {
                    Slog.w(HdmiControlService.TAG, "Invalid port id:" + this.val$portId);
                } else {
                    HdmiControlService.this.mMhlController.sendVendorCommand(this.val$portId, this.val$offset, this.val$length, this.val$data);
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ IHdmiControlCallback val$callback;
            final /* synthetic */ int val$deviceId;

            AnonymousClass1(IHdmiControlCallback val$callback, int val$deviceId) {
                this.val$callback = val$callback;
                this.val$deviceId = val$deviceId;
            }

            public void run() {
                if (this.val$callback == null) {
                    Slog.e(HdmiControlService.TAG, "Callback cannot be null");
                    return;
                }
                HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                if (tv != null) {
                    HdmiMhlLocalDeviceStub device = HdmiControlService.this.mMhlController.getLocalDeviceById(this.val$deviceId);
                    if (device == null) {
                        tv.deviceSelect(this.val$deviceId, this.val$callback);
                    } else if (device.getPortId() == tv.getActivePortId()) {
                        HdmiControlService.this.invokeCallback(this.val$callback, HdmiControlService.STANDBY_SCREEN_OFF);
                    } else {
                        device.turnOn(this.val$callback);
                        tv.doManualPortSwitching(device.getPortId(), null);
                    }
                } else if (HdmiControlService.this.mAddressAllocated) {
                    Slog.w(HdmiControlService.TAG, "Local tv device not available");
                    HdmiControlService.this.invokeCallback(this.val$callback, HdmiControlService.INITIATED_BY_SCREEN_ON);
                } else {
                    HdmiControlService.this.mSelectRequestBuffer.set(SelectRequestBuffer.newDeviceSelect(HdmiControlService.this, this.val$deviceId, this.val$callback));
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ IHdmiControlCallback val$callback;
            final /* synthetic */ int val$portId;

            AnonymousClass2(IHdmiControlCallback val$callback, int val$portId) {
                this.val$callback = val$callback;
                this.val$portId = val$portId;
            }

            public void run() {
                if (this.val$callback == null) {
                    Slog.e(HdmiControlService.TAG, "Callback cannot be null");
                    return;
                }
                HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                if (tv != null) {
                    tv.doManualPortSwitching(this.val$portId, this.val$callback);
                } else if (HdmiControlService.this.mAddressAllocated) {
                    Slog.w(HdmiControlService.TAG, "Local tv device not available");
                    HdmiControlService.this.invokeCallback(this.val$callback, HdmiControlService.INITIATED_BY_SCREEN_ON);
                } else {
                    HdmiControlService.this.mSelectRequestBuffer.set(SelectRequestBuffer.newPortSelect(HdmiControlService.this, this.val$portId, this.val$callback));
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ int val$deviceType;
            final /* synthetic */ boolean val$isPressed;
            final /* synthetic */ int val$keyCode;

            AnonymousClass3(int val$keyCode, boolean val$isPressed, int val$deviceType) {
                this.val$keyCode = val$keyCode;
                this.val$isPressed = val$isPressed;
                this.val$deviceType = val$deviceType;
            }

            public void run() {
                HdmiMhlLocalDeviceStub device = HdmiControlService.this.mMhlController.getLocalDevice(HdmiControlService.this.mActivePortId);
                if (device != null) {
                    device.sendKeyEvent(this.val$keyCode, this.val$isPressed);
                    return;
                }
                if (HdmiControlService.this.mCecController != null) {
                    HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(this.val$deviceType);
                    if (localDevice == null) {
                        Slog.w(HdmiControlService.TAG, "Local device not available");
                        return;
                    }
                    localDevice.sendKeyEvent(this.val$keyCode, this.val$isPressed);
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ IHdmiControlCallback val$callback;

            AnonymousClass4(IHdmiControlCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void run() {
                HdmiControlService.this.oneTouchPlay(this.val$callback);
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ IHdmiControlCallback val$callback;

            AnonymousClass5(IHdmiControlCallback val$callback) {
                this.val$callback = val$callback;
            }

            public void run() {
                HdmiControlService.this.queryDisplayStatus(this.val$callback);
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.6 */
        class AnonymousClass6 implements Runnable {
            final /* synthetic */ IHdmiControlCallback val$callback;
            final /* synthetic */ boolean val$enabled;

            AnonymousClass6(IHdmiControlCallback val$callback, boolean val$enabled) {
                this.val$callback = val$callback;
                this.val$enabled = val$enabled;
            }

            public void run() {
                HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                if (tv == null) {
                    Slog.w(HdmiControlService.TAG, "Local tv device not available");
                    HdmiControlService.this.invokeCallback(this.val$callback, HdmiControlService.INITIATED_BY_SCREEN_ON);
                    return;
                }
                tv.changeSystemAudioMode(this.val$enabled, this.val$callback);
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.7 */
        class AnonymousClass7 implements Runnable {
            final /* synthetic */ int val$maxIndex;
            final /* synthetic */ int val$newIndex;
            final /* synthetic */ int val$oldIndex;

            AnonymousClass7(int val$oldIndex, int val$newIndex, int val$maxIndex) {
                this.val$oldIndex = val$oldIndex;
                this.val$newIndex = val$newIndex;
                this.val$maxIndex = val$maxIndex;
            }

            public void run() {
                HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                if (tv == null) {
                    Slog.w(HdmiControlService.TAG, "Local tv device not available");
                } else {
                    tv.changeVolume(this.val$oldIndex, this.val$newIndex - this.val$oldIndex, this.val$maxIndex);
                }
            }
        }

        /* renamed from: com.android.server.hdmi.HdmiControlService.BinderService.8 */
        class AnonymousClass8 implements Runnable {
            final /* synthetic */ boolean val$mute;

            AnonymousClass8(boolean val$mute) {
                this.val$mute = val$mute;
            }

            public void run() {
                HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                if (tv == null) {
                    Slog.w(HdmiControlService.TAG, "Local tv device not available");
                } else {
                    tv.changeMute(this.val$mute);
                }
            }
        }

        private BinderService() {
        }

        public int[] getSupportedTypes() {
            HdmiControlService.this.enforceAccessPermission();
            int[] localDevices = new int[HdmiControlService.this.mLocalDevices.size()];
            for (int i = HdmiControlService.STANDBY_SCREEN_OFF; i < localDevices.length; i += HdmiControlService.STANDBY_SHUTDOWN) {
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
                return new HdmiDeviceInfo(activeSource.logicalAddress, activeSource.physicalAddress, -1, -1, HdmiControlService.STANDBY_SCREEN_OFF, "");
            }
            int activePath = tv.getActivePath();
            if (activePath == 65535) {
                return null;
            }
            HdmiDeviceInfo info = tv.getSafeDeviceInfoByPath(activePath);
            if (info == null) {
                info = new HdmiDeviceInfo(activePath, tv.getActivePortId());
            }
            return info;
        }

        public void deviceSelect(int deviceId, IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass1(callback, deviceId));
        }

        public void portSelect(int portId, IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass2(callback, portId));
        }

        public void sendKeyEvent(int deviceType, int keyCode, boolean isPressed) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass3(keyCode, isPressed, deviceType));
        }

        public void oneTouchPlay(IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass4(callback));
        }

        public void queryDisplayStatus(IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass5(callback));
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

        public void setSystemAudioMode(boolean enabled, IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass6(callback, enabled));
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

        public void setSystemAudioVolume(int oldIndex, int newIndex, int maxIndex) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass7(oldIndex, newIndex, maxIndex));
        }

        public void setSystemAudioMute(boolean mute) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass8(mute));
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
            HdmiControlService.this.runOnServiceThread(new AnonymousClass10(deviceType, hasVendorId, targetAddress, params));
        }

        public void sendStandby(int deviceType, int deviceId) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass11(deviceId, deviceType));
        }

        public void setHdmiRecordListener(IHdmiRecordListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.setHdmiRecordListener(listener);
        }

        public void startOneTouchRecord(int recorderAddress, byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass12(recorderAddress, recordSource));
        }

        public void stopOneTouchRecord(int recorderAddress) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass13(recorderAddress));
        }

        public void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass14(recorderAddress, sourceType, recordSource));
        }

        public void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass15(recorderAddress, sourceType, recordSource));
        }

        public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new AnonymousClass16(portId, offset, length, data));
        }

        public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.addHdmiMhlVendorCommandListener(listener);
        }

        protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            HdmiControlService.this.getContext().enforceCallingOrSelfPermission("android.permission.DUMP", HdmiControlService.TAG);
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

    private final class CecMessageBuffer {
        private List<HdmiCecMessage> mBuffer;

        /* renamed from: com.android.server.hdmi.HdmiControlService.CecMessageBuffer.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ HdmiCecMessage val$message;

            AnonymousClass1(HdmiCecMessage val$message) {
                this.val$message = val$message;
            }

            public void run() {
                HdmiControlService.this.handleCecCommand(this.val$message);
            }
        }

        private CecMessageBuffer() {
            this.mBuffer = new ArrayList();
        }

        public void bufferMessage(HdmiCecMessage message) {
            switch (message.getOpcode()) {
                case HdmiControlService.INITIATED_BY_HOTPLUG /*4*/:
                case H.APP_TRANSITION_TIMEOUT /*13*/:
                    bufferImageOrTextViewOn(message);
                case 130:
                    bufferActiveSource(message);
                default:
            }
        }

        public void processMessages() {
            for (HdmiCecMessage message : this.mBuffer) {
                HdmiControlService.this.runOnServiceThread(new AnonymousClass1(message));
            }
            this.mBuffer.clear();
        }

        private void bufferActiveSource(HdmiCecMessage message) {
            if (!replaceMessageIfBuffered(message, 130)) {
                this.mBuffer.add(message);
            }
        }

        private void bufferImageOrTextViewOn(HdmiCecMessage message) {
            if (!replaceMessageIfBuffered(message, HdmiControlService.INITIATED_BY_HOTPLUG) && !replaceMessageIfBuffered(message, 13)) {
                this.mBuffer.add(message);
            }
        }

        private boolean replaceMessageIfBuffered(HdmiCecMessage message, int opcode) {
            for (int i = HdmiControlService.STANDBY_SCREEN_OFF; i < this.mBuffer.size(); i += HdmiControlService.STANDBY_SHUTDOWN) {
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
        private HdmiControlBroadcastReceiver() {
        }

        @ServiceThreadOnly
        public void onReceive(Context context, Intent intent) {
            HdmiControlService.this.assertRunOnServiceThread();
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (HdmiControlService.this.isPowerOnOrTransient()) {
                    HdmiControlService.this.onStandby(HdmiControlService.STANDBY_SCREEN_OFF);
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
                HdmiControlService.this.onStandby(HdmiControlService.STANDBY_SHUTDOWN);
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
                HdmiControlService.this.setCecOption(HdmiControlService.STANDBY_SHUTDOWN, HdmiControlService.toInt(enabled));
            } else if (option.equals("hdmi_control_auto_device_off_enabled")) {
                for (Integer intValue : HdmiControlService.this.mLocalDevices) {
                    HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(intValue.intValue());
                    if (localDevice != null) {
                        localDevice.setAutoDeviceOff(enabled);
                    }
                }
            } else if (option.equals("mhl_input_switching_enabled")) {
                HdmiControlService.this.setMhlInputChangeEnabled(enabled);
            } else if (option.equals("mhl_power_charge_enabled")) {
                HdmiControlService.this.mMhlController.setOption(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, HdmiControlService.toInt(enabled));
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
        this.HONG_KONG = new Locale("zh", "HK");
        this.MACAU = new Locale("zh", "MO");
        this.mIoThread = new HandlerThread("Hdmi Control Io Thread");
        this.mLock = new Object();
        this.mHotplugEventListenerRecords = new ArrayList();
        this.mDeviceEventListenerRecords = new ArrayList();
        this.mVendorCommandListenerRecords = new ArrayList();
        this.mSystemAudioModeChangeListenerRecords = new ArrayList();
        this.mHandler = new Handler();
        this.mHdmiControlBroadcastReceiver = new HdmiControlBroadcastReceiver();
        this.mPowerStatus = STANDBY_SHUTDOWN;
        this.mLanguage = Locale.getDefault().getISO3Language();
        this.mStandbyMessageReceived = false;
        this.mWakeUpMessageReceived = false;
        this.mActivePortId = -1;
        this.mMhlVendorCommandListenerRecords = new ArrayList();
        this.mLastInputMhl = -1;
        this.mAddressAllocated = false;
        this.mCecMessageBuffer = new CecMessageBuffer();
        this.mSelectRequestBuffer = new SelectRequestBuffer();
        this.mLocalDevices = getIntList(SystemProperties.get("ro.hdmi.device_type"));
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
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
        this.mPowerStatus = INITIATED_BY_SCREEN_ON;
        this.mProhibitMode = false;
        this.mHdmiControlEnabled = readBooleanSetting("hdmi_control_enabled", true);
        this.mMhlInputChangeEnabled = readBooleanSetting("mhl_input_switching_enabled", true);
        this.mCecController = HdmiCecController.create(this);
        if (this.mCecController != null) {
            if (this.mHdmiControlEnabled) {
                initializeCec(STANDBY_SHUTDOWN);
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
            this.mMhlController.setOption(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, STANDBY_SHUTDOWN);
            return;
        }
        Slog.i(TAG, "Device does not support HDMI-CEC.");
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
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
        if (this.mPowerStatus == INITIATED_BY_SCREEN_ON) {
            this.mPowerStatus = STANDBY_SCREEN_OFF;
        }
        this.mWakeUpMessageReceived = false;
        if (isTvDeviceEnabled()) {
            this.mCecController.setOption(STANDBY_SHUTDOWN, toInt(tv().getAutoWakeup()));
        }
        int reason = -1;
        switch (initiatedBy) {
            case STANDBY_SCREEN_OFF /*0*/:
                reason = STANDBY_SHUTDOWN;
                break;
            case STANDBY_SHUTDOWN /*1*/:
                reason = STANDBY_SCREEN_OFF;
                break;
            case INITIATED_BY_SCREEN_ON /*2*/:
            case INITIATED_BY_WAKE_UP_MESSAGE /*3*/:
                reason = INITIATED_BY_SCREEN_ON;
                break;
        }
        if (reason != -1) {
            invokeVendorCommandListenersOnControlStateChanged(true, reason);
        }
    }

    private void registerContentObserver() {
        ContentResolver resolver = getContext().getContentResolver();
        String[] settings = new String[]{"hdmi_control_enabled", "hdmi_control_auto_wakeup_enabled", "hdmi_control_auto_device_off_enabled", "mhl_input_switching_enabled", "mhl_power_charge_enabled"};
        int length = settings.length;
        for (int i = STANDBY_SCREEN_OFF; i < length; i += STANDBY_SHUTDOWN) {
            resolver.registerContentObserver(Global.getUriFor(settings[i]), false, this.mSettingsObserver, -1);
        }
    }

    private static int toInt(boolean enabled) {
        return enabled ? STANDBY_SHUTDOWN : STANDBY_SCREEN_OFF;
    }

    boolean readBooleanSetting(String key, boolean defVal) {
        if (Global.getInt(getContext().getContentResolver(), key, toInt(defVal)) == STANDBY_SHUTDOWN) {
            return true;
        }
        return false;
    }

    void writeBooleanSetting(String key, boolean value) {
        Global.putInt(getContext().getContentResolver(), key, toInt(value));
    }

    private void initializeCec(int initiatedBy) {
        this.mAddressAllocated = false;
        this.mCecController.setOption(INITIATED_BY_WAKE_UP_MESSAGE, STANDBY_SHUTDOWN);
        this.mCecController.setOption(5, HdmiUtils.languageToInt(this.mLanguage));
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
        ArrayList<HdmiCecLocalDevice> allocatedDevices = new ArrayList();
        int[] finished = new int[STANDBY_SHUTDOWN];
        this.mAddressAllocated = allocatingDevices.isEmpty();
        this.mSelectRequestBuffer.clear();
        for (HdmiCecLocalDevice localDevice : allocatingDevices) {
            this.mCecController.allocateLogicalAddress(localDevice.getType(), localDevice.getPreferredAddress(), new AnonymousClass1(localDevice, allocatedDevices, allocatingDevices, finished, initiatedBy));
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
            int length = cecPortInfo.length;
            for (i = STANDBY_SCREEN_OFF; i < length; i += STANDBY_SHUTDOWN) {
                info = cecPortInfo[i];
                portIdMap.put(info.getAddress(), info.getId());
                portInfoMap.put(info.getId(), info);
                portDeviceMap.put(info.getId(), new HdmiDeviceInfo(info.getAddress(), info.getId()));
            }
            this.mPortIdMap = new UnmodifiableSparseIntArray(portIdMap);
            this.mPortInfoMap = new UnmodifiableSparseArray(portInfoMap);
            this.mPortDeviceMap = new UnmodifiableSparseArray(portDeviceMap);
            HdmiPortInfo[] mhlPortInfo = this.mMhlController.getPortInfos();
            ArraySet<Integer> mhlSupportedPorts = new ArraySet(mhlPortInfo.length);
            length = mhlPortInfo.length;
            for (i = STANDBY_SCREEN_OFF; i < length; i += STANDBY_SHUTDOWN) {
                info = mhlPortInfo[i];
                if (info.isMhlSupported()) {
                    mhlSupportedPorts.add(Integer.valueOf(info.getId()));
                }
            }
            if (mhlSupportedPorts.isEmpty()) {
                this.mPortInfo = Collections.unmodifiableList(Arrays.asList(cecPortInfo));
                return;
            }
            ArrayList<HdmiPortInfo> arrayList = new ArrayList(cecPortInfo.length);
            int length2 = cecPortInfo.length;
            for (int i2 = STANDBY_SCREEN_OFF; i2 < length2; i2 += STANDBY_SHUTDOWN) {
                info = cecPortInfo[i2];
                if (mhlSupportedPorts.contains(Integer.valueOf(info.getId()))) {
                    arrayList.add(new HdmiPortInfo(info.getId(), info.getType(), info.getAddress(), info.isCecSupported(), true, info.isArcSupported()));
                } else {
                    arrayList.add(info);
                }
            }
            this.mPortInfo = Collections.unmodifiableList(arrayList);
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
        return 65535;
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
        HdmiLogger.error("Invalid message type:" + command, new Object[STANDBY_SCREEN_OFF]);
        if (callback != null) {
            callback.onSendCompleted(INITIATED_BY_WAKE_UP_MESSAGE);
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
            if (errorCode == INITIATED_BY_WAKE_UP_MESSAGE) {
                maySendFeatureAbortCommand(message, INITIATED_BY_WAKE_UP_MESSAGE);
            }
            return true;
        }
        this.mCecMessageBuffer.bufferMessage(message);
        return true;
    }

    void setAudioReturnChannel(int portId, boolean enabled) {
        this.mCecController.setAudioReturnChannel(portId, enabled);
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
            HdmiLogger.warning("Unhandled cec command:" + message, new Object[STANDBY_SCREEN_OFF]);
        }
        return false;
    }

    @ServiceThreadOnly
    void onHotplug(int portId, boolean connected) {
        assertRunOnServiceThread();
        if (connected && !isTvDevice()) {
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
            allocateLogicalAddress(localDevices, INITIATED_BY_HOTPLUG);
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
        int strategy = pickStrategy & INITIATED_BY_WAKE_UP_MESSAGE;
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
        AudioManager audioManager = getAudioManager();
        boolean muted = audioManager.isStreamMute(INITIATED_BY_WAKE_UP_MESSAGE);
        if (!mute) {
            if (muted) {
                audioManager.setStreamMute(INITIATED_BY_WAKE_UP_MESSAGE, false);
            }
            audioManager.setStreamVolume(INITIATED_BY_WAKE_UP_MESSAGE, volume, 257);
        } else if (!muted) {
            audioManager.setStreamMute(INITIATED_BY_WAKE_UP_MESSAGE, true);
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
            invokeDeviceEventListeners(newDevice.getInfo(), STANDBY_SHUTDOWN);
            updateSafeMhlInput();
        } else {
            HdmiMhlLocalDeviceStub device = this.mMhlController.removeLocalDevice(portId);
            if (device != null) {
                device.onDeviceRemoved();
                invokeDeviceEventListeners(device.getInfo(), INITIATED_BY_SCREEN_ON);
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
        for (int i = STANDBY_SCREEN_OFF; i < devices.size(); i += STANDBY_SHUTDOWN) {
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
            invokeCallback(callback, INITIATED_BY_SCREEN_ON);
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
            invokeCallback(callback, INITIATED_BY_SCREEN_ON);
            return;
        }
        source.queryDisplayStatus(callback);
    }

    private void addHotplugEventListener(IHdmiHotplugEventListener listener) {
        HotplugEventListenerRecord record = new HotplugEventListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, STANDBY_SCREEN_OFF);
            synchronized (this.mLock) {
                this.mHotplugEventListenerRecords.add(record);
            }
            runOnServiceThread(new AnonymousClass2(record, listener));
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died");
        }
    }

    private void removeHotplugEventListener(IHdmiHotplugEventListener listener) {
        synchronized (this.mLock) {
            for (HotplugEventListenerRecord record : this.mHotplugEventListenerRecords) {
                if (record.mListener.asBinder() == listener.asBinder()) {
                    listener.asBinder().unlinkToDeath(record, STANDBY_SCREEN_OFF);
                    this.mHotplugEventListenerRecords.remove(record);
                    break;
                }
            }
        }
    }

    private void addDeviceEventListener(IHdmiDeviceEventListener listener) {
        DeviceEventListenerRecord record = new DeviceEventListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, STANDBY_SCREEN_OFF);
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
            listener.asBinder().linkToDeath(record, STANDBY_SCREEN_OFF);
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
                    listener.asBinder().unlinkToDeath(record, STANDBY_SCREEN_OFF);
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
                listener.asBinder().linkToDeath(this.mInputChangeListenerRecord, STANDBY_SCREEN_OFF);
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
    }

    private void setHdmiRecordListener(IHdmiRecordListener listener) {
        synchronized (this.mLock) {
            this.mRecordListenerRecord = new HdmiRecordListenerRecord(listener);
            try {
                listener.asBinder().linkToDeath(this.mRecordListenerRecord, STANDBY_SCREEN_OFF);
            } catch (RemoteException e) {
                Slog.w(TAG, "Listener already died.", e);
            }
        }
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
        return (HdmiCecLocalDeviceTv) this.mCecController.getLocalDevice(STANDBY_SCREEN_OFF);
    }

    boolean isTvDevice() {
        return this.mLocalDevices.contains(Integer.valueOf(STANDBY_SCREEN_OFF));
    }

    boolean isTvDeviceEnabled() {
        return isTvDevice() && tv() != null;
    }

    private HdmiCecLocalDevicePlayback playback() {
        return (HdmiCecLocalDevicePlayback) this.mCecController.getLocalDevice(INITIATED_BY_HOTPLUG);
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
        if (this.mPowerStatus == 0 || this.mPowerStatus == INITIATED_BY_SCREEN_ON) {
            return true;
        }
        return false;
    }

    @ServiceThreadOnly
    boolean isPowerStandbyOrTransient() {
        assertRunOnServiceThread();
        if (this.mPowerStatus == STANDBY_SHUTDOWN || this.mPowerStatus == INITIATED_BY_WAKE_UP_MESSAGE) {
            return true;
        }
        return false;
    }

    @ServiceThreadOnly
    boolean isPowerStandby() {
        assertRunOnServiceThread();
        if (this.mPowerStatus == STANDBY_SHUTDOWN) {
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
        this.mStandbyMessageReceived = true;
        this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 5, STANDBY_SCREEN_OFF);
    }

    @ServiceThreadOnly
    private void onWakeUp() {
        assertRunOnServiceThread();
        this.mPowerStatus = INITIATED_BY_SCREEN_ON;
        if (this.mCecController == null) {
            Slog.i(TAG, "Device does not support HDMI-CEC.");
        } else if (this.mHdmiControlEnabled) {
            int startReason = INITIATED_BY_SCREEN_ON;
            if (this.mWakeUpMessageReceived) {
                startReason = INITIATED_BY_WAKE_UP_MESSAGE;
            }
            initializeCec(startReason);
        }
    }

    @ServiceThreadOnly
    private void onStandby(int standbyAction) {
        assertRunOnServiceThread();
        if (canGoToStandby()) {
            this.mPowerStatus = INITIATED_BY_WAKE_UP_MESSAGE;
            invokeVendorCommandListenersOnControlStateChanged(false, INITIATED_BY_WAKE_UP_MESSAGE);
            disableDevices(new AnonymousClass3(getAllLocalDevices(), standbyAction));
        }
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
            this.mCecController.setOption(5, HdmiUtils.languageToInt(language));
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
        if (this.mPowerStatus == INITIATED_BY_WAKE_UP_MESSAGE) {
            this.mPowerStatus = STANDBY_SHUTDOWN;
            for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
                device.onStandby(this.mStandbyMessageReceived, standbyAction);
            }
            this.mStandbyMessageReceived = false;
            this.mAddressAllocated = false;
            this.mCecController.setOption(INITIATED_BY_WAKE_UP_MESSAGE, STANDBY_SCREEN_OFF);
            this.mMhlController.setOption(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, STANDBY_SCREEN_OFF);
        }
    }

    private void addVendorCommandListener(IHdmiVendorCommandListener listener, int deviceType) {
        VendorCommandListenerRecord record = new VendorCommandListenerRecord(listener, deviceType);
        try {
            listener.asBinder().linkToDeath(record, STANDBY_SCREEN_OFF);
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
            listener.asBinder().linkToDeath(record, STANDBY_SCREEN_OFF);
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
    void setCecOption(int key, int value) {
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
        invokeVendorCommandListenersOnControlStateChanged(false, STANDBY_SHUTDOWN);
        runOnServiceThread(new Runnable() {
            public void run() {
                HdmiControlService.this.disableHdmiControlService();
            }
        });
    }

    @ServiceThreadOnly
    private void enableHdmiControlService() {
        this.mCecController.setOption(INITIATED_BY_SCREEN_ON, STANDBY_SHUTDOWN);
        this.mMhlController.setOption(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, STANDBY_SHUTDOWN);
        initializeCec(STANDBY_SCREEN_OFF);
    }

    @ServiceThreadOnly
    private void disableHdmiControlService() {
        disableDevices(new PendingActionClearedCallback() {
            public void onCleared(HdmiCecLocalDevice device) {
                HdmiControlService.this.assertRunOnServiceThread();
                HdmiControlService.this.mCecController.flush(new Runnable() {
                    public void run() {
                        HdmiControlService.this.mCecController.setOption(HdmiControlService.INITIATED_BY_SCREEN_ON, HdmiControlService.STANDBY_SCREEN_OFF);
                        HdmiControlService.this.mMhlController.setOption(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, HdmiControlService.STANDBY_SCREEN_OFF);
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
            int lastInput = contentOn ? tv().getActivePortId() : -1;
            if (portId != -1) {
                tv().doManualPortSwitching(portId, new AnonymousClass6(lastInput));
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
        this.mMhlController.setOption(H.KEYGUARD_DISMISS_DONE, toInt(enabled));
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
