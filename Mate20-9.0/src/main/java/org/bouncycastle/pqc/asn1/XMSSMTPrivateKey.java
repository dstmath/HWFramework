package org.bouncycastle.pqc.asn1;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.util.Arrays;

public class XMSSMTPrivateKey extends ASN1Object {
    private final byte[] bdsState;
    private final int index;
    private final byte[] publicSeed;
    private final byte[] root;
    private final byte[] secretKeyPRF;
    private final byte[] secretKeySeed;

    public XMSSMTPrivateKey(int i, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        this.index = i;
        this.secretKeySeed = Arrays.clone(bArr);
        this.secretKeyPRF = Arrays.clone(bArr2);
        this.publicSeed = Arrays.clone(bArr3);
        this.root = Arrays.clone(bArr4);
        this.bdsState = Arrays.clone(bArr5);
    }

    private XMSSMTPrivateKey(ASN1Sequence aSN1Sequence) {
        if (!ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).getValue().equals(BigInteger.valueOf(0))) {
            throw new IllegalArgumentException("unknown version of sequence");
        } else if (aSN1Sequence.size() == 2 || aSN1Sequence.size() == 3) {
            ASN1Sequence instance = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
            this.index = ASN1Integer.getInstance(instance.getObjectAt(0)).getValue().intValue();
            this.secretKeySeed = Arrays.clone(DEROctetString.getInstance(instance.getObjectAt(1)).getOctets());
            this.secretKeyPRF = Arrays.clone(DEROctetString.getInstance(instance.getObjectAt(2)).getOctets());
            this.publicSeed = Arrays.clone(DEROctetString.getInstance(instance.getObjectAt(3)).getOctets());
            this.root = Arrays.clone(DEROctetString.getInstance(instance.getObjectAt(4)).getOctets());
            this.bdsState = aSN1Sequence.size() == 3 ? Arrays.clone(DEROctetString.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(2)), true).getOctets()) : null;
        } else {
            throw new IllegalArgumentException("key sequence wrong size");
        }
    }

    public static XMSSMTPrivateKey getInstance(Object obj) {
        if (obj instanceof XMSSMTPrivateKey) {
            return (XMSSMTPrivateKey) obj;
        }
        if (obj != null) {
            return new XMSSMTPrivateKey(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public byte[] getBdsState() {
        return Arrays.clone(this.bdsState);
    }

    public int getIndex() {
        return this.index;
    }

    public byte[] getPublicSeed() {
        return Arrays.clone(this.publicSeed);
    }

    public byte[] getRoot() {
        return Arrays.clone(this.root);
    }

    public byte[] getSecretKeyPRF() {
        return Arrays.clone(this.secretKeyPRF);
    }

    public byte[] getSecretKeySeed() {
        return Arrays.clone(this.secretKeySeed);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new ASN1Integer(0));
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        aSN1EncodableVector2.add(new ASN1Integer((long) this.index));
        aSN1EncodableVector2.add(new DEROctetString(this.secretKeySeed));
        aSN1EncodableVector2.add(new DEROctetString(this.secretKeyPRF));
        aSN1EncodableVector2.add(new DEROctetString(this.publicSeed));
        aSN1EncodableVector2.add(new DEROctetString(this.root));
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        aSN1EncodableVector.add(new DERTaggedObject(true, 0, new DEROctetString(this.bdsState)));
        return new DERSequence(aSN1EncodableVector);
    }
}
