package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.util.io.TeeInputStream;

public class TlsDHEKeyExchange extends TlsDHKeyExchange {
    protected TlsSignerCredentials serverCredentials = null;

    public TlsDHEKeyExchange(int i, Vector vector, DHParameters dHParameters) {
        super(i, vector, dHParameters);
    }

    public byte[] generateServerKeyExchange() throws IOException {
        if (this.dhParameters != null) {
            DigestInputBuffer digestInputBuffer = new DigestInputBuffer();
            this.dhAgreePrivateKey = TlsDHUtils.generateEphemeralServerKeyExchange(this.context.getSecureRandom(), this.dhParameters, digestInputBuffer);
            SignatureAndHashAlgorithm signatureAndHashAlgorithm = TlsUtils.getSignatureAndHashAlgorithm(this.context, this.serverCredentials);
            Digest createHash = TlsUtils.createHash(signatureAndHashAlgorithm);
            SecurityParameters securityParameters = this.context.getSecurityParameters();
            createHash.update(securityParameters.clientRandom, 0, securityParameters.clientRandom.length);
            createHash.update(securityParameters.serverRandom, 0, securityParameters.serverRandom.length);
            digestInputBuffer.updateDigest(createHash);
            byte[] bArr = new byte[createHash.getDigestSize()];
            createHash.doFinal(bArr, 0);
            new DigitallySigned(signatureAndHashAlgorithm, this.serverCredentials.generateCertificateSignature(bArr)).encode(digestInputBuffer);
            return digestInputBuffer.toByteArray();
        }
        throw new TlsFatalAlert(80);
    }

    /* access modifiers changed from: protected */
    public Signer initVerifyer(TlsSigner tlsSigner, SignatureAndHashAlgorithm signatureAndHashAlgorithm, SecurityParameters securityParameters) {
        Signer createVerifyer = tlsSigner.createVerifyer(signatureAndHashAlgorithm, this.serverPublicKey);
        createVerifyer.update(securityParameters.clientRandom, 0, securityParameters.clientRandom.length);
        createVerifyer.update(securityParameters.serverRandom, 0, securityParameters.serverRandom.length);
        return createVerifyer;
    }

    public void processServerCredentials(TlsCredentials tlsCredentials) throws IOException {
        if (tlsCredentials instanceof TlsSignerCredentials) {
            processServerCertificate(tlsCredentials.getCertificate());
            this.serverCredentials = (TlsSignerCredentials) tlsCredentials;
            return;
        }
        throw new TlsFatalAlert(80);
    }

    public void processServerKeyExchange(InputStream inputStream) throws IOException {
        SecurityParameters securityParameters = this.context.getSecurityParameters();
        SignerInputBuffer signerInputBuffer = new SignerInputBuffer();
        ServerDHParams parse = ServerDHParams.parse(new TeeInputStream(inputStream, signerInputBuffer));
        DigitallySigned parseSignature = parseSignature(inputStream);
        Signer initVerifyer = initVerifyer(this.tlsSigner, parseSignature.getAlgorithm(), securityParameters);
        signerInputBuffer.updateSigner(initVerifyer);
        if (initVerifyer.verifySignature(parseSignature.getSignature())) {
            this.dhAgreePublicKey = TlsDHUtils.validateDHPublicKey(parse.getPublicKey());
            this.dhParameters = validateDHParameters(this.dhAgreePublicKey.getParameters());
            return;
        }
        throw new TlsFatalAlert(51);
    }
}
