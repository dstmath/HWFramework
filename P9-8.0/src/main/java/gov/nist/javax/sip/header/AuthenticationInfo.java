package gov.nist.javax.sip.header;

import gov.nist.core.NameValue;
import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.header.AuthenticationInfoHeader;

public final class AuthenticationInfo extends ParametersHeader implements AuthenticationInfoHeader {
    private static final long serialVersionUID = -4371927900917127057L;

    public AuthenticationInfo() {
        super("Authentication-Info");
        this.parameters.setSeparator(Separators.COMMA);
    }

    public void add(NameValue nv) {
        this.parameters.set(nv);
    }

    protected String encodeBody() {
        return this.parameters.encode();
    }

    public NameValue getAuthInfo(String name) {
        return this.parameters.getNameValue(name);
    }

    public String getAuthenticationInfo() {
        return encodeBody();
    }

    public String getCNonce() {
        return getParameter("cnonce");
    }

    public String getNextNonce() {
        return getParameter(ParameterNames.NEXT_NONCE);
    }

    public int getNonceCount() {
        return getParameterAsInt("nc");
    }

    public String getQop() {
        return getParameter("qop");
    }

    public String getResponse() {
        return getParameter(ParameterNames.RESPONSE_AUTH);
    }

    public void setCNonce(String cNonce) throws ParseException {
        setParameter("cnonce", cNonce);
    }

    public void setNextNonce(String nextNonce) throws ParseException {
        setParameter(ParameterNames.NEXT_NONCE, nextNonce);
    }

    public void setNonceCount(int nonceCount) throws ParseException {
        if (nonceCount < 0) {
            throw new ParseException("bad value", 0);
        }
        String nc = Integer.toHexString(nonceCount);
        setParameter("nc", "00000000".substring(0, 8 - nc.length()) + nc);
    }

    public void setQop(String qop) throws ParseException {
        setParameter("qop", qop);
    }

    public void setResponse(String response) throws ParseException {
        setParameter(ParameterNames.RESPONSE_AUTH, response);
    }

    public void setParameter(String name, String value) throws ParseException {
        if (name == null) {
            throw new NullPointerException("null name");
        }
        NameValue nv = this.parameters.getNameValue(name.toLowerCase());
        if (nv == null) {
            nv = new NameValue(name, value);
            if (name.equalsIgnoreCase("qop") || name.equalsIgnoreCase(ParameterNames.NEXT_NONCE) || name.equalsIgnoreCase("realm") || name.equalsIgnoreCase("cnonce") || name.equalsIgnoreCase("nonce") || name.equalsIgnoreCase("opaque") || name.equalsIgnoreCase("username") || name.equalsIgnoreCase("domain") || name.equalsIgnoreCase(ParameterNames.NEXT_NONCE) || name.equalsIgnoreCase(ParameterNames.RESPONSE_AUTH)) {
                if (value == null) {
                    throw new NullPointerException("null value");
                } else if (value.startsWith(Separators.DOUBLE_QUOTE)) {
                    throw new ParseException(value + " : Unexpected DOUBLE_QUOTE", 0);
                } else {
                    nv.setQuotedValue();
                }
            }
            super.setParameter(nv);
            return;
        }
        nv.setValueAsObject(value);
    }
}
