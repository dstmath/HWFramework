package android.view;

import android.view.ViewGroup;

public interface ViewManager {
    void addView(View view, ViewGroup.LayoutParams layoutParams);

    void removeView(View view);

    void updateViewLayout(View view, ViewGroup.LayoutParams layoutParams);
}
