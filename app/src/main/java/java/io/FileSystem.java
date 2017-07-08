package java.io;

import java.util.jar.Pack200.Unpacker;

abstract class FileSystem {
    public static final int ACCESS_EXECUTE = 1;
    public static final int ACCESS_OK = 8;
    public static final int ACCESS_READ = 4;
    public static final int ACCESS_WRITE = 2;
    public static final int BA_DIRECTORY = 4;
    public static final int BA_EXISTS = 1;
    public static final int BA_HIDDEN = 8;
    public static final int BA_REGULAR = 2;
    public static final int SPACE_FREE = 1;
    public static final int SPACE_TOTAL = 0;
    public static final int SPACE_USABLE = 2;
    static boolean useCanonCaches;
    static boolean useCanonPrefixCache;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.FileSystem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.FileSystem.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.io.FileSystem.<clinit>():void");
    }

    public static native FileSystem getFileSystem();

    public abstract String canonicalize(String str) throws IOException;

    public abstract boolean checkAccess(File file, int i);

    public abstract int compare(File file, File file2);

    public abstract boolean createDirectory(File file);

    public abstract boolean createFileExclusively(String str) throws IOException;

    public abstract boolean delete(File file);

    public abstract String fromURIPath(String str);

    public abstract int getBooleanAttributes(File file);

    public abstract String getDefaultParent();

    public abstract long getLastModifiedTime(File file);

    public abstract long getLength(File file);

    public abstract char getPathSeparator();

    public abstract char getSeparator();

    public abstract long getSpace(File file, int i);

    public abstract int hashCode(File file);

    public abstract boolean isAbsolute(File file);

    public abstract String[] list(File file);

    public abstract File[] listRoots();

    public abstract String normalize(String str);

    public abstract int prefixLength(String str);

    public abstract boolean rename(File file, File file2);

    public abstract String resolve(File file);

    public abstract String resolve(String str, String str2);

    public abstract boolean setLastModifiedTime(File file, long j);

    public abstract boolean setPermission(File file, int i, boolean z, boolean z2);

    public abstract boolean setReadOnly(File file);

    FileSystem() {
    }

    private static boolean getBooleanProperty(String prop, boolean defaultVal) {
        String val = System.getProperty(prop);
        if (val == null) {
            return defaultVal;
        }
        if (val.equalsIgnoreCase(Unpacker.TRUE)) {
            return true;
        }
        return false;
    }
}
