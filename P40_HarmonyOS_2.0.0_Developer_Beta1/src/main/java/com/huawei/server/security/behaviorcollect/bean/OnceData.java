package com.huawei.server.security.behaviorcollect.bean;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class OnceData implements Parcelable {
    public static final Parcelable.Creator<OnceData> CREATOR = new Parcelable.Creator<OnceData>() {
        /* class com.huawei.server.security.behaviorcollect.bean.OnceData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OnceData createFromParcel(Parcel source) {
            return new OnceData(source);
        }

        @Override // android.os.Parcelable.Creator
        public OnceData[] newArray(int size) {
            return new OnceData[size];
        }
    };
    private static final int MAX_DATA_LEN = 1000;
    private boolean isActiveTouched;
    private ArrayList<SensorData> sensorDatas;
    private ArrayList<TouchPoint> touchPoints;

    public OnceData(boolean isActiveTouched2, ArrayList<SensorData> sensorDatas2, ArrayList<TouchPoint> touchPoints2) {
        this.isActiveTouched = isActiveTouched2;
        if (sensorDatas2 != null && sensorDatas2.size() <= MAX_DATA_LEN) {
            Object sensorDataObj = sensorDatas2.clone();
            if (sensorDataObj instanceof ArrayList) {
                this.sensorDatas = (ArrayList) sensorDataObj;
            }
        }
        if (touchPoints2 != null && touchPoints2.size() <= MAX_DATA_LEN) {
            Object touchDataObj = touchPoints2.clone();
            if (touchDataObj instanceof ArrayList) {
                this.touchPoints = (ArrayList) touchDataObj;
            }
        }
    }

    private OnceData(Parcel source) {
        this.isActiveTouched = source.readBoolean();
        this.sensorDatas = getSensorDataListOrDefault(source.readArrayList(SensorData.class.getClassLoader()));
        this.touchPoints = getTouchPointListOrDefault(source.readArrayList(TouchPoint.class.getClassLoader()));
    }

    public boolean isActiveTouched() {
        return this.isActiveTouched;
    }

    public ArrayList<SensorData> getSensorDatas() {
        return this.sensorDatas;
    }

    public ArrayList<TouchPoint> getTouchPoints() {
        return this.touchPoints;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.isActiveTouched);
        dest.writeList(this.sensorDatas);
        dest.writeList(this.touchPoints);
    }

    private ArrayList<TouchPoint> getTouchPointListOrDefault(ArrayList<TouchPoint> touchPoints2) {
        if (touchPoints2 == null || touchPoints2.size() > MAX_DATA_LEN) {
            return new ArrayList<>(0);
        }
        return touchPoints2;
    }

    private ArrayList<SensorData> getSensorDataListOrDefault(ArrayList<SensorData> sensorDatas2) {
        if (sensorDatas2 == null || sensorDatas2.size() > MAX_DATA_LEN) {
            return new ArrayList<>(0);
        }
        return sensorDatas2;
    }
}
