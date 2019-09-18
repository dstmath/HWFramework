package android.view;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public interface IHwViewRootImpl {
    void clearDisplayPoint();

    boolean filterDecorPointerEvent(Context context, MotionEvent motionEvent, int i, WindowManager.LayoutParams layoutParams, Display display);

    MotionEvent getRedispatchEvent();

    boolean interceptMotionEvent(View view, MotionEvent motionEvent);

    void setRealSize(Point point);

    boolean shouldQueueInputEvent(InputEvent inputEvent, Context context, View view, WindowManager.LayoutParams layoutParams);
}
