package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.ExpiresHeader;

public class Expires extends SIPHeader implements ExpiresHeader {
    private static final long serialVersionUID = 3134344915465784267L;
    protected int expires;

    public Expires() {
        super("Expires");
    }

    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuffer encodeBody(StringBuffer buffer) {
        buffer.append(this.expires);
        return buffer;
    }

    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int expires2) throws InvalidArgumentException {
        if (expires2 >= 0) {
            this.expires = expires2;
            return;
        }
        throw new InvalidArgumentException("bad argument " + expires2);
    }
}
