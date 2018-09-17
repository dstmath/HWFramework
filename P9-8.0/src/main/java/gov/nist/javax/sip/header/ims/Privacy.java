package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class Privacy extends SIPHeader implements PrivacyHeader, SIPHeaderNamesIms, ExtensionHeader {
    private String privacy;

    public Privacy() {
        super("Privacy");
    }

    public Privacy(String privacy) {
        this();
        this.privacy = privacy;
    }

    public String encodeBody() {
        return this.privacy;
    }

    public String getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(String privacy) throws ParseException {
        if (privacy == null || privacy == "") {
            throw new NullPointerException("JAIN-SIP Exception,  Privacy, setPrivacy(), privacy value is null or empty");
        }
        this.privacy = privacy;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public boolean equals(Object other) {
        if (!(other instanceof PrivacyHeader)) {
            return false;
        }
        return getPrivacy().equals(((PrivacyHeader) other).getPrivacy());
    }

    public Object clone() {
        Privacy retval = (Privacy) super.clone();
        if (this.privacy != null) {
            retval.privacy = this.privacy;
        }
        return retval;
    }
}
