package android.content.pm;

import android.content.pm.PackageParser;
import java.util.Comparator;

/* renamed from: android.content.pm.-$$Lambda$PackageParser$0DZRgzfgaIMpCOhJqjw6PUiU5vw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageParser$0DZRgzfgaIMpCOhJqjw6PUiU5vw implements Comparator {
    public static final /* synthetic */ $$Lambda$PackageParser$0DZRgzfgaIMpCOhJqjw6PUiU5vw INSTANCE = new $$Lambda$PackageParser$0DZRgzfgaIMpCOhJqjw6PUiU5vw();

    private /* synthetic */ $$Lambda$PackageParser$0DZRgzfgaIMpCOhJqjw6PUiU5vw() {
    }

    public final int compare(Object obj, Object obj2) {
        return Integer.compare(((PackageParser.Activity) obj2).order, ((PackageParser.Activity) obj).order);
    }
}
