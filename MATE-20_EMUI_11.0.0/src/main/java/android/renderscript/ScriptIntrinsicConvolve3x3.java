package android.renderscript;

import android.renderscript.Script;

public final class ScriptIntrinsicConvolve3x3 extends ScriptIntrinsic {
    private Allocation mInput;
    private final float[] mValues = new float[9];

    private ScriptIntrinsicConvolve3x3(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicConvolve3x3 create(RenderScript rs, Element e) {
        float[] f = {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        if (e.isCompatible(Element.U8(rs)) || e.isCompatible(Element.U8_2(rs)) || e.isCompatible(Element.U8_3(rs)) || e.isCompatible(Element.U8_4(rs)) || e.isCompatible(Element.F32(rs)) || e.isCompatible(Element.F32_2(rs)) || e.isCompatible(Element.F32_3(rs)) || e.isCompatible(Element.F32_4(rs))) {
            ScriptIntrinsicConvolve3x3 si = new ScriptIntrinsicConvolve3x3(rs.nScriptIntrinsicCreate(1, e.getID(rs)), rs);
            si.setCoefficients(f);
            return si;
        }
        throw new RSIllegalArgumentException("Unsupported element type.");
    }

    public void setInput(Allocation ain) {
        this.mInput = ain;
        setVar(1, ain);
    }

    public void setCoefficients(float[] v) {
        FieldPacker fp = new FieldPacker(36);
        int ct = 0;
        while (true) {
            float[] fArr = this.mValues;
            if (ct < fArr.length) {
                fArr[ct] = v[ct];
                fp.addF32(fArr[ct]);
                ct++;
            } else {
                setVar(0, fp);
                return;
            }
        }
    }

    public void forEach(Allocation aout) {
        forEach(0, (Allocation) null, aout, (FieldPacker) null);
    }

    public void forEach(Allocation aout, Script.LaunchOptions opt) {
        forEach(0, (Allocation) null, aout, (FieldPacker) null, opt);
    }

    public Script.KernelID getKernelID() {
        return createKernelID(0, 2, null, null);
    }

    public Script.FieldID getFieldID_Input() {
        return createFieldID(1, null);
    }
}
