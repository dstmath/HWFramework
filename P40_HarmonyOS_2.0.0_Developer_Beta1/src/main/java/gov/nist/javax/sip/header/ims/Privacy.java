package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class Privacy extends SIPHeader implements PrivacyHeader, SIPHeaderNamesIms, ExtensionHeader {
    private String privacy;

    public Privacy() {
        super("Privacy");
    }

    public Privacy(String privacy2) {
        this();
        this.privacy = privacy2;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.privacy;
    }

    @Override // gov.nist.javax.sip.header.ims.PrivacyHeader
    public String getPrivacy() {
        return this.privacy;
    }

    @Override // gov.nist.javax.sip.header.ims.PrivacyHeader
    public void setPrivacy(String privacy2) throws ParseException {
        if (privacy2 == null || privacy2 == "") {
            throw new NullPointerException("JAIN-SIP Exception,  Privacy, setPrivacy(), privacy value is null or empty");
        }
        this.privacy = privacy2;
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        if (other instanceof PrivacyHeader) {
            return getPrivacy().equals(((PrivacyHeader) other).getPrivacy());
        }
        return false;
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        Privacy retval = (Privacy) super.clone();
        String str = this.privacy;
        if (str != null) {
            retval.privacy = str;
        }
        return retval;
    }
}
