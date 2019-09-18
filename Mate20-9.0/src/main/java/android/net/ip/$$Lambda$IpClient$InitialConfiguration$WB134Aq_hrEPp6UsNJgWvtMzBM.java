package android.net.ip;

import android.net.LinkAddress;
import android.net.ip.IpClient;
import java.util.function.Predicate;

/* renamed from: android.net.ip.-$$Lambda$IpClient$InitialConfiguration$WB134Aq_hrEPp-6UsNJgWvtMzBM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IpClient$InitialConfiguration$WB134Aq_hrEPp6UsNJgWvtMzBM implements Predicate {
    public static final /* synthetic */ $$Lambda$IpClient$InitialConfiguration$WB134Aq_hrEPp6UsNJgWvtMzBM INSTANCE = new $$Lambda$IpClient$InitialConfiguration$WB134Aq_hrEPp6UsNJgWvtMzBM();

    private /* synthetic */ $$Lambda$IpClient$InitialConfiguration$WB134Aq_hrEPp6UsNJgWvtMzBM() {
    }

    public final boolean test(Object obj) {
        return IpClient.InitialConfiguration.isIPv6GUA((LinkAddress) obj);
    }
}
