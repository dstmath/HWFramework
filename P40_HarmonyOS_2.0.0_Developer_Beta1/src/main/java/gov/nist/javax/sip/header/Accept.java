package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.InvalidArgumentException;
import javax.sip.header.AcceptHeader;

public final class Accept extends ParametersHeader implements AcceptHeader {
    private static final long serialVersionUID = -7866187924308658151L;
    protected MediaRange mediaRange;

    public Accept() {
        super("Accept");
    }

    @Override // javax.sip.header.AcceptHeader
    public boolean allowsAllContentTypes() {
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 != null && mediaRange2.type.compareTo(Separators.STAR) == 0) {
            return true;
        }
        return false;
    }

    @Override // javax.sip.header.AcceptHeader
    public boolean allowsAllContentSubTypes() {
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 != null && mediaRange2.getSubtype().compareTo(Separators.STAR) == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 != null) {
            mediaRange2.encode(buffer);
        }
        if (this.parameters != null && !this.parameters.isEmpty()) {
            buffer.append(';');
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public MediaRange getMediaRange() {
        return this.mediaRange;
    }

    @Override // javax.sip.header.MediaType
    public String getContentType() {
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 == null) {
            return null;
        }
        return mediaRange2.getType();
    }

    @Override // javax.sip.header.MediaType
    public String getContentSubType() {
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 == null) {
            return null;
        }
        return mediaRange2.getSubtype();
    }

    @Override // javax.sip.header.AcceptHeader
    public float getQValue() {
        return getParameterAsFloat("q");
    }

    @Override // javax.sip.header.AcceptHeader
    public boolean hasQValue() {
        return super.hasParameter("q");
    }

    @Override // javax.sip.header.AcceptHeader
    public void removeQValue() {
        super.removeParameter("q");
    }

    @Override // javax.sip.header.MediaType
    public void setContentSubType(String subtype) {
        if (this.mediaRange == null) {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setSubtype(subtype);
    }

    @Override // javax.sip.header.MediaType
    public void setContentType(String type) {
        if (this.mediaRange == null) {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(type);
    }

    @Override // javax.sip.header.AcceptHeader
    public void setQValue(float qValue) throws InvalidArgumentException {
        if (qValue == -1.0f) {
            super.removeParameter("q");
        }
        super.setParameter("q", qValue);
    }

    public void setMediaRange(MediaRange m) {
        this.mediaRange = m;
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        Accept retval = (Accept) super.clone();
        MediaRange mediaRange2 = this.mediaRange;
        if (mediaRange2 != null) {
            retval.mediaRange = (MediaRange) mediaRange2.clone();
        }
        return retval;
    }
}
