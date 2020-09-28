package android.app.contentsuggestions;

import android.annotation.SystemApi;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class ContentClassification implements Parcelable {
    public static final Parcelable.Creator<ContentClassification> CREATOR = new Parcelable.Creator<ContentClassification>() {
        /* class android.app.contentsuggestions.ContentClassification.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ContentClassification createFromParcel(Parcel source) {
            return new ContentClassification(source.readString(), source.readBundle());
        }

        @Override // android.os.Parcelable.Creator
        public ContentClassification[] newArray(int size) {
            return new ContentClassification[size];
        }
    };
    private final String mClassificationId;
    private final Bundle mExtras;

    public ContentClassification(String classificationId, Bundle extras) {
        this.mClassificationId = classificationId;
        this.mExtras = extras;
    }

    public String getId() {
        return this.mClassificationId;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mClassificationId);
        dest.writeBundle(this.mExtras);
    }
}
