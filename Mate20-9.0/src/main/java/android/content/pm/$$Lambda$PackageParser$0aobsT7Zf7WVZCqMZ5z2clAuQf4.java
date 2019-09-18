package android.content.pm;

import android.content.pm.PackageParser;
import java.util.Comparator;

/* renamed from: android.content.pm.-$$Lambda$PackageParser$0aobsT7Zf7WVZCqMZ5z2clAuQf4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageParser$0aobsT7Zf7WVZCqMZ5z2clAuQf4 implements Comparator {
    public static final /* synthetic */ $$Lambda$PackageParser$0aobsT7Zf7WVZCqMZ5z2clAuQf4 INSTANCE = new $$Lambda$PackageParser$0aobsT7Zf7WVZCqMZ5z2clAuQf4();

    private /* synthetic */ $$Lambda$PackageParser$0aobsT7Zf7WVZCqMZ5z2clAuQf4() {
    }

    public final int compare(Object obj, Object obj2) {
        return Integer.compare(((PackageParser.Activity) obj2).order, ((PackageParser.Activity) obj).order);
    }
}
