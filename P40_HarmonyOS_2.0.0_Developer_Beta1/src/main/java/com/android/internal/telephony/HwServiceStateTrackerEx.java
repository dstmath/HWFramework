package com.android.internal.telephony;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.hardware.display.DisplayManager;
import android.icu.util.TimeZone;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.WorkSource;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwTelephonyManagerInnerUtils;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.cdma.HwCdmaOnsDisplayParamsManager;
import com.android.internal.telephony.cdma.HwCdmaSignalStrengthManager;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.gsm.HwGsmOnsDisplayParamsManager;
import com.android.internal.telephony.gsm.HwGsmSignalStrengthManager;
import com.android.internal.telephony.timezone.HwTimeZoneManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.app.NotificationEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.WorkSourceEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.HwLogEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.NetworkRegistrationInfoEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.TimeServiceHelperEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.uicc.UiccProfileEx;
import com.huawei.libcore.timezone.TimeZoneFinderEx;
import com.huawei.rms.iaware.NetLocationStrategyEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class HwServiceStateTrackerEx extends DefaultHwServiceStateTrackerEx {
    private static final String ACTION_TIMEZONE_SELECTION = "com.huawei.intent.action.ACTION_TIMEZONE_SELECTION";
    private static final int AIRPLANE_MODE_ON = 1;
    private static final int AIR_MODE_OFF = 0;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String CHR_DATA = "chr_data";
    private static final String CHR_PACKAGE_NAME = "com.huawei.android.chr";
    private static final String CLOUD_OTA_DPLMN_UPDATE = "cloud.ota.dplmn.UPDATE";
    private static final String CLOUD_OTA_MCC_UPDATE = "cloud.ota.mcc.UPDATE";
    private static final String CLOUD_OTA_PERMISSION = "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA";
    private static final String CT_MACAO_MCC = "455";
    private static final int CT_MACAO_SID_END = 11311;
    private static final int CT_MACAO_SID_START = 11296;
    private static final String CT_MCC = "460";
    private static final int DATA_OFF_TIME_OUT = SystemPropertiesEx.getInt("ro.config.dataoff_timeout", 30);
    private static final int DEFAULT_DELAY_POWER_OFF_MISEC = 2000;
    private static final int DEFAULT_SID = 0;
    private static final int DEFAULT_TIMER_VALUE = 0;
    private static final int DELAY_TIME_TO_NOTIF_NITZ = 60000;
    private static final int DISABLED = 0;
    private static final String DUAL_CARDS_SETTINGS_ACTIVITY = "com.huawei.settings.intent.DUAL_CARD_SETTINGS";
    private static final int DUAL_SIM = 2;
    private static final int ENABLED = 1;
    private static final int EVENT_256QAM_STATE_CHANGE = 116;
    private static final int EVENT_4R_MIMO_ENABLE = 117;
    private static final int EVENT_CA_STATE_CHANGED = 102;
    private static final int EVENT_CHECK_PREFERRED_NETWORK_TYPE = 119;
    private static final int EVENT_CRR_CONN = 111;
    public static final int EVENT_DELAY_RAT_CHANGED = 121;
    private static final int EVENT_DSDS_MODE = 114;
    private static final int EVENT_GET_RRC_CONNECTION_STATE = 124;
    private static final int EVENT_GET_SIGNAL_STRENGTH = 122;
    private static final int EVENT_ICC_RECORDS_EONS_UPDATED = 103;
    private static final int EVENT_MCC_CHANGED = 115;
    private static final int EVENT_NETWORK_REJECTED_CASE = 112;
    private static final int EVENT_NETWORK_REJINFO_DONE = 109;
    private static final int EVENT_NETWORK_REJINFO_TIMEOUT = 110;
    private static final int EVENT_NITZ_CAPABILITY_NOTIFICATION = 105;
    private static final int EVENT_PLMN_SELINFO = 113;
    private static final int EVENT_POLL_LOCATION_INFO = 104;
    private static final int EVENT_RAT_CHANGED = 123;
    private static final int EVENT_RESUME_DATA = 106;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 101;
    private static final int EVENT_RRC_CONNECTION_STATE_CHAHGE = 120;
    private static final int EVENT_SET_PRE_NETWORKTYPE = 107;
    private static final int EVENT_SIM_HOTPLUG = 108;
    private static final int EVENT_VOICE_CALL_ENDED = 118;
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_SST_IN_SERVICE = "isSstInService";
    private static final String EXTRA_WIFI = "wifi";
    private static final String HWNFF_RSSI_SIM1 = "gsm.rssi.sim1";
    private static final String HWNFF_RSSI_SIM2 = "gsm.rssi.sim2";
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final int HW_SWITCH_SLOT_FAIL = -1;
    private static final int INDEX_4GPLUS_256QAM = 1;
    private static final int INDEX_4GPLUS_MAX = 2;
    private static final int INDEX_4GPLUS_MIMO = 0;
    private static final String INTENT_NR_CLEAR_SCG_CELL_REPORT = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    private static final String INTERNATIONAL_MCC = "901";
    private static final int INTERVAL_TIME = 120000;
    private static final String INVAILD_PLMN = "1023127-123456-1023456-123127-99999-";
    private static final int INVALID_CID = 0;
    private static final int INVALID_NUMBER = -1;
    private static final boolean IS_FEATURE_RECOVER_AUTO_NETWORK_MODE = SystemPropertiesEx.getBoolean("ro.hwpp.recover_auto_mode", false);
    private static final boolean IS_KEEP_3GPLUS_HPLUS = SystemPropertiesEx.getBoolean("ro.config.keep_3gplus_hplus", false);
    private static final boolean IS_KEEP_NW_SEL_MANUAL = SystemPropertiesEx.getBoolean("ro.config.hw_keep_sel_manual", false);
    private static final boolean IS_MIMO_4R_REPORT = SystemPropertiesEx.getBoolean("ro.config.hw_4.5gplus", false);
    private static final boolean IS_MULTI_SIM_ENABLED = TelephonyManagerEx.isMultiSimEnabled();
    private static final boolean IS_PS_CLEARCODE = SystemPropertiesEx.getBoolean("ro.config.hw_clearcode_pdp", false);
    private static final boolean IS_SCREEN_OFF_NOT_UPDATE_LOCATION = SystemPropertiesEx.getBoolean("ro.config.updatelocation", false);
    private static final boolean IS_SHOW_4G_PLUS_ICON = SystemPropertiesEx.getBoolean("ro.config.hw_show_4G_Plus_icon", false);
    private static final boolean IS_SHOW_REJ_INFO_KT = SystemPropertiesEx.getBoolean("ro.config.show_rej_info", false);
    private static final boolean IS_VERIZON = ("389".equals(SystemPropertiesEx.get("ro.config.hw_opta")) && "840".equals(SystemPropertiesEx.get("ro.config.hw_optb")));
    private static final boolean IS_VOICE_REG_STATE_FOR_ONS = "true".equals(SystemPropertiesEx.get("ro.hwpp.IS_VOICE_REG_STATE_FOR_ONS", "false"));
    private static final String LAST_SCG_CELL_INFOS = "last_scg_cell_infos";
    private static final String LOG_TAG = "HwServiceStateTrackerEx";
    private static final int MAX_DATAOFF_DEFAULT_SECOND = 30;
    private static final int MAX_DELAY_POWER_OFF_MISEC = 6000;
    private static final int MAX_TOP_PACKAGE = 10;
    private static final String MCC_INDIAN_1 = "404";
    private static final String MCC_INDIAN_2 = "405";
    private static final int MCC_LENGTH = 3;
    private static final int MCC_MNC_MIN_LENGTH = 5;
    private static final int MIN_DATAOFF_TIMEOUT = 1000;
    private static final long MODE_REQUEST_CELL_LIST_STRATEGY_INVALID = -1;
    private static final long MODE_REQUEST_CELL_LIST_STRATEGY_VALID = 0;
    private static final String MODULE_ID = "module_id";
    private static final int MODULE_ID_TELEPHONY = 26000;
    private static final int MS_PER_SECONE = 1000;
    private static final int[] NETWORK_REJINFO_CAUSES = {7, 11, 12, 13, 14, 15, 17};
    private static final int NETWORK_REJINFO_MAX_COUNT = 3;
    private static final int NETWORK_REJINFO_MAX_TIME = 1200000;
    private static final String NETWORK_REJINFO_NOTIFY_CHANNEL = "network_rejinfo_notify_channel";
    private static final int NITZ_UPDATE_SPACING_TIME = 1800000;
    private static final String NR_TECHNOLOGY_CONFIG = "persist.radio.nsa_display_config";
    private static final String NR_TECHNOLOGY_CONFIGA = "ConfigA";
    private static final String NR_TECHNOLOGY_CONFIGAD = "ConfigAD";
    private static final String NR_TECHNOLOGY_CONFIGB = "ConfigB";
    private static final String NR_TECHNOLOGY_CONFIGC = "ConfigC";
    private static final String NR_TECHNOLOGY_CONFIGD = "ConfigD";
    private static final String OPTIMIZED_FOR_CONFIGD = "persist.radio.optimized_for_config_d";
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final String RAT_CHANGED_DELAY_TIMER = "persist.radio.rat_changed_delay_timer";
    private static final int RAT_LTE = 2;
    private static final int REJCODE_DENIED = 10;
    private static final int REJINFO_LEN = 4;
    private static final int REJ_TIMES = 3;
    private static final int RESUME_DATA_TIME = 8000;
    private static final int RRC_STATE_CONNECTED = 1;
    private static final int RRC_STATE_IDLE = 0;
    private static final int RRC_STATE_INVALID = 3;
    private static final int RRC_STATE_NOT_CAMPED = 4;
    private static final int SCG_CELL_CACHE_ITEM_SIZE = 2;
    private static final List<Integer> SCG_CELL_ID_HAS_REPORTED = new ArrayList();
    private static final int SECOND_TO_MILLISECOND = 1000;
    private static final int SEC_TO_MILL_SEC = 1000;
    private static final int SET_PRE_NETWORK_TIME = 5000;
    private static final int SET_PRE_NETWORK_TIME_DELAY = 2000;
    private static final int SORT_NUM_OF_FIRST_TIMER = 0;
    private static final int SORT_NUM_OF_SECOND_TIMER = 1;
    private static final String SUB_ID = "sub_id";
    private static final int TRANSPORT_TYPE_WWAN = 1;
    private static final int VALID_TIMER_ARRAY_LENGTH = 2;
    private static final long VALUE_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    private static final int VALUE_SCREEN_OFF_TIME_DEFAULT = 10;
    private static String sEnableGetLoaction = null;
    private static HwServiceStateTrackerEx[] sInstances;
    private BaseBroadcastReceiver mBaseIntentReceiver;
    private long mCellIdCacheTimer = 4320000;
    private int mCellIdListSize = 32;
    private Map<Integer, Long> mCellIdMap = new ConcurrentHashMap();
    private Map<String, Integer> mCellInfoMap = new HashMap();
    private int mChangedNsaState = 0;
    private CommandsInterfaceEx mCi;
    private CloudOtaBroadcastReceiver mCloudOtaBroadcastReceiver;
    private Context mContext;
    private ContentResolver mCr;
    private String mCurWifi = BuildConfig.FLAVOR;
    private final DisplayManager.DisplayListener mDisplayListener = new SstExDisplayListener();
    private Handler mHandler;
    private HwCdmaOnsDisplayParamsManager mHwCdmaOnsDisplayParamsManager;
    private HwCdmaSignalStrengthManager mHwCdmaSignalStengthManager;
    private HwGsmOnsDisplayParamsManager mHwGsmOnsDisplayParamsManager;
    private HwGsmSignalStrengthManager mHwGsmSignalStengthManager;
    private final ArrayList<HwServiceStateListener> mHwServiceStateListenerList = new ArrayList<>();
    private int mHwnffValueSim1 = 0;
    private int mHwnffValueSim2 = 0;
    private boolean mIsCurShowWifi = false;
    private boolean mIsNeedHandleRejInfoFlag = true;
    private boolean mIsSetPreNwTypeRequested = false;
    private int mLastCid = -1;
    private int mLastLac = -1;
    private int mLastType = -1;
    private boolean[] mMplStatus;
    private boolean mNeedDelayUpdate = true;
    private List<String> mNetworkRejInfoLists = new ArrayList();
    private int mOldCaState = 0;
    private String mOldRplmn;
    private int mPendingPreNwType = 0;
    private Message mPendingSavedMessage;
    private PhoneExt mPhone;
    private int mPhoneId;
    private int mPreDataRat = 0;
    private int mPreRrcConState = 0;
    private int mRac = -1;
    private BroadcastReceiver mReceiverForDualSim = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwServiceStateTrackerEx.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot", -1);
                ServiceState serviceState = ServiceStateEx.newFromBundle(intent.getExtras());
                if (slotId == HwServiceStateTrackerEx.this.mPhone.getPhoneId() && serviceState.getState() == 0) {
                    HwServiceStateTrackerEx.this.clearNetworkRejInfo();
                    NotificationManager mNotificationManager = (NotificationManager) HwServiceStateTrackerEx.this.mContext.getSystemService("notification");
                    if (mNotificationManager != null) {
                        mNotificationManager.cancel(HwServiceStateTrackerEx.LOG_TAG, slotId);
                    }
                }
                int subId = intent.getIntExtra("subscription", -1);
                if (slotId == HwServiceStateTrackerEx.this.mPhone.getPhoneId() && subId != -1 && HwServiceStateTrackerEx.this.mServiceStateTracker != null) {
                    HwServiceStateTrackerEx.this.mServiceStateTracker.setRetryNotifySsState(false);
                }
            }
        }
    };
    private int mRejCause = -1;
    private boolean mReregisteredResultFlag = false;
    private String mRplmn;
    private int mRrcConnectionState = 0;
    private IServiceStateTrackerInner mServiceStateTracker;
    private long mStartTime = MODE_REQUEST_CELL_LIST_STRATEGY_VALID;
    private String mTag = LOG_TAG;
    private int rejNum = 0;

    /* access modifiers changed from: private */
    public enum HotplugState {
        STATE_PLUG_OUT,
        STATE_PLUG_IN
    }

    public interface HwServiceStateListener {
        void onServiceStateChanged(int i, ServiceState serviceState);

        void onSignalStrengthChanged(int i, SignalStrength signalStrength);
    }

    public HwServiceStateTrackerEx(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt) {
        logi("construct for phone " + phoneExt.getPhoneId());
        this.mServiceStateTracker = serviceStateTracker;
        this.mPhone = phoneExt;
        this.mPhoneId = this.mPhone.getPhoneId();
        this.mTag += "[" + this.mPhoneId + "]";
        this.mHwGsmOnsDisplayParamsManager = new HwGsmOnsDisplayParamsManager(serviceStateTracker, phoneExt, this);
        this.mHwCdmaOnsDisplayParamsManager = new HwCdmaOnsDisplayParamsManager(serviceStateTracker, phoneExt, this);
        this.mHwGsmSignalStengthManager = new HwGsmSignalStrengthManager(serviceStateTracker, phoneExt, this);
        this.mHwCdmaSignalStengthManager = new HwCdmaSignalStrengthManager(serviceStateTracker, phoneExt, this);
        this.mCi = this.mPhone.getCi();
        this.mContext = this.mPhone.getContext();
        this.mCr = this.mContext.getContentResolver();
        this.mHandler = new MyHandler();
        this.mCi.registerForRplmnsStateChanged(this.mHandler, (int) EVENT_RPLMNS_STATE_CHANGED, (Object) null);
        this.mCi.registerForCaStateChanged(this.mHandler, (int) EVENT_CA_STATE_CHANGED, (Object) null);
        this.mCi.registerForCrrConn(this.mHandler, (int) EVENT_CRR_CONN, (Object) null);
        this.mCi.setOnNetReject(this.mHandler, (int) EVENT_NETWORK_REJECTED_CASE, (Object) null);
        this.mCi.setOnRegPLMNSelInfo(this.mHandler, (int) EVENT_PLMN_SELINFO, (Object) null);
        this.mCi.registerForDSDSMode(this.mHandler, (int) EVENT_DSDS_MODE, (Object) null);
        this.mPhone.registerForMccChanged(this.mHandler, (int) EVENT_MCC_CHANGED, (Object) null);
        this.mPhone.registerForVoiceCallEnded(this.mHandler, (int) EVENT_VOICE_CALL_ENDED, (Object) null);
        this.mMplStatus = new boolean[2];
        this.mCi.registerFor256QAMStatus(this.mHandler, (int) EVENT_256QAM_STATE_CHANGE, (Object) null);
        this.mCi.registerForUnsol4RMimoStatus(this.mHandler, (int) EVENT_4R_MIMO_ENABLE, (Object) null);
        initNetworkRejInfo();
        loadAllowUpdateLocationPackage();
        registerCloudOtaBroadcastReceiver();
        int phoneCount = TelephonyManagerEx.getDefault().getPhoneCount();
        phoneCount = HwTelephonyManager.getDefault().isPlatformSupportVsim() ? phoneCount + 1 : phoneCount;
        if (sInstances == null) {
            sInstances = new HwServiceStateTrackerEx[phoneCount];
        }
        int i = this.mPhoneId;
        if (i >= 0 && i < phoneCount) {
            sInstances[i] = this;
        }
        addBroadcastReceiver();
        this.mCi.registerForRrcConnectionStateChange(this.mHandler, 120, (Object) null);
        this.mServiceStateTracker.registerForDataRegStateOrRatChanged(1, this.mHandler, (int) EVENT_RAT_CHANGED, (Object) null);
        ((DisplayManager) this.mPhone.getContext().getSystemService("display")).registerDisplayListener(this.mDisplayListener, null);
    }

    public static synchronized HwServiceStateTrackerEx getInstance(int phoneId) {
        synchronized (HwServiceStateTrackerEx.class) {
            if (sInstances == null) {
                return null;
            }
            if (phoneId < 0 || phoneId > TelephonyManagerEx.getDefault().getPhoneCount()) {
                return null;
            }
            return sInstances[phoneId];
        }
    }

    private static boolean isContainPackage(String data, String packageName) {
        String[] enablePackages = null;
        if (!TextUtils.isEmpty(data)) {
            enablePackages = data.split(";");
        }
        if (enablePackages == null || enablePackages.length == 0) {
            return false;
        }
        for (int i = 0; i < enablePackages.length; i++) {
            if (!TextUtils.isEmpty(packageName) && packageName.equals(enablePackages[i])) {
                return true;
            }
        }
        return false;
    }

    private static void slogd(String str) {
        RlogEx.d(LOG_TAG, str);
    }

    public void setVowifi(boolean isShowWifi, String curWifi) {
        this.mIsCurShowWifi = isShowWifi;
        this.mCurWifi = curWifi;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRplmnsStateChanged(Message msg) {
        logd("EVENT_RPLMNS_STATE_CHANGED, isGsm = " + this.mPhone.isPhoneTypeGsm());
        this.mOldRplmn = this.mRplmn;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getResult() == null || !(ar.getResult() instanceof String)) {
            this.mRplmn = SystemPropertiesEx.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, BuildConfig.FLAVOR);
        } else {
            this.mRplmn = (String) ar.getResult();
        }
        logd("EVENT_RPLMNS_STATE_CHANGED, rplmn = " + this.mRplmn);
        if (this.mPhone.isPhoneTypeGsm()) {
            setNetworkSelectionModeAutomaticHw(this.mOldRplmn, this.mRplmn);
            String mcc = BuildConfig.FLAVOR;
            String oldMcc = BuildConfig.FLAVOR;
            String str = this.mRplmn;
            if (str != null && str.length() > 3) {
                mcc = this.mRplmn.substring(0, 3);
            }
            String str2 = this.mOldRplmn;
            if (str2 != null && str2.length() > 3) {
                oldMcc = this.mOldRplmn.substring(0, 3);
            }
            if (!BuildConfig.FLAVOR.equals(mcc) && !mcc.equals(oldMcc)) {
                this.mPhone.notifyMccChanged(mcc);
                logd("rplmn mcc changed.");
            }
            if (SystemPropertiesEx.getBoolean("ro.config.hw_enable_ota_bip_cust", false) && this.mPhone.getDcTracker() != null) {
                this.mPhone.getDcTracker().checkPLMN(this.mRplmn);
            }
            if (SystemPropertiesEx.getBoolean("ro.config.hw_globalEcc", true)) {
                logd("the global emergency numbers custom-make does enable!!!!");
                toGetRplmnsThenSendEccNumGsm();
            }
        } else if (SystemPropertiesEx.getBoolean("ro.config.hw_globalEcc", false) && SystemPropertiesEx.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
            logd("the global emergency numbers custom-make does enable!!!!");
            toGetRplmnsThenSendEccNumCdma();
        }
    }

    private void setNetworkSelectionModeAutomaticHw(String oldRplmn, String rplmn) {
        String autoSelectMccs = Settings.System.getString(this.mContext.getContentResolver(), "hw_auto_select_network_mcc");
        if (!TextUtils.isEmpty(autoSelectMccs) && !TextUtils.isEmpty(oldRplmn) && !TextUtils.isEmpty(rplmn) && oldRplmn.length() >= 3 && rplmn.length() >= 3) {
            PhoneExt phoneExt = this.mPhone;
            if (phoneExt == null || phoneExt.getServiceState() == null || !this.mPhone.getServiceState().getIsManualSelection()) {
                logd("setNetworkSelectionModeAutomaticHw - already auto, ignoring.");
                return;
            }
            String[] mccs = autoSelectMccs.split(",");
            String oldMcc = oldRplmn.substring(0, 3);
            String newMcc = rplmn.substring(0, 3);
            boolean isNeedSelectAuto = false;
            int i = 0;
            while (true) {
                if (i < mccs.length) {
                    if (!oldMcc.equals(mccs[i]) && newMcc.equals(mccs[i])) {
                        isNeedSelectAuto = true;
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            logd("setNetworkSelectionModeAutomaticHw isNeedSelectAuto:" + isNeedSelectAuto);
            if (isNeedSelectAuto) {
                this.mPhone.setNetworkSelectionModeAutomatic((Message) null);
            }
        }
    }

    private void toGetRplmnsThenSendEccNumCdma() {
        String hplmn = TelephonyManagerEx.getSimOperatorNumericForPhone(this.mPhoneId);
        String forceEccState = SystemPropertiesEx.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        logd("[SLOT" + this.mPhoneId + "]GECC-toGetRplmnsThenSendEccNum: hplmn = " + hplmn + "; rplmn = " + this.mRplmn + "; forceEccState = " + forceEccState);
        UiccProfileEx profile = getUiccProfileEx();
        if (((profile != null && profile.getIccCardStateHw()) || BuildConfig.FLAVOR.equals(hplmn) || "usim_absent".equals(forceEccState)) && !TextUtils.isEmpty(this.mRplmn)) {
            this.mPhone.globalEccCustom(this.mRplmn);
        }
    }

    private UiccProfileEx getUiccProfileEx() {
        UiccCardExt uiccCardExt = UiccControllerExt.getInstance().getUiccCard(PhoneFactoryExt.getDefaultPhone().getPhoneId());
        if (uiccCardExt != null) {
            return uiccCardExt.getUiccProfile();
        }
        return null;
    }

    private void toGetRplmnsThenSendEccNumGsm() {
        String hplmn = TelephonyManagerEx.getSimOperatorNumericForPhone(this.mPhoneId);
        String forceEccState = SystemPropertiesEx.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        logd("[SLOT" + this.mPhoneId + "]GECC-toGetRplmnsThenSendEccNum: hplmn = " + hplmn + "; forceEccState = " + forceEccState + ", rplmn=" + this.mRplmn);
        UiccProfileEx profile = getUiccProfileEx();
        if ((profile != null && profile.getIccCardStateHw()) || BuildConfig.FLAVOR.equals(hplmn) || "usim_absent".equals(forceEccState)) {
            if (!BuildConfig.FLAVOR.equals(SystemPropertiesEx.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, BuildConfig.FLAVOR))) {
                this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
            }
            if (!TextUtils.isEmpty(this.mRplmn)) {
                this.mPhone.globalEccCustom(this.mRplmn);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCaStateChanged(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() != null) {
            logd("handleCaStateChanged: exception;");
            return;
        }
        boolean isOldCaActive = true;
        boolean isCaActive = ((int[]) ar.getResult())[0] == 1;
        if (this.mOldCaState != 19) {
            isOldCaActive = false;
        }
        if (IS_SHOW_4G_PLUS_ICON && isOldCaActive != isCaActive) {
            if (isCaActive) {
                this.mOldCaState = 19;
                logd("[CA] handleCaStateChanged CA activated !");
            } else {
                this.mOldCaState = 0;
                logd("[CA] handleCaStateChanged CA deactivated !");
            }
            broadcastCaState(isCaActive);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIccRecordsEonsUpdated(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() != null) {
            loge("EVENT_ICC_RECORDS_EONS_UPDATED exception " + ar.getException());
            return;
        }
        processIccEonsRecordsUpdated(((Integer) ar.getResult()).intValue());
    }

    private void processIccEonsRecordsUpdated(int eventCode) {
        logd("processIccEonsRecordsUpdated, eventCode = " + eventCode);
        if (eventCode == IccRecordsEx.getEventSpn() || eventCode == 100) {
            this.mServiceStateTracker.updateSpnDisplayHw();
        }
    }

    public void setReregisteredResultFlag(boolean flag) {
        this.mReregisteredResultFlag = flag;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePollLocationInfo(Message msg) {
        logd("GSM EVENT_POLL_LOCATION_INFO");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() != null) {
            loge("EVENT_POLL_LOCATION_INFO exception " + ar.getException());
            return;
        }
        String[] states = (String[]) ar.getResult();
        logi("CLEARCODE EVENT_POLL_LOCATION_INFO");
        if (states.length == 4) {
            try {
                if (states[2] != null && states[2].length() > 0) {
                    this.mRac = Integer.parseInt(states[2], 16);
                    logd("CLEARCODE mRac = " + this.mRac);
                }
            } catch (NumberFormatException ex) {
                loge("error parsing LocationInfoState: " + ex);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendTimeZoneSelectionNotification() {
        String currentMcc = null;
        if (getSs() != null) {
            String operator = getSs().getOperatorNumeric();
            if (operator != null && operator.length() >= 3) {
                currentMcc = operator.substring(0, 3);
            }
            if (!this.mServiceStateTracker.getNitzTimeZoneDetectionSuccessful()) {
                List<TimeZone> timeZones = null;
                int tzListSize = 0;
                String iso = TelephonyManagerEx.getTelephonyProperty(this.mPhoneId, "gsm.operator.iso-country", BuildConfig.FLAVOR);
                String lastMcc = Settings.System.getString(this.mCr, "last_registed_mcc");
                boolean isTheSameNwAsLast = (lastMcc == null || currentMcc == null || !lastMcc.equals(currentMcc)) ? false : true;
                logd("[settimezone] the network " + operator + " don't support nitz! current network isTheSameNwAsLast" + isTheSameNwAsLast);
                if (!BuildConfig.FLAVOR.equals(iso)) {
                    timeZones = TimeZoneFinderEx.lookupTimeZonesByCountry(iso);
                    tzListSize = timeZones == null ? 0 : timeZones.size();
                }
                if (1 != tzListSize || isTheSameNwAsLast) {
                    logd("[settimezone] there are " + tzListSize + " timezones in " + iso);
                    Intent intent = new Intent(ACTION_TIMEZONE_SELECTION);
                    intent.putExtra("operator", operator);
                    intent.putExtra("iso", iso);
                    this.mContext.sendStickyBroadcast(intent);
                } else {
                    TimeZone tz = timeZones.get(0);
                    logd("[settimezone] time zone:" + tz.getID());
                    TimeServiceHelperEx.setDeviceTimeZoneStatic(this.mContext, tz.getID());
                }
            }
            if (currentMcc != null) {
                Settings.System.putString(this.mCr, "last_registed_mcc", currentMcc);
                return;
            }
            return;
        }
        logd("sendTimeZoneSelectionNotification, invalid para");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleResumeData() {
        logd("EVENT_RESUME_DATA, resume data now.");
        this.mPhone.setInternalDataEnabled(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetPreNetworkType(Message msg) {
        if (this.mIsSetPreNwTypeRequested) {
            logd("EVENT_SET_PRE_NETWORKTYPE, setPreferredNetworkType now.");
            setPreferredNetworkType(msg.arg1, (Message) msg.obj);
            this.mIsSetPreNwTypeRequested = false;
            return;
        }
        logd("No need to setPreferredNetworkType");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimHotPlug(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int simCount = TelephonyManagerEx.getDefault().getPhoneCount();
        if (ar != null && ar.getResult() != null && ((int[]) ar.getResult()).length > 0) {
            if (HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.getResult())[0]) {
                if (simCount == 2) {
                    setNeedHandleRejInfoFlag(true);
                }
            } else if (HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.getResult())[0]) {
                if (simCount == 2) {
                    clearNetworkRejInfo();
                }
                this.mServiceStateTracker.clearmLastCellInfoList();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkRejInfoDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() == null) {
            String[] datas = (String[]) ar.getResult();
            String rejectplmn = null;
            int rejectdomain = -1;
            int rejectcause = -1;
            int rejectrat = -1;
            if (datas.length >= 4) {
                try {
                    if (datas[0] != null && datas[0].length() > 0) {
                        rejectplmn = datas[0];
                    }
                    if (datas[1] != null && datas[1].length() > 0) {
                        rejectdomain = Integer.parseInt(datas[1]);
                    }
                    if (datas[2] != null && datas[2].length() > 0) {
                        rejectcause = Integer.parseInt(datas[2]);
                    }
                    if (datas[3] != null && datas[3].length() > 0) {
                        rejectrat = Integer.parseInt(datas[3]);
                    }
                } catch (Exception e) {
                    loge("error parsing NetworkReject!");
                }
                handleNetworkRejinfoNotification(rejectplmn, rejectdomain, rejectcause, rejectrat);
            }
        }
    }

    private synchronized void handleNetworkRejinfoNotification(String rejectplmn, int rejectdomain, int rejectcause, int rejectrat) {
        logd("handleNetworkRejinfoNotification:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat);
        boolean needHandleRejectCause = Arrays.binarySearch(NETWORK_REJINFO_CAUSES, rejectcause) >= 0;
        if (this.mIsNeedHandleRejInfoFlag && !TextUtils.isEmpty(rejectplmn)) {
            if (needHandleRejectCause) {
                if (!this.mHandler.hasMessages(EVENT_NETWORK_REJINFO_TIMEOUT)) {
                    this.mHandler.sendEmptyMessageDelayed(EVENT_NETWORK_REJINFO_TIMEOUT, 1200000);
                }
                if (!this.mNetworkRejInfoLists.contains(rejectplmn)) {
                    this.mNetworkRejInfoLists.add(rejectplmn);
                    logd("handleNetworkRejinfoNotification: add " + rejectplmn + " rejinfoList:" + this.mNetworkRejInfoLists);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkRejInfoTimeout() {
        if (this.mNetworkRejInfoLists.size() > 3) {
            showNetworkRejInfoNotification();
            clearNetworkRejInfo();
            return;
        }
        this.mNetworkRejInfoLists.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCrrConn(Message msg) {
        logd("EVENT_CRR_CONN");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() == null) {
            int[] response = (int[]) ar.getResult();
            if (response.length > 2) {
                sendBroadcastCrrConnInd(response[0], response[1], response[2]);
                return;
            }
            return;
        }
        loge("EVENT_CRR_CONN: exception;");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkRejectedCase(Message msg) {
        if (this.mPhone.isPhoneTypeGsm()) {
            logd("EVENT_NETWORK_REJECTED_CASE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() != null) {
                loge("EVENT_NETWORK_REJECTED_CASE exception " + ar.getException());
                return;
            }
            onNetworkReject(ar);
        }
    }

    private void onNetworkReject(AsyncResultEx ar) {
        String[] datas = (String[]) ar.getResult();
        String rejectplmn = null;
        int rejectdomain = -1;
        int rejectcause = -1;
        int rejectrat = -1;
        int orignalrejectcause = -1;
        if (datas.length > 0) {
            try {
                if (datas[0] != null && datas[0].length() > 0) {
                    rejectplmn = datas[0];
                }
                if (datas.length > 1 && datas[1] != null && datas[1].length() > 0) {
                    rejectdomain = Integer.parseInt(datas[1]);
                }
                if (datas.length > 2 && datas[2] != null && datas[2].length() > 0) {
                    rejectcause = Integer.parseInt(datas[2]);
                }
                if (datas.length > 3 && datas[3] != null && datas[3].length() > 0) {
                    rejectrat = Integer.parseInt(datas[3]);
                }
                if (IS_VERIZON && datas.length > 4 && datas[4] != null && datas[4].length() > 0) {
                    orignalrejectcause = Integer.parseInt(datas[4]);
                }
            } catch (Exception e) {
                loge("error parsing NetworkReject!");
            }
            if (rejectrat == 2) {
                this.mRejCause = rejectcause;
            }
            logd("NetworkReject:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat + " rejNum = " + this.rejNum);
            if (IS_SHOW_REJ_INFO_KT) {
                this.mServiceStateTracker.handleNetworkRejectionEx(rejectcause, rejectrat);
            }
            if (IS_PS_CLEARCODE) {
                if (rejectrat == 2) {
                    this.rejNum++;
                }
                if (this.rejNum >= 3) {
                    this.mPhone.setPreferredNetworkType(3, (Message) null);
                    HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, this.mPhoneId, 3);
                    this.rejNum = 0;
                    this.mRac = -1;
                }
            }
            if (IS_VERIZON) {
                this.mServiceStateTracker.handleLteEmmCause(this.mPhoneId, rejectrat, orignalrejectcause);
            }
        }
    }

    private void sendBroadcastCrrConnInd(int modem0, int modem1, int modem2) {
        logi("sendBroadcastCrrConnInd");
        Intent intent = new Intent("com.huawei.action.ACTION_HW_CRR_CONN_IND");
        intent.putExtra("modem0", modem0);
        intent.putExtra("modem1", modem1);
        intent.putExtra("modem2", modem2);
        logi("modem0: " + modem0 + " modem1: " + modem1 + " modem2: " + modem2);
        this.mContext.sendBroadcast(intent, "com.huawei.permission.CRRCONN_PERMISSION");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePlmnSelInfo(Message msg) {
        logd("EVENT_PLMN_SELINFO");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar.getException() == null) {
            int[] responses = (int[]) ar.getResult();
            if (responses.length != 0) {
                sendBroadcastRegPlmnSelInfo(responses[0], responses[1]);
            }
        }
    }

    private void sendBroadcastRegPlmnSelInfo(int flag, int result) {
        Intent intent = new Intent("com.huawei.action.SIM_PLMN_SELINFO");
        int subId = this.mPhone.getPhoneId();
        intent.putExtra("subId", subId);
        intent.putExtra("flag", flag);
        intent.putExtra("res", result);
        logi("subId: " + subId + " flag: " + flag + " result: " + result);
        this.mContext.sendBroadcast(intent, "com.huawei.permission.HUAWEI_BUSSINESS_PERMISSION");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDsdsMode(Message msg) {
        logd("GSM EVENT_DSDS_MODE");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null || !(ar.getResult() instanceof int[])) {
            loge("GSM EVENT_DSDS_MODE: exception;");
            return;
        }
        int[] responses = (int[]) ar.getResult();
        if (responses.length != 0) {
            sendBroadcastDsdsMode(responses[0]);
        }
    }

    private void sendBroadcastDsdsMode(int dsdsMode) {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_DSDS_MODE_STATE");
        intent.putExtra("dsdsmode", dsdsMode);
        logi("GSM dsdsMode: " + dsdsMode);
        this.mContext.sendBroadcast(intent, "com.huawei.permission.DSDSMODE_PERMISSION");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMccChanged() {
        if (this.mPhone.isPhoneTypeGsm()) {
            logd("EVENT_MCC_CHANGED");
            SystemPropertiesEx.set("gsm.sim.updatenitz", String.valueOf(-1));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handle4GMimoEnable(Message msg) {
        if (IS_MIMO_4R_REPORT && this.mPhone.isPhoneTypeGsm()) {
            logd("EVENT_4R_MIMO_ENABLE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() != null) {
                loge("EVENT_MIMO_ENABLE exception " + ar.getException());
                return;
            }
            on4RMimoChange(ar);
        }
    }

    private void on4RMimoChange(AsyncResultEx ar) {
        int[] responseArray = (int[]) ar.getResult();
        int mimoResult = 0;
        if (responseArray.length != 0) {
            mimoResult = responseArray[0];
        }
        if (mimoResult == 1) {
            boolean[] zArr = this.mMplStatus;
            if (!zArr[0]) {
                zArr[0] = true;
                logd("4R_MIMO_ENABLE = " + mimoResult);
                Intent intent = new Intent("com.huawei.intent.action.MPL_STATUS_CHANGE");
                intent.addFlags(536870912);
                SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, this.mPhoneId);
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandleEx.ALL);
            }
        }
        if (mimoResult == 0) {
            boolean[] zArr2 = this.mMplStatus;
            if (zArr2[0]) {
                zArr2[0] = false;
                logd("4R_MIMO_ENABLE = " + mimoResult);
                Intent intent2 = new Intent("com.huawei.intent.action.MPL_STATUS_CHANGE");
                intent2.addFlags(536870912);
                SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent2, this.mPhoneId);
                this.mContext.sendStickyBroadcastAsUser(intent2, UserHandleEx.ALL);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handle256QamStateChange(Message msg) {
        if (this.mPhone.isPhoneTypeGsm()) {
            logd("EVENT_256QAM_STATE_CHANGE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() != null) {
                loge("EVENT_256QAM_STATE_CHANGE exception " + ar.getException());
                return;
            }
            on256QamStateChange(ar);
        }
    }

    private void on256QamStateChange(AsyncResultEx ar) {
        int[] responseArray = (int[]) ar.getResult();
        int qam256Result = 0;
        if (responseArray != null && responseArray.length > 0) {
            qam256Result = responseArray[0];
        }
        if (qam256Result == 1) {
            boolean[] zArr = this.mMplStatus;
            if (!zArr[1]) {
                zArr[1] = true;
                logd("256Qam state change to " + qam256Result);
                Intent intent = new Intent("com.huawei.intent.action.MPL_STATUS_CHANGE");
                intent.addFlags(536870912);
                SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, this.mPhoneId);
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandleEx.ALL);
            }
        }
        if (qam256Result == 0) {
            boolean[] zArr2 = this.mMplStatus;
            if (zArr2[1]) {
                zArr2[1] = false;
                logd("256Qam state change to " + qam256Result);
                Intent intent2 = new Intent("com.huawei.intent.action.MPL_STATUS_CHANGE");
                intent2.addFlags(536870912);
                SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent2, this.mPhoneId);
                this.mContext.sendStickyBroadcastAsUser(intent2, UserHandleEx.ALL);
            }
        }
    }

    private void showNetworkRejInfoNotification() {
        if (this.mContext != null) {
            logd("showNetworkRejinfoNotification");
            Intent resultIntent = new Intent(DUAL_CARDS_SETTINGS_ACTIVITY);
            resultIntent.setFlags(335544320);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this.mContext, 0, resultIntent, 134217728);
            Notification.Action dualCardSettingsAction = new Notification.Action.Builder((Icon) null, this.mContext.getString(HwPartResourceUtils.getResourceId("network_rejinfo_notification_action")), resultPendingIntent).build();
            int showSubId = this.mPhone.getPhoneId() + 1;
            Notification.Builder builder = new Notification.Builder(this.mContext).setSmallIcon(HwPartResourceUtils.getResourceId("ic_phone_fail_statusbar")).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1).setContentTitle(this.mContext.getString(HwPartResourceUtils.getResourceId("network_rejinfo_notify_title"), Integer.valueOf(showSubId))).setContentText(this.mContext.getString(HwPartResourceUtils.getResourceId("network_rejinfo_notify_content"), Integer.valueOf(showSubId))).setContentIntent(resultPendingIntent).setStyle(new Notification.BigTextStyle()).setChannelId(NETWORK_REJINFO_NOTIFY_CHANNEL).addAction(dualCardSettingsAction);
            NotificationEx.Builder.setAppName(builder, this.mContext.getString(HwPartResourceUtils.getResourceId("network_rejinfo_notification_app")));
            NotificationManager mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (mNotificationManager != null) {
                mNotificationManager.notify(LOG_TAG, this.mPhone.getPhoneId(), builder.build());
            }
        }
    }

    public String getGsmPlmn() {
        return this.mHwGsmOnsDisplayParamsManager.getGsmPlmn();
    }

    public OnsDisplayParams getCdmaOnsDisplayParams() {
        return this.mHwCdmaOnsDisplayParamsManager.getOnsDisplayParamsHw();
    }

    public OnsDisplayParams getGsmOnsDisplayParams(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        return this.mHwGsmOnsDisplayParamsManager.getOnsDisplayParamsHw(isShowSpn, isShowPlmn, rule, plmn, spn);
    }

    public String getGsmRplmn() {
        return this.mRplmn;
    }

    public boolean getGsmRoamingState(boolean isRoaming) {
        return this.mHwGsmOnsDisplayParamsManager.getRoamingStateHw(isRoaming);
    }

    public void sendDualSimUpdateSpnIntent(boolean isShowSpn, String spn, boolean isShowPlmn, String plmn) {
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            Intent intent = null;
            int phoneId = this.mPhone.getPhoneId();
            boolean z = true;
            if (phoneId == 0) {
                intent = new Intent("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
            } else if (1 == phoneId) {
                intent = new Intent("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
            } else {
                loge("unsupport SUB ID :" + phoneId);
            }
            boolean isSstInService = false;
            if (!(this.mServiceStateTracker == null || getSs() == null)) {
                int dataRegState = ServiceStateEx.getDataState(getSs());
                int voiceRegState = ServiceStateEx.getVoiceRegState(getSs());
                if (!(dataRegState == 0 || voiceRegState == 0)) {
                    z = false;
                }
                isSstInService = z;
                logd("dataRegState=" + dataRegState + ", voiceRegState=" + voiceRegState + ", isSstInService=" + isSstInService);
            }
            if (intent != null) {
                intent.addFlags(536870912);
                intent.putExtra("showSpn", isShowSpn);
                intent.putExtra(HwTelephony.VirtualNets.SPN, spn);
                intent.putExtra("showPlmn", isShowPlmn);
                intent.putExtra("plmn", plmn);
                intent.putExtra("subscription", this.mPhone.getSubId());
                intent.putExtra("slot", phoneId);
                intent.putExtra(EXTRA_SHOW_WIFI, this.mIsCurShowWifi);
                intent.putExtra(EXTRA_WIFI, this.mCurWifi);
                intent.putExtra(EXTRA_SST_IN_SERVICE, isSstInService);
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandleEx.ALL);
                logd("Send updateSpnIntent for SUB :" + phoneId);
            }
        }
    }

    public void delaySendDetachAfterDataOff() {
        String simPlmn;
        int delayMilsec = SystemPropertiesEx.getInt("ro.config.hw_delay_detach_time", 0) * 1000;
        boolean isDetachSign = false;
        int slotId = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
        try {
            Integer delayTime = (Integer) HwCfgFilePolicy.getValue("hw_delay_detach_time", slotId, Integer.class);
            Boolean isDetachSignFromCard = (Boolean) HwCfgFilePolicy.getValue("delay_detach_switch", slotId, Boolean.class);
            if (delayTime != null) {
                delayMilsec = delayTime.intValue() * 1000;
            }
            if (isDetachSignFromCard != null) {
                isDetachSign = isDetachSignFromCard.booleanValue();
            }
            if (delayMilsec >= MAX_DELAY_POWER_OFF_MISEC) {
                delayMilsec = 2000;
            }
        } catch (Exception e) {
            loge("Exception: read delay_detach_switch error!");
        }
        if (!isDetachSign || delayMilsec <= 0) {
            String delayPlmn = Settings.System.getString(this.mCr, "hw_delay_imsi_detach_plmn");
            if (TextUtils.isEmpty(delayPlmn)) {
                delayPlmn = SystemPropertiesEx.get("ro.config.hw_delay_detach_plmn", BuildConfig.FLAVOR);
            }
            logd("delayPlmn = " + delayPlmn);
            if (TelephonyManagerEx.isMultiSimEnabled()) {
                simPlmn = TelephonyManagerEx.getSimOperatorNumericForPhone(slotId);
                logd("isMultiSimEnabled + simPlmn = " + simPlmn);
            } else {
                simPlmn = SystemPropertiesEx.get("gsm.sim.operator.numeric", BuildConfig.FLAVOR);
                logd("not isMultiSimEnabled + simPlmn = " + simPlmn);
            }
            if (BuildConfig.FLAVOR.equals(delayPlmn) || BuildConfig.FLAVOR.equals(simPlmn)) {
                logd("Process pending request to turn radio off; delayPlmn:" + delayPlmn + " simPlmn:" + simPlmn);
            } else if (delayPlmn.indexOf(simPlmn) == -1 || delayMilsec <= 0) {
                logd("Process pending request to turn radio off");
            } else {
                if (delayMilsec >= MAX_DELAY_POWER_OFF_MISEC) {
                    delayMilsec = 2000;
                }
                logd("Process pending request to turn radio off " + delayMilsec + " milliseconds later.");
                SystemClock.sleep((long) delayMilsec);
                logd(delayMilsec + " milliseconds past, Process pending request to turn radio off");
            }
        } else {
            logd("HwCfgFile: Process pending request to turn radio off " + delayMilsec + " milliseconds later.");
            SystemClock.sleep((long) delayMilsec);
        }
    }

    public int getCombinedRegState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int regState = ServiceStateEx.getVoiceRegState(serviceState);
        int dataRegState = ServiceStateEx.getDataState(serviceState);
        if ((IS_VOICE_REG_STATE_FOR_ONS && ServiceStateEx.getRilDataRadioTechnology(serviceState) != 14 && ServiceStateEx.getRilDataRadioTechnology(serviceState) != 19) || regState != 1 || dataRegState != 0) {
            return regState;
        }
        slogd("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    public boolean needUpdateNITZTime() {
        int otherCardState;
        int otherPhoneType;
        int otherCardType;
        String mcc = BuildConfig.FLAVOR;
        String str = this.mRplmn;
        if (str != null && str.length() > 3) {
            mcc = this.mRplmn.substring(0, 3);
        }
        if (INTERNATIONAL_MCC.equals(mcc)) {
            logd("international mcc without conuntry code, not allow update time, rplmn is " + this.mRplmn);
            return false;
        }
        long nitzStateSpaceTime = this.mServiceStateTracker.getNitzSpaceTime();
        logd("nitzSpaceTime: " + nitzStateSpaceTime + ", elapsedRealtime:" + SystemClock.elapsedRealtime());
        if (nitzStateSpaceTime > 1800000) {
            return true;
        }
        int phoneId = this.mPhone.getPhoneId();
        int ownCardType = HwTelephonyManagerInner.getDefault().getCardType(phoneId);
        int ownPhoneType = TelephonyManagerEx.getCurrentPhoneTypeForSlot(phoneId);
        if (phoneId == 0) {
            otherCardType = HwTelephonyManagerInner.getDefault().getCardType(1);
            otherCardState = HwTelephonyManagerInner.getDefault().getSubState(1);
            otherPhoneType = TelephonyManagerEx.getCurrentPhoneTypeForSlot(1);
        } else if (phoneId == 1) {
            otherCardType = HwTelephonyManagerInner.getDefault().getCardType(0);
            otherCardState = HwTelephonyManagerInner.getDefault().getSubState((long) MODE_REQUEST_CELL_LIST_STRATEGY_VALID);
            otherPhoneType = TelephonyManagerEx.getCurrentPhoneTypeForSlot(0);
        } else {
            otherCardType = -1;
            otherCardState = 0;
            otherPhoneType = 0;
        }
        logd("ownCardType = " + ownCardType + ", otherCardType = " + otherCardType + ", otherCardState = " + otherCardState + " ownPhoneType = " + ownPhoneType + ", otherPhoneType = " + otherPhoneType);
        if ((ownCardType == 41 || ownCardType == 43) && ownPhoneType == 2) {
            logd("Cdma card, uppdate NITZ time!");
            return true;
        } else if ((otherCardType == 30 || otherCardType == 43 || otherCardType == 41) && otherCardState == 1 && otherPhoneType == 2) {
            HwReportManagerImpl.getDefault().reportNitzIgnore(phoneId, "CG_IGNORE");
            logd("Other cdma card, ignore updating NITZ time!");
            return false;
        } else if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimOn() && HwVSimUtils.isVSimSub(phoneId)) {
            logd("vsim phone, update NITZ time!");
            return true;
        } else if (phoneId == SystemPropertiesEx.getInt("gsm.sim.updatenitz", phoneId) || SystemPropertiesEx.getInt("gsm.sim.updatenitz", -1) == -1 || otherCardState == 0) {
            SystemPropertiesEx.set("gsm.sim.updatenitz", String.valueOf(phoneId));
            logd("Update NITZ time, set update card : " + phoneId);
            return true;
        } else {
            HwReportManagerImpl.getDefault().reportNitzIgnore(phoneId, "GG_IGNORE");
            logd("Ignore updating NITZ time, phoneid : " + phoneId);
            return false;
        }
    }

    public boolean updateCTRoaming(ServiceState newSs, boolean isCdmaRoaming) {
        if (newSs == null) {
            return isCdmaRoaming;
        }
        if (!HwTelephonyManagerInner.getDefault().isChinaTelecom(this.mPhone.getPhoneId())) {
            return isCdmaRoaming;
        }
        setCTRoaming(newSs);
        boolean isCdmaRoamingTemp = newSs.getRoaming();
        ServiceStateEx.setCdmaEriIcon(newSs, isCdmaRoamingTemp);
        return isCdmaRoamingTemp;
    }

    private void setCTRoaming(ServiceState newSs) {
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        boolean isSimMccInvaild = false;
        if (iccRecords == null) {
            logd("setCTRoaming iccRecords is null");
            newSs.setRoaming(false);
            return;
        }
        String plmn = newSs.getOperatorNumeric();
        String hplmn = iccRecords.getOperatorNumeric();
        String nwMcc = getMccFromPlmn(plmn);
        String simMcc = getMccFromPlmn(hplmn);
        int sid = newSs.getCdmaSystemId();
        logd("setCTRoaming: plmn = " + plmn + ", hplmn = " + hplmn + ", sid = " + sid);
        if (plmn != null && plmn.length() >= 5) {
            if (INVAILD_PLMN.indexOf(plmn.trim() + "-") == -1) {
                if (!TextUtils.isEmpty(simMcc) && !simMcc.equals(nwMcc)) {
                    isSimMccInvaild = true;
                }
                logd("setCTRoaming: setRoaming" + isSimMccInvaild);
                newSs.setRoaming(isSimMccInvaild);
                return;
            }
        }
        if (sid == 0 || sid == -1) {
            newSs.setRoaming(false);
        } else if ((sid >= 13568 && sid <= 14335) || (sid >= 25600 && sid <= 26111)) {
            if (!TextUtils.isEmpty(simMcc) && !CT_MCC.equals(simMcc)) {
                isSimMccInvaild = true;
            }
            newSs.setRoaming(isSimMccInvaild);
        } else if (sid < CT_MACAO_SID_START || sid > CT_MACAO_SID_END) {
            logd("setCTRoaming sid is not in the specified range");
            newSs.setRoaming(true);
        } else {
            if (!TextUtils.isEmpty(simMcc) && !CT_MACAO_MCC.equals(simMcc)) {
                isSimMccInvaild = true;
            }
            newSs.setRoaming(isSimMccInvaild);
        }
    }

    private String getMccFromPlmn(String plmn) {
        if (TextUtils.isEmpty(plmn) || plmn.length() < 3) {
            return BuildConfig.FLAVOR;
        }
        return plmn.substring(0, 3);
    }

    public int getCARilRadioType(int type) {
        int radioType = type;
        if (IS_SHOW_4G_PLUS_ICON && type == 19) {
            radioType = 14;
        }
        logd("[CA] radioType=" + radioType + " type=" + type);
        return radioType;
    }

    public int updateCAStatus(int currentType) {
        if (IS_SHOW_4G_PLUS_ICON) {
            logd("[CA] updateCAStatus currentType=" + currentType + " oldCAstate=" + this.mOldCaState);
            boolean isCaDeactivated = true;
            boolean isCaActivated = currentType == 19 && this.mOldCaState != 19;
            if (currentType == 19 || this.mOldCaState != 19) {
                isCaDeactivated = false;
            }
            this.mOldCaState = currentType;
            if (isCaActivated || isCaDeactivated) {
                broadcastCaState(isCaActivated);
            }
        }
        return currentType;
    }

    private void broadcastCaState(boolean isCaActivated) {
        Intent intentLteCaState = new Intent("com.huawei.intent.action.LTE_CA_STATE");
        intentLteCaState.putExtra("subscription", this.mPhone.getSubId());
        intentLteCaState.putExtra("slot", this.mPhone.getPhoneId());
        intentLteCaState.putExtra("LteCAstate", isCaActivated);
        logd("[CA] braodcastCaState CA activate is " + isCaActivated);
        this.mPhone.getContext().sendBroadcast(intentLteCaState);
    }

    public void registerForSimRecordsEvents(IccRecordsEx r) {
        if (r != null) {
            r.registerForRecordsEvents(this.mHandler, (int) EVENT_ICC_RECORDS_EONS_UPDATED, (Object) null);
        }
    }

    public void unregisterForSimRecordsEvents(IccRecordsEx r) {
        if (r != null) {
            r.unregisterForRecordsEvents(this.mHandler);
        }
    }

    public boolean isCustScreenOff() {
        PowerManager powerManager;
        if (!IS_SCREEN_OFF_NOT_UPDATE_LOCATION || (powerManager = (PowerManager) this.mContext.getSystemService("power")) == null || powerManager.isScreenOn()) {
            return false;
        }
        logd(" ScreenOff do nothing");
        return true;
    }

    private ServiceState getSs() {
        return this.mServiceStateTracker.getmSSHw();
    }

    private ServiceState getNewSs() {
        return this.mServiceStateTracker.getmNewSSHw();
    }

    public boolean proccessGsmDelayUpdateRegisterStateDone(ServiceState oldSs, ServiceState newSs) {
        if (oldSs == null || newSs == null) {
            return false;
        }
        if (!oldSs.equals(newSs)) {
            notifyHwServiceStateChanged(newSs);
        }
        return this.mHwGsmSignalStengthManager.proccessGsmDelayUpdateRegisterStateDone(oldSs, newSs);
    }

    public boolean proccessCdmaLteDelayUpdateRegisterStateDone(ServiceState oldSs, ServiceState newSs) {
        if (oldSs == null || newSs == null) {
            return false;
        }
        if (!oldSs.equals(newSs)) {
            notifyHwServiceStateChanged(newSs);
        }
        return this.mHwCdmaSignalStengthManager.proccessCdmaLteDelayUpdateRegisterStateDone(oldSs, newSs);
    }

    public void sendGsmRoamingIntentIfDenied(int regState, int rejectCode) {
        if ((regState == 3 || ServiceStateEx.isEmergencyOnly(getNewSs())) && rejectCode == 10) {
            logd("Posting Managed roaming intent sub = " + this.mPhone.getSubId());
            Intent intent = new Intent("codeaurora.intent.action.ACTION_MANAGED_ROAMING_IND");
            intent.putExtra("subscription", this.mPhone.getSubId());
            intent.putExtra("slot", this.mPhone.getPhoneId());
            this.mContext.sendBroadcast(intent);
        }
    }

    public void getLocationInfo() {
        if (IS_PS_CLEARCODE) {
            this.mCi.getLocationInfo(this.mHandler.obtainMessage(EVENT_POLL_LOCATION_INFO));
        }
    }

    public int updateHSPAStatus(int type) {
        int typeTemp = type;
        if (IS_KEEP_3GPLUS_HPLUS) {
            logd("updateHSPAStatus dataRadioTechnology: " + typeTemp);
            int lac = -1;
            int cid = -1;
            CellLocation cl = this.mPhone.getCellLocation();
            if (cl instanceof GsmCellLocation) {
                GsmCellLocation cellLocation = (GsmCellLocation) cl;
                lac = cellLocation.getLac();
                cid = cellLocation.getCid();
            }
            if (this.mLastLac == lac && this.mLastCid == cid && this.mLastType == 15 && (typeTemp == 3 || typeTemp == 9 || typeTemp == 10 || typeTemp == 11)) {
                typeTemp = this.mLastType;
            }
            if (typeTemp == 15) {
                this.mLastLac = lac;
                this.mLastCid = cid;
                this.mLastType = typeTemp;
            }
        }
        return typeTemp;
    }

    public boolean isCellRequestStrategyPassed(WorkSource workSource) {
        if (workSource == null || TextUtils.isEmpty(WorkSourceEx.getName(workSource, 0))) {
            loge("isCellRequestStrategyPassed():return false.Because null-pointer params");
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null && tm.getSimState(this.mPhone.getPhoneId()) != 5) {
            logd("isCellRequestStrategyPassed():return false.Because sim state not ready.");
            return false;
        } else if (!isCellAgeTimePassed()) {
            logd("isCellRequestStrategyPassed():return false.Because isCellAgeTime is not passed.");
            return false;
        } else {
            long curSysTime = SystemClock.elapsedRealtime();
            long lastRequestTime = this.mServiceStateTracker.getLastCellInfoReqTime();
            String pkgName = WorkSourceEx.getName(workSource, 0);
            int uid = WorkSourceEx.get(workSource, 0);
            long id = Binder.clearCallingIdentity();
            NetLocationStrategyEx strategy = NetLocationStrategyEx.getNetLocationStrategy(pkgName, uid, NetLocationStrategyEx.NETLOCATION_MODEM);
            Binder.restoreCallingIdentity(id);
            if (strategy != null) {
                logd("isCellRequestStrategyPassed():get iAware strategy result = " + strategy.toString());
                if (strategy.getCycle() == MODE_REQUEST_CELL_LIST_STRATEGY_INVALID) {
                    logd("isCellRequestStrategyPassed():return false. Because iAware strategy return NOT_ALLOWED");
                    return false;
                } else if (MODE_REQUEST_CELL_LIST_STRATEGY_VALID >= strategy.getCycle() || curSysTime - lastRequestTime >= strategy.getCycle()) {
                    logd("isCellRequestStrategyPassed():return true.");
                    return true;
                } else {
                    logd("isCellRequestStrategyPassed():return false. Because already requested in iAware strategy");
                    return false;
                }
            } else {
                loge("isCellRequestStrategyPassed():get iAware strategy result = null.");
                return true;
            }
        }
    }

    private boolean isCellAgeTimePassed() {
        long curSysTime = SystemClock.elapsedRealtime();
        long lastRequestTime = this.mServiceStateTracker.getLastCellInfoReqTime();
        boolean isScreenOff = isCustScreenOff();
        int screenOffTimes = SystemPropertiesEx.getInt("ro.config.screen_off_times", 10);
        long cellInfoListMaxAgeTime = VALUE_CELL_INFO_LIST_MAX_AGE_MS;
        if (isScreenOff) {
            cellInfoListMaxAgeTime = VALUE_CELL_INFO_LIST_MAX_AGE_MS * ((long) screenOffTimes);
        }
        logd("isCellAgeTimePassed(): isScreenOff=" + isScreenOff + " cellInfoListMaxAgeTime=" + cellInfoListMaxAgeTime + "ms.");
        if (curSysTime - lastRequestTime > cellInfoListMaxAgeTime) {
            logd("isCellAgeTimePassed():return true.");
            return true;
        }
        logd("isCellAgeTimePassed():return false,because already requested CellInfoList within " + cellInfoListMaxAgeTime + "ms.");
        return false;
    }

    public void sendTimeZoneSelectionNotification() {
        if (this.mPhone.isPhoneTypeGsm() && SystemPropertiesEx.getBoolean("ro.config_hw_doubletime", false) && HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == this.mPhone.getPhoneId()) {
            logd("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported.");
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_NITZ_CAPABILITY_NOTIFICATION), 60000);
        }
    }

    public boolean isAllowLocationUpdate(int pid) {
        return isContainPackage(sEnableGetLoaction, HwTelephonyActivityManagerUtils.getAppName(this.mContext, pid));
    }

    private void loadAllowUpdateLocationPackage() {
        ContentResolver contentResolver = this.mCr;
        sEnableGetLoaction = contentResolver != null ? Settings.System.getString(contentResolver, "enable_get_location") : null;
    }

    public boolean isSupportSingalStrengthHw() {
        return true;
    }

    public boolean signalStrengthResultHw(SignalStrength oldSignalStrength, SignalStrength newSignalStrength, boolean isGsm) {
        if (oldSignalStrength == null || newSignalStrength == null) {
            loge("old or new signal is null, return false");
            return false;
        } else if (oldSignalStrength.equals(newSignalStrength)) {
            logd("signal strength is not changed.");
            return false;
        } else {
            notifyHwSignalStrengthChanged(newSignalStrength);
            this.mHwGsmSignalStengthManager.signalStrengthResultHw(newSignalStrength);
            if (isGsm) {
                return this.mHwGsmSignalStengthManager.notifySignalStrength(oldSignalStrength, newSignalStrength);
            }
            SignalStrengthEx.setCdma(newSignalStrength, true);
            return this.mHwCdmaSignalStengthManager.notifySignalStrength(oldSignalStrength, newSignalStrength);
        }
    }

    public void updateHwnff(int slotId, SignalStrength newSs) {
        int simCardState = TelephonyManagerEx.getDefault().getSimState(slotId);
        if (simCardState != 1 && simCardState != 0) {
            if (slotId == 0) {
                try {
                    if (this.mHwnffValueSim1 != SignalStrengthEx.getDbm(newSs)) {
                        this.mHwnffValueSim1 = SignalStrengthEx.getDbm(newSs);
                        SystemPropertiesEx.set(HWNFF_RSSI_SIM1, Integer.toString(this.mHwnffValueSim1));
                        logd("update hwnff Sub0 to " + this.mHwnffValueSim1);
                        return;
                    }
                } catch (RuntimeException e) {
                    loge("write hwnff Sub" + slotId + " prop failed ");
                    return;
                }
            }
            if (1 == slotId && this.mHwnffValueSim2 != SignalStrengthEx.getDbm(newSs)) {
                this.mHwnffValueSim2 = SignalStrengthEx.getDbm(newSs);
                SystemPropertiesEx.set(HWNFF_RSSI_SIM2, Integer.toString(this.mHwnffValueSim2));
                logd("update hwnff Sub1 to " + this.mHwnffValueSim2);
            }
        }
    }

    public boolean checkForRoamingForIndianOperators(ServiceState serviceState) {
        if (serviceState == null) {
            return false;
        }
        String simNumeric = SystemPropertiesEx.get("gsm.sim.operator.numeric", BuildConfig.FLAVOR);
        String operatorNumeric = serviceState.getOperatorNumeric();
        try {
            String simMcc = simNumeric.substring(0, 3);
            String operatorMcc = operatorNumeric.substring(0, 3);
            if ((MCC_INDIAN_1.equals(simMcc) || MCC_INDIAN_2.equals(simMcc)) && (MCC_INDIAN_1.equals(operatorMcc) || MCC_INDIAN_2.equals(operatorMcc))) {
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            loge("checkForRoamingForIndianOperators occur an exception");
        }
    }

    public boolean recoverAutoSelectMode(boolean isRecoverAutoSelectMode) {
        boolean isSkipRestoringSelection = this.mContext.getResources().getBoolean(HwPartResourceUtils.getResourceId("skip_restoring_network_selection"));
        if (getRecoverAutoModeFutureState()) {
            logd("Feature recover network mode automatic is on..");
            return true;
        }
        if (!isSkipRestoringSelection) {
            if (!HwModemCapability.isCapabilitySupport(4) || IS_KEEP_NW_SEL_MANUAL) {
                this.mPhone.restoreSavedNetworkSelectionHw((Message) null);
            } else {
                logd("Modem can select network auto with manual mode");
            }
        }
        return isRecoverAutoSelectMode;
    }

    private boolean getRecoverAutoModeFutureState() {
        Boolean isValueFromCard = (Boolean) HwCfgFilePolicy.getValue("recover_auto_mode", this.mPhoneId, Boolean.class);
        boolean isValueFromProp = IS_FEATURE_RECOVER_AUTO_NETWORK_MODE;
        logd("getRecoverAutoModeFutureState, card:" + isValueFromCard + ", prop:" + isValueFromProp);
        return isValueFromCard != null ? isValueFromCard.booleanValue() : isValueFromProp;
    }

    public int getDataOffTime(int airPlaneMode) {
        int delayTime = airPlaneMode == 0 ? 30000 : DATA_OFF_TIME_OUT * 1000;
        if (delayTime < 1000 || delayTime > 30000) {
            return 30000;
        }
        return delayTime;
    }

    public int getNrConfigTechnology(int newRat, NetworkRegistrationInfoEx networkRegistrationInfo) {
        if (networkRegistrationInfo == null) {
            return newRat;
        }
        int nsaState = networkRegistrationInfo.getNsaState();
        if (judgeNasStateByNrAbility(networkRegistrationInfo, nsaState)) {
            return newRat;
        }
        if (newRat != 19 && newRat != 14 && newRat != 20) {
            return newRat;
        }
        String config = SystemPropertiesEx.get(NR_TECHNOLOGY_CONFIG);
        if (TextUtils.isEmpty(config)) {
            config = NR_TECHNOLOGY_CONFIGD;
        }
        if (NR_TECHNOLOGY_CONFIGAD.equals(config)) {
            if (this.mRrcConnectionState == 1) {
                logd("rrc is connected, so use configA");
                config = NR_TECHNOLOGY_CONFIGA;
            } else {
                config = NR_TECHNOLOGY_CONFIGD;
            }
        }
        logd("nsaState: " + nsaState + "   config: " + config);
        if (nsaState != 2) {
            if (nsaState != 3) {
                if (nsaState != 4) {
                    if (nsaState != 5) {
                        return newRat;
                    }
                    return 20;
                } else if (NR_TECHNOLOGY_CONFIGB.equals(config) || NR_TECHNOLOGY_CONFIGC.equals(config) || NR_TECHNOLOGY_CONFIGD.equals(config)) {
                    return 20;
                } else {
                    return newRat;
                }
            } else if (NR_TECHNOLOGY_CONFIGC.equals(config) || NR_TECHNOLOGY_CONFIGD.equals(config)) {
                return 20;
            } else {
                return newRat;
            }
        } else if (NR_TECHNOLOGY_CONFIGD.equals(config)) {
            return 20;
        } else {
            return newRat;
        }
    }

    private boolean judgeNasStateByNrAbility(NetworkRegistrationInfoEx networkRegistrationInfo, int nsaState) {
        boolean isNrOff;
        if (!HwTelephonyManager.getDefault().isVSimEnabled() || this.mPhoneId != 2 || !HwNetworkTypeUtils.isNrServiceOn(Settings.Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode2", 3))) {
            if (!HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported()) {
                isNrOff = !isNrSwitchOn(HwTelephonyManager.getDefault().getDefault4GSlotId());
            } else {
                isNrOff = !isNrSwitchOn(this.mPhoneId);
            }
            if (!isNrOff) {
                String config = SystemPropertiesEx.get(NR_TECHNOLOGY_CONFIG);
                boolean lteConfig = NR_TECHNOLOGY_CONFIGAD.equals(config) && this.mRrcConnectionState == 1;
                if ((NR_TECHNOLOGY_CONFIGA.equals(config) || lteConfig) && nsaState == 2 && !this.mHandler.hasMessages(EVENT_DELAY_RAT_CHANGED)) {
                    networkRegistrationInfo.setNsaState(1);
                    this.mServiceStateTracker.setNewNsaState(1);
                    logd("rrc is connected, change state to 1");
                    return true;
                }
            } else if (networkRegistrationInfo != null && nsaState == 2) {
                networkRegistrationInfo.setNsaState(1);
                this.mServiceStateTracker.setNewNsaState(1);
                logd("judgeNasStateByNrAbility nsaState: " + networkRegistrationInfo.getNsaState());
                return true;
            }
            return false;
        }
        logd("judgeNasStateByNrAbility, vsim is on and nr is on, so return false");
        return false;
    }

    public int updateNsaState(ServiceState ss, int nsaState, int cellId) {
        int state = nsaState;
        this.mChangedNsaState = nsaState;
        if (!isNeedOptimizedConfigD(ss, nsaState, cellId)) {
            return state;
        }
        updateCellIdListInfo();
        updateCellIdMap();
        if (isNoneSibInfo(ss, state)) {
            updateCellIdCache(cellId);
            updateSCGCellCache(cellId);
        } else {
            int otherPhoneId = 1;
            if (state == 1) {
                boolean switchMainSlot = HwFullNetworkManager.getInstance().getWaitingSwitchBalongSlot();
                boolean hasAddScg = this.mCellIdMap.containsKey(Integer.valueOf(cellId));
                logd("process switch main slot:" + switchMainSlot + " 1-hasAddScg:" + hasAddScg);
                if (!hasAddScg && TelephonyManagerEx.getDefault().getPhoneCount() == 2) {
                    if (this.mPhoneId != 0) {
                        otherPhoneId = 0;
                    }
                    HwServiceStateTrackerEx hwServiceStateTrackerEx = getInstance(otherPhoneId);
                    if (hwServiceStateTrackerEx != null) {
                        hasAddScg = hwServiceStateTrackerEx.hasCellIdMapContain(cellId);
                    }
                    logd("2- hasAddScg:" + hasAddScg);
                }
                if (hasAddScg && !switchMainSlot) {
                    if (!TextUtils.isEmpty(SystemPropertiesEx.get(OPTIMIZED_FOR_CONFIGD))) {
                        logd("Current cell has ever added SCG, change NSA state 1 into state 2");
                        state = 2;
                    }
                    this.mChangedNsaState = 2;
                }
                if (isNeedReportClearSCGCellInfo(cellId)) {
                    reportClearSCGCellInfo(cellId);
                }
            } else {
                logd("Other state do nothing.");
            }
        }
        return state;
    }

    private boolean isCellIdInvalid(int cellId) {
        return cellId == -1 || cellId == Integer.MAX_VALUE || cellId == 0;
    }

    private boolean isNeedOptimizedConfigD(ServiceState ss, int nsaState, int cellId) {
        if (!HwTelephonyManager.getDefault().isNrSupported()) {
            return false;
        }
        if (nsaState == 5) {
            logd("SCG added, remove delay message.");
            removeRatChangedDelyaMessage();
        }
        if (!HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported()) {
            HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
            if (!isNrSwitchOn(hwTelephonyManager.getDefault4GSlotId()) || hwTelephonyManager.getDefault4GSlotId() != this.mPhoneId) {
                return false;
            }
        } else if (!isNrSwitchOn(this.mPhoneId)) {
            return false;
        }
        if (isCellIdInvalid(cellId)) {
            return false;
        }
        if (getCombinedRegState(ss) != 0 || !ServiceStateEx.isLte(ServiceStateEx.getRilDataRadioTechnology(ss))) {
            logd("data rat is not lte or lte ca, retrun.");
            return false;
        }
        String config = SystemPropertiesEx.get(NR_TECHNOLOGY_CONFIG);
        if (TextUtils.isEmpty(config)) {
            config = NR_TECHNOLOGY_CONFIGD;
        }
        if (NR_TECHNOLOGY_CONFIGD.equals(config) || NR_TECHNOLOGY_CONFIGAD.equals(config)) {
            return true;
        }
        return false;
    }

    private void updateCellIdListInfo() {
        String displayOptimized = SystemPropertiesEx.get(OPTIMIZED_FOR_CONFIGD);
        logd("displayOptimized: " + displayOptimized);
        if (!TextUtils.isEmpty(displayOptimized) && Pattern.compile("^\\d+,\\d+$").matcher(displayOptimized).matches()) {
            String[] param = displayOptimized.split(",");
            try {
                this.mCellIdListSize = Integer.parseInt(param[0]);
                this.mCellIdCacheTimer = Long.parseLong(param[1]) * 60 * 60 * 1000;
                logd("mCellIdListSize: " + this.mCellIdListSize + "  mCellIdCacheTimer: " + this.mCellIdCacheTimer);
            } catch (NumberFormatException e) {
                logd("number formate error.");
            }
        }
    }

    private void updateCellIdMap() {
        Iterator<Map.Entry<Integer, Long>> entries = this.mCellIdMap.entrySet().iterator();
        while (entries.hasNext()) {
            if (SystemClock.elapsedRealtime() - entries.next().getValue().longValue() > this.mCellIdCacheTimer) {
                entries.remove();
            }
        }
        logd("mCellIdMap.size: " + this.mCellIdMap.size());
    }

    private void updateCellIdCache(int cellId) {
        if (this.mCellIdMap.size() >= this.mCellIdListSize && !this.mCellIdMap.containsKey(Integer.valueOf(cellId))) {
            logd("mCellIdMap is full , remove first cellid.");
            List<Map.Entry<Integer, Long>> list = new ArrayList<>(this.mCellIdMap.entrySet());
            sortCellIdMap(list);
            int removeKey = -1;
            if (!(list.size() == 0 || list.get(0) == null)) {
                removeKey = list.get(0).getKey().intValue();
            }
            this.mCellIdMap.remove(Integer.valueOf(removeKey));
        }
        this.mCellIdMap.put(Integer.valueOf(cellId), Long.valueOf(SystemClock.elapsedRealtime()));
        logd("updateCellIdCache size: " + this.mCellIdMap.size());
    }

    private void sortCellIdMap(List<Map.Entry<Integer, Long>> list) {
        Collections.sort(list, new Comparator<Map.Entry<Integer, Long>>() {
            /* class com.android.internal.telephony.HwServiceStateTrackerEx.AnonymousClass2 */

            public int compare(Map.Entry<Integer, Long> o1, Map.Entry<Integer, Long> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
    }

    public boolean isNoneSibInfo(ServiceState ss, int state) {
        boolean isNrAvailable = ServiceStateEx.isNrAvailable(ss, 2);
        boolean isDcNrRestricted = ServiceStateEx.isNrAvailable(ss, 1);
        if (state != 5 || (isNrAvailable && !isDcNrRestricted)) {
            return false;
        }
        return true;
    }

    private Bundle buildClearCellBundle(int cellId) {
        Bundle bundle = new Bundle();
        bundle.putString("EventScenario", "scgCellCacheClear");
        bundle.putInt("CommonPara_Sensitive_Info.CellID", cellId);
        return bundle;
    }

    private void reportClearSCGCellInfo(int cellId) {
        Context context = this.mPhone.getContext();
        if (context == null) {
            logd("context is null");
            return;
        }
        Intent intent = new Intent("com.huawei.android.chr.action.ACTION_REPORT_CHR");
        intent.setFlags(67108864);
        intent.setPackage(CHR_PACKAGE_NAME);
        intent.putExtra("module_id", MODULE_ID_TELEPHONY);
        intent.putExtra("sub_id", this.mPhone.getPhoneId());
        intent.putExtra("chr_data", buildClearCellBundle(cellId));
        logd("reportClearSCGCellInfo report framework CHR");
        context.sendBroadcastAsUser(intent, UserHandleEx.ALL, "com.huawei.android.permission.GET_CHR_DATA");
    }

    private boolean isNeedReportClearSCGCellInfo(int cellId) {
        new ConcurrentHashMap();
        Map<Integer, Long> tempCellIdMap = getSCGCellMapByCache();
        if (tempCellIdMap == null) {
            logd("isNeedReportClearSCGCellInfo is false, tempCellIdMap is null");
            return false;
        } else if (SCG_CELL_ID_HAS_REPORTED.contains(Integer.valueOf(cellId)) || !tempCellIdMap.containsKey(Integer.valueOf(cellId)) || this.mCellIdMap.containsKey(Integer.valueOf(cellId))) {
            logd("isNeedReportClearSCGCellInfo is false");
            return false;
        } else {
            SCG_CELL_ID_HAS_REPORTED.add(Integer.valueOf(cellId));
            logd("isNeedReportClearSCGCellInfo is true");
            return true;
        }
    }

    private void updateSCGCellCache(int cellId) {
        new ConcurrentHashMap();
        Map<Integer, Long> tempCellIdMap = getSCGCellMapByCache();
        if (tempCellIdMap == null) {
            Settings.System.putString(this.mCr, LAST_SCG_CELL_INFOS, cellId + "," + SystemClock.elapsedRealtime() + ";");
            return;
        }
        tempCellIdMap.put(Integer.valueOf(cellId), Long.valueOf(SystemClock.elapsedRealtime()));
        ArrayList<Map.Entry<Integer, Long>> list = new ArrayList<>(tempCellIdMap.entrySet());
        sortCellIdMap(list);
        int listTag = 0;
        int listLength = list.size();
        if (tempCellIdMap.size() > this.mCellIdListSize) {
            logd("ScgCellCache is full , remove first cellid.");
            if (!(listLength == 0 || list.get(0) == null)) {
                listTag = 1;
            }
        }
        String scgCellInfo = BuildConfig.FLAVOR;
        while (listTag < listLength) {
            scgCellInfo = (scgCellInfo + list.get(listTag).getKey() + ",") + list.get(listTag).getValue() + ";";
            listTag++;
        }
        Settings.System.putString(this.mCr, LAST_SCG_CELL_INFOS, scgCellInfo);
    }

    private Map<Integer, Long> getSCGCellMapByCache() {
        Map<Integer, Long> tempCellIdMap = new ConcurrentHashMap<>();
        String lastCellInfos = Settings.System.getString(this.mCr, LAST_SCG_CELL_INFOS);
        if (TextUtils.isEmpty(lastCellInfos) || !Pattern.compile("(\\d+,\\d+;){1,}").matcher(lastCellInfos).matches()) {
            return tempCellIdMap;
        }
        for (String str : lastCellInfos.split(";")) {
            String[] tempCellInfo = str.split(",");
            if (tempCellInfo.length < 2) {
                break;
            }
            long tempValue = Long.valueOf(tempCellInfo[1]).longValue();
            if (SystemClock.elapsedRealtime() - tempValue <= this.mCellIdCacheTimer) {
                tempCellIdMap.put(Integer.valueOf(Integer.valueOf(tempCellInfo[0]).intValue()), Long.valueOf(tempValue));
            }
        }
        return tempCellIdMap;
    }

    public void countPackageUseCellInfo(String packageName) {
        boolean isCharging = false;
        boolean isMainSub = this.mPhoneId == HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (isMainSub || this.mCellInfoMap.size() != 0) {
            logd("countPackageUseCellInfo packageName is :" + packageName);
            if (!TextUtils.isEmpty(packageName)) {
                if (this.mStartTime == MODE_REQUEST_CELL_LIST_STRATEGY_VALID) {
                    this.mStartTime = SystemClock.elapsedRealtime();
                }
                if (Math.abs(SystemClock.elapsedRealtime() - this.mStartTime) >= 120000) {
                    String topPackageString = BuildConfig.FLAVOR;
                    int topPackageNum = 0;
                    int count = this.mCellInfoMap.size();
                    logd("countPackageUseCellInfo size:" + count);
                    for (Map.Entry<String, Integer> entry : this.mCellInfoMap.entrySet()) {
                        topPackageString = topPackageString + " name=" + entry.getKey() + " num=" + entry.getValue();
                        topPackageNum++;
                        if (topPackageNum == 10) {
                            HwLogEx.dubaie("DUBAI_TAG_LOCATION_COUNTER", "count=10" + topPackageString);
                            logd("countPackageUseCellInfo topPackageString:count=10" + topPackageString);
                            topPackageNum = 0;
                            topPackageString = BuildConfig.FLAVOR;
                            count += -10;
                        } else if (count / 10 == 0 && count - 1 == 0) {
                            HwLogEx.dubaie("DUBAI_TAG_LOCATION_COUNTER", "count=" + topPackageNum + topPackageString);
                            logd("countPackageUseCellInfo topPackageString:count=" + topPackageNum + topPackageString);
                        }
                    }
                    this.mStartTime = SystemClock.elapsedRealtime();
                    this.mCellInfoMap.clear();
                }
                BatteryManager batteryManager = (BatteryManager) this.mContext.getSystemService("batterymanager");
                if (batteryManager != null && batteryManager.isCharging()) {
                    isCharging = true;
                }
                if (isMainSub && isCustScreenOff() && !isCharging) {
                    if (this.mCellInfoMap.containsKey(packageName)) {
                        this.mCellInfoMap.put(packageName, Integer.valueOf(this.mCellInfoMap.get(packageName).intValue() + 1));
                    } else {
                        this.mCellInfoMap.put(packageName, 1);
                    }
                }
            }
        }
    }

    public void dispose() {
        logd("dispose for sst.");
        this.mCi.unregisterForRplmnsStateChanged(this.mHandler);
        this.mCi.unregisterForCaStateChanged(this.mHandler);
        this.mCi.unregisterForCrrConn(this.mHandler);
        this.mCi.unSetOnNetReject(this.mHandler);
        this.mCi.unSetOnRegPLMNSelInfo(this.mHandler);
        this.mCi.unregisterForDSDSMode(this.mHandler);
        this.mPhone.unregisterForMccChanged(this.mHandler);
        this.mCi.unregisterFor256QAMStatus(this.mHandler);
        this.mCi.unregisterForUnsol4RMimoStatus(this.mHandler);
        unregisterCloudOtaBroadcastReceiver();
        this.mCi.unRegisterForRrcConnectionStateChange(this.mHandler);
        this.mServiceStateTracker.unregisterForDataRegStateOrRatChanged(1, this.mHandler);
        unRegisterDisplayListener();
    }

    private void unRegisterDisplayListener() {
        ((DisplayManager) this.mPhone.getContext().getSystemService("display")).unregisterDisplayListener(this.mDisplayListener);
    }

    public void setPreferredNetworkTypeSafely(int networkType, Message response) {
        DcTrackerEx dcTracker = this.mPhone.getDcTracker();
        if (networkType != 10) {
            if (this.mIsSetPreNwTypeRequested) {
                this.mHandler.removeMessages(EVENT_SET_PRE_NETWORKTYPE);
                logd("cancel setPreferredNetworkType");
            }
            this.mIsSetPreNwTypeRequested = false;
            logd("PreNetworkType is not LTE, setPreferredNetworkType now!");
            setPreferredNetworkType(networkType, response);
        } else if (!this.mIsSetPreNwTypeRequested && dcTracker != null) {
            if (dcTracker.isDisconnected()) {
                setPreferredNetworkType(networkType, response);
                logd("data is Disconnected, setPreferredNetworkType now!");
                return;
            }
            this.mPhone.setInternalDataEnabled(false);
            logd("Data is disabled and wait up to 8s to resume data.");
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_RESUME_DATA), 8000);
            this.mPendingSavedMessage = response;
            this.mPendingPreNwType = networkType;
            Message msg = Message.obtain(this.mHandler);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = networkType;
            msg.obj = response;
            logd("Wait up to 5s for data disconnect to setPreferredNetworkType.");
            this.mHandler.sendMessageDelayed(msg, 5000);
            this.mIsSetPreNwTypeRequested = true;
        }
    }

    private void setPreferredNetworkType(int networkType, Message response) {
        this.mPhone.getCi().setPreferredNetworkType(networkType, response);
    }

    public void checkAndSetNetworkType() {
        if (this.mIsSetPreNwTypeRequested) {
            logd("mIsSetPreNwTypeRequested is true and wait a few seconds to setPreferredNetworkType");
            this.mHandler.removeMessages(EVENT_SET_PRE_NETWORKTYPE);
            Message msg = Message.obtain(this.mHandler);
            msg.what = EVENT_SET_PRE_NETWORKTYPE;
            msg.arg1 = this.mPendingPreNwType;
            msg.obj = this.mPendingSavedMessage;
            this.mHandler.sendMessageDelayed(msg, VALUE_CELL_INFO_LIST_MAX_AGE_MS);
            return;
        }
        logd("No need to setPreferredNetworkType");
    }

    public boolean isNeedLocationTimeZoneUpdate(String zoneId) {
        return HwTimeZoneManager.getInstance().isNeedLocationTimeZoneUpdate(this.mPhone, zoneId);
    }

    public boolean allowUpdateTimeFromNitz(long nitzTime) {
        return HwTimeZoneManager.getInstance().allowUpdateTimeFromNitz(this.mPhone, nitzTime);
    }

    public void sendNitzTimeZoneUpdateMessage(CellLocation cellLoc) {
        HwTimeZoneManager.getInstance().sendNitzTimeZoneUpdateMessage(cellLoc);
    }

    private void initNetworkRejInfo() {
        int simCount = TelephonyManagerEx.getDefault().getPhoneCount();
        this.mPhone.getCi().registerForSimHotPlug(this.mHandler, (int) EVENT_SIM_HOTPLUG, Integer.valueOf(this.mPhoneId));
        if (simCount == 2) {
            this.mPhone.getCi().setOnNetReject(this.mHandler, (int) EVENT_NETWORK_REJINFO_DONE, (Object) null);
            this.mContext.registerReceiver(this.mReceiverForDualSim, new IntentFilter("android.intent.action.SERVICE_STATE"));
            NotificationManager mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(new NotificationChannel(NETWORK_REJINFO_NOTIFY_CHANNEL, this.mContext.getString(HwPartResourceUtils.getResourceId("network_rejinfo_notification_app")), 3));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearNetworkRejInfo() {
        logd("clearNetworkRejInfo");
        this.mNetworkRejInfoLists.clear();
        this.mHandler.removeMessages(EVENT_NETWORK_REJINFO_TIMEOUT);
        setNeedHandleRejInfoFlag(false);
    }

    private void setNeedHandleRejInfoFlag(boolean isNeedHandleRejInfoFlag) {
        if (this.mIsNeedHandleRejInfoFlag != isNeedHandleRejInfoFlag) {
            this.mIsNeedHandleRejInfoFlag = isNeedHandleRejInfoFlag;
            logd("set mIsNeedHandleRejInfoFlag =" + this.mIsNeedHandleRejInfoFlag);
        }
    }

    private void registerCloudOtaBroadcastReceiver() {
        logi("HwCloudOTAService registerCloudOtaBroadcastReceiver");
        this.mCloudOtaBroadcastReceiver = new CloudOtaBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CLOUD_OTA_MCC_UPDATE);
        filter.addAction(CLOUD_OTA_DPLMN_UPDATE);
        this.mContext.registerReceiver(this.mCloudOtaBroadcastReceiver, filter, CLOUD_OTA_PERMISSION, null);
    }

    private void unregisterCloudOtaBroadcastReceiver() {
        logi("HwCloudOTAService unregisterCloudOtaBroadcastReceiver");
        CloudOtaBroadcastReceiver cloudOtaBroadcastReceiver = this.mCloudOtaBroadcastReceiver;
        if (cloudOtaBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(cloudOtaBroadcastReceiver);
            this.mCloudOtaBroadcastReceiver = null;
        }
    }

    public boolean[] getMobilePhysicsLayerStatus() {
        boolean[] zArr = this.mMplStatus;
        if (zArr != null) {
            return (boolean[]) zArr.clone();
        }
        return null;
    }

    public int getRrcConnectionState() {
        return this.mRrcConnectionState;
    }

    private void addBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("android.intent.action.refreshapn");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        if (HwTelephonyManager.getDefault().isNrSupported() && !HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported()) {
            filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        }
        if (this.mHwGsmOnsDisplayParamsManager.getHwCustGsmServiceStateManager() != null) {
            filter = this.mHwGsmOnsDisplayParamsManager.getHwCustGsmServiceStateManager().getCustIntentFilter(filter);
        }
        this.mBaseIntentReceiver = new BaseBroadcastReceiver();
        this.mContext.registerReceiver(this.mBaseIntentReceiver, filter);
    }

    private void removeBroadcastReceiver() {
        BaseBroadcastReceiver baseBroadcastReceiver = this.mBaseIntentReceiver;
        if (baseBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(baseBroadcastReceiver);
            this.mBaseIntentReceiver = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRefreshApnForGsm() {
        if (this.mPhone.isPhoneTypeGsm()) {
            logi("refresh apn worked,updateSpnDisplay.");
            this.mServiceStateTracker.updateSpnDisplayHw();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimStateChangedForGsm(Intent intent) {
        if (this.mPhone.isPhoneTypeGsm()) {
            String simState = intent.getStringExtra("ss");
            int slotId = intent.getIntExtra("phone", -1000);
            logi("simState = " + simState + " slotId = " + slotId);
            if ("ABSENT".equals(simState)) {
                logi("sim absent, reset");
                if (SystemPropertiesEx.getInt("gsm.sim.updatenitz", -1) != -1) {
                    SystemPropertiesEx.set("gsm.sim.updatenitz", String.valueOf(-1));
                }
                if (slotId == this.mPhoneId) {
                    this.mRejCause = -1;
                    logi("sim absent, clear mRejCause");
                }
            } else if ("LOADED".equals(simState) && slotId == this.mPhoneId) {
                logi("after simrecords loaded,updateSpnDisplay for virtualnet ons.");
                this.mServiceStateTracker.updateSpnDisplayHw();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSubInfoContentChangedForGsm(Intent intent) {
        if (this.mPhone.isPhoneTypeGsm()) {
            int slotId = intent.getIntExtra("slot", -1);
            int intValue = intent.getIntExtra("intContent", 0);
            String column = intent.getStringExtra("columnName");
            logi("Received ACTION_SUBINFO_CONTENT_CHANGE on slotId: " + slotId + " for " + column + ", intValue: " + intValue);
            StringBuilder sb = new StringBuilder();
            sb.append("PROPERTY_GSM_SIM_UPDATE_NITZ=");
            sb.append(SystemPropertiesEx.getInt("gsm.sim.updatenitz", -1));
            logi(sb.toString());
            if ("sub_state".equals(column) && slotId != -1 && intValue == 0 && slotId == SystemPropertiesEx.getInt("gsm.sim.updatenitz", -1)) {
                logi("reset PROPERTY_GSM_SIM_UPDATE_NITZ");
                SystemPropertiesEx.set("gsm.sim.updatenitz", String.valueOf(-1));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAirplaneModeChanged() {
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean isAirplaneModeOn = false;
            if (Settings.Global.getInt(this.mCr, "airplane_mode_on", 0) == 1) {
                isAirplaneModeOn = true;
            }
            logd("isAirplaneModeOn: " + isAirplaneModeOn);
            if (isAirplaneModeOn) {
                SystemPropertiesEx.set("gsm.sim.updatenitz", String.valueOf(-1));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleVoiceCallEnded() {
        logd("reregistered failure: " + this.mReregisteredResultFlag);
        if (this.mReregisteredResultFlag) {
            this.mReregisteredResultFlag = false;
            this.mCi.getPreferredNetworkType(this.mHandler.obtainMessage(EVENT_CHECK_PREFERRED_NETWORK_TYPE));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCheckPreferedNetworkType(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && ar.getException() == null && (ar.getResult() instanceof int[])) {
            int modemConfig = ((int[]) ar.getResult())[0];
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            int userConfig = Settings.Global.getInt(contentResolver, "preferred_network_mode" + this.mPhone.getPhoneId(), 7);
            logd("modemConfig=" + modemConfig + " userConfig=" + userConfig);
            if (modemConfig != userConfig) {
                this.mCi.setPreferredNetworkType(userConfig, (Message) null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRrcConnectionStateChange(Message msg) {
        logd("handleRrcConnectionStateChange");
        if (allowHandleRrcConnectionStateChange()) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() != null) {
                loge("handleRrcConnectionStateChange exception " + ar.getException());
                return;
            }
            if (ar.getResult() instanceof Integer) {
                this.mRrcConnectionState = ((Integer) ar.getResult()).intValue();
            }
            logd("mRrcConnectionState:" + this.mRrcConnectionState + " mPreRrcConState:" + this.mPreRrcConState);
            int i = this.mRrcConnectionState;
            if (i != 3 && i != 4 && i != this.mPreRrcConState) {
                if (!ratChangedDelayFeatureOpened()) {
                    this.mPreRrcConState = this.mRrcConnectionState;
                    return;
                }
                ServiceState oldSs = this.mServiceStateTracker.getmSSHw();
                if (!needDelayForRatChanged(oldSs, updateRatForRrcChanged(new ServiceState(oldSs)))) {
                    handleDelayRatChangedExpired();
                }
                this.mPreRrcConState = this.mRrcConnectionState;
            }
        }
    }

    private boolean ratChangedDelayFeatureOpened() {
        String delayTime = SystemPropertiesEx.get(RAT_CHANGED_DELAY_TIMER, BuildConfig.FLAVOR);
        logd("ratChangedDelayFeatureOpened: " + delayTime);
        if (TextUtils.isEmpty(delayTime)) {
            return false;
        }
        return true;
    }

    private ServiceState updateRatForRrcChanged(ServiceState ss) {
        NetworkRegistrationInfoEx regInfoEx = ServiceStateEx.getNetworkRegistrationInfo(ss, 2, 1);
        int rat = getNrConfigTechnology(ServiceStateEx.getRilDataRadioTechnology(ss), regInfoEx);
        logd("handleRrcConnectionStateChange rat:" + rat);
        regInfoEx.setConfigRadioTechnology(rat);
        ServiceStateEx.addNetworkRegistrationInfo(ss, regInfoEx);
        return ss;
    }

    private boolean allowHandleRrcConnectionStateChange() {
        if (!HwTelephonyManager.getDefault().isNrSupported()) {
            return false;
        }
        if (!HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported()) {
            HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
            int mainSlot = hwTelephonyManager.getDefault4GSlotId();
            if (!(this.mPhoneId == mainSlot && isNrSwitchOn(mainSlot) && hwTelephonyManager.getDefault4GSlotId() == this.mPhoneId)) {
                return false;
            }
        } else if (!isNrSwitchOn(this.mPhoneId)) {
            return false;
        }
        String config = SystemPropertiesEx.get(NR_TECHNOLOGY_CONFIG);
        if (TextUtils.isEmpty(config) || !NR_TECHNOLOGY_CONFIGAD.equals(config)) {
            return false;
        }
        int dataRat = ServiceStateEx.getDataNetworkType(this.mServiceStateTracker.getmSSHw());
        logd("data rat is :" + dataRat);
        if (dataRat != 20) {
            return true;
        }
        logd("data rat is nr, sa");
        return false;
    }

    public boolean needDelayForRatChanged(ServiceState oldSs, ServiceState newSs) {
        if (!ratChangedDelayFeatureOpened()) {
            return false;
        }
        if (!HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported()) {
            int mainSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
            if (this.mPhoneId != mainSlot) {
                removeRatChangedDelyaMessage();
                return false;
            } else if (!isNrSwitchOn(mainSlot)) {
                removeRatChangedDelyaMessage();
                return false;
            }
        } else if (!isNrSwitchOn(this.mPhoneId)) {
            removeRatChangedDelyaMessage();
            return false;
        }
        if (getCombinedRegState(newSs) != 0) {
            removeRatChangedDelyaMessage();
            return false;
        }
        int rat = ServiceStateEx.networkTypeToRilRadioTechnology(ServiceStateEx.getConfigRadioTechnology(newSs));
        if (ServiceStateEx.isLte(rat) || rat == 20) {
            if (this.mPhone.getPhoneType() == 1) {
                if (this.mHwGsmSignalStengthManager.needCancelDelay()) {
                    removeRatChangedDelyaMessage();
                    return false;
                }
            } else if (this.mHwCdmaSignalStengthManager.needCancelDelay()) {
                removeRatChangedDelyaMessage();
                return false;
            }
            if (this.mHandler.hasMessages(EVENT_DELAY_RAT_CHANGED)) {
                logd("In rat changed delay process.");
                return true;
            }
            boolean isMainSlot = this.mPhone.getPhoneId() == HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            if (!ratChangeFromNrToLte(oldSs, newSs) || !this.mNeedDelayUpdate || (!isMainSlot && !HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported())) {
                this.mNeedDelayUpdate = true;
                return false;
            }
            if (!this.mHandler.hasMessages(EVENT_DELAY_RAT_CHANGED)) {
                Message msgDelay = this.mHandler.obtainMessage(EVENT_DELAY_RAT_CHANGED);
                int delayTime = getDelayTimeValue(oldSs, newSs);
                this.mHandler.sendMessageDelayed(msgDelay, (long) delayTime);
                logd("rat changed form nr to lte, so delay " + delayTime + "ms");
            } else {
                logd("process rat changed delay.");
            }
            return true;
        }
        logd("current rat is not lte or nr, remove delay message.");
        removeRatChangedDelyaMessage();
        return false;
    }

    private int getDelayTimeValue(ServiceState oldSs, ServiceState newSs) {
        String delayTimer = SystemPropertiesEx.get(RAT_CHANGED_DELAY_TIMER, BuildConfig.FLAVOR);
        if (TextUtils.isEmpty(delayTimer)) {
            return 0;
        }
        String[] delayTimerValues = delayTimer.split(",");
        if (delayTimerValues.length != 2) {
            return 0;
        }
        int timerA = 0;
        int timerB = 0;
        try {
            timerA = Integer.parseInt(delayTimerValues[0]);
            timerB = Integer.parseInt(delayTimerValues[1]);
        } catch (NumberFormatException e) {
            logd("Parse sortNumOfTimer exception");
        }
        if (isNeedToStartTimeA()) {
            int delayTime = timerA * 1000;
            logd("Need to start TimeA");
            return delayTime;
        } else if (!isAnchorCellToNonAnchorCell(oldSs, newSs)) {
            return timerB * 1000;
        } else {
            boolean is5Gto4GDisableDelay = false;
            Boolean is5Gto4GDisableDelayCustValue = (Boolean) HwCfgFilePolicy.getValue("carrier_5g_to_4g_not_delay", this.mPhoneId, Boolean.class);
            if (is5Gto4GDisableDelayCustValue != null) {
                is5Gto4GDisableDelay = is5Gto4GDisableDelayCustValue.booleanValue();
            }
            if (is5Gto4GDisableDelay) {
                logd("Set TimeA to 0 seconds.");
                return 0;
            }
            int delayTime2 = timerA * 1000;
            logd("Set TimeA by the parameter.");
            return delayTime2;
        }
    }

    private boolean isNeedToStartTimeA() {
        PhoneExt phoneExt;
        if (this.mPreRrcConState != 0 || this.mRrcConnectionState != 1 || (phoneExt = this.mPhone) == null || ServiceStateEx.getNsaState(phoneExt.getServiceState()) == 1) {
            return false;
        }
        return true;
    }

    private boolean isAnchorCellToNonAnchorCell(ServiceState oldSs, ServiceState newSs) {
        PhoneExt phoneExt;
        if (!ServiceStateEx.isNrAvailable(oldSs, 3) || ServiceStateEx.isNrAvailable(newSs, 3) || (phoneExt = this.mPhone) == null || ServiceStateEx.getNsaState(phoneExt.getServiceState()) == 5 || this.mPreRrcConState != 0 || this.mRrcConnectionState != 0) {
            return false;
        }
        return true;
    }

    public boolean isNrSwitchOn(int phoneId) {
        if (HwTelephonyManager.getDefault().getServiceAbility(phoneId, 1) == 1) {
            return true;
        }
        return false;
    }

    private boolean ratChangeFromNrToLte(ServiceState oldSs, ServiceState newSs) {
        logd("old rat:" + ServiceStateEx.getConfigRadioTechnology(oldSs) + "old data rat" + ServiceStateEx.getRilDataRadioTechnology(oldSs) + " new rat :" + ServiceStateEx.getConfigRadioTechnology(newSs));
        if (ServiceStateEx.getConfigRadioTechnology(oldSs) != 20 || ServiceStateEx.getRilDataRadioTechnology(oldSs) == 20 || !ServiceStateEx.isLte(ServiceStateEx.networkTypeToRilRadioTechnology(ServiceStateEx.getConfigRadioTechnology(newSs)))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDelayRatChangedExpired() {
        this.mNeedDelayUpdate = false;
        this.mServiceStateTracker.pollState();
        this.mPhone.getCi().getSignalStrength(this.mHandler.obtainMessage(EVENT_GET_SIGNAL_STRENGTH));
    }

    public void removeRatChangedDelyaMessage() {
        if (this.mHandler.hasMessages(EVENT_DELAY_RAT_CHANGED)) {
            logd("cancel rat changed delay");
            this.mPhone.getCi().getSignalStrength(this.mHandler.obtainMessage(EVENT_GET_SIGNAL_STRENGTH));
            this.mHandler.removeMessages(EVENT_DELAY_RAT_CHANGED);
        }
        this.mNeedDelayUpdate = true;
    }

    public boolean hasRatChangedDelayMessage() {
        return this.mHandler.hasMessages(EVENT_DELAY_RAT_CHANGED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDataRatChanged() {
        if (HwTelephonyManager.getDefault().getDefault4GSlotId() != this.mPhoneId) {
            logd("Not main slot.");
            return;
        }
        int dataRat = 0;
        ServiceState ss = this.mPhone.getServiceState();
        if (ss != null) {
            dataRat = ServiceStateEx.getRilDataRadioTechnology(ss);
        }
        if (this.mPreDataRat == 20 && ServiceStateEx.isLte(dataRat)) {
            this.mPhone.getCi().getRrcConnectionState(this.mHandler.obtainMessage(EVENT_GET_RRC_CONNECTION_STATE));
        }
        this.mPreDataRat = dataRat;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSwitchSlotDone(Intent intent) {
        if (intent.getIntExtra("HW_SWITCH_SLOT_STEP", -1) == 1 && HwTelephonyManager.getDefault().getDefault4GSlotId() != this.mPhoneId && hasRatChangedDelayMessage()) {
            if (this.mHandler.hasMessages(EVENT_DELAY_RAT_CHANGED)) {
                this.mHandler.removeMessages(EVENT_DELAY_RAT_CHANGED);
            }
            handleDelayRatChangedExpired();
        }
    }

    public void addHwServiceStateListener(HwServiceStateListener serviceStateListener) {
        synchronized (this.mHwServiceStateListenerList) {
            this.mHwServiceStateListenerList.add(serviceStateListener);
        }
    }

    public void removeHwServiceStateListener(HwServiceStateListener serviceStateListener) {
        synchronized (this.mHwServiceStateListenerList) {
            this.mHwServiceStateListenerList.remove(serviceStateListener);
        }
    }

    public void notifyHwServiceStateChanged(ServiceState ss) {
        synchronized (this.mHwServiceStateListenerList) {
            Iterator<HwServiceStateListener> it = this.mHwServiceStateListenerList.iterator();
            while (it.hasNext()) {
                it.next().onServiceStateChanged(this.mPhoneId, new ServiceState(ss));
            }
        }
    }

    public void notifyHwSignalStrengthChanged(SignalStrength signalStrength) {
        synchronized (this.mHwServiceStateListenerList) {
            Iterator<HwServiceStateListener> it = this.mHwServiceStateListenerList.iterator();
            while (it.hasNext()) {
                it.next().onSignalStrengthChanged(this.mPhoneId, SignalStrengthEx.newSignalStrength(signalStrength));
            }
        }
    }

    public boolean hasCellIdMapContain(int cellId) {
        return this.mCellIdMap.containsKey(Integer.valueOf(cellId));
    }

    public int getRac() {
        return this.mRac;
    }

    public void setRac(int rac) {
        this.mRac = rac;
    }

    public int getRejCause() {
        return this.mRejCause;
    }

    public void clearRejCause() {
        this.mRejCause = -1;
    }

    public int getChangedNsaState() {
        return this.mChangedNsaState;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String str) {
        RlogEx.i(this.mTag, str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String str) {
        RlogEx.i(this.mTag, str);
    }

    private void loge(String str) {
        RlogEx.e(this.mTag, str);
    }

    private class SstExDisplayListener implements DisplayManager.DisplayListener {
        private SstExDisplayListener() {
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
                DisplayManager dm = (DisplayManager) HwServiceStateTrackerEx.this.mPhone.getContext().getSystemService("display");
                int displayState = 1;
                if (!(dm == null || dm.getDisplay(0) == null)) {
                    displayState = dm.getDisplay(0).getState();
                }
                int mainSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
                HwServiceStateTrackerEx hwServiceStateTrackerEx = HwServiceStateTrackerEx.this;
                hwServiceStateTrackerEx.logi("onDisplayChanged displayState: " + displayState);
                if (displayState == 2 && mainSlot == HwServiceStateTrackerEx.this.mPhone.getPhoneId() && HwTelephonyManager.getDefault().isNrSupported()) {
                    HwServiceStateTrackerEx.this.mPhone.getCi().getRrcConnectionState(HwServiceStateTrackerEx.this.mHandler.obtainMessage(HwServiceStateTrackerEx.EVENT_GET_RRC_CONNECTION_STATE));
                }
            }
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwServiceStateTrackerEx hwServiceStateTrackerEx = HwServiceStateTrackerEx.this;
            hwServiceStateTrackerEx.logd("msg.what = " + msg.what);
            switch (msg.what) {
                case HwServiceStateTrackerEx.EVENT_RPLMNS_STATE_CHANGED /* 101 */:
                    HwServiceStateTrackerEx.this.handleRplmnsStateChanged(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_CA_STATE_CHANGED /* 102 */:
                    HwServiceStateTrackerEx.this.handleCaStateChanged(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_ICC_RECORDS_EONS_UPDATED /* 103 */:
                    HwServiceStateTrackerEx.this.handleIccRecordsEonsUpdated(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_POLL_LOCATION_INFO /* 104 */:
                    HwServiceStateTrackerEx.this.handlePollLocationInfo(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_NITZ_CAPABILITY_NOTIFICATION /* 105 */:
                    HwServiceStateTrackerEx.this.handleSendTimeZoneSelectionNotification();
                    return;
                case HwServiceStateTrackerEx.EVENT_RESUME_DATA /* 106 */:
                    HwServiceStateTrackerEx.this.handleResumeData();
                    return;
                case HwServiceStateTrackerEx.EVENT_SET_PRE_NETWORKTYPE /* 107 */:
                    HwServiceStateTrackerEx.this.handleSetPreNetworkType(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_SIM_HOTPLUG /* 108 */:
                    HwServiceStateTrackerEx.this.logd("EVENT_SIM_HOTPLUG");
                    HwServiceStateTrackerEx.this.handleSimHotPlug(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_NETWORK_REJINFO_DONE /* 109 */:
                    HwServiceStateTrackerEx.this.logd("EVENT_NETWORK_REJINFO_DONE");
                    HwServiceStateTrackerEx.this.handleNetworkRejInfoDone(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_NETWORK_REJINFO_TIMEOUT /* 110 */:
                    HwServiceStateTrackerEx.this.logd("EVENT_NETWORK_REJINFO_TIMEOUT");
                    HwServiceStateTrackerEx.this.handleNetworkRejInfoTimeout();
                    return;
                case HwServiceStateTrackerEx.EVENT_CRR_CONN /* 111 */:
                    HwServiceStateTrackerEx.this.logd("EVENT_CRR_CONN");
                    HwServiceStateTrackerEx.this.handleCrrConn(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_NETWORK_REJECTED_CASE /* 112 */:
                    HwServiceStateTrackerEx.this.handleNetworkRejectedCase(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_PLMN_SELINFO /* 113 */:
                    HwServiceStateTrackerEx.this.handlePlmnSelInfo(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_DSDS_MODE /* 114 */:
                    HwServiceStateTrackerEx.this.handleDsdsMode(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_MCC_CHANGED /* 115 */:
                    HwServiceStateTrackerEx.this.handleMccChanged();
                    return;
                case HwServiceStateTrackerEx.EVENT_256QAM_STATE_CHANGE /* 116 */:
                    HwServiceStateTrackerEx.this.handle256QamStateChange(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_4R_MIMO_ENABLE /* 117 */:
                    HwServiceStateTrackerEx.this.handle4GMimoEnable(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_VOICE_CALL_ENDED /* 118 */:
                    HwServiceStateTrackerEx.this.handleVoiceCallEnded();
                    return;
                case HwServiceStateTrackerEx.EVENT_CHECK_PREFERRED_NETWORK_TYPE /* 119 */:
                    HwServiceStateTrackerEx.this.handleCheckPreferedNetworkType(msg);
                    return;
                case 120:
                    HwServiceStateTrackerEx.this.handleRrcConnectionStateChange(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_DELAY_RAT_CHANGED /* 121 */:
                    HwServiceStateTrackerEx.this.logd("EVENT_DELAY_RAT_CHANGED");
                    HwServiceStateTrackerEx.this.handleDelayRatChangedExpired();
                    return;
                case HwServiceStateTrackerEx.EVENT_GET_SIGNAL_STRENGTH /* 122 */:
                    HwServiceStateTrackerEx.this.mServiceStateTracker.onSignalStrengthResultHw(msg);
                    return;
                case HwServiceStateTrackerEx.EVENT_RAT_CHANGED /* 123 */:
                    HwServiceStateTrackerEx.this.logd("EVENT_RAT_CHANGED");
                    HwServiceStateTrackerEx.this.handleDataRatChanged();
                    return;
                case HwServiceStateTrackerEx.EVENT_GET_RRC_CONNECTION_STATE /* 124 */:
                    HwServiceStateTrackerEx.this.logd("EVENT_GET_RRC_CONNECTION_STATE");
                    HwServiceStateTrackerEx.this.handleRrcConnectionStateChange(msg);
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public class CloudOtaBroadcastReceiver extends BroadcastReceiver {
        private CloudOtaBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwServiceStateTrackerEx.this.mPhone != null && HwServiceStateTrackerEx.this.mPhone.getCi() != null && intent != null) {
                String action = intent.getAction();
                if (HwServiceStateTrackerEx.CLOUD_OTA_MCC_UPDATE.equals(action)) {
                    HwServiceStateTrackerEx.this.logi("HwCloudOTAService CLOUD_OTA_MCC_UPDATE");
                    HwServiceStateTrackerEx.this.mPhone.getCi().sendCloudMessageToModem(1);
                } else if (HwServiceStateTrackerEx.CLOUD_OTA_DPLMN_UPDATE.equals(action)) {
                    HwServiceStateTrackerEx.this.logi("HwCloudOTAService CLOUD_OTA_DPLMN_UPDATE");
                    HwServiceStateTrackerEx.this.mPhone.getCi().sendCloudMessageToModem(2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class BaseBroadcastReceiver extends BroadcastReceiver {
        private BaseBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                    if (!HwServiceStateTrackerEx.this.mPhone.isPhoneTypeGsm()) {
                        HwServiceStateTrackerEx.this.mServiceStateTracker.updateSpnDisplayHw();
                    }
                } else if ("android.intent.action.refreshapn".equals(action)) {
                    HwServiceStateTrackerEx.this.handleRefreshApnForGsm();
                } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    HwServiceStateTrackerEx.this.handleSimStateChangedForGsm(intent);
                } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(action)) {
                    HwServiceStateTrackerEx.this.handleSubInfoContentChangedForGsm(intent);
                } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    HwServiceStateTrackerEx.this.handleAirplaneModeChanged();
                } else if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(action)) {
                    HwServiceStateTrackerEx.this.handleSwitchSlotDone(intent);
                } else if (HwServiceStateTrackerEx.this.mHwGsmOnsDisplayParamsManager.getHwCustGsmServiceStateManager() != null) {
                    HwServiceStateTrackerEx hwServiceStateTrackerEx = HwServiceStateTrackerEx.this;
                    hwServiceStateTrackerEx.mRac = hwServiceStateTrackerEx.mHwGsmOnsDisplayParamsManager.getHwCustGsmServiceStateManager().handleBroadcastReceived(context, intent, HwServiceStateTrackerEx.this.mRac);
                }
            }
        }
    }
}
