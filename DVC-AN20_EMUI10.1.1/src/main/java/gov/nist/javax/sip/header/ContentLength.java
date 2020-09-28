package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.ContentLengthHeader;

public class ContentLength extends SIPHeader implements ContentLengthHeader {
    private static final long serialVersionUID = 1187190542411037027L;
    protected Integer contentLength;

    public ContentLength() {
        super("Content-Length");
    }

    public ContentLength(int length) {
        super("Content-Length");
        this.contentLength = Integer.valueOf(length);
    }

    @Override // javax.sip.header.ContentLengthHeader
    public int getContentLength() {
        return this.contentLength.intValue();
    }

    @Override // javax.sip.header.ContentLengthHeader
    public void setContentLength(int contentLength2) throws InvalidArgumentException {
        if (contentLength2 >= 0) {
            this.contentLength = Integer.valueOf(contentLength2);
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, ContentLength, setContentLength(), the contentLength parameter is <0");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        Integer num = this.contentLength;
        if (num == null) {
            buffer.append("0");
        } else {
            buffer.append(num.toString());
        }
        return buffer;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public boolean match(Object other) {
        if (other instanceof ContentLength) {
            return true;
        }
        return false;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header, gov.nist.core.GenericObject
    public boolean equals(Object other) {
        if (!(other instanceof ContentLengthHeader) || getContentLength() != ((ContentLengthHeader) other).getContentLength()) {
            return false;
        }
        return true;
    }
}
