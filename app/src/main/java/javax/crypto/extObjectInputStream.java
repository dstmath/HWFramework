package javax.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/* compiled from: SealedObject */
final class extObjectInputStream extends ObjectInputStream {
    private static ClassLoader systemClassLoader;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.crypto.extObjectInputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.crypto.extObjectInputStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: javax.crypto.extObjectInputStream.<clinit>():void");
    }

    extObjectInputStream(InputStream in) throws IOException, StreamCorruptedException {
        super(in);
    }

    protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(v);
        } catch (ClassNotFoundException e) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                if (systemClassLoader == null) {
                    systemClassLoader = ClassLoader.getSystemClassLoader();
                }
                loader = systemClassLoader;
                if (loader == null) {
                    throw new ClassNotFoundException(v.getName());
                }
            }
            return Class.forName(v.getName(), false, loader);
        }
    }
}
