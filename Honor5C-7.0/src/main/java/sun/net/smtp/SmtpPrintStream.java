package sun.net.smtp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/* compiled from: SmtpClient */
class SmtpPrintStream extends PrintStream {
    private int lastc;
    private SmtpClient target;

    SmtpPrintStream(OutputStream fos, SmtpClient cl) throws UnsupportedEncodingException {
        super(fos, false, cl.getEncoding());
        this.lastc = 10;
        this.target = cl;
    }

    public void close() {
        if (this.target != null) {
            if (this.lastc != 10) {
                write(10);
            }
            try {
                this.target.issueCommand(".\r\n", 250);
                this.target.message = null;
                this.out = null;
                this.target = null;
            } catch (IOException e) {
            }
        }
    }

    public void write(int b) {
        try {
            if (this.lastc == 10 && b == 46) {
                this.out.write(46);
            }
            if (b == 10 && this.lastc != 13) {
                this.out.write(13);
            }
            this.out.write(b);
            this.lastc = b;
        } catch (IOException e) {
        }
    }

    public void write(byte[] b, int off, int len) {
        try {
            int lc = this.lastc;
            while (true) {
                len--;
                if (len >= 0) {
                    int off2 = off + 1;
                    try {
                        int c = b[off];
                        if (lc == 10 && c == 46) {
                            this.out.write(46);
                        }
                        if (c == 10 && lc != 13) {
                            this.out.write(13);
                        }
                        this.out.write(c);
                        lc = c;
                        off = off2;
                    } catch (IOException e) {
                        off = off2;
                        return;
                    }
                }
                this.lastc = lc;
                return;
            }
        } catch (IOException e2) {
        }
    }

    public void print(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            write(s.charAt(i));
        }
    }
}
