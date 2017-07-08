package android.view;

import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Pools.SynchronizedPool;
import java.util.ArrayList;
import java.util.List;

public class WindowInfo implements Parcelable {
    public static final Creator<WindowInfo> CREATOR = null;
    private static final int MAX_POOL_SIZE = 10;
    private static final SynchronizedPool<WindowInfo> sPool = null;
    public int accessibilityIdOfAnchor;
    public final Rect boundsInScreen;
    public List<IBinder> childTokens;
    public boolean focused;
    public int layer;
    public IBinder parentToken;
    public CharSequence title;
    public IBinder token;
    public int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.WindowInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.WindowInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.WindowInfo.<clinit>():void");
    }

    private WindowInfo() {
        this.boundsInScreen = new Rect();
        this.accessibilityIdOfAnchor = -1;
    }

    public static WindowInfo obtain() {
        WindowInfo window = (WindowInfo) sPool.acquire();
        if (window == null) {
            return new WindowInfo();
        }
        return window;
    }

    public static WindowInfo obtain(WindowInfo other) {
        WindowInfo window = obtain();
        window.type = other.type;
        window.layer = other.layer;
        window.token = other.token;
        window.parentToken = other.parentToken;
        window.focused = other.focused;
        window.boundsInScreen.set(other.boundsInScreen);
        window.title = other.title;
        window.accessibilityIdOfAnchor = other.accessibilityIdOfAnchor;
        if (!(other.childTokens == null || other.childTokens.isEmpty())) {
            if (window.childTokens == null) {
                window.childTokens = new ArrayList(other.childTokens);
            } else {
                window.childTokens.addAll(other.childTokens);
            }
        }
        return window;
    }

    public void recycle() {
        clear();
        sPool.release(this);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        parcel.writeInt(this.type);
        parcel.writeInt(this.layer);
        parcel.writeStrongBinder(this.token);
        parcel.writeStrongBinder(this.parentToken);
        if (this.focused) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        this.boundsInScreen.writeToParcel(parcel, flags);
        parcel.writeCharSequence(this.title);
        parcel.writeInt(this.accessibilityIdOfAnchor);
        if (this.childTokens == null || this.childTokens.isEmpty()) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(1);
        parcel.writeBinderList(this.childTokens);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WindowInfo[");
        builder.append("title=").append(this.title);
        builder.append(", type=").append(this.type);
        builder.append(", layer=").append(this.layer);
        builder.append(", token=").append(this.token);
        builder.append(", bounds=").append(this.boundsInScreen);
        builder.append(", parent=").append(this.parentToken);
        builder.append(", focused=").append(this.focused);
        builder.append(", children=").append(this.childTokens);
        builder.append(", accessibility anchor=").append(this.accessibilityIdOfAnchor);
        builder.append(']');
        return builder.toString();
    }

    private void initFromParcel(Parcel parcel) {
        boolean z;
        boolean hasChildren;
        this.type = parcel.readInt();
        this.layer = parcel.readInt();
        this.token = parcel.readStrongBinder();
        this.parentToken = parcel.readStrongBinder();
        if (parcel.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.focused = z;
        this.boundsInScreen.readFromParcel(parcel);
        this.title = parcel.readCharSequence();
        this.accessibilityIdOfAnchor = parcel.readInt();
        if (parcel.readInt() == 1) {
            hasChildren = true;
        } else {
            hasChildren = false;
        }
        if (hasChildren) {
            if (this.childTokens == null) {
                this.childTokens = new ArrayList();
            }
            parcel.readBinderList(this.childTokens);
        }
    }

    private void clear() {
        this.type = 0;
        this.layer = 0;
        this.token = null;
        this.parentToken = null;
        this.focused = false;
        this.boundsInScreen.setEmpty();
        if (this.childTokens != null) {
            this.childTokens.clear();
        }
    }
}
