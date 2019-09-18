package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.header.SupportedHeader;

public class Supported extends SIPHeader implements SupportedHeader {
    private static final long serialVersionUID = -7679667592702854542L;
    protected String optionTag;

    public Supported() {
        super("Supported");
        this.optionTag = null;
    }

    public Supported(String option_tag) {
        super("Supported");
        this.optionTag = option_tag;
    }

    public String encode() {
        String retval = this.headerName + Separators.COLON;
        if (this.optionTag != null) {
            retval = retval + Separators.SP + this.optionTag;
        }
        return retval + Separators.NEWLINE;
    }

    public String encodeBody() {
        return this.optionTag != null ? this.optionTag : "";
    }

    public void setOptionTag(String optionTag2) throws ParseException {
        if (optionTag2 != null) {
            this.optionTag = optionTag2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Supported, setOptionTag(), the optionTag parameter is null");
    }

    public String getOptionTag() {
        return this.optionTag;
    }
}
