package android.content;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ContentProviderResult implements Parcelable {
    public static final Parcelable.Creator<ContentProviderResult> CREATOR = new Parcelable.Creator<ContentProviderResult>() {
        public ContentProviderResult createFromParcel(Parcel source) {
            return new ContentProviderResult(source);
        }

        public ContentProviderResult[] newArray(int size) {
            return new ContentProviderResult[size];
        }
    };
    public final Integer count;
    public final Uri uri;

    public ContentProviderResult(Uri uri2) {
        if (uri2 != null) {
            this.uri = uri2;
            this.count = null;
            return;
        }
        throw new IllegalArgumentException("uri must not be null");
    }

    public ContentProviderResult(int count2) {
        this.count = Integer.valueOf(count2);
        this.uri = null;
    }

    public ContentProviderResult(Parcel source) {
        if (source.readInt() == 1) {
            this.count = Integer.valueOf(source.readInt());
            this.uri = null;
            return;
        }
        this.count = null;
        this.uri = Uri.CREATOR.createFromParcel(source);
    }

    public ContentProviderResult(ContentProviderResult cpr, int userId) {
        this.uri = ContentProvider.maybeAddUserId(cpr.uri, userId);
        this.count = cpr.count;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.uri == null) {
            dest.writeInt(1);
            dest.writeInt(this.count.intValue());
            return;
        }
        dest.writeInt(2);
        this.uri.writeToParcel(dest, 0);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        if (this.uri != null) {
            return "ContentProviderResult(uri=" + this.uri.toString() + ")";
        }
        return "ContentProviderResult(count=" + this.count + ")";
    }
}
