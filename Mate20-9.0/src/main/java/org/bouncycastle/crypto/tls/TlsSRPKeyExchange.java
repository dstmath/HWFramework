package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Vector;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.agreement.srp.SRP6Client;
import org.bouncycastle.crypto.agreement.srp.SRP6Server;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.SRP6GroupParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.io.TeeInputStream;

public class TlsSRPKeyExchange extends AbstractTlsKeyExchange {
    protected TlsSRPGroupVerifier groupVerifier;
    protected byte[] identity;
    protected byte[] password;
    protected TlsSignerCredentials serverCredentials;
    protected AsymmetricKeyParameter serverPublicKey;
    protected SRP6Client srpClient;
    protected SRP6GroupParameters srpGroup;
    protected BigInteger srpPeerCredentials;
    protected byte[] srpSalt;
    protected SRP6Server srpServer;
    protected BigInteger srpVerifier;
    protected TlsSigner tlsSigner;

    public TlsSRPKeyExchange(int i, Vector vector, TlsSRPGroupVerifier tlsSRPGroupVerifier, byte[] bArr, byte[] bArr2) {
        super(i, vector);
        this.serverPublicKey = null;
        this.srpGroup = null;
        this.srpClient = null;
        this.srpServer = null;
        this.srpPeerCredentials = null;
        this.srpVerifier = null;
        this.srpSalt = null;
        this.serverCredentials = null;
        this.tlsSigner = createSigner(i);
        this.groupVerifier = tlsSRPGroupVerifier;
        this.identity = bArr;
        this.password = bArr2;
        this.srpClient = new SRP6Client();
    }

    public TlsSRPKeyExchange(int i, Vector vector, byte[] bArr, TlsSRPLoginParameters tlsSRPLoginParameters) {
        super(i, vector);
        this.serverPublicKey = null;
        this.srpGroup = null;
        this.srpClient = null;
        this.srpServer = null;
        this.srpPeerCredentials = null;
        this.srpVerifier = null;
        this.srpSalt = null;
        this.serverCredentials = null;
        this.tlsSigner = createSigner(i);
        this.identity = bArr;
        this.srpServer = new SRP6Server();
        this.srpGroup = tlsSRPLoginParameters.getGroup();
        this.srpVerifier = tlsSRPLoginParameters.getVerifier();
        this.srpSalt = tlsSRPLoginParameters.getSalt();
    }

    public TlsSRPKeyExchange(int i, Vector vector, byte[] bArr, byte[] bArr2) {
        this(i, vector, new DefaultTlsSRPGroupVerifier(), bArr, bArr2);
    }

    protected static TlsSigner createSigner(int i) {
        switch (i) {
            case 21:
                return null;
            case 22:
                return new TlsDSSSigner();
            case 23:
                return new TlsRSASigner();
            default:
                throw new IllegalArgumentException("unsupported key exchange algorithm");
        }
    }

    public void generateClientKeyExchange(OutputStream outputStream) throws IOException {
        TlsSRPUtils.writeSRPParameter(this.srpClient.generateClientCredentials(this.srpSalt, this.identity, this.password), outputStream);
        this.context.getSecurityParameters().srpIdentity = Arrays.clone(this.identity);
    }

    public byte[] generatePremasterSecret() throws IOException {
        try {
            return BigIntegers.asUnsignedByteArray(this.srpServer != null ? this.srpServer.calculateSecret(this.srpPeerCredentials) : this.srpClient.calculateSecret(this.srpPeerCredentials));
        } catch (CryptoException e) {
            throw new TlsFatalAlert(47, e);
        }
    }

    public byte[] generateServerKeyExchange() throws IOException {
        this.srpServer.init(this.srpGroup, this.srpVerifier, TlsUtils.createHash(2), this.context.getSecureRandom());
        ServerSRPParams serverSRPParams = new ServerSRPParams(this.srpGroup.getN(), this.srpGroup.getG(), this.srpSalt, this.srpServer.generateServerCredentials());
        DigestInputBuffer digestInputBuffer = new DigestInputBuffer();
        serverSRPParams.encode(digestInputBuffer);
        if (this.serverCredentials != null) {
            SignatureAndHashAlgorithm signatureAndHashAlgorithm = TlsUtils.getSignatureAndHashAlgorithm(this.context, this.serverCredentials);
            Digest createHash = TlsUtils.createHash(signatureAndHashAlgorithm);
            SecurityParameters securityParameters = this.context.getSecurityParameters();
            createHash.update(securityParameters.clientRandom, 0, securityParameters.clientRandom.length);
            createHash.update(securityParameters.serverRandom, 0, securityParameters.serverRandom.length);
            digestInputBuffer.updateDigest(createHash);
            byte[] bArr = new byte[createHash.getDigestSize()];
            createHash.doFinal(bArr, 0);
            new DigitallySigned(signatureAndHashAlgorithm, this.serverCredentials.generateCertificateSignature(bArr)).encode(digestInputBuffer);
        }
        return digestInputBuffer.toByteArray();
    }

    public void init(TlsContext tlsContext) {
        super.init(tlsContext);
        if (this.tlsSigner != null) {
            this.tlsSigner.init(tlsContext);
        }
    }

    /* access modifiers changed from: protected */
    public Signer initVerifyer(TlsSigner tlsSigner2, SignatureAndHashAlgorithm signatureAndHashAlgorithm, SecurityParameters securityParameters) {
        Signer createVerifyer = tlsSigner2.createVerifyer(signatureAndHashAlgorithm, this.serverPublicKey);
        createVerifyer.update(securityParameters.clientRandom, 0, securityParameters.clientRandom.length);
        createVerifyer.update(securityParameters.serverRandom, 0, securityParameters.serverRandom.length);
        return createVerifyer;
    }

    public void processClientCredentials(TlsCredentials tlsCredentials) throws IOException {
        throw new TlsFatalAlert(80);
    }

    public void processClientKeyExchange(InputStream inputStream) throws IOException {
        try {
            this.srpPeerCredentials = SRP6Util.validatePublicValue(this.srpGroup.getN(), TlsSRPUtils.readSRPParameter(inputStream));
            this.context.getSecurityParameters().srpIdentity = Arrays.clone(this.identity);
        } catch (CryptoException e) {
            throw new TlsFatalAlert(47, e);
        }
    }

    public void processServerCertificate(Certificate certificate) throws IOException {
        if (this.tlsSigner == null) {
            throw new TlsFatalAlert(10);
        } else if (!certificate.isEmpty()) {
            Certificate certificateAt = certificate.getCertificateAt(0);
            try {
                this.serverPublicKey = PublicKeyFactory.createKey(certificateAt.getSubjectPublicKeyInfo());
                if (this.tlsSigner.isValidPublicKey(this.serverPublicKey)) {
                    TlsUtils.validateKeyUsage(certificateAt, 128);
                    super.processServerCertificate(certificate);
                    return;
                }
                throw new TlsFatalAlert(46);
            } catch (RuntimeException e) {
                throw new TlsFatalAlert(43, e);
            }
        } else {
            throw new TlsFatalAlert(42);
        }
    }

    public void processServerCredentials(TlsCredentials tlsCredentials) throws IOException {
        if (this.keyExchange == 21 || !(tlsCredentials instanceof TlsSignerCredentials)) {
            throw new TlsFatalAlert(80);
        }
        processServerCertificate(tlsCredentials.getCertificate());
        this.serverCredentials = (TlsSignerCredentials) tlsCredentials;
    }

    public void processServerKeyExchange(InputStream inputStream) throws IOException {
        InputStream inputStream2;
        SignerInputBuffer signerInputBuffer;
        SecurityParameters securityParameters = this.context.getSecurityParameters();
        if (this.tlsSigner != null) {
            signerInputBuffer = new SignerInputBuffer();
            inputStream2 = new TeeInputStream(inputStream, signerInputBuffer);
        } else {
            signerInputBuffer = null;
            inputStream2 = inputStream;
        }
        ServerSRPParams parse = ServerSRPParams.parse(inputStream2);
        if (signerInputBuffer != null) {
            DigitallySigned parseSignature = parseSignature(inputStream);
            Signer initVerifyer = initVerifyer(this.tlsSigner, parseSignature.getAlgorithm(), securityParameters);
            signerInputBuffer.updateSigner(initVerifyer);
            if (!initVerifyer.verifySignature(parseSignature.getSignature())) {
                throw new TlsFatalAlert(51);
            }
        }
        this.srpGroup = new SRP6GroupParameters(parse.getN(), parse.getG());
        if (this.groupVerifier.accept(this.srpGroup)) {
            this.srpSalt = parse.getS();
            try {
                this.srpPeerCredentials = SRP6Util.validatePublicValue(this.srpGroup.getN(), parse.getB());
                this.srpClient.init(this.srpGroup, TlsUtils.createHash(2), this.context.getSecureRandom());
            } catch (CryptoException e) {
                throw new TlsFatalAlert(47, e);
            }
        } else {
            throw new TlsFatalAlert(71);
        }
    }

    public boolean requiresServerKeyExchange() {
        return true;
    }

    public void skipServerCredentials() throws IOException {
        if (this.tlsSigner != null) {
            throw new TlsFatalAlert(10);
        }
    }

    public void validateCertificateRequest(CertificateRequest certificateRequest) throws IOException {
        throw new TlsFatalAlert(10);
    }
}
