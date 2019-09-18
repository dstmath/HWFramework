package org.bouncycastle.asn1.cryptopro;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.Arrays;

public class GostR3410TransportParameters extends ASN1Object {
    private final ASN1ObjectIdentifier encryptionParamSet;
    private final SubjectPublicKeyInfo ephemeralPublicKey;
    private final byte[] ukm;

    public GostR3410TransportParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier, SubjectPublicKeyInfo subjectPublicKeyInfo, byte[] bArr) {
        this.encryptionParamSet = aSN1ObjectIdentifier;
        this.ephemeralPublicKey = subjectPublicKeyInfo;
        this.ukm = Arrays.clone(bArr);
    }

    private GostR3410TransportParameters(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            this.encryptionParamSet = ASN1ObjectIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
            this.ukm = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(1)).getOctets();
            this.ephemeralPublicKey = null;
        } else if (aSN1Sequence.size() == 3) {
            this.encryptionParamSet = ASN1ObjectIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
            this.ephemeralPublicKey = SubjectPublicKeyInfo.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1)), false);
            this.ukm = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(2)).getOctets();
        } else {
            throw new IllegalArgumentException("unknown sequence length: " + aSN1Sequence.size());
        }
    }

    public static GostR3410TransportParameters getInstance(Object obj) {
        if (obj instanceof GostR3410TransportParameters) {
            return (GostR3410TransportParameters) obj;
        }
        if (obj != null) {
            return new GostR3410TransportParameters(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static GostR3410TransportParameters getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return new GostR3410TransportParameters(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public ASN1ObjectIdentifier getEncryptionParamSet() {
        return this.encryptionParamSet;
    }

    public SubjectPublicKeyInfo getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }

    public byte[] getUkm() {
        return Arrays.clone(this.ukm);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.encryptionParamSet);
        if (this.ephemeralPublicKey != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.ephemeralPublicKey));
        }
        aSN1EncodableVector.add(new DEROctetString(this.ukm));
        return new DERSequence(aSN1EncodableVector);
    }
}
