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

    public void setErrorInfo(URI errorInfo2) {
        this.errorInfo = (GenericURI) errorInfo2;
    }

    public URI getErrorInfo() {
        return this.errorInfo;
    }

    public void setErrorMessage(String message) throws ParseException {
        if (message != null) {
            setParameter("message", message);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception , ErrorInfoHeader, setErrorMessage(), the message parameter is null");
    }

    public String getErrorMessage() {
        return getParameter("message");
    }

    public Object clone() {
        ErrorInfo retval = (ErrorInfo) super.clone();
        if (this.errorInfo != null) {
            retval.errorInfo = (GenericURI) this.errorInfo.clone();
        }
        return retval;
    }
}
