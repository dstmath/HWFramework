package android.renderscript;

import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;

public final class ScriptIntrinsicBlur extends ScriptIntrinsic {
    private Allocation mInput;
    private final float[] mValues;

    private ScriptIntrinsicBlur(long id, RenderScript rs) {
        super(id, rs);
        this.mValues = new float[9];
    }

    public static ScriptIntrinsicBlur create(RenderScript rs, Element e) {
        if (e.isCompatible(Element.U8_4(rs)) || e.isCompatible(Element.U8(rs))) {
            ScriptIntrinsicBlur sib = new ScriptIntrinsicBlur(rs.nScriptIntrinsicCreate(5, e.getID(rs)), rs);
            sib.setRadius(5.0f);
            return sib;
        }
        throw new RSIllegalArgumentException("Unsuported element type.");
    }

    public void setInput(Allocation ain) {
        this.mInput = ain;
        setVar(1, (BaseObj) ain);
    }

    public void setRadius(float radius) {
        if (radius <= 0.0f || radius > 25.0f) {
            throw new RSIllegalArgumentException("Radius out of range (0 < r <= 25).");
        }
        setVar(0, radius);
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
