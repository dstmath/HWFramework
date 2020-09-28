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

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
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

    @Override // javax.sip.header.MediaType
    public String getContentSubType() {
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 == null) {
            return null;
        }
        return mediaRange2.getSubtype();
    }

    @Override // javax.sip.header.MediaType
    public String getContentType() {
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 == null) {
            return null;
        }
        return mediaRange2.getType();
    }

    @Override // javax.sip.header.ContentTypeHeader
    public String getCharset() {
        return getParameter("charset");
    }

    public void setMediaRange(MediaRange m) {
        this.mediaRange = m;
    }

    @Override // javax.sip.header.ContentTypeHeader
    public void setContentType(String contentType, String contentSubType) {
        if (this.mediaRange == null) {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(contentType);
        this.mediaRange.setSubtype(contentSubType);
    }

    @Override // javax.sip.header.MediaType
    public void setContentType(String contentType) throws ParseException {
        if (contentType != null) {
            if (this.mediaRange == null) {
                this.mediaRange = new MediaRange();
            }
            this.mediaRange.setType(contentType);
            return;
        }
        throw new NullPointerException("null arg");
    }

    @Override // javax.sip.header.MediaType
    public void setContentSubType(String contentType) throws ParseException {
        if (contentType != null) {
            if (this.mediaRange == null) {
                this.mediaRange = new MediaRange();
            }
            this.mediaRange.setSubtype(contentType);
            return;
        }
        throw new NullPointerException("null arg");
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.core.GenericObject, java.lang.Object, javax.sip.header.Header
    public Object clone() {
        ContentType retval = (ContentType) super.clone();
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 != null) {
            retval.mediaRange = (MediaRange) mediaRange2.clone();
        }
        return retval;
    }

    @Override // gov.nist.core.GenericObject, gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header
    public boolean equals(Object other) {
        if (!(other instanceof ContentTypeHeader)) {
            return false;
        }
        ContentTypeHeader o = (ContentTypeHeader) other;
        if (!getContentType().equalsIgnoreCase(o.getContentType()) || !getContentSubType().equalsIgnoreCase(o.getContentSubType()) || !equalParameters(o)) {
            return false;
        }
        return true;
    }
}
