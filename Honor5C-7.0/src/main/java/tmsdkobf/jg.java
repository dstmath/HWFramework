package tmsdkobf;

import android.content.Context;

/* compiled from: Unknown */
public abstract class jg {
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_FOREVER = 1;
    public static final int TYPE_ONCE = 2;
    private jg tX;

    protected static final boolean cl() {
        return jw.cH().cl();
    }

    protected <ImplType extends jg> void a(ImplType implType) {
        this.tX = implType;
    }

    public int getSingletonType() {
        return this.tX == null ? TYPE_AUTO : this.tX.getSingletonType();
    }

    public abstract void onCreate(Context context);
}
