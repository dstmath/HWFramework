package huawei.android.widget.plume.action;

import android.content.Context;
import android.view.View;

public abstract class PlumeAction {
    protected Context mContext;
    protected View mTarget;

    public abstract void apply(String str, String str2);

    protected PlumeAction(Context context, View view) {
        this.mContext = context;
        this.mTarget = view;
    }
}
