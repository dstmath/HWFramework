package com.android.server.wifi.util;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.MacAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.XmlUtils;
import com.android.server.wifi.WifiBackupRestore;
import com.android.server.wifi.WifiLog;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class XmlUtil {
    private static final String TAG = "WifiXmlUtil";

    /* renamed from: com.android.server.wifi.util.XmlUtil$1  reason: invalid class name */
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

    public static class IpConfigurationXmlUtil {
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
                Iterator it = staticIpConfiguration.dnsServers.iterator();
                while (it.hasNext()) {
                    dnsServers[dnsServerIdx] = ((InetAddress) it.next()).getHostAddress();
                    dnsServerIdx++;
                }
                XmlUtil.writeNextValue(out, XML_TAG_DNS_SERVER_ADDRESSES, dnsServers);
                return;
            }
            XmlUtil.writeNextValue(out, XML_TAG_DNS_SERVER_ADDRESSES, null);
        }

        public static void writeToXml(XmlSerializer out, IpConfiguration ipConfiguration) throws XmlPullParserException, IOException {
            XmlUtil.writeNextValue(out, XML_TAG_IP_ASSIGNMENT, ipConfiguration.ipAssignment.toString());
            if (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$IpAssignment[ipConfiguration.ipAssignment.ordinal()] == 1) {
                writeStaticIpConfigurationToXml(out, ipConfiguration.getStaticIpConfiguration());
            }
            XmlUtil.writeNextValue(out, XML_TAG_PROXY_SETTINGS, ipConfiguration.proxySettings.toString());
            switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$ProxySettings[ipConfiguration.proxySettings.ordinal()]) {
                case 1:
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_HOST, ipConfiguration.httpProxy.getHost());
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_PORT, Integer.valueOf(ipConfiguration.httpProxy.getPort()));
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_EXCLUSION_LIST, ipConfiguration.httpProxy.getExclusionListAsString());
                    return;
                case 2:
                    XmlUtil.writeNextValue(out, XML_TAG_PROXY_PAC_FILE, ipConfiguration.httpProxy.getPacFileUrl().toString());
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
                if (new RouteInfo(null, gateway).isIPv4Default()) {
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
            IpConfiguration.IpAssignment ipAssignment = IpConfiguration.IpAssignment.valueOf((String) XmlUtil.readNextValueWithName(in, XML_TAG_IP_ASSIGNMENT));
            ipConfiguration.setIpAssignment(ipAssignment);
            switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$IpAssignment[ipAssignment.ordinal()]) {
                case 1:
                    ipConfiguration.setStaticIpConfiguration(parseStaticIpConfigurationFromXml(in));
                    break;
                case 2:
                case 3:
                    break;
                default:
                    throw new XmlPullParserException("Unknown ip assignment type: " + ipAssignment);
            }
            IpConfiguration.ProxySettings proxySettings = IpConfiguration.ProxySettings.valueOf((String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_SETTINGS));
            ipConfiguration.setProxySettings(proxySettings);
            switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$ProxySettings[proxySettings.ordinal()]) {
                case 1:
                    ipConfiguration.setHttpProxy(new ProxyInfo((String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_HOST), ((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_PORT)).intValue(), (String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_EXCLUSION_LIST)));
                    break;
                case 2:
                    ipConfiguration.setHttpProxy(new ProxyInfo((String) XmlUtil.readNextValueWithName(in, XML_TAG_PROXY_PAC_FILE)));
                    break;
                case 3:
                case 4:
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

        public static void writeToXml(XmlSerializer out, WifiConfiguration.NetworkSelectionStatus selectionStatus) throws XmlPullParserException, IOException {
            XmlUtil.writeNextValue(out, XML_TAG_SELECTION_STATUS, selectionStatus.getNetworkStatusString());
            XmlUtil.writeNextValue(out, XML_TAG_DISABLE_REASON, selectionStatus.getNetworkDisableReasonString());
            XmlUtil.writeNextValue(out, XML_TAG_CONNECT_CHOICE, selectionStatus.getConnectChoice());
            XmlUtil.writeNextValue(out, XML_TAG_CONNECT_CHOICE_TIMESTAMP, Long.valueOf(selectionStatus.getConnectChoiceTimestamp()));
            XmlUtil.writeNextValue(out, XML_TAG_HAS_EVER_CONNECTED, Boolean.valueOf(selectionStatus.getHasEverConnected()));
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v5, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: java.lang.Long} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v9, resolved type: java.lang.Boolean} */
        /* JADX WARNING: Multi-variable type inference failed */
        public static WifiConfiguration.NetworkSelectionStatus parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            WifiConfiguration.NetworkSelectionStatus selectionStatus = new WifiConfiguration.NetworkSelectionStatus();
            String statusString = "";
            String disableReasonString = "";
            while (true) {
                char c = 65535;
                if (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                    String[] valueName = new String[1];
                    Object value = XmlUtil.readCurrentValue(in, valueName);
                    if (valueName[0] != null) {
                        String str = valueName[0];
                        switch (str.hashCode()) {
                            case -1529270479:
                                if (str.equals(XML_TAG_HAS_EVER_CONNECTED)) {
                                    c = 4;
                                    break;
                                }
                                break;
                            case -822052309:
                                if (str.equals(XML_TAG_CONNECT_CHOICE_TIMESTAMP)) {
                                    c = 3;
                                    break;
                                }
                                break;
                            case -808576245:
                                if (str.equals(XML_TAG_CONNECT_CHOICE)) {
                                    c = 2;
                                    break;
                                }
                                break;
                            case -85195988:
                                if (str.equals(XML_TAG_DISABLE_REASON)) {
                                    c = 1;
                                    break;
                                }
                                break;
                            case 1452117118:
                                if (str.equals(XML_TAG_SELECTION_STATUS)) {
                                    c = 0;
                                    break;
                                }
                                break;
                        }
                        switch (c) {
                            case 0:
                                statusString = value;
                                break;
                            case 1:
                                disableReasonString = value;
                                break;
                            case 2:
                                selectionStatus.setConnectChoice(value);
                                break;
                            case 3:
                                selectionStatus.setConnectChoiceTimestamp(value.longValue());
                                break;
                            case 4:
                                selectionStatus.setHasEverConnected(value.booleanValue());
                                break;
                            default:
                                throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                        }
                    } else {
                        throw new XmlPullParserException("Missing value name");
                    }
                } else {
                    int status = Arrays.asList(WifiConfiguration.NetworkSelectionStatus.QUALITY_NETWORK_SELECTION_STATUS).indexOf(statusString);
                    int disableReason = Arrays.asList(WifiConfiguration.NetworkSelectionStatus.QUALITY_NETWORK_SELECTION_DISABLE_REASON).indexOf(disableReasonString);
                    if (status == -1 || disableReason == -1 || status == 1) {
                        status = 0;
                        disableReason = 0;
                    }
                    selectionStatus.setNetworkSelectionStatus(status);
                    selectionStatus.setNetworkSelectionDisableReason(disableReason);
                    return selectionStatus;
                }
            }
        }
    }

    public static class WifiConfigurationXmlUtil {
        public static final String XML_TAG_ACCESS_POINT_TYPE = "WifiApType";
        public static final String XML_TAG_ALLOWED_AUTH_ALGOS = "AllowedAuthAlgos";
        public static final String XML_TAG_ALLOWED_GROUP_CIPHERS = "AllowedGroupCiphers";
        public static final String XML_TAG_ALLOWED_KEY_MGMT = "AllowedKeyMgmt";
        public static final String XML_TAG_ALLOWED_PAIRWISE_CIPHERS = "AllowedPairwiseCiphers";
        public static final String XML_TAG_ALLOWED_PROTOCOLS = "AllowedProtocols";
        public static final String XML_TAG_BSSID = "BSSID";
        public static final String XML_TAG_CONFIG_KEY = "ConfigKey";
        public static final String XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN = "ConnectToCellularAndWLAN";
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
        public static final String XML_TAG_METERED_OVERRIDE = "MeteredOverride";
        public static final String XML_TAG_NO_INTERNET_ACCESS_EXPECTED = "NoInternetAccessExpected";
        public static final String XML_TAG_NUM_ASSOCIATION = "NumAssociation";
        public static final String XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS = "NumNoInternetAccessReports";
        public static final String XML_TAG_ORI_SSID = "OriSsid";
        public static final String XML_TAG_PORTAL_AUTH_TS = "PORTAL_AUTH_TS";
        public static final String XML_TAG_PORTAL_CONNECT = "PORTAL_CONNECT";
        public static final String XML_TAG_PORTAL_NETWORK = "PORTAL_NETWORK";
        public static final String XML_TAG_PORTAL_VALIDITY_DURATION = "PORTAL_VALIDITY_DURATION";
        public static final String XML_TAG_PRE_SHARED_KEY = "PreSharedKey";
        public static final String XML_TAG_PRIORITY = "Priority";
        public static final String XML_TAG_PROVIDER_FRIENDLY_NAME = "ProviderFriendlyName";
        public static final String XML_TAG_RANDOMIZED_MAC_ADDRESS = "RandomizedMacAddress";
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
            int i = 0;
            if (wepKeys.length > 0) {
                hasWepKey = wepKeys[0] != null;
            }
            while (true) {
                int i2 = i;
                if (i2 >= wepKeys.length) {
                    break;
                }
                if (wepKeys[i2] == null) {
                    wepKeysToWrite[i2] = new String();
                } else {
                    wepKeysToWrite[i2] = wepKeys[i2];
                }
                i = i2 + 1;
            }
            if (hasWepKey) {
                XmlUtil.writeNextValue(out, XML_TAG_WEP_KEYS, wepKeysToWrite);
                return;
            }
            XmlUtil.writeNextValue(out, XML_TAG_WEP_KEYS, null);
            Log.w(XmlUtil.TAG, "writeWepKeysToXml: the first element of source is null");
        }

        public static void writeCommonElementsToXml(XmlSerializer out, WifiConfiguration configuration) throws XmlPullParserException, IOException {
            XmlUtil.writeNextValue(out, XML_TAG_CONFIG_KEY, configuration.configKey());
            XmlUtil.writeNextValue(out, XML_TAG_SSID, configuration.SSID);
            XmlUtil.writeNextValue(out, XML_TAG_ORI_SSID, configuration.oriSsid);
            XmlUtil.writeNextValue(out, XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN, Integer.valueOf(configuration.connectToCellularAndWLAN));
            XmlUtil.writeNextValue(out, XML_TAG_NO_INTERNET_ACCESS_EXPECTED, Boolean.valueOf(configuration.noInternetAccessExpected));
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
            XmlUtil.writeNextValue(out, XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN, Integer.valueOf(configuration.connectToCellularAndWLAN));
            XmlUtil.writeNextValue(out, XML_TAG_ACCESS_POINT_TYPE, Integer.valueOf(configuration.wifiApType));
            XmlUtil.writeNextValue(out, XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS, Integer.valueOf(configuration.numNoInternetAccessReports));
            XmlUtil.writeNextValue(out, XML_TAG_INTERNET_HISTORY, configuration.internetHistory);
            XmlUtil.writeNextValue(out, XML_TAG_PORTAL_NETWORK, Boolean.valueOf(configuration.portalNetwork));
            XmlUtil.writeNextValue(out, XML_TAG_LAST_HAS_INTERNET_TS, Long.valueOf(configuration.lastHasInternetTimestamp));
            XmlUtil.writeNextValue(out, XML_TAG_LAST_TRY_SWTICH_WIFI_TS, Long.valueOf(configuration.lastTrySwitchWifiTimestamp));
            XmlUtil.writeNextValue(out, XML_TAG_WIFI_PRO_TEMP_CREATED, Boolean.valueOf(configuration.isTempCreated));
            XmlUtil.writeNextValue(out, XML_TAG_LAST_DHCP_RESULTS, configuration.lastDhcpResults);
            XmlUtil.writeNextValue(out, XML_TAG_INET_SELF_CURE_HISTORY, configuration.internetSelfCureHistory);
            XmlUtil.writeNextValue(out, XML_TAG_PORTAL_CONNECT, Boolean.valueOf(configuration.isPortalConnect));
            XmlUtil.writeNextValue(out, XML_TAG_PORTAL_AUTH_TS, Long.valueOf(configuration.portalAuthTimestamp));
            XmlUtil.writeNextValue(out, XML_TAG_PORTAL_VALIDITY_DURATION, Long.valueOf(configuration.portalValidityDuration));
            XmlUtil.writeNextValue(out, XML_TAG_USER_APPROVED, Integer.valueOf(configuration.userApproved));
            XmlUtil.writeNextValue(out, XML_TAG_METERED_HINT, Boolean.valueOf(configuration.meteredHint));
            XmlUtil.writeNextValue(out, XML_TAG_METERED_OVERRIDE, Integer.valueOf(configuration.meteredOverride));
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
            XmlUtil.writeNextValue(out, XML_TAG_RANDOMIZED_MAC_ADDRESS, configuration.getRandomizedMacAddress().toString());
        }

        private static void populateWepKeysFromXmlValue(Object value, String[] wepKeys) throws XmlPullParserException, IOException {
            String[] wepKeysInData = (String[]) value;
            if (wepKeysInData != null) {
                if (wepKeysInData.length == wepKeys.length) {
                    for (int i = 0; i < wepKeys.length; i++) {
                        wepKeys[i] = wepKeysInData[i];
                    }
                    return;
                }
                throw new XmlPullParserException("Invalid Wep Keys length: " + wepKeysInData.length);
            }
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        public static Pair<String, WifiConfiguration> parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            char c;
            WifiConfiguration configuration = new WifiConfiguration();
            String configKeyInData = null;
            while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                boolean z = true;
                String[] valueName = new String[1];
                Object value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] != null) {
                    String str = valueName[0];
                    switch (str.hashCode()) {
                        case -2072453770:
                            if (str.equals(XML_TAG_DEFAULT_GW_MAC_ADDRESS)) {
                                c = 21;
                                break;
                            }
                        case -1926002814:
                            if (str.equals(XML_TAG_INET_SELF_CURE_HISTORY)) {
                                c = 31;
                                break;
                            }
                        case -1819699067:
                            if (str.equals(XML_TAG_SHARED)) {
                                c = 16;
                                break;
                            }
                        case -1808614382:
                            if (str.equals(XML_TAG_STATUS)) {
                                c = 17;
                                break;
                            }
                        case -1793081233:
                            if (str.equals(XML_TAG_METERED_HINT)) {
                                c = '$';
                                break;
                            }
                        case -1704616680:
                            if (str.equals(XML_TAG_ALLOWED_KEY_MGMT)) {
                                c = 11;
                                break;
                            }
                        case -1663465224:
                            if (str.equals(XML_TAG_RANDOMIZED_MAC_ADDRESS)) {
                                c = '0';
                                break;
                            }
                        case -1597426155:
                            if (str.equals(XML_TAG_LAST_TRY_SWTICH_WIFI_TS)) {
                                c = 28;
                                break;
                            }
                        case -1568560548:
                            if (str.equals(XML_TAG_LAST_CONNECT_UID)) {
                                c = '-';
                                break;
                            }
                        case -1310532293:
                            if (str.equals(XML_TAG_PORTAL_NETWORK)) {
                                c = 26;
                                break;
                            }
                        case -1268502125:
                            if (str.equals(XML_TAG_VALIDATED_INTERNET_ACCESS)) {
                                c = 22;
                                break;
                            }
                        case -1217370545:
                            if (str.equals(XML_TAG_LAST_HAS_INTERNET_TS)) {
                                c = 27;
                                break;
                            }
                        case -1100816956:
                            if (str.equals(XML_TAG_PRIORITY)) {
                                c = 8;
                                break;
                            }
                        case -1089007030:
                            if (str.equals(XML_TAG_LAST_UPDATE_NAME)) {
                                c = ',';
                                break;
                            }
                        case -922179420:
                            if (str.equals(XML_TAG_CREATOR_UID)) {
                                c = '(';
                                break;
                            }
                        case -711148630:
                            if (str.equals(XML_TAG_CONNECT_TO_CELLULAR_AND_WLAN)) {
                                c = 6;
                                break;
                            }
                        case -346924001:
                            if (str.equals(XML_TAG_ROAMING_CONSORTIUM_OIS)) {
                                c = '/';
                                break;
                            }
                        case -244338402:
                            if (str.equals(XML_TAG_NO_INTERNET_ACCESS_EXPECTED)) {
                                c = 23;
                                break;
                            }
                        case -181205965:
                            if (str.equals(XML_TAG_ALLOWED_PROTOCOLS)) {
                                c = 12;
                                break;
                            }
                        case -135994866:
                            if (str.equals(XML_TAG_IS_LEGACY_PASSPOINT_CONFIG)) {
                                c = '.';
                                break;
                            }
                        case -51197516:
                            if (str.equals(XML_TAG_METERED_OVERRIDE)) {
                                c = WifiLog.PLACEHOLDER;
                                break;
                            }
                        case 2165397:
                            if (str.equals(XML_TAG_FQDN)) {
                                c = 18;
                                break;
                            }
                        case 2554747:
                            if (str.equals(XML_TAG_SSID)) {
                                c = 1;
                                break;
                            }
                        case 63507133:
                            if (str.equals(XML_TAG_BSSID)) {
                                c = 2;
                                break;
                            }
                        case 494456739:
                            if (str.equals(XML_TAG_PORTAL_AUTH_TS)) {
                                c = '!';
                                break;
                            }
                        case 538467443:
                            if (str.equals(XML_TAG_NUM_NO_INTERNET_ACCESS_REPORTS)) {
                                c = 24;
                                break;
                            }
                        case 581636034:
                            if (str.equals(XML_TAG_USER_APPROVED)) {
                                c = '#';
                                break;
                            }
                        case 585379985:
                            if (str.equals(XML_TAG_LAST_DHCP_RESULTS)) {
                                c = 30;
                                break;
                            }
                        case 682791106:
                            if (str.equals(XML_TAG_ALLOWED_PAIRWISE_CIPHERS)) {
                                c = 15;
                                break;
                            }
                        case 736944625:
                            if (str.equals(XML_TAG_ALLOWED_GROUP_CIPHERS)) {
                                c = 14;
                                break;
                            }
                        case 797043831:
                            if (str.equals(XML_TAG_PRE_SHARED_KEY)) {
                                c = 3;
                                break;
                            }
                        case 913629934:
                            if (str.equals(XML_TAG_PORTAL_VALIDITY_DURATION)) {
                                c = '\"';
                                break;
                            }
                        case 943896851:
                            if (str.equals(XML_TAG_USE_EXTERNAL_SCORES)) {
                                c = '&';
                                break;
                            }
                        case 1035394844:
                            if (str.equals(XML_TAG_LINKED_NETWORKS_LIST)) {
                                c = 20;
                                break;
                            }
                        case 1199498141:
                            if (str.equals(XML_TAG_CONFIG_KEY)) {
                                c = 0;
                                break;
                            }
                        case 1350351025:
                            if (str.equals(XML_TAG_LAST_UPDATE_UID)) {
                                c = '+';
                                break;
                            }
                        case 1476993207:
                            if (str.equals(XML_TAG_CREATOR_NAME)) {
                                c = ')';
                                break;
                            }
                        case 1526332790:
                            if (str.equals(XML_TAG_INTERNET_HISTORY)) {
                                c = 25;
                                break;
                            }
                        case 1617589150:
                            if (str.equals(XML_TAG_ACCESS_POINT_TYPE)) {
                                c = 7;
                                break;
                            }
                        case 1750336108:
                            if (str.equals(XML_TAG_CREATION_TIME)) {
                                c = '*';
                                break;
                            }
                        case 1851050768:
                            if (str.equals(XML_TAG_ALLOWED_AUTH_ALGOS)) {
                                c = 13;
                                break;
                            }
                        case 1905126713:
                            if (str.equals(XML_TAG_WEP_TX_KEY_INDEX)) {
                                c = 5;
                                break;
                            }
                        case 1955037270:
                            if (str.equals(XML_TAG_WEP_KEYS)) {
                                c = 4;
                                break;
                            }
                        case 1965854789:
                            if (str.equals(XML_TAG_HIDDEN_SSID)) {
                                c = 9;
                                break;
                            }
                        case 1985675705:
                            if (str.equals(XML_TAG_WIFI_PRO_TEMP_CREATED)) {
                                c = 29;
                                break;
                            }
                        case 2018202939:
                            if (str.equals(XML_TAG_NUM_ASSOCIATION)) {
                                c = '\'';
                                break;
                            }
                        case 2025240199:
                            if (str.equals(XML_TAG_PROVIDER_FRIENDLY_NAME)) {
                                c = 19;
                                break;
                            }
                        case 2092301303:
                            if (str.equals(XML_TAG_PORTAL_CONNECT)) {
                                c = ' ';
                                break;
                            }
                        case 2143705732:
                            if (str.equals(XML_TAG_REQUIRE_PMF)) {
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
                            configuration.BSSID = (String) value;
                            break;
                        case 3:
                            configuration.preSharedKey = (String) value;
                            break;
                        case 4:
                            populateWepKeysFromXmlValue(value, configuration.wepKeys);
                            break;
                        case 5:
                            configuration.wepTxKeyIndex = ((Integer) value).intValue();
                            break;
                        case 6:
                            configuration.connectToCellularAndWLAN = ((Integer) value).intValue();
                            break;
                        case 7:
                            configuration.wifiApType = ((Integer) value).intValue();
                            break;
                        case 8:
                            configuration.priority = ((Integer) value).intValue();
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
                        case 17:
                            int status = ((Integer) value).intValue();
                            if (status == 0) {
                                status = 2;
                            }
                            configuration.status = status;
                            break;
                        case 18:
                            configuration.FQDN = (String) value;
                            break;
                        case 19:
                            configuration.providerFriendlyName = (String) value;
                            break;
                        case 20:
                            configuration.linkedConfigurations = (HashMap) value;
                            break;
                        case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION:
                            configuration.defaultGwMacAddress = (String) value;
                            break;
                        case 22:
                            configuration.validatedInternetAccess = ((Boolean) value).booleanValue();
                            break;
                        case 23:
                            configuration.noInternetAccessExpected = ((Boolean) value).booleanValue();
                            break;
                        case 24:
                            configuration.numNoInternetAccessReports = ((Integer) value).intValue();
                            break;
                        case 25:
                            configuration.internetHistory = (String) value;
                            break;
                        case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED:
                            configuration.portalNetwork = ((Boolean) value).booleanValue();
                            configuration.noInternetAccess = !configuration.portalNetwork && configuration.hasNoInternetAccess();
                            if (!configuration.noInternetAccess && !configuration.portalNetwork) {
                                z = false;
                            }
                            configuration.wifiProNoInternetAccess = z;
                            if (configuration.noInternetAccess) {
                                configuration.internetAccessType = 2;
                            }
                            if (configuration.portalNetwork) {
                                configuration.internetAccessType = 3;
                            }
                            if (configuration.wifiProNoInternetAccess) {
                                break;
                            } else {
                                configuration.internetAccessType = 4;
                                break;
                            }
                        case 27:
                            configuration.lastHasInternetTimestamp = ((Long) value).longValue();
                            break;
                        case 28:
                            configuration.lastTrySwitchWifiTimestamp = ((Long) value).longValue();
                            break;
                        case 29:
                            configuration.isTempCreated = ((Boolean) value).booleanValue();
                            break;
                        case 30:
                            configuration.lastDhcpResults = (String) value;
                            break;
                        case 31:
                            configuration.internetSelfCureHistory = (String) value;
                            break;
                        case ' ':
                            configuration.isPortalConnect = ((Boolean) value).booleanValue();
                            break;
                        case '!':
                            configuration.portalAuthTimestamp = ((Long) value).longValue();
                            break;
                        case '\"':
                            configuration.portalValidityDuration = ((Long) value).longValue();
                            break;
                        case '#':
                            configuration.userApproved = ((Integer) value).intValue();
                            break;
                        case ISupplicantStaIfaceCallback.ReasonCode.STA_LEAVING:
                            configuration.meteredHint = ((Boolean) value).booleanValue();
                            break;
                        case '%':
                            configuration.meteredOverride = ((Integer) value).intValue();
                            break;
                        case '&':
                            configuration.useExternalScores = ((Boolean) value).booleanValue();
                            break;
                        case '\'':
                            configuration.numAssociation = ((Integer) value).intValue();
                            break;
                        case ISupplicantStaIfaceCallback.StatusCode.INVALID_IE:
                            configuration.creatorUid = ((Integer) value).intValue();
                            break;
                        case ISupplicantStaIfaceCallback.StatusCode.GROUP_CIPHER_NOT_VALID:
                            configuration.creatorName = (String) value;
                            break;
                        case '*':
                            configuration.creationTime = (String) value;
                            break;
                        case '+':
                            configuration.lastUpdateUid = ((Integer) value).intValue();
                            break;
                        case ISupplicantStaIfaceCallback.StatusCode.UNSUPPORTED_RSN_IE_VERSION:
                            configuration.lastUpdateName = (String) value;
                            break;
                        case '-':
                            configuration.lastConnectUid = ((Integer) value).intValue();
                            break;
                        case '.':
                            configuration.isLegacyPasspointConfig = ((Boolean) value).booleanValue();
                            break;
                        case '/':
                            configuration.roamingConsortiumIds = (long[]) value;
                            break;
                        case '0':
                            configuration.setRandomizedMacAddress(MacAddress.fromString((String) value));
                            break;
                        default:
                            parseFromXmlEx(configuration, valueName[0], value);
                            break;
                    }
                } else {
                    throw new XmlPullParserException("Missing value name");
                }
            }
            return Pair.create(configKeyInData, configuration);
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x0045  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x005c  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0062  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0068  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x0072  */
        private static void parseFromXmlEx(WifiConfiguration configuration, String name, Object value) throws XmlPullParserException {
            char c;
            int hashCode = name.hashCode();
            if (hashCode == 159114071) {
                if (name.equals(XML_TAG_WAPI_USER_CERT_PATH)) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 180572542) {
                if (name.equals(XML_TAG_WAPI_AS_CERT_PATH)) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 461626209) {
                if (name.equals(XML_TAG_ORI_SSID)) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 553095444 && name.equals(XML_TAG_WAPI_PSK_KEY_TYPE)) {
                c = 1;
                switch (c) {
                    case 0:
                        configuration.oriSsid = (String) value;
                        return;
                    case 1:
                        configuration.wapiPskTypeBcm = ((Integer) value).intValue();
                        return;
                    case 2:
                        configuration.wapiAsCertBcm = (String) value;
                        return;
                    case 3:
                        configuration.wapiUserCertBcm = (String) value;
                        return;
                    default:
                        throw new XmlPullParserException("Unknown value name found: " + name);
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
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
            XmlUtil.writeNextValue(out, XML_TAG_CLIENT_CERT, enterpriseConfig.getFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CLIENT_CERT));
            XmlUtil.writeNextValue(out, XML_TAG_CA_CERT, enterpriseConfig.getFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_CERT));
            XmlUtil.writeNextValue(out, XML_TAG_SUBJECT_MATCH, enterpriseConfig.getFieldValue("subject_match"));
            XmlUtil.writeNextValue(out, XML_TAG_ENGINE, enterpriseConfig.getFieldValue("engine"));
            XmlUtil.writeNextValue(out, XML_TAG_ENGINE_ID, enterpriseConfig.getFieldValue("engine_id"));
            XmlUtil.writeNextValue(out, XML_TAG_PRIVATE_KEY_ID, enterpriseConfig.getFieldValue("key_id"));
            XmlUtil.writeNextValue(out, XML_TAG_ALT_SUBJECT_MATCH, enterpriseConfig.getFieldValue("altsubject_match"));
            XmlUtil.writeNextValue(out, XML_TAG_DOM_SUFFIX_MATCH, enterpriseConfig.getFieldValue("domain_suffix_match"));
            XmlUtil.writeNextValue(out, XML_TAG_CA_PATH, enterpriseConfig.getFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_PATH));
            XmlUtil.writeNextValue(out, XML_TAG_EAP_METHOD, Integer.valueOf(enterpriseConfig.getEapMethod()));
            XmlUtil.writeNextValue(out, XML_TAG_PHASE2_METHOD, Integer.valueOf(enterpriseConfig.getPhase2Method()));
            XmlUtil.writeNextValue(out, XML_TAG_PLMN, enterpriseConfig.getPlmn());
            XmlUtil.writeNextValue(out, XML_TAG_REALM, enterpriseConfig.getRealm());
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ce, code lost:
            if (r5.equals(XML_TAG_ANON_IDENTITY) != false) goto L_0x00d2;
         */
        public static WifiEnterpriseConfig parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                char c = 1;
                String[] valueName = new String[1];
                Object value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] != null) {
                    String str = valueName[0];
                    switch (str.hashCode()) {
                        case -1956487222:
                            break;
                        case -1766961550:
                            if (str.equals(XML_TAG_DOM_SUFFIX_MATCH)) {
                                c = 10;
                                break;
                            }
                        case -1362213863:
                            if (str.equals(XML_TAG_SUBJECT_MATCH)) {
                                c = 5;
                                break;
                            }
                        case -1199574865:
                            if (str.equals(XML_TAG_CLIENT_CERT)) {
                                c = 3;
                                break;
                            }
                        case -596361182:
                            if (str.equals(XML_TAG_ALT_SUBJECT_MATCH)) {
                                c = 9;
                                break;
                            }
                        case -386463240:
                            if (str.equals(XML_TAG_PHASE2_METHOD)) {
                                c = 13;
                                break;
                            }
                        case -71117602:
                            if (str.equals(XML_TAG_IDENTITY)) {
                                c = 0;
                                break;
                            }
                        case 2458781:
                            if (str.equals(XML_TAG_PLMN)) {
                                c = 14;
                                break;
                            }
                        case 11048245:
                            if (str.equals(XML_TAG_EAP_METHOD)) {
                                c = 12;
                                break;
                            }
                        case 78834287:
                            if (str.equals(XML_TAG_REALM)) {
                                c = 15;
                                break;
                            }
                        case 1146405943:
                            if (str.equals(XML_TAG_PRIVATE_KEY_ID)) {
                                c = 8;
                                break;
                            }
                        case 1281629883:
                            if (str.equals(XML_TAG_PASSWORD)) {
                                c = 2;
                                break;
                            }
                        case 1885134621:
                            if (str.equals(XML_TAG_ENGINE_ID)) {
                                c = 7;
                                break;
                            }
                        case 2009831362:
                            if (str.equals(XML_TAG_CA_CERT)) {
                                c = 4;
                                break;
                            }
                        case 2010214851:
                            if (str.equals(XML_TAG_CA_PATH)) {
                                c = 11;
                                break;
                            }
                        case 2080171618:
                            if (str.equals(XML_TAG_ENGINE)) {
                                c = 6;
                                break;
                            }
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            enterpriseConfig.setFieldValue("identity", (String) value);
                            break;
                        case 1:
                            enterpriseConfig.setFieldValue("anonymous_identity", (String) value);
                            break;
                        case 2:
                            enterpriseConfig.setFieldValue("password", (String) value);
                            break;
                        case 3:
                            enterpriseConfig.setFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CLIENT_CERT, (String) value);
                            break;
                        case 4:
                            enterpriseConfig.setFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_CERT, (String) value);
                            break;
                        case 5:
                            enterpriseConfig.setFieldValue("subject_match", (String) value);
                            break;
                        case 6:
                            enterpriseConfig.setFieldValue("engine", (String) value);
                            break;
                        case 7:
                            enterpriseConfig.setFieldValue("engine_id", (String) value);
                            break;
                        case 8:
                            enterpriseConfig.setFieldValue("key_id", (String) value);
                            break;
                        case 9:
                            enterpriseConfig.setFieldValue("altsubject_match", (String) value);
                            break;
                        case 10:
                            enterpriseConfig.setFieldValue("domain_suffix_match", (String) value);
                            break;
                        case 11:
                            enterpriseConfig.setFieldValue(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_CA_PATH, (String) value);
                            break;
                        case 12:
                            enterpriseConfig.setEapMethod(((Integer) value).intValue());
                            break;
                        case 13:
                            enterpriseConfig.setPhase2Method(((Integer) value).intValue());
                            break;
                        case 14:
                            enterpriseConfig.setPlmn((String) value);
                            break;
                        case 15:
                            enterpriseConfig.setRealm((String) value);
                            break;
                        default:
                            throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                    }
                } else {
                    throw new XmlPullParserException("Missing value name");
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
        return !XmlUtils.nextElementWithin(in, sectionDepth);
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
        out.startDocument(null, true);
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
