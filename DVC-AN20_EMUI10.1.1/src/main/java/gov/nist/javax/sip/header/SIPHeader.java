package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.header.Header;

public abstract class SIPHeader extends SIPObject implements SIPHeaderNames, Header, HeaderExt {
    protected String headerName;

    /* access modifiers changed from: protected */
    public abstract String encodeBody();

    protected SIPHeader(String hname) {
        this.headerName = hname;
    }

    public SIPHeader() {
    }

    public String getHeaderName() {
        return this.headerName;
    }

    @Override // javax.sip.header.Header
    public String getName() {
        return this.headerName;
    }

    public void setHeaderName(String hdrname) {
        this.headerName = hdrname;
    }

    public String getHeaderValue() {
        try {
            StringBuffer buffer = new StringBuffer(encode());
            while (buffer.length() > 0 && buffer.charAt(0) != ':') {
                buffer.deleteCharAt(0);
            }
            if (buffer.length() > 0) {
                buffer.deleteCharAt(0);
            }
            return buffer.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isHeaderList() {
        return false;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(this.headerName);
        buffer.append(Separators.COLON);
        buffer.append(Separators.SP);
        encodeBody(buffer);
        buffer.append(Separators.NEWLINE);
        return buffer;
    }

    /* access modifiers changed from: protected */
    public StringBuffer encodeBody(StringBuffer buffer) {
        buffer.append(encodeBody());
        return buffer;
    }

    @Override // gov.nist.javax.sip.header.HeaderExt
    public String getValue() {
        return getHeaderValue();
    }

    @Override // javax.sip.header.Header
    public int hashCode() {
        return this.headerName.hashCode();
    }

    @Override // gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header
    public final String toString() {
        return encode();
    }
}
