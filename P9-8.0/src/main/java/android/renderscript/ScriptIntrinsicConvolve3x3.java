package android.renderscript;

import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;

public final class ScriptIntrinsicConvolve3x3 extends ScriptIntrinsic {
    private Allocation mInput;
    private final float[] mValues = new float[9];

    private ScriptIntrinsicConvolve3x3(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicConvolve3x3 create(RenderScript rs, Element e) {
        float[] f = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        if (e.isCompatible(Element.U8(rs)) || (e.isCompatible(Element.U8_2(rs)) ^ 1) == 0 || (e.isCompatible(Element.U8_3(rs)) ^ 1) == 0 || (e.isCompatible(Element.U8_4(rs)) ^ 1) == 0 || (e.isCompatible(Element.F32(rs)) ^ 1) == 0 || (e.isCompatible(Element.F32_2(rs)) ^ 1) == 0 || (e.isCompatible(Element.F32_3(rs)) ^ 1) == 0 || (e.isCompatible(Element.F32_4(rs)) ^ 1) == 0) {
            ScriptIntrinsicConvolve3x3 si = new ScriptIntrinsicConvolve3x3(rs.nScriptIntrinsicCreate(1, e.getID(rs)), rs);
            si.setCoefficients(f);
            return si;
        }
        throw new RSIllegalArgumentException("Unsupported element type.");
    }

    public void setInput(Allocation ain) {
        this.mInput = ain;
        setVar(1, (BaseObj) ain);
    }

    public void setCoefficients(float[] v) {
        FieldPacker fp = new FieldPacker(36);
        for (int ct = 0; ct < this.mValues.length; ct++) {
            this.mValues[ct] = v[ct];
            fp.addF32(this.mValues[ct]);
        }
        setVar(0, fp);
    }

    public void forEach(Allocation aout) {
        forEach(0, (Allocation) null, aout, null);
    }

    public void forEach(Allocation aout, LaunchOptions opt) {
        forEach(0, (Allocation) null, aout, null, opt);
    }

    public KernelID getKernelID() {
        return createKernelID(0, 2, null, null);
    }

    public FieldID getFieldID_Input() {
        return createFieldID(1, null);
    }
}
