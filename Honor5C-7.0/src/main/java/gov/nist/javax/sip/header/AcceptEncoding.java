package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ims.ParameterNamesIms;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.AcceptEncodingHeader;

public final class AcceptEncoding extends ParametersHeader implements AcceptEncodingHeader {
    private static final long serialVersionUID = -1476807565552873525L;
    protected String contentCoding;

    public AcceptEncoding() {
        super(AcceptEncodingHeader.NAME);
    }

    protected String encodeBody() {
        return encode(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        if (this.contentCoding != null) {
            buffer.append(this.contentCoding);
        }
        if (!(this.parameters == null || this.parameters.isEmpty())) {
            buffer.append(Separators.SEMICOLON).append(this.parameters.encode());
        }
        return buffer;
    }

    public float getQValue() {
        return getParameterAsFloat(ParameterNamesIms.Q);
    }

    public String getEncoding() {
        return this.contentCoding;
    }

    public void setQValue(float q) throws InvalidArgumentException {
        if (((double) q) < 0.0d || ((double) q) > 1.0d) {
            throw new InvalidArgumentException("qvalue out of range!");
        }
        super.setParameter(ParameterNamesIms.Q, q);
    }

    public void setEncoding(String encoding) throws ParseException {
        if (encoding == null) {
            throw new NullPointerException(" encoding parameter is null");
        }
        this.contentCoding = encoding;
    }
}
