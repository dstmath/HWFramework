package android.view;

import android.graphics.Insets;

public interface WindowInsetsAnimationController {
    void changeInsets(Insets insets);

    void finish(int i);

    Insets getCurrentInsets();

    Insets getHiddenStateInsets();

    Insets getShownStateInsets();

    int getTypes();
}
