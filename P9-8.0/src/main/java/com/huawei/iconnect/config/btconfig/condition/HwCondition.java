package com.huawei.iconnect.config.btconfig.condition;

import android.os.ParcelUuid;
import android.util.Log;
import com.huawei.iconnect.hwutil.HwLog;
import com.huawei.iconnect.hwutil.Utils;
import com.huawei.iconnect.wearable.config.BluetoothDeviceData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;

public class HwCondition extends AbsCondition {
    private static final String DEVICE_TYPE_BLE = "BLE";
    private static final String DEVICE_TYPE_BR = "BR";
    private static final int GOOGLE_COMPANY_ID = 224;
    private static final int GOOGLE_MANUFACTURER_DATA_OVERSEA_BIT = 2;
    private static final int HUAWEI_COMPANY_ID = 637;
    private static final int HUAWEI_TLV_MIN_LENGTH = 5;
    private static final int INVALID_VALUE = -1;
    private static final int MAC_ADDRESS_LENGTH = 6;
    private static final String TAG = "HwCondition";
    private int companyId = -1;
    private int mData = -1;
    private String[] regexName = null;
    private String standard = null;
    private String[] uuid128 = null;

    private static int getGoogleDeviceType(byte[] manufacturerData) {
        if (manufacturerData != null && manufacturerData.length >= 2) {
            return manufacturerData[1] & 2;
        }
        Log.d(TAG, " manufacturerData == null || manufacturerData.length < 2 ");
        return -1;
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int getHwDeviceType(int companyId, byte[] manufacturerData, String address) {
        if (manufacturerData == null || manufacturerData.length < 2 || !isHuaweiTLVData(companyId, manufacturerData, address)) {
            return -1;
        }
        Log.d(TAG, "isHuaweiTLVData" + ((manufacturerData[0] & 255) << 8) + (manufacturerData[1] & 255));
        return ((manufacturerData[0] & 255) << 8) + (manufacturerData[1] & 255);
    }

    public static byte[] getManufacturerData(HashMap<Integer, byte[]> data) {
        if (data == null || data.size() <= 0) {
            return new byte[0];
        }
        return (byte[]) data.values().iterator().next();
    }

    public static int getCompanyId(HashMap<Integer, byte[]> data) {
        if (data == null || data.size() <= 0) {
            return -1;
        }
        return ((Integer) data.keySet().iterator().next()).intValue();
    }

    public static boolean isHuaweiTLVData(int companyId, byte[] manufacturerData, String address) {
        boolean z = true;
        if (manufacturerData == null || address == null) {
            return false;
        }
        HwLog.d(TAG, "companyId = " + companyId + " manufacturerData:" + toString(manufacturerData) + " address:" + Utils.toMacSecureString(address));
        if (companyId != HUAWEI_COMPANY_ID) {
            return false;
        }
        if (manufacturerData.length >= MAC_ADDRESS_LENGTH) {
            int length = manufacturerData.length;
            Object[] objArr = new Object[MAC_ADDRESS_LENGTH];
            objArr[0] = Byte.valueOf(manufacturerData[length - 6]);
            objArr[1] = Byte.valueOf(manufacturerData[length - 5]);
            objArr[2] = Byte.valueOf(manufacturerData[length - 4]);
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

    static String toString(byte[] datas) {
        if (datas == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (byte data : datas) {
            result.append(":").append(data);
        }
        return result.toString();
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setData(int mData) {
        this.mData = mData;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public void setRegexName(String[] regexName) {
        if (regexName != null) {
            this.regexName = (String[]) regexName.clone();
        }
    }

    public void setUuid128(String[] uuid128) {
        if (uuid128 != null) {
            this.uuid128 = (String[]) uuid128.clone();
        }
    }

    public boolean isMatch(BluetoothDeviceData cmpDeviceData) {
        boolean isMatch = false;
        int cmpCompanyId = getCompanyId(cmpDeviceData.getManufacturerSpecificData());
        Log.d(TAG, "cmpCompanyId of the bluetooth device: " + cmpCompanyId);
        if (this.companyId != -1) {
            isMatch = this.companyId == cmpCompanyId;
            if (!isMatch) {
                return false;
            }
        }
        if (this.mData != -1) {
            byte[] cmpManufacturerData = getManufacturerData(cmpDeviceData.getManufacturerSpecificData());
            int cmpMData = -1;
            if (cmpCompanyId == HUAWEI_COMPANY_ID) {
                cmpMData = getHwDeviceType(cmpCompanyId, cmpManufacturerData, cmpDeviceData.getAddress());
            } else if (cmpCompanyId == GOOGLE_COMPANY_ID) {
                cmpMData = getGoogleDeviceType(cmpManufacturerData);
            } else {
                Log.e(TAG, "unknown manufacture data");
            }
            Log.d(TAG, " cmpData: " + cmpMData);
            isMatch = this.mData == cmpMData;
            if (!isMatch) {
                return false;
            }
        }
        if (this.standard != null) {
            String cmpStandard;
            if (cmpDeviceData.getStandard() != 1) {
                cmpStandard = DEVICE_TYPE_BLE;
            } else {
                cmpStandard = DEVICE_TYPE_BR;
            }
            isMatch = cmpStandard.equals(this.standard);
            if (!isMatch) {
                return false;
            }
        }
        if (this.uuid128 != null) {
            String[] uuids_in = new String[0];
            if (cmpDeviceData.getUuids().length > 0) {
                ParcelUuid[] uuids = cmpDeviceData.getUuids();
                uuids_in = new String[uuids.length];
                for (int i = 0; i < uuids.length; i++) {
                    uuids_in[i] = uuids[i].toString();
                }
            } else {
                Log.e(TAG, "uuid is null ");
            }
            Arrays.sort(uuids_in);
            Arrays.sort(this.uuid128);
            isMatch = Arrays.equals(uuids_in, this.uuid128);
            if (!isMatch) {
                return false;
            }
        }
        String cmpDeviceName = cmpDeviceData.getDeviceName();
        Log.d(TAG, "cmpDeviceName: " + cmpDeviceName);
        if (this.regexName != null) {
            isMatch = isRegexFind(cmpDeviceName);
        }
        return isMatch;
    }

    private boolean isRegexFind(String localDeviceName) {
        if (this.regexName == null || localDeviceName == null) {
            return false;
        }
        try {
            String[] strArr = this.regexName;
            int length = strArr.length;
            int i = 0;
            while (i < length) {
                String regex = strArr[i];
                if (regex == null || regex.isEmpty() || !ConditionUtil.isRegexFind(regex, localDeviceName)) {
                    i++;
                } else {
                    Log.d(TAG, "regexName find");
                    return true;
                }
            }
        } catch (PatternSyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return false;
    }

    public String toString() {
        return "HwCondition{companyId=" + this.companyId + ", mData=" + this.mData + ", regexName= " + Arrays.toString(this.regexName) + ", standard='" + this.standard + '\'' + ", uuid128=" + Arrays.toString(this.uuid128) + '}';
    }
}
