package com.huawei.connectivitylog;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import com.huawei.device.connectivitychrlog.CSegCOMHEAD;
import com.huawei.device.connectivitychrlog.CSegFILEHEAD;
import com.huawei.device.connectivitychrlog.SHAUtils;
import com.huawei.uikit.effect.BuildConfig;

public class ChrCommonInfo {
    protected static final int NETWORK_TYPE_CDMA = 2;
    protected static final int NETWORK_TYPE_EVDO = 4;
    protected static final int NETWORK_TYPE_GSM = 0;
    protected static final int NETWORK_TYPE_LTE = 3;
    protected static final int NETWORK_TYPE_UMTS = 1;
    protected static final int NETWORK_TYPE_UNKWON = -1;

    private static String getSoftwareVersion() {
        return SystemProperties.get("ro.build.display.id", BuildConfig.FLAVOR);
    }

    private static String getChipsetType() {
        String stringtmp = SystemProperties.get("ro.board.platform", BuildConfig.FLAVOR);
        if (stringtmp.startsWith("k3") || stringtmp.startsWith("hi") || SystemProperties.getBoolean("ro.config.hisi_soc_type", false)) {
            return "HISILICON";
        }
        return null;
    }

    private static Time getTime() {
        Time time = new Time();
        time.setToNow();
        return time;
    }

    public static CSegFILEHEAD getChrFileHead() {
        CSegFILEHEAD fileHeadModel = new CSegFILEHEAD();
        fileHeadModel.strSoftwareVersion.setValue(getSoftwareVersion());
        String chipset = getChipsetType();
        fileHeadModel.enChipsetType.setValue(chipset != null ? chipset : BuildConfig.FLAVOR);
        fileHeadModel.usYear.setValue(getTime().year);
        fileHeadModel.ucMonth.setValue(getTime().month + 1);
        fileHeadModel.ucDay.setValue(getTime().monthDay);
        fileHeadModel.ucHour.setValue(getTime().hour);
        fileHeadModel.ucMinute.setValue(getTime().minute);
        fileHeadModel.ucSecond.setValue(getTime().second);
        fileHeadModel.usTimeZone.setValue(480);
        return fileHeadModel;
    }

    public static CSegCOMHEAD getChrComHead(Context cxt) {
        CSegCOMHEAD ComHeadModel = new CSegCOMHEAD();
        ComHeadModel.strSerialNum.setValue(encyptDeviceIdWithSHA(Build.SERIAL));
        TelephonyManager tm = cxt != null ? TelephonyManager.from(cxt) : TelephonyManager.getDefault();
        int phonecount = tm.getPhoneCount();
        if (phonecount == 1) {
            int phoneType = tm.getPhoneType();
            if (phoneType == 1) {
                ComHeadModel.enDeviceIDType1.setValue("IMEI");
            } else if (phoneType != 2) {
                return null;
            } else {
                ComHeadModel.enDeviceIDType1.setValue("MEID");
            }
            ComHeadModel.strIMEIorMEID1.setValue(encyptDeviceIdWithSHA(tm.getDeviceId()));
        } else if (phonecount != 2) {
            return null;
        } else {
            int currentPhoneType = tm.getCurrentPhoneType(0);
            if (currentPhoneType == 1) {
                ComHeadModel.enDeviceIDType1.setValue("IMEI");
            } else if (currentPhoneType == 2) {
                ComHeadModel.enDeviceIDType1.setValue("MEID");
            }
            ComHeadModel.strIMEIorMEID1.setValue(encyptDeviceIdWithSHA(tm.getDeviceId(0)));
            int currentPhoneType2 = tm.getCurrentPhoneType(1);
            if (currentPhoneType2 == 1) {
                ComHeadModel.enDeviceIDType2.setValue("IMEI");
            } else if (currentPhoneType2 == 2) {
                ComHeadModel.enDeviceIDType2.setValue("MEID");
            }
            ComHeadModel.strIMEIorMEID2.setValue(encyptDeviceIdWithSHA(tm.getDeviceId(1)));
        }
        return ComHeadModel;
    }

    public CSegCOMHEAD getChrComHead(Context cxt, boolean isNeedSHA) {
        CSegCOMHEAD ComHeadModel = new CSegCOMHEAD();
        if (isNeedSHA) {
            return null;
        }
        ComHeadModel.strSerialNum.setValue(Build.SERIAL);
        TelephonyManager tm = cxt != null ? TelephonyManager.from(cxt) : TelephonyManager.getDefault();
        int phonecount = tm.getPhoneCount();
        if (phonecount == 1) {
            int phoneType = tm.getPhoneType();
            if (phoneType == 1) {
                ComHeadModel.enDeviceIDType1.setValue("IMEI");
            } else if (phoneType != 2) {
                return null;
            } else {
                ComHeadModel.enDeviceIDType1.setValue("MEID");
            }
            ComHeadModel.strIMEIorMEID1.setValue(tm.getDeviceId());
        } else if (phonecount != 2) {
            return null;
        } else {
            int currentPhoneType = tm.getCurrentPhoneType(0);
            if (currentPhoneType == 1) {
                ComHeadModel.enDeviceIDType1.setValue("IMEI");
            } else if (currentPhoneType == 2) {
                ComHeadModel.enDeviceIDType1.setValue("MEID");
            }
            ComHeadModel.strIMEIorMEID1.setValue(tm.getDeviceId(0));
            int currentPhoneType2 = tm.getCurrentPhoneType(1);
            if (currentPhoneType2 == 1) {
                ComHeadModel.enDeviceIDType2.setValue("IMEI");
            } else if (currentPhoneType2 == 2) {
                ComHeadModel.enDeviceIDType2.setValue("MEID");
            }
            ComHeadModel.strIMEIorMEID2.setValue(tm.getDeviceId(1));
        }
        return ComHeadModel;
    }

    private static String encyptDeviceIdWithSHA(String deviceId) {
        return SHAUtils.shaBase64(deviceId != null ? deviceId : BuildConfig.FLAVOR);
    }

    public int getCardIndex() {
        return 0;
    }
}
