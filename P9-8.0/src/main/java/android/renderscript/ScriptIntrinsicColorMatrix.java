package android.renderscript;

import android.renderscript.Script.KernelID;
import android.renderscript.Script.LaunchOptions;

public final class ScriptIntrinsicColorMatrix extends ScriptIntrinsic {
    private final Float4 mAdd = new Float4();
    private final Matrix4f mMatrix = new Matrix4f();

    private ScriptIntrinsicColorMatrix(long id, RenderScript rs) {
        super(id, rs);
    }

    @Deprecated
    public static ScriptIntrinsicColorMatrix create(RenderScript rs, Element e) {
        return create(rs);
    }

    public static ScriptIntrinsicColorMatrix create(RenderScript rs) {
        return new ScriptIntrinsicColorMatrix(rs.nScriptIntrinsicCreate(2, 0), rs);
    }

    private void setMatrix() {
        FieldPacker fp = new FieldPacker(64);
        fp.addMatrix(this.mMatrix);
        setVar(0, fp);
    }

    public void setColorMatrix(Matrix4f m) {
        this.mMatrix.load(m);
        setMatrix();
    }

    public void setColorMatrix(Matrix3f m) {
        this.mMatrix.load(m);
        setMatrix();
    }

    public void setAdd(Float4 f) {
        this.mAdd.x = f.x;
        this.mAdd.y = f.y;
        this.mAdd.z = f.z;
        this.mAdd.w = f.w;
        FieldPacker fp = new FieldPacker(16);
        fp.addF32(f.x);
        fp.addF32(f.y);
        fp.addF32(f.z);
        fp.addF32(f.w);
        setVar(1, fp);
    }

    public void setAdd(float r, float g, float b, float a) {
        this.mAdd.x = r;
        this.mAdd.y = g;
        this.mAdd.z = b;
        this.mAdd.w = a;
        FieldPacker fp = new FieldPacker(16);
        fp.addF32(this.mAdd.x);
        fp.addF32(this.mAdd.y);
        fp.addF32(this.mAdd.z);
        fp.addF32(this.mAdd.w);
        setVar(1, fp);
    }

    public void setGreyscale() {
        this.mMatrix.loadIdentity();
        this.mMatrix.set(0, 0, 0.299f);
        this.mMatrix.set(1, 0, 0.587f);
        this.mMatrix.set(2, 0, 0.114f);
        this.mMatrix.set(0, 1, 0.299f);
        this.mMatrix.set(1, 1, 0.587f);
        this.mMatrix.set(2, 1, 0.114f);
        this.mMatrix.set(0, 2, 0.299f);
        this.mMatrix.set(1, 2, 0.587f);
        this.mMatrix.set(2, 2, 0.114f);
        setMatrix();
    }

    public void setYUVtoRGB() {
        this.mMatrix.loadIdentity();
        this.mMatrix.set(0, 0, 1.0f);
        this.mMatrix.set(1, 0, 0.0f);
        this.mMatrix.set(2, 0, 1.13983f);
        this.mMatrix.set(0, 1, 1.0f);
        this.mMatrix.set(1, 1, -0.39465f);
        this.mMatrix.set(2, 1, -0.5806f);
        this.mMatrix.set(0, 2, 1.0f);
        this.mMatrix.set(1, 2, 2.03211f);
        this.mMatrix.set(2, 2, 0.0f);
        setMatrix();
    }

    public void setRGBtoYUV() {
        this.mMatrix.loadIdentity();
        this.mMatrix.set(0, 0, 0.299f);
        this.mMatrix.set(1, 0, 0.587f);
        this.mMatrix.set(2, 0, 0.114f);
        this.mMatrix.set(0, 1, -0.14713f);
        this.mMatrix.set(1, 1, -0.28886f);
        this.mMatrix.set(2, 1, 0.436f);
        this.mMatrix.set(0, 2, 0.615f);
        this.mMatrix.set(1, 2, -0.51499f);
        this.mMatrix.set(2, 2, -0.10001f);
        setMatrix();
    }

    public void forEach(Allocation ain, Allocation aout) {
        forEach(ain, aout, null);
    }

    public void forEach(Allocation ain, Allocation aout, LaunchOptions opt) {
        if (!ain.getElement().isCompatible(Element.U8(this.mRS)) && (ain.getElement().isCompatible(Element.U8_2(this.mRS)) ^ 1) != 0 && (ain.getElement().isCompatible(Element.U8_3(this.mRS)) ^ 1) != 0 && (ain.getElement().isCompatible(Element.U8_4(this.mRS)) ^ 1) != 0 && (ain.getElement().isCompatible(Element.F32(this.mRS)) ^ 1) != 0 && (ain.getElement().isCompatible(Element.F32_2(this.mRS)) ^ 1) != 0 && (ain.getElement().isCompatible(Element.F32_3(this.mRS)) ^ 1) != 0 && (ain.getElement().isCompatible(Element.F32_4(this.mRS)) ^ 1) != 0) {
            throw new RSIllegalArgumentException("Unsupported element type.");
        } else if (aout.getElement().isCompatible(Element.U8(this.mRS)) || (aout.getElement().isCompatible(Element.U8_2(this.mRS)) ^ 1) == 0 || (aout.getElement().isCompatible(Element.U8_3(this.mRS)) ^ 1) == 0 || (aout.getElement().isCompatible(Element.U8_4(this.mRS)) ^ 1) == 0 || (aout.getElement().isCompatible(Element.F32(this.mRS)) ^ 1) == 0 || (aout.getElement().isCompatible(Element.F32_2(this.mRS)) ^ 1) == 0 || (aout.getElement().isCompatible(Element.F32_3(this.mRS)) ^ 1) == 0 || (aout.getElement().isCompatible(Element.F32_4(this.mRS)) ^ 1) == 0) {
            forEach(0, ain, aout, null, opt);
        } else {
            throw new RSIllegalArgumentException("Unsupported element type.");
        }
    }

    public KernelID getKernelID() {
        return createKernelID(0, 3, null, null);
    }
}
