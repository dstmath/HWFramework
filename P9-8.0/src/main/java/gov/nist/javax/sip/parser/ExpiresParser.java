package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class ExpiresParser extends HeaderParser {
    public ExpiresParser(String text) {
        super(text);
    }

    protected ExpiresParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        Expires expires = new Expires();
        if (debug) {
            dbg_enter("parse");
        }
        try {
            this.lexer.match(TokenTypes.EXPIRES);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            String nextId = this.lexer.getNextId();
            this.lexer.match(10);
            expires.setExpires(Integer.parseInt(nextId));
            if (debug) {
                dbg_leave("parse");
            }
            return expires;
        } catch (NumberFormatException e) {
            throw createParseException("bad integer format");
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }
}
