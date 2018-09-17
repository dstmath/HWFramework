package android.content.pm;

import android.content.IntentFilter;
import android.content.pm.InstantAppResolveInfo.InstantAppDigest;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public final class EphemeralResolveInfo implements Parcelable {
    public static final Creator<EphemeralResolveInfo> CREATOR = new Creator<EphemeralResolveInfo>() {
        public EphemeralResolveInfo createFromParcel(Parcel in) {
            return new EphemeralResolveInfo(in);
        }

        public EphemeralResolveInfo[] newArray(int size) {
            return new EphemeralResolveInfo[size];
        }
    };
    public static final String SHA_ALGORITHM = "SHA-256";
    private final InstantAppResolveInfo mInstantAppResolveInfo;
    @Deprecated
    private final List<IntentFilter> mLegacyFilters;

    public static final class EphemeralDigest implements Parcelable {
        public static final Creator<EphemeralDigest> CREATOR = new Creator<EphemeralDigest>() {
            public EphemeralDigest createFromParcel(Parcel in) {
                return new EphemeralDigest(in);
            }

            public EphemeralDigest[] newArray(int size) {
                return new EphemeralDigest[size];
            }
        };
        private final InstantAppDigest mInstantAppDigest;

        public EphemeralDigest(String hostName) {
            this(hostName, -1);
        }

        public EphemeralDigest(String hostName, int maxDigests) {
            this.mInstantAppDigest = new InstantAppDigest(hostName, maxDigests);
        }

        EphemeralDigest(Parcel in) {
            this.mInstantAppDigest = (InstantAppDigest) in.readParcelable(null);
        }

        InstantAppDigest getInstantAppDigest() {
            return this.mInstantAppDigest;
        }

        public byte[][] getDigestBytes() {
            return this.mInstantAppDigest.getDigestBytes();
        }

        public int[] getDigestPrefix() {
            return this.mInstantAppDigest.getDigestPrefix();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(this.mInstantAppDigest, flags);
        }
    }

    @Deprecated
    public EphemeralResolveInfo(Uri uri, String packageName, List<IntentFilter> filters) {
        if (uri == null || packageName == null || filters == null || filters.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<EphemeralIntentFilter> ephemeralFilters = new ArrayList(1);
        ephemeralFilters.add(new EphemeralIntentFilter(packageName, filters));
        this.mInstantAppResolveInfo = new InstantAppResolveInfo(uri.getHost(), packageName, createInstantAppIntentFilterList(ephemeralFilters));
        this.mLegacyFilters = new ArrayList(filters.size());
        this.mLegacyFilters.addAll(filters);
    }

    @Deprecated
    public EphemeralResolveInfo(EphemeralDigest digest, String packageName, List<EphemeralIntentFilter> filters) {
        this(digest, packageName, filters, -1);
    }

    public EphemeralResolveInfo(EphemeralDigest digest, String packageName, List<EphemeralIntentFilter> filters, int versionCode) {
        this.mInstantAppResolveInfo = new InstantAppResolveInfo(digest.getInstantAppDigest(), packageName, createInstantAppIntentFilterList(filters), versionCode);
        this.mLegacyFilters = null;
    }

    public EphemeralResolveInfo(String hostName, String packageName, List<EphemeralIntentFilter> filters) {
        this(new EphemeralDigest(hostName), packageName, (List) filters);
    }

    EphemeralResolveInfo(Parcel in) {
        this.mInstantAppResolveInfo = (InstantAppResolveInfo) in.readParcelable(null);
        this.mLegacyFilters = new ArrayList();
        in.readList(this.mLegacyFilters, null);
    }

    public InstantAppResolveInfo getInstantAppResolveInfo() {
        return this.mInstantAppResolveInfo;
    }

    private static List<InstantAppIntentFilter> createInstantAppIntentFilterList(List<EphemeralIntentFilter> filters) {
        if (filters == null) {
            return null;
        }
        int filterCount = filters.size();
        List<InstantAppIntentFilter> returnList = new ArrayList(filterCount);
        for (int i = 0; i < filterCount; i++) {
            returnList.add(((EphemeralIntentFilter) filters.get(i)).getInstantAppIntentFilter());
        }
        return returnList;
    }

    public byte[] getDigestBytes() {
        return this.mInstantAppResolveInfo.getDigestBytes();
    }

    public int getDigestPrefix() {
        return this.mInstantAppResolveInfo.getDigestPrefix();
    }

    public String getPackageName() {
        return this.mInstantAppResolveInfo.getPackageName();
    }

    public List<EphemeralIntentFilter> getIntentFilters() {
        List<InstantAppIntentFilter> filters = this.mInstantAppResolveInfo.getIntentFilters();
        int filterCount = filters.size();
        List<EphemeralIntentFilter> returnList = new ArrayList(filterCount);
        for (int i = 0; i < filterCount; i++) {
            returnList.add(new EphemeralIntentFilter((InstantAppIntentFilter) filters.get(i)));
        }
        return returnList;
    }

    public int getVersionCode() {
        return this.mInstantAppResolveInfo.getVersionCode();
    }

    @Deprecated
    public List<IntentFilter> getFilters() {
        return this.mLegacyFilters;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.mInstantAppResolveInfo, flags);
        out.writeList(this.mLegacyFilters);
    }
}
