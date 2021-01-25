package com.android.commands.monkey;

public class MonkeyTouchEvent extends MonkeyMotionEvent {
    public MonkeyTouchEvent(int action) {
        super(1, 4098, action);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.commands.monkey.MonkeyMotionEvent
    public String getTypeLabel() {
        return "Touch";
    }
}
