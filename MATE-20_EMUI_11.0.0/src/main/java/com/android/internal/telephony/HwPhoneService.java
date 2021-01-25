package com.android.internal.telephony;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkSecurityPartsFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CallerInfoHW;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.NrCellSsbId;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAuthResponse;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.android.ims.HwImsManager;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.IHwTelephony;
import com.android.internal.telephony.IHwTelephonyInner;
import com.android.internal.telephony.dataconnection.IHwDcTrackerEx;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.app.NotificationEx;
import com.huawei.android.emcom.EmcomManagerExt;
import com.huawei.android.net.ConnectivityManagerEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.telephony.CellLocationEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.ims.ImsManagerExt;
import com.huawei.internal.telephony.CallForwardInfoExt;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.HwTelephonyBoosterUtils;
import com.huawei.internal.telephony.IHwCommonPhoneCallback;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.NrCellSsbIdExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.TelephonyPermissionsExt;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.IccUtilsEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

public class HwPhoneService extends IHwTelephony.Stub implements HwPhoneManager.PhoneServiceInterface {
    private static final String ACTION_CANCEL_NOTIFY = "com.huawei.action.ACTION_HW_CANCEL_NOTIFY";
    private static final String ACTION_HIAIDSENGINE_ON = "com.huawei.broadcast.intent.SMART_CARD_ON";
    private static final String ACTION_IGNORE_NOTIFY = "com.huawei.action.ACTION_HW_IGNORE_NOTIFY";
    private static final String ACTION_NEVER_NOTIFY = "com.huawei.action.ACTION_HW_NERVER_NOTIFY";
    private static final String ACTION_SWITCH_BACK = "com.huawei.action.ACTION_HW_SWITCH_BACK";
    private static final String CALLBACK_AFBS_INFO = "AntiFakeBaseStationInfo";
    private static final String CALLBACK_EXCEPTION = "EXCEPTION";
    private static final String CALLBACK_RESULT = "RESULT";
    private static final String CARDMANAGER_AVTIVITY = "com.huawei.settings.intent.DUAL_CARD_SETTINGS";
    private static final String CARRIER_CONFIG_VERSION_UPDATE_PROP = "persist.sys.version_update";
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    private static final int CMD_ENCRYPT_CALL_INFO = 500;
    private static final int CMD_GET_CSCON_ID = 205;
    private static final int CMD_GET_NR_CELL_SSBID = 608;
    private static final int CMD_GET_NR_OPTION_MODE = 605;
    private static final int CMD_HANDLE_DEMO = 1;
    private static final int CMD_ICC_GET_ATR = 210;
    private static final int CMD_IMS_GET_DOMAIN = 103;
    private static final int CMD_INVOKE_OEM_RIL_REQUEST_RAW = 116;
    private static final int CMD_SET_DEEP_NO_DISTURB = 203;
    private static final int CMD_SET_UL_FREQ_BANDWIDTH_RPT = 207;
    private static final int CMD_UICC_AUTH = 101;
    private static final String DATA_FLOW_NOTIFY_CHANNEL = "data_flow_notify_channel";
    private static final int DATA_SEND_TO_TELEPHONY_BIND_TO_SUBCARD_FREE_TRAFFIC = 8;
    private static final int DATA_SEND_TO_TELEPHONY_SWITCH_MASTER_CARD_CAUSE_SUBCARD_BAD = 7;
    private static final int DATA_SEND_TO_TELEPHONY_SWITCH_TO_SUBCARD_FREE_TRAFFIC = 6;
    private static final String DAY_MODE = "day_mode";
    private static final String DAY_MODE_TIME = "day_mode_time";
    private static final int DEFAULT_INTELLIGENT_SWITCH_VALUE = 0;
    private static final String DEVICEID_PREF = "deviceid";
    private static final int DOWNLINK_LIMIT = SystemPropertiesEx.getInt("ro.config.network_limit_speed", (int) LIMIT_DEFAULT_VALUE);
    private static final int DUAL_SIM_NUM = 2;
    private static final int ENCRYPT_CALL_FEATURE_CLOSE = 0;
    private static final String ENCRYPT_CALL_FEATURE_KEY = "encrypt_version";
    private static final int ENCRYPT_CALL_FEATURE_OPEN = 1;
    private static final int ENCRYPT_CALL_FEATURE_SUPPORT = 1;
    private static final int ENCRYPT_CALL_NV_OFFSET = 4;
    private static final int ERROR_INVALID_PARAM = -3;
    private static final int ERROR_NO_SERVICE = -1;
    private static final int ERROR_REMOTE_EXCEPTION = -2;
    private static final int EVENT_ANTIFAKE_BASESTATION_CHANGED = 122;
    private static final int EVENT_BASIC_COMM_PARA_UPGRADE_DONE = 112;
    private static final int EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE = 113;
    private static final int EVENT_CHANGE_ICC_PIN_COMPLETE = 505;
    private static final int EVENT_CMD_GET_CSCON_ID_DONE = 206;
    private static final int EVENT_COMMON_IMSA_MAPCON_INFO = 53;
    private static final int EVENT_ENABLE_ICC_PIN_COMPLETE = 504;
    private static final int EVENT_ENCRYPT_CALL_INFO_DONE = 501;
    private static final int EVENT_GET_CALLFORWARDING_DONE = 119;
    private static final int EVENT_GET_CARD_TRAY_INFO_DONE = 601;
    private static final int EVENT_GET_CELL_BAND_DONE = 6;
    private static final int EVENT_GET_LAA_STATE_DONE = 115;
    private static final int EVENT_GET_NR_CELL_SSBID_DONE = 609;
    private static final int EVENT_GET_NR_OPTION_MODE_DONE = 604;
    private static final int EVENT_GET_NUMRECBASESTATION_DONE = 121;
    private static final int EVENT_GET_PREF_NETWORKS = 3;
    private static final int EVENT_GET_PREF_NETWORK_TYPE_DONE = 9;
    private static final int EVENT_ICC_GET_ATR_DONE = 211;
    private static final int EVENT_ICC_STATUS_CHANGED = 54;
    private static final int EVENT_IMS_GET_DOMAIN_DONE = 104;
    private static final int EVENT_INVOKE_OEM_RIL_REQUEST_RAW_DONE = 117;
    private static final int EVENT_NOTIFY_CMODEM_STATUS = 110;
    private static final int EVENT_NOTIFY_DEVICE_STATE = 111;
    private static final int EVENT_QUERY_CARD_TYPE_DONE = 506;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE = 502;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE_DONE = 503;
    private static final int EVENT_RADIO_AVAILABLE = 51;
    private static final int EVENT_RADIO_AVAILABLE_FOR_NR_OPTION_MODE = 606;
    private static final int EVENT_RADIO_AVAILABLE_GET_NR_OPTION_MODE_DONE = 607;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 52;
    private static final int EVENT_REG_ANT_STATE_IND = 11;
    private static final int EVENT_REG_BAND_CLASS_IND = 10;
    private static final int EVENT_REG_MAX_TX_POWER_IND = 12;
    private static final int EVENT_RETRY_SET_PREF_NETWORK_TYPE = 14;
    private static final int EVENT_SEND_LAA_CMD_DONE = 114;
    private static final int EVENT_SEND_SIM_MATCHED_OPERATOR_INFO_DONE = 610;
    private static final int EVENT_SET_4G_SLOT_DONE = 200;
    private static final int EVENT_SET_CALLFORWARDING_DONE = 118;
    private static final int EVENT_SET_DEEP_NO_DISTURB_DONE = 204;
    private static final int EVENT_SET_LINENUM_DONE = 202;
    private static final int EVENT_SET_LTE_SWITCH_DONE = 5;
    private static final int EVENT_SET_NR_OPTION_MODE_DONE = 603;
    private static final int EVENT_SET_PREF_NETWORKS = 4;
    private static final int EVENT_SET_PREF_NETWORK_TYPE_DONE = 13;
    private static final int EVENT_SET_SUBSCRIPTION_DONE = 201;
    private static final int EVENT_SET_TEMP_CTRL_DONE = 602;
    private static final int EVENT_SET_UL_FREQ_BANDWIDTH_RPT_DONE = 208;
    private static final int EVENT_SMART_CARD_BIND_TO_SUBCARD_FREE_TRAFFIC_TOAST = 703;
    private static final int EVENT_SMART_CARD_SWITCH_BACK_TOAST = 702;
    private static final int EVENT_SMART_CARD_SWITCH_TO_SUBCARD_CAUSE_FREE_TRAFFIC_TOAST = 701;
    private static final int EVENT_SMART_CARD_TOAST = 700;
    private static final int EVENT_UICC_AUTH_DONE = 102;
    private static final int EVENT_UL_FREQ_BANDWIDTH_RPT = 209;
    private static final int EVENT_USB_TETHER_STATE = 120;
    private static final int HW_CARRIER_CONFIG_STATE_PARA_UPDATE = 3;
    private static final int HW_PHONE_EXTEND_EVENT_BASE = 600;
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final String[] HW_VOLTE_USER_SWITCH_DUALIMS = {"hw_volte_user_switch_0", "hw_volte_user_switch_1"};
    private static final int HW_VOLTE_USER_SWITCH_OFF = 0;
    private static final int ICC_ATR_LENGTH_MAX = 66;
    private static final String IMEI_PREF = "imei";
    private static final String INCOMING_SMS_LIMIT = "incoming_limit";
    private static final String INTELLIGENCE_CARD_SETTING_DB = "intelligence_card_switch";
    private static final int INVALID = -1;
    private static final int INVALID_NETWORK_MODE = -1;
    private static final int INVALID_STEP = -99;
    private static final boolean IS_4G_SWITCH_SUPPORTED = SystemPropertiesEx.getBoolean("persist.sys.dualcards", false);
    private static final boolean IS_ESIM_HW_EXTEND_APDU = SystemPropertiesEx.getBoolean("ro.config.hw_extend_apdu", false);
    private static final boolean IS_FULL_NETWORK_SUPPORTED = SystemPropertiesEx.getBoolean("ro.config.full_network_support", false);
    private static final boolean IS_GSM_NONSUPPORT = SystemPropertiesEx.getBoolean("ro.config.gsm_nonsupport", false);
    private static final boolean IS_MTK_PLATFORM = HuaweiTelephonyConfigs.isMTKPlatform();
    private static final String IS_OUTGOING = "isOutgoing";
    private static final boolean IS_PLATFORM_SUPPORT_VSIM = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);
    private static final boolean IS_SHOW_DIALOG_FOR_NO_SIM = SystemPropertiesEx.getBoolean("ro.config.no_sim", false);
    private static final boolean IS_SPEED_LIMIT_NOTIFIER_ENABLE = SystemPropertiesEx.getBoolean("hw_sc.speed_limit_notifier_enable", false);
    private static final boolean IS_SUPPORT_ENCRYPT_CALL = SystemPropertiesEx.getBoolean("ro.config.support_encrypt", false);
    private static final String KEY1 = "key1";
    private static final int LIMIT_DEFAULT_VALUE = 3174;
    private static final String LOG_TAG = "HwPhoneService";
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    private static final int MAIN_MODEM_ID = 0;
    private static final int MAX_QUERY_COUNT = 10;
    private static final int MESSAGE_RETRY_PENDING_DELAY = 3000;
    private static final long MIN_INTERVAL_TIME = 86400000;
    private static final String MONTH_MODE = "month_mode";
    private static final String MONTH_MODE_TIME = "month_mode_time";
    private static final int MSG_ENCRYPT_CALL_BASE = 500;
    private static final String NETWORK_LIMIT_NOTIFY_CHANNEL = "network_limit_notify_channel";
    private static final int NOTIFICATION_ID_BASE = 1;
    private static final int NOTIFICATION_ID_DATA_FLOW = 2;
    private static final int NOTIFICATION_ID_LIMIT_ACCESS = 1;
    private static final int NOTIFY_CMODEM_STATUS_FAIL = -1;
    private static final int NOTIFY_CMODEM_STATUS_SUCCESS = 1;
    private static final String NR_OPTION_MODE = "nr_option_mode";
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE5 = 5;
    private static final String NULL_ICC_ATR = "null";
    private static final int OPTION_UNKNOWN = 0;
    private static final String OUTGOING_SMS_LIMIT = "outgoing_limit";
    private static final int PARATYPE_BASIC_COMM = 1;
    private static final int PARATYPE_CELLULAR_CLOUD = 2;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final String PATTERN_SIP = "^sip:(\\+)?[0-9]+@[^@]+";
    private static final String PATTERN_TEL = "^tel:(\\+)?[0-9]+";
    private static final String POLICY_REMOVE_ALL = "remove_all_policy";
    private static final String POLICY_REMOVE_SINGLE = "remove_single_policy";
    private static final String PROP_NERVER_NOTIFY = "persist.radio.never.notify";
    private static final String READ_PRIVILEGED_PHONE_STATE = "android.permission.READ_PRIVILEGED_PHONE_STATE";
    private static final String REC_DECISION_NAME = "com.huawei.dsdscardmanager.intent.action.Rec";
    private static final int REDUCE_TYPE_CELL = 1;
    private static final int REDUCE_TYPE_WIFI = 2;
    private static final int REGISTER_TYPE = 1;
    private static final int REGISTER_TYPE_ANTENNA = 2;
    private static final int REGISTER_TYPE_BAND = 1;
    private static final int REGISTER_TYPE_MAX_TX_POWER = 4;
    private static final String REMOVE_TYPE = "removeType";
    private static final int RETRY_MAX_TIME = 20;
    private static final String SECONDARY_CARD_CALL_DATA = "incall_data_switch";
    private static final String SEND_NVCFG_ERRORNO_PROP = "sys.nvcfg_errorno";
    private static final int SERVICE_2G_OFF = 0;
    private static final int SIGNAL_TYPE_CDMA = 3;
    private static final int SIGNAL_TYPE_CDMALTE = 6;
    private static final int SIGNAL_TYPE_EVDO = 4;
    private static final int SIGNAL_TYPE_GSM = 1;
    private static final int SIGNAL_TYPE_LTE = 5;
    private static final int SIGNAL_TYPE_NR = 7;
    private static final int SIGNAL_TYPE_UMTS = 2;
    private static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final int STATE_IN_AIR_PLANE_MODE = 1;
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    private static final int SUB_NONE = -1;
    private static final int SUCCESS = 0;
    private static final int SWITCH_ENABLE = 1;
    private static final String TIME_MODE = "time_mode";
    private static final int TYPEMASK_PARATYPE_BASIC_COMM = 0;
    private static final int TYPEMASK_PARATYPE_CELLULAR_CLOUD = 1;
    private static final int TYPE_DATA_FLOW_EXCEED_THRESHOLD_NOTIFY = 2;
    private static final int TYPE_DATA_SEND_TO_TELEPHONY_SHOW_TOAST = 4;
    private static final int TYPE_INTELLIGENT_RECOMMENDATION = 1;
    private static final int TYPE_INVALID_THIS_MONTH_NOTIFY = 5;
    private static final int TYPE_NETWORK_SPEED_LIMIT = 3;
    private static final int TYPE_REPORT_NO_RETRY_FOR_PDP_FAIL = 702;
    private static final int TYPE_REPORT_SWITCH_BACK = 701;
    private static final int UNREGISTER_TYPE = 2;
    private static final int UPLINK_FREQUENCY_REPORT_ENABLE = 1;
    private static final String USED_DECISION_NAME = "com.huawei.dsdscardmanager.intent.action.Used";
    private static final String USED_OF_DAY = "used_number_day";
    private static final String USED_OF_MONTH = "used_number_month";
    private static final String USED_OF_WEEK = "used_number_week";
    private static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
    private static final String WEEK_MODE = "week_mode";
    private static final String WEEK_MODE_TIME = "week_mode_time";
    private static int queryCount = 0;
    private static HwPhoneService sInstance = null;
    private IHwCommBoosterServiceManager bm = null;
    private Message getLaaStateCompleteMsg = null;
    private IPhoneCallback mAntiFakeBaseStationCB = null;
    private AppOpsManager mAppOps;
    private byte[] mCardTrayInfo = null;
    private final Object mCardTrayLock = new Object();
    private int[] mCardTypes = new int[SIM_NUM];
    private Context mContext;
    private int mEncryptCallStatus = 0;
    private boolean mHasNotifyLimit = false;
    private HwCustPhoneService mHwCustPhoneService = null;
    private HwInnerPhoneService mHwInnerPhoneService = new HwInnerPhoneService();
    private HwPhoneServiceEx mHwPhoneServiceEx;
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.android.internal.telephony.HwPhoneService.AnonymousClass4 */

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x007a: APUT  
          (r5v0 java.lang.Object[])
          (0 ??[int, short, byte, char])
          (wrap: java.lang.Integer : 0x0076: INVOKE  (r4v2 java.lang.Integer) = (r4v1 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
         */
        public void callBack(int type, Bundle b) throws RemoteException {
            HwPhoneService hwPhoneService = HwPhoneService.this;
            hwPhoneService.log("receive booster callback type " + type);
            if (b == null) {
                HwPhoneService.this.log("data is null");
                return;
            }
            Message response = HwPhoneService.this.mMainHandler.obtainMessage();
            switch (type) {
                case 1:
                    HwPhoneService.this.log("TYPE_INTELLIGENT_RECOMMENDATION");
                    HwPhoneService.this.handleIntelligentRecommendation();
                    return;
                case 2:
                    if (SystemPropertiesEx.getBoolean(HwPhoneService.PROP_NERVER_NOTIFY, false)) {
                        HwPhoneService.this.log("user clicked never notify");
                        return;
                    }
                    HwPhoneService.this.log("data flow notify");
                    HwPhoneService.this.showDataFlowNotification(b.getInt("slaveCardDataFlowCost"));
                    return;
                case 3:
                    HwPhoneService.this.handleNetworkSpeedLimit(b);
                    return;
                case 4:
                    response.what = HwPhoneService.EVENT_SMART_CARD_TOAST;
                    HwPhoneService.this.mMainHandler.sendMessage(response);
                    return;
                case 5:
                    Context context = HwPhoneService.this.mContext;
                    int resourceId = HwPartResourceUtils.getResourceId("data_near_limit");
                    int i = 1;
                    Object[] objArr = new Object[1];
                    if (HwPhoneService.this.getDefault4GSlotId() != 1) {
                        i = 2;
                    }
                    objArr[0] = Integer.valueOf(i);
                    HwPhoneService.this.showNotification(context.getString(resourceId, objArr), HwPhoneService.this.mContext.getString(HwPartResourceUtils.getResourceId("data_near_explain")), 0);
                    return;
                case 6:
                    response.what = 701;
                    HwPhoneService.this.mMainHandler.sendMessage(response);
                    return;
                case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                    response.what = 702;
                    HwPhoneService.this.mMainHandler.sendMessage(response);
                    return;
                case 8:
                    response.what = HwPhoneService.EVENT_SMART_CARD_BIND_TO_SUBCARD_FREE_TRAFFIC_TOAST;
                    HwPhoneService.this.mMainHandler.sendMessage(response);
                    return;
                default:
                    return;
            }
        }
    };
    private IPhoneCallback mImsaToMapconInfoCB = null;
    private IntelligenceCardObserver mIntelligenceCardObserver = null;
    private BroadcastReceiver mIntelligenceCardReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwPhoneService.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (HwPhoneService.ACTION_CANCEL_NOTIFY.equals(action)) {
                    HwPhoneService.this.log("Received cancel broadcast");
                    HwPhoneService.this.dismissNotification(1);
                } else if (HwPhoneService.ACTION_SWITCH_BACK.equals(action)) {
                    HwPhoneService.this.log("switch data to default slotid");
                    HwPhoneService.this.reportToBoosterForSwitchBack();
                    HwPhoneService.this.sendActionToNetLinkManager();
                    HwPhoneService.this.dismissNotification(2);
                } else if (HwPhoneService.ACTION_NEVER_NOTIFY.equals(action)) {
                    SystemPropertiesEx.set(HwPhoneService.PROP_NERVER_NOTIFY, "true");
                    HwPhoneService.this.dismissNotification(2);
                } else if (HwPhoneService.ACTION_IGNORE_NOTIFY.equals(action)) {
                    HwPhoneService.this.log("ignore data flow notify");
                    HwPhoneService.this.dismissNotification(2);
                } else {
                    HwPhoneService.this.log("unhandle action");
                }
            }
        }
    };
    private long mLastNotifyTime = 0;
    private final Object mLock = new Object();
    private BroadcastReceiver mMDMSmsReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwPhoneService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                String removeType = intent.getStringExtra(HwPhoneService.REMOVE_TYPE);
                HwPhoneService hwPhoneService = HwPhoneService.this;
                hwPhoneService.log("removeType: " + removeType);
                boolean isOutgoing = intent.getBooleanExtra(HwPhoneService.IS_OUTGOING, false);
                if (HwPhoneService.POLICY_REMOVE_SINGLE.equals(removeType)) {
                    String timeMode = intent.getStringExtra(HwPhoneService.TIME_MODE);
                    HwPhoneService hwPhoneService2 = HwPhoneService.this;
                    hwPhoneService2.log("mMDMSmsReceiver onReceive : " + timeMode);
                    HwPhoneService.this.clearSinglePolicyData(context, timeMode, isOutgoing);
                } else if (HwPhoneService.POLICY_REMOVE_ALL.equals(removeType)) {
                    HwPhoneService.this.clearAllPolicyData(context);
                }
            }
        }
    };
    private MainHandler mMainHandler;
    private HandlerThread mMessageThread = new HandlerThread("HuaweiPhoneTempService");
    private NotificationManager mNotificationManager;
    private PhoneExt mPhone;
    private PhoneServiceReceiver mPhoneServiceReceiver;
    private PhoneStateHandler mPhoneStateHandler;
    private PhoneExt[] mPhones = null;
    private IPhoneCallback mRadioAvailableIndCB = null;
    private IPhoneCallback mRadioNotAvailableIndCB = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwPhoneService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            TelephonyManager telephonyManager;
            if (context != null && intent != null) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    if (HwPhoneService.this.mPhone == null) {
                        HwPhoneService.loge("received ACTION_SIM_STATE_CHANGED, but mPhone is null!");
                        return;
                    }
                    int slotId = intent.getIntExtra("phone", -1000);
                    String simState = intent.getStringExtra("ss");
                    if (!HwPhoneService.IS_FULL_NETWORK_SUPPORTED && "READY".equals(simState)) {
                        HwPhoneService.this.log("mReceiver receive ACTION_SIM_STATE_CHANGED READY,check pref network type");
                        HwPhoneService.this.mPhone.getPreferredNetworkType(HwPhoneService.this.mMainHandler.obtainMessage(9));
                    }
                    if (HwPhoneService.IS_FULL_NETWORK_SUPPORTED && "IMSI".equals(simState)) {
                        HwPhoneService.this.setSingleCardPrefNetwork(slotId);
                    }
                }
                if (HwPhoneService.IS_SHOW_DIALOG_FOR_NO_SIM && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && (telephonyManager = (TelephonyManager) context.getSystemService("phone")) != null && 1 == telephonyManager.getSimState()) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, 3);
                    dialogBuilder.setTitle(33685979).setMessage(33685980).setCancelable(false).setPositiveButton(33685981, (DialogInterface.OnClickListener) null);
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.getWindow().setType(2003);
                    alertDialog.show();
                }
            }
        }
    };
    private final ArrayList<Record> mRecords = new ArrayList<>();
    private Object[] mRegAntStateCallbackArray = {this.mRegAntStateCallbackLists0, this.mRegAntStateCallbackLists1};
    private final ArrayList<Record> mRegAntStateCallbackLists0 = new ArrayList<>();
    private final ArrayList<Record> mRegAntStateCallbackLists1 = new ArrayList<>();
    private Object[] mRegBandClassCallbackArray = {this.mRegBandClassCallbackLists0, this.mRegBandClassCallbackLists1};
    private final ArrayList<Record> mRegBandClassCallbackLists0 = new ArrayList<>();
    private final ArrayList<Record> mRegBandClassCallbackLists1 = new ArrayList<>();
    private Object[] mRegMaxTxPowerCallbackArray = {this.mRegMaxTxPowerCallbackList0, this.mRegMaxTxPowerCallbackList1};
    private final ArrayList<Record> mRegMaxTxPowerCallbackList0 = new ArrayList<>();
    private final ArrayList<Record> mRegMaxTxPowerCallbackList1 = new ArrayList<>();
    private final ArrayList<IBinder> mRemoveRecordsList = new ArrayList<>();
    private String[] mServiceCellBand = new String[2];
    private Message mSetNrOptionModeMsg = null;
    private final Object mSetOPinLock = new Object();
    private BroadcastReceiver mSetRadioCapDoneReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwPhoneService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(intent.getAction())) {
                HwPhoneService.this.handleSwitchSlotDone(intent);
            }
        }
    };
    private Message mSetTempCtrlCompleteMsg = null;
    boolean mSupportEncryptCall = SystemPropertiesEx.getBoolean("persist.sys.cdma_encryption", false);
    private IPhoneCallback mUlFreqReportCB = null;
    private int mUlFreqRptSubId = -1;
    private int retryCount = 0;
    private Message sendLaaCmdCompleteMsg = null;
    private boolean setResultForChangePin = false;
    private boolean setResultForPinLock = false;

    public HwPhoneService() {
        this.mMessageThread.start();
        this.mMainHandler = new MainHandler(this.mMessageThread.getLooper());
        this.mPhoneStateHandler = new PhoneStateHandler(this.mMessageThread.getLooper());
        this.mHwCustPhoneService = (HwCustPhoneService) HwCustUtils.createObj(HwCustPhoneService.class, new Object[]{this, this.mMessageThread.getLooper()});
    }

    private static void saveInstance(HwPhoneService service) {
        sInstance = service;
    }

    public static HwPhoneService getInstance() {
        return sInstance;
    }

    /* access modifiers changed from: private */
    public static void slog(String msg) {
        RlogEx.i(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    /* access modifiers changed from: private */
    public static void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void logi(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        return cm != null && !ConnectivityManagerEx.isNetworkSupported(0, cm);
    }

    public void setPhone(PhoneExt[] phones, Context context) {
        int numPhones = TelephonyManagerEx.getDefault().getPhoneCount();
        this.mPhones = new PhoneExt[numPhones];
        for (int i = 0; i < numPhones; i++) {
            log("Creating PhoneExt sub = " + i);
            this.mPhones[i] = phones[i];
            this.mCardTypes[i] = -1;
        }
        PhoneExt[] phoneExtArr = this.mPhones;
        this.mPhone = phoneExtArr[0];
        this.mContext = context;
        this.mHwPhoneServiceEx = new HwPhoneServiceEx(this.mContext, phoneExtArr, this.mMainHandler);
        HwCustPhoneService hwCustPhoneService = this.mHwCustPhoneService;
        if (hwCustPhoneService != null && hwCustPhoneService.isDisable2GServiceCapabilityEnabled()) {
            this.mHwCustPhoneService.setPhone(this.mPhone, this.mContext);
        }
        log("setPhone numPhones = " + numPhones);
        initService();
        boolean dualImsEnable = HwImsManagerInner.isDualImsAvailable();
        if (phones != null && phones.length == 2 && dualImsEnable) {
            InCallDataStateManager.getInstance().makeInCallDataStateManager(context, phones);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: com.android.internal.telephony.HwPhoneService */
    /* JADX WARN: Multi-variable type inference failed */
    private void initService() {
        log("initService()");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        ServiceManagerEx.addService("phone_huawei", this);
        saveInstance(this);
        initPrefNetworkTypeChecker();
        if (!IS_MTK_PLATFORM && IS_SUPPORT_ENCRYPT_CALL) {
            registerForRadioOnInner();
            if (this.mPhoneServiceReceiver == null) {
                this.mPhoneServiceReceiver = new PhoneServiceReceiver();
            }
        }
        registerMDMSmsReceiver();
        registerSetRadioCapDoneReceiver();
        registerRadioAvailableForNrOptionMode();
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            registerForIccStatusChanged();
            registerIntelligenceCardReceiver();
            initCommBoosterManager();
            this.mIntelligenceCardObserver = new IntelligenceCardObserver(new Handler());
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(INTELLIGENCE_CARD_SETTING_DB), true, this.mIntelligenceCardObserver);
        }
    }

    private void registerForIccStatusChanged() {
        log("registerForIccStatusChanged");
        if (this.mPhones == null) {
            log("register failed, mphones is null");
            return;
        }
        for (int i = 0; i < SIM_NUM; i++) {
            Integer index = Integer.valueOf(i);
            this.mPhones[i].getCi().registerForIccStatusChanged(this.mMainHandler, (int) EVENT_ICC_STATUS_CHANGED, index);
            this.mPhones[i].getCi().registerForAvailable(this.mMainHandler, (int) EVENT_ICC_STATUS_CHANGED, index);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onIccStatusChanged(Integer index) {
        this.mPhones[index.intValue()].getCi().queryCardType(this.mMainHandler.obtainMessage(EVENT_QUERY_CARD_TYPE_DONE, index));
        this.mPhones[index.intValue()].getCi().iccGetATR(this.mMainHandler.obtainMessage(EVENT_ICC_GET_ATR_DONE, index));
    }

    private void saveCardTypeProperties(int cardTypeResult, int index) {
        int cardType = -1;
        int uiccOrIcc = (cardTypeResult & 240) >> 4;
        int appType = cardTypeResult & 15;
        if (appType != 0) {
            if (appType != 1) {
                if (appType == 2) {
                    cardType = 30;
                } else if (appType == 3) {
                    if (uiccOrIcc == 2) {
                        cardType = 43;
                    } else if (uiccOrIcc == 1) {
                        cardType = 41;
                    }
                }
            } else if (uiccOrIcc == 2) {
                cardType = RETRY_MAX_TIME;
            } else if (uiccOrIcc == 1) {
                cardType = 10;
            }
        }
        log("uiccOrIcc :  " + uiccOrIcc + ", appType : " + appType + ", cardType : " + cardType);
        if (index == 0) {
            SystemPropertiesEx.set("gsm.sim1.type", String.valueOf(cardType));
        } else {
            SystemPropertiesEx.set("gsm.sim2.type", String.valueOf(cardType));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Integer getCiIndex(Message msg) {
        if (msg == null) {
            return 0;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (AsyncResultEx.from(msg.obj) != null) {
            AsyncResultEx arEx = AsyncResultEx.from(msg.obj);
            if (arEx.getUserObj() == null || !(arEx.getUserObj() instanceof Integer)) {
                return 0;
            }
            return (Integer) arEx.getUserObj();
        }
        RlogEx.i(LOG_TAG, "invalid index, use default");
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        RlogEx.i(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    private void logd(String msg) {
        RlogEx.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    private void logForOemHook(String msg) {
        RlogEx.i("HwPhoneService_OEMHOOK", msg);
    }

    private void logForSar(String msg) {
        RlogEx.i("HwPhoneService_SAR", msg);
    }

    public String getDemoString() {
        enforceReadPermission();
        return BuildConfig.FLAVOR + this.mPhone + this.mContext;
    }

    public String getMeidForSubscriber(int slot) {
        if (!canReadPhoneState(slot, "getMeid") || slot < 0 || slot >= this.mPhones.length) {
            return null;
        }
        if (-1 != SystemPropertiesEx.getInt("persist.radio.stack_id_0", -1)) {
            slot = SystemPropertiesEx.getInt("persist.radio.stack_id_0", -1);
            log("QC after switch slot = " + slot);
        }
        return this.mPhones[slot].getMeid();
    }

    public String getPesnForSubscriber(int slot) {
        enforceReadPermission();
        if (slot < 0 || slot >= this.mPhones.length) {
            return null;
        }
        if (-1 != SystemPropertiesEx.getInt("persist.radio.stack_id_0", -1)) {
            slot = SystemPropertiesEx.getInt("persist.radio.stack_id_0", -1);
            log("QC after switch slot = " + slot);
        }
        return this.mPhones[slot].getPesn();
    }

    public int getSubState(int slotId) {
        enforceReadPermission();
        if (SubscriptionControllerEx.getInstance() != null) {
            return SubscriptionControllerEx.getInstance().getSubState(slotId);
        }
        return 0;
    }

    public void setUserPrefDataSlotId(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (HwSubscriptionManager.getInstance() != null) {
            HwSubscriptionManager.getInstance().setUserPrefDataSlotId(slotId);
        } else {
            loge("HwSubscriptionManager is null!!");
        }
    }

    public int getDataStateForSubscriber(int phoneId) {
        enforceReadPermission();
        if (!isValidPhone(phoneId)) {
            return 0;
        }
        return this.mPhones[phoneId].getConvertDataConnectionState();
    }

    public String getNVESN() {
        enforceReadPermission();
        return this.mPhone.getNVESN();
    }

    public void closeRrc() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int dataSubId = SubscriptionControllerEx.getInstance().getDefaultDataSubId();
        if (dataSubId >= 0) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (dataSubId < phoneExtArr.length) {
                phoneExtArr[dataSubId].closeRrc();
            }
        }
    }

    public boolean isCTCdmaCardInGsmMode() {
        enforceReadPermission();
        if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
            return false;
        }
        int i = 0;
        while (true) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (i >= phoneExtArr.length) {
                return true;
            }
            if (phoneExtArr[i].isPhoneTypeCdma()) {
                return false;
            }
            i++;
        }
    }

    public boolean isLTESupported() {
        boolean result;
        int networkMode = PhoneExt.getPreferredNetworkMode();
        if (!(networkMode == EVENT_SET_PREF_NETWORK_TYPE_DONE || networkMode == EVENT_RETRY_SET_PREF_NETWORK_TYPE || networkMode == 16 || networkMode == 18 || networkMode == 21 || networkMode == EVENT_RADIO_NOT_AVAILABLE)) {
            switch (networkMode) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                    break;
                default:
                    result = true;
                    break;
            }
            log("isLTESupported " + result);
            return result;
        }
        result = false;
        log("isLTESupported " + result);
        return result;
    }

    public void setDefaultMobileEnable(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        boolean isDataAlwaysOn = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data_always_on", 0) != 0) {
            isDataAlwaysOn = true;
        }
        if (isDataAlwaysOn && !enabled) {
            log("setDefaultMobileEnable: isDataAlwaysOn && !enabled, return.");
        } else if (IS_PLATFORM_SUPPORT_VSIM) {
            HwVSimUtils.setDefaultMobileEnableForVSim(enabled);
        }
    }

    public void setDataEnabledWithoutPromp(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        long identity = Binder.clearCallingIdentity();
        try {
            int phoneId = SubscriptionManagerEx.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
            if (phoneId >= 0 && phoneId < this.mPhones.length) {
                this.mPhones[phoneId].setDataEnabledWithoutPromp(enabled);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setDataRoamingEnabledWithoutPromp(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int phoneId = SubscriptionManagerEx.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (phoneId >= 0) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (phoneId < phoneExtArr.length) {
                phoneExtArr[phoneId].setDataRoamingEnabledWithoutPromp(enabled);
            }
        }
    }

    private Object sendRequest(int command, Object argument) {
        return sendRequest(command, argument, null);
    }

    private Object sendRequest(int command, Object argument, Integer subId) {
        if (Looper.myLooper() != this.mMainHandler.getLooper()) {
            MainThreadRequest request = new MainThreadRequest(argument, subId);
            this.mMainHandler.obtainMessage(command, request).sendToTarget();
            synchronized (request) {
                while (request.result == null) {
                    try {
                        request.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return request.result;
        }
        throw new RuntimeException("This method will deadlock if called from the main thread.");
    }

    public void setPreferredNetworkType(int nwMode) {
        enforceModifyPermissionOrCarrierPrivilege();
        log("[enter]setPreferredNetworkType " + nwMode);
        if (!TelephonyManagerEx.isMultiSimEnabled() || HwFullNetworkManager.getInstance().getBalongSimSlot() != 1) {
            this.mPhone = this.mPhones[0];
        } else {
            this.mPhone = this.mPhones[1];
        }
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("4G-Switch mPhone is null. return!");
        } else {
            phoneExt.setPreferredNetworkType(nwMode, this.mMainHandler.obtainMessage(4, Integer.valueOf(nwMode)));
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:17:0x0041 */
    /* JADX DEBUG: Multi-variable search result rejected for r0v3, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v6 */
    /* JADX WARN: Type inference failed for: r0v7 */
    /* JADX WARN: Type inference failed for: r0v8 */
    /* JADX WARN: Type inference failed for: r0v10, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v17 */
    public int getServiceAbilityForSlotId(int slotId, int type) {
        int ability;
        enforceReadPermission();
        if (!isValidSlotId(slotId) || !isValidServiceAbilityType(type)) {
            return 0;
        }
        if (type != 1 || HwModemCapability.isCapabilitySupport(29)) {
            int curPrefMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, slotId);
            if (type == 1) {
                ability = HwNetworkTypeUtils.isNrServiceOn(curPrefMode);
            } else {
                if (HwTelephonyManager.getDefault().isNrSupported()) {
                    ability = HwNetworkTypeUtils.isNrServiceOn(curPrefMode);
                } else {
                    ability = 0;
                }
                if (ability == 0) {
                    ability = HwNetworkTypeUtils.isLteServiceOn(curPrefMode);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("getServiceAbilityForSlotId, curPrefMode = ");
            sb.append(curPrefMode);
            sb.append(", slotId =");
            sb.append(slotId);
            sb.append(", type =");
            sb.append(type);
            sb.append(", ability =");
            int ability2 = ability == true ? 1 : 0;
            int ability3 = ability == true ? 1 : 0;
            int ability4 = ability == true ? 1 : 0;
            int ability5 = ability == true ? 1 : 0;
            int ability6 = ability == true ? 1 : 0;
            int ability7 = ability == true ? 1 : 0;
            sb.append(ability2);
            log(sb.toString());
            return ability;
        }
        log("Parameter is invalid for MODEM_CAP_SUPPORT_NR is false, will return.");
        return 0;
    }

    public void setServiceAbilityForSlotId(int slotId, int type, int ability) {
        PhoneExt phone;
        enforceModifyPermissionOrCarrierPrivilege();
        log("=4G-Switch= setServiceAbilityForSlotId: type=" + type + ", ability=" + ability + ", slotId=" + slotId);
        if (isValidSlotId(slotId) && isValidServiceAbilityType(type) && isValidServiceAbility(ability)) {
            if (type == 1 && !HwModemCapability.isCapabilitySupport(29)) {
                log("Parameter is invalid for MODEM_CAP_SUPPORT_NR is false, will return.");
            } else if (!TelephonyManagerEx.isMultiSimEnabled() || (!HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK && !HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING)) {
                if (TelephonyManagerEx.isMultiSimEnabled()) {
                    phone = this.mPhones[slotId];
                    if (slotId == getDefault4GSlotId()) {
                        this.mPhone = phone;
                    }
                } else {
                    phone = this.mPhone;
                }
                if (phone == null) {
                    loge("4G-Switch phone is null. return!");
                    return;
                }
                int networkType = getNetworkType(slotId, ability, phone, type);
                if (HwNetworkManagerImpl.getDefault().isNetworkModeAsynchronized(phone)) {
                    HwNetworkManagerImpl.getDefault().handle4GSwitcherForNoMdn(phone, networkType);
                    sendLteServiceSwitchResult(slotId, true);
                    return;
                }
                phone.setPreferredNetworkType(networkType, this.mMainHandler.obtainMessage(5, networkType, slotId));
                log("=4G-Switch= setPreferredNetworkType-> " + networkType);
            } else if (type == 1) {
                log("FullNetwork: Qcom or Mtk NR.");
                HwFullNetworkManager.getInstance().setServiceAbilityForQCOM(slotId, type, ability, HwNetworkTypeUtils.getNrOnMappingMode());
            } else {
                HwFullNetworkManager.getInstance().setLteServiceAbilityForQCOM(slotId, ability, HwNetworkTypeUtils.getLteOnMappingMode());
            }
        }
    }

    private int selectNetworkType(int slotId, int type, int ability, int nrOnNetworkType, int lteOnNetworkType, int lteOffNetworkType) {
        if (type == 0) {
            return ability == 1 ? lteOnNetworkType : lteOffNetworkType;
        }
        HwCustPhoneService hwCustPhoneService = this.mHwCustPhoneService;
        int custNetworkMode = hwCustPhoneService != null ? hwCustPhoneService.getNrOnOffMappingNetworkMode(slotId, type, ability) : -1;
        if (custNetworkMode != -1) {
            return custNetworkMode;
        }
        return ability == 1 ? nrOnNetworkType : lteOnNetworkType;
    }

    @Deprecated
    public void setLteServiceAbilityForSlotId(int slotId, int ability) {
        setServiceAbilityForSlotId(slotId, 0, ability);
    }

    private int getNetworkType(int slotId, int ability, PhoneExt phone, int type) {
        int networkType;
        HwCustPhoneService hwCustPhoneService;
        if (HwNetworkTypeUtils.IS_MODEM_FULL_PREFMODE_SUPPORTED) {
            networkType = calculateNetworkType(ability, type);
        } else if (phone.isPhoneTypeCdma()) {
            networkType = selectNetworkType(slotId, type, ability, 64, 8, 4);
        } else if (IS_GSM_NONSUPPORT) {
            networkType = selectNetworkType(slotId, type, ability, 68, 12, 2);
        } else {
            networkType = selectNetworkType(slotId, type, ability, 65, 9, 3);
        }
        if (!HuaweiTelephonyConfigs.isHisiPlatform() && !TelephonyManagerEx.isMultiSimEnabled() && IS_FULL_NETWORK_SUPPORTED) {
            if (phone.isPhoneTypeCdma()) {
                networkType = ability == 1 ? 10 : 7;
            } else {
                networkType = ability == 1 ? RETRY_MAX_TIME : 18;
            }
        }
        if (type != 0 || (hwCustPhoneService = this.mHwCustPhoneService) == null || !hwCustPhoneService.isDisable2GServiceCapabilityEnabled() || this.mHwCustPhoneService.get2GServiceAbility() != 0) {
            return networkType;
        }
        return this.mHwCustPhoneService.getNetworkTypeBaseOnDisabled2G(networkType);
    }

    private int calculateNetworkType(int ability, int type) {
        int mappingNetworkType;
        int curPrefMode = getCurrentNetworkTypeFromDB();
        if (ability == 1) {
            mappingNetworkType = HwNetworkTypeUtils.getOnModeFromMapping(curPrefMode);
        } else {
            mappingNetworkType = HwNetworkTypeUtils.getOffModeFromMapping(curPrefMode);
        }
        log("=4G-Switch= curPrefMode = " + curPrefMode + " ,mappingNetworkType = " + mappingNetworkType);
        if (-1 != mappingNetworkType) {
            return mappingNetworkType;
        }
        return type == 0 ? ability == 1 ? HwNetworkTypeUtils.getLteOnMappingMode() : HwNetworkTypeUtils.getLteOffMappingMode() : ability == 1 ? HwNetworkTypeUtils.getNrOnMappingMode() : HwNetworkTypeUtils.getNrOffMappingMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getCurrentNetworkTypeFromDB() {
        return HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveNetworkTypeToDB(int setPrefMode) {
        HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, setPrefMode);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetLteSwitchDone(Message msg) {
        log("=4G-Switch= in handleSetLteSwitchDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            loge("=4G-Switch= ar is null!");
            return;
        }
        int setPrefMode = msg.arg1;
        int slotId = msg.arg2;
        if (ar.getException() != null) {
            loge("=4G-Switch=, has exception.");
            sendLteServiceSwitchResult(slotId, false);
            setImsSwitchFromDB(slotId, setPrefMode);
            return;
        }
        int curPrefMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, slotId);
        log("=4G-Switch= subId:" + slotId + " curPrefMode in db:" + curPrefMode + " setPrefMode:" + setPrefMode);
        if (curPrefMode != setPrefMode) {
            HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, slotId, setPrefMode);
        }
        sendLteServiceSwitchResult(slotId, true);
    }

    private void sendLteServiceSwitchResult(int subId, boolean result) {
        if (this.mContext == null) {
            loge("=4G-Switch= mContext is null. return!");
            return;
        }
        Intent intent = new Intent("com.huawei.telephony.PREF_4G_SWITCH_DONE");
        intent.putExtra("subscription", subId);
        intent.putExtra("slot", subId);
        intent.putExtra("setting_result", result);
        this.mContext.sendOrderedBroadcast(intent, "android.permission.READ_PHONE_STATE");
        log("=4G-Switch= result is " + result + ". broadcast PREFERRED_4G_SWITCH_DONE");
    }

    private void setImsSwitchFromDB(int slotId, int prefMode) {
        if (HwNetworkTypeUtils.isNrServiceOn(prefMode)) {
            if (SettingsEx.Systemex.getInt(this.mContext.getContentResolver(), HwImsManager.isDualImsAvailable() ? HW_VOLTE_USER_SWITCH_DUALIMS[slotId] : HW_VOLTE_USER_SWITCH, 0) == 0) {
                setNrSwitch(slotId, false);
            }
        }
    }

    public int getLteServiceAbility() {
        enforceReadPermission();
        return getLteServiceAbilityForSlotId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0);
    }

    @Deprecated
    public void setLteServiceAbility(int ability) {
        setServiceAbilityForSlotId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, 0, ability);
    }

    public int getLteServiceAbilityForSlotId(int slotId) {
        return getServiceAbilityForSlotId(slotId, 0);
    }

    public int get2GServiceAbility() {
        HwCustPhoneService hwCustPhoneService = this.mHwCustPhoneService;
        if (hwCustPhoneService == null || !hwCustPhoneService.isDisable2GServiceCapabilityEnabled()) {
            return 0;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        return this.mHwCustPhoneService.get2GServiceAbility();
    }

    public void set2GServiceAbility(int ability) {
        HwCustPhoneService hwCustPhoneService = this.mHwCustPhoneService;
        if (hwCustPhoneService != null && hwCustPhoneService.isDisable2GServiceCapabilityEnabled()) {
            enforceModifyPermissionOrCarrierPrivilege();
            this.mHwCustPhoneService.set2GServiceAbility(ability);
        }
    }

    private void enforceModifyPermissionOrCarrierPrivilege() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            log("No modify permission, check carrier privilege next.");
            if (hasCarrierPrivileges() != 1) {
                loge("No Carrier Privilege.");
                throw new SecurityException("No modify permission or carrier privilege.");
            }
        }
    }

    private int hasCarrierPrivileges() {
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            log("hasCarrierPrivileges: mPhone is null");
            return -1;
        }
        UiccCardExt card = UiccControllerExt.getInstance().getUiccCard(phoneExt.getPhoneId());
        if (card != null) {
            return card.getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager());
        }
        loge("hasCarrierPrivileges: No UICC");
        return -1;
    }

    public boolean isSubDeactivedByPowerOff(long sub) {
        enforceReadPermission();
        log("isSubDeactivedByPowerOff: in HuaweiPhoneService");
        SubscriptionControllerEx sc = SubscriptionControllerEx.getInstance();
        int slot = SubscriptionManagerEx.getSlotIndex((int) sub);
        if (TelephonyManagerEx.getDefault().getSimState(slot) == 5 && sc.getSubState(slot) == 0) {
            return true;
        }
        return false;
    }

    public boolean isNeedToRadioPowerOn(long sub) {
        enforceReadPermission();
        log("isNeedToRadioPowerOn: in HuaweiPhoneService");
        SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
        if (HuaweiTelephonyConfigs.isQcomPlatform() || (TelephonyManagerEx.MultiSimVariantsExt.DSDS != TelephonyManagerEx.getMultiSimConfiguration() && !SystemPropertiesEx.getBoolean("ro.hwpp.set_uicc_by_radiopower", false))) {
            log("isNeedToRadioPowerOn: hisi dsds not in");
            int phoneId = SubscriptionManagerEx.getPhoneId((int) sub);
            if (PhoneFactoryExt.getPhone(phoneId) != null && PhoneFactoryExt.getPhone(phoneId).getServiceState().getState() == 3) {
                return true;
            }
        }
        if (!isSubDeactivedByPowerOff((long) ((int) sub))) {
            return true;
        }
        subscriptionControllerEx.activateSubId((int) sub);
        return false;
    }

    private void enforceReadPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
    }

    public void updateCrurrentPhone(int lteSlot) {
        log("updateCrurrentPhone with lteSlot = " + lteSlot);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (isValidSlotId(lteSlot)) {
            this.mPhone = this.mPhones[lteSlot];
        } else {
            loge("Invalid slot ID");
        }
    }

    public void setDefaultDataSlotId(int slotId) {
        log("setDefaultDataSlotId: slotId = " + slotId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(slotId)) {
            loge("setDefaultDataSlotId: invalid slotId!!!");
            return;
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, slotId);
        SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
        if (subscriptionControllerEx != null) {
            subscriptionControllerEx.setDefaultDataSubId(SubscriptionManagerEx.getSubIdUsingSlotId(slotId));
        } else {
            loge("SubscriptionControllerEx.getInstance() is null");
        }
    }

    public int getDefault4GSlotId() {
        try {
            return Settings.System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            return 0;
        }
    }

    public void setDefault4GSlotId(int slotId, Message response) {
        int uid = Binder.getCallingUid();
        if (!HwFullNetworkManager.getInstance().isCMCCDsdxDisable() || uid == 1000 || uid == 1001 || uid == 0) {
            enforceModifyPermissionOrCarrierPrivilege();
            log("in setDefault4GSlotId for slotId: " + slotId);
            Message msg = this.mMainHandler.obtainMessage(EVENT_SET_4G_SLOT_DONE);
            msg.obj = response;
            HwFullNetworkManager.getInstance().setMainSlot(slotId, msg);
            return;
        }
        loge("setDefault4GSlotId: Disallowed call for uid " + uid);
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        enforceReadPermission();
        log("in isSetDefault4GSlotIdEnabled.");
        if (IS_PLATFORM_SUPPORT_VSIM && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate() || !HwVSimUtils.isAllowALSwitch())) {
            log("vsim is working, so isSetDefault4GSlotIdEnabled return false");
            return false;
        } else if (!TelephonyManagerEx.isMultiSimEnabled()) {
            return false;
        } else {
            SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
            if (TelephonyManagerEx.getDefault().getSimState(0) == 5 && subscriptionControllerEx.getSubState(0) == 0 && TelephonyManagerEx.getDefault().getSimState(1) == 5 && subscriptionControllerEx.getSubState(1) == 0) {
                return false;
            }
            if (!HwFullNetworkConfig.IS_HISI_DSDX || ((TelephonyManagerEx.getDefault().getSimState(0) != 5 || subscriptionControllerEx.getSubState(0) != 0) && (TelephonyManagerEx.getDefault().getSimState(1) != 5 || subscriptionControllerEx.getSubState(1) != 0))) {
                return HwFullNetworkManager.getInstance().isSwitchDualCardSlotsEnabled();
            }
            log("isSetDefault4GSlotIdEnabled return false when has sim INACTIVE when IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT");
            return false;
        }
    }

    public void waitingSetDefault4GSlotDone(boolean waiting) {
        enforceReadPermission();
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            HwFullNetworkManager.getInstance().setWaitingSwitchBalongSlot(waiting);
        }
    }

    public int getPreferredDataSubscription() {
        enforceReadPermission();
        int subId = SubscriptionControllerEx.getInstance().getPreferredDataSubscription();
        log("getPreferredDataSubscription return subId = " + subId);
        return subId;
    }

    public int getOnDemandDataSubId() {
        throw new RuntimeException("Deleted method, not support getOnDemandDataSubId!");
    }

    public String getCdmaGsmImsi() {
        log("getCdmaGsmImsi: in HWPhoneService");
        enforceReadPermission();
        PhoneExt[] phoneExtArr = this.mPhones;
        for (PhoneExt phone : phoneExtArr) {
            if (phone.getPhoneType() == 2) {
                return phone.getCdmaGsmImsi();
            }
        }
        return null;
    }

    public String getCdmaGsmImsiForSubId(int slotId) {
        enforceReadPermission();
        log("getCdmaGsmImsi: in HWPhoneService slotId:" + slotId);
        if (!isValidSlotId(slotId)) {
            log("slotId is not avaible!");
            return null;
        }
        PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
        if (phone == null) {
            return null;
        }
        if (phone.getCdmaGsmImsi() != null || !isCtSimCard(slotId)) {
            return phone.getCdmaGsmImsi();
        }
        log("getCdmaGsmImsi is null");
        IccRecordsEx iccRecords = UiccControllerExt.getInstance().getIccRecords(slotId, UiccControllerExt.APP_FAM_3GPP2);
        if (iccRecords != null) {
            return iccRecords.getCdmaGsmImsi();
        }
        return null;
    }

    public int getUiccCardType(int slotId) {
        log("getUiccCardType: in HwPhoneService");
        enforceReadPermission();
        if (slotId >= 0) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (slotId < phoneExtArr.length) {
                return phoneExtArr[slotId].getUiccCardType();
            }
        }
        return this.mPhone.getUiccCardType();
    }

    public boolean isCardUimLocked(int slotId) {
        log("isCardUimLocked for slotId " + slotId);
        enforceReadPermission();
        UiccCardExt card = UiccControllerExt.getInstance().getUiccCard(slotId);
        if (card != null) {
            return card.isCardUimLocked();
        }
        loge("isCardUimLocked: No UICC for slotId" + slotId);
        return false;
    }

    public int getSpecCardType(int slotId) {
        log("getSpecCardType for slotId " + slotId);
        enforceReadPermission();
        if (!HuaweiTelephonyConfigs.isHisiPlatform() || slotId < 0 || slotId >= SIM_NUM) {
            return -1;
        }
        return this.mCardTypes[slotId];
    }

    public boolean isRadioOn(int slot) {
        log("isRadioOn for slotId " + slot);
        enforceReadPermission();
        if (slot < 0) {
            return false;
        }
        PhoneExt[] phoneExtArr = this.mPhones;
        if (slot < phoneExtArr.length) {
            return phoneExtArr[slot].isRadioOn();
        }
        return false;
    }

    public Bundle getCellLocation(int slotId) {
        CellLocation cellLoc;
        log("getCellLocation: in HwPhoneService");
        int uid = Binder.getCallingUid();
        if (uid == 1000 || uid == 1001) {
            enforceCellLocationPermission("getCellLocation");
            Bundle data = new Bundle();
            if (slotId >= 0) {
                PhoneExt[] phoneExtArr = this.mPhones;
                if (slotId < phoneExtArr.length) {
                    CellLocation cellLoc2 = phoneExtArr[slotId].getCellLocation();
                    if (cellLoc2 != null) {
                        CellLocationEx.fillInNotifierBundle(cellLoc2, data);
                    }
                    return data;
                }
            }
            if (!IS_PLATFORM_SUPPORT_VSIM || slotId != 2) {
                CellLocation cellLoc3 = this.mPhone.getCellLocation();
                if (cellLoc3 != null) {
                    CellLocationEx.fillInNotifierBundle(cellLoc3, data);
                }
                return data;
            }
            PhoneExt phone = PhoneExt.getVsimPhone();
            if (!(phone == null || (cellLoc = phone.getCellLocation()) == null)) {
                CellLocationEx.fillInNotifierBundle(cellLoc, data);
            }
            return data;
        }
        loge("getCellLocation not allowed for third-party apps");
        return null;
    }

    private void enforceCellLocationPermission(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", message);
    }

    public String getCdmaMlplVersion() {
        log("getCdmaMlplVersion: in HwPhoneService");
        enforceReadPermission();
        PhoneExt[] phoneExtArr = this.mPhones;
        for (PhoneExt phoneExt : phoneExtArr) {
            if (phoneExt.getPhoneType() == 2) {
                return phoneExt.getCdmaMlplVersion();
            }
        }
        return null;
    }

    public String getCdmaMsplVersion() {
        log("getCdmaMsplVersion: in HwPhoneService");
        enforceReadPermission();
        PhoneExt[] phoneExtArr = this.mPhones;
        for (PhoneExt phoneExt : phoneExtArr) {
            if (phoneExt.getPhoneType() == 2) {
                return phoneExt.getCdmaMsplVersion();
            }
        }
        return null;
    }

    private boolean isSystemApp(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            if (appInfo == null || (appInfo.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            loge(packageName + " not found.");
        }
    }

    private boolean canReadPhoneState(int subId, String message) {
        String[] callingPackageName;
        String callingPackage = null;
        PackageManager pm = this.mContext.getPackageManager();
        if (!(pm == null || (callingPackageName = pm.getPackagesForUid(Binder.getCallingUid())) == null)) {
            callingPackage = callingPackageName[0];
        }
        try {
            return TelephonyPermissionsExt.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, message);
        } catch (SecurityException phoneStateException) {
            if (isSystemApp(callingPackage)) {
                loge(callingPackage + " allowed.");
                return true;
            }
            throw phoneStateException;
        }
    }

    public String getUniqueDeviceId(int scope, String callingPackageName) {
        IHwBehaviorCollectManager manager = HwFrameworkSecurityPartsFactory.getInstance().getInnerHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.PHONEINTERFACE_GETDEVICEID);
        }
        if (!TelephonyPermissionsExt.checkCallingOrSelfReadDeviceIdentifiers(this.mContext, 0, callingPackageName, "getUniqueDeviceId")) {
            loge("getUniqueDeviceId can't pass checkCallingOrSelfReadDeviceIdentifiers.");
            return null;
        }
        String sharedPref = DEVICEID_PREF;
        if (1 == scope) {
            sharedPref = IMEI_PREF;
        }
        String deviceId = getDeviceIdFromSP(sharedPref);
        if (deviceId != null) {
            return deviceId;
        }
        String newDeviceId = readDeviceIdFromLL(scope);
        if (newDeviceId != null && !newDeviceId.matches("^0*$")) {
            deviceId = newDeviceId;
            saveDeviceIdToSP(newDeviceId, sharedPref);
        }
        if (!TextUtils.isEmpty(deviceId) || !isWifiOnly(this.mContext)) {
            return deviceId;
        }
        log("Current is wifi-only version, return SN number as DeviceId");
        return Build.SERIAL;
    }

    private void saveDeviceIdToSP(String deviceId, String sharedPref) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        try {
            deviceId = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), deviceId);
        } catch (Exception e) {
            loge("HwAESCryptoUtil encrypt excepiton");
        }
        editor.putString(sharedPref, deviceId);
        editor.commit();
    }

    private String getDeviceIdFromSP(String sharedPref) {
        String deviceId = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(sharedPref, null);
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkManager.getInstance().getMasterPassword(), deviceId);
        } catch (Exception e) {
            loge("HwAESCryptoUtil decrypt excepiton");
            return deviceId;
        }
    }

    private String readDeviceIdFromLL(int scope) {
        int phoneId = 0;
        if (HwModemCapability.isCapabilitySupport(15) && TelephonyManagerEx.isMultiSimEnabled() && SystemPropertiesEx.getBoolean("persist.sys.dualcards", false)) {
            if (getWaitingSwitchBalongSlot()) {
                log("readDeviceIdFromLL getWaitingSwitchBalongSlot");
                return null;
            }
            phoneId = HwTelephonyManagerInner.getDefault().isImeiBindSlotSupported() ? 0 : HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        }
        log("readDeviceIdFromLL: phoneId=" + phoneId);
        PhoneExt[] phoneExtArr = this.mPhones;
        if (phoneExtArr == null || phoneExtArr[phoneId] == null) {
            return null;
        }
        if (1 == scope) {
            return phoneExtArr[phoneId].getImei();
        }
        if (HuaweiTelephonyConfigs.isChinaTelecom()) {
            return this.mPhones[phoneId].getMeid();
        }
        String deviceId = this.mPhones[phoneId].getImei();
        if (deviceId != null) {
            return deviceId;
        }
        return this.mPhones[phoneId].getMeid();
    }

    public boolean getWaitingSwitchBalongSlot() {
        log("getWaitingSwitchBalongSlot start");
        return HwFullNetworkManager.getInstance().getWaitingSwitchBalongSlot();
    }

    public boolean setISMCOEX(String setISMCoex) {
        enforceModifyPermissionOrCarrierPrivilege();
        int phoneId = 0;
        if (TelephonyManagerEx.isMultiSimEnabled() && SystemPropertiesEx.getBoolean("persist.sys.dualcards", false)) {
            phoneId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        }
        PhoneExt[] phoneExtArr = this.mPhones;
        if (phoneExtArr == null || phoneExtArr[phoneId] == null) {
            loge("mPhones is invalid!");
            return false;
        }
        log("setISMCoex =" + setISMCoex);
        return this.mPhones[phoneId].setISMCOEX(setISMCoex);
    }

    public boolean isDomesticCard(int slotId) {
        logd("isDomesticCard start");
        enforceReadPermission();
        return true;
    }

    public boolean setWifiTxPower(int power) {
        logi("setWifiTxPower:" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        return true;
    }

    public boolean setCellTxPower(int power) {
        logi("setCellTxPower:" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (getCommandsInterface() == null) {
            return true;
        }
        getCommandsInterface().setPowerGrade(power, (Message) null);
        return true;
    }

    private CommandsInterfaceEx getCommandsInterface() {
        if (PhoneFactoryExt.getDefaultPhone() != null) {
            return PhoneFactoryExt.getDefaultPhone().getCi();
        }
        return null;
    }

    public String[] queryServiceCellBand() {
        log("queryServiceCellBand");
        enforceReadPermission();
        PhoneExt phone = getPhone(SubscriptionControllerEx.getInstance().getDefaultDataSubId());
        if (phone == null) {
            return new String[0];
        }
        if (ServiceStateEx.isCdma(ServiceStateEx.getRilDataRadioTechnology(phone.getServiceState()))) {
            this.mServiceCellBand = new String[2];
            String[] strArr = this.mServiceCellBand;
            strArr[0] = "CDMA";
            strArr[1] = "0";
        } else {
            synchronized (this.mLock) {
                phone.getCi().queryServiceCellBand(this.mMainHandler.obtainMessage(6));
                for (boolean isWait = true; isWait; isWait = false) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        loge("interrupted while trying to update by index");
                    }
                }
            }
        }
        String[] strArr2 = this.mServiceCellBand;
        if (strArr2 == null) {
            return new String[0];
        }
        return (String[]) strArr2.clone();
    }

    public void handleQueryCellBandDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            this.mServiceCellBand = null;
        } else {
            this.mServiceCellBand = (String[]) ar.getResult();
        }
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleQueryEncryptFeatureDone(Message msg) {
        log("query encrypt call feature received");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        PhoneExt phone = (PhoneExt) ar.getUserObj();
        if (ar.getException() != null || ar.getResult() == null) {
            loge("query encrypt call feature failed, has exception.");
            if (msg.arg1 < 10) {
                this.mMainHandler.sendEmptyMessageDelayed(EVENT_QUERY_ENCRYPT_FEATURE, 1000);
            } else {
                queryCount = 0;
            }
        } else {
            byte[] res = (byte[]) ar.getResult();
            if (res.length > 0) {
                boolean z = true;
                if ((res[0] & 15) != 1) {
                    z = false;
                }
                this.mSupportEncryptCall = z;
                this.mEncryptCallStatus = res[0] >>> 4;
                boolean z2 = this.mSupportEncryptCall;
                if (z2) {
                    SystemPropertiesEx.set("persist.sys.cdma_encryption", Boolean.toString(z2));
                    checkEcSwitchStatusInNV(phone, this.mEncryptCallStatus);
                }
            }
        }
    }

    public boolean registerForRadioAvailable(IPhoneCallback callback) {
        log("registerForRadioAvailable");
        setPhoneForMainSlot();
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioAvailableIndCB = callback;
        phoneExt.getCi().registerForAvailable(this.mMainHandler, (int) EVENT_RADIO_AVAILABLE, (Object) null);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRadioAvailableInd(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (this.mRadioAvailableIndCB == null) {
            loge("handleRadioAvailableInd mRadioAvailableIndCB is null");
        } else if (ar.getException() == null) {
            try {
                this.mRadioAvailableIndCB.onCallback1(0);
            } catch (RemoteException e) {
                loge("handleRadioAvailableInd RemoteException");
            }
        } else {
            loge("radio available ind exception");
        }
    }

    public boolean unregisterForRadioAvailable(IPhoneCallback callback) {
        log("unregisterForRadioAvailable");
        setPhoneForMainSlot();
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioAvailableIndCB = null;
        phoneExt.getCi().unregisterForAvailable(this.mMainHandler);
        return true;
    }

    public boolean registerForRadioNotAvailable(IPhoneCallback callback) {
        log("registerForRadioNotAvailable");
        setPhoneForMainSlot();
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioNotAvailableIndCB = callback;
        phoneExt.getCi().registerForNotAvailable(this.mMainHandler, (int) EVENT_RADIO_NOT_AVAILABLE, (Object) null);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRadioNotAvailableInd(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (this.mRadioNotAvailableIndCB == null) {
            loge("handleRadioNotAvailableInd mRadioNotAvailableIndCB is null");
        } else if (ar.getException() == null) {
            try {
                this.mRadioNotAvailableIndCB.onCallback1(0);
            } catch (RemoteException e) {
                loge("handleRadioNotAvailableInd RemoteException");
            }
        } else {
            loge("radio not available ind exception");
        }
    }

    public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) {
        log("unregisterForRadioNotAvailable");
        setPhoneForMainSlot();
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioNotAvailableIndCB = null;
        phoneExt.getCi().unregisterForNotAvailable(this.mMainHandler);
        return true;
    }

    public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) {
        log("registerCommonImsaToMapconInfo");
        setPhoneForMainSlot();
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("phone is null!");
            return false;
        }
        this.mImsaToMapconInfoCB = callback;
        phoneExt.getCi().registerCommonImsaToMapconInfo(this.mMainHandler, (int) EVENT_COMMON_IMSA_MAPCON_INFO, (Object) null);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCommonImsaToMapconInfoInd(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        log("handleCommonImsaToMapconInfoInd");
        if (this.mImsaToMapconInfoCB == null) {
            loge("handleCommonImsaToMapconInfoInd mImsaToMapconInfoCB is null");
        } else if (ar.getException() == null) {
            Bundle bundle = new Bundle();
            bundle.putByteArray("imsa2mapcon_msg", (byte[]) ar.getResult());
            try {
                this.mImsaToMapconInfoCB.onCallback3(0, 0, bundle);
            } catch (RemoteException e) {
                loge("handleCommonImsaToMapconInfoInd RemoteException");
            }
        } else {
            loge("imsa to mapcon info exception");
        }
    }

    public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) {
        log("unregisterCommonImsaToMapconInfo");
        setPhoneForMainSlot();
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("phone is null!");
            return false;
        }
        this.mImsaToMapconInfoCB = null;
        phoneExt.getCi().unregisterCommonImsaToMapconInfo(this.mMainHandler);
        return true;
    }

    private void handleRemoveListLocked() {
        int size = this.mRemoveRecordsList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                removeRecord(this.mRemoveRecordsList.get(i));
            }
            this.mRemoveRecordsList.clear();
        }
    }

    private void removeRecord(IBinder binder) {
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            for (int i = 0; i < recordCount; i++) {
                if (this.mRecords.get(i).binder == binder) {
                    this.mRecords.remove(i);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPhoneEventWithCallback(int phoneId, int event, int arg, Bundle bundle) {
        log("notifyPhoneEventWithCallback phoneId = " + phoneId + " event = " + event);
        synchronized (this.mRecords) {
            Iterator<Record> it = this.mRecords.iterator();
            while (it.hasNext()) {
                Record r = it.next();
                if (r.matchPhoneEvent(event) && r.phoneId == phoneId) {
                    try {
                        r.callback.onCallback3(event, arg, bundle);
                    } catch (RemoteException e) {
                        this.mRemoveRecordsList.add(r.binder);
                    }
                }
            }
            handleRemoveListLocked();
        }
    }

    public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) {
        Record r;
        log("registerForPhoneEvent, phoneId = " + phoneId + ", events = " + Integer.toHexString(events));
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
            return false;
        } else if (callback == null) {
            loge("phone or callback is null!");
            return false;
        } else {
            synchronized (this.mRecords) {
                IBinder b = callback.asBinder();
                int n = this.mRecords.size();
                int i = 0;
                while (true) {
                    if (i >= n) {
                        r = new Record();
                        r.binder = b;
                        this.mRecords.add(r);
                        log("registerForPhoneEvent: add new record");
                        break;
                    }
                    r = this.mRecords.get(i);
                    if (b == r.binder) {
                        break;
                    }
                    i++;
                }
                r.callback = callback;
                r.phoneId = phoneId;
                r.events = events;
            }
            if ((events & 1) != 0) {
                this.mPhones[phoneId].getCi().registerForAvailable(this.mPhoneStateHandler, 1, Integer.valueOf(phoneId));
            }
            if ((events & 2) != 0) {
                this.mPhones[phoneId].getCi().registerForNotAvailable(this.mPhoneStateHandler, 2, Integer.valueOf(phoneId));
            }
            if ((events & 4) != 0) {
                this.mPhones[phoneId].getCi().registerCommonImsaToMapconInfo(this.mPhoneStateHandler, 4, Integer.valueOf(phoneId));
            }
            return true;
        }
    }

    public void unregisterForPhoneEvent(IPhoneCallback callback) {
        if (callback != null) {
            removeRecord(callback.asBinder());
        }
    }

    public boolean isRadioAvailable() {
        return isRadioAvailableByPhoneId(getPhoneIdForMainSlot());
    }

    public boolean isRadioAvailableByPhoneId(int phoneId) {
        if (checkPhoneIsValid(phoneId)) {
            return this.mPhones[phoneId].isRadioAvailable();
        }
        loge("phone is invalid!");
        return false;
    }

    public void setNrSwitch(int phoneId, boolean value) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
        } else {
            this.mPhones[phoneId].setNrSwitch(value, (Message) null);
        }
    }

    public void setImsSwitchByPhoneId(int phoneId, boolean value) {
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
        } else {
            this.mPhones[phoneId].setImsSwitch(value);
        }
    }

    public boolean getImsSwitch() {
        return getImsSwitchByPhoneId(getPhoneIdForMainSlot());
    }

    public void setImsSwitch(boolean value) {
        setImsSwitchByPhoneId(getPhoneIdForMainSlot(), value);
    }

    public boolean getImsSwitchByPhoneId(int phoneId) {
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
            return false;
        }
        boolean result = this.mPhones[phoneId].getImsSwitch();
        log("ImsSwitch = " + result);
        return result;
    }

    public void setImsDomainConfig(int domainType) {
        setImsDomainConfigByPhoneId(getPhoneIdForMainSlot(), domainType);
    }

    public void setImsDomainConfigByPhoneId(int phoneId, int domainType) {
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
        } else {
            this.mPhones[phoneId].setImsDomainConfig(domainType);
        }
    }

    public boolean handleMapconImsaReq(byte[] Msg) {
        return handleMapconImsaReqByPhoneId(getPhoneIdForMainSlot(), Msg);
    }

    public boolean handleMapconImsaReqByPhoneId(int phoneId, byte[] Msg) {
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
            return false;
        }
        this.mPhones[phoneId].handleMapconImsaReq(Msg);
        return true;
    }

    public int getUiccAppType() {
        return getUiccAppTypeByPhoneId(getPhoneIdForMainSlot());
    }

    public int getUiccAppTypeByPhoneId(int phoneId) {
        if (checkPhoneIsValid(phoneId)) {
            return getUiccAppTypeByPhone(this.mPhones[phoneId]);
        }
        loge("phone is invalid!");
        return 0;
    }

    private int getUiccAppTypeByPhone(PhoneExt phone) {
        int i = AnonymousClass6.$SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppTypeEx[phone.getCurrentUiccAppType().ordinal()];
        if (i == 1) {
            return 1;
        }
        if (i == 2) {
            return 2;
        }
        if (i == 3) {
            return 3;
        }
        if (i == 4) {
            return 4;
        }
        if (i != 5) {
            return 0;
        }
        return 5;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.HwPhoneService$6  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppTypeEx = new int[IccCardApplicationStatusEx.AppTypeEx.values().length];

        static {
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppTypeEx[IccCardApplicationStatusEx.AppTypeEx.APPTYPE_SIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppTypeEx[IccCardApplicationStatusEx.AppTypeEx.APPTYPE_USIM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppTypeEx[IccCardApplicationStatusEx.AppTypeEx.APPTYPE_RUIM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppTypeEx[IccCardApplicationStatusEx.AppTypeEx.APPTYPE_CSIM.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppTypeEx[IccCardApplicationStatusEx.AppTypeEx.APPTYPE_ISIM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public int getImsDomain() {
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt != null) {
            return getImsDomainByPhoneId(phoneExt.getPhoneId());
        }
        loge("phone is null!");
        return -1;
    }

    public int getImsDomainByPhoneId(int phoneId) {
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
            return -1;
        } else if (ImsManagerExt.isWfcEnabledByPlatform(this.mContext)) {
            return ((int[]) sendRequest(CMD_IMS_GET_DOMAIN, null, Integer.valueOf(phoneId)))[0];
        } else {
            log("vowifi not support!");
            return -1;
        }
    }

    public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) {
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt != null) {
            return handleUiccAuthByPhoneId(phoneExt.getPhoneId(), auth_type, rand, auth);
        }
        loge("phone is null!");
        return null;
    }

    public UiccAuthResponse handleUiccAuthByPhoneId(int phoneId, int auth_type, byte[] rand, byte[] auth) {
        if (checkPhoneIsValid(phoneId)) {
            return (UiccAuthResponse) sendRequest(CMD_UICC_AUTH, new UiccAuthPara(auth_type, rand, auth), Integer.valueOf(phoneId));
        }
        loge("phone is invalid!");
        return null;
    }

    private void initPrefNetworkTypeChecker() {
        if (!TelephonyManagerEx.isMultiSimEnabled() && !HuaweiTelephonyConfigs.isHisiPlatform()) {
            log("initPrefNetworkTypeChecker");
            if (IS_SHOW_DIALOG_FOR_NO_SIM) {
                this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
            }
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCmdImsGetDomain(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_IMS_GET_DOMAIN_DONE, request);
        if (request.subId != null) {
            this.mPhones[request.subId.intValue()].getImsDomain(onCompleted);
            return;
        }
        this.mPhone.getImsDomain(onCompleted);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCmdUiccAuth(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        UiccAuthPara para = (UiccAuthPara) request.argument;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_UICC_AUTH_DONE, request);
        if (request.subId != null) {
            this.mPhones[request.subId.intValue()].handleUiccAuth(para.auth_type, para.rand, para.auth, onCompleted);
            return;
        }
        this.mPhone.handleUiccAuth(para.auth_type, para.rand, para.auth, onCompleted);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetPrefNetworkTypeDone(Message msg) {
        PhoneExt phoneExt;
        log("handleGetPrefNetworkTypeDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() == null) {
            int prefNetworkType = ((int[]) ar.getResult())[0];
            int currentprefNetworkTypeInDB = getCurrentNetworkTypeFromDB();
            log("prefNetworkType:" + prefNetworkType + " currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            if (prefNetworkType == currentprefNetworkTypeInDB) {
                return;
            }
            if (currentprefNetworkTypeInDB == -1 || (phoneExt = this.mPhone) == null) {
                log("INVALID_NETWORK_MODE in DB,set 4G-Switch on");
                setServiceAbilityForSlotId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, 0, 1);
                return;
            }
            this.mPhone.setPreferredNetworkType(currentprefNetworkTypeInDB, this.mMainHandler.obtainMessage(5, currentprefNetworkTypeInDB, phoneExt.getPhoneId()));
            log("setPreferredNetworkType -> currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            return;
        }
        log("getPreferredNetworkType exception");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSingleCardPrefNetwork(int slotId) {
        int prefNetwork;
        if (!isValidSlotId(slotId)) {
            loge("invalid slotId " + slotId);
            return;
        }
        int ability = getLteServiceAbility();
        if (isCDMASimCard(slotId)) {
            prefNetwork = 1 == ability ? 10 : 7;
        } else {
            prefNetwork = 1 == ability ? RETRY_MAX_TIME : 18;
        }
        this.mPhone.setPreferredNetworkType(prefNetwork, this.mMainHandler.obtainMessage(EVENT_SET_PREF_NETWORK_TYPE_DONE, slotId, prefNetwork));
        log("setSingleCardPrefNetwork, LTE ability = " + ability + ", pref network = " + prefNetwork);
    }

    private boolean isCDMASimCard(int slotId) {
        HwTelephonyManagerInner hwTelephonyManager = HwTelephonyManagerInner.getDefault();
        return hwTelephonyManager != null && hwTelephonyManager.isCDMASimCard(slotId);
    }

    private void handleSetPrefNetworkTypeDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int slot = msg.arg1;
        int setPrefMode = msg.arg2;
        if (ar == null || ar.getException() != null) {
            int i = this.retryCount;
            if (i < RETRY_MAX_TIME) {
                this.retryCount = i + 1;
                this.mMainHandler.sendMessageDelayed(this.mMainHandler.obtainMessage(EVENT_RETRY_SET_PREF_NETWORK_TYPE, slot, setPrefMode), 3000);
                return;
            }
            this.retryCount = 0;
            loge("handleSetPrefNetworkTypeDone faild.");
            return;
        }
        if (getCurrentNetworkTypeFromDB() != setPrefMode) {
            saveNetworkTypeToDB(setPrefMode);
        }
        this.retryCount = 0;
        log("handleSetPrefNetworkTypeDone, success.");
    }

    public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) {
        enforceReadPermission();
        if (!isValidSlotId(slotId)) {
            return false;
        }
        boolean isSuccess = handleWirelessStateRequest(1, type, slotId, callback);
        logForSar("registerForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) {
        enforceReadPermission();
        if (!isValidSlotId(slotId)) {
            return false;
        }
        boolean isSuccess = handleWirelessStateRequest(2, type, slotId, callback);
        logForSar("unregisterForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean setMaxTxPower(int type, int power) {
        log("setMaxTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (type == 2) {
            if (getCommandsInterface() != null) {
                getCommandsInterface().setWifiTxPowerGrade(power, (Message) null);
            }
        } else if (type == 1) {
            if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                log("setMaxTxPower: hisi");
                if (getCommandsInterface(0) != null) {
                    getCommandsInterface(0).setPowerGrade(power, (Message) null);
                }
                if (getCommandsInterface(1) != null) {
                    getCommandsInterface(1).setPowerGrade(power, (Message) null);
                }
            } else if (getCommandsInterface() != null) {
                getCommandsInterface().setPowerGrade(power, (Message) null);
            }
        }
        log("setMaxTxPower: end=" + power);
        return false;
    }

    private boolean handleWirelessStateRequest(int opertype, int type, int slotId, IPhoneCallback callback) {
        logForSar("In handleWirelessStateRequest service type=" + type + ",slotId=" + slotId);
        boolean isSuccess = false;
        CommandsInterfaceEx ci = getCommandsInterface(slotId);
        if (callback == null) {
            logForSar("handleWirelessStateRequest callback is null.");
            return false;
        } else if (ci == null) {
            logForSar("handleWirelessStateRequest ci is null.");
            return false;
        } else {
            if (opertype == 1) {
                isSuccess = registerUnitSarControl(type, slotId, ci, callback);
            } else if (opertype == 2) {
                isSuccess = unregisterUnitSarControl(type, slotId, ci, callback);
            }
            logForSar("handleWirelessStateRequest type=" + type + ",isSuccess=" + isSuccess);
            return isSuccess;
        }
    }

    private boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < SIM_NUM;
    }

    private boolean isValidServiceAbilityType(int type) {
        return type == 1 || type == 0;
    }

    private boolean isValidServiceAbility(int ability) {
        return ability == 1 || ability == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSarInfoUploaded(int type, Message msg) {
        log("in handleSarInfoUploaded.");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            loge("handleSarInfoUploaded error, ar exception.");
            return;
        }
        int slotId = ((Integer) ar.getUserObj()).intValue();
        CommandsInterfaceEx ci = getCommandsInterface(slotId);
        if (ci == null) {
            loge("handleSarInfoUploaded ci is null.");
        } else if (!handleSarDataFromModem(type, ar)) {
            log("handleSarInfoUploaded hasClient is false,so to close the switch of upload Sar Info in modem");
            closeSarInfoUploadSwitch(type, slotId, ci);
            unregisterSarRegistrant(type, slotId, ci);
        }
    }

    private boolean handleSarDataFromModem(int type, AsyncResultEx ar) {
        log("handleSarDataFromModem start");
        boolean hasClient = false;
        int slotId = ((Integer) ar.getUserObj()).intValue();
        Bundle bundle = getBundleData(ar);
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            loge("handleSarDataFromModem callbackArray is null.");
            return false;
        }
        synchronized (callbackArray[slotId]) {
            ArrayList<Record> callbackList = (ArrayList) callbackArray[slotId];
            for (int i = callbackList.size() - 1; i >= 0; i--) {
                Record r = callbackList.get(i);
                try {
                    r.callback.onCallback3(type, slotId, bundle);
                    hasClient = true;
                    log("handleSarDataFromModem oncallback r=" + r);
                } catch (RemoteException e) {
                    loge("handleSarDataFromModem callback false,ex is RemoteException");
                    callbackList.remove(r);
                } catch (Exception e2) {
                    loge("handleSarDataFromModem callback false,ex is Exception");
                    callbackList.remove(r);
                }
            }
            log("handleSarDataFromModem record size=" + callbackList.size());
        }
        return hasClient;
    }

    private boolean closeSarInfoUploadSwitch(int type, int slotId, CommandsInterfaceEx ci) {
        boolean isSuccess = false;
        if (type == 1) {
            ci.closeSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
            isSuccess = true;
        } else if (type == 2) {
            ci.closeSwitchOfUploadAntOrMaxTxPower(2);
            isSuccess = true;
        } else if (type == 4) {
            ci.closeSwitchOfUploadAntOrMaxTxPower(4);
            isSuccess = true;
        }
        log("closeSarInfoUploadSwitch mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + isSuccess);
        return isSuccess;
    }

    private Bundle getBundleData(AsyncResultEx ar) {
        log("getBundleData start");
        if (ar == null) {
            log("getBundleData: ar is null");
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap((byte[]) ar.getResult(), 0, 4);
        buf.order(ByteOrder.nativeOrder());
        int result = buf.getInt();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY1, result);
        log("getBundleData result = " + result);
        return bundle;
    }

    private CommandsInterfaceEx getCommandsInterface(int slotId) {
        if (isValidSlotId(slotId)) {
            return PhoneFactoryExt.getPhones()[slotId].getCi();
        }
        log("getCommandsInterface the slotId is invalid");
        return null;
    }

    private Object[] getCallbackArray(int type) {
        Object[] callbackArray = null;
        if (type == 1) {
            callbackArray = this.mRegBandClassCallbackArray;
        } else if (type == 2) {
            callbackArray = this.mRegAntStateCallbackArray;
        } else if (type == 4) {
            callbackArray = this.mRegMaxTxPowerCallbackArray;
        }
        logForSar("getCallbackArray type=" + type);
        return callbackArray;
    }

    private boolean unregisterUnitSarControl(int type, int slotId, CommandsInterfaceEx ci, IPhoneCallback callback) {
        boolean isSuccess;
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            logForSar("unregisterUnitSarControl callbackArray is null.");
            return false;
        }
        synchronized (callbackArray[slotId]) {
            ArrayList<Record> callbackList = (ArrayList) callbackArray[slotId];
            IBinder b = callback.asBinder();
            int recordCount = callbackList.size();
            logForSar("callbackArray[" + slotId + "] lenght is " + recordCount);
            int i = recordCount + -1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (callbackList.get(i).binder == b) {
                    logForSar("unregisterUnitSarControl remove: " + callbackList.get(i));
                    callbackList.remove(i);
                    hasFind = true;
                    break;
                } else {
                    i--;
                }
            }
            if (!hasFind) {
                logForSar("unregisterUnitSarControl not find the callback,type=" + type);
                isSuccess = true;
            } else {
                int recordCount2 = callbackList.size();
                logForSar("unregisterUnitSarControl record size = " + recordCount2);
                if (recordCount2 != 0 || !closeSarInfoUploadSwitch(type, slotId, ci)) {
                    isSuccess = true;
                } else {
                    isSuccess = unregisterSarRegistrant(type, slotId, ci);
                }
            }
        }
        return isSuccess;
    }

    private boolean registerUnitSarControl(int type, int slotId, CommandsInterfaceEx ci, IPhoneCallback callback) {
        logForSar("registerUnitSarControl start slotId=" + slotId + ",type=" + type);
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            logForSar("registerUnitSarControl callbackArray is null.");
            return false;
        } else if (!registerSarRegistrant(type, slotId, ci)) {
            logForSar("registerUnitSarControl mPhones[" + slotId + "] register return false");
            return false;
        } else {
            synchronized (callbackArray[slotId]) {
                ArrayList<Record> callbackList = (ArrayList) callbackArray[slotId];
                IBinder b = callback.asBinder();
                int n = callbackList.size();
                int i = 0;
                while (true) {
                    if (i >= n) {
                        break;
                    } else if (b == callbackList.get(i).binder) {
                        hasFind = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!hasFind) {
                    Record r = new Record();
                    r.binder = b;
                    r.callback = callback;
                    callbackList.add(r);
                    logForSar("registerUnitSarControl: add new record");
                }
                logForSar("registerUnitSarControl record size=" + callbackList.size());
            }
            return openSarInfoUploadSwitch(type, slotId, ci);
        }
    }

    private boolean openSarInfoUploadSwitch(int type, int slotId, CommandsInterfaceEx ci) {
        boolean isSuccess = false;
        if (type == 1) {
            ci.openSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
            isSuccess = true;
        } else if (type == 2) {
            isSuccess = ci.openSwitchOfUploadAntOrMaxTxPower(2);
        } else if (type == 4) {
            isSuccess = ci.openSwitchOfUploadAntOrMaxTxPower(4);
        }
        logForSar("openSarInfoUploadSwitch mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + isSuccess);
        return isSuccess;
    }

    private boolean registerSarRegistrant(int type, int slotId, CommandsInterfaceEx ci) {
        boolean isSuccess = false;
        Message message = null;
        boolean z = true;
        if (type == 1) {
            message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
        } else if (type == 2) {
            message = this.mMainHandler.obtainMessage(EVENT_REG_ANT_STATE_IND, Integer.valueOf(slotId));
        } else if (type == 4) {
            message = this.mMainHandler.obtainMessage(12, Integer.valueOf(slotId));
        }
        if (message != null && ci.registerSarRegistrant(type, message)) {
            isSuccess = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("registerSarRegistrant mPhones[");
        sb.append(slotId);
        sb.append("]: type = ");
        sb.append(type);
        sb.append(",isSuccess = ");
        if (isSuccess) {
            z = false;
        }
        sb.append(z);
        loge(sb.toString());
        return isSuccess;
    }

    private boolean unregisterSarRegistrant(int type, int slotId, CommandsInterfaceEx ci) {
        boolean isSuccess = false;
        Message message = null;
        boolean z = true;
        if (type == 1) {
            message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
        } else if (type == 2) {
            message = this.mMainHandler.obtainMessage(EVENT_REG_ANT_STATE_IND, Integer.valueOf(slotId));
        } else if (type == 4) {
            message = this.mMainHandler.obtainMessage(12, Integer.valueOf(slotId));
        }
        if (message != null && ci.unregisterSarRegistrant(type, message)) {
            isSuccess = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("unregisterSarRegistrant mPhones[");
        sb.append(slotId);
        sb.append("]: type = ");
        sb.append(type);
        sb.append(",isSuccess = ");
        if (isSuccess) {
            z = false;
        }
        sb.append(z);
        logForSar(sb.toString());
        return isSuccess;
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        boolean z;
        boolean res = false;
        try {
            enforceReadPermission();
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                PhoneExt[] phoneExtArr = this.mPhones;
                boolean res2 = false;
                for (PhoneExt phone : phoneExtArr) {
                    if (phone != null && phone.isPhoneTypeCdma()) {
                        if (!phone.cmdForECInfo(event, action, buf)) {
                            if (!requestForECInfo(phone, event, action, buf)) {
                                z = false;
                                res2 = z;
                            }
                        }
                        z = true;
                        res2 = z;
                    }
                }
                return res2;
            } else if (this.mPhone == null || !this.mPhone.isPhoneTypeCdma()) {
                return false;
            } else {
                if (this.mPhone.cmdForECInfo(event, action, buf) || requestForECInfo(this.mPhone, event, action, buf)) {
                    res = true;
                }
                return res;
            }
        } catch (Exception e) {
            loge("cmdForECInfo fail");
            return false;
        }
    }

    private void registerForRadioOnInner() {
        log("registerForRadioOnInner");
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null) {
            loge("registerForRadioOnInner failed, phone is null!");
        } else {
            phoneExt.getCi().registerForOn(this.mMainHandler, (int) EVENT_QUERY_ENCRYPT_FEATURE, (Object) null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEventQueryEncryptCall(PhoneExt phone) {
        if (phone != null && phone.isPhoneTypeCdma()) {
            byte[] req = {(byte) 0};
            if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                this.mSupportEncryptCall = phone.cmdForECInfo(7, 0, req);
                boolean z = this.mSupportEncryptCall;
                if (z) {
                    SystemPropertiesEx.set("persist.sys.cdma_encryption", Boolean.toString(z));
                }
                CommandsInterfaceEx ci = getCommandsInterface(phone.getPhoneId());
                if (ci != null) {
                    this.mEncryptCallStatus = ci.getEcCdmaCallVersion() ? 1 : 0;
                    checkEcSwitchStatusInNV(phone, this.mEncryptCallStatus);
                    return;
                }
                loge("qcomRil is null");
                return;
            }
            Message msg = this.mMainHandler.obtainMessage(EVENT_QUERY_ENCRYPT_FEATURE_DONE, phone);
            msg.arg1 = queryCount;
            phone.requestForECInfo(msg, 7, req);
            queryCount++;
            log("query EncryptCall Count : " + queryCount);
        }
    }

    private boolean requestForECInfo(PhoneExt phone, int event, int action, byte[] buf) {
        EncryptCallPara para;
        if (event == 3 || event == 5 || event == 6 || event == 7) {
            para = new EncryptCallPara(phone, event, null);
        } else {
            para = new EncryptCallPara(phone, event, buf);
        }
        byte[] res = (byte[]) sendRequest(500, para);
        int len = res.length;
        if (len == 1) {
            return res[0] > 0 && ((byte) (res[0] & 15)) == 1;
        }
        if (buf == null || 1 >= len || len > buf.length) {
            return false;
        }
        System.arraycopy(res, 0, buf, 0, len);
        log("requestForECInfo res length:" + len);
        return true;
    }

    public boolean isCtSimCard(int slotId) {
        enforceReadPermission();
        if (isValidSlotId(slotId)) {
            return isCtSimCardByPhone(this.mPhones[slotId]);
        }
        return false;
    }

    private boolean isCtSimCardByPhone(PhoneExt phone) {
        String iccId = phone.getIccSerialNumber();
        if (iccId == null || iccId.length() < 7) {
            return false;
        }
        return HwIccIdUtil.isCT(iccId.substring(0, 7));
    }

    public void notifyCModemStatus(int status, IPhoneCallback callback) {
        if (Binder.getCallingUid() == 1000) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_NOTIFY_CMODEM_STATUS);
            msg.obj = callback;
            try {
                if (getCommandsInterface() != null) {
                    getCommandsInterface().notifyCModemStatus(status, msg);
                }
            } catch (RuntimeException e) {
                loge("notifyCModemStatus got exception");
                if (callback != null) {
                    try {
                        callback.onCallback1(-1);
                    } catch (RemoteException e2) {
                        loge("notifyCModemStatus onCallback1 RemoteException");
                    }
                }
            }
        }
    }

    public boolean notifyDeviceState(String device, String state, String extra) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "notifyDeviceState");
        if (TextUtils.isEmpty(device) || TextUtils.isEmpty(state)) {
            return false;
        }
        Message response = this.mMainHandler.obtainMessage(EVENT_NOTIFY_DEVICE_STATE);
        try {
            if (getCommandsInterface() == null) {
                return false;
            }
            getCommandsInterface().notifyDeviceState(device, state, extra, response);
            return true;
        } catch (RuntimeException e) {
            loge("notifyDeviceState got exception");
            return false;
        }
    }

    private void checkEcSwitchStatusInNV(PhoneExt phone, int statusInNV) {
        if (phone != null && phone.isPhoneTypeCdma()) {
            int statusInDB = Settings.Secure.getInt(this.mContext.getContentResolver(), ENCRYPT_CALL_FEATURE_KEY, 0);
            log("checkEcSwitchStatus, encryptCall statusInNV=" + statusInNV + " statusInDB=" + statusInDB);
            if (statusInNV != statusInDB) {
                byte[] buf = {(byte) statusInDB};
                if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                    phone.requestForECInfo(this.mMainHandler.obtainMessage(EVENT_QUERY_ENCRYPT_FEATURE_DONE, phone), 8, buf);
                } else if (phone.cmdForECInfo(8, statusInDB, buf)) {
                    log("qcom reset NV success.");
                } else {
                    loge("qcom reset NV fail!");
                }
            }
        }
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        if (Binder.getCallingUid() != 1000) {
            loge("getCallingUid() != Process.SYSTEM_UID, return");
            return;
        }
        log("notifyCellularCommParaReady");
        if (1 == paratype) {
            setPhoneForMainSlot();
            PhoneExt phoneExt = this.mPhone;
            if (phoneExt == null) {
                loge("phone is null!");
                return;
            }
            phoneExt.notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_BASIC_COMM_PARA_UPGRADE_DONE));
        }
        if (2 == paratype) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (phoneExtArr[0] == null) {
                loge("phone is null!");
                return;
            }
            phoneExtArr[0].notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE));
            if (HwVSimManager.getDefault().isVSimEnabled()) {
                log("isVSimEnabled is true");
                PhoneExt[] phoneExtArr2 = this.mPhones;
                if (phoneExtArr2[2] == null) {
                    loge("phone[2] is null!");
                } else {
                    phoneExtArr2[2].notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE));
                }
            } else if (TelephonyManagerEx.isMultiSimEnabled()) {
                PhoneExt[] phoneExtArr3 = this.mPhones;
                if (phoneExtArr3[1] == null) {
                    loge("phone[1] is null!");
                } else {
                    phoneExtArr3[1].notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE));
                }
            } else {
                log("MultiSim is disable, mPhones[0] already processed above");
            }
        }
    }

    public boolean setPinLockEnabled(boolean enablePinLock, String password, int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "setPinLockEnabled");
        if (!isValidStatus(enablePinLock, slotId)) {
            return false;
        }
        this.setResultForPinLock = false;
        synchronized (this.mSetOPinLock) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_ENABLE_ICC_PIN_COMPLETE);
            PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
            if (phone == null) {
                return false;
            }
            phone.setIccLockEnabled(enablePinLock, password, msg);
            for (boolean isWait = true; isWait; isWait = false) {
                try {
                    this.mSetOPinLock.wait();
                } catch (InterruptedException e) {
                    loge("interrupted while trying to update by index");
                }
            }
            return this.setResultForPinLock;
        }
    }

    public boolean changeSimPinCode(String oldPinCode, String newPinCode, int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "changeSimPinCode");
        if (!isValidStatus(slotId)) {
            return false;
        }
        this.setResultForChangePin = false;
        synchronized (this.mSetOPinLock) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_CHANGE_ICC_PIN_COMPLETE);
            PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
            if (phone == null) {
                return false;
            }
            phone.changeIccLockPassword(oldPinCode, newPinCode, msg);
            for (boolean isWait = true; isWait; isWait = false) {
                try {
                    this.mSetOPinLock.wait();
                } catch (InterruptedException e) {
                    loge("interrupted while trying to update by index");
                }
            }
            return this.setResultForChangePin;
        }
    }

    private boolean isValidStatus(int slotId) {
        return isValidStatus(false, slotId);
    }

    private boolean isValidStatus(boolean enablePinLock, int slotId) {
        PhoneExt phone;
        if (!isValidSlotId(slotId) || (phone = PhoneFactoryExt.getPhone(slotId)) == null) {
            return false;
        }
        int airplaneMode = Settings.Global.getInt(phone.getContext().getContentResolver(), "airplane_mode_on", 0);
        if (1 == airplaneMode) {
            log("airplaneMode : " + airplaneMode);
            return false;
        }
        IccCardConstantsEx.StateEx mExternalState = phone.getIccCardState();
        if (mExternalState == IccCardConstantsEx.StateEx.PIN_REQUIRED || mExternalState == IccCardConstantsEx.StateEx.PUK_REQUIRED) {
            log("Need to unlock pin first! mExternalState : " + mExternalState);
            return false;
        }
        SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
        if (subscriptionControllerEx == null) {
            return false;
        }
        if (!(subscriptionControllerEx.getSubState(slotId) == 1)) {
            return false;
        }
        boolean pinState = phone.getIccLockEnabled();
        log("pinState = " + pinState);
        if (pinState && enablePinLock) {
            log("already in PIN_REQUIRED");
            return false;
        } else if (pinState || enablePinLock) {
            return true;
        } else {
            log("not in PIN_REQUIRED");
            return false;
        }
    }

    private void registerMDMSmsReceiver() {
        IntentFilter filter = new IntentFilter("com.huawei.devicepolicy.action.POLICY_CHANGED");
        Context context = this.mContext;
        if (context != null) {
            context.registerReceiver(this.mMDMSmsReceiver, filter);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearSinglePolicyData(Context context, String timeMode, boolean isOutgoing) {
        log("clearSinglePolicyData: " + timeMode);
        if (!HwInnerSmsManagerImpl.getDefault().isLimitNumOfSmsEnabled(isOutgoing)) {
            clearAllPolicyData(context, isOutgoing);
        } else if (!TextUtils.isEmpty(timeMode)) {
            String policyName = isOutgoing ? OUTGOING_SMS_LIMIT : INCOMING_SMS_LIMIT;
            char c = 65535;
            int hashCode = timeMode.hashCode();
            if (hashCode != -2105529586) {
                if (hashCode != -1628741630) {
                    if (hashCode == 1931104358 && timeMode.equals(DAY_MODE)) {
                        c = 0;
                    }
                } else if (timeMode.equals(MONTH_MODE)) {
                    c = 2;
                }
            } else if (timeMode.equals(WEEK_MODE)) {
                c = 1;
            }
            if (c == 0) {
                removeSharedDayModeData(context, policyName, USED_OF_DAY, DAY_MODE_TIME);
            } else if (c == 1) {
                removeSharedDayModeData(context, policyName, USED_OF_WEEK, WEEK_MODE_TIME);
            } else if (c == 2) {
                removeSharedDayModeData(context, policyName, USED_OF_MONTH, MONTH_MODE_TIME);
            }
        }
    }

    private void removeSharedDayModeData(Context context, String policyName, String keyUsedNum, String keyTime) {
        if (!TextUtils.isEmpty(policyName) && context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(policyName, 0).edit();
            editor.remove(keyUsedNum);
            editor.remove(keyTime);
            editor.commit();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAllPolicyData(Context context) {
        clearAllPolicyData(context, true);
        clearAllPolicyData(context, false);
    }

    private void clearAllPolicyData(Context context, boolean isOutgoing) {
        if (!HwInnerSmsManagerImpl.getDefault().isLimitNumOfSmsEnabled(isOutgoing) && context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(isOutgoing ? OUTGOING_SMS_LIMIT : INCOMING_SMS_LIMIT, 0).edit();
            editor.clear();
            editor.commit();
        }
    }

    private void registerSetRadioCapDoneReceiver() {
        if (HwNetworkTypeUtils.IS_DUAL_IMS_SUPPORTED && HuaweiTelephonyConfigs.isHisiPlatform()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
            Context context = this.mContext;
            if (context != null) {
                context.registerReceiver(this.mSetRadioCapDoneReceiver, filter);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSwitchSlotDone(Intent intent) {
        int switchSlotStep = intent.getIntExtra(HW_SWITCH_SLOT_STEP, -99);
        if (!HwFullNetworkManager.getInstance().isCMCCDsdxDisable() && 1 == switchSlotStep) {
            if (HwNetworkTypeUtils.isDualImsSwitchOpened()) {
                log("handleSwitchSlotDone. dual ims switch open, return.");
            } else if (1 == intent.getIntExtra("if_need_set_radio_cap", 0)) {
                log("handleSwitchSlotDone. not happen real sim slot change, return.");
            } else {
                HwNetworkTypeUtils.exchangeDualCardNetworkModeDB(this.mContext);
            }
        }
    }

    public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterfaceEx ci = getCommandsInterface(subId);
        if (ci == null) {
            loge("sendPseudocellCellInfo  ci is null.");
            return false;
        }
        ci.sendPseudocellCellInfo(type, lac, cid, radioTech, plmn, (Message) null);
        return true;
    }

    private PhoneExt getPhone(int subId) {
        return PhoneFactoryExt.getPhone(SubscriptionManagerEx.getPhoneId(subId));
    }

    private PhoneExt getPhoneBySlotId(int slotId) {
        return PhoneFactoryExt.getPhone(slotId);
    }

    public void setImsRegistrationStateForSubId(int slotId, boolean registered) {
        enforceModifyPermissionOrCarrierPrivilege();
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            phone.setImsRegistrationState(registered);
        }
    }

    public boolean isImsRegisteredForSubId(int slotId) {
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            return phone.isImsRegistered();
        }
        return false;
    }

    public boolean isWifiCallingAvailableForSubId(int slotId) {
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            return phone.isWifiCallingEnabled();
        }
        return false;
    }

    public boolean isVolteAvailableForSubId(int slotId) {
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            return phone.isVolteEnabled();
        }
        return false;
    }

    public boolean isVideoTelephonyAvailableForSubId(int slotId) {
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            return phone.isVideoEnabled();
        }
        return false;
    }

    public boolean isSlaveActive() {
        return InCallDataStateManager.getInstance().isSlaveActive();
    }

    public boolean isSwitchingToSlave() {
        return InCallDataStateManager.getInstance().isSwitchingToSlave();
    }

    public void registerImsCallStates(boolean enable, int phoneId) {
        InCallDataStateManager.getInstance().registerImsCallStates(enable, phoneId);
    }

    public boolean sendLaaCmd(int cmd, String reserved, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterfaceEx ci = getCommandsInterface(getDefault4GSlotId());
        this.sendLaaCmdCompleteMsg = response;
        if (ci == null) {
            loge("sendLaaCmd  ci is null.");
            sendResponseToTarget(response, -1);
            return false;
        }
        if (reserved == null) {
            reserved = BuildConfig.FLAVOR;
        }
        ci.sendLaaCmd(cmd, reserved, this.mMainHandler.obtainMessage(EVENT_SEND_LAA_CMD_DONE));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendLaaCmdDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int result = -1;
        if (ar != null && ar.getException() == null) {
            result = 0;
        }
        sendResponseToTarget(this.sendLaaCmdCompleteMsg, result);
        log("handleSendLaaCmdDone:sendLaaCmd result is " + result);
    }

    public boolean setTemperatureControlToModem(int level, int type, int subId, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        this.mSetTempCtrlCompleteMsg = response;
        if (level < 0 || type < 0) {
            loge("setTemperatureControlToModem input invalid, level:" + level + "type:" + type);
            sendResponseToTarget(response, false);
            return false;
        }
        CommandsInterfaceEx ci = getCommandsInterface(subId);
        if (ci == null) {
            loge("setTemperatureControlToModem ci is null");
            sendResponseToTarget(response, false);
            return false;
        }
        ci.setTemperatureControlToModem(level, type, this.mMainHandler.obtainMessage(EVENT_SET_TEMP_CTRL_DONE));
        return true;
    }

    public boolean getLaaDetailedState(String reserved, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterfaceEx ci = getCommandsInterface(getDefault4GSlotId());
        this.getLaaStateCompleteMsg = response;
        if (ci == null) {
            loge("getLaaDetailedState  ci is null.");
            sendResponseToTarget(response, -1);
            return false;
        }
        if (reserved == null) {
            reserved = BuildConfig.FLAVOR;
        }
        ci.getLaaDetailedState(reserved, this.mMainHandler.obtainMessage(EVENT_GET_LAA_STATE_DONE));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetLaaStateDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int result = -1;
        if (ar != null && ar.getException() == null && (ar.getResult() instanceof int[])) {
            result = ((int[]) ar.getResult())[0];
        }
        sendResponseToTarget(this.getLaaStateCompleteMsg, result);
        log("handleGetLaaStateDone getLaaDetailedState result is " + result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetCallforwardDone(Message msg) {
        Message cbMsg;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (cbMsg = (Message) ar.getUserObj()) != null && cbMsg.replyTo != null) {
            Bundle data = new Bundle();
            if (ar.getException() != null) {
                data.putBoolean(CALLBACK_RESULT, false);
                data.putString(CALLBACK_EXCEPTION, ar.getException().toString());
            } else {
                data.putBoolean(CALLBACK_RESULT, true);
            }
            CallForwardInfoExt.putDataToBundle(ar.getResult(), data);
            cbMsg.setData(data);
            try {
                cbMsg.replyTo.send(cbMsg);
            } catch (RemoteException e) {
                loge("EVENT_GET_CALLFORWARDING_DONE RemoteException");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetNumRecBaseStattionDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            loge("handleGetNumRecBaseStattionDone AsyncResult ar null");
            return;
        }
        Message cbMsg = (Message) ar.getUserObj();
        if (cbMsg == null || cbMsg.replyTo == null) {
            log("handleGetNumRecBaseStattionDone  cbMsg is null or cbMsg.replyTo is null");
            return;
        }
        Bundle data = new Bundle();
        data.putBoolean(CALLBACK_RESULT, true);
        if (ar.getException() != null) {
            CommandExceptionEx commandExceptionEx = CommandExceptionEx.getCommandException(ar.getException());
            if (CommandExceptionEx.isCommandException(ar.getException())) {
                data.putInt(CALLBACK_AFBS_INFO, commandExceptionEx.getError().ordinal());
                log("handleGetNumRecBaseStattionDone command exception is " + commandExceptionEx.getError().ordinal());
            } else {
                data.putInt(CALLBACK_AFBS_INFO, -1);
                log("handleGetNumRecBaseStattionDone fail");
            }
        } else {
            log("handleGetNumRecBaseStattionDone succ result is 0");
            data.putInt(CALLBACK_AFBS_INFO, 0);
        }
        cbMsg.setData(data);
        try {
            cbMsg.replyTo.send(cbMsg);
        } catch (RemoteException e) {
            loge("EVENT_GET_NUMRECBASESTATION_DONE RemoteException");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetFunctionDone(Message msg) {
        Message cbMsg;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (cbMsg = (Message) ar.getUserObj()) != null && cbMsg.replyTo != null) {
            Bundle data = new Bundle();
            if (ar.getException() != null) {
                data.putBoolean(CALLBACK_RESULT, false);
                data.putString(CALLBACK_EXCEPTION, ar.getException().toString());
            } else {
                data.putBoolean(CALLBACK_RESULT, true);
            }
            cbMsg.setData(data);
            try {
                cbMsg.replyTo.send(cbMsg);
            } catch (RemoteException e) {
                loge("EVENT_SET_FUNCTION_DONE RemoteException");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendSimMatchedOperatorInfoDone(Message response) {
        AsyncResultEx ar = AsyncResultEx.from(response.obj);
        int slotId = response.arg1;
        if (ar == null || !isValidSlotId(slotId)) {
            loge("handleSendSimMatchedOperatorInfoDone failed, invalid response");
        } else if (!CommandExceptionEx.isCommandException(ar.getException())) {
            SystemPropertiesEx.set(SEND_NVCFG_ERRORNO_PROP + slotId, BuildConfig.FLAVOR);
            SystemPropertiesEx.set(CARRIER_CONFIG_VERSION_UPDATE_PROP + slotId, "false");
        } else if (response.arg2 == 3) {
            CommandExceptionEx.Error error = CommandExceptionEx.getCommandException(ar.getException()).getError();
            if (error == CommandExceptionEx.Error.RADIO_NOT_AVAILABLE || error == CommandExceptionEx.Error.GENERIC_FAILURE) {
                loge("handleSendSimMatchedOperatorInfoDone failed, occurs exception:" + error.ordinal());
                SystemPropertiesEx.set(SEND_NVCFG_ERRORNO_PROP + slotId, String.valueOf(error.ordinal()));
                SystemPropertiesEx.set(CARRIER_CONFIG_VERSION_UPDATE_PROP + slotId, "true");
            }
        }
    }

    public boolean isCspPlmnEnabled(int slotId) {
        log("isCspPlmnEnabled for slotId " + slotId);
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            return phone.isCspPlmnEnabled();
        }
        return false;
    }

    public void setCallForwardingOption(int slotId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) {
        log("setCallForwardingOption for slotId " + slotId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_SET_CALLFORWARDING_DONE);
            msg.obj = response;
            phone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, msg);
        } else if (response != null) {
            try {
                if (response.replyTo != null) {
                    Message cbMsg = Message.obtain(response);
                    Bundle data = new Bundle();
                    data.putBoolean(CALLBACK_RESULT, false);
                    cbMsg.setData(data);
                    response.replyTo.send(cbMsg);
                }
            } catch (RemoteException e) {
                loge("setCallForwardingOption RemoteException");
            }
        }
    }

    public void getCallForwardingOption(int slotId, int commandInterfaceCFReason, Message response) {
        log("getCallForwardingOption for slotId " + slotId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_GET_CALLFORWARDING_DONE);
            msg.obj = response;
            phone.getCallForwardingOption(commandInterfaceCFReason, msg);
        } else if (response != null) {
            try {
                if (response.replyTo != null) {
                    Message cbMsg = Message.obtain(response);
                    Bundle data = new Bundle();
                    data.putBoolean(CALLBACK_RESULT, false);
                    cbMsg.setData(data);
                    response.replyTo.send(cbMsg);
                }
            } catch (RemoteException e) {
                loge("getCallForwardingOption RemoteException");
            }
        }
    }

    public boolean setSubscription(int slotId, boolean activate, Message response) {
        log("setSubscription for slotId: " + slotId + ", activate: " + activate);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Message msg = this.mMainHandler.obtainMessage(EVENT_SET_SUBSCRIPTION_DONE);
        msg.obj = response;
        return HwSubscriptionManagerUtils.getInstance().setSubscription(slotId, activate, msg);
    }

    public String getImsImpu(int slotId) {
        log("getImsImpu for slotId " + slotId);
        if (HwCustUtil.isVZW) {
            PackageManager pm = this.mContext.getPackageManager();
            String callingPackage = null;
            if (pm != null) {
                callingPackage = pm.getPackagesForUid(Binder.getCallingUid())[0];
            }
            if (!checkReadPhoneNumber(callingPackage, "getImsImpu")) {
                return null;
            }
        } else {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        }
        PhoneExt phone = getPhoneBySlotId(slotId);
        if (phone != null) {
            return phone.getUtIMPUFromNetwork();
        }
        return null;
    }

    public String getLine1NumberFromImpu(int slotId) {
        log("getLine1NumberFromImpu for slotId " + slotId);
        String impu = getImsImpu(slotId);
        if (TextUtils.isEmpty(impu)) {
            return null;
        }
        String impu2 = impu.trim();
        if (impu2.matches(PATTERN_TEL)) {
            return impu2.substring(impu2.indexOf(":") + 1);
        }
        if (!impu2.matches(PATTERN_SIP)) {
            return null;
        }
        String number = impu2.substring(impu2.indexOf(":") + 1, impu2.indexOf("@"));
        return (TextUtils.isEmpty(number) || getPhoneBySlotId(slotId) == null || number.equals(getPhoneBySlotId(slotId).getSubscriberId())) ? null : number;
    }

    private boolean checkReadPhoneNumber(String callingPackage, String message) {
        if (this.mAppOps.noteOp("android:write_sms", Binder.getCallingUid(), callingPackage) == 0) {
            return true;
        }
        try {
            return checkReadPhoneState(callingPackage, message);
        } catch (SecurityException e) {
            try {
                this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SMS", message);
                if (this.mAppOps.noteOp("android.permission.READ_SMS", Binder.getCallingUid(), callingPackage) == 0) {
                    return true;
                }
                return false;
            } catch (SecurityException e2) {
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_NUMBERS", message);
                    if (this.mAppOps.noteOp("android.permission.READ_PHONE_NUMBERS", Binder.getCallingUid(), callingPackage) == 0) {
                        return true;
                    }
                    return false;
                } catch (SecurityException e3) {
                    throw new SecurityException(message + ": Neither user " + Binder.getCallingUid() + " nor current process has android.permission.READ_PHONE_STATE, android.permission.READ_SMS, or android.permission.READ_PHONE_STATE.");
                }
            }
        }
    }

    private boolean checkReadPhoneState(String callingPackage, String message) {
        try {
            this.mContext.enforceCallingOrSelfPermission(READ_PRIVILEGED_PHONE_STATE, message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp("android:read_phone_state", Binder.getCallingUid(), callingPackage) == 0) {
                return true;
            }
            return false;
        }
    }

    private void sendResponseToTarget(Message response, int result) {
        if (response != null && response.getTarget() != null) {
            response.arg1 = result;
            response.sendToTarget();
        }
    }

    public void registerForCallAltSrv(int slotId, IPhoneCallback callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
        if (phone == null || callback == null) {
            log("registerForCallAltSrv:phone or callback is null,return");
            return;
        }
        log("registerForCallAltSrv for slotId = " + slotId);
        phone.getCi().registerForCallAltSrv(phone.getHandler(), (int) EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE, callback);
    }

    public void unregisterForCallAltSrv(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
        if (phone == null) {
            log("unregisterForCallAltSrv:phone or callback is null,return");
            return;
        }
        log("unregisterForCallAltSrv for slotId = " + slotId);
        phone.getCi().unregisterForCallAltSrv(phone.getHandler());
    }

    public int invokeOemRilRequestRaw(int phoneId, byte[] oemReq, byte[] oemResp) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!checkPhoneIsValid(phoneId)) {
            logForOemHook("phone is invalid!");
            return -1;
        } else if (oemReq == null || oemResp == null) {
            logForOemHook("oemReq or oemResp is null!");
            return -1;
        } else {
            try {
                AsyncResultEx result = (AsyncResultEx) sendRequest(CMD_INVOKE_OEM_RIL_REQUEST_RAW, oemReq, Integer.valueOf(phoneId));
                if (result.getException() != null) {
                    int returnValue = CommandExceptionEx.getCommandException(result.getException()).getError().ordinal();
                    logForOemHook("invokeOemRilRequestRaw fail exception + returnValue:" + returnValue);
                    if (returnValue > 0) {
                        return returnValue * -1;
                    }
                    return returnValue;
                } else if (result.getResult() != null) {
                    byte[] responseData = (byte[]) result.getResult();
                    if (responseData.length > oemResp.length) {
                        loge("Buffer to copy response too small: Response length is " + responseData.length + "bytes. Buffer Size is " + oemResp.length + "bytes.");
                    }
                    System.arraycopy(responseData, 0, oemResp, 0, responseData.length);
                    int returnValue2 = responseData.length;
                    logForOemHook("invokeOemRilRequestRaw success, returnValue" + returnValue2);
                    return returnValue2;
                } else {
                    logForOemHook("invokeOemRilRequestRaw fail result.result is null");
                    return 0;
                }
            } catch (RuntimeException e) {
                logForOemHook("invokeOemRilRequestRaw: Runtime Exception");
                int returnValue3 = CommandExceptionEx.Error.GENERIC_FAILURE.ordinal();
                if (returnValue3 > 0) {
                    return returnValue3 * -1;
                }
                return returnValue3;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r2v2 com.huawei.internal.telephony.PhoneExt: [D('phoneId' int), D('phone' com.huawei.internal.telephony.PhoneExt)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCmdOemRilRequestRaw(Message msg) {
        int phoneId;
        PhoneExt phone;
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_INVOKE_OEM_RIL_REQUEST_RAW_DONE, request);
        if (request.subId == null || (phone = this.mPhones[(phoneId = request.subId.intValue())]) == null) {
            PhoneExt phone2 = this.mPhone;
            if (phone2 != null) {
                phone2.invokeOemRilRequestRaw((byte[]) request.argument, onCompleted);
                logForOemHook("invokeOemRilRequestRaw with default phone");
                return;
            }
            return;
        }
        phone.invokeOemRilRequestRaw((byte[]) request.argument, onCompleted);
        logForOemHook("invokeOemRilRequestRaw success by phoneId=" + phoneId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCmdOemRilRequestRawDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
        request.result = ar;
        synchronized (request) {
            request.notifyAll();
        }
    }

    public boolean isSecondaryCardGsmOnly() {
        enforceReadPermission();
        if (TelephonyManagerEx.isMultiSimEnabled() && HwFullNetworkManager.getInstance().isCMCCDsdxDisable() && HwFullNetworkManager.getInstance().isCMCCHybird()) {
            return true;
        }
        return false;
    }

    public boolean bindSimToProfile(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        int numPhones = TelephonyManagerEx.getDefault().getPhoneCount();
        if (slotId >= 0 && slotId <= numPhones) {
            return Settings.Global.putInt(this.mContext.getContentResolver(), "afw_work_slotid", slotId);
        }
        log("bind sim fail");
        return false;
    }

    public boolean setLine1Number(int slotId, String alphaTag, String number, Message onComplete) {
        enforceModifyPermissionOrCarrierPrivilege();
        PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
        if (phone == null) {
            return false;
        }
        Message msg = this.mMainHandler.obtainMessage(EVENT_SET_LINENUM_DONE);
        msg.obj = onComplete;
        return phone.setLine1Number(alphaTag, number, msg);
    }

    public boolean setDeepNoDisturbState(int slotId, int state) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        return ((Boolean) sendRequest(CMD_SET_DEEP_NO_DISTURB, Integer.valueOf(state), Integer.valueOf(slotId))).booleanValue();
    }

    public boolean setUplinkFreqBandwidthReportState(int slotId, int state, IPhoneCallback callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (1 != state) {
            unregisterForUplinkfreqStateRpt();
        } else if (!registerForUplinkfreqStateRpt(slotId, callback)) {
            loge("setUplinkFreqBandwidthReportState: registerForUlfreqStateRpt ERROR ");
            return false;
        }
        boolean response = ((Boolean) sendRequest(CMD_SET_UL_FREQ_BANDWIDTH_RPT, Integer.valueOf(state), Integer.valueOf(slotId))).booleanValue();
        log("setUplinkFreqBandwidthReportState: slotId = " + slotId + " state = " + state + ", set result = " + response);
        return response;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCmdSetUplinkFreqBandwidthReportState(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        if (request != null) {
            if (request.subId == null || request.argument == null) {
                log("handleCmdSetUplinkFreqBandwidthReportState failed, request.subId =  " + request.subId + ", request.argument = " + request.argument);
                request.result = false;
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
            log("handleCmdSetUplinkFreqBandwidthReportState subId = " + request.subId + " arg = " + request.argument);
            CommandsInterfaceEx ci = getCommandsInterface(request.subId.intValue());
            if (ci == null) {
                loge("handleCmdSetUplinkFreqBandwidthReportState failed: ci is null");
                request.result = false;
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
            ci.setUplinkfreqEnable(((Integer) request.argument).intValue(), this.mMainHandler.obtainMessage(EVENT_SET_UL_FREQ_BANDWIDTH_RPT_DONE, request));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetUplinkFreqBandwidthDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            loge("handleSetUplinkFreqBandwidthDone ar == null ");
            return;
        }
        MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
        if (request == null) {
            loge("handleSetUplinkFreqBandwidthDone request == null ");
            return;
        }
        if (ar.getException() == null) {
            request.result = true;
        } else {
            request.result = false;
            loge("handleSetUplinkFreqBandwidthDone notifyAll, request.result = " + request.result);
        }
        synchronized (request) {
            request.notifyAll();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUplinkFreqBandwidthRpt(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (this.mUlFreqReportCB == null) {
            loge("handleUplinkFreqBandwidthRpt mUlFreqReportCB is null");
        } else if (ar == null || ar.getException() != null || ar.getResult() == null) {
            loge("handleUplinkFreqBandwidthRpt exception");
        } else {
            try {
                this.mUlFreqReportCB.onCallback3(0, 0, (Bundle) ar.getResult());
            } catch (RemoteException e) {
                loge("handleUplinkFreqBandwidthRpt RemoteException");
            }
            log("handleUplinkFreqBandwidthRpt: receive UlFreqBandwidth infor");
        }
    }

    private boolean registerForUplinkfreqStateRpt(int subId, IPhoneCallback callback) {
        log("receive registerForUlfreqStateRpt Enter: subId = " + subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (-1 != this.mUlFreqRptSubId || subId == -1 || callback == null) {
            return false;
        }
        this.mUlFreqRptSubId = subId;
        CommandsInterfaceEx ci = getCommandsInterface(this.mUlFreqRptSubId);
        if (ci == null) {
            loge("handleCmdSetUplinkFreqBandwidthReportState ci null");
            return false;
        }
        log("registerForUlfreqStateRpt: register EVENT_UL_FREQ_BANDWIDTH_RPT");
        ci.registerForUplinkfreqStateRpt(this.mMainHandler, (int) EVENT_UL_FREQ_BANDWIDTH_RPT, (Object) null);
        this.mUlFreqReportCB = callback;
        return true;
    }

    private void unregisterForUplinkfreqStateRpt() {
        log("receive unregisterForUlfreqStateRpt");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterfaceEx ci = getCommandsInterface(this.mUlFreqRptSubId);
        this.mUlFreqRptSubId = -1;
        if (ci == null) {
            loge("handleCmdSetUplinkFreqBandwidthReportState ci null");
            return;
        }
        ci.unregisterForUplinkfreqStateRpt(this.mMainHandler);
        this.mUlFreqReportCB = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCmdSetDeepNoDisturb(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        if (request != null) {
            if (request.subId == null || request.argument == null) {
                log("handleCmdSetDeepNoDisturb request null");
                request.result = false;
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
            log("handleCmdSetDeepNoDisturb subId=" + request.subId + " arg=" + request.argument);
            CommandsInterfaceEx ci = getCommandsInterface(request.subId.intValue());
            if (ci == null) {
                log("handleCmdSetDeepNoDisturb ci null");
                request.result = false;
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
            ci.setDeepNoDisturbState(((Integer) request.argument).intValue(), this.mMainHandler.obtainMessage(EVENT_SET_DEEP_NO_DISTURB_DONE, request));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetDeepNoDisturbDone(Message msg) {
        MainThreadRequest request;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (request = (MainThreadRequest) ar.getUserObj()) != null) {
            if (ar.getException() == null) {
                request.result = true;
            } else {
                request.result = false;
            }
            log("handleSetDeepNoDisturbDone notifyAll");
            synchronized (request) {
                request.notifyAll();
            }
        }
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName) {
        Message response = this.mMainHandler.obtainMessage(120);
        try {
            if (getCommandsInterface() != null) {
                getCommandsInterface().informModemTetherStatusToChangeGRO(enable, faceName, response);
            }
        } catch (RuntimeException e) {
            loge("informModemTetherStatusToChangeGRO got RuntimeException");
        }
    }

    public boolean sendSimMatchedOperatorInfo(int slotId, String opKey, String opName, int state, String reserveField) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(slotId)) {
            loge("sendSimMatchedOperatorInfo: slotId = " + slotId + " is invalid.");
            return false;
        }
        CommandsInterfaceEx ci = getCommandsInterface(slotId);
        if (ci == null) {
            loge("sendSimMatchedOperatorInfo: ci is null, slotId = " + slotId);
            return false;
        }
        ci.sendSimMatchedOperatorInfo(opKey, opName, state, reserveField, this.mMainHandler.obtainMessage(EVENT_SEND_SIM_MATCHED_OPERATOR_INFO_DONE, slotId, state));
        return true;
    }

    public boolean[] getMobilePhysicsLayerStatus(int slotId) {
        enforceReadPermission();
        HwServiceStateTrackerEx serviceStateTrackerExInner = HwServiceStateTrackerEx.getInstance(slotId);
        if (serviceStateTrackerExInner != null) {
            return serviceStateTrackerExInner.getMobilePhysicsLayerStatus();
        }
        return null;
    }

    public boolean getAntiFakeBaseStation(Message response) {
        log("getAntiFakeBaseStation ");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (getCommandsInterface() != null) {
            Message msg = this.mMainHandler.obtainMessage(121);
            msg.obj = response;
            return getCommandsInterface().getAntiFakeBaseStation(msg);
        }
        loge("getAntiFakeBaseStation phone null");
        return false;
    }

    public boolean registerForAntiFakeBaseStation(IPhoneCallback callback) {
        log("registerForAntiFakeBaseStation");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int numPhones = TelephonyManagerEx.getDefault().getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            if (this.mPhones[i] != null) {
                log("registerForAntiFakeBaseStation PhoneExt sub = " + i);
                this.mPhones[i].getCi().registerForAntiFakeBaseStation(this.mMainHandler, (int) EVENT_ANTIFAKE_BASESTATION_CHANGED, (Object) null);
            }
        }
        this.mAntiFakeBaseStationCB = callback;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAntiFakeBaseStation(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (this.mAntiFakeBaseStationCB == null) {
            loge("handleAntiFakeBaseStation mAntiFakeBaseStationCB is null");
        } else if (ar == null || ar.getException() != null || ar.getResult() == null) {
            loge("handleAntiFakeBaseStation exception: ar " + ar);
        } else {
            int parm = ((Integer) ar.getResult()).intValue();
            Intent intent = new Intent("com.huawei.action.ACTION_HW_ANTIFAKE_BASESTATION");
            intent.setPackage("com.huawei.systemmanager");
            intent.putExtra(CALLBACK_AFBS_INFO, parm);
            this.mContext.sendBroadcast(intent);
            try {
                this.mAntiFakeBaseStationCB.onCallback1(parm);
            } catch (RemoteException e) {
                loge("handleAntiFakeBaseStation RemoteException");
            }
            log("handleAntiFakeBaseStation send");
        }
    }

    public boolean unregisterForAntiFakeBaseStation() {
        log("unregisterForAntiFakeBaseStation");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int numPhones = TelephonyManagerEx.getDefault().getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            if (this.mPhones[i] != null) {
                log("unregisterForAntiFakeBaseStation PhoneExt sub = " + i);
                this.mPhones[i].getCi().unregisterForAntiFakeBaseStation(this.mMainHandler);
            }
        }
        this.mAntiFakeBaseStationCB = null;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetCardTrayInfoDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getResult() == null || ar.getException() != null) {
            this.mCardTrayInfo = null;
            loge("handleGetCardTrayInfoDone, exception occurs");
        } else if (ar.getResult() instanceof byte[]) {
            this.mCardTrayInfo = (byte[]) ar.getResult();
            log("handleGetCardTrayInfoDone, mCardTrayInfo:" + IccUtilsEx.bytesToHexString(this.mCardTrayInfo));
        }
        synchronized (this.mCardTrayLock) {
            this.mCardTrayLock.notifyAll();
        }
    }

    public byte[] getCardTrayInfo() {
        logd("getCardTrayInfo");
        enforceReadPermission();
        synchronized (this.mCardTrayLock) {
            if (getCommandsInterface() == null) {
                return new byte[0];
            }
            Message response = this.mMainHandler.obtainMessage(EVENT_GET_CARD_TRAY_INFO_DONE);
            logd("getCardTrayInfo, getCommandsInterface()" + getCommandsInterface());
            getCommandsInterface().getCardTrayInfo(response);
            log("getCardTrayInfo, end to call" + response);
            for (boolean isWait = true; isWait; isWait = false) {
                try {
                    this.mCardTrayLock.wait();
                } catch (InterruptedException e) {
                    loge("Interrupted Exception in getCardTrayInfo");
                }
            }
        }
        byte[] bArr = this.mCardTrayInfo;
        if (bArr == null) {
            return new byte[0];
        }
        return (byte[]) bArr.clone();
    }

    public boolean setCsconEnabled(boolean isEnabled) {
        log("setCsconEnabled, isEnabled = " + isEnabled);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int i = 0;
        while (true) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (i >= phoneExtArr.length) {
                return true;
            }
            phoneExtArr[i].getCi().setCsconEnabled(isEnabled ? 1 : 0, (Message) null);
            i++;
        }
    }

    public int[] getCsconEnabled() {
        int[] result = {-1, -1};
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        for (int i = 0; i < this.mPhones.length; i++) {
            result[i] = ((Integer) sendRequest(CMD_GET_CSCON_ID, null, Integer.valueOf(i))).intValue();
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetCscon(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_CMD_GET_CSCON_ID_DONE, request);
        if (request.subId != null) {
            int phoneId = request.subId.intValue();
            if (isValidSlotId(phoneId)) {
                this.mPhones[phoneId].getCi().getCsconEnabled(onCompleted);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetCsconDone(Message msg) {
        MainThreadRequest request;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && (request = (MainThreadRequest) ar.getUserObj()) != null) {
            if (ar.getException() != null || ar.getResult() == null) {
                loge("EVENT_CMD_GET_CSCON_ID: error");
            } else {
                request.result = ar.getResult();
            }
            if (request.result == null) {
                request.result = -1;
            }
            synchronized (request) {
                request.notifyAll();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleQueryCardTypeDone(Message msg, Integer index) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null || ar.getResult() == null || !isValidSlotId(index.intValue())) {
            loge("Received EVENT_QUERY_CARD_TYPE_DONE got exception");
        } else if (ar.getResult() instanceof int[]) {
            this.mCardTypes[index.intValue()] = ((int[]) ar.getResult())[0];
            saveCardTypeProperties(((int[]) ar.getResult())[0], index.intValue());
        }
    }

    private String getIccATRFromProp(int index) {
        if (index == 0) {
            return SystemPropertiesEx.get("gsm.sim.hw_atr");
        }
        return SystemPropertiesEx.get("gsm.sim.hw_atr1");
    }

    public String blockingGetIccATR(int index) {
        if (!isValidSlotId(index) || !IS_ESIM_HW_EXTEND_APDU) {
            return null;
        }
        String strAtr = getIccATRFromProp(index);
        if (strAtr == null || NULL_ICC_ATR.equals(strAtr)) {
            logi("no data, start to query, index = " + index);
            String strAtr2 = (String) sendRequest(CMD_ICC_GET_ATR, null, Integer.valueOf(index));
            if (NULL_ICC_ATR.equals(strAtr2)) {
                return null;
            }
            return strAtr2;
        }
        logi("has received, return direct, strAtr = " + strAtr);
        return strAtr;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetIccAtr(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_ICC_GET_ATR_DONE, request);
        logi("handleGetIccAtr, request.subId = " + request.subId);
        if (request.subId != null) {
            int phoneId = request.subId.intValue();
            if (isValidSlotId(phoneId)) {
                this.mPhones[phoneId].getCi().iccGetATR(onCompleted);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetIccAtrDone(Message msg, Integer index) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            loge("handleGetIccAtrDone, ar is null, return");
            return;
        }
        if (ar.getException() != null) {
            loge("handleGetIccAtrDone got exception");
        } else {
            handleIccATR((String) ar.getResult(), index);
        }
        if (ar.getUserObj() instanceof MainThreadRequest) {
            MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
            if (request != null) {
                request.result = getIccATRFromProp(index.intValue());
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
            return;
        }
        logi("handleGetIccAtrDone, no request, don't need to notify");
    }

    private void handleIccATR(String strATR, Integer index) {
        logi("handleIccATR, ATR: [" + strATR + "], index:[" + index + "]");
        if (strATR == null || strATR.isEmpty()) {
            strATR = NULL_ICC_ATR;
        }
        if (strATR.length() > 66) {
            loge("strATR.length() greater than 66");
            strATR = strATR.substring(0, 66);
        }
        if (index.intValue() == 0) {
            SystemPropertiesEx.set("gsm.sim.hw_atr", strATR);
        } else {
            SystemPropertiesEx.set("gsm.sim.hw_atr1", strATR);
        }
    }

    public int getLevel(int type, int rssi, int ecio, int phoneId) {
        HwSignalStrength.SignalType signalType = HwSignalStrength.SignalType.UNKNOWN;
        if (type == 1) {
            signalType = HwSignalStrength.SignalType.GSM;
        } else if (type == 2) {
            signalType = HwSignalStrength.SignalType.UMTS;
        } else if (type == 3) {
            signalType = HwSignalStrength.SignalType.CDMA;
        } else if (type == 4) {
            signalType = HwSignalStrength.SignalType.EVDO;
        } else if (type == 5) {
            signalType = HwSignalStrength.SignalType.LTE;
        } else if (type == 6) {
            signalType = HwSignalStrength.SignalType.CDMALTE;
        } else if (type == 7) {
            signalType = HwSignalStrength.SignalType.NR;
        } else {
            loge("Invalid type");
        }
        HwSignalStrength hwSigStr = HwSignalStrength.getInstance(phoneId, this.mContext);
        if (hwSigStr != null) {
            return hwSigStr.getLevel(signalType, rssi, ecio);
        }
        return 0;
    }

    private void initCommBoosterManager() {
        this.bm = HwTelephonyBoosterUtils.getHwCommBoosterServiceManager();
        registerBoosterCallback();
    }

    private void registerBoosterCallback() {
        log("registerBoosterCallback enter");
        IHwCommBoosterServiceManager iHwCommBoosterServiceManager = this.bm;
        if (iHwCommBoosterServiceManager != null) {
            int ret = iHwCommBoosterServiceManager.registerCallBack("com.android.internal.telephony", this.mIHwCommBoosterCallback);
            if (ret != 0) {
                log("registerBoosterCallback:registerCallBack failed, ret=" + ret);
                return;
            }
            return;
        }
        log("registerBoosterCallback:null HwCommBoosterServiceManager");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showNotification(String contentTitle, String contentText, int priority) {
        Context context = this.mContext;
        if (context != null) {
            this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(NETWORK_LIMIT_NOTIFY_CHANNEL, this.mContext.getString(HwPartResourceUtils.getResourceId("sim_card_manager")), 4));
            Notification.Builder builder = new Notification.Builder(this.mContext).setSmallIcon(HwPartResourceUtils.getResourceId("ic_sim_statusbar")).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1).setContentTitle(contentTitle).setContentText(contentText).setStyle(new Notification.BigTextStyle()).addAction(getKnownAction()).setChannelId(NETWORK_LIMIT_NOTIFY_CHANNEL).setPriority(priority);
            NotificationEx.Builder.setAppName(builder, this.mContext.getString(HwPartResourceUtils.getResourceId("sim_card_manager")));
            this.mNotificationManager.notify(LOG_TAG, 1, builder.build());
        }
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x007a: APUT  
      (r10v0 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x0076: INVOKE  (r11v2 java.lang.Integer) = (r11v1 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00a0: APUT  
      (r10v1 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x009c: INVOKE  (r4v8 java.lang.Integer) = (r4v7 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* access modifiers changed from: public */
    private void showDataFlowNotification(int count) {
        if (this.mContext != null) {
            Intent resultIntent = getResultIntent();
            resultIntent.setFlags(335544320);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this.mContext, 0, resultIntent, 134217728);
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(DATA_FLOW_NOTIFY_CHANNEL, this.mContext.getString(HwPartResourceUtils.getResourceId("sim_card_manager")), 4));
            int i = 1;
            Notification.Builder defaults = new Notification.Builder(this.mContext).setSmallIcon(HwPartResourceUtils.getResourceId("ic_sim_statusbar")).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1);
            Context context = this.mContext;
            int resourceId = HwPartResourceUtils.getResourceId("data_remind");
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(getDefault4GSlotId() == 1 ? 1 : 2);
            objArr[1] = Integer.valueOf(count);
            Notification.Builder contentTitle = defaults.setContentTitle(context.getString(resourceId, objArr));
            Context context2 = this.mContext;
            int resourceId2 = HwPartResourceUtils.getResourceId("data_remind_summary");
            Object[] objArr2 = new Object[1];
            if (getDefault4GSlotId() != 1) {
                i = 2;
            }
            objArr2[0] = Integer.valueOf(i);
            Notification.Builder builder = contentTitle.setContentText(context2.getString(resourceId2, objArr2)).setContentIntent(resultPendingIntent).setStyle(new Notification.BigTextStyle()).addAction(getSwitchBackAction()).addAction(getIgnoreAction()).addAction(getNeverAction()).setChannelId(DATA_FLOW_NOTIFY_CHANNEL);
            NotificationEx.Builder.setAppName(builder, this.mContext.getString(HwPartResourceUtils.getResourceId("sim_card_manager")));
            this.mNotificationManager.notify(LOG_TAG, 2, builder.build());
        }
    }

    private Notification.Action getNeverAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_NEVER_NOTIFY);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder((Icon) null, this.mContext.getString(HwPartResourceUtils.getResourceId("notification_typeC_nonotify")), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    private Notification.Action getIgnoreAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_IGNORE_NOTIFY);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder((Icon) null, this.mContext.getString(HwPartResourceUtils.getResourceId("magngesture_dialog_ignore")), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    private Notification.Action getSwitchBackAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_SWITCH_BACK);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder((Icon) null, this.mContext.getString(HwPartResourceUtils.getResourceId("back_to_main"), Integer.valueOf(getDefault4GSlotId() + 1)), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    private Notification.Action getKnownAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_CANCEL_NOTIFY);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder((Icon) null, this.mContext.getString(HwPartResourceUtils.getResourceId("alert_dialog_ok")), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissNotification(int notifyId) {
        if (this.mNotificationManager != null) {
            log("dismissLimitNotification");
            this.mNotificationManager.cancel(LOG_TAG, notifyId);
        }
    }

    private void registerIntelligenceCardReceiver() {
        log("register smart card broadcast");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CANCEL_NOTIFY);
        filter.addAction(ACTION_SWITCH_BACK);
        filter.addAction(ACTION_IGNORE_NOTIFY);
        filter.addAction(ACTION_NEVER_NOTIFY);
        Context context = this.mContext;
        if (context != null) {
            context.registerReceiver(this.mIntelligenceCardReceiver, filter);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendActionToNetLinkManager() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_UNDO_DUAL_LINK");
        intent.setPackage("android");
        this.mContext.sendBroadcast(intent);
    }

    public void reportToBoosterForSwitchBack() {
        enforceReadPermission();
        sendReportToBooster(701);
    }

    public void reportToBoosterForNoRetryPdp() {
        enforceReadPermission();
        sendReportToBooster(702);
    }

    private void sendReportToBooster(int reportType) {
        log("sendReportToBooster, reportType = " + reportType);
        if (this.bm == null) {
            log("bm is null");
            return;
        }
        Bundle data = new Bundle();
        data.putInt("sub", getDefault4GSlotId());
        int ret = this.bm.reportBoosterPara("com.android.internal.telephony", reportType, data);
        if (ret != 0) {
            log("reportBoosterPara failed, ret=" + ret);
        }
    }

    private boolean isValidPhone(int phoneId) {
        PhoneExt[] phoneExtArr;
        return isValidSlotId(phoneId) && (phoneExtArr = this.mPhones) != null && phoneId < phoneExtArr.length && phoneExtArr[phoneId] != null;
    }

    public int getDataRegisteredState(int phoneId) {
        enforceReadPermission();
        if (!isValidPhone(phoneId)) {
            loge("getDataRegisteredState: Phone is Invalid for phoneId = " + phoneId);
            return 1;
        }
        ServiceStateTrackerEx sst = this.mPhones[phoneId].getServiceStateTracker();
        if (sst == null || sst.getSS() == null) {
            return 1;
        }
        return ServiceStateEx.getDataState(sst.getSS());
    }

    private String getIccidBySlot(int slotId) {
        UiccControllerExt uiccController = UiccControllerExt.getInstance();
        if (uiccController.getUiccCard(slotId) != null) {
            return uiccController.getUiccCard(slotId).getIccId();
        }
        return null;
    }

    public boolean isCustomAis() {
        return HwFullNetworkConfig.IS_AIS_4G_DSDX_ENABLE;
    }

    public boolean isAISCard(int slotId) {
        String inn = BuildConfig.FLAVOR;
        String iccId = getIccidBySlot(slotId);
        if (!TextUtils.isEmpty(iccId) && iccId.length() >= 7) {
            inn = iccId.substring(0, 7);
        }
        String mccMnc = TelephonyManagerEx.getSimOperatorNumericForPhone(slotId);
        if (!TextUtils.isEmpty(inn) && HwIccIdUtil.isAIS(inn)) {
            return true;
        }
        if (TextUtils.isEmpty(mccMnc) || !HwIccIdUtil.isAISByMccMnc(mccMnc)) {
            return false;
        }
        return true;
    }

    public int getVoiceRegisteredState(int phoneId) {
        enforceReadPermission();
        if (!isValidPhone(phoneId)) {
            loge("getVoiceRegisteredState: Phone is Invalid for phoneId = " + phoneId);
            return 1;
        }
        ServiceStateTrackerEx sst = this.mPhones[phoneId].getServiceStateTracker();
        if (sst == null || sst.getSS() == null) {
            return 1;
        }
        return ServiceStateEx.getVoiceRegState(sst.getSS());
    }

    private boolean checkPhoneIsValid(int phoneId) {
        if (phoneId >= 0) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (phoneId < phoneExtArr.length) {
                if (phoneExtArr[phoneId] != null) {
                    return true;
                }
                loge("phone is null!");
                return false;
            }
        }
        loge("phoneId is invalid!");
        return false;
    }

    private int getPhoneIdForMainSlot() {
        if (!TelephonyManagerEx.isMultiSimEnabled() || getDefault4GSlotId() != 1) {
            return 0;
        }
        return 1;
    }

    private void setPhoneForMainSlot() {
        int phoneId = getPhoneIdForMainSlot();
        if (checkPhoneIsValid(phoneId)) {
            this.mPhone = this.mPhones[phoneId];
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIntelligentRecommendation() {
        boolean hasSwitchOn = SystemPropertiesEx.getBoolean("persist.radio.smart.card", false);
        boolean showEnable = SystemPropertiesEx.getBoolean("persist.sys.smart_switch_enable", false);
        if (hasSwitchOn) {
            log("The switch was used. not allowed recommend");
        } else if (!showEnable) {
            log("Function not supported");
        } else {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long j = this.mLastNotifyTime;
            if (elapsedRealtime - j > MIN_INTERVAL_TIME || j == 0) {
                log("Once a day");
                this.mLastNotifyTime = SystemClock.elapsedRealtime();
                DecisionUtil.bindService(this.mContext, REC_DECISION_NAME);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkSpeedLimit(Bundle b) {
        log("network speed limit prompt");
        if (!CHINA_RELEASE_VERSION || IS_SPEED_LIMIT_NOTIFIER_ENABLE) {
            return;
        }
        if (IS_PLATFORM_SUPPORT_VSIM && HwVSimUtils.isVSimEnabled()) {
            log("vsim is working, so return");
        } else if (b == null) {
            log("input bundle null.");
        } else if (b.getInt("modemId", -1) != 0) {
            log("At present, the main card is only concerned.");
        } else if (b.getInt("downLink") > DOWNLINK_LIMIT) {
            log("dismiss limit notify, limit is recover");
            dismissNotification(1);
        } else if (this.mHasNotifyLimit) {
            log("has notify limit");
        } else {
            int mainSlotId = getDefault4GSlotId();
            if (SubscriptionManagerEx.isValidSlotIndex(mainSlotId)) {
                String number = removePhoneNumberPrefix(getPhoneNumber(mainSlotId));
                String title = this.mContext.getString(HwPartResourceUtils.getResourceId("network_limit_prompt"));
                String content = SIM_NUM > 1 ? this.mContext.getString(HwPartResourceUtils.getResourceId("network_limit_prompt_content"), Integer.valueOf(mainSlotId + 1), number) : this.mContext.getString(HwPartResourceUtils.getResourceId("network_limit_prompt_summary"));
                this.mHasNotifyLimit = true;
                log("show limit notify");
                showNotification(title, content, 2);
            }
        }
    }

    private String removePhoneNumberPrefix(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            log("invalid phone number");
            return BuildConfig.FLAVOR;
        }
        int numberPrefixLen = CallerInfoHW.getInstance().getIntlPrefixAndCCLen(phoneNumber);
        StringBuffer numberBuffer = new StringBuffer();
        numberBuffer.append("(");
        numberBuffer.append(phoneNumber.substring(numberPrefixLen));
        numberBuffer.append(")");
        return numberBuffer.toString();
    }

    public int getNetworkModeFromDB(int slotId) {
        enforceReadPermission();
        if (isValidSlotId(slotId)) {
            return HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, slotId);
        }
        log("getNetworkModeFromDB the slotId is invalid");
        return -1;
    }

    public void saveNetworkModeToDB(int slotId, int mode) {
        enforceModifyPermissionOrCarrierPrivilege();
        if (!isValidSlotId(slotId)) {
            log("saveNetworkModeToDB the slotId is invalid");
        } else {
            HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, slotId, mode);
        }
    }

    private Intent getResultIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.netassistant.ui.NetAssistantMainActivity"));
        return intent;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetTempCtrlDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int result = -1;
        if (ar != null && ar.getException() == null) {
            result = 0;
        }
        sendResponseToTarget(this.mSetTempCtrlCompleteMsg, result != -1);
        log("handleSetTempCtrlDone setTemperatureControl result is " + result);
    }

    private void sendResponseToTarget(Message response, boolean result) {
        if (response != null && response.replyTo != null) {
            Message cbMsg = Message.obtain(response);
            Bundle data = new Bundle();
            data.putBoolean(CALLBACK_RESULT, result);
            cbMsg.setData(data);
            try {
                response.replyTo.send(cbMsg);
            } catch (RemoteException e) {
                loge("SendResponseToTarget RemoteException");
            }
        }
    }

    public boolean isEuicc(int slotId) {
        UiccCardExt uiccCard = UiccControllerExt.getInstance().getUiccCard(slotId);
        if (uiccCard != null) {
            return uiccCard.isEuiccCard();
        }
        log("uiccSlot is invalid");
        return false;
    }

    private String getPhoneNumber(int slotId) {
        int subId = -1;
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds != null && subIds.length > 0) {
            subId = subIds[0];
        }
        if (subId == -1) {
            return BuildConfig.FLAVOR;
        }
        String phoneNumber = TelephonyManagerEx.getLine1Number(subId);
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumber = getLine1NumberFromImpu(slotId);
            if (TextUtils.isEmpty(phoneNumber)) {
                return BuildConfig.FLAVOR;
            }
        }
        return phoneNumber;
    }

    public boolean setNrOptionMode(int mode, Message msg) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterfaceEx ci = getCommandsInterface(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
        this.mSetNrOptionModeMsg = msg;
        Message setNrModeMsg = this.mMainHandler.obtainMessage(EVENT_SET_NR_OPTION_MODE_DONE);
        if (ci != null) {
            ci.setNrOptionMode(mode, setNrModeMsg);
            return true;
        }
        loge("setNrOptionMode ci is null");
        sendResponseToTarget(msg, false);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetNrOptionMode(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int result = -1;
        if (ar != null && ar.getException() == null) {
            result = 0;
        }
        sendResponseToTarget(this.mSetNrOptionModeMsg, result != -1);
        log("handleSetNrOptionMode result: " + result);
    }

    public int getNrOptionMode() {
        return ((Integer) sendRequest(CMD_GET_NR_OPTION_MODE, null)).intValue();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetNrOptionMode(Message msg) {
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_GET_NR_OPTION_MODE_DONE, (MainThreadRequest) msg.obj);
        CommandsInterfaceEx ci = getCommandsInterface(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
        if (ci != null) {
            ci.getNrOptionMode(onCompleted);
            return;
        }
        loge("getNrOptionMode ci is null");
        AsyncResultEx.forMessage(onCompleted, (Object) null, (Throwable) null);
        this.mMainHandler.sendMessage(onCompleted);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetNrOptionModeDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
        if (ar.getException() != null || ar.getResult() == null) {
            request.result = 0;
        } else {
            request.result = ar.getResult();
        }
        synchronized (request) {
            request.notifyAll();
        }
    }

    public boolean isNsaState(int phoneId) {
        PhoneExt nsaPhone = PhoneFactoryExt.getPhone(phoneId);
        ServiceStateTrackerEx sst = nsaPhone != null ? nsaPhone.getServiceStateTracker() : null;
        if (sst == null) {
            loge("sst is null.");
            return false;
        }
        int nsaState = sst.getNewNsaState();
        if (nsaState < 2 || nsaState > 5) {
            return false;
        }
        return true;
    }

    private void registerRadioAvailableForNrOptionMode() {
        if (HwTelephonyManager.getDefault().isNrSupported() && !HuaweiTelephonyConfigs.isQcomPlatform()) {
            for (int i = 0; i < SIM_NUM; i++) {
                Integer index = Integer.valueOf(i);
                if (isValidPhone(i)) {
                    this.mPhones[i].getCi().registerForAvailable(this.mMainHandler, (int) EVENT_RADIO_AVAILABLE_FOR_NR_OPTION_MODE, index);
                    log("registerRadioAvailableForNrOptionMode success, slotId " + i);
                } else {
                    loge("registerRadioAvailableForNrOptionMode failed, slotId " + i);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRadioAvailableForNrOptionMode(Message msg, int slotId) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            loge("handleRadioAvailableForNrOptionMode: got exception for slotId " + slotId);
            return;
        }
        getNrOptionModeFromModem(slotId);
    }

    private void getNrOptionModeFromModem(int slotId) {
        CommandsInterfaceEx ci = getCommandsInterface(slotId);
        if (ci != null) {
            ci.getNrOptionMode(this.mMainHandler.obtainMessage(EVENT_RADIO_AVAILABLE_GET_NR_OPTION_MODE_DONE, Integer.valueOf(slotId)));
            return;
        }
        loge("getNrOptionModeWhenRadioAvailable: ci is null for slotId " + slotId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetNrOptionModeWhenRadioAvailableDone(Message msg, int slotId) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null || !(ar.getResult() instanceof Integer)) {
            loge("handleGetNrOptionModeWhenRadioAvailableDone: got exception for slotId " + slotId);
            return;
        }
        int mode = ((Integer) ar.getResult()).intValue();
        log("handleGetNrOptionModeWhenRadioAvailableDone: mode is " + mode + " for slotId " + slotId);
        if (mode != 0) {
            int oldMode = getNrOptionModeFromDb();
            log("handleGetNrOptionModeWhenRadioAvailableDone: success, old mode is " + oldMode + ", new mode is " + mode);
            if (mode != oldMode) {
                putNrOptionModeToDb(mode);
            }
        }
    }

    private int getNrOptionModeFromDb() {
        return Settings.System.getInt(this.mContext.getContentResolver(), NR_OPTION_MODE, 0);
    }

    private void putNrOptionModeToDb(int mode) {
        log("putNrOptionModeToDb: mode = " + mode);
        Settings.System.putInt(this.mContext.getContentResolver(), NR_OPTION_MODE, mode);
    }

    public NrCellSsbId getNrCellSsbId(int slotId) {
        NrCellSsbId nrCellSssbId = (NrCellSsbId) sendRequest(CMD_GET_NR_CELL_SSBID, null, Integer.valueOf(slotId));
        log("getNrCellSsbId return." + nrCellSssbId);
        return nrCellSssbId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetNrCellSsbid(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_GET_NR_CELL_SSBID_DONE, request);
        CommandsInterfaceEx ci = getCommandsInterface(request.subId.intValue());
        if (ci != null) {
            log("handleGetNrCellSsbid.");
            ci.getNrCellSsbId(onCompleted);
            return;
        }
        loge("getNrCellSsbId ci is null");
        AsyncResultEx.forMessage(onCompleted, (Object) null, (Throwable) null);
        this.mMainHandler.sendMessage(onCompleted);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetNrCellSsbidDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
        if (ar.getException() != null || ar.getResult() == null) {
            log("handleGetNrCellSsbidDone. Exception or result is null.");
            request.result = NrCellSsbIdExt.getDefaultObject();
        } else {
            request.result = ar.getResult();
        }
        synchronized (request) {
            request.notifyAll();
        }
    }

    public String getCTOperator(int slotId, String operator) {
        IHwDcTrackerEx iHwDcTrackerEx;
        PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
        if (phone == null || (iHwDcTrackerEx = phone.getDcTracker().getHwDcTrackerEx()) == null) {
            return operator;
        }
        return iHwDcTrackerEx.getCTOperator(operator);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.internal.telephony.HwPhoneService$HwInnerPhoneService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerPhoneService;
    }

    /* access modifiers changed from: private */
    public static class Record {
        IBinder binder;
        IPhoneCallback callback;
        int events;
        int phoneId;

        private Record() {
            this.phoneId = -1;
        }

        /* access modifiers changed from: package-private */
        public boolean matchPhoneStateListenerEvent(int events2) {
            return (this.callback == null || (this.events & events2) == 0) ? false : true;
        }

        public String toString() {
            return "binder=" + this.binder + " callback=" + this.callback + " phoneId=" + this.phoneId + " events=" + Integer.toHexString(this.events) + "}";
        }

        /* access modifiers changed from: package-private */
        public boolean matchPhoneEvent(int events2) {
            return (this.callback == null || (this.events & events2) == 0) ? false : true;
        }
    }

    /* access modifiers changed from: private */
    public static final class MainThreadRequest {
        public Object argument;
        public Object result;
        public Integer subId;

        public MainThreadRequest(Object argument2) {
            this.argument = argument2;
        }

        public MainThreadRequest(Object argument2, Integer subId2) {
            this.argument = argument2;
            this.subId = subId2;
        }
    }

    /* access modifiers changed from: private */
    public static final class UiccAuthPara {
        byte[] auth;
        int auth_type;
        byte[] rand;

        public UiccAuthPara(int auth_type2, byte[] rand2, byte[] auth2) {
            this.auth_type = auth_type2;
            this.rand = rand2;
            this.auth = auth2;
        }
    }

    /* access modifiers changed from: private */
    public static final class EncryptCallPara {
        byte[] buf = null;
        int event;
        PhoneExt phone = null;

        public EncryptCallPara(PhoneExt phone2, int event2, byte[] buf2) {
            this.phone = phone2;
            this.event = event2;
            this.buf = buf2;
        }
    }

    public static class AidsEngineReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwPhoneService.slog("user click open by HwHiAIDSEngine");
            if (context != null && intent != null && HwPhoneService.ACTION_HIAIDSENGINE_ON.equals(intent.getAction())) {
                Settings.Global.putInt(context.getContentResolver(), HwPhoneService.SECONDARY_CARD_CALL_DATA, 1);
                Settings.Global.putInt(context.getContentResolver(), HwPhoneService.INTELLIGENCE_CARD_SETTING_DB, 1);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                return;
            }
            if (i == HwPhoneService.EVENT_SEND_SIM_MATCHED_OPERATOR_INFO_DONE) {
                HwPhoneService hwPhoneService = HwPhoneService.this;
                hwPhoneService.log("receive EVENT_SEND_SIM_MATCHED_OPERATOR_INFO_DONE, slotId:" + msg.arg1 + ", state:" + msg.arg2);
                HwPhoneService.this.handleSendSimMatchedOperatorInfoDone(msg);
            } else if (i == 3) {
                handleGetPreferredNetworkTypeResponse(msg);
            } else if (i == 4) {
                handleSetPreferredNetworkTypeResponse(msg);
            } else if (i == 5) {
                HwPhoneService.this.log("4G-Switch EVENT_SET_LTE_SWITCH_DONE");
                HwPhoneService.this.handleSetLteSwitchDone(msg);
            } else if (i == 6) {
                HwPhoneService.this.handleQueryCellBandDone(msg);
            } else if (i == HwPhoneService.EVENT_SET_4G_SLOT_DONE) {
                HwPhoneService.this.log("EVENT_SET_4G_SLOT_DONE");
                HwPhoneService.this.handleSetFunctionDone(msg);
            } else if (i != HwPhoneService.EVENT_SET_SUBSCRIPTION_DONE) {
                switch (i) {
                    case 9:
                        HwPhoneService.this.log("EVENT_GET_PREF_NETWORK_TYPE_DONE");
                        HwPhoneService.this.handleGetPrefNetworkTypeDone(msg);
                        return;
                    case 10:
                        HwPhoneService.this.log("EVENT_REG_BAND_CLASS_IND");
                        HwPhoneService.this.handleSarInfoUploaded(1, msg);
                        return;
                    case HwPhoneService.EVENT_REG_ANT_STATE_IND /* 11 */:
                        HwPhoneService.this.log("EVENT_REG_ANT_STATE_IND");
                        HwPhoneService.this.handleSarInfoUploaded(2, msg);
                        return;
                    case 12:
                        HwPhoneService.this.log("EVENT_REG_MAX_TX_POWER_IND");
                        HwPhoneService.this.handleSarInfoUploaded(4, msg);
                        return;
                    default:
                        switch (i) {
                            case HwPhoneService.EVENT_RADIO_AVAILABLE /* 51 */:
                                HwPhoneService.this.handleRadioAvailableInd(msg);
                                return;
                            case HwPhoneService.EVENT_RADIO_NOT_AVAILABLE /* 52 */:
                                HwPhoneService.this.handleRadioNotAvailableInd(msg);
                                return;
                            case HwPhoneService.EVENT_COMMON_IMSA_MAPCON_INFO /* 53 */:
                                HwPhoneService.this.handleCommonImsaToMapconInfoInd(msg);
                                return;
                            default:
                                switch (i) {
                                    case HwPhoneService.CMD_UICC_AUTH /* 101 */:
                                        HwPhoneService.this.handleCmdUiccAuth(msg);
                                        return;
                                    case HwPhoneService.EVENT_UICC_AUTH_DONE /* 102 */:
                                        hanleUiccAuthDone(msg);
                                        return;
                                    case HwPhoneService.CMD_IMS_GET_DOMAIN /* 103 */:
                                        HwPhoneService.this.handleCmdImsGetDomain(msg);
                                        return;
                                    case HwPhoneService.EVENT_IMS_GET_DOMAIN_DONE /* 104 */:
                                        handleImsGetDomainDone(msg);
                                        return;
                                    default:
                                        switch (i) {
                                            case HwPhoneService.EVENT_NOTIFY_CMODEM_STATUS /* 110 */:
                                                HwPhoneService.this.log("EVENT_NOTIFY_CMODEM_STATUS");
                                                handleNotifyCmodemStatus(msg);
                                                return;
                                            case HwPhoneService.EVENT_NOTIFY_DEVICE_STATE /* 111 */:
                                                handleNotifyDeviceState(msg);
                                                return;
                                            case HwPhoneService.EVENT_BASIC_COMM_PARA_UPGRADE_DONE /* 112 */:
                                                handleBasicCommParaUpgradeDone(msg);
                                                return;
                                            case HwPhoneService.EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE /* 113 */:
                                                handleCellularCloudParaUpgradeDone(msg);
                                                return;
                                            case HwPhoneService.EVENT_SEND_LAA_CMD_DONE /* 114 */:
                                                HwPhoneService.this.log("EVENT_SEND_LAA_CMD_DONE");
                                                HwPhoneService.this.handleSendLaaCmdDone(msg);
                                                return;
                                            case HwPhoneService.EVENT_GET_LAA_STATE_DONE /* 115 */:
                                                HwPhoneService.this.log("EVENT_GET_LAA_STATE_DONE");
                                                HwPhoneService.this.handleGetLaaStateDone(msg);
                                                return;
                                            case HwPhoneService.CMD_INVOKE_OEM_RIL_REQUEST_RAW /* 116 */:
                                                HwPhoneService.this.handleCmdOemRilRequestRaw(msg);
                                                return;
                                            case HwPhoneService.EVENT_INVOKE_OEM_RIL_REQUEST_RAW_DONE /* 117 */:
                                                HwPhoneService.this.handleCmdOemRilRequestRawDone(msg);
                                                return;
                                            case HwPhoneService.EVENT_SET_CALLFORWARDING_DONE /* 118 */:
                                                HwPhoneService.this.log("EVENT_SET_CALLFORWARDING_DONE");
                                                HwPhoneService.this.handleSetFunctionDone(msg);
                                                return;
                                            case HwPhoneService.EVENT_GET_CALLFORWARDING_DONE /* 119 */:
                                                HwPhoneService.this.log("EVENT_GET_CALLFORWARDING_DONE");
                                                HwPhoneService.this.handleGetCallforwardDone(msg);
                                                return;
                                            case 120:
                                                handleUsbIetherState(msg);
                                                return;
                                            default:
                                                switch (i) {
                                                    case 500:
                                                        HwPhoneService.this.log("requestForECInfo receive event");
                                                        handleEncryptCallInfo(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE /* 501 */:
                                                        HwPhoneService.this.log("requestForECInfo receive event done");
                                                        handleEncryptCallInfoDone(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE /* 502 */:
                                                        handleQueryEncryptFeature(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE_DONE /* 503 */:
                                                        HwPhoneService.this.handleQueryEncryptFeatureDone(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE /* 504 */:
                                                    case HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE /* 505 */:
                                                        handlePinResult(msg);
                                                        synchronized (HwPhoneService.this.mSetOPinLock) {
                                                            HwPhoneService.this.mSetOPinLock.notifyAll();
                                                        }
                                                        return;
                                                    default:
                                                        handleMessageEx(msg);
                                                        return;
                                                }
                                        }
                                }
                        }
                }
            } else {
                HwPhoneService.this.log("EVENT_SET_SUBSCRIPTION_DONE");
                HwPhoneService.this.handleSetFunctionDone(msg);
            }
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x006e: APUT  
          (r8v7 java.lang.Object[])
          (0 ??[int, short, byte, char])
          (wrap: java.lang.Integer : 0x006a: INVOKE  (r5v11 java.lang.Integer) = (r5v10 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
         */
        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00b8: APUT  
          (r8v5 java.lang.Object[])
          (0 ??[int, short, byte, char])
          (wrap: java.lang.Integer : 0x00b4: INVOKE  (r5v8 java.lang.Integer) = (r5v7 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
         */
        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x010f: APUT  
          (r8v3 java.lang.Object[])
          (0 ??[int, short, byte, char])
          (wrap: java.lang.Integer : 0x010b: INVOKE  (r5v5 java.lang.Integer) = (r5v4 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
         */
        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0159: APUT  
          (r8v1 java.lang.Object[])
          (0 ??[int, short, byte, char])
          (wrap: java.lang.Integer : 0x0155: INVOKE  (r5v2 java.lang.Integer) = (r5v1 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
         */
        private void handleMessageEx(Message msg) {
            Integer index = HwPhoneService.this.getCiIndex(msg);
            int i = msg.what;
            if (i == HwPhoneService.EVENT_ICC_STATUS_CHANGED) {
                HwPhoneService.logi("Received EVENT_ICC_STATUS_CHANGED on index " + index);
                HwPhoneService.this.onIccStatusChanged(index);
            } else if (i == HwPhoneService.EVENT_QUERY_CARD_TYPE_DONE) {
                HwPhoneService.logi("Received EVENT_QUERY_CARD_TYPE_DONE on index " + index);
                HwPhoneService.this.handleQueryCardTypeDone(msg, index);
            } else if (i == 121) {
                HwPhoneService.this.log("EVENT_GET_NUMRECBASESTATION_DONE");
                HwPhoneService.this.handleGetNumRecBaseStattionDone(msg);
            } else if (i != HwPhoneService.EVENT_ANTIFAKE_BASESTATION_CHANGED) {
                switch (i) {
                    case HwPhoneService.EVENT_SET_LINENUM_DONE /* 202 */:
                        HwPhoneService.this.log("EVENT_SET_LINENUM_DONE");
                        HwPhoneService.this.handleSetFunctionDone(msg);
                        return;
                    case HwPhoneService.CMD_SET_DEEP_NO_DISTURB /* 203 */:
                        HwPhoneService.this.log("CMD_SET_DEEP_NO_DISTURB");
                        HwPhoneService.this.handleCmdSetDeepNoDisturb(msg);
                        return;
                    case HwPhoneService.EVENT_SET_DEEP_NO_DISTURB_DONE /* 204 */:
                        HwPhoneService.this.log("EVENT_SET_DEEP_NO_DISTURB_DONE");
                        HwPhoneService.this.handleSetDeepNoDisturbDone(msg);
                        return;
                    case HwPhoneService.CMD_GET_CSCON_ID /* 205 */:
                        HwPhoneService.this.log("CMD_GET_CSCON_ID");
                        HwPhoneService.this.handleGetCscon(msg);
                        return;
                    case HwPhoneService.EVENT_CMD_GET_CSCON_ID_DONE /* 206 */:
                        HwPhoneService.this.log("EVENT_CMD_GET_CSCON_ID_DONE msg.obj" + msg.obj);
                        HwPhoneService.this.handleGetCsconDone(msg);
                        return;
                    case HwPhoneService.CMD_SET_UL_FREQ_BANDWIDTH_RPT /* 207 */:
                        HwPhoneService.this.log("CMD_SET_UL_FREQ_BANDWIDTH_RPT received ");
                        HwPhoneService.this.handleCmdSetUplinkFreqBandwidthReportState(msg);
                        return;
                    case HwPhoneService.EVENT_SET_UL_FREQ_BANDWIDTH_RPT_DONE /* 208 */:
                        HwPhoneService.this.log("EVENT_SET_UL_FREQ_BANDWIDTH_RPT_DONE received ");
                        HwPhoneService.this.handleSetUplinkFreqBandwidthDone(msg);
                        return;
                    case HwPhoneService.EVENT_UL_FREQ_BANDWIDTH_RPT /* 209 */:
                        HwPhoneService.this.log("EVENT_UL_FREQ_BANDWIDTH_RPT received ");
                        HwPhoneService.this.handleUplinkFreqBandwidthRpt(msg);
                        return;
                    case HwPhoneService.CMD_ICC_GET_ATR /* 210 */:
                        HwPhoneService.logi("CMD_ICC_GET_ATR");
                        HwPhoneService.this.handleGetIccAtr(msg);
                        return;
                    case HwPhoneService.EVENT_ICC_GET_ATR_DONE /* 211 */:
                        HwPhoneService.logi("Received EVENT_ICC_GET_ATR_DONE on index " + index);
                        HwPhoneService.this.handleGetIccAtrDone(msg, index);
                        return;
                    default:
                        switch (i) {
                            case HwPhoneService.EVENT_GET_CARD_TRAY_INFO_DONE /* 601 */:
                                HwPhoneService.this.log("EVENT_GET_CARD_TRAY_INFO_DONE");
                                HwPhoneService.this.handleGetCardTrayInfoDone(msg);
                                return;
                            case HwPhoneService.EVENT_SET_TEMP_CTRL_DONE /* 602 */:
                                HwPhoneService.this.log("EVENT_SET_TEMP_CTRL_DONE msg.obj" + msg.obj);
                                HwPhoneService.this.handleSetTempCtrlDone(msg);
                                return;
                            case HwPhoneService.EVENT_SET_NR_OPTION_MODE_DONE /* 603 */:
                                HwPhoneService.logi("Received EVENT_SET_NR_OPTION_MODE_DONE");
                                HwPhoneService.this.handleSetNrOptionMode(msg);
                                return;
                            case HwPhoneService.EVENT_GET_NR_OPTION_MODE_DONE /* 604 */:
                                HwPhoneService.logi("Received EVENT_GET_NR_OPTION_MODE_DONE");
                                HwPhoneService.this.handleGetNrOptionModeDone(msg);
                                return;
                            case HwPhoneService.CMD_GET_NR_OPTION_MODE /* 605 */:
                                HwPhoneService.logi("Received CMD_GET_NR_OPTION_MODE");
                                HwPhoneService.this.handleGetNrOptionMode(msg);
                                return;
                            case HwPhoneService.EVENT_RADIO_AVAILABLE_FOR_NR_OPTION_MODE /* 606 */:
                                HwPhoneService.logi("Received EVENT_RADIO_AVAILABLE_FOR_NR_OPTION_MODE: index " + index);
                                HwPhoneService.this.handleRadioAvailableForNrOptionMode(msg, index.intValue());
                                return;
                            case HwPhoneService.EVENT_RADIO_AVAILABLE_GET_NR_OPTION_MODE_DONE /* 607 */:
                                HwPhoneService.this.handleGetNrOptionModeWhenRadioAvailableDone(msg, index.intValue());
                                return;
                            case HwPhoneService.CMD_GET_NR_CELL_SSBID /* 608 */:
                                HwPhoneService.logi("Received CMD_GET_NR_CELL_SSBID");
                                HwPhoneService.this.handleGetNrCellSsbid(msg);
                                return;
                            case HwPhoneService.EVENT_GET_NR_CELL_SSBID_DONE /* 609 */:
                                HwPhoneService.logi("Received EVENT_GET_NR_CELL_SSBID_DONE");
                                HwPhoneService.this.handleGetNrCellSsbidDone(msg);
                                return;
                            default:
                                int i2 = 2;
                                switch (i) {
                                    case HwPhoneService.EVENT_SMART_CARD_TOAST /* 700 */:
                                        HwPhoneService.this.log("EVENT_SMART_CARD_TOAST");
                                        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(HwPhoneService.this.mContext, HwPhoneService.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
                                        Context context = HwPhoneService.this.mContext;
                                        int resourceId = HwPartResourceUtils.getResourceId("switch_notify_toast");
                                        Object[] objArr = new Object[1];
                                        if (HwPhoneService.this.getDefault4GSlotId() == 1) {
                                            i2 = 1;
                                        }
                                        objArr[0] = Integer.valueOf(i2);
                                        Toast.makeText(contextThemeWrapper, context.getString(resourceId, objArr), 1).show();
                                        return;
                                    case 701:
                                        HwPhoneService.this.log("EVENT_SMART_CARD_SWITCH_TO_SUBCARD_CAUSE_FREE_TRAFFIC_TOAST");
                                        ContextThemeWrapper contextThemeWrapper2 = new ContextThemeWrapper(HwPhoneService.this.mContext, HwPhoneService.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
                                        Context context2 = HwPhoneService.this.mContext;
                                        int resourceId2 = HwPartResourceUtils.getResourceId("smart_card_4G_switch_toast");
                                        Object[] objArr2 = new Object[1];
                                        if (HwPhoneService.this.getDefault4GSlotId() == 1) {
                                            i2 = 1;
                                        }
                                        objArr2[0] = Integer.valueOf(i2);
                                        Toast.makeText(contextThemeWrapper2, context2.getString(resourceId2, objArr2), 1).show();
                                        return;
                                    case 702:
                                        HwPhoneService.this.log("EVENT_SMART_CARD_SWITCH_BACK_TOAST");
                                        ContextThemeWrapper contextThemeWrapper3 = new ContextThemeWrapper(HwPhoneService.this.mContext, HwPhoneService.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
                                        Context context3 = HwPhoneService.this.mContext;
                                        int resourceId3 = HwPartResourceUtils.getResourceId("smart_card_4G_5G_switch_back_toast");
                                        Object[] objArr3 = new Object[2];
                                        if (HwPhoneService.this.getDefault4GSlotId() == 1) {
                                            i2 = 1;
                                        }
                                        objArr3[0] = Integer.valueOf(i2);
                                        objArr3[1] = Integer.valueOf(HwPhoneService.this.getDefault4GSlotId() + 1);
                                        Toast.makeText(contextThemeWrapper3, context3.getString(resourceId3, objArr3), 1).show();
                                        return;
                                    case HwPhoneService.EVENT_SMART_CARD_BIND_TO_SUBCARD_FREE_TRAFFIC_TOAST /* 703 */:
                                        HwPhoneService.this.log("EVENT_SMART_CARD_BIND_TO_SUBCARD_FREE_TRAFFIC_TOAST");
                                        ContextThemeWrapper contextThemeWrapper4 = new ContextThemeWrapper(HwPhoneService.this.mContext, HwPhoneService.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
                                        Context context4 = HwPhoneService.this.mContext;
                                        int resourceId4 = HwPartResourceUtils.getResourceId("smart_card_5G_bind_toast");
                                        Object[] objArr4 = new Object[1];
                                        if (HwPhoneService.this.getDefault4GSlotId() == 1) {
                                            i2 = 1;
                                        }
                                        objArr4[0] = Integer.valueOf(i2);
                                        Toast.makeText(contextThemeWrapper4, context4.getString(resourceId4, objArr4), 1).show();
                                        return;
                                    default:
                                        HwPhoneService.this.mHwInnerPhoneService.handleMessageForServiceEx(msg);
                                        return;
                                }
                        }
                }
            } else {
                HwPhoneService.this.log("EVENT_ANTIFAKE_BASESTATION_CHANGED");
                HwPhoneService.this.handleAntiFakeBaseStation(msg);
            }
        }

        private void handlePinResult(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null || ar.getException() != null) {
                HwPhoneService.this.log("set fail.");
                if (msg.what == HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE) {
                    HwPhoneService.this.setResultForPinLock = false;
                } else if (msg.what == HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE) {
                    HwPhoneService.this.setResultForChangePin = false;
                }
            } else {
                HwPhoneService.this.log("set success.");
                if (msg.what == HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE) {
                    HwPhoneService.this.setResultForPinLock = true;
                } else if (msg.what == HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE) {
                    HwPhoneService.this.setResultForChangePin = true;
                }
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            HwPhoneService.this.log("[enter]handleGetPreferredNetworkTypeResponse");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null || ar.getException() != null) {
                HwPhoneService.this.log("getPreferredNetworkType has exception");
                return;
            }
            int type = ((int[]) ar.getResult())[0];
            HwPhoneService hwPhoneService = HwPhoneService.this;
            hwPhoneService.log("getPreferredNetworkType is " + type);
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            HwPhoneService.this.log("[enter]handleSetPreferredNetworkTypeResponse");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                HwPhoneService.this.log("setPreferredNetworkType ar == null");
            } else if (ar.getException() != null) {
                HwPhoneService.this.log("setPreferredNetworkType has exception");
                HwPhoneService.this.mPhone.getPreferredNetworkType(obtainMessage(3));
            } else {
                int setPrefMode = ((Integer) ar.getUserObj()).intValue();
                if (HwPhoneService.this.getCurrentNetworkTypeFromDB() != setPrefMode) {
                    HwPhoneService.this.saveNetworkTypeToDB(setPrefMode);
                }
            }
        }

        private void hanleUiccAuthDone(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
            HwPhoneService.loge("EVENT_UICC_AUTH_DONE");
            request.result = new UiccAuthResponse();
            if (ar.getException() == null && ar.getResult() != null) {
                request.result = ar.getResult();
            } else if (ar.getResult() == null) {
                HwPhoneService.loge("UiccAuthReq: Empty response");
            } else if (CommandExceptionEx.isCommandException(ar.getException())) {
                HwPhoneService.loge("UiccAuthReq: CommandException: has exception");
            } else {
                HwPhoneService.loge("UiccAuthReq: Unknown exception");
            }
            synchronized (request) {
                request.notifyAll();
            }
        }

        private void handleImsGetDomainDone(Message msg) {
            HwPhoneService.this.log("EVENT_IMS_GET_DOMAIN_DONE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
            if (ar.getException() == null && ar.getResult() != null) {
                request.result = ar.getResult();
            } else if (ar.getResult() == null) {
                request.result = new int[]{2};
                HwPhoneService.loge("getImsDomain: Empty response,return 2");
            } else if (CommandExceptionEx.isCommandException(ar.getException())) {
                request.result = new int[]{2};
                HwPhoneService.loge("getImsDomain: CommandException:return 2");
            } else {
                request.result = new int[]{2};
                HwPhoneService.loge("getImsDomain: Unknown exception,return 2");
            }
            synchronized (request) {
                request.notifyAll();
            }
        }

        private void handleEncryptCallInfoDone(Message msg) {
            HwPhoneService.this.log("EVENT_ENCRYPT_CALL_INFO_DONE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
            if (ar.getException() == null) {
                if (ar.getResult() == null || ((byte[]) ar.getResult()).length <= 0) {
                    request.result = new byte[]{1};
                    HwPhoneService.this.log("requestForECInfo success,return 1");
                } else {
                    request.result = ar.getResult();
                    HwPhoneService.this.log("requestForECInfo success,return ar.getResult()");
                }
            } else if (CommandExceptionEx.isCommandException(ar.getException())) {
                request.result = new byte[]{-1};
                HwPhoneService.loge("requestForECInfo: CommandException:return -1");
            } else {
                request.result = new byte[]{-2};
                HwPhoneService.loge("requestForECInfo: Unknown exception,return -2");
            }
            synchronized (request) {
                request.notifyAll();
            }
        }

        private void handleNotifyCmodemStatus(Message msg) {
            IPhoneCallback callback;
            HwPhoneService.this.log("EVENT_NOTIFY_CMODEM_STATUS");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar != null && (callback = (IPhoneCallback) ar.getUserObj()) != null) {
                int result = 1;
                if (ar.getException() != null) {
                    result = -1;
                }
                try {
                    HwPhoneService.this.log("EVENT_NOTIFY_CMODEM_STATUS onCallback1");
                    callback.onCallback1(result);
                } catch (RemoteException e) {
                    HwPhoneService.loge("EVENT_NOTIFY_CMODEM_STATUS onCallback1 RemoteException");
                }
            }
        }

        private void handleNotifyDeviceState(Message msg) {
            HwPhoneService.this.log("EVENT_NOTIFY_DEVICE_STATE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, ar is null.");
            } else if (ar.getException() == null) {
                HwPhoneService.this.log("EVENT_NOTIFY_DEVICE_STATE success.");
            } else if (CommandExceptionEx.isCommandException(ar.getException())) {
                HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, has exception.");
            } else {
                HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, unknown exception.");
            }
        }

        private void handleBasicCommParaUpgradeDone(Message msg) {
            HwPhoneService.this.log("EVENT_BASIC_COMM_PARA_UPGRADE_DONE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() != null) {
                HwPhoneService.loge("Error in BasicImsNVPara Upgrade, has exception.");
                return;
            }
            int[] resultUpgrade = (int[]) ar.getResult();
            if (resultUpgrade.length != 0) {
                HwPhoneService hwPhoneService = HwPhoneService.this;
                hwPhoneService.log("EVENT_BASIC_COMM_PARA_UPGRADE_DONE: result=" + resultUpgrade[0]);
            } else {
                HwPhoneService.loge("EVENT_BASIC_COMM_PARA_UPGRADE_DONE: resultUpgrade.length = 0");
                resultUpgrade[0] = -1;
            }
            EmcomManagerExt.getInstance().responseForParaUpgrade(1, 1, resultUpgrade[0]);
            HwPhoneService.this.log("responseForParaUpgrade()");
        }

        private void handleCellularCloudParaUpgradeDone(Message msg) {
            HwPhoneService.this.log("EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() != null) {
                HwPhoneService.loge("Error in Cellular Cloud Para Upgrade, has exception.");
                return;
            }
            int[] phoneResult = (int[]) ar.getResult();
            if (phoneResult.length != 0) {
                HwPhoneService hwPhoneService = HwPhoneService.this;
                hwPhoneService.log("EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE: phoneResult=" + phoneResult[0]);
            } else {
                HwPhoneService.loge("EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE: phoneResult.length = 0");
                phoneResult[0] = -1;
            }
            EmcomManagerExt.getInstance().responseForParaUpgrade(2, 1, phoneResult[0]);
            HwPhoneService.this.log("responseForParaUpgrade()");
        }

        private void handleEncryptCallInfo(Message msg) {
            MainThreadRequest request = (MainThreadRequest) msg.obj;
            EncryptCallPara ecPara = (EncryptCallPara) request.argument;
            Message onCompleted = obtainMessage(HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE, request);
            PhoneExt phone = ecPara.phone;
            if (phone != null) {
                phone.requestForECInfo(onCompleted, ecPara.event, ecPara.buf);
            }
        }

        private void handleQueryEncryptFeature(Message msg) {
            HwPhoneService.this.log("radio available, query encrypt call feature");
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                for (PhoneExt phone : HwPhoneService.this.mPhones) {
                    HwPhoneService.this.handleEventQueryEncryptCall(phone);
                }
                return;
            }
            HwPhoneService hwPhoneService = HwPhoneService.this;
            hwPhoneService.handleEventQueryEncryptCall(hwPhoneService.mPhone);
        }

        private void handleUsbIetherState(Message msg) {
            HwPhoneService.this.log("EVENT_USB_IETHER_STATE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null || ar.getException() == null) {
                HwPhoneService.this.log("EVENT_USB_TETHER_STATE is success.");
            } else {
                HwPhoneService.loge("EVENT_USB_TETHER_STATE is failed.");
            }
        }
    }

    private final class PhoneStateHandler extends Handler {
        PhoneStateHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                handleRadioAvailable(msg);
            } else if (i == 2) {
                handleRadioNotAvailable(msg);
            } else if (i == 4) {
                handleCommonImsaToMapconInfo(msg);
            }
        }

        private void handleRadioAvailable(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            int phoneId = HwPhoneService.this.getCiIndex(msg).intValue();
            if (ar.getException() == null) {
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 1, 0, null);
            } else {
                HwPhoneService.loge("radio available exception");
            }
        }

        private void handleRadioNotAvailable(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            int phoneId = HwPhoneService.this.getCiIndex(msg).intValue();
            if (ar.getException() == null) {
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 2, 0, null);
            } else {
                HwPhoneService.loge("radio not available exception");
            }
        }

        private void handleCommonImsaToMapconInfo(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            int phoneId = HwPhoneService.this.getCiIndex(msg).intValue();
            if (ar.getException() == null) {
                Bundle bundle = new Bundle();
                bundle.putByteArray("imsa2mapcon_msg", (byte[]) ar.getResult());
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 4, 0, bundle);
                return;
            }
            HwPhoneService.loge("imsa to mapcon info exception");
        }
    }

    /* access modifiers changed from: private */
    public class PhoneServiceReceiver extends BroadcastReceiver {
        public PhoneServiceReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.RADIO_TECHNOLOGY");
            HwPhoneService.this.mContext.registerReceiver(this, filter);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwPhoneService.this.log("radio tech changed, query encrypt call feature");
            if (intent != null && "android.intent.action.RADIO_TECHNOLOGY".equals(intent.getAction())) {
                int unused = HwPhoneService.queryCount = 0;
                if (TelephonyManagerEx.isMultiSimEnabled()) {
                    for (PhoneExt phone : HwPhoneService.this.mPhones) {
                        HwPhoneService.this.handleEventQueryEncryptCall(phone);
                    }
                    return;
                }
                HwPhoneService hwPhoneService = HwPhoneService.this;
                hwPhoneService.handleEventQueryEncryptCall(hwPhoneService.mPhone);
            }
        }
    }

    /* access modifiers changed from: private */
    public class IntelligenceCardObserver extends ContentObserver {
        public IntelligenceCardObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            int retVal = Settings.Global.getInt(HwPhoneService.this.mContext.getContentResolver(), HwPhoneService.INTELLIGENCE_CARD_SETTING_DB, 0);
            HwPhoneService hwPhoneService = HwPhoneService.this;
            hwPhoneService.log("Intelligence Card switch changed to " + retVal);
            if (retVal == 1) {
                SystemPropertiesEx.set("persist.radio.smart.card", "true");
                SystemPropertiesEx.set(HwPhoneService.PROP_NERVER_NOTIFY, "false");
                DecisionUtil.bindService(HwPhoneService.this.mContext, HwPhoneService.USED_DECISION_NAME);
            }
        }
    }

    public void inputDialerSpecialCode(String inputCode) {
        Context context = this.mContext;
        if (context != null && this.mPhone != null) {
            context.enforceCallingPermission("android.permission.CALL_PHONE", null);
            this.mPhone.sendDialerSpecialCode(inputCode);
        }
    }

    public class HwInnerPhoneService extends IHwTelephonyInner.Stub {
        HwInnerPhoneService() {
        }

        public int getLevelForSa(int phoneId, int nrLevel, int primaryLevel) {
            return HwPhoneService.this.mHwPhoneServiceEx.getLevelForSa(phoneId, nrLevel, primaryLevel);
        }

        public int getRrcConnectionState(int slotId) {
            return HwPhoneService.this.mHwPhoneServiceEx.getRrcConnectionState(slotId);
        }

        public int getPlatformSupportVsimVer(int what) {
            return HwPhoneService.this.mHwPhoneServiceEx.getPlatformSupportVsimVer(what);
        }

        public String getRegPlmn(int slotId) {
            return HwPhoneService.this.mHwPhoneServiceEx.getRegPlmn(slotId);
        }

        public boolean setVsimUserReservedSubId(int slotId) {
            return HwPhoneService.this.mHwPhoneServiceEx.setVsimUserReservedSubId(slotId);
        }

        public int getVsimUserReservedSubId() {
            return HwPhoneService.this.mHwPhoneServiceEx.getVsimUserReservedSubId();
        }

        public void blockingGetVsimService(IGetVsimServiceCallback callback) throws RemoteException {
            HwPhoneService.this.mHwPhoneServiceEx.blockingGetVsimService(callback);
        }

        public boolean isVsimEnabledByDatabase() {
            return HwPhoneService.this.mHwPhoneServiceEx.isVsimEnabledByDatabase();
        }

        public void handleMessageForServiceEx(Message msg) {
            if (HwPhoneService.this.mHwPhoneServiceEx == null) {
                HwPhoneService.this.log("handleMessageForServiceEx the phoneServiceEx is null");
            } else {
                HwPhoneService.this.mHwPhoneServiceEx.handleMessageForServiceEx(msg);
            }
        }

        public void setSimPowerStateForSlot(int slotIndex, int state, Message msg) {
            HwPhoneService.this.mHwPhoneServiceEx.setSimPowerStateForSlot(slotIndex, state, msg);
        }

        public boolean isCustomSmart() {
            return HwPhoneService.this.mHwPhoneServiceEx.isCustomSmart();
        }

        public boolean isSmartCard(int slotId) {
            return HwPhoneService.this.mHwPhoneServiceEx.isSmartCard(slotId);
        }

        public boolean isBlockNonCustomSlot(int slotId, int customBlockType) {
            return HwPhoneService.this.mHwPhoneServiceEx.isBlockNonCustomSlot(slotId, customBlockType);
        }

        public boolean registerForRadioStateChanged(IHwCommonPhoneCallback callback) {
            return HwPhoneService.this.mHwPhoneServiceEx.registerForRadioStateChanged(callback);
        }

        public boolean unregisterForRadioStateChanged(IHwCommonPhoneCallback callback) {
            return HwPhoneService.this.mHwPhoneServiceEx.unregisterForRadioStateChanged(callback);
        }

        public int getNetworkMode(int phoneId) {
            return HwPhoneService.this.mHwPhoneServiceEx.getNetworkMode(phoneId);
        }
    }
}
