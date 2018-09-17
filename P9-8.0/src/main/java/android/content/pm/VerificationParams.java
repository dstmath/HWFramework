package android.content.pm;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

@Deprecated
public class VerificationParams implements Parcelable {
    public static final Creator<VerificationParams> CREATOR = new Creator<VerificationParams>() {
        public VerificationParams createFromParcel(Parcel source) {
            return new VerificationParams(source, null);
        }

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

    /* synthetic */ VerificationParams(Parcel source, VerificationParams -this1) {
        this(source);
    }

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
        if (this.mVerificationURI == null) {
            if (other.mVerificationURI != null) {
                return false;
            }
        } else if (!this.mVerificationURI.equals(other.mVerificationURI)) {
            return false;
        }
        if (this.mOriginatingURI == null) {
            if (other.mOriginatingURI != null) {
                return false;
            }
        } else if (!this.mOriginatingURI.equals(other.mOriginatingURI)) {
            return false;
        }
        if (this.mReferrer == null) {
            if (other.mReferrer != null) {
                return false;
            }
        } else if (!this.mReferrer.equals(other.mReferrer)) {
            return false;
        }
        return this.mOriginatingUid == other.mOriginatingUid && this.mInstallerUid == other.mInstallerUid;
    }

    public int hashCode() {
        int i = 1;
        int hash = (((this.mVerificationURI == null ? 1 : this.mVerificationURI.hashCode()) * 5) + 3) + ((this.mOriginatingURI == null ? 1 : this.mOriginatingURI.hashCode()) * 7);
        if (this.mReferrer != null) {
            i = this.mReferrer.hashCode();
        }
        return ((hash + (i * 11)) + (this.mOriginatingUid * 13)) + (this.mInstallerUid * 17);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(TO_STRING_PREFIX);
        sb.append("mVerificationURI=");
        sb.append(this.mVerificationURI.toString());
        sb.append(",mOriginatingURI=");
        sb.append(this.mOriginatingURI.toString());
        sb.append(",mReferrer=");
        sb.append(this.mReferrer.toString());
        sb.append(",mOriginatingUid=");
        sb.append(this.mOriginatingUid);
        sb.append(",mInstallerUid=");
        sb.append(this.mInstallerUid);
        sb.append('}');
        return sb.toString();
    }

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
