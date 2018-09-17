package android.view;

public interface IHwPointEventFilter {
    MotionEvent convertPointEvent(MotionEvent motionEvent);

    MotionEvent getAdditionalEvent();

    void handleDownResult(MotionEvent motionEvent, boolean z);
}
