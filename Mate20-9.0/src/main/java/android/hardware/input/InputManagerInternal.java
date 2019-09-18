package android.hardware.input;

import android.hardware.display.DisplayViewport;
import android.view.InputEvent;
import java.util.List;

public abstract class InputManagerInternal {
    public abstract boolean injectInputEvent(InputEvent inputEvent, int i, int i2);

    public abstract boolean injectInputEvent(InputEvent inputEvent, int i, int i2, int i3);

    public abstract void onKeyguardStateChanged(boolean z);

    public abstract void setDisplayMode(int i, int i2, int i3, int i4);

    public abstract void setDisplayViewports(DisplayViewport displayViewport, DisplayViewport displayViewport2, List<DisplayViewport> list);

    public abstract void setInputViewOrientation(int i);

    public abstract void setInteractive(boolean z);

    public abstract void setMirrorLinkInputStatus(boolean z);

    public abstract void setPulseGestureEnabled(boolean z);

    public abstract void toggleCapsLock(int i);
}
