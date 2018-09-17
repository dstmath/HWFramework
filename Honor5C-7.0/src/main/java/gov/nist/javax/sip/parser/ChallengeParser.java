package gov.nist.javax.sip.parser;

import gov.nist.core.Token;
import gov.nist.javax.sip.header.AuthenticationHeader;
import java.text.ParseException;

public abstract class ChallengeParser extends HeaderParser {
    protected ChallengeParser(String challenge) {
        super(challenge);
    }

    protected ChallengeParser(Lexer lexer) {
        super(lexer);
    }

    protected void parseParameter(AuthenticationHeader header) throws ParseException {
        if (debug) {
            dbg_enter("parseParameter");
        }
        try {
            header.setParameter(nameValue('='));
            if (debug) {
                dbg_leave("parseParameter");
            }
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parseParameter");
            }
        }
    }

    public void parse(AuthenticationHeader header) throws ParseException {
        this.lexer.SPorHT();
        this.lexer.match(TokenTypes.ID);
        Token type = this.lexer.getNextToken();
        this.lexer.SPorHT();
        header.setScheme(type.getTokenValue());
        while (this.lexer.lookAhead(0) != '\n') {
            try {
                parseParameter(header);
                this.lexer.SPorHT();
                char la = this.lexer.lookAhead(0);
                if (la != '\n' && la != '\u0000') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                } else {
                    return;
                }
            } catch (ParseException ex) {
                throw ex;
            }
        }
    }
}
