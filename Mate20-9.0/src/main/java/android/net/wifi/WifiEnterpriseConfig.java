package android.net.wifi;

import android.common.HwFrameworkFactory;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.BatteryStats;
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
        public WifiEnterpriseConfig createFromParcel(Parcel in) {
            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                enterpriseConfig.mFields.put(in.readString(), in.readString());
            }
            int unused = enterpriseConfig.mEapMethod = in.readInt();
            int unused2 = enterpriseConfig.mPhase2Method = in.readInt();
            int unused3 = enterpriseConfig.mEapSubId = in.readInt();
            X509Certificate[] unused4 = enterpriseConfig.mCaCerts = ParcelUtil.readCertificates(in);
            PrivateKey unused5 = enterpriseConfig.mClientPrivateKey = ParcelUtil.readPrivateKey(in);
            X509Certificate[] unused6 = enterpriseConfig.mClientCertificateChain = ParcelUtil.readCertificates(in);
            return enterpriseConfig;
        }

        public WifiEnterpriseConfig[] newArray(int size) {
            return new WifiEnterpriseConfig[size];
        }
    };
    public static final String DISABLE_TLS_1_2 = "\"tls_disable_tlsv1_2=1\"";
    public static final String DOM_SUFFIX_MATCH_KEY = "domain_suffix_match";
    public static final String EAP_KEY = "eap";
    public static final String EMPTY_VALUE = "NULL";
    public static final String ENABLE_TLS_1_2 = "\"tls_disable_tlsv1_2=0\"";
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
    public static final String PHASE1_KEY = "phase1";
    public static final String PHASE2_KEY = "phase2";
    public static final String PLMN_KEY = "plmn";
    public static final String PRIVATE_KEY_ID_KEY = "key_id";
    public static final String REALM_KEY = "realm";
    public static final String SUBJECT_MATCH_KEY = "subject_match";
    private static final String[] SUPPLICANT_CONFIG_KEYS = {IDENTITY_KEY, ANON_IDENTITY_KEY, "password", CLIENT_CERT_KEY, CA_CERT_KEY, SUBJECT_MATCH_KEY, "engine", ENGINE_ID_KEY, PRIVATE_KEY_ID_KEY, ALTSUBJECT_MATCH_KEY, DOM_SUFFIX_MATCH_KEY, CA_PATH_KEY};
    private static final String TAG = "WifiEnterpriseConfig";
    private static final List<String> UNQUOTED_KEYS = Arrays.asList(new String[]{"engine", OPP_KEY_CACHING});
    /* access modifiers changed from: private */
    public X509Certificate[] mCaCerts;
    /* access modifiers changed from: private */
    public X509Certificate[] mClientCertificateChain;
    /* access modifiers changed from: private */
    public PrivateKey mClientPrivateKey;
    /* access modifiers changed from: private */
    public int mEapMethod = -1;
    /* access modifiers changed from: private */
    public int mEapSubId = Integer.MAX_VALUE;
    /* access modifiers changed from: private */
    public HashMap<String, String> mFields = new HashMap<>();
    /* access modifiers changed from: private */
    public int mPhase2Method = 0;
    private boolean mTls12Enable = true;

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
        public static final String[] strings = {"PEAP", "TLS", "TTLS", "PWD", "SIM", "AKA", "AKA'", "WFA-UNAUTH-TLS"};

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
        if (source.mCaCerts != null) {
            this.mCaCerts = (X509Certificate[]) Arrays.copyOf(source.mCaCerts, source.mCaCerts.length);
        } else {
            this.mCaCerts = null;
        }
        this.mClientPrivateKey = source.mClientPrivateKey;
        if (source.mClientCertificateChain != null) {
            this.mClientCertificateChain = (X509Certificate[]) Arrays.copyOf(source.mClientCertificateChain, source.mClientCertificateChain.length);
        } else {
            this.mClientCertificateChain = null;
        }
        this.mEapMethod = source.mEapMethod;
        this.mPhase2Method = source.mPhase2Method;
        this.mEapSubId = source.mEapSubId;
    }

    public WifiEnterpriseConfig(WifiEnterpriseConfig source) {
        copyFrom(source, false, "");
    }

    public void copyFromExternal(WifiEnterpriseConfig externalConfig, String mask) {
        copyFrom(externalConfig, true, convertToQuotedString(mask));
    }

    public int describeContents() {
        return 0;
    }

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
    }

    public boolean saveToSupplicant(SupplicantSaver saver) {
        boolean is_autheap = false;
        if (!isEapMethodValid()) {
            return false;
        }
        boolean shouldNotWriteAnonIdentity = this.mEapMethod == 4 || this.mEapMethod == 5 || this.mEapMethod == 6;
        for (String key : this.mFields.keySet()) {
            if ((!shouldNotWriteAnonIdentity || !ANON_IDENTITY_KEY.equals(key)) && !saver.saveValue(key, this.mFields.get(key))) {
                return false;
            }
        }
        if (!saver.saveValue(EAP_KEY, Eap.strings[this.mEapMethod])) {
            return false;
        }
        if (this.mEapMethod != 1 && this.mPhase2Method != 0) {
            if (this.mEapMethod == 2 && this.mPhase2Method == 4) {
                is_autheap = true;
            }
            String prefix = is_autheap ? "autheap=" : "auth=";
            return saver.saveValue(PHASE2_KEY, convertToQuotedString(prefix + Phase2.strings[this.mPhase2Method]));
        } else if (this.mPhase2Method == 0) {
            return saver.saveValue(PHASE2_KEY, null);
        } else {
            Log.e(TAG, "WiFi enterprise configuration is invalid as it supplies a phase 2 method but the phase1 method does not support it.");
            return false;
        }
    }

    public void loadFromSupplicant(SupplicantLoader loader) {
        for (String key : SUPPLICANT_CONFIG_KEYS) {
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
            case 1:
            case 7:
                setPhase2Method(0);
                break;
            default:
                throw new IllegalArgumentException("Unknown EAP method");
        }
        this.mEapMethod = eapMethod;
        setFieldValue(OPP_KEY_CACHING, "1");
    }

    public void setTls12Enable(boolean enable) {
        this.mTls12Enable = enable;
        this.mFields.put(PHASE1_KEY, enable ? ENABLE_TLS_1_2 : DISABLE_TLS_1_2);
    }

    public boolean getTls12Enable() {
        return this.mTls12Enable;
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
        for (byte o : bytes) {
            sb.append(String.format("%02x", new Object[]{Integer.valueOf(o & BatteryStats.HistoryItem.CMD_NULL)}));
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

    public void setCaCertificateAlias(String alias) {
        setFieldValue(CA_CERT_KEY, alias, CA_CERT_PREFIX);
    }

    public void setCaCertificateAliases(String[] aliases) {
        if (aliases == null) {
            setFieldValue(CA_CERT_KEY, null, CA_CERT_PREFIX);
            return;
        }
        if (aliases.length == 1) {
            setCaCertificateAlias(aliases[0]);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aliases.length; i++) {
            if (i > 0) {
                sb.append(CA_CERT_ALIAS_DELIMITER);
            }
            sb.append(encodeCaCertificateAlias(Credentials.CA_CERTIFICATE + aliases[i]));
        }
        setFieldValue(CA_CERT_KEY, sb.toString(), KEYSTORES_URI);
    }

    public String getCaCertificateAlias() {
        return getFieldValue(CA_CERT_KEY, CA_CERT_PREFIX);
    }

    public String[] getCaCertificateAliases() {
        String value = getFieldValue(CA_CERT_KEY);
        if (value.startsWith(CA_CERT_PREFIX)) {
            return new String[]{getFieldValue(CA_CERT_KEY, CA_CERT_PREFIX)};
        }
        String[] strArr = null;
        if (value.startsWith(KEYSTORES_URI)) {
            String[] aliases = TextUtils.split(value.substring(KEYSTORES_URI.length()), CA_CERT_ALIAS_DELIMITER);
            for (int i = 0; i < aliases.length; i++) {
                aliases[i] = decodeCaCertificateAlias(aliases[i]);
                if (aliases[i].startsWith(Credentials.CA_CERTIFICATE)) {
                    aliases[i] = aliases[i].substring(Credentials.CA_CERTIFICATE.length());
                }
            }
            if (aliases.length != 0) {
                strArr = aliases;
            }
            return strArr;
        }
        if (!TextUtils.isEmpty(value)) {
            strArr = new String[]{value};
        }
        return strArr;
    }

    public void setCaCertificate(X509Certificate cert) {
        if (cert == null) {
            this.mCaCerts = null;
        } else if (cert.getBasicConstraints() >= 0) {
            this.mCaCerts = new X509Certificate[]{cert};
        } else {
            throw new IllegalArgumentException("Not a CA certificate");
        }
    }

    public X509Certificate getCaCertificate() {
        if (this.mCaCerts == null || this.mCaCerts.length <= 0) {
            return null;
        }
        return this.mCaCerts[0];
    }

    public void setCaCertificates(X509Certificate[] certs) {
        if (certs != null) {
            X509Certificate[] newCerts = new X509Certificate[certs.length];
            int i = 0;
            while (i < certs.length) {
                if (certs[i].getBasicConstraints() >= 0) {
                    newCerts[i] = certs[i];
                    i++;
                } else {
                    throw new IllegalArgumentException("Not a CA certificate");
                }
            }
            this.mCaCerts = newCerts;
            return;
        }
        this.mCaCerts = null;
    }

    public X509Certificate[] getCaCertificates() {
        if (this.mCaCerts == null || this.mCaCerts.length <= 0) {
            return null;
        }
        return this.mCaCerts;
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

    /* JADX WARNING: type inference failed for: r1v8, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void setClientKeyEntryWithCertificateChain(PrivateKey privateKey, X509Certificate[] clientCertificateChain) {
        X509Certificate[] newCerts = null;
        if (clientCertificateChain != null && clientCertificateChain.length > 0) {
            if (clientCertificateChain[0].getBasicConstraints() == -1) {
                int i = 1;
                while (i < clientCertificateChain.length) {
                    if (clientCertificateChain[i].getBasicConstraints() != -1) {
                        i++;
                    } else {
                        throw new IllegalArgumentException("All certificates following the first must be CA certificates");
                    }
                }
                newCerts = Arrays.copyOf(clientCertificateChain, clientCertificateChain.length);
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
    }

    public X509Certificate getClientCertificate() {
        if (this.mClientCertificateChain == null || this.mClientCertificateChain.length <= 0) {
            return null;
        }
        return this.mClientCertificateChain[0];
    }

    public X509Certificate[] getClientCertificateChain() {
        if (this.mClientCertificateChain == null || this.mClientCertificateChain.length <= 0) {
            return null;
        }
        return this.mClientCertificateChain;
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
            return current != null ? current.getKeyId(null) : EMPTY_VALUE;
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
        return sb.toString();
    }

    private boolean isEapMethodValid() {
        if (this.mEapMethod == -1) {
            Log.e(TAG, "WiFi enterprise configuration is invalid as it supplies no EAP method.");
            return false;
        } else if (this.mEapMethod < 0 || this.mEapMethod >= Eap.strings.length) {
            Log.e(TAG, "mEapMethod is invald for WiFi enterprise configuration: " + this.mEapMethod);
            return false;
        } else if (this.mPhase2Method >= 0 && this.mPhase2Method < Phase2.strings.length) {
            return true;
        } else {
            Log.e(TAG, "mPhase2Method is invald for WiFi enterprise configuration: " + this.mPhase2Method);
            return false;
        }
    }

    public void setEapSubId(int subId) {
        if (isSubIdValid(subId)) {
            switch (this.mEapMethod) {
                case 4:
                case 5:
                case 6:
                    this.mEapSubId = subId;
                    Log.d(TAG, "setEapSubId: Eap Method is " + this.mEapMethod + ", subId is " + subId);
                    return;
                default:
                    Log.e(TAG, "Not applicable EAP method for multi cards");
                    return;
            }
        } else {
            throw new IllegalArgumentException("Unknown subId");
        }
    }

    public int getEapSubId() {
        return this.mEapSubId;
    }

    private boolean isSubIdValid(int subId) {
        if (subId == Integer.MAX_VALUE || subId == 0 || subId == 1) {
            return true;
        }
        return false;
    }
}
