package ohos.global.icu.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.impl.Trie2Writable;
import ohos.global.icu.impl.Trie2_16;
import ohos.global.icu.text.RBBIRuleBuilder;

/* access modifiers changed from: package-private */
public class RBBISetBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int DICT_BIT = 16384;
    Trie2_16 fFrozenTrie;
    int fGroupCount;
    RBBIRuleBuilder fRB;
    RangeDescriptor fRangeList;
    boolean fSawBOF;
    Trie2Writable fTrie;

    /* access modifiers changed from: package-private */
    public static class RangeDescriptor {
        int fEndChar;
        List<RBBINode> fIncludesSets;
        RangeDescriptor fNext;
        int fNum;
        int fStartChar;

        RangeDescriptor() {
            this.fIncludesSets = new ArrayList();
        }

        RangeDescriptor(RangeDescriptor rangeDescriptor) {
            this.fStartChar = rangeDescriptor.fStartChar;
            this.fEndChar = rangeDescriptor.fEndChar;
            this.fNum = rangeDescriptor.fNum;
            this.fIncludesSets = new ArrayList(rangeDescriptor.fIncludesSets);
        }

        /* access modifiers changed from: package-private */
        public void split(int i) {
            Assert.assrt(i > this.fStartChar && i <= this.fEndChar);
            RangeDescriptor rangeDescriptor = new RangeDescriptor(this);
            rangeDescriptor.fStartChar = i;
            this.fEndChar = i - 1;
            rangeDescriptor.fNext = this.fNext;
            this.fNext = rangeDescriptor;
        }

        /* access modifiers changed from: package-private */
        public void setDictionaryFlag() {
            RBBINode rBBINode;
            for (int i = 0; i < this.fIncludesSets.size(); i++) {
                RBBINode rBBINode2 = this.fIncludesSets.get(i).fParent;
                if (((rBBINode2 == null || (rBBINode = rBBINode2.fParent) == null || rBBINode.fType != 2) ? "" : rBBINode.fText).equals("dictionary")) {
                    this.fNum |= 16384;
                    return;
                }
            }
        }
    }

    RBBISetBuilder(RBBIRuleBuilder rBBIRuleBuilder) {
        this.fRB = rBBIRuleBuilder;
    }

    /* access modifiers changed from: package-private */
    public void buildRanges() {
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("usets") >= 0) {
            printSets();
        }
        this.fRangeList = new RangeDescriptor();
        RangeDescriptor rangeDescriptor = this.fRangeList;
        rangeDescriptor.fStartChar = 0;
        rangeDescriptor.fEndChar = 1114111;
        for (RBBINode rBBINode : this.fRB.fUSetNodes) {
            UnicodeSet unicodeSet = rBBINode.fInputSet;
            int rangeCount = unicodeSet.getRangeCount();
            RangeDescriptor rangeDescriptor2 = this.fRangeList;
            int i = 0;
            while (i < rangeCount) {
                int rangeStart = unicodeSet.getRangeStart(i);
                int rangeEnd = unicodeSet.getRangeEnd(i);
                while (rangeDescriptor2.fEndChar < rangeStart) {
                    rangeDescriptor2 = rangeDescriptor2.fNext;
                }
                if (rangeDescriptor2.fStartChar < rangeStart) {
                    rangeDescriptor2.split(rangeStart);
                } else {
                    if (rangeDescriptor2.fEndChar > rangeEnd) {
                        rangeDescriptor2.split(rangeEnd + 1);
                    }
                    if (rangeDescriptor2.fIncludesSets.indexOf(rBBINode) == -1) {
                        rangeDescriptor2.fIncludesSets.add(rBBINode);
                    }
                    if (rangeEnd == rangeDescriptor2.fEndChar) {
                        i++;
                    }
                    rangeDescriptor2 = rangeDescriptor2.fNext;
                }
            }
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("range") >= 0) {
            printRanges();
        }
        for (RangeDescriptor rangeDescriptor3 = this.fRangeList; rangeDescriptor3 != null; rangeDescriptor3 = rangeDescriptor3.fNext) {
            RangeDescriptor rangeDescriptor4 = this.fRangeList;
            while (true) {
                if (rangeDescriptor4 == rangeDescriptor3) {
                    break;
                } else if (rangeDescriptor3.fIncludesSets.equals(rangeDescriptor4.fIncludesSets)) {
                    rangeDescriptor3.fNum = rangeDescriptor4.fNum;
                    break;
                } else {
                    rangeDescriptor4 = rangeDescriptor4.fNext;
                }
            }
            if (rangeDescriptor3.fNum == 0) {
                this.fGroupCount++;
                rangeDescriptor3.fNum = this.fGroupCount + 2;
                rangeDescriptor3.setDictionaryFlag();
                addValToSets(rangeDescriptor3.fIncludesSets, this.fGroupCount + 2);
            }
        }
        for (RBBINode rBBINode2 : this.fRB.fUSetNodes) {
            UnicodeSet unicodeSet2 = rBBINode2.fInputSet;
            if (unicodeSet2.contains("eof")) {
                addValToSet(rBBINode2, 1);
            }
            if (unicodeSet2.contains("bof")) {
                addValToSet(rBBINode2, 2);
                this.fSawBOF = true;
            }
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("rgroup") >= 0) {
            printRangeGroups();
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("esets") >= 0) {
            printSets();
        }
    }

    /* access modifiers changed from: package-private */
    public void buildTrie() {
        this.fTrie = new Trie2Writable(0, 0);
        for (RangeDescriptor rangeDescriptor = this.fRangeList; rangeDescriptor != null; rangeDescriptor = rangeDescriptor.fNext) {
            this.fTrie.setRange(rangeDescriptor.fStartChar, rangeDescriptor.fEndChar, rangeDescriptor.fNum, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void mergeCategories(RBBIRuleBuilder.IntPair intPair) {
        for (RangeDescriptor rangeDescriptor = this.fRangeList; rangeDescriptor != null; rangeDescriptor = rangeDescriptor.fNext) {
            int i = rangeDescriptor.fNum & -16385;
            int i2 = rangeDescriptor.fNum & 16384;
            if (i == intPair.second) {
                rangeDescriptor.fNum = intPair.first | i2;
            } else if (i > intPair.second) {
                rangeDescriptor.fNum--;
            }
        }
        this.fGroupCount--;
    }

    /* access modifiers changed from: package-private */
    public int getTrieSize() {
        if (this.fFrozenTrie == null) {
            this.fFrozenTrie = this.fTrie.toTrie2_16();
            this.fTrie = null;
        }
        return this.fFrozenTrie.getSerializedLength();
    }

    /* access modifiers changed from: package-private */
    public void serializeTrie(OutputStream outputStream) throws IOException {
        if (this.fFrozenTrie == null) {
            this.fFrozenTrie = this.fTrie.toTrie2_16();
            this.fTrie = null;
        }
        this.fFrozenTrie.serialize(outputStream);
    }

    /* access modifiers changed from: package-private */
    public void addValToSets(List<RBBINode> list, int i) {
        for (RBBINode rBBINode : list) {
            addValToSet(rBBINode, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void addValToSet(RBBINode rBBINode, int i) {
        RBBINode rBBINode2 = new RBBINode(3);
        rBBINode2.fVal = i;
        if (rBBINode.fLeftChild == null) {
            rBBINode.fLeftChild = rBBINode2;
            rBBINode2.fParent = rBBINode;
            return;
        }
        RBBINode rBBINode3 = new RBBINode(9);
        rBBINode3.fLeftChild = rBBINode.fLeftChild;
        rBBINode3.fRightChild = rBBINode2;
        rBBINode3.fLeftChild.fParent = rBBINode3;
        rBBINode3.fRightChild.fParent = rBBINode3;
        rBBINode.fLeftChild = rBBINode3;
        rBBINode3.fParent = rBBINode;
    }

    /* access modifiers changed from: package-private */
    public int getNumCharCategories() {
        return this.fGroupCount + 3;
    }

    /* access modifiers changed from: package-private */
    public boolean sawBOF() {
        return this.fSawBOF;
    }

    /* access modifiers changed from: package-private */
    public int getFirstChar(int i) {
        for (RangeDescriptor rangeDescriptor = this.fRangeList; rangeDescriptor != null; rangeDescriptor = rangeDescriptor.fNext) {
            if (rangeDescriptor.fNum == i) {
                return rangeDescriptor.fStartChar;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void printRanges() {
        RBBINode rBBINode;
        System.out.print("\n\n Nonoverlapping Ranges ...\n");
        for (RangeDescriptor rangeDescriptor = this.fRangeList; rangeDescriptor != null; rangeDescriptor = rangeDescriptor.fNext) {
            PrintStream printStream = System.out;
            printStream.print(" " + rangeDescriptor.fNum + "   " + rangeDescriptor.fStartChar + "-" + rangeDescriptor.fEndChar);
            for (int i = 0; i < rangeDescriptor.fIncludesSets.size(); i++) {
                RBBINode rBBINode2 = rangeDescriptor.fIncludesSets.get(i).fParent;
                System.out.print((rBBINode2 == null || (rBBINode = rBBINode2.fParent) == null || rBBINode.fType != 2) ? "anon" : rBBINode.fText);
                System.out.print("  ");
            }
            System.out.println("");
        }
    }

    /* access modifiers changed from: package-private */
    public void printRangeGroups() {
        RBBINode rBBINode;
        System.out.print("\nRanges grouped by Unicode Set Membership...\n");
        int i = 0;
        for (RangeDescriptor rangeDescriptor = this.fRangeList; rangeDescriptor != null; rangeDescriptor = rangeDescriptor.fNext) {
            int i2 = rangeDescriptor.fNum & 49151;
            if (i2 > i) {
                if (i2 < 10) {
                    System.out.print(" ");
                }
                System.out.print(i2 + " ");
                if ((rangeDescriptor.fNum & 16384) != 0) {
                    System.out.print(" <DICT> ");
                }
                for (int i3 = 0; i3 < rangeDescriptor.fIncludesSets.size(); i3++) {
                    RBBINode rBBINode2 = rangeDescriptor.fIncludesSets.get(i3).fParent;
                    System.out.print((rBBINode2 == null || (rBBINode = rBBINode2.fParent) == null || rBBINode.fType != 2) ? "anon" : rBBINode.fText);
                    System.out.print(" ");
                }
                int i4 = 0;
                for (RangeDescriptor rangeDescriptor2 = rangeDescriptor; rangeDescriptor2 != null; rangeDescriptor2 = rangeDescriptor2.fNext) {
                    if (rangeDescriptor2.fNum == rangeDescriptor.fNum) {
                        int i5 = i4 + 1;
                        if (i4 % 5 == 0) {
                            System.out.print("\n    ");
                        }
                        RBBINode.printHex(rangeDescriptor2.fStartChar, -1);
                        System.out.print("-");
                        RBBINode.printHex(rangeDescriptor2.fEndChar, 0);
                        i4 = i5;
                    }
                }
                System.out.print("\n");
                i = i2;
            }
        }
        System.out.print("\n");
    }

    /* access modifiers changed from: package-private */
    public void printSets() {
        RBBINode rBBINode;
        System.out.print("\n\nUnicode Sets List\n------------------\n");
        for (int i = 0; i < this.fRB.fUSetNodes.size(); i++) {
            RBBINode rBBINode2 = this.fRB.fUSetNodes.get(i);
            RBBINode.printInt(2, i);
            RBBINode rBBINode3 = rBBINode2.fParent;
            String str = (rBBINode3 == null || (rBBINode = rBBINode3.fParent) == null || rBBINode.fType != 2) ? "anonymous" : rBBINode.fText;
            PrintStream printStream = System.out;
            printStream.print("  " + str);
            System.out.print("   ");
            System.out.print(rBBINode2.fText);
            System.out.print("\n");
            if (rBBINode2.fLeftChild != null) {
                rBBINode2.fLeftChild.printTree(true);
            }
        }
        System.out.print("\n");
    }
}
