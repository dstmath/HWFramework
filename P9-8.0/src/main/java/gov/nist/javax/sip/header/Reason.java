package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.Utils;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.ReasonHeader;

public class Reason extends ParametersHeader implements ReasonHeader {
    private static final long serialVersionUID = -8903376965568297388L;
    public final String CAUSE = ParameterNames.CAUSE;
    public final String TEXT = ParameterNames.TEXT;
    protected String protocol;

    public int getCause() {
        return getParameterAsInt(ParameterNames.CAUSE);
    }

    public void setCause(int cause) throws InvalidArgumentException {
        this.parameters.set(ParameterNames.CAUSE, Integer.valueOf(cause));
    }

    public void setProtocol(String protocol) throws ParseException {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setText(String text) throws ParseException {
        Object text2;
        if (text2.charAt(0) != '\"') {
            text2 = Utils.getQuotedString(text2);
        }
        this.parameters.set(ParameterNames.TEXT, text2);
    }

    public String getText() {
        return this.parameters.getParameter(ParameterNames.TEXT);
    }

    public Reason() {
        super("Reason");
    }

    public String getName() {
        return "Reason";
    }

    protected String encodeBody() {
        StringBuffer s = new StringBuffer();
        s.append(this.protocol);
        if (!(this.parameters == null || (this.parameters.isEmpty() ^ 1) == 0)) {
            s.append(Separators.SEMICOLON).append(this.parameters.encode());
        }
        return s.toString();
    }
}
