package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.o;

public abstract class ic {
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_FOREVER = 1;
    public static final int TYPE_ONCE = 2;
    private ic rw;

    protected static final boolean bE() {
        return (TMSDKContext.isInitialized() && !o.iZ()) ? true : ir.bU().bE();
    }

    protected <ImplType extends ic> void a(ImplType implType) {
        this.rw = implType;
    }

    public int getSingletonType() {
        return this.rw == null ? 0 : this.rw.getSingletonType();
    }

    public abstract void onCreate(Context context);
}
