package org.bouncycastle.mozilla;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.mozilla.PublicKeyAndChallenge;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.operator.ContentVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Encodable;

public class SignedPublicKeyAndChallenge implements Encodable {
    protected final org.bouncycastle.asn1.mozilla.SignedPublicKeyAndChallenge spkacSeq;

    protected SignedPublicKeyAndChallenge(org.bouncycastle.asn1.mozilla.SignedPublicKeyAndChallenge signedPublicKeyAndChallenge) {
        this.spkacSeq = signedPublicKeyAndChallenge;
    }

    public SignedPublicKeyAndChallenge(byte[] bArr) {
        this.spkacSeq = org.bouncycastle.asn1.mozilla.SignedPublicKeyAndChallenge.getInstance(bArr);
    }

    public String getChallenge() {
        return this.spkacSeq.getPublicKeyAndChallenge().getChallenge().getString();
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return toASN1Structure().getEncoded();
    }

    public PublicKey getPublicKey(String str) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        SubjectPublicKeyInfo subjectPublicKeyInfo = this.spkacSeq.getPublicKeyAndChallenge().getSubjectPublicKeyInfo();
        try {
            return KeyFactory.getInstance(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId(), str).generatePublic(new X509EncodedKeySpec(new DERBitString(subjectPublicKeyInfo).getOctets()));
        } catch (Exception e) {
            throw new InvalidKeyException("error encoding public key");
        }
    }

    public PublicKeyAndChallenge getPublicKeyAndChallenge() {
        return this.spkacSeq.getPublicKeyAndChallenge();
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return this.spkacSeq.getPublicKeyAndChallenge().getSubjectPublicKeyInfo();
    }

    public boolean isSignatureValid(ContentVerifierProvider contentVerifierProvider) throws OperatorCreationException, IOException {
        ContentVerifier contentVerifier = contentVerifierProvider.get(this.spkacSeq.getSignatureAlgorithm());
        OutputStream outputStream = contentVerifier.getOutputStream();
        this.spkacSeq.getPublicKeyAndChallenge().encodeTo(outputStream, ASN1Encoding.DER);
        outputStream.close();
        return contentVerifier.verify(this.spkacSeq.getSignature().getOctets());
    }

    public ASN1Primitive toASN1Primitive() {
        return this.spkacSeq.toASN1Primitive();
    }

    public org.bouncycastle.asn1.mozilla.SignedPublicKeyAndChallenge toASN1Structure() {
        return this.spkacSeq;
    }

    public boolean verify() throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return verify(null);
    }

    public boolean verify(String str) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        String id = this.spkacSeq.getSignatureAlgorithm().getAlgorithm().getId();
        Signature instance = str == null ? Signature.getInstance(id) : Signature.getInstance(id, str);
        instance.initVerify(getPublicKey(str));
        try {
            instance.update(this.spkacSeq.getPublicKeyAndChallenge().getEncoded());
            return instance.verify(this.spkacSeq.getSignature().getBytes());
        } catch (Exception e) {
            throw new InvalidKeyException("error encoding public key");
        }
    }
}
