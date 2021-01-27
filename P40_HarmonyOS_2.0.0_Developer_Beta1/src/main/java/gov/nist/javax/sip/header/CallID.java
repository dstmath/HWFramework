package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.CallIdHeader;

public class CallID extends SIPHeader implements CallIdHeader {
    private static final long serialVersionUID = -6463630258703731156L;
    protected CallIdentifier callIdentifier;

    public CallID() {
        super("Call-ID");
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof CallIdHeader) {
            return getCallId().equalsIgnoreCase(((CallIdHeader) other).getCallId());
        }
        return false;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        CallIdentifier callIdentifier2 = this.callIdentifier;
        if (callIdentifier2 != null) {
            callIdentifier2.encode(buffer);
        }
        return buffer;
    }

    @Override // javax.sip.header.CallIdHeader
    public String getCallId() {
        return encodeBody();
    }

    public CallIdentifier getCallIdentifer() {
        return this.callIdentifier;
    }

    @Override // javax.sip.header.CallIdHeader
    public void setCallId(String cid) throws ParseException {
        try {
            this.callIdentifier = new CallIdentifier(cid);
        } catch (IllegalArgumentException e) {
            throw new ParseException(cid, 0);
        }
    }

    public void setCallIdentifier(CallIdentifier cid) {
        this.callIdentifier = cid;
    }

    public CallID(String callId) throws IllegalArgumentException {
        super("Call-ID");
        this.callIdentifier = new CallIdentifier(callId);
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        CallID retval = (CallID) super.clone();
        CallIdentifier callIdentifier2 = this.callIdentifier;
        if (callIdentifier2 != null) {
            retval.callIdentifier = (CallIdentifier) callIdentifier2.clone();
        }
        return retval;
    }
}
