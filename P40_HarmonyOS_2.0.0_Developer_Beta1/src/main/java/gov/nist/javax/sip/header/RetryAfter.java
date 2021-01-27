package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.RetryAfterHeader;

public class RetryAfter extends ParametersHeader implements RetryAfterHeader {
    public static final String DURATION = "duration";
    private static final long serialVersionUID = -1029458515616146140L;
    protected String comment;
    protected Integer retryAfter = new Integer(0);

    public RetryAfter() {
        super("Retry-After");
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        StringBuffer s = new StringBuffer();
        Integer num = this.retryAfter;
        if (num != null) {
            s.append(num);
        }
        if (this.comment != null) {
            s.append(" (" + this.comment + Separators.RPAREN);
        }
        if (!this.parameters.isEmpty()) {
            s.append(Separators.SEMICOLON + this.parameters.encode());
        }
        return s.toString();
    }

    @Override // javax.sip.header.RetryAfterHeader
    public boolean hasComment() {
        return this.comment != null;
    }

    @Override // javax.sip.header.RetryAfterHeader
    public void removeComment() {
        this.comment = null;
    }

    @Override // javax.sip.header.RetryAfterHeader
    public void removeDuration() {
        super.removeParameter("duration");
    }

    @Override // javax.sip.header.RetryAfterHeader
    public void setRetryAfter(int retryAfter2) throws InvalidArgumentException {
        if (retryAfter2 >= 0) {
            this.retryAfter = Integer.valueOf(retryAfter2);
            return;
        }
        throw new InvalidArgumentException("invalid parameter " + retryAfter2);
    }

    @Override // javax.sip.header.RetryAfterHeader
    public int getRetryAfter() {
        return this.retryAfter.intValue();
    }

    @Override // javax.sip.header.RetryAfterHeader
    public String getComment() {
        return this.comment;
    }

    @Override // javax.sip.header.RetryAfterHeader
    public void setComment(String comment2) throws ParseException {
        if (comment2 != null) {
            this.comment = comment2;
            return;
        }
        throw new NullPointerException("the comment parameter is null");
    }

    @Override // javax.sip.header.RetryAfterHeader
    public void setDuration(int duration) throws InvalidArgumentException {
        if (duration >= 0) {
            setParameter("duration", duration);
            return;
        }
        throw new InvalidArgumentException("the duration parameter is <0");
    }

    @Override // javax.sip.header.RetryAfterHeader
    public int getDuration() {
        if (getParameter("duration") == null) {
            return -1;
        }
        return super.getParameterAsInt("duration");
    }
}
