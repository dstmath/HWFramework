package android.hardware.radio;

import android.annotation.SystemApi;
import android.hardware.radio.ProgramSelector;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SystemApi
public final class ProgramSelector implements Parcelable {
    public static final Parcelable.Creator<ProgramSelector> CREATOR = new Parcelable.Creator<ProgramSelector>() {
        public ProgramSelector createFromParcel(Parcel in) {
            return new ProgramSelector(in);
        }

        public ProgramSelector[] newArray(int size) {
            return new ProgramSelector[size];
        }
    };
    public static final int IDENTIFIER_TYPE_AMFM_FREQUENCY = 1;
    public static final int IDENTIFIER_TYPE_DAB_ENSEMBLE = 6;
    public static final int IDENTIFIER_TYPE_DAB_FREQUENCY = 8;
    public static final int IDENTIFIER_TYPE_DAB_SCID = 7;
    public static final int IDENTIFIER_TYPE_DAB_SIDECC = 5;
    public static final int IDENTIFIER_TYPE_DAB_SID_EXT = 5;
    public static final int IDENTIFIER_TYPE_DRMO_FREQUENCY = 10;
    @Deprecated
    public static final int IDENTIFIER_TYPE_DRMO_MODULATION = 11;
    public static final int IDENTIFIER_TYPE_DRMO_SERVICE_ID = 9;
    public static final int IDENTIFIER_TYPE_HD_STATION_ID_EXT = 3;
    public static final int IDENTIFIER_TYPE_HD_STATION_NAME = 10004;
    @Deprecated
    public static final int IDENTIFIER_TYPE_HD_SUBCHANNEL = 4;
    public static final int IDENTIFIER_TYPE_INVALID = 0;
    public static final int IDENTIFIER_TYPE_RDS_PI = 2;
    public static final int IDENTIFIER_TYPE_SXM_CHANNEL = 13;
    public static final int IDENTIFIER_TYPE_SXM_SERVICE_ID = 12;
    public static final int IDENTIFIER_TYPE_VENDOR_END = 1999;
    @Deprecated
    public static final int IDENTIFIER_TYPE_VENDOR_PRIMARY_END = 1999;
    @Deprecated
    public static final int IDENTIFIER_TYPE_VENDOR_PRIMARY_START = 1000;
    public static final int IDENTIFIER_TYPE_VENDOR_START = 1000;
    @Deprecated
    public static final int PROGRAM_TYPE_AM = 1;
    @Deprecated
    public static final int PROGRAM_TYPE_AM_HD = 3;
    @Deprecated
    public static final int PROGRAM_TYPE_DAB = 5;
    @Deprecated
    public static final int PROGRAM_TYPE_DRMO = 6;
    @Deprecated
    public static final int PROGRAM_TYPE_FM = 2;
    @Deprecated
    public static final int PROGRAM_TYPE_FM_HD = 4;
    @Deprecated
    public static final int PROGRAM_TYPE_INVALID = 0;
    @Deprecated
    public static final int PROGRAM_TYPE_SXM = 7;
    @Deprecated
    public static final int PROGRAM_TYPE_VENDOR_END = 1999;
    @Deprecated
    public static final int PROGRAM_TYPE_VENDOR_START = 1000;
    private final Identifier mPrimaryId;
    private final int mProgramType;
    private final Identifier[] mSecondaryIds;
    private final long[] mVendorIds;

    public static final class Identifier implements Parcelable {
        public static final Parcelable.Creator<Identifier> CREATOR = new Parcelable.Creator<Identifier>() {
            public Identifier createFromParcel(Parcel in) {
                return new Identifier(in);
            }

            public Identifier[] newArray(int size) {
                return new Identifier[size];
            }
        };
        private final int mType;
        private final long mValue;

        public Identifier(int type, long value) {
            this.mType = type == 10004 ? 4 : type;
            this.mValue = value;
        }

        public int getType() {
            if (this.mType != 4 || this.mValue <= 10) {
                return this.mType;
            }
            return ProgramSelector.IDENTIFIER_TYPE_HD_STATION_NAME;
        }

        public long getValue() {
            return this.mValue;
        }

        public String toString() {
            return "Identifier(" + this.mType + ", " + this.mValue + ")";
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.mType), Long.valueOf(this.mValue)});
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Identifier)) {
                return false;
            }
            Identifier other = (Identifier) obj;
            if (!(other.getType() == this.mType && other.getValue() == this.mValue)) {
                z = false;
            }
            return z;
        }

        private Identifier(Parcel in) {
            this.mType = in.readInt();
            this.mValue = in.readLong();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mType);
            dest.writeLong(this.mValue);
        }

        public int describeContents() {
            return 0;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface IdentifierType {
    }

    @Deprecated
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProgramType {
    }

    public ProgramSelector(int programType, Identifier primaryId, Identifier[] secondaryIds, long[] vendorIds) {
        secondaryIds = secondaryIds == null ? new Identifier[0] : secondaryIds;
        vendorIds = vendorIds == null ? new long[0] : vendorIds;
        if (!Stream.of(secondaryIds).anyMatch($$Lambda$ProgramSelector$pPCu6h7REdNveY60TFDS4pIKk.INSTANCE)) {
            this.mProgramType = programType;
            this.mPrimaryId = (Identifier) Objects.requireNonNull(primaryId);
            this.mSecondaryIds = secondaryIds;
            this.mVendorIds = vendorIds;
            return;
        }
        throw new IllegalArgumentException("secondaryIds list must not contain nulls");
    }

    static /* synthetic */ boolean lambda$new$0(Identifier id) {
        return id == null;
    }

    @Deprecated
    public int getProgramType() {
        return this.mProgramType;
    }

    public Identifier getPrimaryId() {
        return this.mPrimaryId;
    }

    public Identifier[] getSecondaryIds() {
        return this.mSecondaryIds;
    }

    public long getFirstId(int type) {
        if (this.mPrimaryId.getType() == type) {
            return this.mPrimaryId.getValue();
        }
        for (Identifier id : this.mSecondaryIds) {
            if (id.getType() == type) {
                return id.getValue();
            }
        }
        throw new IllegalArgumentException("Identifier " + type + " not found");
    }

    public Identifier[] getAllIds(int type) {
        List<Identifier> out = new ArrayList<>();
        if (this.mPrimaryId.getType() == type) {
            out.add(this.mPrimaryId);
        }
        for (Identifier id : this.mSecondaryIds) {
            if (id.getType() == type) {
                out.add(id);
            }
        }
        return (Identifier[]) out.toArray(new Identifier[out.size()]);
    }

    @Deprecated
    public long[] getVendorIds() {
        return this.mVendorIds;
    }

    public ProgramSelector withSecondaryPreferred(Identifier preferred) {
        int preferredType = preferred.getType();
        return new ProgramSelector(this.mProgramType, this.mPrimaryId, (Identifier[]) Stream.concat(Arrays.stream(this.mSecondaryIds).filter(new Predicate(preferredType) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return ProgramSelector.lambda$withSecondaryPreferred$1(this.f$0, (ProgramSelector.Identifier) obj);
            }
        }), Stream.of(preferred)).toArray($$Lambda$ProgramSelector$kEsOH_p_eN5KvKLjoDTGZXYtuP4.INSTANCE), this.mVendorIds);
    }

    static /* synthetic */ boolean lambda$withSecondaryPreferred$1(int preferredType, Identifier id) {
        return id.getType() != preferredType;
    }

    static /* synthetic */ Identifier[] lambda$withSecondaryPreferred$2(int x$0) {
        return new Identifier[x$0];
    }

    public static ProgramSelector createAmFmSelector(int band, int frequencyKhz) {
        return createAmFmSelector(band, frequencyKhz, 0);
    }

    private static boolean isValidAmFmFrequency(boolean isAm, int frequencyKhz) {
        boolean z = false;
        if (isAm) {
            if (frequencyKhz > 150 && frequencyKhz <= 30000) {
                z = true;
            }
            return z;
        }
        if (frequencyKhz > 60000 && frequencyKhz < 110000) {
            z = true;
        }
        return z;
    }

    public static ProgramSelector createAmFmSelector(int band, int frequencyKhz, int subChannel) {
        int programType = 2;
        if (band == -1) {
            if (frequencyKhz < 50000) {
                band = subChannel <= 0 ? 0 : 3;
            } else {
                band = subChannel <= 0 ? 1 : 2;
            }
        }
        boolean isAm = band == 0 || band == 3;
        boolean isDigital = band == 3 || band == 2;
        if (!isAm && !isDigital && band != 1) {
            throw new IllegalArgumentException("Unknown band: " + band);
        } else if (subChannel < 0 || subChannel > 8) {
            throw new IllegalArgumentException("Invalid subchannel: " + subChannel);
        } else if (subChannel > 0 && !isDigital) {
            throw new IllegalArgumentException("Subchannels are not supported for non-HD radio");
        } else if (isValidAmFmFrequency(isAm, frequencyKhz)) {
            if (isAm) {
                programType = 1;
            }
            Identifier primary = new Identifier(1, (long) frequencyKhz);
            Identifier[] secondary = null;
            if (subChannel > 0) {
                secondary = new Identifier[]{new Identifier(4, (long) (subChannel - 1))};
            }
            return new ProgramSelector(programType, primary, secondary, null);
        } else {
            throw new IllegalArgumentException("Provided value is not a valid AM/FM frequency: " + frequencyKhz);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ProgramSelector(type=");
        sb.append(this.mProgramType);
        sb.append(", primary=");
        StringBuilder sb2 = sb.append(this.mPrimaryId);
        if (this.mSecondaryIds.length > 0) {
            sb2.append(", secondary=");
            sb2.append(this.mSecondaryIds);
        }
        if (this.mVendorIds.length > 0) {
            sb2.append(", vendor=");
            sb2.append(this.mVendorIds);
        }
        sb2.append(")");
        return sb2.toString();
    }

    public int hashCode() {
        return this.mPrimaryId.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProgramSelector)) {
            return false;
        }
        return this.mPrimaryId.equals(((ProgramSelector) obj).getPrimaryId());
    }

    private ProgramSelector(Parcel in) {
        this.mProgramType = in.readInt();
        this.mPrimaryId = (Identifier) in.readTypedObject(Identifier.CREATOR);
        this.mSecondaryIds = (Identifier[]) in.createTypedArray(Identifier.CREATOR);
        if (!Stream.of(this.mSecondaryIds).anyMatch($$Lambda$ProgramSelector$nFx6NEitx7YUkyrPxAq5zDeJdQ.INSTANCE)) {
            this.mVendorIds = in.createLongArray();
            return;
        }
        throw new IllegalArgumentException("secondaryIds list must not contain nulls");
    }

    static /* synthetic */ boolean lambda$new$3(Identifier id) {
        return id == null;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mProgramType);
        dest.writeTypedObject(this.mPrimaryId, 0);
        dest.writeTypedArray(this.mSecondaryIds, 0);
        dest.writeLongArray(this.mVendorIds);
    }

    public int describeContents() {
        return 0;
    }
}
