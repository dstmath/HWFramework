package com.huawei.hiai.awareness.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.huawei.hiai.awareness.AwarenessConstants;
import java.util.Locale;

public class RequestResult implements Parcelable {
    public static final Parcelable.Creator<RequestResult> CREATOR = new Parcelable.Creator<RequestResult>() {
        /* class com.huawei.hiai.awareness.service.RequestResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RequestResult createFromParcel(Parcel in) {
            return new RequestResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public RequestResult[] newArray(int size) {
            return new RequestResult[size];
        }
    };
    private int mAction = -1;
    private int mConfidence = 100;
    private String mContent = null;
    private int mErrorCode = -1;
    private String mErrorResult = null;
    private String mRegisterTopKey = null;
    private int mResultType = -1;
    private String mSecondAction = null;
    private long mSensorTime = 0;
    private int mStatus = -1;
    private long mTime = 0;
    private int mTriggerStatus = -1;
    private int mType = 0;

    public RequestResult() {
    }

    public RequestResult(int type, int status, int action, String secondAction) {
        this.mType = type;
        this.mStatus = status;
        this.mAction = action;
        this.mSecondAction = secondAction;
    }

    public RequestResult(int errorCode, String errorResult) {
        this.mErrorCode = errorCode;
        this.mErrorResult = errorResult;
    }

    public RequestResult(Parcel in) {
        this.mType = in.readInt();
        this.mStatus = in.readInt();
        this.mAction = in.readInt();
        this.mSecondAction = in.readString();
        this.mTime = in.readLong();
        this.mSensorTime = in.readLong();
        this.mConfidence = in.readInt();
        this.mRegisterTopKey = in.readString();
        this.mResultType = in.readInt();
        this.mContent = in.readString();
        this.mTriggerStatus = in.readInt();
        this.mErrorCode = in.readInt();
        this.mErrorResult = in.readString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeInt(this.mStatus);
        dest.writeInt(this.mAction);
        dest.writeString(this.mSecondAction);
        dest.writeLong(this.mTime);
        dest.writeLong(this.mSensorTime);
        dest.writeInt(this.mConfidence);
        dest.writeString(this.mRegisterTopKey);
        dest.writeInt(this.mResultType);
        dest.writeString(this.mContent);
        dest.writeInt(this.mTriggerStatus);
        dest.writeInt(this.mErrorCode);
        dest.writeString(this.mErrorResult);
    }

    public void readFromParcel(Parcel in) {
        this.mType = in.readInt();
        this.mStatus = in.readInt();
        this.mAction = in.readInt();
        this.mSecondAction = in.readString();
        this.mTime = in.readLong();
        this.mSensorTime = in.readLong();
        this.mConfidence = in.readInt();
        this.mRegisterTopKey = in.readString();
        this.mResultType = in.readInt();
        this.mContent = in.readString();
        this.mTriggerStatus = in.readInt();
        this.mErrorCode = in.readInt();
        this.mErrorResult = in.readString();
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getAction() {
        return this.mAction;
    }

    public void setAction(int action) {
        this.mAction = action;
    }

    public String getSecondAction() {
        return this.mSecondAction;
    }

    public void setSecondAction(String secondAction) {
        this.mSecondAction = secondAction;
    }

    public long getTime() {
        return this.mTime;
    }

    public void setTime(long time) {
        this.mTime = time;
    }

    public long getSensorTime() {
        return this.mSensorTime;
    }

    public void setSensorTime(long sensorTime) {
        this.mSensorTime = sensorTime;
    }

    public int getConfidence() {
        return this.mConfidence;
    }

    public void setConfidence(int confidence) {
        this.mConfidence = confidence;
    }

    public String getRegisterTopKey() {
        return this.mRegisterTopKey;
    }

    public void setRegisterTopKey(String registerTopKey) {
        this.mRegisterTopKey = registerTopKey;
    }

    public int getResultType() {
        return this.mResultType;
    }

    public void setResultType(int resultType) {
        this.mResultType = resultType;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public int getTriggerStatus() {
        return this.mTriggerStatus;
    }

    public void setTriggerStatus(int triggerStatus) {
        this.mTriggerStatus = triggerStatus;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public void setErrorCode(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public String getErrorResult() {
        return this.mErrorResult;
    }

    public void setErrorResult(String errorResult) {
        this.mErrorResult = errorResult;
    }

    @Override // java.lang.Object
    public String toString() {
        String tempTopKey = this.mRegisterTopKey;
        if (this.mType == 11 && !TextUtils.isEmpty(this.mRegisterTopKey)) {
            tempTopKey = this.mRegisterTopKey.split(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)[0];
        }
        return String.format(Locale.ENGLISH, "RequestResult{%d, %d, %d, %s, %d, %d, %d, %s, %d, %s, %d, %d, %s}", Integer.valueOf(this.mType), Integer.valueOf(this.mStatus), Integer.valueOf(this.mAction), this.mSecondAction, Long.valueOf(this.mTime), Long.valueOf(this.mSensorTime), Integer.valueOf(this.mConfidence), tempTopKey, Integer.valueOf(this.mResultType), this.mContent, Integer.valueOf(this.mTriggerStatus), Integer.valueOf(this.mErrorCode), this.mErrorResult);
    }
}
