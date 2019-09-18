package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.InReplyTo;
import gov.nist.javax.sip.header.InReplyToList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class InReplyToParser extends HeaderParser {
    public InReplyToParser(String inReplyTo) {
        super(inReplyTo);
    }

    protected InReplyToParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        String str;
        if (debug) {
            dbg_enter("InReplyToParser.parse");
        }
        InReplyToList list = new InReplyToList();
        try {
            headerName(TokenTypes.IN_REPLY_TO);
            while (this.lexer.lookAhead(0) != 10) {
                InReplyTo inReplyTo = new InReplyTo();
                inReplyTo.setHeaderName("In-Reply-To");
                this.lexer.match(4095);
                Token token = this.lexer.getNextToken();
                if (this.lexer.lookAhead(0) == '@') {
                    this.lexer.match(64);
                    this.lexer.match(4095);
                    Token secToken = this.lexer.getNextToken();
                    inReplyTo.setCallId(token.getTokenValue() + Separators.AT + secToken.getTokenValue());
                } else {
                    inReplyTo.setCallId(token.getTokenValue());
                }
                this.lexer.SPorHT();
                list.add(inReplyTo);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    InReplyTo inReplyTo2 = new InReplyTo();
                    this.lexer.match(4095);
                    Token token2 = this.lexer.getNextToken();
                    if (this.lexer.lookAhead(0) == '@') {
                        this.lexer.match(64);
                        this.lexer.match(4095);
                        Token secToken2 = this.lexer.getNextToken();
                        inReplyTo2.setCallId(token2.getTokenValue() + Separators.AT + secToken2.getTokenValue());
                    } else {
                        inReplyTo2.setCallId(token2.getTokenValue());
                    }
                    list.add(inReplyTo2);
                }
            }
            return list;
        } finally {
            if (debug) {
                str = "InReplyToParser.parse";
                dbg_leave(str);
            }
        }
    }
}
