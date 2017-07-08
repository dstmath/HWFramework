package android.renderscript;

public class ProgramRaster extends BaseObj {
    CullMode mCullMode;
    boolean mPointSprite;

    public static class Builder {
        CullMode mCullMode;
        boolean mPointSprite;
        RenderScript mRS;

        public Builder(RenderScript rs) {
            this.mRS = rs;
            this.mPointSprite = false;
            this.mCullMode = CullMode.BACK;
        }

        public Builder setPointSpriteEnabled(boolean enable) {
            this.mPointSprite = enable;
            return this;
        }

        public Builder setCullMode(CullMode m) {
            this.mCullMode = m;
            return this;
        }

        public ProgramRaster create() {
            this.mRS.validate();
            ProgramRaster programRaster = new ProgramRaster(this.mRS.nProgramRasterCreate(this.mPointSprite, this.mCullMode.mID), this.mRS);
            programRaster.mPointSprite = this.mPointSprite;
            programRaster.mCullMode = this.mCullMode;
            return programRaster;
        }
    }

    public enum CullMode {
        ;
        
        int mID;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.ProgramRaster.CullMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.ProgramRaster.CullMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ProgramRaster.CullMode.<clinit>():void");
        }

        private CullMode(int id) {
            this.mID = id;
        }
    }

    ProgramRaster(long id, RenderScript rs) {
        super(id, rs);
        this.mPointSprite = false;
        this.mCullMode = CullMode.BACK;
    }

    public boolean isPointSpriteEnabled() {
        return this.mPointSprite;
    }

    public CullMode getCullMode() {
        return this.mCullMode;
    }

    public static ProgramRaster CULL_BACK(RenderScript rs) {
        if (rs.mProgramRaster_CULL_BACK == null) {
            Builder builder = new Builder(rs);
            builder.setCullMode(CullMode.BACK);
            rs.mProgramRaster_CULL_BACK = builder.create();
        }
        return rs.mProgramRaster_CULL_BACK;
    }

    public static ProgramRaster CULL_FRONT(RenderScript rs) {
        if (rs.mProgramRaster_CULL_FRONT == null) {
            Builder builder = new Builder(rs);
            builder.setCullMode(CullMode.FRONT);
            rs.mProgramRaster_CULL_FRONT = builder.create();
        }
        return rs.mProgramRaster_CULL_FRONT;
    }

    public static ProgramRaster CULL_NONE(RenderScript rs) {
        if (rs.mProgramRaster_CULL_NONE == null) {
            Builder builder = new Builder(rs);
            builder.setCullMode(CullMode.NONE);
            rs.mProgramRaster_CULL_NONE = builder.create();
        }
        return rs.mProgramRaster_CULL_NONE;
    }
}
