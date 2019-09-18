package com.android.org.conscrypt;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

final class SSLParametersImpl implements Cloneable {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static volatile SSLParametersImpl defaultParameters;
    private static volatile X509KeyManager defaultX509KeyManager;
    private static volatile X509TrustManager defaultX509TrustManager;
    ApplicationProtocolSelectorAdapter applicationProtocolSelector;
    byte[] applicationProtocols = EmptyArray.BYTE;
    boolean channelIdEnabled;
    private final ClientSessionContext clientSessionContext;
    private boolean client_mode = true;
    private boolean ctVerificationEnabled;
    private boolean enable_session_creation = true;
    String[] enabledCipherSuites;
    String[] enabledProtocols;
    private String endpointIdentificationAlgorithm;
    boolean isEnabledProtocolsFiltered;
    private boolean need_client_auth = false;
    byte[] ocspResponse;
    private final PSKKeyManager pskKeyManager;
    byte[] sctExtension;
    private final ServerSessionContext serverSessionContext;
    private boolean useCipherSuitesOrder;
    boolean useSessionTickets;
    private Boolean useSni;
    private boolean want_client_auth = false;
    private final X509KeyManager x509KeyManager;
    private final X509TrustManager x509TrustManager;

    interface AliasChooser {
        String chooseClientAlias(X509KeyManager x509KeyManager, X500Principal[] x500PrincipalArr, String[] strArr);

        String chooseServerAlias(X509KeyManager x509KeyManager, String str);
    }

    interface PSKCallbacks {
        String chooseClientPSKIdentity(PSKKeyManager pSKKeyManager, String str);

        String chooseServerPSKIdentityHint(PSKKeyManager pSKKeyManager);

        SecretKey getPSKKey(PSKKeyManager pSKKeyManager, String str, String str2);
    }

    SSLParametersImpl(KeyManager[] kms, TrustManager[] tms, SecureRandom sr, ClientSessionContext clientSessionContext2, ServerSessionContext serverSessionContext2, String[] protocols) throws KeyManagementException {
        String[] strArr;
        boolean pskCipherSuitesNeeded = true;
        this.serverSessionContext = serverSessionContext2;
        this.clientSessionContext = clientSessionContext2;
        if (kms == null) {
            this.x509KeyManager = getDefaultX509KeyManager();
            this.pskKeyManager = null;
        } else {
            this.x509KeyManager = findFirstX509KeyManager(kms);
            this.pskKeyManager = findFirstPSKKeyManager(kms);
        }
        if (tms == null) {
            this.x509TrustManager = getDefaultX509TrustManager();
        } else {
            this.x509TrustManager = findFirstX509TrustManager(tms);
        }
        if (protocols == null) {
            strArr = NativeCrypto.DEFAULT_PROTOCOLS;
        } else {
            strArr = protocols;
        }
        this.enabledProtocols = (String[]) NativeCrypto.checkEnabledProtocols(strArr).clone();
        this.enabledCipherSuites = getDefaultCipherSuites((this.x509KeyManager == null && this.x509TrustManager == null) ? false : true, this.pskKeyManager == null ? false : pskCipherSuitesNeeded);
    }

    static SSLParametersImpl getDefault() throws KeyManagementException {
        SSLParametersImpl result = defaultParameters;
        if (result == null) {
            SSLParametersImpl sSLParametersImpl = new SSLParametersImpl(null, null, null, new ClientSessionContext(), new ServerSessionContext(), null);
            result = sSLParametersImpl;
            defaultParameters = sSLParametersImpl;
        }
        return (SSLParametersImpl) result.clone();
    }

    /* access modifiers changed from: package-private */
    public AbstractSessionContext getSessionContext() {
        return this.client_mode ? this.clientSessionContext : this.serverSessionContext;
    }

    /* access modifiers changed from: package-private */
    public ClientSessionContext getClientSessionContext() {
        return this.clientSessionContext;
    }

    /* access modifiers changed from: package-private */
    public X509KeyManager getX509KeyManager() {
        return this.x509KeyManager;
    }

    /* access modifiers changed from: package-private */
    public PSKKeyManager getPSKKeyManager() {
        return this.pskKeyManager;
    }

    /* access modifiers changed from: package-private */
    public X509TrustManager getX509TrustManager() {
        return this.x509TrustManager;
    }

    /* access modifiers changed from: package-private */
    public String[] getEnabledCipherSuites() {
        return (String[]) this.enabledCipherSuites.clone();
    }

    /* access modifiers changed from: package-private */
    public void setEnabledCipherSuites(String[] cipherSuites) {
        this.enabledCipherSuites = (String[]) NativeCrypto.checkEnabledCipherSuites(cipherSuites).clone();
    }

    /* access modifiers changed from: package-private */
    public String[] getEnabledProtocols() {
        return (String[]) this.enabledProtocols.clone();
    }

    /* access modifiers changed from: package-private */
    public void setEnabledProtocols(String[] protocols) {
        if (protocols != null) {
            String[] filteredProtocols = filterFromProtocols(protocols, "SSLv3");
            this.isEnabledProtocolsFiltered = protocols.length != filteredProtocols.length;
            this.enabledProtocols = (String[]) NativeCrypto.checkEnabledProtocols(filteredProtocols).clone();
            return;
        }
        throw new IllegalArgumentException("protocols == null");
    }

    /* access modifiers changed from: package-private */
    public void setApplicationProtocols(String[] protocols) {
        this.applicationProtocols = SSLUtils.encodeProtocols(protocols);
    }

    /* access modifiers changed from: package-private */
    public String[] getApplicationProtocols() {
        return SSLUtils.decodeProtocols(this.applicationProtocols);
    }

    /* access modifiers changed from: package-private */
    public void setApplicationProtocolSelector(ApplicationProtocolSelectorAdapter applicationProtocolSelector2) {
        this.applicationProtocolSelector = applicationProtocolSelector2;
    }

    /* access modifiers changed from: package-private */
    public void setUseClientMode(boolean mode) {
        this.client_mode = mode;
    }

    /* access modifiers changed from: package-private */
    public boolean getUseClientMode() {
        return this.client_mode;
    }

    /* access modifiers changed from: package-private */
    public void setNeedClientAuth(boolean need) {
        this.need_client_auth = need;
        this.want_client_auth = false;
    }

    /* access modifiers changed from: package-private */
    public boolean getNeedClientAuth() {
        return this.need_client_auth;
    }

    /* access modifiers changed from: package-private */
    public void setWantClientAuth(boolean want) {
        this.want_client_auth = want;
        this.need_client_auth = false;
    }

    /* access modifiers changed from: package-private */
    public boolean getWantClientAuth() {
        return this.want_client_auth;
    }

    /* access modifiers changed from: package-private */
    public void setEnableSessionCreation(boolean flag) {
        this.enable_session_creation = flag;
    }

    /* access modifiers changed from: package-private */
    public boolean getEnableSessionCreation() {
        return this.enable_session_creation;
    }

    /* access modifiers changed from: package-private */
    public void setUseSessionTickets(boolean useSessionTickets2) {
        this.useSessionTickets = useSessionTickets2;
    }

    /* access modifiers changed from: package-private */
    public void setUseSni(boolean flag) {
        this.useSni = Boolean.valueOf(flag);
    }

    /* access modifiers changed from: package-private */
    public boolean getUseSni() {
        return this.useSni != null ? this.useSni.booleanValue() : isSniEnabledByDefault();
    }

    /* access modifiers changed from: package-private */
    public void setCTVerificationEnabled(boolean enabled) {
        this.ctVerificationEnabled = enabled;
    }

    /* access modifiers changed from: package-private */
    public void setSCTExtension(byte[] extension) {
        this.sctExtension = extension;
    }

    /* access modifiers changed from: package-private */
    public void setOCSPResponse(byte[] response) {
        this.ocspResponse = response;
    }

    /* access modifiers changed from: package-private */
    public byte[] getOCSPResponse() {
        return this.ocspResponse;
    }

    private static String[] filterFromProtocols(String[] protocols, String obsoleteProtocol) {
        if (protocols.length == 1 && obsoleteProtocol.equals(protocols[0])) {
            return EMPTY_STRING_ARRAY;
        }
        ArrayList<String> newProtocols = new ArrayList<>();
        for (String protocol : protocols) {
            if (!obsoleteProtocol.equals(protocol)) {
                newProtocols.add(protocol);
            }
        }
        return (String[]) newProtocols.toArray(EMPTY_STRING_ARRAY);
    }

    private boolean isSniEnabledByDefault() {
        try {
            String enableSNI = System.getProperty("jsse.enableSNIExtension", "true");
            if ("true".equalsIgnoreCase(enableSNI)) {
                return true;
            }
            if ("false".equalsIgnoreCase(enableSNI)) {
                return false;
            }
            throw new RuntimeException("Can only set \"jsse.enableSNIExtension\" to \"true\" or \"false\"");
        } catch (SecurityException e) {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    private static X509KeyManager getDefaultX509KeyManager() throws KeyManagementException {
        X509KeyManager result = defaultX509KeyManager;
        if (result != null) {
            return result;
        }
        X509KeyManager createDefaultX509KeyManager = createDefaultX509KeyManager();
        X509KeyManager result2 = createDefaultX509KeyManager;
        defaultX509KeyManager = createDefaultX509KeyManager;
        return result2;
    }

    private static X509KeyManager createDefaultX509KeyManager() throws KeyManagementException {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(null, null);
            KeyManager[] kms = kmf.getKeyManagers();
            X509KeyManager result = findFirstX509KeyManager(kms);
            if (result != null) {
                return result;
            }
            throw new KeyManagementException("No X509KeyManager among default KeyManagers: " + Arrays.toString(kms));
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagementException(e);
        } catch (KeyStoreException e2) {
            throw new KeyManagementException(e2);
        } catch (UnrecoverableKeyException e3) {
            throw new KeyManagementException(e3);
        }
    }

    private static X509KeyManager findFirstX509KeyManager(KeyManager[] kms) {
        for (X509KeyManager x509KeyManager2 : kms) {
            if (x509KeyManager2 instanceof X509KeyManager) {
                return x509KeyManager2;
            }
        }
        return null;
    }

    private static PSKKeyManager findFirstPSKKeyManager(KeyManager[] kms) {
        int length = kms.length;
        int i = 0;
        while (i < length) {
            PSKKeyManager pSKKeyManager = kms[i];
            if (pSKKeyManager instanceof PSKKeyManager) {
                return pSKKeyManager;
            }
            if (pSKKeyManager != null) {
                try {
                    return DuckTypedPSKKeyManager.getInstance(pSKKeyManager);
                } catch (NoSuchMethodException e) {
                }
            } else {
                i++;
            }
        }
        return null;
    }

    static X509TrustManager getDefaultX509TrustManager() throws KeyManagementException {
        X509TrustManager result = defaultX509TrustManager;
        if (result != null) {
            return result;
        }
        X509TrustManager createDefaultX509TrustManager = createDefaultX509TrustManager();
        X509TrustManager result2 = createDefaultX509TrustManager;
        defaultX509TrustManager = createDefaultX509TrustManager;
        return result2;
    }

    private static X509TrustManager createDefaultX509TrustManager() throws KeyManagementException {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(null);
            TrustManager[] tms = tmf.getTrustManagers();
            X509TrustManager trustManager = findFirstX509TrustManager(tms);
            if (trustManager != null) {
                return trustManager;
            }
            throw new KeyManagementException("No X509TrustManager in among default TrustManagers: " + Arrays.toString(tms));
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagementException(e);
        } catch (KeyStoreException e2) {
            throw new KeyManagementException(e2);
        }
    }

    private static X509TrustManager findFirstX509TrustManager(TrustManager[] tms) {
        for (X509TrustManager x509TrustManager2 : tms) {
            if (x509TrustManager2 instanceof X509TrustManager) {
                return x509TrustManager2;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getEndpointIdentificationAlgorithm() {
        return this.endpointIdentificationAlgorithm;
    }

    /* access modifiers changed from: package-private */
    public void setEndpointIdentificationAlgorithm(String endpointIdentificationAlgorithm2) {
        this.endpointIdentificationAlgorithm = endpointIdentificationAlgorithm2;
    }

    /* access modifiers changed from: package-private */
    public boolean getUseCipherSuitesOrder() {
        return this.useCipherSuitesOrder;
    }

    /* access modifiers changed from: package-private */
    public void setUseCipherSuitesOrder(boolean useCipherSuitesOrder2) {
        this.useCipherSuitesOrder = useCipherSuitesOrder2;
    }

    private static String[] getDefaultCipherSuites(boolean x509CipherSuitesNeeded, boolean pskCipherSuitesNeeded) {
        if (x509CipherSuitesNeeded) {
            if (pskCipherSuitesNeeded) {
                return concat(NativeCrypto.DEFAULT_PSK_CIPHER_SUITES, NativeCrypto.DEFAULT_X509_CIPHER_SUITES, new String[]{"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"});
            }
            return concat(NativeCrypto.DEFAULT_X509_CIPHER_SUITES, new String[]{"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"});
        } else if (!pskCipherSuitesNeeded) {
            return new String[]{"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};
        } else {
            return concat(NativeCrypto.DEFAULT_PSK_CIPHER_SUITES, new String[]{"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"});
        }
    }

    private static String[] concat(String[]... arrays) {
        int resultLength = 0;
        for (String[] array : arrays) {
            resultLength += array.length;
        }
        String[] result = new String[resultLength];
        int resultOffset = 0;
        for (String[] array2 : arrays) {
            System.arraycopy(array2, 0, result, resultOffset, array2.length);
            resultOffset += array2.length;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean isCTVerificationEnabled(String hostname) {
        if (hostname == null) {
            return false;
        }
        if (this.ctVerificationEnabled) {
            return true;
        }
        return Platform.isCTVerificationRequired(hostname);
    }
}
