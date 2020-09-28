package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.core.Token;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class PVisitedNetworkID extends ParametersHeader implements PVisitedNetworkIDHeader, SIPHeaderNamesIms, ExtensionHeader {
    private boolean isQuoted;
    private String networkID;

    public PVisitedNetworkID() {
        super("P-Visited-Network-ID");
    }

    public PVisitedNetworkID(String networkID2) {
        super("P-Visited-Network-ID");
        setVisitedNetworkID(networkID2);
    }

    public PVisitedNetworkID(Token tok) {
        super("P-Visited-Network-ID");
        setVisitedNetworkID(tok.getTokenValue());
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        StringBuffer retval = new StringBuffer();
        if (getVisitedNetworkID() != null) {
            if (this.isQuoted) {
                retval.append(Separators.DOUBLE_QUOTE + getVisitedNetworkID() + Separators.DOUBLE_QUOTE);
            } else {
                retval.append(getVisitedNetworkID());
            }
        }
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON + this.parameters.encode());
        }
        return retval.toString();
    }

    @Override // gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader
    public void setVisitedNetworkID(String networkID2) {
        if (networkID2 != null) {
            this.networkID = networkID2;
            this.isQuoted = true;
            return;
        }
        throw new NullPointerException(" the networkID parameter is null");
    }

    @Override // gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader
    public void setVisitedNetworkID(Token networkID2) {
        if (networkID2 != null) {
            this.networkID = networkID2.getTokenValue();
            this.isQuoted = false;
            return;
        }
        throw new NullPointerException(" the networkID parameter is null");
    }

    @Override // gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader
    public String getVisitedNetworkID() {
        return this.networkID;
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    @Override // gov.nist.core.GenericObject, gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header
    public boolean equals(Object other) {
        if (!(other instanceof PVisitedNetworkIDHeader)) {
            return false;
        }
        PVisitedNetworkIDHeader o = (PVisitedNetworkIDHeader) other;
        if (!getVisitedNetworkID().equals(o.getVisitedNetworkID()) || !equalParameters(o)) {
            return false;
        }
        return true;
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.core.GenericObject, java.lang.Object, javax.sip.header.Header
    public Object clone() {
        PVisitedNetworkID retval = (PVisitedNetworkID) super.clone();
        String str = this.networkID;
        if (str != null) {
            retval.networkID = str;
        }
        retval.isQuoted = this.isQuoted;
        return retval;
    }
}
