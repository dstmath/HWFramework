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
        } finally {
            if (debug) {
                dbg_leave("parseParameter");
            }
        }
    }

    public void parse(AuthenticationHeader header) throws ParseException {
        this.lexer.SPorHT();
        this.lexer.match(4095);
        Token type = this.lexer.getNextToken();
        this.lexer.SPorHT();
        header.setScheme(type.getTokenValue());
        while (this.lexer.lookAhead(0) != 10) {
            try {
                parseParameter(header);
                this.lexer.SPorHT();
                char la = this.lexer.lookAhead(0);
                if (la != 10 && la != 0) {
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
