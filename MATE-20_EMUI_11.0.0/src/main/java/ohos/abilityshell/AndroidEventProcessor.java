package ohos.abilityshell;

import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.Optional;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;

public class AndroidEventProcessor {
    public static Optional<MultimodalEvent> convertTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return Optional.empty();
        }
        return MultimodalEventFactory.createEvent(motionEvent);
    }

    public static Optional<MultimodalEvent> convertKeyEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            return Optional.empty();
        }
        return MultimodalEventFactory.createEvent(keyEvent);
    }
}
