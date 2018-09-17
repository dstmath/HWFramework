package com.huawei.iconnect.wearable;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.SystemProperties;
import java.util.HashMap;
import java.util.Locale;

public class ManufacturerDataHelper {
    public static final int BLE_DEVICE_BAND = 1;
    public static final int BLE_DEVICE_INVALID = 0;
    public static final int BLE_DEVICE_WATCH = 2;
    private static final int GOOGLE_COMPANY_ID = 224;
    private static final int HUAWEI_COMPANY_ID = 637;
    private static final int HUAWEI_TLV_MIN_LENGTH = 5;
    private static final String HUAWEI_WATCH_NAME = "huawei watch";
    public static final String HUAWEI_WATCH_NAME_LOCAL = "HUAWEI-WATCH";
    private static final String HUAWEI_WATCH_PORSCHE_NAME = "PORSCHE DESIGN";
    private static final int MAC_ADDRESS_LENGTH = 6;
    private static final String TAG = "ManufacturerDataHelper";

    static boolean isHuaweiBleDevice(int companyId) {
        return companyId == HUAWEI_COMPANY_ID;
    }

    static boolean isHuaweiBandByName(String name) {
        if (name == null) {
            return false;
        }
        int i;
        HwLog.d(TAG, "isHuaweiBandByName:" + name);
        String[] exceptBandNames = new String[BLE_DEVICE_BAND];
        exceptBandNames[BLE_DEVICE_INVALID] = "honor band A1";
        int length = exceptBandNames.length;
        for (i = BLE_DEVICE_INVALID; i < length; i += BLE_DEVICE_BAND) {
            if (name.startsWith(exceptBandNames[i])) {
                return false;
            }
        }
        String[] bandNameArray = new String[HUAWEI_TLV_MIN_LENGTH];
        bandNameArray[BLE_DEVICE_INVALID] = "honor zero-";
        bandNameArray[BLE_DEVICE_BAND] = "honor band-";
        bandNameArray[BLE_DEVICE_WATCH] = "honor band Z1-";
        bandNameArray[3] = "HUAWEI Band-";
        bandNameArray[4] = "HUAWEI B3-";
        length = bandNameArray.length;
        for (i = BLE_DEVICE_INVALID; i < length; i += BLE_DEVICE_BAND) {
            if (name.startsWith(bandNameArray[i])) {
                return true;
            }
        }
        return false;
    }

    static boolean isHuaweiWatchByName(String name) {
        if (name != null) {
            return (name.toLowerCase(Locale.ENGLISH).contains(HUAWEI_WATCH_NAME) || name.startsWith(HUAWEI_WATCH_PORSCHE_NAME)) ? true : name.startsWith(HUAWEI_WATCH_NAME_LOCAL);
        } else {
            return false;
        }
    }

    public static boolean isGoogleBleDevice(int companyId, String name) {
        if (companyId != GOOGLE_COMPANY_ID || name == null) {
            return false;
        }
        return (name.toLowerCase().contains(HUAWEI_WATCH_NAME) || name.startsWith(HUAWEI_WATCH_PORSCHE_NAME)) ? true : name.startsWith(HUAWEI_WATCH_NAME_LOCAL);
    }

    static int getBleDeviceType(HashMap<Integer, byte[]> data, String address, String name) {
        return getBleDeviceType(getCompanyId(data), getManufacturerData(data), address, name);
    }

    public static int getBleDeviceType(int companyId, byte[] manufacturerData, String address, String name) {
        if (manufacturerData == null || manufacturerData.length < BLE_DEVICE_WATCH) {
            return -1;
        }
        if (isHuaweiTLVData(companyId, manufacturerData, address)) {
            return ((manufacturerData[BLE_DEVICE_INVALID] & 255) << 8) + (manufacturerData[BLE_DEVICE_BAND] & 255);
        }
        if (isGoogleBleDevice(companyId, name)) {
            return manufacturerData[BLE_DEVICE_BAND] & BLE_DEVICE_WATCH;
        }
        return -1;
    }

    public static boolean isHuaweiTLVData(int companyId, byte[] manufacturerData, String address) {
        boolean z = true;
        if (manufacturerData == null || address == null) {
            return false;
        }
        HwLog.d(TAG, "companyId = " + companyId + " manufacturerData:" + toString(manufacturerData) + " address:" + address.substring(address.length() / BLE_DEVICE_WATCH));
        if (companyId != HUAWEI_COMPANY_ID) {
            return false;
        }
        if (manufacturerData.length >= MAC_ADDRESS_LENGTH) {
            int length = manufacturerData.length;
            Object[] objArr = new Object[MAC_ADDRESS_LENGTH];
            objArr[BLE_DEVICE_INVALID] = Byte.valueOf(manufacturerData[length - 6]);
            objArr[BLE_DEVICE_BAND] = Byte.valueOf(manufacturerData[length - 5]);
            objArr[BLE_DEVICE_WATCH] = Byte.valueOf(manufacturerData[length - 4]);
            objArr[3] = Byte.valueOf(manufacturerData[length - 3]);
            objArr[4] = Byte.valueOf(manufacturerData[length - 2]);
            objArr[HUAWEI_TLV_MIN_LENGTH] = Byte.valueOf(manufacturerData[length - 1]);
            if (String.format("%02X:%02X:%02X:%02X:%02X:%02X", objArr).equalsIgnoreCase(address)) {
                if (manufacturerData.length < 11) {
                    z = false;
                }
                return z;
            }
        }
        if (manufacturerData.length < HUAWEI_TLV_MIN_LENGTH) {
            z = false;
        }
        return z;
    }

    public static int getRemoteBleDeviceType(Context context, HashMap<Integer, byte[]> data, String address, String name) {
        return getRemoteBleDeviceType(context, getCompanyId(data), getManufacturerData(data), address, name);
    }

    private static int getRemoteBleDeviceType(Context context, int companyId, byte[] manufacturerData, String address, String name) {
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, companyId, manufacturerData, address, name);
        if (packageName == null) {
            return BLE_DEVICE_INVALID;
        }
        if (CompanionAppHelper.HUAWEI_WARE_PACKAGE_NAME.equals(packageName)) {
            return BLE_DEVICE_BAND;
        }
        if (CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || (name != null && name.startsWith(HUAWEI_WATCH_NAME_LOCAL))) {
            return BLE_DEVICE_WATCH;
        }
        return BLE_DEVICE_INVALID;
    }

    public static int getRemoteBleDeviceType(Context context, HashMap<Integer, byte[]> datas, BluetoothDevice device) {
        if (context == null || device == null) {
            HwLog.e(TAG, "getRemoteBleDeviceType param check error return");
            return BLE_DEVICE_INVALID;
        }
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, datas, device);
        if (packageName == null) {
            return BLE_DEVICE_INVALID;
        }
        if (CompanionAppHelper.HUAWEI_WARE_PACKAGE_NAME.equals(packageName)) {
            return BLE_DEVICE_BAND;
        }
        if (CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || "".equals(packageName)) {
            return BLE_DEVICE_WATCH;
        }
        return BLE_DEVICE_INVALID;
    }

    public static boolean isPhoneAndDeviceTypeUnmatched(Context context, HashMap<Integer, byte[]> datas, BluetoothDevice device) {
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, datas, device);
        if (packageName == null) {
            return false;
        }
        boolean equals;
        boolean isGlobalVersion = isGlobalVersion();
        if (!(CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) && isGlobalVersion) && (!CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || isGlobalVersion)) {
            equals = packageName.equals("");
        } else {
            equals = true;
        }
        return equals;
    }

    public static boolean isPhoneAndDeviceTypeUnmatched(Context context, HashMap<Integer, byte[]> data, String address, String name) {
        boolean z = true;
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, data, address, name);
        if (packageName == null) {
            return false;
        }
        boolean isGlobalVersion = isGlobalVersion();
        if (!(CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) && isGlobalVersion) && (!CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || isGlobalVersion)) {
            z = false;
        }
        return z;
    }

    private static boolean isGlobalVersion() {
        return ("zh".equals(SystemProperties.get("ro.product.locale.language")) && "CN".equals(SystemProperties.get("ro.product.locale.region"))) ? false : true;
    }

    public static boolean isHuaweiWatchPaired(Context context, HashMap<Integer, byte[]> data, String address, String name) {
        return isHuaweiWatchPaired(context, getCompanyId(data), getManufacturerData(data), address, name);
    }

    public static boolean isHuaweiWatchPaired(Context context, int companyId, byte[] manufacturerData, String address, String name) {
        if (isHuaweiTLVData(companyId, manufacturerData, address)) {
            String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, companyId, manufacturerData, address, name);
            if (CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHuaweiWatchPaired(Context context, HashMap<Integer, byte[]> datas, BluetoothDevice device) {
        if (context == null || datas == null || datas.size() < BLE_DEVICE_BAND || device == null) {
            HwLog.e(TAG, "isHuaweiWatchPaired param check error,return");
            return false;
        }
        if (isHuaweiTLVData(getCompanyId(datas), getManufacturerData(datas), device.getAddress())) {
            String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, datas, device);
            if (CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    static int getCompanyId(HashMap<Integer, byte[]> data) {
        if (data == null || data.size() <= 0) {
            return -1;
        }
        return ((Integer) data.keySet().iterator().next()).intValue();
    }

    static byte[] getManufacturerData(HashMap<Integer, byte[]> data) {
        if (data == null || data.size() <= 0) {
            return null;
        }
        return (byte[]) data.values().iterator().next();
    }

    static String toString(byte[] datas) {
        if (datas == null) {
            return null;
        }
        String result = "";
        int length = datas.length;
        for (int i = BLE_DEVICE_INVALID; i < length; i += BLE_DEVICE_BAND) {
            result = result + ":" + datas[i];
        }
        return result;
    }

    public static boolean isSupportAutoReconnect(Context context, int id, byte[] data, String address, String name) {
        boolean z = false;
        if (context == null || ((data == null && name == null) || address == null)) {
            HwLog.e(TAG, "isSupportAutoReconnect param check error");
            return false;
        }
        CompanionAppHelper.loadIfNeeded(context);
        Boolean support;
        if (isHuaweiBleDevice(id)) {
            support = (Boolean) CompanionAppHelper.mHwBleReconnectMap.get(getBleDeviceType(id, data, address, name));
            HwLog.d(TAG, "huawei bone support:" + support);
            if (support != null) {
                z = support.booleanValue();
            }
            return z;
        } else if (isGoogleBleDevice(id, name)) {
            support = (Boolean) CompanionAppHelper.mGoogleReconnecMap.get(getBleDeviceType(id, data, address, name));
            HwLog.d(TAG, "huawei watch:" + support);
            if (support != null) {
                z = support.booleanValue();
            }
            return z;
        } else if (!isHuaweiWatchByName(name)) {
            return false;
        } else {
            HwLog.d(TAG, "is huawei watch by name,return true:" + name);
            return true;
        }
    }
}
