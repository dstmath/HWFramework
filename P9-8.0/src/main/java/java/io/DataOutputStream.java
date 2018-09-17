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
        this.out.write(v ? 1 : 0);
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
            this.out.write((byte) s.charAt(i));
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
        int i;
        int c;
        int strlen = str.length();
        int utflen = 0;
        for (i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if (c >= 1 && c <= 127) {
                utflen++;
            } else if (c > 2047) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        if (utflen > 65535) {
            throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
        }
        byte[] bytearr;
        int count;
        if (out instanceof DataOutputStream) {
            DataOutputStream dos = (DataOutputStream) out;
            if (dos.bytearr == null || dos.bytearr.length < utflen + 2) {
                dos.bytearr = new byte[((utflen * 2) + 2)];
            }
            bytearr = dos.bytearr;
        } else {
            bytearr = new byte[(utflen + 2)];
        }
        bytearr[0] = (byte) ((utflen >>> 8) & 255);
        int count2 = 1 + 1;
        bytearr[1] = (byte) ((utflen >>> 0) & 255);
        i = 0;
        while (i < strlen) {
            c = str.charAt(i);
            if (c < 1 || c > 127) {
                break;
            }
            count = count2 + 1;
            bytearr[count2] = (byte) c;
            i++;
            count2 = count;
        }
        while (i < strlen) {
            c = str.charAt(i);
            if (c >= 1 && c <= 127) {
                count = count2 + 1;
                bytearr[count2] = (byte) c;
            } else if (c > 2047) {
                count = count2 + 1;
                bytearr[count2] = (byte) (((c >> 12) & 15) | 224);
                count2 = count + 1;
                bytearr[count] = (byte) (((c >> 6) & 63) | 128);
                count = count2 + 1;
                bytearr[count2] = (byte) (((c >> 0) & 63) | 128);
            } else {
                count = count2 + 1;
                bytearr[count2] = (byte) (((c >> 6) & 31) | 192);
                count2 = count + 1;
                bytearr[count] = (byte) (((c >> 0) & 63) | 128);
                count = count2;
            }
            i++;
            count2 = count;
        }
        out.write(bytearr, 0, utflen + 2);
        return utflen + 2;
    }

    public final int size() {
        return this.written;
    }
}
