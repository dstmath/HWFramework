package android.content.pm;

import android.annotation.SystemApi;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SystemApi
public final class InstantAppResolveInfo implements Parcelable {
    public static final Parcelable.Creator<InstantAppResolveInfo> CREATOR = new Parcelable.Creator<InstantAppResolveInfo>() {
        public InstantAppResolveInfo createFromParcel(Parcel in) {
            return new InstantAppResolveInfo(in);
        }

        public InstantAppResolveInfo[] newArray(int size) {
            return new InstantAppResolveInfo[size];
        }
    };
    private static final byte[] EMPTY_DIGEST = new byte[0];
    private static final String SHA_ALGORITHM = "SHA-256";
    private final InstantAppDigest mDigest;
    private final Bundle mExtras;
    private final List<InstantAppIntentFilter> mFilters;
    private final String mPackageName;
    private final boolean mShouldLetInstallerDecide;
    private final long mVersionCode;

    @SystemApi
    public static final class InstantAppDigest implements Parcelable {
        public static final Parcelable.Creator<InstantAppDigest> CREATOR = new Parcelable.Creator<InstantAppDigest>() {
            public InstantAppDigest createFromParcel(Parcel in) {
                if (in.readBoolean()) {
                    return InstantAppDigest.UNDEFINED;
                }
                return new InstantAppDigest(in);
            }

            public InstantAppDigest[] newArray(int size) {
                return new InstantAppDigest[size];
            }
        };
        static final int DIGEST_MASK = -4096;
        public static final InstantAppDigest UNDEFINED = new InstantAppDigest(new byte[0][], new int[0]);
        private static Random sRandom;
        /* access modifiers changed from: private */
        public final byte[][] mDigestBytes;
        private final int[] mDigestPrefix;
        private int[] mDigestPrefixSecure;

        static {
            sRandom = null;
            try {
                sRandom = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e) {
                sRandom = new Random();
            }
        }

        public InstantAppDigest(String hostName) {
            this(hostName, -1);
        }

        public InstantAppDigest(String hostName, int maxDigests) {
            if (hostName != null) {
                this.mDigestBytes = generateDigest(hostName.toLowerCase(Locale.ENGLISH), maxDigests);
                this.mDigestPrefix = new int[this.mDigestBytes.length];
                for (int i = 0; i < this.mDigestBytes.length; i++) {
                    this.mDigestPrefix[i] = (((this.mDigestBytes[i][0] & 255) << 24) | ((this.mDigestBytes[i][1] & 255) << 16) | ((this.mDigestBytes[i][2] & 255) << 8) | ((this.mDigestBytes[i][3] & 255) << 0)) & DIGEST_MASK;
                }
                return;
            }
            throw new IllegalArgumentException();
        }

        private InstantAppDigest(byte[][] digestBytes, int[] prefix) {
            this.mDigestPrefix = prefix;
            this.mDigestBytes = digestBytes;
        }

        private static byte[][] generateDigest(String hostName, int maxDigests) {
            ArrayList<byte[]> digests = new ArrayList<>();
            try {
                MessageDigest digest = MessageDigest.getInstance(InstantAppResolveInfo.SHA_ALGORITHM);
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
            this.mDigestPrefixSecure = in.createIntArray();
        }

        public byte[][] getDigestBytes() {
            return this.mDigestBytes;
        }

        public int[] getDigestPrefix() {
            return this.mDigestPrefix;
        }

        public int[] getDigestPrefixSecure() {
            if (this == UNDEFINED) {
                return getDigestPrefix();
            }
            if (this.mDigestPrefixSecure == null) {
                int realSize = getDigestPrefix().length;
                int manufacturedSize = realSize + 10 + sRandom.nextInt(10);
                this.mDigestPrefixSecure = Arrays.copyOf(getDigestPrefix(), manufacturedSize);
                for (int i = realSize; i < manufacturedSize; i++) {
                    this.mDigestPrefixSecure[i] = sRandom.nextInt() & DIGEST_MASK;
                }
                Arrays.sort(this.mDigestPrefixSecure);
            }
            return this.mDigestPrefixSecure;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            boolean isUndefined = this == UNDEFINED;
            out.writeBoolean(isUndefined);
            if (!isUndefined) {
                if (this.mDigestBytes == null) {
                    out.writeInt(-1);
                } else {
                    out.writeInt(this.mDigestBytes.length);
                    for (byte[] writeByteArray : this.mDigestBytes) {
                        out.writeByteArray(writeByteArray);
                    }
                }
                out.writeIntArray(this.mDigestPrefix);
                out.writeIntArray(this.mDigestPrefixSecure);
            }
        }
    }

    public InstantAppResolveInfo(InstantAppDigest digest, String packageName, List<InstantAppIntentFilter> filters, int versionCode) {
        this(digest, packageName, filters, (long) versionCode, null);
    }

    public InstantAppResolveInfo(InstantAppDigest digest, String packageName, List<InstantAppIntentFilter> filters, long versionCode, Bundle extras) {
        this(digest, packageName, filters, versionCode, extras, false);
    }

    public InstantAppResolveInfo(String hostName, String packageName, List<InstantAppIntentFilter> filters) {
        this(new InstantAppDigest(hostName), packageName, filters, -1, null);
    }

    public InstantAppResolveInfo(Bundle extras) {
        this(InstantAppDigest.UNDEFINED, null, null, -1, extras, true);
    }

    private InstantAppResolveInfo(InstantAppDigest digest, String packageName, List<InstantAppIntentFilter> filters, long versionCode, Bundle extras, boolean shouldLetInstallerDecide) {
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
            this.mExtras = extras;
            this.mShouldLetInstallerDecide = shouldLetInstallerDecide;
            return;
        }
        throw new IllegalArgumentException();
    }

    InstantAppResolveInfo(Parcel in) {
        this.mShouldLetInstallerDecide = in.readBoolean();
        this.mExtras = in.readBundle();
        if (this.mShouldLetInstallerDecide) {
            this.mDigest = InstantAppDigest.UNDEFINED;
            this.mPackageName = null;
            this.mFilters = Collections.emptyList();
            this.mVersionCode = -1;
            return;
        }
        this.mDigest = (InstantAppDigest) in.readParcelable(null);
        this.mPackageName = in.readString();
        this.mFilters = new ArrayList();
        in.readList(this.mFilters, null);
        this.mVersionCode = in.readLong();
    }

    public boolean shouldLetInstallerDecide() {
        return this.mShouldLetInstallerDecide;
    }

    public byte[] getDigestBytes() {
        return this.mDigest.mDigestBytes.length > 0 ? this.mDigest.getDigestBytes()[0] : EMPTY_DIGEST;
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

    @Deprecated
    public int getVersionCode() {
        return (int) (this.mVersionCode & -1);
    }

    public long getLongVersionCode() {
        return this.mVersionCode;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeBoolean(this.mShouldLetInstallerDecide);
        out.writeBundle(this.mExtras);
        if (!this.mShouldLetInstallerDecide) {
            out.writeParcelable(this.mDigest, flags);
            out.writeString(this.mPackageName);
            out.writeList(this.mFilters);
            out.writeLong(this.mVersionCode);
        }
    }
}
