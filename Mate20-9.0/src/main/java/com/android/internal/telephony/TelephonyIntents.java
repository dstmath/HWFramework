package com.android.internal.telephony;

public class TelephonyIntents extends AbstractTelephonyIntents {
    public static final String ACTION_ANY_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.ANY_DATA_STATE";
    public static final String ACTION_CARRIER_CERTIFICATE_DOWNLOAD = "com.android.internal.telephony.ACTION_CARRIER_CERTIFICATE_DOWNLOAD";
    public static final String ACTION_CARRIER_SIGNAL_DEFAULT_NETWORK_AVAILABLE = "com.android.internal.telephony.CARRIER_SIGNAL_DEFAULT_NETWORK_AVAILABLE";
    public static final String ACTION_CARRIER_SIGNAL_PCO_VALUE = "com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE";
    public static final String ACTION_CARRIER_SIGNAL_REDIRECTED = "com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED";
    public static final String ACTION_CARRIER_SIGNAL_REQUEST_NETWORK_FAILED = "com.android.internal.telephony.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED";
    public static final String ACTION_CARRIER_SIGNAL_RESET = "com.android.internal.telephony.CARRIER_SIGNAL_RESET";
    public static final String ACTION_DATA_CONNECTION_FAILED = "android.intent.action.DATA_CONNECTION_FAILED";
    public static final String ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED";
    @Deprecated
    public static final String ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED = "android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED";
    @Deprecated
    public static final String ACTION_DEFAULT_SUBSCRIPTION_CHANGED = "android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED";
    public static final String ACTION_EMERGENCY_CALLBACK_MODE_CHANGED = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED";
    public static final String ACTION_EMERGENCY_CALL_STATE_CHANGED = "android.intent.action.EMERGENCY_CALL_STATE_CHANGED";
    public static final String ACTION_FORBIDDEN_NO_SERVICE_AUTHORIZATION = "com.android.internal.intent.action.ACTION_FORBIDDEN_NO_SERVICE_AUTHORIZATION";
    public static final String ACTION_NETWORK_SET_TIME = "android.intent.action.NETWORK_SET_TIME";
    public static final String ACTION_NETWORK_SET_TIMEZONE = "android.intent.action.NETWORK_SET_TIMEZONE";
    public static final String ACTION_RADIO_TECHNOLOGY_CHANGED = "android.intent.action.RADIO_TECHNOLOGY";
    public static final String ACTION_REQUEST_OMADM_CONFIGURATION_UPDATE = "com.android.omadm.service.CONFIGURATION_UPDATE";
    public static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
    public static final String ACTION_SET_RADIO_CAPABILITY_DONE = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE";
    public static final String ACTION_SET_RADIO_CAPABILITY_FAILED = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED";
    public static final String ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS = "com.android.internal.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS";
    public static final String ACTION_SIGNAL_STRENGTH_CHANGED = "android.intent.action.SIG_STR";
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String ACTION_SPEECH_CODEC_WB = "com.huawei.intent.action.SPEECH_CODEC_WB";
    public static final String ACTION_SUBINFO_CONTENT_CHANGE = "android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE";
    public static final String ACTION_SUBINFO_RECORD_UPDATED = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
    public static final String EXTRA_APN_PROTO_KEY = "apnProto";
    public static final String EXTRA_APN_TYPE_KEY = "apnType";
    public static final String EXTRA_DATA_SPN = "spnData";
    public static final String EXTRA_DEFAULT_NETWORK_AVAILABLE_KEY = "defaultNetworkAvailable";
    public static final String EXTRA_ERROR_CODE_KEY = "errorCode";
    public static final String EXTRA_PCO_ID_KEY = "pcoId";
    public static final String EXTRA_PCO_VALUE_KEY = "pcoValue";
    public static final String EXTRA_PLMN = "plmn";
    public static final String EXTRA_RADIO_ACCESS_FAMILY = "rafs";
    public static final String EXTRA_REBROADCAST_ON_UNLOCK = "rebroadcastOnUnlock";
    public static final String EXTRA_REDIRECTION_URL_KEY = "redirectionUrl";
    public static final String EXTRA_SHOW_PLMN = "showPlmn";
    public static final String EXTRA_SHOW_SPN = "showSpn";
    public static final String EXTRA_SPEECH_CODEC_WB = "speechCodecWb";
    public static final String EXTRA_SPN = "spn";
    public static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    public static final String SPN_STRINGS_UPDATED_ACTION = "android.provider.Telephony.SPN_STRINGS_UPDATED";
}
