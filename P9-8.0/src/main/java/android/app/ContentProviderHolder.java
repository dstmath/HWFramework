package android.app;

import android.content.ContentProviderNative;
import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ContentProviderHolder implements Parcelable {
    public static final Creator<ContentProviderHolder> CREATOR = new Creator<ContentProviderHolder>() {
        public ContentProviderHolder createFromParcel(Parcel source) {
            return new ContentProviderHolder(source, null);
        }

        public ContentProviderHolder[] newArray(int size) {
            return new ContentProviderHolder[size];
        }
    };
    public IBinder connection;
    public final ProviderInfo info;
    public boolean noReleaseNeeded;
    public IContentProvider provider;

    /* synthetic */ ContentProviderHolder(Parcel source, ContentProviderHolder -this1) {
        this(source);
    }

    public ContentProviderHolder(ProviderInfo _info) {
        this.info = _info;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        this.info.writeToParcel(dest, 0);
        if (this.provider != null) {
            dest.writeStrongBinder(this.provider.asBinder());
        } else {
            dest.writeStrongBinder(null);
        }
        dest.writeStrongBinder(this.connection);
        if (this.noReleaseNeeded) {
            i = 1;
        }
        dest.writeInt(i);
    }

    private ContentProviderHolder(Parcel source) {
        boolean z;
        this.info = (ProviderInfo) ProviderInfo.CREATOR.createFromParcel(source);
        this.provider = ContentProviderNative.asInterface(source.readStrongBinder());
        this.connection = source.readStrongBinder();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.noReleaseNeeded = z;
    }
}
