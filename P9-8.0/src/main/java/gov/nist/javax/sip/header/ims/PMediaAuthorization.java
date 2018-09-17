package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.ExtensionHeader;

public class PMediaAuthorization extends SIPHeader implements PMediaAuthorizationHeader, SIPHeaderNamesIms, ExtensionHeader {
    private static final long serialVersionUID = -6463630258703731133L;
    private String token;

    public PMediaAuthorization() {
        super("P-Media-Authorization");
    }

    public String getToken() {
        return this.token;
    }

    public void setMediaAuthorizationToken(String token) throws InvalidArgumentException {
        if (token == null || token.length() == 0) {
            throw new InvalidArgumentException(" the Media-Authorization-Token parameter is null or empty");
        }
        this.token = token;
    }

    protected String encodeBody() {
        return this.token;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public boolean equals(Object other) {
        if (!(other instanceof PMediaAuthorizationHeader)) {
            return false;
        }
        return getToken().equals(((PMediaAuthorizationHeader) other).getToken());
    }

    public Object clone() {
        PMediaAuthorization retval = (PMediaAuthorization) super.clone();
        if (this.token != null) {
            retval.token = this.token;
        }
        return retval;
    }
}
