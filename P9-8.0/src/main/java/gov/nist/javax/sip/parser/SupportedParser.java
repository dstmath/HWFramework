package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Supported;
import gov.nist.javax.sip.header.SupportedList;
import java.text.ParseException;

public class SupportedParser extends HeaderParser {
    public SupportedParser(String supported) {
        super(supported);
    }

    protected SupportedParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        SupportedList supportedList = new SupportedList();
        if (debug) {
            dbg_enter("SupportedParser.parse");
        }
        try {
            headerName(2068);
            while (this.lexer.lookAhead(0) != 10) {
                this.lexer.SPorHT();
                Supported supported = new Supported();
                supported.setHeaderName("Supported");
                this.lexer.match(4095);
                supported.setOptionTag(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                supportedList.add((SIPHeader) supported);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    supported = new Supported();
                    this.lexer.match(4095);
                    supported.setOptionTag(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    supportedList.add((SIPHeader) supported);
                }
            }
            return supportedList;
        } finally {
            if (debug) {
                dbg_leave("SupportedParser.parse");
            }
        }
    }
}
