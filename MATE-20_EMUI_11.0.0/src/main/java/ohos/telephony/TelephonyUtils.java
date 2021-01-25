package ohos.telephony;

import ohos.hiviewdfx.HiLogLabel;
import ohos.system.Parameters;

public final class TelephonyUtils {
    public static final int CDMA_PHONE_TYPE = 2;
    public static final int GSM_PHONE_TYPE = 1;
    public static final int HEX_VALUE = 16;
    public static final int IMS_PHONE_TYPE = 5;
    public static final int LOG_ID_TELEPHONY = 218111744;
    public static final int MAX_CELL_INFO_LIST_SIZE = 100;
    public static final int MAX_SIGNAL_INFO_LIST_SIZE = 100;
    public static final int MODEM_CAP_SUPPORT_NR_BIT_OFFSET = 2;
    public static final int MODEM_CAP_SUPPORT_NR_CHAR_OFFSET = 8;
    public static final int MSG_ADD_CALL_OBSERVER = 2006;
    public static final int MSG_ADD_DATA_OBSERVER_ID = 4010;
    public static final int MSG_ADD_OBSERVER = 6000;
    public static final int MSG_CALL_BASE = 2000;
    public static final int MSG_CHECK_EMERGENCY_PHONE_NUMBER = 2008;
    public static final int MSG_CHECK_VIDEO_CALLING_ENABLED = 2009;
    public static final int MSG_DATA_BASE = 4000;
    public static final int MSG_DELETE_DATA_OBSERVER_ID = 4011;
    public static final int MSG_DIAL = 2002;
    public static final int MSG_DISPLAY_CALL_SCCREEN = 2003;
    public static final int MSG_ENABLE_DATA_ROAMING = 4005;
    public static final int MSG_GET_CALL_STATE = 2005;
    public static final int MSG_GET_CELL_INFO_LIST = 6;
    public static final int MSG_GET_CS_RADIO_TECH = 3;
    public static final int MSG_GET_DATA_FLOW_TYPE = 4001;
    public static final int MSG_GET_DATA_STATE = 4007;
    public static final int MSG_GET_DEFAULT_DATA_SLOT_INDEX_ID = 4008;
    public static final int MSG_GET_DEFAULT_SMS_SLOT_ID = 3003;
    public static final int MSG_GET_DEFAULT_SUBSCRIBER_ID = 1001;
    public static final int MSG_GET_DEFAULT_VOICE_SLOT = 1012;
    public static final int MSG_GET_IMEI = 9;
    public static final int MSG_GET_IMEI_SV = 10;
    public static final int MSG_GET_IMS_SMS_FORMAT = 3005;
    public static final int MSG_GET_ISO_FOR_NETWORK = 4;
    public static final int MSG_GET_MANUFACTURER_CODE = 13;
    public static final int MSG_GET_MASTER_SLOT_ID = 14;
    public static final int MSG_GET_MEID = 12;
    public static final int MSG_GET_NR_OPTION_MODE = 17;
    public static final int MSG_GET_PS_RADIO_TECH = 2;
    public static final int MSG_GET_RADIO_TECH = 1;
    public static final int MSG_GET_RADIO_TECHNOLOGY_TYPE = 4006;
    public static final int MSG_GET_SERVICE_STATE = 5;
    public static final int MSG_GET_SIGNAL_INFO_LIST = 7;
    public static final int MSG_GET_SIM_GID1 = 1004;
    public static final int MSG_GET_SIM_ICC_ID = 1002;
    public static final int MSG_GET_SIM_STATE = 1005;
    public static final int MSG_GET_SIM_TELEPHONE_NUMBER = 1003;
    public static final int MSG_GET_SIM_TELE_NUM_IDENTIFIER = 1008;
    public static final int MSG_GET_SUB_STATE = 16;
    public static final int MSG_GET_TA_CODE = 11;
    public static final int MSG_GET_UNIQUE_DEVICE_ID = 8;
    public static final int MSG_GET_VOICE_MAIL_COUNT = 1009;
    public static final int MSG_GET_VOICE_MAIL_IDENTIFIER = 1010;
    public static final int MSG_GET_VOICE_MAIL_NUMBER = 1007;
    public static final int MSG_HAS_CALL = 2001;
    public static final int MSG_HAS_SIM_CARD = 1006;
    public static final int MSG_INPUT_DIALER_SPECIAL_CODE = 2010;
    public static final int MSG_IS_DATA_ENABLED = 4002;
    public static final int MSG_IS_DATA_ROAMING_ENABLED = 4004;
    public static final int MSG_IS_IMS_SMS_SUPPORT = 3006;
    public static final int MSG_IS_NSA_STATE = 18;
    public static final int MSG_IS_SIM_ACTIVE = 1011;
    public static final int MSG_MUTE_RINGER = 2004;
    public static final int MSG_PARSE_OBSERVER_PARCEL_CELL = 6003;
    public static final int MSG_PARSE_OBSERVER_PARCEL_SERVICE_STATE = 6004;
    public static final int MSG_PARSE_OBSERVER_PARCEL_SIGNAL = 6002;
    public static final int MSG_RADIO_BASE = 0;
    public static final int MSG_REMOVE_CALL_OBSERVER = 2007;
    public static final int MSG_REMOVE_OBSERVER = 6001;
    public static final int MSG_SEND_MULTI_PART_TEXT_MESSAGE = 3004;
    public static final int MSG_SEND_SMS_MESSAGE = 3001;
    public static final int MSG_SET_DATA_ENABLED = 4003;
    public static final int MSG_SET_DEFAULT_DATA_SLOT_INDEX_ID = 4009;
    public static final int MSG_SET_DEFAULT_SMS_SLOT_ID = 3002;
    public static final int MSG_SIM_INFO_BASE = 1000;
    public static final int MSG_SMS_BASE = 3000;
    public static final int NONE_PHONE_TYPE = 0;
    public static final String PROPERTY_CURRENT_PHONE_TYPE = "gsm.current.phone-type";
    public static final String PROPERTY_MODEM_CAPABILITY = "persist.radio.modem.cap";
    public static final String PROPERTY_MULTI_SIM_CONFIG = "persist.radio.multisim.config";
    public static final String PROPERTY_OPERATOR_ISROAMING = "gsm.operator.isroaming";
    public static final String PROPERTY_OPERATOR_NAME = "gsm.operator.alpha";
    public static final String PROPERTY_OPERATOR_PLMN = "gsm.operator.numeric";
    public static final String PROPERTY_SIM_ISO_COUNTRY = "gsm.sim.operator.iso-country";
    public static final String PROPERTY_SIM_PLMN_NUMERIC = "gsm.sim.operator.numeric";
    public static final String PROPERTY_SIM_SERVICE_PROVIDER_NAME = "gsm.sim.operator.alpha";
    public static final int SIP_PHONE_TYPE = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, LOG_ID_TELEPHONY, "TelephonyUtils");
    public static final int THIRD_PARTY_PHONE_TYPE = 4;

    public static boolean isValidSlotId(int i) {
        return i >= 0 && i <= 2;
    }

    public static String getTelephonyProperty(int i, String str, String str2) {
        String str3 = Parameters.get(str, "");
        if ("".equals(str3) || str3.length() <= 0) {
            return str2;
        }
        String[] split = str3.split(",");
        return (i < 0 || i >= split.length || split[i] == null) ? str2 : split[i];
    }
}
