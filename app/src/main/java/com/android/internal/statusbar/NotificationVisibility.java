package com.android.internal.statusbar;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayDeque;

public class NotificationVisibility implements Parcelable {
    public static final Creator<NotificationVisibility> CREATOR = null;
    private static final int MAX_POOL_SIZE = 25;
    private static final String TAG = "NoViz";
    private static int sNexrId;
    private static ArrayDeque<NotificationVisibility> sPool;
    int id;
    public String key;
    public int rank;
    public boolean visible;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.statusbar.NotificationVisibility.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.statusbar.NotificationVisibility.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.statusbar.NotificationVisibility.<clinit>():void");
    }

    private NotificationVisibility() {
        this.visible = true;
        int i = sNexrId;
        sNexrId = i + 1;
        this.id = i;
    }

    private NotificationVisibility(String key, int rank, boolean visibile) {
        this();
        this.key = key;
        this.rank = rank;
        this.visible = visibile;
    }

    public String toString() {
        return "NotificationVisibility(id=" + this.id + "key=" + this.key + " rank=" + this.rank + (this.visible ? " visible" : "") + " )";
    }

    public NotificationVisibility clone() {
        return obtain(this.key, this.rank, this.visible);
    }

    public int hashCode() {
        return this.key == null ? 0 : this.key.hashCode();
    }

    public boolean equals(Object that) {
        if (!(that instanceof NotificationVisibility)) {
            return false;
        }
        NotificationVisibility thatViz = (NotificationVisibility) that;
        boolean equals = (this.key == null && thatViz.key == null) ? true : this.key.equals(thatViz.key);
        return equals;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.key);
        out.writeInt(this.rank);
        out.writeInt(this.visible ? 1 : 0);
    }

    private void readFromParcel(Parcel in) {
        boolean z = false;
        this.key = in.readString();
        this.rank = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        }
        this.visible = z;
    }

    public static NotificationVisibility obtain(String key, int rank, boolean visible) {
        NotificationVisibility vo = obtain();
        vo.key = key;
        vo.rank = rank;
        vo.visible = visible;
        return vo;
    }

    private static NotificationVisibility obtain(Parcel in) {
        NotificationVisibility vo = obtain();
        vo.readFromParcel(in);
        return vo;
    }

    private static NotificationVisibility obtain() {
        synchronized (sPool) {
            if (sPool.isEmpty()) {
                return new NotificationVisibility();
            }
            NotificationVisibility notificationVisibility = (NotificationVisibility) sPool.poll();
            return notificationVisibility;
        }
    }

    public void recycle() {
        if (this.key != null) {
            this.key = null;
            if (sPool.size() < MAX_POOL_SIZE) {
                synchronized (sPool) {
                    sPool.offer(this);
                }
            }
        }
    }
}
