package android.app;

import android.view.View;

public abstract class FragmentContainer {
    public abstract View onFindViewById(int i);

    public abstract boolean onHasView();
}
