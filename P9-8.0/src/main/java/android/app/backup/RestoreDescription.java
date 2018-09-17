package android.app.backup;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RestoreDescription implements Parcelable {
    public static final Creator<RestoreDescription> CREATOR = new Creator<RestoreDescription>() {
        public RestoreDescription createFromParcel(Parcel in) {
            RestoreDescription unparceled = new RestoreDescription(in, null);
            if ("".equals(unparceled.mPackageName)) {
                return RestoreDescription.NO_MORE_PACKAGES;
            }
            return unparceled;
        }

        public RestoreDescription[] newArray(int size) {
            return new RestoreDescription[size];
        }
    };
    public static final RestoreDescription NO_MORE_PACKAGES = new RestoreDescription("", 0);
    private static final String NO_MORE_PACKAGES_SENTINEL = "";
    public static final int TYPE_FULL_STREAM = 2;
    public static final int TYPE_KEY_VALUE = 1;
    private final int mDataType;
    private final String mPackageName;

    public String toString() {
        return "RestoreDescription{" + this.mPackageName + " : " + (this.mDataType == 1 ? "KEY_VALUE" : "STREAM") + '}';
    }

    public RestoreDescription(String packageName, int dataType) {
        this.mPackageName = packageName;
        this.mDataType = dataType;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getDataType() {
        return this.mDataType;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mPackageName);
        out.writeInt(this.mDataType);
    }

    private RestoreDescription(Parcel in) {
        this.mPackageName = in.readString();
        this.mDataType = in.readInt();
    }
}
