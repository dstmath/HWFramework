package com.android.ims;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.ims.internal.IImsConfig;

public class ImsConfig {
    public static final String ACTION_IMS_CONFIG_CHANGED = "com.android.intent.action.IMS_CONFIG_CHANGED";
    public static final String ACTION_IMS_FEATURE_CHANGED = "com.android.intent.action.IMS_FEATURE_CHANGED";
    public static final String EXTRA_CHANGED_ITEM = "item";
    public static final String EXTRA_NEW_VALUE = "value";
    private static final String TAG = "ImsConfig";
    private boolean DBG = true;
    private Context mContext;
    private final IImsConfig miConfig;

    public static class ConfigConstants {
        public static final int AMR_BANDWIDTH_EFFICIENT_PT = 50;
        public static final int AMR_DEFAULT_MODE = 53;
        public static final int AMR_OCTET_ALIGNED_PT = 49;
        public static final int AMR_WB_BANDWIDTH_EFFICIENT_PT = 48;
        public static final int AMR_WB_OCTET_ALIGNED_PT = 47;
        public static final int AVAILABILITY_CACHE_EXPIRATION = 19;
        public static final int CANCELLATION_TIMER = 4;
        public static final int CAPABILITIES_CACHE_EXPIRATION = 18;
        public static final int CAPABILITIES_POLL_INTERVAL = 20;
        public static final int CAPABILITY_DISCOVERY_ENABLED = 17;
        public static final int CAPAB_POLL_LIST_SUB_EXP = 23;
        public static final int CONFIG_START = 0;
        public static final int DOMAIN_NAME = 12;
        public static final int DTMF_NB_PT = 52;
        public static final int DTMF_WB_PT = 51;
        public static final int EAB_SETTING_ENABLED = 25;
        public static final int GZIP_FLAG = 24;
        public static final int KEEP_ALIVE_ENABLED = 32;
        public static final int LBO_PCSCF_ADDRESS = 31;
        public static final int LVC_SETTING_ENABLED = 11;
        public static final int MAX_NUMENTRIES_IN_RCL = 22;
        public static final int MIN_SE = 3;
        public static final int MOBILE_DATA_ENABLED = 29;
        public static final int PROVISIONED_CONFIG_END = 65;
        public static final int PROVISIONED_CONFIG_START = 0;
        public static final int PUBLISH_TIMER = 15;
        public static final int PUBLISH_TIMER_EXTENDED = 16;
        public static final int REGISTRATION_RETRY_BASE_TIME_SEC = 33;
        public static final int REGISTRATION_RETRY_MAX_TIME_SEC = 34;
        public static final int SILENT_REDIAL_ENABLE = 6;
        public static final int SIP_ACK_RECEIPT_WAIT_TIME_MSEC = 43;
        public static final int SIP_ACK_RETX_WAIT_TIME_MSEC = 44;
        public static final int SIP_INVITE_REQ_RETX_INTERVAL_MSEC = 37;
        public static final int SIP_INVITE_RSP_RETX_INTERVAL_MSEC = 42;
        public static final int SIP_INVITE_RSP_RETX_WAIT_TIME_MSEC = 39;
        public static final int SIP_INVITE_RSP_WAIT_TIME_MSEC = 38;
        public static final int SIP_NON_INVITE_REQ_RETX_INTERVAL_MSEC = 40;
        public static final int SIP_NON_INVITE_REQ_RETX_WAIT_TIME_MSEC = 45;
        public static final int SIP_NON_INVITE_RSP_RETX_WAIT_TIME_MSEC = 46;
        public static final int SIP_NON_INVITE_TXN_TIMEOUT_TIMER_MSEC = 41;
        public static final int SIP_SESSION_TIMER = 2;
        public static final int SIP_T1_TIMER = 7;
        public static final int SIP_T2_TIMER = 8;
        public static final int SIP_TF_TIMER = 9;
        public static final int SMS_FORMAT = 13;
        public static final int SMS_OVER_IP = 14;
        public static final int SMS_PSI = 54;
        public static final int SOURCE_THROTTLE_PUBLISH = 21;
        public static final int SPEECH_END_PORT = 36;
        public static final int SPEECH_START_PORT = 35;
        public static final int TDELAY = 5;
        public static final int TH_1x = 59;
        public static final int TH_LTE1 = 56;
        public static final int TH_LTE2 = 57;
        public static final int TH_LTE3 = 58;
        public static final int T_EPDG_1X = 64;
        public static final int T_EPDG_LTE = 62;
        public static final int T_EPDG_WIFI = 63;
        public static final int VICE_SETTING_ENABLED = 65;
        public static final int VIDEO_QUALITY = 55;
        public static final int VLT_SETTING_ENABLED = 10;
        public static final int VOCODER_AMRMODESET = 0;
        public static final int VOCODER_AMRWBMODESET = 1;
        public static final int VOICE_OVER_WIFI_MODE = 27;
        public static final int VOICE_OVER_WIFI_ROAMING = 26;
        public static final int VOICE_OVER_WIFI_SETTING_ENABLED = 28;
        public static final int VOLTE_USER_OPT_IN_STATUS = 30;
        public static final int VOWT_A = 60;
        public static final int VOWT_B = 61;
    }

    public static class FeatureConstants {
        public static final int FEATURE_TYPE_UNKNOWN = -1;
        public static final int FEATURE_TYPE_UT_OVER_LTE = 4;
        public static final int FEATURE_TYPE_UT_OVER_WIFI = 5;
        public static final int FEATURE_TYPE_VIDEO_OVER_LTE = 1;
        public static final int FEATURE_TYPE_VIDEO_OVER_WIFI = 3;
        public static final int FEATURE_TYPE_VOICE_OVER_LTE = 0;
        public static final int FEATURE_TYPE_VOICE_OVER_WIFI = 2;
    }

    public static class FeatureValueConstants {
        public static final int OFF = 0;
        public static final int ON = 1;
    }

    public static class OperationStatusConstants {
        public static final int FAILED = 1;
        public static final int SUCCESS = 0;
        public static final int UNKNOWN = -1;
        public static final int UNSUPPORTED_CAUSE_DISABLED = 4;
        public static final int UNSUPPORTED_CAUSE_NONE = 2;
        public static final int UNSUPPORTED_CAUSE_RAT = 3;
    }

    public static class OperationValuesConstants {
        public static final int VIDEO_QUALITY_HIGH = 1;
        public static final int VIDEO_QUALITY_LOW = 0;
        public static final int VIDEO_QUALITY_UNKNOWN = -1;
    }

    public static class VideoQualityFeatureValuesConstants {
        public static final int HIGH = 1;
        public static final int LOW = 0;
    }

    public static class WfcModeFeatureValueConstants {
        public static final int CELLULAR_PREFERRED = 1;
        public static final int WIFI_ONLY = 0;
        public static final int WIFI_PREFERRED = 2;
    }

    public ImsConfig(IImsConfig iconfig, Context context) {
        if (this.DBG) {
            Rlog.d(TAG, "ImsConfig creates");
        }
        this.miConfig = iconfig;
        this.mContext = context;
    }

    public int getProvisionedValue(int item) throws ImsException {
        try {
            int ret = this.miConfig.getProvisionedValue(item);
            if (this.DBG) {
                Rlog.d(TAG, "getProvisionedValue(): item = " + item + ", ret =" + ret);
            }
            return ret;
        } catch (RemoteException e) {
            throw new ImsException("getValue()", e, 131);
        }
    }

    public String getProvisionedStringValue(int item) throws ImsException {
        String ret = "Unknown";
        try {
            ret = this.miConfig.getProvisionedStringValue(item);
            if (this.DBG) {
                Rlog.d(TAG, "getProvisionedStringValue(): item = " + item + ", ret =" + ret);
            }
            return ret;
        } catch (RemoteException e) {
            throw new ImsException("getProvisionedStringValue()", e, 131);
        }
    }

    public int setProvisionedValue(int item, int value) throws ImsException {
        if (this.DBG) {
            Rlog.d(TAG, "setProvisionedValue(): item = " + item + "value = " + value);
        }
        try {
            int ret = this.miConfig.setProvisionedValue(item, value);
            if (this.DBG) {
                Rlog.d(TAG, "setProvisionedValue(): item = " + item + " value = " + value + " ret = " + ret);
            }
            return ret;
        } catch (RemoteException e) {
            throw new ImsException("setProvisionedValue()", e, 131);
        }
    }

    public int setProvisionedStringValue(int item, String value) throws ImsException {
        try {
            int ret = this.miConfig.setProvisionedStringValue(item, value);
            if (this.DBG) {
                Rlog.d(TAG, "setProvisionedStringValue(): item = " + item + ", value =" + value);
            }
            return ret;
        } catch (RemoteException e) {
            throw new ImsException("setProvisionedStringValue()", e, 131);
        }
    }

    public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws ImsException {
        if (this.DBG) {
            Rlog.d(TAG, "getFeatureValue: feature = " + feature + ", network =" + network + ", listener =" + listener);
        }
        try {
            this.miConfig.getFeatureValue(feature, network, listener);
        } catch (RemoteException e) {
            throw new ImsException("getFeatureValue()", e, 131);
        }
    }

    public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws ImsException {
        if (this.DBG) {
            Rlog.d(TAG, "setFeatureValue: feature = " + feature + ", network =" + network + ", value =" + value + ", listener =" + listener);
        }
        try {
            this.miConfig.setFeatureValue(feature, network, value, listener);
        } catch (RemoteException e) {
            throw new ImsException("setFeatureValue()", e, 131);
        }
    }
}
