package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.InReplyToHeader;

public class InReplyTo extends SIPHeader implements InReplyToHeader {
    private static final long serialVersionUID = 1682602905733508890L;
    protected CallIdentifier callId;

    public InReplyTo() {
        super("In-Reply-To");
    }

    public InReplyTo(CallIdentifier cid) {
        super("In-Reply-To");
        this.callId = cid;
    }

    @Override // javax.sip.header.CallIdHeader
    public void setCallId(String callId2) throws ParseException {
        try {
            this.callId = new CallIdentifier(callId2);
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    @Override // javax.sip.header.CallIdHeader
    public String getCallId() {
        CallIdentifier callIdentifier = this.callId;
        if (callIdentifier == null) {
            return null;
        }
        return callIdentifier.encode();
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.callId.encode();
    }

    @Override // java.lang.Object, javax.sip.header.Header, gov.nist.core.GenericObject
    public Object clone() {
        InReplyTo retval = (InReplyTo) super.clone();
        CallIdentifier callIdentifier = this.callId;
        if (callIdentifier != null) {
            retval.callId = (CallIdentifier) callIdentifier.clone();
        }
        return retval;
    }
}
