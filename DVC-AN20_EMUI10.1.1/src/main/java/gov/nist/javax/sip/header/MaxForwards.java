package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.TooManyHopsException;

public class MaxForwards extends SIPHeader implements MaxForwardsHeader {
    private static final long serialVersionUID = -3096874323347175943L;
    protected int maxForwards;

    public MaxForwards() {
        super("Max-Forwards");
    }

    public MaxForwards(int m) throws InvalidArgumentException {
        super("Max-Forwards");
        setMaxForwards(m);
    }

    @Override // javax.sip.header.MaxForwardsHeader
    public int getMaxForwards() {
        return this.maxForwards;
    }

    @Override // javax.sip.header.MaxForwardsHeader
    public void setMaxForwards(int maxForwards2) throws InvalidArgumentException {
        if (maxForwards2 < 0 || maxForwards2 > 255) {
            throw new InvalidArgumentException("bad max forwards value " + maxForwards2);
        }
        this.maxForwards = maxForwards2;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        buffer.append(this.maxForwards);
        return buffer;
    }

    @Override // javax.sip.header.MaxForwardsHeader
    public boolean hasReachedZero() {
        return this.maxForwards == 0;
    }

    @Override // javax.sip.header.MaxForwardsHeader
    public void decrementMaxForwards() throws TooManyHopsException {
        int i = this.maxForwards;
        if (i > 0) {
            this.maxForwards = i - 1;
            return;
        }
        throw new TooManyHopsException("has already reached 0!");
    }

    @Override // gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header, gov.nist.core.GenericObject
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MaxForwardsHeader)) {
            return false;
        }
        if (getMaxForwards() == ((MaxForwardsHeader) other).getMaxForwards()) {
            return true;
        }
        return false;
    }
}
