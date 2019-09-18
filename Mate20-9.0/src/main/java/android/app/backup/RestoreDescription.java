package android.app.backup;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public class RestoreDescription implements Parcelable {
    public static final Parcelable.Creator<RestoreDescription> CREATOR = new Parcelable.Creator<RestoreDescription>() {
        public RestoreDescription createFromParcel(Parcel in) {
            RestoreDescription unparceled = new RestoreDescription(in);
            if (RestoreDescription.NO_MORE_PACKAGES_SENTINEL.equals(unparceled.mPackageName)) {
                return RestoreDescription.NO_MORE_PACKAGES;
            }
            return unparceled;
        }

        public RestoreDescription[] newArray(int size) {
            return new RestoreDescription[size];
        }
    };
    public static final RestoreDescription NO_MORE_PACKAGES = new RestoreDescription(NO_MORE_PACKAGES_SENTINEL, 0);
    private static final String NO_MORE_PACKAGES_SENTINEL = "NO_MORE_PACKAGES";
    public static final int TYPE_FULL_STREAM = 2;
    public static final int TYPE_KEY_VALUE = 1;
    private final int mDataType;
    /* access modifiers changed from: private */
    public final String mPackageName;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RestoreDescription{");
        sb.append(this.mPackageName);
        sb.append(" : ");
        sb.append(this.mDataType == 1 ? "KEY_VALUE" : "STREAM");
        sb.append('}');
        return sb.toString();
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
