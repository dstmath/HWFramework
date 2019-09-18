package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.RequireHeader;

public class Require extends SIPHeader implements RequireHeader {
    private static final long serialVersionUID = -3743425404884053281L;
    protected String optionTag;

    public Require() {
        super("Require");
    }

    public Require(String s) {
        super("Require");
        this.optionTag = s;
    }

    public String encodeBody() {
        return this.optionTag;
    }

    public void setOptionTag(String optionTag2) throws ParseException {
        if (optionTag2 != null) {
            this.optionTag = optionTag2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Require, setOptionTag(), the optionTag parameter is null");
    }

    public String getOptionTag() {
        return this.optionTag;
    }
}
