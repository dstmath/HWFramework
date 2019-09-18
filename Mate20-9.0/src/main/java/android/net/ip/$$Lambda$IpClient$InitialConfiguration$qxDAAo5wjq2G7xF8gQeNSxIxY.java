package android.net.ip;

import android.net.IpPrefix;
import android.net.ip.IpClient;
import java.util.function.Predicate;

/* renamed from: android.net.ip.-$$Lambda$IpClient$InitialConfiguration$-qxDAAo5wjq2G7x-F8gQeNSxIxY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IpClient$InitialConfiguration$qxDAAo5wjq2G7xF8gQeNSxIxY implements Predicate {
    public static final /* synthetic */ $$Lambda$IpClient$InitialConfiguration$qxDAAo5wjq2G7xF8gQeNSxIxY INSTANCE = new $$Lambda$IpClient$InitialConfiguration$qxDAAo5wjq2G7xF8gQeNSxIxY();

    private /* synthetic */ $$Lambda$IpClient$InitialConfiguration$qxDAAo5wjq2G7xF8gQeNSxIxY() {
    }

    public final boolean test(Object obj) {
        return IpClient.InitialConfiguration.isPrefixLengthCompliant((IpPrefix) obj);
    }
}
