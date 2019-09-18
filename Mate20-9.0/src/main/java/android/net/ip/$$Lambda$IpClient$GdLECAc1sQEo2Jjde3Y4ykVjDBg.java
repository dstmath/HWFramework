package android.net.ip;

import android.net.LinkAddress;
import java.util.function.Predicate;

/* renamed from: android.net.ip.-$$Lambda$IpClient$GdLECAc1sQEo2Jjde3Y4ykVjDBg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IpClient$GdLECAc1sQEo2Jjde3Y4ykVjDBg implements Predicate {
    public static final /* synthetic */ $$Lambda$IpClient$GdLECAc1sQEo2Jjde3Y4ykVjDBg INSTANCE = new $$Lambda$IpClient$GdLECAc1sQEo2Jjde3Y4ykVjDBg();

    private /* synthetic */ $$Lambda$IpClient$GdLECAc1sQEo2Jjde3Y4ykVjDBg() {
    }

    public final boolean test(Object obj) {
        return ((LinkAddress) obj).isIPv6();
    }
}
