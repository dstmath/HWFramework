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

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        RequireList requireList = new RequireList();
        if (debug) {
            dbg_enter("RequireParser.parse");
        }
        try {
            headerName(TokenTypes.REQUIRE);
            while (this.lexer.lookAhead(0) != '\n') {
                Require r = new Require();
                r.setHeaderName("Require");
                this.lexer.match(4095);
                r.setOptionTag(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                requireList.add((RequireList) r);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    Require r2 = new Require();
                    this.lexer.match(4095);
                    r2.setOptionTag(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    requireList.add((RequireList) r2);
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
