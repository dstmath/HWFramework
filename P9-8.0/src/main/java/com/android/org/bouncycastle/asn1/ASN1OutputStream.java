package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

public class ASN1OutputStream {
    private OutputStream os;

    private class ImplicitOutputStream extends ASN1OutputStream {
        private boolean first = true;

        public ImplicitOutputStream(OutputStream os) {
            super(os);
        }

        public void write(int b) throws IOException {
            if (this.first) {
                this.first = false;
            } else {
                super.write(b);
            }
        }
    }

    public ASN1OutputStream(OutputStream os) {
        this.os = os;
    }

    void writeLength(int length) throws IOException {
        if (length > 127) {
            int size = 1;
            int val = length;
            while (true) {
                val >>>= 8;
                if (val == 0) {
                    break;
                }
                size++;
            }
            write((byte) (size | 128));
            for (int i = (size - 1) * 8; i >= 0; i -= 8) {
                write((byte) (length >> i));
            }
            return;
        }
        write((byte) length);
    }

    void write(int b) throws IOException {
        this.os.write(b);
    }

    void write(byte[] bytes) throws IOException {
        this.os.write(bytes);
    }

    void write(byte[] bytes, int off, int len) throws IOException {
        this.os.write(bytes, off, len);
    }

    void writeEncoded(int tag, byte[] bytes) throws IOException {
        write(tag);
        writeLength(bytes.length);
        write(bytes);
    }

    void writeTag(int flags, int tagNo) throws IOException {
        if (tagNo < 31) {
            write(flags | tagNo);
            return;
        }
        write(flags | 31);
        if (tagNo < 128) {
            write(tagNo);
            return;
        }
        byte[] stack = new byte[5];
        int pos = stack.length - 1;
        stack[pos] = (byte) (tagNo & 127);
        do {
            tagNo >>= 7;
            pos--;
            stack[pos] = (byte) ((tagNo & 127) | 128);
        } while (tagNo > 127);
        write(stack, pos, stack.length - pos);
    }

    void writeEncoded(int flags, int tagNo, byte[] bytes) throws IOException {
        writeTag(flags, tagNo);
        writeLength(bytes.length);
        write(bytes);
    }

    protected void writeNull() throws IOException {
        this.os.write(5);
        this.os.write(0);
    }

    public void writeObject(ASN1Encodable obj) throws IOException {
        if (obj != null) {
            obj.toASN1Primitive().encode(this);
            return;
        }
        throw new IOException("null object detected");
    }

    void writeImplicitObject(ASN1Primitive obj) throws IOException {
        if (obj != null) {
            obj.encode(new ImplicitOutputStream(this.os));
            return;
        }
        throw new IOException("null object detected");
    }

    public void close() throws IOException {
        this.os.close();
    }

    public void flush() throws IOException {
        this.os.flush();
    }

    ASN1OutputStream getDERSubStream() {
        return new DEROutputStream(this.os);
    }

    ASN1OutputStream getDLSubStream() {
        return new DLOutputStream(this.os);
    }
}
