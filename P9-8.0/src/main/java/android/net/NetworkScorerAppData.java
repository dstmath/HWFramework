package android.net;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class NetworkScorerAppData implements Parcelable {
    public static final Creator<NetworkScorerAppData> CREATOR = new Creator<NetworkScorerAppData>() {
        public NetworkScorerAppData createFromParcel(Parcel in) {
            return new NetworkScorerAppData(in);
        }

        public NetworkScorerAppData[] newArray(int size) {
            return new NetworkScorerAppData[size];
        }
    };
    private final ComponentName mEnableUseOpenWifiActivity;
    private final String mNetworkAvailableNotificationChannelId;
    private final ComponentName mRecommendationService;
    private final String mRecommendationServiceLabel;
    public final int packageUid;

    public NetworkScorerAppData(int packageUid, ComponentName recommendationServiceComp, String recommendationServiceLabel, ComponentName enableUseOpenWifiActivity, String networkAvailableNotificationChannelId) {
        this.packageUid = packageUid;
        this.mRecommendationService = recommendationServiceComp;
        this.mRecommendationServiceLabel = recommendationServiceLabel;
        this.mEnableUseOpenWifiActivity = enableUseOpenWifiActivity;
        this.mNetworkAvailableNotificationChannelId = networkAvailableNotificationChannelId;
    }

    protected NetworkScorerAppData(Parcel in) {
        this.packageUid = in.readInt();
        this.mRecommendationService = ComponentName.readFromParcel(in);
        this.mRecommendationServiceLabel = in.readString();
        this.mEnableUseOpenWifiActivity = ComponentName.readFromParcel(in);
        this.mNetworkAvailableNotificationChannelId = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.packageUid);
        ComponentName.writeToParcel(this.mRecommendationService, dest);
        dest.writeString(this.mRecommendationServiceLabel);
        ComponentName.writeToParcel(this.mEnableUseOpenWifiActivity, dest);
        dest.writeString(this.mNetworkAvailableNotificationChannelId);
    }

    public int describeContents() {
        return 0;
    }

    public String getRecommendationServicePackageName() {
        return this.mRecommendationService.getPackageName();
    }

    public ComponentName getRecommendationServiceComponent() {
        return this.mRecommendationService;
    }

    public ComponentName getEnableUseOpenWifiActivity() {
        return this.mEnableUseOpenWifiActivity;
    }

    public String getRecommendationServiceLabel() {
        return this.mRecommendationServiceLabel;
    }

    public String getNetworkAvailableNotificationChannelId() {
        return this.mNetworkAvailableNotificationChannelId;
    }

    public String toString() {
        return "NetworkScorerAppData{packageUid=" + this.packageUid + ", mRecommendationService=" + this.mRecommendationService + ", mRecommendationServiceLabel=" + this.mRecommendationServiceLabel + ", mEnableUseOpenWifiActivity=" + this.mEnableUseOpenWifiActivity + ", mNetworkAvailableNotificationChannelId=" + this.mNetworkAvailableNotificationChannelId + '}';
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkScorerAppData that = (NetworkScorerAppData) o;
        if (this.packageUid == that.packageUid && Objects.equals(this.mRecommendationService, that.mRecommendationService) && Objects.equals(this.mRecommendationServiceLabel, that.mRecommendationServiceLabel) && Objects.equals(this.mEnableUseOpenWifiActivity, that.mEnableUseOpenWifiActivity)) {
            z = Objects.equals(this.mNetworkAvailableNotificationChannelId, that.mNetworkAvailableNotificationChannelId);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.packageUid), this.mRecommendationService, this.mRecommendationServiceLabel, this.mEnableUseOpenWifiActivity, this.mNetworkAvailableNotificationChannelId});
    }
}
