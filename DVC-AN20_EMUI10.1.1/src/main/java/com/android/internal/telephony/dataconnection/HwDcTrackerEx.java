package com.android.internal.telephony.dataconnection;

import android.annotation.UnsupportedAppUsage;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.DataConnectionEx;
import com.huawei.internal.telephony.dataconnection.DcFailCauseExt;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import huawei.cust.HwCarrierConfigXmlParse;
import huawei.cust.HwCfgFilePolicy;
import huawei.net.NetworkRequestExt;
import huawei.telephony.data.ApnSettingExt;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import libcore.util.EmptyArray;
import vendor.huawei.hardware.hisiradio.V1_1.LteAttachInfo;

public class HwDcTrackerEx extends DefaultHwDcTrackerEx {
    private static final String ACTION_BT_CONNECTION_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ALLOW_MMS_PROPERTY_INT = "allow_mms_property_int";
    private static final int APNCURE_BLACKLIST = 3;
    private static final String APNCURE_BLACK_MCC_LIST = SystemProperties.get("ro.config.hw_hicure.apn_black_mcc", "334,001");
    private static final String APNCURE_BLACK_PLMN_LIST = SystemProperties.get("ro.config.hw_hicure.apn_black_plmn", "");
    private static final int APNCURE_EHPLMN_EMPTY = 2;
    private static final int APNCURE_FAIL_REASON_BASE = 0;
    private static final int APNCURE_HKGC = 7;
    private static final int APNCURE_ROAMINGBROKER = 5;
    private static final int APNCURE_VALIDE = 1;
    private static final int APNCURE_VIRTUALNET = 4;
    private static final int APNCURE_VSIM = 6;
    private static final int APNFINOFROMSP_APN_INDEX = 1;
    private static final int APNFINOFROMSP_PROTOCOL_INDEX = 2;
    private static final int APNFINOFROMSP_RPLMN_INDEX = 0;
    private static final String APN_ID = "apn_id";
    private static final int APN_PDP_TYPE_IPV4 = 1;
    private static final int APN_PDP_TYPE_IPV4V6 = 3;
    private static final int APN_PDP_TYPE_IPV6 = 2;
    private static final String CAUSE_NO_RETRY_AFTER_DISCONNECT = SystemProperties.get("ro.hwpp.disc_noretry_cause", "");
    private static final int CDMA_NOT_ROAMING = 0;
    private static final int CDMA_ROAMING = 1;
    private static final String[] CHINAUNICOM_PLMN = {MO_UNICOM_MCCMNC, "46006", "46009"};
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String CHR_DATA = "chr_data";
    private static final String CHR_PACKAGE_NAME = "com.huawei.android.chr";
    private static final int CODE_GET_NETD_PID_CMD = 1112;
    private static final String CONFIG_XML_KEYNAME = "mcc_mnc";
    private static final String CONFIG_XML_OPKEYNAME = "operator_key";
    private static final String CONFIG_XML_TYPENAME = "carrier";
    private static final String CT_CDMA_OPERATOR = "46003";
    private static final String CT_LTE_APN_PREFIX = SystemProperties.get("ro.config.ct_lte_apn", "ctnet");
    private static final String CT_NOT_ROAMING_APN_PREFIX = SystemProperties.get("ro.config.ct_not_roaming_apn", "ctnet");
    private static final String CT_ROAMING_APN_PREFIX = SystemProperties.get("ro.config.ct_roaming_apn", "ctnet");
    private static final String CUST_FILE_PATH = "carrier/rule/xml/simInfo_mapping.xml";
    private static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static final int DATA_STALL_ALARM_PUNISH_DELAY_IN_MS_DEFAULT = 1800000;
    private static final String DEAULT_ESMCURE_PROP_VALUE = "2";
    private static final boolean DISABLE_GW_PS_ATTACH = SystemProperties.getBoolean("ro.odm.disable_m1_gw_ps_attach", false);
    private static final String DS_USE_DURATION_KEY = "DSUseDuration";
    private static final int DS_USE_STATISTICS_REPORT_INTERVAL = 3600000;
    private static final String ENABLE_ALLOW_MMS = "enable_always_allow_mms";
    private static final String ESMFLAG_CURE_STATE = "persist.radio.hw_hicure.esmflag_state";
    private static final int EVENT_DATA_CONNECTED = 13;
    private static final int EVENT_DATA_CONNECTION_ATTACHED = 18;
    private static final int EVENT_DATA_CONNECTION_DETACHED = 12;
    private static final int EVENT_DATA_ENABLED_CHANGED = 15;
    private static final int EVENT_DATA_RAT_CHANGED = 16;
    private static final int EVENT_DELAY_RETRY_CONNECT_DATA = 19;
    private static final int EVENT_DISABLE_NR = 8;
    private static final int EVENT_GET_NR_SA_STATE_DONE = 11;
    private static final int EVENT_NETWORK_REJINFO_DONE = 6;
    private static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 14;
    private static final int EVENT_REENABLE_NR_ALARM = 7;
    private static final int EVENT_RIL_CONNECTED = 9;
    private static final int EVENT_SET_NR_SA_STATE_DONE = 10;
    private static final int EVENT_SIM_HOTPLUG = 17;
    private static final int EVENT_VOICE_CALL_ENDED = 3;
    private static final int EVENT_VOICE_CALL_STARTED = 5;
    private static final String FAULT_ID = "fault_id";
    private static final int FAULT_ID_SMART_CURE_APN_CURE = 30001;
    private static final int FLAG_NORMAL_RPC = 0;
    private static final String GC_ICCID = "8985231";
    private static final String GC_MCCMNC = "45431";
    private static final String GC_SPN = "CTExcel";
    private static final int GSM_ROAMING_CARD1 = 2;
    private static final int GSM_ROAMING_CARD2 = 3;
    private static final long HICURE_INFORM_BLOCKTIME = 86400000;
    private static final String HK_GC_CARD_OPERATOR = "46003";
    private static final String HW_APN_CURE_NOTITY = "com.huawei.action.APN_CURE_NOTIFY";
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final Set<Integer> IMMEDIATELY_RETRY_FAILCAUSE = new HashSet();
    private static final Set<String> IMMEDIATELY_RETRY_REASON = new HashSet();
    private static final String INTENT_APN_CURE_STATISTICS = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    private static final String INTENT_DISABLE_NR_ALARM = "com.android.internal.telephony.data_disable_nr";
    private static final String INTENT_DISABLE_NR_ALARM_EXTRA_TIME = "disable_nr_alarm_extra_time";
    private static final String INTENT_DISABLE_NR_ALARM_EXTRA_TYPE = "disable_nr_alarm_extra_type";
    private static final String INTENT_DS_RESTART_RADIO = "com.huawei.intent.action.restart_radio";
    private static final String INTENT_PDP_RESET_ALARM = "com.android.internal.telephony.pdp-reset";
    private static final String INTENT_REENABLE_NR_ALARM = "com.android.internal.telephony.data_reenable_nr";
    private static final String INTENT_REENABLE_NR_ALARM_EXTRA_TYPE = "reenable_nr_alarm_extra_type";
    private static final String INTENT_SET_PREF_NETWORK_TYPE = "com.android.internal.telephony.set-pref-networktype";
    private static final int INVALID_PID = -1;
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_DUAL_4G_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    private static final boolean IS_ENABLE_APNCURE = SystemProperties.getBoolean("ro.config.hw_hicure.apn", false);
    private static final boolean IS_ENABLE_ESMFLAG_CURE = SystemProperties.getBoolean("ro.config.hw_hicure.esmflag", false);
    private static final boolean IS_HW_DATA_RETRY_MANAGER_ENABLED = SystemProperties.getBoolean("ro.config.hw_data_retry_enabled", false);
    private static final boolean IS_MULTI_SIM_ENABLED = HwFrameworkFactory.getHwInnerTelephonyManager().isMultiSimEnabled();
    private static final boolean IS_PDN_REJ_CURE_ENABLE = SystemProperties.getBoolean("ro.config.hw_pdn_rej_data_cure", true);
    private static final int LTE_NOT_ROAMING = 4;
    private static final int LTE_WEAK_RSRP_THRESHOLD = -118;
    private static final byte MATCH_ALL = 1;
    private static final int MAX_RETRY_TIMES = 3;
    private static final int MAX_RSRP_VALUE = -44;
    private static final int MCCMNC_MIN_LENGTH = 5;
    private static final int MCC_LENGTH = 3;
    private static final int MIN_RSRP_VALUE = -140;
    private static final boolean MMSIgnoreDSSwitchNotRoaming;
    private static final boolean MMSIgnoreDSSwitchOnRoaming = (((MMS_PROP >> 1) & 1) == 1);
    private static final boolean MMS_ON_ROAMING = ((MMS_PROP & 1) == 1);
    private static final int MMS_PROP = SystemProperties.getInt("ro.config.hw_always_allow_mms", 4);
    private static final int MNC_LENGTH = 3;
    private static final String MODULE_ID = "module_id";
    private static final int MODULE_ID_SMART_CURE = 30000;
    private static final String MO_ICCID_1 = "8985302";
    private static final String MO_ICCID_2 = "8985307";
    private static final String MO_UNICOM_MCCMNC = "46001";
    private static final long NO_SUGGESTED_RETRY_DELAY = -2;
    private static final int NR_NOT_ROAMING = 5;
    private static final int NR_WEAK_RSRP_THRESHOLD = -115;
    private static final int OEM_DO_RECOVERY_EXCUTE = 2;
    private static final int OEM_DO_RECOVERY_NULL = 0;
    private static final int OEM_DO_RECOVERY_RESTART = 1;
    private static final String PDP_RESET_ALARM_TAG_EXTRA = "pdp.reset.alram.tag";
    private static final int PERIOD_TRY_DISABLE_NR_IN_MS = 2000;
    private static final int PERIOD_TRY_REENABLE_NR_IN_MS = 2000;
    private static final boolean PERMANENT_ERROR_HEAL_PROP = SystemProperties.getBoolean("ro.config.permanent_error_heal", false);
    private static final String PKGNAME_TELEPHONY_DATACONNECTION_PHONE0 = "com.android.internal.telephony.dataconnection.phone0";
    private static final String PKGNAME_TELEPHONY_DATACONNECTION_PHONE1 = "com.android.internal.telephony.dataconnection.phone1";
    private static final String PKGNAME_TELEPHONY_DATACONNECTION_PHONE2 = "com.android.internal.telephony.dataconnection.phone2";
    private static final String PREFERRED_APN_ID = "preferredApnIdEx";
    private static final int PREF_APN_ID_LEN = 5;
    private static final String PS_CLEARCODE_PLMN = SystemProperties.get("ro.config.clearcode_plmn", "");
    private static final int RB_SEQUENCE_LENGTH = 3;
    private static final int RECONNECT_ALARM_DELAY_TIME_SHORT = 0;
    private static final int REJINFO_LEN = 6;
    private static final int REJINFO_RAT_INDEX = 3;
    private static final int REJINFO_TYPE_INDEX = 5;
    private static final boolean RESET_PROFILE = SystemProperties.getBoolean("ro.hwpp_reset_profile", false);
    private static final int RESTART_RADIO_PUNISH_TIME_IN_MS = 14400000;
    private static final int RETRY_ALARM_DELAY_TIME_LONG = 5000;
    private static final int RETRY_ALARM_DELAY_TIME_SHORT = 50;
    private static final int RETRY_DATA_CONNECT_TIME = 3000;
    private static final String ROAMING_BROKER_SEQ_LIST = "8985231";
    private static final int ROAMING_BROKE_AFTERPLMN_INDEX = 2;
    private static final int ROAMING_BROKE_BEFOREPLMN_INDEX = 1;
    private static final String SEPARATOR_KEY = "=-=";
    private static final boolean SETAPN_UNTIL_CARDLOADED = SystemProperties.getBoolean("ro.config.delay_setapn", false);
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final String[] SIM_STATES_AFTER_IMSI = {"IMSI", "LOADED"};
    private static final int SUB2 = 1;
    private static final String SUB_ID = "sub_id";
    private static final int SUCCESS = 0;
    private static final int SWITCH_OFF = 0;
    private static final String TELECOM_IOT_APN_DELAY = "persist.radio.telecom_apn_delay";
    private static final int TYPE_CALLBACK_DO_RECOVERY_ACTION = 7;
    private static final int TYPE_CALLBACK_TCP_TX_AND_RX_SUM = 6;
    private static final int TYPE_DO_RECOVERY_OEM = 704;
    private static final int TYPE_GET_TCP_TX_AND_RX_SUM = 703;
    private static final int TYPE_RX_PACKETS = 1;
    private static final int TYPE_TX_PACKETS = 3;
    private static final int UMTS_WEAK_RSRP_THRESHOLD = -110;
    private static final int UNSPECIFIED_INT = -1;
    private static final boolean USER_FORCE_DATA_SETUP = SystemProperties.getBoolean("ro.hwpp.allow_data_onlycs", false);
    private static final int WAIT_BOOSTER_TCP_STAISTICAL_RESULT_TIMER_MS = 1000;
    private static final boolean WCDMA_VP_ENABLED = SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true");
    private static final String XCAP_DATA_ROAMING_ENABLE = "carrier_xcap_data_roaming_switch";
    private static final String pidStatsPath = "/proc/net/xt_qtaguid/stats_pid";
    private static boolean sIsScreenOn = false;
    private static INetworkStatsService sStatsService;
    private ContentObserver allowMmsObserver = null;
    private boolean isAttachedApnRequested = false;
    private boolean isSupportPidStats = false;
    private AlarmManager mAlarmManager;
    private final SparseArray<ApnContextEx> mApnContextsByTypeFor5GSlice = new SparseArray<>();
    private ApnCureStat mApnCureStat = new ApnCureStat();
    private ApnSetting mAttachedApnSettings = null;
    private int mCurrentLteRsrp = 0;
    private int mCurrentNrRsrp = 0;
    private int mCurrentState = -1;
    private int mCurrentUmtsRsrp = 0;
    private int mDSUseDuration = 0;
    private DcTrackerEx mDcTracker;
    private String mDefaultApnId = "0,0,0,0,0";
    private int mDoRecoveryAct = 1;
    private boolean mDoRecoveryAddDnsProp = SystemProperties.getBoolean("ro.config.dorecovery_add_dns", true);
    private ArrayList<ApnSetting> mEhplmnApnSettings = new ArrayList<>(0);
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass4 */

        public void handleMessage(Message msg) {
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("handleMessage msg=" + msg.what);
            int i = msg.what;
            if (i == 3) {
                HwDcTrackerEx.this.mInVoiceCall = false;
                HwDcTrackerEx.this.onVoiceCallEndedHw();
            } else if (i == 5) {
                HwDcTrackerEx.this.mInVoiceCall = true;
            } else if (i != 6) {
                HwDcTrackerEx.this.onDataRetryEventProc(msg);
            } else {
                HwDcTrackerEx.this.onNetworkRejInfoDone(msg);
            }
        }
    };
    private HwDataRetryManager mHwDataRetryManager = null;
    private HwDataSelfCure mHwDataSelfCure = null;
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass3 */

        public void callBack(int type, Bundle b) throws RemoteException {
            if (b == null) {
                HwDcTrackerEx.this.log("data is null");
            } else if (type == 6) {
                HwDcTrackerEx.this.mTcpRxPktSum = b.getLong("tcpRxDataPktSum");
                HwDcTrackerEx.this.mTcpTxPktSum = b.getLong("tcpTxPktSum");
                HwDcTrackerEx.this.mCurrentLteRsrp = b.getInt("currentLteRsrp");
                HwDcTrackerEx.this.mCurrentNrRsrp = b.getInt("currentNrRsrp");
                HwDcTrackerEx.this.mCurrentUmtsRsrp = b.getInt("currentUmtsRsrp");
            } else if (type == 7) {
                if (b.getInt("DoRecoveryAct") == 0) {
                    HwDcTrackerEx.this.mDoRecoveryAct = 0;
                    HwDcTrackerEx.this.mDcTracker.sendMessage(HwDcTrackerEx.this.mDcTracker.obtainMessage(270354, (Object) null));
                    return;
                }
                HwDcTrackerEx.this.mDoRecoveryAct = 2;
            }
        }
    };
    private IHwCommBoosterServiceManager mIhwCommBoosterServiceManager = null;
    private boolean mInVoiceCall = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwDcTrackerEx.this.loge("intent or intent.getAction() is null.");
                return;
            }
            String action = intent.getAction();
            char c = 65535;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 4;
                        break;
                    }
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 3;
                        break;
                    }
                    break;
                case -1076576821:
                    if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                        c = '\f';
                        break;
                    }
                    break;
                case -910144067:
                    if (action.equals("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE")) {
                        c = '\t';
                        break;
                    }
                    break;
                case -843338808:
                    if (action.equals(HwDcTrackerEx.ACTION_BT_CONNECTION_CHANGED)) {
                        c = 5;
                        break;
                    }
                    break;
                case -374985024:
                    if (action.equals(HwDcTrackerEx.HW_SYSTEM_SERVER_START)) {
                        c = 11;
                        break;
                    }
                    break;
                case -238804267:
                    if (action.equals(HwDcTrackerEx.INTENT_DISABLE_NR_ALARM)) {
                        c = '\b';
                        break;
                    }
                    break;
                case -229777127:
                    if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 82734660:
                    if (action.equals(HwDcTrackerEx.INTENT_SET_PREF_NETWORK_TYPE)) {
                        c = 14;
                        break;
                    }
                    break;
                case 798292259:
                    if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1075003621:
                    if (action.equals(HwDcTrackerEx.HW_APN_CURE_NOTITY)) {
                        c = '\r';
                        break;
                    }
                    break;
                case 1843173795:
                    if (action.equals(HwDcTrackerEx.INTENT_PDP_RESET_ALARM)) {
                        c = 6;
                        break;
                    }
                    break;
                case 1896999900:
                    if (action.equals("com.android.telephony.opencard")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 1947666138:
                    if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                        c = 1;
                        break;
                    }
                    break;
                case 2134631443:
                    if (action.equals(HwDcTrackerEx.INTENT_REENABLE_NR_ALARM)) {
                        c = 7;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    onSimStateChange(intent);
                    return;
                case 1:
                    onShutDown();
                    return;
                case 2:
                    onBootCompleted();
                    return;
                case 3:
                    boolean unused = HwDcTrackerEx.sIsScreenOn = true;
                    return;
                case 4:
                    boolean unused2 = HwDcTrackerEx.sIsScreenOn = false;
                    return;
                case 5:
                    onBtConnectChanged(intent);
                    return;
                case 6:
                    HwDcTrackerEx.this.onActionIntentPdpResetAlarm(intent);
                    return;
                case 7:
                    HwDcTrackerEx.this.onActionIntentReenableNrAlarm(intent);
                    return;
                case '\b':
                    HwDcTrackerEx.this.onActionIntentDisableNrAlarm(intent);
                    return;
                case '\t':
                    HwDcTrackerEx.this.onDualCardSwitchDone();
                    return;
                case HwDcTrackerEx.EVENT_SET_NR_SA_STATE_DONE /*{ENCODED_INT: 10}*/:
                    HwDcTrackerEx.this.onUserSelectOpenService();
                    return;
                case HwDcTrackerEx.EVENT_GET_NR_SA_STATE_DONE /*{ENCODED_INT: 11}*/:
                    HwDcTrackerEx.this.log("HW_SYSTEM_SERVER_START restart");
                    HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
                    hwDcTrackerEx.registerBoosterCallback(hwDcTrackerEx.mDcTracker.getTransportType(), HwDcTrackerEx.this.mPhone.getPhoneId());
                    return;
                case '\f':
                    onAirModeOn(intent);
                    return;
                case HwDcTrackerEx.EVENT_DATA_CONNECTED /*{ENCODED_INT: 13}*/:
                    HwDcTrackerEx.this.mIsApnCureEnabled = intent.getBooleanExtra("APN_CURE_ENABLED", false);
                    HwDcTrackerEx.this.log("apn cure notify, retry enable data: " + HwDcTrackerEx.this.mIsApnCureEnabled);
                    if (HwDcTrackerEx.this.mIsApnCureEnabled) {
                        HwDcTrackerEx.this.mHandler.sendMessageDelayed(HwDcTrackerEx.this.mHandler.obtainMessage(HwDcTrackerEx.EVENT_DELAY_RETRY_CONNECT_DATA), 3000);
                        return;
                    }
                    return;
                case HwDcTrackerEx.EVENT_RADIO_OFF_OR_NOT_AVAILABLE /*{ENCODED_INT: 14}*/:
                    HwDcTrackerEx.this.onActionIntentSetNetworkType(intent);
                    return;
                default:
                    HwDcTrackerEx.this.loge("Not handle action " + intent.getAction());
                    return;
            }
        }

        private void onSimStateChange(Intent intent) {
            int slotId = intent.getIntExtra("phone", -1);
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("slotId = " + slotId + ", mSubscription = " + HwDcTrackerEx.this.mSubscription);
            if (!SubscriptionManager.isValidSlotIndex(slotId) || slotId != HwDcTrackerEx.this.mSubscription.intValue()) {
                HwDcTrackerEx.this.log("receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , but the subid is different from mSubscription");
                return;
            }
            String curSimState = intent.getStringExtra("ss");
            if ("NOT_READY".equals(curSimState) || "ABSENT".equals(curSimState)) {
                HwDcTrackerEx.this.setAttachedApnSetting(null);
                HwDcTrackerEx.this.isAttachedApnRequested = false;
                HwDcTrackerEx.this.mIsApnCureEnabled = false;
                HwDcTrackerEx.this.mDcTracker.clearCureApnSettings();
                HwDcTrackerEx.this.mHwDataSelfCure.apnCureFlagReset();
            }
            if (TextUtils.equals(curSimState, HwDcTrackerEx.this.mSimState)) {
                HwDcTrackerEx.this.logd("the curSimState is same as mSimState, so return");
                return;
            }
            if ("ABSENT".equals(curSimState)) {
                HwDcTrackerEx.this.sendApnCureResult();
            }
            if (("ABSENT".equals(curSimState) || "CARD_IO_ERROR".equals(curSimState)) && !"ABSENT".equals(HwDcTrackerEx.this.mSimState) && !"CARD_IO_ERROR".equals(HwDcTrackerEx.this.mSimState) && HwDcTrackerEx.RESET_PROFILE) {
                HwDcTrackerEx.this.logd("receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , resetprofile");
                CommandsInterfaceEx ci = HwDcTrackerEx.this.mPhone.getCi();
                if (ci != null) {
                    ci.resetProfile((Message) null);
                }
            }
            ((HwDcTrackerEx) HwDcTrackerEx.this).mSimState = curSimState;
        }

        private void onShutDown() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HwDcTrackerEx.this.mPhone.getContext());
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(HwDcTrackerEx.DS_USE_DURATION_KEY, HwDcTrackerEx.this.mDSUseDuration);
            editor.commit();
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("Put mDSUseDuration into SharedPreferences, put: " + HwDcTrackerEx.this.mDSUseDuration + ", get: " + sp.getInt(HwDcTrackerEx.DS_USE_DURATION_KEY, 0));
        }

        private void onBootCompleted() {
            int lastDSUseDuration = PreferenceManager.getDefaultSharedPreferences(HwDcTrackerEx.this.mPhone.getContext()).getInt(HwDcTrackerEx.DS_USE_DURATION_KEY, 0);
            HwDcTrackerEx.access$2112(HwDcTrackerEx.this, lastDSUseDuration);
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("Read last mDSUseDuration back from SharedPreferences, lastDSUseDuration: " + lastDSUseDuration + ", mDSUseDuration: " + HwDcTrackerEx.this.mDSUseDuration);
        }

        private void onBtConnectChanged(Intent intent) {
            if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 0) {
                HwDcTrackerEx.this.mIsBtConnected = false;
            } else if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 2) {
                HwDcTrackerEx.this.mIsBtConnected = true;
            }
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("Received bt_connect_state = " + HwDcTrackerEx.this.mIsBtConnected);
        }

        private void onAirModeOn(Intent intent) {
            if (intent.getBooleanExtra("state", false)) {
                HwDcTrackerEx.this.resetDataCureInfo("airplaneModeOn");
            }
        }
    };
    private boolean mIsApnCureEnabled = false;
    private boolean mIsBtConnected = false;
    private boolean mIsMMSAllowed = false;
    private long mLastRadioResetTimestamp = 0;
    private long mMobileDnsRxPktSum = 0;
    private long mMobileDnsTxPktSum = 0;
    private long mNextReportDSUseDurationStamp = (SystemClock.elapsedRealtime() + 3600000);
    private ContentObserver mNwChangeObserver = null;
    private int mNwOldMode = Phone.PREFERRED_NT_MODE;
    private int mOldState = 0;
    private PendingIntent mPdpResetAlarmIntent = null;
    private int mPdpResetAlarmTag = ((int) SystemClock.elapsedRealtime());
    private PhoneExt mPhone;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass1 */

        public void onCallStateChanged(int state, String incomingNumber) {
            if (HwDcTrackerEx.this.mOldState == 2 && state == 0 && HwDcTrackerEx.this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
                HwDcTrackerEx.this.mDcTracker.setupDataOnAllConnectableApns("dataEnabled");
                HwDcTrackerEx.this.log("resetRetryCount");
                HwDcTrackerEx.this.mDcTracker.resetDefaultApnRetryCount();
            }
            HwDcTrackerEx.this.mOldState = state;
        }

        public void onDataConnectionStateChanged(int state) {
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("onDataConnectionStateChanged mSubId state = " + state);
            if (state == 2) {
                HwDcTrackerEx.this.mHandler.sendMessage(HwDcTrackerEx.this.mHandler.obtainMessage(HwDcTrackerEx.EVENT_DATA_CONNECTED));
            }
        }
    };
    private int mRetryTimes = 0;
    private HashSet<String> mRoamingbrokePlmnList = new HashSet<>(0);
    private String mSimState = null;
    private long mStartCureTime = 0;
    private Integer mSubscription;
    private String mTag;
    private long mTcpRxPktSum = 0;
    private long mTcpTxPktSum = 0;
    private HashSet<String> mVirtualNetPlmnList = new HashSet<>(0);
    private long mWifiDnsRxPktSum = 0;
    private long mWifiDnsTxPktSum = 0;
    private int msgReceiveCounter = 0;
    private int msgTransCounter = 0;
    private int nwMode = Phone.PREFERRED_NT_MODE;
    private int preDataRadioTech = -1;
    private int preSetupBasedRadioTech = -1;

    /* access modifiers changed from: private */
    public enum HotplugState {
        PLUG_OUT,
        PLUG_IN
    }

    static /* synthetic */ int access$2112(HwDcTrackerEx x0, int x1) {
        int i = x0.mDSUseDuration + x1;
        x0.mDSUseDuration = i;
        return i;
    }

    static {
        boolean z = true;
        IMMEDIATELY_RETRY_FAILCAUSE.add(Integer.valueOf(DcFailCause.LOST_CONNECTION.getErrorCode()));
        IMMEDIATELY_RETRY_FAILCAUSE.add(Integer.valueOf(DcFailCause.NETWORK_RECONFIGURE.getErrorCode()));
        IMMEDIATELY_RETRY_REASON.add("pdpReset");
        IMMEDIATELY_RETRY_REASON.add("nwTypeChanged");
        IMMEDIATELY_RETRY_REASON.add(DcFailCause.LOST_CONNECTION.toString());
        if (((MMS_PROP >> 2) & 1) != 1) {
            z = false;
        }
        MMSIgnoreDSSwitchNotRoaming = z;
    }

    /* access modifiers changed from: private */
    public class ApnCureStat {
        private String intentType;
        private int mAttachSuccCnt;
        private int mEhplmnSuccCnt;
        private int mEnabledFailCnt;
        private int mFailReason;
        private int mSetupSuccCnt;
        private String simOperator;

        static /* synthetic */ int access$3608(ApnCureStat x0) {
            int i = x0.mSetupSuccCnt;
            x0.mSetupSuccCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$4108(ApnCureStat x0) {
            int i = x0.mEhplmnSuccCnt;
            x0.mEhplmnSuccCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$4308(ApnCureStat x0) {
            int i = x0.mEnabledFailCnt;
            x0.mEnabledFailCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$4408(ApnCureStat x0) {
            int i = x0.mAttachSuccCnt;
            x0.mAttachSuccCnt = i + 1;
            return i;
        }

        private ApnCureStat() {
            this.mAttachSuccCnt = 0;
            this.mEhplmnSuccCnt = 0;
            this.mEnabledFailCnt = 0;
            this.mSetupSuccCnt = 0;
            this.mFailReason = 1;
            this.simOperator = "";
            this.intentType = "";
            this.intentType = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetAll() {
            this.mAttachSuccCnt = 0;
            this.mEhplmnSuccCnt = 0;
            this.mEnabledFailCnt = 0;
            this.mSetupSuccCnt = 0;
            this.mFailReason = 1;
            this.simOperator = "";
            this.intentType = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
        }
    }

    public HwDcTrackerEx(PhoneExt phoneExt, DcTrackerEx dcTrackerEx) {
        if (phoneExt == null || dcTrackerEx == null) {
            loge("error in create HwDcTrackerEx for params is null.");
            return;
        }
        this.mPhone = phoneExt;
        this.mDcTracker = dcTrackerEx;
        this.mTag = HwDcTrackerEx.class.getSimpleName() + " - " + this.mPhone.getPhoneId();
        if (IS_PDN_REJ_CURE_ENABLE) {
            this.mHwDataSelfCure = new HwDataSelfCure(this.mDcTracker, this, this.mPhone);
        }
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED) {
            this.mHwDataRetryManager = new HwDataRetryManager(this.mDcTracker, this, this.mPhone);
        }
    }

    public void init() {
        boolean z = false;
        if (Settings.System.getInt(this.mPhone.getContext().getContentResolver(), ENABLE_ALLOW_MMS, 0) == 1) {
            z = true;
        }
        this.mIsMMSAllowed = z;
        Uri allowMmsUri = Settings.System.CONTENT_URI;
        this.allowMmsObserver = new AllowMmsContentObserver(this.mHandler);
        this.mPhone.getContext().getContentResolver().registerContentObserver(allowMmsUri, true, this.allowMmsObserver);
        this.mNwChangeObserver = new NwModeContentObserver(this.mHandler);
        this.mPhone.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + this.mPhone.getPhoneId()), true, this.mNwChangeObserver);
        this.nwMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mPhone.getContext(), this.mPhone.getPhoneId());
        this.mNwOldMode = this.nwMode;
        this.mPhone.registerForVoiceCallEnded(this.mHandler, 3, (Object) null);
        this.mPhone.registerForVoiceCallStarted(this.mHandler, 5, (Object) null);
        this.mSubscription = Integer.valueOf(this.mPhone.getPhoneId());
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        registerBroadcastReceiver(this.mHandler);
        registerForDataRetryEvents(this.mDcTracker.getTransportType(), this.mHandler);
        HwDataRetryManager hwDataRetryManager = this.mHwDataRetryManager;
        if (hwDataRetryManager != null) {
            hwDataRetryManager.setLogTagSuffix(this.mTag);
            this.mHwDataRetryManager.configDataRetryStrategy();
        }
        isSupportPidStatistics();
        this.mIhwCommBoosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        registerBoosterCallback(this.mDcTracker.getTransportType(), this.mPhone.getPhoneId());
        registerPhoneStateListener(this.mPhone.getContext());
    }

    private void registerBroadcastReceiver(Handler handler) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION_BT_CONNECTION_CHANGED);
        filter.addAction(INTENT_PDP_RESET_ALARM);
        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", false)) {
            filter.addAction("com.android.telephony.opencard");
        }
        filter.addAction(HW_SYSTEM_SERVER_START);
        filter.addAction(HW_APN_CURE_NOTITY);
        if (IS_PDN_REJ_CURE_ENABLE) {
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.setOnNetReject(handler, 6, Integer.valueOf(this.mPhone.getPhoneId()));
            }
        }
        filter.addAction(INTENT_REENABLE_NR_ALARM);
        filter.addAction(INTENT_DISABLE_NR_ALARM);
        filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        filter.addAction(INTENT_SET_PREF_NETWORK_TYPE);
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mDcTracker.getHandler());
    }

    private void registerForDataRetryEvents(int transportType, Handler handler) {
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED) {
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.registerForRilConnected(handler, 9, (Object) null);
                ci.registerForOffOrNotAvailable(handler, (int) EVENT_RADIO_OFF_OR_NOT_AVAILABLE, (Object) null);
                ci.registerForSimHotPlug(handler, (int) EVENT_SIM_HOTPLUG, Integer.valueOf(this.mPhone.getPhoneId()));
            }
            this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(transportType, handler, 12, (Object) null);
            this.mDcTracker.registerForDataEnabledChanged(handler, 15, (Object) null);
        }
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(transportType, handler, (int) EVENT_DATA_CONNECTION_ATTACHED, (Object) null);
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(transportType, handler, 16, (Object) null);
    }

    public void dispose() {
        if (this.allowMmsObserver != null) {
            this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.allowMmsObserver);
        }
        if (this.mNwChangeObserver != null) {
            this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mNwChangeObserver);
        }
        this.mPhone.unregisterForVoiceCallStarted(this.mHandler);
        this.mPhone.unregisterForVoiceCallEnded(this.mHandler);
        if (this.mIntentReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
        CommandsInterfaceEx ci = this.mPhone.getCi();
        if (IS_PDN_REJ_CURE_ENABLE && ci != null) {
            ci.unSetOnNetReject(this.mHandler);
        }
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED && ci != null) {
            ci.unregisterForRilConnected(this.mHandler);
        }
    }

    class NwModeContentObserver extends ContentObserver {
        NwModeContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean Change) {
            if (HwDcTrackerEx.IS_MULTI_SIM_ENABLED) {
                if (TelephonyManager.getTelephonyProperty(HwDcTrackerEx.this.mPhone.getPhoneId(), "gsm.data.gsm_only_not_allow_ps", "false").equals("false")) {
                    return;
                }
            } else if (!SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", false)) {
                return;
            }
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.nwMode = HwNetworkTypeUtils.getNetworkModeFromDB(hwDcTrackerEx.mPhone.getContext(), HwDcTrackerEx.this.mPhone.getPhoneId());
            HwDcTrackerEx hwDcTrackerEx2 = HwDcTrackerEx.this;
            hwDcTrackerEx2.log("NwModeChangeObserver onChange nwMode = " + HwDcTrackerEx.this.nwMode);
            if (1 == HwDcTrackerEx.this.nwMode) {
                HwDcTrackerEx.this.mDcTracker.cleanUpAllConnections("nwTypeChanged");
            } else if (1 == HwDcTrackerEx.this.mNwOldMode) {
                HwDcTrackerEx.this.mDcTracker.setupDataOnAllConnectableApns("nwTypeChanged");
            }
            HwDcTrackerEx hwDcTrackerEx3 = HwDcTrackerEx.this;
            hwDcTrackerEx3.mNwOldMode = hwDcTrackerEx3.nwMode;
        }
    }

    private class AllowMmsContentObserver extends ContentObserver {
        AllowMmsContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            boolean z = false;
            int allowMms = Settings.System.getInt(HwDcTrackerEx.this.mPhone.getContext().getContentResolver(), HwDcTrackerEx.ENABLE_ALLOW_MMS, 0);
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            if (allowMms == 1) {
                z = true;
            }
            hwDcTrackerEx.mIsMMSAllowed = z;
        }
    }

    public void beforeHandleMessage(Message msg) {
        if (msg.what == 271140 && WCDMA_VP_ENABLED) {
            log("EVENT_VP_STATUS_CHANGED");
            onVpStatusChanged((AsyncResult) msg.obj);
        }
    }

    public long getTcpRxPktSum() {
        return this.mTcpRxPktSum;
    }

    public long getTcpTxPktSum() {
        return this.mTcpTxPktSum;
    }

    private boolean isValidRsrpValue(int rsrp) {
        return rsrp <= MAX_RSRP_VALUE && rsrp >= MIN_RSRP_VALUE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x005d  */
    private boolean isWeakSignalStrength() {
        int networkType = TelephonyManager.getDefault().getNetworkType(this.mPhone.getSubId());
        boolean isWeak = false;
        boolean z = true;
        if (!(networkType == 3 || networkType == EVENT_SIM_HOTPLUG || networkType == 30 || networkType == 5 || networkType == 6)) {
            if (networkType != EVENT_DELAY_RETRY_CONNECT_DATA) {
                if (networkType != 20) {
                    switch (networkType) {
                        default:
                            switch (networkType) {
                            }
                        case 8:
                        case 9:
                        case EVENT_SET_NR_SA_STATE_DONE /*{ENCODED_INT: 10}*/:
                            if (isValidRsrpValue(this.mCurrentUmtsRsrp)) {
                                if (this.mCurrentUmtsRsrp > UMTS_WEAK_RSRP_THRESHOLD) {
                                    z = false;
                                }
                                isWeak = z;
                                break;
                            }
                            break;
                    }
                } else if (isValidRsrpValue(this.mCurrentNrRsrp)) {
                    if (this.mCurrentNrRsrp > NR_WEAK_RSRP_THRESHOLD) {
                        z = false;
                    }
                    isWeak = z;
                }
                log("updateDataStallInfo: networkType = " + networkType + " LTE RSRP = " + this.mCurrentLteRsrp + " NR RSRP = " + this.mCurrentNrRsrp + " UMTS RSRP = " + this.mCurrentUmtsRsrp);
                return isWeak;
            }
            if (isValidRsrpValue(this.mCurrentLteRsrp)) {
                if (this.mCurrentLteRsrp > LTE_WEAK_RSRP_THRESHOLD) {
                    z = false;
                }
                isWeak = z;
            }
            log("updateDataStallInfo: networkType = " + networkType + " LTE RSRP = " + this.mCurrentLteRsrp + " NR RSRP = " + this.mCurrentNrRsrp + " UMTS RSRP = " + this.mCurrentUmtsRsrp);
            return isWeak;
        }
        if (isValidRsrpValue(this.mCurrentUmtsRsrp)) {
        }
        log("updateDataStallInfo: networkType = " + networkType + " LTE RSRP = " + this.mCurrentLteRsrp + " NR RSRP = " + this.mCurrentNrRsrp + " UMTS RSRP = " + this.mCurrentUmtsRsrp);
        return isWeak;
    }

    private void updateRecoveryInfo(long tcpSent, long tcpReceived, long dnsSent, long dnsReceived) {
        if (tcpReceived > 0) {
            log("updateDataStallInfo: TCP IN");
            this.mDcTracker.setSentSinceLastRecv(0, false);
            this.mDcTracker.resetRecoveryInfo();
        } else if (tcpSent > 0) {
            if (this.mDcTracker.isPhoneStateIdle()) {
                this.mDcTracker.setSentSinceLastRecv(tcpSent, true);
                this.mDcTracker.setRecoveryReason("tcp sent no recv", false);
                if (dnsSent > 0 && dnsReceived == 0) {
                    this.mDcTracker.setSentSinceLastRecv(dnsSent, true);
                    this.mDcTracker.setRecoveryReason(", dns sent no recv.", true);
                }
            } else {
                this.mDcTracker.setSentSinceLastRecv(0, false);
            }
            log("updateDataStallInfo: DNS+TCP OUT sent=" + tcpSent + dnsSent + " mSentSinceLastRecv=" + this.mDcTracker.getSentSinceLastRecv());
        } else if (dnsSent <= 0 || dnsReceived != 0) {
            log("updateDataStallInfo: TCP NONE");
        } else {
            if (this.mDcTracker.isPhoneStateIdle()) {
                this.mDcTracker.setRecoveryReason("dns sent no recv", false);
                this.mDcTracker.setSentSinceLastRecv(dnsSent, true);
            } else {
                this.mDcTracker.setSentSinceLastRecv(0, false);
            }
            log("updateDataStallInfo: DNS OUT sent=" + dnsSent + " mSentSinceLastRecv=" + this.mDcTracker.getSentSinceLastRecv());
        }
    }

    public void updateRecoveryPktStat() {
        long dnsReceived = 0;
        if (isWeakSignalStrength()) {
            this.mDcTracker.setSentSinceLastRecv(0, false);
            this.mDcTracker.resetRecoveryInfo();
            log("updateDataStallInfo: weak signal do not dorecover");
            return;
        }
        DcTrackerEx.TxRxSumEx preTcpTxRxSum = this.mDcTracker.getPreDataStallTcpTxRxSum();
        DcTrackerEx.TxRxSumEx preDnsTxRxSum = this.mDcTracker.getPreDataStallDnsTxRxSum();
        this.mDcTracker.updateHwTcpTxRxSum(getTcpTxPktSum(), getTcpRxPktSum());
        this.mDcTracker.setDnsTxPktsSum(getDnsUidPackets(1051, 3));
        this.mDcTracker.setDnsRxPktsSum(getDnsUidPackets(1051, 1));
        log("updateDataStallInfo: mDataStallDnsTxRxSum=" + this.mDcTracker.getDataStallDnsTxRxSum() + " preDnsTxRxSum=" + preDnsTxRxSum + "\nupdateDataStallInfo: mDataStallTcpTxRxSum=" + this.mDcTracker.getDataStallTcpTxRxSum() + " preTcpTxRxSum=" + preTcpTxRxSum);
        long tcpSent = this.mDcTracker.getDataStallTcpTxRxSum() != null ? this.mDcTracker.getDataStallTcpTxRxSum().txPkts - preTcpTxRxSum.txPkts : 0;
        long tcpReceived = this.mDcTracker.getDataStallTcpTxRxSum() != null ? this.mDcTracker.getDataStallTcpTxRxSum().rxPkts - preTcpTxRxSum.rxPkts : 0;
        long dnsSent = this.mDcTracker.getDataStallDnsTxRxSum() != null ? this.mDcTracker.getDataStallDnsTxRxSum().txPkts - preDnsTxRxSum.txPkts : 0;
        if (this.mDcTracker.getDataStallDnsTxRxSum() != null) {
            dnsReceived = this.mDcTracker.getDataStallDnsTxRxSum().rxPkts - preDnsTxRxSum.rxPkts;
        }
        updateRecoveryInfo(tcpSent, tcpReceived, dnsSent, dnsReceived);
    }

    @UnsupportedAppUsage
    private static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (HwDcTrackerEx.class) {
            if (sStatsService == null) {
                sStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            }
            iNetworkStatsService = sStatsService;
        }
        return iNetworkStatsService;
    }

    private boolean isWifiHasConnected() {
        ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
        if (cm == null) {
            log("isWifiHasConnected cm is null");
            return false;
        }
        Network[] allNetworks = cm.getAllNetworks();
        if (allNetworks == null) {
            log("isWifiHasConnected allNetworkInfo is null");
            return false;
        }
        for (Network network : allNetworks) {
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            if (nc != null && nc.hasTransport(1)) {
                return true;
            }
        }
        return false;
    }

    public long getDnsUidPackets(int uid, int type) {
        try {
            if (!this.mDoRecoveryAddDnsProp) {
                log("mDoRecoveryAddDnsProp is false");
                return 0;
            } else if (type == 3) {
                long increaseNum = (getStatsService().getUidStats(uid, type) - this.mWifiDnsTxPktSum) - this.mMobileDnsTxPktSum;
                if (!isWifiHasConnected()) {
                    this.mMobileDnsTxPktSum += increaseNum;
                } else {
                    this.mWifiDnsTxPktSum += increaseNum;
                }
                return this.mMobileDnsTxPktSum;
            } else if (type == 1) {
                long increaseNum2 = (getStatsService().getUidStats(uid, type) - this.mWifiDnsRxPktSum) - this.mMobileDnsRxPktSum;
                if (!isWifiHasConnected()) {
                    this.mMobileDnsRxPktSum += increaseNum2;
                } else {
                    this.mWifiDnsRxPktSum += increaseNum2;
                }
                return this.mMobileDnsRxPktSum;
            } else {
                log("getDnsUidPackets type error");
                return 0;
            }
        } catch (RemoteException e) {
            log("getDnsUidPackets fail");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerBoosterCallback(int transportType, int phoneId) {
        int ret;
        if (transportType != 1) {
            log("mTransportType is not TRANSPORT_TYPE_WWAN, need not registerBoosterCallback");
            return;
        }
        log("registerBoosterCallback enter");
        IHwCommBoosterServiceManager iHwCommBoosterServiceManager = this.mIhwCommBoosterServiceManager;
        if (iHwCommBoosterServiceManager != null) {
            if (phoneId == 0) {
                ret = iHwCommBoosterServiceManager.registerCallBack(PKGNAME_TELEPHONY_DATACONNECTION_PHONE0, this.mIHwCommBoosterCallback);
            } else if (phoneId == 1) {
                ret = iHwCommBoosterServiceManager.registerCallBack(PKGNAME_TELEPHONY_DATACONNECTION_PHONE1, this.mIHwCommBoosterCallback);
            } else {
                ret = iHwCommBoosterServiceManager.registerCallBack(PKGNAME_TELEPHONY_DATACONNECTION_PHONE2, this.mIHwCommBoosterCallback);
            }
            if (ret != 0) {
                log("registerBoosterCallback:registerCallBack failed, ret=" + ret);
                return;
            }
            return;
        }
        log("registerBoosterCallback:null HwCommBoosterServiceManager");
    }

    public void notifyGetTcpSumMsgReportToBooster(int tag) {
        int ret;
        if (this.mIhwCommBoosterServiceManager == null) {
            log("mIhwCommBoosterServiceManager is null");
            return;
        }
        Bundle data = new Bundle();
        data.putInt("currentPhoneId", this.mPhone.getPhoneId());
        if (this.mPhone.getPhoneId() == 0) {
            ret = this.mIhwCommBoosterServiceManager.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE0, (int) TYPE_GET_TCP_TX_AND_RX_SUM, data);
        } else if (this.mPhone.getPhoneId() == 1) {
            ret = this.mIhwCommBoosterServiceManager.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE1, (int) TYPE_GET_TCP_TX_AND_RX_SUM, data);
        } else {
            ret = this.mIhwCommBoosterServiceManager.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE2, (int) TYPE_GET_TCP_TX_AND_RX_SUM, data);
        }
        if (ret != 0) {
            log("reportBoosterPara failed, ret=" + ret);
        }
        Message msg = this.mDcTracker.obtainMessage(271148, (Object) null);
        msg.arg1 = tag;
        this.mDcTracker.sendMessageDelayed(msg, 1000);
    }

    public boolean isDataAllowedByApnContext(ApnContextEx apnContext) {
        if (isBipApnType(apnContext.getApnType())) {
            return true;
        }
        if (isGsmOnlyPsNotAllowed()) {
            log("in GsmMode not allowed PS!");
            return false;
        } else if (!this.mDcTracker.isLimitPDPAct()) {
            return true;
        } else {
            log("PSCLEARCODE Limit PDP Act apnContext: " + apnContext);
            return false;
        }
    }

    private boolean isGsmOnlyPsNotAllowed() {
        if (!IS_MULTI_SIM_ENABLED) {
            return SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", false) && 1 == this.nwMode;
        }
        int slotId = this.mPhone.getPhoneId();
        int networkMode = this.nwMode;
        if (IS_DUAL_4G_SUPPORTED && SIM_NUM > 1) {
            networkMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mPhone.getContext(), slotId);
        }
        return TelephonyManager.getTelephonyProperty(slotId, "gsm.data.gsm_only_not_allow_ps", "false").equals("true") && 1 == networkMode;
    }

    public boolean isDataAllowedForRoaming(boolean isMms) {
        int allowMmsPropertyByPlmn = getallowMmsPropertyByPlmn();
        if (-1 == allowMmsPropertyByPlmn) {
            return !this.mPhone.getServiceState().getRoaming() || this.mDcTracker.getDataRoamingEnabled() || ((this.mIsMMSAllowed || MMS_ON_ROAMING) && isMms);
        }
        return !this.mPhone.getServiceState().getRoaming() || this.mDcTracker.getDataRoamingEnabled() || ((this.mIsMMSAllowed || ((allowMmsPropertyByPlmn & 1) == 1)) && isMms);
    }

    public ApnSetting fetchBipApn(ApnSetting preferredApn, List<ApnSetting> allApnSettings) {
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            ApnSetting apnSetting = ApnSetting.fromString(SystemProperties.get("gsm.bip.apn"));
            if ("default".equals(SystemProperties.get("gsm.bip.apn"))) {
                if (preferredApn != null) {
                    log("find prefer apn, use this");
                    return preferredApn;
                }
                if (allApnSettings != null) {
                    int list_size = allApnSettings.size();
                    for (int i = 0; i < list_size; i++) {
                        ApnSetting apn = allApnSettings.get(i);
                        if (apn.canHandleType(EVENT_SIM_HOTPLUG)) {
                            log("find the first default apn");
                            return apn;
                        }
                    }
                }
                log("find non apn for default bip");
                return null;
            } else if (apnSetting != null) {
                log("fetchBipApn: global BIP mDataProfile=" + apnSetting);
                return apnSetting;
            }
        }
        return null;
    }

    public String getDataRoamingSettingItem(String originItem) {
        if (!IS_MULTI_SIM_ENABLED || this.mPhone.getPhoneId() != 1) {
            return originItem;
        }
        return DATA_ROAMING_SIM2;
    }

    public boolean getAnyDataEnabledByApnContext(ApnContextEx apnContext, boolean enable) {
        int allowMmsPropertyByPlmn = getallowMmsPropertyByPlmn();
        if (this.mPhone.getServiceState().getRoaming()) {
            if (getXcapDataRoamingEnable() && "xcap".equals(apnContext.getApnType())) {
                return true;
            }
            if (-1 == allowMmsPropertyByPlmn) {
                return ((this.mIsMMSAllowed || MMSIgnoreDSSwitchOnRoaming) && "mms".equals(apnContext.getApnType())) || enable;
            }
            return ((this.mIsMMSAllowed || (((allowMmsPropertyByPlmn >> 1) & 1) == 1)) && "mms".equals(apnContext.getApnType())) || enable;
        } else if (-1 == allowMmsPropertyByPlmn) {
            return ((this.mIsMMSAllowed || MMSIgnoreDSSwitchNotRoaming) && "mms".equals(apnContext.getApnType())) || enable;
        } else {
            return ((this.mIsMMSAllowed || (((allowMmsPropertyByPlmn >> 2) & 1) == 1)) && "mms".equals(apnContext.getApnType())) || enable;
        }
    }

    public boolean noNeedDoRecovery() {
        long now = SystemClock.elapsedRealtime();
        if (!SystemProperties.getBoolean("persist.radio.hw.nodorecovery", false) && ((!SystemProperties.getBoolean("hw.ds.np.nopollstat", true) || isActiveDefaultApnPreset()) && this.mPhone.getServiceState().getDataRegState() == 0)) {
            long j = this.mLastRadioResetTimestamp;
            if (j == 0 || now - j >= 1800000) {
                return false;
            }
        }
        return true;
    }

    private boolean isActiveDefaultApnPreset() {
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType("default");
        if (apnContext == null || ApnContextEx.StateEx.CONNECTED != apnContext.getState()) {
            return true;
        }
        ApnSetting apnSetting = apnContext.getApnSetting();
        StringBuilder sb = new StringBuilder();
        sb.append("current default apn is ");
        sb.append(apnSetting.isPreset() ? "preset" : "non-preset");
        log(sb.toString());
        return apnSetting.isPreset();
    }

    private void isSupportPidStatistics() {
        if (!this.mDoRecoveryAddDnsProp) {
            return;
        }
        if (new File(pidStatsPath).exists()) {
            this.isSupportPidStats = true;
        } else {
            this.isSupportPidStats = false;
        }
    }

    public void setupDataOnConnectableApns(String reason, String excludedApnType) {
        log("setupDataOnConnectableApns: " + reason + ", excludedApnType = " + excludedApnType);
        this.mDcTracker.setupDataOnConnectableApns(reason, excludedApnType);
    }

    public boolean needRetryAfterDisconnected(int cause) {
        if (cause != 65535) {
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

    private boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    private boolean isCTDualModeCard(int sub) {
        int subType = HwTelephonyManagerInner.getDefault().getCardType(sub);
        if (41 != subType && 43 != subType) {
            return false;
        }
        log("sub = " + sub + ", SubType = " + subType + " is CT dual modem card");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInChina() {
        String mcc = null;
        String operatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkOperatorForPhone(this.mPhone.getPhoneId());
        if (operatorNumeric != null && operatorNumeric.length() > 3) {
            mcc = operatorNumeric.substring(0, 3);
            log("isInChina current mcc = " + mcc);
        }
        if (CHINA_OPERATOR_MCC.equals(mcc)) {
            return true;
        }
        return false;
    }

    public boolean isPingOk() {
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimOn()) {
            log("isPineOk always ok for vsim on");
            return true;
        } else if (noNeedDoRecovery() || this.mPhone.getServiceState().getDataRegState() != 0) {
            log("isPineOk always false if not default apn or dataRegState not in service");
            return false;
        } else {
            startPingThread();
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class PingThread extends Thread {
        private static final int PROCESS_STATUS_FAIL = -1;
        private static final int PROCESS_STATUS_OK = 0;
        private boolean isRecievedPingReply;

        private PingThread() {
            this.isRecievedPingReply = false;
        }

        private class PingProcessRunner extends Thread {
            private final Process process;
            private int status = -1;

            PingProcessRunner(Process process2) {
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

        /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
            return;
         */
        public void run() {
            String pingResultStr = "";
            BufferedReader buf = null;
            String serverName = "grs.dbankcloud.com";
            if (!HwDcTrackerEx.this.isInChina()) {
                serverName = "www.google.com";
            }
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("ping thread enter, server name = " + serverName);
            try {
                HwDcTrackerEx.this.log("pingThread begin to ping");
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("/system/bin/ping -c 1 -W 1 " + serverName);
                PingProcessRunner runner = new PingProcessRunner(process);
                runner.start();
                runner.join(3000);
                int status = runner.status;
                HwDcTrackerEx hwDcTrackerEx2 = HwDcTrackerEx.this;
                hwDcTrackerEx2.log("pingThread, process.waitFor, status = " + status);
                if (status == 0) {
                    buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuffer stringBuffer = new StringBuffer();
                    while (true) {
                        String line = buf.readLine();
                        if (line == null) {
                            break;
                        }
                        stringBuffer.append(line);
                    }
                    pingResultStr = stringBuffer.toString();
                }
                HwDcTrackerEx hwDcTrackerEx3 = HwDcTrackerEx.this;
                hwDcTrackerEx3.log("ping result:" + pingResultStr);
                if ((status != 0 || !pingResultStr.contains("1 packets transmitted, 1 received")) && HwDcTrackerEx.this.mDcTracker.isPhoneStateIdle()) {
                    this.isRecievedPingReply = false;
                    HwDcTrackerEx.this.mDcTracker.sendMessage(HwDcTrackerEx.this.mDcTracker.obtainMessage(270354, (Object) null));
                } else {
                    this.isRecievedPingReply = true;
                }
                HwDcTrackerEx hwDcTrackerEx4 = HwDcTrackerEx.this;
                hwDcTrackerEx4.log("pingThread return is " + this.isRecievedPingReply);
                if (buf != null) {
                    try {
                        buf.close();
                    } catch (IOException e) {
                        HwDcTrackerEx.this.loge("close buffer got IO exception.");
                    }
                }
            } catch (IOException e2) {
                HwDcTrackerEx.this.loge("ping thread IOException.");
                if (0 != 0) {
                    buf.close();
                }
            } catch (InterruptedException e3) {
                HwDcTrackerEx.this.loge("ping thread InterruptedException.");
                if (0 != 0) {
                    buf.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        buf.close();
                    } catch (IOException e4) {
                        HwDcTrackerEx.this.loge("close buffer got IO exception.");
                    }
                }
                throw th;
            }
        }
    }

    private void startPingThread() {
        log("startPingThread() enter.");
        new PingThread().start();
    }

    public void unregisterForImsiReady(IccRecordsEx r) {
        r.unregisterForImsiReady(this.mDcTracker.getHandler());
    }

    public void registerForImsiReady(IccRecordsEx r) {
        r.registerForImsiReady(this.mDcTracker.getHandler(), 270338, (Object) null);
    }

    public void unregisterForRecordsLoaded(IccRecordsEx r) {
        r.unregisterForRecordsLoaded(this.mDcTracker.getHandler());
    }

    public void registerForRecordsLoaded(IccRecordsEx r) {
        r.registerForRecordsLoaded(this.mDcTracker.getHandler(), 270338, (Object) null);
    }

    public void registerForGetAdDone(UiccCardApplicationEx newUiccApplication) {
        newUiccApplication.registerForGetAdDone(this.mDcTracker.getHandler(), 270338, (Object) null);
    }

    public void unregisterForGetAdDone(UiccCardApplicationEx newUiccApplication) {
        newUiccApplication.unregisterForGetAdDone(this.mDcTracker.getHandler());
    }

    public void registerForImsi(UiccCardApplicationEx newUiccApplication, IccRecordsEx newIccRecords) {
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN) || SETAPN_UNTIL_CARDLOADED) {
            newIccRecords.registerForRecordsLoaded(this.mDcTracker.getHandler(), 270338, (Object) null);
            return;
        }
        int appType = newUiccApplication.getType();
        if (appType == 1 || appType == 2) {
            log("New USIM records found");
            newUiccApplication.registerForGetAdDone(this.mDcTracker.getHandler(), 271144, (Object) null);
        } else if (appType == 3 || appType == 4) {
            log("New CSIM records found");
            newIccRecords.registerForImsiReady(this.mDcTracker.getHandler(), 271144, (Object) null);
        } else {
            log("New other records found");
        }
        newIccRecords.registerForRecordsLoaded(this.mDcTracker.getHandler(), 270338, (Object) null);
    }

    public boolean checkMvnoParams() {
        boolean result = false;
        String operator = getCTOperator(getOperatorNumeric());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(this.mPhone.getPhoneId()))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mPhone.getPhoneId()));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            log("checkMvnoParams: selection=" + "numeric = ?");
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, "numeric = ?", new String[]{operator}, "_id");
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
        if (!cursor.moveToFirst()) {
            return false;
        }
        do {
            String mvnoType = cursor.getString(cursor.getColumnIndexOrThrow("mvno_type"));
            String mvnoMatchData = cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data"));
            if (!TextUtils.isEmpty(mvnoType) && !TextUtils.isEmpty(mvnoMatchData)) {
                log("checkMvno: X has mvno paras");
                return true;
            }
        } while (cursor.moveToNext());
        return false;
    }

    public void handleCustMessage(Message msg) {
        HwDataSelfCure hwDataSelfCure;
        int i = msg.what;
        if (i != 271147) {
            if (i == 271149) {
                log("handleMessage-EVENT_DATA_DATA_CURE_FOR_ESM_FLAG_CHANGE!");
                if (IS_PDN_REJ_CURE_ENABLE && (hwDataSelfCure = this.mHwDataSelfCure) != null) {
                    hwDataSelfCure.esmFlagCureTimerTimeOut();
                }
            }
        } else if (this.msgReceiveCounter >= this.msgTransCounter) {
            log("already handleMessage-EVENT_GET_ATTACH_INFO_DONE, do nothing!");
        } else {
            log("handleMessage-EVENT_GET_ATTACH_INFO_DONE!");
            onGetAttachInfoDone((AsyncResult) msg.obj);
            this.msgReceiveCounter = this.msgTransCounter;
        }
    }

    public int getPrimarySlot() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    private int getSecondarySlot() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0 ? 1 : 0;
    }

    private void checkCureResultToChr(DataConnectionEx dc) {
        ApnContextEx apnContext;
        if (dc != null && (apnContext = dc.getApnContextFromCp()) != null) {
            if (this.mDcTracker.isCureApnContainsType(apnContext.getApnType())) {
                ApnCureStat.access$3608(this.mApnCureStat);
            }
        }
    }

    public void addIfacePhoneHashMap(DataConnectionEx dc, HashMap<String, Integer> map) {
        String iface;
        LinkProperties tempLinkProperties = dc.getLinkProperties();
        if (!(tempLinkProperties == null || (iface = tempLinkProperties.getInterfaceName()) == null)) {
            map.put(iface, Integer.valueOf(this.mPhone.getPhoneId()));
        }
        checkCureResultToChr(dc);
    }

    public void sendDSMipErrorBroadcast() {
        if (SystemProperties.getBoolean("ro.config.hw_mip_error_dialog", false)) {
            this.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.DATA_CONNECTION_MOBILE_IP_ERROR"));
        }
    }

    public boolean enableTcpUdpSumForDataStall() {
        return SystemProperties.getBoolean("ro.hwpp_enable_tcp_udp_sum", false);
    }

    public String networkTypeToApnType(int networkType) {
        if (networkType == 0) {
            return "default";
        }
        if (networkType == 2) {
            return "mms";
        }
        if (networkType == 3) {
            return "supl";
        }
        if (networkType == 4) {
            return "dun";
        }
        if (networkType == 5) {
            return "hipri";
        }
        if (networkType == EVENT_RADIO_OFF_OR_NOT_AVAILABLE) {
            return "ia";
        }
        if (networkType == 15) {
            return "emergency";
        }
        switch (networkType) {
            case EVENT_SET_NR_SA_STATE_DONE /*{ENCODED_INT: 10}*/:
                return "fota";
            case EVENT_GET_NR_SA_STATE_DONE /*{ENCODED_INT: 11}*/:
                return "ims";
            case 12:
                return "cbs";
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
                    case 45:
                        return "xcap";
                    default:
                        switch (networkType) {
                            case 48:
                                return "internaldefault";
                            case 49:
                                return "snssai1";
                            case RETRY_ALARM_DELAY_TIME_SHORT /*{ENCODED_INT: 50}*/:
                                return "snssai2";
                            case 51:
                                return "snssai3";
                            case 52:
                                return "snssai4";
                            case 53:
                                return "snssai5";
                            case 54:
                                return "snssai6";
                            default:
                                log("Error mapping networkType " + networkType + " to apnType");
                                return "";
                        }
                }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDataRetryEventProc(Message msg) {
        log("onDataRetryEventProc msg=" + msg.what);
        switch (msg.what) {
            case 7:
                onReenableNrAlarm(msg.getData());
                return;
            case 8:
                onDisableNr(msg.getData());
                return;
            case 9:
                onRilConnected(msg);
                return;
            case EVENT_SET_NR_SA_STATE_DONE /*{ENCODED_INT: 10}*/:
                onSetNrSaStateDone(msg);
                return;
            case EVENT_GET_NR_SA_STATE_DONE /*{ENCODED_INT: 11}*/:
                onGetNrSaStateDone(msg);
                return;
            case 12:
                onDataConnectionDetached();
                return;
            case EVENT_DATA_CONNECTED /*{ENCODED_INT: 13}*/:
                onDataConnected();
                return;
            case EVENT_RADIO_OFF_OR_NOT_AVAILABLE /*{ENCODED_INT: 14}*/:
                onRadioOffOrNotAvailable();
                return;
            case 15:
                onDataEnabledChanged(msg);
                return;
            case 16:
                onDataRatChanged();
                return;
            case EVENT_SIM_HOTPLUG /*{ENCODED_INT: 17}*/:
                onSimHotPlug(msg);
                return;
            case EVENT_DATA_CONNECTION_ATTACHED /*{ENCODED_INT: 18}*/:
                onDataAttached();
                return;
            case EVENT_DELAY_RETRY_CONNECT_DATA /*{ENCODED_INT: 19}*/:
                log("time out, try connect data" + msg.what);
                this.mDcTracker.setupDataOnAllConnectableApns("");
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoiceCallEndedHw() {
        log("onVoiceCallEndedHw");
        if (!HwModemCapability.isCapabilitySupport(0)) {
            int currentSub = this.mPhone.getSubId();
            SubscriptionController subscriptionController = SubscriptionController.getInstance();
            int defaultDataSubId = subscriptionController.getDefaultDataSubId();
            if (subscriptionController.getSubState(subscriptionController.getSlotIndex(defaultDataSubId)) == 0 && currentSub != defaultDataSubId) {
                if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
                    log("defaultDataSub " + defaultDataSubId + " is inactive, set dataSubId to " + currentSub);
                    subscriptionController.setDefaultDataSubId(currentSub);
                } else {
                    log("vsim is in process or cardreload, not set dds to " + currentSub);
                }
            }
            if (this.mPhone.getServiceStateTracker() != null) {
                this.mPhone.notifyServiceStateChanged();
            }
        }
    }

    public boolean isDataConnectivityDisabled(int slotId, String tag) {
        return HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(slotId, tag);
    }

    public boolean isRoamingPushDisabled() {
        return HwTelephonyManagerInner.getDefault().isRoamingPushDisabled();
    }

    public boolean getXcapDataRoamingEnable() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configLoader != null) {
            b = configLoader.getConfigForSubId(this.mPhone.getSubId());
        }
        boolean xcapDataRoamingEnable = false;
        if (b != null) {
            xcapDataRoamingEnable = b.getBoolean(XCAP_DATA_ROAMING_ENABLE);
        }
        log("getXcapDataRoamingEnable:xcapDataRoamingEnable " + xcapDataRoamingEnable);
        try {
            Boolean getXcapEnable = (Boolean) HwCfgFilePolicy.getValue(XCAP_DATA_ROAMING_ENABLE, SubscriptionManager.getSlotIndex(this.mPhone.getSubId()), Boolean.class);
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
        if (sIsScreenOn) {
            this.mDSUseDuration++;
            log("updateDSUseDuration: Update mDSUseDuration: " + this.mDSUseDuration);
            long curTime = SystemClock.elapsedRealtime();
            if (curTime > this.mNextReportDSUseDurationStamp) {
                this.mPhone.sendIntentDsUseStatistics(this.mDSUseDuration);
                log("updateDSUseDuration: report mDSUseDuration: " + this.mDSUseDuration);
                this.mDSUseDuration = 0;
                this.mNextReportDSUseDurationStamp = 3600000 + curTime;
            }
        }
    }

    private int getallowMmsPropertyByPlmn() {
        int allowMmsPropertyInt;
        PersistableBundle bundle;
        int allowMmsPropertyInt2 = -1;
        try {
            int subId = this.mPhone.getSubId();
            Integer hwAlwaysAllowMms = (Integer) HwCfgFilePolicy.getValue(ALLOW_MMS_PROPERTY_INT, SubscriptionManager.getSlotIndex(subId), Integer.class);
            if (hwAlwaysAllowMms != null) {
                allowMmsPropertyInt = hwAlwaysAllowMms.intValue();
            } else {
                CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
                if (configLoader == null || (bundle = configLoader.getConfigForSubId(subId)) == null) {
                    return -1;
                }
                allowMmsPropertyInt = bundle.getInt(ALLOW_MMS_PROPERTY_INT, -1);
            }
            if (allowMmsPropertyInt >= 0) {
                if (allowMmsPropertyInt <= 7) {
                    allowMmsPropertyInt2 = allowMmsPropertyInt;
                    log("getallowMmsPropertyByPlmn:allowMmsPropertyInt " + allowMmsPropertyInt2);
                    return allowMmsPropertyInt2;
                }
            }
            allowMmsPropertyInt2 = -1;
            log("getallowMmsPropertyByPlmn:allowMmsPropertyInt " + allowMmsPropertyInt2);
        } catch (Exception e) {
            log("Exception: read allow_mms_property_int failed");
        }
        return allowMmsPropertyInt2;
    }

    public boolean getAttachedStatus(boolean attached) {
        int dataSub;
        if ((PhoneFactory.IS_QCOM_DUAL_LTE_STACK || DISABLE_GW_PS_ATTACH) && (dataSub = SubscriptionManager.getDefaultDataSubscriptionId()) != this.mPhone.getSubId() && SubscriptionManager.isUsableSubIdValue(dataSub)) {
            return true;
        }
        return attached;
    }

    public boolean isBtConnected() {
        return this.mIsBtConnected;
    }

    public boolean isWifiConnected() {
        NetworkInfo activeNetworkInfo;
        ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
        if (cm == null || (activeNetworkInfo = cm.getActiveNetworkInfo()) == null || activeNetworkInfo.getType() != 1) {
            return false;
        }
        log("isWifiConnected return true");
        return true;
    }

    public boolean isDataNeededWithWifiAndBt() {
        boolean isDataAlwaysOn = Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "mobile_data_always_on", 0) != 0;
        if (isDataAlwaysOn) {
            log("isDataNeededWithWifiAndBt:isDataAlwaysOn = true");
        }
        return isDataAlwaysOn || (!isBtConnected() && !isWifiConnected());
    }

    public void updateLastRadioResetTimestamp() {
        this.mLastRadioResetTimestamp = SystemClock.elapsedRealtime();
    }

    public boolean needRestartRadioOnError(ApnContextEx apnContext, DcFailCauseExt cause) {
        TelephonyManager tm = TelephonyManager.getDefault();
        if (apnContext == null || tm == null) {
            return false;
        }
        long now = SystemClock.elapsedRealtime();
        int current4gSlotId = getPrimarySlot();
        if ("default".equals(apnContext.getApnType()) && tm.getCallState() == 0 && current4gSlotId == this.mPhone.getPhoneId() && PERMANENT_ERROR_HEAL_PROP) {
            long j = this.mLastRadioResetTimestamp;
            if ((now - j > 14400000 || 0 == j) && apnContext.restartOnError(cause.getErrorCode()) && this.mDcTracker.getTransportType() == 1) {
                log("needRestartRadioOnError return true");
                this.mLastRadioResetTimestamp = now;
                return true;
            }
        }
        return false;
    }

    private boolean shouldUseAttachApn(String expectApnType) {
        if (!"default".equals(expectApnType) || this.mAttachedApnSettings == null) {
            return false;
        }
        if (this.mEhplmnApnSettings == null) {
            return true;
        }
        return checkApnName();
    }

    private boolean checkApnName() {
        int size = this.mEhplmnApnSettings.size();
        for (int i = 0; i < size; i++) {
            if (this.mAttachedApnSettings.getApnName().equalsIgnoreCase(this.mEhplmnApnSettings.get(i).getApnName())) {
                return false;
            }
        }
        return true;
    }

    private void addToApnList(ArrayList<ApnSetting> apnList, ApnSetting curApnSetting) {
        if (apnList != null && !apnList.contains(curApnSetting)) {
            apnList.add(curApnSetting);
        }
    }

    /* access modifiers changed from: protected */
    public void apnSelectAndAdd(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        log("apnSelectAndAdd, type:" + requestedApnType + ", tech:" + radioTech + ", operator" + operator);
        if (shouldUseAttachApn(requestedApnType)) {
            checkAttachApnAndAdd(requestedApnType, radioTech, apnList, operator);
        } else {
            checkEhplmnApnAndAdd(requestedApnType, radioTech, apnList);
        }
    }

    private void checkEhplmnApnAndAdd(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList) {
        int size = this.mEhplmnApnSettings.size();
        boolean isEhplmnUsed = false;
        for (int i = 0; i < size; i++) {
            ApnSetting apnSetting = this.mEhplmnApnSettings.get(i);
            if (apnSetting.canHandleType(ApnSetting.getApnTypesBitmaskFromString(requestedApnType)) && ServiceState.bitmaskHasTech(apnSetting.getNetworkTypeBitmask(), radioTech)) {
                log("use ehplmn apn:" + apnSetting);
                isEhplmnUsed = true;
                addToApnList(apnList, apnSetting);
            }
        }
        if (isEhplmnUsed) {
            ApnCureStat.access$4108(this.mApnCureStat);
        } else if (this.mApnCureStat.mFailReason == 1) {
            ApnCureStat.access$4308(this.mApnCureStat);
        }
    }

    private void checkAttachApnAndAdd(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        if (this.mAttachedApnSettings.getOperatorNumeric() != null && this.mAttachedApnSettings.getOperatorNumeric().equals(operator) && this.mAttachedApnSettings.canHandleType(ApnSetting.getApnTypesBitmaskFromString(requestedApnType)) && ServiceState.bitmaskHasTech(this.mAttachedApnSettings.getNetworkTypeBitmask(), radioTech)) {
            log("use attach apn:" + this.mAttachedApnSettings);
            addToApnList(apnList, this.mAttachedApnSettings);
            ApnCureStat.access$4408(this.mApnCureStat);
        }
    }

    /* access modifiers changed from: protected */
    public void onGetAttachInfoDone(AsyncResult ar) {
        log("onGetAttachInfoDoneX");
        LteAttachInfo stAttachInfo = null;
        if (ar.exception != null) {
            log("Exception occurred, failed to report the LTE attach info");
        } else {
            stAttachInfo = (LteAttachInfo) ar.result;
            if (stAttachInfo == null) {
                log("onGetAttachInfoDone::(LteAttachInfo)ar.result is null.");
            } else {
                boolean isValidApn = true;
                if (!isValidLteApn(stAttachInfo.apn)) {
                    log("onGetAttachInfoDone::stAttachInfo.apn is inValid Lte Apn.");
                    isValidApn = false;
                }
                if (!isValidProtocol(stAttachInfo.protocol)) {
                    log("onGetAttachInfoDone::stAttachInfo.protocol is inValid protocol.");
                    isValidApn = false;
                }
                if (!isValidApn) {
                    stAttachInfo = null;
                }
            }
        }
        ApnSetting attachedApnSettings = null;
        if (stAttachInfo != null) {
            attachedApnSettings = convertHalAttachInfo(stAttachInfo);
        }
        this.isAttachedApnRequested = true;
        if (attachedApnSettings != null) {
            this.mAttachedApnSettings = attachedApnSettings;
            saveLteAttachApnInfoToSp(this.mAttachedApnSettings);
            this.mDcTracker.setupDataOnAllConnectableApns("");
            log("attachedApnSetting: " + this.mAttachedApnSettings);
            this.mDcTracker.sendIntentWhenApnNeedReport(this.mPhone, attachedApnSettings, 1024, new LinkProperties());
            return;
        }
        this.mDcTracker.setupDataOnAllConnectableApns("");
        this.mAttachedApnSettings = null;
        log("attachedApnSetting is null!");
    }

    /* access modifiers changed from: protected */
    public ApnSetting convertHalAttachInfo(LteAttachInfo attachInfo) {
        log("convertHalAttachInfo");
        String apn = attachInfo.apn;
        String protocol = covertProtocol(attachInfo.protocol);
        String[] strArr = {"default"};
        String operator = getOperatorNumeric();
        log("convertHalAttachInfo, operator is:" + operator);
        ApnSetting.Builder builder = new ApnSetting.Builder();
        builder.setOperatorNumeric(operator).setApnName(apn).setEntryName("default lte apn").setProxyAddress("").setProxyPort(0).setMmsProxyAddress("").setMmsProxyPort(0).setUser("").setPassword("").setAuthType(-1).setApnTypeBitmask(EVENT_SIM_HOTPLUG).setProtocol(ApnSetting.getProtocolIntFromString(protocol)).setRoamingProtocol(ApnSetting.getProtocolIntFromString(protocol)).setCarrierEnabled(true).setModemCognitive(false);
        return builder.build();
    }

    private boolean notEmptyStr(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return true;
    }

    private String dctGetImsi() {
        IccRecordsEx r = this.mPhone.getIccRecords();
        if (r != null) {
            return r.getIMSI();
        }
        return null;
    }

    private String dctGetIccid() {
        IccRecordsEx r = this.mPhone.getIccRecords();
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    private void saveLteAttachApnInfoToSp(ApnSetting apn) {
        if (apn != null) {
            ApnSetting savedLteAttachApn = getLteAttachApnInfoFromSp();
            String imsi = dctGetImsi();
            String rplmn = TelephonyManager.getDefault().getServiceStateForSubscriber(this.mPhone.getSubId()).getDataOperatorNumeric();
            if (isValidKey(imsi, rplmn)) {
                if (savedLteAttachApn == null || !apn.equals(savedLteAttachApn)) {
                    SharedPreferences.Editor editor = this.mPhone.getContext().getSharedPreferences("lte_attach_apn", 0).edit();
                    editor.putString(imsi, rplmn + SEPARATOR_KEY + apn.getApnName() + SEPARATOR_KEY + apn.getProtocol());
                    editor.commit();
                }
            }
        }
    }

    private boolean isValidKey(String imsi, String rplmn) {
        return notEmptyStr(imsi) && isValidPlmn(rplmn);
    }

    private ApnSetting getLteAttachApnInfoFromSp() {
        return getLteAttachApnInfoFromSp(dctGetImsi());
    }

    private ApnSetting getLteAttachApnInfoFromSp(String imsi) {
        log("getLteAttachApnInfoFromSp");
        if (!notEmptyStr(imsi)) {
            return null;
        }
        String apnInfo = this.mPhone.getContext().getSharedPreferences("lte_attach_apn", 0).getString(imsi, "");
        if (notEmptyStr(apnInfo)) {
            return fillAttachInfo(apnInfo);
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0054, code lost:
        if (r7.equals("IP") != false) goto L_0x0058;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0063  */
    private ApnSetting fillAttachInfo(String apnInfo) {
        String[] info = apnInfo.split(SEPARATOR_KEY);
        boolean z = false;
        String apnRplmn = info[0];
        String apn = info[1];
        String proto = info[2];
        if (!isPlmnInEhplmn(TelephonyManager.getDefault().getServiceStateForSubscriber(this.mPhone.getSubId()).getDataOperatorNumeric()) || !isPlmnInEhplmn(apnRplmn)) {
            return null;
        }
        LteAttachInfo attachInfo = new LteAttachInfo();
        attachInfo.apn = apn;
        int hashCode = proto.hashCode();
        if (hashCode != 2343) {
            if (hashCode == 2254343 && proto.equals("IPV6")) {
                z = true;
                if (z) {
                    attachInfo.protocol = 1;
                } else if (!z) {
                    attachInfo.protocol = 3;
                } else {
                    attachInfo.protocol = 2;
                }
                return convertHalAttachInfo(attachInfo);
            }
        }
        z = true;
        if (z) {
        }
        return convertHalAttachInfo(attachInfo);
    }

    private boolean isHkGcCardForApnCure(int phoneId) {
        String operator = getCTOperator(getOperatorNumeric());
        IccRecordsEx record = this.mPhone.getIccRecords();
        String preSpn = record != null ? record.getServiceProviderName() : "";
        String preIccid = SystemProperties.get("gsm.sim.preiccid_" + phoneId, "");
        if (!"46003".equals(operator) && isValidPlmn(operator)) {
            return false;
        }
        if (!"8985231".equals(preIccid) && !GC_SPN.equals(preSpn)) {
            return false;
        }
        log("is Hongkong GC card");
        this.mApnCureStat.mFailReason = 7;
        return true;
    }

    private void getVirtualNetPlmns() {
        if (this.mVirtualNetPlmnList.size() <= 0) {
            File custFile = HwCfgFilePolicy.getCfgFile(CUST_FILE_PATH, 0);
            if (custFile == null || !custFile.exists()) {
                log("siminfo mapping file not exists");
                return;
            }
            Map map = HwCarrierConfigXmlParse.parseFile(custFile);
            if (map == null || map.isEmpty() || !map.containsKey("carrier")) {
                log("siminfo mapping is invalid");
            } else {
                fillVirtualNetPlmnList(map);
            }
        }
    }

    private void fillVirtualNetPlmnList(Map map) {
        Set<String> mixVnet = new HashSet<>(0);
        HashMap<String, Integer> multiOpkeyVnet = new HashMap<>();
        for (Map<String, String> rule : (List) map.get("carrier")) {
            String mccmnc = rule.get(CONFIG_XML_KEYNAME);
            String opKey = rule.get(CONFIG_XML_OPKEYNAME);
            if (mccmnc != null) {
                if (mccmnc.equals(opKey)) {
                    mixVnet.add(mccmnc);
                }
                if (multiOpkeyVnet.containsKey(mccmnc)) {
                    multiOpkeyVnet.put(mccmnc, Integer.valueOf(multiOpkeyVnet.get(mccmnc).intValue() + 1));
                } else {
                    multiOpkeyVnet.put(mccmnc, 1);
                }
            }
        }
        for (String key : mixVnet) {
            if (multiOpkeyVnet.containsKey(key) && multiOpkeyVnet.get(key).intValue() > 1) {
                this.mVirtualNetPlmnList.add(key);
            }
        }
    }

    private void getRoamingBrokerPlmns() {
        String roamingBrokeConfig;
        if (this.mRoamingbrokePlmnList.size() <= 0 && (roamingBrokeConfig = Settings.System.getString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "roamingBrokerSequenceList")) != null) {
            fillRoamingBrokerPlmnList(roamingBrokeConfig);
        }
    }

    private void fillRoamingBrokerPlmnList(String roamingBrokeConfig) {
        String[] configList = roamingBrokeConfig.split("\\|");
        for (String config : configList) {
            if (config != null) {
                String[] parts = config.split(",");
                if (parts.length >= 3) {
                    String beforeRbPlmn = parts[1];
                    String afterRbPlmn = parts[2];
                    if (!beforeRbPlmn.isEmpty()) {
                        this.mRoamingbrokePlmnList.add(beforeRbPlmn);
                    }
                    if (!afterRbPlmn.isEmpty()) {
                        this.mRoamingbrokePlmnList.add(afterRbPlmn);
                    }
                }
            }
        }
    }

    private boolean checkPlmnList(HashSet<String> plmnList, String[] plmns) {
        if (plmnList.size() == 0) {
            return true;
        }
        for (String plmn : plmns) {
            if (plmnList.contains(plmn)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInVirtualNet(String[] plmns) {
        getVirtualNetPlmns();
        boolean isVnet = checkPlmnList(this.mVirtualNetPlmnList, plmns);
        if (isVnet) {
            this.mApnCureStat.mFailReason = 4;
        }
        return isVnet;
    }

    private boolean isInRoamingBroker(String[] plmns) {
        boolean isRoamBroker;
        String iccid = dctGetIccid();
        if (iccid == null || iccid.startsWith("8985231")) {
            getRoamingBrokerPlmns();
            isRoamBroker = checkPlmnList(this.mRoamingbrokePlmnList, plmns);
        } else {
            isRoamBroker = false;
        }
        if (isRoamBroker) {
            this.mApnCureStat.mFailReason = 5;
        }
        return isRoamBroker;
    }

    private boolean checkConditionForApnCure(int phoneId) {
        if (!IS_ENABLE_APNCURE || isHkGcCardForApnCure(phoneId)) {
            return false;
        }
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (!VSimUtilsInner.isVSimSub(phoneId) && !HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(phoneId))) {
                return true;
            }
            this.mApnCureStat.mFailReason = 6;
            return false;
        } else if (!VSimUtilsInner.isVSimSub(phoneId) && !HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            return true;
        } else {
            this.mApnCureStat.mFailReason = 6;
            return false;
        }
    }

    private static boolean isValidPlmn(String plmn) {
        if (plmn == null || plmn.length() < 5) {
            return false;
        }
        return true;
    }

    private String[] getOrderedSimEhplmn() {
        IccRecordsEx record = UiccControllerExt.getInstance().getIccRecords(SubscriptionManager.getSlotIndex(this.mPhone.getSubId()), 1);
        return record != null ? record.getEhplmnOfSim() : EmptyArray.STRING;
    }

    private boolean isPlmnsInBlackList(String[] plmns) {
        List<String> disabledMccList = Arrays.asList(APNCURE_BLACK_MCC_LIST.split(","));
        List<String> disabledPlmnList = Arrays.asList(APNCURE_BLACK_PLMN_LIST.split(","));
        for (String plmn : plmns) {
            if (isValidPlmn(plmn)) {
                boolean isMccDisabled = disabledMccList == null ? false : disabledMccList.contains(plmn.substring(0, 3));
                boolean isPlmnDisabled = disabledPlmnList == null ? false : disabledPlmnList.contains(plmn);
                if (isMccDisabled || isPlmnDisabled) {
                    this.mApnCureStat.mFailReason = 3;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean plmnsCheck(String[] plmns) {
        if (isPlmnsInBlackList(plmns) || isInVirtualNet(plmns) || isInRoamingBroker(plmns)) {
            return false;
        }
        return true;
    }

    private boolean isPlmnInEhplmn(String plmn) {
        List<String> ehplmnList = Arrays.asList(getOrderedSimEhplmn());
        if (ehplmnList == null) {
            return false;
        }
        return ehplmnList.contains(plmn);
    }

    private boolean isPlmnInEhplmn(String plmn, String[] ehplmn) {
        List<String> ehplmnList = Arrays.asList(ehplmn);
        if (ehplmnList == null) {
            return false;
        }
        return ehplmnList.contains(plmn);
    }

    private boolean isInChinaArea(String[] ehplmn) {
        String rplmn = TelephonyManager.getDefault().getServiceStateForSubscriber(this.mPhone.getSubId()).getDataOperatorNumeric();
        log("isInChinaArea, rplmn:" + rplmn);
        if (!isValidPlmn(rplmn) || !rplmn.substring(0, 3).equals(CHINA_OPERATOR_MCC) || !isPlmnInEhplmn(rplmn, ehplmn)) {
            return false;
        }
        return true;
    }

    private boolean canDoApnCure(int phoneId, String[] ehplmns, String hplmn) {
        if (checkConditionForApnCure(phoneId) && plmnsCheck(ehplmns) && !isHplmnNotForApnCure(ehplmns, hplmn)) {
            return true;
        }
        log("condition not suit");
        return false;
    }

    private boolean isHplmnNotForApnCure(String[] ehplmns, String hplmn) {
        String[] plmnArray = {hplmn};
        if (isValidPlmn(hplmn)) {
            if (!plmnsCheck(plmnArray)) {
                log("hplmn valid, but not suit");
                return true;
            }
        } else if (!isInChinaArea(ehplmns)) {
            log("hplmn invalid, but not suit");
            return true;
        }
        return false;
    }

    private void getEhplmnApn(String[] ehplmnList, String hplmn) {
        int subId = this.mPhone.getSubId();
        int phoneId = this.mPhone.getPhoneId();
        if (ehplmnList != null && canDoApnCure(phoneId, ehplmnList, hplmn)) {
            boolean isFoundApn = false;
            for (String operator : ehplmnList) {
                if (!isFoundApn) {
                    String selection = "numeric = '" + operator + "'";
                    log("getEhplmnApn: selection=" + selection);
                    Cursor cursor = getCursor(selection, Long.toString((long) subId));
                    if (cursor != null) {
                        this.mEhplmnApnSettings.clear();
                        while (cursor.moveToNext()) {
                            this.mEhplmnApnSettings.add(ApnSetting.makeApnSetting(cursor));
                            isFoundApn = true;
                        }
                        cursor.close();
                    }
                } else {
                    return;
                }
            }
        }
    }

    private Cursor getCursor(String selection, String strSubId) {
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return this.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, selection, null, "_id");
        }
        DcTrackerEx dcTrackerEx = this.mDcTracker;
        return this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(DcTrackerEx.MSIM_TELEPHONY_CARRIERS_URI, strSubId), null, selection, null, "_id");
    }

    private String covertProtocol(int protocol) {
        log("covertProtocol - protocol is:" + protocol);
        if (protocol == 1) {
            return "IP";
        }
        if (protocol == 2) {
            return "IPV6";
        }
        if (protocol == 3) {
            return "IPV4V6";
        }
        log("unknown protocol = " + protocol);
        return "unknown";
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

    private boolean isChinaUnicomCard() {
        int subId = this.mPhone.getSubId();
        String rplmn = TelephonyManager.getDefault().getServiceStateForSubscriber(subId).getDataOperatorNumeric();
        if (TelephonyManager.getDefault().getServiceStateForSubscriber(subId).getDataRoaming() || !Arrays.asList(CHINAUNICOM_PLMN).contains(rplmn)) {
            return false;
        }
        return true;
    }

    private Bundle buildBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("AttachSuccCnt", this.mApnCureStat.mAttachSuccCnt);
        bundle.putInt("EhplmnSuccCnt", this.mApnCureStat.mEhplmnSuccCnt);
        bundle.putInt("EnabledFailCnt", this.mApnCureStat.mEnabledFailCnt);
        bundle.putInt("SetupSuccCnt", this.mApnCureStat.mSetupSuccCnt);
        bundle.putByte("FailReason", (byte) this.mApnCureStat.mFailReason);
        bundle.putString("SimOperator", this.mApnCureStat.simOperator);
        return bundle;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendApnCureResult() {
        Context context = this.mPhone.getContext();
        if (context == null) {
            log("context should not be null");
        } else if (this.mApnCureStat.mAttachSuccCnt + this.mApnCureStat.mEhplmnSuccCnt + this.mApnCureStat.mEnabledFailCnt != 0) {
            Intent intent = new Intent("com.huawei.android.chr.action.ACTION_REPORT_CHR");
            intent.setFlags(67108864);
            intent.setPackage(CHR_PACKAGE_NAME);
            intent.putExtra("module_id", MODULE_ID_SMART_CURE);
            intent.putExtra("fault_id", FAULT_ID_SMART_CURE_APN_CURE);
            intent.putExtra("sub_id", UiccControllerExt.getInstance().getSlotIdFromPhoneId(this.mPhone.getPhoneId()));
            intent.putExtra("chr_data", buildBundle());
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.android.permission.GET_CHR_DATA");
            this.mApnCureStat.resetAll();
            this.mStartCureTime = SystemClock.elapsedRealtime();
        }
    }

    private void checkTimeAndSendResult() {
        if (SystemClock.elapsedRealtime() - this.mStartCureTime >= HICURE_INFORM_BLOCKTIME) {
            sendApnCureResult();
        }
    }

    private void getLteAttachedApnSettings(String operator) {
        if (!isPlmnsInBlackList(new String[]{operator})) {
            this.mAttachedApnSettings = getLteAttachApnInfoFromSp();
        } else {
            this.mAttachedApnSettings = null;
        }
    }

    public void updateApnLists(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        log("updateApnLists radioTech:" + radioTech + ",operator:" + operator);
        List<ApnSetting> apnSettings = this.mDcTracker.getAllApnList();
        if ((this.mIsApnCureEnabled || !apnSettings.isEmpty()) && IS_ENABLE_APNCURE) {
            String[] ehplmn = getOrderedSimEhplmn();
            if (!isLTENetworks()) {
                log("Currently is not over LTE network");
                getLteAttachedApnSettings(operator);
            } else {
                log("Currently is over LTE network");
                if (this.mAttachedApnSettings == null && !this.isAttachedApnRequested) {
                    getAttachedApnSettings();
                    return;
                }
            }
            this.isAttachedApnRequested = false;
            boolean isEhplmnReceived = true;
            if (ehplmn == null || ehplmn.length == 0) {
                isEhplmnReceived = false;
            }
            if (isEhplmnReceived || "LOADED".equals(this.mSimState)) {
                if (!isEhplmnReceived && isChinaUnicomCard()) {
                    ehplmn = CHINAUNICOM_PLMN;
                    isEhplmnReceived = true;
                }
                getEhplmnApn(ehplmn, operator);
                if (!isEhplmnReceived) {
                    this.mApnCureStat.mFailReason = 2;
                }
                this.mApnCureStat.simOperator = operator;
                apnSelectAndAdd(requestedApnType, radioTech, apnList, operator);
                if (!apnList.isEmpty()) {
                    this.mDcTracker.addCureApnSettings(requestedApnType, apnList);
                }
                checkTimeAndSendResult();
                return;
            }
            log("ehplmn is empty or sim not loaded");
        }
    }

    public void getAttachedApnSettings() {
        log("getAttachedApnSettings");
        Message result = this.mDcTracker.obtainMessage(271147, (Object) null);
        this.msgTransCounter++;
        CommandsInterfaceEx ci = this.mPhone.getCi();
        if (ci != null) {
            ci.getAttachedApnSettings(result);
        }
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
        if (this.mPhone.getPhoneType() == 2 && SystemProperties.getBoolean("hw.dct.psrecovery", false)) {
            this.mPdpResetAlarmTag++;
            log("startPdpResetAlarm for CDMA: tag=" + this.mPdpResetAlarmTag + " delay=" + (delay / WAIT_BOOSTER_TCP_STAISTICAL_RESULT_TIMER_MS) + "s");
            Intent intent = new Intent(INTENT_PDP_RESET_ALARM);
            intent.putExtra(PDP_RESET_ALARM_TAG_EXTRA, this.mPdpResetAlarmTag);
            log("startPdpResetAlarm: delay=" + delay + " action=" + intent.getAction());
            this.mPdpResetAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delay), this.mPdpResetAlarmIntent);
        }
    }

    public void stopPdpResetAlarm() {
        boolean cdmaPsRecoveryEnabled = false;
        if (this.mPhone.getPhoneType() == 2 && SystemProperties.getBoolean("hw.dct.psrecovery", false)) {
            cdmaPsRecoveryEnabled = true;
        }
        if (cdmaPsRecoveryEnabled) {
            log("stopPdpResetAlarm: current tag=" + this.mPdpResetAlarmTag + " mPdpResetAlarmIntent=" + this.mPdpResetAlarmIntent);
            this.mPdpResetAlarmTag = this.mPdpResetAlarmTag + 1;
            PendingIntent pendingIntent = this.mPdpResetAlarmIntent;
            if (pendingIntent != null) {
                this.mAlarmManager.cancel(pendingIntent);
                this.mPdpResetAlarmIntent = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentPdpResetAlarm(Intent intent) {
        log("onActionIntentPdpResetAlarm: action=" + intent.getAction());
        int tag = intent.getIntExtra(PDP_RESET_ALARM_TAG_EXTRA, 0);
        if (this.mPdpResetAlarmTag != tag) {
            log("onPdpRestAlarm: ignore, tag=" + tag + " expecting " + this.mPdpResetAlarmTag);
            return;
        }
        this.mDcTracker.cleanUpAllConnections("pdpReset");
    }

    public ApnSetting getApnForCT() {
        if (!isCTSimCard(this.mPhone.getPhoneId()) || this.mDcTracker.getAllApnList() == null || this.mDcTracker.getAllApnList().isEmpty()) {
            return null;
        }
        if (getSecondarySlot() == this.mPhone.getPhoneId() && !isCTDualModeCard(getSecondarySlot())) {
            log("getApnForCT otherslot == mPhone.getPhoneId() && !isCTDualModeCard(otherslot)");
            return null;
        } else if (this.mPhone.getServiceState().getOperatorNumeric() == null) {
            log("getApnForCT mPhone.getServiceState().getOperatorNumeric() == null");
            return null;
        } else {
            ApnSetting preferredApn = this.mDcTracker.getPreferredApn();
            if (preferredApn != null && !isApnPreset(preferredApn)) {
                return null;
            }
            this.mCurrentState = getCurState();
            int matchApnId = matchApnId(this.mCurrentState);
            if (-1 != matchApnId) {
                this.mDcTracker.setPreferredApn(matchApnId);
                return null;
            }
            int i = this.mCurrentState;
            if (i != 0) {
                if (i == 1 || i == 2 || i == 3) {
                    return setApnForCT(CT_ROAMING_APN_PREFIX);
                }
                if (i == 4 || i == 5) {
                    return setApnForCT(CT_LTE_APN_PREFIX);
                }
                return null;
            } else if (isCTCardForFullNet()) {
                return setApnForCT(CT_ROAMING_APN_PREFIX);
            } else {
                return setApnForCT(CT_NOT_ROAMING_APN_PREFIX);
            }
        }
    }

    private ApnSetting setApnForCT(String apn) {
        if (apn == null || "".equals(apn)) {
            return null;
        }
        ContentResolver resolver = this.mPhone.getContext().getContentResolver();
        if (this.mDcTracker.getAllApnList() == null || this.mDcTracker.getAllApnList().isEmpty() || resolver == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        Uri uri = Uri.withAppendedPath(DcTracker.PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
        int apnSize = this.mDcTracker.getAllApnList().size();
        for (int i = 0; i < apnSize; i++) {
            ApnSetting dp = (ApnSetting) this.mDcTracker.getAllApnList().get(i);
            int bearer = ServiceState.convertNetworkTypeBitmaskToBearerBitmask(dp.getNetworkTypeBitmask());
            if (apn.equals(dp.getApnName()) && dp.canHandleType(EVENT_SIM_HOTPLUG) && (((!isCTLteNetwork() && !isNRNetwork()) || (bearer != 0 && (dp.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(EVENT_DATA_CONNECTED)) || dp.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(EVENT_RADIO_OFF_OR_NOT_AVAILABLE))))) && (isCTLteNetwork() || isNRNetwork() || bearer == 0 || (!dp.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(EVENT_DATA_CONNECTED)) && !dp.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(EVENT_RADIO_OFF_OR_NOT_AVAILABLE)))))) {
                resolver.delete(uri, null, null);
                values.put(APN_ID, Integer.valueOf(dp.getId()));
                resolver.insert(uri, values);
                return dp;
            }
        }
        return null;
    }

    private int getCurState() {
        int currentStatus = -1;
        if (isCTLteNetwork()) {
            currentStatus = 4;
        } else if (isNRNetwork()) {
            currentStatus = 5;
        } else if (this.mPhone.getPhoneType() == 2) {
            currentStatus = TelephonyManager.getDefault().isNetworkRoaming(getPrimarySlot()) ? 1 : 0;
        } else if (this.mPhone.getPhoneType() == 1) {
            int current4gSlotId = getPrimarySlot();
            int current2gSlotId = getSecondarySlot();
            int current4gSubId = SubscriptionController.getInstance().getSubIdUsingPhoneId(current4gSlotId);
            int current2gSubId = SubscriptionController.getInstance().getSubIdUsingPhoneId(current2gSlotId);
            if (current4gSubId == this.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(current4gSubId)) {
                currentStatus = 2;
            } else if (current2gSubId == this.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(current2gSubId)) {
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
        return isCTSimCard(this.mPhone.getPhoneId());
    }

    private int matchApnId(int sign) {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        int matchId = -1;
        if (IS_MULTI_SIM_ENABLED) {
            preferredApnIdSlot = PREFERRED_APN_ID + (getPrimarySlot() == this.mPhone.getPhoneId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String LastApnId = Settings.System.getString(cr, preferredApnIdSlot);
            log("MatchApnId:LastApnId: " + LastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (LastApnId != null) {
                String[] ApId = LastApnId.split(",");
                if (5 != ApId.length || ApId[this.mCurrentState] == null) {
                    Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                } else if (!"0".equals(ApId[this.mCurrentState])) {
                    matchId = Integer.parseInt(ApId[this.mCurrentState]);
                }
            } else {
                Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
            }
        } catch (Exception e) {
            log("MatchApnId got exception");
            Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        }
        return matchId;
    }

    public boolean isLTENetworks() {
        int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        log("dataRadioTech = " + dataRadioTech);
        return ServiceState.isLte(dataRadioTech);
    }

    public boolean isCTLteNetwork() {
        int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        log("dataRadioTech = " + dataRadioTech);
        if (dataRadioTech == EVENT_DATA_CONNECTED || dataRadioTech == EVENT_RADIO_OFF_OR_NOT_AVAILABLE) {
            return true;
        }
        return false;
    }

    public boolean isNRNetwork() {
        if (this.mPhone.getServiceState() == null) {
            return false;
        }
        int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        log("isNRNetwork dataRadioTech = " + dataRadioTech);
        if (dataRadioTech == 20) {
            return true;
        }
        return false;
    }

    public void updateApnId() {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        if (IS_MULTI_SIM_ENABLED) {
            preferredApnIdSlot = PREFERRED_APN_ID + (getPrimarySlot() == this.mPhone.getPhoneId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String lastApnId = Settings.System.getString(cr, preferredApnIdSlot);
            this.mCurrentState = getCurState();
            log("updateApnId:LastApnId: " + lastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (lastApnId != null) {
                String[] apIds = lastApnId.split(",");
                ApnSetting curPreferApn = this.mDcTracker.getPreferredApn();
                StringBuffer temApnId = new StringBuffer();
                if (5 != apIds.length || apIds[this.mCurrentState] == null) {
                    Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                } else {
                    if (curPreferApn == null) {
                        log("updateApnId:CurPreApn: CurPreApn == null");
                        apIds[this.mCurrentState] = "0";
                    } else {
                        log("updateApnId:CurPreApn: " + curPreferApn + ", CurPreApnId: " + Integer.toString(curPreferApn.getId()));
                        apIds[this.mCurrentState] = Integer.toString(curPreferApn.getId());
                    }
                    for (int i = 0; i < apIds.length; i++) {
                        temApnId.append(apIds[i]);
                        if (i != apIds.length - 1) {
                            temApnId.append(",");
                        }
                    }
                    Settings.System.putString(cr, preferredApnIdSlot, temApnId.toString());
                }
                return;
            }
            Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        } catch (Exception e) {
            log("updateApnId got exception");
            Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        }
    }

    public boolean needSetCTProxy(ApnSetting apn) {
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            return false;
        }
        String networkOperatorNumeric = this.mPhone.getServiceState().getOperatorNumeric();
        if (apn == null || apn.getApnName() == null || !apn.getApnName().contains(CT_NOT_ROAMING_APN_PREFIX) || !"46012".equals(networkOperatorNumeric)) {
            return false;
        }
        return true;
    }

    public void setCtProxy(DataConnectionEx dc) {
        try {
            dc.setLinkPropertiesHttpProxy(new ProxyInfo("10.0.0.200", Integer.parseInt("80"), "127.0.0.1"));
        } catch (NumberFormatException e) {
            log("onDataSetupComplete: NumberFormatException making ProxyProperties for CT.");
        }
    }

    private void onVpStatusChanged(AsyncResult ar) {
        log("onVpStatusChanged");
        if (ar.exception != null) {
            log("Exception occurred, failed to report the rssi and ecio.");
            return;
        }
        int vpStatus = ((Integer) ar.result).intValue();
        this.mDcTracker.setVpStatus(vpStatus);
        log("onVpStatusChanged, mVpStatus:" + vpStatus);
        if (vpStatus == 1) {
            onVpStarted();
        } else {
            onVpEnded();
        }
    }

    private void onVpStarted() {
        log("onVpStarted");
        ServiceStateTrackerEx sstEx = this.mPhone.getServiceStateTracker();
        if (sstEx != null) {
            sstEx.setCurrent3GPsCsAllowed(false);
            if (this.mDcTracker.isConnected() && !sstEx.isConcurrentVoiceAndDataAllowed() && this.mInVoiceCall) {
                log("onVpStarted stop polling");
                this.mDcTracker.stopNetStatPoll();
                this.mDcTracker.stopDataStallAlarm();
                this.mDcTracker.notifyDataConnection();
            }
        }
    }

    public void onVpEnded() {
        log("onVpEnded");
        if (!this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(true);
            if (this.mDcTracker.isConnected() && this.mInVoiceCall) {
                this.mDcTracker.startNetStatPoll();
                this.mDcTracker.startDataStallAlarm(false);
                if (!this.mDcTracker.isDataEnabled()) {
                    this.mDcTracker.cleanUpAllConnections("dataDisabledInternal");
                } else {
                    this.mDcTracker.notifyDataConnection();
                }
            }
        }
    }

    public ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String requestedApnType, int radioTech) {
        ArrayList<ApnSetting> apnList = new ArrayList<>();
        List<ApnSetting> apnSettings = this.mDcTracker.getAllApnList();
        if (apnSettings != null && !apnSettings.isEmpty()) {
            int apnSize = this.mDcTracker.getAllApnList().size();
            for (int i = 0; i < apnSize; i++) {
                ApnSetting apn = (ApnSetting) this.mDcTracker.getAllApnList().get(i);
                int bearerBitmask = ServiceState.convertNetworkTypeBitmaskToBearerBitmask(apn.getNetworkTypeBitmask());
                if (apn.canHandleType(ApnSetting.getApnTypesBitmaskFromString(requestedApnType)) && ((!isCTLteNetwork() && !isNRNetwork() && ServiceState.bitmaskHasTech(bearerBitmask, radioTech)) || ((isCTLteNetwork() || isNRNetwork()) && (ServiceState.bitmaskHasTech(bearerBitmask, EVENT_DATA_CONNECTED) || ServiceState.bitmaskHasTech(bearerBitmask, EVENT_RADIO_OFF_OR_NOT_AVAILABLE))))) {
                    if ((!TelephonyManager.getDefault().isNetworkRoaming(this.mPhone.getSubId()) || !"ctnet".equals(apn.getApnName())) && (TelephonyManager.getDefault().isNetworkRoaming(this.mPhone.getSubId()) || !"ctwap".equals(apn.getApnName()))) {
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

    public boolean isSupportLTE(ApnSetting apnSettings) {
        int bearerBitmask = ServiceState.convertBearerBitmaskToNetworkTypeBitmask(apnSettings.getNetworkTypeBitmask());
        if ((bearerBitmask == 0 || ((!ServiceState.bitmaskHasTech(bearerBitmask, EVENT_DATA_CONNECTED) && !ServiceState.bitmaskHasTech(bearerBitmask, EVENT_RADIO_OFF_OR_NOT_AVAILABLE)) || !isApnPreset(apnSettings))) && isApnPreset(apnSettings)) {
            return false;
        }
        return true;
    }

    public void updateApnContextState() {
        this.mDcTracker.updateApnFromRetryToIdle();
    }

    public void updateForVSim() {
        log("vsim update sub = " + this.mPhone.getSubId());
        this.mDcTracker.unRegisterForAllEvents();
        this.mDcTracker.registerForAllEvents();
        this.mDcTracker.setUserDataEnabled(true);
        this.mDcTracker.update();
    }

    private void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 96);
    }

    public boolean isDisconnectedOrConnecting() {
        return this.mDcTracker.isDisconnectedOrConnecting();
    }

    public void setupDataForSinglePdnArbitration(String reason) {
        log("setupDataForSinglePdn: reason = " + reason + " isDisconnected = " + this.mDcTracker.isDisconnected());
        if (this.mDcTracker.isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && this.mDcTracker.isDisconnected() && !"SinglePdnArbitration".equals(reason)) {
            this.mDcTracker.setupDataOnAllConnectableApns("SinglePdnArbitration");
        }
    }

    public boolean isNeedFilterVowifiMms(ApnSetting apn, String requestedApnType) {
        return "mms".equals(requestedApnType) && (apn != null && apn.canHandleType(16777216)) && HuaweiTelephonyConfigs.isHisiPlatform();
    }

    public boolean isBlockSetInitialAttachApn() {
        String plmnsConfig = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "apn_reminder_plmn");
        IccRecordsEx r = this.mPhone.getIccRecords();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(operator)) {
            return false;
        }
        return plmnsConfig.contains(operator);
    }

    public boolean isNeedForceSetup(ApnContextEx apnContext) {
        return apnContext != null && "dataEnabled".equals(apnContext.getReason()) && this.mPhone.getServiceState().getVoiceRegState() == 0 && USER_FORCE_DATA_SETUP;
    }

    public void clearDefaultLink() {
        DataConnectionEx dc;
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType("default");
        if (apnContext != null && (dc = apnContext.getDataConnection()) != null) {
            dc.clearLink();
        }
    }

    public void resumeDefaultLink() {
        DataConnectionEx dc;
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType("default");
        if (apnContext != null && (dc = apnContext.getDataConnection()) != null) {
            dc.resumeLink();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserSelectOpenService() {
        log("onUserSelectOpenService set apn = bip0");
        List<ApnSetting> allApnSettings = this.mDcTracker.getAllApnList();
        if (allApnSettings != null && allApnSettings.isEmpty()) {
            this.mDcTracker.createAllApnList();
        }
        CommandsInterfaceEx ci = this.mPhone.getCi();
        if (ci != null && ci.getRadioState() == 1) {
            log("onRecordsLoaded: notifying data availability");
            this.mDcTracker.notifyDataConnection();
        }
        this.mDcTracker.setupDataOnAllConnectableApns("SetPSOnlyOK");
    }

    public String getOperatorNumeric() {
        IccRecordsEx r = this.mPhone.getIccRecords();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (operator == null) {
            operator = "";
        }
        log("getOperatorNumberic - returning from card: " + operator);
        return operator;
    }

    public String getCTOperator(String operator) {
        String preSpn;
        String newOperator = operator;
        if (isCTSimCard(this.mPhone.getPhoneId())) {
            newOperator = SystemProperties.get("gsm.national_roaming.apn", "46003");
            log("Select china telecom hplmn: " + newOperator);
        }
        IccRecordsEx record = this.mPhone.getIccRecords();
        IccRecordsEx gsmIccRecords = UiccControllerExt.getInstance().getIccRecords(this.mPhone.getPhoneId(), 1);
        String gsmOperator = gsmIccRecords != null ? gsmIccRecords.getOperatorNumeric() : "";
        if (record != null) {
            preSpn = record.getServiceProviderName();
        } else {
            preSpn = "";
        }
        String preIccid = SystemProperties.get("gsm.sim.preiccid_" + this.mPhone.getPhoneId(), "");
        if (!"46003".equals(newOperator)) {
            return newOperator;
        }
        if ("8985231".equals(preIccid) || GC_SPN.equals(preSpn)) {
            log("Hongkong GC card and iccid is: " + preIccid + ",spn is: " + preSpn);
            return GC_MCCMNC;
        } else if (!MO_ICCID_1.equals(preIccid) && !MO_ICCID_2.equals(preIccid)) {
            log("CT card is not GC and MO card.");
            return newOperator;
        } else if (TextUtils.isEmpty(gsmOperator)) {
            log("MO card, gsmOperator is null");
            return null;
        } else if (MO_UNICOM_MCCMNC.equals(gsmOperator)) {
            log("MO card, gsmOperator is UNICOM");
            return MO_UNICOM_MCCMNC;
        } else {
            log("MO card, gsmOperator is not UNICOM");
            return newOperator;
        }
    }

    public void correctApnAuthType(List<ApnSetting> allApnSettings) {
        if (allApnSettings == null || allApnSettings.isEmpty()) {
            log("correctApnAuthType: allApnSettings is empty");
        } else if (this.mDcTracker.isCustCorrectApnAuthOn()) {
            this.mDcTracker.custCorrectApnAuth(allApnSettings);
        } else {
            int size = allApnSettings.size();
            for (int i = 0; i < size; i++) {
                ApnSetting apn = allApnSettings.get(i);
                String username = apn.getUser();
                String password = apn.getPassword();
                int authType = apn.getAuthType();
                if (authType == 1) {
                    if (TextUtils.isEmpty(username)) {
                        authType = 0;
                        password = "";
                        log("authType is pap but username is null, clear all");
                    }
                } else if (authType == 2) {
                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                        authType = 0;
                        username = "";
                        password = "";
                        log("authType is chap but username or password is null, clear all");
                    }
                } else if (authType != 3) {
                    log("ignore other authType.");
                } else if (TextUtils.isEmpty(username)) {
                    authType = 0;
                    password = "";
                    log("authType is pap_chap but username is null, clear all");
                } else if (TextUtils.isEmpty(password)) {
                    authType = 1;
                    log("authType is pap_chap but password is null, tune authType to pap");
                } else {
                    log("ignore other authType.");
                }
                ApnSetting correctApn = ApnSetting.makeApnSetting(apn.getId(), apn.getOperatorNumeric(), apn.getEntryName(), apn.getApnName(), apn.getProxyAddressAsString(), apn.getProxyPort(), apn.getMmsc(), apn.getMmsProxyAddressAsString(), apn.getMmsProxyPort(), username, password, authType, apn.getApnTypeBitmask(), apn.getProtocol(), apn.getRoamingProtocol(), apn.isEnabled(), apn.getNetworkTypeBitmask(), apn.getProfileId(), apn.isPersistent(), apn.getMaxConns(), apn.getWaitTime(), apn.getMaxConnsTime(), apn.getMtu(), apn.getMvnoType(), apn.getMvnoMatchData(), apn.getApnSetId(), apn.getCarrierId(), apn.getSkip464Xlat());
                correctApn.setIsPreset(apn.isPreset());
                allApnSettings.set(i, correctApn);
            }
        }
    }

    public void onActionIntentReenableNrAlarm(Intent intent) {
        log("onActionIntentReenableNrAlarm: action = " + intent.getAction());
        Message msg = this.mHandler.obtainMessage(7);
        msg.setData(intent.getExtras());
        this.mHandler.sendMessage(msg);
    }

    public void onActionIntentDisableNrAlarm(Intent intent) {
        log("onActionIntentDisableNrAlarm: action = " + intent.getAction());
        Message msg = this.mHandler.obtainMessage(8);
        msg.setData(intent.getExtras());
        this.mHandler.sendMessage(msg);
    }

    private void cancelDisableNrAlarm(ApnContextEx apnContext) {
        if (apnContext != null && apnContext.getDisableNrIntent() != null) {
            log("cancelDisableNrAlarm for apn = " + apnContext.getApnType());
            this.mAlarmManager.cancel(apnContext.getDisableNrIntent());
            apnContext.setDisableNrIntent((PendingIntent) null);
        }
    }

    private void cancelReenableNrAlarm(ApnContextEx apnContext) {
        if (apnContext != null && apnContext.getReenableNrIntent() != null) {
            log("cancelReenableNrAlarm for apn = " + apnContext.getApnType());
            this.mAlarmManager.cancel(apnContext.getReenableNrIntent());
            apnContext.setReenableNrIntent((PendingIntent) null);
        }
    }

    public void startAlarmForReenableNr(ApnContextEx apnContext, long delay) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent(INTENT_REENABLE_NR_ALARM);
        int phoneId = this.mPhone.getPhoneId();
        intent.addFlags(268435456);
        intent.putExtra(INTENT_REENABLE_NR_ALARM_EXTRA_TYPE, apnType);
        if (VSimUtilsInner.isVSimSub(phoneId)) {
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, this.mPhone.getSubId());
        } else {
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
        }
        log("startAlarmForReenableNr: apn = " + apnContext.getApnType() + " delay=" + delay);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        apnContext.setReenableNrIntent(alarmIntent);
        this.mDcTracker.cancelReconnectAlarm(apnContext);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    public void startAlarmForDisableNr(ApnContextEx apnContext, long delay) {
        log("startAlarmForDisableNr: delay = " + delay + "  APN = " + apnContext.getApnType());
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, apnContext.getDisableNrIntent());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public void setNrSaState(ApnContextEx apnContext, boolean on) {
        if (apnContext == null) {
            log("setNrSaState: apnContext is null");
        } else if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            Message response = this.mHandler.obtainMessage(EVENT_SET_NR_SA_STATE_DONE);
            response.obj = new Pair(apnContext, Boolean.valueOf(on));
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.setNrSaState(on, response);
            }
            log("setNrSaState: apnContext=" + apnContext.getApnType() + "new nr sa state=" + (on ? 1 : 0));
        }
    }

    private void getNrSaState() {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            log("getNrSaState from ril");
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.getNrSaState(this.mHandler.obtainMessage(EVENT_GET_NR_SA_STATE_DONE));
            }
        }
    }

    private void onRilConnected(Message msg) {
        if (!(msg.obj instanceof AsyncResult)) {
            log("onRilConnected: msg not instanceof AsyncResult");
        } else if (((AsyncResult) msg.obj).exception == null && IS_HW_DATA_RETRY_MANAGER_ENABLED && this.mHwDataRetryManager != null) {
            log("onRilConnected: sync nr sa state with RIL");
            getNrSaState();
        }
    }

    private void onReenableNrAlarm(Bundle bundle) {
        if (bundle == null) {
            log("onReenableNrAlarm with null bundle");
            return;
        }
        String apnType = bundle.getString(INTENT_REENABLE_NR_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = bundle.getInt("subscription", -1);
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType(apnType);
        if (SubscriptionManager.isValidSubscriptionId(currSubId) && currSubId == phoneSubId && apnContext != null && apnType != null) {
            log("Re-enable nr alarm expired.");
            if (apnContext.getDisableNrIntent() != null) {
                log("Disable nr isnt executed until reenable nr alarm expired, reconnect again.");
                cancelDisableNrAlarm(apnContext);
                apnContext.setReenableNrIntent((PendingIntent) null);
                this.mDcTracker.startAlarmForReconnect(50, apnContext);
            } else if (this.mDcTracker.isPhoneStateIdle()) {
                setNrSaState(apnContext, true);
                resetRetryManager();
            } else {
                log("Re-enable nr alarm expired, phone is not idle, restart alarm.");
                startAlarmForReenableNr(apnContext, 2000);
            }
        }
    }

    public void resetRetryManager() {
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED && this.mHwDataRetryManager != null) {
            log("resetRetryManager.");
            this.mHwDataRetryManager.resetAllRetryStrategy();
        }
    }

    public void restorePreferredRatNr() {
        log("restorePreferredRatNr");
        resetRetryManager();
        for (ApnContextEx apnContext : this.mDcTracker.getApnContextsList()) {
            if (isHwDataRetryManagerApplied(apnContext) && apnContext.getReenableNrIntent() != null) {
                cancelReenableNrAlarm(apnContext);
                if (apnContext.getDisableNrIntent() != null) {
                    log("restorePreferredRatNr: disable nr isnt executed, so just reeconnect");
                    cancelDisableNrAlarm(apnContext);
                    this.mDcTracker.startAlarmForReconnect(50, apnContext);
                    return;
                }
                log("restorePreferredRatNr: reenable nr");
                startAlarmForReenableNr(apnContext, 50);
                return;
            }
        }
    }

    private void onDisableNr(Bundle bundle) {
        log("onDisableNr");
        if (bundle == null) {
            log("onDisableNr with null bundle");
            return;
        }
        String apnType = bundle.getString(INTENT_DISABLE_NR_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = bundle.getInt("subscription", -1);
        if (!SubscriptionManager.isValidSubscriptionId(currSubId) || currSubId != phoneSubId || apnType == null) {
            log("onDisableNr phone id invalid, phoneSubId=" + phoneSubId + " currSubId=" + currSubId + "apnType=" + apnType);
            return;
        }
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType(apnType);
        if (apnContext != null) {
            apnContext.setDisableNrBundle(bundle);
            if (this.mDcTracker.isPhoneStateIdle()) {
                setNrSaState(apnContext, false);
                return;
            }
            log("onDisableNr: phone isnt idle, restart alram for disable nr");
            startAlarmForDisableNr(apnContext, 2000);
        }
    }

    public void sendDisableNr(ApnContextEx apnContext, long delay) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent(INTENT_DISABLE_NR_ALARM);
        int phoneId = this.mPhone.getPhoneId();
        intent.addFlags(268435456);
        intent.putExtra(INTENT_DISABLE_NR_ALARM_EXTRA_TYPE, apnType);
        intent.putExtra(INTENT_DISABLE_NR_ALARM_EXTRA_TIME, delay);
        if (VSimUtilsInner.isVSimSub(phoneId)) {
            log("sendDisableNr: for vsim.");
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, this.mPhone.getSubId());
        } else {
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
        }
        log("sendDisableNr for apn = " + apnType + " delay = " + delay);
        apnContext.setDisableNrIntent(PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728));
        Message msg = this.mHandler.obtainMessage(8);
        msg.setData(intent.getExtras());
        this.mHandler.sendMessage(msg);
    }

    public void onGetNrSaStateDone(Message msg) {
        if (msg.obj instanceof AsyncResult) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                log("onGetNrSaStateDone: ar.exception != null");
                return;
            }
            ApnContextEx defaultApn = this.mDcTracker.getApnContextByType("default");
            if (defaultApn != null) {
                boolean rilNrSaState = false;
                boolean apNrSaState = defaultApn.getReenableNrIntent() == null;
                int result = 0;
                try {
                    result = Integer.parseInt(ar.result.toString());
                } catch (NumberFormatException e) {
                    log("parse result error");
                }
                if (result > 0) {
                    rilNrSaState = true;
                }
                log("onGetNrSaStateDone: apNrSaState=" + apNrSaState + " rilNrSaState=" + rilNrSaState);
                if (apNrSaState && !rilNrSaState) {
                    if (defaultApn.getDisableNrIntent() != null) {
                        this.mAlarmManager.cancel(defaultApn.getDisableNrIntent());
                    }
                    setNrSaState(defaultApn, true);
                    return;
                }
                return;
            }
            return;
        }
        log("onGetNrSaStateDone: msg.obj not instanceof AsyncResult");
    }

    private boolean isDataReconnectNeeded(ApnContextEx apnContext) {
        if (apnContext == null || apnContext.getReconnectIntent() != null || this.mDcTracker.getDataRat() == 20) {
            return false;
        }
        if (apnContext.getState() == ApnContextEx.StateEx.IDLE || apnContext.getState() == ApnContextEx.StateEx.FAILED) {
            return true;
        }
        return false;
    }

    private void onSetNrSaStateError(ApnContextEx apnContext, boolean nrState, AsyncResult ar) {
        if (!(ar.exception instanceof CommandException)) {
            log("onSetNrSaStateError: not CommandException");
        } else if (ar.exception.getCommandError() == CommandException.Error.NOT_PROVISIONED) {
            log("onSetNrSaStateError, operation not supported.");
        } else {
            this.mRetryTimes++;
            if (this.mRetryTimes >= 3) {
                this.mRetryTimes = 0;
                log("onSetNrSaStateError: reach max retry times for setNrState = " + nrState);
            } else if (nrState) {
                log("onSetNrSaStateDone, reenable nr failed, retry again 2s later.");
                startAlarmForReenableNr(apnContext, 2000);
            } else {
                log("onSetNrSaStateDone, disable nr failed, retry again 2s later.");
                startAlarmForDisableNr(apnContext, 2000);
            }
        }
    }

    public void onSetNrSaStateDone(Message msg) {
        if (msg.obj instanceof AsyncResult) {
            AsyncResult ar = (AsyncResult) msg.obj;
            ApnContextEx apnContext = null;
            boolean nrState = true;
            if (ar.userObj instanceof Pair) {
                Pair pair = (Pair) ar.userObj;
                if ((pair.first instanceof ApnContextEx) && (pair.second instanceof Boolean)) {
                    apnContext = (ApnContextEx) pair.first;
                    nrState = ((Boolean) pair.second).booleanValue();
                }
            }
            if (apnContext == null) {
                log("onSetNrSaStateDone, ar.userObj is not instance of Pair");
            } else if (ar.exception == null) {
                this.mRetryTimes = 0;
                apnContext.setDisableNrIntent((PendingIntent) null);
                if (nrState) {
                    apnContext.setReenableNrIntent((PendingIntent) null);
                    if (isDataReconnectNeeded(apnContext)) {
                        this.mDcTracker.startAlarmForReconnect(5000, apnContext);
                    }
                    this.mPhone.sendIntentWhenReenableNr();
                    return;
                }
                Bundle bundle = apnContext.getDisableNrBundle();
                this.mPhone.sendIntentWhenDisableNr(apnContext.getFailCause(), bundle == null ? 0 : bundle.getLong(INTENT_DISABLE_NR_ALARM_EXTRA_TIME));
            } else {
                onSetNrSaStateError(apnContext, nrState, ar);
            }
        } else {
            log("onSetNrSaStateDone, msg.obj is not instance of AsyncResult.");
        }
    }

    private boolean isHwDataRetryManagerApplied(ApnContextEx apnContext) {
        if (!IS_HW_DATA_RETRY_MANAGER_ENABLED || this.mHwDataRetryManager == null || !"default".equals(apnContext.getApnType())) {
            log("HwDataRetryManager is Applied, APN Type= " + apnContext.getApnType() + " enabled=" + IS_HW_DATA_RETRY_MANAGER_ENABLED);
            return false;
        }
        log("HwDataRetryManager is Applied for " + apnContext.getApnType());
        return true;
    }

    public void updateDataRetryStategy(ApnContextEx apnContext) {
        if (isHwDataRetryManagerApplied(apnContext)) {
            log("updateDataRetryStategy: run data retry manager");
            this.mHwDataRetryManager.updateDataRetryStategy(apnContext);
            if (getDataRetryAction(apnContext) != 0) {
                apnContext.resetApnPermanentFailedFlag();
                return;
            }
            return;
        }
        log("updateDataRetryStategy: not satisfied");
        apnContext.setModemSuggestedDelay((long) NO_SUGGESTED_RETRY_DELAY);
    }

    public int getDataRetryAction(ApnContextEx apnContext) {
        if (!isHwDataRetryManagerApplied(apnContext)) {
            log("getDataRetryAction:apn=" + apnContext.getApnType() + ",data retry not applied, return no action");
            return 0;
        }
        int nextAction = this.mHwDataRetryManager.getDataRetryAction(apnContext);
        log("getDataRetryAction:action=" + nextAction);
        return nextAction;
    }

    public long getDataRetryDelay(long originDelay, ApnContextEx apnContext) {
        long delay = originDelay;
        long iotDelayPropTimer = SystemProperties.getLong(TELECOM_IOT_APN_DELAY, 0);
        if (iotDelayPropTimer > 0) {
            return iotDelayPropTimer;
        }
        if (sIsScreenOn && (IMMEDIATELY_RETRY_FAILCAUSE.contains(Integer.valueOf(apnContext.getFailCause())) || IMMEDIATELY_RETRY_REASON.contains(apnContext.getReason()))) {
            delay = 0;
            log(apnContext.getReason() + "  " + apnContext.getFailCause() + " reduce the delay time to " + 0L);
        }
        if (getDataRetryAction(apnContext) == 0) {
            return delay;
        }
        if (!isHwDataRetryManagerApplied(apnContext)) {
            log("getDataRetryDelay:apn=" + apnContext.getApnType() + ",data retry not applied, return 0");
            return delay;
        }
        long delay2 = this.mHwDataRetryManager.getDataRetryDelay(apnContext);
        log("getDataRetryDelay:apn=" + apnContext.getApnType() + ",retry delay=" + delay2);
        return delay2;
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached");
        resetRetryManager();
        getNrSaState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDualCardSwitchDone() {
        log("onDualCardSwitchDone");
        ApnContextEx defaultApn = this.mDcTracker.getApnContextByType("default");
        if (defaultApn != null) {
            cancelReenableNrAlarm(defaultApn);
            cancelDisableNrAlarm(defaultApn);
            defaultApn.setDisableNrBundle((Bundle) null);
        }
        resetRetryManager();
    }

    private void onDataConnected() {
        ApnContextEx defaultApn = this.mDcTracker.getApnContextByType("default");
        if (defaultApn == null) {
            loge("onDataConnected: default apn is null, return.");
        } else if (defaultApn.getState() == ApnContextEx.StateEx.CONNECTED) {
            log("onDataConnected: reset retry manager");
            defaultApn.setFailCause(0);
            resetRetryManager();
            if (defaultApn.getDisableNrIntent() != null) {
                log("onDataConnected: canel disable nr operation");
                cancelDisableNrAlarm(defaultApn);
                cancelReenableNrAlarm(defaultApn);
            }
        }
    }

    private void onRadioOffOrNotAvailable() {
        log("onRadioOffOrNotAvailable: restore preferred rat.");
        restorePreferredRatNr();
    }

    private void onDataEnabledChanged(Message msg) {
        if (msg.obj instanceof AsyncResult) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.result instanceof Pair) {
                Pair p = (Pair) ar.result;
                Boolean dataEnabled = true;
                Integer dataEnabledReason = 1;
                if ((p.first instanceof Boolean) && (p.second instanceof Integer)) {
                    dataEnabled = (Boolean) p.first;
                    dataEnabledReason = (Integer) p.second;
                }
                if (!dataEnabled.booleanValue() && dataEnabledReason.intValue() == 2) {
                    log("onDataEnabledChanged: data disabled so restore preferred rat.");
                    restorePreferredRatNr();
                }
            }
        }
    }

    private void onDataAttached() {
        if (isLTENetworks() && this.mAttachedApnSettings == null) {
            getAttachedApnSettings();
        }
    }

    private void onDataRatChanged() {
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED && this.mDcTracker.getDataRat() != 0) {
            log("onDataRatChanged: reset retry manager");
            resetRetryManager();
        }
        if (this.mAttachedApnSettings == null && isLTENetworks()) {
            getAttachedApnSettings();
        }
    }

    private void onSimHotPlug(Message msg) {
        if (msg.obj instanceof AsyncResult) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.result != null && (ar.result instanceof int[]) && ((int[]) ar.result).length > 0 && ((int[]) ar.result)[0] == HotplugState.PLUG_OUT.ordinal()) {
                log("onSimHotPlug: sim removed");
                restorePreferredRatNr();
            }
        }
    }

    public void checkOnlyIpv6Cure(ApnContextEx apn) {
        HwDataSelfCure hwDataSelfCure = this.mHwDataSelfCure;
        if (hwDataSelfCure != null) {
            hwDataSelfCure.checkOnlyIpv6Cure(apn);
        }
    }

    public boolean isNeedDataCure(int cause, ApnContextEx apn) {
        if (!IS_PDN_REJ_CURE_ENABLE || this.mHwDataSelfCure == null) {
            return false;
        }
        log("datacure subid: " + this.mPhone.getPhoneId());
        return this.mHwDataSelfCure.isNeedDataCure(cause, apn);
    }

    public void setEsmFlag(int esmInfo) {
        HwDataSelfCure hwDataSelfCure;
        if (IS_PDN_REJ_CURE_ENABLE && (hwDataSelfCure = this.mHwDataSelfCure) != null) {
            hwDataSelfCure.setEsmFlag(esmInfo);
        }
    }

    public void updateDataCureProtocol(ApnContextEx apn) {
        if (IS_PDN_REJ_CURE_ENABLE && this.mHwDataSelfCure != null && apn != null) {
            if (!"default".equals(apn.getApnType())) {
                log("checkUpdateApnInfo request type is:" + apn.getApnType());
                return;
            }
            this.mHwDataSelfCure.updateDataCureProtocol(apn);
        }
    }

    public int getDataCureEsmFlag(String operator) {
        HwDataSelfCure hwDataSelfCure;
        if (!IS_PDN_REJ_CURE_ENABLE || (hwDataSelfCure = this.mHwDataSelfCure) == null) {
            return 0;
        }
        return hwDataSelfCure.getDataCureEsmFlag(operator);
    }

    public void resetDataCureInfo(String reason) {
        HwDataSelfCure hwDataSelfCure;
        if (IS_PDN_REJ_CURE_ENABLE && (hwDataSelfCure = this.mHwDataSelfCure) != null) {
            hwDataSelfCure.resetDataCureInfo(reason);
        }
    }

    public ApnSetting getRegApnForCure(ApnSetting apnSetting) {
        return this.mHwDataSelfCure.getRegApnForCure(apnSetting);
    }

    public void sendRestartRadioChr(int subId, int cause) {
        Context mContext = this.mPhone.getContext();
        if (mContext != null) {
            logd("sendRestartRadioChr.");
            Intent intent = new Intent(INTENT_DS_RESTART_RADIO);
            intent.putExtra("subscription", subId);
            intent.putExtra("restart_reason", cause);
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNetworkRejInfoDone(Message msg) {
        if (IS_PDN_REJ_CURE_ENABLE && this.mHwDataSelfCure != null) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                String[] datas = (String[]) ar.result;
                int rejectRat = -1;
                int rejectType = -1;
                if (datas.length >= 6 && datas[3] != null && datas[3].length() > 0 && datas[5] != null && datas[5].length() > 0) {
                    try {
                        rejectRat = Integer.parseInt(datas[3]);
                        rejectType = Integer.parseInt(datas[5]);
                    } catch (NumberFormatException e) {
                        log("error parsing NetworkReject fail!");
                    }
                    this.mHwDataSelfCure.handleNetworkRejectInfo(rejectRat, rejectType);
                }
            }
        }
    }

    public void updateCustomizedEsmFlagState(int defaultEsmFlag, int isEsmFlagCustomized) {
        int phoneId = this.mPhone.getPhoneId();
        String propKey = ESMFLAG_CURE_STATE + String.valueOf(phoneId);
        int customizedState = 1;
        if (IS_ENABLE_ESMFLAG_CURE) {
            if (defaultEsmFlag == 1 && isEsmFlagCustomized == 0) {
                if (!this.mDcTracker.isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
                    log("mpdn yes");
                    customizedState = 0;
                } else {
                    log("mpdn no");
                }
            }
        } else if (SystemProperties.get(propKey, DEAULT_ESMCURE_PROP_VALUE).equals(DEAULT_ESMCURE_PROP_VALUE)) {
            return;
        }
        SystemProperties.set(propKey, String.valueOf(customizedState));
        log("esmflag customized value=" + customizedState);
    }

    public String getSimState() {
        return this.mSimState;
    }

    public int notifyBoosterDoRecovery(int event) {
        int ret;
        if (this.mIhwCommBoosterServiceManager == null) {
            log("mIhwCommBoosterServiceManager is null");
            return -1;
        } else if (event == 0) {
            this.mDoRecoveryAct = 1;
            return -1;
        } else if (this.mDoRecoveryAct == 0) {
            this.mDoRecoveryAct = 1;
            return -1;
        } else {
            Bundle data = new Bundle();
            int phoneId = this.mPhone.getPhoneId();
            data.putInt("currentPhoneId", phoneId);
            data.putInt("recoveryAct", this.mDoRecoveryAct);
            if (phoneId == 0) {
                ret = this.mIhwCommBoosterServiceManager.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE0, (int) TYPE_DO_RECOVERY_OEM, data);
            } else if (phoneId == 1) {
                ret = this.mIhwCommBoosterServiceManager.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE1, (int) TYPE_DO_RECOVERY_OEM, data);
            } else {
                ret = this.mIhwCommBoosterServiceManager.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE2, (int) TYPE_DO_RECOVERY_OEM, data);
            }
            if (ret != 0) {
                log("reportBoosterPara failed, ret=" + ret);
            }
            return ret;
        }
    }

    public boolean isMultiPdpPlmnMatched(String operator) {
        String plmnsConfig = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "mpdn_plmn_matched_by_network");
        if (!TextUtils.isEmpty(plmnsConfig)) {
            String[] plmns = plmnsConfig.split(",");
            for (String plmn : plmns) {
                if (!TextUtils.isEmpty(plmn) && plmn.equals(operator)) {
                    return true;
                }
            }
            return false;
        }
        log("mpdn_plmn_matched_by_network is empty");
        return true;
    }

    public ApnContextEx getNrSliceApnContext(NetworkRequestExt networkRequest) {
        if (networkRequest == null) {
            return null;
        }
        ApnContextEx apnContextFor5GSlice = this.mApnContextsByTypeFor5GSlice.get(networkRequest.getNetCapability5GSliceType());
        if (apnContextFor5GSlice == null) {
            log("Can not get 5G Slice ApnContext");
            return null;
        }
        apnContextFor5GSlice.setDnn(networkRequest.getDnn());
        apnContextFor5GSlice.setSnssai(networkRequest.getSnssai());
        apnContextFor5GSlice.setSscMode(networkRequest.getSscMode());
        apnContextFor5GSlice.setPduSessionType(networkRequest.getPduSessionType());
        apnContextFor5GSlice.setRouteBitmap(networkRequest.getRouteBitmap());
        return apnContextFor5GSlice;
    }

    public void putApnContextFor5GSlice(int sliceIndex, ApnContextEx apnContext) {
        this.mApnContextsByTypeFor5GSlice.put(sliceIndex, apnContext);
    }

    public ApnContextEx getApnContextFor5GSlice(int sliceIndex) {
        return this.mApnContextsByTypeFor5GSlice.get(sliceIndex);
    }

    public boolean hasMatchAllSlice() {
        for (int i = 0; i < this.mApnContextsByTypeFor5GSlice.size(); i++) {
            SparseArray<ApnContextEx> sparseArray = this.mApnContextsByTypeFor5GSlice;
            ApnContextEx apnContextEx = sparseArray.get(sparseArray.keyAt(i));
            if (!(apnContextEx == null || (apnContextEx.getRouteBitmap() & MATCH_ALL) == 0)) {
                return true;
            }
        }
        return false;
    }

    public ApnSetting createSliceApnSetting(ApnSetting apn) {
        if (apn == null) {
            return null;
        }
        return ApnSettingExt.makeApnSettingForSlice(apn);
    }

    public boolean isApnSettingsSimilar(ApnSetting first, ApnSetting second) {
        if (first == null || second == null) {
            return false;
        }
        boolean similarApnState = false;
        Boolean similarApnSign = (Boolean) HwCfgFilePolicy.getValue("compatible_apn_switch", this.mPhone.getPhoneId(), Boolean.class);
        if (similarApnSign != null) {
            similarApnState = similarApnSign.booleanValue();
        }
        if (!similarApnState) {
            return first.similar(second);
        }
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            return getApnSimilarMtk(first, second);
        }
        return getApnSimilarHisi(first, second);
    }

    private boolean getApnSimilarMtk(ApnSetting first, ApnSetting second) {
        String secondMmsc = null;
        String firstMmsc = first.getMmsc() == null ? null : first.getMmsc().toString();
        if (second.getMmsc() != null) {
            secondMmsc = second.getMmsc().toString();
        }
        if (Objects.equals(first.getApnName(), second.getApnName()) && xorEqualsProtocol(ApnSetting.getProtocolStringFromInt(first.getProtocol()), ApnSetting.getProtocolStringFromInt(second.getProtocol())) && xorEqualsProtocol(ApnSetting.getProtocolStringFromInt(first.getRoamingProtocol()), ApnSetting.getProtocolStringFromInt(second.getRoamingProtocol())) && first.isEnabled() == second.isEnabled() && xorEquals(firstMmsc, secondMmsc) && xorEquals(first.getMmsProxyAddressAsString(), second.getMmsProxyAddressAsString()) && xorEqualsInt(first.getMmsProxyPort(), second.getMmsProxyPort())) {
            return true;
        }
        return false;
    }

    private boolean getApnSimilarHisi(ApnSetting first, ApnSetting second) {
        String secondMmsc = null;
        String firstMmsc = first.getMmsc() == null ? null : first.getMmsc().toString();
        if (second.getMmsc() != null) {
            secondMmsc = second.getMmsc().toString();
        }
        return Objects.equals(first.getApnName(), second.getApnName()) && (first.getAuthType() == second.getAuthType() || -1 == first.getAuthType() || -1 == second.getAuthType()) && Objects.equals(first.getUser(), second.getUser()) && Objects.equals(first.getPassword(), second.getPassword()) && Objects.equals(first.getProxyAddressAsString(), second.getProxyAddressAsString()) && xorEqualsInt(first.getProxyPort(), second.getProxyPort()) && xorEqualsProtocol(ApnSetting.getProtocolStringFromInt(first.getProtocol()), ApnSetting.getProtocolStringFromInt(second.getProtocol())) && xorEqualsProtocol(ApnSetting.getProtocolStringFromInt(first.getRoamingProtocol()), ApnSetting.getProtocolStringFromInt(second.getRoamingProtocol())) && first.isEnabled() == second.isEnabled() && first.getNetworkTypeBitmask() == second.getNetworkTypeBitmask() && first.getMtu() == second.getMtu() && xorEquals(firstMmsc, secondMmsc) && xorEquals(first.getMmsProxyAddressAsString(), second.getMmsProxyAddressAsString()) && xorEqualsInt(first.getMmsProxyPort(), second.getMmsProxyPort());
    }

    private boolean xorEqualsInt(int first, int second) {
        return first == -1 || second == -1 || Objects.equals(Integer.valueOf(first), Integer.valueOf(second));
    }

    private boolean xorEquals(String first, String second) {
        return Objects.equals(first, second) || TextUtils.isEmpty(first) || TextUtils.isEmpty(second);
    }

    private boolean xorEqualsProtocol(String first, String second) {
        return Objects.equals(first, second) || ("IPV4V6".equals(first) && ("IP".equals(second) || "IPV6".equals(second))) || (("IP".equals(first) && "IPV4V6".equals(second)) || ("IPV6".equals(first) && "IPV4V6".equals(second)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentSetNetworkType(Intent intent) {
        restorePreferredRatNr();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String message) {
        RlogEx.i(this.mTag, message);
    }

    private void logv(String message) {
        RlogEx.v(this.mTag, message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String message) {
        RlogEx.i(this.mTag, message);
    }

    private void logi(String message) {
        RlogEx.i(this.mTag, message);
    }

    private void logw(String message) {
        RlogEx.w(this.mTag, message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String message) {
        RlogEx.e(this.mTag, message);
    }
}
