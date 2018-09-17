package gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class SecurityClient extends SecurityAgree implements SecurityClientHeader, ExtensionHeader {
    public SecurityClient() {
        super("Security-Client");
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
