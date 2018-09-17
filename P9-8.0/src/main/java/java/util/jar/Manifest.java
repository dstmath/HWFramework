package java.util.jar;

import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Manifest implements Cloneable {
    private Attributes attr = new Attributes();
    private Map<String, Attributes> entries = new HashMap();

    static class FastInputStream extends FilterInputStream {
        private byte[] buf;
        private int count;
        private int pos;

        FastInputStream(InputStream in) {
            this(in, 8192);
        }

        FastInputStream(InputStream in, int size) {
            super(in);
            this.count = 0;
            this.pos = 0;
            this.buf = new byte[size];
        }

        public int read() throws IOException {
            if (this.pos >= this.count) {
                fill();
                if (this.pos >= this.count) {
                    return -1;
                }
            }
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            return Byte.toUnsignedInt(bArr[i]);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int avail = this.count - this.pos;
            if (avail <= 0) {
                if (len >= this.buf.length) {
                    return this.in.read(b, off, len);
                }
                fill();
                avail = this.count - this.pos;
                if (avail <= 0) {
                    return -1;
                }
            }
            if (len > avail) {
                len = avail;
            }
            System.arraycopy(this.buf, this.pos, b, off, len);
            this.pos += len;
            return len;
        }

        public int readLine(byte[] b, int off, int len) throws IOException {
            byte[] tbuf = this.buf;
            int total = 0;
            while (total < len) {
                int avail = this.count - this.pos;
                if (avail <= 0) {
                    fill();
                    avail = this.count - this.pos;
                    if (avail <= 0) {
                        return -1;
                    }
                }
                int n = len - total;
                if (n > avail) {
                    n = avail;
                }
                int tpos = this.pos;
                int maxpos = tpos + n;
                int tpos2 = tpos;
                while (tpos2 < maxpos) {
                    tpos = tpos2 + 1;
                    if (tbuf[tpos2] == (byte) 10) {
                        break;
                    }
                    tpos2 = tpos;
                }
                tpos = tpos2;
                n = tpos - this.pos;
                System.arraycopy(tbuf, this.pos, b, off, n);
                off += n;
                total += n;
                this.pos = tpos;
                if (tbuf[tpos - 1] == (byte) 10) {
                    break;
                }
            }
            return total;
        }

        public byte peek() throws IOException {
            if (this.pos == this.count) {
                fill();
            }
            if (this.pos == this.count) {
                return (byte) -1;
            }
            return this.buf[this.pos];
        }

        public int readLine(byte[] b) throws IOException {
            return readLine(b, 0, b.length);
        }

        public long skip(long n) throws IOException {
            if (n <= 0) {
                return 0;
            }
            long avail = (long) (this.count - this.pos);
            if (avail <= 0) {
                return this.in.skip(n);
            }
            if (n > avail) {
                n = avail;
            }
            this.pos = (int) (((long) this.pos) + n);
            return n;
        }

        public int available() throws IOException {
            return (this.count - this.pos) + this.in.available();
        }

        public void close() throws IOException {
            if (this.in != null) {
                this.in.close();
                this.in = null;
                this.buf = null;
            }
        }

        private void fill() throws IOException {
            this.pos = 0;
            this.count = 0;
            int n = this.in.read(this.buf, 0, this.buf.length);
            if (n > 0) {
                this.count = n;
            }
        }
    }

    public Manifest(InputStream is) throws IOException {
        read(is);
    }

    public Manifest(Manifest man) {
        this.attr.putAll(man.getMainAttributes());
        this.entries.putAll(man.getEntries());
    }

    public Attributes getMainAttributes() {
        return this.attr;
    }

    public Map<String, Attributes> getEntries() {
        return this.entries;
    }

    public Attributes getAttributes(String name) {
        return (Attributes) getEntries().get(name);
    }

    public void clear() {
        this.attr.clear();
        this.entries.clear();
    }

    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        this.attr.writeMain(dos);
        for (Entry<String, Attributes> e : this.entries.entrySet()) {
            StringBuffer buffer = new StringBuffer("Name: ");
            String value = (String) e.getKey();
            if (value != null) {
                byte[] vb = value.getBytes("UTF8");
                value = new String(vb, 0, 0, vb.length);
            }
            buffer.append(value);
            buffer.append("\r\n");
            make72Safe(buffer);
            dos.writeBytes(buffer.toString());
            ((Attributes) e.getValue()).write(dos);
        }
        dos.flush();
    }

    static void make72Safe(StringBuffer line) {
        int length = line.length();
        if (length > 72) {
            int index = 70;
            while (index < length - 2) {
                line.insert(index, "\r\n ");
                index += 72;
                length += 3;
            }
        }
    }

    public void read(InputStream is) throws IOException {
        FastInputStream fis = new FastInputStream(is);
        byte[] lbuf = new byte[512];
        this.attr.read(fis, lbuf);
        int ecount = 0;
        int acount = 0;
        int asize = 2;
        String name = null;
        boolean skipEmptyLines = true;
        byte[] lastline = null;
        while (true) {
            int len = fis.readLine(lbuf);
            if (len != -1) {
                len--;
                if (lbuf[len] != (byte) 10) {
                    throw new IOException("manifest line too long");
                }
                if (len > 0 && lbuf[len - 1] == (byte) 13) {
                    len--;
                }
                if (len != 0 || !skipEmptyLines) {
                    skipEmptyLines = false;
                    if (name == null) {
                        name = parseName(lbuf, len);
                        if (name == null) {
                            throw new IOException("invalid manifest format");
                        } else if (fis.peek() == (byte) 32) {
                            lastline = new byte[(len - 6)];
                            System.arraycopy(lbuf, 6, lastline, 0, len - 6);
                        }
                    } else {
                        byte[] buf = new byte[((lastline.length + len) - 1)];
                        System.arraycopy(lastline, 0, buf, 0, lastline.length);
                        System.arraycopy(lbuf, 1, buf, lastline.length, len - 1);
                        if (fis.peek() == (byte) 32) {
                            lastline = buf;
                        } else {
                            name = new String(buf, 0, buf.length, "UTF8");
                            lastline = null;
                        }
                    }
                    Attributes attr = getAttributes(name);
                    if (attr == null) {
                        attr = new Attributes(asize);
                        this.entries.put(name, attr);
                    }
                    attr.read(fis, lbuf);
                    ecount++;
                    acount += attr.size();
                    asize = Math.max(2, acount / ecount);
                    name = null;
                    skipEmptyLines = true;
                }
            } else {
                return;
            }
        }
    }

    private String parseName(byte[] lbuf, int len) {
        if (toLower(lbuf[0]) == 110 && toLower(lbuf[1]) == 97 && toLower(lbuf[2]) == 109 && toLower(lbuf[3]) == 101 && lbuf[4] == (byte) 58 && lbuf[5] == (byte) 32) {
            try {
                return new String(lbuf, 6, len - 6, "UTF8");
            } catch (Exception e) {
            }
        }
        return null;
    }

    private int toLower(int c) {
        return (c < 65 || c > 90) ? c : (c - 65) + 97;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof Manifest)) {
            return false;
        }
        Manifest m = (Manifest) o;
        if (this.attr.equals(m.getMainAttributes())) {
            z = this.entries.equals(m.getEntries());
        }
        return z;
    }

    public int hashCode() {
        return this.attr.hashCode() + this.entries.hashCode();
    }

    public Object clone() {
        return new Manifest(this);
    }
}
