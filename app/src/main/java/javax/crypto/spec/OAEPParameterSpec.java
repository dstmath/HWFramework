package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.PSource.PSpecified;

public class OAEPParameterSpec implements AlgorithmParameterSpec {
    public static final OAEPParameterSpec DEFAULT = null;
    private String mdName;
    private String mgfName;
    private AlgorithmParameterSpec mgfSpec;
    private PSource pSrc;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.crypto.spec.OAEPParameterSpec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.crypto.spec.OAEPParameterSpec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: javax.crypto.spec.OAEPParameterSpec.<clinit>():void");
    }

    private OAEPParameterSpec() {
        this.mdName = "SHA-1";
        this.mgfName = "MGF1";
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.pSrc = PSpecified.DEFAULT;
    }

    public OAEPParameterSpec(String mdName, String mgfName, AlgorithmParameterSpec mgfSpec, PSource pSrc) {
        this.mdName = "SHA-1";
        this.mgfName = "MGF1";
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.pSrc = PSpecified.DEFAULT;
        if (mdName == null) {
            throw new NullPointerException("digest algorithm is null");
        } else if (mgfName == null) {
            throw new NullPointerException("mask generation function algorithm is null");
        } else if (pSrc == null) {
            throw new NullPointerException("source of the encoding input is null");
        } else {
            this.mdName = mdName;
            this.mgfName = mgfName;
            this.mgfSpec = mgfSpec;
            this.pSrc = pSrc;
        }
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

    public PSource getPSource() {
        return this.pSrc;
    }
}
