package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.SIPIfMatchHeader;

public class SIPIfMatch extends SIPHeader implements SIPIfMatchHeader, ExtensionHeader {
    private static final long serialVersionUID = 3833745477828359730L;
    protected String entityTag;

    public SIPIfMatch() {
        super("SIP-If-Match");
    }

    public SIPIfMatch(String etag) throws ParseException {
        this();
        setETag(etag);
    }

    public String encodeBody() {
        return this.entityTag;
    }

    public String getETag() {
        return this.entityTag;
    }

    public void setETag(String etag) throws ParseException {
        if (etag != null) {
            this.entityTag = etag;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception,SIP-If-Match, setETag(), the etag parameter is null");
    }

    public void setValue(String value) throws ParseException {
        setETag(value);
    }
}
