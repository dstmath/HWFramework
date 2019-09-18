package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public abstract class AbstractTlsKeyExchange implements TlsKeyExchange {
    protected TlsContext context;
    protected int keyExchange;
    protected Vector supportedSignatureAlgorithms;

    protected AbstractTlsKeyExchange(int i, Vector vector) {
        this.keyExchange = i;
        this.supportedSignatureAlgorithms = vector;
    }

    public byte[] generateServerKeyExchange() throws IOException {
        if (!requiresServerKeyExchange()) {
            return null;
        }
        throw new TlsFatalAlert(80);
    }

    public void init(TlsContext tlsContext) {
        Vector vector;
        this.context = tlsContext;
        ProtocolVersion clientVersion = tlsContext.getClientVersion();
        if (TlsUtils.isSignatureAlgorithmsExtensionAllowed(clientVersion)) {
            if (this.supportedSignatureAlgorithms == null) {
                int i = this.keyExchange;
                if (i != 1) {
                    if (i != 3) {
                        if (i != 5) {
                            if (i != 7) {
                                if (i != 9) {
                                    switch (i) {
                                        case 13:
                                        case 14:
                                            break;
                                        case 15:
                                        case 18:
                                        case 19:
                                            break;
                                        case 16:
                                        case 17:
                                            vector = TlsUtils.getDefaultECDSASignatureAlgorithms();
                                            break;
                                        default:
                                            switch (i) {
                                                case 21:
                                                case 24:
                                                    break;
                                                case 22:
                                                    break;
                                                case 23:
                                                    break;
                                                default:
                                                    throw new IllegalStateException("unsupported key exchange algorithm");
                                            }
                                    }
                                }
                            }
                        }
                    }
                    vector = TlsUtils.getDefaultDSSSignatureAlgorithms();
                    this.supportedSignatureAlgorithms = vector;
                }
                vector = TlsUtils.getDefaultRSASignatureAlgorithms();
                this.supportedSignatureAlgorithms = vector;
            }
        } else if (this.supportedSignatureAlgorithms != null) {
            throw new IllegalStateException("supported_signature_algorithms not allowed for " + clientVersion);
        }
    }

    /* access modifiers changed from: protected */
    public DigitallySigned parseSignature(InputStream inputStream) throws IOException {
        DigitallySigned parse = DigitallySigned.parse(this.context, inputStream);
        SignatureAndHashAlgorithm algorithm = parse.getAlgorithm();
        if (algorithm != null) {
            TlsUtils.verifySupportedSignatureAlgorithm(this.supportedSignatureAlgorithms, algorithm);
        }
        return parse;
    }

    public void processClientCertificate(Certificate certificate) throws IOException {
    }

    public void processClientKeyExchange(InputStream inputStream) throws IOException {
        throw new TlsFatalAlert(80);
    }

    public void processServerCertificate(Certificate certificate) throws IOException {
        Vector vector = this.supportedSignatureAlgorithms;
    }

    public void processServerCredentials(TlsCredentials tlsCredentials) throws IOException {
        processServerCertificate(tlsCredentials.getCertificate());
    }

    public void processServerKeyExchange(InputStream inputStream) throws IOException {
        if (!requiresServerKeyExchange()) {
            throw new TlsFatalAlert(10);
        }
    }

    public boolean requiresServerKeyExchange() {
        return false;
    }

    public void skipClientCredentials() throws IOException {
    }

    public void skipServerKeyExchange() throws IOException {
        if (requiresServerKeyExchange()) {
            throw new TlsFatalAlert(10);
        }
    }
}
