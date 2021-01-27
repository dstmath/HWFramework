package ohos.location;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class RequestParam implements Sequenceable {
    public static final int PRIORITY_ACCURACY = 513;
    public static final int PRIORITY_FAST_FIRST_FIX = 515;
    public static final int PRIORITY_LOW_POWER = 514;
    public static final int PRIORITY_UNSET = 512;
    public static final int SCENE_CAR_HAILING = 771;
    public static final int SCENE_DAILY_LIFE_SERVICE = 772;
    public static final int SCENE_NAVIGATION = 769;
    public static final int SCENE_NO_POWER = 773;
    public static final int SCENE_TRAJECTORY_TRACKING = 770;
    public static final int SCENE_UNSET = 768;
    private int distanceInterval = 0;
    private float maxAccuracy = 0.0f;
    private int priority = 768;
    private int scenario = 512;
    private int timeInterval = 1;

    public RequestParam(int i) throws IllegalArgumentException {
        if (i == 769 || i == 770 || i == 771 || i == 772 || i == 773 || i == 768) {
            this.scenario = i;
            return;
        }
        throw new IllegalArgumentException();
    }

    public RequestParam(int i, int i2, int i3) throws IllegalArgumentException {
        if (i == 513 || i == 514 || i == 515 || i == 512) {
            this.priority = i;
            this.timeInterval = i2;
            this.distanceInterval = i3;
            this.scenario = 768;
            return;
        }
        throw new IllegalArgumentException();
    }

    public int getScenario() {
        return this.scenario;
    }

    public void setScenario(int i) throws IllegalArgumentException {
        if (i == 769 || i == 770 || i == 771 || i == 772 || i == 773 || i == 768) {
            this.scenario = i;
            return;
        }
        throw new IllegalArgumentException();
    }

    public int getPriorityLevel() {
        return this.priority;
    }

    public void setPriorityLevel(int i) throws IllegalArgumentException {
        if (i == 513 || i == 514 || i == 515 || i == 512) {
            this.priority = i;
            return;
        }
        throw new IllegalArgumentException();
    }

    public int getTimeInterval() {
        return this.timeInterval;
    }

    public void setTimeInterval(int i) {
        if (i >= 0) {
            this.timeInterval = i;
        } else {
            this.timeInterval = 0;
        }
    }

    public int getDistanceInterval() {
        return this.distanceInterval;
    }

    public void setDistanceInterval(int i) {
        if (i >= 1) {
            this.distanceInterval = i;
        } else {
            this.distanceInterval = 0;
        }
    }

    public float getMaxAccuracy() {
        return this.maxAccuracy;
    }

    public void setMaxAccuracy(float f) {
        if (f >= 1.0f) {
            this.maxAccuracy = f;
        } else {
            this.maxAccuracy = 0.0f;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.scenario = parcel.readInt();
        this.priority = parcel.readInt();
        this.timeInterval = parcel.readInt();
        this.distanceInterval = parcel.readInt();
        this.maxAccuracy = parcel.readFloat();
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeInt(this.scenario);
        parcel.writeInt(this.priority);
        parcel.writeInt(this.timeInterval);
        parcel.writeInt(this.distanceInterval);
        parcel.writeFloat(this.maxAccuracy);
        return true;
    }
}
