package com.android.server.pm.auth.processor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public class DeviceIdProcessor extends BaseProcessor {
    private static final int FIRST_SLOT = 0;
    private static final int SECOND_SLOT = 1;

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_DEVICE_IDS)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_DEVICE_IDS.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e("HwCertificationManager", "readCert:DeviceId is empty");
            return false;
        }
        rawCert.mDeviceIdsString = key;
        return true;
    }

    @SuppressLint({"AvoidMethodInForLoop", "AvoidInHardConnectInString", "PreferForInArrayList"})
    public boolean parserCert(HwCertification rawCert) {
        String deviceids = rawCert.mCertificationData.mDeviceIdsString;
        if (deviceids == null || (deviceids.isEmpty() ^ 1) == 0) {
            HwAuthLogger.e("HwCertificationManager", "parserCert:DeviceId error,deviceids line is null or empty");
            return false;
        } else if ("*".equals(deviceids)) {
            rawCert.setReleaseState(true);
            return true;
        } else {
            String devRegGroup = "(" + ("IMEI/\\d+" + ProcStateStatisData.SEPERATOR_CHAR + "WIFIMAC/[\\w&&[^_]]+" + ProcStateStatisData.SEPERATOR_CHAR + "IMEI/\\d+-\\d+" + ProcStateStatisData.SEPERATOR_CHAR + "MEID/[0-9a-fA-F]{14}") + ")";
            if (deviceids.matches(devRegGroup + "(," + devRegGroup + ")*")) {
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
                        if (rawMac.length() % 2 != 0) {
                            HwAuthLogger.e("HwCertificationManager", "parserCert:DeviceId error,the lenght is wrong");
                            return false;
                        }
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < rawMac.length(); i += 2) {
                            sb.append(rawMac.substring(i, i + 2));
                            if (i != rawMac.length() - 2) {
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
                        HwAuthLogger.e("HwCertificationManager", "parserCert:DeviceId error,irregular");
                        return false;
                    }
                }
                return true;
            }
            HwAuthLogger.e("HwCertificationManager", "parserCert:DeviceId error,irregular deviceids:" + deviceids);
            return false;
        }
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "--Check devices--");
        }
        if (!HwCertificationManager.getIntance().isSystemReady()) {
            HwAuthLogger.w("HwCertificationManager", "verifyCert:deviceId,system is not ready,ignore device ids");
            return true;
        } else if (cert.isReleased()) {
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "verifyCert:deviceId line ok,released cert");
            }
            return true;
        } else {
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
            HwAuthLogger.e("HwCertificationManager", "verifyCert:deviceId error,not in deviceId list");
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
            for (String imei : imeList) {
                if (dev.contain(imei)) {
                    if (HwAuthLogger.getHWDEBUG()) {
                        HwAuthLogger.w("HwCertificationManager", "verifyCert:deviceId line IMEI ok,debug cert");
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
        if (HwAuthLogger.getHWDEBUG()) {
            HwAuthLogger.w("HwCertificationManager", "verifyCert:deviceId line WIFIMACok,debug cert");
        }
        return true;
    }

    private boolean verifyMeid(DeviceId dev, String meid) {
        if (!(dev instanceof DeviceIdMeid) || (TextUtils.isEmpty(meid) ^ 1) == 0 || !dev.contain(meid)) {
            return false;
        }
        HwAuthLogger.w("HwCertificationManager", "verifyCert:deviceId meid line ok,debug cert");
        return true;
    }

    private ArrayList<String> getImeis(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        if (telephony == null) {
            HwAuthLogger.e("HwCertificationManager", "failed to get telephony imei.");
            return null;
        }
        ArrayList<String> imeiList = new ArrayList();
        String imei = "";
        String secondImei = "";
        if (Utils.isMultiSimEnabled()) {
            addImeiInList(telephony.getImei(0), telephony.getImei(1), imeiList);
        } else if (!Utils.isCDMAPhone(telephony.getCurrentPhoneType()) || telephony.getLteOnCdmaMode() == 1) {
            imei = telephony.getImei();
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
        if (!(TextUtils.isEmpty(secondImei) || (imeiList.contains(secondImei) ^ 1) == 0)) {
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
        } else if (Utils.isCDMAPhone(telephony.getCurrentPhoneType())) {
            meid = hwTelephonyManager.getMeid();
        } else {
            HwAuthLogger.w("HwCertificationManager", "not cdma phone, can not get meid.");
        }
        if (meid != null) {
            meid = meid.toLowerCase(Locale.US);
        }
        return meid;
    }

    private String getWifiMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService("wifi");
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
