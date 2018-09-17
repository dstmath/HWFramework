package com.android.org.bouncycastle.util.io.pem;

import com.android.org.bouncycastle.util.Strings;
import com.android.org.bouncycastle.util.encoders.Base64;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class PemWriter extends BufferedWriter {
    private static final int LINE_LENGTH = 64;
    private char[] buf = new char[64];
    private final int nlLength;

    public PemWriter(Writer out) {
        super(out);
        String nl = Strings.lineSeparator();
        if (nl != null) {
            this.nlLength = nl.length();
        } else {
            this.nlLength = 2;
        }
    }

    public int getOutputSize(PemObject obj) {
        int size = ((((obj.getType().length() + 10) + this.nlLength) * 2) + 6) + 4;
        if (!obj.getHeaders().isEmpty()) {
            for (PemHeader hdr : obj.getHeaders()) {
                size += ((hdr.getName().length() + ": ".length()) + hdr.getValue().length()) + this.nlLength;
            }
            size += this.nlLength;
        }
        int dataLen = ((obj.getContent().length + 2) / 3) * 4;
        return size + (((((dataLen + 64) - 1) / 64) * this.nlLength) + dataLen);
    }

    public void writeObject(PemObjectGenerator objGen) throws IOException {
        PemObject obj = objGen.generate();
        writePreEncapsulationBoundary(obj.getType());
        if (!obj.getHeaders().isEmpty()) {
            for (PemHeader hdr : obj.getHeaders()) {
                write(hdr.getName());
                write(": ");
                write(hdr.getValue());
                newLine();
            }
            newLine();
        }
        writeEncoded(obj.getContent());
        writePostEncapsulationBoundary(obj.getType());
    }

    private void writeEncoded(byte[] bytes) throws IOException {
        bytes = Base64.encode(bytes);
        int i = 0;
        while (i < bytes.length) {
            int index = 0;
            while (index != this.buf.length && i + index < bytes.length) {
                this.buf[index] = (char) bytes[i + index];
                index++;
            }
            write(this.buf, 0, index);
            newLine();
            i += this.buf.length;
        }
    }

    private void writePreEncapsulationBoundary(String type) throws IOException {
        write("-----BEGIN " + type + "-----");
        newLine();
    }

    private void writePostEncapsulationBoundary(String type) throws IOException {
        write("-----END " + type + "-----");
        newLine();
    }
}
