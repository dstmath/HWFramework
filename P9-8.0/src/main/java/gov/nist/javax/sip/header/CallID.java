package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.CallIdHeader;

public class CallID extends SIPHeader implements CallIdHeader {
    private static final long serialVersionUID = -6463630258703731156L;
    protected CallIdentifier callIdentifier;

    public CallID() {
        super("Call-ID");
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CallIdHeader)) {
            return false;
        }
        return getCallId().equalsIgnoreCase(((CallIdHeader) other).getCallId());
    }

    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        if (this.callIdentifier != null) {
            this.callIdentifier.encode(buffer);
        }
        return buffer;
    }

    public String getCallId() {
        return encodeBody();
    }

    public CallIdentifier getCallIdentifer() {
        return this.callIdentifier;
    }

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

    public Object clone() {
        CallID retval = (CallID) super.clone();
        if (this.callIdentifier != null) {
            retval.callIdentifier = (CallIdentifier) this.callIdentifier.clone();
        }
        return retval;
    }
}
