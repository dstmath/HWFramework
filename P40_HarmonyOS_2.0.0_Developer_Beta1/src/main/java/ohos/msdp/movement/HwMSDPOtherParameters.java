package ohos.msdp.movement;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class HwMSDPOtherParameters implements Parcelable {
    public static final Parcelable.Creator<HwMSDPOtherParameters> CREATOR = new Parcelable.Creator<HwMSDPOtherParameters>() {
        /* class ohos.msdp.movement.HwMSDPOtherParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPOtherParameters createFromParcel(Parcel parcel) {
            if (parcel != null) {
                return new HwMSDPOtherParameters(parcel.readDouble(), parcel.readDouble(), parcel.readDouble(), parcel.readDouble(), parcel.readString());
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPOtherParameters[] newArray(int i) {
            return new HwMSDPOtherParameters[i];
        }
    };
    private double mParam1;
    private double mParam2;
    private double mParam3;
    private double mParam4;
    private String mParam5;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public HwMSDPOtherParameters(double d, double d2, double d3, double d4, String str) {
        this.mParam1 = d;
        this.mParam2 = d2;
        this.mParam3 = d3;
        this.mParam4 = d4;
        this.mParam5 = str;
    }

    public double getParam1() {
        return this.mParam1;
    }

    public double getParam2() {
        return this.mParam2;
    }

    public double getParam3() {
        return this.mParam3;
    }

    public double getParam4() {
        return this.mParam4;
    }

    public String getParam5() {
        return this.mParam5;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        if (parcel != null) {
            parcel.writeDouble(this.mParam1);
            parcel.writeDouble(this.mParam2);
            parcel.writeDouble(this.mParam3);
            parcel.writeDouble(this.mParam4);
            parcel.writeString(this.mParam5);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "Param1=%s, Param2=%s, Param3=%s, Param4=%s, Param5=%s", Double.valueOf(this.mParam1), Double.valueOf(this.mParam2), Double.valueOf(this.mParam3), Double.valueOf(this.mParam4), this.mParam5);
    }
}
