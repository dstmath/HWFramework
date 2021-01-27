package ohos.bundle;

import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ShellInfo implements Sequenceable {
    public static final Sequenceable.Producer<ShellInfo> PRODUCER = $$Lambda$ShellInfo$AW7W4jpPydpReu_Dwiedarn82Uk.INSTANCE;
    private static final int SHELL_TYPE_INVALID_NUM = -1;
    private String name = "";
    private String packageName = "";
    private ShellType type = ShellType.UNKNOWN;

    public enum ShellType {
        UNKNOWN,
        ACTIVITY,
        SERVICE,
        PROVIDER,
        WEB
    }

    static /* synthetic */ ShellInfo lambda$static$0(Parcel parcel) {
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.unmarshalling(parcel);
        return shellInfo;
    }

    public ShellInfo() {
    }

    public ShellInfo(ShellInfo shellInfo) {
        this.packageName = shellInfo.packageName;
        this.name = shellInfo.name;
        this.type = shellInfo.type;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public ShellType getType() {
        return this.type;
    }

    public void setType(ShellType shellType) {
        this.type = shellType;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel.writeString(this.packageName) && parcel.writeString(this.name) && parcel.writeInt(this.type.ordinal())) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.packageName = parcel.readString();
        this.name = parcel.readString();
        int readInt = parcel.readInt();
        if (readInt <= -1 || readInt >= ShellType.values().length) {
            return false;
        }
        this.type = ShellType.values()[readInt];
        return true;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ShellInfo)) {
            return false;
        }
        ShellInfo shellInfo = (ShellInfo) obj;
        if (Objects.equals(this.packageName, shellInfo.packageName) && Objects.equals(this.name, shellInfo.name)) {
            return this.type == shellInfo.type;
        }
        return false;
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }
}
