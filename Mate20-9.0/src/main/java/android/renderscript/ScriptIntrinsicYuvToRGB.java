package android.renderscript;

import android.renderscript.Script;

public final class ScriptIntrinsicYuvToRGB extends ScriptIntrinsic {
    private Allocation mInput;

    ScriptIntrinsicYuvToRGB(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicYuvToRGB create(RenderScript rs, Element e) {
        return new ScriptIntrinsicYuvToRGB(rs.nScriptIntrinsicCreate(6, e.getID(rs)), rs);
    }

    public void setInput(Allocation ain) {
        this.mInput = ain;
        setVar(0, (BaseObj) ain);
    }

    public void forEach(Allocation aout) {
        forEach(0, (Allocation) null, aout, (FieldPacker) null);
    }

    public Script.KernelID getKernelID() {
        return createKernelID(0, 2, null, null);
    }

    public Script.FieldID getFieldID_Input() {
        return createFieldID(0, null);
    }
}
