package com.android.server.wifi.hotspot2;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hotspot2.pps.Credential.CertificateCredential;
import android.net.wifi.hotspot2.pps.Credential.SimCredential;
import android.net.wifi.hotspot2.pps.Credential.UserCredential;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.IMSIParameter;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.anqp.DomainNameElement;
import com.android.server.wifi.hotspot2.anqp.NAIRealmElement;
import com.android.server.wifi.hotspot2.anqp.RoamingConsortiumElement;
import com.android.server.wifi.hotspot2.anqp.ThreeGPPNetworkElement;
import com.android.server.wifi.hotspot2.anqp.eap.AuthParam;
import com.android.server.wifi.hotspot2.anqp.eap.NonEAPInnerAuth;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PasspointProvider {
    private static final String ALIAS_HS_TYPE = "HS2_";
    private static final String TAG = "PasspointProvider";
    private final AuthParam mAuthParam;
    private String mCaCertificateAlias;
    private String mClientCertificateAlias;
    private String mClientPrivateKeyAlias;
    private final PasspointConfiguration mConfig;
    private final int mCreatorUid;
    private final int mEAPMethodID;
    private final IMSIParameter mImsiParameter;
    private final WifiKeyStore mKeyStore;
    private final List<String> mMatchingSIMImsiList;
    private final long mProviderId;

    public PasspointProvider(PasspointConfiguration config, WifiKeyStore keyStore, SIMAccessor simAccessor, long providerId, int creatorUid) {
        this(config, keyStore, simAccessor, providerId, creatorUid, null, null, null);
    }

    public PasspointProvider(PasspointConfiguration config, WifiKeyStore keyStore, SIMAccessor simAccessor, long providerId, int creatorUid, String caCertificateAlias, String clientCertificateAlias, String clientPrivateKeyAlias) {
        this.mConfig = new PasspointConfiguration(config);
        this.mKeyStore = keyStore;
        this.mProviderId = providerId;
        this.mCreatorUid = creatorUid;
        this.mCaCertificateAlias = caCertificateAlias;
        this.mClientCertificateAlias = clientCertificateAlias;
        this.mClientPrivateKeyAlias = clientPrivateKeyAlias;
        if (this.mConfig.getCredential().getUserCredential() != null) {
            this.mEAPMethodID = 21;
            this.mAuthParam = new NonEAPInnerAuth(NonEAPInnerAuth.getAuthTypeID(this.mConfig.getCredential().getUserCredential().getNonEapInnerMethod()));
            this.mImsiParameter = null;
            this.mMatchingSIMImsiList = null;
        } else if (this.mConfig.getCredential().getCertCredential() != null) {
            this.mEAPMethodID = 13;
            this.mAuthParam = null;
            this.mImsiParameter = null;
            this.mMatchingSIMImsiList = null;
        } else {
            this.mEAPMethodID = this.mConfig.getCredential().getSimCredential().getEapType();
            this.mAuthParam = null;
            this.mImsiParameter = IMSIParameter.build(this.mConfig.getCredential().getSimCredential().getImsi());
            this.mMatchingSIMImsiList = simAccessor.getMatchingImsis(this.mImsiParameter);
        }
    }

    public PasspointConfiguration getConfig() {
        return new PasspointConfiguration(this.mConfig);
    }

    public String getCaCertificateAlias() {
        return this.mCaCertificateAlias;
    }

    public String getClientPrivateKeyAlias() {
        return this.mClientPrivateKeyAlias;
    }

    public String getClientCertificateAlias() {
        return this.mClientCertificateAlias;
    }

    public long getProviderId() {
        return this.mProviderId;
    }

    public int getCreatorUid() {
        return this.mCreatorUid;
    }

    public boolean installCertsAndKeys() {
        if (this.mConfig.getCredential().getCaCertificate() != null) {
            if (this.mKeyStore.putCertInKeyStore("CACERT_HS2_" + this.mProviderId, this.mConfig.getCredential().getCaCertificate())) {
                this.mCaCertificateAlias = ALIAS_HS_TYPE + this.mProviderId;
            } else {
                Log.e(TAG, "Failed to install CA Certificate");
                uninstallCertsAndKeys();
                return false;
            }
        }
        if (this.mConfig.getCredential().getClientPrivateKey() != null) {
            if (this.mKeyStore.putKeyInKeyStore("USRPKEY_HS2_" + this.mProviderId, this.mConfig.getCredential().getClientPrivateKey())) {
                this.mClientPrivateKeyAlias = ALIAS_HS_TYPE + this.mProviderId;
            } else {
                Log.e(TAG, "Failed to install client private key");
                uninstallCertsAndKeys();
                return false;
            }
        }
        if (this.mConfig.getCredential().getClientCertificateChain() != null) {
            X509Certificate clientCert = getClientCertificate(this.mConfig.getCredential().getClientCertificateChain(), this.mConfig.getCredential().getCertCredential().getCertSha256Fingerprint());
            if (clientCert == null) {
                Log.e(TAG, "Failed to locate client certificate");
                uninstallCertsAndKeys();
                return false;
            }
            if (this.mKeyStore.putCertInKeyStore("USRCERT_HS2_" + this.mProviderId, clientCert)) {
                this.mClientCertificateAlias = ALIAS_HS_TYPE + this.mProviderId;
            } else {
                Log.e(TAG, "Failed to install client certificate");
                uninstallCertsAndKeys();
                return false;
            }
        }
        this.mConfig.getCredential().setCaCertificate(null);
        this.mConfig.getCredential().setClientPrivateKey(null);
        this.mConfig.getCredential().setClientCertificateChain(null);
        return true;
    }

    public void uninstallCertsAndKeys() {
        if (this.mCaCertificateAlias != null) {
            if (!this.mKeyStore.removeEntryFromKeyStore("CACERT_" + this.mCaCertificateAlias)) {
                Log.e(TAG, "Failed to remove entry: " + this.mCaCertificateAlias);
            }
            this.mCaCertificateAlias = null;
        }
        if (this.mClientPrivateKeyAlias != null) {
            if (!this.mKeyStore.removeEntryFromKeyStore("USRPKEY_" + this.mClientPrivateKeyAlias)) {
                Log.e(TAG, "Failed to remove entry: " + this.mClientPrivateKeyAlias);
            }
            this.mClientPrivateKeyAlias = null;
        }
        if (this.mClientCertificateAlias != null) {
            if (!this.mKeyStore.removeEntryFromKeyStore("USRCERT_" + this.mClientCertificateAlias)) {
                Log.e(TAG, "Failed to remove entry: " + this.mClientCertificateAlias);
            }
            this.mClientCertificateAlias = null;
        }
    }

    public PasspointMatch match(Map<ANQPElementType, ANQPElement> anqpElements) {
        PasspointMatch providerMatch = matchProvider(anqpElements);
        int authMatch = ANQPMatcher.matchNAIRealm((NAIRealmElement) anqpElements.get(ANQPElementType.ANQPNAIRealm), this.mConfig.getCredential().getRealm(), this.mEAPMethodID, this.mAuthParam);
        if (authMatch == -1) {
            return PasspointMatch.None;
        }
        if ((authMatch & 4) == 0) {
            return providerMatch;
        }
        if (providerMatch == PasspointMatch.None) {
            providerMatch = PasspointMatch.RoamingProvider;
        }
        return providerMatch;
    }

    public WifiConfiguration getWifiConfig() {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.FQDN = this.mConfig.getHomeSp().getFqdn();
        if (this.mConfig.getHomeSp().getRoamingConsortiumOis() != null) {
            wifiConfig.roamingConsortiumIds = Arrays.copyOf(this.mConfig.getHomeSp().getRoamingConsortiumOis(), this.mConfig.getHomeSp().getRoamingConsortiumOis().length);
        }
        wifiConfig.providerFriendlyName = this.mConfig.getHomeSp().getFriendlyName();
        wifiConfig.allowedKeyManagement.set(2);
        wifiConfig.allowedKeyManagement.set(3);
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setRealm(this.mConfig.getCredential().getRealm());
        enterpriseConfig.setDomainSuffixMatch(this.mConfig.getHomeSp().getFqdn());
        if (this.mConfig.getCredential().getUserCredential() != null) {
            buildEnterpriseConfigForUserCredential(enterpriseConfig, this.mConfig.getCredential().getUserCredential());
            setAnonymousIdentityToNaiRealm(enterpriseConfig, this.mConfig.getCredential().getRealm());
        } else if (this.mConfig.getCredential().getCertCredential() != null) {
            buildEnterpriseConfigForCertCredential(enterpriseConfig);
            setAnonymousIdentityToNaiRealm(enterpriseConfig, this.mConfig.getCredential().getRealm());
        } else {
            buildEnterpriseConfigForSimCredential(enterpriseConfig, this.mConfig.getCredential().getSimCredential());
        }
        wifiConfig.enterpriseConfig = enterpriseConfig;
        return wifiConfig;
    }

    public static PasspointConfiguration convertFromWifiConfig(WifiConfiguration wifiConfig) {
        PasspointConfiguration passpointConfig = new PasspointConfiguration();
        HomeSp homeSp = new HomeSp();
        if (TextUtils.isEmpty(wifiConfig.FQDN)) {
            Log.e(TAG, "Missing FQDN");
            return null;
        }
        homeSp.setFqdn(wifiConfig.FQDN);
        homeSp.setFriendlyName(wifiConfig.providerFriendlyName);
        if (wifiConfig.roamingConsortiumIds != null) {
            homeSp.setRoamingConsortiumOis(Arrays.copyOf(wifiConfig.roamingConsortiumIds, wifiConfig.roamingConsortiumIds.length));
        }
        passpointConfig.setHomeSp(homeSp);
        Credential credential = new Credential();
        credential.setRealm(wifiConfig.enterpriseConfig.getRealm());
        switch (wifiConfig.enterpriseConfig.getEapMethod()) {
            case 1:
                CertificateCredential certCred = new CertificateCredential();
                certCred.setCertType("x509v3");
                credential.setCertCredential(certCred);
                break;
            case 2:
                credential.setUserCredential(buildUserCredentialFromEnterpriseConfig(wifiConfig.enterpriseConfig));
                break;
            case 4:
                credential.setSimCredential(buildSimCredentialFromEnterpriseConfig(18, wifiConfig.enterpriseConfig));
                break;
            case 5:
                credential.setSimCredential(buildSimCredentialFromEnterpriseConfig(23, wifiConfig.enterpriseConfig));
                break;
            case 6:
                credential.setSimCredential(buildSimCredentialFromEnterpriseConfig(50, wifiConfig.enterpriseConfig));
                break;
            default:
                Log.e(TAG, "Unsupport EAP method: " + wifiConfig.enterpriseConfig.getEapMethod());
                return null;
        }
        if (credential.getUserCredential() == null && credential.getCertCredential() == null && credential.getSimCredential() == null) {
            Log.e(TAG, "Missing credential");
            return null;
        }
        passpointConfig.setCredential(credential);
        return passpointConfig;
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof PasspointProvider)) {
            return false;
        }
        PasspointProvider that = (PasspointProvider) thatObject;
        if (this.mProviderId != that.mProviderId || !TextUtils.equals(this.mCaCertificateAlias, that.mCaCertificateAlias) || !TextUtils.equals(this.mClientCertificateAlias, that.mClientCertificateAlias) || !TextUtils.equals(this.mClientPrivateKeyAlias, that.mClientPrivateKeyAlias)) {
            z = false;
        } else if (this.mConfig != null) {
            z = this.mConfig.equals(that.mConfig);
        } else if (that.mConfig != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Long.valueOf(this.mProviderId), this.mCaCertificateAlias, this.mClientCertificateAlias, this.mClientPrivateKeyAlias, this.mConfig});
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProviderId: ").append(this.mProviderId).append("\n");
        builder.append("CreatorUID: ").append(this.mCreatorUid).append("\n");
        builder.append("Configuration Begin ---\n");
        builder.append(this.mConfig);
        builder.append("Configuration End ---\n");
        return builder.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0026 A:{Splitter: B:3:0x0004, ExcHandler: java.security.cert.CertificateEncodingException (e java.security.cert.CertificateEncodingException)} */
    /* JADX WARNING: Missing block: B:11:0x0027, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static X509Certificate getClientCertificate(X509Certificate[] certChain, byte[] expectedSha256Fingerprint) {
        if (certChain == null) {
            return null;
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            for (X509Certificate certificate : certChain) {
                digester.reset();
                if (Arrays.equals(expectedSha256Fingerprint, digester.digest(certificate.getEncoded()))) {
                    return certificate;
                }
            }
            return null;
        } catch (CertificateEncodingException e) {
        }
    }

    private PasspointMatch matchProvider(Map<ANQPElementType, ANQPElement> anqpElements) {
        if (ANQPMatcher.matchDomainName((DomainNameElement) anqpElements.get(ANQPElementType.ANQPDomName), this.mConfig.getHomeSp().getFqdn(), this.mImsiParameter, this.mMatchingSIMImsiList)) {
            return PasspointMatch.HomeProvider;
        }
        if (ANQPMatcher.matchRoamingConsortium((RoamingConsortiumElement) anqpElements.get(ANQPElementType.ANQPRoamingConsortium), this.mConfig.getHomeSp().getRoamingConsortiumOis())) {
            return PasspointMatch.RoamingProvider;
        }
        if (ANQPMatcher.matchThreeGPPNetwork((ThreeGPPNetworkElement) anqpElements.get(ANQPElementType.ANQP3GPPNetwork), this.mImsiParameter, this.mMatchingSIMImsiList)) {
            return PasspointMatch.RoamingProvider;
        }
        return PasspointMatch.None;
    }

    private void buildEnterpriseConfigForUserCredential(WifiEnterpriseConfig config, UserCredential credential) {
        String decodedPassword = new String(Base64.decode(credential.getPassword(), 0), StandardCharsets.UTF_8);
        config.setEapMethod(2);
        config.setIdentity(credential.getUsername());
        config.setPassword(decodedPassword);
        config.setCaCertificateAlias(this.mCaCertificateAlias);
        int phase2Method = 0;
        String nonEapInnerMethod = credential.getNonEapInnerMethod();
        if (nonEapInnerMethod.equals("PAP")) {
            phase2Method = 1;
        } else if (nonEapInnerMethod.equals("MS-CHAP")) {
            phase2Method = 2;
        } else if (nonEapInnerMethod.equals("MS-CHAP-V2")) {
            phase2Method = 3;
        } else {
            Log.wtf(TAG, "Unsupported Auth: " + credential.getNonEapInnerMethod());
        }
        config.setPhase2Method(phase2Method);
    }

    private void buildEnterpriseConfigForCertCredential(WifiEnterpriseConfig config) {
        config.setEapMethod(1);
        config.setClientCertificateAlias(this.mClientCertificateAlias);
        config.setCaCertificateAlias(this.mCaCertificateAlias);
    }

    private void buildEnterpriseConfigForSimCredential(WifiEnterpriseConfig config, SimCredential credential) {
        int eapMethod = -1;
        switch (credential.getEapType()) {
            case 18:
                eapMethod = 4;
                break;
            case 23:
                eapMethod = 5;
                break;
            case 50:
                eapMethod = 6;
                break;
            default:
                Log.wtf(TAG, "Unsupported EAP Method: " + credential.getEapType());
                break;
        }
        config.setEapMethod(eapMethod);
        config.setPlmn(credential.getImsi());
    }

    private static void setAnonymousIdentityToNaiRealm(WifiEnterpriseConfig config, String realm) {
        config.setAnonymousIdentity("anonymous@" + realm);
    }

    private static UserCredential buildUserCredentialFromEnterpriseConfig(WifiEnterpriseConfig config) {
        UserCredential userCredential = new UserCredential();
        userCredential.setEapType(21);
        if (TextUtils.isEmpty(config.getIdentity())) {
            Log.e(TAG, "Missing username for user credential");
            return null;
        }
        userCredential.setUsername(config.getIdentity());
        if (TextUtils.isEmpty(config.getPassword())) {
            Log.e(TAG, "Missing password for user credential");
            return null;
        }
        userCredential.setPassword(new String(Base64.encode(config.getPassword().getBytes(StandardCharsets.UTF_8), 0), StandardCharsets.UTF_8));
        switch (config.getPhase2Method()) {
            case 1:
                userCredential.setNonEapInnerMethod("PAP");
                break;
            case 2:
                userCredential.setNonEapInnerMethod("MS-CHAP");
                break;
            case 3:
                userCredential.setNonEapInnerMethod("MS-CHAP-V2");
                break;
            default:
                Log.e(TAG, "Unsupported phase2 method for TTLS: " + config.getPhase2Method());
                return null;
        }
        return userCredential;
    }

    private static SimCredential buildSimCredentialFromEnterpriseConfig(int eapType, WifiEnterpriseConfig config) {
        SimCredential simCredential = new SimCredential();
        if (TextUtils.isEmpty(config.getPlmn())) {
            Log.e(TAG, "Missing IMSI for SIM credential");
            return null;
        }
        simCredential.setImsi(config.getPlmn());
        simCredential.setEapType(eapType);
        return simCredential;
    }
}
