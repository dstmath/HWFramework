package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Warning;
import gov.nist.javax.sip.header.WarningList;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class WarningParser extends HeaderParser {
    public WarningParser(String warning) {
        super(warning);
    }

    protected WarningParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        WarningList warningList = new WarningList();
        if (debug) {
            dbg_enter("WarningParser.parse");
        }
        try {
            headerName(TokenTypes.WARNING);
            while (this.lexer.lookAhead(0) != 10) {
                Warning warning = new Warning();
                warning.setHeaderName("Warning");
                this.lexer.match(4095);
                warning.setCode(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                this.lexer.SPorHT();
                this.lexer.match(4095);
                Token token = this.lexer.getNextToken();
                if (this.lexer.lookAhead(0) == ':') {
                    this.lexer.match(58);
                    this.lexer.match(4095);
                    warning.setAgent(token.getTokenValue() + Separators.COLON + this.lexer.getNextToken().getTokenValue());
                } else {
                    warning.setAgent(token.getTokenValue());
                }
                this.lexer.SPorHT();
                warning.setText(this.lexer.quotedString());
                this.lexer.SPorHT();
                warningList.add((SIPHeader) warning);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    warning = new Warning();
                    this.lexer.match(4095);
                    warning.setCode(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    Token tok = this.lexer.getNextToken();
                    if (this.lexer.lookAhead(0) == ':') {
                        this.lexer.match(58);
                        this.lexer.match(4095);
                        warning.setAgent(tok.getTokenValue() + Separators.COLON + this.lexer.getNextToken().getTokenValue());
                    } else {
                        warning.setAgent(tok.getTokenValue());
                    }
                    this.lexer.SPorHT();
                    warning.setText(this.lexer.quotedString());
                    this.lexer.SPorHT();
                    warningList.add((SIPHeader) warning);
                }
            }
            if (debug) {
                dbg_leave("WarningParser.parse");
            }
            return warningList;
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
                dbg_leave("WarningParser.parse");
            }
        }
    }
}
