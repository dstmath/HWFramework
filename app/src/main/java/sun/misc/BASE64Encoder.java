package sun.misc;

import java.io.IOException;
import java.io.OutputStream;

public class BASE64Encoder extends CharacterEncoder {
    private static final char[] pem_array = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.BASE64Encoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.BASE64Encoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.BASE64Encoder.<clinit>():void");
    }

    protected int bytesPerAtom() {
        return 3;
    }

    protected int bytesPerLine() {
        return 57;
    }

    protected void encodeAtom(OutputStream outStream, byte[] data, int offset, int len) throws IOException {
        byte a;
        if (len == 1) {
            a = data[offset];
            outStream.write(pem_array[(a >>> 2) & 63]);
            outStream.write(pem_array[((a << 4) & 48) + 0]);
            outStream.write(61);
            outStream.write(61);
        } else if (len == 2) {
            a = data[offset];
            b = data[offset + 1];
            outStream.write(pem_array[(a >>> 2) & 63]);
            outStream.write(pem_array[((a << 4) & 48) + ((b >>> 4) & 15)]);
            outStream.write(pem_array[((b << 2) & 60) + 0]);
            outStream.write(61);
        } else {
            a = data[offset];
            b = data[offset + 1];
            byte c = data[offset + 2];
            outStream.write(pem_array[(a >>> 2) & 63]);
            outStream.write(pem_array[((a << 4) & 48) + ((b >>> 4) & 15)]);
            outStream.write(pem_array[((b << 2) & 60) + ((c >>> 6) & 3)]);
            outStream.write(pem_array[c & 63]);
        }
    }
}
