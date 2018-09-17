package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.MinExpires;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class MinExpiresParser extends HeaderParser {
    public MinExpiresParser(String minExpires) {
        super(minExpires);
    }

    protected MinExpiresParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("MinExpiresParser.parse");
        }
        MinExpires minExpires = new MinExpires();
        try {
            headerName(TokenTypes.MIN_EXPIRES);
            minExpires.setHeaderName("Min-Expires");
            minExpires.setExpires(Integer.parseInt(this.lexer.number()));
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("MinExpiresParser.parse");
            }
            return minExpires;
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("MinExpiresParser.parse");
            }
        }
    }
}
