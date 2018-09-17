package android.graphics;

public class Shader {
    private Matrix mLocalMatrix;
    private long native_instance;

    public enum TileMode {
        ;
        
        final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Shader.TileMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Shader.TileMode.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Shader.TileMode.<clinit>():void");
        }

        private TileMode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    private static native void nativeDestructor(long j);

    private static native long nativeSetLocalMatrix(long j, long j2);

    public Shader() {
    }

    protected void init(long ni) {
        this.native_instance = ni;
    }

    public boolean getLocalMatrix(Matrix localM) {
        boolean z = false;
        if (this.mLocalMatrix == null) {
            return false;
        }
        localM.set(this.mLocalMatrix);
        if (!this.mLocalMatrix.isIdentity()) {
            z = true;
        }
        return z;
    }

    public void setLocalMatrix(Matrix localM) {
        this.mLocalMatrix = localM;
        this.native_instance = nativeSetLocalMatrix(this.native_instance, localM == null ? 0 : localM.native_instance);
    }

    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            nativeDestructor(this.native_instance);
            this.native_instance = 0;
        }
    }

    protected Shader copy() {
        Shader copy = new Shader();
        copyLocalMatrix(copy);
        return copy;
    }

    protected void copyLocalMatrix(Shader dest) {
        if (this.mLocalMatrix != null) {
            Matrix lm = new Matrix();
            getLocalMatrix(lm);
            dest.setLocalMatrix(lm);
            return;
        }
        dest.setLocalMatrix(null);
    }

    public long getNativeInstance() {
        return this.native_instance;
    }
}
