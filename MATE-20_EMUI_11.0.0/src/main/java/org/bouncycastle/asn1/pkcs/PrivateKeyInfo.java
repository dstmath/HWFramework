package org.bouncycastle.asn1.pkcs;

import java.io.IOException;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.BigIntegers;

public class PrivateKeyInfo extends ASN1Object {
    private ASN1Set attributes;
    private ASN1OctetString privateKey;
    private AlgorithmIdentifier privateKeyAlgorithm;
    private ASN1BitString publicKey;
    private ASN1Integer version;

    private PrivateKeyInfo(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.version = ASN1Integer.getInstance(objects.nextElement());
        int versionValue = getVersionValue(this.version);
        this.privateKeyAlgorithm = AlgorithmIdentifier.getInstance(objects.nextElement());
        this.privateKey = ASN1OctetString.getInstance(objects.nextElement());
        int i = -1;
        while (objects.hasMoreElements()) {
            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) objects.nextElement();
            int tagNo = aSN1TaggedObject.getTagNo();
            if (tagNo > i) {
                if (tagNo == 0) {
                    this.attributes = ASN1Set.getInstance(aSN1TaggedObject, false);
                } else if (tagNo != 1) {
                    throw new IllegalArgumentException("unknown optional field in private key info");
                } else if (versionValue >= 1) {
                    this.publicKey = DERBitString.getInstance(aSN1TaggedObject, false);
                } else {
                    throw new IllegalArgumentException("'publicKey' requires version v2(1) or later");
                }
                i = tagNo;
            } else {
                throw new IllegalArgumentException("invalid optional field in private key info");
            }
        }
    }

    public PrivateKeyInfo(AlgorithmIdentifier algorithmIdentifier, ASN1Encodable aSN1Encodable) throws IOException {
        this(algorithmIdentifier, aSN1Encodable, null, null);
    }

    public PrivateKeyInfo(AlgorithmIdentifier algorithmIdentifier, ASN1Encodable aSN1Encodable, ASN1Set aSN1Set) throws IOException {
        this(algorithmIdentifier, aSN1Encodable, aSN1Set, null);
    }

    public PrivateKeyInfo(AlgorithmIdentifier algorithmIdentifier, ASN1Encodable aSN1Encodable, ASN1Set aSN1Set, byte[] bArr) throws IOException {
        this.version = new ASN1Integer(bArr != null ? BigIntegers.ONE : BigIntegers.ZERO);
        this.privateKeyAlgorithm = algorithmIdentifier;
        this.privateKey = new DEROctetString(aSN1Encodable);
        this.attributes = aSN1Set;
        this.publicKey = bArr == null ? null : new DERBitString(bArr);
    }

    public static PrivateKeyInfo getInstance(Object obj) {
        if (obj instanceof PrivateKeyInfo) {
            return (PrivateKeyInfo) obj;
        }
        if (obj != null) {
            return new PrivateKeyInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static PrivateKeyInfo getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    private static int getVersionValue(ASN1Integer aSN1Integer) {
        int intValueExact = aSN1Integer.intValueExact();
        if (intValueExact >= 0 && intValueExact <= 1) {
            return intValueExact;
        }
        throw new IllegalArgumentException("invalid version for private key info");
    }

    public ASN1Set getAttributes() {
        return this.attributes;
    }

    public ASN1OctetString getPrivateKey() {
        return new DEROctetString(this.privateKey.getOctets());
    }

    public AlgorithmIdentifier getPrivateKeyAlgorithm() {
        return this.privateKeyAlgorithm;
    }

    public ASN1BitString getPublicKeyData() {
        return this.publicKey;
    }

    public boolean hasPublicKey() {
        return this.publicKey != null;
    }

    public ASN1Encodable parsePrivateKey() throws IOException {
        return ASN1Primitive.fromByteArray(this.privateKey.getOctets());
    }

    public ASN1Encodable parsePublicKey() throws IOException {
        ASN1BitString aSN1BitString = this.publicKey;
        if (aSN1BitString == null) {
            return null;
        }
        return ASN1Primitive.fromByteArray(aSN1BitString.getOctets());
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(5);
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.privateKeyAlgorithm);
        aSN1EncodableVector.add(this.privateKey);
        ASN1Set aSN1Set = this.attributes;
        if (aSN1Set != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, aSN1Set));
        }
        ASN1BitString aSN1BitString = this.publicKey;
        if (aSN1BitString != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, aSN1BitString));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
