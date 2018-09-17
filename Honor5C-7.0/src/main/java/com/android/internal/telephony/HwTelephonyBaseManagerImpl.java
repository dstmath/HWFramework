package com.android.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.telephony.AbstractRIL.HwRILReference;
import com.android.internal.telephony.CommandException.Error;

public class HwTelephonyBaseManagerImpl implements HwTelephonyBaseManager {
    private static HwTelephonyBaseManager mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwTelephonyBaseManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwTelephonyBaseManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwTelephonyBaseManagerImpl.<clinit>():void");
    }

    public static HwTelephonyBaseManager getDefault() {
        return mInstance;
    }

    public String responseToStringEx(int request) {
        switch (request) {
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
            case 3034:
                return "RIL_UNSOL_HW_EXIST_NETWORK_INFO";
            case 3035:
                return "RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT";
            case 3037:
                return "RIL_UNSOL_HW_CA_STATE_CHANGED";
            case 3041:
                return "RIL_UNSOL_HW_IMSA_VOWIFI_MSG";
            default:
                return "<unknown response>";
        }
    }

    public String requestToStringEx(int request) {
        switch (request) {
            case 115:
                return "SIM_OPEN_CHANNEL";
            case 116:
                return "SIM_CLOSE_CHANNEL";
            case 136:
                return "SIM_GET_ATR";
            case 501:
                return "SIM_TRANSMIT_BASIC";
            case 504:
                return "SIM_TRANSMIT_CHANNEL";
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
            case 2028:
                return "RIL_REQUEST_HW_SET_SIM_SLOT_CFG";
            case 2029:
                return "RIL_REQUEST_HW_GET_SIM_SLOT_CFG";
            case 2032:
                return "RIL_REQUEST_HW_SIM_GET_ATR";
            case 2038:
                return "RIL_REQUEST_HW_VSIM_GET_SIM_STATE";
            case 2064:
                return "RIL_REQUEST_GET_POL_CAPABILITY";
            case 2065:
                return "RIL_REQUEST_GET_POL_LIST";
            case 2066:
                return "RIL_REQUEST_SET_POL_ENTRY";
            case 2072:
                return "RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO";
            case 2073:
                return "RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO";
            case 2075:
                return "RIL_REQUEST_HW_GET_ICCID";
            case 2099:
                return "SET_VOICEPREFER_STATUS";
            case 2100:
                return "GET_VOICEPREFER_STATUS";
            case 2107:
                return "RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH";
            case 2109:
                return "RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION";
            case 2114:
                return "HW_SET_IMS_SWITCH";
            case 2115:
                return "HW_GET_IMS_SWITCH";
            case 2118:
                return "RIL_REQUEST_HW_SET_CDMA_MODE_SIDE";
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
            default:
                return "<unknown request>";
        }
    }

    public CommandException fromRilErrnoEx(int ril_errno) {
        switch (ril_errno) {
            case 29:
                return new CommandException(Error.MISSING_RESOURCE);
            case HwAllInOneController.SINGLE_MODE_RUIM_CARD /*30*/:
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
