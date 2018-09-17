package android.renderscript;

import android.provider.BrowserContract.Bookmarks;
import android.renderscript.Program.BaseProgramBuilder;

public class ProgramVertexFixedFunction extends ProgramVertex {

    public static class Builder {
        RenderScript mRS;
        String mShader;
        boolean mTextureMatrixEnable;

        public Builder(RenderScript rs) {
            this.mRS = rs;
        }

        public Builder setTextureMatrixEnable(boolean enable) {
            this.mTextureMatrixEnable = enable;
            return this;
        }

        static Type getConstantInputType(RenderScript rs) {
            android.renderscript.Element.Builder b = new android.renderscript.Element.Builder(rs);
            b.add(Element.MATRIX4X4(rs), "MV");
            b.add(Element.MATRIX4X4(rs), "P");
            b.add(Element.MATRIX4X4(rs), "TexMatrix");
            b.add(Element.MATRIX4X4(rs), "MVP");
            android.renderscript.Type.Builder typeBuilder = new android.renderscript.Type.Builder(rs, b.create());
            typeBuilder.setX(1);
            return typeBuilder.create();
        }

        private void buildShaderString() {
            this.mShader = "//rs_shader_internal\n";
            this.mShader += "varying vec4 varColor;\n";
            this.mShader += "varying vec2 varTex0;\n";
            this.mShader += "void main() {\n";
            this.mShader += "  gl_Position = UNI_MVP * ATTRIB_position;\n";
            this.mShader += "  gl_PointSize = 1.0;\n";
            this.mShader += "  varColor = ATTRIB_color;\n";
            if (this.mTextureMatrixEnable) {
                this.mShader += "  varTex0 = (UNI_TexMatrix * vec4(ATTRIB_texture0, 0.0, 1.0)).xy;\n";
            } else {
                this.mShader += "  varTex0 = ATTRIB_texture0;\n";
            }
            this.mShader += "}\n";
        }

        public ProgramVertexFixedFunction create() {
            buildShaderString();
            InternalBuilder sb = new InternalBuilder(this.mRS);
            sb.setShader(this.mShader);
            sb.addConstant(getConstantInputType(this.mRS));
            android.renderscript.Element.Builder b = new android.renderscript.Element.Builder(this.mRS);
            b.add(Element.F32_4(this.mRS), Bookmarks.POSITION);
            b.add(Element.F32_4(this.mRS), "color");
            b.add(Element.F32_3(this.mRS), "normal");
            b.add(Element.F32_2(this.mRS), "texture0");
            sb.addInput(b.create());
            return sb.create();
        }
    }

    public static class Constants {
        static final int MODELVIEW_OFFSET = 0;
        static final int PROJECTION_OFFSET = 16;
        static final int TEXTURE_OFFSET = 32;
        Allocation mAlloc;
        private FieldPacker mIOBuffer;
        Matrix4f mModel = new Matrix4f();
        Matrix4f mProjection = new Matrix4f();
        Matrix4f mTexture = new Matrix4f();

        Allocation getAllocation() {
            return this.mAlloc;
        }

        public Constants(RenderScript rs) {
            Type constInputType = Builder.getConstantInputType(rs);
            this.mAlloc = Allocation.createTyped(rs, constInputType);
            this.mIOBuffer = new FieldPacker(constInputType.getElement().getBytesSize() * constInputType.getCount());
            setModelview(new Matrix4f());
            setProjection(new Matrix4f());
            setTexture(new Matrix4f());
        }

        public void destroy() {
            this.mAlloc.destroy();
            this.mAlloc = null;
        }

        private void addToBuffer(int offset, Matrix4f m) {
            this.mIOBuffer.reset(offset);
            for (int i = 0; i < 16; i++) {
                this.mIOBuffer.addF32(m.mMat[i]);
            }
            this.mIOBuffer.reset(this.mIOBuffer.getData().length);
            this.mAlloc.setFromFieldPacker(0, this.mIOBuffer);
        }

        public void setModelview(Matrix4f m) {
            this.mModel.load(m);
            addToBuffer(0, m);
        }

        public void setProjection(Matrix4f m) {
            this.mProjection.load(m);
            addToBuffer(64, m);
        }

        public void setTexture(Matrix4f m) {
            this.mTexture.load(m);
            addToBuffer(128, m);
        }
    }

    static class InternalBuilder extends BaseProgramBuilder {
        public InternalBuilder(RenderScript rs) {
            super(rs);
        }

        public InternalBuilder addInput(Element e) throws IllegalStateException {
            if (this.mInputCount >= 8) {
                throw new RSIllegalArgumentException("Max input count exceeded.");
            } else if (e.isComplex()) {
                throw new RSIllegalArgumentException("Complex elements not allowed.");
            } else {
                Element[] elementArr = this.mInputs;
                int i = this.mInputCount;
                this.mInputCount = i + 1;
                elementArr[i] = e;
                return this;
            }
        }

        public ProgramVertexFixedFunction create() {
            int i;
            int i2;
            this.mRS.validate();
            long[] tmp = new long[((((this.mInputCount + this.mOutputCount) + this.mConstantCount) + this.mTextureCount) * 2)];
            String[] texNames = new String[this.mTextureCount];
            int idx = 0;
            for (i = 0; i < this.mInputCount; i++) {
                i2 = idx + 1;
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
            ProgramVertexFixedFunction pv = new ProgramVertexFixedFunction(this.mRS.nProgramVertexCreate(this.mShader, texNames, tmp), this.mRS);
            initProgram(pv);
            return pv;
        }
    }

    ProgramVertexFixedFunction(long id, RenderScript rs) {
        super(id, rs);
    }

    public void bindConstants(Constants va) {
        this.mRS.validate();
        bindConstants(va.getAllocation(), 0);
    }
}
