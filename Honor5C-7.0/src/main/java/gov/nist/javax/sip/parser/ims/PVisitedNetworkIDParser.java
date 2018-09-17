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

    public SIPHeader parse() throws ParseException {
        PVisitedNetworkIDList visitedNetworkIDList = new PVisitedNetworkIDList();
        if (debug) {
            dbg_enter("VisitedNetworkIDParser.parse");
        }
        try {
            char la;
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

    /* JADX WARNING: inconsistent code. */
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
            while (true) {
                char next = this.lexer.getNextChar();
                if (next == '\"') {
                    break;
                } else if (next == '\u0000') {
                    break;
                } else if (next == '\\') {
                    retval.append(next);
                    retval.append(this.lexer.getNextChar());
                } else {
                    retval.append(next);
                }
            }
            visitedNetworkID.setVisitedNetworkID(retval.toString());
            super.parse(visitedNetworkID);
            if (debug) {
                dbg_leave("parseQuotedString.parse");
            }
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parseQuotedString.parse");
            }
        }
    }

    protected void parseToken(PVisitedNetworkID visitedNetworkID) throws ParseException {
        this.lexer.match(TokenTypes.ID);
        visitedNetworkID.setVisitedNetworkID(this.lexer.getNextToken());
        super.parse(visitedNetworkID);
    }
}
