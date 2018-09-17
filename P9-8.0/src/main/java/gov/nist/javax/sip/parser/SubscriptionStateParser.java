package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SubscriptionState;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class SubscriptionStateParser extends HeaderParser {
    public SubscriptionStateParser(String subscriptionState) {
        super(subscriptionState);
    }

    protected SubscriptionStateParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("SubscriptionStateParser.parse");
        }
        SubscriptionState subscriptionState = new SubscriptionState();
        try {
            headerName(TokenTypes.SUBSCRIPTION_STATE);
            subscriptionState.setHeaderName("Subscription-State");
            this.lexer.match(4095);
            subscriptionState.setState(this.lexer.getNextToken().getTokenValue());
            while (this.lexer.lookAhead(0) == ';') {
                this.lexer.match(59);
                this.lexer.SPorHT();
                this.lexer.match(4095);
                String value = this.lexer.getNextToken().getTokenValue();
                if (value.equalsIgnoreCase("reason")) {
                    this.lexer.match(61);
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    subscriptionState.setReasonCode(this.lexer.getNextToken().getTokenValue());
                } else if (value.equalsIgnoreCase("expires")) {
                    this.lexer.match(61);
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    subscriptionState.setExpires(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                } else if (value.equalsIgnoreCase("retry-after")) {
                    this.lexer.match(61);
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    subscriptionState.setRetryAfter(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                } else {
                    this.lexer.match(61);
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    subscriptionState.setParameter(value, this.lexer.getNextToken().getTokenValue());
                }
                this.lexer.SPorHT();
            }
            if (debug) {
                dbg_leave("SubscriptionStateParser.parse");
            }
            return subscriptionState;
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
                dbg_leave("SubscriptionStateParser.parse");
            }
        }
    }
}
