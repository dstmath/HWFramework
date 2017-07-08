package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class WindowContentFrameStats extends FrameStats implements Parcelable {
    public static final Creator<WindowContentFrameStats> CREATOR = null;
    private long[] mFramesPostedTimeNano;
    private long[] mFramesReadyTimeNano;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.WindowContentFrameStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.WindowContentFrameStats.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.WindowContentFrameStats.<clinit>():void");
    }

    public void init(long refreshPeriodNano, long[] framesPostedTimeNano, long[] framesPresentedTimeNano, long[] framesReadyTimeNano) {
        this.mRefreshPeriodNano = refreshPeriodNano;
        this.mFramesPostedTimeNano = framesPostedTimeNano;
        this.mFramesPresentedTimeNano = framesPresentedTimeNano;
        this.mFramesReadyTimeNano = framesReadyTimeNano;
    }

    private WindowContentFrameStats(Parcel parcel) {
        this.mRefreshPeriodNano = parcel.readLong();
        this.mFramesPostedTimeNano = parcel.createLongArray();
        this.mFramesPresentedTimeNano = parcel.createLongArray();
        this.mFramesReadyTimeNano = parcel.createLongArray();
    }

    public long getFramePostedTimeNano(int index) {
        if (this.mFramesPostedTimeNano != null) {
            return this.mFramesPostedTimeNano[index];
        }
        throw new IndexOutOfBoundsException();
    }

    public long getFrameReadyTimeNano(int index) {
        if (this.mFramesReadyTimeNano != null) {
            return this.mFramesReadyTimeNano[index];
        }
        throw new IndexOutOfBoundsException();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(this.mRefreshPeriodNano);
        parcel.writeLongArray(this.mFramesPostedTimeNano);
        parcel.writeLongArray(this.mFramesPresentedTimeNano);
        parcel.writeLongArray(this.mFramesReadyTimeNano);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WindowContentFrameStats[");
        builder.append("frameCount:").append(getFrameCount());
        builder.append(", fromTimeNano:").append(getStartTimeNano());
        builder.append(", toTimeNano:").append(getEndTimeNano());
        builder.append(']');
        return builder.toString();
    }
}
