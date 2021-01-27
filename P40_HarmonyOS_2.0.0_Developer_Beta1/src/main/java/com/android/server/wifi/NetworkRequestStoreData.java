package com.android.server.wifi;

import android.net.MacAddress;
import android.util.Log;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.WifiNetworkFactory;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NetworkRequestStoreData implements WifiConfigStore.StoreData {
    private static final String TAG = "NetworkRequestStoreData";
    private static final String XML_TAG_ACCESS_POINT_BSSID = "BSSID";
    private static final String XML_TAG_ACCESS_POINT_NETWORK_TYPE = "NetworkType";
    private static final String XML_TAG_ACCESS_POINT_SSID = "SSID";
    private static final String XML_TAG_REQUESTOR_PACKAGE_NAME = "RequestorPackageName";
    private static final String XML_TAG_SECTION_HEADER_ACCESS_POINT = "AccessPoint";
    private static final String XML_TAG_SECTION_HEADER_APPROVED_ACCESS_POINTS_PER_APP = "ApprovedAccessPointsPerApp";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_REQUEST_MAP = "NetworkRequestMap";
    private final DataSource mDataSource;

    public interface DataSource {
        void fromDeserialized(Map<String, Set<WifiNetworkFactory.AccessPoint>> map);

        boolean hasNewDataToSerialize();

        void reset();

        Map<String, Set<WifiNetworkFactory.AccessPoint>> toSerialize();
    }

    public NetworkRequestStoreData(DataSource dataSource) {
        this.mDataSource = dataSource;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeApprovedAccessPointsMap(out, this.mDataSource.toSerialize());
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            this.mDataSource.fromDeserialized(parseApprovedAccessPointsMap(in, outerTagDepth));
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
        return XML_TAG_SECTION_HEADER_NETWORK_REQUEST_MAP;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public int getStoreFileId() {
        return 1;
    }

    private void serializeApprovedAccessPointsMap(XmlSerializer out, Map<String, Set<WifiNetworkFactory.AccessPoint>> approvedAccessPointsMap) throws XmlPullParserException, IOException {
        if (approvedAccessPointsMap != null) {
            for (Map.Entry<String, Set<WifiNetworkFactory.AccessPoint>> entry : approvedAccessPointsMap.entrySet()) {
                XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_APPROVED_ACCESS_POINTS_PER_APP);
                XmlUtil.writeNextValue(out, XML_TAG_REQUESTOR_PACKAGE_NAME, entry.getKey());
                serializeApprovedAccessPoints(out, entry.getValue());
                XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_APPROVED_ACCESS_POINTS_PER_APP);
            }
        }
    }

    private void serializeApprovedAccessPoints(XmlSerializer out, Set<WifiNetworkFactory.AccessPoint> approvedAccessPoints) throws XmlPullParserException, IOException {
        for (WifiNetworkFactory.AccessPoint approvedAccessPoint : approvedAccessPoints) {
            serializeApprovedAccessPoint(out, approvedAccessPoint);
        }
    }

    private void serializeApprovedAccessPoint(XmlSerializer out, WifiNetworkFactory.AccessPoint approvedAccessPoint) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_ACCESS_POINT);
        XmlUtil.writeNextValue(out, "SSID", approvedAccessPoint.ssid);
        XmlUtil.writeNextValue(out, "BSSID", approvedAccessPoint.bssid.toString());
        XmlUtil.writeNextValue(out, XML_TAG_ACCESS_POINT_NETWORK_TYPE, Integer.valueOf(approvedAccessPoint.networkType));
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_ACCESS_POINT);
    }

    private Map<String, Set<WifiNetworkFactory.AccessPoint>> parseApprovedAccessPointsMap(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Map<String, Set<WifiNetworkFactory.AccessPoint>> approvedAccessPointsMap = new HashMap<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_APPROVED_ACCESS_POINTS_PER_APP, outerTagDepth)) {
            try {
                approvedAccessPointsMap.put((String) XmlUtil.readNextValueWithName(in, XML_TAG_REQUESTOR_PACKAGE_NAME), parseApprovedAccessPoints(in, outerTagDepth + 1));
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to parse network suggestion. Skipping...", e);
            }
        }
        return approvedAccessPointsMap;
    }

    private Set<WifiNetworkFactory.AccessPoint> parseApprovedAccessPoints(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Set<WifiNetworkFactory.AccessPoint> approvedAccessPoints = new HashSet<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_ACCESS_POINT, outerTagDepth)) {
            try {
                approvedAccessPoints.add(parseApprovedAccessPoint(in, outerTagDepth + 1));
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to parse network suggestion. Skipping...", e);
            }
        }
        return approvedAccessPoints;
    }

    private WifiNetworkFactory.AccessPoint parseApprovedAccessPoint(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        String ssid = null;
        MacAddress bssid = null;
        int networkType = -1;
        while (true) {
            char c = 65535;
            if (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                String[] valueName = new String[1];
                Object value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] != null) {
                    String str = valueName[0];
                    int hashCode = str.hashCode();
                    if (hashCode != -272744856) {
                        if (hashCode != 2554747) {
                            if (hashCode == 63507133 && str.equals("BSSID")) {
                                c = 1;
                            }
                        } else if (str.equals("SSID")) {
                            c = 0;
                        }
                    } else if (str.equals(XML_TAG_ACCESS_POINT_NETWORK_TYPE)) {
                        c = 2;
                    }
                    if (c == 0) {
                        ssid = (String) value;
                    } else if (c == 1) {
                        bssid = MacAddress.fromString((String) value);
                    } else if (c == 2) {
                        networkType = ((Integer) value).intValue();
                    } else {
                        throw new XmlPullParserException("Unknown value name found: " + valueName[0]);
                    }
                } else {
                    throw new XmlPullParserException("Missing value name");
                }
            } else if (ssid == null) {
                throw new XmlPullParserException("XML parsing of ssid failed");
            } else if (bssid == null) {
                throw new XmlPullParserException("XML parsing of bssid failed");
            } else if (networkType != -1) {
                return new WifiNetworkFactory.AccessPoint(ssid, bssid, networkType);
            } else {
                throw new XmlPullParserException("XML parsing of network type failed");
            }
        }
    }
}
