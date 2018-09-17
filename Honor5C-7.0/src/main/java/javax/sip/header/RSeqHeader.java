package javax.sip.header;

import javax.sip.InvalidArgumentException;

public interface RSeqHeader extends Header {
    public static final String NAME = "RSeq";

    long getSeqNumber();

    int getSequenceNumber();

    void setSeqNumber(long j) throws InvalidArgumentException;

    void setSequenceNumber(int i) throws InvalidArgumentException;
}
