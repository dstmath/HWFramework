package org.bouncycastle.pqc.asn1;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.pqc.crypto.rainbow.Layer;
import org.bouncycastle.pqc.crypto.rainbow.util.RainbowUtil;

public class RainbowPrivateKey extends ASN1Object {
    private byte[] b1;
    private byte[] b2;
    private byte[][] invA1;
    private byte[][] invA2;
    private Layer[] layers;
    private ASN1ObjectIdentifier oid;
    private ASN1Integer version;
    private byte[] vi;

    private RainbowPrivateKey(ASN1Sequence aSN1Sequence) {
        ASN1Sequence aSN1Sequence2 = aSN1Sequence;
        int i = 0;
        if (aSN1Sequence2.getObjectAt(0) instanceof ASN1Integer) {
            this.version = ASN1Integer.getInstance(aSN1Sequence2.getObjectAt(0));
        } else {
            this.oid = ASN1ObjectIdentifier.getInstance(aSN1Sequence2.getObjectAt(0));
        }
        ASN1Sequence aSN1Sequence3 = (ASN1Sequence) aSN1Sequence2.getObjectAt(1);
        this.invA1 = new byte[aSN1Sequence3.size()][];
        for (int i2 = 0; i2 < aSN1Sequence3.size(); i2++) {
            this.invA1[i2] = ((ASN1OctetString) aSN1Sequence3.getObjectAt(i2)).getOctets();
        }
        this.b1 = ((ASN1OctetString) ((ASN1Sequence) aSN1Sequence2.getObjectAt(2)).getObjectAt(0)).getOctets();
        ASN1Sequence aSN1Sequence4 = (ASN1Sequence) aSN1Sequence2.getObjectAt(3);
        this.invA2 = new byte[aSN1Sequence4.size()][];
        for (int i3 = 0; i3 < aSN1Sequence4.size(); i3++) {
            this.invA2[i3] = ((ASN1OctetString) aSN1Sequence4.getObjectAt(i3)).getOctets();
        }
        this.b2 = ((ASN1OctetString) ((ASN1Sequence) aSN1Sequence2.getObjectAt(4)).getObjectAt(0)).getOctets();
        this.vi = ((ASN1OctetString) ((ASN1Sequence) aSN1Sequence2.getObjectAt(5)).getObjectAt(0)).getOctets();
        ASN1Sequence aSN1Sequence5 = (ASN1Sequence) aSN1Sequence2.getObjectAt(6);
        byte[][][][] bArr = new byte[aSN1Sequence5.size()][][][];
        byte[][][][] bArr2 = new byte[aSN1Sequence5.size()][][][];
        byte[][][] bArr3 = new byte[aSN1Sequence5.size()][][];
        byte[][] bArr4 = new byte[aSN1Sequence5.size()][];
        int i4 = 0;
        while (i4 < aSN1Sequence5.size()) {
            ASN1Sequence aSN1Sequence6 = (ASN1Sequence) aSN1Sequence5.getObjectAt(i4);
            ASN1Sequence aSN1Sequence7 = (ASN1Sequence) aSN1Sequence6.getObjectAt(i);
            bArr[i4] = new byte[aSN1Sequence7.size()][][];
            for (int i5 = i; i5 < aSN1Sequence7.size(); i5++) {
                ASN1Sequence aSN1Sequence8 = (ASN1Sequence) aSN1Sequence7.getObjectAt(i5);
                bArr[i4][i5] = new byte[aSN1Sequence8.size()][];
                for (int i6 = 0; i6 < aSN1Sequence8.size(); i6++) {
                    bArr[i4][i5][i6] = ((ASN1OctetString) aSN1Sequence8.getObjectAt(i6)).getOctets();
                }
            }
            ASN1Sequence aSN1Sequence9 = (ASN1Sequence) aSN1Sequence6.getObjectAt(1);
            bArr2[i4] = new byte[aSN1Sequence9.size()][][];
            for (int i7 = 0; i7 < aSN1Sequence9.size(); i7++) {
                ASN1Sequence aSN1Sequence10 = (ASN1Sequence) aSN1Sequence9.getObjectAt(i7);
                bArr2[i4][i7] = new byte[aSN1Sequence10.size()][];
                for (int i8 = 0; i8 < aSN1Sequence10.size(); i8++) {
                    bArr2[i4][i7][i8] = ((ASN1OctetString) aSN1Sequence10.getObjectAt(i8)).getOctets();
                }
            }
            ASN1Sequence aSN1Sequence11 = (ASN1Sequence) aSN1Sequence6.getObjectAt(2);
            bArr3[i4] = new byte[aSN1Sequence11.size()][];
            for (int i9 = 0; i9 < aSN1Sequence11.size(); i9++) {
                bArr3[i4][i9] = ((ASN1OctetString) aSN1Sequence11.getObjectAt(i9)).getOctets();
            }
            bArr4[i4] = ((ASN1OctetString) aSN1Sequence6.getObjectAt(3)).getOctets();
            i4++;
            i = 0;
        }
        int length = this.vi.length - 1;
        this.layers = new Layer[length];
        int i10 = 0;
        while (i10 < length) {
            int i11 = i10 + 1;
            Layer layer = new Layer(this.vi[i10], this.vi[i11], RainbowUtil.convertArray(bArr[i10]), RainbowUtil.convertArray(bArr2[i10]), RainbowUtil.convertArray(bArr3[i10]), RainbowUtil.convertArray(bArr4[i10]));
            this.layers[i10] = layer;
            i10 = i11;
        }
    }

    public RainbowPrivateKey(short[][] sArr, short[] sArr2, short[][] sArr3, short[] sArr4, int[] iArr, Layer[] layerArr) {
        this.version = new ASN1Integer(1);
        this.invA1 = RainbowUtil.convertArray(sArr);
        this.b1 = RainbowUtil.convertArray(sArr2);
        this.invA2 = RainbowUtil.convertArray(sArr3);
        this.b2 = RainbowUtil.convertArray(sArr4);
        this.vi = RainbowUtil.convertIntArray(iArr);
        this.layers = layerArr;
    }

    public static RainbowPrivateKey getInstance(Object obj) {
        if (obj instanceof RainbowPrivateKey) {
            return (RainbowPrivateKey) obj;
        }
        if (obj != null) {
            return new RainbowPrivateKey(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public short[] getB1() {
        return RainbowUtil.convertArray(this.b1);
    }

    public short[] getB2() {
        return RainbowUtil.convertArray(this.b2);
    }

    public short[][] getInvA1() {
        return RainbowUtil.convertArray(this.invA1);
    }

    public short[][] getInvA2() {
        return RainbowUtil.convertArray(this.invA2);
    }

    public Layer[] getLayers() {
        return this.layers;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public int[] getVi() {
        return RainbowUtil.convertArraytoInt(this.vi);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version != null ? this.version : this.oid);
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        for (byte[] dEROctetString : this.invA1) {
            aSN1EncodableVector2.add(new DEROctetString(dEROctetString));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        ASN1EncodableVector aSN1EncodableVector3 = new ASN1EncodableVector();
        aSN1EncodableVector3.add(new DEROctetString(this.b1));
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector3));
        ASN1EncodableVector aSN1EncodableVector4 = new ASN1EncodableVector();
        for (byte[] dEROctetString2 : this.invA2) {
            aSN1EncodableVector4.add(new DEROctetString(dEROctetString2));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector4));
        ASN1EncodableVector aSN1EncodableVector5 = new ASN1EncodableVector();
        aSN1EncodableVector5.add(new DEROctetString(this.b2));
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector5));
        ASN1EncodableVector aSN1EncodableVector6 = new ASN1EncodableVector();
        aSN1EncodableVector6.add(new DEROctetString(this.vi));
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector6));
        ASN1EncodableVector aSN1EncodableVector7 = new ASN1EncodableVector();
        for (int i = 0; i < this.layers.length; i++) {
            ASN1EncodableVector aSN1EncodableVector8 = new ASN1EncodableVector();
            byte[][][] convertArray = RainbowUtil.convertArray(this.layers[i].getCoeffAlpha());
            ASN1EncodableVector aSN1EncodableVector9 = new ASN1EncodableVector();
            for (int i2 = 0; i2 < convertArray.length; i2++) {
                ASN1EncodableVector aSN1EncodableVector10 = new ASN1EncodableVector();
                for (byte[] dEROctetString3 : convertArray[i2]) {
                    aSN1EncodableVector10.add(new DEROctetString(dEROctetString3));
                }
                aSN1EncodableVector9.add(new DERSequence(aSN1EncodableVector10));
            }
            aSN1EncodableVector8.add(new DERSequence(aSN1EncodableVector9));
            byte[][][] convertArray2 = RainbowUtil.convertArray(this.layers[i].getCoeffBeta());
            ASN1EncodableVector aSN1EncodableVector11 = new ASN1EncodableVector();
            for (int i3 = 0; i3 < convertArray2.length; i3++) {
                ASN1EncodableVector aSN1EncodableVector12 = new ASN1EncodableVector();
                for (byte[] dEROctetString4 : convertArray2[i3]) {
                    aSN1EncodableVector12.add(new DEROctetString(dEROctetString4));
                }
                aSN1EncodableVector11.add(new DERSequence(aSN1EncodableVector12));
            }
            aSN1EncodableVector8.add(new DERSequence(aSN1EncodableVector11));
            byte[][] convertArray3 = RainbowUtil.convertArray(this.layers[i].getCoeffGamma());
            ASN1EncodableVector aSN1EncodableVector13 = new ASN1EncodableVector();
            for (byte[] dEROctetString5 : convertArray3) {
                aSN1EncodableVector13.add(new DEROctetString(dEROctetString5));
            }
            aSN1EncodableVector8.add(new DERSequence(aSN1EncodableVector13));
            aSN1EncodableVector8.add(new DEROctetString(RainbowUtil.convertArray(this.layers[i].getCoeffEta())));
            aSN1EncodableVector7.add(new DERSequence(aSN1EncodableVector8));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector7));
        return new DERSequence(aSN1EncodableVector);
    }
}
