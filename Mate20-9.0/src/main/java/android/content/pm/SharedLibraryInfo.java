package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

public final class SharedLibraryInfo implements Parcelable {
    public static final Parcelable.Creator<SharedLibraryInfo> CREATOR = new Parcelable.Creator<SharedLibraryInfo>() {
        public SharedLibraryInfo createFromParcel(Parcel source) {
            return new SharedLibraryInfo(source);
        }

        public SharedLibraryInfo[] newArray(int size) {
            return new SharedLibraryInfo[size];
        }
    };
    public static final int TYPE_BUILTIN = 0;
    public static final int TYPE_DYNAMIC = 1;
    public static final int TYPE_STATIC = 2;
    public static final int VERSION_UNDEFINED = -1;
    private final VersionedPackage mDeclaringPackage;
    private final List<VersionedPackage> mDependentPackages;
    private final String mName;
    private final int mType;
    private final long mVersion;

    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
    }

    public SharedLibraryInfo(String name, long version, int type, VersionedPackage declaringPackage, List<VersionedPackage> dependentPackages) {
        this.mName = name;
        this.mVersion = version;
        this.mType = type;
        this.mDeclaringPackage = declaringPackage;
        this.mDependentPackages = dependentPackages;
    }

    private SharedLibraryInfo(Parcel parcel) {
        this(parcel.readString(), parcel.readLong(), parcel.readInt(), (VersionedPackage) parcel.readParcelable(null), parcel.readArrayList(null));
    }

    public int getType() {
        return this.mType;
    }

    public String getName() {
        return this.mName;
    }

    @Deprecated
    public int getVersion() {
        return (int) (this.mVersion < 0 ? this.mVersion : this.mVersion & 2147483647L);
    }

    public long getLongVersion() {
        return this.mVersion;
    }

    public boolean isBuiltin() {
        return this.mType == 0;
    }

    public boolean isDynamic() {
        return this.mType == 1;
    }

    public boolean isStatic() {
        return this.mType == 2;
    }

    public VersionedPackage getDeclaringPackage() {
        return this.mDeclaringPackage;
    }

    public List<VersionedPackage> getDependentPackages() {
        if (this.mDependentPackages == null) {
            return Collections.emptyList();
        }
        return this.mDependentPackages;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SharedLibraryInfo[name:");
        sb.append(this.mName);
        sb.append(", type:");
        sb.append(typeToString(this.mType));
        sb.append(", version:");
        sb.append(this.mVersion);
        sb.append(!getDependentPackages().isEmpty() ? " has dependents" : "");
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mName);
        parcel.writeLong(this.mVersion);
        parcel.writeInt(this.mType);
        parcel.writeParcelable(this.mDeclaringPackage, flags);
        parcel.writeList(this.mDependentPackages);
    }

    private static String typeToString(int type) {
        switch (type) {
            case 0:
                return "builtin";
            case 1:
                return "dynamic";
            case 2:
                return "static";
            default:
                return "unknown";
        }
    }
}
