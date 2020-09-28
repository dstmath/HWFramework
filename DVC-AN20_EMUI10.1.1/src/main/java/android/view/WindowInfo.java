package android.view;

import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pools;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;

public class WindowInfo implements Parcelable {
    public static final Parcelable.Creator<WindowInfo> CREATOR = new Parcelable.Creator<WindowInfo>() {
        /* class android.view.WindowInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WindowInfo createFromParcel(Parcel parcel) {
            WindowInfo window = WindowInfo.obtain();
            window.initFromParcel(parcel);
            return window;
        }

        @Override // android.os.Parcelable.Creator
        public WindowInfo[] newArray(int size) {
            return new WindowInfo[size];
        }
    };
    private static final int MAX_POOL_SIZE = 10;
    private static final Pools.SynchronizedPool<WindowInfo> sPool = new Pools.SynchronizedPool<>(10);
    public long accessibilityIdOfAnchor = AccessibilityNodeInfo.UNDEFINED_NODE_ID;
    public IBinder activityToken;
    public final Rect boundsInScreen = new Rect();
    public List<IBinder> childTokens;
    public boolean focused;
    public boolean hasFlagWatchOutsideTouch;
    public boolean inPictureInPicture;
    public int layer;
    public IBinder parentToken;
    public CharSequence title;
    public IBinder token;
    public int type;

    private WindowInfo() {
    }

    public static WindowInfo obtain() {
        WindowInfo window = sPool.acquire();
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
        window.activityToken = other.activityToken;
        window.focused = other.focused;
        window.boundsInScreen.set(other.boundsInScreen);
        window.title = other.title;
        window.accessibilityIdOfAnchor = other.accessibilityIdOfAnchor;
        window.inPictureInPicture = other.inPictureInPicture;
        window.hasFlagWatchOutsideTouch = other.hasFlagWatchOutsideTouch;
        List<IBinder> list = other.childTokens;
        if (list != null && !list.isEmpty()) {
            List<IBinder> list2 = window.childTokens;
            if (list2 == null) {
                window.childTokens = new ArrayList(other.childTokens);
            } else {
                list2.addAll(other.childTokens);
            }
        }
        return window;
    }

    public void recycle() {
        clear();
        sPool.release(this);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.type);
        parcel.writeInt(this.layer);
        parcel.writeStrongBinder(this.token);
        parcel.writeStrongBinder(this.parentToken);
        parcel.writeStrongBinder(this.activityToken);
        parcel.writeInt(this.focused ? 1 : 0);
        this.boundsInScreen.writeToParcel(parcel, flags);
        parcel.writeCharSequence(this.title);
        parcel.writeLong(this.accessibilityIdOfAnchor);
        parcel.writeInt(this.inPictureInPicture ? 1 : 0);
        parcel.writeInt(this.hasFlagWatchOutsideTouch ? 1 : 0);
        List<IBinder> list = this.childTokens;
        if (list == null || list.isEmpty()) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(1);
        parcel.writeBinderList(this.childTokens);
    }

    public String toString() {
        return "WindowInfo[" + "title=" + this.title + ", type=" + this.type + ", layer=" + this.layer + ", token=" + this.token + ", bounds=" + this.boundsInScreen + ", parent=" + this.parentToken + ", focused=" + this.focused + ", children=" + this.childTokens + ", accessibility anchor=" + this.accessibilityIdOfAnchor + ", pictureInPicture=" + this.inPictureInPicture + ", watchOutsideTouch=" + this.hasFlagWatchOutsideTouch + ']';
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFromParcel(Parcel parcel) {
        this.type = parcel.readInt();
        this.layer = parcel.readInt();
        this.token = parcel.readStrongBinder();
        this.parentToken = parcel.readStrongBinder();
        this.activityToken = parcel.readStrongBinder();
        boolean hasChildren = false;
        this.focused = parcel.readInt() == 1;
        this.boundsInScreen.readFromParcel(parcel);
        this.title = parcel.readCharSequence();
        this.accessibilityIdOfAnchor = parcel.readLong();
        this.inPictureInPicture = parcel.readInt() == 1;
        this.hasFlagWatchOutsideTouch = parcel.readInt() == 1;
        if (parcel.readInt() == 1) {
            hasChildren = true;
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
        this.activityToken = null;
        this.focused = false;
        this.boundsInScreen.setEmpty();
        List<IBinder> list = this.childTokens;
        if (list != null) {
            list.clear();
        }
        this.inPictureInPicture = false;
        this.hasFlagWatchOutsideTouch = false;
    }
}
