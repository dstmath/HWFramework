package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.SIPConstants;

public final class StatusLine extends SIPObject implements SipStatusLine {
    private static final long serialVersionUID = -4738092215519950414L;
    protected boolean matchStatusClass;
    protected String reasonPhrase = null;
    protected String sipVersion = SIPConstants.SIP_VERSION_STRING;
    protected int statusCode;

    public boolean match(Object matchObj) {
        if (!(matchObj instanceof StatusLine)) {
            return false;
        }
        StatusLine sl = (StatusLine) matchObj;
        if (sl.matchExpression != null) {
            return sl.matchExpression.match(encode());
        }
        if (sl.sipVersion != null && (sl.sipVersion.equals(this.sipVersion) ^ 1) != 0) {
            return false;
        }
        if (sl.statusCode != 0) {
            if (this.matchStatusClass) {
                int hiscode = sl.statusCode;
                if (Integer.toString(sl.statusCode).charAt(0) != Integer.toString(this.statusCode).charAt(0)) {
                    return false;
                }
            } else if (this.statusCode != sl.statusCode) {
                return false;
            }
        }
        if (sl.reasonPhrase == null || this.reasonPhrase == sl.reasonPhrase) {
            return true;
        }
        return this.reasonPhrase.equals(sl.reasonPhrase);
    }

    public void setMatchStatusClass(boolean flag) {
        this.matchStatusClass = flag;
    }

    public String encode() {
        String encoding = "SIP/2.0 " + this.statusCode;
        if (this.reasonPhrase != null) {
            encoding = encoding + Separators.SP + this.reasonPhrase;
        }
        return encoding + Separators.NEWLINE;
    }

    public String getSipVersion() {
        return this.sipVersion;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public void setSipVersion(String s) {
        this.sipVersion = s;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

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
                if (major == null) {
                    major = "" + this.sipVersion.charAt(i);
                } else {
                    major = major + this.sipVersion.charAt(i);
                }
            }
            if (this.sipVersion.charAt(i) == '/') {
                slash = true;
            }
        }
        return major;
    }

    public String getVersionMinor() {
        if (this.sipVersion == null) {
            return null;
        }
        String minor = null;
        boolean dot = false;
        for (int i = 0; i < this.sipVersion.length(); i++) {
            if (dot) {
                if (minor == null) {
                    minor = "" + this.sipVersion.charAt(i);
                } else {
                    minor = minor + this.sipVersion.charAt(i);
                }
            }
            if (this.sipVersion.charAt(i) == '.') {
                dot = true;
            }
        }
        return minor;
    }
}
