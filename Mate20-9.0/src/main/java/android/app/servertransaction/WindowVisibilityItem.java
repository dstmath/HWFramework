package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Trace;

public class WindowVisibilityItem extends ClientTransactionItem {
    public static final Parcelable.Creator<WindowVisibilityItem> CREATOR = new Parcelable.Creator<WindowVisibilityItem>() {
        public WindowVisibilityItem createFromParcel(Parcel in) {
            return new WindowVisibilityItem(in);
        }

        public WindowVisibilityItem[] newArray(int size) {
            return new WindowVisibilityItem[size];
        }
    };
    private boolean mShowWindow;

    public void execute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
        Trace.traceBegin(64, "activityShowWindow");
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

    public void recycle() {
        this.mShowWindow = false;
        ObjectPool.recycle(this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mShowWindow);
    }

    private WindowVisibilityItem(Parcel in) {
        this.mShowWindow = in.readBoolean();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.mShowWindow != ((WindowVisibilityItem) o).mShowWindow) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return 17 + (true * (this.mShowWindow ? 1 : 0));
    }

    public String toString() {
        return "WindowVisibilityItem{showWindow=" + this.mShowWindow + "}";
    }
}
