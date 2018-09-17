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

    /* JADX WARNING: Missing block: B:19:0x006c, code:
            if (r0 != 10) goto L_0x0079;
     */
    /* JADX WARNING: Missing block: B:23:0x0091, code:
            throw createParseException("unexpected char = " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SIPHeader parse() throws ParseException {
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
                char la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            return visitedNetworkIDList;
        } finally {
            if (debug) {
                dbg_leave("VisitedNetworkIDParser.parse");
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x003c, code:
            r6.setVisitedNetworkID(r1.toString());
            super.parse(r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void parseQuotedString(PVisitedNetworkID visitedNetworkID) throws ParseException {
        if (debug) {
            dbg_enter("parseQuotedString");
        }
        try {
            StringBuffer retval = new StringBuffer();
            if (this.lexer.lookAhead(0) != '\"') {
                throw createParseException("unexpected char");
            }
            this.lexer.consume(1);
            loop0:
            while (true) {
                char next = this.lexer.getNextChar();
                if (next == '\"') {
                    break loop0;
                } else if (next == 0) {
                    throw new ParseException("unexpected EOL", 1);
                } else if (next == '\\') {
                    retval.append(next);
                    retval.append(this.lexer.getNextChar());
                } else {
                    retval.append(next);
                }
            }
        } finally {
            if (debug) {
                dbg_leave("parseQuotedString.parse");
            }
        }
    }

    protected void parseToken(PVisitedNetworkID visitedNetworkID) throws ParseException {
        this.lexer.match(4095);
        visitedNetworkID.setVisitedNetworkID(this.lexer.getNextToken());
        super.parse(visitedNetworkID);
    }
}
