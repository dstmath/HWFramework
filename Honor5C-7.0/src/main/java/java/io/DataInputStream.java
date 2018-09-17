package java.io;

import java.util.Calendar;
import java.util.regex.Pattern;
import sun.misc.FloatConsts;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

public class DataInputStream extends FilterInputStream implements DataInput {
    private byte[] bytearr;
    private char[] chararr;
    private char[] lineBuffer;
    private byte[] readBuffer;

    public DataInputStream(InputStream in) {
        super(in);
        this.bytearr = new byte[80];
        this.chararr = new char[80];
        this.readBuffer = new byte[8];
    }

    public final int read(byte[] b) throws IOException {
        return this.in.read(b, 0, b.length);
    }

    public final int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    public final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = this.in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
    }

    public final int skipBytes(int n) throws IOException {
        int total = 0;
        while (total < n) {
            int cur = (int) this.in.skip((long) (n - total));
            if (cur <= 0) {
                break;
            }
            total += cur;
        }
        return total;
    }

    public final boolean readBoolean() throws IOException {
        int ch = this.in.read();
        if (ch < 0) {
            throw new EOFException();
        } else if (ch != 0) {
            return true;
        } else {
            return false;
        }
    }

    public final byte readByte() throws IOException {
        int ch = this.in.read();
        if (ch >= 0) {
            return (byte) ch;
        }
        throw new EOFException();
    }

    public final int readUnsignedByte() throws IOException {
        int ch = this.in.read();
        if (ch >= 0) {
            return ch;
        }
        throw new EOFException();
    }

    public final short readShort() throws IOException {
        int ch1 = this.in.read();
        int ch2 = this.in.read();
        if ((ch1 | ch2) >= 0) {
            return (short) ((ch1 << 8) + (ch2 << 0));
        }
        throw new EOFException();
    }

    public final int readUnsignedShort() throws IOException {
        int ch1 = this.in.read();
        int ch2 = this.in.read();
        if ((ch1 | ch2) >= 0) {
            return (ch1 << 8) + (ch2 << 0);
        }
        throw new EOFException();
    }

    public final char readChar() throws IOException {
        int ch1 = this.in.read();
        int ch2 = this.in.read();
        if ((ch1 | ch2) >= 0) {
            return (char) ((ch1 << 8) + (ch2 << 0));
        }
        throw new EOFException();
    }

    public final int readInt() throws IOException {
        int ch1 = this.in.read();
        int ch2 = this.in.read();
        int ch3 = this.in.read();
        int ch4 = this.in.read();
        if ((((ch1 | ch2) | ch3) | ch4) >= 0) {
            return (((ch1 << 24) + (ch2 << 16)) + (ch3 << 8)) + (ch4 << 0);
        }
        throw new EOFException();
    }

    public final long readLong() throws IOException {
        readFully(this.readBuffer, 0, 8);
        return (((((((((long) this.readBuffer[0]) << 56) + (((long) (this.readBuffer[1] & 255)) << 48)) + (((long) (this.readBuffer[2] & 255)) << 40)) + (((long) (this.readBuffer[3] & 255)) << 32)) + (((long) (this.readBuffer[4] & 255)) << 24)) + ((long) ((this.readBuffer[5] & 255) << 16))) + ((long) ((this.readBuffer[6] & 255) << 8))) + ((long) ((this.readBuffer[7] & 255) << 0));
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Deprecated
    public final String readLine() throws IOException {
        char[] buf = this.lineBuffer;
        if (buf == null) {
            buf = new char[Pattern.CANON_EQ];
            this.lineBuffer = buf;
        }
        int room = buf.length;
        int offset = 0;
        while (true) {
            int c = this.in.read();
            switch (c) {
                case GeneralNameInterface.NAME_DIFF_TYPE /*-1*/:
                case BaseCalendar.OCTOBER /*10*/:
                    break;
                case Calendar.SECOND /*13*/:
                    int c2 = this.in.read();
                    if (!(c2 == 10 || c2 == -1)) {
                        if (!(this.in instanceof PushbackInputStream)) {
                            this.in = new PushbackInputStream(this.in);
                        }
                        ((PushbackInputStream) this.in).unread(c2);
                        break;
                    }
                default:
                    room--;
                    if (room < 0) {
                        buf = new char[(offset + Pattern.CANON_EQ)];
                        room = (buf.length - offset) - 1;
                        System.arraycopy(this.lineBuffer, 0, buf, 0, offset);
                        this.lineBuffer = buf;
                    }
                    int offset2 = offset + 1;
                    buf[offset] = (char) c;
                    offset = offset2;
            }
            if (c == -1 && offset == 0) {
                return null;
            }
            return String.copyValueOf(buf, 0, offset);
        }
    }

    public final String readUTF() throws IOException {
        return readUTF(this);
    }

    public static final String readUTF(DataInput in) throws IOException {
        char[] chararr;
        byte[] bytearr;
        int utflen = in.readUnsignedShort();
        if (in instanceof DataInputStream) {
            DataInputStream dis = (DataInputStream) in;
            if (dis.bytearr.length < utflen) {
                dis.bytearr = new byte[(utflen * 2)];
                dis.chararr = new char[(utflen * 2)];
            }
            chararr = dis.chararr;
            bytearr = dis.bytearr;
        } else {
            bytearr = new byte[utflen];
            chararr = new char[utflen];
        }
        int count = 0;
        in.readFully(bytearr, 0, utflen);
        int chararr_count = 0;
        while (count < utflen) {
            int c = bytearr[count] & 255;
            if (c > FloatConsts.MAX_EXPONENT) {
                break;
            }
            count++;
            int chararr_count2 = chararr_count + 1;
            chararr[chararr_count] = (char) c;
            chararr_count = chararr_count2;
        }
        while (count < utflen) {
            c = bytearr[count] & 255;
            int char2;
            switch (c >> 4) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                case BaseCalendar.SUNDAY /*1*/:
                case BaseCalendar.MONDAY /*2*/:
                case BaseCalendar.TUESDAY /*3*/:
                case BaseCalendar.WEDNESDAY /*4*/:
                case BaseCalendar.THURSDAY /*5*/:
                case BaseCalendar.JUNE /*6*/:
                case BaseCalendar.SATURDAY /*7*/:
                    count++;
                    chararr_count2 = chararr_count + 1;
                    chararr[chararr_count] = (char) c;
                    break;
                case BaseCalendar.DECEMBER /*12*/:
                case Calendar.SECOND /*13*/:
                    count += 2;
                    if (count <= utflen) {
                        char2 = bytearr[count - 1];
                        if ((char2 & 192) == Pattern.CANON_EQ) {
                            chararr_count2 = chararr_count + 1;
                            chararr[chararr_count] = (char) (((c & 31) << 6) | (char2 & 63));
                            break;
                        }
                        throw new UTFDataFormatException("malformed input around byte " + count);
                    }
                    throw new UTFDataFormatException("malformed input: partial character at end");
                case ZipConstants.LOCCRC /*14*/:
                    count += 3;
                    if (count <= utflen) {
                        char2 = bytearr[count - 2];
                        int char3 = bytearr[count - 1];
                        if ((char2 & 192) == Pattern.CANON_EQ && (char3 & 192) == Pattern.CANON_EQ) {
                            chararr_count2 = chararr_count + 1;
                            chararr[chararr_count] = (char) ((((c & 15) << 12) | ((char2 & 63) << 6)) | ((char3 & 63) << 0));
                            break;
                        }
                        throw new UTFDataFormatException("malformed input around byte " + (count - 1));
                    }
                    throw new UTFDataFormatException("malformed input: partial character at end");
                    break;
                default:
                    throw new UTFDataFormatException("malformed input around byte " + count);
            }
            chararr_count = chararr_count2;
        }
        return new String(chararr, 0, chararr_count);
    }
}
