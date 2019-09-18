package org.bouncycastle.pqc.asn1;

import java.math.BigInteger;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.pqc.crypto.gmss.GMSSLeaf;
import org.bouncycastle.pqc.crypto.gmss.GMSSParameters;
import org.bouncycastle.pqc.crypto.gmss.GMSSRootCalc;
import org.bouncycastle.pqc.crypto.gmss.GMSSRootSig;
import org.bouncycastle.pqc.crypto.gmss.Treehash;

public class GMSSPrivateKey extends ASN1Object {
    private ASN1Primitive primitive;

    private GMSSPrivateKey(ASN1Sequence aSN1Sequence) {
        ASN1Sequence aSN1Sequence2 = (ASN1Sequence) aSN1Sequence.getObjectAt(0);
        int[] iArr = new int[aSN1Sequence2.size()];
        for (int i = 0; i < aSN1Sequence2.size(); i++) {
            iArr[i] = checkBigIntegerInIntRange(aSN1Sequence2.getObjectAt(i));
        }
        ASN1Sequence aSN1Sequence3 = (ASN1Sequence) aSN1Sequence.getObjectAt(1);
        byte[][] bArr = new byte[aSN1Sequence3.size()][];
        for (int i2 = 0; i2 < bArr.length; i2++) {
            bArr[i2] = ((DEROctetString) aSN1Sequence3.getObjectAt(i2)).getOctets();
        }
        ASN1Sequence aSN1Sequence4 = (ASN1Sequence) aSN1Sequence.getObjectAt(2);
        byte[][] bArr2 = new byte[aSN1Sequence4.size()][];
        for (int i3 = 0; i3 < bArr2.length; i3++) {
            bArr2[i3] = ((DEROctetString) aSN1Sequence4.getObjectAt(i3)).getOctets();
        }
        ASN1Sequence aSN1Sequence5 = (ASN1Sequence) aSN1Sequence.getObjectAt(3);
        byte[][][] bArr3 = new byte[aSN1Sequence5.size()][][];
        for (int i4 = 0; i4 < bArr3.length; i4++) {
            ASN1Sequence aSN1Sequence6 = (ASN1Sequence) aSN1Sequence5.getObjectAt(i4);
            bArr3[i4] = new byte[aSN1Sequence6.size()][];
            for (int i5 = 0; i5 < bArr3[i4].length; i5++) {
                bArr3[i4][i5] = ((DEROctetString) aSN1Sequence6.getObjectAt(i5)).getOctets();
            }
        }
        ASN1Sequence aSN1Sequence7 = (ASN1Sequence) aSN1Sequence.getObjectAt(4);
        byte[][][] bArr4 = new byte[aSN1Sequence7.size()][][];
        for (int i6 = 0; i6 < bArr4.length; i6++) {
            ASN1Sequence aSN1Sequence8 = (ASN1Sequence) aSN1Sequence7.getObjectAt(i6);
            bArr4[i6] = new byte[aSN1Sequence8.size()][];
            for (int i7 = 0; i7 < bArr4[i6].length; i7++) {
                bArr4[i6][i7] = ((DEROctetString) aSN1Sequence8.getObjectAt(i7)).getOctets();
            }
        }
        Treehash[][] treehashArr = new Treehash[((ASN1Sequence) aSN1Sequence.getObjectAt(5)).size()][];
    }

    public GMSSPrivateKey(int[] iArr, byte[][] bArr, byte[][] bArr2, byte[][][] bArr3, byte[][][] bArr4, Treehash[][] treehashArr, Treehash[][] treehashArr2, Vector[] vectorArr, Vector[] vectorArr2, Vector[][] vectorArr3, Vector[][] vectorArr4, byte[][][] bArr5, GMSSLeaf[] gMSSLeafArr, GMSSLeaf[] gMSSLeafArr2, GMSSLeaf[] gMSSLeafArr3, int[] iArr2, byte[][] bArr6, GMSSRootCalc[] gMSSRootCalcArr, byte[][] bArr7, GMSSRootSig[] gMSSRootSigArr, GMSSParameters gMSSParameters, AlgorithmIdentifier algorithmIdentifier) {
        this.primitive = encode(iArr, bArr, bArr2, bArr3, bArr4, bArr5, treehashArr, treehashArr2, vectorArr, vectorArr2, vectorArr3, vectorArr4, gMSSLeafArr, gMSSLeafArr2, gMSSLeafArr3, iArr2, bArr6, gMSSRootCalcArr, bArr7, gMSSRootSigArr, gMSSParameters, new AlgorithmIdentifier[]{algorithmIdentifier});
    }

    private static int checkBigIntegerInIntRange(ASN1Encodable aSN1Encodable) {
        BigInteger value = ((ASN1Integer) aSN1Encodable).getValue();
        if (value.compareTo(BigInteger.valueOf(2147483647L)) <= 0 && value.compareTo(BigInteger.valueOf(-2147483648L)) >= 0) {
            return value.intValue();
        }
        throw new IllegalArgumentException("BigInteger not in Range: " + value.toString());
    }

    private ASN1Primitive encode(int[] iArr, byte[][] bArr, byte[][] bArr2, byte[][][] bArr3, byte[][][] bArr4, byte[][][] bArr5, Treehash[][] treehashArr, Treehash[][] treehashArr2, Vector[] vectorArr, Vector[] vectorArr2, Vector[][] vectorArr3, Vector[][] vectorArr4, GMSSLeaf[] gMSSLeafArr, GMSSLeaf[] gMSSLeafArr2, GMSSLeaf[] gMSSLeafArr3, int[] iArr2, byte[][] bArr6, GMSSRootCalc[] gMSSRootCalcArr, byte[][] bArr7, GMSSRootSig[] gMSSRootSigArr, GMSSParameters gMSSParameters, AlgorithmIdentifier[] algorithmIdentifierArr) {
        char c;
        int[] iArr3 = iArr;
        byte[][] bArr8 = bArr;
        byte[][] bArr9 = bArr2;
        byte[][][] bArr10 = bArr3;
        byte[][][] bArr11 = bArr4;
        byte[][][] bArr12 = bArr5;
        Treehash[][] treehashArr3 = treehashArr;
        Treehash[][] treehashArr4 = treehashArr2;
        Vector[] vectorArr5 = vectorArr;
        Vector[] vectorArr6 = vectorArr2;
        Vector[][] vectorArr7 = vectorArr3;
        Vector[][] vectorArr8 = vectorArr4;
        GMSSLeaf[] gMSSLeafArr4 = gMSSLeafArr;
        GMSSLeaf[] gMSSLeafArr5 = gMSSLeafArr2;
        GMSSLeaf[] gMSSLeafArr6 = gMSSLeafArr3;
        int[] iArr4 = iArr2;
        AlgorithmIdentifier[] algorithmIdentifierArr2 = algorithmIdentifierArr;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        int i = 0;
        while (i < iArr3.length) {
            aSN1EncodableVector2.add(new ASN1Integer((long) iArr3[i]));
            i++;
            Vector[] vectorArr9 = vectorArr2;
            Vector[][] vectorArr10 = vectorArr3;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        ASN1EncodableVector aSN1EncodableVector3 = new ASN1EncodableVector();
        for (byte[] dEROctetString : bArr8) {
            aSN1EncodableVector3.add(new DEROctetString(dEROctetString));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector3));
        ASN1EncodableVector aSN1EncodableVector4 = new ASN1EncodableVector();
        for (byte[] dEROctetString2 : bArr9) {
            aSN1EncodableVector4.add(new DEROctetString(dEROctetString2));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector4));
        ASN1EncodableVector aSN1EncodableVector5 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector6 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector7 = aSN1EncodableVector5;
        for (int i2 = 0; i2 < bArr10.length; i2++) {
            for (byte[] dEROctetString3 : bArr10[i2]) {
                aSN1EncodableVector7.add(new DEROctetString(dEROctetString3));
            }
            aSN1EncodableVector6.add(new DERSequence(aSN1EncodableVector7));
            aSN1EncodableVector7 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector6));
        ASN1EncodableVector aSN1EncodableVector8 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector9 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector10 = aSN1EncodableVector8;
        for (int i3 = 0; i3 < bArr11.length; i3++) {
            for (byte[] dEROctetString4 : bArr11[i3]) {
                aSN1EncodableVector10.add(new DEROctetString(dEROctetString4));
            }
            aSN1EncodableVector9.add(new DERSequence(aSN1EncodableVector10));
            aSN1EncodableVector10 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector9));
        ASN1EncodableVector aSN1EncodableVector11 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector12 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector13 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector14 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector15 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector16 = aSN1EncodableVector14;
        ASN1EncodableVector aSN1EncodableVector17 = aSN1EncodableVector13;
        ASN1EncodableVector aSN1EncodableVector18 = aSN1EncodableVector12;
        int i4 = 0;
        while (i4 < treehashArr3.length) {
            ASN1EncodableVector aSN1EncodableVector19 = aSN1EncodableVector15;
            ASN1EncodableVector aSN1EncodableVector20 = aSN1EncodableVector16;
            ASN1EncodableVector aSN1EncodableVector21 = aSN1EncodableVector17;
            int i5 = 0;
            while (i5 < treehashArr3[i4].length) {
                aSN1EncodableVector21.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr2[0]));
                int i6 = treehashArr3[i4][i5].getStatInt()[1];
                aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i4][i5].getStatByte()[0]));
                aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i4][i5].getStatByte()[1]));
                aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i4][i5].getStatByte()[2]));
                int i7 = 0;
                while (i7 < i6) {
                    aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i4][i5].getStatByte()[3 + i7]));
                    i7++;
                    Vector[] vectorArr11 = vectorArr;
                }
                aSN1EncodableVector21.add(new DERSequence(aSN1EncodableVector20));
                aSN1EncodableVector20 = new ASN1EncodableVector();
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i4][i5].getStatInt()[0]));
                aSN1EncodableVector19.add(new ASN1Integer((long) i6));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i4][i5].getStatInt()[2]));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i4][i5].getStatInt()[3]));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i4][i5].getStatInt()[4]));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i4][i5].getStatInt()[5]));
                int i8 = 0;
                while (i8 < i6) {
                    aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i4][i5].getStatInt()[6 + i8]));
                    i8++;
                    byte[][][] bArr13 = bArr5;
                    treehashArr3 = treehashArr;
                }
                aSN1EncodableVector21.add(new DERSequence(aSN1EncodableVector19));
                aSN1EncodableVector19 = new ASN1EncodableVector();
                aSN1EncodableVector18.add(new DERSequence(aSN1EncodableVector21));
                aSN1EncodableVector21 = new ASN1EncodableVector();
                i5++;
                byte[][][] bArr14 = bArr5;
                treehashArr3 = treehashArr;
                Vector[] vectorArr12 = vectorArr;
            }
            aSN1EncodableVector11.add(new DERSequence(aSN1EncodableVector18));
            aSN1EncodableVector18 = new ASN1EncodableVector();
            i4++;
            aSN1EncodableVector17 = aSN1EncodableVector21;
            aSN1EncodableVector16 = aSN1EncodableVector20;
            aSN1EncodableVector15 = aSN1EncodableVector19;
            byte[][][] bArr15 = bArr5;
            treehashArr3 = treehashArr;
            Vector[] vectorArr13 = vectorArr;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector11));
        ASN1EncodableVector aSN1EncodableVector22 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector23 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector24 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector25 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector26 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector27 = aSN1EncodableVector25;
        ASN1EncodableVector aSN1EncodableVector28 = aSN1EncodableVector24;
        ASN1EncodableVector aSN1EncodableVector29 = aSN1EncodableVector23;
        int i9 = 0;
        while (i9 < treehashArr4.length) {
            ASN1EncodableVector aSN1EncodableVector30 = aSN1EncodableVector26;
            ASN1EncodableVector aSN1EncodableVector31 = aSN1EncodableVector27;
            ASN1EncodableVector aSN1EncodableVector32 = aSN1EncodableVector28;
            for (int i10 = 0; i10 < treehashArr4[i9].length; i10++) {
                aSN1EncodableVector32.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr2[0]));
                int i11 = treehashArr4[i9][i10].getStatInt()[1];
                aSN1EncodableVector31.add(new DEROctetString(treehashArr4[i9][i10].getStatByte()[0]));
                aSN1EncodableVector31.add(new DEROctetString(treehashArr4[i9][i10].getStatByte()[1]));
                aSN1EncodableVector31.add(new DEROctetString(treehashArr4[i9][i10].getStatByte()[2]));
                for (int i12 = 0; i12 < i11; i12++) {
                    aSN1EncodableVector31.add(new DEROctetString(treehashArr4[i9][i10].getStatByte()[3 + i12]));
                }
                aSN1EncodableVector32.add(new DERSequence(aSN1EncodableVector31));
                aSN1EncodableVector31 = new ASN1EncodableVector();
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr4[i9][i10].getStatInt()[0]));
                aSN1EncodableVector30.add(new ASN1Integer((long) i11));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr4[i9][i10].getStatInt()[2]));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr4[i9][i10].getStatInt()[3]));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr4[i9][i10].getStatInt()[4]));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr4[i9][i10].getStatInt()[5]));
                for (int i13 = 0; i13 < i11; i13++) {
                    aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr4[i9][i10].getStatInt()[6 + i13]));
                }
                aSN1EncodableVector32.add(new DERSequence(aSN1EncodableVector30));
                aSN1EncodableVector30 = new ASN1EncodableVector();
                aSN1EncodableVector29.add(new DERSequence(aSN1EncodableVector32));
                aSN1EncodableVector32 = new ASN1EncodableVector();
            }
            aSN1EncodableVector22.add(new DERSequence((ASN1Encodable) new DERSequence(aSN1EncodableVector29)));
            aSN1EncodableVector29 = new ASN1EncodableVector();
            i9++;
            aSN1EncodableVector28 = aSN1EncodableVector32;
            aSN1EncodableVector27 = aSN1EncodableVector31;
            aSN1EncodableVector26 = aSN1EncodableVector30;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector22));
        ASN1EncodableVector aSN1EncodableVector33 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector34 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector35 = aSN1EncodableVector33;
        byte[][][] bArr16 = bArr5;
        for (int i14 = 0; i14 < bArr16.length; i14++) {
            for (byte[] dEROctetString5 : bArr16[i14]) {
                aSN1EncodableVector35.add(new DEROctetString(dEROctetString5));
            }
            aSN1EncodableVector34.add(new DERSequence(aSN1EncodableVector35));
            aSN1EncodableVector35 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector34));
        ASN1EncodableVector aSN1EncodableVector36 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector37 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector38 = aSN1EncodableVector36;
        Vector[] vectorArr14 = vectorArr;
        for (int i15 = 0; i15 < vectorArr14.length; i15++) {
            for (int i16 = 0; i16 < vectorArr14[i15].size(); i16++) {
                aSN1EncodableVector38.add(new DEROctetString((byte[]) vectorArr14[i15].elementAt(i16)));
            }
            aSN1EncodableVector37.add(new DERSequence(aSN1EncodableVector38));
            aSN1EncodableVector38 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector37));
        ASN1EncodableVector aSN1EncodableVector39 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector40 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector41 = aSN1EncodableVector39;
        Vector[] vectorArr15 = vectorArr2;
        for (int i17 = 0; i17 < vectorArr15.length; i17++) {
            for (int i18 = 0; i18 < vectorArr15[i17].size(); i18++) {
                aSN1EncodableVector41.add(new DEROctetString((byte[]) vectorArr15[i17].elementAt(i18)));
            }
            aSN1EncodableVector40.add(new DERSequence(aSN1EncodableVector41));
            aSN1EncodableVector41 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector40));
        ASN1EncodableVector aSN1EncodableVector42 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector43 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector44 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector45 = aSN1EncodableVector42;
        ASN1EncodableVector aSN1EncodableVector46 = aSN1EncodableVector43;
        int i19 = 0;
        Vector[][] vectorArr16 = vectorArr3;
        while (i19 < vectorArr16.length) {
            ASN1EncodableVector aSN1EncodableVector47 = aSN1EncodableVector45;
            for (int i20 = 0; i20 < vectorArr16[i19].length; i20++) {
                for (int i21 = 0; i21 < vectorArr16[i19][i20].size(); i21++) {
                    aSN1EncodableVector47.add(new DEROctetString((byte[]) vectorArr16[i19][i20].elementAt(i21)));
                }
                aSN1EncodableVector46.add(new DERSequence(aSN1EncodableVector47));
                aSN1EncodableVector47 = new ASN1EncodableVector();
            }
            aSN1EncodableVector44.add(new DERSequence(aSN1EncodableVector46));
            aSN1EncodableVector46 = new ASN1EncodableVector();
            i19++;
            aSN1EncodableVector45 = aSN1EncodableVector47;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector44));
        ASN1EncodableVector aSN1EncodableVector48 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector49 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector50 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector51 = aSN1EncodableVector48;
        ASN1EncodableVector aSN1EncodableVector52 = aSN1EncodableVector49;
        int i22 = 0;
        Vector[][] vectorArr17 = vectorArr4;
        while (i22 < vectorArr17.length) {
            ASN1EncodableVector aSN1EncodableVector53 = aSN1EncodableVector51;
            for (int i23 = 0; i23 < vectorArr17[i22].length; i23++) {
                for (int i24 = 0; i24 < vectorArr17[i22][i23].size(); i24++) {
                    aSN1EncodableVector53.add(new DEROctetString((byte[]) vectorArr17[i22][i23].elementAt(i24)));
                }
                aSN1EncodableVector52.add(new DERSequence(aSN1EncodableVector53));
                aSN1EncodableVector53 = new ASN1EncodableVector();
            }
            aSN1EncodableVector50.add(new DERSequence(aSN1EncodableVector52));
            aSN1EncodableVector52 = new ASN1EncodableVector();
            i22++;
            aSN1EncodableVector51 = aSN1EncodableVector53;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector50));
        ASN1EncodableVector aSN1EncodableVector54 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector55 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector56 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector57 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector58 = aSN1EncodableVector55;
        GMSSLeaf[] gMSSLeafArr7 = gMSSLeafArr;
        for (int i25 = 0; i25 < gMSSLeafArr7.length; i25++) {
            aSN1EncodableVector58.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr2[0]));
            byte[][] statByte = gMSSLeafArr7[i25].getStatByte();
            aSN1EncodableVector56.add(new DEROctetString(statByte[0]));
            aSN1EncodableVector56.add(new DEROctetString(statByte[1]));
            aSN1EncodableVector56.add(new DEROctetString(statByte[2]));
            aSN1EncodableVector56.add(new DEROctetString(statByte[3]));
            aSN1EncodableVector58.add(new DERSequence(aSN1EncodableVector56));
            aSN1EncodableVector56 = new ASN1EncodableVector();
            int[] statInt = gMSSLeafArr7[i25].getStatInt();
            aSN1EncodableVector57.add(new ASN1Integer((long) statInt[0]));
            aSN1EncodableVector57.add(new ASN1Integer((long) statInt[1]));
            aSN1EncodableVector57.add(new ASN1Integer((long) statInt[2]));
            aSN1EncodableVector57.add(new ASN1Integer((long) statInt[3]));
            aSN1EncodableVector58.add(new DERSequence(aSN1EncodableVector57));
            aSN1EncodableVector57 = new ASN1EncodableVector();
            aSN1EncodableVector54.add(new DERSequence(aSN1EncodableVector58));
            aSN1EncodableVector58 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector54));
        ASN1EncodableVector aSN1EncodableVector59 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector60 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector61 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector62 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector63 = aSN1EncodableVector60;
        GMSSLeaf[] gMSSLeafArr8 = gMSSLeafArr2;
        for (int i26 = 0; i26 < gMSSLeafArr8.length; i26++) {
            aSN1EncodableVector63.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr2[0]));
            byte[][] statByte2 = gMSSLeafArr8[i26].getStatByte();
            aSN1EncodableVector61.add(new DEROctetString(statByte2[0]));
            aSN1EncodableVector61.add(new DEROctetString(statByte2[1]));
            aSN1EncodableVector61.add(new DEROctetString(statByte2[2]));
            aSN1EncodableVector61.add(new DEROctetString(statByte2[3]));
            aSN1EncodableVector63.add(new DERSequence(aSN1EncodableVector61));
            aSN1EncodableVector61 = new ASN1EncodableVector();
            int[] statInt2 = gMSSLeafArr8[i26].getStatInt();
            aSN1EncodableVector62.add(new ASN1Integer((long) statInt2[0]));
            aSN1EncodableVector62.add(new ASN1Integer((long) statInt2[1]));
            aSN1EncodableVector62.add(new ASN1Integer((long) statInt2[2]));
            aSN1EncodableVector62.add(new ASN1Integer((long) statInt2[3]));
            aSN1EncodableVector63.add(new DERSequence(aSN1EncodableVector62));
            aSN1EncodableVector62 = new ASN1EncodableVector();
            aSN1EncodableVector59.add(new DERSequence(aSN1EncodableVector63));
            aSN1EncodableVector63 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector59));
        ASN1EncodableVector aSN1EncodableVector64 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector65 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector66 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector67 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector68 = aSN1EncodableVector;
        GMSSLeaf[] gMSSLeafArr9 = gMSSLeafArr3;
        for (int i27 = 0; i27 < gMSSLeafArr9.length; i27++) {
            aSN1EncodableVector65.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr2[0]));
            byte[][] statByte3 = gMSSLeafArr9[i27].getStatByte();
            aSN1EncodableVector66.add(new DEROctetString(statByte3[0]));
            aSN1EncodableVector66.add(new DEROctetString(statByte3[1]));
            aSN1EncodableVector66.add(new DEROctetString(statByte3[2]));
            aSN1EncodableVector66.add(new DEROctetString(statByte3[3]));
            aSN1EncodableVector65.add(new DERSequence(aSN1EncodableVector66));
            aSN1EncodableVector66 = new ASN1EncodableVector();
            int[] statInt3 = gMSSLeafArr9[i27].getStatInt();
            aSN1EncodableVector67.add(new ASN1Integer((long) statInt3[0]));
            aSN1EncodableVector67.add(new ASN1Integer((long) statInt3[1]));
            aSN1EncodableVector67.add(new ASN1Integer((long) statInt3[2]));
            aSN1EncodableVector67.add(new ASN1Integer((long) statInt3[3]));
            aSN1EncodableVector65.add(new DERSequence(aSN1EncodableVector67));
            aSN1EncodableVector67 = new ASN1EncodableVector();
            aSN1EncodableVector64.add(new DERSequence(aSN1EncodableVector65));
            aSN1EncodableVector65 = new ASN1EncodableVector();
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector64));
        ASN1EncodableVector aSN1EncodableVector69 = new ASN1EncodableVector();
        AlgorithmIdentifier[] algorithmIdentifierArr3 = algorithmIdentifierArr2;
        int[] iArr5 = iArr2;
        for (int i28 : iArr5) {
            aSN1EncodableVector69.add(new ASN1Integer((long) i28));
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector69));
        ASN1EncodableVector aSN1EncodableVector70 = new ASN1EncodableVector();
        byte[][] bArr17 = bArr6;
        for (byte[] dEROctetString6 : bArr17) {
            aSN1EncodableVector70.add(new DEROctetString(dEROctetString6));
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector70));
        ASN1EncodableVector aSN1EncodableVector71 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector72 = new ASN1EncodableVector();
        new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector73 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector74 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector75 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector76 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector77 = aSN1EncodableVector73;
        ASN1EncodableVector aSN1EncodableVector78 = aSN1EncodableVector74;
        int i29 = 0;
        ASN1EncodableVector aSN1EncodableVector79 = aSN1EncodableVector72;
        GMSSRootCalc[] gMSSRootCalcArr2 = gMSSRootCalcArr;
        while (true) {
            c = 7;
            int i30 = 8;
            if (i29 >= gMSSRootCalcArr2.length) {
                break;
            }
            aSN1EncodableVector79.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr3[0]));
            new ASN1EncodableVector();
            int i31 = gMSSRootCalcArr2[i29].getStatInt()[0];
            int i32 = gMSSRootCalcArr2[i29].getStatInt()[7];
            aSN1EncodableVector77.add(new DEROctetString(gMSSRootCalcArr2[i29].getStatByte()[0]));
            int i33 = 0;
            while (i33 < i31) {
                i33 = 1 + i33;
                aSN1EncodableVector77.add(new DEROctetString(gMSSRootCalcArr2[i29].getStatByte()[i33]));
            }
            for (int i34 = 0; i34 < i32; i34++) {
                aSN1EncodableVector77.add(new DEROctetString(gMSSRootCalcArr2[i29].getStatByte()[1 + i31 + i34]));
            }
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector77));
            ASN1EncodableVector aSN1EncodableVector80 = new ASN1EncodableVector();
            aSN1EncodableVector78.add(new ASN1Integer((long) i31));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[1]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[2]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[3]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[4]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[5]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[6]));
            aSN1EncodableVector78.add(new ASN1Integer((long) i32));
            int i35 = 0;
            while (i35 < i31) {
                aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[i30 + i35]));
                i35++;
                i30 = 8;
            }
            for (int i36 = 0; i36 < i32; i36++) {
                aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getStatInt()[8 + i31 + i36]));
            }
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector78));
            ASN1EncodableVector aSN1EncodableVector81 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector82 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector83 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector84 = new ASN1EncodableVector();
            if (gMSSRootCalcArr2[i29].getTreehash() != null) {
                ASN1EncodableVector aSN1EncodableVector85 = aSN1EncodableVector84;
                ASN1EncodableVector aSN1EncodableVector86 = aSN1EncodableVector83;
                ASN1EncodableVector aSN1EncodableVector87 = aSN1EncodableVector82;
                int i37 = 0;
                while (i37 < gMSSRootCalcArr2[i29].getTreehash().length) {
                    aSN1EncodableVector87.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr3[0]));
                    int i38 = gMSSRootCalcArr2[i29].getTreehash()[i37].getStatInt()[1];
                    ASN1EncodableVector aSN1EncodableVector88 = aSN1EncodableVector80;
                    aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr2[i29].getTreehash()[i37].getStatByte()[0]));
                    aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr2[i29].getTreehash()[i37].getStatByte()[1]));
                    aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr2[i29].getTreehash()[i37].getStatByte()[2]));
                    int i39 = 0;
                    while (i39 < i38) {
                        aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr2[i29].getTreehash()[i37].getStatByte()[3 + i39]));
                        i39++;
                        aSN1EncodableVector81 = aSN1EncodableVector81;
                    }
                    ASN1EncodableVector aSN1EncodableVector89 = aSN1EncodableVector81;
                    aSN1EncodableVector87.add(new DERSequence(aSN1EncodableVector86));
                    aSN1EncodableVector86 = new ASN1EncodableVector();
                    ASN1EncodableVector aSN1EncodableVector90 = aSN1EncodableVector68;
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getTreehash()[i37].getStatInt()[0]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) i38));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getTreehash()[i37].getStatInt()[2]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getTreehash()[i37].getStatInt()[3]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getTreehash()[i37].getStatInt()[4]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getTreehash()[i37].getStatInt()[5]));
                    for (int i40 = 0; i40 < i38; i40++) {
                        aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr2[i29].getTreehash()[i37].getStatInt()[6 + i40]));
                    }
                    aSN1EncodableVector87.add(new DERSequence(aSN1EncodableVector85));
                    aSN1EncodableVector85 = new ASN1EncodableVector();
                    aSN1EncodableVector75.add(new DERSequence(aSN1EncodableVector87));
                    aSN1EncodableVector87 = new ASN1EncodableVector();
                    i37++;
                    aSN1EncodableVector80 = aSN1EncodableVector88;
                    aSN1EncodableVector81 = aSN1EncodableVector89;
                    aSN1EncodableVector68 = aSN1EncodableVector90;
                    algorithmIdentifierArr3 = algorithmIdentifierArr;
                }
            }
            ASN1EncodableVector aSN1EncodableVector91 = aSN1EncodableVector68;
            ASN1EncodableVector aSN1EncodableVector92 = aSN1EncodableVector80;
            ASN1EncodableVector aSN1EncodableVector93 = aSN1EncodableVector81;
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector75));
            aSN1EncodableVector75 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector94 = new ASN1EncodableVector();
            if (gMSSRootCalcArr2[i29].getRetain() != null) {
                ASN1EncodableVector aSN1EncodableVector95 = aSN1EncodableVector94;
                for (int i41 = 0; i41 < gMSSRootCalcArr2[i29].getRetain().length; i41++) {
                    for (int i42 = 0; i42 < gMSSRootCalcArr2[i29].getRetain()[i41].size(); i42++) {
                        aSN1EncodableVector95.add(new DEROctetString((byte[]) gMSSRootCalcArr2[i29].getRetain()[i41].elementAt(i42)));
                    }
                    aSN1EncodableVector76.add(new DERSequence(aSN1EncodableVector95));
                    aSN1EncodableVector95 = new ASN1EncodableVector();
                }
            }
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector76));
            aSN1EncodableVector76 = new ASN1EncodableVector();
            aSN1EncodableVector71.add(new DERSequence(aSN1EncodableVector79));
            aSN1EncodableVector79 = new ASN1EncodableVector();
            i29++;
            aSN1EncodableVector77 = aSN1EncodableVector92;
            aSN1EncodableVector78 = aSN1EncodableVector93;
            aSN1EncodableVector68 = aSN1EncodableVector91;
            algorithmIdentifierArr3 = algorithmIdentifierArr;
        }
        DERSequence dERSequence = new DERSequence(aSN1EncodableVector71);
        ASN1EncodableVector aSN1EncodableVector96 = aSN1EncodableVector68;
        aSN1EncodableVector96.add(dERSequence);
        ASN1EncodableVector aSN1EncodableVector97 = new ASN1EncodableVector();
        byte[][] bArr18 = bArr7;
        for (byte[] dEROctetString7 : bArr18) {
            aSN1EncodableVector97.add(new DEROctetString(dEROctetString7));
        }
        aSN1EncodableVector96.add(new DERSequence(aSN1EncodableVector97));
        ASN1EncodableVector aSN1EncodableVector98 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector99 = new ASN1EncodableVector();
        new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector100 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector101 = new ASN1EncodableVector();
        int i43 = 0;
        ASN1EncodableVector aSN1EncodableVector102 = aSN1EncodableVector99;
        GMSSRootSig[] gMSSRootSigArr2 = gMSSRootSigArr;
        while (i43 < gMSSRootSigArr2.length) {
            aSN1EncodableVector102.add(new DERSequence((ASN1Encodable) algorithmIdentifierArr[0]));
            new ASN1EncodableVector();
            aSN1EncodableVector100.add(new DEROctetString(gMSSRootSigArr2[i43].getStatByte()[0]));
            aSN1EncodableVector100.add(new DEROctetString(gMSSRootSigArr2[i43].getStatByte()[1]));
            aSN1EncodableVector100.add(new DEROctetString(gMSSRootSigArr2[i43].getStatByte()[2]));
            aSN1EncodableVector100.add(new DEROctetString(gMSSRootSigArr2[i43].getStatByte()[3]));
            aSN1EncodableVector100.add(new DEROctetString(gMSSRootSigArr2[i43].getStatByte()[4]));
            aSN1EncodableVector102.add(new DERSequence(aSN1EncodableVector100));
            aSN1EncodableVector100 = new ASN1EncodableVector();
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[0]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[1]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[2]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[3]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[4]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[5]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[6]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[c]));
            aSN1EncodableVector101.add(new ASN1Integer((long) gMSSRootSigArr2[i43].getStatInt()[8]));
            aSN1EncodableVector102.add(new DERSequence(aSN1EncodableVector101));
            aSN1EncodableVector101 = new ASN1EncodableVector();
            aSN1EncodableVector98.add(new DERSequence(aSN1EncodableVector102));
            aSN1EncodableVector102 = new ASN1EncodableVector();
            i43++;
            c = 7;
        }
        AlgorithmIdentifier[] algorithmIdentifierArr4 = algorithmIdentifierArr;
        aSN1EncodableVector96.add(new DERSequence(aSN1EncodableVector98));
        ASN1EncodableVector aSN1EncodableVector103 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector104 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector105 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector106 = new ASN1EncodableVector();
        for (int i44 = 0; i44 < gMSSParameters.getHeightOfTrees().length; i44++) {
            aSN1EncodableVector104.add(new ASN1Integer((long) gMSSParameters.getHeightOfTrees()[i44]));
            aSN1EncodableVector105.add(new ASN1Integer((long) gMSSParameters.getWinternitzParameter()[i44]));
            aSN1EncodableVector106.add(new ASN1Integer((long) gMSSParameters.getK()[i44]));
        }
        aSN1EncodableVector103.add(new ASN1Integer((long) gMSSParameters.getNumOfLayers()));
        aSN1EncodableVector103.add(new DERSequence(aSN1EncodableVector104));
        aSN1EncodableVector103.add(new DERSequence(aSN1EncodableVector105));
        aSN1EncodableVector103.add(new DERSequence(aSN1EncodableVector106));
        aSN1EncodableVector96.add(new DERSequence(aSN1EncodableVector103));
        ASN1EncodableVector aSN1EncodableVector107 = new ASN1EncodableVector();
        for (AlgorithmIdentifier add : algorithmIdentifierArr4) {
            aSN1EncodableVector107.add(add);
        }
        aSN1EncodableVector96.add(new DERSequence(aSN1EncodableVector107));
        return new DERSequence(aSN1EncodableVector96);
    }

    public ASN1Primitive toASN1Primitive() {
        return this.primitive;
    }
}
