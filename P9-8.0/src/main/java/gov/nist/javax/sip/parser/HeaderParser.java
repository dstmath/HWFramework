package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ExtensionHeaderImpl;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

public class HeaderParser extends Parser {
    protected int wkday() throws ParseException {
        dbg_enter("wkday");
        try {
            String id = this.lexer.ttoken().toLowerCase();
            if ("Mon".equalsIgnoreCase(id)) {
                return 2;
            }
            if ("Tue".equalsIgnoreCase(id)) {
                dbg_leave("wkday");
                return 3;
            } else if ("Wed".equalsIgnoreCase(id)) {
                dbg_leave("wkday");
                return 4;
            } else if ("Thu".equalsIgnoreCase(id)) {
                dbg_leave("wkday");
                return 5;
            } else if ("Fri".equalsIgnoreCase(id)) {
                dbg_leave("wkday");
                return 6;
            } else if ("Sat".equalsIgnoreCase(id)) {
                dbg_leave("wkday");
                return 7;
            } else if ("Sun".equalsIgnoreCase(id)) {
                dbg_leave("wkday");
                return 1;
            } else {
                throw createParseException("bad wkday");
            }
        } finally {
            dbg_leave("wkday");
        }
    }

    protected Calendar date() throws ParseException {
        try {
            Calendar retval = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            int day = Integer.parseInt(this.lexer.number());
            if (day <= 0 || day > 31) {
                throw createParseException("Bad day ");
            }
            retval.set(5, day);
            this.lexer.match(32);
            String month = this.lexer.ttoken().toLowerCase();
            if (month.equals("jan")) {
                retval.set(2, 0);
            } else if (month.equals("feb")) {
                retval.set(2, 1);
            } else if (month.equals("mar")) {
                retval.set(2, 2);
            } else if (month.equals("apr")) {
                retval.set(2, 3);
            } else if (month.equals("may")) {
                retval.set(2, 4);
            } else if (month.equals("jun")) {
                retval.set(2, 5);
            } else if (month.equals("jul")) {
                retval.set(2, 6);
            } else if (month.equals("aug")) {
                retval.set(2, 7);
            } else if (month.equals("sep")) {
                retval.set(2, 8);
            } else if (month.equals("oct")) {
                retval.set(2, 9);
            } else if (month.equals("nov")) {
                retval.set(2, 10);
            } else if (month.equals("dec")) {
                retval.set(2, 11);
            }
            this.lexer.match(32);
            retval.set(1, Integer.parseInt(this.lexer.number()));
            return retval;
        } catch (Exception e) {
            throw createParseException("bad date field");
        }
    }

    protected void time(Calendar calendar) throws ParseException {
        try {
            calendar.set(11, Integer.parseInt(this.lexer.number()));
            this.lexer.match(58);
            calendar.set(12, Integer.parseInt(this.lexer.number()));
            this.lexer.match(58);
            calendar.set(13, Integer.parseInt(this.lexer.number()));
        } catch (Exception e) {
            throw createParseException("error processing time ");
        }
    }

    protected HeaderParser(String header) {
        this.lexer = new Lexer("command_keywordLexer", header);
    }

    protected HeaderParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }

    public SIPHeader parse() throws ParseException {
        String name = this.lexer.getNextToken(':');
        this.lexer.consume(1);
        String body = this.lexer.getLine().trim();
        ExtensionHeaderImpl retval = new ExtensionHeaderImpl(name);
        retval.setValue(body);
        return retval;
    }

    protected void headerName(int tok) throws ParseException {
        this.lexer.match(tok);
        this.lexer.SPorHT();
        this.lexer.match(58);
        this.lexer.SPorHT();
    }
}
