package org.bouncycastle.est.jcajce;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import org.bouncycastle.crypto.CryptoServicesRegistrar;

class SSLSocketFactoryCreatorBuilder {
    protected KeyManager[] keyManagers;
    protected SecureRandom secureRandom = CryptoServicesRegistrar.getSecureRandom();
    protected Provider tlsProvider;
    protected String tlsVersion = "TLS";
    protected X509TrustManager[] trustManagers;

    public SSLSocketFactoryCreatorBuilder(X509TrustManager x509TrustManager) {
        if (x509TrustManager != null) {
            this.trustManagers = new X509TrustManager[]{x509TrustManager};
            return;
        }
        throw new NullPointerException("Trust managers can not be null");
    }

    public SSLSocketFactoryCreatorBuilder(X509TrustManager[] x509TrustManagerArr) {
        if (x509TrustManagerArr != null) {
            this.trustManagers = x509TrustManagerArr;
            return;
        }
        throw new NullPointerException("Trust managers can not be null");
    }

    public SSLSocketFactoryCreator build() {
        return new SSLSocketFactoryCreator() {
            /* class org.bouncycastle.est.jcajce.SSLSocketFactoryCreatorBuilder.AnonymousClass1 */

            @Override // org.bouncycastle.est.jcajce.SSLSocketFactoryCreator
            public SSLSocketFactory createFactory() throws NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
                SSLContext instance = SSLSocketFactoryCreatorBuilder.this.tlsProvider != null ? SSLContext.getInstance(SSLSocketFactoryCreatorBuilder.this.tlsVersion, SSLSocketFactoryCreatorBuilder.this.tlsProvider) : SSLContext.getInstance(SSLSocketFactoryCreatorBuilder.this.tlsVersion);
                instance.init(SSLSocketFactoryCreatorBuilder.this.keyManagers, SSLSocketFactoryCreatorBuilder.this.trustManagers, SSLSocketFactoryCreatorBuilder.this.secureRandom);
                return instance.getSocketFactory();
            }

            @Override // org.bouncycastle.est.jcajce.SSLSocketFactoryCreator
            public boolean isTrusted() {
                for (int i = 0; i != SSLSocketFactoryCreatorBuilder.this.trustManagers.length; i++) {
                    if (SSLSocketFactoryCreatorBuilder.this.trustManagers[i].getAcceptedIssuers().length > 0) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public SSLSocketFactoryCreatorBuilder withKeyManager(KeyManager keyManager) {
        if (keyManager == null) {
            this.keyManagers = null;
        } else {
            this.keyManagers = new KeyManager[]{keyManager};
        }
        return this;
    }

    public SSLSocketFactoryCreatorBuilder withKeyManagers(KeyManager[] keyManagerArr) {
        this.keyManagers = keyManagerArr;
        return this;
    }

    public SSLSocketFactoryCreatorBuilder withProvider(String str) throws NoSuchProviderException {
        this.tlsProvider = Security.getProvider(str);
        if (this.tlsProvider != null) {
            return this;
        }
        throw new NoSuchProviderException("JSSE provider not found: " + str);
    }

    public SSLSocketFactoryCreatorBuilder withProvider(Provider provider) {
        this.tlsProvider = provider;
        return this;
    }

    public SSLSocketFactoryCreatorBuilder withSecureRandom(SecureRandom secureRandom2) {
        this.secureRandom = secureRandom2;
        return this;
    }

    public SSLSocketFactoryCreatorBuilder withTLSVersion(String str) {
        this.tlsVersion = str;
        return this;
    }
}
