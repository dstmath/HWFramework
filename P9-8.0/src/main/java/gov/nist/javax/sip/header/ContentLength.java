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

    public int getContentLength() {
        return this.contentLength.intValue();
    }

    public void setContentLength(int contentLength) throws InvalidArgumentException {
        if (contentLength < 0) {
            throw new InvalidArgumentException("JAIN-SIP Exception, ContentLength, setContentLength(), the contentLength parameter is <0");
        }
        this.contentLength = Integer.valueOf(contentLength);
    }

    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        if (this.contentLength == null) {
            buffer.append("0");
        } else {
            buffer.append(this.contentLength.toString());
        }
        return buffer;
    }

    public boolean match(Object other) {
        if (other instanceof ContentLength) {
            return true;
        }
        return false;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof ContentLengthHeader)) {
            return false;
        }
        if (getContentLength() == ((ContentLengthHeader) other).getContentLength()) {
            z = true;
        }
        return z;
    }
}
