package java.io;

public class DataOutputStream extends FilterOutputStream implements DataOutput {
    private byte[] bytearr = null;
    private byte[] writeBuffer = new byte[8];
    protected int written;

    public DataOutputStream(OutputStream out) {
        super(out);
    }

    private void incCount(int value) {
        int temp = this.written + value;
        if (temp < 0) {
            temp = Integer.MAX_VALUE;
        }
        this.written = temp;
    }

    public synchronized void write(int b) throws IOException {
        this.out.write(b);
        incCount(1);
    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        incCount(len);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public final void writeBoolean(boolean v) throws IOException {
        this.out.write((int) v);
        incCount(1);
    }

    public final void writeByte(int v) throws IOException {
        this.out.write(v);
        incCount(1);
    }

    public final void writeShort(int v) throws IOException {
        this.out.write((v >>> 8) & 255);
        this.out.write((v >>> 0) & 255);
        incCount(2);
    }

    public final void writeChar(int v) throws IOException {
        this.out.write((v >>> 8) & 255);
        this.out.write((v >>> 0) & 255);
        incCount(2);
    }

    public final void writeInt(int v) throws IOException {
        this.out.write((v >>> 24) & 255);
        this.out.write((v >>> 16) & 255);
        this.out.write((v >>> 8) & 255);
        this.out.write((v >>> 0) & 255);
        incCount(4);
    }

    public final void writeLong(long v) throws IOException {
        this.writeBuffer[0] = (byte) ((int) (v >>> 56));
        this.writeBuffer[1] = (byte) ((int) (v >>> 48));
        this.writeBuffer[2] = (byte) ((int) (v >>> 40));
        this.writeBuffer[3] = (byte) ((int) (v >>> 32));
        this.writeBuffer[4] = (byte) ((int) (v >>> 24));
        this.writeBuffer[5] = (byte) ((int) (v >>> 16));
        this.writeBuffer[6] = (byte) ((int) (v >>> 8));
        this.writeBuffer[7] = (byte) ((int) (v >>> 0));
        this.out.write(this.writeBuffer, 0, 8);
        incCount(8);
    }

    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public final void writeBytes(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            this.out.write((int) (byte) s.charAt(i));
        }
        incCount(len);
    }

    public final void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int v = s.charAt(i);
            this.out.write((v >>> 8) & 255);
            this.out.write((v >>> 0) & 255);
        }
        incCount(len * 2);
    }

    public final void writeUTF(String str) throws IOException {
        writeUTF(str, this);
    }

    static int writeUTF(String str, DataOutput out) throws IOException {
        byte[] bytearr2;
        int count;
        int strlen = str.length();
        int utflen = 0;
        for (int i = 0; i < strlen; i++) {
            int c = str.charAt(i);
            if (c >= 1 && c <= 127) {
                utflen++;
            } else if (c > 2047) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        if (utflen <= 65535) {
            if (out instanceof DataOutputStream) {
                DataOutputStream dos = (DataOutputStream) out;
                if (dos.bytearr == null || dos.bytearr.length < utflen + 2) {
                    dos.bytearr = new byte[((utflen * 2) + 2)];
                }
                bytearr2 = dos.bytearr;
            } else {
                bytearr2 = new byte[(utflen + 2)];
            }
            int count2 = 0 + 1;
            bytearr2[0] = (byte) ((utflen >>> 8) & 255);
            int count3 = count2 + 1;
            bytearr2[count2] = (byte) ((utflen >>> 0) & 255);
            int i2 = 0;
            while (i2 < strlen) {
                int c2 = str.charAt(i2);
                if (c2 < 1 || c2 > 127) {
                    break;
                }
                bytearr2[count] = (byte) c2;
                i2++;
                count3 = count + 1;
            }
            while (i2 < strlen) {
                int c3 = str.charAt(i2);
                if (c3 >= 1 && c3 <= 127) {
                    bytearr2[count] = (byte) c3;
                    count++;
                } else if (c3 > 2047) {
                    int count4 = count + 1;
                    bytearr2[count] = (byte) (224 | ((c3 >> 12) & 15));
                    int count5 = count4 + 1;
                    bytearr2[count4] = (byte) (((c3 >> 6) & 63) | 128);
                    bytearr2[count5] = (byte) (128 | ((c3 >> 0) & 63));
                    count = count5 + 1;
                } else {
                    int count6 = count + 1;
                    bytearr2[count] = (byte) (192 | ((c3 >> 6) & 31));
                    count = count6 + 1;
                    bytearr2[count6] = (byte) (128 | ((c3 >> 0) & 63));
                }
                i2++;
            }
            out.write(bytearr2, 0, utflen + 2);
            return utflen + 2;
        }
        throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
    }

    public final int size() {
        return this.written;
    }
}
