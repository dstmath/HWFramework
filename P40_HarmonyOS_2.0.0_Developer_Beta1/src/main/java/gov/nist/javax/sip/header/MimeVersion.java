package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.InvalidArgumentException;
import javax.sip.header.MimeVersionHeader;

public class MimeVersion extends SIPHeader implements MimeVersionHeader {
    private static final long serialVersionUID = -7951589626435082068L;
    protected int majorVersion;
    protected int minorVersion;

    public MimeVersion() {
        super("MIME-Version");
    }

    @Override // javax.sip.header.MimeVersionHeader
    public int getMinorVersion() {
        return this.minorVersion;
    }

    @Override // javax.sip.header.MimeVersionHeader
    public int getMajorVersion() {
        return this.majorVersion;
    }

    @Override // javax.sip.header.MimeVersionHeader
    public void setMinorVersion(int minorVersion2) throws InvalidArgumentException {
        if (minorVersion2 >= 0) {
            this.minorVersion = minorVersion2;
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, MimeVersion, setMinorVersion(), the minorVersion parameter is null");
    }

    @Override // javax.sip.header.MimeVersionHeader
    public void setMajorVersion(int majorVersion2) throws InvalidArgumentException {
        if (majorVersion2 >= 0) {
            this.majorVersion = majorVersion2;
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, MimeVersion, setMajorVersion(), the majorVersion parameter is null");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return Integer.toString(this.majorVersion) + Separators.DOT + Integer.toString(this.minorVersion);
    }
}
