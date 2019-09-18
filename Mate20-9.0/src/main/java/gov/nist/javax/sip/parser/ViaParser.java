package gov.nist.javax.sip.parser;

import gov.nist.core.HostNameParser;
import gov.nist.core.LexerCore;
import gov.nist.core.NameValue;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.Protocol;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import java.text.ParseException;

public class ViaParser extends HeaderParser {
    public ViaParser(String via) {
        super(via);
    }

    public ViaParser(Lexer lexer) {
        super(lexer);
    }

    private void parseVia(Via v) throws ParseException {
        this.lexer.match(4095);
        Token protocolName = this.lexer.getNextToken();
        this.lexer.SPorHT();
        this.lexer.match(47);
        this.lexer.SPorHT();
        this.lexer.match(4095);
        this.lexer.SPorHT();
        Token protocolVersion = this.lexer.getNextToken();
        this.lexer.SPorHT();
        this.lexer.match(47);
        this.lexer.SPorHT();
        this.lexer.match(4095);
        this.lexer.SPorHT();
        Token transport = this.lexer.getNextToken();
        this.lexer.SPorHT();
        Protocol protocol = new Protocol();
        protocol.setProtocolName(protocolName.getTokenValue());
        protocol.setProtocolVersion(protocolVersion.getTokenValue());
        protocol.setTransport(transport.getTokenValue());
        v.setSentProtocol(protocol);
        v.setSentBy(new HostNameParser((LexerCore) getLexer()).hostPort(true));
        this.lexer.SPorHT();
        while (this.lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
            this.lexer.SPorHT();
            NameValue nameValue = nameValue();
            if (!nameValue.getName().equals("branch") || ((String) nameValue.getValueAsObject()) != null) {
                v.setParameter(nameValue);
                this.lexer.SPorHT();
            } else {
                throw new ParseException("null branch Id", this.lexer.getPtr());
            }
        }
        if (this.lexer.lookAhead(0) == '(') {
            this.lexer.selectLexer("charLexer");
            this.lexer.consume(1);
            StringBuffer comment = new StringBuffer();
            while (true) {
                char ch = this.lexer.lookAhead(0);
                if (ch == ')') {
                    this.lexer.consume(1);
                    break;
                } else if (ch == '\\') {
                    comment.append(this.lexer.getNextToken().getTokenValue());
                    this.lexer.consume(1);
                    comment.append(this.lexer.getNextToken().getTokenValue());
                    this.lexer.consume(1);
                } else if (ch == 10) {
                    break;
                } else {
                    comment.append(ch);
                    this.lexer.consume(1);
                }
            }
            v.setComment(comment.toString());
        }
    }

    /* access modifiers changed from: protected */
    public NameValue nameValue() throws ParseException {
        Token name;
        String str;
        if (debug) {
            dbg_enter("nameValue");
        }
        try {
            this.lexer.match(4095);
            name = this.lexer.getNextToken();
            this.lexer.SPorHT();
            boolean quoted = false;
            if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                this.lexer.SPorHT();
                if (name.getTokenValue().compareToIgnoreCase("received") == 0) {
                    str = this.lexer.byteStringNoSemicolon();
                } else if (this.lexer.lookAhead(0) == '\"') {
                    str = this.lexer.quotedString();
                    quoted = true;
                } else {
                    this.lexer.match(4095);
                    str = this.lexer.getNextToken().getTokenValue();
                }
                NameValue nv = new NameValue(name.getTokenValue().toLowerCase(), str);
                if (quoted) {
                    nv.setQuotedValue();
                }
                if (debug) {
                    dbg_leave("nameValue");
                }
                return nv;
            }
            NameValue nameValue = new NameValue(name.getTokenValue().toLowerCase(), null);
            if (debug) {
                dbg_leave("nameValue");
            }
            return nameValue;
        } catch (ParseException e) {
            NameValue nameValue2 = new NameValue(name.getTokenValue(), null);
            if (debug) {
                dbg_leave("nameValue");
            }
            return nameValue2;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("nameValue");
            }
            throw th;
        }
    }

    public SIPHeader parse() throws ParseException {
        String str;
        if (debug) {
            dbg_enter("parse");
        }
        try {
            ViaList viaList = new ViaList();
            this.lexer.match(TokenTypes.VIA);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            do {
                Via v = new Via();
                parseVia(v);
                viaList.add(v);
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) == ',') {
                    this.lexer.consume(1);
                    this.lexer.SPorHT();
                }
            } while (this.lexer.lookAhead(0) != 10);
            this.lexer.match(10);
            return viaList;
        } finally {
            if (debug) {
                str = "parse";
                dbg_leave(str);
            }
        }
    }
}
