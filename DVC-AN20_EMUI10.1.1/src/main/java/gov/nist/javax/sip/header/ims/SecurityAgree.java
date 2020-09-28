package gov.nist.javax.sip.header.ims;

import gov.nist.core.NameValue;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public abstract class SecurityAgree extends ParametersHeader {
    private String secMechanism;

    public SecurityAgree(String name) {
        super(name);
        this.parameters.setSeparator(Separators.SEMICOLON);
    }

    public SecurityAgree() {
        this.parameters.setSeparator(Separators.SEMICOLON);
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, javax.sip.header.Parameters
    public void setParameter(String name, String value) throws ParseException {
        if (value != null) {
            NameValue nv = this.parameters.getNameValue(name.toLowerCase());
            if (nv == null) {
                NameValue nv2 = new NameValue(name, value);
                if (name.equalsIgnoreCase(ParameterNamesIms.D_VER)) {
                    nv2.setQuotedValue();
                    if (value.startsWith(Separators.DOUBLE_QUOTE)) {
                        throw new ParseException(value + " : Unexpected DOUBLE_QUOTE", 0);
                    }
                }
                super.setParameter(nv2);
                return;
            }
            nv.setValueAsObject(value);
            return;
        }
        throw new NullPointerException("null value");
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.secMechanism + Separators.SEMICOLON + Separators.SP + this.parameters.encode();
    }

    public void setSecurityMechanism(String secMech) throws ParseException {
        if (secMech != null) {
            this.secMechanism = secMech;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, SecurityAgree, setSecurityMechanism(), the sec-mechanism parameter is null");
    }

    public void setEncryptionAlgorithm(String ealg) throws ParseException {
        if (ealg != null) {
            setParameter(ParameterNamesIms.EALG, ealg);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, SecurityClient, setEncryptionAlgorithm(), the encryption-algorithm parameter is null");
    }

    public void setAlgorithm(String alg) throws ParseException {
        if (alg != null) {
            setParameter(ParameterNamesIms.ALG, alg);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, SecurityClient, setAlgorithm(), the algorithm parameter is null");
    }

    public void setProtocol(String prot) throws ParseException {
        if (prot != null) {
            setParameter(ParameterNamesIms.PROT, prot);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, SecurityClient, setProtocol(), the protocol parameter is null");
    }

    public void setMode(String mod) throws ParseException {
        if (mod != null) {
            setParameter(ParameterNamesIms.MOD, mod);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, SecurityClient, setMode(), the mode parameter is null");
    }

    public void setSPIClient(int spic) throws InvalidArgumentException {
        if (spic >= 0) {
            setParameter(ParameterNamesIms.SPI_C, spic);
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, SecurityClient, setSPIClient(), the spi-c parameter is <0");
    }

    public void setSPIServer(int spis) throws InvalidArgumentException {
        if (spis >= 0) {
            setParameter(ParameterNamesIms.SPI_S, spis);
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, SecurityClient, setSPIServer(), the spi-s parameter is <0");
    }

    public void setPortClient(int portC) throws InvalidArgumentException {
        if (portC >= 0) {
            setParameter(ParameterNamesIms.PORT_C, portC);
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, SecurityClient, setPortClient(), the port-c parameter is <0");
    }

    public void setPortServer(int portS) throws InvalidArgumentException {
        if (portS >= 0) {
            setParameter(ParameterNamesIms.PORT_S, portS);
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, SecurityClient, setPortServer(), the port-s parameter is <0");
    }

    public void setPreference(float q) throws InvalidArgumentException {
        if (q >= 0.0f) {
            setParameter("q", q);
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, SecurityClient, setPreference(), the preference (q) parameter is <0");
    }

    public String getSecurityMechanism() {
        return this.secMechanism;
    }

    public String getEncryptionAlgorithm() {
        return getParameter(ParameterNamesIms.EALG);
    }

    public String getAlgorithm() {
        return getParameter(ParameterNamesIms.ALG);
    }

    public String getProtocol() {
        return getParameter(ParameterNamesIms.PROT);
    }

    public String getMode() {
        return getParameter(ParameterNamesIms.MOD);
    }

    public int getSPIClient() {
        return Integer.parseInt(getParameter(ParameterNamesIms.SPI_C));
    }

    public int getSPIServer() {
        return Integer.parseInt(getParameter(ParameterNamesIms.SPI_S));
    }

    public int getPortClient() {
        return Integer.parseInt(getParameter(ParameterNamesIms.PORT_C));
    }

    public int getPortServer() {
        return Integer.parseInt(getParameter(ParameterNamesIms.PORT_S));
    }

    public float getPreference() {
        return Float.parseFloat(getParameter("q"));
    }

    @Override // gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header, gov.nist.core.GenericObject
    public boolean equals(Object other) {
        if (!(other instanceof SecurityAgreeHeader)) {
            return false;
        }
        SecurityAgreeHeader o = (SecurityAgreeHeader) other;
        if (!getSecurityMechanism().equals(o.getSecurityMechanism()) || !equalParameters(o)) {
            return false;
        }
        return true;
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, java.lang.Object, javax.sip.header.Header, gov.nist.core.GenericObject
    public Object clone() {
        SecurityAgree retval = (SecurityAgree) super.clone();
        String str = this.secMechanism;
        if (str != null) {
            retval.secMechanism = str;
        }
        return retval;
    }
}
