package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.GenericURI;
import java.text.ParseException;
import javax.sip.address.URI;
import javax.sip.header.AlertInfoHeader;

public final class AlertInfo extends ParametersHeader implements AlertInfoHeader {
    private static final long serialVersionUID = 4159657362051508719L;
    protected String string;
    protected GenericURI uri;

    public AlertInfo() {
        super("Alert-Info");
    }

    protected String encodeBody() {
        StringBuffer encoding = new StringBuffer();
        if (this.uri != null) {
            encoding.append(Separators.LESS_THAN).append(this.uri.encode()).append(Separators.GREATER_THAN);
        } else if (this.string != null) {
            encoding.append(this.string);
        }
        if (!this.parameters.isEmpty()) {
            encoding.append(Separators.SEMICOLON).append(this.parameters.encode());
        }
        return encoding.toString();
    }

    public void setAlertInfo(URI uri) {
        this.uri = (GenericURI) uri;
    }

    public void setAlertInfo(String string) {
        this.string = string;
    }

    public URI getAlertInfo() {
        if (this.uri != null) {
            return this.uri;
        }
        try {
            return new GenericURI(this.string);
        } catch (ParseException e) {
            return null;
        }
    }

    public Object clone() {
        AlertInfo retval = (AlertInfo) super.clone();
        if (this.uri != null) {
            retval.uri = (GenericURI) this.uri.clone();
        } else if (this.string != null) {
            retval.string = this.string;
        }
        return retval;
    }
}
