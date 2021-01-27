package com.android.server.wifi.hotspot2;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.IMSIParameter;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.DomainNameElement;
import com.android.server.wifi.hotspot2.anqp.NAIRealmElement;
import com.android.server.wifi.hotspot2.anqp.RoamingConsortiumElement;
import com.android.server.wifi.hotspot2.anqp.ThreeGPPNetworkElement;
import com.android.server.wifi.hotspot2.anqp.eap.AuthParam;
import com.android.server.wifi.hotspot2.anqp.eap.NonEAPInnerAuth;
import com.android.server.wifi.util.InformationElementUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PasspointProvider {
    private static final String ALIAS_ALIAS_REMEDIATION_TYPE = "REMEDIATION_";
    private static final String ALIAS_HS_TYPE = "HS2_";
    private static final String TAG = "PasspointProvider";
    private final AuthParam mAuthParam;
    private List<String> mCaCertificateAliases;
    private String mClientCertificateAlias;
    private String mClientPrivateKeyAlias;
    private final PasspointConfiguration mConfig;
    private final int mCreatorUid;
    private final int mEAPMethodID;
    private boolean mHasEverConnected;
    private final IMSIParameter mImsiParameter;
    private boolean mIsEphemeral;
    private boolean mIsShared;
    private final WifiKeyStore mKeyStore;
    private final List<String> mMatchingSIMImsiList;
    private final String mPackageName;
    private final long mProviderId;
    private String mRemediationCaCertificateAlias;

    public PasspointProvider(PasspointConfiguration config, WifiKeyStore keyStore, SIMAccessor simAccessor, long providerId, int creatorUid, String packageName) {
        this(config, keyStore, simAccessor, providerId, creatorUid, packageName, null, null, null, null, false, false);
    }

    public PasspointProvider(PasspointConfiguration config, WifiKeyStore keyStore, SIMAccessor simAccessor, long providerId, int creatorUid, String packageName, List<String> caCertificateAliases, String clientCertificateAlias, String clientPrivateKeyAlias, String remediationCaCertificateAlias, boolean hasEverConnected, boolean isShared) {
        this.mIsEphemeral = false;
        this.mConfig = new PasspointConfiguration(config);
        this.mKeyStore = keyStore;
        this.mProviderId = providerId;
        this.mCreatorUid = creatorUid;
        this.mPackageName = packageName;
        this.mCaCertificateAliases = caCertificateAliases;
        this.mClientCertificateAlias = clientCertificateAlias;
        this.mClientPrivateKeyAlias = clientPrivateKeyAlias;
        this.mRemediationCaCertificateAlias = remediationCaCertificateAlias;
        this.mHasEverConnected = hasEverConnected;
        this.mIsShared = isShared;
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

    public List<String> getCaCertificateAliases() {
        return this.mCaCertificateAliases;
    }

    public String getClientPrivateKeyAlias() {
        return this.mClientPrivateKeyAlias;
    }

    public String getClientCertificateAlias() {
        return this.mClientCertificateAlias;
    }

    public String getRemediationCaCertificateAlias() {
        return this.mRemediationCaCertificateAlias;
    }

    public long getProviderId() {
        return this.mProviderId;
    }

    public int getCreatorUid() {
        return this.mCreatorUid;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public boolean getHasEverConnected() {
        return this.mHasEverConnected;
    }

    public void setHasEverConnected(boolean hasEverConnected) {
        this.mHasEverConnected = hasEverConnected;
    }

    public boolean isEphemeral() {
        return this.mIsEphemeral;
    }

    public void setEphemeral(boolean isEphemeral) {
        this.mIsEphemeral = isEphemeral;
    }

    public IMSIParameter getImsiParameter() {
        return this.mImsiParameter;
    }

    public boolean installCertsAndKeys() {
        X509Certificate[] x509Certificates = this.mConfig.getCredential().getCaCertificates();
        if (x509Certificates != null) {
            this.mCaCertificateAliases = new ArrayList();
            for (int i = 0; i < x509Certificates.length; i++) {
                String alias = String.format("%s%s_%d", ALIAS_HS_TYPE, Long.valueOf(this.mProviderId), Integer.valueOf(i));
                WifiKeyStore wifiKeyStore = this.mKeyStore;
                if (!wifiKeyStore.putCertInKeyStore("CACERT_" + alias, x509Certificates[i])) {
                    Log.e(TAG, "Failed to install CA Certificate");
                    uninstallCertsAndKeys();
                    return false;
                }
                this.mCaCertificateAliases.add(alias);
            }
        }
        if (this.mConfig.getCredential().getClientPrivateKey() != null) {
            if (!this.mKeyStore.putKeyInKeyStore("USRPKEY_HS2_" + this.mProviderId, this.mConfig.getCredential().getClientPrivateKey())) {
                Log.e(TAG, "Failed to install client private key");
                uninstallCertsAndKeys();
                return false;
            }
            this.mClientPrivateKeyAlias = ALIAS_HS_TYPE + this.mProviderId;
        }
        if (this.mConfig.getCredential().getClientCertificateChain() != null) {
            X509Certificate clientCert = getClientCertificate(this.mConfig.getCredential().getClientCertificateChain(), this.mConfig.getCredential().getCertCredential().getCertSha256Fingerprint());
            if (clientCert == null) {
                Log.e(TAG, "Failed to locate client certificate");
                uninstallCertsAndKeys();
                return false;
            }
            if (!this.mKeyStore.putCertInKeyStore("USRCERT_HS2_" + this.mProviderId, clientCert)) {
                Log.e(TAG, "Failed to install client certificate");
                uninstallCertsAndKeys();
                return false;
            }
            this.mClientCertificateAlias = ALIAS_HS_TYPE + this.mProviderId;
        }
        if (this.mConfig.getSubscriptionUpdate() != null) {
            X509Certificate certificate = this.mConfig.getSubscriptionUpdate().getCaCertificate();
            if (certificate == null) {
                Log.e(TAG, "Failed to locate CA certificate for remediation");
                uninstallCertsAndKeys();
                return false;
            }
            this.mRemediationCaCertificateAlias = "HS2_REMEDIATION_" + this.mProviderId;
            if (!this.mKeyStore.putCertInKeyStore("CACERT_" + this.mRemediationCaCertificateAlias, certificate)) {
                Log.e(TAG, "Failed to install CA certificate for remediation");
                this.mRemediationCaCertificateAlias = null;
                uninstallCertsAndKeys();
                return false;
            }
        }
        this.mConfig.getCredential().setCaCertificates(null);
        this.mConfig.getCredential().setClientPrivateKey(null);
        this.mConfig.getCredential().setClientCertificateChain(null);
        if (this.mConfig.getSubscriptionUpdate() != null) {
            this.mConfig.getSubscriptionUpdate().setCaCertificate((X509Certificate) null);
        }
        return true;
    }

    public void uninstallCertsAndKeys() {
        List<String> list = this.mCaCertificateAliases;
        if (list != null) {
            for (String certificateAlias : list) {
                WifiKeyStore wifiKeyStore = this.mKeyStore;
                if (!wifiKeyStore.removeEntryFromKeyStore("CACERT_" + certificateAlias)) {
                    Log.e(TAG, "Failed to remove entry: " + certificateAlias);
                }
            }
            this.mCaCertificateAliases = null;
        }
        if (this.mClientPrivateKeyAlias != null) {
            WifiKeyStore wifiKeyStore2 = this.mKeyStore;
            if (!wifiKeyStore2.removeEntryFromKeyStore("USRPKEY_" + this.mClientPrivateKeyAlias)) {
                Log.e(TAG, "Failed to remove entry: " + this.mClientPrivateKeyAlias);
            }
            this.mClientPrivateKeyAlias = null;
        }
        if (this.mClientCertificateAlias != null) {
            WifiKeyStore wifiKeyStore3 = this.mKeyStore;
            if (!wifiKeyStore3.removeEntryFromKeyStore("USRCERT_" + this.mClientCertificateAlias)) {
                Log.e(TAG, "Failed to remove entry: " + this.mClientCertificateAlias);
            }
            this.mClientCertificateAlias = null;
        }
        if (this.mRemediationCaCertificateAlias != null) {
            WifiKeyStore wifiKeyStore4 = this.mKeyStore;
            if (!wifiKeyStore4.removeEntryFromKeyStore("CACERT_" + this.mRemediationCaCertificateAlias)) {
                Log.e(TAG, "Failed to remove entry: " + this.mRemediationCaCertificateAlias);
            }
            this.mRemediationCaCertificateAlias = null;
        }
    }

    public PasspointMatch match(Map<Constants.ANQPElementType, ANQPElement> anqpElements, InformationElementUtil.RoamingConsortium roamingConsortium) {
        PasspointMatch providerMatch = matchProviderExceptFor3GPP(anqpElements, roamingConsortium);
        if (providerMatch == PasspointMatch.None && ANQPMatcher.matchThreeGPPNetwork((ThreeGPPNetworkElement) anqpElements.get(Constants.ANQPElementType.ANQP3GPPNetwork), this.mImsiParameter, this.mMatchingSIMImsiList)) {
            return PasspointMatch.RoamingProvider;
        }
        int authMatch = ANQPMatcher.matchNAIRealm((NAIRealmElement) anqpElements.get(Constants.ANQPElementType.ANQPNAIRealm), this.mConfig.getCredential().getRealm(), this.mEAPMethodID, this.mAuthParam);
        if (authMatch == -1) {
            return PasspointMatch.None;
        }
        if ((authMatch & 4) == 0) {
            return providerMatch;
        }
        return providerMatch == PasspointMatch.None ? PasspointMatch.RoamingProvider : providerMatch;
    }

    public WifiConfiguration getWifiConfig() {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.FQDN = this.mConfig.getHomeSp().getFqdn();
        if (this.mConfig.getHomeSp().getRoamingConsortiumOis() != null) {
            wifiConfig.roamingConsortiumIds = Arrays.copyOf(this.mConfig.getHomeSp().getRoamingConsortiumOis(), this.mConfig.getHomeSp().getRoamingConsortiumOis().length);
        }
        if (this.mConfig.getUpdateIdentifier() != Integer.MIN_VALUE) {
            wifiConfig.updateIdentifier = Integer.toString(this.mConfig.getUpdateIdentifier());
            if (isMeteredNetwork(this.mConfig)) {
                wifiConfig.meteredOverride = 1;
            }
        }
        wifiConfig.providerFriendlyName = this.mConfig.getHomeSp().getFriendlyName();
        wifiConfig.allowedKeyManagement.set(2);
        wifiConfig.allowedKeyManagement.set(3);
        wifiConfig.allowedProtocols.set(1);
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
        wifiConfig.shared = this.mIsShared;
        return wifiConfig;
    }

    public boolean isSimCredential() {
        return this.mConfig.getCredential().getSimCredential() != null;
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
        int eapMethod = wifiConfig.enterpriseConfig.getEapMethod();
        if (eapMethod == 1) {
            Credential.CertificateCredential certCred = new Credential.CertificateCredential();
            certCred.setCertType("x509v3");
            credential.setCertCredential(certCred);
        } else if (eapMethod == 2) {
            credential.setUserCredential(buildUserCredentialFromEnterpriseConfig(wifiConfig.enterpriseConfig));
        } else if (eapMethod == 4) {
            credential.setSimCredential(buildSimCredentialFromEnterpriseConfig(18, wifiConfig.enterpriseConfig));
        } else if (eapMethod == 5) {
            credential.setSimCredential(buildSimCredentialFromEnterpriseConfig(23, wifiConfig.enterpriseConfig));
        } else if (eapMethod != 6) {
            Log.e(TAG, "Unsupport EAP method: " + wifiConfig.enterpriseConfig.getEapMethod());
            return null;
        } else {
            credential.setSimCredential(buildSimCredentialFromEnterpriseConfig(50, wifiConfig.enterpriseConfig));
        }
        if (credential.getUserCredential() == null && credential.getCertCredential() == null && credential.getSimCredential() == null) {
            Log.e(TAG, "Missing credential");
            return null;
        }
        passpointConfig.setCredential(credential);
        return passpointConfig;
    }

    public boolean equals(Object thatObject) {
        List<String> list;
        PasspointConfiguration passpointConfiguration;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof PasspointProvider)) {
            return false;
        }
        PasspointProvider that = (PasspointProvider) thatObject;
        if (this.mProviderId != that.mProviderId || ((list = this.mCaCertificateAliases) != null ? !list.equals(that.mCaCertificateAliases) : that.mCaCertificateAliases != null) || !TextUtils.equals(this.mClientCertificateAlias, that.mClientCertificateAlias) || !TextUtils.equals(this.mClientPrivateKeyAlias, that.mClientPrivateKeyAlias) || ((passpointConfiguration = this.mConfig) != null ? !passpointConfiguration.equals(that.mConfig) : that.mConfig != null) || !TextUtils.equals(this.mRemediationCaCertificateAlias, that.mRemediationCaCertificateAlias)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.mProviderId), this.mCaCertificateAliases, this.mClientCertificateAlias, this.mClientPrivateKeyAlias, this.mConfig, this.mRemediationCaCertificateAlias);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProviderId: ");
        builder.append(this.mProviderId);
        builder.append("\n");
        builder.append("CreatorUID: ");
        builder.append(this.mCreatorUid);
        builder.append("\n");
        if (this.mPackageName != null) {
            builder.append("PackageName: ");
            builder.append(this.mPackageName);
            builder.append("\n");
        }
        builder.append("Configuration Begin ---\n");
        builder.append(this.mConfig);
        builder.append("Configuration End ---\n");
        return builder.toString();
    }

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
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            return null;
        }
    }

    private boolean isMeteredNetwork(PasspointConfiguration passpointConfig) {
        if (passpointConfig == null) {
            return false;
        }
        if (passpointConfig.getUsageLimitDataLimit() > 0 || passpointConfig.getUsageLimitTimeLimitInMinutes() > 0) {
            return true;
        }
        return false;
    }

    private PasspointMatch matchProviderExceptFor3GPP(Map<Constants.ANQPElementType, ANQPElement> anqpElements, InformationElementUtil.RoamingConsortium roamingConsortium) {
        if (ANQPMatcher.matchDomainName((DomainNameElement) anqpElements.get(Constants.ANQPElementType.ANQPDomName), this.mConfig.getHomeSp().getFqdn(), this.mImsiParameter, this.mMatchingSIMImsiList)) {
            return PasspointMatch.HomeProvider;
        }
        long[] providerOIs = this.mConfig.getHomeSp().getRoamingConsortiumOis();
        if (ANQPMatcher.matchRoamingConsortium((RoamingConsortiumElement) anqpElements.get(Constants.ANQPElementType.ANQPRoamingConsortium), providerOIs)) {
            return PasspointMatch.RoamingProvider;
        }
        long[] roamingConsortiums = roamingConsortium.getRoamingConsortiums();
        if (!(roamingConsortiums == null || providerOIs == null)) {
            for (long sta_oi : roamingConsortiums) {
                for (long ap_oi : providerOIs) {
                    if (sta_oi == ap_oi) {
                        return PasspointMatch.RoamingProvider;
                    }
                }
            }
        }
        return PasspointMatch.None;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005f, code lost:
        if (r5.equals("PAP") != false) goto L_0x0063;
     */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0088  */
    private void buildEnterpriseConfigForUserCredential(WifiEnterpriseConfig config, Credential.UserCredential credential) {
        char c = 0;
        String decodedPassword = new String(Base64.decode(credential.getPassword(), 0), StandardCharsets.UTF_8);
        config.setEapMethod(2);
        config.setIdentity(credential.getUsername());
        config.setPassword(decodedPassword);
        config.setCaCertificateAliases((String[]) this.mCaCertificateAliases.toArray(new String[0]));
        int phase2Method = 0;
        String nonEapInnerMethod = credential.getNonEapInnerMethod();
        int hashCode = nonEapInnerMethod.hashCode();
        if (hashCode != 78975) {
            if (hashCode != 632512142) {
                if (hashCode == 2038151963 && nonEapInnerMethod.equals("MS-CHAP")) {
                    c = 1;
                    if (c == 0) {
                        phase2Method = 1;
                    } else if (c == 1) {
                        phase2Method = 2;
                    } else if (c != 2) {
                        Log.wtf(TAG, "Unsupported Auth: " + credential.getNonEapInnerMethod());
                    } else {
                        phase2Method = 3;
                    }
                    config.setPhase2Method(phase2Method);
                }
            } else if (nonEapInnerMethod.equals("MS-CHAP-V2")) {
                c = 2;
                if (c == 0) {
                }
                config.setPhase2Method(phase2Method);
            }
        }
        c = 65535;
        if (c == 0) {
        }
        config.setPhase2Method(phase2Method);
    }

    private void buildEnterpriseConfigForCertCredential(WifiEnterpriseConfig config) {
        config.setEapMethod(1);
        config.setClientCertificateAlias(this.mClientCertificateAlias);
        config.setCaCertificateAliases((String[]) this.mCaCertificateAliases.toArray(new String[0]));
    }

    private void buildEnterpriseConfigForSimCredential(WifiEnterpriseConfig config, Credential.SimCredential credential) {
        int eapMethod = -1;
        int eapType = credential.getEapType();
        if (eapType == 18) {
            eapMethod = 4;
        } else if (eapType == 23) {
            eapMethod = 5;
        } else if (eapType != 50) {
            Log.wtf(TAG, "Unsupported EAP Method: " + credential.getEapType());
        } else {
            eapMethod = 6;
        }
        config.setEapMethod(eapMethod);
        config.setPlmn(credential.getImsi());
    }

    private static void setAnonymousIdentityToNaiRealm(WifiEnterpriseConfig config, String realm) {
        config.setAnonymousIdentity("anonymous@" + realm);
    }

    private static Credential.UserCredential buildUserCredentialFromEnterpriseConfig(WifiEnterpriseConfig config) {
        Credential.UserCredential userCredential = new Credential.UserCredential();
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
        int phase2Method = config.getPhase2Method();
        if (phase2Method == 1) {
            userCredential.setNonEapInnerMethod("PAP");
        } else if (phase2Method == 2) {
            userCredential.setNonEapInnerMethod("MS-CHAP");
        } else if (phase2Method != 3) {
            Log.e(TAG, "Unsupported phase2 method for TTLS: " + config.getPhase2Method());
            return null;
        } else {
            userCredential.setNonEapInnerMethod("MS-CHAP-V2");
        }
        return userCredential;
    }

    private static Credential.SimCredential buildSimCredentialFromEnterpriseConfig(int eapType, WifiEnterpriseConfig config) {
        Credential.SimCredential simCredential = new Credential.SimCredential();
        if (TextUtils.isEmpty(config.getPlmn())) {
            Log.e(TAG, "Missing IMSI for SIM credential");
            return null;
        }
        simCredential.setImsi(config.getPlmn());
        simCredential.setEapType(eapType);
        return simCredential;
    }
}
