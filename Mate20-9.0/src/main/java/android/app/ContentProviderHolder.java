package android.app;

import android.content.ContentProviderNative;
import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class ContentProviderHolder implements Parcelable {
    public static final Parcelable.Creator<ContentProviderHolder> CREATOR = new Parcelable.Creator<ContentProviderHolder>() {
        public ContentProviderHolder createFromParcel(Parcel source) {
            return new ContentProviderHolder(source);
        }

        public ContentProviderHolder[] newArray(int size) {
            return new ContentProviderHolder[size];
        }
    };
    public IBinder connection;
    public final ProviderInfo info;
    public boolean noReleaseNeeded;
    public IContentProvider provider;

    public ContentProviderHolder(ProviderInfo _info) {
        this.info = _info;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.info.writeToParcel(dest, 0);
        if (this.provider != null) {
            dest.writeStrongBinder(this.provider.asBinder());
        } else {
            dest.writeStrongBinder(null);
        }
        dest.writeStrongBinder(this.connection);
        dest.writeInt(this.noReleaseNeeded ? 1 : 0);
    }

    private ContentProviderHolder(Parcel source) {
        this.info = ProviderInfo.CREATOR.createFromParcel(source);
        this.provider = ContentProviderNative.asInterface(source.readStrongBinder());
        this.connection = source.readStrongBinder();
        this.noReleaseNeeded = source.readInt() != 0;
    }
}
