package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.util.Hashtable;

public class SRPTlsServer extends AbstractTlsServer {
    protected TlsSRPLoginParameters loginParameters;
    protected byte[] srpIdentity;
    protected TlsSRPIdentityManager srpIdentityManager;

    public SRPTlsServer(TlsCipherFactory tlsCipherFactory, TlsSRPIdentityManager tlsSRPIdentityManager) {
        super(tlsCipherFactory);
        this.srpIdentity = null;
        this.loginParameters = null;
        this.srpIdentityManager = tlsSRPIdentityManager;
    }

    public SRPTlsServer(TlsSRPIdentityManager tlsSRPIdentityManager) {
        this(new DefaultTlsCipherFactory(), tlsSRPIdentityManager);
    }

    /* access modifiers changed from: protected */
    public TlsKeyExchange createSRPKeyExchange(int i) {
        return new TlsSRPKeyExchange(i, this.supportedSignatureAlgorithms, this.srpIdentity, this.loginParameters);
    }

    /* access modifiers changed from: protected */
    public int[] getCipherSuites() {
        return new int[]{CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA, CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA, CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_SRP_SHA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_SRP_SHA_WITH_AES_128_CBC_SHA};
    }

    public TlsCredentials getCredentials() throws IOException {
        switch (TlsUtils.getKeyExchangeAlgorithm(this.selectedCipherSuite)) {
            case 21:
                return null;
            case 22:
                return getDSASignerCredentials();
            case 23:
                return getRSASignerCredentials();
            default:
                throw new TlsFatalAlert(80);
        }
    }

    /* access modifiers changed from: protected */
    public TlsSignerCredentials getDSASignerCredentials() throws IOException {
        throw new TlsFatalAlert(80);
    }

    public TlsKeyExchange getKeyExchange() throws IOException {
        int keyExchangeAlgorithm = TlsUtils.getKeyExchangeAlgorithm(this.selectedCipherSuite);
        switch (keyExchangeAlgorithm) {
            case 21:
            case 22:
            case 23:
                return createSRPKeyExchange(keyExchangeAlgorithm);
            default:
                throw new TlsFatalAlert(80);
        }
    }

    /* access modifiers changed from: protected */
    public TlsSignerCredentials getRSASignerCredentials() throws IOException {
        throw new TlsFatalAlert(80);
    }

    public int getSelectedCipherSuite() throws IOException {
        int selectedCipherSuite = super.getSelectedCipherSuite();
        if (!TlsSRPUtils.isSRPCipherSuite(selectedCipherSuite)) {
            return selectedCipherSuite;
        }
        if (this.srpIdentity != null) {
            this.loginParameters = this.srpIdentityManager.getLoginParameters(this.srpIdentity);
        }
        if (this.loginParameters != null) {
            return selectedCipherSuite;
        }
        throw new TlsFatalAlert(AlertDescription.unknown_psk_identity);
    }

    public void processClientExtensions(Hashtable hashtable) throws IOException {
        super.processClientExtensions(hashtable);
        this.srpIdentity = TlsSRPUtils.getSRPExtension(hashtable);
    }
}
