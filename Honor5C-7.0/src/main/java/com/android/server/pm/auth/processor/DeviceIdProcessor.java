package com.android.server.pm.auth.processor;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.pm.auth.HwCertXmlHandler;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.deviceid.DeviceId;
import com.android.server.pm.auth.deviceid.DeviceIdList;
import com.android.server.pm.auth.deviceid.DeviceIdMac;
import com.android.server.pm.auth.deviceid.DeviceIdMeid;
import com.android.server.pm.auth.deviceid.DeviceIdSection;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public class DeviceIdProcessor extends BaseProcessor {
    private static final int FIRST_SLOT = 0;
    private static final int SECOND_SLOT = 1;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_DEVICE_IDS)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_DEVICE_IDS.length() + SECOND_SLOT);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "readCert:DeviceId is empty");
            return false;
        }
        rawCert.mDeviceIdsString = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        String deviceids = rawCert.mCertificationData.mDeviceIdsString;
        if (deviceids == null || deviceids.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "parserCert:DeviceId error,deviceids line is null or empty");
            return false;
        }
        if ("*".equals(deviceids)) {
            rawCert.setReleaseState(true);
            return true;
        }
        String oneDevReg = "IMEI/\\d+" + "|" + "WIFIMAC/[\\w&&[^_]]+" + "|" + "IMEI/\\d+-\\d+" + "|" + "MEID/[0-9a-fA-F]{14}";
        String devRegGroup = "(" + oneDevReg + ")";
        if (deviceids.matches(devRegGroup + "(," + devRegGroup + ")*")) {
            String[] ids = deviceids.split(",");
            int length = ids.length;
            for (int i = FIRST_SLOT; i < length; i += SECOND_SLOT) {
                String id = ids[i];
                if (DeviceIdSection.isType(id)) {
                    DeviceIdSection dev = new DeviceIdSection();
                    dev.addDeviceId(id.substring(DeviceId.TAG_IMEI.length()));
                    rawCert.getDeviceIdList().add(dev);
                } else if (DeviceIdList.isType(id)) {
                    DeviceIdList dev2 = new DeviceIdList();
                    dev2.addDeviceId(id.substring(DeviceId.TAG_IMEI.length()));
                    rawCert.getDeviceIdList().add(dev2);
                } else if (DeviceIdMac.isType(id)) {
                    DeviceIdMac dev3 = new DeviceIdMac();
                    String rawMac = id.substring(DeviceId.TAG_WIFIMAC.length());
                    if (rawMac.length() % 2 != 0) {
                        HwAuthLogger.e(Utils.TAG, "parserCert:DeviceId error,the lenght is wrong");
                        return false;
                    }
                    StringBuffer sb = new StringBuffer();
                    for (int i2 = FIRST_SLOT; i2 < rawMac.length(); i2 += 2) {
                        sb.append(rawMac.substring(i2, i2 + 2));
                        if (i2 != rawMac.length() - 2) {
                            sb.append(":");
                        }
                    }
                    dev3.addDeviceId(sb.toString());
                    rawCert.getDeviceIdList().add(dev3);
                } else if (DeviceIdMeid.isType(id)) {
                    DeviceIdMeid dev4 = new DeviceIdMeid();
                    dev4.addDeviceId(id.substring(DeviceId.TAG_MEID.length()));
                    rawCert.getDeviceIdList().add(dev4);
                } else {
                    HwAuthLogger.e(Utils.TAG, "parserCert:DeviceId error,irregular");
                    return false;
                }
            }
            return true;
        }
        HwAuthLogger.e(Utils.TAG, "parserCert:DeviceId error,irregular deviceids:" + deviceids);
        return false;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(Utils.TAG, "--Check devices--");
        }
        if (!HwCertificationManager.getIntance().isSystemReady()) {
            HwAuthLogger.w(Utils.TAG, "verifyCert:deviceId,system is not ready,ignore device ids");
            return true;
        } else if (cert.isReleased()) {
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i(Utils.TAG, "verifyCert:deviceId line ok,released cert");
            }
            return true;
        } else {
            List<DeviceId> devIds = cert.getDeviceIdList();
            if (devIds == null || devIds.isEmpty()) {
                return false;
            }
            Context context = HwCertificationManager.getIntance().getContext();
            if (context == null) {
                HwAuthLogger.w(Utils.TAG, "context is null");
                return false;
            }
            boolean hasImei = true;
            ArrayList<String> imeList = getImeis(context);
            if (imeList == null || imeList.isEmpty()) {
                hasImei = false;
                HwAuthLogger.e(Utils.TAG, "there is no imei on this phone.");
            }
            String meid = getMeid(context);
            String wifiMac = getWifiMac(context);
            for (DeviceId dev : devIds) {
                if (hasImei && ((dev instanceof DeviceIdSection) || (dev instanceof DeviceIdList))) {
                    for (String imei : imeList) {
                        if (dev.contain(imei)) {
                            if (HwAuthLogger.getHWDEBUG()) {
                                HwAuthLogger.w(Utils.TAG, "verifyCert:deviceId line IMEI ok,debug cert");
                            }
                            return true;
                        }
                    }
                }
                if ((dev instanceof DeviceIdMac) && dev.contain(wifiMac)) {
                    if (HwAuthLogger.getHWDEBUG()) {
                        HwAuthLogger.w(Utils.TAG, "verifyCert:deviceId line WIFIMACok,debug cert");
                    }
                    return true;
                } else if ((dev instanceof DeviceIdMeid) && !TextUtils.isEmpty(meid) && dev.contain(meid)) {
                    HwAuthLogger.w(Utils.TAG, "verifyCert:deviceId meid line ok,debug cert");
                    return true;
                }
            }
            HwAuthLogger.e(Utils.TAG, "verifyCert:deviceId error,not in deviceId list");
            return false;
        }
    }

    private ArrayList<String> getImeis(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        if (telephony == null) {
            HwAuthLogger.e(Utils.TAG, "failed to get telephony imei.");
            return null;
        }
        ArrayList<String> imeiList = new ArrayList();
        String imei = AppHibernateCst.INVALID_PKG;
        String secondImei = AppHibernateCst.INVALID_PKG;
        if (Utils.isMultiSimEnabled()) {
            addImeiInList(telephony.getImei(FIRST_SLOT), telephony.getImei(SECOND_SLOT), imeiList);
        } else if (!Utils.isCDMAPhone(telephony.getCurrentPhoneType()) || telephony.getLteOnCdmaMode() == SECOND_SLOT) {
            imei = telephony.getImei();
            if (!TextUtils.isEmpty(imei)) {
                imeiList.add(imei);
            }
        } else {
            HwAuthLogger.w(Utils.TAG, "cdma phone, there is no imei.");
        }
        return imeiList;
    }

    private void addImeiInList(String imei, String secondImei, ArrayList<String> imeiList) {
        if (imeiList == null) {
            HwAuthLogger.w(Utils.TAG, "list is null, can't add imeis");
            return;
        }
        if (!TextUtils.isEmpty(imei)) {
            imeiList.add(imei);
        }
        if (!(TextUtils.isEmpty(secondImei) || imeiList.contains(secondImei))) {
            imeiList.add(secondImei);
        }
    }

    private String getMeid(Context context) {
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        if (hwTelephonyManager == null || telephony == null) {
            HwAuthLogger.e(Utils.TAG, "failed to get hwTelephonyManager meid.");
            return null;
        }
        String meid = AppHibernateCst.INVALID_PKG;
        if (Utils.isMultiSimEnabled()) {
            meid = hwTelephonyManager.getMeid(FIRST_SLOT);
            if (TextUtils.isEmpty(meid)) {
                meid = hwTelephonyManager.getMeid(SECOND_SLOT);
            }
        } else if (Utils.isCDMAPhone(telephony.getCurrentPhoneType())) {
            meid = hwTelephonyManager.getMeid();
        } else {
            HwAuthLogger.w(Utils.TAG, "not cdma phone, can not get meid.");
        }
        if (meid != null) {
            meid = meid.toLowerCase(Locale.US);
        }
        return meid;
    }

    private String getWifiMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        WifiInfo info = null;
        if (wifi != null) {
            info = wifi.getConnectionInfo();
        }
        if (info == null) {
            HwAuthLogger.e(Utils.TAG, "WifiInfo == null");
            return AppHibernateCst.INVALID_PKG;
        }
        return info.getMacAddress() == null ? AppHibernateCst.INVALID_PKG : info.getMacAddress();
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwCertification.KEY_DEVICE_IDS.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mDeviceIdsString = parser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE);
        return true;
    }
}
