package android.media;

import android.media.AudioAttributes.Builder;
import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class AudioFocusInfo implements Parcelable {
    public static final Creator<AudioFocusInfo> CREATOR = new Creator<AudioFocusInfo>() {
        public AudioFocusInfo createFromParcel(Parcel in) {
            return new AudioFocusInfo((AudioAttributes) AudioAttributes.CREATOR.createFromParcel(in), in.readInt(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
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
    private int mLossReceived;
    private final String mPackageName;
    private final int mSdkTarget;

    public AudioFocusInfo(AudioAttributes aa, int clientUid, String clientId, String packageName, int gainRequest, int lossReceived, int flags, int sdk) {
        if (aa == null) {
            aa = new Builder().build();
        }
        this.mAttributes = aa;
        this.mClientUid = clientUid;
        if (clientId == null) {
            clientId = ProxyInfo.LOCAL_EXCL_LIST;
        }
        this.mClientId = clientId;
        if (packageName == null) {
            packageName = ProxyInfo.LOCAL_EXCL_LIST;
        }
        this.mPackageName = packageName;
        this.mGainRequest = gainRequest;
        this.mLossReceived = lossReceived;
        this.mFlags = flags;
        this.mSdkTarget = sdk;
    }

    public AudioAttributes getAttributes() {
        return this.mAttributes;
    }

    public int getClientUid() {
        return this.mClientUid;
    }

    public String getClientId() {
        return this.mClientId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getGainRequest() {
        return this.mGainRequest;
    }

    public int getLossReceived() {
        return this.mLossReceived;
    }

    public int getSdkTarget() {
        return this.mSdkTarget;
    }

    public void clearLossReceived() {
        this.mLossReceived = 0;
    }

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
        return this.mAttributes.equals(other.mAttributes) && this.mClientUid == other.mClientUid && this.mClientId.equals(other.mClientId) && this.mPackageName.equals(other.mPackageName) && this.mGainRequest == other.mGainRequest && this.mLossReceived == other.mLossReceived && this.mFlags == other.mFlags && this.mSdkTarget == other.mSdkTarget;
    }
}
