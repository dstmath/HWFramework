package sun.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import sun.util.calendar.BaseCalendar;

public class BASE64Decoder extends CharacterDecoder {
    private static final char[] pem_array = null;
    private static final byte[] pem_convert_array = null;
    byte[] decode_buffer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.BASE64Decoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.BASE64Decoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.BASE64Decoder.<clinit>():void");
    }

    public BASE64Decoder() {
        this.decode_buffer = new byte[4];
    }

    protected int bytesPerAtom() {
        return 4;
    }

    protected int bytesPerLine() {
        return 72;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void decodeAtom(PushbackInputStream inStream, OutputStream outStream, int rem) throws IOException {
        int a = -1;
        int b = -1;
        int i = -1;
        int i2 = -1;
        if (rem >= 2) {
            int i3;
            while (true) {
                i3 = inStream.read();
                if (i3 != -1) {
                    if (i3 != 10 && i3 != 13) {
                        break;
                    }
                } else {
                    break;
                }
            }
            this.decode_buffer[0] = (byte) i3;
            if (readFully(inStream, this.decode_buffer, 1, rem - 1) != -1) {
                if (rem > 3 && this.decode_buffer[3] == 61) {
                    rem = 3;
                }
                if (rem > 2 && this.decode_buffer[2] == 61) {
                    rem = 2;
                }
                switch (rem) {
                    case BaseCalendar.MONDAY /*2*/:
                        break;
                    case BaseCalendar.TUESDAY /*3*/:
                        break;
                    case BaseCalendar.WEDNESDAY /*4*/:
                        i2 = pem_convert_array[this.decode_buffer[3] & 255];
                        break;
                }
            }
            throw new CEStreamExhausted();
        }
        throw new CEFormatException("BASE64Decoder: Not enough bytes for an atom.");
    }
}
