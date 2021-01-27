package com.android.internal.telephony.vsim;

import com.huawei.android.os.SystemPropertiesEx;

public final class HwVSimConstants {
    public static final int AIRPLANE_MODE_ON = 1;
    public static final int APN_RESULT_ERROR = 1;
    public static final int APN_RESULT_SUCCESS = 0;
    public static final int APN_RESULT_TAREADYERROR = 4;
    public static final int APN_RESULT_WRONGCHALLENGE = 6;
    public static final int CARD_NUMBER_DUAL_CARD = 2;
    public static final int CARD_NUMBER_NO_CARD = 0;
    public static final int CARD_NUMBER_SINGLE_CARD = 1;
    public static final long CARD_RELOAD_TIMEOUT_FOR_DUAL_MODEM = 120000;
    public static final long CARD_RELOAD_TIMEOUT_FOR_TRI_MODEM = 180000;
    public static final long CARD_RELOAD_TIMEOUT_FOR_TRI_MODEM_DUAL_IMS = 45000;
    public static final int CLOSE_SESSION_CHANNEL = 0;
    public static final int CMD_CLEAR_TRAFFICDATA = 12;
    public static final int CMD_DISABLE_VSIM = 52;
    public static final int CMD_ENABLE_VSIM = 40;
    public static final int CMD_GET_DEVSUBMODE = 25;
    public static final int CMD_GET_PREFERREDNETWORKTYPE = 27;
    public static final int CMD_GET_SIM_STATE = 1;
    public static final int CMD_GET_SIM_STATE_VIA_SYSINFOEX = 22;
    public static final int CMD_GET_TRAFFICDATA = 14;
    public static final int CMD_RESTART_RILD_FOR_NV_MATCH = 84;
    public static final int CMD_SET_APDSFLOWCFG = 16;
    public static final int CMD_SET_APN_READY = 20;
    public static final int CMD_SET_DSFLOWNVCFG = 18;
    public static final int CMD_SWITCH_WORKMODE = 58;
    public static final boolean DEBUG = SystemPropertiesEx.getBoolean("ro.debuggable", false);
    public static final int DISABLE_PROCESSOR = 11;
    public static final int DREADY_PROCESSOR = 13;
    public static final int DWORK_PROCESSOR = 12;
    public static final String ENABLE_PARA_ACQORDER = "acqorder";
    public static final String ENABLE_PARA_APNTYPE = "apnType";
    public static final String ENABLE_PARA_BATCH_WAFER = "batchWafer";
    public static final String ENABLE_PARA_CARDTYPE = "cardType";
    public static final String ENABLE_PARA_CARD_IN_MODEM1 = "cardInModem1";
    public static final String ENABLE_PARA_CHALLENGE = "challenge";
    public static final String ENABLE_PARA_IMSI = "imsi";
    public static final String ENABLE_PARA_TAPATH = "tapath";
    public static final String ENABLE_PARA_VSIMLOC = "vsimloc";
    public static final String ENABLE_PARA_VSIM_RULE = "rule";
    public static final String ENABLE_PARA_VSIM_SPN = "spn";
    public static final int ENABLE_PROCESSOR = 1;
    public static final int EREAY_PROCESSOR = 3;
    public static final int EVENT_AIRPLANE_MODE_ON = 93;
    public static final int EVENT_CARD_POWER_OFF_DONE = 42;
    public static final int EVENT_CARD_POWER_ON_DONE = 45;
    public static final int EVENT_CARD_RELOAD_TIMEOUT = 61;
    public static final int EVENT_CLEAR_TRAFFICDATA_DONE = 13;
    public static final int EVENT_CMD_DISABLE_EXTERNAL_SIM = 98;
    public static final int EVENT_CMD_ENABLE_EXTERNAL_SIM = 94;
    public static final int EVENT_CMD_INTERRUPT = 72;
    public static final int EVENT_CMD_NOTIFY_PLUG_IN = 96;
    public static final int EVENT_CMD_NOTIFY_PLUG_OUT = 100;
    public static final int EVENT_DISABLE_EXTERNAL_SIM_DONE = 99;
    public static final int EVENT_DISABLE_VSIM_DONE = 53;
    public static final int EVENT_ENABLE_EXTERNAL_SIM_DONE = 95;
    public static final int EVENT_ENABLE_VSIM_DONE = 51;
    public static final int EVENT_ENABLE_VSIM_FINISH = 57;
    public static final int EVENT_GET_DEVSUBMODE_DONE = 26;
    public static final int EVENT_GET_ICC_STATUS_DONE = 79;
    public static final int EVENT_GET_ICC_STATUS_DONE_FOR_GET_CARD_COUNT = 85;
    public static final int EVENT_GET_PREFERREDNETWORKTYPE_DONE = 28;
    public static final int EVENT_GET_PREFERRED_NETWORK_TYPE_DONE = 48;
    public static final int EVENT_GET_RADIO_CAPABILITY = 35;
    public static final int EVENT_GET_SIM_SLOT_DONE = 54;
    public static final int EVENT_GET_SIM_STATE_DONE = 2;
    public static final int EVENT_GET_SIM_STATE_VIA_SYSINFOEX = 23;
    public static final int EVENT_GET_TRAFFICDATA_DONE = 15;
    public static final int EVENT_ICC_CHANGED = 3;
    public static final int EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT = 86;
    public static final int EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT_TIMEOUT = 87;
    public static final int EVENT_INITIAL_GET_MODEM_SUPPORT_VSIM_VER = 78;
    public static final int EVENT_INITIAL_SUBSTATE_DONE = 76;
    public static final int EVENT_INITIAL_TIMEOUT = 75;
    public static final int EVENT_INITIAL_UPDATE_CARDTYPE = 77;
    public static final int EVENT_JUDGE_RESTART_RILD_NV_MATCH = 81;
    public static final int EVENT_JUDGE_RESTART_RILD_NV_MATCH_TIMEOUT = 82;
    public static final int EVENT_NETWORK_CONNECTED = 50;
    public static final int EVENT_NETWORK_CONNECT_TIMEOUT = 71;
    public static final int EVENT_NETWORK_SCAN_COMPLETED = 24;
    public static final int EVENT_NOTIFY_PLUG_IN_DONE = 97;
    public static final int EVENT_NOTIFY_PLUG_OUT_DONE = 101;
    public static final int EVENT_QUERY_CARD_TYPE_DONE = 56;
    public static final int EVENT_RADIO_AVAILABLE = 83;
    public static final int EVENT_RADIO_POWER_OFF_DONE = 41;
    public static final int EVENT_RADIO_POWER_ON_DONE = 46;
    public static final int EVENT_SEND_CLOSE_SESSION_CONFIG_DONE = 90;
    public static final int EVENT_SEND_OPEN_SESSION_CONFIG_DONE = 88;
    public static final int EVENT_SEND_OPEN_SESSION_CONFIG_TIMEOUT = 89;
    public static final int EVENT_SEND_VSIM_DATA_TO_MODEM_DONE = 91;
    public static final int EVENT_SEND_VSIM_DATA_TO_MODEM_TIMEOUT = 92;
    public static final int EVENT_SERVICE_STATE_CHANGE = 103;
    public static final int EVENT_SET_ACTIVE_MODEM_MODE_DONE = 47;
    public static final int EVENT_SET_APDSFLOWCFG_DONE = 17;
    public static final int EVENT_SET_CDMA_MODE_SIDE_DONE = 80;
    public static final int EVENT_SET_DSFLOWNVCFG_DONE = 19;
    public static final int EVENT_SET_MAIN_SLOT_DONE = 102;
    public static final int EVENT_SET_NETWORK_RAT_AND_SRVDOMAIN_DONE = 66;
    public static final int EVENT_SET_PREFERRED_NETWORK_TYPE_DONE = 49;
    public static final int EVENT_SLOTSWITCH_INIT_DONE = 5;
    public static final int EVENT_SWITCH_SLOT_DONE = 43;
    public static final int EVENT_SWITCH_WORKMODE_DONE = 59;
    public static final int EVENT_SWITCH_WORKMODE_FINISH = 60;
    public static final int EVENT_VSIM_PLMN_SELINFO = 65;
    public static final int EWORK_PROCESSOR = 2;
    public static final String EXTRA_NETWORK_SCAN_OPEARTORINFO = "operatorInfo";
    public static final String EXTRA_NETWORK_SCAN_SUBID = "subId";
    public static final String EXTRA_NETWORK_SCAN_TYPE = "type";
    public static final int HW_NETWORK_MODE_NR_LTE = 67;
    public static final int HW_NETWORK_MODE_NR_LTE_EVDO_CDMA = 64;
    public static final int HW_NETWORK_MODE_NR_LTE_EVDO_CDMA_WCDMA_GSM = 69;
    public static final int HW_NETWORK_MODE_NR_LTE_WCDMA = 68;
    public static final int HW_NETWORK_MODE_NR_LTE_WCDMA_GSM = 65;
    public static final int HW_NETWORK_MODE_NR_ONLY = 66;
    public static final String HW_VSIM_SERVICE_STATE_CHANGED = "com.huawei.vsim.action.VSIM_REG_PLMN_CHANGED";
    public static final long INITIAL_TIMEOUT = 60000;
    public static final String INTENT_KEY_RILD_RESTART = "rild_restart";
    public static final String INTENT_KEY_VSIM = "vsim";
    public static final int INTENT_VALUE_RILD_RESTART = 1;
    public static final String INTENT_VALUE_VSIM_RELOAD = "VSIM_RELOAD";
    public static final int INVALID = -1;
    public static final int MAX_VSIM_WAIT_TIME = 90000;
    public static final int MODEM0 = 0;
    public static final int MODEM1 = 1;
    public static final int MODEM2 = 2;
    public static final int NEED_RESTART_RILD_TO_NV_CFG_MATCH = 1;
    public static final int NETWORK_SCAN_PENDING = 0;
    public static final int NETWORK_SCAN_RUNNING = 1;
    public static final int NOT_SUPPORT_VSIMCA = 0;
    public static final int NO_DESIGNATED = 0;
    public static final int NO_NEED_RESTART_RILD_TO_NV_CFG_MATCH = 0;
    public static final int NSA_STATE_2 = 2;
    public static final int NSA_STATE_5 = 5;
    public static final int OPEN_SESSION_CHANNEL = 1;
    public static final String PHONE_REASON_VSIM_ENDED = "vsimEnded";
    public static final String PHONE_REASON_VSIM_STARTED = "vsimStarted";
    public static final int PLMN_FLAG_BEGIN = 0;
    public static final int PLMN_FLAG_END = 1;
    public static final int PLMN_FLAG_INT = 2;
    public static final int PLMN_RES_INT = 3;
    public static final int PLMN_RES_NOSERVICE = 2;
    public static final int PLMN_RES_OK = 0;
    public static final int PLMN_RES_RESTRICT = 1;
    public static final int PLMN_RES_START = 4;
    public static final int PREFERRED_NETWORK_TYPE_DEFAULT = 0;
    public static final int PREFERRED_NETWORK_TYPE_MAIN = 2;
    public static final int PREFERRED_NETWORK_TYPE_SLAVE = 1;
    public static final int SREADY_PROCESSOR = 23;
    public static final int STATE_DEFAULT = 0;
    public static final int STATE_DISABLE = 5;
    public static final int STATE_DISABLE_READY = 7;
    public static final int STATE_DISABLE_WORK = 6;
    public static final int STATE_ENABLE = 2;
    public static final int STATE_ENABLE_READY = 4;
    public static final int STATE_ENABLE_WORK = 3;
    public static final int STATE_INITIAL = 1;
    public static final int STATE_RESTART_RILD = 14;
    public static final int STATE_SWITCHMODE = 11;
    public static final int STATE_SWITCHMODE_READY = 13;
    public static final int STATE_SWITCHMODE_WORK = 12;
    public static final String SUB_ID = "subId";
    public static final int SUB_ID_VSIM = 999999;
    public static final int SUPPORT_VSIMCA = 1;
    public static final int SWITCH_MODE_PROCESSOR = 21;
    public static final int SWORK_PROCESSOR = 22;
    public static final int VALUE_VSIM_USER_SWITCH_DISABLED = 0;
    public static final int VALUE_VSIM_USER_SWITCH_ENABLED = 1;
    public static final int VALUE_VSIM_USER_SWITCH_UNKNOWN = -1;
    public static final String VSIM_BUSSINESS_PERMISSION = "com.huawei.skytone.permission.VSIM_BUSSINESS";
    public static final String VSIM_CARDTYPE = "vsim_cardtype";
    public static final int VSIM_DIALUP_RESULT_FAILED = -1;
    public static final int VSIM_DIALUP_RESULT_SUCCESS = 0;
    public static final int VSIM_DISABLE_RETRY_COUNT_MAX = 3;
    public static final long VSIM_DISABLE_RETRY_TIMEOUT = 10000;
    public static final int VSIM_DUAL_MODEM_COUNT = 2;
    public static final int VSIM_EANBLE_RESULT_ICC_CA_COMMON = 21;
    public static final int VSIM_ENABLE_RESULT_BUSY = 5;
    public static final int VSIM_ENABLE_RESULT_CHECKCARDERROR = 8;
    public static final int VSIM_ENABLE_RESULT_ENABLECARDERROR = 2;
    public static final int VSIM_ENABLE_RESULT_FAIL = 3;
    public static final int VSIM_ENABLE_RESULT_ICC_CHANNEL_ERROR = 9;
    public static final int VSIM_ENABLE_RESULT_ICC_TA_COMMON = 19;
    public static final int VSIM_ENABLE_RESULT_ICC_TA_TIMEOUT = 11;
    public static final int VSIM_ENABLE_RESULT_IS_STUB_ON_BATCH_WAFER = 10;
    public static final int VSIM_ENABLE_RESULT_NO_RESERVED = 7;
    public static final int VSIM_ENABLE_RESULT_PCIE_NO_IPK = 18;
    public static final int VSIM_ENABLE_RESULT_SUCCESS = 0;
    public static final int VSIM_ENABLE_RESULT_TAERROR = 1;
    public static final int VSIM_ENABLE_RESULT_TAREADYERROR = 4;
    public static final int VSIM_ENABLE_RESULT_TEE_SERVICE_NOT_EXIST = 17;
    public static final int VSIM_ENABLE_RESULT_WRONGCHALLENGE = 6;
    public static final String VSIM_OLD_CARDTYPE = "vsim_old_cardtype";
    public static final int VSIM_OP_ENABLEVSIM = 1;
    public static final int VSIM_OP_ENABLEVSIM_FORHASH = 3;
    public static final int VSIM_OP_ENABLEVSIM_OFFLINE = 5;
    public static final int VSIM_OP_ENABLEVSIM_SETSPN_AND_RULE = 7;
    public static final int VSIM_OP_SETAPN = 2;
    public static final int VSIM_OP_SETAPN_FORHASH = 4;
    public static final int VSIM_OP_SWITCH_HARD_SIM = 6;
    public static final String VSIM_PKG_NAME = "com.huawei.skytone";
    public static final String VSIM_PKG_NAME_CHILD_THREAD = "com.huawei.skytone:";
    public static final int VSIM_POWER_OFF = 0;
    public static final int VSIM_POWER_ON = 1;
    public static final int VSIM_TRI_MODEM_COUNT = 3;
    public static final int VSIM_WORKMODE_HIGH_SPEED = 2;
    public static final int VSIM_WORKMODE_RESERVE_SUB1 = 0;
    public static final int VSIM_WORKMODE_RESERVE_SUB2 = 1;
    public static final long WAIT_FOR_NV_CFG_MATCH_TIMEOUT = 30000;
    public static final long WAIT_FOR_SEND_MUTI_CHIP_SESSION_CONFIG_TIMEOUT = 5000;
    public static final long WAIT_FOR_SEND_VSIM_DATA_TO_MODEM_TIMEOUT = 5000;
    public static final long WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT = 5000;

    public enum ProcessAction {
        PROCESS_ACTION_NONE,
        PROCESS_ACTION_ENABLE,
        PROCESS_ACTION_DISABLE,
        PROCESS_ACTION_ENABLE_OFFLINE,
        PROCESS_ACTION_DISABLE_OFFLINE,
        PROCESS_ACTION_SWITCHWORKMODE,
        PROCESS_ACTION_MAX;

        public boolean isEnableProcess() {
            return this == PROCESS_ACTION_ENABLE || this == PROCESS_ACTION_ENABLE_OFFLINE;
        }

        public boolean isDisableProcess() {
            return this == PROCESS_ACTION_DISABLE || this == PROCESS_ACTION_DISABLE_OFFLINE;
        }

        public boolean isSwitchModeProcess() {
            return this == PROCESS_ACTION_SWITCHWORKMODE;
        }
    }

    public enum ProcessType {
        PROCESS_TYPE_NONE,
        PROCESS_TYPE_SWAP,
        PROCESS_TYPE_CROSS,
        PROCESS_TYPE_DIRECT,
        PROCESS_TYPE_MAX;

        public boolean isSwapProcess() {
            return this == PROCESS_TYPE_SWAP;
        }

        public boolean isCrossProcess() {
            return this == PROCESS_TYPE_CROSS;
        }

        public boolean isDirectProcess() {
            return this == PROCESS_TYPE_DIRECT;
        }
    }

    public enum ProcessState {
        PROCESS_STATE_NONE,
        PROCESS_STATE_WORK,
        PROCESS_STATE_READY,
        PROCESS_STATE_MAX;

        public boolean isWorkProcess() {
            return this == PROCESS_STATE_WORK;
        }

        public boolean isReadyProcess() {
            return this == PROCESS_STATE_READY;
        }
    }

    public static class EnableParam {
        public String acqOrder;
        public int apnType;
        public int cardInModem1;
        public int cardType;
        public String challenge;
        public String imsi;
        public int operation;
        public int supportVsimCa;
        public String taPath;
        public int vsimLoc;

        public EnableParam(String imsi2, int cardType2, int apnType2, String acqOrder2, String challenge2, int operation2, String tapath, int vsimloc, int cardInModem12, int supportVsimCa2) {
            this.imsi = imsi2;
            this.cardType = cardType2;
            this.apnType = apnType2;
            this.acqOrder = acqOrder2;
            this.challenge = challenge2;
            this.operation = operation2;
            this.taPath = tapath;
            this.vsimLoc = vsimloc;
            this.cardInModem1 = cardInModem12;
            this.supportVsimCa = supportVsimCa2;
        }
    }

    public static class ApnParams {
        public int apnType;
        public int cardType;
        public String challenge;
        public String imsi;
        public boolean isForHash;
        public int supportVSimCa;
        public String taPath;

        public String toString() {
            return "ApnParams{, cardType=" + this.cardType + ", apnType=" + this.apnType + ", supportVSimCa=" + this.supportVSimCa + '}';
        }
    }

    public static class WorkModeParam {
        public boolean isHotplug;
        public int oldMode;
        public int workMode;

        public WorkModeParam(int workMode2, int oldMode2, boolean isHotplug2) {
            this.workMode = workMode2;
            this.oldMode = oldMode2;
            this.isHotplug = isHotplug2;
        }
    }
}
