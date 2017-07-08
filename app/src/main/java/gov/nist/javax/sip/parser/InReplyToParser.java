package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.InReplyTo;
import gov.nist.javax.sip.header.InReplyToList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.header.InReplyToHeader;

public class InReplyToParser extends HeaderParser {
    public InReplyToParser(String inReplyTo) {
        super(inReplyTo);
    }

    protected InReplyToParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("InReplyToParser.parse");
        }
        InReplyToList list = new InReplyToList();
        try {
            headerName(TokenTypes.IN_REPLY_TO);
            while (this.lexer.lookAhead(0) != '\n') {
                InReplyTo inReplyTo = new InReplyTo();
                inReplyTo.setHeaderName(InReplyToHeader.NAME);
                this.lexer.match(TokenTypes.ID);
                Token token = this.lexer.getNextToken();
                if (this.lexer.lookAhead(0) == '@') {
                    this.lexer.match(64);
                    this.lexer.match(TokenTypes.ID);
                    inReplyTo.setCallId(token.getTokenValue() + Separators.AT + this.lexer.getNextToken().getTokenValue());
                } else {
                    inReplyTo.setCallId(token.getTokenValue());
                }
                this.lexer.SPorHT();
                list.add((SIPHeader) inReplyTo);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    inReplyTo = new InReplyTo();
                    this.lexer.match(TokenTypes.ID);
                    token = this.lexer.getNextToken();
                    if (this.lexer.lookAhead(0) == '@') {
                        this.lexer.match(64);
                        this.lexer.match(TokenTypes.ID);
                        inReplyTo.setCallId(token.getTokenValue() + Separators.AT + this.lexer.getNextToken().getTokenValue());
                    } else {
                        inReplyTo.setCallId(token.getTokenValue());
                    }
                    list.add((SIPHeader) inReplyTo);
                }
            }
            return list;
        } finally {
            if (debug) {
                dbg_leave("InReplyToParser.parse");
            }
        }
    }
}
