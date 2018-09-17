package com.android.server.wifi.util;

import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.XmlUtils;
import com.android.server.wifi.WifiBackupRestore.SupplicantBackupMigration;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class XmlUtil {
    private static final String TAG = "WifiXmlUtil";

    public static class IpConfigurationXmlUtil {
        private static final /* synthetic */ int[] -android-net-IpConfiguration$IpAssignmentSwitchesValues = null;
        private static final /* synthetic */ int[] -android-net-IpConfiguration$ProxySettingsSwitchesValues = null;
        public static final String XML_TAG_DNS_SERVER_ADDRESSES = "DNSServers";
        public static final String XML_TAG_GATEWAY_ADDRESS = "GatewayAddress";
        public static final String XML_TAG_IP_ASSIGNMENT = "IpAssignment";
        public static final String XML_TAG_LINK_ADDRESS = "LinkAddress";
        public static final String XML_TAG_LINK_PREFIX_LENGTH = "LinkPrefixLength";
        public static final String XML_TAG_PROXY_EXCLUSION_LIST = "ProxyExclusionList";
        public static final String XML_TAG_PROXY_HOST = "ProxyHost";
        public static final String XML_TAG_PROXY_PAC_FILE = "ProxyPac";
        public static final String XML_TAG_PROXY_PORT = "ProxyPort";
        public static final String XML_TAG_PROXY_SETTINGS = "ProxySettings";

        private static /* synthetic */ int[] -getandroid-net-IpConfiguration$IpAssignmentSwitchesValues() {
            if (-android-net-IpConfiguration$IpAssignmentSwitchesValues != null) {
                return -android-net-IpConfiguration$IpAssignmentSwitchesValues;
            }
            int[] iArr = new int[IpAssignment.values().length];
            try {
                iArr[IpAssignment.DHCP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[IpAssignment.STATIC.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[IpAssignment.UNASSIGNED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -android-net-IpConfiguration$IpAssignmentSwitchesValues = iArr;
            return iArr;
        }

        private static /* synthetic */ int[] -getandroid-net-IpConfiguration$ProxySettingsSwitchesValues() {
            if (-android-net-IpConfiguration$ProxySettingsSwitchesValues != null) {
                return -android-net-IpConfiguration$ProxySettingsSwitchesValues;
            }
            int[] iArr = new int[ProxySettings.values().length];
            try {
                iArr[ProxySettings.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ProxySettings.PAC.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ProxySettings.STATIC.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ProxySettings.UNASSIGNED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -android-net-IpConfiguration$ProxySettingsSwitchesValues = iArr;
            return iArr;
        }

        private static void writeStaticIpConfigurationToXml(XmlSerializer out, StaticIpConfiguration staticIpConfiguration) throws XmlPullParserException, IOException {
            if (staticIpConfiguration.ipAddress != null) {
                XmlUtil.writeNextValue(out, XML_TAG_LINK_ADDRESS, staticIpConfiguration.ipAddress.getAddress().getHostAddress());
                XmlUtil.writeNextValue(out, XML_TAG_LINK_PREFIX_LENGTH, Integer.valueOf(staticIpConfiguration.ipAddress.getPrefixLength()));
            } else {
                XmlUtil.writeNextValue(out, XML_TAG_LINK_ADDRESS, null);
                XmlUtil.writeNextValue(out, XML_TAG_LINK_PREFIX_LENGTH, null);
            }
            if (staticIpConfiguration.gateway != null) {
                XmlUtil.writeNextValue(out, XML_TAG_GATEWAY_ADDRESS, staticIpConfiguration.gateway.getHostAddress());
            } else {
                XmlUtil.writeNextValue(out, XML_TAG_GATEWAY_ADDRESS, null);
            }
            if (staticIpConfiguration.dnsServers != null) {
                String[] dnsServers = new String[staticIpConfiguration.dnsServers.size()];
                int dnsServerIdx = 0;
                for (InetAddress inetAddr : staticIpConfiguration.dnsServers) {
                    int dnsServerIdx2 = dnsServerIdx + 1;
                    dnsServers[dnsServerIdx] = inetAddr.getHostAddress();
                    dnsServerIdx = dnsServerIdx2;
                }
                XmlUtil.writeNextValue(out, XML_TAG_DNS_SERVER_ADDRESSES, dnsServers);
                return;
            }
            XmlUtil.writeNextValue(out, XML_TAG_DNS_SERVER_ADDRESSES, null);
        }

        public static void writeToXml(XmlSerializer out, IpConfiguration ipConfiguration) throws XmlPullParserException, IOException {
            XmlUtil.writeNextValue(out, XML_TAG_IP_ASSIGNMENT, ipConfiguration.ipAssignment.toString());
            switch (-getandroid-net-IpConfiguration$IpAssignmentSwitchesValues()[ipConfiguration.ipAssignment.ordinal()]) {
                case 2:
                    writeStaticIpConfigurationToXml(out, ipConfiguration.getStaticIpConfiguration());
                    break;
            }
            XmlUtil.writeNextValue(out, XML_TAG_PROXY_SETTINGS, ipConfiguration.proxySettings.toString());
            switch (-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues()[ipConfiguration.proxySettings.ordinal()]) {
                case 2:
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_PAC_FILE, ipConfiguration.httpProxy.getPacFileUrl().toString());
                    return;
                case 3:
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_HOST, ipConfiguration.httpProxy.getHost());
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_PORT, Integer.valueOf(ipConfiguration.httpProxy.getPort()));
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_EXCLUSION_LIST, ipConfiguration.httpProxy.getExclusionListAsString());
                    return;
                default:
                    return;
            }
        }

        private static StaticIpConfiguration parseStaticIpConfigurationFromXml(XmlPullParser in) throws XmlPullParserException, IOException {
            StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
            String linkAddressString = (String) XmlUtil.readNextValueWithName(in, XML_TAG_LINK_ADDRESS);
            Integer linkPrefixLength = (Integer) XmlUtil.readNextValueWithName(in, XML_TAG_LINK_PREFIX_LENGTH);
            if (!(linkAddressString == null || linkPrefixLength == null)) {
                LinkAddress linkAddress = new LinkAddress(NetworkUtils.numericToInetAddress(linkAddressString), linkPrefixLength.intValue());
                if (linkAddress.getAddress() instanceof Inet4Address) {
                    staticIpConfiguration.ipAddress = linkAddress;
                } else {
                    Log.w(XmlUtil.TAG, "Non-IPv4 address: " + linkAddress);
                }
            }
            String gatewayAddressString = (String) XmlUtil.readNextValueWithName(in, XML_TAG_GATEWAY_ADDRESS);
            if (gatewayAddressString != null) {
                InetAddress gateway = NetworkUtils.numericToInetAddress(gatewayAddressString);
                RouteInfo route = new RouteInfo(null, gateway);
                if (route.isIPv4Default()) {
                    staticIpConfiguration.gateway = gateway;
                } else {
                    Log.w(XmlUtil.TAG, "Non-IPv4 default route: " + route);
                }
            }
            String[] dnsServerAddressesString = (String[]) XmlUtil.readNextValueWithName(in, XML_TAG_DNS_SERVER_ADDRESSES);
            if (dnsServerAddressesString != null) {
                for (String dnsServerAddressString : dnsServerAddressesString) {
                    staticIpConfiguration.dnsServers.add(NetworkUtils.numericToInetAddress(dnsServerAddressString));
                }
            }
            return staticIpConfiguration;
        }

        public static IpConfiguration parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            IpConfiguration ipConfiguration = new IpConfiguration();
            IpAssignment ipAssignment = IpAssignment.valueOf((String) XmlUtil.readNextValueWithName(in, XML_TAG_IP_ASSIGNMENT));
            ipConfiguration.setIpAssignment(ipAssignment);
            switch (-getandroid-net-IpConfiguration$IpAssignmentSwitchesValues()[ipAssignment.ordinal()]) {
                case 1:
                case 3:
                    break;
                case 2:
                    ipConfiguration.setStaticIpConfiguration(parseStaticIpConfigurationFromXml(in));
                    break;
                default:
                    throw new XmlPullParserException("Unknown ip assignment type: " + ipAssignment);
            }
            ProxySettings proxySettings = ProxySettings.valueOf((String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_SETTINGS));
            ipConfiguration.setProxySettings(proxySettings);
            switch (-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues()[proxySettings.ordinal()]) {
                case 1:
                case 4:
                    break;
                case 2:
                    ipConfiguration.setHttpProxy(new ProxyInfo((String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_PAC_FILE)));
                    break;
                case 3:
                    ipConfiguration.setHttpProxy(new ProxyInfo((String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_HOST), ((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_PORT)).intValue(), (String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_EXCLUSION_LIST)));
                    break;
                default:
                    throw new XmlPullParserException("Unknown proxy settings type: " + proxySettings);
            }
            return ipConfiguration;
        }
    }

    public static class NetworkSelectionStatusXmlUtil {
        public static final String XML_TAG_CONNECT_CHOICE = "ConnectChoice";
        public static final String XML_TAG_CONNECT_CHOICE_TIMESTAMP = "ConnectChoiceTimeStamp";
        public static final String XML_TAG_DISABLE_REASON = "DisableReason";
        public static final String XML_TAG_HAS_EVER_CONNECTED = "HasEverConnected";
        public static final String XML_TAG_SELECTION_STATUS = "SelectionStatus";

        public static void writeToXml(XmlSerializer out, NetworkSelectionStatus selectionStatus) throws XmlPullParserException, IOException {
            XmlUtil.writeNextValue(out, XML_TAG_SELECTION_STATUS, selectionStatus.getNetworkStatusString());
            XmlUtil.writeNextValue(out, XML_TAG_DISABLE_REASON, selectionStatus.getNetworkDisableReasonString());
            XmlUtil.writeNextValue(out, XML_TAG_CONNECT_CHOICE, selectionStatus.getConnectChoice());
            XmlUtil.writeNextValue(out, XML_TAG_CONNECT_CHOICE_TIMESTAMP, Long.valueOf(selectionStatus.getConnectChoiceTimestamp()));
            XmlUtil.writeNextValue(out, XML_TAG_HAS_EVER_CONNECTED, Boolean.valueOf(selectionStatus.getHasEverConnected()));
        }

        public static NetworkSelectionStatus parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            NetworkSelectionStatus selectionStatus = new NetworkSelectionStatus();
            String statusString = "";
            String disableReasonString = "";
            while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                String[] valueName = new String[1];
                String value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] == null) {
                    throw new XmlPullParserException("Missing value name");
                }
                String str = valueName[0];
                if (str.equals(XML_TAG_SELECTION_STATUS)) {
                    statusString = value;
                } else if (str.equals(XML_TAG_DISABLE_REASON)) {
                    disableReasonString = value;
                } else if (str.equals(XML_TAG_CONNECT_CHOICE)) {
                    selectionStatus.setConnectChoice(value);
                } else if (str.equals(XML_TAG_CONNECT_CHOICE_TIMESTAMP)) {
                    selectionStatus.setConnectChoiceTimestamp(((Long) value).longValue());
                } else if (str.equals(XML_TAG_HAS_EVER_CONNECTED)) {
                    selectionStatus.setHasEverConnected(((Boolean) value).booleanValue());
                } else {
                    throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                }
            }
            int status = Arrays.asList(NetworkSelectionStatus.QUALITY_NETWORK_SELECTION_STATUS).indexOf(statusString);
            int disableReason = Arrays.asList(NetworkSelectionStatus.QUALITY_NETWORK_SELECTION_DISABLE_REASON).indexOf(disableReasonString);
            if (status == -1 || disableReason == -1 || status == 1) {
                status = 0;
                disableReason = 0;
            }
            selectionStatus.setNetworkSelectionStatus(status);
            selectionStatus.setNetworkSelectionDisableReason(disableReason);
            return selectionStatus;
        }
    }

    public static class WifiConfigurationXmlUtil {
        public static final String XML_TAG_ALLOWED_AUTH_ALGOS = "AllowedAuthAlgos";
        public static final String XML_TAG_ALLOWED_GROUP_CIPHERS = "AllowedGroupCiphers";
        public static final String XML_TAG_ALLOWED_KEY_MGMT = "AllowedKeyMgmt";
        public static final String XML_TAG_ALLOWED_PAIRWISE_CIPHERS = "AllowedPairwiseCiphers";
        public static final String XML_TAG_ALLOWED_PROTOCOLS = "AllowedProtocols";
        public static final String XML_TAG_BSSID = "BSSID";
        public static final String XML_TAG_CONFIG_KEY = "ConfigKey";
        public static final String XML_TAG_CREATION_TIME = "CreationTime";
        public static final String XML_TAG_CREATOR_NAME = "CreatorName";
        public static final String XML_TAG_CREATOR_UID = "CreatorUid";
        public static final String XML_TAG_DEFAULT_GW_MAC_ADDRESS = "DefaultGwMacAddress";
        public static final String XML_TAG_FQDN = "FQDN";
        public static final String XML_TAG_HIDDEN_SSID = "HiddenSSID";
        public static final String XML_TAG_INET_SELF_CURE_HISTORY = "INET_SELF_CURE_HISTORY";
        public static final String XML_TAG_INTERNET_HISTORY = "INTERNET_HISTORY";
        public static final String XML_TAG_IS_LEGACY_PASSPOINT_CONFIG = "IsLegacyPasspointConfig";
        public static final String XML_TAG_LAST_CONNECT_UID = "LastConnectUid";
        public static final String XML_TAG_LAST_DHCP_RESULTS = "LAST_DHCP_RESULTS";
        public static final String XML_TAG_LAST_HAS_INTERNET_TS = "LAST_HAS_INTERNET_TS";
        public static final String XML_TAG_LAST_TRY_SWTICH_WIFI_TS = "LAST_TRY_SWTICH_WIFI_TS";
        public static final String XML_TAG_LAST_UPDATE_NAME = "LastUpdateName";
        public static final String XML_TAG_LAST_UPDATE_UID = "LastUpdateUid";
        public static final String XML_TAG_LINKED_NETWORKS_LIST = "LinkedNetworksList";
        public static final String XML_TAG_METERED_HINT = "MeteredHint";
        public static final String XML_TAG_NO_INTERNET_ACCESS_EXPECTED = "NoInternetAccessExpected";
        public static final String XML_TAG_NUM_ASSOCIATION = "NumAssociation";
        public static final String XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS = "NumNoInternetAccessReports";
        public static final String XML_TAG_ORI_SSID = "OriSsid";
        public static final String XML_TAG_PORTAL_NETWORK = "PORTAL_NETWORK";
        public static final String XML_TAG_PRE_SHARED_KEY = "PreSharedKey";
        public static final String XML_TAG_PRIORITY = "Priority";
        public static final String XML_TAG_PROVIDER_FRIENDLY_NAME = "ProviderFriendlyName";
        public static final String XML_TAG_REQUIRE_PMF = "RequirePMF";
        public static final String XML_TAG_ROAMING_CONSORTIUM_OIS = "RoamingConsortiumOIs";
        public static final String XML_TAG_SHARED = "Shared";
        public static final String XML_TAG_SSID = "SSID";
        public static final String XML_TAG_STATUS = "Status";
        public static final String XML_TAG_USER_APPROVED = "UserApproved";
        public static final String XML_TAG_USE_EXTERNAL_SCORES = "UseExternalScores";
        public static final String XML_TAG_VALIDATED_INTERNET_ACCESS = "ValidatedInternetAccess";
        public static final String XML_TAG_WAPI_AS_CERT_PATH = "WapiAsCertPath";
        public static final String XML_TAG_WAPI_PSK_KEY_TYPE = "WapiPskKeyType";
        public static final String XML_TAG_WAPI_USER_CERT_PATH = "WapiUserCertPath";
        public static final String XML_TAG_WEP_KEYS = "WEPKeys";
        public static final String XML_TAG_WEP_TX_KEY_INDEX = "WEPTxKeyIndex";
        public static final String XML_TAG_WIFI_PRO_TEMP_CREATED = "WIFI_PRO_TEMP_CREATED";

        private static void writeWepKeysToXml(XmlSerializer out, String[] wepKeys) throws XmlPullParserException, IOException {
            String[] wepKeysToWrite = new String[wepKeys.length];
            boolean hasWepKey = false;
            for (int i = 0; i < wepKeys.length; i++) {
                if (wepKeys[i] == null) {
                    wepKeysToWrite[i] = new String();
                } else {
                    wepKeysToWrite[i] = wepKeys[i];
                    hasWepKey = true;
                }
            }
            if (hasWepKey) {
                XmlUtil.writeNextValue(out, XML_TAG_WEP_KEYS, wepKeysToWrite);
            } else {
                XmlUtil.writeNextValue(out, XML_TAG_WEP_KEYS, null);
            }
        }

        public static void writeCommonElementsToXml(XmlSerializer out, WifiConfiguration configuration) throws XmlPullParserException, IOException {
            XmlUtil.writeNextValue(out, XML_TAG_CONFIG_KEY, configuration.configKey());
            XmlUtil.writeNextValue(out, XML_TAG_SSID, configuration.SSID);
            XmlUtil.writeNextValue(out, XML_TAG_ORI_SSID, configuration.oriSsid);
            XmlUtil.writeNextValue(out, XML_TAG_BSSID, configuration.BSSID);
            XmlUtil.writeNextValue(out, XML_TAG_PRE_SHARED_KEY, configuration.preSharedKey);
            writeWepKeysToXml(out, configuration.wepKeys);
            XmlUtil.writeNextValue(out, XML_TAG_WEP_TX_KEY_INDEX, Integer.valueOf(configuration.wepTxKeyIndex));
            XmlUtil.writeNextValue(out, XML_TAG_PRIORITY, Integer.valueOf(configuration.priority));
            XmlUtil.writeNextValue(out, XML_TAG_HIDDEN_SSID, Boolean.valueOf(configuration.hiddenSSID));
            XmlUtil.writeNextValue(out, XML_TAG_REQUIRE_PMF, Boolean.valueOf(configuration.requirePMF));
            XmlUtil.writeNextValue(out, XML_TAG_ALLOWED_KEY_MGMT, configuration.allowedKeyManagement.toByteArray());
            XmlUtil.writeNextValue(out, XML_TAG_ALLOWED_PROTOCOLS, configuration.allowedProtocols.toByteArray());
            XmlUtil.writeNextValue(out, XML_TAG_ALLOWED_AUTH_ALGOS, configuration.allowedAuthAlgorithms.toByteArray());
            XmlUtil.writeNextValue(out, XML_TAG_ALLOWED_GROUP_CIPHERS, configuration.allowedGroupCiphers.toByteArray());
            XmlUtil.writeNextValue(out, XML_TAG_ALLOWED_PAIRWISE_CIPHERS, configuration.allowedPairwiseCiphers.toByteArray());
            XmlUtil.writeNextValue(out, XML_TAG_WAPI_PSK_KEY_TYPE, Integer.valueOf(configuration.wapiPskTypeBcm));
            XmlUtil.writeNextValue(out, XML_TAG_WAPI_AS_CERT_PATH, configuration.wapiAsCertBcm);
            XmlUtil.writeNextValue(out, XML_TAG_WAPI_USER_CERT_PATH, configuration.wapiUserCertBcm);
            XmlUtil.writeNextValue(out, XML_TAG_SHARED, Boolean.valueOf(configuration.shared));
        }

        public static void writeToXmlForBackup(XmlSerializer out, WifiConfiguration configuration) throws XmlPullParserException, IOException {
            writeCommonElementsToXml(out, configuration);
        }

        public static void writeToXmlForConfigStore(XmlSerializer out, WifiConfiguration configuration) throws XmlPullParserException, IOException {
            writeCommonElementsToXml(out, configuration);
            XmlUtil.writeNextValue(out, XML_TAG_STATUS, Integer.valueOf(configuration.status));
            XmlUtil.writeNextValue(out, XML_TAG_FQDN, configuration.FQDN);
            XmlUtil.writeNextValue(out, XML_TAG_PROVIDER_FRIENDLY_NAME, configuration.providerFriendlyName);
            XmlUtil.writeNextValue(out, XML_TAG_LINKED_NETWORKS_LIST, configuration.linkedConfigurations);
            XmlUtil.writeNextValue(out, XML_TAG_DEFAULT_GW_MAC_ADDRESS, configuration.defaultGwMacAddress);
            XmlUtil.writeNextValue(out, XML_TAG_VALIDATED_INTERNET_ACCESS, Boolean.valueOf(configuration.validatedInternetAccess));
            XmlUtil.writeNextValue(out, XML_TAG_NO_INTERNET_ACCESS_EXPECTED, Boolean.valueOf(configuration.noInternetAccessExpected));
            XmlUtil.writeNextValue(out, XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS, Integer.valueOf(configuration.numNoInternetAccessReports));
            XmlUtil.writeNextValue(out, XML_TAG_INTERNET_HISTORY, configuration.internetHistory);
            XmlUtil.writeNextValue(out, XML_TAG_PORTAL_NETWORK, Boolean.valueOf(configuration.portalNetwork));
            XmlUtil.writeNextValue(out, XML_TAG_LAST_HAS_INTERNET_TS, Long.valueOf(configuration.lastHasInternetTimestamp));
            XmlUtil.writeNextValue(out, XML_TAG_LAST_TRY_SWTICH_WIFI_TS, Long.valueOf(configuration.lastTrySwitchWifiTimestamp));
            XmlUtil.writeNextValue(out, XML_TAG_WIFI_PRO_TEMP_CREATED, Boolean.valueOf(configuration.isTempCreated));
            XmlUtil.writeNextValue(out, XML_TAG_LAST_DHCP_RESULTS, configuration.lastDhcpResults);
            XmlUtil.writeNextValue(out, XML_TAG_INET_SELF_CURE_HISTORY, configuration.internetSelfCureHistory);
            XmlUtil.writeNextValue(out, XML_TAG_USER_APPROVED, Integer.valueOf(configuration.userApproved));
            XmlUtil.writeNextValue(out, XML_TAG_METERED_HINT, Boolean.valueOf(configuration.meteredHint));
            XmlUtil.writeNextValue(out, XML_TAG_USE_EXTERNAL_SCORES, Boolean.valueOf(configuration.useExternalScores));
            XmlUtil.writeNextValue(out, XML_TAG_NUM_ASSOCIATION, Integer.valueOf(configuration.numAssociation));
            XmlUtil.writeNextValue(out, XML_TAG_CREATOR_UID, Integer.valueOf(configuration.creatorUid));
            XmlUtil.writeNextValue(out, XML_TAG_CREATOR_NAME, configuration.creatorName);
            XmlUtil.writeNextValue(out, XML_TAG_CREATION_TIME, configuration.creationTime);
            XmlUtil.writeNextValue(out, XML_TAG_LAST_UPDATE_UID, Integer.valueOf(configuration.lastUpdateUid));
            XmlUtil.writeNextValue(out, XML_TAG_LAST_UPDATE_NAME, configuration.lastUpdateName);
            XmlUtil.writeNextValue(out, XML_TAG_LAST_CONNECT_UID, Integer.valueOf(configuration.lastConnectUid));
            XmlUtil.writeNextValue(out, XML_TAG_IS_LEGACY_PASSPOINT_CONFIG, Boolean.valueOf(configuration.isLegacyPasspointConfig));
            XmlUtil.writeNextValue(out, XML_TAG_ROAMING_CONSORTIUM_OIS, configuration.roamingConsortiumIds);
        }

        private static void populateWepKeysFromXmlValue(Object value, String[] wepKeys) throws XmlPullParserException, IOException {
            String[] wepKeysInData = (String[]) value;
            if (wepKeysInData != null) {
                if (wepKeysInData.length != wepKeys.length) {
                    throw new XmlPullParserException("Invalid Wep Keys length: " + wepKeysInData.length);
                }
                for (int i = 0; i < wepKeys.length; i++) {
                    if (wepKeysInData[i].isEmpty()) {
                        wepKeys[i] = null;
                    } else {
                        wepKeys[i] = wepKeysInData[i];
                    }
                }
            }
        }

        public static Pair<String, WifiConfiguration> parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            WifiConfiguration configuration = new WifiConfiguration();
            Object configKeyInData = null;
            while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                String[] valueName = new String[1];
                String value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] == null) {
                    throw new XmlPullParserException("Missing value name");
                }
                String str = valueName[0];
                if (str.equals(XML_TAG_CONFIG_KEY)) {
                    configKeyInData = value;
                } else if (str.equals(XML_TAG_SSID)) {
                    configuration.SSID = value;
                } else if (str.equals(XML_TAG_BSSID)) {
                    configuration.BSSID = value;
                } else if (str.equals(XML_TAG_PRE_SHARED_KEY)) {
                    configuration.preSharedKey = value;
                } else if (str.equals(XML_TAG_WEP_KEYS)) {
                    populateWepKeysFromXmlValue(value, configuration.wepKeys);
                } else if (str.equals(XML_TAG_WEP_TX_KEY_INDEX)) {
                    configuration.wepTxKeyIndex = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_PRIORITY)) {
                    configuration.priority = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_HIDDEN_SSID)) {
                    configuration.hiddenSSID = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_REQUIRE_PMF)) {
                    configuration.requirePMF = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_ALLOWED_KEY_MGMT)) {
                    configuration.allowedKeyManagement = BitSet.valueOf((byte[]) value);
                } else if (str.equals(XML_TAG_ALLOWED_PROTOCOLS)) {
                    configuration.allowedProtocols = BitSet.valueOf((byte[]) value);
                } else if (str.equals(XML_TAG_ALLOWED_AUTH_ALGOS)) {
                    configuration.allowedAuthAlgorithms = BitSet.valueOf((byte[]) value);
                } else if (str.equals(XML_TAG_ALLOWED_GROUP_CIPHERS)) {
                    configuration.allowedGroupCiphers = BitSet.valueOf((byte[]) value);
                } else if (str.equals(XML_TAG_ALLOWED_PAIRWISE_CIPHERS)) {
                    configuration.allowedPairwiseCiphers = BitSet.valueOf((byte[]) value);
                } else if (str.equals(XML_TAG_SHARED)) {
                    configuration.shared = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_STATUS)) {
                    int status = ((Integer) value).intValue();
                    if (status == 0) {
                        status = 2;
                    }
                    configuration.status = status;
                } else if (str.equals(XML_TAG_FQDN)) {
                    configuration.FQDN = value;
                } else if (str.equals(XML_TAG_PROVIDER_FRIENDLY_NAME)) {
                    configuration.providerFriendlyName = value;
                } else if (str.equals(XML_TAG_LINKED_NETWORKS_LIST)) {
                    configuration.linkedConfigurations = (HashMap) value;
                } else if (str.equals(XML_TAG_DEFAULT_GW_MAC_ADDRESS)) {
                    configuration.defaultGwMacAddress = value;
                } else if (str.equals(XML_TAG_VALIDATED_INTERNET_ACCESS)) {
                    configuration.validatedInternetAccess = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_NO_INTERNET_ACCESS_EXPECTED)) {
                    configuration.noInternetAccessExpected = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS)) {
                    configuration.numNoInternetAccessReports = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_INTERNET_HISTORY)) {
                    configuration.internetHistory = value;
                } else if (str.equals(XML_TAG_PORTAL_NETWORK)) {
                    configuration.portalNetwork = ((Boolean) value).booleanValue();
                    configuration.noInternetAccess = !configuration.portalNetwork ? configuration.hasNoInternetAccess() : false;
                    configuration.wifiProNoInternetAccess = !configuration.noInternetAccess ? configuration.portalNetwork : true;
                    if (configuration.noInternetAccess) {
                        configuration.internetAccessType = 2;
                    }
                    if (configuration.portalNetwork) {
                        configuration.internetAccessType = 3;
                    }
                    if (!configuration.wifiProNoInternetAccess) {
                        configuration.internetAccessType = 4;
                    }
                } else if (str.equals(XML_TAG_LAST_HAS_INTERNET_TS)) {
                    configuration.lastHasInternetTimestamp = ((Long) value).longValue();
                } else if (str.equals(XML_TAG_LAST_TRY_SWTICH_WIFI_TS)) {
                    configuration.lastTrySwitchWifiTimestamp = ((Long) value).longValue();
                } else if (str.equals(XML_TAG_WIFI_PRO_TEMP_CREATED)) {
                    configuration.isTempCreated = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_LAST_DHCP_RESULTS)) {
                    configuration.lastDhcpResults = value;
                } else if (str.equals(XML_TAG_INET_SELF_CURE_HISTORY)) {
                    configuration.internetSelfCureHistory = value;
                } else if (str.equals(XML_TAG_USER_APPROVED)) {
                    configuration.userApproved = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_METERED_HINT)) {
                    configuration.meteredHint = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_USE_EXTERNAL_SCORES)) {
                    configuration.useExternalScores = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_NUM_ASSOCIATION)) {
                    configuration.numAssociation = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_CREATOR_UID)) {
                    configuration.creatorUid = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_CREATOR_NAME)) {
                    configuration.creatorName = value;
                } else if (str.equals(XML_TAG_CREATION_TIME)) {
                    configuration.creationTime = value;
                } else if (str.equals(XML_TAG_LAST_UPDATE_UID)) {
                    configuration.lastUpdateUid = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_LAST_UPDATE_NAME)) {
                    configuration.lastUpdateName = value;
                } else if (str.equals(XML_TAG_LAST_CONNECT_UID)) {
                    configuration.lastConnectUid = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_IS_LEGACY_PASSPOINT_CONFIG)) {
                    configuration.isLegacyPasspointConfig = ((Boolean) value).booleanValue();
                } else if (str.equals(XML_TAG_ROAMING_CONSORTIUM_OIS)) {
                    configuration.roamingConsortiumIds = (long[]) value;
                } else {
                    parseFromXmlEx(configuration, valueName[0], value);
                }
            }
            return Pair.create(configKeyInData, configuration);
        }

        private static void parseFromXmlEx(WifiConfiguration configuration, String name, Object value) throws XmlPullParserException {
            if (name.equals(XML_TAG_ORI_SSID)) {
                configuration.oriSsid = (String) value;
            } else if (name.equals(XML_TAG_WAPI_PSK_KEY_TYPE)) {
                configuration.wapiPskTypeBcm = ((Integer) value).intValue();
            } else if (name.equals(XML_TAG_WAPI_AS_CERT_PATH)) {
                configuration.wapiAsCertBcm = (String) value;
            } else if (name.equals(XML_TAG_WAPI_USER_CERT_PATH)) {
                configuration.wapiUserCertBcm = (String) value;
            } else {
                throw new XmlPullParserException("Unknown value name found: " + name);
            }
        }
    }

    public static class WifiEnterpriseConfigXmlUtil {
        public static final String XML_TAG_ALT_SUBJECT_MATCH = "AltSubjectMatch";
        public static final String XML_TAG_ANON_IDENTITY = "AnonIdentity";
        public static final String XML_TAG_CA_CERT = "CaCert";
        public static final String XML_TAG_CA_PATH = "CaPath";
        public static final String XML_TAG_CLIENT_CERT = "ClientCert";
        public static final String XML_TAG_DOM_SUFFIX_MATCH = "DomSuffixMatch";
        public static final String XML_TAG_EAP_METHOD = "EapMethod";
        public static final String XML_TAG_ENGINE = "Engine";
        public static final String XML_TAG_ENGINE_ID = "EngineId";
        public static final String XML_TAG_IDENTITY = "Identity";
        public static final String XML_TAG_PASSWORD = "Password";
        public static final String XML_TAG_PHASE2_METHOD = "Phase2Method";
        public static final String XML_TAG_PLMN = "PLMN";
        public static final String XML_TAG_PRIVATE_KEY_ID = "PrivateKeyId";
        public static final String XML_TAG_REALM = "Realm";
        public static final String XML_TAG_SUBJECT_MATCH = "SubjectMatch";

        public static void writeToXml(XmlSerializer out, WifiEnterpriseConfig enterpriseConfig) throws XmlPullParserException, IOException {
            XmlUtil.writeNextValue(out, XML_TAG_IDENTITY, enterpriseConfig.getFieldValue("identity"));
            XmlUtil.writeNextValue(out, XML_TAG_ANON_IDENTITY, enterpriseConfig.getFieldValue("anonymous_identity"));
            XmlUtil.writeNextValue(out, XML_TAG_PASSWORD, enterpriseConfig.getFieldValue("password"));
            XmlUtil.writeNextValue(out, XML_TAG_CLIENT_CERT, enterpriseConfig.getFieldValue(SupplicantBackupMigration.SUPPLICANT_KEY_CLIENT_CERT));
            XmlUtil.writeNextValue(out, XML_TAG_CA_CERT, enterpriseConfig.getFieldValue(SupplicantBackupMigration.SUPPLICANT_KEY_CA_CERT));
            XmlUtil.writeNextValue(out, XML_TAG_SUBJECT_MATCH, enterpriseConfig.getFieldValue("subject_match"));
            XmlUtil.writeNextValue(out, XML_TAG_ENGINE, enterpriseConfig.getFieldValue("engine"));
            XmlUtil.writeNextValue(out, XML_TAG_ENGINE_ID, enterpriseConfig.getFieldValue("engine_id"));
            XmlUtil.writeNextValue(out, XML_TAG_PRIVATE_KEY_ID, enterpriseConfig.getFieldValue("key_id"));
            XmlUtil.writeNextValue(out, XML_TAG_ALT_SUBJECT_MATCH, enterpriseConfig.getFieldValue("altsubject_match"));
            XmlUtil.writeNextValue(out, XML_TAG_DOM_SUFFIX_MATCH, enterpriseConfig.getFieldValue("domain_suffix_match"));
            XmlUtil.writeNextValue(out, XML_TAG_CA_PATH, enterpriseConfig.getFieldValue(SupplicantBackupMigration.SUPPLICANT_KEY_CA_PATH));
            XmlUtil.writeNextValue(out, XML_TAG_EAP_METHOD, Integer.valueOf(enterpriseConfig.getEapMethod()));
            XmlUtil.writeNextValue(out, XML_TAG_PHASE2_METHOD, Integer.valueOf(enterpriseConfig.getPhase2Method()));
            XmlUtil.writeNextValue(out, XML_TAG_PLMN, enterpriseConfig.getPlmn());
            XmlUtil.writeNextValue(out, XML_TAG_REALM, enterpriseConfig.getRealm());
        }

        public static WifiEnterpriseConfig parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                String[] valueName = new String[1];
                Object value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] == null) {
                    throw new XmlPullParserException("Missing value name");
                }
                String str = valueName[0];
                if (str.equals(XML_TAG_IDENTITY)) {
                    enterpriseConfig.setFieldValue("identity", (String) value);
                } else if (str.equals(XML_TAG_ANON_IDENTITY)) {
                    enterpriseConfig.setFieldValue("anonymous_identity", (String) value);
                } else if (str.equals(XML_TAG_PASSWORD)) {
                    enterpriseConfig.setFieldValue("password", (String) value);
                } else if (str.equals(XML_TAG_CLIENT_CERT)) {
                    enterpriseConfig.setFieldValue(SupplicantBackupMigration.SUPPLICANT_KEY_CLIENT_CERT, (String) value);
                } else if (str.equals(XML_TAG_CA_CERT)) {
                    enterpriseConfig.setFieldValue(SupplicantBackupMigration.SUPPLICANT_KEY_CA_CERT, (String) value);
                } else if (str.equals(XML_TAG_SUBJECT_MATCH)) {
                    enterpriseConfig.setFieldValue("subject_match", (String) value);
                } else if (str.equals(XML_TAG_ENGINE)) {
                    enterpriseConfig.setFieldValue("engine", (String) value);
                } else if (str.equals(XML_TAG_ENGINE_ID)) {
                    enterpriseConfig.setFieldValue("engine_id", (String) value);
                } else if (str.equals(XML_TAG_PRIVATE_KEY_ID)) {
                    enterpriseConfig.setFieldValue("key_id", (String) value);
                } else if (str.equals(XML_TAG_ALT_SUBJECT_MATCH)) {
                    enterpriseConfig.setFieldValue("altsubject_match", (String) value);
                } else if (str.equals(XML_TAG_DOM_SUFFIX_MATCH)) {
                    enterpriseConfig.setFieldValue("domain_suffix_match", (String) value);
                } else if (str.equals(XML_TAG_CA_PATH)) {
                    enterpriseConfig.setFieldValue(SupplicantBackupMigration.SUPPLICANT_KEY_CA_PATH, (String) value);
                } else if (str.equals(XML_TAG_EAP_METHOD)) {
                    enterpriseConfig.setEapMethod(((Integer) value).intValue());
                } else if (str.equals(XML_TAG_PHASE2_METHOD)) {
                    enterpriseConfig.setPhase2Method(((Integer) value).intValue());
                } else if (str.equals(XML_TAG_PLMN)) {
                    enterpriseConfig.setPlmn((String) value);
                } else if (str.equals(XML_TAG_REALM)) {
                    enterpriseConfig.setRealm((String) value);
                } else {
                    throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                }
            }
            return enterpriseConfig;
        }
    }

    private static void gotoStartTag(XmlPullParser in) throws XmlPullParserException, IOException {
        int type = in.getEventType();
        while (type != 2 && type != 1) {
            type = in.next();
        }
    }

    private static void gotoEndTag(XmlPullParser in) throws XmlPullParserException, IOException {
        int type = in.getEventType();
        while (type != 3 && type != 1) {
            type = in.next();
        }
    }

    public static void gotoDocumentStart(XmlPullParser in, String headerName) throws XmlPullParserException, IOException {
        XmlUtils.beginDocument(in, headerName);
    }

    public static boolean gotoNextSectionOrEnd(XmlPullParser in, String[] headerName, int outerDepth) throws XmlPullParserException, IOException {
        if (!XmlUtils.nextElementWithin(in, outerDepth)) {
            return false;
        }
        headerName[0] = in.getName();
        return true;
    }

    public static boolean gotoNextSectionWithNameOrEnd(XmlPullParser in, String expectedName, int outerDepth) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        if (!gotoNextSectionOrEnd(in, headerName, outerDepth)) {
            return false;
        }
        if (headerName[0].equals(expectedName)) {
            return true;
        }
        throw new XmlPullParserException("Next section name does not match expected name: " + expectedName);
    }

    public static void gotoNextSectionWithName(XmlPullParser in, String expectedName, int outerDepth) throws XmlPullParserException, IOException {
        if (!gotoNextSectionWithNameOrEnd(in, expectedName, outerDepth)) {
            throw new XmlPullParserException("Section not found. Expected: " + expectedName);
        }
    }

    public static boolean isNextSectionEnd(XmlPullParser in, int sectionDepth) throws XmlPullParserException, IOException {
        return XmlUtils.nextElementWithin(in, sectionDepth) ^ 1;
    }

    public static Object readCurrentValue(XmlPullParser in, String[] valueName) throws XmlPullParserException, IOException {
        Object value = XmlUtils.readValueXml(in, valueName);
        gotoEndTag(in);
        return value;
    }

    public static Object readNextValueWithName(XmlPullParser in, String expectedName) throws XmlPullParserException, IOException {
        String[] valueName = new String[1];
        XmlUtils.nextElement(in);
        Object value = readCurrentValue(in, valueName);
        if (valueName[0].equals(expectedName)) {
            return value;
        }
        throw new XmlPullParserException("Value not found. Expected: " + expectedName + ", but got: " + valueName[0]);
    }

    public static void writeDocumentStart(XmlSerializer out, String headerName) throws IOException {
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, headerName);
    }

    public static void writeDocumentEnd(XmlSerializer out, String headerName) throws IOException {
        out.endTag(null, headerName);
        out.endDocument();
    }

    public static void writeNextSectionStart(XmlSerializer out, String headerName) throws IOException {
        out.startTag(null, headerName);
    }

    public static void writeNextSectionEnd(XmlSerializer out, String headerName) throws IOException {
        out.endTag(null, headerName);
    }

    public static void writeNextValue(XmlSerializer out, String name, Object value) throws XmlPullParserException, IOException {
        XmlUtils.writeValueXml(value, name, out);
    }
}
