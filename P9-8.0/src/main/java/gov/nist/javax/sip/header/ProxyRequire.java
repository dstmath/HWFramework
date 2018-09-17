package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.ProxyRequireHeader;

public class ProxyRequire extends SIPHeader implements ProxyRequireHeader {
    private static final long serialVersionUID = -3269274234851067893L;
    protected String optionTag;

    public ProxyRequire() {
        super("Proxy-Require");
    }

    public ProxyRequire(String s) {
        super("Proxy-Require");
        this.optionTag = s;
    }

    public String encodeBody() {
        return this.optionTag;
    }

    public void setOptionTag(String optionTag) throws ParseException {
        if (optionTag == null) {
            throw new NullPointerException("JAIN-SIP Exception, ProxyRequire, setOptionTag(), the optionTag parameter is null");
        }
        this.optionTag = optionTag;
    }

    public String getOptionTag() {
        return this.optionTag;
    }
}
