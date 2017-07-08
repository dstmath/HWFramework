package android.view;

import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class InputEvent implements Parcelable {
    public static final Creator<InputEvent> CREATOR = null;
    protected static final int PARCEL_TOKEN_KEY_EVENT = 2;
    protected static final int PARCEL_TOKEN_MOTION_EVENT = 1;
    private static final boolean TRACK_RECYCLED_LOCATION = false;
    private static final AtomicInteger mNextSeq = null;
    protected boolean mRecycled;
    private RuntimeException mRecycledLocation;
    protected int mSeq;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.InputEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.InputEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.InputEvent.<clinit>():void");
    }

    public abstract void cancel();

    public abstract InputEvent copy();

    public abstract int getDeviceId();

    public abstract long getEventTime();

    public abstract long getEventTimeNano();

    public abstract int getSource();

    public abstract boolean isTainted();

    public abstract void setSource(int i);

    public abstract void setTainted(boolean z);

    InputEvent() {
        this.mSeq = mNextSeq.getAndIncrement();
    }

    public final InputDevice getDevice() {
        return InputDevice.getDevice(getDeviceId());
    }

    public boolean isFromSource(int source) {
        return (getSource() & source) == source;
    }

    public void recycle() {
        if (this.mRecycled) {
            throw new RuntimeException(toString() + " recycled twice!");
        }
        this.mRecycled = true;
    }

    public void recycleIfNeededAfterDispatch() {
        recycle();
    }

    protected void prepareForReuse() {
        this.mRecycled = false;
        this.mRecycledLocation = null;
        this.mSeq = mNextSeq.getAndIncrement();
    }

    public int getSequenceNumber() {
        return this.mSeq;
    }

    public int describeContents() {
        return 0;
    }
}
