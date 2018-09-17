package gov.nist.javax.sip.parser;

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

    public SIPHeader parse() throws ParseException {
        RouteList routeList = new RouteList();
        if (debug) {
            dbg_enter("parse");
        }
        try {
            char la;
            this.lexer.match(TokenTypes.ROUTE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                Route route = new Route();
                super.parse(route);
                routeList.add((SIPHeader) route);
                this.lexer.SPorHT();
                la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (la == 10) {
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
