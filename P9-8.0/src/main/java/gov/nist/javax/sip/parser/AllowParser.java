package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Allow;
import gov.nist.javax.sip.header.AllowList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class AllowParser extends HeaderParser {
    public AllowParser(String allow) {
        super(allow);
    }

    protected AllowParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("AllowParser.parse");
        }
        AllowList list = new AllowList();
        try {
            headerName(TokenTypes.ALLOW);
            Allow allow = new Allow();
            allow.setHeaderName("Allow");
            this.lexer.SPorHT();
            this.lexer.match(4095);
            allow.setMethod(this.lexer.getNextToken().getTokenValue());
            list.add((SIPHeader) allow);
            this.lexer.SPorHT();
            while (this.lexer.lookAhead(0) == ',') {
                this.lexer.match(44);
                this.lexer.SPorHT();
                allow = new Allow();
                this.lexer.match(4095);
                allow.setMethod(this.lexer.getNextToken().getTokenValue());
                list.add((SIPHeader) allow);
                this.lexer.SPorHT();
            }
            this.lexer.SPorHT();
            this.lexer.match(10);
            return list;
        } finally {
            if (debug) {
                dbg_leave("AllowParser.parse");
            }
        }
    }
}
