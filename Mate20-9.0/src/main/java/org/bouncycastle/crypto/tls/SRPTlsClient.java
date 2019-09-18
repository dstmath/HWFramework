package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.util.Hashtable;
import org.bouncycastle.util.Arrays;

public class SRPTlsClient extends AbstractTlsClient {
    protected TlsSRPGroupVerifier groupVerifier;
    protected byte[] identity;
    protected byte[] password;

    public SRPTlsClient(TlsCipherFactory tlsCipherFactory, TlsSRPGroupVerifier tlsSRPGroupVerifier, byte[] bArr, byte[] bArr2) {
        super(tlsCipherFactory);
        this.groupVerifier = tlsSRPGroupVerifier;
        this.identity = Arrays.clone(bArr);
        this.password = Arrays.clone(bArr2);
    }

    public SRPTlsClient(TlsCipherFactory tlsCipherFactory, byte[] bArr, byte[] bArr2) {
        this(tlsCipherFactory, new DefaultTlsSRPGroupVerifier(), bArr, bArr2);
    }

    public SRPTlsClient(byte[] bArr, byte[] bArr2) {
        this(new DefaultTlsCipherFactory(), new DefaultTlsSRPGroupVerifier(), bArr, bArr2);
    }

    /* access modifiers changed from: protected */
    public TlsKeyExchange createSRPKeyExchange(int i) {
        TlsSRPKeyExchange tlsSRPKeyExchange = new TlsSRPKeyExchange(i, this.supportedSignatureAlgorithms, this.groupVerifier, this.identity, this.password);
        return tlsSRPKeyExchange;
    }

    public TlsAuthentication getAuthentication() throws IOException {
        throw new TlsFatalAlert(80);
    }

    public int[] getCipherSuites() {
        return new int[]{49182};
    }

    public Hashtable getClientExtensions() throws IOException {
        Hashtable ensureExtensionsInitialised = TlsExtensionsUtils.ensureExtensionsInitialised(super.getClientExtensions());
        TlsSRPUtils.addSRPExtension(ensureExtensionsInitialised, this.identity);
        return ensureExtensionsInitialised;
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

    public void processServerExtensions(Hashtable hashtable) throws IOException {
        if (TlsUtils.hasExpectedEmptyExtensionData(hashtable, TlsSRPUtils.EXT_SRP, 47) || !requireSRPServerExtension()) {
            super.processServerExtensions(hashtable);
            return;
        }
        throw new TlsFatalAlert(47);
    }

    /* access modifiers changed from: protected */
    public boolean requireSRPServerExtension() {
        return false;
    }
}
