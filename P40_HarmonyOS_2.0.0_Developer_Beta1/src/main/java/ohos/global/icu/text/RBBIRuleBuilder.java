package ohos.global.icu.text;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.ICUDebug;
import ohos.global.icu.impl.RBBIDataWrapper;

/* access modifiers changed from: package-private */
public class RBBIRuleBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int U_BRK_ASSIGN_ERROR = 66054;
    static final int U_BRK_ERROR_LIMIT = 66064;
    static final int U_BRK_ERROR_START = 66048;
    static final int U_BRK_HEX_DIGITS_EXPECTED = 66050;
    static final int U_BRK_INIT_ERROR = 66059;
    static final int U_BRK_INTERNAL_ERROR = 66049;
    static final int U_BRK_MALFORMED_RULE_TAG = 66062;
    static final int U_BRK_MALFORMED_SET = 66063;
    static final int U_BRK_MISMATCHED_PAREN = 66056;
    static final int U_BRK_NEW_LINE_IN_QUOTED_STRING = 66057;
    static final int U_BRK_RULE_EMPTY_SET = 66060;
    static final int U_BRK_RULE_SYNTAX = 66052;
    static final int U_BRK_SEMICOLON_EXPECTED = 66051;
    static final int U_BRK_UNCLOSED_SET = 66053;
    static final int U_BRK_UNDEFINED_VARIABLE = 66058;
    static final int U_BRK_UNRECOGNIZED_OPTION = 66061;
    static final int U_BRK_VARIABLE_REDFINITION = 66055;
    static final int fForwardTree = 0;
    static final int fReverseTree = 1;
    static final int fSafeFwdTree = 2;
    static final int fSafeRevTree = 3;
    boolean fChainRules;
    String fDebugEnv;
    int fDefaultTree = 0;
    RBBITableBuilder fForwardTable;
    boolean fLBCMNoChain;
    boolean fLookAheadHardBreak;
    List<Integer> fRuleStatusVals;
    String fRules;
    RBBIRuleScanner fScanner;
    RBBISetBuilder fSetBuilder;
    Map<Set<Integer>, Integer> fStatusSets = new HashMap();
    StringBuilder fStrippedRules;
    RBBINode[] fTreeRoots = new RBBINode[4];
    List<RBBINode> fUSetNodes;

    static final int align8(int i) {
        return (i + 7) & -8;
    }

    RBBIRuleBuilder(String str) {
        this.fDebugEnv = ICUDebug.enabled("rbbi") ? ICUDebug.value("rbbi") : null;
        this.fRules = str;
        this.fStrippedRules = new StringBuilder(str);
        this.fUSetNodes = new ArrayList();
        this.fRuleStatusVals = new ArrayList();
        this.fScanner = new RBBIRuleScanner(this);
        this.fSetBuilder = new RBBISetBuilder(this);
    }

    /* access modifiers changed from: package-private */
    public void flattenData(OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        String stripRules = RBBIRuleScanner.stripRules(this.fStrippedRules.toString());
        int align8 = align8(this.fForwardTable.getTableSize());
        int align82 = align8(this.fForwardTable.getSafeTableSize());
        int align83 = align8(this.fSetBuilder.getTrieSize());
        int align84 = align8(this.fRuleStatusVals.size() * 4);
        ICUBinary.writeHeader(1114794784, 83886080, 0, dataOutputStream);
        int[] iArr = new int[20];
        iArr[0] = 45472;
        boolean z = true;
        iArr[1] = 83886080;
        iArr[2] = align8 + 80 + align82 + align84 + align83 + align8(stripRules.length() * 2);
        iArr[3] = this.fSetBuilder.getNumCharCategories();
        iArr[4] = 80;
        iArr[5] = align8;
        iArr[6] = iArr[4] + align8;
        iArr[7] = align82;
        iArr[8] = iArr[6] + iArr[7];
        iArr[9] = this.fSetBuilder.getTrieSize();
        iArr[12] = iArr[8] + iArr[9];
        iArr[13] = align84;
        iArr[10] = iArr[12] + align84;
        iArr[11] = stripRules.length() * 2;
        int i = 0;
        for (int i2 : iArr) {
            dataOutputStream.writeInt(i2);
            i += 4;
        }
        int put = i + this.fForwardTable.exportTable().put(dataOutputStream);
        RBBIDataWrapper.RBBIStateTable exportSafeTable = this.fForwardTable.exportSafeTable();
        Assert.assrt(put == iArr[6]);
        int put2 = put + exportSafeTable.put(dataOutputStream);
        Assert.assrt(put2 == iArr[8]);
        this.fSetBuilder.serializeTrie(outputStream);
        int i3 = put2 + iArr[9];
        while (i3 % 8 != 0) {
            dataOutputStream.write(0);
            i3++;
        }
        Assert.assrt(i3 == iArr[12]);
        for (Integer num : this.fRuleStatusVals) {
            dataOutputStream.writeInt(num.intValue());
            i3 += 4;
        }
        while (i3 % 8 != 0) {
            dataOutputStream.write(0);
            i3++;
        }
        if (i3 != iArr[10]) {
            z = false;
        }
        Assert.assrt(z);
        dataOutputStream.writeChars(stripRules);
        for (int length = i3 + (stripRules.length() * 2); length % 8 != 0; length++) {
            dataOutputStream.write(0);
        }
    }

    static void compileRules(String str, OutputStream outputStream) throws IOException {
        new RBBIRuleBuilder(str).build(outputStream);
    }

    /* access modifiers changed from: package-private */
    public void build(OutputStream outputStream) throws IOException {
        this.fScanner.parse();
        this.fSetBuilder.buildRanges();
        this.fForwardTable = new RBBITableBuilder(this, 0);
        this.fForwardTable.buildForwardTable();
        optimizeTables();
        this.fForwardTable.buildSafeReverseTable();
        String str = this.fDebugEnv;
        if (str != null && str.indexOf("states") >= 0) {
            this.fForwardTable.printStates();
            this.fForwardTable.printRuleStatusTable();
            this.fForwardTable.printReverseTable();
        }
        this.fSetBuilder.buildTrie();
        flattenData(outputStream);
    }

    /* access modifiers changed from: package-private */
    public static class IntPair {
        int first = 0;
        int second = 0;

        IntPair() {
        }

        IntPair(int i, int i2) {
            this.first = i;
            this.second = i2;
        }
    }

    /* access modifiers changed from: package-private */
    public void optimizeTables() {
        boolean z;
        do {
            z = false;
            IntPair intPair = new IntPair(3, 0);
            while (this.fForwardTable.findDuplCharClassFrom(intPair)) {
                this.fSetBuilder.mergeCategories(intPair);
                this.fForwardTable.removeColumn(intPair.second);
                z = true;
            }
            while (this.fForwardTable.removeDuplicateStates() > 0) {
                z = true;
            }
        } while (z);
    }
}
