package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;

public class ExportResult implements Parcelable {
    public static final Parcelable.Creator<ExportResult> CREATOR = new Parcelable.Creator<ExportResult>() {
        public ExportResult createFromParcel(Parcel in) {
            return new ExportResult(in);
        }

        public ExportResult[] newArray(int length) {
            return new ExportResult[length];
        }
    };
    public final byte[] exportData;
    public final int resultCode;

    protected ExportResult(Parcel in) {
        this.resultCode = in.readInt();
        this.exportData = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.resultCode);
        out.writeByteArray(this.exportData);
    }
}
