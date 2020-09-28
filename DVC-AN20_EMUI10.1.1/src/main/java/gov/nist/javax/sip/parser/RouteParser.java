package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class RouteParser extends AddressParametersParser {
    public RouteParser(String route) {
        super(route);
    }

    protected RouteParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        char la;
        RouteList routeList = new RouteList();
        if (debug) {
            dbg_enter("parse");
        }
        try {
            this.lexer.match(TokenTypes.ROUTE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                Route route = new Route();
                super.parse((AddressParametersHeader) route);
                routeList.add((SIPHeader) route);
                this.lexer.SPorHT();
                la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (la == '\n') {
                return routeList;
            }
            throw createParseException("unexpected char");
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }
}
