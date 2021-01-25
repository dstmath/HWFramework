package android.view;

import android.view.WindowInsets;

public interface WindowInsetsController {
    void controlWindowInsetsAnimation(int i, WindowInsetsAnimationControlListener windowInsetsAnimationControlListener);

    void hide(int i);

    void show(int i);

    default void controlInputMethodAnimation(WindowInsetsAnimationControlListener listener) {
        controlWindowInsetsAnimation(WindowInsets.Type.ime(), listener);
    }

    default void showInputMethod() {
        show(WindowInsets.Type.ime());
    }

    default void hideInputMethod() {
        hide(WindowInsets.Type.ime());
    }
}
