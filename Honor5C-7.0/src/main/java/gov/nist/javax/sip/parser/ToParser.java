package gov.nist.javax.sip.parser;

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

    public SIPHeader parse() throws ParseException {
        headerName(TokenTypes.TO);
        To to = new To();
        super.parse(to);
        this.lexer.match(10);
        return to;
    }
}
