package com.android.commands.monkey;

public class MonkeyTrackballEvent extends MonkeyMotionEvent {
    public MonkeyTrackballEvent(int action) {
        super(2, 65540, action);
    }

    protected String getTypeLabel() {
        return "Trackball";
    }
}
