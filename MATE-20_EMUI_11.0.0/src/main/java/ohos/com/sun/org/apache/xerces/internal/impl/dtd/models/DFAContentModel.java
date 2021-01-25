package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

import java.io.PrintStream;
import java.util.HashMap;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;

public class DFAContentModel implements ContentModelValidator {
    private static final boolean DEBUG_VALIDATE_CONTENT = false;
    private static String fEOCString = fEOCString.intern();
    private static String fEpsilonString = fEpsilonString.intern();
    private int fEOCPos = 0;
    private QName[] fElemMap = null;
    private int fElemMapSize = 0;
    private int[] fElemMapType = null;
    private boolean fEmptyContentIsValid = false;
    private boolean[] fFinalStateFlags = null;
    private CMStateSet[] fFollowList = null;
    private CMNode fHeadNode = null;
    private int fLeafCount = 0;
    private CMLeaf[] fLeafList = null;
    private int[] fLeafListType = null;
    private boolean fMixed;
    private final QName fQName = new QName();
    private int[][] fTransTable = null;
    private int fTransTableSize = 0;

    public DFAContentModel(CMNode cMNode, int i, boolean z) {
        this.fLeafCount = i;
        this.fMixed = z;
        buildDFA(cMNode);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.ContentModelValidator
    public int validate(QName[] qNameArr, int i, int i2) {
        if (i2 == 0) {
            return this.fEmptyContentIsValid ? -1 : 0;
        }
        int i3 = 0;
        int i4 = 0;
        while (i3 < i2) {
            QName qName = qNameArr[i + i3];
            if (!this.fMixed || qName.localpart != null) {
                int i5 = 0;
                while (i5 < this.fElemMapSize) {
                    int i6 = this.fElemMapType[i5] & 15;
                    if (i6 != 0) {
                        if (i6 != 6) {
                            if (i6 != 8) {
                                if (i6 == 7 && this.fElemMap[i5].uri != qName.uri) {
                                    break;
                                }
                            } else if (qName.uri == null) {
                                break;
                            }
                        } else {
                            String str = this.fElemMap[i5].uri;
                            if (str == null || str == qName.uri) {
                                break;
                            }
                        }
                    } else if (this.fElemMap[i5].rawname == qName.rawname) {
                        break;
                    }
                    i5++;
                }
                if (i5 == this.fElemMapSize || (i4 = this.fTransTable[i4][i5]) == -1) {
                    return i3;
                }
            }
            i3++;
        }
        if (!this.fFinalStateFlags[i4]) {
            return i2;
        }
        return -1;
    }

    private void buildDFA(CMNode cMNode) {
        int i;
        int i2;
        int i3;
        int[] iArr;
        int i4;
        int i5;
        QName qName = this.fQName;
        String str = fEOCString;
        CMStateSet cMStateSet = null;
        qName.setValues(null, str, str, null);
        CMLeaf cMLeaf = new CMLeaf(this.fQName);
        this.fHeadNode = new CMBinOp(5, cMNode, cMLeaf);
        int i6 = this.fLeafCount;
        this.fEOCPos = i6;
        this.fLeafCount = i6 + 1;
        cMLeaf.setPosition(i6);
        int i7 = this.fLeafCount;
        this.fLeafList = new CMLeaf[i7];
        this.fLeafListType = new int[i7];
        postTreeBuildInit(this.fHeadNode, 0);
        this.fFollowList = new CMStateSet[this.fLeafCount];
        int i8 = 0;
        while (true) {
            int i9 = this.fLeafCount;
            if (i8 >= i9) {
                break;
            }
            this.fFollowList[i8] = new CMStateSet(i9);
            i8++;
        }
        calcFollowList(this.fHeadNode);
        int i10 = this.fLeafCount;
        this.fElemMap = new QName[i10];
        this.fElemMapType = new int[i10];
        this.fElemMapSize = 0;
        int i11 = 0;
        while (true) {
            i = this.fLeafCount;
            i2 = 1;
            if (i11 >= i) {
                break;
            }
            this.fElemMap[i11] = new QName();
            QName element = this.fLeafList[i11].getElement();
            int i12 = 0;
            while (i12 < this.fElemMapSize && this.fElemMap[i12].rawname != element.rawname) {
                i12++;
            }
            int i13 = this.fElemMapSize;
            if (i12 == i13) {
                this.fElemMap[i13].setValues(element);
                int[] iArr2 = this.fElemMapType;
                int i14 = this.fElemMapSize;
                iArr2[i14] = this.fLeafListType[i11];
                this.fElemMapSize = i14 + 1;
            }
            i11++;
        }
        int[] iArr3 = new int[(i + this.fElemMapSize)];
        int i15 = 0;
        int i16 = 0;
        while (true) {
            i3 = -1;
            if (i15 >= this.fElemMapSize) {
                break;
            }
            int i17 = i16;
            for (int i18 = 0; i18 < this.fLeafCount; i18++) {
                if (this.fLeafList[i18].getElement().rawname == this.fElemMap[i15].rawname) {
                    iArr3[i17] = i18;
                    i17++;
                }
            }
            i16 = i17 + 1;
            iArr3[i17] = -1;
            i15++;
        }
        int i19 = this.fLeafCount * 4;
        CMStateSet[] cMStateSetArr = new CMStateSet[i19];
        this.fFinalStateFlags = new boolean[i19];
        this.fTransTable = new int[i19][];
        CMStateSet firstPos = this.fHeadNode.firstPos();
        this.fTransTable[0] = makeDefStateList();
        cMStateSetArr[0] = firstPos;
        HashMap hashMap = new HashMap();
        int i20 = i19;
        int i21 = 0;
        while (i21 < i2) {
            CMStateSet cMStateSet2 = cMStateSetArr[i21];
            int[] iArr4 = this.fTransTable[i21];
            this.fFinalStateFlags[i21] = cMStateSet2.getBit(this.fEOCPos);
            i21++;
            int i22 = 0;
            CMStateSet cMStateSet3 = cMStateSet;
            int i23 = i20;
            int i24 = i2;
            int i25 = 0;
            while (i25 < this.fElemMapSize) {
                if (cMStateSet3 == null) {
                    cMStateSet3 = new CMStateSet(this.fLeafCount);
                } else {
                    cMStateSet3.zeroBits();
                }
                int i26 = i22 + 1;
                int i27 = iArr3[i22];
                while (i27 != i3) {
                    if (cMStateSet2.getBit(i27)) {
                        cMStateSet3.union(this.fFollowList[i27]);
                    }
                    i27 = iArr3[i26];
                    i26++;
                    i3 = -1;
                }
                if (!cMStateSet3.isEmpty()) {
                    Integer num = (Integer) hashMap.get(cMStateSet3);
                    if (num == null) {
                        i5 = i24;
                    } else {
                        i5 = num.intValue();
                    }
                    if (i5 == i24) {
                        cMStateSetArr[i24] = cMStateSet3;
                        this.fTransTable[i24] = makeDefStateList();
                        hashMap.put(cMStateSet3, new Integer(i24));
                        i24++;
                        cMStateSet3 = cMStateSet;
                    }
                    iArr4[i25] = i5;
                    if (i24 == i23) {
                        i4 = i21;
                        int i28 = (int) (((double) i23) * 1.5d);
                        CMStateSet[] cMStateSetArr2 = new CMStateSet[i28];
                        boolean[] zArr = new boolean[i28];
                        iArr = iArr3;
                        int[][] iArr5 = new int[i28][];
                        System.arraycopy(cMStateSetArr, 0, cMStateSetArr2, 0, i23);
                        System.arraycopy(this.fFinalStateFlags, 0, zArr, 0, i23);
                        System.arraycopy(this.fTransTable, 0, iArr5, 0, i23);
                        this.fFinalStateFlags = zArr;
                        this.fTransTable = iArr5;
                        i23 = i28;
                        cMStateSetArr = cMStateSetArr2;
                        i25++;
                        i21 = i4;
                        i22 = i26;
                        iArr3 = iArr;
                        cMStateSet = null;
                        i3 = -1;
                    }
                }
                iArr = iArr3;
                i4 = i21;
                i25++;
                i21 = i4;
                i22 = i26;
                iArr3 = iArr;
                cMStateSet = null;
                i3 = -1;
            }
            i2 = i24;
            i20 = i23;
        }
        this.fEmptyContentIsValid = ((CMBinOp) this.fHeadNode).getLeft().isNullable();
        this.fHeadNode = null;
        this.fLeafList = null;
        this.fFollowList = null;
    }

    private void calcFollowList(CMNode cMNode) {
        if (cMNode.type() == 4) {
            CMBinOp cMBinOp = (CMBinOp) cMNode;
            calcFollowList(cMBinOp.getLeft());
            calcFollowList(cMBinOp.getRight());
            return;
        }
        int i = 0;
        if (cMNode.type() == 5) {
            CMBinOp cMBinOp2 = (CMBinOp) cMNode;
            calcFollowList(cMBinOp2.getLeft());
            calcFollowList(cMBinOp2.getRight());
            CMStateSet lastPos = cMBinOp2.getLeft().lastPos();
            CMStateSet firstPos = cMBinOp2.getRight().firstPos();
            while (i < this.fLeafCount) {
                if (lastPos.getBit(i)) {
                    this.fFollowList[i].union(firstPos);
                }
                i++;
            }
        } else if (cMNode.type() == 2 || cMNode.type() == 3) {
            calcFollowList(((CMUniOp) cMNode).getChild());
            CMStateSet firstPos2 = cMNode.firstPos();
            CMStateSet lastPos2 = cMNode.lastPos();
            while (i < this.fLeafCount) {
                if (lastPos2.getBit(i)) {
                    this.fFollowList[i].union(firstPos2);
                }
                i++;
            }
        } else if (cMNode.type() == 1) {
            calcFollowList(((CMUniOp) cMNode).getChild());
        }
    }

    private void dumpTree(CMNode cMNode, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            System.out.print("   ");
        }
        int type = cMNode.type();
        if (type == 4 || type == 5) {
            if (type == 4) {
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
            CMBinOp cMBinOp = (CMBinOp) cMNode;
            int i3 = i + 1;
            dumpTree(cMBinOp.getLeft(), i3);
            dumpTree(cMBinOp.getRight(), i3);
        } else if (cMNode.type() == 2) {
            System.out.print("Rep Node ");
            if (cMNode.isNullable()) {
                System.out.print("Nullable ");
            }
            System.out.print("firstPos=");
            System.out.print(cMNode.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(cMNode.lastPos().toString());
            dumpTree(((CMUniOp) cMNode).getChild(), i + 1);
        } else if (cMNode.type() == 0) {
            PrintStream printStream = System.out;
            StringBuilder sb = new StringBuilder();
            sb.append("Leaf: (pos=");
            CMLeaf cMLeaf = (CMLeaf) cMNode;
            sb.append(cMLeaf.getPosition());
            sb.append("), ");
            sb.append(cMLeaf.getElement());
            sb.append("(elemIndex=");
            sb.append(cMLeaf.getElement());
            sb.append(") ");
            printStream.print(sb.toString());
            if (cMNode.isNullable()) {
                System.out.print(" Nullable ");
            }
            System.out.print("firstPos=");
            System.out.print(cMNode.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(cMNode.lastPos().toString());
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

    private int postTreeBuildInit(CMNode cMNode, int i) {
        cMNode.setMaxStates(this.fLeafCount);
        if ((cMNode.type() & 15) == 6 || (cMNode.type() & 15) == 8 || (cMNode.type() & 15) == 7) {
            CMAny cMAny = (CMAny) cMNode;
            this.fLeafList[i] = new CMLeaf(new QName(null, null, null, cMAny.getURI()), cMAny.getPosition());
            this.fLeafListType[i] = cMNode.type();
            return i + 1;
        } else if (cMNode.type() == 4 || cMNode.type() == 5) {
            CMBinOp cMBinOp = (CMBinOp) cMNode;
            return postTreeBuildInit(cMBinOp.getRight(), postTreeBuildInit(cMBinOp.getLeft(), i));
        } else if (cMNode.type() == 2 || cMNode.type() == 3 || cMNode.type() == 1) {
            return postTreeBuildInit(((CMUniOp) cMNode).getChild(), i);
        } else {
            if (cMNode.type() == 0) {
                CMLeaf cMLeaf = (CMLeaf) cMNode;
                if (cMLeaf.getElement().localpart == fEpsilonString) {
                    return i;
                }
                this.fLeafList[i] = cMLeaf;
                this.fLeafListType[i] = 0;
                return i + 1;
            }
            throw new RuntimeException("ImplementationMessages.VAL_NIICM: type=" + cMNode.type());
        }
    }
}
