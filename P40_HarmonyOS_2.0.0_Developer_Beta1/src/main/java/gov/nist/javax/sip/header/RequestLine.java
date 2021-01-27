package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.address.GenericURI;
import javax.sip.address.URI;

public class RequestLine extends SIPObject implements SipRequestLine {
    private static final long serialVersionUID = -3286426172326043129L;
    protected String method;
    protected String sipVersion = SIPConstants.SIP_VERSION_STRING;
    protected GenericURI uri;

    public RequestLine() {
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        String str = this.method;
        if (str != null) {
            buffer.append(str);
            buffer.append(Separators.SP);
        }
        GenericURI genericURI = this.uri;
        if (genericURI != null) {
            genericURI.encode(buffer);
            buffer.append(Separators.SP);
        }
        buffer.append(this.sipVersion);
        buffer.append(Separators.NEWLINE);
        return buffer;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
    public GenericURI getUri() {
        return this.uri;
    }

    public RequestLine(GenericURI requestURI, String method2) {
        this.uri = requestURI;
        this.method = method2;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
    public String getMethod() {
        return this.method;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
    public String getSipVersion() {
        return this.sipVersion;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
    public void setUri(URI uri2) {
        this.uri = (GenericURI) uri2;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
    public void setMethod(String method2) {
        this.method = method2;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
    public void setSipVersion(String version) {
        this.sipVersion = version;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
    public String getVersionMajor() {
        if (this.sipVersion == null) {
            return null;
        }
        String major = null;
        boolean slash = false;
        int i = 0;
        while (i < this.sipVersion.length() && this.sipVersion.charAt(i) != '.') {
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
            i++;
        }
        return major;
    }

    @Override // gov.nist.javax.sip.header.SipRequestLine
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

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        if (!other.getClass().equals(getClass())) {
            return false;
        }
        RequestLine that = (RequestLine) other;
        try {
            if (!this.method.equals(that.method) || !this.uri.equals(that.uri) || !this.sipVersion.equals(that.sipVersion)) {
                return false;
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        RequestLine retval = (RequestLine) super.clone();
        GenericURI genericURI = this.uri;
        if (genericURI != null) {
            retval.uri = (GenericURI) genericURI.clone();
        }
        return retval;
    }
}
