package org.bouncycastle.jce.netscape;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public class NetscapeCertRequest extends ASN1Object {
    String challenge;
    DERBitString content;
    AlgorithmIdentifier keyAlg;
    PublicKey pubkey;
    AlgorithmIdentifier sigAlg;
    byte[] sigBits;

    public NetscapeCertRequest(String str, AlgorithmIdentifier algorithmIdentifier, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        this.challenge = str;
        this.sigAlg = algorithmIdentifier;
        this.pubkey = publicKey;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(getKeySpec());
        aSN1EncodableVector.add(new DERIA5String(str));
        try {
            this.content = new DERBitString((ASN1Encodable) new DERSequence(aSN1EncodableVector));
        } catch (IOException e) {
            throw new InvalidKeySpecException("exception encoding key: " + e.toString());
        }
    }

    public NetscapeCertRequest(ASN1Sequence aSN1Sequence) {
        try {
            if (aSN1Sequence.size() == 3) {
                this.sigAlg = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(1));
                this.sigBits = ((DERBitString) aSN1Sequence.getObjectAt(2)).getOctets();
                ASN1Sequence aSN1Sequence2 = (ASN1Sequence) aSN1Sequence.getObjectAt(0);
                if (aSN1Sequence2.size() == 2) {
                    this.challenge = ((DERIA5String) aSN1Sequence2.getObjectAt(1)).getString();
                    this.content = new DERBitString((ASN1Encodable) aSN1Sequence2);
                    SubjectPublicKeyInfo instance = SubjectPublicKeyInfo.getInstance(aSN1Sequence2.getObjectAt(0));
                    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(new DERBitString((ASN1Encodable) instance).getBytes());
                    this.keyAlg = instance.getAlgorithm();
                    this.pubkey = KeyFactory.getInstance(this.keyAlg.getAlgorithm().getId(), "BC").generatePublic(x509EncodedKeySpec);
                    return;
                }
                throw new IllegalArgumentException("invalid PKAC (len): " + aSN1Sequence2.size());
            }
            throw new IllegalArgumentException("invalid SPKAC (size):" + aSN1Sequence.size());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    public NetscapeCertRequest(byte[] bArr) throws IOException {
        this(getReq(bArr));
    }

    private ASN1Primitive getKeySpec() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(this.pubkey.getEncoded());
            byteArrayOutputStream.close();
            return new ASN1InputStream((InputStream) new ByteArrayInputStream(byteArrayOutputStream.toByteArray())).readObject();
        } catch (IOException e) {
            throw new InvalidKeySpecException(e.getMessage());
        }
    }

    private static ASN1Sequence getReq(byte[] bArr) throws IOException {
        return ASN1Sequence.getInstance(new ASN1InputStream((InputStream) new ByteArrayInputStream(bArr)).readObject());
    }

    public String getChallenge() {
        return this.challenge;
    }

    public AlgorithmIdentifier getKeyAlgorithm() {
        return this.keyAlg;
    }

    public PublicKey getPublicKey() {
        return this.pubkey;
    }

    public AlgorithmIdentifier getSigningAlgorithm() {
        return this.sigAlg;
    }

    public void setChallenge(String str) {
        this.challenge = str;
    }

    public void setKeyAlgorithm(AlgorithmIdentifier algorithmIdentifier) {
        this.keyAlg = algorithmIdentifier;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.pubkey = publicKey;
    }

    public void setSigningAlgorithm(AlgorithmIdentifier algorithmIdentifier) {
        this.sigAlg = algorithmIdentifier;
    }

    public void sign(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException, InvalidKeySpecException {
        sign(privateKey, null);
    }

    public void sign(PrivateKey privateKey, SecureRandom secureRandom) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException, InvalidKeySpecException {
        Signature instance = Signature.getInstance(this.sigAlg.getAlgorithm().getId(), "BC");
        if (secureRandom != null) {
            instance.initSign(privateKey, secureRandom);
        } else {
            instance.initSign(privateKey);
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(getKeySpec());
        aSN1EncodableVector.add(new DERIA5String(this.challenge));
        try {
            instance.update(new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER));
            this.sigBits = instance.sign();
        } catch (IOException e) {
            throw new SignatureException(e.getMessage());
        }
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        try {
            aSN1EncodableVector2.add(getKeySpec());
        } catch (Exception e) {
        }
        aSN1EncodableVector2.add(new DERIA5String(this.challenge));
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        aSN1EncodableVector.add(this.sigAlg);
        aSN1EncodableVector.add(new DERBitString(this.sigBits));
        return new DERSequence(aSN1EncodableVector);
    }

    public boolean verify(String str) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        if (!str.equals(this.challenge)) {
            return false;
        }
        Signature instance = Signature.getInstance(this.sigAlg.getAlgorithm().getId(), "BC");
        instance.initVerify(this.pubkey);
        instance.update(this.content.getBytes());
        return instance.verify(this.sigBits);
    }
}
