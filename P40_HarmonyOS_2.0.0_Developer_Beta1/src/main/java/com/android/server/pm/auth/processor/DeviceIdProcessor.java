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
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlPullParser;

public class DeviceIdProcessor implements IProcessor {
    private static final String DEVICE_REGEX_GROUP = "(IMEI/\\d+|WIFIMAC/[\\w&&[^_]]+|IMEI/\\d+-\\d+|MEID/[0-9a-fA-F]{14})";
    private static final int FIRST_SLOT = 0;
    private static final int IMEIS_SIZE = 2;
    private static final String IMEI_LIST_REGEX = "IMEI/\\d+";
    private static final String IMEI_SECTION_REGEX = "IMEI/\\d+-\\d+";
    private static final int MAC_SECTION_LENGTH = 2;
    private static final String MEID_REGEX = "MEID/[0-9a-fA-F]{14}";
    private static final int SECOND_SLOT = 1;
    private static final String TOTAL_REGEX = "(IMEI/\\d+|WIFIMAC/[\\w&&[^_]]+|IMEI/\\d+-\\d+|MEID/[0-9a-fA-F]{14})(,(IMEI/\\d+|WIFIMAC/[\\w&&[^_]]+|IMEI/\\d+-\\d+|MEID/[0-9a-fA-F]{14}))*";
    private static final String WIFI_MAC_REGEX = "WIFIMAC/[\\w&&[^_]]+";

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null || !certLine.startsWith(HwCertification.KEY_DEVICE_IDS) || certLine.length() <= HwCertification.KEY_DEVICE_IDS.length() + 1) {
            return false;
        }
        String deviceIds = certLine.substring(HwCertification.KEY_DEVICE_IDS.length() + 1);
        if (TextUtils.isEmpty(deviceIds)) {
            HwAuthLogger.error(IProcessor.TAG, "DI_RC is empty!");
            return false;
        }
        certData.setDeviceIdsString(deviceIds);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String deviceIds = certData.getDeviceIdsString();
        if (TextUtils.isEmpty(deviceIds)) {
            HwAuthLogger.error(IProcessor.TAG, "DI_PC line is null!");
            return false;
        } else if ("*".equals(deviceIds)) {
            hwCert.setReleased(true);
            return true;
        } else if ("#".equals(deviceIds)) {
            hwCert.setReleased(true);
            hwCert.setCustomized(true);
            return true;
        } else if (!deviceIds.matches(TOTAL_REGEX)) {
            HwAuthLogger.error(IProcessor.TAG, "DI_PC irregular id:" + deviceIds);
            return false;
        } else {
            for (String id : deviceIds.split(",")) {
                if (!addDeviceId(id, hwCert)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            return false;
        }
        if (hwCert.isReleased()) {
            return true;
        }
        HwCertificationManager manager = HwCertificationManager.getInstance();
        if (manager == null) {
            return false;
        }
        if (manager.isSystemReady()) {
            List<DeviceId> deviceIds = hwCert.getDeviceIds();
            if (deviceIds == null || deviceIds.isEmpty()) {
                return false;
            }
            Context context = manager.getContext();
            if (context == null) {
                HwAuthLogger.error(IProcessor.TAG, "context is null!");
                return false;
            }
            ArrayList<String> imeis = getImeis(context);
            boolean hasImei = true;
            if (imeis == null || imeis.isEmpty()) {
                hasImei = false;
                HwAuthLogger.error(IProcessor.TAG, "there is no imei on this phone!");
            }
            if (verifyDeviceId(hasImei, deviceIds, imeis, context)) {
                return true;
            }
            HwAuthLogger.error(IProcessor.TAG, "DI_VC error not in list!");
            return false;
        } else if (HwCertificationManager.getInstance().isContainHwCertification(pkg.packageName)) {
            return true;
        } else {
            HwAuthLogger.error(IProcessor.TAG, "system not ready, not in hwCert xml:" + pkg.packageName);
            return false;
        }
    }

    private boolean addDeviceId(String id, HwCertification hwCert) {
        if (TextUtils.isEmpty(id)) {
            return false;
        }
        if (DeviceIdSection.isType(id) && id.length() > "IMEI/".length()) {
            DeviceIdSection deviceIdSection = new DeviceIdSection();
            deviceIdSection.addDeviceId(id.substring("IMEI/".length()));
            hwCert.getDeviceIds().add(deviceIdSection);
            return true;
        } else if (DeviceIdList.isType(id) && id.length() > "IMEI/".length()) {
            DeviceIdList deviceIdList = new DeviceIdList();
            deviceIdList.addDeviceId(id.substring("IMEI/".length()));
            hwCert.getDeviceIds().add(deviceIdList);
            return true;
        } else if (DeviceIdMac.isType(id) && id.length() > "WIFIMAC/".length()) {
            DeviceIdMac deviceIdMac = new DeviceIdMac();
            String rawMac = id.substring("WIFIMAC/".length());
            if (rawMac.length() % 2 != 0) {
                HwAuthLogger.error(IProcessor.TAG, "DI_PC length error!");
                return false;
            }
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < rawMac.length(); i += 2) {
                if (i + 2 <= rawMac.length()) {
                    strBuilder.append(rawMac.substring(i, i + 2));
                }
                if (i != rawMac.length() - 2) {
                    strBuilder.append(AwarenessInnerConstants.COLON_KEY);
                }
            }
            deviceIdMac.addDeviceId(strBuilder.toString());
            hwCert.getDeviceIds().add(deviceIdMac);
            return true;
        } else if (!DeviceIdMeid.isType(id) || id.length() <= "MEID/".length()) {
            HwAuthLogger.error(IProcessor.TAG, "DI_PC irregular!");
            return false;
        } else {
            DeviceIdMeid deviceIdMeid = new DeviceIdMeid();
            deviceIdMeid.addDeviceId(id.substring("MEID/".length()));
            hwCert.getDeviceIds().add(deviceIdMeid);
            return true;
        }
    }

    private boolean verifyDeviceId(boolean hasImei, List<DeviceId> deviceIds, ArrayList<String> imeis, Context context) {
        String meid = getMeid(context);
        List<String> macAddresses = getMacAddresses(context);
        int size = deviceIds.size();
        for (int i = 0; i < size; i++) {
            DeviceId deviceId = deviceIds.get(i);
            if (verifyImei(hasImei, deviceId, imeis) || verifyMac(deviceId, macAddresses) || verifyMeid(deviceId, meid)) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint({"PreferForInArrayList"})
    private boolean verifyImei(boolean hasImei, DeviceId deviceId, ArrayList<String> imeis) {
        if (!hasImei) {
            return false;
        }
        if (!((deviceId instanceof DeviceIdSection) || (deviceId instanceof DeviceIdList))) {
            return false;
        }
        int size = imeis.size();
        for (int i = 0; i < size; i++) {
            if (deviceId.contain(imeis.get(i))) {
                HwAuthLogger.warn(IProcessor.TAG, "DI_VC imei ok debug cert!");
                return true;
            }
        }
        return false;
    }

    private boolean verifyMac(DeviceId deviceId, List<String> macAddresses) {
        if (!(deviceId instanceof DeviceIdMac) || !macAddresses.stream().anyMatch(new Predicate() {
            /* class com.android.server.pm.auth.processor.$$Lambda$DeviceIdProcessor$6SyH6GaMrJ8oPNKKiJ2L95H4ZMI */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DeviceIdProcessor.lambda$verifyMac$0(DeviceId.this, (String) obj);
            }
        })) {
            return false;
        }
        HwAuthLogger.info(IProcessor.TAG, "DI_VC wifi mac ok debug cert!");
        return true;
    }

    private boolean verifyMeid(DeviceId deviceId, String meid) {
        if (!(deviceId instanceof DeviceIdMeid) || TextUtils.isEmpty(meid) || !deviceId.contain(meid)) {
            return false;
        }
        HwAuthLogger.warn(IProcessor.TAG, "DI_VC meid ok debug cert!");
        return true;
    }

    private ArrayList<String> getImeis(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        ArrayList<String> imeis = new ArrayList<>(2);
        if (telephonyManager == null) {
            HwAuthLogger.error(IProcessor.TAG, "failed to get telephony imei!");
            return imeis;
        }
        if (!Utils.isMultiSimEnabled()) {
            String imei = telephonyManager.getImei();
            if (!TextUtils.isEmpty(imei)) {
                imeis.add(imei);
            }
        } else {
            addImeiToList(telephonyManager.getImei(0), telephonyManager.getImei(1), imeis);
        }
        return imeis;
    }

    private void addImeiToList(String imei, String secondImei, ArrayList<String> imeis) {
        if (!TextUtils.isEmpty(imei)) {
            imeis.add(imei);
        }
        if (!TextUtils.isEmpty(secondImei) && !imeis.contains(secondImei)) {
            imeis.add(secondImei);
        }
    }

    private String getMeid(Context context) {
        String meid;
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        if (hwTelephonyManager == null) {
            HwAuthLogger.error(IProcessor.TAG, "failed to get hwTelephonyManager meid!");
            return "";
        }
        if (!Utils.isMultiSimEnabled()) {
            meid = hwTelephonyManager.getMeid();
        } else {
            meid = hwTelephonyManager.getMeid(0);
            if (TextUtils.isEmpty(meid)) {
                meid = hwTelephonyManager.getMeid(1);
            }
        }
        if (meid != null) {
            return meid.toLowerCase(Locale.ROOT);
        }
        return meid;
    }

    private List<String> getMacAddresses(Context context) {
        String macAddress;
        String macAddress2;
        List<String> macAddresses = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        if (wifiManager == null) {
            HwAuthLogger.error(IProcessor.TAG, "wifiManager is null!");
            return macAddresses;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!(wifiInfo == null || (macAddress2 = wifiInfo.getMacAddress()) == null)) {
            macAddresses.add(macAddress2);
        }
        String[] factoryMacAddresses = wifiManager.getFactoryMacAddresses();
        if (!(factoryMacAddresses == null || factoryMacAddresses.length <= 0 || (macAddress = factoryMacAddresses[0]) == null)) {
            macAddresses.add(macAddress);
        }
        return macAddresses;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && parser != null && hwCert != null && HwCertification.KEY_DEVICE_IDS.equals(tag)) {
            String deviceIdsString = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setDeviceIdsString(deviceIdsString);
                return true;
            }
        }
        return false;
    }
}
