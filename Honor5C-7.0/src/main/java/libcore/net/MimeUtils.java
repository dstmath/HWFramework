package libcore.net;

import java.util.Map;

public final class MimeUtils {
    private static final Map<String, String> extensionToMimeTypeMap = null;
    private static final Map<String, String> mimeTypeToExtensionMap = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.net.MimeUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.net.MimeUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.net.MimeUtils.<clinit>():void");
    }

    private static void add(String mimeType, String extension) {
        if (!mimeTypeToExtensionMap.containsKey(mimeType)) {
            mimeTypeToExtensionMap.put(mimeType, extension);
        }
        if (!extensionToMimeTypeMap.containsKey(extension)) {
            extensionToMimeTypeMap.put(extension, mimeType);
        }
    }

    private MimeUtils() {
    }

    public static boolean hasMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeTypeToExtensionMap.containsKey(mimeType);
    }

    public static String guessMimeTypeFromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }
        return (String) extensionToMimeTypeMap.get(extension);
    }

    public static boolean hasExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        return extensionToMimeTypeMap.containsKey(extension);
    }

    public static String guessExtensionFromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return null;
        }
        return (String) mimeTypeToExtensionMap.get(mimeType);
    }

    private static void addDrmMimeType() {
        add("application/vnd.oma.dd+xml", "dd");
        add("application/vnd.oma.drm.message", "dm");
        add("application/vnd.oma.drm.content", "dcf");
        add("application/vnd.oma.drm.rights+xml", "dr");
        add("application/vnd.oma.drm.rights+wbxml", "drc");
    }
}
