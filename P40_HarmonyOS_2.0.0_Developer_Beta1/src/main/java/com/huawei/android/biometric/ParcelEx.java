package com.huawei.android.biometric;

import android.os.Bundle;
import android.os.Parcel;
import java.util.ArrayList;
import java.util.List;

public class ParcelEx {
    private static final int DEFAULT_CAPACITY = 10;
    private Parcel dataReply;

    public Parcel getDataReply() {
        return this.dataReply;
    }

    public void setDataReply(Parcel dataReply2) {
        this.dataReply = dataReply2;
    }

    public void enforceInterface(String interfaceName) {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            parcel.enforceInterface(interfaceName);
        }
    }

    public void writeNoException() {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            parcel.writeNoException();
        }
    }

    public void writeInt(int val) {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            parcel.writeInt(val);
        }
    }

    public int readInt() {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            return parcel.readInt();
        }
        return 0;
    }

    public final Bundle readBundle() {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            return parcel.readBundle();
        }
        return null;
    }

    public void isWriteBoolean(boolean isVal) {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            parcel.writeBoolean(isVal);
        }
    }

    public void writeTypedList(List<FingerprintEx> fingerList) {
        Parcel parcel;
        if (fingerList != null) {
            ArrayList arrayList = new ArrayList(10);
            for (FingerprintEx fingerEx : fingerList) {
                arrayList.add(fingerEx.getFingerprint());
            }
            if (!(arrayList.isEmpty() || (parcel = this.dataReply) == null)) {
                parcel.writeTypedList(arrayList);
            }
        }
    }

    public boolean isReadBoolean() {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            return parcel.readBoolean();
        }
        return false;
    }

    public String readString() {
        Parcel parcel = this.dataReply;
        if (parcel != null) {
            return parcel.readString();
        }
        return "";
    }
}
