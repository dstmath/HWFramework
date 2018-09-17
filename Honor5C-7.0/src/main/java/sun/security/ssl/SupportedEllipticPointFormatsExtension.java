package sun.security.ssl;

import java.io.IOException;
import java.util.ArrayList;
import javax.net.ssl.SSLProtocolException;
import sun.security.x509.GeneralNameInterface;

/* compiled from: HelloExtensions */
final class SupportedEllipticPointFormatsExtension extends HelloExtension {
    static final HelloExtension DEFAULT = null;
    static final int FMT_ANSIX962_COMPRESSED_CHAR2 = 2;
    static final int FMT_ANSIX962_COMPRESSED_PRIME = 1;
    static final int FMT_UNCOMPRESSED = 0;
    private final byte[] formats;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SupportedEllipticPointFormatsExtension.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SupportedEllipticPointFormatsExtension.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SupportedEllipticPointFormatsExtension.<clinit>():void");
    }

    private SupportedEllipticPointFormatsExtension(byte[] formats) {
        super(ExtensionType.EXT_EC_POINT_FORMATS);
        this.formats = formats;
    }

    SupportedEllipticPointFormatsExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_EC_POINT_FORMATS);
        this.formats = s.getBytes8();
        boolean uncompressed = false;
        byte[] bArr = this.formats;
        int length = bArr.length;
        for (int i = 0; i < length; i += FMT_ANSIX962_COMPRESSED_PRIME) {
            if (bArr[i] == 0) {
                uncompressed = true;
                break;
            }
        }
        if (!uncompressed) {
            throw new SSLProtocolException("Peer does not support uncompressed points");
        }
    }

    int length() {
        return this.formats.length + 5;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        s.putInt16(this.formats.length + FMT_ANSIX962_COMPRESSED_PRIME);
        s.putBytes8(this.formats);
    }

    private static String toString(byte format) {
        int f = format & 255;
        switch (f) {
            case GeneralNameInterface.NAME_MATCH /*0*/:
                return "uncompressed";
            case FMT_ANSIX962_COMPRESSED_PRIME /*1*/:
                return "ansiX962_compressed_prime";
            case FMT_ANSIX962_COMPRESSED_CHAR2 /*2*/:
                return "ansiX962_compressed_char2";
            default:
                return "unknown-" + f;
        }
    }

    public String toString() {
        Object list = new ArrayList();
        byte[] bArr = this.formats;
        int length = bArr.length;
        for (int i = 0; i < length; i += FMT_ANSIX962_COMPRESSED_PRIME) {
            list.add(toString(bArr[i]));
        }
        return "Extension " + this.type + ", formats: " + list;
    }
}
