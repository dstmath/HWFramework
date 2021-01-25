package android.net.wifi;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hwUtil.WifiEnterpriseConfigUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.Credentials;
import android.telecom.Logging.Session;
import android.text.TextUtils;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.conn.ssl.SSLSocketFactory;

public class WifiEnterpriseConfig implements Parcelable {
    public static final String ALTSUBJECT_MATCH_KEY = "altsubject_match";
    public static final String ANON_IDENTITY_KEY = "anonymous_identity";
    public static final String CA_CERT_ALIAS_DELIMITER = " ";
    public static final String CA_CERT_KEY = "ca_cert";
    public static final String CA_CERT_PREFIX = "keystore://CACERT_";
    public static final String CA_PATH_KEY = "ca_path";
    public static final String CLIENT_CERT_KEY = "client_cert";
    public static final String CLIENT_CERT_PREFIX = "keystore://USRCERT_";
    public static final Parcelable.Creator<WifiEnterpriseConfig> CREATOR = new Parcelable.Creator<WifiEnterpriseConfig>() {
        /* class android.net.wifi.WifiEnterpriseConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiEnterpriseConfig createFromParcel(Parcel in) {
            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                enterpriseConfig.mFields.put(in.readString(), in.readString());
            }
            enterpriseConfig.mEapMethod = in.readInt();
            enterpriseConfig.mPhase2Method = in.readInt();
            enterpriseConfig.mEapSubId = in.readInt();
            enterpriseConfig.mCaCerts = ParcelUtil.readCertificates(in);
            enterpriseConfig.mClientPrivateKey = ParcelUtil.readPrivateKey(in);
            enterpriseConfig.mClientCertificateChain = ParcelUtil.readCertificates(in);
            enterpriseConfig.mIsAppInstalledDeviceKeyAndCert = in.readBoolean();
            enterpriseConfig.mIsAppInstalledCaCert = in.readBoolean();
            return enterpriseConfig;
        }

        @Override // android.os.Parcelable.Creator
        public WifiEnterpriseConfig[] newArray(int size) {
            return new WifiEnterpriseConfig[size];
        }
    };
    public static final String DOM_SUFFIX_MATCH_KEY = "domain_suffix_match";
    public static final String EAP_KEY = "eap";
    public static final String EMPTY_VALUE = "NULL";
    public static final String ENGINE_DISABLE = "0";
    public static final String ENGINE_ENABLE = "1";
    public static final String ENGINE_ID_KEY = "engine_id";
    public static final String ENGINE_ID_KEYSTORE = "keystore";
    public static final String ENGINE_KEY = "engine";
    public static final String IDENTITY_KEY = "identity";
    public static final String KEYSTORES_URI = "keystores://";
    public static final String KEYSTORE_URI = "keystore://";
    public static final String OPP_KEY_CACHING = "proactive_key_caching";
    public static final String PASSWORD_KEY = "password";
    public static final String PHASE2_KEY = "phase2";
    public static final String PLMN_KEY = "plmn";
    public static final String PRIVATE_KEY_ID_KEY = "key_id";
    public static final String REALM_KEY = "realm";
    public static final String SUBJECT_MATCH_KEY = "subject_match";
    private static final String[] SUPPLICANT_CONFIG_KEYS = {IDENTITY_KEY, ANON_IDENTITY_KEY, "password", CLIENT_CERT_KEY, CA_CERT_KEY, SUBJECT_MATCH_KEY, "engine", ENGINE_ID_KEY, PRIVATE_KEY_ID_KEY, ALTSUBJECT_MATCH_KEY, DOM_SUFFIX_MATCH_KEY, CA_PATH_KEY};
    private static final String TAG = "WifiEnterpriseConfig";
    private static final List<String> UNQUOTED_KEYS = Arrays.asList("engine", OPP_KEY_CACHING);
    private X509Certificate[] mCaCerts;
    private X509Certificate[] mClientCertificateChain;
    private PrivateKey mClientPrivateKey;
    private int mEapMethod = -1;
    private int mEapSubId = Integer.MAX_VALUE;
    @UnsupportedAppUsage
    private HashMap<String, String> mFields = new HashMap<>();
    private boolean mIsAppInstalledCaCert = false;
    private boolean mIsAppInstalledDeviceKeyAndCert = false;
    private int mPhase2Method = 0;

    public interface SupplicantLoader {
        String loadValue(String str);
    }

    public interface SupplicantSaver {
        boolean saveValue(String str, String str2);
    }

    public WifiEnterpriseConfig() {
    }

    private void copyFrom(WifiEnterpriseConfig source, boolean ignoreMaskedPassword, String mask) {
        for (String key : source.mFields.keySet()) {
            if (!ignoreMaskedPassword || !key.equals("password") || !TextUtils.equals(source.mFields.get(key), mask)) {
                this.mFields.put(key, source.mFields.get(key));
            }
        }
        X509Certificate[] x509CertificateArr = source.mCaCerts;
        if (x509CertificateArr != null) {
            this.mCaCerts = (X509Certificate[]) Arrays.copyOf(x509CertificateArr, x509CertificateArr.length);
        } else {
            this.mCaCerts = null;
        }
        this.mClientPrivateKey = source.mClientPrivateKey;
        X509Certificate[] x509CertificateArr2 = source.mClientCertificateChain;
        if (x509CertificateArr2 != null) {
            this.mClientCertificateChain = (X509Certificate[]) Arrays.copyOf(x509CertificateArr2, x509CertificateArr2.length);
        } else {
            this.mClientCertificateChain = null;
        }
        this.mEapMethod = source.mEapMethod;
        this.mPhase2Method = source.mPhase2Method;
        this.mIsAppInstalledDeviceKeyAndCert = source.mIsAppInstalledDeviceKeyAndCert;
        this.mIsAppInstalledCaCert = source.mIsAppInstalledCaCert;
        this.mEapSubId = source.mEapSubId;
    }

    public WifiEnterpriseConfig(WifiEnterpriseConfig source) {
        copyFrom(source, false, "");
    }

    public void copyFromExternal(WifiEnterpriseConfig externalConfig, String mask) {
        copyFrom(externalConfig, true, convertToQuotedString(mask));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mFields.size());
        for (Map.Entry<String, String> entry : this.mFields.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeInt(this.mEapMethod);
        dest.writeInt(this.mPhase2Method);
        dest.writeInt(this.mEapSubId);
        ParcelUtil.writeCertificates(dest, this.mCaCerts);
        ParcelUtil.writePrivateKey(dest, this.mClientPrivateKey);
        ParcelUtil.writeCertificates(dest, this.mClientCertificateChain);
        dest.writeBoolean(this.mIsAppInstalledDeviceKeyAndCert);
        dest.writeBoolean(this.mIsAppInstalledCaCert);
    }

    public static final class Eap {
        public static final int AKA = 5;
        public static final int AKA_PRIME = 6;
        public static final int NONE = -1;
        public static final int PEAP = 0;
        public static final int PWD = 3;
        public static final int SIM = 4;
        public static final int TLS = 1;
        public static final int TTLS = 2;
        public static final int UNAUTH_TLS = 7;
        public static final String[] strings = {"PEAP", SSLSocketFactory.TLS, "TTLS", "PWD", "SIM", "AKA", "AKA'", "WFA-UNAUTH-TLS"};

        private Eap() {
        }
    }

    public static final class Phase2 {
        public static final int AKA = 6;
        public static final int AKA_PRIME = 7;
        private static final String AUTHEAP_PREFIX = "autheap=";
        private static final String AUTH_PREFIX = "auth=";
        public static final int GTC = 4;
        public static final int MSCHAP = 2;
        public static final int MSCHAPV2 = 3;
        public static final int NONE = 0;
        public static final int PAP = 1;
        public static final int SIM = 5;
        public static final String[] strings = {WifiEnterpriseConfig.EMPTY_VALUE, Credential.UserCredential.AUTH_METHOD_PAP, "MSCHAP", "MSCHAPV2", "GTC", "SIM", "AKA", "AKA'"};

        private Phase2() {
        }
    }

    public boolean saveToSupplicant(SupplicantSaver saver) {
        int i;
        boolean is_autheap = false;
        if (!isEapMethodValid()) {
            return false;
        }
        int i2 = this.mEapMethod;
        boolean shouldNotWriteAnonIdentity = i2 == 4 || i2 == 5 || i2 == 6;
        for (String key : this.mFields.keySet()) {
            if ((!shouldNotWriteAnonIdentity || !ANON_IDENTITY_KEY.equals(key)) && !saver.saveValue(key, this.mFields.get(key))) {
                return false;
            }
        }
        if (!saver.saveValue(EAP_KEY, Eap.strings[this.mEapMethod])) {
            return false;
        }
        int i3 = this.mEapMethod;
        if (i3 != 1 && (i = this.mPhase2Method) != 0) {
            if (i3 == 2 && i == 4) {
                is_autheap = true;
            }
            return saver.saveValue(PHASE2_KEY, convertToQuotedString((is_autheap ? "autheap=" : "auth=") + Phase2.strings[this.mPhase2Method]));
        } else if (this.mPhase2Method == 0) {
            return saver.saveValue(PHASE2_KEY, null);
        } else {
            Log.e(TAG, "WiFi enterprise configuration is invalid as it supplies a phase 2 method but the phase1 method does not support it.");
            return false;
        }
    }

    public void loadFromSupplicant(SupplicantLoader loader) {
        String[] strArr = SUPPLICANT_CONFIG_KEYS;
        for (String key : strArr) {
            String value = loader.loadValue(key);
            if (value == null) {
                this.mFields.put(key, EMPTY_VALUE);
            } else {
                this.mFields.put(key, value);
            }
        }
        this.mEapMethod = getStringIndex(Eap.strings, loader.loadValue(EAP_KEY), -1);
        String phase2Method = removeDoubleQuotes(loader.loadValue(PHASE2_KEY));
        if (phase2Method.startsWith("auth=")) {
            phase2Method = phase2Method.substring("auth=".length());
        } else if (phase2Method.startsWith("autheap=")) {
            phase2Method = phase2Method.substring("autheap=".length());
        }
        this.mPhase2Method = getStringIndex(Phase2.strings, phase2Method, 0);
    }

    public void setEapMethod(int eapMethod) {
        HwFrameworkFactory.getHwInnerWifiManager().setWifiEnterpriseConfigEapMethod(eapMethod, this.mFields);
        switch (eapMethod) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                break;
            default:
                throw new IllegalArgumentException("Unknown EAP method");
            case 1:
            case 7:
                setPhase2Method(0);
                break;
        }
        this.mEapMethod = eapMethod;
        setFieldValue(OPP_KEY_CACHING, "1");
    }

    public int getEapMethod() {
        return this.mEapMethod;
    }

    public void setPhase2Method(int phase2Method) {
        switch (phase2Method) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                this.mPhase2Method = phase2Method;
                return;
            default:
                throw new IllegalArgumentException("Unknown Phase 2 method");
        }
    }

    public int getPhase2Method() {
        return this.mPhase2Method;
    }

    public void setIdentity(String identity) {
        setFieldValue(IDENTITY_KEY, identity, "");
    }

    public String getIdentity() {
        return getFieldValue(IDENTITY_KEY);
    }

    public void setAnonymousIdentity(String anonymousIdentity) {
        setFieldValue(ANON_IDENTITY_KEY, anonymousIdentity);
    }

    public String getAnonymousIdentity() {
        return getFieldValue(ANON_IDENTITY_KEY);
    }

    public void setPassword(String password) {
        setFieldValue("password", password);
    }

    public String getPassword() {
        return getFieldValue("password");
    }

    public static String encodeCaCertificateAlias(String alias) {
        byte[] bytes = alias.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", Integer.valueOf(bytes[i] & 255)));
        }
        return sb.toString();
    }

    public static String decodeCaCertificateAlias(String alias) {
        byte[] data = new byte[(alias.length() >> 1)];
        int n = 0;
        int position = 0;
        while (n < alias.length()) {
            data[position] = (byte) Integer.parseInt(alias.substring(n, n + 2), 16);
            n += 2;
            position++;
        }
        try {
            return new String(data, StandardCharsets.UTF_8);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return alias;
        }
    }

    @UnsupportedAppUsage
    public void setCaCertificateAlias(String alias) {
        setFieldValue(CA_CERT_KEY, alias, CA_CERT_PREFIX);
    }

    public void setCaCertificateAliases(String[] aliases) {
        if (aliases == null) {
            setFieldValue(CA_CERT_KEY, null, CA_CERT_PREFIX);
        } else if (aliases.length == 1) {
            setCaCertificateAlias(aliases[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < aliases.length; i++) {
                if (i > 0) {
                    sb.append(CA_CERT_ALIAS_DELIMITER);
                }
                sb.append(encodeCaCertificateAlias(Credentials.CA_CERTIFICATE + aliases[i]));
            }
            setFieldValue(CA_CERT_KEY, sb.toString(), KEYSTORES_URI);
        }
    }

    @UnsupportedAppUsage
    public String getCaCertificateAlias() {
        return getFieldValue(CA_CERT_KEY, CA_CERT_PREFIX);
    }

    public String[] getCaCertificateAliases() {
        String value = getFieldValue(CA_CERT_KEY);
        if (value.startsWith(CA_CERT_PREFIX)) {
            return new String[]{getFieldValue(CA_CERT_KEY, CA_CERT_PREFIX)};
        }
        if (value.startsWith(KEYSTORES_URI)) {
            String[] aliases = TextUtils.split(value.substring(KEYSTORES_URI.length()), CA_CERT_ALIAS_DELIMITER);
            for (int i = 0; i < aliases.length; i++) {
                aliases[i] = decodeCaCertificateAlias(aliases[i]);
                if (aliases[i].startsWith(Credentials.CA_CERTIFICATE)) {
                    aliases[i] = aliases[i].substring(Credentials.CA_CERTIFICATE.length());
                }
            }
            if (aliases.length != 0) {
                return aliases;
            }
            return null;
        } else if (TextUtils.isEmpty(value)) {
            return null;
        } else {
            return new String[]{value};
        }
    }

    public void setCaCertificate(X509Certificate cert) {
        if (cert == null) {
            this.mCaCerts = null;
        } else if (cert.getBasicConstraints() >= 0) {
            this.mIsAppInstalledCaCert = true;
            this.mCaCerts = new X509Certificate[]{cert};
        } else {
            this.mCaCerts = null;
            throw new IllegalArgumentException("Not a CA certificate");
        }
    }

    public X509Certificate getCaCertificate() {
        X509Certificate[] x509CertificateArr = this.mCaCerts;
        if (x509CertificateArr == null || x509CertificateArr.length <= 0) {
            return null;
        }
        return x509CertificateArr[0];
    }

    public void setCaCertificates(X509Certificate[] certs) {
        if (certs != null) {
            X509Certificate[] newCerts = new X509Certificate[certs.length];
            for (int i = 0; i < certs.length; i++) {
                if (certs[i].getBasicConstraints() >= 0) {
                    newCerts[i] = certs[i];
                } else {
                    this.mCaCerts = null;
                    throw new IllegalArgumentException("Not a CA certificate");
                }
            }
            this.mCaCerts = newCerts;
            this.mIsAppInstalledCaCert = true;
            return;
        }
        this.mCaCerts = null;
    }

    public X509Certificate[] getCaCertificates() {
        X509Certificate[] x509CertificateArr = this.mCaCerts;
        if (x509CertificateArr == null || x509CertificateArr.length <= 0) {
            return null;
        }
        return x509CertificateArr;
    }

    public void resetCaCertificate() {
        this.mCaCerts = null;
    }

    public void setCaPath(String path) {
        setFieldValue(CA_PATH_KEY, path);
    }

    public String getCaPath() {
        return getFieldValue(CA_PATH_KEY);
    }

    @UnsupportedAppUsage
    public void setClientCertificateAlias(String alias) {
        setFieldValue(CLIENT_CERT_KEY, alias, CLIENT_CERT_PREFIX);
        setFieldValue(PRIVATE_KEY_ID_KEY, alias, Credentials.USER_PRIVATE_KEY);
        if (TextUtils.isEmpty(alias)) {
            setFieldValue("engine", ENGINE_DISABLE);
            setFieldValue(ENGINE_ID_KEY, "");
            return;
        }
        setFieldValue("engine", "1");
        setFieldValue(ENGINE_ID_KEY, ENGINE_ID_KEYSTORE);
    }

    @UnsupportedAppUsage
    public String getClientCertificateAlias() {
        return getFieldValue(CLIENT_CERT_KEY, CLIENT_CERT_PREFIX);
    }

    public void setClientKeyEntry(PrivateKey privateKey, X509Certificate clientCertificate) {
        X509Certificate[] clientCertificates = null;
        if (clientCertificate != null) {
            clientCertificates = new X509Certificate[]{clientCertificate};
        }
        setClientKeyEntryWithCertificateChain(privateKey, clientCertificates);
    }

    public void setClientKeyEntryWithCertificateChain(PrivateKey privateKey, X509Certificate[] clientCertificateChain) {
        X509Certificate[] newCerts = null;
        if (clientCertificateChain != null && clientCertificateChain.length > 0) {
            if (clientCertificateChain[0].getBasicConstraints() == -1) {
                for (int i = 1; i < clientCertificateChain.length; i++) {
                    if (clientCertificateChain[i].getBasicConstraints() == -1) {
                        throw new IllegalArgumentException("All certificates following the first must be CA certificates");
                    }
                }
                newCerts = (X509Certificate[]) Arrays.copyOf(clientCertificateChain, clientCertificateChain.length);
                if (privateKey == null) {
                    throw new IllegalArgumentException("Client cert without a private key");
                } else if (privateKey.getEncoded() == null) {
                    throw new IllegalArgumentException("Private key cannot be encoded");
                }
            } else {
                throw new IllegalArgumentException("First certificate in the chain must be a client end certificate");
            }
        }
        this.mClientPrivateKey = privateKey;
        this.mClientCertificateChain = newCerts;
        this.mIsAppInstalledDeviceKeyAndCert = true;
    }

    public X509Certificate getClientCertificate() {
        X509Certificate[] x509CertificateArr = this.mClientCertificateChain;
        if (x509CertificateArr == null || x509CertificateArr.length <= 0) {
            return null;
        }
        return x509CertificateArr[0];
    }

    public X509Certificate[] getClientCertificateChain() {
        X509Certificate[] x509CertificateArr = this.mClientCertificateChain;
        if (x509CertificateArr == null || x509CertificateArr.length <= 0) {
            return null;
        }
        return x509CertificateArr;
    }

    public void resetClientKeyEntry() {
        this.mClientPrivateKey = null;
        this.mClientCertificateChain = null;
    }

    public PrivateKey getClientPrivateKey() {
        return this.mClientPrivateKey;
    }

    public void setSubjectMatch(String subjectMatch) {
        setFieldValue(SUBJECT_MATCH_KEY, subjectMatch);
    }

    public String getSubjectMatch() {
        return getFieldValue(SUBJECT_MATCH_KEY);
    }

    public void setAltSubjectMatch(String altSubjectMatch) {
        setFieldValue(ALTSUBJECT_MATCH_KEY, altSubjectMatch);
    }

    public String getAltSubjectMatch() {
        return getFieldValue(ALTSUBJECT_MATCH_KEY);
    }

    public void setDomainSuffixMatch(String domain) {
        setFieldValue(DOM_SUFFIX_MATCH_KEY, domain);
    }

    public String getDomainSuffixMatch() {
        return getFieldValue(DOM_SUFFIX_MATCH_KEY);
    }

    public void setRealm(String realm) {
        setFieldValue(REALM_KEY, realm);
    }

    public String getRealm() {
        return getFieldValue(REALM_KEY);
    }

    public void setPlmn(String plmn) {
        setFieldValue("plmn", plmn);
    }

    public String getPlmn() {
        return getFieldValue("plmn");
    }

    public String getKeyId(WifiEnterpriseConfig current) {
        if (this.mEapMethod == -1) {
            if (current != null) {
                return current.getKeyId(null);
            }
            return EMPTY_VALUE;
        } else if (!isEapMethodValid()) {
            return EMPTY_VALUE;
        } else {
            return Eap.strings[this.mEapMethod] + Session.SESSION_SEPARATION_CHAR_CHILD + Phase2.strings[this.mPhase2Method];
        }
    }

    private String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    private String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private int getStringIndex(String[] arr, String toBeFound, int defaultIndex) {
        if (TextUtils.isEmpty(toBeFound)) {
            return defaultIndex;
        }
        for (int i = 0; i < arr.length; i++) {
            if (toBeFound.equals(arr[i])) {
                return i;
            }
        }
        return defaultIndex;
    }

    public String getFieldValue(String key, String prefix) {
        String value = this.mFields.get(key);
        if (TextUtils.isEmpty(value) || EMPTY_VALUE.equals(value)) {
            return "";
        }
        String value2 = removeDoubleQuotes(value);
        if (value2.startsWith(prefix)) {
            return value2.substring(prefix.length());
        }
        return value2;
    }

    public String getFieldValue(String key) {
        return getFieldValue(key, "");
    }

    private void setFieldValue(String key, String value, String prefix) {
        String valueToSet;
        if (TextUtils.isEmpty(value)) {
            this.mFields.put(key, EMPTY_VALUE);
            return;
        }
        if (!UNQUOTED_KEYS.contains(key)) {
            valueToSet = convertToQuotedString(prefix + value);
        } else {
            valueToSet = prefix + value;
        }
        this.mFields.put(key, valueToSet);
    }

    public void setFieldValue(String key, String value) {
        setFieldValue(key, value, "");
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String key : this.mFields.keySet()) {
            String value = "password".equals(key) ? "<removed>" : this.mFields.get(key);
            sb.append(key);
            sb.append(CA_CERT_ALIAS_DELIMITER);
            sb.append(value);
            sb.append("\n");
        }
        int i = this.mEapMethod;
        if (i >= 0 && i < Eap.strings.length) {
            sb.append("eap_method: ");
            sb.append(Eap.strings[this.mEapMethod]);
            sb.append("\n");
        }
        int i2 = this.mPhase2Method;
        if (i2 > 0 && i2 < Phase2.strings.length) {
            sb.append("phase2_method: ");
            sb.append(Phase2.strings[this.mPhase2Method]);
            sb.append("\n");
        }
        return sb.toString();
    }

    private boolean isEapMethodValid() {
        int i = this.mEapMethod;
        if (i == -1) {
            Log.e(TAG, "WiFi enterprise configuration is invalid as it supplies no EAP method.");
            return false;
        } else if (i < 0 || i >= Eap.strings.length) {
            Log.e(TAG, "mEapMethod is invald for WiFi enterprise configuration: " + this.mEapMethod);
            return false;
        } else {
            int i2 = this.mPhase2Method;
            if (i2 >= 0 && i2 < Phase2.strings.length) {
                return true;
            }
            Log.e(TAG, "mPhase2Method is invald for WiFi enterprise configuration: " + this.mPhase2Method);
            return false;
        }
    }

    public boolean isAppInstalledDeviceKeyAndCert() {
        return this.mIsAppInstalledDeviceKeyAndCert;
    }

    public boolean isAppInstalledCaCert() {
        return this.mIsAppInstalledCaCert;
    }

    public void setEapSubId(int subId) {
        if (WifiEnterpriseConfigUtil.isEapMethodValid(subId, this.mEapMethod)) {
            this.mEapSubId = subId;
        }
    }

    public int getEapSubId() {
        return this.mEapSubId;
    }
}
