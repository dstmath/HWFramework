package com.android.internal.telephony;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.emcom.EmcomManager;
import android.net.ConnectivityManager;
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
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.telephony.UiccAuthResponse;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.android.internal.telephony.HwPhoneManager.PhoneServiceInterface;
import com.android.internal.telephony.IHwTelephony.Stub;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.dataconnection.InCallDataStateMachine;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import huawei.cust.HwCustUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class HwPhoneService extends Stub implements PhoneServiceInterface {
    private static final int CMD_ENCRYPT_CALL_INFO = 200;
    private static final int CMD_HANDLE_DEMO = 1;
    private static final int CMD_IMS_GET_DOMAIN = 103;
    private static final int CMD_UICC_AUTH = 101;
    private static final String DAY_MODE = "day_mode";
    private static final String DAY_MODE_TIME = "day_mode_time";
    private static final String DEVICEID_PREF = "deviceid";
    private static final int EVENT_BASIC_COMM_PARA_UPGRADE_DONE = 112;
    private static final int EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE = 113;
    private static final int EVENT_CHANGE_ICC_PIN_COMPLETE = 205;
    private static final int EVENT_COMMON_IMSA_MAPCON_INFO = 53;
    private static final int EVENT_ENABLE_ICC_PIN_COMPLETE = 204;
    private static final int EVENT_ENCRYPT_CALL_INFO_DONE = 201;
    private static final int EVENT_GET_CELL_BAND_DONE = 6;
    private static final int EVENT_GET_LAA_STATE_DONE = 115;
    private static final int EVENT_GET_PREF_NETWORKS = 3;
    private static final int EVENT_GET_PREF_NETWORK_TYPE_DONE = 9;
    private static final int EVENT_IMS_GET_DOMAIN_DONE = 104;
    private static final int EVENT_NOTIFY_CMODEM_STATUS = 110;
    private static final int EVENT_NOTIFY_DEVICE_STATE = 111;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE = 202;
    private static final int EVENT_QUERY_ENCRYPT_FEATURE_DONE = 203;
    private static final int EVENT_RADIO_AVAILABLE = 51;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 52;
    private static final int EVENT_REG_ANT_STATE_IND = 11;
    private static final int EVENT_REG_BAND_CLASS_IND = 10;
    private static final int EVENT_REG_MAX_TX_POWER_IND = 12;
    private static final int EVENT_SEND_LAA_CMD_DONE = 114;
    private static final int EVENT_SET_LTE_SWITCH_DONE = 5;
    private static final int EVENT_SET_PREF_NETWORKS = 4;
    private static final int EVENT_UICC_AUTH_DONE = 102;
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final String IMEI_PREF = "imei";
    private static final String INCOMING_SMS_LIMIT = "incoming_limit";
    private static final int INVALID = -1;
    private static final int INVALID_NETWORK_MODE = -1;
    private static final int INVALID_STEP = -99;
    private static final boolean IS_4G_SWITCH_SUPPORTED = SystemProperties.getBoolean("persist.sys.dualcards", false);
    private static final boolean IS_DUAL_IMS_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    private static final boolean IS_FULL_NETWORK_SUPPORTED = SystemProperties.getBoolean("ro.config.full_network_support", false);
    private static final boolean IS_GSM_NONSUPPORT = SystemProperties.getBoolean("ro.config.gsm_nonsupport", false);
    private static final boolean IS_MODEM_FULL_PREFMODE_SUPPORTED = HwModemCapability.isCapabilitySupport(3);
    private static final String IS_OUTGOING = "isOutgoing";
    private static final boolean IS_QCRIL_CROSS_MAPPING = SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false);
    private static final String KEY1 = "key1";
    private static final int LENGTH_LOCK_DATA_LAYER = SystemProperties.getInt("ro.config.hw_simlock_layer", 4);
    private static final String LOG_TAG = "HwPhoneService";
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    private static final int MAX_QUERY_COUNT = 10;
    private static final String MONTH_MODE = "month_mode";
    private static final String MONTH_MODE_TIME = "month_mode_time";
    private static final int MSG_ENCRYPT_CALL_BASE = 200;
    private static final int NOTIFY_CMODEM_STATUS_FAIL = -1;
    private static final int NOTIFY_CMODEM_STATUS_SUCCESS = 1;
    private static final String OUTGOING_SMS_LIMIT = "outgoing_limit";
    private static final int PARATYPE_BASIC_COMM = 1;
    private static final int PARATYPE_CELLULAR_CLOUD = 2;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final String POLICY_REMOVE_ALL = "remove_all_policy";
    private static final String POLICY_REMOVE_SINGLE = "remove_single_policy";
    private static final int REDUCE_TYPE_CELL = 1;
    private static final int REDUCE_TYPE_WIFI = 2;
    private static final int REGISTER_TYPE = 1;
    private static final int REGISTER_TYPE_ANTENNA = 2;
    private static final int REGISTER_TYPE_BAND = 1;
    private static final int REGISTER_TYPE_MAX_TX_POWER = 4;
    private static final String REMOVE_TYPE = "removeType";
    private static final int REQUEST_SEND_SIM_LOCK_NW_DATA = 210;
    private static final int SERVICE_2G_OFF = 0;
    private static final boolean SHOW_DIALOG_FOR_NO_SIM = SystemProperties.getBoolean("ro.config.no_sim", false);
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final int STATE_IN_AIR_PLANE_MODE = 1;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final int SUB_NONE = -1;
    private static final int SUCCESS = 0;
    private static final String TIME_MODE = "time_mode";
    private static final int TYPEMASK_PARATYPE_BASIC_COMM = 0;
    private static final int TYPEMASK_PARATYPE_CELLULAR_CLOUD = 1;
    private static final int UNREGISTER_TYPE = 2;
    private static final String USED_OF_DAY = "used_number_day";
    private static final String USED_OF_MONTH = "used_number_month";
    private static final String USED_OF_WEEK = "used_number_week";
    private static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
    private static final String WEEK_MODE = "week_mode";
    private static final String WEEK_MODE_TIME = "week_mode_time";
    private static int lteOffMappingMode;
    private static int lteOnMappingMode;
    private static HashMap<Integer, Integer> mLteOnOffMapping = new HashMap();
    private static int queryCount = 0;
    private static HwPhoneService sInstance = null;
    private static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private final int ENCRYPT_CALL_FEATURE_CLOSE = 0;
    private final String ENCRYPT_CALL_FEATURE_KEY = "encrypt_version";
    private final int ENCRYPT_CALL_FEATURE_OPEN = 1;
    private final int ENCRYPT_CALL_FEATURE_SUPPORT = 1;
    private final int ENCRYPT_CALL_NV_OFFSET = 4;
    private Message getLaaStateCompleteMsg = null;
    private AppOpsManager mAppOps;
    private Context mContext;
    private int mEncryptCallStatus = 0;
    private HwCustPhoneService mHwCustPhoneService = null;
    private IPhoneCallback mImsaToMapconInfoCB = null;
    InCallDataStateMachine mInCallDataStateMachine;
    protected final Object mLock = new Object();
    private BroadcastReceiver mMDMSmsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                String removeType = intent.getStringExtra(HwPhoneService.REMOVE_TYPE);
                HwPhoneService.this.log("removeType: " + removeType);
                boolean isOutgoing = intent.getBooleanExtra(HwPhoneService.IS_OUTGOING, false);
                if (HwPhoneService.POLICY_REMOVE_SINGLE.equals(removeType)) {
                    String timeMode = intent.getStringExtra(HwPhoneService.TIME_MODE);
                    HwPhoneService.this.log("mMDMSmsReceiver onReceive : " + timeMode);
                    HwPhoneService.this.clearSinglePolicyData(context, timeMode, isOutgoing);
                } else if (HwPhoneService.POLICY_REMOVE_ALL.equals(removeType)) {
                    HwPhoneService.this.clearAllPolicyData(context);
                }
            }
        }
    };
    private MainHandler mMainHandler;
    private HandlerThread mMessageThread = new HandlerThread("HuaweiPhoneTempService");
    private HwPhone mPhone;
    private PhoneServiceReceiver mPhoneServiceReceiver;
    private PhoneStateHandler mPhoneStateHandler;
    private HwPhone[] mPhones = null;
    private IPhoneCallback mRadioAvailableIndCB = null;
    private IPhoneCallback mRadioNotAvailableIndCB = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && HwPhoneService.this.mPhone != null && "android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && "READY".equals(intent.getExtra("ss"))) {
                HwPhoneService.this.log("mReceiver receive ACTION_SIM_STATE_CHANGED READY,check pref network type");
                HwPhoneService.this.mPhone.getPreferredNetworkType(HwPhoneService.this.mMainHandler.obtainMessage(9));
            }
            if (HwPhoneService.SHOW_DIALOG_FOR_NO_SIM && intent != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (telephonyManager != null && 1 == telephonyManager.getSimState()) {
                    Builder dialogBuilder = new Builder(context, 3);
                    dialogBuilder.setTitle(33685979).setMessage(33685980).setCancelable(false).setPositiveButton(33685981, null);
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.getWindow().setType(2003);
                    alertDialog.show();
                }
            }
        }
    };
    private final ArrayList<Record> mRecords = new ArrayList();
    private Object[] mRegAntStateCallbackArray = new Object[]{this.mRegAntStateCallbackLists0, this.mRegAntStateCallbackLists1};
    private final ArrayList<Record> mRegAntStateCallbackLists0 = new ArrayList();
    private final ArrayList<Record> mRegAntStateCallbackLists1 = new ArrayList();
    private Object[] mRegBandClassCallbackArray = new Object[]{this.mRegBandClassCallbackLists0, this.mRegBandClassCallbackLists1};
    private final ArrayList<Record> mRegBandClassCallbackLists0 = new ArrayList();
    private final ArrayList<Record> mRegBandClassCallbackLists1 = new ArrayList();
    private Object[] mRegMaxTxPowerCallbackArray = new Object[]{this.mRegMaxTxPowerCallbackList0, this.mRegMaxTxPowerCallbackList1};
    private final ArrayList<Record> mRegMaxTxPowerCallbackList0 = new ArrayList();
    private final ArrayList<Record> mRegMaxTxPowerCallbackList1 = new ArrayList();
    private final ArrayList<IBinder> mRemoveRecordsList = new ArrayList();
    private String[] mServiceCellBand = new String[2];
    protected final Object mSetOPinLock = new Object();
    private BroadcastReceiver mSetRadioCapDoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null && "com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(action)) {
                    HwPhoneService.this.handleSwitchSlotDone(intent);
                }
            }
        }
    };
    boolean mSupportEncryptCall = SystemProperties.getBoolean("persist.sys.cdma_encryption", false);
    Phone phone;
    private Message sendLaaCmdCompleteMsg = null;
    private boolean setResultForChangePin = false;
    private boolean setResultForPinLock = false;
    private String[] simLockFinalDatas = new String[((LENGTH_LOCK_DATA_LAYER * 2) + 1)];

    private static final class EncryptCallPara {
        byte[] buf = null;
        int event;
        HwPhone phone = null;

        public EncryptCallPara(HwPhone phone, int event, byte[] buf) {
            this.phone = phone;
            this.event = event;
            this.buf = buf;
        }
    }

    private final class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            AsyncResult ar;
            MainThreadRequest request;
            HwPhone phone;
            switch (msg.what) {
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
                case 51:
                    HwPhoneService.this.handleRadioAvailableInd(msg);
                    return;
                case 52:
                    HwPhoneService.this.handleRadioNotAvailableInd(msg);
                    return;
                case 53:
                    HwPhoneService.this.handleCommonImsaToMapconInfoInd(msg);
                    return;
                case HwPhoneService.CMD_UICC_AUTH /*101*/:
                    HwPhoneService.this.handleCmdUiccAuth(msg);
                    return;
                case HwPhoneService.EVENT_UICC_AUTH_DONE /*102*/:
                    ar = msg.obj;
                    request = ar.userObj;
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
                        break;
                    }
                case HwPhoneService.CMD_IMS_GET_DOMAIN /*103*/:
                    HwPhoneService.this.handleCmdImsGetDomain(msg);
                    return;
                case HwPhoneService.EVENT_IMS_GET_DOMAIN_DONE /*104*/:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
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
                        break;
                    }
                case HwPhoneService.EVENT_NOTIFY_CMODEM_STATUS /*110*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS");
                    ar = (AsyncResult) msg.obj;
                    if (ar != null) {
                        IPhoneCallback callback = ar.userObj;
                        if (callback != null) {
                            int result = 1;
                            if (ar.exception != null) {
                                result = -1;
                            }
                            try {
                                Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS onCallback1");
                                callback.onCallback1(result);
                                return;
                            } catch (RemoteException ex) {
                                Rlog.e(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_CMODEM_STATUS onCallback1 RemoteException:" + ex);
                                return;
                            }
                        }
                        return;
                    }
                    return;
                case HwPhoneService.EVENT_NOTIFY_DEVICE_STATE /*111*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar == null) {
                        HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, ar is null.");
                        return;
                    } else if (ar.exception == null) {
                        Rlog.d(HwPhoneService.LOG_TAG, "EVENT_NOTIFY_DEVICE_STATE success.");
                        return;
                    } else if (ar.exception instanceof CommandException) {
                        HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, " + ar.exception);
                        return;
                    } else {
                        HwPhoneService.loge("EVENT_NOTIFY_DEVICE_STATE, unknown exception.");
                        return;
                    }
                case HwPhoneService.EVENT_BASIC_COMM_PARA_UPGRADE_DONE /*112*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "EVENT_BASIC_COMM_PARA_UPGRADE_DONE");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Rlog.e(HwPhoneService.LOG_TAG, "Error in BasicImsNVPara Upgrade:" + ar.exception);
                        return;
                    }
                    int[] resultUpgrade = ar.result;
                    if (resultUpgrade.length != 0) {
                        Rlog.d(HwPhoneService.LOG_TAG, "EVENT_BASIC_COMM_PARA_UPGRADE_DONE: result=" + resultUpgrade[0]);
                    } else {
                        Rlog.e(HwPhoneService.LOG_TAG, "EVENT_BASIC_COMM_PARA_UPGRADE_DONE: resultUpgrade.length = 0");
                        resultUpgrade[0] = -1;
                    }
                    EmcomManager.getInstance().responseForParaUpgrade(1, 1, resultUpgrade[0]);
                    Rlog.d(HwPhoneService.LOG_TAG, "responseForParaUpgrade()");
                    return;
                case HwPhoneService.EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE /*113*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Rlog.e(HwPhoneService.LOG_TAG, "Error in Cellular Cloud Para Upgrade:" + ar.exception);
                        return;
                    }
                    int[] phoneResult = ar.result;
                    if (phoneResult.length != 0) {
                        Rlog.d(HwPhoneService.LOG_TAG, "EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE: phoneResult=" + phoneResult[0]);
                    } else {
                        Rlog.e(HwPhoneService.LOG_TAG, "EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE: phoneResult.length = 0");
                        phoneResult[0] = -1;
                    }
                    EmcomManager.getInstance().responseForParaUpgrade(2, 1, phoneResult[0]);
                    Rlog.d(HwPhoneService.LOG_TAG, "responseForParaUpgrade()");
                    return;
                case HwPhoneService.EVENT_SEND_LAA_CMD_DONE /*114*/:
                    HwPhoneService.this.log("EVENT_SEND_LAA_CMD_DONE");
                    HwPhoneService.this.handleSendLaaCmdDone(msg);
                    return;
                case HwPhoneService.EVENT_GET_LAA_STATE_DONE /*115*/:
                    HwPhoneService.this.log("EVENT_GET_LAA_STATE_DONE");
                    HwPhoneService.this.handleGetLaaStateDone(msg);
                    return;
                case 200:
                    Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo receive event");
                    request = (MainThreadRequest) msg.obj;
                    EncryptCallPara ECpara = request.argument;
                    Message onCompleted = obtainMessage(HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE, request);
                    HwPhone sPhone = ECpara.phone;
                    if (sPhone != null) {
                        sPhone.requestForECInfo(onCompleted, ECpara.event, ECpara.buf);
                        return;
                    }
                    return;
                case HwPhoneService.EVENT_ENCRYPT_CALL_INFO_DONE /*201*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo receive event done");
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null) {
                        if (ar.result == null || ((byte[]) ar.result).length <= 0) {
                            request.result = new byte[]{(byte) 1};
                            Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo success,return 1");
                        } else {
                            request.result = ar.result;
                            Rlog.d(HwPhoneService.LOG_TAG, "requestForECInfo success,return ar.result");
                        }
                    } else if (ar.exception instanceof CommandException) {
                        request.result = new byte[]{(byte) -1};
                        HwPhoneService.loge("requestForECInfo: CommandException:return -1 " + ar.exception);
                    } else {
                        request.result = new byte[]{(byte) -2};
                        HwPhoneService.loge("requestForECInfo: Unknown exception,return -2");
                    }
                    synchronized (request) {
                        request.notifyAll();
                        break;
                    }
                case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE /*202*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "radio available, query encrypt call feature");
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        for (HwPhone phone2 : HwPhoneService.this.mPhones) {
                            HwPhoneService.this.handleEventQueryEncryptCall(phone2);
                        }
                        return;
                    }
                    HwPhoneService.this.handleEventQueryEncryptCall(HwPhoneService.this.mPhone);
                    return;
                case HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE_DONE /*203*/:
                    Rlog.d(HwPhoneService.LOG_TAG, "query encrypt call feature received");
                    ar = (AsyncResult) msg.obj;
                    phone2 = (HwPhone) ar.userObj;
                    if (ar.exception != null || ar.result == null) {
                        HwPhoneService.loge("query encrypt call feature failed " + ar.exception);
                        if (msg.arg1 < 10) {
                            sendEmptyMessageDelayed(HwPhoneService.EVENT_QUERY_ENCRYPT_FEATURE, 1000);
                            return;
                        } else {
                            HwPhoneService.queryCount = 0;
                            return;
                        }
                    }
                    byte[] res = ar.result;
                    if (res.length > 0) {
                        HwPhoneService.this.mSupportEncryptCall = (res[0] & 15) == 1;
                        HwPhoneService.this.mEncryptCallStatus = res[0] >>> 4;
                        if (HwPhoneService.this.mSupportEncryptCall) {
                            SystemProperties.set("persist.sys.cdma_encryption", Boolean.toString(HwPhoneService.this.mSupportEncryptCall));
                            HwPhoneService.this.checkEcSwitchStatusInNV(phone2, HwPhoneService.this.mEncryptCallStatus);
                            return;
                        }
                        return;
                    }
                    return;
                case HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE /*204*/:
                case HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE /*205*/:
                    handlePinResult(msg);
                    synchronized (HwPhoneService.this.mSetOPinLock) {
                        HwPhoneService.this.mSetOPinLock.notifyAll();
                    }
                    return;
                case HwPhoneService.REQUEST_SEND_SIM_LOCK_NW_DATA /*210*/:
                    HwPhoneService.this.log("REQUEST_SEND_SIM_LOCK_NW_DATA");
                    HwPhoneService.this.handleSendSimlockData(msg);
                    return;
                default:
                    return;
            }
        }

        private void handlePinResult(Message msg) {
            if (msg.obj.exception != null) {
                Rlog.d(HwPhoneService.LOG_TAG, "set fail.");
                if (msg.what == HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE) {
                    HwPhoneService.this.setResultForPinLock = false;
                    return;
                } else if (msg.what == HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE) {
                    HwPhoneService.this.setResultForChangePin = false;
                    return;
                } else {
                    return;
                }
            }
            Rlog.d(HwPhoneService.LOG_TAG, "set success.");
            if (msg.what == HwPhoneService.EVENT_ENABLE_ICC_PIN_COMPLETE) {
                HwPhoneService.this.setResultForPinLock = true;
            } else if (msg.what == HwPhoneService.EVENT_CHANGE_ICC_PIN_COMPLETE) {
                HwPhoneService.this.setResultForChangePin = true;
            }
        }

        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "[enter]handleGetPreferredNetworkTypeResponse");
            AsyncResult ar = msg.obj;
            if (ar.exception == null) {
                Rlog.d(HwPhoneService.LOG_TAG, "getPreferredNetworkType is " + ((int[]) ar.result)[0]);
                return;
            }
            Rlog.d(HwPhoneService.LOG_TAG, "getPreferredNetworkType exception=" + ar.exception);
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            Rlog.d(HwPhoneService.LOG_TAG, "[enter]handleSetPreferredNetworkTypeResponse");
            AsyncResult ar = msg.obj;
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
    }

    private static final class MainThreadRequest {
        public Object argument;
        public Object result;
        public Integer subId;

        public MainThreadRequest(Object argument) {
            this.argument = argument;
        }

        public MainThreadRequest(Object argument, Integer subId) {
            this.argument = argument;
            this.subId = subId;
        }
    }

    private class PhoneServiceReceiver extends BroadcastReceiver {
        public PhoneServiceReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.RADIO_TECHNOLOGY");
            HwPhoneService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            int i = 0;
            Rlog.d(HwPhoneService.LOG_TAG, "radio tech changed, query encrypt call feature");
            if (intent != null && "android.intent.action.RADIO_TECHNOLOGY".equals(intent.getAction())) {
                HwPhoneService.queryCount = 0;
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    HwPhone[] -get5 = HwPhoneService.this.mPhones;
                    int length = -get5.length;
                    while (i < length) {
                        HwPhoneService.this.handleEventQueryEncryptCall(-get5[i]);
                        i++;
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
            switch (msg.what) {
                case 1:
                    handleRadioAvailable(msg);
                    return;
                case 2:
                    handleRadioNotAvailable(msg);
                    return;
                case 4:
                    handleCommonImsaToMapconInfo(msg);
                    return;
                default:
                    return;
            }
        }

        private void handleRadioAvailable(Message msg) {
            AsyncResult ar = msg.obj;
            int phoneId = HwPhoneService.this.getPhoneId(msg).intValue();
            if (ar.exception == null) {
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 1, 0, null);
            } else {
                HwPhoneService.loge("radio available exception: " + ar.exception);
            }
        }

        private void handleRadioNotAvailable(Message msg) {
            AsyncResult ar = msg.obj;
            int phoneId = HwPhoneService.this.getPhoneId(msg).intValue();
            if (ar.exception == null) {
                HwPhoneService.this.notifyPhoneEventWithCallback(phoneId, 2, 0, null);
            } else {
                HwPhoneService.loge("radio not available exception: " + ar.exception);
            }
        }

        private void handleCommonImsaToMapconInfo(Message msg) {
            AsyncResult ar = msg.obj;
            int phoneId = HwPhoneService.this.getPhoneId(msg).intValue();
            if (ar.exception == null) {
                Bundle bundle = new Bundle();
                bundle.putByteArray("imsa2mapcon_msg", ar.result);
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

        /* synthetic */ Record(Record -this0) {
            this();
        }

        private Record() {
            this.phoneId = -1;
        }

        boolean matchPhoneStateListenerEvent(int events) {
            return (this.callback == null || (this.events & events) == 0) ? false : true;
        }

        public String toString() {
            return "binder=" + this.binder + " callback=" + this.callback + " phoneId=" + this.phoneId + " events=" + Integer.toHexString(this.events) + "}";
        }

        boolean matchPhoneEvent(int events) {
            return (this.callback == null || (this.events & events) == 0) ? false : true;
        }
    }

    private static final class UiccAuthPara {
        byte[] auth;
        int auth_type;
        byte[] rand;

        public UiccAuthPara(int auth_type, byte[] rand, byte[] auth) {
            this.auth_type = auth_type;
            this.rand = rand;
            this.auth = auth;
        }
    }

    static {
        lteOnMappingMode = -1;
        lteOffMappingMode = -1;
        mLteOnOffMapping.put(Integer.valueOf(20), Integer.valueOf(18));
        mLteOnOffMapping.put(Integer.valueOf(9), Integer.valueOf(3));
        mLteOnOffMapping.put(Integer.valueOf(15), Integer.valueOf(13));
        mLteOnOffMapping.put(Integer.valueOf(8), Integer.valueOf(4));
        mLteOnOffMapping.put(Integer.valueOf(10), Integer.valueOf(7));
        mLteOnOffMapping.put(Integer.valueOf(11), Integer.valueOf(7));
        mLteOnOffMapping.put(Integer.valueOf(12), Integer.valueOf(2));
        mLteOnOffMapping.put(Integer.valueOf(17), Integer.valueOf(16));
        mLteOnOffMapping.put(Integer.valueOf(19), Integer.valueOf(14));
        mLteOnOffMapping.put(Integer.valueOf(22), Integer.valueOf(21));
        mLteOnOffMapping.put(Integer.valueOf(25), Integer.valueOf(24));
        mLteOnOffMapping.put(Integer.valueOf(26), Integer.valueOf(1));
        lteOnMappingMode = SystemProperties.getInt("ro.telephony.default_network", -1);
        if (mLteOnOffMapping.containsKey(Integer.valueOf(lteOnMappingMode))) {
            lteOffMappingMode = ((Integer) mLteOnOffMapping.get(Integer.valueOf(lteOnMappingMode))).intValue();
        } else {
            lteOnMappingMode = -1;
        }
        String[] lteOnOffMapings = SystemProperties.get("ro.hwpp.lteonoff_mapping", "0,0").split(",");
        if (lteOnOffMapings.length == 2 && Integer.parseInt(lteOnOffMapings[0]) != 0) {
            lteOnMappingMode = Integer.parseInt(lteOnOffMapings[0]);
            lteOffMappingMode = Integer.parseInt(lteOnOffMapings[1]);
        }
    }

    public HwPhoneService() {
        this.mMessageThread.start();
        this.mMainHandler = new MainHandler(this.mMessageThread.getLooper());
        this.mPhoneStateHandler = new PhoneStateHandler(this.mMessageThread.getLooper());
        this.mHwCustPhoneService = (HwCustPhoneService) HwCustUtils.createObj(HwCustPhoneService.class, new Object[]{this, this.mMessageThread.getLooper()});
    }

    public void setPhone(Phone phone, Context context) {
        this.mPhone = new HwPhone(phone);
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

    private void initService() {
        Rlog.d(LOG_TAG, "initService()");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        ServiceManager.addService("phone_huawei", this);
        saveInstance(this);
        initPrefNetworkTypeChecker();
        registerForRadioOnInner();
        if (this.mPhoneServiceReceiver == null) {
            this.mPhoneServiceReceiver = new PhoneServiceReceiver();
        }
        registerMDMSmsReceiver();
        registerSetRadioCapDoneReceiver();
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    public String getDemoString() {
        enforceReadPermission();
        return "" + this.mPhone + this.mContext;
    }

    public String getMeidForSubscriber(int slot) {
        if (!canReadPhoneState("getMeid") || slot < 0 || slot >= this.mPhones.length) {
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
        if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
            return false;
        }
        boolean noCdmaPhone = true;
        for (HwPhone isCDMAPhone : this.mPhones) {
            if (isCDMAPhone.isCDMAPhone()) {
                noCdmaPhone = false;
                break;
            }
        }
        return noCdmaPhone;
    }

    public boolean isLTESupported() {
        boolean result;
        switch (RILConstants.PREFERRED_NETWORK_MODE) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 13:
            case 14:
            case 16:
            case 18:
            case 21:
            case 52:
                result = false;
                break;
            default:
                result = true;
                break;
        }
        Rlog.i(LOG_TAG, "isLTESupported " + result);
        return result;
    }

    public void setDefaultMobileEnable(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Rlog.d(LOG_TAG, "setDefaultMobileEnable to " + enabled);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            this.mPhones[i].setDefaultMobileEnable(enabled);
            TelephonyNetworkFactory telephonyNetworkFactory = PhoneFactory.getTelephonyNetworkFactory(i);
            if (telephonyNetworkFactory != null) {
                telephonyNetworkFactory.reconnectDefaultRequestRejectByWifi();
            }
        }
        Phone vsimPhone = HwVSimPhoneFactory.getVSimPhone();
        if (vsimPhone != null) {
            HwPhone hwVsimPhone = new HwPhone(vsimPhone);
            Rlog.d(LOG_TAG, "setDefaultMobileEnable to " + enabled + " for vsimPhone");
            hwVsimPhone.setDefaultMobileEnable(enabled);
        }
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
        if (Looper.myLooper() == this.mMainHandler.getLooper()) {
            throw new RuntimeException("This method will deadlock if called from the main thread.");
        }
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

    public void setPreferredNetworkType(int nwMode) {
        enforceModifyPermissionOrCarrierPrivilege();
        Rlog.d(LOG_TAG, "[enter]setPreferredNetworkType " + nwMode);
        if (TelephonyManager.getDefault().isMultiSimEnabled() && HwAllInOneController.getInstance().getBalongSimSlot() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            Rlog.e(LOG_TAG, "4G-Switch mPhone is null. return!");
        } else {
            this.mPhone.setPreferredNetworkType(nwMode, this.mMainHandler.obtainMessage(4, Integer.valueOf(nwMode)));
        }
    }

    public void setLteServiceAbility(int ability) {
        enforceModifyPermissionOrCarrierPrivilege();
        setLteServiceAbilityForSubId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, ability);
    }

    public void setLteServiceAbilityForSubId(int subId, int ability) {
        enforceModifyPermissionOrCarrierPrivilege();
        log("=4G-Switch= setLteServiceAbility: ability=" + ability + ", subId=" + subId);
        if (!isValidSlotId(subId)) {
            return;
        }
        if (IS_QCRIL_CROSS_MAPPING) {
            HwAllInOneController.getInstance().setLteServiceAbilityForQCOM(ability, lteOnMappingMode);
            return;
        }
        HwPhone phone;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            phone = this.mPhones[subId];
            if (subId == getDefault4GSlotId()) {
                this.mPhone = phone;
            }
        } else {
            phone = this.mPhone;
        }
        if (phone == null) {
            loge("4G-Switch phone is null. return!");
            return;
        }
        boolean isQcomSRAL;
        int networkType = IS_MODEM_FULL_PREFMODE_SUPPORTED ? calculateNetworkType(ability) : phone.isCDMAPhone() ? ability == 1 ? 8 : 4 : IS_GSM_NONSUPPORT ? ability == 1 ? 12 : 2 : ability == 1 ? 9 : 3;
        if (!HuaweiTelephonyConfigs.isQcomPlatform() || (TelephonyManager.getDefault().isMultiSimEnabled() ^ 1) == 0) {
            isQcomSRAL = false;
        } else {
            isQcomSRAL = IS_FULL_NETWORK_SUPPORTED;
        }
        if (isQcomSRAL) {
            networkType = phone.isCDMAPhone() ? ability == 1 ? 10 : 7 : ability == 1 ? 20 : 18;
        }
        if (this.mHwCustPhoneService != null && this.mHwCustPhoneService.isDisable2GServiceCapabilityEnabled() && this.mHwCustPhoneService.get2GServiceAbility() == 0) {
            networkType = this.mHwCustPhoneService.getNetworkTypeBaseOnDisabled2G(networkType);
        }
        if (HwTelephonyFactory.getHwNetworkManager().isNetworkModeAsynchronized(phone.getPhone())) {
            HwTelephonyFactory.getHwNetworkManager().handle4GSwitcherForNoMdn(phone.getPhone(), networkType);
            sendLteServiceSwitchResult(subId, true);
            return;
        }
        phone.setPreferredNetworkType(networkType, this.mMainHandler.obtainMessage(5, networkType, subId));
        log("=4G-Switch= setPreferredNetworkType-> " + networkType);
    }

    private int calculateNetworkType(int ability) {
        int networkType = ability == 1 ? lteOnMappingMode : lteOffMappingMode;
        int curPrefMode = getCurrentNetworkTypeFromDB();
        int mappingNetworkType = ability == 1 ? getOnKeyFromMapping(curPrefMode) : getOffValueFromMapping(curPrefMode);
        if (!HwAllInOneController.IS_CMCC_4GSWITCH_DISABLE && ability == 1 && mappingNetworkType == 26) {
            log("=4G-Switch= mappingNetworkType change from " + mappingNetworkType + " to " + 9);
            mappingNetworkType = 9;
        }
        log("=4G-Switch= curPrefMode = " + curPrefMode + " ,mappingNetworkType = " + mappingNetworkType);
        if (-1 != mappingNetworkType) {
            return mappingNetworkType;
        }
        return networkType;
    }

    private int getCurrentNetworkTypeFromDB() {
        return getCurrentNetworkTypeFromDB(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0);
    }

    private int getCurrentNetworkTypeFromDB(int subId) {
        int curPrefMode = -1;
        try {
            if (IS_MODEM_FULL_PREFMODE_SUPPORTED && TelephonyManager.getDefault().isMultiSimEnabled()) {
                return TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", subId);
            }
            if (!IS_DUAL_IMS_SUPPORTED || !TelephonyManager.getDefault().isMultiSimEnabled()) {
                return Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", -1);
            }
            curPrefMode = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId, -1);
            int curModeOfDefault4G = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", -1);
            if (curModeOfDefault4G != curPrefMode && subId == getDefault4GSlotId()) {
                saveNetworkTypeToDB(subId, curModeOfDefault4G);
                curPrefMode = curModeOfDefault4G;
                loge("=4G-Switch= curModeOfDefault4G of sub" + subId + " is " + curModeOfDefault4G);
                return curPrefMode;
            } else if (-1 != curPrefMode) {
                return curPrefMode;
            } else {
                curPrefMode = isDualImsSwitchOpened() ? RILConstants.PREFERRED_NETWORK_MODE : 3;
                saveNetworkTypeToDB(subId, curPrefMode);
                loge("=4G-Switch= curPrefMode of sub" + subId + " is " + curPrefMode);
                return curPrefMode;
            }
        } catch (RuntimeException e) {
            loge("=4G-Switch=  PREFERRED_NETWORK_MODE RuntimeException = " + e);
            return curPrefMode;
        } catch (Exception e2) {
            loge("=4G-Switch=  PREFERRED_NETWORK_MODE Exception = " + e2);
            return curPrefMode;
        }
    }

    private void saveNetworkTypeToDB(int setPrefMode) {
        saveNetworkTypeToDB(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0, setPrefMode);
    }

    private void saveNetworkTypeToDB(int subId, int setPrefMode) {
        if (IS_MODEM_FULL_PREFMODE_SUPPORTED && TelephonyManager.getDefault().isMultiSimEnabled()) {
            TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", subId, setPrefMode);
        } else if (IS_DUAL_IMS_SUPPORTED && TelephonyManager.getDefault().isMultiSimEnabled()) {
            Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId, setPrefMode);
            if (subId == getDefault4GSlotId()) {
                Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode", setPrefMode);
            }
        } else {
            Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode", setPrefMode);
        }
        log("=4G-Switch= save network mode " + setPrefMode + " to database success!");
    }

    private boolean isDualImsSwitchOpened() {
        return 1 == SystemProperties.getInt("persist.radio.dualltecap", 0);
    }

    private int getOnKeyFromMapping(int curPrefMode) {
        int onKey = -1;
        for (Entry<Integer, Integer> entry : mLteOnOffMapping.entrySet()) {
            if (curPrefMode == ((Integer) entry.getValue()).intValue()) {
                onKey = ((Integer) entry.getKey()).intValue();
                if (7 != curPrefMode || 11 != onKey) {
                    break;
                }
            }
        }
        return onKey;
    }

    private int getOffValueFromMapping(int curPrefMode) {
        if (mLteOnOffMapping.containsKey(Integer.valueOf(curPrefMode))) {
            return ((Integer) mLteOnOffMapping.get(Integer.valueOf(curPrefMode))).intValue();
        }
        return -1;
    }

    private void handleSetLteSwitchDone(Message msg) {
        log("=4G-Switch= in handleSetLteSwitchDone");
        AsyncResult ar = msg.obj;
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
        int curPrefMode = getCurrentNetworkTypeFromDB(subId);
        log("=4G-Switch= subId:" + subId + " curPrefMode in db:" + curPrefMode + " setPrefMode:" + setPrefMode);
        if (curPrefMode != setPrefMode) {
            saveNetworkTypeToDB(subId, setPrefMode);
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
        this.mContext.sendBroadcast(intent);
        log("=4G-Switch= result is " + result + ". broadcast PREFERRED_4G_SWITCH_DONE");
    }

    public int getLteServiceAbility() {
        enforceReadPermission();
        return getLteServiceAbilityForSubId(IS_4G_SWITCH_SUPPORTED ? getDefault4GSlotId() : 0);
    }

    public int getLteServiceAbilityForSubId(int subId) {
        enforceReadPermission();
        log("getLteServiceAbility, subId = " + subId);
        if (!isValidSlotId(subId)) {
            return 0;
        }
        int curPrefMode = getCurrentNetworkTypeFromDB(subId);
        int ability = IS_MODEM_FULL_PREFMODE_SUPPORTED ? mLteOnOffMapping.containsKey(Integer.valueOf(curPrefMode)) ? 1 : 0 : isLteServiceOn(curPrefMode) ? 1 : 0;
        log("getLteServiceAbility, curPrefMode = " + curPrefMode + ", ability =" + ability);
        return ability;
    }

    private boolean isLteServiceOn(int curPrefMode) {
        if ((curPrefMode >= 8 && curPrefMode <= 31) || curPrefMode == 61 || curPrefMode == 63) {
            return true;
        }
        return false;
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

    private static void loge(String msg) {
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
        if (HwModemCapability.isCapabilitySupport(9) || !(MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration() || (SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false) ^ 1) == 0)) {
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
        if (slotId < 0 || slotId >= SIM_NUM) {
            Rlog.d(LOG_TAG, "setDefaultDataSlotId: invalid slotId!!!");
            return;
        }
        Global.putInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, slotId);
        if (SubscriptionController.getInstance() != null) {
            SubscriptionController.getInstance().setDefaultDataSubId(SubscriptionController.getInstance().getSubId(slotId)[0]);
        } else {
            Rlog.d(LOG_TAG, "SubscriptionController.getInstance()! null");
        }
        Rlog.d(LOG_TAG, "setDefaultDataSlotId done");
    }

    public int getDefault4GSlotId() {
        int subscription = 0;
        try {
            subscription = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            Rlog.d(LOG_TAG, "Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
        }
        Rlog.d(LOG_TAG, "4GSlot: " + subscription);
        return subscription;
    }

    public void setDefault4GSlotId(int slotId, Message msg) {
        int uid = Binder.getCallingUid();
        if (!HwAllInOneController.IS_CMCC_4GSWITCH_DISABLE || uid == HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE || uid == HwVSimEventHandler.EVENT_VSIM_DISABLE_RETRY || uid == 0) {
            enforceModifyPermissionOrCarrierPrivilege();
            log("in setDefault4GSlotId.");
            HwAllInOneController.getInstance().setDefault4GSlot(slotId, msg);
            return;
        }
        loge("setDefault4GSlotId: Disallowed call for uid " + uid);
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        enforceReadPermission();
        log("in isSetDefault4GSlotIdEnabled.");
        if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate() || (HwVSimUtils.isAllowALSwitch() ^ 1) != 0) {
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
            if (!HwAllInOneController.IS_HISI_DSDX || ((TelephonyManager.getDefault().getSimState(0) != 5 || sc.getSubState(sub1[0]) != 0) && (TelephonyManager.getDefault().getSimState(1) != 5 || sc.getSubState(sub2[0]) != 0))) {
                return HwAllInOneController.getInstance().isSwitchDualCardSlotsEnabled();
            }
            log("isSetDefault4GSlotIdEnabled return false when has sim INACTIVE when IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT");
            return false;
        }
    }

    public void waitingSetDefault4GSlotDone(boolean waiting) {
        enforceReadPermission();
        if (!HwModemCapability.isCapabilitySupport(9)) {
            HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(waiting);
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
        if (isValidSlotId(subId)) {
            Phone phone = PhoneFactory.getPhone(subId);
            if (phone == null) {
                return null;
            }
            if (phone.getCdmaGsmImsi() != null || !isCtSimCard(subId)) {
                return phone.getCdmaGsmImsi();
            }
            Rlog.d(LOG_TAG, "getCdmaGsmImsi is null");
            IccRecords iccRecords = UiccController.getInstance().getIccRecords(subId, 2);
            if (iccRecords != null) {
                return iccRecords.getCdmaGsmImsi();
            }
            return null;
        }
        Rlog.d(LOG_TAG, "subId is not avaible!");
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
        return HwAllInOneController.getInstance().getSpecCardType(slotId);
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
        if (uid == HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE || uid == HwVSimEventHandler.EVENT_VSIM_DISABLE_RETRY) {
            enforceCellLocationPermission("getCellLocation");
            Bundle data = new Bundle();
            CellLocation cellLoc;
            if (slotId >= 0 && slotId < this.mPhones.length) {
                cellLoc = this.mPhones[slotId].getCellLocation();
                if (cellLoc != null) {
                    cellLoc.fillInNotifierBundle(data);
                }
            } else if (sIsPlatformSupportVSim && slotId == 2) {
                cellLoc = getVSimPhone().getCellLocation();
                if (cellLoc != null) {
                    cellLoc.fillInNotifierBundle(data);
                }
            } else {
                cellLoc = this.mPhone.getCellLocation();
                if (cellLoc != null) {
                    cellLoc.fillInNotifierBundle(data);
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
            if (!(appInfo == null || (appInfo.flags & 1) == 0)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Rlog.e(LOG_TAG, packageName + " not found.");
        }
        return false;
    }

    private boolean canReadPhoneState(String message) {
        boolean z = true;
        String callingPackage = null;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm != null) {
                String[] callingPackageName = pm.getPackagesForUid(Binder.getCallingUid());
                callingPackage = callingPackageName[0];
                if (isSystemApp(callingPackageName[0])) {
                    Rlog.d(LOG_TAG, callingPackageName[0] + " allowed.");
                    return true;
                }
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
            }
            return z;
        }
    }

    public String getUniqueDeviceId(int scope) {
        isReadPhoneNumberBlocked();
        if (canReadPhoneState("getDeviceId")) {
            String sharedPref = DEVICEID_PREF;
            if (1 == scope) {
                sharedPref = IMEI_PREF;
            }
            String deviceId = getDeviceIdFromSP(sharedPref);
            if (deviceId != null) {
                return deviceId;
            }
            String newDeviceId = readDeviceIdFromLL(scope);
            if (!(newDeviceId == null || (newDeviceId.matches("^0*$") ^ 1) == 0)) {
                deviceId = newDeviceId;
                saveDeviceIdToSP(newDeviceId, sharedPref);
            }
            if (TextUtils.isEmpty(deviceId) && isWifiOnly(this.mContext)) {
                Rlog.d(LOG_TAG, "Current is wifi-only version, return SN number as DeviceId");
                deviceId = Build.SERIAL;
            }
            return deviceId;
        }
        Rlog.e(LOG_TAG, "getUniqueDeviceId can't read phone state.");
        return null;
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
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        try {
            deviceId = HwAESCryptoUtil.encrypt(HwAllInOneController.MASTER_PASSWORD, deviceId);
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "HwAESCryptoUtil encrypt excepiton");
        }
        editor.putString(sharedPref, deviceId);
        editor.commit();
    }

    private String getDeviceIdFromSP(String sharedPref) {
        String deviceId = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(sharedPref, null);
        try {
            return HwAESCryptoUtil.decrypt(HwAllInOneController.MASTER_PASSWORD, deviceId);
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

    public void testVoiceLoopBack(int mode) {
        enforceReadPermission();
        for (HwPhone phone : this.mPhones) {
            phone.testVoiceLoopBack(mode);
        }
    }

    private Phone getVSimPhone() {
        return HwVSimPhoneFactory.getVSimPhone();
    }

    public boolean getWaitingSwitchBalongSlot() {
        log("getWaitingSwitchBalongSlot start");
        return HwAllInOneController.getInstance().getWaitingSwitchBalongSlot();
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
        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
            return HwForeignUsimForTelecom.getInstance().isDomesticCard(slotId);
        }
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
        Phone phone = PhoneFactory.getPhone(SubscriptionController.getInstance().getDefaultDataSubId());
        if (ServiceState.isCdma(phone.getServiceState().getRilDataRadioTechnology())) {
            this.mServiceCellBand = new String[2];
            this.mServiceCellBand[0] = "CDMA";
            this.mServiceCellBand[1] = "0";
        } else {
            synchronized (this.mLock) {
                phone.mCi.queryServiceCellBand(this.mMainHandler.obtainMessage(6));
                boolean isWait = true;
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
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            this.mServiceCellBand = null;
        } else {
            this.mServiceCellBand = (String[]) ar.result;
        }
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
    }

    public boolean registerForRadioAvailable(IPhoneCallback callback) {
        log("registerForRadioAvailable");
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioAvailableIndCB = callback;
        this.mPhone.getPhone().mCi.registerForAvailable(this.mMainHandler, 51, null);
        return true;
    }

    private void handleRadioAvailableInd(Message msg) {
        AsyncResult ar = msg.obj;
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
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
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
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mRadioNotAvailableIndCB = callback;
        this.mPhone.getPhone().mCi.registerForNotAvailable(this.mMainHandler, 52, null);
        return true;
    }

    private void handleRadioNotAvailableInd(Message msg) {
        AsyncResult ar = msg.obj;
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
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
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
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
        if (this.mPhone == null) {
            loge("phone is null!");
            return false;
        }
        this.mImsaToMapconInfoCB = callback;
        this.mPhone.getPhone().mCi.registerCommonImsaToMapconInfo(this.mMainHandler, 53, null);
        return true;
    }

    private void handleCommonImsaToMapconInfoInd(Message msg) {
        AsyncResult ar = msg.obj;
        log("handleCommonImsaToMapconInfoInd");
        if (this.mImsaToMapconInfoCB == null) {
            loge("handleCommonImsaToMapconInfoInd mImsaToMapconInfoCB is null");
            return;
        }
        if (ar.exception == null) {
            byte[] data = ar.result;
            Bundle bundle = new Bundle();
            bundle.putByteArray("imsa2mapcon_msg", data);
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
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            this.mPhone = this.mPhones[1];
        } else {
            this.mPhone = this.mPhones[0];
        }
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
                removeRecord((IBinder) this.mRemoveRecordsList.get(i));
            }
            this.mRemoveRecordsList.clear();
        }
    }

    private void removeRecord(IBinder binder) {
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            for (int i = 0; i < recordCount; i++) {
                if (((Record) this.mRecords.get(i)).binder == binder) {
                    this.mRecords.remove(i);
                    return;
                }
            }
        }
    }

    private void notifyPhoneEventWithCallback(int phoneId, int event, int arg, Bundle bundle) {
        log("notifyPhoneEventWithCallback phoneId = " + phoneId + " event = " + event);
        synchronized (this.mRecords) {
            for (Record r : this.mRecords) {
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
        log("registerForPhoneEvent, phoneId = " + phoneId + ", events = " + Integer.toHexString(events));
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return false;
        } else if (this.mPhones[phoneId] == null) {
            loge("phone is null!");
            return false;
        } else {
            synchronized (this.mRecords) {
                Record r;
                IBinder b = callback.asBinder();
                int N = this.mRecords.size();
                for (int i = 0; i < N; i++) {
                    r = (Record) this.mRecords.get(i);
                    if (b == r.binder) {
                        break;
                    }
                }
                r = new Record();
                r.binder = b;
                this.mRecords.add(r);
                log("registerForPhoneEvent: add new record");
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
        removeRecord(callback.asBinder());
    }

    public boolean isRadioAvailable() {
        int phoneId;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            phoneId = 1;
        } else {
            phoneId = 0;
        }
        return isRadioAvailableByPhoneId(phoneId);
    }

    public boolean isRadioAvailableByPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return false;
        } else if (this.mPhones[phoneId] != null) {
            return this.mPhones[phoneId].isRadioAvailable();
        } else {
            loge("phone is null!");
            return false;
        }
    }

    public void setImsSwitch(boolean value) {
        int phoneId;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            phoneId = 1;
        } else {
            phoneId = 0;
        }
        setImsSwitchByPhoneId(phoneId, value);
    }

    public void setImsSwitchByPhoneId(int phoneId, boolean value) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
        } else if (this.mPhones[phoneId] == null) {
            loge("phone is null!");
        } else {
            this.mPhones[phoneId].setImsSwitch(value);
        }
    }

    public boolean getImsSwitch() {
        int phoneId;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            phoneId = 1;
        } else {
            phoneId = 0;
        }
        return getImsSwitchByPhoneId(phoneId);
    }

    public boolean getImsSwitchByPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return false;
        } else if (this.mPhones[phoneId] == null) {
            loge("phone is null!");
            return false;
        } else {
            boolean result = this.mPhones[phoneId].getImsSwitch();
            Rlog.d(LOG_TAG, "ImsSwitch = " + result);
            return result;
        }
    }

    public void setImsDomainConfig(int domainType) {
        int phoneId;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            phoneId = 1;
        } else {
            phoneId = 0;
        }
        setImsDomainConfigByPhoneId(phoneId, domainType);
    }

    public void setImsDomainConfigByPhoneId(int phoneId, int domainType) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
        } else if (this.mPhones[phoneId] == null) {
            loge("phone is null!");
        } else {
            this.mPhones[phoneId].setImsDomainConfig(domainType);
        }
    }

    public boolean handleMapconImsaReq(byte[] Msg) {
        int phoneId;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            phoneId = 1;
        } else {
            phoneId = 0;
        }
        return handleMapconImsaReqByPhoneId(phoneId, Msg);
    }

    public boolean handleMapconImsaReqByPhoneId(int phoneId, byte[] Msg) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return false;
        } else if (this.mPhones[phoneId] == null) {
            loge("phone is null!");
            return false;
        } else {
            this.mPhones[phoneId].handleMapconImsaReq(Msg);
            return true;
        }
    }

    public int getUiccAppType() {
        int phoneId;
        if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
            phoneId = 1;
        } else {
            phoneId = 0;
        }
        return getUiccAppTypeByPhoneId(phoneId);
    }

    public int getUiccAppTypeByPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return 0;
        } else if (this.mPhones[phoneId] != null) {
            return this.mPhones[phoneId].getUiccAppType();
        } else {
            loge("phone is null!");
            return 0;
        }
    }

    public int getImsDomain() {
        if (this.mPhone != null) {
            return getImsDomainByPhoneId(this.mPhone.getPhone().getPhoneId());
        }
        loge("phone is null!");
        return -1;
    }

    public int getImsDomainByPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return -1;
        } else if (this.mPhones[phoneId] == null) {
            loge("phone is null!");
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
        if (phoneId < 0 || phoneId >= this.mPhones.length) {
            loge("phoneId is invalid!");
            return null;
        } else if (this.mPhones[phoneId] != null) {
            return (UiccAuthResponse) sendRequest(CMD_UICC_AUTH, new UiccAuthPara(auth_type, rand, auth), Integer.valueOf(phoneId));
        } else {
            loge("phone is null!");
            return null;
        }
    }

    private Integer getPhoneId(Message msg) {
        Integer id = Integer.valueOf(-1);
        if (msg == null) {
            return id;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return id;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return id;
        }
        return ar.userObj;
    }

    private void initPrefNetworkTypeChecker() {
        TelephonyManager mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (mTelephonyManager != null && (mTelephonyManager.isMultiSimEnabled() ^ 1) != 0 && HwModemCapability.isCapabilitySupport(9)) {
            log("initPrefNetworkTypeChecker");
            if (SHOW_DIALOG_FOR_NO_SIM) {
                this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
            }
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    private void handleCmdImsGetDomain(Message msg) {
        MainThreadRequest request = msg.obj;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_IMS_GET_DOMAIN_DONE, request);
        if (request.subId != null) {
            this.mPhones[request.subId.intValue()].getImsDomain(onCompleted);
            return;
        }
        this.mPhone.getImsDomain(onCompleted);
    }

    private void handleCmdUiccAuth(Message msg) {
        MainThreadRequest request = msg.obj;
        UiccAuthPara para = request.argument;
        Message onCompleted = this.mMainHandler.obtainMessage(EVENT_UICC_AUTH_DONE, request);
        if (request.subId != null) {
            this.mPhones[request.subId.intValue()].handleUiccAuth(para.auth_type, para.rand, para.auth, onCompleted);
            return;
        }
        this.mPhone.handleUiccAuth(para.auth_type, para.rand, para.auth, onCompleted);
    }

    private void handleGetPrefNetworkTypeDone(Message msg) {
        Rlog.d(LOG_TAG, "handleGetPrefNetworkTypeDone");
        AsyncResult ar = msg.obj;
        if (ar.exception == null) {
            int prefNetworkType = ((int[]) ar.result)[0];
            int currentprefNetworkTypeInDB = getCurrentNetworkTypeFromDB();
            log("prefNetworkType:" + prefNetworkType + " currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            if (prefNetworkType == currentprefNetworkTypeInDB) {
                return;
            }
            if (currentprefNetworkTypeInDB == -1 || this.mPhone == null || this.mPhone.getPhone() == null) {
                log("INVALID_NETWORK_MODE in DB,set 4G-Switch on");
                setLteServiceAbility(1);
                return;
            }
            this.mPhone.setPreferredNetworkType(currentprefNetworkTypeInDB, this.mMainHandler.obtainMessage(5, currentprefNetworkTypeInDB, this.mPhone.getPhone().getSubId()));
            log("setPreferredNetworkType -> currentprefNetworkTypeInDB:" + currentprefNetworkTypeInDB);
            return;
        }
        log("getPreferredNetworkType exception=" + ar.exception);
    }

    private static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm != null) {
            return cm.isNetworkSupported(0) ^ 1;
        }
        return false;
    }

    public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) {
        enforceReadPermission();
        boolean isSuccess = handleWirelessStateRequest(1, type, slotId, callback);
        log("registerForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) {
        enforceReadPermission();
        boolean isSuccess = handleWirelessStateRequest(2, type, slotId, callback);
        log("unregisterForWirelessState type=" + type + ",isSuccess=" + isSuccess);
        return isSuccess;
    }

    public boolean setMaxTxPower(int type, int power) {
        Rlog.d(LOG_TAG, "setMaxTxPower: start=" + power);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (type == 2) {
            getCommandsInterface().setWifiTxPowerGrade(power, null);
        } else if (type == 1) {
            if (HwModemCapability.isCapabilitySupport(9)) {
                getCommandsInterface().setPowerGrade(power, null);
            } else {
                if (getCommandsInterface(0) != null) {
                    getCommandsInterface(0).setPowerGrade(power, null);
                }
                if (getCommandsInterface(1) != null) {
                    getCommandsInterface(1).setPowerGrade(power, null);
                }
            }
        }
        Rlog.d(LOG_TAG, "setMaxTxPower: end=" + power);
        return false;
    }

    private boolean handleWirelessStateRequest(int opertype, int type, int slotId, IPhoneCallback callback) {
        log("In handleWirelessStateRequest service type=" + type + ",slotId=" + slotId);
        boolean isSuccess = false;
        CommandsInterface ci = getCommandsInterface(slotId);
        if (callback == null) {
            loge("handleWirelessStateRequest callback is null.");
            return false;
        } else if (ci == null) {
            loge("handleWirelessStateRequest ci is null.");
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
            log("handleWirelessStateRequest type=" + type + ",isSuccess=" + isSuccess);
            return isSuccess;
        }
    }

    private boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < SIM_NUM;
    }

    private void handleSarInfoUploaded(int type, Message msg) {
        log("in handleSarInfoUploaded.");
        AsyncResult ar = msg.obj;
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
            ArrayList<Record> callbackList = callbackArray[slotId];
            for (int i = callbackList.size() - 1; i >= 0; i--) {
                Record r = (Record) callbackList.get(i);
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
        switch (type) {
            case 1:
                ci.closeSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
                isSuccess = true;
                break;
            case 2:
                ci.closeSwitchOfUploadAntOrMaxTxPower(2);
                isSuccess = true;
                break;
            case 4:
                ci.closeSwitchOfUploadAntOrMaxTxPower(4);
                isSuccess = true;
                break;
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
        switch (type) {
            case 1:
                callbackArray = this.mRegBandClassCallbackArray;
                break;
            case 2:
                callbackArray = this.mRegAntStateCallbackArray;
                break;
            case 4:
                callbackArray = this.mRegMaxTxPowerCallbackArray;
                break;
        }
        log("getCallbackArray type=" + type);
        return callbackArray;
    }

    private boolean unregisterUnitSarControl(int type, int slotId, CommandsInterface ci, IPhoneCallback callback) {
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            loge("unregisterUnitSarControl callbackArray is null.");
            return false;
        }
        boolean isSuccess;
        synchronized (callbackArray[slotId]) {
            ArrayList<Record> callbackList = callbackArray[slotId];
            IBinder b = callback.asBinder();
            int recordCount = callbackList.size();
            log("callbackArray[" + slotId + "] lenght is " + recordCount);
            for (int i = recordCount - 1; i >= 0; i--) {
                if (((Record) callbackList.get(i)).binder == b) {
                    log("unregisterUnitSarControl remove: " + ((Record) callbackList.get(i)));
                    callbackList.remove(i);
                    hasFind = true;
                    break;
                }
            }
            if (hasFind) {
                recordCount = callbackList.size();
                log("unregisterUnitSarControl record size = " + recordCount);
                if (recordCount == 0 && closeSarInfoUploadSwitch(type, slotId, ci)) {
                    isSuccess = unregisterSarRegistrant(type, slotId, ci);
                } else {
                    isSuccess = true;
                }
            } else {
                log("unregisterUnitSarControl not find the callback,type=" + type);
                isSuccess = true;
            }
        }
        return isSuccess;
    }

    private boolean registerUnitSarControl(int type, int slotId, CommandsInterface ci, IPhoneCallback callback) {
        log("registerUnitSarControl start slotId=" + slotId + ",type=" + type);
        boolean hasFind = false;
        Object[] callbackArray = getCallbackArray(type);
        if (callbackArray == null) {
            loge("registerUnitSarControl callbackArray is null.");
            return false;
        } else if (registerSarRegistrant(type, slotId, ci)) {
            synchronized (callbackArray[slotId]) {
                ArrayList<Record> callbackList = callbackArray[slotId];
                IBinder b = callback.asBinder();
                int N = callbackList.size();
                for (int i = 0; i < N; i++) {
                    if (b == ((Record) callbackList.get(i)).binder) {
                        hasFind = true;
                        break;
                    }
                }
                if (!hasFind) {
                    Record r = new Record();
                    r.binder = b;
                    r.callback = callback;
                    callbackList.add(r);
                    log("registerUnitSarControl: add new record");
                }
                log("registerUnitSarControl record size=" + callbackList.size());
            }
            return openSarInfoUploadSwitch(type, slotId, ci);
        } else {
            loge("registerUnitSarControl mPhones[" + slotId + "] register return false");
            return false;
        }
    }

    private boolean openSarInfoUploadSwitch(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        switch (type) {
            case 1:
                ci.openSwitchOfUploadBandClass(this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId)));
                isSuccess = true;
                break;
            case 2:
                isSuccess = ci.openSwitchOfUploadAntOrMaxTxPower(2);
                break;
            case 4:
                isSuccess = ci.openSwitchOfUploadAntOrMaxTxPower(4);
                break;
        }
        log("openSarInfoUploadSwitch mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + isSuccess);
        return isSuccess;
    }

    private boolean registerSarRegistrant(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        Message message = null;
        switch (type) {
            case 1:
                message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
                break;
            case 2:
                message = this.mMainHandler.obtainMessage(11, Integer.valueOf(slotId));
                break;
            case 4:
                message = this.mMainHandler.obtainMessage(12, Integer.valueOf(slotId));
                break;
        }
        if (message != null && ci.registerSarRegistrant(type, message)) {
            isSuccess = true;
        }
        loge("registerSarRegistrant mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + (isSuccess ^ 1));
        return isSuccess;
    }

    private boolean unregisterSarRegistrant(int type, int slotId, CommandsInterface ci) {
        boolean isSuccess = false;
        Message message = null;
        switch (type) {
            case 1:
                message = this.mMainHandler.obtainMessage(10, Integer.valueOf(slotId));
                break;
            case 2:
                message = this.mMainHandler.obtainMessage(11, Integer.valueOf(slotId));
                break;
            case 4:
                message = this.mMainHandler.obtainMessage(12, Integer.valueOf(slotId));
                break;
        }
        if (message != null && ci.unregisterSarRegistrant(type, message)) {
            isSuccess = true;
        }
        loge("unregisterSarRegistrant mPhones[" + slotId + "]: type = " + type + ",isSuccess = " + (isSuccess ^ 1));
        return isSuccess;
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        try {
            enforceReadPermission();
            boolean res = false;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                for (HwPhone phone : this.mPhones) {
                    if (phone != null && phone.isCDMAPhone()) {
                        if (phone.cmdForECInfo(event, action, buf)) {
                            res = true;
                        } else {
                            res = requestForECInfo(phone, event, action, buf);
                        }
                    }
                }
            } else if (this.mPhone != null && this.mPhone.isCDMAPhone()) {
                res = !this.mPhone.cmdForECInfo(event, action, buf) ? requestForECInfo(this.mPhone, event, action, buf) : true;
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

    private void handleEventQueryEncryptCall(HwPhone phone) {
        int i = 1;
        if (phone != null && phone.isCDMAPhone()) {
            byte[] req = new byte[]{(byte) 0};
            if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                this.mSupportEncryptCall = phone.cmdForECInfo(7, 0, req);
                if (this.mSupportEncryptCall) {
                    SystemProperties.set("persist.sys.cdma_encryption", Boolean.toString(this.mSupportEncryptCall));
                }
                CommandsInterface ci = getCommandsInterface(phone.getPhone().getPhoneId());
                if (ci != null) {
                    if (!((HwQualcommRIL) ci).getEcCdmaCallVersion()) {
                        i = 0;
                    }
                    this.mEncryptCallStatus = i;
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
            Rlog.d(LOG_TAG, "query EncryptCall Count : " + queryCount);
        }
    }

    private boolean requestForECInfo(HwPhone phone, int event, int action, byte[] buf) {
        EncryptCallPara para;
        boolean z = true;
        switch (event) {
            case 3:
            case 5:
            case 6:
            case 7:
                para = new EncryptCallPara(phone, event, null);
                break;
            default:
                para = new EncryptCallPara(phone, event, buf);
                break;
        }
        byte[] res = (byte[]) sendRequest(200, para);
        int len = res.length;
        if (len == 1) {
            if (res[0] <= (byte) 0 || ((byte) (res[0] & 15)) != (byte) 1) {
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
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        if (slotId < 0 || slotId >= numPhones) {
            return false;
        }
        return this.mPhones[slotId].isCtSimCard();
    }

    public void notifyCModemStatus(int status, IPhoneCallback callback) {
        if (Binder.getCallingUid() == HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE) {
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

    private void checkEcSwitchStatusInNV(HwPhone phone, int statusInNV) {
        if (phone != null && (phone.isCDMAPhone() ^ 1) == 0) {
            int statusInDB = Secure.getInt(this.mContext.getContentResolver(), "encrypt_version", 0);
            log("checkEcSwitchStatus, encryptCall statusInNV=" + statusInNV + " statusInDB=" + statusInDB);
            if (statusInNV != statusInDB) {
                int action = statusInDB;
                byte[] buf = new byte[]{(byte) statusInDB};
                if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
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
        if (Binder.getCallingUid() != HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE) {
            Rlog.e(LOG_TAG, "getCallingUid() != Process.SYSTEM_UID, return");
            return;
        }
        Rlog.d(LOG_TAG, "notifyCellularCommParaReady");
        if (1 == paratype) {
            if (TelephonyManager.getDefault().isMultiSimEnabled() && getDefault4GSlotId() == 1) {
                this.mPhone = this.mPhones[1];
            } else {
                this.mPhone = this.mPhones[0];
            }
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
        int airplaneMode = Global.getInt(this.phone.getContext().getContentResolver(), "airplane_mode_on", 0);
        if (1 == airplaneMode) {
            log("airplaneMode : " + airplaneMode);
            return false;
        }
        State mExternalState = this.phone.getIccCard().getState();
        if (mExternalState == State.PIN_REQUIRED || mExternalState == State.PUK_REQUIRED) {
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
            } else if (pinState || (enablePinLock ^ 1) == 0) {
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

    private void clearSinglePolicyData(Context context, String timeMode, boolean isOutgoing) {
        log("clearSinglePolicyData: " + timeMode);
        if (HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(isOutgoing)) {
            if (!TextUtils.isEmpty(timeMode)) {
                String policyName = isOutgoing ? OUTGOING_SMS_LIMIT : INCOMING_SMS_LIMIT;
                if (timeMode.equals(DAY_MODE)) {
                    removeSharedDayModeData(context, policyName, USED_OF_DAY, DAY_MODE_TIME);
                } else if (timeMode.equals(WEEK_MODE)) {
                    removeSharedDayModeData(context, policyName, USED_OF_WEEK, WEEK_MODE_TIME);
                } else if (timeMode.equals(MONTH_MODE)) {
                    removeSharedDayModeData(context, policyName, USED_OF_MONTH, MONTH_MODE_TIME);
                }
            }
            return;
        }
        clearAllPolicyData(context, isOutgoing);
    }

    private void removeSharedDayModeData(Context context, String policyName, String keyUsedNum, String keyTime) {
        if (!TextUtils.isEmpty(policyName) && context != null) {
            Editor editor = context.getSharedPreferences(policyName, 0).edit();
            editor.remove(keyUsedNum);
            editor.remove(keyTime);
            editor.commit();
        }
    }

    private void clearAllPolicyData(Context context) {
        clearAllPolicyData(context, true);
        clearAllPolicyData(context, false);
    }

    private void clearAllPolicyData(Context context, boolean isOutgoing) {
        if (!HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(isOutgoing) && context != null) {
            Editor editor = context.getSharedPreferences(isOutgoing ? OUTGOING_SMS_LIMIT : INCOMING_SMS_LIMIT, 0).edit();
            editor.clear();
            editor.commit();
        }
    }

    private void registerSetRadioCapDoneReceiver() {
        if (IS_DUAL_IMS_SUPPORTED) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
            if (this.mContext != null) {
                this.mContext.registerReceiver(this.mSetRadioCapDoneReceiver, filter);
            }
        }
    }

    private void handleSwitchSlotDone(Intent intent) {
        int switchSlotStep = intent.getIntExtra(HW_SWITCH_SLOT_STEP, -99);
        if (!HwAllInOneController.IS_CMCC_4GSWITCH_DISABLE && 1 == switchSlotStep) {
            if (isDualImsSwitchOpened()) {
                log("handleSwitchSlotDone. dual ims switch open, return.");
            } else if (1 == intent.getIntExtra(HwAllInOneController.IF_NEED_SET_RADIO_CAP, 0)) {
                log("handleSwitchSlotDone. not happen real sim slot change, return.");
            } else {
                int networkTypeForSub1 = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode0", -1);
                int networkTypeForSub2 = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode1", -1);
                log("handleSwitchSlotDone: sub1 is " + networkTypeForSub1 + ", sub2 is " + networkTypeForSub2);
                if (-1 != networkTypeForSub1 && -1 != networkTypeForSub2) {
                    if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0) {
                        if (isLteServiceOn(networkTypeForSub1)) {
                            networkTypeForSub1 = 3;
                            log("handleSwitchSlotDone: sub2 is slave sim card, shouldn't have LTE ability.");
                        }
                    } else if (isLteServiceOn(networkTypeForSub2)) {
                        networkTypeForSub2 = 3;
                        log("handleSwitchSlotDone: sub1 is slave sim card, shouldn't have LTE ability.");
                    }
                    saveNetworkTypeToDB(0, networkTypeForSub2);
                    saveNetworkTypeToDB(1, networkTypeForSub1);
                }
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

    public boolean setDmRcsConfig(int subId, int rcsCapability, int devConfig, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(subId)) {
            subId = getDefault4GSlotId();
        }
        CommandsInterface ci = getCommandsInterface(subId);
        if (ci == null) {
            loge("setDmRcsConfig: ci is null.");
            return false;
        }
        ci.setDmRcsConfig(rcsCapability, devConfig, response);
        return true;
    }

    public boolean setRcsSwitch(int subId, int switchState, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(subId)) {
            subId = getDefault4GSlotId();
        }
        CommandsInterface ci = getCommandsInterface(subId);
        if (ci == null) {
            loge("setRcsSwitch: ci is null.");
            return false;
        }
        ci.setRcsSwitch(switchState, response);
        return true;
    }

    public boolean getRcsSwitchState(int subId, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(subId)) {
            subId = getDefault4GSlotId();
        }
        CommandsInterface ci = getCommandsInterface(subId);
        if (ci == null) {
            loge("getRcsSwitchState: ci is null.");
            return false;
        }
        ci.getRcsSwitchState(response);
        return true;
    }

    public boolean setDmPcscf(int subId, String pcscf, Message response) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        if (!isValidSlotId(subId)) {
            subId = getDefault4GSlotId();
        }
        CommandsInterface ci = getCommandsInterface(subId);
        if (ci == null) {
            loge("setDmPcscf: ci is null.");
            return false;
        }
        ci.setDmPcscf(pcscf, response);
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
        Phone phone = getPhone(subId);
        if (phone != null) {
            phone.setImsRegistrationState(registered);
        }
    }

    public boolean isImsRegisteredForSubId(int subId) {
        Phone phone = getPhone(subId);
        if (phone != null) {
            return phone.isImsRegistered();
        }
        return false;
    }

    public boolean isWifiCallingAvailableForSubId(int subId) {
        Phone phone = getPhone(subId);
        if (phone != null) {
            return phone.isWifiCallingEnabled();
        }
        return false;
    }

    public boolean isVolteAvailableForSubId(int subId) {
        Phone phone = getPhone(subId);
        if (phone != null) {
            return phone.isVolteEnabled();
        }
        return false;
    }

    public boolean isVideoTelephonyAvailableForSubId(int subId) {
        Phone phone = getPhone(subId);
        if (phone != null) {
            return phone.isVideoEnabled();
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

    private void handleSendLaaCmdDone(Message msg) {
        AsyncResult ar = msg.obj;
        int result = -1;
        if (ar != null && ar.exception == null) {
            result = 0;
        }
        sendResponseToTarget(this.sendLaaCmdCompleteMsg, result);
        log("handleSendLaaCmdDone:sendLaaCmd result is " + result);
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

    private void handleGetLaaStateDone(Message msg) {
        AsyncResult ar = msg.obj;
        int result = -1;
        if (ar != null && ar.exception == null && (ar.result instanceof int[])) {
            result = ((int[]) ar.result)[0];
        }
        sendResponseToTarget(this.getLaaStateCompleteMsg, result);
        log("handleGetLaaStateDone getLaaDetailedState result is " + result);
    }

    private void sendResponseToTarget(Message response, int result) {
        if (response != null && response.getTarget() != null) {
            response.arg1 = result;
            response.sendToTarget();
        }
    }

    private void handleSendSimlockData(Message msg) {
        AsyncResult ar = msg.obj;
        if (ar.exception != null) {
            log("REQUEST_SEND_SIM_LOCK_NW_DATA: ar.exception=" + ar.exception);
            return;
        }
        int[] result = ar.result;
        if (result != null && result.length == 1 && result[0] == 0) {
            log("write success once");
            if (msg.arg1 == LENGTH_LOCK_DATA_LAYER - 1) {
                log("the last layer lock data finish writing");
                return;
            } else {
                writeSingleLayerSimLockNwData(msg.arg1 + 1);
                return;
            }
        }
        log("write failure");
    }

    public void registerForCallAltSrv(int subId, IPhoneCallback callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        Phone phone = PhoneFactory.getPhone(subId);
        if (phone == null || callback == null) {
            log("registerForCallAltSrv:phone or callback is null,return");
            return;
        }
        log("registerForCallAltSrv for subId=" + subId);
        phone.registerForCallAltSrv(phone, EVENT_CELLULAR_CLOUD_PARA_UPGRADE_DONE, callback);
    }

    public void unregisterForCallAltSrv(int subId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        Phone phone = PhoneFactory.getPhone(subId);
        if (phone == null) {
            log("unregisterForCallAltSrv:phone or callback is null,return");
            return;
        }
        log("unregisterForCallAltSrv for subId=" + subId);
        phone.unregisterForCallAltSrv(phone);
    }

    public String getImsImpu(int subId) {
        log("getImsImpu for subId " + subId);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", null);
        Phone phone = getPhone(subId);
        if (phone == null) {
            return null;
        }
        ImsPhone imsPhone = (ImsPhone) phone.getImsPhone();
        if (imsPhone != null) {
            return imsPhone.getImsImpu();
        }
        return null;
    }

    public void writeSimLockNwData(String[] data) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (data == null || data.length != (LENGTH_LOCK_DATA_LAYER * 2) + 1) {
            log("data invalid");
            return;
        }
        this.simLockFinalDatas = data;
        SystemProperties.set("persist.sys.kddi_opName", this.simLockFinalDatas[this.simLockFinalDatas.length - 1]);
        log("start to write simlock data to modem");
        writeSingleLayerSimLockNwData(0);
    }

    /* JADX WARNING: Missing block: B:6:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeSingleLayerSimLockNwData(int layer) {
        if ((layer == 0 || (this.simLockFinalDatas != null && this.simLockFinalDatas.length == (LENGTH_LOCK_DATA_LAYER * 2) + 1)) && layer < LENGTH_LOCK_DATA_LAYER) {
            String lockData = "";
            int field = layer;
            if (!TextUtils.isEmpty(this.simLockFinalDatas[layer])) {
                lockData = this.simLockFinalDatas[layer];
            } else if (TextUtils.isEmpty(this.simLockFinalDatas[LENGTH_LOCK_DATA_LAYER + layer])) {
                log("empty data can not be written");
                return;
            } else {
                field = layer + LENGTH_LOCK_DATA_LAYER;
                lockData = this.simLockFinalDatas[LENGTH_LOCK_DATA_LAYER + layer];
            }
            Message msg = this.mMainHandler.obtainMessage(REQUEST_SEND_SIM_LOCK_NW_DATA);
            msg.arg1 = layer;
            getCommandsInterface().writeSimLockNwData(field, lockData, msg);
        }
    }
}
