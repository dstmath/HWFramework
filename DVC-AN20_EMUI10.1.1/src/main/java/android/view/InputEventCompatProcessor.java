package android.view;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class InputEventCompatProcessor {
    protected Context mContext;
    private List<InputEvent> mProcessedEvents = new ArrayList();
    protected int mTargetSdkVersion;

    public InputEventCompatProcessor(Context context) {
        this.mContext = context;
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
    }

    public List<InputEvent> processInputEventForCompatibility(InputEvent e) {
        if (this.mTargetSdkVersion >= 23 || !(e instanceof MotionEvent)) {
            return null;
        }
        this.mProcessedEvents.clear();
        MotionEvent motion = (MotionEvent) e;
        int buttonState = motion.getButtonState();
        int compatButtonState = (buttonState & 96) >> 4;
        if (compatButtonState != 0) {
            motion.setButtonState(buttonState | compatButtonState);
        }
        this.mProcessedEvents.add(motion);
        return this.mProcessedEvents;
    }

    public InputEvent processInputEventBeforeFinish(InputEvent e) {
        return e;
    }
}
