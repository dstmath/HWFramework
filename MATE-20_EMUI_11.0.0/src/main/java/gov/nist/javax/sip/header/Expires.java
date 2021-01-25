package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.ExpiresHeader;

public class Expires extends SIPHeader implements ExpiresHeader {
    private static final long serialVersionUID = 3134344915465784267L;
    protected int expires;

    public Expires() {
        super("Expires");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        buffer.append(this.expires);
        return buffer;
    }

    @Override // javax.sip.header.ExpiresHeader
    public int getExpires() {
        return this.expires;
    }

    @Override // javax.sip.header.ExpiresHeader
    public void setExpires(int expires2) throws InvalidArgumentException {
        if (expires2 >= 0) {
            this.expires = expires2;
            return;
        }
        throw new InvalidArgumentException("bad argument " + expires2);
    }
}
