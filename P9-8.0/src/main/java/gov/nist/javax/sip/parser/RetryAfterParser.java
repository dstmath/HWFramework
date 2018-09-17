package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.RetryAfter;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class RetryAfterParser extends HeaderParser {
    public RetryAfterParser(String retryAfter) {
        super(retryAfter);
    }

    protected RetryAfterParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("RetryAfterParser.parse");
        }
        RetryAfter retryAfter = new RetryAfter();
        try {
            headerName(TokenTypes.RETRY_AFTER);
            retryAfter.setRetryAfter(Integer.parseInt(this.lexer.number()));
            this.lexer.SPorHT();
            if (this.lexer.lookAhead(0) == '(') {
                retryAfter.setComment(this.lexer.comment());
            }
            this.lexer.SPorHT();
            while (this.lexer.lookAhead(0) == ';') {
                this.lexer.match(59);
                this.lexer.SPorHT();
                this.lexer.match(4095);
                String value = this.lexer.getNextToken().getTokenValue();
                if (value.equalsIgnoreCase("duration")) {
                    this.lexer.match(61);
                    this.lexer.SPorHT();
                    retryAfter.setDuration(Integer.parseInt(this.lexer.number()));
                } else {
                    this.lexer.SPorHT();
                    this.lexer.match(61);
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    retryAfter.setParameter(value, this.lexer.getNextToken().getTokenValue());
                }
                this.lexer.SPorHT();
            }
            if (debug) {
                dbg_leave("RetryAfterParser.parse");
            }
            return retryAfter;
        } catch (NumberFormatException ex) {
            throw createParseException(ex.getMessage());
        } catch (InvalidArgumentException ex2) {
            throw createParseException(ex2.getMessage());
        } catch (NumberFormatException ex3) {
            throw createParseException(ex3.getMessage());
        } catch (InvalidArgumentException ex22) {
            throw createParseException(ex22.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("RetryAfterParser.parse");
            }
        }
    }
}
