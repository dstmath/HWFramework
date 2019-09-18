package android.content.pm;

import android.content.pm.PackageParser;
import java.util.Comparator;

/* renamed from: android.content.pm.-$$Lambda$PackageParser$M-9fHqS_eEp1oYkuKJhRHOGUxf8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageParser$M9fHqS_eEp1oYkuKJhRHOGUxf8 implements Comparator {
    public static final /* synthetic */ $$Lambda$PackageParser$M9fHqS_eEp1oYkuKJhRHOGUxf8 INSTANCE = new $$Lambda$PackageParser$M9fHqS_eEp1oYkuKJhRHOGUxf8();

    private /* synthetic */ $$Lambda$PackageParser$M9fHqS_eEp1oYkuKJhRHOGUxf8() {
    }

    public final int compare(Object obj, Object obj2) {
        return Integer.compare(((PackageParser.Service) obj2).order, ((PackageParser.Service) obj).order);
    }
}
