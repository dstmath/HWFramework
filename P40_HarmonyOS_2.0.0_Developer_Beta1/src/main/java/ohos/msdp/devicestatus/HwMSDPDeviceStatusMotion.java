package ohos.msdp.devicestatus;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class HwMSDPDeviceStatusMotion implements Parcelable {
    public static final Parcelable.Creator<HwMSDPDeviceStatusMotion> CREATOR = new Parcelable.Creator<HwMSDPDeviceStatusMotion>() {
        /* class ohos.msdp.devicestatus.HwMSDPDeviceStatusMotion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusMotion createFromParcel(Parcel parcel) {
            return new HwMSDPDeviceStatusMotion(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusMotion[] newArray(int i) {
            return new HwMSDPDeviceStatusMotion[i];
        }
    };
    private int mActivityRunSteps = 0;
    private int mActivityState = 0;
    private int mActivityTotalSteps = 0;
    private int mActivityWalkSteps = 0;
    private int mMotionDirection = 0;
    private int mMotionRecoResult = 0;
    private int mMotionType = 0;
    public Bundle motionExtras = null;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public HwMSDPDeviceStatusMotion() {
    }

    public HwMSDPDeviceStatusMotion(Parcel parcel) {
        this.mMotionType = parcel.readInt();
        this.mMotionRecoResult = parcel.readInt();
        this.mMotionDirection = parcel.readInt();
    }

    public Bundle getMotionExtras() {
        return this.motionExtras;
    }

    public void setMotionExtras(Bundle bundle) {
        this.motionExtras = bundle;
    }

    public int getMotionType() {
        return this.mMotionType;
    }

    public void setMotionType(int i) {
        this.mMotionType = i;
    }

    public int getMotionRecoResult() {
        return this.mMotionRecoResult;
    }

    public void setMotionRecoResult(int i) {
        this.mMotionRecoResult = i;
    }

    public int getMotionDirection() {
        return this.mMotionDirection;
    }

    public void setMotionDirection(int i) {
        this.mMotionDirection = i;
    }

    public int getActivityState() {
        return this.mActivityState;
    }

    public void setActivityState(int i) {
        this.mActivityState = i;
    }

    public int getTotalSteps() {
        return this.mActivityTotalSteps;
    }

    public void setTotalSteps(int i) {
        this.mActivityTotalSteps = i;
    }

    public int getActivityWalkSteps() {
        return this.mActivityWalkSteps;
    }

    public void setActivityWalkSteps(int i) {
        this.mActivityWalkSteps = i;
    }

    public int getActivityRunSteps() {
        return this.mActivityRunSteps;
    }

    public void setActivityRunSteps(int i) {
        this.mActivityRunSteps = i;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mMotionType);
        parcel.writeInt(this.mMotionRecoResult);
        parcel.writeInt(this.mMotionDirection);
    }

    @Override // java.lang.Object
    public String toString() {
        return "HwMSDPDeviceStatusMotion{mMotionExtras=" + this.motionExtras + ", mMotionType=" + this.mMotionType + ", mMotionRecoResult=" + this.mMotionRecoResult + ", mMotionDirection=" + this.mMotionDirection + ", mActivityState=" + this.mActivityState + ", mActivityTotalSteps=" + this.mActivityTotalSteps + ", mActivityWalkSteps=" + this.mActivityWalkSteps + ", mActivityRunSteps=" + this.mActivityRunSteps + '}';
    }
}
