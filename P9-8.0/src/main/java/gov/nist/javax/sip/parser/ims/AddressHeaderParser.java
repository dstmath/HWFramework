package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.ims.AddressHeaderIms;
import gov.nist.javax.sip.parser.AddressParser;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import java.text.ParseException;

abstract class AddressHeaderParser extends HeaderParser {
    protected AddressHeaderParser(Lexer lexer) {
        super(lexer);
    }

    protected AddressHeaderParser(String buffer) {
        super(buffer);
    }

    protected void parse(AddressHeaderIms addressHeader) throws ParseException {
        dbg_enter("AddressHeaderParser.parse");
        try {
            addressHeader.setAddress(new AddressParser(getLexer()).address(true));
            dbg_leave("AddressParametersParser.parse");
        } catch (ParseException ex) {
            throw ex;
        } catch (Throwable th) {
            dbg_leave("AddressParametersParser.parse");
        }
    }
}
