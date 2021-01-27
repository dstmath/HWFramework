package com.huawei.nb.searchmanager.emuiclient.query.bulkcursor;

import android.database.CursorWindow;
import android.os.Parcel;
import android.os.Parcelable;

public final class BulkCursorDescriptorEx implements Parcelable {
    public static final Parcelable.Creator<BulkCursorDescriptorEx> CREATOR = new Parcelable.Creator<BulkCursorDescriptorEx>() {
        /* class com.huawei.nb.searchmanager.emuiclient.query.bulkcursor.BulkCursorDescriptorEx.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BulkCursorDescriptorEx createFromParcel(Parcel in) {
            return new BulkCursorDescriptorEx(in);
        }

        @Override // android.os.Parcelable.Creator
        public BulkCursorDescriptorEx[] newArray(int size) {
            return new BulkCursorDescriptorEx[size];
        }
    };
    private String[] columnNames;
    private int count;
    private IBulkCursor cursor;
    private boolean wantsAllOnMoveCalls;
    private CursorWindow window;

    public BulkCursorDescriptorEx(IBulkCursor cursor2, String[] columnNames2, boolean wantsAllOnMoveCalls2, int count2, CursorWindow window2) {
        this.cursor = cursor2;
        this.columnNames = columnNames2 == null ? null : (String[]) columnNames2.clone();
        this.wantsAllOnMoveCalls = wantsAllOnMoveCalls2;
        this.count = count2;
        this.window = window2;
    }

    public BulkCursorDescriptorEx(Parcel in) {
        this.cursor = BulkCursorNative.asInterface(in.readStrongBinder());
        this.columnNames = in.createStringArray();
        this.wantsAllOnMoveCalls = in.readInt() != 0;
        this.count = in.readInt();
        if (in.readInt() != 0) {
            this.window = (CursorWindow) CursorWindow.CREATOR.createFromParcel(in);
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
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
}
