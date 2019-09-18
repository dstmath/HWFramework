package org.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;

public class TlsECDHKeyExchange extends AbstractTlsKeyExchange {
    protected TlsAgreementCredentials agreementCredentials;
    protected short[] clientECPointFormats;
    protected ECPrivateKeyParameters ecAgreePrivateKey;
    protected ECPublicKeyParameters ecAgreePublicKey;
    protected int[] namedCurves;
    protected short[] serverECPointFormats;
    protected AsymmetricKeyParameter serverPublicKey;
    protected TlsSigner tlsSigner;

    public TlsECDHKeyExchange(int i, Vector vector, int[] iArr, short[] sArr, short[] sArr2) {
        super(i, vector);
        TlsSigner tlsSigner2;
        switch (i) {
            case 16:
            case 18:
            case 20:
                tlsSigner2 = null;
                break;
            case 17:
                tlsSigner2 = new TlsECDSASigner();
                break;
            case 19:
                tlsSigner2 = new TlsRSASigner();
                break;
            default:
                throw new IllegalArgumentException("unsupported key exchange algorithm");
        }
        this.tlsSigner = tlsSigner2;
        this.namedCurves = iArr;
        this.clientECPointFormats = sArr;
        this.serverECPointFormats = sArr2;
    }

    public void generateClientKeyExchange(OutputStream outputStream) throws IOException {
        if (this.agreementCredentials == null) {
            this.ecAgreePrivateKey = TlsECCUtils.generateEphemeralClientKeyExchange(this.context.getSecureRandom(), this.serverECPointFormats, this.ecAgreePublicKey.getParameters(), outputStream);
        }
    }

    public byte[] generatePremasterSecret() throws IOException {
        if (this.agreementCredentials != null) {
            return this.agreementCredentials.generateAgreement(this.ecAgreePublicKey);
        }
        if (this.ecAgreePrivateKey != null) {
            return TlsECCUtils.calculateECDHBasicAgreement(this.ecAgreePublicKey, this.ecAgreePrivateKey);
        }
        throw new TlsFatalAlert(80);
    }

    public byte[] generateServerKeyExchange() throws IOException {
        if (!requiresServerKeyExchange()) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.ecAgreePrivateKey = TlsECCUtils.generateEphemeralServerKeyExchange(this.context.getSecureRandom(), this.namedCurves, this.clientECPointFormats, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void init(TlsContext tlsContext) {
        super.init(tlsContext);
        if (this.tlsSigner != null) {
            this.tlsSigner.init(tlsContext);
        }
    }

    public void processClientCertificate(Certificate certificate) throws IOException {
        if (this.keyExchange == 20) {
            throw new TlsFatalAlert(10);
        }
    }

    public void processClientCredentials(TlsCredentials tlsCredentials) throws IOException {
        if (this.keyExchange == 20) {
            throw new TlsFatalAlert(80);
        } else if (tlsCredentials instanceof TlsAgreementCredentials) {
            this.agreementCredentials = (TlsAgreementCredentials) tlsCredentials;
        } else if (!(tlsCredentials instanceof TlsSignerCredentials)) {
            throw new TlsFatalAlert(80);
        }
    }

    public void processClientKeyExchange(InputStream inputStream) throws IOException {
        if (this.ecAgreePublicKey == null) {
            byte[] readOpaque8 = TlsUtils.readOpaque8(inputStream);
            this.ecAgreePublicKey = TlsECCUtils.validateECPublicKey(TlsECCUtils.deserializeECPublicKey(this.serverECPointFormats, this.ecAgreePrivateKey.getParameters(), readOpaque8));
        }
    }

    public void processServerCertificate(Certificate certificate) throws IOException {
        int i;
        if (this.keyExchange == 20) {
            throw new TlsFatalAlert(10);
        } else if (!certificate.isEmpty()) {
            Certificate certificateAt = certificate.getCertificateAt(0);
            try {
                this.serverPublicKey = PublicKeyFactory.createKey(certificateAt.getSubjectPublicKeyInfo());
                if (this.tlsSigner == null) {
                    try {
                        this.ecAgreePublicKey = TlsECCUtils.validateECPublicKey((ECPublicKeyParameters) this.serverPublicKey);
                        i = 8;
                    } catch (ClassCastException e) {
                        throw new TlsFatalAlert(46, e);
                    }
                } else if (this.tlsSigner.isValidPublicKey(this.serverPublicKey)) {
                    i = 128;
                } else {
                    throw new TlsFatalAlert(46);
                }
                TlsUtils.validateKeyUsage(certificateAt, i);
                super.processServerCertificate(certificate);
            } catch (RuntimeException e2) {
                throw new TlsFatalAlert(43, e2);
            }
        } else {
            throw new TlsFatalAlert(42);
        }
    }

    public void processServerKeyExchange(InputStream inputStream) throws IOException {
        if (requiresServerKeyExchange()) {
            this.ecAgreePublicKey = TlsECCUtils.validateECPublicKey(TlsECCUtils.deserializeECPublicKey(this.clientECPointFormats, TlsECCUtils.readECParameters(this.namedCurves, this.clientECPointFormats, inputStream), TlsUtils.readOpaque8(inputStream)));
            return;
        }
        throw new TlsFatalAlert(10);
    }

    public boolean requiresServerKeyExchange() {
        switch (this.keyExchange) {
            case 17:
            case 19:
            case 20:
                return true;
            default:
                return false;
        }
    }

    public void skipServerCredentials() throws IOException {
        if (this.keyExchange != 20) {
            throw new TlsFatalAlert(10);
        }
    }

    public void validateCertificateRequest(CertificateRequest certificateRequest) throws IOException {
        if (this.keyExchange != 20) {
            short[] certificateTypes = certificateRequest.getCertificateTypes();
            for (short s : certificateTypes) {
                switch (s) {
                    case 1:
                    case 2:
                        break;
                    default:
                        switch (s) {
                            case 64:
                            case 65:
                            case 66:
                                break;
                            default:
                                throw new TlsFatalAlert(47);
                        }
                }
            }
            return;
        }
        throw new TlsFatalAlert(40);
    }
}
