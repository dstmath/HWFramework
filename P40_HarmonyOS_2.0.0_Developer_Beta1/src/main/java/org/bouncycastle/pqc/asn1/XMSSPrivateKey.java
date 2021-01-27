package org.bouncycastle.pqc.asn1;

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
import org.bouncycastle.util.BigIntegers;

public class XMSSPrivateKey extends ASN1Object {
    private final byte[] bdsState;
    private final int index;
    private final int maxIndex;
    private final byte[] publicSeed;
    private final byte[] root;
    private final byte[] secretKeyPRF;
    private final byte[] secretKeySeed;
    private final int version;

    public XMSSPrivateKey(int i, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        this.version = 0;
        this.index = i;
        this.secretKeySeed = Arrays.clone(bArr);
        this.secretKeyPRF = Arrays.clone(bArr2);
        this.publicSeed = Arrays.clone(bArr3);
        this.root = Arrays.clone(bArr4);
        this.bdsState = Arrays.clone(bArr5);
        this.maxIndex = -1;
    }

    public XMSSPrivateKey(int i, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, int i2) {
        this.version = 1;
        this.index = i;
        this.secretKeySeed = Arrays.clone(bArr);
        this.secretKeyPRF = Arrays.clone(bArr2);
        this.publicSeed = Arrays.clone(bArr3);
        this.root = Arrays.clone(bArr4);
        this.bdsState = Arrays.clone(bArr5);
        this.maxIndex = i2;
    }

    private XMSSPrivateKey(ASN1Sequence aSN1Sequence) {
        int i;
        ASN1Integer instance = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        if (instance.hasValue(BigIntegers.ZERO) || instance.hasValue(BigIntegers.ONE)) {
            this.version = instance.intValueExact();
            if (aSN1Sequence.size() == 2 || aSN1Sequence.size() == 3) {
                ASN1Sequence instance2 = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
                this.index = ASN1Integer.getInstance(instance2.getObjectAt(0)).intValueExact();
                this.secretKeySeed = Arrays.clone(DEROctetString.getInstance(instance2.getObjectAt(1)).getOctets());
                this.secretKeyPRF = Arrays.clone(DEROctetString.getInstance(instance2.getObjectAt(2)).getOctets());
                this.publicSeed = Arrays.clone(DEROctetString.getInstance(instance2.getObjectAt(3)).getOctets());
                this.root = Arrays.clone(DEROctetString.getInstance(instance2.getObjectAt(4)).getOctets());
                if (instance2.size() == 6) {
                    ASN1TaggedObject instance3 = ASN1TaggedObject.getInstance(instance2.getObjectAt(5));
                    if (instance3.getTagNo() == 0) {
                        i = ASN1Integer.getInstance(instance3, false).intValueExact();
                    } else {
                        throw new IllegalArgumentException("unknown tag in XMSSPrivateKey");
                    }
                } else if (instance2.size() == 5) {
                    i = -1;
                } else {
                    throw new IllegalArgumentException("keySeq should be 5 or 6 in length");
                }
                this.maxIndex = i;
                this.bdsState = aSN1Sequence.size() == 3 ? Arrays.clone(DEROctetString.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(2)), true).getOctets()) : null;
                return;
            }
            throw new IllegalArgumentException("key sequence wrong size");
        }
        throw new IllegalArgumentException("unknown version of sequence");
    }

    public static XMSSPrivateKey getInstance(Object obj) {
        if (obj instanceof XMSSPrivateKey) {
            return (XMSSPrivateKey) obj;
        }
        if (obj != null) {
            return new XMSSPrivateKey(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public byte[] getBdsState() {
        return Arrays.clone(this.bdsState);
    }

    public int getIndex() {
        return this.index;
    }

    public int getMaxIndex() {
        return this.maxIndex;
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

    public int getVersion() {
        return this.version;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.maxIndex >= 0 ? new ASN1Integer(1) : new ASN1Integer(0));
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        aSN1EncodableVector2.add(new ASN1Integer((long) this.index));
        aSN1EncodableVector2.add(new DEROctetString(this.secretKeySeed));
        aSN1EncodableVector2.add(new DEROctetString(this.secretKeyPRF));
        aSN1EncodableVector2.add(new DEROctetString(this.publicSeed));
        aSN1EncodableVector2.add(new DEROctetString(this.root));
        int i = this.maxIndex;
        if (i >= 0) {
            aSN1EncodableVector2.add(new DERTaggedObject(false, 0, new ASN1Integer((long) i)));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        aSN1EncodableVector.add(new DERTaggedObject(true, 0, new DEROctetString(this.bdsState)));
        return new DERSequence(aSN1EncodableVector);
    }
}
