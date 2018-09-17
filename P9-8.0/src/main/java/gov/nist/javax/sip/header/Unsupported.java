package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.UnsupportedHeader;

public class Unsupported extends SIPHeader implements UnsupportedHeader {
    private static final long serialVersionUID = -2479414149440236199L;
    protected String optionTag;

    public Unsupported() {
        super("Unsupported");
    }

    public Unsupported(String ot) {
        super("Unsupported");
        this.optionTag = ot;
    }

    public String encodeBody() {
        return this.optionTag;
    }

    public String getOptionTag() {
        return this.optionTag;
    }

    public void setOptionTag(String o) throws ParseException {
        if (o == null) {
            throw new NullPointerException("JAIN-SIP Exception,  Unsupported, setOptionTag(), The option tag parameter is null");
        }
        this.optionTag = o;
    }
}
