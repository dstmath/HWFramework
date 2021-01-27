package ohos.global.icu.text;

import java.text.ParsePosition;
import java.util.HashMap;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.RBBIRuleParseTable;

/* access modifiers changed from: package-private */
public class RBBIRuleScanner {
    static final int chLS = 8232;
    static final int chNEL = 133;
    private static String gRuleSet_digit_char_pattern = "[0-9]";
    private static String gRuleSet_name_char_pattern = "[_\\p{L}\\p{N}]";
    private static String gRuleSet_name_start_char_pattern = "[_\\p{L}]";
    private static String gRuleSet_rule_char_pattern = "[^[\\p{Z}\\u0020-\\u007f]-[\\p{L}]-[\\p{N}]]";
    private static String gRuleSet_white_space_pattern = "[\\p{Pattern_White_Space}]";
    private static String kAny = "any";
    private static final int kStackSize = 100;
    RBBIRuleChar fC = new RBBIRuleChar();
    int fCharNum;
    int fLastChar;
    int fLineNum;
    boolean fLookAheadRule;
    int fNextIndex;
    boolean fNoChainInRule;
    RBBINode[] fNodeStack = new RBBINode[100];
    int fNodeStackPtr;
    int fOptionStart;
    boolean fQuoteMode;
    RBBIRuleBuilder fRB;
    boolean fReverseRule;
    int fRuleNum;
    UnicodeSet[] fRuleSets = new UnicodeSet[10];
    int fScanIndex;
    HashMap<String, RBBISetTableEl> fSetTable = new HashMap<>();
    short[] fStack = new short[100];
    int fStackPtr;
    RBBISymbolTable fSymbolTable;

    /* access modifiers changed from: package-private */
    public static class RBBIRuleChar {
        int fChar;
        boolean fEscaped;

        RBBIRuleChar() {
        }
    }

    RBBIRuleScanner(RBBIRuleBuilder rBBIRuleBuilder) {
        this.fRB = rBBIRuleBuilder;
        this.fLineNum = 1;
        this.fRuleSets[3] = new UnicodeSet(gRuleSet_rule_char_pattern);
        this.fRuleSets[4] = new UnicodeSet(gRuleSet_white_space_pattern);
        this.fRuleSets[1] = new UnicodeSet(gRuleSet_name_char_pattern);
        this.fRuleSets[2] = new UnicodeSet(gRuleSet_name_start_char_pattern);
        this.fRuleSets[0] = new UnicodeSet(gRuleSet_digit_char_pattern);
        this.fSymbolTable = new RBBISymbolTable(this);
    }

    /* access modifiers changed from: package-private */
    public boolean doParseActions(int i) {
        int i2 = 3;
        switch (i) {
            case 1:
                if (this.fNodeStack[this.fNodeStackPtr].fLeftChild == null) {
                    error(66058);
                    return false;
                }
                break;
            case 2:
                RBBINode pushNewNode = pushNewNode(0);
                findSetFor(kAny, pushNewNode, null);
                pushNewNode.fFirstPos = this.fScanIndex;
                pushNewNode.fLastPos = this.fNextIndex;
                pushNewNode.fText = this.fRB.fRules.substring(pushNewNode.fFirstPos, pushNewNode.fLastPos);
                break;
            case 3:
                fixOpStack(1);
                RBBINode[] rBBINodeArr = this.fNodeStack;
                int i3 = this.fNodeStackPtr;
                RBBINode rBBINode = rBBINodeArr[i3 - 2];
                RBBINode rBBINode2 = rBBINodeArr[i3 - 1];
                RBBINode rBBINode3 = rBBINodeArr[i3];
                rBBINode3.fFirstPos = rBBINode.fFirstPos;
                rBBINode3.fLastPos = this.fScanIndex;
                rBBINode3.fText = this.fRB.fRules.substring(rBBINode3.fFirstPos, rBBINode3.fLastPos);
                rBBINode2.fLeftChild = rBBINode3;
                rBBINode3.fParent = rBBINode2;
                this.fSymbolTable.addEntry(rBBINode2.fText, rBBINode2);
                this.fNodeStackPtr -= 3;
                break;
            case 4:
                fixOpStack(1);
                if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("rtree") >= 0) {
                    printNodeStack("end of rule");
                }
                Assert.assrt(this.fNodeStackPtr == 1);
                RBBINode rBBINode4 = this.fNodeStack[this.fNodeStackPtr];
                if (this.fLookAheadRule) {
                    RBBINode pushNewNode2 = pushNewNode(6);
                    RBBINode pushNewNode3 = pushNewNode(8);
                    this.fNodeStackPtr -= 2;
                    pushNewNode3.fLeftChild = rBBINode4;
                    pushNewNode3.fRightChild = pushNewNode2;
                    this.fNodeStack[this.fNodeStackPtr] = pushNewNode3;
                    pushNewNode2.fVal = this.fRuleNum;
                    pushNewNode2.fLookAheadEnd = true;
                    rBBINode4 = pushNewNode3;
                }
                rBBINode4.fRuleRoot = true;
                if (this.fRB.fChainRules && !this.fNoChainInRule) {
                    rBBINode4.fChainIn = true;
                }
                if (!this.fReverseRule) {
                    i2 = this.fRB.fDefaultTree;
                }
                if (this.fRB.fTreeRoots[i2] != null) {
                    RBBINode rBBINode5 = this.fNodeStack[this.fNodeStackPtr];
                    RBBINode rBBINode6 = this.fRB.fTreeRoots[i2];
                    RBBINode pushNewNode4 = pushNewNode(9);
                    pushNewNode4.fLeftChild = rBBINode6;
                    rBBINode6.fParent = pushNewNode4;
                    pushNewNode4.fRightChild = rBBINode5;
                    rBBINode5.fParent = pushNewNode4;
                    this.fRB.fTreeRoots[i2] = pushNewNode4;
                } else {
                    this.fRB.fTreeRoots[i2] = this.fNodeStack[this.fNodeStackPtr];
                }
                this.fReverseRule = false;
                this.fLookAheadRule = false;
                this.fNoChainInRule = false;
                this.fNodeStackPtr = 0;
                break;
            case 5:
                RBBINode rBBINode7 = this.fNodeStack[this.fNodeStackPtr];
                if (rBBINode7 != null && rBBINode7.fType == 2) {
                    rBBINode7.fLastPos = this.fScanIndex;
                    rBBINode7.fText = this.fRB.fRules.substring(rBBINode7.fFirstPos + 1, rBBINode7.fLastPos);
                    rBBINode7.fLeftChild = this.fSymbolTable.lookupNode(rBBINode7.fText);
                    break;
                } else {
                    error(66049);
                    break;
                }
                break;
            case 6:
                return false;
            case 7:
                fixOpStack(4);
                RBBINode[] rBBINodeArr2 = this.fNodeStack;
                int i4 = this.fNodeStackPtr;
                this.fNodeStackPtr = i4 - 1;
                RBBINode rBBINode8 = rBBINodeArr2[i4];
                RBBINode pushNewNode5 = pushNewNode(8);
                pushNewNode5.fLeftChild = rBBINode8;
                rBBINode8.fParent = pushNewNode5;
                break;
            case 8:
            case 13:
                break;
            case 9:
                fixOpStack(4);
                RBBINode[] rBBINodeArr3 = this.fNodeStack;
                int i5 = this.fNodeStackPtr;
                this.fNodeStackPtr = i5 - 1;
                RBBINode rBBINode9 = rBBINodeArr3[i5];
                RBBINode pushNewNode6 = pushNewNode(9);
                pushNewNode6.fLeftChild = rBBINode9;
                rBBINode9.fParent = pushNewNode6;
                break;
            case 10:
                fixOpStack(2);
                break;
            case 11:
                pushNewNode(7);
                this.fRuleNum++;
                break;
            case 12:
                pushNewNode(15);
                break;
            case 14:
                this.fNoChainInRule = true;
                break;
            case 15:
                String substring = this.fRB.fRules.substring(this.fOptionStart, this.fScanIndex);
                if (!substring.equals("chain")) {
                    if (!substring.equals("LBCMNoChain")) {
                        if (!substring.equals("forward")) {
                            if (!substring.equals("reverse")) {
                                if (!substring.equals("safe_forward")) {
                                    if (!substring.equals("safe_reverse")) {
                                        if (!substring.equals("lookAheadHardBreak")) {
                                            if (!substring.equals("quoted_literals_only")) {
                                                if (!substring.equals("unquoted_literals")) {
                                                    error(66061);
                                                    break;
                                                } else {
                                                    this.fRuleSets[3].applyPattern(gRuleSet_rule_char_pattern);
                                                    break;
                                                }
                                            } else {
                                                this.fRuleSets[3].clear();
                                                break;
                                            }
                                        } else {
                                            this.fRB.fLookAheadHardBreak = true;
                                            break;
                                        }
                                    } else {
                                        this.fRB.fDefaultTree = 3;
                                        break;
                                    }
                                } else {
                                    this.fRB.fDefaultTree = 2;
                                    break;
                                }
                            } else {
                                this.fRB.fDefaultTree = 1;
                                break;
                            }
                        } else {
                            this.fRB.fDefaultTree = 0;
                            break;
                        }
                    } else {
                        this.fRB.fLBCMNoChain = true;
                        break;
                    }
                } else {
                    this.fRB.fChainRules = true;
                    break;
                }
            case 16:
                this.fOptionStart = this.fScanIndex;
                break;
            case 17:
                this.fReverseRule = true;
                break;
            case 18:
                RBBINode pushNewNode7 = pushNewNode(0);
                findSetFor(String.valueOf((char) this.fC.fChar), pushNewNode7, null);
                pushNewNode7.fFirstPos = this.fScanIndex;
                pushNewNode7.fLastPos = this.fNextIndex;
                pushNewNode7.fText = this.fRB.fRules.substring(pushNewNode7.fFirstPos, pushNewNode7.fLastPos);
                break;
            case 19:
                error(66052);
                return false;
            case 20:
                error(66054);
                return false;
            case 21:
                scanSet();
                break;
            case 22:
                RBBINode pushNewNode8 = pushNewNode(4);
                pushNewNode8.fVal = this.fRuleNum;
                pushNewNode8.fFirstPos = this.fScanIndex;
                pushNewNode8.fLastPos = this.fNextIndex;
                pushNewNode8.fText = this.fRB.fRules.substring(pushNewNode8.fFirstPos, pushNewNode8.fLastPos);
                this.fLookAheadRule = true;
                break;
            case 23:
                this.fNodeStack[this.fNodeStackPtr - 1].fFirstPos = this.fNextIndex;
                pushNewNode(7);
                break;
            case 24:
                RBBINode pushNewNode9 = pushNewNode(5);
                pushNewNode9.fVal = 0;
                pushNewNode9.fFirstPos = this.fScanIndex;
                pushNewNode9.fLastPos = this.fNextIndex;
                break;
            case 25:
                pushNewNode(2).fFirstPos = this.fScanIndex;
                break;
            case 26:
                RBBINode rBBINode10 = this.fNodeStack[this.fNodeStackPtr];
                rBBINode10.fVal = (rBBINode10.fVal * 10) + UCharacter.digit((char) this.fC.fChar, 10);
                break;
            case 27:
                error(66062);
                return false;
            case 28:
                RBBINode rBBINode11 = this.fNodeStack[this.fNodeStackPtr];
                rBBINode11.fLastPos = this.fNextIndex;
                rBBINode11.fText = this.fRB.fRules.substring(rBBINode11.fFirstPos, rBBINode11.fLastPos);
                break;
            case 29:
                RBBINode[] rBBINodeArr4 = this.fNodeStack;
                int i6 = this.fNodeStackPtr;
                this.fNodeStackPtr = i6 - 1;
                RBBINode rBBINode12 = rBBINodeArr4[i6];
                RBBINode pushNewNode10 = pushNewNode(11);
                pushNewNode10.fLeftChild = rBBINode12;
                rBBINode12.fParent = pushNewNode10;
                break;
            case 30:
                RBBINode[] rBBINodeArr5 = this.fNodeStack;
                int i7 = this.fNodeStackPtr;
                this.fNodeStackPtr = i7 - 1;
                RBBINode rBBINode13 = rBBINodeArr5[i7];
                RBBINode pushNewNode11 = pushNewNode(12);
                pushNewNode11.fLeftChild = rBBINode13;
                rBBINode13.fParent = pushNewNode11;
                break;
            case 31:
                RBBINode[] rBBINodeArr6 = this.fNodeStack;
                int i8 = this.fNodeStackPtr;
                this.fNodeStackPtr = i8 - 1;
                RBBINode rBBINode14 = rBBINodeArr6[i8];
                RBBINode pushNewNode12 = pushNewNode(10);
                pushNewNode12.fLeftChild = rBBINode14;
                rBBINode14.fParent = pushNewNode12;
                break;
            case 32:
                error(66052);
                break;
            default:
                error(66049);
                return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void error(int i) {
        throw new IllegalArgumentException("Error " + i + " at line " + this.fLineNum + " column " + this.fCharNum);
    }

    /* access modifiers changed from: package-private */
    public void fixOpStack(int i) {
        RBBINode rBBINode;
        while (true) {
            rBBINode = this.fNodeStack[this.fNodeStackPtr - 1];
            if (rBBINode.fPrecedence == 0) {
                System.out.print("RBBIRuleScanner.fixOpStack, bad operator node");
                error(66049);
                return;
            } else if (rBBINode.fPrecedence < i || rBBINode.fPrecedence <= 2) {
                break;
            } else {
                RBBINode[] rBBINodeArr = this.fNodeStack;
                int i2 = this.fNodeStackPtr;
                rBBINode.fRightChild = rBBINodeArr[i2];
                rBBINodeArr[i2].fParent = rBBINode;
                this.fNodeStackPtr = i2 - 1;
            }
        }
        if (i <= 2) {
            if (rBBINode.fPrecedence != i) {
                error(66056);
            }
            RBBINode[] rBBINodeArr2 = this.fNodeStack;
            int i3 = this.fNodeStackPtr;
            rBBINodeArr2[i3 - 1] = rBBINodeArr2[i3];
            this.fNodeStackPtr = i3 - 1;
        }
    }

    /* access modifiers changed from: package-private */
    public static class RBBISetTableEl {
        String key;
        RBBINode val;

        RBBISetTableEl() {
        }
    }

    /* access modifiers changed from: package-private */
    public void findSetFor(String str, RBBINode rBBINode, UnicodeSet unicodeSet) {
        RBBISetTableEl rBBISetTableEl = this.fSetTable.get(str);
        boolean z = false;
        if (rBBISetTableEl != null) {
            rBBINode.fLeftChild = rBBISetTableEl.val;
            if (rBBINode.fLeftChild.fType == 1) {
                z = true;
            }
            Assert.assrt(z);
            return;
        }
        if (unicodeSet == null) {
            if (str.equals(kAny)) {
                unicodeSet = new UnicodeSet(0, 1114111);
            } else {
                int charAt = UTF16.charAt(str, 0);
                unicodeSet = new UnicodeSet(charAt, charAt);
            }
        }
        RBBINode rBBINode2 = new RBBINode(1);
        rBBINode2.fInputSet = unicodeSet;
        rBBINode2.fParent = rBBINode;
        rBBINode.fLeftChild = rBBINode2;
        rBBINode2.fText = str;
        this.fRB.fUSetNodes.add(rBBINode2);
        RBBISetTableEl rBBISetTableEl2 = new RBBISetTableEl();
        rBBISetTableEl2.key = str;
        rBBISetTableEl2.val = rBBINode2;
        this.fSetTable.put(rBBISetTableEl2.key, rBBISetTableEl2);
    }

    static String stripRules(String str) {
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        int i = 0;
        boolean z = false;
        while (i < length) {
            int codePointAt = str.codePointAt(i);
            boolean hasBinaryProperty = UCharacter.hasBinaryProperty(codePointAt, 43);
            if (!z || !hasBinaryProperty) {
                sb.appendCodePoint(codePointAt);
                z = hasBinaryProperty;
            }
            i = str.offsetByCodePoints(i, 1);
        }
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public int nextCharLL() {
        if (this.fNextIndex >= this.fRB.fRules.length()) {
            return -1;
        }
        int charAt = UTF16.charAt(this.fRB.fRules, this.fNextIndex);
        this.fNextIndex = UTF16.moveCodePointOffset(this.fRB.fRules, this.fNextIndex, 1);
        if (charAt == 13 || charAt == chNEL || charAt == chLS || (charAt == 10 && this.fLastChar != 13)) {
            this.fLineNum++;
            this.fCharNum = 0;
            if (this.fQuoteMode) {
                error(66057);
                this.fQuoteMode = false;
            }
        } else if (charAt != 10) {
            this.fCharNum++;
        }
        this.fLastChar = charAt;
        return charAt;
    }

    /* access modifiers changed from: package-private */
    public void nextChar(RBBIRuleChar rBBIRuleChar) {
        this.fScanIndex = this.fNextIndex;
        rBBIRuleChar.fChar = nextCharLL();
        rBBIRuleChar.fEscaped = false;
        if (rBBIRuleChar.fChar == 39) {
            if (UTF16.charAt(this.fRB.fRules, this.fNextIndex) == 39) {
                rBBIRuleChar.fChar = nextCharLL();
                rBBIRuleChar.fEscaped = true;
            } else {
                this.fQuoteMode = !this.fQuoteMode;
                if (this.fQuoteMode) {
                    rBBIRuleChar.fChar = 40;
                } else {
                    rBBIRuleChar.fChar = 41;
                }
                rBBIRuleChar.fEscaped = false;
                return;
            }
        }
        if (this.fQuoteMode) {
            rBBIRuleChar.fEscaped = true;
            return;
        }
        if (rBBIRuleChar.fChar == 35) {
            do {
                rBBIRuleChar.fChar = nextCharLL();
                if (rBBIRuleChar.fChar == -1 || rBBIRuleChar.fChar == 13 || rBBIRuleChar.fChar == 10 || rBBIRuleChar.fChar == chNEL) {
                    break;
                }
            } while (rBBIRuleChar.fChar != chLS);
            for (int i = this.fScanIndex; i < this.fNextIndex - 1; i++) {
                this.fRB.fStrippedRules.setCharAt(i, ' ');
            }
        }
        if (rBBIRuleChar.fChar != -1 && rBBIRuleChar.fChar == 92) {
            rBBIRuleChar.fEscaped = true;
            int[] iArr = {this.fNextIndex};
            rBBIRuleChar.fChar = Utility.unescapeAt(this.fRB.fRules, iArr);
            if (iArr[0] == this.fNextIndex) {
                error(66050);
            }
            this.fCharNum += iArr[0] - this.fNextIndex;
            this.fNextIndex = iArr[0];
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:89:0x0007 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:93:0x0007 */
    /* JADX DEBUG: Multi-variable search result rejected for r1v45, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r1v46, resolved type: short[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v47, resolved type: short */
    /* JADX DEBUG: Multi-variable search result rejected for r1v48, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    public void parse() {
        RBBIRuleParseTable.RBBIRuleTableElement rBBIRuleTableElement;
        nextChar(this.fC);
        short s = 1;
        while (s != 0) {
            RBBIRuleParseTable.RBBIRuleTableElement rBBIRuleTableElement2 = RBBIRuleParseTable.gRuleParseStateTable[s];
            if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("scan") >= 0) {
                System.out.println("char, line, col = ('" + ((char) this.fC.fChar) + "', " + this.fLineNum + ", " + this.fCharNum + "    state = " + rBBIRuleTableElement2.fStateName);
            }
            while (true) {
                rBBIRuleTableElement = RBBIRuleParseTable.gRuleParseStateTable[s];
                if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("scan") >= 0) {
                    System.out.print(".");
                }
                if ((rBBIRuleTableElement.fCharClass >= 127 || this.fC.fEscaped || rBBIRuleTableElement.fCharClass != this.fC.fChar) && rBBIRuleTableElement.fCharClass != 255 && ((rBBIRuleTableElement.fCharClass != 254 || !this.fC.fEscaped) && (!(rBBIRuleTableElement.fCharClass == 253 && this.fC.fEscaped && (this.fC.fChar == 80 || this.fC.fChar == 112)) && (!(rBBIRuleTableElement.fCharClass == 252 && this.fC.fChar == -1) && (rBBIRuleTableElement.fCharClass < 128 || rBBIRuleTableElement.fCharClass >= 240 || this.fC.fEscaped || this.fC.fChar == -1 || !this.fRuleSets[rBBIRuleTableElement.fCharClass - 128].contains(this.fC.fChar)))))) {
                    s++;
                }
            }
            if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("scan") >= 0) {
                System.out.println("");
            }
            if (!doParseActions(rBBIRuleTableElement.fAction)) {
                break;
            }
            if (rBBIRuleTableElement.fPushState != 0) {
                this.fStackPtr++;
                if (this.fStackPtr >= 100) {
                    System.out.println("RBBIRuleScanner.parse() - state stack overflow.");
                    error(66049);
                }
                this.fStack[this.fStackPtr] = rBBIRuleTableElement.fPushState;
            }
            if (rBBIRuleTableElement.fNextChar) {
                nextChar(this.fC);
            }
            if (rBBIRuleTableElement.fNextState != 255) {
                s = rBBIRuleTableElement.fNextState;
            } else {
                short[] sArr = this.fStack;
                int i = this.fStackPtr;
                s = sArr[i];
                this.fStackPtr = i - 1;
                if (this.fStackPtr < 0) {
                    System.out.println("RBBIRuleScanner.parse() - state stack underflow.");
                    error(66049);
                }
            }
        }
        if (this.fRB.fTreeRoots[0] == null) {
            error(66052);
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("symbols") >= 0) {
            this.fSymbolTable.rbbiSymtablePrint();
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("ptree") >= 0) {
            System.out.println("Completed Forward Rules Parse Tree...");
            this.fRB.fTreeRoots[0].printTree(true);
            System.out.println("\nCompleted Reverse Rules Parse Tree...");
            this.fRB.fTreeRoots[1].printTree(true);
            System.out.println("\nCompleted Safe Point Forward Rules Parse Tree...");
            if (this.fRB.fTreeRoots[2] == null) {
                System.out.println("  -- null -- ");
            } else {
                this.fRB.fTreeRoots[2].printTree(true);
            }
            System.out.println("\nCompleted Safe Point Reverse Rules Parse Tree...");
            if (this.fRB.fTreeRoots[3] == null) {
                System.out.println("  -- null -- ");
            } else {
                this.fRB.fTreeRoots[3].printTree(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void printNodeStack(String str) {
        System.out.println(str + ".  Dumping node stack...\n");
        for (int i = this.fNodeStackPtr; i > 0; i--) {
            this.fNodeStack[i].printTree(true);
        }
    }

    /* access modifiers changed from: package-private */
    public RBBINode pushNewNode(int i) {
        this.fNodeStackPtr++;
        if (this.fNodeStackPtr >= 100) {
            System.out.println("RBBIRuleScanner.pushNewNode - stack overflow.");
            error(66049);
        }
        this.fNodeStack[this.fNodeStackPtr] = new RBBINode(i);
        return this.fNodeStack[this.fNodeStackPtr];
    }

    /* access modifiers changed from: package-private */
    public void scanSet() {
        UnicodeSet unicodeSet;
        ParsePosition parsePosition = new ParsePosition(this.fScanIndex);
        int i = this.fScanIndex;
        try {
            unicodeSet = new UnicodeSet(this.fRB.fRules, parsePosition, this.fSymbolTable, 1);
        } catch (Exception unused) {
            error(66063);
            unicodeSet = null;
        }
        if (unicodeSet.isEmpty()) {
            error(66060);
        }
        int index = parsePosition.getIndex();
        while (this.fNextIndex < index) {
            nextCharLL();
        }
        RBBINode pushNewNode = pushNewNode(0);
        pushNewNode.fFirstPos = i;
        pushNewNode.fLastPos = this.fNextIndex;
        pushNewNode.fText = this.fRB.fRules.substring(pushNewNode.fFirstPos, pushNewNode.fLastPos);
        findSetFor(pushNewNode.fText, pushNewNode, unicodeSet);
    }
}
