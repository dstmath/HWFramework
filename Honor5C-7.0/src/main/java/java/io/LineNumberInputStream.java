package java.io;

import java.lang.reflect.Modifier;
import java.util.Calendar;
import sun.util.calendar.BaseCalendar;

@Deprecated
public class LineNumberInputStream extends FilterInputStream {
    int lineNumber;
    int markLineNumber;
    int markPushBack;
    int pushBack;

    public LineNumberInputStream(InputStream in) {
        super(in);
        this.pushBack = -1;
        this.markPushBack = -1;
    }

    public int read() throws IOException {
        int c = this.pushBack;
        if (c != -1) {
            this.pushBack = -1;
        } else {
            c = this.in.read();
        }
        switch (c) {
            case BaseCalendar.OCTOBER /*10*/:
                break;
            case Calendar.SECOND /*13*/:
                this.pushBack = this.in.read();
                if (this.pushBack == 10) {
                    this.pushBack = -1;
                    break;
                }
                break;
            default:
                return c;
        }
        this.lineNumber++;
        return 10;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int c = read();
            if (c == -1) {
                return -1;
            }
            b[off] = (byte) c;
            int i = 1;
            while (i < len) {
                try {
                    c = read();
                    if (c == -1) {
                        break;
                    }
                    if (b != null) {
                        b[off + i] = (byte) c;
                    }
                    i++;
                } catch (IOException e) {
                }
            }
            return i;
        }
    }

    public long skip(long n) throws IOException {
        long remaining = n;
        if (n <= 0) {
            return 0;
        }
        byte[] data = new byte[Modifier.STRICT];
        while (remaining > 0) {
            int nr = read(data, 0, (int) Math.min(2048, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= (long) nr;
        }
        return n - remaining;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int available() throws IOException {
        return this.pushBack == -1 ? super.available() / 2 : (super.available() / 2) + 1;
    }

    public void mark(int readlimit) {
        this.markLineNumber = this.lineNumber;
        this.markPushBack = this.pushBack;
        this.in.mark(readlimit);
    }

    public void reset() throws IOException {
        this.lineNumber = this.markLineNumber;
        this.pushBack = this.markPushBack;
        this.in.reset();
    }
}
