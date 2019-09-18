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

    public void setVisitedNetworkID(String networkID2) {
        if (networkID2 != null) {
            this.networkID = networkID2;
            this.isQuoted = true;
            return;
        }
        throw new NullPointerException(" the networkID parameter is null");
    }

    public void setVisitedNetworkID(Token networkID2) {
        if (networkID2 != null) {
            this.networkID = networkID2.getTokenValue();
            this.isQuoted = false;
            return;
        }
        throw new NullPointerException(" the networkID parameter is null");
    }

    public String getVisitedNetworkID() {
        return this.networkID;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof PVisitedNetworkIDHeader)) {
            return false;
        }
        PVisitedNetworkIDHeader o = (PVisitedNetworkIDHeader) other;
        if (getVisitedNetworkID().equals(o.getVisitedNetworkID()) && equalParameters(o)) {
            z = true;
        }
        return z;
    }

    public Object clone() {
        PVisitedNetworkID retval = (PVisitedNetworkID) super.clone();
        if (this.networkID != null) {
            retval.networkID = this.networkID;
        }
        retval.isQuoted = this.isQuoted;
        return retval;
    }
}
