package sun.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

public abstract class CharacterDecoder {
    /* access modifiers changed from: protected */
    public abstract int bytesPerAtom();

    /* access modifiers changed from: protected */
    public abstract int bytesPerLine();

    /* access modifiers changed from: protected */
    public void decodeBufferPrefix(PushbackInputStream aStream, OutputStream bStream) throws IOException {
    }

    /* access modifiers changed from: protected */
    public void decodeBufferSuffix(PushbackInputStream aStream, OutputStream bStream) throws IOException {
    }

    /* access modifiers changed from: protected */
    public int decodeLinePrefix(PushbackInputStream aStream, OutputStream bStream) throws IOException {
        return bytesPerLine();
    }

    /* access modifiers changed from: protected */
    public void decodeLineSuffix(PushbackInputStream aStream, OutputStream bStream) throws IOException {
    }

    /* access modifiers changed from: protected */
    public void decodeAtom(PushbackInputStream aStream, OutputStream bStream, int l) throws IOException {
        throw new CEStreamExhausted();
    }

    /* access modifiers changed from: protected */
    public int readFully(InputStream in, byte[] buffer, int offset, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int q = in.read();
            int i2 = -1;
            if (q == -1) {
                if (i != 0) {
                    i2 = i;
                }
                return i2;
            }
            buffer[i + offset] = (byte) q;
        }
        return len;
    }

    public void decodeBuffer(InputStream aStream, OutputStream bStream) throws IOException {
        int totalBytes = 0;
        PushbackInputStream ps = new PushbackInputStream(aStream);
        decodeBufferPrefix(ps, bStream);
        while (true) {
            try {
                int length = decodeLinePrefix(ps, bStream);
                int i = 0;
                while (bytesPerAtom() + i < length) {
                    decodeAtom(ps, bStream, bytesPerAtom());
                    totalBytes += bytesPerAtom();
                    i += bytesPerAtom();
                }
                if (bytesPerAtom() + i == length) {
                    decodeAtom(ps, bStream, bytesPerAtom());
                    totalBytes += bytesPerAtom();
                } else {
                    decodeAtom(ps, bStream, length - i);
                    totalBytes += length - i;
                }
                decodeLineSuffix(ps, bStream);
            } catch (CEStreamExhausted e) {
                decodeBufferSuffix(ps, bStream);
                return;
            }
        }
    }

    public byte[] decodeBuffer(String inputString) throws IOException {
        byte[] inputBuffer = new byte[inputString.length()];
        inputString.getBytes(0, inputString.length(), inputBuffer, 0);
        ByteArrayInputStream inStream = new ByteArrayInputStream(inputBuffer);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        decodeBuffer(inStream, outStream);
        return outStream.toByteArray();
    }

    public byte[] decodeBuffer(InputStream in) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        decodeBuffer(in, outStream);
        return outStream.toByteArray();
    }

    public ByteBuffer decodeBufferToByteBuffer(String inputString) throws IOException {
        return ByteBuffer.wrap(decodeBuffer(inputString));
    }

    public ByteBuffer decodeBufferToByteBuffer(InputStream in) throws IOException {
        return ByteBuffer.wrap(decodeBuffer(in));
    }
}
