package ohos.security.permission;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class UriPermissionDef implements Sequenceable {
    public static final Sequenceable.Producer<UriPermissionDef> PRODUCER = new Sequenceable.Producer<UriPermissionDef>() {
        /* class ohos.security.permission.UriPermissionDef.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public UriPermissionDef createFromParcel(Parcel parcel) {
            return new UriPermissionDef(parcel);
        }
    };
    private Uri mHosUri;
    private long mPermissionPersistedTime;
    private int mReadWriteModeFlags;

    public UriPermissionDef() {
    }

    public UriPermissionDef(Uri uri, int i, long j) {
        this.mHosUri = uri;
        this.mReadWriteModeFlags = i;
        this.mPermissionPersistedTime = j;
    }

    public UriPermissionDef(Parcel parcel) {
        if (parcel != null) {
            this.mHosUri = Uri.readFromParcel(parcel);
            this.mReadWriteModeFlags = parcel.readInt();
            this.mPermissionPersistedTime = parcel.readLong();
        }
    }

    public Uri getUri() {
        return this.mHosUri;
    }

    public boolean isGrantReadPermission() {
        return (this.mReadWriteModeFlags & 1) != 0;
    }

    public boolean isGrantWritePermission() {
        return (this.mReadWriteModeFlags & 2) != 0;
    }

    public long getPersistedTime() {
        return this.mPermissionPersistedTime;
    }

    public String toString() {
        return "UriPermissionDef {uri=" + this.mHosUri + ", modeFlags=" + this.mReadWriteModeFlags + ", persistedTime=" + this.mPermissionPersistedTime + "}";
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeSequenceable(this.mHosUri);
        parcel.writeInt(this.mReadWriteModeFlags);
        parcel.writeLong(this.mPermissionPersistedTime);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.mHosUri = Uri.readFromParcel(parcel);
        this.mReadWriteModeFlags = parcel.readInt();
        this.mPermissionPersistedTime = parcel.readLong();
        return true;
    }
}
