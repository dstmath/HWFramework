package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiNetworkSuggestion;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.XmlUtils;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.WifiNetworkSuggestionsManager;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NetworkSuggestionStoreData implements WifiConfigStore.StoreData {
    private static final String TAG = "NetworkSuggestionStoreData";
    private static final String XML_TAG_IS_APP_INTERACTION_REQUIRED = "IsAppInteractionRequired";
    private static final String XML_TAG_IS_USER_INTERACTION_REQUIRED = "IsUserInteractionRequired";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION = "NetworkSuggestion";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION_MAP = "NetworkSuggestionMap";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION_PER_APP = "NetworkSuggestionPerApp";
    private static final String XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION = "WifiConfiguration";
    private static final String XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION = "WifiEnterpriseConfiguration";
    private static final String XML_TAG_SUGGESTOR_HAS_USER_APPROVED = "SuggestorHasUserApproved";
    private static final String XML_TAG_SUGGESTOR_MAX_SIZE = "SuggestorMaxSize";
    private static final String XML_TAG_SUGGESTOR_PACKAGE_NAME = "SuggestorPackageName";
    private static final String XML_TAG_SUGGESTOR_UID = "SuggestorUid";
    private final DataSource mDataSource;

    public interface DataSource {
        void fromDeserialized(Map<String, WifiNetworkSuggestionsManager.PerAppInfo> map);

        boolean hasNewDataToSerialize();

        void reset();

        Map<String, WifiNetworkSuggestionsManager.PerAppInfo> toSerialize();
    }

    public NetworkSuggestionStoreData(DataSource dataSource) {
        this.mDataSource = dataSource;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeNetworkSuggestionsMap(out, this.mDataSource.toSerialize());
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            this.mDataSource.fromDeserialized(parseNetworkSuggestionsMap(in, outerTagDepth));
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mDataSource.reset();
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public boolean hasNewDataToSerialize() {
        return this.mDataSource.hasNewDataToSerialize();
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public String getName() {
        return XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION_MAP;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public int getStoreFileId() {
        return 2;
    }

    private void serializeNetworkSuggestionsMap(XmlSerializer out, Map<String, WifiNetworkSuggestionsManager.PerAppInfo> networkSuggestionsMap) throws XmlPullParserException, IOException {
        if (networkSuggestionsMap != null) {
            for (Map.Entry<String, WifiNetworkSuggestionsManager.PerAppInfo> entry : networkSuggestionsMap.entrySet()) {
                boolean hasUserApproved = entry.getValue().hasUserApproved;
                int maxSize = entry.getValue().maxSize;
                Set<WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion> networkSuggestions = entry.getValue().extNetworkSuggestions;
                XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION_PER_APP);
                XmlUtil.writeNextValue(out, XML_TAG_SUGGESTOR_PACKAGE_NAME, entry.getKey());
                XmlUtil.writeNextValue(out, XML_TAG_SUGGESTOR_HAS_USER_APPROVED, Boolean.valueOf(hasUserApproved));
                XmlUtil.writeNextValue(out, XML_TAG_SUGGESTOR_MAX_SIZE, Integer.valueOf(maxSize));
                serializeExtNetworkSuggestions(out, networkSuggestions);
                XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION_PER_APP);
            }
        }
    }

    private void serializeExtNetworkSuggestions(XmlSerializer out, Set<WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion> extNetworkSuggestions) throws XmlPullParserException, IOException {
        for (WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion extNetworkSuggestion : extNetworkSuggestions) {
            serializeNetworkSuggestion(out, extNetworkSuggestion.wns);
        }
    }

    private void serializeNetworkSuggestion(XmlSerializer out, WifiNetworkSuggestion suggestion) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        XmlUtil.WifiConfigurationXmlUtil.writeToXmlForConfigStore(out, suggestion.wifiConfiguration);
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        if (!(suggestion.wifiConfiguration.enterpriseConfig == null || suggestion.wifiConfiguration.enterpriseConfig.getEapMethod() == -1)) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
            XmlUtil.WifiEnterpriseConfigXmlUtil.writeToXml(out, suggestion.wifiConfiguration.enterpriseConfig);
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
        }
        XmlUtil.writeNextValue(out, XML_TAG_IS_APP_INTERACTION_REQUIRED, Boolean.valueOf(suggestion.isAppInteractionRequired));
        XmlUtil.writeNextValue(out, XML_TAG_IS_USER_INTERACTION_REQUIRED, Boolean.valueOf(suggestion.isUserInteractionRequired));
        XmlUtil.writeNextValue(out, XML_TAG_SUGGESTOR_UID, Integer.valueOf(suggestion.suggestorUid));
        XmlUtil.writeNextValue(out, XML_TAG_SUGGESTOR_PACKAGE_NAME, suggestion.suggestorPackageName);
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION);
    }

    private Map<String, WifiNetworkSuggestionsManager.PerAppInfo> parseNetworkSuggestionsMap(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Map<String, WifiNetworkSuggestionsManager.PerAppInfo> networkSuggestionsMap = new HashMap<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION_PER_APP, outerTagDepth)) {
            try {
                String packageName = (String) XmlUtil.readNextValueWithName(in, XML_TAG_SUGGESTOR_PACKAGE_NAME);
                boolean hasUserApproved = ((Boolean) XmlUtil.readNextValueWithName(in, XML_TAG_SUGGESTOR_HAS_USER_APPROVED)).booleanValue();
                int maxSize = ((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_SUGGESTOR_MAX_SIZE)).intValue();
                WifiNetworkSuggestionsManager.PerAppInfo perAppInfo = new WifiNetworkSuggestionsManager.PerAppInfo(packageName);
                Set<WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion> extNetworkSuggestions = parseExtNetworkSuggestions(in, outerTagDepth + 1, perAppInfo);
                perAppInfo.hasUserApproved = hasUserApproved;
                perAppInfo.maxSize = maxSize;
                perAppInfo.extNetworkSuggestions.addAll(extNetworkSuggestions);
                networkSuggestionsMap.put(packageName, perAppInfo);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to parse network suggestion. Skipping...", e);
            }
        }
        return networkSuggestionsMap;
    }

    private Set<WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion> parseExtNetworkSuggestions(XmlPullParser in, int outerTagDepth, WifiNetworkSuggestionsManager.PerAppInfo perAppInfo) throws XmlPullParserException, IOException {
        Set<WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion> extNetworkSuggestions = new HashSet<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_NETWORK_SUGGESTION, outerTagDepth)) {
            try {
                extNetworkSuggestions.add(WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion.fromWns(parseNetworkSuggestion(in, outerTagDepth + 1), perAppInfo));
            } catch (RuntimeException | XmlPullParserException e) {
                Log.e(TAG, "Failed to parse network suggestion. Skipping...", e);
            }
        }
        return extNetworkSuggestions;
    }

    private WifiNetworkSuggestion parseNetworkSuggestion(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Pair<String, WifiConfiguration> parsedConfig = null;
        WifiEnterpriseConfig enterpriseConfig = null;
        boolean isAppInteractionRequired = false;
        boolean isUserInteractionRequired = false;
        int suggestorUid = -1;
        String suggestorPackageName = null;
        while (true) {
            char c = 65535;
            if (XmlUtils.nextElementWithin(in, outerTagDepth)) {
                if (in.getAttributeValue(null, "name") != null) {
                    String[] valueName = new String[1];
                    Object value = XmlUtil.readCurrentValue(in, valueName);
                    String str = valueName[0];
                    switch (str.hashCode()) {
                        case -1365630628:
                            if (str.equals(XML_TAG_IS_USER_INTERACTION_REQUIRED)) {
                                c = 1;
                                break;
                            }
                            break;
                        case -914503382:
                            if (str.equals(XML_TAG_SUGGESTOR_PACKAGE_NAME)) {
                                c = 3;
                                break;
                            }
                            break;
                        case -879551014:
                            if (str.equals(XML_TAG_IS_APP_INTERACTION_REQUIRED)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 129648265:
                            if (str.equals(XML_TAG_SUGGESTOR_UID)) {
                                c = 2;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        isAppInteractionRequired = ((Boolean) value).booleanValue();
                    } else if (c == 1) {
                        isUserInteractionRequired = ((Boolean) value).booleanValue();
                    } else if (c == 2) {
                        suggestorUid = ((Integer) value).intValue();
                    } else if (c == 3) {
                        suggestorPackageName = (String) value;
                    } else {
                        throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                    }
                } else {
                    String tagName = in.getName();
                    if (tagName != null) {
                        int hashCode = tagName.hashCode();
                        if (hashCode != 46473153) {
                            if (hashCode == 1285464096 && tagName.equals(XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION)) {
                                c = 1;
                            }
                        } else if (tagName.equals(XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION)) {
                            c = 0;
                        }
                        if (c != 0) {
                            if (c != 1) {
                                throw new XmlPullParserException("Unknown tag under NetworkSuggestion: " + in.getName());
                            } else if (enterpriseConfig == null) {
                                enterpriseConfig = XmlUtil.WifiEnterpriseConfigXmlUtil.parseFromXml(in, outerTagDepth + 1);
                            } else {
                                throw new XmlPullParserException("Detected duplicate tag for: WifiEnterpriseConfiguration");
                            }
                        } else if (parsedConfig == null) {
                            parsedConfig = XmlUtil.WifiConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                        } else {
                            throw new XmlPullParserException("Detected duplicate tag for: WifiConfiguration");
                        }
                    } else {
                        throw new XmlPullParserException("Unexpected null under NetworkSuggestion");
                    }
                }
            } else if (parsedConfig == null || parsedConfig.second == null) {
                throw new XmlPullParserException("XML parsing of wifi configuration failed");
            } else if (suggestorUid == -1) {
                throw new XmlPullParserException("XML parsing of suggestor uid failed");
            } else if (suggestorPackageName != null) {
                WifiConfiguration wifiConfiguration = (WifiConfiguration) parsedConfig.second;
                if (enterpriseConfig != null) {
                    wifiConfiguration.enterpriseConfig = enterpriseConfig;
                }
                return new WifiNetworkSuggestion(wifiConfiguration, isAppInteractionRequired, isUserInteractionRequired, suggestorUid, suggestorPackageName);
            } else {
                throw new XmlPullParserException("XML parsing of suggestor package name failed");
            }
        }
    }
}
