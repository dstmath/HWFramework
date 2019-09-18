package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NetworkListStoreData implements WifiConfigStore.StoreData {
    private static final String TAG = "NetworkListStoreData";
    private static final String XML_TAG_SECTION_HEADER_IP_CONFIGURATION = "IpConfiguration";
    private static final String XML_TAG_SECTION_HEADER_NETWORK = "Network";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_LIST = "NetworkList";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_STATUS = "NetworkStatus";
    private static final String XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION = "WifiConfiguration";
    private static final String XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION = "WifiEnterpriseConfiguration";
    private final Context mContext;
    private List<WifiConfiguration> mSharedConfigurations;
    private List<WifiConfiguration> mUserConfigurations;

    NetworkListStoreData(Context context) {
        this.mContext = context;
    }

    public void serializeData(XmlSerializer out, boolean shared) throws XmlPullParserException, IOException {
        if (shared) {
            serializeNetworkList(out, this.mSharedConfigurations);
        } else {
            serializeNetworkList(out, this.mUserConfigurations);
        }
    }

    public void deserializeData(XmlPullParser in, int outerTagDepth, boolean shared) throws XmlPullParserException, IOException {
        if (in != null) {
            if (shared) {
                this.mSharedConfigurations = parseNetworkList(in, outerTagDepth);
            } else {
                this.mUserConfigurations = parseNetworkList(in, outerTagDepth);
            }
        }
    }

    public void resetData(boolean shared) {
        if (shared) {
            this.mSharedConfigurations = null;
        } else {
            this.mUserConfigurations = null;
        }
    }

    public String getName() {
        return XML_TAG_SECTION_HEADER_NETWORK_LIST;
    }

    public boolean supportShareData() {
        return true;
    }

    public void setSharedConfigurations(List<WifiConfiguration> configs) {
        this.mSharedConfigurations = configs;
    }

    public List<WifiConfiguration> getSharedConfigurations() {
        if (this.mSharedConfigurations == null) {
            return new ArrayList();
        }
        return this.mSharedConfigurations;
    }

    public void setUserConfigurations(List<WifiConfiguration> configs) {
        this.mUserConfigurations = configs;
    }

    public List<WifiConfiguration> getUserConfigurations() {
        if (this.mUserConfigurations == null) {
            return new ArrayList();
        }
        return this.mUserConfigurations;
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
                    Log.w(TAG, "parseFromXml creater: " + config.creatorUid + ", ssid: " + config.SSID + ", Bssid:" + ScanResultUtil.getConfusedBssid(config.BSSID));
                    config.BSSID = null;
                }
                networkList.add(config);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to parse network config. Skipping...", e);
            }
        }
        return networkList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0056 A[SYNTHETIC] */
    private WifiConfiguration parseNetwork(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        char c;
        Pair<String, WifiConfiguration> parsedConfig = null;
        WifiConfiguration.NetworkSelectionStatus status = null;
        IpConfiguration ipConfiguration = null;
        WifiEnterpriseConfig enterpriseConfig = null;
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            String str = headerName[0];
            int hashCode = str.hashCode();
            if (hashCode == -148477024) {
                if (str.equals(XML_TAG_SECTION_HEADER_NETWORK_STATUS)) {
                    c = 1;
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
            } else if (hashCode == 46473153) {
                if (str.equals(XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION)) {
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
            } else if (hashCode == 325854959) {
                if (str.equals(XML_TAG_SECTION_HEADER_IP_CONFIGURATION)) {
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
            } else if (hashCode == 1285464096 && str.equals(XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION)) {
                c = 3;
                switch (c) {
                    case 0:
                        if (parsedConfig == null) {
                            parsedConfig = XmlUtil.WifiConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                            break;
                        } else {
                            throw new XmlPullParserException("Detected duplicate tag for: WifiConfiguration");
                        }
                    case 1:
                        if (status == null) {
                            status = XmlUtil.NetworkSelectionStatusXmlUtil.parseFromXml(in, outerTagDepth + 1);
                            break;
                        } else {
                            throw new XmlPullParserException("Detected duplicate tag for: NetworkStatus");
                        }
                    case 2:
                        if (ipConfiguration == null) {
                            ipConfiguration = XmlUtil.IpConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                            break;
                        } else {
                            throw new XmlPullParserException("Detected duplicate tag for: IpConfiguration");
                        }
                    case 3:
                        if (enterpriseConfig == null) {
                            enterpriseConfig = XmlUtil.WifiEnterpriseConfigXmlUtil.parseFromXml(in, outerTagDepth + 1);
                            break;
                        } else {
                            throw new XmlPullParserException("Detected duplicate tag for: WifiEnterpriseConfiguration");
                        }
                    default:
                        throw new XmlPullParserException("Unknown tag under Network: " + headerName[0]);
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
        if (parsedConfig == null || parsedConfig.first == null || parsedConfig.second == null) {
            throw new XmlPullParserException("XML parsing of wifi configuration failed");
        }
        String configKeyParsed = (String) parsedConfig.first;
        WifiConfiguration configuration = (WifiConfiguration) parsedConfig.second;
        if (configKeyParsed.equals(configuration.configKey())) {
            String creatorName = this.mContext.getPackageManager().getNameForUid(configuration.creatorUid);
            if (creatorName == null) {
                Log.e(TAG, "Invalid creatorUid for saved network " + configuration.configKey() + ", creatorUid=" + configuration.creatorUid);
                configuration.creatorUid = 1000;
                configuration.creatorName = this.mContext.getPackageManager().getNameForUid(1000);
            } else if (!creatorName.equals(configuration.creatorName)) {
                Log.w(TAG, "Invalid creatorName for saved network " + configuration.configKey() + ", creatorUid=" + configuration.creatorUid + ", creatorName=" + configuration.creatorName);
                configuration.creatorName = creatorName;
            }
            configuration.setNetworkSelectionStatus(status);
            configuration.setIpConfiguration(ipConfiguration);
            if (enterpriseConfig != null) {
                configuration.enterpriseConfig = enterpriseConfig;
            }
            return configuration;
        }
        throw new XmlPullParserException("Configuration key does not match. Retrieved: " + configKeyParsed + ", Calculated: " + configKeyCalculated);
    }
}
