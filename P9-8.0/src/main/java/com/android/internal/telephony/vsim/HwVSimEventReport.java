package com.android.internal.telephony.vsim;

import android.content.Context;
import android.content.Intent;
import android.telephony.Rlog;
import android.util.Log;

public class HwVSimEventReport {
    private static final String CARDPRESENT_INFO = "cardpresent_info";
    private static final String CARD_TYPE = "card_type";
    private static final String CAUSE_TYPE = "cause_type";
    private static final String CHR_EVENT_ID = "event_id";
    private static final String CHR_MODULE_ID = "module_id";
    private static final String CHR_REPORT_ACTION = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    private static final String CHR_REPORT_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static boolean HWDBG = false;
    private static boolean HWFLOW = false;
    static final String LOG_TAG = "VSimEventReport";
    private static final String PROCESS_TYPE = "process_type";
    private static final String PSREG_TIME = "psreg_time";
    private static final String RESULT_TYPE = "result_type";
    private static final String SAVED_COMMRILMODE = "saved_commrilmode";
    private static final String SAVED_MAINSLOT = "saved_mainslot";
    private static final String SAVED_NETWORKMODE = "saved_networkmode";
    private static final String SIM_MODE = "sim_mode";
    private static final String SIM_OPERATOR = "sim_operator";
    private static final String SLOTS_TABLE = "slots_table";
    public static final int VSIM_CARD_TYPE_INVALID = -1;
    public static final int VSIM_CARD_TYPE_MAIN = 1;
    public static final int VSIM_CARD_TYPE_SUB = 2;
    public static final int VSIM_CAUSE_TYPE_ACTIVE_MODEM_MODE = 8;
    public static final int VSIM_CAUSE_TYPE_CARD_POWER_OFF = 4;
    public static final int VSIM_CAUSE_TYPE_CARD_POWER_ON = 7;
    public static final int VSIM_CAUSE_TYPE_DISABLE_VSIM_DONE = 14;
    public static final int VSIM_CAUSE_TYPE_ENABLE_VSIM_DONE = 12;
    public static final int VSIM_CAUSE_TYPE_ENABLE_VSIM_FINISH = 13;
    public static final int VSIM_CAUSE_TYPE_GET_NETWORK_TYPE = 9;
    public static final int VSIM_CAUSE_TYPE_GET_SIM_STATE = 3;
    public static final int VSIM_CAUSE_TYPE_INVALID = -1;
    public static final int VSIM_CAUSE_TYPE_NETWORK_CONNECTED = 16;
    public static final int VSIM_CAUSE_TYPE_PLMN_SELINFO = 15;
    public static final int VSIM_CAUSE_TYPE_RADIO_POWER_OFF = 2;
    public static final int VSIM_CAUSE_TYPE_RADIO_POWER_ON = 11;
    public static final int VSIM_CAUSE_TYPE_SET_NETWORK_TYPE = 10;
    public static final int VSIM_CAUSE_TYPE_SET_TEE_DATA = 6;
    public static final int VSIM_CAUSE_TYPE_SWITCH_SLOT = 5;
    public static final int VSIM_CAUSE_TYPE_UNKNOWN = 1;
    public static final int VSIM_PROCESS_TYPE_DA = 11;
    public static final int VSIM_PROCESS_TYPE_DB = 12;
    public static final int VSIM_PROCESS_TYPE_DC = 13;
    public static final int VSIM_PROCESS_TYPE_EA = 1;
    public static final int VSIM_PROCESS_TYPE_EB = 2;
    public static final int VSIM_PROCESS_TYPE_ED = 4;
    public static final int VSIM_RESULT_TYPE_FAIL = 2;
    public static final int VSIM_RESULT_TYPE_SUCCESS = 1;
    private static final String WORK_MODE = "work_mode";
    private Context mContext;

    public static class VSimEventInfo {
        private int mCardPresent;
        private int mCardType;
        private int mCauseType;
        private int mProcessType;
        private int mPsRegTime;
        private int mResultType;
        private int mSavedCommrilMode;
        private int mSavedMainSlot;
        private int mSavedNetworkMode;
        private int mSimMode;
        private String mSimOperator;
        private int mSlotsTable;
        private int mWorkMode;

        public void setCauseType(int causeType) {
            this.mCauseType = causeType;
        }

        public int getCauseType() {
            return this.mCauseType;
        }

        public void setResultType(int resultType) {
            this.mResultType = resultType;
        }

        public int getResultType() {
            return this.mResultType;
        }

        public void setPocessType(int processType) {
            this.mProcessType = processType;
        }

        public int getProcessType() {
            return this.mProcessType;
        }

        public void setCardType(int cardType) {
            this.mCardType = cardType;
        }

        public int getCardType() {
            return this.mCardType;
        }

        public void setPsRegTime(int psRegTime) {
            this.mPsRegTime = psRegTime;
        }

        public int getPsRegTime() {
            return this.mPsRegTime;
        }

        public void setSimOperator(String simOperator) {
            this.mSimOperator = simOperator;
        }

        public String getSimOperator() {
            return this.mSimOperator;
        }

        public void setSavedCommrilMode(int savedCommrilMode) {
            this.mSavedCommrilMode = savedCommrilMode;
        }

        public int getSavedCommrilMode() {
            return this.mSavedCommrilMode;
        }

        public void setSavedMainSlot(int savedMainSlot) {
            this.mSavedMainSlot = savedMainSlot;
        }

        public int getSavedMainSlot() {
            return this.mSavedMainSlot;
        }

        public void setSimMode(int simMode) {
            this.mSimMode = simMode;
        }

        public int getSimMode() {
            return this.mSimMode;
        }

        public void setSlotsTable(int slotsTable) {
            this.mSlotsTable = slotsTable;
        }

        public int getSlotsTable() {
            return this.mSlotsTable;
        }

        public void setSavedNetworkMode(int savedNetworkMode) {
            this.mSavedNetworkMode = savedNetworkMode;
        }

        public int getSavedNetworkMode() {
            return this.mSavedNetworkMode;
        }

        public void setCardPresent(int cardPresent) {
            this.mCardPresent = cardPresent;
        }

        public int getCardPresent() {
            return this.mCardPresent;
        }

        public void setWorkMode(int workMode) {
            this.mWorkMode = workMode;
        }

        public int getWorkMode() {
            return this.mWorkMode;
        }

        public String toString() {
            return "[cause=" + this.mCauseType + ", result=" + this.mResultType + ", process=" + this.mProcessType + ", card=" + this.mCardType + ", ps reg time=" + this.mPsRegTime + ", sim operator=" + this.mSimOperator + ", saved commril mode=" + this.mSavedCommrilMode + ", saved main slot=" + this.mSavedMainSlot + ", simmode=" + this.mSimMode + ", slots table=" + this.mSlotsTable + ", saved network mode=" + this.mSavedNetworkMode + ", cardpresent info=" + this.mCardPresent + ", work mode=" + this.mWorkMode + "]";
        }
    }

    public static class VSimEventInfoUtils {
        public static void setCauseType(VSimEventInfo eventInfo, int causeType) {
            if (eventInfo != null) {
                eventInfo.setCauseType(causeType);
            }
        }

        public static void setResultType(VSimEventInfo eventInfo, int resultType) {
            if (eventInfo != null) {
                eventInfo.setResultType(resultType);
            }
        }

        public static void setPocessType(VSimEventInfo eventInfo, int processType) {
            if (eventInfo != null) {
                eventInfo.setPocessType(processType);
            }
        }

        public static void setCardType(VSimEventInfo eventInfo, int cardType) {
            if (eventInfo != null) {
                eventInfo.setCardType(cardType);
            }
        }

        public static void setPsRegTime(VSimEventInfo eventInfo, long psRegTime) {
            if (eventInfo != null) {
                eventInfo.setPsRegTime((int) psRegTime);
            }
        }

        public static void setSimOperator(VSimEventInfo eventInfo, String simOperator) {
            if (eventInfo != null) {
                eventInfo.setSimOperator(simOperator);
            }
        }

        public static void setSavedCommrilMode(VSimEventInfo eventInfo, int savedCommrilMode) {
            if (eventInfo != null) {
                eventInfo.setSavedCommrilMode(savedCommrilMode);
            }
        }

        public static void setSavedMainSlot(VSimEventInfo eventInfo, int savedMainSlot) {
            if (eventInfo != null) {
                eventInfo.setSavedMainSlot(savedMainSlot);
            }
        }

        public static void setSimMode(VSimEventInfo eventInfo, int simMode) {
            if (eventInfo != null) {
                eventInfo.setSimMode(simMode);
            }
        }

        public static void setSlotsTable(VSimEventInfo eventInfo, int slotsTable) {
            if (eventInfo != null) {
                eventInfo.setSlotsTable(slotsTable);
            }
        }

        public static void setSavedNetworkMode(VSimEventInfo eventInfo, int savedNetworkMode) {
            if (eventInfo != null) {
                eventInfo.setSavedNetworkMode(savedNetworkMode);
            }
        }

        public static void setCardPresent(VSimEventInfo eventInfo, int cardPresent) {
            if (eventInfo != null) {
                eventInfo.setCardPresent(cardPresent);
            }
        }

        public static void setWorkMode(VSimEventInfo eventInfo, int workMode) {
            if (eventInfo != null) {
                eventInfo.setWorkMode(workMode);
            }
        }
    }

    static {
        boolean z;
        boolean z2 = true;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(LOG_TAG, 3);
        } else {
            z = false;
        }
        HWDBG = z;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z2 = Log.isLoggable(LOG_TAG, 4);
            } else {
                z2 = false;
            }
        }
        HWFLOW = z2;
    }

    public HwVSimEventReport(Context context) {
        if (HWDBG) {
            logd("ctor - " + this);
        }
        this.mContext = context;
    }

    private void logd(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, s);
    }

    public void reportEvent(VSimEventInfo eventInfo) {
        if (HWFLOW) {
            logi("reportEvent, event info: " + eventInfo);
        }
        reportChr(eventInfo);
    }

    private void reportChr(VSimEventInfo eventInfo) {
        if (this.mContext == null) {
            if (HWDBG) {
                logd("no context");
            }
        } else if (eventInfo == null) {
            if (HWDBG) {
                logd("no event info");
            }
        } else if (isChrScenarioValid(eventInfo.getResultType(), eventInfo.getProcessType())) {
            Intent intent = new Intent("com.huawei.android.chr.action.ACTION_REPORT_CHR");
            intent.putExtra("module_id", 13000);
            intent.putExtra(CAUSE_TYPE, toChrCauseType(eventInfo.getCauseType()));
            intent.putExtra(RESULT_TYPE, toChrResultType(eventInfo.getResultType()));
            intent.putExtra(PROCESS_TYPE, toChrProcessType(eventInfo.getProcessType()));
            intent.putExtra(CARD_TYPE, toChrCardType(eventInfo.getCardType()));
            intent.putExtra(PSREG_TIME, msToSec(eventInfo.getPsRegTime()));
            intent.putExtra(SIM_OPERATOR, eventInfo.getSimOperator());
            intent.putExtra(SAVED_COMMRILMODE, eventInfo.getSavedCommrilMode());
            intent.putExtra(SAVED_MAINSLOT, eventInfo.getSavedMainSlot());
            intent.putExtra(SIM_MODE, eventInfo.getSimMode());
            intent.putExtra(SLOTS_TABLE, eventInfo.getSlotsTable());
            intent.putExtra(SAVED_NETWORKMODE, eventInfo.getSavedNetworkMode());
            intent.putExtra(CARDPRESENT_INFO, eventInfo.getCardPresent());
            intent.putExtra(WORK_MODE, eventInfo.getWorkMode());
            this.mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        } else {
            if (HWDBG) {
                logd("scenario not valid");
            }
        }
    }

    private static boolean isChrScenarioValid(int resultType, int processType) {
        Boolean isValid = Boolean.valueOf(true);
        switch (processType) {
            case 1:
            case 2:
            case 4:
                break;
            case 11:
            case 12:
            case 13:
                if (resultType == 1) {
                    isValid = Boolean.valueOf(false);
                    break;
                }
                break;
            default:
                isValid = Boolean.valueOf(false);
                break;
        }
        return isValid.booleanValue();
    }

    private static int toChrResultType(int resultType) {
        switch (resultType) {
            case 1:
                return 1;
            case 2:
                return 0;
            default:
                return 0;
        }
    }

    private static int toChrProcessType(int processType) {
        switch (processType) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 11:
                return 3;
            case 12:
                return 4;
            case 13:
                return 5;
            default:
                return 0;
        }
    }

    private static int toChrCauseType(int causeType) {
        switch (causeType) {
            case 2:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_RADIO_POWER_OFF;
            case 3:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_GET_SIM_STATE;
            case 4:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_CARD_POWER_OFF;
            case 5:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_SWITCH_SLOT;
            case 6:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_SET_TEE_DATA;
            case 7:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_CARD_POWER_ON;
            case 8:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_ACTIVE_MODEM_MODE;
            case 9:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_GET_NETWORK_TYPE;
            case 10:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_SET_NETWORK_TYPE;
            case 11:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_RADIO_POWER_ON;
            case 12:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_ENABLE_VSIM_DONE;
            case 13:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_ENABLE_VSIM_FINISH;
            case 14:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_DISABLE_VSIM_DONE;
            case 15:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_PLMN_SELINFO;
            case 16:
                return HwChrVSimConstants.CHR_VSIM_CAUSE_TYPE_NETWORK_CONNECTED;
            default:
                return 13000;
        }
    }

    private static int toChrCardType(int cardType) {
        switch (cardType) {
            case 1:
                return 0;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    private int msToSec(int ms) {
        return ms / HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
    }
}
