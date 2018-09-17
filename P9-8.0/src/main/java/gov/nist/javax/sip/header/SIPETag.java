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

    public String encodeBody() {
        return this.entityTag;
    }

    public String getETag() {
        return this.entityTag;
    }

    public void setETag(String etag) throws ParseException {
        if (etag == null) {
            throw new NullPointerException("JAIN-SIP Exception,SIP-ETag, setETag(), the etag parameter is null");
        }
        this.entityTag = etag;
    }

    public void setValue(String value) throws ParseException {
        setETag(value);
    }
}
