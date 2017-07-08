package jcifs.dcerpc;

public interface DcerpcError {
    public static final int DCERPC_FAULT_ACCESS_DENIED = 5;
    public static final int DCERPC_FAULT_CANT_PERFORM = 1752;
    public static final int[] DCERPC_FAULT_CODES = null;
    public static final int DCERPC_FAULT_CONTEXT_MISMATCH = 469762074;
    public static final int DCERPC_FAULT_INVALID_TAG = 469762054;
    public static final String[] DCERPC_FAULT_MESSAGES = null;
    public static final int DCERPC_FAULT_NDR = 1783;
    public static final int DCERPC_FAULT_OP_RNG_ERROR = 469827586;
    public static final int DCERPC_FAULT_OTHER = 1;
    public static final int DCERPC_FAULT_PROTO_ERROR = 469827595;
    public static final int DCERPC_FAULT_UNK_IF = 469827587;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.dcerpc.DcerpcError.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.dcerpc.DcerpcError.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.dcerpc.DcerpcError.<clinit>():void");
    }
}
