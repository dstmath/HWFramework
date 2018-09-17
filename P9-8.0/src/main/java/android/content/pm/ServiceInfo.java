package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Printer;

public class ServiceInfo extends ComponentInfo implements Parcelable {
    public static final Creator<ServiceInfo> CREATOR = new Creator<ServiceInfo>() {
        public ServiceInfo createFromParcel(Parcel source) {
            return new ServiceInfo(source, null);
        }

        public ServiceInfo[] newArray(int size) {
            return new ServiceInfo[size];
        }
    };
    public static final int FLAG_EXTERNAL_SERVICE = 4;
    public static final int FLAG_ISOLATED_PROCESS = 2;
    public static final int FLAG_SINGLE_USER = 1073741824;
    public static final int FLAG_STOP_WITH_TASK = 1;
    public static final int FLAG_VISIBLE_TO_INSTANT_APP = 1048576;
    public int flags;
    public String permission;

    /* synthetic */ ServiceInfo(Parcel source, ServiceInfo -this1) {
        this(source);
    }

    public ServiceInfo(ServiceInfo orig) {
        super((ComponentInfo) orig);
        this.permission = orig.permission;
        this.flags = orig.flags;
    }

    public void dump(Printer pw, String prefix) {
        dump(pw, prefix, 3);
    }

    void dump(Printer pw, String prefix, int flags) {
        super.dumpFront(pw, prefix);
        pw.println(prefix + "permission=" + this.permission);
        pw.println(prefix + "flags=0x" + Integer.toHexString(flags));
        super.dumpBack(pw, prefix, flags);
    }

    public String toString() {
        return "ServiceInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.name + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.permission);
        dest.writeInt(this.flags);
    }

    private ServiceInfo(Parcel source) {
        super(source);
        this.permission = source.readString();
        this.flags = source.readInt();
    }
}
