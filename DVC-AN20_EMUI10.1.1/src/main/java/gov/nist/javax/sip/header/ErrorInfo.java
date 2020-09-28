package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.GenericURI;
import java.text.ParseException;
import javax.sip.address.URI;
import javax.sip.header.ErrorInfoHeader;

public final class ErrorInfo extends ParametersHeader implements ErrorInfoHeader {
    private static final long serialVersionUID = -6347702901964436362L;
    protected GenericURI errorInfo;

    public ErrorInfo() {
        super("Error-Info");
    }

    public ErrorInfo(GenericURI errorInfo2) {
        this();
        this.errorInfo = errorInfo2;
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        StringBuffer stringBuffer = new StringBuffer(Separators.LESS_THAN);
        stringBuffer.append(this.errorInfo.toString());
        StringBuffer retval = stringBuffer.append(Separators.GREATER_THAN);
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON);
            retval.append(this.parameters.encode());
        }
        return retval.toString();
    }

    @Override // javax.sip.header.ErrorInfoHeader
    public void setErrorInfo(URI errorInfo2) {
        this.errorInfo = (GenericURI) errorInfo2;
    }

    @Override // javax.sip.header.ErrorInfoHeader
    public URI getErrorInfo() {
        return this.errorInfo;
    }

    @Override // javax.sip.header.ErrorInfoHeader
    public void setErrorMessage(String message) throws ParseException {
        if (message != null) {
            setParameter("message", message);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception , ErrorInfoHeader, setErrorMessage(), the message parameter is null");
    }

    @Override // javax.sip.header.ErrorInfoHeader
    public String getErrorMessage() {
        return getParameter("message");
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, java.lang.Object, javax.sip.header.Header, gov.nist.core.GenericObject
    public Object clone() {
        ErrorInfo retval = (ErrorInfo) super.clone();
        GenericURI genericURI = this.errorInfo;
        if (genericURI != null) {
            retval.errorInfo = (GenericURI) genericURI.clone();
        }
        return retval;
    }
}
