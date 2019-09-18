package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.os.AsyncResult;
import android.os.BaseBundle;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.DataSpecificRegistrationStates;
import android.telephony.NetworkRegistrationState;
import android.telephony.PhysicalChannelConfig;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoiceSpecificRegistrationStates;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.view.Display;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.telephony.util.TimeStampedValue;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.PatternSyntaxException;

public class ServiceStateTracker extends Handler {
    private static final String ACTION_COMMFORCE = "huawei.intent.action.COMMFORCE";
    private static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    private static final boolean CLEAR_NITZ_WHEN_REG = SystemProperties.getBoolean("ro.config.clear_nitz_when_reg", true);
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    public static final int CS_REJECT_CAUSE_ENABLED = 2001;
    public static final int CS_REJECT_CAUSE_NOTIFICATION = 111;
    static final boolean DBG = true;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    protected static final int DELAYED_ECC_TO_NOSERVICE_VALUE = SystemProperties.getInt("ro.ecc_to_noservice.timer", 0);
    private static final boolean ENABLE_DEMO = SystemProperties.getBoolean("ro.config.enable_demo", false);
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
    protected static final int EVENT_ERI_FILE_LOADED = 36;
    protected static final int EVENT_GET_AD_DONE = 1002;
    protected static final int EVENT_GET_CELL_INFO_LIST = 43;
    protected static final int EVENT_GET_LOC_DONE = 15;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE = 19;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 3;
    public static final int EVENT_ICC_CHANGED = 42;
    protected static final int EVENT_IMS_CAPABILITY_CHANGED = 48;
    protected static final int EVENT_IMS_SERVICE_STATE_CHANGED = 53;
    protected static final int EVENT_IMS_STATE_CHANGED = 46;
    protected static final int EVENT_IMS_STATE_DONE = 47;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED = 18;
    protected static final int EVENT_NETWORK_STATE_CHANGED = 2;
    protected static final int EVENT_NITZ_TIME = 11;
    protected static final int EVENT_NV_READY = 35;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE = 37;
    protected static final int EVENT_PHONE_TYPE_SWITCHED = 50;
    protected static final int EVENT_PHYSICAL_CHANNEL_CONFIG = 55;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH = 10;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION = 34;
    protected static final int EVENT_POLL_STATE_GPRS = 5;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 6;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_POWER_FROM_CARRIER = 51;
    protected static final int EVENT_RADIO_POWER_OFF = 56;
    protected static final int EVENT_RADIO_POWER_OFF_DONE = 54;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
    protected static final int EVENT_SIM_NOT_INSERTED = 52;
    protected static final int EVENT_SIM_READY = 17;
    protected static final int EVENT_SIM_RECORDS_LOADED = 16;
    protected static final int EVENT_UNSOL_CELL_INFO_LIST = 44;
    private static final String EXTRA_SHOW_EMERGENCYONLY = "showEmergencyOnly";
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_WIFI = "wifi";
    private static final boolean FEATURE_DELAY_UPDATE_SIGANL_STENGTH = SystemProperties.getBoolean("ro.config.delay_send_signal", true);
    protected static final boolean HW_FAST_SET_RADIO_OFF = SystemProperties.getBoolean("ro.config.hw_fast_set_radio_off", false);
    private static final int HW_OPTA = SystemProperties.getInt("ro.config.hw_opta", -1);
    private static final int HW_OPTB = SystemProperties.getInt("ro.config.hw_optb", -1);
    private static final boolean IGNORE_GOOGLE_NON_ROAMING = true;
    private static final int INVALID_LTE_EARFCN = -1;
    public static final String INVALID_MCC = "000";
    public static final boolean ISDEMO = ((HW_OPTA == 735 && HW_OPTB == 156) || ENABLE_DEMO);
    private static final boolean IS_HISI_PLATFORM = HuaweiTelephonyConfigs.isHisiPlatform();
    private static final boolean IS_QCOM_PLATFORM = HuaweiTelephonyConfigs.isQcomPlatform();
    private static final long LAST_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    private static final boolean MDOEM_WORK_MODE_IS_SRLTE = SystemProperties.getBoolean("ro.config.hw_srlte", false);
    private static final int MS_PER_HOUR = 3600000;
    protected static final int NOT_REGISTERED_ON_CDMA_SYSTEM = -1;
    private static final String PERMISSION_COMM_FORCE = "android.permission.COMM_FORCE";
    private static boolean PLUS_TRANFER_IN_MDOEM = HwModemCapability.isCapabilitySupport(2);
    private static final int POLL_PERIOD_MILLIS = 20000;
    protected static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
    protected static final int PS_CS = 1;
    public static final int PS_DISABLED = 1002;
    public static final int PS_ENABLED = 1001;
    public static final int PS_NOTIFICATION = 888;
    protected static final int PS_ONLY = 0;
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    protected static final String REGISTRATION_DENIED_GEN = "General";
    protected static final boolean RESET_PROFILE = SystemProperties.getBoolean("ro.hwpp_reset_profile", false);
    public static final String UNACTIVATED_MIN2_VALUE = "000000";
    public static final String UNACTIVATED_MIN_VALUE = "1111110111";
    protected static final boolean VDBG = false;
    protected static final boolean display_blank_ons = "true".equals(SystemProperties.get("ro.config.hw_no_display_ons", "false"));
    private String LOG_TAG = "ServiceStateTracker";
    private boolean hasUpdateCellLocByPS = false;
    protected boolean isCurrent3GPsCsAllowed = true;
    /* access modifiers changed from: private */
    public boolean mAlarmSwitch = false;
    private final LocalLog mAttachLog = new LocalLog(10);
    protected RegistrantList mAttachedRegistrants = new RegistrantList();
    private CarrierServiceStateTracker mCSST;
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public CellLocation mCellLoc;
    protected CommandsInterface mCi;
    private final ContentResolver mCr;
    private String mCurDataSpn = null;
    /* access modifiers changed from: private */
    public String mCurPlmn = null;
    private String mCurRegplmn = null;
    /* access modifiers changed from: private */
    public boolean mCurShowPlmn = false;
    /* access modifiers changed from: private */
    public boolean mCurShowSpn = false;
    private boolean mCurShowWifi = false;
    /* access modifiers changed from: private */
    public String mCurSpn = null;
    private String mCurWifi = null;
    private String mCurrentCarrier = null;
    private int mCurrentOtaspMode = 0;
    private RegistrantList mDataRegStateOrRatChangedRegistrants = new RegistrantList();
    private boolean mDataRoaming = false;
    private RegistrantList mDataRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mDataRoamingOnRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public Display mDefaultDisplay;
    /* access modifiers changed from: private */
    public int mDefaultDisplayState = 0;
    private int mDefaultRoamingIndicator;
    protected boolean mDesiredPowerState;
    protected RegistrantList mDetachedRegistrants = new RegistrantList();
    private boolean mDeviceShuttingDown = false;
    private final DisplayManager.DisplayListener mDisplayListener = new SstDisplayListener();
    protected boolean mDontPollSignalStrength = false;
    private ArrayList<Pair<Integer, Integer>> mEarfcnPairListForRsrpBoost = null;
    private boolean mEmergencyOnly = false;
    private boolean mGsmRoaming = false;
    private final HandlerThread mHandlerThread;
    private HbpcdUtils mHbpcdUtils = null;
    private int[] mHomeNetworkId = null;
    private int[] mHomeSystemId = null;
    /* access modifiers changed from: private */
    public HwCustGsmServiceStateTracker mHwCustGsmServiceStateTracker;
    protected IccRecords mIccRecords = null;
    private boolean mImsRegistered = false;
    private boolean mImsRegistrationOnOff = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                    ServiceStateTracker serviceStateTracker = ServiceStateTracker.this;
                    serviceStateTracker.loge("intent: " + intent.getAction());
                    String unused = ServiceStateTracker.this.mCurPlmn = null;
                    ServiceStateTracker.this.updateSpnDisplay();
                    if (ServiceStateTracker.this.mHwCustGsmServiceStateTracker != null && intent.getBooleanExtra("state", false)) {
                        ServiceStateTracker.this.mHwCustGsmServiceStateTracker.clearPcoValue(ServiceStateTracker.this.mPhone);
                    }
                } else if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                    ServiceStateTracker.this.onCarrierConfigChanged();
                } else if (!ServiceStateTracker.this.mPhone.isPhoneTypeGsm()) {
                    ServiceStateTracker serviceStateTracker2 = ServiceStateTracker.this;
                    serviceStateTracker2.loge("Ignoring intent " + intent + " received on CDMA phone");
                } else {
                    if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                        ServiceStateTracker.this.updateSpnDisplay();
                    } else if (ServiceStateTracker.ACTION_RADIO_OFF.equals(intent.getAction())) {
                        boolean unused2 = ServiceStateTracker.this.mAlarmSwitch = false;
                        ServiceStateTracker.this.powerOffRadioSafely(ServiceStateTracker.this.mPhone.mDcTracker);
                    } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction()) || "android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) || ServiceStateTracker.ACTION_COMMFORCE.equals(intent.getAction()) || "android.intent.action.HEADSET_PLUG".equals(intent.getAction()) || "android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                        ServiceStateTracker.this.mPhone.updateReduceSARState();
                    }
                }
            }
        }
    };
    private boolean mIsEriTextLoaded = false;
    private boolean mIsInPrl;
    private boolean mIsMinInfoReady = false;
    private boolean mIsSimReady = false;
    private boolean mIsSubscriptionFromRuim = false;
    private List<CellInfo> mLastCellInfoList = null;
    private long mLastCellInfoListTime;
    private List<PhysicalChannelConfig> mLastPhysicalChannelConfigList = null;
    long mLastReceivedNITZReferenceTime;
    private SignalStrength mLastSignalStrength = null;
    private final LocaleTracker mLocaleTracker;
    private int mLteRsrpBoost = 0;
    private final Object mLteRsrpBoostLock = new Object();
    private int mMaxDataCalls = 1;
    private String mMdn;
    private String mMin;
    protected String mMlplVersion;
    protected String mMsplVersion;
    private RegistrantList mNetworkAttachedRegistrants = new RegistrantList();
    private RegistrantList mNetworkDetachedRegistrants = new RegistrantList();
    private CellLocation mNewCellLoc;
    private int mNewMaxDataCalls = 1;
    private int mNewReasonDataDenied = -1;
    private int mNewRejectCode;
    protected ServiceState mNewSS;
    private final NitzStateMachine mNitzState;
    private Notification mNotification;
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SstSubscriptionsChangedListener();
    private boolean mPendingRadioPowerOffAfterDataOff = false;
    private int mPendingRadioPowerOffAfterDataOffTag = 0;
    /* access modifiers changed from: private */
    public final GsmCdmaPhone mPhone;
    private final LocalLog mPhoneTypeLog = new LocalLog(10);
    @VisibleForTesting
    public int[] mPollingContext;
    private boolean mPowerOffDelayNeed = true;
    private boolean mPreVowifiState = false;
    private int mPreferredNetworkType;
    /* access modifiers changed from: private */
    public int mPrevSubId = -1;
    private String mPrlVersion;
    private RegistrantList mPsRestrictDisabledRegistrants = new RegistrantList();
    private RegistrantList mPsRestrictEnabledRegistrants = new RegistrantList();
    private boolean mRadioDisabledByCarrier = false;
    private boolean mRadioOffByDoRecovery = false;
    private PendingIntent mRadioOffIntent = null;
    private final LocalLog mRadioPowerLog = new LocalLog(20);
    private final LocalLog mRatLog = new LocalLog(20);
    private final RatRatcheter mRatRatcheter;
    private int mReasonDataDenied = -1;
    private boolean mRecoverAutoSelectMode = false;
    private final SparseArray<NetworkRegistrationManager> mRegStateManagers = new SparseArray<>();
    private String mRegistrationDeniedReason;
    private int mRegistrationState = -1;
    private int mRejectCode;
    private boolean mReportedGprsNoReg;
    public RestrictedState mRestrictedState;
    private int mRoamingIndicator;
    private final LocalLog mRoamingLog = new LocalLog(10);
    private boolean mRplmnIsNull = false;
    public ServiceState mSS;
    protected SignalStrength mSignalStrength;
    private boolean mSimCardsLoaded = false;
    /* access modifiers changed from: private */
    public boolean mSpnUpdatePending = false;
    private boolean mStartedGprsRegCheck;
    @VisibleForTesting
    public int mSubId = -1;
    /* access modifiers changed from: private */
    public SubscriptionController mSubscriptionController;
    private SubscriptionManager mSubscriptionManager;
    private final TransportManager mTransportManager;
    protected UiccCardApplication mUiccApplcation = null;
    private UiccController mUiccController = null;
    protected boolean mVoiceCapable;
    private RegistrantList mVoiceRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mVoiceRoamingOnRegistrants = new RegistrantList();
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;

    private class CellInfoResult {
        List<CellInfo> list;
        Object lockObj;

        private CellInfoResult() {
            this.lockObj = new Object();
        }
    }

    public class SstDisplayListener implements DisplayManager.DisplayListener {
        public SstDisplayListener() {
        }

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                int oldState = ServiceStateTracker.this.mDefaultDisplayState;
                int unused = ServiceStateTracker.this.mDefaultDisplayState = ServiceStateTracker.this.mDefaultDisplay.getState();
                if (ServiceStateTracker.this.mDefaultDisplayState != oldState && ServiceStateTracker.this.mDefaultDisplayState == 2 && !ServiceStateTracker.this.mDontPollSignalStrength) {
                    ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(10));
                }
            }
        }
    }

    private class SstSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        private SstSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            ServiceStateTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = ServiceStateTracker.this.mPhone.getSubId();
            int unused = ServiceStateTracker.this.mPrevSubId = this.mPreviousSubId.get();
            if (this.mPreviousSubId.getAndSet(subId) != subId) {
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    Context context = ServiceStateTracker.this.mPhone.getContext();
                    ServiceStateTracker.this.mPhone.notifyPhoneStateChanged();
                    ServiceStateTracker.this.mPhone.notifyCallForwardingIndicator();
                    ServiceStateTracker.this.mPhone.sendSubscriptionSettings(!context.getResources().getBoolean(17957110));
                    ServiceStateTracker.this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(ServiceStateTracker.this.mSS.getRilDataRadioTechnology()));
                    if (ServiceStateTracker.this.mSpnUpdatePending) {
                        ServiceStateTracker.this.mSubscriptionController.setPlmnSpn(ServiceStateTracker.this.mPhone.getPhoneId(), ServiceStateTracker.this.mCurShowPlmn, ServiceStateTracker.this.mCurPlmn, ServiceStateTracker.this.mCurShowSpn, ServiceStateTracker.this.mCurSpn);
                        boolean unused2 = ServiceStateTracker.this.mSpnUpdatePending = false;
                    }
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, "");
                    String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, "");
                    String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, "");
                    if (!TextUtils.isEmpty(oldNetworkSelection) || !TextUtils.isEmpty(oldNetworkSelectionName) || !TextUtils.isEmpty(oldNetworkSelectionShort)) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(Phone.NETWORK_SELECTION_KEY + subId, oldNetworkSelection);
                        editor.putString(Phone.NETWORK_SELECTION_NAME_KEY + subId, oldNetworkSelectionName);
                        editor.putString(Phone.NETWORK_SELECTION_SHORT_KEY + subId, oldNetworkSelectionShort);
                        editor.remove(Phone.NETWORK_SELECTION_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_NAME_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_SHORT_KEY);
                        editor.commit();
                    }
                    ServiceStateTracker.this.updateSpnDisplay();
                }
                if (ServiceStateTracker.this.mSubscriptionController.getSlotIndex(subId) == -1) {
                    ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(52));
                }
            }
        }
    }

    public ServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        this.mNitzState = TelephonyComponentFactory.getInstance().makeNitzStateMachine(phone);
        this.mPhone = phone;
        this.LOG_TAG += "[SUB" + this.mPhone.getPhoneId() + "]";
        this.mCi = ci;
        this.mRatRatcheter = new RatRatcheter(this.mPhone);
        this.mVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17957068);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 42, null);
        this.mCi.setOnSignalStrengthUpdate(this, 12, null);
        this.mCi.registerForCellInfoList(this, 44, null);
        this.mCi.registerForPhysicalChannelConfiguration(this, 55, null);
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mSubscriptionManager = SubscriptionManager.from(phone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mRestrictedState = new RestrictedState();
        this.mTransportManager = new TransportManager();
        for (Integer intValue : this.mTransportManager.getAvailableTransports()) {
            int transportType = intValue.intValue();
            this.mRegStateManagers.append(transportType, new NetworkRegistrationManager(transportType, phone));
            this.mRegStateManagers.get(transportType).registerForNetworkRegistrationStateChanged(this, 2, null);
        }
        this.mHandlerThread = new HandlerThread(LocaleTracker.class.getSimpleName());
        this.mHandlerThread.start();
        this.mLocaleTracker = TelephonyComponentFactory.getInstance().makeLocaleTracker(this.mPhone, this.mHandlerThread.getLooper());
        this.mCi.registerForImsNetworkStateChanged(this, 46, null);
        this.mCi.registerForRadioStateChanged(this, 1, null);
        this.mCi.setOnNITZTime(this, 11, null);
        this.mCr = phone.getContext().getContentResolver();
        int airplaneMode = Settings.Global.getInt(this.mCr, "airplane_mode_on", 0);
        this.mDesiredPowerState = Settings.Global.getInt(this.mCr, "enable_cellular_on_boot", 1) > 0 && airplaneMode <= 0;
        this.mRadioPowerLog.log("init : airplane mode = " + airplaneMode + " enableCellularOnBoot = " + enableCellularOnBoot);
        setSignalStrengthDefaultValues();
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(1, this, 51, null, false);
        this.mCi.getSignalStrength(obtainMessage(3));
        Context context = this.mPhone.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_RADIO_OFF);
        filter2.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter2.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter2.addAction("android.intent.action.HEADSET_PLUG");
        filter2.addAction("android.intent.action.PHONE_STATE");
        filter2.addAction("android.intent.action.AIRPLANE_MODE");
        context.registerReceiver(this.mIntentReceiver, filter2);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(ACTION_COMMFORCE);
        context.registerReceiver(this.mIntentReceiver, filter3, PERMISSION_COMM_FORCE, null);
        this.mHwCustGsmServiceStateTracker = (HwCustGsmServiceStateTracker) HwCustUtils.createObj(HwCustGsmServiceStateTracker.class, new Object[]{this.mPhone});
        if (this.mHwCustGsmServiceStateTracker != null && (this.mHwCustGsmServiceStateTracker.isDataOffForbidLTE() || this.mHwCustGsmServiceStateTracker.isDataOffbyRoamAndData())) {
            this.mHwCustGsmServiceStateTracker.initOnce(this.mPhone, this.mCi);
        }
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter4);
        this.mPhone.notifyOtaspChanged(0);
        this.mCi.setOnRestrictedStateChanged(this, 23, null);
        updatePhoneType();
        this.mCi.registerForOffOrNotAvailable(this, 56, null);
        this.mCSST = new CarrierServiceStateTracker(phone, this);
        registerForNetworkAttached(this.mCSST, 101, null);
        registerForNetworkDetached(this.mCSST, 102, null);
        registerForDataConnectionAttached(this.mCSST, 103, null);
        registerForDataConnectionDetached(this.mCSST, AbstractPhoneBase.EVENT_ECC_NUM, null);
        initDisplay();
    }

    private void initDisplay() {
        DisplayManager dm = (DisplayManager) this.mPhone.getContext().getSystemService("display");
        dm.registerDisplayListener(this.mDisplayListener, null);
        this.mDefaultDisplay = dm.getDisplay(0);
    }

    @VisibleForTesting
    public void updatePhoneType() {
        if (this.mSS != null && this.mSS.getVoiceRoaming()) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getDataRoaming()) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getVoiceRegState() == 0) {
            this.mNetworkDetachedRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getDataRegState() == 0) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        this.mSS = new ServiceState();
        this.mNewSS = new ServiceState();
        this.mLastCellInfoListTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
        this.mStartedGprsRegCheck = false;
        this.mReportedGprsNoReg = false;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = false;
        this.mNitzState.handleNetworkUnavailable();
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mCdmaSSM != null) {
                this.mCdmaSSM.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mPhone.unregisterForEriFileLoaded(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCellLoc = new GsmCellLocation();
            this.mNewCellLoc = new GsmCellLocation();
        } else {
            if (!IS_HISI_PLATFORM) {
                this.mCurPlmn = null;
            }
            this.mPhone.registerForSimRecordsLoaded(this, 16, null);
            this.mCellLoc = new CdmaCellLocation();
            this.mNewCellLoc = new CdmaCellLocation();
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0;
            this.mCi.registerForCdmaPrlChanged(this, 40, null);
            this.mPhone.registerForEriFileLoaded(this, 36, null);
            this.mCi.registerForCdmaOtaProvision(this, 37, null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(0));
        this.mCi.getSignalStrength(obtainMessage(3));
        sendMessage(obtainMessage(50));
        logPhoneTypeChange();
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    @VisibleForTesting
    public void requestShutdown() {
        if (!this.mDeviceShuttingDown) {
            this.mDeviceShuttingDown = true;
            this.mDesiredPowerState = false;
            setPowerStateToDesired();
        }
    }

    public void dispose() {
        this.mCi.unSetOnSignalStrengthUpdate(this);
        this.mUiccController.unregisterForIccChanged(this);
        this.mCi.unregisterForCellInfoList(this);
        this.mCi.unregisterForPhysicalChannelConfiguration(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mHandlerThread.quit();
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 1);
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.unregisterForGetAdDone(this);
        }
        unregisterForRuimEvents();
        HwTelephonyFactory.getHwNetworkManager().dispose(this);
        if (this.mHwCustGsmServiceStateTracker != null && (this.mHwCustGsmServiceStateTracker.isDataOffForbidLTE() || this.mHwCustGsmServiceStateTracker.isDataOffbyRoamAndData())) {
            this.mHwCustGsmServiceStateTracker.dispose(this.mPhone);
        }
        if (this.mCSST != null) {
            this.mCSST.dispose();
            this.mCSST = null;
        }
    }

    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public boolean getPowerStateFromCarrier() {
        return !this.mRadioDisabledByCarrier;
    }

    public long getLastCellInfoListTime() {
        return this.mLastCellInfoListTime;
    }

    public void setDesiredPowerState(boolean dps) {
        log("setDesiredPowerState, dps = " + dps);
        this.mDesiredPowerState = dps;
    }

    /* access modifiers changed from: protected */
    public boolean notifySignalStrength() {
        boolean notified = false;
        if (this.mSignalStrength.equals(this.mLastSignalStrength)) {
            return false;
        }
        try {
            this.mPhone.notifySignalStrength();
            notified = true;
            this.mLastSignalStrength = this.mSignalStrength;
            return true;
        } catch (NullPointerException ex) {
            loge("updateSignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return notified;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyDataRegStateRilRadioTechnologyChanged() {
        int rat = this.mSS.getRilDataRadioTechnology();
        int drs = this.mSS.getDataRegState();
        log("notifyDataRegStateRilRadioTechnologyChanged: drs=" + drs + " rat=" + rat);
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(rat));
        this.mDataRegStateOrRatChangedRegistrants.notifyResult(new Pair(Integer.valueOf(drs), Integer.valueOf(rat)));
    }

    /* access modifiers changed from: protected */
    public void useDataRegStateForDataOnlyDevices() {
        if (!this.mVoiceCapable) {
            log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            this.mNewSS.setVoiceRegState(this.mNewSS.getDataRegState());
        }
    }

    /* access modifiers changed from: protected */
    public void updatePhoneObject() {
        if (this.mPhone.getContext().getResources().getBoolean(17957050)) {
            if (!(this.mSS.getVoiceRegState() == 0 || this.mSS.getVoiceRegState() == 2)) {
                log("updatePhoneObject: Ignore update");
                return;
            }
            this.mPhone.updatePhoneObject(this.mSS.getRilVoiceRadioTechnology());
        }
    }

    public void registerForVoiceRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOnRegistrants.add(r);
        if (this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOn(Handler h) {
        this.mVoiceRoamingOnRegistrants.remove(h);
    }

    public void registerForVoiceRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOffRegistrants.add(r);
        if (!this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOff(Handler h) {
        this.mVoiceRoamingOffRegistrants.remove(h);
    }

    public void registerForDataRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOnRegistrants.add(r);
        if (this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOn(Handler h) {
        this.mDataRoamingOnRegistrants.remove(h);
    }

    public void registerForDataRoamingOff(Handler h, int what, Object obj, boolean notifyNow) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOffRegistrants.add(r);
        if (notifyNow && !this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOff(Handler h) {
        this.mDataRoamingOffRegistrants.remove(h);
    }

    public void reRegisterNetwork(Message onComplete) {
        if (!HwModemCapability.isCapabilitySupport(7) || !this.mPhone.isPhoneTypeGsm()) {
            log("modem not support rettach, reRegisterNetwork");
            this.mCi.getPreferredNetworkType(obtainMessage(19, onComplete));
            return;
        }
        log("modem support rettach, rettach");
        int i = 0;
        this.mCi.dataConnectionDetach(14 == this.mSS.getRilDataRadioTechnology() ? 1 : 0, null);
        CommandsInterface commandsInterface = this.mCi;
        if (14 == this.mSS.getRilDataRadioTechnology()) {
            i = 1;
        }
        commandsInterface.dataConnectionAttach(i, null);
    }

    public void setRadioPower(boolean power) {
        this.mDesiredPowerState = power;
        setPowerStateToDesired();
    }

    /* access modifiers changed from: protected */
    public void setPowerStateToDesired(boolean power, Message msg) {
        if (Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0) <= 0) {
            this.mDesiredPowerState = power;
        }
        log("mDesiredPowerState = " + this.mDesiredPowerState);
        getCaller();
        this.mCi.setRadioPower(this.mDesiredPowerState, msg);
    }

    public void setRadioPower(boolean power, Message msg) {
        setPowerStateToDesired(power, msg);
    }

    public void setRadioPowerFromCarrier(boolean enable) {
        this.mRadioDisabledByCarrier = !enable;
        setPowerStateToDesired();
    }

    public void enableSingleLocationUpdate() {
        if ((!HwTelephonyFactory.getHwNetworkManager().isCustScreenOff(this.mPhone) || HwTelephonyFactory.getHwNetworkManager().isAllowLocationUpdate(this, this.mPhone, Binder.getCallingPid())) && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantSingleLocationUpdate = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    public void enableLocationUpdates() {
        if (!HwTelephonyFactory.getHwNetworkManager().isCustScreenOff(this.mPhone) && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantContinuousLocationUpdates = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    /* access modifiers changed from: protected */
    public void disableSingleLocationUpdate() {
        this.mWantSingleLocationUpdate = false;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    public void disableLocationUpdates() {
        this.mWantContinuousLocationUpdates = false;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    private void processCellLocationInfo(CellLocation cellLocation, CellIdentity cellIdentity) {
        if (!this.mPhone.isPhoneTypeGsm()) {
            int baseStationId = -1;
            int baseStationLatitude = KeepaliveStatus.INVALID_HANDLE;
            int baseStationLongitude = KeepaliveStatus.INVALID_HANDLE;
            int systemId = 0;
            int networkId = 0;
            if (cellIdentity != null && cellIdentity.getType() == 2) {
                baseStationId = ((CellIdentityCdma) cellIdentity).getBasestationId();
                baseStationLatitude = ((CellIdentityCdma) cellIdentity).getLatitude();
                baseStationLongitude = ((CellIdentityCdma) cellIdentity).getLongitude();
                systemId = ((CellIdentityCdma) cellIdentity).getSystemId();
                networkId = ((CellIdentityCdma) cellIdentity).getNetworkId();
            }
            int systemId2 = systemId;
            int networkId2 = networkId;
            if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                baseStationLatitude = KeepaliveStatus.INVALID_HANDLE;
                baseStationLongitude = KeepaliveStatus.INVALID_HANDLE;
            }
            ((CdmaCellLocation) cellLocation).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId2, networkId2);
        } else if (!this.mPhone.isCTSimCard(this.mPhone.getPhoneId()) || !this.hasUpdateCellLocByPS) {
            int psc = -1;
            int cid = -1;
            int lac = -1;
            if (cellIdentity != null) {
                int type = cellIdentity.getType();
                if (type != 1) {
                    switch (type) {
                        case 3:
                            cid = ((CellIdentityLte) cellIdentity).getCi();
                            lac = ((CellIdentityLte) cellIdentity).getTac();
                            break;
                        case 4:
                            cid = ((CellIdentityWcdma) cellIdentity).getCid();
                            lac = ((CellIdentityWcdma) cellIdentity).getLac();
                            psc = ((CellIdentityWcdma) cellIdentity).getPsc();
                            break;
                        case 5:
                            cid = ((CellIdentityTdscdma) cellIdentity).getCid();
                            lac = ((CellIdentityTdscdma) cellIdentity).getLac();
                            break;
                    }
                } else {
                    cid = ((CellIdentityGsm) cellIdentity).getCid();
                    lac = ((CellIdentityGsm) cellIdentity).getLac();
                }
            }
            if (HwTelephonyFactory.getHwNetworkManager().isUpdateLacAndCid(this, this.mPhone, cid) || this.mHwCustGsmServiceStateTracker.isUpdateLacAndCidCust(this)) {
                ((GsmCellLocation) cellLocation).setLacAndCid(lac, cid);
                ((GsmCellLocation) cellLocation).setPsc(psc);
            }
        }
    }

    private int getLteEarfcn(CellIdentity cellIdentity) {
        if (cellIdentity == null || cellIdentity.getType() != 3) {
            return -1;
        }
        return ((CellIdentityLte) cellIdentity).getEarfcn();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v92, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v101, resolved type: android.os.AsyncResult} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void handleMessage(Message msg) {
        boolean enable;
        if (this.mHwCustGsmServiceStateTracker == null || !this.mHwCustGsmServiceStateTracker.handleMessage(msg)) {
            int i = msg.what;
            boolean z = false;
            if (i != 1002) {
                switch (i) {
                    case 1:
                        handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                        queueNextSignalStrengthPoll();
                        setPowerStateToDesired();
                        modemTriggeredPollState();
                        break;
                    case 2:
                        modemTriggeredPollState();
                        break;
                    case 3:
                        if (this.mCi.getRadioState().isOn()) {
                            onSignalStrengthResult((AsyncResult) msg.obj);
                            queueNextSignalStrengthPoll();
                            break;
                        } else {
                            return;
                        }
                    case 4:
                    case 5:
                    case 6:
                        handlePollStateResult(msg.what, (AsyncResult) msg.obj);
                        break;
                    default:
                        switch (i) {
                            case 10:
                                this.mCi.getSignalStrength(obtainMessage(3));
                                break;
                            case 11:
                                if (HwTelephonyFactory.getHwNetworkManager().needGsmUpdateNITZTime(this, this.mPhone)) {
                                    AsyncResult ar = (AsyncResult) msg.obj;
                                    long nitzReceiveTime = ((Long) ((Object[]) ar.result)[1]).longValue();
                                    this.mLastReceivedNITZReferenceTime = nitzReceiveTime;
                                    setTimeFromNITZString((String) ((Object[]) ar.result)[0], nitzReceiveTime);
                                    break;
                                } else {
                                    return;
                                }
                            case 12:
                                this.mDontPollSignalStrength = true;
                                onSignalStrengthResult((AsyncResult) msg.obj);
                                break;
                            default:
                                switch (i) {
                                    case 14:
                                        log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                                        AsyncResult ar2 = (AsyncResult) msg.obj;
                                        if (!this.mPhone.isPhoneTypeGsm()) {
                                            if (ar2.exception == null && ar2.result != null) {
                                                if (((int[]) ar2.result)[0] == 1) {
                                                    this.mPhone.setNetworkSelectionModeAutomatic(null);
                                                    break;
                                                }
                                            } else {
                                                log("Unable to getNetworkSelectionMode");
                                                break;
                                            }
                                        } else {
                                            handlePollStateResult(msg.what, ar2);
                                            break;
                                        }
                                        break;
                                    case 15:
                                        AsyncResult ar3 = (AsyncResult) msg.obj;
                                        if (ar3.exception == null && (ar3.result instanceof NetworkRegistrationState)) {
                                            processCellLocationInfo(this.mCellLoc, ((NetworkRegistrationState) ar3.result).getCellIdentity());
                                            this.mPhone.notifyLocationChanged();
                                        }
                                        disableSingleLocationUpdate();
                                        break;
                                    case 16:
                                        log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                                        this.mSimCardsLoaded = true;
                                        updatePhoneObject();
                                        updateOtaspState();
                                        if (this.mPhone.isPhoneTypeGsm()) {
                                            updateSpnDisplay();
                                            if (this.mHwCustGsmServiceStateTracker != null) {
                                                this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
                                                break;
                                            }
                                        }
                                        break;
                                    case 17:
                                        this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
                                        this.mPrevSubId = -1;
                                        this.mIsSimReady = true;
                                        this.mSimCardsLoaded = false;
                                        log("skip setPreferredNetworkType when EVENT_SIM_READY");
                                        this.mRecoverAutoSelectMode = HwTelephonyFactory.getHwNetworkManager().recoverAutoSelectMode(this, this.mPhone, this.mRecoverAutoSelectMode);
                                        pollState();
                                        queueNextSignalStrengthPoll();
                                        break;
                                    case 18:
                                        if (((AsyncResult) msg.obj).exception == null) {
                                            this.mRegStateManagers.get(1).getNetworkRegistrationState(1, obtainMessage(15, null));
                                            break;
                                        }
                                        break;
                                    case 19:
                                        AsyncResult ar4 = (AsyncResult) msg.obj;
                                        if (ar4.exception == null) {
                                            this.mPreferredNetworkType = ((int[]) ar4.result)[0];
                                        } else {
                                            this.mPreferredNetworkType = 7;
                                        }
                                        this.mCi.setPreferredNetworkType(7, obtainMessage(20, ar4.userObj));
                                        break;
                                    case 20:
                                        this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(21, ((AsyncResult) msg.obj).userObj));
                                        break;
                                    case 21:
                                        AsyncResult ar5 = (AsyncResult) msg.obj;
                                        if (ar5.userObj != null) {
                                            AsyncResult.forMessage((Message) ar5.userObj).exception = ar5.exception;
                                            ((Message) ar5.userObj).sendToTarget();
                                            break;
                                        }
                                        break;
                                    case 22:
                                        if (this.mPhone.isPhoneTypeGsm() && this.mSS != null && !isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                                            this.mPhone.getCellLocation();
                                            this.mReportedGprsNoReg = true;
                                        }
                                        this.mStartedGprsRegCheck = false;
                                        break;
                                    case 23:
                                        if (this.mPhone.isPhoneTypeGsm()) {
                                            log("EVENT_RESTRICTED_STATE_CHANGED");
                                            onRestrictedStateChanged((AsyncResult) msg.obj);
                                            break;
                                        }
                                        break;
                                    default:
                                        switch (i) {
                                            case 26:
                                                if (this.mPhone.getLteOnCdmaMode() == 1) {
                                                    log("Receive EVENT_RUIM_READY");
                                                    pollState();
                                                } else {
                                                    log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                                                    getSubscriptionInfoAndStartPollingThreads();
                                                }
                                                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                                                break;
                                            case 27:
                                                if (!this.mPhone.isPhoneTypeGsm()) {
                                                    log("EVENT_RUIM_RECORDS_LOADED: what=" + msg.what);
                                                    updatePhoneObject();
                                                    if (!this.mPhone.isPhoneTypeCdma()) {
                                                        RuimRecords ruim = (RuimRecords) this.mIccRecords;
                                                        if (ruim != null) {
                                                            if (ruim.isProvisioned()) {
                                                                this.mMdn = ruim.getMdn();
                                                                this.mMin = ruim.getMin();
                                                                parseSidNid(ruim.getSid(), ruim.getNid());
                                                                String prlVersion = ruim.getPrlVersion();
                                                                if (prlVersion != null) {
                                                                    String prlVersion2 = prlVersion.trim();
                                                                    if (!"".equals(prlVersion2) && !"65535".equals(prlVersion2)) {
                                                                        this.mPrlVersion = prlVersion2;
                                                                        SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                                                                    }
                                                                }
                                                                this.mIsMinInfoReady = true;
                                                            }
                                                            updateOtaspState();
                                                            notifyCdmaSubscriptionInfoReady();
                                                        }
                                                        pollState();
                                                        break;
                                                    } else {
                                                        updateSpnDisplay();
                                                        break;
                                                    }
                                                }
                                                break;
                                            default:
                                                switch (i) {
                                                    case 34:
                                                        if (!this.mPhone.isPhoneTypeGsm()) {
                                                            AsyncResult ar6 = (AsyncResult) msg.obj;
                                                            if (ar6.exception == null) {
                                                                String[] cdmaSubscription = (String[]) ar6.result;
                                                                if (cdmaSubscription != null && cdmaSubscription.length >= 5) {
                                                                    if (cdmaSubscription[0] != null) {
                                                                        this.mMdn = cdmaSubscription[0];
                                                                    }
                                                                    parseSidNid(cdmaSubscription[1], cdmaSubscription[2]);
                                                                    if (cdmaSubscription[3] != null) {
                                                                        this.mMin = cdmaSubscription[3];
                                                                    }
                                                                    if (cdmaSubscription[4] != null) {
                                                                        this.mPrlVersion = cdmaSubscription[4];
                                                                        SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                                                                    }
                                                                    if (this.mMdn != null) {
                                                                        log("GET_CDMA_SUBSCRIPTION: MDN = ****");
                                                                    }
                                                                    String strMlplMsplver = SystemProperties.get("ril.csim.mlpl_mspl_ver" + this.mPhone.getPhoneId());
                                                                    if (strMlplMsplver != null) {
                                                                        String[] arrayMlplMspl = strMlplMsplver.split(",");
                                                                        if (arrayMlplMspl.length >= 2) {
                                                                            this.mMlplVersion = arrayMlplMspl[0];
                                                                            this.mMsplVersion = arrayMlplMspl[1];
                                                                        }
                                                                    }
                                                                    log("GET_CDMA_SUBSCRIPTION: mMlplVersion=" + this.mMlplVersion + " mMsplVersion=" + this.mMsplVersion);
                                                                    this.mIsMinInfoReady = true;
                                                                    updateOtaspState();
                                                                    notifyCdmaSubscriptionInfoReady();
                                                                    if (!this.mIsSubscriptionFromRuim && this.mIccRecords != null) {
                                                                        log("GET_CDMA_SUBSCRIPTION set imsi in mIccRecords");
                                                                        this.mIccRecords.setImsi(getImsi());
                                                                        break;
                                                                    } else {
                                                                        log("GET_CDMA_SUBSCRIPTION either mIccRecords is null or NV type device - not setting Imsi in mIccRecords");
                                                                        break;
                                                                    }
                                                                } else {
                                                                    log("GET_CDMA_SUBSCRIPTION: error parsing cdmaSubscription params num=" + cdmaSubscription.length);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        break;
                                                    case 35:
                                                        updatePhoneObject();
                                                        this.mCi.getNetworkSelectionMode(obtainMessage(14));
                                                        getSubscriptionInfoAndStartPollingThreads();
                                                        break;
                                                    case 36:
                                                        log("ERI file has been loaded, repolling.");
                                                        pollState();
                                                        break;
                                                    case 37:
                                                        AsyncResult ar7 = (AsyncResult) msg.obj;
                                                        if (ar7.exception == null) {
                                                            int otaStatus = ((int[]) ar7.result)[0];
                                                            if (otaStatus == 8 || otaStatus == 10) {
                                                                log("EVENT_OTA_PROVISION_STATUS_CHANGE: Complete, Reload MDN");
                                                                this.mCi.getCDMASubscription(obtainMessage(34));
                                                                break;
                                                            }
                                                        }
                                                        break;
                                                    case 38:
                                                        synchronized (this) {
                                                            if (!this.mPendingRadioPowerOffAfterDataOff || msg.arg1 != this.mPendingRadioPowerOffAfterDataOffTag) {
                                                                log("EVENT_SET_RADIO_OFF is stale arg1=" + msg.arg1 + "!= tag=" + this.mPendingRadioPowerOffAfterDataOffTag);
                                                            } else {
                                                                log("EVENT_SET_RADIO_OFF, turn radio off now.");
                                                                hangupAndPowerOff();
                                                                this.mPendingRadioPowerOffAfterDataOffTag++;
                                                                this.mPendingRadioPowerOffAfterDataOff = false;
                                                            }
                                                            releaseWakeLock();
                                                        }
                                                        break;
                                                    case 39:
                                                        handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                                                        break;
                                                    case 40:
                                                        AsyncResult ar8 = (AsyncResult) msg.obj;
                                                        if (ar8.exception == null) {
                                                            this.mPrlVersion = Integer.toString(((int[]) ar8.result)[0]);
                                                            SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                                                            break;
                                                        }
                                                        break;
                                                    default:
                                                        switch (i) {
                                                            case 42:
                                                                onUpdateIccAvailability();
                                                                if (!(this.mUiccApplcation == null || this.mUiccApplcation.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY)) {
                                                                    this.mIsSimReady = false;
                                                                    updateSpnDisplay();
                                                                    break;
                                                                }
                                                            case 43:
                                                                AsyncResult ar9 = (AsyncResult) msg.obj;
                                                                if (ar9.userObj instanceof AsyncResult) {
                                                                    log("EVENT_GET_CELL_INFO_LIST userObj is AsyncResult!");
                                                                    ar9 = ar9.userObj;
                                                                }
                                                                if (ar9.userObj instanceof CellInfoResult) {
                                                                    CellInfoResult result = (CellInfoResult) ar9.userObj;
                                                                    synchronized (result.lockObj) {
                                                                        if (ar9.exception != null) {
                                                                            log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar9.exception);
                                                                            result.list = null;
                                                                        } else {
                                                                            result.list = (List) ar9.result;
                                                                        }
                                                                        this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                                                                        this.mLastCellInfoList = result.list;
                                                                        result.lockObj.notify();
                                                                    }
                                                                    break;
                                                                } else {
                                                                    log("EVENT_GET_CELL_INFO_LIST userObj:" + ar9.userObj);
                                                                    break;
                                                                }
                                                            case 44:
                                                                AsyncResult ar10 = (AsyncResult) msg.obj;
                                                                if (ar10.exception == null) {
                                                                    List<CellInfo> list = (List) ar10.result;
                                                                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                                                                    this.mLastCellInfoList = list;
                                                                    this.mPhone.notifyCellInfo(list);
                                                                    break;
                                                                } else {
                                                                    log("EVENT_UNSOL_CELL_INFO_LIST: error ignoring, e=" + ar10.exception);
                                                                    break;
                                                                }
                                                            case 45:
                                                                log("EVENT_CHANGE_IMS_STATE:");
                                                                setPowerStateToDesired();
                                                                break;
                                                            case 46:
                                                                this.mCi.getImsRegistrationState(obtainMessage(47));
                                                                break;
                                                            case 47:
                                                                AsyncResult ar11 = (AsyncResult) msg.obj;
                                                                if (ar11.exception == null) {
                                                                    if (((int[]) ar11.result)[0] == 1) {
                                                                        z = true;
                                                                    }
                                                                    this.mImsRegistered = z;
                                                                }
                                                                if (this.mPhone.isCTSimCard(this.mPhone.getPhoneId())) {
                                                                    pollState();
                                                                    break;
                                                                }
                                                                break;
                                                            case 48:
                                                                log("EVENT_IMS_CAPABILITY_CHANGED");
                                                                updateSpnDisplay();
                                                                if (!(this.mPhone == null || this.mPhone.getImsPhone() == null)) {
                                                                    boolean tempPreVowifiState = this.mPreVowifiState;
                                                                    this.mPreVowifiState = this.mPhone.getImsPhone().isWifiCallingEnabled();
                                                                    if (tempPreVowifiState && !this.mPhone.getImsPhone().isWifiCallingEnabled() && this.mPollingContext[0] == 0) {
                                                                        log("mPollingContext == 0");
                                                                        pollState();
                                                                        break;
                                                                    }
                                                                }
                                                            case 49:
                                                                ProxyController.getInstance().unregisterForAllDataDisconnected(SubscriptionManager.getDefaultDataSubscriptionId(), this);
                                                                synchronized (this) {
                                                                    if (this.mPendingRadioPowerOffAfterDataOff) {
                                                                        log("EVENT_ALL_DATA_DISCONNECTED, turn radio off now.");
                                                                        hangupAndPowerOff();
                                                                        this.mPendingRadioPowerOffAfterDataOff = false;
                                                                    } else {
                                                                        log("EVENT_ALL_DATA_DISCONNECTED is stale");
                                                                    }
                                                                }
                                                                break;
                                                            case 50:
                                                                break;
                                                            case 51:
                                                                AsyncResult ar12 = (AsyncResult) msg.obj;
                                                                if (ar12.exception == null) {
                                                                    log("EVENT_RADIO_POWER_FROM_CARRIER: " + enable);
                                                                    setRadioPowerFromCarrier(enable);
                                                                    break;
                                                                }
                                                                break;
                                                            case 52:
                                                                log("EVENT_SIM_NOT_INSERTED");
                                                                cancelAllNotifications();
                                                                this.mMdn = null;
                                                                this.mMin = null;
                                                                this.mIsMinInfoReady = false;
                                                                break;
                                                            case 53:
                                                                log("EVENT_IMS_SERVICE_STATE_CHANGED");
                                                                if (this.mSS.getState() != 0) {
                                                                    this.mPhone.notifyServiceStateChanged(this.mPhone.getServiceState());
                                                                    break;
                                                                }
                                                                break;
                                                            case 54:
                                                                log("EVENT_RADIO_POWER_OFF_DONE");
                                                                if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
                                                                    this.mCi.requestShutdown(null);
                                                                    break;
                                                                }
                                                            case 55:
                                                                AsyncResult ar13 = (AsyncResult) msg.obj;
                                                                if (ar13.exception == null) {
                                                                    List<PhysicalChannelConfig> list2 = (List) ar13.result;
                                                                    this.mPhone.notifyPhysicalChannelConfiguration(list2);
                                                                    this.mLastPhysicalChannelConfigList = list2;
                                                                    if (RatRatcheter.updateBandwidths(getBandwidthsFromConfigs(list2), this.mSS)) {
                                                                        this.mPhone.notifyServiceStateChanged(this.mSS);
                                                                        break;
                                                                    }
                                                                }
                                                                break;
                                                            case 56:
                                                                log("EVENT_RADIO_POWER_OFF");
                                                                this.mNewSS.setStateOff();
                                                                pollStateDone();
                                                                break;
                                                            default:
                                                                log("Unhandled message with number: " + msg.what);
                                                                break;
                                                        }
                                                        break;
                                                }
                                        }
                                }
                        }
                        if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == CommandsInterface.RadioState.RADIO_ON) {
                            handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                            queueNextSignalStrengthPoll();
                        }
                        setPowerStateToDesired();
                        modemTriggeredPollState();
                        break;
                }
            } else if (this.mPollingContext[0] == 0) {
                log("EVENT_GET_AD_DONE pollState ");
                pollState();
            } else {
                log("EVENT_GET_AD_DONE pollState working ,no need do again");
            }
        }
    }

    private int[] getBandwidthsFromConfigs(List<PhysicalChannelConfig> list) {
        return list.stream().map($$Lambda$ServiceStateTracker$WWHOcG5P4jgjzPPgLwmwN15OM.INSTANCE).mapToInt($$Lambda$ServiceStateTracker$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    public String getRplmn() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return HwTelephonyFactory.getHwNetworkManager().getGsmRplmn(this, this.mPhone);
        }
        return "";
    }

    public void setCurrent3GPsCsAllowed(boolean allowed) {
        log("setCurrent3GPsCsAllowed:" + allowed);
        this.isCurrent3GPsCsAllowed = allowed;
    }

    /* access modifiers changed from: protected */
    public boolean isSidsAllZeros() {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (i != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isHomeSid(int sid) {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (sid == i) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getMdnNumber() {
        return this.mMdn;
    }

    public String getCdmaMin() {
        return this.mMin;
    }

    public String getPrlVersion() {
        int subId = this.mPhone.getSubId();
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (5 == simCardState) {
            this.mPrlVersion = this.mCi.getHwPrlVersion();
        } else {
            this.mPrlVersion = ProxyController.MODEM_0;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getPrlVersion: prlVersion=" + this.mPrlVersion + ", subid=" + subId + ", simState=" + simCardState);
        return this.mPrlVersion;
    }

    public String getImsi() {
        String operatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (TextUtils.isEmpty(operatorNumeric) || getCdmaMin() == null) {
            return null;
        }
        return operatorNumeric + getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mIsMinInfoReady;
    }

    public int getOtasp() {
        int provisioningState = 3;
        if (this.mPhone.isPhoneTypeGsm()) {
            log("getOtasp: otasp not needed for GSM");
            return 3;
        } else if (this.mIsSubscriptionFromRuim && this.mMin == null) {
            return 3;
        } else {
            if (this.mMin == null || this.mMin.length() < 6) {
                log("getOtasp: bad mMin='" + this.mMin + "'");
                provisioningState = 1;
            } else if (this.mMin.equals(UNACTIVATED_MIN_VALUE) || this.mMin.substring(0, 6).equals(UNACTIVATED_MIN2_VALUE) || SystemProperties.getBoolean("test_cdma_setup", false)) {
                provisioningState = 2;
            }
            int provisioningState2 = provisioningState;
            log("getOtasp: state=" + provisioningState2);
            return provisioningState2;
        }
    }

    /* access modifiers changed from: protected */
    public void parseSidNid(String sidStr, String nidStr) {
        if (sidStr != null) {
            String[] sid = sidStr.split(",");
            this.mHomeSystemId = new int[sid.length];
            for (int i = 0; i < sid.length; i++) {
                try {
                    this.mHomeSystemId[i] = Integer.parseInt(sid[i]);
                } catch (NumberFormatException ex) {
                    loge("error parsing system id: " + ex);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: SID=" + sidStr);
        if (nidStr != null) {
            String[] nid = nidStr.split(",");
            this.mHomeNetworkId = new int[nid.length];
            for (int i2 = 0; i2 < nid.length; i2++) {
                try {
                    this.mHomeNetworkId[i2] = Integer.parseInt(nid[i2]);
                } catch (NumberFormatException ex2) {
                    loge("CDMA_SUBSCRIPTION: error parsing network id: " + ex2);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: NID=" + nidStr);
    }

    /* access modifiers changed from: protected */
    public void updateOtaspState() {
        int otaspMode = getOtasp();
        int oldOtaspMode = this.mCurrentOtaspMode;
        this.mCurrentOtaspMode = otaspMode;
        if (oldOtaspMode != this.mCurrentOtaspMode) {
            log("updateOtaspState: call notifyOtaspChanged old otaspMode=" + oldOtaspMode + " new otaspMode=" + this.mCurrentOtaspMode);
            this.mPhone.notifyOtaspChanged(this.mCurrentOtaspMode);
        }
    }

    /* access modifiers changed from: protected */
    public Phone getPhone() {
        return this.mPhone;
    }

    /* access modifiers changed from: protected */
    public void handlePollStateResult(int what, AsyncResult ar) {
        if (this.mHwCustGsmServiceStateTracker != null) {
            this.mHwCustGsmServiceStateTracker.custHandlePollStateResult(what, ar, this.mPollingContext);
        }
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                CommandException.Error err = null;
                if (ar.exception instanceof IllegalStateException) {
                    log("handlePollStateResult exception " + ar.exception);
                }
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    return;
                } else if (err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                    loge("RIL implementation has returned an error where it must succeed" + ar.exception);
                }
            } else {
                try {
                    handlePollStateResultMessage(what, ar);
                } catch (RuntimeException ex) {
                    loge("Exception while polling service state. Probably malformed RIL response." + ex);
                }
            }
            int[] iArr = this.mPollingContext;
            boolean isVoiceInService = false;
            iArr[0] = iArr[0] - 1;
            if (this.mPollingContext[0] == 0) {
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateRoamingState();
                    this.hasUpdateCellLocByPS = false;
                } else {
                    boolean namMatch = false;
                    if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getCdmaSystemId())) {
                        namMatch = true;
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        boolean isRoamingBetweenOperators = isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS);
                        if (isRoamingBetweenOperators != this.mNewSS.getVoiceRoaming()) {
                            log("isRoamingBetweenOperators=" + isRoamingBetweenOperators + ". Override CDMA voice roaming to " + isRoamingBetweenOperators);
                            this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators);
                        }
                    }
                    if (ServiceState.isCdma(this.mNewSS.getRilDataRadioTechnology())) {
                        if (this.mNewSS.getVoiceRegState() == 0) {
                            isVoiceInService = true;
                        }
                        if (isVoiceInService) {
                            boolean isVoiceRoaming = this.mNewSS.getVoiceRoaming();
                            if (this.mNewSS.getDataRoaming() != isVoiceRoaming) {
                                log("Data roaming != Voice roaming. Override data roaming to " + isVoiceRoaming);
                                this.mNewSS.setDataRoaming(isVoiceRoaming);
                            }
                        } else if (-1 != this.mRoamingIndicator) {
                            boolean isRoamIndForHomeSystem = isRoamIndForHomeSystem(Integer.toString(this.mRoamingIndicator));
                            if (this.mNewSS.getDataRoaming() == isRoamIndForHomeSystem) {
                                log("isRoamIndForHomeSystem=" + isRoamIndForHomeSystem + ", override data roaming to " + (!isRoamIndForHomeSystem));
                                this.mNewSS.setDataRoaming(isRoamIndForHomeSystem ^ true);
                            }
                        }
                    }
                    this.mNewSS.setCdmaDefaultRoamingIndicator(this.mDefaultRoamingIndicator);
                    this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                    boolean isPrlLoaded = true;
                    if (TextUtils.isEmpty(this.mPrlVersion)) {
                        isPrlLoaded = false;
                    }
                    if (!isPrlLoaded || this.mNewSS.getRilVoiceRadioTechnology() == 0) {
                        log("Turn off roaming indicator if !isPrlLoaded or voice RAT is unknown");
                        this.mNewSS.setCdmaRoamingIndicator(1);
                    } else if (!isSidsAllZeros()) {
                        if (!namMatch && !this.mIsInPrl) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                        } else if (!namMatch || this.mIsInPrl) {
                            if (!namMatch && this.mIsInPrl) {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            } else if (this.mRoamingIndicator <= 2) {
                                this.mNewSS.setCdmaRoamingIndicator(1);
                            } else {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            }
                        } else if (ServiceState.isLte(this.mNewSS.getRilVoiceRadioTechnology())) {
                            log("Turn off roaming indicator as voice is LTE");
                            this.mNewSS.setCdmaRoamingIndicator(1);
                        } else {
                            this.mNewSS.setCdmaRoamingIndicator(2);
                        }
                    }
                    int roamingIndicator = this.mNewSS.getCdmaRoamingIndicator();
                    this.mNewSS.setCdmaEriIconIndex(this.mPhone.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                    this.mNewSS.setCdmaEriIconMode(this.mPhone.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                    HwTelephonyFactory.getHwNetworkManager().updateCTRoaming(this, this.mPhone, this.mNewSS, this.mNewSS.getRoaming());
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator);
                }
                this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                pollStateDone();
                if (this.mHwCustGsmServiceStateTracker != null) {
                    this.mHwCustGsmServiceStateTracker.clearLteEmmCause(getPhone().getPhoneId(), this.mSS);
                }
            }
        }
    }

    private boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return cdmaRoaming && !isSameOperatorNameFromSimAndSS(s);
    }

    /* access modifiers changed from: package-private */
    public void handlePollStateResultMessage(int what, AsyncResult ar) {
        int i = what;
        AsyncResult asyncResult = ar;
        String str = null;
        if (i != 14) {
            switch (i) {
                case 4:
                    NetworkRegistrationState networkRegState = (NetworkRegistrationState) asyncResult.result;
                    VoiceSpecificRegistrationStates voiceSpecificStates = networkRegState.getVoiceSpecificStates();
                    int registrationState = networkRegState.getRegState();
                    int cssIndicator = voiceSpecificStates.cssSupported;
                    int newVoiceRat = HwTelephonyFactory.getHwNetworkManager().getCARilRadioType(this, this.mPhone, ServiceState.networkTypeToRilRadioTechnology(networkRegState.getAccessNetworkTechnology()));
                    this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
                    this.mNewSS.setCssIndicator((int) cssIndicator);
                    int newVoiceRat2 = HwTelephonyFactory.getHwNetworkManager().getNrConfigTechnology(this, this.mPhone, newVoiceRat, networkRegState.getNsaState());
                    this.mNewSS.setRilVoiceRadioTechnology(newVoiceRat2);
                    this.mNewSS.addNetworkRegistrationState(networkRegState);
                    setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState.getCellIdentity());
                    int reasonForDenial = networkRegState.getReasonForDenial();
                    this.mEmergencyOnly = networkRegState.isEmergencyEnabled();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mGsmRoaming = regCodeIsRoaming(registrationState);
                        this.mNewRejectCode = reasonForDenial;
                        HwTelephonyFactory.getHwNetworkManager().sendGsmRoamingIntentIfDenied(this, this.mPhone, registrationState, reasonForDenial);
                        this.mPhone.getContext().getResources().getBoolean(17957068);
                    } else {
                        int roamingIndicator = voiceSpecificStates.roamingIndicator;
                        int systemIsInPrl = voiceSpecificStates.systemIsInPrl;
                        int defaultRoamingIndicator = voiceSpecificStates.defaultRoamingIndicator;
                        this.mRegistrationState = registrationState;
                        this.mNewSS.setVoiceRoaming(regCodeIsRoaming(registrationState) && !isRoamIndForHomeSystem(Integer.toString(roamingIndicator)));
                        this.mRoamingIndicator = roamingIndicator;
                        this.mIsInPrl = systemIsInPrl != 0;
                        this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                        int systemId = 0;
                        int networkId = 0;
                        CellIdentity cellIdentity = networkRegState.getCellIdentity();
                        if (cellIdentity != null && cellIdentity.getType() == 2) {
                            systemId = ((CellIdentityCdma) cellIdentity).getSystemId();
                            networkId = ((CellIdentityCdma) cellIdentity).getNetworkId();
                        }
                        int networkId2 = networkId;
                        this.mNewSS.setCdmaSystemAndNetworkId(systemId, networkId2);
                        if (reasonForDenial == 0) {
                            this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                        } else if (reasonForDenial == 1) {
                            this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                        } else {
                            this.mRegistrationDeniedReason = "";
                        }
                        int i2 = networkId2;
                        if (this.mRegistrationState == 3) {
                            log("Registration denied, " + this.mRegistrationDeniedReason);
                        }
                    }
                    processCellLocationInfo(this.mNewCellLoc, networkRegState.getCellIdentity());
                    log("handlPollVoiceRegResultMessage: regState=" + registrationState + " radioTechnology=" + newVoiceRat2);
                    return;
                case 5:
                    NetworkRegistrationState networkRegState2 = (NetworkRegistrationState) asyncResult.result;
                    DataSpecificRegistrationStates dataSpecificStates = networkRegState2.getDataSpecificStates();
                    int registrationState2 = networkRegState2.getRegState();
                    int serviceState = regCodeToServiceState(registrationState2);
                    int newDataRat = HwTelephonyFactory.getHwNetworkManager().getNrConfigTechnology(this, this.mPhone, HwTelephonyFactory.getHwNetworkManager().updateHSPAStatus(this, this.mPhone, HwTelephonyFactory.getHwNetworkManager().updateCAStatus(this, this.mPhone, ServiceState.networkTypeToRilRadioTechnology(networkRegState2.getAccessNetworkTechnology()))), networkRegState2.getNsaState());
                    this.mNewSS.setDataRegState(serviceState);
                    this.mNewSS.setRilDataRadioTechnology(newDataRat);
                    this.mNewSS.addNetworkRegistrationState(networkRegState2);
                    if (serviceState == 1) {
                        this.mLastPhysicalChannelConfigList = null;
                    }
                    setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState2.getCellIdentity());
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mNewReasonDataDenied = networkRegState2.getReasonForDenial();
                        this.mNewMaxDataCalls = dataSpecificStates.maxDataCalls;
                        this.mDataRoaming = regCodeIsRoaming(registrationState2);
                        this.mNewSS.setDataRoamingFromRegistration(this.mDataRoaming);
                        log("handlPollStateResultMessage: GsmSST dataServiceState=" + serviceState + " regState=" + registrationState2 + " dataRadioTechnology=" + newDataRat);
                    } else if (this.mPhone.isPhoneTypeCdma()) {
                        boolean isDataRoaming = regCodeIsRoaming(registrationState2);
                        this.mNewSS.setDataRoaming(isDataRoaming);
                        this.mNewSS.setDataRoamingFromRegistration(isDataRoaming);
                        log("handlPollStateResultMessage: cdma dataServiceState=" + serviceState + " regState=" + registrationState2 + " dataRadioTechnology=" + newDataRat);
                    } else {
                        int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                        if ((oldDataRAT == 0 && newDataRat != 0) || ((ServiceState.isCdma(oldDataRAT) && ServiceState.isLte(newDataRat)) || (ServiceState.isLte(oldDataRAT) && ServiceState.isCdma(newDataRat)))) {
                            this.mCi.getSignalStrength(obtainMessage(3));
                        }
                        boolean isDataRoaming2 = regCodeIsRoaming(registrationState2);
                        this.mNewSS.setDataRoaming(isDataRoaming2);
                        this.mNewSS.setDataRoamingFromRegistration(isDataRoaming2);
                        log("handlPollStateResultMessage: CdmaLteSST dataServiceState=" + serviceState + " registrationState=" + registrationState2 + " dataRadioTechnology=" + newDataRat);
                    }
                    updateServiceStateLteEarfcnBoost(this.mNewSS, getLteEarfcn(networkRegState2.getCellIdentity()));
                    if (serviceState == 0 && this.mPhone.isCTSimCard(this.mPhone.getPhoneId())) {
                        processCtVolteCellLocationInfo(this.mNewCellLoc, networkRegState2.getCellIdentity());
                        return;
                    }
                    return;
                case 6:
                    if (this.mPhone.isPhoneTypeGsm()) {
                        String[] opNames = (String[]) asyncResult.result;
                        if (opNames != null && opNames.length >= 3) {
                            if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                                str = this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride();
                            }
                            String brandOverride = str;
                            if (brandOverride != null) {
                                log("EVENT_POLL_STATE_OPERATOR: use brandOverride=" + brandOverride);
                                this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                            } else {
                                this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                            }
                            upatePlmn(brandOverride, opNames[0], opNames[1], opNames[2]);
                            return;
                        }
                        return;
                    }
                    String[] opNames2 = (String[]) asyncResult.result;
                    if (opNames2 == null || opNames2.length < 3) {
                        log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                        return;
                    }
                    if (opNames2[2] == null || opNames2[2].length() < 5 || "00000".equals(opNames2[2])) {
                        opNames2[0] = null;
                        opNames2[1] = null;
                        opNames2[2] = null;
                    }
                    if (!this.mIsSubscriptionFromRuim) {
                        this.mNewSS.setOperatorName(opNames2[0], opNames2[1], opNames2[2]);
                        return;
                    }
                    if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                        str = this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride();
                    }
                    String brandOverride2 = str;
                    if (brandOverride2 != null) {
                        this.mNewSS.setOperatorName(brandOverride2, brandOverride2, opNames2[2]);
                        return;
                    } else {
                        this.mNewSS.setOperatorName(opNames2[0], opNames2[1], opNames2[2]);
                        return;
                    }
                default:
                    loge("handlePollStateResultMessage: Unexpected RIL response received: " + i);
                    return;
            }
        } else {
            int[] ints = (int[]) asyncResult.result;
            this.mNewSS.setIsManualSelection(ints[0] == 1);
            if (ints[0] == 1 && (this.mPhone.shouldForceAutoNetworkSelect() || this.mRecoverAutoSelectMode)) {
                this.mPhone.setNetworkSelectionModeAutomatic(null);
                log(" Forcing Automatic Network Selection, manual selection is not allowed");
                this.mRecoverAutoSelectMode = false;
            } else if (this.mRecoverAutoSelectMode) {
                this.mRecoverAutoSelectMode = false;
            }
        }
    }

    private static boolean isValidLteBandwidthKhz(int bandwidth) {
        if (bandwidth == 1400 || bandwidth == 3000 || bandwidth == 5000 || bandwidth == 10000 || bandwidth == 15000 || bandwidth == POLL_PERIOD_MILLIS) {
            return true;
        }
        return false;
    }

    private void setPhyCellInfoFromCellIdentity(ServiceState ss, CellIdentity cellIdentity) {
        if (cellIdentity == null) {
            log("Could not set ServiceState channel number. CellIdentity null");
            return;
        }
        ss.setChannelNumber(cellIdentity.getChannelNumber());
        if (cellIdentity instanceof CellIdentityLte) {
            CellIdentityLte cl = (CellIdentityLte) cellIdentity;
            int[] bandwidths = null;
            if (!ArrayUtils.isEmpty(this.mLastPhysicalChannelConfigList)) {
                bandwidths = getBandwidthsFromConfigs(this.mLastPhysicalChannelConfigList);
                int length = bandwidths.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    if (!isValidLteBandwidthKhz(bandwidths[i])) {
                        loge("Invalid LTE Bandwidth in RegistrationState, " + bw);
                        bandwidths = null;
                        break;
                    }
                    i++;
                }
            }
            if (bandwidths == null || bandwidths.length == 1) {
                int cbw = cl.getBandwidth();
                if (isValidLteBandwidthKhz(cbw)) {
                    bandwidths = new int[]{cbw};
                } else if (cbw != Integer.MAX_VALUE) {
                    loge("Invalid LTE Bandwidth in RegistrationState, " + cbw);
                }
            }
            if (bandwidths != null) {
                ss.setCellBandwidths(bandwidths);
            }
        }
    }

    private boolean isRoamIndForHomeSystem(String roamInd) {
        String[] homeRoamIndicators = Resources.getSystem().getStringArray(17235993);
        log("isRoamIndForHomeSystem: homeRoamIndicators=" + Arrays.toString(homeRoamIndicators));
        if (homeRoamIndicators != null) {
            for (String homeRoamInd : homeRoamIndicators) {
                if (homeRoamInd.equals(roamInd)) {
                    return true;
                }
            }
            log("isRoamIndForHomeSystem: No match found against list for roamInd=" + roamInd);
            return false;
        }
        log("isRoamIndForHomeSystem: No list found");
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateRoamingState() {
        boolean z = false;
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mGsmRoaming || this.mDataRoaming) {
                z = true;
            }
            boolean roaming = z;
            log("updateRoamingState: original roaming = " + roaming + " mGsmRoaming:" + this.mGsmRoaming + " mDataRoaming:" + this.mDataRoaming);
            if (HwTelephonyFactory.getHwNetworkManager().checkForRoamingForIndianOperators(this, this.mPhone, this.mNewSS)) {
                log("indian operator,skip");
            } else if (this.mGsmRoaming && !isOperatorConsideredRoaming(this.mNewSS) && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                roaming = false;
                log("updateRoamingState: set roaming = false");
            }
            CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    PersistableBundle b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (alwaysOnHomeNetwork(b)) {
                        log("updateRoamingState: carrier config override always on home network");
                        roaming = false;
                    } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = false;
                    } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = true;
                    }
                } catch (Exception e) {
                    loge("updateRoamingState: unable to access carrier config service");
                }
            } else {
                log("updateRoamingState: no carrier config service available");
            }
            boolean roaming2 = HwTelephonyFactory.getHwNetworkManager().getGsmRoamingState(this, this.mPhone, roaming);
            this.mNewSS.setVoiceRoaming(roaming2);
            this.mNewSS.setDataRoaming(roaming2);
            return;
        }
        CarrierConfigManager configLoader2 = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configLoader2 != null) {
            try {
                PersistableBundle b2 = configLoader2.getConfigForSubId(this.mPhone.getSubId());
                String systemId = Integer.toString(this.mNewSS.getCdmaSystemId());
                if (alwaysOnHomeNetwork(b2)) {
                    log("updateRoamingState: carrier config override always on home network");
                    setRoamingOff();
                } else {
                    if (!isNonRoamingInGsmNetwork(b2, this.mNewSS.getOperatorNumeric())) {
                        if (!isNonRoamingInCdmaNetwork(b2, systemId)) {
                            if (isRoamingInGsmNetwork(b2, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(b2, systemId)) {
                                log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                                setRoamingOn();
                            }
                        }
                    }
                    log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                    setRoamingOff();
                }
            } catch (Exception e2) {
                loge("updateRoamingState: unable to access carrier config service");
            }
        } else {
            log("updateRoamingState: no carrier config service available");
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
    }

    private void setRoamingOn() {
        this.mNewSS.setVoiceRoaming(true);
        this.mNewSS.setDataRoaming(true);
        this.mNewSS.setCdmaEriIconIndex(0);
        this.mNewSS.setCdmaEriIconMode(0);
    }

    private void setRoamingOff() {
        this.mNewSS.setVoiceRoaming(false);
        this.mNewSS.setDataRoaming(false);
        this.mNewSS.setCdmaEriIconIndex(1);
    }

    /* JADX WARNING: type inference failed for: r14v6, types: [boolean] */
    /* JADX WARNING: type inference failed for: r14v9 */
    /* JADX WARNING: type inference failed for: r14v10 */
    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    public void updateSpnDisplay() {
        /*
            r40 = this;
            r10 = r40
            r40.updateOperatorNameFromEri()
            r1 = 0
            r2 = 0
            com.android.internal.telephony.HwNetworkManager r0 = com.android.internal.telephony.HwTelephonyFactory.getHwNetworkManager()
            com.android.internal.telephony.GsmCdmaPhone r3 = r10.mPhone
            android.telephony.ServiceState r4 = r10.mSS
            int r11 = r0.getGsmCombinedRegState(r10, r3, r4)
            com.android.internal.telephony.GsmCdmaPhone r0 = r10.mPhone
            com.android.internal.telephony.Phone r0 = r0.getImsPhone()
            if (r0 == 0) goto L_0x0082
            com.android.internal.telephony.GsmCdmaPhone r0 = r10.mPhone
            com.android.internal.telephony.Phone r0 = r0.getImsPhone()
            boolean r0 = r0.isWifiCallingEnabled()
            if (r0 == 0) goto L_0x0082
            if (r11 != 0) goto L_0x0082
            com.android.internal.telephony.GsmCdmaPhone r0 = r10.mPhone
            android.content.Context r0 = r0.getContext()
            android.content.res.Resources r0 = r0.getResources()
            r3 = 17236092(0x107007c, float:2.4795931E-38)
            java.lang.String[] r3 = r0.getStringArray(r3)
            r4 = 0
            r5 = 0
            com.android.internal.telephony.GsmCdmaPhone r0 = r10.mPhone
            android.content.Context r0 = r0.getContext()
            java.lang.String r6 = "carrier_config"
            java.lang.Object r0 = r0.getSystemService(r6)
            r6 = r0
            android.telephony.CarrierConfigManager r6 = (android.telephony.CarrierConfigManager) r6
            if (r6 == 0) goto L_0x007e
            com.android.internal.telephony.GsmCdmaPhone r0 = r10.mPhone     // Catch:{ Exception -> 0x0069 }
            int r0 = r0.getSubId()     // Catch:{ Exception -> 0x0069 }
            android.os.PersistableBundle r0 = r6.getConfigForSubId(r0)     // Catch:{ Exception -> 0x0069 }
            if (r0 == 0) goto L_0x0068
            java.lang.String r7 = "wfc_spn_format_idx_int"
            int r7 = r0.getInt(r7)     // Catch:{ Exception -> 0x0069 }
            r4 = r7
            java.lang.String r7 = "wfc_data_spn_format_idx_int"
            int r7 = r0.getInt(r7)     // Catch:{ Exception -> 0x0069 }
            r0 = r7
            r5 = r0
        L_0x0068:
            goto L_0x007e
        L_0x0069:
            r0 = move-exception
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "updateSpnDisplay: carrier config error: "
            r7.append(r8)
            r7.append(r0)
            java.lang.String r7 = r7.toString()
            r10.loge(r7)
        L_0x007e:
            r1 = r3[r4]
            r2 = r3[r5]
        L_0x0082:
            r0 = r1
            r12 = r2
            com.android.internal.telephony.GsmCdmaPhone r1 = r10.mPhone
            boolean r1 = r1.isPhoneTypeGsm()
            r2 = 17040350(0x10403de, float:2.4247346E-38)
            r14 = 2
            r15 = 1
            r9 = 0
            if (r1 == 0) goto L_0x038a
            com.android.internal.telephony.uicc.IccRecords r8 = r10.mIccRecords
            r1 = 0
            r3 = 0
            if (r8 == 0) goto L_0x009f
            android.telephony.ServiceState r4 = r10.mSS
            int r4 = r8.getDisplayRule(r4)
            goto L_0x00a0
        L_0x009f:
            r4 = r9
        L_0x00a0:
            r16 = r4
            android.telephony.ServiceState r4 = r10.mSS
            java.lang.String r7 = r4.getOperatorNumeric()
            r4 = 0
            if (r11 == r15) goto L_0x00fa
            if (r11 != r14) goto L_0x00ae
            goto L_0x00fa
        L_0x00ae:
            if (r11 != 0) goto L_0x00cd
            r40.getOperator()
            com.android.internal.telephony.HwNetworkManager r2 = com.android.internal.telephony.HwTelephonyFactory.getHwNetworkManager()
            com.android.internal.telephony.GsmCdmaPhone r5 = r10.mPhone
            java.lang.String r1 = r2.getGsmPlmn(r10, r5)
            boolean r2 = android.text.TextUtils.isEmpty(r1)
            if (r2 != 0) goto L_0x00c9
            r2 = r16 & 2
            if (r2 != r14) goto L_0x00c9
            r2 = r15
            goto L_0x00ca
        L_0x00c9:
            r2 = r9
        L_0x00ca:
            r14 = r1
            r3 = r2
            goto L_0x00f7
        L_0x00cd:
            r3 = 1
            android.content.res.Resources r5 = android.content.res.Resources.getSystem()
            java.lang.CharSequence r2 = r5.getText(r2)
            java.lang.String r1 = r2.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "updateSpnDisplay: radio is off w/ showPlmn="
            r2.append(r5)
            r2.append(r3)
            java.lang.String r5 = " plmn="
            r2.append(r5)
            r2.append(r1)
            java.lang.String r2 = r2.toString()
            r10.log(r2)
        L_0x00f6:
            r14 = r1
        L_0x00f7:
            r6 = r4
            goto L_0x016d
        L_0x00fa:
            r3 = 1
            com.android.internal.telephony.GsmCdmaPhone r5 = r10.mPhone
            android.content.Context r5 = r5.getContext()
            android.content.res.Resources r5 = r5.getResources()
            r6 = 17956934(0x1120046, float:2.681616E-38)
            boolean r5 = r5.getBoolean(r6)
            if (r5 == 0) goto L_0x0114
            boolean r5 = r10.mIsSimReady
            if (r5 != 0) goto L_0x0114
            r5 = r15
            goto L_0x0115
        L_0x0114:
            r5 = r9
        L_0x0115:
            int r6 = DELAYED_ECC_TO_NOSERVICE_VALUE
            if (r6 <= 0) goto L_0x0121
            android.telephony.ServiceState r6 = r10.mSS
            boolean r6 = r6.isEmergencyOnly()
            r10.mEmergencyOnly = r6
        L_0x0121:
            boolean r6 = r10.mEmergencyOnly
            if (r6 == 0) goto L_0x0139
            if (r5 != 0) goto L_0x0139
            android.content.res.Resources r2 = android.content.res.Resources.getSystem()
            r6 = 17039987(0x1040273, float:2.4246328E-38)
            java.lang.CharSequence r2 = r2.getText(r6)
            java.lang.String r1 = r2.toString()
            r2 = 1
            r4 = r2
            goto L_0x0145
        L_0x0139:
            android.content.res.Resources r6 = android.content.res.Resources.getSystem()
            java.lang.CharSequence r2 = r6.getText(r2)
            java.lang.String r1 = r2.toString()
        L_0x0145:
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r2 = r10.mHwCustGsmServiceStateTracker
            if (r2 == 0) goto L_0x0153
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r2 = r10.mHwCustGsmServiceStateTracker
            android.telephony.ServiceState r6 = r10.mSS
            boolean r14 = r10.mEmergencyOnly
            java.lang.String r1 = r2.setEmergencyToNoService(r6, r1, r14)
        L_0x0153:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r6 = "updateSpnDisplay: radio is on but out of service, set plmn='"
            r2.append(r6)
            r2.append(r1)
            java.lang.String r6 = "'"
            r2.append(r6)
            java.lang.String r2 = r2.toString()
            r10.log(r2)
            goto L_0x00f6
        L_0x016d:
            if (r8 == 0) goto L_0x0174
            java.lang.String r1 = r8.getServiceProviderName()
            goto L_0x0176
        L_0x0174:
            java.lang.String r1 = ""
        L_0x0176:
            r2 = r1
            if (r11 != 0) goto L_0x0185
            boolean r4 = android.text.TextUtils.isEmpty(r1)
            if (r4 != 0) goto L_0x0185
            r4 = r16 & 1
            if (r4 != r15) goto L_0x0185
            r4 = r15
            goto L_0x0186
        L_0x0185:
            r4 = r9
        L_0x0186:
            boolean r5 = android.text.TextUtils.isEmpty(r1)
            if (r5 != 0) goto L_0x01ae
            boolean r5 = android.text.TextUtils.isEmpty(r0)
            if (r5 != 0) goto L_0x01ae
            boolean r5 = android.text.TextUtils.isEmpty(r12)
            if (r5 != 0) goto L_0x01ae
            java.lang.String r5 = r1.trim()
            java.lang.Object[] r13 = new java.lang.Object[r15]
            r13[r9] = r5
            java.lang.String r2 = java.lang.String.format(r12, r13)
            r4 = 1
            r3 = 0
        L_0x01a7:
            r20 = r1
            r5 = r2
            r19 = r3
            r13 = r4
            goto L_0x01d2
        L_0x01ae:
            boolean r5 = android.text.TextUtils.isEmpty(r14)
            if (r5 != 0) goto L_0x01be
            boolean r5 = android.text.TextUtils.isEmpty(r0)
            if (r5 != 0) goto L_0x01be
            r14.trim()
            goto L_0x01a7
        L_0x01be:
            android.telephony.ServiceState r5 = r10.mSS
            int r5 = r5.getVoiceRegState()
            r13 = 3
            if (r5 == r13) goto L_0x01cf
            if (r3 == 0) goto L_0x01a7
            boolean r5 = android.text.TextUtils.equals(r1, r14)
            if (r5 == 0) goto L_0x01a7
        L_0x01cf:
            r1 = 0
            r4 = 0
            goto L_0x01a7
        L_0x01d2:
            com.android.internal.telephony.HwNetworkManager r1 = com.android.internal.telephony.HwTelephonyFactory.getHwNetworkManager()
            com.android.internal.telephony.GsmCdmaPhone r3 = r10.mPhone
            r2 = r10
            r4 = r13
            r21 = r5
            r5 = r19
            r22 = r6
            r6 = r16
            r23 = r7
            r7 = r14
            r24 = r8
            r8 = r20
            com.android.internal.telephony.OnsDisplayParams r8 = r1.getGsmOnsDisplayParams(r2, r3, r4, r5, r6, r7, r8)
            boolean r1 = r8.mShowSpn
            boolean r2 = r8.mShowPlmn
            int r13 = r8.mRule
            java.lang.String r3 = r8.mPlmn
            java.lang.String r4 = r8.mSpn
            boolean r14 = r8.mShowWifi
            java.lang.String r7 = r8.mWifi
            r5 = -1
            com.android.internal.telephony.GsmCdmaPhone r6 = r10.mPhone
            int r6 = r6.getPhoneId()
            int[] r6 = android.telephony.SubscriptionManager.getSubId(r6)
            if (r6 == 0) goto L_0x020d
            int r15 = r6.length
            if (r15 <= 0) goto L_0x020d
            r5 = r6[r9]
        L_0x020d:
            r15 = r5
            r5 = 0
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r9 = r10.mHwCustGsmServiceStateTracker
            if (r9 == 0) goto L_0x0221
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r9 = r10.mHwCustGsmServiceStateTracker
            r25 = r1
            boolean r1 = r10.mSimCardsLoaded
            boolean r1 = r9.isStopUpdateName(r1)
            if (r1 == 0) goto L_0x0223
            r5 = 1
            goto L_0x0223
        L_0x0221:
            r25 = r1
        L_0x0223:
            r16 = r5
            boolean r1 = display_blank_ons
            if (r1 != 0) goto L_0x022b
            if (r16 == 0) goto L_0x023d
        L_0x022b:
            if (r11 != 0) goto L_0x023d
            java.lang.String r1 = "In service , display blank ons for tracfone"
            r10.log(r1)
            java.lang.String r1 = " "
            java.lang.String r3 = " "
            r2 = 1
            r4 = 0
            r9 = r2
            r5 = r4
            r4 = r3
            r3 = r1
            goto L_0x0240
        L_0x023d:
            r9 = r2
            r5 = r25
        L_0x0240:
            int r1 = r10.mSubId
            if (r1 != r15) goto L_0x0288
            r1 = r10
            r2 = r9
            r31 = r3
            r3 = r5
            r32 = r4
            r33 = r12
            r12 = r5
            r5 = r21
            r19 = r6
            r6 = r31
            r34 = r7
            r7 = r14
            r20 = r8
            r8 = r34
            r35 = r0
            r0 = r9
            r36 = r14
            r14 = 0
            r9 = r23
            boolean r1 = r1.isOperatorChanged(r2, r3, r4, r5, r6, r7, r8, r9)
            if (r1 != 0) goto L_0x029b
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r1 = r10.mHwCustGsmServiceStateTracker
            if (r1 == 0) goto L_0x0276
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r1 = r10.mHwCustGsmServiceStateTracker
            boolean r1 = r1.isInServiceState((int) r11)
            if (r1 == 0) goto L_0x0276
            goto L_0x029b
        L_0x0276:
            r38 = r11
            r37 = r13
            r11 = r21
            r17 = r22
            r8 = r31
            r9 = r32
            r14 = r34
            r13 = r36
            goto L_0x036f
        L_0x0288:
            r35 = r0
            r31 = r3
            r32 = r4
            r19 = r6
            r34 = r7
            r20 = r8
            r0 = r9
            r33 = r12
            r36 = r14
            r14 = 0
            r12 = r5
        L_0x029b:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "updateSpnDisplay: changed sending intent rule="
            r1.append(r2)
            r1.append(r13)
            java.lang.String r2 = " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' subId='%d'"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r2 = 6
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r0)
            r2[r14] = r3
            r8 = r31
            r3 = 1
            r2[r3] = r8
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r12)
            r4 = 2
            r2[r4] = r3
            r9 = r32
            r3 = 3
            r2[r3] = r9
            r3 = 4
            r7 = r21
            r2[r3] = r7
            r3 = 5
            java.lang.Integer r4 = java.lang.Integer.valueOf(r15)
            r2[r3] = r4
            java.lang.String r1 = java.lang.String.format(r1, r2)
            r10.log(r1)
            r40.updateOperatorProp()
            android.content.Intent r1 = new android.content.Intent
            java.lang.String r2 = "android.provider.Telephony.SPN_STRINGS_UPDATED"
            r1.<init>(r2)
            r6 = r1
            java.lang.String r1 = "showSpn"
            r6.putExtra(r1, r12)
            java.lang.String r1 = "spn"
            r6.putExtra(r1, r9)
            java.lang.String r1 = "spnData"
            r6.putExtra(r1, r7)
            java.lang.String r1 = "showPlmn"
            r6.putExtra(r1, r0)
            java.lang.String r1 = "plmn"
            r6.putExtra(r1, r8)
            java.lang.String r1 = "showWifi"
            r5 = r36
            r6.putExtra(r1, r5)
            java.lang.String r1 = "wifi"
            r4 = r34
            r6.putExtra(r1, r4)
            java.lang.String r1 = "showEmergencyOnly"
            r3 = r22
            r6.putExtra(r1, r3)
            com.android.internal.telephony.GsmCdmaPhone r1 = r10.mPhone
            int r1 = r1.getPhoneId()
            android.telephony.SubscriptionManager.putPhoneIdAndSubIdExtra(r6, r1)
            com.android.internal.telephony.GsmCdmaPhone r1 = r10.mPhone
            android.content.Context r1 = r1.getContext()
            android.os.UserHandle r2 = android.os.UserHandle.ALL
            r1.sendStickyBroadcastAsUser(r6, r2)
            android.telephony.ServiceState r1 = r10.mSS
            if (r1 == 0) goto L_0x034c
            if (r11 != 0) goto L_0x034c
            com.android.internal.telephony.SubscriptionController r1 = r10.mSubscriptionController
            com.android.internal.telephony.GsmCdmaPhone r2 = r10.mPhone
            int r26 = r2.getPhoneId()
            r25 = r1
            r27 = r0
            r28 = r8
            r29 = r12
            r30 = r9
            boolean r1 = r25.setPlmnSpn(r26, r27, r28, r29, r30)
            if (r1 != 0) goto L_0x034c
            r1 = 1
            r10.mSpnUpdatePending = r1
        L_0x034c:
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r1 = r10.mHwCustGsmServiceStateTracker
            if (r1 == 0) goto L_0x0355
            com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker r1 = r10.mHwCustGsmServiceStateTracker
            r1.setExtPlmnSent(r14)
        L_0x0355:
            com.android.internal.telephony.HwNetworkManager r1 = com.android.internal.telephony.HwTelephonyFactory.getHwNetworkManager()
            com.android.internal.telephony.GsmCdmaPhone r14 = r10.mPhone
            r2 = r10
            r17 = r3
            r3 = r14
            r14 = r4
            r4 = r12
            r37 = r13
            r13 = r5
            r5 = r9
            r18 = r6
            r6 = r0
            r38 = r11
            r11 = r7
            r7 = r8
            r1.sendGsmDualSimUpdateSpnIntent(r2, r3, r4, r5, r6, r7)
        L_0x036f:
            r10.mSubId = r15
            r10.mCurShowSpn = r12
            r10.mCurShowPlmn = r0
            r10.mCurSpn = r9
            r10.mCurDataSpn = r11
            r10.mCurPlmn = r8
            r10.mCurShowWifi = r13
            r10.mCurWifi = r14
            r1 = r23
            r10.mCurRegplmn = r1
            r16 = r35
            r18 = r38
            goto L_0x04d7
        L_0x038a:
            r35 = r0
            r14 = r9
            r38 = r11
            r33 = r12
            com.android.internal.telephony.HwNetworkManager r0 = com.android.internal.telephony.HwTelephonyFactory.getHwNetworkManager()
            com.android.internal.telephony.GsmCdmaPhone r1 = r10.mPhone
            com.android.internal.telephony.OnsDisplayParams r0 = r0.getCdmaOnsDisplayParams(r10, r1)
            java.lang.String r1 = r0.mPlmn
            boolean r9 = r0.mShowPlmn
            boolean r11 = r0.mShowWifi
            java.lang.String r12 = r0.mWifi
            r3 = -1
            com.android.internal.telephony.GsmCdmaPhone r4 = r10.mPhone
            int r4 = r4.getPhoneId()
            int[] r13 = android.telephony.SubscriptionManager.getSubId(r4)
            if (r13 == 0) goto L_0x03b5
            int r4 = r13.length
            if (r4 <= 0) goto L_0x03b5
            r3 = r13[r14]
        L_0x03b5:
            r15 = r3
            boolean r3 = android.text.TextUtils.isEmpty(r1)
            if (r3 != 0) goto L_0x03c2
            r8 = r35
            android.text.TextUtils.isEmpty(r8)
            goto L_0x03c4
        L_0x03c2:
            r8 = r35
        L_0x03c4:
            boolean r3 = display_blank_ons
            if (r3 == 0) goto L_0x03d9
            if (r1 != 0) goto L_0x03d2
            android.telephony.ServiceState r3 = r10.mSS
            int r3 = r3.getState()
            if (r3 != 0) goto L_0x03d9
        L_0x03d2:
            java.lang.String r3 = "In service , display blank ons for tracfone"
            r10.log(r3)
            java.lang.String r1 = " "
        L_0x03d9:
            r7 = r38
            r3 = 1
            if (r7 != r3) goto L_0x0403
            android.content.res.Resources r3 = android.content.res.Resources.getSystem()
            java.lang.CharSequence r2 = r3.getText(r2)
            java.lang.String r1 = r2.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "updateSpnDisplay: radio is on but out of svc, set plmn='"
            r2.append(r3)
            r2.append(r1)
            java.lang.String r3 = "'"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r10.log(r2)
        L_0x0403:
            r2 = r1
            int r1 = r10.mSubId
            if (r1 != r15) goto L_0x0425
            java.lang.String r1 = r10.mCurPlmn
            boolean r1 = android.text.TextUtils.equals(r2, r1)
            if (r1 == 0) goto L_0x0425
            boolean r1 = r10.mCurShowWifi
            if (r1 != r11) goto L_0x0425
            java.lang.String r1 = r10.mCurWifi
            boolean r1 = android.text.TextUtils.equals(r12, r1)
            if (r1 != 0) goto L_0x041d
            goto L_0x0425
        L_0x041d:
            r39 = r2
            r18 = r7
            r16 = r8
            goto L_0x04c2
        L_0x0425:
            java.lang.String r1 = "updateSpnDisplay: changed sending intent showPlmn='%b' plmn='%s' subId='%d'"
            r3 = 3
            java.lang.Object[] r3 = new java.lang.Object[r3]
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r9)
            r3[r14] = r4
            r4 = 1
            r3[r4] = r2
            java.lang.Integer r4 = java.lang.Integer.valueOf(r15)
            r5 = 2
            r3[r5] = r4
            java.lang.String r1 = java.lang.String.format(r1, r3)
            r10.log(r1)
            android.content.Intent r1 = new android.content.Intent
            java.lang.String r3 = "android.provider.Telephony.SPN_STRINGS_UPDATED"
            r1.<init>(r3)
            java.lang.String r3 = "showSpn"
            r1.putExtra(r3, r14)
            java.lang.String r3 = "spn"
            java.lang.String r4 = ""
            r1.putExtra(r3, r4)
            java.lang.String r3 = "showPlmn"
            r1.putExtra(r3, r9)
            java.lang.String r3 = "plmn"
            r1.putExtra(r3, r2)
            com.android.internal.telephony.GsmCdmaPhone r3 = r10.mPhone
            int r3 = r3.getPhoneId()
            android.telephony.SubscriptionManager.putPhoneIdAndSubIdExtra(r1, r3)
            java.lang.String r3 = "showWifi"
            r1.putExtra(r3, r11)
            java.lang.String r3 = "wifi"
            r1.putExtra(r3, r12)
            com.android.internal.telephony.GsmCdmaPhone r3 = r10.mPhone
            android.content.Context r3 = r3.getContext()
            android.os.UserHandle r4 = android.os.UserHandle.ALL
            r3.sendStickyBroadcastAsUser(r1, r4)
            android.telephony.ServiceState r3 = r10.mSS
            if (r3 == 0) goto L_0x04a8
            android.telephony.ServiceState r3 = r10.mSS
            int r3 = r3.getState()
            if (r3 != 0) goto L_0x04a8
            com.android.internal.telephony.SubscriptionController r3 = r10.mSubscriptionController
            com.android.internal.telephony.GsmCdmaPhone r4 = r10.mPhone
            int r4 = r4.getPhoneId()
            r16 = 0
            java.lang.String r17 = ""
            r5 = r9
            r6 = r2
            r18 = r7
            r7 = r16
            r16 = r8
            r8 = r17
            boolean r3 = r3.setPlmnSpn(r4, r5, r6, r7, r8)
            if (r3 != 0) goto L_0x04ac
            r3 = 1
            r10.mSpnUpdatePending = r3
            goto L_0x04ac
        L_0x04a8:
            r18 = r7
            r16 = r8
        L_0x04ac:
            com.android.internal.telephony.HwNetworkManager r3 = com.android.internal.telephony.HwTelephonyFactory.getHwNetworkManager()
            com.android.internal.telephony.GsmCdmaPhone r4 = r10.mPhone
            r5 = 0
            java.lang.String r6 = ""
            r8 = r1
            r1 = r3
            r7 = r2
            r2 = r10
            r3 = r4
            r4 = r5
            r5 = r6
            r6 = r9
            r39 = r7
            r1.sendCdmaDualSimUpdateSpnIntent(r2, r3, r4, r5, r6, r7)
        L_0x04c2:
            r40.updateOperatorProp()
            r10.mSubId = r15
            r10.mCurShowSpn = r14
            r10.mCurShowPlmn = r9
            java.lang.String r1 = ""
            r10.mCurSpn = r1
            r1 = r39
            r10.mCurPlmn = r1
            r10.mCurShowWifi = r11
            r10.mCurWifi = r12
        L_0x04d7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.updateSpnDisplay():void");
    }

    /* access modifiers changed from: protected */
    public void setPowerStateToDesired() {
        getCaller();
        String tmpLog = "mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch + ", mRadioDisabledByCarrier=" + this.mRadioDisabledByCarrier;
        log(tmpLog);
        this.mRadioPowerLog.log(tmpLog);
        if (ISDEMO) {
            this.mCi.setRadioPower(false, null);
        }
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            log("mAlarmSwitch == true");
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
        }
        if (this.mDesiredPowerState && !this.mRadioDisabledByCarrier && this.mCi.getRadioState() == CommandsInterface.RadioState.RADIO_OFF) {
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.setRadioPower(this.mCi, true);
            }
            this.mCi.setRadioPower(true, null);
        } else if ((!this.mDesiredPowerState || this.mRadioDisabledByCarrier) && this.mCi.getRadioState().isOn()) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else if (!this.mImsRegistrationOnOff || this.mAlarmSwitch) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else {
                log("mImsRegistrationOnOff == true");
                Context context = this.mPhone.getContext();
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_RADIO_OFF), 0);
                this.mAlarmSwitch = true;
                log("Alarm setting");
                ((AlarmManager) context.getSystemService("alarm")).set(2, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.setRadioPower(this.mCi, false);
            }
        } else if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
            this.mCi.requestShutdown(null);
        }
    }

    /* access modifiers changed from: protected */
    public void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    this.mUiccApplcation.unregisterForGetAdDone(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                        HwTelephonyFactory.getHwNetworkManager().unregisterForSimRecordsEvents(this, this.mPhone, this.mIccRecords);
                    }
                    this.mIccRecords = null;
                    this.mUiccApplcation = null;
                }
                if (newUiccApplication != null) {
                    log("New card found");
                    this.mUiccApplcation = newUiccApplication;
                    this.mIccRecords = this.mUiccApplcation.getIccRecords();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mUiccApplcation.registerForReady(this, 17, null);
                        this.mUiccApplcation.registerForGetAdDone(this, 1002, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 16, null);
                            HwTelephonyFactory.getHwNetworkManager().registerForSimRecordsEvents(this, this.mPhone, this.mIccRecords);
                        }
                    } else if (this.mIsSubscriptionFromRuim) {
                        registerForRuimEvents();
                    }
                }
            }
        }
    }

    private void logRoamingChange() {
        this.mRoamingLog.log(this.mSS.toString());
    }

    private void logAttachChange() {
        this.mAttachLog.log(this.mSS.toString());
    }

    private void logPhoneTypeChange() {
        this.mPhoneTypeLog.log(Integer.toString(this.mPhone.getPhoneType()));
    }

    private void logRatChange() {
        this.mRatLog.log(this.mSS.toString());
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        String str = this.LOG_TAG;
        Rlog.d(str, "[" + this.mPhone.getPhoneId() + "] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        String str = this.LOG_TAG;
        Rlog.e(str, "[" + this.mPhone.getPhoneId() + "] " + s);
    }

    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        boolean z = false;
        if (this.mSS.getCssIndicator() == 1) {
            return this.mSS.getRilDataRadioTechnology() == 14 && !MDOEM_WORK_MODE_IS_SRLTE;
        }
        if (!this.mPhone.isPhoneTypeGsm()) {
            return false;
        }
        if (SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true") && !this.isCurrent3GPsCsAllowed) {
            log("current not allow voice and data simultaneously by vp");
            return false;
        } else if (this.mSS.getRilDataRadioTechnology() >= 3 && this.mSS.getRilDataRadioTechnology() != 16) {
            return true;
        } else {
            if (this.mSS.getCssIndicator() == 1) {
                z = true;
            }
            return z;
        }
    }

    public void onImsServiceStateChanged() {
        sendMessage(obtainMessage(53));
    }

    public void setImsRegistrationState(boolean registered) {
        log("ImsRegistrationState - registered : " + registered);
        if (!this.mImsRegistrationOnOff || registered || !this.mAlarmSwitch) {
            this.mImsRegistrationOnOff = registered;
            return;
        }
        this.mImsRegistrationOnOff = registered;
        ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
        this.mAlarmSwitch = false;
        sendMessage(obtainMessage(45));
    }

    public void onImsCapabilityChanged() {
        sendMessage(obtainMessage(48));
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState() == CommandsInterface.RadioState.RADIO_ON;
    }

    public void pollState() {
        pollState(false);
    }

    private void modemTriggeredPollState() {
        pollState(true);
    }

    public void pollState(boolean modemTriggered) {
        this.mPollingContext = new int[1];
        this.mPollingContext[0] = 0;
        log("pollState: modemTriggered=" + modemTriggered + ", radioState is " + this.mCi.getRadioState());
        switch (this.mCi.getRadioState()) {
            case RADIO_UNAVAILABLE:
                this.mNewSS.setStateOutOfService();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mNitzState.handleNetworkUnavailable();
                pollStateDone();
                return;
            case RADIO_OFF:
                this.mNewSS.setStateOff();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mNitzState.handleNetworkUnavailable();
                if (this.mDeviceShuttingDown || (!modemTriggered && 18 != this.mSS.getRilDataRadioTechnology())) {
                    pollStateDone();
                    return;
                }
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getOperator(obtainMessage(6, this.mPollingContext));
        int[] iArr2 = this.mPollingContext;
        iArr2[0] = iArr2[0] + 1;
        this.mRegStateManagers.get(1).getNetworkRegistrationState(2, obtainMessage(5, this.mPollingContext));
        int[] iArr3 = this.mPollingContext;
        iArr3[0] = iArr3[0] + 1;
        this.mRegStateManagers.get(1).getNetworkRegistrationState(1, obtainMessage(4, this.mPollingContext));
        if (this.mPhone.isPhoneTypeGsm()) {
            int[] iArr4 = this.mPollingContext;
            iArr4[0] = iArr4[0] + 1;
            this.mCi.getNetworkSelectionMode(obtainMessage(14, this.mPollingContext));
            HwTelephonyFactory.getHwNetworkManager().getLocationInfo(this, this.mPhone);
        }
        if (this.mHwCustGsmServiceStateTracker != null) {
            this.mHwCustGsmServiceStateTracker.getLteFreqWithWlanCoex(this.mCi, this);
        }
    }

    private void updateOperatorProp() {
        if (this.mPhone != null && this.mSS != null) {
            this.mPhone.setSystemProperty("gsm.operator.alpha", this.mSS.getOperatorAlphaLong());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:133:0x0288, code lost:
        if (r0.mNewSS.getRilDataRadioTechnology() == 13) goto L_0x028d;
     */
    /* JADX WARNING: Removed duplicated region for block: B:284:0x07cb  */
    /* JADX WARNING: Removed duplicated region for block: B:291:0x07de  */
    /* JADX WARNING: Removed duplicated region for block: B:293:0x07ee  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x07f5  */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x0805  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x080c  */
    /* JADX WARNING: Removed duplicated region for block: B:302:0x0819  */
    private void pollStateDone() {
        boolean hasLostMultiApnSupport;
        boolean hasMultiApnSupport;
        boolean has4gHandoff;
        boolean hasLocationChanged;
        boolean has4gHandoff2;
        boolean hasLocationChanged2;
        boolean hasVoiceRoamingOff;
        boolean hasDataRoamingOn;
        boolean hasVoiceRoamingOn;
        boolean hasRilDataRadioTechnologyChanged;
        boolean hasDataDetached;
        boolean hasDeregistered;
        boolean hasDataRegStateChanged;
        boolean hasRilVoiceRadioTechnologyChanged;
        boolean hasRegistered;
        boolean needNotifyData;
        boolean z;
        int i;
        boolean has4gHandoff3;
        boolean hasMultiApnSupport2;
        if (!this.mPhone.isPhoneTypeGsm()) {
            updateRoamingState();
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        if (Build.IS_DEBUGGABLE && this.mPhone.mTelephonyTester != null) {
            this.mPhone.mTelephonyTester.overrideServiceState(this.mNewSS);
        }
        log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "] oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        boolean hasRegistered2 = this.mSS.getVoiceRegState() != 0 && this.mNewSS.getVoiceRegState() == 0;
        boolean hasDeregistered2 = this.mSS.getVoiceRegState() == 0 && this.mNewSS.getVoiceRegState() != 0;
        boolean hasDataAttached = this.mSS.getDataRegState() != 0 && this.mNewSS.getDataRegState() == 0;
        boolean hasDataDetached2 = this.mSS.getDataRegState() == 0 && this.mNewSS.getDataRegState() != 0;
        boolean hasDataRegStateChanged2 = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasVoiceRegStateChanged = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
        boolean hasOperatorNumericChanged = false;
        if (this.mNewSS.getOperatorNumeric() != null) {
            hasOperatorNumericChanged = !this.mNewSS.getOperatorNumeric().equals(this.mSS.getOperatorNumeric());
        }
        boolean hasLocationChanged3 = !this.mNewCellLoc.equals(this.mCellLoc);
        if (this.mHwCustGsmServiceStateTracker != null) {
            this.mHwCustGsmServiceStateTracker.updateLTEBandWidth(this.mNewSS);
        }
        boolean updateCaByCell = true;
        if (this.mHwCustGsmServiceStateTracker != null) {
            updateCaByCell = this.mHwCustGsmServiceStateTracker.isUpdateCAByCell(this.mNewSS);
        }
        boolean isCtVolte = this.mPhone.isCTSimCard(this.mPhone.getPhoneId()) && this.mImsRegistered;
        boolean isDataInService = this.mNewSS.getDataRegState() == 0;
        if (isDataInService && updateCaByCell && !isCtVolte) {
            this.mRatRatcheter.ratchet(this.mSS, this.mNewSS, hasLocationChanged3);
        }
        boolean hasRilVoiceRadioTechnologyChanged2 = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasRilDataRadioTechnologyChanged2 = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = !this.mNewSS.equals(this.mSS);
        boolean hasVoiceRoamingOn2 = !this.mSS.getVoiceRoaming() && this.mNewSS.getVoiceRoaming();
        boolean z2 = updateCaByCell;
        boolean hasVoiceRoamingOff2 = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean z3 = isCtVolte;
        boolean hasDataRoamingOn2 = !this.mSS.getDataRoaming() && this.mNewSS.getDataRoaming();
        boolean z4 = isDataInService;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        boolean hasVoiceRegStateChanged2 = hasVoiceRegStateChanged;
        boolean hasOperatorNumericChanged2 = hasOperatorNumericChanged;
        boolean hasRejectCauseChanged = this.mRejectCode != this.mNewRejectCode;
        boolean hasCssIndicatorChanged = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        boolean hasLacChanged = false;
        boolean needNotifyData2 = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        if (this.mPhone.isPhoneTypeGsm()) {
            hasLacChanged = ((GsmCellLocation) this.mNewCellLoc).isNotLacEquals((GsmCellLocation) this.mCellLoc);
        }
        boolean hasLacChanged2 = hasLacChanged;
        if (this.mPhone.isPhoneTypeCdmaLte()) {
            boolean has4gHandoff4 = this.mNewSS.getDataRegState() == 0 && ((ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mNewSS.getRilDataRadioTechnology() == 13) || (this.mSS.getRilDataRadioTechnology() == 13 && ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())));
            if (!ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())) {
                has4gHandoff3 = has4gHandoff4;
            } else {
                has4gHandoff3 = has4gHandoff4;
            }
            if (!ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mSS.getRilDataRadioTechnology() != 13) {
                hasMultiApnSupport2 = true;
                hasMultiApnSupport = hasMultiApnSupport2;
                hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
                has4gHandoff = has4gHandoff3;
            }
            hasMultiApnSupport2 = false;
            hasMultiApnSupport = hasMultiApnSupport2;
            hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
            has4gHandoff = has4gHandoff3;
        } else {
            hasMultiApnSupport = false;
            hasLostMultiApnSupport = false;
            has4gHandoff = false;
        }
        boolean hasRilRadioTechnologyChanged = hasRilVoiceRadioTechnologyChanged2 || hasRilDataRadioTechnologyChanged2;
        boolean hasMultiApnSupport3 = hasMultiApnSupport;
        if (this.mHwCustGsmServiceStateTracker != null) {
            has4gHandoff2 = has4gHandoff;
            hasLocationChanged = hasLocationChanged3;
            hasLocationChanged2 = hasRilRadioTechnologyChanged;
            this.mHwCustGsmServiceStateTracker.tryClearRejFlag(this.mNewSS, hasLocationChanged2, this);
        } else {
            has4gHandoff2 = has4gHandoff;
            hasLocationChanged = hasLocationChanged3;
            hasLocationChanged2 = hasRilRadioTechnologyChanged;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("pollStateDone: hasRegistered=");
        sb.append(hasRegistered2);
        sb.append(" hasDeregistered=");
        sb.append(hasDeregistered2);
        sb.append(" hasDataAttached=");
        sb.append(hasDataAttached);
        sb.append(" hasDataDetached=");
        sb.append(hasDataDetached2);
        sb.append(" hasDataRegStateChanged=");
        sb.append(hasDataRegStateChanged2);
        sb.append(" hasRilVoiceRadioTechnologyChanged= ");
        sb.append(hasRilVoiceRadioTechnologyChanged2);
        sb.append(" hasRilDataRadioTechnologyChanged=");
        sb.append(hasRilDataRadioTechnologyChanged2);
        sb.append(" hasChanged=");
        sb.append(hasChanged);
        sb.append(" hasVoiceRoamingOn=");
        sb.append(hasVoiceRoamingOn2);
        sb.append(" hasVoiceRoamingOff=");
        sb.append(hasVoiceRoamingOff2);
        sb.append(" hasDataRoamingOn=");
        sb.append(hasDataRoamingOn2);
        sb.append(" hasDataRoamingOff=");
        sb.append(hasDataRoamingOff);
        sb.append(" hasLocationChanged=");
        boolean hasLocationChanged4 = hasLocationChanged;
        sb.append(hasLocationChanged4);
        boolean z5 = hasLocationChanged2;
        sb.append(" has4gHandoff = ");
        boolean has4gHandoff5 = has4gHandoff2;
        sb.append(has4gHandoff5);
        boolean hasLocationChanged5 = hasLocationChanged4;
        sb.append(" hasMultiApnSupport=");
        boolean hasMultiApnSupport4 = hasMultiApnSupport3;
        sb.append(hasMultiApnSupport4);
        boolean z6 = hasMultiApnSupport4;
        sb.append(" hasLostMultiApnSupport=");
        boolean hasLostMultiApnSupport2 = hasLostMultiApnSupport;
        sb.append(hasLostMultiApnSupport2);
        boolean z7 = hasLostMultiApnSupport2;
        sb.append(" hasCssIndicatorChanged=");
        boolean hasCssIndicatorChanged2 = hasCssIndicatorChanged;
        sb.append(hasCssIndicatorChanged2);
        boolean hasDataRoamingOff2 = hasDataRoamingOff;
        sb.append(" hasOperatorNumericChanged");
        boolean hasOperatorNumericChanged3 = hasOperatorNumericChanged2;
        sb.append(hasOperatorNumericChanged3);
        log(sb.toString());
        if (hasVoiceRegStateChanged2 || hasDataRegStateChanged2) {
            if (this.mPhone.isPhoneTypeGsm()) {
                i = EventLogTags.GSM_SERVICE_STATE_CHANGE;
            } else {
                i = EventLogTags.CDMA_SERVICE_STATE_CHANGE;
            }
            hasDataRoamingOn = hasDataRoamingOn2;
            hasVoiceRoamingOff = hasVoiceRoamingOff2;
            EventLog.writeEvent(i, new Object[]{Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState())});
        } else {
            hasVoiceRoamingOff = hasVoiceRoamingOff2;
            hasDataRoamingOn = hasDataRoamingOn2;
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (hasRilVoiceRadioTechnologyChanged2) {
                CellLocation cellLocation = this.mNewCellLoc;
                log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + -1);
            }
            if (hasCssIndicatorChanged2) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_CSS_INDICATOR_CHANGED);
            }
            if (hasChanged && hasRegistered2) {
                this.mPhone.mDcTracker.setMPDNByNetWork(this.mNewSS.getOperatorNumeric());
            }
            this.mReasonDataDenied = this.mNewReasonDataDenied;
            this.mMaxDataCalls = this.mNewMaxDataCalls;
            this.mRejectCode = this.mNewRejectCode;
        }
        if (hasRegistered2 || hasDataAttached) {
            log("service state hasRegistered , poll signal strength at once");
            sendMessage(obtainMessage(10));
        }
        ServiceState oldMergedSS = this.mPhone.getServiceState();
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean z8 = hasCssIndicatorChanged2;
            hasVoiceRoamingOn = hasVoiceRoamingOn2;
            if (HwTelephonyFactory.getHwNetworkManager().proccessGsmDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                return;
            }
        } else {
            hasVoiceRoamingOn = hasVoiceRoamingOn2;
            if (this.mPhone.isPhoneTypeCdmaLte() && HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                return;
            }
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        if (hasRilVoiceRadioTechnologyChanged2) {
            updatePhoneObject();
        }
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (hasRilDataRadioTechnologyChanged2) {
            ServiceState serviceState = tss;
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            CellLocation cellLocation2 = tcl;
            StatsLog.write(76, ServiceState.rilRadioTechnologyToNetworkType(this.mSS.getRilDataRadioTechnology()), this.mPhone.getPhoneId());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        } else {
            CellLocation cellLocation3 = tcl;
        }
        if (hasRegistered2 || hasOperatorNumericChanged3) {
            this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
            this.mNetworkAttachedRegistrants.notifyRegistrants();
            if (CLEAR_NITZ_WHEN_REG) {
                boolean z9 = hasOperatorNumericChanged3;
                hasRilDataRadioTechnologyChanged = hasRilDataRadioTechnologyChanged2;
                if (SystemClock.elapsedRealtime() - this.mLastReceivedNITZReferenceTime > 5000) {
                    this.mNitzState.handleNetworkAvailable();
                }
            } else {
                hasRilDataRadioTechnologyChanged = hasRilDataRadioTechnologyChanged2;
            }
        } else {
            boolean z10 = hasOperatorNumericChanged3;
            hasRilDataRadioTechnologyChanged = hasRilDataRadioTechnologyChanged2;
        }
        if (hasDeregistered2) {
            if (this.mPhone.isPhoneTypeCdma() && SystemProperties.getBoolean("ro.config_hw_doubletime", false)) {
                String mccmnc = this.mSS.getOperatorNumeric();
                if (mccmnc != null) {
                    Settings.System.putString(this.mCr, "last_registed_mcc", mccmnc.substring(0, 3));
                }
            }
            this.mNetworkDetachedRegistrants.notifyRegistrants();
            this.mNitzState.handleNetworkUnavailable();
        }
        if (hasRejectCauseChanged) {
            setNotification(2001);
        }
        if (hasChanged) {
            updateSpnDisplay();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
            if (!this.mPhone.isPhoneTypeCdma()) {
                updateOperatorProp();
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                judgeToLaunchCsgPeriodicSearchTimer();
            }
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String prevCountryIsoCode = tm.getNetworkCountryIso(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (!this.mPhone.isPhoneTypeGsm() && isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getCdmaSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                log("operatorNumeric " + operatorNumeric + " is invalid");
                this.mLocaleTracker.updateOperatorNumericAsync("");
                this.mNitzState.handleNetworkUnavailable();
                hasRegistered = hasRegistered2;
                hasRilVoiceRadioTechnologyChanged = hasRilVoiceRadioTechnologyChanged2;
                boolean z11 = hasChanged;
                hasDeregistered = hasDeregistered2;
                hasDataDetached = hasDataDetached2;
                hasDataRegStateChanged = hasDataRegStateChanged2;
            } else if (this.mSS.getRilDataRadioTechnology() != 18) {
                if (!this.mPhone.isPhoneTypeGsm()) {
                    setOperatorIdd(operatorNumeric);
                }
                this.mLocaleTracker.updateOperatorNumericSync(operatorNumeric);
                String countryIsoCode = this.mLocaleTracker.getCurrentCountry();
                boolean iccCardExists = iccCardExists();
                boolean z12 = hasChanged;
                boolean networkIsoChanged = networkCountryIsoChanged(countryIsoCode, prevCountryIsoCode);
                boolean countryChanged = iccCardExists && networkIsoChanged;
                hasRegistered = hasRegistered2;
                hasRilVoiceRadioTechnologyChanged = hasRilVoiceRadioTechnologyChanged2;
                long ctm = System.currentTimeMillis();
                hasDataRegStateChanged = hasDataRegStateChanged2;
                StringBuilder sb2 = new StringBuilder();
                hasDeregistered = hasDeregistered2;
                sb2.append("Before handleNetworkCountryCodeKnown: countryChanged=");
                boolean countryChanged2 = countryChanged;
                sb2.append(countryChanged2);
                hasDataDetached = hasDataDetached2;
                sb2.append(" iccCardExist=");
                sb2.append(iccCardExists);
                sb2.append(" countryIsoChanged=");
                sb2.append(networkIsoChanged);
                sb2.append(" operatorNumeric=");
                sb2.append(operatorNumeric);
                sb2.append(" prevOperatorNumeric=");
                sb2.append(prevOperatorNumeric);
                sb2.append(" countryIsoCode=");
                sb2.append(countryIsoCode);
                sb2.append(" prevCountryIsoCode=");
                sb2.append(prevCountryIsoCode);
                sb2.append(" ltod=");
                sb2.append(TimeUtils.logTimeOfDay(ctm));
                log(sb2.toString());
                this.mNitzState.handleNetworkCountryCodeSet(countryChanged2);
            } else {
                hasRegistered = hasRegistered2;
                hasRilVoiceRadioTechnologyChanged = hasRilVoiceRadioTechnologyChanged2;
                boolean z13 = hasChanged;
                hasDeregistered = hasDeregistered2;
                hasDataDetached = hasDataDetached2;
                hasDataRegStateChanged = hasDataRegStateChanged2;
            }
            int phoneId = this.mPhone.getPhoneId();
            if (this.mPhone.isPhoneTypeGsm()) {
                z = this.mSS.getVoiceRoaming();
            } else {
                z = this.mSS.getVoiceRoaming() || this.mSS.getDataRoaming();
            }
            tm.setNetworkRoamingForPhone(phoneId, z);
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.setLTEUsageForRomaing(this.mSS.getVoiceRoaming());
            }
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            if (!oldMergedSS.equals(this.mPhone.getServiceState())) {
                this.mPhone.notifyServiceStateChanged(this.mPhone.getServiceState());
            }
            this.mPhone.getContext().getContentResolver().insert(Telephony.ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), Telephony.ServiceStateTable.getContentValuesForServiceState(this.mSS));
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
        } else {
            hasRegistered = hasRegistered2;
            hasRilVoiceRadioTechnologyChanged = hasRilVoiceRadioTechnologyChanged2;
            boolean z14 = hasChanged;
            hasDeregistered = hasDeregistered2;
            hasDataDetached = hasDataDetached2;
            hasDataRegStateChanged = hasDataRegStateChanged2;
        }
        if (hasDataAttached || has4gHandoff5 || hasDataDetached || hasRegistered || hasDeregistered) {
            logAttachChange();
        }
        if (hasDataAttached || has4gHandoff5) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasDataDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasRilDataRadioTechnologyChanged || hasRilVoiceRadioTechnologyChanged) {
            logRatChange();
        }
        if (hasDataRegStateChanged || hasRilDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                needNotifyData = true;
                if (needNotifyData) {
                    this.mPhone.notifyDataConnection(null);
                }
                if (hasVoiceRoamingOn || hasVoiceRoamingOff || hasDataRoamingOn || hasDataRoamingOff2) {
                    logRoamingChange();
                }
                if (hasVoiceRoamingOn) {
                    HwTelephonyFactory.getHwNetworkManager().sendTimeZoneSelectionNotification(this, this.mPhone);
                    this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                }
                if (hasVoiceRoamingOff) {
                    this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                }
                if (hasDataRoamingOn) {
                    HwTelephonyFactory.getHwNetworkManager().sendTimeZoneSelectionNotification(this, this.mPhone);
                    this.mDataRoamingOnRegistrants.notifyRegistrants();
                }
                if (hasDataRoamingOff2) {
                    this.mDataRoamingOffRegistrants.notifyRegistrants();
                }
                if (hasLocationChanged5) {
                    this.mPhone.notifyLocationChanged();
                }
                if (this.mPhone.isPhoneTypeGsm()) {
                    if (hasLacChanged2) {
                        Rlog.i(this.LOG_TAG, "LAC changed, update operator name display");
                        updateSpnDisplay();
                    }
                    if (isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                        this.mReportedGprsNoReg = false;
                    } else if (!this.mStartedGprsRegCheck && !this.mReportedGprsNoReg) {
                        this.mStartedGprsRegCheck = true;
                        sendMessageDelayed(obtainMessage(22), (long) Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DEFAULT_GPRS_CHECK_PERIOD_MILLIS));
                    }
                }
            }
        }
        needNotifyData = needNotifyData2;
        if (needNotifyData) {
        }
        logRoamingChange();
        if (hasVoiceRoamingOn) {
        }
        if (hasVoiceRoamingOff) {
        }
        if (hasDataRoamingOn) {
        }
        if (hasDataRoamingOff2) {
        }
        if (hasLocationChanged5) {
        }
        if (this.mPhone.isPhoneTypeGsm()) {
        }
    }

    private void updateOperatorNameFromEri() {
        String eriText;
        if (this.mPhone.isPhoneTypeCdma()) {
            if (this.mCi.getRadioState().isOn() && !this.mIsSubscriptionFromRuim) {
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else {
                    eriText = this.mPhone.getContext().getText(17041035).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
        } else if (this.mPhone.isPhoneTypeCdmaLte()) {
            if (!((this.mUiccController.getUiccCard(getPhoneId()) == null || this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) ? false : true) && this.mCi.getRadioState().isOn() && this.mPhone.isEriFileLoaded() && ((!ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) || this.mPhone.getContext().getResources().getBoolean(17956867)) && !this.mIsSubscriptionFromRuim)) {
                String eriText2 = this.mSS.getOperatorAlpha();
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText2 = this.mPhone.getCdmaEriText();
                } else if (this.mSS.getVoiceRegState() == 3) {
                    eriText2 = this.mIccRecords != null ? this.mIccRecords.getServiceProviderName() : null;
                    if (TextUtils.isEmpty(eriText2)) {
                        eriText2 = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText2 = this.mPhone.getContext().getText(17041035).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText2);
            }
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY && this.mIccRecords != null) {
                if ((this.mSS.getVoiceRegState() == 0 || this.mSS.getDataRegState() == 0) && !ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology())) {
                    boolean showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                    int iconIndex = this.mSS.getCdmaEriIconIndex();
                    if (showSpn && iconIndex == 1 && isInHomeSidNid(this.mSS.getCdmaSystemId(), this.mSS.getCdmaNetworkId()) && this.mIccRecords != null && !TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                        this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                    }
                }
            }
        }
    }

    private boolean isInHomeSidNid(int sid, int nid) {
        if (isSidsAllZeros() || this.mHomeSystemId.length != this.mHomeNetworkId.length || sid == 0) {
            return true;
        }
        for (int i = 0; i < this.mHomeSystemId.length; i++) {
            if (this.mHomeSystemId[i] == sid && (this.mHomeNetworkId[i] == 0 || this.mHomeNetworkId[i] == 65535 || nid == 0 || nid == 65535 || this.mHomeNetworkId[i] == nid)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setOperatorIdd(String operatorNumeric) {
        if (PLUS_TRANFER_IN_MDOEM) {
            log("setOperatorIdd() return. because of PLUS_TRANFER_IN_MDOEM=" + PLUS_TRANFER_IN_MDOEM);
            return;
        }
        String idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
        if (idd == null || idd.isEmpty()) {
            this.mPhone.setGlobalSystemProperty("gsm.operator.idpstring", "+");
        } else {
            this.mPhone.setGlobalSystemProperty("gsm.operator.idpstring", idd);
        }
    }

    private boolean isInvalidOperatorNumeric(String operatorNumeric) {
        return operatorNumeric == null || operatorNumeric.length() < 5 || operatorNumeric.startsWith(INVALID_MCC);
    }

    private String fixUnknownMcc(String operatorNumeric, int sid) {
        boolean isNitzTimeZone;
        TimeZone tzone;
        TimeZone tzone2;
        if (sid <= 0) {
            return operatorNumeric;
        }
        int i = 0;
        if (this.mNitzState.getSavedTimeZoneId() != null) {
            tzone = TimeZone.getTimeZone(this.mNitzState.getSavedTimeZoneId());
            isNitzTimeZone = true;
        } else {
            NitzData lastNitzData = this.mNitzState.getCachedNitzData();
            if (lastNitzData == null) {
                tzone2 = null;
            } else {
                tzone2 = TimeZoneLookupHelper.guessZoneByNitzStatic(lastNitzData);
                StringBuilder sb = new StringBuilder();
                sb.append("fixUnknownMcc(): guessNitzTimeZone returned ");
                sb.append(tzone2 == null ? tzone2 : tzone2.getID());
                log(sb.toString());
            }
            tzone = tzone2;
            isNitzTimeZone = false;
        }
        int utcOffsetHours = 0;
        if (tzone != null) {
            utcOffsetHours = tzone.getRawOffset() / MS_PER_HOUR;
        }
        NitzData nitzData = this.mNitzState.getCachedNitzData();
        boolean isDst = nitzData != null && nitzData.isDst();
        HbpcdUtils hbpcdUtils = this.mHbpcdUtils;
        if (isDst) {
            i = 1;
        }
        if (hbpcdUtils.getMcc(sid, utcOffsetHours, i, isNitzTimeZone) > 0) {
            operatorNumeric = Integer.toString(mcc) + DEFAULT_MNC;
        }
        return operatorNumeric;
    }

    private boolean isGprsConsistent(int dataRegState, int voiceRegState) {
        return voiceRegState != 0 || dataRegState == 0;
    }

    private int regCodeToServiceState(int code) {
        if (code == 1 || code == 5) {
            return 0;
        }
        return 1;
    }

    private boolean regCodeIsRoaming(int code) {
        return 5 == code;
    }

    private boolean isSameOperatorNameFromSimAndSS(ServiceState s) {
        String spn = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNameForPhone(getPhoneId());
        String onsl = s.getOperatorAlphaLong();
        String onss = s.getOperatorAlphaShort();
        boolean equalsOnsl = !TextUtils.isEmpty(spn) && spn.equalsIgnoreCase(onsl);
        boolean equalsOnss = !TextUtils.isEmpty(spn) && spn.equalsIgnoreCase(onss);
        if (equalsOnsl || equalsOnss) {
            return true;
        }
        return false;
    }

    private boolean isSameNamedOperators(ServiceState s) {
        return currentMccEqualsSimMcc(s) && isSameOperatorNameFromSimAndSS(s);
    }

    private boolean currentMccEqualsSimMcc(ServiceState s) {
        try {
            return ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId()).substring(0, 3).equals(s.getOperatorNumeric().substring(0, 3));
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isOperatorConsideredNonRoaming(ServiceState s) {
        return false;
    }

    private boolean isOperatorConsideredRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        String[] numericArray = null;
        if (configManager != null) {
            PersistableBundle config = configManager.getConfigForSubId(this.mPhone.getSubId());
            if (config != null) {
                numericArray = config.getStringArray("roaming_operator_string_array");
            }
        }
        if (ArrayUtils.isEmpty(numericArray) || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (!TextUtils.isEmpty(numeric) && operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private void onRestrictedStateChanged(AsyncResult ar) {
        RestrictedState newRs = new RestrictedState();
        log("onRestrictedStateChanged: E rs " + this.mRestrictedState);
        if (ar.exception == null && ar.result != null) {
            int state = ((Integer) ar.result).intValue();
            boolean z = false;
            newRs.setCsEmergencyRestricted(((state & 1) == 0 && (state & 4) == 0) ? false : true);
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY) {
                newRs.setCsNormalRestricted(((state & 2) == 0 && (state & 4) == 0) ? false : true);
                if ((state & 16) != 0) {
                    z = true;
                }
                newRs.setPsRestricted(z);
            }
            log("onRestrictedStateChanged: new rs " + newRs);
            if (!this.mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                this.mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(1001);
            } else if (this.mRestrictedState.isPsRestricted() && !newRs.isPsRestricted()) {
                this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                setNotification(1002);
            }
            if (this.mRestrictedState.isCsRestricted()) {
                if (!newRs.isAnyCsRestricted()) {
                    setNotification(1004);
                } else if (!newRs.isCsNormalRestricted()) {
                    setNotification(1006);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    setNotification(1005);
                }
            } else if (!this.mRestrictedState.isCsEmergencyRestricted() || this.mRestrictedState.isCsNormalRestricted()) {
                if (this.mRestrictedState.isCsEmergencyRestricted() || !this.mRestrictedState.isCsNormalRestricted()) {
                    if (newRs.isCsRestricted()) {
                        setNotification(1003);
                    } else if (newRs.isCsEmergencyRestricted()) {
                        setNotification(1006);
                    } else if (newRs.isCsNormalRestricted()) {
                        setNotification(1005);
                    }
                } else if (!newRs.isAnyCsRestricted()) {
                    setNotification(1004);
                } else if (newRs.isCsRestricted()) {
                    setNotification(1003);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(1006);
                }
            } else if (!newRs.isAnyCsRestricted()) {
                setNotification(1004);
            } else if (newRs.isCsRestricted()) {
                setNotification(1003);
            } else if (newRs.isCsNormalRestricted()) {
                setNotification(1005);
            }
            this.mRestrictedState = newRs;
        }
        log("onRestrictedStateChanged: X rs " + this.mRestrictedState);
    }

    public CellLocation getCellLocation(WorkSource workSource) {
        if (((GsmCellLocation) this.mCellLoc).getLac() >= 0 && ((GsmCellLocation) this.mCellLoc).getCid() >= 0) {
            return this.mCellLoc;
        }
        List<CellInfo> result = getAllCellInfo(workSource);
        if (result == null) {
            return this.mCellLoc;
        }
        GsmCellLocation cellLocOther = new GsmCellLocation();
        for (CellInfo ci : result) {
            if (ci instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                cellLocOther.setPsc(cellIdentityGsm.getPsc());
                return cellLocOther;
            } else if (ci instanceof CellInfoWcdma) {
                CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                return cellLocOther;
            } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                    cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                    cellLocOther.setPsc(0);
                }
            }
        }
        return cellLocOther;
    }

    private void setTimeFromNITZString(String nitzString, long nitzReceiveTime) {
        String str;
        String str2;
        long start = SystemClock.elapsedRealtime();
        String str3 = this.LOG_TAG;
        Rlog.d(str3, "NITZ: " + nitzString + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
        NitzData newNitzData = NitzData.parse(nitzString);
        if (newNitzData != null) {
            try {
                this.mNitzState.handleNitzReceived(new TimeStampedValue<>(newNitzData, nitzReceiveTime));
            } finally {
                long end = SystemClock.elapsedRealtime();
                String str4 = this.LOG_TAG;
                StringBuilder sb = new StringBuilder();
                str = "NITZ: end=";
                sb.append(str);
                sb.append(end);
                str2 = " dur=";
                sb.append(str2);
                sb.append(end - start);
                Rlog.d(str4, sb.toString());
            }
        }
    }

    private void cancelAllNotifications() {
        log("cancelAllNotifications: mPrevSubId=" + this.mPrevSubId);
        NotificationManager notificationManager = (NotificationManager) this.mPhone.getContext().getSystemService("notification");
        if (SubscriptionManager.isValidSubscriptionId(this.mPrevSubId)) {
            notificationManager.cancel(Integer.toString(this.mPrevSubId), PS_NOTIFICATION);
            notificationManager.cancel(Integer.toString(this.mPrevSubId), CS_NOTIFICATION);
            notificationManager.cancel(Integer.toString(this.mPrevSubId), 111);
        }
    }

    @VisibleForTesting
    public void setNotification(int notifyType) {
        CharSequence charSequence;
        CharSequence charSequence2;
        CharSequence charSequence3;
        CharSequence charSequence4;
        int i = notifyType;
        if (this.mHwCustGsmServiceStateTracker != null && this.mHwCustGsmServiceStateTracker.isCsPopShow(i)) {
            log("cs notification no need to send" + i);
        } else if (SystemProperties.getBoolean("ro.hwpp.cell_access_report", false)) {
            log("setNotification: create notification " + i);
            if (!SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
                loge("cannot setNotification on invalid subid mSubId=" + this.mSubId);
            } else if (!SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
                loge("cannot setNotification on invalid subid mSubId=" + this.mSubId);
            } else if (!this.mPhone.getContext().getResources().getBoolean(17957067)) {
                log("Ignore all the notifications");
            } else {
                Context context = this.mPhone.getContext();
                CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
                if (configManager != null) {
                    PersistableBundle bundle = configManager.getConfig();
                    if (bundle != null && bundle.getBoolean("disable_voice_barring_notification_bool", false) && (i == 1003 || i == 1005 || i == 1006)) {
                        log("Voice/emergency call barred notification disabled");
                        return;
                    }
                }
                CharSequence details = "";
                CharSequence title = "";
                int notificationId = CS_NOTIFICATION;
                int icon = 17301642;
                boolean multipleSubscriptions = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getPhoneCount() > 1;
                int simNumber = this.mSubscriptionController.getSlotIndex(this.mSubId) + 1;
                if (i != 2001) {
                    switch (i) {
                        case 1001:
                            int simNumber2 = simNumber;
                            if (((long) SubscriptionManager.getDefaultDataSubscriptionId()) == ((long) this.mPhone.getSubId())) {
                                notificationId = PS_NOTIFICATION;
                                title = context.getText(17039466);
                                if (multipleSubscriptions) {
                                    charSequence = context.getString(17039477, new Object[]{Integer.valueOf(simNumber2)});
                                } else {
                                    charSequence = context.getText(17039476);
                                }
                                details = charSequence;
                                break;
                            } else {
                                return;
                            }
                        case 1002:
                            notificationId = PS_NOTIFICATION;
                            break;
                        case 1003:
                            title = context.getText(17039463);
                            if (multipleSubscriptions) {
                                charSequence2 = context.getString(17039477, new Object[]{Integer.valueOf(simNumber)});
                            } else {
                                charSequence2 = context.getText(17039476);
                            }
                            details = charSequence2;
                            break;
                        case 1005:
                            title = context.getText(17039472);
                            if (multipleSubscriptions) {
                                charSequence3 = context.getString(17039477, new Object[]{Integer.valueOf(simNumber)});
                            } else {
                                charSequence3 = context.getText(17039476);
                            }
                            details = charSequence3;
                            break;
                        case 1006:
                            title = context.getText(17039469);
                            if (multipleSubscriptions) {
                                charSequence4 = context.getString(17039477, new Object[]{Integer.valueOf(simNumber)});
                            } else {
                                charSequence4 = context.getText(17039476);
                            }
                            details = charSequence4;
                            break;
                    }
                } else {
                    notificationId = 111;
                    int resId = selectResourceForRejectCode(this.mRejectCode, multipleSubscriptions);
                    if (resId == 0) {
                        loge("setNotification: mRejectCode=" + this.mRejectCode + " is not handled.");
                        return;
                    }
                    icon = 17303476;
                    title = context.getString(resId, new Object[]{Integer.valueOf(this.mSubId)});
                    details = null;
                }
                log("setNotification, create notification, notifyType: " + i + ", title: " + title + ", details: " + details + ", subId: " + this.mSubId);
                this.mNotification = new Notification.Builder(context).setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(icon).setTicker(title).setColor(context.getResources().getColor(17170784)).setContentTitle(title).setStyle(new Notification.BigTextStyle().bigText(details)).setContentText(details).setChannel(NotificationChannelController.CHANNEL_ID_ALERT).build();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                if (i == 1002 || i == 1004) {
                    notificationManager.cancel(Integer.toString(this.mSubId), notificationId);
                } else {
                    boolean show = false;
                    if (this.mSS.isEmergencyOnly() && i == 1006) {
                        show = true;
                    } else if (i == 2001) {
                        show = true;
                    } else if (this.mSS.getState() == 0) {
                        show = true;
                    }
                    if (show) {
                        notificationManager.notify(Integer.toString(this.mSubId), notificationId, this.mNotification);
                    }
                }
            }
        }
    }

    private int selectResourceForRejectCode(int rejCode, boolean multipleSubscriptions) {
        int rejResourceId;
        int rejResourceId2;
        int rejResourceId3;
        int rejResourceId4;
        if (rejCode != 6) {
            switch (rejCode) {
                case 1:
                    if (multipleSubscriptions) {
                        rejResourceId2 = 17040523;
                    } else {
                        rejResourceId2 = 17040522;
                    }
                    return rejResourceId2;
                case 2:
                    if (multipleSubscriptions) {
                        rejResourceId3 = 17040529;
                    } else {
                        rejResourceId3 = 17040528;
                    }
                    return rejResourceId3;
                case 3:
                    if (multipleSubscriptions) {
                        rejResourceId4 = 17040527;
                    } else {
                        rejResourceId4 = 17040526;
                    }
                    return rejResourceId4;
                default:
                    return 0;
            }
        } else {
            if (multipleSubscriptions) {
                rejResourceId = 17040525;
            } else {
                rejResourceId = 17040524;
            }
            return rejResourceId;
        }
    }

    private UiccCardApplication getUiccCardApplication() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 2);
    }

    private void queueNextSignalStrengthPoll() {
        if (!this.mDontPollSignalStrength && this.mDefaultDisplayState == 2) {
            Message msg = obtainMessage();
            msg.what = 10;
            sendMessageDelayed(msg, 20000);
        }
    }

    private void notifyCdmaSubscriptionInfoReady() {
        if (this.mCdmaForSubscriptionInfoReadyRegistrants != null) {
            log("CDMA_SUBSCRIPTION: call notifyRegistrants()");
            this.mCdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
        }
    }

    public void registerForDataConnectionAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mAttachedRegistrants.add(r);
        if (getCurrentDataConnectionState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionAttached(Handler h) {
        this.mAttachedRegistrants.remove(h);
    }

    public void registerForDataConnectionDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDetachedRegistrants.add(r);
        if (getCurrentDataConnectionState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionDetached(Handler h) {
        this.mDetachedRegistrants.remove(h);
    }

    public void registerForDataRegStateOrRatChanged(Handler h, int what, Object obj) {
        this.mDataRegStateOrRatChangedRegistrants.add(new Registrant(h, what, obj));
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    public void unregisterForDataRegStateOrRatChanged(Handler h) {
        this.mDataRegStateOrRatChangedRegistrants.remove(h);
    }

    public void registerForNetworkAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkAttachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkAttached(Handler h) {
        this.mNetworkAttachedRegistrants.remove(h);
    }

    public void registerForNetworkDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkDetachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkDetached(Handler h) {
        this.mNetworkDetachedRegistrants.remove(h);
    }

    public void registerForPsRestrictedEnabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictEnabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedEnabled(Handler h) {
        this.mPsRestrictEnabledRegistrants.remove(h);
    }

    public void registerForPsRestrictedDisabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictDisabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedDisabled(Handler h) {
        this.mPsRestrictDisabledRegistrants.remove(h);
    }

    public void powerOffRadioSafely(DcTracker dcTracker) {
        synchronized (this) {
            if (RESET_PROFILE && this.mPhone.getContext() != null && Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", -1) == 0) {
                Rlog.d(this.LOG_TAG, "powerOffRadioSafely, it is not airplaneMode, resetProfile.");
                this.mCi.resetProfile(null);
            }
            if (this.mPendingRadioPowerOffAfterDataOff == 0) {
                int dds = SubscriptionManager.getDefaultDataSubscriptionId();
                boolean isDisconnected = false;
                if (HW_FAST_SET_RADIO_OFF || IS_HISI_PLATFORM) {
                    isDisconnected = dcTracker.isDisconnectedOrConnecting();
                }
                if (!isDisconnected) {
                    if (dcTracker.isDisconnected()) {
                        if (dds != this.mPhone.getSubId() && (dds == this.mPhone.getSubId() || !ProxyController.getInstance().isDataDisconnected(dds))) {
                            if (!SubscriptionManager.isValidSubscriptionId(dds)) {
                            }
                        }
                    }
                    if (this.mPhone.isInCall()) {
                        this.mPhone.mCT.mRingingCall.hangupIfAlive();
                        this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
                        this.mPhone.mCT.mForegroundCall.hangupIfAlive();
                    }
                    ImsPhone imsPhone = null;
                    if (this.mPhone.getImsPhone() != null) {
                        imsPhone = (ImsPhone) this.mPhone.getImsPhone();
                    }
                    if (imsPhone != null && imsPhone.isInCall()) {
                        imsPhone.getForegroundCall().hangupIfAlive();
                        imsPhone.getBackgroundCall().hangupIfAlive();
                        imsPhone.getRingingCall().hangupIfAlive();
                    }
                    dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                    if (dds != this.mPhone.getSubId() && !ProxyController.getInstance().isDataDisconnected(dds)) {
                        log("Data is active on DDS.  Wait for all data disconnect");
                        ProxyController.getInstance().registerForAllDataDisconnected(dds, this, 49, null);
                        this.mPendingRadioPowerOffAfterDataOff = true;
                    }
                    Message msg = Message.obtain(this);
                    msg.what = 38;
                    int i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                    this.mPendingRadioPowerOffAfterDataOffTag = i;
                    msg.arg1 = i;
                    if (sendMessageDelayed(msg, 30000)) {
                        log("Wait upto 30s for data to disconnect, then turn off radio.");
                        acquireWakeLock();
                        this.mPendingRadioPowerOffAfterDataOff = true;
                    } else {
                        log("Cannot send delayed Msg, turn off radio right away.");
                        hangupAndPowerOff();
                        this.mPendingRadioPowerOffAfterDataOff = false;
                    }
                }
                dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                log("Data disconnected, turn off radio right away.");
                hangupAndPowerOff();
            }
        }
    }

    public boolean processPendingRadioPowerOffAfterDataOff() {
        synchronized (this) {
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                return false;
            }
            HwTelephonyFactory.getHwNetworkManager().delaySendDetachAfterDataOff(this.mPhone);
            this.mPendingRadioPowerOffAfterDataOffTag++;
            hangupAndPowerOff();
            this.mPendingRadioPowerOffAfterDataOff = false;
            return true;
        }
    }

    private boolean containsEarfcnInEarfcnRange(ArrayList<Pair<Integer, Integer>> earfcnPairList, int earfcn) {
        if (earfcnPairList != null) {
            Iterator<Pair<Integer, Integer>> it = earfcnPairList.iterator();
            while (it.hasNext()) {
                Pair<Integer, Integer> earfcnPair = it.next();
                if (earfcn >= ((Integer) earfcnPair.first).intValue() && earfcn <= ((Integer) earfcnPair.second).intValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Pair<Integer, Integer>> convertEarfcnStringArrayToPairList(String[] earfcnsList) {
        ArrayList<Pair<Integer, Integer>> earfcnPairList = new ArrayList<>();
        if (earfcnsList != null) {
            int i = 0;
            while (i < earfcnsList.length) {
                try {
                    String[] earfcns = earfcnsList[i].split("-");
                    if (earfcns.length != 2) {
                        return null;
                    }
                    int earfcnStart = Integer.parseInt(earfcns[0]);
                    int earfcnEnd = Integer.parseInt(earfcns[1]);
                    if (earfcnStart > earfcnEnd) {
                        return null;
                    }
                    earfcnPairList.add(new Pair(Integer.valueOf(earfcnStart), Integer.valueOf(earfcnEnd)));
                    i++;
                } catch (PatternSyntaxException e) {
                    return null;
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
        return earfcnPairList;
    }

    /* access modifiers changed from: private */
    public void onCarrierConfigChanged() {
        PersistableBundle config = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        if (config != null) {
            updateLteEarfcnLists(config);
            updateReportingCriteria(config);
        }
    }

    private void updateLteEarfcnLists(PersistableBundle config) {
        synchronized (this.mLteRsrpBoostLock) {
            this.mLteRsrpBoost = config.getInt("lte_earfcns_rsrp_boost_int", 0);
            this.mEarfcnPairListForRsrpBoost = convertEarfcnStringArrayToPairList(config.getStringArray("boosted_lte_earfcns_string_array"));
        }
    }

    private void updateReportingCriteria(PersistableBundle config) {
        this.mPhone.setSignalStrengthReportingCriteria(config.getIntArray("lte_rsrp_thresholds_int_array"), 3);
        this.mPhone.setSignalStrengthReportingCriteria(config.getIntArray("wcdma_rscp_thresholds_int_array"), 2);
    }

    private void updateServiceStateLteEarfcnBoost(ServiceState serviceState, int lteEarfcn) {
        synchronized (this.mLteRsrpBoostLock) {
            if (lteEarfcn != -1) {
                try {
                    if (containsEarfcnInEarfcnRange(this.mEarfcnPairListForRsrpBoost, lteEarfcn)) {
                        serviceState.setLteEarfcnRsrpBoost(this.mLteRsrpBoost);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            serviceState.setLteEarfcnRsrpBoost(0);
        }
    }

    private boolean getDelaySendSignalFutureState() {
        int subId = this.mPhone.getSubId();
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("delay_send_signal", subId, Boolean.class);
        boolean valueFromProp = FEATURE_DELAY_UPDATE_SIGANL_STENGTH;
        log("getDelaySendSignalFutureState, subId:" + subId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    public boolean onSignalStrengthResult(AsyncResult ar) {
        boolean isGsm = false;
        int dataRat = this.mSS.getRilDataRadioTechnology();
        int voiceRat = this.mSS.getRilVoiceRadioTechnology();
        if (this.mPhone.isPhoneTypeGsm() || ((dataRat != 18 && ServiceState.isGsm(dataRat)) || (voiceRat != 18 && ServiceState.isGsm(voiceRat)))) {
            isGsm = true;
        }
        if (getDelaySendSignalFutureState()) {
            return HwTelephonyFactory.getHwNetworkManager().signalStrengthResultHW(this, this.mPhone, ar, this.mSignalStrength, isGsm);
        }
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            this.mSignalStrength = new SignalStrength(isGsm);
        } else {
            this.mSignalStrength = (SignalStrength) ar.result;
            this.mSignalStrength.validateInput();
            if (dataRat == 0 && voiceRat == 0) {
                this.mSignalStrength.fixType();
            } else {
                this.mSignalStrength.setGsm(isGsm);
            }
            this.mSignalStrength.setLteRsrpBoost(this.mSS.getLteEarfcnRsrpBoost());
            PersistableBundle config = getCarrierConfig();
            this.mSignalStrength.setUseOnlyRsrpForLteLevel(config.getBoolean("use_only_rsrp_for_lte_signal_bar_bool"));
            this.mSignalStrength.setLteRsrpThresholds(config.getIntArray("lte_rsrp_thresholds_int_array"));
            this.mSignalStrength.setWcdmaDefaultSignalMeasurement(config.getString("wcdma_default_signal_strength_measurement_string"));
            this.mSignalStrength.setWcdmaRscpThresholds(config.getIntArray("wcdma_rscp_thresholds_int_array"));
        }
        HwTelephonyFactory.getHwNetworkManager().updateHwnff(this, this.mSignalStrength);
        return notifySignalStrength();
    }

    /* access modifiers changed from: protected */
    public void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        this.mCi.setRadioPower(false, obtainMessage(54));
    }

    /* access modifiers changed from: protected */
    public void cancelPollState() {
        this.mPollingContext = new int[1];
    }

    private boolean networkCountryIsoChanged(String newCountryIsoCode, String prevCountryIsoCode) {
        if (TextUtils.isEmpty(newCountryIsoCode)) {
            log("countryIsoChanged: no new country ISO code");
            return false;
        } else if (!TextUtils.isEmpty(prevCountryIsoCode)) {
            return !newCountryIsoCode.equals(prevCountryIsoCode);
        } else {
            log("countryIsoChanged: no previous country ISO code");
            return true;
        }
    }

    private boolean iccCardExists() {
        if (this.mUiccApplcation == null) {
            return false;
        }
        return this.mUiccApplcation.getState() != IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN;
    }

    public String getSystemProperty(String property, String defValue) {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), property, defValue);
    }

    public List<CellInfo> getAllCellInfo(WorkSource workSource) {
        CellInfoResult result = new CellInfoResult();
        if (this.mCi.getRilVersion() < 8) {
            log("SST.getAllCellInfo(): not implemented");
            result.list = null;
        } else if (!isCallerOnDifferentThread()) {
            log("SST.getAllCellInfo(): return last, same thread can't block");
            result.list = this.mLastCellInfoList;
        } else if (HwTelephonyFactory.getHwNetworkManager().isCellRequestStrategyPassed(this, workSource, this.mPhone)) {
            Message msg = obtainMessage(43, result);
            synchronized (result.lockObj) {
                result.list = null;
                if (workSource != null) {
                    HwTelephonyFactory.getHwNetworkManager().countPackageUseCellInfo(this, this.mPhone, workSource.getName(0));
                }
                this.mCi.getCellInfoList(msg, workSource);
                try {
                    result.lockObj.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            log("SST.getAllCellInfo(): return last, back to back calls");
            result.list = this.mLastCellInfoList;
        }
        synchronized (result.lockObj) {
            if (result.list != null) {
                List<CellInfo> list = result.list;
                return list;
            }
            log("SST.getAllCellInfo(): X size=0 list=null");
            return null;
        }
    }

    public SignalStrength getSignalStrength() {
        return this.mSignalStrength;
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mCdmaForSubscriptionInfoReadyRegistrants.add(r);
        if (isMinInfoReady()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mCdmaForSubscriptionInfoReadyRegistrants.remove(h);
    }

    private void saveCdmaSubscriptionSource(int source) {
        log("Storing cdma subscription source: " + source);
        Settings.Global.putInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", source);
        log("Read from settings: " + Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", -1));
    }

    private void getSubscriptionInfoAndStartPollingThreads() {
        this.mCi.getCDMASubscription(obtainMessage(34));
        pollState();
    }

    private void handleCdmaSubscriptionSource(int newSubscriptionSource) {
        log("Subscription Source : " + newSubscriptionSource);
        this.mIsSubscriptionFromRuim = newSubscriptionSource == 0;
        log("isFromRuim: " + this.mIsSubscriptionFromRuim);
        saveCdmaSubscriptionSource(newSubscriptionSource);
        if (!this.mIsSubscriptionFromRuim) {
            unregisterForRuimEvents();
            sendMessage(obtainMessage(35));
            return;
        }
        registerForRuimEvents();
    }

    private void dumpEarfcnPairList(PrintWriter pw) {
        pw.print(" mEarfcnPairListForRsrpBoost={");
        if (this.mEarfcnPairListForRsrpBoost != null) {
            int i = this.mEarfcnPairListForRsrpBoost.size();
            Iterator<Pair<Integer, Integer>> it = this.mEarfcnPairListForRsrpBoost.iterator();
            while (it.hasNext()) {
                Pair<Integer, Integer> earfcnPair = it.next();
                pw.print("(");
                pw.print(earfcnPair.first);
                pw.print(",");
                pw.print(earfcnPair.second);
                pw.print(")");
                i--;
                if (i != 0) {
                    pw.print(",");
                }
            }
        }
        pw.println("}");
    }

    private void dumpCellInfoList(PrintWriter pw) {
        pw.print(" mLastCellInfoList={");
        if (this.mLastCellInfoList != null) {
            boolean first = true;
            for (CellInfo info : this.mLastCellInfoList) {
                if (!first) {
                    pw.print(",");
                }
                first = false;
                pw.print(info.toString());
            }
        }
        pw.println("}");
    }

    private void registerForRuimEvents() {
        log("registerForRuimEvents");
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.registerForReady(this, 26, null);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.registerForRecordsLoaded(this, 27, null);
        }
    }

    private void unregisterForRuimEvents() {
        log("unregisterForRuimEvents");
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.unregisterForReady(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForRecordsLoaded(this);
        }
    }

    public void setSignalStrength(SignalStrength signalStrength) {
        log("setSignalStrength : " + signalStrength);
        this.mSignalStrength = signalStrength;
    }

    public void setDoRecoveryTriggerState(boolean state) {
        this.mRadioOffByDoRecovery = state;
        log("setDoRecoveryTriggerState, " + this.mRadioOffByDoRecovery);
    }

    public boolean getDoRecoveryTriggerState() {
        return this.mRadioOffByDoRecovery;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ServiceStateTracker:");
        pw.println(" mSubId=" + this.mSubId);
        pw.println(" mSS=" + this.mSS);
        pw.println(" mNewSS=" + this.mNewSS);
        pw.println(" mVoiceCapable=" + this.mVoiceCapable);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        StringBuilder sb = new StringBuilder();
        sb.append(" mPollingContext=");
        sb.append(this.mPollingContext);
        sb.append(" - ");
        sb.append(this.mPollingContext != null ? Integer.valueOf(this.mPollingContext[0]) : "");
        pw.println(sb.toString());
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mDontPollSignalStrength=" + this.mDontPollSignalStrength);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellLoc=" + Rlog.pii(false, this.mCellLoc));
        pw.println(" mNewCellLoc=" + Rlog.pii(false, this.mNewCellLoc));
        pw.println(" mLastCellInfoListTime=" + this.mLastCellInfoListTime);
        dumpCellInfoList(pw);
        pw.flush();
        pw.println(" mPreferredNetworkType=" + this.mPreferredNetworkType);
        pw.println(" mMaxDataCalls=" + this.mMaxDataCalls);
        pw.println(" mNewMaxDataCalls=" + this.mNewMaxDataCalls);
        pw.println(" mReasonDataDenied=" + this.mReasonDataDenied);
        pw.println(" mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        pw.println(" mGsmRoaming=" + this.mGsmRoaming);
        pw.println(" mDataRoaming=" + this.mDataRoaming);
        pw.println(" mEmergencyOnly=" + this.mEmergencyOnly);
        pw.flush();
        this.mNitzState.dumpState(pw);
        pw.flush();
        pw.println(" mStartedGprsRegCheck=" + this.mStartedGprsRegCheck);
        pw.println(" mReportedGprsNoReg=" + this.mReportedGprsNoReg);
        pw.println(" mNotification=" + this.mNotification);
        pw.println(" mCurSpn=" + this.mCurSpn);
        pw.println(" mCurDataSpn=" + this.mCurDataSpn);
        pw.println(" mCurShowSpn=" + this.mCurShowSpn);
        pw.println(" mCurPlmn=" + this.mCurPlmn);
        pw.println(" mCurShowPlmn=" + this.mCurShowPlmn);
        pw.flush();
        pw.println(" mCurrentOtaspMode=" + this.mCurrentOtaspMode);
        pw.println(" mRoamingIndicator=" + this.mRoamingIndicator);
        pw.println(" mIsInPrl=" + this.mIsInPrl);
        pw.println(" mDefaultRoamingIndicator=" + this.mDefaultRoamingIndicator);
        pw.println(" mRegistrationState=" + this.mRegistrationState);
        pw.println(" mMdn=xxxx");
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.println(" mMin=xxxx");
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mIsMinInfoReady=" + this.mIsMinInfoReady);
        pw.println(" mIsEriTextLoaded=" + this.mIsEriTextLoaded);
        pw.println(" mIsSubscriptionFromRuim=" + this.mIsSubscriptionFromRuim);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mRegistrationDeniedReason=" + this.mRegistrationDeniedReason);
        pw.println(" mCurrentCarrier=" + this.mCurrentCarrier);
        pw.flush();
        pw.println(" mImsRegistered=" + this.mImsRegistered);
        pw.println(" mImsRegistrationOnOff=" + this.mImsRegistrationOnOff);
        pw.println(" mAlarmSwitch=" + this.mAlarmSwitch);
        pw.println(" mRadioDisabledByCarrier" + this.mRadioDisabledByCarrier);
        pw.println(" mPowerOffDelayNeed=" + this.mPowerOffDelayNeed);
        pw.println(" mDeviceShuttingDown=" + this.mDeviceShuttingDown);
        pw.println(" mSpnUpdatePending=" + this.mSpnUpdatePending);
        pw.println(" mLteRsrpBoost=" + this.mLteRsrpBoost);
        dumpEarfcnPairList(pw);
        this.mLocaleTracker.dump(fd, pw, args);
        pw.println(" Roaming Log:");
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.increaseIndent();
        this.mRoamingLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Attach Log:");
        ipw.increaseIndent();
        this.mAttachLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Phone Change Log:");
        ipw.increaseIndent();
        this.mPhoneTypeLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Rat Change Log:");
        ipw.increaseIndent();
        this.mRatLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Radio power Log:");
        ipw.increaseIndent();
        this.mRadioPowerLog.dump(fd, ipw, args);
        this.mNitzState.dumpLogs(fd, ipw, args);
    }

    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    /* access modifiers changed from: protected */
    public void checkCorrectThread() {
        if (Thread.currentThread() != getLooper().getThread()) {
            throw new RuntimeException("ServiceStateTracker must be used from within one thread");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCallerOnDifferentThread() {
        return Thread.currentThread() != getLooper().getThread();
    }

    /* access modifiers changed from: protected */
    public void updateCarrierMccMncConfiguration(String newOp, String oldOp, Context context) {
        if ((newOp == null && !TextUtils.isEmpty(oldOp)) || (newOp != null && !newOp.equals(oldOp))) {
            log("update mccmnc=" + newOp + " fromServiceState=true");
            MccTable.updateMccMncConfiguration(context, newOp, true);
        }
    }

    /* access modifiers changed from: protected */
    public boolean inSameCountry(String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric) || operatorNumeric.length() < 5) {
            return false;
        }
        String homeNumeric = getHomeOperatorNumeric();
        if (TextUtils.isEmpty(homeNumeric) || homeNumeric.length() < 5) {
            return false;
        }
        String networkMCC = operatorNumeric.substring(0, 3);
        String homeMCC = homeNumeric.substring(0, 3);
        String networkCountry = "";
        String homeCountry = "";
        try {
            networkCountry = MccTable.countryCodeForMcc(Integer.parseInt(networkMCC));
            homeCountry = MccTable.countryCodeForMcc(Integer.parseInt(homeMCC));
        } catch (NumberFormatException ex) {
            log("inSameCountry: get networkCountry or homeCountry error: " + ex);
        }
        if (networkCountry.isEmpty() || homeCountry.isEmpty()) {
            return false;
        }
        boolean inSameCountry = homeCountry.equals(networkCountry);
        if (inSameCountry) {
            return inSameCountry;
        }
        if ("us".equals(homeCountry) && "vi".equals(networkCountry)) {
            inSameCountry = true;
        } else if ("vi".equals(homeCountry) && "us".equals(networkCountry)) {
            inSameCountry = true;
        }
        return inSameCountry;
    }

    /* access modifiers changed from: protected */
    public void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService = currentServiceState.getVoiceRegState() == 0;
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17235994);
                if (intRoamingIndicators != null && intRoamingIndicators.length > 0) {
                    currentServiceState.setVoiceRoamingType(2);
                    int curRoamingIndicator = currentServiceState.getCdmaRoamingIndicator();
                    int i = 0;
                    while (true) {
                        if (i >= intRoamingIndicators.length) {
                            break;
                        } else if (curRoamingIndicator == intRoamingIndicators[i]) {
                            currentServiceState.setVoiceRoamingType(3);
                            break;
                        } else {
                            i++;
                        }
                    }
                } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(2);
                } else {
                    currentServiceState.setVoiceRoamingType(3);
                }
            } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                currentServiceState.setVoiceRoamingType(2);
            } else {
                currentServiceState.setVoiceRoamingType(3);
            }
        }
        boolean isDataInService = currentServiceState.getDataRegState() == 0;
        int dataRegType = currentServiceState.getRilDataRadioTechnology();
        if (!isDataInService) {
            return;
        }
        if (!currentServiceState.getDataRoaming()) {
            currentServiceState.setDataRoamingType(0);
        } else if (this.mPhone.isPhoneTypeGsm()) {
            if (!ServiceState.isGsm(dataRegType)) {
                currentServiceState.setDataRoamingType(1);
            } else if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (ServiceState.isCdma(dataRegType)) {
            if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (inSameCountry(currentServiceState.getDataOperatorNumeric())) {
            currentServiceState.setDataRoamingType(2);
        } else {
            currentServiceState.setDataRoamingType(3);
        }
    }

    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength(true);
    }

    /* access modifiers changed from: protected */
    public String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "");
    }

    /* access modifiers changed from: protected */
    public int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    /* access modifiers changed from: protected */
    public void resetServiceStateInIwlanMode() {
        if (this.mCi.getRadioState() == CommandsInterface.RadioState.RADIO_OFF) {
            boolean resetIwlanRatVal = false;
            log("set service state as POWER_OFF");
            if (18 == this.mNewSS.getRilDataRadioTechnology()) {
                log("pollStateDone: mNewSS = " + this.mNewSS);
                log("pollStateDone: reset iwlan RAT value");
                resetIwlanRatVal = true;
            }
            String operator = this.mNewSS.getOperatorAlphaLong();
            this.mNewSS.setStateOff();
            if (resetIwlanRatVal) {
                this.mNewSS.setRilDataRadioTechnology(18);
                this.mNewSS.setDataRegState(0);
                this.mNewSS.setOperatorAlphaLong(operator);
                log("pollStateDone: mNewSS = " + this.mNewSS);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final boolean alwaysOnHomeNetwork(BaseBundle b) {
        return b.getBoolean("force_home_network_bool");
    }

    private boolean isInNetwork(BaseBundle b, String network, String key) {
        String[] networks = b.getStringArray(key);
        if (networks == null || !Arrays.asList(networks).contains(network)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public final boolean isRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_roaming_networks_string_array");
    }

    /* access modifiers changed from: protected */
    public final boolean isNonRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_nonroaming_networks_string_array");
    }

    /* access modifiers changed from: protected */
    public final boolean isRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_roaming_networks_string_array");
    }

    /* access modifiers changed from: protected */
    public final boolean isNonRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_nonroaming_networks_string_array");
    }

    public boolean isDeviceShuttingDown() {
        return this.mDeviceShuttingDown;
    }

    /* access modifiers changed from: protected */
    public void getCaller() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            log("    at " + ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")");
        }
    }

    /* access modifiers changed from: protected */
    public void acquireWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) this.mPhone.getContext().getSystemService("power")).newWakeLock(1, "SERVICESTATE_WAIT_DISCONNECT_WAKELOCK");
        }
        this.mWakeLock.setReferenceCounted(false);
        log("Servicestate wait disconnect, acquire wakelock");
        this.mWakeLock.acquire();
    }

    /* access modifiers changed from: protected */
    public void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            log("release wakelock");
        }
    }

    /* access modifiers changed from: protected */
    public void judgeToLaunchCsgPeriodicSearchTimer() {
        if (this.mHwCustGsmServiceStateTracker != null) {
            this.mHwCustGsmServiceStateTracker.judgeToLaunchCsgPeriodicSearchTimer();
            log("mHwCustGsmServiceStateTracker is not null");
        }
    }

    public HwCustGsmServiceStateTracker returnObject() {
        return this.mHwCustGsmServiceStateTracker;
    }

    private boolean isOperatorChanged(boolean showPlmn, boolean showSpn, String spn, String dataSpn, String plmn, boolean showWifi, String wifi, String regplmn) {
        boolean isRealChange = IS_HISI_PLATFORM || !TextUtils.isEmpty(this.mSS.getOperatorNumeric()) || HwTelephonyFactory.getHwNetworkManager().getGsmCombinedRegState(this, this.mPhone, this.mSS) != 0;
        if ((showPlmn != this.mCurShowPlmn || showSpn != this.mCurShowSpn || !TextUtils.equals(spn, this.mCurSpn) || !TextUtils.equals(dataSpn, this.mCurDataSpn) || !TextUtils.equals(plmn, this.mCurPlmn) || showWifi != this.mCurShowWifi || !TextUtils.equals(wifi, this.mCurWifi) || !TextUtils.equals(regplmn, this.mCurRegplmn)) && isRealChange) {
            return true;
        }
        return false;
    }

    private void getOperator() {
        if (!IS_HISI_PLATFORM && HwTelephonyFactory.getHwNetworkManager().getGsmCombinedRegState(this, this.mPhone, this.mSS) == 0 && TextUtils.isEmpty(this.mSS.getOperatorNumeric()) && !this.mRplmnIsNull) {
            this.mCi.getOperator(obtainMessage(6, this.mPollingContext));
            this.mRplmnIsNull = true;
        }
    }

    private void upatePlmn(String brandOverride, String opNames0, String opNames1, String rplmn) {
        if (this.mRplmnIsNull && !TextUtils.isEmpty(rplmn) && IS_QCOM_PLATFORM) {
            if (brandOverride != null) {
                this.mSS.setOperatorName(brandOverride, brandOverride, rplmn);
            } else {
                this.mSS.setOperatorName(opNames0, opNames1, rplmn);
            }
            this.mRplmnIsNull = false;
            updateSpnDisplay();
        }
    }

    /* access modifiers changed from: protected */
    public int getCombinedRegState() {
        int regState = this.mSS.getVoiceRegState();
        int dataRegState = this.mSS.getDataRegState();
        if ((regState != 1 && regState != 3) || dataRegState != 0) {
            return regState;
        }
        log("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    private PersistableBundle getCarrierConfig() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle config = configManager.getConfigForSubId(this.mPhone.getSubId());
            if (config != null) {
                return config;
            }
        }
        return CarrierConfigManager.getDefaultConfig();
    }

    public LocaleTracker getLocaleTracker() {
        return this.mLocaleTracker;
    }

    public NitzStateMachine getNitzState() {
        return this.mNitzState;
    }

    /* access modifiers changed from: protected */
    public SparseArray<NetworkRegistrationManager> getRegStateManagers() {
        return this.mRegStateManagers;
    }

    public CellLocation getCellLocationInfo() {
        return this.mCellLoc;
    }

    private void processCtVolteCellLocationInfo(CellLocation cellLoc, CellIdentity cellIdentity) {
        if (cellIdentity != null && cellIdentity.getType() == 3) {
            int cid = ((CellIdentityLte) cellIdentity).getCi();
            int lac = ((CellIdentityLte) cellIdentity).getTac();
            if (this.mPhone.isPhoneTypeGsm()) {
                ((GsmCellLocation) cellLoc).setLacAndCid(lac, cid);
                this.hasUpdateCellLocByPS = true;
                return;
            }
            ((CdmaCellLocation) cellLoc).setLacAndCid(lac, cid);
        }
    }
}
