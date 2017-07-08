package android.icu.text;

class RBBIRuleParseTable {
    static final short doCheckVarDef = (short) 1;
    static final short doDotAny = (short) 2;
    static final short doEndAssign = (short) 3;
    static final short doEndOfRule = (short) 4;
    static final short doEndVariableName = (short) 5;
    static final short doExit = (short) 6;
    static final short doExprCatOperator = (short) 7;
    static final short doExprFinished = (short) 8;
    static final short doExprOrOperator = (short) 9;
    static final short doExprRParen = (short) 10;
    static final short doExprStart = (short) 11;
    static final short doLParen = (short) 12;
    static final short doNOP = (short) 13;
    static final short doOptionEnd = (short) 14;
    static final short doOptionStart = (short) 15;
    static final short doReverseDir = (short) 16;
    static final short doRuleChar = (short) 17;
    static final short doRuleError = (short) 18;
    static final short doRuleErrorAssignExpr = (short) 19;
    static final short doScanUnicodeSet = (short) 20;
    static final short doSlash = (short) 21;
    static final short doStartAssign = (short) 22;
    static final short doStartTagValue = (short) 23;
    static final short doStartVariableName = (short) 24;
    static final short doTagDigit = (short) 25;
    static final short doTagExpectedError = (short) 26;
    static final short doTagValue = (short) 27;
    static final short doUnaryOpPlus = (short) 28;
    static final short doUnaryOpQuestion = (short) 29;
    static final short doUnaryOpStar = (short) 30;
    static final short doVariableNameExpectedErr = (short) 31;
    static RBBIRuleTableElement[] gRuleParseStateTable = null;
    static final short kRuleSet_default = (short) 255;
    static final short kRuleSet_digit_char = (short) 128;
    static final short kRuleSet_eof = (short) 252;
    static final short kRuleSet_escaped = (short) 254;
    static final short kRuleSet_name_char = (short) 129;
    static final short kRuleSet_name_start_char = (short) 130;
    static final short kRuleSet_rule_char = (short) 131;
    static final short kRuleSet_white_space = (short) 132;

    static class RBBIRuleTableElement {
        short fAction;
        short fCharClass;
        boolean fNextChar;
        short fNextState;
        short fPushState;
        String fStateName;

        RBBIRuleTableElement(short a, int cc, int ns, int ps, boolean nc, String sn) {
            this.fAction = a;
            this.fCharClass = (short) cc;
            this.fNextState = (short) ns;
            this.fPushState = (short) ps;
            this.fNextChar = nc;
            this.fStateName = sn;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.RBBIRuleParseTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.RBBIRuleParseTable.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RBBIRuleParseTable.<clinit>():void");
    }

    RBBIRuleParseTable() {
    }
}
