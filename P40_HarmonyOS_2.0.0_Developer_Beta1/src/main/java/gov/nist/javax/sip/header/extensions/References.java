package gov.nist.javax.sip.header.extensions;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import java.util.Iterator;
import javax.sip.header.ExtensionHeader;

public class References extends ParametersHeader implements ReferencesHeader, ExtensionHeader {
    private static final long serialVersionUID = 8536961681006637622L;
    private String callId;

    public References() {
        super(ReferencesHeader.NAME);
    }

    @Override // gov.nist.javax.sip.header.extensions.ReferencesHeader
    public String getCallId() {
        return this.callId;
    }

    @Override // gov.nist.javax.sip.header.extensions.ReferencesHeader
    public String getRel() {
        return getParameter(ReferencesHeader.REL);
    }

    @Override // gov.nist.javax.sip.header.extensions.ReferencesHeader
    public void setCallId(String callId2) {
        this.callId = callId2;
    }

    @Override // gov.nist.javax.sip.header.extensions.ReferencesHeader
    public void setRel(String rel) throws ParseException {
        if (rel != null) {
            setParameter(ReferencesHeader.REL, rel);
        }
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, javax.sip.header.Parameters
    public String getParameter(String name) {
        return super.getParameter(name);
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, javax.sip.header.Parameters
    public Iterator getParameterNames() {
        return super.getParameterNames();
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, javax.sip.header.Parameters
    public void removeParameter(String name) {
        super.removeParameter(name);
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, javax.sip.header.Parameters
    public void setParameter(String name, String value) throws ParseException {
        super.setParameter(name, value);
    }

    @Override // gov.nist.javax.sip.header.SIPHeader, javax.sip.header.Header
    public String getName() {
        return ReferencesHeader.NAME;
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        if (this.parameters.isEmpty()) {
            return this.callId;
        }
        return this.callId + Separators.SEMICOLON + this.parameters.encode();
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        throw new UnsupportedOperationException("operation not supported");
    }
}
