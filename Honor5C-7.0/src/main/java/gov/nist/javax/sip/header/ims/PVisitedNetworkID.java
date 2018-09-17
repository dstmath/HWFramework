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
        super(PVisitedNetworkIDHeader.NAME);
    }

    public PVisitedNetworkID(String networkID) {
        super(PVisitedNetworkIDHeader.NAME);
        setVisitedNetworkID(networkID);
    }

    public PVisitedNetworkID(Token tok) {
        super(PVisitedNetworkIDHeader.NAME);
        setVisitedNetworkID(tok.getTokenValue());
    }

    protected String encodeBody() {
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

    public void setVisitedNetworkID(String networkID) {
        if (networkID == null) {
            throw new NullPointerException(" the networkID parameter is null");
        }
        this.networkID = networkID;
        this.isQuoted = true;
    }

    public void setVisitedNetworkID(Token networkID) {
        if (networkID == null) {
            throw new NullPointerException(" the networkID parameter is null");
        }
        this.networkID = networkID.getTokenValue();
        this.isQuoted = false;
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
        if (getVisitedNetworkID().equals(o.getVisitedNetworkID())) {
            z = equalParameters(o);
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
