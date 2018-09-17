package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class KeymasterCertificateChain implements Parcelable {
    public static final Creator<KeymasterCertificateChain> CREATOR = new Creator<KeymasterCertificateChain>() {
        public KeymasterCertificateChain createFromParcel(Parcel in) {
            return new KeymasterCertificateChain(in, null);
        }

        public KeymasterCertificateChain[] newArray(int size) {
            return new KeymasterCertificateChain[size];
        }
    };
    private List<byte[]> mCertificates;

    /* synthetic */ KeymasterCertificateChain(Parcel in, KeymasterCertificateChain -this1) {
        this(in);
    }

    public KeymasterCertificateChain() {
        this.mCertificates = null;
    }

    public KeymasterCertificateChain(List<byte[]> mCertificates) {
        this.mCertificates = mCertificates;
    }

    private KeymasterCertificateChain(Parcel in) {
        readFromParcel(in);
    }

    public List<byte[]> getCertificates() {
        return this.mCertificates;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mCertificates == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.mCertificates.size());
        for (byte[] arg : this.mCertificates) {
            out.writeByteArray(arg);
        }
    }

    public void readFromParcel(Parcel in) {
        int length = in.readInt();
        this.mCertificates = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            this.mCertificates.add(in.createByteArray());
        }
    }

    public int describeContents() {
        return 0;
    }
}
