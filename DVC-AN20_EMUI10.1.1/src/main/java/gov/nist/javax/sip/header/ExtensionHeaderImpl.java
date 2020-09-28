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

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value2) {
        this.value = value2;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String getHeaderValue() {
        String str = this.value;
        if (str != null) {
            return str;
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

    @Override // gov.nist.javax.sip.header.SIPHeader, gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        StringBuffer stringBuffer = new StringBuffer(this.headerName);
        stringBuffer.append(Separators.COLON);
        stringBuffer.append(Separators.SP);
        stringBuffer.append(this.value);
        stringBuffer.append(Separators.NEWLINE);
        return stringBuffer.toString();
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return getHeaderValue();
    }
}
