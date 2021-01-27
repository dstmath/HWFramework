package ohos.agp.components;

import ohos.agp.utils.MimeData;

public class DragEvent {
    public static final int DRAG_BEGIN = 1;
    public static final int DRAG_DROP = 6;
    public static final int DRAG_FINISH = 5;
    public static final int DRAG_IN = 3;
    public static final int DRAG_MOVE = 2;
    public static final int DRAG_OUT = 4;
    private final int mAction;
    private final MimeData mMimeData;
    private final float mX;
    private final float mY;

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

    @Deprecated
    public final MimeData getClipData() {
        return this.mMimeData;
    }

    public final MimeData getMimeData() {
        return this.mMimeData;
    }

    public boolean isBroadcast() {
        int i = this.mAction;
        return i == 2 || i == 1 || i == 5;
    }
}
