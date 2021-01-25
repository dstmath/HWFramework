package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PMediaAuthorization;
import gov.nist.javax.sip.header.ims.PMediaAuthorizationList;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class PMediaAuthorizationParser extends HeaderParser implements TokenTypes {
    public PMediaAuthorizationParser(String mediaAuthorization) {
        super(mediaAuthorization);
    }

    public PMediaAuthorizationParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        PMediaAuthorizationList mediaAuthorizationList = new PMediaAuthorizationList();
        if (debug) {
            dbg_enter("MediaAuthorizationParser.parse");
        }
        try {
            headerName(TokenTypes.P_MEDIA_AUTHORIZATION);
            PMediaAuthorization mediaAuthorization = new PMediaAuthorization();
            mediaAuthorization.setHeaderName("P-Media-Authorization");
            while (this.lexer.lookAhead(0) != '\n') {
                this.lexer.match(4095);
                try {
                    mediaAuthorization.setMediaAuthorizationToken(this.lexer.getNextToken().getTokenValue());
                    mediaAuthorizationList.add((PMediaAuthorizationList) mediaAuthorization);
                    this.lexer.SPorHT();
                    if (this.lexer.lookAhead(0) == ',') {
                        this.lexer.match(44);
                        mediaAuthorization = new PMediaAuthorization();
                    }
                    this.lexer.SPorHT();
                } catch (InvalidArgumentException e) {
                    throw createParseException(e.getMessage());
                }
            }
            return mediaAuthorizationList;
        } finally {
            if (debug) {
                dbg_leave("MediaAuthorizationParser.parse");
            }
        }
    }
}
