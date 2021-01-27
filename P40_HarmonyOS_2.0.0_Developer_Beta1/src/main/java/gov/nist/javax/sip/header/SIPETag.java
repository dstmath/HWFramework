package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.SIPETagHeader;

public class SIPETag extends SIPHeader implements SIPETagHeader, ExtensionHeader {
    private static final long serialVersionUID = 3837543366074322107L;
    protected String entityTag;

    public SIPETag() {
        super("SIP-ETag");
    }

    public SIPETag(String tag) throws ParseException {
        this();
        setETag(tag);
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.entityTag;
    }

    @Override // javax.sip.header.SIPETagHeader
    public String getETag() {
        return this.entityTag;
    }

    @Override // javax.sip.header.SIPETagHeader
    public void setETag(String etag) throws ParseException {
        if (etag != null) {
            this.entityTag = etag;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception,SIP-ETag, setETag(), the etag parameter is null");
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        setETag(value);
    }
}
