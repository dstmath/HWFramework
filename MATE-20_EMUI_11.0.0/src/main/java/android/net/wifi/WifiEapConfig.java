package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;

public class WifiEapConfig implements Parcelable {
    public static final Parcelable.Creator<WifiEapConfig> CREATOR = new Parcelable.Creator<WifiEapConfig>() {
        /* class android.net.wifi.WifiEapConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiEapConfig createFromParcel(Parcel in) {
            WifiEapConfig config = new WifiEapConfig();
            config.eapMethod = (EapMethod) WifiEapConfig.convertEnum(EapMethod.class, in.readString(), EapMethod.NONE);
            config.phase2Method = (Phase2Method) WifiEapConfig.convertEnum(Phase2Method.class, in.readString(), Phase2Method.NONE);
            config.identity = in.readString();
            config.anonymousIdentity = in.readString();
            config.password = in.readString();
            config.caCertAliases = in.readStringArray();
            if (config.caCertAliases != null && config.caCertAliases.length == 0) {
                config.caCertAliases = null;
            }
            config.caPath = in.readString();
            config.clientCertAlias = in.readString();
            config.altSubjectMatch = in.readString();
            config.domainSuffixMatch = in.readString();
            config.realm = in.readString();
            config.plmn = in.readString();
            config.eapSubId = in.readInt();
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public WifiEapConfig[] newArray(int size) {
            return new WifiEapConfig[size];
        }
    };
    private static final String LINE = System.lineSeparator();
    private static final String TAG = "WifiEapConfig";
    private String altSubjectMatch;
    private String anonymousIdentity;
    private String[] caCertAliases;
    private String caPath;
    private String clientCertAlias;
    private String domainSuffixMatch;
    private EapMethod eapMethod = EapMethod.NONE;
    private int eapSubId = Integer.MAX_VALUE;
    private String identity;
    private String password;
    private Phase2Method phase2Method = Phase2Method.NONE;
    private String plmn;
    private String realm;

    /* access modifiers changed from: private */
    public enum EapMethod {
        NONE,
        PEAP,
        TLS,
        TTLS,
        PWD,
        SIM,
        AKA,
        AKA_PRIME,
        UNAUTH_TLS
    }

    /* access modifiers changed from: private */
    public enum Phase2Method {
        NONE,
        PAP,
        MSCHAP,
        MSCHAPV2,
        GTC,
        SIM,
        AKA,
        AKA_PRIME
    }

    public WifiEapConfig() {
    }

    public WifiEapConfig(WifiEapConfig source) {
        if (source != null) {
            this.eapMethod = source.eapMethod;
            this.phase2Method = source.phase2Method;
            this.identity = source.identity;
            this.anonymousIdentity = source.anonymousIdentity;
            this.password = source.password;
            String[] strArr = source.caCertAliases;
            if (strArr != null) {
                this.caCertAliases = (String[]) Arrays.copyOf(strArr, strArr.length);
            } else {
                this.caCertAliases = null;
            }
            this.caPath = source.caPath;
            this.clientCertAlias = source.clientCertAlias;
            this.altSubjectMatch = source.altSubjectMatch;
            this.domainSuffixMatch = source.domainSuffixMatch;
            this.realm = source.realm;
            this.plmn = source.plmn;
            this.eapSubId = source.eapSubId;
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.eapMethod.name());
        dest.writeString(this.phase2Method.name());
        dest.writeString(this.identity);
        dest.writeString(this.anonymousIdentity);
        dest.writeString(this.password);
        dest.writeStringArray(this.caCertAliases);
        dest.writeString(this.caPath);
        dest.writeString(this.clientCertAlias);
        dest.writeString(this.altSubjectMatch);
        dest.writeString(this.domainSuffixMatch);
        dest.writeString(this.realm);
        dest.writeString(this.plmn);
        dest.writeInt(this.eapSubId);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static WifiEapConfig fromWifiEnterpriseConfig(WifiEnterpriseConfig config) {
        WifiEapConfig eapConfig = new WifiEapConfig();
        if (config != null) {
            eapConfig.eapMethod = methodIntToEnum(config.getEapMethod());
            eapConfig.phase2Method = phase2MethodIntToEnum(config.getPhase2Method());
            eapConfig.identity = config.getIdentity();
            eapConfig.anonymousIdentity = config.getIdentity();
            eapConfig.password = config.getPassword();
            String[] caCertAliases2 = config.getCaCertificateAliases();
            if (caCertAliases2 != null && caCertAliases2.length != 0) {
                String[] caCertAliases3 = (String[]) Arrays.copyOf(caCertAliases2, caCertAliases2.length);
            }
            eapConfig.caPath = config.getCaPath();
            eapConfig.clientCertAlias = config.getClientCertificateAlias();
            eapConfig.altSubjectMatch = config.getAltSubjectMatch();
            eapConfig.domainSuffixMatch = config.getDomainSuffixMatch();
            eapConfig.realm = config.getRealm();
            eapConfig.plmn = config.getPlmn();
            eapConfig.eapSubId = config.getEapSubId();
        }
        return eapConfig;
    }

    public WifiEnterpriseConfig toWifiEnterpriseConfig() {
        WifiEnterpriseConfig config = new WifiEnterpriseConfig();
        try {
            config.setEapMethod(methodEnum2Int(this.eapMethod));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Failed to set eap method");
        }
        try {
            config.setPhase2Method(phase2MethodEnumToInt(this.phase2Method));
        } catch (IllegalArgumentException e2) {
            Log.w(TAG, "Failed to set phase2 method");
        }
        if (!TextUtils.isEmpty(this.identity)) {
            config.setIdentity(this.identity);
        }
        if (!TextUtils.isEmpty(this.anonymousIdentity)) {
            config.setAnonymousIdentity(this.anonymousIdentity);
        }
        if (!TextUtils.isEmpty(this.password)) {
            config.setPassword(this.password);
        }
        String[] strArr = this.caCertAliases;
        if (!(strArr == null || strArr.length == 0)) {
            config.setCaCertificateAliases(strArr);
        }
        if (!TextUtils.isEmpty(this.caPath)) {
            config.setCaPath(this.caPath);
        }
        if (!TextUtils.isEmpty(this.clientCertAlias)) {
            config.setClientCertificateAlias(this.clientCertAlias);
        }
        if (!TextUtils.isEmpty(this.altSubjectMatch)) {
            config.setAltSubjectMatch(this.altSubjectMatch);
        }
        if (!TextUtils.isEmpty(this.domainSuffixMatch)) {
            config.setDomainSuffixMatch(this.domainSuffixMatch);
        }
        if (!TextUtils.isEmpty(this.realm)) {
            config.setRealm(this.realm);
        }
        if (!TextUtils.isEmpty(this.plmn)) {
            config.setPlmn(this.plmn);
        }
        int i = this.eapSubId;
        if (i != Integer.MAX_VALUE) {
            config.setEapSubId(i);
        }
        return config;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("eap_method: ");
        sb.append(this.eapMethod.name());
        sb.append(LINE);
        sb.append("phase2_method: ");
        sb.append(this.phase2Method.name());
        sb.append(LINE);
        sb.append("identity: ");
        sb.append(this.identity != null ? "*" : "null");
        sb.append(LINE);
        sb.append("anonymousIdentity: ");
        String str = this.anonymousIdentity;
        if (str == null) {
            str = "null";
        }
        sb.append(str);
        sb.append(LINE);
        sb.append("password: ");
        sb.append("*");
        sb.append(LINE);
        sb.append("caCertAliases: ");
        String[] strArr = this.caCertAliases;
        if (strArr == null) {
            strArr = "null";
        }
        sb.append(strArr);
        sb.append(LINE);
        sb.append("caPath: ");
        String str2 = this.caPath;
        if (str2 == null) {
            str2 = "null";
        }
        sb.append(str2);
        sb.append(LINE);
        sb.append("clientCertAlias: ");
        String str3 = this.clientCertAlias;
        if (str3 == null) {
            str3 = "null";
        }
        sb.append(str3);
        sb.append(LINE);
        sb.append("altSubjectMatch: ");
        String str4 = this.altSubjectMatch;
        if (str4 == null) {
            str4 = "null";
        }
        sb.append(str4);
        sb.append(LINE);
        sb.append("domainSuffixMatch: ");
        String str5 = this.domainSuffixMatch;
        if (str5 == null) {
            str5 = "null";
        }
        sb.append(str5);
        sb.append(LINE);
        sb.append("realm: ");
        String str6 = this.realm;
        if (str6 == null) {
            str6 = "null";
        }
        sb.append(str6);
        sb.append(LINE);
        sb.append("plmn: ");
        String str7 = this.plmn;
        if (str7 == null) {
            str7 = "null";
        }
        sb.append(str7);
        sb.append(LINE);
        sb.append("eapSubId: ");
        sb.append(this.eapSubId);
        sb.append(LINE);
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static <T extends Enum<T>> T convertEnum(Class<T> enumType, String enumStr, T defVal) {
        if (enumStr == null || enumStr.isEmpty()) {
            Log.w(TAG, "Read enum string is empty in eap config");
            return defVal;
        }
        try {
            return (T) Enum.valueOf(enumType, enumStr);
        } catch (IllegalArgumentException e) {
            return defVal;
        }
    }

    private static EapMethod methodIntToEnum(int method) {
        switch (method) {
            case -1:
                return EapMethod.NONE;
            case 0:
                return EapMethod.PEAP;
            case 1:
                return EapMethod.TLS;
            case 2:
                return EapMethod.TTLS;
            case 3:
                return EapMethod.PWD;
            case 4:
                return EapMethod.SIM;
            case 5:
                return EapMethod.AKA;
            case 6:
                return EapMethod.AKA_PRIME;
            case 7:
                return EapMethod.UNAUTH_TLS;
            default:
                return EapMethod.NONE;
        }
    }

    private static Phase2Method phase2MethodIntToEnum(int method) {
        switch (method) {
            case 0:
                return Phase2Method.NONE;
            case 1:
                return Phase2Method.PAP;
            case 2:
                return Phase2Method.MSCHAP;
            case 3:
                return Phase2Method.MSCHAPV2;
            case 4:
                return Phase2Method.GTC;
            case 5:
                return Phase2Method.SIM;
            case 6:
                return Phase2Method.AKA;
            case 7:
                return Phase2Method.AKA_PRIME;
            default:
                return Phase2Method.NONE;
        }
    }

    private int methodEnum2Int(EapMethod method) {
        switch (method) {
            case NONE:
                return -1;
            case PEAP:
                return 0;
            case TLS:
                return 1;
            case TTLS:
                return 2;
            case PWD:
                return 3;
            case SIM:
                return 4;
            case AKA:
                return 5;
            case AKA_PRIME:
                return 6;
            case UNAUTH_TLS:
                return 7;
            default:
                return -1;
        }
    }

    private int phase2MethodEnumToInt(Phase2Method method) {
        switch (method) {
            case NONE:
                return 0;
            case PAP:
                return 1;
            case MSCHAP:
                return 2;
            case MSCHAPV2:
                return 3;
            case GTC:
                return 4;
            case SIM:
                return 5;
            case AKA:
                return 6;
            case AKA_PRIME:
                return 7;
            default:
                return 0;
        }
    }
}
