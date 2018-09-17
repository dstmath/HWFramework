package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ContentLength;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class ContentLengthParser extends HeaderParser {
    public ContentLengthParser(String contentLength) {
        super(contentLength);
    }

    protected ContentLengthParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("ContentLengthParser.enter");
        }
        try {
            ContentLength contentLength = new ContentLength();
            headerName(TokenTypes.CONTENT_LENGTH);
            contentLength.setContentLength(Integer.parseInt(this.lexer.number()));
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("ContentLengthParser.leave");
            }
            return contentLength;
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (NumberFormatException ex2) {
            throw createParseException(ex2.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("ContentLengthParser.leave");
            }
        }
    }
}
