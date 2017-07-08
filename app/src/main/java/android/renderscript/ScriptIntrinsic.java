package android.renderscript;

public abstract class ScriptIntrinsic extends Script {
    ScriptIntrinsic(long id, RenderScript rs) {
        super(id, rs);
        if (id == 0) {
            throw new RSRuntimeException("Loading of ScriptIntrinsic failed.");
        }
    }
}
