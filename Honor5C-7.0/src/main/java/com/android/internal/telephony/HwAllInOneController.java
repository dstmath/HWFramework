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
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HwAllInOneController extends Handler {
    private static final /* synthetic */ int[] -com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues = null;
    private static final byte[] C2 = null;
    public static final int CARD_TYPE_DUAL_MODE = 3;
    public static final int CARD_TYPE_NO_SIM = 0;
    public static final String CARD_TYPE_SIM1 = "gsm.sim1.type";
    public static final String CARD_TYPE_SIM2 = "gsm.sim2.type";
    public static final int CARD_TYPE_SINGLE_CDMA = 2;
    public static final int CARD_TYPE_SINGLE_GSM = 1;
    private static final List<String> CMCC_ICCID_ARRAY = null;
    private static final List<String> CMCC_MCCMNC_ARRAY = null;
    private static final List<String> CT_ICCID_ARRAY = null;
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    private static final List<String> CU_ICCID_ARRAY = null;
    private static final List<String> CU_MCCMNC_ARRAY = null;
    private static final int DEFAULT_NETWORK_MODE = 0;
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_TELECOM_LTE_CARD = 43;
    public static final int DUAL_MODE_UG_CARD = 50;
    private static final int EVENT_CHANGE_PREF_NETWORK_DONE = 13;
    private static final int EVENT_CHECK_ALL_CARDS_READY = 108;
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
    private static final int ICCID_LEN_MINIMUM = 6;
    private static final int INVALID = -1;
    private static final int INVALID_NETWORK_MODE = -1;
    private static final boolean IS_4G_SWITCH_SUPPORTED = false;
    public static final boolean IS_CARD2_CDMA_SUPPORTED = false;
    private static final boolean IS_CHINA_TELECOM = false;
    public static final boolean IS_CMCC_4GSWITCH_DISABLE = false;
    private static final boolean IS_CMCC_4G_DSDX_ENABLE = false;
    private static final boolean IS_CMCC_CU_DSDX_ENABLE = false;
    public static final boolean IS_FAST_SWITCH_SIMSLOT = false;
    private static final boolean IS_FULL_NETWORK_SUPPORTED = false;
    private static final boolean IS_FULL_NETWORK_SUPPORTED_IN_HISI = false;
    public static final boolean IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT = false;
    public static final boolean IS_HISI_DSDX = false;
    public static final boolean IS_QCRIL_CROSS_MAPPING = false;
    private static final boolean IS_SINGLE_CARD_TRAY = false;
    public static final boolean IS_VICE_WCDMA = false;
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    private static final String MAIN_CARD_INDEX = "main_card_id";
    private static final String MASTER_PASSWORD = null;
    private static final int MCCMNC_LEN_MINIMUM = 5;
    private static final int MESSAGE_PENDING_DELAY = 500;
    private static final int MESSAGE_RETRY_PENDING_DELAY = 6000;
    private static final int MODEM0 = 0;
    private static final int MODEM1 = 1;
    private static final int MSG_RETRY_CHANGE_PREF_NETWORK = 2;
    private static final int MSG_RETRY_SET_DEFAULT_LTESLOT = 1;
    private static final String NETWORK_MODE_2G_ONLY = "network_mode_2G_only";
    private static final String NETWORK_MODE_3G_PRE = "network_mode_3G_pre";
    private static final String NETWORK_MODE_4G_PRE = "network_mode_4G_pre";
    private static final int OOS_DELAY_TIME = 20000;
    private static final String PREFIX_LOCAL_ICCID = "8986";
    private static final String PREFIX_LOCAL_MCC = "460";
    private static final String PROPERTY_HW_OPTA_TELECOM = "92";
    private static final String PROPERTY_HW_OPTB_CHINA = "156";
    public static final String PROP_MAIN_STACK = "persist.radio.msim.stackid_0";
    private static final boolean RESET_PROFILE = false;
    private static final int RETRY_CHANGE_MAX_TIME = 20;
    private static final int RETRY_MAX_TIME = 20;
    private static final int REVIEW_4G_MODE_AUTO = 1;
    private static final int REVIEW_4G_MODE_FIX = 2;
    private static final int REVIEW_4G_MODE_NONE = 0;
    private static final int SIM_NUM = 0;
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
    public static final int UNKNOWN_CARD = -1;
    public static final String USER_DEFAULT_SUBSCRIPTION = "user_default_sub";
    private static HwHotplugController mHotPlugController;
    private static HwAllInOneController mInstance;
    private static final Object mLock = null;
    private String TAG;
    private int curSetDataAllowCount;
    private int current4GSlotBackup;
    private Map<Integer, SubType> currentSubTypeMap;
    private int default4GSlot;
    int expectedDDSsubId;
    private int is4GSlotReviewNeeded;
    private boolean isPreBootCompleted;
    private boolean isSet4GSlotInProgress;
    public boolean isSet4GSlotManuallyTriggered;
    private boolean[] isSimInsertedArray;
    private boolean isVoiceCallEndedRegistered;
    private boolean mAllCardsReady;
    private boolean mAutoSwitchDualCardsSlotDone;
    private int mBalongSimSlot;
    private boolean mBroadcastDone;
    private int[] mCardTypes;
    private CommandsInterface[] mCis;
    private boolean mCommrilRestartRild;
    Context mContext;
    private LinkedList<DelayedEvent> mEventsQ;
    private String[] mFullIccIds;
    private boolean[] mGetBalongSimSlotDone;
    private boolean[] mGetUiccCardsStatusDone;
    private boolean mHas3Modem;
    private RegistrantList mIccChangedRegistrants;
    private String[] mIccIds;
    private int[] mModemPreferMode;
    private boolean mNeedSetLteServiceAbility;
    private int mNumOfGetPrefNwModeSuccess;
    private boolean mNvRestartRildDone;
    private int[] mPrefNwMode;
    private int mPrimaryStackNetworkType;
    private int mPrimaryStackPhoneId;
    private boolean[] mRadioOns;
    private BroadcastReceiver mReceiver;
    private Handler mRetryHandler;
    private int mSecondaryStackNetworkType;
    private int mSecondaryStackPhoneId;
    private Message mSet4GSlotCompleteMsg;
    private int mSetPrimaryStackPrefMode;
    private Message mSetSdcsCompleteMsg;
    private int mSetSecondaryStackPrefMode;
    private int[] mSetUiccSubscriptionResult;
    private int[] mSwitchTypes;
    private UiccController mUiccController;
    private int mUserPref4GSlot;
    private boolean needRetrySetPrefNetwork;
    private int needSetDataAllowCount;
    private int[] nwModeArray;
    private int retryChangeCount;
    private int retryCount;
    private SubCarrierType[] subCarrierTypeArray;
    private boolean updateUserDefaultFlag;

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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwAllInOneController.SubCarrierType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwAllInOneController.SubCarrierType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwAllInOneController.SubCarrierType.<clinit>():void");
        }

        boolean isUCard() {
            if (this == CARRIER_CMCC_USIM || this == CARRIER_CMCC_SIM || this == CARRIER_CU_USIM || this == CARRIER_CU_SIM || this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_SIM) {
                return true;
            }
            return HwAllInOneController.RESET_PROFILE;
        }

        boolean isCCard() {
            if (this == CARRIER_CT_CSIM || this == CARRIER_CT_RUIM || this == CARRIER_FOREIGN_CSIM || this == CARRIER_FOREIGN_RUIM) {
                return true;
            }
            return HwAllInOneController.RESET_PROFILE;
        }

        boolean is3G4GCard() {
            if (this == CARRIER_CMCC_USIM || this == CARRIER_CU_USIM || this == CARRIER_CT_CSIM || this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_CSIM) {
                return true;
            }
            return HwAllInOneController.RESET_PROFILE;
        }

        boolean is2GCard() {
            if (this == CARRIER_CMCC_SIM || this == CARRIER_CU_SIM || this == CARRIER_CT_RUIM || this == CARRIER_FOREIGN_SIM || this == CARRIER_FOREIGN_RUIM) {
                return true;
            }
            return HwAllInOneController.RESET_PROFILE;
        }

        boolean isCMCCCard() {
            if (this == CARRIER_CMCC_SIM || this == CARRIER_CMCC_USIM) {
                return true;
            }
            return HwAllInOneController.RESET_PROFILE;
        }

        boolean isCUCard() {
            if (this == CARRIER_CU_USIM || this == CARRIER_CU_SIM) {
                return true;
            }
            return HwAllInOneController.RESET_PROFILE;
        }

        boolean isReCheckFail() {
            if (this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_SIM || this == OTHER) {
                return true;
            }
            return HwAllInOneController.RESET_PROFILE;
        }
    }

    public enum SubType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwAllInOneController.SubType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwAllInOneController.SubType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwAllInOneController.SubType.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues() {
        if (-com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues != null) {
            return -com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues;
        }
        int[] iArr = new int[SubCarrierType.values().length];
        try {
            iArr[SubCarrierType.CARRIER_CMCC_SIM.ordinal()] = SUB_1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CMCC_USIM.ordinal()] = SLOT2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CT_CSIM.ordinal()] = EVENT_RADIO_UNAVAILABLE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CT_RUIM.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CU_SIM.ordinal()] = MCCMNC_LEN_MINIMUM;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SubCarrierType.CARRIER_CU_USIM.ordinal()] = ICCID_LEN_MINIMUM;
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
            iArr[SubCarrierType.CARRIER_FOREIGN_USIM.ordinal()] = SUB_BOTH;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SubCarrierType.OTHER.ordinal()] = EVENT_GET_PREF_NETWORK_DONE;
        } catch (NoSuchFieldError e11) {
        }
        -com-android-internal-telephony-HwAllInOneController$SubCarrierTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwAllInOneController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwAllInOneController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwAllInOneController.<clinit>():void");
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
        if (IS_CMCC_4GSWITCH_DISABLE && judgeDefaultSlotId4HisiCmcc(true) && this.default4GSlot != getUserSwitchDualCardSlots() && !this.isSet4GSlotInProgress) {
            logd("setDefault4GSlotForCMCC: need setDefault4GSlot");
            setDefault4GSlot(this.default4GSlot);
        }
    }

    private Message obtainSetPreNWMessage(int slotId) {
        return obtainMessage(SUB_BOTH, Integer.valueOf(slotId));
    }

    private HwAllInOneController(Context c, CommandsInterface[] ci) {
        int i;
        this.TAG = "HwAllInOneController";
        this.updateUserDefaultFlag = RESET_PROFILE;
        this.mAutoSwitchDualCardsSlotDone = RESET_PROFILE;
        this.mCommrilRestartRild = RESET_PROFILE;
        this.mRadioOns = new boolean[SIM_NUM];
        this.mIccIds = new String[SIM_NUM];
        this.mFullIccIds = new String[SIM_NUM];
        this.needRetrySetPrefNetwork = RESET_PROFILE;
        this.isVoiceCallEndedRegistered = RESET_PROFILE;
        this.mNeedSetLteServiceAbility = RESET_PROFILE;
        this.mSwitchTypes = new int[SIM_NUM];
        this.mCardTypes = new int[SIM_NUM];
        this.mBalongSimSlot = SUB_0;
        this.mHas3Modem = RESET_PROFILE;
        this.mSetSdcsCompleteMsg = null;
        this.retryCount = SUB_0;
        this.mNumOfGetPrefNwModeSuccess = SUB_0;
        this.mModemPreferMode = new int[SIM_NUM];
        this.retryChangeCount = SUB_0;
        this.mBroadcastDone = RESET_PROFILE;
        this.mEventsQ = new LinkedList();
        this.currentSubTypeMap = new HashMap();
        this.mGetUiccCardsStatusDone = new boolean[SIM_NUM];
        this.mGetBalongSimSlotDone = new boolean[SIM_NUM];
        this.mAllCardsReady = RESET_PROFILE;
        this.mPrimaryStackPhoneId = UNKNOWN_CARD;
        this.mSecondaryStackPhoneId = UNKNOWN_CARD;
        this.mPrimaryStackNetworkType = UNKNOWN_CARD;
        this.mSecondaryStackNetworkType = UNKNOWN_CARD;
        this.mSetPrimaryStackPrefMode = UNKNOWN_CARD;
        this.mSetSecondaryStackPrefMode = UNKNOWN_CARD;
        this.expectedDDSsubId = UNKNOWN_CARD;
        this.needSetDataAllowCount = SUB_0;
        this.curSetDataAllowCount = SUB_0;
        this.isSimInsertedArray = new boolean[SIM_NUM];
        this.nwModeArray = new int[SIM_NUM];
        this.subCarrierTypeArray = new SubCarrierType[SIM_NUM];
        this.default4GSlot = SUB_0;
        this.mUiccController = null;
        this.isSet4GSlotInProgress = RESET_PROFILE;
        this.mSet4GSlotCompleteMsg = null;
        this.isSet4GSlotManuallyTriggered = RESET_PROFILE;
        this.isPreBootCompleted = RESET_PROFILE;
        this.mPrefNwMode = new int[SIM_NUM];
        this.current4GSlotBackup = SUB_0;
        this.is4GSlotReviewNeeded = SUB_0;
        this.mUserPref4GSlot = SUB_0;
        this.mSetUiccSubscriptionResult = new int[SIM_NUM];
        this.mNvRestartRildDone = RESET_PROFILE;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    HwAllInOneController.this.loge("intent is null, return");
                    return;
                }
                if ("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED".equals(intent.getAction())) {
                    int status = intent.getIntExtra("simDetectStatus", HwAllInOneController.UNKNOWN_CARD);
                    if (HwModemCapability.isCapabilitySupport(9)) {
                        if (!(!HwAllInOneController.IS_QCRIL_CROSS_MAPPING || status == HwAllInOneController.UNKNOWN_CARD || status == 4)) {
                            HwAllInOneController.this.mNeedSetLteServiceAbility = true;
                        }
                        if (status != HwAllInOneController.UNKNOWN_CARD) {
                            HwAllInOneController.this.processSubInfoRecordUpdated(status);
                        }
                    } else {
                        HwAllInOneController.this.logd("Not process for HISI");
                    }
                } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(intent.getAction())) {
                    slotId = intent.getIntExtra("subscription", -1000);
                    int intValue = intent.getIntExtra("intContent", HwAllInOneController.SUB_0);
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
                        if ((HwAllInOneController.SUB_1 == HwAllInOneController.this.is4GSlotReviewNeeded || HwAllInOneController.SLOT2 == HwAllInOneController.this.is4GSlotReviewNeeded) && -1000 != slotId) {
                            HwAllInOneController.this.processSimStateChanged(simState, slotId);
                        }
                    } else {
                        HwAllInOneController.this.logd("Not process for HISI");
                    }
                } else if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction())) {
                    HwAllInOneController.this.logd("received ACTION_SET_RADIO_CAPABILITY_DONE");
                    if (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT) {
                        HwAllInOneController.this.sendResponseToTarget(HwAllInOneController.this.obtainMessage(HwAllInOneController.EVENT_FAST_SWITCH_SIM_SLOT_DONE, Integer.valueOf(HwAllInOneController.this.expectedDDSsubId)), HwAllInOneController.SUB_0);
                    } else {
                        HwAllInOneController.this.sendSetRadioCapabilitySuccess(true);
                    }
                } else if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED".equals(intent.getAction())) {
                    HwAllInOneController.this.logd("received ACTION_SET_RADIO_CAPABILITY_FAILED");
                    if (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT) {
                        HwAllInOneController.this.logd("received ACTION_SET_RADIO_CAPABILITY_FAILED ,response GENERIC_FAILURE");
                        HwAllInOneController.this.sendResponseToTarget(HwAllInOneController.this.mSet4GSlotCompleteMsg, HwAllInOneController.SLOT2);
                    } else {
                        Message response = HwAllInOneController.this.obtainMessage(HwAllInOneController.SUB_BOTH, Integer.valueOf(HwAllInOneController.this.expectedDDSsubId));
                        AsyncResult.forMessage(response, null, new Exception());
                        response.sendToTarget();
                    }
                } else if ("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                    slotId = HwAllInOneController.UNKNOWN_CARD;
                    int subState = HwAllInOneController.UNKNOWN_CARD;
                    int result = HwAllInOneController.UNKNOWN_CARD;
                    try {
                        slotId = ((Integer) intent.getExtra("subscription", Integer.valueOf(HwAllInOneController.UNKNOWN_CARD))).intValue();
                        subState = ((Integer) intent.getExtra("newSubState", Integer.valueOf(HwAllInOneController.UNKNOWN_CARD))).intValue();
                        result = ((Integer) intent.getExtra("operationResult", Integer.valueOf(HwAllInOneController.UNKNOWN_CARD))).intValue();
                    } catch (Exception ex) {
                        HwAllInOneController.this.logd("Get Intent Extra exception ex:" + ex.getMessage());
                    }
                    HwAllInOneController.this.logd("received ACTION_SUBSCRIPTION_SET_UICC_RESULT,slotId:" + slotId + " subState:" + subState + " result:" + result);
                    if (slotId >= 0 && HwAllInOneController.SIM_NUM > slotId) {
                        if (HwAllInOneController.SUB_1 == result) {
                            HwAllInOneController.this.mSetUiccSubscriptionResult[slotId] = subState;
                        } else if (result == 0) {
                            HwAllInOneController.this.mSetUiccSubscriptionResult[slotId] = HwAllInOneController.UNKNOWN_CARD;
                        }
                    }
                } else if ("android.intent.action.PRE_BOOT_COMPLETED".equals(intent.getAction())) {
                    HwAllInOneController.this.logd("received ACTION_PRE_BOOT_COMPLETED");
                    HwAllInOneController.this.isPreBootCompleted = true;
                    Message msg = HwAllInOneController.this.obtainMessage(HwAllInOneController.EVENT_SWITCH_DUAL_CARD_IF_NEEDED);
                    AsyncResult.forMessage(msg, null, null);
                    HwAllInOneController.this.sendMessage(msg);
                }
            }
        };
        this.mRetryHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwAllInOneController.SUB_1 /*1*/:
                        HwAllInOneController.this.logd("MSG_RETRY_SET_DEFAULT_LTESLOT");
                        HwAllInOneController.this.setPrefNetworkTypeAndStartTimer(msg.arg1);
                    case HwAllInOneController.SLOT2 /*2*/:
                        HwAllInOneController.this.logd("MSG_RETRY_CHANGE_PREF_NETWORK");
                        HwAllInOneController.this.handleGetPreferredNetwork();
                    default:
                        HwAllInOneController.this.logd("Unknown msg:" + msg.what);
                }
            }
        };
        this.mIccChangedRegistrants = new RegistrantList();
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
        for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
            this.isSimInsertedArray[i] = RESET_PROFILE;
            this.subCarrierTypeArray[i] = SubCarrierType.OTHER;
            this.mGetUiccCardsStatusDone[i] = RESET_PROFILE;
            this.mGetBalongSimSlotDone[i] = RESET_PROFILE;
            this.mSwitchTypes[i] = UNKNOWN_CARD;
            this.mCardTypes[i] = UNKNOWN_CARD;
            this.mRadioOns[i] = RESET_PROFILE;
            this.mIccIds[i] = null;
            this.mSetUiccSubscriptionResult[i] = UNKNOWN_CARD;
        }
        for (i = SUB_0; i < this.mCis.length; i += SUB_1) {
            Integer index = Integer.valueOf(i);
            this.mCis[i].registerForIccStatusChanged(this, SUB_1, index);
            this.mCis[i].registerForAvailable(this, SUB_1, index);
            this.mCis[i].registerForNotAvailable(this, EVENT_RADIO_UNAVAILABLE, index);
            this.mCis[i].registerForAvailable(this, EVENT_RADIO_AVAILABLE, index);
            if (IS_HISI_DSDX && (!IS_FULL_NETWORK_SUPPORTED_IN_HISI || IS_FAST_SWITCH_SIMSLOT)) {
                this.mCis[i].registerForSimHotPlug(this, EVENT_SIM_HOTPLUG, index);
            }
        }
        this.mUiccController = UiccController.getInstance();
        if (HwModemCapability.isCapabilitySupport(9)) {
            boolean z;
            if (IS_FULL_NETWORK_SUPPORTED || IS_CMCC_4G_DSDX_ENABLE) {
                z = true;
            } else {
                z = IS_CMCC_CU_DSDX_ENABLE;
            }
            if (!z) {
                logd("there is no need to enter HwAllInOneController");
                return;
            } else if (SUB_1 == SIM_NUM) {
                logd("only one card not to enter HwAllInOneController");
                return;
            }
        }
        this.isSet4GSlotInProgress = true;
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        if (IS_CMCC_4GSWITCH_DISABLE) {
            filter.addAction("android.intent.action.PRE_BOOT_COMPLETED");
        }
        if (IS_QCRIL_CROSS_MAPPING || IS_FAST_SWITCH_SIMSLOT) {
            filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
            filter.addAction("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        }
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

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
        int i2;
        switch (msg.what) {
            case SUB_1 /*1*/:
                logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
                if (IS_CMCC_4G_DSDX_ENABLE || HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
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
            case EVENT_RADIO_UNAVAILABLE /*3*/:
                logd("EVENT_RADIO_UNAVAILABLE, on index " + index);
                this.mCis[index.intValue()].iccGetATR(obtainMessage(EVENT_ICC_GET_ATR_DONE, index));
                this.mRadioOns[index.intValue()] = RESET_PROFILE;
                this.mSwitchTypes[index.intValue()] = UNKNOWN_CARD;
                this.mGetUiccCardsStatusDone[index.intValue()] = RESET_PROFILE;
                this.mGetBalongSimSlotDone[index.intValue()] = RESET_PROFILE;
                this.mCardTypes[index.intValue()] = UNKNOWN_CARD;
                this.mIccIds[index.intValue()] = null;
                this.mFullIccIds[index.intValue()] = null;
                break;
            case SUB_BOTH /*10*/:
                if (hasMessages(EVENT_SET_PREF_NETWORK_TIMEOUT)) {
                    removeMessages(EVENT_SET_PREF_NETWORK_TIMEOUT);
                }
                if (ar == null || ar.exception != null) {
                    refreshCardState();
                    if (!this.isSimInsertedArray[this.default4GSlot]) {
                        this.retryCount = SINGLE_MODE_USIM_CARD;
                        logd("current app destoryed, this error don't need retry send.set retryCount to max value");
                    }
                    loge("handleMessage: EVENT_SET_PREF_NETWORK_DONE failed for slot: " + index + "with retryCount = " + this.retryCount);
                    i = this.retryCount;
                    if (r0 < SINGLE_MODE_USIM_CARD) {
                        this.retryCount += SUB_1;
                        this.mRetryHandler.sendMessageDelayed(this.mRetryHandler.obtainMessage(SUB_1, index.intValue(), SUB_0), 500);
                        return;
                    }
                    if (this.mCis[index.intValue()] != null) {
                        if (RadioState.RADIO_UNAVAILABLE == this.mCis[index.intValue()].getRadioState()) {
                            if (this.mEventsQ.size() == 0) {
                                this.mCis[index.intValue()].registerForOn(this, EVENT_RADIO_ON, index);
                                logd("having retried 20 times, then add the event to queue waiting for radio on to process it!");
                                this.mEventsQ.addLast(new DelayedEvent(SLOT2, index.intValue(), this.nwModeArray));
                                return;
                            }
                        }
                    }
                    if (IS_QCRIL_CROSS_MAPPING) {
                        loge("EVENT_SET_PREF_NETWORK_DONE failed ,response GENERIC_FAILURE");
                        sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
                        this.retryCount = SUB_0;
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
                        sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
                        this.retryCount = SUB_0;
                    }
                } else {
                    logd("handleMessage: EVENT_SET_PREF_NETWORK_DONE success for slot: " + index);
                    HwFrameworkFactory.getHwInnerTelephonyManager().updateCrurrentPhone(index.intValue());
                    sendResponseToTarget(this.mSet4GSlotCompleteMsg, SUB_0);
                    if (this.mSet4GSlotCompleteMsg == null) {
                        logd("slience set network mode done, prepare to set DDS for slot " + index);
                        HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(index.intValue());
                        if (IS_QCRIL_CROSS_MAPPING && PhoneFactory.onDataSubChange(SUB_0, null) == 0) {
                            for (i2 = SUB_0; i2 < SIM_NUM; i2 += SUB_1) {
                                PhoneFactory.resendDataAllowed(i2);
                                logd("EVENT_SET_PREF_NETWORK_DONE resend data allow with slot " + i2);
                            }
                        }
                    }
                    if (this.updateUserDefaultFlag) {
                        Global.putInt(this.mContext.getContentResolver(), USER_DEFAULT_SUBSCRIPTION, index.intValue());
                    }
                    this.retryCount = SUB_0;
                }
                this.mSet4GSlotCompleteMsg = null;
                this.isSet4GSlotInProgress = RESET_PROFILE;
                this.updateUserDefaultFlag = RESET_PROFILE;
                logd("check if there are still events unprocessed in queque with mEventsQ.size() = " + this.mEventsQ.size());
                if (this.mEventsQ.size() != 0) {
                    handleDelayedEvent();
                    break;
                }
                break;
            case EVENT_GET_PREF_NETWORK_DONE /*11*/:
                int subId = msg.arg1;
                this.mNumOfGetPrefNwModeSuccess += SUB_1;
                if (ar.exception == null) {
                    int modemNetworkMode = ((int[]) ar.result)[SUB_0];
                    logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
                    this.mModemPreferMode[subId] = modemNetworkMode;
                } else {
                    logd("Failed to get preferred network mode for slot" + subId);
                    this.mModemPreferMode[subId] = UNKNOWN_CARD;
                }
                if (this.mNumOfGetPrefNwModeSuccess == SIM_NUM) {
                    handleGetPreferredNetwork();
                    this.mNumOfGetPrefNwModeSuccess = SUB_0;
                    break;
                }
                break;
            case EVENT_RADIO_ON /*12*/:
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
            case EVENT_CHANGE_PREF_NETWORK_DONE /*13*/:
                int changeSlotId = msg.arg1;
                if (ar != null && ar.exception == null) {
                    logd("handleMessage: EVENT_CHANGE_PREF_NETWORK_DONE successful for slot: " + changeSlotId);
                    break;
                }
                loge("handleMessage: EVENT_CHANGE_PREF_NETWORK_DONE failed for slot: " + changeSlotId + "with retryCount = " + this.retryChangeCount);
                i = this.retryChangeCount;
                if (r0 < SINGLE_MODE_USIM_CARD) {
                    if (this.mRetryHandler.hasMessages(SLOT2)) {
                        this.mRetryHandler.removeMessages(SLOT2);
                    } else {
                        this.retryChangeCount += SUB_1;
                    }
                    this.mRetryHandler.sendMessageDelayed(this.mRetryHandler.obtainMessage(SLOT2, changeSlotId, SUB_0), 6000);
                    break;
                }
                break;
            case EVENT_SET_PREF_NETWORK_TIMEOUT /*14*/:
                logd("EVENT_SET_PREF_NETWORK_TIMEOUT");
                if (!IS_QCRIL_CROSS_MAPPING) {
                    if (this.isSet4GSlotInProgress) {
                        logd("set retryCount to Max Time and restore ModemBinding and ModemStack to prev State!");
                        this.retryCount = SINGLE_MODE_USIM_CARD;
                        HwModemBindingPolicyHandler.getInstance().restoreToPrevState();
                        break;
                    }
                }
                logd("setRadioCapability timeout");
                sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
                this.mSet4GSlotCompleteMsg = null;
                this.isSet4GSlotInProgress = RESET_PROFILE;
                this.updateUserDefaultFlag = RESET_PROFILE;
                this.retryCount = SINGLE_MODE_USIM_CARD;
                break;
                break;
            case EVENT_SET_DATA_ALLOW_DONE /*15*/:
                logd("Received EVENT_SET_DATA_ALLOW_DONE curSetDataAllowCount = " + this.curSetDataAllowCount);
                this.curSetDataAllowCount += SUB_1;
                if (this.needSetDataAllowCount == this.curSetDataAllowCount) {
                    this.needSetDataAllowCount = SUB_0;
                    this.curSetDataAllowCount = SUB_0;
                    logd("all EVENT_SET_DATA_ALLOW_DONE message got, start switch 4G slot");
                    setWaitingSwitchBalongSlot(true);
                    setDefault4GSlot(this.default4GSlot);
                    break;
                }
                break;
            case EVENT_SIM_HOTPLUG /*16*/:
                onSimHotPlug(ar, index);
                break;
            case EVENT_SWITCH_DUAL_CARD_SLOT /*101*/:
                logd("Received EVENT_SWITCH_DUAL_CARD_SLOT on index " + index);
                if (isntFirstPowerup()) {
                    setWaitingSwitchBalongSlot(RESET_PROFILE);
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
                logd("Received EVENT_RADIO_AVAILABLE on index" + index);
                if (IS_QCRIL_CROSS_MAPPING) {
                    syncNetworkTypeFromDB();
                }
                if (!(SIM_NUM != SLOT2 || this.isVoiceCallEndedRegistered || IS_CHINA_TELECOM)) {
                    Phone[] phones = PhoneFactory.getPhones();
                    int length = phones.length;
                    for (i = SUB_0; i < length; i += SUB_1) {
                        Phone phone = phones[i];
                        if (phone != null) {
                            logd("registerForVoiceCallEnded for phone " + phone.getPhoneId());
                            phone.getCallTracker().registerForVoiceCallEnded(this, EVENT_VOICE_CALL_ENDED, Integer.valueOf(phone.getPhoneId()));
                        }
                    }
                    this.isVoiceCallEndedRegistered = true;
                }
                if (!HwModemCapability.isCapabilitySupport(9)) {
                    int dataSub;
                    int curr4GSlot;
                    boolean ready = true;
                    if (IS_FAST_SWITCH_SIMSLOT && index.intValue() == getUserSwitchDualCardSlots()) {
                        this.mCis[index.intValue()].getCdmaModeSide(obtainMessage(EVENT_GET_CDMA_MODE_SIDE_DONE, index));
                    }
                    this.mRadioOns[index.intValue()] = true;
                    for (i2 = SUB_0; i2 < SIM_NUM; i2 += SUB_1) {
                        if (!this.mRadioOns[i2]) {
                            ready = RESET_PROFILE;
                            logd("mRadioOns is " + this.mRadioOns[i2]);
                            if (ready) {
                                if (this.mSetSdcsCompleteMsg != null || this.mNvRestartRildDone) {
                                    logd("clean iccids!!");
                                    PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                                    break;
                                }
                            }
                            if (!(!IS_HISI_DSDX || HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate())) {
                                dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
                                curr4GSlot = getUserSwitchDualCardSlots();
                                if (dataSub != curr4GSlot && this.mSet4GSlotCompleteMsg == null) {
                                    SubscriptionController.getInstance().setDefaultDataSubId(curr4GSlot);
                                    logd("EVENT_RADIO_AVAILABLE set default data sub to 4G slot");
                                }
                                if ((this.mNvRestartRildDone || this.mSetSdcsCompleteMsg != null) && PhoneFactory.onDataSubChange(SUB_0, null) == 0) {
                                    for (i2 = SUB_0; i2 < SIM_NUM; i2 += SUB_1) {
                                        PhoneFactory.resendDataAllowed(i2);
                                        logd("EVENT_RADIO_AVAILABLE resend data allow with slot " + i2);
                                    }
                                }
                            }
                            logd("EVENT_RADIO_AVAILABLE set isSet4GSlotInProgress to false");
                            setWaitingSwitchBalongSlot(RESET_PROFILE);
                            setDsdsCfgDone(RESET_PROFILE);
                            this.mNvRestartRildDone = RESET_PROFILE;
                            if (this.mSet4GSlotCompleteMsg != null) {
                                AsyncResult.forMessage(this.mSet4GSlotCompleteMsg, Boolean.valueOf(true), null);
                                logd("Switch Dual Card Slots Done!! Sending the mSet4GSlotCompleteMsg back!");
                                sendResponseToTarget(this.mSet4GSlotCompleteMsg, SUB_0);
                                this.mSet4GSlotCompleteMsg = null;
                            }
                            if (this.mSetSdcsCompleteMsg != null) {
                                AsyncResult.forMessage(this.mSetSdcsCompleteMsg, Boolean.valueOf(true), null);
                                logd("Switch Dual Card Slots Done!! Sending the mSetSdcsCompleteMsg back!");
                                this.mSetSdcsCompleteMsg.sendToTarget();
                                this.mSetSdcsCompleteMsg = null;
                                break;
                            }
                        }
                    }
                    if (ready) {
                        dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
                        curr4GSlot = getUserSwitchDualCardSlots();
                        SubscriptionController.getInstance().setDefaultDataSubId(curr4GSlot);
                        logd("EVENT_RADIO_AVAILABLE set default data sub to 4G slot");
                        for (i2 = SUB_0; i2 < SIM_NUM; i2 += SUB_1) {
                            PhoneFactory.resendDataAllowed(i2);
                            logd("EVENT_RADIO_AVAILABLE resend data allow with slot " + i2);
                        }
                        logd("EVENT_RADIO_AVAILABLE set isSet4GSlotInProgress to false");
                        setWaitingSwitchBalongSlot(RESET_PROFILE);
                        setDsdsCfgDone(RESET_PROFILE);
                        this.mNvRestartRildDone = RESET_PROFILE;
                        if (this.mSet4GSlotCompleteMsg != null) {
                            AsyncResult.forMessage(this.mSet4GSlotCompleteMsg, Boolean.valueOf(true), null);
                            logd("Switch Dual Card Slots Done!! Sending the mSet4GSlotCompleteMsg back!");
                            sendResponseToTarget(this.mSet4GSlotCompleteMsg, SUB_0);
                            this.mSet4GSlotCompleteMsg = null;
                        }
                        if (this.mSetSdcsCompleteMsg != null) {
                            AsyncResult.forMessage(this.mSetSdcsCompleteMsg, Boolean.valueOf(true), null);
                            logd("Switch Dual Card Slots Done!! Sending the mSetSdcsCompleteMsg back!");
                            this.mSetSdcsCompleteMsg.sendToTarget();
                            this.mSetSdcsCompleteMsg = null;
                        }
                        break;
                    }
                    logd("clean iccids!!");
                    PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                }
                break;
            case EVENT_SWITCH_SIM_SLOT_CFG_DONE /*105*/:
                logd("Received EVENT_SWITCH_SIM_SLOT_CFG_DONE on index " + index);
                if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
                    HwDsdsController.getInstance().setNeedWatingSlotSwitchDone(RESET_PROFILE);
                }
                ar = msg.obj;
                if (ar != null && ar.exception == null) {
                    logd(" EVENT_SWITCH_SIM_SLOT_CFG_DONE   need to restart rild now ");
                    if (!IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                        if (!HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
                            try {
                                this.mCis[SUB_0].restartRild(null);
                                if (HwDsdsController.IS_DSDSPOWER_SUPPORT && !HwModemCapability.isCapabilitySupport(EVENT_SIM_HOTPLUG)) {
                                    this.mCis[SUB_1].restartRild(null);
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
                    AsyncResult.forMessage(this.mSetSdcsCompleteMsg, Boolean.valueOf(RESET_PROFILE), null);
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
                HwHotplugController.getInstance().processNotifyPromptHotPlug(RESET_PROFILE);
                break;
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
                    String iccid = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, SUB_0, data.length);
                    if (TextUtils.isEmpty(iccid) || ICCID_LEN_MINIMUM > iccid.length()) {
                        logd("iccId is invalid, set it as \"\" ");
                        this.mIccIds[index.intValue()] = "";
                    } else {
                        this.mIccIds[index.intValue()] = iccid.substring(SUB_0, ICCID_LEN_MINIMUM);
                    }
                    this.mFullIccIds[index.intValue()] = iccid;
                    logd("get iccid is " + this.mIccIds[index.intValue()] + " on index " + index);
                    checkIfAllCardsReady();
                    break;
                }
                logd("get iccid exception, maybe card is absent. set iccid as \"\"");
                this.mIccIds[index.intValue()] = "";
                this.mFullIccIds[index.intValue()] = "";
                checkIfAllCardsReady();
                break;
            case EVENT_CHECK_ALL_CARDS_READY /*108*/:
                logd("Received EVENT_CHECK_ALL_CARDS_READY on index " + index);
                checkIfAllCardsReady();
                try {
                    HwHotplugController.getInstance().processNotifyPromptHotPlug(RESET_PROFILE);
                    break;
                } catch (RuntimeException e2) {
                    loge("HwHotplugController is null");
                    break;
                }
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
                if (SubscriptionController.getInstance().getSubState(index.intValue() == 0 ? SUB_1 : SUB_0) != SUB_1) {
                    setDefault4GSlot(index.intValue());
                    break;
                }
                break;
            case EVENT_FAST_SWITCH_SIM_SLOT_DONE /*114*/:
                logd("Received EVENT_FAST_SWITCH_SIM_SLOT_DONE on index " + index);
                if (hasMessages(EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT)) {
                    removeMessages(EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT);
                }
                this.mCis[index.intValue()].getCdmaModeSide(obtainMessage(EVENT_GET_CDMA_MODE_SIDE_DONE, index));
                if (ar == null || ar.exception != null) {
                    loge("EVENT_FAST_SWITCH_SIM_SLOT_DONE failed ,response GENERIC_FAILURE");
                    sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
                } else {
                    logd("EVENT_FAST_SWITCH_SIM_SLOT_DONE success for slot: " + index);
                    HwFrameworkFactory.getHwInnerTelephonyManager().updateCrurrentPhone(index.intValue());
                    setUserSwitchDualCardSlots(index.intValue());
                    if ("0".equals(SystemProperties.get("gsm.nvcfg.rildrestarting", "0"))) {
                        logd("send mSet4GSlotCompleteMsg");
                        sendResponseToTarget(this.mSet4GSlotCompleteMsg, SUB_0);
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
                this.mSet4GSlotCompleteMsg = null;
                setWaitingSwitchBalongSlot(RESET_PROFILE);
                this.updateUserDefaultFlag = RESET_PROFILE;
                break;
            case EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT /*115*/:
                logd("Received EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT on index " + index);
                sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
                this.mSet4GSlotCompleteMsg = null;
                setWaitingSwitchBalongSlot(RESET_PROFILE);
                this.updateUserDefaultFlag = RESET_PROFILE;
                break;
            case EVENT_GET_CDMA_MODE_SIDE_DONE /*116*/:
                logd("Received EVENT_GET_CDMA_MODE_SIDE_DONE on index " + index);
                onGetCdmaModeSideDone(ar, index);
                break;
            case EVENT_SWITCH_DUAL_CARD_IF_NEEDED /*117*/:
                logd("Received EVENT_SWITCH_DUAL_CARD_IF_NEEDED");
                this.mAutoSwitchDualCardsSlotDone = RESET_PROFILE;
                switchDualCardsSlotIfNeeded();
                break;
            case EVENT_RESET_OOS_FLAG /*118*/:
                logd("Received EVENT_RESET_OOS_FLAG on index " + index);
                PhoneFactory.getPhone(index.intValue()).setOOSFlagOnSelectNetworkManually(RESET_PROFILE);
                break;
            default:
                logd("Unknown msg:" + msg.what);
                break;
        }
    }

    private void setPrefNwForCmcc() {
        logd("setPrefNwForCmcc enter.");
        if (IS_CMCC_4GSWITCH_DISABLE && (!IS_CMCC_4GSWITCH_DISABLE || IS_VICE_WCDMA || HwModemCapability.isCapabilitySupport(EVENT_SET_PREF_NETWORK_TIMEOUT))) {
            int i;
            for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
                if (this.mIccIds[i] == null) {
                    logd("setPrefNwForCmcc: mIccIds[" + i + "] is null");
                    return;
                }
            }
            Phone[] phones = PhoneFactory.getPhones();
            i = SUB_0;
            while (i < SIM_NUM) {
                Phone phone = phones[i];
                if (phone == null) {
                    loge("setPrefNwForCmcc: phone " + i + " is null");
                } else {
                    int networkMode;
                    if (getUserSwitchDualCardSlots() == i) {
                        if (IS_FAST_SWITCH_SIMSLOT || !isCDMASimCard(i)) {
                            networkMode = 9;
                        } else {
                            networkMode = 8;
                        }
                    } else if (isCMCCCardBySlotId(i) || !isCMCCHybird()) {
                        if (HwModemCapability.isCapabilitySupport(EVENT_SET_PREF_NETWORK_TIMEOUT)) {
                            if (isCDMASimCard(i)) {
                                networkMode = 4;
                            } else {
                                networkMode = SUB_1;
                            }
                        } else if (isCDMASimCard(i)) {
                            networkMode = MCCMNC_LEN_MINIMUM;
                        } else {
                            networkMode = EVENT_RADIO_UNAVAILABLE;
                        }
                    } else if (isCDMASimCard(i)) {
                        networkMode = MCCMNC_LEN_MINIMUM;
                    } else {
                        networkMode = SUB_1;
                    }
                    logd("setPrefNwForCmcc: i = " + i + ", mode = " + networkMode);
                    phone.setPreferredNetworkType(networkMode, null);
                }
                i += SUB_1;
            }
        }
    }

    private void handleGetPreferredNetwork() {
        int i = SUB_0;
        while (i < SIM_NUM) {
            try {
                int prefNwMode = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i);
                logd("subid = " + i + " prefNwMode = " + prefNwMode);
                if (this.mModemPreferMode[i] != prefNwMode) {
                    logd("modemprefermode is not same with prefer mode in slot = " + i);
                    if (this.mCis[i] != null) {
                        this.mCis[i].setPreferredNetworkType(prefNwMode, obtainMessage(EVENT_CHANGE_PREF_NETWORK_DONE, i, SUB_0));
                    }
                }
                i += SUB_1;
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

    private boolean isValidIndex(int index) {
        return (index < 0 || index >= SIM_NUM) ? RESET_PROFILE : true;
    }

    public void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        logd("onGetIccCardStatusDone on index " + index);
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
        boolean bDSDA;
        boolean isCardTrayOut = IS_SINGLE_CARD_TRAY ? HwCardTrayUtil.isCardTrayOut(SUB_0) : RESET_PROFILE;
        boolean isCardTrayOut2 = (IS_SINGLE_CARD_TRAY || !HwCardTrayUtil.isCardTrayOut(SUB_0)) ? RESET_PROFILE : HwCardTrayUtil.isCardTrayOut(SUB_1);
        if (MultiSimVariants.DSDA == TelephonyManager.getDefault().getMultiSimConfiguration()) {
            bDSDA = true;
        } else {
            bDSDA = RESET_PROFILE;
        }
        if (!bDSDA) {
            return;
        }
        if ((isCardTrayOut || isCardTrayOut2) && IS_HISI_DSDX) {
            logd("DSDX all tray out. disposeCardStatus");
            disposeCardStatus(true);
            setWaitingSwitchBalongSlot(RESET_PROFILE);
        }
    }

    public void checkIfAllCardsReady() {
        boolean bDSDA = true;
        if (!HwModemCapability.isCapabilitySupport(9)) {
            disposeCardStatusWhenAllTrayOut();
            boolean ready = true;
            int i = SUB_0;
            while (i < SIM_NUM) {
                if (this.mSwitchTypes[i] != UNKNOWN_CARD) {
                    if (this.mGetUiccCardsStatusDone[i]) {
                        if (IS_CMCC_4G_DSDX_ENABLE && this.mIccIds[i] == null) {
                            logd("mIccIds[" + i + "] invalid");
                            ready = RESET_PROFILE;
                            break;
                        }
                        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT && !HwVSimUtils.isVSimEnabled()) {
                            if (this.mIccIds[i] == null) {
                                logd("mIccIds[" + i + "] invalid");
                                ready = RESET_PROFILE;
                                break;
                            } else if (UNKNOWN_CARD == HwForeignUsimForTelecom.getInstance().getRatCombineMode(i)) {
                                logd("RatCombineMode[" + i + "] invalid");
                                ready = RESET_PROFILE;
                                break;
                            }
                        }
                        i += SUB_1;
                    } else {
                        logd("mGetUiccCardsStatusDone[" + i + "] == false");
                        ready = RESET_PROFILE;
                        break;
                    }
                }
                logd("mSwitchTypes[" + i + "] == INVALID");
                ready = RESET_PROFILE;
                break;
            }
            int countGetBalongSimSlotDone = SUB_0;
            for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
                if (this.mGetBalongSimSlotDone[i]) {
                    countGetBalongSimSlotDone += SUB_1;
                }
            }
            if (countGetBalongSimSlotDone == 0) {
                logd("mGetBalongSimSlotDone all false");
                ready = RESET_PROFILE;
            }
            if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < SIM_NUM) {
                logd("haven't get all UiccCards done, please wait!");
                ready = RESET_PROFILE;
            } else {
                UiccCard[] uc = this.mUiccController.getUiccCards();
                for (i = SUB_0; i < uc.length; i += SUB_1) {
                    if (uc[i] == null) {
                        logd("UiccCard[" + i + "]" + "is null");
                        ready = RESET_PROFILE;
                    }
                }
            }
            if (IS_FULL_NETWORK_SUPPORTED_IN_HISI && !IS_FAST_SWITCH_SIMSLOT) {
                HwFullNetwork.getInstance().checkIfAllCardsReady();
                if (IS_HISI_DSDX && !HwFullNetwork.getInstance().isGetCdmaModeDone()) {
                    logd("HwFullNetwork not getCdmaModeDone");
                    ready = RESET_PROFILE;
                }
            }
            if (HwVSimUtils.isPlatformTwoModems() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload())) {
                logd("checkIfAllCardsReady()...vsim enabled or card reloading on two modem platform.");
                if (isntFirstPowerup()) {
                    setWaitingSwitchBalongSlot(RESET_PROFILE);
                }
                return;
            }
            if (IS_FAST_SWITCH_SIMSLOT) {
                try {
                    Phone[] phones = PhoneFactory.getPhones();
                    i = SUB_0;
                    while (i < SIM_NUM) {
                        if (phones[i] == null || (phones[i] != null && phones[i].getRadioCapability() == null)) {
                            ready = RESET_PROFILE;
                            logd("RadioCapability is null");
                        }
                        i += SUB_1;
                    }
                } catch (Exception e) {
                    logd("PhoneFactory.getPhones is null");
                    return;
                }
            }
            if (!this.mAllCardsReady && ready) {
                if ("0".equals(SystemProperties.get("gsm.nvcfg.rildrestarting", "0"))) {
                    logd("send mSet4GSlotCompleteMsg. mSet4GSlotCompleteMsg = " + this.mSet4GSlotCompleteMsg);
                    sendResponseToTarget(this.mSet4GSlotCompleteMsg, SUB_0);
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
                if (!this.mAutoSwitchDualCardsSlotDone || !isBalongSimSynced()) {
                    logd("switchDualCardsSlotIfNeeded!");
                    if (MultiSimVariants.DSDA != TelephonyManager.getDefault().getMultiSimConfiguration()) {
                        bDSDA = RESET_PROFILE;
                    }
                    if (bDSDA && IS_CMCC_4GSWITCH_DISABLE && isAllCardsAbsent()) {
                        logd("not need switch slot!");
                    } else {
                        switchDualCardsSlotIfNeeded();
                    }
                } else if (this.mCommrilRestartRild && !getWaitingSwitchBalongSlot()) {
                    logd("mCommrilRestartRild is true");
                    setCommrilRestartRild(RESET_PROFILE);
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
        if (!IS_CMCC_4GSWITCH_DISABLE) {
            return;
        }
        if ((IS_VICE_WCDMA || HwModemCapability.isCapabilitySupport(EVENT_SET_PREF_NETWORK_TIMEOUT)) && this.needRetrySetPrefNetwork) {
            logd("needRetrySetPrefNetwork");
            setPrefNwForCmcc();
            this.needRetrySetPrefNetwork = RESET_PROFILE;
        }
    }

    private void restartRildForNvcfg() {
        if (!this.mNvRestartRildDone && this.mAutoSwitchDualCardsSlotDone && this.mSetSdcsCompleteMsg == null && "1".equals(SystemProperties.get("gsm.nvcfg.resetrild", "0")) && IS_FULL_NETWORK_SUPPORTED_IN_HISI && !HwFullNetwork.getInstance().getNeedSwitchCommrilMode()) {
            logd("checkIfAllCardsReady nv store need restart rild");
            this.mNvRestartRildDone = true;
            HwFullNetwork.getInstance().waitToRestartRild();
        }
        if (!this.mNvRestartRildDone && this.mAutoSwitchDualCardsSlotDone && this.mSetSdcsCompleteMsg == null && "1".equals(SystemProperties.get("gsm.nvcfg.resetrild", "0")) && !IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
            logd("checkIfAllCardsReady nv store need restart rild");
            this.mNvRestartRildDone = true;
            try {
                this.mCis[SUB_0].restartRild(null);
                if (HwDsdsController.IS_DSDSPOWER_SUPPORT && !HwModemCapability.isCapabilitySupport(EVENT_SIM_HOTPLUG)) {
                    this.mCis[SUB_1].restartRild(null);
                }
            } catch (RuntimeException e) {
            }
        }
    }

    private void switchDualCardsSlotIfNeeded() {
        if (!SystemProperties.getBoolean("persist.sys.dualcards", RESET_PROFILE) || (this.mAutoSwitchDualCardsSlotDone && isBalongSimSynced())) {
            logd("mAutoSwitchDualCardsSlotDone has been completed before");
            if (isntFirstPowerup()) {
                setWaitingSwitchBalongSlot(RESET_PROFILE);
            }
        } else if (!this.mAutoSwitchDualCardsSlotDone || isBalongSimSynced()) {
            this.mAutoSwitchDualCardsSlotDone = true;
            if (judgeDefalt4GSlot()) {
                logd("Need to set the 4G slot");
            } else {
                this.default4GSlot = getUserSwitchDualCardSlots();
                logd("there is no need to set the 4G slot, setdefault slot as " + this.default4GSlot);
            }
            if (!(!IS_HISI_DSDX || HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate())) {
                if (this.needSetDataAllowCount == 0) {
                    SubscriptionController.getInstance().setDefaultDataSubId(this.default4GSlot);
                    this.needSetDataAllowCount = PhoneFactory.onDataSubChange(EVENT_SET_DATA_ALLOW_DONE, this);
                    if (this.needSetDataAllowCount > 0) {
                        logd("switchDualCardsSlotIfNeeded return because needSetDataAllowCount = " + this.needSetDataAllowCount);
                        this.curSetDataAllowCount = SUB_0;
                        return;
                    }
                    logd("switchDualCardsSlotIfNeeded no need set_data_allow to any phone");
                } else {
                    logd("switchDualCardsSlotIfNeeded already in set_data_allow process , needSetDataAllowCount = " + this.needSetDataAllowCount);
                    return;
                }
            }
            setWaitingSwitchBalongSlot(true);
            setDefault4GSlot(this.default4GSlot);
        } else {
            logd("mAutoSwitchDualCardsSlotDone && !isBalongSimSynced() is true");
        }
    }

    public void setUserSwitchDualCardSlots(int subscription) {
        System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", subscription);
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
        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT && !HwVSimUtils.isVSimEnabled()) {
            int iForeignSlot = HwForeignUsimForTelecom.getInstance().getForeignCardSlot();
            if (isValidIndex(iForeignSlot)) {
                this.default4GSlot = iForeignSlot;
                logd("switchDualCardsSlotCdma default4GSlot for foreign card :" + this.default4GSlot);
                return true;
            }
        }
        int temSub = getUserSwitchDualCardSlots();
        if (autoSwitchToSlot() == 0) {
            temSub = isBalongSimSynced() ? SUB_0 : SUB_1;
        } else if (autoSwitchToSlot() == SUB_1) {
            temSub = isBalongSimSynced() ? SUB_1 : SUB_0;
        }
        this.default4GSlot = temSub;
        logd("switchDualCardsSlotCdma default4GSlot " + this.default4GSlot);
        return true;
    }

    private int autoSwitchToSlot() {
        int slot = UNKNOWN_CARD;
        if (getUserSwitchDualCardSlots() == 0) {
            if (!((this.mSwitchTypes[SUB_0] == SUB_1 && this.mSwitchTypes[SUB_1] == EVENT_RADIO_UNAVAILABLE) || ((this.mSwitchTypes[SUB_0] == SUB_1 && this.mSwitchTypes[SUB_1] == SLOT2) || ((this.mSwitchTypes[SUB_0] == SUB_1 && this.mSwitchTypes[SUB_1] == 0) || (this.mSwitchTypes[SUB_0] == 0 && this.mSwitchTypes[SUB_1] == EVENT_RADIO_UNAVAILABLE))))) {
                if (this.mSwitchTypes[SUB_0] == 0 && this.mSwitchTypes[SUB_1] == SLOT2) {
                }
            }
            slot = SUB_1;
        } else {
            if (!((this.mSwitchTypes[SUB_0] == 0 && this.mSwitchTypes[SUB_1] == SUB_1) || ((this.mSwitchTypes[SUB_0] == EVENT_RADIO_UNAVAILABLE && this.mSwitchTypes[SUB_1] == SUB_1) || ((this.mSwitchTypes[SUB_0] == EVENT_RADIO_UNAVAILABLE && this.mSwitchTypes[SUB_1] == 0) || (this.mSwitchTypes[SUB_0] == SLOT2 && this.mSwitchTypes[SUB_1] == SUB_1))))) {
                if (this.mSwitchTypes[SUB_0] == SLOT2 && this.mSwitchTypes[SUB_1] == 0) {
                }
            }
            slot = SUB_0;
        }
        logd("mSwitchTypes[0] = " + this.mSwitchTypes[SUB_0] + ", mSwitchTypes[1] = " + this.mSwitchTypes[SUB_1] + ", slot = " + slot);
        return slot;
    }

    public void setCommrilRestartRild(boolean bCommrilRestartRild) {
        this.mCommrilRestartRild = bCommrilRestartRild;
        logd("setCommrilRestartRild = " + bCommrilRestartRild);
    }

    private void initDefaultDBIfNeeded() {
        try {
            System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            logd("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
            System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", SUB_0);
        }
    }

    private void processSubInfoRecordUpdated(int detectedType) {
        if (4 != detectedType) {
            logd("cards in the slots are changed with detectedType: " + detectedType);
            refreshCardState();
            judgeSubCarrierType();
            this.is4GSlotReviewNeeded = SUB_1;
            if (judgeDefalt4GSlot()) {
                judgeNwMode(this.default4GSlot);
                if (this.isSet4GSlotInProgress) {
                    logd("There is event in progress, need to add the new event to queqe!");
                    this.mEventsQ.addLast(new DelayedEvent(SUB_1, this.default4GSlot, this.nwModeArray));
                } else {
                    setDefault4GSlot(this.default4GSlot);
                }
            } else {
                logd("there is no need to set the 4G slot");
            }
        } else if (IS_FULL_NETWORK_SUPPORTED || IS_CMCC_CU_DSDX_ENABLE || IS_CMCC_4G_DSDX_ENABLE) {
            if (this.isSet4GSlotInProgress) {
                logd("processSubInfoRecordUpdated: setting lte slot is in progress, ignore this event");
                return;
            }
            logd("processSubInfoRecordUpdated EXTRA_VALUE_NOCHANGE check!");
            refreshCardState();
            judgeSubCarrierType();
            int userPref4GSlot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
            boolean set4GDefaltSlot = (userPref4GSlot >= SIM_NUM || this.isSimInsertedArray[userPref4GSlot]) ? userPref4GSlot >= SIM_NUM ? true : RESET_PROFILE : true;
            boolean need4GCheckWhenBoot = (!IS_CMCC_4GSWITCH_DISABLE || this.subCarrierTypeArray[SUB_0].isCMCCCard() == this.subCarrierTypeArray[SUB_1].isCMCCCard()) ? RESET_PROFILE : true;
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
                    logd("setDefault4GSlot when networkmode change!");
                    return;
                }
                this.retryChangeCount = SUB_0;
                for (int sub = SUB_0; sub < SIM_NUM; sub += SUB_1) {
                    if (this.mCis[sub] != null) {
                        this.mCis[sub].getPreferredNetworkType(obtainMessage(EVENT_GET_PREF_NETWORK_DONE, sub, SUB_0));
                    }
                }
            } else {
                this.is4GSlotReviewNeeded = SLOT2;
                this.mUserPref4GSlot = userPref4GSlot;
                this.subCarrierTypeArray[userPref4GSlot] = SubCarrierType.OTHER;
                logd("userPref4GSlot=" + userPref4GSlot + " SubCarrierType=" + this.subCarrierTypeArray[userPref4GSlot] + " Need to check in Sim State Change!");
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
            this.isSimInsertedArray[slotId] = subState == SUB_1 ? true : RESET_PROFILE;
            refreshCardState();
            judgeSubCarrierType();
            this.is4GSlotReviewNeeded = SUB_1;
            if (IS_CMCC_4GSWITCH_DISABLE && this.subCarrierTypeArray[slotId].isCMCCCard()) {
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
                this.mCis[slotId].registerForOn(this, EVENT_RADIO_ON, Integer.valueOf(slotId));
                this.mEventsQ.addLast(new DelayedEvent(EVENT_RADIO_UNAVAILABLE, slotId, this.nwModeArray));
                return;
            }
            int i;
            boolean isSubCarrierTypeChangedForReady;
            logd("processSimStateChanged: check if it needs to update main card!");
            refreshCardState();
            SubCarrierType[] oldSubCarrierTypeArr = new SubCarrierType[SIM_NUM];
            for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
                oldSubCarrierTypeArr[i] = this.subCarrierTypeArray[i];
            }
            judgeSubCarrierType();
            judgeSubCarrierTypeByMccMnc(slotId);
            for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
                if (i != slotId) {
                    this.subCarrierTypeArray[i] = oldSubCarrierTypeArr[i];
                }
            }
            logd("processSimStateChanged: READY!oldSubCarrierType is " + oldSubCarrierTypeArr[slotId] + " for slot" + slotId);
            if (oldSubCarrierTypeArr[slotId] == this.subCarrierTypeArray[slotId] || this.subCarrierTypeArray[slotId].isReCheckFail()) {
                isSubCarrierTypeChangedForReady = RESET_PROFILE;
            } else {
                isSubCarrierTypeChangedForReady = true;
            }
            int sub;
            if (isSubCarrierTypeChangedForReady) {
                boolean isSetDefault4GNeed = RESET_PROFILE;
                int new4GSlotId = this.default4GSlot;
                if (SUB_1 == this.is4GSlotReviewNeeded) {
                    logd("processSimStateChanged: auto mode!");
                    isSetDefault4GNeed = judgeDefalt4GSlot();
                    new4GSlotId = this.default4GSlot;
                } else if (SLOT2 == this.is4GSlotReviewNeeded) {
                    logd("processSimStateChanged: fix mode!");
                    judgeNwMode(this.mUserPref4GSlot);
                    isSetDefault4GNeed = isSetDefault4GSlotNeeded(this.mUserPref4GSlot);
                    new4GSlotId = this.mUserPref4GSlot;
                }
                if (isSetDefault4GNeed) {
                    judgeNwMode(new4GSlotId);
                    if (this.isSet4GSlotInProgress) {
                        logd("There is event in progress, need to add the new event to queqe!");
                        this.mEventsQ.addLast(new DelayedEvent(SUB_1, new4GSlotId, this.nwModeArray));
                    } else {
                        setDefault4GSlot(new4GSlotId);
                    }
                } else {
                    logd("processSimStateChanged: there is no need to set the 4G slot");
                    if (SLOT2 == this.is4GSlotReviewNeeded) {
                        this.retryChangeCount = SUB_0;
                        for (sub = SUB_0; sub < SIM_NUM; sub += SUB_1) {
                            if (this.mCis[sub] != null) {
                                this.mCis[sub].getPreferredNetworkType(obtainMessage(EVENT_GET_PREF_NETWORK_DONE, sub, SUB_0));
                            }
                        }
                    }
                }
            } else {
                logd("processSimStateChanged: there is no need to update main card!");
                if (SLOT2 == this.is4GSlotReviewNeeded) {
                    this.retryChangeCount = SUB_0;
                    for (sub = SUB_0; sub < SIM_NUM; sub += SUB_1) {
                        if (this.mCis[sub] != null) {
                            this.mCis[sub].getPreferredNetworkType(obtainMessage(EVENT_GET_PREF_NETWORK_DONE, sub, SUB_0));
                        }
                    }
                }
            }
        }
    }

    private String getIccId(int slot) {
        String iccId = null;
        if (this.mUiccController.getUiccCard(slot) != null) {
            iccId = this.mUiccController.getUiccCard(slot).getIccId();
        }
        if (iccId != null) {
            return iccId;
        }
        List<SubscriptionInfo> subInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIdWithCheck(slot, RESET_PROFILE, this.mContext.getOpPackageName());
        if (subInfo != null) {
            iccId = ((SubscriptionInfo) subInfo.get(SUB_0)).getIccId();
        }
        return iccId;
    }

    private void refreshCardState() {
        for (int index = SUB_0; index < SIM_NUM; index += SUB_1) {
            if (HwModemCapability.isCapabilitySupport(9)) {
                boolean isSubActivated = SubscriptionController.getInstance().getSubState(index) == SUB_1 ? true : RESET_PROFILE;
                boolean[] zArr = this.isSimInsertedArray;
                if (!isCardPresent(index)) {
                    isSubActivated = RESET_PROFILE;
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
                return RESET_PROFILE;
            }
            logd("judgeSubCarrierTypeByMccMnc: current subCarrierTypeArray is : " + this.subCarrierTypeArray[slotId]);
            if (isCMCCCardByMccMnc(mccMnc)) {
                if (SLOT2 == appType) {
                    this.subCarrierTypeArray[slotId] = SubCarrierType.CARRIER_CMCC_USIM;
                } else if (SUB_1 == appType) {
                    this.subCarrierTypeArray[slotId] = SubCarrierType.CARRIER_CMCC_SIM;
                }
            } else if (isCUCardByMccMnc(mccMnc)) {
                if (SLOT2 == appType) {
                    this.subCarrierTypeArray[slotId] = SubCarrierType.CARRIER_CU_USIM;
                } else if (SUB_1 == appType) {
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
            if (SLOT2 == appType || SUB_1 == appType) {
                app = uiccCard.getApplication(SUB_1);
            } else if (4 == appType || EVENT_RADIO_UNAVAILABLE == appType) {
                app = uiccCard.getApplication(SLOT2);
            } else {
                logd("unknown appType, return!");
                return null;
            }
        }
        if (app != null) {
            IccRecords records = app.getIccRecords();
            if (records != null) {
                String imsi = records.getIMSI();
                if (imsi == null || MCCMNC_LEN_MINIMUM >= imsi.length()) {
                    logd("invalid imsi!");
                } else {
                    mccMnc = imsi.substring(SUB_0, MCCMNC_LEN_MINIMUM);
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
        int sub = SUB_0;
        while (sub < SIM_NUM) {
            logd("judgeSubCarrierType: isSimInsertedArray[" + sub + "] = " + this.isSimInsertedArray[sub]);
            if (this.isSimInsertedArray[sub]) {
                String iccId = getIccId(sub);
                if (TextUtils.isEmpty(iccId) || ICCID_LEN_MINIMUM > iccId.length()) {
                    loge("judgeSubCarrierType: iccId is invalid, set the sub carrier type as OTHER");
                    this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                } else {
                    String inn = iccId.substring(SUB_0, ICCID_LEN_MINIMUM);
                    int appType = getCardAppType(sub);
                    logd("judgeSubCarrierType: iccId is " + inn + " and app type is " + appType + " for sub " + sub);
                    if (isCMCCCard(inn)) {
                        if (SLOT2 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CMCC_USIM;
                        } else if (SUB_1 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CMCC_SIM;
                        } else if (4 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (EVENT_RADIO_UNAVAILABLE == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                        }
                    } else if (isCUCard(inn)) {
                        if (SLOT2 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CU_USIM;
                        } else if (SUB_1 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CU_SIM;
                        } else if (4 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (EVENT_RADIO_UNAVAILABLE == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                        }
                    } else if (isCTCard(inn)) {
                        if (4 == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (EVENT_RADIO_UNAVAILABLE == appType) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else if (DUAL_MODE_TELECOM_LTE_CARD == HwFrameworkFactory.getHwInnerTelephonyManager().getCardType(sub)) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_CSIM;
                        } else if (CT_NATIONAL_ROAMING_CARD == HwFrameworkFactory.getHwInnerTelephonyManager().getCardType(sub) || SINGLE_MODE_RUIM_CARD == HwFrameworkFactory.getHwInnerTelephonyManager().getCardType(sub)) {
                            this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_CT_RUIM;
                        } else {
                            this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                        }
                    } else if (SLOT2 == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_USIM;
                    } else if (SUB_1 == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_SIM;
                    } else if (4 == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_CSIM;
                    } else if (EVENT_RADIO_UNAVAILABLE == appType) {
                        this.subCarrierTypeArray[sub] = SubCarrierType.CARRIER_FOREIGN_RUIM;
                    } else {
                        this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
                    }
                }
            } else {
                logd("judgeSubCarrierType: sub " + sub + " is absent");
                this.subCarrierTypeArray[sub] = SubCarrierType.OTHER;
            }
            sub += SUB_1;
        }
    }

    private void judgeNwMode(int lteSlotId) {
        int nwMode4GforCU;
        int nwMode4GforCMCC;
        int nwMode4GforCT;
        int otherNWModeInCMCC;
        boolean is4GAbilityOn = SUB_1 == HwFrameworkFactory.getHwInnerTelephonyManager().getLteServiceAbility() ? true : RESET_PROFILE;
        logd("judgeNwMode: the LTE slot will be " + lteSlotId + " with the is4GAbilityOn = " + is4GAbilityOn);
        if (is4GAbilityOn) {
            nwMode4GforCU = 9;
            if (DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCMCC = 17;
            } else {
                nwMode4GforCMCC = SINGLE_MODE_USIM_CARD;
            }
            nwMode4GforCT = SUB_BOTH;
            otherNWModeInCMCC = 9;
        } else {
            nwMode4GforCU = EVENT_RADIO_UNAVAILABLE;
            if (DEFAULT_NETWORK_MODE == 17) {
                nwMode4GforCMCC = EVENT_SIM_HOTPLUG;
            } else {
                nwMode4GforCMCC = 18;
            }
            nwMode4GforCT = 7;
            otherNWModeInCMCC = EVENT_RADIO_UNAVAILABLE;
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
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = SUB_1;
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
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = SUB_1;
            }
        }
        logd("judgeNwModeForCMCC_CU prefer sub network mode is " + this.nwModeArray[lteSlotId]);
    }

    private void putPreNWModeListToDBforCMCC() {
        logd("in putPreNWModeListToDBforCMCC");
        ContentResolver resolver = this.mContext.getContentResolver();
        switch (DEFAULT_NETWORK_MODE) {
            case HwVSimConstants.EVENT_SET_APDSFLOWCFG_DONE /*17*/:
                System.putInt(resolver, NETWORK_MODE_4G_PRE, 17);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, EVENT_SIM_HOTPLUG);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, SUB_1);
            case SINGLE_MODE_USIM_CARD /*20*/:
                System.putInt(resolver, NETWORK_MODE_4G_PRE, SINGLE_MODE_USIM_CARD);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, 18);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, SUB_1);
            default:
                System.putInt(resolver, NETWORK_MODE_4G_PRE, UNKNOWN_CARD);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, UNKNOWN_CARD);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, UNKNOWN_CARD);
        }
    }

    private void judgePreNwModeSubIdAndListForCU(int lteSlotId) {
        logd("in judgePreNwModeSubIdAndListForCU ");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.subCarrierTypeArray[lteSlotId].isCCard()) {
            System.putInt(resolver, MAIN_CARD_INDEX, UNKNOWN_CARD);
            System.putInt(resolver, NETWORK_MODE_4G_PRE, UNKNOWN_CARD);
            System.putInt(resolver, NETWORK_MODE_3G_PRE, UNKNOWN_CARD);
            System.putInt(resolver, NETWORK_MODE_2G_ONLY, UNKNOWN_CARD);
        } else {
            System.putInt(resolver, MAIN_CARD_INDEX, lteSlotId);
            if (this.subCarrierTypeArray[lteSlotId].isCMCCCard()) {
                putPreNWModeListToDBforCMCC();
            } else {
                System.putInt(resolver, NETWORK_MODE_4G_PRE, 9);
                System.putInt(resolver, NETWORK_MODE_3G_PRE, EVENT_RADIO_UNAVAILABLE);
                System.putInt(resolver, NETWORK_MODE_2G_ONLY, SUB_1);
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
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (i != lteSlotId) {
                this.nwModeArray[i] = SUB_1;
            }
            logd("judgeNwModeForCMCC, slotId = " + i + "nwModeArray[i] = " + this.nwModeArray[i]);
        }
    }

    private void judgePreNwModeSubIdAndListForCMCC(int lteSlotId) {
        logd("in judgePreNwModeSubIdAndListForCMCC ");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.subCarrierTypeArray[lteSlotId].isCUCard() || this.subCarrierTypeArray[lteSlotId].isCCard()) {
            System.putInt(resolver, MAIN_CARD_INDEX, UNKNOWN_CARD);
            System.putInt(resolver, NETWORK_MODE_4G_PRE, UNKNOWN_CARD);
            System.putInt(resolver, NETWORK_MODE_3G_PRE, UNKNOWN_CARD);
            System.putInt(resolver, NETWORK_MODE_2G_ONLY, UNKNOWN_CARD);
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
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (i != lteSlotId) {
                if (!IS_CARD2_CDMA_SUPPORTED) {
                    this.nwModeArray[i] = SUB_1;
                } else if (!this.subCarrierTypeArray[i].isCCard() || this.subCarrierTypeArray[lteSlotId].isCCard()) {
                    this.nwModeArray[i] = EVENT_RADIO_UNAVAILABLE;
                } else {
                    this.nwModeArray[i] = 7;
                }
            }
        }
    }

    private void saveIccidsWhenAllCardsReady() {
        logd("saveIccidsWhenAllCardsReady");
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            String iccIdToSave = this.mFullIccIds[i];
            if ((iccIdToSave != null && !"".equals(iccIdToSave)) || IS_CMCC_4GSWITCH_DISABLE) {
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
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            String oldIccId = sp.getString("4G_AUTO_SWITCH_ICCID_SLOT" + i, "");
            if (!"".equals(oldIccId)) {
                try {
                    oldIccId = HwAESCryptoUtil.decrypt(MASTER_PASSWORD, oldIccId);
                } catch (Exception ex) {
                    logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
                }
            }
            String nowIccId = this.mFullIccIds[i];
            logd("anySimCardChanged oldIccId" + i + " = " + oldIccId);
            logd("anySimCardChanged nowIccId" + i + " = " + this.mIccIds[i]);
            if (nowIccId == null) {
                nowIccId = "";
            }
            if (!oldIccId.equals(this.mFullIccIds[i])) {
                return true;
            }
        }
        return RESET_PROFILE;
    }

    private boolean judgeDefalt4GSlot() {
        logd("judgeDefalt4GSlot start");
        if (IS_CMCC_4G_DSDX_ENABLE) {
            logd("cmcc 4g dsdx begin.");
            if (HwModemCapability.isCapabilitySupport(9)) {
                return judgeDefault4GSlotForCMCC();
            }
            return judgeDefaultSlotId4HisiCmcc(RESET_PROFILE);
        } else if (IS_CMCC_CU_DSDX_ENABLE) {
            logd("cmcc_cu 4g dsdx begin.");
            if (HwModemCapability.isCapabilitySupport(9)) {
                return judgeDefault4GSlotForCMCC();
            }
            return judgeDefaultSlotId4Hisi();
        } else if (IS_4G_SWITCH_SUPPORTED && IS_FULL_NETWORK_SUPPORTED) {
            return judgeDefault4GSlotForChannelVersion();
        } else {
            if (!IS_4G_SWITCH_SUPPORTED && IS_FULL_NETWORK_SUPPORTED) {
                this.default4GSlot = SUB_0;
                return true;
            } else if (IS_CHINA_TELECOM) {
                logd("china telecom dsdx begin.");
                return switchDualCardsSlotCdma();
            } else if (HwModemCapability.isCapabilitySupport(9)) {
                return RESET_PROFILE;
            } else {
                return judgeDefaultSlotId4Hisi();
            }
        }
    }

    private boolean judgeDefault4GSlotForCMCC() {
        logd("judgeDefault4GSlotForCMCC enter");
        int curSimCount = SUB_0;
        int noSimCount = SUB_0;
        boolean is4GSlotFound = RESET_PROFILE;
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (this.isSimInsertedArray[i]) {
                curSimCount += SUB_1;
                if (!is4GSlotFound) {
                    this.default4GSlot = i;
                }
                is4GSlotFound = true;
            } else {
                noSimCount += SUB_1;
            }
        }
        logd("curSimCount =" + curSimCount + ", noSimCount = " + noSimCount);
        if (SIM_NUM != curSimCount + noSimCount || curSimCount == 0) {
            loge("cards status error or all cards absent.");
            return RESET_PROFILE;
        } else if (SUB_1 == curSimCount) {
            return true;
        } else {
            initSubTypes();
            int mSub = getMainCardSubByPriority(SubType.CARRIER_PREFERRED);
            logd(this.currentSubTypeMap.toString());
            logd("4G slot sub is " + mSub);
            switch (mSub) {
                case SUB_0 /*0*/:
                case SUB_1 /*1*/:
                    this.default4GSlot = mSub;
                    break;
                case SUB_BOTH /*10*/:
                    logd("The two cards inserted have the same priority!");
                    this.default4GSlot = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
                    break;
                default:
                    is4GSlotFound = RESET_PROFILE;
                    break;
            }
            return is4GSlotFound;
        }
    }

    private void initSubTypes() {
        logd("in initSubTypes.");
        for (int index = SUB_0; index < SIM_NUM; index += SUB_1) {
            this.currentSubTypeMap.put(Integer.valueOf(index), getSubTypeBySub(index));
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
            case SUB_1 /*1*/:
                subType = SubType.CARRIER;
                break;
            case SLOT2 /*2*/:
                subType = SubType.CARRIER_PREFERRED;
                break;
            case EVENT_RADIO_UNAVAILABLE /*3*/:
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                if (IS_CHINA_TELECOM) {
                    subType = SubType.CARRIER_PREFERRED;
                    break;
                }
                break;
            case ICCID_LEN_MINIMUM /*6*/:
                if (IS_CMCC_CU_DSDX_ENABLE) {
                    subType = SubType.CARRIER_PREFERRED;
                    break;
                }
            case MCCMNC_LEN_MINIMUM /*5*/:
                if (!IS_CMCC_CU_DSDX_ENABLE) {
                    subType = SubType.LOCAL_CARRIER;
                    break;
                }
                subType = SubType.CARRIER;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
            case SUB_BOTH /*10*/:
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
        return Integer.valueOf(UNKNOWN_CARD);
    }

    private int getMainCardSubByPriority(SubType targetType) {
        logd("in getMainCardSubByPriority, input targetType = " + targetType);
        int count = SUB_0;
        for (Entry<Integer, SubType> mapEntry : this.currentSubTypeMap.entrySet()) {
            if (mapEntry.getValue() == targetType) {
                count += SUB_1;
            }
        }
        logd("count = " + count);
        if (SUB_1 == count) {
            return getKeyFromMap(this.currentSubTypeMap, targetType).intValue();
        }
        if (SUB_1 < count) {
            logd("The priority is the same between two slots: return SAME_PRIORITY");
            return SUB_BOTH;
        } else if (targetType.ordinal() < SubType.LOCAL_CARRIER.ordinal()) {
            return getMainCardSubByPriority(SubType.values()[targetType.ordinal() + SUB_1]);
        } else {
            return UNKNOWN_CARD;
        }
    }

    private boolean judgeDefault4GSlotForChannelVersion() {
        logd("judgeDefault4GSlotForChannelVersion start");
        int numOfSimPresent = SUB_0;
        if (!HwModemCapability.isCapabilitySupport(9)) {
            return judgeDefaultSlotId4Hisi();
        }
        int i;
        boolean is4GSlotFound;
        for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (this.isSimInsertedArray[i]) {
                numOfSimPresent += SUB_1;
            }
        }
        if (numOfSimPresent == 0) {
            logd("no card inserted");
            is4GSlotFound = RESET_PROFILE;
        } else if (SUB_1 == numOfSimPresent) {
            i = SUB_0;
            while (SIM_NUM > i && !this.isSimInsertedArray[i]) {
                logd("isSimInsertedArray[" + i + "] = " + this.isSimInsertedArray[i]);
                i += SUB_1;
            }
            if (SIM_NUM == i) {
                logd("there is no sim card inserted, error happen!!");
                is4GSlotFound = RESET_PROFILE;
            } else {
                logd("there is only one card inserted, set it as the 4G slot");
                is4GSlotFound = true;
                this.default4GSlot = i;
            }
        } else if (IS_CARD2_CDMA_SUPPORTED || IS_QCRIL_CROSS_MAPPING) {
            is4GSlotFound = true;
            this.default4GSlot = SUB_0;
        } else {
            int numOfCdmaCard = SUB_0;
            int indexOfCCard = SUB_0;
            i = SUB_0;
            while (i < SIM_NUM) {
                if (this.isSimInsertedArray[i] && this.subCarrierTypeArray[i].isCCard()) {
                    numOfCdmaCard += SUB_1;
                    indexOfCCard = i;
                }
                i += SUB_1;
            }
            if (SUB_1 == numOfCdmaCard) {
                logd("there is only one CDMA card inserted, set it as the 4G slot");
                is4GSlotFound = true;
                this.default4GSlot = indexOfCCard;
            } else {
                logd("there are multiple CDMA cards or U cards inserted, set the SUB_0 as the lte slot");
                is4GSlotFound = true;
                this.default4GSlot = SUB_0;
                if (numOfCdmaCard > SUB_1) {
                    this.updateUserDefaultFlag = true;
                }
            }
        }
        return is4GSlotFound;
    }

    public boolean isCMCCCard(String inn) {
        return CMCC_ICCID_ARRAY.contains(inn);
    }

    public boolean isCUCard(String inn) {
        return CU_ICCID_ARRAY.contains(inn);
    }

    public boolean isCTCard(String inn) {
        return CT_ICCID_ARRAY.contains(inn);
    }

    private boolean isCMCCCardByMccMnc(String mccMnc) {
        return CMCC_MCCMNC_ARRAY.contains(mccMnc);
    }

    private boolean isCUCardByMccMnc(String mccMnc) {
        return CU_MCCMNC_ARRAY.contains(mccMnc);
    }

    public boolean isCMCCCardBySlotId(int slotId) {
        if (!isValidIndex(slotId)) {
            logd("isCMCCCardBySlotId: Invalid slotId: " + slotId);
            return RESET_PROFILE;
        } else if (this.mIccIds[slotId] != null) {
            return isCMCCCard(this.mIccIds[slotId]);
        } else {
            logd("isCMCCCardBySlotId: mIccIds[" + slotId + "] is null");
            return RESET_PROFILE;
        }
    }

    public boolean isCMCCHybird() {
        boolean hasCMCC = RESET_PROFILE;
        boolean hasOther = RESET_PROFILE;
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (this.mIccIds[i] == null) {
                return RESET_PROFILE;
            }
            if (isCMCCCard(this.mIccIds[i])) {
                hasCMCC = true;
            } else {
                hasOther = true;
            }
        }
        if (!hasCMCC) {
            hasOther = RESET_PROFILE;
        }
        return hasOther;
    }

    private int getCardAppType(int slotId) {
        if (this.mUiccController == null) {
            return SUB_0;
        }
        UiccCard uiccCard = this.mUiccController.getUiccCard(slotId);
        if (uiccCard == null) {
            logd("getCardAppType: uiccCard is null for slot " + slotId);
            return SUB_0;
        }
        int appType;
        if (uiccCard.getApplicationByType(4) != null) {
            appType = 4;
        } else if (uiccCard.getApplicationByType(EVENT_RADIO_UNAVAILABLE) != null) {
            appType = EVENT_RADIO_UNAVAILABLE;
        } else if (uiccCard.getApplicationByType(SLOT2) != null) {
            appType = SLOT2;
        } else if (uiccCard.getApplicationByType(SUB_1) != null) {
            appType = SUB_1;
        } else {
            appType = SUB_0;
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
            LteSlot = SUB_0;
            gsmOnlySlot = SUB_1;
        } else if (SUB_1 == slotId) {
            LteSlot = SUB_1;
            gsmOnlySlot = SUB_0;
        } else {
            loge("setDefault4GSlot: invalid slotId is input, right now, only support two cards!");
            if (this.mEventsQ.size() > 0) {
                handleDelayedEvent();
            } else {
                this.isSet4GSlotInProgress = RESET_PROFILE;
            }
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
            this.mSet4GSlotCompleteMsg = null;
            return;
        }
        if (!isSetDefault4GSlotNeeded(slotId)) {
            loge("setDefault4GSlot: there is no need to set the lte slot");
            if (this.mEventsQ.size() > 0) {
                handleDelayedEvent();
            } else {
                this.isSet4GSlotInProgress = RESET_PROFILE;
            }
            if (IS_CARD2_CDMA_SUPPORTED && this.mSet4GSlotCompleteMsg == null) {
                logd("In auto set 4GSlot mode , makesure DDS slot same as 4G slot so set DDS to slot: " + slotId);
                HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(slotId);
            }
            if (IS_QCRIL_CROSS_MAPPING) {
                setLteServiceAbility();
                logd("set DDS to slot: " + slotId);
                HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(slotId);
            }
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, SUB_0);
            this.mSet4GSlotCompleteMsg = null;
        } else if (this.mCis[slotId] == null || RadioState.RADIO_UNAVAILABLE != this.mCis[slotId].getRadioState()) {
            if (RESET_PROFILE) {
                this.mCis[gsmOnlySlot].resetProfile(null);
                logd("setDefault4GSlot: resetProfile");
            }
            this.isSet4GSlotInProgress = true;
            if (IS_QCRIL_CROSS_MAPPING) {
                int expectedMaxCapabilitySubId = getExpectedMaxCapabilitySubId(slotId);
                if (UNKNOWN_CARD != expectedMaxCapabilitySubId) {
                    logd("setDefault4GSlot:setMaxRadioCapability,expectedMaxCapabilitySubId=" + expectedMaxCapabilitySubId);
                    setMaxRadioCapability(expectedMaxCapabilitySubId);
                } else {
                    logd("setDefault4GSlot:don't setMaxRadioCapability,response message");
                    sendSetRadioCapabilitySuccess(RESET_PROFILE);
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
                    fastSwitchDualCardsSlot(slotId, obtainMessage(EVENT_SWITCH_DUAL_CARD_SLOT));
                } else {
                    switchDualCardsSlot(slotId, obtainMessage(EVENT_SWITCH_DUAL_CARD_SLOT));
                }
            }
        } else {
            loge("setDefault4GSlot: radio is unavailable, return with failure");
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
            this.mSet4GSlotCompleteMsg = null;
            if (HwModemCapability.isCapabilitySupport(9)) {
                if (this.mEventsQ.size() == 0) {
                    this.mCis[slotId].registerForOn(this, EVENT_RADIO_ON, Integer.valueOf(slotId));
                    logd("put the event into queue to process it when radio on!");
                    this.isSet4GSlotInProgress = true;
                    this.mEventsQ.addLast(new DelayedEvent(SUB_1, slotId, this.nwModeArray));
                } else {
                    logd("there are still events unprocessed in queque with mEventsQ.size() = " + this.mEventsQ.size());
                    handleDelayedEvent();
                }
            }
        }
    }

    public void switchDualCardsSlot(int slot, Message onCompleteMsg) {
        logd(" switchDualCardsSlot   ------>>> begin");
        if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate() || !HwVSimUtils.isAllowALSwitch()) {
            logd("vsim on sub");
            if (isntFirstPowerup()) {
                setWaitingSwitchBalongSlot(RESET_PROFILE);
            }
            if (onCompleteMsg != null) {
                AsyncResult.forMessage(onCompleteMsg, Boolean.valueOf(RESET_PROFILE), null);
                loge("Switch Dual Card Slots failed!! Sending the cnf back!");
                onCompleteMsg.sendToTarget();
            }
            return;
        }
        this.mSetSdcsCompleteMsg = onCompleteMsg;
        if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
            HwFullNetwork.getInstance().switchCommrilModeIfNeeded(slot, SUB_1);
        }
        if (HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT) {
            HwForeignUsimForTelecom.getInstance().setMainSlot(slot);
        }
        Message callbackMsg = obtainMessage(EVENT_SWITCH_SIM_SLOT_CFG_DONE, Integer.valueOf(slot));
        if (slot == this.mBalongSimSlot && isBalongSimSynced()) {
            loge("switchDualCardsSlot: slot is same as mBalongSimSlot, return failure");
            if (IS_CMCC_4G_DSDX_ENABLE) {
                saveIccidsWhenAllCardsReady();
            }
            sendResponseToTarget(callbackMsg, SLOT2);
            return;
        }
        int index = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
        if (!(index == 0 || index == SUB_1)) {
            index = slot == 0 ? SUB_0 : SUB_1;
            loge("index =  " + index);
        }
        if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
            HwDsdsController.getInstance().setNeedWatingSlotSwitchDone(true);
        }
        if (this.mHas3Modem) {
            int expectSlot = slot;
            this.mCis[index].switchBalongSim(slot, slot == 0 ? SUB_1 : SUB_0, SLOT2, callbackMsg);
        } else if (slot == 0) {
            this.mCis[index].switchBalongSim(SUB_1, SLOT2, callbackMsg);
        } else {
            this.mCis[index].switchBalongSim(SLOT2, SUB_1, callbackMsg);
        }
        logd(" switchDualCardsSlot   ------>>> end");
    }

    private boolean isSetDefault4GSlotNeeded(int lteSlotId) {
        if (!HwModemCapability.isCapabilitySupport(9)) {
            return true;
        }
        int gsmOnlySlot = SUB_1;
        if (lteSlotId == 0) {
            gsmOnlySlot = SUB_1;
        } else if (SUB_1 == lteSlotId) {
            gsmOnlySlot = SUB_0;
        }
        if (lteSlotId != HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId()) {
            logd("lte slot is not the same, return true");
            return true;
        } else if (IS_QCRIL_CROSS_MAPPING) {
            boolean isSetDefault4GSlot = UNKNOWN_CARD != getExpectedMaxCapabilitySubId(lteSlotId) ? true : RESET_PROFILE;
            logd("isSetDefault4GSlotNeeded:" + isSetDefault4GSlot);
            return isSetDefault4GSlot;
        } else {
            int nwModeForLte;
            try {
                nwModeForLte = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", lteSlotId);
            } catch (SettingNotFoundException e) {
                logd("reading the preferred nework mokde failed!!");
                nwModeForLte = UNKNOWN_CARD;
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
                nwModeforGsmOnlySlot = UNKNOWN_CARD;
            }
            if (nwModeforGsmOnlySlot != this.nwModeArray[gsmOnlySlot]) {
                logd("update the current nwMode into DB for gsmOnlySlot when factory reset");
                if (IS_CARD2_CDMA_SUPPORTED) {
                    return true;
                }
                TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", gsmOnlySlot, this.nwModeArray[gsmOnlySlot]);
            }
            return RESET_PROFILE;
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
        for (int i = SUB_0; i < SIM_NUM && HwModemCapability.isCapabilitySupport(9); i += SUB_1) {
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
            this.current4GSlotBackup = SUB_0;
        }
        logd("current4GSlotBackup = " + this.current4GSlotBackup);
    }

    private void restorePreferredNwModeAndLteSlot() {
        for (int i = SUB_0; i < SIM_NUM && HwModemCapability.isCapabilitySupport(9); i += SUB_1) {
            TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i, this.mPrefNwMode[i]);
        }
        System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", this.current4GSlotBackup);
    }

    public void setDefault4GSlot(int slotId, Message responseMsg) {
        this.is4GSlotReviewNeeded = SLOT2;
        this.mUserPref4GSlot = slotId;
        if (slotId == HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId()) {
            loge("setDefault4GSlot: the default 4G slot is already " + slotId);
            sendResponseToTarget(responseMsg, SUB_0);
        } else if (this.isSet4GSlotInProgress) {
            loge("setDefault4GSlot: The setting is in progress, return failure");
            sendResponseToTarget(responseMsg, SLOT2);
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
        int numOfCardInserted = SUB_0;
        if (SUB_1 >= SIM_NUM) {
            logd("This is not a multi-sim handset");
            return RESET_PROFILE;
        }
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            refreshCardState();
            if (this.isSimInsertedArray[i]) {
                numOfCardInserted += SUB_1;
            }
        }
        if (SLOT2 > numOfCardInserted) {
            z = RESET_PROFILE;
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
                    case SUB_1 /*1*/:
                        setDefault4GSlot(slotId);
                    case SLOT2 /*2*/:
                        logd("EVENT_RADIO_ON_SET_PREF_NETWORK");
                        setPrefNetworkTypeAndStartTimer(slotId);
                    case EVENT_RADIO_UNAVAILABLE /*3*/:
                        logd("EVENT_RADIO_ON_PROCESS_SIM_STATE");
                        processSimStateChanged("IMSI", slotId);
                    default:
                        loge("unsupported event in handleDelayedEvent!");
                }
            }
        }
    }

    private void startSetPrefNetworkTimer() {
        Message message = obtainMessage(EVENT_SET_PREF_NETWORK_TIMEOUT);
        AsyncResult.forMessage(message, null, null);
        sendMessageDelayed(message, 60000);
        logd("startSetPrefNetworkTimer!");
    }

    private void setPrefNetworkTypeAndStartTimer(int slotId) {
        startSetPrefNetworkTimer();
        if (IS_QCRIL_CROSS_MAPPING) {
            setMaxRadioCapability(slotId);
        } else {
            HwModemBindingPolicyHandler.getInstance().setPreferredNetworkType(this.nwModeArray[slotId], slotId, obtainSetPreNWMessage(slotId));
        }
    }

    private synchronized void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        logd("onQueryCardTypeDone");
        if (!(ar == null || ar.result == null)) {
            this.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[SUB_0] & EVENT_SET_DATA_ALLOW_DONE;
            saveCardTypeProperties(((int[]) ar.result)[SUB_0], index.intValue());
            this.mCardTypes[index.intValue()] = ((int[]) ar.result)[SUB_0];
            HwVSimUtils.updateSimCardTypes(this.mSwitchTypes);
        }
        logd("mSwitchTypes[" + index + "] = " + this.mSwitchTypes[index.intValue()]);
        checkIfAllCardsReady();
    }

    private void saveCardTypeProperties(int cardTypeResult, int index) {
        int cardType;
        int uiccOrIcc = (cardTypeResult & 240) >> 4;
        int appType = cardTypeResult & EVENT_SET_DATA_ALLOW_DONE;
        switch (appType) {
            case SUB_1 /*1*/:
                if (uiccOrIcc != SLOT2) {
                    if (uiccOrIcc != SUB_1) {
                        cardType = UNKNOWN_CARD;
                        break;
                    } else {
                        cardType = SUB_BOTH;
                        break;
                    }
                }
                cardType = SINGLE_MODE_USIM_CARD;
                break;
            case SLOT2 /*2*/:
                cardType = SINGLE_MODE_RUIM_CARD;
                break;
            case EVENT_RADIO_UNAVAILABLE /*3*/:
                if (uiccOrIcc != SLOT2) {
                    if (uiccOrIcc != SUB_1) {
                        cardType = UNKNOWN_CARD;
                        break;
                    } else {
                        cardType = CT_NATIONAL_ROAMING_CARD;
                        break;
                    }
                }
                cardType = DUAL_MODE_TELECOM_LTE_CARD;
                break;
            default:
                cardType = UNKNOWN_CARD;
                break;
        }
        logd("uiccOrIcc :  " + uiccOrIcc + ", appType : " + appType + ", cardType : " + cardType);
        if (index == 0) {
            SystemProperties.set(CARD_TYPE_SIM1, String.valueOf(cardType));
        } else {
            SystemProperties.set(CARD_TYPE_SIM2, String.valueOf(cardType));
        }
        if (!this.mBroadcastDone && !HwHotplugController.IS_HOTSWAP_SUPPORT && IS_CHINA_TELECOM && isNoneCTcard()) {
            this.mBroadcastDone = true;
            broadcastForHwCardManager(index);
        }
    }

    private boolean isNoneCTcard() {
        boolean z = true;
        if (IS_4G_SWITCH_SUPPORTED) {
            boolean result = this.mSwitchTypes[SUB_0] == SUB_1 ? this.mSwitchTypes[SUB_1] == SUB_1 ? true : RESET_PROFILE : RESET_PROFILE;
            return result;
        }
        if (this.mSwitchTypes[HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId()] != SUB_1) {
            z = RESET_PROFILE;
        }
        return z;
    }

    private void broadcastForHwCardManager(int sub) {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        Rlog.d(this.TAG, "[broadcastForHwCardManager]");
        intent.putExtra("popupDialog", "true");
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", UNKNOWN_CARD);
    }

    private void onGetBalongSimDone(AsyncResult ar, Integer index) {
        logd("onGetBalongSimDone");
        if (ar != null && ar.result != null && ((int[]) ar.result).length == EVENT_RADIO_UNAVAILABLE) {
            int[] slots = ar.result;
            boolean isMainSlotOnVSim = RESET_PROFILE;
            logd("slot result = " + Arrays.toString(slots));
            if (slots[SUB_0] == 0 && slots[SUB_1] == SUB_1 && slots[SLOT2] == SLOT2) {
                this.mBalongSimSlot = SUB_0;
                isMainSlotOnVSim = RESET_PROFILE;
            } else if (slots[SUB_0] == SUB_1 && slots[SUB_1] == 0 && slots[SLOT2] == SLOT2) {
                this.mBalongSimSlot = SUB_1;
                isMainSlotOnVSim = RESET_PROFILE;
            } else if (slots[SUB_0] == SLOT2 && slots[SUB_1] == SUB_1 && slots[SLOT2] == 0) {
                this.mBalongSimSlot = SUB_0;
                isMainSlotOnVSim = true;
            } else if (slots[SUB_0] == SLOT2 && slots[SUB_1] == 0 && slots[SLOT2] == SUB_1) {
                this.mBalongSimSlot = SUB_1;
                isMainSlotOnVSim = true;
            } else {
                loge("onGetBalongSimDone invalid slot result");
            }
            logd("isMainSlotOnVSim = " + isMainSlotOnVSim);
            this.mHas3Modem = true;
            this.mGetBalongSimSlotDone[index.intValue()] = true;
        } else if (ar == null || ar.result == null || ((int[]) ar.result).length != SLOT2) {
            loge("onGetBalongSimDone error");
        } else {
            if (((int[]) ar.result)[SUB_1] + ((int[]) ar.result)[SUB_0] > SUB_1) {
                this.mBalongSimSlot = ((int[]) ar.result)[SUB_0] + UNKNOWN_CARD;
            } else {
                this.mBalongSimSlot = ((int[]) ar.result)[SUB_0];
            }
            this.mHas3Modem = RESET_PROFILE;
            this.mGetBalongSimSlotDone[index.intValue()] = true;
        }
        logd("mBalongSimSlot = " + this.mBalongSimSlot);
        int countGetBalongSimSlotDone = SUB_0;
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (this.mGetBalongSimSlotDone[i]) {
                countGetBalongSimSlotDone += SUB_1;
            }
        }
        if (countGetBalongSimSlotDone == SUB_1) {
            checkIfAllCardsReady();
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(SUB_0);
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
        boolean z = RESET_PROFILE;
        if (this.mUiccController.getUiccCard(slotId) == null) {
            return RESET_PROFILE;
        }
        if (this.mUiccController.getUiccCard(slotId).getCardState() != CardState.CARDSTATE_ABSENT) {
            z = true;
        }
        return z;
    }

    public boolean isAllCardsAbsent() {
        for (int i = SUB_0; i < TelephonyManager.getDefault().getPhoneCount(); i += SUB_1) {
            if (isCardPresent(i)) {
                return RESET_PROFILE;
            }
        }
        return true;
    }

    private boolean isntFirstPowerup() {
        logd(" isntFirstPowerup   ------>>>  " + true);
        return true;
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        boolean z = RESET_PROFILE;
        Rlog.d(this.TAG, "mUiccCardSwitchTypes[0] = " + this.mSwitchTypes[SUB_0] + ", mUiccCardSwitchTypes[1] = " + this.mSwitchTypes[SUB_1]);
        boolean result = RESET_PROFILE;
        if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < SLOT2) {
            Rlog.d(this.TAG, "haven't get all UiccCards done, please wait!");
            return RESET_PROFILE;
        }
        UiccCard[] uiccCards = this.mUiccController.getUiccCards();
        int length = uiccCards.length;
        for (int i = SUB_0; i < length; i += SUB_1) {
            if (uiccCards[i] == null) {
                Rlog.d(this.TAG, "haven't get all UiccCards done, pls wait!");
                return RESET_PROFILE;
            }
        }
        if (IS_CHINA_TELECOM) {
            if (!((this.mSwitchTypes[SUB_0] == EVENT_RADIO_UNAVAILABLE && this.mSwitchTypes[SUB_1] == EVENT_RADIO_UNAVAILABLE) || ((this.mSwitchTypes[SUB_0] == EVENT_RADIO_UNAVAILABLE && this.mSwitchTypes[SUB_1] == SLOT2) || ((this.mSwitchTypes[SUB_0] == SLOT2 && this.mSwitchTypes[SUB_1] == EVENT_RADIO_UNAVAILABLE) || (this.mSwitchTypes[SUB_0] == SLOT2 && this.mSwitchTypes[SUB_1] == SLOT2))))) {
                if (this.mSwitchTypes[SUB_0] == SUB_1 && this.mSwitchTypes[SUB_1] == SUB_1) {
                }
                if (this.mSwitchTypes[SUB_0] == SUB_1 && this.mSwitchTypes[SUB_1] == SUB_1 && HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT && !HwForeignUsimForTelecom.getInstance().isDomesticCard(this.mBalongSimSlot)) {
                    Rlog.d(this.TAG, "CT mode got 2 U cards, foreign U card is in main slot, return false");
                    result = RESET_PROFILE;
                }
                return result;
            }
            result = true;
            Rlog.d(this.TAG, "CT mode got 2 U cards, foreign U card is in main slot, return false");
            result = RESET_PROFILE;
            return result;
        }
        refreshCardState();
        if (IS_CMCC_4G_DSDX_ENABLE && IS_CMCC_4GSWITCH_DISABLE && ((this.subCarrierTypeArray[SUB_0].isCMCCCard() && !this.subCarrierTypeArray[SUB_1].isCMCCCard()) || (this.subCarrierTypeArray[SUB_1].isCMCCCard() && !this.subCarrierTypeArray[SUB_0].isCMCCCard()))) {
            return RESET_PROFILE;
        }
        if (this.mSwitchTypes[SUB_0] > 0 || this.isSimInsertedArray[SUB_0]) {
            if (this.mSwitchTypes[SUB_1] <= 0) {
                z = this.isSimInsertedArray[SUB_1];
            } else {
                z = true;
            }
        }
        return z;
    }

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        logd("setWaitingSwitchBalongSlot  iSetResult = " + iSetResult);
        if (IS_FULL_NETWORK_SUPPORTED_IN_HISI && HwFullNetwork.getInstance().getWaitingSwitchCommrilMode() && !iSetResult) {
            logd("mWaitingSwitchCommrilMode is true, don't setWaitingSwitchBalongSlot false now.");
            return;
        }
        this.isSet4GSlotInProgress = iSetResult;
        SystemProperties.set("gsm.dualcards.switch", iSetResult ? "true" : "false");
        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(SUB_0), null));
        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(SUB_1), null));
    }

    public boolean getWaitingSwitchBalongSlot() {
        if (HwModemCapability.isCapabilitySupport(9)) {
            logd("qcom platform pass this way");
            return RESET_PROFILE;
        }
        logd("isSet4GSlotInProgress is " + this.isSet4GSlotInProgress);
        return this.isSet4GSlotInProgress;
    }

    public boolean get4GSlotInProgress() {
        return this.isSet4GSlotInProgress;
    }

    private void switchSlotFailed() {
        logd("switchSlotFailed");
        restorePreferredNwModeAndLteSlot();
        if (IS_CMCC_4G_DSDX_ENABLE) {
            judgePreNwModeSubIdAndListForCMCC(this.current4GSlotBackup);
            if (IS_CMCC_4GSWITCH_DISABLE && (IS_VICE_WCDMA || HwModemCapability.isCapabilitySupport(EVENT_SET_PREF_NETWORK_TIMEOUT))) {
                if (IS_FULL_NETWORK_SUPPORTED_IN_HISI && HwFullNetwork.getInstance().getNeedSwitchCommrilMode()) {
                    this.needRetrySetPrefNetwork = true;
                } else {
                    setPrefNwForCmcc();
                }
            }
        }
        if (HwModemCapability.isCapabilitySupport(9)) {
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, SLOT2);
            this.mSet4GSlotCompleteMsg = null;
        }
        setWaitingSwitchBalongSlot(RESET_PROFILE);
    }

    private void switchSlotSuccess(int index) {
        logd("switchSlotSuccess");
        if (IS_CMCC_4G_DSDX_ENABLE) {
            saveIccidsWhenAllCardsReady();
        }
        HwFrameworkFactory.getHwInnerTelephonyManager().updateCrurrentPhone(index);
        if (HwModemCapability.isCapabilitySupport(9)) {
            sendResponseToTarget(this.mSet4GSlotCompleteMsg, SUB_0);
            if (this.mSet4GSlotCompleteMsg == null) {
                logd("slience set network mode done, prepare to set DDS for slot " + index);
                HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(index);
            }
            this.mSet4GSlotCompleteMsg = null;
            this.isSet4GSlotInProgress = RESET_PROFILE;
        }
        if (index != getUserSwitchDualCardSlots()) {
            this.isSet4GSlotManuallyTriggered = this.mSet4GSlotCompleteMsg == null ? RESET_PROFILE : true;
            logd("switch 4G slot from " + getUserSwitchDualCardSlots() + " to " + index + ", isSet4GSlotManuallyTriggered = " + this.isSet4GSlotManuallyTriggered);
        }
        setUserSwitchDualCardSlots(index);
        if (IS_CMCC_4GSWITCH_DISABLE && (IS_VICE_WCDMA || HwModemCapability.isCapabilitySupport(EVENT_SET_PREF_NETWORK_TIMEOUT))) {
            this.needRetrySetPrefNetwork = true;
        }
        disposeCardStatus((boolean) RESET_PROFILE);
    }

    private void handleIccATR(String strATR, Integer index) {
        logd("handleIccATR, ATR: [" + strATR + "], index:[" + index + "]");
        if (strATR == null || strATR.isEmpty()) {
            strATR = "null";
        }
        if (strATR.length() > 66) {
            logd("strATR.length() greater than PROP_VALUE_MAX");
            strATR = strATR.substring(SUB_0, 66);
        }
        if (index.intValue() == 0) {
            SystemProperties.set("gsm.sim.hw_atr", strATR);
        } else {
            SystemProperties.set("gsm.sim.hw_atr1", strATR);
        }
    }

    public void disposeCardStatus(boolean resetSwitchDualCardsFlag) {
        logd("disposeCardStatus. resetSwitchDualCardsFlag = " + resetSwitchDualCardsFlag);
        for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
            this.mSwitchTypes[i] = UNKNOWN_CARD;
            this.mGetUiccCardsStatusDone[i] = RESET_PROFILE;
            this.mGetBalongSimSlotDone[i] = RESET_PROFILE;
            this.mCardTypes[i] = UNKNOWN_CARD;
            this.mIccIds[i] = null;
            this.mFullIccIds[i] = null;
        }
        this.mAllCardsReady = RESET_PROFILE;
        if (IS_HISI_DSDX && resetSwitchDualCardsFlag) {
            logd("set mAutoSwitchDualCardsSlotDone to false");
            this.mAutoSwitchDualCardsSlotDone = RESET_PROFILE;
        }
    }

    public void disposeCardStatus(int slotID) {
        logd("disposeCardStatus slotid = " + slotID);
        if (slotID >= 0 && slotID < SIM_NUM) {
            this.mSwitchTypes[slotID] = UNKNOWN_CARD;
            this.mGetUiccCardsStatusDone[slotID] = RESET_PROFILE;
            this.mGetBalongSimSlotDone[slotID] = RESET_PROFILE;
            this.mCardTypes[slotID] = UNKNOWN_CARD;
            this.mIccIds[slotID] = null;
            this.mFullIccIds[slotID] = null;
            this.mAllCardsReady = RESET_PROFILE;
            if (IS_HISI_DSDX) {
                logd("set mAutoSwitchDualCardsSlotDone to false");
                this.mAutoSwitchDualCardsSlotDone = RESET_PROFILE;
                if (IS_CMCC_4GSWITCH_DISABLE && IS_VICE_WCDMA) {
                    Phone phone = PhoneFactory.getPhone(slotID);
                    if (phone != null) {
                        logd("disposeCardStatus: set network mode to NETWORK_MODE_GSM_ONLY");
                        phone.setPreferredNetworkType(SUB_1, null);
                    }
                }
            }
        }
    }

    public boolean isBalongSimSynced() {
        int currSlot = getUserSwitchDualCardSlots();
        Rlog.d(this.TAG, "currSlot  = " + currSlot + ", mBalongSimSlot = " + this.mBalongSimSlot);
        return currSlot == this.mBalongSimSlot ? true : RESET_PROFILE;
    }

    public int getUserSwitchDualCardSlots() {
        int subscription = SUB_0;
        try {
            subscription = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            Rlog.e(this.TAG, "Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
        }
        return subscription;
    }

    public boolean judgeDefaultSlotId4Hisi() {
        int temSub = getUserSwitchDualCardSlots();
        if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < SLOT2) {
            Rlog.d(this.TAG, "haven't get all UiccCards done, please wait!");
            return RESET_PROFILE;
        }
        boolean isCard1Present;
        boolean isCard2Present;
        UiccCard[] uiccCards = this.mUiccController.getUiccCards();
        int length = uiccCards.length;
        for (int i = SUB_0; i < length; i += SUB_1) {
            if (uiccCards[i] == null) {
                Rlog.d(this.TAG, "haven't get all UiccCards done, pls wait!");
                return RESET_PROFILE;
            }
        }
        UiccCard[] mUiccCards = this.mUiccController.getUiccCards();
        if (IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
            isCard1Present = (this.mSwitchTypes[SUB_0] != 0 || mUiccCards[SUB_0].getCardState() == CardState.CARDSTATE_PRESENT) ? true : RESET_PROFILE;
            isCard2Present = (this.mSwitchTypes[SUB_1] != 0 || mUiccCards[SUB_1].getCardState() == CardState.CARDSTATE_PRESENT) ? true : RESET_PROFILE;
        } else {
            isCard1Present = mUiccCards[SUB_0].getCardState() == CardState.CARDSTATE_PRESENT ? true : RESET_PROFILE;
            isCard2Present = mUiccCards[SUB_1].getCardState() == CardState.CARDSTATE_PRESENT ? true : RESET_PROFILE;
        }
        if (isCard1Present && !isCard2Present) {
            temSub = isBalongSimSynced() ? SUB_0 : SUB_1;
        } else if (!isCard1Present && isCard2Present) {
            temSub = isBalongSimSynced() ? SUB_1 : SUB_0;
        }
        this.default4GSlot = temSub;
        logd("isCard1Present = " + isCard1Present + ", isCard2Present = " + isCard2Present + ", default4GSlot " + this.default4GSlot);
        return true;
    }

    public boolean isSetDualCardSlotComplete() {
        return this.mSetSdcsCompleteMsg == null ? true : RESET_PROFILE;
    }

    public boolean judgeDefaultSlotId4HisiCmcc(boolean forceSwitch) {
        logd("judgeDefaultSlotId4HisiCmcc start");
        int temSub = getUserSwitchDualCardSlots();
        if (this.mUiccController == null || this.mUiccController.getUiccCards() == null || this.mUiccController.getUiccCards().length < SLOT2) {
            Rlog.d(this.TAG, "haven't get all UiccCards done, please wait!");
            return RESET_PROFILE;
        }
        int i;
        UiccCard[] uiccCards = this.mUiccController.getUiccCards();
        int length = uiccCards.length;
        for (int i2 = SUB_0; i2 < length; i2 += SUB_1) {
            if (uiccCards[i2] == null) {
                Rlog.d(this.TAG, "haven't get all UiccCards done, pls wait!");
                return RESET_PROFILE;
            }
        }
        for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
            if (this.mIccIds[i] == null) {
                logd("mIccIds[" + i + "] is null, and return");
                return RESET_PROFILE;
            }
        }
        UiccCard[] mUiccCards = this.mUiccController.getUiccCards();
        boolean isCard1Present = mUiccCards[SUB_0].getCardState() == CardState.CARDSTATE_PRESENT ? true : RESET_PROFILE;
        boolean isCard2Present = mUiccCards[SUB_1].getCardState() == CardState.CARDSTATE_PRESENT ? true : RESET_PROFILE;
        boolean z = (anySimCardChanged() || this.isPreBootCompleted) ? true : forceSwitch;
        if (this.isPreBootCompleted) {
            logd("judgeDefaultSlotId4HisiCmcc: reset isPreBootCompleted.");
            this.isPreBootCompleted = RESET_PROFILE;
        }
        logd("judgeDefaultSlotId4HisiCmcc isAnySimCardChanged = " + z);
        if (isCard1Present && !isCard2Present) {
            temSub = isBalongSimSynced() ? SUB_0 : SUB_1;
        } else if (!isCard1Present && isCard2Present) {
            temSub = isBalongSimSynced() ? SUB_1 : SUB_0;
        } else if (isCard1Present && isCard2Present) {
            if (z || !IS_HISI_DSDX) {
                boolean[] isCmccCards = new boolean[SIM_NUM];
                int cardtype1 = (this.mCardTypes[SUB_0] & 240) >> 4;
                int cardtype2 = (this.mCardTypes[SUB_1] & 240) >> 4;
                for (i = SUB_0; i < SIM_NUM; i += SUB_1) {
                    isCmccCards[i] = isCMCCCard(this.mIccIds[i]);
                }
                if (isCmccCards[SUB_0] && isCmccCards[SUB_1]) {
                    if (cardtype1 == SLOT2 && cardtype2 == SUB_1) {
                        temSub = isBalongSimSynced() ? SUB_0 : SUB_1;
                    } else if (cardtype1 == SUB_1 && cardtype2 == SLOT2) {
                        temSub = isBalongSimSynced() ? SUB_1 : SUB_0;
                    }
                } else if (isCmccCards[SUB_0]) {
                    temSub = isBalongSimSynced() ? SUB_0 : SUB_1;
                } else if (isCmccCards[SUB_1]) {
                    temSub = isBalongSimSynced() ? SUB_1 : SUB_0;
                }
                logd("cardtype1 = " + cardtype1 + ", cardtype2 = " + cardtype2 + ", isCmccCards[SUB1] " + isCmccCards[SUB_0] + ", isCmccCards[SUB2] " + isCmccCards[SUB_1]);
            } else {
                logd("judgeDefaultSlotId4HisiCmcc all sim present but none sim change ");
                return RESET_PROFILE;
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
            return UNKNOWN_CARD;
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
        int expectedMaxCapSubId = UNKNOWN_CARD;
        int cdmaCardNums = SUB_0;
        int cdmaSubId = UNKNOWN_CARD;
        if (IS_QCRIL_CROSS_MAPPING) {
            int CurrentMaxCapabilitySubId = SystemProperties.getInt(PROP_MAIN_STACK, SUB_0);
            for (int i = SUB_0; i < SIM_NUM; i += SUB_1) {
                if (this.subCarrierTypeArray[i].isCCard()) {
                    cdmaSubId = i;
                    cdmaCardNums += SUB_1;
                }
            }
            if (SUB_1 == cdmaCardNums && CurrentMaxCapabilitySubId != cdmaSubId) {
                expectedMaxCapSubId = cdmaSubId;
            } else if (SLOT2 == cdmaCardNums && CurrentMaxCapabilitySubId != ddsSubId) {
                expectedMaxCapSubId = ddsSubId;
            }
            logd("[getExpectedMaxCapabilitySubId] cdmaCardNums=" + cdmaCardNums + " expectedMaxCapSubId=" + expectedMaxCapSubId + " CurrentMaxCapabilitySubId=" + CurrentMaxCapabilitySubId);
        }
        return expectedMaxCapSubId;
    }

    private void setMaxRadioCapability(int ddsSubId) {
        ProxyController proxyController = ProxyController.getInstance();
        Phone[] phoneArr = null;
        try {
            phoneArr = PhoneFactory.getPhones();
        } catch (Exception ex) {
            logd("getPhones exception:" + ex.getMessage());
        }
        if (SubscriptionManager.isValidSubscriptionId(ddsSubId) && phoneArr != null) {
            RadioAccessFamily[] rafs = new RadioAccessFamily[phoneArr.length];
            boolean atLeastOneMatch = RESET_PROFILE;
            for (int phoneId = SUB_0; phoneId < phoneArr.length; phoneId += SUB_1) {
                int raf;
                int id = phoneArr[phoneId].getSubId();
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
        Message response = obtainMessage(SUB_BOTH, Integer.valueOf(this.expectedDDSsubId));
        AsyncResult.forMessage(response, null, null);
        response.sendToTarget();
        System.putInt(this.mContext.getContentResolver(), "switch_dual_card_slots", this.expectedDDSsubId);
        for (int slotId = SUB_0; slotId < SIM_NUM; slotId += SUB_1) {
            if (UNKNOWN_CARD != this.mSetUiccSubscriptionResult[slotId]) {
                boolean active = this.mSetUiccSubscriptionResult[slotId] == SUB_1 ? true : RESET_PROFILE;
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
            this.mNeedSetLteServiceAbility = RESET_PROFILE;
        }
    }

    private void syncNetworkTypeFromDB() {
        logd("in syncNetworkTypeFromDB");
        int pefMode0 = getNetworkTypeFromDB(SUB_0);
        int pefMode1 = getNetworkTypeFromDB(SUB_1);
        boolean firstStart = Global.getInt(this.mContext.getContentResolver(), "device_provisioned", SUB_1) == 0 ? Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", SUB_1) == 0 ? true : RESET_PROFILE : RESET_PROFILE;
        logd("pefMode0 = " + pefMode0 + ",pefMode1 = " + pefMode1 + ",firstStart =" + firstStart);
        if (!(pefMode0 == UNKNOWN_CARD || pefMode1 == UNKNOWN_CARD)) {
            if (!firstStart) {
                return;
            }
        }
        setLteServiceAbilityForQCOM(SUB_1, SystemProperties.getInt("ro.telephony.default_network", UNKNOWN_CARD));
    }

    public void setLteServiceAbilityForQCOM(int ability, int lteOnMappingMode) {
        logd("in setLteServiceAbilityForQCOM");
        getStackPhoneId();
        recordPrimaryAndSecondaryStackNetworkType(ability, lteOnMappingMode);
        this.mCis[this.mPrimaryStackPhoneId].getPreferredNetworkType(obtainMessage(EVENT_GET_PREF_NETWORK_MODE_DONE, this.mPrimaryStackPhoneId, SUB_0));
    }

    private void getStackPhoneId() {
        int i = SUB_0;
        this.mPrimaryStackPhoneId = SystemProperties.getInt(PROP_MAIN_STACK, SUB_0);
        if (this.mPrimaryStackPhoneId == 0) {
            i = SUB_1;
        }
        this.mSecondaryStackPhoneId = i;
        logd("getStackPhoneId mPrimaryStackPhoneId:" + this.mPrimaryStackPhoneId + ",mSecondaryStackPhoneId:" + this.mSecondaryStackPhoneId);
    }

    private void recordPrimaryAndSecondaryStackNetworkType(int ability, int lteOnMappingMode) {
        int primaryLteOnNetworkType;
        int primaryLteOffNetworkType;
        boolean isCmccHybird;
        int otherSub = SUB_0;
        switch (lteOnMappingMode) {
            case HwVSimEventReport.VSIM_CAUSE_TYPE_GET_NETWORK_TYPE /*9*/:
                primaryLteOnNetworkType = 9;
                primaryLteOffNetworkType = EVENT_RADIO_UNAVAILABLE;
                break;
            case SUB_BOTH /*10*/:
                primaryLteOnNetworkType = SUB_BOTH;
                primaryLteOffNetworkType = 7;
                break;
            case SINGLE_MODE_USIM_CARD /*20*/:
                primaryLteOnNetworkType = SINGLE_MODE_USIM_CARD;
                primaryLteOffNetworkType = 18;
                break;
            case HwVSimConstants.CMD_GET_SIM_STATE_VIA_SYSINFOEX /*22*/:
                primaryLteOnNetworkType = 22;
                primaryLteOffNetworkType = 21;
                break;
            default:
                primaryLteOnNetworkType = 22;
                primaryLteOffNetworkType = 21;
                break;
        }
        if (ability == SUB_1) {
            this.mPrimaryStackNetworkType = primaryLteOnNetworkType;
            this.mSecondaryStackNetworkType = SINGLE_MODE_USIM_CARD;
            if (lteOnMappingMode == 9 || lteOnMappingMode == SUB_BOTH) {
                this.mSecondaryStackNetworkType = 9;
            }
        } else {
            this.mPrimaryStackNetworkType = primaryLteOffNetworkType;
            this.mSecondaryStackNetworkType = 18;
            if (lteOnMappingMode == 9 || lteOnMappingMode == SUB_BOTH) {
                this.mSecondaryStackNetworkType = EVENT_RADIO_UNAVAILABLE;
            }
        }
        if (IS_CMCC_4GSWITCH_DISABLE) {
            isCmccHybird = isCmccHybirdBySubCarrierType();
        } else {
            isCmccHybird = RESET_PROFILE;
        }
        if (isCmccHybird) {
            if (this.mPrimaryStackPhoneId == this.default4GSlot) {
                this.mSecondaryStackNetworkType = ability == SUB_1 ? 25 : 24;
            } else {
                this.mPrimaryStackNetworkType = ability == SUB_1 ? 25 : 24;
            }
            if (this.default4GSlot == 0) {
                otherSub = SUB_1;
            }
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
        return this.subCarrierTypeArray[SUB_0].isCMCCCard() != this.subCarrierTypeArray[SUB_1].isCMCCCard() ? true : RESET_PROFILE;
    }

    private void handleSetPrimaryStackLteSwitchDone(Message msg) {
        logd("in handleSetPrimaryStackLteSwitchDone");
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            loge("set prefer network mode failed!");
            sendLteServiceSwitchResult(RESET_PROFILE);
            return;
        }
        this.mSetPrimaryStackPrefMode = msg.arg1;
        logd("setPrimaryStackPrefMode = " + this.mSetPrimaryStackPrefMode);
        this.mCis[this.mSecondaryStackPhoneId].getPreferredNetworkType(obtainMessage(EVENT_GET_PREF_NETWORK_MODE_DONE, this.mSecondaryStackPhoneId, SUB_0));
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
        int curPrefMode = UNKNOWN_CARD;
        try {
            curPrefMode = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", this.mPrimaryStackPhoneId);
        } catch (Exception e) {
            loge("rollbackPrimaryStackPrefNetworkType PREFERRED_NETWORK_MODE Exception = " + e);
        }
        logd("curPrefMode = " + curPrefMode + "mSetPrimaryStackPrefMode =" + this.mSetPrimaryStackPrefMode);
        if (curPrefMode != this.mSetPrimaryStackPrefMode) {
            this.mCis[this.mPrimaryStackPhoneId].setPreferredNetworkType(curPrefMode, obtainMessage(EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE, curPrefMode, SUB_0, Integer.valueOf(this.mPrimaryStackPhoneId)));
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
        int networkType = UNKNOWN_CARD;
        try {
            networkType = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", phoneId);
        } catch (Exception e) {
            loge("getNetworkTypeFromDB Exception = " + e + ",phoneId");
        }
        return networkType;
    }

    private void setNetworkTypeToDB(int phoneId, int prefMode) {
        try {
            TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", phoneId, prefMode);
        } catch (Exception e) {
            loge("setNetworkTypeToDB Exception = " + e + ",phoneId:" + phoneId + ",prefMode:" + prefMode);
        }
    }

    private void exchangeNetworkTypeInDB() {
        int previousNetworkTypeSub0 = getNetworkTypeFromDB(SUB_0);
        int previousNetworkTypeSub1 = getNetworkTypeFromDB(SUB_1);
        logd("exchangeNetworkTypeInDB PREFERRED_NETWORK_MODE:" + previousNetworkTypeSub0 + "," + previousNetworkTypeSub1 + "->" + previousNetworkTypeSub1 + "," + previousNetworkTypeSub0);
        setNetworkTypeToDB(SUB_0, previousNetworkTypeSub1);
        setNetworkTypeToDB(SUB_1, previousNetworkTypeSub0);
    }

    private void handleGetPrefNetworkModeDone(Message msg) {
        int subId = msg.arg1;
        int modemNetworkMode = UNKNOWN_CARD;
        AsyncResult ar = msg.obj;
        if (ar.exception == null) {
            modemNetworkMode = ((int[]) ar.result)[SUB_0];
        }
        logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
        Message response;
        if (this.mPrimaryStackPhoneId == subId) {
            response = obtainMessage(EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE, this.mPrimaryStackNetworkType, SUB_0, Integer.valueOf(this.mPrimaryStackPhoneId));
            if (modemNetworkMode == UNKNOWN_CARD || modemNetworkMode != this.mPrimaryStackNetworkType) {
                this.mCis[this.mPrimaryStackPhoneId].setPreferredNetworkType(this.mPrimaryStackNetworkType, response);
                return;
            }
            AsyncResult.forMessage(response);
            response.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's ,don't set again");
        } else if (this.mSecondaryStackPhoneId == subId) {
            response = obtainMessage(EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE, this.mSecondaryStackNetworkType, SUB_0, Integer.valueOf(this.mSecondaryStackPhoneId));
            if (modemNetworkMode == UNKNOWN_CARD || modemNetworkMode != this.mSecondaryStackNetworkType) {
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
        if (ar != null && ar.result != null && ((int[]) ar.result).length > 0 && HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[SUB_0]) {
            disposeCardStatus(index.intValue());
        }
    }

    public void fastSwitchDualCardsSlot(int expectedMainSlotId, Message onCompleteMsg) {
        logd("fastSwitchDualCardsSlot: expectedMainSlot=" + expectedMainSlotId);
        this.mSetSdcsCompleteMsg = onCompleteMsg;
        ProxyController proxyController = ProxyController.getInstance();
        int cdmaSimSlotId = getCdmaSimCardSlotId(expectedMainSlotId);
        if (isNeedSetRadioCapability(expectedMainSlotId, cdmaSimSlotId)) {
            if (!proxyController.setRadioCapability(expectedMainSlotId, cdmaSimSlotId)) {
                logd("fastSwitchDualCardsSlot: setRadioCapability fail ,response GENERIC_FAILURE");
                sendResponseToTarget(obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_DONE, Integer.valueOf(expectedMainSlotId)), SLOT2);
            }
            startFastSwithSIMSlotTimer();
            return;
        }
        logd("fastSwitchDualCardsSlot: don't need SetRadioCapability,response SUCCESS");
        sendResponseToTarget(obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_DONE, Integer.valueOf(expectedMainSlotId)), SUB_0);
    }

    private boolean isNeedSetRadioCapability(int expectedMainSlotId, int cdmaSimSlotId) {
        Phone[] mPhones = PhoneFactory.getPhones();
        if (mPhones == null) {
            logd("isNeedSetRadioCapability: mPhones is null");
            return RESET_PROFILE;
        }
        boolean same = true;
        if (SubscriptionManager.isValidSlotId(expectedMainSlotId) && mPhones[expectedMainSlotId] != null) {
            RadioCapability expectedMainSlotRC = mPhones[expectedMainSlotId].getRadioCapability();
            if (expectedMainSlotRC == null || "0".equals(expectedMainSlotRC.getLogicalModemUuid())) {
                logd("isNeedSetRadioCapability: expectedMainSlotId equals with LogicalModemUuid");
            } else {
                logd("isNeedSetRadioCapability: need switch LogicalModemUuid for expectedMainSlotId");
                same = RESET_PROFILE;
            }
        }
        if (SubscriptionManager.isValidSlotId(cdmaSimSlotId) && mPhones[cdmaSimSlotId] != null) {
            RadioCapability cdmaSimSlotRC = mPhones[cdmaSimSlotId].getRadioCapability();
            int cdmaSimSlotRaf = SUB_1;
            if (cdmaSimSlotRC != null) {
                cdmaSimSlotRaf = cdmaSimSlotRC.getRadioAccessFamily();
            }
            if (64 != (cdmaSimSlotRaf & 64)) {
                logd("isNeedSetRadioCapability: need add RAF_1xRTT for cdmaSimSlotRaf");
                same = RESET_PROFILE;
            } else {
                logd("isNeedSetRadioCapability: cdmaSimSlotRaf has RAF_1xRTT");
            }
        }
        if (!same) {
            return true;
        }
        logd("isNeedSetRadioCapability: Already in requested configuration, nothing to do.");
        return RESET_PROFILE;
    }

    private int getCdmaSimCardSlotId(int expectedMainSlotId) {
        HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();
        if (mHwTelephonyManager.isCDMASimCard(SUB_0) && mHwTelephonyManager.isCDMASimCard(SUB_1)) {
            return expectedMainSlotId;
        }
        if (mHwTelephonyManager.isCDMASimCard(SUB_0)) {
            return SUB_0;
        }
        if (mHwTelephonyManager.isCDMASimCard(SUB_1)) {
            return SUB_1;
        }
        return UNKNOWN_CARD;
    }

    private void startFastSwithSIMSlotTimer() {
        Message message = obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_TIMEOUT);
        AsyncResult.forMessage(message, null, null);
        sendMessageDelayed(message, 60000);
        logd("startFastSwithSIMSlotTimer");
    }

    private void onGetCdmaModeSideDone(AsyncResult ar, Integer index) {
        int mCdmaModemSide = SUB_0;
        CommrilMode currentCommrilModem = CommrilMode.NON_MODE;
        if (!(ar == null || ar.exception != null || ar.result == null)) {
            mCdmaModemSide = ((int[]) ar.result)[SUB_0];
        }
        if (mCdmaModemSide == 0) {
            currentCommrilModem = CommrilMode.HISI_CGUL_MODE;
        } else if (SUB_1 == mCdmaModemSide) {
            currentCommrilModem = CommrilMode.HISI_CG_MODE;
        }
        SystemProperties.set("persist.radio.commril_mode", currentCommrilModem.toString());
        logd("onGetCdmaModeSideDone mCdmaModemSide = " + mCdmaModemSide + " set currentCommrilModem=" + currentCommrilModem);
    }

    private boolean isCDMASimCard(int slotId) {
        HwTelephonyManagerInner hwTelephonyManager = HwTelephonyManagerInner.getDefault();
        return hwTelephonyManager != null ? hwTelephonyManager.isCDMASimCard(slotId) : RESET_PROFILE;
    }
}
