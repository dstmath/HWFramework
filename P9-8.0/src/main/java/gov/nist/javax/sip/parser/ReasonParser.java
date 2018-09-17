package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Reason;
import gov.nist.javax.sip.header.ReasonList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ReasonParser extends ParametersParser {
    public ReasonParser(String reason) {
        super(reason);
    }

    protected ReasonParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        ReasonList reasonList = new ReasonList();
        if (debug) {
            dbg_enter("ReasonParser.parse");
        }
        try {
            headerName(TokenTypes.REASON);
            this.lexer.SPorHT();
            while (this.lexer.lookAhead(0) != 10) {
                Reason reason = new Reason();
                this.lexer.match(4095);
                reason.setProtocol(this.lexer.getNextToken().getTokenValue());
                super.parse(reason);
                reasonList.add((SIPHeader) reason);
                if (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                } else {
                    this.lexer.SPorHT();
                }
            }
            return reasonList;
        } finally {
            if (debug) {
                dbg_leave("ReasonParser.parse");
            }
        }
    }
}
