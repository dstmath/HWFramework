package com.huawei.haptic;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HwHapticCurve implements Parcelable {
    public static final Parcelable.Creator<HwHapticCurve> CREATOR = new Parcelable.Creator<HwHapticCurve>() {
        /* class com.huawei.haptic.HwHapticCurve.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwHapticCurve createFromParcel(Parcel in) {
            return new HwHapticCurve(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwHapticCurve[] newArray(int size) {
            if (size > 64) {
                return new HwHapticCurve[0];
            }
            return new HwHapticCurve[size];
        }
    };
    public static final int CURVE_TYPE_INTENSITY = 1;
    public static final int CURVE_TYPE_SHARPNESS = 2;
    public List<HwAdjustPoint> mAdjustPoints = new ArrayList();

    public HwHapticCurve() {
    }

    public HwHapticCurve(Parcel in) {
        this.mAdjustPoints = in.readArrayList(null);
    }

    protected HwHapticCurve(Builder builder) {
        this.mAdjustPoints = builder.mAdjustPoints;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(this.mAdjustPoints);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static class HwAdjustPoint implements Parcelable {
        public static final Parcelable.Creator<HwAdjustPoint> CREATOR = new Parcelable.Creator<HwAdjustPoint>() {
            /* class com.huawei.haptic.HwHapticCurve.HwAdjustPoint.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public HwAdjustPoint createFromParcel(Parcel in) {
                return new HwAdjustPoint(in);
            }

            @Override // android.os.Parcelable.Creator
            public HwAdjustPoint[] newArray(int size) {
                return new HwAdjustPoint[size];
            }
        };
        public int mTimeStamp;
        public float mValue;

        public HwAdjustPoint(int time, float value) {
            this.mTimeStamp = time;
            this.mValue = value;
        }

        public HwAdjustPoint(Parcel in) {
            this.mTimeStamp = in.readInt();
            this.mValue = in.readFloat();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mTimeStamp);
            out.writeFloat(this.mValue);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }
    }

    public static class TimeStampComparator implements Comparator<Object> {
        @Override // java.util.Comparator
        public int compare(Object object1, Object object2) {
            if (!(object1 instanceof HwAdjustPoint) || !(object2 instanceof HwAdjustPoint)) {
                return 0;
            }
            return Integer.compare(((HwAdjustPoint) object1).mTimeStamp, ((HwAdjustPoint) object2).mTimeStamp);
        }
    }

    public static class Builder {
        private final List<HwAdjustPoint> mAdjustPoints = new ArrayList();

        public Builder addHwAdjustPoint(int time, float value) {
            this.mAdjustPoints.add(new HwAdjustPoint(time, value));
            return this;
        }

        public Builder addHwAdjustPoint(HwAdjustPoint point) {
            this.mAdjustPoints.add(point);
            return this;
        }

        public HwHapticCurve build() {
            return new HwHapticCurve(this);
        }
    }
}
