package ohos.agp.components;

import ohos.app.Context;

public final class Space extends Component {
    public Space(Context context) {
        this(context, null);
    }

    public Space(Context context, AttrSet attrSet) {
        this(context, attrSet, "SpaceDefaultStyle");
    }

    public Space(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        if (getVisibility() == 0) {
            setVisibility(4);
        }
    }
}
