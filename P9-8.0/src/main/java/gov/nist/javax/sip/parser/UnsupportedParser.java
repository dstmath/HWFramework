package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Unsupported;
import gov.nist.javax.sip.header.UnsupportedList;
import java.text.ParseException;

public class UnsupportedParser extends HeaderParser {
    public UnsupportedParser(String unsupported) {
        super(unsupported);
    }

    protected UnsupportedParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        UnsupportedList unsupportedList = new UnsupportedList();
        if (debug) {
            dbg_enter("UnsupportedParser.parse");
        }
        try {
            headerName(TokenTypes.UNSUPPORTED);
            while (this.lexer.lookAhead(0) != 10) {
                this.lexer.SPorHT();
                Unsupported unsupported = new Unsupported();
                unsupported.setHeaderName("Unsupported");
                this.lexer.match(4095);
                unsupported.setOptionTag(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                unsupportedList.add((SIPHeader) unsupported);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    unsupported = new Unsupported();
                    this.lexer.match(4095);
                    unsupported.setOptionTag(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    unsupportedList.add((SIPHeader) unsupported);
                }
            }
            return unsupportedList;
        } finally {
            if (debug) {
                dbg_leave("UnsupportedParser.parse");
            }
        }
    }
}
