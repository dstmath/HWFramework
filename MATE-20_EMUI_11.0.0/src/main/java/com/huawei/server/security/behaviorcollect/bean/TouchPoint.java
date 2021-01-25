package com.huawei.server.security.behaviorcollect.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class TouchPoint implements Parcelable {
    public static final Parcelable.Creator<TouchPoint> CREATOR = new Parcelable.Creator<TouchPoint>() {
        /* class com.huawei.server.security.behaviorcollect.bean.TouchPoint.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TouchPoint createFromParcel(Parcel source) {
            return new TouchPoint(source);
        }

        @Override // android.os.Parcelable.Creator
        public TouchPoint[] newArray(int size) {
            return new TouchPoint[size];
        }
    };
    private int action;
    private double ori;
    private int pointerId;
    private double positionX;
    private double positionY;
    private double pressure;
    private double timestamp;
    private double width;

    public TouchPoint(double timestamp2, double positionX2, double positionY2, double pressure2, double width2) {
        this.timestamp = timestamp2;
        this.positionX = positionX2;
        this.positionY = positionY2;
        this.pressure = pressure2;
        this.width = width2;
    }

    public TouchPoint(Parcel source) {
        this.timestamp = source.readDouble();
        this.positionX = source.readDouble();
        this.positionY = source.readDouble();
        this.pressure = source.readDouble();
        this.width = source.readDouble();
        this.pointerId = source.readInt();
        this.ori = source.readDouble();
        this.action = source.readInt();
    }

    public double getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(double timestamp2) {
        this.timestamp = timestamp2;
    }

    public double getPositionX() {
        return this.positionX;
    }

    public void setPositionX(double positionX2) {
        this.positionX = positionX2;
    }

    public double getPositionY() {
        return this.positionY;
    }

    public void setPositionY(double positionY2) {
        this.positionY = positionY2;
    }

    public double getPressure() {
        return this.pressure;
    }

    public void setPressure(double pressure2) {
        this.pressure = pressure2;
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double width2) {
        this.width = width2;
    }

    public int getPointerId() {
        return this.pointerId;
    }

    public void setPointerId(int pointerId2) {
        this.pointerId = pointerId2;
    }

    public double getOri() {
        return this.ori;
    }

    public void setOri(double ori2) {
        this.ori = ori2;
    }

    public int getAction() {
        return this.action;
    }

    public void setAction(int action2) {
        this.action = action2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.timestamp);
        dest.writeDouble(this.positionX);
        dest.writeDouble(this.positionY);
        dest.writeDouble(this.pressure);
        dest.writeDouble(this.width);
        dest.writeInt(this.pointerId);
        dest.writeDouble(this.ori);
        dest.writeInt(this.action);
    }
}
