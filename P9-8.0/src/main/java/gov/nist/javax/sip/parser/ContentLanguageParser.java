package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ContentLanguage;
import gov.nist.javax.sip.header.ContentLanguageList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ContentLanguageParser extends HeaderParser {
    public ContentLanguageParser(String contentLanguage) {
        super(contentLanguage);
    }

    protected ContentLanguageParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("ContentLanguageParser.parse");
        }
        ContentLanguageList list = new ContentLanguageList();
        try {
            headerName(TokenTypes.CONTENT_LANGUAGE);
            while (this.lexer.lookAhead(0) != 10) {
                this.lexer.SPorHT();
                this.lexer.match(4095);
                ContentLanguage cl = new ContentLanguage(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                list.add((SIPHeader) cl);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    this.lexer.SPorHT();
                    cl = new ContentLanguage(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    list.add((SIPHeader) cl);
                }
            }
            if (debug) {
                dbg_leave("ContentLanguageParser.parse");
            }
            return list;
        } catch (ParseException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("ContentLanguageParser.parse");
            }
        }
    }
}
