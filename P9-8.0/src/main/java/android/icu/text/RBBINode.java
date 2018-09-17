package android.icu.text;

import android.icu.impl.Assert;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class RBBINode {
    static final int endMark = 6;
    static int gLastSerial = 0;
    static final int leafChar = 3;
    static final int lookAhead = 4;
    static final int nodeTypeLimit = 16;
    static final String[] nodeTypeNames = new String[]{"setRef", "uset", "varRef", "leafChar", "lookAhead", "tag", "endMark", "opStart", "opCat", "opOr", "opStar", "opPlus", "opQuestion", "opBreak", "opReverse", "opLParen"};
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

    RBBINode(int t) {
        Assert.assrt(t < 16);
        int i = gLastSerial + 1;
        gLastSerial = i;
        this.fSerialNum = i;
        this.fType = t;
        this.fFirstPosSet = new HashSet();
        this.fLastPosSet = new HashSet();
        this.fFollowPos = new HashSet();
        if (t == 8) {
            this.fPrecedence = 4;
        } else if (t == 9) {
            this.fPrecedence = 3;
        } else if (t == 7) {
            this.fPrecedence = 1;
        } else if (t == 15) {
            this.fPrecedence = 2;
        } else {
            this.fPrecedence = 0;
        }
    }

    RBBINode(RBBINode other) {
        int i = gLastSerial + 1;
        gLastSerial = i;
        this.fSerialNum = i;
        this.fType = other.fType;
        this.fInputSet = other.fInputSet;
        this.fPrecedence = other.fPrecedence;
        this.fText = other.fText;
        this.fFirstPos = other.fFirstPos;
        this.fLastPos = other.fLastPos;
        this.fNullable = other.fNullable;
        this.fVal = other.fVal;
        this.fRuleRoot = false;
        this.fChainIn = other.fChainIn;
        this.fFirstPosSet = new HashSet(other.fFirstPosSet);
        this.fLastPosSet = new HashSet(other.fLastPosSet);
        this.fFollowPos = new HashSet(other.fFollowPos);
    }

    RBBINode cloneTree() {
        if (this.fType == 2) {
            return this.fLeftChild.cloneTree();
        }
        if (this.fType == 1) {
            return this;
        }
        RBBINode n = new RBBINode(this);
        if (this.fLeftChild != null) {
            n.fLeftChild = this.fLeftChild.cloneTree();
            n.fLeftChild.fParent = n;
        }
        if (this.fRightChild == null) {
            return n;
        }
        n.fRightChild = this.fRightChild.cloneTree();
        n.fRightChild.fParent = n;
        return n;
    }

    RBBINode flattenVariables() {
        if (this.fType == 2) {
            RBBINode retNode = this.fLeftChild.cloneTree();
            retNode.fRuleRoot = this.fRuleRoot;
            retNode.fChainIn = this.fChainIn;
            return retNode;
        }
        if (this.fLeftChild != null) {
            this.fLeftChild = this.fLeftChild.flattenVariables();
            this.fLeftChild.fParent = this;
        }
        if (this.fRightChild != null) {
            this.fRightChild = this.fRightChild.flattenVariables();
            this.fRightChild.fParent = this;
        }
        return this;
    }

    void flattenSets() {
        boolean z = false;
        if (this.fType != 0) {
            z = true;
        }
        Assert.assrt(z);
        if (this.fLeftChild != null) {
            if (this.fLeftChild.fType == 0) {
                this.fLeftChild = this.fLeftChild.fLeftChild.fLeftChild.cloneTree();
                this.fLeftChild.fParent = this;
            } else {
                this.fLeftChild.flattenSets();
            }
        }
        if (this.fRightChild == null) {
            return;
        }
        if (this.fRightChild.fType == 0) {
            this.fRightChild = this.fRightChild.fLeftChild.fLeftChild.cloneTree();
            this.fRightChild.fParent = this;
            return;
        }
        this.fRightChild.flattenSets();
    }

    void findNodes(List<RBBINode> dest, int kind) {
        if (this.fType == kind) {
            dest.add(this);
        }
        if (this.fLeftChild != null) {
            this.fLeftChild.findNodes(dest, kind);
        }
        if (this.fRightChild != null) {
            this.fRightChild.findNodes(dest, kind);
        }
    }

    static void printNode(RBBINode n) {
        int i = 0;
        if (n == null) {
            System.out.print(" -- null --\n");
        } else {
            printInt(n.fSerialNum, 10);
            printString(nodeTypeNames[n.fType], 11);
            printInt(n.fParent == null ? 0 : n.fParent.fSerialNum, 11);
            printInt(n.fLeftChild == null ? 0 : n.fLeftChild.fSerialNum, 11);
            if (n.fRightChild != null) {
                i = n.fRightChild.fSerialNum;
            }
            printInt(i, 12);
            printInt(n.fFirstPos, 12);
            printInt(n.fVal, 7);
            if (n.fType == 2) {
                System.out.print(" " + n.fText);
            }
        }
        System.out.println("");
    }

    static void printString(String s, int minWidth) {
        int i;
        for (i = minWidth; i < 0; i++) {
            System.out.print(' ');
        }
        for (i = s.length(); i < minWidth; i++) {
            System.out.print(' ');
        }
        System.out.print(s);
    }

    static void printInt(int i, int minWidth) {
        String s = Integer.toString(i);
        printString(s, Math.max(minWidth, s.length() + 1));
    }

    static void printHex(int i, int minWidth) {
        String s = Integer.toString(i, 16);
        printString("00000".substring(0, Math.max(0, 5 - s.length())) + s, minWidth);
    }

    void printTree(boolean printHeading) {
        if (printHeading) {
            System.out.println("-------------------------------------------------------------------");
            System.out.println("    Serial       type     Parent  LeftChild  RightChild    position  value");
        }
        printNode(this);
        if (this.fType != 2) {
            if (this.fLeftChild != null) {
                this.fLeftChild.printTree(false);
            }
            if (this.fRightChild != null) {
                this.fRightChild.printTree(false);
            }
        }
    }
}
