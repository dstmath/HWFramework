package ohos.agp.components;

import ohos.agp.utils.MimeData;

public class DragEvent {
    public static final int ACTION_DRAG_ENDED = 4;
    public static final int ACTION_DRAG_ENTERED = 5;
    public static final int ACTION_DRAG_EXITED = 6;
    public static final int ACTION_DRAG_MOVE = 2;
    public static final int ACTION_DRAG_STARTED = 1;
    public static final int ACTION_DROP = 3;
    private int mAction;
    private MimeData mMimeData;
    private float mX;
    private float mY;

    private DragEvent(int i, float f, float f2, MimeData mimeData) {
        this.mAction = i;
        this.mX = f;
        this.mY = f2;
        this.mMimeData = mimeData;
    }

    public static DragEvent obtain(int i, float f, float f2, MimeData mimeData) {
        return new DragEvent(i, f, f2, mimeData);
    }

    public final int getAction() {
        return this.mAction;
    }

    public final float getX() {
        return this.mX;
    }

    public final float getY() {
        return this.mY;
    }

    public final MimeData getClipData() {
        return this.mMimeData;
    }

    public boolean isBroadcast() {
        int i = this.mAction;
        return i == 2 || i == 1 || i == 4;
    }
}
