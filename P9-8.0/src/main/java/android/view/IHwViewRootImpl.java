package android.view;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager.LayoutParams;

public interface IHwViewRootImpl {
    void clearDisplayPoint();

    boolean filterDecorPointerEvent(Context context, MotionEvent motionEvent, int i, LayoutParams layoutParams, Display display);

    MotionEvent getRedispatchEvent();

    boolean interceptMotionEvent(View view, MotionEvent motionEvent);

    void setRealSize(Point point);

    boolean shouldQueueInputEvent(InputEvent inputEvent, Context context, View view, LayoutParams layoutParams);
}
