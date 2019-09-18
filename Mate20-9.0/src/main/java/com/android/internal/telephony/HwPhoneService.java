package com.android.internal.telephony;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.emcom.EmcomManager;
import android.net.ConnectivityManager;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAuthResponse;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.ims.HwImsUtManager;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsUt;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.IHwTelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.dataconnection.InCallDataStateMachine;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.gsm.HwGsmServiceStateManager;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

public class HwPhoneService extends IHwTelephony.Stub implements HwPhoneManager.PhoneServiceInterface {
    private static final String ACTION_CANCEL_NOTIFY = "huawei.intent.action.cancel.notify";
    private static final String ACTION_HIAIDSENGINE_ON = "com.huawei.broadcast.intent.SMART_CARD_ON";
    private static final String ACTION_IGNORE_NOTIFY = "huawei.intent.action.ignore.notify";
    private static final String ACTION_NEVER_NOTIFY = "huawei.intent.action.never.notify";
    private static final String ACTION_SWITCH_BACK = "huawei.intent.action_switch_back";
    private static final String CALLBACK_AFBS_INFO = "AntiFakeBaseStationInfo";
    private static final String CALLBACK_CF_INFO = "CallForwardInfos";
    private static final String CALLBACK_EXCEPTION = "EXCEPTION";
    private static final String CALLBACK_RESULT = "RESULT";
    private static final String CARDMANAGER_AVTIVITY = "com.huawei.settings.intent.DUAL_CARD_SETTINGS";
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final int CMD_ENCRYPT_CALL_INFO = 500;
    private static final int CMD_GET_CSCON_ID = 205;
    private static final int CMD_HANDLE_DEMO = 1;
    private static final int CMD_IMS_GET_DOMAIN = 103;
    private static final int CMD_INVOKE_OEM_RIL_REQUEST_RAW = 116;
    private static final int CMD_SET_DEEP_NO_DISTURB = 203;
    private static final int CMD_SET_UL_FREQ_BANDWIDTH_RPT = 207;
    private static final int CMD_UICC_AUTH = 101;
    private static final String DATA_FLOW_NOTIFY_CHANNEL = "data_flow_notify_channel";
    private static final String DAY_MODE = "day_mode";
    private static final String DAY_MODE_TIME = "day_mode_time";
    private static final int DEFAULT_INTELLIGENT_SWITCH_VALUE = 0;
    private static final String DEVICEID_PREF = "deviceid";
    private static final int DOWNLINK_LIMIT = SystemProperties.getInt("ro.config.network_limit_speed", LIMIT_DEFAULT_VALUE);
    public static final int ERROR_INVALID_PARAM = -3;
    public static final int ERROR_NO_SERVICE = -1;
    public static final int ERROR_REMOTE_EXCEPTION = -2;
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
    private static final int EVENT_GET_NUMRECBASESTATION_DONE = 121;
    private static final int EVENT_GET_PREF_NETWORKS = 3;
    private static final int EVENT_GET_PREF_NETWORK_TYPE_DONE = 9;
    private static final int EVENT_ICC_GET_ATR_DONE = 507;
    private static final int EVENT_ICC_STATUS_CHANGED = 54;
    private static final int EVENT_IMS_GET_DOMAIN_DONE = 104;
    private static final int EVENT_INVOKE_OEM_RIL_REQUEST_RAW_DONE = 117;
    private static final int EVENT_NOTIFY_CMODEM_STATUS = 110;
    private static final int EVENT_NOTIFY_DEVICE_STATE = 111;
    private static final int EVENT_QUERY_CARD_TYPE_DONE = 506;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE = 502;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE_DONE = 503;
    private static final int EVENT_RADIO_AVAILABLE = 51;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 52;
    private static final int EVENT_REG_ANT_STATE_IND = 11;
    private static final int EVENT_REG_BAND_CLASS_IND = 10;
    private static final int EVENT_REG_MAX_TX_POWER_IND = 12;
    private static final int EVENT_RETRY_SET_PREF_NETWORK_TYPE = 14;
    private static final int EVENT_SEND_LAA_CMD_DONE = 114;
    private static final int EVENT_SET_4G_SLOT_DONE = 200;
    private static final int EVENT_SET_CALLFORWARDING_DONE = 118;
    private static final int EVENT_SET_DEEP_NO_DISTURB_DONE = 204;
    private static final int EVENT_SET_LINENUM_DONE = 202;
    private static final int EVENT_SET_LTE_SWITCH_DONE = 5;
    private static final int EVENT_SET_PREF_NETWORKS = 4;
    private static final int EVENT_SET_PREF_NETWORK_TYPE_DONE = 13;
    private static final int EVENT_SET_SUBSCRIPTION_DONE = 201;
    private static final int EVENT_SET_TEMP_CTRL_DONE = 602;
    private static final int EVENT_SET_UL_FREQ_BANDWIDTH_RPT_DONE = 208;
    private static final int EVENT_SMART_CARD_TOAST = 700;
    private static final int EVENT_UICC_AUTH_DONE = 102;
    private static final int EVENT_UL_FREQ_BANDWIDTH_RPT = 209;
    private static final int EVENT_USB_TETHER_STATE = 120;
    private static final int HW_PHONE_EXTEND_EVENT_BASE = 600;
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final String IMEI_PREF = "imei";
    private static final String INCOMING_SMS_LIMIT = "incoming_limit";
    private static final String INTELLIGENCE_CARD_SETTING_DB = "intelligence_card_switch";
    private static final int INVALID = -1;
    private static final int INVALID_NETWORK_MODE = -1;
    private static final int INVALID_STEP = -99;
    private static final boolean IS_4G_SWITCH_SUPPORTED = SystemProperties.getBoolean("persist.sys.dualcards", false);
    /* access modifiers changed from: private */
    public static final boolean IS_FULL_NETWORK_SUPPORTED = SystemProperties.getBoolean(HwFullNetworkConfig.PROPERTY_FULL_NETWORK_SUPPORT, false);
    private static final boolean IS_GSM_NONSUPPORT = SystemProperties.getBoolean("ro.config.gsm_nonsupport", false);
    private static final boolean IS_MTK_PLATFORM = HuaweiTelephonyConfigs.isMTKPlatform();
    private static final String IS_OUTGOING = "isOutgoing";
    private static final String KEY1 = "key1";
    private static final int LIMIT_DEFAULT_VALUE = 8000;
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
    private static final String OUTGOING_SMS_LIMIT = "outgoing_limit";
    private static final int PARATYPE_BASIC_COMM = 1;
    private static final int PARATYPE_CELLULAR_CLOUD = 2;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final String PATTERN_SIP = "^sip:(\\+)?[0-9]+@[^@]+";
    private static final String PATTERN_TEL = "^tel:(\\+)?[0-9]+";
    private static final String POLICY_REMOVE_ALL = "remove_all_policy";
    private static final String POLICY_REMOVE_SINGLE = "remove_single_policy";
    private static final String PROP_NERVER_NOTIFY = "persist.radio.never.notify";
    public static final String REC_DECISION_NAME = "com.huawei.android.dsdscardmanger.intent.action.Rec";
    private static final int REDUCE_TYPE_CELL = 1;
    private static final int REDUCE_TYPE_WIFI = 2;
    private static final int REGISTER_TYPE = 1;
    private static final int REGISTER_TYPE_ANTENNA = 2;
    private static final int REGISTER_TYPE_BAND = 1;
    private static final int REGISTER_TYPE_MAX_TX_POWER = 4;
    private static final String REMOVE_TYPE = "removeType";
    private static final int RETRY_MAX_TIME = 20;
    private static final String SECONDARY_CARD_CALL_DATA = "incall_data_switch";
    private static final int SERVICE_2G_OFF = 0;
    /* access modifiers changed from: private */
    public static final boolean SHOW_DIALOG_FOR_NO_SIM = SystemProperties.getBoolean("ro.config.no_sim", false);
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final int STATE_IN_AIR_PLANE_MODE = 1;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final int SUB_NONE = -1;
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
    public static final String USED_DECISION_NAME = "com.huawei.android.dsdscardmanger.intent.action.Used";
    private static final String USED_OF_DAY = "used_number_day";
    private static final String USED_OF_MONTH = "used_number_month";
    private static final String USED_OF_WEEK = "used_number_week";
    private static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
    private static final String WEEK_MODE = "week_mode";
    private static final String WEEK_MODE_TIME = "week_mode_time";
    /* access modifiers changed from: private */
    public static int queryCount = 0;
    private static HwPhoneService sInstance = null;
    private static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private final int ENCRYPT_CALL_FEATURE_CLOSE = 0;
    private final String ENCRYPT_CALL_FEATURE_KEY = "encrypt_version";
    private final int ENCRYPT_CALL_FEATURE_OPEN = 1;
    private final int ENCRYPT_CALL_FEATURE_SUPPORT = 1;
    private final int ENCRYPT_CALL_NV_OFFSET = 4;
    private final int SIGNAL_TYPE_CDMA = 3;
    private final int SIGNAL_TYPE_CDMALTE = 6;
    private final int SIGNAL_TYPE_EVDO = 4;
    private final int SIGNAL_TYPE_GSM = 1;
    private final int SIGNAL_TYPE_LTE = 5;
    private final int SIGNAL_TYPE_NR = 7;
    private final int SIGNAL_TYPE_UMTS = 2;
    private IHwCommBoosterServiceManager bm = null;
    private Message getLaaStateCompleteMsg = null;
    private IPhoneCallback mAntiFakeBaseStationCB = null;
    private AppOpsManager mAppOps;
    private byte[] mCardTrayInfo = null;
    private final Object mCardTrayLock = new Object();
    /* access modifiers changed from: private */
    public int[] mCardTypes = new int[SIM_NUM];
    /* access modifiers changed from: private */
    public Context mContext;
    private int mEncryptCallStatus = 0;
    private boolean mHasNotifyLimit = false;
    private HwCustPhoneService mHwCustPhoneService = null;
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        public void callBack(int type, Bundle b) throws RemoteException {
            HwPhoneService hwPhoneService = HwPhoneService.this;
            hwPhoneService.log("receive booster callback type " + type);
            if (b == null) {
                HwPhoneService.this.log("data is null");
                return;
            }
            switch (type) {
                case 1:
                    HwPhoneService.this.log("Temporarily forbidden");
                    break;
                case 2:
                    if (!SystemProperties.getBoolean(HwPhoneService.PROP_NERVER_NOTIFY, false)) {
                        HwPhoneService.this.log("data flow notify");
                        HwPhoneService.this.showDataFlowNotification(b.getInt("slaveCardDataFlowCost"));
                        break;
                    } else {
                        HwPhoneService.this.log("user clicked never notify");
                        return;
                    }
                case 3:
                    HwPhoneService.this.handleNetworkSpeedLimit(b);
                    break;
                case 4:
                    HwPhoneService.this.mMainHandler.sendMessage(HwPhoneService.this.mMainHandler.obtainMessage(HwPhoneService.EVENT_SMART_CARD_TOAST));
                    break;
                case 5:
                    Context access$3400 = HwPhoneService.this.mContext;
                    int i = 1;
                    Object[] objArr = new Object[1];
                    if (HwPhoneService.this.getDefault4GSlotId() != 1) {
                        i = 2;
                    }
                    objArr[0] = Integer.valueOf(i);
                    HwPhoneService.this.showNotification(access$3400.getString(33686068, objArr), HwPhoneService.this.mContext.getString(33686067));
                    break;
            }
        }
    };
    private IPhoneCallback mImsaToMapconInfoCB = null;
    InCallDataStateMachine mInCallDataStateMachine;
    private IntelligenceCardObserver mIntelligenceCardObserver = null;
    private BroadcastReceiver mIntelligenceCardReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (HwPhoneService.ACTION_CANCEL_NOTIFY.equals(action)) {
                    HwPhoneService.this.log("Received cancel broadcast");
                    HwPhoneService.this.dismissNotification(1);
                } else if (HwPhoneService.ACTION_SWITCH_BACK.equals(action)) {
                    HwPhoneService.this.log("switch data to default slotid");
                    HwPhoneService.this.repotToBoosterForSwitchBack();
                    HwPhoneService.this.dismissNotification(2);
                } else if (HwPhoneService.ACTION_NEVER_NOTIFY.equals(action)) {
                    SystemProperties.set(HwPhoneService.PROP_NERVER_NOTIFY, "true");
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
    protected final Object mLock = new Object();
    private BroadcastReceiver mMDMSmsReceiver = new BroadcastReceiver() {
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
    /* access modifiers changed from: private */
    public MainHandler mMainHandler;
    private HandlerThread mMessageThread = new HandlerThread("HuaweiPhoneTempService");
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public HwPhone mPhone;
    private PhoneServiceReceiver mPhoneServiceReceiver;
    private PhoneStateHandler mPhoneStateHandler;
    /* access modifiers changed from: private */
    public HwPhone[] mPhones = null;
    private IPhoneCallback mRadioAvailableIndCB = null;
    private IPhoneCallback mRadioNotAvailableIndCB = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                if (HwPhoneService.this.mPhone == null) {
                    HwPhoneService.loge("received ACTION_SIM_STATE_CHANGED, but mPhone is null!");
                    return;
                }
                int slotId = intent.getIntExtra("slot", -1000);
                String simState = intent.getStringExtra("ss");
                if (!HwPhoneService.IS_FULL_NETWORK_SUPPORTED && "READY".equals(simState)) {
                    HwPhoneService.this.log("mReceiver receive ACTION_SIM_STATE_CHANGED READY,check pref network type");
                    HwPhoneService.this.mPhone.getPreferredNetworkType(HwPhoneService.this.mMainHandler.obtainMessage(9));
                }
                if (HwPhoneService.IS_FULL_NETWORK_SUPPORTED && "IMSI".equals(simState)) {
                    HwPhoneService.this.setSingleCardPrefNetwork(slotId);
                }
            }
            if (!(HwPhoneService.SHOW_DIALOG_FOR_NO_SIM == 0 || intent == null || !"android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (telephonyManager != null && 1 == telephonyManager.getSimState()) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, 3);
                    dialogBuilder.setTitle(33685979).setMessage(33685980).setCancelable(false).setPositiveButton(33685981, null);
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.getWindow().setType(HwFullNetworkConstants.EVENT_GET_PREF_NETWORK_MODE_DONE);
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
    protected final Object mSetOPinLock = new Object();
    private BroadcastReceiver mSetRadioCapDoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(intent.getAction())) {
                HwPhoneService.this.handleSwitchSlotDone(intent);
            }
        }
    };
    private Message mSetTempCtrlCompleteMsg = null;
    boolean mSupportEncryptCall = SystemProperties.getBoolean("persist.sys.cdma_encryption", false);
    private IPhoneCallback mUlFreqReportCB = null;
    private int mUlFreqRptSubId = -1;
    Phone phone;
    private int retryCount = 0;
    private Message sendLaaCmdCompleteMsg = null;
    /* access modifiers changed from: private */
    public boolean setResultForChangePin = false;
    /* access modifiers changed from: private */
    public boolean setResultForPinLock = false;

    public static class AidsEngineReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Rlog.d(HwPhoneService.LOG_TAG, "user click open by HwHiAIDSEngine");
            if (intent != null && HwPhoneService.ACTION_HIAIDSENGINE_ON.equals(intent.getAction())) {
                Settings.Global.putInt(context.getContentResolver(), HwPhoneService.SECONDARY_CARD_CALL_DATA, 1);
                Settings.Global.putInt(context.getContentResolver(), HwPhoneService.INTELLIGENCE_CARD_SETTING_DB, 1);
            }
        }
    }

    private static final class EncryptCallPara {
        byte[] buf = null;
        int event;
        HwPhone phone = null;

        public EncryptCallPara(HwPhone phone2, int event2, byte[] buf2) {
            this.phone = phone2;
            this.event = event2;
            this.buf = buf2;
        }
    }

    private class IntelligenceCardObserver extends ContentObserver {
        public IntelligenceCardObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            int retVal = Settings.Global.getInt(HwPhoneService.this.mContext.getContentResolver(), HwPhoneService.INTELLIGENCE_CARD_SETTING_DB, 0);
            HwPhoneService hwPhoneService = HwPhoneService.this;
            hwPhoneService.log("Intelligence Card switch changed to " + retVal);
            if (retVal == 1) {
                SystemProperties.set("persist.radio.smart.card", "true");
                SystemProperties.set(HwPhoneService.PROP_NERVER_NOTIFY, "false");
                DecisionUtil.bindService(HwPhoneService.this.mContext, HwPhoneService.USED_DECISION_NAME);
            }
        }
    }

    private final class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1) {
                switch (i) {
                    case 3:
                        handleGetPreferredNetworkTypeResponse(msg);
                        return;
                    case 4:
                        handleSetPreferredNetworkTypeResponse(msg);
                        return;
                    case 5:
                        HwPhoneService.this.log("4G-Switch EVENT_SET_LTE_SWITCH_DONE");
                        HwPhoneService.this.handleSetLteSwitchDone(msg);
                        return;
                    case 6:
                        HwPhoneService.this.handleQueryCellBandDone(msg);
                        return;
                    default:
                        switch (i) {
                            case 9:
                                HwPhoneService.this.log("EVENT_GET_PREF_NETWORK_TYPE_DONE");
                                HwPhoneService.this.handleGetPrefNetworkTypeDone(msg);
                                return;
                            case 10:
                                HwPhoneService.this.log("EVENT_REG_BAND_CLASS_IND");
                                HwPhoneService.this.handleSarInfoUploaded(1, msg);
                                return;
                            case 11:
                                HwPhoneService.this.log("EVENT_REG_ANT_STATE_IND");
                                HwPhoneService.this.handleSarInfoUploaded(2, msg);
                                return;
                            case 12:
                                HwPhoneService.this.log("EVENT_REG_MAX_TX_POWER_IND");
                                HwPhoneService.this.handleSarInfoUploaded(4, msg);
                                return;
                            default:
                                switch (i) {
                                    case 51:
                                        HwPhoneService.this.handleRadioAvailableInd(msg);
                                        return;
                                    case 52:
                                        HwPhoneService.this.handleRadioNotAvailableInd(msg);
                                        return;
                                    case 53:
                                        HwPhoneService.this.handleCommonImsaToMapconInfoInd(msg);
                                        return;
                                    default:
                                        switch (i) {
                                            case HwPhoneService.CMD_UICC_AUTH /*101*/:
                                                HwPhoneService.this.handleCmdUiccAuth(msg);
                                                return;
                                            case HwPhoneService.EVENT_UICC_AUTH_DONE /*102*/:
                                                hanleUiccAuthDone(msg);
                                                return;
                                            case HwPhoneService.CMD_IMS_GET_DOMAIN /*103*/:
                                                HwPhoneService.this.handleCmdImsGetDomain(msg);
                                                return;
                                            case HwPhoneService.EVENT_IMS_GET_DOMAIN_DONE /*104*/:
                                                handleImsGetDomainDone(msg);
                                                return;
                                            default:
                                                switch (i) {
                                                    case HwPhoneService.EVENT_NOTIFY_CMODEM_STATUS /*110*/:
                                                        Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS");
                                                        handleNotifyCmodemStatus(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_NOTIFY_DEVICE_STATE /*111*/:
                                                        handleNotifyDeviceState(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_BASIC_COMM_PARA_UPGRADE_DONE /*112*/:
                                                        handleBasicCommParaUpgradeDone(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE /*113*/:
                                                        handleCellularCloudParaUpgradeDone(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_SEND_LAA_CMD_DONE /*114*/:
                                                        HwPhoneService.this.log("EVENT_SEND_LAA_CMD_DONE");
                                                        HwPhoneService.this.handleSendLaaCmdDone(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_GET_LAA_STATE_DONE /*115*/:
                                                        HwPhoneService.this.log("EVENT_GET_LAA_STATE_DONE");
                                                        HwPhoneService.this.handleGetLaaStateDone(msg);
                                                        return;
                                                    case HwPhoneService.CMD_INVOKE_OEM_RIL_REQUEST_RAW /*116*/:
                                                        HwPhoneService.this.handleCmdOemRilRequestRaw(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_INVOKE_OEM_RIL_REQUEST_RAW_DONE /*117*/:
                                                        HwPhoneService.this.handleCmdOemRilRequestRawDone(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_SET_CALLFORWARDING_DONE /*118*/:
                                                        HwPhoneService.this.log("EVENT_SET_CALLFORWARDING_DONE");
                                                        HwPhoneService.this.handleSetFunctionDone(msg);
                                                        return;
                                                    case HwPhoneService.EVENT_GET_CALLFORWARDING_DONE /*119*/:
                                                        HwPhoneService.this.log("EVENT_GET_CALLFORWARDING_DONE");
                                                        HwPhoneService.this.handleGetCallforwardDone(msg);
                                                        return;
                                                    case 120:
                                                        handleUsbIetherState(msg);
                                                        return;
                                                    default:
                                                        switch (i) {
                                                            case 200:
                                                                HwPhoneService.this.log("EVENT_SET_4G_SLOT_DONE");
                                                                HwPhoneService.this.handleSetFunctionDone(msg);
                                                                return;
                                                            case 201:
                                                                HwPhoneService.this.log("EVENT_SET_SUBSCRIPTION_DONE");
                                                                HwPhoneService.this.handleSetFunctionDone(msg);
                                                                return;
                                                            default:
                                                                switch (i) {
                                                                    case HwFullNetworkConstants.MESSAGE_PENDING_DELAY /*500*/:
                                                                        Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo receive event");
                                                                        handleEncryptCallInfo(msg);
                                                                        return;
                                                                    case HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE /*501*/:
                                                                        Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo receive event done");
                                                                        handleEncryptCallInfoDone(msg);
                                                                        return;
                                                                    case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE /*502*/:
                                                                        handleQueryEncryptFeature(msg);
                                                                        return;
                                                                    case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE_DONE /*503*/:
                                                                        HwPhoneService.this.handleQueryEncryptFeatureDone(msg);
                                                                        return;
                                                                    case HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE /*504*/:
                                                                    case HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE /*505*/:
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
                        }
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v53, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.os.AsyncResult} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void handleMessageEx(Message msg) {
            AsyncResult ar = null;
            Integer index = null;
            if (msg.what == 54 || msg.what == HwPhoneService.EVENT_QUERY_CARD_TYPE_DONE || msg.what == HwPhoneService.EVENT_ICC_GET_ATR_DONE) {
                index = HwPhoneService.this.getCiIndex(msg);
                if (index.intValue() < 0 || index.intValue() >= HwPhoneService.this.mPhones.length) {
                    HwPhoneService.loge("Invalid index : " + index + " received with event " + msg.what);
                    return;
                } else if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
                    ar = msg.obj;
                }
            }
            int i = msg.what;
            if (i == 54) {
                Rlog.d(HwPhoneService.LOG_TAG, "Received EVENT_ICC_STATUS_CHANGED on index " + index);
                HwPhoneService.this.onIccStatusChanged(index);
            } else if (i != HwPhoneService.EVENT_SMART_CARD_TOAST) {
                switch (i) {
                    case HwPhoneService.EVENT_GET_NUMRECBASESTATION_DONE /*121*/:
                        HwPhoneService.this.log("EVENT_GET_NUMRECBASESTATION_DONE");
                        HwPhoneService.this.handleGetNumRecBaseStattionDone(msg);
                        break;
                    case HwPhoneService.EVENT_ANTIFAKE_BASESTATION_CHANGED /*122*/:
                        HwPhoneService.this.log("EVENT_ANTIFAKE_BASESTATION_CHANGED");
                        HwPhoneService.this.handleAntiFakeBaseStation(msg);
                        break;
                    default:
                        switch (i) {
                            case 202:
                                HwPhoneService.this.log("EVENT_SET_LINENUM_DONE");
                                HwPhoneService.this.handleSetFunctionDone(msg);
                                break;
                            case 203:
                                HwPhoneService.this.log("CMD_SET_DEEP_NO_DISTURB");
                                HwPhoneService.this.handleCmdSetDeepNoDisturb(msg);
                                break;
                            case 204:
                                HwPhoneService.this.log("EVENT_SET_DEEP_NO_DISTURB_DONE");
                                HwPhoneService.this.handleSetDeepNoDisturbDone(msg);
                                break;
                            case 205:
                                HwPhoneService.this.log("CMD_GET_CSCON_ID");
                                HwPhoneService.this.handleGetCscon(msg);
                                break;
                            case 206:
                                HwPhoneService hwPhoneService = HwPhoneService.this;
                                hwPhoneService.log("EVENT_CMD_GET_CSCON_ID_DONE msg.obj" + msg.obj);
                                HwPhoneService.this.handleGetCsconDone(msg);
                                break;
                            case 207:
                                Rlog.d(HwPhoneService.LOG_TAG, "CMD_SET_UL_FREQ_BANDWIDTH_RPT received ");
                                HwPhoneService.this.handleCmdSetUplinkFreqBandwidthReportState(msg);
                                break;
                            case HwPhoneService.EVENT_SET_UL_FREQ_BANDWIDTH_RPT_DONE /*208*/:
                                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_SET_UL_FREQ_BANDWIDTH_RPT_DONE received ");
                                HwPhoneService.this.handleSetUplinkFreqBandwidthDone(msg);
                                break;
                            case HwPhoneService.EVENT_UL_FREQ_BANDWIDTH_RPT /*209*/:
                                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_UL_FREQ_BANDWIDTH_RPT received ");
                                HwPhoneService.this.handleUplinkFreqBandwidthRpt(msg);
                                break;
                            default:
                                switch (i) {
                                    case HwPhoneService.EVENT_QUERY_CARD_TYPE_DONE /*506*/:
                                        Rlog.d(HwPhoneService.LOG_TAG, "Received EVENT_QUERY_CARD_TYPE_DONE on index " + index);
                                        if (ar != null && ar.exception == null) {
                                            if (!(ar == null || ar.result == null || !(ar.result instanceof int[]))) {
                                                HwPhoneService.this.mCardTypes[index.intValue()] = ((int[]) ar.result)[0];
                                                HwPhoneService.this.saveCardTypeProperties(((int[]) ar.result)[0], index.intValue());
                                                break;
                                            }
                                        } else {
                                            Rlog.d(HwPhoneService.LOG_TAG, "Received EVENT_QUERY_CARD_TYPE_DONE got exception");
                                            break;
                                        }
                                        break;
                                    case HwPhoneService.EVENT_ICC_GET_ATR_DONE /*507*/:
                                        Rlog.d(HwPhoneService.LOG_TAG, "Received EVENT_ICC_GET_ATR_DONE on index " + index);
                                        if (ar != null && ar.exception == null) {
                                            HwPhoneService.this.handleIccATR((String) ar.result, index);
                                            break;
                                        }
                                    default:
                                        switch (i) {
                                            case HwPhoneService.EVENT_GET_CARD_TRAY_INFO_DONE /*601*/:
                                                HwPhoneService.this.log("EVENT_GET_CARD_TRAY_INFO_DONE");
                                                HwPhoneService.this.handleGetCardTrayInfoDone(msg);
                                                break;
                                            case HwPhoneService.EVENT_SET_TEMP_CTRL_DONE /*602*/:
                                                HwPhoneService hwPhoneService2 = HwPhoneService.this;
                                                hwPhoneService2.log("EVENT_SET_TEMP_CTRL_DONE msg.obj" + msg.obj);
                                                HwPhoneService.this.handleSetTempCtrlDone(msg);
                                                break;
                                            default:
                                                HwPhoneService hwPhoneService3 = HwPhoneService.this;
                                                hwPhoneService3.log("MainHandler unhandled message: " + msg.what);
                                                break;
                                        }
                                }
                                break;
                        }
                }
            } else {
                HwPhoneService.this.log("EVENT_SMART_CARD_TOAST");
                Context access$3400 = HwPhoneService.this.mContext;
                Context access$34002 = HwPhoneService.this.mContext;
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(HwPhoneService.this.getDefault4GSlotId() == 1 ? 1 : 2);
                Toast.makeText(access$3400, access$34002.getString(33686234, objArr), 1).show();
            }
        }

        private void handlePinResult(Message msg) {
            if (((AsyncResult) msg.obj).exception != null) {
                Rlog.d(HwPhoneService.LOG_TAG, "set fail.");
                if (msg.what == HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE) {
                    boolean unused = HwPhoneService.this.setResultForPinLock = false;
                } else if (msg.what == HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE) {
                    boolean unused2 = HwPhoneService.this.setResultForChangePin = false;
                }
            } else {
                Rlog.d(HwPhoneService.LOG_TAG, "set success.");
                if (msg.what == HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE) {
                    boolean unused3 = HwPhoneService.this.setResultForPinLock = true;
                } else if (msg.what == HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE) {
                    boolean unused4 = HwPhoneService.this.setResultForChangePin = true;
                }
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "[enter]handleGetPreferredNetworkTypeResponse");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                int type = ((int[]) ar.result)[0];
                Rlog.d(HwPhoneService.LOG_TAG, "getPreferredNetworkType is " + type);
                return;
            }
            Rlog.d(HwPhoneService.LOG_TAG, "getPreferredNetworkType exception=" + ar.exception);
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "[enter]handleSetPreferredNetworkTypeResponse");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null) {
                Rlog.d(HwPhoneService.LOG_TAG, "setPreferredNetworkType ar == null");
            } else if (ar.exception != null) {
                Rlog.d(HwPhoneService.LOG_TAG, "setPreferredNetworkType exception=" + ar.exception);
                HwPhoneService.this.mPhone.getPreferredNetworkType(obtainMessage(3));
            } else {
                int setPrefMode = ((Integer) ar.userObj).intValue();
                if (HwPhoneService.this.getCurrentNetworkTypeFromDB() != setPrefMode) {
                    HwPhoneService.this.saveNetworkTypeToDB(setPrefMode);
                }
            }
        }

        private void hanleUiccAuthDone(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            MainThreadRequest request = (MainThreadRequest) ar.userObj;
            HwPhoneService.loge("EVENT_UICC_AUTH_DONE");
            request.result = new UiccAuthResponse();
            if (ar.exception == null && ar.result != null) {
                request.result = ar.result;
            } else if (ar.result == null) {
                HwPhoneService.loge("UiccAuthReq: Empty response");
            } else if (ar.exception instanceof CommandException) {
                HwPhoneService.loge("UiccAuthReq: CommandException: " + ar.exception);
            } else {
                HwPhoneService.loge("UiccAuthReq: Unknown exception");
            }
            synchronized (request) {
                request.notifyAll();
            }
        }

        private void handleImsGetDomainDone(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "EVENT_IMS_GET_DOMAIN_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            MainThreadRequest request = (MainThreadRequest) ar.userObj;
            if (ar.exception == null && ar.result != null) {
                request.result = ar.result;
            } else if (ar.result == null) {
                request.result = new int[]{2};
                HwPhoneService.loge("getImsDomain: Empty response,return 2");
            } else if (ar.exception instanceof CommandException) {
                request.result = new int[]{2};
                HwPhoneService.loge("getImsDomain: CommandException:return 2 " + ar.exception);
            } else {
                request.result = new int[]{2};
                HwPhoneService.loge("getImsDomain: Unknown exception,return 2");
            }
            synchronized (request) {
                request.notifyAll();
            }
        }

        private void handleEncryptCallInfoDone(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "EVENT_ENCRYPT_CALL_INFO_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            MainThreadRequest request = (MainThreadRequest) ar.userObj;
            if (ar.exception == null) {
                if (ar.result == null || ((byte[]) ar.result).length <= 0) {
                    request.result = new byte[]{1};
                    Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo success,return 1");
                } else {
                    request.result = ar.result;
                    Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo success,return ar.result");
                }
            } else if (ar.exception instanceof CommandException) {
                request.result = new byte[]{-1};
                HwPhoneService.loge("requestForECInfo: CommandException:return -1 " + ar.exception);
            } else {
                request.result = new byte[]{-2};
                HwPhoneService.loge("requestForECInfo: Unknown exception,return -2");
            }
            synchronized (request) {
                request.notifyAll();
            }
        }

        private void handleNotifyCmodemStatus(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar != null) {
                IPhoneCallback callback = (IPhoneCallback) ar.userObj;
                if (callback != null) {
                    int result = 1;
                    if (ar.exception != null) {
                        result = -1;
                    }
                    try {
                        Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS onCallback1");
                        callback.onCallback1(result);
                    } catch (RemoteException ex) {
                        Rlog.e(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS onCallback1 RemoteException:" + ex);
                    }
                }
            }
        }

        private void handleNotifyDeviceState(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_DEVICE_STATE");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null) {
                HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, ar is null.");
                return;
            }
            if (ar.exception == null) {
                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_DEVICE_STATE success.");
            } else if (ar.exception instanceof CommandException) {
                HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, " + ar.exception);
            } else {
                HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, unknown exception.");
            }
        }

        private void handleBasicCommParaUpgradeDone(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "EVENT_BASIC_COMM_PARA_UPGRADE_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                Rlog.e(HwPhoneService.LOG_TAG, "Error in BasicImsNVPara Upgrade:" + ar.exception);
                return;
            }
            int[] resultUpgrade = (int[]) ar.result;
            if (resultUpgrade.length != 0) {
                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_BASIC_COMM_PARA_UPGRADE_DONE: result=" + resultUpgrade[0]);
            } else {
                Rlog.e(HwPhoneService.LOG_TAG, "EVENT_BASIC_COMM_PARA_UPGRADE_DONE: resultUpgrade.length = 0");
                resultUpgrade[0] = -1;
            }
            EmcomManager.getInstance().responseForParaUpgrade(1, 1, resultUpgrade[0]);
            Rlog.d(HwPhoneService.LOG_TAG, "responseForParaUpgrade()");
        }

        private void handleCellularCloudParaUpgradeDone(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                Rlog.e(HwPhoneService.LOG_TAG, "Error in Cellular Cloud Para Upgrade:" + ar.exception);
                return;
            }
            int[] phoneResult = (int[]) ar.result;
            if (phoneResult.length != 0) {
                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE: phoneResult=" + phoneResult[0]);
            } else {
                Rlog.e(HwPhoneService.LOG_TAG, "EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE: phoneResult.length = 0");
                phoneResult[0] = -1;
            }
            EmcomManager.getInstance().responseForParaUpgrade(2, 1, phoneResult[0]);
            Rlog.d(HwPhoneService.LOG_TAG, "responseForParaUpgrade()");
        }

        private void handleEncryptCallInfo(Message msg) {
            MainThreadRequest request = (MainThreadRequest) msg.obj;
            EncryptCallPara ecPara = (EncryptCallPara) request.argument;
            Message onCompleted = obtainMessage(HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE, request);
            HwPhone sPhone = ecPara.phone;
            if (sPhone != null) {
                sPhone.requestForECInfo(onCompleted, ecPara.event, ecPara.buf);
            }
        }

        private void handleQueryEncryptFeature(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "radio available, query encrypt call feature");
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                for (HwPhone phone : HwPhoneService.this.mPhones) {
                    HwPhoneService.this.handleEventQueryEncryptCall(phone);
                }
                return;
            }
            HwPhoneService.this.handleEventQueryEncryptCall(HwPhoneService.this.mPhone);
        }

        private void handleUsbIetherState(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "EVENT_USB_IETHER_STATE");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null || ar.exception == null) {
                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_USB_TETHER_STATE is success.");
            } else {
                HwPhoneService.loge("EVENT_USB_TETHER_STATE is failed.");
            }
        }
    }

    private static final class MainThreadRequest {
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

    private class PhoneServiceReceiver extends BroadcastReceiver {
        public PhoneServiceReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.RADIO_TECHNOLOGY");
            HwPhoneService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            Rlog.d(HwPhoneService.LOG_TAG, "radio tech changed, query encrypt call feature");
            if (intent != null && "android.intent.action.RADIO_TECHNOLOGY".equals(intent.getAction())) {
                int unused = HwPhoneService.queryCount = 0;
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    for (HwPhone phone : HwPhoneService.this.mPhones) {
                        HwPhoneService.this.handleEventQueryEncryptCall(phone);
                    }
                    return;
                }
                HwPhoneService.this.handleEventQueryEncryptCall(HwPhoneService.this.mPhone);
            }
        }
    }

    private final class PhoneStateHandler extends Handler {
        PhoneStateHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 4) {
                switch (i) {
                    case 1:
                        handleRadioAvailable(msg);
                        return;
                    case 2:
                        handleRadioNotAvailable(msg);
                        return;
                    default:
                        return;
                }
            } else {
                handleCommonImsaToMapconInfo(msg);
            }
        }

        private void handleRadioAvailable(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            int phoneId = HwPhoneService.this.getPhoneId(msg).intValue();
            if (ar.exception == null) {
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 1, 0, null);
                return;
            }
            HwPhoneService.loge("radio available exception: " + ar.exception);
        }

        private void handleRadioNotAvailable(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            int phoneId = HwPhoneService.this.getPhoneId(msg).intValue();
            if (ar.exception == null) {
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 2, 0, null);
                return;
            }
            HwPhoneService.loge("radio not available exception: " + ar.exception);
        }

        private void handleCommonImsaToMapconInfo(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            int phoneId = HwPhoneService.this.getPhoneId(msg).intValue();
            if (ar.exception == null) {
                Bundle bundle = new Bundle();
                bundle.putByteArray("imsa2mapcon_msg", (byte[]) ar.result);
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 4, 0, bundle);
                return;
            }
            HwPhoneService.loge("imsa to mapcon info exception: " + ar.exception);
        }
    }

    private static class Record {
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

    private static final class UiccAuthPara {
        byte[] auth;
        int auth_type;
        byte[] rand;

        public UiccAuthPara(int auth_type2, byte[] rand2, byte[] auth2) {
            this.auth_type = auth_type2;
            this.rand = rand2;
            this.auth = auth2;
        }
    }

    public HwPhoneService() {
        this.mMessageThread.start();
        this.mMainHandler = new MainHandler(this.mMessageThread.getLooper());
        this.mPhoneStateHandler = new PhoneStateHandler(this.mMessageThread.getLooper());
        this.mHwCustPhoneService = (HwCustPhoneService) HwCustUtils.createObj(HwCustPhoneService.class, new Object[]{this, this.mMessageThread.getLooper()});
    }

    public void setPhone(Phone phone2, Context context) {
        this.mPhone = new HwPhone(phone2);
        this.mContext = context;
        if (this.mHwCustPhoneService != null && this.mHwCustPhoneService.isDisable2GServiceCapabilityEnabled()) {
            this.mHwCustPhoneService.setPhone(this.mPhone, this.mContext);
        }
        initService();
    }

    public void setPhone(Phone[] phones, Context context) {
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        this.mPhones = new HwPhone[numPhones];
        for (int i = 0; i < numPhones; i++) {
            Rlog.d(LOG_TAG, "Creating HwPhone sub = " + i);
            this.mPhones[i] = new HwPhone(phones[i]);
            this.mCardTypes[i] = -1;
        }
        this.mPhone = this.mPhones[0];
        this.mContext = context;
        if (this.mHwCustPhoneService != null && this.mHwCustPhoneService.isDisable2GServiceCapabilityEnabled()) {
            this.mHwCustPhoneService.setPhone(this.mPhone, this.mContext);
        }
        log("setPhone mPhones = " + this.mPhones);
        initService();
        this.mInCallDataStateMachine = new InCallDataStateMachine(context, phones);
        this.mInCallDataStateMachine.start();
    }

    private static void saveInstance(HwPhoneService service) {
        sInstance = service;
    }

    public static HwPhoneService getInstance() {
        return sInstance;
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [com.android.internal.telephony.HwPhoneService, android.os.IBinder] */
    private void initService() {
        Rlog.d(LOG_TAG, "initService()");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        ServiceManager.addService("phone_huawei", this);
        saveInstance(this);
        initPrefNetworkTypeChecker();
        if (!IS_MTK_PLATFORM) {
            registerForRadioOnInner();
            if (this.mPhoneServiceReceiver == null) {
                this.mPhoneServiceReceiver = new PhoneServiceReceiver();
            }
        }
        registerMDMSmsReceiver();
        registerSetRadioCapDoneReceiver();
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            registerForIccStatusChanged();
            registerIntelligenceCardReceiver();
            initCommBoosterManager();
            this.mIntelligenceCardObserver = new IntelligenceCardObserver(new Handler());
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(INTELLIGENCE_CARD_SETTING_DB), true, this.mIntelligenceCardObserver);
        }
    }

    private void registerForIccStatusChanged() {
        Rlog.d(LOG_TAG, "registerForIccStatusChanged");
        if (this.mPhones == null) {
            Rlog.d(LOG_TAG, "register failed, mphones is null");
            return;
        }
        for (int i = 0; i < SIM_NUM; i++) {
            Integer index = Integer.valueOf(i);
            this.mPhones[i].getPhone().mCi.registerForIccStatusChanged(this.mMainHandler, 54, index);
            this.mPhones[i].getPhone().mCi.registerForAvailable(this.mMainHandler, 54, index);
        }
    }

    /* access modifiers changed from: private */
    public void onIccStatusChanged(Integer index) {
        this.mPhones[index.intValue()].getPhone().mCi.queryCardType(this.mMainHandler.obtainMessage(EVENT_QUERY_CARD_TYPE_DONE, index));
        this.mPhones[index.intValue()].getPhone().mCi.iccGetATR(this.mMainHandler.obtainMessage(EVENT_ICC_GET_ATR_DONE, index));
    }

    /* access modifiers changed from: private */
    public void saveCardTypeProperties(int cardTypeResult, int index) {
        int cardType = -1;
        int uiccOrIcc = (cardTypeResult & 240) >> 4;
        int appType = cardTypeResult & 15;
        switch (appType) {
            case 1:
                if (uiccOrIcc != 2) {
                    if (uiccOrIcc == 1) {
                        cardType = 10;
                        break;
                    }
                } else {
                    cardType = 20;
                    break;
                }
                break;
            case 2:
                cardType = 30;
                break;
            case 3:
                if (uiccOrIcc != 2) {
                    if (uiccOrIcc == 1) {
                        cardType = 41;
                        break;
                    }
                } else {
                    cardType = 43;
                    break;
                }
                break;
        }
        Rlog.d(LOG_TAG, "uiccOrIcc :  " + uiccOrIcc + ", appType : " + appType + ", cardType : " + cardType);
        if (index == 0) {
            SystemProperties.set(HwFullNetworkConstants.CARD_TYPE_SIM1, String.valueOf(cardType));
        } else {
            SystemProperties.set(HwFullNetworkConstants.CARD_TYPE_SIM2, String.valueOf(cardType));
        }
    }

    /* access modifiers changed from: private */
    public void handleIccATR(String strATR, Integer index) {
        Rlog.d(LOG_TAG, "handleIccATR, ATR: [" + strATR + "], index:[" + index + "]");
        if (strATR == null || strATR.isEmpty()) {
            strATR = "null";
        }
        if (strATR.length() > 66) {
            Rlog.d(LOG_TAG, "strATR.length() greater than PROP_VALUE_MAX");
            strATR = strATR.substring(0, 66);
        }
        if (index.intValue() == 0) {
            SystemProperties.set("gsm.sim.hw_atr", strATR);
        } else {
            SystemProperties.set("gsm.sim.hw_atr1", strATR);
        }
    }

    /* access modifiers changed from: private */
    public Integer getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return (Integer) ar.userObj;
    }

    /* access modifiers changed from: private */
    public void log(String msg) {
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    private void logForOemHook(String msg) {
        Rlog.d("HwPhoneService_OEMHOOK", msg);
    }

    private void logForSar(String msg) {
        Rlog.d("HwPhoneService_SAR", msg);
    }

    public String getDemoString() {
        enforceReadPermission();
        return "" + this.mPhone + this.mContext;
    }

    public String getMeidForSubscriber(int slot) {
        if (!canReadPhoneState(slot, "getMeid") || slot < 0 || slot >= this.mPhones.length) {
            return null;
        }
        if (-1 != SystemProperties.getInt("persist.radio.stack_id_0", -1)) {
            slot = SystemProperties.getInt("persist.radio.stack_id_0", -1);
            log("QC after switch slot = " + slot);
        }
        return this.mPhones[slot].getMeid();
    }

    public String getPesnForSubscriber(int slot) {
        enforceReadPermission();
        if (slot < 0 || slot >= this.mPhones.length) {
            return null;
        }
        if (-1 != SystemProperties.getInt("persist.radio.stack_id_0", -1)) {
            slot = SystemProperties.getInt("persist.radio.stack_id_0", -1);
            log("QC after switch slot = " + slot);
        }
        return this.mPhones[slot].getPesn();
    }

    public int getSubState(int subId) {
        enforceReadPermission();
        if (SubscriptionController.getInstance() != null) {
            return SubscriptionController.getInstance().getSubState(subId);
        }
        return 0;
    }

    public void setUserPrefDataSlotId(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (HwSubscriptionManager.getInstance() != null) {
            HwSubscriptionManager.getInstance().setUserPrefDataSlotId(slotId);
        } else {
            Rlog.e(LOG_TAG, "HwSubscriptionManager is null!!");
        }
    }

    public int getDataStateForSubscriber(int subId) {
        enforceReadPermission();
        return PhoneConstantConversions.convertDataState(this.mPhones[SubscriptionController.getInstance().getPhoneId(subId)].getDataConnectionState());
    }

    public String getNVESN() {
        enforceReadPermission();
        return this.mPhone.getNVESN();
    }

    public void closeRrc() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int dataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
        if (dataSubId >= 0 && dataSubId < this.mPhones.length) {
            this.mPhones[dataSubId].closeRrc();
        }
    }

    public boolean isCTCdmaCardInGsmMode() {
        enforceReadPermission();
        int i = 0;
        if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
            return false;
        }
        boolean noCdmaPhone = true;
        while (true) {
            if (i >= this.mPhones.length) {
                break;
            } else if (this.mPhones[i].isCDMAPhone()) {
                noCdmaPhone = false;
                break;
            } else {
                i++;
            }
        }
        return noCdmaPhone;
    }

    public boolean isLTESupported() {
        boolean result;
        int networkMode = RILConstants.PREFERRED_NETWORK_MODE;
        if (!(networkMode == 16 || networkMode == 18 || networkMode == 21 || networkMode == 52)) {
            switch (networkMode) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    break;
                default:
                    switch (networkMode) {
                        case 13:
                        case 14:
                            break;
                        default:
                            result = true;
                            break;
                    }
            }
        }
        result = false;
        Rlog.i(LOG_TAG, "isLTESupported " + result);
        return result;
    }

    public void setDefaultMobileEnable(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!(Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data_always_on", 0) != 0) || enabled) {
            int numPhones = TelephonyManager.getDefault().getPhoneCount();
            int defaultDataSub = SubscriptionController.getInstance().getDefaultDataSubId();
            for (int i = 0; i < numPhones; i++) {
                if (this.mPhones[i].getPhone().getSubId() == defaultDataSub) {
                    Rlog.d(LOG_TAG, "setDefaultMobileEnable true for phone " + i);
                    this.mPhones[i].setDefaultMobileEnable(enabled);
                }
            }
            Phone vsimPhone = HwVSimPhoneFactory.getVSimPhone();
            if (vsimPhone != null) {
                HwPhone hwVsimPhone = new HwPhone(vsimPhone);
                Rlog.d(LOG_TAG, "setDefaultMobileEnable to " + enabled + " for vsimPhone");
                hwVsimPhone.setDefaultMobileEnable(enabled);
            }
            return;
        }
        Rlog.d(LOG_TAG, "setDefaultMobileEnable: isDataAlwaysOn && !enabled, return.");
    }

    public void setDataEnabledWithoutPromp(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int phoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (phoneId >= 0 && phoneId < this.mPhones.length) {
            this.mPhones[phoneId].setDataEnabledWithoutPromp(enabled);
        }
    }

    public void setDataRoamingEnabledWithoutPromp(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int phoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (phoneId >= 0 && phoneId < this.mPhones.length) {
            this.mPhones[phoneId].setDataRoamingEnabledWithoutPromp(enabled);
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
        Rlog.d(LOG_TAG, "[enter]setPreferredNetworkType " + nwMode);
        if (!TelephonyManager.getDefault().isMultiSimEnabled() || HwFullNetworkManager.getInstance().getBalongSimSlot() != 1) {
            this.mPhone = this.mPhones[0];
        } else {
            this.mPhone = this.mPhones[1];
        }
        if (this.mPhone == null) {
            Rlog.e(LOG_TAG, "4G-Switch mPhone is null. return!");
        } else {
            this.mPhone.setPreferredNetworkType(nwMode, this.mMainHandler.obtainMessage(4, Integer.valueOf(nwMode)));
        }
    }

    public int getServiceAbilityForSubId(int subId, int type) {
        int ability;
        enforceReadPermission();
        if (!isValidSlotId(subId) || !isValidServiceAbilityType(type)) {
            return 0;
        }
        if (type != 1 || HwModemCapability.isCapabilitySupport(29)) {
            int curPrefMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, subId);
            if (type == 1) {
                ability = HwNetworkTypeUtils.isNrServiceOn(curPrefMode);
            } else {
                ability = HwNetworkTypeUtils.isLteServiceOn(curPrefMode);
            }
            log("getServiceAbilityForSubId, curPrefMode = " + curPrefMode + ", subId =" + subId + ", type =" + type + ", ability =" + ((int) ability));
            return ability;
        }
        log("Parameter is invalid for MODEM_CAP_SUPPORT_NR is false, will return.");
        return 0;
    }

    public void setServiceAbilityForSubId(int subId, int type, int ability) {
        HwPhone phone2;
        enforceModifyPermissionOrCarrierPrivilege();
        log("=4G-Switch= setServiceAbilityForSubId: type=" + type + ", ability=" + ability + ", subId=" + subId);
        if (isValidSlotId(subId) && isValidServiceAbilityType(type) && isValidServiceAbility(ability)) {
            if (type == 1 && !HwModemCapability.isCapabilitySupport(29)) {
                log("Parameter is invalid for MODEM_CAP_SUPPORT_NR is false, will return.");
            } else if (!TelephonyManager.getDefault().isMultiSimEnabled() || (!HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK && !HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING)) {
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    phone2 = this.mPhones[subId];
                    if (subId == getDefault4GSlotId()) {
                        this.mPhone = phone2;
                    }
                } else {
                    phone2 = this.mPhone;
                }
                if (phone2 == null) {
                    loge("4G-Switch phone is null. return!");
                    return;
                }
                int networkType = getNetworkType(ability, phone2, type);
                if (HwTelephonyFactory.getHwNetworkManager().isNetworkModeAsynchronized(phone2.getPhone())) {
                    HwTelephonyFactory.getHwNetworkManager().handle4GSwitcherForNoMdn(phone2.getPhone(), networkType);
                    sendLteServiceSwitchResult(subId, true);
                    return;
                }
                phone2.setPreferredNetworkType(networkType, this.mMainHandler.obtainMessage(5, networkType, subId));
                log("=4G-Switch= setPreferredNetworkType-> " + networkType);
            } else if (type == 1) {
                log("FullNetwork: Qcom donnot adapt to NR, will return.");
            } else {
                HwFullNetworkManager.getInstance().setLteServiceAbilityForQCOM(subId, ability, HwNetworkTypeUtils.getLteOnMappingMode());
            }
        }
    }

    private int selectNetworkType(int type, int ability, int nrOnNetworkType, int lteOnNetworkType, int lteOffNetworkType) {
        if (type == 0) {
            return ability == 1 ? lteOnNetworkType : lteOffNetworkType;
        }
        return ability == 1 ? nrOnNetworkType : lteOnNetworkType;
    }

    @Deprecated
    public void setLteServiceAbility(int ability) {
        setServiceAbilityForSubId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, 0, ability);
    }

    @Deprecated
    public void setLteServiceAbilityForSubId(int subId, int ability) {
        setServiceAbilityForSubId(subId, 0, ability);
    }

    private int getNetworkType(int ability, HwPhone phone2, int type) {
        int networkType;
        int i;
        int i2;
        if (HwNetworkTypeUtils.IS_MODEM_FULL_PREFMODE_SUPPORTED) {
            networkType = calculateNetworkType(ability);
        } else if (phone2.isCDMAPhone()) {
            networkType = selectNetworkType(type, ability, 64, 8, 4);
        } else if (IS_GSM_NONSUPPORT) {
            networkType = selectNetworkType(type, ability, 68, 12, 2);
        } else {
            networkType = selectNetworkType(type, ability, 65, 9, 3);
        }
        if (!HuaweiTelephonyConfigs.isHisiPlatform() && !TelephonyManager.getDefault().isMultiSimEnabled() && IS_FULL_NETWORK_SUPPORTED) {
            if (phone2.isCDMAPhone()) {
                if (ability == 1) {
                    i2 = 10;
                } else {
                    i2 = 7;
                }
                networkType = i2;
            } else {
                if (ability == 1) {
                    i = 20;
                } else {
                    i = 18;
                }
                networkType = i;
            }
        }
        if (type != 0 || this.mHwCustPhoneService == null || !this.mHwCustPhoneService.isDisable2GServiceCapabilityEnabled() || this.mHwCustPhoneService.get2GServiceAbility() != 0) {
            return networkType;
        }
        return this.mHwCustPhoneService.getNetworkTypeBaseOnDisabled2G(networkType);
    }

    private int calculateNetworkType(int ability) {
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
        return ability == 1 ? HwNetworkTypeUtils.getLteOnMappingMode() : HwNetworkTypeUtils.getLteOffMappingMode();
    }

    /* access modifiers changed from: private */
    public int getCurrentNetworkTypeFromDB() {
        return HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0);
    }

    /* access modifiers changed from: private */
    public void saveNetworkTypeToDB(int setPrefMode) {
        HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, setPrefMode);
    }

    /* access modifiers changed from: private */
    public void handleSetLteSwitchDone(Message msg) {
        log("=4G-Switch= in handleSetLteSwitchDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null) {
            loge("=4G-Switch= ar is null!");
            return;
        }
        int setPrefMode = msg.arg1;
        int subId = msg.arg2;
        if (ar.exception != null) {
            loge("=4G-Switch= " + ar.exception);
            sendLteServiceSwitchResult(subId, false);
            return;
        }
        int curPrefMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, subId);
        log("=4G-Switch= subId:" + subId + " curPrefMode in db:" + curPrefMode + " setPrefMode:" + setPrefMode);
        if (curPrefMode != setPrefMode) {
            HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, subId, setPrefMode);
        }
        sendLteServiceSwitchResult(subId, true);
    }

    private void sendLteServiceSwitchResult(int subId, boolean result) {
        if (this.mContext == null) {
            loge("=4G-Switch= mContext is null. return!");
            return;
        }
        Intent intent = new Intent("com.huawei.telephony.PREF_4G_SWITCH_DONE");
        intent.putExtra("subscription", subId);
        intent.putExtra("setting_result", result);
        this.mContext.sendOrderedBroadcast(intent, "android.permission.READ_PHONE_STATE");
        log("=4G-Switch= result is " + result + ". broadcast PREFERRED_4G_SWITCH_DONE");
    }

    public int getLteServiceAbility() {
        enforceReadPermission();
        return getServiceAbilityForSubId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, 0);
    }

    @Deprecated
    public int getLteServiceAbilityForSubId(int subId) {
        return getServiceAbilityForSubId(subId, 0);
    }

    public int get2GServiceAbility() {
        if (this.mHwCustPhoneService == null || !this.mHwCustPhoneService.isDisable2GServiceCapabilityEnabled()) {
            return 0;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        return this.mHwCustPhoneService.get2GServiceAbility();
    }

    public void set2GServiceAbility(int ability) {
        if (this.mHwCustPhoneService != null && this.mHwCustPhoneService.isDisable2GServiceCapabilityEnabled()) {
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
        if (this.mPhone == null) {
            log("hasCarrierPrivileges: mPhone is null");
            return -1;
        }
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhone().getPhoneId());
        if (card != null) {
            return card.getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager());
        }
        loge("hasCarrierPrivileges: No UICC");
        return -1;
    }

    /* access modifiers changed from: private */
    public static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public boolean isSubDeactivedByPowerOff(long sub) {
        enforceReadPermission();
        Rlog.d(LOG_TAG, "isSubDeactivedByPowerOff: in HuaweiPhoneService");
        SubscriptionController sc = SubscriptionController.getInstance();
        if (TelephonyManager.getDefault().getSimState(sc.getSlotIndex((int) sub)) == 5 && sc.getSubState((int) sub) == 0) {
            return true;
        }
        return false;
    }

    public boolean isNeedToRadioPowerOn(long sub) {
        enforceReadPermission();
        Rlog.d(LOG_TAG, "isNeedToRadioPowerOn: in HuaweiPhoneService");
        if (HuaweiTelephonyConfigs.isQcomPlatform() || (TelephonyManager.MultiSimVariants.DSDS != TelephonyManager.getDefault().getMultiSimConfiguration() && !SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false))) {
            Rlog.d(LOG_TAG, "isNeedToRadioPowerOn: hisi dsds not in");
            int phoneId = SubscriptionController.getInstance().getPhoneId((int) sub);
            if (PhoneFactory.getPhone(phoneId) != null && PhoneFactory.getPhone(phoneId).getServiceState().getState() == 3) {
                return true;
            }
        }
        if (!isSubDeactivedByPowerOff((long) ((int) sub))) {
            return true;
        }
        SubscriptionController.getInstance().activateSubId((int) sub);
        return false;
    }

    private void enforceReadPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
    }

    public void updateCrurrentPhone(int lteSlot) {
        Rlog.d(LOG_TAG, "updateCrurrentPhone with lteSlot = " + lteSlot);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (TelephonyManager.getDefault().getPhoneCount() > lteSlot) {
            this.mPhone = this.mPhones[lteSlot];
        } else {
            Rlog.e(LOG_TAG, "Invalid slot ID");
        }
    }

    public void setDefaultDataSlotId(int slotId) {
        Rlog.d(LOG_TAG, "setDefaultDataSlotId: slotId = " + slotId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(slotId)) {
            Rlog.d(LOG_TAG, "setDefaultDataSlotId: invalid slotId!!!");
            return;
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, slotId);
        if (SubscriptionController.getInstance() != null) {
            SubscriptionController.getInstance().setDefaultDataSubId(SubscriptionController.getInstance().getSubId(slotId)[0]);
        } else {
            Rlog.d(LOG_TAG, "SubscriptionController.getInstance()! null");
        }
        Rlog.d(LOG_TAG, "setDefaultDataSlotId done");
    }

    public int getDefault4GSlotId() {
        try {
            return Settings.System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (Settings.SettingNotFoundException e) {
            Rlog.d(LOG_TAG, "Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            return 0;
        }
    }

    public void setDefault4GSlotId(int slotId, Message response) {
        int uid = Binder.getCallingUid();
        if (!HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE || uid == 1000 || uid == 1001 || uid == 0) {
            enforceModifyPermissionOrCarrierPrivilege();
            log("in setDefault4GSlotId for slotId: " + slotId);
            Message msg = this.mMainHandler.obtainMessage(200);
            msg.obj = response;
            HwFullNetworkManager.getInstance().setMainSlot(slotId, msg);
            return;
        }
        loge("setDefault4GSlotId: Disallowed call for uid " + uid);
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        enforceReadPermission();
        log("in isSetDefault4GSlotIdEnabled.");
        if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate() || !HwVSimUtils.isAllowALSwitch()) {
            log("vsim is working, so isSetDefault4GSlotIdEnabled return false");
            return false;
        } else if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return false;
        } else {
            SubscriptionController sc = SubscriptionController.getInstance();
            int[] sub1 = sc.getSubId(0);
            int[] sub2 = sc.getSubId(1);
            if (TelephonyManager.getDefault().getSimState(0) == 5 && sc.getSubState(sub1[0]) == 0 && TelephonyManager.getDefault().getSimState(1) == 5 && sc.getSubState(sub2[0]) == 0) {
                return false;
            }
            if (!HwFullNetworkConfig.IS_HISI_DSDX || ((TelephonyManager.getDefault().getSimState(0) != 5 || sc.getSubState(sub1[0]) != 0) && (TelephonyManager.getDefault().getSimState(1) != 5 || sc.getSubState(sub2[0]) != 0))) {
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
        int subId = SubscriptionController.getInstance().getPreferredDataSubscription();
        log("getPreferredDataSubscription return subId = " + subId);
        return subId;
    }

    public int getOnDemandDataSubId() {
        enforceReadPermission();
        int subId = SubscriptionController.getInstance().getOnDemandDataSubId();
        log("getOnDemandDataSubId return subId = " + subId);
        return subId;
    }

    public String getCdmaGsmImsi() {
        Rlog.d(LOG_TAG, "getCdmaGsmImsi: in HWPhoneService");
        enforceReadPermission();
        for (HwPhone hp : this.mPhones) {
            if (hp.getHwPhoneType() == 2) {
                return hp.getCdmaGsmImsi();
            }
        }
        return null;
    }

    public String getCdmaGsmImsiForSubId(int subId) {
        enforceReadPermission();
        Rlog.d(LOG_TAG, "getCdmaGsmImsi: in HWPhoneService subId:" + subId);
        if (!isValidSlotId(subId)) {
            Rlog.d(LOG_TAG, "subId is not avaible!");
            return null;
        }
        Phone phone2 = PhoneFactory.getPhone(subId);
        if (phone2 == null) {
            return null;
        }
        if (phone2.getCdmaGsmImsi() != null || !isCtSimCard(subId)) {
            return phone2.getCdmaGsmImsi();
        }
        Rlog.d(LOG_TAG, "getCdmaGsmImsi is null");
        IccRecords iccRecords = UiccController.getInstance().getIccRecords(subId, 2);
        if (iccRecords != null) {
            return iccRecords.getCdmaGsmImsi();
        }
        return null;
    }

    public int getUiccCardType(int slotId) {
        Rlog.d(LOG_TAG, "getUiccCardType: in HwPhoneService");
        enforceReadPermission();
        if (slotId < 0 || slotId >= this.mPhones.length) {
            return this.mPhone.getUiccCardType();
        }
        return this.mPhones[slotId].getUiccCardType();
    }

    public boolean isCardUimLocked(int slotId) {
        log("isCardUimLocked for slotId " + slotId);
        enforceReadPermission();
        UiccCard card = UiccController.getInstance().getUiccCard(slotId);
        if (card != null) {
            return card.isCardUimLocked();
        }
        loge("isCardUimLocked: No UICC for slotId" + slotId);
        return false;
    }

    public int getSpecCardType(int slotId) {
        log("getSpecCardType for slotId " + slotId);
        enforceReadPermission();
        if (!HuaweiTelephonyConfigs.isHisiPlatform() || !isValidSlotId(slotId)) {
            return -1;
        }
        return this.mCardTypes[slotId];
    }

    public boolean isRadioOn(int slot) {
        log("isRadioOn for slotId " + slot);
        enforceReadPermission();
        if (slot < 0 || slot >= this.mPhones.length) {
            return false;
        }
        return this.mPhones[slot].getPhone().isRadioOn();
    }

    public Bundle getCellLocation(int slotId) {
        Rlog.d(LOG_TAG, "getCellLocation: in HwPhoneService");
        int uid = Binder.getCallingUid();
        if (uid == 1000 || uid == 1001) {
            enforceCellLocationPermission("getCellLocation");
            Bundle data = new Bundle();
            WorkSource workSource = new WorkSource(uid, this.mContext.getPackageManager().getNameForUid(uid));
            if (slotId >= 0 && slotId < this.mPhones.length) {
                CellLocation cellLoc = this.mPhones[slotId].getCellLocation(workSource);
                if (cellLoc != null) {
                    cellLoc.fillInNotifierBundle(data);
                }
            } else if (!sIsPlatformSupportVSim || slotId != 2) {
                CellLocation cellLoc2 = this.mPhone.getCellLocation(workSource);
                if (cellLoc2 != null) {
                    cellLoc2.fillInNotifierBundle(data);
                }
            } else {
                CellLocation cellLoc3 = getVSimPhone().getCellLocation(workSource);
                if (cellLoc3 != null) {
                    cellLoc3.fillInNotifierBundle(data);
                }
            }
            return data;
        }
        Rlog.e(LOG_TAG, "getCellLocation not allowed for third-party apps");
        return null;
    }

    private void enforceCellLocationPermission(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", message);
    }

    public String getCdmaMlplVersion() {
        Rlog.d(LOG_TAG, "getCdmaMlplVersion: in HwPhoneService");
        enforceReadPermission();
        for (HwPhone hp : this.mPhones) {
            if (hp.getHwPhoneType() == 2) {
                return hp.getCdmaMlplVersion();
            }
        }
        return null;
    }

    public String getCdmaMsplVersion() {
        Rlog.d(LOG_TAG, "getCdmaMsplVersion: in HwPhoneService");
        enforceReadPermission();
        for (HwPhone hp : this.mPhones) {
            if (hp.getHwPhoneType() == 2) {
                return hp.getCdmaMsplVersion();
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
            Rlog.e(LOG_TAG, packageName + " not found.");
        }
    }

    private boolean canReadPhoneState(int subId, String message) {
        String callingPackage = null;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            String[] callingPackageName = pm.getPackagesForUid(Binder.getCallingUid());
            if (callingPackageName != null) {
                callingPackage = callingPackageName[0];
            }
        }
        try {
            return TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, message);
        } catch (SecurityException phoneStateException) {
            if (isSystemApp(callingPackage)) {
                Rlog.d(LOG_TAG, callingPackage + " allowed.");
                return true;
            }
            throw phoneStateException;
        }
    }

    public String getUniqueDeviceId(int scope) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.PHONEINTERFACE_GETDEVICEID);
        }
        isReadPhoneNumberBlocked();
        if (!canReadPhoneState(0, "getDeviceId")) {
            Rlog.e(LOG_TAG, "getUniqueDeviceId can't read phone state.");
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
        if (TextUtils.isEmpty(deviceId) && isWifiOnly(this.mContext)) {
            Rlog.d(LOG_TAG, "Current is wifi-only version, return SN number as DeviceId");
            deviceId = Build.SERIAL;
        }
        return deviceId;
    }

    private boolean isReadPhoneNumberBlocked() {
        try {
            return ProxyController.getInstance().getPhoneSubInfoController().isReadPhoneNumberBlocked();
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "isReadPhoneNumberBlocked, exception=" + e);
            return false;
        }
    }

    private void saveDeviceIdToSP(String deviceId, String sharedPref) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        try {
            deviceId = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, deviceId);
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "HwAESCryptoUtil encrypt excepiton");
        }
        editor.putString(sharedPref, deviceId);
        editor.commit();
    }

    private String getDeviceIdFromSP(String sharedPref) {
        String deviceId = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(sharedPref, null);
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkConstants.MASTER_PASSWORD, deviceId);
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "HwAESCryptoUtil decrypt excepiton");
            return deviceId;
        }
    }

    private String readDeviceIdFromLL(int scope) {
        int phoneId = 0;
        if (HwModemCapability.isCapabilitySupport(15) && TelephonyManager.getDefault().isMultiSimEnabled() && SystemProperties.getBoolean("persist.sys.dualcards", false)) {
            if (getWaitingSwitchBalongSlot()) {
                Rlog.d(LOG_TAG, "readDeviceIdFromLL getWaitingSwitchBalongSlot");
                return null;
            }
            phoneId = HwTelephonyManagerInner.getDefault().isImeiBindSlotSupported() ? 0 : HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        }
        Rlog.d(LOG_TAG, "readDeviceIdFromLL: phoneId=" + phoneId);
        if (this.mPhones == null || this.mPhones[phoneId] == null) {
            return null;
        }
        if (1 == scope) {
            return this.mPhones[phoneId].getImei();
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

    private Phone getVSimPhone() {
        return HwVSimPhoneFactory.getVSimPhone();
    }

    public boolean getWaitingSwitchBalongSlot() {
        log("getWaitingSwitchBalongSlot start");
        return HwFullNetworkManager.getInstance().getWaitingSwitchBalongSlot();
    }

    public boolean setISMCOEX(String setISMCoex) {
        enforceModifyPermissionOrCarrierPrivilege();
        int phoneId = 0;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && SystemProperties.getBoolean("persist.sys.dualcards", false)) {
            phoneId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        }
        if (this.mPhones == null || this.mPhones[phoneId] == null) {
            loge("mPhones is invalid!");
            return false;
        }
        log("setISMCoex =" + setISMCoex);
        return this.mPhones[phoneId].setISMCOEX(setISMCoex);
    }

    public boolean isDomesticCard(int slotId) {
        log("isDomesticCard start");
        enforceReadPermission();
        return true;
    }

    public boolean setWifiTxPower(int power) {
        Rlog.d(LOG_TAG, "setWifiTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Rlog.d(LOG_TAG, "setWifiTxPower: end=" + power);
        return true;
    }

    public boolean setCellTxPower(int power) {
        Rlog.d(LOG_TAG, "setCellTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        getCommandsInterface().setPowerGrade(power, null);
        Rlog.d(LOG_TAG, "setCellTxPower: end=" + power);
        return true;
    }

    private CommandsInterface getCommandsInterface() {
        return PhoneFactory.getDefaultPhone().mCi;
    }

    public String[] queryServiceCellBand() {
        Rlog.d(LOG_TAG, "queryServiceCellBand");
        enforceReadPermission();
        Phone phone2 = PhoneFactory.getPhone(SubscriptionController.getInstance().getDefaultDataSubId());
        boolean isWait = true;
        if (ServiceState.isCdma(phone2.getServiceState().getRilDataRadioTechnology())) {
            this.mServiceCellBand = new String[2];
            this.mServiceCellBand[0] = "CDMA";
            this.mServiceCellBand[1] = "0";
        } else {
            synchronized (this.mLock) {
                phone2.mCi.queryServiceCellBand(this.mMainHandler.obtainMessage(6));
                while (isWait) {
                    try {
                        this.mLock.wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        log("interrupted while trying to update by index");
                    }
                }
            }
        }
        if (this.mServiceCellBand == null) {
            return new String[0];
        }
        return (String[]) this.mServiceCellBand.clone();
    }

    public void handleQueryCellBandDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            this.mServiceCellBand = null;
        } else {
            this.mServiceCellBand = (String[]) ar.result;
        }
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
    }

    /* access modifiers changed from: private */
    public void handleQueryEncryptFeatureDone(Message msg) {
        Rlog.d(LOG_TAG, "query encrypt call feature received");
        AsyncResult ar = (AsyncResult) msg.obj;
        HwPhone phone2 = (HwPhone) ar.userObj;
        if (ar.exception != null || ar.result == null) {
            loge("query encrypt call feature failed " + ar.exception);
            if (msg.arg1 < 10) {
                this.mMainHandler.sendEmptyMessageDelayed(EVENT_QUERY_ENCRYPT_FEATURE, 1000);
            } else {
                queryCount = 0;
            }
        } else {
            byte[] res = (byte[]) ar.result;
            if (res.length > 0) {
                boolean z = true;
                if ((res[0] & 15) != 1) {
                    z = false;
                }
                this.mSupportEncryptCall = z;
                this.mEncryptCallStatus = res[0] >>> 4;
                if (this.mSupportEncryptCall) {
                    SystemProperties.set("persist.sys.cdma_encryption", Boolean.toString(this.mSupportEncryptCall));
                    checkEcSwitchStatusInNV(phone2, this.mEncryptCallStatus);
                }
            }
        }
    }

    public boolean registerForRadioAvailable(IPhoneCallback callback) {
        log("registerForRadioAvailable");
        setPhoneForMainSlot();
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioAvailableIndCB = callback;
        this.mPhone.getPhone().mCi.registerForAvailable(this.mMainHandler, 51, null);
        return true;
    }

    /* access modifiers changed from: private */
    public void handleRadioAvailableInd(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (this.mRadioAvailableIndCB == null) {
            loge("handleRadioAvailableInd mRadioAvailableIndCB is null");
            return;
        }
        if (ar.exception == null) {
            try {
                this.mRadioAvailableIndCB.onCallback1(0);
            } catch (RemoteException ex) {
                loge("handleRadioAvailableInd RemoteException: ex = " + ex);
            }
        } else {
            loge("radio available ind exception: " + ar.exception);
        }
    }

    public boolean unregisterForRadioAvailable(IPhoneCallback callback) {
        log("unregisterForRadioAvailable");
        setPhoneForMainSlot();
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioAvailableIndCB = null;
        this.mPhone.getPhone().mCi.unregisterForAvailable(this.mMainHandler);
        return true;
    }

    public boolean registerForRadioNotAvailable(IPhoneCallback callback) {
        log("registerForRadioNotAvailable");
        setPhoneForMainSlot();
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioNotAvailableIndCB = callback;
        this.mPhone.getPhone().mCi.registerForNotAvailable(this.mMainHandler, 52, null);
        return true;
    }

    /* access modifiers changed from: private */
    public void handleRadioNotAvailableInd(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (this.mRadioNotAvailableIndCB == null) {
            loge("handleRadioNotAvailableInd mRadioNotAvailableIndCB is null");
            return;
        }
        if (ar.exception == null) {
            try {
                this.mRadioNotAvailableIndCB.onCallback1(0);
            } catch (RemoteException ex) {
                loge("handleRadioNotAvailableInd RemoteException: ex = " + ex);
            }
        } else {
            loge("radio not available ind exception: " + ar.exception);
        }
    }

    public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) {
        log("unregisterForRadioNotAvailable");
        setPhoneForMainSlot();
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioNotAvailableIndCB = null;
        this.mPhone.getPhone().mCi.unregisterForNotAvailable(this.mMainHandler);
        return true;
    }

    public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) {
        log("registerCommonImsaToMapconInfo");
        setPhoneForMainSlot();
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mImsaToMapconInfoCB = callback;
        this.mPhone.getPhone().mCi.registerCommonImsaToMapconInfo(this.mMainHandler, 53, null);
        return true;
    }

    /* access modifiers changed from: private */
    public void handleCommonImsaToMapconInfoInd(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        log("handleCommonImsaToMapconInfoInd");
        if (this.mImsaToMapconInfoCB == null) {
            loge("handleCommonImsaToMapconInfoInd mImsaToMapconInfoCB is null");
            return;
        }
        if (ar.exception == null) {
            Bundle bundle = new Bundle();
            bundle.putByteArray("imsa2mapcon_msg", (byte[]) ar.result);
            try {
                this.mImsaToMapconInfoCB.onCallback3(0, 0, bundle);
            } catch (RemoteException ex) {
                loge("handleCommonImsaToMapconInfoInd RemoteException: ex = " + ex);
            }
        } else {
            loge("imsa to mapcon info exception: " + ar.exception);
        }
    }

    public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) {
        log("unregisterCommonImsaToMapconInfo");
        setPhoneForMainSlot();
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mImsaToMapconInfoCB = null;
        this.mPhone.getPhone().mCi.unregisterCommonImsaToMapconInfo(this.mMainHandler);
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
    public void notifyPhoneEventWithCallback(int phoneId, int event, int arg, Bundle bundle) {
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
        int i = 0;
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
                this.mPhones[phoneId].getPhone().mCi.registerForAvailable(this.mPhoneStateHandler, 1, Integer.valueOf(phoneId));
            }
            if ((events & 2) != 0) {
                this.mPhones[phoneId].getPhone().mCi.registerForNotAvailable(this.mPhoneStateHandler, 2, Integer.valueOf(phoneId));
            }
            if ((events & 4) != 0) {
                this.mPhones[phoneId].getPhone().mCi.registerCommonImsaToMapconInfo(this.mPhoneStateHandler, 4, Integer.valueOf(phoneId));
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

    public void setImsSwitch(boolean value) {
        setImsSwitchByPhoneId(getPhoneIdForMainSlot(), value);
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

    public boolean getImsSwitchByPhoneId(int phoneId) {
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
            return false;
        }
        boolean result = this.mPhones[phoneId].getImsSwitch();
        Rlog.d(LOG_TAG, "ImsSwitch = " + result);
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
            return this.mPhones[phoneId].getUiccAppType();
        }
        loge("phone is invalid!");
        return 0;
    }

    public int getImsDomain() {
        if (this.mPhone != null) {
            return getImsDomainByPhoneId(this.mPhone.getPhone().getPhoneId());
        }
        loge("phone is null!");
        return -1;
    }

    public int getImsDomainByPhoneId(int phoneId) {
        if (!checkPhoneIsValid(phoneId)) {
            loge("phone is invalid!");
            return -1;
        } else if (ImsManager.isWfcEnabledByPlatform(this.mContext)) {
            return ((int[]) sendRequest(CMD_IMS_GET_DOMAIN, null, Integer.valueOf(phoneId)))[0];
        } else {
            log("vowifi not support!");
            return -1;
        }
    }

    public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) {
        if (this.mPhone != null) {
            return handleUiccAuthByPhoneId(this.mPhone.getPhone().getPhoneId(), auth_type, rand, auth);
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

    /* access modifiers changed from: private */
    public Integer getPhoneId(Message msg) {
        if (msg == null) {
            return -1;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return -1;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return -1;
        }
        return (Integer) ar.userObj;
    }

    private void initPrefNetworkTypeChecker() {
        TelephonyManager mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (mTelephonyManager != null && !mTelephonyManager.isMultiSimEnabled() && !HuaweiTelephonyConfigs.isHisiPlatform()) {
            log("initPrefNetworkTypeChecker");
            if (SHOW_DIALOG_FOR_NO_SIM) {
                this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
            }
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    /* access modifiers changed from: private */
    public void handleCmdImsGetDomain(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_IMS_GET_DOMAIN_DONE, request);
        if (request.subId != null) {
            this.mPhones[request.subId.intValue()].getImsDomain(onCompleted);
            return;
        }
        this.mPhone.getImsDomain(onCompleted);
    }

    /* access modifiers changed from: private */
    public void handleCmdUiccAuth(Message msg) {
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
    public void handleGetPrefNetworkTypeDone(Message msg) {
        Rlog.d(LOG_TAG, "handleGetPrefNetworkTypeDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            int prefNetworkType = ((int[]) ar.result)[0];
            int currentprefNetworkTypeInDB = getCurrentNetworkTypeFromDB();
            log("prefNetworkType:" + prefNetworkType + " currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            if (prefNetworkType == currentprefNetworkTypeInDB) {
                return;
            }
            if (currentprefNetworkTypeInDB == -1 || this.mPhone == null || this.mPhone.getPhone() == null) {
                log("INVALID_NETWORK_MODE in DB,set 4G-Switch on");
                setServiceAbilityForSubId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, 0, 1);
                return;
            }
            this.mPhone.setPreferredNetworkType(currentprefNetworkTypeInDB, this.mMainHandler.obtainMessage(5, currentprefNetworkTypeInDB, this.mPhone.getPhone().getSubId()));
            log("setPreferredNetworkType -> currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            return;
        }
        log("getPreferredNetworkType exception=" + ar.exception);
    }

    /* access modifiers changed from: private */
    public void setSingleCardPrefNetwork(int slotId) {
        int prefNetwork;
        int i;
        int i2;
        if (!isValidSlotId(slotId)) {
            loge("invalid slotId " + slotId);
            return;
        }
        int ability = getLteServiceAbility();
        if (isCDMASimCard(slotId)) {
            if (1 == ability) {
                i2 = 10;
            } else {
                i2 = 7;
            }
            prefNetwork = i2;
        } else {
            if (1 == ability) {
                i = 20;
            } else {
                i = 18;
            }
            prefNetwork = i;
        }
        this.mPhone.setPreferredNetworkType(prefNetwork, this.mMainHandler.obtainMessage(13, slotId, prefNetwork));
        log("setSingleCardPrefNetwork, LTE ability = " + ability + ", pref network = " + prefNetwork);
    }

    private boolean isCDMASimCard(int slotId) {
        HwTelephonyManagerInner hwTelephonyManager = HwTelephonyManagerInner.getDefault();
        return hwTelephonyManager != null && hwTelephonyManager.isCDMASimCard(slotId);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.os.AsyncResult} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void handleSetPrefNetworkTypeDone(Message msg) {
        AsyncResult ar = null;
        if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
            ar = msg.obj;
        }
        int slot = msg.arg1;
        int setPrefMode = msg.arg2;
        if (ar != null && ar.exception == null) {
            if (getCurrentNetworkTypeFromDB() != setPrefMode) {
                saveNetworkTypeToDB(setPrefMode);
            }
            this.retryCount = 0;
            log("handleSetPrefNetworkTypeDone, success.");
        } else if (this.retryCount < 20) {
            this.retryCount++;
            this.mMainHandler.sendMessageDelayed(this.mMainHandler.obtainMessage(14, slot, setPrefMode), 3000);
        } else {
            this.retryCount = 0;
            loge("handleSetPrefNetworkTypeDone faild.");
        }
    }

    private static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        return cm != null && !cm.isNetworkSupported(0);
    }

    public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) {
        enforceReadPermission();
        boolean isSuccess = handleWirelessStateRequest(1, type, slotId, callback);
        logForSar("registerForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) {
        enforceReadPermission();
        boolean isSuccess = handleWirelessStateRequest(2, type, slotId, callback);
        logForSar("unregisterForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean setMaxTxPower(int type, int power) {
        Rlog.d(LOG_TAG, "setMaxTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (type == 2) {
            getCommandsInterface().setWifiTxPowerGrade(power, null);
        } else if (type == 1) {
            if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                Rlog.d(LOG_TAG, "setMaxTxPower: hisi");
                if (getCommandsInterface(0) != null) {
                    getCommandsInterface(0).setPowerGrade(power, null);
                }
                if (getCommandsInterface(1) != null) {
                    getCommandsInterface(1).setPowerGrade(power, null);
                }
            } else {
                getCommandsInterface().setPowerGrade(power, null);
            }
        }
        Rlog.d(LOG_TAG, "setMaxTxPower: end=" + power);
        return false;
    }

    private boolean handleWirelessStateRequest(int opertype, int type, int slotId, IPhoneCallback callback) {
        logForSar("In handleWirelessStateRequest service type=" + type + ",slotId=" + slotId);
        boolean isSuccess = false;
        CommandsInterface ci = getCommandsInterface(slotId);
        if (callback == null) {
            logForSar("handleWirelessStateRequest callback is null.");
            return false;
        } else if (ci == null) {
            logForSar("handleWirelessStateRequest ci is null.");
            return false;
        } else {
            switch (opertype) {
                case 1:
                    isSuccess = registerUnitSarControl(type, slotId, ci, callback);
                    break;
                case 2:
                    isSuccess = unregisterUnitSarControl(type, slotId, ci, callback);
                    break;
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
    public void handleSarInfoUploaded(int type, Message msg) {
        log("in handleSarInfoUploaded.");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            loge("handleSarInfoUploaded error, ar exception.");
            return;
        }
        int slotId = ((Integer) ar.userObj).intValue();
        CommandsInterface ci = getCommandsInterface(slotId);
        if (ci == null) {
            loge("handleSarInfoUploaded ci is null.");
            return;
        }
        if (!handleSarDataFromModem(type, ar)) {
            log("handleSarInfoUploaded hasClient is false,so to close the switch of upload Sar Info in modem");
            closeSarInfoUploadSwitch(type, slotId, ci);
            unregisterSarRegistrant(type, slotId, ci);
        }
    }

    private boolean handleSarDataFromModem(int type, AsyncResult ar) {
        log("handleSarDataFromModem start");
        boolean hasClient = false;
        int slotId = ((Integer) ar.userObj).intValue();
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
                    log("handleSarDataFromModem callback false,ex is RemoteException");
                    callbackList.remove(r);
                } catch (Exception e2) {
                    log("handleSarDataFromModem callback false,ex is Exception");
                    callbackList.remove(r);
                }
            }
            log("handleSarDataFromModem record size=" + callbackList.size());
        }
        return hasClient;
    }

    private boolean closeSarInfoUploadSwitch(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        if (type != 4) {
            switch (type) {
                case 1:
                    ci.closeSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
                    isSuccess = true;
                    break;
                case 2:
                    ci.closeSwitchOfUploadAntOrMaxTxPower(2);
                    isSuccess = true;
                    break;
            }
        } else {
            ci.closeSwitchOfUploadAntOrMaxTxPower(4);
            isSuccess = true;
        }
        log("closeSarInfoUploadSwitch mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + isSuccess);
        return isSuccess;
    }

    private Bundle getBundleData(AsyncResult ar) {
        log("getBundleData start");
        if (ar == null) {
            Rlog.d(LOG_TAG, "getBundleData: ar is null");
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap((byte[]) ar.result, 0, 4);
        buf.order(ByteOrder.nativeOrder());
        int result = buf.getInt();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY1, result);
        log("getBundleData result = " + result);
        return bundle;
    }

    private CommandsInterface getCommandsInterface(int slotId) {
        if (isValidSlotId(slotId)) {
            return PhoneFactory.getPhones()[slotId].mCi;
        }
        log("getCommandsInterface the slotId is invalid");
        return null;
    }

    private Object[] getCallbackArray(int type) {
        Object[] callbackArray = null;
        if (type != 4) {
            switch (type) {
                case 1:
                    callbackArray = this.mRegBandClassCallbackArray;
                    break;
                case 2:
                    callbackArray = this.mRegAntStateCallbackArray;
                    break;
            }
        } else {
            callbackArray = this.mRegMaxTxPowerCallbackArray;
        }
        logForSar("getCallbackArray type=" + type);
        return callbackArray;
    }

    private boolean unregisterUnitSarControl(int type, int slotId, CommandsInterface ci, IPhoneCallback callback) {
        int recordCount;
        boolean isSuccess;
        int recordCount2;
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            logForSar("unregisterUnitSarControl callbackArray is null.");
            return false;
        }
        synchronized (callbackArray[slotId]) {
            ArrayList<Record> callbackList = (ArrayList) callbackArray[slotId];
            IBinder b = callback.asBinder();
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

    private boolean registerUnitSarControl(int type, int slotId, CommandsInterface ci, IPhoneCallback callback) {
        logForSar("registerUnitSarControl start slotId=" + slotId + ",type=" + type);
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        int i = 0;
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

    private boolean openSarInfoUploadSwitch(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        if (type != 4) {
            switch (type) {
                case 1:
                    ci.openSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
                    isSuccess = true;
                    break;
                case 2:
                    isSuccess = ci.openSwitchOfUploadAntOrMaxTxPower(2);
                    break;
            }
        } else {
            isSuccess = ci.openSwitchOfUploadAntOrMaxTxPower(4);
        }
        logForSar("openSarInfoUploadSwitch mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + isSuccess);
        return isSuccess;
    }

    private boolean registerSarRegistrant(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        Message message = null;
        if (type != 4) {
            switch (type) {
                case 1:
                    message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
                    break;
                case 2:
                    message = this.mMainHandler.obtainMessage(11, Integer.valueOf(slotId));
                    break;
            }
        } else {
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
        sb.append(!isSuccess);
        loge(sb.toString());
        return isSuccess;
    }

    private boolean unregisterSarRegistrant(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        Message message = null;
        if (type != 4) {
            switch (type) {
                case 1:
                    message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
                    break;
                case 2:
                    message = this.mMainHandler.obtainMessage(11, Integer.valueOf(slotId));
                    break;
            }
        } else {
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
        sb.append(!isSuccess);
        logForSar(sb.toString());
        return isSuccess;
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        boolean z;
        boolean z2 = false;
        try {
            enforceReadPermission();
            boolean res = false;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                boolean res2 = false;
                for (HwPhone phone2 : this.mPhones) {
                    if (phone2 != null && phone2.isCDMAPhone()) {
                        if (!phone2.cmdForECInfo(event, action, buf)) {
                            if (!requestForECInfo(phone2, event, action, buf)) {
                                z = false;
                                res2 = z;
                            }
                        }
                        z = true;
                        res2 = z;
                    }
                }
                res = res2;
            } else if (this.mPhone != null && this.mPhone.isCDMAPhone()) {
                if (this.mPhone.cmdForECInfo(event, action, buf) || requestForECInfo(this.mPhone, event, action, buf)) {
                    z2 = true;
                }
                res = z2;
            }
            return res;
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "cmdForECInfo fail:" + ex);
            return false;
        }
    }

    private void registerForRadioOnInner() {
        Rlog.d(LOG_TAG, "registerForRadioOnInner");
        if (this.mPhone == null) {
            Rlog.e(LOG_TAG, "registerForRadioOnInner failed, phone is null!");
        } else {
            this.mPhone.getPhone().mCi.registerForOn(this.mMainHandler, EVENT_QUERY_ENCRYPT_FEATURE, null);
        }
    }

    /* access modifiers changed from: private */
    public void handleEventQueryEncryptCall(HwPhone phone2) {
        if (phone2 != null && phone2.isCDMAPhone()) {
            int i = 1;
            byte[] req = {(byte) 0};
            if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                this.mSupportEncryptCall = phone2.cmdForECInfo(7, 0, req);
                if (this.mSupportEncryptCall) {
                    SystemProperties.set("persist.sys.cdma_encryption", Boolean.toString(this.mSupportEncryptCall));
                }
                CommandsInterface ci = getCommandsInterface(phone2.getPhone().getPhoneId());
                if (ci != null) {
                    if (!((HwQualcommRIL) ci).getEcCdmaCallVersion()) {
                        i = 0;
                    }
                    this.mEncryptCallStatus = i;
                    checkEcSwitchStatusInNV(phone2, this.mEncryptCallStatus);
                    return;
                }
                loge("qcomRil is null");
                return;
            }
            Message msg = this.mMainHandler.obtainMessage(EVENT_QUERY_ENCRYPT_FEATURE_DONE, phone2);
            msg.arg1 = queryCount;
            phone2.requestForECInfo(msg, 7, req);
            queryCount++;
            Rlog.d(LOG_TAG, "query EncryptCall Count : " + queryCount);
        }
    }

    private boolean requestForECInfo(HwPhone phone2, int event, int action, byte[] buf) {
        EncryptCallPara para;
        if (event != 3) {
            switch (event) {
                case 5:
                case 6:
                case 7:
                    break;
                default:
                    para = new EncryptCallPara(phone2, event, buf);
                    break;
            }
        }
        para = new EncryptCallPara(phone2, event, null);
        byte[] res = (byte[]) sendRequest(HwFullNetworkConstants.MESSAGE_PENDING_DELAY, para);
        int len = res.length;
        boolean z = true;
        if (len == 1) {
            if (res[0] <= 0 || ((byte) (res[0] & 15)) != 1) {
                z = false;
            }
            return z;
        } else if (buf == null || 1 >= len || len > buf.length) {
            return false;
        } else {
            System.arraycopy(res, 0, buf, 0, len);
            Rlog.d(LOG_TAG, "requestForECInfo res length:" + len);
            return true;
        }
    }

    public boolean isCtSimCard(int slotId) {
        enforceReadPermission();
        if (isValidSlotId(slotId)) {
            return this.mPhones[slotId].isCtSimCard();
        }
        return false;
    }

    public void notifyCModemStatus(int status, IPhoneCallback callback) {
        if (Binder.getCallingUid() == 1000) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_NOTIFY_CMODEM_STATUS);
            msg.obj = callback;
            try {
                getCommandsInterface().notifyCModemStatus(status, msg);
            } catch (RuntimeException e) {
                loge("notifyCModemStatus got e = " + e);
                e.printStackTrace();
                if (callback != null) {
                    try {
                        callback.onCallback1(-1);
                    } catch (RemoteException ex) {
                        Rlog.e(LOG_TAG, "notifyCModemStatus onCallback1 RemoteException:" + ex);
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
        try {
            getCommandsInterface().notifyDeviceState(device, state, extra, this.mMainHandler.obtainMessage(EVENT_NOTIFY_DEVICE_STATE));
            return true;
        } catch (RuntimeException e) {
            loge("notifyDeviceState got e = " + e);
            return false;
        }
    }

    private void checkEcSwitchStatusInNV(HwPhone phone2, int statusInNV) {
        if (phone2 != null && phone2.isCDMAPhone()) {
            int statusInDB = Settings.Secure.getInt(this.mContext.getContentResolver(), "encrypt_version", 0);
            log("checkEcSwitchStatus, encryptCall statusInNV=" + statusInNV + " statusInDB=" + statusInDB);
            if (statusInNV != statusInDB) {
                int action = statusInDB;
                byte[] buf = {(byte) statusInDB};
                if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
                    phone2.requestForECInfo(this.mMainHandler.obtainMessage(EVENT_QUERY_ENCRYPT_FEATURE_DONE, phone2), 8, buf);
                } else if (phone2.cmdForECInfo(8, action, buf)) {
                    log("qcom reset NV success.");
                } else {
                    loge("qcom reset NV fail!");
                }
            }
        }
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        if (Binder.getCallingUid() != 1000) {
            Rlog.e(LOG_TAG, "getCallingUid() != Process.SYSTEM_UID, return");
            return;
        }
        Rlog.d(LOG_TAG, "notifyCellularCommParaReady");
        if (1 == paratype) {
            setPhoneForMainSlot();
            if (this.mPhone == null) {
                Rlog.e(LOG_TAG, "phone is null!");
                return;
            }
            this.mPhone.getPhone().notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_BASIC_COMM_PARA_UPGRADE_DONE));
        }
        if (2 == paratype) {
            if (this.mPhones[0] == null) {
                Rlog.e(LOG_TAG, "phone is null!");
                return;
            }
            this.mPhones[0].getPhone().notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE));
            if (HwVSimManager.getDefault().isVSimEnabled()) {
                Rlog.d(LOG_TAG, "isVSimEnabled is true");
                if (this.mPhones[2] == null) {
                    Rlog.e(LOG_TAG, "phone[2] is null!");
                    return;
                }
                this.mPhones[2].getPhone().notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE));
            } else if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                Rlog.d(LOG_TAG, "MultiSim is disable, mPhones[0] already processed above");
            } else if (this.mPhones[1] == null) {
                Rlog.e(LOG_TAG, "phone[1] is null!");
            } else {
                this.mPhones[1].getPhone().notifyCellularCommParaReady(paratype, pathtype, this.mMainHandler.obtainMessage(EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE));
            }
        }
    }

    public boolean setPinLockEnabled(boolean enablePinLock, String password, int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "setPinLockEnabled");
        if (!isValidStatus(enablePinLock, subId)) {
            return false;
        }
        this.setResultForPinLock = false;
        synchronized (this.mSetOPinLock) {
            this.phone.getIccCard().setIccLockEnabled(enablePinLock, password, this.mMainHandler.obtainMessage(EVENT_ENABLE_ICC_PIN_COMPLETE));
            boolean isWait = true;
            while (isWait) {
                try {
                    this.mSetOPinLock.wait();
                    isWait = false;
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                }
            }
        }
        return this.setResultForPinLock;
    }

    public boolean changeSimPinCode(String oldPinCode, String newPinCode, int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "changeSimPinCode");
        if (!isValidStatus(subId)) {
            return false;
        }
        this.setResultForChangePin = false;
        synchronized (this.mSetOPinLock) {
            this.phone.getIccCard().changeIccLockPassword(oldPinCode, newPinCode, this.mMainHandler.obtainMessage(EVENT_CHANGE_ICC_PIN_COMPLETE));
            boolean isWait = true;
            while (isWait) {
                try {
                    this.mSetOPinLock.wait();
                    isWait = false;
                } catch (InterruptedException e) {
                    log("interrupted while trying to update by index");
                }
            }
        }
        return this.setResultForChangePin;
    }

    private boolean isValidStatus(int subId) {
        return isValidStatus(false, subId);
    }

    private boolean isValidStatus(boolean enablePinLock, int subId) {
        if (!isValidSlotId(subId)) {
            return false;
        }
        this.phone = PhoneFactory.getPhone(subId);
        if (this.phone == null) {
            return false;
        }
        int airplaneMode = Settings.Global.getInt(this.phone.getContext().getContentResolver(), "airplane_mode_on", 0);
        if (1 == airplaneMode) {
            log("airplaneMode : " + airplaneMode);
            return false;
        }
        IccCardConstants.State mExternalState = this.phone.getIccCard().getState();
        if (mExternalState == IccCardConstants.State.PIN_REQUIRED || mExternalState == IccCardConstants.State.PUK_REQUIRED) {
            log("Need to unlock pin first! mExternalState : " + mExternalState);
            return false;
        } else if (SubscriptionController.getInstance() == null) {
            return false;
        } else {
            if (!(SubscriptionController.getInstance().getSubState(subId) == 1)) {
                return false;
            }
            boolean pinState = this.phone.getIccCard().getIccLockEnabled();
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
    }

    private void registerMDMSmsReceiver() {
        IntentFilter filter = new IntentFilter("com.huawei.devicepolicy.action.POLICY_CHANGED");
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mMDMSmsReceiver, filter);
        }
    }

    /* access modifiers changed from: private */
    public void clearSinglePolicyData(Context context, String timeMode, boolean isOutgoing) {
        log("clearSinglePolicyData: " + timeMode);
        if (!HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(isOutgoing)) {
            clearAllPolicyData(context, isOutgoing);
            return;
        }
        if (!TextUtils.isEmpty(timeMode)) {
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
            switch (c) {
                case 0:
                    removeSharedDayModeData(context, policyName, USED_OF_DAY, DAY_MODE_TIME);
                    break;
                case 1:
                    removeSharedDayModeData(context, policyName, USED_OF_WEEK, WEEK_MODE_TIME);
                    break;
                case 2:
                    removeSharedDayModeData(context, policyName, USED_OF_MONTH, MONTH_MODE_TIME);
                    break;
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
    public void clearAllPolicyData(Context context) {
        clearAllPolicyData(context, true);
        clearAllPolicyData(context, false);
    }

    private void clearAllPolicyData(Context context, boolean isOutgoing) {
        if (!HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(isOutgoing) && context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(isOutgoing ? OUTGOING_SMS_LIMIT : INCOMING_SMS_LIMIT, 0).edit();
            editor.clear();
            editor.commit();
        }
    }

    private void registerSetRadioCapDoneReceiver() {
        if (HwNetworkTypeUtils.IS_DUAL_IMS_SUPPORTED && HuaweiTelephonyConfigs.isHisiPlatform()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
            if (this.mContext != null) {
                this.mContext.registerReceiver(this.mSetRadioCapDoneReceiver, filter);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSwitchSlotDone(Intent intent) {
        int switchSlotStep = intent.getIntExtra("HW_SWITCH_SLOT_STEP", -99);
        if (!HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && 1 == switchSlotStep) {
            if (HwNetworkTypeUtils.isDualImsSwitchOpened()) {
                log("handleSwitchSlotDone. dual ims switch open, return.");
            } else if (1 == intent.getIntExtra(HwFullNetworkConstants.IF_NEED_SET_RADIO_CAP, 0)) {
                log("handleSwitchSlotDone. not happen real sim slot change, return.");
            } else {
                HwNetworkTypeUtils.exchangeDualCardNetworkModeDB(this.mContext);
            }
        }
    }

    public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterface ci = getCommandsInterface(subId);
        if (ci == null) {
            loge("sendPseudocellCellInfo  ci is null.");
            return false;
        }
        ci.sendPseudocellCellInfo(type, lac, cid, radioTech, plmn, null);
        return true;
    }

    private Phone getPhone(int subId) {
        SubscriptionController sc = SubscriptionController.getInstance();
        if (sc != null) {
            return PhoneFactory.getPhone(sc.getPhoneId(subId));
        }
        return null;
    }

    public void setImsRegistrationStateForSubId(int subId, boolean registered) {
        enforceModifyPermissionOrCarrierPrivilege();
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            phone2.setImsRegistrationState(registered);
        }
    }

    public boolean isImsRegisteredForSubId(int subId) {
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            return phone2.isImsRegistered();
        }
        return false;
    }

    public boolean isWifiCallingAvailableForSubId(int subId) {
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            return phone2.isWifiCallingEnabled();
        }
        return false;
    }

    public boolean isVolteAvailableForSubId(int subId) {
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            return phone2.isVolteEnabled();
        }
        return false;
    }

    public boolean isVideoTelephonyAvailableForSubId(int subId) {
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            return phone2.isVideoEnabled();
        }
        return false;
    }

    public boolean isDeactivatingSlaveData() {
        if (this.mInCallDataStateMachine == null) {
            return false;
        }
        return this.mInCallDataStateMachine.isDeactivatingSlaveData();
    }

    public boolean isSlaveActive() {
        if (this.mInCallDataStateMachine == null) {
            return false;
        }
        return this.mInCallDataStateMachine.isSlaveActive();
    }

    public boolean isSwitchingToSlave() {
        if (this.mInCallDataStateMachine == null) {
            return false;
        }
        return this.mInCallDataStateMachine.isSwitchingToSlave();
    }

    public void registerImsCallStates(boolean enable, int phoneId) {
        if (this.mInCallDataStateMachine != null) {
            this.mInCallDataStateMachine.registerImsCallStates(enable, phoneId);
        }
    }

    public boolean sendLaaCmd(int cmd, String reserved, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterface ci = getCommandsInterface(getDefault4GSlotId());
        this.sendLaaCmdCompleteMsg = response;
        if (ci == null) {
            loge("sendLaaCmd  ci is null.");
            sendResponseToTarget(response, -1);
            return false;
        }
        if (reserved == null) {
            reserved = "";
        }
        ci.sendLaaCmd(cmd, reserved, this.mMainHandler.obtainMessage(EVENT_SEND_LAA_CMD_DONE));
        return true;
    }

    /* access modifiers changed from: private */
    public void handleSendLaaCmdDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        int result = -1;
        if (ar != null && ar.exception == null) {
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
        CommandsInterface ci = getCommandsInterface(subId);
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
        CommandsInterface ci = getCommandsInterface(getDefault4GSlotId());
        this.getLaaStateCompleteMsg = response;
        if (ci == null) {
            loge("getLaaDetailedState  ci is null.");
            sendResponseToTarget(response, -1);
            return false;
        }
        if (reserved == null) {
            reserved = "";
        }
        ci.getLaaDetailedState(reserved, this.mMainHandler.obtainMessage(EVENT_GET_LAA_STATE_DONE));
        return true;
    }

    /* access modifiers changed from: private */
    public void handleGetLaaStateDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        int result = -1;
        if (ar != null && ar.exception == null && (ar.result instanceof int[])) {
            result = ((int[]) ar.result)[0];
        }
        sendResponseToTarget(this.getLaaStateCompleteMsg, result);
        log("handleGetLaaStateDone getLaaDetailedState result is " + result);
    }

    /* access modifiers changed from: private */
    public void handleGetCallforwardDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            Message cbMsg = (Message) ar.userObj;
            if (!(cbMsg == null || cbMsg.replyTo == null)) {
                Bundle data = new Bundle();
                if (ar.exception != null) {
                    data.putBoolean(CALLBACK_RESULT, false);
                    data.putString(CALLBACK_EXCEPTION, ar.exception.toString());
                } else {
                    data.putBoolean(CALLBACK_RESULT, true);
                }
                if (ar.result != null) {
                    CallForwardInfo[] cfiArray = (CallForwardInfo[]) ar.result;
                    if (cfiArray != null && cfiArray.length > 0) {
                        ArrayList<CallForwardInfo> cfiList = new ArrayList<>();
                        for (CallForwardInfo add : cfiArray) {
                            cfiList.add(add);
                        }
                        data.putParcelableArrayList(CALLBACK_CF_INFO, cfiList);
                    }
                }
                cbMsg.setData(data);
                try {
                    cbMsg.replyTo.send(cbMsg);
                } catch (RemoteException ex) {
                    Rlog.e(LOG_TAG, "EVENT_GET_CALLFORWARDING_DONE RemoteException:" + ex);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleGetNumRecBaseStattionDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null) {
            Rlog.e(LOG_TAG, "handleGetNumRecBaseStattionDone AsyncResult ar null");
            return;
        }
        Message cbMsg = (Message) ar.userObj;
        if (cbMsg == null || cbMsg.replyTo == null) {
            log("handleGetNumRecBaseStattionDone  cbMsg is null or cbMsg.replyTo is null");
        } else {
            Bundle data = new Bundle();
            data.putBoolean(CALLBACK_RESULT, true);
            if (ar.exception == null) {
                log("handleGetNumRecBaseStattionDone succ result is 0");
                data.putInt(CALLBACK_AFBS_INFO, 0);
            } else if (ar.exception instanceof CommandException) {
                data.putInt(CALLBACK_AFBS_INFO, ar.exception.getCommandError().ordinal());
                log("handleGetNumRecBaseStattionDone succ result is " + ar.exception.getCommandError().ordinal());
            } else {
                data.putInt(CALLBACK_AFBS_INFO, -1);
                log("handleGetNumRecBaseStattionDone fail result is  " + ar.exception);
            }
            cbMsg.setData(data);
            try {
                cbMsg.replyTo.send(cbMsg);
            } catch (RemoteException ex) {
                Rlog.e(LOG_TAG, "EVENT_GET_NUMRECBASESTATION_DONE RemoteException:" + ex);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSetFunctionDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            Message cbMsg = (Message) ar.userObj;
            if (!(cbMsg == null || cbMsg.replyTo == null)) {
                Bundle data = new Bundle();
                if (ar.exception != null) {
                    data.putBoolean(CALLBACK_RESULT, false);
                    data.putString(CALLBACK_EXCEPTION, ar.exception.toString());
                } else {
                    data.putBoolean(CALLBACK_RESULT, true);
                }
                cbMsg.setData(data);
                try {
                    cbMsg.replyTo.send(cbMsg);
                } catch (RemoteException ex) {
                    Rlog.e(LOG_TAG, "EVENT_SET_FUNCTION_DONE RemoteException:" + ex);
                }
            }
        }
    }

    public boolean isCspPlmnEnabled(int subId) {
        log("isCspPlmnEnabled for subId " + subId);
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            return phone2.isCspPlmnEnabled();
        }
        return false;
    }

    public void setCallForwardingOption(int subId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) {
        log("setCallForwardingOption for subId " + subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_SET_CALLFORWARDING_DONE);
            msg.obj = response;
            phone2.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, msg);
        } else if (response != null) {
            try {
                if (response.replyTo != null) {
                    Message cbMsg = Message.obtain(response);
                    Bundle data = new Bundle();
                    data.putBoolean(CALLBACK_RESULT, false);
                    cbMsg.setData(data);
                    response.replyTo.send(cbMsg);
                }
            } catch (RemoteException ex) {
                Rlog.e(LOG_TAG, "setCallForwardingOption RemoteException:" + ex);
            }
        }
    }

    public void getCallForwardingOption(int subId, int commandInterfaceCFReason, Message response) {
        log("getCallForwardingOption for subId " + subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_GET_CALLFORWARDING_DONE);
            msg.obj = response;
            phone2.getCallForwardingOption(commandInterfaceCFReason, msg);
        } else if (response != null) {
            try {
                if (response.replyTo != null) {
                    Message cbMsg = Message.obtain(response);
                    Bundle data = new Bundle();
                    data.putBoolean(CALLBACK_RESULT, false);
                    cbMsg.setData(data);
                    response.replyTo.send(cbMsg);
                }
            } catch (RemoteException ex) {
                Rlog.e(LOG_TAG, "getCallForwardingOption RemoteException:" + ex);
            }
        }
    }

    public boolean setSubscription(int subId, boolean activate, Message response) {
        log("setSubscription for subId: " + subId + ", activate: " + activate);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Message msg = this.mMainHandler.obtainMessage(201);
        msg.obj = response;
        if (HwSubscriptionManager.getInstance() != null) {
            return HwSubscriptionManager.getInstance().setSubscription(subId, activate, msg);
        }
        return false;
    }

    public String getImsImpu(int subId) {
        log("getImsImpu for subId " + subId);
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
        String impu = null;
        Phone phone2 = getPhone(subId);
        if (phone2 != null) {
            ImsPhone imsPhone = phone2.getImsPhone();
            if (imsPhone != null && (imsPhone.getCallTracker() instanceof ImsPhoneCallTracker)) {
                try {
                    ImsUt imsUt = imsPhone.getCallTracker().getUtInterface();
                    if (imsUt instanceof ImsUt) {
                        impu = HwImsUtManager.getUtIMPUFromNetwork(subId, imsUt);
                    }
                } catch (ImsException e) {
                    loge("get UtInterface occures exception" + e);
                }
            }
        }
        return impu;
    }

    public String getLine1NumberFromImpu(int subId) {
        log("getLine1NumberFromImpu for subId " + subId);
        String impu = getImsImpu(subId);
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
        return (TextUtils.isEmpty(number) || getPhone(subId) == null || number.equals(getPhone(subId).getSubscriberId())) ? null : number;
    }

    private boolean checkReadPhoneNumber(String callingPackage, String message) {
        if (this.mAppOps.noteOp(15, Binder.getCallingUid(), callingPackage) == 0) {
            return true;
        }
        try {
            return checkReadPhoneState(callingPackage, message);
        } catch (SecurityException e) {
            boolean z = false;
            try {
                this.mContext.enforceCallingOrSelfPermission("android.permission.READ_SMS", message);
                AppOpsManager appOpsManager = this.mAppOps;
                int opCode = AppOpsManager.permissionToOpCode("android.permission.READ_SMS");
                if (opCode == -1) {
                    return true;
                }
                if (this.mAppOps.noteOp(opCode, Binder.getCallingUid(), callingPackage) == 0) {
                    z = true;
                }
                return z;
            } catch (SecurityException e2) {
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_NUMBERS", message);
                    AppOpsManager appOpsManager2 = this.mAppOps;
                    int opCode2 = AppOpsManager.permissionToOpCode("android.permission.READ_PHONE_NUMBERS");
                    if (opCode2 == -1) {
                        return true;
                    }
                    if (this.mAppOps.noteOp(opCode2, Binder.getCallingUid(), callingPackage) == 0) {
                        z = true;
                    }
                    return z;
                } catch (SecurityException e3) {
                    throw new SecurityException(message + ": Neither user " + Binder.getCallingUid() + " nor current process has " + "android.permission.READ_PHONE_STATE" + ", " + "android.permission.READ_SMS" + ", or " + "android.permission.READ_PHONE_STATE" + ".");
                }
            }
        }
    }

    private boolean checkReadPhoneState(String callingPackage, String message) {
        boolean z = true;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
            }
            return z;
        }
    }

    private void sendResponseToTarget(Message response, int result) {
        if (response != null && response.getTarget() != null) {
            response.arg1 = result;
            response.sendToTarget();
        }
    }

    public void registerForCallAltSrv(int subId, IPhoneCallback callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        Phone phone2 = PhoneFactory.getPhone(subId);
        if (phone2 == null || callback == null) {
            log("registerForCallAltSrv:phone or callback is null,return");
            return;
        }
        log("registerForCallAltSrv for subId=" + subId);
        phone2.registerForCallAltSrv(phone2, EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE, callback);
    }

    public void unregisterForCallAltSrv(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        Phone phone2 = PhoneFactory.getPhone(subId);
        if (phone2 == null) {
            log("unregisterForCallAltSrv:phone or callback is null,return");
            return;
        }
        log("unregisterForCallAltSrv for subId=" + subId);
        phone2.unregisterForCallAltSrv(phone2);
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
            int returnValue = 0;
            try {
                AsyncResult result = (AsyncResult) sendRequest(CMD_INVOKE_OEM_RIL_REQUEST_RAW, oemReq, Integer.valueOf(phoneId));
                if (result.exception != null) {
                    returnValue = result.exception.getCommandError().ordinal();
                    logForOemHook("invokeOemRilRequestRaw fail exception + returnValue:" + returnValue);
                    if (returnValue > 0) {
                        returnValue *= -1;
                    }
                } else if (result.result != null) {
                    byte[] responseData = (byte[]) result.result;
                    if (responseData.length > oemResp.length) {
                        loge("Buffer to copy response too small: Response length is " + responseData.length + "bytes. Buffer Size is " + oemResp.length + "bytes.");
                    }
                    System.arraycopy(responseData, 0, oemResp, 0, responseData.length);
                    returnValue = responseData.length;
                    logForOemHook("invokeOemRilRequestRaw success, returnValue" + returnValue);
                } else {
                    logForOemHook("invokeOemRilRequestRaw fail result.result is null");
                }
            } catch (RuntimeException e) {
                logForOemHook("invokeOemRilRequestRaw: Runtime Exception");
                returnValue = CommandException.Error.GENERIC_FAILURE.ordinal();
                if (returnValue > 0) {
                    returnValue *= -1;
                }
            }
            return returnValue;
        }
    }

    /* access modifiers changed from: private */
    public void handleCmdOemRilRequestRaw(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_INVOKE_OEM_RIL_REQUEST_RAW_DONE, request);
        if (request.subId != null) {
            int phoneId = request.subId.intValue();
            Phone phone2 = this.mPhones[phoneId].getPhone();
            if (phone2 != null) {
                phone2.invokeOemRilRequestRaw((byte[]) request.argument, onCompleted);
                logForOemHook("invokeOemRilRequestRaw success by phoneId=" + phoneId);
                return;
            }
        }
        Phone phone3 = this.mPhone.getPhone();
        if (phone3 != null) {
            phone3.invokeOemRilRequestRaw((byte[]) request.argument, onCompleted);
            logForOemHook("invokeOemRilRequestRaw with default phone");
        }
    }

    /* access modifiers changed from: private */
    public void handleCmdOemRilRequestRawDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        MainThreadRequest request = (MainThreadRequest) ar.userObj;
        request.result = ar;
        synchronized (request) {
            request.notifyAll();
        }
    }

    public boolean isSecondaryCardGsmOnly() {
        enforceReadPermission();
        TelephonyManager mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        boolean z = false;
        if (mTelephonyManager != null && !mTelephonyManager.isMultiSimEnabled()) {
            return false;
        }
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && HwFullNetworkManager.getInstance().isCMCCHybird()) {
            z = true;
        }
        return z;
    }

    public boolean bindSimToProfile(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        if (slotId >= 0 && slotId <= numPhones) {
            return Settings.Global.putInt(this.mContext.getContentResolver(), "afw_work_slotid", slotId);
        }
        log("bind sim fail");
        return false;
    }

    public boolean setLine1Number(int subId, String alphaTag, String number, Message onComplete) {
        enforceModifyPermissionOrCarrierPrivilege();
        Phone phone2 = PhoneFactory.getPhone(subId);
        if (phone2 == null) {
            return false;
        }
        Message msg = this.mMainHandler.obtainMessage(202);
        msg.obj = onComplete;
        return phone2.setLine1Number(alphaTag, number, msg);
    }

    public boolean setDeepNoDisturbState(int slotId, int state) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        return ((Boolean) sendRequest(203, Integer.valueOf(state), Integer.valueOf(slotId))).booleanValue();
    }

    public boolean setUplinkFreqBandwidthReportState(int slotId, int state, IPhoneCallback callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (1 != state) {
            unregisterForUplinkfreqStateRpt();
        } else if (!registerForUplinkfreqStateRpt(slotId, callback)) {
            Rlog.e(LOG_TAG, "setUplinkFreqBandwidthReportState: registerForUlfreqStateRpt ERROR ");
            return false;
        }
        boolean response = ((Boolean) sendRequest(207, Integer.valueOf(state), Integer.valueOf(slotId))).booleanValue();
        Rlog.d(LOG_TAG, "setUplinkFreqBandwidthReportState: slotId = " + slotId + " state = " + state + ", set result = " + response);
        return response;
    }

    /* access modifiers changed from: private */
    public void handleCmdSetUplinkFreqBandwidthReportState(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        if (request != null) {
            if (request.subId == null || request.argument == null) {
                Rlog.d(LOG_TAG, "handleCmdSetUplinkFreqBandwidthReportState failed, request.subId =  " + request.subId + ", request.argument = " + request.argument);
                request.result = false;
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
            Rlog.d(LOG_TAG, "handleCmdSetUplinkFreqBandwidthReportState subId = " + request.subId + " arg = " + request.argument);
            CommandsInterface ci = getCommandsInterface(request.subId.intValue());
            if (ci == null) {
                Rlog.e(LOG_TAG, "handleCmdSetUplinkFreqBandwidthReportState failed: ci is null");
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
    public void handleSetUplinkFreqBandwidthDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            MainThreadRequest request = (MainThreadRequest) ar.userObj;
            if (request != null) {
                if (ar.exception == null) {
                    request.result = true;
                } else {
                    request.result = false;
                }
                Rlog.d(LOG_TAG, "handleSetUplinkFreqBandwidthDone notifyAll, request.result = " + request.result);
                synchronized (request) {
                    request.notifyAll();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleUplinkFreqBandwidthRpt(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (this.mUlFreqReportCB == null) {
            Rlog.e(LOG_TAG, "handleUplinkFreqBandwidthRpt mUlFreqReportCB is null");
            return;
        }
        if (ar == null || ar.exception != null || ar.result == null) {
            Rlog.e(LOG_TAG, "handleUplinkFreqBandwidthRpt exception");
        } else {
            try {
                this.mUlFreqReportCB.onCallback3(0, 0, (Bundle) ar.result);
            } catch (RemoteException e) {
                Rlog.e(LOG_TAG, "handleUplinkFreqBandwidthRpt RemoteException");
            }
            Rlog.d(LOG_TAG, "handleUplinkFreqBandwidthRpt: receive UlFreqBandwidth infor");
        }
    }

    private boolean registerForUplinkfreqStateRpt(int subId, IPhoneCallback callback) {
        log("receive registerForUlfreqStateRpt Enter: subId = " + subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (-1 != this.mUlFreqRptSubId || subId == -1 || callback == null) {
            return false;
        }
        this.mUlFreqRptSubId = subId;
        CommandsInterface ci = getCommandsInterface(this.mUlFreqRptSubId);
        if (ci == null) {
            Rlog.e(LOG_TAG, "handleCmdSetUplinkFreqBandwidthReportState ci null");
            return false;
        }
        Rlog.d(LOG_TAG, "registerForUlfreqStateRpt: register EVENT_UL_FREQ_BANDWIDTH_RPT");
        ci.registerForUplinkfreqStateRpt(this.mMainHandler, EVENT_UL_FREQ_BANDWIDTH_RPT, null);
        this.mUlFreqReportCB = callback;
        return true;
    }

    private void unregisterForUplinkfreqStateRpt() {
        Rlog.d(LOG_TAG, "receive unregisterForUlfreqStateRpt");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        CommandsInterface ci = getCommandsInterface(this.mUlFreqRptSubId);
        this.mUlFreqRptSubId = -1;
        if (ci == null) {
            Rlog.e(LOG_TAG, "handleCmdSetUplinkFreqBandwidthReportState ci null");
            return;
        }
        ci.unregisterForUplinkfreqStateRpt(this.mMainHandler);
        this.mUlFreqReportCB = null;
    }

    /* access modifiers changed from: private */
    public void handleCmdSetDeepNoDisturb(Message msg) {
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
            CommandsInterface ci = getCommandsInterface(request.subId.intValue());
            if (ci == null) {
                log("handleCmdSetDeepNoDisturb ci null");
                request.result = false;
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
            ci.setDeepNoDisturbState(((Integer) request.argument).intValue(), this.mMainHandler.obtainMessage(204, request));
        }
    }

    /* access modifiers changed from: private */
    public void handleSetDeepNoDisturbDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            MainThreadRequest request = (MainThreadRequest) ar.userObj;
            if (request != null) {
                if (ar.exception == null) {
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
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName) {
        try {
            getCommandsInterface().informModemTetherStatusToChangeGRO(enable, faceName, this.mMainHandler.obtainMessage(120));
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
        CommandsInterface ci = getCommandsInterface(slotId);
        if (ci == null) {
            loge("sendSimMatchedOperatorInfo: ci is null, slotId = " + slotId);
            return false;
        }
        ci.sendSimMatchedOperatorInfo(opKey, opName, state, reserveField, null);
        return true;
    }

    public boolean is4RMimoEnabled(int subId) {
        Phone phone2 = PhoneFactory.getPhone(subId);
        if (!(phone2 == null || phone2.getServiceStateTracker() == null)) {
            HwGsmServiceStateManager hwGsmSSM = HwServiceStateManager.getHwGsmServiceStateManager(phone2.getServiceStateTracker(), (GsmCdmaPhone) phone2);
            if (hwGsmSSM != null) {
                return hwGsmSSM.is4RMimoEnabled();
            }
        }
        return false;
    }

    public boolean getAntiFakeBaseStation(Message response) {
        log("getAntiFakeBaseStation ");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (getCommandsInterface() != null) {
            Message msg = this.mMainHandler.obtainMessage(EVENT_GET_NUMRECBASESTATION_DONE);
            msg.obj = response;
            return getCommandsInterface().getAntiFakeBaseStation(msg);
        }
        loge("getAntiFakeBaseStation phone null");
        return false;
    }

    public boolean registerForAntiFakeBaseStation(IPhoneCallback callback) {
        log("registerForAntiFakeBaseStation");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            if (this.mPhones[i] != null) {
                Rlog.d(LOG_TAG, "registerForAntiFakeBaseStation HwPhone sub = " + i);
                this.mPhones[i].getPhone().mCi.registerForAntiFakeBaseStation(this.mMainHandler, EVENT_ANTIFAKE_BASESTATION_CHANGED, null);
            }
        }
        this.mAntiFakeBaseStationCB = callback;
        return true;
    }

    /* access modifiers changed from: private */
    public void handleAntiFakeBaseStation(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (this.mAntiFakeBaseStationCB == null) {
            loge("handleAntiFakeBaseStation mAntiFakeBaseStationCB is null");
            return;
        }
        if (ar == null || ar.exception != null || ar.result == null) {
            loge("handleAntiFakeBaseStation exception: ar " + ar);
        } else {
            int parm = ((Integer) ar.result).intValue();
            Intent intent = new Intent("com.huawei.action.ACTION_HW_ANTIFAKE_BASESTATION");
            intent.setPackage("com.huawei.systemmanager");
            intent.putExtra(CALLBACK_AFBS_INFO, parm);
            this.mContext.sendBroadcast(intent);
            try {
                this.mAntiFakeBaseStationCB.onCallback1(parm);
            } catch (RemoteException ex) {
                loge("handleAntiFakeBaseStation RemoteException: ex = " + ex);
            }
            log("handleAntiFakeBaseStation send");
        }
    }

    public boolean unregisterForAntiFakeBaseStation() {
        log("unregisterForAntiFakeBaseStation");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            if (this.mPhones[i] != null) {
                Rlog.d(LOG_TAG, "unregisterForAntiFakeBaseStation HwPhone sub = " + i);
                this.mPhones[i].getPhone().mCi.unregisterForAntiFakeBaseStation(this.mMainHandler);
            }
        }
        this.mAntiFakeBaseStationCB = null;
        return true;
    }

    /* access modifiers changed from: private */
    public void handleGetCardTrayInfoDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.result == null || ar.exception != null) {
            this.mCardTrayInfo = null;
            loge("handleGetCardTrayInfoDone, exception occurs");
        } else if (ar.result instanceof byte[]) {
            this.mCardTrayInfo = (byte[]) ar.result;
            log("handleGetCardTrayInfoDone, mCardTrayInfo:" + IccUtils.bytesToHexString(this.mCardTrayInfo));
        }
        synchronized (this.mCardTrayLock) {
            this.mCardTrayLock.notifyAll();
        }
    }

    public byte[] getCardTrayInfo() {
        Rlog.d(LOG_TAG, "getCardTrayInfo");
        enforceReadPermission();
        synchronized (this.mCardTrayLock) {
            getCommandsInterface().getCardTrayInfo(this.mMainHandler.obtainMessage(EVENT_GET_CARD_TRAY_INFO_DONE));
            boolean isWait = true;
            while (isWait) {
                try {
                    this.mCardTrayLock.wait();
                    isWait = false;
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getCardTrayInfo");
                }
            }
        }
        if (this.mCardTrayInfo == null) {
            return new byte[0];
        }
        return (byte[]) this.mCardTrayInfo.clone();
    }

    public boolean setCsconEnabled(boolean isEnabled) {
        log("setCsconEnabled, isEnabled = " + isEnabled);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        int enable = isEnabled;
        for (HwPhone phone2 : this.mPhones) {
            phone2.getPhone().mCi.setCsconEnabled((int) enable, null);
        }
        return true;
    }

    public int[] getCsconEnabled() {
        int[] result = {-1, -1};
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        for (int i = 0; i < this.mPhones.length; i++) {
            result[i] = ((Integer) sendRequest(205, null, Integer.valueOf(i))).intValue();
        }
        return result;
    }

    private boolean isPhoneIDValid(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handleGetCscon(Message msg) {
        MainThreadRequest request = (MainThreadRequest) msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(206, request);
        if (request.subId != null) {
            int phoneId = request.subId.intValue();
            if (isValidSlotId(phoneId)) {
                this.mPhones[phoneId].getPhone().mCi.getCsconEnabled(onCompleted);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleGetCsconDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            MainThreadRequest request = (MainThreadRequest) ar.userObj;
            if (request != null) {
                if (ar.exception != null || ar.result == null) {
                    loge("EVENT_CMD_GET_CSCON_ID: error");
                } else {
                    request.result = ar.result;
                }
                if (request.result == null) {
                    request.result = -1;
                }
                synchronized (request) {
                    request.notifyAll();
                }
            }
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
        }
        HwSignalStrength hwSigStr = HwSignalStrength.getInstance(PhoneFactory.getPhone(phoneId));
        if (hwSigStr != null) {
            return hwSigStr.getLevel(signalType, rssi, ecio);
        }
        log("hwSigStr is null.");
        return 0;
    }

    private void initCommBoosterManager() {
        this.bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
        registerBoosterCallback();
    }

    private void registerBoosterCallback() {
        log("registerBoosterCallback enter");
        if (this.bm != null) {
            int ret = this.bm.registerCallBack("com.android.internal.telephony", this.mIHwCommBoosterCallback);
            if (ret != 0) {
                log("registerBoosterCallback:registerCallBack failed, ret=" + ret);
                return;
            }
            return;
        }
        log("registerBoosterCallback:null HwCommBoosterServiceManager");
    }

    /* access modifiers changed from: private */
    public void showNotification(String contentTitle, String contentText) {
        if (this.mContext != null) {
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(NETWORK_LIMIT_NOTIFY_CHANNEL, this.mContext.getString(33686061), 4));
            this.mNotificationManager.notify(LOG_TAG, 1, new Notification.Builder(this.mContext).setSmallIcon(33752043).setAppName(this.mContext.getString(33686061)).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1).setContentTitle(contentTitle).setContentText(contentText).setStyle(new Notification.BigTextStyle()).addAction(getKnownAction()).setChannelId(NETWORK_LIMIT_NOTIFY_CHANNEL).build());
        }
    }

    /* access modifiers changed from: private */
    public void showDataFlowNotification(int count) {
        if (this.mContext != null) {
            Intent resultIntent = getResultIntent();
            resultIntent.setFlags(335544320);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this.mContext, 0, resultIntent, 134217728);
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(DATA_FLOW_NOTIFY_CHANNEL, this.mContext.getString(33686061), 4));
            int i = 1;
            Notification.Builder defaults = new Notification.Builder(this.mContext).setSmallIcon(33752043).setAppName(this.mContext.getString(33686061)).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1);
            Context context = this.mContext;
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(getDefault4GSlotId() == 1 ? 1 : 2);
            objArr[1] = Integer.valueOf(count);
            Notification.Builder contentTitle = defaults.setContentTitle(context.getString(33686069, objArr));
            Context context2 = this.mContext;
            Object[] objArr2 = new Object[1];
            if (getDefault4GSlotId() != 1) {
                i = 2;
            }
            objArr2[0] = Integer.valueOf(i);
            this.mNotificationManager.notify(LOG_TAG, 2, contentTitle.setContentText(context2.getString(33686070, objArr2)).setContentIntent(resultPendingIntent).setStyle(new Notification.BigTextStyle()).addAction(getSwitchBackAction()).addAction(getIgnoreAction()).addAction(getNeverAction()).setChannelId(DATA_FLOW_NOTIFY_CHANNEL).build());
        }
    }

    private Notification.Action getNeverAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_NEVER_NOTIFY);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder(null, this.mContext.getString(33685985), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    private Notification.Action getIgnoreAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_IGNORE_NOTIFY);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder(null, this.mContext.getString(33685756), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    private Notification.Action getSwitchBackAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_SWITCH_BACK);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder(null, this.mContext.getString(33685534, new Object[]{Integer.valueOf(getDefault4GSlotId() + 1)}), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    private Notification.Action getKnownAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_CANCEL_NOTIFY);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder(null, this.mContext.getString(33685725), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    /* access modifiers changed from: private */
    public void dismissNotification(int notifyId) {
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
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mIntelligenceCardReceiver, filter);
        }
    }

    public void repotToBoosterForSwitchBack() {
        sendReportToBooster(TYPE_REPORT_SWITCH_BACK);
    }

    public void reportToBoosterForNoRetryPdp() {
        sendReportToBooster(TYPE_REPORT_NO_RETRY_FOR_PDP_FAIL);
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
        return (!isValidSlotId(phoneId) || this.mPhones == null || phoneId >= this.mPhones.length || this.mPhones[phoneId] == null || this.mPhones[phoneId].getPhone() == null) ? false : true;
    }

    public int getDataRegisteredState(int subId) {
        enforceReadPermission();
        if (!isValidPhone(subId)) {
            loge("getDataRegisteredState: Phone is Invalid for subId = " + subId);
            return 1;
        }
        ServiceStateTracker sst = this.mPhones[subId].getPhone().getServiceStateTracker();
        if (sst == null || sst.mSS == null) {
            return 1;
        }
        return sst.mSS.getDataRegState();
    }

    private String getIccidBySub(int subId) {
        UiccController uiccController = UiccController.getInstance();
        if (uiccController.getUiccCardForSlot(subId) != null) {
            return uiccController.getUiccCardForSlot(subId).getIccId();
        }
        return null;
    }

    public boolean isCustomAis() {
        return HwFullNetworkConfig.IS_AIS_4G_DSDX_ENABLE;
    }

    public boolean isAISCard(int subId) {
        String inn = "";
        String iccId = getIccidBySub(subId);
        if (!TextUtils.isEmpty(iccId) && iccId.length() >= 7) {
            inn = iccId.substring(0, 7);
        }
        String mccMnc = TelephonyManager.getDefault().getSimOperator(subId);
        if ((TextUtils.isEmpty(inn) || !HwIccIdUtil.isAIS(inn)) && (TextUtils.isEmpty(mccMnc) || !HwIccIdUtil.isAISByMccMnc(mccMnc))) {
            return false;
        }
        return true;
    }

    public int getVoiceRegisteredState(int subId) {
        enforceReadPermission();
        if (!isValidPhone(subId)) {
            loge("getVoiceRegisteredState: Phone is Invalid for subId = " + subId);
            return 1;
        }
        ServiceStateTracker sst = this.mPhones[subId].getPhone().getServiceStateTracker();
        if (sst == null || sst.mSS == null) {
            return 1;
        }
        return sst.mSS.getVoiceRegState();
    }

    private boolean checkPhoneIsValid(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return false;
        } else if (this.mPhones[phoneId] != null) {
            return true;
        } else {
            loge("phone is null!");
            return false;
        }
    }

    private int getPhoneIdForMainSlot() {
        if (!TelephonyManager.getDefault().isMultiSimEnabled() || getDefault4GSlotId() != 1) {
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

    private void handleIntelligentRecommendation() {
        boolean hasSwitchOn = SystemProperties.getBoolean("persist.radio.smart.card", false);
        boolean showEnable = SystemProperties.getBoolean("persist.sys.smart_switch_enable", false);
        if (hasSwitchOn) {
            log("The switch was used. not allowed recommend");
        } else if (!showEnable) {
            log("Function not supported");
        } else {
            if (SystemClock.elapsedRealtime() - this.mLastNotifyTime > MIN_INTERVAL_TIME || this.mLastNotifyTime == 0) {
                log("Once a day");
                this.mLastNotifyTime = SystemClock.elapsedRealtime();
                DecisionUtil.bindService(this.mContext, REC_DECISION_NAME);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNetworkSpeedLimit(Bundle b) {
        log("network speed limit prompt");
        if (CHINA_RELEASE_VERSION) {
            if (HwVSimUtils.isVSimEnabled()) {
                log("vsim is working, so return");
            } else if (this.mHasNotifyLimit) {
                log("has notify limit");
            } else if (b.getInt("modemId", -1) != 0) {
                log("At present, the main card is only concerned.");
            } else {
                if (b.getInt("downLink") <= DOWNLINK_LIMIT) {
                    log("show limit notify");
                    this.mHasNotifyLimit = true;
                    showNotification(this.mContext.getString(33686143), this.mContext.getString(33686144));
                }
            }
        }
    }

    public int getNetworkModeFromDB(int subId) {
        enforceReadPermission();
        if (isValidSlotId(subId)) {
            return HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, subId);
        }
        log("getNetworkModeFromDB the slotId is invalid");
        return -1;
    }

    public void saveNetworkModeToDB(int subId, int mode) {
        enforceModifyPermissionOrCarrierPrivilege();
        if (!isValidSlotId(subId)) {
            log("saveNetworkModeToDB the slotId is invalid");
        } else {
            HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, subId, mode);
        }
    }

    /* access modifiers changed from: private */
    public void handleSetTempCtrlDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        int result = -1;
        if (ar != null && ar.exception == null) {
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
                Rlog.e(LOG_TAG, "SendResponseToTarget RemoteException");
            }
        }
    }

    private Intent getResultIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.netassistant.ui.NetAssistantMainActivity"));
        return intent;
    }
}
