package android.view;

import android.view.ViewGroup.LayoutParams;

public interface ViewManager {
    void addView(View view, LayoutParams layoutParams);

    void removeView(View view);

    void updateViewLayout(View view, LayoutParams layoutParams);
}
