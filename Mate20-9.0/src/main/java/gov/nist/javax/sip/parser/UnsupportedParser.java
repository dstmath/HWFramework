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
        String str;
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
                unsupportedList.add(unsupported);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    Unsupported unsupported2 = new Unsupported();
                    this.lexer.match(4095);
                    unsupported2.setOptionTag(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    unsupportedList.add(unsupported2);
                }
            }
            return unsupportedList;
        } finally {
            if (debug) {
                str = "UnsupportedParser.parse";
                dbg_leave(str);
            }
        }
    }
}
