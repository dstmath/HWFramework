package com.huawei.nb.query.bulkcursor;

import android.database.CursorWindow;
import android.os.Parcel;
import android.os.Parcelable;

public final class BulkCursorDescriptor implements Parcelable {
    public static final Parcelable.Creator<BulkCursorDescriptor> CREATOR = new Parcelable.Creator<BulkCursorDescriptor>() {
        /* class com.huawei.nb.query.bulkcursor.BulkCursorDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BulkCursorDescriptor createFromParcel(Parcel parcel) {
            return new BulkCursorDescriptor(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public BulkCursorDescriptor[] newArray(int i) {
            return new BulkCursorDescriptor[i];
        }
    };
    private String[] columnNames;
    private int count;
    private IBulkCursor cursor;
    private boolean wantsAllOnMoveCalls;
    private CursorWindow window;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public BulkCursorDescriptor(IBulkCursor iBulkCursor, String[] strArr, boolean z, int i, CursorWindow cursorWindow) {
        String[] strArr2;
        this.cursor = iBulkCursor;
        if (strArr == null) {
            strArr2 = null;
        } else {
            strArr2 = (String[]) strArr.clone();
        }
        this.columnNames = strArr2;
        this.wantsAllOnMoveCalls = z;
        this.count = i;
        this.window = cursorWindow;
    }

    public BulkCursorDescriptor(Parcel parcel) {
        this.cursor = BulkCursorNative.asInterface(parcel.readStrongBinder());
        this.columnNames = parcel.createStringArray();
        this.wantsAllOnMoveCalls = parcel.readInt() != 0;
        this.count = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.window = (CursorWindow) CursorWindow.CREATOR.createFromParcel(parcel);
        }
    }

    public IBulkCursor getCursor() {
        return this.cursor;
    }

    public String[] getColumnNames() {
        return (String[]) this.columnNames.clone();
    }

    public boolean isWantsAllOnMoveCalls() {
        return this.wantsAllOnMoveCalls;
    }

    public int getCount() {
        return this.count;
    }

    public CursorWindow getWindow() {
        return this.window;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStrongBinder(this.cursor.asBinder());
        parcel.writeStringArray(this.columnNames);
        parcel.writeInt(this.wantsAllOnMoveCalls ? 1 : 0);
        parcel.writeInt(this.count);
        if (this.window != null) {
            parcel.writeInt(1);
            this.window.writeToParcel(parcel, i);
            return;
        }
        parcel.writeInt(0);
    }
}
