package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PCalledPartyID;
import gov.nist.javax.sip.parser.AddressParametersParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PCalledPartyIDParser extends AddressParametersParser {
    public PCalledPartyIDParser(String calledPartyID) {
        super(calledPartyID);
    }

    protected PCalledPartyIDParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PCalledPartyIDParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_CALLED_PARTY_ID);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            PCalledPartyID calledPartyID = new PCalledPartyID();
            super.parse(calledPartyID);
            return calledPartyID;
        } finally {
            if (debug) {
                dbg_leave("PCalledPartyIDParser.parse");
            }
        }
    }
}
