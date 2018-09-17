package android.net.wifi.hotspot2.pps;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public final class HomeSp implements Parcelable {
    public static final Creator<HomeSp> CREATOR = new Creator<HomeSp>() {
        public HomeSp createFromParcel(Parcel in) {
            HomeSp homeSp = new HomeSp();
            homeSp.setFqdn(in.readString());
            homeSp.setFriendlyName(in.readString());
            homeSp.setIconUrl(in.readString());
            homeSp.setHomeNetworkIds(readHomeNetworkIds(in));
            homeSp.setMatchAllOis(in.createLongArray());
            homeSp.setMatchAnyOis(in.createLongArray());
            homeSp.setOtherHomePartners(in.createStringArray());
            homeSp.setRoamingConsortiumOis(in.createLongArray());
            return homeSp;
        }

        public HomeSp[] newArray(int size) {
            return new HomeSp[size];
        }

        private Map<String, Long> readHomeNetworkIds(Parcel in) {
            int size = in.readInt();
            if (size == -1) {
                return null;
            }
            Map<String, Long> networkIds = new HashMap(size);
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                Long value = null;
                long readValue = in.readLong();
                if (readValue != -1) {
                    value = Long.valueOf(readValue);
                }
                networkIds.put(key, value);
            }
            return networkIds;
        }
    };
    private static final int MAX_SSID_BYTES = 32;
    private static final int NULL_VALUE = -1;
    private static final String TAG = "HomeSp";
    private String mFqdn = null;
    private String mFriendlyName = null;
    private Map<String, Long> mHomeNetworkIds = null;
    private String mIconUrl = null;
    private long[] mMatchAllOis = null;
    private long[] mMatchAnyOis = null;
    private String[] mOtherHomePartners = null;
    private long[] mRoamingConsortiumOis = null;

    public void setFqdn(String fqdn) {
        this.mFqdn = fqdn;
    }

    public String getFqdn() {
        return this.mFqdn;
    }

    public void setFriendlyName(String friendlyName) {
        this.mFriendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return this.mFriendlyName;
    }

    public void setIconUrl(String iconUrl) {
        this.mIconUrl = iconUrl;
    }

    public String getIconUrl() {
        return this.mIconUrl;
    }

    public void setHomeNetworkIds(Map<String, Long> homeNetworkIds) {
        this.mHomeNetworkIds = homeNetworkIds;
    }

    public Map<String, Long> getHomeNetworkIds() {
        return this.mHomeNetworkIds;
    }

    public void setMatchAllOis(long[] matchAllOis) {
        this.mMatchAllOis = matchAllOis;
    }

    public long[] getMatchAllOis() {
        return this.mMatchAllOis;
    }

    public void setMatchAnyOis(long[] matchAnyOis) {
        this.mMatchAnyOis = matchAnyOis;
    }

    public long[] getMatchAnyOis() {
        return this.mMatchAnyOis;
    }

    public void setOtherHomePartners(String[] otherHomePartners) {
        this.mOtherHomePartners = otherHomePartners;
    }

    public String[] getOtherHomePartners() {
        return this.mOtherHomePartners;
    }

    public void setRoamingConsortiumOis(long[] roamingConsortiumOis) {
        this.mRoamingConsortiumOis = roamingConsortiumOis;
    }

    public long[] getRoamingConsortiumOis() {
        return this.mRoamingConsortiumOis;
    }

    public HomeSp(HomeSp source) {
        if (source != null) {
            this.mFqdn = source.mFqdn;
            this.mFriendlyName = source.mFriendlyName;
            this.mIconUrl = source.mIconUrl;
            if (source.mHomeNetworkIds != null) {
                this.mHomeNetworkIds = Collections.unmodifiableMap(source.mHomeNetworkIds);
            }
            if (source.mMatchAllOis != null) {
                this.mMatchAllOis = Arrays.copyOf(source.mMatchAllOis, source.mMatchAllOis.length);
            }
            if (source.mMatchAnyOis != null) {
                this.mMatchAnyOis = Arrays.copyOf(source.mMatchAnyOis, source.mMatchAnyOis.length);
            }
            if (source.mOtherHomePartners != null) {
                this.mOtherHomePartners = (String[]) Arrays.copyOf(source.mOtherHomePartners, source.mOtherHomePartners.length);
            }
            if (source.mRoamingConsortiumOis != null) {
                this.mRoamingConsortiumOis = Arrays.copyOf(source.mRoamingConsortiumOis, source.mRoamingConsortiumOis.length);
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFqdn);
        dest.writeString(this.mFriendlyName);
        dest.writeString(this.mIconUrl);
        writeHomeNetworkIds(dest, this.mHomeNetworkIds);
        dest.writeLongArray(this.mMatchAllOis);
        dest.writeLongArray(this.mMatchAnyOis);
        dest.writeStringArray(this.mOtherHomePartners);
        dest.writeLongArray(this.mRoamingConsortiumOis);
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof HomeSp)) {
            return false;
        }
        HomeSp that = (HomeSp) thatObject;
        if (TextUtils.equals(this.mFqdn, that.mFqdn) && TextUtils.equals(this.mFriendlyName, that.mFriendlyName) && TextUtils.equals(this.mIconUrl, that.mIconUrl) && (this.mHomeNetworkIds != null ? !this.mHomeNetworkIds.equals(that.mHomeNetworkIds) : that.mHomeNetworkIds != null) && Arrays.equals(this.mMatchAllOis, that.mMatchAllOis) && Arrays.equals(this.mMatchAnyOis, that.mMatchAnyOis) && Arrays.equals(this.mOtherHomePartners, that.mOtherHomePartners)) {
            z = Arrays.equals(this.mRoamingConsortiumOis, that.mRoamingConsortiumOis);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mFqdn, this.mFriendlyName, this.mIconUrl, this.mHomeNetworkIds, this.mMatchAllOis, this.mMatchAnyOis, this.mOtherHomePartners, this.mRoamingConsortiumOis});
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FQDN: ").append(this.mFqdn).append("\n");
        builder.append("FriendlyName: ").append(this.mFriendlyName).append("\n");
        builder.append("IconURL: ").append(this.mIconUrl).append("\n");
        builder.append("HomeNetworkIDs: ").append(this.mHomeNetworkIds).append("\n");
        builder.append("MatchAllOIs: ").append(this.mMatchAllOis).append("\n");
        builder.append("MatchAnyOIs: ").append(this.mMatchAnyOis).append("\n");
        builder.append("OtherHomePartners: ").append(this.mOtherHomePartners).append("\n");
        builder.append("RoamingConsortiumOIs: ").append(this.mRoamingConsortiumOis).append("\n");
        return builder.toString();
    }

    public boolean validate() {
        if (TextUtils.isEmpty(this.mFqdn)) {
            Log.d(TAG, "Missing FQDN");
            return false;
        } else if (TextUtils.isEmpty(this.mFriendlyName)) {
            Log.d(TAG, "Missing friendly name");
            return false;
        } else {
            if (this.mHomeNetworkIds != null) {
                for (Entry<String, Long> entry : this.mHomeNetworkIds.entrySet()) {
                    if (entry.getKey() != null) {
                        if (((String) entry.getKey()).getBytes(StandardCharsets.UTF_8).length > 32) {
                        }
                    }
                    Log.d(TAG, "Invalid SSID in HomeNetworkIDs");
                    return false;
                }
            }
            return true;
        }
    }

    private static void writeHomeNetworkIds(Parcel dest, Map<String, Long> networkIds) {
        if (networkIds == null) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(networkIds.size());
        for (Entry<String, Long> entry : networkIds.entrySet()) {
            dest.writeString((String) entry.getKey());
            if (entry.getValue() == null) {
                dest.writeLong(-1);
            } else {
                dest.writeLong(((Long) entry.getValue()).longValue());
            }
        }
    }
}
