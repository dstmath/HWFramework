package com.android.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.telephony.AbstractRIL;
import com.android.internal.telephony.CommandException;

public class HwTelephonyBaseManagerImpl implements HwTelephonyBaseManager {
    private static HwTelephonyBaseManager mInstance = new HwTelephonyBaseManagerImpl();

    public static HwTelephonyBaseManager getDefault() {
        return mInstance;
    }

    public String responseToStringEx(int request) {
        if (request == 3026) {
            return "RIL_UNSOL_HW_DS_FLOW_INFO_REPORT";
        }
        if (request == 3027) {
            return "RIL_UNSOL_HW_TIMER_TASK_EXPIRED";
        }
        if (request == 3031) {
            return "RIL_UNSOL_HW_XPASS_RESELECT_INFO";
        }
        if (request == 3032) {
            return "UNSOL_HOOK_HW_VP_STATUS";
        }
        if (request == 3034) {
            return "RIL_UNSOL_HW_EXIST_NETWORK_INFO";
        }
        if (request == 3035) {
            return "RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT";
        }
        if (request == 3047) {
            return "UNSOL_HW_CRR_CONN_IND";
        }
        if (request == 3048) {
            return "UNSOL_HW_DSDS_MODE_STATE_IND";
        }
        switch (request) {
            case 1125:
                return "RIL_UNSOL_HW_SIGNAL_STRENGTH";
            case 3001:
                return "RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED";
            case 3003:
                return "UNSOL_HW_CS_CHANNEL_INFO_IND";
            case 3010:
                return "RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND";
            case 3020:
                return "UNSOL_HW_UIM_LOCKCARD";
            case 3037:
                return "RIL_UNSOL_HW_CA_STATE_CHANGED";
            case 3041:
                return "RIL_UNSOL_HW_IMSA_VOWIFI_MSG";
            case 3051:
                return "UNSOL_HW_LIMIT_PDP_ACT_IND";
            case 3074:
                return "RIL_UNSOL_HW_256QAM_STATE_IND";
            case 3117:
                return "RIL_UNSOL_TX_POWER_STATUS";
            case 3122:
                return "RIL_UNSOL_RRC_CONNECTION_STATE";
            case 3125:
                return "RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH";
            default:
                switch (request) {
                    case 1520:
                        return "UNSOL_HW_SIM_HOTPLUG";
                    case 1521:
                        return "UNSOL_HW_SIM_ICCID_CHANGED";
                    case 1522:
                        return "RIL_UNSOL_RSRVCC_STATE_NOTIFY";
                    case 1523:
                        return "RIL_UNSOL_HW_PROPERTY_CHANGED_IND";
                    default:
                        switch (request) {
                            case 3005:
                                return "UNSOL_HW_ECCNUM";
                            case 3006:
                                return "RIL_UNSOL_HW_NETWORK_REJECT_CASE";
                            case 3007:
                                return "RIL_UNSOL_HW_VSIM_RDH_REQUEST";
                            default:
                                switch (request) {
                                    case 3056:
                                        return "RIL_UNSOL_HW_CALL_ALT_SRV";
                                    case 3057:
                                        return "RIL_UNSOL_HW_LAA_STATE";
                                    case 3058:
                                        return "UNSOL_HW_NCFG_FINISHED_IND";
                                    case 3059:
                                        return "RIL_UNSOL_HW_MIMO_STATE_IND";
                                    case 3060:
                                        return "RIL_UNSOL_HW_RESTART_RILD_IND";
                                    case 3061:
                                        return "RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT";
                                    case 3062:
                                        return "RIL_UNSOL_HW_ANTIFAKE_BASESTATION_CHANGED";
                                    case 3063:
                                        return "RIL_UNSOL_UL_FREQ_BAND_WIDTH_IND";
                                    default:
                                        return "<unknown response>" + request;
                                }
                        }
                }
        }
    }

    public String requestToStringEx(int request) {
        switch (request) {
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
            case 532:
                return "GET_CDMA_CHR_INFO";
            case 535:
                return "RIL_REQUEST_HW_SET_WIFI_POWER_GRADE";
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
        if (request == 59) {
            return "RIL_REQUEST_OEM_HOOK_RAW";
        }
        if (request == 60) {
            return "RIL_REQUEST_OEM_HOOK_STRINGS";
        }
        if (request == 331) {
            return "RIL_REQUEST_HW_SIGNAL_STRENGTH";
        }
        if (request == 533) {
            return "RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE";
        }
        if (request == 2088) {
            return "RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE";
        }
        if (request == 538) {
            return "RIL_REQUEST_HW_TECHER_CONNECTIONS_STATUS";
        }
        if (request == 539) {
            return "RIL_REQUEST_HW_SET_ULFREQ_ENABLE";
        }
        switch (request) {
            case 2159:
                return "RIL_REQUEST_HW_SET_TEMPERATURE_CONTROL";
            case 2160:
                return "RIL_REQUEST_SET_NR_OPTION_MODE";
            case 2161:
                return "RIL_REQUEST_GET_NR_OPTION_MODE";
            case 2162:
                return "RIL_REQUEST_SEND_MUTI_CHIP_SESSION_CONFIG";
            case 2163:
                return "RIL_REQUEST_SEND_VSIM_DATA_TO_MODEM";
            case 2164:
                return "RIL_REQUEST_GET_NR_CELL_SSBID";
            case 2165:
                return "RIL_REQUEST_HW_SET_DATA_SWITCH";
            case 2166:
                return "RIL_REQUEST_HW_SET_DATA_ROAM_SWITCH";
            case 2167:
                return "RIL_REQUEST_HW_GET_CSGIDSRCH_INFO";
            default:
                switch (request) {
                    case 2171:
                        return "RIL_REQUEST_HW_REJ_CALL";
                    case 2172:
                        return "RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA";
                    case 2173:
                        return "RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY";
                    case 2174:
                        return "RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY";
                    case 2175:
                        return "RIL_REQUEST_HW_SET_DEEP_NO_DISTURB_SWITCH";
                    case 2176:
                        return "RIL_REQUEST_HW_GET_CURRENT_CALLS_EXT";
                    case 2177:
                        return "RIL_REQUEST_HW_SEND_NCFG_OPER_INFO";
                    case 2178:
                        return "RIL_REQUEST_HW_SEND_SIM_CHG_TYPE_INFO";
                    case 2179:
                        return "RIL_REQUEST_HW_GET_SIM_MATCHED_FILE";
                    case 2180:
                        return "RIL_REQUEST_HW_GET_NUM_OF_REC_PSE_BASE_STATION";
                    case 2181:
                        return "RIL_REQUEST_HW_GET_CARD_TRAY_INFO";
                    case 2182:
                        return "RIL_REQUEST_HW_GET_NVCFG_RESULT";
                    case 2183:
                        return "RIL_REQUEST_SET_CSCON_ENABLED";
                    case 2184:
                        return "RIL_REQUEST_GET_CSCON_ENABLED";
                    default:
                        switch (request) {
                            case 2186:
                                return "RIL_REQUEST_SET_NR_SA_STATE";
                            case 2187:
                                return "RIL_REQUEST_GET_NR_SA_STATE";
                            case 2188:
                                return "RIL_REQUEST_PROCESS_SMS_ANTI_ATTACK";
                            case 2189:
                                return "RIL_REQUEST_SET_VONR_SWITCH";
                            default:
                                switch (request) {
                                    case 2198:
                                        return "RIL_REQUEST_SET_TX_POWER_STATUS";
                                    case 2199:
                                        return "RIL_REQUEST_GET_RRC_CONNECTION_STATE";
                                    case 2200:
                                        return "RIL_REQUEST_DEACTIVATE_NRSCG_COMM";
                                    case 2201:
                                        return "RIL_REQUEST_GET_DEACTIVATE_NRSCG_COMM";
                                    case 2202:
                                        return "RIL_REQUEST_SET_MAX_ULSPEED";
                                    case 2203:
                                        return "RIL_REQUEST_SMART_RATSWITCH";
                                    case 2204:
                                        return "RIL_REQUEST_GET_SMART_RATSWITCH";
                                    case 2205:
                                        return "RIL_REQUEST_SET_SMART_RATSWITCH";
                                    case 2206:
                                        return "RIL_REQUEST_SEND_SAR_INDICATOR";
                                    default:
                                        return "<unknown request:" + request + ">";
                                }
                        }
                }
        }
    }

    public CommandException fromRilErrnoEx(int ril_errno) {
        if (ril_errno == 29) {
            return new CommandException(CommandException.Error.MISSING_RESOURCE);
        }
        if (ril_errno == 30) {
            return new CommandException(CommandException.Error.NO_SUCH_ELEMENT);
        }
        if (ril_errno == 401) {
            return new CommandException(CommandException.Error.INVALID_PARAMETER);
        }
        Rlog.e("GSM", "Unrecognized RIL errno " + ril_errno);
        return new CommandException(CommandException.Error.INVALID_RESPONSE);
    }

    public AbstractRIL.HwRILReference createHwRILReference(AbstractRIL ril) {
        return new HwRILReferenceImpl((RIL) ril);
    }

    public String gsm8BitUnpackedToString(byte[] data, int offset, int length, boolean needConvertCharacter) {
        return HwGsmAlphabet.gsm8BitUnpackedToString(data, offset, length, needConvertCharacter);
    }

    public String retToStringEx(int req, Object ret) {
        return HwResponseInfoToString.getDefault().retToStringEx(req, ret);
    }
}
