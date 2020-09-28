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

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        WarningList warningList = new WarningList();
        if (debug) {
            dbg_enter("WarningParser.parse");
        }
        try {
            headerName(TokenTypes.WARNING);
            while (this.lexer.lookAhead(0) != '\n') {
                Warning warning = new Warning();
                warning.setHeaderName("Warning");
                this.lexer.match(4095);
                try {
                    warning.setCode(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    Token token = this.lexer.getNextToken();
                    if (this.lexer.lookAhead(0) == ':') {
                        this.lexer.match(58);
                        this.lexer.match(4095);
                        Token token2 = this.lexer.getNextToken();
                        warning.setAgent(token.getTokenValue() + Separators.COLON + token2.getTokenValue());
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
                        Warning warning2 = new Warning();
                        this.lexer.match(4095);
                        try {
                            warning2.setCode(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                            this.lexer.SPorHT();
                            this.lexer.match(4095);
                            Token tok = this.lexer.getNextToken();
                            if (this.lexer.lookAhead(0) == ':') {
                                this.lexer.match(58);
                                this.lexer.match(4095);
                                Token token22 = this.lexer.getNextToken();
                                warning2.setAgent(tok.getTokenValue() + Separators.COLON + token22.getTokenValue());
                            } else {
                                warning2.setAgent(tok.getTokenValue());
                            }
                            this.lexer.SPorHT();
                            warning2.setText(this.lexer.quotedString());
                            this.lexer.SPorHT();
                            warningList.add((SIPHeader) warning2);
                        } catch (NumberFormatException ex) {
                            throw createParseException(ex.getMessage());
                        } catch (InvalidArgumentException ex2) {
                            throw createParseException(ex2.getMessage());
                        }
                    }
                } catch (NumberFormatException ex3) {
                    throw createParseException(ex3.getMessage());
                } catch (InvalidArgumentException ex4) {
                    throw createParseException(ex4.getMessage());
                }
            }
            return warningList;
        } finally {
            if (debug) {
                dbg_leave("WarningParser.parse");
            }
        }
    }
}
