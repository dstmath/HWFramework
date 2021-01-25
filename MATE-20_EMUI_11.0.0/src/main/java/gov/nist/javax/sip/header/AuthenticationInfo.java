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

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.parameters.encode();
    }

    public NameValue getAuthInfo(String name) {
        return this.parameters.getNameValue(name);
    }

    public String getAuthenticationInfo() {
        return encodeBody();
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public String getCNonce() {
        return getParameter("cnonce");
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public String getNextNonce() {
        return getParameter(ParameterNames.NEXT_NONCE);
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public int getNonceCount() {
        return getParameterAsInt("nc");
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public String getQop() {
        return getParameter("qop");
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public String getResponse() {
        return getParameter(ParameterNames.RESPONSE_AUTH);
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public void setCNonce(String cNonce) throws ParseException {
        setParameter("cnonce", cNonce);
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public void setNextNonce(String nextNonce) throws ParseException {
        setParameter(ParameterNames.NEXT_NONCE, nextNonce);
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public void setNonceCount(int nonceCount) throws ParseException {
        if (nonceCount >= 0) {
            String nc = Integer.toHexString(nonceCount);
            setParameter("nc", "00000000".substring(0, 8 - nc.length()) + nc);
            return;
        }
        throw new ParseException("bad value", 0);
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public void setQop(String qop) throws ParseException {
        setParameter("qop", qop);
    }

    @Override // javax.sip.header.AuthenticationInfoHeader
    public void setResponse(String response) throws ParseException {
        setParameter(ParameterNames.RESPONSE_AUTH, response);
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, javax.sip.header.Parameters
    public void setParameter(String name, String value) throws ParseException {
        if (name != null) {
            NameValue nv = this.parameters.getNameValue(name.toLowerCase());
            if (nv == null) {
                NameValue nv2 = new NameValue(name, value);
                if (name.equalsIgnoreCase("qop") || name.equalsIgnoreCase(ParameterNames.NEXT_NONCE) || name.equalsIgnoreCase("realm") || name.equalsIgnoreCase("cnonce") || name.equalsIgnoreCase("nonce") || name.equalsIgnoreCase("opaque") || name.equalsIgnoreCase("username") || name.equalsIgnoreCase("domain") || name.equalsIgnoreCase(ParameterNames.NEXT_NONCE) || name.equalsIgnoreCase(ParameterNames.RESPONSE_AUTH)) {
                    if (value == null) {
                        throw new NullPointerException("null value");
                    } else if (!value.startsWith(Separators.DOUBLE_QUOTE)) {
                        nv2.setQuotedValue();
                    } else {
                        throw new ParseException(value + " : Unexpected DOUBLE_QUOTE", 0);
                    }
                }
                super.setParameter(nv2);
                return;
            }
            nv.setValueAsObject(value);
            return;
        }
        throw new NullPointerException("null name");
    }
}
