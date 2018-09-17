package android.icu.text;

import android.icu.impl.Assert;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;

class RBBINode {
    static final int endMark = 6;
    static int gLastSerial = 0;
    static final int leafChar = 3;
    static final int lookAhead = 4;
    static final int nodeTypeLimit = 16;
    static final String[] nodeTypeNames = null;
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
    int fPrecedence;
    RBBINode fRightChild;
    int fSerialNum;
    String fText;
    int fType;
    int fVal;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.RBBINode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.RBBINode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RBBINode.<clinit>():void");
    }

    RBBINode(int t) {
        boolean z;
        this.fPrecedence = setRef;
        if (t < nodeTypeLimit) {
            z = true;
        } else {
            z = false;
        }
        Assert.assrt(z);
        int i = gLastSerial + uset;
        gLastSerial = i;
        this.fSerialNum = i;
        this.fType = t;
        this.fFirstPosSet = new HashSet();
        this.fLastPosSet = new HashSet();
        this.fFollowPos = new HashSet();
        if (t == opCat) {
            this.fPrecedence = precOpCat;
        } else if (t == opOr) {
            this.fPrecedence = precOpOr;
        } else if (t == opStart) {
            this.fPrecedence = uset;
        } else if (t == opLParen) {
            this.fPrecedence = varRef;
        } else {
            this.fPrecedence = setRef;
        }
    }

    RBBINode(RBBINode other) {
        this.fPrecedence = setRef;
        int i = gLastSerial + uset;
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
        this.fFirstPosSet = new HashSet(other.fFirstPosSet);
        this.fLastPosSet = new HashSet(other.fLastPosSet);
        this.fFollowPos = new HashSet(other.fFollowPos);
    }

    RBBINode cloneTree() {
        if (this.fType == varRef) {
            return this.fLeftChild.cloneTree();
        }
        if (this.fType == uset) {
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
        if (this.fType == varRef) {
            return this.fLeftChild.cloneTree();
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
        int i = setRef;
        if (n == null) {
            System.out.print(" -- null --\n");
        } else {
            printInt(n.fSerialNum, opStar);
            printString(nodeTypeNames[n.fType], opPlus);
            printInt(n.fParent == null ? setRef : n.fParent.fSerialNum, opPlus);
            printInt(n.fLeftChild == null ? setRef : n.fLeftChild.fSerialNum, opPlus);
            if (n.fRightChild != null) {
                i = n.fRightChild.fSerialNum;
            }
            printInt(i, opQuestion);
            printInt(n.fFirstPos, opQuestion);
            printInt(n.fVal, opStart);
            if (n.fType == varRef) {
                System.out.print(" " + n.fText);
            }
        }
        System.out.println(XmlPullParser.NO_NAMESPACE);
    }

    static void printString(String s, int minWidth) {
        int i;
        for (i = minWidth; i < 0; i += uset) {
            System.out.print(' ');
        }
        for (i = s.length(); i < minWidth; i += uset) {
            System.out.print(' ');
        }
        System.out.print(s);
    }

    static void printInt(int i, int minWidth) {
        String s = Integer.toString(i);
        printString(s, Math.max(minWidth, s.length() + uset));
    }

    static void printHex(int i, int minWidth) {
        String s = Integer.toString(i, nodeTypeLimit);
        printString("00000".substring(setRef, Math.max(setRef, 5 - s.length())) + s, minWidth);
    }

    void printTree(boolean printHeading) {
        if (printHeading) {
            System.out.println("-------------------------------------------------------------------");
            System.out.println("    Serial       type     Parent  LeftChild  RightChild    position  value");
        }
        printNode(this);
        if (this.fType != varRef) {
            if (this.fLeftChild != null) {
                this.fLeftChild.printTree(false);
            }
            if (this.fRightChild != null) {
                this.fRightChild.printTree(false);
            }
        }
    }
}
