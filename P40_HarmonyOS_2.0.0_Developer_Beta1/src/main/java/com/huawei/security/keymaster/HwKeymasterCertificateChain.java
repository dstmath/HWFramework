package com.huawei.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class HwKeymasterCertificateChain implements Parcelable {
    private static final int ARRAY_LEN_MAX = 5242880;
    public static final Parcelable.Creator<HwKeymasterCertificateChain> CREATOR = new Parcelable.Creator<HwKeymasterCertificateChain>() {
        /* class com.huawei.security.keymaster.HwKeymasterCertificateChain.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwKeymasterCertificateChain createFromParcel(Parcel in) {
            return new HwKeymasterCertificateChain(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwKeymasterCertificateChain[] newArray(int size) {
            return new HwKeymasterCertificateChain[size];
        }
    };
    private List<byte[]> mCertificates;

    public HwKeymasterCertificateChain() {
        this.mCertificates = new ArrayList();
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        List<byte[]> list = this.mCertificates;
        if (list == null || list.size() == 0) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.mCertificates.size());
        for (byte[] arg : this.mCertificates) {
            out.writeByteArray(arg);
        }
    }

    public final void readFromParcel(Parcel in) {
        int length = in.readInt();
        if (length > 0 && length <= ARRAY_LEN_MAX) {
            this.mCertificates = new ArrayList(length);
            for (int i = 0; i < length; i++) {
                this.mCertificates.add(in.createByteArray());
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
