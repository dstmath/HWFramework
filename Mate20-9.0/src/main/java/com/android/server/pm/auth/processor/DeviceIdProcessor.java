package com.android.server.pm.auth.processor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageParser;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.deviceid.DeviceId;
import com.android.server.pm.auth.deviceid.DeviceIdList;
import com.android.server.pm.auth.deviceid.DeviceIdMac;
import com.android.server.pm.auth.deviceid.DeviceIdMeid;
import com.android.server.pm.auth.deviceid.DeviceIdSection;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public class DeviceIdProcessor extends BaseProcessor {
    private static final int FIRST_SLOT = 0;
    private static final int MAC_LENGTH_CONST = 2;
    private static final int SECOND_SLOT = 1;

    public boolean readCert(String line, HwCertification.CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_DEVICE_IDS)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_DEVICE_IDS.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "DI_RC is empty");
            return false;
        }
        rawCert.mDeviceIdsString = key;
        return true;
    }

    @SuppressLint({"AvoidMethodInForLoop", "AvoidInHardConnectInString", "PreferForInArrayList"})
    public boolean parserCert(HwCertification rawCert) {
        String deviceids = rawCert.mCertificationData.mDeviceIdsString;
        if (deviceids == null || deviceids.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "DI_PC line is null");
            return false;
        } else if ("*".equals(deviceids)) {
            rawCert.setReleaseState(true);
            return true;
        } else {
            String oneDevReg = "IMEI/\\d+" + "|" + "WIFIMAC/[\\w&&[^_]]+" + "|" + "IMEI/\\d+-\\d+" + "|" + "MEID/[0-9a-fA-F]{14}";
            String devRegGroup = "(" + oneDevReg + ")";
            if (deviceids.matches(devRegGroup + "(," + devRegGroup + ")*")) {
                return handleVaryDeviceIdType(rawCert, deviceids);
            }
            HwAuthLogger.e("HwCertificationManager", "DI_PC irregular id :" + deviceids);
            return false;
        }
    }

    private boolean handleVaryDeviceIdType(HwCertification rawCert, String deviceids) {
        for (String id : deviceids.split(",")) {
            if (DeviceIdSection.isType(id)) {
                DeviceIdSection dev = new DeviceIdSection();
                dev.addDeviceId(id.substring("IMEI/".length()));
                rawCert.getDeviceIdList().add(dev);
            } else if (DeviceIdList.isType(id)) {
                DeviceIdList dev2 = new DeviceIdList();
                dev2.addDeviceId(id.substring("IMEI/".length()));
                rawCert.getDeviceIdList().add(dev2);
            } else if (DeviceIdMac.isType(id)) {
                DeviceIdMac dev3 = new DeviceIdMac();
                String rawMac = id.substring("WIFIMAC/".length());
                int rawMacLength = rawMac.length();
                if (rawMacLength % 2 != 0) {
                    HwAuthLogger.e("HwCertificationManager", "DI_PC length error");
                    return false;
                }
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < rawMacLength; i += 2) {
                    sb.append(rawMac.substring(i, i + 2));
                    if (i != rawMacLength - 2) {
                        sb.append(":");
                    }
                }
                dev3.addDeviceId(sb.toString());
                rawCert.getDeviceIdList().add(dev3);
            } else if (DeviceIdMeid.isType(id)) {
                DeviceIdMeid dev4 = new DeviceIdMeid();
                dev4.addDeviceId(id.substring("MEID/".length()));
                rawCert.getDeviceIdList().add(dev4);
            } else {
                HwAuthLogger.e("HwCertificationManager", "DI_PC irregular");
                return false;
            }
        }
        return true;
    }

    public boolean verifyCert(PackageParser.Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHwFlow()) {
            HwAuthLogger.i("HwCertificationManager", "DI_VC start");
        }
        if (cert.isReleased()) {
            if (HwAuthLogger.getHwFlow()) {
                HwAuthLogger.i("HwCertificationManager", "DI_VC ok released cert");
            }
            return true;
        } else if (HwCertificationManager.getIntance().isSystemReady()) {
            List<DeviceId> devIds = cert.getDeviceIdList();
            if (devIds == null || devIds.isEmpty()) {
                return false;
            }
            Context context = HwCertificationManager.getIntance().getContext();
            if (context == null) {
                HwAuthLogger.w("HwCertificationManager", "context is null");
                return false;
            }
            boolean hasImei = true;
            ArrayList<String> imeList = getImeis(context);
            if (imeList == null || imeList.isEmpty()) {
                hasImei = false;
                HwAuthLogger.e("HwCertificationManager", "there is no imei on this phone.");
            }
            if (verifyDevId(hasImei, devIds, imeList, context)) {
                return true;
            }
            HwAuthLogger.e("HwCertificationManager", "DI_VC error not in list");
            return false;
        } else if (HwCertificationManager.getIntance().isContainHwCertification(pkg.packageName)) {
            HwAuthLogger.w("HwCertificationManager", "DI_VC ignore ids: " + pkg.packageName);
            return true;
        } else {
            HwAuthLogger.e("HwCertificationManager", "system not ready, not in hwCert xml: " + pkg.packageName);
            return false;
        }
    }

    private boolean verifyDevId(boolean hasImei, List<DeviceId> devIds, ArrayList<String> imeList, Context context) {
        String meid = getMeid(context);
        String wifiMac = getWifiMac(context);
        for (DeviceId dev : devIds) {
            if (verifyImie(hasImei, dev, imeList) || verifyMac(dev, wifiMac)) {
                return true;
            }
            if (verifyMeid(dev, meid)) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint({"PreferForInArrayList"})
    private boolean verifyImie(boolean hasImei, DeviceId dev, ArrayList<String> imeList) {
        if (hasImei && ((dev instanceof DeviceIdSection) || (dev instanceof DeviceIdList))) {
            Iterator<String> it = imeList.iterator();
            while (it.hasNext()) {
                if (dev.contain(it.next())) {
                    if (HwAuthLogger.getHwDebug()) {
                        HwAuthLogger.w("HwCertificationManager", "DI_VC imei ok debuge cert");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean verifyMac(DeviceId dev, String wifiMac) {
        if (!(dev instanceof DeviceIdMac) || !dev.contain(wifiMac)) {
            return false;
        }
        if (HwAuthLogger.getHwDebug()) {
            HwAuthLogger.w("HwCertificationManager", "DI_VC wifimack ok debuge cert");
        }
        return true;
    }

    private boolean verifyMeid(DeviceId dev, String meid) {
        if (!(dev instanceof DeviceIdMeid) || TextUtils.isEmpty(meid) || !dev.contain(meid)) {
            return false;
        }
        HwAuthLogger.w("HwCertificationManager", "DI_VC meid ok debuge cert");
        return true;
    }

    private ArrayList<String> getImeis(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        if (telephony == null) {
            HwAuthLogger.e("HwCertificationManager", "failed to get telephony imei.");
            return null;
        }
        ArrayList<String> imeiList = new ArrayList<>();
        if (Utils.isMultiSimEnabled()) {
            addImeiInList(telephony.getImei(0), telephony.getImei(1), imeiList);
        } else if (!Utils.isCDMAPhone(telephony.getCurrentPhoneType()) || telephony.getLteOnCdmaMode() == 1) {
            String imei = telephony.getImei();
            if (!TextUtils.isEmpty(imei)) {
                imeiList.add(imei);
            }
        } else {
            HwAuthLogger.w("HwCertificationManager", "cdma phone, there is no imei.");
        }
        return imeiList;
    }

    private void addImeiInList(String imei, String secondImei, ArrayList<String> imeiList) {
        if (imeiList == null) {
            HwAuthLogger.w("HwCertificationManager", "list is null, can't add imeis");
            return;
        }
        if (!TextUtils.isEmpty(imei)) {
            imeiList.add(imei);
        }
        if (!TextUtils.isEmpty(secondImei) && !imeiList.contains(secondImei)) {
            imeiList.add(secondImei);
        }
    }

    private String getMeid(Context context) {
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        if (hwTelephonyManager == null || telephony == null) {
            HwAuthLogger.e("HwCertificationManager", "failed to get hwTelephonyManager meid.");
            return null;
        }
        String meid = "";
        if (Utils.isMultiSimEnabled()) {
            meid = hwTelephonyManager.getMeid(0);
            if (TextUtils.isEmpty(meid)) {
                meid = hwTelephonyManager.getMeid(1);
            }
        } else if (!Utils.isCDMAPhone(telephony.getCurrentPhoneType())) {
            HwAuthLogger.w("HwCertificationManager", "not cdma phone, can not get meid.");
        } else {
            meid = hwTelephonyManager.getMeid();
        }
        if (meid != null) {
            meid = meid.toLowerCase(Locale.ENGLISH);
        }
        return meid;
    }

    private String getWifiMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        WifiInfo info = null;
        if (wifi != null) {
            info = wifi.getConnectionInfo();
        }
        if (info == null) {
            HwAuthLogger.e("HwCertificationManager", "WifiInfo == null");
            return "";
        }
        return info.getMacAddress() == null ? "" : info.getMacAddress();
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwCertification.KEY_DEVICE_IDS.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mDeviceIdsString = parser.getAttributeValue(null, "value");
        return true;
    }
}
