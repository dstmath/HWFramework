package com.android.server.wifi.hotspot2;

import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PasspointConfigSharedStoreData implements WifiConfigStore.StoreData {
    private static final String XML_TAG_PROVIDER_INDEX = "ProviderIndex";
    private static final String XML_TAG_SECTION_HEADER_PASSPOINT_CONFIG_DATA = "PasspointConfigData";
    private final DataSource mDataSource;

    public interface DataSource {
        long getProviderIndex();

        void setProviderIndex(long j);
    }

    PasspointConfigSharedStoreData(DataSource dataSource) {
        this.mDataSource = dataSource;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeShareData(out);
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            deserializeShareData(in, outerTagDepth);
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mDataSource.setProviderIndex(0);
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public boolean hasNewDataToSerialize() {
        return true;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public String getName() {
        return XML_TAG_SECTION_HEADER_PASSPOINT_CONFIG_DATA;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public int getStoreFileId() {
        return 0;
    }

    private void serializeShareData(XmlSerializer out) throws XmlPullParserException, IOException {
        XmlUtil.writeNextValue(out, XML_TAG_PROVIDER_INDEX, Long.valueOf(this.mDataSource.getProviderIndex()));
    }

    private void deserializeShareData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                char c = 65535;
                if (str.hashCode() == 682520897 && str.equals(XML_TAG_PROVIDER_INDEX)) {
                    c = 0;
                }
                if (c == 0) {
                    this.mDataSource.setProviderIndex(((Long) value).longValue());
                } else {
                    throw new XmlPullParserException("Unknown value under share store data " + valueName[0]);
                }
            } else {
                throw new XmlPullParserException("Missing value name");
            }
        }
    }
}
