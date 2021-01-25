package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.hwUtil.HwWifiConfigurationXmlUtil;
import com.android.server.wifi.hwUtil.StringUtilEx;
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
    private static final int HIGHEST_SUPPORTED_MINOR_VERSION = 1;
    private static final Set<String> IP_CONFIGURATION_MINOR_V0_V1_SUPPORTED_TAGS = new HashSet(Arrays.asList(XmlUtil.IpConfigurationXmlUtil.XML_TAG_IP_ASSIGNMENT, XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_ADDRESS, XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_PREFIX_LENGTH, XmlUtil.IpConfigurationXmlUtil.XML_TAG_GATEWAY_ADDRESS, XmlUtil.IpConfigurationXmlUtil.XML_TAG_DNS_SERVER_ADDRESSES, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_SETTINGS, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_HOST, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PORT, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_EXCLUSION_LIST, XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PAC_FILE));
    private static final int RUN_WITH_SCISSORS_TIMEOUT_MILLIS = 4000;
    private static final String TAG = "WifiBackupDataV1Parser";
    private static final Set<String> WIFI_CONFIGURATION_MINOR_V0_SUPPORTED_TAGS = new HashSet(Arrays.asList(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_CONFIG_KEY, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SSID, HwWifiConfigurationXmlUtil.XML_TAG_ORI_SSID, HwWifiConfigurationXmlUtil.XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_NO_INTERNET_ACCESS_EXPECTED, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_BSSID, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_PRE_SHARED_KEY, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_KEYS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_TX_KEY_INDEX, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_HIDDEN_SSID, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_REQUIRE_PMF, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_KEY_MGMT, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PROTOCOLS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_AUTH_ALGOS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_GROUP_CIPHERS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PAIRWISE_CIPHERS, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SHARED, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_MAC_RANDOMIZATION_SETTING, XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SECURITY_COMP, HwWifiConfigurationXmlUtil.XML_TAG_INTERNET_HISTORY, HwWifiConfigurationXmlUtil.XML_TAG_PORTAL_NETWORK, HwWifiConfigurationXmlUtil.XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS, "ValidatedInternetAccess"));
    private static final Set<String> WIFI_CONFIGURATION_MINOR_V1_SUPPORTED_TAGS = new HashSet<String>() {
        /* class com.android.server.wifi.WifiBackupDataV1Parser.AnonymousClass1 */

        {
            addAll(WifiBackupDataV1Parser.WIFI_CONFIGURATION_MINOR_V0_SUPPORTED_TAGS);
            add(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_METERED_OVERRIDE);
        }
    };
    private static List<WifiConfiguration> sCurrentSavedWifiConfigurations = new ArrayList();

    WifiBackupDataV1Parser() {
    }

    @Override // com.android.server.wifi.WifiBackupDataParser
    public List<WifiConfiguration> parseNetworkConfigurationsFromXml(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        if (minorVersion > 1) {
            minorVersion = 1;
        }
        getCurrentSavedNetworks();
        XmlUtil.gotoNextSectionWithName(in, "NetworkList", outerTagDepth);
        int networkListTagDepth = outerTagDepth + 1;
        List<WifiConfiguration> configurations = new ArrayList<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, "Network", networkListTagDepth)) {
            WifiConfiguration configuration = parseNetworkConfigurationFromXml(in, minorVersion, networkListTagDepth);
            if (configuration != null) {
                Log.i(TAG, "Parsed Configuration: " + StringUtilEx.safeDisplaySsid(configuration.getPrintableSsid()));
                configurations.add(configuration);
            }
        }
        sCurrentSavedWifiConfigurations.clear();
        return configurations;
    }

    private WifiConfiguration parseNetworkConfigurationFromXml(XmlPullParser in, int minorVersion, int outerTagDepth) throws XmlPullParserException, IOException {
        int networkTagDepth = outerTagDepth + 1;
        XmlUtil.gotoNextSectionWithName(in, "WifiConfiguration", networkTagDepth);
        int configTagDepth = networkTagDepth + 1;
        WifiConfiguration configuration = parseWifiConfigurationFromXml(in, configTagDepth, minorVersion);
        if (configuration == null) {
            return null;
        }
        XmlUtil.gotoNextSectionWithName(in, "IpConfiguration", networkTagDepth);
        configuration.setIpConfiguration(parseIpConfigurationFromXml(in, configTagDepth, minorVersion));
        return configuration;
    }

    private WifiConfiguration parseWifiConfigurationFromXml(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        Pair<String, WifiConfiguration> parsedConfig = parseWifiConfigurationFromXmlInternal(in, outerTagDepth, minorVersion);
        if (parsedConfig == null || parsedConfig.first == null || parsedConfig.second == null) {
            return null;
        }
        String configKeyParsed = (String) parsedConfig.first;
        WifiConfiguration configuration = (WifiConfiguration) parsedConfig.second;
        String configKeyCalculated = configuration.configKey();
        if (!configKeyParsed.equals(configKeyCalculated)) {
            Log.w(TAG, "Configuration key does not match. Retrieved: " + StringUtilEx.safeDisplaySsid(configKeyParsed) + ", Calculated: " + StringUtilEx.safeDisplaySsid(configKeyCalculated));
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00f2, code lost:
        if (r8.equals(com.android.server.wifi.util.XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SSID) != false) goto L_0x0158;
     */
    private static Pair<String, WifiConfiguration> parseWifiConfigurationFromXmlInternal(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        WifiConfiguration configuration = new WifiConfiguration();
        String configKeyInData = null;
        Set<String> supportedTags = getSupportedWifiConfigurationTags(minorVersion);
        boolean hasMacRandomizationSetting = false;
        while (true) {
            char c = 1;
            if (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
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
                            c = 65535;
                            break;
                        case -1704616680:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_KEY_MGMT)) {
                                c = 11;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1310532293:
                            if (tagName.equals(HwWifiConfigurationXmlUtil.XML_TAG_PORTAL_NETWORK)) {
                                c = 21;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1268502125:
                            if (tagName.equals("ValidatedInternetAccess")) {
                                c = 23;
                                break;
                            }
                            c = 65535;
                            break;
                        case -711148630:
                            if (tagName.equals(HwWifiConfigurationXmlUtil.XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN)) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case -244338402:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_NO_INTERNET_ACCESS_EXPECTED)) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case -181205965:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PROTOCOLS)) {
                                c = '\f';
                                break;
                            }
                            c = 65535;
                            break;
                        case -94981986:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_MAC_RANDOMIZATION_SETTING)) {
                                c = 18;
                                break;
                            }
                            c = 65535;
                            break;
                        case -51197516:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_METERED_OVERRIDE)) {
                                c = 17;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2554747:
                            break;
                        case 63507133:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_BSSID)) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 461626209:
                            if (tagName.equals(HwWifiConfigurationXmlUtil.XML_TAG_ORI_SSID)) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case 538467443:
                            if (tagName.equals(HwWifiConfigurationXmlUtil.XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS)) {
                                c = 22;
                                break;
                            }
                            c = 65535;
                            break;
                        case 682791106:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_PAIRWISE_CIPHERS)) {
                                c = 15;
                                break;
                            }
                            c = 65535;
                            break;
                        case 736944625:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_GROUP_CIPHERS)) {
                                c = 14;
                                break;
                            }
                            c = 65535;
                            break;
                        case 797043831:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_PRE_SHARED_KEY)) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 972050063:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SECURITY_COMP)) {
                                c = 19;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1199498141:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_CONFIG_KEY)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1526332790:
                            if (tagName.equals(HwWifiConfigurationXmlUtil.XML_TAG_INTERNET_HISTORY)) {
                                c = 20;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1851050768:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_ALLOWED_AUTH_ALGOS)) {
                                c = '\r';
                                break;
                            }
                            c = 65535;
                            break;
                        case 1905126713:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_TX_KEY_INDEX)) {
                                c = '\b';
                                break;
                            }
                            c = 65535;
                            break;
                        case 1955037270:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_WEP_KEYS)) {
                                c = 7;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1965854789:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_HIDDEN_SSID)) {
                                c = '\t';
                                break;
                            }
                            c = 65535;
                            break;
                        case 2143705732:
                            if (tagName.equals(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_REQUIRE_PMF)) {
                                c = '\n';
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
                            configKeyInData = (String) value;
                            continue;
                        case 1:
                            configuration.SSID = (String) value;
                            continue;
                        case 2:
                            configuration.oriSsid = (String) value;
                            continue;
                        case 3:
                            configuration.connectToCellularAndWLAN = ((Integer) value).intValue();
                            continue;
                        case 4:
                            configuration.noInternetAccessExpected = ((Boolean) value).booleanValue();
                            continue;
                        case 5:
                            configuration.BSSID = (String) value;
                            continue;
                        case 6:
                            configuration.preSharedKey = (String) value;
                            continue;
                        case 7:
                            populateWepKeysFromXmlValue(value, configuration.wepKeys);
                            continue;
                        case '\b':
                            configuration.wepTxKeyIndex = ((Integer) value).intValue();
                            continue;
                        case '\t':
                            configuration.hiddenSSID = ((Boolean) value).booleanValue();
                            continue;
                        case '\n':
                            configuration.requirePMF = ((Boolean) value).booleanValue();
                            continue;
                        case 11:
                            configuration.allowedKeyManagement = BitSet.valueOf((byte[]) value);
                            continue;
                        case '\f':
                            configuration.allowedProtocols = BitSet.valueOf((byte[]) value);
                            continue;
                        case '\r':
                            configuration.allowedAuthAlgorithms = BitSet.valueOf((byte[]) value);
                            continue;
                        case 14:
                            configuration.allowedGroupCiphers = BitSet.valueOf((byte[]) value);
                            continue;
                        case 15:
                            configuration.allowedPairwiseCiphers = BitSet.valueOf((byte[]) value);
                            continue;
                        case 16:
                            configuration.shared = ((Boolean) value).booleanValue();
                            continue;
                        case 17:
                            configuration.meteredOverride = ((Integer) value).intValue();
                            continue;
                        case 18:
                            configuration.macRandomizationSetting = ((Integer) value).intValue();
                            hasMacRandomizationSetting = true;
                            continue;
                        case 19:
                            configuration.setCombinationType(((Boolean) value).booleanValue());
                            continue;
                        case 20:
                            configuration.internetHistory = (String) value;
                            continue;
                        case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /* 21 */:
                            configuration.portalNetwork = ((Boolean) value).booleanValue();
                            continue;
                        case 22:
                            configuration.numNoInternetAccessReports = ((Integer) value).intValue();
                            continue;
                        case 23:
                            configuration.validatedInternetAccess = ((Boolean) value).booleanValue();
                            continue;
                        default:
                            throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                    }
                }
            } else {
                if (!hasMacRandomizationSetting) {
                    restoreRandomMacSetting(configuration);
                }
                configuration.mIsCreatedByClone = true;
                clearAnyKnownIssuesInParsedConfiguration(configuration);
                return Pair.create(configKeyInData, configuration);
            }
        }
    }

    private static void getCurrentSavedNetworks() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (sCurrentSavedWifiConfigurations == null || wifiInjector == null) {
            Log.e(TAG, "wifiInjector is null");
        } else if (!wifiInjector.getClientModeImplHandler().runWithScissors(new Runnable() {
            /* class com.android.server.wifi.$$Lambda$WifiBackupDataV1Parser$No2NQNJU2oEprE3iRCYlEJwRw */

            @Override // java.lang.Runnable
            public final void run() {
                WifiBackupDataV1Parser.lambda$getCurrentSavedNetworks$0(WifiInjector.this);
            }
        }, 4000)) {
            Log.e(TAG, "getCurrentSavedNetworks failed");
        }
    }

    static /* synthetic */ void lambda$getCurrentSavedNetworks$0(WifiInjector wifiInjector) {
        List<WifiConfiguration> configNetworks = wifiInjector.getWifiConfigManager().getSavedNetworks(Binder.getCallingUid());
        if (configNetworks != null) {
            sCurrentSavedWifiConfigurations.addAll(configNetworks);
        }
    }

    private static void restoreRandomMacSetting(WifiConfiguration wifiConfiguration) {
        List<WifiConfiguration> list = sCurrentSavedWifiConfigurations;
        if (list == null || wifiConfiguration == null) {
            Log.e(TAG, "wifiConfiguration is null");
            return;
        }
        for (WifiConfiguration savedConfig : list) {
            if (savedConfig != null && wifiConfiguration.configKey().equals(savedConfig.configKey())) {
                wifiConfiguration.macRandomizationSetting = savedConfig.macRandomizationSetting;
                return;
            }
        }
    }

    private static Set<String> getSupportedWifiConfigurationTags(int minorVersion) {
        if (minorVersion == 0) {
            return WIFI_CONFIGURATION_MINOR_V0_SUPPORTED_TAGS;
        }
        if (minorVersion == 1) {
            return WIFI_CONFIGURATION_MINOR_V1_SUPPORTED_TAGS;
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ad, code lost:
        if (r13.equals(com.android.server.wifi.util.XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_ADDRESS) != false) goto L_0x00bb;
     */
    private static IpConfiguration parseIpConfigurationFromXml(XmlPullParser in, int outerTagDepth, int minorVersion) throws XmlPullParserException, IOException {
        Set<String> supportedTags = getSupportedIpConfigurationTags(minorVersion);
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
        while (true) {
            char c = 1;
            if (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                String[] valueName = new String[1];
                Object value = XmlUtil.readCurrentValue(in, valueName);
                String tagName = valueName[0];
                if (tagName == null) {
                    throw new XmlPullParserException("Missing value name");
                } else if (!supportedTags.contains(tagName)) {
                    Log.w(TAG, "Unsupported tag + \"" + tagName + "\" found in <IpConfiguration> section, ignoring.");
                    supportedTags = supportedTags;
                } else {
                    switch (tagName.hashCode()) {
                        case -1747338169:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_DNS_SERVER_ADDRESSES)) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case -1520820614:
                            break;
                        case -1464842926:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_LINK_PREFIX_LENGTH)) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case -920546460:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PAC_FILE)) {
                                c = '\t';
                                break;
                            }
                            c = 65535;
                            break;
                        case 162774900:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_IP_ASSIGNMENT)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 858907952:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_GATEWAY_ADDRESS)) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1527606550:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_HOST)) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1527844847:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_PORT)) {
                                c = 7;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1940148190:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_EXCLUSION_LIST)) {
                                c = '\b';
                                break;
                            }
                            c = 65535;
                            break;
                        case 1968819345:
                            if (tagName.equals(XmlUtil.IpConfigurationXmlUtil.XML_TAG_PROXY_SETTINGS)) {
                                c = 5;
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
                            ipAssignmentString = (String) value;
                            break;
                        case 1:
                            linkAddressString = (String) value;
                            break;
                        case 2:
                            linkPrefixLength = (Integer) value;
                            break;
                        case 3:
                            gatewayAddressString = (String) value;
                            break;
                        case 4:
                            dnsServerAddressesString = (String[]) value;
                            break;
                        case 5:
                            proxySettingsString = (String) value;
                            break;
                        case 6:
                            proxyHost = (String) value;
                            break;
                        case 7:
                            proxyPort = ((Integer) value).intValue();
                            break;
                        case '\b':
                            proxyExclusionList = (String) value;
                            break;
                        case '\t':
                            proxyPacFile = (String) value;
                            break;
                        default:
                            throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                    }
                    supportedTags = supportedTags;
                }
            } else {
                IpConfiguration ipConfiguration = new IpConfiguration();
                if (ipAssignmentString != null) {
                    IpConfiguration.IpAssignment ipAssignment = IpConfiguration.IpAssignment.valueOf(ipAssignmentString);
                    ipConfiguration.setIpAssignment(ipAssignment);
                    int i = AnonymousClass2.$SwitchMap$android$net$IpConfiguration$IpAssignment[ipAssignment.ordinal()];
                    if (i == 1) {
                        StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                        if (linkAddressString != null && linkPrefixLength != null) {
                            LinkAddress linkAddress = new LinkAddress(NetworkUtils.numericToInetAddress(linkAddressString), linkPrefixLength.intValue());
                            if (linkAddress.getAddress() instanceof Inet4Address) {
                                staticIpConfiguration.ipAddress = linkAddress;
                            } else {
                                Log.w(TAG, "Non-IPv4 address");
                            }
                        }
                        if (gatewayAddressString != null) {
                            InetAddress gateway = NetworkUtils.numericToInetAddress(gatewayAddressString);
                            RouteInfo route = new RouteInfo(null, gateway);
                            if (route.isIPv4Default()) {
                                staticIpConfiguration.gateway = gateway;
                            } else {
                                Log.w(TAG, "Non-IPv4 default route: " + route);
                            }
                        }
                        if (dnsServerAddressesString != null) {
                            int i2 = 0;
                            for (int length = dnsServerAddressesString.length; i2 < length; length = length) {
                                staticIpConfiguration.dnsServers.add(NetworkUtils.numericToInetAddress(dnsServerAddressesString[i2]));
                                i2++;
                            }
                        }
                        ipConfiguration.setStaticIpConfiguration(staticIpConfiguration);
                    } else if (i != 2 && i != 3) {
                        throw new XmlPullParserException("Unknown ip assignment type: " + ipAssignment);
                    }
                    if (proxySettingsString != null) {
                        IpConfiguration.ProxySettings proxySettings = IpConfiguration.ProxySettings.valueOf(proxySettingsString);
                        ipConfiguration.setProxySettings(proxySettings);
                        int i3 = AnonymousClass2.$SwitchMap$android$net$IpConfiguration$ProxySettings[proxySettings.ordinal()];
                        if (i3 != 1) {
                            if (i3 != 2) {
                                if (!(i3 == 3 || i3 == 4)) {
                                    throw new XmlPullParserException("Unknown proxy settings type: " + proxySettings);
                                }
                            } else if (proxyPacFile != null) {
                                ipConfiguration.setHttpProxy(new ProxyInfo(proxyPacFile));
                            } else {
                                throw new XmlPullParserException("ProxyPac was missing in IpConfiguration section");
                            }
                        } else if (proxyHost == null) {
                            throw new XmlPullParserException("ProxyHost was missing in IpConfiguration section");
                        } else if (proxyPort == -1) {
                            throw new XmlPullParserException("ProxyPort was missing in IpConfiguration section");
                        } else if (proxyExclusionList != null) {
                            ipConfiguration.setHttpProxy(new ProxyInfo(proxyHost, proxyPort, proxyExclusionList));
                        } else {
                            throw new XmlPullParserException("ProxyExclusionList was missing in IpConfiguration section");
                        }
                        return ipConfiguration;
                    }
                    throw new XmlPullParserException("ProxySettings was missing in IpConfiguration section");
                }
                throw new XmlPullParserException("IpAssignment was missing in IpConfiguration section");
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.wifi.WifiBackupDataV1Parser$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
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

    private static Set<String> getSupportedIpConfigurationTags(int minorVersion) {
        if (minorVersion == 0 || minorVersion == 1) {
            return IP_CONFIGURATION_MINOR_V0_V1_SUPPORTED_TAGS;
        }
        Log.e(TAG, "Invalid minorVersion: " + minorVersion);
        return Collections.emptySet();
    }
}
