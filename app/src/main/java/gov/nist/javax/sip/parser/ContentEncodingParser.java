package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ContentEncoding;
import gov.nist.javax.sip.header.ContentEncodingList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.header.ContentEncodingHeader;

public class ContentEncodingParser extends HeaderParser {
    public ContentEncodingParser(String contentEncoding) {
        super(contentEncoding);
    }

    protected ContentEncodingParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("ContentEncodingParser.parse");
        }
        ContentEncodingList list = new ContentEncodingList();
        try {
            headerName(TokenTypes.CONTENT_ENCODING);
            while (this.lexer.lookAhead(0) != '\n') {
                ContentEncoding cl = new ContentEncoding();
                cl.setHeaderName(ContentEncodingHeader.NAME);
                this.lexer.SPorHT();
                this.lexer.match(TokenTypes.ID);
                cl.setEncoding(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                list.add((SIPHeader) cl);
                while (this.lexer.lookAhead(0) == ',') {
                    cl = new ContentEncoding();
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    this.lexer.match(TokenTypes.ID);
                    this.lexer.SPorHT();
                    cl.setEncoding(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    list.add((SIPHeader) cl);
                }
            }
            if (debug) {
                dbg_leave("ContentEncodingParser.parse");
            }
            return list;
        } catch (ParseException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("ContentEncodingParser.parse");
            }
        }
    }
}
