package java.security.spec;

public class PSSParameterSpec implements AlgorithmParameterSpec {
    public static final PSSParameterSpec DEFAULT = null;
    private String mdName;
    private String mgfName;
    private AlgorithmParameterSpec mgfSpec;
    private int saltLen;
    private int trailerField;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.spec.PSSParameterSpec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.spec.PSSParameterSpec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.security.spec.PSSParameterSpec.<clinit>():void");
    }

    private PSSParameterSpec() {
        this.mdName = "SHA-1";
        this.mgfName = "MGF1";
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.saltLen = 20;
        this.trailerField = 1;
    }

    public PSSParameterSpec(String mdName, String mgfName, AlgorithmParameterSpec mgfSpec, int saltLen, int trailerField) {
        this.mdName = "SHA-1";
        this.mgfName = "MGF1";
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.saltLen = 20;
        this.trailerField = 1;
        if (mdName == null) {
            throw new NullPointerException("digest algorithm is null");
        } else if (mgfName == null) {
            throw new NullPointerException("mask generation function algorithm is null");
        } else if (saltLen < 0) {
            throw new IllegalArgumentException("negative saltLen value: " + saltLen);
        } else if (trailerField < 0) {
            throw new IllegalArgumentException("negative trailerField: " + trailerField);
        } else {
            this.mdName = mdName;
            this.mgfName = mgfName;
            this.mgfSpec = mgfSpec;
            this.saltLen = saltLen;
            this.trailerField = trailerField;
        }
    }

    public PSSParameterSpec(int saltLen) {
        this.mdName = "SHA-1";
        this.mgfName = "MGF1";
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.saltLen = 20;
        this.trailerField = 1;
        if (saltLen < 0) {
            throw new IllegalArgumentException("negative saltLen value: " + saltLen);
        }
        this.saltLen = saltLen;
    }

    public String getDigestAlgorithm() {
        return this.mdName;
    }

    public String getMGFAlgorithm() {
        return this.mgfName;
    }

    public AlgorithmParameterSpec getMGFParameters() {
        return this.mgfSpec;
    }

    public int getSaltLength() {
        return this.saltLen;
    }

    public int getTrailerField() {
        return this.trailerField;
    }
}
