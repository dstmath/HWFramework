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

    @Override // gov.nist.javax.sip.header.SIPHeader, gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        String retval = this.headerName + Separators.COLON;
        if (this.optionTag != null) {
            retval = retval + Separators.SP + this.optionTag;
        }
        return retval + Separators.NEWLINE;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        String str = this.optionTag;
        return str != null ? str : "";
    }

    @Override // javax.sip.header.OptionTag
    public void setOptionTag(String optionTag2) throws ParseException {
        if (optionTag2 != null) {
            this.optionTag = optionTag2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Supported, setOptionTag(), the optionTag parameter is null");
    }

    @Override // javax.sip.header.OptionTag
    public String getOptionTag() {
        return this.optionTag;
    }
}
