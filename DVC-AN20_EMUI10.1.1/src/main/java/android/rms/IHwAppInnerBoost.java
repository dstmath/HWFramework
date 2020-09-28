package android.rms;

import android.view.InputEvent;

public interface IHwAppInnerBoost {
    void initialize(String str);

    void onInputEvent(InputEvent inputEvent);

    void onJitter(long j);

    void onTraversal();
}
