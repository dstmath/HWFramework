package huawei.com.android.internal.widget;

import android.view.ActionMode;

public interface ActionModeView {
    void animateToVisibility(int i);

    void cancelVisibilityAnimation();

    void closeMode();

    void initForMode(ActionMode actionMode);

    void killMode();
}
