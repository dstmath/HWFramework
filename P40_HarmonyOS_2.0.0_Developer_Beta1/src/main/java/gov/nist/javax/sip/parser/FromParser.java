package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.AddressParametersHeader;
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

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        From from = new From();
        this.lexer.match(TokenTypes.FROM);
        this.lexer.SPorHT();
        this.lexer.match(58);
        this.lexer.SPorHT();
        super.parse((AddressParametersHeader) from);
        this.lexer.match(10);
        return from;
    }
}
