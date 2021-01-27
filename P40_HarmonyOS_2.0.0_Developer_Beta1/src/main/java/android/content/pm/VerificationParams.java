package android.content.pm;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

@Deprecated
public class VerificationParams implements Parcelable {
    public static final Parcelable.Creator<VerificationParams> CREATOR = new Parcelable.Creator<VerificationParams>() {
        /* class android.content.pm.VerificationParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VerificationParams createFromParcel(Parcel source) {
            return new VerificationParams(source);
        }

        @Override // android.os.Parcelable.Creator
        public VerificationParams[] newArray(int size) {
            return new VerificationParams[size];
        }
    };
    public static final int NO_UID = -1;
    private static final String TO_STRING_PREFIX = "VerificationParams{";
    private int mInstallerUid;
    private final Uri mOriginatingURI;
    private final int mOriginatingUid;
    private final Uri mReferrer;
    private final Uri mVerificationURI;

    public VerificationParams(Uri verificationURI, Uri originatingURI, Uri referrer, int originatingUid) {
        this.mVerificationURI = verificationURI;
        this.mOriginatingURI = originatingURI;
        this.mReferrer = referrer;
        this.mOriginatingUid = originatingUid;
        this.mInstallerUid = -1;
    }

    public Uri getVerificationURI() {
        return this.mVerificationURI;
    }

    public Uri getOriginatingURI() {
        return this.mOriginatingURI;
    }

    public Uri getReferrer() {
        return this.mReferrer;
    }

    public int getOriginatingUid() {
        return this.mOriginatingUid;
    }

    public int getInstallerUid() {
        return this.mInstallerUid;
    }

    public void setInstallerUid(int uid) {
        this.mInstallerUid = uid;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerificationParams)) {
            return false;
        }
        VerificationParams other = (VerificationParams) o;
        Uri uri = this.mVerificationURI;
        if (uri == null) {
            if (other.mVerificationURI != null) {
                return false;
            }
        } else if (!uri.equals(other.mVerificationURI)) {
            return false;
        }
        Uri uri2 = this.mOriginatingURI;
        if (uri2 == null) {
            if (other.mOriginatingURI != null) {
                return false;
            }
        } else if (!uri2.equals(other.mOriginatingURI)) {
            return false;
        }
        Uri uri3 = this.mReferrer;
        if (uri3 == null) {
            if (other.mReferrer != null) {
                return false;
            }
        } else if (!uri3.equals(other.mReferrer)) {
            return false;
        }
        if (this.mOriginatingUid == other.mOriginatingUid && this.mInstallerUid == other.mInstallerUid) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        Uri uri = this.mVerificationURI;
        int i = 1;
        int hash = 3 + ((uri == null ? 1 : uri.hashCode()) * 5);
        Uri uri2 = this.mOriginatingURI;
        int hash2 = hash + ((uri2 == null ? 1 : uri2.hashCode()) * 7);
        Uri uri3 = this.mReferrer;
        if (uri3 != null) {
            i = uri3.hashCode();
        }
        return hash2 + (i * 11) + (this.mOriginatingUid * 13) + (this.mInstallerUid * 17);
    }

    public String toString() {
        return TO_STRING_PREFIX + "mVerificationURI=" + this.mVerificationURI.toString() + ",mOriginatingURI=" + this.mOriginatingURI.toString() + ",mReferrer=" + this.mReferrer.toString() + ",mOriginatingUid=" + this.mOriginatingUid + ",mInstallerUid=" + this.mInstallerUid + '}';
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mVerificationURI, 0);
        dest.writeParcelable(this.mOriginatingURI, 0);
        dest.writeParcelable(this.mReferrer, 0);
        dest.writeInt(this.mOriginatingUid);
        dest.writeInt(this.mInstallerUid);
    }

    private VerificationParams(Parcel source) {
        this.mVerificationURI = (Uri) source.readParcelable(Uri.class.getClassLoader());
        this.mOriginatingURI = (Uri) source.readParcelable(Uri.class.getClassLoader());
        this.mReferrer = (Uri) source.readParcelable(Uri.class.getClassLoader());
        this.mOriginatingUid = source.readInt();
        this.mInstallerUid = source.readInt();
    }
}
