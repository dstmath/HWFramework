package gov.nist.javax.sip.parser;

import gov.nist.core.Token;
import gov.nist.javax.sip.header.ContentType;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ContentTypeParser extends ParametersParser {
    public ContentTypeParser(String contentType) {
        super(contentType);
    }

    protected ContentTypeParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        ContentType contentType = new ContentType();
        if (debug) {
            dbg_enter("ContentTypeParser.parse");
        }
        try {
            headerName(TokenTypes.CONTENT_TYPE);
            this.lexer.match(4095);
            Token type = this.lexer.getNextToken();
            this.lexer.SPorHT();
            contentType.setContentType(type.getTokenValue());
            this.lexer.match(47);
            this.lexer.match(4095);
            Token subType = this.lexer.getNextToken();
            this.lexer.SPorHT();
            contentType.setContentSubType(subType.getTokenValue());
            super.parse(contentType);
            this.lexer.match(10);
            return contentType;
        } finally {
            if (debug) {
                dbg_leave("ContentTypeParser.parse");
            }
        }
    }
}
