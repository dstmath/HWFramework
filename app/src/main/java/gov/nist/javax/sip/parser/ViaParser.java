package gov.nist.javax.sip.parser;

import gov.nist.core.HostNameParser;
import gov.nist.core.NameValue;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.Protocol;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import java.text.ParseException;

public class ViaParser extends HeaderParser {
    protected gov.nist.core.NameValue nameValue() throws java.text.ParseException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00b7 in list []
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
        r10 = this;
        r7 = debug;
        if (r7 == 0) goto L_0x000a;
    L_0x0004:
        r7 = "nameValue";
        r10.dbg_enter(r7);
    L_0x000a:
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = 4095; // 0xfff float:5.738E-42 double:2.023E-320;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7.match(r8);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r2 = r7.getNextToken();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7.SPorHT();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r4 = 0;
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = 0;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r1 = r7.lookAhead(r8);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = 61;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        if (r1 != r7) goto L_0x0089;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x0028:
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = 1;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7.consume(r8);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7.SPorHT();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r5 = 0;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r2.getTokenValue();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = "received";	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r7.compareToIgnoreCase(r8);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        if (r7 != 0) goto L_0x0064;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x0041:
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r5 = r7.byteStringNoSemicolon();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x0047:
        r3 = new gov.nist.core.NameValue;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r2.getTokenValue();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r7.toLowerCase();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r3.<init>(r7, r5);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        if (r4 == 0) goto L_0x0059;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x0056:
        r3.setQuotedValue();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x0059:
        r7 = debug;
        if (r7 == 0) goto L_0x0063;
    L_0x005d:
        r7 = "nameValue";
        r10.dbg_leave(r7);
    L_0x0063:
        return r3;
    L_0x0064:
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = 0;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r7.lookAhead(r8);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = 34;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        if (r7 != r8) goto L_0x0077;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x006f:
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r5 = r7.quotedString();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r4 = 1;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        goto L_0x0047;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x0077:
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = 4095; // 0xfff float:5.738E-42 double:2.023E-320;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7.match(r8);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7 = r10.lexer;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r6 = r7.getNextToken();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r5 = r6.getTokenValue();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        goto L_0x0047;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
    L_0x0089:
        r7 = new gov.nist.core.NameValue;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = r2.getTokenValue();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = r8.toLowerCase();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r9 = 0;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7.<init>(r8, r9);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = debug;
        if (r8 == 0) goto L_0x00a1;
    L_0x009b:
        r8 = "nameValue";
        r10.dbg_leave(r8);
    L_0x00a1:
        return r7;
    L_0x00a2:
        r0 = move-exception;
        r7 = new gov.nist.core.NameValue;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = r2.getTokenValue();	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r9 = 0;	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r7.<init>(r8, r9);	 Catch:{ ParseException -> 0x00a2, all -> 0x00b8 }
        r8 = debug;
        if (r8 == 0) goto L_0x00b7;
    L_0x00b1:
        r8 = "nameValue";
        r10.dbg_leave(r8);
    L_0x00b7:
        return r7;
    L_0x00b8:
        r7 = move-exception;
        r8 = debug;
        if (r8 == 0) goto L_0x00c3;
    L_0x00bd:
        r8 = "nameValue";
        r10.dbg_leave(r8);
    L_0x00c3:
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.parser.ViaParser.nameValue():gov.nist.core.NameValue");
    }

    public ViaParser(String via) {
        super(via);
    }

    public ViaParser(Lexer lexer) {
        super(lexer);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseVia(Via v) throws ParseException {
        this.lexer.match(TokenTypes.ID);
        Token protocolName = this.lexer.getNextToken();
        this.lexer.SPorHT();
        this.lexer.match(47);
        this.lexer.SPorHT();
        this.lexer.match(TokenTypes.ID);
        this.lexer.SPorHT();
        Token protocolVersion = this.lexer.getNextToken();
        this.lexer.SPorHT();
        this.lexer.match(47);
        this.lexer.SPorHT();
        this.lexer.match(TokenTypes.ID);
        this.lexer.SPorHT();
        Token transport = this.lexer.getNextToken();
        this.lexer.SPorHT();
        Protocol protocol = new Protocol();
        protocol.setProtocolName(protocolName.getTokenValue());
        protocol.setProtocolVersion(protocolVersion.getTokenValue());
        protocol.setTransport(transport.getTokenValue());
        v.setSentProtocol(protocol);
        v.setSentBy(new HostNameParser(getLexer()).hostPort(true));
        this.lexer.SPorHT();
        while (this.lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
            this.lexer.SPorHT();
            NameValue nameValue = nameValue();
            if (nameValue.getName().equals(Via.BRANCH) && ((String) nameValue.getValueAsObject()) == null) {
                throw new ParseException("null branch Id", this.lexer.getPtr());
            }
            v.setParameter(nameValue);
            this.lexer.SPorHT();
        }
        if (this.lexer.lookAhead(0) == '(') {
            this.lexer.selectLexer("charLexer");
            this.lexer.consume(1);
            StringBuffer comment = new StringBuffer();
            while (true) {
                char ch = this.lexer.lookAhead(0);
                if (ch != ')') {
                    if (ch != '\\') {
                        if (ch == '\n') {
                            break;
                        }
                        comment.append(ch);
                        this.lexer.consume(1);
                    } else {
                        comment.append(this.lexer.getNextToken().getTokenValue());
                        this.lexer.consume(1);
                        comment.append(this.lexer.getNextToken().getTokenValue());
                        this.lexer.consume(1);
                    }
                } else {
                    break;
                }
            }
            v.setComment(comment.toString());
        }
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            ViaList viaList = new ViaList();
            this.lexer.match(TokenTypes.VIA);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                Via v = new Via();
                parseVia(v);
                viaList.add((SIPHeader) v);
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) == ',') {
                    this.lexer.consume(1);
                    this.lexer.SPorHT();
                }
                if (this.lexer.lookAhead(0) == '\n') {
                    break;
                }
            }
            this.lexer.match(10);
            return viaList;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }
}
