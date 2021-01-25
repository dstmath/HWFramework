package com.android.server.wifi;

import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DeletedEphemeralSsidsStoreData implements WifiConfigStore.StoreData {
    private static final String XML_TAG_SECTION_HEADER_DELETED_EPHEMERAL_SSID_LIST = "DeletedEphemeralSSIDList";
    private static final String XML_TAG_SSID_LIST = "SSIDList";
    private final Clock mClock;
    private Map<String, Long> mSsidToTimeMap;

    DeletedEphemeralSsidsStoreData(Clock clock) {
        this.mClock = clock;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        Map<String, Long> map = this.mSsidToTimeMap;
        if (map != null) {
            XmlUtil.writeNextValue(out, XML_TAG_SSID_LIST, map);
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                String[] valueName = new String[1];
                Object value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] != null) {
                    String str = valueName[0];
                    char c = 65535;
                    if (str.hashCode() == 1427827385 && str.equals(XML_TAG_SSID_LIST)) {
                        c = 0;
                    }
                    if (c != 0) {
                        throw new XmlPullParserException("Unknown tag under DeletedEphemeralSSIDList: " + valueName[0]);
                    } else if (value instanceof Set) {
                        this.mSsidToTimeMap = new HashMap();
                        for (String ssid : (Set) value) {
                            this.mSsidToTimeMap.put(ssid, Long.valueOf(this.mClock.getWallClockMillis()));
                        }
                    } else if (value instanceof Map) {
                        this.mSsidToTimeMap = (Map) value;
                    }
                } else {
                    throw new XmlPullParserException("Missing value name");
                }
            }
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mSsidToTimeMap = null;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public boolean hasNewDataToSerialize() {
        return true;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public String getName() {
        return XML_TAG_SECTION_HEADER_DELETED_EPHEMERAL_SSID_LIST;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public int getStoreFileId() {
        return 1;
    }

    public Map<String, Long> getSsidToTimeMap() {
        Map<String, Long> map = this.mSsidToTimeMap;
        if (map == null) {
            return new HashMap();
        }
        return map;
    }

    public void setSsidToTimeMap(Map<String, Long> ssidMap) {
        this.mSsidToTimeMap = ssidMap;
    }
}
