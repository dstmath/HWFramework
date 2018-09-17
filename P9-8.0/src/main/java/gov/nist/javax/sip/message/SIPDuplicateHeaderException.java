package gov.nist.javax.sip.message;

import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class SIPDuplicateHeaderException extends ParseException {
    private static final long serialVersionUID = 8241107266407879291L;
    protected SIPHeader sipHeader;
    protected SIPMessage sipMessage;

    public SIPDuplicateHeaderException(String msg) {
        super(msg, 0);
    }

    public SIPMessage getSIPMessage() {
        return this.sipMessage;
    }

    public SIPHeader getSIPHeader() {
        return this.sipHeader;
    }

    public void setSIPHeader(SIPHeader sipHeader) {
        this.sipHeader = sipHeader;
    }

    public void setSIPMessage(SIPMessage sipMessage) {
        this.sipMessage = sipMessage;
    }
}
