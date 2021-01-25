package ohos.global.icu.text;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.global.icu.impl.Assert;

class RBBINode {
    static final int endMark = 6;
    static int gLastSerial = 0;
    static final int leafChar = 3;
    static final int lookAhead = 4;
    static final int nodeTypeLimit = 16;
    static final String[] nodeTypeNames = {"setRef", "uset", "varRef", "leafChar", "lookAhead", "tag", "endMark", "opStart", "opCat", "opOr", "opStar", "opPlus", "opQuestion", "opBreak", "opReverse", "opLParen"};
    static final int opBreak = 13;
    static final int opCat = 8;
    static final int opLParen = 15;
    static final int opOr = 9;
    static final int opPlus = 11;
    static final int opQuestion = 12;
    static final int opReverse = 14;
    static final int opStar = 10;
    static final int opStart = 7;
    static final int precLParen = 2;
    static final int precOpCat = 4;
    static final int precOpOr = 3;
    static final int precStart = 1;
    static final int precZero = 0;
    static final int setRef = 0;
    static final int tag = 5;
    static final int uset = 1;
    static final int varRef = 2;
    boolean fChainIn;
    int fFirstPos;
    Set<RBBINode> fFirstPosSet;
    Set<RBBINode> fFollowPos;
    UnicodeSet fInputSet;
    int fLastPos;
    Set<RBBINode> fLastPosSet;
    RBBINode fLeftChild;
    boolean fLookAheadEnd;
    boolean fNullable;
    RBBINode fParent;
    int fPrecedence = 0;
    RBBINode fRightChild;
    boolean fRuleRoot;
    int fSerialNum;
    String fText;
    int fType;
    int fVal;

    RBBINode(int i) {
        Assert.assrt(i < 16);
        int i2 = gLastSerial + 1;
        gLastSerial = i2;
        this.fSerialNum = i2;
        this.fType = i;
        this.fFirstPosSet = new HashSet();
        this.fLastPosSet = new HashSet();
        this.fFollowPos = new HashSet();
        if (i == 8) {
            this.fPrecedence = 4;
        } else if (i == 9) {
            this.fPrecedence = 3;
        } else if (i == 7) {
            this.fPrecedence = 1;
        } else if (i == 15) {
            this.fPrecedence = 2;
        } else {
            this.fPrecedence = 0;
        }
    }

    RBBINode(RBBINode rBBINode) {
        int i = gLastSerial + 1;
        gLastSerial = i;
        this.fSerialNum = i;
        this.fType = rBBINode.fType;
        this.fInputSet = rBBINode.fInputSet;
        this.fPrecedence = rBBINode.fPrecedence;
        this.fText = rBBINode.fText;
        this.fFirstPos = rBBINode.fFirstPos;
        this.fLastPos = rBBINode.fLastPos;
        this.fNullable = rBBINode.fNullable;
        this.fVal = rBBINode.fVal;
        this.fRuleRoot = false;
        this.fChainIn = rBBINode.fChainIn;
        this.fFirstPosSet = new HashSet(rBBINode.fFirstPosSet);
        this.fLastPosSet = new HashSet(rBBINode.fLastPosSet);
        this.fFollowPos = new HashSet(rBBINode.fFollowPos);
    }

    /* access modifiers changed from: package-private */
    public RBBINode cloneTree() {
        int i = this.fType;
        if (i == 2) {
            return this.fLeftChild.cloneTree();
        }
        if (i == 1) {
            return this;
        }
        RBBINode rBBINode = new RBBINode(this);
        RBBINode rBBINode2 = this.fLeftChild;
        if (rBBINode2 != null) {
            rBBINode.fLeftChild = rBBINode2.cloneTree();
            rBBINode.fLeftChild.fParent = rBBINode;
        }
        RBBINode rBBINode3 = this.fRightChild;
        if (rBBINode3 != null) {
            rBBINode.fRightChild = rBBINode3.cloneTree();
            rBBINode.fRightChild.fParent = rBBINode;
        }
        return rBBINode;
    }

    /* access modifiers changed from: package-private */
    public RBBINode flattenVariables() {
        if (this.fType == 2) {
            RBBINode cloneTree = this.fLeftChild.cloneTree();
            cloneTree.fRuleRoot = this.fRuleRoot;
            cloneTree.fChainIn = this.fChainIn;
            return cloneTree;
        }
        RBBINode rBBINode = this.fLeftChild;
        if (rBBINode != null) {
            this.fLeftChild = rBBINode.flattenVariables();
            this.fLeftChild.fParent = this;
        }
        RBBINode rBBINode2 = this.fRightChild;
        if (rBBINode2 != null) {
            this.fRightChild = rBBINode2.flattenVariables();
            this.fRightChild.fParent = this;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public void flattenSets() {
        Assert.assrt(this.fType != 0);
        RBBINode rBBINode = this.fLeftChild;
        if (rBBINode != null) {
            if (rBBINode.fType == 0) {
                this.fLeftChild = rBBINode.fLeftChild.fLeftChild.cloneTree();
                this.fLeftChild.fParent = this;
            } else {
                rBBINode.flattenSets();
            }
        }
        RBBINode rBBINode2 = this.fRightChild;
        if (rBBINode2 == null) {
            return;
        }
        if (rBBINode2.fType == 0) {
            this.fRightChild = rBBINode2.fLeftChild.fLeftChild.cloneTree();
            this.fRightChild.fParent = this;
            return;
        }
        rBBINode2.flattenSets();
    }

    /* access modifiers changed from: package-private */
    public void findNodes(List<RBBINode> list, int i) {
        if (this.fType == i) {
            list.add(this);
        }
        RBBINode rBBINode = this.fLeftChild;
        if (rBBINode != null) {
            rBBINode.findNodes(list, i);
        }
        RBBINode rBBINode2 = this.fRightChild;
        if (rBBINode2 != null) {
            rBBINode2.findNodes(list, i);
        }
    }

    static void printNode(RBBINode rBBINode) {
        if (rBBINode == null) {
            System.out.print(" -- null --\n");
        } else {
            printInt(rBBINode.fSerialNum, 10);
            printString(nodeTypeNames[rBBINode.fType], 11);
            RBBINode rBBINode2 = rBBINode.fParent;
            int i = 0;
            printInt(rBBINode2 == null ? 0 : rBBINode2.fSerialNum, 11);
            RBBINode rBBINode3 = rBBINode.fLeftChild;
            printInt(rBBINode3 == null ? 0 : rBBINode3.fSerialNum, 11);
            RBBINode rBBINode4 = rBBINode.fRightChild;
            if (rBBINode4 != null) {
                i = rBBINode4.fSerialNum;
            }
            printInt(i, 12);
            printInt(rBBINode.fFirstPos, 12);
            printInt(rBBINode.fVal, 7);
            if (rBBINode.fType == 2) {
                PrintStream printStream = System.out;
                printStream.print(" " + rBBINode.fText);
            }
        }
        System.out.println("");
    }

    static void printString(String str, int i) {
        for (int i2 = i; i2 < 0; i2++) {
            System.out.print(' ');
        }
        for (int length = str.length(); length < i; length++) {
            System.out.print(' ');
        }
        System.out.print(str);
    }

    static void printInt(int i, int i2) {
        String num = Integer.toString(i);
        printString(num, Math.max(i2, num.length() + 1));
    }

    static void printHex(int i, int i2) {
        String num = Integer.toString(i, 16);
        String substring = "00000".substring(0, Math.max(0, 5 - num.length()));
        printString(substring + num, i2);
    }

    /* access modifiers changed from: package-private */
    public void printTree(boolean z) {
        if (z) {
            System.out.println("-------------------------------------------------------------------");
            System.out.println("    Serial       type     Parent  LeftChild  RightChild    position  value");
        }
        printNode(this);
        if (this.fType != 2) {
            RBBINode rBBINode = this.fLeftChild;
            if (rBBINode != null) {
                rBBINode.printTree(false);
            }
            RBBINode rBBINode2 = this.fRightChild;
            if (rBBINode2 != null) {
                rBBINode2.printTree(false);
            }
        }
    }
}
