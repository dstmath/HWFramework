package com.android.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.telephony.AbstractRIL.HwRILReference;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.vsim.HwVSimConstants;

public class HwTelephonyBaseManagerImpl implements HwTelephonyBaseManager {
    private static HwTelephonyBaseManager mInstance = new HwTelephonyBaseManagerImpl();

    public static HwTelephonyBaseManager getDefault() {
        return mInstance;
    }

    public String responseToStringEx(int request) {
        switch (request) {
            case 1125:
                return "RIL_UNSOL_HW_SIGNAL_STRENGTH";
            case 1520:
                return "UNSOL_HW_SIM_HOTPLUG";
            case 1521:
                return "UNSOL_HW_SIM_ICCID_CHANGED";
            case 1522:
                return "RIL_UNSOL_RSRVCC_STATE_NOTIFY";
            case 3001:
                return "RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED";
            case 3003:
                return "UNSOL_HW_CS_CHANNEL_INFO_IND";
            case 3005:
                return "UNSOL_HW_ECCNUM";
            case 3006:
                return "RIL_UNSOL_HW_NETWORK_REJECT_CASE";
            case 3007:
                return "RIL_UNSOL_HW_VSIM_RDH_REQUEST";
            case 3010:
                return "RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND";
            case 3020:
                return "UNSOL_HW_UIM_LOCKCARD";
            case 3026:
                return "RIL_UNSOL_HW_DS_FLOW_INFO_REPORT";
            case 3027:
                return "RIL_UNSOL_HW_TIMER_TASK_EXPIRED";
            case 3031:
                return "RIL_UNSOL_HW_XPASS_RESELECT_INFO";
            case 3032:
                return "UNSOL_HOOK_HW_VP_STATUS";
            case 3034:
                return "RIL_UNSOL_HW_EXIST_NETWORK_INFO";
            case 3035:
                return "RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT";
            case 3037:
                return "RIL_UNSOL_HW_CA_STATE_CHANGED";
            case 3041:
                return "RIL_UNSOL_HW_IMSA_VOWIFI_MSG";
            case 3047:
                return "UNSOL_HW_CRR_CONN_IND";
            case 3051:
                return "UNSOL_HW_LIMIT_PDP_ACT_IND";
            case 3056:
                return "RIL_UNSOL_HW_CALL_ALT_SRV";
            case 3057:
                return "RIL_UNSOL_HW_LAA_STATE";
            case 3061:
                return "RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT";
            case 3125:
                return "RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH";
            default:
                return "<unknown response>";
        }
    }

    public String requestToStringEx(int request) {
        switch (request) {
            case 136:
                return "SIM_GET_ATR";
            case 518:
                return "RIL_REQUEST_HW_SET_POWER_GRADE";
            case 522:
                return "QUERY_EMERGENCY_NUMBERS";
            case 524:
                return "RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ";
            case 528:
                return "RIL_REQUEST_HW_QUERY_CARDTYPE";
            case 529:
                return "GET_CDMA_GSM_IMSI";
            case 531:
                return "RIL_REQUEST_HW_VOICE_LOOPBACK";
            case 532:
                return "GET_CDMA_CHR_INFO";
            case 535:
                return "RIL_REQUEST_HW_SET_WIFI_POWER_GRADE";
            case 536:
                return "RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2";
            case 537:
                return "RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE";
            case 2001:
                return "RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS";
            case 2005:
                return "RIL_REQUEST_HW_RESTRAT_RILD";
            case 2011:
                return "RIL_REQUEST_DATA_CONNECTION_DETACH";
            case 2012:
                return "RIL_REQUEST_DATA_CONNECTION_ATTACH";
            case 2015:
                return "RIL_REQUEST_HW_SET_LONG_MESSAGE";
            case 2017:
                return "RIL_REQUEST_HW_RESET_ALL_CONNECTIONS";
            case 2019:
                return "RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE";
            case 2022:
                return "RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG";
            case 2028:
                return "RIL_REQUEST_HW_SET_SIM_SLOT_CFG";
            case 2029:
                return "RIL_REQUEST_HW_GET_SIM_SLOT_CFG";
            case 2032:
                return "RIL_REQUEST_HW_SIM_GET_ATR";
            case 2037:
                return "RIL_REQUEST_HW_VSIM_SET_SIM_STATE";
            case 2038:
                return "RIL_REQUEST_HW_VSIM_GET_SIM_STATE";
            case 2041:
                return "RIL_REQUEST_HW_GET_SYSTEM_INFO_EX";
            case 2042:
                return "RIL_REQUEST_HW_GET_PLMN_INFO";
            case 2064:
                return "RIL_REQUEST_GET_POL_CAPABILITY";
            case 2065:
                return "RIL_REQUEST_GET_POL_LIST";
            case 2066:
                return "RIL_REQUEST_SET_POL_ENTRY";
            case 2068:
                return "RIL_REQUEST_HW_SET_ISMCOEX";
            case 2072:
                return "RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO";
            case 2073:
                return "RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO";
            case 2075:
                return "RIL_REQUEST_HW_GET_ICCID";
            case 2087:
                return "RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX";
            case 2089:
                return "RIL_REQUEST_HW_GET_DS_FLOW_INFO";
            case 2090:
                return "RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO";
            case 2092:
                return "RIL_REQUEST_HW_GET_DEVICE_VERSION";
            case 2094:
                return "RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD";
            case 2099:
                return "SET_VOICEPREFER_STATUS";
            case 2100:
                return "GET_VOICEPREFER_STATUS";
            case 2107:
                return "RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH";
            case 2108:
                return "RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION";
            case 2109:
                return "RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION";
            case 2110:
                return "RIL_REQUEST_HW_SET_AP_DS_FLOW_CONFIG";
            case 2111:
                return "RIL_REQUEST_HW_VSIM_CHECK_CARD";
            case 2112:
                return "RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA";
            case 2114:
                return "HW_SET_IMS_SWITCH";
            case 2115:
                return "HW_GET_IMS_SWITCH";
            case 2118:
                return "RIL_REQUEST_HW_SET_CDMA_MODE_SIDE";
            case 2119:
                return "RIL_REQUEST_HW_SET_UE_OPERATION_MODE";
            case 2120:
                return "RIL_REQUEST_HW_VSIM_POWER";
            case 2121:
                return "RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS";
            case 2124:
                return "RIL_REQUEST_HW_IMS_DOMAIN_CONFIG";
            case 2125:
                return "RIL_REQUEST_HW_VOWIFI_IMSA_MSG";
            case 2126:
                return "RIL_REQUEST_HW_GET_IMS_DOMAIN";
            case 2127:
                return "RIL_REQUEST_HW_GET_CDMA_MODE_SIDE";
            case 2128:
                return "RIL_REQUEST_HW_VOWIFI_UICC_AUTH";
            case 2129:
                return "RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND";
            case 2130:
                return "RIL_REQUEST_HW_SET_TIME";
            case 2131:
                return "RIL_REQUEST_HW_GET_VSIM_BASEBAND_VERSION";
            case 2132:
                return "RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY";
            case 2133:
                return "RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY";
            case 2154:
                return "RIL_REQUEST_HW_SET_PSEUDO_INFO";
            case 2155:
                return "RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID";
            case 2156:
                return "RIL_REQUEST_HW_SELECT_CSGID";
            case 2157:
                return "RIL_REQUEST_HW_SEND_LAA_CMD";
            case 2158:
                return "RIL_REQUEST_HW_GET_LAA_STATE";
            default:
                return extendRequestToStringEx(request);
        }
    }

    public String extendRequestToStringEx(int request) {
        switch (request) {
            case 331:
                return "RIL_REQUEST_HW_SIGNAL_STRENGTH";
            case 2161:
                return "RIL_REQUEST_HW_SET_RCS_SWITCH";
            case 2162:
                return "RIL_REQUEST_HW_GET_RCS_SWITCH_STATE";
            case 2163:
                return "RIL_REQUEST_HW_SET_DM_RCS_CFG";
            case 2164:
                return "RIL_REQUEST_HW_IMS_SET_DM_PCSCF_COMMON";
            case 2171:
                return "RIL_REQUEST_HW_REJ_CALL";
            case 2172:
                return "RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA";
            case 2173:
                return "RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY";
            case 2174:
                return "RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY";
            case 2177:
                return "RIL_REQUEST_HW_SIMLOCK_NW_DATA_WRITE";
            default:
                return "<unknown request>";
        }
    }

    public CommandException fromRilErrnoEx(int ril_errno) {
        switch (ril_errno) {
            case HwVSimConstants.CMD_GET_MODEMSUPPORTVSIMVER_INNER /*29*/:
                return new CommandException(Error.MISSING_RESOURCE);
            case 30:
                return new CommandException(Error.NO_SUCH_ELEMENT);
            case 401:
                return new CommandException(Error.INVALID_PARAMETER);
            default:
                Rlog.e("GSM", "Unrecognized RIL errno " + ril_errno);
                return new CommandException(Error.INVALID_RESPONSE);
        }
    }

    public HwRILReference createHwRILReference(AbstractRIL ril) {
        return new HwRILReferenceImpl((RIL) ril);
    }

    public String gsm8BitUnpackedToString(byte[] data, int offset, int length, boolean needConvertCharacter) {
        return HwGsmAlphabet.gsm8BitUnpackedToString(data, offset, length, needConvertCharacter);
    }
}
