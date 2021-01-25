package android.renderscript;

import android.renderscript.Script;

public final class ScriptIntrinsicBlur extends ScriptIntrinsic {
    private Allocation mInput;
    private final float[] mValues = new float[9];

    private ScriptIntrinsicBlur(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicBlur create(RenderScript rs, Element e) {
        if (e.isCompatible(Element.U8_4(rs)) || e.isCompatible(Element.U8(rs))) {
            ScriptIntrinsicBlur sib = new ScriptIntrinsicBlur(rs.nScriptIntrinsicCreate(5, e.getID(rs)), rs);
            sib.setRadius(5.0f);
            return sib;
        }
        throw new RSIllegalArgumentException("Unsupported element type.");
    }

    public void setInput(Allocation ain) {
        if (ain.getType().getY() != 0) {
            Element e = ain.getElement();
            if (e.isCompatible(Element.U8_4(this.mRS)) || e.isCompatible(Element.U8(this.mRS))) {
                this.mInput = ain;
                setVar(1, ain);
                return;
            }
            throw new RSIllegalArgumentException("Unsupported element type.");
        }
        throw new RSIllegalArgumentException("Input set to a 1D Allocation");
    }

    public void setRadius(float radius) {
        if (radius <= 0.0f || radius > 25.0f) {
            throw new RSIllegalArgumentException("Radius out of range (0 < r <= 25).");
        }
        setVar(0, radius);
    }

    public void forEach(Allocation aout) {
        if (aout.getType().getY() != 0) {
            forEach(0, (Allocation) null, aout, (FieldPacker) null);
            return;
        }
        throw new RSIllegalArgumentException("Output is a 1D Allocation");
    }

    public void forEach(Allocation aout, Script.LaunchOptions opt) {
        if (aout.getType().getY() != 0) {
            forEach(0, (Allocation) null, aout, (FieldPacker) null, opt);
            return;
        }
        throw new RSIllegalArgumentException("Output is a 1D Allocation");
    }

    public Script.KernelID getKernelID() {
        return createKernelID(0, 2, null, null);
    }

    public Script.FieldID getFieldID_Input() {
        return createFieldID(1, null);
    }
}
