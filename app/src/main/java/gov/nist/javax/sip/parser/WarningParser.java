package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Warning;
import gov.nist.javax.sip.header.WarningList;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.WarningHeader;

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
        headerName(TokenTypes.WARNING);
        while (this.lexer.lookAhead(0) != '\n') {
            Warning warning = new Warning();
            warning.setHeaderName(WarningHeader.NAME);
            this.lexer.match(TokenTypes.ID);
            try {
                warning.setCode(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                this.lexer.SPorHT();
                this.lexer.match(TokenTypes.ID);
                Token token = this.lexer.getNextToken();
                if (this.lexer.lookAhead(0) == ':') {
                    this.lexer.match(58);
                    this.lexer.match(TokenTypes.ID);
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
                    this.lexer.match(TokenTypes.ID);
                    warning.setCode(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                    this.lexer.SPorHT();
                    this.lexer.match(TokenTypes.ID);
                    Token tok = this.lexer.getNextToken();
                    if (this.lexer.lookAhead(0) == ':') {
                        this.lexer.match(58);
                        this.lexer.match(TokenTypes.ID);
                        warning.setAgent(tok.getTokenValue() + Separators.COLON + this.lexer.getNextToken().getTokenValue());
                    } else {
                        warning.setAgent(tok.getTokenValue());
                    }
                    this.lexer.SPorHT();
                    warning.setText(this.lexer.quotedString());
                    this.lexer.SPorHT();
                    warningList.add((SIPHeader) warning);
                }
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
        if (debug) {
            dbg_leave("WarningParser.parse");
        }
        return warningList;
    }
}
