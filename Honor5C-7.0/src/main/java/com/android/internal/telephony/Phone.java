package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.encrypt.PasswordUtil;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.telecom.VideoProfile;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.text.TextUtils;
import android.util.Base64;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.imsphone.HwImsPhone;
import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Phone extends AbstractPhoneBase implements PhoneInternalInterface {
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = null;
    private static final String CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_non_roaming_list_";
    private static final String CDMA_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_roaming_list_";
    public static final String CF_ENABLED = "cf_enabled_key";
    public static final String CF_ID = "cf_id_key";
    public static final String CF_STATUS = "cf_status_key";
    public static final String CLIR_KEY = "clir_key";
    public static final String CS_FALLBACK = "cs_fallback";
    public static final String DATA_DISABLED_ON_BOOT_KEY = "disabled_on_boot_key";
    private static final int DEFAULT_REPORT_INTERVAL_MS = 200;
    private static final String DNS_SERVER_CHECK_DISABLED_KEY = "dns_server_check_disabled_key";
    protected static final int EVENT_CALL_RING = 14;
    private static final int EVENT_CALL_RING_CONTINUE = 15;
    protected static final int EVENT_CARRIER_CONFIG_CHANGED = 43;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 27;
    private static final int EVENT_CHECK_FOR_NETWORK_AUTOMATIC = 38;
    private static final int EVENT_CONFIG_LCE = 37;
    protected static final int EVENT_EMERGENCY_CALLBACK_MODE_ENTER = 25;
    protected static final int EVENT_EXIT_EMERGENCY_CALLBACK_RESPONSE = 26;
    protected static final int EVENT_GET_BASEBAND_VERSION_DONE = 6;
    protected static final int EVENT_GET_CALLFORWARDING_STATUS = 44;
    protected static final int EVENT_GET_CALL_FORWARD_DONE = 13;
    public static final int EVENT_GET_DEVICE_IDENTITY_DONE = 21;
    protected static final int EVENT_GET_IMEISV_DONE = 10;
    public static final int EVENT_GET_IMEI_DONE = 9;
    protected static final int EVENT_GET_RADIO_CAPABILITY = 35;
    private static final int EVENT_GET_SIM_STATUS_DONE = 11;
    protected static final int EVENT_ICC_CHANGED = 30;
    protected static final int EVENT_ICC_RECORD_EVENTS = 29;
    private static final int EVENT_INITIATE_SILENT_REDIAL = 32;
    protected static final int EVENT_LAST = 44;
    private static final int EVENT_MMI_DONE = 4;
    protected static final int EVENT_NV_READY = 23;
    public static final int EVENT_RADIO_AVAILABLE = 1;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 33;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 8;
    protected static final int EVENT_RADIO_ON = 5;
    protected static final int EVENT_REGISTERED_TO_NETWORK = 19;
    protected static final int EVENT_REQUEST_VOICE_RADIO_TECH_DONE = 40;
    protected static final int EVENT_RIL_CONNECTED = 41;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 22;
    protected static final int EVENT_SET_CALL_FORWARD_DONE = 12;
    protected static final int EVENT_SET_CLIR_COMPLETE = 18;
    private static final int EVENT_SET_ENHANCED_VP = 24;
    protected static final int EVENT_SET_NETWORK_AUTOMATIC = 28;
    private static final int EVENT_SET_NETWORK_AUTOMATIC_COMPLETE = 17;
    private static final int EVENT_SET_NETWORK_MANUAL_COMPLETE = 16;
    protected static final int EVENT_SET_ROAMING_PREFERENCE_DONE = 44;
    protected static final int EVENT_SET_VM_NUMBER_DONE = 20;
    protected static final int EVENT_SIM_RECORDS_LOADED = 3;
    private static final int EVENT_SRVCC_STATE_CHANGED = 31;
    protected static final int EVENT_SS = 36;
    protected static final int EVENT_SSN = 2;
    private static final int EVENT_UNSOL_OEM_HOOK_RAW = 34;
    protected static final int EVENT_UPDATE_PHONE_OBJECT = 42;
    protected static final int EVENT_USSD = 7;
    protected static final int EVENT_VOICE_RADIO_TECH_CHANGED = 39;
    public static final String EXTRA_KEY_ALERT_MESSAGE = "alertMessage";
    public static final String EXTRA_KEY_ALERT_SHOW = "alertShow";
    public static final String EXTRA_KEY_ALERT_TITLE = "alertTitle";
    protected static final String EXTRA_KEY_NOTIFICATION_MESSAGE = "notificationMessage";
    protected static final boolean FAST_PREF_NET_REG = false;
    private static final String GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_non_roaming_list_";
    private static final String GSM_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_roaming_list_";
    private static final boolean LCE_PULL_MODE = true;
    private static final String LOG_TAG_STATIC = "Phone";
    public static final String NETWORK_SELECTION_KEY = "network_selection_key";
    public static final String NETWORK_SELECTION_NAME_KEY = "network_selection_name_key";
    public static final String NETWORK_SELECTION_SHORT_KEY = "network_selection_short_key";
    public static final String SIM_IMSI = "sim_imsi_key";
    private static final String VM_COUNT = "vm_count_key";
    private static final String VM_ID = "vm_id_key";
    public static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    protected static final Object lockForRadioTechnologyChange = null;
    private static PasswordUtil mPasswordUtil;
    private String LOG_TAG;
    private final String mActionAttached;
    private final String mActionDetached;
    private int mCallRingContinueToken;
    private int mCallRingDelay;
    public CommandsInterface mCi;
    protected final Context mContext;
    public DcTracker mDcTracker;
    protected final RegistrantList mDisconnectRegistrants;
    private boolean mDnsCheckDisabled;
    private boolean mDoesRilSendMultipleCallRing;
    protected final RegistrantList mEmergencyCallToggledRegistrants;
    private final RegistrantList mHandoverRegistrants;
    public HwImsPhone mHwImsPhone;
    public final AtomicReference<IccRecords> mIccRecords;
    private BroadcastReceiver mImsIntentReceiver;
    protected Phone mImsPhone;
    private boolean mImsServiceReady;
    private final RegistrantList mIncomingRingRegistrants;
    protected boolean mIsVideoCapable;
    private boolean mIsVoiceCapable;
    private int mLceStatus;
    private Looper mLooper;
    protected final RegistrantList mMccChangedRegistrants;
    protected final RegistrantList mMmiCompleteRegistrants;
    protected final RegistrantList mMmiRegistrants;
    private String mName;
    private final RegistrantList mNewRingingConnectionRegistrants;
    protected PhoneNotifier mNotifier;
    protected int mPhoneId;
    protected Registrant mPostDialHandler;
    private final RegistrantList mPreciseCallStateRegistrants;
    private final AtomicReference<RadioCapability> mRadioCapability;
    protected final RegistrantList mRadioOffOrNotAvailableRegistrants;
    private final RegistrantList mServiceStateRegistrants;
    protected final RegistrantList mSimRecordsLoadedRegistrants;
    protected SimulatedRadioControl mSimulatedRadioControl;
    public SmsStorageMonitor mSmsStorageMonitor;
    public SmsUsageMonitor mSmsUsageMonitor;
    protected final RegistrantList mSuppServiceFailedRegistrants;
    protected TelephonyComponentFactory mTelephonyComponentFactory;
    private TelephonyTester mTelephonyTester;
    protected AtomicReference<UiccCardApplication> mUiccApplication;
    protected UiccController mUiccController;
    private boolean mUnitTestMode;
    protected final RegistrantList mUnknownConnectionRegistrants;
    private final RegistrantList mVideoCapabilityChangedRegistrants;
    protected int mVmCount;

    private static class NetworkSelectMessage {
        public Message message;
        public String operatorAlphaLong;
        public String operatorAlphaShort;
        public String operatorNumeric;

        private NetworkSelectMessage() {
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues;
        }
        int[] iArr = new int[AppType.values().length];
        try {
            iArr[AppType.APPTYPE_CSIM.ordinal()] = EVENT_RADIO_AVAILABLE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppType.APPTYPE_ISIM.ordinal()] = EVENT_SSN;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppType.APPTYPE_RUIM.ordinal()] = EVENT_SIM_RECORDS_LOADED;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppType.APPTYPE_SIM.ordinal()] = EVENT_MMI_DONE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = EVENT_GET_BASEBAND_VERSION_DONE;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = EVENT_RADIO_ON;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.Phone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.Phone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Phone.<clinit>():void");
    }

    public abstract int getPhoneType();

    public abstract State getState();

    protected abstract void onUpdateIccAvailability();

    public abstract void sendEmergencyCallStateChange(boolean z);

    public abstract void setBroadcastEmergencyCallStateChanges(boolean z);

    public IccRecords getIccRecords() {
        return (IccRecords) this.mIccRecords.get();
    }

    public String getPhoneName() {
        return this.mName;
    }

    protected void setPhoneName(String name) {
        this.mName = name;
    }

    public String getNai() {
        return null;
    }

    public String getActionDetached() {
        return this.mActionDetached;
    }

    public String getActionAttached() {
        return this.mActionAttached;
    }

    public void setSystemProperty(String property, String value) {
        if (!getUnitTestMode()) {
            SystemProperties.set(property, value);
        }
    }

    public String getSystemProperty(String property, String defValue) {
        if (getUnitTestMode()) {
            return null;
        }
        return SystemProperties.get(property, defValue);
    }

    protected Phone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode) {
        this(name, notifier, context, ci, unitTestMode, 0, TelephonyComponentFactory.getInstance());
    }

    protected Phone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode, int phoneId, TelephonyComponentFactory telephonyComponentFactory) {
        super(ci);
        this.LOG_TAG = LOG_TAG_STATIC;
        this.mImsIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Rlog.d(Phone.this.LOG_TAG, "mImsIntentReceiver: action " + intent.getAction());
                if (intent.hasExtra("android:phone_id")) {
                    int extraPhoneId = intent.getIntExtra("android:phone_id", -1);
                    Rlog.d(Phone.this.LOG_TAG, "mImsIntentReceiver: extraPhoneId = " + extraPhoneId);
                    if (extraPhoneId == -1 || extraPhoneId != Phone.this.getPhoneId()) {
                        return;
                    }
                }
                synchronized (Phone.lockForRadioTechnologyChange) {
                    if (intent.getAction().equals("com.android.ims.IMS_SERVICE_UP")) {
                        ImsManager imsManager = ImsManager.getInstance(Phone.this.mContext, Phone.this.getPhoneId());
                        if (imsManager != null) {
                            Phone.this.mImsServiceReady = imsManager.isServiceAvailable();
                        } else {
                            Phone.this.mImsServiceReady = Phone.LCE_PULL_MODE;
                        }
                        Phone.this.updateImsPhone();
                        ImsManager.updateImsServiceConfig(Phone.this.mContext, Phone.this.mPhoneId, Phone.FAST_PREF_NET_REG);
                    } else if (intent.getAction().equals("com.android.ims.IMS_SERVICE_DOWN")) {
                        Phone.this.mImsServiceReady = Phone.FAST_PREF_NET_REG;
                        Phone.this.updateImsPhone();
                    }
                }
            }
        };
        this.mVmCount = 0;
        this.mIsVoiceCapable = LCE_PULL_MODE;
        this.mIsVideoCapable = FAST_PREF_NET_REG;
        this.mUiccController = null;
        this.mIccRecords = new AtomicReference();
        this.mUiccApplication = new AtomicReference();
        this.mImsServiceReady = FAST_PREF_NET_REG;
        this.mImsPhone = null;
        this.mRadioCapability = new AtomicReference();
        this.mLceStatus = -1;
        this.mPreciseCallStateRegistrants = new RegistrantList();
        this.mHandoverRegistrants = new RegistrantList();
        this.mNewRingingConnectionRegistrants = new RegistrantList();
        this.mIncomingRingRegistrants = new RegistrantList();
        this.mDisconnectRegistrants = new RegistrantList();
        this.mServiceStateRegistrants = new RegistrantList();
        this.mMmiCompleteRegistrants = new RegistrantList();
        this.mMmiRegistrants = new RegistrantList();
        this.mUnknownConnectionRegistrants = new RegistrantList();
        this.mSuppServiceFailedRegistrants = new RegistrantList();
        this.mRadioOffOrNotAvailableRegistrants = new RegistrantList();
        this.mSimRecordsLoadedRegistrants = new RegistrantList();
        this.mVideoCapabilityChangedRegistrants = new RegistrantList();
        this.mEmergencyCallToggledRegistrants = new RegistrantList();
        this.mMccChangedRegistrants = new RegistrantList();
        this.mPhoneId = phoneId;
        this.mName = name;
        this.mNotifier = notifier;
        this.mContext = context;
        this.mLooper = Looper.myLooper();
        this.mCi = ci;
        this.mActionDetached = getClass().getPackage().getName() + ".action_detached";
        this.mActionAttached = getClass().getPackage().getName() + ".action_attached";
        this.LOG_TAG += "[SUB" + phoneId + "]";
        if (Build.IS_DEBUGGABLE) {
            this.mTelephonyTester = new TelephonyTester(this);
        }
        HwTelephonyFactory.getHwVolteChrManager().init(this.mContext);
        HwTelephonyFactory.getHwPhoneManager().setDefaultTimezone(this.mContext);
        HwTelephonyFactory.getHwDataServiceChrManager().init(this.mContext);
        setUnitTestMode(unitTestMode);
        this.mDnsCheckDisabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DNS_SERVER_CHECK_DISABLED_KEY, FAST_PREF_NET_REG);
        this.mCi.setOnCallRing(this, EVENT_CALL_RING, null);
        this.mCi.setOnECCNum(this, AbstractPhoneBase.EVENT_ECC_NUM, null);
        this.mCi.queryEmergencyNumbers();
        this.mIsVoiceCapable = this.mContext.getResources().getBoolean(17956956);
        this.mDoesRilSendMultipleCallRing = SystemProperties.getBoolean("ro.telephony.call_ring.multiple", LCE_PULL_MODE);
        Rlog.d(this.LOG_TAG, "mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing);
        this.mCallRingDelay = SystemProperties.getInt("ro.telephony.call_ring.delay", 3000);
        Rlog.d(this.LOG_TAG, "mCallRingDelay=" + this.mCallRingDelay);
        if (getPhoneType() != EVENT_RADIO_ON) {
            Locale carrierLocale = getLocaleFromCarrierProperties(this.mContext);
            if (!(carrierLocale == null || TextUtils.isEmpty(carrierLocale.getCountry()))) {
                String country = carrierLocale.getCountry();
                try {
                    Global.getInt(this.mContext.getContentResolver(), "wifi_country_code");
                } catch (SettingNotFoundException e) {
                    ((WifiManager) this.mContext.getSystemService("wifi")).setCountryCode(country, FAST_PREF_NET_REG);
                }
            }
            this.mTelephonyComponentFactory = telephonyComponentFactory;
            this.mSmsStorageMonitor = this.mTelephonyComponentFactory.makeSmsStorageMonitor(this);
            this.mSmsUsageMonitor = this.mTelephonyComponentFactory.makeSmsUsageMonitor(context);
            this.mUiccController = UiccController.getInstance();
            this.mUiccController.registerForIccChanged(this, EVENT_ICC_CHANGED, null);
            if (getPhoneType() != EVENT_SIM_RECORDS_LOADED) {
                this.mCi.registerForSrvccStateChanged(this, EVENT_SRVCC_STATE_CHANGED, null);
            }
            this.mCi.setOnUnsolOemHookRaw(this, EVENT_UNSOL_OEM_HOOK_RAW, null);
            this.mCi.startLceService(DEFAULT_REPORT_INTERVAL_MS, LCE_PULL_MODE, obtainMessage(EVENT_CONFIG_LCE));
        }
    }

    public void startMonitoringImsService() {
        if (getPhoneType() != EVENT_SIM_RECORDS_LOADED) {
            synchronized (lockForRadioTechnologyChange) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("com.android.ims.IMS_SERVICE_UP");
                filter.addAction("com.android.ims.IMS_SERVICE_DOWN");
                this.mContext.registerReceiver(this.mImsIntentReceiver, filter);
                if (getPhoneId() != HwTelephonyFactory.getHwUiccManager().getUserSwitchSlots()) {
                    return;
                }
                ImsManager imsManager = ImsManager.getInstance(this.mContext, getPhoneId());
                if (imsManager != null && imsManager.isServiceAvailable()) {
                    this.mImsServiceReady = LCE_PULL_MODE;
                    updateImsPhone();
                    ImsManager.updateImsServiceConfig(this.mContext, this.mPhoneId, FAST_PREF_NET_REG);
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_SET_NETWORK_MANUAL_COMPLETE /*16*/:
            case EVENT_SET_NETWORK_AUTOMATIC_COMPLETE /*17*/:
                if (msg.what == EVENT_SET_NETWORK_MANUAL_COMPLETE) {
                    setOOSFlagOnSelectNetworkManually(FAST_PREF_NET_REG);
                    restoreNetworkSelectionAuto();
                }
                if (msg.what == EVENT_SET_NETWORK_AUTOMATIC_COMPLETE) {
                    ServiceStateTracker tracker = getServiceStateTracker();
                    if (tracker != null) {
                        tracker.pollState();
                    } else {
                        Rlog.d(this.LOG_TAG, "tracker is null");
                    }
                }
                handleSetSelectNetwork((AsyncResult) msg.obj);
            default:
                AsyncResult ar;
                switch (msg.what) {
                    case EVENT_CALL_RING /*14*/:
                        Rlog.d(this.LOG_TAG, "Event EVENT_CALL_RING Received state=" + getState());
                        if (msg.obj.exception == null) {
                            State state = getState();
                            if (!this.mDoesRilSendMultipleCallRing && (state == State.RINGING || state == State.IDLE)) {
                                this.mCallRingContinueToken += EVENT_RADIO_AVAILABLE;
                                sendIncomingCallRingNotification(this.mCallRingContinueToken);
                                break;
                            }
                            notifyIncomingRing();
                            break;
                        }
                        break;
                    case EVENT_CALL_RING_CONTINUE /*15*/:
                        Rlog.d(this.LOG_TAG, "Event EVENT_CALL_RING_CONTINUE Received state=" + getState());
                        if (getState() == State.RINGING) {
                            sendIncomingCallRingNotification(msg.arg1);
                            break;
                        }
                        break;
                    case EVENT_ICC_CHANGED /*30*/:
                        onUpdateIccAvailability();
                        break;
                    case EVENT_SRVCC_STATE_CHANGED /*31*/:
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            Rlog.e(this.LOG_TAG, "Srvcc exception: " + ar.exception);
                            break;
                        } else {
                            handleSrvccStateChanged((int[]) ar.result);
                            break;
                        }
                    case EVENT_INITIATE_SILENT_REDIAL /*32*/:
                        Rlog.d(this.LOG_TAG, "Event EVENT_INITIATE_SILENT_REDIAL Received");
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null && ar.result != null) {
                            String dialString = ar.result;
                            if (!TextUtils.isEmpty(dialString)) {
                                try {
                                    dialInternal(dialString, null, 0, null);
                                    break;
                                } catch (CallStateException e) {
                                    Rlog.e(this.LOG_TAG, "silent redial failed: " + e);
                                    break;
                                }
                            }
                        }
                    case EVENT_UNSOL_OEM_HOOK_RAW /*34*/:
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            Rlog.e(this.LOG_TAG, "OEM hook raw exception: " + ar.exception);
                            break;
                        }
                        this.mNotifier.notifyOemHookRawEventForSubscriber(getSubId(), ar.result);
                        break;
                    case EVENT_CONFIG_LCE /*37*/:
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            this.mLceStatus = ((Integer) ar.result.get(0)).intValue();
                            break;
                        } else {
                            Rlog.d(this.LOG_TAG, "config LCE service failed: " + ar.exception);
                            break;
                        }
                    case EVENT_CHECK_FOR_NETWORK_AUTOMATIC /*38*/:
                        onCheckForNetworkSelectionModeAutomatic(msg);
                        break;
                    default:
                        throw new RuntimeException("unexpected event not handled");
                }
        }
    }

    public ArrayList<Connection> getHandoverConnection() {
        return null;
    }

    public void notifySrvccState(SrvccState state) {
    }

    public void registerForSilentRedial(Handler h, int what, Object obj) {
    }

    public void unregisterForSilentRedial(Handler h) {
    }

    private void handleSrvccStateChanged(int[] ret) {
        Rlog.d(this.LOG_TAG, "handleSrvccStateChanged");
        ArrayList<Connection> conn = null;
        Phone imsPhone = this.mImsPhone;
        SrvccState srvccState = SrvccState.NONE;
        if (!(ret == null || ret.length == 0)) {
            int state = ret[0];
            switch (state) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                    srvccState = SrvccState.STARTED;
                    if (imsPhone == null) {
                        Rlog.d(this.LOG_TAG, "HANDOVER_STARTED: mImsPhone null");
                        break;
                    }
                    conn = imsPhone.getHandoverConnection();
                    migrateFrom(imsPhone);
                    break;
                case EVENT_RADIO_AVAILABLE /*1*/:
                    srvccState = SrvccState.COMPLETED;
                    if (imsPhone == null) {
                        Rlog.d(this.LOG_TAG, "HANDOVER_COMPLETED: mImsPhone null");
                        break;
                    } else {
                        imsPhone.notifySrvccState(srvccState);
                        break;
                    }
                case EVENT_SSN /*2*/:
                case EVENT_SIM_RECORDS_LOADED /*3*/:
                    srvccState = SrvccState.FAILED;
                    break;
                default:
                    return;
            }
            getCallTracker().notifySrvccState(srvccState, conn);
            notifyVoLteServiceStateChanged(new VoLteServiceState(state));
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public void disableDnsCheck(boolean b) {
        this.mDnsCheckDisabled = b;
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(DNS_SERVER_CHECK_DISABLED_KEY, b);
        editor.apply();
    }

    public boolean isDnsCheckDisabled() {
        return this.mDnsCheckDisabled;
    }

    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mPreciseCallStateRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForPreciseCallStateChanged(Handler h) {
        this.mPreciseCallStateRegistrants.remove(h);
    }

    protected void notifyPreciseCallStateChangedP() {
        this.mPreciseCallStateRegistrants.notifyRegistrants(new AsyncResult(null, this, null));
        this.mNotifier.notifyPreciseCallState(this);
    }

    public void registerForHandoverStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mHandoverRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForHandoverStateChanged(Handler h) {
        this.mHandoverRegistrants.remove(h);
    }

    public void notifyHandoverStateChanged(Connection cn) {
        this.mHandoverRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
    }

    protected void setIsInEmergencyCall() {
    }

    protected void migrateFrom(Phone from) {
        migrate(this.mHandoverRegistrants, from.mHandoverRegistrants);
        migrate(this.mPreciseCallStateRegistrants, from.mPreciseCallStateRegistrants);
        migrate(this.mNewRingingConnectionRegistrants, from.mNewRingingConnectionRegistrants);
        migrate(this.mIncomingRingRegistrants, from.mIncomingRingRegistrants);
        migrate(this.mDisconnectRegistrants, from.mDisconnectRegistrants);
        migrate(this.mServiceStateRegistrants, from.mServiceStateRegistrants);
        migrate(this.mMmiCompleteRegistrants, from.mMmiCompleteRegistrants);
        migrate(this.mMmiRegistrants, from.mMmiRegistrants);
        migrate(this.mUnknownConnectionRegistrants, from.mUnknownConnectionRegistrants);
        migrate(this.mSuppServiceFailedRegistrants, from.mSuppServiceFailedRegistrants);
        if (from.isInEmergencyCall()) {
            setIsInEmergencyCall();
        }
    }

    protected void migrate(RegistrantList to, RegistrantList from) {
        from.removeCleared();
        int n = from.size();
        for (int i = 0; i < n; i += EVENT_RADIO_AVAILABLE) {
            Message msg = ((Registrant) from.get(i)).messageForRegistrant();
            if (msg == null) {
                Rlog.d(this.LOG_TAG, "msg is null");
            } else if (msg.obj != CallManager.getInstance().getRegistrantIdentifier()) {
                to.add((Registrant) from.get(i));
            }
        }
    }

    public void registerForUnknownConnection(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mUnknownConnectionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForUnknownConnection(Handler h) {
        this.mUnknownConnectionRegistrants.remove(h);
    }

    public void registerForNewRingingConnection(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mNewRingingConnectionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForNewRingingConnection(Handler h) {
        this.mNewRingingConnectionRegistrants.remove(h);
    }

    public void registerForVideoCapabilityChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mVideoCapabilityChangedRegistrants.addUnique(h, what, obj);
        notifyForVideoCapabilityChanged(this.mIsVideoCapable);
    }

    public void unregisterForVideoCapabilityChanged(Handler h) {
        this.mVideoCapabilityChangedRegistrants.remove(h);
    }

    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
        this.mCi.registerForInCallVoicePrivacyOn(h, what, obj);
    }

    public void unregisterForInCallVoicePrivacyOn(Handler h) {
        this.mCi.unregisterForInCallVoicePrivacyOn(h);
    }

    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
        this.mCi.registerForInCallVoicePrivacyOff(h, what, obj);
    }

    public void unregisterForInCallVoicePrivacyOff(Handler h) {
        this.mCi.unregisterForInCallVoicePrivacyOff(h);
    }

    public void registerForIncomingRing(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mIncomingRingRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForIncomingRing(Handler h) {
        this.mIncomingRingRegistrants.remove(h);
    }

    public void registerForDisconnect(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mDisconnectRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForDisconnect(Handler h) {
        this.mDisconnectRegistrants.remove(h);
    }

    public void registerForSuppServiceFailed(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mSuppServiceFailedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSuppServiceFailed(Handler h) {
        this.mSuppServiceFailedRegistrants.remove(h);
    }

    public void registerForMmiInitiate(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mMmiRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForMmiInitiate(Handler h) {
        this.mMmiRegistrants.remove(h);
    }

    public void registerForMmiComplete(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mMmiCompleteRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForMmiComplete(Handler h) {
        checkCorrectThread(h);
        this.mMmiCompleteRegistrants.remove(h);
    }

    public void registerForSimRecordsLoaded(Handler h, int what, Object obj) {
    }

    public void unregisterForSimRecordsLoaded(Handler h) {
    }

    public void registerForTtyModeReceived(Handler h, int what, Object obj) {
    }

    public void unregisterForTtyModeReceived(Handler h) {
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        Rlog.d(this.LOG_TAG, "setNetworkSelectionModeAutomatic, querying current mode");
        Message msg = obtainMessage(EVENT_CHECK_FOR_NETWORK_AUTOMATIC);
        msg.obj = response;
        this.mCi.getNetworkSelectionMode(msg);
    }

    private void onCheckForNetworkSelectionModeAutomatic(Message fromRil) {
        AsyncResult ar = fromRil.obj;
        Message response = ar.userObj;
        boolean doAutomatic = LCE_PULL_MODE;
        if (ar.exception == null && ar.result != null) {
            try {
                if (ar.result[0] == 0) {
                    doAutomatic = FAST_PREF_NET_REG;
                }
            } catch (Exception e) {
            }
        }
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = "";
        nsm.operatorAlphaLong = "";
        nsm.operatorAlphaShort = "";
        if (doAutomatic) {
            this.mCi.setNetworkSelectionModeAutomatic(obtainMessage(EVENT_SET_NETWORK_AUTOMATIC_COMPLETE, nsm));
        } else {
            Rlog.d(this.LOG_TAG, "setNetworkSelectionModeAutomatic - already auto, ignoring");
            ar.userObj = nsm;
            handleSetSelectNetwork(ar);
        }
        updateSavedNetworkOperator(nsm);
    }

    public void getNetworkSelectionMode(Message message) {
        this.mCi.getNetworkSelectionMode(message);
    }

    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = network.getOperatorNumeric();
        nsm.operatorAlphaLong = network.getOperatorAlphaLong();
        nsm.operatorAlphaShort = network.getOperatorAlphaShort();
        setOOSFlagOnSelectNetworkManually(LCE_PULL_MODE);
        hasNetworkSelectionAuto();
        this.mCi.setNetworkSelectionModeManual(network.getOperatorNumeric(), obtainMessage(EVENT_SET_NETWORK_MANUAL_COMPLETE, nsm));
        if (persistSelection) {
            updateSavedNetworkOperator(nsm);
        } else {
            clearSavedNetworkSelection();
        }
    }

    public void registerForEmergencyCallToggle(Handler h, int what, Object obj) {
        this.mEmergencyCallToggledRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEmergencyCallToggle(Handler h) {
        this.mEmergencyCallToggledRegistrants.remove(h);
    }

    private void updateSavedNetworkOperator(NetworkSelectMessage nsm) {
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putString(NETWORK_SELECTION_KEY + subId, nsm.operatorNumeric);
            editor.putString(NETWORK_SELECTION_NAME_KEY + subId, nsm.operatorAlphaLong);
            editor.putString(NETWORK_SELECTION_SHORT_KEY + subId, nsm.operatorAlphaShort);
            if (!editor.commit()) {
                Rlog.e(this.LOG_TAG, "failed to commit network selection preference");
                return;
            }
            return;
        }
        Rlog.e(this.LOG_TAG, "Cannot update network selection preference due to invalid subId " + subId);
    }

    private void handleSetSelectNetwork(AsyncResult ar) {
        if (ar == null || ar.userObj == null || !(ar.userObj instanceof NetworkSelectMessage)) {
            Rlog.e(this.LOG_TAG, "unexpected result from user object.");
            return;
        }
        NetworkSelectMessage nsm = ar.userObj;
        if (nsm.message != null) {
            AsyncResult.forMessage(nsm.message, ar.result, ar.exception);
            nsm.message.sendToTarget();
        }
    }

    private OperatorInfo getSavedNetworkSelection() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return new OperatorInfo(sp.getString(NETWORK_SELECTION_NAME_KEY + getSubId(), ""), sp.getString(NETWORK_SELECTION_SHORT_KEY + getSubId(), ""), sp.getString(NETWORK_SELECTION_KEY + getSubId(), ""));
    }

    private void clearSavedNetworkSelection() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().remove(NETWORK_SELECTION_KEY + getSubId()).remove(NETWORK_SELECTION_NAME_KEY + getSubId()).remove(NETWORK_SELECTION_SHORT_KEY + getSubId()).commit();
    }

    public void restoreSavedNetworkSelection(Message response) {
        OperatorInfo networkSelection = getSavedNetworkSelection();
        if (networkSelection != null && !TextUtils.isEmpty(networkSelection.getOperatorNumeric())) {
            selectNetworkManually(networkSelection, LCE_PULL_MODE, response);
        }
    }

    public void saveClirSetting(int commandInterfaceCLIRMode) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putInt(CLIR_KEY + getPhoneId(), commandInterfaceCLIRMode);
        if (!editor.commit()) {
            Rlog.e(this.LOG_TAG, "Failed to commit CLIR preference");
        }
    }

    private void setUnitTestMode(boolean f) {
        this.mUnitTestMode = f;
    }

    public boolean getUnitTestMode() {
        return this.mUnitTestMode;
    }

    protected void notifyDisconnectP(Connection cn) {
        this.mDisconnectRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
    }

    public void registerForServiceStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mServiceStateRegistrants.add(h, what, obj);
    }

    public void unregisterForServiceStateChanged(Handler h) {
        this.mServiceStateRegistrants.remove(h);
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mCi.registerForRingbackTone(h, what, obj);
    }

    public void unregisterForRingbackTone(Handler h) {
        this.mCi.unregisterForRingbackTone(h);
    }

    public void registerForOnHoldTone(Handler h, int what, Object obj) {
    }

    public void unregisterForOnHoldTone(Handler h) {
    }

    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        this.mCi.registerForResendIncallMute(h, what, obj);
    }

    public void unregisterForResendIncallMute(Handler h) {
        this.mCi.unregisterForResendIncallMute(h);
    }

    public void setEchoSuppressionEnabled() {
    }

    public void notifyServiceStateChangedP(ServiceState ss) {
        this.mServiceStateRegistrants.notifyRegistrants(new AsyncResult(null, ss, null));
        this.mNotifier.notifyServiceState(this);
    }

    public SimulatedRadioControl getSimulatedRadioControl() {
        return this.mSimulatedRadioControl;
    }

    private void checkCorrectThread(Handler h) {
        if (h.getLooper() != this.mLooper) {
            throw new RuntimeException("com.android.internal.telephony.Phone must be used from within one thread");
        }
    }

    private static Locale getLocaleFromCarrierProperties(Context ctx) {
        String carrier = SystemProperties.get("ro.carrier");
        if (carrier == null || carrier.length() == 0 || "unknown".equals(carrier)) {
            return null;
        }
        CharSequence[] carrierLocales = ctx.getResources().getTextArray(17236052);
        for (int i = 0; i < carrierLocales.length; i += EVENT_SIM_RECORDS_LOADED) {
            if (carrier.equals(carrierLocales[i].toString())) {
                return Locale.forLanguageTag(carrierLocales[i + EVENT_RADIO_AVAILABLE].toString().replace('_', '-'));
            }
        }
        return null;
    }

    public IccFileHandler getIccFileHandler() {
        IccFileHandler iccFileHandler;
        UiccCardApplication uiccApplication = (UiccCardApplication) this.mUiccApplication.get();
        if (uiccApplication == null) {
            Rlog.d(this.LOG_TAG, "getIccFileHandler: uiccApplication == null, return null");
            iccFileHandler = null;
        } else {
            iccFileHandler = uiccApplication.getIccFileHandler();
        }
        Rlog.d(this.LOG_TAG, "getIccFileHandler: fh=" + iccFileHandler);
        return iccFileHandler;
    }

    public Handler getHandler() {
        return this;
    }

    public void updatePhoneObject(int voiceRadioTech) {
    }

    public ServiceStateTracker getServiceStateTracker() {
        return null;
    }

    public CallTracker getCallTracker() {
        return null;
    }

    public void updateVoiceMail() {
        Rlog.e(this.LOG_TAG, "updateVoiceMail() should be overridden");
    }

    public AppType getCurrentUiccAppType() {
        UiccCardApplication currentApp = (UiccCardApplication) this.mUiccApplication.get();
        if (currentApp != null) {
            return currentApp.getType();
        }
        return AppType.APPTYPE_UNKNOWN;
    }

    public IccCard getIccCard() {
        return null;
    }

    public String getIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    public String getFullIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getFullIccId();
        }
        return null;
    }

    public boolean getIccRecordsLoaded() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        return r != null ? r.getRecordsLoaded() : FAST_PREF_NET_REG;
    }

    public List<CellInfo> getAllCellInfo() {
        return privatizeCellInfoList(getServiceStateTracker().getAllCellInfo());
    }

    private List<CellInfo> privatizeCellInfoList(List<CellInfo> cellInfoList) {
        if (cellInfoList == null) {
            return new ArrayList(0);
        }
        if (Secure.getInt(getContext().getContentResolver(), "location_mode", 0) == 0) {
            ArrayList<CellInfo> privateCellInfoList = new ArrayList(cellInfoList.size());
            for (CellInfo c : cellInfoList) {
                if (c instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) c;
                    CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
                    CellIdentityCdma maskedCellIdentity = new CellIdentityCdma(cellIdentity.getNetworkId(), cellIdentity.getSystemId(), cellIdentity.getBasestationId(), Integer.MAX_VALUE, Integer.MAX_VALUE);
                    CellInfoCdma privateCellInfoCdma = new CellInfoCdma(cellInfoCdma);
                    privateCellInfoCdma.setCellIdentity(maskedCellIdentity);
                    privateCellInfoList.add(privateCellInfoCdma);
                } else {
                    privateCellInfoList.add(c);
                }
            }
            cellInfoList = privateCellInfoList;
        }
        return cellInfoList;
    }

    public void setCellInfoListRate(int rateInMillis) {
        this.mCi.setCellInfoListRate(rateInMillis, null);
    }

    public void setRadioPower(boolean power, Message msg) {
    }

    public boolean getMessageWaitingIndicator() {
        return this.mVmCount != 0 ? LCE_PULL_MODE : FAST_PREF_NET_REG;
    }

    private int getCallForwardingIndicatorFromSharedPref() {
        boolean z = LCE_PULL_MODE;
        int status = 0;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            status = sp.getInt(CF_STATUS + subId, -1);
            Rlog.d(this.LOG_TAG, "getCallForwardingIndicatorFromSharedPref: for subId " + subId + "= " + status);
            if (status == -1) {
                String subscriberId = sp.getString(CF_ID, null);
                if (subscriberId != null) {
                    if (subscriberId.equals(getSubscriberId())) {
                        status = sp.getInt(CF_STATUS, 0);
                        if (status != EVENT_RADIO_AVAILABLE) {
                            z = FAST_PREF_NET_REG;
                        }
                        setCallForwardingIndicatorInSharedPref(z);
                        Rlog.d(this.LOG_TAG, "getCallForwardingIndicatorFromSharedPref: " + status);
                    } else {
                        Rlog.d(this.LOG_TAG, "getCallForwardingIndicatorFromSharedPref: returning DISABLED as status for matching subscriberId not found");
                    }
                    Editor editor = sp.edit();
                    editor.remove(CF_ID);
                    editor.remove(CF_STATUS);
                    editor.apply();
                }
            }
        } else {
            Rlog.e(this.LOG_TAG, "getCallForwardingIndicatorFromSharedPref: invalid subId " + subId);
        }
        return status;
    }

    private void setCallForwardingIndicatorInSharedPref(boolean enable) {
        int status;
        if (enable) {
            status = EVENT_RADIO_AVAILABLE;
        } else {
            status = 0;
        }
        int subId = getSubId();
        Rlog.d(this.LOG_TAG, "setCallForwardingIndicatorInSharedPref: Storing status = " + status + " in pref " + CF_STATUS + subId);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putInt(CF_STATUS + subId, status);
        editor.apply();
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.setVoiceCallForwardingFlag(line, enable, number);
        }
    }

    protected void setVoiceCallForwardingFlag(IccRecords r, int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        r.setVoiceCallForwardingFlag(line, enable, number);
    }

    public boolean getCallForwardingIndicator() {
        boolean z = LCE_PULL_MODE;
        if (getPhoneType() == EVENT_SSN) {
            Rlog.e(this.LOG_TAG, "getCallForwardingIndicator: not possible in CDMA");
            return FAST_PREF_NET_REG;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        int callForwardingIndicator = -1;
        if (r != null) {
            callForwardingIndicator = r.getVoiceCallForwardingFlag();
        }
        if (callForwardingIndicator == -1) {
            callForwardingIndicator = getCallForwardingIndicatorFromSharedPref();
        }
        if (callForwardingIndicator != EVENT_RADIO_AVAILABLE) {
            z = FAST_PREF_NET_REG;
        }
        return z;
    }

    public void setCallForwardingPreference(boolean enabled) {
        Rlog.d(this.LOG_TAG, "Set callforwarding info to perferences");
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        edit.putBoolean(CF_ENABLED + getSubId(), enabled);
        edit.commit();
        setVmSimImsi(getSubscriberId());
    }

    public boolean getCallForwardingPreference() {
        Rlog.d(this.LOG_TAG, "Get callforwarding info from perferences");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        Editor edit;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (!sp.contains(CF_ENABLED + getSubId()) && sp.contains(CF_ENABLED + this.mPhoneId)) {
                setCallForwardingPreference(sp.getBoolean(CF_ENABLED + this.mPhoneId, FAST_PREF_NET_REG));
                edit = sp.edit();
                edit.remove(CF_ENABLED + this.mPhoneId);
                edit.commit();
            }
        } else if (!sp.contains(CF_ENABLED + getSubId()) && sp.contains(CF_ENABLED)) {
            setCallForwardingPreference(sp.getBoolean(CF_ENABLED, FAST_PREF_NET_REG));
            edit = sp.edit();
            edit.remove(CF_ENABLED);
            edit.commit();
        }
        return sp.getBoolean(CF_ENABLED + getSubId(), FAST_PREF_NET_REG);
    }

    public String getVmSimImsi() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String imsi;
        String oldDecodeVmSimImsi;
        Editor editor;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (!sp.contains(SIM_IMSI + getSubId()) && sp.contains(VM_SIM_IMSI + this.mPhoneId)) {
                imsi = sp.getString(VM_SIM_IMSI + this.mPhoneId, null);
                if (!(imsi == null || mPasswordUtil == null)) {
                    oldDecodeVmSimImsi = mPasswordUtil.pswd2PlainText(imsi);
                    try {
                        imsi = new String(Base64.decode(imsi, 0), CharacterSets.MIMENAME_UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        Rlog.e(this.LOG_TAG, "getVmSimImsi UnsupportedEncodingException");
                    } catch (Exception e2) {
                        Rlog.e(this.LOG_TAG, "getVmSimImsi Exception");
                    }
                    if (!imsi.equals(getSubscriberId()) && oldDecodeVmSimImsi.equals(getSubscriberId())) {
                        imsi = oldDecodeVmSimImsi;
                        Rlog.d(this.LOG_TAG, "getVmSimImsi: Old IMSI encryption is not supported, now setVmSimImsi again.");
                        setVmSimImsi(oldDecodeVmSimImsi);
                        editor = sp.edit();
                        editor.remove(VM_SIM_IMSI + this.mPhoneId);
                        editor.commit();
                    }
                }
            }
        } else if (!sp.contains(SIM_IMSI + getSubId()) && sp.contains(VM_SIM_IMSI)) {
            imsi = sp.getString(VM_SIM_IMSI, null);
            if (!(imsi == null || mPasswordUtil == null)) {
                oldDecodeVmSimImsi = mPasswordUtil.pswd2PlainText(imsi);
                try {
                    imsi = new String(Base64.decode(imsi, 0), CharacterSets.MIMENAME_UTF_8);
                } catch (UnsupportedEncodingException e3) {
                    Rlog.e(this.LOG_TAG, "getVmSimImsi UnsupportedEncodingException");
                } catch (Exception e4) {
                    Rlog.e(this.LOG_TAG, "getVmSimImsi Exception");
                }
                if (!imsi.equals(getSubscriberId()) && oldDecodeVmSimImsi.equals(getSubscriberId())) {
                    imsi = oldDecodeVmSimImsi;
                    Rlog.d(this.LOG_TAG, "getVmSimImsi: Old IMSI encryption is not supported, now setVmSimImsi again.");
                    setVmSimImsi(oldDecodeVmSimImsi);
                    editor = sp.edit();
                    editor.remove(VM_SIM_IMSI);
                    editor.commit();
                }
            }
        }
        String simimsi = sp.getString(SIM_IMSI + getSubId(), null);
        if (simimsi == null) {
            return simimsi;
        }
        try {
            return new String(Base64.decode(simimsi, 0), CharacterSets.MIMENAME_UTF_8);
        } catch (UnsupportedEncodingException e5) {
            Rlog.e(this.LOG_TAG, "getVmSimImsi UnsupportedEncodingException");
            return simimsi;
        } catch (Exception e6) {
            Rlog.e(this.LOG_TAG, "getVmSimImsi Exception");
            return simimsi;
        }
    }

    public void setVmSimImsi(String imsi) {
        try {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            if (imsi != null) {
                editor.putString(SIM_IMSI + getSubId(), new String(Base64.encode(imsi.getBytes(CharacterSets.MIMENAME_UTF_8), 0), CharacterSets.MIMENAME_UTF_8));
            } else {
                editor.putString(SIM_IMSI + getSubId(), null);
            }
            editor.commit();
        } catch (UnsupportedEncodingException e) {
            Rlog.e(this.LOG_TAG, "setVmSimImsi UnsupportedEncodingException");
        } catch (Exception e2) {
            Rlog.e(this.LOG_TAG, "getVmSimImsi Exception");
        }
    }

    public void queryCdmaRoamingPreference(Message response) {
        this.mCi.queryCdmaRoamingPreference(response);
    }

    public SignalStrength getSignalStrength() {
        ServiceStateTracker sst = getServiceStateTracker();
        if (sst == null) {
            return new SignalStrength();
        }
        return sst.getSignalStrength();
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        this.mCi.setCdmaRoamingPreference(cdmaRoamingType, response);
    }

    public void setCdmaSubscription(int cdmaSubscriptionType, Message response) {
        this.mCi.setCdmaSubscriptionSource(cdmaSubscriptionType, response);
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        ServiceStateTracker sSt = getServiceStateTracker();
        if (FAST_PREF_NET_REG && sSt != null) {
            Rlog.d(this.LOG_TAG, "FAST_PREF_NET_REG is ture, setPreferredNetworkTypeSafely.");
            HwTelephonyFactory.getHwNetworkManager().setPreferredNetworkTypeSafely(this, sSt, networkType, response);
        } else if (!HwModemCapability.isCapabilitySupport(EVENT_GET_IMEI_DONE) || TelephonyManager.getDefault().getPhoneCount() <= EVENT_RADIO_AVAILABLE) {
            this.mCi.setPreferredNetworkType(networkType, response);
        } else {
            HwTelephonyFactory.getHwUiccManager().setPreferredNetworkType(networkType, getPhoneId(), response);
        }
    }

    public void getPreferredNetworkType(Message response) {
        this.mCi.getPreferredNetworkType(response);
    }

    public void getSmscAddress(Message result) {
        this.mCi.getSmscAddress(result);
    }

    public void setSmscAddress(String address, Message result) {
        this.mCi.setSmscAddress(address, result);
    }

    public void setTTYMode(int ttyMode, Message onComplete) {
        this.mCi.setTTYMode(ttyMode, onComplete);
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        Rlog.d(this.LOG_TAG, "unexpected setUiTTYMode method call");
    }

    public void queryTTYMode(Message onComplete) {
        this.mCi.queryTTYMode(onComplete);
    }

    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
    }

    public void getEnhancedVoicePrivacy(Message onComplete) {
    }

    public void setBandMode(int bandMode, Message response) {
        this.mCi.setBandMode(bandMode, response);
    }

    public void queryAvailableBandMode(Message response) {
        this.mCi.queryAvailableBandMode(response);
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        this.mCi.invokeOemRilRequestRaw(data, response);
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        this.mCi.invokeOemRilRequestStrings(strings, response);
    }

    public void nvReadItem(int itemID, Message response) {
        this.mCi.nvReadItem(itemID, response);
    }

    public void nvWriteItem(int itemID, String itemValue, Message response) {
        this.mCi.nvWriteItem(itemID, itemValue, response);
    }

    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message response) {
        this.mCi.nvWriteCdmaPrl(preferredRoamingList, response);
    }

    public void nvResetConfig(int resetType, Message response) {
        this.mCi.nvResetConfig(resetType, response);
    }

    public void notifyDataActivity() {
        this.mNotifier.notifyDataActivity(this);
    }

    private void notifyMessageWaitingIndicator() {
        if (this.mIsVoiceCapable) {
            this.mNotifier.notifyMessageWaitingChanged(this);
        }
    }

    public void notifyDataConnection(String reason, String apnType, DataState state) {
        this.mNotifier.notifyDataConnection(this, reason, apnType, state);
    }

    public void notifyDataConnection(String reason, String apnType) {
        this.mNotifier.notifyDataConnection(this, reason, apnType, getDataConnectionState(apnType));
    }

    public void notifyDataConnection(String reason) {
        String[] types = getActiveApnTypes();
        int length = types.length;
        for (int i = 0; i < length; i += EVENT_RADIO_AVAILABLE) {
            String apnType = types[i];
            this.mNotifier.notifyDataConnection(this, reason, apnType, getDataConnectionState(apnType));
        }
    }

    public void notifyOtaspChanged(int otaspMode) {
        this.mNotifier.notifyOtaspChanged(this, otaspMode);
    }

    public void notifySignalStrength() {
        this.mNotifier.notifySignalStrength(this);
    }

    public void notifyCellInfo(List<CellInfo> cellInfo) {
        this.mNotifier.notifyCellInfo(this, privatizeCellInfoList(cellInfo));
    }

    public void notifyVoLteServiceStateChanged(VoLteServiceState lteState) {
        this.mNotifier.notifyVoLteServiceStateChanged(this, lteState);
    }

    public boolean isInEmergencyCall() {
        return FAST_PREF_NET_REG;
    }

    public boolean isInEcm() {
        return FAST_PREF_NET_REG;
    }

    private static int getVideoState(Call call) {
        Connection conn = call.getEarliestConnection();
        if (conn != null) {
            return conn.getVideoState();
        }
        return 0;
    }

    private boolean isVideoCall(Call call) {
        return VideoProfile.isVideo(getVideoState(call));
    }

    public boolean isVideoCallPresent() {
        boolean isVideoCallActive = FAST_PREF_NET_REG;
        if (this.mImsPhone != null) {
            if (isVideoCall(this.mImsPhone.getForegroundCall()) || isVideoCall(this.mImsPhone.getBackgroundCall())) {
                isVideoCallActive = LCE_PULL_MODE;
            } else {
                isVideoCallActive = isVideoCall(this.mImsPhone.getRingingCall());
            }
        }
        Rlog.d(this.LOG_TAG, "isVideoCallActive: " + isVideoCallActive);
        return isVideoCallActive;
    }

    public int getVoiceMessageCount() {
        return this.mVmCount;
    }

    public void setVoiceMessageCount(int countWaiting) {
        this.mVmCount = countWaiting;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            Rlog.d(this.LOG_TAG, "setVoiceMessageCount: Storing Voice Mail Count = " + countWaiting + " for mVmCountKey = " + VM_COUNT + subId + " in preferences.");
            Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            editor.putInt(VM_COUNT + subId, countWaiting);
            editor.apply();
        } else {
            Rlog.e(this.LOG_TAG, "setVoiceMessageCount in sharedPreference: invalid subId " + subId);
        }
        notifyMessageWaitingIndicator();
    }

    protected int getStoredVoiceMessageCount() {
        int countVoiceMessages = 0;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            int countFromSP = sp.getInt(VM_COUNT + subId, -2);
            if (countFromSP != -2) {
                countVoiceMessages = countFromSP;
                Rlog.d(this.LOG_TAG, "getStoredVoiceMessageCount: from preference for subId " + subId + "= " + countFromSP);
                return countVoiceMessages;
            }
            String subscriberId = sp.getString(VM_ID, null);
            if (subscriberId == null) {
                return 0;
            }
            String currentSubscriberId = getSubscriberId();
            if (currentSubscriberId == null || !currentSubscriberId.equals(subscriberId)) {
                Rlog.d(this.LOG_TAG, "getStoredVoiceMessageCount: returning 0 as count for matching subscriberId not found");
            } else {
                countVoiceMessages = sp.getInt(VM_COUNT, 0);
                setVoiceMessageCount(countVoiceMessages);
                Rlog.d(this.LOG_TAG, "getStoredVoiceMessageCount: from preference = " + countVoiceMessages);
            }
            Editor editor = sp.edit();
            editor.remove(VM_ID);
            editor.remove(VM_COUNT);
            editor.apply();
            return countVoiceMessages;
        }
        Rlog.e(this.LOG_TAG, "getStoredVoiceMessageCount: invalid subId " + subId);
        return 0;
    }

    public int getCdmaEriIconIndex() {
        return -1;
    }

    public int getCdmaEriIconMode() {
        return -1;
    }

    public String getCdmaEriText() {
        return "GSM nw, no ERI";
    }

    public String getCdmaMin() {
        return null;
    }

    public boolean isMinInfoReady() {
        return FAST_PREF_NET_REG;
    }

    public String getCdmaPrlVersion() {
        return null;
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
    }

    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
        this.mPostDialHandler = new Registrant(h, what, obj);
    }

    public Registrant getPostDialHandler() {
        return this.mPostDialHandler;
    }

    public void exitEmergencyCallbackMode() {
    }

    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
    }

    public void unregisterForCdmaOtaStatusChange(Handler h) {
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
    }

    public boolean needsOtaServiceProvisioning() {
        return FAST_PREF_NET_REG;
    }

    public boolean isOtaSpNumber(String dialStr) {
        return FAST_PREF_NET_REG;
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
    }

    public void unregisterForCallWaiting(Handler h) {
    }

    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
    }

    public void unregisterForEcmTimerReset(Handler h) {
    }

    public void registerForSignalInfo(Handler h, int what, Object obj) {
        this.mCi.registerForSignalInfo(h, what, obj);
    }

    public void unregisterForSignalInfo(Handler h) {
        this.mCi.unregisterForSignalInfo(h);
    }

    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        this.mCi.registerForDisplayInfo(h, what, obj);
    }

    public void unregisterForDisplayInfo(Handler h) {
        this.mCi.unregisterForDisplayInfo(h);
    }

    public void registerForNumberInfo(Handler h, int what, Object obj) {
        this.mCi.registerForNumberInfo(h, what, obj);
    }

    public void unregisterForNumberInfo(Handler h) {
        this.mCi.unregisterForNumberInfo(h);
    }

    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
        this.mCi.registerForRedirectedNumberInfo(h, what, obj);
    }

    public void unregisterForRedirectedNumberInfo(Handler h) {
        this.mCi.unregisterForRedirectedNumberInfo(h);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mCi.registerForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mCi.unregisterForLineControlInfo(h);
    }

    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
        this.mCi.registerFoT53ClirlInfo(h, what, obj);
    }

    public void unregisterForT53ClirInfo(Handler h) {
        this.mCi.unregisterForT53ClirInfo(h);
    }

    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
        this.mCi.registerForT53AudioControlInfo(h, what, obj);
    }

    public void unregisterForT53AudioControlInfo(Handler h) {
        this.mCi.unregisterForT53AudioControlInfo(h);
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
    }

    public void registerForRadioOffOrNotAvailable(Handler h, int what, Object obj) {
        this.mRadioOffOrNotAvailableRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForRadioOffOrNotAvailable(Handler h) {
        this.mRadioOffOrNotAvailableRegistrants.remove(h);
    }

    public String[] getActiveApnTypes() {
        if (this.mDcTracker == null) {
            return null;
        }
        return this.mDcTracker.getActiveApnTypes();
    }

    public boolean hasMatchedTetherApnSetting() {
        return this.mDcTracker.hasMatchedTetherApnSetting();
    }

    public String getActiveApnHost(String apnType) {
        return this.mDcTracker.getActiveApnString(apnType);
    }

    public LinkProperties getLinkProperties(String apnType) {
        return this.mDcTracker.getLinkProperties(apnType);
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        return this.mDcTracker.getNetworkCapabilities(apnType);
    }

    public boolean isDataConnectivityPossible() {
        return isDataConnectivityPossible("default");
    }

    public boolean isDataConnectivityPossible(String apnType) {
        if (this.mDcTracker != null) {
            return this.mDcTracker.isDataPossible(apnType);
        }
        return FAST_PREF_NET_REG;
    }

    public void notifyNewRingingConnectionP(Connection cn) {
        if (this.mIsVoiceCapable) {
            this.mNewRingingConnectionRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
        }
    }

    public void notifyUnknownConnectionP(Connection cn) {
        this.mUnknownConnectionRegistrants.notifyResult(cn);
    }

    public void registerForMccChanged(Handler h, int what, Object obj) {
        synchronized (lockForRadioTechnologyChange) {
            this.mMccChangedRegistrants.add(new Registrant(h, what, obj));
        }
    }

    public void unregisterForMccChanged(Handler h) {
        synchronized (lockForRadioTechnologyChange) {
            this.mMccChangedRegistrants.remove(h);
        }
    }

    public void notifyMccChanged(String mcc) {
        Rlog.d(this.LOG_TAG, "notifyMccChanged new mcc is " + mcc);
        this.mMccChangedRegistrants.notifyRegistrants(new AsyncResult(null, mcc, null));
    }

    public void notifyForVideoCapabilityChanged(boolean isVideoCallCapable) {
        this.mIsVideoCapable = isVideoCallCapable;
        this.mVideoCapabilityChangedRegistrants.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(isVideoCallCapable), null));
    }

    private void notifyIncomingRing() {
        if (this.mIsVoiceCapable) {
            this.mIncomingRingRegistrants.notifyRegistrants(new AsyncResult(null, this, null));
        }
    }

    private void sendIncomingCallRingNotification(int token) {
        if (this.mIsVoiceCapable && !this.mDoesRilSendMultipleCallRing && token == this.mCallRingContinueToken) {
            Rlog.d(this.LOG_TAG, "Sending notifyIncomingRing");
            notifyIncomingRing();
            sendMessageDelayed(obtainMessage(EVENT_CALL_RING_CONTINUE, token, 0), (long) this.mCallRingDelay);
            return;
        }
        Rlog.d(this.LOG_TAG, "Ignoring ring notification request, mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing + " token=" + token + " mCallRingContinueToken=" + this.mCallRingContinueToken + " mIsVoiceCapable=" + this.mIsVoiceCapable);
    }

    public boolean isCspPlmnEnabled() {
        return FAST_PREF_NET_REG;
    }

    public IsimRecords getIsimRecords() {
        Rlog.e(this.LOG_TAG, "getIsimRecords() is only supported on LTE devices");
        return null;
    }

    public String getMsisdn() {
        return null;
    }

    public DataState getDataConnectionState() {
        return getDataConnectionState("default");
    }

    public void notifyCallForwardingIndicator() {
    }

    public void notifyDataConnectionFailed(String reason, String apnType) {
        this.mNotifier.notifyDataConnectionFailed(this, reason, apnType);
    }

    public void notifyPreciseDataConnectionFailed(String reason, String apnType, String apn, String failCause) {
        this.mNotifier.notifyPreciseDataConnectionFailed(this, reason, apnType, apn, failCause);
    }

    public int getLteOnCdmaMode() {
        return this.mCi.getLteOnCdmaMode();
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        Rlog.e(this.LOG_TAG, "Error! This function should never be executed, inactive Phone.");
    }

    public UsimServiceTable getUsimServiceTable() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getUsimServiceTable();
        }
        return null;
    }

    public UiccCard getUiccCard() {
        return this.mUiccController.getUiccCard(this.mPhoneId);
    }

    public String[] getPcscfAddress(String apnType) {
        return this.mDcTracker.getPcscfAddress(apnType);
    }

    public void setImsRegistrationState(boolean registered) {
    }

    public Phone getImsPhone() {
        return this.mImsPhone;
    }

    public boolean isUtEnabled() {
        return FAST_PREF_NET_REG;
    }

    public void dispose() {
    }

    private void updateImsPhone() {
        Rlog.d(this.LOG_TAG, "updateImsPhone mImsServiceReady=" + this.mImsServiceReady);
        if (this.mImsServiceReady && this.mImsPhone == null) {
            this.mImsPhone = PhoneFactory.makeImsPhone(this.mNotifier, this);
            CallManager.getInstance().registerPhone(this.mImsPhone);
            this.mImsPhone.registerForSilentRedial(this, EVENT_INITIATE_SILENT_REDIAL, null);
        } else if (!this.mImsServiceReady && this.mImsPhone != null) {
            CallManager.getInstance().unregisterPhone(this.mImsPhone);
            this.mImsPhone.unregisterForSilentRedial(this);
            this.mImsPhone.dispose();
            this.mImsPhone = null;
        }
    }

    protected Connection dialInternal(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        return null;
    }

    public int getSubId() {
        return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public int getVoicePhoneServiceState() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
            return getServiceState().getState();
        }
        return 0;
    }

    public boolean setOperatorBrandOverride(String brand) {
        return FAST_PREF_NET_REG;
    }

    public boolean setRoamingOverride(List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) {
        String iccId = getIccSerialNumber();
        if (TextUtils.isEmpty(iccId)) {
            return FAST_PREF_NET_REG;
        }
        setRoamingOverrideHelper(gsmRoamingList, GSM_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(gsmNonRoamingList, GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(cdmaRoamingList, CDMA_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(cdmaNonRoamingList, CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        ServiceStateTracker tracker = getServiceStateTracker();
        if (tracker != null) {
            tracker.pollState();
        }
        return LCE_PULL_MODE;
    }

    private void setRoamingOverrideHelper(List<String> list, String prefix, String iccId) {
        Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        String key = prefix + iccId;
        if (list == null || list.isEmpty()) {
            spEditor.remove(key).commit();
        } else {
            spEditor.putStringSet(key, new HashSet(list)).commit();
        }
    }

    public boolean isMccMncMarkedAsRoaming(String mccMnc) {
        return getRoamingOverrideHelper(GSM_ROAMING_LIST_OVERRIDE_PREFIX, mccMnc);
    }

    public boolean isMccMncMarkedAsNonRoaming(String mccMnc) {
        return getRoamingOverrideHelper(GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX, mccMnc);
    }

    public boolean isSidMarkedAsRoaming(int SID) {
        return getRoamingOverrideHelper(CDMA_ROAMING_LIST_OVERRIDE_PREFIX, Integer.toString(SID));
    }

    public boolean isSidMarkedAsNonRoaming(int SID) {
        return getRoamingOverrideHelper(CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX, Integer.toString(SID));
    }

    public boolean isImsRegistered() {
        Phone imsPhone = this.mImsPhone;
        boolean isImsRegistered = FAST_PREF_NET_REG;
        if (imsPhone != null) {
            isImsRegistered = imsPhone.isImsRegistered();
        } else {
            ServiceStateTracker sst = getServiceStateTracker();
            if (sst != null) {
                isImsRegistered = sst.isImsRegistered();
            }
        }
        Rlog.d(this.LOG_TAG, "isImsRegistered =" + isImsRegistered);
        return isImsRegistered;
    }

    public boolean isWifiCallingEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isWifiCallingEnabled = FAST_PREF_NET_REG;
        if (imsPhone != null) {
            isWifiCallingEnabled = imsPhone.isWifiCallingEnabled();
        }
        Rlog.d(this.LOG_TAG, "isWifiCallingEnabled =" + isWifiCallingEnabled);
        return isWifiCallingEnabled;
    }

    public boolean isVolteEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isVolteEnabled = FAST_PREF_NET_REG;
        if (imsPhone != null) {
            isVolteEnabled = imsPhone.isVolteEnabled();
        }
        Rlog.d(this.LOG_TAG, "isImsRegistered =" + isVolteEnabled);
        return isVolteEnabled;
    }

    private boolean getRoamingOverrideHelper(String prefix, String key) {
        String iccId = getIccSerialNumber();
        if (TextUtils.isEmpty(iccId) || TextUtils.isEmpty(key)) {
            return FAST_PREF_NET_REG;
        }
        Set<String> value = PreferenceManager.getDefaultSharedPreferences(this.mContext).getStringSet(prefix + iccId, null);
        if (value == null) {
            return FAST_PREF_NET_REG;
        }
        return value.contains(key);
    }

    public boolean isRadioAvailable() {
        return this.mCi.getRadioState().isAvailable();
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState().isOn();
    }

    public void shutdownRadio() {
        if (VSimUtilsInner.isPlatformTwoModems() && VSimUtilsInner.isVSimEnabled()) {
            Phone phone = VSimUtilsInner.getVSimPhone();
            if (!(phone == null || !VSimUtilsInner.isVSimPhone(phone) || phone.getServiceStateTracker() == null)) {
                phone.getServiceStateTracker().requestShutdown();
                Rlog.d(this.LOG_TAG, "Vsim is enable, send shut down radio to VSIM sub!");
            }
        }
        getServiceStateTracker().requestShutdown();
    }

    public boolean isShuttingDown() {
        return getServiceStateTracker().isDeviceShuttingDown();
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
        this.mCi.setRadioCapability(rc, response);
    }

    public int getRadioAccessFamily() {
        RadioCapability rc = getRadioCapability();
        return rc == null ? EVENT_RADIO_AVAILABLE : rc.getRadioAccessFamily();
    }

    public String getModemUuId() {
        RadioCapability rc = getRadioCapability();
        return rc == null ? "" : rc.getLogicalModemUuid();
    }

    public RadioCapability getRadioCapability() {
        return (RadioCapability) this.mRadioCapability.get();
    }

    public void radioCapabilityUpdated(RadioCapability rc) {
        this.mRadioCapability.set(rc);
        if (SubscriptionManager.isValidSubscriptionId(getSubId())) {
            Rlog.d(this.LOG_TAG, "skip restoreNetworkSelection in sendSubscriptionSettings");
            sendSubscriptionSettings(FAST_PREF_NET_REG);
        }
    }

    public void sendSubscriptionSettings(boolean restoreNetworkSelection) {
        Rlog.d(this.LOG_TAG, "skip setPreferredNetworkType in sendSubscriptionSettings");
        if (restoreNetworkSelection) {
            restoreSavedNetworkSelection(null);
        }
    }

    protected void setPreferredNetworkTypeIfSimLoaded() {
        if (SubscriptionManager.isValidSubscriptionId(getSubId())) {
            Rlog.d(this.LOG_TAG, "skip setPreferredNetworkType in setPreferredNetworkTypeIfSimLoaded");
        }
    }

    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
        this.mCi.registerForRadioCapabilityChanged(h, what, obj);
    }

    public void unregisterForRadioCapabilityChanged(Handler h) {
        this.mCi.unregisterForRadioCapabilityChanged(this);
    }

    public boolean isImsUseEnabled() {
        Rlog.d(this.LOG_TAG, "isImsUseEnabled, isVolteEnabledByPlatform=" + ImsManager.isVolteEnabledByPlatform(this.mContext) + " isEnhanced4gLteModeSettingEnabledByUser=" + ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mContext) + " isWfcEnabledByPlatform=" + ImsManager.isWfcEnabledByPlatform(this.mContext) + " isWfcEnabledByUser=" + ImsManager.isWfcEnabledByUser(this.mContext) + " isNonTtyOrTtyOnVolteEnabled=" + ImsManager.isNonTtyOrTtyOnVolteEnabled(this.mContext));
        if (ImsManager.isVolteEnabledByPlatform(this.mContext) && ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mContext)) {
            return LCE_PULL_MODE;
        }
        if (ImsManager.isWfcEnabledByPlatform(this.mContext) && ImsManager.isWfcEnabledByUser(this.mContext)) {
            return ImsManager.isNonTtyOrTtyOnVolteEnabled(this.mContext);
        }
        return FAST_PREF_NET_REG;
    }

    public boolean isVideoEnabled() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
            return FAST_PREF_NET_REG;
        }
        return imsPhone.isVideoEnabled();
    }

    public int getLceStatus() {
        return this.mLceStatus;
    }

    public void getModemActivityInfo(Message response) {
        this.mCi.getModemActivityInfo(response);
    }

    public void startLceAfterRadioIsAvailable() {
        this.mCi.startLceService(DEFAULT_REPORT_INTERVAL_MS, LCE_PULL_MODE, obtainMessage(EVENT_CONFIG_LCE));
    }

    public Locale getLocaleFromSimAndCarrierPrefs() {
        IccRecords records = (IccRecords) this.mIccRecords.get();
        if (records == null || records.getSimLanguage() == null) {
            return getLocaleFromCarrierProperties(this.mContext);
        }
        return new Locale(records.getSimLanguage());
    }

    public void updateDataConnectionTracker() {
        this.mDcTracker.update();
    }

    public void setInternalDataEnabled(boolean enable, Message onCompleteMsg) {
        this.mDcTracker.setInternalDataEnabled(enable, onCompleteMsg);
    }

    public boolean updateCurrentCarrierInProvider() {
        return FAST_PREF_NET_REG;
    }

    public void registerForAllDataDisconnected(Handler h, int what, Object obj) {
        this.mDcTracker.registerForAllDataDisconnected(h, what, obj);
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mDcTracker.unregisterForAllDataDisconnected(h);
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return null;
    }

    protected boolean isMatchGid(String gid) {
        String gid1 = getGroupIdLevel1();
        int gidLength = gid.length();
        if (TextUtils.isEmpty(gid1) || gid1.length() < gidLength || !gid1.substring(0, gidLength).equalsIgnoreCase(gid)) {
            return FAST_PREF_NET_REG;
        }
        return LCE_PULL_MODE;
    }

    public static void checkWfcWifiOnlyModeBeforeDial(Phone imsPhone, Context context) throws CallStateException {
        boolean wfcWiFiOnly = FAST_PREF_NET_REG;
        if (imsPhone == null || !imsPhone.isWifiCallingEnabled()) {
            if (ImsManager.isWfcEnabledByPlatform(context) && ImsManager.isWfcEnabledByUser(context) && ImsManager.getWfcMode(context) == 0) {
                wfcWiFiOnly = LCE_PULL_MODE;
            }
            if (wfcWiFiOnly) {
                throw new CallStateException(EVENT_RADIO_AVAILABLE, "WFC Wi-Fi Only Mode: IMS not registered");
            }
        }
    }

    public void startRingbackTone() {
    }

    public void stopRingbackTone() {
    }

    public void callEndCleanupHandOverCallIfAny() {
    }

    public void cancelUSSD() {
    }

    public Phone getDefaultPhone() {
        return this;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Phone: subId=" + getSubId());
        pw.println(" mPhoneId=" + this.mPhoneId);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mDnsCheckDisabled=" + this.mDnsCheckDisabled);
        pw.println(" mDcTracker=" + this.mDcTracker);
        pw.println(" mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing);
        pw.println(" mCallRingContinueToken=" + this.mCallRingContinueToken);
        pw.println(" mCallRingDelay=" + this.mCallRingDelay);
        pw.println(" mIsVoiceCapable=" + this.mIsVoiceCapable);
        pw.println(" mIccRecords=" + this.mIccRecords.get());
        pw.println(" mUiccApplication=" + this.mUiccApplication.get());
        pw.println(" mSmsStorageMonitor=" + this.mSmsStorageMonitor);
        pw.println(" mSmsUsageMonitor=" + this.mSmsUsageMonitor);
        pw.flush();
        pw.println(" mLooper=" + this.mLooper);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mNotifier=" + this.mNotifier);
        pw.println(" mSimulatedRadioControl=" + this.mSimulatedRadioControl);
        pw.println(" mUnitTestMode=" + this.mUnitTestMode);
        pw.println(" isDnsCheckDisabled()=" + isDnsCheckDisabled());
        pw.println(" getUnitTestMode()=" + getUnitTestMode());
        pw.println(" getState()=" + getState());
        pw.println(" getIccSerialNumber()=" + getIccSerialNumber());
        pw.println(" getIccRecordsLoaded()=" + getIccRecordsLoaded());
        pw.println(" getMessageWaitingIndicator()=" + getMessageWaitingIndicator());
        pw.println(" getCallForwardingIndicator()=" + getCallForwardingIndicator());
        pw.println(" isInEmergencyCall()=" + isInEmergencyCall());
        pw.flush();
        pw.println(" isInEcm()=" + isInEcm());
        pw.println(" getPhoneName()=" + getPhoneName());
        pw.println(" getPhoneType()=" + getPhoneType());
        pw.println(" getVoiceMessageCount()=" + getVoiceMessageCount());
        pw.println(" getActiveApnTypes()=" + getActiveApnTypes());
        pw.println(" isDataConnectivityPossible()=" + isDataConnectivityPossible());
        pw.println(" needsOtaServiceProvisioning=" + needsOtaServiceProvisioning());
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        if (this.mImsPhone != null) {
            try {
                this.mImsPhone.dump(fd, pw, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mDcTracker != null) {
            try {
                this.mDcTracker.dump(fd, pw, args);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (getServiceStateTracker() != null) {
            try {
                getServiceStateTracker().dump(fd, pw, args);
            } catch (Exception e22) {
                e22.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (getCallTracker() != null) {
            try {
                getCallTracker().dump(fd, pw, args);
            } catch (Exception e222) {
                e222.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mCi != null && (this.mCi instanceof RIL)) {
            try {
                ((RIL) this.mCi).dump(fd, pw, args);
            } catch (Exception e2222) {
                e2222.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
    }

    public String getCdmaGsmImsi() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getCdmaGsmImsi();
        }
        return null;
    }

    public int getUiccCardType() {
        AppType at = getCurrentUiccAppType();
        Rlog.d(this.LOG_TAG, "at = " + at);
        switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues()[at.ordinal()]) {
            case EVENT_RADIO_AVAILABLE /*1*/:
            case EVENT_SSN /*2*/:
            case EVENT_RADIO_ON /*5*/:
                return EVENT_SSN;
            case EVENT_SIM_RECORDS_LOADED /*3*/:
            case EVENT_MMI_DONE /*4*/:
                return EVENT_RADIO_AVAILABLE;
            default:
                return 0;
        }
    }

    public String getCdmaMlplVersion() {
        return null;
    }

    public String getCdmaMsplVersion() {
        return null;
    }

    public void testVoiceLoopBack(int mode) {
    }

    public void registerForCdmaWaitingNumberChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForCdmaWaitingNumberChanged(Handler h) {
    }
}
