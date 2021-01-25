package com.android.server.wifi.hotspot2.omadm;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.SystemInfo;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DevDetailMo {
    private static final String DEVICE_TYPE = "Smartphone";
    @VisibleForTesting
    public static final String HS20_URN = "urn:wfa:mo-ext:hotspot2dot0-devdetail-ext:1.0";
    private static final String IFNAME = "wlan0";
    private static final String INNER_METHOD_MS_CHAP = "MS-CHAP";
    private static final String INNER_METHOD_MS_CHAP_V2 = "MS-CHAP-V2";
    private static final String INNER_METHOD_PAP = "PAP";
    private static final String MO_NAME = "DevDetail";
    private static final String TAG = "DevDetailMo";
    private static final String TAG_CERTIFICATE_ISSUER_NAME = "CertificateIssuerName";
    private static final String TAG_CLIENT_TRIGGER_REDIRECT_URI = "ClientTriggerRedirectURI";
    private static final String TAG_DEV_TYPE = "DevType";
    private static final String TAG_EAP_METHOD = "EAPMethod";
    private static final String TAG_EAP_METHOD_LIST = "EAPMethodList";
    private static final String TAG_EAP_TYPE = "EAPType";
    private static final String TAG_EXT = "ext";
    private static final String TAG_FW_VER = "FwV";
    private static final String TAG_GET_CERTIFICATE = "getCertificate";
    private static final String TAG_HW_VER = "HwV";
    private static final String TAG_IMEI_MEID = "IMEI_MEID";
    private static final String TAG_IMSI = "IMSI";
    private static final String TAG_INNER_EAP_TYPE = "InnerEAPType";
    private static final String TAG_INNER_METHOD = "InnerMethod";
    private static final String TAG_INNER_VENDOR_ID = "InnerVendorID";
    private static final String TAG_INNER_VENDOR_TYPE = "InnerVendorType";
    private static final String TAG_LAUNCH_BROWSER_TO_URI = "launchBrowserToURI";
    private static final String TAG_LRG_ORJ = "LrgOrj";
    private static final String TAG_MANUFACTURING_CERT = "ManufacturingCertificate";
    private static final String TAG_MAX_DEPTH = "MaxDepth";
    private static final String TAG_MAX_SEG_LEN = "MaxSegLen";
    private static final String TAG_MAX_TOT_LEN = "MaxTotLen";
    private static final String TAG_NEGOTIATE_CLIENT_CERT_TLS = "negotiateClientCertTLS";
    private static final String TAG_OEM = "OEM";
    private static final String TAG_OPS = "Ops";
    private static final String TAG_ORG_WIFI = "org.wi-fi";
    private static final String TAG_SP_CERTIFICATE = "SPCertificate";
    private static final String TAG_SW_VER = "SwV";
    private static final String TAG_URI = "URI";
    private static final String TAG_VENDOR_ID = "VendorId";
    private static final String TAG_VENDOR_TYPE = "VendorType";
    private static final String TAG_WIFI = "Wi-Fi";
    private static final String TAG_WIFI_MAC_ADDR = "Wi-FiMACAddress";
    @VisibleForTesting
    public static final String URN = "urn:oma:mo:oma-dm-devdetail:1.0";
    private static boolean sAllowToSendImsiImeiInfo = false;
    private static final List<Pair<Integer, String>> sEapMethods = new ArrayList();
    private static final List<String> sSupportedOps = new ArrayList();

    static {
        sEapMethods.add(Pair.create(21, INNER_METHOD_MS_CHAP_V2));
        sEapMethods.add(Pair.create(21, INNER_METHOD_MS_CHAP));
        sEapMethods.add(Pair.create(21, INNER_METHOD_PAP));
        sEapMethods.add(Pair.create(13, null));
        sEapMethods.add(Pair.create(18, null));
        sEapMethods.add(Pair.create(23, null));
        sEapMethods.add(Pair.create(50, null));
        sSupportedOps.add(TAG_LAUNCH_BROWSER_TO_URI);
    }

    @VisibleForTesting
    public static void setAllowToSendImsiImeiInfo(boolean allowToSendImsiImeiInfo) {
        sAllowToSendImsiImeiInfo = allowToSendImsiImeiInfo;
    }

    public static String serializeToXml(Context context, SystemInfo info, String redirectUri) {
        String macAddress;
        String macAddress2 = info.getMacAddress(IFNAME);
        if (macAddress2 != null) {
            macAddress = macAddress2.replace(":", "");
        } else {
            macAddress = macAddress2;
        }
        if (TextUtils.isEmpty(macAddress)) {
            Log.e(TAG, "mac address is empty");
            return null;
        }
        try {
            MoSerializer moSerializer = new MoSerializer();
            Document doc = moSerializer.createNewDocument();
            Element rootElement = moSerializer.createMgmtTree(doc);
            rootElement.appendChild(moSerializer.writeVersion(doc));
            Element moNode = moSerializer.createNode(doc, MO_NAME);
            moNode.appendChild(moSerializer.createNodeForUrn(doc, URN));
            Element extNode = moSerializer.createNode(doc, TAG_EXT);
            Element orgNode = moSerializer.createNode(doc, TAG_ORG_WIFI);
            orgNode.appendChild(moSerializer.createNodeForUrn(doc, HS20_URN));
            Element wifiNode = moSerializer.createNode(doc, TAG_WIFI);
            Element eapMethodListNode = moSerializer.createNode(doc, TAG_EAP_METHOD_LIST);
            int i = 0;
            for (Pair<Integer, String> entry : sEapMethods) {
                int i2 = i + 1;
                Element eapMethodNode = moSerializer.createNode(doc, String.format("%s%02d", TAG_EAP_METHOD, Integer.valueOf(i2)));
                eapMethodNode.appendChild(moSerializer.createNodeForValue(doc, TAG_EAP_TYPE, ((Integer) entry.first).toString()));
                if (entry.second != null) {
                    eapMethodNode.appendChild(moSerializer.createNodeForValue(doc, TAG_INNER_METHOD, (String) entry.second));
                }
                eapMethodListNode.appendChild(eapMethodNode);
                i = i2;
            }
            wifiNode.appendChild(eapMethodListNode);
            wifiNode.appendChild(moSerializer.createNodeForValue(doc, TAG_MANUFACTURING_CERT, "FALSE"));
            wifiNode.appendChild(moSerializer.createNodeForValue(doc, TAG_CLIENT_TRIGGER_REDIRECT_URI, redirectUri));
            wifiNode.appendChild(moSerializer.createNodeForValue(doc, TAG_WIFI_MAC_ADDR, macAddress));
            String imsi = ((TelephonyManager) context.getSystemService(TelephonyManager.class)).createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).getSubscriberId();
            if (imsi != null && sAllowToSendImsiImeiInfo) {
                wifiNode.appendChild(moSerializer.createNodeForValue(doc, TAG_IMSI, imsi));
                wifiNode.appendChild(moSerializer.createNodeForValue(doc, TAG_IMEI_MEID, info.getDeviceId()));
            }
            Element opsNode = moSerializer.createNode(doc, TAG_OPS);
            for (String op : sSupportedOps) {
                opsNode.appendChild(moSerializer.createNodeForValue(doc, op, ""));
                eapMethodListNode = eapMethodListNode;
                macAddress = macAddress;
            }
            wifiNode.appendChild(opsNode);
            orgNode.appendChild(wifiNode);
            extNode.appendChild(orgNode);
            moNode.appendChild(extNode);
            Element uriNode = moSerializer.createNode(doc, TAG_URI);
            uriNode.appendChild(moSerializer.createNodeForValue(doc, TAG_MAX_DEPTH, "32"));
            uriNode.appendChild(moSerializer.createNodeForValue(doc, TAG_MAX_TOT_LEN, "2048"));
            uriNode.appendChild(moSerializer.createNodeForValue(doc, TAG_MAX_SEG_LEN, "64"));
            moNode.appendChild(uriNode);
            moNode.appendChild(moSerializer.createNodeForValue(doc, TAG_DEV_TYPE, DEVICE_TYPE));
            moNode.appendChild(moSerializer.createNodeForValue(doc, TAG_OEM, info.getDeviceManufacturer()));
            moNode.appendChild(moSerializer.createNodeForValue(doc, TAG_FW_VER, info.getFirmwareVersion()));
            moNode.appendChild(moSerializer.createNodeForValue(doc, TAG_SW_VER, info.getSoftwareVersion()));
            moNode.appendChild(moSerializer.createNodeForValue(doc, TAG_HW_VER, info.getHwVersion()));
            moNode.appendChild(moSerializer.createNodeForValue(doc, TAG_LRG_ORJ, "TRUE"));
            rootElement.appendChild(moNode);
            return moSerializer.serialize(doc);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "failed to create the MoSerializer: " + e);
            return null;
        }
    }
}
