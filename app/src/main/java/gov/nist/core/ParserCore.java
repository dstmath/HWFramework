package gov.nist.core;

import java.text.ParseException;

public abstract class ParserCore {
    public static final boolean debug = false;
    static int nesting_level;
    protected LexerCore lexer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.core.ParserCore.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.core.ParserCore.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.core.ParserCore.<clinit>():void");
    }

    protected gov.nist.core.NameValue nameValue(char r13) throws java.text.ParseException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x009c in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r12 = this;
        r8 = debug;
        if (r8 == 0) goto L_0x000a;
    L_0x0004:
        r8 = "nameValue";
        r12.dbg_enter(r8);
    L_0x000a:
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = 4095; // 0xfff float:5.738E-42 double:2.023E-320;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8.match(r9);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r3 = r8.getNextToken();	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8.SPorHT();	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r5 = 0;
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = 0;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r2 = r8.lookAhead(r9);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        if (r2 != r13) goto L_0x0072;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
    L_0x0026:
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = 1;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8.consume(r9);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8.SPorHT();	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r6 = 0;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r1 = 0;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = 0;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8 = r8.lookAhead(r9);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = 34;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        if (r8 != r9) goto L_0x005c;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
    L_0x003e:
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r6 = r8.quotedString();	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r5 = 1;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
    L_0x0045:
        r4 = new gov.nist.core.NameValue;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8 = r3.tokenValue;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r4.<init>(r8, r6, r1);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        if (r5 == 0) goto L_0x0051;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
    L_0x004e:
        r4.setQuotedValue();	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
    L_0x0051:
        r8 = debug;
        if (r8 == 0) goto L_0x005b;
    L_0x0055:
        r8 = "nameValue";
        r12.dbg_leave(r8);
    L_0x005b:
        return r4;
    L_0x005c:
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = 4095; // 0xfff float:5.738E-42 double:2.023E-320;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8.match(r9);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8 = r12.lexer;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r7 = r8.getNextToken();	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r6 = r7.tokenValue;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        if (r6 != 0) goto L_0x0045;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
    L_0x006d:
        r6 = "";	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r1 = 1;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        goto L_0x0045;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
    L_0x0072:
        r8 = new gov.nist.core.NameValue;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = r3.tokenValue;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r10 = "";	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r11 = 1;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8.<init>(r9, r10, r11);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = debug;
        if (r9 == 0) goto L_0x0087;
    L_0x0081:
        r9 = "nameValue";
        r12.dbg_leave(r9);
    L_0x0087:
        return r8;
    L_0x0088:
        r0 = move-exception;
        r8 = new gov.nist.core.NameValue;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = r3.tokenValue;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r10 = 0;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r11 = 0;	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r8.<init>(r9, r10, r11);	 Catch:{ ParseException -> 0x0088, all -> 0x009d }
        r9 = debug;
        if (r9 == 0) goto L_0x009c;
    L_0x0096:
        r9 = "nameValue";
        r12.dbg_leave(r9);
    L_0x009c:
        return r8;
    L_0x009d:
        r8 = move-exception;
        r9 = debug;
        if (r9 == 0) goto L_0x00a8;
    L_0x00a2:
        r9 = "nameValue";
        r12.dbg_leave(r9);
    L_0x00a8:
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.core.ParserCore.nameValue(char):gov.nist.core.NameValue");
    }

    protected void dbg_enter(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nesting_level; i++) {
            stringBuffer.append(Separators.GREATER_THAN);
        }
        if (debug) {
            System.out.println(stringBuffer + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level++;
    }

    protected void dbg_leave(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nesting_level; i++) {
            stringBuffer.append(Separators.LESS_THAN);
        }
        if (debug) {
            System.out.println(stringBuffer + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level--;
    }

    protected NameValue nameValue() throws ParseException {
        return nameValue('=');
    }

    protected void peekLine(String rule) {
        if (debug) {
            Debug.println(rule + Separators.SP + this.lexer.peekLine());
        }
    }
}
