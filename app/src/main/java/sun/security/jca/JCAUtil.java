package sun.security.jca;

import java.security.SecureRandom;

public final class JCAUtil {
    private static final int ARRAY_SIZE = 4096;
    private static final Object LOCK = null;
    private static volatile SecureRandom secureRandom;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.jca.JCAUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.jca.JCAUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.jca.JCAUtil.<clinit>():void");
    }

    private JCAUtil() {
    }

    public static int getTempArraySize(int totalSize) {
        return Math.min((int) ARRAY_SIZE, totalSize);
    }

    public static SecureRandom getSecureRandom() {
        Throwable th;
        SecureRandom r = secureRandom;
        if (r == null) {
            synchronized (LOCK) {
                try {
                    r = secureRandom;
                    if (r == null) {
                        SecureRandom r2 = new SecureRandom();
                        try {
                            secureRandom = r2;
                            r = r2;
                        } catch (Throwable th2) {
                            th = th2;
                            r = r2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        return r;
    }
}
