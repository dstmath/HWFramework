package ohos.bluetooth;

import java.util.Arrays;
import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class A2dpCodecStatus implements Sequenceable {
    private A2dpCodecInfo mCodecInfo;
    private A2dpCodecInfo[] mLocalCodecs;
    private A2dpCodecInfo[] mSelectableCodecs;

    public A2dpCodecInfo getCurrentCodecInfo() {
        return this.mCodecInfo;
    }

    public A2dpCodecInfo[] getLocalCodecs() {
        return this.mLocalCodecs;
    }

    public A2dpCodecInfo[] getselectableCodecs() {
        return this.mSelectableCodecs;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof A2dpCodecStatus)) {
            return false;
        }
        A2dpCodecStatus a2dpCodecStatus = (A2dpCodecStatus) obj;
        if (!Objects.equals(a2dpCodecStatus.mCodecInfo, this.mCodecInfo) || !isSameCodecInfoArray(a2dpCodecStatus.mLocalCodecs, this.mLocalCodecs) || !isSameCodecInfoArray(a2dpCodecStatus.mSelectableCodecs, this.mSelectableCodecs)) {
            return false;
        }
        return true;
    }

    private static boolean isSameCodecInfoArray(A2dpCodecInfo[] a2dpCodecInfoArr, A2dpCodecInfo[] a2dpCodecInfoArr2) {
        if (a2dpCodecInfoArr == null) {
            return a2dpCodecInfoArr2 == null;
        }
        if (a2dpCodecInfoArr2 != null && a2dpCodecInfoArr.length == a2dpCodecInfoArr2.length) {
            return Arrays.asList(a2dpCodecInfoArr).containsAll(Arrays.asList(a2dpCodecInfoArr2));
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.mCodecInfo, this.mLocalCodecs, this.mSelectableCodecs);
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(this.mCodecInfo);
        A2dpCodecInfo[] a2dpCodecInfoArr = this.mLocalCodecs;
        int i = 0;
        parcel.writeInt(a2dpCodecInfoArr == null ? 0 : a2dpCodecInfoArr.length);
        parcel.writeSequenceableArray(this.mLocalCodecs);
        A2dpCodecInfo[] a2dpCodecInfoArr2 = this.mSelectableCodecs;
        if (a2dpCodecInfoArr2 != null) {
            i = a2dpCodecInfoArr2.length;
        }
        parcel.writeInt(i);
        parcel.writeSequenceableArray(this.mSelectableCodecs);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.mCodecInfo = new A2dpCodecInfo();
        parcel.readSequenceable(this.mCodecInfo);
        int readInt = parcel.readInt();
        if (readInt >= 0 && readInt <= 50) {
            this.mLocalCodecs = new A2dpCodecInfo[readInt];
            for (int i = 0; i < readInt; i++) {
                A2dpCodecInfo a2dpCodecInfo = new A2dpCodecInfo();
                parcel.readSequenceable(a2dpCodecInfo);
                this.mLocalCodecs[i] = a2dpCodecInfo;
            }
            int readInt2 = parcel.readInt();
            if (readInt2 >= 0 && readInt2 <= 50) {
                this.mSelectableCodecs = new A2dpCodecInfo[readInt2];
                for (int i2 = 0; i2 < readInt; i2++) {
                    A2dpCodecInfo a2dpCodecInfo2 = new A2dpCodecInfo();
                    parcel.readSequenceable(a2dpCodecInfo2);
                    this.mSelectableCodecs[i2] = a2dpCodecInfo2;
                }
                return true;
            }
        }
        return false;
    }
}
