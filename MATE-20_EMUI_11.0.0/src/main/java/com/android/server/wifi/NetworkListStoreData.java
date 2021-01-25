package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.hwUtil.ScanResultUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class NetworkListStoreData implements WifiConfigStore.StoreData {
    private static final String TAG = "NetworkListStoreData";
    private static final String XML_TAG_SECTION_HEADER_IP_CONFIGURATION = "IpConfiguration";
    private static final String XML_TAG_SECTION_HEADER_NETWORK = "Network";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_LIST = "NetworkList";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_STATUS = "NetworkStatus";
    private static final String XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION = "WifiConfiguration";
    private static final String XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION = "WifiEnterpriseConfiguration";
    private List<WifiConfiguration> mConfigurations;
    private final Context mContext;

    NetworkListStoreData(Context context) {
        this.mContext = context;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeNetworkList(out, this.mConfigurations);
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            this.mConfigurations = parseNetworkList(in, outerTagDepth);
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mConfigurations = null;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public boolean hasNewDataToSerialize() {
        return true;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public String getName() {
        return XML_TAG_SECTION_HEADER_NETWORK_LIST;
    }

    public void setConfigurations(List<WifiConfiguration> configs) {
        this.mConfigurations = configs;
    }

    public List<WifiConfiguration> getConfigurations() {
        List<WifiConfiguration> list = this.mConfigurations;
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }

    private void serializeNetworkList(XmlSerializer out, List<WifiConfiguration> networkList) throws XmlPullParserException, IOException {
        if (networkList != null) {
            for (WifiConfiguration network : networkList) {
                serializeNetwork(out, network);
            }
        }
    }

    private void serializeNetwork(XmlSerializer out, WifiConfiguration config) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        XmlUtil.WifiConfigurationXmlUtil.writeToXmlForConfigStore(out, config);
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK_STATUS);
        XmlUtil.NetworkSelectionStatusXmlUtil.writeToXml(out, config.getNetworkSelectionStatus());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK_STATUS);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
        XmlUtil.IpConfigurationXmlUtil.writeToXml(out, config.getIpConfiguration());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
        if (!(config.enterpriseConfig == null || config.enterpriseConfig.getEapMethod() == -1)) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
            XmlUtil.WifiEnterpriseConfigXmlUtil.writeToXml(out, config.enterpriseConfig);
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
        }
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK);
    }

    private List<WifiConfiguration> parseNetworkList(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        List<WifiConfiguration> networkList = new ArrayList<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_NETWORK, outerTagDepth)) {
            try {
                WifiConfiguration config = parseNetwork(in, outerTagDepth + 1);
                int appId = UserHandle.getAppId(config.creatorUid);
                if (config.BSSID != null && (appId == 0 || appId == 1000 || appId == 1010)) {
                    Log.w(TAG, "parseFromXml creater: " + config.creatorUid + ", ssid: " + StringUtilEx.safeDisplaySsid(config.SSID) + ", Bssid:" + ScanResultUtilEx.getConfusedBssid(config.BSSID));
                    config.BSSID = null;
                }
                networkList.add(config);
            } catch (RuntimeException | XmlPullParserException e) {
                Log.e(TAG, "Failed to parse network config. Skipping...", e);
            }
        }
        return networkList;
    }

    private WifiConfiguration parseNetwork(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Pair<String, WifiConfiguration> parsedConfig = null;
        WifiConfiguration.NetworkSelectionStatus status = null;
        IpConfiguration ipConfiguration = null;
        WifiEnterpriseConfig enterpriseConfig = null;
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            String str = headerName[0];
            char c = 65535;
            switch (str.hashCode()) {
                case -148477024:
                    if (str.equals(XML_TAG_SECTION_HEADER_NETWORK_STATUS)) {
                        c = 1;
                        break;
                    }
                    break;
                case 46473153:
                    if (str.equals(XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION)) {
                        c = 0;
                        break;
                    }
                    break;
                case 325854959:
                    if (str.equals(XML_TAG_SECTION_HEADER_IP_CONFIGURATION)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1285464096:
                    if (str.equals(XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c != 0) {
                if (c != 1) {
                    if (c != 2) {
                        if (c != 3) {
                            throw new XmlPullParserException("Unknown tag under Network: " + headerName[0]);
                        } else if (enterpriseConfig == null) {
                            enterpriseConfig = XmlUtil.WifiEnterpriseConfigXmlUtil.parseFromXml(in, outerTagDepth + 1);
                        } else {
                            throw new XmlPullParserException("Detected duplicate tag for: WifiEnterpriseConfiguration");
                        }
                    } else if (ipConfiguration == null) {
                        ipConfiguration = XmlUtil.IpConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                    } else {
                        throw new XmlPullParserException("Detected duplicate tag for: IpConfiguration");
                    }
                } else if (status == null) {
                    status = XmlUtil.NetworkSelectionStatusXmlUtil.parseFromXml(in, outerTagDepth + 1);
                } else {
                    throw new XmlPullParserException("Detected duplicate tag for: NetworkStatus");
                }
            } else if (parsedConfig == null) {
                parsedConfig = XmlUtil.WifiConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
            } else {
                throw new XmlPullParserException("Detected duplicate tag for: WifiConfiguration");
            }
        }
        if (parsedConfig == null || parsedConfig.first == null || parsedConfig.second == null) {
            throw new XmlPullParserException("XML parsing of wifi configuration failed");
        }
        String configKeyParsed = (String) parsedConfig.first;
        WifiConfiguration configuration = (WifiConfiguration) parsedConfig.second;
        String configKeyCalculated = configuration.configKey();
        if (configKeyParsed.equals(configKeyCalculated)) {
            String creatorName = this.mContext.getPackageManager().getNameForUid(configuration.creatorUid);
            if (creatorName == null) {
                Log.e(TAG, "Invalid creatorUid for saved network " + StringUtilEx.safeDisplaySsid(configuration.getPrintableSsid()) + ", creatorUid=" + configuration.creatorUid);
                configuration.creatorUid = 1000;
                configuration.creatorName = this.mContext.getPackageManager().getNameForUid(1000);
            } else if (!creatorName.equals(configuration.creatorName)) {
                Log.w(TAG, "Invalid creatorName for saved network " + StringUtilEx.safeDisplaySsid(configuration.getPrintableSsid()) + ", creatorUid=" + configuration.creatorUid + ", creatorName=" + configuration.creatorName);
                configuration.creatorName = creatorName;
            }
            configuration.setNetworkSelectionStatus(status);
            configuration.setIpConfiguration(ipConfiguration);
            if (enterpriseConfig != null) {
                configuration.enterpriseConfig = enterpriseConfig;
            }
            return configuration;
        }
        throw new XmlPullParserException("Configuration key does not match. Retrieved: " + StringUtilEx.safeDisplaySsid(configKeyParsed) + ", Calculated: " + StringUtilEx.safeDisplaySsid(configKeyCalculated));
    }
}
