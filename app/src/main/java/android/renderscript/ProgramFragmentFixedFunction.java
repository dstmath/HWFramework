package android.renderscript;

import android.renderscript.Program.BaseProgramBuilder;
import android.renderscript.Program.TextureType;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;

public class ProgramFragmentFixedFunction extends ProgramFragment {

    public static class Builder {
        private static final /* synthetic */ int[] -android-renderscript-ProgramFragmentFixedFunction$Builder$EnvModeSwitchesValues = null;
        private static final /* synthetic */ int[] -android-renderscript-ProgramFragmentFixedFunction$Builder$FormatSwitchesValues = null;
        public static final int MAX_TEXTURE = 2;
        int mNumTextures;
        boolean mPointSpriteEnable;
        RenderScript mRS;
        String mShader;
        Slot[] mSlots;
        boolean mVaryingColorEnable;

        public enum EnvMode {
            ;
            
            int mID;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.ProgramFragmentFixedFunction.Builder.EnvMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.ProgramFragmentFixedFunction.Builder.EnvMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ProgramFragmentFixedFunction.Builder.EnvMode.<clinit>():void");
            }

            private EnvMode(int id) {
                this.mID = id;
            }
        }

        public enum Format {
            ;
            
            int mID;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.ProgramFragmentFixedFunction.Builder.Format.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.ProgramFragmentFixedFunction.Builder.Format.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ProgramFragmentFixedFunction.Builder.Format.<clinit>():void");
            }

            private Format(int id) {
                this.mID = id;
            }
        }

        private class Slot {
            EnvMode env;
            Format format;
            final /* synthetic */ Builder this$1;

            Slot(Builder this$1, EnvMode _env, Format _fmt) {
                this.this$1 = this$1;
                this.env = _env;
                this.format = _fmt;
            }
        }

        private static /* synthetic */ int[] -getandroid-renderscript-ProgramFragmentFixedFunction$Builder$EnvModeSwitchesValues() {
            if (-android-renderscript-ProgramFragmentFixedFunction$Builder$EnvModeSwitchesValues != null) {
                return -android-renderscript-ProgramFragmentFixedFunction$Builder$EnvModeSwitchesValues;
            }
            int[] iArr = new int[EnvMode.values().length];
            try {
                iArr[EnvMode.DECAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[EnvMode.MODULATE.ordinal()] = MAX_TEXTURE;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[EnvMode.REPLACE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -android-renderscript-ProgramFragmentFixedFunction$Builder$EnvModeSwitchesValues = iArr;
            return iArr;
        }

        private static /* synthetic */ int[] -getandroid-renderscript-ProgramFragmentFixedFunction$Builder$FormatSwitchesValues() {
            if (-android-renderscript-ProgramFragmentFixedFunction$Builder$FormatSwitchesValues != null) {
                return -android-renderscript-ProgramFragmentFixedFunction$Builder$FormatSwitchesValues;
            }
            int[] iArr = new int[Format.values().length];
            try {
                iArr[Format.ALPHA.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Format.LUMINANCE_ALPHA.ordinal()] = MAX_TEXTURE;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Format.RGB.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Format.RGBA.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -android-renderscript-ProgramFragmentFixedFunction$Builder$FormatSwitchesValues = iArr;
            return iArr;
        }

        private void buildShaderString() {
            this.mShader = "//rs_shader_internal\n";
            this.mShader += "varying lowp vec4 varColor;\n";
            this.mShader += "varying vec2 varTex0;\n";
            this.mShader += "void main() {\n";
            if (this.mVaryingColorEnable) {
                this.mShader += "  lowp vec4 col = varColor;\n";
            } else {
                this.mShader += "  lowp vec4 col = UNI_Color;\n";
            }
            if (this.mNumTextures != 0) {
                if (this.mPointSpriteEnable) {
                    this.mShader += "  vec2 t0 = gl_PointCoord;\n";
                } else {
                    this.mShader += "  vec2 t0 = varTex0.xy;\n";
                }
            }
            for (int i = 0; i < this.mNumTextures; i++) {
                switch (-getandroid-renderscript-ProgramFragmentFixedFunction$Builder$EnvModeSwitchesValues()[this.mSlots[i].env.ordinal()]) {
                    case AudioState.ROUTE_EARPIECE /*1*/:
                        this.mShader += "  col = texture2D(UNI_Tex0, t0);\n";
                        break;
                    case MAX_TEXTURE /*2*/:
                        switch (-getandroid-renderscript-ProgramFragmentFixedFunction$Builder$FormatSwitchesValues()[this.mSlots[i].format.ordinal()]) {
                            case AudioState.ROUTE_EARPIECE /*1*/:
                                this.mShader += "  col.a *= texture2D(UNI_Tex0, t0).a;\n";
                                break;
                            case MAX_TEXTURE /*2*/:
                                this.mShader += "  col.rgba *= texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                            case Engine.DEFAULT_STREAM /*3*/:
                                this.mShader += "  col.rgb *= texture2D(UNI_Tex0, t0).rgb;\n";
                                break;
                            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                                this.mShader += "  col.rgba *= texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                            default:
                                break;
                        }
                    case Engine.DEFAULT_STREAM /*3*/:
                        switch (-getandroid-renderscript-ProgramFragmentFixedFunction$Builder$FormatSwitchesValues()[this.mSlots[i].format.ordinal()]) {
                            case AudioState.ROUTE_EARPIECE /*1*/:
                                this.mShader += "  col.a = texture2D(UNI_Tex0, t0).a;\n";
                                break;
                            case MAX_TEXTURE /*2*/:
                                this.mShader += "  col.rgba = texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                            case Engine.DEFAULT_STREAM /*3*/:
                                this.mShader += "  col.rgb = texture2D(UNI_Tex0, t0).rgb;\n";
                                break;
                            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                                this.mShader += "  col.rgba = texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                            default:
                                break;
                        }
                    default:
                        break;
                }
            }
            this.mShader += "  gl_FragColor = col;\n";
            this.mShader += "}\n";
        }

        public Builder(RenderScript rs) {
            this.mRS = rs;
            this.mSlots = new Slot[MAX_TEXTURE];
            this.mPointSpriteEnable = false;
        }

        public Builder setTexture(EnvMode env, Format fmt, int slot) throws IllegalArgumentException {
            if (slot < 0 || slot >= MAX_TEXTURE) {
                throw new IllegalArgumentException("MAX_TEXTURE exceeded.");
            }
            this.mSlots[slot] = new Slot(this, env, fmt);
            return this;
        }

        public Builder setPointSpriteTexCoordinateReplacement(boolean enable) {
            this.mPointSpriteEnable = enable;
            return this;
        }

        public Builder setVaryingColor(boolean enable) {
            this.mVaryingColorEnable = enable;
            return this;
        }

        public ProgramFragmentFixedFunction create() {
            int i;
            InternalBuilder sb = new InternalBuilder(this.mRS);
            this.mNumTextures = 0;
            for (i = 0; i < MAX_TEXTURE; i++) {
                if (this.mSlots[i] != null) {
                    this.mNumTextures++;
                }
            }
            buildShaderString();
            sb.setShader(this.mShader);
            Type type = null;
            if (!this.mVaryingColorEnable) {
                android.renderscript.Element.Builder b = new android.renderscript.Element.Builder(this.mRS);
                b.add(Element.F32_4(this.mRS), "Color");
                android.renderscript.Type.Builder typeBuilder = new android.renderscript.Type.Builder(this.mRS, b.create());
                typeBuilder.setX(1);
                type = typeBuilder.create();
                sb.addConstant(type);
            }
            for (i = 0; i < this.mNumTextures; i++) {
                sb.addTexture(TextureType.TEXTURE_2D);
            }
            ProgramFragmentFixedFunction pf = sb.create();
            pf.mTextureCount = MAX_TEXTURE;
            if (!this.mVaryingColorEnable) {
                Allocation constantData = Allocation.createTyped(this.mRS, type);
                FieldPacker fp = new FieldPacker(16);
                fp.addF32(new Float4(Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME));
                constantData.setFromFieldPacker(0, fp);
                pf.bindConstants(constantData, 0);
            }
            return pf;
        }
    }

    static class InternalBuilder extends BaseProgramBuilder {
        public InternalBuilder(RenderScript rs) {
            super(rs);
        }

        public ProgramFragmentFixedFunction create() {
            int i;
            this.mRS.validate();
            long[] tmp = new long[((((this.mInputCount + this.mOutputCount) + this.mConstantCount) + this.mTextureCount) * 2)];
            String[] texNames = new String[this.mTextureCount];
            int idx = 0;
            for (i = 0; i < this.mInputCount; i++) {
                int i2 = idx + 1;
                tmp[idx] = (long) ProgramParam.INPUT.mID;
                idx = i2 + 1;
                tmp[i2] = this.mInputs[i].getID(this.mRS);
            }
            for (i = 0; i < this.mOutputCount; i++) {
                i2 = idx + 1;
                tmp[idx] = (long) ProgramParam.OUTPUT.mID;
                idx = i2 + 1;
                tmp[i2] = this.mOutputs[i].getID(this.mRS);
            }
            for (i = 0; i < this.mConstantCount; i++) {
                i2 = idx + 1;
                tmp[idx] = (long) ProgramParam.CONSTANT.mID;
                idx = i2 + 1;
                tmp[i2] = this.mConstants[i].getID(this.mRS);
            }
            for (i = 0; i < this.mTextureCount; i++) {
                i2 = idx + 1;
                tmp[idx] = (long) ProgramParam.TEXTURE_TYPE.mID;
                idx = i2 + 1;
                tmp[i2] = (long) this.mTextureTypes[i].mID;
                texNames[i] = this.mTextureNames[i];
            }
            ProgramFragmentFixedFunction pf = new ProgramFragmentFixedFunction(this.mRS.nProgramFragmentCreate(this.mShader, texNames, tmp), this.mRS);
            initProgram(pf);
            return pf;
        }
    }

    ProgramFragmentFixedFunction(long id, RenderScript rs) {
        super(id, rs);
    }
}
