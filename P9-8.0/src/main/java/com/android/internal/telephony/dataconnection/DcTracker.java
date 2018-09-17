package com.android.internal.telephony.dataconnection;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.radio.V1_0.RadioError;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RegistrantList;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PcoData;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwDataConnectionManager;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.google.android.mms.pdu.CharacterSets;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DcTracker extends AbstractDcTrackerBase {
    private static final /* synthetic */ int[] -com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
    protected static final int ACTIVE_PDP_FAIL_TO_RESTART_RILD_COUNT = 3;
    protected static final long ACTIVE_PDP_FAIL_TO_RESTART_RILD_MILLIS = 600000;
    static final String APN_ID = "apn_id";
    public static final String APN_TYPE_VOWIFIMMS = "vowifi_mms";
    private static final int CDMA_NOT_ROAMING = 0;
    private static final int CDMA_ROAMING = 1;
    private static final String CT_LTE_APN_PREFIX = SystemProperties.get("ro.config.ct_lte_apn", "ctnet");
    private static final String CT_NOT_ROAMING_APN_PREFIX = SystemProperties.get("ro.config.ct_not_roaming_apn", "ctnet");
    private static final String CT_ROAMING_APN_PREFIX = SystemProperties.get("ro.config.ct_roaming_apn", "ctnet");
    public static final boolean CT_SUPL_FEATURE_ENABLE = SystemProperties.getBoolean("ro.hwpp.ct_supl_feature_enable", false);
    public static final boolean CUST_RETRY_CONFIG = SystemProperties.getBoolean("ro.config.cust_retry_config", false);
    private static final int DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 60000;
    private static int DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 600000;
    private static final String DATA_STALL_ALARM_TAG_EXTRA = "data.stall.alram.tag";
    private static final boolean DATA_STALL_NOT_SUSPECTED = false;
    private static final boolean DATA_STALL_SUSPECTED = true;
    private static final boolean DBG = true;
    private static final String DEBUG_PROV_APN_ALARM = "persist.debug.prov_apn_alarm";
    private static final String GC_ICCID = "8985231";
    private static final String GC_MCCMNC = "45431";
    private static final String GC_SPN = "CTExcel";
    private static final int GSM_ROAMING_CARD1 = 2;
    private static final int GSM_ROAMING_CARD2 = 3;
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final String INTENT_DATA_STALL_ALARM = "com.android.internal.telephony.data-stall";
    protected static final String INTENT_PDP_RESET_ALARM = "com.android.internal.telephony.pdp-reset";
    private static final String INTENT_PROVISIONING_APN_ALARM = "com.android.internal.telephony.provisioning_apn_alarm";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.data-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reconnect_alarm_extra_reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "reconnect_alarm_extra_type";
    private static final int INVALID_STEP = -99;
    public static final boolean IS_DELAY_ATTACH_ENABLED = SystemProperties.getBoolean("ro.config.delay_attach_enabled", false);
    private static String LOG_TAG = "DCT";
    private static final int LTE_NOT_ROAMING = 4;
    static final Uri MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
    private static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    protected static final int PDP_RESET_ALARM_DELAY_IN_MS = 300000;
    protected static final String PDP_RESET_ALARM_TAG_EXTRA = "pdp.reset.alram.tag";
    private static final int POLL_NETSTAT_MILLIS = 1000;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 600000;
    private static final int POLL_PDP_MILLIS = 5000;
    static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = Uri.parse("content://telephony/carriers/preferapn_no_update/subId/");
    private static final String PREFERRED_APN_ID = "preferredApnIdEx";
    private static final int PREF_APN_ID_LEN = 5;
    private static final int PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT = 900000;
    private static final String PROVISIONING_APN_ALARM_TAG_EXTRA = "provisioning.apn.alarm.tag";
    private static final int PROVISIONING_SPINNER_TIMEOUT_MILLIS = 120000;
    private static final String PUPPET_MASTER_RADIO_STRESS_TEST = "gsm.defaultpdpcontext.active";
    private static final boolean RADIO_TESTS = false;
    private static final int RECONNECT_ALARM_DELAY_TIME_FOR_CS_ATTACHED = 5000;
    private static final int RECONNECT_ALARM_DELAY_TIME_FOR_LOST_CONNECTION = 50;
    private static final int SUB_1 = 1;
    private static final boolean VDBG = true;
    private static final boolean VDBG_STALL = false;
    public static final int VP_END = 0;
    public static final int VP_START = 1;
    private static final String WAP_APN = "3gwap";
    protected static final HashMap<String, Integer> mIfacePhoneHashMap = new HashMap();
    protected static final boolean mWcdmaVpEnabled = SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true");
    private static int sEnableFailFastRefCounter = 0;
    private String RADIO_RESET_PROPERTY;
    public AtomicBoolean isCleanupRequired;
    protected boolean isMultiSimEnabled;
    private Activity mActivity;
    private final AlarmManager mAlarmManager;
    private ArrayList<ApnSetting> mAllApnSettings;
    private RegistrantList mAllDataDisconnectedRegistrants;
    private boolean mAllowUserEditTetherApn;
    public final ConcurrentHashMap<String, ApnContext> mApnContexts;
    private final SparseArray<ApnContext> mApnContextsById;
    private ApnChangeObserver mApnObserver;
    private HashMap<String, Integer> mApnToDataConnectionId;
    private AtomicBoolean mAttached;
    private AtomicBoolean mAutoAttachOnCreation;
    private boolean mAutoAttachOnCreationConfig;
    private boolean mCanSetPreferApn;
    private boolean mCdmaPsRecoveryEnabled;
    private final ConnectivityManager mCm;
    private int mCurrentState;
    private HashMap<Integer, DcAsyncChannel> mDataConnectionAcHashMap;
    private final Handler mDataConnectionTracker;
    private HashMap<Integer, DataConnection> mDataConnections;
    private final DataEnabledSettings mDataEnabledSettings;
    private PendingIntent mDataStallAlarmIntent;
    private int mDataStallAlarmTag;
    private volatile boolean mDataStallDetectionEnabled;
    private TxRxSum mDataStallTxRxSum;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcController mDcc;
    private String mDefaultApnId;
    private ArrayList<Message> mDisconnectAllCompleteMsgList;
    private int mDisconnectPendingCount;
    private ApnSetting mEmergencyApn;
    private boolean mEmergencyApnLoaded;
    private volatile boolean mFailFast;
    protected long mFirstPdpActFailTimestamp;
    protected HwCustDcTracker mHwCustDcTracker;
    private final AtomicReference<IccRecords> mIccRecords;
    public boolean mImsRegistrationState;
    private boolean mInVoiceCall;
    private final BroadcastReceiver mIntentReceiver;
    protected boolean mIsBtConnected;
    private boolean mIsDisposed;
    private boolean mIsProvisioning;
    private boolean mIsPsRestricted;
    private boolean mIsScreenOn;
    private boolean mIsWifiConnected;
    private boolean mMvnoMatched;
    private boolean mNetStatPollEnabled;
    private int mNetStatPollPeriod;
    private int mNoRecvPollCount;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    protected int mPdpActFailCount;
    protected PendingIntent mPdpResetAlarmIntent;
    protected int mPdpResetAlarmTag;
    protected final Phone mPhone;
    PhoneStateListener mPhoneStateListener;
    private final Runnable mPollNetStat;
    private ApnSetting mPreferredApn;
    protected final ArrayList<ApnContext> mPrioritySortedApnContexts;
    private final String mProvisionActionName;
    private BroadcastReceiver mProvisionBroadcastReceiver;
    private PendingIntent mProvisioningApnAlarmIntent;
    private int mProvisioningApnAlarmTag;
    private ProgressDialog mProvisioningSpinner;
    private String mProvisioningUrl;
    private PendingIntent mReconnectIntent;
    private AsyncChannel mReplyAc;
    private String mRequestedApnType;
    private boolean mReregisterOnReconnectFailure;
    private ContentResolver mResolver;
    protected boolean mRestartRildEnabled;
    private long mRxPkts;
    private long mSentSinceLastRecv;
    private final SettingsObserver mSettingsObserver;
    private State mState;
    private SubscriptionManager mSubscriptionManager;
    private long mTxPkts;
    protected UiccCardApplication mUiccApplcation;
    private final UiccController mUiccController;
    private AtomicInteger mUniqueIdGenerator;
    public int mVpStatus;
    private int oldCallState;
    private int preDataRadioTech;
    private int preSetupBasedRadioTech;

    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(DcTracker.this.mDataConnectionTracker);
        }

        public void onChange(boolean selfChange) {
            DcTracker.this.sendMessage(DcTracker.this.obtainMessage(270355));
        }
    }

    public static class DataAllowFailReason {
        private HashSet<DataAllowFailReasonType> mDataAllowFailReasonSet = new HashSet();

        public void addDataAllowFailReason(DataAllowFailReasonType type) {
            this.mDataAllowFailReasonSet.add(type);
        }

        public String getDataAllowFailReason() {
            StringBuilder failureReason = new StringBuilder();
            failureReason.append("isDataAllowed: No");
            for (DataAllowFailReasonType reason : this.mDataAllowFailReasonSet) {
                failureReason.append(reason.mFailReasonStr);
            }
            return failureReason.toString();
        }

        public boolean isFailForSingleReason(DataAllowFailReasonType failReasonType) {
            if (this.mDataAllowFailReasonSet.size() == 1) {
                return this.mDataAllowFailReasonSet.contains(failReasonType);
            }
            return false;
        }

        public void clearAllReasons() {
            this.mDataAllowFailReasonSet.clear();
        }

        public boolean isFailed() {
            return this.mDataAllowFailReasonSet.size() > 0;
        }
    }

    public enum DataAllowFailReasonType {
        NOT_ATTACHED(" - Not attached"),
        RECORD_NOT_LOADED(" - SIM not loaded"),
        ROAMING_DISABLED(" - Roaming and data roaming not enabled"),
        INVALID_PHONE_STATE(" - PhoneState is not idle"),
        CONCURRENT_VOICE_DATA_NOT_ALLOWED(" - Concurrent voice and data not allowed"),
        PS_RESTRICTED(" - mIsPsRestricted= true"),
        UNDESIRED_POWER_STATE(" - desiredPowerState= false"),
        INTERNAL_DATA_DISABLED(" - mInternalDataEnabled= false"),
        DEFAULT_DATA_UNSELECTED(" - defaultDataSelected= false"),
        RADIO_DISABLED_BY_CARRIER(" - powerStateFromCarrier= false"),
        PS_RESTRICTED_BY_FDN(" - ps not allowed by fdn");
        
        public String mFailReasonStr;

        private DataAllowFailReasonType(String reason) {
            this.mFailReasonStr = reason;
        }
    }

    private class ProvisionNotificationBroadcastReceiver extends BroadcastReceiver {
        private final String mNetworkOperator;
        private final String mProvisionUrl;

        public ProvisionNotificationBroadcastReceiver(String provisionUrl, String networkOperator) {
            this.mNetworkOperator = networkOperator;
            this.mProvisionUrl = provisionUrl;
        }

        private void setEnableFailFastMobileData(int enabled) {
            DcTracker.this.sendMessage(DcTracker.this.obtainMessage(270372, enabled, 0));
        }

        private void enableMobileProvisioning() {
            Message msg = DcTracker.this.obtainMessage(270373);
            msg.setData(Bundle.forPair("provisioningUrl", this.mProvisionUrl));
            DcTracker.this.sendMessage(msg);
        }

        public void onReceive(Context context, Intent intent) {
            DcTracker.this.log("onReceive : ProvisionNotificationBroadcastReceiver");
            DcTracker.this.mProvisioningSpinner = new ProgressDialog(context);
            DcTracker.this.mProvisioningSpinner.setTitle(this.mNetworkOperator);
            DcTracker.this.mProvisioningSpinner.setMessage(context.getText(17040345));
            DcTracker.this.mProvisioningSpinner.setIndeterminate(true);
            DcTracker.this.mProvisioningSpinner.setCancelable(true);
            DcTracker.this.mProvisioningSpinner.getWindow().setType(2009);
            DcTracker.this.mProvisioningSpinner.show();
            DcTracker.this.sendMessageDelayed(DcTracker.this.obtainMessage(270378, DcTracker.this.mProvisioningSpinner), 120000);
            DcTracker.this.setRadio(true);
            setEnableFailFastMobileData(1);
            enableMobileProvisioning();
        }
    }

    private static class RecoveryAction {
        public static final int CLEANUP = 1;
        public static final int GET_DATA_CALL_LIST = 0;
        public static final int RADIO_RESTART = 3;
        public static final int RADIO_RESTART_WITH_PROP = 4;
        public static final int REREGISTER = 2;

        private RecoveryAction() {
        }

        private static boolean isAggressiveRecovery(int value) {
            if (value == 1 || value == 2 || value == 3 || value == 4) {
                return true;
            }
            return false;
        }
    }

    private enum RetryFailures {
        ALWAYS,
        ONLY_ON_CHANGE
    }

    private class SettingsObserver extends ContentObserver {
        private static final String TAG = "DcTracker.SettingsObserver";
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        void unobserve() {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            Rlog.e(TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = (Integer) this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
            } else {
                Rlog.e(TAG, "No matching event to send for URI=" + uri);
            }
            DcTracker.this.sendRoamingDataStatusChangBroadcast();
        }
    }

    public static class TxRxSum {
        public long rxPkts;
        public long txPkts;

        public TxRxSum() {
            reset();
        }

        public TxRxSum(long txPkts, long rxPkts) {
            this.txPkts = txPkts;
            this.rxPkts = rxPkts;
        }

        public TxRxSum(TxRxSum sum) {
            this.txPkts = sum.txPkts;
            this.rxPkts = sum.rxPkts;
        }

        public void reset() {
            this.txPkts = -1;
            this.rxPkts = -1;
        }

        public String toString() {
            return "{txSum=" + this.txPkts + " rxSum=" + this.rxPkts + "}";
        }

        public void updateTxRxSum() {
            this.txPkts = TrafficStats.getMobileTcpTxPackets();
            this.rxPkts = TrafficStats.getMobileTcpRxPackets();
        }

        public void updateThisModemMobileTxRxSum(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
            this.txPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileTxPackets(ifacePhoneHashMap, phoneId);
            this.rxPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileRxPackets(ifacePhoneHashMap, phoneId);
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-DctConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-DctConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-DctConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.FAILED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.RETRYING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.SCANNING.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-internal-telephony-DctConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    private void registerSettingsObserver() {
        Uri contentUri;
        this.mSettingsObserver.unobserve();
        if (TelephonyManager.getDefault().getSimCount() == 1) {
            contentUri = Global.getUriFor("data_roaming");
        } else {
            contentUri = Global.getUriFor(getDataRoamingSettingItem("data_roaming"));
        }
        this.mSettingsObserver.observe(contentUri, 270347);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioned"), 270379);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioning_mobile_data"), 270379);
    }

    private void onActionIntentReconnectAlarm(Intent intent) {
        String reason = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON);
        String apnType = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = intent.getIntExtra("subscription", -1);
        log("onActionIntentReconnectAlarm: currSubId = " + currSubId + " phoneSubId=" + phoneSubId);
        if (SubscriptionManager.isValidSubscriptionId(currSubId) && currSubId == phoneSubId) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            log("onActionIntentReconnectAlarm: mState=" + this.mState + " reason=" + reason + " apnType=" + apnType + " apnContext=" + apnContext + " mDataConnectionAsyncChannels=" + this.mDataConnectionAcHashMap);
            if (apnContext != null && apnContext.isEnabled()) {
                apnContext.setReason(reason);
                State apnContextState = apnContext.getState();
                log("onActionIntentReconnectAlarm: apnContext state=" + apnContextState);
                if (apnContextState == State.FAILED || apnContextState == State.IDLE) {
                    log("onActionIntentReconnectAlarm: state is FAILED|IDLE, disassociate");
                    DcAsyncChannel dcac = apnContext.getDcAc();
                    if (dcac != null) {
                        log("onActionIntentReconnectAlarm: tearDown apnContext=" + apnContext);
                        dcac.tearDown(apnContext, "", null);
                    }
                    apnContext.setDataConnectionAc(null);
                    apnContext.setState(State.IDLE);
                } else {
                    log("onActionIntentReconnectAlarm: keep associated");
                }
                sendMessage(obtainMessage(270339, apnContext));
                apnContext.setReconnectIntent(null);
            }
            return;
        }
        log("receive ReconnectAlarm but subId incorrect, ignore");
    }

    private void onActionIntentDataStallAlarm(Intent intent) {
        Message msg = obtainMessage(270353, intent.getAction());
        msg.arg1 = intent.getIntExtra(DATA_STALL_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    public DcTracker(Phone phone) {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.oldCallState = 0;
        this.mRequestedApnType = "default";
        this.mDataEnabledSettings = new DataEnabledSettings();
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mPrioritySortedApnContexts = new ArrayList();
        this.mAllApnSettings = new ArrayList();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = false;
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    DcTracker.this.log("screen on");
                    DcTracker.this.mIsScreenOn = true;
                    DcTracker.this.stopNetStatPoll();
                    DcTracker.this.startNetStatPoll();
                    DcTracker.this.restartDataStallAlarm();
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    DcTracker.this.log("screen off");
                    DcTracker.this.mIsScreenOn = false;
                    DcTracker.this.stopNetStatPoll();
                    DcTracker.this.startNetStatPoll();
                    DcTracker.this.restartDataStallAlarm();
                } else if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                    DcTracker.this.log("Reconnect alarm. Previous state was " + DcTracker.this.mState);
                    DcTracker.this.onActionIntentReconnectAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_DATA_STALL_ALARM)) {
                    DcTracker.this.log("Data stall alarm");
                    DcTracker.this.onActionIntentDataStallAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_PROVISIONING_APN_ALARM)) {
                    DcTracker.this.log("Provisioning apn alarm");
                    DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    DcTracker dcTracker = DcTracker.this;
                    if (networkInfo != null) {
                        z = networkInfo.isConnected();
                    }
                    dcTracker.mIsWifiConnected = z;
                    DcTracker.this.log("NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + DcTracker.this.mIsWifiConnected);
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    DcTracker.this.log("Wifi state changed");
                    boolean enabled = intent.getIntExtra("wifi_state", 4) == 3;
                    if (!enabled) {
                        DcTracker.this.mIsWifiConnected = false;
                    }
                    DcTracker.this.log("WIFI_STATE_CHANGED_ACTION: enabled=" + enabled + " mIsWifiConnected=" + DcTracker.this.mIsWifiConnected);
                } else if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    CarrierConfigManager configMgr = (CarrierConfigManager) DcTracker.this.mPhone.getContext().getSystemService("carrier_config");
                    if (configMgr != null) {
                        PersistableBundle cfg = configMgr.getConfigForSubId(DcTracker.this.mPhone.getSubId());
                        if (cfg != null) {
                            DcTracker.this.mAllowUserEditTetherApn = cfg.getBoolean("editable_tether_apn_bool");
                        }
                    }
                } else if (action.equals(DcTracker.INTENT_PDP_RESET_ALARM)) {
                    DcTracker.this.log("Pdp reset alarm");
                    DcTracker.this.onActionIntentPdpResetAlarm(intent);
                } else if (action.equals("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED")) {
                    DcTracker.this.mIsBtConnected = intent.getBooleanExtra("btt_connect_state", false);
                    DcTracker.this.log("Received bt_connect_state = " + DcTracker.this.mIsBtConnected);
                } else if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(action)) {
                    DcTracker.this.log("Received SWITCH_SLOT_DONE");
                    String operator = DcTracker.this.getOperatorNumeric();
                    int switchSlotStep = intent.getIntExtra(DcTracker.HW_SWITCH_SLOT_STEP, -99);
                    if (!TextUtils.isEmpty(operator) && 1 == switchSlotStep) {
                        DcTracker.this.onRecordsLoadedOrSubIdChanged();
                    }
                } else if (action.equals(AbstractPhoneInternalInterface.OTA_OPEN_CARD_ACTION)) {
                    DcTracker.this.log("onUserSelectOpenService ");
                    DcTracker.this.onUserSelectOpenService();
                } else if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                    DcTracker.this.log("com.huawei.devicepolicy.action.POLICY_CHANGED");
                    String action_tag = intent.getStringExtra("action_tag");
                    if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_data_4G") && DcTracker.this.mPhone != null && DcTracker.this.mPhone.getSubId() == 1) {
                        if (intent.getBooleanExtra("dataState", false)) {
                            DcTracker.this.cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
                        } else {
                            DcTracker.this.onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                        }
                    }
                } else {
                    DcTracker.this.log("onReceive: Unknown action=" + action);
                }
            }
        };
        this.mPollNetStat = new Runnable() {
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_long_poll_interval_ms", 600000);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
                if (SubscriptionManager.isValidSubscriptionId(DcTracker.this.mPhone.getSubId())) {
                    DcTracker.this.registerSettingsObserver();
                }
            }
        };
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mIsBtConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = true;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = 0;
        this.mAllowUserEditTetherApn = false;
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mImsRegistrationState = false;
        this.mCdmaPsRecoveryEnabled = false;
        this.mDefaultApnId = "0,0,0,0,0";
        this.mCurrentState = -1;
        this.preDataRadioTech = -1;
        this.preSetupBasedRadioTech = -1;
        this.mVpStatus = 0;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mPdpActFailCount = 0;
        this.mFirstPdpActFailTimestamp = 0;
        this.mRestartRildEnabled = true;
        this.mPdpResetAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mPdpResetAlarmIntent = null;
        this.mUiccApplcation = null;
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (DcTracker.this.oldCallState == 2 && state == 0 && DcTracker.this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
                    DcTracker.this.onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                    if (DcTracker.this.mPrioritySortedApnContexts != null) {
                        int list_size = DcTracker.this.mPrioritySortedApnContexts.size();
                        for (int i = 0; i < list_size; i++) {
                            ApnContext apnContext = (ApnContext) DcTracker.this.mPrioritySortedApnContexts.get(i);
                            if (apnContext.getApnType().equals("default")) {
                                DcTracker.this.log("resetRetryCount");
                                apnContext.resetRetryCount();
                            }
                        }
                    }
                }
                DcTracker.this.oldCallState = state;
            }
        };
        this.mEmergencyApnLoaded = false;
        this.mPhone = phone;
        if (this.mHwCustDcTracker == null) {
            this.mHwCustDcTracker = (HwCustDcTracker) HwCustUtils.createObj(HwCustDcTracker.class, new Object[]{this});
        }
        if (phone.getPhoneType() == 1) {
            LOG_TAG = "GsmDCT";
        } else if (phone.getPhoneType() == 2) {
            LOG_TAG = "CdmaDCT";
        } else {
            LOG_TAG = "DCT";
            loge("unexpected phone type [" + phone.getPhoneType() + "]");
        }
        log(LOG_TAG + ".constructor");
        if (phone.getPhoneType() == 2 && SystemProperties.getBoolean("hw.dct.psrecovery", false)) {
            this.mCdmaPsRecoveryEnabled = true;
        } else {
            this.mCdmaPsRecoveryEnabled = false;
        }
        this.mResolver = this.mPhone.getContext().getContentResolver();
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 270369, null);
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            VSimUtilsInner.registerForIccChanged(this, 270369, null);
        }
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mCm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (this.mCm != null) {
            NetworkInfo mWifiNetworkInfo = this.mCm.getNetworkInfo(1);
            if (mWifiNetworkInfo != null && mWifiNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                this.mIsWifiConnected = true;
            }
        }
        log("In DcTracker constructor mIsWifiConnected is" + this.mIsWifiConnected);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction(INTENT_DATA_STALL_ALARM);
        filter.addAction(INTENT_PROVISIONING_APN_ALARM);
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction(INTENT_PDP_RESET_ALARM);
        filter.addAction("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED");
        filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false)) {
            filter.addAction(AbstractPhoneInternalInterface.OTA_OPEN_CARD_ACTION);
        }
        this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mAutoAttachOnCreation.set(PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getBoolean(Phone.DATA_DISABLED_ON_BOOT_KEY, false));
        registerPhoneStateListener(this.mPhone.getContext());
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        HandlerThread dcHandlerThread = new HandlerThread("DcHandlerThread");
        dcHandlerThread.start();
        Handler dcHandler = new Handler(dcHandlerThread.getLooper());
        this.mDcc = DcController.makeDcc(this.mPhone, this, dcHandler);
        this.mDcTesterFailBringUpAll = new DcTesterFailBringUpAll(this.mPhone, dcHandler);
        this.mDataConnectionTracker = this;
        registerForAllEvents();
        update();
        this.mApnObserver = new ApnChangeObserver();
        phone.getContext().getContentResolver().registerContentObserver(Carriers.CONTENT_URI, true, this.mApnObserver);
        initApnContexts();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            filter = new IntentFilter();
            filter.addAction("com.android.internal.telephony.data-reconnect." + apnContext.getApnType());
            this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        }
        this.mProvisionActionName = "com.android.internal.telephony.PROVISION" + phone.getPhoneId();
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this);
        registerSettingsObserver();
        super.init();
        if (isClearCodeEnabled()) {
            startListenCellLocationChange();
        }
        registerForFdn();
        sendMessage(obtainMessage(271137));
    }

    public DcTracker() {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.oldCallState = 0;
        this.mRequestedApnType = "default";
        this.mDataEnabledSettings = new DataEnabledSettings();
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mPrioritySortedApnContexts = new ArrayList();
        this.mAllApnSettings = new ArrayList();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mIntentReceiver = /* anonymous class already generated */;
        this.mPollNetStat = /* anonymous class already generated */;
        this.mOnSubscriptionsChangedListener = /* anonymous class already generated */;
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mIsBtConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = true;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = 0;
        this.mAllowUserEditTetherApn = false;
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mImsRegistrationState = false;
        this.mCdmaPsRecoveryEnabled = false;
        this.mDefaultApnId = "0,0,0,0,0";
        this.mCurrentState = -1;
        this.preDataRadioTech = -1;
        this.preSetupBasedRadioTech = -1;
        this.mVpStatus = 0;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mPdpActFailCount = 0;
        this.mFirstPdpActFailTimestamp = 0;
        this.mRestartRildEnabled = true;
        this.mPdpResetAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mPdpResetAlarmIntent = null;
        this.mUiccApplcation = null;
        this.mPhoneStateListener = /* anonymous class already generated */;
        this.mEmergencyApnLoaded = false;
        this.mAlarmManager = null;
        this.mCm = null;
        this.mPhone = null;
        this.mUiccController = null;
        this.mDataConnectionTracker = null;
        this.mProvisionActionName = null;
        this.mSettingsObserver = new SettingsObserver(null, this);
    }

    public void registerServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this, 270352, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this, 270345, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOn(this, 270347, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 270348, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedEnabled(this, 270358, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedDisabled(this, 270359, null);
        log("registerForDataRegStateOrRatChanged");
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 270377, null);
        if (mWcdmaVpEnabled) {
            this.mPhone.mCi.registerForReportVpStatus(this, 271140, null);
        }
    }

    public void unregisterServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedEnabled(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedDisabled(this);
        log("unregisterForDataRegStateOrRatChanged");
        this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this);
    }

    private void registerForAllEvents() {
        this.mPhone.mCi.registerForAvailable(this, 270337, null);
        this.mPhone.mCi.registerForOffOrNotAvailable(this, 270342, null);
        this.mPhone.mCi.registerForDataCallListChanged(this, 270340, null);
        if (this.mPhone.getCallTracker() != null) {
            this.mPhone.getCallTracker().registerForVoiceCallEnded(this, 270344, null);
            this.mPhone.getCallTracker().registerForVoiceCallStarted(this, 270343, null);
        }
        registerServiceStateTrackerEvents();
        this.mPhone.mCi.registerForPcoData(this, 270381, null);
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(0, this, 270382, null, false);
    }

    public void dispose() {
        log("DCT.dispose");
        if (this.mProvisionBroadcastReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
            this.mProvisionBroadcastReceiver = null;
        }
        if (this.mProvisioningSpinner != null) {
            this.mProvisioningSpinner.dismiss();
            this.mProvisioningSpinner = null;
        }
        cleanUpAllConnections(true, null);
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            dcac.disconnect();
        }
        this.mDataConnectionAcHashMap.clear();
        this.mIsDisposed = true;
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            VSimUtilsInner.unregisterForIccChanged(this);
        }
        this.mUiccController.unregisterForIccChanged(this);
        this.mSettingsObserver.unobserve();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mDcc.dispose();
        this.mDcTesterFailBringUpAll.dispose();
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mApnObserver);
        this.mApnContexts.clear();
        this.mApnContextsById.clear();
        this.mPrioritySortedApnContexts.clear();
        unregisterForAllEvents();
        if (isClearCodeEnabled()) {
            stopListenCellLocationChange();
        }
        unregisterForFdn();
        destroyDataConnections();
        super.dispose();
    }

    private void unregisterForAllEvents() {
        this.mPhone.mCi.unregisterForAvailable(this);
        this.mPhone.mCi.unregisterForOffOrNotAvailable(this);
        if (this.mUiccApplcation != null) {
            unregisterForGetAdDone(this.mUiccApplcation);
            this.mUiccApplcation = null;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            unregisterForRecordsLoaded(r);
            unregisterForImsiReady(r);
            unregisterForFdnRecordsLoaded(r);
            this.mIccRecords.set(null);
        }
        this.mPhone.mCi.unregisterForDataCallListChanged(this);
        if (this.mPhone.getCallTracker() != null) {
            this.mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
            this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        }
        unregisterServiceStateTrackerEvents();
        this.mPhone.mCi.unregisterForPcoData(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 0);
        if (mWcdmaVpEnabled) {
            this.mPhone.mCi.unregisterForReportVpStatus(this);
        }
    }

    private void onResetDone(AsyncResult ar) {
        log("EVENT_RESET_DONE");
        String str = null;
        if (ar.userObj instanceof String) {
            str = ar.userObj;
        }
        gotoIdleAndNotifyDataConnection(str);
    }

    protected void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
    }

    private String getAppName(int pid) {
        String processName = "";
        List<RunningAppProcessInfo> l = ((ActivityManager) this.mPhone.getContext().getSystemService("activity")).getRunningAppProcesses();
        if (l == null) {
            return processName;
        }
        for (RunningAppProcessInfo info : l) {
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                }
            } catch (RuntimeException e) {
                log("RuntimeException");
            } catch (Exception e2) {
                log("Get The appName is wrong");
            }
        }
        return processName;
    }

    public void setDataEnabled(boolean enable) {
        log("DcTrackerBase setDataEnabled=" + enable);
        int pid = Binder.getCallingPid();
        log("Get the caller pid and appName. pid is " + pid + ", " + "appName is " + getAppName(pid));
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString() + "\n");
        }
        log(sb.toString());
        Message msg = obtainMessage(270366);
        msg.arg1 = enable ? 1 : 0;
        log("setDataEnabled: sendMessage: enable=" + enable);
        sendMessage(msg);
    }

    private void onSetUserDataEnabled(boolean enabled) {
        int i = 1;
        synchronized (this.mDataEnabledSettings) {
            ContentResolver contentResolver;
            String str;
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                contentResolver = this.mResolver;
                str = "mobile_data";
                if (!enabled) {
                    i = 0;
                }
                Global.putInt(contentResolver, str, i);
            } else if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
                log("vsim does not save mobile data");
            } else {
                int phoneCount = TelephonyManager.getDefault().getPhoneCount();
                for (int i2 = 0; i2 < phoneCount; i2++) {
                    int i3;
                    ContentResolver contentResolver2 = this.mResolver;
                    String str2 = "mobile_data" + i2;
                    if (enabled) {
                        i3 = 1;
                    } else {
                        i3 = 0;
                    }
                    Global.putInt(contentResolver2, str2, i3);
                }
                contentResolver = this.mResolver;
                str = "mobile_data";
                if (!enabled) {
                    i = 0;
                }
                Global.putInt(contentResolver, str, i);
            }
            if (this.mDataEnabledSettings.isUserDataEnabled() != enabled) {
                this.mDataEnabledSettings.setUserDataEnabled(enabled);
                if (!getDataRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
                    if (enabled) {
                        notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
                    } else {
                        notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_DISABLED);
                    }
                }
                if (enabled) {
                    ApnContext apnContext = (ApnContext) this.mApnContexts.get("default");
                    if (!(apnContext == null || (apnContext.isEnabled() ^ 1) == 0 || (this.mIsWifiConnected ^ 1) == 0 || (this.mIsBtConnected ^ 1) == 0)) {
                        log("onSetUserDataEnabled default apn is disabled and wifi and bluetooth is disconnect, so we need try to restore apncontext");
                        apnContext.setEnabled(true);
                        apnContext.setDependencyMet(true);
                    }
                }
                if (enabled) {
                    reevaluateDataConnections();
                    onTrySetupData(AbstractPhoneInternalInterface.REASON_USER_DATA_ENABLED);
                } else {
                    onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                    clearRestartRildParam();
                }
            }
        }
    }

    private void reevaluateDataConnections() {
        if (this.mDataEnabledSettings.isDataEnabled()) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.isConnectedOrConnecting()) {
                    DcAsyncChannel dcac = apnContext.getDcAc();
                    if (dcac != null) {
                        NetworkCapabilities netCaps = dcac.getNetworkCapabilitiesSync();
                        if (netCaps != null && (netCaps.hasCapability(13) ^ 1) != 0 && (netCaps.hasCapability(11) ^ 1) != 0) {
                            log("Tearing down restricted metered net:" + apnContext);
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            cleanUpConnection(true, apnContext);
                        } else if (apnContext.getApnSetting().isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming()) && netCaps != null && netCaps.hasCapability(11)) {
                            log("Tearing down unmetered net:" + apnContext);
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            cleanUpConnection(true, apnContext);
                        }
                    }
                }
            }
        }
    }

    private void onDeviceProvisionedChange() {
        if (getDataEnabled()) {
            this.mDataEnabledSettings.setUserDataEnabled(true);
            reevaluateDataConnections();
            onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            return;
        }
        this.mDataEnabledSettings.setUserDataEnabled(false);
        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
    }

    public long getSubId() {
        return (long) this.mPhone.getSubId();
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    private void setActivity(Activity activity) {
        log("setActivity = " + activity);
        this.mActivity = activity;
        this.mPhone.notifyDataActivity();
    }

    public boolean isDisconnectedOrConnecting() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() != State.CONNECTED) {
                if (apnContext.getState() == State.DISCONNECTING) {
                }
            }
            return false;
        }
        return true;
    }

    public void requestNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.requestNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.requestNetwork(networkRequest, log);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.releaseNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.releaseNetwork(networkRequest, log);
        }
    }

    public void clearDefaultLink() {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(0);
        if (apnContext != null) {
            DcAsyncChannel dcac = apnContext.getDcAc();
            if (dcac != null) {
                dcac.clearLink(null, null, null);
            }
        }
    }

    public void resumeDefaultLink() {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(0);
        if (apnContext != null) {
            DcAsyncChannel dcac = apnContext.getDcAc();
            if (dcac != null) {
                dcac.resumeLink(null, null, null);
            }
        }
    }

    public boolean isApnSupported(String name) {
        if (name == null) {
            loge("isApnSupported: name=null");
            return false;
        } else if (((ApnContext) this.mApnContexts.get(name)) != null) {
            return true;
        } else {
            loge("Request for unsupported mobile name: " + name);
            return false;
        }
    }

    public int getApnPriority(String name) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(name);
        if (apnContext == null) {
            loge("Request for unsupported mobile name: " + name);
        }
        return apnContext.priority;
    }

    private void setRadio(boolean on) {
        try {
            Stub.asInterface(ServiceManager.checkService("phone")).setRadio(on);
        } catch (Exception e) {
        }
    }

    public boolean isDataPossible(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext == null) {
            return false;
        }
        int i = apnContext.isEnabled() ? apnContext.getState() == State.FAILED ? 1 : 0 : 0;
        boolean apnTypePossible = i ^ 1;
        boolean dataAllowed = (apnContext.getApnType().equals("emergency") || isDataAllowedByApnType(null, apnType)) ? true : isBipApnType(apnType);
        boolean possible = dataAllowed ? apnTypePossible : false;
        if ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            log("Default data call activation not possible in iwlan.");
            possible = false;
        }
        log(String.format("isDataPossible(%s): possible=%b isDataAllowed=%b apnTypePossible=%b apnContextisEnabled=%b apnContextState()=%s", new Object[]{apnType, Boolean.valueOf(possible), Boolean.valueOf(dataAllowed), Boolean.valueOf(apnTypePossible), Boolean.valueOf(apnContextIsEnabled), apnContextState}));
        return possible;
    }

    protected void finalize() {
        if (this.mPhone != null) {
            log("finalize");
        }
    }

    private ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new ApnContext(this.mPhone, type, LOG_TAG, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        this.mApnContextsById.put(ApnContext.apnIdForApnName(type), apnContext);
        this.mPrioritySortedApnContexts.add(0, apnContext);
        return apnContext;
    }

    private void initApnContexts() {
        log("initApnContexts: E");
        for (String networkConfigString : this.mPhone.getContext().getResources().getStringArray(17236057)) {
            NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
            if (!VSimUtilsInner.isVSimFiltrateApn(this.mPhone.getSubId(), networkConfig.type)) {
                String apnType = networkTypeToApnType(networkConfig.type);
                if (isApnTypeDisabled(apnType)) {
                    log("apn type " + apnType + " disabled!");
                } else {
                    ApnContext apnContext;
                    switch (networkConfig.type) {
                        case 0:
                            apnContext = addApnContext("default", networkConfig);
                            break;
                        case 2:
                            apnContext = addApnContext("mms", networkConfig);
                            break;
                        case 3:
                            apnContext = addApnContext("supl", networkConfig);
                            break;
                        case 4:
                            apnContext = addApnContext("dun", networkConfig);
                            break;
                        case 5:
                            apnContext = addApnContext("hipri", networkConfig);
                            break;
                        case 10:
                            apnContext = addApnContext("fota", networkConfig);
                            break;
                        case 11:
                            apnContext = addApnContext("ims", networkConfig);
                            break;
                        case 12:
                            apnContext = addApnContext("cbs", networkConfig);
                            break;
                        case 14:
                            apnContext = addApnContext("ia", networkConfig);
                            break;
                        case 15:
                            apnContext = addApnContext("emergency", networkConfig);
                            break;
                        case 38:
                            apnContext = addApnContext("bip0", networkConfig);
                            break;
                        case 39:
                            apnContext = addApnContext("bip1", networkConfig);
                            break;
                        case 40:
                            apnContext = addApnContext("bip2", networkConfig);
                            break;
                        case 41:
                            apnContext = addApnContext("bip3", networkConfig);
                            break;
                        case 42:
                            apnContext = addApnContext("bip4", networkConfig);
                            break;
                        case 43:
                            apnContext = addApnContext("bip5", networkConfig);
                            break;
                        case 44:
                            apnContext = addApnContext("bip6", networkConfig);
                            break;
                        case 45:
                            apnContext = addApnContext("xcap", networkConfig);
                            break;
                        case RadioError.NO_SMS_TO_ACK /*48*/:
                            apnContext = addApnContext("internaldefault", networkConfig);
                            break;
                        default:
                            log("initApnContexts: skipping unknown type=" + networkConfig.type);
                            continue;
                    }
                    log("initApnContexts: apnContext=" + apnContext);
                }
            }
        }
        log("initApnContexts: X mApnContexts=" + this.mApnContexts);
        Collections.sort(this.mPrioritySortedApnContexts, new Comparator<ApnContext>() {
            public int compare(ApnContext c1, ApnContext c2) {
                return c2.priority - c1.priority;
            }
        });
    }

    public LinkProperties getLinkProperties(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dcac = apnContext.getDcAc();
            if (dcac != null) {
                log("return link properites for " + apnType);
                return dcac.getLinkPropertiesSync();
            }
        }
        log("return new LinkProperties");
        return new LinkProperties();
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dataConnectionAc = apnContext.getDcAc();
            if (dataConnectionAc != null) {
                log("get active pdp is not null, return NetworkCapabilities for " + apnType);
                return dataConnectionAc.getNetworkCapabilitiesSync();
            }
        }
        log("return new NetworkCapabilities");
        return new NetworkCapabilities();
    }

    public String[] getActiveApnTypes() {
        log("get all active apn types");
        ArrayList<String> result = new ArrayList();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    public String getActiveApnString(String apnType) {
        log("get active apn string for type:" + apnType);
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null) {
                return apnSetting.apn;
            }
        }
        return null;
    }

    public State getState(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getState();
        }
        return State.FAILED;
    }

    private boolean isProvisioningApn(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.isProvisioningApn();
        }
        return false;
    }

    public State getOverallState() {
        boolean isConnecting = false;
        boolean isFailed = true;
        boolean isAnyEnabled = false;
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.isEnabled()) {
                isAnyEnabled = true;
                switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[apnContext.getState().ordinal()]) {
                    case 1:
                    case 3:
                        log("overall state is CONNECTED");
                        return State.CONNECTED;
                    case 2:
                    case 6:
                        isConnecting = true;
                        isFailed = false;
                        break;
                    case 5:
                    case 7:
                        isFailed = false;
                        break;
                    default:
                        isAnyEnabled = true;
                        break;
                }
            }
        }
        if (!isAnyEnabled) {
            log("overall state is IDLE");
            return State.IDLE;
        } else if (isConnecting) {
            log("overall state is CONNECTING");
            return State.CONNECTING;
        } else if (isFailed) {
            log("overall state is FAILED");
            return State.FAILED;
        } else {
            log("overall state is IDLE");
            return State.IDLE;
        }
    }

    public boolean getAnyDataEnabled() {
        if (!this.mDataEnabledSettings.isDataEnabled()) {
            return false;
        }
        DataAllowFailReason failureReason = new DataAllowFailReason();
        if (isDataAllowed(failureReason)) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (isDataAllowedForApn(apnContext)) {
                    return true;
                }
            }
            return false;
        }
        log(failureReason.getDataAllowFailReason());
        return false;
    }

    public boolean isDataEnabled() {
        return this.mDataEnabledSettings.isDataEnabled();
    }

    private boolean isDataAllowedForApn(ApnContext apnContext) {
        boolean z = false;
        if ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            log("Default data call activation not allowed in iwlan.");
            apnContext.setPdpFailCause(DcFailCause.NOT_ALLOWED_RADIO_TECHNOLOGY_IWLAN);
            return false;
        }
        if (apnContext.isReady()) {
            z = !isDataAllowedByApnContext(apnContext) ? isBipApnType(apnContext.getApnType()) : true;
        }
        return z;
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();
        notifyDataConnection(PhoneInternalInterface.REASON_DATA_DETACHED);
        this.mAttached.set(false);
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(false);
        this.mPhone.getServiceStateTracker().setDoRecoveryMarker(true);
        if (this.mCdmaPsRecoveryEnabled && getOverallState() == State.CONNECTED) {
            startPdpResetAlarm(PDP_RESET_ALARM_DELAY_IN_MS);
        }
    }

    private void onDataConnectionAttached() {
        log("onDataConnectionAttached");
        this.mAttached.set(true);
        if (this.mCdmaPsRecoveryEnabled) {
            stopPdpResetAlarm();
        }
        clearRestartRildParam();
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(false);
        if (getOverallState() == State.CONNECTED) {
            log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(false);
            notifyDataConnection(PhoneInternalInterface.REASON_DATA_ATTACHED);
        } else {
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_ATTACHED);
        }
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachOnCreation.set(true);
        }
        if (!(!isCTSimCard(this.mPhone.getPhoneId()) || this.preSetupBasedRadioTech == 0 || this.preSetupBasedRadioTech == this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            log("onDataConnectionAttached need to clear ApnContext, preSetupBasedRadioTech: " + this.preSetupBasedRadioTech);
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == State.SCANNING) {
                    apnContext.setState(State.IDLE);
                    cancelReconnectAlarm(apnContext);
                }
            }
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ATTACHED);
    }

    public boolean isDataAllowed(DataAllowFailReason failureReason, boolean isMms, boolean isUserEnable) {
        boolean z;
        boolean internalDataEnabled = this.mDataEnabledSettings.isInternalDataEnabled();
        boolean attachedState = this.mAttached.get();
        boolean desiredPowerState = this.mPhone.getServiceStateTracker().getDesiredPowerState();
        boolean radioStateFromCarrier = this.mPhone.getServiceStateTracker().getPowerStateFromCarrier();
        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        if (radioTech == 18) {
            desiredPowerState = true;
            radioStateFromCarrier = true;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        int recordsLoaded = 0;
        if (r != null) {
            recordsLoaded = !r.getImsiReady() ? r.getRecordsLoaded() : 1;
            if ((recordsLoaded ^ 1) != 0) {
                log("isDataAllowed getImsiReady=" + r.getImsiReady());
            }
        }
        boolean isDataAllowedVoWiFi = HuaweiTelephonyConfigs.isQcomPlatform() && radioTech == 18;
        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        boolean defaultDataSelected = SubscriptionManager.isValidSubscriptionId(dataSub);
        if (VSimUtilsInner.isVSimEnabled()) {
            defaultDataSelected = true;
        }
        PhoneConstants.State state = PhoneConstants.State.IDLE;
        if (this.mPhone.getCallTracker() != null) {
            state = this.mPhone.getCallTracker().getState();
        }
        if (failureReason != null) {
            failureReason.clearAllReasons();
        }
        if (attachedState || (this.mAutoAttachOnCreation.get() && this.mPhone.getSubId() == dataSub)) {
            z = true;
        } else {
            z = isUserEnable;
        }
        if (!z) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.NOT_ATTACHED);
        }
        if (!(recordsLoaded != 0 || (isUserEnable ^ 1) == 0 || (isDataAllowedVoWiFi ^ 1) == 0)) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.RECORD_NOT_LOADED);
        }
        if (!(state == PhoneConstants.State.IDLE || (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) == 0)) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INVALID_PHONE_STATE);
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.CONCURRENT_VOICE_DATA_NOT_ALLOWED);
        }
        if (!internalDataEnabled) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INTERNAL_DATA_DISABLED);
        }
        if (!defaultDataSelected) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.DEFAULT_DATA_UNSELECTED);
        }
        if (!(isDataAllowedForRoaming(isMms) || !this.mPhone.getServiceState().getDataRoaming() || (getDataRoamingEnabled() ^ 1) == 0) || (isMms && this.mPhone.getServiceState().getDataRoaming() && isRoamingPushDisabled())) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.ROAMING_DISABLED);
        }
        if (this.mIsPsRestricted) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.PS_RESTRICTED);
        }
        if (!desiredPowerState) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.UNDESIRED_POWER_STATE);
        }
        if (!radioStateFromCarrier) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.RADIO_DISABLED_BY_CARRIER);
        }
        if (!isPsAllowedByFdn()) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.PS_RESTRICTED_BY_FDN);
        }
        if (dataSub == 1 && isDataConnectivityDisabled(1, "disable-data")) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INTERNAL_DATA_DISABLED);
            cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
        }
        if (HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave() && get4gSlot() == this.mPhone.getSubId()) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INTERNAL_DATA_DISABLED);
        }
        return failureReason != null ? failureReason.isFailed() ^ 1 : true;
    }

    private void setupDataOnConnectableApns(String reason) {
        setupDataOnConnectableApns(reason, RetryFailures.ALWAYS);
    }

    private void setupDataOnConnectableApns(String reason, RetryFailures retryFailures) {
        this.preSetupBasedRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        log("setupDataOnConnectableApns: current radio technology: " + this.preSetupBasedRadioTech);
        log("setupDataOnConnectableApns: " + reason);
        StringBuilder sb = new StringBuilder(120);
        for (ApnContext apnContext : this.mPrioritySortedApnContexts) {
            sb.append(apnContext.getApnType());
            sb.append(":[state=");
            sb.append(apnContext.getState());
            sb.append(",enabled=");
            sb.append(apnContext.isEnabled());
            sb.append("] ");
        }
        log("setupDataOnConnectableApns: " + reason + " " + sb);
        if (!getmIsPseudoImsi() || (reason.equals(AbstractPhoneInternalInterface.REASON_SET_PS_ONLY_OK) ^ 1) == 0) {
            for (ApnContext apnContext2 : this.mPrioritySortedApnContexts) {
                ArrayList waitingApns = null;
                if (apnContext2.getState() == State.FAILED || apnContext2.getState() == State.SCANNING) {
                    if (retryFailures == RetryFailures.ALWAYS) {
                        apnContext2.releaseDataConnection(reason);
                    } else if (apnContext2.isConcurrentVoiceAndDataAllowed() || !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                        ArrayList<ApnSetting> originalApns = apnContext2.getWaitingApns();
                        if (!(originalApns == null || originalApns.isEmpty())) {
                            waitingApns = buildWaitingApns(apnContext2.getApnType(), radioTech);
                            if (originalApns.size() != waitingApns.size() || !originalApns.containsAll(waitingApns)) {
                                apnContext2.releaseDataConnection(reason);
                            }
                        }
                    } else {
                        apnContext2.releaseDataConnection(reason);
                    }
                }
                if (isDefaultDataSubscription() && (apnContext2.isEnabled() ^ 1) != 0 && PhoneInternalInterface.REASON_SIM_LOADED.equals(reason) && "default".equals(apnContext2.getApnType()) && (this.mIsWifiConnected ^ 1) != 0 && (this.mIsBtConnected ^ 1) != 0) {
                    log("setupDataOnConnectableApns: for IMSI done, call setEnabled");
                    apnContext2.setEnabled(true);
                }
                if (apnContext2.isConnectable()) {
                    log("isConnectable() call trySetupData");
                    if (!getmIsPseudoImsi() || (apnContext2.getApnType().equals("bip0") ^ 1) == 0) {
                        apnContext2.setReason(reason);
                        trySetupData(apnContext2, waitingApns);
                        if (getmIsPseudoImsi()) {
                            log("setupDataOnConnectableApns: pseudo imsi single connection only");
                            break;
                        }
                        HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(true);
                    }
                } else {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, this.mIsWifiConnected, this.mDataEnabledSettings.isDataEnabled(), apnContext2);
                }
            }
            return;
        }
        log("getmIsPseudoImsi(): " + getmIsPseudoImsi() + "  reason: " + reason);
    }

    boolean isEmergency() {
        boolean result = !this.mPhone.isInEcm() ? this.mPhone.isInEmergencyCall() : true;
        log("isEmergency: result=" + result);
        return result;
    }

    private boolean trySetupData(ApnContext apnContext) {
        return trySetupData(apnContext, null);
    }

    private boolean trySetupData(ApnContext apnContext, ArrayList<ApnSetting> waitingApns) {
        log("trySetupData for type:" + apnContext.getApnType() + " due to " + apnContext.getReason() + ", mIsPsRestricted=" + this.mIsPsRestricted);
        HwDataConnectionManager sHwDataConnectionManager = HwTelephonyFactory.getHwDataConnectionManager();
        if (sHwDataConnectionManager != null && sHwDataConnectionManager.getNamSwitcherForSoftbank() && sHwDataConnectionManager.isSoftBankCard(this.mPhone) && !sHwDataConnectionManager.isValidMsisdn(this.mPhone)) {
            log("trySetupData sbnam not allow activate data if MSISDN of softbank card is empty  !");
            return false;
        } else if (isDataConnectivityDisabled(this.mPhone.getSubId(), "disable-data")) {
            return false;
        } else {
            apnContext.requestLog("trySetupData due to " + apnContext.getReason());
            int voiceState = this.mPhone.getServiceState().getVoiceRegState();
            int dataState = this.mPhone.getServiceState().getDataRegState();
            log("dataState = " + dataState + "voiceState = " + voiceState + "OperatorNumeric = " + getOperatorNumeric());
            if ("default".equals(apnContext.getApnType()) && (dataState == 0 || voiceState == 0)) {
                this.mPreferredApn = getApnForCT();
                log("get prefered dp for CT " + this.mPreferredApn);
                if (this.mPreferredApn == null) {
                    this.mPreferredApn = getPreferredApn();
                }
                log("get prefered DP " + this.mPreferredApn);
            }
            if (VSimUtilsInner.isVSimEnabled() && (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) ^ 1) != 0 && ("mms".equals(apnContext.getApnType()) ^ 1) != 0) {
                log("trySetupData not allowed vsim is on for non vsim Dds except mms is enabled");
                return false;
            } else if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) && VSimUtilsInner.isMmsOnM2()) {
                log("trySetupData not allowed for vsim sub while mms is on m2");
                return false;
            } else if (VSimUtilsInner.isVSimOn() && VSimUtilsInner.isSubOnM2(this.mPhone.getPhoneId()) && "mms".equals(apnContext.getApnType()) && VSimUtilsInner.isM2CSOnly()) {
                log("trySetupData not allowed for sub on m2 while ps not ready");
                VSimUtilsInner.checkMmsStart(this.mPhone.getPhoneId());
                return false;
            } else {
                if (!isDefaultDataSubscription()) {
                    if (!(((!"mms".equals(apnContext.getApnType()) ? "xcap".equals(apnContext.getApnType()) : 1) ^ 1) == 0 || (NetworkFactory.isDualCellDataEnable() ^ 1) == 0)) {
                        log("trySetupData not allowed on non defaultDds except mms or xcap is enabled");
                        return false;
                    }
                }
                if (this.mPhone.getSimulatedRadioControl() != null) {
                    apnContext.setState(State.CONNECTED);
                    this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
                    log("trySetupData: X We're on the simulator; assuming connected retValue=true");
                    return true;
                }
                boolean isEmergencyApn = apnContext.getApnType().equals("emergency");
                ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
                DataAllowFailReason failureReason = new DataAllowFailReason();
                boolean unmeteredUseOnly = false;
                boolean isDataAllowed = isDataAllowed(failureReason);
                boolean isMeteredApnType = ApnSetting.isMeteredApnType(apnContext.getApnType(), this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming());
                log("key value isDataAllowed" + isDataAllowed + "isMeteredApnType" + isMeteredApnType);
                if (!isDataAllowed) {
                    if (failureReason.isFailForSingleReason(DataAllowFailReasonType.ROAMING_DISABLED) && (isMeteredApnType ^ 1) != 0) {
                        isDataAllowed = true;
                        unmeteredUseOnly = true;
                    } else if (failureReason.isFailForSingleReason(DataAllowFailReasonType.ROAMING_DISABLED) && (isDataAllowedForRoaming("mms".equals(apnContext.getApnType())) || ("xcap".equals(apnContext.getApnType()) && getXcapDataRoamingEnable()))) {
                        isDataAllowed = true;
                    }
                }
                if (isDataAllowed && !this.mDataEnabledSettings.isDataEnabled()) {
                    isDataAllowed = false;
                    if (!apnContext.hasNoRestrictedRequests(true)) {
                        isDataAllowed = true;
                        unmeteredUseOnly = false;
                    } else if (!isMeteredApnType) {
                        isDataAllowed = true;
                        unmeteredUseOnly = true;
                        if ("default".equals(SystemProperties.get("gsm.bip.apn")) && isBipApnType(apnContext.getApnType())) {
                            unmeteredUseOnly = false;
                        }
                    } else if (getAnyDataEnabledByApnContext(apnContext, getAnyDataEnabled())) {
                        isDataAllowed = true;
                    }
                }
                if (getmIsPseudoImsi() || (apnContext.isConnectable() && (isEmergencyApn || (isDataAllowed && isDataAllowedForApn(apnContext) && (isEmergency() ^ 1) != 0)))) {
                    String str;
                    if (apnContext.getState() == State.FAILED) {
                        str = "trySetupData: make a FAILED ApnContext IDLE so its reusable";
                        log(str);
                        apnContext.requestLog(str);
                        apnContext.setState(State.IDLE);
                    }
                    int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                    apnContext.setConcurrentVoiceAndDataAllowed(sst.isConcurrentVoiceAndDataAllowed());
                    if (apnContext.getState() == State.IDLE) {
                        if (waitingApns == null) {
                            waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                        }
                        if (waitingApns.isEmpty()) {
                            notifyNoData(DcFailCause.MISSING_UNKNOWN_APN, apnContext);
                            notifyOffApnsOfAvailability(apnContext.getReason());
                            str = "trySetupData: X No APN found retValue=false";
                            log(str);
                            apnContext.requestLog(str);
                            return false;
                        }
                        apnContext.setWaitingApns(waitingApns);
                        log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
                    }
                    boolean retValue = setupData(apnContext, radioTech, unmeteredUseOnly);
                    notifyOffApnsOfAvailability(apnContext.getReason());
                    sendOTAAttachTimeoutMsg(apnContext, retValue);
                    log("trySetupData: X retValue=" + retValue);
                    return retValue;
                }
                if (!apnContext.getApnType().equals("default") && apnContext.isConnectable()) {
                    this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
                }
                notifyOffApnsOfAvailability(apnContext.getReason());
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, this.mIsWifiConnected, this.mDataEnabledSettings.isDataEnabled(), apnContext);
                StringBuilder str2 = new StringBuilder();
                str2.append("trySetupData failed. apnContext = [type=").append(apnContext.getApnType()).append(", mState=").append(apnContext.getState()).append(", mDataEnabled=").append(apnContext.isEnabled()).append(", mDependencyMet=").append(apnContext.getDependencyMet()).append("].");
                if (!apnContext.isConnectable()) {
                    str2.append(" isConnectable = false.");
                }
                if (!isDataAllowed) {
                    str2.append(" data not allowed: ").append(failureReason.getDataAllowFailReason()).append(".");
                }
                if (!isDataAllowedForApn(apnContext)) {
                    str2.append(" isDataAllowedForApn = false. RAT = ").append(this.mPhone.getServiceState().getRilDataRadioTechnology()).append(".");
                }
                if (!this.mDataEnabledSettings.isDataEnabled()) {
                    str2.append(" isDataEnabled() = false. isInternalDataEnabled = ").append(this.mDataEnabledSettings.isInternalDataEnabled()).append(", userDataEnabled = ").append(this.mDataEnabledSettings.isUserDataEnabled()).append(", isPolicyDataEnabled = ").append(this.mDataEnabledSettings.isPolicyDataEnabled()).append(", isCarrierDataEnabled = ").append(this.mDataEnabledSettings.isCarrierDataEnabled()).append(".");
                }
                if (isEmergency()) {
                    str2.append(" emergency = true.");
                }
                if (apnContext.getState() == State.SCANNING) {
                    apnContext.setState(State.FAILED);
                    str2.append(" Stop retrying.");
                }
                log(str2.toString());
                apnContext.requestLog(str2.toString());
                return false;
            }
        }
    }

    protected void notifyOffApnsOfAvailability(String reason) {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && (apnContext.isReady() ^ 1) == 0) {
                log("notifyOffApnsOfAvailability skipped apn due to attached && isReady " + apnContext.toString());
            } else if (apnContext.getApnType() == null || !apnContext.getApnType().startsWith("bip")) {
                log("notifyOffApnOfAvailability type:" + apnContext.getApnType());
                this.mPhone.notifyDataConnection(reason != null ? reason : apnContext.getReason(), apnContext.getApnType(), DataState.DISCONNECTED);
            }
        }
    }

    protected boolean cleanUpAllConnections(boolean tearDown, String reason) {
        log("cleanUpAllConnections: tearDown=" + tearDown + " reason=" + reason);
        boolean didDisconnect = false;
        boolean disableMeteredOnly = false;
        if (!TextUtils.isEmpty(reason)) {
            if (reason.equals(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED) || reason.equals(PhoneInternalInterface.REASON_ROAMING_ON)) {
                disableMeteredOnly = true;
            } else {
                disableMeteredOnly = reason.equals(PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
            }
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[apnContext.getState().ordinal()]) {
                case 4:
                case 5:
                case 7:
                    break;
                default:
                    didDisconnect = true;
                    if (!disableMeteredOnly) {
                        apnContext.setReason(reason);
                        cleanUpConnection(tearDown, apnContext);
                        break;
                    }
                    ApnSetting apnSetting = apnContext.getApnSetting();
                    if (!(apnSetting == null || !apnSetting.isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming()) || (apnContext.getApnType().equals("xcap") ^ 1) == 0)) {
                        log("clean up metered ApnContext Type: " + apnContext.getApnType());
                        apnContext.setReason(reason);
                        cleanUpConnection(tearDown, apnContext);
                        break;
                    }
            }
        }
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mRequestedApnType = "default";
        log("cleanUpConnection: mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
        return didDisconnect;
    }

    private void onCleanUpAllConnections(String cause) {
        cleanUpAllConnections(true, cause);
    }

    void sendCleanUpConnection(boolean tearDown, ApnContext apnContext) {
        int i;
        log("sendCleanUpConnection: tearDown=" + tearDown + " apnContext=" + apnContext);
        Message msg = obtainMessage(270360);
        if (tearDown) {
            i = 1;
        } else {
            i = 0;
        }
        msg.arg1 = i;
        msg.arg2 = 0;
        msg.obj = apnContext;
        sendMessage(msg);
    }

    protected void cleanUpConnection(boolean tearDown, ApnContext apnContext) {
        if (apnContext == null) {
            log("cleanUpConnection: apn context is null");
            return;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        String str = "cleanUpConnection: tearDown=" + tearDown + " reason=" + apnContext.getReason();
        log(str + " apnContext=" + apnContext);
        apnContext.requestLog(str);
        if (tearDown) {
            if (apnContext.isDisconnected()) {
                apnContext.setState(State.IDLE);
                if (!apnContext.isReady()) {
                    if (dcac != null) {
                        str = "cleanUpConnection: teardown, disconnected, !ready";
                        log(str + " apnContext=" + apnContext);
                        apnContext.requestLog(str);
                        dcac.tearDown(apnContext, "", null);
                    }
                    apnContext.setDataConnectionAc(null);
                }
            } else if (dcac == null) {
                apnContext.setState(State.IDLE);
                apnContext.requestLog("cleanUpConnection: connected, bug no DCAC");
                this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            } else if (apnContext.getState() != State.DISCONNECTING) {
                boolean disconnectAll = false;
                if ("dun".equals(apnContext.getApnType()) && teardownForDun()) {
                    log("cleanUpConnection: disconnectAll DUN connection");
                    disconnectAll = true;
                }
                int generation = apnContext.getConnectionGeneration();
                str = "cleanUpConnection: tearing down" + (disconnectAll ? " all" : "") + " using gen#" + generation;
                log(str + "apnContext=" + apnContext);
                apnContext.requestLog(str);
                Message msg = obtainMessage(270351, new Pair(apnContext, Integer.valueOf(generation)));
                if (disconnectAll) {
                    apnContext.getDcAc().tearDownAll(apnContext.getReason(), msg);
                } else {
                    apnContext.getDcAc().tearDown(apnContext, apnContext.getReason(), msg);
                }
                apnContext.setState(State.DISCONNECTING);
                this.mDisconnectPendingCount++;
            }
        } else if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(apnContext.getReason()) && apnContext.getState() == State.CONNECTING) {
            log("ignore the set IDLE message, because the current state is connecting!");
        } else {
            if (dcac != null) {
                dcac.reqReset();
            }
            apnContext.setState(State.IDLE);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            apnContext.setDataConnectionAc(null);
        }
        setupDataForSinglePdnArbitration(apnContext.getReason());
        if (dcac != null) {
            cancelReconnectAlarm(apnContext);
        }
        str = "cleanUpConnection: X tearDown=" + tearDown + " reason=" + apnContext.getReason();
        log(str + " apnContext=" + apnContext + " dcac=" + apnContext.getDcAc());
        apnContext.requestLog(str);
    }

    ApnSetting fetchDunApn() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            log("fetchDunApn: net.tethering.noprovisioning=true ret: null");
            return null;
        }
        ApnSetting preferredApn = getPreferredApn();
        if (this.mHwCustDcTracker != null && this.mHwCustDcTracker.isDocomoApn(preferredApn)) {
            return this.mHwCustDcTracker.getDocomoApn(preferredApn);
        }
        int bearer = this.mPhone.getServiceState().getRilDataRadioTechnology();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        ArrayList<ApnSetting> dunCandidates = new ArrayList();
        ApnSetting retDunSetting = null;
        String apnData = Global.getString(this.mResolver, "tether_dun_apn");
        if (!TextUtils.isEmpty(apnData)) {
            dunCandidates.addAll(ApnSetting.arrayFromString(apnData));
            log("fetchDunApn: dunCandidates from Setting: " + dunCandidates);
        } else if (this.mAllowUserEditTetherApn) {
            if (preferredApn != null) {
                for (String type : preferredApn.types) {
                    if (type.contains("dun")) {
                        dunCandidates.add(preferredApn);
                        log("fetchDunApn: add preferredApn");
                        break;
                    }
                }
            }
            for (ApnSetting apn : this.mAllApnSettings) {
                for (String contains : apn.types) {
                    if (contains.contains("dun")) {
                        dunCandidates.add(apn);
                    }
                }
            }
            log("fetchDunApn: dunCandidates from database: " + dunCandidates);
        }
        for (ApnSetting dunSetting : dunCandidates) {
            if (ServiceState.bitmaskHasTech(dunSetting.bearerBitmask, bearer) && dunSetting.numeric.equals(operator)) {
                if (this.mHwCustDcTracker == null || !this.mHwCustDcTracker.addSpecifiedApnSwitch()) {
                    if (dunSetting.hasMvnoParams()) {
                        if (r != null && ApnSetting.mvnoMatches(r, dunSetting.mvnoType, dunSetting.mvnoMatchData)) {
                            retDunSetting = dunSetting;
                            break;
                        }
                    } else if (!this.mMvnoMatched) {
                        retDunSetting = dunSetting;
                        break;
                    }
                } else if (this.mHwCustDcTracker.addSpecifiedApnToWaitingApns(this, preferredApn, dunSetting)) {
                    retDunSetting = dunSetting;
                    break;
                }
            }
        }
        disableGoogleDunApn(this.mPhone.getContext(), apnData, retDunSetting);
        log("fetchDunApn: dunSetting=" + retDunSetting);
        return retDunSetting;
    }

    public boolean hasMatchedTetherApnSetting() {
        ApnSetting matched = fetchDunApn();
        log("hasMatchedTetherApnSetting: APN=" + matched);
        return matched != null;
    }

    protected void setupDataForSinglePdnArbitration(String reason) {
        log("setupDataForSinglePdn: reason = " + reason + " isDisconnected = " + isDisconnected());
        if (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isDisconnected() && (PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION.equals(reason) ^ 1) != 0) {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
        }
    }

    private boolean teardownForDun() {
        boolean z = true;
        if (ServiceState.isCdma(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            return true;
        }
        if (fetchDunApn() == null) {
            z = false;
        }
        return z;
    }

    private void cancelReconnectAlarm(ApnContext apnContext) {
        if (apnContext != null) {
            PendingIntent intent = apnContext.getReconnectIntent();
            if (intent != null) {
                ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(intent);
                apnContext.setReconnectIntent(null);
            }
        }
    }

    private String[] parseTypes(String types) {
        if (types != null && !types.equals("")) {
            return types.split(",");
        }
        return new String[]{CharacterSets.MIMENAME_ANY_CHARSET};
    }

    boolean isPermanentFailure(DcFailCause dcFailCause) {
        if (dcFailCause.isPermanentFailure(this.mPhone.getContext(), this.mPhone.getSubId())) {
            return (this.mAttached.get() && dcFailCause == DcFailCause.SIGNAL_LOST) ? false : true;
        } else {
            return false;
        }
    }

    private ApnSetting makeApnSetting(Cursor cursor) {
        String[] types = parseTypes(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        ApnSetting apn = new ApnSetting(cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID)), cursor.getString(cursor.getColumnIndexOrThrow("numeric")), cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getString(cursor.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("proxy"))), cursor.getString(cursor.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsproxy"))), cursor.getString(cursor.getColumnIndexOrThrow("mmsport")), cursor.getString(cursor.getColumnIndexOrThrow("user")), cursor.getString(cursor.getColumnIndexOrThrow("password")), cursor.getInt(cursor.getColumnIndexOrThrow("authtype")), types, cursor.getString(cursor.getColumnIndexOrThrow("protocol")), cursor.getString(cursor.getColumnIndexOrThrow("roaming_protocol")), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_enabled")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("bearer")), cursor.getInt(cursor.getColumnIndexOrThrow("bearer_bitmask")), cursor.getInt(cursor.getColumnIndexOrThrow("profile_id")), cursor.getInt(cursor.getColumnIndexOrThrow("modem_cognitive")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("max_conns")), cursor.getInt(cursor.getColumnIndexOrThrow("wait_time")), cursor.getInt(cursor.getColumnIndexOrThrow("max_conns_time")), cursor.getInt(cursor.getColumnIndexOrThrow("mtu")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_type")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data")));
        ApnSetting hwApn = makeHwApnSetting(cursor, types);
        if (hwApn != null) {
            return hwApn;
        }
        return apn;
    }

    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ArrayList<ApnSetting> result;
        ArrayList<ApnSetting> mnoApns = new ArrayList();
        ArrayList<ApnSetting> mvnoApns = new ArrayList();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (cursor.moveToFirst()) {
            do {
                ApnSetting apn = makeApnSetting(cursor);
                if (apn != null) {
                    if (!apn.hasMvnoParams()) {
                        mnoApns.add(apn);
                    } else if (r != null && ApnSetting.mvnoMatches(r, apn.mvnoType, apn.mvnoMatchData)) {
                        mvnoApns.add(apn);
                    }
                }
            } while (cursor.moveToNext());
        }
        if (mvnoApns.isEmpty()) {
            result = mnoApns;
            this.mMvnoMatched = false;
        } else {
            result = mvnoApns;
            this.mMvnoMatched = true;
        }
        log("createApnList: X result=" + result);
        return result;
    }

    private boolean dataConnectionNotInUse(DcAsyncChannel dcac) {
        log("dataConnectionNotInUse: check if dcac is inuse dcac=" + dcac);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getDcAc() == dcac) {
                log("dataConnectionNotInUse: in use by apnContext=" + apnContext);
                return false;
            }
        }
        log("dataConnectionNotInUse: tearDownAll");
        dcac.tearDownAll("No connection", null);
        log("dataConnectionNotInUse: not in use return true");
        return true;
    }

    private DcAsyncChannel findFreeDataConnection() {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            if (dcac.isInactiveSync() && dataConnectionNotInUse(dcac)) {
                log("findFreeDataConnection: found free DataConnection= dcac=" + dcac);
                return dcac;
            }
        }
        log("findFreeDataConnection: NO free DataConnection");
        return null;
    }

    protected boolean isLTENetwork() {
        int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        log("dataRadioTech = " + dataRadioTech);
        if (dataRadioTech == 13 || dataRadioTech == 14) {
            return true;
        }
        return false;
    }

    private ApnSetting getApnForCT() {
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            log("getApnForCT not isCTSimCard");
            return null;
        } else if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            log("getApnForCT mAllApnSettings == null");
            return null;
        } else if (get2gSlot() == this.mPhone.getSubId() && (isCTDualModeCard(get2gSlot()) ^ 1) != 0) {
            log("getApnForCT otherslot == mPhone.getSubId() && !isCTDualModeCard(otherslot)");
            return null;
        } else if (this.mPhone.getServiceState().getOperatorNumeric() == null) {
            log("getApnForCT mPhone.getServiceState().getOperatorNumeric() == null");
            return null;
        } else if (getPreferredApn() != null && (isApnPreset(getPreferredApn()) ^ 1) != 0) {
            return null;
        } else {
            ApnSetting apnSetting = null;
            this.mCurrentState = getCurState();
            int matchApnId = matchApnId(this.mCurrentState);
            if (-1 == matchApnId) {
                switch (this.mCurrentState) {
                    case 0:
                        if (!isCTCardForFullNet()) {
                            apnSetting = setApnForCT(CT_NOT_ROAMING_APN_PREFIX);
                            break;
                        }
                        log("getApnForCT: select ctnet for fullNet product");
                        apnSetting = setApnForCT(CT_ROAMING_APN_PREFIX);
                        break;
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
            }
            setPreferredApn(matchApnId);
            return apnSetting;
        }
    }

    private int matchApnId(int sign) {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        if (this.isMultiSimEnabled) {
            preferredApnIdSlot = PREFERRED_APN_ID + (get4gSlot() == this.mPhone.getSubId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String LastApnId = System.getString(cr, preferredApnIdSlot);
            log("MatchApnId:LastApnId: " + LastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (LastApnId != null) {
                String[] ApId = LastApnId.split(",");
                if (5 != ApId.length || ApId[this.mCurrentState] == null) {
                    System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                    return -1;
                } else if (ProxyController.MODEM_0.equals(ApId[this.mCurrentState])) {
                    return -1;
                } else {
                    return Integer.parseInt(ApId[this.mCurrentState]);
                }
            }
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
            return -1;
        } catch (Exception ex) {
            log("MatchApnId got exception =" + ex);
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
            return -1;
        }
    }

    private int getCurState() {
        int currentStatus = -1;
        if (isLTENetwork()) {
            currentStatus = 4;
        } else if (this.mPhone.getPhoneType() == 2) {
            currentStatus = TelephonyManager.getDefault().isNetworkRoaming(get4gSlot()) ? 1 : 0;
        } else if (this.mPhone.getPhoneType() == 1) {
            if (get4gSlot() == this.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(get4gSlot())) {
                currentStatus = 2;
            } else if (get2gSlot() == this.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(get2gSlot())) {
                currentStatus = 3;
            }
        }
        log("getCurState:CurrentStatus =" + currentStatus);
        return currentStatus;
    }

    /* JADX WARNING: Missing block: B:9:0x0027, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ApnSetting setApnForCT(String apn) {
        if (apn == null || "".equals(apn)) {
            return null;
        }
        ContentResolver resolver = this.mPhone.getContext().getContentResolver();
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty() || resolver == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
        for (ApnSetting dp : this.mAllApnSettings) {
            if (apn.equals(dp.apn) && dp.canHandleType(this.mRequestedApnType)) {
                if ((!isLTENetwork() || dp.bearer == 13 || dp.bearer == 14) && (isLTENetwork() || !(dp.bearer == 13 || dp.bearer == 14))) {
                    resolver.delete(uri, null, null);
                    values.put(APN_ID, Integer.valueOf(dp.id));
                    resolver.insert(uri, values);
                    return dp;
                }
            }
        }
        return null;
    }

    private boolean setupData(ApnContext apnContext, int radioTech, boolean unmeteredUseOnly) {
        log("setupData: apnContext=" + apnContext);
        apnContext.requestLog("setupData");
        DcAsyncChannel dcac = null;
        ApnSetting apnSetting = apnContext.getNextApnSetting();
        if (apnSetting == null) {
            log("setupData: return for no apn found!");
            return false;
        }
        int profileId = apnSetting.profileId;
        if (profileId == 0) {
            profileId = getApnProfileID(apnContext.getApnType());
        }
        if (!(apnContext.getApnType().equals("dun") && teardownForDun())) {
            dcac = checkForCompatibleConnectedApnContext(apnContext);
        }
        if (dcac == null) {
            if (isOnlySingleDcAllowed(radioTech)) {
                if (isHigherPriorityApnContextActive(apnContext)) {
                    log("setupData: Higher priority ApnContext active.  Ignoring call");
                    return false;
                } else if (cleanUpAllConnections(true, PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION)) {
                    log("setupData: Some calls are disconnecting first.  Wait and retry");
                    return false;
                } else {
                    log("setupData: Single pdp. Continue setting up data call.");
                }
            }
            dcac = findFreeDataConnection();
            if (dcac == null) {
                dcac = createDataConnection();
            }
            if (dcac == null) {
                log("setupData: No free DataConnection and couldn't create one, WEIRD");
                return false;
            }
        }
        int generation = apnContext.incAndGetConnectionGeneration();
        log("setupData: dcac=" + dcac + " apnSetting=" + apnSetting + " gen#=" + generation);
        apnContext.setDataConnectionAc(dcac);
        apnContext.setApnSetting(apnSetting);
        apnContext.setState(State.CONNECTING);
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        Message msg = obtainMessage();
        msg.what = 270336;
        msg.obj = new Pair(apnContext, Integer.valueOf(generation));
        HwTelephonyFactory.getHwDataServiceChrManager().setBringUp(true);
        dcac.bringUp(apnContext, profileId, radioTech, unmeteredUseOnly, msg, generation);
        log("setupData: initing!");
        return true;
    }

    private void setInitialAttachApn() {
        ApnSetting iaApnSetting = null;
        ApnSetting defaultApnSetting = null;
        ApnSetting firstApnSetting = null;
        if (get4gSlot() != this.mPhone.getSubId() && (HwModemCapability.isCapabilitySupport(21) ^ 1) != 0) {
            log("setInitialAttachApn: not 4g slot , skip");
            if (IS_DELAY_ATTACH_ENABLED) {
                log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                this.mPhone.mCi.dataConnectionAttach(1, null);
            }
        } else if (VSimUtilsInner.isVSimOn() || VSimUtilsInner.isVSimInProcess()) {
            log("setInitialAttachApn: vsim is on or in process, skip");
        } else if (SystemProperties.getBoolean("persist.radio.iot_attach_apn", false)) {
            log("setInitialAttachApn: iot attach apn enabled, skip");
        } else {
            int esmFlag = 0;
            boolean esmFlagAdaptionEnabled = SystemProperties.getBoolean("ro.config.attach_apn_enabled", false);
            if (esmFlagAdaptionEnabled) {
                String plmnsConfig = System.getString(this.mPhone.getContext().getContentResolver(), "plmn_esm_flag");
                log("setInitialAttachApn: plmnsConfig = " + plmnsConfig);
                IccRecords r = (IccRecords) this.mIccRecords.get();
                String operator = r != null ? r.getOperatorNumeric() : "null";
                if (plmnsConfig != null) {
                    for (String plmn : plmnsConfig.split(",")) {
                        if (plmn != null && plmn.equals(operator)) {
                            log("setInitialAttachApn: send initial attach apn for operator " + operator);
                            esmFlag = 1;
                            break;
                        }
                    }
                }
            }
            if (isCTSimCard(this.mPhone.getPhoneId())) {
                log("setInitialAttachApn: send initial attach apn for CT");
                esmFlag = 1;
            }
            if (esmFlag != 0) {
                log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
                if (this.mPreferredApn != null && this.mPreferredApn.canHandleType("ia")) {
                    iaApnSetting = this.mPreferredApn;
                } else if (this.mAllApnSettings != null && (this.mAllApnSettings.isEmpty() ^ 1) != 0) {
                    firstApnSetting = (ApnSetting) this.mAllApnSettings.get(0);
                    log("setInitialApn: firstApnSetting=" + firstApnSetting);
                    for (ApnSetting apn : this.mAllApnSettings) {
                        if (apn.canHandleType("ia")) {
                            log("setInitialApn: iaApnSetting=" + apn);
                            iaApnSetting = apn;
                            break;
                        } else if (defaultApnSetting == null) {
                            if (apn.canHandleType("default")) {
                                log("setInitialApn: defaultApnSetting=" + apn);
                                if (!isCTSimCard(this.mPhone.getPhoneId())) {
                                    defaultApnSetting = apn;
                                } else if (isSupportLTE(apn)) {
                                    defaultApnSetting = apn;
                                }
                            }
                        }
                    }
                }
                ApnSetting initialAttachApnSetting = null;
                if (this.mPreferredApn != null) {
                    log("setInitialAttachApn: using mPreferredApn");
                    if (isCTSimCard(this.mPhone.getPhoneId())) {
                        initialAttachApnSetting = isSupportLTE(this.mPreferredApn) ? this.mPreferredApn : defaultApnSetting != null ? defaultApnSetting : iaApnSetting;
                    } else {
                        initialAttachApnSetting = this.mPreferredApn;
                    }
                } else if (defaultApnSetting != null) {
                    log("setInitialAttachApn: using defaultApnSetting");
                    initialAttachApnSetting = defaultApnSetting;
                } else if (iaApnSetting != null) {
                    log("setInitialAttachApn: using iaApnSetting");
                    initialAttachApnSetting = iaApnSetting;
                } else if (firstApnSetting != null) {
                    log("setInitialAttachApn: using firstApnSetting");
                    if (!isCTSimCard(this.mPhone.getPhoneId())) {
                        initialAttachApnSetting = firstApnSetting;
                    } else if (isSupportLTE(firstApnSetting)) {
                        initialAttachApnSetting = firstApnSetting;
                    }
                }
                if (initialAttachApnSetting == null) {
                    log("setInitialAttachApn: X There in no available apn");
                    if (IS_DELAY_ATTACH_ENABLED) {
                        log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                        this.mPhone.mCi.dataConnectionAttach(1, null);
                    }
                } else {
                    log("setInitialAttachApn: X selected Apn=" + initialAttachApnSetting);
                    HwDataConnectionManager sHwDataConnectionManager = HwTelephonyFactory.getHwDataConnectionManager();
                    if (sHwDataConnectionManager != null && sHwDataConnectionManager.getNamSwitcherForSoftbank()) {
                        HashMap<String, String> userInfo = sHwDataConnectionManager.encryptApnInfoForSoftBank(this.mPhone, initialAttachApnSetting);
                        if (userInfo != null) {
                            this.mPhone.mCi.setInitialAttachApn(new DataProfile(initialAttachApnSetting, (String) userInfo.get("username"), (String) userInfo.get("password")), this.mPhone.getServiceState().getDataRoaming(), null);
                            log("onConnect: mApnSetting.user-mApnSetting.password handle finish");
                            return;
                        }
                    }
                    this.mPhone.mCi.setInitialAttachApn(new DataProfile(initialAttachApnSetting), this.mPhone.getServiceState().getDataRoaming(), null);
                }
            } else if (esmFlagAdaptionEnabled) {
                log("setInitialAttachApn: send empty initial attach apn to clear esmflag");
                this.mPhone.mCi.setInitialAttachApn(new DataProfile(0, "", SystemProperties.get("ro.config.attach_ip_type", "IP"), 0, "", "", 0, 0, 0, 0, false, 0, "", 0, 0, "", "", false), this.mPhone.getServiceState().getDataRoaming(), null);
            } else {
                log("setInitialAttachApn: no need to send initial attach apn");
                if (IS_DELAY_ATTACH_ENABLED) {
                    log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                    this.mPhone.mCi.dataConnectionAttach(1, null);
                }
            }
        }
    }

    private void onApnChanged() {
        State overallState = getOverallState();
        boolean isDisconnected = overallState != State.IDLE ? overallState == State.FAILED : true;
        if (this.mPhone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        }
        log("onApnChanged: createAllApnList and cleanUpAllConnections");
        createAllApnList();
        setInitialAttachApn();
        ApnSetting mCurPreApn = getPreferredApn();
        if (this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId() && mCurPreApn == null) {
            cleanUpAllConnections(isDisconnected ^ 1, PhoneInternalInterface.REASON_APN_CHANGED);
        } else {
            cleanUpConnectionsOnUpdatedApns(isDisconnected ^ 1);
        }
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_APN_CHANGED);
        }
    }

    private void updateApnId() {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        if (this.isMultiSimEnabled) {
            preferredApnIdSlot = PREFERRED_APN_ID + (get4gSlot() == this.mPhone.getSubId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String LastApnId = System.getString(cr, preferredApnIdSlot);
            this.mCurrentState = getCurState();
            log("updateApnId:LastApnId: " + LastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (LastApnId != null) {
                String[] ApId = LastApnId.split(",");
                ApnSetting CurPreApn = getPreferredApn();
                StringBuffer temApnId = new StringBuffer();
                if (5 != ApId.length || ApId[this.mCurrentState] == null) {
                    System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                    return;
                }
                if (CurPreApn == null) {
                    log("updateApnId:CurPreApn: CurPreApn == null");
                    ApId[this.mCurrentState] = ProxyController.MODEM_0;
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
                System.putString(cr, preferredApnIdSlot, temApnId.toString());
                return;
            }
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        } catch (Exception ex) {
            log("updateApnId got exception =" + ex);
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        }
    }

    private DcAsyncChannel findDataConnectionAcByCid(int cid) {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            if (dcac.getCidSync() == cid) {
                return dcac;
            }
        }
        return null;
    }

    private void gotoIdleAndNotifyDataConnection(String reason) {
        log("gotoIdleAndNotifyDataConnection: reason=" + reason);
        notifyDataConnection(reason);
    }

    private boolean isHigherPriorityApnContextActive(ApnContext apnContext) {
        for (ApnContext otherContext : this.mPrioritySortedApnContexts) {
            if (apnContext.getApnType().equalsIgnoreCase(otherContext.getApnType())) {
                return false;
            }
            if (otherContext.isEnabled() && otherContext.getState() != State.FAILED) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnlySingleDcAllowed(int rilRadioTech) {
        int[] singleDcRats = null;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle bundle = configManager.getConfig();
            if (bundle != null) {
                singleDcRats = bundle.getIntArray("only_single_dc_allowed_int_array");
            }
        }
        boolean onlySingleDcAllowed = false;
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("persist.telephony.test.singleDc", false)) {
            onlySingleDcAllowed = true;
        }
        if (singleDcRats != null) {
            for (int i = 0; i < singleDcRats.length && !onlySingleDcAllowed; i++) {
                if (rilRadioTech == singleDcRats[i]) {
                    onlySingleDcAllowed = true;
                }
            }
        }
        onlySingleDcAllowed = shouldDisableMultiPdps(onlySingleDcAllowed);
        log("isOnlySingleDcAllowed(" + rilRadioTech + "): " + onlySingleDcAllowed);
        return onlySingleDcAllowed;
    }

    void sendRestartRadio() {
        log("sendRestartRadio:");
        sendMessage(obtainMessage(270362));
    }

    private void restartRadio() {
        log("restartRadio: ************TURN OFF RADIO**************");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        this.mPhone.getServiceStateTracker().powerOffRadioSafely(this);
        SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", ProxyController.MODEM_0)) + 1));
    }

    private boolean retryAfterDisconnected(ApnContext apnContext) {
        if (isDataConnectivityDisabled(this.mPhone.getSubId(), "disable-data")) {
            return false;
        }
        boolean retry = true;
        String reason = apnContext.getReason();
        if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(reason) || (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = false;
        }
        if (AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(reason)) {
            retry = false;
        }
        return retry;
    }

    private void startAlarmForReconnect(long delay, ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent("com.android.internal.telephony.data-reconnect." + apnType);
        intent.addFlags(268435456);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, apnContext.getReason());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE, apnType);
        intent.addFlags(268435456);
        intent.putExtra("subscription", this.mPhone.getSubId());
        log("startAlarmForReconnect: delay=" + delay + " action=" + intent.getAction() + " apn=" + apnContext);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        apnContext.setReconnectIntent(alarmIntent);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    private void notifyNoData(DcFailCause lastFailCauseCode, ApnContext apnContext) {
        log("notifyNoData: type=" + apnContext.getApnType());
        if (isPermanentFailure(lastFailCauseCode) && (apnContext.getApnType().equals("default") ^ 1) != 0) {
            SystemProperties.set("ril.ps_ce_reason", String.valueOf(lastFailCauseCode.getErrorCode()));
            this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
    }

    public boolean getAutoAttachOnCreation() {
        return this.mAutoAttachOnCreation.get();
    }

    private void onRecordsLoadedOrSubIdChanged() {
        log("onRecordsLoadedOrSubIdChanged: createAllApnList");
        if (HwTelephonyFactory.getHwDataConnectionManager().getNamSwitcherForSoftbank()) {
            HwTelephonyFactory.getHwNetworkManager().setPreferredNetworkTypeForLoaded(this.mPhone, Global.getInt(this.mPhone.getContext().getContentResolver(), "preferred_network_mode", 9));
        }
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17956893);
        updateApnContextState();
        createAllApnList();
        if (isBlockSetInitialAttachApn()) {
            log("onRecordsLoadedOrSubIdChanged: block setInitialAttachApn");
        } else if (!getmIsPseudoImsi()) {
            setInitialAttachApn();
        }
        if (getmIsPseudoImsi()) {
            checkPLMN(this.mHwCustDcTracker.getPlmn());
            log("onRecordsLoaded: createAllApnList --return due to IsPseudoImsi");
            return;
        }
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoadedOrSubIdChanged: notifying data availability");
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_SIM_LOADED);
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SIM_LOADED);
    }

    private void onSetCarrierDataEnabled(AsyncResult ar) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "CarrierDataEnable exception: " + ar.exception);
            return;
        }
        synchronized (this.mDataEnabledSettings) {
            boolean enabled = ((Boolean) ar.result).booleanValue();
            if (enabled != this.mDataEnabledSettings.isCarrierDataEnabled()) {
                log("carrier Action: set metered apns enabled: " + enabled);
                this.mDataEnabledSettings.setCarrierDataEnabled(enabled);
                if (enabled) {
                    this.mPhone.notifyOtaspChanged(this.mPhone.getServiceStateTracker().getOtasp());
                    reevaluateDataConnections();
                    setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ENABLED);
                } else {
                    this.mPhone.notifyOtaspChanged(5);
                    cleanUpAllConnections(true, PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
                }
            }
        }
    }

    private void onSimNotReady() {
        log("onSimNotReady");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_SIM_NOT_READY);
        this.mAllApnSettings = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachOnCreation.set(false);
    }

    private void onSetDependencyMet(String apnType, boolean met) {
        if (!"hipri".equals(apnType)) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            if (apnContext == null) {
                loge("onSetDependencyMet: ApnContext not found in onSetDependencyMet(" + apnType + ", " + met + ")");
                return;
            }
            applyNewState(apnContext, apnContext.isEnabled(), met);
            if ("default".equals(apnType)) {
                apnContext = (ApnContext) this.mApnContexts.get("hipri");
                if (apnContext != null) {
                    applyNewState(apnContext, apnContext.isEnabled(), met);
                }
            }
        }
    }

    public void setPolicyDataEnabled(boolean enabled) {
        log("setPolicyDataEnabled: " + enabled);
        Message msg = obtainMessage(270368);
        msg.arg1 = enabled ? 1 : 0;
        sendMessage(msg);
    }

    private void onSetPolicyDataEnabled(boolean enabled) {
        synchronized (this.mDataEnabledSettings) {
            if (this.mDataEnabledSettings.isPolicyDataEnabled() != enabled) {
                this.mDataEnabledSettings.setPolicyDataEnabled(enabled);
                if (enabled) {
                    reevaluateDataConnections();
                    onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                } else {
                    onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                }
            }
        }
    }

    private void applyNewState(ApnContext apnContext, boolean enabled, boolean met) {
        boolean cleanup = false;
        boolean trySetup = false;
        String str = "applyNewState(" + apnContext.getApnType() + ", " + enabled + "(" + apnContext.isEnabled() + "), " + met + "(" + apnContext.getDependencyMet() + "))";
        log(str);
        apnContext.requestLog(str);
        if (apnContext.isReady()) {
            cleanup = true;
            if (enabled && met) {
                State state = apnContext.getState();
                switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[state.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                        log("applyNewState: 'ready' so return");
                        apnContext.requestLog("applyNewState state=" + state + ", so return");
                        return;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        if (!CUST_RETRY_CONFIG || !"mms".equals(apnContext.getApnType())) {
                            trySetup = true;
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            break;
                        }
                        log("applyNewState: the mms is retrying,return.");
                        return;
                        break;
                }
            } else if (met) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DISABLED);
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            }
        } else if (enabled && met) {
            if (apnContext.isEnabled()) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_MET);
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
            }
            if (apnContext.getState() == State.FAILED) {
                apnContext.setState(State.IDLE);
            }
            trySetup = true;
        }
        apnContext.setEnabled(enabled);
        apnContext.setDependencyMet(met);
        if (cleanup) {
            cleanUpConnection(true, apnContext);
            if ("default".equals(apnContext.getApnType())) {
                log("applyNewState disable default apncontext, need to reset all param");
                clearRestartRildParam();
            }
        }
        if (trySetup) {
            apnContext.resetErrorCodeRetries();
            trySetupData(apnContext);
        }
    }

    private DcAsyncChannel checkForCompatibleConnectedApnContext(ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        ApnSetting dunSetting = null;
        ApnSetting bipSetting = null;
        if ("dun".equals(apnType)) {
            dunSetting = fetchDunApn();
        }
        if (isBipApnType(apnType)) {
            bipSetting = fetchBipApn(this.mPreferredApn, this.mAllApnSettings);
        }
        log("checkForCompatibleConnectedApnContext: apnContext=" + apnContext);
        DcAsyncChannel potentialDcac = null;
        Object potentialApnCtx = null;
        for (ApnContext curApnCtx : this.mApnContexts.values()) {
            DcAsyncChannel curDcac = curApnCtx.getDcAc();
            if (curDcac != null) {
                ApnSetting apnSetting = curApnCtx.getApnSetting();
                log("apnSetting: " + apnSetting);
                if (dunSetting == null) {
                    if (bipSetting == null) {
                        if (apnSetting != null && ((apnContext.getWaitingApns() == null && apnSetting.canHandleType(apnType)) || ((apnContext.getWaitingApns() != null && apnContext.getWaitingApns().contains(apnSetting)) || (this.mHwCustDcTracker != null && this.mHwCustDcTracker.isDocomoTetheringApn(apnSetting, apnType))))) {
                            switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[curApnCtx.getState().ordinal()]) {
                                case 1:
                                    log("checkForCompatibleConnectedApnContext: found canHandle conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                    return curDcac;
                                case 2:
                                case 6:
                                    potentialDcac = curDcac;
                                    potentialApnCtx = curApnCtx;
                                    break;
                                case 3:
                                    if (potentialDcac != null) {
                                        break;
                                    }
                                    potentialDcac = curDcac;
                                    potentialApnCtx = curApnCtx;
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (bipSetting.equals(apnSetting)) {
                        switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[curApnCtx.getState().ordinal()]) {
                            case 1:
                                log("checkForCompatibleConnectedApnContext: found bip conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                return curDcac;
                            case 2:
                            case 6:
                                potentialDcac = curDcac;
                                potentialApnCtx = curApnCtx;
                                break;
                            default:
                                break;
                        }
                    } else {
                        continue;
                    }
                } else if (dunSetting.equals(apnSetting)) {
                    switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[curApnCtx.getState().ordinal()]) {
                        case 1:
                            log("checkForCompatibleConnectedApnContext: found dun conn=" + curDcac + " curApnCtx=" + curApnCtx);
                            return curDcac;
                        case 2:
                        case 6:
                            potentialDcac = curDcac;
                            potentialApnCtx = curApnCtx;
                            break;
                        case 3:
                            if (potentialDcac != null) {
                                break;
                            }
                            potentialDcac = curDcac;
                            potentialApnCtx = curApnCtx;
                            break;
                        default:
                            break;
                    }
                } else {
                    continue;
                }
            } else {
                log("checkForCompatibleConnectedApnContext: not conn curApnCtx=" + curApnCtx);
            }
        }
        if (potentialDcac != null) {
            log("checkForCompatibleConnectedApnContext: found potential conn=" + potentialDcac + " curApnCtx=" + potentialApnCtx);
            return potentialDcac;
        }
        log("checkForCompatibleConnectedApnContext: NO conn apnContext=" + apnContext);
        return null;
    }

    public void setEnabled(int id, boolean enable) {
        Message msg = obtainMessage(270349);
        msg.arg1 = id;
        msg.arg2 = enable ? 1 : 0;
        sendMessage(msg);
    }

    private void onEnableApn(int apnId, int enabled) {
        boolean z = true;
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext == null) {
            loge("onEnableApn(" + apnId + ", " + enabled + "): NO ApnContext");
            return;
        }
        clearAndResumeNetInfiForWifiLteCoexist(apnId, enabled, apnContext);
        log("onEnableApn: apnContext=" + apnContext + " call applyNewState");
        if (enabled != 1) {
            z = false;
        }
        applyNewState(apnContext, z, apnContext.getDependencyMet());
        if (enabled == 0 && isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && (isHigherPriorityApnContextActive(apnContext) ^ 1) != 0) {
            log("onEnableApn: isOnlySingleDcAllowed true & higher priority APN disabled");
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
        }
    }

    protected boolean onTrySetupData(String reason) {
        log("onTrySetupData: reason=" + reason);
        setupDataOnConnectableApns(reason);
        return true;
    }

    protected boolean onTrySetupData(ApnContext apnContext) {
        log("onTrySetupData: apnContext=" + apnContext);
        return trySetupData(apnContext);
    }

    public boolean getDataEnabled() {
        int i = 1;
        int device_provisioned = Global.getInt(this.mResolver, "device_provisioned", 0);
        boolean retVal = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true"));
        if (TelephonyManager.getDefault().getSimCount() == 1) {
            int i2;
            ContentResolver contentResolver = this.mResolver;
            String str = "mobile_data";
            if (retVal) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            retVal = Global.getInt(contentResolver, str, i2) != 0;
        } else {
            int phoneSubId = this.mPhone.getSubId();
            try {
                retVal = VSimUtilsInner.isVSimSub(phoneSubId) ? this.mDataEnabledSettings.isUserDataEnabled() : TelephonyManager.getIntWithSubId(this.mResolver, "mobile_data", phoneSubId) != 0;
            } catch (SettingNotFoundException e) {
            }
        }
        log("getDataEnabled: retVal=" + retVal);
        if (hasSetCustDataFeature()) {
            return retVal;
        }
        if (device_provisioned == 0) {
            String prov_property = SystemProperties.get("ro.com.android.prov_mobiledata", retVal ? "true" : "false");
            retVal = "true".equalsIgnoreCase(prov_property);
            ContentResolver contentResolver2 = this.mResolver;
            String str2 = "device_provisioning_mobile_data";
            if (!retVal) {
                i = 0;
            }
            int prov_mobile_data = Global.getInt(contentResolver2, str2, i);
            retVal = prov_mobile_data != 0;
            log("getDataEnabled during provisioning retVal=" + retVal + " - (" + prov_property + ", " + prov_mobile_data + ")");
        }
        return retVal;
    }

    public void setDataRoamingEnabled(boolean enabled) {
        int phoneSubId = this.mPhone.getSubId();
        if (getDataRoamingEnabled() != enabled) {
            int roaming = enabled ? 1 : 0;
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                Global.putInt(this.mResolver, "data_roaming", roaming);
            } else {
                Global.putInt(this.mResolver, getDataRoamingSettingItem("data_roaming"), roaming);
            }
            this.mSubscriptionManager.setDataRoaming(roaming, phoneSubId);
            log("setDataRoamingEnabled: set phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
            return;
        }
        log("setDataRoamingEnabled: unchanged phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
    }

    public boolean getDataRoamingEnabled() {
        int i = 1;
        boolean isDataRoamingEnabled = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false"));
        int phoneSubId = this.mPhone.getSubId();
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            return true;
        }
        try {
            if (isNeedDataRoamingExpend()) {
                isDataRoamingEnabled = getDataRoamingEnabledWithNational();
            } else if (TelephonyManager.getDefault().getSimCount() == 1) {
                ContentResolver contentResolver = this.mResolver;
                String str = "data_roaming";
                if (!isDataRoamingEnabled) {
                    i = 0;
                }
                isDataRoamingEnabled = Global.getInt(contentResolver, str, i) != 0;
            } else {
                isDataRoamingEnabled = Global.getInt(this.mResolver, getDataRoamingSettingItem("data_roaming")) != 0;
            }
        } catch (SettingNotFoundException snfe) {
            log("getDataRoamingEnabled: SettingNofFoundException snfe=" + snfe);
        }
        log("getDataRoamingEnabled: phoneSubId=" + phoneSubId + " isDataRoamingEnabled=" + isDataRoamingEnabled);
        return isDataRoamingEnabled;
    }

    private void onDataRoamingOff() {
        log("onDataRoamingOff");
        if (processAttDataRoamingOff()) {
            log("process ATT DataRoaming off");
            return;
        }
        if (getDataRoamingEnabled()) {
            notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_OFF);
        } else {
            if (!TextUtils.isEmpty(getOperatorNumeric())) {
                setInitialAttachApn();
            }
            setDataProfilesAsNeeded();
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_OFF);
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_OFF);
        }
    }

    private void onDataRoamingOnOrSettingsChanged() {
        log("onDataRoamingOnOrSettingsChanged");
        if (processAttDataRoamingOn()) {
            log("process ATT DataRoaming off");
        } else if (this.mPhone.getServiceState().getDataRoaming()) {
            if (getDataRoamingEnabled()) {
                log("onDataRoamingOnOrSettingsChanged: setup data on roaming");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_ON);
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_ON);
            } else {
                log("onDataRoamingOnOrSettingsChanged: Tear down data connection on roaming.");
                cleanUpAllConnections(true, PhoneInternalInterface.REASON_ROAMING_ON);
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
            }
        } else {
            log("device is not roaming. ignored the request.");
        }
    }

    private void onRadioAvailable() {
        log("onRadioAvailable");
        if (this.mPhone.getSimulatedRadioControl() != null) {
            notifyDataConnection(null);
            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null && (r.getImsiReady() || r.getRecordsLoaded())) {
            notifyOffApnsOfAvailability(null);
        }
        if (getOverallState() != State.IDLE) {
            cleanUpConnection(true, null);
        }
    }

    private void onRadioOffOrNotAvailable() {
        this.mReregisterOnReconnectFailure = false;
        this.mAutoAttachOnCreation.set(false);
        this.mIsPsRestricted = false;
        this.mPhone.getServiceStateTracker().mRestrictedState.setPsRestricted(false);
        if (this.mPhone.getSimulatedRadioControl() != null) {
            log("We're on the simulator; assuming radio off is meaningless");
        } else {
            log("onRadioOffOrNotAvailable: is off and clean up all connections");
            cleanUpAllConnections(false, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        }
        notifyOffApnsOfAvailability(null);
    }

    private void completeConnection(ApnContext apnContext) {
        log("completeConnection: successful, notify the world apnContext=" + apnContext);
        if (this.mIsProvisioning && (TextUtils.isEmpty(this.mProvisioningUrl) ^ 1) != 0) {
            log("completeConnection: MOBILE_PROVISIONING_ACTION url=" + this.mProvisioningUrl);
            Intent newIntent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
            newIntent.setData(Uri.parse(this.mProvisioningUrl));
            newIntent.setFlags(272629760);
            try {
                this.mPhone.getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                loge("completeConnection: startActivityAsUser failed" + e);
            }
        }
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        if (this.mProvisioningSpinner != null) {
            sendMessage(obtainMessage(270378, this.mProvisioningSpinner));
        }
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        startNetStatPoll();
        startDataStallAlarm(false);
    }

    private boolean needSetCTProxy(ApnSetting apn) {
        boolean needSet = false;
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            return false;
        }
        String networkOperatorNumeric = this.mPhone.getServiceState().getOperatorNumeric();
        if (!(apn == null || apn.apn == null || !apn.apn.contains(CT_NOT_ROAMING_APN_PREFIX) || networkOperatorNumeric == null || !"46012".equals(networkOperatorNumeric))) {
            needSet = true;
        }
        return needSet;
    }

    private void onDataSetupComplete(AsyncResult ar) {
        DcFailCause cause = DcFailCause.UNKNOWN;
        boolean handleError = false;
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupComplete");
        if (apnContext != null) {
            boolean isDefault = "default".equals(apnContext.getApnType());
            ApnSetting apn;
            Intent intent;
            if (ar.exception == null) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac == null) {
                    log("onDataSetupComplete: no connection to DC, handle as error");
                    cause = DcFailCause.CONNECTION_TO_DATACONNECTIONAC_BROKEN;
                    handleError = true;
                } else {
                    addIfacePhoneHashMap(dcac, mIfacePhoneHashMap);
                    if (isClearCodeEnabled() && isDefault) {
                        resetTryTimes();
                    }
                    apn = apnContext.getApnSetting();
                    log("onDataSetupComplete: success apn=" + (apn == null ? "unknown" : apn.apn));
                    if (isDefault) {
                        SystemProperties.set("gsm.default.apn", apn == null ? "" : apn.apn);
                        log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
                    }
                    if (needSetCTProxy(apn)) {
                        try {
                            dcac.setLinkPropertiesHttpProxySync(new ProxyInfo("10.0.0.200", Integer.parseInt("80"), "127.0.0.1"));
                        } catch (NumberFormatException e) {
                            loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.apn + "): " + e);
                        }
                    } else if (!(apn == null || apn.proxy == null || apn.proxy.length() == 0)) {
                        try {
                            String port = apn.port;
                            if (TextUtils.isEmpty(port)) {
                                port = "8080";
                            }
                            dcac.setLinkPropertiesHttpProxySync(new ProxyInfo(apn.proxy, Integer.parseInt(port), "127.0.0.1"));
                        } catch (NumberFormatException e2) {
                            loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.port + "): " + e2);
                        }
                    }
                    if (TextUtils.equals(apnContext.getApnType(), "default")) {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "true");
                        } catch (RuntimeException e3) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to true");
                        }
                        if (this.mCanSetPreferApn && this.mPreferredApn == null) {
                            log("onDataSetupComplete: PREFERRED APN is null");
                            this.mPreferredApn = apn;
                            if (this.mPreferredApn != null) {
                                setPreferredApn(this.mPreferredApn.id);
                            }
                        }
                    } else {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                        } catch (RuntimeException e4) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                        }
                    }
                    apnContext.setPdpFailCause(DcFailCause.NONE);
                    apnContext.setState(State.CONNECTED);
                    boolean isProvApn = apnContext.isProvisioningApn();
                    ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                    if (this.mProvisionBroadcastReceiver != null) {
                        this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
                        this.mProvisionBroadcastReceiver = null;
                    }
                    if (!isProvApn || this.mIsProvisioning) {
                        cm.setProvisioningNotificationVisible(false, 0, this.mProvisionActionName);
                        completeConnection(apnContext);
                    } else {
                        log("onDataSetupComplete: successful, BUT send connected to prov apn as mIsProvisioning:" + this.mIsProvisioning + " == false" + " && (isProvisioningApn:" + isProvApn + " == true");
                        this.mProvisionBroadcastReceiver = new ProvisionNotificationBroadcastReceiver(cm.getMobileProvisioningUrl(), TelephonyManager.getDefault().getNetworkOperatorName());
                        this.mPhone.getContext().registerReceiver(this.mProvisionBroadcastReceiver, new IntentFilter(this.mProvisionActionName));
                        cm.setProvisioningNotificationVisible(true, 0, this.mProvisionActionName);
                        setRadio(false);
                    }
                    log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType() + ", reason:" + apnContext.getReason());
                    if (Build.IS_DEBUGGABLE) {
                        int pcoVal = SystemProperties.getInt("persist.radio.test.pco", -1);
                        if (pcoVal != -1) {
                            log("PCO testing: read pco value from persist.radio.test.pco " + pcoVal);
                            byte[] value = new byte[]{(byte) pcoVal};
                            intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                            intent.putExtra("apnType", "default");
                            intent.putExtra("apnProto", "IPV4V6");
                            intent.putExtra("pcoId", 65280);
                            intent.putExtra("pcoValue", value);
                            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                        }
                    }
                    clearRestartRildParam();
                    openServiceStart(this.mUiccController);
                    setFirstTimeEnableData();
                    log("CHR inform CHR the APN info when data setup succ");
                    LinkProperties chrLinkProperties = getLinkProperties(apnContext.getApnType());
                    if (chrLinkProperties != null) {
                        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDataConnected(this.mPhone, apn, chrLinkProperties);
                    }
                    getNetdPid();
                }
            } else {
                cause = ar.result;
                apn = apnContext.getApnSetting();
                String str = "onDataSetupComplete: error apn=%s cause=%s";
                Object[] objArr = new Object[2];
                objArr[0] = apn == null ? "unknown" : apn.apn;
                objArr[1] = cause;
                log(String.format(str, objArr));
                sendDSMipErrorBroadcast();
                if (cause.isEventLoggable()) {
                    int cid = getCellLocationId();
                    EventLog.writeEvent(EventLogTags.PDP_SETUP_FAIL, new Object[]{Integer.valueOf(cause.ordinal()), Integer.valueOf(cid), Integer.valueOf(TelephonyManager.getDefault().getNetworkType())});
                }
                apn = apnContext.getApnSetting();
                this.mPhone.notifyPreciseDataConnectionFailed(apnContext.getReason(), apnContext.getApnType(), apn != null ? apn.apn : "unknown", cause.toString());
                intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED");
                intent.putExtra("errorCode", cause.getErrorCode());
                intent.putExtra("apnType", apnContext.getApnType());
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                if (cause.isRestartRadioFail(this.mPhone.getContext(), this.mPhone.getSubId()) || apnContext.restartOnError(cause.getErrorCode())) {
                    log("Modem restarted.");
                    sendRestartRadio();
                }
                if (isClearCodeEnabled()) {
                    long delay = apnContext.getDelayForNextApn(this.mFailFast);
                    Rlog.d(LOG_TAG, "clearcode onDataSetupComplete delay=" + delay);
                    operateClearCodeProcess(apnContext, cause, (int) delay);
                } else if (isPermanentFailure(cause)) {
                    log("cause = " + cause + ", mark apn as permanent failed. apn = " + apn);
                    apnContext.markApnPermanentFailed(apn);
                }
                handleError = true;
            }
            if (handleError) {
                onDataSetupCompleteError(ar);
            }
            if (!this.mDataEnabledSettings.isInternalDataEnabled()) {
                cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
            }
        }
    }

    private ApnContext getValidApnContext(AsyncResult ar, String logString) {
        if (ar != null && (ar.userObj instanceof Pair)) {
            Pair<ApnContext, Integer> pair = ar.userObj;
            ApnContext apnContext = pair.first;
            if (apnContext != null) {
                int generation = apnContext.getConnectionGeneration();
                log("getValidApnContext (" + logString + ") on " + apnContext + " got " + generation + " vs " + pair.second);
                if (generation == ((Integer) pair.second).intValue()) {
                    return apnContext;
                }
                log("ignoring obsolete " + logString);
                return null;
            }
        }
        throw new RuntimeException(logString + ": No apnContext");
    }

    private void onDataSetupCompleteError(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupCompleteError");
        if (apnContext != null) {
            if (apnContext.isLastApnSetting()) {
                onAllApnFirstActiveFailed();
                if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false) && "bip0".equals(apnContext.getApnType())) {
                    Intent intent = new Intent();
                    intent.setAction(AbstractPhoneInternalInterface.OTA_OPEN_SERVICE_ACTION);
                    intent.putExtra(AbstractPhoneInternalInterface.OTA_TAG, 1);
                    this.mPhone.getContext().sendBroadcast(intent);
                    if (hasMessages(270383)) {
                        removeMessages(270383);
                    }
                    log("sendbroadcast OTA_OPEN_SERVICE_ACTION");
                    return;
                }
            }
            if (HwTelephonyFactory.getHwPhoneManager().isSupportOrangeApn(this.mPhone)) {
                HwTelephonyFactory.getHwPhoneManager().addSpecialAPN(this.mPhone);
                Rlog.d(LOG_TAG, "onDataSetupCompleteError.addSpecialAPN()");
            }
            long delay = apnContext.getDelayForNextApn(this.mFailFast);
            if (isPSClearCodeRplmnMatched()) {
                log("PSCLEARCODE retry APN. old delay = " + delay);
                delay = updatePSClearCodeApnContext(ar, apnContext, delay);
            }
            if (delay >= 0) {
                log("onDataSetupCompleteError: Try next APN. delay = " + delay);
                if (isClearCodeEnabled()) {
                    setCurFailCause(ar);
                }
                apnContext.setState(State.SCANNING);
                if (isClearCodeEnabled()) {
                    delay = (long) getDelayTime();
                }
                startAlarmForReconnect(delay, apnContext);
                if (!(apnContext.getReason() == null || apnContext.getApnSetting() == null || apnContext.getApnType() == null)) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendIntentDataConnectionSetupResult(this.mPhone.getSubId(), DataState.DISCONNECTED.toString(), apnContext.getReason(), apnContext.getApnSetting().apn, apnContext.getApnType(), getLinkProperties(apnContext.getApnType()));
                }
            } else {
                onAllApnPermActiveFailed();
                apnContext.setState(State.FAILED);
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_APN_FAILED, apnContext.getApnType());
                apnContext.setDataConnectionAc(null);
                log("onDataSetupCompleteError: Stop retrying APNs.");
            }
        }
    }

    private void onDataConnectionRedirected(String redirectUrl) {
        if (!TextUtils.isEmpty(redirectUrl)) {
            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED");
            intent.putExtra("redirectionUrl", redirectUrl);
            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            log("Notify carrier signal receivers with redirectUrl: " + redirectUrl);
        }
    }

    private void onDisconnectDone(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDone");
        if (apnContext != null) {
            if (apnContext.getState() == State.CONNECTING) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (!(dcac == null || (dcac.isInactiveSync() ^ 1) == 0 || !dcac.checkApnContextSync(apnContext))) {
                    loge("onDisconnectDone: apnContext is activating, ignore " + apnContext);
                    return;
                }
            }
            log("onDisconnectDone: EVENT_DISCONNECT_DONE apnContext=" + apnContext);
            apnContext.setState(State.IDLE);
            if ("default".equals(apnContext.getApnType())) {
                SystemProperties.set("gsm.default.apn", "");
                log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
            }
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            if (this.mCdmaPsRecoveryEnabled && getOverallState() != State.CONNECTED) {
                stopPdpResetAlarm();
            }
            if (isDisconnected()) {
                if (this.mPhone.getServiceStateTracker().processPendingRadioPowerOffAfterDataOff()) {
                    log("onDisconnectDone: radio will be turned off, no retries");
                    apnContext.setApnSetting(null);
                    apnContext.setDataConnectionAc(null);
                    if (this.mDisconnectPendingCount > 0) {
                        this.mDisconnectPendingCount--;
                    }
                    if (this.mDisconnectPendingCount == 0) {
                        notifyDataDisconnectComplete();
                        notifyAllDataDisconnected();
                    }
                    return;
                }
                log("data is disconnected and check if need to setPreferredNetworkType");
                ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
                if (sst != null) {
                    HwTelephonyFactory.getHwNetworkManager().checkAndSetNetworkType(sst, this.mPhone);
                }
                if (!(sst == null || sst.returnObject() == null || !sst.returnObject().isDataOffForbidLTE())) {
                    log("DCT onDisconnectDone");
                    sst.returnObject().processEnforceLTENetworkTypePending();
                }
            }
            if (this.mAttached.get() && apnContext.isReady() && retryAfterDisconnected(apnContext)) {
                try {
                    SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                } catch (RuntimeException e) {
                    log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                }
                log("onDisconnectDone: attached, ready and retry after disconnect");
                long delay = apnContext.getInterApnDelay(this.mFailFast);
                if (this.mIsScreenOn && DcFailCause.LOST_CONNECTION.toString().equals(apnContext.getReason())) {
                    delay = 50;
                    log("LOST_CONNECTION reduce the delay time to " + 50);
                }
                if (delay > 0) {
                    startAlarmForReconnect(delay, apnContext);
                }
            } else {
                boolean restartRadioAfterProvisioning = this.mPhone.getContext().getResources().getBoolean(17956995);
                if (apnContext.isProvisioningApn() && restartRadioAfterProvisioning) {
                    log("onDisconnectDone: restartRadio after provisioning");
                    restartRadio();
                }
                apnContext.setApnSetting(null);
                apnContext.setDataConnectionAc(null);
                if (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
                    log("onDisconnectDone: isOnlySigneDcAllowed true so setup single apn");
                    if (AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(apnContext.getReason())) {
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION, apnContext.getApnType());
                    } else {
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
                    }
                } else if (this.mCdmaPsRecoveryEnabled && this.mPhone.getServiceState().getVoiceRegState() == 0 && retryAfterDisconnected(apnContext)) {
                    log("onDisconnectDone: cdma cs attached, retry after disconnect");
                    startAlarmForReconnect(5000, apnContext);
                } else {
                    log("onDisconnectDone: not retrying");
                }
            }
            if (this.mDisconnectPendingCount > 0) {
                this.mDisconnectPendingCount--;
            }
            if (this.mDisconnectPendingCount == 0) {
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                notifyDataDisconnectComplete();
                notifyAllDataDisconnected();
            }
        }
    }

    private void onDisconnectDcRetrying(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDcRetrying");
        if (apnContext != null) {
            apnContext.setState(State.RETRYING);
            log("onDisconnectDcRetrying: apnContext=" + apnContext);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        }
    }

    private void onVoiceCallStarted() {
        log("onVoiceCallStarted");
        this.mInVoiceCall = true;
        if (isConnected() && (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) != 0) {
            log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_STARTED);
        }
    }

    private void onVoiceCallEnded() {
        log("onVoiceCallEnded");
        this.mInVoiceCall = false;
        if (isConnected()) {
            if (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                resetPollStats();
            } else {
                startNetStatPoll();
                startDataStallAlarm(false);
                notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
            }
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
    }

    protected void onVpStatusChanged(AsyncResult ar) {
        log("onVpStatusChanged");
        if (ar.exception != null) {
            log("Exception occurred, failed to report the rssi and ecio.");
            return;
        }
        this.mVpStatus = ((Integer) ar.result).intValue();
        log("onVpStatusChanged, mVpStatus:" + this.mVpStatus);
        if (1 == this.mVpStatus) {
            onVPStarted();
        } else {
            onVPEnded();
        }
    }

    public void onVPStarted() {
        log("onVPStarted");
        this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(false);
        if (isConnected() && (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) != 0 && this.mInVoiceCall) {
            log("onVPStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(PhoneInternalInterface.REASON_VP_STARTED);
        }
    }

    public void onVPEnded() {
        boolean z = false;
        log("onVPEnded");
        if (!this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(true);
            if (isConnected() && this.mInVoiceCall) {
                startNetStatPoll();
                startDataStallAlarm(false);
                synchronized (this.mDataEnabledSettings) {
                    if (this.mDataEnabledSettings.isInternalDataEnabled() && this.mDataEnabledSettings.isUserDataEnabled()) {
                        z = this.mDataEnabledSettings.isPolicyDataEnabled();
                    }
                    if (z) {
                        notifyDataConnection(PhoneInternalInterface.REASON_VP_ENDED);
                    } else {
                        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
                    }
                }
            }
        }
    }

    private void onCleanUpConnection(boolean tearDown, int apnId, String reason) {
        log("onCleanUpConnection");
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext != null) {
            apnContext.setReason(reason);
            cleanUpConnection(tearDown, apnContext);
        }
    }

    private boolean isConnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public boolean isDisconnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!apnContext.isDisconnected()) {
                return false;
            }
        }
        return true;
    }

    protected void notifyDataConnection(String reason) {
        log("notifyDataConnection: reason=" + reason);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                log("notifyDataConnection: type:" + apnContext.getApnType());
                this.mPhone.notifyDataConnection(reason != null ? reason : apnContext.getReason(), apnContext.getApnType());
            }
        }
        notifyOffApnsOfAvailability(reason);
    }

    private void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        if (this.mAllApnSettings != null && (this.mAllApnSettings.isEmpty() ^ 1) != 0) {
            ArrayList<DataProfile> dps = new ArrayList();
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.modemCognitive) {
                    DataProfile dp = new DataProfile(apn);
                    if (!dps.contains(dp)) {
                        dps.add(dp);
                    }
                }
            }
            if (dps.size() > 0) {
                this.mPhone.mCi.setDataProfile((DataProfile[]) dps.toArray(new DataProfile[0]), this.mPhone.getServiceState().getDataRoaming(), null);
            }
        }
    }

    public String getOperatorNumeric() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (operator == null) {
            operator = "";
        }
        log("getOperatorNumberic - returning from card: " + operator);
        return operator;
    }

    public String getCTOperator(String operator) {
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            return operator;
        }
        operator = SystemProperties.get("gsm.national_roaming.apn", "46003");
        log("Select china telecom hplmn: " + operator);
        return operator;
    }

    private void createAllApnList() {
        this.mMvnoMatched = false;
        this.mAllApnSettings = new ArrayList();
        String operator = getCTOperator(getOperatorNumeric());
        IccRecords record = (IccRecords) this.mIccRecords.get();
        String preSpn = record != null ? record.getServiceProviderName() : "";
        String preIccid = SystemProperties.get("gsm.sim.preiccid_" + this.mPhone.getPhoneId(), "");
        if ("46003".equals(operator) && (GC_ICCID.equals(preIccid) || GC_SPN.equals(preSpn))) {
            log("Hongkong GC card and iccid is: " + preIccid + ",spn is: " + preSpn);
            operator = GC_MCCMNC;
        }
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(this.mPhone.getSubId()))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mPhone.getSubId()));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            Cursor cursor;
            String selection = "numeric = '" + operator + "'";
            log("createAllApnList: selection=" + selection);
            String subId = Long.toString((long) this.mPhone.getSubId());
            if (this.isMultiSimEnabled) {
                cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, subId), null, selection, null, HbpcdLookup.ID);
            } else {
                cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, HbpcdLookup.ID);
            }
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mAllApnSettings = createApnList(cursor);
                }
                cursor.close();
            }
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (this.mAllApnSettings.isEmpty() && (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) ^ 1) != 0 && get4gSlot() == this.mPhone.getSubId() && r != null && r.getRecordsLoaded() && operator.length() != 0) {
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnListEmpty(this.mPhone.getSubId());
            }
        }
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) && this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: vsim enabled and apn not in database");
            this.mAllApnSettings = VSimUtilsInner.createVSimApnList();
        }
        addEmergencyApnSetting();
        if (this.mAllApnSettings.isEmpty() || VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            log("createAllApnList: No APN found for carrier: " + operator);
            this.mPreferredApn = null;
        } else {
            this.mPreferredApn = getPreferredApn();
            if (this.mPreferredApn == null) {
                ApnSetting apn = getCustPreferredApn(this.mAllApnSettings);
                if (apn != null) {
                    setPreferredApn(apn.id);
                    this.mPreferredApn = getPreferredApn();
                }
            }
            if (!(this.mPreferredApn == null || (this.mPreferredApn.numeric.equals(operator) ^ 1) == 0)) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            if (!SystemProperties.getBoolean("persist.radio.fixwapapn", false)) {
                if (this.mPreferredApn != null && WAP_APN.equals(this.mPreferredApn.apn)) {
                    log("fixWapApn:" + this.mPreferredApn);
                    this.mPreferredApn = null;
                    setPreferredApn(-1);
                }
                SystemProperties.set("persist.radio.fixwapapn", "true");
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
        setDataProfilesAsNeeded();
    }

    public ArrayList<ApnSetting> getAllApnList() {
        return this.mAllApnSettings;
    }

    private DcAsyncChannel createDataConnection() {
        log("createDataConnection E");
        int id = this.mUniqueIdGenerator.getAndIncrement();
        DataConnection conn = DataConnection.makeDataConnection(this.mPhone, id, this, this.mDcTesterFailBringUpAll, this.mDcc);
        this.mDataConnections.put(Integer.valueOf(id), conn);
        DcAsyncChannel dcac = new DcAsyncChannel(conn, LOG_TAG);
        int status = dcac.fullyConnectSync(this.mPhone.getContext(), this, conn.getHandler());
        if (status == 0) {
            this.mDataConnectionAcHashMap.put(Integer.valueOf(dcac.getDataConnectionIdSync()), dcac);
        } else {
            loge("createDataConnection: Could not connect to dcac=" + dcac + " status=" + status);
        }
        log("createDataConnection() X id=" + id + " dc=" + conn);
        return dcac;
    }

    private void destroyDataConnections() {
        if (this.mDataConnections != null) {
            log("destroyDataConnections: clear mDataConnectionList");
            this.mDataConnections.clear();
            return;
        }
        log("destroyDataConnections: mDataConnecitonList is empty, ignore");
    }

    /* JADX WARNING: Missing block: B:64:0x02d7, code:
            if (r21.mHwCustDcTracker.apnRoamingAdjust(r21, r21.mPreferredApn, r21.mPhone) != false) goto L_0x02d9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<ApnSetting> buildWaitingApns(String requestedApnType, int radioTech) {
        boolean usePreferred;
        log("buildWaitingApns: E requestedApnType=" + requestedApnType);
        ArrayList<ApnSetting> apnList = new ArrayList();
        if (requestedApnType.equals("dun")) {
            ApnSetting dun = fetchDunApn();
            if (dun != null) {
                apnList.add(dun);
                log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList);
                return apnList;
            }
        }
        if (isBipApnType(requestedApnType)) {
            ApnSetting bip = fetchBipApn(this.mPreferredApn, this.mAllApnSettings);
            if (bip != null) {
                apnList.add(bip);
                log("buildWaitingApns: X added APN_TYPE_BIP apnList=" + apnList);
                return apnList;
            }
        }
        if (CT_SUPL_FEATURE_ENABLE && "supl".equals(requestedApnType) && isCTSimCard(this.mPhone.getSubId())) {
            ArrayList<ApnSetting> suplApnList = buildWaitingApnsForCTSupl(requestedApnType, radioTech);
            if (!suplApnList.isEmpty()) {
                return suplApnList;
            }
        }
        String operator = getCTOperator(getOperatorNumeric());
        try {
            usePreferred = this.mPhone.getContext().getResources().getBoolean(17956928) ^ 1;
        } catch (NotFoundException e) {
            log("buildWaitingApns: usePreferred NotFoundException set to true");
            usePreferred = true;
        }
        if (usePreferred) {
            this.mPreferredApn = getPreferredApn();
        }
        log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech);
        boolean isApnCanHandleType = true;
        if (this.mHwCustDcTracker != null) {
            isApnCanHandleType = this.mHwCustDcTracker.isCanHandleType(this.mPreferredApn, requestedApnType);
        }
        boolean isNeedFilterVowifiMmsForPrefApn = isNeedFilterVowifiMms(this.mPreferredApn, requestedApnType);
        log("buildWaitingApns: isNeedFilterVowifiMmsForPrefApn = " + isNeedFilterVowifiMmsForPrefApn);
        if (usePreferred && this.mCanSetPreferApn && this.mPreferredApn != null && this.mPreferredApn.canHandleType(requestedApnType) && isApnCanHandleType && (isNeedFilterVowifiMmsForPrefApn ^ 1) != 0) {
            log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.numeric + ":" + this.mPreferredApn);
            if (this.mPreferredApn.numeric == null || !this.mPreferredApn.numeric.equals(operator)) {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            } else {
                if (isCTSimCard(this.mPhone.getPhoneId()) && isLTENetwork()) {
                    if (isApnPreset(this.mPreferredApn)) {
                        if (this.mPreferredApn.bearer == 13 || this.mPreferredApn.bearer == 14) {
                            apnList.add(this.mPreferredApn);
                            return apnList;
                        }
                    }
                }
                if (ServiceState.bitmaskHasTech(this.mPreferredApn.bearerBitmask, radioTech)) {
                    if (this.mHwCustDcTracker != null) {
                    }
                    apnList.add(this.mPreferredApn);
                    log("buildWaitingApns: X added preferred apnList=" + apnList);
                    return apnList;
                }
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            }
        }
        String operatorCT = this.mPhone.getServiceState().getOperatorNumeric();
        if (operatorCT != null && ("".equals(operatorCT) ^ 1) != 0) {
            if (!"46003".equals(operatorCT) && !"46011".equals(operatorCT)) {
                boolean equals = "46012".equals(operatorCT);
            }
        }
        if (this.mAllApnSettings == null || (this.mAllApnSettings.isEmpty() ^ 1) == 0) {
            loge("mAllApnSettings is null!");
        } else {
            log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
            for (ApnSetting apn : this.mAllApnSettings) {
                isApnCanHandleType = true;
                if (this.mHwCustDcTracker != null) {
                    isApnCanHandleType = this.mHwCustDcTracker.isCanHandleType(apn, requestedApnType);
                }
                boolean isNeedFilterVowifiMms = isNeedFilterVowifiMms(apn, requestedApnType);
                log("buildWaitingApns: isNeedFilterVowifiMms = " + isNeedFilterVowifiMms);
                if (!apn.canHandleType(requestedApnType) || !isApnCanHandleType || (isNeedFilterVowifiMms ^ 1) == 0) {
                    log("buildWaitingApns: couldn't handle requested ApnType=" + requestedApnType);
                } else if (isCTSimCard(this.mPhone.getPhoneId()) && isLTENetwork()) {
                    if (apn.bearer == 13 || apn.bearer == 14) {
                        log("buildWaitingApns: adding apn=" + apn.toString());
                        apnList.add(apn);
                    }
                } else if (ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech)) {
                    if (this.mHwCustDcTracker != null) {
                        if (!this.mHwCustDcTracker.apnRoamingAdjust(this, apn, this.mPhone)) {
                        }
                    }
                    log("buildWaitingApns: adding apn=" + apn);
                    apnList.add(apn);
                } else {
                    log("buildWaitingApns: bearerBitmask:" + apn.bearerBitmask + " does " + "not include radioTech:" + radioTech);
                }
            }
        }
        log("buildWaitingApns: " + apnList.size() + " APNs in the list: " + apnList);
        return apnList;
    }

    private String apnListToString(ArrayList<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        if (apns == null) {
            return null;
        }
        int size = apns.size();
        for (int i = 0; i < size; i++) {
            result.append('[').append(((ApnSetting) apns.get(i)).toString()).append(']');
        }
        return result.toString();
    }

    private void setPreferredApn(int pos) {
        if (this.mCanSetPreferApn) {
            Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
            log("setPreferredApn: delete");
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            resolver.delete(uri, null, null);
            if (pos >= 0) {
                log("setPreferredApn: insert");
                ContentValues values = new ContentValues();
                values.put(APN_ID, Integer.valueOf(pos));
                resolver.insert(uri, values);
            }
            return;
        }
        log("setPreferredApn: X !canSEtPreferApn");
    }

    private ApnSetting getPreferredApn() {
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            log("getPreferredApn: mAllApnSettings is " + (this.mAllApnSettings == null ? "null" : "empty"));
            return null;
        } else if (needRemovedPreferredApn()) {
            return null;
        } else {
            int count;
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId())), new String[]{HbpcdLookup.ID, "name", "apn"}, null, null, "name ASC");
            if (cursor != null) {
                this.mCanSetPreferApn = true;
            } else {
                this.mCanSetPreferApn = false;
            }
            StringBuilder append = new StringBuilder().append("getPreferredApn: mRequestedApnType=").append(this.mRequestedApnType).append(" cursor=").append(cursor).append(" cursor.count=");
            if (cursor != null) {
                count = cursor.getCount();
            } else {
                count = 0;
            }
            log(append.append(count).toString());
            if (this.mCanSetPreferApn && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int pos = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
                for (ApnSetting p : this.mAllApnSettings) {
                    log("getPreferredApn: apnSetting=" + p);
                    if (p.id == pos && p.canHandleType(this.mRequestedApnType)) {
                        log("getPreferredApn: X found apnSetting" + p);
                        cursor.close();
                        return p;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            log("getPreferredApn: X not found");
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:12:0x006d, code:
            if (r18.isSoftBankCard(r27.mPhone) == false) goto L_0x006f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        log("handleMessage msg=" + msg);
        beforeHandleMessage(msg);
        ApnContext apnContext;
        boolean enabled;
        Bundle bundle;
        switch (msg.what) {
            case 69636:
                log("DISCONNECTED_CONNECTED: msg=" + msg);
                DcAsyncChannel dcac = msg.obj;
                this.mDataConnectionAcHashMap.remove(Integer.valueOf(dcac.getDataConnectionIdSync()));
                dcac.disconnected();
                break;
            case 270336:
                onDataSetupComplete((AsyncResult) msg.obj);
                break;
            case 270337:
                break;
            case 270338:
                if (!hasSetCustDataFeature()) {
                    log("msg.what=" + msg.what + ", hasSetCustDataFeature = false");
                    setCustDataEnableByHplmn();
                }
                HwTelephonyFactory.getHwDataServiceChrManager().setReceivedSimloadedMsg(this.mPhone, true, this.mApnContexts, this.mDataEnabledSettings.isUserDataEnabled());
                int subId = this.mPhone.getSubId();
                if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                    log("Ignoring EVENT_RECORDS_LOADED as subId is not valid: " + subId);
                    break;
                } else {
                    onRecordsLoadedOrSubIdChanged();
                    break;
                }
            case 270339:
                if (!(msg.obj instanceof ApnContext)) {
                    if (!(msg.obj instanceof String)) {
                        loge("EVENT_TRY_SETUP request w/o apnContext or String");
                        break;
                    } else {
                        onTrySetupData((String) msg.obj);
                        break;
                    }
                }
                onTrySetupData((ApnContext) msg.obj);
                break;
            case 270340:
                break;
            case 270342:
                onRadioOffOrNotAvailable();
                break;
            case 270343:
                onVoiceCallStarted();
                break;
            case 270344:
                onVoiceCallEnded();
                if (mWcdmaVpEnabled) {
                    this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(true);
                    break;
                }
                break;
            case 270345:
                onDataConnectionDetached();
                break;
            case 270347:
                onDataRoamingOnOrSettingsChanged();
                break;
            case 270348:
                onDataRoamingOff();
                break;
            case 270349:
                onEnableApn(msg.arg1, msg.arg2);
                break;
            case 270351:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DONE ");
                onDisconnectDone((AsyncResult) msg.obj);
                break;
            case 270352:
                onDataConnectionAttached();
                break;
            case 270353:
                onDataStallAlarm(msg.arg1);
                break;
            case 270354:
                doRecovery();
                break;
            case 270355:
                if (isCTSimCard(this.mPhone.getPhoneId())) {
                    updateApnId();
                }
                onApnChanged();
                break;
            case 270358:
                log("EVENT_PS_RESTRICT_ENABLED " + this.mIsPsRestricted);
                stopNetStatPoll();
                stopDataStallAlarm();
                this.mIsPsRestricted = true;
                break;
            case 270359:
                log("EVENT_PS_RESTRICT_DISABLED " + this.mIsPsRestricted);
                this.mIsPsRestricted = false;
                if (isConnected()) {
                    startNetStatPoll();
                    startDataStallAlarm(false);
                    break;
                }
                if (this.mState == State.FAILED) {
                    cleanUpAllConnections(false, PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    this.mReregisterOnReconnectFailure = false;
                }
                apnContext = (ApnContext) this.mApnContextsById.get(0);
                if (apnContext != null) {
                    apnContext.setReason(PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    trySetupData(apnContext);
                    break;
                }
                loge("**** Default ApnContext not found ****");
                if (Build.IS_DEBUGGABLE) {
                    throw new RuntimeException("Default ApnContext not found");
                }
                break;
            case 270360:
                boolean tearDown = msg.arg1 != 0;
                log("EVENT_CLEAN_UP_CONNECTION tearDown=" + tearDown);
                if (!(msg.obj instanceof ApnContext)) {
                    onCleanUpConnection(tearDown, msg.arg2, (String) msg.obj);
                    break;
                } else {
                    cleanUpConnection(tearDown, (ApnContext) msg.obj);
                    break;
                }
            case 270362:
                restartRadio();
                break;
            case 270363:
                onSetInternalDataEnabled(msg.arg1 == 1, (Message) msg.obj);
                break;
            case 270364:
                log("EVENT_RESET_DONE");
                onResetDone((AsyncResult) msg.obj);
                break;
            case 270365:
                if (!(msg.obj == null || (msg.obj instanceof String))) {
                    msg.obj = null;
                }
                onCleanUpAllConnections((String) msg.obj);
                break;
            case 270366:
                enabled = msg.arg1 == 1;
                log("CMD_SET_USER_DATA_ENABLE enabled=" + enabled);
                onSetUserDataEnabled(enabled);
                break;
            case 270367:
                boolean met = msg.arg1 == 1;
                log("CMD_SET_DEPENDENCY_MET met=" + met);
                bundle = msg.getData();
                if (bundle != null) {
                    String apnType = (String) bundle.get("apnType");
                    if (apnType != null) {
                        onSetDependencyMet(apnType, met);
                        break;
                    }
                }
                break;
            case 270368:
                onSetPolicyDataEnabled(msg.arg1 == 1);
                break;
            case 270369:
                onUpdateIcc();
                break;
            case 270370:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DC_RETRYING");
                onDisconnectDcRetrying((AsyncResult) msg.obj);
                break;
            case 270371:
                onDataSetupCompleteError((AsyncResult) msg.obj);
                break;
            case 270372:
                sEnableFailFastRefCounter = (msg.arg1 == 1 ? 1 : -1) + sEnableFailFastRefCounter;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA:  sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (sEnableFailFastRefCounter < 0) {
                    loge("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: sEnableFailFastRefCounter:" + sEnableFailFastRefCounter + " < 0");
                    sEnableFailFastRefCounter = 0;
                }
                enabled = sEnableFailFastRefCounter > 0;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: enabled=" + enabled + " sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (this.mFailFast != enabled) {
                    this.mFailFast = enabled;
                    this.mDataStallDetectionEnabled = enabled ^ 1;
                    if (!this.mDataStallDetectionEnabled || getOverallState() != State.CONNECTED || (this.mInVoiceCall && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
                        log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: stop data stall");
                        stopDataStallAlarm();
                        break;
                    }
                    log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: start data stall");
                    stopDataStallAlarm();
                    startDataStallAlarm(false);
                    break;
                }
                break;
            case 270373:
                bundle = msg.getData();
                if (bundle != null) {
                    try {
                        this.mProvisioningUrl = (String) bundle.get("provisioningUrl");
                    } catch (ClassCastException e) {
                        loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url not a string" + e);
                        this.mProvisioningUrl = null;
                    }
                }
                if (!TextUtils.isEmpty(this.mProvisioningUrl)) {
                    loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioningUrl=" + this.mProvisioningUrl);
                    this.mIsProvisioning = true;
                    startProvisioningApnAlarm();
                    break;
                }
                loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url is empty, ignoring");
                this.mIsProvisioning = false;
                this.mProvisioningUrl = null;
                break;
            case 270374:
                boolean isProvApn;
                log("CMD_IS_PROVISIONING_APN");
                Object apnType2 = null;
                try {
                    bundle = msg.getData();
                    if (bundle != null) {
                        apnType2 = (String) bundle.get("apnType");
                    }
                    if (TextUtils.isEmpty(apnType2)) {
                        loge("CMD_IS_PROVISIONING_APN: apnType is empty");
                        isProvApn = false;
                    } else {
                        isProvApn = isProvisioningApn(apnType2);
                    }
                } catch (ClassCastException e2) {
                    loge("CMD_IS_PROVISIONING_APN: NO provisioning url ignoring");
                    isProvApn = false;
                }
                log("CMD_IS_PROVISIONING_APN: ret=" + isProvApn);
                this.mReplyAc.replyToMessage(msg, 270374, isProvApn ? 1 : 0);
                break;
            case 270375:
                log("EVENT_PROVISIONING_APN_ALARM");
                ApnContext apnCtx = (ApnContext) this.mApnContextsById.get(0);
                if (!apnCtx.isProvisioningApn() || !apnCtx.isConnectedOrConnecting()) {
                    log("EVENT_PROVISIONING_APN_ALARM: Not connected ignore");
                    break;
                }
                if (this.mProvisioningApnAlarmTag != msg.arg1) {
                    log("EVENT_PROVISIONING_APN_ALARM: ignore stale tag, mProvisioningApnAlarmTag:" + this.mProvisioningApnAlarmTag + " != arg1:" + msg.arg1);
                    break;
                }
                log("EVENT_PROVISIONING_APN_ALARM: Disconnecting");
                this.mIsProvisioning = false;
                this.mProvisioningUrl = null;
                stopProvisioningApnAlarm();
                sendCleanUpConnection(true, apnCtx);
                break;
                break;
            case 270376:
                if (msg.arg1 != 1) {
                    if (msg.arg1 == 0) {
                        handleStopNetStatPoll((Activity) msg.obj);
                        break;
                    }
                }
                handleStartNetStatPoll((Activity) msg.obj);
                break;
                break;
            case 270377:
                onRatChange();
                if (!onUpdateIcc()) {
                    if (!isCTSimCard(this.mPhone.getPhoneId()) && getAnyDataEnabled()) {
                        for (ApnContext apnContext2 : this.mApnContexts.values()) {
                            if ((DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED == apnContext2.getPdpFailCause() || DcFailCause.NOT_ALLOWED_RADIO_TECHNOLOGY_IWLAN == apnContext2.getPdpFailCause()) && (apnContext2.getState() == State.FAILED || apnContext2.getState() == State.IDLE)) {
                                log("tryRestartDataConnections, which reason is " + apnContext2.getPdpFailCause());
                                apnContext2.setPdpFailCause(DcFailCause.NONE);
                                setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                                break;
                            }
                        }
                        break;
                    }
                }
                log("onUpdateIcc: tryRestartDataConnections nwTypeChanged");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED, RetryFailures.ONLY_ON_CHANGE);
                break;
                break;
            case 270378:
                if (this.mProvisioningSpinner == msg.obj) {
                    this.mProvisioningSpinner.dismiss();
                    this.mProvisioningSpinner = null;
                    break;
                }
                break;
            case 270379:
                onDeviceProvisionedChange();
                break;
            case 270380:
                String url = msg.obj;
                log("dataConnectionTracker.handleMessage: EVENT_REDIRECTION_DETECTED=" + url);
                onDataConnectionRedirected(url);
                break;
            case 270381:
                handlePcoData((AsyncResult) msg.obj);
                break;
            case 270382:
                onSetCarrierDataEnabled((AsyncResult) msg.obj);
                break;
            case 270383:
                onOtaAttachFailed((ApnContext) msg.obj);
                break;
            case 271137:
                this.mPhone.mCi.resetAllConnections();
                break;
            case 271138:
                onDataSetupCompleteFailed();
                break;
            case 271139:
                onPdpResetAlarm(msg.arg1);
                break;
            case 271140:
                if (mWcdmaVpEnabled) {
                    log("EVENT_VP_STATUS_CHANGED");
                    onVpStatusChanged(msg.obj);
                    break;
                }
                break;
            case 271144:
                if (!checkMvnoParams()) {
                    HwDataConnectionManager sHwDataConnectionManager = HwTelephonyFactory.getHwDataConnectionManager();
                    if (sHwDataConnectionManager != null && sHwDataConnectionManager.getNamSwitcherForSoftbank()) {
                        break;
                    }
                }
                break;
            default:
                Rlog.e("DcTracker", "Unhandled event=" + msg);
                break;
        }
        onRadioAvailable();
        handleCustMessage(msg);
    }

    private void onRatChange() {
        if (isCTSimCard(this.mPhone.getPhoneId())) {
            int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
            boolean RatChange = ServiceState.isHrpd1X(dataRadioTech) != ServiceState.isHrpd1X(this.preDataRadioTech);
            boolean SetupRatChange = ServiceState.isHrpd1X(dataRadioTech) != ServiceState.isHrpd1X(this.preSetupBasedRadioTech);
            State overallState = getOverallState();
            boolean isConnected = overallState != State.CONNECTED ? overallState == State.CONNECTING : true;
            log("onRatChange: preDataRadioTech is: " + this.preDataRadioTech + "; dataRadioTech is: " + dataRadioTech);
            log("onRatChange: preSetupBasedRadioTech is: " + this.preSetupBasedRadioTech + "; overallState is: " + overallState);
            if (dataRadioTech != 0) {
                if (this.preDataRadioTech != -1 && RatChange) {
                    if (this.preSetupBasedRadioTech == 0 || SetupRatChange) {
                        if (isConnected) {
                            cleanUpAllConnections(true, PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                        } else {
                            cleanUpAllConnections(false, PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                            updateApnContextState();
                        }
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                    } else {
                        log("setup data call has been trigger by other flow, have no need to execute again.");
                    }
                }
                this.preDataRadioTech = dataRadioTech;
            }
        }
    }

    public void updateApnContextState() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == State.SCANNING) {
                apnContext.setState(State.IDLE);
                apnContext.setDataConnectionAc(null);
                cancelReconnectAlarm(apnContext);
            }
        }
    }

    private int getApnProfileID(String apnType) {
        if (TextUtils.equals(apnType, "ims") || TextUtils.equals(apnType, "xcap")) {
            return 2;
        }
        if (TextUtils.equals(apnType, "fota")) {
            return 3;
        }
        if (TextUtils.equals(apnType, "cbs")) {
            return 4;
        }
        if (!TextUtils.equals(apnType, "ia") && TextUtils.equals(apnType, "dun")) {
            return 1;
        }
        return 0;
    }

    private int getCellLocationId() {
        CellLocation loc = this.mPhone.getCellLocation();
        if (loc == null) {
            return -1;
        }
        if (loc instanceof GsmCellLocation) {
            return ((GsmCellLocation) loc).getCid();
        }
        if (loc instanceof CdmaCellLocation) {
            return ((CdmaCellLocation) loc).getBaseStationId();
        }
        return -1;
    }

    private IccRecords getUiccRecords(int appFamily) {
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.fetchVSimIccRecords(appFamily);
        }
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    private UiccCardApplication getUiccCardApplication(int appFamily) {
        if (this.mPhone == null) {
            return null;
        }
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.getVSimUiccCardApplication(appFamily);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), appFamily);
    }

    private boolean onUpdateIcc() {
        boolean result = false;
        if (this.mUiccController == null) {
            loge("onUpdateIcc: mUiccController is null. Error!");
            return false;
        }
        String name;
        int appFamily = 1;
        if (VSimUtilsInner.isVSimPhone(this.mPhone)) {
            appFamily = 1;
        } else if (this.mPhone.getPhoneType() == 1) {
            appFamily = 1;
        } else if (this.mPhone.getPhoneType() == 2) {
            appFamily = 2;
        } else {
            log("Wrong phone type");
        }
        IccRecords newIccRecords = getUiccRecords(appFamily);
        StringBuilder append = new StringBuilder().append("onUpdateIcc: newIccRecords ");
        if (newIccRecords != null) {
            name = newIccRecords.getClass().getName();
        } else {
            name = null;
        }
        log(append.append(name).toString());
        UiccCardApplication newUiccApplication = getUiccCardApplication(appFamily);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!(this.mUiccApplcation == newUiccApplication && r == newIccRecords)) {
            if (this.mUiccApplcation != null) {
                log("Removing stale icc objects.");
                unregisterForGetAdDone(this.mUiccApplcation);
                if (r != null) {
                    unregisterForImsiReady(r);
                    unregisterForRecordsLoaded(r);
                    this.mIccRecords.set(null);
                }
                this.mUiccApplcation = null;
            }
            if (newUiccApplication == null || newIccRecords == null) {
                onSimNotReady();
            } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                log("New records found");
                this.mUiccApplcation = newUiccApplication;
                this.mIccRecords.set(newIccRecords);
                registerForImsi(newUiccApplication, newIccRecords);
                HwTelephonyFactory.getHwDataServiceChrManager().setRecordsLoadedRegistered(true, this.mPhone.getSubId());
                registerForFdnRecordsLoaded(newIccRecords);
                newIccRecords.registerForRecordsLoaded(this, 270338, null);
            }
            result = true;
        }
        return result;
    }

    public void update() {
        log("update sub = " + this.mPhone.getSubId());
        log("update(): Active DDS, register for all events now!");
        onUpdateIcc();
        updateCustMobileDataFeature();
        this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
        this.mAutoAttachOnCreation.set(false);
        ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(false);
    }

    public void updateForVSim() {
        log("vsim update sub = " + this.mPhone.getSubId());
        unregisterForAllEvents();
        log("update(): Active DDS, register for all events now!");
        registerForAllEvents();
        onUpdateIcc();
        this.mDataEnabledSettings.setUserDataEnabled(true);
    }

    public void cleanUpAllConnections(String cause) {
        cleanUpAllConnections(cause, null);
    }

    public void updateRecords() {
        onUpdateIcc();
    }

    public void cleanUpAllConnections(String cause, Message disconnectAllCompleteMsg) {
        log("cleanUpAllConnections");
        if (disconnectAllCompleteMsg != null) {
            this.mDisconnectAllCompleteMsgList.add(disconnectAllCompleteMsg);
        }
        Message msg = obtainMessage(270365);
        msg.obj = cause;
        sendMessage(msg);
    }

    private void notifyDataDisconnectComplete() {
        log("notifyDataDisconnectComplete");
        for (Message m : this.mDisconnectAllCompleteMsgList) {
            m.sendToTarget();
        }
        this.mDisconnectAllCompleteMsgList.clear();
    }

    private void notifyAllDataDisconnected() {
        sEnableFailFastRefCounter = 0;
        this.mFailFast = false;
        this.mAllDataDisconnectedRegistrants.notifyRegistrants();
    }

    public void registerForAllDataDisconnected(Handler h, int what, Object obj) {
        this.mAllDataDisconnectedRegistrants.addUnique(h, what, obj);
        if (isDisconnected()) {
            log("notify All Data Disconnected");
            notifyAllDataDisconnected();
        }
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mAllDataDisconnectedRegistrants.remove(h);
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDataEnabledSettings.registerForDataEnabledChanged(h, what, obj);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDataEnabledSettings.unregisterForDataEnabledChanged(h);
    }

    private void onSetInternalDataEnabled(boolean enabled, Message onCompleteMsg) {
        synchronized (this.mDataEnabledSettings) {
            log("onSetInternalDataEnabled: enabled=" + enabled);
            boolean sendOnComplete = true;
            this.mDataEnabledSettings.setInternalDataEnabled(enabled);
            if (enabled) {
                log("onSetInternalDataEnabled: changed to enabled, try to setup data call");
                onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            } else {
                sendOnComplete = false;
                log("onSetInternalDataEnabled: changed to disabled, cleanUpAllConnections");
                cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED, onCompleteMsg);
            }
            if (sendOnComplete && onCompleteMsg != null) {
                onCompleteMsg.sendToTarget();
            }
        }
    }

    public boolean setInternalDataEnabledFlag(boolean enable) {
        log("setInternalDataEnabledFlag(" + enable + ")");
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString() + "\n");
        }
        log(sb.toString());
        this.mDataEnabledSettings.setInternalDataEnabled(enable);
        return true;
    }

    public boolean setInternalDataEnabled(boolean enable) {
        return setInternalDataEnabled(enable, null);
    }

    public boolean setInternalDataEnabled(boolean enable, Message onCompleteMsg) {
        log("setInternalDataEnabled(" + enable + ")");
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString() + "\n");
        }
        log(sb.toString());
        Message msg = obtainMessage(270363, onCompleteMsg);
        msg.arg1 = enable ? 1 : 0;
        sendMessage(msg);
        return true;
    }

    public void setDataAllowed(boolean enable, Message response) {
        log("setDataAllowed: enable=" + enable);
        this.isCleanupRequired.set(enable ^ 1);
        this.mPhone.mCi.setDataAllowed(enable, response);
        this.mDataEnabledSettings.setInternalDataEnabled(enable);
    }

    protected boolean isDefaultDataSubscription() {
        boolean z = true;
        long subId = (long) this.mPhone.getSubId();
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            return true;
        }
        long defaultDds = (long) SubscriptionController.getInstance().getDefaultDataSubId();
        log("isDefaultDataSubscription subId: " + subId + "defaultDds: " + defaultDds);
        if (subId != defaultDds) {
            z = false;
        }
        return z;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("DcTracker:");
        pw.println(" RADIO_TESTS=false");
        pw.println(" isInternalDataEnabled=" + this.mDataEnabledSettings.isInternalDataEnabled());
        pw.println(" isUserDataEnabled=" + this.mDataEnabledSettings.isUserDataEnabled());
        pw.println(" isPolicyDataEnabled=" + this.mDataEnabledSettings.isPolicyDataEnabled());
        pw.flush();
        pw.println(" mRequestedApnType=" + this.mRequestedApnType);
        pw.println(" mPhone=" + this.mPhone.getPhoneName());
        pw.println(" mActivity=" + this.mActivity);
        pw.println(" mState=" + this.mState);
        pw.println(" mTxPkts=" + this.mTxPkts);
        pw.println(" mRxPkts=" + this.mRxPkts);
        pw.println(" mNetStatPollPeriod=" + this.mNetStatPollPeriod);
        pw.println(" mNetStatPollEnabled=" + this.mNetStatPollEnabled);
        pw.println(" mDataStallTxRxSum=" + this.mDataStallTxRxSum);
        pw.println(" mDataStallAlarmTag=" + this.mDataStallAlarmTag);
        pw.println(" mDataStallDetectionEnabled=" + this.mDataStallDetectionEnabled);
        pw.println(" mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        pw.println(" mNoRecvPollCount=" + this.mNoRecvPollCount);
        pw.println(" mResolver=" + this.mResolver);
        pw.println(" mIsWifiConnected=" + this.mIsWifiConnected);
        pw.println(" mReconnectIntent=" + this.mReconnectIntent);
        pw.println(" mAutoAttachOnCreation=" + this.mAutoAttachOnCreation.get());
        pw.println(" mIsScreenOn=" + this.mIsScreenOn);
        pw.println(" mUniqueIdGenerator=" + this.mUniqueIdGenerator);
        pw.flush();
        pw.println(" ***************************************");
        DcController dcc = this.mDcc;
        if (dcc != null) {
            dcc.dump(fd, pw, args);
        } else {
            pw.println(" mDcc=null");
        }
        pw.println(" ***************************************");
        if (this.mDataConnections != null) {
            Set<Entry<Integer, DataConnection>> mDcSet = this.mDataConnections.entrySet();
            pw.println(" mDataConnections: count=" + mDcSet.size());
            for (Entry<Integer, DataConnection> entry : mDcSet) {
                pw.printf(" *** mDataConnection[%d] \n", new Object[]{entry.getKey()});
                ((DataConnection) entry.getValue()).dump(fd, pw, args);
            }
        } else {
            pw.println("mDataConnections=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        HashMap<String, Integer> apnToDcId = this.mApnToDataConnectionId;
        if (apnToDcId != null) {
            Set<Entry<String, Integer>> apnToDcIdSet = apnToDcId.entrySet();
            pw.println(" mApnToDataConnectonId size=" + apnToDcIdSet.size());
            for (Entry<String, Integer> entry2 : apnToDcIdSet) {
                pw.printf(" mApnToDataConnectonId[%s]=%d\n", new Object[]{entry2.getKey(), entry2.getValue()});
            }
        } else {
            pw.println("mApnToDataConnectionId=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        ConcurrentHashMap<String, ApnContext> apnCtxs = this.mApnContexts;
        if (apnCtxs != null) {
            Set<Entry<String, ApnContext>> apnCtxsSet = apnCtxs.entrySet();
            pw.println(" mApnContexts size=" + apnCtxsSet.size());
            for (Entry<String, ApnContext> entry3 : apnCtxsSet) {
                ((ApnContext) entry3.getValue()).dump(fd, pw, args);
            }
            pw.println(" ***************************************");
        } else {
            pw.println(" mApnContexts=null");
        }
        pw.flush();
        ArrayList<ApnSetting> apnSettings = this.mAllApnSettings;
        if (apnSettings != null) {
            pw.println(" mAllApnSettings size=" + apnSettings.size());
            for (int i = 0; i < apnSettings.size(); i++) {
                pw.printf(" mAllApnSettings[%d]: %s\n", new Object[]{Integer.valueOf(i), apnSettings.get(i)});
            }
            pw.flush();
        } else {
            pw.println(" mAllApnSettings=null");
        }
        pw.println(" mPreferredApn=" + this.mPreferredApn);
        pw.println(" mIsPsRestricted=" + this.mIsPsRestricted);
        pw.println(" mIsDisposed=" + this.mIsDisposed);
        pw.println(" mIntentReceiver=" + this.mIntentReceiver);
        pw.println(" mReregisterOnReconnectFailure=" + this.mReregisterOnReconnectFailure);
        pw.println(" canSetPreferApn=" + this.mCanSetPreferApn);
        pw.println(" mApnObserver=" + this.mApnObserver);
        pw.println(" getOverallState=" + getOverallState());
        pw.println(" mDataConnectionAsyncChannels=%s\n" + this.mDataConnectionAcHashMap);
        pw.println(" mAttached=" + this.mAttached.get());
        pw.flush();
    }

    public String[] getPcscfAddress(String apnType) {
        log("getPcscfAddress()");
        if (apnType == null) {
            log("apnType is null, return null");
            return null;
        }
        ApnContext apnContext;
        if (TextUtils.equals(apnType, "emergency")) {
            apnContext = (ApnContext) this.mApnContextsById.get(9);
        } else if (TextUtils.equals(apnType, "ims")) {
            apnContext = (ApnContext) this.mApnContextsById.get(5);
        } else {
            log("apnType is invalid, return null");
            return null;
        }
        if (apnContext == null) {
            log("apnContext is null, return null");
            return null;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        if (dcac == null) {
            return null;
        }
        String[] result = dcac.getPcscfAddr();
        for (int i = 0; i < result.length; i++) {
            log("Pcscf[" + i + "]: " + result[i]);
        }
        return result;
    }

    private void initEmergencyApnSetting() {
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, "type=\"emergency\"", null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                this.mEmergencyApn = makeApnSetting(cursor);
            }
            cursor.close();
        }
    }

    private void addEmergencyApnSetting() {
        if (!this.mEmergencyApnLoaded) {
            initEmergencyApnSetting();
            this.mEmergencyApnLoaded = true;
        }
        if (this.mEmergencyApn == null) {
            return;
        }
        if (this.mAllApnSettings == null) {
            this.mAllApnSettings = new ArrayList();
            return;
        }
        boolean hasEmergencyApn = false;
        for (ApnSetting apn : this.mAllApnSettings) {
            if (ArrayUtils.contains(apn.types, "emergency")) {
                hasEmergencyApn = true;
                break;
            }
        }
        if (hasEmergencyApn) {
            log("addEmergencyApnSetting - E-APN setting is already present");
        } else {
            this.mAllApnSettings.add(this.mEmergencyApn);
        }
    }

    protected void onDataSetupCompleteFailed() {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get("default");
        long currentTimeMillis = System.currentTimeMillis();
        TelephonyManager tm = TelephonyManager.getDefault();
        if (this.mPhone.getServiceState().getVoiceRegState() != 0 || (this.mAttached.get() ^ 1) == 0) {
            log("onDataSetupCompleteFailed, cs out of service || ps in service!");
            return;
        }
        log("onDataSetupCompleteFailed, cs in service & ps out of service!");
        if (apnContext.isReady() && this.mDataEnabledSettings.isInternalDataEnabled() && this.mDataEnabledSettings.isUserDataEnabled()) {
            this.mPdpActFailCount++;
            if (1 == this.mPdpActFailCount) {
                this.mFirstPdpActFailTimestamp = currentTimeMillis;
            }
            log("onDataSetupCompleteFailed, mFirstPdpActFailTimestamp " + this.mFirstPdpActFailTimestamp + ", currentTimeMillis " + currentTimeMillis + ", mAttached " + this.mAttached + ", mRestartRildEnabled " + this.mRestartRildEnabled + ", mPdpActFailCount " + this.mPdpActFailCount);
            if (3 <= this.mPdpActFailCount && currentTimeMillis - this.mFirstPdpActFailTimestamp >= ACTIVE_PDP_FAIL_TO_RESTART_RILD_MILLIS && tm.getCallState(0) == 0 && tm.getCallState(1) == 0 && this.mRestartRildEnabled) {
                this.mPhone.mCi.restartRild(null);
                this.mRestartRildEnabled = false;
            }
        }
    }

    protected void clearRestartRildParam() {
        log("clearRestartRildParam");
        this.mFirstPdpActFailTimestamp = 0;
        this.mPdpActFailCount = 0;
        this.mRestartRildEnabled = true;
    }

    private void cleanUpConnectionsOnUpdatedApns(boolean tearDown) {
        log("cleanUpConnectionsOnUpdatedApns: tearDown=" + tearDown);
        if (this.mAllApnSettings.isEmpty()) {
            cleanUpAllConnections(tearDown, PhoneInternalInterface.REASON_APN_CHANGED);
        } else {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                log("cleanUpConnectionsOnUpdatedApns for " + apnContext);
                boolean cleanUpApn = true;
                ArrayList<ApnSetting> currentWaitingApns = apnContext.getWaitingApns();
                if (currentWaitingApns != null && (apnContext.isDisconnected() ^ 1) != 0) {
                    ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), this.mPhone.getServiceState().getRilDataRadioTechnology());
                    log("new waitingApns:" + waitingApns);
                    if (waitingApns.size() == currentWaitingApns.size()) {
                        cleanUpApn = false;
                        for (int i = 0; i < waitingApns.size(); i++) {
                            if (!((ApnSetting) currentWaitingApns.get(i)).equals(waitingApns.get(i))) {
                                log("new waiting apn is different at " + i);
                                cleanUpApn = true;
                                apnContext.setWaitingApns(waitingApns);
                                break;
                            }
                        }
                    }
                }
                if (cleanUpApn) {
                    apnContext.setReason(PhoneInternalInterface.REASON_APN_CHANGED);
                    cleanUpConnection(true, apnContext);
                }
            }
        }
        if (!isConnected()) {
            stopNetStatPoll();
            stopDataStallAlarm();
        }
        this.mRequestedApnType = "default";
        log("mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
    }

    private void resetPollStats() {
        this.mTxPkts = -1;
        this.mRxPkts = -1;
        this.mNetStatPollPeriod = 1000;
    }

    private void startNetStatPoll() {
        if (getOverallState() == State.CONNECTED && !this.mNetStatPollEnabled) {
            log("startNetStatPoll");
            resetPollStats();
            this.mNetStatPollEnabled = true;
            this.mPollNetStat.run();
        }
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    private void stopNetStatPoll() {
        this.mNetStatPollEnabled = false;
        removeCallbacks(this.mPollNetStat);
        log("stopNetStatPoll");
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    public void sendStartNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 1;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStartNetStatPoll(Activity activity) {
        startNetStatPoll();
        startDataStallAlarm(false);
        setActivity(activity);
    }

    public void sendStopNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 0;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStopNetStatPoll(Activity activity) {
        stopNetStatPoll();
        stopDataStallAlarm();
        setActivity(activity);
    }

    private void updateDataActivity() {
        TxRxSum preTxRxSum = new TxRxSum(this.mTxPkts, this.mRxPkts);
        TxRxSum curTxRxSum = new TxRxSum();
        curTxRxSum.updateThisModemMobileTxRxSum(mIfacePhoneHashMap, this.mPhone.getPhoneId());
        this.mTxPkts = curTxRxSum.txPkts;
        this.mRxPkts = curTxRxSum.rxPkts;
        log("updateDataActivity: curTxRxSum=" + curTxRxSum + " preTxRxSum=" + preTxRxSum);
        if (!this.mNetStatPollEnabled) {
            return;
        }
        if (preTxRxSum.txPkts > 0 || preTxRxSum.rxPkts > 0) {
            long sent = this.mTxPkts - preTxRxSum.txPkts;
            long received = this.mRxPkts - preTxRxSum.rxPkts;
            log("updateDataActivity: sent=" + sent + " received=" + received);
            Activity newActivity = (sent <= 0 || received <= 0) ? (sent <= 0 || received != 0) ? (sent != 0 || received <= 0) ? this.mActivity == Activity.DORMANT ? this.mActivity : Activity.NONE : Activity.DATAIN : Activity.DATAOUT : Activity.DATAINANDOUT;
            if (this.mActivity != newActivity && this.mIsScreenOn) {
                log("updateDataActivity: newActivity=" + newActivity);
                this.mActivity = newActivity;
                this.mPhone.notifyDataActivity();
            }
        }
    }

    private void handlePcoData(AsyncResult ar) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "PCO_DATA exception: " + ar.exception);
            return;
        }
        PcoData pcoData = ar.result;
        ArrayList<DataConnection> dcList = new ArrayList();
        DataConnection temp = this.mDcc.getActiveDcByCid(pcoData.cid);
        if (temp != null) {
            dcList.add(temp);
        }
        if (dcList.size() == 0) {
            Rlog.e(LOG_TAG, "PCO_DATA for unknown cid: " + pcoData.cid + ", inferring");
            for (DataConnection dc : this.mDataConnections.values()) {
                int cid = dc.getCid();
                if (cid == pcoData.cid) {
                    Rlog.d(LOG_TAG, "  found " + dc);
                    dcList.clear();
                    dcList.add(dc);
                    break;
                } else if (cid == -1) {
                    for (ApnContext apnContext : dc.mApnContexts.keySet()) {
                        if (apnContext.getState() == State.CONNECTING) {
                            Rlog.d(LOG_TAG, "  found potential " + dc);
                            dcList.add(dc);
                            break;
                        }
                    }
                }
            }
        }
        if (dcList.size() == 0) {
            Rlog.e(LOG_TAG, "PCO_DATA - couldn't infer cid");
            return;
        }
        for (DataConnection dc2 : dcList) {
            if (dc2.mApnContexts.size() == 0) {
                break;
            }
            for (ApnContext apnContext2 : dc2.mApnContexts.keySet()) {
                String apnType = apnContext2.getApnType();
                Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                intent.putExtra("apnType", apnType);
                intent.putExtra("apnProto", pcoData.bearerProto);
                intent.putExtra("pcoId", pcoData.pcoId);
                intent.putExtra("pcoValue", pcoData.contents);
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            }
        }
    }

    private int getRecoveryAction() {
        return System.getInt(this.mResolver, "radio.data.stall.recovery.action", 0);
    }

    protected void putRecoveryAction(int action) {
        System.putInt(this.mResolver, "radio.data.stall.recovery.action", action);
    }

    private void doRecovery() {
        if (getOverallState() == State.CONNECTED) {
            int recoveryAction = getRecoveryAction();
            TelephonyMetrics.getInstance().writeDataStallEvent(this.mPhone.getPhoneId(), recoveryAction);
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDorecovery(this.mPhone, recoveryAction);
            switch (recoveryAction) {
                case 0:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, this.mSentSinceLastRecv);
                    log("doRecovery() get data call list");
                    if (this.mDcc != null) {
                        this.mDcc.getDataCallList();
                    }
                    if (!noNeedDoRecovery(this.mApnContexts)) {
                        log("Since this apn is preseted apn, so we need to do recovery.");
                        putRecoveryAction(1);
                        break;
                    }
                    putRecoveryAction(0);
                    log("This apn is not preseted apn or we set nodorecovery to fobid do recovery, so we needn't to do recovery.");
                    break;
                case 1:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_CLEANUP, this.mSentSinceLastRecv);
                    log("doRecovery() cleanup all connections");
                    cleanUpAllConnections(PhoneInternalInterface.REASON_PDP_RESET);
                    putRecoveryAction(2);
                    break;
                case 2:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_REREGISTER, this.mSentSinceLastRecv);
                    log("doRecovery() re-register");
                    this.mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    putRecoveryAction(3);
                    break;
                case 3:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART, this.mSentSinceLastRecv);
                    log("restarting radio");
                    putRecoveryAction(4);
                    this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(true);
                    restartRadio();
                    break;
                case 4:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART_WITH_PROP, -1);
                    log("restarting radio with gsm.radioreset to true");
                    SystemProperties.set(this.RADIO_RESET_PROPERTY, "true");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    restartRadio();
                    putRecoveryAction(0);
                    break;
                default:
                    throw new RuntimeException("doRecovery: Invalid recoveryAction=" + recoveryAction);
            }
            this.mSentSinceLastRecv = 0;
        }
    }

    private void updateDataStallInfo() {
        TxRxSum preTxRxSum = new TxRxSum(this.mDataStallTxRxSum);
        if (enableTcpUdpSumForDataStall()) {
            this.mDataStallTxRxSum.updateThisModemMobileTxRxSum(mIfacePhoneHashMap, this.mPhone.getPhoneId());
        } else {
            this.mDataStallTxRxSum.updateTxRxSum();
            long[] dnsTxRx = getDnsPacketTxRxSum();
            TxRxSum txRxSum = this.mDataStallTxRxSum;
            txRxSum.txPkts += dnsTxRx[0];
            txRxSum = this.mDataStallTxRxSum;
            txRxSum.rxPkts += dnsTxRx[1];
            log("updateDataStallInfo: getDnsPacketTxRxSum dnsTx=" + dnsTxRx[0] + " dnsRx=" + dnsTxRx[1]);
        }
        log("updateDataStallInfo: mDataStallTxRxSum=" + this.mDataStallTxRxSum + " preTxRxSum=" + preTxRxSum);
        long sent = this.mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        long received = this.mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;
        if (sent > 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(0);
        } else if (sent > 0 && received == 0) {
            if (isPhoneStateIdle()) {
                this.mSentSinceLastRecv += sent;
            } else {
                this.mSentSinceLastRecv = 0;
            }
            log("updateDataStallInfo: OUT sent=" + sent + " mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        } else if (sent == 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(0);
        }
    }

    private boolean isPhoneStateIdle() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        int i = 0;
        while (i < phoneCount) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone == null || phone.getState() == PhoneConstants.State.IDLE) {
                i++;
            } else {
                log("isPhoneStateIdle: Voice call active on sub: " + i);
                return false;
            }
        }
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhone.isBusy()) {
            return true;
        }
        log("isPhoneStateIdle: ImsPhone isBusy true");
        return false;
    }

    private void onDataStallAlarm(int tag) {
        if (this.mDataStallAlarmTag != tag) {
            log("onDataStallAlarm: ignore, tag=" + tag + " expecting " + this.mDataStallAlarmTag);
            return;
        }
        updateDataStallInfo();
        boolean suspectedStall = false;
        if (this.mSentSinceLastRecv >= ((long) Global.getInt(this.mResolver, "pdp_watchdog_trigger_packet_count", 10))) {
            if (isPingOk()) {
                this.mSentSinceLastRecv = 0;
            } else {
                log("onDataStallAlarm: tag=" + tag + " do recovery action=" + getRecoveryAction());
                suspectedStall = true;
                sendMessage(obtainMessage(270354));
            }
        }
        startDataStallAlarm(suspectedStall);
    }

    private void startDataStallAlarm(boolean suspectedStall) {
        int nextAction = getRecoveryAction();
        if (this.mDataStallDetectionEnabled && getOverallState() == State.CONNECTED) {
            int delayInMs;
            if (this.mIsScreenOn || suspectedStall || RecoveryAction.isAggressiveRecovery(nextAction)) {
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_aggressive_delay_in_ms", 60000);
            } else {
                if (SystemProperties.getBoolean("ro.config.power", false)) {
                    DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 6000000;
                }
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_non_aggressive_delay_in_ms", DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            }
            this.mDataStallAlarmTag++;
            Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
            intent.addFlags(268435456);
            intent.putExtra(DATA_STALL_ALARM_TAG_EXTRA, this.mDataStallAlarmTag);
            this.mDataStallAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
            this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mDataStallAlarmIntent);
        }
    }

    private void stopDataStallAlarm() {
        this.mDataStallAlarmTag++;
        if (this.mDataStallAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mDataStallAlarmIntent);
            this.mDataStallAlarmIntent = null;
        }
    }

    private void restartDataStallAlarm() {
        if (!isConnected()) {
            return;
        }
        if (RecoveryAction.isAggressiveRecovery(getRecoveryAction())) {
            log("restartDataStallAlarm: action is pending. not resetting the alarm.");
            return;
        }
        stopDataStallAlarm();
        startDataStallAlarm(false);
    }

    boolean isSupportLTE(ApnSetting apnSettings) {
        if (((apnSettings.bearer == 13 || apnSettings.bearer == 14) && isApnPreset(apnSettings)) || (isApnPreset(apnSettings) ^ 1) != 0) {
            return true;
        }
        return false;
    }

    protected boolean isCTCardForFullNet() {
        if (isFullNetworkSupported()) {
            return isCTSimCard(this.mPhone.getPhoneId());
        }
        return false;
    }

    private void onActionIntentProvisioningApnAlarm(Intent intent) {
        log("onActionIntentProvisioningApnAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(270375, intent.getAction());
        msg.arg1 = intent.getIntExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    private void startProvisioningApnAlarm() {
        int delayInMs = Global.getInt(this.mResolver, "provisioning_apn_alarm_delay_in_ms", PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT);
        if (Build.IS_DEBUGGABLE) {
            try {
                delayInMs = Integer.parseInt(System.getProperty(DEBUG_PROV_APN_ALARM, Integer.toString(delayInMs)));
            } catch (NumberFormatException e) {
                loge("startProvisioningApnAlarm: e=" + e);
            }
        }
        this.mProvisioningApnAlarmTag++;
        log("startProvisioningApnAlarm: tag=" + this.mProvisioningApnAlarmTag + " delay=" + (delayInMs / 1000) + "s");
        Intent intent = new Intent(INTENT_PROVISIONING_APN_ALARM);
        intent.addFlags(268435456);
        intent.putExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, this.mProvisioningApnAlarmTag);
        this.mProvisioningApnAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mProvisioningApnAlarmIntent);
    }

    private void stopProvisioningApnAlarm() {
        log("stopProvisioningApnAlarm: current tag=" + this.mProvisioningApnAlarmTag + " mProvsioningApnAlarmIntent=" + this.mProvisioningApnAlarmIntent);
        this.mProvisioningApnAlarmTag++;
        if (this.mProvisioningApnAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mProvisioningApnAlarmIntent);
            this.mProvisioningApnAlarmIntent = null;
        }
    }

    private ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String requestedApnType, int radioTech) {
        ArrayList<ApnSetting> apnList = new ArrayList();
        if (!(this.mAllApnSettings == null || (this.mAllApnSettings.isEmpty() ^ 1) == 0)) {
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.canHandleType(requestedApnType) && ((!isLTENetwork() && ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech)) || (isLTENetwork() && (apn.bearer == 13 || apn.bearer == 14)))) {
                    if (!(TelephonyManager.getDefault().isNetworkRoaming(this.mPhone.getSubId()) && "ctnet".equals(apn.apn)) && (TelephonyManager.getDefault().isNetworkRoaming(this.mPhone.getSubId()) || !"ctwap".equals(apn.apn))) {
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

    protected void onActionIntentPdpResetAlarm(Intent intent) {
        log("onActionIntentPdpResetAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(271139, intent.getAction());
        msg.arg1 = intent.getIntExtra(PDP_RESET_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    protected void onPdpResetAlarm(int tag) {
        if (this.mPdpResetAlarmTag != tag) {
            log("onPdpRestAlarm: ignore, tag=" + tag + " expecting " + this.mPdpResetAlarmTag);
        } else {
            cleanUpAllConnections(PhoneInternalInterface.REASON_PDP_RESET);
        }
    }

    protected void startPdpResetAlarm(int delay) {
        this.mPdpResetAlarmTag++;
        log("startPdpResetAlarm: tag=" + this.mPdpResetAlarmTag + " delay=" + (delay / 1000) + "s");
        Intent intent = new Intent(INTENT_PDP_RESET_ALARM);
        intent.putExtra(PDP_RESET_ALARM_TAG_EXTRA, this.mPdpResetAlarmTag);
        log("startPdpResetAlarm: delay=" + delay + " action=" + intent.getAction());
        this.mPdpResetAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delay), this.mPdpResetAlarmIntent);
    }

    protected void stopPdpResetAlarm() {
        log("stopPdpResetAlarm: current tag=" + this.mPdpResetAlarmTag + " mPdpResetAlarmIntent=" + this.mPdpResetAlarmIntent);
        this.mPdpResetAlarmTag++;
        if (this.mPdpResetAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mPdpResetAlarmIntent);
            this.mPdpResetAlarmIntent = null;
        }
    }

    void sendDataSetupCompleteFailed() {
        log("sendDataSetupCompleteFailed:");
        sendMessage(obtainMessage(271138));
    }

    private boolean isBlockSetInitialAttachApn() {
        String plmnsConfig = System.getString(this.mPhone.getContext().getContentResolver(), "apn_reminder_plmn");
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (TextUtils.isEmpty(plmnsConfig) || (TextUtils.isEmpty(operator) ^ 1) == 0) {
            return false;
        }
        return plmnsConfig.contains(operator);
    }

    public boolean isBtConnected() {
        return this.mIsBtConnected;
    }

    public boolean isUserDataEnabled() {
        return this.mDataEnabledSettings.isUserDataEnabled();
    }

    private void onUserSelectOpenService() {
        log("onUserSelectOpenService set apn = bip0");
        if (this.mAllApnSettings != null && this.mAllApnSettings.isEmpty()) {
            createAllApnList();
        }
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoaded: notifying data availability");
            notifyOffApnsOfAvailability(AbstractPhoneInternalInterface.REASON_SIM_LOADED_PSEUDOIMSI);
        }
        setupDataOnConnectableApns(AbstractPhoneInternalInterface.REASON_SET_PS_ONLY_OK);
    }

    public void checkPLMN(String plmn) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.checkPLMN(plmn);
        }
    }

    private void onOtaAttachFailed(ApnContext apnContext) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.onOtaAttachFailed(apnContext);
        }
    }

    private boolean getmIsPseudoImsi() {
        if (this.mHwCustDcTracker != null) {
            return this.mHwCustDcTracker.getmIsPseudoImsi();
        }
        return false;
    }

    private void sendOTAAttachTimeoutMsg(ApnContext apnContext, boolean retValue) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.sendOTAAttachTimeoutMsg(apnContext, retValue);
        }
    }

    private void openServiceStart(UiccController uiccController) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.openServiceStart(uiccController);
        }
    }

    private void clearAndResumeNetInfiForWifiLteCoexist(int apnId, int enabled, ApnContext apnContext) {
        if (SystemProperties.getBoolean("ro.config.enable_wl_coexist", false)) {
            String apnType = apnContext.getApnType();
            if (apnType.equals("default") || "hipri".equals(apnType)) {
                log("enableApnType but already actived");
                if (enabled != 1) {
                    ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                    boolean isWifiConnected = false;
                    if (cm != null) {
                        NetworkInfo networkInfo = cm.getNetworkInfo(1);
                        if (networkInfo != null) {
                            isWifiConnected = networkInfo.isConnected();
                        }
                    }
                    if (!apnContext.isDisconnected() && (this.mIsWifiConnected || isWifiConnected)) {
                        log("disableApnType due to WIFI Connected");
                        stopNetStatPoll();
                        stopDataStallAlarm();
                        apnContext.setEnabled(false);
                        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
                    }
                } else if (apnContext.getState() == State.CONNECTED) {
                    log("enableApnType: return APN_ALREADY_ACTIVE");
                    apnContext.setEnabled(true);
                    startNetStatPoll();
                    restartDataStallAlarm();
                    this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
                }
            }
        }
    }

    boolean isNeedFilterVowifiMms(ApnSetting apn, String requestedApnType) {
        boolean isMmsRequested = "mms".equals(requestedApnType);
        boolean hasVowifiMmsType = apn != null ? ArrayUtils.contains(apn.types, APN_TYPE_VOWIFIMMS) : false;
        if (isMmsRequested && hasVowifiMmsType) {
            return HuaweiTelephonyConfigs.isHisiPlatform();
        }
        return false;
    }

    public void setCustDataEnableByHplmn() {
        if (this.mHwCustDcTracker == null) {
            log("setCustDataEnableByHplmn: Maybe Exception occurs, mHwCustDcTracker is null");
        } else {
            this.mHwCustDcTracker.setCustDataEnableByHplmn();
        }
    }

    public boolean hasSetCustDataFeature() {
        if (this.mHwCustDcTracker != null) {
            return this.mHwCustDcTracker.hasSetCustDataFeature();
        }
        log("hasSetCustDataFeature: Maybe Exception occurs, mHwCustDcTracker is null");
        return false;
    }

    public void updateCustMobileDataFeature() {
        if (this.mHwCustDcTracker == null) {
            log("updateCustMobileDataFeature: Maybe Exception occurs, mHwCustDcTracker is null");
        } else {
            this.mHwCustDcTracker.updateCustMobileDataFeature();
        }
    }

    protected void setCustUserDataEnabled(boolean enabled) {
        onSetUserDataEnabled(enabled);
    }
}
