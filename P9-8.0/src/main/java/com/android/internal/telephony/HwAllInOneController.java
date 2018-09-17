package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.HwFullNetwork.CommrilMode;
import com.android.internal.telephony.HwFullNetwork.HotplugState;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HwAllInOneController extends Handler {
    private static final /* synthetic */ int[] -com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues = null;
    private static final byte[] C2 = new byte[]{(byte) -89, (byte) 82, (byte) 3, (byte) 85, (byte) -88, (byte) -104, (byte) 57, (byte) -10, (byte) -103, (byte) 108, (byte) -88, (byte) 122, (byte) -38, (byte) -12, (byte) -55, (byte) -2};
    public static final int CARD_TYPE_DUAL_MODE = 3;
    public static final int CARD_TYPE_NO_SIM = 0;
    public static final String CARD_TYPE_SIM1 = "gsm.sim1.type";
    public static final String CARD_TYPE_SIM2 = "gsm.sim2.type";
    public static final int CARD_TYPE_SINGLE_CDMA = 2;
    public static final int CARD_TYPE_SINGLE_GSM = 1;
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    private static final int DEFAULT_NETWORK_MODE = SystemProperties.getInt("ro.telephony.default_network", -1);
    public static final int DEFAULT_VALUE = 0;
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_TELECOM_LTE_CARD = 43;
    public static final int DUAL_MODE_UG_CARD = 50;
    private static final int EVENT_CHANGE_PREF_NETWORK_DONE = 13;
    private static final int EVENT_CHECK_ALL_CARDS_READY = 108;
    private static final int EVENT_CMCC_SET_NETWOR_DONE = 17;
    private static final int EVENT_DELAYED_SET_PREF_NETWORK = 1;
    private static final int EVENT_FAST_SWITCH_SIM_SLOT_DONE = 114;
    private static final int EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT = 115;
    private static final int EVENT_GET_BALONG_SIM_DONE = 103;
    private static final int EVENT_GET_CDMA_MODE_SIDE_DONE = 116;
    private static final int EVENT_GET_ICCID_DONE = 107;
    private static final int EVENT_GET_PREF_NETWORK_DONE = 11;
    private static final int EVENT_GET_PREF_NETWORK_MODE_DONE = 112;
    private static final int EVENT_ICC_GET_ATR_DONE = 106;
    private static final int EVENT_ICC_STATUS_CHANGED = 1;
    private static final int EVENT_QUERY_CARD_TYPE_DONE = 102;
    private static final int EVENT_RADIO_AVAILABLE = 104;
    private static final int EVENT_RADIO_ON = 12;
    private static final int EVENT_RADIO_ON_PROCESS_SIM_STATE = 3;
    private static final int EVENT_RADIO_ON_SET_PREF_NETWORK = 2;
    private static final int EVENT_RADIO_UNAVAILABLE = 3;
    private static final int EVENT_RESET_OOS_FLAG = 118;
    private static final int EVENT_SET_DATA_ALLOW_DONE = 15;
    private static final int EVENT_SET_PREF_NETWORK_DONE = 10;
    private static final int EVENT_SET_PREF_NETWORK_TIMEOUT = 14;
    private static final int EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE = 109;
    private static final int EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE = 111;
    private static final int EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE = 110;
    private static final int EVENT_SIM_HOTPLUG = 16;
    private static final int EVENT_SWITCH_DUAL_CARD_IF_NEEDED = 117;
    private static final int EVENT_SWITCH_DUAL_CARD_SLOT = 101;
    private static final int EVENT_SWITCH_SIM_SLOT_CFG_DONE = 105;
    private static final int EVENT_VOICE_CALL_ENDED = 113;
    public static final int EXTRA_VALUE_INVALID = -1;
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final int HW_SWITCH_SLOT_FAIL = -1;
    private static final int HW_SWITCH_SLOT_START = 0;
    private static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final int ICCID_LEN_MINIMUM = 7;
    public static final int ICC_CARD = 1;
    public static final String IF_NEED_SET_RADIO_CAP = "if_need_set_radio_cap";
    private static final int INVALID = -1;
    private static final int INVALID_NETWORK_MODE = -1;
    private static final boolean IS_4G_SWITCH_SUPPORTED = SystemProperties.getBoolean("persist.sys.dualcards", false);
    public static final boolean IS_CARD2_CDMA_SUPPORTED = SystemProperties.getBoolean("ro.hwpp.card2_cdma_support", false);
    private static final boolean IS_CHINA_TELECOM;
    public static final boolean IS_CMCC_4GSWITCH_DISABLE;
    private static final boolean IS_CMCC_4G_DSDX_ENABLE = SystemProperties.getBoolean("ro.hwpp.cmcc_4G_dsdx_enable", false);
    private static final boolean IS_CMCC_CU_DSDX_ENABLE = SystemProperties.getBoolean("ro.hwpp.cmcc_cu_dsdx_enable", false);
    public static final boolean IS_CT_4GSWITCH_DISABLE = "ct".equalsIgnoreCase(SystemProperties.get("ro.hwpp.dualsim_swap_solution", ""));
    private static final boolean IS_DUAL_4G_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false);
    private static final boolean IS_FULL_NETWORK_SUPPORTED = SystemProperties.getBoolean("ro.config.full_network_support", false);
    private static final boolean IS_FULL_NETWORK_SUPPORTED_IN_HISI;
    public static final boolean IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT;
    public static final boolean IS_HISI_DSDX;
    public static final boolean IS_QCRIL_CROSS_MAPPING = SystemProperties.getBoolean("ro.hwpp.qcril_cross_mapping", false);
    private static final boolean IS_SINGLE_CARD_TRAY = SystemProperties.getBoolean("persist.radio.single_card_tray", true);
    public static final boolean IS_SUPPORT_CDMA = SystemProperties.getBoolean("ro.config.hisi_cdma_supported", true);
    public static final boolean IS_VICE_WCDMA = SystemProperties.getBoolean("ro.config.support_wcdma_modem1", false);
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    private static final String MAIN_CARD_INDEX = "main_card_id";
    public static final String MASTER_PASSWORD = HwAESCryptoUtil.getKey(SubscriptionHelper.C1, C2, SubscriptionInfoUpdaterUtils.C3);
    private static final int MCCMNC_LEN_MINIMUM = 5;
    private static final int MESSAGE_PENDING_DELAY = 500;
    private static final int MESSAGE_RETRY_PENDING_DELAY = 3000;
    private static final int MODEM0 = 0;
    private static final int MODEM1 = 1;
    private static final int MODEM2 = 2;
    private static final int MSG_RETRY_CHANGE_PREF_NETWORK = 2;
    private static final int MSG_RETRY_SET_DEFAULT_LTESLOT = 1;
    private static final String NETWORK_MODE_2G_ONLY = "network_mode_2G_only";
    private static final String NETWORK_MODE_3G_PRE = "network_mode_3G_pre";
    private static final String NETWORK_MODE_4G_PRE = "network_mode_4G_pre";
    public static final int NO_NEED_SET_RADIO_CAP = 1;
    private static final int OOS_DELAY_TIME = 20000;
    private static final String PROPERTY_HW_OPTA_TELECOM = "92";
    private static final String PROPERTY_HW_OPTB_CHINA = "156";
    public static final String PROP_MAIN_STACK = "persist.radio.msim.stackid_0";
    private static final boolean RESET_PROFILE = SystemProperties.getBoolean("ro.hwpp_reset_profile", false);
    private static final int RETRY_CHANGE_MAX_TIME = 20;
    private static final int RETRY_MAX_TIME = 20;
    private static final int REVIEW_4G_MODE_AUTO = 1;
    private static final int REVIEW_4G_MODE_FIX = 2;
    private static final int REVIEW_4G_MODE_NONE = 0;
    private static final String ROAMINGSTATE_PREF = "lastroamingstate";
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    private static final int SLOT1 = 1;
    private static final int SLOT2 = 2;
    private static final int SUB_0 = 0;
    private static final int SUB_1 = 1;
    private static final int SUB_BOTH = 10;
    private static final int SUB_ERROR = -1;
    private static final int TIME_FAST_SWITCH_SIM_SLOT_TIMEOUT = 60000;
    private static final int TIME_SET_PREF_NETWORK_TIMEOUT = 60000;
    public static final int UICC_CARD = 2;
    public static final int UNKNOWN_CARD = -1;
    public static final String USER_DEFAULT_SUBSCRIPTION = "user_default_sub";
    private static HwHotplugController mHotPlugController;
    private static HwAllInOneController mInstance;
    private static final Object mLock = new Object();
    private String TAG = "HwAllInOneController";
    private int curSetDataAllowCount = 0;
    private int current4GSlotBackup = 0;
    private Map<Integer, SubType> currentSubTypeMap = new HashMap();
    private int default4GSlot = 0;
    int expectedDDSsubId = -1;
    private int is4GSlotReviewNeeded = 0;
    private boolean isHotPlugCompleted = false;
    private boolean isPreBootCompleted = false;
    private boolean isSet4GSlotInProgress = false;
    public boolean isSet4GSlotManuallyTriggered = false;
    private boolean[] isSimInsertedArray = new boolean[SIM_NUM];
    private boolean isVoiceCallEndedRegistered = false;
    private boolean mAllCardsReady = false;
    private boolean mAutoSwitchDualCardsSlotDone = false;
    private int mBalongSimSlot = 0;
    private boolean mBroadcastDone = false;
    private int[] mCardTypes = new int[SIM_NUM];
    private CommandsInterface[] mCis;
    private int mCmccSubIdOldState = 1;
    private boolean mCommrilRestartRild = false;
    Context mContext;
    private LinkedList<DelayedEvent> mEventsQ = new LinkedList();
    private String[] mFullIccIds = new String[SIM_NUM];
    private boolean[] mGetBalongSimSlotDone = new boolean[SIM_NUM];
    private boolean[] mGetUiccCardsStatusDone = new boolean[SIM_NUM];
    private boolean mHas3Modem = false;
    private RegistrantList mIccChangedRegistrants = new RegistrantList();
    private String[] mIccIds = new String[SIM_NUM];
    private int[] mModemPreferMode = new int[SIM_NUM];
    private boolean mNeedSetAllowData = false;
    private boolean mNeedSetLteServiceAbility = false;
    private int mNumOfGetPrefNwModeSuccess = 0;
    private boolean mNvRestartRildDone = false;
    private int[] mPrefNwMode = new int[SIM_NUM];
    private int mPrimaryStackNetworkType = -1;
    private int mPrimaryStackPhoneId = -1;
    private boolean[] mRadioOns = new boolean[SIM_NUM];
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwAllInOneController.this.loge("intent is null, return");
                return;
            }
            int slotId;
            if ("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED".equals(intent.getAction())) {
                int status = intent.getIntExtra("simDetectStatus", -1);
                if (HwModemCapability.isCapabilitySupport(9)) {
                    if (!(!HwAllInOneController.IS_QCRIL_CROSS_MAPPING || status == -1 || status == 4)) {
                        HwAllInOneController.this.mNeedSetLteServiceAbility = true;
                    }
                    if (status != -1) {
                        HwAllInOneController.this.processSubInfoRecordUpdated(status);
                    }
                } else {
                    HwAllInOneController.this.logd("Not process for HISI");
                }
            } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(intent.getAction())) {
                slotId = intent.getIntExtra("subscription", -1000);
                int intValue = intent.getIntExtra("intContent", 0);
                String column = intent.getStringExtra("columnName");
                if (!HwModemCapability.isCapabilitySupport(9)) {
                    HwAllInOneController.this.logd("Not process for HISI");
                } else if ("sub_state".equals(column) && -1000 != slotId) {
                    HwAllInOneController.this.processSubStateChanged(slotId, intValue);
                }
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                if (HwModemCapability.isCapabilitySupport(9)) {
                    String simState = (String) intent.getExtra("ss");
                    slotId = intent.getIntExtra("slot", -1000);
                    if ((1 == HwAllInOneController.this.is4GSlotReviewNeeded || 2 == HwAllInOneController.this.is4GSlotReviewNeeded) && -1000 != slotId) {
                        HwAllInOneController.this.processSimStateChanged(simState, slotId);
                    }
                } else {
                    HwAllInOneController.this.logd("Not process for HISI");
                }
            } else if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction())) {
                HwAllInOneController.this.logd("received ACTION_SET_RADIO_CAPABILITY_DONE");
                if (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT) {
                    HwAllInOneController.this.logd("reset mNvRestartRildDone");
                    HwAllInOneController.this.mNvRestartRildDone = false;
                    HwAllInOneController.this.sendResponseToTarget(HwAllInOneController.this.obtainMessage(HwAllInOneController.EVENT_FAST_SWITCH_SIM_SLOT_DONE, Integer.valueOf(intent.getIntExtra("intContent", 0))), 0);
                } else {
                    HwAllInOneController.this.sendSetRadioCapabilitySuccess(true);
                }
            } else if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED".equals(intent.getAction())) {
                HwAllInOneController.this.logd("received ACTION_SET_RADIO_CAPABILITY_FAILED");
                Message response;
                if (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT) {
                    response = HwAllInOneController.this.obtainMessage(HwAllInOneController.EVENT_FAST_SWITCH_SIM_SLOT_DONE, Integer.valueOf(HwAllInOneController.this.expectedDDSsubId));
                    HwAllInOneController.this.mAutoSwitchDualCardsSlotDone = false;
                    HwAllInOneController.this.sendResponseToTarget(response, 2);
                } else {
                    response = HwAllInOneController.this.obtainMessage(10, Integer.valueOf(HwAllInOneController.this.expectedDDSsubId));
                    AsyncResult.forMessage(response, null, new Exception());
                    response.sendToTarget();
                }
                HwAllInOneController.this.sendHwSwitchSlotFailedBroadcast();
            } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                slotId = -1;
                int subState = -1;
                int result = -1;
                try {
                    slotId = ((Integer) intent.getExtra("subscription", Integer.valueOf(-1))).intValue();
                    subState = ((Integer) intent.getExtra("newSubState", Integer.valueOf(-1))).intValue();
                    result = ((Integer) intent.getExtra("operationResult", Integer.valueOf(-1))).intValue();
                } catch (Exception ex) {
                    HwAllInOneController.this.logd("Get Intent Extra exception ex:" + ex.getMessage());
                }
                HwAllInOneController.this.logd("received ACTION_SUBSCRIPTION_SET_UICC_RESULT,slotId:" + slotId + " subState:" + subState + " result:" + result);
                if (slotId >= 0 && HwAllInOneController.SIM_NUM > slotId) {
                    if (1 == result) {
                        if (1 == subState) {
                            HwAllInOneController.this.mSetUiccSubscriptionResult[slotId] = subState;
                        } else {
                            HwAllInOneController.this.mSetUiccSubscriptionResult[slotId] = -1;
                        }
                    } else if (result == 0) {
                        HwAllInOneController.this.mSetUiccSubscriptionResult[slotId] = -1;
                    }
                }
            } else if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                HwAllInOneController.this.logd("com.huawei.devicepolicy.action.POLICY_CHANGED");
                String action_tag = intent.getStringExtra("action_tag");
                if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_data_4G")) {
                    int targetId = intent.getIntExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, -1);
                    HwAllInOneController.this.isSet4GSlotInProgress = false;
                    boolean dataState = intent.getBooleanExtra("dataState", false);
                    boolean isSub0Active = SubscriptionController.getInstance().getSubState(0) == 1;
                    if (HwAllInOneController.this.isCardPresent(0) && HwAllInOneController.this.isCardPresent(1) && dataState && isSub0Active) {
                        HwAllInOneController.this.setDefault4GSlot(targetId, null);
                    }
                }
            } else if ("android.intent.action.PRE_BOOT_COMPLETED".equals(intent.getAction())) {
                HwAllInOneController.this.logd("received ACTION_PRE_BOOT_COMPLETED");
                HwAllInOneController.this.isPreBootCompleted = true;
                Message msg = HwAllInOneController.this.obtainMessage(HwAllInOneController.EVENT_SWITCH_DUAL_CARD_IF_NEEDED);
                AsyncResult.forMessage(msg, null, null);
                HwAllInOneController.this.sendMessage(msg);
            } else if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                HwAllInOneController.this.logd("received ACTION_SERVICE_STATE_CHANGED");
                HwAllInOneController.this.onServiceStateChangedForCMCC(intent);
            }
        }
    };
    private Handler mRetryHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwAllInOneController.this.logd("MSG_RETRY_SET_DEFAULT_LTESLOT");
                    HwAllInOneController.this.setPrefNetworkTypeAndStartTimer(msg.arg1);
                    return;
                case 2:
                    HwAllInOneController.this.logd("MSG_RETRY_CHANGE_PREF_NETWORK");
                    HwAllInOneController.this.handleGetPreferredNetwork();
                    return;
                default:
                    HwAllInOneController.this.logd("Unknown msg:" + msg.what);
                    return;
            }
        }
    };
    private int mSecondaryStackNetworkType = -1;
    private int mSecondaryStackPhoneId = -1;
    private Message mSet4GSlotCompleteMsg = null;
    private int mSetPrimaryStackPrefMode = -1;
    private Message mSetSdcsCompleteMsg = null;
    private int mSetSecondaryStackPrefMode = -1;
    private int[] mSetUiccSubscriptionResult = new int[SIM_NUM];
    private BroadcastReceiver mSingleCardReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwAllInOneController.this.loge("SingleCardReceiver, intent is null, return");
                return;
            }
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                HwAllInOneController.this.processSingleSimStateChanged((String) intent.getExtra("ss"), intent.getIntExtra("slot", -1000));
            }
        }
    };
    private int[] mSwitchTypes = new int[SIM_NUM];
    private UiccController mUiccController = null;
    private int mUserPref4GSlot = 0;
    private boolean needRetrySetPrefNetwork = false;
    private int needSetDataAllowCount = 0;
    private int[] nwModeArray = new int[SIM_NUM];
    private int prefer4GSlot = 0;
    private int retryChangeCount = 0;
    private int retryCount = 0;
    private SubCarrierType[] subCarrierTypeArray = new SubCarrierType[SIM_NUM];
    private boolean updateUserDefaultFlag = false;

    private static class DelayedEvent {
        int id;
        int[] networkModeArray;
        int slotId;

        DelayedEvent(int id, int slotId, int[] networkModeArray) {
            this.id = id;
            this.slotId = slotId;
            this.networkModeArray = networkModeArray;
        }
    }

    private enum SubCarrierType {
        CARRIER_CMCC_USIM,
        CARRIER_CMCC_SIM,
        CARRIER_CU_USIM,
        CARRIER_CU_SIM,
        CARRIER_CT_CSIM,
        CARRIER_CT_RUIM,
        CARRIER_FOREIGN_USIM,
        CARRIER_FOREIGN_SIM,
        CARRIER_FOREIGN_CSIM,
        CARRIER_FOREIGN_RUIM,
        OTHER;

        boolean isUCard() {
            if (this == CARRIER_CMCC_USIM || this == CARRIER_CMCC_SIM || this == CARRIER_CU_USIM || this == CARRIER_CU_SIM || this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_SIM) {
                return true;
            }
            return false;
        }

        boolean isCCard() {
            if (this == CARRIER_CT_CSIM || this == CARRIER_CT_RUIM || this == CARRIER_FOREIGN_CSIM || this == CARRIER_FOREIGN_RUIM) {
                return true;
            }
            return false;
        }

        boolean is3G4GCard() {
            if (this == CARRIER_CMCC_USIM || this == CARRIER_CU_USIM || this == CARRIER_CT_CSIM || this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_CSIM) {
                return true;
            }
            return false;
        }

        boolean is2GCard() {
            if (this == CARRIER_CMCC_SIM || this == CARRIER_CU_SIM || this == CARRIER_CT_RUIM || this == CARRIER_FOREIGN_SIM || this == CARRIER_FOREIGN_RUIM) {
                return true;
            }
            return false;
        }

        boolean isCMCCCard() {
            if (this == CARRIER_CMCC_SIM || this == CARRIER_CMCC_USIM) {
                return true;
            }
            return false;
        }

        boolean isCUCard() {
            if (this == CARRIER_CU_USIM || this == CARRIER_CU_SIM) {
                return true;
            }
            return false;
        }

        boolean isCTCard() {
            if (this == CARRIER_CT_CSIM || this == CARRIER_CT_RUIM) {
                return true;
            }
            return false;
        }

        boolean isReCheckFail() {
            if (this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_SIM || this == OTHER) {
                return true;
            }
            return false;
        }
    }

    public enum SubType {
        CARRIER_PREFERRED,
        CARRIER,
        FOREIGN_CARRIER_PREFERRED,
        FOREIGN_CARRIER,
        LOCAL_CARRIER,
        ERROR
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues() {
        if (-com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues != null) {
            return -com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues;
        }
        int[] iArr = new int[SubCarrierType.values().length];
        try {
            iArr[SubCarrierType.CARRIER_CMCC_SIM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CMCC_USIM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CT_CSIM.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CT_RUIM.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CU_SIM.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CU_USIM.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SubCarrierType.CARRIER_FOREIGN_CSIM.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SubCarrierType.CARRIER_FOREIGN_RUIM.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SubCarrierType.CARRIER_FOREIGN_SIM.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SubCarrierType.CARRIER_FOREIGN_USIM.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SubCarrierType.OTHER.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        -com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals(PROPERTY_HW_OPTA_TELECOM)) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals(PROPERTY_HW_OPTB_CHINA);
        } else {
            equals = false;
        }
        IS_CHINA_TELECOM = equals;
        if (IS_CMCC_4G_DSDX_ENABLE) {
            equals = "cmcc".equalsIgnoreCase(SystemProperties.get("ro.hwpp.dualsim_swap_solution", ""));
        } else {
            equals = false;
        }
        IS_CMCC_4GSWITCH_DISABLE = equals;
        if (IS_FULL_NETWORK_SUPPORTED && (HwModemCapability.isCapabilitySupport(9) ^ 1) != 0 && SystemProperties.getBoolean("persist.hisi.fullnetwork", true)) {
            equals = "normal".equals(SystemProperties.get("ro.runmode", "normal"));
        } else {
            equals = false;
        }
        IS_FULL_NETWORK_SUPPORTED_IN_HISI = equals;
        if (HwModemCapability.isCapabilitySupport(9) || !"normal".equals(SystemProperties.get("ro.runmode", "normal"))) {
            equals = false;
        } else {
            equals = IS_CHINA_TELECOM ^ 1;
        }
        IS_HISI_DSDX = equals;
        if (HwModemCapability.isCapabilitySupport(9) || !"normal".equals(SystemProperties.get("ro.runmode", "normal"))) {
            equals = false;
        } else {
            equals = SystemProperties.getBoolean("ro.config.hw_switchdata_4G", false);
        }
        IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT = equals;
    }

    public static HwAllInOneController make(Context context, CommandsInterface[] ci) {
        HwAllInOneController hwAllInOneController;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("HwAllInOneController.make() should only be called once");
            }
            mInstance = new HwAllInOneController(context, ci);
            if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
                mHotPlugController = HwHotplugController.make(context, ci);
            }
            if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
                HwForeignUsimForTelecom.make(context, ci);
            }
            hwAllInOneController = mInstance;
        }
        return hwAllInOneController;
    }

    public static HwAllInOneController getInstance() {
        HwAllInOneController hwAllInOneController;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("HwAllInOneController.getInstance can't be called before make()");
            }
            hwAllInOneController = mInstance;
        }
        return hwAllInOneController;
    }

    public void setDefault4GSlotForCMCC() {
        logd("setDefault4GSlotForCMCC");
        if (IS_CT_4GSWITCH_DISABLE) {
            logd("setDefault4GSlotForCMCC for CT");
            if (judgeDefault4GSlotForCT(true) && this.default4GSlot != getUserSwitchDualCardSlots() && (this.isSet4GSlotInProgress ^ 1) != 0) {
                logd("setDefault4GSlotForCMCC for CT: need setDefault4GSlot");
                setDefault4GSlot(this.default4GSlot);
            }
        }
    }

    private Message obtainSetPreNWMessage(int slotId) {
        return obtainMessage(10, Integer.valueOf(slotId));
    }

    private HwAllInOneController(Context c, CommandsInterface[] ci) {
        int i;
        if (IS_CMCC_4G_DSDX_ENABLE) {
            this.TAG += "ForCMCC";
        } else if (IS_CMCC_CU_DSDX_ENABLE) {
            this.TAG += "ForCMCC_CU";
        }
        logd("HwAllInOneController constructor");
        this.mContext = c;
        this.mCis = ci;
        initDefaultDBIfNeeded();
        this.mEventsQ.clear();
        for (i = 0; i < SIM_NUM; i++) {
            this.isSimInsertedArray[i] = false;
            this.subCarrierTypeArray[i] = SubCarrierType.OTHER;
            this.mGetUiccCardsStatusDone[i] = false;
            this.mGetBalongSimSlotDone[i] = false;
            this.mSwitchTypes[i] = -1;
            this.mCardTypes[i] = -1;
            this.mRadioOns[i] = false;
            this.mIccIds[i] = null;
            this.mSetUiccSubscriptionResult[i] = -1;
        }
        for (i = 0; i < this.mCis.length; i++) {
            Integer index = Integer.valueOf(i);
            this.mCis[i].registerForIccStatusChanged(this, 1, index);
            this.mCis[i].registerForAvailable(this, 1, index);
            this.mCis[i].registerForNotAvailable(this, 3, index);
            this.mCis[i].registerForAvailable(this, EVENT_RADIO_AVAILABLE, index);
            if (IS_HISI_DSDX && (!IS_FULL_NETWORK_SUPPORTED_IN_HISI || IS_FAST_SWITCH_SIMSLOT)) {
                this.mCis[i].registerForSimHotPlug(this, 16, index);
            }
        }
        this.mUiccController = UiccController.getInstance();
        if (!HwModemCapability.isCapabilitySupport(9)) {
            this.isSet4GSlotInProgress = true;
        } else if (!isNeedHwAllInOneController()) {
            logd("there is no need to enter HwAllInOneController");
            return;
        } else if (1 == SIM_NUM) {
            if (IS_FULL_NETWORK_SUPPORTED) {
                this.mContext.registerReceiver(this.mSingleCardReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
                logd("qcom single card and full network, enter HwAllInOneController");
                return;
            }
            logd("qcom single card not enter HwAllInOneController");
            return;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        if (IS_CMCC_4GSWITCH_DISABLE || IS_CT_4GSWITCH_DISABLE) {
            filter.addAction("android.intent.action.PRE_BOOT_COMPLETED");
        }
        if (IS_QCRIL_CROSS_MAPPING || IS_FAST_SWITCH_SIMSLOT) {
            filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
            filter.addAction("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        }
        filter.addAction("android.intent.action.SERVICE_STATE");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public static boolean isNeedHwAllInOneController() {
        boolean z;
        if (IS_FULL_NETWORK_SUPPORTED || IS_CMCC_4G_DSDX_ENABLE) {
            z = true;
        } else {
            z = IS_CMCC_CU_DSDX_ENABLE;
        }
        if (z) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x0502  */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x04ec  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        Integer index = getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        AsyncResult ar = msg.obj;
        int i;
        switch (msg.what) {
            case 1:
                logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
                if (IS_CMCC_4G_DSDX_ENABLE || HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT || IS_CT_4GSWITCH_DISABLE) {
                    this.mCis[index.intValue()].getICCID(obtainMessage(EVENT_GET_ICCID_DONE, index));
                }
                this.mCis[index.intValue()].queryCardType(obtainMessage(EVENT_QUERY_CARD_TYPE_DONE, index));
                this.mCis[index.intValue()].getBalongSim(obtainMessage(EVENT_GET_BALONG_SIM_DONE, index));
                this.mCis[index.intValue()].iccGetATR(obtainMessage(EVENT_ICC_GET_ATR_DONE, index));
                if (mHotPlugController != null) {
                    mHotPlugController.onHotPlugIccStatusChanged(index);
                    break;
                }
                break;
            case 3:
                logd("EVENT_RADIO_UNAVAILABLE, on index " + index);
                this.mCis[index.intValue()].iccGetATR(obtainMessage(EVENT_ICC_GET_ATR_DONE, index));
                this.mRadioOns[index.intValue()] = false;
                this.mSwitchTypes[index.intValue()] = -1;
                this.mGetUiccCardsStatusDone[index.intValue()] = false;
                this.mGetBalongSimSlotDone[index.intValue()] = false;
                this.mCardTypes[index.intValue()] = -1;
                this.mIccIds[index.intValue()] = null;
                this.mNvRestartRildDone = true;
                break;
            case 10:
                if (hasMessages(14)) {
                    removeMessages(14);
                }
                if (ar == null || ar.exception != null) {
                    refreshCardState();
                    if (!this.isSimInsertedArray[this.default4GSlot]) {
                        this.retryCount = 20;
                        logd("current app destoryed, this error don't need retry send.set retryCount to max value");
                    }
                    loge("handleMessage: EVENT_SET_PREF_NETWORK_DONE failed for slot: " + index + "with retryCount = " + this.retryCount);
                    if (this.retryCount < 20) {
                        this.retryCount++;
                        this.mRetryHandler.sendMessageDelayed(this.mRetryHandler.obtainMessage(1, index.intValue(), 0), 500);
                        return;
                    } else if (this.mCis[index.intValue()] == null || RadioState.RADIO_UNAVAILABLE != this.mCis[index.intValue()].getRadioState()) {
                        if (IS_QCRIL_CROSS_MAPPING) {
                            loge("EVENT_SET_PREF_NETWORK_DONE failed ,response GENERIC_FAILURE");
                            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
                            this.retryCount = 0;
                        } else {
                            restorePreferredNwModeAndLteSlot();
                            logd("restore prefNWMode for LteSlot !");
                            if (this.nwModeArray[this.current4GSlotBackup] != this.mPrefNwMode[this.current4GSlotBackup]) {
                                this.mCis[this.current4GSlotBackup].setPreferredNetworkType(this.mPrefNwMode[this.current4GSlotBackup], null);
                            }
                            if (IS_CMCC_4G_DSDX_ENABLE) {
                                judgePreNwModeSubIdAndListForCMCC(this.current4GSlotBackup);
                            } else if (IS_CMCC_CU_DSDX_ENABLE) {
                                logd("resume the 4G slot in CMCC_CU_DSDX");
                                judgePreNwModeSubIdAndListForCU(this.current4GSlotBackup);
                            }
                            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
                            this.retryCount = 0;
                        }
                    } else if (this.mEventsQ.size() == 0) {
                        this.mCis[index.intValue()].registerForOn(this, 12, index);
                        logd("having retried 20 times, then add the event to queue waiting for radio on to process it!");
                        this.mEventsQ.addLast(new DelayedEvent(2, index.intValue(), this.nwModeArray));
                        return;
                    }
                }
                logd("handleMessage: EVENT_SET_PREF_NETWORK_DONE success for slot: " + index);
                HwFrameworkFactory.getHwInnerTelephonyManager().updateCrurrentPhone(index.intValue());
                sendResponseToTarget(this.mSet4GSlotCompleteMsg, 0);
                if (this.mSet4GSlotCompleteMsg == null && !(IS_QCRIL_CROSS_MAPPING && (checkIfDDSEventExist() ^ 1) == 0)) {
                    logd("slience set network mode done, prepare to set DDS for slot " + index);
                    HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(index.intValue());
                    if (IS_QCRIL_CROSS_MAPPING && PhoneFactory.onDataSubChange(0, null) == 0) {
                        for (i = 0; i < SIM_NUM; i++) {
                            PhoneFactory.resendDataAllowed(i);
                            logd("EVENT_SET_PREF_NETWORK_DONE resend data allow with slot " + i);
                        }
                    }
                }
                if (this.updateUserDefaultFlag) {
                    Global.putInt(this.mContext.getContentResolver(), USER_DEFAULT_SUBSCRIPTION, index.intValue());
                }
                this.retryCount = 0;
                this.mSet4GSlotCompleteMsg = null;
                this.isSet4GSlotInProgress = false;
                this.updateUserDefaultFlag = false;
                logd("check if there are still events unprocessed in queque with mEventsQ.size() = " + this.mEventsQ.size());
                if (this.mEventsQ.size() != 0) {
                    if (IS_QCRIL_CROSS_MAPPING) {
                        this.mNeedSetAllowData = true;
                    }
                    handleDelayedEvent();
                    break;
                }
                break;
            case 11:
                int subId = msg.arg1;
                this.mNumOfGetPrefNwModeSuccess++;
                if (ar.exception == null) {
                    int modemNetworkMode = ((int[]) ar.result)[0];
                    logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
                    this.mModemPreferMode[subId] = modemNetworkMode;
                } else {
                    logd("Failed to get preferred network mode for slot" + subId);
                    this.mModemPreferMode[subId] = -1;
                }
                if (this.mNumOfGetPrefNwModeSuccess == SIM_NUM) {
                    if (IS_QCRIL_CROSS_MAPPING) {
                        handleGetPreferredNetworkForMapping();
                    } else {
                        handleGetPreferredNetwork();
                    }
                    this.mNumOfGetPrefNwModeSuccess = 0;
                    break;
                }
                break;
            case 12:
                logd("EVENT_RADIO_ON   on index " + index);
                if (HwModemCapability.isCapabilitySupport(9)) {
                    this.mCis[index.intValue()].unregisterForOn(this);
                    if (this.mEventsQ.size() != 0) {
                        logd("process the events in queue because of radio unavailable with mEventsQ.size() = " + this.mEventsQ.size());
                        handleDelayedEvent();
                        break;
                    }
                }
                break;
            case 13:
                int changeSlotId = msg.arg1;
                if (ar != null && ar.exception == null) {
                    this.mModemPreferMode[changeSlotId] = msg.arg2;
                    logd("handleMessage: EVENT_CHANGE_PREF_NETWORK_DONE successful for slot: " + changeSlotId);
                    break;
                }
                loge("handleMessage: EVENT_CHANGE_PREF_NETWORK_DONE failed for slot: " + changeSlotId + "with retryCount = " + this.retryChangeCount);
                if (this.retryChangeCount < 20) {
                    if (this.mRetryHandler.hasMessages(2)) {
                        this.mRetryHandler.removeMessages(2);
                    } else {
                        this.retryChangeCount++;
                    }
                    this.mRetryHandler.sendMessageDelayed(this.mRetryHandler.obtainMessage(2, changeSlotId, 0), 3000);
                    break;
                }
                break;
            case 14:
                logd("EVENT_SET_PREF_NETWORK_TIMEOUT");
                if (!IS_QCRIL_CROSS_MAPPING) {
                    if (this.isSet4GSlotInProgress) {
                        logd("set retryCount to Max Time and restore ModemBinding and ModemStack to prev State!");
                        this.retryCount = 20;
                        HwModemBindingPolicyHandler.getInstance().restoreToPrevState();
                        break;
                    }
                }
                logd("setRadioCapability timeout");
                sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
                this.mSet4GSlotCompleteMsg = null;
                this.isSet4GSlotInProgress = false;
                this.updateUserDefaultFlag = false;
                this.retryCount = 20;
                break;
                break;
            case 15:
                logd("Received EVENT_SET_DATA_ALLOW_DONE curSetDataAllowCount = " + this.curSetDataAllowCount);
                this.curSetDataAllowCount++;
                if (this.needSetDataAllowCount == this.curSetDataAllowCount) {
                    this.needSetDataAllowCount = 0;
                    this.curSetDataAllowCount = 0;
                    logd("all EVENT_SET_DATA_ALLOW_DONE message got, start switch 4G slot");
                    setWaitingSwitchBalongSlot(true);
                    setDefault4GSlot(this.default4GSlot);
                    break;
                }
                break;
            case 16:
                onSimHotPlug(ar, index);
                break;
            case 17:
                logd("EVENT_CMCC_SET_NETWOR_DONE reveived for slot: " + msg.arg1);
                handleSetCmccPrefNetwork(msg);
                break;
            case EVENT_SWITCH_DUAL_CARD_SLOT /*101*/:
                logd("Received EVENT_SWITCH_DUAL_CARD_SLOT on index " + index);
                if (isntFirstPowerup()) {
                    setWaitingSwitchBalongSlot(false);
                    break;
                }
                break;
            case EVENT_QUERY_CARD_TYPE_DONE /*102*/:
                logd("Received EVENT_QUERY_CARD_TYPE_DONE on index " + index);
                if (ar != null && ar.exception == null) {
                    if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                        HwFullNetwork.getInstance().onQueryCardTypeDone(ar, index);
                    }
                    if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
                        HwForeignUsimForTelecom.getInstance().onQueryCardTypeDone(ar, index);
                    }
                    onQueryCardTypeDone(ar, index);
                    if (mHotPlugController != null) {
                        mHotPlugController.onHotPlugQueryCardTypeDone(ar, index);
                        break;
                    }
                }
                logd("Received EVENT_QUERY_CARD_TYPE_DONE got exception, ar  = " + ar);
                break;
                break;
            case EVENT_GET_BALONG_SIM_DONE /*103*/:
                logd("Received EVENT_GET_BALONG_SIM_DONE on index " + index);
                if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                    HwFullNetwork.getInstance().onGetBalongSimDone(ar, index);
                }
                if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
                    HwForeignUsimForTelecom.getInstance().onGetBalongSimDone(ar, index);
                }
                if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
                    HwDsdsController.getInstance().onGetBalongSimDone(ar, index);
                }
                onGetBalongSimDone(ar, index);
                break;
            case EVENT_RADIO_AVAILABLE /*104*/:
                int i2;
                logd("Received EVENT_RADIO_AVAILABLE on index" + index);
                if (IS_QCRIL_CROSS_MAPPING && SIM_NUM == 2) {
                    syncNetworkTypeFromDB();
                }
                if (!(SIM_NUM != 2 || (this.isVoiceCallEndedRegistered ^ 1) == 0 || (IS_CHINA_TELECOM ^ 1) == 0)) {
                    for (Phone phone : PhoneFactory.getPhones()) {
                        if (phone != null) {
                            logd("registerForVoiceCallEnded for phone " + phone.getPhoneId());
                            phone.getCallTracker().registerForVoiceCallEnded(this, EVENT_VOICE_CALL_ENDED, Integer.valueOf(phone.getPhoneId()));
                        }
                    }
                    this.isVoiceCallEndedRegistered = true;
                }
                if (!HwModemCapability.isCapabilitySupport(9)) {
                    boolean ready = true;
                    if (IS_SUPPORT_CDMA && IS_FAST_SWITCH_SIMSLOT && index.intValue() == getUserSwitchDualCardSlots()) {
                        this.mCis[index.intValue()].getCdmaModeSide(obtainMessage(EVENT_GET_CDMA_MODE_SIDE_DONE, index));
                    }
                    this.mRadioOns[index.intValue()] = true;
                    for (i = 0; i < SIM_NUM; i++) {
                        if (!this.mRadioOns[i]) {
                            ready = false;
                            logd("mRadioOns is " + this.mRadioOns[i]);
                            if (!ready) {
                                if (IS_HISI_DSDX) {
                                    if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload()) {
                                        i2 = 1;
                                    } else {
                                        i2 = HwVSimUtils.isSubActivationUpdate();
                                    }
                                    if ((i2 ^ 1) != 0) {
                                        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
                                        int curr4GSlot = getUserSwitchDualCardSlots();
                                        if (dataSub != curr4GSlot && this.mSet4GSlotCompleteMsg == null) {
                                            SubscriptionController.getInstance().setDefaultDataSubId(curr4GSlot);
                                            logd("EVENT_RADIO_AVAILABLE set default data sub to 4G slot");
                                        }
                                        if ((this.mNvRestartRildDone || this.mSetSdcsCompleteMsg != null) && PhoneFactory.onDataSubChange(0, null) == 0) {
                                            for (i = 0; i < SIM_NUM; i++) {
                                                PhoneFactory.resendDataAllowed(i);
                                                logd("EVENT_RADIO_AVAILABLE resend data allow with slot " + i);
                                            }
                                        }
                                    }
                                }
                                logd("EVENT_RADIO_AVAILABLE set isSet4GSlotInProgress to false");
                                setWaitingSwitchBalongSlot(false);
                                setDsdsCfgDone(false);
                                this.mNvRestartRildDone = false;
                                if (!(IS_FAST_SWITCH_SIMSLOT || this.mSet4GSlotCompleteMsg == null)) {
                                    AsyncResult.forMessage(this.mSet4GSlotCompleteMsg, Boolean.valueOf(true), null);
                                    logd("Sending the mSet4GSlotCompleteMsg back!");
                                    sendResponseToTarget(this.mSet4GSlotCompleteMsg, 0);
                                    this.mSet4GSlotCompleteMsg = null;
                                }
                                if (this.mSetSdcsCompleteMsg != null) {
                                    AsyncResult.forMessage(this.mSetSdcsCompleteMsg, Boolean.valueOf(true), null);
                                    logd("Sending the mSetSdcsCompleteMsg back!");
                                    this.mSetSdcsCompleteMsg.sendToTarget();
                                    this.mSetSdcsCompleteMsg = null;
                                    break;
                                }
                            }
                            logd("clean iccids!!");
                            PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                            break;
                        }
                    }
                    if (!ready) {
                    }
                }
                break;
            case EVENT_SWITCH_SIM_SLOT_CFG_DONE /*105*/:
                logd("Received EVENT_SWITCH_SIM_SLOT_CFG_DONE on index " + index);
                if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
                    HwDsdsController.getInstance().setNeedWatingSlotSwitchDone(false);
                }
                ar = msg.obj;
                if (ar != null && ar.exception == null) {
                    logd(" EVENT_SWITCH_SIM_SLOT_CFG_DONE   need to restart rild now ");
                    if (!IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                        if (!HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
                            try {
                                this.mCis[0].restartRild(null);
                                if (HwDsdsController.IS_DSDSPOWER_SUPPORT && !HwModemCapability.isCapabilitySupport(16)) {
                                    this.mCis[1].restartRild(null);
                                }
                                switchSlotSuccess(index.intValue());
                            } catch (RuntimeException e) {
                            }
                            logd(" switchDualCardsSlot   ------>>> end");
                            break;
                        }
                        HwForeignUsimForTelecom.getInstance().waitToRestartRild();
                        switchSlotSuccess(index.intValue());
                        break;
                    }
                    HwFullNetwork.getInstance().waitToRestartRild();
                    switchSlotSuccess(index.intValue());
                    break;
                }
                switchSlotFailed();
                if (this.mSetSdcsCompleteMsg != null) {
                    AsyncResult.forMessage(this.mSetSdcsCompleteMsg, Boolean.valueOf(false), null);
                    loge("Switch Dual Cardse Slots failed!! Sending the cnf back!");
                    this.mSetSdcsCompleteMsg.sendToTarget();
                    this.mSetSdcsCompleteMsg = null;
                }
                if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                    HwFullNetwork.getInstance().decPollingCount();
                }
                if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
                    HwForeignUsimForTelecom.getInstance().decPollingCount();
                }
                if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
                    HwHotplugController.getInstance().processNotifyPromptHotPlug(false);
                    break;
                }
                break;
            case EVENT_ICC_GET_ATR_DONE /*106*/:
                logd("Received EVENT_ICC_GET_ATR_DONE on index " + index);
                AsyncResult ar_atr = msg.obj;
                if (ar_atr != null && ar_atr.exception == null) {
                    handleIccATR((String) ar_atr.result, index);
                    break;
                }
            case EVENT_GET_ICCID_DONE /*107*/:
                logd("Received EVENT_GET_ICCID_DONE on index " + index);
                if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
                    HwForeignUsimForTelecom.getInstance().onGetIccidDone(ar, index);
                }
                ar = msg.obj;
                if (ar != null && ar.exception == null) {
                    byte[] data = ar.result;
                    String iccid = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                    if (TextUtils.isEmpty(iccid) || 7 > iccid.length()) {
                        logd("iccId is invalid, set it as \"\" ");
                        this.mIccIds[index.intValue()] = "";
                    } else {
                        this.mIccIds[index.intValue()] = iccid.substring(0, 7);
                    }
                    this.mFullIccIds[index.intValue()] = iccid;
                    logd("get iccid is " + SubscriptionInfo.givePrintableIccid(this.mIccIds[index.intValue()]) + " on index " + index);
                    checkIfAllCardsReady();
                    break;
                }
                logd("get iccid exception, maybe card is absent. set iccid as \"\"");
                this.mIccIds[index.intValue()] = "";
                this.mFullIccIds[index.intValue()] = "";
                checkIfAllCardsReady();
                break;
                break;
            case EVENT_CHECK_ALL_CARDS_READY /*108*/:
                logd("Received EVENT_CHECK_ALL_CARDS_READY on index " + index);
                checkIfAllCardsReady();
                if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
                    HwHotplugController.getInstance().processNotifyPromptHotPlug(false);
                    break;
                }
                break;
            case EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE /*109*/:
                logd("Received EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE");
                handleSetPrimaryStackLteSwitchDone(msg);
                break;
            case EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE /*110*/:
                logd("Received EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE");
                handleSetSecondaryStackLteSwitchDone(msg);
                break;
            case EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE /*111*/:
                logd("Received EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE");
                handleRollbackDone(msg);
                break;
            case EVENT_GET_PREF_NETWORK_MODE_DONE /*112*/:
                logd("Received EVENT_GET_PREF_NETWORK_MODE_DONE");
                handleGetPrefNetworkModeDone(msg);
                break;
            case EVENT_VOICE_CALL_ENDED /*113*/:
                logd("Received EVENT_VOICE_CALL_ENDED on index " + index);
                if (!(SubscriptionController.getInstance().getSubState(index.intValue() == 0 ? 1 : 0) == 1 || SubscriptionController.getInstance().getSubState(index.intValue()) != 1 || index.intValue() == HwTelephonyManagerInner.getDefault().getDefault4GSlotId())) {
                    setDefault4GSlot(index.intValue());
                }
                if (index.intValue() == 1 && SubscriptionController.getInstance().getSubState(index.intValue()) == 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
                    HwSubscriptionManager.getInstance().setSubscription(index.intValue(), false, null);
                    break;
                }
            case EVENT_FAST_SWITCH_SIM_SLOT_DONE /*114*/:
                logd("Received EVENT_FAST_SWITCH_SIM_SLOT_DONE on index " + index);
                if (hasMessages(EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT)) {
                    removeMessages(EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT);
                }
                if (IS_SUPPORT_CDMA) {
                    this.mCis[index.intValue()].getCdmaModeSide(obtainMessage(EVENT_GET_CDMA_MODE_SIDE_DONE, index));
                }
                if (ar == null || ar.exception != null) {
                    loge("EVENT_FAST_SWITCH_SIM_SLOT_DONE failed ,response GENERIC_FAILURE");
                    sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
                    this.mSet4GSlotCompleteMsg = null;
                    revertDefaultDataSubId(index.intValue());
                } else {
                    logd("EVENT_FAST_SWITCH_SIM_SLOT_DONE success for slot: " + index);
                    if (IS_CMCC_4G_DSDX_ENABLE || IS_CT_4GSWITCH_DISABLE) {
                        saveIccidsWhenAllCardsReady();
                    }
                    HwFrameworkFactory.getHwInnerTelephonyManager().updateCrurrentPhone(index.intValue());
                    sendHwSwitchSlotDoneBroadcast(index.intValue());
                    if ("0".equals(SystemProperties.get("gsm.nvcfg.rildrestarting", "0"))) {
                        logd("send mSet4GSlotCompleteMsg");
                        sendResponseToTarget(this.mSet4GSlotCompleteMsg, 0);
                        this.mSet4GSlotCompleteMsg = null;
                        setPrefNwForCmcc();
                    } else {
                        logd("waiting for rild restart");
                        this.needRetrySetPrefNetwork = true;
                    }
                    logd("set DDS for slot " + index);
                    HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(index.intValue());
                    if (this.updateUserDefaultFlag) {
                        Global.putInt(this.mContext.getContentResolver(), USER_DEFAULT_SUBSCRIPTION, index.intValue());
                    }
                }
                setWaitingSwitchBalongSlot(false);
                this.updateUserDefaultFlag = false;
                if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data")) {
                    setDefault4GSlotForMDM();
                    break;
                }
                break;
            case EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT /*115*/:
                logd("Received EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT on index " + index);
                sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
                this.mSet4GSlotCompleteMsg = null;
                revertDefaultDataSubId(index.intValue());
                setWaitingSwitchBalongSlot(false);
                this.updateUserDefaultFlag = false;
                if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data")) {
                    setDefault4GSlotForMDM();
                    break;
                }
                break;
            case EVENT_GET_CDMA_MODE_SIDE_DONE /*116*/:
                logd("Received EVENT_GET_CDMA_MODE_SIDE_DONE on index " + index);
                onGetCdmaModeSideDone(ar, index);
                break;
            case EVENT_SWITCH_DUAL_CARD_IF_NEEDED /*117*/:
                logd("Received EVENT_SWITCH_DUAL_CARD_IF_NEEDED");
                this.mAutoSwitchDualCardsSlotDone = false;
                switchDualCardsSlotIfNeeded();
                break;
            case EVENT_RESET_OOS_FLAG /*118*/:
                logd("Received EVENT_RESET_OOS_FLAG on index " + index);
                PhoneFactory.getPhone(index.intValue()).setOOSFlagOnSelectNetworkManually(false);
                break;
            default:
                logd("Unknown msg:" + msg.what);
                break;
        }
    }

    private void setPrefNwForCmcc() {
        logd("setPrefNwForCmcc enter.");
        if (IS_CMCC_4GSWITCH_DISABLE && (IS_VICE_WCDMA ^ 1) == 0) {
            int i;
            for (i = 0; i < SIM_NUM; i++) {
                if (this.mIccIds[i] == null) {
                    logd("setPrefNwForCmcc: mIccIds[" + i + "] is null");
                    return;
                }
            }
            Phone[] phones = PhoneFactory.getPhones();
            i = 0;
            while (i < SIM_NUM) {
                Phone phone = phones[i];
                if (phone == null) {
                    loge("setPrefNwForCmcc: phone " + i + " is null");
                } else {
                    int networkMode;
                    if (getUserSwitchDualCardSlots() == i) {
                        HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();
                        int ability = 1;
                        if (mHwTelephonyManager != null) {
                            ability = mHwTelephonyManager.getLteServiceAbility();
                            logd("setPrefNwForCmcc: LteServiceAbility = " + ability);
                        }
                        networkMode = (IS_FAST_SWITCH_SIMSLOT || !isCDMASimCard(i)) ? (ability == 1 || isCMCCCardBySlotId(i)) ? 9 : 3 : ability == 1 ? 8 : 4;
                    } else if (!isCMCCHybird() || (isCMCCCardBySlotId(i) ^ 1) == 0 || (TelephonyManager.getDefault().isNetworkRoaming(getCMCCCardSlotId()) ^ 1) == 0) {
                        if (isCDMASimCard(i)) {
                            networkMode = 5;
                        } else {
                            networkMode = 3;
                        }
                        if (IS_DUAL_4G_SUPPORTED) {
                            networkMode = isCMCCCardBySlotId(i) ? isDualImsSwitchOpened() ? 9 : 3 : isDualImsSwitchOpened() ? isCDMASimCard(i) ? 8 : 9 : isCDMASimCard(i) ? 4 : 3;
                        }
                    } else {
                        networkMode = isCDMASimCard(i) ? 5 : 1;
                    }
                    phone.setPreferredNetworkType(networkMode, obtainMessage(17, i, networkMode));
                    logd("setPrefNwForCmcc: i = " + i + ", mode = " + networkMode);
                }
                i++;
            }
        }
    }

    private void handleSetCmccPrefNetwork(Message msg) {
        int prefslot = msg.arg1;
        int setPrefMode = msg.arg2;
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            this.needRetrySetPrefNetwork = true;
            loge("setPrefNwForCmcc: Fail, slot " + prefslot + " network mode " + setPrefMode);
            return;
        }
        if (getNetworkTypeFromDB(prefslot) != setPrefMode) {
            setNetworkTypeToDB(prefslot, setPrefMode);
        }
        logd("setPrefNwForCmcc: Success, slot " + prefslot + " network mode " + setPrefMode);
    }

    private void handleGetPreferredNetwork() {
        int i = 0;
        while (i < SIM_NUM) {
            try {
                int prefNwMode = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i);
                logd("subid = " + i + " prefNwMode = " + prefNwMode);
                if (this.mModemPreferMode[i] != prefNwMode) {
                    logd("modemprefermode is not same with prefer mode in slot = " + i);
                    if (this.mCis[i] != null) {
                        this.mCis[i].setPreferredNetworkType(prefNwMode, obtainMessage(13, i, prefNwMode));
                    }
                }
                i++;
            } catch (SettingNotFoundException e) {
                loge("getPreferredNetworkMode: Could not find PREFERRED_NETWORK_MODE!!!");
                refreshCardState();
                judgeSubCarrierType();
                if (judgeDefalt4GSlot()) {
                    judgeNwMode(this.default4GSlot);
                    setDefault4GSlot(this.default4GSlot);
                    return;
                }
                logd("there is no need to set the 4G slot");
                return;
            }
        }
    }

    private void handleGetPreferredNetworkForMapping() {
        int curr4GSlot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        if (dataSub != curr4GSlot) {
            logd("handleGetPreferredNetworkForMapping dataSub = " + dataSub + " ;curr4GSlot = " + curr4GSlot);
            HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(curr4GSlot);
        }
        boolean isDiff = false;
        for (int i = 0; i < SIM_NUM; i++) {
            int prefNwMode = getNetworkTypeFromDB(i);
            logd("subid = " + i + " prefNwMode = " + prefNwMode);
            if (this.mModemPreferMode[i] != prefNwMode) {
                logd("modemprefermode is not same with prefer mode in slot = " + i);
                isDiff = true;
                break;
            }
        }
        if (isDiff) {
            this.mNeedSetLteServiceAbility = true;
            setLteServiceAbility();
            return;
        }
        logd("handleGetPreferredNetworkForMapping PreferMode same");
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < SIM_NUM;
    }

    public void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
            HwForeignUsimForTelecom.getInstance().onGetIccCardStatusDone(ar, index);
        }
        if (ar.exception != null) {
            loge("Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error: " + ar.exception);
        } else if (isValidIndex(index.intValue())) {
            this.mGetUiccCardsStatusDone[index.intValue()] = true;
            if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                HwFullNetwork.getInstance().onGetIccCardStatusDone(ar, index);
            }
            Message msg = obtainMessage(EVENT_CHECK_ALL_CARDS_READY, index);
            AsyncResult.forMessage(msg, null, null);
            sendMessage(msg);
        } else {
            loge("onGetIccCardStatusDone: invalid index : " + index);
        }
    }

    public void disposeCardStatusWhenAllTrayOut() {
        boolean isSingleCardTrayOut = IS_SINGLE_CARD_TRAY ? HwCardTrayUtil.isCardTrayOut(0) : false;
        boolean isBothCardTrayOut = (IS_SINGLE_CARD_TRAY || !HwCardTrayUtil.isCardTrayOut(0)) ? false : HwCardTrayUtil.isCardTrayOut(1);
        if (!(MultiSimVariants.DSDA == TelephonyManager.getDefault().getMultiSimConfiguration())) {
            return;
        }
        if ((isSingleCardTrayOut || isBothCardTrayOut) && IS_HISI_DSDX) {
            logd("DSDX all tray out. disposeCardStatus");
            disposeCardStatus(true);
            setWaitingSwitchBalongSlot(false);
        }
    }

    private boolean checkIfDDSEventExist() {
        int eventsQSize = 0;
        if (this.mEventsQ != null) {
            eventsQSize = this.mEventsQ.size();
        }
        int i = 0;
        while (i < eventsQSize) {
            if (((DelayedEvent) this.mEventsQ.get(i)).id == 1 || ((DelayedEvent) this.mEventsQ.get(i)).id == 2) {
                return true;
            }
            i++;
        }
        return false;
    }

    public void checkIfAllCardsReady() {
        if (!HwModemCapability.isCapabilitySupport(9)) {
            disposeCardStatusWhenAllTrayOut();
            boolean ready = true;
            int i = 0;
            while (i < SIM_NUM) {
                if (this.mSwitchTypes[i] == -1) {
                    logd("mSwitchTypes[" + i + "] == INVALID");
                    ready = false;
                    break;
                } else if (!this.mGetUiccCardsStatusDone[i]) {
                    logd("mGetUiccCardsStatusDone[" + i + "] == false");
                    ready = false;
                    break;
                } else if ((IS_CMCC_4G_DSDX_ENABLE || IS_CT_4GSWITCH_DISABLE) && this.mIccIds[i] == null) {
                    logd("mIccIds[" + i + "] invalid");
                    ready = false;
                    break;
                } else {
                    if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT && (HwVSimUtils.isVSimEnabled() ^ 1) != 0) {
                        if (this.mIccIds[i] == null) {
                            logd("mIccIds[" + i + "] invalid");
                            ready = false;
                            break;
                        } else if (-1 == HwForeignUsimForTelecom.getInstance().getRatCombineMode(i)) {
                            logd("RatCombineMode[" + i + "] invalid");
                            ready = false;
                            break;
                        }
                    }
                    i++;
                }
            }
            int countGetBalongSimSlotDone = 0;
            for (i = 0; i < SIM_NUM; i++) {
                if (this.mGetBalongSimSlotDone[i]) {
                    countGetBalongSimSlotDone++;
                }
            }
            if (countGetBalongSimSlotDone == 0) {
                logd("mGetBalongSimSlotDone all false");
                ready = false;
            }
            if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < SIM_NUM) {
                logd("haven't get all UiccCards done, please wait!");
                ready = false;
            } else {
                UiccCard[] uc = this.mUiccController.getUiccCards();
                for (i = 0; i < uc.length; i++) {
                    if (uc[i] == null) {
                        logd("UiccCard[" + i + "]" + "is null");
                        ready = false;
                        break;
                    }
                }
            }
            if (IS_FULL_NETWORK_SUPPORTED_IN_HISI && (IS_FAST_SWITCH_SIMSLOT ^ 1) != 0) {
                HwFullNetwork.getInstance().checkIfAllCardsReady();
                if (IS_HISI_DSDX && (HwFullNetwork.getInstance().isGetCdmaModeDone() ^ 1) != 0) {
                    logd("HwFullNetwork not getCdmaModeDone");
                    ready = false;
                }
            }
            if (HwVSimUtils.isPlatformTwoModems() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload())) {
                logd("checkIfAllCardsReady()...vsim enabled or card reloading on two modem platform.");
                if (isntFirstPowerup()) {
                    setWaitingSwitchBalongSlot(false);
                }
                return;
            }
            if (IS_FAST_SWITCH_SIMSLOT) {
                try {
                    Phone[] phones = PhoneFactory.getPhones();
                    i = 0;
                    while (i < SIM_NUM) {
                        if (phones[i] == null || (phones[i] != null && phones[i].getRadioCapability() == null)) {
                            ready = false;
                            logd("RadioCapability is null");
                        }
                        i++;
                    }
                } catch (Exception e) {
                    logd("PhoneFactory.getPhones is null");
                    return;
                }
            }
            if (!this.mAllCardsReady && ready) {
                if ("0".equals(SystemProperties.get("gsm.nvcfg.rildrestarting", "0"))) {
                    logd("send mSet4GSlotCompleteMsg to target.");
                    sendResponseToTarget(this.mSet4GSlotCompleteMsg, 0);
                    this.mSet4GSlotCompleteMsg = null;
                    setPrefNetworkIfNeeded();
                } else {
                    logd("gsm.nvcfg.rildrestarting not 0");
                }
            }
            if (this.mAllCardsReady != ready) {
                this.mAllCardsReady = ready;
                logd("mAllCardsReady is " + ready);
            }
            if (this.mAllCardsReady) {
                refreshCardState();
                logd("checkIfAllCardsReady mAutoSwitchDualCardsSlotDone = " + this.mAutoSwitchDualCardsSlotDone + " ; mCommrilRestartRild= " + this.mCommrilRestartRild);
                if (!this.mAutoSwitchDualCardsSlotDone || (isBalongSimSynced() ^ 1) != 0) {
                    logd("switchDualCardsSlotIfNeeded!");
                    switchDualCardsSlotIfNeeded();
                } else if (this.mCommrilRestartRild && (getWaitingSwitchBalongSlot() ^ 1) != 0) {
                    logd("mCommrilRestartRild is true");
                    setCommrilRestartRild(false);
                    this.mAutoSwitchDualCardsSlotDone = true;
                    setWaitingSwitchBalongSlot(true);
                    this.default4GSlot = getUserSwitchDualCardSlots();
                    setDefault4GSlot(this.default4GSlot);
                }
                if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
                    if (IS_FULL_NETWORK_SUPPORTED_IN_HISI && HwFullNetwork.getInstance().getNeedSwitchCommrilMode()) {
                        logd("Need to switch commril, so wait for radio on to SetActiveMode");
                    } else {
                        HwDsdsController.getInstance().custHwdsdsSetActiveModeIfNeeded(this.mUiccController.getUiccCards());
                    }
                }
                restartRildForNvcfg();
            }
        }
    }

    private void setPrefNetworkIfNeeded() {
        if (IS_CMCC_4GSWITCH_DISABLE && IS_VICE_WCDMA && this.needRetrySetPrefNetwork) {
            logd("needRetrySetPrefNetwork");
            setPrefNwForCmcc();
            this.needRetrySetPrefNetwork = false;
        }
    }

    private void restartRildForNvcfg() {
        if (!this.mNvRestartRildDone && this.mAutoSwitchDualCardsSlotDone && this.mSetSdcsCompleteMsg == null && "1".equals(SystemProperties.get("gsm.nvcfg.resetrild", "0"))) {
            if (!(!IS_FULL_NETWORK_SUPPORTED_IN_HISI || (HwFullNetwork.getInstance().getNeedSwitchCommrilMode() ^ 1) == 0 || (IS_FAST_SWITCH_SIMSLOT ^ 1) == 0)) {
                logd("restartRildForNvcfg: restart rild by HwFullNetwork");
                this.mNvRestartRildDone = true;
                HwFullNetwork.getInstance().waitToRestartRild();
            }
            if ((!IS_FULL_NETWORK_SUPPORTED_IN_HISI || IS_FAST_SWITCH_SIMSLOT) && this.needSetDataAllowCount == 0) {
                logd("restartRildForNvcfg: call restartRild");
                this.mNvRestartRildDone = true;
                try {
                    this.mCis[0].restartRild(null);
                    if (HwDsdsController.IS_DSDSPOWER_SUPPORT && !HwModemCapability.isCapabilitySupport(16)) {
                        this.mCis[1].restartRild(null);
                    }
                } catch (RuntimeException e) {
                }
            }
        }
    }

    private void switchDualCardsSlotIfNeeded() {
        boolean isCompleted = SystemProperties.getBoolean("persist.sys.dualcards", false) ? this.mAutoSwitchDualCardsSlotDone ? isBalongSimSynced() : false : true;
        if (isCompleted) {
            logd("mAutoSwitchDualCardsSlotDone has been completed before");
            setWaitingSwitchBalongSlot(false);
        } else if (!this.mAutoSwitchDualCardsSlotDone || (isBalongSimSynced() ^ 1) == 0) {
            this.mAutoSwitchDualCardsSlotDone = true;
            if (judgeDefalt4GSlot()) {
                logd("Need to set the 4G slot");
            } else {
                this.default4GSlot = getUserSwitchDualCardSlots();
                logd("there is no need to set the 4G slot, setdefault slot as " + this.default4GSlot);
            }
            if (IS_HISI_DSDX) {
                int i;
                if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload()) {
                    i = 1;
                } else {
                    i = HwVSimUtils.isSubActivationUpdate();
                }
                if ((i ^ 1) != 0) {
                    if (this.needSetDataAllowCount == 0) {
                        SubscriptionController.getInstance().setDefaultDataSubId(this.default4GSlot);
                        this.needSetDataAllowCount = PhoneFactory.onDataSubChange(15, this);
                        if (this.needSetDataAllowCount > 0) {
                            logd("switchDualCardsSlotIfNeeded return because needSetDataAllowCount = " + this.needSetDataAllowCount);
                            this.curSetDataAllowCount = 0;
                            setWaitingSwitchBalongSlot(true);
                            return;
                        }
                        logd("switchDualCardsSlotIfNeeded no need set_data_allow to any phone");
                    } else {
                        logd("switchDualCardsSlotIfNeeded already in set_data_allow process , needSetDataAllowCount = " + this.needSetDataAllowCount);
                        return;
                    }
                }
            }
            setWaitingSwitchBalongSlot(true);
            setDefault4GSlot(this.default4GSlot);
        } else {
            logd("mAutoSwitchDualCardsSlotDone && !isBalongSimSynced() is true");
        }
    }

    public boolean isSettingDefaultData() {
        return this.needSetDataAllowCount > 0;
    }

    public void setUserSwitchDualCardSlots(int subscription) {
        System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", subscription);
        this.prefer4GSlot = subscription;
        logd("setUserSwitchDualCardSlots: " + subscription);
        if (mHotPlugController != null) {
            UiccCard uc = this.mUiccController.getUiccCard(subscription);
            if (uc != null) {
                mHotPlugController.updateHotPlugMainSlotIccId(uc.getIccId());
            }
        }
    }

    public boolean switchDualCardsSlotCdma() {
        logd("switchDualCardsSlotCdma");
        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT && (HwVSimUtils.isVSimEnabled() ^ 1) != 0) {
            int iForeignSlot = HwForeignUsimForTelecom.getInstance().getForeignCardSlot();
            if (isValidIndex(iForeignSlot)) {
                this.default4GSlot = iForeignSlot;
                logd("switchDualCardsSlotCdma default4GSlot for foreign card :" + this.default4GSlot);
                return true;
            }
        }
        int temSub = getUserSwitchDualCardSlots();
        if (autoSwitchToSlot() == 0) {
            temSub = isBalongSimSynced() ? 0 : 1;
        } else if (autoSwitchToSlot() == 1) {
            temSub = isBalongSimSynced() ? 1 : 0;
        }
        this.default4GSlot = temSub;
        logd("switchDualCardsSlotCdma default4GSlot " + this.default4GSlot);
        return true;
    }

    private int autoSwitchToSlot() {
        int slot = -1;
        if (getUserSwitchDualCardSlots() == 0) {
            if ((this.mSwitchTypes[0] == 1 && this.mSwitchTypes[1] == 3) || ((this.mSwitchTypes[0] == 1 && this.mSwitchTypes[1] == 2) || ((this.mSwitchTypes[0] == 1 && this.mSwitchTypes[1] == 0) || ((this.mSwitchTypes[0] == 0 && this.mSwitchTypes[1] == 3) || (this.mSwitchTypes[0] == 0 && this.mSwitchTypes[1] == 2))))) {
                slot = 1;
            }
        } else if ((this.mSwitchTypes[0] == 0 && this.mSwitchTypes[1] == 1) || ((this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 1) || ((this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 0) || ((this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 1) || (this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 0))))) {
            slot = 0;
        }
        logd("mSwitchTypes[0] = " + this.mSwitchTypes[0] + ", mSwitchTypes[1] = " + this.mSwitchTypes[1] + ", slot = " + slot);
        return slot;
    }

    public void setCommrilRestartRild(boolean bCommrilRestartRild) {
        if (this.mCommrilRestartRild != bCommrilRestartRild) {
            this.mCommrilRestartRild = bCommrilRestartRild;
            logd("setCommrilRestartRild = " + bCommrilRestartRild);
        }
    }

    private void initDefaultDBIfNeeded() {
        try {
            System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            logd("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", 0);
        }
    }

    private void processSubInfoRecordUpdated(int detectedType) {
        if (4 != detectedType) {
            logd("cards in the slots are changed with detectedType: " + detectedType);
            refreshCardState();
            judgeSubCarrierType();
            this.is4GSlotReviewNeeded = 1;
            if (judgeDefalt4GSlot()) {
                judgeNwMode(this.default4GSlot);
                if (this.isSet4GSlotInProgress) {
                    logd("There is event in progress, need to add the new event to queqe!");
                    this.mEventsQ.addLast(new DelayedEvent(1, this.default4GSlot, this.nwModeArray));
                } else {
                    setDefault4GSlot(this.default4GSlot);
                }
            } else {
                logd("there is no need to set the 4G slot");
            }
        } else {
            boolean is4GState = (IS_FULL_NETWORK_SUPPORTED || IS_CMCC_CU_DSDX_ENABLE) ? true : IS_CMCC_4G_DSDX_ENABLE;
            if (is4GState) {
                if (this.isSet4GSlotInProgress) {
                    logd("processSubInfoRecordUpdated: setting lte slot is in progress, ignore this event");
                    return;
                }
                logd("processSubInfoRecordUpdated EXTRA_VALUE_NOCHANGE check!");
                refreshCardState();
                judgeSubCarrierType();
                int userPref4GSlot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
                boolean set4GDefaltSlot = (userPref4GSlot >= SIM_NUM || (this.isSimInsertedArray[userPref4GSlot] ^ 1) == 0) ? userPref4GSlot >= SIM_NUM : true;
                boolean need4GCheckWhenBoot = IS_CT_4GSWITCH_DISABLE && this.subCarrierTypeArray[0].isCTCard() != this.subCarrierTypeArray[1].isCTCard();
                if (set4GDefaltSlot) {
                    judgeDefalt4GSlot();
                    userPref4GSlot = this.default4GSlot;
                } else if (need4GCheckWhenBoot && judgeDefault4GSlotForCMCC()) {
                    userPref4GSlot = this.default4GSlot;
                }
                if (judgeSubCarrierTypeByMccMnc(userPref4GSlot)) {
                    judgeNwMode(userPref4GSlot);
                    if (isSetDefault4GSlotNeeded(userPref4GSlot)) {
                        setDefault4GSlot(userPref4GSlot);
                        return;
                    }
                    this.retryChangeCount = 0;
                    for (int sub = 0; sub < SIM_NUM; sub++) {
                        if (this.mCis[sub] != null) {
                            this.mCis[sub].getPreferredNetworkType(obtainMessage(11, sub, 0));
                        }
                    }
                } else {
                    this.is4GSlotReviewNeeded = 2;
                    this.mUserPref4GSlot = userPref4GSlot;
                    this.subCarrierTypeArray[userPref4GSlot] = SubCarrierType.OTHER;
                    logd("userPref4GSlot=" + userPref4GSlot + " SubCarrierType=" + this.subCarrierTypeArray[userPref4GSlot] + " Need to check in Sim State Change!");
                }
            }
        }
    }

    private void processSubStateChanged(int slotId, int subState) {
        logd("processSubStateChanged: slot Id = " + slotId + " subState = " + subState);
        if (this.isSet4GSlotInProgress) {
            logd("processSubStateChanged: setting lte slot is in progress, ignore this event");
        } else if (SIM_NUM <= slotId) {
            logd("processSubStateChanged: invalid slotId, return");
        } else {
            boolean oldSimCardTypeIsCMCCCard = this.subCarrierTypeArray[slotId].isCMCCCard();
            logd("processSubStateChanged: oldSubCarrierType = " + this.subCarrierTypeArray[slotId]);
            this.isSimInsertedArray[slotId] = subState == 1;
            refreshCardState();
            judgeSubCarrierType();
            this.is4GSlotReviewNeeded = 1;
            if (IS_CMCC_4GSWITCH_DISABLE && (this.subCarrierTypeArray[slotId].isCMCCCard() || (subState != 1 && oldSimCardTypeIsCMCCCard))) {
                this.mNeedSetLteServiceAbility = true;
            }
            if (judgeDefalt4GSlot()) {
                PhoneFactory.getSubInfoRecordUpdater().resetInsertSimState();
                judgeNwMode(this.default4GSlot);
                setDefault4GSlot(this.default4GSlot);
            } else {
                logd("there is no need to set the 4G slot");
            }
        }
    }

    private void processSimStateChanged(String simState, int slotId) {
        if (slotId < 0 || SIM_NUM <= slotId) {
            logd("processSimStateChanged: invalid slotId, return");
        } else if (!"IMSI".equals(simState)) {
        } else {
            if (RadioState.RADIO_ON != this.mCis[slotId].getRadioState()) {
                logd("processSimStateChanged radioState=" + this.mCis[slotId].getRadioState());
                this.mCis[slotId].registerForOn(this, 12, Integer.valueOf(slotId));
                this.mEventsQ.addLast(new DelayedEvent(3, slotId, this.nwModeArray));
                return;
            }
            int i;
            logd("processSimStateChanged: check if it needs to update main card!");
            refreshCardState();
            SubCarrierType[] oldSubCarrierTypeArr = new SubCarrierType[SIM_NUM];
            for (i = 0; i < SIM_NUM; i++) {
                oldSubCarrierTypeArr[i] = this.subCarrierTypeArray[i];
            }
            judgeSubCarrierType();
            judgeSubCarrierTypeByMccMnc(slotId);
            for (i = 0; i < SIM_NUM; i++) {
                if (i != slotId) {
                    this.subCarrierTypeArray[i] = oldSubCarrierTypeArr[i];
                }
            }
            logd("processSimStateChanged: READY!oldSubCarrierType is " + oldSubCarrierTypeArr[slotId] + " for slot" + slotId);
            int sub;
            if (oldSubCarrierTypeArr[slotId] != this.subCarrierTypeArray[slotId] ? this.subCarrierTypeArray[slotId].isReCheckFail() ^ 1 : false) {
                boolean isSetDefault4GNeed = false;
                int new4GSlotId = this.default4GSlot;
                if (1 == this.is4GSlotReviewNeeded) {
                    logd("processSimStateChanged: auto mode!");
                    isSetDefault4GNeed = judgeDefalt4GSlot();
                    new4GSlotId = this.default4GSlot;
                } else if (2 == this.is4GSlotReviewNeeded) {
                    logd("processSimStateChanged: fix mode!");
                    judgeNwMode(this.mUserPref4GSlot);
                    isSetDefault4GNeed = isSetDefault4GSlotNeeded(this.mUserPref4GSlot);
                    new4GSlotId = this.mUserPref4GSlot;
                }
                if (isSetDefault4GNeed) {
                    judgeNwMode(new4GSlotId);
                    if (this.isSet4GSlotInProgress) {
                        logd("There is event in progress, need to add the new event to queqe!");
                        this.mEventsQ.addLast(new DelayedEvent(1, new4GSlotId, this.nwModeArray));
                    } else {
                        setDefault4GSlot(new4GSlotId);
                    }
                } else {
                    logd("processSimStateChanged: there is no need to set the 4G slot");
                    if (2 == this.is4GSlotReviewNeeded) {
                        this.retryChangeCount = 0;
                        for (sub = 0; sub < SIM_NUM; sub++) {
                            if (this.mCis[sub] != null) {
                                this.mCis[sub].getPreferredNetworkType(obtainMessage(11, sub, 0));
                            }
                        }
                    }
                }
            } else {
                logd("processSimStateChanged: there is no need to update main card!");
                if (2 == this.is4GSlotReviewNeeded && (this.isSet4GSlotInProgress ^ 1) != 0) {
                    this.retryChangeCount = 0;
                    for (sub = 0; sub < SIM_NUM; sub++) {
                        if (this.mCis[sub] != null) {
                            this.mCis[sub].getPreferredNetworkType(obtainMessage(11, sub, 0));
                        }
                    }
                }
            }
        }
    }

    private String getIccId(int slot) {
        String iccId = this.mUiccController.getUiccCard(slot) == null ? null : this.mUiccController.getUiccCard(slot).getIccId();
        if (iccId != null) {
            return iccId;
        }
        List<SubscriptionInfo> subInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexWithCheck(slot, false, this.mContext.getOpPackageName());
        if (subInfo != null) {
            iccId = ((SubscriptionInfo) subInfo.get(0)).getIccId();
        }
        return iccId;
    }

    private void refreshCardState() {
        for (int index = 0; index < SIM_NUM; index++) {
            if (HwModemCapability.isCapabilitySupport(9)) {
                boolean isSubActivated = SubscriptionController.getInstance().getSubState(index) == 1;
                boolean[] zArr = this.isSimInsertedArray;
                if (!isCardPresent(index)) {
                    isSubActivated = false;
                }
                zArr[index] = isSubActivated;
            } else {
                this.isSimInsertedArray[index] = isCardPresent(index);
            }
        }
    }

    private boolean judgeSubCarrierTypeByMccMnc(int slotId) {
        logd("judgeSubCarrierTypeByMccMnc: judge subCarrierType for slot: " + slotId);
        if (this.subCarrierTypeArray[slotId].isCCard()) {
            logd("judgeSubCarrierTypeByMccMnc C Card is no need to judge MCCMNC!");
            return true;
        }
        if (this.isSimInsertedArray[slotId]) {
            int appType = getCardAppType(slotId);
            String mccMnc = getMccMnc(slotId, appType);
            if (mccMnc == null) {
                loge("processSimStateChanged: mccMnc is invalid, return!");
                return false;
            }
            logd("judgeSubCarrierTypeByMccMnc: current subCarrierTypeArray is : " + this.subCarrierTypeArray[slotId]);
            if (isCMCCCardByMccMnc(mccMnc)) {
                if (2 == appType) {
                    this.subCarrierTypeArray[slotId] = SubCarrierType.CARRIER_CMCC_USIM;
                } else if (1 == appType) {
                    this.subCarrierTypeArray[slotId] = SubCarrierType.CARRIER_CMCC_SIM;
                }
            } else if (isCUCardByMccMnc(mccMnc)) {
                if (2 == appType) {
                    this.subCarrierTypeArray[slotId] = SubCarrierType.CARRIER_CU_USIM;
                } else if (1 == appType) {
                    this.subCarrierTypeArray[slotId] = SubCarrierType.CARRIER_CU_SIM;
                }
            }
            logd("judgeSubCarrierTypeByMccMnc: after update subCarrierTypeArray is : " + this.subCarrierTypeArray[slotId]);
        }
        return true;
    }

    private String getMccMnc(int slotId, int appType) {
        UiccCardApplication app = null;
        String mccMnc = null;
        if (this.mUiccController == null) {
            return null;
        }
        UiccCard uiccCard = this.mUiccController.getUiccCard(slotId);
        if (uiccCard != null) {
            if (2 == appType || 1 == appType) {
                app = uiccCard.getApplication(1);
            } else if (4 == appType || 3 == appType) {
                app = uiccCard.getApplication(2);
            } else {
                logd("unknown appType, return!");
                return null;
            }
        }
        if (app != null) {
            IccRecords records = app.getIccRecords();
            if (records != null) {
                String imsi = records.getIMSI();
                if (imsi == null || 5 >= imsi.length()) {
                    logd("invalid imsi!");
                } else {
                    mccMnc = imsi.substring(0, 5);
                    logd("mccMnc = " + mccMnc);
                }
            }
        } else {
            logd("app is null, return");
        }
        return mccMnc;
    }

    private void judgeSubCarrierType() {
        logd("judgeSubCarrierType: judge the sub Type for each slot");
        int sub = 0;
        while (sub < SIM_NUM) {
            logd("judgeSubCarrierType: isSimInsertedArray[" + sub + "] = " + this.isSimInsertedArray[sub]);
            if (this.isSimInsertedArray[sub]) {
                String iccId = getIccId(sub);
                if (TextUtils.isEmpty(iccId) || 7 > iccId.length()) {
                    loge("judgeSubCarrierType: iccId is invalid, set the sub carrier type as OTHER");
                    this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                } else {
                    String inn = iccId.substring(0, 7);
                    int appType = getCardAppType(sub);
                    logd("judgeSubCarrierType: iccId is " + inn + " and app type is " + appType + " for sub " + sub);
                    if (isCMCCCard(inn)) {
                        if (2 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CMCC_USIM;
                        } else if (1 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CMCC_SIM;
                        } else if (4 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (3 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                        }
                    } else if (isCUCard(inn)) {
                        if (2 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CU_USIM;
                        } else if (1 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CU_SIM;
                        } else if (4 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (3 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                        }
                    } else if (isCTCard(inn)) {
                        if (4 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (3 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else if (43 == HwFrameworkFactory.getHwInnerTelephonyManager().getCardType(sub)) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (41 == HwFrameworkFactory.getHwInnerTelephonyManager().getCardType(sub) || 30 == HwFrameworkFactory.getHwInnerTelephonyManager().getCardType(sub)) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                        }
                    } else if (2 == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_USIM;
                    } else if (1 == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_SIM;
                    } else if (4 == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_CSIM;
                    } else if (3 == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_RUIM;
                    } else {
                        this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                    }
                }
            } else {
                logd("judgeSubCarrierType: sub " + sub + " is absent");
                this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
            }
            sub++;
        }
    }

    private void judgeNwMode(int lteSlotId) {
        int nwMode4GforCU;
        int nwMode4GforCMCC;
        int nwMode4GforCT;
        int otherNWModeInCMCC;
        boolean is4GAbilityOn = 1 == HwFrameworkFactory.getHwInnerTelephonyManager().getLteServiceAbility();
        logd("judgeNwMode: the LTE slot will be " + lteSlotId + " with the is4GAbilityOn = " + is4GAbilityOn);
        if (is4GAbilityOn) {
            nwMode4GforCU = 9;
            if (DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCMCC = 17;
            } else {
                nwMode4GforCMCC = 20;
            }
            nwMode4GforCT = 10;
            otherNWModeInCMCC = 9;
        } else {
            nwMode4GforCU = 3;
            if (DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCMCC = 16;
            } else {
                nwMode4GforCMCC = 18;
            }
            nwMode4GforCT = 7;
            otherNWModeInCMCC = 3;
        }
        logd("judgeNwMode: subCarrierTypeArray[" + lteSlotId + "] = " + this.subCarrierTypeArray[lteSlotId]);
        if (IS_FULL_NETWORK_SUPPORTED) {
            judgeNwModeForFullNetwork(lteSlotId, nwMode4GforCU, nwMode4GforCMCC, nwMode4GforCT);
        } else if (IS_CMCC_CU_DSDX_ENABLE) {
            judgeNwModeForCMCC_CU(lteSlotId, nwMode4GforCMCC, nwMode4GforCU);
            judgePreNwModeSubIdAndListForCU(lteSlotId);
        } else if (IS_CMCC_4G_DSDX_ENABLE) {
            judgeNwModeForCMCC(lteSlotId, nwMode4GforCMCC, otherNWModeInCMCC);
            judgePreNwModeSubIdAndListForCMCC(lteSlotId);
        } else if (IS_CHINA_TELECOM) {
            judgeNwModeForCT(lteSlotId, nwMode4GforCT, nwMode4GforCU);
        } else {
            logd("judgeNwMode: do nothing.");
        }
    }

    private void judgeNwModeForCT(int lteSlotId, int CT_DefaultMode, int UMTS_DefaultMode) {
        logd("judgeNwModeForCT, lteSlotId = " + lteSlotId);
        this.nwModeArray[lteSlotId] = CT_DefaultMode;
        for (int i = 0; i < SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
        }
        logd("judgeNwModeForCT prefer sub network mode is " + this.nwModeArray[lteSlotId]);
    }

    private void judgeNwModeForCMCC_CU(int lteSlotId, int TD_DefaultMode, int UMTS_DefaultMode) {
        logd("judgeNwModeForCMCC_CU, lteSlotId = " + lteSlotId);
        if (this.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
            this.nwModeArray[lteSlotId] = TD_DefaultMode;
        } else {
            this.nwModeArray[lteSlotId] = UMTS_DefaultMode;
        }
        for (int i = 0; i < SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
        }
        logd("judgeNwModeForCMCC_CU prefer sub network mode is " + this.nwModeArray[lteSlotId]);
    }

    private void putPreNWModeListToDBforCMCC() {
        logd("in putPreNWModeListToDBforCMCC");
        ContentResolver resolver = this.mContext.getContentResolver();
        switch (DEFAULT_NETWORK_MODE) {
            case 17:
                System.putInt(resolver, NETWORK_MODE_4G_PRE, 17);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, 16);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
                return;
            case 20:
                System.putInt(resolver, NETWORK_MODE_4G_PRE, 20);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, 18);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
                return;
            default:
                System.putInt(resolver, NETWORK_MODE_4G_PRE, -1);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, -1);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, -1);
                return;
        }
    }

    private void judgePreNwModeSubIdAndListForCU(int lteSlotId) {
        logd("in judgePreNwModeSubIdAndListForCU ");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.subCarrierTypeArray[lteSlotId].isCCard()) {
            System.putInt(resolver, MAIN_CARD_INDEX, -1);
            System.putInt(resolver, NETWORK_MODE_4G_PRE, -1);
            System.putInt(resolver, NETWORK_MODE_3G_PRE, -1);
            System.putInt(resolver, NETWORK_MODE_2G_ONLY, -1);
        } else {
            System.putInt(resolver, MAIN_CARD_INDEX, lteSlotId);
            if (this.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
                putPreNWModeListToDBforCMCC();
            } else {
                System.putInt(resolver, NETWORK_MODE_4G_PRE, 9);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, 3);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, 1);
            }
        }
        try {
            logd("main card index: " + System.getInt(resolver, MAIN_CARD_INDEX) + " network mode 4G pre: " + System.getInt(resolver, NETWORK_MODE_4G_PRE) + " network mode 3G pre: " + System.getInt(resolver, NETWORK_MODE_3G_PRE) + " network mode 2G only: " + System.getInt(resolver, NETWORK_MODE_2G_ONLY));
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading PreNwMode SubId AndList Values");
        }
    }

    private void judgeNwModeForCMCC(int lteSlotId, int nwMode4GforCMCC, int otherNWModeInCMCC) {
        if (this.subCarrierTypeArray[lteSlotId].isCUCard() || this.subCarrierTypeArray[lteSlotId].isCCard()) {
            this.nwModeArray[lteSlotId] = otherNWModeInCMCC;
        } else {
            this.nwModeArray[lteSlotId] = nwMode4GforCMCC;
        }
        for (int i = 0; i < SIM_NUM; i++) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = 1;
            }
            logd("judgeNwModeForCMCC, slotId = " + i + "nwModeArray[i] = " + this.nwModeArray[i]);
        }
    }

    private void judgePreNwModeSubIdAndListForCMCC(int lteSlotId) {
        logd("in judgePreNwModeSubIdAndListForCMCC ");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.subCarrierTypeArray[lteSlotId].isCUCard() || this.subCarrierTypeArray[lteSlotId].isCCard()) {
            System.putInt(resolver, MAIN_CARD_INDEX, -1);
            System.putInt(resolver, NETWORK_MODE_4G_PRE, -1);
            System.putInt(resolver, NETWORK_MODE_3G_PRE, -1);
            System.putInt(resolver, NETWORK_MODE_2G_ONLY, -1);
        } else {
            System.putInt(resolver, MAIN_CARD_INDEX, lteSlotId);
            putPreNWModeListToDBforCMCC();
        }
        try {
            logd("main card index: " + System.getInt(resolver, MAIN_CARD_INDEX) + " network mode 4G pre: " + System.getInt(resolver, NETWORK_MODE_4G_PRE) + " network mode 3G pre: " + System.getInt(resolver, NETWORK_MODE_3G_PRE) + " network mode 2G only: " + System.getInt(resolver, NETWORK_MODE_2G_ONLY));
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading PreNwMode SubId AndList Values");
        }
    }

    private void judgeNwModeForFullNetwork(int lteSlotId, int nwMode4GforCU, int nwMode4GforCMCC, int nwMode4GforCT) {
        logd("judgeNwModeForFullNetwork start");
        if (this.subCarrierTypeArray[lteSlotId].isCCard()) {
            this.nwModeArray[lteSlotId] = nwMode4GforCT;
        } else if (this.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
            this.nwModeArray[lteSlotId] = nwMode4GforCMCC;
        } else {
            this.nwModeArray[lteSlotId] = nwMode4GforCU;
        }
        for (int i = 0; i < SIM_NUM; i++) {
            if (i != lteSlotId) {
                if (!IS_CARD2_CDMA_SUPPORTED) {
                    this.nwModeArray[i] = 1;
                } else if (!this.subCarrierTypeArray[i].isCCard() || (this.subCarrierTypeArray[lteSlotId].isCCard() ^ 1) == 0) {
                    this.nwModeArray[i] = 3;
                } else {
                    this.nwModeArray[i] = 7;
                }
            }
        }
    }

    private void saveIccidsWhenAllCardsReady() {
        logd("saveIccidsWhenAllCardsReady");
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        for (int i = 0; i < SIM_NUM; i++) {
            String iccIdToSave = this.mFullIccIds[i];
            if ((iccIdToSave != null && !"".equals(iccIdToSave)) || (IS_CMCC_4GSWITCH_DISABLE ^ 1) == 0 || (IS_CT_4GSWITCH_DISABLE ^ 1) == 0) {
                try {
                    iccIdToSave = HwAESCryptoUtil.encrypt(MASTER_PASSWORD, iccIdToSave);
                } catch (Exception ex) {
                    logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
                }
                editor.putString("4G_AUTO_SWITCH_ICCID_SLOT" + i, iccIdToSave);
                editor.apply();
            }
        }
    }

    private boolean anySimCardChanged() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        for (int i = 0; i < SIM_NUM; i++) {
            String oldIccId = sp.getString("4G_AUTO_SWITCH_ICCID_SLOT" + i, "");
            if (!"".equals(oldIccId)) {
                try {
                    oldIccId = HwAESCryptoUtil.decrypt(MASTER_PASSWORD, oldIccId);
                } catch (Exception ex) {
                    logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
                }
            }
            String nowIccId = this.mFullIccIds[i];
            logd("anySimCardChanged oldIccId" + i + " = " + SubscriptionInfo.givePrintableIccid(oldIccId));
            logd("anySimCardChanged nowIccId" + i + " = " + SubscriptionInfo.givePrintableIccid(this.mIccIds[i]));
            if (nowIccId == null) {
                nowIccId = "";
            }
            if (!oldIccId.equals(this.mFullIccIds[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean judgeDefalt4GSlot() {
        logd("judgeDefalt4GSlot start");
        if (judgeDefalt4GSlotForMDM()) {
            return true;
        }
        if (IS_CMCC_4G_DSDX_ENABLE) {
            logd("cmcc 4g dsdx begin.");
            if (HwModemCapability.isCapabilitySupport(9)) {
                return judgeDefault4GSlotForCMCC();
            }
            return judgeDefaultSlotId4HisiCmcc(false);
        } else if (IS_CMCC_CU_DSDX_ENABLE) {
            logd("cmcc_cu 4g dsdx begin.");
            if (HwModemCapability.isCapabilitySupport(9)) {
                return judgeDefault4GSlotForCMCC();
            }
            return judgeDefaultSlotId4Hisi();
        } else if (IS_CT_4GSWITCH_DISABLE) {
            logd("ct 4g switch begin.");
            if (HwModemCapability.isCapabilitySupport(9)) {
                return judgeDefault4GSlotForCMCC();
            }
            return judgeDefault4GSlotForCT(false);
        } else if (IS_4G_SWITCH_SUPPORTED && IS_FULL_NETWORK_SUPPORTED) {
            return judgeDefault4GSlotForChannelVersion();
        } else {
            if (!IS_4G_SWITCH_SUPPORTED && IS_FULL_NETWORK_SUPPORTED) {
                this.default4GSlot = 0;
                return true;
            } else if (IS_CHINA_TELECOM) {
                logd("china telecom dsdx begin.");
                return switchDualCardsSlotCdma();
            } else if (HwModemCapability.isCapabilitySupport(9)) {
                return false;
            } else {
                return judgeDefaultSlotId4Hisi();
            }
        }
    }

    private boolean judgeDefault4GSlotForCMCC() {
        logd("judgeDefault4GSlotForCMCC enter");
        int curSimCount = 0;
        int noSimCount = 0;
        boolean is4GSlotFound = false;
        for (int i = 0; i < SIM_NUM; i++) {
            if (this.isSimInsertedArray[i]) {
                curSimCount++;
                if (!is4GSlotFound) {
                    this.default4GSlot = i;
                }
                is4GSlotFound = true;
            } else {
                noSimCount++;
            }
        }
        logd("curSimCount =" + curSimCount + ", noSimCount = " + noSimCount);
        if (SIM_NUM != curSimCount + noSimCount || curSimCount == 0) {
            loge("cards status error or all cards absent.");
            return false;
        } else if (1 == curSimCount) {
            return true;
        } else {
            initSubTypes();
            int mSub = getMainCardSubByPriority(SubType.CARRIER_PREFERRED);
            logd(this.currentSubTypeMap.toString());
            logd("4G slot sub is " + mSub);
            switch (mSub) {
                case 0:
                case 1:
                    this.default4GSlot = mSub;
                    break;
                case 10:
                    logd("The two cards inserted have the same priority!");
                    this.default4GSlot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
                    break;
                default:
                    is4GSlotFound = false;
                    break;
            }
            return is4GSlotFound;
        }
    }

    private void initSubTypes() {
        logd("in initSubTypes.");
        for (int index = 0; index < SIM_NUM; index++) {
            SubType mSubType;
            if (IS_CT_4GSWITCH_DISABLE) {
                mSubType = getSubTypeBySubForCT(index);
            } else {
                mSubType = getSubTypeBySub(index);
            }
            this.currentSubTypeMap.put(Integer.valueOf(index), mSubType);
        }
    }

    public SubType getSubTypeBySub(int sub) {
        logd("in getSubTypeBySub, sub = " + sub);
        SubType subType = SubType.ERROR;
        if (sub < 0 || sub >= SIM_NUM || !this.isSimInsertedArray[sub]) {
            loge("Error, sub = " + sub);
            return SubType.ERROR;
        }
        logd("subCarrierTypeArray[sub] = " + this.subCarrierTypeArray[sub]);
        switch (-getcom-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues()[this.subCarrierTypeArray[sub].ordinal()]) {
            case 1:
                subType = SubType.CARRIER;
                break;
            case 2:
                subType = SubType.CARRIER_PREFERRED;
                break;
            case 3:
            case 4:
                if (IS_CHINA_TELECOM) {
                    subType = SubType.CARRIER_PREFERRED;
                    break;
                }
                break;
            case 6:
                if (IS_CMCC_CU_DSDX_ENABLE) {
                    subType = SubType.CARRIER_PREFERRED;
                    break;
                }
            case 5:
                if (!IS_CMCC_CU_DSDX_ENABLE) {
                    subType = SubType.LOCAL_CARRIER;
                    break;
                }
                subType = SubType.CARRIER;
                break;
            case 7:
            case 10:
                subType = SubType.FOREIGN_CARRIER_PREFERRED;
                break;
            default:
                subType = SubType.FOREIGN_CARRIER;
                break;
        }
        return subType;
    }

    private Integer getKeyFromMap(Map<Integer, SubType> map, SubType type) {
        for (Entry<Integer, SubType> mapEntry : map.entrySet()) {
            if (mapEntry.getValue() == type) {
                return (Integer) mapEntry.getKey();
            }
        }
        return Integer.valueOf(-1);
    }

    private int getMainCardSubByPriority(SubType targetType) {
        logd("in getMainCardSubByPriority, input targetType = " + targetType);
        int count = 0;
        for (Entry<Integer, SubType> mapEntry : this.currentSubTypeMap.entrySet()) {
            if (mapEntry.getValue() == targetType) {
                count++;
            }
        }
        logd("count = " + count);
        if (1 == count) {
            return getKeyFromMap(this.currentSubTypeMap, targetType).intValue();
        }
        if (1 < count) {
            logd("The priority is the same between two slots: return SAME_PRIORITY");
            return 10;
        } else if (targetType.ordinal() < SubType.ERROR.ordinal()) {
            return getMainCardSubByPriority(SubType.values()[targetType.ordinal() + 1]);
        } else {
            return -1;
        }
    }

    private boolean judgeDefault4GSlotForChannelVersion() {
        logd("judgeDefault4GSlotForChannelVersion start");
        int numOfSimPresent = 0;
        if (!HwModemCapability.isCapabilitySupport(9)) {
            return judgeDefaultSlotId4Hisi();
        }
        int i;
        boolean is4GSlotFound;
        for (i = 0; i < SIM_NUM; i++) {
            if (this.isSimInsertedArray[i]) {
                numOfSimPresent++;
            }
        }
        if (numOfSimPresent == 0) {
            logd("no card inserted");
            is4GSlotFound = false;
        } else if (1 == numOfSimPresent) {
            is4GSlotFound = judgeDefault4GSlotForSingleSim(false);
        } else if (IS_CARD2_CDMA_SUPPORTED || IS_QCRIL_CROSS_MAPPING) {
            is4GSlotFound = true;
            this.default4GSlot = 0;
        } else {
            int numOfCdmaCard = 0;
            int indexOfCCard = 0;
            i = 0;
            while (i < SIM_NUM) {
                if (this.isSimInsertedArray[i] && this.subCarrierTypeArray[i].isCCard()) {
                    numOfCdmaCard++;
                    indexOfCCard = i;
                }
                i++;
            }
            if (1 == numOfCdmaCard) {
                logd("there is only one CDMA card inserted, set it as the 4G slot");
                is4GSlotFound = true;
                this.default4GSlot = indexOfCCard;
            } else {
                logd("there are multiple CDMA cards or U cards inserted, set the SUB_0 as the lte slot");
                is4GSlotFound = true;
                this.default4GSlot = 0;
                if (numOfCdmaCard > 1) {
                    this.updateUserDefaultFlag = true;
                }
            }
        }
        return is4GSlotFound;
    }

    private boolean judgeDefault4GSlotForSingleSim(boolean is4GSlotFound) {
        int i = 0;
        while (SIM_NUM > i && !this.isSimInsertedArray[i]) {
            logd("isSimInsertedArray[" + i + "] = " + this.isSimInsertedArray[i]);
            i++;
        }
        if (SIM_NUM == i) {
            logd("there is no sim card inserted, error happen!!");
            return false;
        }
        logd("there is only one card inserted, set it as the 4G slot");
        this.default4GSlot = i;
        return true;
    }

    public boolean isCMCCCard(String inn) {
        return HwIccIdUtil.isCMCC(inn);
    }

    public boolean isCUCard(String inn) {
        return HwIccIdUtil.isCU(inn);
    }

    public static boolean isCTCard(String inn) {
        return HwIccIdUtil.isCT(inn);
    }

    private boolean isCMCCCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isCMCCByMccMnc(mccMnc);
    }

    private boolean isCUCardByMccMnc(String mccMnc) {
        return HwIccIdUtil.isCUByMccMnc(mccMnc);
    }

    public boolean isCMCCCardBySlotId(int slotId) {
        if (!isValidIndex(slotId)) {
            logd("isCMCCCardBySlotId: Invalid slotId: " + slotId);
            return false;
        } else if (this.mIccIds[slotId] != null) {
            return isCMCCCard(this.mIccIds[slotId]);
        } else {
            logd("isCMCCCardBySlotId: mIccIds[" + slotId + "] is null");
            return false;
        }
    }

    public boolean isCMCCHybird() {
        boolean hasCMCC = false;
        boolean hasOther = false;
        for (int i = 0; i < SIM_NUM; i++) {
            if (this.mIccIds[i] == null) {
                return false;
            }
            if (isCMCCCard(this.mIccIds[i])) {
            }
            hasCMCC = true;
        }
        if (!hasCMCC) {
            hasOther = false;
        }
        return hasOther;
    }

    public boolean isCTCardBySlotId(int slotId) {
        if (!isValidIndex(slotId)) {
            logd("isCMCCCardBySlotId: Invalid slotId: " + slotId);
            return false;
        } else if (HwModemCapability.isCapabilitySupport(9)) {
            if (this.subCarrierTypeArray[slotId] != null) {
                return this.subCarrierTypeArray[slotId].isCTCard();
            }
            logd("isCTCardBySlotId: subCarrierTypeArray[" + slotId + "] is null");
            return false;
        } else if (this.mIccIds[slotId] != null) {
            return isCTCard(this.mIccIds[slotId]);
        } else {
            logd("isCTCardBySlotId: mIccIds[" + slotId + "] is null");
            return false;
        }
    }

    private void saveIccidBySlot(int slot, String iccId) {
        logd("saveIccidBySlot");
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        String iccIdToSave = "";
        try {
            iccIdToSave = HwAESCryptoUtil.encrypt(MASTER_PASSWORD, iccId);
        } catch (Exception ex) {
            logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
        }
        editor.putString("4G_AUTO_SWITCH_ICCID_SLOT" + slot, iccIdToSave);
        editor.apply();
    }

    public boolean isCTHybird() {
        boolean hasCTCard = false;
        for (int i = 0; i < SIM_NUM; i++) {
            if (isCTCardBySlotId(i)) {
            }
            hasCTCard = true;
        }
        logd("isCTHybird : hasCTCard = " + hasCTCard + " ; hasOther = " + false);
        if (hasCTCard) {
            return false;
        }
        return false;
    }

    public boolean judgeDefault4GSlotForCT(boolean forceSwitch) {
        logd("judgeDefault4GSlotForCT start");
        int temSub = getUserSwitchDualCardSlots();
        if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < SIM_NUM) {
            Rlog.d(this.TAG, "haven't get all UiccCards done, please wait!");
            return false;
        }
        int i;
        for (UiccCard uc : this.mUiccController.getUiccCards()) {
            if (uc == null) {
                Rlog.d(this.TAG, "haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        for (i = 0; i < SIM_NUM; i++) {
            if (this.mIccIds[i] == null) {
                logd("mIccIds[" + i + "] is null, and return");
                return false;
            }
        }
        UiccCard[] mUiccCards = this.mUiccController.getUiccCards();
        boolean isCard1Present = mUiccCards[0].getCardState() == CardState.CARDSTATE_PRESENT;
        boolean isCard2Present = mUiccCards[1].getCardState() == CardState.CARDSTATE_PRESENT;
        boolean isAnySimCardChanged = (anySimCardChanged() || this.isPreBootCompleted) ? true : forceSwitch;
        if (this.isPreBootCompleted) {
            logd("judgeDefault4GSlotForCT: reset isPreBootCompleted.");
            this.isPreBootCompleted = false;
        }
        logd("judgeDefault4GSlotForCT isAnySimCardChanged = " + isAnySimCardChanged);
        if (isCard1Present && (isCard2Present ^ 1) != 0) {
            temSub = IS_FAST_SWITCH_SIMSLOT ? 0 : isBalongSimSynced() ? 0 : 1;
        } else if (!isCard1Present && isCard2Present) {
            temSub = IS_FAST_SWITCH_SIMSLOT ? 1 : isBalongSimSynced() ? 1 : 0;
        } else if (isCard1Present && isCard2Present) {
            if (isAnySimCardChanged || !IS_HISI_DSDX) {
                boolean[] isCTCards = new boolean[SIM_NUM];
                int cardtype1 = (this.mCardTypes[0] & 240) >> 4;
                int cardtype2 = (this.mCardTypes[1] & 240) >> 4;
                for (i = 0; i < SIM_NUM; i++) {
                    isCTCards[i] = isCTCard(this.mIccIds[i]);
                }
                if (isCTCards[0] && isCTCards[1]) {
                    if (cardtype1 == 2 && cardtype2 == 1) {
                        temSub = isBalongSimSynced() ? 0 : 1;
                    } else if (cardtype1 == 1 && cardtype2 == 2) {
                        temSub = isBalongSimSynced() ? 1 : 0;
                    }
                } else if (isCTCards[0]) {
                    temSub = IS_FAST_SWITCH_SIMSLOT ? 0 : isBalongSimSynced() ? 0 : 1;
                } else if (isCTCards[1]) {
                    temSub = IS_FAST_SWITCH_SIMSLOT ? 1 : isBalongSimSynced() ? 1 : 0;
                }
                logd("cardtype1 = " + cardtype1 + ", cardtype2 = " + cardtype2 + ", isCTCards[SUB1] " + isCTCards[0] + ", isCTCards[SUB2] " + isCTCards[1]);
            } else {
                logd("judgeDefaultSlotId4HisiCmcc all sim present but none sim change ");
                return false;
            }
        }
        this.default4GSlot = temSub;
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", default4GSlot " + this.default4GSlot);
        return true;
    }

    public SubType getSubTypeBySubForCT(int sub) {
        logd("in getSubTypeBySubForCT, sub = " + sub);
        SubType subType = SubType.ERROR;
        if (sub < 0 || sub >= SIM_NUM || !this.isSimInsertedArray[sub]) {
            loge("Error, sub = " + sub);
            return SubType.ERROR;
        }
        logd("subCarrierTypeArray[sub] = " + this.subCarrierTypeArray[sub]);
        switch (-getcom-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues()[this.subCarrierTypeArray[sub].ordinal()]) {
            case 3:
                subType = SubType.CARRIER_PREFERRED;
                break;
            case 4:
                subType = SubType.CARRIER;
                break;
            case 7:
                subType = SubType.FOREIGN_CARRIER_PREFERRED;
                break;
            case 8:
                subType = SubType.FOREIGN_CARRIER;
                break;
            default:
                subType = SubType.LOCAL_CARRIER;
                break;
        }
        return subType;
    }

    private int getCardAppType(int slotId) {
        if (this.mUiccController == null) {
            return 0;
        }
        UiccCard uiccCard = this.mUiccController.getUiccCard(slotId);
        if (uiccCard == null) {
            logd("getCardAppType: uiccCard is null for slot " + slotId);
            return 0;
        }
        int appType;
        if (uiccCard.getApplicationByType(4) != null) {
            appType = 4;
        } else if (uiccCard.getApplicationByType(3) != null) {
            appType = 3;
        } else if (uiccCard.getApplicationByType(2) != null) {
            appType = 2;
        } else if (uiccCard.getApplicationByType(1) != null) {
            appType = 1;
        } else {
            appType = 0;
        }
        logd("getCardAppType: the app type for slot " + slotId + " is " + appType);
        return appType;
    }

    private void setDefault4GSlot(int slotId) {
        int LteSlot;
        int gsmOnlySlot;
        this.expectedDDSsubId = slotId;
        logd("setDefault4GSlot: will set " + slotId + " as the default 4G slot");
        if (slotId == 0) {
            LteSlot = 0;
            gsmOnlySlot = 1;
        } else if (1 == slotId) {
            LteSlot = 1;
            gsmOnlySlot = 0;
        } else {
            loge("setDefault4GSlot: invalid slotId is input, right now, only support two cards!");
            if (this.mEventsQ.size() > 0) {
                handleDelayedEvent();
            } else {
                this.isSet4GSlotInProgress = false;
            }
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
            this.mSet4GSlotCompleteMsg = null;
            return;
        }
        if (!isSetDefault4GSlotNeeded(slotId)) {
            loge("setDefault4GSlot: there is no need to set the lte slot");
            if (this.mEventsQ.size() > 0) {
                handleDelayedEvent();
                return;
            }
            this.isSet4GSlotInProgress = false;
            if (IS_CARD2_CDMA_SUPPORTED && this.mSet4GSlotCompleteMsg == null) {
                logd("In auto set 4GSlot mode , makesure DDS slot same as 4G slot so set DDS to slot: " + slotId);
                HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(slotId);
            }
            if (IS_QCRIL_CROSS_MAPPING) {
                setLteServiceAbility();
                logd("set DDS to slot: " + slotId);
                HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(slotId);
                int needSetCount = PhoneFactory.onDataSubChange(0, null);
                logd("needSetCount = " + needSetCount + "; mNeedSetAllowData = " + this.mNeedSetAllowData);
                if (needSetCount == 0 && this.mNeedSetAllowData) {
                    this.mNeedSetAllowData = false;
                    for (int i = 0; i < SIM_NUM; i++) {
                        PhoneFactory.resendDataAllowed(i);
                        logd("setDefault4GSlot resend data allow with slot " + i);
                    }
                }
            }
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 0);
            this.mSet4GSlotCompleteMsg = null;
        } else if (this.mCis[slotId] == null || RadioState.RADIO_UNAVAILABLE != this.mCis[slotId].getRadioState()) {
            if (RESET_PROFILE) {
                this.mCis[gsmOnlySlot].resetProfile(null);
                logd("setDefault4GSlot: resetProfile");
            }
            this.isSet4GSlotInProgress = true;
            sendHwSwitchSlotStartBroadcast();
            if (IS_QCRIL_CROSS_MAPPING) {
                int expectedMaxCapabilitySubId = getExpectedMaxCapabilitySubId(slotId);
                if (-1 != expectedMaxCapabilitySubId) {
                    logd("setDefault4GSlot:setMaxRadioCapability,expectedMaxCapabilitySubId=" + expectedMaxCapabilitySubId);
                    setMaxRadioCapability(expectedMaxCapabilitySubId);
                } else {
                    logd("setDefault4GSlot:don't setMaxRadioCapability,response message");
                    sendSetRadioCapabilitySuccess(false);
                }
            } else {
                backupPreferredNwModeAndLteSlot();
                if (HwModemCapability.isCapabilitySupport(9)) {
                    TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", LteSlot, this.nwModeArray[LteSlot]);
                    TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", gsmOnlySlot, this.nwModeArray[gsmOnlySlot]);
                    System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", slotId);
                    logd("set prefNWMode for gsmOnlySlot before set LteSlot");
                    this.mCis[gsmOnlySlot].setPreferredNetworkType(this.nwModeArray[gsmOnlySlot], null);
                    setPrefNetworkTypeAndStartTimer(LteSlot);
                } else if (IS_FAST_SWITCH_SIMSLOT) {
                    fastSwitchDualCardsSlot(slotId, null);
                } else {
                    switchDualCardsSlot(slotId, obtainMessage(EVENT_SWITCH_DUAL_CARD_SLOT));
                }
            }
        } else {
            loge("setDefault4GSlot: radio is unavailable, return with failure");
            this.mAutoSwitchDualCardsSlotDone = false;
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
            this.mSet4GSlotCompleteMsg = null;
            if (HwModemCapability.isCapabilitySupport(9)) {
                if (this.mEventsQ.size() == 0) {
                    this.mCis[slotId].registerForOn(this, 12, Integer.valueOf(slotId));
                    logd("put the event into queue to process it when radio on!");
                    this.isSet4GSlotInProgress = true;
                    this.mEventsQ.addLast(new DelayedEvent(1, slotId, this.nwModeArray));
                } else {
                    logd("there are still events unprocessed in queque with mEventsQ.size() = " + this.mEventsQ.size());
                    handleDelayedEvent();
                }
            }
        }
    }

    public void switchDualCardsSlot(int slot, Message onCompleteMsg) {
        int isVSimRelated;
        logd(" switchDualCardsSlot   ------>>> begin");
        if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate()) {
            isVSimRelated = 1;
        } else {
            isVSimRelated = HwVSimUtils.isAllowALSwitch() ^ 1;
        }
        if (isVSimRelated != 0) {
            logd("vsim on sub");
            setWaitingSwitchBalongSlot(false);
            if (onCompleteMsg != null) {
                AsyncResult.forMessage(onCompleteMsg, Boolean.valueOf(false), null);
                loge("Switch Dual Card Slots failed!! Sending the cnf back!");
                onCompleteMsg.sendToTarget();
            }
            return;
        }
        this.mSetSdcsCompleteMsg = onCompleteMsg;
        if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
            HwFullNetwork.getInstance().switchCommrilModeIfNeeded(slot, 1);
        }
        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
            HwForeignUsimForTelecom.getInstance().setMainSlot(slot);
        }
        Message callbackMsg = obtainMessage(EVENT_SWITCH_SIM_SLOT_CFG_DONE, Integer.valueOf(slot));
        if (slot == this.mBalongSimSlot && isBalongSimSynced()) {
            loge("switchDualCardsSlot: slot is same as mBalongSimSlot, return failure");
            if (IS_CMCC_4G_DSDX_ENABLE || IS_CT_4GSWITCH_DISABLE) {
                saveIccidsWhenAllCardsReady();
            }
            sendResponseToTarget(callbackMsg, 2);
            return;
        }
        int index = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
        if (!(index == 0 || index == 1)) {
            index = slot == 0 ? 0 : 1;
            loge("index =  " + index);
        }
        if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
            HwDsdsController.getInstance().setNeedWatingSlotSwitchDone(true);
        }
        if (this.mHas3Modem) {
            int expectSlot = slot;
            this.mCis[index].switchBalongSim(slot, slot == 0 ? 1 : 0, 2, callbackMsg);
        } else if (slot == 0) {
            this.mCis[index].switchBalongSim(1, 2, callbackMsg);
        } else {
            this.mCis[index].switchBalongSim(2, 1, callbackMsg);
        }
        logd(" switchDualCardsSlot   ------>>> end");
    }

    private boolean isSetDefault4GSlotNeeded(int lteSlotId) {
        if (!HwModemCapability.isCapabilitySupport(9)) {
            return true;
        }
        int gsmOnlySlot = 1;
        if (lteSlotId == 0) {
            gsmOnlySlot = 1;
        } else if (1 == lteSlotId) {
            gsmOnlySlot = 0;
        }
        if (lteSlotId != HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId()) {
            logd("lte slot is not the same, return true");
            return true;
        } else if (IS_QCRIL_CROSS_MAPPING) {
            boolean isSetDefault4GSlot = -1 != getExpectedMaxCapabilitySubId(lteSlotId);
            logd("isSetDefault4GSlotNeeded:" + isSetDefault4GSlot);
            return isSetDefault4GSlot;
        } else {
            int nwModeForLte;
            try {
                nwModeForLte = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", lteSlotId);
            } catch (SettingNotFoundException e) {
                logd("reading the preferred nework mokde failed!!");
                nwModeForLte = -1;
            }
            logd("check if set default 4G slot is needed with nwModeForLte = " + nwModeForLte + " nwModeArray[" + lteSlotId + "] = " + this.nwModeArray[lteSlotId]);
            if (nwModeForLte != this.nwModeArray[lteSlotId]) {
                return true;
            }
            int nwModeforGsmOnlySlot;
            try {
                nwModeforGsmOnlySlot = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", gsmOnlySlot);
            } catch (SettingNotFoundException e2) {
                logd("reading the preferred nework mokde failed!!");
                nwModeforGsmOnlySlot = -1;
            }
            if (nwModeforGsmOnlySlot != this.nwModeArray[gsmOnlySlot]) {
                logd("update the current nwMode into DB for gsmOnlySlot when factory reset");
                if (IS_CARD2_CDMA_SUPPORTED) {
                    return true;
                }
                TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", gsmOnlySlot, this.nwModeArray[gsmOnlySlot]);
            }
            return false;
        }
    }

    private void sendResponseToTarget(Message response, int responseCode) {
        if (response != null && response.getTarget() != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            try {
                response.sendToTarget();
            } catch (IllegalStateException e) {
                loge("response is sent, don't send again!!");
            }
        }
    }

    private void backupPreferredNwModeAndLteSlot() {
        for (int i = 0; i < SIM_NUM && HwModemCapability.isCapabilitySupport(9); i++) {
            try {
                this.mPrefNwMode[i] = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i);
            } catch (SettingNotFoundException e) {
                loge("getPreferredNetworkMode: Could not find PREFERRED_NETWORK_MODE!!!");
                this.mPrefNwMode[i] = Phone.PREFERRED_NT_MODE;
            }
            logd("getPreferredNetworkMode: mPrefNwMode[" + i + "] = " + this.mPrefNwMode[i]);
        }
        try {
            this.current4GSlotBackup = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e2) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            this.current4GSlotBackup = 0;
        }
        logd("current4GSlotBackup = " + this.current4GSlotBackup);
    }

    private void restorePreferredNwModeAndLteSlot() {
        for (int i = 0; i < SIM_NUM && HwModemCapability.isCapabilitySupport(9); i++) {
            TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i, this.mPrefNwMode[i]);
        }
        System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", this.current4GSlotBackup);
    }

    public void setDefault4GSlot(int slotId, Message responseMsg) {
        this.is4GSlotReviewNeeded = 2;
        this.mUserPref4GSlot = slotId;
        this.prefer4GSlot = slotId;
        if (slotId == HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId()) {
            loge("setDefault4GSlot: the default 4G slot is already " + slotId);
            sendResponseToTarget(responseMsg, 0);
        } else if (this.isSet4GSlotInProgress) {
            loge("setDefault4GSlot: The setting is in progress, return failure");
            sendResponseToTarget(responseMsg, 2);
        } else {
            logd("setDefault4GSlot: target slot id is: " + slotId);
            this.mSet4GSlotCompleteMsg = responseMsg;
            refreshCardState();
            if (HwModemCapability.isCapabilitySupport(9)) {
                judgeSubCarrierType();
                judgeNwMode(slotId);
            }
            setDefault4GSlot(slotId);
        }
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        boolean z = true;
        int numOfCardInserted = 0;
        if (1 >= SIM_NUM) {
            logd("This is not a multi-sim handset");
            return false;
        }
        for (int i = 0; i < SIM_NUM; i++) {
            refreshCardState();
            if (this.isSimInsertedArray[i]) {
                numOfCardInserted++;
            }
        }
        if (2 > numOfCardInserted) {
            z = false;
        }
        return z;
    }

    private void handleDelayedEvent() {
        if (this.mEventsQ.size() != 0) {
            DelayedEvent event = (DelayedEvent) this.mEventsQ.poll();
            if (event != null) {
                int slotId = event.slotId;
                this.nwModeArray = event.networkModeArray;
                switch (event.id) {
                    case 1:
                        setDefault4GSlot(slotId);
                        return;
                    case 2:
                        logd("EVENT_RADIO_ON_SET_PREF_NETWORK");
                        setPrefNetworkTypeAndStartTimer(slotId);
                        return;
                    case 3:
                        logd("EVENT_RADIO_ON_PROCESS_SIM_STATE");
                        processSimStateChanged("IMSI", slotId);
                        return;
                    default:
                        loge("unsupported event in handleDelayedEvent!");
                        return;
                }
            }
        }
    }

    private void startSetPrefNetworkTimer() {
        Message message = obtainMessage(14);
        AsyncResult.forMessage(message, null, null);
        sendMessageDelayed(message, 60000);
        logd("startSetPrefNetworkTimer!");
    }

    private void setPrefNetworkTypeAndStartTimer(int slotId) {
        startSetPrefNetworkTimer();
        if (IS_QCRIL_CROSS_MAPPING) {
            ProxyController.getInstance().retrySetRadioCapabilities();
        } else {
            HwModemBindingPolicyHandler.getInstance().setPreferredNetworkType(this.nwModeArray[slotId], slotId, obtainSetPreNWMessage(slotId));
        }
    }

    private synchronized void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        if (ar != null) {
            if (ar.result != null) {
                this.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[0] & 15;
                saveCardTypeProperties(((int[]) ar.result)[0], index.intValue());
                this.mCardTypes[index.intValue()] = ((int[]) ar.result)[0];
                HwVSimUtils.updateSimCardTypes(this.mSwitchTypes);
            }
        }
        checkIfAllCardsReady();
    }

    private void saveCardTypeProperties(int cardTypeResult, int index) {
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
                }
                cardType = 20;
                break;
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
                }
                cardType = 43;
                break;
                break;
        }
        logd("uiccOrIcc :  " + uiccOrIcc + ", appType : " + appType + ", cardType : " + cardType);
        if (index == 0) {
            SystemProperties.set(CARD_TYPE_SIM1, String.valueOf(cardType));
        } else {
            SystemProperties.set(CARD_TYPE_SIM2, String.valueOf(cardType));
        }
        if (!this.mBroadcastDone && (HwHotplugController.IS_HOTSWAP_SUPPORT ^ 1) != 0 && IS_CHINA_TELECOM && isNoneCTcard()) {
            this.mBroadcastDone = true;
            broadcastForHwCardManager(index);
        }
    }

    private boolean isNoneCTcard() {
        boolean z = true;
        if (IS_4G_SWITCH_SUPPORTED) {
            boolean result = this.mSwitchTypes[0] == 1 ? this.mSwitchTypes[1] == 1 : false;
            return result;
        }
        if (this.mSwitchTypes[HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId()] != 1) {
            z = false;
        }
        return z;
    }

    private void broadcastForHwCardManager(int sub) {
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        Rlog.d(this.TAG, "[broadcastForHwCardManager]");
        intent.putExtra("popupDialog", "true");
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    private void onGetBalongSimDone(AsyncResult ar, Integer index) {
        if (ar != null && ar.result != null && ((int[]) ar.result).length == 3) {
            int[] slots = ar.result;
            boolean isMainSlotOnVSim = false;
            logd("slot result = " + Arrays.toString(slots));
            if (slots[0] == 0 && slots[1] == 1 && slots[2] == 2) {
                this.mBalongSimSlot = 0;
                isMainSlotOnVSim = false;
            } else if (slots[0] == 1 && slots[1] == 0 && slots[2] == 2) {
                this.mBalongSimSlot = 1;
                isMainSlotOnVSim = false;
            } else if (slots[0] == 2 && slots[1] == 1 && slots[2] == 0) {
                this.mBalongSimSlot = 0;
                isMainSlotOnVSim = true;
            } else if (slots[0] == 2 && slots[1] == 0 && slots[2] == 1) {
                this.mBalongSimSlot = 1;
                isMainSlotOnVSim = true;
            } else {
                loge("onGetBalongSimDone invalid slot result");
            }
            logd("isMainSlotOnVSim = " + isMainSlotOnVSim);
            this.mHas3Modem = true;
            this.mGetBalongSimSlotDone[index.intValue()] = true;
        } else if (ar == null || ar.result == null || ((int[]) ar.result).length != 2) {
            loge("onGetBalongSimDone error");
        } else {
            if (((int[]) ar.result)[1] + ((int[]) ar.result)[0] > 1) {
                this.mBalongSimSlot = ((int[]) ar.result)[0] - 1;
            } else {
                this.mBalongSimSlot = ((int[]) ar.result)[0];
            }
            this.mHas3Modem = false;
            this.mGetBalongSimSlotDone[index.intValue()] = true;
        }
        logd("mBalongSimSlot = " + this.mBalongSimSlot);
        int countGetBalongSimSlotDone = 0;
        for (int i = 0; i < SIM_NUM; i++) {
            if (this.mGetBalongSimSlotDone[i]) {
                countGetBalongSimSlotDone++;
            }
        }
        if (countGetBalongSimSlotDone == 1) {
            checkIfAllCardsReady();
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    public int getBalongSimSlot() {
        return this.mBalongSimSlot;
    }

    public boolean isCardPresent(int slotId) {
        boolean z = false;
        if (this.mUiccController.getUiccCard(slotId) == null) {
            return false;
        }
        if (this.mUiccController.getUiccCard(slotId).getCardState() != CardState.CARDSTATE_ABSENT) {
            z = true;
        }
        return z;
    }

    public boolean isAllCardsAbsent() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            if (isCardPresent(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isntFirstPowerup() {
        return true;
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        boolean z = false;
        Rlog.d(this.TAG, "mSwitchTypes[0] = " + this.mSwitchTypes[0] + ", mSwitchTypes[1] = " + this.mSwitchTypes[1]);
        boolean result = false;
        if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < 2) {
            Rlog.d(this.TAG, "haven't get all UiccCards done, please wait!");
            return false;
        }
        for (UiccCard uc : this.mUiccController.getUiccCards()) {
            if (uc == null) {
                Rlog.d(this.TAG, "haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        if (!isSwitchSlotEnabledForCMCC()) {
            Rlog.d(this.TAG, "isSwitchSlotEnabledForCMCC: CMCC hybird and CMCC is not roaming return false");
            return false;
        } else if (IS_CT_4GSWITCH_DISABLE && isCTHybird()) {
            return false;
        } else {
            if (IS_CHINA_TELECOM) {
                boolean isValidSwitchType = ((this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 3) || ((this.mSwitchTypes[0] == 3 && this.mSwitchTypes[1] == 2) || ((this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 3) || (this.mSwitchTypes[0] == 2 && this.mSwitchTypes[1] == 2)))) ? true : this.mSwitchTypes[0] == 1 && this.mSwitchTypes[1] == 1;
                if (isValidSwitchType) {
                    result = true;
                }
                if (this.mSwitchTypes[0] == 1 && this.mSwitchTypes[1] == 1 && HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT && (HwForeignUsimForTelecom.getInstance().isDomesticCard(this.mBalongSimSlot) ^ 1) != 0) {
                    Rlog.d(this.TAG, "CT mode got 2 U cards, foreign U card is in main slot, return false");
                    result = false;
                }
                return result;
            }
            refreshCardState();
            if (this.mSwitchTypes[0] > 0 || this.isSimInsertedArray[0]) {
                z = this.mSwitchTypes[1] <= 0 ? this.isSimInsertedArray[1] : true;
            }
            return z;
        }
    }

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        logd("setWaitingSwitchBalongSlot  iSetResult = " + iSetResult);
        if (IS_FULL_NETWORK_SUPPORTED_IN_HISI && HwFullNetwork.getInstance().getWaitingSwitchCommrilMode() && (iSetResult ^ 1) != 0) {
            logd("mWaitingSwitchCommrilMode is true, don't setWaitingSwitchBalongSlot false now.");
            return;
        }
        this.isSet4GSlotInProgress = iSetResult;
        SystemProperties.set("gsm.dualcards.switch", iSetResult ? "true" : "false");
        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(0), null));
        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(1), null));
    }

    public boolean getWaitingSwitchBalongSlot() {
        if (!HwModemCapability.isCapabilitySupport(9)) {
            return this.isSet4GSlotInProgress;
        }
        logd("qcom platform pass this way");
        return false;
    }

    public boolean get4GSlotInProgress() {
        return this.isSet4GSlotInProgress;
    }

    private void switchSlotFailed() {
        logd("switchSlotFailed");
        restorePreferredNwModeAndLteSlot();
        if (IS_CMCC_4G_DSDX_ENABLE) {
            judgePreNwModeSubIdAndListForCMCC(this.current4GSlotBackup);
            if (IS_CMCC_4GSWITCH_DISABLE && IS_VICE_WCDMA) {
                if (IS_FULL_NETWORK_SUPPORTED_IN_HISI && HwFullNetwork.getInstance().getNeedSwitchCommrilMode()) {
                    this.needRetrySetPrefNetwork = true;
                } else {
                    setPrefNwForCmcc();
                }
            }
        }
        if (HwModemCapability.isCapabilitySupport(9)) {
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
            this.mSet4GSlotCompleteMsg = null;
        }
        setWaitingSwitchBalongSlot(false);
    }

    private void switchSlotSuccess(int index) {
        logd("switchSlotSuccess");
        if (IS_CMCC_4G_DSDX_ENABLE || IS_CT_4GSWITCH_DISABLE) {
            saveIccidsWhenAllCardsReady();
        }
        HwFrameworkFactory.getHwInnerTelephonyManager().updateCrurrentPhone(index);
        if (HwModemCapability.isCapabilitySupport(9)) {
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 0);
            if (this.mSet4GSlotCompleteMsg == null) {
                logd("slience set network mode done, prepare to set DDS for slot " + index);
                HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(index);
            }
            this.mSet4GSlotCompleteMsg = null;
            this.isSet4GSlotInProgress = false;
        }
        if (index != getUserSwitchDualCardSlots()) {
            this.isSet4GSlotManuallyTriggered = this.mSet4GSlotCompleteMsg != null;
            logd("switch 4G slot from " + getUserSwitchDualCardSlots() + " to " + index + ", isSet4GSlotManuallyTriggered = " + this.isSet4GSlotManuallyTriggered);
        }
        setUserSwitchDualCardSlots(index);
        if (IS_CMCC_4GSWITCH_DISABLE && IS_VICE_WCDMA) {
            this.needRetrySetPrefNetwork = true;
        }
        disposeCardStatus(false);
    }

    private void handleIccATR(String strATR, Integer index) {
        logd("handleIccATR, ATR: [" + strATR + "], index:[" + index + "]");
        if (strATR == null || strATR.isEmpty()) {
            strATR = "null";
        }
        if (strATR.length() > 66) {
            logd("strATR.length() greater than PROP_VALUE_MAX");
            strATR = strATR.substring(0, 66);
        }
        if (index.intValue() == 0) {
            SystemProperties.set("gsm.sim.hw_atr", strATR);
        } else {
            SystemProperties.set("gsm.sim.hw_atr1", strATR);
        }
    }

    public void disposeCardStatus(boolean resetSwitchDualCardsFlag) {
        logd("disposeCardStatus. resetSwitchDualCardsFlag = " + resetSwitchDualCardsFlag);
        for (int i = 0; i < SIM_NUM; i++) {
            this.mSwitchTypes[i] = -1;
            this.mGetUiccCardsStatusDone[i] = false;
            this.mGetBalongSimSlotDone[i] = false;
            this.mCardTypes[i] = -1;
            this.mIccIds[i] = null;
            this.mFullIccIds[i] = null;
        }
        this.mAllCardsReady = false;
        if (IS_HISI_DSDX && resetSwitchDualCardsFlag) {
            logd("set mAutoSwitchDualCardsSlotDone to false");
            this.mAutoSwitchDualCardsSlotDone = false;
        }
    }

    public void disposeCardStatus(int slotID) {
        logd("disposeCardStatus slotid = " + slotID);
        if (slotID >= 0 && slotID < SIM_NUM) {
            this.mSwitchTypes[slotID] = -1;
            this.mGetUiccCardsStatusDone[slotID] = false;
            this.mGetBalongSimSlotDone[slotID] = false;
            this.mCardTypes[slotID] = -1;
            this.mIccIds[slotID] = null;
            this.mFullIccIds[slotID] = null;
            this.mAllCardsReady = false;
            this.mNvRestartRildDone = false;
            if (IS_CT_4GSWITCH_DISABLE) {
                saveIccidBySlot(slotID, "");
            }
            if (IS_HISI_DSDX) {
                logd("set mAutoSwitchDualCardsSlotDone to false");
                this.mAutoSwitchDualCardsSlotDone = false;
                if (IS_CMCC_4GSWITCH_DISABLE && IS_VICE_WCDMA) {
                    Phone phone = PhoneFactory.getPhone(slotID);
                    if (phone != null) {
                        logd("disposeCardStatus: set network mode to NETWORK_MODE_GSM_ONLY");
                        phone.setPreferredNetworkType(1, null);
                    }
                }
            }
        }
    }

    public boolean isBalongSimSynced() {
        return getUserSwitchDualCardSlots() == this.mBalongSimSlot;
    }

    public int getUserSwitchDualCardSlots() {
        int subscription = 0;
        try {
            return System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            Rlog.e(this.TAG, "Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            return subscription;
        }
    }

    public boolean judgeDefaultSlotId4Hisi() {
        int temSub = getUserSwitchDualCardSlots();
        boolean getAllUiccCards = (this.mUiccController == null || this.mUiccController.getUiccCards() == null) ? true : this.mUiccController.getUiccCards().length < 2;
        if (getAllUiccCards) {
            Rlog.d(this.TAG, "haven't get all UiccCards done, please wait!");
            return false;
        }
        boolean isCard1Present;
        boolean isCard2Present;
        for (UiccCard uc : this.mUiccController.getUiccCards()) {
            if (uc == null) {
                Rlog.d(this.TAG, "haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        UiccCard[] mUiccCards = this.mUiccController.getUiccCards();
        if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
            isCard1Present = this.mSwitchTypes[0] != 0 || mUiccCards[0].getCardState() == CardState.CARDSTATE_PRESENT;
            isCard2Present = this.mSwitchTypes[1] != 0 || mUiccCards[1].getCardState() == CardState.CARDSTATE_PRESENT;
        } else {
            isCard1Present = mUiccCards[0].getCardState() == CardState.CARDSTATE_PRESENT;
            isCard2Present = mUiccCards[1].getCardState() == CardState.CARDSTATE_PRESENT;
        }
        int onlyCard1Present = isCard1Present ? isCard2Present ^ 1 : 0;
        boolean onlyCard2Present = !isCard1Present ? isCard2Present : false;
        if (IS_FAST_SWITCH_SIMSLOT) {
            if (onlyCard1Present != 0) {
                temSub = 0;
            } else if (onlyCard2Present) {
                temSub = 1;
            }
        } else if (onlyCard1Present != 0) {
            temSub = isBalongSimSynced() ? 0 : 1;
        } else if (onlyCard2Present) {
            temSub = isBalongSimSynced() ? 1 : 0;
        }
        this.default4GSlot = temSub;
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", default4GSlot " + this.default4GSlot);
        return true;
    }

    public boolean isSetDualCardSlotComplete() {
        return this.mSetSdcsCompleteMsg == null;
    }

    public boolean judgeDefaultSlotId4HisiCmcc(boolean forceSwitch) {
        logd("judgeDefaultSlotId4HisiCmcc start");
        int temSub = getUserSwitchDualCardSlots();
        if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < 2) {
            Rlog.d(this.TAG, "haven't get all UiccCards done, please wait!");
            return false;
        }
        int i;
        for (UiccCard uc : this.mUiccController.getUiccCards()) {
            if (uc == null) {
                Rlog.d(this.TAG, "haven't get all UiccCards done, pls wait!");
                return false;
            }
        }
        for (i = 0; i < SIM_NUM; i++) {
            if (this.mIccIds[i] == null) {
                logd("mIccIds[" + i + "] is null, and return");
                return false;
            }
        }
        UiccCard[] mUiccCards = this.mUiccController.getUiccCards();
        boolean isCard1Present = mUiccCards[0].getCardState() == CardState.CARDSTATE_PRESENT;
        boolean isCard2Present = mUiccCards[1].getCardState() == CardState.CARDSTATE_PRESENT;
        boolean isAnySimCardChanged = (anySimCardChanged() || this.isPreBootCompleted || forceSwitch) ? true : this.isHotPlugCompleted;
        if (this.isPreBootCompleted) {
            logd("judgeDefaultSlotId4HisiCmcc: reset isPreBootCompleted.");
            this.isPreBootCompleted = false;
        }
        if (this.isHotPlugCompleted) {
            logd("judgeDefaultSlotId4HisiCmcc: reset isHotPlugCompleted.");
            this.isHotPlugCompleted = false;
        }
        logd("judgeDefaultSlotId4HisiCmcc isAnySimCardChanged = " + isAnySimCardChanged);
        if (isCard1Present && (isCard2Present ^ 1) != 0) {
            temSub = IS_FAST_SWITCH_SIMSLOT ? 0 : isBalongSimSynced() ? 0 : 1;
        } else if (!isCard1Present && isCard2Present) {
            temSub = IS_FAST_SWITCH_SIMSLOT ? 1 : isBalongSimSynced() ? 1 : 0;
        } else if (isCard1Present && isCard2Present) {
            if (isAnySimCardChanged || !IS_HISI_DSDX) {
                boolean[] isCmccCards = new boolean[SIM_NUM];
                int cardtype1 = (this.mCardTypes[0] & 240) >> 4;
                int cardtype2 = (this.mCardTypes[1] & 240) >> 4;
                for (i = 0; i < SIM_NUM; i++) {
                    isCmccCards[i] = isCMCCCard(this.mIccIds[i]);
                }
                if (isCmccCards[0] && isCmccCards[1]) {
                    if (cardtype1 == 2 && cardtype2 == 1) {
                        temSub = isBalongSimSynced() ? 0 : 1;
                    } else if (cardtype1 == 1 && cardtype2 == 2) {
                        temSub = isBalongSimSynced() ? 1 : 0;
                    }
                } else if (isCmccCards[0]) {
                    temSub = IS_FAST_SWITCH_SIMSLOT ? 0 : isBalongSimSynced() ? 0 : 1;
                } else if (isCmccCards[1]) {
                    temSub = IS_FAST_SWITCH_SIMSLOT ? 1 : isBalongSimSynced() ? 1 : 0;
                }
                logd("cardtype1 = " + cardtype1 + ", cardtype2 = " + cardtype2 + ", isCmccCards[SUB1] " + isCmccCards[0] + ", isCmccCards[SUB2] " + isCmccCards[1]);
            } else {
                logd("judgeDefaultSlotId4HisiCmcc all sim present but none sim change ");
                return false;
            }
        }
        this.default4GSlot = temSub;
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", default4GSlot " + this.default4GSlot);
        return true;
    }

    public void initUiccCard(UiccCard uiccCard, IccCardStatus status, Integer index) {
        if (mHotPlugController != null) {
            mHotPlugController.initHotPlugCardState(uiccCard, status, index);
        }
    }

    public void updateUiccCard(UiccCard uiccCard, IccCardStatus status, Integer index) {
        if (mHotPlugController != null) {
            mHotPlugController.updateHotPlugCardState(uiccCard, status, index);
        }
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mIccChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
    }

    public int getSpecCardType(int slotId) {
        if (slotId < 0 || slotId >= SIM_NUM) {
            return -1;
        }
        return this.mCardTypes[slotId];
    }

    public boolean isFullNetwork() {
        logd("isFullNetwork " + IS_FULL_NETWORK_SUPPORTED_IN_HISI);
        return IS_FULL_NETWORK_SUPPORTED_IN_HISI;
    }

    private void logd(String message) {
        Rlog.d(this.TAG, message);
    }

    private void loge(String message) {
        Rlog.e(this.TAG, message);
    }

    private void setDsdsCfgDone(boolean isDone) {
        if (!HwDsdsController.IS_DSDSPOWER_SUPPORT) {
            return;
        }
        if (HwVSimUtils.isVSimInProcess()) {
            logd("setDsdsCfgDone, vsim in process, do nothing");
        } else {
            HwDsdsController.getInstance().setDsdsCfgDone(isDone);
        }
    }

    private int getExpectedMaxCapabilitySubId(int ddsSubId) {
        int expectedMaxCapSubId = -1;
        int cdmaCardNums = 0;
        int cdmaSubId = -1;
        if (IS_QCRIL_CROSS_MAPPING) {
            int CurrentMaxCapabilitySubId = SystemProperties.getInt(PROP_MAIN_STACK, 0);
            ProxyController.getInstance().syncRadioCapability(CurrentMaxCapabilitySubId);
            for (int i = 0; i < SIM_NUM; i++) {
                if (this.subCarrierTypeArray[i].isCCard()) {
                    cdmaSubId = i;
                    cdmaCardNums++;
                }
            }
            if (1 == cdmaCardNums && CurrentMaxCapabilitySubId != cdmaSubId) {
                expectedMaxCapSubId = cdmaSubId;
            } else if (2 == cdmaCardNums && CurrentMaxCapabilitySubId != ddsSubId) {
                expectedMaxCapSubId = ddsSubId;
            }
            logd("[getExpectedMaxCapabilitySubId] cdmaCardNums=" + cdmaCardNums + " expectedMaxCapSubId=" + expectedMaxCapSubId + " CurrentMaxCapabilitySubId=" + CurrentMaxCapabilitySubId);
        }
        return expectedMaxCapSubId;
    }

    private void setMaxRadioCapability(int ddsSubId) {
        ProxyController proxyController = ProxyController.getInstance();
        Phone[] phones = null;
        try {
            phones = PhoneFactory.getPhones();
        } catch (Exception ex) {
            logd("getPhones exception:" + ex.getMessage());
        }
        if (SubscriptionManager.isValidSubscriptionId(ddsSubId) && phones != null) {
            RadioAccessFamily[] rafs = new RadioAccessFamily[phones.length];
            boolean atLeastOneMatch = false;
            for (int phoneId = 0; phoneId < phones.length; phoneId++) {
                int raf;
                int id = phones[phoneId].getSubId();
                if (id == ddsSubId) {
                    raf = proxyController.getMaxRafSupported();
                    atLeastOneMatch = true;
                } else {
                    raf = proxyController.getMinRafSupported();
                }
                logd("[setMaxRadioCapability] phoneId=" + phoneId + " subId=" + id + " raf=" + raf);
                rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
            }
            if (atLeastOneMatch) {
                proxyController.setRadioCapability(rafs);
                startSetPrefNetworkTimer();
                return;
            }
            logd("[setMaxRadioCapability] no valid subId's found - not updating.");
        }
    }

    private void sendSetRadioCapabilitySuccess(boolean needChangeNetworkTypeInDB) {
        logd("sendSetRadioCapabilitySuccess,needChangeNetworkTypeInDB:" + needChangeNetworkTypeInDB);
        Message response = obtainMessage(10, Integer.valueOf(this.expectedDDSsubId));
        AsyncResult.forMessage(response, null, null);
        response.sendToTarget();
        sendHwSwitchSlotDoneBroadcast(this.expectedDDSsubId);
        for (int slotId = 0; slotId < SIM_NUM; slotId++) {
            if (-1 != this.mSetUiccSubscriptionResult[slotId]) {
                boolean active = this.mSetUiccSubscriptionResult[slotId] == 1;
                logd("sendSetRadioCapabilitySuccess,setSubscription: slotId = " + slotId + ", activate = " + active);
                HwSubscriptionManager.getInstance().setSubscription(slotId, active, null);
            }
        }
        if (needChangeNetworkTypeInDB) {
            exchangeNetworkTypeInDB();
        }
        setLteServiceAbility();
    }

    private void setLteServiceAbility() {
        HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();
        if (mHwTelephonyManager != null && this.mNeedSetLteServiceAbility) {
            int ability = mHwTelephonyManager.getLteServiceAbility();
            logd("setLteServiceAbility:" + ability);
            mHwTelephonyManager.setLteServiceAbility(ability);
            this.mNeedSetLteServiceAbility = false;
        }
    }

    private void syncNetworkTypeFromDB() {
        logd("in syncNetworkTypeFromDB");
        int pefMode0 = getNetworkTypeFromDB(0);
        int pefMode1 = getNetworkTypeFromDB(1);
        boolean firstStart = Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0 ? Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 1) == 0 : false;
        logd("pefMode0 = " + pefMode0 + ",pefMode1 = " + pefMode1 + ",firstStart =" + firstStart);
        if (pefMode0 == -1 || pefMode1 == -1 || firstStart) {
            setLteServiceAbilityForQCOM(1, SystemProperties.getInt("ro.telephony.default_network", -1));
        }
    }

    public void setLteServiceAbilityForQCOM(int ability, int lteOnMappingMode) {
        logd("in setLteServiceAbilityForQCOM");
        getStackPhoneId();
        recordPrimaryAndSecondaryStackNetworkType(ability, lteOnMappingMode);
        this.mCis[this.mPrimaryStackPhoneId].getPreferredNetworkType(obtainMessage(EVENT_GET_PREF_NETWORK_MODE_DONE, this.mPrimaryStackPhoneId, 0));
    }

    private void getStackPhoneId() {
        int i = 0;
        this.mPrimaryStackPhoneId = SystemProperties.getInt(PROP_MAIN_STACK, 0);
        if (this.mPrimaryStackPhoneId == 0) {
            i = 1;
        }
        this.mSecondaryStackPhoneId = i;
        logd("getStackPhoneId mPrimaryStackPhoneId:" + this.mPrimaryStackPhoneId + ",mSecondaryStackPhoneId:" + this.mSecondaryStackPhoneId);
    }

    private void recordPrimaryAndSecondaryStackNetworkType(int ability, int lteOnMappingMode) {
        int primaryLteOnNetworkType;
        int primaryLteOffNetworkType;
        int i = 24;
        switch (lteOnMappingMode) {
            case 9:
                primaryLteOnNetworkType = 9;
                primaryLteOffNetworkType = 3;
                break;
            case 10:
                primaryLteOnNetworkType = 10;
                primaryLteOffNetworkType = 7;
                break;
            case 20:
                primaryLteOnNetworkType = 20;
                primaryLteOffNetworkType = 18;
                break;
            case 22:
                primaryLteOnNetworkType = 22;
                primaryLteOffNetworkType = 21;
                break;
            default:
                primaryLteOnNetworkType = 22;
                primaryLteOffNetworkType = 21;
                break;
        }
        if (ability == 1) {
            this.mPrimaryStackNetworkType = primaryLteOnNetworkType;
            this.mSecondaryStackNetworkType = 20;
            if (lteOnMappingMode == 9 || lteOnMappingMode == 10) {
                this.mSecondaryStackNetworkType = 9;
            }
        } else {
            this.mPrimaryStackNetworkType = primaryLteOffNetworkType;
            this.mSecondaryStackNetworkType = 18;
            if (lteOnMappingMode == 9 || lteOnMappingMode == 10) {
                this.mSecondaryStackNetworkType = 3;
            }
        }
        int isCmccHybird = (IS_CMCC_4GSWITCH_DISABLE && isCmccHybirdBySubCarrierType()) ? TelephonyManager.getDefault().isNetworkRoaming(getCMCCCardSlotId()) ^ 1 : 0;
        if (isCmccHybird != 0) {
            if (this.mPrimaryStackPhoneId == this.default4GSlot) {
                if (ability == 1) {
                    i = 25;
                }
                this.mSecondaryStackNetworkType = i;
            } else {
                if (ability == 1) {
                    i = 25;
                }
                this.mPrimaryStackNetworkType = i;
            }
            int otherSub = this.default4GSlot == 0 ? 1 : 0;
            PhoneFactory.getPhone(otherSub).setOOSFlagOnSelectNetworkManually(true);
            if (hasMessages(EVENT_RESET_OOS_FLAG)) {
                removeMessages(EVENT_RESET_OOS_FLAG);
            }
            Message msg = obtainMessage(EVENT_RESET_OOS_FLAG, Integer.valueOf(otherSub));
            AsyncResult.forMessage(msg, null, null);
            sendMessageDelayed(msg, 20000);
        }
        logd("recordPrimaryAndSecondaryStackNetworkType mPrimaryStackNetworkType:" + this.mPrimaryStackNetworkType + ",mSecondaryStackNetworkType:" + this.mSecondaryStackNetworkType);
    }

    private boolean isCmccHybirdBySubCarrierType() {
        return this.subCarrierTypeArray[0].isCMCCCard() != this.subCarrierTypeArray[1].isCMCCCard();
    }

    private void handleSetPrimaryStackLteSwitchDone(Message msg) {
        logd("in handleSetPrimaryStackLteSwitchDone");
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            loge("set prefer network mode failed!");
            sendLteServiceSwitchResult(false);
            return;
        }
        this.mSetPrimaryStackPrefMode = msg.arg1;
        logd("setPrimaryStackPrefMode = " + this.mSetPrimaryStackPrefMode);
        this.mCis[this.mSecondaryStackPhoneId].getPreferredNetworkType(obtainMessage(EVENT_GET_PREF_NETWORK_MODE_DONE, this.mSecondaryStackPhoneId, 0));
    }

    private void sendLteServiceSwitchResult(boolean result) {
        logd("LTE service Switch result is " + result + ". broadcast PREFERRED_4G_SWITCH_DONE");
        if (this.mContext == null) {
            loge("mContext is null. return!");
            return;
        }
        Intent intent = new Intent("com.huawei.telephony.PREF_4G_SWITCH_DONE");
        intent.putExtra("setting_result", result);
        this.mContext.sendBroadcast(intent);
    }

    private void handleSetSecondaryStackLteSwitchDone(Message msg) {
        logd("in handleSetSecondaryStackLteSwitchDone");
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            loge(" set prefer network mode failed!");
            sendLteServiceSwitchResult(false);
            rollbackPrimaryStackPrefNetworkType();
            return;
        }
        this.mSetSecondaryStackPrefMode = msg.arg1;
        logd(" setSecondaryStackPrefMode = " + this.mSetSecondaryStackPrefMode);
        saveNetworkTypeToDB();
        logd("set prefer network mode success!");
        sendLteServiceSwitchResult(true);
    }

    private void rollbackPrimaryStackPrefNetworkType() {
        logd("in rollbackPrimaryStackPrefNetworkType");
        int curPrefMode = -1;
        try {
            curPrefMode = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", this.mPrimaryStackPhoneId);
        } catch (Exception e) {
            loge("rollbackPrimaryStackPrefNetworkType PREFERRED_NETWORK_MODE Exception = " + e);
        }
        logd("curPrefMode = " + curPrefMode + "mSetPrimaryStackPrefMode =" + this.mSetPrimaryStackPrefMode);
        if (curPrefMode != this.mSetPrimaryStackPrefMode) {
            this.mCis[this.mPrimaryStackPhoneId].setPreferredNetworkType(curPrefMode, obtainMessage(EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE, curPrefMode, 0, Integer.valueOf(this.mPrimaryStackPhoneId)));
        }
    }

    private void handleRollbackDone(Message msg) {
        logd("in rollbackDone");
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            loge("set prefer network mode failed!");
        }
    }

    private void saveNetworkTypeToDB() {
        logd("in saveNetworkTypeToDB");
        int curPrimaryStackPrefMode = getNetworkTypeFromDB(this.mPrimaryStackPhoneId);
        logd("curPrimaryStackPrefMode = " + curPrimaryStackPrefMode + "mSetPrimaryStackPrefMode =" + this.mSetPrimaryStackPrefMode);
        if (curPrimaryStackPrefMode != this.mSetPrimaryStackPrefMode) {
            setNetworkTypeToDB(this.mPrimaryStackPhoneId, this.mSetPrimaryStackPrefMode);
        }
        int curSecondaryStackPrefMode = getNetworkTypeFromDB(this.mSecondaryStackPhoneId);
        logd("curSecondaryStackPrefMode = " + curSecondaryStackPrefMode + "mSetSecondaryStackPrefMode =" + this.mSetSecondaryStackPrefMode);
        if (curSecondaryStackPrefMode != this.mSetSecondaryStackPrefMode) {
            setNetworkTypeToDB(this.mSecondaryStackPhoneId, this.mSetSecondaryStackPrefMode);
        }
    }

    private int getNetworkTypeFromDB(int phoneId) {
        try {
            if (HwModemCapability.isCapabilitySupport(9)) {
                return TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", phoneId);
            }
            if (IS_DUAL_4G_SUPPORTED && SIM_NUM > 1) {
                return Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode" + phoneId, -1);
            }
            if (phoneId == getUserSwitchDualCardSlots() || 1 == SIM_NUM) {
                return Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", -1);
            }
            return -1;
        } catch (Exception e) {
            loge("getNetworkTypeFromDB Exception = " + e + ",phoneId");
            return -1;
        }
    }

    /* JADX WARNING: Missing block: B:18:0x00aa, code:
            if (1 == SIM_NUM) goto L_0x00ac;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setNetworkTypeToDB(int phoneId, int prefMode) {
        try {
            int mainCardIndex = getUserSwitchDualCardSlots();
            if (HwModemCapability.isCapabilitySupport(9)) {
                TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", phoneId, prefMode);
            } else if (!IS_DUAL_4G_SUPPORTED || SIM_NUM <= 1) {
                if (phoneId != mainCardIndex) {
                }
                Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode", prefMode);
            } else {
                Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode" + phoneId, prefMode);
                if (phoneId == mainCardIndex) {
                    Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode", prefMode);
                }
            }
            logd("setNetworkTypeToDB id = " + phoneId + ", mode = " + prefMode + " to database success!");
        } catch (Exception e) {
            loge("setNetworkTypeToDB Exception = " + e + ",phoneId:" + phoneId + ",prefMode:" + prefMode);
        }
    }

    private void exchangeNetworkTypeInDB() {
        int previousNetworkTypeSub0 = getNetworkTypeFromDB(0);
        int previousNetworkTypeSub1 = getNetworkTypeFromDB(1);
        logd("exchangeNetworkTypeInDB PREFERRED_NETWORK_MODE:" + previousNetworkTypeSub0 + "," + previousNetworkTypeSub1 + "->" + previousNetworkTypeSub1 + "," + previousNetworkTypeSub0);
        setNetworkTypeToDB(0, previousNetworkTypeSub1);
        setNetworkTypeToDB(1, previousNetworkTypeSub0);
    }

    private void handleGetPrefNetworkModeDone(Message msg) {
        int subId = msg.arg1;
        int modemNetworkMode = -1;
        AsyncResult ar = msg.obj;
        if (ar.exception == null) {
            modemNetworkMode = ((int[]) ar.result)[0];
        }
        logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
        Message response;
        if (this.mPrimaryStackPhoneId == subId) {
            response = obtainMessage(EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE, this.mPrimaryStackNetworkType, 0, Integer.valueOf(this.mPrimaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mPrimaryStackNetworkType) {
                this.mCis[this.mPrimaryStackPhoneId].setPreferredNetworkType(this.mPrimaryStackNetworkType, response);
                return;
            }
            AsyncResult.forMessage(response);
            response.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's ,don't set again");
        } else if (this.mSecondaryStackPhoneId == subId) {
            response = obtainMessage(EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE, this.mSecondaryStackNetworkType, 0, Integer.valueOf(this.mSecondaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mSecondaryStackNetworkType) {
                this.mCis[this.mSecondaryStackPhoneId].setPreferredNetworkType(this.mSecondaryStackNetworkType, response);
                return;
            }
            AsyncResult.forMessage(response);
            response.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's ,don't set again");
        }
    }

    private void onSimHotPlug(AsyncResult ar, Integer index) {
        logd("onSimHotPlug");
        if (ar != null && ar.result != null && ((int[]) ar.result).length > 0) {
            if (HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[0]) {
                this.isHotPlugCompleted = true;
                disposeCardStatus(index.intValue());
            } else if (HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.result)[0]) {
                HwVSimUtils.simHotPlugOut(index.intValue());
            }
        }
    }

    public void fastSwitchDualCardsSlot(int expectedMainSlotId, Message onCompleteMsg) {
        logd("fastSwitchDualCardsSlot: expectedMainSlot=" + expectedMainSlotId);
        if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate() || (HwVSimUtils.isAllowALSwitch() ^ 1) != 0) {
            logd("vsim on sub");
            setWaitingSwitchBalongSlot(false);
            if (onCompleteMsg != null) {
                AsyncResult.forMessage(onCompleteMsg, Boolean.valueOf(false), null);
                loge("Switch Dual Card Slots failed!! Sending the cnf back!");
                onCompleteMsg.sendToTarget();
            }
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, 2);
            this.mSet4GSlotCompleteMsg = null;
            return;
        }
        SubscriptionController.getInstance().setDataSubId(expectedMainSlotId);
        logd("fastSwitchDualCardsSlot:setDefaultDataSubId=" + expectedMainSlotId + ",only set database to expectedMainSlotId");
        this.mSetSdcsCompleteMsg = onCompleteMsg;
        ProxyController proxyController = ProxyController.getInstance();
        int cdmaSimSlotId = getCdmaSimCardSlotId(expectedMainSlotId);
        if (isNeedSetRadioCapability(expectedMainSlotId, cdmaSimSlotId)) {
            if (!proxyController.setRadioCapability(expectedMainSlotId, cdmaSimSlotId)) {
                logd("fastSwitchDualCardsSlot: setRadioCapability fail ,response GENERIC_FAILURE");
                sendResponseToTarget(obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_DONE, Integer.valueOf(expectedMainSlotId)), 2);
            }
            startFastSwithSIMSlotTimer();
            return;
        }
        logd("fastSwitchDualCardsSlot: don't need SetRadioCapability,response SUCCESS");
        sendResponseToTarget(obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_DONE, Integer.valueOf(expectedMainSlotId)), 0);
    }

    private boolean isNeedSetRadioCapability(int expectedMainSlotId, int cdmaSimSlotId) {
        Phone[] mPhones = PhoneFactory.getPhones();
        if (mPhones == null) {
            logd("isNeedSetRadioCapability: mPhones is null");
            return false;
        }
        boolean same = true;
        if (SubscriptionManager.isValidSlotIndex(expectedMainSlotId) && mPhones[expectedMainSlotId] != null) {
            RadioCapability expectedMainSlotRC = mPhones[expectedMainSlotId].getRadioCapability();
            if (expectedMainSlotRC == null || ("0".equals(expectedMainSlotRC.getLogicalModemUuid()) ^ 1) == 0) {
                logd("isNeedSetRadioCapability: expectedMainSlotId equals with LogicalModemUuid");
            } else {
                logd("isNeedSetRadioCapability: need switch LogicalModemUuid for expectedMainSlotId");
                same = false;
            }
        }
        if (SubscriptionManager.isValidSlotIndex(cdmaSimSlotId) && mPhones[cdmaSimSlotId] != null) {
            RadioCapability cdmaSimSlotRC = mPhones[cdmaSimSlotId].getRadioCapability();
            int cdmaSimSlotRaf = 1;
            if (cdmaSimSlotRC != null) {
                cdmaSimSlotRaf = cdmaSimSlotRC.getRadioAccessFamily();
            }
            if (64 != (cdmaSimSlotRaf & 64)) {
                logd("isNeedSetRadioCapability: need add RAF_1xRTT for cdmaSimSlotRaf");
                same = false;
            } else {
                logd("isNeedSetRadioCapability: cdmaSimSlotRaf has RAF_1xRTT");
            }
        }
        if (same) {
            logd("isNeedSetRadioCapability: Already in requested configuration, nothing to do.");
            return false;
        }
        this.mNvRestartRildDone = true;
        return true;
    }

    private int getCdmaSimCardSlotId(int expectedMainSlotId) {
        HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();
        if (mHwTelephonyManager.isCDMASimCard(0) && mHwTelephonyManager.isCDMASimCard(1)) {
            return expectedMainSlotId;
        }
        if (mHwTelephonyManager.isCDMASimCard(0)) {
            return 0;
        }
        if (mHwTelephonyManager.isCDMASimCard(1)) {
            return 1;
        }
        return -1;
    }

    private void startFastSwithSIMSlotTimer() {
        Message message = obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT);
        AsyncResult.forMessage(message, null, null);
        sendMessageDelayed(message, 60000);
        logd("startFastSwithSIMSlotTimer");
    }

    private void onGetCdmaModeSideDone(AsyncResult ar, Integer index) {
        int mCdmaModemSide = 0;
        CommrilMode currentCommrilModem = CommrilMode.NON_MODE;
        if (!(ar == null || ar.exception != null || ar.result == null)) {
            mCdmaModemSide = ((int[]) ar.result)[0];
        }
        if (mCdmaModemSide == 0) {
            currentCommrilModem = CommrilMode.HISI_CGUL_MODE;
        } else if (1 == mCdmaModemSide) {
            currentCommrilModem = CommrilMode.HISI_CG_MODE;
        } else if (2 == mCdmaModemSide) {
            currentCommrilModem = CommrilMode.HISI_VSIM_MODE;
        }
        SystemProperties.set(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, currentCommrilModem.toString());
        logd("onGetCdmaModeSideDone mCdmaModemSide = " + mCdmaModemSide + " set currentCommrilModem=" + currentCommrilModem);
    }

    public boolean isSet4GDoneAfterSimInsert() {
        return this.mAutoSwitchDualCardsSlotDone;
    }

    public int getUserPref4GSlot() {
        return this.prefer4GSlot;
    }

    public boolean isUserPref4GSlot(int slotId) {
        return this.prefer4GSlot == slotId;
    }

    private void processSingleSimStateChanged(String simState, int slotId) {
        if (!isValidIndex(slotId)) {
            loge("processSingleSimStateChanged: invalid slotId " + slotId + ", return");
        } else if ("IMSI".equals(simState)) {
            setSingleCardPrefNetwork(slotId);
        } else {
            logd("processSingleSimStateChanged: simState is " + simState + ", return");
        }
    }

    private void setSingleCardPrefNetwork(int slotId) {
        int ability = HwFrameworkFactory.getHwInnerTelephonyManager().getLteServiceAbility();
        int prefNetwork = DEFAULT_NETWORK_MODE;
        if (isCDMASimCard(slotId)) {
            if (1 == ability) {
                prefNetwork = 10;
            } else {
                prefNetwork = 7;
            }
        } else if (1 == ability) {
            prefNetwork = 20;
        } else {
            prefNetwork = 18;
        }
        logd("setSingleCardPrefNetwork, LTE ability = " + ability + ", pref network = " + prefNetwork);
        if (this.mCis[slotId] != null) {
            setNetworkTypeToDB(slotId, prefNetwork);
            this.retryChangeCount = 0;
            this.mCis[slotId].setPreferredNetworkType(prefNetwork, obtainMessage(13, slotId, prefNetwork));
            return;
        }
        loge("mCis[" + slotId + "] is null!");
    }

    private boolean isCDMASimCard(int slotId) {
        HwTelephonyManagerInner hwTelephonyManager = HwTelephonyManagerInner.getDefault();
        return hwTelephonyManager != null ? hwTelephonyManager.isCDMASimCard(slotId) : false;
    }

    private boolean judgeDefalt4GSlotForMDM() {
        boolean isSub0Active = SubscriptionController.getInstance().getSubState(0) == 1;
        logd("isSub0Active = " + isSub0Active);
        if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data") && isCardPresent(0) && isCardPresent(1) && isSub0Active) {
            this.default4GSlot = 0;
            return true;
        } else if (!HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub") || !isCardPresent(0)) {
            return false;
        } else {
            this.default4GSlot = 0;
            return true;
        }
    }

    public void setDefault4GSlotForMDM() {
        int preDefault4GSlot = this.default4GSlot;
        try {
            preDefault4GSlot = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
        }
        boolean updateDefault4GSlot = judgeDefalt4GSlotForMDM();
        logd("setDefault4GSlotForMDM  default4GSlot= " + this.default4GSlot + "  preDefault4GSlot= " + preDefault4GSlot + "  getWaitingSwitchBalongSlot: " + getWaitingSwitchBalongSlot());
        if (preDefault4GSlot != this.default4GSlot && updateDefault4GSlot && (getWaitingSwitchBalongSlot() ^ 1) != 0) {
            setDefault4GSlot(this.default4GSlot);
        }
    }

    private void revertDefaultDataSubId(int expectedMainSlotId) {
        int slaveSubId = expectedMainSlotId == 0 ? 1 : 0;
        SubscriptionController.getInstance().setDataSubId(slaveSubId);
        logd("revertDefaultDataSubId,setDefaultDataSubId=" + slaveSubId + ",only set database to original");
    }

    private boolean isDualImsSwitchOpened() {
        return 1 == SystemProperties.getInt("persist.radio.dualltecap", 0);
    }

    public void resetUiccSubscriptionResultFlag(int slotId) {
        if (slotId >= 0 && slotId < SIM_NUM) {
            logd("UiccSubscriptionResult:  slotId=" + slotId + "PreResult:" + this.mSetUiccSubscriptionResult[slotId]);
            this.mSetUiccSubscriptionResult[slotId] = -1;
        }
    }

    private boolean isSwitchSlotEnabledForCMCC() {
        boolean isCmccHybird = !isCMCCHybird() ? isCmccHybirdBySubCarrierType() : true;
        boolean isCmccSlotIdRoaming = TelephonyManager.getDefault().isNetworkRoaming(getCMCCCardSlotId());
        if (IS_CMCC_4GSWITCH_DISABLE && isCmccHybird && (isCmccSlotIdRoaming ^ 1) != 0) {
            return false;
        }
        return true;
    }

    private int getCMCCCardSlotId() {
        int i;
        if (HwModemCapability.isCapabilitySupport(9)) {
            if (isCmccHybirdBySubCarrierType()) {
                for (i = 0; i < SIM_NUM; i++) {
                    if (this.subCarrierTypeArray[i].isCMCCCard()) {
                        return i;
                    }
                }
            }
        } else if (isCMCCHybird()) {
            for (i = 0; i < SIM_NUM; i++) {
                if (isCMCCCard(this.mIccIds[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void forceSetDefault4GSlotForCMCC(int cmccSlotId) {
        logd("forceSetDefault4GSlotForCMCC cmccSlotId:" + cmccSlotId);
        if (HwModemCapability.isCapabilitySupport(9)) {
            if (cmccSlotId != getUserSwitchDualCardSlots() && (this.isSet4GSlotInProgress ^ 1) != 0) {
                this.default4GSlot = cmccSlotId;
                setDefault4GSlot(this.default4GSlot);
            }
        } else if (judgeDefaultSlotId4HisiCmcc(true) && this.default4GSlot != getUserSwitchDualCardSlots() && (this.isSet4GSlotInProgress ^ 1) != 0) {
            setDefault4GSlot(this.default4GSlot);
        }
    }

    private boolean needForceSetDefaultSlot(boolean roaming, int cmccSlotId) {
        return (roaming || getUserSwitchDualCardSlots() == cmccSlotId) ? false : true;
    }

    private void onServiceStateChangedForCMCC(Intent intent) {
        int cmccSlotId = getCMCCCardSlotId();
        if (IS_CMCC_4GSWITCH_DISABLE && cmccSlotId != -1) {
            int slotId = intent.getIntExtra("subscription", -1);
            ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
            if (slotId == cmccSlotId && serviceState.getState() == 0) {
                boolean newRoamingState = TelephonyManager.getDefault().isNetworkRoaming(cmccSlotId);
                boolean oldRoamingState = getLastRoamingStateFromSP();
                logd("mPhoneStateListener cmcccSlotId = " + cmccSlotId + " oldRoamingState=" + oldRoamingState + " newRoamingState=" + newRoamingState);
                if (oldRoamingState != newRoamingState) {
                    saveLastRoamingStateToSP(newRoamingState);
                    if (needForceSetDefaultSlot(newRoamingState, cmccSlotId)) {
                        forceSetDefault4GSlotForCMCC(cmccSlotId);
                        return;
                    } else if (HwModemCapability.isCapabilitySupport(9)) {
                        this.mNeedSetLteServiceAbility = true;
                        setLteServiceAbility();
                    } else {
                        setPrefNwForCmcc();
                    }
                }
            }
            if (slotId == cmccSlotId) {
                if (this.mCmccSubIdOldState != 0 && serviceState.getState() == 0) {
                    logd("OUT_OF_SERVICE -> IN_SERVICE, setPrefNW");
                    if (HwModemCapability.isCapabilitySupport(9)) {
                        this.mNeedSetLteServiceAbility = true;
                        setLteServiceAbility();
                    } else {
                        setPrefNwForCmcc();
                    }
                }
                this.mCmccSubIdOldState = serviceState.getState();
            }
        }
    }

    private void saveLastRoamingStateToSP(boolean roamingState) {
        logd("saveRoamingState " + roamingState);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putBoolean(ROAMINGSTATE_PREF, roamingState);
        editor.apply();
    }

    private boolean getLastRoamingStateFromSP() {
        return PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(ROAMINGSTATE_PREF, false);
    }

    private void sendHwSwitchSlotStartBroadcast() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HW_SWITCH_SLOT_STEP, 0);
        this.mContext.sendBroadcast(intent);
    }

    private void sendHwSwitchSlotDoneBroadcast(int mainSlotId) {
        int i = 1;
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HW_SWITCH_SLOT_STEP, 1);
        int oldSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        setUserSwitchDualCardSlots(mainSlotId);
        String str = IF_NEED_SET_RADIO_CAP;
        if (oldSlotId != mainSlotId) {
            i = 0;
        }
        intent.putExtra(str, i);
        this.mContext.sendBroadcast(intent);
    }

    private void sendHwSwitchSlotFailedBroadcast() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        intent.putExtra(HW_SWITCH_SLOT_STEP, -1);
        this.mContext.sendBroadcast(intent);
    }
}
