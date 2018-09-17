package android.content.pm;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Collections;
import java.util.List;

public final class SharedLibraryInfo implements Parcelable {
    public static final Creator<SharedLibraryInfo> CREATOR = new Creator<SharedLibraryInfo>() {
        public SharedLibraryInfo createFromParcel(Parcel source) {
            return new SharedLibraryInfo(source, null);
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
    private final int mVersion;

    /* synthetic */ SharedLibraryInfo(Parcel parcel, SharedLibraryInfo -this1) {
        this(parcel);
    }

    public SharedLibraryInfo(String name, int version, int type, VersionedPackage declaringPackage, List<VersionedPackage> dependentPackages) {
        this.mName = name;
        this.mVersion = version;
        this.mType = type;
        this.mDeclaringPackage = declaringPackage;
        this.mDependentPackages = dependentPackages;
    }

    private SharedLibraryInfo(Parcel parcel) {
        this(parcel.readString(), parcel.readInt(), parcel.readInt(), (VersionedPackage) parcel.readParcelable(null), parcel.readArrayList(null));
    }

    public int getType() {
        return this.mType;
    }

    public String getName() {
        return this.mName;
    }

    public int getVersion() {
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
        return "SharedLibraryInfo[name:" + this.mName + ", type:" + typeToString(this.mType) + ", version:" + this.mVersion + (!getDependentPackages().isEmpty() ? " has dependents" : ProxyInfo.LOCAL_EXCL_LIST);
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mName);
        parcel.writeInt(this.mVersion);
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
