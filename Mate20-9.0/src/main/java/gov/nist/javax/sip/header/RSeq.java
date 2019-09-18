package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.RSeqHeader;

public class RSeq extends SIPHeader implements RSeqHeader {
    private static final long serialVersionUID = 8765762413224043394L;
    protected long sequenceNumber;

    public RSeq() {
        super("RSeq");
    }

    public int getSequenceNumber() {
        return (int) this.sequenceNumber;
    }

    /* access modifiers changed from: protected */
    public String encodeBody() {
        return Long.toString(this.sequenceNumber);
    }

    public long getSeqNumber() {
        return this.sequenceNumber;
    }

    public void setSeqNumber(long sequenceNumber2) throws InvalidArgumentException {
        if (sequenceNumber2 <= 0 || sequenceNumber2 > 2147483648L) {
            throw new InvalidArgumentException("Bad seq number " + sequenceNumber2);
        }
        this.sequenceNumber = sequenceNumber2;
    }

    public void setSequenceNumber(int sequenceNumber2) throws InvalidArgumentException {
        setSeqNumber((long) sequenceNumber2);
    }
}
