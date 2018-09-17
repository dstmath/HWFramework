package com.android.commands.monkey;

import android.app.IActivityManager;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public abstract class MonkeyMotionEvent extends MonkeyEvent {
    private int mAction;
    private int mDeviceId;
    private long mDownTime = -1;
    private int mEdgeFlags;
    private long mEventTime = -1;
    private int mFlags;
    private boolean mIntermediateNote;
    private int mMetaState;
    private SparseArray<PointerCoords> mPointers;
    private int mSource;
    private float mXPrecision;
    private float mYPrecision;

    protected abstract String getTypeLabel();

    protected MonkeyMotionEvent(int type, int source, int action) {
        super(type);
        this.mSource = source;
        this.mAction = action;
        this.mPointers = new SparseArray();
        this.mXPrecision = 1.0f;
        this.mYPrecision = 1.0f;
    }

    public MonkeyMotionEvent addPointer(int id, float x, float y) {
        return addPointer(id, x, y, 0.0f, 0.0f);
    }

    public MonkeyMotionEvent addPointer(int id, float x, float y, float pressure, float size) {
        PointerCoords c = new PointerCoords();
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
        PointerCoords[] pointerCoords = new PointerCoords[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            pointerIds[i] = this.mPointers.keyAt(i);
            pointerCoords[i] = (PointerCoords) this.mPointers.valueAt(i);
        }
        return MotionEvent.obtain(this.mDownTime, this.mEventTime < 0 ? SystemClock.uptimeMillis() : this.mEventTime, this.mAction, pointerCount, pointerIds, pointerCoords, this.mMetaState, this.mXPrecision, this.mYPrecision, this.mDeviceId, this.mEdgeFlags, this.mSource, this.mFlags);
    }

    public boolean isThrottlable() {
        return getAction() == 1;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        MotionEvent me = getEvent();
        if ((verbose > 0 && (this.mIntermediateNote ^ 1) != 0) || verbose > 1) {
            StringBuilder msg = new StringBuilder(":Sending ");
            msg.append(getTypeLabel()).append(" (");
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
                    msg.append("ACTION_POINTER_DOWN ").append(me.getPointerId(me.getActionIndex()));
                    break;
                case 6:
                    msg.append("ACTION_POINTER_UP ").append(me.getPointerId(me.getActionIndex()));
                    break;
                default:
                    msg.append(me.getAction());
                    break;
            }
            msg.append("):");
            int pointerCount = me.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                msg.append(" ").append(me.getPointerId(i));
                msg.append(":(").append(me.getX(i)).append(",").append(me.getY(i)).append(")");
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
