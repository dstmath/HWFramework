package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PVisitedNetworkID;
import gov.nist.javax.sip.header.ims.PVisitedNetworkIDList;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PVisitedNetworkIDParser extends ParametersParser implements TokenTypes {
    public PVisitedNetworkIDParser(String networkID) {
        super(networkID);
    }

    protected PVisitedNetworkIDParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        char la;
        PVisitedNetworkIDList visitedNetworkIDList = new PVisitedNetworkIDList();
        if (debug) {
            dbg_enter("VisitedNetworkIDParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_VISITED_NETWORK_ID);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                PVisitedNetworkID visitedNetworkID = new PVisitedNetworkID();
                if (this.lexer.lookAhead(0) == '\"') {
                    parseQuotedString(visitedNetworkID);
                } else {
                    parseToken(visitedNetworkID);
                }
                visitedNetworkIDList.add((SIPHeader) visitedNetworkID);
                this.lexer.SPorHT();
                la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (la == '\n') {
                return visitedNetworkIDList;
            }
            throw createParseException("unexpected char = " + la);
        } finally {
            if (debug) {
                dbg_leave("VisitedNetworkIDParser.parse");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void parseQuotedString(PVisitedNetworkID visitedNetworkID) throws ParseException {
        boolean z;
        if (debug) {
            dbg_enter("parseQuotedString");
        }
        try {
            StringBuffer retval = new StringBuffer();
            if (this.lexer.lookAhead(0) == '\"') {
                this.lexer.consume(1);
                while (true) {
                    char next = this.lexer.getNextChar();
                    if (next == '\"') {
                        visitedNetworkID.setVisitedNetworkID(retval.toString());
                        super.parse(visitedNetworkID);
                        if (!z) {
                            return;
                        }
                        return;
                    } else if (next == 0) {
                        throw new ParseException("unexpected EOL", 1);
                    } else if (next == '\\') {
                        retval.append(next);
                        retval.append(this.lexer.getNextChar());
                    } else {
                        retval.append(next);
                    }
                }
            } else {
                throw createParseException("unexpected char");
            }
        } finally {
            if (debug) {
                dbg_leave("parseQuotedString.parse");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void parseToken(PVisitedNetworkID visitedNetworkID) throws ParseException {
        this.lexer.match(4095);
        visitedNetworkID.setVisitedNetworkID(this.lexer.getNextToken());
        super.parse(visitedNetworkID);
    }
}
