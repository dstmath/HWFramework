package android.view;

import android.content.Context;
import android.view.WindowManager.LayoutParams;

public interface IHwViewRootImpl {
    void clearDisplayPoint();

    boolean filterDecorPointerEvent(Context context, MotionEvent motionEvent, int i, LayoutParams layoutParams, Display display);

    boolean shouldQueueInputEvent(InputEvent inputEvent, Context context, View view);
}
