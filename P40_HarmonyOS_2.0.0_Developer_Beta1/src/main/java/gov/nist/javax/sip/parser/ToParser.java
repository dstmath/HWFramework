package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.To;
import java.text.ParseException;

public class ToParser extends AddressParametersParser {
    public ToParser(String to) {
        super(to);
    }

    protected ToParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        headerName(TokenTypes.TO);
        To to = new To();
        super.parse((AddressParametersHeader) to);
        this.lexer.match(10);
        return to;
    }
}
