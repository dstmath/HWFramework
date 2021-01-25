package com.android.server.wifi.hwUtil;

import android.net.wifi.WifiConfiguration;
import android.util.Log;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwWifiConfigurationXmlUtil {
    public static final String XML_TAG_ACCESS_POINT_TYPE = "WifiApType";
    public static final String XML_TAG_CONFIG_CREATED_BY_CLONE = "CREATED_BY_CLONE";
    public static final String XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN = "ConnectToCellularAndWLAN";
    public static final String XML_TAG_INET_SELF_CURE_HISTORY = "INET_SELF_CURE_HISTORY";
    public static final String XML_TAG_INTERNET_HISTORY = "INTERNET_HISTORY";
    public static final String XML_TAG_LAST_DHCP_RESULTS = "LAST_DHCP_RESULTS";
    public static final String XML_TAG_LAST_HAS_INTERNET_TS = "LAST_HAS_INTERNET_TS";
    public static final String XML_TAG_LAST_TRY_SWTICH_WIFI_TS = "LAST_TRY_SWTICH_WIFI_TS";
    public static final String XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS = "NumNoInternetAccessReports";
    public static final String XML_TAG_ORI_SSID = "OriSsid";
    public static final String XML_TAG_PORTAL_AUTH_TS = "PORTAL_AUTH_TS";
    public static final String XML_TAG_PORTAL_CONNECT = "PORTAL_CONNECT";
    public static final String XML_TAG_PORTAL_NETWORK = "PORTAL_NETWORK";
    public static final String XML_TAG_PORTAL_VALIDITY_DURATION = "PORTAL_VALIDITY_DURATION";
    public static final String XML_TAG_PRIORITY = "Priority";
    public static final String XML_TAG_RANDOMIZED_MAC_SUCCESS_EVER = "RANDOMIZED_MAC_SUCCESS_EVER";
    public static final String XML_TAG_REASSOC_WITH_FAC_MAC = "REASSOC_WITH_FAC_MAC";
    public static final String XML_TAG_VALIDATED_INTERNET_ACCESS = "ValidatedInternetAccess";
    public static final String XML_TAG_WAPI_AS_CERT_PATH = "WapiAsCertPath";
    public static final String XML_TAG_WAPI_PSK_KEY_TYPE = "WapiPskKeyType";
    public static final String XML_TAG_WAPI_USER_CERT_PATH = "WapiUserCertPath";
    public static final String XML_TAG_WIFI_PRO_TEMP_CREATED = "WIFI_PRO_TEMP_CREATED";

    public static void writeExtraCommonElementsToXml(XmlSerializer out, WifiConfiguration configuration) throws XmlPullParserException, IOException {
        XmlUtil.writeNextValue(out, XML_TAG_ORI_SSID, configuration.oriSsid);
        XmlUtil.writeNextValue(out, XML_TAG_PRIORITY, Integer.valueOf(configuration.priority));
        XmlUtil.writeNextValue(out, XML_TAG_WAPI_PSK_KEY_TYPE, Integer.valueOf(configuration.wapiPskTypeBcm));
        XmlUtil.writeNextValue(out, XML_TAG_WAPI_AS_CERT_PATH, configuration.wapiAsCertBcm);
        XmlUtil.writeNextValue(out, XML_TAG_WAPI_USER_CERT_PATH, configuration.wapiUserCertBcm);
        XmlUtil.writeNextValue(out, XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN, Integer.valueOf(configuration.connectToCellularAndWLAN));
        XmlUtil.writeNextValue(out, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_NO_INTERNET_ACCESS_EXPECTED, Boolean.valueOf(configuration.noInternetAccessExpected));
        XmlUtil.writeNextValue(out, XML_TAG_INTERNET_HISTORY, configuration.internetHistory);
        XmlUtil.writeNextValue(out, XML_TAG_PORTAL_NETWORK, Boolean.valueOf(configuration.portalNetwork));
        XmlUtil.writeNextValue(out, XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS, Integer.valueOf(configuration.numNoInternetAccessReports));
    }

    public static void writeToXmlForWifiProConfigStore(XmlSerializer out, WifiConfiguration configuration) throws XmlPullParserException, IOException {
        XmlUtil.writeNextValue(out, XML_TAG_LAST_HAS_INTERNET_TS, Long.valueOf(configuration.lastHasInternetTimestamp));
        XmlUtil.writeNextValue(out, XML_TAG_LAST_TRY_SWTICH_WIFI_TS, Long.valueOf(configuration.lastTrySwitchWifiTimestamp));
        XmlUtil.writeNextValue(out, XML_TAG_WIFI_PRO_TEMP_CREATED, Boolean.valueOf(configuration.isTempCreated));
        XmlUtil.writeNextValue(out, XML_TAG_LAST_DHCP_RESULTS, configuration.lastDhcpResults);
        XmlUtil.writeNextValue(out, XML_TAG_INET_SELF_CURE_HISTORY, configuration.internetSelfCureHistory);
        XmlUtil.writeNextValue(out, XML_TAG_PORTAL_CONNECT, Boolean.valueOf(configuration.isPortalConnect));
        XmlUtil.writeNextValue(out, XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN, Integer.valueOf(configuration.connectToCellularAndWLAN));
        XmlUtil.writeNextValue(out, XML_TAG_ACCESS_POINT_TYPE, Integer.valueOf(configuration.wifiApType));
        XmlUtil.writeNextValue(out, XML_TAG_RANDOMIZED_MAC_SUCCESS_EVER, Boolean.valueOf(configuration.mRandomizedMacSuccessEver));
        XmlUtil.writeNextValue(out, XML_TAG_CONFIG_CREATED_BY_CLONE, Boolean.valueOf(configuration.mIsCreatedByClone));
        XmlUtil.writeNextValue(out, XML_TAG_PORTAL_AUTH_TS, Long.valueOf(configuration.portalAuthTimestamp));
        XmlUtil.writeNextValue(out, XML_TAG_PORTAL_VALIDITY_DURATION, Long.valueOf(configuration.portalValidityDuration));
        XmlUtil.writeNextValue(out, XML_TAG_REASSOC_WITH_FAC_MAC, Boolean.valueOf(configuration.isReassocSelfcureWithFactoryMacAddress));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static void parseWifiConfigFromXml(WifiConfiguration configuration, String name, Object value) {
        char c;
        boolean z = true;
        switch (name.hashCode()) {
            case -1926002814:
                if (name.equals(XML_TAG_INET_SELF_CURE_HISTORY)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1849283492:
                if (name.equals(XML_TAG_REASSOC_WITH_FAC_MAC)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -1597426155:
                if (name.equals(XML_TAG_LAST_TRY_SWTICH_WIFI_TS)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1537262868:
                if (name.equals(XML_TAG_CONFIG_CREATED_BY_CLONE)) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -1337595146:
                if (name.equals(XML_TAG_RANDOMIZED_MAC_SUCCESS_EVER)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -1310532293:
                if (name.equals(XML_TAG_PORTAL_NETWORK)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1217370545:
                if (name.equals(XML_TAG_LAST_HAS_INTERNET_TS)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1100816956:
                if (name.equals(XML_TAG_PRIORITY)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -711148630:
                if (name.equals(XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 159114071:
                if (name.equals(XML_TAG_WAPI_USER_CERT_PATH)) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 180572542:
                if (name.equals(XML_TAG_WAPI_AS_CERT_PATH)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 461626209:
                if (name.equals(XML_TAG_ORI_SSID)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 494456739:
                if (name.equals(XML_TAG_PORTAL_AUTH_TS)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 538467443:
                if (name.equals(XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 553095444:
                if (name.equals(XML_TAG_WAPI_PSK_KEY_TYPE)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 585379985:
                if (name.equals(XML_TAG_LAST_DHCP_RESULTS)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 913629934:
                if (name.equals(XML_TAG_PORTAL_VALIDITY_DURATION)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 1526332790:
                if (name.equals(XML_TAG_INTERNET_HISTORY)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1617589150:
                if (name.equals(XML_TAG_ACCESS_POINT_TYPE)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 1985675705:
                if (name.equals(XML_TAG_WIFI_PRO_TEMP_CREATED)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 2092301303:
                if (name.equals(XML_TAG_PORTAL_CONNECT)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                configuration.priority = ((Integer) value).intValue();
                return;
            case 1:
                configuration.numNoInternetAccessReports = ((Integer) value).intValue();
                configuration.noInternetAccess = !configuration.portalNetwork && configuration.hasNoInternetAccess();
                if (!configuration.noInternetAccess && !configuration.portalNetwork) {
                    z = false;
                }
                configuration.wifiProNoInternetAccess = z;
                updateAccessType(configuration);
                return;
            case 2:
                configuration.internetHistory = (String) value;
                return;
            case 3:
                configuration.portalNetwork = ((Boolean) value).booleanValue();
                return;
            case 4:
                configuration.lastHasInternetTimestamp = ((Long) value).longValue();
                return;
            case 5:
                configuration.lastTrySwitchWifiTimestamp = ((Long) value).longValue();
                return;
            case 6:
                configuration.isTempCreated = ((Boolean) value).booleanValue();
                return;
            case 7:
                configuration.lastDhcpResults = (String) value;
                return;
            case '\b':
                configuration.internetSelfCureHistory = (String) value;
                return;
            case '\t':
                configuration.isPortalConnect = ((Boolean) value).booleanValue();
                return;
            case '\n':
                configuration.oriSsid = (String) value;
                return;
            case 11:
                configuration.wapiPskTypeBcm = ((Integer) value).intValue();
                return;
            case '\f':
                configuration.wapiAsCertBcm = (String) value;
                return;
            case '\r':
                configuration.wapiUserCertBcm = (String) value;
                return;
            case 14:
                configuration.connectToCellularAndWLAN = ((Integer) value).intValue();
                return;
            case 15:
                configuration.wifiApType = ((Integer) value).intValue();
                return;
            case 16:
                configuration.portalAuthTimestamp = ((Long) value).longValue();
                return;
            case 17:
                configuration.portalValidityDuration = ((Long) value).longValue();
                return;
            case 18:
                configuration.mRandomizedMacSuccessEver = ((Boolean) value).booleanValue();
                return;
            case 19:
                configuration.mIsCreatedByClone = ((Boolean) value).booleanValue();
                return;
            case 20:
                if (configuration != null && value != null) {
                    configuration.isReassocSelfcureWithFactoryMacAddress = ((Boolean) value).booleanValue();
                    return;
                }
                return;
            default:
                Log.e("WifiConfig", "unknow config: " + name);
                return;
        }
    }

    public static void updateAccessType(WifiConfiguration configuration) {
        if (configuration != null) {
            if (configuration.noInternetAccess) {
                configuration.internetAccessType = 2;
            }
            if (configuration.portalNetwork) {
                configuration.internetAccessType = 3;
            }
            if (!configuration.wifiProNoInternetAccess) {
                configuration.internetAccessType = 4;
            }
        }
    }
}
