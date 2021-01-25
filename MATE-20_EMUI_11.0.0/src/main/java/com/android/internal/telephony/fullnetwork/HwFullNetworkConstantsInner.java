package com.android.internal.telephony.fullnetwork;

import com.huawei.android.telephony.TelephonyManagerEx;

public class HwFullNetworkConstantsInner {
    public static final int CARD_TYPE_DUAL_MODE = 3;
    public static final int CARD_TYPE_NO_SIM = 0;
    public static final String CARD_TYPE_SIM1 = "gsm.sim1.type";
    public static final String CARD_TYPE_SIM2 = "gsm.sim2.type";
    public static final int CARD_TYPE_SINGLE_CDMA = 2;
    public static final int CARD_TYPE_SINGLE_GSM = 1;
    public static final String CMCC_UNBIND_FLAG_BIND = "BIND";
    public static final String CMCC_UNBIND_FLAG_INVALID = "INVALID";
    public static final String CMCC_UNBIND_FLAG_UNBIND = "UNBIND";
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    public static final int DEFAULT_VALUE = 0;
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_TELECOM_LTE_CARD = 43;
    public static final int DUAL_MODE_UG_CARD = 50;
    public static final int EVENT_CHECK_MAIN_SLOT = 201;
    public static final int EVENT_CHECK_MAIN_SLOT_FOR_MDM = 204;
    public static final int EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR = 203;
    public static final int EVENT_CHECK_NETWORK_TYPE = 205;
    public static final int EVENT_CHECK_STATE_BASE = 300;
    public static final int EVENT_CHIP_HISI1_BASE = 1100;
    public static final int EVENT_CHIP_HISI2_BASE = 1200;
    public static final int EVENT_CHIP_HISI_BASE = 1000;
    public static final int EVENT_CHIP_QCOM1_BASE = 2300;
    public static final int EVENT_CHIP_QCOM2_BASE = 2400;
    public static final int EVENT_CHIP_QCOM_BASE = 2000;
    public static final int EVENT_CHIP_QCOM_CHECK_STATE_BASE = 2100;
    public static final int EVENT_CHIP_QCOM_SET_STATE_BASE = 2200;
    public static final int EVENT_CMCC_SET_NETWOR_DONE = 1011;
    public static final int EVENT_DEFAULT_STATE_BASE = 200;
    public static final int EVENT_DELAYED_SET_PREF_NETWORK = 2101;
    public static final int EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC = 207;
    public static final int EVENT_GET_BALONG_SIM_DONE = 1006;
    public static final int EVENT_GET_CDMA_MODE_SIDE_DONE = 1007;
    public static final int EVENT_GET_ICCID_DONE = 1009;
    public static final int EVENT_GET_ICC_STATUS_DONE = 1012;
    public static final int EVENT_GET_MAIN_SLOT = 301;
    public static final int EVENT_GET_PREF_NETWORK_DONE = 2102;
    public static final int EVENT_GET_PREF_NETWORK_MODE_DONE = 2003;
    public static final int EVENT_ICC_GET_ATR_DONE = 1004;
    public static final int EVENT_ICC_STATUS_CHANGED = 1001;
    public static final int EVENT_INIT_STATE_BASE = 100;
    public static final int EVENT_QUERY_CARD_TYPE_DONE = 1005;
    public static final int EVENT_RADIO_AVAILABLE = 1003;
    public static final int EVENT_RADIO_ON = 2203;
    public static final int EVENT_RADIO_ON_PROCESS_SIM_STATE = 2001;
    public static final int EVENT_RADIO_ON_SET_PREF_NETWORK = 2002;
    public static final int EVENT_RADIO_UNAVAILABLE = 1002;
    public static final int EVENT_RESET_OOS_FLAG = 2008;
    public static final int EVENT_RESTART_RILD_FOR_NV = 1013;
    public static final int EVENT_SET_DATA_ALLOW_DONE = 302;
    public static final int EVENT_SET_LTE_SERVICE_ABILITY = 2004;
    public static final int EVENT_SET_MAIN_SLOT = 202;
    public static final int EVENT_SET_MAIN_SLOT_DONE = 401;
    public static final int EVENT_SET_MAIN_SLOT_TIMEOUT = 402;
    public static final int EVENT_SET_NETWORK_TYPE = 206;
    public static final int EVENT_SET_PREF_NETWORK_TIMEOUT = 2201;
    public static final int EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE = 2005;
    public static final int EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE = 2007;
    public static final int EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE = 2006;
    public static final int EVENT_SET_STATE_BASE = 400;
    public static final int EVENT_SIM_HOTPLUG = 1008;
    public static final int EVENT_STATE_MACHINE_BASE = 0;
    public static final int EVENT_SWITCH_SLOT_TYPE_DONE = 2205;
    public static final int EVENT_TRANS_TO_DEFAULT = 403;
    public static final int EVENT_UNSOL_RESTART_RILD_STATUS = 2206;
    public static final int EVENT_VOICE_CALL_ENDED = 1010;
    public static final int EXTRA_VALUE_INVALID = -1;
    public static final int HW_SWITCH_SLOT_AUTO = 0;
    public static final int HW_SWITCH_SLOT_DONE = 1;
    public static final int HW_SWITCH_SLOT_FAIL = -1;
    public static final int HW_SWITCH_SLOT_MANUL = 1;
    public static final int HW_SWITCH_SLOT_SIM_DONE = 2;
    public static final int HW_SWITCH_SLOT_START = 0;
    public static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    public static final int ICCID_LEN_MINIMUM = 7;
    public static final int ICC_CARD = 1;
    public static final String IF_NEED_SET_RADIO_CAP = "if_need_set_radio_cap";
    public static final int INVALID = -1;
    public static final int INVALID_NETWORK_MODE = -1;
    public static final String KEY_CMCC_UNBIND_FLAG = "cmcc_unbind_flag";
    public static final int LTE_SERVICE_OFF = 0;
    public static final int LTE_SERVICE_ON = 1;
    public static final String MASTER_PASSWORD = HwFullNetworkManager.getInstance().getMasterPassword();
    public static final int MCCMNC_LEN_MINIMUM = 5;
    public static final int MESSAGE_PENDING_DELAY = 500;
    public static final int MODEM0 = 0;
    public static final int MODEM1 = 1;
    public static final int MODEM2 = 2;
    public static final int MSG_RETRY_CHANGE_PREF_NETWORK = 2204;
    public static final int MSG_RETRY_SET_DEFAULT_LTESLOT = 2202;
    public static final String NET_WORK_MODE_HIDE = "carrier_networkmode_hide_bool";
    public static final int NO_NEED_SET_RADIO_CAP = 1;
    public static final String NR_HIDE_PREF_NETWORK_MODE = "hide_network_mode_pref_5G";
    public static final String NR_SWITCH_AVAILABLE = "switch_nr_available_bool";
    public static final int RETRY_MAX_TIME = 20;
    public static final int REVIEW_4G_MODE_AUTO = 1;
    public static final int REVIEW_4G_MODE_FIX = 2;
    public static final int REVIEW_4G_MODE_NONE = 0;
    public static final int SERVICE_OFF = 0;
    public static final int SERVICE_ON = 1;
    public static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    public static final int SUB_0 = 0;
    public static final int SUB_1 = 1;
    public static final int SUB_BOTH = 10;
    public static final int SUB_ERROR = -1;
    public static final int SUB_NO_CMCC = 11;
    public static final int TIME_SET_MAIN_SLOT_TIMEOUT = 60000;
    public static final int TIME_SET_PREF_NETWORK_TIMEOUT = 60000;
    public static final int UICC_CARD = 2;
    public static final int UNKNOWN_CARD = -1;
    public static final String USER_DEFAULT_SUBSCRIPTION = "user_default_sub";

    public enum CommrilMode {
        NON_MODE,
        HISI_CGUL_MODE,
        HISI_CG_MODE,
        HISI_VSIM_MODE
    }

    public enum HotplugState {
        STATE_PLUG_OUT,
        STATE_PLUG_IN
    }

    public enum SubType {
        CARRIER_PREFERRED,
        CARRIER,
        FOREIGN_CARRIER_PREFERRED,
        FOREIGN_CARRIER,
        LOCAL_CARRIER,
        ERROR
    }

    public enum SubCarrierType {
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
        OTHER,
        ABSENT,
        CARRIER_FOREIGN_AIS_USIM,
        CARRIER_FOREIGN_SMART_USIM,
        CARRIER_FOREIGN_MTN_USIM;

        /* access modifiers changed from: package-private */
        public boolean isUCard() {
            return this == CARRIER_CMCC_USIM || this == CARRIER_CMCC_SIM || this == CARRIER_CU_USIM || this == CARRIER_CU_SIM || this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_SIM;
        }

        /* access modifiers changed from: package-private */
        public boolean isCCard() {
            return this == CARRIER_CT_CSIM || this == CARRIER_CT_RUIM || this == CARRIER_FOREIGN_CSIM || this == CARRIER_FOREIGN_RUIM;
        }

        /* access modifiers changed from: package-private */
        public boolean is3G4GCard() {
            return this == CARRIER_CMCC_USIM || this == CARRIER_CU_USIM || this == CARRIER_CT_CSIM || this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_CSIM;
        }

        /* access modifiers changed from: package-private */
        public boolean is2GCard() {
            return this == CARRIER_CMCC_SIM || this == CARRIER_CU_SIM || this == CARRIER_CT_RUIM || this == CARRIER_FOREIGN_SIM || this == CARRIER_FOREIGN_RUIM;
        }

        /* access modifiers changed from: package-private */
        public boolean isCMCCCard() {
            return this == CARRIER_CMCC_SIM || this == CARRIER_CMCC_USIM;
        }

        /* access modifiers changed from: package-private */
        public boolean isCUCard() {
            return this == CARRIER_CU_USIM || this == CARRIER_CU_SIM;
        }

        /* access modifiers changed from: package-private */
        public boolean isCTCard() {
            return this == CARRIER_CT_CSIM || this == CARRIER_CT_RUIM;
        }

        /* access modifiers changed from: package-private */
        public boolean isReCheckFail() {
            return this == CARRIER_FOREIGN_USIM || this == CARRIER_FOREIGN_SIM || this == OTHER || this == ABSENT;
        }
    }

    public static class DelayedEvent {
        int id;
        int[] networkModeArray;
        int slotId;

        DelayedEvent(int id2, int slotId2, int[] networkModeArray2) {
            this.id = id2;
            this.slotId = slotId2;
            this.networkModeArray = networkModeArray2;
        }
    }
}
