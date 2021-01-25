package android.net.shared;

import android.net.PrivateDnsConfigParcel;
import android.text.TextUtils;
import com.android.server.slice.SliceClientPermissions;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class PrivateDnsConfig {
    public final String hostname;
    public final InetAddress[] ips;
    public final boolean useTls;

    public PrivateDnsConfig() {
        this(false);
    }

    public PrivateDnsConfig(boolean useTls2) {
        this.useTls = useTls2;
        this.hostname = "";
        this.ips = new InetAddress[0];
    }

    public PrivateDnsConfig(String hostname2, InetAddress[] ips2) {
        this.useTls = !TextUtils.isEmpty(hostname2);
        this.hostname = this.useTls ? hostname2 : "";
        this.ips = ips2 != null ? ips2 : new InetAddress[0];
    }

    public PrivateDnsConfig(PrivateDnsConfig cfg) {
        this.useTls = cfg.useTls;
        this.hostname = cfg.hostname;
        this.ips = cfg.ips;
    }

    public boolean inStrictMode() {
        return this.useTls && !TextUtils.isEmpty(this.hostname);
    }

    public String toString() {
        return PrivateDnsConfig.class.getSimpleName() + "{" + this.useTls + ":" + this.hostname + SliceClientPermissions.SliceAuthority.DELIMITER + Arrays.toString(this.ips) + "}";
    }

    public PrivateDnsConfigParcel toParcel() {
        PrivateDnsConfigParcel parcel = new PrivateDnsConfigParcel();
        parcel.hostname = this.hostname;
        parcel.ips = (String[]) ParcelableUtil.toParcelableArray(Arrays.asList(this.ips), $$Lambda$OsobWheG5dMvEj_cOJtueqUBqBI.INSTANCE, String.class);
        return parcel;
    }

    public static PrivateDnsConfig fromParcel(PrivateDnsConfigParcel parcel) {
        ArrayList fromParcelableArray = ParcelableUtil.fromParcelableArray(parcel.ips, $$Lambda$SYWvjOUPlAZ_O2Z6yfFU9np1858.INSTANCE);
        return new PrivateDnsConfig(parcel.hostname, (InetAddress[]) fromParcelableArray.toArray(new InetAddress[parcel.ips.length]));
    }
}
