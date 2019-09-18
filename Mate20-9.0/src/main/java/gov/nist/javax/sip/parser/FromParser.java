package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class FromParser extends AddressParametersParser {
    public FromParser(String from) {
        super(from);
    }

    protected FromParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        From from = new From();
        this.lexer.match(TokenTypes.FROM);
        this.lexer.SPorHT();
        this.lexer.match(58);
        this.lexer.SPorHT();
        super.parse(from);
        this.lexer.match(10);
        return from;
    }
}
