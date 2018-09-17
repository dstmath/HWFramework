package sun.net.ftp;

import java.io.IOException;

public class FtpLoginException extends IOException {
    private static final long serialVersionUID = 2218162403237941536L;

    public FtpLoginException(String s) {
        super(s);
    }
}
