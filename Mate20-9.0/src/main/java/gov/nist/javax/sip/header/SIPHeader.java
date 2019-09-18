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

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

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

    public String getValue() {
        return getHeaderValue();
    }

    public int hashCode() {
        return this.headerName.hashCode();
    }

    public final String toString() {
        return encode();
    }
}
