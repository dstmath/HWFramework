package android.renderscript;

import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;

public final class ScriptIntrinsicResize extends ScriptIntrinsic {
    private Allocation mInput;

    private ScriptIntrinsicResize(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicResize create(RenderScript rs) {
        return new ScriptIntrinsicResize(rs.nScriptIntrinsicCreate(12, 0), rs);
    }

    public void setInput(Allocation ain) {
        Element e = ain.getElement();
        if (e.isCompatible(Element.U8(this.mRS)) || e.isCompatible(Element.U8_2(this.mRS)) || e.isCompatible(Element.U8_3(this.mRS)) || e.isCompatible(Element.U8_4(this.mRS)) || e.isCompatible(Element.F32(this.mRS)) || e.isCompatible(Element.F32_2(this.mRS)) || e.isCompatible(Element.F32_3(this.mRS)) || e.isCompatible(Element.F32_4(this.mRS))) {
            this.mInput = ain;
            setVar(0, (BaseObj) ain);
            return;
        }
        throw new RSIllegalArgumentException("Unsuported element type.");
    }

    public FieldID getFieldID_Input() {
        return createFieldID(0, null);
    }

    public void forEach_bicubic(Allocation aout) {
        if (aout == this.mInput) {
            throw new RSIllegalArgumentException("Output cannot be same as Input.");
        }
        forEach_bicubic(aout, null);
    }

    public void forEach_bicubic(Allocation aout, LaunchOptions opt) {
        forEach(0, (Allocation) null, aout, null, opt);
    }

    public KernelID getKernelID_bicubic() {
        return createKernelID(0, 2, null, null);
    }
}
