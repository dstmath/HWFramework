package com.huawei.connectivitylog;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import com.huawei.device.connectivitychrlog.CSegCOMHEAD;
import com.huawei.device.connectivitychrlog.CSegFILEHEAD;
import com.huawei.device.connectivitychrlog.ENCChipsetType;
import com.huawei.device.connectivitychrlog.SHAUtils;

public class ChrCommonInfo {
    protected static final int NETWORK_TYPE_CDMA = 2;
    protected static final int NETWORK_TYPE_EVDO = 4;
    protected static final int NETWORK_TYPE_GSM = 0;
    protected static final int NETWORK_TYPE_LTE = 3;
    protected static final int NETWORK_TYPE_UMTS = 1;
    protected static final int NETWORK_TYPE_UNKWON = -1;

    private static String getSoftwareVersion() {
        return SystemProperties.get("ro.build.display.id", "");
    }

    private static String getChipsetType() {
        String stringtmp = SystemProperties.get("ro.board.platform", "");
        if (stringtmp.startsWith("k3") || stringtmp.startsWith("hi")) {
            return "HISILICON";
        }
        if (SystemProperties.getBoolean("ro.config.hisi_soc_type", false)) {
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
        ENCChipsetType eNCChipsetType = fileHeadModel.enChipsetType;
        if (chipset == null) {
            chipset = "";
        }
        eNCChipsetType.setValue(chipset);
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
            switch (tm.getPhoneType()) {
                case 1:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case 2:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
                default:
                    return null;
            }
            ComHeadModel.strIMEIorMEID1.setValue(encyptDeviceIdWithSHA(tm.getDeviceId()));
        } else if (phonecount != 2) {
            return null;
        } else {
            switch (tm.getCurrentPhoneType(0)) {
                case 1:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case 2:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
            }
            ComHeadModel.strIMEIorMEID1.setValue(encyptDeviceIdWithSHA(tm.getDeviceId(0)));
            switch (tm.getCurrentPhoneType(1)) {
                case 1:
                    ComHeadModel.enDeviceIDType2.setValue("IMEI");
                    break;
                case 2:
                    ComHeadModel.enDeviceIDType2.setValue("MEID");
                    break;
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
            switch (tm.getPhoneType()) {
                case 1:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case 2:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
                default:
                    return null;
            }
            ComHeadModel.strIMEIorMEID1.setValue(tm.getDeviceId());
        } else if (phonecount != 2) {
            return null;
        } else {
            switch (tm.getCurrentPhoneType(0)) {
                case 1:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case 2:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
            }
            ComHeadModel.strIMEIorMEID1.setValue(tm.getDeviceId(0));
            switch (tm.getCurrentPhoneType(1)) {
                case 1:
                    ComHeadModel.enDeviceIDType2.setValue("IMEI");
                    break;
                case 2:
                    ComHeadModel.enDeviceIDType2.setValue("MEID");
                    break;
            }
            ComHeadModel.strIMEIorMEID2.setValue(tm.getDeviceId(1));
        }
        return ComHeadModel;
    }

    private static String encyptDeviceIdWithSHA(String deviceId) {
        if (deviceId == null) {
            deviceId = "";
        }
        return SHAUtils.shaBase64(deviceId);
    }

    public int getCardIndex() {
        return 0;
    }
}
