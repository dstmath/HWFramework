package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.TimeStamp;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class TimeStampParser extends HeaderParser {
    public TimeStampParser(String timeStamp) {
        super(timeStamp);
    }

    protected TimeStampParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("TimeStampParser.parse");
        }
        TimeStamp timeStamp = new TimeStamp();
        try {
            headerName(TokenTypes.TIMESTAMP);
            timeStamp.setHeaderName("Timestamp");
            this.lexer.SPorHT();
            String firstNumber = this.lexer.number();
            if (this.lexer.lookAhead(0) == '.') {
                this.lexer.match(46);
                String secondNumber = this.lexer.number();
                timeStamp.setTimeStamp(Float.parseFloat(firstNumber + Separators.DOT + secondNumber));
            } else {
                timeStamp.setTime(Long.parseLong(firstNumber));
            }
            this.lexer.SPorHT();
            if (this.lexer.lookAhead(0) != 10) {
                String firstNumber2 = this.lexer.number();
                if (this.lexer.lookAhead(0) == '.') {
                    this.lexer.match(46);
                    String secondNumber2 = this.lexer.number();
                    timeStamp.setDelay(Float.parseFloat(firstNumber2 + Separators.DOT + secondNumber2));
                } else {
                    timeStamp.setDelay((float) Integer.parseInt(firstNumber2));
                }
            }
            if (debug) {
                dbg_leave("TimeStampParser.parse");
            }
            return timeStamp;
        } catch (NumberFormatException ex) {
            throw createParseException(ex.getMessage());
        } catch (InvalidArgumentException ex2) {
            throw createParseException(ex2.getMessage());
        } catch (NumberFormatException ex3) {
            throw createParseException(ex3.getMessage());
        } catch (InvalidArgumentException ex4) {
            throw createParseException(ex4.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("TimeStampParser.parse");
            }
            throw th;
        }
    }
}
