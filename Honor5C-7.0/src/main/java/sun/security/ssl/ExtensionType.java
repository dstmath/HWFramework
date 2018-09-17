package sun.security.ssl;

import java.util.List;

/* compiled from: HelloExtensions */
final class ExtensionType {
    static final ExtensionType EXT_CERT_TYPE = null;
    static final ExtensionType EXT_CLIENT_CERTIFICATE_URL = null;
    static final ExtensionType EXT_EC_POINT_FORMATS = null;
    static final ExtensionType EXT_ELLIPTIC_CURVES = null;
    static final ExtensionType EXT_MAX_FRAGMENT_LENGTH = null;
    static final ExtensionType EXT_RENEGOTIATION_INFO = null;
    static final ExtensionType EXT_SERVER_NAME = null;
    static final ExtensionType EXT_SIGNATURE_ALGORITHMS = null;
    static final ExtensionType EXT_SRP = null;
    static final ExtensionType EXT_STATUS_REQUEST = null;
    static final ExtensionType EXT_TRUNCATED_HMAC = null;
    static final ExtensionType EXT_TRUSTED_CA_KEYS = null;
    static final ExtensionType EXT_USER_MAPPING = null;
    static List<ExtensionType> knownExtensions;
    final int id;
    final String name;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.ExtensionType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.ExtensionType.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.ExtensionType.<clinit>():void");
    }

    private ExtensionType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    static ExtensionType get(int id) {
        for (ExtensionType ext : knownExtensions) {
            if (ext.id == id) {
                return ext;
            }
        }
        return new ExtensionType(id, "type_" + id);
    }

    private static ExtensionType e(int id, String name) {
        ExtensionType ext = new ExtensionType(id, name);
        knownExtensions.add(ext);
        return ext;
    }
}
