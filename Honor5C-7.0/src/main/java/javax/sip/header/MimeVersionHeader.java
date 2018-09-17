package javax.sip.header;

import javax.sip.InvalidArgumentException;

public interface MimeVersionHeader extends Header {
    public static final String NAME = "MIME-Version";

    int getMajorVersion();

    int getMinorVersion();

    void setMajorVersion(int i) throws InvalidArgumentException;

    void setMinorVersion(int i) throws InvalidArgumentException;
}
