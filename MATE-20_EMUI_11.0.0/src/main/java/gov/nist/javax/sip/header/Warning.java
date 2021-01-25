package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.WarningHeader;

public class Warning extends SIPHeader implements WarningHeader {
    private static final long serialVersionUID = -3433328864230783899L;
    protected String agent;
    protected int code;
    protected String text;

    public Warning() {
        super("Warning");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        if (this.text != null) {
            return Integer.toString(this.code) + Separators.SP + this.agent + Separators.SP + Separators.DOUBLE_QUOTE + this.text + Separators.DOUBLE_QUOTE;
        }
        return Integer.toString(this.code) + Separators.SP + this.agent;
    }

    @Override // javax.sip.header.WarningHeader
    public int getCode() {
        return this.code;
    }

    @Override // javax.sip.header.WarningHeader
    public String getAgent() {
        return this.agent;
    }

    @Override // javax.sip.header.WarningHeader
    public String getText() {
        return this.text;
    }

    @Override // javax.sip.header.WarningHeader
    public void setCode(int code2) throws InvalidArgumentException {
        if (code2 <= 99 || code2 >= 1000) {
            throw new InvalidArgumentException("Code parameter in the Warning header is invalid: code=" + code2);
        }
        this.code = code2;
    }

    @Override // javax.sip.header.WarningHeader
    public void setAgent(String host) throws ParseException {
        if (host != null) {
            this.agent = host;
            return;
        }
        throw new NullPointerException("the host parameter in the Warning header is null");
    }

    @Override // javax.sip.header.WarningHeader
    public void setText(String text2) throws ParseException {
        if (text2 != null) {
            this.text = text2;
            return;
        }
        throw new ParseException("The text parameter in the Warning header is null", 0);
    }
}
