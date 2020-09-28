package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.SubscriptionStateHeader;

public class SubscriptionState extends ParametersHeader implements SubscriptionStateHeader {
    private static final long serialVersionUID = -6673833053927258745L;
    protected int expires = -1;
    protected String reasonCode;
    protected int retryAfter = -1;
    protected String state;

    public SubscriptionState() {
        super("Subscription-State");
    }

    @Override // javax.sip.header.ExpiresHeader
    public void setExpires(int expires2) throws InvalidArgumentException {
        if (expires2 >= 0) {
            this.expires = expires2;
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, SubscriptionState, setExpires(), the expires parameter is  < 0");
    }

    @Override // javax.sip.header.ExpiresHeader
    public int getExpires() {
        return this.expires;
    }

    @Override // javax.sip.header.SubscriptionStateHeader
    public void setRetryAfter(int retryAfter2) throws InvalidArgumentException {
        if (retryAfter2 > 0) {
            this.retryAfter = retryAfter2;
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, SubscriptionState, setRetryAfter(), the retryAfter parameter is <=0");
    }

    @Override // javax.sip.header.SubscriptionStateHeader
    public int getRetryAfter() {
        return this.retryAfter;
    }

    @Override // javax.sip.header.SubscriptionStateHeader
    public String getReasonCode() {
        return this.reasonCode;
    }

    @Override // javax.sip.header.SubscriptionStateHeader
    public void setReasonCode(String reasonCode2) throws ParseException {
        if (reasonCode2 != null) {
            this.reasonCode = reasonCode2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, SubscriptionState, setReasonCode(), the reasonCode parameter is null");
    }

    @Override // javax.sip.header.SubscriptionStateHeader
    public String getState() {
        return this.state;
    }

    @Override // javax.sip.header.SubscriptionStateHeader
    public void setState(String state2) throws ParseException {
        if (state2 != null) {
            this.state = state2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, SubscriptionState, setState(), the state parameter is null");
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        String str = this.state;
        if (str != null) {
            buffer.append(str);
        }
        if (this.reasonCode != null) {
            buffer.append(";reason=");
            buffer.append(this.reasonCode);
        }
        if (this.expires != -1) {
            buffer.append(";expires=");
            buffer.append(this.expires);
        }
        if (this.retryAfter != -1) {
            buffer.append(";retry-after=");
            buffer.append(this.retryAfter);
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }
}
