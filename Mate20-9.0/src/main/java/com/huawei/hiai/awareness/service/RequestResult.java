package com.huawei.hiai.awareness.service;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestResult implements Parcelable {
    public static final Parcelable.Creator<RequestResult> CREATOR = new Parcelable.Creator<RequestResult>() {
        public RequestResult createFromParcel(Parcel in) {
            return new RequestResult(in);
        }

        public RequestResult[] newArray(int size) {
            return new RequestResult[size];
        }
    };
    private int action = -1;
    private int confidence = 100;
    private String content = null;
    private int errorCode = -1;
    private String errorResult = null;
    private String registerTopKey = null;
    private int resultType = -1;
    private String secondAction = null;
    private long sensorTime = 0;
    private int status = -1;
    private long time = 0;
    private int triggerStatus = -1;
    private int type = -1;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.status);
        dest.writeInt(this.action);
        dest.writeString(this.secondAction);
        dest.writeLong(this.time);
        dest.writeLong(this.sensorTime);
        dest.writeInt(this.confidence);
        dest.writeString(this.registerTopKey);
        dest.writeInt(this.resultType);
        dest.writeString(this.content);
        dest.writeInt(this.triggerStatus);
        dest.writeInt(this.errorCode);
        dest.writeString(this.errorResult);
    }

    public void readFromParcel(Parcel in) {
        this.type = in.readInt();
        this.status = in.readInt();
        this.action = in.readInt();
        this.secondAction = in.readString();
        this.time = in.readLong();
        this.sensorTime = in.readLong();
        this.confidence = in.readInt();
        this.registerTopKey = in.readString();
        this.resultType = in.readInt();
        this.content = in.readString();
        this.triggerStatus = in.readInt();
        this.errorCode = in.readInt();
        this.errorResult = in.readString();
    }

    public RequestResult() {
    }

    public RequestResult(int type2, int status2, int action2, String secondAction2, long time2, long sensorTime2, int confidence2) {
        this.type = type2;
        this.status = status2;
        this.action = action2;
        this.secondAction = secondAction2;
        this.time = time2;
        this.sensorTime = sensorTime2;
        this.confidence = confidence2;
    }

    public RequestResult(int errorCode2, String errorResult2) {
        this.errorCode = errorCode2;
        this.errorResult = errorResult2;
    }

    public RequestResult(Parcel in) {
        this.type = in.readInt();
        this.status = in.readInt();
        this.action = in.readInt();
        this.secondAction = in.readString();
        this.time = in.readLong();
        this.sensorTime = in.readLong();
        this.confidence = in.readInt();
        this.registerTopKey = in.readString();
        this.resultType = in.readInt();
        this.content = in.readString();
        this.triggerStatus = in.readInt();
        this.errorCode = in.readInt();
        this.errorResult = in.readString();
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status2) {
        this.status = status2;
    }

    public int getAction() {
        return this.action;
    }

    public void setAction(int action2) {
        this.action = action2;
    }

    public String getSecondAction() {
        return this.secondAction;
    }

    public void setSecondAction(String secondAction2) {
        this.secondAction = secondAction2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public long getSensorTime() {
        return this.sensorTime;
    }

    public void setSensorTime(long sensorTime2) {
        this.sensorTime = sensorTime2;
    }

    public int getConfidence() {
        return this.confidence;
    }

    public void setConfidence(int confidence2) {
        this.confidence = confidence2;
    }

    public String getRegisterTopKey() {
        return this.registerTopKey;
    }

    public void setRegisterTopKey(String registerTopKey2) {
        this.registerTopKey = registerTopKey2;
    }

    public int getResultType() {
        return this.resultType;
    }

    public void setResultType(int resultType2) {
        this.resultType = resultType2;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content2) {
        this.content = content2;
    }

    public int getTriggerStatus() {
        return this.triggerStatus;
    }

    public void setTriggerStatus(int triggerStatus2) {
        this.triggerStatus = triggerStatus2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public String getErrorResult() {
        return this.errorResult;
    }

    public void setErrorResult(String errorResult2) {
        this.errorResult = errorResult2;
    }

    public String toString() {
        return String.format("RequestResult{type = %d, status = %d, action = %d, secondAction = %s, time = %d, sensorTime = %d, confidence = %d, registerTopKey = %s, resultType = %d, content = %s, triggerStatus = %d, errorCode = %d, errorResult = %s}", new Object[]{Integer.valueOf(this.type), Integer.valueOf(this.status), Integer.valueOf(this.action), this.secondAction, Long.valueOf(this.time), Long.valueOf(this.sensorTime), Integer.valueOf(this.confidence), this.registerTopKey, Integer.valueOf(this.resultType), this.content, Integer.valueOf(this.triggerStatus), Integer.valueOf(this.errorCode), this.errorResult});
    }
}
