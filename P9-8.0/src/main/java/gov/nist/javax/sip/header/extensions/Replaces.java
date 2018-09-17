package gov.nist.javax.sip.header.extensions;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.CallIdentifier;
import gov.nist.javax.sip.header.ParameterNames;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class Replaces extends ParametersHeader implements ExtensionHeader, ReplacesHeader {
    public static final String NAME = "Replaces";
    private static final long serialVersionUID = 8765762413224043300L;
    public String callId;
    public CallIdentifier callIdentifier;

    public Replaces() {
        super("Replaces");
    }

    public Replaces(String callId) throws IllegalArgumentException {
        super("Replaces");
        this.callIdentifier = new CallIdentifier(callId);
    }

    public String encodeBody() {
        if (this.callId == null) {
            return null;
        }
        String retVal = this.callId;
        if (!this.parameters.isEmpty()) {
            retVal = retVal + Separators.SEMICOLON + this.parameters.encode();
        }
        return retVal;
    }

    public String getCallId() {
        return this.callId;
    }

    public CallIdentifier getCallIdentifer() {
        return this.callIdentifier;
    }

    public void setCallId(String cid) {
        this.callId = cid;
    }

    public void setCallIdentifier(CallIdentifier cid) {
        this.callIdentifier = cid;
    }

    public String getToTag() {
        if (this.parameters == null) {
            return null;
        }
        return getParameter(ParameterNames.TO_TAG);
    }

    public void setToTag(String t) throws ParseException {
        if (t == null) {
            throw new NullPointerException("null tag ");
        } else if (t.trim().equals("")) {
            throw new ParseException("bad tag", 0);
        } else {
            setParameter(ParameterNames.TO_TAG, t);
        }
    }

    public boolean hasToTag() {
        return hasParameter(ParameterNames.TO_TAG);
    }

    public void removeToTag() {
        this.parameters.delete(ParameterNames.TO_TAG);
    }

    public String getFromTag() {
        if (this.parameters == null) {
            return null;
        }
        return getParameter(ParameterNames.FROM_TAG);
    }

    public void setFromTag(String t) throws ParseException {
        if (t == null) {
            throw new NullPointerException("null tag ");
        } else if (t.trim().equals("")) {
            throw new ParseException("bad tag", 0);
        } else {
            setParameter(ParameterNames.FROM_TAG, t);
        }
    }

    public boolean hasFromTag() {
        return hasParameter(ParameterNames.FROM_TAG);
    }

    public void removeFromTag() {
        this.parameters.delete(ParameterNames.FROM_TAG);
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
