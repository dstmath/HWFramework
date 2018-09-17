package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PAccessNetworkInfo;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PAccessNetworkInfoParser extends HeaderParser implements TokenTypes {
    public PAccessNetworkInfoParser(String accessNetwork) {
        super(accessNetwork);
    }

    protected PAccessNetworkInfoParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("AccessNetworkInfoParser.parse");
        }
        try {
            headerName(TokenTypes.P_ACCESS_NETWORK_INFO);
            PAccessNetworkInfo accessNetworkInfo = new PAccessNetworkInfo();
            accessNetworkInfo.setHeaderName("P-Access-Network-Info");
            this.lexer.SPorHT();
            this.lexer.match(4095);
            accessNetworkInfo.setAccessType(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            while (this.lexer.lookAhead(0) == ';') {
                this.lexer.match(59);
                this.lexer.SPorHT();
                accessNetworkInfo.setParameter(super.nameValue('='));
                this.lexer.SPorHT();
            }
            this.lexer.SPorHT();
            this.lexer.match(10);
            return accessNetworkInfo;
        } finally {
            if (debug) {
                dbg_leave("AccessNetworkInfoParser.parse");
            }
        }
    }
}
