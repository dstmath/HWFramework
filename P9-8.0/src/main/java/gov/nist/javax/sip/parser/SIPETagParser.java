package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPETag;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class SIPETagParser extends HeaderParser {
    public SIPETagParser(String etag) {
        super(etag);
    }

    protected SIPETagParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("SIPEtag.parse");
        }
        SIPETag sipEtag = new SIPETag();
        try {
            headerName(TokenTypes.SIP_ETAG);
            this.lexer.SPorHT();
            this.lexer.match(4095);
            sipEtag.setETag(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            this.lexer.match(10);
            return sipEtag;
        } finally {
            if (debug) {
                dbg_leave("SIPEtag.parse");
            }
        }
    }
}
