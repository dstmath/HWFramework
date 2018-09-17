package android.view;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.internal.view.IDragAndDropPermissions.Stub;

public class DragEvent implements Parcelable {
    public static final int ACTION_DRAG_ENDED = 4;
    public static final int ACTION_DRAG_ENTERED = 5;
    public static final int ACTION_DRAG_EXITED = 6;
    public static final int ACTION_DRAG_LOCATION = 2;
    public static final int ACTION_DRAG_STARTED = 1;
    public static final int ACTION_DROP = 3;
    public static final Creator<DragEvent> CREATOR = new Creator<DragEvent>() {
        public DragEvent createFromParcel(Parcel in) {
            boolean z = false;
            DragEvent event = DragEvent.obtain();
            event.mAction = in.readInt();
            event.mX = in.readFloat();
            event.mY = in.readFloat();
            if (in.readInt() != 0) {
                z = true;
            }
            event.mDragResult = z;
            if (in.readInt() != 0) {
                event.mClipData = (ClipData) ClipData.CREATOR.createFromParcel(in);
            }
            if (in.readInt() != 0) {
                event.mClipDescription = (ClipDescription) ClipDescription.CREATOR.createFromParcel(in);
            }
            if (in.readInt() != 0) {
                event.mDragAndDropPermissions = Stub.asInterface(in.readStrongBinder());
            }
            return event;
        }

        public DragEvent[] newArray(int size) {
            return new DragEvent[size];
        }
    };
    private static final int MAX_RECYCLED = 10;
    private static final boolean TRACK_RECYCLED_LOCATION = false;
    private static final Object gRecyclerLock = new Object();
    private static DragEvent gRecyclerTop = null;
    private static int gRecyclerUsed = 0;
    int mAction;
    ClipData mClipData;
    ClipDescription mClipDescription;
    IDragAndDropPermissions mDragAndDropPermissions;
    boolean mDragResult;
    boolean mEventHandlerWasCalled;
    Object mLocalState;
    private DragEvent mNext;
    private boolean mRecycled;
    private RuntimeException mRecycledLocation;
    float mX;
    float mY;

    private DragEvent() {
    }

    private void init(int action, float x, float y, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, Object localState, boolean result) {
        this.mAction = action;
        this.mX = x;
        this.mY = y;
        this.mClipDescription = description;
        this.mClipData = data;
        this.mDragAndDropPermissions = dragAndDropPermissions;
        this.mLocalState = localState;
        this.mDragResult = result;
    }

    static DragEvent obtain() {
        return obtain(0, 0.0f, 0.0f, null, null, null, null, false);
    }

    public static DragEvent obtain(int action, float x, float y, Object localState, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, boolean result) {
        synchronized (gRecyclerLock) {
            DragEvent ev;
            if (gRecyclerTop == null) {
                ev = new DragEvent();
                ev.init(action, x, y, description, data, dragAndDropPermissions, localState, result);
                return ev;
            }
            ev = gRecyclerTop;
            gRecyclerTop = ev.mNext;
            gRecyclerUsed--;
            ev.mRecycledLocation = null;
            ev.mRecycled = false;
            ev.mNext = null;
            ev.init(action, x, y, description, data, dragAndDropPermissions, localState, result);
            return ev;
        }
    }

    public static DragEvent obtain(DragEvent source) {
        return obtain(source.mAction, source.mX, source.mY, source.mLocalState, source.mClipDescription, source.mClipData, source.mDragAndDropPermissions, source.mDragResult);
    }

    public int getAction() {
        return this.mAction;
    }

    public float getX() {
        return this.mX;
    }

    public float getY() {
        return this.mY;
    }

    public ClipData getClipData() {
        return this.mClipData;
    }

    public ClipDescription getClipDescription() {
        return this.mClipDescription;
    }

    public IDragAndDropPermissions getDragAndDropPermissions() {
        return this.mDragAndDropPermissions;
    }

    public Object getLocalState() {
        return this.mLocalState;
    }

    public boolean getResult() {
        return this.mDragResult;
    }

    public final void recycle() {
        if (this.mRecycled) {
            throw new RuntimeException(toString() + " recycled twice!");
        }
        this.mRecycled = true;
        this.mClipData = null;
        this.mClipDescription = null;
        this.mLocalState = null;
        this.mEventHandlerWasCalled = false;
        synchronized (gRecyclerLock) {
            if (gRecyclerUsed < 10) {
                gRecyclerUsed++;
                this.mNext = gRecyclerTop;
                gRecyclerTop = this;
            }
        }
    }

    public String toString() {
        return "DragEvent{" + Integer.toHexString(System.identityHashCode(this)) + " action=" + this.mAction + " @ (" + this.mX + ", " + this.mY + ") desc=" + this.mClipDescription + " data=" + this.mClipData + " local=" + this.mLocalState + " result=" + this.mDragResult + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAction);
        dest.writeFloat(this.mX);
        dest.writeFloat(this.mY);
        dest.writeInt(this.mDragResult ? 1 : 0);
        if (this.mClipData == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mClipData.writeToParcel(dest, flags);
        }
        if (this.mClipDescription == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mClipDescription.writeToParcel(dest, flags);
        }
        if (this.mDragAndDropPermissions == null) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(1);
        dest.writeStrongBinder(this.mDragAndDropPermissions.asBinder());
    }
}
