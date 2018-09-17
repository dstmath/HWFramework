package android.renderscript;

import android.renderscript.Program.BaseProgramBuilder;

public class ProgramVertex extends Program {

    public static class Builder extends BaseProgramBuilder {
        public Builder(RenderScript rs) {
            super(rs);
        }

        public Builder addInput(Element e) throws IllegalStateException {
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

        public ProgramVertex create() {
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
            ProgramVertex pv = new ProgramVertex(this.mRS.nProgramVertexCreate(this.mShader, texNames, tmp), this.mRS);
            initProgram(pv);
            return pv;
        }
    }

    ProgramVertex(long id, RenderScript rs) {
        super(id, rs);
    }

    public int getInputCount() {
        return this.mInputs != null ? this.mInputs.length : 0;
    }

    public Element getInput(int slot) {
        if (slot >= 0 && slot < this.mInputs.length) {
            return this.mInputs[slot];
        }
        throw new IllegalArgumentException("Slot ID out of range.");
    }
}
