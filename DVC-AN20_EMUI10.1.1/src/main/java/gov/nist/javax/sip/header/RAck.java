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
    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.rSeqNumber);
        stringBuffer.append(Separators.SP);
        stringBuffer.append(this.cSeqNumber);
        stringBuffer.append(Separators.SP);
        stringBuffer.append(this.method);
        return stringBuffer.toString();
    }

    @Override // javax.sip.header.RAckHeader
    public int getCSeqNumber() {
        return (int) this.cSeqNumber;
    }

    public long getCSeqNumberLong() {
        return this.cSeqNumber;
    }

    @Override // javax.sip.header.RAckHeader
    public String getMethod() {
        return this.method;
    }

    @Override // javax.sip.header.RAckHeader
    public int getRSeqNumber() {
        return (int) this.rSeqNumber;
    }

    @Override // javax.sip.header.RAckHeader
    public void setCSeqNumber(int cSeqNumber2) throws InvalidArgumentException {
        setCSequenceNumber((long) cSeqNumber2);
    }

    @Override // javax.sip.header.RAckHeader
    public void setMethod(String method2) throws ParseException {
        this.method = method2;
    }

    @Override // javax.sip.header.RAckHeader
    public long getCSequenceNumber() {
        return this.cSeqNumber;
    }

    @Override // javax.sip.header.RAckHeader
    public long getRSequenceNumber() {
        return this.rSeqNumber;
    }

    @Override // javax.sip.header.RAckHeader
    public void setCSequenceNumber(long cSeqNumber2) throws InvalidArgumentException {
        if (cSeqNumber2 <= 0 || cSeqNumber2 > 2147483648L) {
            throw new InvalidArgumentException("Bad CSeq # " + cSeqNumber2);
        }
        this.cSeqNumber = cSeqNumber2;
    }

    @Override // javax.sip.header.RAckHeader
    public void setRSeqNumber(int rSeqNumber2) throws InvalidArgumentException {
        setRSequenceNumber((long) rSeqNumber2);
    }

    @Override // javax.sip.header.RAckHeader
    public void setRSequenceNumber(long rSeqNumber2) throws InvalidArgumentException {
        if (rSeqNumber2 <= 0 || this.cSeqNumber > 2147483648L) {
            throw new InvalidArgumentException("Bad rSeq # " + rSeqNumber2);
        }
        this.rSeqNumber = rSeqNumber2;
    }
}
