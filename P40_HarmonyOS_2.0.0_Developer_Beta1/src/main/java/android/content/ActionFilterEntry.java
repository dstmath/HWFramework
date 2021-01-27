package android.content;

import android.os.Parcel;

public final class ActionFilterEntry {
    private String mAction;
    private String mFilterName;
    private String mFilterValue;

    public ActionFilterEntry(String action, String filterName, String filterValue) {
        this.mAction = action;
        this.mFilterName = filterName;
        this.mFilterValue = filterValue;
    }

    ActionFilterEntry(Parcel src) {
        this.mAction = src.readString();
        this.mFilterName = src.readString();
        this.mFilterValue = src.readString();
    }

    /* access modifiers changed from: package-private */
    public void writeToParcel(Parcel dest) {
        dest.writeString(this.mAction);
        dest.writeString(this.mFilterName);
        dest.writeString(this.mFilterValue);
    }

    public String getAction() {
        return this.mAction;
    }

    public String getFilterName() {
        return this.mFilterName;
    }

    public String getFilterValue() {
        return this.mFilterValue;
    }

    public String toString() {
        return "actionFilter[action = " + this.mAction + ", filterName = " + this.mFilterName + ", filterValue = " + this.mFilterValue + "]";
    }
}
