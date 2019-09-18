package java.io;

import java.nio.ByteOrder;
import libcore.io.Memory;
import sun.security.util.DerValue;

public class DataInputStream extends FilterInputStream implements DataInput {
    private byte[] bytearr = new byte[80];
    private char[] chararr = new char[80];
    private char[] lineBuffer;
    private byte[] readBuffer = new byte[8];

    public DataInputStream(InputStream in) {
        super(in);
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
        if (len >= 0) {
            int n = 0;
            while (n < len) {
                int count = this.in.read(b, off + n, len - n);
                if (count >= 0) {
                    n += count;
                } else {
                    throw new EOFException();
                }
            }
            return;
        }
        throw new IndexOutOfBoundsException();
    }

    public final int skipBytes(int n) throws IOException {
        int total = 0;
        while (total < n) {
            int skip = (int) this.in.skip((long) (n - total));
            int cur = skip;
            if (skip <= 0) {
                break;
            }
            total += cur;
        }
        return total;
    }

    public final boolean readBoolean() throws IOException {
        int ch = this.in.read();
        if (ch >= 0) {
            return ch != 0;
        }
        throw new EOFException();
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
        readFully(this.readBuffer, 0, 2);
        return Memory.peekShort(this.readBuffer, 0, ByteOrder.BIG_ENDIAN);
    }

    public final int readUnsignedShort() throws IOException {
        readFully(this.readBuffer, 0, 2);
        return Memory.peekShort(this.readBuffer, 0, ByteOrder.BIG_ENDIAN) & 65535;
    }

    public final char readChar() throws IOException {
        readFully(this.readBuffer, 0, 2);
        return (char) Memory.peekShort(this.readBuffer, 0, ByteOrder.BIG_ENDIAN);
    }

    public final int readInt() throws IOException {
        readFully(this.readBuffer, 0, 4);
        return Memory.peekInt(this.readBuffer, 0, ByteOrder.BIG_ENDIAN);
    }

    public final long readLong() throws IOException {
        readFully(this.readBuffer, 0, 8);
        return (((long) this.readBuffer[0]) << 56) + (((long) (this.readBuffer[1] & Character.DIRECTIONALITY_UNDEFINED)) << 48) + (((long) (this.readBuffer[2] & Character.DIRECTIONALITY_UNDEFINED)) << 40) + (((long) (this.readBuffer[3] & Character.DIRECTIONALITY_UNDEFINED)) << 32) + (((long) (this.readBuffer[4] & Character.DIRECTIONALITY_UNDEFINED)) << 24) + ((long) ((this.readBuffer[5] & Character.DIRECTIONALITY_UNDEFINED) << 16)) + ((long) ((this.readBuffer[6] & Character.DIRECTIONALITY_UNDEFINED) << 8)) + ((long) ((this.readBuffer[7] & Character.DIRECTIONALITY_UNDEFINED) << 0));
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Deprecated
    public final String readLine() throws IOException {
        int c;
        char[] buf = this.lineBuffer;
        if (buf == null) {
            char[] cArr = new char[128];
            this.lineBuffer = cArr;
            buf = cArr;
        }
        int room = buf.length;
        char[] buf2 = buf;
        int offset = 0;
        while (true) {
            int read = this.in.read();
            c = read;
            if (read == -1 || read == 10) {
                break;
            } else if (read != 13) {
                room--;
                if (room < 0) {
                    buf2 = new char[(offset + 128)];
                    System.arraycopy((Object) this.lineBuffer, 0, (Object) buf2, 0, offset);
                    this.lineBuffer = buf2;
                    room = (buf2.length - offset) - 1;
                }
                buf2[offset] = (char) c;
                offset++;
            } else {
                int c2 = this.in.read();
                if (c2 != 10 && c2 != -1) {
                    if (!(this.in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(this.in);
                    }
                    ((PushbackInputStream) this.in).unread(c2);
                }
            }
        }
        if (c == -1 && offset == 0) {
            return null;
        }
        return String.copyValueOf(buf2, 0, offset);
    }

    public final String readUTF() throws IOException {
        return readUTF(this);
    }

    public static final String readUTF(DataInput in) throws IOException {
        char[] chararr2;
        byte[] bytearr2;
        int chararr_count;
        int count;
        int chararr_count2;
        int utflen = in.readUnsignedShort();
        if (in instanceof DataInputStream) {
            DataInputStream dis = (DataInputStream) in;
            if (dis.bytearr.length < utflen) {
                dis.bytearr = new byte[(utflen * 2)];
                dis.chararr = new char[(utflen * 2)];
            }
            chararr2 = dis.chararr;
            bytearr2 = dis.bytearr;
        } else {
            bytearr2 = new byte[utflen];
            chararr2 = new char[utflen];
        }
        int count2 = 0;
        int chararr_count3 = 0;
        in.readFully(bytearr2, 0, utflen);
        while (count < utflen) {
            int c = bytearr2[count] & 255;
            if (c > 127) {
                break;
            }
            count2 = count + 1;
            chararr2[chararr_count] = (char) c;
            chararr_count3 = chararr_count + 1;
        }
        while (count < utflen) {
            int c2 = bytearr2[count] & 255;
            int i = c2 >> 4;
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    count++;
                    chararr2[chararr_count] = (char) c2;
                    chararr_count++;
                    break;
                default:
                    switch (i) {
                        case 12:
                        case 13:
                            count += 2;
                            if (count <= utflen) {
                                byte char2 = bytearr2[count - 1];
                                if ((char2 & DerValue.TAG_PRIVATE) == 128) {
                                    chararr_count2 = chararr_count + 1;
                                    chararr2[chararr_count] = (char) (((c2 & 31) << 6) | (char2 & 63));
                                    break;
                                } else {
                                    throw new UTFDataFormatException("malformed input around byte " + count);
                                }
                            } else {
                                throw new UTFDataFormatException("malformed input: partial character at end");
                            }
                        case 14:
                            count += 3;
                            if (count <= utflen) {
                                byte char22 = bytearr2[count - 2];
                                byte char3 = bytearr2[count - 1];
                                if ((char22 & DerValue.TAG_PRIVATE) == 128 && (char3 & DerValue.TAG_PRIVATE) == 128) {
                                    chararr_count2 = chararr_count + 1;
                                    chararr2[chararr_count] = (char) (((c2 & 15) << 12) | ((char22 & 63) << 6) | ((char3 & 63) << 0));
                                    break;
                                } else {
                                    throw new UTFDataFormatException("malformed input around byte " + (count - 1));
                                }
                            } else {
                                throw new UTFDataFormatException("malformed input: partial character at end");
                            }
                            break;
                        default:
                            throw new UTFDataFormatException("malformed input around byte " + count);
                    }
                    chararr_count = chararr_count2;
                    break;
            }
        }
        return new String(chararr2, 0, chararr_count);
    }
}
