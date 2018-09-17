package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ContentDisposition;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.header.ContentDispositionHeader;

public class ContentDispositionParser extends ParametersParser {
    public ContentDispositionParser(String contentDisposition) {
        super(contentDisposition);
    }

    protected ContentDispositionParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("ContentDispositionParser.parse");
        }
        try {
            headerName(TokenTypes.CONTENT_DISPOSITION);
            ContentDisposition cd = new ContentDisposition();
            cd.setHeaderName(ContentDispositionHeader.NAME);
            this.lexer.SPorHT();
            this.lexer.match(TokenTypes.ID);
            cd.setDispositionType(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            super.parse(cd);
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("ContentDispositionParser.parse");
            }
            return cd;
        } catch (ParseException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("ContentDispositionParser.parse");
            }
        }
    }
}
