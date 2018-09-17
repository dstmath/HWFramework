package android.renderscript;

import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;

public final class ScriptIntrinsicBlur extends ScriptIntrinsic {
    private Allocation mInput;
    private final float[] mValues = new float[9];

    private ScriptIntrinsicBlur(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicBlur create(RenderScript rs, Element e) {
        if (e.isCompatible(Element.U8_4(rs)) || (e.isCompatible(Element.U8(rs)) ^ 1) == 0) {
            ScriptIntrinsicBlur sib = new ScriptIntrinsicBlur(rs.nScriptIntrinsicCreate(5, e.getID(rs)), rs);
            sib.setRadius(5.0f);
            return sib;
        }
        throw new RSIllegalArgumentException("Unsupported element type.");
    }

    public void setInput(Allocation ain) {
        if (ain.getType().getY() == 0) {
            throw new RSIllegalArgumentException("Input set to a 1D Allocation");
        }
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
        if (aout.getType().getY() == 0) {
            throw new RSIllegalArgumentException("Output is a 1D Allocation");
        }
        forEach(0, (Allocation) null, aout, null);
    }

    public void forEach(Allocation aout, LaunchOptions opt) {
        if (aout.getType().getY() == 0) {
            throw new RSIllegalArgumentException("Output is a 1D Allocation");
        }
        forEach(0, (Allocation) null, aout, null, opt);
    }

    public KernelID getKernelID() {
        return createKernelID(0, 2, null, null);
    }

    public FieldID getFieldID_Input() {
        return createFieldID(1, null);
    }
}
