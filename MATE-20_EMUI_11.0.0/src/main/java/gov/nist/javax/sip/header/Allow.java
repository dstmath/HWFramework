package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.AllowHeader;

public final class Allow extends SIPHeader implements AllowHeader {
    private static final long serialVersionUID = -3105079479020693930L;
    protected String method;

    public Allow() {
        super("Allow");
    }

    public Allow(String m) {
        super("Allow");
        this.method = m;
    }

    @Override // javax.sip.header.AllowHeader
    public String getMethod() {
        return this.method;
    }

    @Override // javax.sip.header.AllowHeader
    public void setMethod(String method2) throws ParseException {
        if (method2 != null) {
            this.method = method2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Allow, setMethod(), the method parameter is null.");
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.method;
    }
}
