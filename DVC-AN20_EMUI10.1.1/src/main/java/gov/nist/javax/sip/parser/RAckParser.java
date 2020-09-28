package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.RAck;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class RAckParser extends HeaderParser {
    public RAckParser(String rack) {
        super(rack);
    }

    protected RAckParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("RAckParser.parse");
        }
        RAck rack = new RAck();
        try {
            headerName(TokenTypes.RACK);
            rack.setHeaderName("RAck");
            try {
                rack.setRSequenceNumber(Long.parseLong(this.lexer.number()));
                this.lexer.SPorHT();
                rack.setCSequenceNumber(Long.parseLong(this.lexer.number()));
                this.lexer.SPorHT();
                this.lexer.match(4095);
                rack.setMethod(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                this.lexer.match(10);
                return rack;
            } catch (InvalidArgumentException ex) {
                throw createParseException(ex.getMessage());
            }
        } finally {
            if (debug) {
                dbg_leave("RAckParser.parse");
            }
        }
    }
}
