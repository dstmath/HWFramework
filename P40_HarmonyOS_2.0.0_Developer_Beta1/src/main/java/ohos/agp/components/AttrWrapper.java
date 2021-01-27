package ohos.agp.components;

import ohos.app.Context;

public class AttrWrapper {
    private final Attr mAttrImpl;

    public AttrWrapper(Context context, Attr attr) {
        this.mAttrImpl = attr;
        this.mAttrImpl.setContext(context);
    }

    public int getIntegerValue() {
        return this.mAttrImpl.getIntegerValue();
    }

    public int getDimensionValue() {
        return this.mAttrImpl.getDimensionValue();
    }
}
