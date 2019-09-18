package android.renderscript;

import android.renderscript.Element;
import android.renderscript.Program;
import android.renderscript.Type;

public class ProgramFragmentFixedFunction extends ProgramFragment {

    public static class Builder {
        public static final int MAX_TEXTURE = 2;
        int mNumTextures;
        boolean mPointSpriteEnable = false;
        RenderScript mRS;
        String mShader;
        Slot[] mSlots = new Slot[2];
        boolean mVaryingColorEnable;

        public enum EnvMode {
            REPLACE(1),
            MODULATE(2),
            DECAL(3);
            
            int mID;

            private EnvMode(int id) {
                this.mID = id;
            }
        }

        public enum Format {
            ALPHA(1),
            LUMINANCE_ALPHA(2),
            RGB(3),
            RGBA(4);
            
            int mID;

            private Format(int id) {
                this.mID = id;
            }
        }

        private class Slot {
            EnvMode env;
            Format format;

            Slot(EnvMode _env, Format _fmt) {
                this.env = _env;
                this.format = _fmt;
            }
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
                switch (this.mSlots[i].env) {
                    case REPLACE:
                        switch (this.mSlots[i].format) {
                            case ALPHA:
                                this.mShader += "  col.a = texture2D(UNI_Tex0, t0).a;\n";
                                break;
                            case LUMINANCE_ALPHA:
                                this.mShader += "  col.rgba = texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                            case RGB:
                                this.mShader += "  col.rgb = texture2D(UNI_Tex0, t0).rgb;\n";
                                break;
                            case RGBA:
                                this.mShader += "  col.rgba = texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                        }
                    case MODULATE:
                        switch (this.mSlots[i].format) {
                            case ALPHA:
                                this.mShader += "  col.a *= texture2D(UNI_Tex0, t0).a;\n";
                                break;
                            case LUMINANCE_ALPHA:
                                this.mShader += "  col.rgba *= texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                            case RGB:
                                this.mShader += "  col.rgb *= texture2D(UNI_Tex0, t0).rgb;\n";
                                break;
                            case RGBA:
                                this.mShader += "  col.rgba *= texture2D(UNI_Tex0, t0).rgba;\n";
                                break;
                        }
                    case DECAL:
                        this.mShader += "  col = texture2D(UNI_Tex0, t0);\n";
                        break;
                }
            }
            this.mShader += "  gl_FragColor = col;\n";
            this.mShader += "}\n";
        }

        public Builder(RenderScript rs) {
            this.mRS = rs;
        }

        public Builder setTexture(EnvMode env, Format fmt, int slot) throws IllegalArgumentException {
            if (slot < 0 || slot >= 2) {
                throw new IllegalArgumentException("MAX_TEXTURE exceeded.");
            }
            this.mSlots[slot] = new Slot(env, fmt);
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
            InternalBuilder sb = new InternalBuilder(this.mRS);
            this.mNumTextures = 0;
            for (int i = 0; i < 2; i++) {
                if (this.mSlots[i] != null) {
                    this.mNumTextures++;
                }
            }
            buildShaderString();
            sb.setShader(this.mShader);
            Type constType = null;
            if (!this.mVaryingColorEnable) {
                Element.Builder b = new Element.Builder(this.mRS);
                b.add(Element.F32_4(this.mRS), "Color");
                Type.Builder typeBuilder = new Type.Builder(this.mRS, b.create());
                typeBuilder.setX(1);
                constType = typeBuilder.create();
                sb.addConstant(constType);
            }
            for (int i2 = 0; i2 < this.mNumTextures; i2++) {
                sb.addTexture(Program.TextureType.TEXTURE_2D);
            }
            ProgramFragmentFixedFunction pf = sb.create();
            pf.mTextureCount = 2;
            if (!this.mVaryingColorEnable) {
                Allocation constantData = Allocation.createTyped(this.mRS, constType);
                FieldPacker fp = new FieldPacker(16);
                fp.addF32(new Float4(1.0f, 1.0f, 1.0f, 1.0f));
                constantData.setFromFieldPacker(0, fp);
                pf.bindConstants(constantData, 0);
            }
            return pf;
        }
    }

    static class InternalBuilder extends Program.BaseProgramBuilder {
        public InternalBuilder(RenderScript rs) {
            super(rs);
        }

        public ProgramFragmentFixedFunction create() {
            this.mRS.validate();
            long[] tmp = new long[((this.mInputCount + this.mOutputCount + this.mConstantCount + this.mTextureCount) * 2)];
            String[] texNames = new String[this.mTextureCount];
            int i = 0;
            int idx = 0;
            for (int i2 = 0; i2 < this.mInputCount; i2++) {
                int idx2 = idx + 1;
                tmp[idx] = (long) Program.ProgramParam.INPUT.mID;
                idx = idx2 + 1;
                tmp[idx2] = this.mInputs[i2].getID(this.mRS);
            }
            for (int i3 = 0; i3 < this.mOutputCount; i3++) {
                int idx3 = idx + 1;
                tmp[idx] = (long) Program.ProgramParam.OUTPUT.mID;
                idx = idx3 + 1;
                tmp[idx3] = this.mOutputs[i3].getID(this.mRS);
            }
            for (int i4 = 0; i4 < this.mConstantCount; i4++) {
                int idx4 = idx + 1;
                tmp[idx] = (long) Program.ProgramParam.CONSTANT.mID;
                idx = idx4 + 1;
                tmp[idx4] = this.mConstants[i4].getID(this.mRS);
            }
            while (true) {
                int i5 = i;
                if (i5 < this.mTextureCount) {
                    int idx5 = idx + 1;
                    tmp[idx] = (long) Program.ProgramParam.TEXTURE_TYPE.mID;
                    idx = idx5 + 1;
                    tmp[idx5] = (long) this.mTextureTypes[i5].mID;
                    texNames[i5] = this.mTextureNames[i5];
                    i = i5 + 1;
                } else {
                    ProgramFragmentFixedFunction pf = new ProgramFragmentFixedFunction(this.mRS.nProgramFragmentCreate(this.mShader, texNames, tmp), this.mRS);
                    initProgram(pf);
                    return pf;
                }
            }
        }
    }

    ProgramFragmentFixedFunction(long id, RenderScript rs) {
        super(id, rs);
    }
}
