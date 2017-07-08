package javax.crypto.spec;

public class PSource {
    private String pSrcName;

    public static final class PSpecified extends PSource {
        public static final PSpecified DEFAULT = null;
        private byte[] p;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.crypto.spec.PSource.PSpecified.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.crypto.spec.PSource.PSpecified.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: javax.crypto.spec.PSource.PSpecified.<clinit>():void");
        }

        public PSpecified(byte[] p) {
            super("PSpecified");
            this.p = new byte[0];
            this.p = (byte[]) p.clone();
        }

        public byte[] getValue() {
            return this.p.length == 0 ? this.p : (byte[]) this.p.clone();
        }
    }

    protected PSource(String pSrcName) {
        if (pSrcName == null) {
            throw new NullPointerException("pSource algorithm is null");
        }
        this.pSrcName = pSrcName;
    }

    public String getAlgorithm() {
        return this.pSrcName;
    }
}
