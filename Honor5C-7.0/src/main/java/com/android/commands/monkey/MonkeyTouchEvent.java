package com.android.commands.monkey;

public class MonkeyTouchEvent extends MonkeyMotionEvent {
    public MonkeyTouchEvent(int action) {
        super(1, 4098, action);
    }

    protected String getTypeLabel() {
        return "Touch";
    }
}
