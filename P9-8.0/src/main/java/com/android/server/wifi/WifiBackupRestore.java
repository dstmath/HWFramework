package com.android.server.wifi;

import android.net.IpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.net.IpConfigStore;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.XmlUtil;
import com.android.server.wifi.util.XmlUtil.IpConfigurationXmlUtil;
import com.android.server.wifi.util.XmlUtil.WifiConfigurationXmlUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WifiBackupRestore {
    private static final int CURRENT_BACKUP_DATA_VERSION = 1;
    private static final int INITIAL_BACKUP_DATA_VERSION = 1;
    private static final String PSK_MASK_LINE_MATCH_PATTERN = "<.*PreSharedKey.*>.*<.*>";
    private static final String PSK_MASK_REPLACE_PATTERN = "$1*$3";
    private static final String PSK_MASK_SEARCH_PATTERN = "(<.*PreSharedKey.*>)(.*)(<.*>)";
    private static final String TAG = "WifiBackupRestore";
    private static final String WEP_KEYS_MASK_LINE_END_MATCH_PATTERN = "</string-array>";
    private static final String WEP_KEYS_MASK_LINE_START_MATCH_PATTERN = "<string-array.*WEPKeys.*num=\"[0-9]\">";
    private static final String WEP_KEYS_MASK_REPLACE_PATTERN = "$1*$3";
    private static final String WEP_KEYS_MASK_SEARCH_PATTERN = "(<.*=)(.*)(/>)";
    private static final String XML_TAG_DOCUMENT_HEADER = "WifiBackupData";
    private static final String XML_TAG_SECTION_HEADER_IP_CONFIGURATION = "IpConfiguration";
    private static final String XML_TAG_SECTION_HEADER_NETWORK = "Network";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_LIST = "NetworkList";
    private static final String XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION = "WifiConfiguration";
    private static final String XML_TAG_VERSION = "Version";
    private byte[] mDebugLastBackupDataRestored;
    private byte[] mDebugLastBackupDataRetrieved;
    private byte[] mDebugLastSupplicantBackupDataRestored;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiPermissionsUtil mWifiPermissionsUtil;

    public static class SupplicantBackupMigration {
        private static final String PSK_MASK_LINE_MATCH_PATTERN = ".*psk.*=.*";
        private static final String PSK_MASK_REPLACE_PATTERN = "$1*";
        private static final String PSK_MASK_SEARCH_PATTERN = "(.*psk.*=)(.*)";
        public static final String SUPPLICANT_KEY_CA_CERT = "ca_cert";
        public static final String SUPPLICANT_KEY_CA_PATH = "ca_path";
        public static final String SUPPLICANT_KEY_CLIENT_CERT = "client_cert";
        public static final String SUPPLICANT_KEY_EAP = "eap";
        public static final String SUPPLICANT_KEY_HIDDEN = "scan_ssid";
        public static final String SUPPLICANT_KEY_ID_STR = "id_str";
        public static final String SUPPLICANT_KEY_KEY_MGMT = "key_mgmt";
        public static final String SUPPLICANT_KEY_PSK = "psk";
        public static final String SUPPLICANT_KEY_SSID = "ssid";
        public static final String SUPPLICANT_KEY_WEP_KEY0 = WifiConfiguration.wepKeyVarNames[0];
        public static final String SUPPLICANT_KEY_WEP_KEY1 = WifiConfiguration.wepKeyVarNames[1];
        public static final String SUPPLICANT_KEY_WEP_KEY2 = WifiConfiguration.wepKeyVarNames[2];
        public static final String SUPPLICANT_KEY_WEP_KEY3 = WifiConfiguration.wepKeyVarNames[3];
        public static final String SUPPLICANT_KEY_WEP_KEY_IDX = "wep_tx_keyidx";
        private static final String WEP_KEYS_MASK_LINE_MATCH_PATTERN = (".*" + SUPPLICANT_KEY_WEP_KEY0.replace(HwWifiCHRStateManager.TYPE_AP_VENDOR, "") + ".*=.*");
        private static final String WEP_KEYS_MASK_REPLACE_PATTERN = "$1*";
        private static final String WEP_KEYS_MASK_SEARCH_PATTERN = ("(.*" + SUPPLICANT_KEY_WEP_KEY0.replace(HwWifiCHRStateManager.TYPE_AP_VENDOR, "") + ".*=)(.*)");

        static class SupplicantNetwork {
            public boolean certUsed = false;
            public boolean isEap = false;
            private String mParsedHiddenLine;
            private String mParsedIdStrLine;
            private String mParsedKeyMgmtLine;
            private String mParsedPskLine;
            private String mParsedSSIDLine;
            private String[] mParsedWepKeyLines = new String[4];
            private String mParsedWepTxKeyIdxLine;

            SupplicantNetwork() {
            }

            public static SupplicantNetwork readNetworkFromStream(BufferedReader in) {
                SupplicantNetwork n = new SupplicantNetwork();
                while (in.ready()) {
                    try {
                        String line = in.readLine();
                        if (line == null || line.startsWith("}")) {
                            break;
                        }
                        n.parseLine(line);
                    } catch (IOException e) {
                        return null;
                    }
                }
                return n;
            }

            void parseLine(String line) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.startsWith("ssid=")) {
                        this.mParsedSSIDLine = line;
                    } else if (line.startsWith("scan_ssid=")) {
                        this.mParsedHiddenLine = line;
                    } else if (line.startsWith("key_mgmt=")) {
                        this.mParsedKeyMgmtLine = line;
                        if (line.contains("EAP")) {
                            this.isEap = true;
                        }
                    } else if (line.startsWith("client_cert=")) {
                        this.certUsed = true;
                    } else if (line.startsWith("ca_cert=")) {
                        this.certUsed = true;
                    } else if (line.startsWith("ca_path=")) {
                        this.certUsed = true;
                    } else if (line.startsWith("eap=")) {
                        this.isEap = true;
                    } else if (line.startsWith("psk=")) {
                        this.mParsedPskLine = line;
                    } else if (line.startsWith(SupplicantBackupMigration.SUPPLICANT_KEY_WEP_KEY0 + "=")) {
                        this.mParsedWepKeyLines[0] = line;
                    } else if (line.startsWith(SupplicantBackupMigration.SUPPLICANT_KEY_WEP_KEY1 + "=")) {
                        this.mParsedWepKeyLines[1] = line;
                    } else if (line.startsWith(SupplicantBackupMigration.SUPPLICANT_KEY_WEP_KEY2 + "=")) {
                        this.mParsedWepKeyLines[2] = line;
                    } else if (line.startsWith(SupplicantBackupMigration.SUPPLICANT_KEY_WEP_KEY3 + "=")) {
                        this.mParsedWepKeyLines[3] = line;
                    } else if (line.startsWith("wep_tx_keyidx=")) {
                        this.mParsedWepTxKeyIdxLine = line;
                    } else if (line.startsWith("id_str=")) {
                        this.mParsedIdStrLine = line;
                    }
                }
            }

            public WifiConfiguration createWifiConfiguration() {
                if (this.mParsedSSIDLine == null) {
                    return null;
                }
                WifiConfiguration configuration = new WifiConfiguration();
                configuration.SSID = this.mParsedSSIDLine.substring(this.mParsedSSIDLine.indexOf(61) + 1);
                if (this.mParsedHiddenLine != null) {
                    configuration.hiddenSSID = Integer.parseInt(this.mParsedHiddenLine.substring(this.mParsedHiddenLine.indexOf(61) + 1)) != 0;
                }
                if (this.mParsedKeyMgmtLine == null) {
                    configuration.allowedKeyManagement.set(1);
                    configuration.allowedKeyManagement.set(2);
                } else {
                    String[] typeStrings = this.mParsedKeyMgmtLine.substring(this.mParsedKeyMgmtLine.indexOf(61) + 1).split("\\s+");
                    for (String ktype : typeStrings) {
                        if (ktype.equals("NONE")) {
                            configuration.allowedKeyManagement.set(0);
                        } else if (ktype.equals("WPA-PSK")) {
                            configuration.allowedKeyManagement.set(1);
                        } else if (ktype.equals("WPA-EAP")) {
                            configuration.allowedKeyManagement.set(2);
                        } else if (ktype.equals("IEEE8021X")) {
                            configuration.allowedKeyManagement.set(3);
                        }
                    }
                }
                if (this.mParsedPskLine != null) {
                    configuration.preSharedKey = this.mParsedPskLine.substring(this.mParsedPskLine.indexOf(61) + 1);
                }
                if (this.mParsedWepKeyLines[0] != null) {
                    configuration.wepKeys[0] = this.mParsedWepKeyLines[0].substring(this.mParsedWepKeyLines[0].indexOf(61) + 1);
                }
                if (this.mParsedWepKeyLines[1] != null) {
                    configuration.wepKeys[1] = this.mParsedWepKeyLines[1].substring(this.mParsedWepKeyLines[1].indexOf(61) + 1);
                }
                if (this.mParsedWepKeyLines[2] != null) {
                    configuration.wepKeys[2] = this.mParsedWepKeyLines[2].substring(this.mParsedWepKeyLines[2].indexOf(61) + 1);
                }
                if (this.mParsedWepKeyLines[3] != null) {
                    configuration.wepKeys[3] = this.mParsedWepKeyLines[3].substring(this.mParsedWepKeyLines[3].indexOf(61) + 1);
                }
                if (this.mParsedWepTxKeyIdxLine != null) {
                    configuration.wepTxKeyIndex = Integer.valueOf(this.mParsedWepTxKeyIdxLine.substring(this.mParsedWepTxKeyIdxLine.indexOf(61) + 1)).intValue();
                }
                if (this.mParsedIdStrLine != null) {
                    String idString = this.mParsedIdStrLine.substring(this.mParsedIdStrLine.indexOf(61) + 1);
                    if (idString != null) {
                        Map<String, String> extras = SupplicantStaNetworkHal.parseNetworkExtra(NativeUtil.removeEnclosingQuotes(idString));
                        String configKey = (String) extras.get("configKey");
                        if (!configKey.equals(configuration.configKey())) {
                            Log.w(WifiBackupRestore.TAG, "Configuration key does not match. Retrieved: " + configKey + ", Calculated: " + configuration.configKey());
                        }
                        if (Integer.parseInt((String) extras.get("creatorUid")) >= 10000) {
                            Log.d(WifiBackupRestore.TAG, "Ignoring network from non-system app: " + configuration.configKey());
                            return null;
                        }
                    }
                }
                return configuration;
            }
        }

        static class SupplicantNetworks {
            final ArrayList<SupplicantNetwork> mNetworks = new ArrayList(8);

            SupplicantNetworks() {
            }

            public void readNetworksFromStream(BufferedReader in) {
                while (in.ready()) {
                    try {
                        String line = in.readLine();
                        if (line != null && line.startsWith("network")) {
                            SupplicantNetwork net = SupplicantNetwork.readNetworkFromStream(in);
                            if (net.isEap || net.certUsed) {
                                Log.d(WifiBackupRestore.TAG, "Skipping enterprise network for restore: " + net.mParsedSSIDLine + " / " + net.mParsedKeyMgmtLine);
                            } else {
                                this.mNetworks.add(net);
                            }
                        }
                    } catch (IOException e) {
                        return;
                    }
                }
            }

            public List<WifiConfiguration> retrieveWifiConfigurations() {
                ArrayList<WifiConfiguration> wifiConfigurations = new ArrayList();
                for (SupplicantNetwork net : this.mNetworks) {
                    WifiConfiguration wifiConfiguration = net.createWifiConfiguration();
                    if (wifiConfiguration != null) {
                        Log.v(WifiBackupRestore.TAG, "Parsed Configuration: " + wifiConfiguration.configKey());
                        wifiConfigurations.add(wifiConfiguration);
                    }
                }
                return wifiConfigurations;
            }
        }

        public static String createLogFromBackupData(byte[] data) {
            StringBuilder sb = new StringBuilder();
            try {
                for (String line : new String(data, StandardCharsets.UTF_8.name()).split("\n")) {
                    String line2;
                    if (line2.matches(PSK_MASK_LINE_MATCH_PATTERN)) {
                        line2 = line2.replaceAll(PSK_MASK_SEARCH_PATTERN, "$1*");
                    }
                    if (line2.matches(WEP_KEYS_MASK_LINE_MATCH_PATTERN)) {
                        line2 = line2.replaceAll(WEP_KEYS_MASK_SEARCH_PATTERN, "$1*");
                    }
                    sb.append(line2).append("\n");
                }
                return sb.toString();
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }
    }

    public WifiBackupRestore(WifiPermissionsUtil wifiPermissionsUtil) {
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
    }

    public byte[] retrieveBackupDataFromConfigurations(List<WifiConfiguration> configurations) {
        if (configurations == null) {
            Log.e(TAG, "Invalid configuration list received");
            return new byte[0];
        }
        try {
            XmlSerializer out = new FastXmlSerializer();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            out.setOutput(outputStream, StandardCharsets.UTF_8.name());
            XmlUtil.writeDocumentStart(out, XML_TAG_DOCUMENT_HEADER);
            XmlUtil.writeNextValue(out, XML_TAG_VERSION, Integer.valueOf(1));
            writeNetworkConfigurationsToXml(out, configurations);
            XmlUtil.writeDocumentEnd(out, XML_TAG_DOCUMENT_HEADER);
            byte[] data = outputStream.toByteArray();
            if (this.mVerboseLoggingEnabled) {
                this.mDebugLastBackupDataRetrieved = data;
            }
            return data;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error retrieving the backup data: " + e);
            return new byte[0];
        } catch (IOException e2) {
            Log.e(TAG, "Error retrieving the backup data: " + e2);
            return new byte[0];
        }
    }

    private void writeNetworkConfigurationsToXml(XmlSerializer out, List<WifiConfiguration> configurations) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK_LIST);
        for (WifiConfiguration configuration : configurations) {
            if (!(configuration.isEnterprise() || configuration.isPasspoint())) {
                if (this.mWifiPermissionsUtil.checkConfigOverridePermission(configuration.creatorUid)) {
                    XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK);
                    writeNetworkConfigurationToXml(out, configuration);
                    XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK);
                } else {
                    Log.d(TAG, "Ignoring network from an app with no config override permission: " + configuration.configKey());
                }
            }
        }
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK_LIST);
    }

    private void writeNetworkConfigurationToXml(XmlSerializer out, WifiConfiguration configuration) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        WifiConfigurationXmlUtil.writeToXmlForBackup(out, configuration);
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
        IpConfigurationXmlUtil.writeToXml(out, configuration.getIpConfiguration());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
    }

    public List<WifiConfiguration> retrieveConfigurationsFromBackupData(byte[] data) {
        if (data == null || data.length == 0) {
            Log.e(TAG, "Invalid backup data received");
            return null;
        }
        try {
            if (this.mVerboseLoggingEnabled) {
                this.mDebugLastBackupDataRestored = data;
            }
            XmlPullParser in = Xml.newPullParser();
            in.setInput(new ByteArrayInputStream(data), StandardCharsets.UTF_8.name());
            XmlUtil.gotoDocumentStart(in, XML_TAG_DOCUMENT_HEADER);
            int rootTagDepth = in.getDepth();
            int version = ((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_VERSION)).intValue();
            if (version >= 1 && version <= 1) {
                return parseNetworkConfigurationsFromXml(in, rootTagDepth, version);
            }
            Log.e(TAG, "Invalid version of data: " + version);
            return null;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error parsing the backup data: " + e);
            return null;
        } catch (IOException e2) {
            Log.e(TAG, "Error parsing the backup data: " + e2);
            return null;
        }
    }

    private List<WifiConfiguration> parseNetworkConfigurationsFromXml(XmlPullParser in, int outerTagDepth, int dataVersion) throws XmlPullParserException, IOException {
        XmlUtil.gotoNextSectionWithName(in, XML_TAG_SECTION_HEADER_NETWORK_LIST, outerTagDepth);
        int networkListTagDepth = outerTagDepth + 1;
        List<WifiConfiguration> configurations = new ArrayList();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_NETWORK, networkListTagDepth)) {
            WifiConfiguration configuration = parseNetworkConfigurationFromXml(in, dataVersion, networkListTagDepth);
            if (configuration != null) {
                Log.v(TAG, "Parsed Configuration: " + configuration.configKey());
                configurations.add(configuration);
            }
        }
        return configurations;
    }

    private WifiConfiguration parseWifiConfigurationFromXmlAndValidateConfigKey(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Pair<String, WifiConfiguration> parsedConfig = WifiConfigurationXmlUtil.parseFromXml(in, outerTagDepth);
        if (parsedConfig == null || parsedConfig.first == null || parsedConfig.second == null) {
            return null;
        }
        String configKeyParsed = parsedConfig.first;
        WifiConfiguration configuration = parsedConfig.second;
        String configKeyCalculated = configuration.configKey();
        if (!configKeyParsed.equals(configKeyCalculated)) {
            String configKeyMismatchLog = "Configuration key does not match. Retrieved: " + configKeyParsed + ", Calculated: " + configKeyCalculated;
            if (configuration.shared) {
                Log.e(TAG, configKeyMismatchLog);
                return null;
            }
            Log.w(TAG, configKeyMismatchLog);
        }
        return configuration;
    }

    private WifiConfiguration parseNetworkConfigurationFromXml(XmlPullParser in, int dataVersion, int outerTagDepth) throws XmlPullParserException, IOException {
        if (dataVersion != 1) {
            return null;
        }
        int networkTagDepth = outerTagDepth + 1;
        XmlUtil.gotoNextSectionWithName(in, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION, networkTagDepth);
        int configTagDepth = networkTagDepth + 1;
        WifiConfiguration configuration = parseWifiConfigurationFromXmlAndValidateConfigKey(in, configTagDepth);
        if (configuration == null) {
            return null;
        }
        XmlUtil.gotoNextSectionWithName(in, XML_TAG_SECTION_HEADER_IP_CONFIGURATION, networkTagDepth);
        configuration.setIpConfiguration(IpConfigurationXmlUtil.parseFromXml(in, configTagDepth));
        return configuration;
    }

    private String createLogFromBackupData(byte[] data) {
        StringBuilder sb = new StringBuilder();
        try {
            boolean wepKeysLine = false;
            for (String line : new String(data, StandardCharsets.UTF_8.name()).split("\n")) {
                String line2;
                if (line2.matches(PSK_MASK_LINE_MATCH_PATTERN)) {
                    line2 = line2.replaceAll(PSK_MASK_SEARCH_PATTERN, "$1*$3");
                }
                if (line2.matches(WEP_KEYS_MASK_LINE_START_MATCH_PATTERN)) {
                    wepKeysLine = true;
                } else if (line2.matches(WEP_KEYS_MASK_LINE_END_MATCH_PATTERN)) {
                    wepKeysLine = false;
                } else if (wepKeysLine) {
                    line2 = line2.replaceAll(WEP_KEYS_MASK_SEARCH_PATTERN, "$1*$3");
                }
                sb.append(line2).append("\n");
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public List<WifiConfiguration> retrieveConfigurationsFromSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) {
        if (supplicantData == null || supplicantData.length == 0) {
            Log.e(TAG, "Invalid supplicant backup data received");
            return null;
        }
        int i;
        if (this.mVerboseLoggingEnabled) {
            this.mDebugLastSupplicantBackupDataRestored = supplicantData;
        }
        SupplicantNetworks supplicantNetworks = new SupplicantNetworks();
        char[] restoredAsChars = new char[supplicantData.length];
        for (i = 0; i < supplicantData.length; i++) {
            restoredAsChars[i] = (char) supplicantData[i];
        }
        supplicantNetworks.readNetworksFromStream(new BufferedReader(new CharArrayReader(restoredAsChars)));
        List<WifiConfiguration> configurations = supplicantNetworks.retrieveWifiConfigurations();
        if (ipConfigData == null || ipConfigData.length == 0) {
            Log.e(TAG, "Invalid ipconfig backup data received");
        } else {
            SparseArray<IpConfiguration> networks = IpConfigStore.readIpAndProxyConfigurations(new ByteArrayInputStream(ipConfigData));
            if (networks != null) {
                for (i = 0; i < networks.size(); i++) {
                    int id = networks.keyAt(i);
                    for (WifiConfiguration configuration : configurations) {
                        if (configuration.configKey().hashCode() == id) {
                            configuration.setIpConfiguration((IpConfiguration) networks.valueAt(i));
                        }
                    }
                }
            } else {
                Log.e(TAG, "Failed to parse ipconfig data");
            }
        }
        return configurations;
    }

    public void enableVerboseLogging(int verbose) {
        boolean z = false;
        if (verbose > 0) {
            z = true;
        }
        this.mVerboseLoggingEnabled = z;
        if (!this.mVerboseLoggingEnabled) {
            this.mDebugLastBackupDataRetrieved = null;
            this.mDebugLastBackupDataRestored = null;
            this.mDebugLastSupplicantBackupDataRestored = null;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiBackupRestore");
        if (this.mDebugLastBackupDataRetrieved != null) {
            pw.println("Last backup data retrieved: " + createLogFromBackupData(this.mDebugLastBackupDataRetrieved));
        }
        if (this.mDebugLastBackupDataRestored != null) {
            pw.println("Last backup data restored: " + createLogFromBackupData(this.mDebugLastBackupDataRestored));
        }
        if (this.mDebugLastSupplicantBackupDataRestored != null) {
            pw.println("Last old backup data restored: " + SupplicantBackupMigration.createLogFromBackupData(this.mDebugLastSupplicantBackupDataRestored));
        }
    }
}
