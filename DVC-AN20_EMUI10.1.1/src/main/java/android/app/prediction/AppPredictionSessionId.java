package android.app.prediction;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class AppPredictionSessionId implements Parcelable {
    public static final Parcelable.Creator<AppPredictionSessionId> CREATOR = new Parcelable.Creator<AppPredictionSessionId>() {
        /* class android.app.prediction.AppPredictionSessionId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppPredictionSessionId createFromParcel(Parcel parcel) {
            return new AppPredictionSessionId(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AppPredictionSessionId[] newArray(int size) {
            return new AppPredictionSessionId[size];
        }
    };
    private final String mId;

    public AppPredictionSessionId(String id) {
        this.mId = id;
    }

    private AppPredictionSessionId(Parcel p) {
        this.mId = p.readString();
    }

    public boolean equals(Object o) {
        if (!getClass().equals(o != null ? o.getClass() : null)) {
            return false;
        }
        return this.mId.equals(((AppPredictionSessionId) o).mId);
    }

    public String toString() {
        return this.mId;
    }

    public int hashCode() {
        return this.mId.hashCode();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
    }
}
