package com.android.commands.monkey;

public class MonkeyTrackballEvent extends MonkeyMotionEvent {
    public MonkeyTrackballEvent(int action) {
        super(2, 65540, action);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.commands.monkey.MonkeyMotionEvent
    public String getTypeLabel() {
        return "Trackball";
    }
}
