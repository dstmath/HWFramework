package com.android.internal.telephony.cdma.sms;

public final class SmsEnvelope {
    public static final int MESSAGE_TYPE_ACKNOWLEDGE = 2;
    public static final int MESSAGE_TYPE_BROADCAST = 1;
    public static final int MESSAGE_TYPE_POINT_TO_POINT = 0;
    public static final int SERVICE_CATEGORY_CMAS_CHILD_ABDUCTION_EMERGENCY = 4099;
    public static final int SERVICE_CATEGORY_CMAS_EXTREME_THREAT = 4097;
    public static final int SERVICE_CATEGORY_CMAS_LAST_RESERVED_VALUE = 4351;
    public static final int SERVICE_CATEGORY_CMAS_PRESIDENTIAL_LEVEL_ALERT = 4096;
    public static final int SERVICE_CATEGORY_CMAS_SEVERE_THREAT = 4098;
    public static final int SERVICE_CATEGORY_CMAS_TEST_MESSAGE = 4100;
    public static final int TELESERVICEID_CT_AUTO_REG_NOTIFICATION = 65005;
    public static final int TELESERVICE_CT_MMS_NOTIFICATION = 65002;
    public static final int TELESERVICE_MWI = 262144;
    public static final int TELESERVICE_NOT_SET = 0;
    public static final int TELESERVICE_SCPT = 4102;
    public static final int TELESERVICE_VMN = 4099;
    public static final int TELESERVICE_WAP = 4100;
    public static final int TELESERVICE_WEMT = 4101;
    public static final int TELESERVICE_WMT = 4098;
    public byte[] bearerData;
    public int bearerReply;
    public byte causeCode;
    public CdmaSmsAddress destAddress;
    public byte errorClass;
    public int messageType;
    public CdmaSmsAddress origAddress;
    public CdmaSmsSubaddress origSubaddress;
    public byte replySeqNo;
    public int serviceCategory;
    public int teleService = 0;
}
