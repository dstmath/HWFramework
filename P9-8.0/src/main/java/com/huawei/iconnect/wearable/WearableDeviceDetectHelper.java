package com.huawei.iconnect.wearable;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.iconnect.config.ConfigFileStruct;
import com.huawei.iconnect.config.btconfig.BtBodyConfigItem;
import com.huawei.iconnect.config.btconfig.condition.HwCondition;
import com.huawei.iconnect.higuide.DeviceGuideUtil;
import com.huawei.iconnect.hwutil.HwLog;
import com.huawei.iconnect.wearable.config.BluetoothDeviceData;
import com.huawei.iconnect.wearable.config.Info;
import java.util.HashMap;

public class WearableDeviceDetectHelper {
    private static final String ACTION_SETUP = "Setup";
    private static final String COMMAND_FROM_SETTINGS = "true";
    private static final String EXTRA_CALLER = "com.huawei.bone.extra.CALLER";
    private static final String EXTRA_DEVICE_MAC_ADDRESS = "com.huawei.bone.extra.DEVICE_MAC_ADDRESS";
    private static final String EXTRA_MANUFACTURER_INFO = "com.huawei.bone.extra.MANUFACTURER_INFO";
    private static final String HIGUIDE_ACTION = "&hi_action=";
    private static final String HIGUIDE_AUTO_DOWNLOAD = "&hi_autodownload=";
    private static final String HIGUIDE_COM_HUAWEI_HIGUIDE_ACTION_LAUNCH = "higuide://com.huawei.higuide.action.LAUNCH?";
    private static final String HIGUIDE_MODEL = "&hi_model=";
    private static final String HIGUIDE_PROTOCOL = "hi_protocol=";
    private static final String HIGUIDE_VENDOR = "&hi_vendor=";
    private static final String HIGUIDE_VERSION = "&hi_version=";
    private static final String HUAWEI_VERSION = "Huawei";
    private static final String HW_PROTOCOL = "Bt_hw";
    private static final String ICONNECT_PACKAGE_NAME = "com.huawei.iconnect";
    private static final String TAG = "WearableDeviceDetectHelper";
    private static ConfigFileStruct mConfigFile;

    public static Info getDetectInfo(Context context, BluetoothDeviceData deviceData) {
        Info info = getDetectInfoImpl(context, deviceData, true);
        if (info == null || !info.isNeedGuide()) {
            return null;
        }
        return info;
    }

    private static boolean isIConnectInstall(Context context) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(ICONNECT_PACKAGE_NAME, 8192);
            if (app != null) {
                HwLog.d(TAG, "get the iconnect " + app.processName);
            }
            return true;
        } catch (NameNotFoundException e) {
            HwLog.d(TAG, "NameNotFoundException iconnect not exist");
            return false;
        }
    }

    public static Info getDetectInfoImpl(Context context, BluetoothDeviceData deviceData, boolean forceLoadFile) {
        HwLog.d(TAG, "enter getDetectInfo:" + deviceData);
        if (context == null || deviceData == null) {
            HwLog.w(TAG, "context == null || device == null");
            return null;
        } else if (isIConnectInstall(context)) {
            ConfigFileStruct configFile = getConfigFile(context, forceLoadFile);
            if (configFile == null) {
                HwLog.w(TAG, "configFile == null");
                return null;
            }
            for (BtBodyConfigItem c : configFile.getBtBodyItems()) {
                c.outputString();
                Info info = c.getInfo(deviceData);
                if (info != null) {
                    HwLog.d(TAG, "getDetectInfo:" + info);
                    return info;
                }
            }
            HwLog.w(TAG, "getDetectInfoImpl return null");
            return null;
        } else {
            HwLog.w(TAG, "iconnect is not install");
            return null;
        }
    }

    public static ConfigFileStruct getConfigFile(Context context, boolean forceLoadFile) {
        if (forceLoadFile || mConfigFile == null) {
            mConfigFile = ConfigFileStruct.getCfgFileStruct(context, false);
        }
        if (mConfigFile == null) {
            HwLog.w(TAG, "configFile==null ");
        }
        return mConfigFile;
    }

    public static boolean startDeviceGuide(Context context, Info deviceInfo, String deviceType, HashMap<Integer, byte[]> manufacturerSpecificData, String address) {
        if (context == null || deviceInfo == null || deviceType == null) {
            HwLog.w(TAG, "context == null||deviceInfo == null||deviceType == null ");
            return false;
        }
        String urlReturn;
        String hiAction = "Setup";
        String hiAutoDownload = "true";
        String hiProtocol = deviceInfo.getProtocol();
        String hiVendor = deviceInfo.getVendor();
        String hiVersion = deviceInfo.getVersion();
        String hiModel = deviceInfo.getModel();
        if (TextUtils.isEmpty(hiVersion)) {
            urlReturn = "higuide://com.huawei.higuide.action.LAUNCH?hi_protocol=" + hiProtocol + "&hi_vendor=" + hiVendor + "&hi_model=" + hiModel + "&hi_action=" + hiAction + "&hi_autodownload=" + hiAutoDownload;
        } else {
            urlReturn = "higuide://com.huawei.higuide.action.LAUNCH?hi_protocol=" + hiProtocol + "&hi_vendor=" + hiVendor + "&hi_model=" + hiModel + "&hi_action=" + hiAction + "&hi_version=" + hiVersion + "&hi_autodownload=" + hiAutoDownload;
        }
        HwLog.d(TAG, "startDeviceGuide: " + urlReturn);
        Intent intentUrl = new Intent();
        intentUrl.setData(Uri.parse(urlReturn));
        if (hiProtocol.equals("Bt_hw") && hiVersion.equals(HUAWEI_VERSION)) {
            intentUrl.putExtra(EXTRA_CALLER, "EMUI");
            intentUrl.putExtra(EXTRA_MANUFACTURER_INFO, manufacturerSpecificData);
            intentUrl.putExtra(EXTRA_DEVICE_MAC_ADDRESS, address);
        }
        DeviceGuideUtil.startDeviceGuide(context, intentUrl);
        return true;
    }

    public static boolean isHuaweiWatchPaired(Context context, BluetoothDeviceData deviceData) {
        if (context == null || deviceData == null || deviceData.getManufacturerSpecificData() == null || deviceData.getManufacturerSpecificData().size() < 1) {
            HwLog.e(TAG, " isHuaweiWatchPaired param check error,return");
            return false;
        }
        int companyId = HwCondition.getCompanyId(deviceData.getManufacturerSpecificData());
        byte[] data = HwCondition.getManufacturerData(deviceData.getManufacturerSpecificData());
        HwLog.d(TAG, "companyId:" + companyId + "  |data:" + manufacturerDataToString(data));
        return HwCondition.isHuaweiTLVData(companyId, data, deviceData.getAddress());
    }

    private static String manufacturerDataToString(byte[] data) {
        if (data == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (byte d : data) {
            result.append(":").append(d);
        }
        return result.toString();
    }
}
