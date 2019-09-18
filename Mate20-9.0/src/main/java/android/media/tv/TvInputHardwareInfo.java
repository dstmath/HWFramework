package android.media.tv;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class TvInputHardwareInfo implements Parcelable {
    public static final int CABLE_CONNECTION_STATUS_CONNECTED = 1;
    public static final int CABLE_CONNECTION_STATUS_DISCONNECTED = 2;
    public static final int CABLE_CONNECTION_STATUS_UNKNOWN = 0;
    public static final Parcelable.Creator<TvInputHardwareInfo> CREATOR = new Parcelable.Creator<TvInputHardwareInfo>() {
        public TvInputHardwareInfo createFromParcel(Parcel source) {
            try {
                TvInputHardwareInfo info = new TvInputHardwareInfo();
                info.readFromParcel(source);
                return info;
            } catch (Exception e) {
                Log.e(TvInputHardwareInfo.TAG, "Exception creating TvInputHardwareInfo from parcel", e);
                return null;
            }
        }

        public TvInputHardwareInfo[] newArray(int size) {
            return new TvInputHardwareInfo[size];
        }
    };
    static final String TAG = "TvInputHardwareInfo";
    public static final int TV_INPUT_TYPE_COMPONENT = 6;
    public static final int TV_INPUT_TYPE_COMPOSITE = 3;
    public static final int TV_INPUT_TYPE_DISPLAY_PORT = 10;
    public static final int TV_INPUT_TYPE_DVI = 8;
    public static final int TV_INPUT_TYPE_HDMI = 9;
    public static final int TV_INPUT_TYPE_OTHER_HARDWARE = 1;
    public static final int TV_INPUT_TYPE_SCART = 5;
    public static final int TV_INPUT_TYPE_SVIDEO = 4;
    public static final int TV_INPUT_TYPE_TUNER = 2;
    public static final int TV_INPUT_TYPE_VGA = 7;
    /* access modifiers changed from: private */
    public String mAudioAddress;
    /* access modifiers changed from: private */
    public int mAudioType;
    /* access modifiers changed from: private */
    public int mCableConnectionStatus;
    /* access modifiers changed from: private */
    public int mDeviceId;
    /* access modifiers changed from: private */
    public int mHdmiPortId;
    /* access modifiers changed from: private */
    public int mType;

    public static final class Builder {
        private String mAudioAddress = "";
        private int mAudioType = 0;
        private Integer mCableConnectionStatus = 0;
        private Integer mDeviceId = null;
        private Integer mHdmiPortId = null;
        private Integer mType = null;

        public Builder deviceId(int deviceId) {
            this.mDeviceId = Integer.valueOf(deviceId);
            return this;
        }

        public Builder type(int type) {
            this.mType = Integer.valueOf(type);
            return this;
        }

        public Builder audioType(int audioType) {
            this.mAudioType = audioType;
            return this;
        }

        public Builder audioAddress(String audioAddress) {
            this.mAudioAddress = audioAddress;
            return this;
        }

        public Builder hdmiPortId(int hdmiPortId) {
            this.mHdmiPortId = Integer.valueOf(hdmiPortId);
            return this;
        }

        public Builder cableConnectionStatus(int cableConnectionStatus) {
            this.mCableConnectionStatus = Integer.valueOf(cableConnectionStatus);
            return this;
        }

        public TvInputHardwareInfo build() {
            if (this.mDeviceId == null || this.mType == null) {
                throw new UnsupportedOperationException();
            } else if (!(this.mType.intValue() == 9 && this.mHdmiPortId == null) && (this.mType.intValue() == 9 || this.mHdmiPortId == null)) {
                TvInputHardwareInfo info = new TvInputHardwareInfo();
                int unused = info.mDeviceId = this.mDeviceId.intValue();
                int unused2 = info.mType = this.mType.intValue();
                int unused3 = info.mAudioType = this.mAudioType;
                if (info.mAudioType != 0) {
                    String unused4 = info.mAudioAddress = this.mAudioAddress;
                }
                if (this.mHdmiPortId != null) {
                    int unused5 = info.mHdmiPortId = this.mHdmiPortId.intValue();
                }
                int unused6 = info.mCableConnectionStatus = this.mCableConnectionStatus.intValue();
                return info;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CableConnectionStatus {
    }

    private TvInputHardwareInfo() {
    }

    public int getDeviceId() {
        return this.mDeviceId;
    }

    public int getType() {
        return this.mType;
    }

    public int getAudioType() {
        return this.mAudioType;
    }

    public String getAudioAddress() {
        return this.mAudioAddress;
    }

    public int getHdmiPortId() {
        if (this.mType == 9) {
            return this.mHdmiPortId;
        }
        throw new IllegalStateException();
    }

    public int getCableConnectionStatus() {
        return this.mCableConnectionStatus;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(128);
        b.append("TvInputHardwareInfo {id=");
        b.append(this.mDeviceId);
        b.append(", type=");
        b.append(this.mType);
        b.append(", audio_type=");
        b.append(this.mAudioType);
        b.append(", audio_addr=");
        b.append(this.mAudioAddress);
        if (this.mType == 9) {
            b.append(", hdmi_port=");
            b.append(this.mHdmiPortId);
        }
        b.append(", cable_connection_status=");
        b.append(this.mCableConnectionStatus);
        b.append("}");
        return b.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDeviceId);
        dest.writeInt(this.mType);
        dest.writeInt(this.mAudioType);
        dest.writeString(this.mAudioAddress);
        if (this.mType == 9) {
            dest.writeInt(this.mHdmiPortId);
        }
        dest.writeInt(this.mCableConnectionStatus);
    }

    public void readFromParcel(Parcel source) {
        this.mDeviceId = source.readInt();
        this.mType = source.readInt();
        this.mAudioType = source.readInt();
        this.mAudioAddress = source.readString();
        if (this.mType == 9) {
            this.mHdmiPortId = source.readInt();
        }
        this.mCableConnectionStatus = source.readInt();
    }
}
