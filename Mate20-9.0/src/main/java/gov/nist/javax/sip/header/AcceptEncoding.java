package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.AcceptEncodingHeader;

public final class AcceptEncoding extends ParametersHeader implements AcceptEncodingHeader {
    private static final long serialVersionUID = -1476807565552873525L;
    protected String contentCoding;

    public AcceptEncoding() {
        super("Accept-Encoding");
    }

    /* access modifiers changed from: protected */
    public String encodeBody() {
        return encode(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuffer encodeBody(StringBuffer buffer) {
        if (this.contentCoding != null) {
            buffer.append(this.contentCoding);
        }
        if (this.parameters != null && !this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            buffer.append(this.parameters.encode());
        }
        return buffer;
    }

    public float getQValue() {
        return getParameterAsFloat("q");
    }

    public String getEncoding() {
        return this.contentCoding;
    }

    public void setQValue(float q) throws InvalidArgumentException {
        if (((double) q) < 0.0d || ((double) q) > 1.0d) {
            throw new InvalidArgumentException("qvalue out of range!");
        }
        super.setParameter("q", q);
    }

    public void setEncoding(String encoding) throws ParseException {
        if (encoding != null) {
            this.contentCoding = encoding;
            return;
        }
        throw new NullPointerException(" encoding parameter is null");
    }
}
