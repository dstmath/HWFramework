package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Accept;
import gov.nist.javax.sip.header.AcceptList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class AcceptParser extends ParametersParser {
    public AcceptParser(String accept) {
        super(accept);
    }

    protected AcceptParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("AcceptParser.parse");
        }
        AcceptList list = new AcceptList();
        try {
            headerName(2068);
            Accept accept = new Accept();
            accept.setHeaderName("Accept");
            this.lexer.SPorHT();
            this.lexer.match(4095);
            accept.setContentType(this.lexer.getNextToken().getTokenValue());
            this.lexer.match(47);
            this.lexer.match(4095);
            accept.setContentSubType(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            super.parse(accept);
            list.add((SIPHeader) accept);
            while (this.lexer.lookAhead(0) == ',') {
                this.lexer.match(44);
                this.lexer.SPorHT();
                accept = new Accept();
                this.lexer.match(4095);
                accept.setContentType(this.lexer.getNextToken().getTokenValue());
                this.lexer.match(47);
                this.lexer.match(4095);
                accept.setContentSubType(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                super.parse(accept);
                list.add((SIPHeader) accept);
            }
            return list;
        } finally {
            if (debug) {
                dbg_leave("AcceptParser.parse");
            }
        }
    }
}
