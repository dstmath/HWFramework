package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ReplyTo;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ReplyToParser extends AddressParametersParser {
    public ReplyToParser(String replyTo) {
        super(replyTo);
    }

    protected ReplyToParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        ReplyTo replyTo = new ReplyTo();
        if (debug) {
            dbg_enter("ReplyTo.parse");
        }
        try {
            headerName(TokenTypes.REPLY_TO);
            replyTo.setHeaderName("Reply-To");
            super.parse(replyTo);
            return replyTo;
        } finally {
            if (debug) {
                dbg_leave("ReplyTo.parse");
            }
        }
    }
}
