package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
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
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
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
import android.telephony.CellLocation;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.PhysicalChannelConfig;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoiceSpecificRegistrationInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StatsLog;
import android.util.TimestampedValue;
import android.view.Display;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.cdnr.CarrierDisplayNameData;
import com.android.internal.telephony.cdnr.CarrierDisplayNameResolver;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.huawei.internal.telephony.NetworkRegistrationInfoEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ServiceStateTracker extends Handler implements IServiceStateTrackerInner {
    private static final String ACTION_COMMFORCE = "huawei.intent.action.COMMFORCE";
    private static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    private static final int AIR_MODE_OFF = 0;
    public static final int CARRIER_NAME_DISPLAY_BITMASK_SHOW_PLMN = 2;
    public static final int CARRIER_NAME_DISPLAY_BITMASK_SHOW_SPN = 1;
    private static final long CELL_INFO_LIST_QUERY_TIMEOUT = 2000;
    private static final boolean CLEAR_NITZ_WHEN_REG = SystemProperties.getBoolean("ro.config.clear_nitz_when_reg", true);
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    public static final int CS_REJECT_CAUSE_DISABLED = 2002;
    public static final int CS_REJECT_CAUSE_ENABLED = 2001;
    public static final int CS_REJECT_CAUSE_NOTIFICATION = 111;
    static final boolean DBG = true;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    protected static final int DELAYED_ECC_TO_NOSERVICE_VALUE = SystemProperties.getInt("ro.ecc_to_noservice.timer", 0);
    private static final boolean ENABLE_DEMO = SystemProperties.getBoolean("ro.config.enable_demo", false);
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CARRIER_CONFIG_CHANGED = 57;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CELL_LOCATION_RESPONSE = 56;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
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
    protected static final int EVENT_POLL_STATE_CS_CELLULAR_REGISTRATION = 4;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 7;
    protected static final int EVENT_POLL_STATE_PS_CELLULAR_REGISTRATION = 5;
    protected static final int EVENT_POLL_STATE_PS_IWLAN_REGISTRATION = 6;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_POWER_FROM_CARRIER = 51;
    protected static final int EVENT_RADIO_POWER_OFF = 58;
    protected static final int EVENT_RADIO_POWER_OFF_DONE = 54;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
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
    private static final int INVALID_CONFIG = -1;
    private static final int INVALID_LTE_EARFCN = -1;
    public static final String INVALID_MCC = "000";
    public static final boolean ISDEMO = ((HW_OPTA == 735 && HW_OPTB == 156) || ENABLE_DEMO);
    private static final boolean IS_HISI_PLATFORM = HuaweiTelephonyConfigs.isHisiPlatform();
    private static final boolean IS_MTK_PLATFORM = HuaweiTelephonyConfigs.isMTKPlatform();
    private static final boolean IS_QCOM_PLATFORM = HuaweiTelephonyConfigs.isQcomPlatform();
    private static final int LOCAL_LOG_SIZE = 10;
    private static final boolean MDOEM_WORK_MODE_IS_SRLTE = SystemProperties.getBoolean("ro.config.hw_srlte", false);
    private static final int MS_PER_HOUR = 3600000;
    protected static final int NOT_REGISTERED_ON_CDMA_SYSTEM = -1;
    protected static final int NSA_INVALID_STATE = 0;
    public static final int NSA_STATE1 = 1;
    public static final int NSA_STATE2 = 2;
    public static final int NSA_STATE5 = 5;
    private static final String PERMISSION_COMM_FORCE = "android.permission.COMM_FORCE";
    private static boolean PLUS_TRANFER_IN_MDOEM = HwModemCapability.isCapabilitySupport(2);
    private static final int POLL_PERIOD_MILLIS = 20000;
    private static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
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
    private static final boolean VDBG = false;
    protected static final boolean display_blank_ons = "true".equals(SystemProperties.get("ro.config.hw_no_display_ons", "false"));
    private String LOG_TAG = "ServiceStateTracker";
    private boolean hasUpdateCellLocByPS = false;
    protected boolean isCurrent3GPsCsAllowed = true;
    private boolean mAlarmSwitch = false;
    private final LocalLog mAttachLog = new LocalLog(10);
    protected SparseArray<RegistrantList> mAttachedRegistrants = new SparseArray<>();
    private CarrierServiceStateTracker mCSST;
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
    private CdmaSubscriptionSourceManager mCdmaSSM;
    private CarrierDisplayNameResolver mCdnr;
    private final LocalLog mCdnrLogs = new LocalLog(10);
    private CellIdentity mCellIdentity;
    private int mCellInfoMinIntervalMs = TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_REASON_UNSPECIFIED;
    CommandsInterface mCi;
    @UnsupportedAppUsage
    private final ContentResolver mCr;
    @UnsupportedAppUsage
    private String mCurDataSpn = null;
    @UnsupportedAppUsage
    private String mCurPlmn = null;
    private String mCurRegplmn = null;
    @UnsupportedAppUsage
    private boolean mCurShowPlmn = false;
    @UnsupportedAppUsage
    private boolean mCurShowSpn = false;
    private boolean mCurShowWifi = false;
    @UnsupportedAppUsage
    private String mCurSpn = null;
    private String mCurWifi = null;
    private String mCurrentCarrier = null;
    private int mCurrentOtaspMode = 0;
    private SparseArray<RegistrantList> mDataRegStateOrRatChangedRegistrants = new SparseArray<>();
    private boolean mDataRoaming = false;
    @UnsupportedAppUsage
    private RegistrantList mDataRoamingOffRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    private RegistrantList mDataRoamingOnRegistrants = new RegistrantList();
    private Display mDefaultDisplay;
    private int mDefaultDisplayState = 0;
    @UnsupportedAppUsage
    private int mDefaultRoamingIndicator;
    @UnsupportedAppUsage
    private boolean mDesiredPowerState;
    protected SparseArray<RegistrantList> mDetachedRegistrants = new SparseArray<>();
    @UnsupportedAppUsage
    private boolean mDeviceShuttingDown = false;
    private final DisplayManager.DisplayListener mDisplayListener = new SstDisplayListener();
    private boolean mDontPollSignalStrength = false;
    private ArrayList<Pair<Integer, Integer>> mEarfcnPairListForRsrpBoost = null;
    @UnsupportedAppUsage
    private boolean mEmergencyOnly = false;
    private final EriManager mEriManager;
    private boolean mGsmRoaming = false;
    private HbpcdUtils mHbpcdUtils = null;
    private int[] mHomeNetworkId = null;
    private int[] mHomeSystemId = null;
    private HwCustGsmServiceStateTracker mHwCustGsmServiceStateTracker;
    private IHwServiceStateTrackerEx mHwServiceStateTrackerEx;
    @UnsupportedAppUsage
    private IccRecords mIccRecords = null;
    private RegistrantList mImsCapabilityChangedRegistrants = new RegistrantList();
    private boolean mImsRegistered = false;
    private boolean mImsRegistrationOnOff = false;
    @UnsupportedAppUsage
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.ServiceStateTracker.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals("android.intent.action.AIRPLANE_MODE")) {
                    ServiceStateTracker serviceStateTracker = ServiceStateTracker.this;
                    serviceStateTracker.loge("intent: " + intent.getAction());
                    ServiceStateTracker.this.mCurPlmn = null;
                    ServiceStateTracker.this.updateSpnDisplay();
                    if (ServiceStateTracker.this.mHwCustGsmServiceStateTracker != null && intent.getBooleanExtra("state", false)) {
                        ServiceStateTracker.this.mHwCustGsmServiceStateTracker.clearPcoValue(ServiceStateTracker.this.mPhone);
                        return;
                    }
                    return;
                }
                if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction()) && intent.getExtras() != null && intent.getExtras().getInt("android.telephony.extra.SLOT_INDEX") == ServiceStateTracker.this.mPhone.getPhoneId()) {
                    ServiceStateTracker.this.sendEmptyMessage(57);
                }
                if (!ServiceStateTracker.this.mPhone.isPhoneTypeGsm()) {
                    ServiceStateTracker serviceStateTracker2 = ServiceStateTracker.this;
                    serviceStateTracker2.loge("Ignoring intent " + intent + " received on CDMA phone");
                } else if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                    ServiceStateTracker.this.updateSpnDisplay();
                } else if (ServiceStateTracker.ACTION_RADIO_OFF.equals(intent.getAction())) {
                    ServiceStateTracker.this.mAlarmSwitch = false;
                    ServiceStateTracker.this.powerOffRadioSafely();
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction()) || "android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) || ServiceStateTracker.ACTION_COMMFORCE.equals(intent.getAction()) || "android.intent.action.HEADSET_PLUG".equals(intent.getAction()) || "android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                    ServiceStateTracker.this.mPhone.updateReduceSARState();
                }
            }
        }
    };
    private boolean mIsEriTextLoaded = false;
    private boolean mIsInPrl;
    private boolean mIsMinInfoReady = false;
    private boolean mIsPendingCellInfoRequest = false;
    private boolean mIsSimReady = false;
    @UnsupportedAppUsage
    private boolean mIsSubscriptionFromRuim = false;
    private List<CellInfo> mLastCellInfoList = null;
    private long mLastCellInfoReqTime;
    private List<PhysicalChannelConfig> mLastPhysicalChannelConfigList = null;
    long mLastReceivedNITZReferenceTime;
    private SignalStrength mLastSignalStrength = null;
    private final LocaleTracker mLocaleTracker;
    private int mLteRsrpBoost = 0;
    private final Object mLteRsrpBoostLock = new Object();
    @UnsupportedAppUsage
    private int mMaxDataCalls = 1;
    private String mMdn;
    private String mMin;
    protected String mMlplVersion;
    protected String mMsplVersion;
    private boolean mNeedRetryNotifySs = true;
    @UnsupportedAppUsage
    private RegistrantList mNetworkAttachedRegistrants = new RegistrantList();
    private RegistrantList mNetworkDetachedRegistrants = new RegistrantList();
    private CellIdentity mNewCellIdentity;
    @UnsupportedAppUsage
    private int mNewMaxDataCalls = 1;
    private int mNewNsaState = 0;
    @UnsupportedAppUsage
    private int mNewReasonDataDenied = -1;
    private int mNewRejectCode;
    @UnsupportedAppUsage
    private ServiceState mNewSS;
    private final NitzStateMachine mNitzState;
    private Notification mNotification;
    private int mOldCsRegState = 1;
    @UnsupportedAppUsage
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SstSubscriptionsChangedListener();
    private Pattern mOperatorNameStringPattern;
    private List<Message> mPendingCellInfoRequests = new LinkedList();
    private boolean mPendingRadioPowerOffAfterDataOff = false;
    private int mPendingRadioPowerOffAfterDataOffTag = 0;
    @UnsupportedAppUsage
    private final GsmCdmaPhone mPhone;
    private final LocalLog mPhoneTypeLog = new LocalLog(10);
    @VisibleForTesting
    public int[] mPollingContext;
    private boolean mPowerOffDelayNeed = true;
    private int mPreNsaState = 0;
    private boolean mPreVowifiState = false;
    @UnsupportedAppUsage
    private int mPreferredNetworkType;
    private int mPrevSubId = -1;
    private String mPrlVersion;
    private RegistrantList mPsRestrictDisabledRegistrants = new RegistrantList();
    private RegistrantList mPsRestrictEnabledRegistrants = new RegistrantList();
    private boolean mRadioDisabledByCarrier = false;
    private boolean mRadioOffByDoRecovery = false;
    private PendingIntent mRadioOffIntent = null;
    private final LocalLog mRadioPowerLog = new LocalLog(10);
    private final LocalLog mRatLog = new LocalLog(10);
    private final RatRatcheter mRatRatcheter;
    @UnsupportedAppUsage
    private int mReasonDataDenied = -1;
    private boolean mRecoverAutoSelectMode = false;
    private final SparseArray<NetworkRegistrationManager> mRegStateManagers = new SparseArray<>();
    private String mRegistrationDeniedReason;
    private int mRegistrationState = -1;
    private int mRejectCode;
    @UnsupportedAppUsage
    private boolean mReportedGprsNoReg;
    public RestrictedState mRestrictedState;
    @UnsupportedAppUsage
    private int mRoamingIndicator;
    private final LocalLog mRoamingLog = new LocalLog(10);
    private boolean mRplmnIsNull = false;
    @UnsupportedAppUsage
    public ServiceState mSS;
    private boolean mShowEmergencyOnly = false;
    @UnsupportedAppUsage
    private SignalStrength mSignalStrength;
    private boolean mSimCardsLoaded = false;
    @UnsupportedAppUsage
    private boolean mSpnUpdatePending = false;
    @UnsupportedAppUsage
    private boolean mStartedGprsRegCheck;
    @UnsupportedAppUsage
    @VisibleForTesting
    public int mSubId = -1;
    @UnsupportedAppUsage
    private SubscriptionController mSubscriptionController;
    @UnsupportedAppUsage
    private SubscriptionManager mSubscriptionManager;
    private final TransportManager mTransportManager;
    @UnsupportedAppUsage
    private UiccCardApplication mUiccApplcation = null;
    @UnsupportedAppUsage
    private UiccController mUiccController = null;
    private boolean mVoiceCapable;
    private RegistrantList mVoiceRegStateOrRatChangedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    private RegistrantList mVoiceRoamingOffRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    private RegistrantList mVoiceRoamingOnRegistrants = new RegistrantList();
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CarrierNameDisplayBitmask {
    }

    private class CellInfoResult {
        List<CellInfo> list;
        Object lockObj = new Object();

        private CellInfoResult() {
        }
    }

    public class SstDisplayListener implements DisplayManager.DisplayListener {
        public SstDisplayListener() {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                int oldState = ServiceStateTracker.this.mDefaultDisplayState;
                ServiceStateTracker serviceStateTracker = ServiceStateTracker.this;
                serviceStateTracker.mDefaultDisplayState = serviceStateTracker.mDefaultDisplay.getState();
                if (ServiceStateTracker.this.mDefaultDisplayState != oldState && ServiceStateTracker.this.mDefaultDisplayState == 2 && !ServiceStateTracker.this.mDontPollSignalStrength) {
                    ServiceStateTracker serviceStateTracker2 = ServiceStateTracker.this;
                    serviceStateTracker2.sendMessage(serviceStateTracker2.obtainMessage(10));
                }
            }
        }
    }

    private class SstSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        private SstSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            ServiceStateTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = ServiceStateTracker.this.mPhone.getSubId();
            ServiceStateTracker.this.mPrevSubId = this.mPreviousSubId.get();
            if (this.mPreviousSubId.getAndSet(subId) != subId) {
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    Context context = ServiceStateTracker.this.mPhone.getContext();
                    ServiceStateTracker.this.mPhone.notifyPhoneStateChanged();
                    ServiceStateTracker.this.mPhone.notifyCallForwardingIndicator();
                    ServiceStateTracker.this.retryNotifyServiceState(subId);
                    ServiceStateTracker.this.mPhone.sendSubscriptionSettings(!context.getResources().getBoolean(17891621));
                    ServiceStateTracker.this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(ServiceStateTracker.this.mSS.getRilDataRadioTechnology()));
                    if (ServiceStateTracker.this.mSpnUpdatePending) {
                        ServiceStateTracker.this.mSubscriptionController.setPlmnSpn(ServiceStateTracker.this.mPhone.getPhoneId(), ServiceStateTracker.this.mCurShowPlmn, ServiceStateTracker.this.mCurPlmn, ServiceStateTracker.this.mCurShowSpn, ServiceStateTracker.this.mCurSpn);
                        ServiceStateTracker.this.mSpnUpdatePending = false;
                    }
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, PhoneConfigurationManager.SSSS);
                    String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, PhoneConfigurationManager.SSSS);
                    String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, PhoneConfigurationManager.SSSS);
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
                ServiceStateTracker.this.mPhone.updateVoiceMail();
            }
        }
    }

    public ServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        this.mNitzState = TelephonyComponentFactory.getInstance().inject(NitzStateMachine.class.getName()).makeNitzStateMachine(phone);
        this.mPhone = phone;
        this.LOG_TAG += "[SUB" + this.mPhone.getPhoneId() + "]";
        this.mCi = ci;
        this.mCdnr = new CarrierDisplayNameResolver(this.mPhone);
        this.mEriManager = TelephonyComponentFactory.getInstance().inject(EriManager.class.getName()).makeEriManager(this.mPhone, 0);
        this.mRatRatcheter = new RatRatcheter(this.mPhone);
        this.mVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17891573);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 42, null);
        this.mCi.setOnSignalStrengthUpdate(this, 12, null);
        this.mCi.registerForCellInfoList(this, 44, null);
        this.mCi.registerForPhysicalChannelConfiguration(this, 55, null);
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mSubscriptionManager = SubscriptionManager.from(phone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mRestrictedState = new RestrictedState();
        this.mTransportManager = this.mPhone.getTransportManager();
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        for (int transportType : availableTransports) {
            this.mRegStateManagers.append(transportType, new NetworkRegistrationManager(transportType, phone));
            this.mRegStateManagers.get(transportType).registerForNetworkRegistrationInfoChanged(this, 2, null);
        }
        this.mLocaleTracker = TelephonyComponentFactory.getInstance().inject(LocaleTracker.class.getName()).makeLocaleTracker(this.mPhone, this.mNitzState, getLooper());
        this.mCi.registerForImsNetworkStateChanged(this, 46, null);
        this.mCi.registerForRadioStateChanged(this, 1, null);
        this.mCi.setOnNITZTime(this, 11, null);
        this.mCr = phone.getContext().getContentResolver();
        int airplaneMode = Settings.Global.getInt(this.mCr, "airplane_mode_on", 0);
        int enableCellularOnBoot = Settings.Global.getInt(this.mCr, "enable_cellular_on_boot", 1);
        this.mDesiredPowerState = enableCellularOnBoot > 0 && airplaneMode <= 0;
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
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null && (hwCustGsmServiceStateTracker.isDataOffForbidLTE() || this.mHwCustGsmServiceStateTracker.isDataOffbyRoamAndData())) {
            this.mHwCustGsmServiceStateTracker.initOnce(this.mPhone, this.mCi);
        }
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter4);
        this.mPhone.notifyOtaspChanged(0);
        this.mCi.setOnRestrictedStateChanged(this, 23, null);
        updatePhoneType();
        this.mCi.registerForOffOrNotAvailable(this, 58, null);
        this.mCSST = new CarrierServiceStateTracker(phone, this);
        registerForNetworkAttached(this.mCSST, 101, null);
        registerForNetworkDetached(this.mCSST, CallFailCause.RECOVERY_ON_TIMER_EXPIRY, null);
        registerForDataConnectionAttached(1, this.mCSST, 103, null);
        registerForDataConnectionDetached(1, this.mCSST, 104, null);
        registerForImsCapabilityChanged(this.mCSST, 105, null);
        initDisplay();
    }

    private void initDisplay() {
        DisplayManager dm = (DisplayManager) this.mPhone.getContext().getSystemService("display");
        dm.registerDisplayListener(this.mDisplayListener, null);
        this.mDefaultDisplay = dm.getDisplay(0);
    }

    @VisibleForTesting
    public void updatePhoneType() {
        NetworkRegistrationInfo nrs;
        ServiceState serviceState = this.mSS;
        if (serviceState != null && serviceState.getVoiceRoaming()) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        ServiceState serviceState2 = this.mSS;
        if (serviceState2 != null && serviceState2.getDataRoaming()) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        ServiceState serviceState3 = this.mSS;
        if (serviceState3 != null && serviceState3.getVoiceRegState() == 0) {
            this.mNetworkDetachedRegistrants.notifyRegistrants();
        }
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        for (int transport : availableTransports) {
            ServiceState serviceState4 = this.mSS;
            if (!(serviceState4 == null || (nrs = serviceState4.getNetworkRegistrationInfoHw(2, transport)) == null || !nrs.isInService() || this.mDetachedRegistrants.get(transport) == null)) {
                this.mDetachedRegistrants.get(transport).notifyRegistrants();
            }
        }
        this.mSS = new ServiceState();
        this.mSS.setStateOutOfService();
        this.mNewSS = new ServiceState();
        this.mNewSS.setStateOutOfService();
        this.mLastCellInfoReqTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
        this.mStartedGprsRegCheck = false;
        this.mReportedGprsNoReg = false;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = false;
        this.mNitzState.handleNetworkCountryCodeUnavailable();
        this.mCellIdentity = null;
        this.mNewCellIdentity = null;
        this.mPreNsaState = 0;
        if (this.mHwServiceStateTrackerEx == null) {
            PhoneExt phoneExt = new PhoneExt();
            phoneExt.setPhone(this.mPhone);
            this.mHwServiceStateTrackerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwServiceStateTrackerEx(this, phoneExt);
        }
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            CdmaSubscriptionSourceManager cdmaSubscriptionSourceManager = this.mCdmaSSM;
            if (cdmaSubscriptionSourceManager != null) {
                cdmaSubscriptionSourceManager.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
        } else {
            if (!IS_HISI_PLATFORM) {
                this.mCurPlmn = null;
            }
            this.mPhone.registerForSimRecordsLoaded(this, 16, null);
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0;
            this.mCi.registerForCdmaPrlChanged(this, 40, null);
            this.mCi.registerForCdmaOtaProvision(this, 37, null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(0));
        this.mCi.getSignalStrength(obtainMessage(3));
        sendMessage(obtainMessage(50));
        logPhoneTypeChange();
        notifyVoiceRegStateRilRadioTechnologyChanged();
        for (int transport2 : this.mTransportManager.getAvailableTransports()) {
            notifyDataRegStateRilRadioTechnologyChanged(transport2);
        }
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
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 1);
        UiccCardApplication uiccCardApplication = this.mUiccApplcation;
        if (uiccCardApplication != null) {
            uiccCardApplication.unregisterForGetAdDone(this);
        }
        unregisterForRuimEvents();
        this.mHwServiceStateTrackerEx.dispose();
        if (VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId())) {
            VSimUtilsInner.disposeSSTForVSim();
        }
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null && (hwCustGsmServiceStateTracker.isDataOffForbidLTE() || this.mHwCustGsmServiceStateTracker.isDataOffbyRoamAndData())) {
            this.mHwCustGsmServiceStateTracker.dispose(this.mPhone);
        }
        CarrierServiceStateTracker carrierServiceStateTracker = this.mCSST;
        if (carrierServiceStateTracker != null) {
            carrierServiceStateTracker.dispose();
            this.mCSST = null;
        }
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    @UnsupportedAppUsage
    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public boolean getPowerStateFromCarrier() {
        return !this.mRadioDisabledByCarrier;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public long getLastCellInfoReqTime() {
        return this.mLastCellInfoReqTime;
    }

    public void setDesiredPowerState(boolean dps) {
        log("setDesiredPowerState, dps = " + dps);
        this.mDesiredPowerState = dps;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean notifySignalStrength() {
        GsmCdmaPhone gsmCdmaPhone;
        if (this.mSignalStrength.equals(this.mLastSignalStrength) || (gsmCdmaPhone = this.mPhone) == null) {
            return false;
        }
        gsmCdmaPhone.notifySignalStrength();
        this.mLastSignalStrength = this.mSignalStrength;
        return true;
    }

    /* access modifiers changed from: protected */
    public void notifyVoiceRegStateRilRadioTechnologyChanged() {
        int rat = this.mSS.getRilVoiceRadioTechnology();
        int vrs = this.mSS.getVoiceRegState();
        log("notifyVoiceRegStateRilRadioTechnologyChanged: vrs=" + vrs + " rat=" + rat);
        this.mVoiceRegStateOrRatChangedRegistrants.notifyResult(new Pair(Integer.valueOf(vrs), Integer.valueOf(rat)));
    }

    /* access modifiers changed from: protected */
    public void notifyDataRegStateRilRadioTechnologyChanged(int transport) {
        NetworkRegistrationInfo nrs = this.mSS.getNetworkRegistrationInfoHw(2, transport);
        if (nrs != null) {
            int rat = ServiceState.networkTypeToRilRadioTechnology(nrs.getAccessNetworkTechnology());
            int drs = regCodeToServiceState(nrs.getRegistrationState());
            log("notifyDataRegStateRilRadioTechnologyChanged: drs=" + drs + " rat=" + rat);
            RegistrantList registrantList = this.mDataRegStateOrRatChangedRegistrants.get(transport);
            if (registrantList != null) {
                registrantList.notifyResult(new Pair(Integer.valueOf(drs), Integer.valueOf(rat)));
            }
        }
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(this.mSS.getRilDataRadioTechnology()));
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void useDataRegStateForDataOnlyDevices() {
        if (!this.mVoiceCapable) {
            log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            ServiceState serviceState = this.mNewSS;
            serviceState.setVoiceRegState(serviceState.getDataRegState());
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void updatePhoneObject() {
        if (this.mPhone.getContext().getResources().getBoolean(17891550)) {
            if (!(this.mSS.getVoiceRegState() == 0 || this.mSS.getVoiceRegState() == 2)) {
                log("updatePhoneObject: Ignore update");
            } else {
                this.mPhone.updatePhoneObject(this.mSS.getRilVoiceRadioTechnology());
            }
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

    @UnsupportedAppUsage
    public void reRegisterNetwork(Message onComplete) {
        if (!HwModemCapability.isCapabilitySupport(7) || (!this.mPhone.isPhoneTypeGsm() && !isLteOrNrTechnology(this.mSS.getRilDataRadioTechnology()))) {
            log("modem not support rettach, reRegisterNetwork");
            this.mCi.getPreferredNetworkType(obtainMessage(19, onComplete));
            return;
        }
        log("modem support rettach, rettach");
        this.mCi.dataConnectionDetach(isLteOrNrTechnology(this.mSS.getRilDataRadioTechnology()) ? 1 : 0, null);
        this.mCi.dataConnectionAttach(isLteOrNrTechnology(this.mSS.getRilDataRadioTechnology()) ? 1 : 0, null);
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
        if ((!this.mHwServiceStateTrackerEx.isCustScreenOff() || this.mHwServiceStateTrackerEx.isAllowLocationUpdate(Binder.getCallingPid())) && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantSingleLocationUpdate = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    public void enableLocationUpdates() {
        if (!this.mHwServiceStateTrackerEx.isCustScreenOff() && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
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

    private int getLteEarfcn(CellIdentity cellIdentity) {
        if (cellIdentity == null || cellIdentity.getType() != 3) {
            return -1;
        }
        return ((CellIdentityLte) cellIdentity).getEarfcn();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        ServiceState serviceState;
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker == null || !hwCustGsmServiceStateTracker.handleMessage(msg)) {
            int i = msg.what;
            boolean z = true;
            if (i == 26) {
                if (this.mPhone.getLteOnCdmaMode() == 1) {
                    log("Receive EVENT_RUIM_READY");
                    pollState();
                } else {
                    log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                    getSubscriptionInfoAndStartPollingThreads();
                }
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
            } else if (i != 27) {
                if (i != 34) {
                    if (i == 35) {
                        updatePhoneObject();
                        this.mCi.getNetworkSelectionMode(obtainMessage(14));
                        getSubscriptionInfoAndStartPollingThreads();
                    } else if (i != 1002) {
                        switch (i) {
                            case 1:
                                break;
                            case 2:
                                modemTriggeredPollState();
                                return;
                            case 3:
                                if (this.mCi.getRadioState() == 1) {
                                    onSignalStrengthResult((AsyncResult) msg.obj);
                                    queueNextSignalStrengthPoll();
                                    return;
                                }
                                return;
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                                handlePollStateResult(msg.what, (AsyncResult) msg.obj);
                                return;
                            default:
                                switch (i) {
                                    case 10:
                                        this.mCi.getSignalStrength(obtainMessage(3));
                                        return;
                                    case 11:
                                        if (this.mHwServiceStateTrackerEx.needUpdateNITZTime()) {
                                            AsyncResult ar = (AsyncResult) msg.obj;
                                            long nitzReceiveTime = ((Long) ((Object[]) ar.result)[1]).longValue();
                                            this.mLastReceivedNITZReferenceTime = nitzReceiveTime;
                                            setTimeFromNITZString((String) ((Object[]) ar.result)[0], nitzReceiveTime);
                                            return;
                                        }
                                        return;
                                    case 12:
                                        this.mDontPollSignalStrength = true;
                                        onSignalStrengthResult((AsyncResult) msg.obj);
                                        return;
                                    default:
                                        switch (i) {
                                            case 14:
                                                log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                                                AsyncResult ar2 = (AsyncResult) msg.obj;
                                                if (this.mPhone.isPhoneTypeGsm()) {
                                                    handlePollStateResult(msg.what, ar2);
                                                    return;
                                                } else if (ar2.exception != null || ar2.result == null) {
                                                    log("Unable to getNetworkSelectionMode");
                                                    return;
                                                } else if (((int[]) ar2.result)[0] == 1) {
                                                    this.mPhone.setNetworkSelectionModeAutomatic(null);
                                                    return;
                                                } else {
                                                    return;
                                                }
                                            case 15:
                                                AsyncResult ar3 = (AsyncResult) msg.obj;
                                                if (ar3.exception == null && (ar3.result instanceof NetworkRegistrationInfo)) {
                                                    CellIdentity cellIdentity = ((NetworkRegistrationInfo) ar3.result).getCellIdentity();
                                                    updateOperatorNameForCellIdentity(cellIdentity);
                                                    this.mCellIdentity = cellIdentity;
                                                    this.mPhone.notifyLocationChanged(getCellLocation());
                                                }
                                                disableSingleLocationUpdate();
                                                return;
                                            case 16:
                                                log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                                                this.mSimCardsLoaded = true;
                                                updatePhoneObject();
                                                updateOtaspState();
                                                if (this.mPhone.isPhoneTypeGsm()) {
                                                    this.mCdnr.updateEfFromUsim((SIMRecords) this.mIccRecords);
                                                    updateSpnDisplay();
                                                    HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker2 = this.mHwCustGsmServiceStateTracker;
                                                    if (hwCustGsmServiceStateTracker2 != null) {
                                                        hwCustGsmServiceStateTracker2.updateRomingVoicemailNumber(this.mSS);
                                                        return;
                                                    }
                                                    return;
                                                }
                                                return;
                                            case 17:
                                                this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
                                                this.mPrevSubId = -1;
                                                this.mIsSimReady = true;
                                                this.mSimCardsLoaded = false;
                                                log("skip setPreferredNetworkType when EVENT_SIM_READY");
                                                this.mRecoverAutoSelectMode = this.mHwServiceStateTrackerEx.recoverAutoSelectMode(this.mRecoverAutoSelectMode);
                                                pollState();
                                                queueNextSignalStrengthPoll();
                                                return;
                                            case 18:
                                                if (((AsyncResult) msg.obj).exception == null) {
                                                    this.mRegStateManagers.get(1).requestNetworkRegistrationInfo(1, obtainMessage(15, null));
                                                    return;
                                                }
                                                return;
                                            case 19:
                                                AsyncResult ar4 = (AsyncResult) msg.obj;
                                                if (ar4.exception == null) {
                                                    this.mPreferredNetworkType = ((int[]) ar4.result)[0];
                                                } else {
                                                    this.mPreferredNetworkType = Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "preferred_network_mode" + this.mPhone.getPhoneId(), 7);
                                                }
                                                this.mCi.setPreferredNetworkType(7, obtainMessage(20, ar4.userObj));
                                                return;
                                            case 20:
                                                this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(21, ((AsyncResult) msg.obj).userObj));
                                                return;
                                            case 21:
                                                AsyncResult ar5 = (AsyncResult) msg.obj;
                                                if (ar5.userObj != null) {
                                                    AsyncResult.forMessage((Message) ar5.userObj).exception = ar5.exception;
                                                    ((Message) ar5.userObj).sendToTarget();
                                                }
                                                IHwServiceStateTrackerEx iHwServiceStateTrackerEx = this.mHwServiceStateTrackerEx;
                                                if (ar5.exception == null) {
                                                    z = false;
                                                }
                                                iHwServiceStateTrackerEx.setReregisteredResultFlag(z);
                                                return;
                                            case 22:
                                                if (this.mPhone.isPhoneTypeGsm() && (serviceState = this.mSS) != null && !isGprsConsistent(serviceState.getDataRegState(), this.mSS.getVoiceRegState())) {
                                                    EventLog.writeEvent((int) EventLogTags.DATA_NETWORK_REGISTRATION_FAIL, this.mSS.getOperatorNumeric(), Integer.valueOf(getCidFromCellIdentity(this.mCellIdentity)));
                                                    this.mReportedGprsNoReg = true;
                                                }
                                                this.mStartedGprsRegCheck = false;
                                                return;
                                            case 23:
                                                if (this.mPhone.isPhoneTypeGsm()) {
                                                    log("EVENT_RESTRICTED_STATE_CHANGED");
                                                    onRestrictedStateChanged((AsyncResult) msg.obj);
                                                    return;
                                                }
                                                return;
                                            default:
                                                switch (i) {
                                                    case 37:
                                                        AsyncResult ar6 = (AsyncResult) msg.obj;
                                                        if (ar6.exception == null) {
                                                            int otaStatus = ((int[]) ar6.result)[0];
                                                            if (otaStatus == 8 || otaStatus == 10) {
                                                                log("EVENT_OTA_PROVISION_STATUS_CHANGE: Complete, Reload MDN");
                                                                this.mCi.getCDMASubscription(obtainMessage(34));
                                                                return;
                                                            }
                                                            return;
                                                        }
                                                        return;
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
                                                        return;
                                                    case 39:
                                                        handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                                                        return;
                                                    case 40:
                                                        AsyncResult ar7 = (AsyncResult) msg.obj;
                                                        if (ar7.exception == null) {
                                                            this.mPrlVersion = Integer.toString(((int[]) ar7.result)[0]);
                                                            SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                                                            return;
                                                        }
                                                        return;
                                                    default:
                                                        switch (i) {
                                                            case 42:
                                                                if (isSimAbsent()) {
                                                                    log("EVENT_ICC_CHANGED: SIM absent");
                                                                    cancelAllNotifications();
                                                                    this.mMdn = null;
                                                                    this.mMin = null;
                                                                    this.mIsMinInfoReady = false;
                                                                    this.mCdnr.updateEfFromRuim(null);
                                                                    this.mCdnr.updateEfFromUsim(null);
                                                                }
                                                                onUpdateIccAvailability();
                                                                UiccCardApplication uiccCardApplication = this.mUiccApplcation;
                                                                if (!(uiccCardApplication == null || uiccCardApplication.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY)) {
                                                                    this.mIsSimReady = false;
                                                                    updateSpnDisplay();
                                                                    return;
                                                                }
                                                                return;
                                                            case 43:
                                                            case 44:
                                                                List<CellInfo> cellInfo = null;
                                                                Throwable ex = null;
                                                                if (msg.obj != null) {
                                                                    AsyncResult ar8 = (AsyncResult) msg.obj;
                                                                    if (ar8.exception != null) {
                                                                        log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar8.exception);
                                                                        ex = ar8.exception;
                                                                    } else if (ar8.result == null) {
                                                                        loge("Invalid CellInfo result");
                                                                    } else if (!(ar8.result instanceof List)) {
                                                                        loge("EVENT_GET_CELL_INFO_LIST error result:" + ar8.result);
                                                                        return;
                                                                    } else {
                                                                        cellInfo = (List) ar8.result;
                                                                        updateOperatorNameForCellInfo(cellInfo);
                                                                        this.mLastCellInfoList = cellInfo;
                                                                        this.mPhone.notifyCellInfo(cellInfo);
                                                                    }
                                                                } else if (this.mIsPendingCellInfoRequest && SystemClock.elapsedRealtime() - this.mLastCellInfoReqTime >= CELL_INFO_LIST_QUERY_TIMEOUT) {
                                                                    loge("Timeout waiting for CellInfo; (everybody panic)!");
                                                                    this.mLastCellInfoList = null;
                                                                } else {
                                                                    return;
                                                                }
                                                                synchronized (this.mPendingCellInfoRequests) {
                                                                    if (this.mIsPendingCellInfoRequest) {
                                                                        this.mIsPendingCellInfoRequest = false;
                                                                        for (Message m : this.mPendingCellInfoRequests) {
                                                                            AsyncResult.forMessage(m, cellInfo, ex);
                                                                            m.sendToTarget();
                                                                        }
                                                                        this.mPendingCellInfoRequests.clear();
                                                                    }
                                                                }
                                                                return;
                                                            case 45:
                                                                log("EVENT_CHANGE_IMS_STATE:");
                                                                setPowerStateToDesired();
                                                                return;
                                                            case 46:
                                                                this.mCi.getImsRegistrationState(obtainMessage(47));
                                                                return;
                                                            case 47:
                                                                AsyncResult ar9 = (AsyncResult) msg.obj;
                                                                if (ar9.exception == null) {
                                                                    if (((int[]) ar9.result)[0] != 1) {
                                                                        z = false;
                                                                    }
                                                                    this.mImsRegistered = z;
                                                                }
                                                                GsmCdmaPhone gsmCdmaPhone = this.mPhone;
                                                                if (gsmCdmaPhone.isCTSimCard(gsmCdmaPhone.getPhoneId())) {
                                                                    pollState();
                                                                    return;
                                                                }
                                                                return;
                                                            case 48:
                                                                log("EVENT_IMS_CAPABILITY_CHANGED");
                                                                updateSpnDisplay();
                                                                GsmCdmaPhone gsmCdmaPhone2 = this.mPhone;
                                                                if (!(gsmCdmaPhone2 == null || gsmCdmaPhone2.getImsPhone() == null)) {
                                                                    boolean tempPreVowifiState = this.mPreVowifiState;
                                                                    int airplaneMode = Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", -1);
                                                                    this.mPreVowifiState = this.mPhone.getImsPhone().isWifiCallingEnabled();
                                                                    if (tempPreVowifiState && !this.mPhone.getImsPhone().isWifiCallingEnabled() && airplaneMode == 0 && this.mPollingContext[0] == 0) {
                                                                        log("mPollingContext == 0");
                                                                        pollState();
                                                                    }
                                                                }
                                                                this.mImsCapabilityChangedRegistrants.notifyRegistrants();
                                                                return;
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
                                                                return;
                                                            case 50:
                                                                break;
                                                            case 51:
                                                                AsyncResult ar10 = (AsyncResult) msg.obj;
                                                                if (ar10.exception == null) {
                                                                    boolean enable = ((Boolean) ar10.result).booleanValue();
                                                                    log("EVENT_RADIO_POWER_FROM_CARRIER: " + enable);
                                                                    setRadioPowerFromCarrier(enable);
                                                                    return;
                                                                }
                                                                return;
                                                            default:
                                                                switch (i) {
                                                                    case 53:
                                                                        log("EVENT_IMS_SERVICE_STATE_CHANGED");
                                                                        if (this.mSS.getState() != 0) {
                                                                            GsmCdmaPhone gsmCdmaPhone3 = this.mPhone;
                                                                            gsmCdmaPhone3.notifyServiceStateChanged(gsmCdmaPhone3.getServiceState());
                                                                            return;
                                                                        }
                                                                        return;
                                                                    case 54:
                                                                        log("EVENT_RADIO_POWER_OFF_DONE");
                                                                        if (this.mDeviceShuttingDown && this.mCi.getRadioState() != 2) {
                                                                            this.mCi.requestShutdown(null);
                                                                            return;
                                                                        }
                                                                        return;
                                                                    case 55:
                                                                        AsyncResult ar11 = (AsyncResult) msg.obj;
                                                                        if (ar11.exception == null) {
                                                                            List<PhysicalChannelConfig> list = (List) ar11.result;
                                                                            this.mPhone.notifyPhysicalChannelConfiguration(list);
                                                                            this.mLastPhysicalChannelConfigList = list;
                                                                            if ((updateNrFrequencyRangeFromPhysicalChannelConfigs(list, this.mSS) || updateNrStateFromPhysicalChannelConfigs(list, this.mSS)) || RatRatcheter.updateBandwidths(getBandwidthsFromConfigs(list), this.mSS)) {
                                                                                ServiceState ss = new ServiceState(this.mSS);
                                                                                NetworkRegistrationInfo regInfo = ss.getNetworkRegistrationInfoHw(2, 1);
                                                                                regInfo.setNsaState(this.mPreNsaState);
                                                                                int preDataRat = regInfo.getConfigRadioTechnology();
                                                                                ss.addNetworkRegistrationInfo(regInfo);
                                                                                updateDataRatByConfig(this.mSS.getRilDataRadioTechnology(), this.mSS, this.mPhone);
                                                                                if (isNeedDelayUpdateRegisterStateDone(ss, this.mSS)) {
                                                                                    NetworkRegistrationInfo regInfoSS = this.mSS.getNetworkRegistrationInfoHw(2, 1);
                                                                                    regInfoSS.setNsaState(this.mPreNsaState);
                                                                                    regInfoSS.setConfigRadioTechnology(preDataRat);
                                                                                    this.mNewNsaState = this.mPreNsaState;
                                                                                    this.mSS.addNetworkRegistrationInfo(regInfoSS);
                                                                                    return;
                                                                                }
                                                                                if (HuaweiTelephonyConfigs.isChinaMobile()) {
                                                                                    updateSpnDisplay();
                                                                                }
                                                                                this.mPhone.notifyServiceStateChanged(this.mSS);
                                                                                for (int transport : this.mTransportManager.getAvailableTransports()) {
                                                                                    notifyDataRegStateRilRadioTechnologyChanged(transport);
                                                                                }
                                                                                return;
                                                                            }
                                                                            return;
                                                                        }
                                                                        return;
                                                                    case 56:
                                                                        AsyncResult ar12 = (AsyncResult) msg.obj;
                                                                        if (ar12 == null) {
                                                                            loge("Invalid null response to getCellLocation!");
                                                                            return;
                                                                        }
                                                                        Message rspRspMsg = (Message) ar12.userObj;
                                                                        AsyncResult.forMessage(rspRspMsg, getCellLocation(), ar12.exception);
                                                                        rspRspMsg.sendToTarget();
                                                                        return;
                                                                    case 57:
                                                                        onCarrierConfigChanged();
                                                                        return;
                                                                    case 58:
                                                                        log("EVENT_RADIO_POWER_OFF");
                                                                        this.mNewSS.setStateOff();
                                                                        pollStateDone();
                                                                        return;
                                                                    default:
                                                                        log("Unhandled message with number: " + msg.what);
                                                                        return;
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                        if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == 1) {
                            handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                            queueNextSignalStrengthPoll();
                        }
                        setPowerStateToDesired();
                        modemTriggeredPollState();
                    } else if (this.mPollingContext[0] == 0) {
                        log("EVENT_GET_AD_DONE pollState ");
                        pollState();
                    } else {
                        log("EVENT_GET_AD_DONE pollState working ,no need do again");
                    }
                } else if (!this.mPhone.isPhoneTypeGsm()) {
                    AsyncResult ar13 = (AsyncResult) msg.obj;
                    if (ar13.exception == null) {
                        String[] cdmaSubscription = (String[]) ar13.result;
                        if (cdmaSubscription != null && cdmaSubscription.length >= 5) {
                            if (cdmaSubscription[0] != null) {
                                this.mMdn = cdmaSubscription[0];
                                log("EVENT_POLL_STATE_CDMA_SUBSCRIPTION: setting mMdn to ****.");
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
                            if (this.mIsSubscriptionFromRuim || this.mIccRecords == null) {
                                log("GET_CDMA_SUBSCRIPTION either mIccRecords is null or NV type device - not setting Imsi in mIccRecords");
                                return;
                            }
                            log("GET_CDMA_SUBSCRIPTION set imsi in mIccRecords");
                            this.mIccRecords.setImsi(getImsi());
                        } else if (cdmaSubscription == null) {
                            log("GET_CDMA_SUBSCRIPTION: cdmaSubscription is null");
                        } else {
                            log("GET_CDMA_SUBSCRIPTION: error parsing cdmaSubscription params num=" + cdmaSubscription.length);
                        }
                    }
                }
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                log("EVENT_RUIM_RECORDS_LOADED: what=" + msg.what);
                this.mCdnr.updateEfFromRuim((RuimRecords) this.mIccRecords);
                updatePhoneObject();
                if (this.mPhone.isPhoneTypeCdma()) {
                    updateSpnDisplay();
                    return;
                }
                RuimRecords ruim = (RuimRecords) this.mIccRecords;
                if (ruim != null) {
                    this.mMdn = ruim.getMdn();
                    if (ruim.isProvisioned()) {
                        this.mMin = ruim.getMin();
                        parseSidNid(ruim.getSid(), ruim.getNid());
                        String prlVersion = ruim.getPrlVersion();
                        if (prlVersion != null) {
                            String prlVersion2 = prlVersion.trim();
                            if (!PhoneConfigurationManager.SSSS.equals(prlVersion2) && !"65535".equals(prlVersion2)) {
                                this.mPrlVersion = prlVersion2;
                                SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                            }
                        }
                        this.mIsMinInfoReady = true;
                    } else {
                        log("EVENT_RUIM_RECORDS_LOADED: ruim not provisioned; not updating mMdn ****.");
                    }
                    updateOtaspState();
                    notifyCdmaSubscriptionInfoReady();
                } else {
                    log("EVENT_RUIM_RECORDS_LOADED: ruim is null; not updating mMdn ****.");
                }
                pollState();
            }
        }
    }

    private boolean isSimAbsent() {
        boolean simAbsent = true;
        if (VSimUtilsInner.isHisiVSimSlot(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.getVSimCardState() == IccCardStatus.CardState.CARDSTATE_ABSENT;
        }
        UiccController uiccController = this.mUiccController;
        if (uiccController == null) {
            return true;
        }
        UiccCard uiccCard = uiccController.getUiccCard(this.mPhone.getPhoneId());
        if (uiccCard == null) {
            return true;
        }
        if (uiccCard.getCardState() != IccCardStatus.CardState.CARDSTATE_ABSENT) {
            simAbsent = false;
        }
        return simAbsent;
    }

    private int[] getBandwidthsFromConfigs(List<PhysicalChannelConfig> list) {
        return list.stream().map($$Lambda$WWHOcG5P4jgjzPPgLwmwN15OM.INSTANCE).mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    public String getRplmn() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mHwServiceStateTrackerEx.getGsmRplmn();
        }
        return PhoneConfigurationManager.SSSS;
    }

    public void setCurrent3GPsCsAllowed(boolean allowed) {
        log("setCurrent3GPsCsAllowed:" + allowed);
        this.isCurrent3GPsCsAllowed = allowed;
    }

    /* access modifiers changed from: protected */
    public boolean isSidsAllZeros() {
        if (this.mHomeSystemId == null) {
            return true;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mHomeSystemId;
            if (i >= iArr.length) {
                return true;
            }
            if (iArr[i] != 0) {
                return false;
            }
            i++;
        }
    }

    public ServiceState getServiceState() {
        return new ServiceState(this.mSS);
    }

    private boolean isHomeSid(int sid) {
        if (this.mHomeSystemId == null) {
            return false;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mHomeSystemId;
            if (i >= iArr.length) {
                return false;
            }
            if (sid == iArr[i]) {
                return true;
            }
            i++;
        }
    }

    public String getMdnNumber() {
        return this.mMdn;
    }

    public String getCdmaMin() {
        return this.mMin;
    }

    public String getPrlVersion() {
        int slotId = this.mPhone.getPhoneId();
        int simCardState = TelephonyManager.getDefault().getSimState(slotId);
        if (5 == simCardState) {
            this.mPrlVersion = this.mCi.getHwPrlVersion();
        } else {
            this.mPrlVersion = ProxyController.MODEM_0;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getPrlVersion: prlVersion=" + this.mPrlVersion + ", slotId=" + slotId + ", simState=" + simCardState);
        return this.mPrlVersion;
    }

    public String getMlplVersion() {
        String realMlplVersion = null;
        int slotId = this.mPhone.getPhoneId();
        if (true == HwModemCapability.isCapabilitySupport(9) && 5 == TelephonyManager.getDefault().getSimState(slotId)) {
            realMlplVersion = this.mCi.getHwCDMAMlplVersion();
        }
        if (realMlplVersion == null) {
            realMlplVersion = this.mMlplVersion;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getMlplVersion: mlplVersion=" + realMlplVersion);
        return realMlplVersion;
    }

    public String getMsplVersion() {
        String realMsplVersion = null;
        int slotId = this.mPhone.getPhoneId();
        if (true == HwModemCapability.isCapabilitySupport(9) && 5 == TelephonyManager.getDefault().getSimState(slotId)) {
            realMsplVersion = this.mCi.getHwCDMAMsplVersion();
        }
        if (realMsplVersion == null) {
            realMsplVersion = this.mMsplVersion;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getMsplVersion: msplVersion=" + realMsplVersion);
        return realMsplVersion;
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
        int provisioningState;
        if (this.mPhone.isPhoneTypeGsm()) {
            log("getOtasp: otasp not needed for GSM");
            return 3;
        } else if (this.mIsSubscriptionFromRuim && this.mMin == null) {
            return 3;
        } else {
            String str = this.mMin;
            if (str == null || str.length() < 6) {
                log("getOtasp: bad mMin='" + this.mMin + "'");
                provisioningState = 1;
            } else if (this.mMin.equals(UNACTIVATED_MIN_VALUE) || this.mMin.substring(0, 6).equals(UNACTIVATED_MIN2_VALUE) || SystemProperties.getBoolean("test_cdma_setup", false)) {
                provisioningState = 2;
            } else {
                provisioningState = 3;
            }
            log("getOtasp: state=" + provisioningState);
            return provisioningState;
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
    @UnsupportedAppUsage
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
        boolean isRoamIndForHomeSystem;
        boolean isRoamingBetweenOperators;
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null) {
            hwCustGsmServiceStateTracker.custHandlePollStateResult(what, ar, this.mPollingContext);
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
            if (iArr[0] == 0) {
                this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                combinePsRegistrationStates(this.mNewSS);
                updateOperatorNameForServiceState(this.mNewSS);
                this.hasUpdateCellLocByPS = false;
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateRoamingState();
                } else {
                    boolean namMatch = false;
                    if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getCdmaSystemId())) {
                        namMatch = true;
                    }
                    if (this.mIsSubscriptionFromRuim && (isRoamingBetweenOperators = isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS)) != this.mNewSS.getVoiceRoaming()) {
                        log("isRoamingBetweenOperators=" + isRoamingBetweenOperators + ". Override CDMA voice roaming to " + isRoamingBetweenOperators);
                        this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators);
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
                        } else {
                            int i = this.mRoamingIndicator;
                            if (-1 != i && this.mNewSS.getDataRoaming() == (isRoamIndForHomeSystem = isRoamIndForHomeSystem(i))) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("isRoamIndForHomeSystem=");
                                sb.append(isRoamIndForHomeSystem);
                                sb.append(", override data roaming to ");
                                sb.append(!isRoamIndForHomeSystem);
                                log(sb.toString());
                                this.mNewSS.setDataRoaming(!isRoamIndForHomeSystem);
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
                            if (namMatch || !this.mIsInPrl) {
                                int i2 = this.mRoamingIndicator;
                                if (i2 <= 2) {
                                    this.mNewSS.setCdmaRoamingIndicator(1);
                                } else {
                                    this.mNewSS.setCdmaRoamingIndicator(i2);
                                }
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
                    this.mNewSS.setCdmaEriIconIndex(this.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                    this.mNewSS.setCdmaEriIconMode(this.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                    IHwServiceStateTrackerEx iHwServiceStateTrackerEx = this.mHwServiceStateTrackerEx;
                    ServiceState serviceState = this.mNewSS;
                    iHwServiceStateTrackerEx.updateCTRoaming(serviceState, serviceState.getRoaming());
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator);
                }
                this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                pollStateDone();
                HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker2 = this.mHwCustGsmServiceStateTracker;
                if (hwCustGsmServiceStateTracker2 != null) {
                    hwCustGsmServiceStateTracker2.clearLteEmmCause(getPhone().getPhoneId(), this.mSS);
                }
            }
        }
    }

    private boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return cdmaRoaming && !isSameOperatorNameFromSimAndSS(s);
    }

    private boolean isNrStateChanged(NetworkRegistrationInfo oldRegState, NetworkRegistrationInfo newRegState) {
        return (oldRegState == null || newRegState == null) ? oldRegState != newRegState : oldRegState.getNrState() != newRegState.getNrState();
    }

    private boolean updateNrFrequencyRangeFromPhysicalChannelConfigs(List<PhysicalChannelConfig> physicalChannelConfigs, ServiceState ss) {
        int newFrequencyRange = -1;
        boolean hasChanged = false;
        if (physicalChannelConfigs != null) {
            DcTracker dcTracker = this.mPhone.getDcTracker(1);
            for (PhysicalChannelConfig config : physicalChannelConfigs) {
                if (isNrPhysicalChannelConfig(config)) {
                    int[] contextIds = config.getContextIds();
                    int length = contextIds.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        DataConnection dc = dcTracker.getDataConnectionByContextId(contextIds[i]);
                        if (dc != null && dc.getNetworkCapabilities().hasCapability(12)) {
                            newFrequencyRange = ServiceState.getBetterNRFrequencyRange(newFrequencyRange, config.getFrequencyRange());
                            break;
                        }
                        i++;
                    }
                }
            }
        }
        if (newFrequencyRange != ss.getNrFrequencyRange()) {
            hasChanged = true;
        }
        ss.setNrFrequencyRange(newFrequencyRange);
        return hasChanged;
    }

    private boolean updateNrStateFromPhysicalChannelConfigs(List<PhysicalChannelConfig> configs, ServiceState ss) {
        int newNsaState;
        boolean hasChanged = true;
        NetworkRegistrationInfo regInfo = ss.getNetworkRegistrationInfoHw(2, 1);
        this.mPreNsaState = this.mSS.getNsaState();
        boolean hasNrSecondaryServingCell = false;
        if (configs != null) {
            Iterator<PhysicalChannelConfig> it = configs.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                PhysicalChannelConfig config = it.next();
                if (isNrPhysicalChannelConfig(config) && config.getConnectionStatus() == 2) {
                    hasNrSecondaryServingCell = true;
                    break;
                }
            }
        }
        if (regInfo == null) {
            return false;
        }
        int newNrState = regInfo.getNrState();
        regInfo.getNsaState();
        if (hasNrSecondaryServingCell) {
            newNrState = 3;
            newNsaState = 5;
        } else if (regInfo.getNrState() == 3) {
            newNrState = 2;
            newNsaState = 2;
        } else if (regInfo.getNrState() == 2) {
            newNsaState = 2;
        } else if (ss.getRilDataRadioTechnology() != 20) {
            newNsaState = 1;
        } else {
            newNsaState = 0;
        }
        if (newNsaState != 5) {
            newNsaState = this.mHwServiceStateTrackerEx.updateNsaState(ss, newNsaState, getCidFromCellIdentity(regInfo.getCellIdentity()));
        }
        this.mNewNsaState = newNsaState;
        if (newNsaState == regInfo.getNsaState()) {
            hasChanged = false;
        }
        regInfo.setNrState(newNrState);
        regInfo.setNsaState(newNsaState);
        ss.addNetworkRegistrationInfo(regInfo);
        return hasChanged;
    }

    private boolean isNrPhysicalChannelConfig(PhysicalChannelConfig config) {
        return config.getRat() == 20;
    }

    private void combinePsRegistrationStates(ServiceState serviceState) {
        NetworkRegistrationInfo wlanPsRegState = serviceState.getNetworkRegistrationInfoHw(2, 2);
        NetworkRegistrationInfo wwanPsRegState = serviceState.getNetworkRegistrationInfoHw(2, 1);
        boolean isIwlanPreferred = this.mTransportManager.isAnyApnPreferredOnIwlan();
        serviceState.setIwlanPreferred(isIwlanPreferred);
        if (wlanPsRegState != null && wlanPsRegState.getAccessNetworkTechnology() == 18 && wlanPsRegState.getRegistrationState() == 1 && isIwlanPreferred) {
            serviceState.setDataRegState(0);
        } else if (wwanPsRegState != null) {
            serviceState.setDataRegState(regCodeToServiceState(wwanPsRegState.getRegistrationState()));
        }
        log("combinePsRegistrationStates: " + serviceState.getDataRegState() + " CsRegistrationStates: " + serviceState.getVoiceRegState());
    }

    /* access modifiers changed from: package-private */
    public void handlePollStateResultMessage(int what, AsyncResult ar) {
        int networkId;
        if (what == 4) {
            NetworkRegistrationInfo networkRegState = (NetworkRegistrationInfo) ar.result;
            VoiceSpecificRegistrationInfo voiceSpecificStates = networkRegState.getVoiceSpecificInfo();
            int registrationState = networkRegState.getRegistrationState();
            boolean z = voiceSpecificStates.cssSupported;
            this.mHwServiceStateTrackerEx.getCARilRadioType(ServiceState.networkTypeToRilRadioTechnology(networkRegState.getAccessNetworkTechnology()));
            this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
            setOldCsRegState(regCodeToServiceState(registrationState));
            this.mNewSS.setCssIndicator(z ? 1 : 0);
            this.mNewSS.addNetworkRegistrationInfo(networkRegState);
            setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState.getCellIdentity());
            int reasonForDenial = networkRegState.getRejectCause();
            this.mEmergencyOnly = networkRegState.isEmergencyEnabled();
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mGsmRoaming = regCodeIsRoaming(registrationState);
                this.mNewRejectCode = reasonForDenial;
                this.mHwServiceStateTrackerEx.sendGsmRoamingIntentIfDenied(registrationState, reasonForDenial);
                this.mPhone.getContext().getResources().getBoolean(17891573);
            } else {
                int roamingIndicator = voiceSpecificStates.roamingIndicator;
                int systemIsInPrl = voiceSpecificStates.systemIsInPrl;
                int defaultRoamingIndicator = voiceSpecificStates.defaultRoamingIndicator;
                this.mRegistrationState = registrationState;
                this.mNewSS.setVoiceRoaming(regCodeIsRoaming(registrationState) && !isRoamIndForHomeSystem(roamingIndicator));
                this.mRoamingIndicator = roamingIndicator;
                this.mIsInPrl = systemIsInPrl != 0;
                this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                int systemId = 0;
                CellIdentity cellIdentity = networkRegState.getCellIdentity();
                if (cellIdentity == null || cellIdentity.getType() != 2) {
                    networkId = 0;
                } else {
                    systemId = ((CellIdentityCdma) cellIdentity).getSystemId();
                    networkId = ((CellIdentityCdma) cellIdentity).getNetworkId();
                }
                this.mNewSS.setCdmaSystemAndNetworkId(systemId, networkId);
                if (reasonForDenial == 0) {
                    this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                } else if (reasonForDenial == 1) {
                    this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                } else {
                    this.mRegistrationDeniedReason = PhoneConfigurationManager.SSSS;
                }
                if (this.mRegistrationState == 3) {
                    log("Registration denied, " + this.mRegistrationDeniedReason);
                }
            }
            processCtVolteCellLocationInfo(regCodeToServiceState(registrationState), networkRegState, true);
        } else if (what == 5) {
            NetworkRegistrationInfo networkRegState2 = (NetworkRegistrationInfo) ar.result;
            this.mNewSS.addNetworkRegistrationInfo(networkRegState2);
            DataSpecificRegistrationInfo dataSpecificStates = networkRegState2.getDataSpecificInfo();
            int registrationState2 = networkRegState2.getRegistrationState();
            int serviceState = regCodeToServiceState(registrationState2);
            int newDataRat = ServiceState.networkTypeToRilRadioTechnology(networkRegState2.getAccessNetworkTechnology());
            this.mNewSS.setDataRegState(serviceState);
            int newDataRat2 = this.mHwServiceStateTrackerEx.updateHSPAStatus(this.mHwServiceStateTrackerEx.updateCAStatus(newDataRat));
            log("handlePollStateResultMessage: PS cellular. " + networkRegState2);
            if (serviceState == 1) {
                this.mLastPhysicalChannelConfigList = null;
                updateNrFrequencyRangeFromPhysicalChannelConfigs(null, this.mNewSS);
            }
            if (serviceState == 0) {
                updateNrStateFromPhysicalChannelConfigs(this.mLastPhysicalChannelConfigList, this.mNewSS);
                this.mHwServiceStateTrackerEx.updateNsaState(this.mNewSS, this.mNewNsaState, getCidFromCellIdentity(networkRegState2.getCellIdentity()));
                updateDataRatByConfig(newDataRat2, this.mNewSS, this.mPhone);
            }
            setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState2.getCellIdentity());
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mNewReasonDataDenied = networkRegState2.getRejectCause();
                this.mNewMaxDataCalls = dataSpecificStates.maxDataCalls;
                this.mDataRoaming = regCodeIsRoaming(registrationState2);
            } else if (this.mPhone.isPhoneTypeCdma()) {
                this.mNewSS.setDataRoaming(regCodeIsRoaming(registrationState2));
            } else {
                int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                if ((oldDataRAT == 0 && newDataRat2 != 0) || ((ServiceState.isCdma(oldDataRAT) && ServiceState.isLte(newDataRat2)) || (ServiceState.isLte(oldDataRAT) && ServiceState.isCdma(newDataRat2)))) {
                    this.mCi.getSignalStrength(obtainMessage(3));
                }
                this.mNewSS.setDataRoaming(regCodeIsRoaming(registrationState2));
            }
            updateServiceStateLteEarfcnBoost(this.mNewSS, getLteEarfcn(networkRegState2.getCellIdentity()));
            processCtVolteCellLocationInfo(serviceState, networkRegState2, false);
        } else if (what == 6) {
            this.mNewSS.addNetworkRegistrationInfo((NetworkRegistrationInfo) ar.result);
        } else if (what == 7) {
            String brandOverride = getOperatorBrandOverride();
            this.mCdnr.updateEfForBrandOverride(brandOverride);
            if (this.mPhone.isPhoneTypeGsm()) {
                String[] opNames = (String[]) ar.result;
                if (opNames != null && opNames.length >= 3) {
                    this.mNewSS.setOperatorAlphaLongRaw(opNames[0]);
                    this.mNewSS.setOperatorAlphaShortRaw(opNames[1]);
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
            String[] opNames2 = (String[]) ar.result;
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
            } else if (brandOverride != null) {
                this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames2[2]);
            } else {
                this.mNewSS.setOperatorName(opNames2[0], opNames2[1], opNames2[2]);
            }
        } else if (what != 14) {
            loge("handlePollStateResultMessage: Unexpected RIL response received: " + what);
        } else {
            int[] ints = (int[]) ar.result;
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

    private static int getCidFromCellIdentity(CellIdentity id) {
        if (id == null) {
            return -1;
        }
        int cid = -1;
        int type = id.getType();
        if (type == 1) {
            cid = ((CellIdentityGsm) id).getCid();
        } else if (type == 3) {
            cid = ((CellIdentityLte) id).getCi();
        } else if (type == 4) {
            cid = ((CellIdentityWcdma) id).getCid();
        } else if (type == 5) {
            cid = ((CellIdentityTdscdma) id).getCid();
        }
        if (cid == Integer.MAX_VALUE) {
            return -1;
        }
        return cid;
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
                    int bw = bandwidths[i];
                    if (!isValidLteBandwidthKhz(bw)) {
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

    private boolean isRoamIndForHomeSystem(int roamInd) {
        int[] homeRoamIndicators = getCarrierConfig().getIntArray("cdma_enhanced_roaming_indicator_for_home_network_int_array");
        log("isRoamIndForHomeSystem: homeRoamIndicators=" + Arrays.toString(homeRoamIndicators));
        if (homeRoamIndicators != null) {
            for (int homeRoamInd : homeRoamIndicators) {
                if (homeRoamInd == roamInd) {
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
    @UnsupportedAppUsage
    public void updateRoamingState() {
        PersistableBundle bundle = getCarrierConfig();
        boolean z = false;
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mGsmRoaming || this.mDataRoaming) {
                z = true;
            }
            boolean roaming = z;
            log("updateRoamingState: original roaming = " + roaming + " mGsmRoaming:" + this.mGsmRoaming + " mDataRoaming:" + this.mDataRoaming);
            if (this.mHwServiceStateTrackerEx.checkForRoamingForIndianOperators(this.mNewSS)) {
                log("indian operator,skip");
            } else if (this.mGsmRoaming && !isOperatorConsideredRoaming(this.mNewSS) && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                log("updateRoamingState: resource override set non roaming.isSameNamedOperators=" + isSameNamedOperators(this.mNewSS) + ",isOperatorConsideredNonRoaming=" + isOperatorConsideredNonRoaming(this.mNewSS));
                roaming = false;
                log("updateRoamingState: set roaming = false");
            }
            if (alwaysOnHomeNetwork(bundle)) {
                log("updateRoamingState: carrier config override always on home network");
                roaming = false;
            } else if (isNonRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric())) {
                log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                roaming = false;
            } else if (isRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric())) {
                log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                roaming = true;
            }
            boolean roaming2 = this.mHwServiceStateTrackerEx.getGsmRoamingState(roaming);
            this.mNewSS.setVoiceRoaming(roaming2);
            this.mNewSS.setDataRoaming(roaming2);
            return;
        }
        String systemId = Integer.toString(this.mNewSS.getCdmaSystemId());
        if (alwaysOnHomeNetwork(bundle)) {
            log("updateRoamingState: carrier config override always on home network");
            setRoamingOff();
        } else if (isNonRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric()) || isNonRoamingInCdmaNetwork(bundle, systemId)) {
            log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
            setRoamingOff();
        } else if (isRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(bundle, systemId)) {
            log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
            setRoamingOn();
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

    private void updateOperatorNameFromCarrierConfig() {
        if (!this.mPhone.isPhoneTypeGsm() && !this.mSS.getRoaming()) {
            if (!((this.mUiccController.getUiccCard(getPhoneId()) == null || this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) ? false : true)) {
                PersistableBundle config = getCarrierConfig();
                if (config.getBoolean("cdma_home_registered_plmn_name_override_bool")) {
                    String operator = config.getString("cdma_home_registered_plmn_name_string");
                    log("updateOperatorNameFromCarrierConfig: changing from " + this.mSS.getOperatorAlpha() + " to " + operator);
                    ServiceState serviceState = this.mSS;
                    serviceState.setOperatorName(operator, operator, serviceState.getOperatorNumeric());
                }
            }
        }
    }

    private void notifySpnDisplayUpdate(CarrierDisplayNameData data, boolean showWifi, String wifi, String regplmn) {
        Intent intent;
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker;
        int subId = this.mPhone.getSubId();
        int combinedRegState = this.mHwServiceStateTrackerEx.getCombinedRegState(this.mSS);
        if (this.mSubId != subId || isOperatorChanged(data, showWifi, wifi, regplmn) || ((hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker) != null && hwCustGsmServiceStateTracker.isInServiceState(combinedRegState))) {
            String log = String.format("updateSpnDisplay: changed sending intent, rule=%d, showPlmn='%b', plmn='%s', showSpn='%b', spn='%s', dataSpn='%s', subId='%d'", Integer.valueOf(getCarrierNameDisplayBitmask(this.mSS)), Boolean.valueOf(data.shouldShowPlmn()), data.getPlmn(), Boolean.valueOf(data.shouldShowSpn()), data.getSpn(), data.getDataSpn(), Integer.valueOf(subId));
            this.mCdnrLogs.log(log);
            log("updateSpnDisplay: " + log);
            if (!this.mPhone.isPhoneTypeGsm()) {
                updateOperatorProp();
            }
            if (VSimUtilsInner.isHisiVSimSlot(this.mPhone.mPhoneId)) {
                intent = new Intent("com.huawei.vsim.action.SPN_STRINGS_UPDATED_VSIM");
            } else {
                intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            }
            intent.putExtra("showSpn", data.shouldShowSpn());
            intent.putExtra("spn", data.getSpn());
            intent.putExtra("spnData", data.getDataSpn());
            intent.putExtra("showPlmn", data.shouldShowPlmn());
            intent.putExtra("plmn", data.getPlmn());
            intent.putExtra(EXTRA_SHOW_WIFI, showWifi);
            intent.putExtra(EXTRA_WIFI, wifi);
            if (this.mPhone.isPhoneTypeGsm()) {
                intent.putExtra(EXTRA_SHOW_EMERGENCYONLY, this.mShowEmergencyOnly);
            }
            if (VSimUtilsInner.isHisiVSimSlot(this.mPhone.mPhoneId)) {
                intent.putExtra("subscription", 2);
                intent.putExtra("phone", 2);
                intent.putExtra("slot", 2);
            } else {
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            }
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (this.mSS != null && combinedRegState == 0 && !this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), data.shouldShowPlmn(), data.getPlmn(), data.shouldShowSpn(), data.getSpn())) {
                this.mSpnUpdatePending = true;
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker2 = this.mHwCustGsmServiceStateTracker;
                if (hwCustGsmServiceStateTracker2 != null) {
                    hwCustGsmServiceStateTracker2.setExtPlmnSent(false);
                }
                this.mHwServiceStateTrackerEx.sendDualSimUpdateSpnIntent(data.shouldShowSpn(), data.getSpn(), data.shouldShowPlmn(), data.getPlmn());
            } else {
                this.mHwServiceStateTrackerEx.sendDualSimUpdateSpnIntent(false, PhoneConfigurationManager.SSSS, data.shouldShowPlmn(), data.getPlmn());
            }
        }
        if (!this.mPhone.isPhoneTypeGsm()) {
            updateOperatorProp();
        }
        this.mSubId = subId;
        this.mCurShowSpn = data.shouldShowSpn();
        this.mCurShowPlmn = data.shouldShowPlmn();
        this.mCurSpn = data.getSpn();
        this.mCurDataSpn = data.getDataSpn();
        this.mCurPlmn = data.getPlmn();
        this.mCurShowWifi = showWifi;
        this.mCurWifi = wifi;
        this.mCurRegplmn = regplmn;
    }

    private void updateSpnDisplayCdnr() {
        log("updateSpnDisplayCdnr+");
        notifySpnDisplayUpdate(this.mCdnr.getCarrierDisplayNameData(), this.mCurShowWifi, this.mCurWifi, this.mCurRegplmn);
        log("updateSpnDisplayCdnr-");
    }

    @UnsupportedAppUsage
    @VisibleForTesting
    public void updateSpnDisplay() {
        if (this.mHwServiceStateTrackerEx.hasRatChangedDelayMessage()) {
            log("In rat changed delay process.");
        } else if (getCarrierConfig().getBoolean("enable_carrier_display_name_resolver_bool")) {
            updateSpnDisplayCdnr();
        } else {
            updateSpnDisplayLegacy();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:93:0x0297  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x02a4  */
    private void updateSpnDisplayLegacy() {
        boolean showSpn;
        String dataSpn;
        String spn;
        String dataSpn2;
        boolean showPlmn;
        boolean showSpn2;
        String plmn;
        String spn2;
        boolean showPlmn2;
        String plmn2;
        boolean showSpn3;
        boolean showSpn4;
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker;
        String plmn3;
        String plmn4;
        boolean showPlmn3;
        boolean noService;
        log("updateSpnDisplayLegacy+");
        boolean showWifi = false;
        String wifi = null;
        String regplmn = null;
        String wfcVoiceSpnFormat = null;
        String wfcDataSpnFormat = null;
        String wfcFlightSpnFormat = null;
        int combinedRegState = this.mHwServiceStateTrackerEx.getCombinedRegState(this.mSS);
        if (this.mPhone.getImsPhone() == null || !this.mPhone.getImsPhone().isWifiCallingEnabled() || combinedRegState != 0) {
            spn = null;
            dataSpn = null;
            showSpn = false;
        } else {
            spn = null;
            PersistableBundle bundle = getCarrierConfig();
            dataSpn = null;
            int voiceIdx = bundle.getInt("wfc_spn_format_idx_int");
            int dataIdx = bundle.getInt("wfc_data_spn_format_idx_int");
            int flightModeIdx = bundle.getInt("wfc_flight_mode_spn_format_idx_int");
            showSpn = false;
            String[] wfcSpnFormats = SubscriptionManager.getResourcesForSubId(this.mPhone.getContext(), this.mPhone.getSubId(), bundle.getBoolean("wfc_spn_use_root_locale")).getStringArray(17236117);
            if (voiceIdx < 0 || voiceIdx >= wfcSpnFormats.length) {
                loge("updateSpnDisplay: KEY_WFC_SPN_FORMAT_IDX_INT out of bounds: " + voiceIdx);
                voiceIdx = 0;
            }
            if (dataIdx < 0 || dataIdx >= wfcSpnFormats.length) {
                loge("updateSpnDisplay: KEY_WFC_DATA_SPN_FORMAT_IDX_INT out of bounds: " + dataIdx);
                dataIdx = 0;
            }
            if (flightModeIdx < 0 || flightModeIdx >= wfcSpnFormats.length) {
                flightModeIdx = voiceIdx;
            }
            wfcVoiceSpnFormat = wfcSpnFormats[voiceIdx];
            wfcDataSpnFormat = wfcSpnFormats[dataIdx];
            wfcFlightSpnFormat = wfcSpnFormats[flightModeIdx];
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            IccRecords iccRecords = this.mIccRecords;
            int rule = getCarrierNameDisplayBitmask(this.mSS);
            if (VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId())) {
                rule = VSimUtilsInner.changeRuleForVSim(rule);
            }
            regplmn = this.mSS.getOperatorNumeric();
            this.mShowEmergencyOnly = false;
            boolean noService2 = false;
            if (combinedRegState == 1 || combinedRegState == 2) {
                boolean forceDisplayNoService = this.mPhone.getContext().getResources().getBoolean(17891415) && !this.mIsSimReady;
                if (DELAYED_ECC_TO_NOSERVICE_VALUE > 0) {
                    this.mEmergencyOnly = this.mSS.isEmergencyOnly();
                }
                if (!this.mEmergencyOnly || forceDisplayNoService) {
                    plmn4 = Resources.getSystem().getText(17040422).toString();
                    noService2 = true;
                } else {
                    plmn4 = Resources.getSystem().getText(17040034).toString();
                    this.mShowEmergencyOnly = true;
                }
                HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker2 = this.mHwCustGsmServiceStateTracker;
                if (hwCustGsmServiceStateTracker2 != null) {
                    noService = noService2;
                    showPlmn3 = true;
                    plmn4 = hwCustGsmServiceStateTracker2.setEmergencyToNoService(this.mSS, plmn4, this.mEmergencyOnly);
                } else {
                    noService = noService2;
                    showPlmn3 = true;
                }
                log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn4 + "'");
                plmn2 = plmn4;
                noService2 = noService;
                showPlmn2 = showPlmn3;
            } else if (combinedRegState == 0) {
                getOperator();
                plmn2 = this.mHwServiceStateTrackerEx.getGsmPlmn();
                showPlmn2 = !TextUtils.isEmpty(plmn2) && (rule & 2) == 2;
                log("updateSpnDisplay: rawPlmn = " + plmn2);
            } else {
                showPlmn2 = true;
                plmn2 = Resources.getSystem().getText(17040422).toString();
                log("updateSpnDisplay: radio is off w/ showPlmn=true plmn=" + plmn2);
            }
            String spn3 = getServiceProviderName();
            if (VSimUtilsInner.isVSimSlot(this.mPhone.mPhoneId)) {
                spn3 = VSimUtilsInner.changeSpnForVSim(spn3);
            }
            dataSpn2 = spn3;
            if (combinedRegState == 0 && !TextUtils.isEmpty(spn3)) {
                if ((rule & 1) == 1) {
                    showSpn3 = true;
                    StringBuilder sb = new StringBuilder();
                    showSpn4 = showSpn3;
                    sb.append("updateSpnDisplay: rawSpn = ");
                    sb.append(spn3);
                    log(sb.toString());
                    if (TextUtils.isEmpty(spn3) && !TextUtils.isEmpty(wfcVoiceSpnFormat) && !TextUtils.isEmpty(wfcDataSpnFormat)) {
                        if (this.mSS.getVoiceRegState() == 3) {
                        }
                        dataSpn2 = String.format(wfcDataSpnFormat, spn3.trim());
                        showPlmn2 = false;
                        showSpn4 = true;
                    } else if (TextUtils.isEmpty(plmn2) && !TextUtils.isEmpty(wfcVoiceSpnFormat)) {
                        plmn2.trim();
                    } else if (this.mSS.getVoiceRegState() == 3 || (showPlmn2 && TextUtils.equals(spn3, plmn2))) {
                        spn3 = null;
                        showSpn4 = false;
                    }
                    OnsDisplayParams onsDispalyParams = this.mHwServiceStateTrackerEx.getGsmOnsDisplayParams(showSpn4, showPlmn2, rule, plmn2, spn3);
                    boolean showSpn5 = onsDispalyParams.mShowSpn;
                    showPlmn = onsDispalyParams.mShowPlmn;
                    int rule2 = onsDispalyParams.mRule;
                    String plmn5 = onsDispalyParams.mPlmn;
                    String spn4 = onsDispalyParams.mSpn;
                    showWifi = onsDispalyParams.mShowWifi;
                    wifi = onsDispalyParams.mWifi;
                    boolean show_blank_ons = false;
                    hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
                    if (hwCustGsmServiceStateTracker == null) {
                        plmn3 = plmn5;
                        if (hwCustGsmServiceStateTracker.isStopUpdateName(this.mSimCardsLoaded)) {
                            show_blank_ons = true;
                        }
                    } else {
                        plmn3 = plmn5;
                    }
                    if ((!display_blank_ons || show_blank_ons) && combinedRegState == 0) {
                        log("In service , display blank ons for tracfone");
                        spn2 = " ";
                        showPlmn = true;
                        plmn3 = " ";
                        showSpn2 = false;
                    } else {
                        showSpn2 = showSpn5;
                        spn2 = spn4;
                    }
                    plmn = plmn3;
                }
            }
            showSpn3 = false;
            StringBuilder sb2 = new StringBuilder();
            showSpn4 = showSpn3;
            sb2.append("updateSpnDisplay: rawSpn = ");
            sb2.append(spn3);
            log(sb2.toString());
            if (TextUtils.isEmpty(spn3)) {
            }
            if (TextUtils.isEmpty(plmn2)) {
            }
            spn3 = null;
            showSpn4 = false;
            OnsDisplayParams onsDispalyParams2 = this.mHwServiceStateTrackerEx.getGsmOnsDisplayParams(showSpn4, showPlmn2, rule, plmn2, spn3);
            boolean showSpn52 = onsDispalyParams2.mShowSpn;
            showPlmn = onsDispalyParams2.mShowPlmn;
            int rule22 = onsDispalyParams2.mRule;
            String plmn52 = onsDispalyParams2.mPlmn;
            String spn42 = onsDispalyParams2.mSpn;
            showWifi = onsDispalyParams2.mShowWifi;
            wifi = onsDispalyParams2.mWifi;
            boolean show_blank_ons2 = false;
            hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
            if (hwCustGsmServiceStateTracker == null) {
            }
            if (!display_blank_ons) {
            }
            log("In service , display blank ons for tracfone");
            spn2 = " ";
            showPlmn = true;
            plmn3 = " ";
            showSpn2 = false;
            plmn = plmn3;
        } else {
            boolean z = false;
            String eriText = getOperatorNameFromEri();
            if (eriText != null) {
                this.mSS.setOperatorAlphaLong(eriText);
            }
            updateOperatorNameFromCarrierConfig();
            String plmn6 = this.mSS.getOperatorAlpha();
            log("updateSpnDisplay: cdma rawPlmn = " + plmn6);
            if (plmn6 != null) {
                z = true;
            }
            showPlmn = z;
            OnsDisplayParams onsDispalyParams3 = this.mHwServiceStateTrackerEx.getCdmaOnsDisplayParams();
            if (onsDispalyParams3 != null) {
                plmn6 = onsDispalyParams3.mPlmn;
                showPlmn = onsDispalyParams3.mShowPlmn;
                showWifi = onsDispalyParams3.mShowWifi;
                wifi = onsDispalyParams3.mWifi;
            }
            log("updateSpnDisplay: cdma rawPlmn = " + plmn6);
            if (!TextUtils.isEmpty(plmn6)) {
                TextUtils.isEmpty(wfcVoiceSpnFormat);
            }
            if (!display_blank_ons || (plmn6 == null && this.mSS.getState() != 0)) {
                plmn = plmn6;
            } else {
                log("In service , display blank ons for tracfone");
                plmn = " ";
            }
            if (combinedRegState == 1) {
                plmn = Resources.getSystem().getText(17040422).toString();
                log("updateSpnDisplay: radio is on but out of svc, set plmn='" + plmn + "'");
                spn2 = spn;
                dataSpn2 = dataSpn;
                showSpn2 = showSpn;
            } else {
                spn2 = spn;
                dataSpn2 = dataSpn;
                showSpn2 = showSpn;
            }
        }
        notifySpnDisplayUpdate(new CarrierDisplayNameData.Builder().setSpn(spn2).setDataSpn(dataSpn2).setShowSpn(showSpn2).setPlmn(plmn).setShowPlmn(showPlmn).build(), showWifi, wifi, regplmn);
        log("updateSpnDisplayLegacy-");
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
        if (this.mDesiredPowerState && !this.mRadioDisabledByCarrier && this.mCi.getRadioState() == 0) {
            HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
            if (hwCustGsmServiceStateTracker != null) {
                hwCustGsmServiceStateTracker.setRadioPower(this.mCi, true);
            }
            this.mCi.setRadioPower(true, null);
        } else if ((!this.mDesiredPowerState || this.mRadioDisabledByCarrier) && this.mCi.getRadioState() == 1) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely();
            } else if (!this.mImsRegistrationOnOff || this.mAlarmSwitch) {
                powerOffRadioSafely();
            } else {
                log("mImsRegistrationOnOff == true");
                Context context = this.mPhone.getContext();
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_RADIO_OFF), 67108864);
                this.mAlarmSwitch = true;
                log("Alarm setting");
                ((AlarmManager) context.getSystemService("alarm")).set(2, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
            HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker2 = this.mHwCustGsmServiceStateTracker;
            if (hwCustGsmServiceStateTracker2 != null) {
                hwCustGsmServiceStateTracker2.setRadioPower(this.mCi, false);
            }
        } else if (this.mDeviceShuttingDown && this.mCi.getRadioState() != 2) {
            this.mCi.requestShutdown(null);
        }
    }

    /* access modifiers changed from: protected */
    public void onUpdateIccAvailability() {
        UiccCardApplication newUiccApplication;
        if (this.mUiccController != null && this.mUiccApplcation != (newUiccApplication = getUiccCardApplication())) {
            IccRecords iccRecords = this.mIccRecords;
            if (iccRecords instanceof SIMRecords) {
                this.mCdnr.updateEfFromUsim(null);
            } else if (iccRecords instanceof RuimRecords) {
                this.mCdnr.updateEfFromRuim(null);
            }
            if (this.mUiccApplcation != null) {
                log("Removing stale icc objects.");
                this.mUiccApplcation.unregisterForReady(this);
                this.mUiccApplcation.unregisterForGetAdDone(this);
                IccRecords iccRecords2 = this.mIccRecords;
                if (iccRecords2 != null) {
                    iccRecords2.unregisterForRecordsLoaded(this);
                    IccRecordsEx iccRecordsEx = new IccRecordsEx();
                    iccRecordsEx.setIccRecords(this.mIccRecords);
                    this.mHwServiceStateTrackerEx.unregisterForSimRecordsEvents(iccRecordsEx);
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
                    IccRecords iccRecords3 = this.mIccRecords;
                    if (iccRecords3 != null) {
                        iccRecords3.registerForRecordsLoaded(this, 16, null);
                        IccRecordsEx iccRecordsEx2 = new IccRecordsEx();
                        iccRecordsEx2.setIccRecords(this.mIccRecords);
                        this.mHwServiceStateTrackerEx.registerForSimRecordsEvents(iccRecordsEx2);
                    }
                } else if (this.mIsSubscriptionFromRuim) {
                    registerForRuimEvents();
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
        Rlog.i(str, "[" + this.mPhone.getPhoneId() + "] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        String str = this.LOG_TAG;
        Rlog.e(str, "[" + this.mPhone.getPhoneId() + "] " + s);
    }

    @UnsupportedAppUsage
    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    @UnsupportedAppUsage
    public boolean isConcurrentVoiceAndDataAllowed() {
        if (this.mSS.getCssIndicator() == 1) {
            return isLteOrNrTechnology(this.mSS.getRilDataRadioTechnology()) && !MDOEM_WORK_MODE_IS_SRLTE;
        }
        if (!this.mPhone.isPhoneTypeGsm()) {
            return false;
        }
        if (SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true") && !this.isCurrent3GPsCsAllowed) {
            log("current not allow voice and data simultaneously by vp");
            return false;
        } else if (this.mSS.getRilDataRadioTechnology() < 3 || this.mSS.getRilDataRadioTechnology() == 16) {
            return this.mSS.getCssIndicator() == 1;
        } else {
            return true;
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
        return this.mCi.getRadioState() == 1;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    @UnsupportedAppUsage
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
        int radioState = this.mCi.getRadioState();
        if (radioState == 0) {
            setOldCsRegState(3);
            this.mNewSS.setStateOff();
            this.mNewCellIdentity = null;
            setSignalStrengthDefaultValues();
            this.mNitzState.handleNetworkCountryCodeUnavailable();
            if (this.mDeviceShuttingDown || (!modemTriggered && 18 != this.mSS.getRilDataRadioTechnology())) {
                pollStateDone();
                return;
            }
        } else if (radioState == 2) {
            setOldCsRegState(1);
            this.mNewSS.setStateOutOfService();
            this.mNewCellIdentity = null;
            setSignalStrengthDefaultValues();
            this.mNitzState.handleNetworkCountryCodeUnavailable();
            pollStateDone();
            return;
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        Message operatorMsg = obtainMessage(7, iArr);
        operatorMsg.setVsync(true);
        this.mCi.getOperator(operatorMsg);
        int[] iArr2 = this.mPollingContext;
        iArr2[0] = iArr2[0] + 1;
        this.mRegStateManagers.get(1).requestNetworkRegistrationInfo(2, obtainMessage(5, this.mPollingContext));
        int[] iArr3 = this.mPollingContext;
        iArr3[0] = iArr3[0] + 1;
        this.mRegStateManagers.get(1).requestNetworkRegistrationInfo(1, obtainMessage(4, this.mPollingContext));
        if (this.mRegStateManagers.get(2) != null) {
            int[] iArr4 = this.mPollingContext;
            iArr4[0] = iArr4[0] + 1;
            this.mRegStateManagers.get(2).requestNetworkRegistrationInfo(2, obtainMessage(6, this.mPollingContext));
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            int[] iArr5 = this.mPollingContext;
            iArr5[0] = iArr5[0] + 1;
            Message selecModeMsg = obtainMessage(14, iArr5);
            selecModeMsg.setVsync(true);
            this.mCi.getNetworkSelectionMode(selecModeMsg);
            this.mHwServiceStateTrackerEx.getLocationInfo();
        }
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null) {
            hwCustGsmServiceStateTracker.getLteFreqWithWlanCoex(this.mCi, this);
        }
    }

    private void updateOperatorProp() {
        ServiceState serviceState;
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        if (gsmCdmaPhone != null && (serviceState = this.mSS) != null) {
            gsmCdmaPhone.setSystemProperty("gsm.operator.alpha", serviceState.getOperatorAlphaLong());
        }
    }

    /* JADX INFO: Multiple debug info for r12v14 android.telephony.CellIdentity: [D('tss' android.telephony.ServiceState), D('tempCellId' android.telephony.CellIdentity)] */
    /* JADX INFO: Multiple debug info for r4v11 int: [D('transport' int), D('hasRegistered' boolean)] */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x03b3, code lost:
        if (r43.mNewSS.getRilDataRadioTechnology() == 13) goto L_0x03b8;
     */
    private void pollStateDone() {
        boolean hasOperatorNumericChanged;
        boolean updateCaByCell;
        boolean anyDataRegChanged;
        boolean hasVoiceRegStateChanged;
        boolean hasAirplaneModeOnChanged;
        boolean has4gHandoff;
        boolean hasLostMultiApnSupport;
        boolean hasNrStateChanged;
        boolean hasNrFrequencyRangeChanged;
        boolean has4gHandoff2;
        boolean hasDataRoamingOn;
        boolean hasVoiceRoamingOff;
        boolean hasVoiceRoamingOn;
        boolean hasOperatorNumericChanged2;
        boolean hasDeregistered;
        boolean z;
        int[] iArr;
        DcTracker dcTracker;
        int i;
        boolean hasMultiApnSupport;
        boolean has4gHandoff3;
        boolean hasMultiApnSupport2;
        int oldRAT;
        int newRAT;
        boolean anyDataRatChanged;
        int oldRegState;
        int newRegState;
        if (!this.mPhone.isPhoneTypeGsm()) {
            updateRoamingState();
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        processIwlanRegistrationInfo();
        if (Build.IS_DEBUGGABLE && this.mPhone.mTelephonyTester != null) {
            this.mPhone.mTelephonyTester.overrideServiceState(this.mNewSS);
        }
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 && this.mNewSS.getVoiceRegState() == 0;
        boolean hasDeregistered2 = this.mSS.getVoiceRegState() == 0 && this.mNewSS.getVoiceRegState() != 0;
        boolean hasAirplaneModeOnChanged2 = this.mSS.getVoiceRegState() != 3 && this.mNewSS.getVoiceRegState() == 3;
        SparseBooleanArray hasDataAttached = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        SparseBooleanArray hasDataDetached = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        SparseBooleanArray hasRilDataRadioTechnologyChanged = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        SparseBooleanArray hasDataRegStateChanged = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        boolean anyDataRatChanged2 = false;
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        int length = availableTransports.length;
        boolean anyDataRegChanged2 = false;
        int i2 = 0;
        while (i2 < length) {
            int transport = availableTransports[i2];
            NetworkRegistrationInfo oldNrs = this.mSS.getNetworkRegistrationInfoHw(2, transport);
            NetworkRegistrationInfo newNrs = this.mNewSS.getNetworkRegistrationInfoHw(2, transport);
            hasDataAttached.put(transport, (oldNrs == null || !oldNrs.isInService() || hasAirplaneModeOnChanged2) && newNrs != null && newNrs.isInService());
            hasDataDetached.put(transport, oldNrs != null && oldNrs.isInService() && (newNrs == null || !newNrs.isInService()));
            if (oldNrs != null) {
                oldRAT = oldNrs.getAccessNetworkTechnology();
            } else {
                oldRAT = 0;
            }
            if (newNrs != null) {
                newRAT = newNrs.getAccessNetworkTechnology();
            } else {
                newRAT = 0;
            }
            hasRilDataRadioTechnologyChanged.put(transport, oldRAT != newRAT);
            if (oldRAT != newRAT) {
                anyDataRatChanged = true;
            } else {
                anyDataRatChanged = anyDataRatChanged2;
            }
            if (oldNrs != null) {
                oldRegState = oldNrs.getRegistrationState();
            } else {
                oldRegState = 4;
            }
            if (newNrs != null) {
                newRegState = newNrs.getRegistrationState();
            } else {
                newRegState = 4;
            }
            hasDataRegStateChanged.put(transport, oldRegState != newRegState);
            if (oldRegState != newRegState) {
                anyDataRegChanged2 = true;
            }
            i2++;
            availableTransports = availableTransports;
            length = length;
            anyDataRatChanged2 = anyDataRatChanged;
        }
        boolean hasVoiceRegStateChanged2 = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
        if (this.mNewSS.getOperatorNumeric() != null) {
            hasOperatorNumericChanged = !this.mNewSS.getOperatorNumeric().equals(this.mSS.getOperatorNumeric());
        } else {
            hasOperatorNumericChanged = false;
        }
        boolean hasNrFrequencyRangeChanged2 = this.mSS.getNrFrequencyRange() != this.mNewSS.getNrFrequencyRange();
        boolean hasNrStateChanged2 = isNrStateChanged(this.mSS.getNetworkRegistrationInfoHw(2, 3), this.mNewSS.getNetworkRegistrationInfoHw(2, 3));
        boolean hasLocationChanged = !Objects.equals(this.mNewCellIdentity, this.mCellIdentity);
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null) {
            hwCustGsmServiceStateTracker.updateLTEBandWidth(this.mNewSS);
        }
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker2 = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker2 != null) {
            updateCaByCell = hwCustGsmServiceStateTracker2.isUpdateCAByCell(this.mNewSS);
        } else {
            updateCaByCell = true;
        }
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        boolean isCtVolte = gsmCdmaPhone.isCTSimCard(gsmCdmaPhone.getPhoneId()) && this.mImsRegistered;
        if (!(this.mNewSS.getDataRegState() == 0) || !updateCaByCell || isCtVolte) {
            anyDataRegChanged = anyDataRegChanged2;
        } else {
            anyDataRegChanged = anyDataRegChanged2;
            this.mRatRatcheter.ratchet(this.mSS, this.mNewSS, hasLocationChanged);
        }
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasChanged = !this.mNewSS.equals(this.mSS);
        if (hasChanged) {
            StringBuilder sb = new StringBuilder();
            hasVoiceRegStateChanged = hasVoiceRegStateChanged2;
            sb.append("Poll ServiceState done:  oldSS=[");
            sb.append(this.mSS);
            sb.append("] newSS=[");
            sb.append(this.mNewSS);
            sb.append("] oldMaxDataCalls=");
            sb.append(this.mMaxDataCalls);
            sb.append(" mNewMaxDataCalls=");
            sb.append(this.mNewMaxDataCalls);
            sb.append(" oldReasonDataDenied=");
            sb.append(this.mReasonDataDenied);
            sb.append(" mNewReasonDataDenied=");
            sb.append(this.mNewReasonDataDenied);
            log(sb.toString());
        } else {
            hasVoiceRegStateChanged = hasVoiceRegStateChanged2;
            log("Poll ServiceState done: no change");
        }
        boolean hasVoiceRoamingOn2 = !this.mSS.getVoiceRoaming() && this.mNewSS.getVoiceRoaming();
        boolean hasVoiceRoamingOff2 = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean hasDataRoamingOn2 = !this.mSS.getDataRoaming() && this.mNewSS.getDataRoaming();
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        boolean hasRejectCauseChanged = this.mRejectCode != this.mNewRejectCode;
        boolean hasCssIndicatorChanged = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        boolean hasLacChanged = false;
        if (this.mPhone.isPhoneTypeGsm()) {
            CellIdentity cellIdentity = this.mNewCellIdentity;
            boolean isNewCellIdentityValid = cellIdentity != null && (cellIdentity.asCellLocation() instanceof GsmCellLocation);
            hasAirplaneModeOnChanged = hasAirplaneModeOnChanged2;
            CellIdentity cellIdentity2 = this.mCellIdentity;
            boolean isCellIdentityValid = cellIdentity2 != null && (cellIdentity2.asCellLocation() instanceof GsmCellLocation);
            if (isNewCellIdentityValid && isCellIdentityValid) {
                hasLacChanged = ((GsmCellLocation) this.mNewCellIdentity.asCellLocation()).getLac() != ((GsmCellLocation) this.mCellIdentity.asCellLocation()).getLac();
            }
        } else {
            hasAirplaneModeOnChanged = hasAirplaneModeOnChanged2;
        }
        if (this.mPhone.isPhoneTypeCdmaLte()) {
            boolean has4gHandoff4 = this.mNewSS.getDataRegState() == 0 && ((ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mNewSS.getRilDataRadioTechnology() == 13) || (this.mSS.getRilDataRadioTechnology() == 13 && ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())));
            if (!ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())) {
                has4gHandoff3 = has4gHandoff4;
            } else {
                has4gHandoff3 = has4gHandoff4;
            }
            if (!ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mSS.getRilDataRadioTechnology() != 13) {
                hasMultiApnSupport2 = true;
                has4gHandoff2 = has4gHandoff3;
                has4gHandoff = hasNrFrequencyRangeChanged2;
                hasNrFrequencyRangeChanged = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
                hasLostMultiApnSupport = hasNrStateChanged2;
                hasNrStateChanged = hasMultiApnSupport2;
            }
            hasMultiApnSupport2 = false;
            has4gHandoff2 = has4gHandoff3;
            has4gHandoff = hasNrFrequencyRangeChanged2;
            hasNrFrequencyRangeChanged = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
            hasLostMultiApnSupport = hasNrStateChanged2;
            hasNrStateChanged = hasMultiApnSupport2;
        } else {
            has4gHandoff2 = false;
            has4gHandoff = hasNrFrequencyRangeChanged2;
            hasNrFrequencyRangeChanged = false;
            hasLostMultiApnSupport = hasNrStateChanged2;
            hasNrStateChanged = false;
        }
        int[] availableTransports2 = this.mTransportManager.getAvailableTransports();
        int length2 = availableTransports2.length;
        int i3 = 0;
        while (i3 < length2) {
            boolean hasRilRadioTechnologyChanged = hasRilVoiceRadioTechnologyChanged || hasRilDataRadioTechnologyChanged.get(availableTransports2[i3]);
            HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker3 = this.mHwCustGsmServiceStateTracker;
            if (hwCustGsmServiceStateTracker3 != null) {
                hasMultiApnSupport = hasNrStateChanged;
                hwCustGsmServiceStateTracker3.tryClearRejCause(this.mNewSS, hasRilRadioTechnologyChanged, this);
            } else {
                hasMultiApnSupport = hasNrStateChanged;
            }
            i3++;
            length2 = length2;
            availableTransports2 = availableTransports2;
            hasNrStateChanged = hasMultiApnSupport;
        }
        log("pollStateDone: hasRegistered = " + hasRegistered + " hasDeregistered = " + hasDeregistered2 + " hasDataAttached = " + hasDataAttached + " hasDataDetached = " + hasDataDetached + " hasDataRegStateChanged = " + hasDataRegStateChanged + " hasRilVoiceRadioTechnologyChanged = " + hasRilVoiceRadioTechnologyChanged + " hasRilDataRadioTechnologyChanged = " + hasRilDataRadioTechnologyChanged + " hasChanged = " + hasChanged + " hasVoiceRoamingOn = " + hasVoiceRoamingOn2 + " hasVoiceRoamingOff = " + hasVoiceRoamingOff2 + " hasDataRoamingOn =" + hasDataRoamingOn2 + " hasDataRoamingOff = " + hasDataRoamingOff + " hasLocationChanged = " + hasLocationChanged + " has4gHandoff = " + has4gHandoff2 + " hasMultiApnSupport = " + hasNrStateChanged + " hasLostMultiApnSupport = " + hasNrFrequencyRangeChanged + " hasCssIndicatorChanged = " + hasCssIndicatorChanged + " hasNrFrequencyRangeChanged = " + has4gHandoff + " hasNrStateChanged = " + hasLostMultiApnSupport + " hasAirplaneModeOnlChanged = " + hasAirplaneModeOnChanged + " hasOperatorNumericChanged" + hasOperatorNumericChanged);
        if (hasVoiceRegStateChanged || anyDataRegChanged) {
            if (this.mPhone.isPhoneTypeGsm()) {
                i = EventLogTags.GSM_SERVICE_STATE_CHANGE;
            } else {
                i = EventLogTags.CDMA_SERVICE_STATE_CHANGE;
            }
            hasDataRoamingOn = hasDataRoamingOn2;
            hasVoiceRoamingOff = hasVoiceRoamingOff2;
            EventLog.writeEvent(i, Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState()));
        } else {
            hasVoiceRoamingOff = hasVoiceRoamingOff2;
            hasDataRoamingOn = hasDataRoamingOn2;
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (hasRilVoiceRadioTechnologyChanged) {
                log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + getCidFromCellIdentity(this.mNewCellIdentity));
            }
            if (hasCssIndicatorChanged) {
                this.mPhone.notifyDataConnection();
            }
            if (hasChanged && hasRegistered && (dcTracker = this.mPhone.getDcTracker(1)) != null && dcTracker.getHwCustDcTracker() != null) {
                dcTracker.getHwCustDcTracker().setMPDNByNetwork(this.mNewSS.getOperatorNumeric());
            }
            this.mReasonDataDenied = this.mNewReasonDataDenied;
            this.mMaxDataCalls = this.mNewMaxDataCalls;
            this.mRejectCode = this.mNewRejectCode;
        }
        int[] availableTransports3 = this.mTransportManager.getAvailableTransports();
        int length3 = availableTransports3.length;
        int i4 = 0;
        while (i4 < length3) {
            int transport2 = availableTransports3[i4];
            if (hasRegistered || hasDataAttached.get(transport2)) {
                iArr = availableTransports3;
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(10));
            } else {
                iArr = availableTransports3;
            }
            i4++;
            length3 = length3;
            availableTransports3 = iArr;
        }
        ServiceState oldMergedSS = new ServiceState(this.mPhone.getServiceState());
        if (isNeedDelayUpdateRegisterStateDone(this.mSS, this.mNewSS)) {
            this.mNewSS.setStateOutOfService();
            return;
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellIdentity tempCellId = this.mCellIdentity;
        this.mCellIdentity = this.mNewCellIdentity;
        this.mNewCellIdentity = tempCellId;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (anyDataRatChanged2) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            hasVoiceRoamingOn = hasVoiceRoamingOn2;
            StatsLog.write(76, ServiceState.rilRadioTechnologyToNetworkType(this.mSS.getRilDataRadioTechnology()), this.mPhone.getPhoneId());
        } else {
            hasVoiceRoamingOn = hasVoiceRoamingOn2;
        }
        if (hasRegistered || hasOperatorNumericChanged) {
            this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
            this.mNetworkAttachedRegistrants.notifyRegistrants();
            if (CLEAR_NITZ_WHEN_REG) {
                hasOperatorNumericChanged2 = hasOperatorNumericChanged;
                if (SystemClock.elapsedRealtime() - this.mLastReceivedNITZReferenceTime > 5000) {
                    this.mNitzState.handleNetworkAvailable();
                }
            } else {
                hasOperatorNumericChanged2 = hasOperatorNumericChanged;
            }
        } else {
            hasOperatorNumericChanged2 = hasOperatorNumericChanged;
        }
        if (hasDeregistered2) {
            if (this.mPhone.isPhoneTypeCdma() && SystemProperties.getBoolean("ro.config_hw_doubletime", false)) {
                String mccmnc = this.mSS.getOperatorNumeric();
                if (mccmnc != null) {
                    Settings.System.putString(this.mCr, "last_registed_mcc", mccmnc.substring(0, 3));
                }
            }
            this.mNetworkDetachedRegistrants.notifyRegistrants();
        }
        if (hasRejectCauseChanged) {
            setNotification(2001);
        }
        if (hasChanged) {
            updateSpnDisplay();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
            if (!this.mPhone.isPhoneTypeCdma()) {
                if (VSimUtilsInner.isVSimSlot(this.mPhone.mPhoneId)) {
                    try {
                        SystemProperties.set("gsm.operator.alpha.vsim", this.mSS.getOperatorAlphaLong());
                    } catch (RuntimeException e) {
                        loge("set system property failed");
                    }
                } else {
                    updateOperatorProp();
                }
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                judgeToLaunchCsgPeriodicSearchTimer();
            }
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (!this.mPhone.isPhoneTypeGsm() && isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getCdmaSystemId());
            }
            if (VSimUtilsInner.isVSimSlot(this.mPhone.mPhoneId)) {
                SystemProperties.set("gsm.operator.numeric.vsim", operatorNumeric);
                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            } else {
                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            }
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                log("operatorNumeric " + operatorNumeric + " is invalid");
                this.mLocaleTracker.updateOperatorNumeric(PhoneConfigurationManager.SSSS);
                if (VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId())) {
                    SystemProperties.set("gsm.operator.iso-country.vsim", PhoneConfigurationManager.SSSS);
                }
            } else if (this.mSS.getRilDataRadioTechnology() != 18) {
                if (!this.mPhone.isPhoneTypeGsm()) {
                    setOperatorIdd(operatorNumeric);
                }
                this.mLocaleTracker.updateOperatorNumeric(operatorNumeric);
                if (VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId())) {
                    SystemProperties.set("gsm.operator.iso-country.vsim", operatorNumeric);
                }
            }
            int phoneId = this.mPhone.getPhoneId();
            if (this.mPhone.isPhoneTypeGsm()) {
                z = this.mSS.getVoiceRoaming();
            } else {
                z = this.mSS.getVoiceRoaming() || this.mSS.getDataRoaming();
            }
            tm.setNetworkRoamingForPhone(phoneId, z);
            HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker4 = this.mHwCustGsmServiceStateTracker;
            if (hwCustGsmServiceStateTracker4 != null) {
                hwCustGsmServiceStateTracker4.setLTEUsageForRomaing(this.mSS.getVoiceRoaming());
            }
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            ServiceState ss = this.mPhone.getServiceState();
            if (!oldMergedSS.equals(ss)) {
                if (ss.getDataRegState() == 0) {
                    updateNrStateFromPhysicalChannelConfigs(this.mLastPhysicalChannelConfigList, ss);
                    updateDataRatByConfig(ss.getRilDataRadioTechnology(), ss, this.mPhone);
                }
                this.mPhone.notifyServiceStateChanged(ss);
            }
            this.mPhone.getContext().getContentResolver().insert(Telephony.ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), Telephony.ServiceStateTable.getContentValuesForServiceState(this.mSS));
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
        }
        boolean shouldLogAttachedChange = false;
        boolean shouldLogRatChange = false;
        if (hasRegistered || hasDeregistered2) {
            shouldLogAttachedChange = true;
        }
        if (has4gHandoff2) {
            this.mAttachedRegistrants.get(1).notifyRegistrants();
            shouldLogAttachedChange = true;
        }
        if (hasRilVoiceRadioTechnologyChanged) {
            shouldLogRatChange = true;
            notifySignalStrength();
        }
        int[] availableTransports4 = this.mTransportManager.getAvailableTransports();
        int length4 = availableTransports4.length;
        boolean shouldLogRatChange2 = shouldLogRatChange;
        boolean shouldLogAttachedChange2 = shouldLogAttachedChange;
        int i5 = 0;
        while (i5 < length4) {
            int transport3 = availableTransports4[i5];
            if (hasRilDataRadioTechnologyChanged.get(transport3)) {
                shouldLogRatChange2 = true;
                notifySignalStrength();
            }
            if (hasDataRegStateChanged.get(transport3) || hasRilDataRadioTechnologyChanged.get(transport3)) {
                notifyDataRegStateRilRadioTechnologyChanged(transport3);
                hasDeregistered = hasDeregistered2;
                this.mPhone.notifyDataConnection();
            } else {
                hasDeregistered = hasDeregistered2;
            }
            if (hasDataAttached.get(transport3)) {
                shouldLogAttachedChange2 = true;
                if (this.mAttachedRegistrants.get(transport3) != null) {
                    this.mAttachedRegistrants.get(transport3).notifyRegistrants();
                }
            }
            if (hasDataDetached.get(transport3)) {
                shouldLogAttachedChange2 = true;
                if (this.mDetachedRegistrants.get(transport3) != null) {
                    this.mDetachedRegistrants.get(transport3).notifyRegistrants();
                }
            }
            i5++;
            hasRegistered = hasRegistered;
            hasDeregistered2 = hasDeregistered;
        }
        if (shouldLogAttachedChange2) {
            logAttachChange();
        }
        if (shouldLogRatChange2) {
            logRatChange();
        }
        if (hasVoiceRegStateChanged || hasRilVoiceRadioTechnologyChanged) {
            notifyVoiceRegStateRilRadioTechnologyChanged();
        }
        if (hasVoiceRoamingOn || hasVoiceRoamingOff || hasDataRoamingOn || hasDataRoamingOff) {
            logRoamingChange();
        }
        if (hasVoiceRoamingOn) {
            this.mHwServiceStateTrackerEx.sendTimeZoneSelectionNotification();
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOn) {
            this.mHwServiceStateTrackerEx.sendTimeZoneSelectionNotification();
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged(getCellLocation());
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (hasLacChanged) {
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

    private String getOperatorNameFromEri() {
        IccRecords iccRecords;
        String eriText = null;
        if (this.mPhone.isPhoneTypeCdma()) {
            if (this.mCi.getRadioState() != 1 || this.mIsSubscriptionFromRuim) {
                return null;
            }
            if (this.mSS.getVoiceRegState() == 0) {
                return this.mPhone.getCdmaEriText();
            }
            return this.mPhone.getContext().getText(17041159).toString();
        } else if (!this.mPhone.isPhoneTypeCdmaLte()) {
            return null;
        } else {
            if (!((this.mUiccController.getUiccCard(getPhoneId()) == null || this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) ? false : true) && this.mCi.getRadioState() == 1 && this.mEriManager.isEriFileLoaded() && ((!ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) || this.mPhone.getContext().getResources().getBoolean(17891337)) && !this.mIsSubscriptionFromRuim)) {
                eriText = this.mSS.getOperatorAlpha();
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else if (this.mSS.getVoiceRegState() == 3) {
                    eriText = getServiceProviderName();
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText = this.mPhone.getContext().getText(17041159).toString();
                }
            }
            UiccCardApplication uiccCardApplication = this.mUiccApplcation;
            if (uiccCardApplication == null || uiccCardApplication.getState() != IccCardApplicationStatus.AppState.APPSTATE_READY || this.mIccRecords == null || getCombinedRegState(this.mSS) != 0 || ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology())) {
                return eriText;
            }
            boolean showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
            int iconIndex = this.mSS.getCdmaEriIconIndex();
            if (!showSpn || iconIndex != 1 || !isInHomeSidNid(this.mSS.getCdmaSystemId(), this.mSS.getCdmaNetworkId()) || (iccRecords = this.mIccRecords) == null || TextUtils.isEmpty(iccRecords.getServiceProviderName())) {
                return eriText;
            }
            return getServiceProviderName();
        }
    }

    public String getServiceProviderName() {
        String operatorBrandOverride = getOperatorBrandOverride();
        if (!TextUtils.isEmpty(operatorBrandOverride)) {
            return operatorBrandOverride;
        }
        IccRecords iccRecords = this.mIccRecords;
        String carrierName = iccRecords != null ? iccRecords.getServiceProviderName() : PhoneConfigurationManager.SSSS;
        PersistableBundle config = getCarrierConfig();
        if (config.getBoolean("carrier_name_override_bool") || TextUtils.isEmpty(carrierName)) {
            return config.getString("carrier_name_string");
        }
        return carrierName;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public int getCarrierNameDisplayBitmask(ServiceState ss) {
        boolean isRoaming;
        PersistableBundle config = getCarrierConfig();
        if (!TextUtils.isEmpty(getOperatorBrandOverride())) {
            return 1;
        }
        if (TextUtils.isEmpty(getServiceProviderName())) {
            return 2;
        }
        boolean useRoamingFromServiceState = config.getBoolean("spn_display_rule_use_roaming_from_service_state_bool");
        IccRecords iccRecords = this.mIccRecords;
        int carrierDisplayNameConditionFromSim = iccRecords == null ? 0 : iccRecords.getCarrierNameDisplayCondition();
        if (useRoamingFromServiceState) {
            isRoaming = ss.getRoaming();
        } else {
            IccRecords iccRecords2 = this.mIccRecords;
            isRoaming = !ArrayUtils.contains(iccRecords2 != null ? iccRecords2.getHomePlmns() : null, ss.getOperatorNumeric());
        }
        if (isRoaming) {
            if ((carrierDisplayNameConditionFromSim & 2) == 2) {
                return 2 | 1;
            }
            return 2;
        } else if ((carrierDisplayNameConditionFromSim & 1) == 1) {
            return 1 | 2;
        } else {
            return 1;
        }
    }

    private String getOperatorBrandOverride() {
        UiccProfile profile;
        UiccCard card = this.mPhone.getUiccCard();
        if (card == null || (profile = card.getUiccProfile()) == null) {
            return null;
        }
        return profile.getOperatorBrandOverride();
    }

    @UnsupportedAppUsage
    private boolean isInHomeSidNid(int sid, int nid) {
        if (isSidsAllZeros() || this.mHomeSystemId.length != this.mHomeNetworkId.length || sid == 0) {
            return true;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mHomeSystemId;
            if (i >= iArr.length) {
                return false;
            }
            if (iArr[i] == sid) {
                int[] iArr2 = this.mHomeNetworkId;
                if (iArr2[i] == 0 || iArr2[i] == 65535 || nid == 0 || nid == 65535 || iArr2[i] == nid) {
                    break;
                }
            }
            i++;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    private boolean isInvalidOperatorNumeric(String operatorNumeric) {
        return operatorNumeric == null || operatorNumeric.length() < 5 || operatorNumeric.startsWith(INVALID_MCC);
    }

    @UnsupportedAppUsage
    private String fixUnknownMcc(String operatorNumeric, int sid) {
        boolean isNitzTimeZone;
        TimeZone tzone;
        TimeZone tzone2;
        if (sid <= 0) {
            return operatorNumeric;
        }
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
                sb.append((Object) (tzone2 == null ? tzone2 : tzone2.getID()));
                log(sb.toString());
            }
            isNitzTimeZone = false;
            tzone = tzone2;
        }
        int utcOffsetHours = 0;
        if (tzone != null) {
            utcOffsetHours = tzone.getRawOffset() / MS_PER_HOUR;
        }
        NitzData nitzData = this.mNitzState.getCachedNitzData();
        int i = 1;
        boolean isDst = nitzData != null && nitzData.isDst();
        HbpcdUtils hbpcdUtils = this.mHbpcdUtils;
        if (!isDst) {
            i = 0;
        }
        int mcc = hbpcdUtils.getMcc(sid, utcOffsetHours, i, isNitzTimeZone);
        if (mcc <= 0) {
            return operatorNumeric;
        }
        return Integer.toString(mcc) + DEFAULT_MNC;
    }

    @UnsupportedAppUsage
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
        return (!TextUtils.isEmpty(spn) && spn.equalsIgnoreCase(s.getOperatorAlphaLong())) || (!TextUtils.isEmpty(spn) && spn.equalsIgnoreCase(s.getOperatorAlphaShort()));
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
        String[] numericArray = getCarrierConfig().getStringArray("roaming_operator_string_array");
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
            UiccCardApplication uiccCardApplication = this.mUiccApplcation;
            if (uiccCardApplication != null && uiccCardApplication.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY) {
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

    public CellLocation getCellLocation() {
        CellIdentity cellIdentity = this.mCellIdentity;
        if (cellIdentity != null) {
            return cellIdentity.asCellLocation();
        }
        CellLocation cl = getCellLocationFromCellInfo(getAllCellInfo());
        if (cl != null) {
            return cl;
        }
        return this.mPhone.getPhoneType() == 2 ? new CdmaCellLocation() : new GsmCellLocation();
    }

    public void requestCellLocation(WorkSource workSource, Message rspMsg) {
        CellIdentity cellIdentity = this.mCellIdentity;
        if (cellIdentity != null) {
            AsyncResult.forMessage(rspMsg, cellIdentity.asCellLocation(), (Throwable) null);
            rspMsg.sendToTarget();
            return;
        }
        requestAllCellInfo(workSource, obtainMessage(56, rspMsg));
    }

    private static CellLocation getCellLocationFromCellInfo(List<CellInfo> info) {
        CellLocation cl = null;
        if (info == null || info.size() <= 0) {
            return null;
        }
        CellIdentity fallbackLteCid = null;
        Iterator<CellInfo> it = info.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            CellIdentity c = it.next().getCellIdentity();
            if (!(c instanceof CellIdentityLte) || fallbackLteCid != null) {
                if (getCidFromCellIdentity(c) != -1) {
                    cl = c.asCellLocation();
                    break;
                }
            } else if (getCidFromCellIdentity(c) != -1) {
                fallbackLteCid = c;
            }
        }
        if (cl != null || fallbackLteCid == null) {
            return cl;
        }
        return fallbackLteCid.asCellLocation();
    }

    private void setTimeFromNITZString(String nitzString, long nitzReceiveTime) {
        long start = SystemClock.elapsedRealtime();
        String str = this.LOG_TAG;
        Rlog.d(str, "NITZ: " + nitzString + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
        NitzData newNitzData = NitzData.parse(nitzString);
        if (newNitzData != null) {
            try {
                this.mNitzState.handleNitzReceived(new TimestampedValue<>(nitzReceiveTime, newNitzData));
            } finally {
                long end = SystemClock.elapsedRealtime();
                String str2 = this.LOG_TAG;
                Rlog.d(str2, "NITZ: end=" + end + " dur=" + (end - start));
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

    /* JADX INFO: Multiple debug info for r4v24 long: [D('dataSubId' long), D('isSetNotification' boolean)] */
    @VisibleForTesting
    public void setNotification(int notifyType) {
        CharSequence charSequence;
        CharSequence charSequence2;
        CharSequence charSequence3;
        CharSequence charSequence4;
        int notifyType2 = notifyType;
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null && hwCustGsmServiceStateTracker.isCsPopShow(notifyType2)) {
            log("cs notification no need to send" + notifyType2);
        } else if (SystemProperties.getBoolean("ro.hwpp.cell_access_report", false)) {
            log("setNotification: create notification " + notifyType2);
            if (!SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
                loge("cannot setNotification on invalid subid mSubId=" + this.mSubId);
                return;
            }
            Context context = this.mPhone.getContext();
            SubscriptionInfo info = this.mSubscriptionController.getActiveSubscriptionInfo(this.mPhone.getSubId(), context.getOpPackageName());
            if (info != null) {
                if (!info.isOpportunistic() || info.getGroupUuid() == null) {
                    if (!SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
                        loge("cannot setNotification on invalid subid mSubId=" + this.mSubId);
                        return;
                    } else if (!context.getResources().getBoolean(17891572)) {
                        log("Ignore all the notifications");
                        return;
                    } else {
                        PersistableBundle bundle = getCarrierConfig();
                        if (!bundle.getBoolean("disable_voice_barring_notification_bool", false) || !(notifyType2 == 1003 || notifyType2 == 1005 || notifyType2 == 1006)) {
                            boolean autoCancelCsRejectNotification = bundle.getBoolean("carrier_auto_cancel_cs_notification", false);
                            CharSequence details = PhoneConfigurationManager.SSSS;
                            CharSequence title = PhoneConfigurationManager.SSSS;
                            int notificationId = CS_NOTIFICATION;
                            int icon = 17301642;
                            boolean multipleSubscriptions = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getPhoneCount() > 1;
                            int simNumber = this.mSubscriptionController.getSlotIndex(this.mSubId) + 1;
                            if (notifyType2 != 2001) {
                                switch (notifyType2) {
                                    case 1001:
                                        if (((long) SubscriptionManager.getDefaultDataSubscriptionId()) == ((long) this.mPhone.getSubId())) {
                                            notificationId = PS_NOTIFICATION;
                                            title = context.getText(17039474);
                                            if (multipleSubscriptions) {
                                                charSequence = context.getString(17039483, Integer.valueOf(simNumber));
                                            } else {
                                                charSequence = context.getText(17039482);
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
                                        title = context.getText(17039472);
                                        if (multipleSubscriptions) {
                                            charSequence2 = context.getString(17039483, Integer.valueOf(simNumber));
                                        } else {
                                            charSequence2 = context.getText(17039482);
                                        }
                                        details = charSequence2;
                                        break;
                                    case 1005:
                                        title = context.getText(17039478);
                                        if (multipleSubscriptions) {
                                            charSequence3 = context.getString(17039483, Integer.valueOf(simNumber));
                                        } else {
                                            charSequence3 = context.getText(17039482);
                                        }
                                        details = charSequence3;
                                        break;
                                    case 1006:
                                        title = context.getText(17039476);
                                        if (multipleSubscriptions) {
                                            charSequence4 = context.getString(17039483, Integer.valueOf(simNumber));
                                        } else {
                                            charSequence4 = context.getText(17039482);
                                        }
                                        details = charSequence4;
                                        break;
                                }
                            } else {
                                notificationId = 111;
                                int resId = selectResourceForRejectCode(this.mRejectCode, multipleSubscriptions);
                                if (resId != 0) {
                                    icon = 17303538;
                                    title = context.getString(resId, Integer.valueOf(simNumber));
                                    details = null;
                                } else if (autoCancelCsRejectNotification) {
                                    notifyType2 = 2002;
                                } else {
                                    loge("setNotification: mRejectCode=" + this.mRejectCode + " is not handled.");
                                    return;
                                }
                            }
                            log("setNotification, create notification, notifyType: " + notifyType2 + ", title: " + ((Object) title) + ", details: " + ((Object) details) + ", subId: " + this.mSubId);
                            this.mNotification = new Notification.Builder(context).setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(icon).setTicker(title).setColor(context.getResources().getColor(17170460)).setContentTitle(title).setStyle(new Notification.BigTextStyle().bigText(details)).setContentText(details).setChannel(NotificationChannelController.CHANNEL_ID_ALERT).build();
                            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                            if (notifyType2 == 1002 || notifyType2 == 1004 || notifyType2 == 2002) {
                                notificationManager.cancel(Integer.toString(this.mSubId), notificationId);
                                return;
                            }
                            boolean show = false;
                            if (this.mSS.isEmergencyOnly() && notifyType2 == 1006) {
                                show = true;
                            } else if (notifyType2 == 2001) {
                                show = true;
                            } else if (this.mSS.getState() == 0) {
                                show = true;
                            }
                            if (show) {
                                notificationManager.notify(Integer.toString(this.mSubId), notificationId, this.mNotification);
                                return;
                            }
                            return;
                        }
                        log("Voice/emergency call barred notification disabled");
                        return;
                    }
                }
            }
            log("cannot setNotification on invisible subid mSubId=" + this.mSubId);
        }
    }

    private int selectResourceForRejectCode(int rejCode, boolean multipleSubscriptions) {
        int rejResourceId;
        int rejResourceId2;
        int rejResourceId3;
        int rejResourceId4;
        if (rejCode == 1) {
            if (multipleSubscriptions) {
                rejResourceId = 17040612;
            } else {
                rejResourceId = 17040611;
            }
            return rejResourceId;
        } else if (rejCode == 2) {
            if (multipleSubscriptions) {
                rejResourceId2 = 17040618;
            } else {
                rejResourceId2 = 17040617;
            }
            return rejResourceId2;
        } else if (rejCode == 3) {
            if (multipleSubscriptions) {
                rejResourceId3 = 17040616;
            } else {
                rejResourceId3 = 17040615;
            }
            return rejResourceId3;
        } else if (rejCode != 6) {
            return 0;
        } else {
            if (multipleSubscriptions) {
                rejResourceId4 = 17040614;
            } else {
                rejResourceId4 = 17040613;
            }
            return rejResourceId4;
        }
    }

    private UiccCardApplication getUiccCardApplication() {
        if (VSimUtilsInner.isHisiVSimSlot(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.getVSimUiccCardApplication(1);
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 2);
    }

    private void queueNextSignalStrengthPoll() {
        if (!this.mDontPollSignalStrength) {
            UiccCard uiccCard = UiccController.getInstance().getUiccCard(getPhoneId());
            if (uiccCard == null || uiccCard.getCardState() == IccCardStatus.CardState.CARDSTATE_ABSENT) {
                log("Not polling signal strength due to absence of SIM");
            } else if (this.mDefaultDisplayState == 2) {
                Message msg = obtainMessage();
                msg.what = 10;
                sendMessageDelayed(msg, 20000);
            }
        }
    }

    private void notifyCdmaSubscriptionInfoReady() {
        if (this.mCdmaForSubscriptionInfoReadyRegistrants != null) {
            log("CDMA_SUBSCRIPTION: call notifyRegistrants()");
            this.mCdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
        }
    }

    public void registerForDataConnectionAttached(int transport, Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        if (this.mAttachedRegistrants.get(transport) == null) {
            this.mAttachedRegistrants.put(transport, new RegistrantList());
        }
        this.mAttachedRegistrants.get(transport).add(r);
        ServiceState serviceState = this.mSS;
        if (serviceState != null) {
            NetworkRegistrationInfo netRegState = serviceState.getNetworkRegistrationInfoHw(2, transport);
            if (netRegState == null || netRegState.isInService()) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForDataConnectionAttached(int transport, Handler h) {
        if (this.mAttachedRegistrants.get(transport) != null) {
            this.mAttachedRegistrants.get(transport).remove(h);
        }
    }

    public void registerForDataConnectionDetached(int transport, Handler h, int what, Object obj) {
        NetworkRegistrationInfo netRegState;
        Registrant r = new Registrant(h, what, obj);
        if (this.mDetachedRegistrants.get(transport) == null) {
            this.mDetachedRegistrants.put(transport, new RegistrantList());
        }
        this.mDetachedRegistrants.get(transport).add(r);
        ServiceState serviceState = this.mSS;
        if (serviceState != null && (netRegState = serviceState.getNetworkRegistrationInfoHw(2, transport)) != null && !netRegState.isInService()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionDetached(int transport, Handler h) {
        if (this.mDetachedRegistrants.get(transport) != null) {
            this.mDetachedRegistrants.get(transport).remove(h);
        }
    }

    public void registerForVoiceRegStateOrRatChanged(Handler h, int what, Object obj) {
        this.mVoiceRegStateOrRatChangedRegistrants.add(new Registrant(h, what, obj));
        notifyVoiceRegStateRilRadioTechnologyChanged();
    }

    public void unregisterForVoiceRegStateOrRatChanged(Handler h) {
        this.mVoiceRegStateOrRatChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void registerForDataRegStateOrRatChanged(int transport, Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        if (this.mDataRegStateOrRatChangedRegistrants.get(transport) == null) {
            this.mDataRegStateOrRatChangedRegistrants.put(transport, new RegistrantList());
        }
        this.mDataRegStateOrRatChangedRegistrants.get(transport).add(r);
        notifyDataRegStateRilRadioTechnologyChanged(transport);
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void unregisterForDataRegStateOrRatChanged(int transport, Handler h) {
        if (this.mDataRegStateOrRatChangedRegistrants.get(transport) != null) {
            this.mDataRegStateOrRatChangedRegistrants.get(transport).remove(h);
        }
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

    public void registerForImsCapabilityChanged(Handler h, int what, Object obj) {
        this.mImsCapabilityChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsCapabilityChanged(Handler h) {
        this.mImsCapabilityChangedRegistrants.remove(h);
    }

    public void powerOffRadioSafely() {
        synchronized (this) {
            int airplaneMode = Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", -1);
            if (RESET_PROFILE && this.mPhone.getContext() != null && airplaneMode == 0) {
                Rlog.d(this.LOG_TAG, "powerOffRadioSafely, it is not airplaneMode, resetProfile.");
                this.mCi.resetProfile(null);
            }
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                int dds = SubscriptionManager.getDefaultDataSubscriptionId();
                boolean areAllDataDisconnected = false;
                if ((HW_FAST_SET_RADIO_OFF || !IS_QCOM_PLATFORM) && this.mPhone.getDcTracker(1) != null) {
                    areAllDataDisconnected = this.mPhone.getDcTracker(1).isDisconnectedOrConnecting();
                }
                if (!areAllDataDisconnected) {
                    if (this.mPhone.areAllDataDisconnected()) {
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
                    if (imsPhone != null && imsPhone.isInCallHw()) {
                        imsPhone.getForegroundCall().hangupIfAlive();
                        imsPhone.getBackgroundCall().hangupIfAlive();
                        imsPhone.getRingingCall().hangupIfAlive();
                    }
                    int[] availableTransports = this.mTransportManager.getAvailableTransports();
                    for (int transport : availableTransports) {
                        if (this.mPhone.getDcTracker(transport) != null) {
                            this.mPhone.getDcTracker(transport).cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        }
                    }
                    if (dds != this.mPhone.getSubId() && !ProxyController.getInstance().areAllDataDisconnected(dds)) {
                        log("Data is active on DDS.  Wait for all data disconnect");
                        ProxyController.getInstance().registerForAllDataDisconnected(dds, this, 49);
                        this.mPendingRadioPowerOffAfterDataOff = true;
                    }
                    Message msg = Message.obtain(this);
                    msg.what = 38;
                    int i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                    this.mPendingRadioPowerOffAfterDataOffTag = i;
                    msg.arg1 = i;
                    int delayTime = this.mHwServiceStateTrackerEx.getDataOffTime(airplaneMode);
                    if (sendMessageDelayed(msg, (long) delayTime)) {
                        log("Wait upto " + delayTime + " ms for data to disconnect, then turn off radio.");
                        acquireWakeLock();
                        this.mPendingRadioPowerOffAfterDataOff = true;
                    } else {
                        log("Cannot send delayed Msg, turn off radio right away.");
                        hangupAndPowerOff();
                        this.mPendingRadioPowerOffAfterDataOff = false;
                    }
                }
                int[] availableTransports2 = this.mTransportManager.getAvailableTransports();
                for (int transport2 : availableTransports2) {
                    if (this.mPhone.getDcTracker(transport2) != null) {
                        this.mPhone.getDcTracker(transport2).cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                    }
                }
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
            log("Process pending request to turn radio off.");
            this.mHwServiceStateTrackerEx.delaySendDetachAfterDataOff();
            this.mPendingRadioPowerOffAfterDataOffTag++;
            hangupAndPowerOff();
            this.mPendingRadioPowerOffAfterDataOff = false;
            return true;
        }
    }

    private boolean containsEarfcnInEarfcnRange(ArrayList<Pair<Integer, Integer>> earfcnPairList, int earfcn) {
        if (earfcnPairList == null) {
            return false;
        }
        Iterator<Pair<Integer, Integer>> it = earfcnPairList.iterator();
        while (it.hasNext()) {
            Pair<Integer, Integer> earfcnPair = it.next();
            if (earfcn >= ((Integer) earfcnPair.first).intValue() && earfcn <= ((Integer) earfcnPair.second).intValue()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Pair<Integer, Integer>> convertEarfcnStringArrayToPairList(String[] earfcnsList) {
        int earfcnStart;
        int earfcnEnd;
        ArrayList<Pair<Integer, Integer>> earfcnPairList = new ArrayList<>();
        if (earfcnsList != null) {
            for (String str : earfcnsList) {
                try {
                    String[] earfcns = str.split("-");
                    if (earfcns.length != 2 || (earfcnStart = Integer.parseInt(earfcns[0])) > (earfcnEnd = Integer.parseInt(earfcns[1]))) {
                        return null;
                    }
                    earfcnPairList.add(new Pair<>(Integer.valueOf(earfcnStart), Integer.valueOf(earfcnEnd)));
                } catch (PatternSyntaxException e) {
                    return null;
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
        return earfcnPairList;
    }

    private void onCarrierConfigChanged() {
        PersistableBundle config = getCarrierConfig();
        log("CarrierConfigChange " + config);
        this.mEriManager.loadEriFile();
        this.mCdnr.updateEfForEri(getOperatorNameFromEri());
        updateLteEarfcnLists(config);
        updateReportingCriteria(config);
        updateOperatorNamePattern(config);
        this.mCdnr.updateEfFromCarrierConfig(config);
        pollState();
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
                if (containsEarfcnInEarfcnRange(this.mEarfcnPairListForRsrpBoost, lteEarfcn)) {
                    serviceState.setLteEarfcnRsrpBoost(this.mLteRsrpBoost);
                }
            }
            serviceState.setLteEarfcnRsrpBoost(0);
        }
    }

    private boolean getDelaySendSignalFutureState() {
        int slotId = this.mPhone.getPhoneId();
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("delay_send_signal", slotId, Boolean.class);
        boolean valueFromProp = FEATURE_DELAY_UPDATE_SIGANL_STENGTH;
        log("getDelaySendSignalFutureState, slotId:" + slotId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    /* access modifiers changed from: protected */
    public boolean onSignalStrengthResult(AsyncResult ar) {
        SignalStrength newSignalStrength;
        this.mSS.getRilDataRadioTechnology();
        this.mSS.getRilVoiceRadioTechnology();
        if (!getDelaySendSignalFutureState() || !this.mHwServiceStateTrackerEx.isSupportSingalStrengthHw()) {
            if (ar.exception != null || ar.result == null) {
                log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
                this.mSignalStrength = new SignalStrength();
            } else {
                this.mSignalStrength = (SignalStrength) ar.result;
                this.mSignalStrength.updateLevel(getCarrierConfig(), this.mSS);
            }
            return notifySignalStrength();
        }
        if (ar.exception != null || ar.result == null || !(ar.result instanceof SignalStrength)) {
            if (ar.exception != null) {
                log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            } else if (ar.result != null) {
                log("result : " + ar.result);
            } else {
                log("ar.result is null!");
            }
            newSignalStrength = new SignalStrength();
        } else {
            newSignalStrength = (SignalStrength) ar.result;
        }
        return this.mHwServiceStateTrackerEx.signalStrengthResultHw(this.mSignalStrength, newSignalStrength, this.mPhone.isPhoneTypeGsm());
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
        UiccCardApplication uiccCardApplication = this.mUiccApplcation;
        if (uiccCardApplication == null) {
            return false;
        }
        return uiccCardApplication.getState() != IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN;
    }

    @UnsupportedAppUsage
    public String getSystemProperty(String property, String defValue) {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), property, defValue);
    }

    public List<CellInfo> getAllCellInfo() {
        return this.mLastCellInfoList;
    }

    public void setCellInfoMinInterval(int interval) {
        this.mCellInfoMinIntervalMs = interval;
    }

    public void requestAllCellInfo(WorkSource workSource, Message rspMsg) {
        if (this.mCi.getRilVersion() < 8) {
            AsyncResult.forMessage(rspMsg);
            rspMsg.sendToTarget();
            log("SST.requestAllCellInfo(): not implemented");
            return;
        }
        synchronized (this.mPendingCellInfoRequests) {
            if (this.mIsPendingCellInfoRequest) {
                if (rspMsg != null) {
                    this.mPendingCellInfoRequests.add(rspMsg);
                }
                return;
            }
            long curTime = SystemClock.elapsedRealtime();
            if (!this.mHwServiceStateTrackerEx.isCellRequestStrategyPassed(workSource)) {
                if (rspMsg != null) {
                    log("SST.requestAllCellInfo(): return last, back to back calls");
                    AsyncResult.forMessage(rspMsg, this.mLastCellInfoList, (Throwable) null);
                    rspMsg.sendToTarget();
                }
                return;
            }
            if (workSource != null) {
                this.mHwServiceStateTrackerEx.countPackageUseCellInfo(workSource.getName(0));
            }
            if (rspMsg != null) {
                this.mPendingCellInfoRequests.add(rspMsg);
            }
            this.mLastCellInfoReqTime = curTime;
            this.mIsPendingCellInfoRequest = true;
            this.mCi.getCellInfoList(obtainMessage(43), workSource);
            sendMessageDelayed(obtainMessage(43), CELL_INFO_LIST_QUERY_TIMEOUT);
        }
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
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
        ArrayList<Pair<Integer, Integer>> arrayList = this.mEarfcnPairListForRsrpBoost;
        if (arrayList != null) {
            int i = arrayList.size();
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
        List<CellInfo> list = this.mLastCellInfoList;
        if (list != null) {
            boolean first = true;
            for (CellInfo info : list) {
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
        UiccCardApplication uiccCardApplication = this.mUiccApplcation;
        if (uiccCardApplication != null) {
            uiccCardApplication.registerForReady(this, 26, null);
        }
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.registerForRecordsLoaded(this, 27, null);
        }
    }

    private void unregisterForRuimEvents() {
        log("unregisterForRuimEvents");
        UiccCardApplication uiccCardApplication = this.mUiccApplcation;
        if (uiccCardApplication != null) {
            uiccCardApplication.unregisterForReady(this);
        }
        IccRecords iccRecords = this.mIccRecords;
        if (iccRecords != null) {
            iccRecords.unregisterForRecordsLoaded(this);
        }
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void setSignalStrength(SignalStrength signalStrength) {
        log("setSignalStrength : " + signalStrength);
        this.mSignalStrength = signalStrength;
    }

    public void setDoRecoveryTriggerState(boolean state) {
        this.mRadioOffByDoRecovery = state;
        log("setDoRecoveryTriggerState, " + this.mRadioOffByDoRecovery);
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
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
        int[] iArr = this.mPollingContext;
        sb.append(iArr != null ? Integer.valueOf(iArr[0]) : PhoneConfigurationManager.SSSS);
        pw.println(sb.toString());
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mDontPollSignalStrength=" + this.mDontPollSignalStrength);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellIdentity=" + Rlog.pii(false, this.mCellIdentity));
        pw.println(" mNewCellIdentity=" + Rlog.pii(false, this.mNewCellIdentity));
        pw.println(" mLastCellInfoReqTime=" + this.mLastCellInfoReqTime);
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
        pw.println(" mCellInfoMinIntervalMs=" + this.mCellInfoMinIntervalMs);
        pw.println(" mEriManager=" + this.mEriManager);
        dumpEarfcnPairList(pw);
        this.mLocaleTracker.dump(fd, pw, args);
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "    ");
        this.mCdnr.dump(ipw);
        ipw.println(" Carrier Display Name update records:");
        ipw.increaseIndent();
        this.mCdnrLogs.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Roaming Log:");
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
        ipw.decreaseIndent();
        this.mNitzState.dumpLogs(fd, ipw, args);
        ipw.flush();
    }

    @UnsupportedAppUsage
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
        String networkCountry = PhoneConfigurationManager.SSSS;
        String homeCountry = PhoneConfigurationManager.SSSS;
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
            return true;
        }
        if (!"vi".equals(homeCountry) || !"us".equals(networkCountry)) {
            return inSameCountry;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService = currentServiceState.getVoiceRegState() == 0;
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17235998);
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

    @UnsupportedAppUsage
    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength();
    }

    /* access modifiers changed from: protected */
    public String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, PhoneConfigurationManager.SSSS);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    private void processIwlanRegistrationInfo() {
        NetworkRegistrationInfo wwanNri;
        if (this.mCi.getRadioState() == 0) {
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
                this.mNewSS.setDataRegState(0);
                this.mNewSS.addNetworkRegistrationInfo(new NetworkRegistrationInfo.Builder().setTransportType(2).setDomain(2).setAccessNetworkTechnology(18).setRegistrationState(1).build());
                if (this.mTransportManager.isInLegacyMode()) {
                    this.mNewSS.addNetworkRegistrationInfo(new NetworkRegistrationInfo.Builder().setTransportType(1).setDomain(2).setAccessNetworkTechnology(18).setRegistrationState(1).build());
                }
                this.mNewSS.setOperatorAlphaLong(operator);
                log("pollStateDone: mNewSS = " + this.mNewSS);
            }
        } else if (this.mTransportManager.isInLegacyMode() && (wwanNri = this.mNewSS.getNetworkRegistrationInfo(2, 1)) != null && wwanNri.getAccessNetworkTechnology() == 18) {
            this.mNewSS.addNetworkRegistrationInfo(new NetworkRegistrationInfo.Builder().setTransportType(2).setDomain(2).setRegistrationState(wwanNri.getRegistrationState()).setAccessNetworkTechnology(18).setRejectCause(wwanNri.getRejectCause()).setEmergencyOnly(wwanNri.isEmergencyEnabled()).setAvailableServices(wwanNri.getAvailableServices()).build());
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
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : stack) {
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
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null && wakeLock.isHeld()) {
            this.mWakeLock.release();
            log("release wakelock");
        }
    }

    /* access modifiers changed from: protected */
    public void judgeToLaunchCsgPeriodicSearchTimer() {
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null) {
            hwCustGsmServiceStateTracker.judgeToLaunchCsgPeriodicSearchTimer();
            log("mHwCustGsmServiceStateTracker is not null");
        }
    }

    public HwCustGsmServiceStateTracker returnObject() {
        return this.mHwCustGsmServiceStateTracker;
    }

    private boolean isOperatorChanged(CarrierDisplayNameData data, boolean showWifi, String wifi, String regplmn) {
        int combinedRegState = this.mHwServiceStateTrackerEx.getCombinedRegState(this.mSS);
        boolean isRealChange = true;
        boolean isMtkVowifiRegistered = false;
        if (this.mPhone.isPhoneTypeGsm()) {
            isRealChange = IS_HISI_PLATFORM || !TextUtils.isEmpty(this.mSS.getOperatorNumeric()) || combinedRegState != 0;
            isMtkVowifiRegistered = IS_MTK_PLATFORM && this.mSS.getRilDataRadioTechnology() == 18;
        }
        if (data.shouldShowPlmn() != this.mCurShowPlmn || data.shouldShowSpn() != this.mCurShowSpn || !TextUtils.equals(data.getSpn(), this.mCurSpn) || !TextUtils.equals(data.getDataSpn(), this.mCurDataSpn) || !TextUtils.equals(data.getPlmn(), this.mCurPlmn) || showWifi != this.mCurShowWifi || !TextUtils.equals(wifi, this.mCurWifi) || !TextUtils.equals(regplmn, this.mCurRegplmn)) {
            return isRealChange || isMtkVowifiRegistered;
        }
        return false;
    }

    private void getOperator() {
        if (!IS_HISI_PLATFORM && this.mHwServiceStateTrackerEx.getCombinedRegState(this.mSS) == 0 && TextUtils.isEmpty(this.mSS.getOperatorNumeric()) && !this.mRplmnIsNull) {
            this.mCi.getOperator(obtainMessage(7, this.mPollingContext));
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
    public int getCombinedRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if ((regState != 1 && regState != 3) || dataRegState != 0) {
            return regState;
        }
        log("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    private PersistableBundle getCarrierConfig() {
        PersistableBundle config;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager == null || (config = configManager.getConfigForSubId(this.mPhone.getSubId())) == null) {
            return CarrierConfigManager.getDefaultConfig();
        }
        return config;
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

    public int getRejCause() {
        return this.mHwServiceStateTrackerEx.getRejCause();
    }

    public void clearRejCause() {
        this.mHwServiceStateTrackerEx.clearRejCause();
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public ServiceState getmSSHw() {
        return this.mSS;
    }

    public int getPhoneIdHw() {
        return this.mPhone.getPhoneId();
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void onSignalStrengthResultHw(Message msg) {
        onSignalStrengthResult((AsyncResult) msg.obj);
    }

    /* access modifiers changed from: protected */
    public IccRecords getmIccRecordsHw() {
        return this.mIccRecords;
    }

    /* access modifiers changed from: protected */
    public void setmIccRecordsHw(IccRecords mIccRecords2) {
        this.mIccRecords = mIccRecords2;
    }

    /* access modifiers changed from: protected */
    public UiccCardApplication getmUiccApplcationHw() {
        return this.mUiccApplcation;
    }

    /* access modifiers changed from: protected */
    public void setmUiccApplcationHw(UiccCardApplication mUiccApplcation2) {
        this.mUiccApplcation = mUiccApplcation2;
    }

    /* access modifiers changed from: protected */
    public CommandsInterface getmCiHw() {
        return this.mCi;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public ServiceState getmNewSSHw() {
        return this.mNewSS;
    }

    /* access modifiers changed from: protected */
    public void setmNewSSHw(ServiceState mNewSS2) {
        this.mNewSS = mNewSS2;
    }

    /* access modifiers changed from: protected */
    public void setmSignalStrengthHw(SignalStrength mSignalStrength2) {
        this.mSignalStrength = mSignalStrength2;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void updateSpnDisplayHw() {
        updateSpnDisplay();
    }

    public boolean needPollSignalStrength() {
        return this.mDontPollSignalStrength;
    }

    public void setDisplayState(int state) {
        this.mDefaultDisplayState = state;
    }

    public int getPollSignalStrengthEvent() {
        return 10;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public CellLocation getCellLocationInfo() {
        CellIdentity cellIdentity = this.mCellIdentity;
        if (cellIdentity == null) {
            return null;
        }
        return cellIdentity.asCellLocation();
    }

    public CellLocation getNewCellLocationInfo() {
        CellIdentity cellIdentity = this.mNewCellIdentity;
        if (cellIdentity == null) {
            return null;
        }
        return cellIdentity.asCellLocation();
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public CellIdentity getmCellIdentity() {
        return this.mCellIdentity;
    }

    public void setmCellIdentity(CellIdentity mCellIdentity2) {
        this.mCellIdentity = mCellIdentity2;
    }

    public CellIdentity getmNewCellIdentity() {
        return this.mNewCellIdentity;
    }

    public void setmNewCellIdentity(CellIdentity mNewCellIdentity2) {
        this.mNewCellIdentity = mNewCellIdentity2;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public PersistableBundle getCarrierConfigHw() {
        return getCarrierConfig();
    }

    public void combinePsRegistrationStatesHw(ServiceState serviceState) {
        combinePsRegistrationStates(serviceState);
    }

    public CommandsInterface getCommandsInterface() {
        return this.mCi;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void clearmLastCellInfoList() {
        this.mLastCellInfoList = null;
    }

    private boolean isLteOrNrTechnology(int rat) {
        if (ServiceState.isLte(rat) || rat == 20) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0057  */
    private void processCtVolteCellLocationInfo(int serviceState, NetworkRegistrationInfo networkRegState, boolean isCsCellularRegistration) {
        boolean isCTSimCardInService;
        boolean isCTSimCardBlockUpdateCellIdentity;
        CellIdentity cellIdentity = networkRegState.getCellIdentity();
        boolean isNrAccessNetworkTechnology = false;
        boolean isUsingPsCellIdentity = cellIdentity != null && (cellIdentity.getType() == 3 || cellIdentity.getType() == 6);
        if (serviceState == 0) {
            GsmCdmaPhone gsmCdmaPhone = this.mPhone;
            if (gsmCdmaPhone.isCTSimCard(gsmCdmaPhone.getPhoneId())) {
                isCTSimCardInService = true;
                if (this.hasUpdateCellLocByPS) {
                    GsmCdmaPhone gsmCdmaPhone2 = this.mPhone;
                    if (gsmCdmaPhone2.isCTSimCard(gsmCdmaPhone2.getPhoneId())) {
                        isCTSimCardBlockUpdateCellIdentity = true;
                        if (networkRegState.getAccessNetworkTechnology() == 20) {
                            isNrAccessNetworkTechnology = true;
                        }
                        if (isCsCellularRegistration) {
                            if (isCTSimCardBlockUpdateCellIdentity || isNrAccessNetworkTechnology) {
                                log("processCtVolteCellLocationInfo, [cs], block update cell identity.");
                                return;
                            } else {
                                this.mNewCellIdentity = cellIdentity;
                                return;
                            }
                        } else if ((isUsingPsCellIdentity && isCTSimCardInService) || isNrAccessNetworkTechnology) {
                            log("processCtVolteCellLocationInfo, [ps], update for ct sim card in LTE.");
                            this.mNewCellIdentity = cellIdentity;
                            this.hasUpdateCellLocByPS = true;
                            return;
                        } else {
                            return;
                        }
                    }
                }
                isCTSimCardBlockUpdateCellIdentity = false;
                if (networkRegState.getAccessNetworkTechnology() == 20) {
                }
                if (isCsCellularRegistration) {
                }
            }
        }
        isCTSimCardInService = false;
        if (this.hasUpdateCellLocByPS) {
        }
        isCTSimCardBlockUpdateCellIdentity = false;
        if (networkRegState.getAccessNetworkTechnology() == 20) {
        }
        if (isCsCellularRegistration) {
        }
    }

    private void setOldCsRegState(int oldCsRegState) {
        CarrierServiceStateTracker carrierServiceStateTracker = this.mCSST;
        if (carrierServiceStateTracker == null || !carrierServiceStateTracker.isRadioOffOrAirplaneMode()) {
            log("set old cs reg state: " + oldCsRegState);
            this.mOldCsRegState = oldCsRegState;
            return;
        }
        log("set old cs reg state to radio power off");
        this.mOldCsRegState = 3;
    }

    public int getOldCsRegState() {
        return this.mOldCsRegState;
    }

    public int getNewNsaState() {
        return this.mNewNsaState;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public int getCid(CellIdentity id) {
        return getCidFromCellIdentity(id);
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public long getNitzSpaceTime() {
        NitzStateMachine nitzStateMachine = this.mNitzState;
        if (nitzStateMachine != null) {
            return nitzStateMachine.getNitzSpaceTime();
        }
        return -1;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public boolean getNitzTimeZoneDetectionSuccessful() {
        return getNitzState().getNitzTimeZoneDetectionSuccessful();
    }

    public void setPreferredNetworkTypeSafely(int networkType, Message response) {
        this.mHwServiceStateTrackerEx.setPreferredNetworkTypeSafely(networkType, response);
    }

    public void checkAndSetNetworkType() {
        this.mHwServiceStateTrackerEx.checkAndSetNetworkType();
    }

    public boolean isNeedLocationTimeZoneUpdate(String zoneId) {
        return this.mHwServiceStateTrackerEx.isNeedLocationTimeZoneUpdate(zoneId);
    }

    public boolean allowUpdateTimeFromNitz(long nitzTime) {
        return this.mHwServiceStateTrackerEx.allowUpdateTimeFromNitz(nitzTime);
    }

    public void sendNitzTimeZoneUpdateMessage() {
        this.mHwServiceStateTrackerEx.sendNitzTimeZoneUpdateMessage(getCellLocationInfo());
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void handleNetworkRejectionEx(int rejectcause, int rejectrat) {
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null) {
            hwCustGsmServiceStateTracker.handleNetworkRejectionEx(rejectcause, rejectrat);
        }
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void handleLteEmmCause(int phoneId, int rejrat, int originalrejectcause) {
        HwCustGsmServiceStateTracker hwCustGsmServiceStateTracker = this.mHwCustGsmServiceStateTracker;
        if (hwCustGsmServiceStateTracker != null) {
            hwCustGsmServiceStateTracker.handleLteEmmCause(phoneId, rejrat, originalrejectcause);
        }
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void setRetryNotifySsState(boolean needRetry) {
        log("set retry notify service state: " + needRetry);
        this.mNeedRetryNotifySs = needRetry;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void retryNotifyServiceState(int subId) {
        log("SubscriptionListener.onSubscriptionInfoChanged mPrevSubId:" + this.mPrevSubId + "  subId:" + subId);
        if (this.mPrevSubId == -1 && subId != -1 && this.mNeedRetryNotifySs) {
            log("SubscriptionListener notifyServiceStateChanged");
            this.mPhone.notifyServiceStateChanged(this.mSS);
        }
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public void setNewNsaState(int state) {
        this.mNewNsaState = state;
    }

    @Override // com.android.internal.telephony.IServiceStateTrackerInner
    public boolean hasSecondaryCellServing() {
        List<PhysicalChannelConfig> list = this.mLastPhysicalChannelConfigList;
        if (list == null) {
            return false;
        }
        for (PhysicalChannelConfig config : list) {
            if (isNrPhysicalChannelConfig(config) && config.getConnectionStatus() == 2) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public String getCdmaEriText(int roamInd, int defRoamInd) {
        return this.mEriManager.getCdmaEriText(roamInd, defRoamInd);
    }

    private void updateOperatorNamePattern(PersistableBundle config) {
        String operatorNamePattern = config.getString("operator_name_filter_pattern_string");
        if (!TextUtils.isEmpty(operatorNamePattern)) {
            this.mOperatorNameStringPattern = Pattern.compile(operatorNamePattern);
            log("mOperatorNameStringPattern: " + this.mOperatorNameStringPattern.toString());
        }
    }

    private void updateOperatorNameForServiceState(ServiceState servicestate) {
        if (servicestate != null) {
            servicestate.setOperatorName(filterOperatorNameByPattern(servicestate.getOperatorAlphaLong()), filterOperatorNameByPattern(servicestate.getOperatorAlphaShort()), servicestate.getOperatorNumeric());
            List<NetworkRegistrationInfo> networkRegistrationInfos = servicestate.getNetworkRegistrationInfoList();
            for (int i = 0; i < networkRegistrationInfos.size(); i++) {
                if (networkRegistrationInfos.get(i) != null) {
                    updateOperatorNameForCellIdentity(networkRegistrationInfos.get(i).getCellIdentity());
                }
            }
        }
    }

    private void updateOperatorNameForCellIdentity(CellIdentity cellIdentity) {
        if (cellIdentity != null) {
            cellIdentity.setOperatorAlphaLong(filterOperatorNameByPattern((String) cellIdentity.getOperatorAlphaLong()));
            cellIdentity.setOperatorAlphaShort(filterOperatorNameByPattern((String) cellIdentity.getOperatorAlphaShort()));
        }
    }

    public void updateOperatorNameForCellInfo(List<CellInfo> cellInfos) {
        if (!(cellInfos == null || cellInfos.isEmpty())) {
            for (CellInfo cellInfo : cellInfos) {
                if (cellInfo.isRegistered()) {
                    updateOperatorNameForCellIdentity(cellInfo.getCellIdentity());
                }
            }
        }
    }

    public String filterOperatorNameByPattern(String operatorName) {
        if (this.mOperatorNameStringPattern == null || TextUtils.isEmpty(operatorName)) {
            return operatorName;
        }
        Matcher matcher = this.mOperatorNameStringPattern.matcher(operatorName);
        if (!matcher.find()) {
            return operatorName;
        }
        if (matcher.groupCount() > 0) {
            return matcher.group(1);
        }
        log("filterOperatorNameByPattern: pattern no group");
        return operatorName;
    }

    private void updateDataRatByConfig(int rat, ServiceState ss, GsmCdmaPhone phone) {
        NetworkRegistrationInfo regInfo = ss.getNetworkRegistrationInfoHw(2, 1);
        if (regInfo == null) {
            log("regInfo is null");
            return;
        }
        int rat2 = this.mHwServiceStateTrackerEx.getNrConfigTechnology(rat, NetworkRegistrationInfoEx.getNetworkRegistrationInfoEx(regInfo));
        log("updateDataRatByConfig rat: " + rat2);
        regInfo.setConfigRadioTechnology(rat2);
        ss.addNetworkRegistrationInfo(regInfo);
    }

    private boolean isNeedDelayUpdateRegisterStateDone(ServiceState oldSS, ServiceState newSS) {
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mHwServiceStateTrackerEx.proccessGsmDelayUpdateRegisterStateDone(oldSS, newSS)) {
                return true;
            }
            return false;
        } else if (!this.mPhone.isPhoneTypeCdmaLte() || !this.mHwServiceStateTrackerEx.proccessCdmaLteDelayUpdateRegisterStateDone(oldSS, newSS)) {
            return false;
        } else {
            return true;
        }
    }
}
