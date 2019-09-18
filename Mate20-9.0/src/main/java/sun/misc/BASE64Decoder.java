package sun.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Locale;

public class BASE64Decoder extends CharacterDecoder {
    private static final char[] pem_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] pem_convert_array = new byte[256];
    byte[] decode_buffer = new byte[4];

    /* access modifiers changed from: protected */
    public int bytesPerAtom() {
        return 4;
    }

    /* access modifiers changed from: protected */
    public int bytesPerLine() {
        return 72;
    }

    static {
        for (int i = 0; i < 255; i++) {
            pem_convert_array[i] = -1;
        }
        for (int i2 = 0; i2 < pem_array.length; i2++) {
            pem_convert_array[pem_array[i2]] = (byte) i2;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004a, code lost:
        r2 = pem_convert_array[r11.decode_buffer[2] & java.lang.Character.DIRECTIONALITY_UNDEFINED];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0054, code lost:
        r1 = pem_convert_array[r11.decode_buffer[1] & java.lang.Character.DIRECTIONALITY_UNDEFINED];
        r0 = pem_convert_array[r11.decode_buffer[0] & java.lang.Character.DIRECTIONALITY_UNDEFINED];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0068, code lost:
        switch(r14) {
            case 2: goto L_0x00ab;
            case 3: goto L_0x0091;
            case 4: goto L_0x006c;
            default: goto L_0x006b;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006c, code lost:
        r13.write((int) (byte) (((r0 << 2) & 252) | ((r1 >>> 4) & 3)));
        r13.write((int) (byte) (((r1 << 4) & 240) | ((r2 >>> 2) & 15)));
        r13.write((int) (byte) (((r2 << 6) & sun.security.util.DerValue.TAG_PRIVATE) | (r3 & 63)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0091, code lost:
        r13.write((int) (byte) (((r0 << 2) & 252) | ((r1 >>> 4) & 3)));
        r13.write((int) (byte) (((r1 << 4) & 240) | ((r2 >>> 2) & 15)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ab, code lost:
        r13.write((int) (byte) (((r0 << 2) & 252) | ((r1 >>> 4) & 3)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        return;
     */
    public void decodeAtom(PushbackInputStream inStream, OutputStream outStream, int rem) throws IOException {
        byte a = -1;
        byte b = -1;
        byte c = -1;
        byte d = -1;
        if (rem >= 2) {
            while (true) {
                int i = inStream.read();
                if (i == -1) {
                    throw new CEStreamExhausted();
                } else if (i != 10 && i != 13) {
                    this.decode_buffer[0] = (byte) i;
                    if (readFully(inStream, this.decode_buffer, 1, rem - 1) != -1) {
                        if (rem > 3 && this.decode_buffer[3] == 61) {
                            rem = 3;
                        }
                        if (rem > 2 && this.decode_buffer[2] == 61) {
                            rem = 2;
                        }
                        switch (rem) {
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                d = pem_convert_array[this.decode_buffer[3] & Character.DIRECTIONALITY_UNDEFINED];
                                break;
                        }
                    } else {
                        throw new CEStreamExhausted();
                    }
                }
            }
        } else {
            throw new CEFormatException("BASE64Decoder: Not enough bytes for an atom.");
        }
    }
}
