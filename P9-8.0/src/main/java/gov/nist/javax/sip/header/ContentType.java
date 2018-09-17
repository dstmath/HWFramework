package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.header.ContentTypeHeader;

public class ContentType extends ParametersHeader implements ContentTypeHeader {
    private static final long serialVersionUID = 8475682204373446610L;
    protected MediaRange mediaRange;

    public ContentType() {
        super("Content-Type");
    }

    public ContentType(String contentType, String contentSubtype) {
        this();
        setContentType(contentType, contentSubtype);
    }

    public int compareMediaRange(String media) {
        return (this.mediaRange.type + Separators.SLASH + this.mediaRange.subtype).compareToIgnoreCase(media);
    }

    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        this.mediaRange.encode(buffer);
        if (hasParameters()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public MediaRange getMediaRange() {
        return this.mediaRange;
    }

    public String getMediaType() {
        return this.mediaRange.type;
    }

    public String getMediaSubType() {
        return this.mediaRange.subtype;
    }

    public String getContentSubType() {
        return this.mediaRange == null ? null : this.mediaRange.getSubtype();
    }

    public String getContentType() {
        return this.mediaRange == null ? null : this.mediaRange.getType();
    }

    public String getCharset() {
        return getParameter("charset");
    }

    public void setMediaRange(MediaRange m) {
        this.mediaRange = m;
    }

    public void setContentType(String contentType, String contentSubType) {
        if (this.mediaRange == null) {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(contentType);
        this.mediaRange.setSubtype(contentSubType);
    }

    public void setContentType(String contentType) throws ParseException {
        if (contentType == null) {
            throw new NullPointerException("null arg");
        }
        if (this.mediaRange == null) {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(contentType);
    }

    public void setContentSubType(String contentType) throws ParseException {
        if (contentType == null) {
            throw new NullPointerException("null arg");
        }
        if (this.mediaRange == null) {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setSubtype(contentType);
    }

    public Object clone() {
        ContentType retval = (ContentType) super.clone();
        if (this.mediaRange != null) {
            retval.mediaRange = (MediaRange) this.mediaRange.clone();
        }
        return retval;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof ContentTypeHeader)) {
            return false;
        }
        ContentTypeHeader o = (ContentTypeHeader) other;
        if (getContentType().equalsIgnoreCase(o.getContentType()) && getContentSubType().equalsIgnoreCase(o.getContentSubType())) {
            z = equalParameters(o);
        }
        return z;
    }
}
