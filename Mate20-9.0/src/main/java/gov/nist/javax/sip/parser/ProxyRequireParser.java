package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ProxyRequire;
import gov.nist.javax.sip.header.ProxyRequireList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ProxyRequireParser extends HeaderParser {
    public ProxyRequireParser(String require) {
        super(require);
    }

    protected ProxyRequireParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        String str;
        ProxyRequireList list = new ProxyRequireList();
        if (debug) {
            dbg_enter("ProxyRequireParser.parse");
        }
        try {
            headerName(TokenTypes.PROXY_REQUIRE);
            while (this.lexer.lookAhead(0) != 10) {
                ProxyRequire r = new ProxyRequire();
                r.setHeaderName("Proxy-Require");
                this.lexer.match(4095);
                r.setOptionTag(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                list.add(r);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    ProxyRequire r2 = new ProxyRequire();
                    this.lexer.match(4095);
                    r2.setOptionTag(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    list.add(r2);
                }
            }
            return list;
        } finally {
            if (debug) {
                str = "ProxyRequireParser.parse";
                dbg_leave(str);
            }
        }
    }
}
