package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.encrypt.PasswordUtil;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.carrier.CarrierIdentifier;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellLocation;
import android.telephony.ClientRequestStats;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.PhysicalChannelConfig;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.text.TextUtils;
import android.util.Base64;
import com.android.ims.ImsCall;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.vsim.VSimUtilsInner;
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
    private static final int ALREADY_IN_AUTO_SELECTION = 1;
    private static final String CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_non_roaming_list_";
    private static final String CDMA_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_roaming_list_";
    public static final String CF_ENABLED = "cf_enabled_key";
    public static final String CF_ID = "cf_id_key";
    public static final String CF_STATUS = "cf_status_key";
    public static final String CLIR_KEY = "clir_key";
    public static final String CS_FALLBACK = "cs_fallback";
    public static final String DATA_DISABLED_ON_BOOT_KEY = "disabled_on_boot_key";
    public static final String DATA_ROAMING_IS_USER_SETTING_KEY = "data_roaming_is_user_setting_key";
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
    protected static final int EVENT_GET_CALLFORWARDING_STATUS = 46;
    protected static final int EVENT_GET_CALL_FORWARD_DONE = 13;
    public static final int EVENT_GET_DEVICE_IDENTITY_DONE = 21;
    protected static final int EVENT_GET_IMEISV_DONE = 10;
    public static final int EVENT_GET_IMEI_DONE = 9;
    protected static final int EVENT_GET_RADIO_CAPABILITY = 35;
    private static final int EVENT_GET_SIM_STATUS_DONE = 11;
    protected static final int EVENT_ICC_CHANGED = 30;
    protected static final int EVENT_ICC_RECORD_EVENTS = 29;
    private static final int EVENT_INITIATE_SILENT_REDIAL = 32;
    protected static final int EVENT_LAST = 46;
    private static final int EVENT_MMI_DONE = 4;
    protected static final int EVENT_MODEM_RESET = 45;
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
    public static final String EXTRA_KEY_NOTIFICATION_MESSAGE = "notificationMessage";
    protected static final boolean FAST_PREF_NET_REG = SystemProperties.getBoolean("ro.hwpp_fast_pref_net_reg", false);
    protected static final String FORCE_AUTO_PLMN = SystemProperties.get("ro.config.force_auto_plmn", "");
    private static final String GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_non_roaming_list_";
    private static final String GSM_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_roaming_list_";
    private static final boolean LCE_PULL_MODE = true;
    private static final String LOG_TAG_STATIC = "Phone";
    public static final String NETWORK_SELECTION_KEY = "network_selection_key";
    public static final String NETWORK_SELECTION_NAME_KEY = "network_selection_name_key";
    public static final String NETWORK_SELECTION_SHORT_KEY = "network_selection_short_key";
    public static final String SIM_IMSI = "sim_imsi_key";
    protected static final int USSD_MAX_QUEUE = 10;
    private static final String VM_COUNT = "vm_count_key";
    private static final String VM_ID = "vm_id_key";
    public static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    protected static final Object lockForRadioTechnologyChange = new Object();
    private static PasswordUtil mPasswordUtil = HwFrameworkFactory.getPasswordUtil();
    /* access modifiers changed from: private */
    public String LOG_TAG;
    private final String mActionAttached;
    private final String mActionDetached;
    private final AppSmsManager mAppSmsManager;
    private int mCallRingContinueToken;
    private int mCallRingDelay;
    protected CarrierActionAgent mCarrierActionAgent;
    protected CarrierSignalAgent mCarrierSignalAgent;
    public CommandsInterface mCi;
    /* access modifiers changed from: protected */
    public final Context mContext;
    public DcTracker mDcTracker;
    protected DeviceStateMonitor mDeviceStateMonitor;
    protected final RegistrantList mDisconnectRegistrants;
    private boolean mDnsCheckDisabled;
    private boolean mDoesRilSendMultipleCallRing;
    protected final RegistrantList mEmergencyCallToggledRegistrants;
    private final RegistrantList mHandoverRegistrants;
    public final AtomicReference<IccRecords> mIccRecords;
    private BroadcastReceiver mImsIntentReceiver;
    protected Phone mImsPhone;
    /* access modifiers changed from: private */
    public boolean mImsServiceReady;
    private final RegistrantList mIncomingRingRegistrants;
    protected boolean mIsPhoneInEcmState;
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
    private SimActivationTracker mSimActivationTracker;
    protected final RegistrantList mSimRecordsLoadedRegistrants;
    protected SimulatedRadioControl mSimulatedRadioControl;
    public SmsStorageMonitor mSmsStorageMonitor;
    public SmsUsageMonitor mSmsUsageMonitor;
    protected final RegistrantList mSuppServiceFailedRegistrants;
    protected TelephonyComponentFactory mTelephonyComponentFactory;
    TelephonyTester mTelephonyTester;
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

    public abstract int getPhoneType();

    public abstract PhoneConstants.State getState();

    /* access modifiers changed from: protected */
    public abstract void onUpdateIccAvailability();

    public abstract void sendEmergencyCallStateChange(boolean z);

    public abstract void setBroadcastEmergencyCallStateChanges(boolean z);

    /* access modifiers changed from: protected */
    public void handleExitEmergencyCallbackMode() {
    }

    public IccRecords getIccRecords() {
        return this.mIccRecords.get();
    }

    public String getPhoneName() {
        return this.mName;
    }

    /* access modifiers changed from: protected */
    public void setPhoneName(String name) {
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
            TelephonyManager.setTelephonyProperty(this.mPhoneId, property, value);
        }
    }

    public void setGlobalSystemProperty(String property, String value) {
        if (!getUnitTestMode()) {
            TelephonyManager.setTelephonyProperty(property, value);
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
                if (intent != null && intent.getAction() != null) {
                    String access$000 = Phone.this.LOG_TAG;
                    Rlog.d(access$000, "mImsIntentReceiver: action " + intent.getAction());
                    if (intent.hasExtra("android:phone_id")) {
                        int extraPhoneId = intent.getIntExtra("android:phone_id", -1);
                        String access$0002 = Phone.this.LOG_TAG;
                        Rlog.d(access$0002, "mImsIntentReceiver: extraPhoneId = " + extraPhoneId);
                        if (extraPhoneId == -1 || extraPhoneId != Phone.this.getPhoneId()) {
                            return;
                        }
                    }
                    synchronized (Phone.lockForRadioTechnologyChange) {
                        if ("com.android.ims.IMS_SERVICE_UP".equals(intent.getAction())) {
                            ImsManager imsManager = ImsManager.getInstance(Phone.this.mContext, Phone.this.getPhoneId());
                            if (imsManager != null) {
                                boolean unused = Phone.this.mImsServiceReady = imsManager.isServiceAvailable();
                            } else {
                                boolean unused2 = Phone.this.mImsServiceReady = true;
                            }
                            Phone.this.updateImsPhone();
                            HwFrameworkFactory.updateImsServiceConfig(Phone.this.mContext, Phone.this.mPhoneId, false);
                        } else if ("com.android.ims.IMS_SERVICE_DOWN".equals(intent.getAction())) {
                            boolean unused3 = Phone.this.mImsServiceReady = false;
                            Phone.this.updateImsPhone();
                        }
                    }
                }
            }
        };
        this.mVmCount = 0;
        this.mIsVoiceCapable = true;
        this.mIsPhoneInEcmState = false;
        this.mIsVideoCapable = false;
        this.mUiccController = null;
        this.mIccRecords = new AtomicReference<>();
        this.mUiccApplication = new AtomicReference<>();
        this.mImsServiceReady = false;
        this.mImsPhone = null;
        this.mRadioCapability = new AtomicReference<>();
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
        this.mAppSmsManager = telephonyComponentFactory.makeAppSmsManager(context);
        this.LOG_TAG += "[SUB" + phoneId + "]";
        if (Build.IS_DEBUGGABLE) {
            this.mTelephonyTester = new TelephonyTester(this);
        }
        HwTelephonyFactory.getHwVolteChrManager().init(this.mContext);
        HwTelephonyFactory.getHwPhoneManager().setDefaultTimezone(this.mContext);
        HwTelephonyFactory.getHwDataServiceChrManager().init(this.mContext);
        HwTelephonyFactory.getHwTelephonyChrManager().init(this.mContext);
        setUnitTestMode(unitTestMode);
        this.mDnsCheckDisabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DNS_SERVER_CHECK_DISABLED_KEY, false);
        this.mCi.setOnCallRing(this, 14, null);
        this.mCi.setOnECCNum(this, AbstractPhoneBase.EVENT_ECC_NUM, null);
        this.mCi.registerForLaaStateChange(this, 112, Integer.valueOf(phoneId));
        this.mCi.queryEmergencyNumbers();
        this.mIsVoiceCapable = this.mContext.getResources().getBoolean(17957068);
        this.mDoesRilSendMultipleCallRing = SystemProperties.getBoolean("ro.telephony.call_ring.multiple", true);
        Rlog.d(this.LOG_TAG, "mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing);
        this.mCallRingDelay = SystemProperties.getInt("ro.telephony.call_ring.delay", 3000);
        Rlog.d(this.LOG_TAG, "mCallRingDelay=" + this.mCallRingDelay);
        if (getPhoneType() != 5) {
            Locale carrierLocale = getLocaleFromCarrierProperties(this.mContext);
            if (carrierLocale != null && !TextUtils.isEmpty(carrierLocale.getCountry())) {
                String country = carrierLocale.getCountry();
                try {
                    Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_country_code");
                } catch (Settings.SettingNotFoundException e) {
                    ((WifiManager) this.mContext.getSystemService("wifi")).setCountryCode(country);
                }
            }
            this.mTelephonyComponentFactory = telephonyComponentFactory;
            this.mSmsStorageMonitor = this.mTelephonyComponentFactory.makeSmsStorageMonitor(this);
            this.mSmsUsageMonitor = this.mTelephonyComponentFactory.makeSmsUsageMonitor(context, this);
            this.mUiccController = UiccController.getInstance();
            this.mUiccController.registerForIccChanged(this, 30, null);
            this.mSimActivationTracker = this.mTelephonyComponentFactory.makeSimActivationTracker(this);
            if (getPhoneType() != 3) {
                this.mCi.registerForSrvccStateChanged(this, 31, null);
            }
            this.mCi.setOnUnsolOemHookRaw(this, 34, null);
            this.mCi.startLceService(200, true, obtainMessage(37));
            this.mCi.registerForUnsolNvCfgFinished(this, 114, null);
            if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                this.mCi.registerCsconModeInfo(this, 116, null);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0050, code lost:
        return;
     */
    public void startMonitoringImsService() {
        if (getPhoneType() != 3) {
            synchronized (lockForRadioTechnologyChange) {
                IntentFilter filter = new IntentFilter();
                ImsManager imsManager = ImsManager.getInstance(this.mContext, getPhoneId());
                filter.addAction("com.android.ims.IMS_SERVICE_UP");
                filter.addAction("com.android.ims.IMS_SERVICE_DOWN");
                this.mContext.registerReceiver(this.mImsIntentReceiver, filter);
                if (getPhoneId() == HwTelephonyFactory.getHwUiccManager().getUserSwitchSlots()) {
                    if (imsManager != null && (imsManager.isDynamicBinding() || imsManager.isServiceAvailable())) {
                        this.mImsServiceReady = true;
                        updateImsPhone();
                    }
                }
            }
        }
    }

    public boolean supportsConversionOfCdmaCallerIdMmiCodesWhileRoaming() {
        PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfig();
        if (b != null) {
            return b.getBoolean("convert_cdma_caller_id_mmi_codes_while_roaming_on_3gpp_bool", false);
        }
        return false;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 16:
            case 17:
                if (msg.what == 16) {
                    setOOSFlagOnSelectNetworkManually(false);
                    restoreNetworkSelectionAuto();
                    if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
                        ServiceStateTracker tracker = getServiceStateTracker();
                        if (tracker == null || tracker.mSS == null) {
                            Rlog.d(this.LOG_TAG, "tracker is null");
                        } else {
                            tracker.mSS.setIsManualSelection(true);
                        }
                    }
                }
                if (msg.what == 17) {
                    ServiceStateTracker tracker2 = getServiceStateTracker();
                    if (tracker2 != null) {
                        tracker2.pollState();
                    } else {
                        Rlog.d(this.LOG_TAG, "tracker is null");
                    }
                }
                handleSetSelectNetwork((AsyncResult) msg.obj);
                return;
            default:
                switch (msg.what) {
                    case 14:
                        Rlog.d(this.LOG_TAG, "Event EVENT_CALL_RING Received state=" + getState());
                        if (((AsyncResult) msg.obj).exception == null) {
                            PhoneConstants.State state = getState();
                            if (!this.mDoesRilSendMultipleCallRing && (state == PhoneConstants.State.RINGING || state == PhoneConstants.State.IDLE)) {
                                this.mCallRingContinueToken++;
                                sendIncomingCallRingNotification(this.mCallRingContinueToken);
                                break;
                            } else {
                                notifyIncomingRing();
                                break;
                            }
                        }
                        break;
                    case 15:
                        Rlog.d(this.LOG_TAG, "Event EVENT_CALL_RING_CONTINUE Received state=" + getState());
                        if (getState() == PhoneConstants.State.RINGING) {
                            sendIncomingCallRingNotification(msg.arg1);
                            break;
                        }
                        break;
                    case 30:
                        onUpdateIccAvailability();
                        break;
                    case 31:
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            Rlog.e(this.LOG_TAG, "Srvcc exception: " + ar.exception);
                            break;
                        } else {
                            handleSrvccStateChanged((int[]) ar.result);
                            break;
                        }
                    case 32:
                        Rlog.d(this.LOG_TAG, "Event EVENT_INITIATE_SILENT_REDIAL Received");
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null && ar2.result != null) {
                            String dialString = (String) ar2.result;
                            if (!TextUtils.isEmpty(dialString)) {
                                try {
                                    dialInternal(dialString, new PhoneInternalInterface.DialArgs.Builder().build());
                                    break;
                                } catch (CallStateException e) {
                                    Rlog.e(this.LOG_TAG, "silent redial failed: " + e);
                                    break;
                                }
                            } else {
                                return;
                            }
                        }
                    case 34:
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception != null) {
                            Rlog.e(this.LOG_TAG, "OEM hook raw exception: " + ar3.exception);
                            break;
                        } else {
                            this.mNotifier.notifyOemHookRawEventForSubscriber(getSubId(), (byte[]) ar3.result);
                            break;
                        }
                    case 37:
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            this.mLceStatus = ((ArrayList) ar4.result).get(0).intValue();
                            break;
                        } else {
                            Rlog.d(this.LOG_TAG, "config LCE service failed: " + ar4.exception);
                            break;
                        }
                    case 38:
                        onCheckForNetworkSelectionModeAutomatic(msg);
                        break;
                    default:
                        throw new RuntimeException("unexpected event not handled");
                }
                return;
        }
    }

    public ArrayList<Connection> getHandoverConnection() {
        return null;
    }

    public void notifySrvccState(Call.SrvccState state) {
    }

    public void registerForSilentRedial(Handler h, int what, Object obj) {
    }

    public void unregisterForSilentRedial(Handler h) {
    }

    private void handleSrvccStateChanged(int[] ret) {
        Call.SrvccState srvccState;
        Rlog.d(this.LOG_TAG, "handleSrvccStateChanged");
        ArrayList<Connection> conn = null;
        Phone imsPhone = this.mImsPhone;
        Call.SrvccState srvccState2 = Call.SrvccState.NONE;
        if (!(ret == null || ret.length == 0)) {
            int state = ret[0];
            switch (state) {
                case 0:
                    srvccState = Call.SrvccState.STARTED;
                    if (imsPhone == null) {
                        Rlog.d(this.LOG_TAG, "HANDOVER_STARTED: mImsPhone null");
                        break;
                    } else {
                        conn = imsPhone.getHandoverConnection();
                        migrateFrom(imsPhone);
                        break;
                    }
                case 1:
                    if (imsPhone != null) {
                        conn = imsPhone.getHandoverConnection();
                    } else {
                        Rlog.d(this.LOG_TAG, "HANDOVER_STARTED: mImsPhone null");
                    }
                    srvccState = Call.SrvccState.COMPLETED;
                    break;
                case 2:
                case 3:
                    srvccState = Call.SrvccState.FAILED;
                    break;
                default:
                    return;
            }
            getCallTracker().notifySrvccState(srvccState, conn);
            if (imsPhone != null && state == 1) {
                imsPhone.notifySrvccState(srvccState);
            }
            notifyVoLteServiceStateChanged(new VoLteServiceState(state));
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public void disableDnsCheck(boolean b) {
        this.mDnsCheckDisabled = b;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
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

    /* access modifiers changed from: protected */
    public void notifyPreciseCallStateChangedP() {
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
        String str = this.LOG_TAG;
        Rlog.d(str, "notifyHandoverStateChanged mHandoverRegistrants.size:" + this.mHandoverRegistrants.size() + ", connection:" + cn);
        this.mHandoverRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
    }

    /* access modifiers changed from: protected */
    public void setIsInEmergencyCall() {
    }

    /* access modifiers changed from: protected */
    public void migrateFrom(Phone from) {
        String str = this.LOG_TAG;
        Rlog.d(str, "migrateFrom phone:" + from.getPhoneName() + ", mHandoverRegistrants.size:" + from.mHandoverRegistrants.size() + ", mPreciseCallStateRegistrants.size:" + from.mPreciseCallStateRegistrants.size() + ", mNewRingingConnectionRegistrants.size:" + from.mNewRingingConnectionRegistrants.size() + ", mIncomingRingRegistrants.size:" + from.mIncomingRingRegistrants.size() + ", mDisconnectRegistrants.size:" + from.mDisconnectRegistrants.size() + ", mServiceStateRegistrants.size:" + from.mServiceStateRegistrants.size() + ", mMmiCompleteRegistrants.size:" + from.mMmiCompleteRegistrants.size() + ", mMmiRegistrants.size:" + from.mMmiRegistrants.size() + ", mUnknownConnectionRegistrants.size:" + from.mUnknownConnectionRegistrants.size() + ", mSuppServiceFailedRegistrants.size:" + from.mSuppServiceFailedRegistrants.size());
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

    /* access modifiers changed from: protected */
    public void migrate(RegistrantList to, RegistrantList from) {
        from.removeCleared();
        int n = from.size();
        for (int i = 0; i < n; i++) {
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
        Message msg = obtainMessage(38);
        msg.obj = response;
        this.mCi.getNetworkSelectionMode(msg);
    }

    private void onCheckForNetworkSelectionModeAutomatic(Message fromRil) {
        AsyncResult ar = (AsyncResult) fromRil.obj;
        Message response = (Message) ar.userObj;
        boolean doAutomatic = true;
        if (ar.exception == null && ar.result != null) {
            try {
                if (((int[]) ar.result)[0] == 0) {
                    doAutomatic = false;
                }
            } catch (Exception e) {
            }
        }
        String hplmn = null;
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = "";
        nsm.operatorAlphaLong = "";
        nsm.operatorAlphaShort = "";
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            hplmn = r.getOperatorNumeric();
        }
        if (!("".equals(FORCE_AUTO_PLMN) || hplmn == null || -1 == FORCE_AUTO_PLMN.indexOf(hplmn))) {
            doAutomatic = true;
        }
        if (doAutomatic) {
            this.mCi.setNetworkSelectionModeAutomatic(obtainMessage(17, nsm));
        } else {
            Rlog.d(this.LOG_TAG, "setNetworkSelectionModeAutomatic - already auto, ignoring");
            if (nsm.message != null) {
                nsm.message.arg1 = 1;
            }
            ar.userObj = nsm;
            handleSetSelectNetwork(ar);
        }
        updateSavedNetworkOperator(nsm);
    }

    public void getNetworkSelectionMode(Message message) {
        this.mCi.getNetworkSelectionMode(message);
    }

    public List<ClientRequestStats> getClientRequestStats() {
        return this.mCi.getClientRequestStats();
    }

    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        if (network == null) {
            if (response != null) {
                AsyncResult.forMessage(response, null, new Exception("netwrok is null"));
                response.sendToTarget();
            }
            return;
        }
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = network.getOperatorNumeric();
        nsm.operatorAlphaLong = network.getOperatorAlphaLong();
        nsm.operatorAlphaShort = network.getOperatorAlphaShort();
        setOOSFlagOnSelectNetworkManually(true);
        hasNetworkSelectionAuto();
        this.mCi.setNetworkSelectionModeManual(network.getOperatorNumeric(), obtainMessage(16, nsm));
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
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putString(NETWORK_SELECTION_KEY + subId, nsm.operatorNumeric);
            editor.putString(NETWORK_SELECTION_NAME_KEY + subId, nsm.operatorAlphaLong);
            editor.putString(NETWORK_SELECTION_SHORT_KEY + subId, nsm.operatorAlphaShort);
            if (!editor.commit()) {
                Rlog.e(this.LOG_TAG, "failed to commit network selection preference");
                return;
            }
            return;
        }
        String str = this.LOG_TAG;
        Rlog.e(str, "Cannot update network selection preference due to invalid subId " + subId);
    }

    private void handleSetSelectNetwork(AsyncResult ar) {
        if (ar == null || ar.userObj == null || !(ar.userObj instanceof NetworkSelectMessage)) {
            Rlog.e(this.LOG_TAG, "unexpected result from user object.");
            return;
        }
        NetworkSelectMessage nsm = (NetworkSelectMessage) ar.userObj;
        if (nsm.message != null) {
            AsyncResult.forMessage(nsm.message, ar.result, ar.exception);
            nsm.message.sendToTarget();
        }
    }

    private OperatorInfo getSavedNetworkSelection() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String numeric = sp.getString(NETWORK_SELECTION_KEY + getSubId(), "");
        String name = sp.getString(NETWORK_SELECTION_NAME_KEY + getSubId(), "");
        return new OperatorInfo(name, sp.getString(NETWORK_SELECTION_SHORT_KEY + getSubId(), ""), numeric);
    }

    private void clearSavedNetworkSelection() {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        SharedPreferences.Editor remove = edit.remove(NETWORK_SELECTION_KEY + getSubId());
        SharedPreferences.Editor remove2 = remove.remove(NETWORK_SELECTION_NAME_KEY + getSubId());
        remove2.remove(NETWORK_SELECTION_SHORT_KEY + getSubId()).commit();
    }

    public void restoreSavedNetworkSelection(Message response) {
        OperatorInfo networkSelection = getSavedNetworkSelection();
        if (networkSelection != null && !TextUtils.isEmpty(networkSelection.getOperatorNumeric())) {
            selectNetworkManually(networkSelection, true, response);
        }
    }

    public void saveClirSetting(int commandInterfaceCLIRMode) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putInt(CLIR_KEY + getPhoneId(), commandInterfaceCLIRMode);
        String str = this.LOG_TAG;
        Rlog.i(str, "saveClirSetting: clir_key" + getPhoneId() + "=" + commandInterfaceCLIRMode);
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

    /* access modifiers changed from: protected */
    public void notifyDisconnectP(Connection cn) {
        this.mDisconnectRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
    }

    public void registerForServiceStateChanged(Handler h, int what, Object obj) {
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
        CharSequence[] carrierLocales = ctx.getResources().getTextArray(17235974);
        for (int i = 0; i < carrierLocales.length; i += 3) {
            if (carrier.equals(carrierLocales[i].toString())) {
                return Locale.forLanguageTag(carrierLocales[i + 1].toString().replace('_', '-'));
            }
        }
        return null;
    }

    public IccFileHandler getIccFileHandler() {
        IccFileHandler fh;
        UiccCardApplication uiccApplication = this.mUiccApplication.get();
        if (uiccApplication == null) {
            Rlog.d(this.LOG_TAG, "getIccFileHandler: uiccApplication == null, return null");
            fh = null;
        } else {
            fh = uiccApplication.getIccFileHandler();
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getIccFileHandler: fh=" + fh);
        return fh;
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

    public void setVoiceActivationState(int state) {
        this.mSimActivationTracker.setVoiceActivationState(state);
    }

    public void setDataActivationState(int state) {
        this.mSimActivationTracker.setDataActivationState(state);
    }

    public int getVoiceActivationState() {
        return this.mSimActivationTracker.getVoiceActivationState();
    }

    public int getDataActivationState() {
        return this.mSimActivationTracker.getDataActivationState();
    }

    public void updateVoiceMail() {
        Rlog.e(this.LOG_TAG, "updateVoiceMail() should be overridden");
    }

    public IccCardApplicationStatus.AppType getCurrentUiccAppType() {
        UiccCardApplication currentApp = this.mUiccApplication.get();
        if (currentApp != null) {
            return currentApp.getType();
        }
        return IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN;
    }

    public IccCard getIccCard() {
        return null;
    }

    public String getIccSerialNumber() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    public String getFullIccSerialNumber() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getFullIccId();
        }
        return null;
    }

    public boolean getIccRecordsLoaded() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getRecordsLoaded();
        }
        return false;
    }

    public List<CellInfo> getAllCellInfo(WorkSource workSource) {
        return privatizeCellInfoList(getServiceStateTracker().getAllCellInfo(workSource));
    }

    public CellLocation getCellLocation() {
        return getCellLocation(null);
    }

    private List<CellInfo> privatizeCellInfoList(List<CellInfo> cellInfoList) {
        if (cellInfoList == null) {
            return null;
        }
        if (Settings.Secure.getInt(getContext().getContentResolver(), "location_mode", 0) == 0) {
            ArrayList<CellInfo> privateCellInfoList = new ArrayList<>(cellInfoList.size());
            for (CellInfo c : cellInfoList) {
                if (c instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) c;
                    CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
                    CellIdentityCdma maskedCellIdentity = new CellIdentityCdma(cellIdentity.getNetworkId(), cellIdentity.getSystemId(), cellIdentity.getBasestationId(), KeepaliveStatus.INVALID_HANDLE, KeepaliveStatus.INVALID_HANDLE);
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

    public void setCellInfoListRate(int rateInMillis, WorkSource workSource) {
        this.mCi.setCellInfoListRate(rateInMillis, null, workSource);
    }

    public void setRadioPower(boolean power, Message msg) {
    }

    public boolean getMessageWaitingIndicator() {
        return this.mVmCount != 0;
    }

    private int getCallForwardingIndicatorFromSharedPref() {
        int status = 0;
        if (SubscriptionManager.isValidSubscriptionId(getSubId())) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            status = sp.getInt(CF_STATUS + subId, -1);
            Rlog.d(this.LOG_TAG, "getCallForwardingIndicatorFromSharedPref: for subId " + subId + "= " + status);
            if (status == -1) {
                String subscriberId = sp.getString(CF_ID, null);
                if (subscriberId != null) {
                    if (subscriberId.equals(getSubscriberId())) {
                        status = sp.getInt(CF_STATUS, 0);
                        boolean z = true;
                        if (status != 1) {
                            z = false;
                        }
                        setCallForwardingIndicatorInSharedPref(z);
                        Rlog.d(this.LOG_TAG, "getCallForwardingIndicatorFromSharedPref: " + status);
                    } else {
                        Rlog.d(this.LOG_TAG, "getCallForwardingIndicatorFromSharedPref: returning DISABLED as status for matching subscriberId not found");
                    }
                    SharedPreferences.Editor editor = sp.edit();
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
            status = 1;
        } else {
            status = 0;
        }
        int subId = getSubId();
        String str = this.LOG_TAG;
        Rlog.i(str, "setCallForwardingIndicatorInSharedPref: Storing status = " + status + " in pref " + CF_STATUS + subId);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        StringBuilder sb = new StringBuilder();
        sb.append(CF_STATUS);
        sb.append(subId);
        editor.putInt(sb.toString(), status);
        editor.apply();
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            r.setVoiceCallForwardingFlag(line, enable, number);
        }
    }

    /* access modifiers changed from: protected */
    public void setVoiceCallForwardingFlag(IccRecords r, int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        r.setVoiceCallForwardingFlag(line, enable, number);
    }

    public boolean getCallForwardingIndicator() {
        boolean z = false;
        if (getPhoneType() == 2) {
            Rlog.e(this.LOG_TAG, "getCallForwardingIndicator: not possible in CDMA");
            return false;
        }
        IccRecords r = this.mIccRecords.get();
        int callForwardingIndicator = -1;
        if (r != null) {
            callForwardingIndicator = r.getVoiceCallForwardingFlag();
        }
        if (callForwardingIndicator == -1) {
            callForwardingIndicator = getCallForwardingIndicatorFromSharedPref();
        }
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("getCallForwardingIndicator: iccForwardingFlag=");
        sb.append(r != null ? Integer.valueOf(r.getVoiceCallForwardingFlag()) : "null");
        sb.append(", sharedPrefFlag=");
        sb.append(getCallForwardingIndicatorFromSharedPref());
        Rlog.v(str, sb.toString());
        if (callForwardingIndicator == 1) {
            z = true;
        }
        return z;
    }

    public CarrierSignalAgent getCarrierSignalAgent() {
        return this.mCarrierSignalAgent;
    }

    public void setCallForwardingPreference(boolean enabled) {
        Rlog.d(this.LOG_TAG, "Set callforwarding info to perferences");
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        edit.putBoolean("cf_enabled_key" + getSubId(), enabled);
        edit.commit();
        setVmSimImsi(getSubscriberId());
    }

    public boolean getCallForwardingPreference() {
        Rlog.d(this.LOG_TAG, "Get callforwarding info from perferences");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (!sp.contains("cf_enabled_key" + getSubId())) {
                if (sp.contains("cf_enabled_key" + this.mPhoneId)) {
                    setCallForwardingPreference(sp.getBoolean("cf_enabled_key" + this.mPhoneId, false));
                    SharedPreferences.Editor edit = sp.edit();
                    edit.remove("cf_enabled_key" + this.mPhoneId);
                    edit.commit();
                }
            }
        } else {
            if (!sp.contains("cf_enabled_key" + getSubId()) && sp.contains("cf_enabled_key")) {
                setCallForwardingPreference(sp.getBoolean("cf_enabled_key", false));
                SharedPreferences.Editor edit2 = sp.edit();
                edit2.remove("cf_enabled_key");
                edit2.commit();
            }
        }
        return sp.getBoolean("cf_enabled_key" + getSubId(), false);
    }

    public String getVmSimImsi() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (!sp.contains(SIM_IMSI + getSubId())) {
                if (sp.contains(VM_SIM_IMSI + this.mPhoneId)) {
                    String imsi = sp.getString(VM_SIM_IMSI + this.mPhoneId, null);
                    if (!(imsi == null || mPasswordUtil == null)) {
                        String oldDecodeVmSimImsi = mPasswordUtil.pswd2PlainText(imsi);
                        try {
                            imsi = new String(Base64.decode(imsi, 0), "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            Rlog.e(this.LOG_TAG, "getVmSimImsi UnsupportedEncodingException");
                        } catch (Exception e2) {
                            Rlog.e(this.LOG_TAG, "getVmSimImsi Exception");
                        }
                        if (!imsi.equals(getSubscriberId()) && oldDecodeVmSimImsi.equals(getSubscriberId())) {
                            Rlog.d(this.LOG_TAG, "getVmSimImsi: Old IMSI encryption is not supported, now setVmSimImsi again.");
                            setVmSimImsi(oldDecodeVmSimImsi);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.remove(VM_SIM_IMSI + this.mPhoneId);
                            editor.commit();
                        }
                    }
                }
            }
        } else {
            if (!sp.contains(SIM_IMSI + getSubId()) && sp.contains(VM_SIM_IMSI)) {
                String imsi2 = sp.getString(VM_SIM_IMSI, null);
                if (!(imsi2 == null || mPasswordUtil == null)) {
                    String oldDecodeVmSimImsi2 = mPasswordUtil.pswd2PlainText(imsi2);
                    try {
                        imsi2 = new String(Base64.decode(imsi2, 0), "utf-8");
                    } catch (UnsupportedEncodingException e3) {
                        Rlog.e(this.LOG_TAG, "getVmSimImsi UnsupportedEncodingException");
                    } catch (Exception e4) {
                        Rlog.e(this.LOG_TAG, "getVmSimImsi Exception");
                    }
                    if (!imsi2.equals(getSubscriberId()) && oldDecodeVmSimImsi2.equals(getSubscriberId())) {
                        Rlog.d(this.LOG_TAG, "getVmSimImsi: Old IMSI encryption is not supported, now setVmSimImsi again.");
                        setVmSimImsi(oldDecodeVmSimImsi2);
                        SharedPreferences.Editor editor2 = sp.edit();
                        editor2.remove(VM_SIM_IMSI);
                        editor2.commit();
                    }
                }
            }
        }
        String simimsi = sp.getString(SIM_IMSI + getSubId(), null);
        if (simimsi == null) {
            return simimsi;
        }
        try {
            return new String(Base64.decode(simimsi, 0), "utf-8");
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
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            if (imsi != null) {
                editor.putString(SIM_IMSI + getSubId(), new String(Base64.encode(imsi.getBytes("utf-8"), 0), "utf-8"));
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

    public CarrierActionAgent getCarrierActionAgent() {
        return this.mCarrierActionAgent;
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

    public boolean isConcurrentVoiceAndDataAllowed() {
        ServiceStateTracker sst = getServiceStateTracker();
        if (sst == null) {
            return false;
        }
        return sst.isConcurrentVoiceAndDataAllowed();
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
        } else if (HuaweiTelephonyConfigs.isHisiPlatform() || TelephonyManager.getDefault().getPhoneCount() <= 1) {
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

    @Deprecated
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        this.mCi.invokeOemRilRequestRaw(data, response);
    }

    @Deprecated
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

    public void notifyDataConnection(String reason, String apnType, PhoneConstants.DataState state) {
        this.mNotifier.notifyDataConnection(this, reason, apnType, state);
    }

    public void notifyDataConnection(String reason, String apnType) {
        this.mNotifier.notifyDataConnection(this, reason, apnType, getDataConnectionState(apnType));
    }

    public void notifyDataConnection(String reason) {
        for (String apnType : getActiveApnTypes()) {
            this.mNotifier.notifyDataConnection(this, reason, apnType, getDataConnectionState(apnType));
        }
    }

    public void notifyOtaspChanged(int otaspMode) {
        this.mNotifier.notifyOtaspChanged(this, otaspMode);
    }

    public void notifyVoiceActivationStateChanged(int state) {
        this.mNotifier.notifyVoiceActivationStateChanged(this, state);
    }

    public void notifyDataActivationStateChanged(int state) {
        this.mNotifier.notifyDataActivationStateChanged(this, state);
    }

    public void notifyUserMobileDataStateChanged(boolean state) {
        this.mNotifier.notifyUserMobileDataStateChanged(this, state);
    }

    public void notifySignalStrength() {
        this.mNotifier.notifySignalStrength(this);
    }

    public void notifyCellInfo(List<CellInfo> cellInfo) {
        this.mNotifier.notifyCellInfo(this, privatizeCellInfoList(cellInfo));
    }

    public void notifyPhysicalChannelConfiguration(List<PhysicalChannelConfig> configs) {
        this.mNotifier.notifyPhysicalChannelConfiguration(this, configs);
    }

    public void notifyVoLteServiceStateChanged(VoLteServiceState lteState) {
        this.mNotifier.notifyVoLteServiceStateChanged(this, lteState);
    }

    public boolean isInEmergencyCall() {
        return false;
    }

    protected static boolean getInEcmMode() {
        return SystemProperties.getBoolean("ril.cdma.inecmmode", false);
    }

    public boolean isInEcm() {
        return this.mIsPhoneInEcmState;
    }

    public void setIsInEcm(boolean isInEcm) {
        setGlobalSystemProperty("ril.cdma.inecmmode", String.valueOf(isInEcm));
        this.mIsPhoneInEcmState = isInEcm;
    }

    private static int getVideoState(Call call) {
        Connection conn = call.getEarliestConnection();
        if (conn != null) {
            return conn.getVideoState();
        }
        return 0;
    }

    private boolean isVideoCallOrConference(Call call) {
        boolean z = true;
        if (call.isMultiparty()) {
            return true;
        }
        if (!(call instanceof ImsPhoneCall)) {
            return false;
        }
        ImsCall imsCall = ((ImsPhoneCall) call).getImsCall();
        if (imsCall == null || (!imsCall.isVideoCall() && !imsCall.wasVideoCall())) {
            z = false;
        }
        return z;
    }

    public boolean isImsVideoCallOrConferencePresent() {
        boolean isPresent = false;
        if (this.mImsPhone != null) {
            isPresent = isVideoCallOrConference(this.mImsPhone.getForegroundCall()) || isVideoCallOrConference(this.mImsPhone.getBackgroundCall()) || isVideoCallOrConference(this.mImsPhone.getRingingCall());
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "isImsVideoCallOrConferencePresent: " + isPresent);
        return isPresent;
    }

    public int getVoiceMessageCount() {
        return this.mVmCount;
    }

    public void setVoiceMessageCount(int countWaiting) {
        this.mVmCount = countWaiting;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            String str = this.LOG_TAG;
            Rlog.d(str, "setVoiceMessageCount: Storing Voice Mail Count = " + countWaiting + " for mVmCountKey = " + VM_COUNT + subId + " in preferences.");
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            StringBuilder sb = new StringBuilder();
            sb.append(VM_COUNT);
            sb.append(subId);
            editor.putInt(sb.toString(), countWaiting);
            editor.apply();
        } else {
            String str2 = this.LOG_TAG;
            Rlog.e(str2, "setVoiceMessageCount in sharedPreference: invalid subId " + subId);
        }
        notifyMessageWaitingIndicator();
    }

    /* access modifiers changed from: protected */
    public int getStoredVoiceMessageCount() {
        int countVoiceMessages = 0;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            int countFromSP = sp.getInt(VM_COUNT + subId, -2);
            if (countFromSP != -2) {
                int countVoiceMessages2 = countFromSP;
                String str = this.LOG_TAG;
                Rlog.d(str, "getStoredVoiceMessageCount: from preference for subId " + subId + "= " + countVoiceMessages2);
                return countVoiceMessages2;
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
                String str2 = this.LOG_TAG;
                Rlog.d(str2, "getStoredVoiceMessageCount: from preference = " + countVoiceMessages);
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(VM_ID);
            editor.remove(VM_COUNT);
            editor.apply();
            return countVoiceMessages;
        }
        String str3 = this.LOG_TAG;
        Rlog.e(str3, "getStoredVoiceMessageCount: invalid subId " + subId);
        return 0;
    }

    public void sendDialerSpecialCode(String code) {
        if (!TextUtils.isEmpty(code)) {
            Intent intent = new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + code));
            intent.addFlags(16777216);
            this.mContext.sendBroadcast(intent);
        }
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
        return false;
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
        return false;
    }

    public boolean isOtaSpNumber(String dialStr) {
        return false;
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

    public boolean isDataAllowed() {
        return this.mDcTracker != null && this.mDcTracker.isDataAllowed(null);
    }

    public boolean isDataAllowed(DataConnectionReasons reasons) {
        return this.mDcTracker != null && this.mDcTracker.isDataAllowed(reasons);
    }

    public void carrierActionSetMeteredApnsEnabled(boolean enabled) {
        this.mCarrierActionAgent.carrierActionSetMeteredApnsEnabled(enabled);
    }

    public void carrierActionSetRadioEnabled(boolean enabled) {
        this.mCarrierActionAgent.carrierActionSetRadioEnabled(enabled);
    }

    public void carrierActionReportDefaultNetworkStatus(boolean report) {
        this.mCarrierActionAgent.carrierActionReportDefaultNetworkStatus(report);
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
        String str = this.LOG_TAG;
        Rlog.d(str, "notifyMccChanged new mcc is " + mcc);
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
        if (!this.mIsVoiceCapable || this.mDoesRilSendMultipleCallRing || token != this.mCallRingContinueToken) {
            String str = this.LOG_TAG;
            Rlog.d(str, "Ignoring ring notification request, mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing + " token=" + token + " mCallRingContinueToken=" + this.mCallRingContinueToken + " mIsVoiceCapable=" + this.mIsVoiceCapable);
            return;
        }
        Rlog.d(this.LOG_TAG, "Sending notifyIncomingRing");
        notifyIncomingRing();
        sendMessageDelayed(obtainMessage(15, token, 0), (long) this.mCallRingDelay);
    }

    public boolean isCspPlmnEnabled() {
        return false;
    }

    public IsimRecords getIsimRecords() {
        Rlog.e(this.LOG_TAG, "getIsimRecords() is only supported on LTE devices");
        return null;
    }

    public String getMsisdn() {
        return null;
    }

    public String getPlmn() {
        return null;
    }

    public PhoneConstants.DataState getDataConnectionState() {
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
        IccRecords r = this.mIccRecords.get();
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

    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int keyType) {
        return null;
    }

    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo) {
    }

    public int getCarrierId() {
        return -1;
    }

    public String getCarrierName() {
        return null;
    }

    public int getCarrierIdListVersion() {
        return -1;
    }

    public void resetCarrierKeysForImsiEncryption() {
    }

    public boolean isUtEnabled() {
        if (this.mImsPhone != null) {
            return this.mImsPhone.isUtEnabled();
        }
        return false;
    }

    public void dispose() {
    }

    /* access modifiers changed from: private */
    public void updateImsPhone() {
        String str = this.LOG_TAG;
        Rlog.d(str, "updateImsPhone mImsServiceReady=" + this.mImsServiceReady);
        if (this.mImsServiceReady && this.mImsPhone == null) {
            this.mImsPhone = PhoneFactory.makeImsPhone(this.mNotifier, this);
            HwTelephonyFactory.getHwDataConnectionManager().registerImsCallStates(true, this.mPhoneId);
            CallManager.getInstance().registerPhone(this.mImsPhone);
            if (this.mImsPhone != null) {
                this.mImsPhone.registerForSilentRedial(this, 32, null);
            }
        } else if (!this.mImsServiceReady && this.mImsPhone != null) {
            CallManager.getInstance().unregisterPhone(this.mImsPhone);
            HwTelephonyFactory.getHwDataConnectionManager().registerImsCallStates(false, this.mPhoneId);
            this.mImsPhone.unregisterForSilentRedial(this);
            this.mImsPhone.dispose();
            this.mImsPhone = null;
        }
    }

    /* access modifiers changed from: protected */
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
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
        return false;
    }

    public boolean setRoamingOverride(List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) {
        String iccId = getIccSerialNumber();
        if (TextUtils.isEmpty(iccId)) {
            return false;
        }
        setRoamingOverrideHelper(gsmRoamingList, GSM_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(gsmNonRoamingList, GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(cdmaRoamingList, CDMA_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(cdmaNonRoamingList, CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        ServiceStateTracker tracker = getServiceStateTracker();
        if (tracker != null) {
            tracker.pollState();
        }
        return true;
    }

    private void setRoamingOverrideHelper(List<String> list, String prefix, String iccId) {
        SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
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
        boolean isImsRegistered = false;
        if (imsPhone != null) {
            isImsRegistered = imsPhone.isImsRegistered();
        } else {
            ServiceStateTracker sst = getServiceStateTracker();
            if (sst != null) {
                isImsRegistered = sst.isImsRegistered();
            }
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "isImsRegistered =" + isImsRegistered);
        return isImsRegistered;
    }

    public boolean isWifiCallingEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isWifiCallingEnabled = false;
        if (imsPhone != null) {
            isWifiCallingEnabled = imsPhone.isWifiCallingEnabled();
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "isWifiCallingEnabled =" + isWifiCallingEnabled);
        return isWifiCallingEnabled;
    }

    public boolean isVolteEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isVolteEnabled = false;
        if (imsPhone != null) {
            isVolteEnabled = imsPhone.isVolteEnabled();
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "isImsRegistered =" + isVolteEnabled);
        return isVolteEnabled;
    }

    public int getImsRegistrationTech() {
        Phone imsPhone = this.mImsPhone;
        int regTech = -1;
        if (imsPhone != null) {
            regTech = imsPhone.getImsRegistrationTech();
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getImsRegistrationTechnology =" + regTech);
        return regTech;
    }

    private boolean getRoamingOverrideHelper(String prefix, String key) {
        String iccId = getIccSerialNumber();
        if (TextUtils.isEmpty(iccId) || TextUtils.isEmpty(key)) {
            return false;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        Set<String> value = sp.getStringSet(prefix + iccId, null);
        if (value == null) {
            return false;
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
        if (rc == null) {
            return 1;
        }
        return rc.getRadioAccessFamily();
    }

    public String getModemUuId() {
        RadioCapability rc = getRadioCapability();
        return rc == null ? "" : rc.getLogicalModemUuid();
    }

    public RadioCapability getRadioCapability() {
        return this.mRadioCapability.get();
    }

    public void radioCapabilityUpdated(RadioCapability rc) {
        this.mRadioCapability.set(rc);
        if (SubscriptionManager.isValidSubscriptionId(getSubId())) {
            Rlog.d(this.LOG_TAG, "skip restoreNetworkSelection in sendSubscriptionSettings");
            sendSubscriptionSettings(!this.mContext.getResources().getBoolean(17957110));
        }
    }

    public void sendSubscriptionSettings(boolean restoreNetworkSelection) {
        Rlog.d(this.LOG_TAG, "skip setPreferredNetworkType in sendSubscriptionSettings");
        if (restoreNetworkSelection) {
            restoreSavedNetworkSelection(null);
        }
    }

    /* access modifiers changed from: protected */
    public void setPreferredNetworkTypeIfSimLoaded() {
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
        boolean z = false;
        if (this.mContext == null) {
            Rlog.d(this.LOG_TAG, "isImsUseEnabled, context=null, return");
            return false;
        }
        ImsManager imsManager = ImsManager.getInstance(this.mContext, getPhoneId());
        boolean volteEnableByPlatform = imsManager.isVolteEnabledByPlatform();
        boolean volteEnableByUser = imsManager.isEnhanced4gLteModeSettingEnabledByUser();
        boolean wfcEnableByPlatform = imsManager.isWfcEnabledByPlatform();
        boolean wfcEnableByUser = imsManager.isWfcEnabledByUser();
        boolean nonTtyOrTtyOnVolteEnabled = imsManager.isNonTtyOrTtyOnVolteEnabled();
        String str = this.LOG_TAG;
        Rlog.d(str, " VolteEnabledByPlatform=" + volteEnableByPlatform + " Enhanced4gLteModeSettingEnabledByUser=" + volteEnableByUser + " WfcEnabledByPlatform=" + wfcEnableByPlatform + " WfcEnabledByUser=" + wfcEnableByUser + " NonTtyOrTtyOnVolteEnabled=" + nonTtyOrTtyOnVolteEnabled);
        if ((volteEnableByPlatform && volteEnableByUser) || (wfcEnableByPlatform && wfcEnableByUser && nonTtyOrTtyOnVolteEnabled)) {
            z = true;
        }
        return z;
    }

    public boolean isImsAvailable() {
        if (this.mImsPhone == null) {
            return false;
        }
        return this.mImsPhone.isImsAvailable();
    }

    public boolean isVideoEnabled() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null) {
            return imsPhone.isVideoEnabled();
        }
        return false;
    }

    public int getLceStatus() {
        return this.mLceStatus;
    }

    public void getModemActivityInfo(Message response) {
        this.mCi.getModemActivityInfo(response);
    }

    public void startLceAfterRadioIsAvailable() {
        this.mCi.startLceService(200, true, obtainMessage(37));
    }

    public void setAllowedCarriers(List<CarrierIdentifier> carriers, Message response) {
        this.mCi.setAllowedCarriers(carriers, response);
    }

    public void setSignalStrengthReportingCriteria(int[] thresholds, int ran) {
    }

    public void setLinkCapacityReportingCriteria(int[] dlThresholds, int[] ulThresholds, int ran) {
    }

    public void getAllowedCarriers(Message response) {
        this.mCi.getAllowedCarriers(response);
    }

    public Locale getLocaleFromSimAndCarrierPrefs() {
        IccRecords records = this.mIccRecords.get();
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
        return false;
    }

    public void registerForAllDataDisconnected(Handler h, int what, Object obj) {
        this.mDcTracker.registerForAllDataDisconnected(h, what, obj);
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mDcTracker.unregisterForAllDataDisconnected(h);
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDcTracker.registerForDataEnabledChanged(h, what, obj);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDcTracker.unregisterForDataEnabledChanged(h);
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isMatchGid(String gid) {
        String gid1 = getGroupIdLevel1();
        int gidLength = gid.length();
        if (TextUtils.isEmpty(gid1) || gid1.length() < gidLength || !gid1.substring(0, gidLength).equalsIgnoreCase(gid)) {
            return false;
        }
        return true;
    }

    public static void checkWfcWifiOnlyModeBeforeDial(Phone imsPhone, int phoneId, Context context) throws CallStateException {
        if (imsPhone == null || !imsPhone.isWifiCallingEnabled()) {
            ImsManager imsManager = ImsManager.getInstance(context, phoneId);
            if (imsManager.isWfcEnabledByPlatform() && imsManager.isWfcEnabledByUser() && imsManager.getWfcMode() == 0) {
                throw new CallStateException(1, "WFC Wi-Fi Only Mode: IMS not registered");
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

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        if (this.mImsPhone == null) {
            return null;
        }
        return this.mImsPhone.getVtDataUsage(perUidStats);
    }

    public void setPolicyDataEnabled(boolean enabled) {
        this.mDcTracker.setPolicyDataEnabled(enabled);
    }

    public Uri[] getCurrentSubscriberUris() {
        return null;
    }

    public AppSmsManager getAppSmsManager() {
        return this.mAppSmsManager;
    }

    public void setSimPowerState(int state) {
        this.mCi.setSimCardPower(state, null);
    }

    public void setRadioIndicationUpdateMode(int filters, int mode) {
        if (this.mDeviceStateMonitor != null) {
            this.mDeviceStateMonitor.setIndicationUpdateMode(filters, mode);
        }
    }

    public void setCarrierTestOverride(String mccmnc, String imsi, String iccid, String gid1, String gid2, String pnn, String spn) {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            r.setCarrierTestOverride(mccmnc, imsi, iccid, gid1, gid2, pnn, spn);
        }
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
        pw.println(" getIccSerialNumber()=" + SubscriptionInfo.givePrintableIccid(getIccSerialNumber()));
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
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mCarrierActionAgent != null) {
            try {
                this.mCarrierActionAgent.dump(fd, pw, args);
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mCarrierSignalAgent != null) {
            try {
                this.mCarrierSignalAgent.dump(fd, pw, args);
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (getCallTracker() != null) {
            try {
                getCallTracker().dump(fd, pw, args);
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mSimActivationTracker != null) {
            try {
                this.mSimActivationTracker.dump(fd, pw, args);
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mDeviceStateMonitor != null) {
            pw.println("DeviceStateMonitor:");
            this.mDeviceStateMonitor.dump(fd, pw, args);
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mCi != null && (this.mCi instanceof RIL)) {
            try {
                ((RIL) this.mCi).dump(fd, pw, args);
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
    }

    public String getCdmaGsmImsi() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getCdmaGsmImsi();
        }
        return null;
    }

    public int getUiccCardType() {
        IccCardApplicationStatus.AppType at = getCurrentUiccAppType();
        String str = this.LOG_TAG;
        Rlog.d(str, "at = " + at);
        switch (at) {
            case APPTYPE_SIM:
            case APPTYPE_RUIM:
                return 1;
            case APPTYPE_CSIM:
            case APPTYPE_ISIM:
            case APPTYPE_USIM:
                return 2;
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

    public void registerForCdmaWaitingNumberChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForCdmaWaitingNumberChanged(Handler h) {
    }
}
