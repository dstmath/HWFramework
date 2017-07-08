package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.header.Header;

public abstract class SIPHeader extends SIPObject implements SIPHeaderNames, Header, HeaderExt {
    protected String headerName;

    protected abstract String encodeBody();

    protected SIPHeader(String hname) {
        this.headerName = hname;
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
        buffer.append(this.headerName).append(Separators.COLON).append(Separators.SP);
        encodeBody(buffer);
        buffer.append(Separators.NEWLINE);
        return buffer;
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        return buffer.append(encodeBody());
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
