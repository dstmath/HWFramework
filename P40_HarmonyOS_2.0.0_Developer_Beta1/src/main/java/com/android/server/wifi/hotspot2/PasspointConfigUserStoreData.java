package com.android.server.wifi.hotspot2;

import android.net.wifi.hotspot2.PasspointConfiguration;
import android.text.TextUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PasspointConfigUserStoreData implements WifiConfigStore.StoreData {
    private static final String XML_TAG_CA_CERTIFICATE_ALIAS = "CaCertificateAlias";
    private static final String XML_TAG_CA_CERTIFICATE_ALIASES = "CaCertificateAliases";
    private static final String XML_TAG_CLIENT_CERTIFICATE_ALIAS = "ClientCertificateAlias";
    private static final String XML_TAG_CLIENT_PRIVATE_KEY_ALIAS = "ClientPrivateKeyAlias";
    private static final String XML_TAG_CREATOR_UID = "CreatorUID";
    private static final String XML_TAG_HAS_EVER_CONNECTED = "HasEverConnected";
    private static final String XML_TAG_PACKAGE_NAME = "PackageName";
    private static final String XML_TAG_PROVIDER_ID = "ProviderID";
    private static final String XML_TAG_REMEDIATION_CA_CERTIFICATE_ALIAS = "RemediationCaCertificateAlias";
    private static final String XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION = "Configuration";
    private static final String XML_TAG_SECTION_HEADER_PASSPOINT_CONFIG_DATA = "PasspointConfigData";
    private static final String XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER = "Provider";
    protected static final String XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST = "ProviderList";
    private final DataSource mDataSource;
    private final WifiKeyStore mKeyStore;
    private final SIMAccessor mSimAccessor;

    public interface DataSource {
        List<PasspointProvider> getProviders();

        void setProviders(List<PasspointProvider> list);
    }

    PasspointConfigUserStoreData(WifiKeyStore keyStore, SIMAccessor simAccessor, DataSource dataSource) {
        this.mKeyStore = keyStore;
        this.mSimAccessor = simAccessor;
        this.mDataSource = dataSource;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeUserData(out);
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            deserializeUserData(in, outerTagDepth);
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mDataSource.setProviders(new ArrayList());
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
        return 1;
    }

    private void serializeUserData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeProviderList(out, this.mDataSource.getProviders());
    }

    private void serializeProviderList(XmlSerializer out, List<PasspointProvider> providerList) throws XmlPullParserException, IOException {
        if (providerList != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST);
            for (PasspointProvider provider : providerList) {
                if (!provider.isEphemeral()) {
                    serializeProvider(out, provider);
                }
            }
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST);
        }
    }

    private void serializeProvider(XmlSerializer out, PasspointProvider provider) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER);
        XmlUtil.writeNextValue(out, XML_TAG_PROVIDER_ID, Long.valueOf(provider.getProviderId()));
        XmlUtil.writeNextValue(out, XML_TAG_CREATOR_UID, Integer.valueOf(provider.getCreatorUid()));
        if (provider.getPackageName() != null) {
            XmlUtil.writeNextValue(out, XML_TAG_PACKAGE_NAME, provider.getPackageName());
        }
        XmlUtil.writeNextValue(out, XML_TAG_CA_CERTIFICATE_ALIASES, provider.getCaCertificateAliases());
        XmlUtil.writeNextValue(out, XML_TAG_CLIENT_CERTIFICATE_ALIAS, provider.getClientCertificateAlias());
        XmlUtil.writeNextValue(out, XML_TAG_CLIENT_PRIVATE_KEY_ALIAS, provider.getClientPrivateKeyAlias());
        XmlUtil.writeNextValue(out, "HasEverConnected", Boolean.valueOf(provider.getHasEverConnected()));
        if (provider.getConfig() != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION);
            PasspointXmlUtils.serializePasspointConfiguration(out, provider.getConfig());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION);
        }
        XmlUtil.writeNextValue(out, XML_TAG_REMEDIATION_CA_CERTIFICATE_ALIAS, provider.getRemediationCaCertificateAlias());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER);
    }

    private void deserializeUserData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            String str = headerName[0];
            char c = 65535;
            if (str.hashCode() == -254992817 && str.equals(XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER_LIST)) {
                c = 0;
            }
            if (c == 0) {
                this.mDataSource.setProviders(deserializeProviderList(in, outerTagDepth + 1));
            } else {
                throw new XmlPullParserException("Unknown Passpoint user store data " + headerName[0]);
            }
        }
    }

    /* access modifiers changed from: protected */
    public List<PasspointProvider> deserializeProviderList(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        List<PasspointProvider> providerList = new ArrayList<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_PASSPOINT_PROVIDER, outerTagDepth)) {
            providerList.add(deserializeProvider(in, outerTagDepth + 1));
        }
        return providerList;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private PasspointProvider deserializeProvider(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        char c;
        long providerId = Long.MIN_VALUE;
        List<String> caCertificateAliases = null;
        String caCertificateAlias = null;
        String clientCertificateAlias = null;
        String clientPrivateKeyAlias = null;
        String remediationCaCertificateAlias = null;
        String packageName = null;
        boolean hasEverConnected = false;
        int creatorUid = Integer.MIN_VALUE;
        PasspointConfiguration config = null;
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            if (in.getAttributeValue(null, "name") != null) {
                String[] name = new String[1];
                Object value = XmlUtil.readCurrentValue(in, name);
                String str = name[0];
                switch (str.hashCode()) {
                    case -2096352532:
                        if (str.equals(XML_TAG_PROVIDER_ID)) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1882773911:
                        if (str.equals(XML_TAG_CLIENT_PRIVATE_KEY_ALIAS)) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1718339631:
                        if (str.equals(XML_TAG_PACKAGE_NAME)) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1709680548:
                        if (str.equals(XML_TAG_REMEDIATION_CA_CERTIFICATE_ALIAS)) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1529270479:
                        if (str.equals("HasEverConnected")) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    case -922180444:
                        if (str.equals(XML_TAG_CREATOR_UID)) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case -603932412:
                        if (str.equals(XML_TAG_CLIENT_CERTIFICATE_ALIAS)) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case 801332119:
                        if (str.equals(XML_TAG_CA_CERTIFICATE_ALIAS)) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1281023621:
                        if (str.equals(XML_TAG_CA_CERTIFICATE_ALIASES)) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        providerId = ((Long) value).longValue();
                        continue;
                    case 1:
                        creatorUid = ((Integer) value).intValue();
                        continue;
                    case 2:
                        packageName = (String) value;
                        continue;
                    case 3:
                        caCertificateAliases = (List) value;
                        continue;
                    case 4:
                        caCertificateAlias = (String) value;
                        continue;
                    case 5:
                        clientCertificateAlias = (String) value;
                        continue;
                    case 6:
                        clientPrivateKeyAlias = (String) value;
                        continue;
                    case 7:
                        remediationCaCertificateAlias = (String) value;
                        continue;
                    case '\b':
                        hasEverConnected = ((Boolean) value).booleanValue();
                        continue;
                }
            } else if (TextUtils.equals(in.getName(), XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION)) {
                config = PasspointXmlUtils.deserializePasspointConfiguration(in, outerTagDepth + 1);
            } else {
                throw new XmlPullParserException("Unexpected section under Provider: " + in.getName());
            }
        }
        if (providerId == Long.MIN_VALUE) {
            throw new XmlPullParserException("Missing provider ID");
        } else if (caCertificateAliases == null || caCertificateAlias == null) {
            if (caCertificateAlias != null) {
                caCertificateAliases = Arrays.asList(caCertificateAlias);
            }
            if (config != null) {
                return new PasspointProvider(config, this.mKeyStore, this.mSimAccessor, providerId, creatorUid, packageName, caCertificateAliases, clientCertificateAlias, clientPrivateKeyAlias, remediationCaCertificateAlias, hasEverConnected, false);
            }
            throw new XmlPullParserException("Missing Passpoint configuration");
        } else {
            throw new XmlPullParserException("Should not have valid entry for caCertificateAliases and caCertificateAlias at the same time");
        }
    }
}
