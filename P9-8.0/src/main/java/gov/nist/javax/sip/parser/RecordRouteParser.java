package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.RecordRouteList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class RecordRouteParser extends AddressParametersParser {
    public RecordRouteParser(String recordRoute) {
        super(recordRoute);
    }

    protected RecordRouteParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        RecordRouteList recordRouteList = new RecordRouteList();
        if (debug) {
            dbg_enter("RecordRouteParser.parse");
        }
        try {
            char la;
            this.lexer.match(TokenTypes.RECORD_ROUTE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                RecordRoute recordRoute = new RecordRoute();
                super.parse(recordRoute);
                recordRouteList.add((SIPHeader) recordRoute);
                this.lexer.SPorHT();
                la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (la == 10) {
                return recordRouteList;
            }
            throw createParseException("unexpected char");
        } finally {
            if (debug) {
                dbg_leave("RecordRouteParser.parse");
            }
        }
    }
}
