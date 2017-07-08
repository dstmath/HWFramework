package java.security.spec;

public class MGF1ParameterSpec implements AlgorithmParameterSpec {
    public static final MGF1ParameterSpec SHA1 = null;
    public static final MGF1ParameterSpec SHA256 = null;
    public static final MGF1ParameterSpec SHA384 = null;
    public static final MGF1ParameterSpec SHA512 = null;
    private String mdName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.spec.MGF1ParameterSpec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.spec.MGF1ParameterSpec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.security.spec.MGF1ParameterSpec.<clinit>():void");
    }

    public MGF1ParameterSpec(String mdName) {
        if (mdName == null) {
            throw new NullPointerException("digest algorithm is null");
        }
        this.mdName = mdName;
    }

    public String getDigestAlgorithm() {
        return this.mdName;
    }
}
