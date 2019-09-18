package android.database;

import android.os.Parcel;
import android.os.Parcelable;

public final class BulkCursorDescriptor implements Parcelable {
    public static final Parcelable.Creator<BulkCursorDescriptor> CREATOR = new Parcelable.Creator<BulkCursorDescriptor>() {
        public BulkCursorDescriptor createFromParcel(Parcel in) {
            BulkCursorDescriptor d = new BulkCursorDescriptor();
            d.readFromParcel(in);
            return d;
        }

        public BulkCursorDescriptor[] newArray(int size) {
            return new BulkCursorDescriptor[size];
        }
    };
    public String[] columnNames;
    public int count;
    public IBulkCursor cursor;
    public boolean wantsAllOnMoveCalls;
    public CursorWindow window;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.cursor.asBinder());
        out.writeStringArray(this.columnNames);
        out.writeInt(this.wantsAllOnMoveCalls ? 1 : 0);
        out.writeInt(this.count);
        if (this.window != null) {
            out.writeInt(1);
            this.window.writeToParcel(out, flags);
            return;
        }
        out.writeInt(0);
    }

    public void readFromParcel(Parcel in) {
        this.cursor = BulkCursorNative.asInterface(in.readStrongBinder());
        this.columnNames = in.readStringArray();
        this.wantsAllOnMoveCalls = in.readInt() != 0;
        this.count = in.readInt();
        if (in.readInt() != 0) {
            this.window = CursorWindow.CREATOR.createFromParcel(in);
        }
    }
}
