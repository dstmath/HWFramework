package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.Trie2Writable;
import android.icu.impl.Trie2_16;
import android.icu.impl.locale.LanguageTag;
import android.icu.impl.number.Padder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

class RBBISetBuilder {
    Trie2_16 fFrozenTrie;
    int fGroupCount;
    RBBIRuleBuilder fRB;
    RangeDescriptor fRangeList;
    boolean fSawBOF;
    Trie2Writable fTrie;

    static class RangeDescriptor {
        int fEndChar;
        List<RBBINode> fIncludesSets;
        RangeDescriptor fNext;
        int fNum;
        int fStartChar;

        RangeDescriptor() {
            this.fIncludesSets = new ArrayList();
        }

        RangeDescriptor(RangeDescriptor other) {
            this.fStartChar = other.fStartChar;
            this.fEndChar = other.fEndChar;
            this.fNum = other.fNum;
            this.fIncludesSets = new ArrayList(other.fIncludesSets);
        }

        /* access modifiers changed from: package-private */
        public void split(int where) {
            Assert.assrt(where > this.fStartChar && where <= this.fEndChar);
            RangeDescriptor nr = new RangeDescriptor(this);
            nr.fStartChar = where;
            this.fEndChar = where - 1;
            nr.fNext = this.fNext;
            this.fNext = nr;
        }

        /* access modifiers changed from: package-private */
        public void setDictionaryFlag() {
            for (int i = 0; i < this.fIncludesSets.size(); i++) {
                String setName = "";
                RBBINode setRef = this.fIncludesSets.get(i).fParent;
                if (setRef != null) {
                    RBBINode varRef = setRef.fParent;
                    if (varRef != null && varRef.fType == 2) {
                        setName = varRef.fText;
                    }
                }
                if (setName.equals("dictionary")) {
                    this.fNum |= 16384;
                    return;
                }
            }
        }
    }

    RBBISetBuilder(RBBIRuleBuilder rb) {
        this.fRB = rb;
    }

    /* access modifiers changed from: package-private */
    public void build() {
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("usets") >= 0) {
            printSets();
        }
        this.fRangeList = new RangeDescriptor();
        this.fRangeList.fStartChar = 0;
        this.fRangeList.fEndChar = 1114111;
        for (RBBINode usetNode : this.fRB.fUSetNodes) {
            UnicodeSet inputSet = usetNode.fInputSet;
            int inputSetRangeCount = inputSet.getRangeCount();
            int inputSetRangeIndex = 0;
            RangeDescriptor rlRange = this.fRangeList;
            while (inputSetRangeIndex < inputSetRangeCount) {
                int inputSetRangeBegin = inputSet.getRangeStart(inputSetRangeIndex);
                int inputSetRangeEnd = inputSet.getRangeEnd(inputSetRangeIndex);
                while (rlRange.fEndChar < inputSetRangeBegin) {
                    rlRange = rlRange.fNext;
                }
                if (rlRange.fStartChar < inputSetRangeBegin) {
                    rlRange.split(inputSetRangeBegin);
                } else {
                    if (rlRange.fEndChar > inputSetRangeEnd) {
                        rlRange.split(inputSetRangeEnd + 1);
                    }
                    if (rlRange.fIncludesSets.indexOf(usetNode) == -1) {
                        rlRange.fIncludesSets.add(usetNode);
                    }
                    if (inputSetRangeEnd == rlRange.fEndChar) {
                        inputSetRangeIndex++;
                    }
                    rlRange = rlRange.fNext;
                }
            }
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("range") >= 0) {
            printRanges();
        }
        for (RangeDescriptor rlRange2 = this.fRangeList; rlRange2 != null; rlRange2 = rlRange2.fNext) {
            RangeDescriptor rlSearchRange = this.fRangeList;
            while (true) {
                if (rlSearchRange == rlRange2) {
                    break;
                } else if (rlRange2.fIncludesSets.equals(rlSearchRange.fIncludesSets)) {
                    rlRange2.fNum = rlSearchRange.fNum;
                    break;
                } else {
                    rlSearchRange = rlSearchRange.fNext;
                }
            }
            if (rlRange2.fNum == 0) {
                this.fGroupCount++;
                rlRange2.fNum = this.fGroupCount + 2;
                rlRange2.setDictionaryFlag();
                addValToSets(rlRange2.fIncludesSets, this.fGroupCount + 2);
            }
        }
        for (RBBINode usetNode2 : this.fRB.fUSetNodes) {
            UnicodeSet inputSet2 = usetNode2.fInputSet;
            if (inputSet2.contains((CharSequence) "eof")) {
                addValToSet(usetNode2, 1);
            }
            if (inputSet2.contains((CharSequence) "bof")) {
                addValToSet(usetNode2, 2);
                this.fSawBOF = true;
            }
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("rgroup") >= 0) {
            printRangeGroups();
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("esets") >= 0) {
            printSets();
        }
        this.fTrie = new Trie2Writable(0, 0);
        for (RangeDescriptor rlRange3 = this.fRangeList; rlRange3 != null; rlRange3 = rlRange3.fNext) {
            this.fTrie.setRange(rlRange3.fStartChar, rlRange3.fEndChar, rlRange3.fNum, true);
        }
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
    public void serializeTrie(OutputStream os) throws IOException {
        if (this.fFrozenTrie == null) {
            this.fFrozenTrie = this.fTrie.toTrie2_16();
            this.fTrie = null;
        }
        this.fFrozenTrie.serialize(os);
    }

    /* access modifiers changed from: package-private */
    public void addValToSets(List<RBBINode> sets, int val) {
        for (RBBINode usetNode : sets) {
            addValToSet(usetNode, val);
        }
    }

    /* access modifiers changed from: package-private */
    public void addValToSet(RBBINode usetNode, int val) {
        RBBINode leafNode = new RBBINode(3);
        leafNode.fVal = val;
        if (usetNode.fLeftChild == null) {
            usetNode.fLeftChild = leafNode;
            leafNode.fParent = usetNode;
            return;
        }
        RBBINode orNode = new RBBINode(9);
        orNode.fLeftChild = usetNode.fLeftChild;
        orNode.fRightChild = leafNode;
        orNode.fLeftChild.fParent = orNode;
        orNode.fRightChild.fParent = orNode;
        usetNode.fLeftChild = orNode;
        orNode.fParent = usetNode;
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
    public int getFirstChar(int category) {
        for (RangeDescriptor rlRange = this.fRangeList; rlRange != null; rlRange = rlRange.fNext) {
            if (rlRange.fNum == category) {
                return rlRange.fStartChar;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void printRanges() {
        System.out.print("\n\n Nonoverlapping Ranges ...\n");
        for (RangeDescriptor rlRange = this.fRangeList; rlRange != null; rlRange = rlRange.fNext) {
            PrintStream printStream = System.out;
            printStream.print(Padder.FALLBACK_PADDING_STRING + rlRange.fNum + "   " + rlRange.fStartChar + LanguageTag.SEP + rlRange.fEndChar);
            for (int i = 0; i < rlRange.fIncludesSets.size(); i++) {
                String setName = "anon";
                RBBINode setRef = rlRange.fIncludesSets.get(i).fParent;
                if (setRef != null) {
                    RBBINode varRef = setRef.fParent;
                    if (varRef != null && varRef.fType == 2) {
                        setName = varRef.fText;
                    }
                }
                System.out.print(setName);
                System.out.print("  ");
            }
            System.out.println("");
        }
    }

    /* access modifiers changed from: package-private */
    public void printRangeGroups() {
        int lastPrintedGroupNum = 0;
        System.out.print("\nRanges grouped by Unicode Set Membership...\n");
        for (RangeDescriptor rlRange = this.fRangeList; rlRange != null; rlRange = rlRange.fNext) {
            int groupNum = rlRange.fNum & 49151;
            if (groupNum > lastPrintedGroupNum) {
                lastPrintedGroupNum = groupNum;
                if (groupNum < 10) {
                    System.out.print(Padder.FALLBACK_PADDING_STRING);
                }
                System.out.print(groupNum + Padder.FALLBACK_PADDING_STRING);
                if ((rlRange.fNum & 16384) != 0) {
                    System.out.print(" <DICT> ");
                }
                for (int i = 0; i < rlRange.fIncludesSets.size(); i++) {
                    String setName = "anon";
                    RBBINode setRef = rlRange.fIncludesSets.get(i).fParent;
                    if (setRef != null) {
                        RBBINode varRef = setRef.fParent;
                        if (varRef != null && varRef.fType == 2) {
                            setName = varRef.fText;
                        }
                    }
                    System.out.print(setName);
                    System.out.print(Padder.FALLBACK_PADDING_STRING);
                }
                int i2 = 0;
                for (RangeDescriptor tRange = rlRange; tRange != null; tRange = tRange.fNext) {
                    if (tRange.fNum == rlRange.fNum) {
                        int i3 = i2 + 1;
                        if (i2 % 5 == 0) {
                            System.out.print("\n    ");
                        }
                        RBBINode.printHex(tRange.fStartChar, -1);
                        System.out.print(LanguageTag.SEP);
                        RBBINode.printHex(tRange.fEndChar, 0);
                        i2 = i3;
                    }
                }
                System.out.print("\n");
            }
        }
        System.out.print("\n");
    }

    /* access modifiers changed from: package-private */
    public void printSets() {
        System.out.print("\n\nUnicode Sets List\n------------------\n");
        for (int i = 0; i < this.fRB.fUSetNodes.size(); i++) {
            RBBINode usetNode = this.fRB.fUSetNodes.get(i);
            RBBINode.printInt(2, i);
            String setName = "anonymous";
            RBBINode setRef = usetNode.fParent;
            if (setRef != null) {
                RBBINode varRef = setRef.fParent;
                if (varRef != null && varRef.fType == 2) {
                    setName = varRef.fText;
                }
            }
            PrintStream printStream = System.out;
            printStream.print("  " + setName);
            System.out.print("   ");
            System.out.print(usetNode.fText);
            System.out.print("\n");
            if (usetNode.fLeftChild != null) {
                usetNode.fLeftChild.printTree(true);
            }
        }
        System.out.print("\n");
    }
}
