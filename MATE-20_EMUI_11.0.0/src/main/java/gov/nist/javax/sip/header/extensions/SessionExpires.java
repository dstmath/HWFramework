package gov.nist.javax.sip.header.extensions;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.ExtensionHeader;

public final class SessionExpires extends ParametersHeader implements ExtensionHeader, SessionExpiresHeader {
    public static final String NAME = "Session-Expires";
    public static final String REFRESHER = "refresher";
    private static final long serialVersionUID = 8765762413224043300L;
    public int expires;

    public SessionExpires() {
        super("Session-Expires");
    }

    @Override // gov.nist.javax.sip.header.extensions.SessionExpiresHeader
    public int getExpires() {
        return this.expires;
    }

    @Override // gov.nist.javax.sip.header.extensions.SessionExpiresHeader
    public void setExpires(int expires2) throws InvalidArgumentException {
        if (expires2 >= 0) {
            this.expires = expires2;
            return;
        }
        throw new InvalidArgumentException("bad argument " + expires2);
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        String retval = Integer.toString(this.expires);
        if (this.parameters.isEmpty()) {
            return retval;
        }
        return retval + Separators.SEMICOLON + this.parameters.encode();
    }

    @Override // gov.nist.javax.sip.header.extensions.SessionExpiresHeader
    public String getRefresher() {
        return this.parameters.getParameter(REFRESHER);
    }

    @Override // gov.nist.javax.sip.header.extensions.SessionExpiresHeader
    public void setRefresher(String refresher) {
        this.parameters.set(REFRESHER, refresher);
    }
}
