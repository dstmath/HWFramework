package android.view;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.view.IDragAndDropPermissions;

public class DragEvent implements Parcelable {
    public static final int ACTION_DRAG_ENDED = 4;
    public static final int ACTION_DRAG_ENTERED = 5;
    public static final int ACTION_DRAG_EXITED = 6;
    public static final int ACTION_DRAG_LOCATION = 2;
    public static final int ACTION_DRAG_STARTED = 1;
    public static final int ACTION_DROP = 3;
    public static final Creator<DragEvent> CREATOR = null;
    private static final int MAX_RECYCLED = 10;
    private static final boolean TRACK_RECYCLED_LOCATION = false;
    private static final Object gRecyclerLock = null;
    private static DragEvent gRecyclerTop;
    private static int gRecyclerUsed;
    int mAction;
    ClipData mClipData;
    ClipDescription mClipDescription;
    IDragAndDropPermissions mDragAndDropPermissions;
    boolean mDragResult;
    Object mLocalState;
    private DragEvent mNext;
    private boolean mRecycled;
    private RuntimeException mRecycledLocation;
    float mX;
    float mY;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.DragEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.DragEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.DragEvent.<clinit>():void");
    }

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
            if (gRecyclerTop == null) {
                DragEvent ev = new DragEvent();
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
        synchronized (gRecyclerLock) {
            if (gRecyclerUsed < MAX_RECYCLED) {
                gRecyclerUsed += ACTION_DRAG_STARTED;
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
        int i;
        dest.writeInt(this.mAction);
        dest.writeFloat(this.mX);
        dest.writeFloat(this.mY);
        if (this.mDragResult) {
            i = ACTION_DRAG_STARTED;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mClipData == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(ACTION_DRAG_STARTED);
            this.mClipData.writeToParcel(dest, flags);
        }
        if (this.mClipDescription == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(ACTION_DRAG_STARTED);
            this.mClipDescription.writeToParcel(dest, flags);
        }
        if (this.mDragAndDropPermissions == null) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(ACTION_DRAG_STARTED);
        dest.writeStrongBinder(this.mDragAndDropPermissions.asBinder());
    }
}
