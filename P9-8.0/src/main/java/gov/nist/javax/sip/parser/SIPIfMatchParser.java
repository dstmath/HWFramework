package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SIPIfMatch;
import java.text.ParseException;

public class SIPIfMatchParser extends HeaderParser {
    public SIPIfMatchParser(String etag) {
        super(etag);
    }

    protected SIPIfMatchParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("SIPIfMatch.parse");
        }
        SIPIfMatch sipIfMatch = new SIPIfMatch();
        try {
            headerName(TokenTypes.SIP_IF_MATCH);
            this.lexer.SPorHT();
            this.lexer.match(4095);
            sipIfMatch.setETag(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            this.lexer.match(10);
            return sipIfMatch;
        } finally {
            if (debug) {
                dbg_leave("SIPIfMatch.parse");
            }
        }
    }
}
