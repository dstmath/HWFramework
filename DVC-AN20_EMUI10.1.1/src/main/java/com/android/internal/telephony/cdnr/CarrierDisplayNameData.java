package com.android.internal.telephony.cdnr;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class CarrierDisplayNameData implements Parcelable {
    public static final Parcelable.Creator<CarrierDisplayNameData> CREATOR = new Parcelable.Creator<CarrierDisplayNameData>() {
        /* class com.android.internal.telephony.cdnr.CarrierDisplayNameData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CarrierDisplayNameData createFromParcel(Parcel source) {
            return new CarrierDisplayNameData(source);
        }

        @Override // android.os.Parcelable.Creator
        public CarrierDisplayNameData[] newArray(int size) {
            return new CarrierDisplayNameData[size];
        }
    };
    private final String mDataSpn;
    private final String mPlmn;
    private final boolean mShowPlmn;
    private final boolean mShowSpn;
    private final String mSpn;

    private CarrierDisplayNameData(String spn, String dataSpn, boolean showSpn, String plmn, boolean showPlmn) {
        this.mSpn = spn;
        this.mDataSpn = dataSpn;
        this.mShowSpn = showSpn;
        this.mPlmn = plmn;
        this.mShowPlmn = showPlmn;
    }

    public String getSpn() {
        return this.mSpn;
    }

    public String getDataSpn() {
        return this.mDataSpn;
    }

    public String getPlmn() {
        return this.mPlmn;
    }

    public boolean shouldShowSpn() {
        return this.mShowSpn;
    }

    public boolean shouldShowPlmn() {
        return this.mShowPlmn;
    }

    public String toString() {
        return String.format("{ spn = %s, dataSpn = %s, showSpn = %b, plmn = %s, showPlmn = %b", this.mSpn, this.mDataSpn, Boolean.valueOf(this.mShowSpn), this.mPlmn, Boolean.valueOf(this.mShowPlmn));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSpn);
        dest.writeString(this.mDataSpn);
        dest.writeString(this.mPlmn);
        dest.writeBoolean(this.mShowSpn);
        dest.writeBoolean(this.mShowPlmn);
    }

    private CarrierDisplayNameData(Parcel source) {
        this.mSpn = source.readString();
        this.mDataSpn = source.readString();
        this.mPlmn = source.readString();
        this.mShowSpn = source.readBoolean();
        this.mShowPlmn = source.readBoolean();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CarrierDisplayNameData that = (CarrierDisplayNameData) o;
        if (this.mShowSpn != that.mShowSpn || this.mShowPlmn != that.mShowPlmn || !Objects.equals(this.mSpn, that.mSpn) || !Objects.equals(this.mDataSpn, that.mDataSpn) || !Objects.equals(this.mPlmn, that.mPlmn)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mSpn, this.mDataSpn, this.mPlmn, Boolean.valueOf(this.mShowSpn), Boolean.valueOf(this.mShowPlmn));
    }

    public static final class Builder {
        private String mDataSpn = null;
        private String mPlmn = null;
        private boolean mShowPlmn = false;
        private boolean mShowSpn = false;
        private String mSpn = null;

        public CarrierDisplayNameData build() {
            return new CarrierDisplayNameData(this.mSpn, this.mDataSpn, this.mShowSpn, this.mPlmn, this.mShowPlmn);
        }

        public Builder setSpn(String spn) {
            this.mSpn = spn;
            return this;
        }

        public Builder setDataSpn(String dataSpn) {
            this.mDataSpn = dataSpn;
            return this;
        }

        public Builder setPlmn(String plmn) {
            this.mPlmn = plmn;
            return this;
        }

        public Builder setShowSpn(boolean showSpn) {
            this.mShowSpn = showSpn;
            return this;
        }

        public Builder setShowPlmn(boolean showPlmn) {
            this.mShowPlmn = showPlmn;
            return this;
        }
    }
}
