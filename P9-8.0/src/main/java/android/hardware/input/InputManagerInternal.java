package android.hardware.input;

import android.hardware.display.DisplayViewport;
import android.view.InputEvent;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import java.util.List;

public abstract class InputManagerInternal {
    public abstract boolean injectInputEvent(InputEvent inputEvent, int i, int i2);

    public abstract void onInputMethodSubtypeChanged(int i, InputMethodInfo inputMethodInfo, InputMethodSubtype inputMethodSubtype);

    public abstract void setDisplayViewports(DisplayViewport displayViewport, DisplayViewport displayViewport2, List<DisplayViewport> list);

    public abstract void setInteractive(boolean z);

    public abstract void setPulseGestureEnabled(boolean z);

    public abstract void toggleCapsLock(int i);
}
