package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.header.ExtensionHeader;

public class ExtensionHeaderImpl extends SIPHeader implements ExtensionHeader {
    private static final long serialVersionUID = -8693922839612081849L;
    protected String value;

    public ExtensionHeaderImpl() {
    }

    public ExtensionHeaderImpl(String headerName) {
        super(headerName);
    }

    public void setName(String headerName) {
        this.headerName = headerName;
    }

    public void setValue(String value2) {
        this.value = value2;
    }

    public String getHeaderValue() {
        if (this.value != null) {
            return this.value;
        }
        try {
            StringBuffer buffer = new StringBuffer(encode());
            while (buffer.length() > 0 && buffer.charAt(0) != ':') {
                buffer.deleteCharAt(0);
            }
            buffer.deleteCharAt(0);
            this.value = buffer.toString().trim();
            return this.value;
        } catch (Exception e) {
            return null;
        }
    }

    public String encode() {
        StringBuffer stringBuffer = new StringBuffer(this.headerName);
        stringBuffer.append(Separators.COLON);
        stringBuffer.append(Separators.SP);
        stringBuffer.append(this.value);
        stringBuffer.append(Separators.NEWLINE);
        return stringBuffer.toString();
    }

    public String encodeBody() {
        return getHeaderValue();
    }
}
