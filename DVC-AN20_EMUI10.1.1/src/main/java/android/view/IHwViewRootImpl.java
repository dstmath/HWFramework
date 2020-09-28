package android.view;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public interface IHwViewRootImpl {
    void checkOldestInputTime();

    void clearDisplayPoint();

    boolean filterDecorPointerEvent(Context context, MotionEvent motionEvent, int i, WindowManager.LayoutParams layoutParams, Display display);

    MotionEvent getRedispatchEvent();

    boolean interceptMotionEvent(View view, MotionEvent motionEvent);

    void onBatchedInputConsumed(long j);

    void onChgCallBackCountsChanged(int i);

    void processJank(boolean z, long[] jArr, String str, int i);

    void setFrameDelayTime(long j);

    void setIsFirstFrame(boolean z);

    void setIsNeedDraw(boolean z);

    void setLastFrameDoneTime(long j);

    void setRealFrameTime(long j);

    void setRealSize(Point point);

    boolean shouldQueueInputEvent(InputEvent inputEvent, Context context, View view, WindowManager.LayoutParams layoutParams);

    void traceInputEventInfo(InputEvent inputEvent);

    void updateDoframeStatus(boolean z);

    void updateLastTraversal(boolean z);

    void updateOldestInputTime(long j);
}
