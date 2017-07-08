package android.renderscript;

import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;

public final class ScriptIntrinsicConvolve5x5 extends ScriptIntrinsic {
    private Allocation mInput;
    private final float[] mValues;

    private ScriptIntrinsicConvolve5x5(long id, RenderScript rs) {
        super(id, rs);
        this.mValues = new float[25];
    }

    public static ScriptIntrinsicConvolve5x5 create(RenderScript rs, Element e) {
        if (e.isCompatible(Element.U8(rs)) || e.isCompatible(Element.U8_2(rs)) || e.isCompatible(Element.U8_3(rs)) || e.isCompatible(Element.U8_4(rs)) || e.isCompatible(Element.F32(rs)) || e.isCompatible(Element.F32_2(rs)) || e.isCompatible(Element.F32_3(rs)) || e.isCompatible(Element.F32_4(rs))) {
            return new ScriptIntrinsicConvolve5x5(rs.nScriptIntrinsicCreate(4, e.getID(rs)), rs);
        }
        throw new RSIllegalArgumentException("Unsuported element type.");
    }

    public void setInput(Allocation ain) {
        this.mInput = ain;
        setVar(1, (BaseObj) ain);
    }

    public void setCoefficients(float[] v) {
        FieldPacker fp = new FieldPacker(100);
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
