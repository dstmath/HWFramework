package ohos.agp.window.aspbshell;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public class AGPWindowInternal {
    private Context mContext;

    public AGPWindowInternal(Context context) {
        this.mContext = context;
    }

    public void setContentView(View view) {
        if (view != null) {
            Context context = this.mContext;
            if (context instanceof Activity) {
                ((Activity) context).setContentView(view);
                return;
            }
            throw new IllegalArgumentException("Context is not an activity.");
        }
    }
}
