package com.android.commands.monkey;

import android.app.IActivityManager;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.IWindowManager;
import android.view.MotionEvent;

public abstract class MonkeyMotionEvent extends MonkeyEvent {
    private int mAction;
    private int mDeviceId;
    private long mDownTime = -1;
    private int mEdgeFlags;
    private long mEventTime = -1;
    private int mFlags;
    private boolean mIntermediateNote;
    private int mMetaState;
    private SparseArray<MotionEvent.PointerCoords> mPointers;
    private int mSource;
    private float mXPrecision;
    private float mYPrecision;

    /* access modifiers changed from: protected */
    public abstract String getTypeLabel();

    protected MonkeyMotionEvent(int type, int source, int action) {
        super(type);
        this.mSource = source;
        this.mAction = action;
        this.mPointers = new SparseArray<>();
        this.mXPrecision = 1.0f;
        this.mYPrecision = 1.0f;
    }

    public MonkeyMotionEvent addPointer(int id, float x, float y) {
        return addPointer(id, x, y, 0.0f, 0.0f);
    }

    public MonkeyMotionEvent addPointer(int id, float x, float y, float pressure, float size) {
        MotionEvent.PointerCoords c = new MotionEvent.PointerCoords();
        c.x = x;
        c.y = y;
        c.pressure = pressure;
        c.size = size;
        this.mPointers.append(id, c);
        return this;
    }

    public MonkeyMotionEvent setIntermediateNote(boolean b) {
        this.mIntermediateNote = b;
        return this;
    }

    public boolean getIntermediateNote() {
        return this.mIntermediateNote;
    }

    public int getAction() {
        return this.mAction;
    }

    public long getDownTime() {
        return this.mDownTime;
    }

    public long getEventTime() {
        return this.mEventTime;
    }

    public MonkeyMotionEvent setDownTime(long downTime) {
        this.mDownTime = downTime;
        return this;
    }

    public MonkeyMotionEvent setEventTime(long eventTime) {
        this.mEventTime = eventTime;
        return this;
    }

    public MonkeyMotionEvent setMetaState(int metaState) {
        this.mMetaState = metaState;
        return this;
    }

    public MonkeyMotionEvent setPrecision(float xPrecision, float yPrecision) {
        this.mXPrecision = xPrecision;
        this.mYPrecision = yPrecision;
        return this;
    }

    public MonkeyMotionEvent setDeviceId(int deviceId) {
        this.mDeviceId = deviceId;
        return this;
    }

    public MonkeyMotionEvent setEdgeFlags(int edgeFlags) {
        this.mEdgeFlags = edgeFlags;
        return this;
    }

    private MotionEvent getEvent() {
        int pointerCount = this.mPointers.size();
        int[] pointerIds = new int[pointerCount];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            pointerIds[i] = this.mPointers.keyAt(i);
            pointerCoords[i] = this.mPointers.valueAt(i);
        }
        long j = this.mDownTime;
        long uptimeMillis = this.mEventTime < 0 ? SystemClock.uptimeMillis() : this.mEventTime;
        int i2 = this.mAction;
        int i3 = this.mMetaState;
        float f = this.mXPrecision;
        float f2 = this.mYPrecision;
        int i4 = this.mDeviceId;
        int i5 = this.mEdgeFlags;
        int i6 = i5;
        MotionEvent.PointerCoords[] pointerCoordsArr = pointerCoords;
        MotionEvent.PointerCoords[] pointerCoordsArr2 = pointerCoords;
        int i7 = i6;
        int[] iArr = pointerIds;
        return MotionEvent.obtain(j, uptimeMillis, i2, pointerCount, pointerIds, pointerCoordsArr, i3, f, f2, i4, i7, this.mSource, this.mFlags);
    }

    public boolean isThrottlable() {
        return getAction() == 1;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        MotionEvent me = getEvent();
        if ((verbose > 0 && !this.mIntermediateNote) || verbose > 1) {
            StringBuilder msg = new StringBuilder(":Sending ");
            msg.append(getTypeLabel());
            msg.append(" (");
            switch (me.getActionMasked()) {
                case 0:
                    msg.append("ACTION_DOWN");
                    break;
                case 1:
                    msg.append("ACTION_UP");
                    break;
                case 2:
                    msg.append("ACTION_MOVE");
                    break;
                case 3:
                    msg.append("ACTION_CANCEL");
                    break;
                case 5:
                    msg.append("ACTION_POINTER_DOWN ");
                    msg.append(me.getPointerId(me.getActionIndex()));
                    break;
                case 6:
                    msg.append("ACTION_POINTER_UP ");
                    msg.append(me.getPointerId(me.getActionIndex()));
                    break;
                default:
                    msg.append(me.getAction());
                    break;
            }
            msg.append("):");
            int pointerCount = me.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                msg.append(" ");
                msg.append(me.getPointerId(i));
                msg.append(":(");
                msg.append(me.getX(i));
                msg.append(",");
                msg.append(me.getY(i));
                msg.append(")");
            }
            Logger.out.println(msg.toString());
        }
        try {
            if (!InputManager.getInstance().injectInputEvent(me, 1)) {
                return 0;
            }
            me.recycle();
            return 1;
        } finally {
            me.recycle();
        }
    }
}
