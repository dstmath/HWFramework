package ohos.multimodalinput.eventimpl;

import android.view.KeyEvent;
import android.view.MotionEvent;

public interface HostEventAdapter {
    KeyEvent getHostKeyEvent();

    MotionEvent getHostMotionEvent();
}
