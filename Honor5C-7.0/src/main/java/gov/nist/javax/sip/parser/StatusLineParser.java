package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.StatusLine;
import java.text.ParseException;

public class StatusLineParser extends Parser {
    public StatusLineParser(String statusLine) {
        this.lexer = new Lexer("status_lineLexer", statusLine);
    }

    public StatusLineParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("status_lineLexer");
    }

    protected int statusCode() throws ParseException {
        String scode = this.lexer.number();
        if (debug) {
            dbg_enter("statusCode");
        }
        try {
            int retval = Integer.parseInt(scode);
            if (debug) {
                dbg_leave("statusCode");
            }
            return retval;
        } catch (NumberFormatException ex) {
            throw new ParseException(this.lexer.getBuffer() + Separators.COLON + ex.getMessage(), this.lexer.getPtr());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("statusCode");
            }
        }
    }

    protected String reasonPhrase() throws ParseException {
        return this.lexer.getRest().trim();
    }

    public StatusLine parse() throws ParseException {
        try {
            if (debug) {
                dbg_enter("parse");
            }
            StatusLine retval = new StatusLine();
            retval.setSipVersion(sipVersion());
            this.lexer.SPorHT();
            retval.setStatusCode(statusCode());
            this.lexer.SPorHT();
            retval.setReasonPhrase(reasonPhrase());
            this.lexer.SPorHT();
            return retval;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }
}
