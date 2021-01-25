package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.SIPConstants;

public final class StatusLine extends SIPObject implements SipStatusLine {
    private static final long serialVersionUID = -4738092215519950414L;
    protected boolean matchStatusClass;
    protected String reasonPhrase = null;
    protected String sipVersion = SIPConstants.SIP_VERSION_STRING;
    protected int statusCode;

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public boolean match(Object matchObj) {
        String str;
        if (!(matchObj instanceof StatusLine)) {
            return false;
        }
        StatusLine sl = (StatusLine) matchObj;
        if (sl.matchExpression != null) {
            return sl.matchExpression.match(encode());
        }
        String str2 = sl.sipVersion;
        if (str2 != null && !str2.equals(this.sipVersion)) {
            return false;
        }
        int i = sl.statusCode;
        if (i != 0) {
            if (this.matchStatusClass) {
                int i2 = sl.statusCode;
                if (Integer.toString(i).charAt(0) != Integer.toString(this.statusCode).charAt(0)) {
                    return false;
                }
            } else if (this.statusCode != i) {
                return false;
            }
        }
        String str3 = sl.reasonPhrase;
        if (str3 == null || (str = this.reasonPhrase) == str3) {
            return true;
        }
        return str.equals(str3);
    }

    public void setMatchStatusClass(boolean flag) {
        this.matchStatusClass = flag;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        String encoding = "SIP/2.0 " + this.statusCode;
        if (this.reasonPhrase != null) {
            encoding = encoding + Separators.SP + this.reasonPhrase;
        }
        return encoding + Separators.NEWLINE;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public String getSipVersion() {
        return this.sipVersion;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public void setSipVersion(String s) {
        this.sipVersion = s;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public void setStatusCode(int statusCode2) {
        this.statusCode = statusCode2;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public void setReasonPhrase(String reasonPhrase2) {
        this.reasonPhrase = reasonPhrase2;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public String getVersionMajor() {
        if (this.sipVersion == null) {
            return null;
        }
        String major = null;
        boolean slash = false;
        for (int i = 0; i < this.sipVersion.length(); i++) {
            if (this.sipVersion.charAt(i) == '.') {
                slash = false;
            }
            if (slash) {
                major = major == null ? "" + this.sipVersion.charAt(i) : major + this.sipVersion.charAt(i);
            }
            if (this.sipVersion.charAt(i) == '/') {
                slash = true;
            }
        }
        return major;
    }

    @Override // gov.nist.javax.sip.header.SipStatusLine
    public String getVersionMinor() {
        if (this.sipVersion == null) {
            return null;
        }
        String minor = null;
        boolean dot = false;
        for (int i = 0; i < this.sipVersion.length(); i++) {
            if (dot) {
                minor = minor == null ? "" + this.sipVersion.charAt(i) : minor + this.sipVersion.charAt(i);
            }
            if (this.sipVersion.charAt(i) == '.') {
                dot = true;
            }
        }
        return minor;
    }
}
