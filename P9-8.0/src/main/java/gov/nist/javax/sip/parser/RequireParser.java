package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Require;
import gov.nist.javax.sip.header.RequireList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class RequireParser extends HeaderParser {
    public RequireParser(String require) {
        super(require);
    }

    protected RequireParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        RequireList requireList = new RequireList();
        if (debug) {
            dbg_enter("RequireParser.parse");
        }
        try {
            headerName(TokenTypes.REQUIRE);
            while (this.lexer.lookAhead(0) != 10) {
                Require r = new Require();
                r.setHeaderName("Require");
                this.lexer.match(4095);
                r.setOptionTag(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                requireList.add((SIPHeader) r);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    r = new Require();
                    this.lexer.match(4095);
                    r.setOptionTag(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    requireList.add((SIPHeader) r);
                }
            }
            return requireList;
        } finally {
            if (debug) {
                dbg_leave("RequireParser.parse");
            }
        }
    }
}
