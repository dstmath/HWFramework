package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationInfo implements Parcelable {
    public static final Parcelable.Creator<NotificationInfo> CREATOR = new Parcelable.Creator<NotificationInfo>() {
        /* class com.huawei.airsharing.api.NotificationInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NotificationInfo[] newArray(int size) {
            return new NotificationInfo[size];
        }

        @Override // android.os.Parcelable.Creator
        public NotificationInfo createFromParcel(Parcel source) {
            NotificationInfo castResponseInfo = new NotificationInfo();
            castResponseInfo.setPlayerState(source.readInt());
            castResponseInfo.setDuration(source.readInt());
            castResponseInfo.setMinSystemVolume(source.readInt());
            castResponseInfo.setMaxSystemVolume(source.readInt());
            castResponseInfo.setCurrentSystemVolume(source.readInt());
            castResponseInfo.setErrorCode(source.readInt());
            castResponseInfo.setErrorDescription(source.readString());
            castResponseInfo.setIndexInList(source.readInt());
            castResponseInfo.setCurrPosition(source.readInt());
            return castResponseInfo;
        }
    };
    public static final int ERROR_CODE_CHINA_DRM_TYPE_UNSUPPORTED = 1805;
    public static final int ERROR_CODE_CLEARKEY_DRM_TYPE_UNSUPPORTED = 1804;
    public static final int ERROR_CODE_DRM_ERROR = 1800;
    public static final int ERROR_CODE_EXECUTION_FAILED = 1600;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER = 1300;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER_CAST_MEDIA_TYPE = 1301;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER_FIVE = 1307;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER_FOUR = 1306;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER_ONE = 1303;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER_PLAYER_NAME = 1302;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER_THREE = 1305;
    public static final int ERROR_CODE_ILLEGAL_PARAMETER_TWO = 1304;
    public static final int ERROR_CODE_MEDIA_SOURCE = 1700;
    public static final int ERROR_CODE_MIME_UNSUPPORTED = 1701;
    public static final int ERROR_CODE_NETWORK_ERROR = 1400;
    public static final int ERROR_CODE_OTHERS = 1900;
    public static final int ERROR_CODE_OTHER_DRM_TYPE_UNSUPPORTED = 1801;
    public static final int ERROR_CODE_OUT_OF_MEMORY = 1902;
    public static final int ERROR_CODE_PARSE_CMD_FAILED = 202;
    public static final int ERROR_CODE_PLAYER_ERROR = 1500;
    public static final int ERROR_CODE_PLAYER_ILLEGAL_STATE = 1601;
    public static final int ERROR_CODE_PLAYER_NOT_FIND = 1101;
    public static final int ERROR_CODE_PLAYREADY_DRM_TYPE_UNSUPPORTED = 1802;
    public static final int ERROR_CODE_PROJECTION_PREEMPTED = 201;
    public static final int ERROR_CODE_RUNTIME = 1901;
    public static final int ERROR_CODE_SDK_ERROR = 1100;
    public static final int ERROR_CODE_UNSUPPORTED_FUNC = 1200;
    public static final int ERROR_CODE_WIDEVINE_DRM_TYPE_UNSUPPORTED = 1803;
    public static final String ERROR_DESC_PROJECTION_PREEMPTED = "projection server has been preempted.";
    private int mCurrPosition = 0;
    private int mCurrentSystemVolume = 0;
    private int mDuration = 0;
    private int mErrorCode = 0;
    private String mErrorDescription = null;
    private int mIndexInList = 0;
    private int mMaxSystemVolume = 0;
    private int mMinSystemVolume = 0;
    private int mPlayerState = -1;

    public int getCurrPosition() {
        return this.mCurrPosition;
    }

    public void setCurrPosition(int currPosition) {
        this.mCurrPosition = currPosition;
    }

    public int getIndexInList() {
        return this.mIndexInList;
    }

    public void setIndexInList(int indexInList) {
        this.mIndexInList = indexInList;
    }

    public String getErrorDescription() {
        return this.mErrorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.mErrorDescription = errorDescription;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public void setErrorCode(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public int getCurrentSystemVolume() {
        return this.mCurrentSystemVolume;
    }

    public void setCurrentSystemVolume(int currentSystemVolume) {
        this.mCurrentSystemVolume = currentSystemVolume;
    }

    public int getMaxSystemVolume() {
        return this.mMaxSystemVolume;
    }

    public void setMaxSystemVolume(int maxSystemVolume) {
        this.mMaxSystemVolume = maxSystemVolume;
    }

    public int getMinSystemVolume() {
        return this.mMinSystemVolume;
    }

    public void setMinSystemVolume(int minSystemVolume) {
        this.mMinSystemVolume = minSystemVolume;
    }

    public int getPlayerState() {
        return this.mPlayerState;
    }

    public void setPlayerState(int playState) {
        this.mPlayerState = playState;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPlayerState);
        dest.writeInt(this.mDuration);
        dest.writeInt(this.mMinSystemVolume);
        dest.writeInt(this.mMaxSystemVolume);
        dest.writeInt(this.mCurrentSystemVolume);
        dest.writeInt(this.mErrorCode);
        dest.writeString(this.mErrorDescription);
        dest.writeInt(this.mIndexInList);
        dest.writeInt(this.mCurrPosition);
    }
}
