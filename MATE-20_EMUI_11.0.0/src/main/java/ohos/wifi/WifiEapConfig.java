package ohos.wifi;

import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.wifi.WifiSecurity;

@SystemApi
public final class WifiEapConfig implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiEapConfig");
    private String altSubjectMatch;
    private String anonymousIdentity;
    private String[] caCertAliases = new String[0];
    private String caPath;
    private String clientCertAlias;
    private String domainSuffixMatch;
    private WifiSecurity.EapMethod eapMethod = WifiSecurity.EapMethod.NONE;
    private int eapSubId = Integer.MAX_VALUE;
    private String identity;
    private String password;
    private WifiSecurity.Phase2Method phase2Method = WifiSecurity.Phase2Method.NONE;
    private String plmn;
    private String realm;

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if ((((((((((((parcel.writeString(this.eapMethod.name()) && parcel.writeString(this.phase2Method.name())) && parcel.writeString(this.identity)) && parcel.writeString(this.anonymousIdentity)) && parcel.writeString(this.password)) && parcel.writeStringArray(this.caCertAliases)) && parcel.writeString(this.caPath)) && parcel.writeString(this.clientCertAlias)) && parcel.writeString(this.altSubjectMatch)) && parcel.writeString(this.domainSuffixMatch)) && parcel.writeString(this.realm)) && parcel.writeString(this.plmn)) && parcel.writeInt(this.eapSubId)) {
            return true;
        }
        HiLog.warn(LABEL, "Write eapconfig error", new Object[0]);
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        try {
            this.eapMethod = WifiSecurity.EapMethod.valueOf(parcel.readString());
            this.phase2Method = WifiSecurity.Phase2Method.valueOf(parcel.readString());
            this.identity = parcel.readString();
            this.anonymousIdentity = parcel.readString();
            this.password = parcel.readString();
            this.caCertAliases = parcel.readStringArray();
            this.caPath = parcel.readString();
            this.clientCertAlias = parcel.readString();
            this.altSubjectMatch = parcel.readString();
            this.domainSuffixMatch = parcel.readString();
            this.realm = parcel.readString();
            this.plmn = parcel.readString();
            this.eapSubId = parcel.readInt();
            return true;
        } catch (IllegalArgumentException unused) {
            return false;
        }
    }

    public WifiSecurity.EapMethod getEapMethod() {
        return this.eapMethod;
    }

    public void setEapMethod(WifiSecurity.EapMethod eapMethod2) {
        this.eapMethod = eapMethod2;
    }

    public WifiSecurity.Phase2Method getPhase2Method() {
        return this.phase2Method;
    }

    public void setPhase2Method(WifiSecurity.Phase2Method phase2Method2) {
        this.phase2Method = phase2Method2;
    }

    public String getIdentity() {
        return this.identity;
    }

    public void setIdentity(String str) {
        this.identity = str;
    }

    public String getAnonymousIdentity() {
        return this.anonymousIdentity;
    }

    public void setAnonymousIdentity(String str) {
        this.anonymousIdentity = str;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String str) {
        this.password = str;
    }

    public String getAltSubjectMatch() {
        return this.altSubjectMatch;
    }

    public void setAltSubjectMatch(String str) {
        this.altSubjectMatch = str;
    }

    public String getDomainSuffixMatch() {
        return this.domainSuffixMatch;
    }

    public void setDomainSuffixMatch(String str) {
        this.domainSuffixMatch = str;
    }

    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String str) {
        this.realm = str;
    }

    public String getPlmn() {
        return this.plmn;
    }

    public void setPlmn(String str) {
        this.plmn = str;
    }

    public int getEapSubId() {
        return this.eapSubId;
    }

    public void setEapSubId(int i) {
        this.eapSubId = i;
    }

    public String getCaCertNames() {
        String[] strArr = this.caCertAliases;
        if (strArr != null) {
            return (strArr == null || strArr.length != 0) ? this.caCertAliases[0] : "";
        }
        return "";
    }

    public void setCaCertNames(String str) {
        this.caCertAliases = new String[]{str};
    }

    public String getCaPath() {
        return this.caPath;
    }

    public void setCaPath(String str) {
        this.caPath = str;
    }

    public String getClientCertNames() {
        return this.clientCertAlias;
    }

    public void setClientCertNames(String str) {
        this.clientCertAlias = str;
    }
}
