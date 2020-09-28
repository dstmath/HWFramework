package com.mediatek.ims;

public class MtkImsConstants {
    public static final String ACTION_IMS_INCOMING_CALL_INDICATION = "com.android.ims.IMS_INCOMING_CALL_INDICATION";
    public static final String ACTION_IMS_SERVICE_DEREGISTERED = "com.android.ims.IMS_SERVICE_DEREGISTERED";
    public static final String ACTION_MTK_IMS_SERVICE_UP = "com.mediatek.ims.MTK_IMS_SERVICE_UP";
    public static final String ACTION_MTK_MMTEL_READY = "com.mediatek.ims.MTK_MMTEL_READY";
    public static final String EXTRA_CALL_MODE = "android:imsCallMode";
    public static final String EXTRA_DIAL_STRING = "android:imsDialString";
    public static final String EXTRA_MT_TO_NUMBER = "mediatek:mtToNumber";
    public static final String EXTRA_PHONE_ID = "android:phoneId";
    public static final String EXTRA_SEQ_NUM = "android:imsSeqNum";
    public static final int IMS_REGISTERED = 1;
    public static final int IMS_REGISTERING = 0;
    public static final int IMS_REGISTER_FAIL = 2;
    public static final int MTK_CONFIG_START = 1000;
    public static final String MTK_IMS_SERVICE = "mtkIms";
    public static final String MTK_KEY_SUPPORT_ENHANCED_CALL_BLOCKING_BOOL = "mtk_support_enhanced_call_blocking_bool";
    public static final String MULTI_IMS_SUPPORT = "persist.vendor.mims_support";
    public static final int OOS_END_WITH_DISCONN = 0;
    public static final int OOS_END_WITH_RESUME = 2;
    public static final int OOS_START = 1;
    public static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    public static final String PROPERTY_IMS_SUPPORT = "persist.vendor.ims_support";
    public static final int SERVICE_REG_CAPABILITY_EVENT_ECC_NOT_SUPPORT = 4;
    public static final int SERVICE_REG_CAPABILITY_EVENT_ECC_SUPPORT = 2;
    public static final int SERVICE_REG_EVENT_WIFI_PDN_OOS_END_WITH_DISCONN = 6;
    public static final int SERVICE_REG_EVENT_WIFI_PDN_OOS_END_WITH_RESUME = 7;
    public static final int SERVICE_REG_EVENT_WIFI_PDN_OOS_START = 5;
    public static final String WFC_IMS_ENABLED_SIM2 = "wfc_ims_enabled_sim2";
    public static final String WFC_IMS_ENABLED_SIM3 = "wfc_ims_enabled_sim3";
    public static final String WFC_IMS_ENABLED_SIM4 = "wfc_ims_enabled_sim4";

    public static class ConfigConstants {
        public static final int CONFIG_START = 1000;
        public static final int EPDG_ADDRESS = 1000;
        public static final int PROVISIONED_CONFIG_END = 1002;
        public static final int PROVISIONED_CONFIG_START = 1000;
        public static final int PUBLISH_ERROR_RETRY_TIMER = 1001;
        public static final int VOICE_OVER_WIFI_MDN = 1002;
    }

    public static class FeatureConstants {
        public static final int FEATURE_TYPE_VIDEO_OVER_NR = 7;
        public static final int FEATURE_TYPE_VOICE_OVER_NR = 6;
    }
}
