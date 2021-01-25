package com.android.server.display;

import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.RemoteException;
import com.android.server.appprotect.AppProtectActionConstant;
import com.android.server.hidata.hinetwork.HwHiNetworkParmStatistics;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.multiwin.HwMultiWinConstants;
import com.huawei.displayengine.DeLog;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

class GetEffectAdapter {
    public static final int ERR_INVALID_INPUT = -2;
    public static final int ERR_OTHER = -1;
    private static final String TAG = "DE J GetEffectAdapter";

    GetEffectAdapter() {
    }

    public static int getEffect(IDisplayEngineService service, int feature, int type, Bundle data) throws RemoteException {
        if (feature == 33) {
            return FoldingCompensationProcessor.getEffect(service, type, data);
        }
        if (feature == 14) {
            return PanelInfoProcessor.getEffect(service, type, data);
        }
        if (feature == 37) {
            return MaintenanceProcessor.getEffect(service, type, data);
        }
        DeLog.e(TAG, "Unknown feature:" + feature);
        return -2;
    }

    /* access modifiers changed from: private */
    public static class FoldingCompensationProcessor {
        private FoldingCompensationProcessor() {
        }

        public static int getEffect(IDisplayEngineService service, int type, Bundle data) throws RemoteException {
            if (data == null) {
                DeLog.e(GetEffectAdapter.TAG, "Invalid input: data is null!");
                return -2;
            } else if (type == 2) {
                return getPanelInfo(service, data);
            } else {
                if (type == 6) {
                    return getCalibrationGain(service, data);
                }
                if (type == 8) {
                    return getCalibrationTestLevels(service, data);
                }
                if (type == 9) {
                    return getCalibrationApplyTime(service, data);
                }
                DeLog.e(GetEffectAdapter.TAG, "Invalid input: unsupport type=" + type);
                return -2;
            }
        }

        private static int getPanelInfo(IDisplayEngineService service, Bundle data) throws RemoteException {
            int[] result = new int[3];
            int ret = service.getEffectEx(33, 2, result, result.length);
            if (ret != 0) {
                return ret;
            }
            data.putInt("RegionLine1", result[0]);
            data.putInt("RegionLine2", result[1]);
            int i = result[2];
            if (i == 0) {
                data.putString("RegionDirection", "top");
            } else if (i == 1) {
                data.putString("RegionDirection", HwMultiWinConstants.LEFT_HAND_LAZY_MODE_STR);
            } else if (i == 2) {
                data.putString("RegionDirection", "bottom");
            } else if (i != 3) {
                DeLog.e(GetEffectAdapter.TAG, "Invalid panel region direction=" + result[2]);
                return -1;
            } else {
                data.putString("RegionDirection", HwMultiWinConstants.RIGHT_HAND_LAZY_MODE_STR);
            }
            return 0;
        }

        private static int getCalibrationGain(IDisplayEngineService service, Bundle data) throws RemoteException {
            int[] result = new int[4];
            result[0] = data.getInt("level");
            String color = data.getString("RGBDimension");
            if (color == null) {
                DeLog.e(GetEffectAdapter.TAG, "Invalid input: color is null!");
                return -2;
            }
            if (AwarenessInnerConstants.HAS_RELATION_FENCE_SEPARATE_KEY.equals(color)) {
                result[1] = 0;
            } else if ("G".equals(color)) {
                result[1] = 1;
            } else if (AppProtectActionConstant.APP_PROTECT_B.equals(color)) {
                result[1] = 2;
            } else {
                DeLog.e(GetEffectAdapter.TAG, "Invalid input: color=" + color);
                return -2;
            }
            String region = data.getString("Region");
            if (region == null) {
                DeLog.e(GetEffectAdapter.TAG, "Invalid input: region is null!");
                return -2;
            }
            if (HwMultiWinConstants.LEFT_HAND_LAZY_MODE_STR.equals(region)) {
                result[2] = 1;
            } else if (HwMultiWinConstants.RIGHT_HAND_LAZY_MODE_STR.equals(region)) {
                result[2] = 3;
            } else {
                DeLog.e(GetEffectAdapter.TAG, "Invalid input: region=" + region);
                return -2;
            }
            int ret = service.getEffectEx(33, 6, result, result.length);
            if (ret != 0) {
                return ret;
            }
            data.putInt(HwHiNetworkParmStatistics.GAIN_SUB, result[3]);
            return 0;
        }

        private static int getCalibrationTestLevels(IDisplayEngineService service, Bundle data) throws RemoteException {
            int[] result = new int[9];
            int ret = service.getEffectEx(33, 8, result, result.length);
            if (ret != 0) {
                return ret;
            }
            data.putIntArray("levels", result);
            return 0;
        }

        private static int getCalibrationApplyTime(IDisplayEngineService service, Bundle data) throws RemoteException {
            int[] result = new int[1];
            int ret = service.getEffectEx(33, 9, result, result.length);
            if (ret != 0) {
                return ret;
            }
            data.putInt("IntervalInMs", result[0]);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public static class PanelInfoProcessor {
        private static final int DISPLAY_FULL = 1;
        private static final int DISPLAY_MAIN = 0;
        private static final int DISPLAY_NUM = 2;
        private static final int IDX_BOARD_VERSION = 384;
        private static final int IDX_FACTORY_RUN_MODE = 0;
        private static final int IDX_HEIGHT = 8;
        private static final int IDX_IC_TYPE = 256;
        private static final int IDX_LCD_PANEL_VERSION = 28;
        private static final int IDX_MAX_BACKLIGHT = 20;
        private static final int IDX_MAX_LUMINANCE = 12;
        private static final int IDX_MIN_BACKLIGHT = 24;
        private static final int IDX_MIN_LUMINANCE = 16;
        private static final int IDX_PANEL_NAME = 128;
        private static final int IDX_SN_CODE = 64;
        private static final int IDX_SN_CODE_LENGTH = 60;
        private static final int IDX_WIDTH = 4;
        private static final int PANEL_INFO_LENGTH = 388;

        private PanelInfoProcessor() {
        }

        public static int getEffect(IDisplayEngineService service, int type, Bundle data) throws RemoteException {
            if (data == null) {
                DeLog.e(GetEffectAdapter.TAG, "PanelInfoProcessor.getEffect() Invalid input: data is null!");
                return -2;
            } else if (type == 13) {
                return getPanelInfo(service, data);
            } else {
                DeLog.e(GetEffectAdapter.TAG, "PanelInfoProcessor.getEffect() Invalid input: unsupport type=" + type);
                return -2;
            }
        }

        private static int getPanelInfo(IDisplayEngineService service, Bundle data) throws RemoteException {
            byte[] bytes = new byte[(HwFoldScreenState.isInwardFoldDevice() ? 776 : PANEL_INFO_LENGTH)];
            int ret = service.getEffect(14, 13, bytes, bytes.length);
            if (ret != 0) {
                DeLog.e(GetEffectAdapter.TAG, "getPanelInfo -> getEffect(DE_FEATURE_PANELINFO, DE_EFFECT_TYPE_PANEL_INFOS, bytes, bytes.length:" + bytes.length + ") failed, return " + ret);
                return ret;
            }
            data.putInt("FacRunMode", convertBytesToInt(bytes, 0));
            if (HwFoldScreenState.isInwardFoldDevice()) {
                fillPanelInfoBytesToData(bytes, 0, "Main", data);
                fillPanelInfoBytesToData(bytes, PANEL_INFO_LENGTH, "Full", data);
            } else {
                fillPanelInfoBytesToData(bytes, 0, "", data);
            }
            DeLog.i(GetEffectAdapter.TAG, "getPanelInfo -> factoryRunMode:" + data.getInt("FacRunMode"));
            return 0;
        }

        private static void fillPanelInfoBytesToData(byte[] bytes, int base, String name, Bundle data) {
            data.putInt(name + "Width", convertBytesToInt(bytes, base + 4));
            data.putInt(name + "Height", convertBytesToInt(bytes, base + 8));
            data.putInt(name + "MaxLuminance", convertBytesToInt(bytes, base + 12));
            data.putInt(name + "MinLuminance", convertBytesToInt(bytes, base + 16));
            data.putInt(name + "MaxBacklight", convertBytesToInt(bytes, base + 20));
            data.putInt(name + "MinBacklight", convertBytesToInt(bytes, base + 24));
            int index = base + 28;
            data.putString(name + "lcdPanelVersion", new String(bytes, index, indexOfBytesEnd(bytes, index) - index, StandardCharsets.UTF_8));
            int index2 = base + 64;
            data.putByteArray(name + "snCode", Arrays.copyOfRange(bytes, index2, convertBytesToInt(bytes, base + 60) + index2));
            int index3 = base + 128;
            data.putString(name + "panelName", new String(bytes, index3, indexOfBytesEnd(bytes, index3) - index3, StandardCharsets.UTF_8));
            int index4 = base + 256;
            data.putString(name + "icType", new String(bytes, index4, indexOfBytesEnd(bytes, index4) - index4, StandardCharsets.UTF_8));
            data.putInt(name + "boardVersion", convertBytesToInt(bytes, base + IDX_BOARD_VERSION));
            printPanelInfoData(name, data);
        }

        private static void printPanelInfoData(String name, Bundle data) {
            String str;
            String str2;
            if (HwFoldScreenState.isInwardFoldDevice()) {
                StringBuilder sb = new StringBuilder();
                sb.append(name);
                sb.append(" getPanelInfo -> { name=");
                sb.append(data.getString(name + "panelName"));
                sb.append(" res=[");
                sb.append(data.getInt(name + "Width"));
                sb.append("x");
                sb.append(data.getInt(name + "Height"));
                sb.append("] lum=[");
                sb.append(data.getInt(name + "MinLuminance"));
                sb.append(",");
                sb.append(data.getInt(name + "MaxLuminance"));
                sb.append("] bl=[");
                sb.append(data.getInt(name + "MinBacklight"));
                sb.append(",");
                sb.append(data.getInt(name + "MaxBacklight"));
                sb.append("] panelVersion=");
                sb.append(data.getString(name + "lcdPanelVersion"));
                sb.append(" icType=");
                sb.append(data.getString(name + "icType"));
                sb.append(" boardVersion=");
                sb.append(data.getInt(name + "boardVersion"));
                sb.append(" snLen=");
                sb.append(getBytesLength(getByteArray(data, name + "snCode")));
                sb.append(" sn=");
                sb.append(convertBytesToStr(getByteArray(data, name + "snCode")));
                sb.append(" }");
                DeLog.i(GetEffectAdapter.TAG, sb.toString());
                return;
            }
            StringBuilder sb2 = new StringBuilder();
            if (name.length() <= 0) {
                str2 = "getPanelInfo -> { name=";
                str = "MaxBacklight";
            } else {
                str = "MaxBacklight";
                str2 = name + " getPanelInfo -> { name=";
            }
            sb2.append(str2);
            sb2.append(data.getString(name + "panelName"));
            sb2.append(" res=[");
            sb2.append(data.getInt(name + "Width"));
            sb2.append("x");
            sb2.append(data.getInt(name + "Height"));
            sb2.append("] lum=[");
            sb2.append(data.getInt(name + "MinLuminance"));
            sb2.append(",");
            sb2.append(data.getInt(name + "MaxLuminance"));
            sb2.append("] bl=[");
            sb2.append(data.getInt(name + "MinBacklight"));
            sb2.append(",");
            sb2.append(data.getInt(name + str));
            sb2.append("] panelVersion=");
            sb2.append(data.getString(name + "lcdPanelVersion"));
            sb2.append(" icType=");
            sb2.append(data.getString(name + "icType"));
            sb2.append(" boardVersion=");
            sb2.append(data.getInt(name + "boardVersion"));
            sb2.append(" snLen=");
            sb2.append(getBytesLength(getByteArray(data, name + "snCode")));
            sb2.append(" sn=");
            sb2.append(convertBytesToStr(getByteArray(data, name + "snCode")));
            sb2.append(" }");
            DeLog.d(GetEffectAdapter.TAG, sb2.toString());
        }

        /* JADX INFO: Multiple debug info for r0v1 byte[]: [D('bytes' byte[]), D('e' java.lang.ArrayIndexOutOfBoundsException)] */
        private static byte[] getByteArray(Bundle data, String key) {
            try {
                return data.getByteArray(key);
            } catch (ArrayIndexOutOfBoundsException e) {
                DeLog.e(GetEffectAdapter.TAG, "getByteArrayFromBundle(" + key + ") failed: " + e.getMessage());
                return new byte[0];
            }
        }

        private static String convertBytesToStr(byte[] bytes) {
            if (bytes == null) {
                DeLog.e(GetEffectAdapter.TAG, "convertBytesToStr(): bytes is null");
                return "";
            }
            String text = "";
            for (byte b : bytes) {
                text = text + String.format(Locale.ROOT, "%02x", Integer.valueOf(b));
            }
            return text;
        }

        private static int getBytesLength(byte[] bytes) {
            if (bytes != null) {
                return bytes.length;
            }
            DeLog.e(GetEffectAdapter.TAG, "getBytesLength(): bytes is null");
            return 0;
        }

        private static int convertBytesToInt(byte[] bytes, int index) {
            if (index + 3 < bytes.length) {
                return ((bytes[index] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 0) | ((bytes[index + 1] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8) | ((bytes[index + 2] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 16) | ((bytes[index + 3] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 24);
            }
            DeLog.e(GetEffectAdapter.TAG, "convertBytesToInt(): index=" + index + " bytes.length=" + bytes.length);
            return 0;
        }

        private static int indexOfBytesEnd(byte[] bytes, int index) {
            for (int i = index; i < bytes.length; i++) {
                if (bytes[i] == 0) {
                    return i;
                }
            }
            DeLog.e(GetEffectAdapter.TAG, "indexOfBytesEnd(): index=" + index + " can't find end position.");
            return bytes.length;
        }
    }

    /* access modifiers changed from: private */
    public static class MaintenanceProcessor {
        private MaintenanceProcessor() {
        }

        public static int getEffect(IDisplayEngineService service, int type, Bundle data) throws RemoteException {
            if (service.getSupported(37) != 1) {
                DeLog.e(GetEffectAdapter.TAG, "maintenance not supported!");
                return -2;
            } else if (data == null) {
                DeLog.e(GetEffectAdapter.TAG, "MaintenanceProcessor.getEffect() Invalid input: data is null!");
                return -2;
            } else if (type == 8) {
                return getCalibLvls(service, data);
            } else {
                if (type == 14) {
                    return getCalibRange(service, data);
                }
                DeLog.e(GetEffectAdapter.TAG, "MaintenanceProcessor.getEffect() Invalid input: unsupport type=" + type);
                return -2;
            }
        }

        private static int getCalibRange(IDisplayEngineService service, Bundle data) throws RemoteException {
            int[] result = new int[9];
            int ret = service.getEffectEx(38, 14, result, result.length);
            if (ret != 0) {
                DeLog.e(GetEffectAdapter.TAG, "getCalibRange -> getEffectEx(DE_FEATURE_MAINTENANCE, DE_EFFECT_TYPE_CALIB_RANGE, result, result.length:" + result.length + ") failed, return " + ret);
            }
            data.putIntArray("DefaultRanges", new int[]{result[6], result[7], result[8]});
            data.putIntArray("MainDefaultLevels", new int[]{result[0], result[1], result[2]});
            data.putIntArray("FullDefaultLevels", new int[]{result[3], result[4], result[5]});
            return 0;
        }

        private static int getCalibLvls(IDisplayEngineService service, Bundle data) throws RemoteException {
            int[] result = new int[9];
            int ret = service.getEffectEx(38, 14, result, result.length);
            if (ret != 0) {
                DeLog.e(GetEffectAdapter.TAG, "getCalibLvls -> getEffectEx(DE_FEATURE_MAINTENANCE, DE_EFFECT_TYPE_CALIB_RANGE, result, result.length:" + result.length + ") failed, return " + ret);
                return ret;
            }
            String panel = data.getString("DisplayMode", "");
            int[] defaultLevels = new int[3];
            if ("Main".equals(panel)) {
                defaultLevels[0] = result[0];
                defaultLevels[1] = result[1];
                defaultLevels[2] = result[2];
            } else if ("Full".equals(panel)) {
                defaultLevels[0] = result[3];
                defaultLevels[1] = result[4];
                defaultLevels[2] = result[5];
            } else {
                DeLog.e(GetEffectAdapter.TAG, "not find Main or Full");
            }
            data.putIntArray("DefaultLevels", defaultLevels);
            return 0;
        }
    }
}
