package com.android.server.location.gnsschrlog;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class GnssChrCommonInfo {
    protected static final int NETWORK_TYPE_CDMA = 2;
    protected static final int NETWORK_TYPE_EVDO = 4;
    protected static final int NETWORK_TYPE_GSM = 0;
    protected static final int NETWORK_TYPE_LTE = 3;
    protected static final int NETWORK_TYPE_UMTS = 1;
    protected static final int NETWORK_TYPE_UNKWON = -1;

    private static String getSoftwareVersion() {
        return SystemProperties.get("ro.build.display.id", AppHibernateCst.INVALID_PKG);
    }

    private static String getHardwareVersion() {
        return SystemProperties.get("ro.product.hardwareversion", AppHibernateCst.INVALID_PKG);
    }

    private static String getChipsetType() {
        String stringtmp = SystemProperties.get("ro.board.platform", AppHibernateCst.INVALID_PKG);
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
            chipset = AppHibernateCst.INVALID_PKG;
        }
        eNCChipsetType.setValue(chipset);
        fileHeadModel.usYear.setValue(getTime().year);
        fileHeadModel.ucMonth.setValue(getTime().month + NETWORK_TYPE_UMTS);
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
        ComHeadModel.strHardwareVersion.setValue(getHardwareVersion());
        TelephonyManager tm = cxt != null ? TelephonyManager.from(cxt) : TelephonyManager.getDefault();
        int phonecount = tm.getPhoneCount();
        if (phonecount == NETWORK_TYPE_UMTS) {
            switch (tm.getPhoneType()) {
                case NETWORK_TYPE_UMTS /*1*/:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case NETWORK_TYPE_CDMA /*2*/:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
                default:
                    return null;
            }
            ComHeadModel.strIMEIorMEID1.setValue(encyptDeviceIdWithSHA(tm.getDeviceId()));
        } else if (phonecount != NETWORK_TYPE_CDMA) {
            return null;
        } else {
            switch (tm.getCurrentPhoneType(NETWORK_TYPE_GSM)) {
                case NETWORK_TYPE_UMTS /*1*/:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case NETWORK_TYPE_CDMA /*2*/:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
            }
            ComHeadModel.strIMEIorMEID1.setValue(encyptDeviceIdWithSHA(tm.getDeviceId(NETWORK_TYPE_GSM)));
            switch (tm.getCurrentPhoneType(NETWORK_TYPE_UMTS)) {
                case NETWORK_TYPE_UMTS /*1*/:
                    ComHeadModel.enDeviceIDType2.setValue("IMEI");
                    break;
                case NETWORK_TYPE_CDMA /*2*/:
                    ComHeadModel.enDeviceIDType2.setValue("MEID");
                    break;
            }
            ComHeadModel.strIMEIorMEID2.setValue(encyptDeviceIdWithSHA(tm.getDeviceId(NETWORK_TYPE_UMTS)));
        }
        return ComHeadModel;
    }

    public CSegCOMHEAD getChrComHead(Context cxt, boolean isNeedSHA) {
        CSegCOMHEAD ComHeadModel = new CSegCOMHEAD();
        if (isNeedSHA) {
            return null;
        }
        ComHeadModel.strSerialNum.setValue(Build.SERIAL);
        ComHeadModel.strHardwareVersion.setValue(getHardwareVersion());
        TelephonyManager tm = cxt != null ? TelephonyManager.from(cxt) : TelephonyManager.getDefault();
        int phonecount = tm.getPhoneCount();
        if (phonecount == NETWORK_TYPE_UMTS) {
            switch (tm.getPhoneType()) {
                case NETWORK_TYPE_UMTS /*1*/:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case NETWORK_TYPE_CDMA /*2*/:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
                default:
                    return null;
            }
            ComHeadModel.strIMEIorMEID1.setValue(tm.getDeviceId());
        } else if (phonecount != NETWORK_TYPE_CDMA) {
            return null;
        } else {
            switch (tm.getCurrentPhoneType(NETWORK_TYPE_GSM)) {
                case NETWORK_TYPE_UMTS /*1*/:
                    ComHeadModel.enDeviceIDType1.setValue("IMEI");
                    break;
                case NETWORK_TYPE_CDMA /*2*/:
                    ComHeadModel.enDeviceIDType1.setValue("MEID");
                    break;
            }
            ComHeadModel.strIMEIorMEID1.setValue(tm.getDeviceId(NETWORK_TYPE_GSM));
            switch (tm.getCurrentPhoneType(NETWORK_TYPE_UMTS)) {
                case NETWORK_TYPE_UMTS /*1*/:
                    ComHeadModel.enDeviceIDType2.setValue("IMEI");
                    break;
                case NETWORK_TYPE_CDMA /*2*/:
                    ComHeadModel.enDeviceIDType2.setValue("MEID");
                    break;
            }
            ComHeadModel.strIMEIorMEID2.setValue(tm.getDeviceId(NETWORK_TYPE_UMTS));
        }
        return ComHeadModel;
    }

    private static String encyptDeviceIdWithSHA(String deviceId) {
        if (deviceId == null) {
            deviceId = AppHibernateCst.INVALID_PKG;
        }
        return SHAUtils.shaBase64(deviceId);
    }

    public int getCardIndex() {
        return NETWORK_TYPE_GSM;
    }
}
