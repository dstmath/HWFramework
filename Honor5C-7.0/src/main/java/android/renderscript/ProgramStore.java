package android.renderscript;

public class ProgramStore extends BaseObj {
    BlendDstFunc mBlendDst;
    BlendSrcFunc mBlendSrc;
    boolean mColorMaskA;
    boolean mColorMaskB;
    boolean mColorMaskG;
    boolean mColorMaskR;
    DepthFunc mDepthFunc;
    boolean mDepthMask;
    boolean mDither;

    public enum BlendDstFunc {
        ;
        
        int mID;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.ProgramStore.BlendDstFunc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.ProgramStore.BlendDstFunc.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ProgramStore.BlendDstFunc.<clinit>():void");
        }

        private BlendDstFunc(int id) {
            this.mID = id;
        }
    }

    public enum BlendSrcFunc {
        ;
        
        int mID;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.ProgramStore.BlendSrcFunc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.ProgramStore.BlendSrcFunc.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ProgramStore.BlendSrcFunc.<clinit>():void");
        }

        private BlendSrcFunc(int id) {
            this.mID = id;
        }
    }

    public static class Builder {
        BlendDstFunc mBlendDst;
        BlendSrcFunc mBlendSrc;
        boolean mColorMaskA;
        boolean mColorMaskB;
        boolean mColorMaskG;
        boolean mColorMaskR;
        DepthFunc mDepthFunc;
        boolean mDepthMask;
        boolean mDither;
        RenderScript mRS;

        public Builder(RenderScript rs) {
            this.mRS = rs;
            this.mDepthFunc = DepthFunc.ALWAYS;
            this.mDepthMask = false;
            this.mColorMaskR = true;
            this.mColorMaskG = true;
            this.mColorMaskB = true;
            this.mColorMaskA = true;
            this.mBlendSrc = BlendSrcFunc.ONE;
            this.mBlendDst = BlendDstFunc.ZERO;
        }

        public Builder setDepthFunc(DepthFunc func) {
            this.mDepthFunc = func;
            return this;
        }

        public Builder setDepthMaskEnabled(boolean enable) {
            this.mDepthMask = enable;
            return this;
        }

        public Builder setColorMaskEnabled(boolean r, boolean g, boolean b, boolean a) {
            this.mColorMaskR = r;
            this.mColorMaskG = g;
            this.mColorMaskB = b;
            this.mColorMaskA = a;
            return this;
        }

        public Builder setBlendFunc(BlendSrcFunc src, BlendDstFunc dst) {
            this.mBlendSrc = src;
            this.mBlendDst = dst;
            return this;
        }

        public Builder setDitherEnabled(boolean enable) {
            this.mDither = enable;
            return this;
        }

        public ProgramStore create() {
            this.mRS.validate();
            ProgramStore programStore = new ProgramStore(this.mRS.nProgramStoreCreate(this.mColorMaskR, this.mColorMaskG, this.mColorMaskB, this.mColorMaskA, this.mDepthMask, this.mDither, this.mBlendSrc.mID, this.mBlendDst.mID, this.mDepthFunc.mID), this.mRS);
            programStore.mDepthFunc = this.mDepthFunc;
            programStore.mDepthMask = this.mDepthMask;
            programStore.mColorMaskR = this.mColorMaskR;
            programStore.mColorMaskG = this.mColorMaskG;
            programStore.mColorMaskB = this.mColorMaskB;
            programStore.mColorMaskA = this.mColorMaskA;
            programStore.mBlendSrc = this.mBlendSrc;
            programStore.mBlendDst = this.mBlendDst;
            programStore.mDither = this.mDither;
            return programStore;
        }
    }

    public enum DepthFunc {
        ;
        
        int mID;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.ProgramStore.DepthFunc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.ProgramStore.DepthFunc.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ProgramStore.DepthFunc.<clinit>():void");
        }

        private DepthFunc(int id) {
            this.mID = id;
        }
    }

    ProgramStore(long id, RenderScript rs) {
        super(id, rs);
    }

    public DepthFunc getDepthFunc() {
        return this.mDepthFunc;
    }

    public boolean isDepthMaskEnabled() {
        return this.mDepthMask;
    }

    public boolean isColorMaskRedEnabled() {
        return this.mColorMaskR;
    }

    public boolean isColorMaskGreenEnabled() {
        return this.mColorMaskG;
    }

    public boolean isColorMaskBlueEnabled() {
        return this.mColorMaskB;
    }

    public boolean isColorMaskAlphaEnabled() {
        return this.mColorMaskA;
    }

    public BlendSrcFunc getBlendSrcFunc() {
        return this.mBlendSrc;
    }

    public BlendDstFunc getBlendDstFunc() {
        return this.mBlendDst;
    }

    public boolean isDitherEnabled() {
        return this.mDither;
    }

    public static ProgramStore BLEND_NONE_DEPTH_TEST(RenderScript rs) {
        if (rs.mProgramStore_BLEND_NONE_DEPTH_TEST == null) {
            Builder builder = new Builder(rs);
            builder.setDepthFunc(DepthFunc.LESS);
            builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ZERO);
            builder.setDitherEnabled(false);
            builder.setDepthMaskEnabled(true);
            rs.mProgramStore_BLEND_NONE_DEPTH_TEST = builder.create();
        }
        return rs.mProgramStore_BLEND_NONE_DEPTH_TEST;
    }

    public static ProgramStore BLEND_NONE_DEPTH_NONE(RenderScript rs) {
        if (rs.mProgramStore_BLEND_NONE_DEPTH_NO_DEPTH == null) {
            Builder builder = new Builder(rs);
            builder.setDepthFunc(DepthFunc.ALWAYS);
            builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ZERO);
            builder.setDitherEnabled(false);
            builder.setDepthMaskEnabled(false);
            rs.mProgramStore_BLEND_NONE_DEPTH_NO_DEPTH = builder.create();
        }
        return rs.mProgramStore_BLEND_NONE_DEPTH_NO_DEPTH;
    }

    public static ProgramStore BLEND_ALPHA_DEPTH_TEST(RenderScript rs) {
        if (rs.mProgramStore_BLEND_ALPHA_DEPTH_TEST == null) {
            Builder builder = new Builder(rs);
            builder.setDepthFunc(DepthFunc.LESS);
            builder.setBlendFunc(BlendSrcFunc.SRC_ALPHA, BlendDstFunc.ONE_MINUS_SRC_ALPHA);
            builder.setDitherEnabled(false);
            builder.setDepthMaskEnabled(true);
            rs.mProgramStore_BLEND_ALPHA_DEPTH_TEST = builder.create();
        }
        return rs.mProgramStore_BLEND_ALPHA_DEPTH_TEST;
    }

    public static ProgramStore BLEND_ALPHA_DEPTH_NONE(RenderScript rs) {
        if (rs.mProgramStore_BLEND_ALPHA_DEPTH_NO_DEPTH == null) {
            Builder builder = new Builder(rs);
            builder.setDepthFunc(DepthFunc.ALWAYS);
            builder.setBlendFunc(BlendSrcFunc.SRC_ALPHA, BlendDstFunc.ONE_MINUS_SRC_ALPHA);
            builder.setDitherEnabled(false);
            builder.setDepthMaskEnabled(false);
            rs.mProgramStore_BLEND_ALPHA_DEPTH_NO_DEPTH = builder.create();
        }
        return rs.mProgramStore_BLEND_ALPHA_DEPTH_NO_DEPTH;
    }
}
