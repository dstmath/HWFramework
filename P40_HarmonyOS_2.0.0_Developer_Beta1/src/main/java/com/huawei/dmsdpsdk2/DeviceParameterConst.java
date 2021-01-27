package com.huawei.dmsdpsdk2;

public class DeviceParameterConst {
    public static final int AUDIO_CHANNELMASK_STRING = 2005;
    public static final int AUDIO_FORMAT_STRING = 2004;
    public static final int AUDIO_MIC_CAP_STRING = 2022;
    public static final int AUDIO_SAMPLERATES_STRING = 2003;
    public static final int AUDIO_SPEAKER_CAP_STRING = 2023;
    public static final int AUTH_MODE_JSON_STRING = 2018;
    public static final int CAMERA_CFG_WHITELIST_STRING = 2012;
    public static final int CAMERA_CUSTOM_ON_DEVICE = 2019;
    public static final int CAMERA_INFO_JSON_STRING = 2014;
    public static final int CAMERA_LOCATION_STRING = 2013;
    public static final int CAMERA_PARAMETERS_STRING = 2002;
    public static final int CAMERA_SUPPORTFORMATS_STRING = 2009;
    public static final int CAMERA_SUPPORTSIZES_STRING = 2010;
    public static final int CAMERA_SUPPROTFPSRANGE_STRING = 2011;
    public static final int CAMERA_VIRTUAL_ID = 2026;
    public static final int DEVICE_BLUETOOTHMAC_STRING = 2006;
    public static final int DEVICE_BTNAME_STRING = 2008;
    public static final int DEVICE_CAMERASTATUS_BOOLEAN = 5011;
    public static final int DEVICE_CARCOLOR_STRING = 2100;
    public static final int DEVICE_CARTYPE_STRING = 2101;
    public static final int DEVICE_COAP_ID_STRING = 2021;
    public static final int DEVICE_DISCOVER_PROTOCOL_INT = 1013;
    public static final int DEVICE_DISPLAYSTATUS_BOOLEAN = 5014;
    public static final int DEVICE_ENCRYPT_ALG_INT = 1026;
    public static final int DEVICE_GPSSTATUS_BOOLEAN = 5015;
    public static final int DEVICE_HUAWEIIDNAME_STRING = 2007;
    public static final int DEVICE_ISCONNECTED_BOOLEAN = 5020;
    public static final int DEVICE_ISHUAWEITV_BOOLEAN = 5019;
    public static final int DEVICE_LASTUSETIME_LONG = 3003;
    public static final int DEVICE_LOCALIP_STRING = 2001;
    public static final int DEVICE_MICSTATUS_BOOLEAN = 5012;
    public static final int DEVICE_PERMISSION_TYPE = 1025;
    public static final int DEVICE_PROJECTIONPROTOCOL_INT = 1001;
    public static final int DEVICE_SPEAKSTATUS_BOOLEAN = 5013;
    public static final int DEVICE_SUPPORT5G_BOOLEAN = 5006;
    public static final int DEVICE_SUPPORTA2DP_BOOLEAN = 5018;
    public static final int DEVICE_SUPPORTBLE_BOOLEAN = 5008;
    public static final int DEVICE_SUPPORTBR_BOOLEAN = 5009;
    public static final int DEVICE_SUPPORTCAMERA_BOOLEAN = 5001;
    public static final int DEVICE_SUPPORTDISPLAY_BOOLEAN = 5004;
    public static final int DEVICE_SUPPORTGPS_BOOLEAN = 5005;
    public static final int DEVICE_SUPPORTHFP_BOOLEAN = 5017;
    public static final int DEVICE_SUPPORTMIC_BOOLEAN = 5002;
    public static final int DEVICE_SUPPORTP2P_BOOLEAN = 5010;
    public static final int DEVICE_SUPPORTSPEAKER_BOOLEAN = 5003;
    public static final int DEVICE_SUPPORTUSB_BOOLEAN = 5007;
    public static final int DEVICE_SUPPORT_SOFT_BUS_BOOLEAN = 5031;
    public static final int DEVICE_TIMEDURATION_LONG = 3002;
    public static final int DEVICE_TRIGGER_CONNECT_APP = 2024;
    public static final int DEVICE_TYPE_INT = 1003;
    public static final int DEVICE_USECOUNT_LONG = 3001;
    public static final int DEVICE_WEAR_SUPPORT_ACCELEROMETER_BOOLEAN = 5026;
    public static final int DEVICE_WEAR_SUPPORT_GYROSCOPE_BOOLEAN = 5027;
    public static final int DEVICE_WEAR_SUPPORT_HEART_RATE_BOOLEAN = 5023;
    public static final int DEVICE_WEAR_SUPPORT_NOTIFICATION_BOOLEAN = 5024;
    public static final int DEVICE_WEAR_SUPPORT_PPG_BOOLEAN = 5022;
    public static final int DEVICE_WEAR_SUPPORT_PRESURE_BOOLEAN = 5028;
    public static final int DEVICE_WEAR_SUPPORT_SENSOR_BOOLEAN = 5029;
    public static final int DEVICE_WEAR_SUPPORT_VIBRATE_BOOLEAN = 5021;
    public static final int DISPLAY_CONFIG_FLAG_INT = 1024;
    public static final int DISPLAY_DENSITY_DOUBLE = 4001;
    public static final int DISPLAY_DPI_INT = 1015;
    public static final int DISPLAY_HEIGHT_INT = 1010;
    public static final int DISPLAY_ISFULLSCREEN_BOOLEAN = 5016;
    public static final int DISPLAY_MAX_VIDEO_BITRATE_INT = 1028;
    public static final int DISPLAY_MIN_VIDEO_BITRATE_INT = 1029;
    public static final int DISPLAY_PHYSICS_HEIGHT = 1023;
    public static final int DISPLAY_PHYSICS_WIDTH = 1022;
    public static final int DISPLAY_VIDEO_BITRATE_INT = 1021;
    public static final int DISPLAY_VIDEO_CODEC_TYPE_INT = 1018;
    public static final int DISPLAY_VIDEO_FPS_INT = 1019;
    public static final int DISPLAY_VIDEO_GOP_INT = 1020;
    public static final int DISPLAY_VIDEO_HEIGHT_INT = 1017;
    public static final int DISPLAY_VIDEO_WIDTH_INT = 1016;
    public static final int DISPLAY_VITUALHEIGHT_INT = 1006;
    public static final int DISPLAY_VITUALWIDTH_INT = 1005;
    public static final int DISPLAY_VITUALXOFFSET_INT = 1007;
    public static final int DISPLAY_VITUALYOFFSET_INT = 1008;
    public static final int DISPLAY_WIDTH_INT = 1009;
    public static final int HILINK_DEVICE = 1050;
    public static final int HIWEAR_DEVICE = 1014;
    public static final int HIWEAR_STRING = 2017;
    public static final int HW_TV_MIRACAST_IDENTIFY_STRING = 2016;
    public static final int ICONNECT_MODEL_ID_STRING = 2020;
    public static final int IGNORE_ANDROID_CAMERA = 5025;
    private static final int PARAMETER_CONST = 1000;
    public static final int PHONE_COAP_VERSION_INT = 1027;
    public static final int PROTOCOL = 1011;
    public static final int SERVICE_STATUS_INT = 1002;
    public static final int SERVICE_TYPE_INT = 1004;
    private static final String TAG = "DeviceParameterConst";
    public static final int VIDEO_MODE_STRING = 2015;

    private DeviceParameterConst() {
    }

    public static boolean invalidCheck(int key, Object value) {
        int type = key / 1000;
        if (type != 1) {
            if (type != 2) {
                if (type != 3) {
                    if (type != 4) {
                        if (type != 5) {
                            HwLog.e(TAG, "parameter " + key + " not allowed");
                            return false;
                        } else if (value instanceof Boolean) {
                            return true;
                        } else {
                            HwLog.e(TAG, "parameter " + key + " should be Boolean but not");
                            return false;
                        }
                    } else if (value instanceof Double) {
                        return true;
                    } else {
                        HwLog.e(TAG, "parameter " + key + " should be Double but not");
                        return false;
                    }
                } else if (value instanceof Long) {
                    return true;
                } else {
                    HwLog.e(TAG, "parameter " + key + " should be Long but not");
                    return false;
                }
            } else if (value instanceof String) {
                return true;
            } else {
                HwLog.e(TAG, "parameter " + key + " should be String but not");
                return false;
            }
        } else if (value instanceof Integer) {
            return true;
        } else {
            HwLog.e(TAG, "parameter " + key + " should be Integer but not");
            return false;
        }
    }
}
