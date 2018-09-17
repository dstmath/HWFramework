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

    public SIPHeader parse() throws ParseException {
        PMediaAuthorizationList mediaAuthorizationList = new PMediaAuthorizationList();
        if (debug) {
            dbg_enter("MediaAuthorizationParser.parse");
        }
        try {
            headerName(TokenTypes.P_MEDIA_AUTHORIZATION);
            SIPHeader mediaAuthorization = new PMediaAuthorization();
            mediaAuthorization.setHeaderName("P-Media-Authorization");
            while (this.lexer.lookAhead(0) != 10) {
                this.lexer.match(4095);
                mediaAuthorization.setMediaAuthorizationToken(this.lexer.getNextToken().getTokenValue());
                mediaAuthorizationList.add(mediaAuthorization);
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    mediaAuthorization = new PMediaAuthorization();
                }
                this.lexer.SPorHT();
            }
            if (debug) {
                dbg_leave("MediaAuthorizationParser.parse");
            }
            return mediaAuthorizationList;
        } catch (InvalidArgumentException e) {
            throw createParseException(e.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("MediaAuthorizationParser.parse");
            }
        }
    }
}
