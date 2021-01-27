package ohos.global.icu.text;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.impl.RBBIDataWrapper;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.RBBIRuleBuilder;

/* access modifiers changed from: package-private */
public class RBBITableBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private List<RBBIStateDescriptor> fDStates = new ArrayList();
    private RBBIRuleBuilder fRB;
    private int fRootIx;
    private List<short[]> fSafeTable;

    /* access modifiers changed from: package-private */
    public static class RBBIStateDescriptor {
        int fAccepting;
        int[] fDtran;
        int fLookAhead;
        boolean fMarked;
        Set<RBBINode> fPositions = new HashSet();
        SortedSet<Integer> fTagVals = new TreeSet();
        int fTagsIdx;

        RBBIStateDescriptor(int i) {
            this.fDtran = new int[(i + 1)];
        }
    }

    RBBITableBuilder(RBBIRuleBuilder rBBIRuleBuilder, int i) {
        this.fRootIx = i;
        this.fRB = rBBIRuleBuilder;
    }

    /* access modifiers changed from: package-private */
    public void buildForwardTable() {
        if (this.fRB.fTreeRoots[this.fRootIx] != null) {
            this.fRB.fTreeRoots[this.fRootIx] = this.fRB.fTreeRoots[this.fRootIx].flattenVariables();
            if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("ftree") >= 0) {
                System.out.println("Parse tree after flattening variable references.");
                this.fRB.fTreeRoots[this.fRootIx].printTree(true);
            }
            if (this.fRB.fSetBuilder.sawBOF()) {
                RBBINode rBBINode = new RBBINode(8);
                RBBINode rBBINode2 = new RBBINode(3);
                rBBINode.fLeftChild = rBBINode2;
                rBBINode.fRightChild = this.fRB.fTreeRoots[this.fRootIx];
                rBBINode2.fParent = rBBINode;
                rBBINode2.fVal = 2;
                this.fRB.fTreeRoots[this.fRootIx] = rBBINode;
            }
            RBBINode rBBINode3 = new RBBINode(8);
            rBBINode3.fLeftChild = this.fRB.fTreeRoots[this.fRootIx];
            this.fRB.fTreeRoots[this.fRootIx].fParent = rBBINode3;
            rBBINode3.fRightChild = new RBBINode(6);
            rBBINode3.fRightChild.fParent = rBBINode3;
            this.fRB.fTreeRoots[this.fRootIx] = rBBINode3;
            this.fRB.fTreeRoots[this.fRootIx].flattenSets();
            if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("stree") >= 0) {
                System.out.println("Parse tree after flattening Unicode Set references.");
                this.fRB.fTreeRoots[this.fRootIx].printTree(true);
            }
            calcNullable(this.fRB.fTreeRoots[this.fRootIx]);
            calcFirstPos(this.fRB.fTreeRoots[this.fRootIx]);
            calcLastPos(this.fRB.fTreeRoots[this.fRootIx]);
            calcFollowPos(this.fRB.fTreeRoots[this.fRootIx]);
            if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("pos") >= 0) {
                System.out.print("\n");
                printPosSets(this.fRB.fTreeRoots[this.fRootIx]);
            }
            if (this.fRB.fChainRules) {
                calcChainedFollowPos(this.fRB.fTreeRoots[this.fRootIx]);
            }
            if (this.fRB.fSetBuilder.sawBOF()) {
                bofFixup();
            }
            buildStateTable();
            flagAcceptingStates();
            flagLookAheadStates();
            flagTaggedStates();
            mergeRuleStatusVals();
        }
    }

    /* access modifiers changed from: package-private */
    public void calcNullable(RBBINode rBBINode) {
        if (rBBINode != null) {
            boolean z = false;
            if (rBBINode.fType == 0 || rBBINode.fType == 6) {
                rBBINode.fNullable = false;
            } else if (rBBINode.fType == 4 || rBBINode.fType == 5) {
                rBBINode.fNullable = true;
            } else {
                calcNullable(rBBINode.fLeftChild);
                calcNullable(rBBINode.fRightChild);
                if (rBBINode.fType == 9) {
                    if (rBBINode.fLeftChild.fNullable || rBBINode.fRightChild.fNullable) {
                        z = true;
                    }
                    rBBINode.fNullable = z;
                } else if (rBBINode.fType == 8) {
                    if (rBBINode.fLeftChild.fNullable && rBBINode.fRightChild.fNullable) {
                        z = true;
                    }
                    rBBINode.fNullable = z;
                } else if (rBBINode.fType == 10 || rBBINode.fType == 12) {
                    rBBINode.fNullable = true;
                } else {
                    rBBINode.fNullable = false;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void calcFirstPos(RBBINode rBBINode) {
        if (rBBINode != null) {
            if (rBBINode.fType == 3 || rBBINode.fType == 6 || rBBINode.fType == 4 || rBBINode.fType == 5) {
                rBBINode.fFirstPosSet.add(rBBINode);
                return;
            }
            calcFirstPos(rBBINode.fLeftChild);
            calcFirstPos(rBBINode.fRightChild);
            if (rBBINode.fType == 9) {
                rBBINode.fFirstPosSet.addAll(rBBINode.fLeftChild.fFirstPosSet);
                rBBINode.fFirstPosSet.addAll(rBBINode.fRightChild.fFirstPosSet);
            } else if (rBBINode.fType == 8) {
                rBBINode.fFirstPosSet.addAll(rBBINode.fLeftChild.fFirstPosSet);
                if (rBBINode.fLeftChild.fNullable) {
                    rBBINode.fFirstPosSet.addAll(rBBINode.fRightChild.fFirstPosSet);
                }
            } else if (rBBINode.fType == 10 || rBBINode.fType == 12 || rBBINode.fType == 11) {
                rBBINode.fFirstPosSet.addAll(rBBINode.fLeftChild.fFirstPosSet);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void calcLastPos(RBBINode rBBINode) {
        if (rBBINode != null) {
            if (rBBINode.fType == 3 || rBBINode.fType == 6 || rBBINode.fType == 4 || rBBINode.fType == 5) {
                rBBINode.fLastPosSet.add(rBBINode);
                return;
            }
            calcLastPos(rBBINode.fLeftChild);
            calcLastPos(rBBINode.fRightChild);
            if (rBBINode.fType == 9) {
                rBBINode.fLastPosSet.addAll(rBBINode.fLeftChild.fLastPosSet);
                rBBINode.fLastPosSet.addAll(rBBINode.fRightChild.fLastPosSet);
            } else if (rBBINode.fType == 8) {
                rBBINode.fLastPosSet.addAll(rBBINode.fRightChild.fLastPosSet);
                if (rBBINode.fRightChild.fNullable) {
                    rBBINode.fLastPosSet.addAll(rBBINode.fLeftChild.fLastPosSet);
                }
            } else if (rBBINode.fType == 10 || rBBINode.fType == 12 || rBBINode.fType == 11) {
                rBBINode.fLastPosSet.addAll(rBBINode.fLeftChild.fLastPosSet);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void calcFollowPos(RBBINode rBBINode) {
        if (!(rBBINode == null || rBBINode.fType == 3 || rBBINode.fType == 6)) {
            calcFollowPos(rBBINode.fLeftChild);
            calcFollowPos(rBBINode.fRightChild);
            if (rBBINode.fType == 8) {
                for (RBBINode rBBINode2 : rBBINode.fLeftChild.fLastPosSet) {
                    rBBINode2.fFollowPos.addAll(rBBINode.fRightChild.fFirstPosSet);
                }
            }
            if (rBBINode.fType == 10 || rBBINode.fType == 11) {
                for (RBBINode rBBINode3 : rBBINode.fLastPosSet) {
                    rBBINode3.fFollowPos.addAll(rBBINode.fFirstPosSet);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addRuleRootNodes(List<RBBINode> list, RBBINode rBBINode) {
        if (rBBINode != null) {
            if (rBBINode.fRuleRoot) {
                list.add(rBBINode);
                return;
            }
            addRuleRootNodes(list, rBBINode.fLeftChild);
            addRuleRootNodes(list, rBBINode.fRightChild);
        }
    }

    /* access modifiers changed from: package-private */
    public void calcChainedFollowPos(RBBINode rBBINode) {
        int firstChar;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        rBBINode.findNodes(arrayList, 6);
        rBBINode.findNodes(arrayList2, 3);
        ArrayList arrayList3 = new ArrayList();
        addRuleRootNodes(arrayList3, rBBINode);
        HashSet<RBBINode> hashSet = new HashSet();
        for (RBBINode rBBINode2 : arrayList3) {
            if (rBBINode2.fChainIn) {
                hashSet.addAll(rBBINode2.fFirstPosSet);
            }
        }
        for (RBBINode rBBINode3 : arrayList2) {
            Iterator<RBBINode> it = arrayList.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (rBBINode3.fFollowPos.contains(it.next())) {
                        break;
                    }
                } else {
                    rBBINode3 = null;
                    break;
                }
            }
            if (rBBINode3 != null && (!this.fRB.fLBCMNoChain || (firstChar = this.fRB.fSetBuilder.getFirstChar(rBBINode3.fVal)) == -1 || UCharacter.getIntPropertyValue(firstChar, 4104) != 9)) {
                for (RBBINode rBBINode4 : hashSet) {
                    if (rBBINode4.fType == 3 && rBBINode3.fVal == rBBINode4.fVal) {
                        rBBINode3.fFollowPos.addAll(rBBINode4.fFollowPos);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void bofFixup() {
        RBBINode rBBINode = this.fRB.fTreeRoots[this.fRootIx].fLeftChild.fLeftChild;
        boolean z = true;
        Assert.assrt(rBBINode.fType == 3);
        if (rBBINode.fVal != 2) {
            z = false;
        }
        Assert.assrt(z);
        for (RBBINode rBBINode2 : this.fRB.fTreeRoots[this.fRootIx].fLeftChild.fRightChild.fFirstPosSet) {
            if (rBBINode2.fType == 3 && rBBINode2.fVal == rBBINode.fVal) {
                rBBINode.fFollowPos.addAll(rBBINode2.fFollowPos);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void buildStateTable() {
        RBBIStateDescriptor rBBIStateDescriptor;
        int numCharCategories = this.fRB.fSetBuilder.getNumCharCategories() - 1;
        this.fDStates.add(new RBBIStateDescriptor(numCharCategories));
        RBBIStateDescriptor rBBIStateDescriptor2 = new RBBIStateDescriptor(numCharCategories);
        rBBIStateDescriptor2.fPositions.addAll(this.fRB.fTreeRoots[this.fRootIx].fFirstPosSet);
        this.fDStates.add(rBBIStateDescriptor2);
        while (true) {
            int i = 1;
            while (true) {
                if (i >= this.fDStates.size()) {
                    rBBIStateDescriptor = null;
                    break;
                }
                rBBIStateDescriptor = this.fDStates.get(i);
                if (!rBBIStateDescriptor.fMarked) {
                    break;
                }
                i++;
            }
            if (rBBIStateDescriptor != null) {
                rBBIStateDescriptor.fMarked = true;
                for (int i2 = 1; i2 <= numCharCategories; i2++) {
                    Set<RBBINode> set = null;
                    for (RBBINode rBBINode : rBBIStateDescriptor.fPositions) {
                        if (rBBINode.fType == 3 && rBBINode.fVal == i2) {
                            if (set == null) {
                                set = new HashSet<>();
                            }
                            set.addAll(rBBINode.fFollowPos);
                        }
                    }
                    if (set != null) {
                        boolean z = false;
                        Assert.assrt(set.size() > 0);
                        int i3 = 0;
                        while (true) {
                            if (i3 >= this.fDStates.size()) {
                                i3 = 0;
                                break;
                            }
                            RBBIStateDescriptor rBBIStateDescriptor3 = this.fDStates.get(i3);
                            if (set.equals(rBBIStateDescriptor3.fPositions)) {
                                set = rBBIStateDescriptor3.fPositions;
                                z = true;
                                break;
                            }
                            i3++;
                        }
                        if (!z) {
                            RBBIStateDescriptor rBBIStateDescriptor4 = new RBBIStateDescriptor(numCharCategories);
                            rBBIStateDescriptor4.fPositions = set;
                            this.fDStates.add(rBBIStateDescriptor4);
                            i3 = this.fDStates.size() - 1;
                        }
                        rBBIStateDescriptor.fDtran[i2] = i3;
                    }
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flagAcceptingStates() {
        ArrayList arrayList = new ArrayList();
        this.fRB.fTreeRoots[this.fRootIx].findNodes(arrayList, 6);
        for (int i = 0; i < arrayList.size(); i++) {
            RBBINode rBBINode = (RBBINode) arrayList.get(i);
            for (int i2 = 0; i2 < this.fDStates.size(); i2++) {
                RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i2);
                if (rBBIStateDescriptor.fPositions.contains(rBBINode)) {
                    if (rBBIStateDescriptor.fAccepting == 0) {
                        rBBIStateDescriptor.fAccepting = rBBINode.fVal;
                        if (rBBIStateDescriptor.fAccepting == 0) {
                            rBBIStateDescriptor.fAccepting = -1;
                        }
                    }
                    if (rBBIStateDescriptor.fAccepting == -1 && rBBINode.fVal != 0) {
                        rBBIStateDescriptor.fAccepting = rBBINode.fVal;
                    }
                    if (rBBINode.fLookAheadEnd) {
                        rBBIStateDescriptor.fLookAhead = rBBIStateDescriptor.fAccepting;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flagLookAheadStates() {
        ArrayList arrayList = new ArrayList();
        this.fRB.fTreeRoots[this.fRootIx].findNodes(arrayList, 4);
        for (int i = 0; i < arrayList.size(); i++) {
            RBBINode rBBINode = (RBBINode) arrayList.get(i);
            for (int i2 = 0; i2 < this.fDStates.size(); i2++) {
                RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i2);
                if (rBBIStateDescriptor.fPositions.contains(rBBINode)) {
                    rBBIStateDescriptor.fLookAhead = rBBINode.fVal;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flagTaggedStates() {
        ArrayList arrayList = new ArrayList();
        this.fRB.fTreeRoots[this.fRootIx].findNodes(arrayList, 5);
        for (int i = 0; i < arrayList.size(); i++) {
            RBBINode rBBINode = (RBBINode) arrayList.get(i);
            for (int i2 = 0; i2 < this.fDStates.size(); i2++) {
                RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i2);
                if (rBBIStateDescriptor.fPositions.contains(rBBINode)) {
                    rBBIStateDescriptor.fTagVals.add(Integer.valueOf(rBBINode.fVal));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void mergeRuleStatusVals() {
        if (this.fRB.fRuleStatusVals.size() == 0) {
            this.fRB.fRuleStatusVals.add(1);
            this.fRB.fRuleStatusVals.add(0);
            TreeSet treeSet = new TreeSet();
            this.fRB.fStatusSets.put(treeSet, 0);
            new TreeSet().add(0);
            this.fRB.fStatusSets.put(treeSet, 0);
        }
        for (int i = 0; i < this.fDStates.size(); i++) {
            RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i);
            SortedSet<Integer> sortedSet = rBBIStateDescriptor.fTagVals;
            Integer num = this.fRB.fStatusSets.get(sortedSet);
            if (num == null) {
                num = Integer.valueOf(this.fRB.fRuleStatusVals.size());
                this.fRB.fStatusSets.put(sortedSet, num);
                this.fRB.fRuleStatusVals.add(Integer.valueOf(sortedSet.size()));
                this.fRB.fRuleStatusVals.addAll(sortedSet);
            }
            rBBIStateDescriptor.fTagsIdx = num.intValue();
        }
    }

    /* access modifiers changed from: package-private */
    public void printPosSets(RBBINode rBBINode) {
        if (rBBINode != null) {
            RBBINode.printNode(rBBINode);
            PrintStream printStream = System.out;
            printStream.print("         Nullable:  " + rBBINode.fNullable);
            System.out.print("         firstpos:  ");
            printSet(rBBINode.fFirstPosSet);
            System.out.print("         lastpos:   ");
            printSet(rBBINode.fLastPosSet);
            System.out.print("         followpos: ");
            printSet(rBBINode.fFollowPos);
            printPosSets(rBBINode.fLeftChild);
            printPosSets(rBBINode.fRightChild);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004c, code lost:
        r10.first++;
     */
    public boolean findDuplCharClassFrom(RBBIRuleBuilder.IntPair intPair) {
        int size = this.fDStates.size();
        int numCharCategories = this.fRB.fSetBuilder.getNumCharCategories();
        int i = 0;
        int i2 = 0;
        while (intPair.first < numCharCategories - 1) {
            int i3 = intPair.first;
            while (true) {
                intPair.second = i3 + 1;
                if (intPair.second >= numCharCategories) {
                    break;
                }
                int i4 = i2;
                int i5 = i;
                int i6 = 0;
                while (true) {
                    if (i6 >= size) {
                        i = i5;
                        i2 = i4;
                        break;
                    }
                    RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i6);
                    int i7 = rBBIStateDescriptor.fDtran[intPair.first];
                    i2 = rBBIStateDescriptor.fDtran[intPair.second];
                    if (i7 != i2) {
                        i = i7;
                        break;
                    }
                    i6++;
                    i4 = i2;
                    i5 = i7;
                }
                if (i == i2) {
                    return true;
                }
                i3 = intPair.second;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void removeColumn(int i) {
        int size = this.fDStates.size();
        for (int i2 = 0; i2 < size; i2++) {
            RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i2);
            int[] copyOf = Arrays.copyOf(rBBIStateDescriptor.fDtran, rBBIStateDescriptor.fDtran.length - 1);
            System.arraycopy(rBBIStateDescriptor.fDtran, i + 1, copyOf, i, copyOf.length - i);
            rBBIStateDescriptor.fDtran = copyOf;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x006f, code lost:
        r11.first++;
     */
    public boolean findDuplicateState(RBBIRuleBuilder.IntPair intPair) {
        boolean z;
        int size = this.fDStates.size();
        int numCharCategories = this.fRB.fSetBuilder.getNumCharCategories();
        while (intPair.first < size - 1) {
            RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(intPair.first);
            int i = intPair.first;
            while (true) {
                intPair.second = i + 1;
                if (intPair.second >= size) {
                    break;
                }
                RBBIStateDescriptor rBBIStateDescriptor2 = this.fDStates.get(intPair.second);
                if (rBBIStateDescriptor.fAccepting == rBBIStateDescriptor2.fAccepting && rBBIStateDescriptor.fLookAhead == rBBIStateDescriptor2.fLookAhead && rBBIStateDescriptor.fTagsIdx == rBBIStateDescriptor2.fTagsIdx) {
                    int i2 = 0;
                    while (true) {
                        if (i2 >= numCharCategories) {
                            z = true;
                            break;
                        }
                        int i3 = rBBIStateDescriptor.fDtran[i2];
                        int i4 = rBBIStateDescriptor2.fDtran[i2];
                        if (i3 == i4 || ((i3 == intPair.first || i3 == intPair.second) && (i4 == intPair.first || i4 == intPair.second))) {
                            i2++;
                        }
                    }
                    z = false;
                    if (z) {
                        return true;
                    }
                }
                i = intPair.second;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0051, code lost:
        r11.first++;
     */
    public boolean findDuplicateSafeState(RBBIRuleBuilder.IntPair intPair) {
        boolean z;
        int size = this.fSafeTable.size();
        while (intPair.first < size - 1) {
            short[] sArr = this.fSafeTable.get(intPair.first);
            int i = intPair.first;
            while (true) {
                intPair.second = i + 1;
                if (intPair.second >= size) {
                    break;
                }
                short[] sArr2 = this.fSafeTable.get(intPair.second);
                int length = sArr.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        z = true;
                        break;
                    }
                    short s = sArr[i2];
                    short s2 = sArr2[i2];
                    if (s == s2 || ((s == intPair.first || s == intPair.second) && (s2 == intPair.first || s2 == intPair.second))) {
                        i2++;
                    }
                }
                z = false;
                if (z) {
                    return true;
                }
                i = intPair.second;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void removeState(RBBIRuleBuilder.IntPair intPair) {
        int i = intPair.first;
        int i2 = intPair.second;
        this.fDStates.remove(i2);
        int size = this.fDStates.size();
        int numCharCategories = this.fRB.fSetBuilder.getNumCharCategories();
        for (int i3 = 0; i3 < size; i3++) {
            RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i3);
            for (int i4 = 0; i4 < numCharCategories; i4++) {
                int i5 = rBBIStateDescriptor.fDtran[i4];
                if (i5 == i2) {
                    i5 = i;
                } else if (i5 > i2) {
                    i5--;
                }
                rBBIStateDescriptor.fDtran[i4] = i5;
            }
            if (rBBIStateDescriptor.fAccepting == i2) {
                rBBIStateDescriptor.fAccepting = i;
            } else if (rBBIStateDescriptor.fAccepting > i2) {
                rBBIStateDescriptor.fAccepting--;
            }
            if (rBBIStateDescriptor.fLookAhead == i2) {
                rBBIStateDescriptor.fLookAhead = i;
            } else if (rBBIStateDescriptor.fLookAhead > i2) {
                rBBIStateDescriptor.fLookAhead--;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:17:0x0029 */
    /* JADX DEBUG: Multi-variable search result rejected for r4v2, resolved type: short[] */
    /* JADX DEBUG: Multi-variable search result rejected for r6v1, resolved type: short */
    /* JADX DEBUG: Multi-variable search result rejected for r6v4, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v2 */
    /* access modifiers changed from: package-private */
    public void removeSafeState(RBBIRuleBuilder.IntPair intPair) {
        int i = intPair.first;
        int i2 = intPair.second;
        this.fSafeTable.remove(i2);
        int size = this.fSafeTable.size();
        for (int i3 = 0; i3 < size; i3++) {
            short[] sArr = this.fSafeTable.get(i3);
            for (int i4 = 0; i4 < sArr.length; i4++) {
                short s = sArr[i4];
                if (s == i2) {
                    s = i;
                } else if (s > i2) {
                    s--;
                }
                sArr[i4] = s == true ? (short) 1 : 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int removeDuplicateStates() {
        int i = 0;
        RBBIRuleBuilder.IntPair intPair = new RBBIRuleBuilder.IntPair(3, 0);
        while (findDuplicateState(intPair)) {
            removeState(intPair);
            i++;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public int getTableSize() {
        if (this.fRB.fTreeRoots[this.fRootIx] == null) {
            return 0;
        }
        return ((this.fDStates.size() * ((this.fRB.fSetBuilder.getNumCharCategories() * 2) + 8)) + 16 + 7) & -8;
    }

    /* access modifiers changed from: package-private */
    public RBBIDataWrapper.RBBIStateTable exportTable() {
        RBBIDataWrapper.RBBIStateTable rBBIStateTable = new RBBIDataWrapper.RBBIStateTable();
        if (this.fRB.fTreeRoots[this.fRootIx] == null) {
            return rBBIStateTable;
        }
        Assert.assrt(this.fRB.fSetBuilder.getNumCharCategories() < 32767 && this.fDStates.size() < 32767);
        rBBIStateTable.fNumStates = this.fDStates.size();
        int numCharCategories = this.fRB.fSetBuilder.getNumCharCategories() + 4;
        rBBIStateTable.fTable = new short[((getTableSize() - 16) / 2)];
        rBBIStateTable.fRowLen = numCharCategories * 2;
        if (this.fRB.fLookAheadHardBreak) {
            rBBIStateTable.fFlags |= 1;
        }
        if (this.fRB.fSetBuilder.sawBOF()) {
            rBBIStateTable.fFlags |= 2;
        }
        int numCharCategories2 = this.fRB.fSetBuilder.getNumCharCategories();
        for (int i = 0; i < rBBIStateTable.fNumStates; i++) {
            RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i);
            int i2 = i * numCharCategories;
            Assert.assrt(-32768 < rBBIStateDescriptor.fAccepting && rBBIStateDescriptor.fAccepting <= 32767);
            Assert.assrt(-32768 < rBBIStateDescriptor.fLookAhead && rBBIStateDescriptor.fLookAhead <= 32767);
            rBBIStateTable.fTable[i2 + 0] = (short) rBBIStateDescriptor.fAccepting;
            rBBIStateTable.fTable[i2 + 1] = (short) rBBIStateDescriptor.fLookAhead;
            rBBIStateTable.fTable[i2 + 2] = (short) rBBIStateDescriptor.fTagsIdx;
            for (int i3 = 0; i3 < numCharCategories2; i3++) {
                rBBIStateTable.fTable[i2 + 4 + i3] = (short) rBBIStateDescriptor.fDtran[i3];
            }
        }
        return rBBIStateTable;
    }

    /* access modifiers changed from: package-private */
    public void buildSafeReverseTable() {
        int i;
        StringBuilder sb = new StringBuilder();
        int numCharCategories = this.fRB.fSetBuilder.getNumCharCategories();
        int size = this.fDStates.size();
        for (int i2 = 0; i2 < numCharCategories; i2++) {
            for (int i3 = 0; i3 < numCharCategories; i3++) {
                int i4 = 0;
                int i5 = -1;
                for (int i6 = 1; i6 < size; i6++) {
                    i4 = this.fDStates.get(this.fDStates.get(i6).fDtran[i2]).fDtran[i3];
                    if (i5 < 0) {
                        i5 = i4;
                    } else if (i5 != i4) {
                        break;
                    }
                }
                if (i5 == i4) {
                    sb.append((char) i2);
                    sb.append((char) i3);
                }
            }
        }
        this.fSafeTable = new ArrayList();
        int i7 = 0;
        while (true) {
            i = numCharCategories + 2;
            if (i7 >= i) {
                break;
            }
            this.fSafeTable.add(new short[numCharCategories]);
            i7++;
        }
        short[] sArr = this.fSafeTable.get(1);
        for (int i8 = 0; i8 < numCharCategories; i8++) {
            sArr[i8] = (short) (i8 + 2);
        }
        for (int i9 = 2; i9 < i; i9++) {
            System.arraycopy(sArr, 0, this.fSafeTable.get(i9), 0, sArr.length);
        }
        for (int i10 = 0; i10 < sb.length(); i10 += 2) {
            this.fSafeTable.get(sb.charAt(i10 + 1) + 2)[sb.charAt(i10)] = 0;
        }
        RBBIRuleBuilder.IntPair intPair = new RBBIRuleBuilder.IntPair(1, 0);
        while (findDuplicateSafeState(intPair)) {
            removeSafeState(intPair);
        }
    }

    /* access modifiers changed from: package-private */
    public int getSafeTableSize() {
        List<short[]> list = this.fSafeTable;
        if (list == null) {
            return 0;
        }
        return ((list.size() * ((this.fSafeTable.get(0).length * 2) + 8)) + 16 + 7) & -8;
    }

    /* access modifiers changed from: package-private */
    public RBBIDataWrapper.RBBIStateTable exportSafeTable() {
        RBBIDataWrapper.RBBIStateTable rBBIStateTable = new RBBIDataWrapper.RBBIStateTable();
        rBBIStateTable.fNumStates = this.fSafeTable.size();
        int length = this.fSafeTable.get(0).length;
        int i = length + 4;
        rBBIStateTable.fTable = new short[((getSafeTableSize() - 16) / 2)];
        rBBIStateTable.fRowLen = i * 2;
        for (int i2 = 0; i2 < rBBIStateTable.fNumStates; i2++) {
            short[] sArr = this.fSafeTable.get(i2);
            int i3 = i2 * i;
            for (int i4 = 0; i4 < length; i4++) {
                rBBIStateTable.fTable[i3 + 4 + i4] = sArr[i4];
            }
        }
        return rBBIStateTable;
    }

    /* access modifiers changed from: package-private */
    public void printSet(Collection<RBBINode> collection) {
        for (RBBINode rBBINode : collection) {
            RBBINode.printInt(rBBINode.fSerialNum, 8);
        }
        System.out.println();
    }

    /* access modifiers changed from: package-private */
    public void printStates() {
        System.out.print("state |           i n p u t     s y m b o l s \n");
        System.out.print("      | Acc  LA    Tag");
        for (int i = 0; i < this.fRB.fSetBuilder.getNumCharCategories(); i++) {
            RBBINode.printInt(i, 3);
        }
        System.out.print("\n");
        System.out.print("      |---------------");
        for (int i2 = 0; i2 < this.fRB.fSetBuilder.getNumCharCategories(); i2++) {
            System.out.print("---");
        }
        System.out.print("\n");
        for (int i3 = 0; i3 < this.fDStates.size(); i3++) {
            RBBIStateDescriptor rBBIStateDescriptor = this.fDStates.get(i3);
            RBBINode.printInt(i3, 5);
            System.out.print(" | ");
            RBBINode.printInt(rBBIStateDescriptor.fAccepting, 3);
            RBBINode.printInt(rBBIStateDescriptor.fLookAhead, 4);
            RBBINode.printInt(rBBIStateDescriptor.fTagsIdx, 6);
            System.out.print(" ");
            for (int i4 = 0; i4 < this.fRB.fSetBuilder.getNumCharCategories(); i4++) {
                RBBINode.printInt(rBBIStateDescriptor.fDtran[i4], 3);
            }
            System.out.print("\n");
        }
        System.out.print("\n\n");
    }

    /* access modifiers changed from: package-private */
    public void printReverseTable() {
        System.out.printf("    Safe Reverse Table \n", new Object[0]);
        List<short[]> list = this.fSafeTable;
        if (list == null) {
            System.out.printf("   --- nullptr ---\n", new Object[0]);
            return;
        }
        int length = list.get(0).length;
        System.out.printf("state |           i n p u t     s y m b o l s \n", new Object[0]);
        System.out.printf("      | Acc  LA    Tag", new Object[0]);
        for (int i = 0; i < length; i++) {
            System.out.printf(" %2d", Integer.valueOf(i));
        }
        System.out.printf("\n", new Object[0]);
        System.out.printf("      |---------------", new Object[0]);
        for (int i2 = 0; i2 < length; i2++) {
            System.out.printf("---", new Object[0]);
        }
        System.out.printf("\n", new Object[0]);
        for (int i3 = 0; i3 < this.fSafeTable.size(); i3++) {
            short[] sArr = this.fSafeTable.get(i3);
            System.out.printf("  %3d | ", Integer.valueOf(i3));
            System.out.printf("%3d %3d %5d ", 0, 0, 0);
            for (int i4 = 0; i4 < length; i4++) {
                System.out.printf(" %2d", Short.valueOf(sArr[i4]));
            }
            System.out.printf("\n", new Object[0]);
        }
        System.out.printf("\n\n", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    public void printRuleStatusTable() {
        List<Integer> list = this.fRB.fRuleStatusVals;
        System.out.print("index |  tags \n");
        System.out.print("-------------------\n");
        int i = 0;
        while (i < list.size()) {
            int intValue = list.get(i).intValue() + i + 1;
            RBBINode.printInt(i, 7);
            while (true) {
                i++;
                if (i >= intValue) {
                    break;
                }
                RBBINode.printInt(list.get(i).intValue(), 7);
            }
            System.out.print("\n");
            i = intValue;
        }
        System.out.print("\n\n");
    }
}
