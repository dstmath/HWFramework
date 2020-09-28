package android.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

@Deprecated
public abstract class FragmentContainer {
    public abstract <T extends View> T onFindViewById(int i);

    public abstract boolean onHasView();

    public Fragment instantiate(Context context, String className, Bundle arguments) {
        return Fragment.instantiate(context, className, arguments);
    }
}
