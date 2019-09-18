package com.android.server.wifi;

import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class WifiBackupDataV1Parser implements WifiBackupDataParser {
    private static final int HIGHEST_SUPPORTED_MINOR_VERSION = 0;
    private static final Set<String> IP_CONFIGURATION_MINOR_V0_SUPPORTED_TAGS = new HashSet(Arrays.asList(new String[]{XmlUtil.IpConfigurationXmlUtil.XML_TAG_IP_ASSIGNMENT, XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_ADDRESS, XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_PREFIX_LENGTH, XmlUtil.IpConfigurationXmlUtil.XML_TAG_GATEWAY_ADDRESS, XmlUtil.IpConfigurationXmlUtil.XML_TAG_DNS_SERVER_ADDRESSES, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_SETTINGS, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_HOST, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PORT, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_EXCLUSION_LIST, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PAC_FILE}));
    private static final String TAG = "WifiBackupDataV1Parser";
    private static final Set<String> WIFI_CONFIGURATION_MINOR_V0_SUPPORTED_TAGS = new HashSet(Arrays.asList(new String[]{XmlUtil.WifiConfigurationXmlUtil.XML_TAG_CONFIG_KEY, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SSID, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ORI_SSID, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_NO_INTERNET_ACCESS_EXPECTED, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_BSSID, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_PRE_SHARED_KEY, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_KEYS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_TX_KEY_INDEX, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_HIDDEN_SSID, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_REQUIRE_PMF, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_KEY_MGMT, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PROTOCOLS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_AUTH_ALGOS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_GROUP_CIPHERS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PAIRWISE_CIPHERS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SHARED}));

    /* renamed from: com.android.server.wifi.WifiBackupDataV1Parser$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$IpConfiguration$IpAssignment = new int[IpConfiguration.IpAssignment.values().length];
        static final /* synthetic */ int[] $SwitchMap$android$net$IpConfiguration$ProxySettings = new int[IpConfiguration.ProxySettings.values().length];

        static {
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.STATIC.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.PAC.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.NONE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.UNASSIGNED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$IpAssignment[IpConfiguration.IpAssignment.STATIC.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$IpAssignment[IpConfiguration.IpAssignment.DHCP.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$IpAssignment[IpConfiguration.IpAssignment.UNASSIGNED.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    WifiBackupDataV1Parser() {
    }

    public List<WifiConfiguration> parseNetworkConfigurationsFromXml(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        if (minorVersion > 0) {
            minorVersion = 0;
        }
        XmlUtil.gotoNextSectionWithName(in, "NetworkList", outerTagDepth);
        int networkListTagDepth = outerTagDepth + 1;
        List<WifiConfiguration> configurations = new ArrayList<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, "Network", networkListTagDepth)) {
            WifiConfiguration configuration = parseNetworkConfigurationFromXml(in, minorVersion, networkListTagDepth);
            if (configuration != null) {
                Log.v(TAG, "Parsed Configuration: " + configuration.configKey());
                configurations.add(configuration);
            }
        }
        return configurations;
    }

    private WifiConfiguration parseNetworkConfigurationFromXml(XmlPullParser in, int minorVersion, int outerTagDepth) throws XmlPullParserException, IOException {
        int networkTagDepth = outerTagDepth + 1;
        XmlUtil.gotoNextSectionWithName(in, "WifiConfiguration", networkTagDepth);
        int configTagDepth = networkTagDepth + 1;
        WifiConfiguration configuration = parseWifiConfigurationFromXmlAndValidateConfigKey(in, configTagDepth, minorVersion);
        if (configuration == null) {
            return null;
        }
        XmlUtil.gotoNextSectionWithName(in, "IpConfiguration", networkTagDepth);
        configuration.setIpConfiguration(parseIpConfigurationFromXml(in, configTagDepth, minorVersion));
        return configuration;
    }

    private WifiConfiguration parseWifiConfigurationFromXmlAndValidateConfigKey(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        Pair<String, WifiConfiguration> parsedConfig = parseWifiConfigurationFromXml(in, outerTagDepth, minorVersion);
        if (parsedConfig == null || parsedConfig.first == null || parsedConfig.second == null) {
            return null;
        }
        String configKeyParsed = (String) parsedConfig.first;
        WifiConfiguration configuration = (WifiConfiguration) parsedConfig.second;
        if (!configKeyParsed.equals(configuration.configKey())) {
            String configKeyMismatchLog = "Configuration key does not match. Retrieved: " + configKeyParsed + ", Calculated: " + configKeyCalculated;
            if (configuration.shared) {
                Log.e(TAG, configKeyMismatchLog);
                return null;
            }
            Log.w(TAG, configKeyMismatchLog);
        }
        return configuration;
    }

    private static void clearAnyKnownIssuesInParsedConfiguration(WifiConfiguration config) {
        if (config.allowedKeyManagement.length() > WifiConfiguration.KeyMgmt.strings.length) {
            config.allowedKeyManagement.clear(WifiConfiguration.KeyMgmt.strings.length, config.allowedKeyManagement.length());
        }
        if (config.allowedProtocols.length() > WifiConfiguration.Protocol.strings.length) {
            config.allowedProtocols.clear(WifiConfiguration.Protocol.strings.length, config.allowedProtocols.length());
        }
        if (config.allowedAuthAlgorithms.length() > WifiConfiguration.AuthAlgorithm.strings.length) {
            config.allowedAuthAlgorithms.clear(WifiConfiguration.AuthAlgorithm.strings.length, config.allowedAuthAlgorithms.length());
        }
        if (config.allowedGroupCiphers.length() > WifiConfiguration.GroupCipher.strings.length) {
            config.allowedGroupCiphers.clear(WifiConfiguration.GroupCipher.strings.length, config.allowedGroupCiphers.length());
        }
        if (config.allowedPairwiseCiphers.length() > WifiConfiguration.PairwiseCipher.strings.length) {
            config.allowedPairwiseCiphers.clear(WifiConfiguration.PairwiseCipher.strings.length, config.allowedPairwiseCiphers.length());
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00c9, code lost:
        if (r7.equals(com.android.server.wifi.util.XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SSID) != false) goto L_0x0102;
     */
    private static Pair<String, WifiConfiguration> parseWifiConfigurationFromXml(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        WifiConfiguration configuration = new WifiConfiguration();
        String configKeyInData = null;
        Set<String> supportedTags = getSupportedWifiConfigurationTags(minorVersion);
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            char c = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            String tagName = valueName[0];
            if (tagName == null) {
                throw new XmlPullParserException("Missing value name");
            } else if (!supportedTags.contains(tagName)) {
                Log.w(TAG, "Unsupported tag + \"" + tagName + "\" found in <WifiConfiguration> section, ignoring.");
            } else {
                switch (tagName.hashCode()) {
                    case -1819699067:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SHARED)) {
                            c = 16;
                            break;
                        }
                    case -1704616680:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_KEY_MGMT)) {
                            c = 11;
                            break;
                        }
                    case -711148630:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN)) {
                            c = 3;
                            break;
                        }
                    case -244338402:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_NO_INTERNET_ACCESS_EXPECTED)) {
                            c = 4;
                            break;
                        }
                    case -181205965:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PROTOCOLS)) {
                            c = 12;
                            break;
                        }
                    case 2554747:
                        break;
                    case 63507133:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_BSSID)) {
                            c = 5;
                            break;
                        }
                    case 461626209:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ORI_SSID)) {
                            c = 2;
                            break;
                        }
                    case 682791106:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PAIRWISE_CIPHERS)) {
                            c = 15;
                            break;
                        }
                    case 736944625:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_GROUP_CIPHERS)) {
                            c = 14;
                            break;
                        }
                    case 797043831:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_PRE_SHARED_KEY)) {
                            c = 6;
                            break;
                        }
                    case 1199498141:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_CONFIG_KEY)) {
                            c = 0;
                            break;
                        }
                    case 1851050768:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_AUTH_ALGOS)) {
                            c = 13;
                            break;
                        }
                    case 1905126713:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_TX_KEY_INDEX)) {
                            c = 8;
                            break;
                        }
                    case 1955037270:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_KEYS)) {
                            c = 7;
                            break;
                        }
                    case 1965854789:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_HIDDEN_SSID)) {
                            c = 9;
                            break;
                        }
                    case 2143705732:
                        if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_REQUIRE_PMF)) {
                            c = 10;
                            break;
                        }
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        configKeyInData = (String) value;
                        break;
                    case 1:
                        configuration.SSID = (String) value;
                        break;
                    case 2:
                        configuration.oriSsid = (String) value;
                        break;
                    case 3:
                        configuration.connectToCellularAndWLAN = ((Integer) value).intValue();
                        break;
                    case 4:
                        configuration.noInternetAccessExpected = ((Boolean) value).booleanValue();
                        break;
                    case 5:
                        configuration.BSSID = (String) value;
                        break;
                    case 6:
                        configuration.preSharedKey = (String) value;
                        break;
                    case 7:
                        populateWepKeysFromXmlValue(value, configuration.wepKeys);
                        break;
                    case 8:
                        configuration.wepTxKeyIndex = ((Integer) value).intValue();
                        break;
                    case 9:
                        configuration.hiddenSSID = ((Boolean) value).booleanValue();
                        break;
                    case 10:
                        configuration.requirePMF = ((Boolean) value).booleanValue();
                        break;
                    case 11:
                        configuration.allowedKeyManagement = BitSet.valueOf((byte[]) value);
                        break;
                    case 12:
                        configuration.allowedProtocols = BitSet.valueOf((byte[]) value);
                        break;
                    case 13:
                        configuration.allowedAuthAlgorithms = BitSet.valueOf((byte[]) value);
                        break;
                    case 14:
                        configuration.allowedGroupCiphers = BitSet.valueOf((byte[]) value);
                        break;
                    case 15:
                        configuration.allowedPairwiseCiphers = BitSet.valueOf((byte[]) value);
                        break;
                    case 16:
                        configuration.shared = ((Boolean) value).booleanValue();
                        break;
                    default:
                        throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                }
            }
        }
        clearAnyKnownIssuesInParsedConfiguration(configuration);
        return Pair.create(configKeyInData, configuration);
    }

    private static Set<String> getSupportedWifiConfigurationTags(int minorVersion) {
        if (minorVersion == 0) {
            return WIFI_CONFIGURATION_MINOR_V0_SUPPORTED_TAGS;
        }
        Log.e(TAG, "Invalid minorVersion: " + minorVersion);
        return Collections.emptySet();
    }

    private static void populateWepKeysFromXmlValue(Object value, String[] wepKeys) throws XmlPullParserException, IOException {
        String[] wepKeysInData = (String[]) value;
        if (wepKeysInData != null) {
            if (wepKeysInData.length == wepKeys.length) {
                for (int i = 0; i < wepKeys.length; i++) {
                    if (wepKeysInData[i].isEmpty()) {
                        wepKeys[i] = null;
                    } else {
                        wepKeys[i] = wepKeysInData[i];
                    }
                }
                return;
            }
            throw new XmlPullParserException("Invalid Wep Keys length: " + wepKeysInData.length);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v14, resolved type: java.lang.String[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v20, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v25, resolved type: java.lang.String} */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Multi-variable type inference failed */
    private static IpConfiguration parseIpConfigurationFromXml(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        Set<String> supportedTags;
        char c;
        Set<String> supportedTags2 = getSupportedIpConfigurationTags(minorVersion);
        String ipAssignmentString = null;
        String linkAddressString = null;
        Integer linkPrefixLength = null;
        String gatewayAddressString = null;
        String[] dnsServerAddressesString = null;
        String proxySettingsString = null;
        String proxyHost = null;
        int proxyPort = -1;
        String proxyExclusionList = null;
        String proxyPacFile = null;
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            String tagName = valueName[0];
            if (tagName != null) {
                if (!supportedTags2.contains(tagName)) {
                    StringBuilder sb = new StringBuilder();
                    supportedTags = supportedTags2;
                    sb.append("Unsupported tag + \"");
                    sb.append(tagName);
                    sb.append("\" found in <IpConfiguration> section, ignoring.");
                    Log.w(TAG, sb.toString());
                } else {
                    supportedTags = supportedTags2;
                    switch (tagName.hashCode()) {
                        case -1747338169:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_DNS_SERVER_ADDRESSES)) {
                                c = 4;
                                break;
                            }
                        case -1520820614:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_ADDRESS)) {
                                c = 1;
                                break;
                            }
                        case -1464842926:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_PREFIX_LENGTH)) {
                                c = 2;
                                break;
                            }
                        case -920546460:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PAC_FILE)) {
                                c = 9;
                                break;
                            }
                        case 162774900:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_IP_ASSIGNMENT)) {
                                c = 0;
                                break;
                            }
                        case 858907952:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_GATEWAY_ADDRESS)) {
                                c = 3;
                                break;
                            }
                        case 1527606550:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_HOST)) {
                                c = 6;
                                break;
                            }
                        case 1527844847:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PORT)) {
                                c = 7;
                                break;
                            }
                        case 1940148190:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_EXCLUSION_LIST)) {
                                c = 8;
                                break;
                            }
                        case 1968819345:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_SETTINGS)) {
                                c = 5;
                                break;
                            }
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            ipAssignmentString = value;
                            break;
                        case 1:
                            linkAddressString = value;
                            break;
                        case 2:
                            linkPrefixLength = value;
                            break;
                        case 3:
                            gatewayAddressString = value;
                            break;
                        case 4:
                            dnsServerAddressesString = value;
                            break;
                        case 5:
                            proxySettingsString = value;
                            break;
                        case 6:
                            proxyHost = value;
                            break;
                        case 7:
                            proxyPort = value.intValue();
                            break;
                        case 8:
                            proxyExclusionList = value;
                            break;
                        case 9:
                            proxyPacFile = value;
                            break;
                        default:
                            StringBuilder sb2 = new StringBuilder();
                            String str = tagName;
                            sb2.append("Unknown value name found: ");
                            sb2.append(valueName[0]);
                            throw new XmlPullParserException(sb2.toString());
                    }
                }
                supportedTags2 = supportedTags;
            } else {
                String str2 = tagName;
                throw new XmlPullParserException("Missing value name");
            }
        }
        XmlPullParser xmlPullParser = in;
        Set<String> set = supportedTags2;
        IpConfiguration ipConfiguration = new IpConfiguration();
        if (ipAssignmentString != null) {
            IpConfiguration.IpAssignment ipAssignment = IpConfiguration.IpAssignment.valueOf(ipAssignmentString);
            ipConfiguration.setIpAssignment(ipAssignment);
            switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$IpAssignment[ipAssignment.ordinal()]) {
                case 1:
                    StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                    if (linkAddressString == null || linkPrefixLength == null) {
                        String str3 = linkAddressString;
                    } else {
                        String str4 = ipAssignmentString;
                        LinkAddress linkAddress = new LinkAddress(NetworkUtils.numericToInetAddress(linkAddressString), linkPrefixLength.intValue());
                        if (linkAddress.getAddress() instanceof Inet4Address) {
                            staticIpConfiguration.ipAddress = linkAddress;
                            String str5 = linkAddressString;
                        } else {
                            StringBuilder sb3 = new StringBuilder();
                            String str6 = linkAddressString;
                            sb3.append("Non-IPv4 address: ");
                            sb3.append(linkAddress);
                            Log.w(TAG, sb3.toString());
                        }
                    }
                    if (gatewayAddressString != null) {
                        InetAddress gateway = NetworkUtils.numericToInetAddress(gatewayAddressString);
                        RouteInfo route = new RouteInfo(null, gateway);
                        if (route.isIPv4Default()) {
                            staticIpConfiguration.gateway = gateway;
                        } else {
                            StringBuilder sb4 = new StringBuilder();
                            InetAddress inetAddress = gateway;
                            sb4.append("Non-IPv4 default route: ");
                            sb4.append(route);
                            Log.w(TAG, sb4.toString());
                        }
                    }
                    if (dnsServerAddressesString != null) {
                        int i = 0;
                        for (int length = dnsServerAddressesString.length; i < length; length = length) {
                            staticIpConfiguration.dnsServers.add(NetworkUtils.numericToInetAddress(dnsServerAddressesString[i]));
                            i++;
                        }
                    }
                    ipConfiguration.setStaticIpConfiguration(staticIpConfiguration);
                    break;
                case 2:
                case 3:
                    String str7 = ipAssignmentString;
                    String str8 = linkAddressString;
                    break;
                default:
                    String str9 = ipAssignmentString;
                    String str10 = linkAddressString;
                    throw new XmlPullParserException("Unknown ip assignment type: " + ipAssignment);
            }
            if (proxySettingsString != null) {
                IpConfiguration.ProxySettings proxySettings = IpConfiguration.ProxySettings.valueOf(proxySettingsString);
                ipConfiguration.setProxySettings(proxySettings);
                switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$ProxySettings[proxySettings.ordinal()]) {
                    case 1:
                        if (proxyHost == null) {
                            throw new XmlPullParserException("ProxyHost was missing in IpConfiguration section");
                        } else if (proxyPort == -1) {
                            throw new XmlPullParserException("ProxyPort was missing in IpConfiguration section");
                        } else if (proxyExclusionList != null) {
                            ipConfiguration.setHttpProxy(new ProxyInfo(proxyHost, proxyPort, proxyExclusionList));
                            break;
                        } else {
                            throw new XmlPullParserException("ProxyExclusionList was missing in IpConfiguration section");
                        }
                    case 2:
                        if (proxyPacFile != null) {
                            ipConfiguration.setHttpProxy(new ProxyInfo(proxyPacFile));
                            break;
                        } else {
                            throw new XmlPullParserException("ProxyPac was missing in IpConfiguration section");
                        }
                    case 3:
                    case 4:
                        break;
                    default:
                        throw new XmlPullParserException("Unknown proxy settings type: " + proxySettings);
                }
                return ipConfiguration;
            }
            throw new XmlPullParserException("ProxySettings was missing in IpConfiguration section");
        }
        String str11 = linkAddressString;
        throw new XmlPullParserException("IpAssignment was missing in IpConfiguration section");
    }

    private static Set<String> getSupportedIpConfigurationTags(int minorVersion) {
        if (minorVersion == 0) {
            return IP_CONFIGURATION_MINOR_V0_SUPPORTED_TAGS;
        }
        Log.e(TAG, "Invalid minorVersion: " + minorVersion);
        return Collections.emptySet();
    }
}
