package ohos.dmsdp.sdk;

public class DeviceParameterConst {
    public static final int AUDIO_CHANNELMASK_STRING = 2005;
    public static final int AUDIO_FORMAT_STRING = 2004;
    public static final int AUDIO_SAMPLERATES_STRING = 2003;
    public static final int CAMERA_CFG_WHITELIST_STRING = 2012;
    public static final int CAMERA_INFO_JSON_STRING = 2014;
    public static final int CAMERA_LOCATION_STRING = 2013;
    public static final int CAMERA_PARAMETERS_STRING = 2002;
    public static final int CAMERA_SUPPORTFORMATS_STRING = 2009;
    public static final int CAMERA_SUPPORTSIZES_STRING = 2010;
    public static final int CAMERA_SUPPROTFPSRANGE_STRING = 2011;
    public static final int DEVICE_BLUETOOTHMAC_STRING = 2006;
    public static final int DEVICE_BTNAME_STRING = 2008;
    public static final int DEVICE_CAMERASTATUS_BOOLEAN = 5011;
    public static final int DEVICE_CARCOLOR_STRING = 2100;
    public static final int DEVICE_CARTYPE_STRING = 2101;
    public static final int DEVICE_DISCOVER_PROTOCOL_INT = 1013;
    public static final int DEVICE_DISPLAYSTATUS_BOOLEAN = 5014;
    public static final int DEVICE_GPSSTATUS_BOOLEAN = 5015;
    public static final int DEVICE_HUAWEIIDNAME_STRING = 2007;
    public static final int DEVICE_ISCONNECTED_BOOLEAN = 5020;
    public static final int DEVICE_ISHUAWEITV_BOOLEAN = 5019;
    public static final int DEVICE_LASTUSETIME_LONG = 3003;
    public static final int DEVICE_LOCALIP_STRING = 2001;
    public static final int DEVICE_MICSTATUS_BOOLEAN = 5012;
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
    public static final int DEVICE_TIMEDURATION_LONG = 3002;
    public static final int DEVICE_TYPE_INT = 1003;
    public static final int DEVICE_USECOUNT_LONG = 3001;
    public static final int DEVICE_WEAR_SUPPORT_HEART_RATE_BOOLEAN = 5023;
    public static final int DEVICE_WEAR_SUPPORT_PPG_BOOLEAN = 5022;
    public static final int DEVICE_WEAR_SUPPORT_VIBRATE_BOOLEAN = 5021;
    public static final int DISPLAY_DENSITY_DOUBLE = 4001;
    public static final int DISPLAY_HEIGHT_INT = 1010;
    public static final int DISPLAY_ISFULLSCREEN_BOOLEAN = 5016;
    public static final int DISPLAY_VITUALHEIGHT_INT = 1006;
    public static final int DISPLAY_VITUALWIDTH_INT = 1005;
    public static final int DISPLAY_VITUALXOFFSET_INT = 1007;
    public static final int DISPLAY_VITUALYOFFSET_INT = 1008;
    public static final int DISPLAY_WIDTH_INT = 1009;
    public static final int HILINK_DEVICE = 1012;
    public static final int HIWEAR_DEVICE = 1014;
    public static final int HIWEAR_STRING = 2017;
    public static final int PARAMETER_CONST = 1000;
    public static final int PROTOCOL = 1011;
    public static final int SERVICE_STATUS_INT = 1002;
    public static final int SERVICE_TYPE_INT = 1004;
    public static final String TAG = "DeviceParameterConst";
    public static final int VIDEO_MODE_STRING = 2015;

    private DeviceParameterConst() {
    }

    public static boolean invalidCheck(int i, Object obj) {
        int i2 = i / 1000;
        if (i2 != 1) {
            if (i2 != 2) {
                if (i2 != 3) {
                    if (i2 != 4) {
                        if (i2 != 5) {
                            HwLog.e(TAG, "parameter " + i + " not allowed");
                            return false;
                        } else if (obj instanceof Boolean) {
                            return true;
                        } else {
                            HwLog.e(TAG, "parameter " + i + " should be Boolean but not");
                            return false;
                        }
                    } else if (obj instanceof Double) {
                        return true;
                    } else {
                        HwLog.e(TAG, "parameter " + i + " should be Double but not");
                        return false;
                    }
                } else if (obj instanceof Long) {
                    return true;
                } else {
                    HwLog.e(TAG, "parameter " + i + " should be Long but not");
                    return false;
                }
            } else if (obj instanceof String) {
                return true;
            } else {
                HwLog.e(TAG, "parameter " + i + " should be String but not");
                return false;
            }
        } else if (obj instanceof Integer) {
            return true;
        } else {
            HwLog.e(TAG, "parameter " + i + " should be Integer but not");
            return false;
        }
    }
}
