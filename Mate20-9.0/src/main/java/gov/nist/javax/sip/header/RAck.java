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

    /* access modifiers changed from: protected */
    public String encodeBody() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.rSeqNumber);
        stringBuffer.append(Separators.SP);
        stringBuffer.append(this.cSeqNumber);
        stringBuffer.append(Separators.SP);
        stringBuffer.append(this.method);
        return stringBuffer.toString();
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

    public void setCSeqNumber(int cSeqNumber2) throws InvalidArgumentException {
        setCSequenceNumber((long) cSeqNumber2);
    }

    public void setMethod(String method2) throws ParseException {
        this.method = method2;
    }

    public long getCSequenceNumber() {
        return this.cSeqNumber;
    }

    public long getRSequenceNumber() {
        return this.rSeqNumber;
    }

    public void setCSequenceNumber(long cSeqNumber2) throws InvalidArgumentException {
        if (cSeqNumber2 <= 0 || cSeqNumber2 > 2147483648L) {
            throw new InvalidArgumentException("Bad CSeq # " + cSeqNumber2);
        }
        this.cSeqNumber = cSeqNumber2;
    }

    public void setRSeqNumber(int rSeqNumber2) throws InvalidArgumentException {
        setRSequenceNumber((long) rSeqNumber2);
    }

    public void setRSequenceNumber(long rSeqNumber2) throws InvalidArgumentException {
        if (rSeqNumber2 <= 0 || this.cSeqNumber > 2147483648L) {
            throw new InvalidArgumentException("Bad rSeq # " + rSeqNumber2);
        }
        this.rSeqNumber = rSeqNumber2;
    }
}
