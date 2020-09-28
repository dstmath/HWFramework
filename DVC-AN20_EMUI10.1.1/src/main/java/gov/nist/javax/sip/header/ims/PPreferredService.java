package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class PPreferredService extends SIPHeader implements PPreferredServiceHeader, SIPHeaderNamesIms, ExtensionHeader {
    private String subAppIds;
    private String subServiceIds;

    protected PPreferredService(String name) {
        super("P-Preferred-Service");
    }

    public PPreferredService() {
        super("P-Preferred-Service");
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        StringBuffer retval = new StringBuffer();
        retval.append(ParameterNamesIms.SERVICE_ID);
        if (this.subServiceIds != null) {
            retval.append(ParameterNamesIms.SERVICE_ID_LABEL);
            retval.append(Separators.DOT);
            retval.append(getSubserviceIdentifiers());
        } else if (this.subAppIds != null) {
            retval.append(ParameterNamesIms.APPLICATION_ID_LABEL);
            retval.append(Separators.DOT);
            retval.append(getApplicationIdentifiers());
        }
        return retval.toString();
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    @Override // gov.nist.javax.sip.header.ims.PPreferredServiceHeader
    public String getApplicationIdentifiers() {
        if (this.subAppIds.charAt(0) == '.') {
            return this.subAppIds.substring(1);
        }
        return this.subAppIds;
    }

    @Override // gov.nist.javax.sip.header.ims.PPreferredServiceHeader
    public String getSubserviceIdentifiers() {
        if (this.subServiceIds.charAt(0) == '.') {
            return this.subServiceIds.substring(1);
        }
        return this.subServiceIds;
    }

    @Override // gov.nist.javax.sip.header.ims.PPreferredServiceHeader
    public void setApplicationIdentifiers(String appids) {
        this.subAppIds = appids;
    }

    @Override // gov.nist.javax.sip.header.ims.PPreferredServiceHeader
    public void setSubserviceIdentifiers(String subservices) {
        this.subServiceIds = Separators.DOT.concat(subservices);
    }

    @Override // gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header, gov.nist.core.GenericObject
    public boolean equals(Object other) {
        return (other instanceof PPreferredServiceHeader) && super.equals(other);
    }

    @Override // java.lang.Object, javax.sip.header.Header, gov.nist.core.GenericObject
    public Object clone() {
        return (PPreferredService) super.clone();
    }
}
