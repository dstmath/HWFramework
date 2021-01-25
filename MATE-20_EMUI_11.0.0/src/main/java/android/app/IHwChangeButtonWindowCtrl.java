package android.app;

import android.graphics.Rect;
import android.view.WindowManager;

public interface IHwChangeButtonWindowCtrl {
    void addView(WindowManager windowManager, Rect rect);

    void ceateView();

    void destoryView();

    boolean hasView();

    boolean hasViewAdd();

    boolean isLongScreenPhone();

    boolean isToOtherApp();

    boolean isViewHide();

    void removeView(WindowManager windowManager);

    void setViewHide(boolean z);

    void showChangeButtonWindow(Rect rect);

    void updateView(Rect rect);
}
