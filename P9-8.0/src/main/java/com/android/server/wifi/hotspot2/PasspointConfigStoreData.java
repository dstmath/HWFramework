package com.android.server.wifi.hotspot2;

import android.net.wifi.hotspot2.PasspointConfiguration;
import android.text.TextUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiConfigStore.StoreData;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PasspointConfigStoreData implements StoreData {
    private static final String XML_TAG_CA_CERTIFICATE_ALIAS = "CaCertificateAlias";
    private static final String XML_TAG_CLIENT_CERTIFICATE_ALIAS = "ClientCertificateAlias";
    private static final String XML_TAG_CLIENT_PRIVATE_KEY_ALIAS = "ClientPrivateKeyAlias";
    private static final String XML_TAG_CREATOR_UID = "CreatorUID";
    private static final String XML_TAG_PROVIDER_ID = "ProviderID";
    private static final String XML_TAG_PROVIDER_INDEX = "ProviderIndex";
    private static final String XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION = "Configuration";
    private static final String XML_TAG_SECTION_HEADER_PASSPOINT_CONFIG_DATA = "PasspointConfigData";
    private static final String XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER = "Provider";
    protected static final String XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST = "ProviderList";
    private final DataSource mDataSource;
    private final WifiKeyStore mKeyStore;
    private final SIMAccessor mSimAccessor;

    public interface DataSource {
        long getProviderIndex();

        List<PasspointProvider> getProviders();

        void setProviderIndex(long j);

        void setProviders(List<PasspointProvider> list);
    }

    PasspointConfigStoreData(WifiKeyStore keyStore, SIMAccessor simAccessor, DataSource dataSource) {
        this.mKeyStore = keyStore;
        this.mSimAccessor = simAccessor;
        this.mDataSource = dataSource;
    }

    public void serializeData(XmlSerializer out, boolean shared) throws XmlPullParserException, IOException {
        if (shared) {
            serializeShareData(out);
        } else {
            serializeUserData(out);
        }
    }

    public void deserializeData(XmlPullParser in, int outerTagDepth, boolean shared) throws XmlPullParserException, IOException {
        if (shared) {
            deserializeShareData(in, outerTagDepth);
        } else {
            deserializeUserData(in, outerTagDepth);
        }
    }

    public void resetData(boolean shared) {
        if (shared) {
            resetShareData();
        } else {
            resetUserData();
        }
    }

    public String getName() {
        return XML_TAG_SECTION_HEADER_PASSPOINT_CONFIG_DATA;
    }

    public boolean supportShareData() {
        return true;
    }

    private void serializeShareData(XmlSerializer out) throws XmlPullParserException, IOException {
        XmlUtil.writeNextValue(out, XML_TAG_PROVIDER_INDEX, Long.valueOf(this.mDataSource.getProviderIndex()));
    }

    private void serializeUserData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeProviderList(out, this.mDataSource.getProviders());
    }

    private void serializeProviderList(XmlSerializer out, List<PasspointProvider> providerList) throws XmlPullParserException, IOException {
        if (providerList != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST);
            for (PasspointProvider provider : providerList) {
                serializeProvider(out, provider);
            }
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST);
        }
    }

    private void serializeProvider(XmlSerializer out, PasspointProvider provider) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER);
        XmlUtil.writeNextValue(out, XML_TAG_PROVIDER_ID, Long.valueOf(provider.getProviderId()));
        XmlUtil.writeNextValue(out, XML_TAG_CREATOR_UID, Integer.valueOf(provider.getCreatorUid()));
        XmlUtil.writeNextValue(out, XML_TAG_CA_CERTIFICATE_ALIAS, provider.getCaCertificateAlias());
        XmlUtil.writeNextValue(out, XML_TAG_CLIENT_CERTIFICATE_ALIAS, provider.getClientCertificateAlias());
        XmlUtil.writeNextValue(out, XML_TAG_CLIENT_PRIVATE_KEY_ALIAS, provider.getClientPrivateKeyAlias());
        if (provider.getConfig() != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION);
            PasspointXmlUtils.serializePasspointConfiguration(out, provider.getConfig());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION);
        }
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER);
    }

    private void deserializeShareData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] == null) {
                throw new XmlPullParserException("Missing value name");
            } else if (valueName[0].equals(XML_TAG_PROVIDER_INDEX)) {
                this.mDataSource.setProviderIndex(((Long) value).longValue());
            } else {
                throw new XmlPullParserException("Unknown value under share store data " + valueName[0]);
            }
        }
    }

    private void deserializeUserData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            if (headerName[0].equals(XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST)) {
                this.mDataSource.setProviders(deserializeProviderList(in, outerTagDepth + 1));
            } else {
                throw new XmlPullParserException("Unknown Passpoint user store data " + headerName[0]);
            }
        }
    }

    protected List<PasspointProvider> deserializeProviderList(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        List<PasspointProvider> providerList = new ArrayList();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER, outerTagDepth)) {
            providerList.add(deserializeProvider(in, outerTagDepth + 1));
        }
        return providerList;
    }

    private PasspointProvider deserializeProvider(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        long providerId = Long.MIN_VALUE;
        int creatorUid = Integer.MIN_VALUE;
        String caCertificateAlias = null;
        String clientCertificateAlias = null;
        String clientPrivateKeyAlias = null;
        PasspointConfiguration config = null;
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            if (in.getAttributeValue(null, "name") != null) {
                String[] name = new String[1];
                String value = XmlUtil.readCurrentValue(in, name);
                String str = name[0];
                if (str.equals(XML_TAG_PROVIDER_ID)) {
                    providerId = ((Long) value).longValue();
                } else if (str.equals(XML_TAG_CREATOR_UID)) {
                    creatorUid = ((Integer) value).intValue();
                } else if (str.equals(XML_TAG_CA_CERTIFICATE_ALIAS)) {
                    caCertificateAlias = value;
                } else if (str.equals(XML_TAG_CLIENT_CERTIFICATE_ALIAS)) {
                    clientCertificateAlias = value;
                } else if (str.equals(XML_TAG_CLIENT_PRIVATE_KEY_ALIAS)) {
                    clientPrivateKeyAlias = value;
                }
            } else if (TextUtils.equals(in.getName(), XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION)) {
                config = PasspointXmlUtils.deserializePasspointConfiguration(in, outerTagDepth + 1);
            } else {
                throw new XmlPullParserException("Unexpected section under Provider: " + in.getName());
            }
        }
        if (providerId == Long.MIN_VALUE) {
            throw new XmlPullParserException("Missing provider ID");
        } else if (config != null) {
            return new PasspointProvider(config, this.mKeyStore, this.mSimAccessor, providerId, creatorUid, caCertificateAlias, clientCertificateAlias, clientPrivateKeyAlias);
        } else {
            throw new XmlPullParserException("Missing Passpoint configuration");
        }
    }

    private void resetShareData() {
        this.mDataSource.setProviderIndex(0);
    }

    private void resetUserData() {
        this.mDataSource.setProviders(new ArrayList());
    }
}
