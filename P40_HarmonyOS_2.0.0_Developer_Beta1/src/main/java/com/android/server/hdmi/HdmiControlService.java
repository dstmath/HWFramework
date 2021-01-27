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
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.SystemService;
import com.android.server.hdmi.HdmiAnnotations;
import com.android.server.hdmi.HdmiCecController;
import com.android.server.hdmi.HdmiCecLocalDevice;
import com.android.server.power.ShutdownThread;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import libcore.util.EmptyArray;

public class HdmiControlService extends SystemService {
    static final int INITIATED_BY_BOOT_UP = 1;
    static final int INITIATED_BY_ENABLE_CEC = 0;
    static final int INITIATED_BY_HOTPLUG = 4;
    static final int INITIATED_BY_SCREEN_ON = 2;
    static final int INITIATED_BY_WAKE_UP_MESSAGE = 3;
    static final String PERMISSION = "android.permission.HDMI_CEC";
    static final int STANDBY_SCREEN_OFF = 0;
    static final int STANDBY_SHUTDOWN = 1;
    private static final String TAG = "HdmiControlService";
    private static final boolean isHdmiCecNeverClaimPlaybackLogicAddr = SystemProperties.getBoolean("ro.hdmi.property_hdmi_cec_never_claim_playback_logical_address", false);
    private static final Map<String, String> mTerminologyToBibliographicMap = new HashMap();
    private final Locale HONG_KONG = new Locale("zh", "HK");
    private final Locale MACAU = new Locale("zh", "MO");
    @HdmiAnnotations.ServiceThreadOnly
    private int mActivePortId = -1;
    @GuardedBy({"mLock"})
    protected final HdmiCecLocalDevice.ActiveSource mActiveSource = new HdmiCecLocalDevice.ActiveSource();
    private boolean mAddressAllocated = false;
    private HdmiCecController mCecController;
    private final CecMessageBuffer mCecMessageBuffer = new CecMessageBuffer();
    @GuardedBy({"mLock"})
    private final ArrayList<DeviceEventListenerRecord> mDeviceEventListenerRecords = new ArrayList<>();
    private final Handler mHandler = new Handler();
    private final HdmiControlBroadcastReceiver mHdmiControlBroadcastReceiver = new HdmiControlBroadcastReceiver();
    @GuardedBy({"mLock"})
    private boolean mHdmiControlEnabled;
    @GuardedBy({"mLock"})
    private final ArrayList<HotplugEventListenerRecord> mHotplugEventListenerRecords = new ArrayList<>();
    @GuardedBy({"mLock"})
    private InputChangeListenerRecord mInputChangeListenerRecord;
    private Looper mIoLooper;
    private final HandlerThread mIoThread = new HandlerThread("Hdmi Control Io Thread");
    @HdmiAnnotations.ServiceThreadOnly
    private String mLanguage = Locale.getDefault().getISO3Language();
    @HdmiAnnotations.ServiceThreadOnly
    private int mLastInputMhl = -1;
    private final List<Integer> mLocalDevices = getIntList(SystemProperties.get("ro.hdmi.device_type"));
    private final Object mLock = new Object();
    private HdmiCecMessageValidator mMessageValidator;
    private HdmiMhlControllerStub mMhlController;
    @GuardedBy({"mLock"})
    private List<HdmiDeviceInfo> mMhlDevices;
    @GuardedBy({"mLock"})
    private boolean mMhlInputChangeEnabled;
    @GuardedBy({"mLock"})
    private final ArrayList<HdmiMhlVendorCommandListenerRecord> mMhlVendorCommandListenerRecords = new ArrayList<>();
    @GuardedBy({"mLock"})
    private int mPhysicalAddress = 65535;
    private UnmodifiableSparseArray<HdmiDeviceInfo> mPortDeviceMap;
    private UnmodifiableSparseIntArray mPortIdMap;
    private List<HdmiPortInfo> mPortInfo;
    private UnmodifiableSparseArray<HdmiPortInfo> mPortInfoMap;
    private PowerManager mPowerManager;
    @HdmiAnnotations.ServiceThreadOnly
    private int mPowerStatus = 1;
    @GuardedBy({"mLock"})
    private boolean mProhibitMode;
    @GuardedBy({"mLock"})
    private HdmiRecordListenerRecord mRecordListenerRecord;
    private final SelectRequestBuffer mSelectRequestBuffer = new SelectRequestBuffer();
    private final SettingsObserver mSettingsObserver = new SettingsObserver(this.mHandler);
    @HdmiAnnotations.ServiceThreadOnly
    private boolean mStandbyMessageReceived = false;
    @GuardedBy({"mLock"})
    private boolean mSystemAudioActivated = false;
    private final ArrayList<SystemAudioModeChangeListenerRecord> mSystemAudioModeChangeListenerRecords = new ArrayList<>();
    private TvInputManager mTvInputManager;
    @GuardedBy({"mLock"})
    private final ArrayList<VendorCommandListenerRecord> mVendorCommandListenerRecords = new ArrayList<>();
    @HdmiAnnotations.ServiceThreadOnly
    private boolean mWakeUpMessageReceived = false;

    /* access modifiers changed from: package-private */
    public interface DevicePollingCallback {
        void onPollingFinished(List<Integer> list);
    }

    /* access modifiers changed from: package-private */
    public interface SendMessageCallback {
        void onSendCompleted(int i);
    }

    static {
        mTerminologyToBibliographicMap.put("sqi", "alb");
        mTerminologyToBibliographicMap.put("hye", "arm");
        mTerminologyToBibliographicMap.put("eus", "baq");
        mTerminologyToBibliographicMap.put("mya", "bur");
        mTerminologyToBibliographicMap.put("ces", "cze");
        mTerminologyToBibliographicMap.put("nld", "dut");
        mTerminologyToBibliographicMap.put("kat", "geo");
        mTerminologyToBibliographicMap.put("deu", "ger");
        mTerminologyToBibliographicMap.put("ell", "gre");
        mTerminologyToBibliographicMap.put("fra", "fre");
        mTerminologyToBibliographicMap.put("isl", "ice");
        mTerminologyToBibliographicMap.put("mkd", "mac");
        mTerminologyToBibliographicMap.put("mri", "mao");
        mTerminologyToBibliographicMap.put("msa", "may");
        mTerminologyToBibliographicMap.put("fas", "per");
        mTerminologyToBibliographicMap.put("ron", "rum");
        mTerminologyToBibliographicMap.put("slk", "slo");
        mTerminologyToBibliographicMap.put("bod", "tib");
        mTerminologyToBibliographicMap.put("cym", "wel");
    }

    private class HdmiControlBroadcastReceiver extends BroadcastReceiver {
        private HdmiControlBroadcastReceiver() {
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        @HdmiAnnotations.ServiceThreadOnly
        public void onReceive(Context context, Intent intent) {
            char c;
            HdmiControlService.this.assertRunOnServiceThread();
            boolean isReboot = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY).contains("1");
            String action = intent.getAction();
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 158859398:
                    if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1947666138:
                    if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c != 0) {
                if (c != 1) {
                    if (c == 2) {
                        String language = getMenuLanguage();
                        if (!HdmiControlService.this.mLanguage.equals(language)) {
                            HdmiControlService.this.onLanguageChanged(language);
                        }
                    } else if (c == 3 && HdmiControlService.this.isPowerOnOrTransient() && !isReboot) {
                        HdmiControlService.this.onStandby(1);
                    }
                } else if (HdmiControlService.this.isPowerStandbyOrTransient()) {
                    HdmiControlService.this.onWakeUp();
                }
            } else if (HdmiControlService.this.isPowerOnOrTransient() && !isReboot) {
                HdmiControlService.this.onStandby(0);
            }
        }

        private String getMenuLanguage() {
            Locale locale = Locale.getDefault();
            if (locale.equals(Locale.TAIWAN) || locale.equals(HdmiControlService.this.HONG_KONG) || locale.equals(HdmiControlService.this.MACAU)) {
                return "chi";
            }
            String language = locale.getISO3Language();
            if (HdmiControlService.mTerminologyToBibliographicMap.containsKey(language)) {
                return (String) HdmiControlService.mTerminologyToBibliographicMap.get(language);
            }
            return language;
        }
    }

    /* access modifiers changed from: private */
    public final class CecMessageBuffer {
        private List<HdmiCecMessage> mBuffer;

        private CecMessageBuffer() {
            this.mBuffer = new ArrayList();
        }

        public boolean bufferMessage(HdmiCecMessage message) {
            int opcode = message.getOpcode();
            if (opcode == 4 || opcode == 13) {
                bufferImageOrTextViewOn(message);
                return true;
            } else if (opcode != 130) {
                return false;
            } else {
                bufferActiveSource(message);
                return true;
            }
        }

        public void processMessages() {
            for (final HdmiCecMessage message : this.mBuffer) {
                HdmiControlService.this.runOnServiceThread(new Runnable() {
                    /* class com.android.server.hdmi.HdmiControlService.CecMessageBuffer.AnonymousClass1 */

                    @Override // java.lang.Runnable
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

    public HdmiControlService(Context context) {
        super(context);
    }

    protected static List<Integer> getIntList(String string) {
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

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.android.server.hdmi.HdmiControlService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v8, types: [android.os.IBinder, com.android.server.hdmi.HdmiControlService$BinderService] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        if (this.mIoLooper == null) {
            this.mIoThread.start();
            this.mIoLooper = this.mIoThread.getLooper();
        }
        this.mPowerStatus = 2;
        this.mProhibitMode = false;
        this.mHdmiControlEnabled = readBooleanSetting("hdmi_control_enabled", true);
        this.mMhlInputChangeEnabled = readBooleanSetting("mhl_input_switching_enabled", true);
        if (this.mCecController == null) {
            this.mCecController = HdmiCecController.create(this);
        }
        HdmiCecController hdmiCecController = this.mCecController;
        if (hdmiCecController != null) {
            if (this.mHdmiControlEnabled) {
                initializeCec(1);
            } else {
                hdmiCecController.setOption(2, false);
            }
            if (this.mMhlController == null) {
                this.mMhlController = HdmiMhlControllerStub.create(this);
            }
            if (!this.mMhlController.isReady()) {
                Slog.i(TAG, "Device does not support MHL-control.");
            }
            this.mMhlDevices = Collections.emptyList();
            initPortInfo();
            if (this.mMessageValidator == null) {
                this.mMessageValidator = new HdmiCecMessageValidator(this);
            }
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setCecController(HdmiCecController cecController) {
        this.mCecController = cecController;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setHdmiMhlController(HdmiMhlControllerStub hdmiMhlController) {
        this.mMhlController = hdmiMhlController;
    }

    @Override // com.android.server.SystemService
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
        TvInputManager tvInputManager = this.mTvInputManager;
        if (tvInputManager != null) {
            tvInputManager.registerCallback(callback, this.mHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterTvInputCallback(TvInputManager.TvInputCallback callback) {
        TvInputManager tvInputManager = this.mTvInputManager;
        if (tvInputManager != null) {
            tvInputManager.unregisterCallback(callback);
        }
    }

    /* access modifiers changed from: package-private */
    public PowerManager getPowerManager() {
        return this.mPowerManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInitializeCecComplete(int initiatedBy) {
        if (this.mPowerStatus == 2) {
            this.mPowerStatus = 0;
        }
        this.mWakeUpMessageReceived = false;
        if (isTvDeviceEnabled()) {
            this.mCecController.setOption(1, tv().getAutoWakeup());
        }
        int reason = -1;
        if (initiatedBy == 0) {
            reason = 1;
        } else if (initiatedBy == 1) {
            reason = 0;
        } else if (initiatedBy == 2 || initiatedBy == 3) {
            reason = 2;
        }
        if (reason != -1) {
            invokeVendorCommandListenersOnControlStateChanged(true, reason);
        }
    }

    private void registerContentObserver() {
        ContentResolver resolver = getContext().getContentResolver();
        for (String s : new String[]{"hdmi_control_enabled", "hdmi_control_auto_wakeup_enabled", "hdmi_control_auto_device_off_enabled", "hdmi_system_audio_control_enabled", "mhl_input_switching_enabled", "mhl_power_charge_enabled", "hdmi_cec_switch_enabled", "device_name"}) {
            resolver.registerContentObserver(Settings.Global.getUriFor(s), false, this.mSettingsObserver, -1);
        }
    }

    /* access modifiers changed from: private */
    public class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.database.ContentObserver
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
                    c = 65535;
                    break;
                case -1573020421:
                    if (option.equals("hdmi_cec_switch_enabled")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1543071020:
                    if (option.equals("device_name")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1489007315:
                    if (option.equals("hdmi_system_audio_control_enabled")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1262529811:
                    if (option.equals("mhl_input_switching_enabled")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -885757826:
                    if (option.equals("mhl_power_charge_enabled")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 726613192:
                    if (option.equals("hdmi_control_auto_wakeup_enabled")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1628046095:
                    if (option.equals("hdmi_control_auto_device_off_enabled")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
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
                    for (Integer num : HdmiControlService.this.mLocalDevices) {
                        HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(num.intValue());
                        if (localDevice != null) {
                            localDevice.setAutoDeviceOff(enabled);
                        }
                    }
                    return;
                case 3:
                    if (HdmiControlService.this.isTvDeviceEnabled()) {
                        HdmiControlService.this.tv().setSystemAudioControlFeatureEnabled(enabled);
                    }
                    if (!HdmiControlService.this.isAudioSystemDevice()) {
                        return;
                    }
                    if (HdmiControlService.this.audioSystem() == null) {
                        Slog.e(HdmiControlService.TAG, "Audio System device has not registered yet. Can't turn system audio mode on.");
                        return;
                    } else {
                        HdmiControlService.this.audioSystem().onSystemAduioControlFeatureSupportChanged(enabled);
                        return;
                    }
                case 4:
                    if (!HdmiControlService.this.isAudioSystemDevice()) {
                        return;
                    }
                    if (HdmiControlService.this.audioSystem() == null) {
                        Slog.w(HdmiControlService.TAG, "Switch device has not registered yet. Can't turn routing on.");
                        return;
                    } else {
                        HdmiControlService.this.audioSystem().setRoutingControlFeatureEnables(enabled);
                        return;
                    }
                case 5:
                    HdmiControlService.this.setMhlInputChangeEnabled(enabled);
                    return;
                case 6:
                    HdmiControlService.this.mMhlController.setOption(102, HdmiControlService.toInt(enabled));
                    return;
                case 7:
                    HdmiControlService.this.setDisplayName(HdmiControlService.this.readStringSetting(option, Build.MODEL));
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static int toInt(boolean enabled) {
        return enabled ? 1 : 0;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean readBooleanSetting(String key, boolean defVal) {
        return Settings.Global.getInt(getContext().getContentResolver(), key, toInt(defVal)) == 1;
    }

    /* access modifiers changed from: package-private */
    public void writeBooleanSetting(String key, boolean value) {
        Settings.Global.putInt(getContext().getContentResolver(), key, toInt(value));
    }

    /* access modifiers changed from: package-private */
    public void writeStringSystemProperty(String key, String value) {
        SystemProperties.set(key, value);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean readBooleanSystemProperty(String key, boolean defVal) {
        return SystemProperties.getBoolean(key, defVal);
    }

    /* access modifiers changed from: package-private */
    public String readStringSetting(String key, String defVal) {
        String content = Settings.Global.getString(getContext().getContentResolver(), key);
        if (TextUtils.isEmpty(content)) {
            return defVal;
        }
        return content;
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
        for (Integer num : this.mLocalDevices) {
            int type = num.intValue();
            if (type != 4 || !isHdmiCecNeverClaimPlaybackLogicAddr) {
                HdmiCecLocalDevice localDevice = this.mCecController.getLocalDevice(type);
                if (localDevice == null) {
                    localDevice = HdmiCecLocalDevice.create(this, type);
                }
                localDevice.init();
                localDevices.add(localDevice);
            }
        }
        clearLocalDevices();
        allocateLogicalAddress(localDevices, initiatedBy);
    }

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    @VisibleForTesting
    public void allocateLogicalAddress(final ArrayList<HdmiCecLocalDevice> allocatingDevices, final int initiatedBy) {
        assertRunOnServiceThread();
        this.mCecController.clearLogicalAddress();
        final ArrayList<HdmiCecLocalDevice> allocatedDevices = new ArrayList<>();
        final int[] finished = new int[1];
        this.mAddressAllocated = allocatingDevices.isEmpty();
        this.mSelectRequestBuffer.clear();
        Iterator<HdmiCecLocalDevice> it = allocatingDevices.iterator();
        while (it.hasNext()) {
            final HdmiCecLocalDevice localDevice = it.next();
            this.mCecController.allocateLogicalAddress(localDevice.getType(), localDevice.getPreferredAddress(), new HdmiCecController.AllocateAddressCallback() {
                /* class com.android.server.hdmi.HdmiControlService.AnonymousClass1 */

                @Override // com.android.server.hdmi.HdmiCecController.AllocateAddressCallback
                public void onAllocated(int deviceType, int logicalAddress) {
                    if (logicalAddress == 15) {
                        Slog.e(HdmiControlService.TAG, "Failed to allocate address:[device_type:" + deviceType + "]");
                    } else {
                        localDevice.setDeviceInfo(HdmiControlService.this.createDeviceInfo(logicalAddress, deviceType, 0));
                        HdmiControlService.this.mCecController.addLocalDevice(deviceType, localDevice);
                        HdmiControlService.this.mCecController.addLogicalAddress(logicalAddress);
                        allocatedDevices.add(localDevice);
                    }
                    int size = allocatingDevices.size();
                    int[] iArr = finished;
                    int i = iArr[0] + 1;
                    iArr[0] = i;
                    if (size == i) {
                        HdmiControlService.this.mAddressAllocated = true;
                        int i2 = initiatedBy;
                        if (i2 != 4) {
                            HdmiControlService.this.onInitializeCecComplete(i2);
                        }
                        HdmiControlService.this.notifyAddressAllocated(allocatedDevices, initiatedBy);
                        HdmiControlService.this.mCecMessageBuffer.processMessages();
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private void notifyAddressAllocated(ArrayList<HdmiCecLocalDevice> devices, int initiatedBy) {
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

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    @VisibleForTesting
    public void initPortInfo() {
        assertRunOnServiceThread();
        HdmiPortInfo[] cecPortInfo = null;
        synchronized (this.mLock) {
            this.mPhysicalAddress = getPhysicalAddress();
        }
        HdmiCecController hdmiCecController = this.mCecController;
        if (hdmiCecController != null) {
            cecPortInfo = hdmiCecController.getPortInfos();
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
            HdmiMhlControllerStub hdmiMhlControllerStub = this.mMhlController;
            if (hdmiMhlControllerStub != null) {
                HdmiPortInfo[] mhlPortInfo = hdmiMhlControllerStub.getPortInfos();
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
                        result.add(new HdmiPortInfo(info3.getId(), info3.getType(), info3.getAddress(), info3.isCecSupported(), true, info3.isArcSupported()));
                    } else {
                        result.add(info3);
                    }
                }
                this.mPortInfo = Collections.unmodifiableList(result);
            }
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
        return 65535;
    }

    /* access modifiers changed from: package-private */
    public int pathToPortId(int path) {
        int physicalAddress;
        int mask = 61440;
        int finalMask = 61440;
        synchronized (this.mLock) {
            physicalAddress = this.mPhysicalAddress;
        }
        int maskedAddress = physicalAddress;
        while (maskedAddress != 0) {
            maskedAddress = physicalAddress & mask;
            finalMask |= mask;
            mask >>= 4;
        }
        return this.mPortIdMap.get(path & finalMask, -1);
    }

    /* access modifiers changed from: package-private */
    public boolean isValidPortId(int portId) {
        return getPortInfo(portId) != null;
    }

    /* access modifiers changed from: package-private */
    public Looper getIoLooper() {
        return this.mIoLooper;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setIoLooper(Looper ioLooper) {
        this.mIoLooper = ioLooper;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setMessageValidator(HdmiCecMessageValidator messageValidator) {
        this.mMessageValidator = messageValidator;
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
    /* access modifiers changed from: public */
    private void assertRunOnServiceThread() {
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
        int errorCode = this.mMessageValidator.isValid(message);
        if (errorCode != 0) {
            if (errorCode == 3) {
                maySendFeatureAbortCommand(message, 3);
            }
            return true;
        } else if (dispatchMessageToLocalDevice(message)) {
            return true;
        } else {
            if (!this.mAddressAllocated) {
                return this.mCecMessageBuffer.bufferMessage(message);
            }
            return false;
        }
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
        if (connected && !isTvDevice() && getPortInfo(portId).getType() == 1) {
            if (isSwitchDevice()) {
                initPortInfo();
                HdmiLogger.debug("initPortInfo for switch device when onHotplug from tx.", new Object[0]);
            }
            ArrayList<HdmiCecLocalDevice> localDevices = new ArrayList<>();
            for (Integer num : this.mLocalDevices) {
                int type = num.intValue();
                if (type != 4 || !isHdmiCecNeverClaimPlaybackLogicAddr) {
                    HdmiCecLocalDevice localDevice = this.mCecController.getLocalDevice(type);
                    if (localDevice == null) {
                        localDevice = HdmiCecLocalDevice.create(this, type);
                        localDevice.init();
                    }
                    localDevices.add(localDevice);
                }
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
    /* access modifiers changed from: public */
    private HdmiDeviceInfo createDeviceInfo(int logicalAddress, int deviceType, int powerStatus) {
        return new HdmiDeviceInfo(logicalAddress, getPhysicalAddress(), pathToPortId(getPhysicalAddress()), deviceType, getVendorId(), readStringSetting("device_name", Build.MODEL), powerStatus);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayName(String newDisplayName) {
        for (HdmiCecLocalDevice device : getAllLocalDevices()) {
            HdmiDeviceInfo deviceInfo = device.getDeviceInfo();
            if (!deviceInfo.getDisplayName().equals(newDisplayName)) {
                device.setDeviceInfo(new HdmiDeviceInfo(deviceInfo.getLogicalAddress(), deviceInfo.getPhysicalAddress(), deviceInfo.getPortId(), deviceInfo.getDeviceType(), deviceInfo.getVendorId(), newDisplayName, deviceInfo.getDevicePowerStatus()));
                sendCecCommand(HdmiCecMessageBuilder.buildSetOsdNameCommand(device.mAddress, 0, newDisplayName));
            }
        }
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
                    inputs = new ArrayList();
                }
                inputs.add(device.getInfo());
            }
        }
        synchronized (this.mLock) {
            this.mMhlDevices = inputs;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private List<HdmiDeviceInfo> getMhlDevicesLocked() {
        return this.mMhlDevices;
    }

    /* access modifiers changed from: private */
    public class HdmiMhlVendorCommandListenerRecord implements IBinder.DeathRecipient {
        private final IHdmiMhlVendorCommandListener mListener;

        public HdmiMhlVendorCommandListenerRecord(IHdmiMhlVendorCommandListener listener) {
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HdmiControlService.this.mMhlVendorCommandListenerRecords.remove(this);
        }
    }

    /* access modifiers changed from: private */
    public final class HotplugEventListenerRecord implements IBinder.DeathRecipient {
        private final IHdmiHotplugEventListener mListener;

        public HotplugEventListenerRecord(IHdmiHotplugEventListener listener) {
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mHotplugEventListenerRecords.remove(this);
            }
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            if (!(obj instanceof HotplugEventListenerRecord)) {
                return false;
            }
            if (obj == this || ((HotplugEventListenerRecord) obj).mListener == this.mListener) {
                return true;
            }
            return false;
        }

        @Override // java.lang.Object
        public int hashCode() {
            return this.mListener.hashCode();
        }
    }

    /* access modifiers changed from: private */
    public final class DeviceEventListenerRecord implements IBinder.DeathRecipient {
        private final IHdmiDeviceEventListener mListener;

        public DeviceEventListenerRecord(IHdmiDeviceEventListener listener) {
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mDeviceEventListenerRecords.remove(this);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SystemAudioModeChangeListenerRecord implements IBinder.DeathRecipient {
        private final IHdmiSystemAudioModeChangeListener mListener;

        public SystemAudioModeChangeListenerRecord(IHdmiSystemAudioModeChangeListener listener) {
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mSystemAudioModeChangeListenerRecords.remove(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class VendorCommandListenerRecord implements IBinder.DeathRecipient {
        private final int mDeviceType;
        private final IHdmiVendorCommandListener mListener;

        public VendorCommandListenerRecord(IHdmiVendorCommandListener listener, int deviceType) {
            this.mListener = listener;
            this.mDeviceType = deviceType;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                HdmiControlService.this.mVendorCommandListenerRecords.remove(this);
            }
        }
    }

    /* access modifiers changed from: private */
    public class HdmiRecordListenerRecord implements IBinder.DeathRecipient {
        private final IHdmiRecordListener mListener;

        public HdmiRecordListenerRecord(IHdmiRecordListener listener) {
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                if (HdmiControlService.this.mRecordListenerRecord == this) {
                    HdmiControlService.this.mRecordListenerRecord = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceAccessPermission() {
        getContext().enforceCallingOrSelfPermission(PERMISSION, TAG);
    }

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
            if (tv != null) {
                HdmiCecLocalDevice.ActiveSource activeSource = tv.getActiveSource();
                if (activeSource.isValid()) {
                    return new HdmiDeviceInfo(activeSource.logicalAddress, activeSource.physicalAddress, -1, -1, 0, "");
                }
                int activePath = tv.getActivePath();
                if (activePath == 65535) {
                    return null;
                }
                HdmiDeviceInfo info = tv.getSafeDeviceInfoByPath(activePath);
                return info != null ? info : new HdmiDeviceInfo(activePath, tv.getActivePortId());
            } else if (HdmiControlService.this.isTvDevice()) {
                Slog.e(HdmiControlService.TAG, "Local tv device not available.");
                return null;
            } else if (!HdmiControlService.this.isPlaybackDevice()) {
                return null;
            } else {
                if (HdmiControlService.this.playback() != null && HdmiControlService.this.playback().mIsActiveSource) {
                    return HdmiControlService.this.playback().getDeviceInfo();
                }
                HdmiCecLocalDevice.ActiveSource activeSource2 = HdmiControlService.this.mActiveSource;
                if (!activeSource2.isValid()) {
                    return null;
                }
                if (HdmiControlService.this.audioSystem() != null) {
                    for (HdmiDeviceInfo info2 : HdmiControlService.this.audioSystem().getSafeCecDevicesLocked()) {
                        if (info2.getLogicalAddress() == activeSource2.logicalAddress) {
                            return info2;
                        }
                    }
                }
                return new HdmiDeviceInfo(activeSource2.logicalAddress, activeSource2.physicalAddress, HdmiControlService.this.pathToPortId(activeSource2.physicalAddress), HdmiUtils.getTypeFromAddress(activeSource2.logicalAddress), 0, HdmiUtils.getDefaultDeviceName(activeSource2.logicalAddress));
            }
        }

        public void deviceSelect(final int deviceId, final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass1 */

                @Override // java.lang.Runnable
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
                    } else if (HdmiControlService.this.isTvDevice()) {
                        Slog.e(HdmiControlService.TAG, "Local tv device not available");
                    } else {
                        HdmiControlService.this.invokeCallback(callback, 2);
                    }
                }
            });
        }

        public void portSelect(final int portId, final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    if (callback == null) {
                        Slog.e(HdmiControlService.TAG, "Callback cannot be null");
                        return;
                    }
                    HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                    if (tv != null) {
                        tv.doManualPortSwitching(portId, callback);
                        return;
                    }
                    HdmiCecLocalDeviceAudioSystem audioSystem = HdmiControlService.this.audioSystem();
                    if (audioSystem != null) {
                        audioSystem.doManualPortSwitching(portId, callback);
                    } else if (!HdmiControlService.this.mAddressAllocated) {
                        HdmiControlService.this.mSelectRequestBuffer.set(SelectRequestBuffer.newPortSelect(HdmiControlService.this, portId, callback));
                    } else {
                        Slog.w(HdmiControlService.TAG, "Local device not available");
                        HdmiControlService.this.invokeCallback(callback, 2);
                    }
                }
            });
        }

        public void sendKeyEvent(final int deviceType, final int keyCode, final boolean isPressed) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    HdmiMhlLocalDeviceStub device = HdmiControlService.this.mMhlController.getLocalDevice(HdmiControlService.this.mActivePortId);
                    if (device != null) {
                        device.sendKeyEvent(keyCode, isPressed);
                    } else if (HdmiControlService.this.mCecController != null) {
                        HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(deviceType);
                        if (localDevice == null) {
                            Slog.w(HdmiControlService.TAG, "Local device not available to send key event.");
                        } else {
                            localDevice.sendKeyEvent(keyCode, isPressed);
                        }
                    }
                }
            });
        }

        public void sendVolumeKeyEvent(final int deviceType, final int keyCode, final boolean isPressed) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    if (HdmiControlService.this.mCecController == null) {
                        Slog.w(HdmiControlService.TAG, "CEC controller not available to send volume key event.");
                        return;
                    }
                    HdmiCecLocalDevice localDevice = HdmiControlService.this.mCecController.getLocalDevice(deviceType);
                    if (localDevice == null) {
                        Slog.w(HdmiControlService.TAG, "Local device " + deviceType + " not available to send volume key event.");
                        return;
                    }
                    localDevice.sendVolumeKeyEvent(keyCode, isPressed);
                }
            });
        }

        public void oneTouchPlay(final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    HdmiControlService.this.oneTouchPlay(callback);
                }
            });
        }

        public void queryDisplayStatus(final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass6 */

                @Override // java.lang.Runnable
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
            HdmiCecLocalDeviceAudioSystem audioSystem = HdmiControlService.this.audioSystem();
            return (tv != null && tv.isSystemAudioActivated()) || (audioSystem != null && audioSystem.isSystemAudioActivated());
        }

        public int getPhysicalAddress() {
            int i;
            HdmiControlService.this.enforceAccessPermission();
            synchronized (HdmiControlService.this.mLock) {
                i = HdmiControlService.this.mPhysicalAddress;
            }
            return i;
        }

        public void setSystemAudioMode(final boolean enabled, final IHdmiControlCallback callback) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass7 */

                @Override // java.lang.Runnable
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
            List<HdmiDeviceInfo> mergeToUnmodifiableList;
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            synchronized (HdmiControlService.this.mLock) {
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
            List<HdmiDeviceInfo> list;
            List<HdmiDeviceInfo> safeCecDevicesLocked;
            HdmiControlService.this.enforceAccessPermission();
            HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
            if (tv != null) {
                synchronized (HdmiControlService.this.mLock) {
                    safeCecDevicesLocked = tv.getSafeCecDevicesLocked();
                }
                return safeCecDevicesLocked;
            }
            HdmiCecLocalDeviceAudioSystem audioSystem = HdmiControlService.this.audioSystem();
            synchronized (HdmiControlService.this.mLock) {
                if (audioSystem == null) {
                    list = Collections.emptyList();
                } else {
                    list = audioSystem.getSafeCecDevicesLocked();
                }
            }
            return list;
        }

        public void powerOffRemoteDevice(final int logicalAddress, final int powerStatus) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass8 */

                @Override // java.lang.Runnable
                public void run() {
                    Slog.w(HdmiControlService.TAG, "Device " + logicalAddress + " power status is " + powerStatus + " before standby command sent out");
                    HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildStandby(HdmiControlService.this.getRemoteControlSourceAddress(), logicalAddress));
                }
            });
        }

        public void powerOnRemoteDevice(int logicalAddress, int powerStatus) {
        }

        public void askRemoteDeviceToBecomeActiveSource(final int physicalAddress) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass9 */

                @Override // java.lang.Runnable
                public void run() {
                    HdmiCecMessage setStreamPath = HdmiCecMessageBuilder.buildSetStreamPath(HdmiControlService.this.getRemoteControlSourceAddress(), physicalAddress);
                    if (HdmiControlService.this.pathToPortId(physicalAddress) != -1) {
                        if (HdmiControlService.this.getSwitchDevice() != null) {
                            HdmiControlService.this.getSwitchDevice().handleSetStreamPath(setStreamPath);
                        } else {
                            Slog.e(HdmiControlService.TAG, "Can't get the correct local device to handle routing.");
                        }
                    }
                    HdmiControlService.this.sendCecCommand(setStreamPath);
                }
            });
        }

        public void setSystemAudioVolume(final int oldIndex, final int newIndex, final int maxIndex) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass10 */

                @Override // java.lang.Runnable
                public void run() {
                    HdmiCecLocalDeviceTv tv = HdmiControlService.this.tv();
                    if (tv == null) {
                        Slog.w(HdmiControlService.TAG, "Local tv device not available");
                        return;
                    }
                    int i = oldIndex;
                    tv.changeVolume(i, newIndex - i, maxIndex);
                }
            });
        }

        public void setSystemAudioMute(final boolean mute) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass11 */

                @Override // java.lang.Runnable
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
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass12 */

                @Override // java.lang.Runnable
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

        public void sendVendorCommand(final int deviceType, final int targetAddress, final byte[] params, final boolean hasVendorId) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass13 */

                @Override // java.lang.Runnable
                public void run() {
                    HdmiCecLocalDevice device = HdmiControlService.this.mCecController.getLocalDevice(deviceType);
                    if (device == null) {
                        Slog.w(HdmiControlService.TAG, "Local device not available");
                    } else if (hasVendorId) {
                        HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildVendorCommandWithId(device.getDeviceInfo().getLogicalAddress(), targetAddress, HdmiControlService.this.getVendorId(), params));
                    } else {
                        HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildVendorCommand(device.getDeviceInfo().getLogicalAddress(), targetAddress, params));
                    }
                }
            });
        }

        public void sendStandby(final int deviceType, final int deviceId) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass14 */

                @Override // java.lang.Runnable
                public void run() {
                    HdmiMhlLocalDeviceStub mhlDevice = HdmiControlService.this.mMhlController.getLocalDeviceById(deviceId);
                    if (mhlDevice != null) {
                        mhlDevice.sendStandby();
                        return;
                    }
                    HdmiCecLocalDevice device = HdmiControlService.this.mCecController.getLocalDevice(deviceType);
                    if (device == null) {
                        device = HdmiControlService.this.audioSystem();
                    }
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
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass15 */

                @Override // java.lang.Runnable
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
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass16 */

                @Override // java.lang.Runnable
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
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass17 */

                @Override // java.lang.Runnable
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
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass18 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!HdmiControlService.this.isTvDeviceEnabled()) {
                        Slog.w(HdmiControlService.TAG, "TV device is not enabled.");
                    } else {
                        HdmiControlService.this.tv().clearTimerRecording(recorderAddress, sourceType, recordSource);
                    }
                }
            });
        }

        public void sendMhlVendorCommand(final int portId, final int offset, final int length, final byte[] data) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass19 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!HdmiControlService.this.isControlEnabled()) {
                        Slog.w(HdmiControlService.TAG, "Hdmi control is disabled.");
                    } else if (HdmiControlService.this.mMhlController.getLocalDevice(portId) == null) {
                        Slog.w(HdmiControlService.TAG, "Invalid port id:" + portId);
                    } else {
                        HdmiControlService.this.mMhlController.sendVendorCommand(portId, offset, length, data);
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
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass20 */

                @Override // java.lang.Runnable
                public void run() {
                    HdmiControlService.this.setStandbyMode(isStandbyModeOn);
                }
            });
        }

        public void reportAudioStatus(final int deviceType, int volume, int maxVolume, boolean isMute) {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass21 */

                @Override // java.lang.Runnable
                public void run() {
                    if (HdmiControlService.this.mCecController.getLocalDevice(deviceType) == null) {
                        Slog.w(HdmiControlService.TAG, "Local device not available");
                    } else if (HdmiControlService.this.audioSystem() == null) {
                        Slog.w(HdmiControlService.TAG, "audio system is not available");
                    } else if (!HdmiControlService.this.audioSystem().isSystemAudioActivated()) {
                        Slog.w(HdmiControlService.TAG, "audio system is not in system audio mode");
                    } else {
                        HdmiControlService.this.audioSystem().reportAudioStatus(0);
                    }
                }
            });
        }

        public void setSystemAudioModeOnForAudioOnlySource() {
            HdmiControlService.this.enforceAccessPermission();
            HdmiControlService.this.runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.BinderService.AnonymousClass22 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!HdmiControlService.this.isAudioSystemDevice()) {
                        Slog.e(HdmiControlService.TAG, "Not an audio system device. Won't set system audio mode on");
                    } else if (HdmiControlService.this.audioSystem() == null) {
                        Slog.e(HdmiControlService.TAG, "Audio System local device is not registered");
                    } else if (!HdmiControlService.this.audioSystem().checkSupportAndSetSystemAudioMode(true)) {
                        Slog.e(HdmiControlService.TAG, "System Audio Mode is not supported.");
                    } else {
                        HdmiControlService.this.sendCecCommand(HdmiCecMessageBuilder.buildSetSystemAudioMode(HdmiControlService.this.audioSystem().mAddress, 15, true));
                    }
                }
            });
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            if (DumpUtils.checkDumpPermission(HdmiControlService.this.getContext(), HdmiControlService.TAG, writer)) {
                IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
                pw.println("mProhibitMode: " + HdmiControlService.this.mProhibitMode);
                pw.println("mPowerStatus: " + HdmiControlService.this.mPowerStatus);
                pw.println("System_settings:");
                pw.increaseIndent();
                pw.println("mHdmiControlEnabled: " + HdmiControlService.this.mHdmiControlEnabled);
                pw.println("mMhlInputChangeEnabled: " + HdmiControlService.this.mMhlInputChangeEnabled);
                pw.println("mSystemAudioActivated: " + HdmiControlService.this.isSystemAudioActivated());
                pw.decreaseIndent();
                pw.println("mMhlController: ");
                pw.increaseIndent();
                HdmiControlService.this.mMhlController.dump(pw);
                pw.decreaseIndent();
                HdmiUtils.dumpIterable(pw, "mPortInfo:", HdmiControlService.this.mPortInfo);
                if (HdmiControlService.this.mCecController != null) {
                    pw.println("mCecController: ");
                    pw.increaseIndent();
                    HdmiControlService.this.mCecController.dump(pw);
                    pw.decreaseIndent();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getRemoteControlSourceAddress() {
        if (isAudioSystemDevice()) {
            return audioSystem().getDeviceInfo().getLogicalAddress();
        }
        if (isPlaybackDevice()) {
            return playback().getDeviceInfo().getLogicalAddress();
        }
        return 15;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HdmiCecLocalDeviceSource getSwitchDevice() {
        if (isAudioSystemDevice()) {
            return audioSystem();
        }
        if (isPlaybackDevice()) {
            return playback();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private void oneTouchPlay(IHdmiControlCallback callback) {
        assertRunOnServiceThread();
        HdmiCecLocalDeviceSource source = playback();
        if (source == null) {
            source = audioSystem();
        }
        if (source == null) {
            Slog.w(TAG, "Local source device not available");
            invokeCallback(callback, 2);
            return;
        }
        source.oneTouchPlay(callback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addHotplugEventListener(final IHdmiHotplugEventListener listener) {
        final HotplugEventListenerRecord record = new HotplugEventListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, 0);
            synchronized (this.mLock) {
                this.mHotplugEventListenerRecords.add(record);
            }
            runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiControlService.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    synchronized (HdmiControlService.this.mLock) {
                        if (!HdmiControlService.this.mHotplugEventListenerRecords.contains(record)) {
                            return;
                        }
                    }
                    for (HdmiPortInfo port : HdmiControlService.this.mPortInfo) {
                        HdmiHotplugEvent event = new HdmiHotplugEvent(port.getId(), HdmiControlService.this.mCecController.isConnected(port.getId()));
                        synchronized (HdmiControlService.this.mLock) {
                            HdmiControlService.this.invokeHotplugEventListenerLocked(listener, event);
                        }
                    }
                }
            });
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeHotplugEventListener(IHdmiHotplugEventListener listener) {
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
    /* access modifiers changed from: public */
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
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {
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
    public final class InputChangeListenerRecord implements IBinder.DeathRecipient {
        private final IHdmiInputChangeListener mListener;

        public InputChangeListenerRecord(IHdmiInputChangeListener listener) {
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HdmiControlService.this.mLock) {
                if (HdmiControlService.this.mInputChangeListenerRecord == this) {
                    HdmiControlService.this.mInputChangeListenerRecord = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
    /* access modifiers changed from: public */
    private void setHdmiRecordListener(IHdmiRecordListener listener) {
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
                    return this.mRecordListenerRecord.mListener.getOneTouchRecordSource(recorderAddress);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to start record.", e);
                }
            }
            return EmptyArray.BYTE;
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
    /* access modifiers changed from: public */
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
            Iterator<HotplugEventListenerRecord> it = this.mHotplugEventListenerRecords.iterator();
            while (it.hasNext()) {
                invokeHotplugEventListenerLocked(it.next().mListener, event);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: package-private */
    public boolean isTvDevice() {
        return this.mLocalDevices.contains(0);
    }

    /* access modifiers changed from: package-private */
    public boolean isAudioSystemDevice() {
        return this.mLocalDevices.contains(5);
    }

    /* access modifiers changed from: package-private */
    public boolean isPlaybackDevice() {
        return this.mLocalDevices.contains(4);
    }

    /* access modifiers changed from: package-private */
    public boolean isSwitchDevice() {
        return SystemProperties.getBoolean("ro.hdmi.property_is_device_hdmi_cec_switch", false);
    }

    /* access modifiers changed from: package-private */
    public boolean isTvDeviceEnabled() {
        return isTvDevice() && tv() != null;
    }

    /* access modifiers changed from: protected */
    public HdmiCecLocalDevicePlayback playback() {
        return (HdmiCecLocalDevicePlayback) this.mCecController.getLocalDevice(4);
    }

    public HdmiCecLocalDeviceAudioSystem audioSystem() {
        return (HdmiCecLocalDeviceAudioSystem) this.mCecController.getLocalDevice(5);
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
        int i = this.mPowerStatus;
        return i == 0 || i == 2;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isPowerStandbyOrTransient() {
        assertRunOnServiceThread();
        int i = this.mPowerStatus;
        return i == 1 || i == 3;
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
        this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 8, "android.server.hdmi:WAKE");
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isStandbyMessageReceived() {
        return this.mStandbyMessageReceived;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
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

    /* access modifiers changed from: protected */
    @HdmiAnnotations.ServiceThreadOnly
    @VisibleForTesting
    public void onStandby(final int standbyAction) {
        assertRunOnServiceThread();
        this.mPowerStatus = 3;
        invokeVendorCommandListenersOnControlStateChanged(false, 3);
        final List<HdmiCecLocalDevice> devices = getAllLocalDevices();
        if (isStandbyMessageReceived() || canGoToStandby()) {
            disableDevices(new HdmiCecLocalDevice.PendingActionClearedCallback() {
                /* class com.android.server.hdmi.HdmiControlService.AnonymousClass3 */

                @Override // com.android.server.hdmi.HdmiCecLocalDevice.PendingActionClearedCallback
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
        for (HdmiCecLocalDevice device : devices) {
            device.onStandby(this.mStandbyMessageReceived, standbyAction);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private void onLanguageChanged(String language) {
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
        HdmiCecController hdmiCecController = this.mCecController;
        if (hdmiCecController != null) {
            for (HdmiCecLocalDevice device : hdmiCecController.getLocalDeviceList()) {
                device.disableDevice(this.mStandbyMessageReceived, callback);
            }
        }
        this.mMhlController.clearAllLocalDevices();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private void clearLocalDevices() {
        assertRunOnServiceThread();
        HdmiCecController hdmiCecController = this.mCecController;
        if (hdmiCecController != null) {
            hdmiCecController.clearLogicalAddress();
            this.mCecController.clearLocalDevices();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private void onStandbyCompleted(int standbyAction) {
        assertRunOnServiceThread();
        Slog.v(TAG, "onStandbyCompleted");
        if (this.mPowerStatus == 3) {
            this.mPowerStatus = 1;
            for (HdmiCecLocalDevice device : this.mCecController.getLocalDeviceList()) {
                device.onStandby(this.mStandbyMessageReceived, standbyAction);
            }
            this.mStandbyMessageReceived = false;
            if (!isAudioSystemDevice()) {
                this.mCecController.setOption(3, false);
                this.mMhlController.setOption(104, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
    /* access modifiers changed from: public */
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
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 8, "android.server.hdmi:WAKE");
            if (playback() != null) {
                oneTouchPlay(new IHdmiControlCallback.Stub() {
                    /* class com.android.server.hdmi.HdmiControlService.AnonymousClass4 */

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
    public boolean isSystemAudioActivated() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSystemAudioActivated;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void setSystemAudioActivated(boolean on) {
        synchronized (this.mLock) {
            this.mSystemAudioActivated = on;
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
            /* class com.android.server.hdmi.HdmiControlService.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                HdmiControlService.this.disableHdmiControlService();
            }
        });
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void enableHdmiControlService() {
        this.mCecController.setOption(2, true);
        this.mCecController.setOption(3, true);
        this.mMhlController.setOption(103, 1);
        initializeCec(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private void disableHdmiControlService() {
        disableDevices(new HdmiCecLocalDevice.PendingActionClearedCallback() {
            /* class com.android.server.hdmi.HdmiControlService.AnonymousClass6 */

            @Override // com.android.server.hdmi.HdmiCecLocalDevice.PendingActionClearedCallback
            public void onCleared(HdmiCecLocalDevice device) {
                HdmiControlService.this.assertRunOnServiceThread();
                HdmiControlService.this.mCecController.flush(new Runnable() {
                    /* class com.android.server.hdmi.HdmiControlService.AnonymousClass6.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HdmiControlService.this.mCecController.setOption(2, false);
                        HdmiControlService.this.mCecController.setOption(3, false);
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
    public HdmiCecLocalDevice.ActiveSource getActiveSource() {
        HdmiCecLocalDevice.ActiveSource activeSource;
        synchronized (this.mLock) {
            activeSource = this.mActiveSource;
        }
        return activeSource;
    }

    /* access modifiers changed from: package-private */
    public void setActiveSource(int logicalAddress, int physicalAddress) {
        synchronized (this.mLock) {
            this.mActiveSource.logicalAddress = logicalAddress;
            this.mActiveSource.physicalAddress = physicalAddress;
        }
    }

    /* access modifiers changed from: protected */
    public void setAndBroadcastActiveSource(int physicalAddress, int deviceType, int source) {
        if (deviceType == 4) {
            HdmiCecLocalDevicePlayback playback = playback();
            playback.setIsActiveSource(true);
            playback.wakeUpIfActiveSource();
            playback.maySendActiveSource(source);
            setActiveSource(playback.mAddress, physicalAddress);
        }
        if (deviceType == 5) {
            HdmiCecLocalDeviceAudioSystem audioSystem = audioSystem();
            if (playback() != null) {
                audioSystem.setIsActiveSource(false);
                return;
            }
            audioSystem.setIsActiveSource(true);
            audioSystem.wakeUpIfActiveSource();
            audioSystem.maySendActiveSource(source);
            setActiveSource(audioSystem.mAddress, physicalAddress);
        }
    }

    /* access modifiers changed from: protected */
    public void setAndBroadcastActiveSourceFromOneDeviceType(int sourceAddress, int physicalAddress) {
        HdmiCecLocalDevicePlayback playback = playback();
        HdmiCecLocalDeviceAudioSystem audioSystem = audioSystem();
        if (playback != null) {
            playback.setIsActiveSource(true);
            playback.wakeUpIfActiveSource();
            playback.maySendActiveSource(sourceAddress);
            if (audioSystem != null) {
                audioSystem.setIsActiveSource(false);
            }
            setActiveSource(playback.mAddress, physicalAddress);
        } else if (audioSystem != null) {
            audioSystem.setIsActiveSource(true);
            audioSystem.wakeUpIfActiveSource();
            audioSystem.maySendActiveSource(sourceAddress);
            setActiveSource(audioSystem.mAddress, physicalAddress);
        }
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
                    /* class com.android.server.hdmi.HdmiControlService.AnonymousClass7 */

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
