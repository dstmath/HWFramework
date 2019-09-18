package android.media.tv;

import android.annotation.SystemApi;
import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class TvContentRatingSystemInfo implements Parcelable {
    public static final Parcelable.Creator<TvContentRatingSystemInfo> CREATOR = new Parcelable.Creator<TvContentRatingSystemInfo>() {
        public TvContentRatingSystemInfo createFromParcel(Parcel in) {
            return new TvContentRatingSystemInfo(in);
        }

        public TvContentRatingSystemInfo[] newArray(int size) {
            return new TvContentRatingSystemInfo[size];
        }
    };
    private final ApplicationInfo mApplicationInfo;
    private final Uri mXmlUri;

    public static final TvContentRatingSystemInfo createTvContentRatingSystemInfo(int xmlResourceId, ApplicationInfo applicationInfo) {
        return new TvContentRatingSystemInfo(new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(applicationInfo.packageName).appendPath(String.valueOf(xmlResourceId)).build(), applicationInfo);
    }

    private TvContentRatingSystemInfo(Uri xmlUri, ApplicationInfo applicationInfo) {
        this.mXmlUri = xmlUri;
        this.mApplicationInfo = applicationInfo;
    }

    public final boolean isSystemDefined() {
        return (this.mApplicationInfo.flags & 1) != 0;
    }

    public final Uri getXmlUri() {
        return this.mXmlUri;
    }

    private TvContentRatingSystemInfo(Parcel in) {
        this.mXmlUri = (Uri) in.readParcelable(null);
        this.mApplicationInfo = (ApplicationInfo) in.readParcelable(null);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mXmlUri, flags);
        dest.writeParcelable(this.mApplicationInfo, flags);
    }

    public int describeContents() {
        return 0;
    }
}
