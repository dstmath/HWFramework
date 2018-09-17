package jcifs.smb;

public interface WinError {
    public static final int ERROR_ACCESS_DENIED = 5;
    public static final int ERROR_BAD_PIPE = 230;
    public static final int ERROR_MORE_DATA = 234;
    public static final int ERROR_NO_BROWSER_SERVERS_FOUND = 6118;
    public static final int ERROR_NO_DATA = 232;
    public static final int ERROR_PIPE_BUSY = 231;
    public static final int ERROR_PIPE_NOT_CONNECTED = 233;
    public static final int ERROR_REQ_NOT_ACCEP = 71;
    public static final int ERROR_SUCCESS = 0;
    public static final int[] WINERR_CODES = null;
    public static final String[] WINERR_MESSAGES = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.WinError.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.WinError.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.WinError.<clinit>():void");
    }
}
