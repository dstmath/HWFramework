package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InstantAppResolveInfo implements Parcelable {
    public static final Creator<InstantAppResolveInfo> CREATOR = new Creator<InstantAppResolveInfo>() {
        public InstantAppResolveInfo createFromParcel(Parcel in) {
            return new InstantAppResolveInfo(in);
        }

        public InstantAppResolveInfo[] newArray(int size) {
            return new InstantAppResolveInfo[size];
        }
    };
    public static final String SHA_ALGORITHM = "SHA-256";
    private final InstantAppDigest mDigest;
    private final List<InstantAppIntentFilter> mFilters;
    private final String mPackageName;
    private final int mVersionCode;

    public static final class InstantAppDigest implements Parcelable {
        public static final Creator<InstantAppDigest> CREATOR = new Creator<InstantAppDigest>() {
            public InstantAppDigest createFromParcel(Parcel in) {
                return new InstantAppDigest(in);
            }

            public InstantAppDigest[] newArray(int size) {
                return new InstantAppDigest[size];
            }
        };
        private static final int DIGEST_MASK = -4096;
        private static final int DIGEST_PREFIX_COUNT = 5;
        private final byte[][] mDigestBytes;
        private final int[] mDigestPrefix;

        public InstantAppDigest(String hostName) {
            this(hostName, -1);
        }

        public InstantAppDigest(String hostName, int maxDigests) {
            if (hostName == null) {
                throw new IllegalArgumentException();
            }
            this.mDigestBytes = generateDigest(hostName.toLowerCase(Locale.ENGLISH), maxDigests);
            this.mDigestPrefix = new int[this.mDigestBytes.length];
            for (int i = 0; i < this.mDigestBytes.length; i++) {
                this.mDigestPrefix[i] = (((((this.mDigestBytes[i][0] & 255) << 24) | ((this.mDigestBytes[i][1] & 255) << 16)) | ((this.mDigestBytes[i][2] & 255) << 8)) | ((this.mDigestBytes[i][3] & 255) << 0)) & DIGEST_MASK;
            }
        }

        private static byte[][] generateDigest(String hostName, int maxDigests) {
            ArrayList<byte[]> digests = new ArrayList();
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                if (maxDigests <= 0) {
                    digests.add(digest.digest(hostName.getBytes()));
                } else {
                    int prevDot = hostName.lastIndexOf(46, hostName.lastIndexOf(46) - 1);
                    if (prevDot < 0) {
                        digests.add(digest.digest(hostName.getBytes()));
                    } else {
                        digests.add(digest.digest(hostName.substring(prevDot + 1, hostName.length()).getBytes()));
                        int digestCount = 1;
                        while (prevDot >= 0 && digestCount < maxDigests) {
                            prevDot = hostName.lastIndexOf(46, prevDot - 1);
                            digests.add(digest.digest(hostName.substring(prevDot + 1, hostName.length()).getBytes()));
                            digestCount++;
                        }
                    }
                }
                return (byte[][]) digests.toArray(new byte[digests.size()][]);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("could not find digest algorithm");
            }
        }

        InstantAppDigest(Parcel in) {
            int digestCount = in.readInt();
            if (digestCount == -1) {
                this.mDigestBytes = null;
            } else {
                this.mDigestBytes = new byte[digestCount][];
                for (int i = 0; i < digestCount; i++) {
                    this.mDigestBytes[i] = in.createByteArray();
                }
            }
            this.mDigestPrefix = in.createIntArray();
        }

        public byte[][] getDigestBytes() {
            return this.mDigestBytes;
        }

        public int[] getDigestPrefix() {
            return this.mDigestPrefix;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            if (this.mDigestBytes == null) {
                out.writeInt(-1);
            } else {
                out.writeInt(this.mDigestBytes.length);
                for (byte[] writeByteArray : this.mDigestBytes) {
                    out.writeByteArray(writeByteArray);
                }
            }
            out.writeIntArray(this.mDigestPrefix);
        }
    }

    public InstantAppResolveInfo(InstantAppDigest digest, String packageName, List<InstantAppIntentFilter> filters, int versionCode) {
        if ((packageName != null || filters == null || filters.size() == 0) && (packageName == null || !(filters == null || filters.size() == 0))) {
            this.mDigest = digest;
            if (filters != null) {
                this.mFilters = new ArrayList(filters.size());
                this.mFilters.addAll(filters);
            } else {
                this.mFilters = null;
            }
            this.mPackageName = packageName;
            this.mVersionCode = versionCode;
            return;
        }
        throw new IllegalArgumentException();
    }

    public InstantAppResolveInfo(String hostName, String packageName, List<InstantAppIntentFilter> filters) {
        this(new InstantAppDigest(hostName), packageName, filters, -1);
    }

    InstantAppResolveInfo(Parcel in) {
        this.mDigest = (InstantAppDigest) in.readParcelable(null);
        this.mPackageName = in.readString();
        this.mFilters = new ArrayList();
        in.readList(this.mFilters, null);
        this.mVersionCode = in.readInt();
    }

    public byte[] getDigestBytes() {
        return this.mDigest.getDigestBytes()[0];
    }

    public int getDigestPrefix() {
        return this.mDigest.getDigestPrefix()[0];
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public List<InstantAppIntentFilter> getIntentFilters() {
        return this.mFilters;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.mDigest, flags);
        out.writeString(this.mPackageName);
        out.writeList(this.mFilters);
        out.writeInt(this.mVersionCode);
    }
}
