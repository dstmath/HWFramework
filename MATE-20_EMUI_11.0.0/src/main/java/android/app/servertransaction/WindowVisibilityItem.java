package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;

public class WindowVisibilityItem extends ClientTransactionItem {
    public static final Parcelable.Creator<WindowVisibilityItem> CREATOR = new Parcelable.Creator<WindowVisibilityItem>() {
        /* class android.app.servertransaction.WindowVisibilityItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WindowVisibilityItem createFromParcel(Parcel in) {
            return new WindowVisibilityItem(in);
        }

        @Override // android.os.Parcelable.Creator
        public WindowVisibilityItem[] newArray(int size) {
            return new WindowVisibilityItem[size];
        }
    };
    private boolean mShowWindow;

    @Override // android.app.servertransaction.BaseClientRequest
    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, this.mShowWindow ? "activityShowWindow" : "activityHideWindow");
        client.handleWindowVisibility(token, this.mShowWindow);
        Trace.traceEnd(64);
    }

    private WindowVisibilityItem() {
    }

    public static WindowVisibilityItem obtain(boolean showWindow) {
        WindowVisibilityItem instance = (WindowVisibilityItem) ObjectPool.obtain(WindowVisibilityItem.class);
        if (instance == null) {
            instance = new WindowVisibilityItem();
        }
        instance.mShowWindow = showWindow;
        return instance;
    }

    @Override // android.app.servertransaction.ObjectPoolItem
    public void recycle() {
        this.mShowWindow = false;
        ObjectPool.recycle(this);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mShowWindow);
    }

    private WindowVisibilityItem(Parcel in) {
        this.mShowWindow = in.readBoolean();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.mShowWindow == ((WindowVisibilityItem) o).mShowWindow) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((this.mShowWindow ? 1 : 0) * 31) + 17;
    }

    public String toString() {
        return "WindowVisibilityItem{showWindow=" + this.mShowWindow + "}";
    }
}
