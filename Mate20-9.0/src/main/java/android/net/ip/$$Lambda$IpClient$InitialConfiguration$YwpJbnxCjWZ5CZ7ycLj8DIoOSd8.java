package android.net.ip;

import android.net.LinkAddress;
import android.net.ip.IpClient;
import java.util.function.Predicate;

/* renamed from: android.net.ip.-$$Lambda$IpClient$InitialConfiguration$YwpJbnxCjWZ5CZ7ycLj8DIoOSd8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IpClient$InitialConfiguration$YwpJbnxCjWZ5CZ7ycLj8DIoOSd8 implements Predicate {
    public static final /* synthetic */ $$Lambda$IpClient$InitialConfiguration$YwpJbnxCjWZ5CZ7ycLj8DIoOSd8 INSTANCE = new $$Lambda$IpClient$InitialConfiguration$YwpJbnxCjWZ5CZ7ycLj8DIoOSd8();

    private /* synthetic */ $$Lambda$IpClient$InitialConfiguration$YwpJbnxCjWZ5CZ7ycLj8DIoOSd8() {
    }

    public final boolean test(Object obj) {
        return IpClient.InitialConfiguration.isPrefixLengthCompliant((LinkAddress) obj);
    }
}
