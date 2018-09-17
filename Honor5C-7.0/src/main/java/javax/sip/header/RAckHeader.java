package javax.sip.header;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public interface RAckHeader extends Header {
    public static final String NAME = "RAck";

    int getCSeqNumber();

    long getCSequenceNumber();

    String getMethod();

    int getRSeqNumber();

    long getRSequenceNumber();

    void setCSeqNumber(int i) throws InvalidArgumentException;

    void setCSequenceNumber(long j) throws InvalidArgumentException;

    void setMethod(String str) throws ParseException;

    void setRSeqNumber(int i) throws InvalidArgumentException;

    void setRSequenceNumber(long j) throws InvalidArgumentException;
}
