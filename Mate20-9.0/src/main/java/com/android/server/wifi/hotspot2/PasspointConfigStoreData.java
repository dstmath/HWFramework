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
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PasspointConfigStoreData implements WifiConfigStore.StoreData {
    private static final String XML_TAG_CA_CERTIFICATE_ALIAS = "CaCertificateAlias";
    private static final String XML_TAG_CLIENT_CERTIFICATE_ALIAS = "ClientCertificateAlias";
    private static final String XML_TAG_CLIENT_PRIVATE_KEY_ALIAS = "ClientPrivateKeyAlias";
    private static final String XML_TAG_CREATOR_UID = "CreatorUID";
    private static final String XML_TAG_HAS_EVER_CONNECTED = "HasEverConnected";
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
        if (in != null) {
            if (shared) {
                deserializeShareData(in, outerTagDepth);
            } else {
                deserializeUserData(in, outerTagDepth);
            }
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
        XmlUtil.writeNextValue(out, "HasEverConnected", Boolean.valueOf(provider.getHasEverConnected()));
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v13, resolved type: java.lang.Long} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v15, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v17, resolved type: java.lang.Boolean} */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004f, code lost:
        if (r14.equals(XML_TAG_CREATOR_UID) != false) goto L_0x0071;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private PasspointProvider deserializeProvider(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser = in;
        String caCertificateAlias = null;
        String clientCertificateAlias = null;
        String clientPrivateKeyAlias = null;
        boolean hasEverConnected = false;
        int creatorUid = Integer.MIN_VALUE;
        long providerId = Long.MIN_VALUE;
        PasspointConfiguration config = null;
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            if (xmlPullParser.getAttributeValue(null, "name") != null) {
                char c = 1;
                String[] name = new String[1];
                Object value = XmlUtil.readCurrentValue(xmlPullParser, name);
                String str = name[0];
                switch (str.hashCode()) {
                    case -2096352532:
                        if (str.equals(XML_TAG_PROVIDER_ID)) {
                            c = 0;
                            break;
                        }
                    case -1882773911:
                        if (str.equals(XML_TAG_CLIENT_PRIVATE_KEY_ALIAS)) {
                            c = 4;
                            break;
                        }
                    case -1529270479:
                        if (str.equals("HasEverConnected")) {
                            c = 5;
                            break;
                        }
                    case -922180444:
                        break;
                    case -603932412:
                        if (str.equals(XML_TAG_CLIENT_CERTIFICATE_ALIAS)) {
                            c = 3;
                            break;
                        }
                    case 801332119:
                        if (str.equals(XML_TAG_CA_CERTIFICATE_ALIAS)) {
                            c = 2;
                            break;
                        }
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        providerId = value.longValue();
                        break;
                    case 1:
                        creatorUid = value.intValue();
                        break;
                    case 2:
                        caCertificateAlias = value;
                        break;
                    case 3:
                        clientCertificateAlias = value;
                        break;
                    case 4:
                        clientPrivateKeyAlias = value;
                        break;
                    case 5:
                        hasEverConnected = value.booleanValue();
                        break;
                }
            } else if (TextUtils.equals(in.getName(), XML_TAG_SECTION_HEADER_PASSPOINT_CONFIGURATION)) {
                config = PasspointXmlUtils.deserializePasspointConfiguration(xmlPullParser, outerTagDepth + 1);
            } else {
                throw new XmlPullParserException("Unexpected section under Provider: " + in.getName());
            }
        }
        if (providerId == Long.MIN_VALUE) {
            throw new XmlPullParserException("Missing provider ID");
        } else if (config != null) {
            PasspointProvider passpointProvider = new PasspointProvider(config, this.mKeyStore, this.mSimAccessor, providerId, creatorUid, caCertificateAlias, clientCertificateAlias, clientPrivateKeyAlias, hasEverConnected, false);
            return passpointProvider;
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
