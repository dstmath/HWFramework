package ohos.agp.components;

import ohos.app.Context;

public class PositionLayout extends AbsoluteLayout {
    public PositionLayout(Context context) {
        super(context, null);
    }

    public PositionLayout(Context context, AttrSet attrSet) {
        super(context, attrSet, "PositionLayoutDefaultStyle");
    }

    public PositionLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }
}
