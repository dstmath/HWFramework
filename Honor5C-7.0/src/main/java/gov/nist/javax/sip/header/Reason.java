package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.Utils;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.ReasonHeader;

public class Reason extends ParametersHeader implements ReasonHeader {
    private static final long serialVersionUID = -8903376965568297388L;
    public final String CAUSE;
    public final String TEXT;
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
        if (text.charAt(0) != '\"') {
            text = Utils.getQuotedString(text);
        }
        this.parameters.set(ParameterNames.TEXT, text);
    }

    public String getText() {
        return this.parameters.getParameter(ParameterNames.TEXT);
    }

    public Reason() {
        super(ReasonHeader.NAME);
        this.TEXT = ParameterNames.TEXT;
        this.CAUSE = ParameterNames.CAUSE;
    }

    public String getName() {
        return ReasonHeader.NAME;
    }

    protected String encodeBody() {
        StringBuffer s = new StringBuffer();
        s.append(this.protocol);
        if (!(this.parameters == null || this.parameters.isEmpty())) {
            s.append(Separators.SEMICOLON).append(this.parameters.encode());
        }
        return s.toString();
    }
}
