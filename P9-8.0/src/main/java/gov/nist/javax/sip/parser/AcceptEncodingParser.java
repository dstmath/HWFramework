package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.AcceptEncoding;
import gov.nist.javax.sip.header.AcceptEncodingList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class AcceptEncodingParser extends HeaderParser {
    public AcceptEncodingParser(String acceptEncoding) {
        super(acceptEncoding);
    }

    protected AcceptEncodingParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        AcceptEncodingList acceptEncodingList = new AcceptEncodingList();
        if (debug) {
            dbg_enter("AcceptEncodingParser.parse");
        }
        try {
            headerName(TokenTypes.ACCEPT_ENCODING);
            if (this.lexer.lookAhead(0) == 10) {
                acceptEncodingList.add((SIPHeader) new AcceptEncoding());
            } else {
                while (this.lexer.lookAhead(0) != 10) {
                    AcceptEncoding acceptEncoding = new AcceptEncoding();
                    if (this.lexer.lookAhead(0) != ';') {
                        this.lexer.match(4095);
                        acceptEncoding.setEncoding(this.lexer.getNextToken().getTokenValue());
                    }
                    while (this.lexer.lookAhead(0) == ';') {
                        this.lexer.match(59);
                        this.lexer.SPorHT();
                        this.lexer.match(113);
                        this.lexer.SPorHT();
                        this.lexer.match(61);
                        this.lexer.SPorHT();
                        this.lexer.match(4095);
                        acceptEncoding.setQValue(Float.parseFloat(this.lexer.getNextToken().getTokenValue()));
                        this.lexer.SPorHT();
                    }
                    acceptEncodingList.add((SIPHeader) acceptEncoding);
                    if (this.lexer.lookAhead(0) == ',') {
                        this.lexer.match(44);
                        this.lexer.SPorHT();
                    }
                }
            }
            if (debug) {
                dbg_leave("AcceptEncodingParser.parse");
            }
            return acceptEncodingList;
        } catch (NumberFormatException ex) {
            throw createParseException(ex.getMessage());
        } catch (InvalidArgumentException ex2) {
            throw createParseException(ex2.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("AcceptEncodingParser.parse");
            }
        }
    }
}
