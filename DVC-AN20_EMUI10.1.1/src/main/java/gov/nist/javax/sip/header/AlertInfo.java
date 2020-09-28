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

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        StringBuffer encoding = new StringBuffer();
        if (this.uri != null) {
            encoding.append(Separators.LESS_THAN);
            encoding.append(this.uri.encode());
            encoding.append(Separators.GREATER_THAN);
        } else {
            String str = this.string;
            if (str != null) {
                encoding.append(str);
            }
        }
        if (!this.parameters.isEmpty()) {
            encoding.append(Separators.SEMICOLON);
            encoding.append(this.parameters.encode());
        }
        return encoding.toString();
    }

    @Override // javax.sip.header.AlertInfoHeader
    public void setAlertInfo(URI uri2) {
        this.uri = (GenericURI) uri2;
    }

    @Override // javax.sip.header.AlertInfoHeader
    public void setAlertInfo(String string2) {
        this.string = string2;
    }

    @Override // javax.sip.header.AlertInfoHeader
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

    @Override // gov.nist.javax.sip.header.ParametersHeader, java.lang.Object, javax.sip.header.Header, gov.nist.core.GenericObject
    public Object clone() {
        AlertInfo retval = (AlertInfo) super.clone();
        GenericURI genericURI = this.uri;
        if (genericURI != null) {
            retval.uri = (GenericURI) genericURI.clone();
        } else {
            String str = this.string;
            if (str != null) {
                retval.string = str;
            }
        }
        return retval;
    }
}
