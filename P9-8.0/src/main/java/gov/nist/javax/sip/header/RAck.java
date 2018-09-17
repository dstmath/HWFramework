package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.RAckHeader;

public class RAck extends SIPHeader implements RAckHeader {
    private static final long serialVersionUID = 743999286077404118L;
    protected long cSeqNumber;
    protected String method;
    protected long rSeqNumber;

    public RAck() {
        super("RAck");
    }

    protected String encodeBody() {
        return new StringBuffer().append(this.rSeqNumber).append(Separators.SP).append(this.cSeqNumber).append(Separators.SP).append(this.method).toString();
    }

    public int getCSeqNumber() {
        return (int) this.cSeqNumber;
    }

    public long getCSeqNumberLong() {
        return this.cSeqNumber;
    }

    public String getMethod() {
        return this.method;
    }

    public int getRSeqNumber() {
        return (int) this.rSeqNumber;
    }

    public void setCSeqNumber(int cSeqNumber) throws InvalidArgumentException {
        setCSequenceNumber((long) cSeqNumber);
    }

    public void setMethod(String method) throws ParseException {
        this.method = method;
    }

    public long getCSequenceNumber() {
        return this.cSeqNumber;
    }

    public long getRSequenceNumber() {
        return this.rSeqNumber;
    }

    public void setCSequenceNumber(long cSeqNumber) throws InvalidArgumentException {
        if (cSeqNumber <= 0 || cSeqNumber > 2147483648L) {
            throw new InvalidArgumentException("Bad CSeq # " + cSeqNumber);
        }
        this.cSeqNumber = cSeqNumber;
    }

    public void setRSeqNumber(int rSeqNumber) throws InvalidArgumentException {
        setRSequenceNumber((long) rSeqNumber);
    }

    public void setRSequenceNumber(long rSeqNumber) throws InvalidArgumentException {
        if (rSeqNumber <= 0 || this.cSeqNumber > 2147483648L) {
            throw new InvalidArgumentException("Bad rSeq # " + rSeqNumber);
        }
        this.rSeqNumber = rSeqNumber;
    }
}
