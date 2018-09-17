package android.companion;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.provider.OneTimeUseBuilder;
import android.text.TextUtils;
import com.android.internal.util.BitUtils;
import com.android.internal.util.ObjectUtils;
import com.android.internal.util.Preconditions;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public final class BluetoothLeDeviceFilter implements DeviceFilter<ScanResult> {
    public static final Creator<BluetoothLeDeviceFilter> CREATOR = new Creator<BluetoothLeDeviceFilter>() {
        public BluetoothLeDeviceFilter createFromParcel(Parcel in) {
            Builder builder = new Builder().setNamePattern(BluetoothDeviceFilterUtils.patternFromString(in.readString())).setScanFilter((ScanFilter) in.readParcelable(null));
            byte[] rawDataFilter = in.createByteArray();
            byte[] rawDataFilterMask = in.createByteArray();
            if (rawDataFilter != null) {
                builder.setRawDataFilter(rawDataFilter, rawDataFilterMask);
            }
            String renamePrefix = in.readString();
            String suffix = in.readString();
            int bytesFrom = in.readInt();
            int bytesTo = in.readInt();
            int nameFrom = in.readInt();
            int nameTo = in.readInt();
            boolean bytesReverseOrder = in.readBoolean();
            if (renamePrefix != null) {
                if (bytesFrom >= 0) {
                    builder.setRenameFromBytes(renamePrefix, suffix, bytesFrom, bytesTo, bytesReverseOrder ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                } else {
                    builder.setRenameFromName(renamePrefix, suffix, nameFrom, nameTo);
                }
            }
            return builder.build();
        }

        public BluetoothLeDeviceFilter[] newArray(int size) {
            return new BluetoothLeDeviceFilter[size];
        }
    };
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "BluetoothLeDeviceFilter";
    private static final int RENAME_PREFIX_LENGTH_LIMIT = 10;
    private final Pattern mNamePattern;
    private final byte[] mRawDataFilter;
    private final byte[] mRawDataFilterMask;
    private final int mRenameBytesFrom;
    private final int mRenameBytesLength;
    private final boolean mRenameBytesReverseOrder;
    private final int mRenameNameFrom;
    private final int mRenameNameLength;
    private final String mRenamePrefix;
    private final String mRenameSuffix;
    private final ScanFilter mScanFilter;

    public static final class Builder extends OneTimeUseBuilder<BluetoothLeDeviceFilter> {
        private Pattern mNamePattern;
        private byte[] mRawDataFilter;
        private byte[] mRawDataFilterMask;
        private int mRenameBytesFrom = -1;
        private int mRenameBytesLength;
        private boolean mRenameBytesReverseOrder = false;
        private int mRenameNameFrom = -1;
        private int mRenameNameLength;
        private String mRenamePrefix;
        private String mRenameSuffix;
        private ScanFilter mScanFilter;

        public Builder setNamePattern(Pattern regex) {
            checkNotUsed();
            this.mNamePattern = regex;
            return this;
        }

        public Builder setScanFilter(ScanFilter scanFilter) {
            checkNotUsed();
            this.mScanFilter = scanFilter;
            return this;
        }

        public Builder setRawDataFilter(byte[] rawDataFilter, byte[] rawDataFilterMask) {
            boolean z = true;
            checkNotUsed();
            Preconditions.checkNotNull(rawDataFilter);
            if (!(rawDataFilterMask == null || rawDataFilter.length == rawDataFilterMask.length)) {
                z = false;
            }
            Preconditions.checkArgument(z, "Mask and filter should be the same length");
            this.mRawDataFilter = rawDataFilter;
            this.mRawDataFilterMask = rawDataFilterMask;
            return this;
        }

        public Builder setRenameFromBytes(String prefix, String suffix, int bytesFrom, int bytesLength, ByteOrder byteOrder) {
            checkRenameNotSet();
            checkRangeNotEmpty(bytesLength);
            this.mRenameBytesFrom = bytesFrom;
            this.mRenameBytesLength = bytesLength;
            this.mRenameBytesReverseOrder = byteOrder == ByteOrder.LITTLE_ENDIAN;
            return setRename(prefix, suffix);
        }

        public Builder setRenameFromName(String prefix, String suffix, int nameFrom, int nameLength) {
            checkRenameNotSet();
            checkRangeNotEmpty(nameLength);
            this.mRenameNameFrom = nameFrom;
            this.mRenameNameLength = nameLength;
            this.mRenameBytesReverseOrder = false;
            return setRename(prefix, suffix);
        }

        private void checkRenameNotSet() {
            Preconditions.checkState(this.mRenamePrefix == null, "Renaming rule can only be set once");
        }

        private void checkRangeNotEmpty(int length) {
            boolean z = false;
            if (length > 0) {
                z = true;
            }
            Preconditions.checkArgument(z, "Range must be non-empty");
        }

        private Builder setRename(String prefix, String suffix) {
            checkNotUsed();
            Preconditions.checkArgument(TextUtils.length(prefix) <= BluetoothLeDeviceFilter.getRenamePrefixLengthLimit(), "Prefix is too long");
            this.mRenamePrefix = prefix;
            this.mRenameSuffix = suffix;
            return this;
        }

        public BluetoothLeDeviceFilter build() {
            markUsed();
            return new BluetoothLeDeviceFilter(this.mNamePattern, this.mScanFilter, this.mRawDataFilter, this.mRawDataFilterMask, this.mRenamePrefix, this.mRenameSuffix, this.mRenameBytesFrom, this.mRenameBytesLength, this.mRenameNameFrom, this.mRenameNameLength, this.mRenameBytesReverseOrder, null);
        }
    }

    /* synthetic */ BluetoothLeDeviceFilter(Pattern namePattern, ScanFilter scanFilter, byte[] rawDataFilter, byte[] rawDataFilterMask, String renamePrefix, String renameSuffix, int renameBytesFrom, int renameBytesLength, int renameNameFrom, int renameNameLength, boolean renameBytesReverseOrder, BluetoothLeDeviceFilter -this11) {
        this(namePattern, scanFilter, rawDataFilter, rawDataFilterMask, renamePrefix, renameSuffix, renameBytesFrom, renameBytesLength, renameNameFrom, renameNameLength, renameBytesReverseOrder);
    }

    private BluetoothLeDeviceFilter(Pattern namePattern, ScanFilter scanFilter, byte[] rawDataFilter, byte[] rawDataFilterMask, String renamePrefix, String renameSuffix, int renameBytesFrom, int renameBytesLength, int renameNameFrom, int renameNameLength, boolean renameBytesReverseOrder) {
        this.mNamePattern = namePattern;
        this.mScanFilter = (ScanFilter) ObjectUtils.firstNotNull(scanFilter, ScanFilter.EMPTY);
        this.mRawDataFilter = rawDataFilter;
        this.mRawDataFilterMask = rawDataFilterMask;
        this.mRenamePrefix = renamePrefix;
        this.mRenameSuffix = renameSuffix;
        this.mRenameBytesFrom = renameBytesFrom;
        this.mRenameBytesLength = renameBytesLength;
        this.mRenameNameFrom = renameNameFrom;
        this.mRenameNameLength = renameNameLength;
        this.mRenameBytesReverseOrder = renameBytesReverseOrder;
    }

    public Pattern getNamePattern() {
        return this.mNamePattern;
    }

    public ScanFilter getScanFilter() {
        return this.mScanFilter;
    }

    public byte[] getRawDataFilter() {
        return this.mRawDataFilter;
    }

    public byte[] getRawDataFilterMask() {
        return this.mRawDataFilterMask;
    }

    public String getRenamePrefix() {
        return this.mRenamePrefix;
    }

    public String getRenameSuffix() {
        return this.mRenameSuffix;
    }

    public int getRenameBytesFrom() {
        return this.mRenameBytesFrom;
    }

    public int getRenameBytesLength() {
        return this.mRenameBytesLength;
    }

    public boolean isRenameBytesReverseOrder() {
        return this.mRenameBytesReverseOrder;
    }

    public String getDeviceDisplayName(ScanResult sr) {
        if (this.mRenameBytesFrom < 0 && this.mRenameNameFrom < 0) {
            return BluetoothDeviceFilterUtils.getDeviceDisplayNameInternal(sr.getDevice());
        }
        StringBuilder sb = new StringBuilder(TextUtils.emptyIfNull(this.mRenamePrefix));
        if (this.mRenameBytesFrom >= 0) {
            byte[] bytes = sr.getScanRecord().getBytes();
            int startInclusive = this.mRenameBytesFrom;
            int endInclusive = (this.mRenameBytesFrom + this.mRenameBytesLength) - 1;
            int initial = this.mRenameBytesReverseOrder ? endInclusive : startInclusive;
            int step = this.mRenameBytesReverseOrder ? -1 : 1;
            int i = initial;
            while (startInclusive <= i && i <= endInclusive) {
                sb.append(Byte.toHexString(bytes[i], true));
                i += step;
            }
        } else {
            sb.append(BluetoothDeviceFilterUtils.getDeviceDisplayNameInternal(sr.getDevice()).substring(this.mRenameNameFrom, this.mRenameNameFrom + this.mRenameNameLength));
        }
        return sb.append(TextUtils.emptyIfNull(this.mRenameSuffix)).toString();
    }

    public boolean matches(ScanResult device) {
        if (!matches(device.getDevice())) {
            return false;
        }
        if (this.mRawDataFilter != null) {
            return BitUtils.maskedEquals(device.getScanRecord().getBytes(), this.mRawDataFilter, this.mRawDataFilterMask);
        }
        return true;
    }

    private boolean matches(BluetoothDevice device) {
        if (BluetoothDeviceFilterUtils.matches(getScanFilter(), device)) {
            return BluetoothDeviceFilterUtils.matchesName(getNamePattern(), device);
        }
        return false;
    }

    public int getMediumType() {
        return 1;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BluetoothLeDeviceFilter that = (BluetoothLeDeviceFilter) o;
        if (this.mRenameBytesFrom == that.mRenameBytesFrom && this.mRenameBytesLength == that.mRenameBytesLength && this.mRenameNameFrom == that.mRenameNameFrom && this.mRenameNameLength == that.mRenameNameLength && this.mRenameBytesReverseOrder == that.mRenameBytesReverseOrder && Objects.equals(this.mNamePattern, that.mNamePattern) && Objects.equals(this.mScanFilter, that.mScanFilter) && Arrays.equals(this.mRawDataFilter, that.mRawDataFilter) && Arrays.equals(this.mRawDataFilterMask, that.mRawDataFilterMask) && Objects.equals(this.mRenamePrefix, that.mRenamePrefix)) {
            z = Objects.equals(this.mRenameSuffix, that.mRenameSuffix);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mNamePattern, this.mScanFilter, this.mRawDataFilter, this.mRawDataFilterMask, this.mRenamePrefix, this.mRenameSuffix, Integer.valueOf(this.mRenameBytesFrom), Integer.valueOf(this.mRenameBytesLength), Integer.valueOf(this.mRenameNameFrom), Integer.valueOf(this.mRenameNameLength), Boolean.valueOf(this.mRenameBytesReverseOrder)});
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(BluetoothDeviceFilterUtils.patternToString(getNamePattern()));
        dest.writeParcelable(this.mScanFilter, flags);
        dest.writeByteArray(this.mRawDataFilter);
        dest.writeByteArray(this.mRawDataFilterMask);
        dest.writeString(this.mRenamePrefix);
        dest.writeString(this.mRenameSuffix);
        dest.writeInt(this.mRenameBytesFrom);
        dest.writeInt(this.mRenameBytesLength);
        dest.writeInt(this.mRenameNameFrom);
        dest.writeInt(this.mRenameNameLength);
        dest.writeBoolean(this.mRenameBytesReverseOrder);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "BluetoothLEDeviceFilter{mNamePattern=" + this.mNamePattern + ", mScanFilter=" + this.mScanFilter + ", mRawDataFilter=" + Arrays.toString(this.mRawDataFilter) + ", mRawDataFilterMask=" + Arrays.toString(this.mRawDataFilterMask) + ", mRenamePrefix='" + this.mRenamePrefix + '\'' + ", mRenameSuffix='" + this.mRenameSuffix + '\'' + ", mRenameBytesFrom=" + this.mRenameBytesFrom + ", mRenameBytesLength=" + this.mRenameBytesLength + ", mRenameNameFrom=" + this.mRenameNameFrom + ", mRenameNameLength=" + this.mRenameNameLength + ", mRenameBytesReverseOrder=" + this.mRenameBytesReverseOrder + '}';
    }

    public static int getRenamePrefixLengthLimit() {
        return 10;
    }
}
