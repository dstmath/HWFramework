package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class PAccessNetworkInfo extends ParametersHeader implements PAccessNetworkInfoHeader, ExtensionHeader {
    private String accessType;
    private Object extendAccessInfo;

    public PAccessNetworkInfo() {
        super("P-Access-Network-Info");
        this.parameters.setSeparator(Separators.SEMICOLON);
    }

    public PAccessNetworkInfo(String accessTypeVal) {
        this();
        setAccessType(accessTypeVal);
    }

    public void setAccessType(String accessTypeVal) {
        if (accessTypeVal != null) {
            this.accessType = accessTypeVal;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Access-Network-Info, setAccessType(), the accessType parameter is null.");
    }

    public String getAccessType() {
        return this.accessType;
    }

    public void setCGI3GPP(String cgi) throws ParseException {
        if (cgi != null) {
            setParameter(ParameterNamesIms.CGI_3GPP, cgi);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Access-Network-Info, setCGI3GPP(), the cgi parameter is null.");
    }

    public String getCGI3GPP() {
        return getParameter(ParameterNamesIms.CGI_3GPP);
    }

    public void setUtranCellID3GPP(String utranCellID) throws ParseException {
        if (utranCellID != null) {
            setParameter(ParameterNamesIms.UTRAN_CELL_ID_3GPP, utranCellID);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Access-Network-Info, setUtranCellID3GPP(), the utranCellID parameter is null.");
    }

    public String getUtranCellID3GPP() {
        return getParameter(ParameterNamesIms.UTRAN_CELL_ID_3GPP);
    }

    public void setDSLLocation(String dslLocation) throws ParseException {
        if (dslLocation != null) {
            setParameter(ParameterNamesIms.DSL_LOCATION, dslLocation);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Access-Network-Info, setDSLLocation(), the dslLocation parameter is null.");
    }

    public String getDSLLocation() {
        return getParameter(ParameterNamesIms.DSL_LOCATION);
    }

    public void setCI3GPP2(String ci3Gpp2) throws ParseException {
        if (ci3Gpp2 != null) {
            setParameter(ParameterNamesIms.CI_3GPP2, ci3Gpp2);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Access-Network-Info, setCI3GPP2(), the ci3Gpp2 parameter is null.");
    }

    public String getCI3GPP2() {
        return getParameter(ParameterNamesIms.CI_3GPP2);
    }

    public void setParameter(String name, Object value) {
        if (name.equalsIgnoreCase(ParameterNamesIms.CGI_3GPP) || name.equalsIgnoreCase(ParameterNamesIms.UTRAN_CELL_ID_3GPP) || name.equalsIgnoreCase(ParameterNamesIms.DSL_LOCATION) || name.equalsIgnoreCase(ParameterNamesIms.CI_3GPP2)) {
            try {
                super.setQuotedParameter(name, value.toString());
            } catch (ParseException e) {
            }
        } else {
            super.setParameter(name, value);
        }
    }

    public void setExtensionAccessInfo(Object extendAccessInfo2) throws ParseException {
        if (extendAccessInfo2 != null) {
            this.extendAccessInfo = extendAccessInfo2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, P-Access-Network-Info, setExtendAccessInfo(), the extendAccessInfo parameter is null.");
    }

    public Object getExtensionAccessInfo() {
        return this.extendAccessInfo;
    }

    /* access modifiers changed from: protected */
    public String encodeBody() {
        StringBuffer encoding = new StringBuffer();
        if (getAccessType() != null) {
            encoding.append(getAccessType());
        }
        if (!this.parameters.isEmpty()) {
            encoding.append("; " + this.parameters.encode());
        }
        if (getExtensionAccessInfo() != null) {
            encoding.append("; " + getExtensionAccessInfo().toString());
        }
        return encoding.toString();
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public boolean equals(Object other) {
        return (other instanceof PAccessNetworkInfoHeader) && super.equals(other);
    }

    public Object clone() {
        return (PAccessNetworkInfo) super.clone();
    }
}
