package ohos.com.sun.org.apache.xerces.internal.impl.xs.models;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMStateSet;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SubstitutionGroupHandler;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSConstraints;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class XSDFACM implements XSCMValidator {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_VALIDATE_CONTENT = false;
    private static long time;
    private Occurence[] fCountingStates = null;
    private Object[] fElemMap = null;
    private int[] fElemMapCounter;
    private int[] fElemMapCounterLowerBound;
    private int[] fElemMapCounterUpperBound;
    private int[] fElemMapId = null;
    private int fElemMapSize = 0;
    private int[] fElemMapType = null;
    private boolean[] fFinalStateFlags = null;
    private CMStateSet[] fFollowList = null;
    private CMNode fHeadNode = null;
    private int fLeafCount = 0;
    private XSCMLeaf[] fLeafList = null;
    private int[] fLeafListType = null;
    private int[][] fTransTable = null;
    private int fTransTableSize = 0;

    /* access modifiers changed from: package-private */
    public static final class Occurence {
        final int elemIndex;
        final int maxOccurs;
        final int minOccurs;

        public Occurence(XSCMRepeatingLeaf xSCMRepeatingLeaf, int i) {
            this.minOccurs = xSCMRepeatingLeaf.getMinOccurs();
            this.maxOccurs = xSCMRepeatingLeaf.getMaxOccurs();
            this.elemIndex = i;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("minOccurs=");
            sb.append(this.minOccurs);
            sb.append(";maxOccurs=");
            int i = this.maxOccurs;
            sb.append(i != -1 ? Integer.toString(i) : SchemaSymbols.ATTVAL_UNBOUNDED);
            return sb.toString();
        }
    }

    public XSDFACM(CMNode cMNode, int i) {
        this.fLeafCount = i;
        buildDFA(cMNode);
    }

    public boolean isFinalState(int i) {
        if (i < 0) {
            return false;
        }
        return this.fFinalStateFlags[i];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public Object oneTransition(QName qName, int[] iArr, SubstitutionGroupHandler substitutionGroupHandler) {
        int i;
        int i2 = iArr[0];
        if (i2 == -1 || i2 == -2) {
            if (i2 == -1) {
                iArr[0] = -2;
            }
            return findMatchingDecl(qName, substitutionGroupHandler);
        }
        int i3 = 0;
        Object obj = null;
        int i4 = 0;
        while (true) {
            i = 1;
            if (i4 >= this.fElemMapSize) {
                break;
            }
            i3 = this.fTransTable[i2][i4];
            if (i3 != -1) {
                int i5 = this.fElemMapType[i4];
                if (i5 == 1) {
                    obj = substitutionGroupHandler.getMatchingElemDecl(qName, (XSElementDecl) this.fElemMap[i4]);
                    if (obj != null) {
                        int[] iArr2 = this.fElemMapCounter;
                        if (iArr2[i4] >= 0) {
                            iArr2[i4] = iArr2[i4] + 1;
                        }
                    }
                } else if (i5 == 2 && ((XSWildcardDecl) this.fElemMap[i4]).allowNamespace(qName.uri)) {
                    obj = this.fElemMap[i4];
                    int[] iArr3 = this.fElemMapCounter;
                    if (iArr3[i4] >= 0) {
                        iArr3[i4] = iArr3[i4] + 1;
                    }
                }
            }
            i4++;
        }
        if (i4 == this.fElemMapSize) {
            iArr[1] = iArr[0];
            iArr[0] = -1;
            return findMatchingDecl(qName, substitutionGroupHandler);
        }
        Occurence[] occurenceArr = this.fCountingStates;
        if (occurenceArr != null) {
            Occurence occurence = occurenceArr[i2];
            if (occurence == null) {
                Occurence occurence2 = occurenceArr[i3];
                if (occurence2 != null) {
                    if (i4 != occurence2.elemIndex) {
                        i = 0;
                    }
                    iArr[2] = i;
                }
            } else if (i2 == i3) {
                int i6 = iArr[2] + 1;
                iArr[2] = i6;
                if (i6 > occurence.maxOccurs && occurence.maxOccurs != -1) {
                    return findMatchingDecl(qName, iArr, substitutionGroupHandler, i4);
                }
            } else if (iArr[2] < occurence.minOccurs) {
                iArr[1] = iArr[0];
                iArr[0] = -1;
                return findMatchingDecl(qName, substitutionGroupHandler);
            } else {
                Occurence occurence3 = this.fCountingStates[i3];
                if (occurence3 != null) {
                    if (i4 != occurence3.elemIndex) {
                        i = 0;
                    }
                    iArr[2] = i;
                }
            }
        }
        iArr[0] = i3;
        return obj;
    }

    /* access modifiers changed from: package-private */
    public Object findMatchingDecl(QName qName, SubstitutionGroupHandler substitutionGroupHandler) {
        for (int i = 0; i < this.fElemMapSize; i++) {
            int i2 = this.fElemMapType[i];
            if (i2 == 1) {
                XSElementDecl matchingElemDecl = substitutionGroupHandler.getMatchingElemDecl(qName, (XSElementDecl) this.fElemMap[i]);
                if (matchingElemDecl != null) {
                    return matchingElemDecl;
                }
            } else if (i2 == 2 && ((XSWildcardDecl) this.fElemMap[i]).allowNamespace(qName.uri)) {
                return this.fElemMap[i];
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Object findMatchingDecl(QName qName, int[] iArr, SubstitutionGroupHandler substitutionGroupHandler, int i) {
        int i2 = 0;
        int i3 = iArr[0];
        Object obj = null;
        int i4 = 0;
        while (true) {
            i++;
            if (i >= this.fElemMapSize) {
                break;
            }
            i4 = this.fTransTable[i3][i];
            if (i4 != -1) {
                int i5 = this.fElemMapType[i];
                if (i5 != 1) {
                    if (i5 == 2 && ((XSWildcardDecl) this.fElemMap[i]).allowNamespace(qName.uri)) {
                        obj = this.fElemMap[i];
                        break;
                    }
                } else {
                    obj = substitutionGroupHandler.getMatchingElemDecl(qName, (XSElementDecl) this.fElemMap[i]);
                    if (obj != null) {
                        break;
                    }
                }
            }
        }
        if (i == this.fElemMapSize) {
            iArr[1] = iArr[0];
            iArr[0] = -1;
            return findMatchingDecl(qName, substitutionGroupHandler);
        }
        iArr[0] = i4;
        Occurence occurence = this.fCountingStates[i4];
        if (occurence != null) {
            if (i == occurence.elemIndex) {
                i2 = 1;
            }
            iArr[2] = i2;
        }
        return obj;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public int[] startContentModel() {
        for (int i = 0; i < this.fElemMapSize; i++) {
            int[] iArr = this.fElemMapCounter;
            if (iArr[i] != -1) {
                iArr[i] = 0;
            }
        }
        return new int[3];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public boolean endContentModel(int[] iArr) {
        Occurence occurence;
        int i = iArr[0];
        if (!this.fFinalStateFlags[i]) {
            return false;
        }
        Occurence[] occurenceArr = this.fCountingStates;
        if (occurenceArr == null || (occurence = occurenceArr[i]) == null || iArr[2] >= occurence.minOccurs) {
            return true;
        }
        return false;
    }

    private void buildDFA(CMNode cMNode) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = this.fLeafCount;
        this.fLeafCount = i5 + 1;
        int i6 = -1;
        CMStateSet cMStateSet = null;
        this.fHeadNode = new XSCMBinOp(102, cMNode, new XSCMLeaf(1, null, -1, i5));
        int i7 = this.fLeafCount;
        this.fLeafList = new XSCMLeaf[i7];
        this.fLeafListType = new int[i7];
        postTreeBuildInit(this.fHeadNode);
        this.fFollowList = new CMStateSet[this.fLeafCount];
        int i8 = 0;
        int i9 = 0;
        while (true) {
            int i10 = this.fLeafCount;
            if (i9 >= i10) {
                break;
            }
            this.fFollowList[i9] = new CMStateSet(i10);
            i9++;
        }
        calcFollowList(this.fHeadNode);
        int i11 = this.fLeafCount;
        this.fElemMap = new Object[i11];
        this.fElemMapType = new int[i11];
        this.fElemMapId = new int[i11];
        this.fElemMapCounter = new int[i11];
        this.fElemMapCounterLowerBound = new int[i11];
        this.fElemMapCounterUpperBound = new int[i11];
        this.fElemMapSize = 0;
        int i12 = 0;
        Occurence[] occurenceArr = null;
        while (true) {
            i = this.fLeafCount;
            if (i12 >= i) {
                break;
            }
            this.fElemMap[i12] = null;
            int particleId = this.fLeafList[i12].getParticleId();
            int i13 = 0;
            while (i13 < this.fElemMapSize && particleId != this.fElemMapId[i13]) {
                i13++;
            }
            int i14 = this.fElemMapSize;
            if (i13 == i14) {
                XSCMLeaf xSCMLeaf = this.fLeafList[i12];
                this.fElemMap[i14] = xSCMLeaf.getLeaf();
                if (xSCMLeaf instanceof XSCMRepeatingLeaf) {
                    if (occurenceArr == null) {
                        occurenceArr = new Occurence[this.fLeafCount];
                    }
                    int i15 = this.fElemMapSize;
                    occurenceArr[i15] = new Occurence((XSCMRepeatingLeaf) xSCMLeaf, i15);
                }
                int[] iArr = this.fElemMapType;
                int i16 = this.fElemMapSize;
                iArr[i16] = this.fLeafListType[i12];
                this.fElemMapId[i16] = particleId;
                int[] iArr2 = (int[]) xSCMLeaf.getUserData();
                if (iArr2 != null) {
                    int[] iArr3 = this.fElemMapCounter;
                    int i17 = this.fElemMapSize;
                    iArr3[i17] = 0;
                    this.fElemMapCounterLowerBound[i17] = iArr2[0];
                    this.fElemMapCounterUpperBound[i17] = iArr2[1];
                } else {
                    int[] iArr4 = this.fElemMapCounter;
                    int i18 = this.fElemMapSize;
                    iArr4[i18] = -1;
                    this.fElemMapCounterLowerBound[i18] = -1;
                    this.fElemMapCounterUpperBound[i18] = -1;
                }
                this.fElemMapSize++;
            }
            i12++;
        }
        this.fElemMapSize--;
        int[] iArr5 = new int[(i + this.fElemMapSize)];
        int i19 = 0;
        for (int i20 = 0; i20 < this.fElemMapSize; i20++) {
            int i21 = this.fElemMapId[i20];
            int i22 = i19;
            for (int i23 = 0; i23 < this.fLeafCount; i23++) {
                if (i21 == this.fLeafList[i23].getParticleId()) {
                    iArr5[i22] = i23;
                    i22++;
                }
            }
            i19 = i22 + 1;
            iArr5[i22] = -1;
        }
        int i24 = this.fLeafCount * 4;
        CMStateSet[] cMStateSetArr = new CMStateSet[i24];
        this.fFinalStateFlags = new boolean[i24];
        this.fTransTable = new int[i24][];
        CMStateSet firstPos = this.fHeadNode.firstPos();
        this.fTransTable[0] = makeDefStateList();
        cMStateSetArr[0] = firstPos;
        HashMap hashMap = new HashMap();
        int i25 = i24;
        int i26 = 1;
        int i27 = 0;
        while (i27 < i26) {
            CMStateSet cMStateSet2 = cMStateSetArr[i27];
            int[] iArr6 = this.fTransTable[i27];
            this.fFinalStateFlags[i27] = cMStateSet2.getBit(i5);
            i27++;
            int i28 = i8;
            CMStateSet cMStateSet3 = cMStateSet;
            int i29 = i25;
            CMStateSet[] cMStateSetArr2 = cMStateSetArr;
            int i30 = i26;
            int i31 = i28;
            while (i31 < this.fElemMapSize) {
                if (cMStateSet3 == null) {
                    cMStateSet3 = new CMStateSet(this.fLeafCount);
                } else {
                    cMStateSet3.zeroBits();
                }
                int i32 = i28 + 1;
                int i33 = iArr5[i28];
                while (i33 != i6) {
                    if (cMStateSet2.getBit(i33)) {
                        cMStateSet3.union(this.fFollowList[i33]);
                    }
                    i33 = iArr5[i32];
                    i32++;
                    i6 = -1;
                }
                if (!cMStateSet3.isEmpty()) {
                    Integer num = (Integer) hashMap.get(cMStateSet3);
                    if (num == null) {
                        i4 = i30;
                    } else {
                        i4 = num.intValue();
                    }
                    if (i4 == i30) {
                        cMStateSetArr2[i30] = cMStateSet3;
                        this.fTransTable[i30] = makeDefStateList();
                        hashMap.put(cMStateSet3, new Integer(i30));
                        i30++;
                        cMStateSet3 = null;
                    }
                    iArr6[i31] = i4;
                    if (i30 == i29) {
                        i28 = i32;
                        int i34 = (int) (((double) i29) * 1.5d);
                        CMStateSet[] cMStateSetArr3 = new CMStateSet[i34];
                        i3 = i5;
                        boolean[] zArr = new boolean[i34];
                        i2 = i27;
                        int[][] iArr7 = new int[i34][];
                        System.arraycopy(cMStateSetArr2, 0, cMStateSetArr3, 0, i29);
                        System.arraycopy(this.fFinalStateFlags, 0, zArr, 0, i29);
                        System.arraycopy(this.fTransTable, 0, iArr7, 0, i29);
                        this.fFinalStateFlags = zArr;
                        this.fTransTable = iArr7;
                        cMStateSetArr2 = cMStateSetArr3;
                        i29 = i34;
                        i31++;
                        i5 = i3;
                        i27 = i2;
                        i6 = -1;
                    }
                }
                i3 = i5;
                i28 = i32;
                i2 = i27;
                i31++;
                i5 = i3;
                i27 = i2;
                i6 = -1;
            }
            i26 = i30;
            cMStateSetArr = cMStateSetArr2;
            i8 = 0;
            i25 = i29;
            cMStateSet = null;
        }
        if (occurenceArr != null) {
            this.fCountingStates = new Occurence[i26];
            for (int i35 = i8; i35 < i26; i35++) {
                int[] iArr8 = this.fTransTable[i35];
                int i36 = i8;
                while (true) {
                    if (i36 >= iArr8.length) {
                        break;
                    } else if (i35 == iArr8[i36]) {
                        this.fCountingStates[i35] = occurenceArr[i36];
                        break;
                    } else {
                        i36++;
                    }
                }
            }
        }
        this.fHeadNode = null;
        this.fLeafList = null;
        this.fFollowList = null;
        this.fLeafListType = null;
        this.fElemMapId = null;
    }

    private void calcFollowList(CMNode cMNode) {
        if (cMNode.type() == 101) {
            XSCMBinOp xSCMBinOp = (XSCMBinOp) cMNode;
            calcFollowList(xSCMBinOp.getLeft());
            calcFollowList(xSCMBinOp.getRight());
            return;
        }
        int i = 0;
        if (cMNode.type() == 102) {
            XSCMBinOp xSCMBinOp2 = (XSCMBinOp) cMNode;
            calcFollowList(xSCMBinOp2.getLeft());
            calcFollowList(xSCMBinOp2.getRight());
            CMStateSet lastPos = xSCMBinOp2.getLeft().lastPos();
            CMStateSet firstPos = xSCMBinOp2.getRight().firstPos();
            while (i < this.fLeafCount) {
                if (lastPos.getBit(i)) {
                    this.fFollowList[i].union(firstPos);
                }
                i++;
            }
        } else if (cMNode.type() == 4 || cMNode.type() == 6) {
            calcFollowList(((XSCMUniOp) cMNode).getChild());
            CMStateSet firstPos2 = cMNode.firstPos();
            CMStateSet lastPos2 = cMNode.lastPos();
            while (i < this.fLeafCount) {
                if (lastPos2.getBit(i)) {
                    this.fFollowList[i].union(firstPos2);
                }
                i++;
            }
        } else if (cMNode.type() == 5) {
            calcFollowList(((XSCMUniOp) cMNode).getChild());
        }
    }

    private void dumpTree(CMNode cMNode, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            System.out.print("   ");
        }
        int type = cMNode.type();
        if (type == 1) {
            PrintStream printStream = System.out;
            StringBuilder sb = new StringBuilder();
            sb.append("Leaf: (pos=");
            XSCMLeaf xSCMLeaf = (XSCMLeaf) cMNode;
            sb.append(xSCMLeaf.getPosition());
            sb.append("), (elemIndex=");
            sb.append(xSCMLeaf.getLeaf());
            sb.append(") ");
            printStream.print(sb.toString());
            if (cMNode.isNullable()) {
                System.out.print(" Nullable ");
            }
            System.out.print("firstPos=");
            System.out.print(cMNode.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(cMNode.lastPos().toString());
        } else if (type == 2) {
            System.out.print("Any Node: ");
            System.out.print("firstPos=");
            System.out.print(cMNode.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(cMNode.lastPos().toString());
        } else if (type == 4 || type == 5 || type == 6) {
            System.out.print("Rep Node ");
            if (cMNode.isNullable()) {
                System.out.print("Nullable ");
            }
            System.out.print("firstPos=");
            System.out.print(cMNode.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(cMNode.lastPos().toString());
            dumpTree(((XSCMUniOp) cMNode).getChild(), i + 1);
        } else if (type == 101 || type == 102) {
            if (type == 101) {
                System.out.print("Choice Node ");
            } else {
                System.out.print("Seq Node ");
            }
            if (cMNode.isNullable()) {
                System.out.print("Nullable ");
            }
            System.out.print("firstPos=");
            System.out.print(cMNode.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(cMNode.lastPos().toString());
            XSCMBinOp xSCMBinOp = (XSCMBinOp) cMNode;
            int i3 = i + 1;
            dumpTree(xSCMBinOp.getLeft(), i3);
            dumpTree(xSCMBinOp.getRight(), i3);
        } else {
            throw new RuntimeException("ImplementationMessages.VAL_NIICM");
        }
    }

    private int[] makeDefStateList() {
        int[] iArr = new int[this.fElemMapSize];
        for (int i = 0; i < this.fElemMapSize; i++) {
            iArr[i] = -1;
        }
        return iArr;
    }

    private void postTreeBuildInit(CMNode cMNode) throws RuntimeException {
        cMNode.setMaxStates(this.fLeafCount);
        if (cMNode.type() == 2) {
            XSCMLeaf xSCMLeaf = (XSCMLeaf) cMNode;
            int position = xSCMLeaf.getPosition();
            this.fLeafList[position] = xSCMLeaf;
            this.fLeafListType[position] = 2;
        } else if (cMNode.type() == 101 || cMNode.type() == 102) {
            XSCMBinOp xSCMBinOp = (XSCMBinOp) cMNode;
            postTreeBuildInit(xSCMBinOp.getLeft());
            postTreeBuildInit(xSCMBinOp.getRight());
        } else if (cMNode.type() == 4 || cMNode.type() == 6 || cMNode.type() == 5) {
            postTreeBuildInit(((XSCMUniOp) cMNode).getChild());
        } else if (cMNode.type() == 1) {
            XSCMLeaf xSCMLeaf2 = (XSCMLeaf) cMNode;
            int position2 = xSCMLeaf2.getPosition();
            this.fLeafList[position2] = xSCMLeaf2;
            this.fLeafListType[position2] = 1;
        } else {
            throw new RuntimeException("ImplementationMessages.VAL_NIICM");
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler substitutionGroupHandler) throws XMLSchemaException {
        Occurence occurence;
        int i = this.fElemMapSize;
        byte[][] bArr = (byte[][]) Array.newInstance(byte.class, i, i);
        int i2 = 0;
        while (true) {
            int[][] iArr = this.fTransTable;
            if (i2 >= iArr.length || iArr[i2] == null) {
                break;
            }
            int i3 = 0;
            while (i3 < this.fElemMapSize) {
                int i4 = i3 + 1;
                for (int i5 = i4; i5 < this.fElemMapSize; i5++) {
                    int[][] iArr2 = this.fTransTable;
                    if (!(iArr2[i2][i3] == -1 || iArr2[i2][i5] == -1 || bArr[i3][i5] != 0)) {
                        Object[] objArr = this.fElemMap;
                        if (XSConstraints.overlapUPA(objArr[i3], objArr[i5], substitutionGroupHandler)) {
                            Occurence[] occurenceArr = this.fCountingStates;
                            if (!(occurenceArr == null || (occurence = occurenceArr[i2]) == null)) {
                                if (((this.fTransTable[i2][i3] == i2) ^ (this.fTransTable[i2][i5] == i2)) && occurence.minOccurs == occurence.maxOccurs) {
                                    bArr[i3][i5] = -1;
                                }
                            }
                            bArr[i3][i5] = 1;
                        } else {
                            bArr[i3][i5] = -1;
                        }
                    }
                }
                i3 = i4;
            }
            i2++;
        }
        for (int i6 = 0; i6 < this.fElemMapSize; i6++) {
            for (int i7 = 0; i7 < this.fElemMapSize; i7++) {
                if (bArr[i6][i7] == 1) {
                    throw new XMLSchemaException("cos-nonambig", new Object[]{this.fElemMap[i6].toString(), this.fElemMap[i7].toString()});
                }
            }
        }
        for (int i8 = 0; i8 < this.fElemMapSize; i8++) {
            if (this.fElemMapType[i8] == 2) {
                XSWildcardDecl xSWildcardDecl = (XSWildcardDecl) this.fElemMap[i8];
                if (xSWildcardDecl.fType == 3 || xSWildcardDecl.fType == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public Vector whatCanGoHere(int[] iArr) {
        int i = iArr[0];
        if (i < 0) {
            i = iArr[1];
        }
        Occurence[] occurenceArr = this.fCountingStates;
        Occurence occurence = occurenceArr != null ? occurenceArr[i] : null;
        int i2 = iArr[2];
        Vector vector = new Vector();
        for (int i3 = 0; i3 < this.fElemMapSize; i3++) {
            int i4 = this.fTransTable[i][i3];
            if (i4 != -1) {
                if (occurence != null) {
                    if (i == i4) {
                        if (i2 >= occurence.maxOccurs && occurence.maxOccurs != -1) {
                        }
                    } else if (i2 < occurence.minOccurs) {
                    }
                }
                vector.addElement(this.fElemMap[i3]);
            }
        }
        return vector;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.models.XSCMValidator
    public ArrayList checkMinMaxBounds() {
        ArrayList arrayList = null;
        for (int i = 0; i < this.fElemMapSize; i++) {
            int i2 = this.fElemMapCounter[i];
            if (i2 != -1) {
                int i3 = this.fElemMapCounterLowerBound[i];
                int i4 = this.fElemMapCounterUpperBound[i];
                if (i2 < i3) {
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add("cvc-complex-type.2.4.b");
                    arrayList.add("{" + this.fElemMap[i] + "}");
                }
                if (i4 != -1 && i2 > i4) {
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add("cvc-complex-type.2.4.e");
                    arrayList.add("{" + this.fElemMap[i] + "}");
                }
            }
        }
        return arrayList;
    }
}
