package android.security.net.config;

import android.util.ArrayMap;
import com.android.org.conscrypt.TrustManagerImpl;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

public class NetworkSecurityTrustManager extends X509ExtendedTrustManager {
    private final TrustManagerImpl mDelegate;
    private X509Certificate[] mIssuers;
    private final Object mIssuersLock = new Object();
    private final NetworkSecurityConfig mNetworkSecurityConfig;

    public NetworkSecurityTrustManager(NetworkSecurityConfig config) {
        if (config != null) {
            this.mNetworkSecurityConfig = config;
            try {
                TrustedCertificateStoreAdapter certStore = new TrustedCertificateStoreAdapter(config);
                KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
                store.load(null);
                this.mDelegate = new TrustManagerImpl(store, null, certStore);
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new NullPointerException("config must not be null");
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.mDelegate.checkClientTrusted(chain, authType);
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException {
        this.mDelegate.checkClientTrusted(certs, authType, socket);
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException {
        this.mDelegate.checkClientTrusted(certs, authType, engine);
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        checkServerTrusted(certs, authType, (String) null);
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException {
        checkPins(this.mDelegate.getTrustedChainForServer(certs, authType, socket));
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException {
        checkPins(this.mDelegate.getTrustedChainForServer(certs, authType, engine));
    }

    public List<X509Certificate> checkServerTrusted(X509Certificate[] certs, String authType, String host) throws CertificateException {
        List<X509Certificate> trustedChain = this.mDelegate.checkServerTrusted(certs, authType, host);
        checkPins(trustedChain);
        return trustedChain;
    }

    private void checkPins(List<X509Certificate> chain) throws CertificateException {
        PinSet pinSet = this.mNetworkSecurityConfig.getPins();
        if (!pinSet.pins.isEmpty() && System.currentTimeMillis() <= pinSet.expirationTime && isPinningEnforced(chain)) {
            Set<String> pinAlgorithms = pinSet.getPinAlgorithms();
            Map<String, MessageDigest> digestMap = new ArrayMap<>(pinAlgorithms.size());
            for (int i = chain.size() - 1; i >= 0; i--) {
                byte[] encodedSPKI = chain.get(i).getPublicKey().getEncoded();
                for (String algorithm : pinAlgorithms) {
                    MessageDigest md = digestMap.get(algorithm);
                    if (md == null) {
                        try {
                            md = MessageDigest.getInstance(algorithm);
                            digestMap.put(algorithm, md);
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (pinSet.pins.contains(new Pin(algorithm, md.digest(encodedSPKI)))) {
                        return;
                    }
                }
            }
            throw new CertificateException("Pin verification failed");
        }
    }

    private boolean isPinningEnforced(List<X509Certificate> chain) throws CertificateException {
        if (chain.isEmpty()) {
            return false;
        }
        TrustAnchor chainAnchor = this.mNetworkSecurityConfig.findTrustAnchorBySubjectAndPublicKey(chain.get(chain.size() - 1));
        if (chainAnchor != null) {
            return !chainAnchor.overridesPins;
        }
        throw new CertificateException("Trusted chain does not end in a TrustAnchor");
    }

    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] x509CertificateArr;
        synchronized (this.mIssuersLock) {
            if (this.mIssuers == null) {
                Set<TrustAnchor> anchors = this.mNetworkSecurityConfig.getTrustAnchors();
                X509Certificate[] issuers = new X509Certificate[anchors.size()];
                int i = 0;
                for (TrustAnchor anchor : anchors) {
                    issuers[i] = anchor.certificate;
                    i++;
                }
                this.mIssuers = issuers;
            }
            x509CertificateArr = (X509Certificate[]) this.mIssuers.clone();
        }
        return x509CertificateArr;
    }

    public void handleTrustStorageUpdate() {
        synchronized (this.mIssuersLock) {
            this.mIssuers = null;
            this.mDelegate.handleTrustStorageUpdate();
        }
    }
}
