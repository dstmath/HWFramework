package com.huawei.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class HwKeymasterCertificateChain implements Parcelable {
    public static final Parcelable.Creator<HwKeymasterCertificateChain> CREATOR = new Parcelable.Creator<HwKeymasterCertificateChain>() {
        public HwKeymasterCertificateChain createFromParcel(Parcel in) {
            return new HwKeymasterCertificateChain(in);
        }

        public HwKeymasterCertificateChain[] newArray(int size) {
            return new HwKeymasterCertificateChain[size];
        }
    };
    private List<byte[]> mCertificates;

    public HwKeymasterCertificateChain() {
        this.mCertificates = null;
    }

    public HwKeymasterCertificateChain(List<byte[]> mCertificates2) {
        this.mCertificates = mCertificates2;
    }

    private HwKeymasterCertificateChain(Parcel in) {
        readFromParcel(in);
    }

    public List<byte[]> getCertificates() {
        return this.mCertificates;
    }

    public void setCertificates(List<byte[]> certs) {
        this.mCertificates = certs;
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
