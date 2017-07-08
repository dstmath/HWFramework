package android.system;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class UnixSocketAddress extends SocketAddress {
    private static final int NAMED_PATH_LENGTH = 0;
    private static final byte[] UNNAMED_PATH = null;
    private byte[] sun_path;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.system.UnixSocketAddress.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.system.UnixSocketAddress.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.system.UnixSocketAddress.<clinit>():void");
    }

    private UnixSocketAddress(byte[] sun_path) {
        if (sun_path == null) {
            throw new IllegalArgumentException("sun_path must not be null");
        } else if (sun_path.length > NAMED_PATH_LENGTH) {
            throw new IllegalArgumentException("sun_path exceeds the maximum length");
        } else if (sun_path.length == 0) {
            this.sun_path = UNNAMED_PATH;
        } else {
            this.sun_path = new byte[sun_path.length];
            System.arraycopy(sun_path, 0, this.sun_path, 0, sun_path.length);
        }
    }

    public static UnixSocketAddress createAbstract(String name) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] path = new byte[(nameBytes.length + 1)];
        System.arraycopy(nameBytes, 0, path, 1, nameBytes.length);
        return new UnixSocketAddress(path);
    }

    public static UnixSocketAddress createFileSystem(String pathName) {
        byte[] pathNameBytes = pathName.getBytes(StandardCharsets.UTF_8);
        byte[] path = new byte[(pathNameBytes.length + 1)];
        System.arraycopy(pathNameBytes, 0, path, 0, pathNameBytes.length);
        return new UnixSocketAddress(path);
    }

    public static UnixSocketAddress createUnnamed() {
        return new UnixSocketAddress(UNNAMED_PATH);
    }

    public byte[] getSunPath() {
        if (this.sun_path.length == 0) {
            return this.sun_path;
        }
        byte[] sunPathCopy = new byte[this.sun_path.length];
        System.arraycopy(this.sun_path, 0, sunPathCopy, 0, this.sun_path.length);
        return sunPathCopy;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(this.sun_path, ((UnixSocketAddress) o).sun_path);
    }

    public int hashCode() {
        return Arrays.hashCode(this.sun_path);
    }

    public String toString() {
        return "UnixSocketAddress[sun_path=" + Arrays.toString(this.sun_path) + ']';
    }
}
