package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.header.RequestLine;
import java.text.ParseException;

public class RequestLineParser extends Parser {
    public RequestLineParser(String requestLine) {
        this.lexer = new Lexer("method_keywordLexer", requestLine);
    }

    public RequestLineParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("method_keywordLexer");
    }

    public RequestLine parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            RequestLine retval = new RequestLine();
            String m = method();
            this.lexer.SPorHT();
            retval.setMethod(m);
            this.lexer.selectLexer("sip_urlLexer");
            GenericURI url = new URLParser(getLexer()).uriReference(true);
            this.lexer.SPorHT();
            retval.setUri(url);
            this.lexer.selectLexer("request_lineLexer");
            retval.setSipVersion(sipVersion());
            this.lexer.SPorHT();
            this.lexer.match(10);
            return retval;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] requestLines = new String[]{"REGISTER sip:192.168.0.68 SIP/2.0\n", "REGISTER sip:company.com SIP/2.0\n", "INVITE sip:3660@166.35.231.140 SIP/2.0\n", "INVITE sip:user@company.com SIP/2.0\n", "REGISTER sip:[2001::1]:5060;transport=tcp SIP/2.0\n", "REGISTER sip:[2002:800:700:600:30:4:6:1]:5060;transport=udp SIP/2.0\n", "REGISTER sip:[3ffe:800:700::30:4:6:1]:5060;transport=tls SIP/2.0\n", "REGISTER sip:[2001:720:1710:0:201:29ff:fe21:f403]:5060;transport=udp SIP/2.0\n", "OPTIONS sip:135.180.130.133 SIP/2.0\n"};
        for (String requestLineParser : requestLines) {
            System.out.println("encoded = " + new RequestLineParser(requestLineParser).parse().encode());
        }
    }
}
