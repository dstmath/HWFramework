package org.bouncycastle.pqc.asn1;

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
        return ((ASN1Integer) aSN1Encodable).intValueExact();
    }

    private ASN1Primitive encode(int[] iArr, byte[][] bArr, byte[][] bArr2, byte[][][] bArr3, byte[][][] bArr4, byte[][][] bArr5, Treehash[][] treehashArr, Treehash[][] treehashArr2, Vector[] vectorArr, Vector[] vectorArr2, Vector[][] vectorArr3, Vector[][] vectorArr4, GMSSLeaf[] gMSSLeafArr, GMSSLeaf[] gMSSLeafArr2, GMSSLeaf[] gMSSLeafArr3, int[] iArr2, byte[][] bArr6, GMSSRootCalc[] gMSSRootCalcArr, byte[][] bArr7, GMSSRootSig[] gMSSRootSigArr, GMSSParameters gMSSParameters, AlgorithmIdentifier[] algorithmIdentifierArr) {
        Treehash[][] treehashArr3 = treehashArr;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        for (int i : iArr) {
            aSN1EncodableVector2.add(new ASN1Integer((long) i));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        ASN1EncodableVector aSN1EncodableVector3 = new ASN1EncodableVector();
        for (byte[] bArr8 : bArr) {
            aSN1EncodableVector3.add(new DEROctetString(bArr8));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector3));
        ASN1EncodableVector aSN1EncodableVector4 = new ASN1EncodableVector();
        for (byte[] bArr9 : bArr2) {
            aSN1EncodableVector4.add(new DEROctetString(bArr9));
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector4));
        ASN1EncodableVector aSN1EncodableVector5 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector6 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector7 = aSN1EncodableVector5;
        for (int i2 = 0; i2 < bArr3.length; i2++) {
            for (int i3 = 0; i3 < bArr3[i2].length; i3++) {
                aSN1EncodableVector7.add(new DEROctetString(bArr3[i2][i3]));
            }
            aSN1EncodableVector6.add(new DERSequence(aSN1EncodableVector7));
            aSN1EncodableVector7 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector6));
        ASN1EncodableVector aSN1EncodableVector8 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector9 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector10 = aSN1EncodableVector8;
        for (int i4 = 0; i4 < bArr4.length; i4++) {
            for (int i5 = 0; i5 < bArr4[i4].length; i5++) {
                aSN1EncodableVector10.add(new DEROctetString(bArr4[i4][i5]));
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
        int i6 = 0;
        while (i6 < treehashArr3.length) {
            ASN1EncodableVector aSN1EncodableVector19 = aSN1EncodableVector15;
            ASN1EncodableVector aSN1EncodableVector20 = aSN1EncodableVector16;
            ASN1EncodableVector aSN1EncodableVector21 = aSN1EncodableVector17;
            int i7 = 0;
            while (i7 < treehashArr3[i6].length) {
                aSN1EncodableVector21.add(new DERSequence(algorithmIdentifierArr[0]));
                int i8 = treehashArr3[i6][i7].getStatInt()[1];
                aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i6][i7].getStatByte()[0]));
                aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i6][i7].getStatByte()[1]));
                aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i6][i7].getStatByte()[2]));
                for (int i9 = 0; i9 < i8; i9++) {
                    aSN1EncodableVector20.add(new DEROctetString(treehashArr3[i6][i7].getStatByte()[i9 + 3]));
                }
                aSN1EncodableVector21.add(new DERSequence(aSN1EncodableVector20));
                aSN1EncodableVector20 = new ASN1EncodableVector();
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i6][i7].getStatInt()[0]));
                aSN1EncodableVector19.add(new ASN1Integer((long) i8));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i6][i7].getStatInt()[2]));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i6][i7].getStatInt()[3]));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i6][i7].getStatInt()[4]));
                aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i6][i7].getStatInt()[5]));
                int i10 = 0;
                while (i10 < i8) {
                    aSN1EncodableVector19.add(new ASN1Integer((long) treehashArr3[i6][i7].getStatInt()[i10 + 6]));
                    i10++;
                    treehashArr3 = treehashArr;
                }
                aSN1EncodableVector21.add(new DERSequence(aSN1EncodableVector19));
                aSN1EncodableVector19 = new ASN1EncodableVector();
                aSN1EncodableVector18.add(new DERSequence(aSN1EncodableVector21));
                aSN1EncodableVector21 = new ASN1EncodableVector();
                i7++;
                treehashArr3 = treehashArr;
            }
            aSN1EncodableVector11.add(new DERSequence(aSN1EncodableVector18));
            aSN1EncodableVector18 = new ASN1EncodableVector();
            i6++;
            treehashArr3 = treehashArr;
            aSN1EncodableVector17 = aSN1EncodableVector21;
            aSN1EncodableVector16 = aSN1EncodableVector20;
            aSN1EncodableVector15 = aSN1EncodableVector19;
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
        int i11 = 0;
        while (i11 < treehashArr2.length) {
            ASN1EncodableVector aSN1EncodableVector30 = aSN1EncodableVector26;
            ASN1EncodableVector aSN1EncodableVector31 = aSN1EncodableVector27;
            ASN1EncodableVector aSN1EncodableVector32 = aSN1EncodableVector28;
            for (int i12 = 0; i12 < treehashArr2[i11].length; i12++) {
                aSN1EncodableVector32.add(new DERSequence(algorithmIdentifierArr[0]));
                int i13 = treehashArr2[i11][i12].getStatInt()[1];
                aSN1EncodableVector31.add(new DEROctetString(treehashArr2[i11][i12].getStatByte()[0]));
                aSN1EncodableVector31.add(new DEROctetString(treehashArr2[i11][i12].getStatByte()[1]));
                aSN1EncodableVector31.add(new DEROctetString(treehashArr2[i11][i12].getStatByte()[2]));
                for (int i14 = 0; i14 < i13; i14++) {
                    aSN1EncodableVector31.add(new DEROctetString(treehashArr2[i11][i12].getStatByte()[i14 + 3]));
                }
                aSN1EncodableVector32.add(new DERSequence(aSN1EncodableVector31));
                aSN1EncodableVector31 = new ASN1EncodableVector();
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr2[i11][i12].getStatInt()[0]));
                aSN1EncodableVector30.add(new ASN1Integer((long) i13));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr2[i11][i12].getStatInt()[2]));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr2[i11][i12].getStatInt()[3]));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr2[i11][i12].getStatInt()[4]));
                aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr2[i11][i12].getStatInt()[5]));
                for (int i15 = 0; i15 < i13; i15++) {
                    aSN1EncodableVector30.add(new ASN1Integer((long) treehashArr2[i11][i12].getStatInt()[i15 + 6]));
                }
                aSN1EncodableVector32.add(new DERSequence(aSN1EncodableVector30));
                aSN1EncodableVector30 = new ASN1EncodableVector();
                aSN1EncodableVector29.add(new DERSequence(aSN1EncodableVector32));
                aSN1EncodableVector32 = new ASN1EncodableVector();
            }
            aSN1EncodableVector22.add(new DERSequence(new DERSequence(aSN1EncodableVector29)));
            aSN1EncodableVector29 = new ASN1EncodableVector();
            i11++;
            aSN1EncodableVector28 = aSN1EncodableVector32;
            aSN1EncodableVector27 = aSN1EncodableVector31;
            aSN1EncodableVector26 = aSN1EncodableVector30;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector22));
        ASN1EncodableVector aSN1EncodableVector33 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector34 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector35 = aSN1EncodableVector33;
        for (int i16 = 0; i16 < bArr5.length; i16++) {
            for (int i17 = 0; i17 < bArr5[i16].length; i17++) {
                aSN1EncodableVector35.add(new DEROctetString(bArr5[i16][i17]));
            }
            aSN1EncodableVector34.add(new DERSequence(aSN1EncodableVector35));
            aSN1EncodableVector35 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector34));
        ASN1EncodableVector aSN1EncodableVector36 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector37 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector38 = aSN1EncodableVector36;
        for (int i18 = 0; i18 < vectorArr.length; i18++) {
            for (int i19 = 0; i19 < vectorArr[i18].size(); i19++) {
                aSN1EncodableVector38.add(new DEROctetString((byte[]) vectorArr[i18].elementAt(i19)));
            }
            aSN1EncodableVector37.add(new DERSequence(aSN1EncodableVector38));
            aSN1EncodableVector38 = new ASN1EncodableVector();
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector37));
        ASN1EncodableVector aSN1EncodableVector39 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector40 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector41 = aSN1EncodableVector39;
        for (int i20 = 0; i20 < vectorArr2.length; i20++) {
            for (int i21 = 0; i21 < vectorArr2[i20].size(); i21++) {
                aSN1EncodableVector41.add(new DEROctetString((byte[]) vectorArr2[i20].elementAt(i21)));
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
        int i22 = 0;
        while (i22 < vectorArr3.length) {
            ASN1EncodableVector aSN1EncodableVector47 = aSN1EncodableVector45;
            for (int i23 = 0; i23 < vectorArr3[i22].length; i23++) {
                for (int i24 = 0; i24 < vectorArr3[i22][i23].size(); i24++) {
                    aSN1EncodableVector47.add(new DEROctetString((byte[]) vectorArr3[i22][i23].elementAt(i24)));
                }
                aSN1EncodableVector46.add(new DERSequence(aSN1EncodableVector47));
                aSN1EncodableVector47 = new ASN1EncodableVector();
            }
            aSN1EncodableVector44.add(new DERSequence(aSN1EncodableVector46));
            aSN1EncodableVector46 = new ASN1EncodableVector();
            i22++;
            aSN1EncodableVector45 = aSN1EncodableVector47;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector44));
        ASN1EncodableVector aSN1EncodableVector48 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector49 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector50 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector51 = aSN1EncodableVector48;
        ASN1EncodableVector aSN1EncodableVector52 = aSN1EncodableVector49;
        int i25 = 0;
        while (i25 < vectorArr4.length) {
            ASN1EncodableVector aSN1EncodableVector53 = aSN1EncodableVector51;
            for (int i26 = 0; i26 < vectorArr4[i25].length; i26++) {
                for (int i27 = 0; i27 < vectorArr4[i25][i26].size(); i27++) {
                    aSN1EncodableVector53.add(new DEROctetString((byte[]) vectorArr4[i25][i26].elementAt(i27)));
                }
                aSN1EncodableVector52.add(new DERSequence(aSN1EncodableVector53));
                aSN1EncodableVector53 = new ASN1EncodableVector();
            }
            aSN1EncodableVector50.add(new DERSequence(aSN1EncodableVector52));
            aSN1EncodableVector52 = new ASN1EncodableVector();
            i25++;
            aSN1EncodableVector51 = aSN1EncodableVector53;
        }
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector50));
        ASN1EncodableVector aSN1EncodableVector54 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector55 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector56 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector57 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector58 = aSN1EncodableVector55;
        for (int i28 = 0; i28 < gMSSLeafArr.length; i28++) {
            aSN1EncodableVector58.add(new DERSequence(algorithmIdentifierArr[0]));
            byte[][] statByte = gMSSLeafArr[i28].getStatByte();
            aSN1EncodableVector56.add(new DEROctetString(statByte[0]));
            aSN1EncodableVector56.add(new DEROctetString(statByte[1]));
            aSN1EncodableVector56.add(new DEROctetString(statByte[2]));
            aSN1EncodableVector56.add(new DEROctetString(statByte[3]));
            aSN1EncodableVector58.add(new DERSequence(aSN1EncodableVector56));
            aSN1EncodableVector56 = new ASN1EncodableVector();
            int[] statInt = gMSSLeafArr[i28].getStatInt();
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
        for (int i29 = 0; i29 < gMSSLeafArr2.length; i29++) {
            aSN1EncodableVector63.add(new DERSequence(algorithmIdentifierArr[0]));
            byte[][] statByte2 = gMSSLeafArr2[i29].getStatByte();
            aSN1EncodableVector61.add(new DEROctetString(statByte2[0]));
            aSN1EncodableVector61.add(new DEROctetString(statByte2[1]));
            aSN1EncodableVector61.add(new DEROctetString(statByte2[2]));
            aSN1EncodableVector61.add(new DEROctetString(statByte2[3]));
            aSN1EncodableVector63.add(new DERSequence(aSN1EncodableVector61));
            aSN1EncodableVector61 = new ASN1EncodableVector();
            int[] statInt2 = gMSSLeafArr2[i29].getStatInt();
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
        for (int i30 = 0; i30 < gMSSLeafArr3.length; i30++) {
            aSN1EncodableVector65.add(new DERSequence(algorithmIdentifierArr[0]));
            byte[][] statByte3 = gMSSLeafArr3[i30].getStatByte();
            aSN1EncodableVector66.add(new DEROctetString(statByte3[0]));
            aSN1EncodableVector66.add(new DEROctetString(statByte3[1]));
            aSN1EncodableVector66.add(new DEROctetString(statByte3[2]));
            aSN1EncodableVector66.add(new DEROctetString(statByte3[3]));
            aSN1EncodableVector65.add(new DERSequence(aSN1EncodableVector66));
            aSN1EncodableVector66 = new ASN1EncodableVector();
            int[] statInt3 = gMSSLeafArr3[i30].getStatInt();
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
        AlgorithmIdentifier[] algorithmIdentifierArr2 = algorithmIdentifierArr;
        for (int i31 : iArr2) {
            aSN1EncodableVector69.add(new ASN1Integer((long) i31));
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector69));
        ASN1EncodableVector aSN1EncodableVector70 = new ASN1EncodableVector();
        for (byte[] bArr10 : bArr6) {
            aSN1EncodableVector70.add(new DEROctetString(bArr10));
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
        int i32 = 0;
        ASN1EncodableVector aSN1EncodableVector79 = aSN1EncodableVector72;
        while (i32 < gMSSRootCalcArr.length) {
            aSN1EncodableVector79.add(new DERSequence(algorithmIdentifierArr2[0]));
            new ASN1EncodableVector();
            int i33 = gMSSRootCalcArr[i32].getStatInt()[0];
            int i34 = gMSSRootCalcArr[i32].getStatInt()[7];
            aSN1EncodableVector77.add(new DEROctetString(gMSSRootCalcArr[i32].getStatByte()[0]));
            int i35 = 0;
            while (i35 < i33) {
                i35++;
                aSN1EncodableVector77.add(new DEROctetString(gMSSRootCalcArr[i32].getStatByte()[i35]));
            }
            for (int i36 = 0; i36 < i34; i36++) {
                aSN1EncodableVector77.add(new DEROctetString(gMSSRootCalcArr[i32].getStatByte()[i33 + 1 + i36]));
            }
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector77));
            ASN1EncodableVector aSN1EncodableVector80 = new ASN1EncodableVector();
            aSN1EncodableVector78.add(new ASN1Integer((long) i33));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[1]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[2]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[3]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[4]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[5]));
            aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[6]));
            aSN1EncodableVector78.add(new ASN1Integer((long) i34));
            for (int i37 = 0; i37 < i33; i37++) {
                aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[i37 + 8]));
            }
            for (int i38 = 0; i38 < i34; i38++) {
                aSN1EncodableVector78.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getStatInt()[i33 + 8 + i38]));
            }
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector78));
            ASN1EncodableVector aSN1EncodableVector81 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector82 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector83 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector84 = new ASN1EncodableVector();
            if (gMSSRootCalcArr[i32].getTreehash() != null) {
                ASN1EncodableVector aSN1EncodableVector85 = aSN1EncodableVector84;
                ASN1EncodableVector aSN1EncodableVector86 = aSN1EncodableVector83;
                ASN1EncodableVector aSN1EncodableVector87 = aSN1EncodableVector82;
                int i39 = 0;
                while (i39 < gMSSRootCalcArr[i32].getTreehash().length) {
                    aSN1EncodableVector87.add(new DERSequence(algorithmIdentifierArr2[0]));
                    int i40 = gMSSRootCalcArr[i32].getTreehash()[i39].getStatInt()[1];
                    aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr[i32].getTreehash()[i39].getStatByte()[0]));
                    aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr[i32].getTreehash()[i39].getStatByte()[1]));
                    aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr[i32].getTreehash()[i39].getStatByte()[2]));
                    int i41 = 0;
                    while (i41 < i40) {
                        aSN1EncodableVector86.add(new DEROctetString(gMSSRootCalcArr[i32].getTreehash()[i39].getStatByte()[i41 + 3]));
                        i41++;
                        aSN1EncodableVector81 = aSN1EncodableVector81;
                    }
                    aSN1EncodableVector87.add(new DERSequence(aSN1EncodableVector86));
                    aSN1EncodableVector86 = new ASN1EncodableVector();
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getTreehash()[i39].getStatInt()[0]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) i40));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getTreehash()[i39].getStatInt()[2]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getTreehash()[i39].getStatInt()[3]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getTreehash()[i39].getStatInt()[4]));
                    aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getTreehash()[i39].getStatInt()[5]));
                    for (int i42 = 0; i42 < i40; i42++) {
                        aSN1EncodableVector85.add(new ASN1Integer((long) gMSSRootCalcArr[i32].getTreehash()[i39].getStatInt()[i42 + 6]));
                    }
                    aSN1EncodableVector87.add(new DERSequence(aSN1EncodableVector85));
                    aSN1EncodableVector85 = new ASN1EncodableVector();
                    aSN1EncodableVector75.add(new DERSequence(aSN1EncodableVector87));
                    aSN1EncodableVector87 = new ASN1EncodableVector();
                    i39++;
                    aSN1EncodableVector80 = aSN1EncodableVector80;
                    aSN1EncodableVector81 = aSN1EncodableVector81;
                    algorithmIdentifierArr2 = algorithmIdentifierArr;
                    aSN1EncodableVector68 = aSN1EncodableVector68;
                }
            }
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector75));
            aSN1EncodableVector75 = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector88 = new ASN1EncodableVector();
            if (gMSSRootCalcArr[i32].getRetain() != null) {
                ASN1EncodableVector aSN1EncodableVector89 = aSN1EncodableVector88;
                for (int i43 = 0; i43 < gMSSRootCalcArr[i32].getRetain().length; i43++) {
                    for (int i44 = 0; i44 < gMSSRootCalcArr[i32].getRetain()[i43].size(); i44++) {
                        aSN1EncodableVector89.add(new DEROctetString((byte[]) gMSSRootCalcArr[i32].getRetain()[i43].elementAt(i44)));
                    }
                    aSN1EncodableVector76.add(new DERSequence(aSN1EncodableVector89));
                    aSN1EncodableVector89 = new ASN1EncodableVector();
                }
            }
            aSN1EncodableVector79.add(new DERSequence(aSN1EncodableVector76));
            aSN1EncodableVector76 = new ASN1EncodableVector();
            aSN1EncodableVector71.add(new DERSequence(aSN1EncodableVector79));
            aSN1EncodableVector79 = new ASN1EncodableVector();
            i32++;
            aSN1EncodableVector77 = aSN1EncodableVector80;
            aSN1EncodableVector78 = aSN1EncodableVector81;
            algorithmIdentifierArr2 = algorithmIdentifierArr;
            aSN1EncodableVector68 = aSN1EncodableVector68;
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector71));
        ASN1EncodableVector aSN1EncodableVector90 = new ASN1EncodableVector();
        for (byte[] bArr11 : bArr7) {
            aSN1EncodableVector90.add(new DEROctetString(bArr11));
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector90));
        ASN1EncodableVector aSN1EncodableVector91 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector92 = new ASN1EncodableVector();
        new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector93 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector94 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector95 = aSN1EncodableVector92;
        for (int i45 = 0; i45 < gMSSRootSigArr.length; i45++) {
            aSN1EncodableVector95.add(new DERSequence(algorithmIdentifierArr[0]));
            new ASN1EncodableVector();
            aSN1EncodableVector93.add(new DEROctetString(gMSSRootSigArr[i45].getStatByte()[0]));
            aSN1EncodableVector93.add(new DEROctetString(gMSSRootSigArr[i45].getStatByte()[1]));
            aSN1EncodableVector93.add(new DEROctetString(gMSSRootSigArr[i45].getStatByte()[2]));
            aSN1EncodableVector93.add(new DEROctetString(gMSSRootSigArr[i45].getStatByte()[3]));
            aSN1EncodableVector93.add(new DEROctetString(gMSSRootSigArr[i45].getStatByte()[4]));
            aSN1EncodableVector95.add(new DERSequence(aSN1EncodableVector93));
            aSN1EncodableVector93 = new ASN1EncodableVector();
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[0]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[1]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[2]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[3]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[4]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[5]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[6]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[7]));
            aSN1EncodableVector94.add(new ASN1Integer((long) gMSSRootSigArr[i45].getStatInt()[8]));
            aSN1EncodableVector95.add(new DERSequence(aSN1EncodableVector94));
            aSN1EncodableVector94 = new ASN1EncodableVector();
            aSN1EncodableVector91.add(new DERSequence(aSN1EncodableVector95));
            aSN1EncodableVector95 = new ASN1EncodableVector();
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector91));
        ASN1EncodableVector aSN1EncodableVector96 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector97 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector98 = new ASN1EncodableVector();
        ASN1EncodableVector aSN1EncodableVector99 = new ASN1EncodableVector();
        for (int i46 = 0; i46 < gMSSParameters.getHeightOfTrees().length; i46++) {
            aSN1EncodableVector97.add(new ASN1Integer((long) gMSSParameters.getHeightOfTrees()[i46]));
            aSN1EncodableVector98.add(new ASN1Integer((long) gMSSParameters.getWinternitzParameter()[i46]));
            aSN1EncodableVector99.add(new ASN1Integer((long) gMSSParameters.getK()[i46]));
        }
        aSN1EncodableVector96.add(new ASN1Integer((long) gMSSParameters.getNumOfLayers()));
        aSN1EncodableVector96.add(new DERSequence(aSN1EncodableVector97));
        aSN1EncodableVector96.add(new DERSequence(aSN1EncodableVector98));
        aSN1EncodableVector96.add(new DERSequence(aSN1EncodableVector99));
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector96));
        ASN1EncodableVector aSN1EncodableVector100 = new ASN1EncodableVector();
        for (AlgorithmIdentifier algorithmIdentifier : algorithmIdentifierArr) {
            aSN1EncodableVector100.add(algorithmIdentifier);
        }
        aSN1EncodableVector68.add(new DERSequence(aSN1EncodableVector100));
        return new DERSequence(aSN1EncodableVector68);
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        return this.primitive;
    }
}
