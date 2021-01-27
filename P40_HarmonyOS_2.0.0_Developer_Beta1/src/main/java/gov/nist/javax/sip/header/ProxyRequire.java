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

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.optionTag;
    }

    @Override // javax.sip.header.OptionTag
    public void setOptionTag(String optionTag2) throws ParseException {
        if (optionTag2 != null) {
            this.optionTag = optionTag2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, ProxyRequire, setOptionTag(), the optionTag parameter is null");
    }

    @Override // javax.sip.header.OptionTag
    public String getOptionTag() {
        return this.optionTag;
    }
}
