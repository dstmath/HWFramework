package android.content.pm;

import android.content.pm.PackageParser;
import android.os.Parcel;
import android.os.Parcelable;

public final class SigningInfo implements Parcelable {
    public static final Parcelable.Creator<SigningInfo> CREATOR = new Parcelable.Creator<SigningInfo>() {
        public SigningInfo createFromParcel(Parcel source) {
            return new SigningInfo(source);
        }

        public SigningInfo[] newArray(int size) {
            return new SigningInfo[size];
        }
    };
    private final PackageParser.SigningDetails mSigningDetails;

    public SigningInfo() {
        this.mSigningDetails = PackageParser.SigningDetails.UNKNOWN;
    }

    public SigningInfo(PackageParser.SigningDetails signingDetails) {
        this.mSigningDetails = new PackageParser.SigningDetails(signingDetails);
    }

    public SigningInfo(SigningInfo orig) {
        this.mSigningDetails = new PackageParser.SigningDetails(orig.mSigningDetails);
    }

    private SigningInfo(Parcel source) {
        this.mSigningDetails = PackageParser.SigningDetails.CREATOR.createFromParcel(source);
    }

    public boolean hasMultipleSigners() {
        return this.mSigningDetails.signatures != null && this.mSigningDetails.signatures.length > 1;
    }

    public boolean hasPastSigningCertificates() {
        return (this.mSigningDetails.signatures == null || this.mSigningDetails.pastSigningCertificates == null) ? false : true;
    }

    public Signature[] getSigningCertificateHistory() {
        if (hasMultipleSigners()) {
            return null;
        }
        if (!hasPastSigningCertificates()) {
            return this.mSigningDetails.signatures;
        }
        return this.mSigningDetails.pastSigningCertificates;
    }

    public Signature[] getApkContentsSigners() {
        return this.mSigningDetails.signatures;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        this.mSigningDetails.writeToParcel(dest, parcelableFlags);
    }
}
