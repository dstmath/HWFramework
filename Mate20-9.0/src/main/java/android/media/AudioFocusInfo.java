package android.media;

import android.annotation.SystemApi;
import android.media.AudioAttributes;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

@SystemApi
public final class AudioFocusInfo implements Parcelable {
    public static final Parcelable.Creator<AudioFocusInfo> CREATOR = new Parcelable.Creator<AudioFocusInfo>() {
        public AudioFocusInfo createFromParcel(Parcel in) {
            AudioFocusInfo afi = new AudioFocusInfo(AudioAttributes.CREATOR.createFromParcel(in), in.readInt(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
            afi.setGen(in.readLong());
            return afi;
        }

        public AudioFocusInfo[] newArray(int size) {
            return new AudioFocusInfo[size];
        }
    };
    private final AudioAttributes mAttributes;
    private final String mClientId;
    private final int mClientUid;
    private int mFlags;
    private int mGainRequest;
    private long mGenCount = -1;
    private int mLossReceived;
    private final String mPackageName;
    private final int mSdkTarget;

    public AudioFocusInfo(AudioAttributes aa, int clientUid, String clientId, String packageName, int gainRequest, int lossReceived, int flags, int sdk) {
        this.mAttributes = aa == null ? new AudioAttributes.Builder().build() : aa;
        this.mClientUid = clientUid;
        this.mClientId = clientId == null ? "" : clientId;
        this.mPackageName = packageName == null ? "" : packageName;
        this.mGainRequest = gainRequest;
        this.mLossReceived = lossReceived;
        this.mFlags = flags;
        this.mSdkTarget = sdk;
    }

    public void setGen(long g) {
        this.mGenCount = g;
    }

    public long getGen() {
        return this.mGenCount;
    }

    @SystemApi
    public AudioAttributes getAttributes() {
        return this.mAttributes;
    }

    @SystemApi
    public int getClientUid() {
        return this.mClientUid;
    }

    @SystemApi
    public String getClientId() {
        return this.mClientId;
    }

    @SystemApi
    public String getPackageName() {
        return this.mPackageName;
    }

    @SystemApi
    public int getGainRequest() {
        return this.mGainRequest;
    }

    @SystemApi
    public int getLossReceived() {
        return this.mLossReceived;
    }

    public int getSdkTarget() {
        return this.mSdkTarget;
    }

    public void clearLossReceived() {
        this.mLossReceived = 0;
    }

    @SystemApi
    public int getFlags() {
        return this.mFlags;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mAttributes.writeToParcel(dest, flags);
        dest.writeInt(this.mClientUid);
        dest.writeString(this.mClientId);
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mGainRequest);
        dest.writeInt(this.mLossReceived);
        dest.writeInt(this.mFlags);
        dest.writeInt(this.mSdkTarget);
        dest.writeLong(this.mGenCount);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mAttributes, Integer.valueOf(this.mClientUid), this.mClientId, this.mPackageName, Integer.valueOf(this.mGainRequest), Integer.valueOf(this.mFlags)});
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AudioFocusInfo other = (AudioFocusInfo) obj;
        if (this.mAttributes.equals(other.mAttributes) && this.mClientUid == other.mClientUid && this.mClientId.equals(other.mClientId) && this.mPackageName.equals(other.mPackageName) && this.mGainRequest == other.mGainRequest && this.mLossReceived == other.mLossReceived && this.mFlags == other.mFlags && this.mSdkTarget == other.mSdkTarget) {
            return true;
        }
        return false;
    }
}
