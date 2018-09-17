package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.GenericURI;
import java.text.ParseException;
import javax.sip.address.URI;
import javax.sip.header.CallInfoHeader;

public final class CallInfo extends ParametersHeader implements CallInfoHeader {
    private static final long serialVersionUID = -8179246487696752928L;
    protected GenericURI info;

    public CallInfo() {
        super("Call-Info");
    }

    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        buffer.append(Separators.LESS_THAN);
        this.info.encode(buffer);
        buffer.append(Separators.GREATER_THAN);
        if (!(this.parameters == null || (this.parameters.isEmpty() ^ 1) == 0)) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public String getPurpose() {
        return getParameter(ParameterNames.PURPOSE);
    }

    public URI getInfo() {
        return this.info;
    }

    public void setPurpose(String purpose) {
        if (purpose == null) {
            throw new NullPointerException("null arg");
        }
        try {
            setParameter(ParameterNames.PURPOSE, purpose);
        } catch (ParseException e) {
        }
    }

    public void setInfo(URI info) {
        this.info = (GenericURI) info;
    }

    public Object clone() {
        CallInfo retval = (CallInfo) super.clone();
        if (this.info != null) {
            retval.info = (GenericURI) this.info.clone();
        }
        return retval;
    }
}
