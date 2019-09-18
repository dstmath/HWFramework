package com.android.internal.telephony.dataconnection;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.GlobalParamsAdaptor;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.HwPhoneService;
import com.android.internal.telephony.HwServiceStateManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.ArrayUtils;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import vendor.huawei.hardware.hisiradio.V1_1.LteAttachInfo;

public class HwDcTrackerBaseReference implements AbstractDcTrackerBase.DcTrackerBaseReference {
    private static final String ACTION_BT_CONNECTION_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    protected static final int ACTIVE_PDP_FAIL_TO_RESTART_RILD_COUNT = 3;
    protected static final long ACTIVE_PDP_FAIL_TO_RESTART_RILD_MILLIS = 600000;
    private static final String ALLOW_MMS_PROPERTY_INT = "allow_mms_property_int";
    static final String APN_ID = "apn_id";
    public static final int APN_PDP_TYPE_IPV4 = 1;
    public static final int APN_PDP_TYPE_IPV4V6 = 3;
    public static final int APN_PDP_TYPE_IPV6 = 2;
    public static final String APN_TYPE_VOWIFIMMS = "vowifi_mms";
    protected static final String CAUSE_NO_RETRY_AFTER_DISCONNECT = SystemProperties.get("ro.hwpp.disc_noretry_cause", "");
    private static final int CDMA_NOT_ROAMING = 0;
    private static final int CDMA_ROAMING = 1;
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final String CLEARCODE_2HOUR_DELAY_OVER = "clearcode2HourDelayOver";
    private static final String CT_CDMA_OPERATOR = "46003";
    private static final String CT_LTE_APN_PREFIX = SystemProperties.get("ro.config.ct_lte_apn", "ctnet");
    private static final String CT_NOT_ROAMING_APN_PREFIX = SystemProperties.get("ro.config.ct_not_roaming_apn", "ctnet");
    private static final String CT_ROAMING_APN_PREFIX = SystemProperties.get("ro.config.ct_roaming_apn", "ctnet");
    private static final String CUST_PREFERRED_APN = SystemProperties.get("ro.hwpp.preferred_apn", "").trim();
    public static final boolean CUST_RETRY_CONFIG = SystemProperties.getBoolean("ro.config.cust_retry_config", false);
    public static final int DATA_ROAMING_EXCEPTION = -1;
    public static final int DATA_ROAMING_INTERNATIONAL = 2;
    public static final int DATA_ROAMING_NATIONAL = 1;
    public static final int DATA_ROAMING_OFF = 0;
    public static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static int DATA_STALL_ALARM_PUNISH_DELAY_IN_MS_DEFAULT = 1800000;
    private static final boolean DBG = true;
    private static final int DELAY_2_HOUR = 7200000;
    private static final boolean DISABLE_GW_PS_ATTACH = SystemProperties.getBoolean("ro.odm.disable_m1_gw_ps_attach", false);
    private static final String DS_USE_DURATION_KEY = "DSUseDuration";
    private static final int DS_USE_STATISTICS_REPORT_INTERVAL = 3600000;
    protected static final String ENABLE_ALLOW_MMS = "enable_always_allow_mms";
    private static final boolean ENABLE_WIFI_LTE_CE = SystemProperties.getBoolean("ro.config.enable_wl_coexist", false);
    private static final boolean ESM_FLAG_ADAPTION_ENABLED = SystemProperties.getBoolean("ro.config.attach_apn_enabled", false);
    private static final int ESM_FLAG_INVALID = -1;
    private static final int EVENT_FDN_RECORDS_LOADED = 2;
    private static final int EVENT_FDN_SWITCH_CHANGED = 1;
    private static final int EVENT_LIMIT_PDP_ACT_IND = 4;
    private static final int EVENT_VOICE_CALL_ENDED = 3;
    private static final int EVENT_VOICE_CALL_STARTED = 5;
    protected static final Uri FDN_URL = Uri.parse("content://icc/fdn/subId/");
    private static final String GC_ICCID = "8985231";
    private static final String GC_MCCMNC = "45431";
    private static final String GC_SPN = "CTExcel";
    private static final int GSM_ROAMING_CARD1 = 2;
    private static final int GSM_ROAMING_CARD2 = 3;
    private static final String INTENT_LIMIT_PDP_ACT_IND = "com.android.internal.telephony.limitpdpactind";
    protected static final String INTENT_PDP_RESET_ALARM = "com.android.internal.telephony.pdp-reset";
    private static final String INTENT_SET_PREF_NETWORK_TYPE = "com.android.internal.telephony.set-pref-networktype";
    private static final String INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE = "network_type";
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_ATT = ("07".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")));
    private static final boolean IS_DUAL_4G_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    private static final String IS_LIMIT_PDP_ACT = "islimitpdpact";
    private static final int LTE_NOT_ROAMING = 4;
    private static final int MCC_LENGTH = 3;
    protected static final boolean MMSIgnoreDSSwitchNotRoaming;
    protected static final boolean MMSIgnoreDSSwitchOnRoaming = (((MMS_PROP >> 1) & 1) == 1);
    protected static final boolean MMS_ON_ROAMING = ((MMS_PROP & 1) == 1);
    protected static final int MMS_PROP = SystemProperties.getInt("ro.config.hw_always_allow_mms", 4);
    private static final String NETD_PROCESS_UID = "0";
    private static final int NETWORK_MODE_GSM_UMTS = 3;
    private static final int NETWORK_MODE_LTE_GSM_WCDMA = 9;
    private static final int NETWORK_MODE_UMTS_ONLY = 2;
    protected static final String PDP_RESET_ALARM_TAG_EXTRA = "pdp.reset.alram.tag";
    protected static final boolean PERMANENT_ERROR_HEAL_PROP = SystemProperties.getBoolean("ro.config.permanent_error_heal", false);
    private static final int PID_STATS_FILE_IFACE_INDEX = 1;
    private static final int PID_STATS_FILE_PROCESS_NAME_INDEX = 2;
    private static final int PID_STATS_FILE_UDP_RX_INDEX = 14;
    private static final int PID_STATS_FILE_UDP_TX_INDEX = 20;
    private static final int PID_STATS_FILE_UID_INDEX = 3;
    private static final String PREFERRED_APN_ID = "preferredApnIdEx";
    private static final int PREF_APN_ID_LEN = 5;
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G = 10000;
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G = 45000;
    private static final long PS_CLEARCODE_APN_DELAY_MILLIS_2G_3G = (SystemProperties.getLong("ro.config.clearcode_2g3g_timer", 45) * 1000);
    private static final long PS_CLEARCODE_APN_DELAY_MILLIS_4G = (SystemProperties.getLong("ro.config.clearcode_4g_timer", 10) * 1000);
    private static final long PS_CLEARCODE_LIMIT_PDP_ACT_DELAY = (SystemProperties.getLong("ro.config.clearcode_limit_timer", 1) * 1000);
    private static final String PS_CLEARCODE_PLMN = SystemProperties.get("ro.config.clearcode_plmn", "");
    /* access modifiers changed from: private */
    public static final boolean RESET_PROFILE = SystemProperties.getBoolean("ro.hwpp_reset_profile", false);
    private static int RESTART_RADIO_PUNISH_TIME_IN_MS = 43200000;
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final int SUB2 = 1;
    private static final String TAG = "HwDcTrackerBaseReference";
    protected static final boolean USER_FORCE_DATA_SETUP = SystemProperties.getBoolean("ro.hwpp.allow_data_onlycs", false);
    private static final String XCAP_DATA_ROAMING_ENABLE = "carrier_xcap_data_roaming_switch";
    protected static final boolean isMultiSimEnabled = HwFrameworkFactory.getHwInnerTelephonyManager().isMultiSimEnabled();
    /* access modifiers changed from: private */
    public static boolean mIsScreenOn = false;
    protected static final boolean mWcdmaVpEnabled = SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true");
    /* access modifiers changed from: private */
    public static int newRac = -1;
    /* access modifiers changed from: private */
    public static int oldRac = -1;
    private static final String pidStatsPath = "/proc/net/xt_qtaguid/stats_pid";
    protected boolean ALLOW_MMS = false;
    private boolean SETAPN_UNTIL_CARDLOADED = SystemProperties.getBoolean("ro.config.delay_setapn", false);
    private boolean SUPPORT_MPDN = SystemProperties.getBoolean("persist.telephony.mpdn", true);
    private ContentObserver allowMmsObserver = null;
    private boolean broadcastPrePostPay = true;
    GsmCellLocation cellLoc = new GsmCellLocation();
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            hwDcTrackerBaseReference.log("handleMessage msg=" + msg.what);
            switch (msg.what) {
                case 3:
                    boolean unused = HwDcTrackerBaseReference.this.mInVoiceCall = false;
                    HwDcTrackerBaseReference.this.onVoiceCallEndedHw();
                    return;
                case 4:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        HwDcTrackerBaseReference hwDcTrackerBaseReference2 = HwDcTrackerBaseReference.this;
                        hwDcTrackerBaseReference2.log("PSCLEARCODE EVENT_LIMIT_PDP_ACT_IND exception " + ar.exception);
                        return;
                    }
                    HwDcTrackerBaseReference.this.onLimitPDPActInd(ar);
                    return;
                case 5:
                    boolean unused2 = HwDcTrackerBaseReference.this.mInVoiceCall = true;
                    return;
                default:
                    return;
            }
        }
    };
    private boolean isSupportPidStats = false;
    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmManager;
    private ApnSetting mAttachedApnSettings = null;
    private PendingIntent mClearCodeLimitAlarmIntent = null;
    public DcFailCause mCurFailCause;
    private int mCurrentState = -1;
    /* access modifiers changed from: private */
    public int mDSUseDuration = 0;
    /* access modifiers changed from: private */
    public DcTracker mDcTrackerBase;
    private String mDefaultApnId = "0,0,0,0,0";
    private int mDelayTime = 3000;
    private boolean mDoRecoveryAddDnsProp = SystemProperties.getBoolean("ro.config.dorecovery_add_dns", true);
    private FdnAsyncQueryHandler mFdnAsyncQuery;
    private FdnChangeObserver mFdnChangeObserver;
    ServiceStateTracker mGsmServiceStateTracker = null;
    /* access modifiers changed from: private */
    public boolean mInVoiceCall = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Rlog.w(HwDcTrackerBaseReference.TAG, "intent or intent.getAction() is null.");
                return;
            }
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                int subid = 0;
                if (intent.getExtra("subscription") != null) {
                    subid = intent.getIntExtra("subscription", -1);
                }
                if (subid != HwDcTrackerBaseReference.this.mSubscription.intValue()) {
                    Rlog.d(HwDcTrackerBaseReference.TAG, "receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , but the subid is different from mSubscription");
                    return;
                }
                String curSimState = intent.getStringExtra("ss");
                if (TextUtils.equals(curSimState, HwDcTrackerBaseReference.this.mSimState)) {
                    Rlog.d(HwDcTrackerBaseReference.TAG, "the curSimState is same as mSimState, so return");
                    return;
                }
                if (("ABSENT".equals(curSimState) || "CARD_IO_ERROR".equals(curSimState)) && !"ABSENT".equals(HwDcTrackerBaseReference.this.mSimState) && !"CARD_IO_ERROR".equals(HwDcTrackerBaseReference.this.mSimState) && HwDcTrackerBaseReference.RESET_PROFILE) {
                    Rlog.d(HwDcTrackerBaseReference.TAG, "receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , resetprofile");
                    HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.mCi.resetProfile(null);
                }
                String unused = HwDcTrackerBaseReference.this.mSimState = curSimState;
            } else if (intent.getAction().equals(HwDcTrackerBaseReference.INTENT_SET_PREF_NETWORK_TYPE)) {
                HwDcTrackerBaseReference.this.onActionIntentSetNetworkType(intent);
            } else if (HwDcTrackerBaseReference.INTENT_LIMIT_PDP_ACT_IND.equals(intent.getAction())) {
                HwDcTrackerBaseReference.this.onActionIntentLimitPDPActInd(intent);
            } else if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(HwDcTrackerBaseReference.DS_USE_DURATION_KEY, HwDcTrackerBaseReference.this.mDSUseDuration);
                editor.commit();
                HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
                hwDcTrackerBaseReference.log("Put mDSUseDuration into SharedPreferences, put: " + HwDcTrackerBaseReference.this.mDSUseDuration + ", get: " + sp.getInt(HwDcTrackerBaseReference.DS_USE_DURATION_KEY, 0));
            } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                int lastDSUseDuration = PreferenceManager.getDefaultSharedPreferences(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext()).getInt(HwDcTrackerBaseReference.DS_USE_DURATION_KEY, 0);
                HwDcTrackerBaseReference.access$512(HwDcTrackerBaseReference.this, lastDSUseDuration);
                HwDcTrackerBaseReference hwDcTrackerBaseReference2 = HwDcTrackerBaseReference.this;
                hwDcTrackerBaseReference2.log("Read last mDSUseDuration back from SharedPreferences, lastDSUseDuration: " + lastDSUseDuration + ", mDSUseDuration: " + HwDcTrackerBaseReference.this.mDSUseDuration);
            } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                boolean unused2 = HwDcTrackerBaseReference.mIsScreenOn = true;
            } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                boolean unused3 = HwDcTrackerBaseReference.mIsScreenOn = false;
            } else if (HwDcTrackerBaseReference.ACTION_BT_CONNECTION_CHANGED.equals(intent.getAction())) {
                if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 0) {
                    HwDcTrackerBaseReference.this.mIsBtConnected = false;
                } else if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 2) {
                    HwDcTrackerBaseReference.this.mIsBtConnected = true;
                }
                HwDcTrackerBaseReference hwDcTrackerBaseReference3 = HwDcTrackerBaseReference.this;
                hwDcTrackerBaseReference3.log("Received bt_connect_state = " + HwDcTrackerBaseReference.this.mIsBtConnected);
            } else if (HwDcTrackerBaseReference.INTENT_PDP_RESET_ALARM.equals(intent.getAction())) {
                HwDcTrackerBaseReference.this.log("Pdp reset alarm");
                HwDcTrackerBaseReference.this.onActionIntentPdpResetAlarm(intent);
            } else if ("com.android.telephony.opencard".equals(intent.getAction())) {
                HwDcTrackerBaseReference.this.log("onUserSelectOpenService ");
                HwDcTrackerBaseReference.this.onUserSelectOpenService();
            }
        }
    };
    protected boolean mIsBtConnected = false;
    private boolean mIsClearCodeEnabled = SystemProperties.getBoolean("ro.config.hw_clearcode_pdp", false);
    private boolean mIsLimitPDPAct = false;
    private long mLastRadioResetTimestamp = 0;
    private INetworkManagementService mNetworkManager = null;
    private long mNextReportDSUseDurationStamp = (SystemClock.elapsedRealtime() + 3600000);
    /* access modifiers changed from: private */
    public int mNwOldMode = Phone.PREFERRED_NT_MODE;
    /* access modifiers changed from: private */
    public AlertDialog mPSClearCodeDialog = null;
    protected PendingIntent mPdpResetAlarmIntent = null;
    protected int mPdpResetAlarmTag = ((int) SystemClock.elapsedRealtime());
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (HwDcTrackerBaseReference.this.oldCallState == 2 && state == 0 && HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
                HwDcTrackerBaseReference.this.mDcTrackerBase.onTrySetupDataHw("dataEnabled");
                if (HwDcTrackerBaseReference.this.mDcTrackerBase.getMPrioritySortedApnContexts() != null) {
                    ArrayList<ApnContext> apnContexts = HwDcTrackerBaseReference.this.mDcTrackerBase.getMPrioritySortedApnContexts();
                    int size = apnContexts.size();
                    for (int i = 0; i < size; i++) {
                        ApnContext apnContext = apnContexts.get(i);
                        if (apnContext.getApnType().equals("default")) {
                            HwDcTrackerBaseReference.this.log("resetRetryCount");
                            apnContext.resetRetryCount();
                        }
                    }
                }
            }
            int unused = HwDcTrackerBaseReference.this.oldCallState = state;
        }
    };
    /* access modifiers changed from: private */
    public String mSimState = null;
    /* access modifiers changed from: private */
    public Integer mSubscription;
    private int mTryIndex = 0;
    protected UiccController mUiccController = UiccController.getInstance();
    private int netdPid = -1;
    /* access modifiers changed from: private */
    public int nwMode = Phone.PREFERRED_NT_MODE;
    private ContentObserver nwModeChangeObserver = null;
    /* access modifiers changed from: private */
    public int oldCallState = 0;
    /* access modifiers changed from: private */
    public int oldRadioTech = 0;
    private int preDataRadioTech = -1;
    private int preSetupBasedRadioTech = -1;
    PhoneStateListener pslForCellLocation = new PhoneStateListener() {
        /* JADX WARNING: Removed duplicated region for block: B:48:0x01f1 A[Catch:{ Exception -> 0x020c }] */
        public void onCellLocationChanged(CellLocation location) {
            GsmCellLocation newCellLoc;
            boolean isDisconnected;
            if (HwDcTrackerBaseReference.this.mDcTrackerBase.mApnContexts != null) {
                try {
                    HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged");
                    if (!(location instanceof GsmCellLocation)) {
                        HwDcTrackerBaseReference.this.log("CLEARCODE location not instanceof GsmCellLocation");
                        return;
                    }
                    HwDcTrackerBaseReference.this.mGsmServiceStateTracker = HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getServiceStateTracker();
                    int unused = HwDcTrackerBaseReference.newRac = HwServiceStateManager.getHwGsmServiceStateManager(HwDcTrackerBaseReference.this.mGsmServiceStateTracker, HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone).getRac();
                    int radioTech = HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
                    HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
                    hwDcTrackerBaseReference.log("CLEARCODE newCellLoc = " + newCellLoc + ", oldCellLoc = " + HwDcTrackerBaseReference.this.cellLoc + " oldRac = " + HwDcTrackerBaseReference.oldRac + " newRac = " + HwDcTrackerBaseReference.newRac + " radioTech = " + radioTech + " oldRadioTech = " + HwDcTrackerBaseReference.this.oldRadioTech);
                    boolean z = false;
                    if (HuaweiTelephonyConfigs.isHisiPlatform() && HwDcTrackerBaseReference.this.oldRadioTech != radioTech) {
                        int unused2 = HwDcTrackerBaseReference.this.oldRadioTech = radioTech;
                        HwDcTrackerBaseReference.this.log("clearcode oldRadioTech = " + HwDcTrackerBaseReference.this.oldRadioTech);
                        int unused3 = HwDcTrackerBaseReference.oldRac = -1;
                        HwDcTrackerBaseReference.this.resetTryTimes();
                    }
                    if (-1 == HwDcTrackerBaseReference.newRac) {
                        HwDcTrackerBaseReference.this.log("CLEARCODE not really changed");
                    } else if (HwDcTrackerBaseReference.oldRac == HwDcTrackerBaseReference.newRac || radioTech != 3) {
                        HwDcTrackerBaseReference.this.log("CLEARCODE RAC not really changed");
                    } else if (-1 == HwDcTrackerBaseReference.oldRac) {
                        int unused4 = HwDcTrackerBaseReference.oldRac = HwDcTrackerBaseReference.newRac;
                        HwDcTrackerBaseReference.this.log("CLEARCODE oldRac = -1 return");
                    } else {
                        int unused5 = HwDcTrackerBaseReference.oldRac = HwDcTrackerBaseReference.newRac;
                        HwDcTrackerBaseReference.this.cellLoc = newCellLoc;
                        DcTracker dcTracker = HwDcTrackerBaseReference.this.mDcTrackerBase;
                        ApnContext defaultApn = (ApnContext) HwDcTrackerBaseReference.this.mDcTrackerBase.mApnContexts.get("default");
                        if (!(!HwDcTrackerBaseReference.this.mDcTrackerBase.isUserDataEnabled() || defaultApn == null || defaultApn.getState() == DctConstants.State.CONNECTED)) {
                            int curPrefMode = HwNetworkTypeUtils.getNetworkModeFromDB(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext(), HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getSubId());
                            HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged radioTech = " + radioTech + " curPrefMode" + curPrefMode);
                            if (!(curPrefMode == 9 || curPrefMode == 2)) {
                                HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.setPreferredNetworkType(9, null);
                                HwNetworkTypeUtils.saveNetworkModeToDB(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext(), HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getSubId(), 9);
                                HwServiceStateManager.getHwGsmServiceStateManager(HwDcTrackerBaseReference.this.mGsmServiceStateTracker, HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone).setRac(-1);
                                HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged try switch 3G to 4G and set newrac to -1");
                            }
                            if (defaultApn.getState() != DctConstants.State.IDLE) {
                                if (defaultApn.getState() != DctConstants.State.FAILED) {
                                    isDisconnected = false;
                                    HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged try setup data again");
                                    if (!isDisconnected) {
                                        z = true;
                                    }
                                    DcTrackerUtils.cleanUpConnection(dcTracker, z, defaultApn);
                                    HwDcTrackerBaseReference.this.setupDataOnConnectableApns("cellLocationChanged", null);
                                    HwDcTrackerBaseReference.this.resetTryTimes();
                                }
                            }
                            isDisconnected = true;
                            HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged try setup data again");
                            if (!isDisconnected) {
                            }
                            DcTrackerUtils.cleanUpConnection(dcTracker, z, defaultApn);
                            HwDcTrackerBaseReference.this.setupDataOnConnectableApns("cellLocationChanged", null);
                            HwDcTrackerBaseReference.this.resetTryTimes();
                        }
                    }
                } catch (Exception e) {
                    Rlog.e(HwDcTrackerBaseReference.TAG, "Exception in CellStateHandler.handleMessage");
                }
            }
        }
    };
    private boolean removePreferredApn = true;

    /* renamed from: com.android.internal.telephony.dataconnection.HwDcTrackerBaseReference$6  reason: invalid class name */
    static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType = new int[IccCardApplicationStatus.AppType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_USIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_SIM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_RUIM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_CSIM.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    class AllowMmmsContentObserver extends ContentObserver {
        public AllowMmmsContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            boolean z = false;
            int allowMms = Settings.System.getInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), HwDcTrackerBaseReference.ENABLE_ALLOW_MMS, 0);
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            if (allowMms == 1) {
                z = true;
            }
            hwDcTrackerBaseReference.ALLOW_MMS = z;
        }
    }

    private class FdnAsyncQueryHandler extends AsyncQueryHandler {
        public FdnAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            long subId = (long) HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getSubId();
            boolean isFdnActivated1 = SystemProperties.getBoolean("gsm.hw.fdn.activated1", false);
            boolean isFdnActivated2 = SystemProperties.getBoolean("gsm.hw.fdn.activated2", false);
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            hwDcTrackerBaseReference.log("fddn onQueryComplete subId:" + subId + " ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2);
            if ((subId == 0 && isFdnActivated1) || (subId == 1 && isFdnActivated2)) {
                HwDcTrackerBaseReference.this.retryDataConnectionByFdn();
            }
        }
    }

    private class FdnChangeObserver extends ContentObserver {
        public FdnChangeObserver() {
            super(HwDcTrackerBaseReference.this.mDcTrackerBase);
        }

        public void onChange(boolean selfChange) {
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            hwDcTrackerBaseReference.log("fddn FdnChangeObserver onChange, selfChange:" + selfChange);
            HwDcTrackerBaseReference.this.asyncQueryContact();
        }
    }

    class NwModeContentObserver extends ContentObserver {
        public NwModeContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean Change) {
            if (HwDcTrackerBaseReference.isMultiSimEnabled) {
                if (TelephonyManager.getTelephonyProperty(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getPhoneId(), "gsm.data.gsm_only_not_allow_ps", "false").equals("false")) {
                    return;
                }
            } else if (!SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", false)) {
                return;
            }
            int unused = HwDcTrackerBaseReference.this.nwMode = HwNetworkTypeUtils.getNetworkModeFromDB(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext(), HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getSubId());
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            hwDcTrackerBaseReference.log("NwModeChangeObserver onChange nwMode = " + HwDcTrackerBaseReference.this.nwMode);
            if (HwDcTrackerBaseReference.this.mDcTrackerBase instanceof DcTracker) {
                DcTracker dcTracker = HwDcTrackerBaseReference.this.mDcTrackerBase;
                if (1 == HwDcTrackerBaseReference.this.nwMode) {
                    DcTrackerUtils.cleanUpAllConnections(dcTracker, true, "nwTypeChanged");
                } else if (1 == HwDcTrackerBaseReference.this.mNwOldMode) {
                    DcTrackerUtils.onTrySetupData(dcTracker, "nwTypeChanged");
                }
            }
            int unused2 = HwDcTrackerBaseReference.this.mNwOldMode = HwDcTrackerBaseReference.this.nwMode;
        }
    }

    private class PingThread extends Thread {
        private static final int PROCESS_STATUS_FAIL = -1;
        private static final int PROCESS_STATUS_OK = 0;
        private boolean isRecievedPingReply;

        private class PingProcessRunner extends Thread {
            private final Process process;
            protected int status = -1;

            public PingProcessRunner(Process process2) {
                this.process = process2;
            }

            public void run() {
                try {
                    this.status = this.process.waitFor();
                } catch (InterruptedException e) {
                    this.status = -1;
                }
            }
        }

        private PingThread() {
            this.isRecievedPingReply = false;
        }

        public void run() {
            String pingResultStr = "";
            BufferedReader buf = null;
            String serverName = "connectivitycheck.platform.hicloud.com";
            if (!HwDcTrackerBaseReference.this.isInChina()) {
                serverName = "www.google.com";
            }
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            hwDcTrackerBaseReference.debugLog("ping thread enter, server name = " + serverName);
            try {
                HwDcTrackerBaseReference.this.debugLog("pingThread begin to ping");
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("/system/bin/ping -c 1 -W 1 " + serverName);
                PingProcessRunner runner = new PingProcessRunner(process);
                runner.start();
                runner.join(3000);
                int status = runner.status;
                HwDcTrackerBaseReference hwDcTrackerBaseReference2 = HwDcTrackerBaseReference.this;
                hwDcTrackerBaseReference2.debugLog("pingThread, process.waitFor, status = " + status);
                if (status == 0) {
                    buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuffer stringBuffer = new StringBuffer();
                    while (true) {
                        String readLine = buf.readLine();
                        String line = readLine;
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(line);
                    }
                    pingResultStr = stringBuffer.toString();
                }
                HwDcTrackerBaseReference hwDcTrackerBaseReference3 = HwDcTrackerBaseReference.this;
                hwDcTrackerBaseReference3.debugLog("ping result:" + pingResultStr);
                if (status != 0 || pingResultStr.indexOf("1 packets transmitted, 1 received") < 0) {
                    this.isRecievedPingReply = false;
                    HwDcTrackerBaseReference.this.mDcTrackerBase.sendMessage(HwDcTrackerBaseReference.this.mDcTrackerBase.obtainMessage(270354));
                } else {
                    this.isRecievedPingReply = true;
                }
                HwDcTrackerBaseReference hwDcTrackerBaseReference4 = HwDcTrackerBaseReference.this;
                hwDcTrackerBaseReference4.debugLog("pingThread return is " + this.isRecievedPingReply);
                if (buf != null) {
                    try {
                        buf.close();
                    } catch (IOException e) {
                        Rlog.e(HwDcTrackerBaseReference.TAG, "close buffer got IO exception.");
                    }
                }
            } catch (IOException e2) {
                Rlog.e(HwDcTrackerBaseReference.TAG, "ping thread IOException.");
                if (buf != null) {
                    buf.close();
                }
            } catch (InterruptedException e3) {
                Rlog.e(HwDcTrackerBaseReference.TAG, "ping thread InterruptedException.");
                if (buf != null) {
                    buf.close();
                }
            } catch (Throwable th) {
                if (buf != null) {
                    try {
                        buf.close();
                    } catch (IOException e4) {
                        Rlog.e(HwDcTrackerBaseReference.TAG, "close buffer got IO exception.");
                    }
                }
                throw th;
            }
        }
    }

    static /* synthetic */ int access$512(HwDcTrackerBaseReference x0, int x1) {
        int i = x0.mDSUseDuration + x1;
        x0.mDSUseDuration = i;
        return i;
    }

    static {
        boolean z = true;
        if (((MMS_PROP >> 2) & 1) != 1) {
            z = false;
        }
        MMSIgnoreDSSwitchNotRoaming = z;
    }

    public HwDcTrackerBaseReference(DcTracker dcTrackerBase) {
        this.mDcTrackerBase = dcTrackerBase;
    }

    public void init() {
        this.ALLOW_MMS = Settings.System.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), ENABLE_ALLOW_MMS, 0) == 1;
        Uri allowMmsUri = Settings.System.CONTENT_URI;
        this.allowMmsObserver = new AllowMmmsContentObserver(this.mDcTrackerBase);
        this.mDcTrackerBase.mPhone.getContext().getContentResolver().registerContentObserver(allowMmsUri, true, this.allowMmsObserver);
        this.nwModeChangeObserver = new NwModeContentObserver(this.mDcTrackerBase);
        ContentResolver contentResolver = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + this.mDcTrackerBase.mPhone.getSubId()), true, this.nwModeChangeObserver);
        this.nwMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getSubId());
        this.mNwOldMode = this.nwMode;
        if (this.mDcTrackerBase.mPhone.getCallTracker() != null) {
            this.mDcTrackerBase.mPhone.getCallTracker().registerForVoiceCallEnded(this.handler, 3, null);
            this.mDcTrackerBase.mPhone.getCallTracker().registerForVoiceCallStarted(this.handler, 5, null);
        }
        this.mSubscription = Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mAlarmManager = (AlarmManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("alarm");
        filter.addAction(INTENT_SET_PREF_NETWORK_TYPE);
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN)) {
            this.mDcTrackerBase.mPhone.mCi.registerForLimitPDPAct(this.handler, 4, null);
            filter.addAction(INTENT_LIMIT_PDP_ACT_IND);
        }
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION_BT_CONNECTION_CHANGED);
        filter.addAction(INTENT_PDP_RESET_ALARM);
        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false)) {
            filter.addAction("com.android.telephony.opencard");
        }
        this.mDcTrackerBase.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mDcTrackerBase.mPhone);
        isSupportPidStatistics();
    }

    public void dispose() {
        if (this.allowMmsObserver != null) {
            this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.allowMmsObserver);
        }
        if (this.nwModeChangeObserver != null) {
            this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.nwModeChangeObserver);
        }
        if (this.mDcTrackerBase.mPhone.getCallTracker() != null) {
            this.mDcTrackerBase.mPhone.getCallTracker().unregisterForVoiceCallEnded(this.handler);
        }
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN)) {
            this.mDcTrackerBase.mPhone.mCi.unregisterForLimitPDPAct(this.handler);
        }
        if (this.mIntentReceiver != null) {
            this.mDcTrackerBase.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    private void onResetApn() {
        ApnContext apnContext = (ApnContext) this.mDcTrackerBase.mApnContexts.get("default");
        if (apnContext != null) {
            apnContext.setEnabled(true);
            apnContext.setDependencyMet(true);
        }
    }

    public void beforeHandleMessage(Message msg) {
        if (200 == msg.what) {
            onResetApn();
        }
    }

    public boolean isDataAllowedByApnContext(ApnContext apnContext) {
        if (isBipApnType(apnContext.getApnType())) {
            return true;
        }
        if (isGsmOnlyPsNotAllowed()) {
            log("in GsmMode not allowed PS!");
            return false;
        } else if (!isLimitPDPAct()) {
            return true;
        } else {
            log("PSCLEARCODE Limit PDP Act apnContext: " + apnContext);
            return false;
        }
    }

    public boolean isLimitPDPAct() {
        return this.mIsLimitPDPAct && isPSClearCodeRplmnMatched();
    }

    public boolean isPSClearCodeRplmnMatched() {
        if (!HuaweiTelephonyConfigs.isHisiPlatform() || this.mDcTrackerBase == null || this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.getServiceState() == null) {
            return false;
        }
        String operator = this.mDcTrackerBase.mPhone.getServiceState().getOperatorNumeric();
        if (TextUtils.isEmpty(PS_CLEARCODE_PLMN) || TextUtils.isEmpty(operator)) {
            return false;
        }
        return PS_CLEARCODE_PLMN.contains(operator);
    }

    private boolean isGsmOnlyPsNotAllowed() {
        boolean z = false;
        if (isMultiSimEnabled) {
            int subId = this.mDcTrackerBase.mPhone.getPhoneId();
            int networkMode = this.nwMode;
            if (IS_DUAL_4G_SUPPORTED && SIM_NUM > 1) {
                networkMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mDcTrackerBase.mPhone.getContext(), subId);
            }
            if (TelephonyManager.getTelephonyProperty(subId, "gsm.data.gsm_only_not_allow_ps", "false").equals("true") && 1 == networkMode) {
                z = true;
            }
            return z;
        }
        if (SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", false) && 1 == this.nwMode) {
            z = true;
        }
        return z;
    }

    public boolean isDataAllowedForRoaming(boolean isMms) {
        int allowMmsPropertyByPlmn = getallowMmsPropertyByPlmn();
        boolean z = false;
        if (-1 != allowMmsPropertyByPlmn) {
            boolean mmsOnRoaming = (allowMmsPropertyByPlmn & 1) == 1;
            if (!this.mDcTrackerBase.mPhone.getServiceState().getRoaming() || this.mDcTrackerBase.getDataRoamingEnabled() || ((this.ALLOW_MMS || mmsOnRoaming) && isMms)) {
                z = true;
            }
            return z;
        }
        if (!this.mDcTrackerBase.mPhone.getServiceState().getRoaming() || this.mDcTrackerBase.getDataRoamingEnabled() || ((this.ALLOW_MMS || MMS_ON_ROAMING) && isMms)) {
            z = true;
        }
        return z;
    }

    public void onAllApnFirstActiveFailed() {
        if (isMultiSimEnabled) {
            ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getPhoneId()).allApnActiveFailed();
            return;
        }
        ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext()).allApnActiveFailed();
    }

    public void onAllApnPermActiveFailed() {
        if (true == this.broadcastPrePostPay && GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            log("tryToActionPrePostPay.");
            GlobalParamsAdaptor.tryToActionPrePostPay();
            this.broadcastPrePostPay = false;
        }
        ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext()).getCust().handleAllApnPermActiveFailed(this.mDcTrackerBase.mPhone.getContext());
        HwPhoneService.getInstance().reportToBoosterForNoRetryPdp();
    }

    public boolean isBipApnType(String type) {
        if (HuaweiTelephonyConfigs.isModemBipEnable() || (!type.equals("bip0") && !type.equals("bip1") && !type.equals("bip2") && !type.equals("bip3") && !type.equals("bip4") && !type.equals("bip5") && !type.equals("bip6"))) {
            return false;
        }
        return true;
    }

    public ApnSetting fetchBipApn(ApnSetting preferredApn, ArrayList<ApnSetting> allApnSettings) {
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            ApnSetting mDataProfile = ApnSetting.fromString(SystemProperties.get("gsm.bip.apn"));
            if ("default".equals(SystemProperties.get("gsm.bip.apn"))) {
                if (preferredApn != null) {
                    log("find prefer apn, use this");
                    return preferredApn;
                }
                if (allApnSettings != null) {
                    int list_size = allApnSettings.size();
                    for (int i = 0; i < list_size; i++) {
                        ApnSetting apn = allApnSettings.get(i);
                        if (apn.canHandleType("default")) {
                            log("find the first default apn");
                            return apn;
                        }
                    }
                }
                log("find non apn for default bip");
                return null;
            } else if (mDataProfile != null) {
                log("fetchBipApn: global BIP mDataProfile=" + mDataProfile);
                return mDataProfile;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void log(String string) {
        Rlog.d(TAG, string);
    }

    public void setFirstTimeEnableData() {
        log("=PREPOSTPAY=, Data Setup Successful.");
        if (this.broadcastPrePostPay) {
            this.broadcastPrePostPay = false;
        }
    }

    public boolean needRemovedPreferredApn() {
        if (true != this.removePreferredApn || !GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            return false;
        }
        log("Remove preferred apn.");
        this.removePreferredApn = false;
        return true;
    }

    public String getDataRoamingSettingItem(String originItem) {
        if (!isMultiSimEnabled || this.mDcTrackerBase.mPhone.getPhoneId() != 1) {
            return originItem;
        }
        return DATA_ROAMING_SIM2;
    }

    public void disableGoogleDunApn(Context c, String apnData, ApnSetting dunSetting) {
        if (SystemProperties.getBoolean("ro.config.enable.gdun", false)) {
            ApnSetting dunSetting2 = ApnSetting.fromString("this is false");
        }
    }

    public boolean getAnyDataEnabledByApnContext(ApnContext apnContext, boolean enable) {
        int allowMmsPropertyByPlmn = getallowMmsPropertyByPlmn();
        boolean z = false;
        if (this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
            if (getXcapDataRoamingEnable() && "xcap".equals(apnContext.getApnType())) {
                return true;
            }
            if (-1 != allowMmsPropertyByPlmn) {
                boolean ignoreDSSwitchOnRoaming = ((allowMmsPropertyByPlmn >> 1) & 1) == 1;
                if (((this.ALLOW_MMS || ignoreDSSwitchOnRoaming) && "mms".equals(apnContext.getApnType())) || enable) {
                    z = true;
                }
                return z;
            }
            if (((this.ALLOW_MMS || MMSIgnoreDSSwitchOnRoaming) && "mms".equals(apnContext.getApnType())) || enable) {
                z = true;
            }
            return z;
        } else if (-1 != allowMmsPropertyByPlmn) {
            boolean ignoreDSSwitchNotRoaming = ((allowMmsPropertyByPlmn >> 2) & 1) == 1;
            if (((this.ALLOW_MMS || ignoreDSSwitchNotRoaming) && "mms".equals(apnContext.getApnType())) || enable) {
                z = true;
            }
            return z;
        } else {
            if (((this.ALLOW_MMS || MMSIgnoreDSSwitchNotRoaming) && "mms".equals(apnContext.getApnType())) || enable) {
                z = true;
            }
            return z;
        }
    }

    public boolean shouldDisableMultiPdps(boolean onlySingleDcAllowed) {
        if (!this.SUPPORT_MPDN && !SystemProperties.getBoolean("gsm.multipdp.plmn.matched", false)) {
            onlySingleDcAllowed = true;
            log("SUPPORT_MPDN: " + this.SUPPORT_MPDN);
        }
        if (isMultiSimEnabled) {
            int subId = this.mDcTrackerBase.mPhone.getPhoneId();
            if (subId == 0) {
                SystemProperties.set("gsm.check_is_single_pdp_sub1", Boolean.toString(onlySingleDcAllowed));
            } else if (subId == 1) {
                SystemProperties.set("gsm.check_is_single_pdp_sub2", Boolean.toString(onlySingleDcAllowed));
            }
        } else {
            SystemProperties.set("gsm.check_is_single_pdp", Boolean.toString(onlySingleDcAllowed));
        }
        return onlySingleDcAllowed;
    }

    public void setMPDN(boolean bMPDN) {
        if (bMPDN == this.SUPPORT_MPDN) {
            log("MPDN is same,Don't need change");
            return;
        }
        if (bMPDN) {
            int radioTech = this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
            if (ServiceState.isCdma(radioTech) && radioTech != 13) {
                log("technology is not EHRPD and ServiceState is CDMA,Can't set MPDN");
                return;
            }
        }
        this.SUPPORT_MPDN = bMPDN;
        log("SUPPORT_MPDN change to " + bMPDN);
    }

    public void setMPDNByNetWork(String plmnNetWork) {
        if (this.mDcTrackerBase.mPhone == null) {
            log("mPhone is null");
            return;
        }
        String plmnsConfig = Settings.System.getString(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "mpdn_plmn_matched_by_network");
        if (TextUtils.isEmpty(plmnsConfig)) {
            log("plmnConfig is Empty");
            return;
        }
        boolean bMPDN = false;
        String[] plmns = plmnsConfig.split(",");
        int length = plmns.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String plmn = plmns[i];
            if (!TextUtils.isEmpty(plmn) && plmn.equals(plmnNetWork)) {
                bMPDN = true;
                break;
            }
            i++;
        }
        setMPDN(bMPDN);
        log("setMpdnByNewNetwork done, bMPDN is " + bMPDN);
    }

    public String getCTOperatorNumeric(String operator) {
        String result = operator;
        if (!HuaweiTelephonyConfigs.isChinaTelecom() || this.mDcTrackerBase.mPhone.getPhoneId() != 0) {
            return result;
        }
        log("getCTOperatorNumeric: use china telecom operator=" + CT_CDMA_OPERATOR);
        return CT_CDMA_OPERATOR;
    }

    public ApnSetting makeHwApnSetting(Cursor cursor, String[] types) {
        return new HwApnSetting(cursor, types);
    }

    public boolean noNeedDoRecovery(ConcurrentHashMap mApnContexts) {
        return SystemProperties.getBoolean("persist.radio.hw.nodorecovery", false) || (SystemProperties.getBoolean("hw.ds.np.nopollstat", true) && !isActiveDefaultApnPreset(mApnContexts)) || this.mDcTrackerBase.mPhone.getServiceState().getDataRegState() != 0 || (this.mLastRadioResetTimestamp != 0 && SystemClock.elapsedRealtime() - this.mLastRadioResetTimestamp < ((long) DATA_STALL_ALARM_PUNISH_DELAY_IN_MS_DEFAULT));
    }

    public boolean isActiveDefaultApnPreset(ConcurrentHashMap<String, ApnContext> mApnContexts) {
        ApnContext apnContext = mApnContexts.get("default");
        if (apnContext != null && DctConstants.State.CONNECTED == apnContext.getState()) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null && (apnSetting instanceof HwApnSetting)) {
                HwApnSetting hwapnSetting = (HwApnSetting) apnSetting;
                StringBuilder sb = new StringBuilder();
                sb.append("current default apn is ");
                sb.append(hwapnSetting.isPreset() ? "preset" : "non-preset");
                log(sb.toString());
                return hwapnSetting.isPreset();
            }
        }
        return true;
    }

    public boolean isApnPreset(ApnSetting apnSetting) {
        if (apnSetting == null || !(apnSetting instanceof HwApnSetting)) {
            return true;
        }
        return ((HwApnSetting) apnSetting).isPreset();
    }

    public void getNetdPid() {
        int ret = -1;
        if (this.mDoRecoveryAddDnsProp) {
            try {
                this.mNetworkManager = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
                if (this.mNetworkManager != null) {
                    ret = this.mNetworkManager.getNetdPid();
                } else {
                    log("getNetdPid mNetdService is null");
                }
            } catch (RemoteException e) {
                log("getNetdPid mNetdService RemoteException");
            }
            this.netdPid = ret;
        }
        log("getNetdPid:" + ret + ",prop:" + this.mDoRecoveryAddDnsProp);
    }

    public void isSupportPidStatistics() {
        if (!this.mDoRecoveryAddDnsProp) {
            return;
        }
        if (new File(pidStatsPath).exists()) {
            this.isSupportPidStats = true;
        } else {
            this.isSupportPidStats = false;
        }
    }

    private long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00ea, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x00eb, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00ed, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x00ee, code lost:
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:?, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x00f4, code lost:
        throw r0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00ed A[ExcHandler: Throwable (r0v5 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:6:0x0018] */
    public long[] getDnsPacketTxRxSum() {
        Throwable th;
        Throwable th2;
        Throwable th3;
        char c = 2;
        long[] ret = {0, 0};
        if (!this.isSupportPidStats || this.netdPid == -1) {
            log("isSupportPidStats=" + this.isSupportPidStats + ",netdPid=" + this.netdPid);
        } else {
            try {
                FileInputStream fis = new FileInputStream(pidStatsPath);
                try {
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    try {
                        BufferedReader bReader = new BufferedReader(isr);
                        long udpTx = 0;
                        long udpRx = 0;
                        try {
                            String netdPidKey = ":" + String.valueOf(this.netdPid) + "_";
                            String[] allMobiles = TrafficStats.getMobileIfacesEx();
                            while (true) {
                                String readLine = bReader.readLine();
                                String line = readLine;
                                if (readLine == null) {
                                    break;
                                }
                                try {
                                    String[] tokens = line.split(" ");
                                    if (tokens.length > 20 && tokens[3].equals(NETD_PROCESS_UID) && (tokens[c].equals("netd") || tokens[c].contains(netdPidKey))) {
                                        long udpRx2 = udpRx;
                                        long udpTx2 = udpTx;
                                        for (String iface : allMobiles) {
                                            if (tokens[1].equals(iface)) {
                                                udpTx2 += parseLong(tokens[20]);
                                                udpRx2 += parseLong(tokens[14]);
                                            }
                                        }
                                        udpTx = udpTx2;
                                        udpRx = udpRx2;
                                    }
                                    c = 2;
                                } catch (Throwable th4) {
                                    th = th4;
                                    th2 = null;
                                    th3 = null;
                                    $closeResource(th3, bReader);
                                    throw th;
                                }
                            }
                            ret[0] = ret[0] + udpTx;
                            ret[1] = ret[1] + udpRx;
                            th2 = null;
                        } catch (Throwable th5) {
                            th = th5;
                            th2 = null;
                            th3 = null;
                            $closeResource(th3, bReader);
                            throw th;
                        }
                        try {
                            $closeResource(null, bReader);
                            $closeResource(null, isr);
                            $closeResource(null, fis);
                        } catch (Throwable th6) {
                            th = th6;
                            th = th2;
                            $closeResource(th, isr);
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        th2 = null;
                        th = th2;
                        $closeResource(th, isr);
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                }
            } catch (IOException e) {
                log("pidStatsPath not found");
            }
        }
        return ret;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public HwCustDcTracker getCust(DcTracker dcTracker) {
        return (HwCustDcTracker) HwCustUtils.createObj(HwCustDcTracker.class, new Object[]{dcTracker});
    }

    public void setupDataOnConnectableApns(String reason, String excludedApnType) {
        log("setupDataOnConnectableApns: " + reason + ", excludedApnType = " + excludedApnType);
        Iterator it = this.mDcTrackerBase.mPrioritySortedApnContexts.iterator();
        while (it.hasNext()) {
            ApnContext apnContext = (ApnContext) it.next();
            if (TextUtils.isEmpty(excludedApnType) || !excludedApnType.equals(apnContext.getApnType())) {
                log("setupDataOnConnectableApns: apnContext " + apnContext);
                if (apnContext.getState() == DctConstants.State.FAILED) {
                    apnContext.setState(DctConstants.State.IDLE);
                }
                if (apnContext.isConnectable()) {
                    log("setupDataOnConnectableApns: isConnectable() call trySetupData");
                    apnContext.setReason(reason);
                    this.mDcTrackerBase.onTrySetupData(apnContext);
                }
            }
        }
    }

    public boolean needRetryAfterDisconnected(DcFailCause cause) {
        if (DcFailCause.ERROR_UNSPECIFIED != cause) {
            return true;
        }
        String failCauseStr = SystemProperties.get("ril.ps_ce_reason", "");
        if (TextUtils.isEmpty(failCauseStr)) {
            return true;
        }
        for (String noRetryCause : CAUSE_NO_RETRY_AFTER_DISCONNECT.split(",")) {
            if (failCauseStr.equals(noRetryCause)) {
                return false;
            }
        }
        return true;
    }

    public void setRetryAfterDisconnectedReason(DataConnection dc, ArrayList<ApnContext> apnsToCleanup) {
        for (ApnContext apnContext : dc.mApnContexts.keySet()) {
            apnContext.setReason("noRetryAfterDisconnect");
        }
        apnsToCleanup.addAll(dc.mApnContexts.keySet());
    }

    public boolean isChinaTelecom(int slotId) {
        return HwTelephonyManagerInner.getDefault().isChinaTelecom(slotId);
    }

    public boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public boolean isCTDualModeCard(int sub) {
        int SubType = HwTelephonyManagerInner.getDefault().getCardType(sub);
        if (41 != SubType && 43 != SubType) {
            return false;
        }
        log("sub = " + sub + ", SubType = " + SubType + " is CT dual modem card");
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isInChina() {
        String mcc = null;
        String operatorNumeric = ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).getNetworkOperatorForPhone(this.mDcTrackerBase.mPhone.getSubId());
        if (operatorNumeric != null && operatorNumeric.length() > 3) {
            mcc = operatorNumeric.substring(0, 3);
            debugLog("isInChina current mcc = " + mcc);
        }
        if ("460".equals(mcc)) {
            return true;
        }
        return false;
    }

    public boolean isPingOk() {
        if (HwVSimUtils.isVSimOn()) {
            debugLog("isPineOk always ok for vsim on");
            return true;
        } else if (noNeedDoRecovery(this.mDcTrackerBase.mApnContexts) || this.mDcTrackerBase.mPhone.getServiceState().getDataRegState() != 0) {
            debugLog("isPineOk always false if not default apn or dataRegState not in service");
            return false;
        } else {
            startPingThread();
            return true;
        }
    }

    private void startPingThread() {
        debugLog("startPingThread() enter.");
        new PingThread().start();
    }

    public boolean isClearCodeEnabled() {
        return this.mIsClearCodeEnabled;
    }

    public void startListenCellLocationChange() {
        ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).listen(this.pslForCellLocation, 16);
    }

    public void stopListenCellLocationChange() {
        ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).listen(this.pslForCellLocation, 0);
    }

    public void operateClearCodeProcess(ApnContext apnContext, DcFailCause cause, int delay) {
        this.mDelayTime = delay;
        if (!cause.isPermanentFailure(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getSubId())) {
            this.mTryIndex = 0;
            log("CLEARCODE not isPermanentFailure ");
            return;
        }
        log("CLEARCODE isPermanentFailure,perhaps APN is wrong");
        boolean isClearcodeDcFailCause = cause == DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED || cause == DcFailCause.USER_AUTHENTICATION;
        if (!"default".equals(apnContext.getApnType()) || !isClearcodeDcFailCause) {
            this.mTryIndex = 0;
            apnContext.markApnPermanentFailed(apnContext.getApnSetting());
        } else {
            this.mTryIndex++;
            log("CLEARCODE mTryIndex increase,current mTryIndex = " + this.mTryIndex);
            if (this.mTryIndex >= 3) {
                if (isLteRadioTech()) {
                    this.mDcTrackerBase.mPhone.setPreferredNetworkType(3, null);
                    HwNetworkTypeUtils.saveNetworkModeToDB(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getSubId(), 3);
                    this.mGsmServiceStateTracker = this.mDcTrackerBase.mPhone.getServiceStateTracker();
                    HwServiceStateManager.getHwGsmServiceStateManager(this.mGsmServiceStateTracker, this.mDcTrackerBase.mPhone).setRac(-1);
                    log("CLEARCODE mTryIndex >= 3 and is LTE,switch 4G to 3G and set newrac to -1");
                } else {
                    log("CLEARCODE mTryIndex >= 3 and is 3G,show clearcode dialog");
                    if (this.mPSClearCodeDialog == null) {
                        this.mPSClearCodeDialog = createPSClearCodeDiag(cause);
                        if (this.mPSClearCodeDialog != null) {
                            this.mPSClearCodeDialog.show();
                        }
                    }
                    set2HourDelay();
                }
                this.mTryIndex = 0;
                apnContext.markApnPermanentFailed(apnContext.getApnSetting());
            }
        }
    }

    public void resetTryTimes() {
        if (isClearCodeEnabled()) {
            this.mTryIndex = 0;
            if (this.mAlarmManager != null && this.mAlarmIntent != null) {
                this.mAlarmManager.cancel(this.mAlarmIntent);
                log("CLEARCODE cancel Alarm resetTryTimes");
            }
        }
    }

    private boolean isLteRadioTech() {
        if (this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology() == 14) {
            return true;
        }
        return false;
    }

    public void setCurFailCause(AsyncResult ar) {
        if (isClearCodeEnabled()) {
            if (ar.result instanceof DcFailCause) {
                this.mCurFailCause = (DcFailCause) ar.result;
            } else {
                this.mCurFailCause = null;
            }
        }
    }

    private AlertDialog createPSClearCodeDiag(DcFailCause cause) {
        AlertDialog.Builder buider = new AlertDialog.Builder(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getContext().getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        if (cause == DcFailCause.USER_AUTHENTICATION) {
            buider.setMessage(33685827);
            log("CLEARCODE clear_code_29");
        } else if (cause != DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED) {
            return null;
        } else {
            buider.setMessage(33685828);
            log("CLEARCODE clear_code_33");
        }
        buider.setIcon(17301543);
        buider.setCancelable(false);
        buider.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                AlertDialog unused = HwDcTrackerBaseReference.this.mPSClearCodeDialog = null;
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(HwFullNetworkConstants.EVENT_RESET_OOS_FLAG);
        return dialog;
    }

    private void set2HourDelay() {
        int delayTime = SystemProperties.getInt("gsm.radio.debug.cause_delay", DELAY_2_HOUR);
        log("CLEARCODE dataRadioTech is 3G and mTryIndex >= 3,so set2HourDelay delayTime =" + delayTime);
        Intent intent = new Intent(INTENT_SET_PREF_NETWORK_TYPE);
        intent.putExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, 9);
        this.mAlarmIntent = PendingIntent.getBroadcast(this.mDcTrackerBase.mPhone.getContext(), 0, intent, 134217728);
        if (this.mAlarmManager != null) {
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delayTime), this.mAlarmIntent);
        }
    }

    public int getDelayTime() {
        if (this.mCurFailCause == DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED || this.mCurFailCause == DcFailCause.USER_AUTHENTICATION) {
            if (isLteRadioTech()) {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G;
            } else {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G;
            }
        }
        return this.mDelayTime;
    }

    /* access modifiers changed from: protected */
    public void onActionIntentSetNetworkType(Intent intent) {
        int networkType = intent.getIntExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, 9);
        int curPrefMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getSubId());
        log("CLEARCODE switch network type : " + networkType + " curPrefMode = " + curPrefMode);
        if (!(networkType == curPrefMode || curPrefMode == 2)) {
            this.mDcTrackerBase.mPhone.setPreferredNetworkType(networkType, null);
            log("CLEARCODE switch network type to 4G and set newRac to -1");
            HwNetworkTypeUtils.saveNetworkModeToDB(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getSubId(), networkType);
            this.mGsmServiceStateTracker = this.mDcTrackerBase.mPhone.getServiceStateTracker();
            HwServiceStateManager.getHwGsmServiceStateManager(this.mGsmServiceStateTracker, this.mDcTrackerBase.mPhone).setRac(-1);
        }
        ApnContext defaultApn = (ApnContext) this.mDcTrackerBase.mApnContexts.get("default");
        boolean z = true;
        boolean isDisconnected = defaultApn.getState() == DctConstants.State.IDLE || defaultApn.getState() == DctConstants.State.FAILED;
        log("CLEARCODE 2 hours of delay is over,try setup data");
        DcTracker dcTracker = this.mDcTrackerBase;
        if (isDisconnected) {
            z = false;
        }
        DcTrackerUtils.cleanUpConnection(dcTracker, z, defaultApn);
        setupDataOnConnectableApns(CLEARCODE_2HOUR_DELAY_OVER, null);
    }

    public void unregisterForImsiReady(IccRecords r) {
        r.unregisterForImsiReady(this.mDcTrackerBase);
    }

    public void registerForImsiReady(IccRecords r) {
        r.registerForImsiReady(this.mDcTrackerBase, 270338, null);
    }

    public void unregisterForRecordsLoaded(IccRecords r) {
        r.unregisterForRecordsLoaded(this.mDcTrackerBase);
    }

    public void registerForRecordsLoaded(IccRecords r) {
        r.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
    }

    public void registerForGetAdDone(UiccCardApplication newUiccApplication) {
        newUiccApplication.registerForGetAdDone(this.mDcTrackerBase, 270338, null);
    }

    public void unregisterForGetAdDone(UiccCardApplication newUiccApplication) {
        newUiccApplication.unregisterForGetAdDone(this.mDcTrackerBase);
    }

    public void registerForImsi(UiccCardApplication newUiccApplication, IccRecords newIccRecords) {
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN) || this.SETAPN_UNTIL_CARDLOADED) {
            newIccRecords.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
            return;
        }
        switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[newUiccApplication.getType().ordinal()]) {
            case 1:
            case 2:
                log("New USIM records found");
                newUiccApplication.registerForGetAdDone(this.mDcTrackerBase, 271144, null);
                break;
            case 3:
            case 4:
                log("New CSIM records found");
                newIccRecords.registerForImsiReady(this.mDcTrackerBase, 271144, null);
                break;
            default:
                log("New other records found");
                break;
        }
        newIccRecords.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
    }

    public boolean checkMvnoParams() {
        boolean result = false;
        String operator = this.mDcTrackerBase.getCTOperator(this.mDcTrackerBase.getOperatorNumeric());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId()))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId()));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            log("checkMvnoParams: selection=" + "numeric = ?");
            Cursor cursor = this.mDcTrackerBase.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, "numeric = ?", new String[]{operator}, "_id");
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    result = checkMvno(cursor);
                }
                cursor.close();
            }
        }
        log("checkMvnoParams: X result = " + result);
        return result;
    }

    private boolean checkMvno(Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                String mvnoType = cursor.getString(cursor.getColumnIndexOrThrow("mvno_type"));
                String mvnoMatchData = cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data"));
                if (!TextUtils.isEmpty(mvnoType) && !TextUtils.isEmpty(mvnoMatchData)) {
                    log("checkMvno: X has mvno paras");
                    return true;
                }
            } while (cursor.moveToNext());
        }
        return false;
    }

    public void registerForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.registerForFdnRecordsLoaded(this.mDcTrackerBase, 2, null);
        }
    }

    public void unregisterForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.unregisterForFdnRecordsLoaded(this.mDcTrackerBase);
        }
    }

    public void registerForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("registerForFdn");
            this.mUiccController.registerForFdnStatusChange(this.mDcTrackerBase, 1, null);
            this.mFdnChangeObserver = new FdnChangeObserver();
            ContentResolver cr = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
            cr.registerContentObserver(FDN_URL, true, this.mFdnChangeObserver);
            this.mFdnAsyncQuery = new FdnAsyncQueryHandler(cr);
        }
    }

    public void unregisterForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("unregisterForFdn");
            this.mUiccController.unregisterForFdnStatusChange(this.mDcTrackerBase);
            if (this.mFdnChangeObserver != null) {
                this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mFdnChangeObserver);
            }
        }
    }

    public boolean isPsAllowedByFdn() {
        long curSubId = (long) this.mDcTrackerBase.mPhone.getSubId();
        String isFdnActivated1 = SystemProperties.get("gsm.hw.fdn.activated1", "false");
        String isFdnActivated2 = SystemProperties.get("gsm.hw.fdn.activated2", "false");
        String isPSAllowedByFdn1 = SystemProperties.get("gsm.hw.fdn.ps.flag.exists1", "false");
        String isPSAllowedByFdn2 = SystemProperties.get("gsm.hw.fdn.ps.flag.exists2", "false");
        log("fddn isPSAllowedByFdn ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2 + " ,isPSAllowedByFdn1:" + isPSAllowedByFdn1 + " ,isPSAllowedByFdn2:" + isPSAllowedByFdn2);
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            if (curSubId == 0 && "true".equals(isFdnActivated1) && "false".equals(isPSAllowedByFdn1)) {
                return false;
            }
            if (curSubId == 1 && "true".equals(isFdnActivated2) && "false".equals(isPSAllowedByFdn2)) {
                return false;
            }
        }
        return true;
    }

    public void handleCustMessage(Message msg) {
        int i = msg.what;
        if (i != 271147) {
            switch (i) {
                case 1:
                case 2:
                    log("fddn msg.what = " + msg.what);
                    retryDataConnectionByFdn();
                    return;
                default:
                    return;
            }
        } else {
            log("handleMessage-EVENT_GET_ATTACH_INFO_DONE!");
            onGetAttachInfoDone((AsyncResult) msg.obj);
        }
    }

    public void retryDataConnectionByFdn() {
        if (this.mDcTrackerBase.mPhone.getSubId() != SubscriptionController.getInstance().getCurrentDds()) {
            log("fddn retryDataConnectionByFdn, not dds sub, do nothing.");
            return;
        }
        if (isPsAllowedByFdn()) {
            log("fddn retryDataConnectionByFdn, FDN status change and PS is enable, try setup data.");
            setupDataOnConnectableApns("psRestrictDisabled", null);
        } else {
            log("fddn retryDataConnectionByFdn, PS restricted by FDN, cleaup all connections.");
            this.mDcTrackerBase.cleanUpAllConnections(true, "psRestrictEnabled");
        }
    }

    /* access modifiers changed from: private */
    public void asyncQueryContact() {
        long subId = (long) this.mDcTrackerBase.mPhone.getSubId();
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mFdnAsyncQuery.startQuery(0, null, ContentUris.withAppendedId(FDN_URL, subId), new String[]{"number"}, null, null, null);
        }
    }

    public boolean isActiveDataSubscription() {
        log("isActiveDataSubscription getSubId= " + this.mDcTrackerBase.mPhone.getSubId() + "mCurrentDds" + SubscriptionController.getInstance().getCurrentDds());
        return this.mDcTrackerBase.mPhone.getSubId() == SubscriptionController.getInstance().getCurrentDds();
    }

    public int get4gSlot() {
        int slot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (slot == 0) {
        }
        return slot;
    }

    public int get2gSlot() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0 ? 1 : 0;
    }

    public void addIfacePhoneHashMap(DcAsyncChannel dcac, HashMap<String, Integer> mIfacePhoneHashMap) {
        LinkProperties tempLinkProperties = dcac.getLinkPropertiesSync();
        if (tempLinkProperties != null) {
            String iface = tempLinkProperties.getInterfaceName();
            if (iface != null) {
                mIfacePhoneHashMap.put(iface, Integer.valueOf(this.mDcTrackerBase.mPhone.getPhoneId()));
            }
        }
    }

    public int getVSimSubId() {
        return HwVSimManager.getDefault().getVSimSubId();
    }

    public void sendRoamingDataStatusChangBroadcast() {
        this.mDcTrackerBase.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.INTERNATIONAL_ROAMING_DATA_STATUS_CHANGED"));
    }

    public void sendDSMipErrorBroadcast() {
        if (SystemProperties.getBoolean("ro.config.hw_mip_error_dialog", false)) {
            this.mDcTrackerBase.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.DATA_CONNECTION_MOBILE_IP_ERROR"));
        }
    }

    public boolean enableTcpUdpSumForDataStall() {
        return SystemProperties.getBoolean("ro.hwpp_enable_tcp_udp_sum", false);
    }

    public String networkTypeToApnType(int networkType) {
        if (networkType == 0) {
            return "default";
        }
        if (networkType == 48) {
            return "internaldefault";
        }
        switch (networkType) {
            case 2:
                return "mms";
            case 3:
                return "supl";
            case 4:
                return "dun";
            case 5:
                return "hipri";
            default:
                switch (networkType) {
                    case 10:
                        return "fota";
                    case 11:
                        return "ims";
                    case 12:
                        return "cbs";
                    default:
                        switch (networkType) {
                            case 14:
                                return "ia";
                            case 15:
                                return "emergency";
                            default:
                                switch (networkType) {
                                    case 38:
                                        return "bip0";
                                    case 39:
                                        return "bip1";
                                    case 40:
                                        return "bip2";
                                    case 41:
                                        return "bip3";
                                    case 42:
                                        return "bip4";
                                    case 43:
                                        return "bip5";
                                    case 44:
                                        return "bip6";
                                    case HwVSimConstants.EVENT_CARD_POWER_ON_DONE /*45*/:
                                        return "xcap";
                                    default:
                                        log("Error mapping networkType " + networkType + " to apnType");
                                        return "";
                                }
                        }
                }
        }
    }

    public boolean isApnTypeDisabled(String apnType) {
        if (TextUtils.isEmpty(apnType)) {
            return false;
        }
        for (String type : "ro.hwpp.disabled_apn_type".split(",")) {
            if (apnType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNeedDataRoamingExpend() {
        if (this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.mIccRecords == null || this.mDcTrackerBase.mPhone.mIccRecords.get() == null) {
            log("mPhone or mIccRecords is null");
            return false;
        }
        boolean dataRoamState = false;
        boolean hasHwCfgConfig = false;
        try {
            Boolean dataRoam = (Boolean) HwCfgFilePolicy.getValue("hw_data_roam_option", SubscriptionManager.getSlotIndex(this.mDcTrackerBase.mPhone.getSubId()), Boolean.class);
            if (dataRoam != null) {
                dataRoamState = dataRoam.booleanValue();
                hasHwCfgConfig = true;
            }
            if (dataRoamState) {
                return true;
            }
            if (hasHwCfgConfig && !dataRoamState) {
                return false;
            }
            String plmnsConfig = Settings.System.getString(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "hw_data_roam_option");
            if (TextUtils.isEmpty(plmnsConfig)) {
                log("plmnConfig is Empty");
                return false;
            } else if ("ALL".equals(plmnsConfig)) {
                return true;
            } else {
                String mccmnc = ((IccRecords) this.mDcTrackerBase.mPhone.mIccRecords.get()).getOperatorNumeric();
                for (String plmn : plmnsConfig.split(",")) {
                    if (!TextUtils.isEmpty(plmn) && plmn.equals(mccmnc)) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            Rlog.e(TAG, "read data_roam_option failed");
        }
    }

    public boolean setDataRoamingScope(int scope) {
        log("dram setDataRoamingScope scope " + scope);
        if (scope < 0 || scope > 2) {
            return false;
        }
        if (getDataRoamingScope() != scope) {
            Settings.Global.putInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), getDataRoamingSettingItem("data_roaming"), scope);
            if (this.mDcTrackerBase.mPhone.getServiceState() != null && this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
                log("dram setDataRoamingScope send EVENT_ROAMING_ON");
                this.mDcTrackerBase.sendMessage(this.mDcTrackerBase.obtainMessage(270347));
            }
        }
        return true;
    }

    public int getDataRoamingScope() {
        try {
            return Settings.Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), getDataRoamingSettingItem("data_roaming"));
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    public boolean getDataRoamingEnabledWithNational() {
        boolean result = true;
        int dataRoamingScope = getDataRoamingScope();
        if (dataRoamingScope == 0 || (1 == dataRoamingScope && true == isInternationalRoaming())) {
            result = false;
        }
        log("dram getDataRoamingEnabledWithNational result " + result + " dataRoamingScope " + dataRoamingScope);
        return result;
    }

    public boolean isInternationalRoaming() {
        if (this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.mIccRecords == null || this.mDcTrackerBase.mPhone.mIccRecords.get() == null) {
            log("mPhone or mIccRecords is null");
            return false;
        } else if (this.mDcTrackerBase.mPhone.getServiceState() == null) {
            log("dram isInternationalRoaming ServiceState is not start up");
            return false;
        } else if (!this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
            log("dram isInternationalRoaming Current service state is not roaming, bail ");
            return false;
        } else {
            String simNumeric = ((IccRecords) this.mDcTrackerBase.mPhone.mIccRecords.get()).getOperatorNumeric();
            String operatorNumeric = this.mDcTrackerBase.mPhone.getServiceState().getOperatorNumeric();
            if (TextUtils.isEmpty(simNumeric) || TextUtils.isEmpty(operatorNumeric)) {
                log("dram isInternationalRoaming SIMNumeric or OperatorNumeric is not got!");
                return false;
            }
            log("dram isInternationalRoaming simNumeric " + simNumeric + " operatorNumeric " + operatorNumeric);
            if (simNumeric.length() <= 3 || operatorNumeric.length() <= 3 || simNumeric.substring(0, 3).equals(operatorNumeric.substring(0, 3))) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void onLimitPDPActInd(AsyncResult ar) {
        if (ar != null && ar.exception == null && ar.result != null) {
            int[] responseArray = (int[]) ar.result;
            if (responseArray != null && responseArray.length >= 2) {
                log("PSCLEARCODE onLimitPDPActInd result flag: " + responseArray[0] + " , cause: " + responseArray[1]);
                this.mIsLimitPDPAct = responseArray[0] == 1;
                DcFailCause cause = DcFailCause.fromInt(responseArray[1]);
                if (this.mIsLimitPDPAct && !isLteRadioTech()) {
                    showPSClearCodeDialog(cause);
                }
                if (!(this.mAlarmManager == null || this.mClearCodeLimitAlarmIntent == null)) {
                    this.mAlarmManager.cancel(this.mClearCodeLimitAlarmIntent);
                    this.mClearCodeLimitAlarmIntent = null;
                }
                Intent intent = new Intent(INTENT_LIMIT_PDP_ACT_IND);
                intent.putExtra(IS_LIMIT_PDP_ACT, this.mIsLimitPDPAct);
                intent.addFlags(268435456);
                this.mClearCodeLimitAlarmIntent = PendingIntent.getBroadcast(this.mDcTrackerBase.mPhone.getContext(), 0, intent, 134217728);
                if (this.mAlarmManager != null) {
                    this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + PS_CLEARCODE_LIMIT_PDP_ACT_DELAY, this.mClearCodeLimitAlarmIntent);
                }
                log("PSCLEARCODE startAlarmForLimitPDPActInd: delay=" + PS_CLEARCODE_LIMIT_PDP_ACT_DELAY + " flag=" + this.mIsLimitPDPAct);
            }
        }
    }

    private void showPSClearCodeDialog(DcFailCause cause) {
        if (this.mPSClearCodeDialog == null) {
            this.mPSClearCodeDialog = createPSClearCodeDiag(cause);
            if (this.mPSClearCodeDialog != null) {
                this.mPSClearCodeDialog.show();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onActionIntentLimitPDPActInd(Intent intent) {
        if (intent != null) {
            boolean isLimitPDPAct = intent.getBooleanExtra(IS_LIMIT_PDP_ACT, false);
            log("PSCLEARCODE onActionIntentLimitPDPActInd: flag = " + isLimitPDPAct);
            if (!isLimitPDPAct) {
                this.mDcTrackerBase.updateApnContextState();
                setupDataOnConnectableApns("limitPDPActDisabled", null);
            }
        }
    }

    public long updatePSClearCodeApnContext(AsyncResult ar, ApnContext apnContext, long delay) {
        long delayTime = delay;
        if (ar == null || apnContext == null || apnContext.getApnSetting() == null) {
            return delayTime;
        }
        DcFailCause dcFailCause = (DcFailCause) ar.result;
        if (DcFailCause.PDP_ACTIVE_LIMIT == dcFailCause) {
            log("PSCLEARCODE retry APN. new delay = -1");
            return -1;
        }
        if (DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED == dcFailCause || DcFailCause.USER_AUTHENTICATION == dcFailCause) {
            if (isLteRadioTech()) {
                delayTime = PS_CLEARCODE_APN_DELAY_MILLIS_4G;
            } else {
                delayTime = PS_CLEARCODE_APN_DELAY_MILLIS_2G_3G;
            }
            apnContext.getApnSetting().permanentFailed = false;
        }
        log("PSCLEARCODE retry APN. new delay = " + delayTime);
        return delayTime;
    }

    /* access modifiers changed from: private */
    public void onVoiceCallEndedHw() {
        log("onVoiceCallEndedHw");
        if (!HwModemCapability.isCapabilitySupport(0)) {
            int currentSub = this.mDcTrackerBase.mPhone.getPhoneId();
            SubscriptionController subscriptionController = SubscriptionController.getInstance();
            int defaultDataSubId = subscriptionController.getDefaultDataSubId();
            if (subscriptionController.getSubState(defaultDataSubId) == 0 && currentSub != defaultDataSubId) {
                if (HwVSimUtils.isVSimInProcess() || HwVSimUtils.isVSimCauseCardReload()) {
                    debugLog("vsim is in process or cardreload, not set dds to " + currentSub);
                } else {
                    debugLog("defaultDataSub " + defaultDataSubId + " is inactive, set dataSubId to " + currentSub);
                    subscriptionController.setDefaultDataSubId(currentSub);
                }
            }
            if (this.mDcTrackerBase.mPhone.getServiceStateTracker() != null) {
                this.mDcTrackerBase.mPhone.notifyServiceStateChangedP(this.mDcTrackerBase.mPhone.getServiceStateTracker().mSS);
            }
        }
    }

    public boolean isDataConnectivityDisabled(int slotId, String tag) {
        return HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(slotId, tag);
    }

    /* access modifiers changed from: private */
    public void debugLog(String logStr) {
        log(logStr);
    }

    public ApnSetting getCustPreferredApn(ArrayList<ApnSetting> apnSettings) {
        if (CUST_PREFERRED_APN == null || "".equals(CUST_PREFERRED_APN)) {
            return null;
        }
        if (apnSettings == null || apnSettings.isEmpty()) {
            log("getCustPreferredApn mAllApnSettings == null");
            return null;
        }
        int list_size = apnSettings.size();
        for (int i = 0; i < list_size; i++) {
            ApnSetting p = apnSettings.get(i);
            if (CUST_PREFERRED_APN.equals(p.apn)) {
                log("getCustPreferredApn: X found apnSetting" + p);
                return p;
            }
        }
        log("getCustPreferredApn: not found apn: " + CUST_PREFERRED_APN);
        return null;
    }

    public boolean isRoamingPushDisabled() {
        return HwTelephonyManagerInner.getDefault().isRoamingPushDisabled();
    }

    public boolean processAttDataRoamingOff() {
        boolean z = false;
        if (!IS_ATT) {
            return false;
        }
        if (Settings.Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "ATT_DOMESTIC_DATA", 0) != 0) {
            z = true;
        }
        boolean domesticDataEnabled = z;
        log("processAttRoamingOff domesticDataEnabled = " + domesticDataEnabled);
        this.mDcTrackerBase.setUserDataEnabled(domesticDataEnabled);
        if (domesticDataEnabled) {
            this.mDcTrackerBase.notifyOffApnsOfAvailability("roamingOff");
            setupDataOnConnectableApns("roamingOff", null);
        } else {
            this.mDcTrackerBase.cleanUpAllConnections(true, "roamingOff");
            this.mDcTrackerBase.notifyOffApnsOfAvailability("roamingOff");
        }
        return true;
    }

    public boolean processAttDataRoamingOn() {
        if (!IS_ATT) {
            return false;
        }
        if (!this.mDcTrackerBase.mPhone.getServiceState().getDataRoaming()) {
            return true;
        }
        boolean dataRoamingEnabled = this.mDcTrackerBase.getDataRoamingEnabled();
        this.mDcTrackerBase.setUserDataEnabled(dataRoamingEnabled);
        if (dataRoamingEnabled) {
            log("onRoamingOn: setup data on in internal roaming");
            setupDataOnConnectableApns("roamingOn", null);
            this.mDcTrackerBase.notifyDataConnection("roamingOn");
        } else {
            log("onRoamingOn: Tear down data connection on internal roaming.");
            this.mDcTrackerBase.cleanUpAllConnections(true, "roamingOn");
            this.mDcTrackerBase.notifyOffApnsOfAvailability("roamingOn");
        }
        return true;
    }

    public boolean getXcapDataRoamingEnable() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configLoader != null) {
            b = configLoader.getConfigForSubId(this.mDcTrackerBase.mPhone.getSubId());
        }
        boolean xcapDataRoamingEnable = false;
        if (b != null) {
            xcapDataRoamingEnable = b.getBoolean(XCAP_DATA_ROAMING_ENABLE);
        }
        log("getXcapDataRoamingEnable:xcapDataRoamingEnable " + xcapDataRoamingEnable);
        try {
            Boolean getXcapEnable = (Boolean) HwCfgFilePolicy.getValue(XCAP_DATA_ROAMING_ENABLE, SubscriptionManager.getSlotIndex(this.mDcTrackerBase.mPhone.getSubId()), Boolean.class);
            if (getXcapEnable != null) {
                return getXcapEnable.booleanValue();
            }
            return xcapDataRoamingEnable;
        } catch (Exception e) {
            log("Exception: read carrier_xcap_data_roaming_switch failed");
            return xcapDataRoamingEnable;
        }
    }

    public void updateDSUseDuration() {
        if (mIsScreenOn) {
            this.mDSUseDuration++;
            log("updateDSUseDuration: Update mDSUseDuration: " + this.mDSUseDuration);
            long curTime = SystemClock.elapsedRealtime();
            if (curTime > this.mNextReportDSUseDurationStamp) {
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentDSUseStatistics(this.mDcTrackerBase.mPhone, this.mDSUseDuration);
                log("updateDSUseDuration: report mDSUseDuration: " + this.mDSUseDuration);
                this.mDSUseDuration = 0;
                this.mNextReportDSUseDurationStamp = 3600000 + curTime;
            }
        }
    }

    private int getallowMmsPropertyByPlmn() {
        int allowMmsPropertyInt;
        int allowMmsPropertyInt2 = -1;
        try {
            int subId = this.mDcTrackerBase.mPhone.getSubId();
            Integer hwAlwaysAllowMms = (Integer) HwCfgFilePolicy.getValue(ALLOW_MMS_PROPERTY_INT, SubscriptionManager.getSlotIndex(subId), Integer.class);
            if (hwAlwaysAllowMms != null) {
                allowMmsPropertyInt = hwAlwaysAllowMms.intValue();
            } else {
                CarrierConfigManager configLoader = (CarrierConfigManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("carrier_config");
                if (configLoader == null) {
                    return -1;
                }
                PersistableBundle bundle = configLoader.getConfigForSubId(subId);
                if (bundle == null) {
                    return -1;
                }
                allowMmsPropertyInt = bundle.getInt(ALLOW_MMS_PROPERTY_INT, -1);
            }
            if (allowMmsPropertyInt >= 0) {
                if (allowMmsPropertyInt > 7) {
                }
                allowMmsPropertyInt2 = allowMmsPropertyInt;
                log("getallowMmsPropertyByPlmn:allowMmsPropertyInt " + allowMmsPropertyInt2);
                return allowMmsPropertyInt2;
            }
            allowMmsPropertyInt = -1;
            allowMmsPropertyInt2 = allowMmsPropertyInt;
            log("getallowMmsPropertyByPlmn:allowMmsPropertyInt " + allowMmsPropertyInt2);
        } catch (Exception e) {
            log("Exception: read allow_mms_property_int failed");
        }
        return allowMmsPropertyInt2;
    }

    public boolean getAttachedStatus(boolean attached) {
        if (PhoneFactory.IS_QCOM_DUAL_LTE_STACK || DISABLE_GW_PS_ATTACH) {
            int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
            if (dataSub != this.mDcTrackerBase.mPhone.getSubId() && SubscriptionManager.isUsableSubIdValue(dataSub)) {
                return true;
            }
        }
        return attached;
    }

    public boolean isBtConnected() {
        return this.mIsBtConnected;
    }

    public boolean isWifiConnected() {
        ConnectivityManager cm = ConnectivityManager.from(this.mDcTrackerBase.mPhone.getContext());
        if (cm != null) {
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.getType() == 1) {
                log("isWifiConnected return true");
                return true;
            }
        }
        return false;
    }

    public boolean isDataNeededWithWifiAndBt() {
        boolean isDataAlwaysOn = Settings.Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "mobile_data_always_on", 0) != 0;
        if (isDataAlwaysOn) {
            log("isDataNeededWithWifiAndBt:isDataAlwaysOn = true");
        }
        if (isDataAlwaysOn) {
            return true;
        }
        if (isBtConnected() || isWifiConnected()) {
            return false;
        }
        return true;
    }

    public void updateLastRadioResetTimestamp() {
        this.mLastRadioResetTimestamp = SystemClock.elapsedRealtime();
    }

    public boolean needRestartRadioOnError(ApnContext apnContext, DcFailCause cause) {
        TelephonyManager tm = TelephonyManager.getDefault();
        if (apnContext == null || tm == null) {
            return false;
        }
        long now = SystemClock.elapsedRealtime();
        if (!"default".equals(apnContext.getApnType()) || tm.getCallState() != 0 || get4gSlot() != this.mDcTrackerBase.mPhone.getSubId() || !PERMANENT_ERROR_HEAL_PROP || ((now - this.mLastRadioResetTimestamp <= ((long) RESTART_RADIO_PUNISH_TIME_IN_MS) && 0 != this.mLastRadioResetTimestamp) || !apnContext.restartOnError(cause.getErrorCode()))) {
            return false;
        }
        log("needRestartRadioOnError return true");
        this.mLastRadioResetTimestamp = now;
        HwPhoneService.getInstance().reportToBoosterForNoRetryPdp();
        return true;
    }

    /* access modifiers changed from: protected */
    public void onGetAttachInfoDone(AsyncResult ar) {
        log("onGetAttachInfoDoneX");
        if (ar.exception != null) {
            log("Exception occurred, failed to report the LTE attach info");
            return;
        }
        LteAttachInfo stAttachInfo = (LteAttachInfo) ar.result;
        if (stAttachInfo == null) {
            log("onGetAttachInfoDone::(LteAttachInfo)ar.result is null.");
        } else if (!isValidLteApn(stAttachInfo.apn)) {
            log("onGetAttachInfoDone::stAttachInfo.apn is inValid Lte Apn.");
        } else if (!isValidProtocol(stAttachInfo.protocol)) {
            log("onGetAttachInfoDone::stAttachInfo.protocol is inValid protocol.");
        } else {
            ApnSetting attachedApnSettings = convertHalAttachInfo(stAttachInfo);
            if (attachedApnSettings != null) {
                this.mAttachedApnSettings = attachedApnSettings;
                this.mDcTrackerBase.onTrySetupData("");
            } else {
                log("attachedApnSetting is null, will not re-try SetupData process!");
            }
        }
    }

    /* access modifiers changed from: protected */
    public ApnSetting convertHalAttachInfo(LteAttachInfo attachInfo) {
        LteAttachInfo lteAttachInfo = attachInfo;
        log("convertHalAttachInfo");
        String apn = lteAttachInfo.apn;
        String protocol = covertProtocol(lteAttachInfo.protocol);
        String operator = this.mDcTrackerBase.getOperatorNumeric();
        log("convertHalAttachInfo, operator is:" + operator);
        String str = operator;
        String str2 = apn;
        String str3 = operator;
        ApnSetting apnSetting = new ApnSetting(0, str, "default lte apn", str2, "", "", "", "", "", "", "", -1, new String[]{"default"}, protocol, protocol, true, 0, 0, 0, false, 0, 0, 0, 0, "", "");
        return apnSetting;
    }

    private String covertProtocol(int protocol) {
        log("covertProtocol - protocol is:" + protocol);
        switch (protocol) {
            case 1:
                return "ipv4";
            case 2:
                return "ipv6";
            case 3:
                return "ipv4v6";
            default:
                log("unknown protocol = " + protocol);
                return "unknown";
        }
    }

    private boolean isValidLteApn(String apn) {
        if (apn == null) {
            log("apn is null.");
            return false;
        } else if (!apn.toLowerCase().contains("ims")) {
            return true;
        } else {
            log("apn contain string ims.");
            return false;
        }
    }

    private boolean isValidProtocol(int protocol) {
        if (1 == protocol || 2 == protocol || 3 == protocol) {
            return true;
        }
        log("Protocol is:" + protocol);
        return false;
    }

    public void updateApnLists(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        if (!isLTENetworks()) {
            log("Currently is not over LTE network, Do nothing!");
        } else if (this.mAttachedApnSettings == null) {
            getAttachedApnSettings();
        } else if (this.mAttachedApnSettings.canHandleType(requestedApnType) && this.mAttachedApnSettings.numeric != null && this.mAttachedApnSettings.numeric.equals(operator) && ServiceState.bitmaskHasTech(this.mAttachedApnSettings.bearerBitmask, radioTech) && apnList != null) {
            log("add mAttachedApnSettings into apnlist!");
            apnList.add(this.mAttachedApnSettings);
        }
    }

    private boolean isLTENetworks() {
        int dataRadioTech = this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
        log("dataRadioTech = " + dataRadioTech);
        return ServiceState.isLte(dataRadioTech);
    }

    public void getAttachedApnSettings() {
        log("getAttachedApnSettings");
        this.mDcTrackerBase.mPhone.mCi.getAttachedApnSettings(this.mDcTrackerBase.obtainMessage(271147));
    }

    public void setAttachedApnSetting(ApnSetting apnSetting) {
        log("setAttachedApnSetting");
        this.mAttachedApnSettings = apnSetting;
    }

    public ApnSetting getAttachedApnSetting() {
        log("getAttachedApnSetting");
        return this.mAttachedApnSettings;
    }

    public void startPdpResetAlarm(int delay) {
        if (this.mDcTrackerBase.mPhone.getPhoneType() == 2 && SystemProperties.getBoolean("hw.dct.psrecovery", false)) {
            this.mPdpResetAlarmTag++;
            log("startPdpResetAlarm for CDMA: tag=" + this.mPdpResetAlarmTag + " delay=" + (delay / 1000) + "s");
            Intent intent = new Intent(INTENT_PDP_RESET_ALARM);
            intent.putExtra(PDP_RESET_ALARM_TAG_EXTRA, this.mPdpResetAlarmTag);
            log("startPdpResetAlarm: delay=" + delay + " action=" + intent.getAction());
            this.mPdpResetAlarmIntent = PendingIntent.getBroadcast(this.mDcTrackerBase.mPhone.getContext(), 0, intent, 134217728);
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delay), this.mPdpResetAlarmIntent);
        }
    }

    public void stopPdpResetAlarm() {
        boolean cdmaPsRecoveryEnabled = false;
        if (this.mDcTrackerBase.mPhone.getPhoneType() == 2 && SystemProperties.getBoolean("hw.dct.psrecovery", false)) {
            cdmaPsRecoveryEnabled = true;
        }
        if (cdmaPsRecoveryEnabled) {
            log("stopPdpResetAlarm: current tag=" + this.mPdpResetAlarmTag + " mPdpResetAlarmIntent=" + this.mPdpResetAlarmIntent);
            this.mPdpResetAlarmTag = this.mPdpResetAlarmTag + 1;
            if (this.mPdpResetAlarmIntent != null) {
                this.mAlarmManager.cancel(this.mPdpResetAlarmIntent);
                this.mPdpResetAlarmIntent = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onActionIntentPdpResetAlarm(Intent intent) {
        log("onActionIntentPdpResetAlarm: action=" + intent.getAction());
        int tag = intent.getIntExtra(PDP_RESET_ALARM_TAG_EXTRA, 0);
        if (this.mPdpResetAlarmTag != tag) {
            log("onPdpRestAlarm: ignore, tag=" + tag + " expecting " + this.mPdpResetAlarmTag);
            return;
        }
        this.mDcTrackerBase.cleanUpAllConnections("pdpReset");
    }

    public ApnSetting getApnForCT() {
        if (!isCTSimCard(this.mDcTrackerBase.mPhone.getPhoneId())) {
            log("getApnForCT not isCTSimCard");
            return null;
        } else if (this.mDcTrackerBase.getAllApnList() == null || this.mDcTrackerBase.getAllApnList().isEmpty()) {
            log("getApnForCT mAllApnSettings == null");
            return null;
        } else if (get2gSlot() == this.mDcTrackerBase.mPhone.getSubId() && !isCTDualModeCard(get2gSlot())) {
            log("getApnForCT otherslot == mPhone.getSubId() && !isCTDualModeCard(otherslot)");
            return null;
        } else if (this.mDcTrackerBase.mPhone.getServiceState().getOperatorNumeric() == null) {
            log("getApnForCT mPhone.getServiceState().getOperatorNumeric() == null");
            return null;
        } else {
            ApnSetting preferredApn = this.mDcTrackerBase.getPreferredApnHw();
            if (preferredApn != null && !isApnPreset(preferredApn)) {
                return null;
            }
            ApnSetting apnSetting = null;
            this.mCurrentState = getCurState();
            int matchApnId = matchApnId(this.mCurrentState);
            if (-1 == matchApnId) {
                switch (this.mCurrentState) {
                    case 0:
                        if (!isCTCardForFullNet()) {
                            apnSetting = setApnForCT(CT_NOT_ROAMING_APN_PREFIX);
                            break;
                        } else {
                            log("getApnForCT: select ctnet for fullNet product");
                            apnSetting = setApnForCT(CT_ROAMING_APN_PREFIX);
                            break;
                        }
                    case 1:
                    case 2:
                    case 3:
                        apnSetting = setApnForCT(CT_ROAMING_APN_PREFIX);
                        break;
                    case 4:
                        apnSetting = setApnForCT(CT_LTE_APN_PREFIX);
                        break;
                    default:
                        log("Error in CurrentState" + this.mCurrentState);
                        break;
                }
            } else {
                this.mDcTrackerBase.setPreferredApnHw(matchApnId);
            }
            return apnSetting;
        }
    }

    private ApnSetting setApnForCT(String apn) {
        if (apn == null || "".equals(apn)) {
            return null;
        }
        ContentResolver resolver = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
        if (this.mDcTrackerBase.getAllApnList() == null || this.mDcTrackerBase.getAllApnList().isEmpty() || resolver == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        String subId = Long.toString((long) this.mDcTrackerBase.mPhone.getSubId());
        DcTracker dcTracker = this.mDcTrackerBase;
        Uri uri = Uri.withAppendedPath(DcTracker.PREFERAPN_NO_UPDATE_URI_USING_SUBID, subId);
        int apnSize = this.mDcTrackerBase.getAllApnList().size();
        int i = 0;
        while (i < apnSize) {
            ApnSetting dp = (ApnSetting) this.mDcTrackerBase.getAllApnList().get(i);
            if (!apn.equals(dp.apn) || !dp.canHandleType("default") || (!(!isLTENetwork() || dp.bearer == 13 || dp.bearer == 14) || (!isLTENetwork() && (dp.bearer == 13 || dp.bearer == 14)))) {
                i++;
            } else {
                resolver.delete(uri, null, null);
                values.put(APN_ID, Integer.valueOf(dp.id));
                resolver.insert(uri, values);
                return dp;
            }
        }
        return null;
    }

    private int getCurState() {
        int currentStatus = -1;
        if (isLTENetwork()) {
            currentStatus = 4;
        } else if (this.mDcTrackerBase.mPhone.getPhoneType() == 2) {
            currentStatus = TelephonyManager.getDefault().isNetworkRoaming(get4gSlot()) ? 1 : 0;
        } else if (this.mDcTrackerBase.mPhone.getPhoneType() == 1) {
            if (get4gSlot() == this.mDcTrackerBase.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(get4gSlot())) {
                currentStatus = 2;
            } else if (get2gSlot() == this.mDcTrackerBase.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(get2gSlot())) {
                currentStatus = 3;
            }
        }
        log("getCurState:CurrentStatus =" + currentStatus);
        return currentStatus;
    }

    /* access modifiers changed from: protected */
    public boolean isCTCardForFullNet() {
        if (!isFullNetworkSupported()) {
            return false;
        }
        return isCTSimCard(this.mDcTrackerBase.mPhone.getPhoneId());
    }

    private int matchApnId(int sign) {
        String preferredApnIdSlot;
        ContentResolver cr = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
        int matchId = -1;
        if (isMultiSimEnabled) {
            String is4gSlot = get4gSlot() == this.mDcTrackerBase.mPhone.getSubId() ? "4gSlot" : "2gSlot";
            preferredApnIdSlot = PREFERRED_APN_ID + is4gSlot;
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        String preferredApnIdSlot2 = preferredApnIdSlot;
        try {
            String preferredApnIdSlot3 = Settings.System.getString(cr, preferredApnIdSlot2);
            log("MatchApnId:LastApnId: " + preferredApnIdSlot3 + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot2);
            if (preferredApnIdSlot3 != null) {
                String[] ApId = preferredApnIdSlot3.split(",");
                if (5 != ApId.length || ApId[this.mCurrentState] == null) {
                    Settings.System.putString(cr, preferredApnIdSlot2, this.mDefaultApnId);
                } else if (!NETD_PROCESS_UID.equals(ApId[this.mCurrentState])) {
                    matchId = Integer.parseInt(ApId[this.mCurrentState]);
                }
            } else {
                Settings.System.putString(cr, preferredApnIdSlot2, this.mDefaultApnId);
            }
        } catch (Exception ex) {
            log("MatchApnId got exception =" + ex);
            Settings.System.putString(cr, preferredApnIdSlot2, this.mDefaultApnId);
        }
        return matchId;
    }

    public boolean isLTENetwork() {
        int dataRadioTech = this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
        log("dataRadioTech = " + dataRadioTech);
        if (dataRadioTech == 13 || dataRadioTech == 14) {
            return true;
        }
        return false;
    }

    public void updateApnId() {
        String preferredApnIdSlot;
        ContentResolver cr = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
        if (isMultiSimEnabled) {
            String is4gSlot = get4gSlot() == this.mDcTrackerBase.mPhone.getSubId() ? "4gSlot" : "2gSlot";
            preferredApnIdSlot = PREFERRED_APN_ID + is4gSlot;
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        String preferredApnIdSlot2 = preferredApnIdSlot;
        try {
            String preferredApnIdSlot3 = Settings.System.getString(cr, preferredApnIdSlot2);
            this.mCurrentState = getCurState();
            log("updateApnId:LastApnId: " + preferredApnIdSlot3 + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot2);
            if (preferredApnIdSlot3 != null) {
                String[] ApId = preferredApnIdSlot3.split(",");
                ApnSetting CurPreApn = this.mDcTrackerBase.getPreferredApnHw();
                StringBuffer temApnId = new StringBuffer();
                if (5 != ApId.length || ApId[this.mCurrentState] == null) {
                    Settings.System.putString(cr, preferredApnIdSlot2, this.mDefaultApnId);
                } else {
                    if (CurPreApn == null) {
                        log("updateApnId:CurPreApn: CurPreApn == null");
                        ApId[this.mCurrentState] = NETD_PROCESS_UID;
                    } else {
                        log("updateApnId:CurPreApn: " + CurPreApn + ", CurPreApnId: " + Integer.toString(CurPreApn.id));
                        ApId[this.mCurrentState] = Integer.toString(CurPreApn.id);
                    }
                    for (int i = 0; i < ApId.length; i++) {
                        temApnId.append(ApId[i]);
                        if (i != ApId.length - 1) {
                            temApnId.append(",");
                        }
                    }
                    Settings.System.putString(cr, preferredApnIdSlot2, temApnId.toString());
                }
                return;
            }
            Settings.System.putString(cr, preferredApnIdSlot2, this.mDefaultApnId);
        } catch (Exception ex) {
            log("updateApnId got exception =" + ex);
            Settings.System.putString(cr, preferredApnIdSlot2, this.mDefaultApnId);
        }
    }

    public boolean needSetCTProxy(ApnSetting apn) {
        boolean needSet = false;
        if (!isCTSimCard(this.mDcTrackerBase.mPhone.getPhoneId())) {
            return false;
        }
        String networkOperatorNumeric = this.mDcTrackerBase.mPhone.getServiceState().getOperatorNumeric();
        if (!(apn == null || apn.apn == null || !apn.apn.contains(CT_NOT_ROAMING_APN_PREFIX) || networkOperatorNumeric == null || !"46012".equals(networkOperatorNumeric))) {
            needSet = true;
        }
        return needSet;
    }

    public void setCtProxy(DcAsyncChannel dcac) {
        try {
            dcac.setLinkPropertiesHttpProxySync(new ProxyInfo("10.0.0.200", Integer.parseInt("80"), "127.0.0.1"));
        } catch (NumberFormatException e) {
            log("onDataSetupComplete: NumberFormatException making ProxyProperties for CT.");
        }
    }

    public void onVpStatusChanged(AsyncResult ar) {
        log("onVpStatusChanged");
        if (ar.exception != null) {
            log("Exception occurred, failed to report the rssi and ecio.");
            return;
        }
        this.mDcTrackerBase.mVpStatus = ((Integer) ar.result).intValue();
        log("onVpStatusChanged, mVpStatus:" + this.mDcTrackerBase.mVpStatus);
        DcTracker dcTracker = this.mDcTrackerBase;
        if (1 == this.mDcTrackerBase.mVpStatus) {
            onVPStarted();
        } else {
            onVPEnded();
        }
    }

    public void onVPStarted() {
        log("onVPStarted");
        boolean isConnected = false;
        this.mDcTrackerBase.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(false);
        if (this.mDcTrackerBase.getOverallState() == DctConstants.State.CONNECTED) {
            isConnected = true;
        }
        if (isConnected && !this.mDcTrackerBase.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() && this.mInVoiceCall) {
            log("onVPStarted stop polling");
            this.mDcTrackerBase.stopNetStatPollHw();
            this.mDcTrackerBase.stopDataStallAlarmHw();
            this.mDcTrackerBase.notifyDataConnectionHw("vpStarted");
        }
    }

    public void onVPEnded() {
        log("onVPEnded");
        if (!this.mDcTrackerBase.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            boolean isConnected = true;
            this.mDcTrackerBase.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(true);
            if (this.mDcTrackerBase.getOverallState() != DctConstants.State.CONNECTED) {
                isConnected = false;
            }
            if (isConnected && this.mInVoiceCall) {
                this.mDcTrackerBase.startNetStatPollHw();
                this.mDcTrackerBase.startDataStallAlarmHw(false);
                if (!this.mDcTrackerBase.isDataEnabled()) {
                    this.mDcTrackerBase.cleanUpAllConnections("dataDisabled");
                } else {
                    this.mDcTrackerBase.notifyDataConnectionHw("vpEnded");
                }
            }
        }
    }

    public ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String requestedApnType, int radioTech) {
        ArrayList<ApnSetting> apnList = new ArrayList<>();
        ArrayList<ApnSetting> apnSettings = this.mDcTrackerBase.getAllApnList();
        if (apnSettings != null && !apnSettings.isEmpty()) {
            int apnSize = this.mDcTrackerBase.getAllApnList().size();
            for (int i = 0; i < apnSize; i++) {
                ApnSetting apn = (ApnSetting) this.mDcTrackerBase.getAllApnList().get(i);
                if (apn.canHandleType(requestedApnType) && ((!isLTENetwork() && ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech)) || (isLTENetwork() && (apn.bearer == 13 || apn.bearer == 14)))) {
                    if ((!TelephonyManager.getDefault().isNetworkRoaming(this.mDcTrackerBase.mPhone.getSubId()) || !"ctnet".equals(apn.apn)) && (TelephonyManager.getDefault().isNetworkRoaming(this.mDcTrackerBase.mPhone.getSubId()) || !"ctwap".equals(apn.apn))) {
                        log("buildWaitingApns: ct supl featrue endabled, APN not match");
                    } else {
                        log("buildWaitingApns: adding apn=" + apn);
                        apnList.add(apn);
                    }
                }
            }
        }
        return apnList;
    }

    public void onRatChange() {
        boolean isSetupDataNeeded;
        if (isCTSimCard(this.mDcTrackerBase.mPhone.getPhoneId())) {
            int dataRadioTech = this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
            boolean RatChange = ServiceState.isHrpd1X(dataRadioTech) != ServiceState.isHrpd1X(this.preDataRadioTech);
            boolean SetupRatChange = ServiceState.isHrpd1X(dataRadioTech) != ServiceState.isHrpd1X(this.mDcTrackerBase.preSetupBasedRadioTech);
            DctConstants.State overallState = this.mDcTrackerBase.getOverallState();
            boolean isConnected = overallState == DctConstants.State.CONNECTED || overallState == DctConstants.State.CONNECTING;
            log("onRatChange: preDataRadioTech is: " + this.preDataRadioTech + "; dataRadioTech is: " + dataRadioTech);
            log("onRatChange: preSetupBasedRadioTech is: " + this.mDcTrackerBase.preSetupBasedRadioTech + "; overallState is: " + overallState);
            if (dataRadioTech != 0) {
                if (this.preDataRadioTech != -1 && RatChange) {
                    if (this.mDcTrackerBase.preSetupBasedRadioTech == 0 || SetupRatChange) {
                        if (isConnected) {
                            this.mDcTrackerBase.cleanUpAllConnectionsHw(true, "nwTypeChanged");
                        } else {
                            this.mDcTrackerBase.cleanUpAllConnectionsHw(false, "nwTypeChanged");
                            updateApnContextState();
                        }
                        this.mDcTrackerBase.setupDataOnConnectableApnsHw("nwTypeChanged");
                    } else {
                        log("setup data call has been trigger by other flow, have no need to execute again.");
                    }
                }
                this.preDataRadioTech = dataRadioTech;
            }
        } else if (!isCTSimCard(this.mDcTrackerBase.mPhone.getPhoneId())) {
            for (ApnContext apnContext : this.mDcTrackerBase.getMApnContextsHw().values()) {
                if ((this.mDcTrackerBase.isPermanentFailure(apnContext.getPdpFailCause()) || DcFailCause.NOT_ALLOWED_RADIO_TECHNOLOGY_IWLAN == apnContext.getPdpFailCause()) && (apnContext.getState() == DctConstants.State.FAILED || apnContext.getState() == DctConstants.State.IDLE)) {
                    isSetupDataNeeded = true;
                    continue;
                } else {
                    isSetupDataNeeded = false;
                    continue;
                }
                if (isSetupDataNeeded) {
                    log("tryRestartDataConnections, which reason is " + apnContext.getPdpFailCause());
                    apnContext.setPdpFailCause(DcFailCause.NONE);
                    this.mDcTrackerBase.onTrySetupDataHw("nwTypeChanged");
                    return;
                }
            }
        }
    }

    public boolean isSupportLTE(ApnSetting apnSettings) {
        if (((apnSettings.bearer == 13 || apnSettings.bearer == 14) && isApnPreset(apnSettings)) || !isApnPreset(apnSettings)) {
            return true;
        }
        return false;
    }

    public void clearAndResumeNetInfoForWifiLteCoexist(int apnId, int enabled, ApnContext apnContext) {
        if (ENABLE_WIFI_LTE_CE) {
            String apnType = apnContext.getApnType();
            if (apnType.equals("default") || "hipri".equals(apnType)) {
                log("enableApnType but already actived");
                if (enabled != 1) {
                    ConnectivityManager from = ConnectivityManager.from(this.mDcTrackerBase.mPhone.getContext());
                    if (!apnContext.isDisconnected() && isWifiConnected()) {
                        log("clearAndResumeNetInfiForWifiLteCoexist:disableApnType due to WIFI Connected");
                        this.mDcTrackerBase.stopNetStatPollHw();
                        this.mDcTrackerBase.stopDataStallAlarmHw();
                        apnContext.setEnabled(false);
                        this.mDcTrackerBase.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
                    }
                } else if (apnContext.getState() == DctConstants.State.CONNECTED) {
                    log("enableApnType: return APN_ALREADY_ACTIVE");
                    apnContext.setEnabled(true);
                    this.mDcTrackerBase.startNetStatPollHw();
                    this.mDcTrackerBase.restartDataStallAlarmHw();
                    this.mDcTrackerBase.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
                }
            }
        }
    }

    public void updateApnContextState() {
        for (ApnContext apnContext : this.mDcTrackerBase.getMApnContextsHw().values()) {
            if (apnContext.getState() == DctConstants.State.SCANNING) {
                apnContext.setState(DctConstants.State.IDLE);
                apnContext.setDataConnectionAc(null);
                this.mDcTrackerBase.cancelReconnectAlarmHw(apnContext);
            }
        }
    }

    public UiccCardApplication getUiccCardApplication(int appFamily) {
        if (this.mDcTrackerBase.mPhone == null) {
            return null;
        }
        if (VSimUtilsInner.isVSimSub(this.mDcTrackerBase.mPhone.getPhoneId())) {
            return VSimUtilsInner.getVSimUiccCardApplication(appFamily);
        }
        return this.mUiccController.getUiccCardApplication(this.mDcTrackerBase.mPhone.getPhoneId(), appFamily);
    }

    public void updateForVSim() {
        log("vsim update sub = " + this.mDcTrackerBase.mPhone.getSubId());
        this.mDcTrackerBase.unregisterForAllEventsHw();
        this.mDcTrackerBase.registerForAllEventsHw();
        this.mDcTrackerBase.updateRecords();
        this.mDcTrackerBase.setUserDataEnabled(true);
    }

    public String getAppName(int pid) {
        String processName = "";
        List<ActivityManager.RunningAppProcessInfo> l = ((ActivityManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("activity")).getRunningAppProcesses();
        if (l == null) {
            return processName;
        }
        Iterator<ActivityManager.RunningAppProcessInfo> it = l.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo info = it.next();
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        return processName;
    }

    public void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
    }

    public boolean isDisconnectedOrConnecting() {
        for (ApnContext apnContext : this.mDcTrackerBase.getMApnContextsHw().values()) {
            if (apnContext.getState() != DctConstants.State.CONNECTED) {
                if (apnContext.getState() == DctConstants.State.DISCONNECTING) {
                }
            }
            return false;
        }
        return true;
    }

    public void setupDataForSinglePdnArbitration(String reason) {
        log("setupDataForSinglePdn: reason = " + reason + " isDisconnected = " + this.mDcTrackerBase.isDisconnected());
        if (this.mDcTrackerBase.isOnlySingleDcAllowedHw(this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology()) && this.mDcTrackerBase.isDisconnected() && !"SinglePdnArbitration".equals(reason)) {
            this.mDcTrackerBase.setupDataOnConnectableApnsHw("SinglePdnArbitration");
        }
    }

    public boolean isNeedFilterVowifiMms(ApnSetting apn, String requestedApnType) {
        boolean isMmsRequested = "mms".equals(requestedApnType);
        boolean hasVowifiMmsType = apn != null && ArrayUtils.contains(apn.types, APN_TYPE_VOWIFIMMS);
        if (!isMmsRequested || !hasVowifiMmsType || !HuaweiTelephonyConfigs.isHisiPlatform()) {
            return false;
        }
        return true;
    }

    public boolean isBlockSetInitialAttachApn() {
        String plmnsConfig = Settings.System.getString(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "apn_reminder_plmn");
        IccRecords r = this.mDcTrackerBase.getIccRecordsHw();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(operator)) {
            return false;
        }
        return plmnsConfig.contains(operator);
    }

    public boolean isNeedForceSetup(ApnContext apnContext) {
        return apnContext != null && "dataEnabled".equals(apnContext.getReason()) && this.mDcTrackerBase.mPhone.getServiceState().getVoiceRegState() == 0 && USER_FORCE_DATA_SETUP;
    }

    public boolean isDataDisableBySim2() {
        if (this.mDcTrackerBase.mHwCustDcTracker != null) {
            return this.mDcTrackerBase.mHwCustDcTracker.isDataDisableBySim2();
        }
        log("isDataDisableBySim2: Maybe Exception occurs, mHwCustDcTracker is null");
        return false;
    }

    public boolean getCustRetryConfig() {
        int subId = this.mDcTrackerBase.mPhone.getSubId();
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("cust_retry_config", subId, Boolean.class);
        boolean valueFromProp = CUST_RETRY_CONFIG;
        log("getCustRetryConfig, subId:" + subId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    public boolean getEsmFlagAdaptionEnabled() {
        int subId = this.mDcTrackerBase.mPhone.getSubId();
        Boolean esmFlagAdaptionEnabled = (Boolean) HwCfgFilePolicy.getValue("attach_apn_enabled", subId, Boolean.class);
        log("getEsmFlagAdaptionEnabled, subId:" + subId + ", card:" + esmFlagAdaptionEnabled);
        return esmFlagAdaptionEnabled != null ? esmFlagAdaptionEnabled.booleanValue() : ESM_FLAG_ADAPTION_ENABLED;
    }

    public int getEsmFlagFromCard() {
        int subId = this.mDcTrackerBase.mPhone.getSubId();
        Integer esmFlagFromCard = (Integer) HwCfgFilePolicy.getValue("plmn_esm_flag", subId, Integer.class);
        log("getEsmFlagFromCard, subId:" + subId + ", card:" + esmFlagFromCard);
        if (esmFlagFromCard != null) {
            return esmFlagFromCard.intValue();
        }
        return -1;
    }

    public String getOpKeyByActivedApn(String activedNumeric, String activedApn, String activedUser) {
        if (this.mDcTrackerBase.mHwCustDcTracker != null) {
            return this.mDcTrackerBase.mHwCustDcTracker.getOpKeyByActivedApn(activedNumeric, activedApn, activedUser);
        }
        log("getOpKeyByActivedApn: Maybe Exception occurs, mHwCustDcTracker is null");
        return null;
    }

    public void setApnOpkeyToSettingsDB(String activedApnOpkey) {
        if (this.mDcTrackerBase.mHwCustDcTracker == null) {
            log("setApnOpkeyToSettingsDB: Maybe Exception occurs, mHwCustDcTracker is null");
        } else {
            this.mDcTrackerBase.mHwCustDcTracker.setApnOpkeyToSettingsDB(activedApnOpkey);
        }
    }

    public void disposeCustDct() {
        if (this.mDcTrackerBase.mHwCustDcTracker == null) {
            log("dispose: Maybe Exception occurs, mHwCustDcTracker is null");
        } else {
            this.mDcTrackerBase.mHwCustDcTracker.dispose();
        }
    }

    public void clearDefaultLink() {
        if (this.mDcTrackerBase.getApnContextsHw() != null) {
            ApnContext apnContext = (ApnContext) this.mDcTrackerBase.getApnContextsHw().get(0);
            if (apnContext != null) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac != null) {
                    dcac.clearLink(null, null, null);
                }
            }
        }
    }

    public void resumeDefaultLink() {
        if (this.mDcTrackerBase.getApnContextsHw() != null) {
            ApnContext apnContext = (ApnContext) this.mDcTrackerBase.getApnContextsHw().get(0);
            if (apnContext != null) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac != null) {
                    dcac.resumeLink(null, null, null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserSelectOpenService() {
        log("onUserSelectOpenService set apn = bip0");
        ArrayList<ApnSetting> allApnSettings = this.mDcTrackerBase.getAllApnList();
        if (allApnSettings != null && allApnSettings.isEmpty()) {
            this.mDcTrackerBase.createAllApnListHw();
        }
        if (this.mDcTrackerBase.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoaded: notifying data availability");
            this.mDcTrackerBase.notifyOffApnsOfAvailabilityHw("simLoadedandpseudoimsi");
        }
        this.mDcTrackerBase.setupDataOnConnectableApnsHw("SetPSOnlyOK");
    }

    public String getOperatorNumeric() {
        IccRecords r = this.mDcTrackerBase.getIccRecordsHw();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (operator == null) {
            operator = "";
        }
        log("getOperatorNumberic - returning from card: " + operator);
        return operator;
    }

    public String getCTOperator(String operator) {
        if (isCTSimCard(this.mDcTrackerBase.mPhone.getPhoneId())) {
            operator = SystemProperties.get("gsm.national_roaming.apn", CT_CDMA_OPERATOR);
            log("Select china telecom hplmn: " + operator);
        }
        IccRecords record = this.mDcTrackerBase.getIccRecordsHw();
        String preSpn = record != null ? record.getServiceProviderName() : "";
        String preIccid = SystemProperties.get("gsm.sim.preiccid_" + this.mDcTrackerBase.mPhone.getPhoneId(), "");
        if (!CT_CDMA_OPERATOR.equals(operator)) {
            return operator;
        }
        if (!GC_ICCID.equals(preIccid) && !GC_SPN.equals(preSpn)) {
            return operator;
        }
        log("Hongkong GC card and iccid is: " + preIccid + ",spn is: " + preSpn);
        return GC_MCCMNC;
    }
}
