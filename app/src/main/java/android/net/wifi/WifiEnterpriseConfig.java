package android.net.wifi;

import android.common.HwFrameworkFactory;
import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.security.Credentials;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.text.TextUtils;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map.Entry;

public class WifiEnterpriseConfig implements Parcelable {
    public static final String ALTSUBJECT_MATCH_KEY = "altsubject_match";
    public static final String ANON_IDENTITY_KEY = "anonymous_identity";
    public static final String CA_CERT_ALIAS_DELIMITER = " ";
    public static final String CA_CERT_KEY = "ca_cert";
    public static final String CA_CERT_PREFIX = "keystore://CACERT_";
    public static final String CA_PATH_KEY = "ca_path";
    public static final String CLIENT_CERT_KEY = "client_cert";
    public static final String CLIENT_CERT_PREFIX = "keystore://USRCERT_";
    public static final Creator<WifiEnterpriseConfig> CREATOR = null;
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
    private static final String[] SUPPLICANT_CONFIG_KEYS = null;
    private static final String TAG = "WifiEnterpriseConfig";
    private X509Certificate[] mCaCerts;
    private X509Certificate mClientCertificate;
    private PrivateKey mClientPrivateKey;
    private int mEapMethod;
    private HashMap<String, String> mFields;
    private int mPhase2Method;
    private boolean mTls12Enable;

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
        public static final String[] strings = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiEnterpriseConfig.Eap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiEnterpriseConfig.Eap.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiEnterpriseConfig.Eap.<clinit>():void");
        }

        private Eap() {
        }
    }

    public static final class Phase2 {
        private static final String AUTHEAP_PREFIX = "autheap=";
        private static final String AUTH_PREFIX = "auth=";
        public static final int GTC = 4;
        public static final int MSCHAP = 2;
        public static final int MSCHAPV2 = 3;
        public static final int NONE = 0;
        public static final int PAP = 1;
        public static final String[] strings = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiEnterpriseConfig.Phase2.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiEnterpriseConfig.Phase2.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiEnterpriseConfig.Phase2.<clinit>():void");
        }

        private Phase2() {
        }
    }

    public interface SupplicantLoader {
        String loadValue(String str);
    }

    public interface SupplicantSaver {
        boolean saveValue(String str, String str2);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiEnterpriseConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiEnterpriseConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiEnterpriseConfig.<clinit>():void");
    }

    public WifiEnterpriseConfig() {
        this.mFields = new HashMap();
        this.mTls12Enable = true;
        this.mEapMethod = -1;
        this.mPhase2Method = 0;
    }

    public WifiEnterpriseConfig(WifiEnterpriseConfig source) {
        this.mFields = new HashMap();
        this.mTls12Enable = true;
        this.mEapMethod = -1;
        this.mPhase2Method = 0;
        for (String key : source.mFields.keySet()) {
            this.mFields.put(key, (String) source.mFields.get(key));
        }
        this.mEapMethod = source.mEapMethod;
        this.mPhase2Method = source.mPhase2Method;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeInt(this.mFields.size());
        for (Entry<String, String> entry : this.mFields.entrySet()) {
            dest.writeString((String) entry.getKey());
            dest.writeString((String) entry.getValue());
        }
        dest.writeInt(this.mEapMethod);
        dest.writeInt(this.mPhase2Method);
        writeCertificates(dest, this.mCaCerts);
        if (this.mClientPrivateKey != null) {
            String algorithm = this.mClientPrivateKey.getAlgorithm();
            byte[] userKeyBytes = this.mClientPrivateKey.getEncoded();
            dest.writeInt(userKeyBytes.length);
            dest.writeByteArray(userKeyBytes);
            dest.writeString(algorithm);
        } else {
            dest.writeInt(0);
        }
        writeCertificate(dest, this.mClientCertificate);
        if (this.mTls12Enable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
    }

    private void writeCertificates(Parcel dest, X509Certificate[] cert) {
        if (cert == null || cert.length == 0) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(cert.length);
        for (X509Certificate writeCertificate : cert) {
            writeCertificate(dest, writeCertificate);
        }
    }

    private void writeCertificate(Parcel dest, X509Certificate cert) {
        if (cert != null) {
            try {
                byte[] certBytes = cert.getEncoded();
                dest.writeInt(certBytes.length);
                dest.writeByteArray(certBytes);
                return;
            } catch (CertificateEncodingException e) {
                dest.writeInt(0);
                return;
            }
        }
        dest.writeInt(0);
    }

    public boolean saveToSupplicant(SupplicantSaver saver) {
        boolean is_autheap = true;
        if (!isEapMethodValid()) {
            return false;
        }
        for (String key : this.mFields.keySet()) {
            if (!saver.saveValue(key, (String) this.mFields.get(key))) {
                return false;
            }
        }
        if (!saver.saveValue(EAP_KEY, Eap.strings[this.mEapMethod])) {
            return false;
        }
        if (this.mEapMethod != 1 && this.mPhase2Method != 0) {
            if (!(this.mEapMethod == 2 && this.mPhase2Method == 4)) {
                is_autheap = false;
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
            case TextToSpeech.SUCCESS /*0*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                break;
            case AudioState.ROUTE_EARPIECE /*1*/:
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                setPhase2Method(0);
                break;
            default:
                throw new IllegalArgumentException("Unknown EAP method");
        }
        this.mEapMethod = eapMethod;
        this.mFields.put(OPP_KEY_CACHING, ENGINE_ENABLE);
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
            case TextToSpeech.SUCCESS /*0*/:
            case AudioState.ROUTE_EARPIECE /*1*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                this.mPhase2Method = phase2Method;
            default:
                throw new IllegalArgumentException("Unknown Phase 2 method");
        }
    }

    public int getPhase2Method() {
        return this.mPhase2Method;
    }

    public void setIdentity(String identity) {
        setFieldValue(IDENTITY_KEY, identity, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getIdentity() {
        return getFieldValue(IDENTITY_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void setAnonymousIdentity(String anonymousIdentity) {
        setFieldValue(ANON_IDENTITY_KEY, anonymousIdentity, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getAnonymousIdentity() {
        return getFieldValue(ANON_IDENTITY_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void setPassword(String password) {
        setFieldValue(PASSWORD_KEY, password, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getPassword() {
        return getFieldValue(PASSWORD_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public static String encodeCaCertificateAlias(String alias) {
        byte[] bytes = alias.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", new Object[]{Integer.valueOf(bytes[i] & Process.PROC_TERM_MASK)}));
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

    public String getCaCertificateAlias() {
        return getFieldValue(CA_CERT_KEY, CA_CERT_PREFIX);
    }

    public String[] getCaCertificateAliases() {
        String[] strArr = null;
        String value = getFieldValue(CA_CERT_KEY, ProxyInfo.LOCAL_EXCL_LIST);
        if (value.startsWith(CA_CERT_PREFIX)) {
            return new String[]{getFieldValue(CA_CERT_KEY, CA_CERT_PREFIX)};
        } else if (value.startsWith(KEYSTORES_URI)) {
            String[] aliases = TextUtils.split(value.substring(KEYSTORES_URI.length()), CA_CERT_ALIAS_DELIMITER);
            for (int i = 0; i < aliases.length; i++) {
                aliases[i] = decodeCaCertificateAlias(aliases[i]);
                if (aliases[i].startsWith(Credentials.CA_CERTIFICATE)) {
                    aliases[i] = aliases[i].substring(Credentials.CA_CERTIFICATE.length());
                }
            }
            if (aliases.length == 0) {
                aliases = null;
            }
            return aliases;
        } else {
            if (!TextUtils.isEmpty(value)) {
                strArr = new String[]{value};
            }
            return strArr;
        }
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
        return getFieldValue(CA_PATH_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void setClientCertificateAlias(String alias) {
        setFieldValue(CLIENT_CERT_KEY, alias, CLIENT_CERT_PREFIX);
        setFieldValue(PRIVATE_KEY_ID_KEY, alias, Credentials.USER_PRIVATE_KEY);
        if (TextUtils.isEmpty(alias)) {
            this.mFields.put(ENGINE_KEY, ENGINE_DISABLE);
            this.mFields.put(ENGINE_ID_KEY, EMPTY_VALUE);
            return;
        }
        this.mFields.put(ENGINE_KEY, ENGINE_ENABLE);
        this.mFields.put(ENGINE_ID_KEY, convertToQuotedString(ENGINE_ID_KEYSTORE));
    }

    public String getClientCertificateAlias() {
        return getFieldValue(CLIENT_CERT_KEY, CLIENT_CERT_PREFIX);
    }

    public void setClientKeyEntry(PrivateKey privateKey, X509Certificate clientCertificate) {
        if (clientCertificate != null) {
            if (clientCertificate.getBasicConstraints() != -1) {
                throw new IllegalArgumentException("Cannot be a CA certificate");
            } else if (privateKey == null) {
                throw new IllegalArgumentException("Client cert without a private key");
            } else if (privateKey.getEncoded() == null) {
                throw new IllegalArgumentException("Private key cannot be encoded");
            }
        }
        this.mClientPrivateKey = privateKey;
        this.mClientCertificate = clientCertificate;
    }

    public X509Certificate getClientCertificate() {
        return this.mClientCertificate;
    }

    public void resetClientKeyEntry() {
        this.mClientPrivateKey = null;
        this.mClientCertificate = null;
    }

    public PrivateKey getClientPrivateKey() {
        return this.mClientPrivateKey;
    }

    public void setSubjectMatch(String subjectMatch) {
        setFieldValue(SUBJECT_MATCH_KEY, subjectMatch, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getSubjectMatch() {
        return getFieldValue(SUBJECT_MATCH_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void setAltSubjectMatch(String altSubjectMatch) {
        setFieldValue(ALTSUBJECT_MATCH_KEY, altSubjectMatch, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getAltSubjectMatch() {
        return getFieldValue(ALTSUBJECT_MATCH_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void setDomainSuffixMatch(String domain) {
        setFieldValue(DOM_SUFFIX_MATCH_KEY, domain);
    }

    public String getDomainSuffixMatch() {
        return getFieldValue(DOM_SUFFIX_MATCH_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void setRealm(String realm) {
        setFieldValue(REALM_KEY, realm, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getRealm() {
        return getFieldValue(REALM_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void setPlmn(String plmn) {
        setFieldValue(PLMN_KEY, plmn, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getPlmn() {
        return getFieldValue(PLMN_KEY, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getKeyId(WifiEnterpriseConfig current) {
        if (this.mEapMethod == -1) {
            return current != null ? current.getKeyId(null) : EMPTY_VALUE;
        } else if (isEapMethodValid()) {
            return Eap.strings[this.mEapMethod] + "_" + Phase2.strings[this.mPhase2Method];
        } else {
            return EMPTY_VALUE;
        }
    }

    private String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) {
            return ProxyInfo.LOCAL_EXCL_LIST;
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
        String value = (String) this.mFields.get(key);
        if (TextUtils.isEmpty(value) || EMPTY_VALUE.equals(value)) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        value = removeDoubleQuotes(value);
        if (value.startsWith(prefix)) {
            return value.substring(prefix.length());
        }
        return value;
    }

    public void setFieldValue(String key, String value, String prefix) {
        if (TextUtils.isEmpty(value)) {
            this.mFields.put(key, EMPTY_VALUE);
        } else {
            this.mFields.put(key, convertToQuotedString(prefix + value));
        }
    }

    public void setFieldValue(String key, String value) {
        if (TextUtils.isEmpty(value)) {
            this.mFields.put(key, EMPTY_VALUE);
        } else {
            this.mFields.put(key, convertToQuotedString(value));
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String key : this.mFields.keySet()) {
            if (!(PASSWORD_KEY.equals(key) || IDENTITY_KEY.equals(key))) {
                sb.append(key).append(CA_CERT_ALIAS_DELIMITER).append((String) this.mFields.get(key)).append("\n");
            }
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
}
