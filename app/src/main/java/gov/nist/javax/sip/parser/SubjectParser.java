package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Subject;
import java.text.ParseException;

public class SubjectParser extends HeaderParser {
    public SubjectParser(String subject) {
        super(subject);
    }

    protected SubjectParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        Subject subject = new Subject();
        if (debug) {
            dbg_enter("SubjectParser.parse");
        }
        try {
            headerName(TokenTypes.SUBJECT);
            this.lexer.SPorHT();
            subject.setSubject(this.lexer.getRest().trim());
            return subject;
        } finally {
            if (debug) {
                dbg_leave("SubjectParser.parse");
            }
        }
    }
}
