package sun.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;

public abstract class CharacterEncoder {
    protected PrintStream pStream;

    protected abstract int bytesPerAtom();

    protected abstract int bytesPerLine();

    protected abstract void encodeAtom(OutputStream outputStream, byte[] bArr, int i, int i2) throws IOException;

    protected void encodeBufferPrefix(OutputStream aStream) throws IOException {
        this.pStream = new PrintStream(aStream);
    }

    protected void encodeBufferSuffix(OutputStream aStream) throws IOException {
    }

    protected void encodeLinePrefix(OutputStream aStream, int aLength) throws IOException {
    }

    protected void encodeLineSuffix(OutputStream aStream) throws IOException {
        this.pStream.println();
    }

    protected int readFully(InputStream in, byte[] buffer) throws IOException {
        for (int i = 0; i < buffer.length; i++) {
            int q = in.read();
            if (q == -1) {
                return i;
            }
            buffer[i] = (byte) q;
        }
        return buffer.length;
    }

    public void encode(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] tmpbuffer = new byte[bytesPerLine()];
        encodeBufferPrefix(outStream);
        while (true) {
            int numBytes = readFully(inStream, tmpbuffer);
            if (numBytes != 0) {
                encodeLinePrefix(outStream, numBytes);
                int j = 0;
                while (j < numBytes) {
                    if (bytesPerAtom() + j <= numBytes) {
                        encodeAtom(outStream, tmpbuffer, j, bytesPerAtom());
                    } else {
                        encodeAtom(outStream, tmpbuffer, j, numBytes - j);
                    }
                    j += bytesPerAtom();
                }
                if (numBytes < bytesPerLine()) {
                    break;
                }
                encodeLineSuffix(outStream);
            } else {
                break;
            }
        }
        encodeBufferSuffix(outStream);
    }

    public void encode(byte[] aBuffer, OutputStream aStream) throws IOException {
        encode(new ByteArrayInputStream(aBuffer), aStream);
    }

    public String encode(byte[] aBuffer) {
        OutputStream outStream = new ByteArrayOutputStream();
        try {
            encode(new ByteArrayInputStream(aBuffer), outStream);
            return outStream.toString("8859_1");
        } catch (Exception e) {
            throw new Error("CharacterEncoder.encode internal error");
        }
    }

    private byte[] getBytes(ByteBuffer bb) {
        byte[] buf = null;
        if (bb.hasArray()) {
            byte[] tmp = bb.array();
            if (tmp.length == bb.capacity() && tmp.length == bb.remaining()) {
                buf = tmp;
                bb.position(bb.limit());
            }
        }
        if (buf != null) {
            return buf;
        }
        buf = new byte[bb.remaining()];
        bb.get(buf);
        return buf;
    }

    public void encode(ByteBuffer aBuffer, OutputStream aStream) throws IOException {
        encode(getBytes(aBuffer), aStream);
    }

    public String encode(ByteBuffer aBuffer) {
        return encode(getBytes(aBuffer));
    }

    public void encodeBuffer(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] tmpbuffer = new byte[bytesPerLine()];
        encodeBufferPrefix(outStream);
        int numBytes;
        do {
            numBytes = readFully(inStream, tmpbuffer);
            if (numBytes == 0) {
                break;
            }
            encodeLinePrefix(outStream, numBytes);
            int j = 0;
            while (j < numBytes) {
                if (bytesPerAtom() + j <= numBytes) {
                    encodeAtom(outStream, tmpbuffer, j, bytesPerAtom());
                } else {
                    encodeAtom(outStream, tmpbuffer, j, numBytes - j);
                }
                j += bytesPerAtom();
            }
            encodeLineSuffix(outStream);
        } while (numBytes >= bytesPerLine());
        encodeBufferSuffix(outStream);
    }

    public void encodeBuffer(byte[] aBuffer, OutputStream aStream) throws IOException {
        encodeBuffer(new ByteArrayInputStream(aBuffer), aStream);
    }

    public String encodeBuffer(byte[] aBuffer) {
        OutputStream outStream = new ByteArrayOutputStream();
        try {
            encodeBuffer(new ByteArrayInputStream(aBuffer), outStream);
            return outStream.toString();
        } catch (Exception e) {
            throw new Error("CharacterEncoder.encodeBuffer internal error");
        }
    }

    public void encodeBuffer(ByteBuffer aBuffer, OutputStream aStream) throws IOException {
        encodeBuffer(getBytes(aBuffer), aStream);
    }

    public String encodeBuffer(ByteBuffer aBuffer) {
        return encodeBuffer(getBytes(aBuffer));
    }
}
