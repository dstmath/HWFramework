package com.huawei.internal.telephony;

import com.huawei.annotation.HwSystemApi;

public interface PhoneConstantsEx {
    public static final int APN_ALREADY_ACTIVE = 0;
    public static final int APN_REQUEST_FAILED = 3;
    public static final int APN_REQUEST_STARTED = 1;
    public static final String APN_TYPE_ALL = "*";
    @HwSystemApi
    public static final String APN_TYPE_BIP0 = "bip0";
    @HwSystemApi
    public static final String APN_TYPE_BIP1 = "bip1";
    @HwSystemApi
    public static final String APN_TYPE_BIP2 = "bip2";
    @HwSystemApi
    public static final String APN_TYPE_BIP3 = "bip3";
    @HwSystemApi
    public static final String APN_TYPE_BIP4 = "bip4";
    @HwSystemApi
    public static final String APN_TYPE_BIP5 = "bip5";
    @HwSystemApi
    public static final String APN_TYPE_BIP6 = "bip6";
    public static final String APN_TYPE_CBS = "cbs";
    public static final String APN_TYPE_DEFAULT = "default";
    @HwSystemApi
    public static final String APN_TYPE_DUN = "dun";
    @HwSystemApi
    public static final String APN_TYPE_EMERGENCY = "emergency";
    public static final String APN_TYPE_FOTA = "fota";
    public static final String APN_TYPE_HIPRI = "hipri";
    @HwSystemApi
    public static final String APN_TYPE_IA = "ia";
    public static final String APN_TYPE_IMS = "ims";
    @HwSystemApi
    public static final String APN_TYPE_INTERNALDEFAULT = "internaldefault";
    public static final String APN_TYPE_MMS = "mms";
    @HwSystemApi
    public static final String APN_TYPE_SNSSAI = "snssai";
    @HwSystemApi
    public static final String APN_TYPE_SNSSAI1 = "snssai1";
    @HwSystemApi
    public static final String APN_TYPE_SNSSAI2 = "snssai2";
    @HwSystemApi
    public static final String APN_TYPE_SNSSAI3 = "snssai3";
    @HwSystemApi
    public static final String APN_TYPE_SNSSAI4 = "snssai4";
    @HwSystemApi
    public static final String APN_TYPE_SNSSAI5 = "snssai5";
    @HwSystemApi
    public static final String APN_TYPE_SNSSAI6 = "snssai6";
    public static final String APN_TYPE_SUPL = "supl";
    @HwSystemApi
    public static final String APN_TYPE_XCAP = "xcap";
    @HwSystemApi
    public static final int APPTYPE_CSIM = 4;
    @HwSystemApi
    public static final int APPTYPE_ISIM = 5;
    @HwSystemApi
    public static final int APPTYPE_RUIM = 3;
    @HwSystemApi
    public static final int APPTYPE_SIM = 1;
    @HwSystemApi
    public static final int APPTYPE_UNKNOWN = 0;
    @HwSystemApi
    public static final int APPTYPE_USIM = 2;
    @HwSystemApi
    public static final int DEFAULT_CARD_INDEX = 0;
    public static final int FAILURE = 1;
    @HwSystemApi
    public static final int LTE_ON_CDMA_UNKNOWN = -1;
    @HwSystemApi
    public static final int MAX_PHONE_COUNT_DUAL_SIM = 2;
    @HwSystemApi
    public static final int MAX_PHONE_COUNT_SINGLE_SIM = 1;
    @HwSystemApi
    public static final String PHONE_KEY = "phone";
    @HwSystemApi
    public static final String PHONE_NAME_KEY = "phoneName";
    public static final int PHONE_TYPE_CDMA = 2;
    @HwSystemApi
    public static final int PHONE_TYPE_CDMA_LTE = 6;
    public static final int PHONE_TYPE_GSM = 1;
    public static final int PHONE_TYPE_IMS = 4;
    public static final int PHONE_TYPE_NONE = 0;
    public static final int PHONE_TYPE_SIP = 3;
    public static final int PHONE_TYPE_THIRD_PARTY = 4;
    public static final String SLOT_KEY = "slot";
    @HwSystemApi
    public static final String STATE_KEY = "state";
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    @HwSystemApi
    public static final int SUB3 = 2;
    public static final String SUBSCRIPTION_KEY = "subscription";
    @HwSystemApi
    public static final int SUB_VSIM = 2;
    public static final int SUCCESS = 0;

    public enum DataState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        SUSPENDED
    }

    @HwSystemApi
    public enum StateEx {
        IDLE,
        RINGING,
        OFFHOOK
    }
}
