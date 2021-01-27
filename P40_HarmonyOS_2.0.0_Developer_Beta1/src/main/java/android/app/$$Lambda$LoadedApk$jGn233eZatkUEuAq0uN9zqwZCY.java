package android.app;

import java.io.File;
import java.io.FilenameFilter;

/* renamed from: android.app.-$$Lambda$LoadedApk$jGn233eZ-atkUEuAq0uN9zqwZCY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LoadedApk$jGn233eZatkUEuAq0uN9zqwZCY implements FilenameFilter {
    public static final /* synthetic */ $$Lambda$LoadedApk$jGn233eZatkUEuAq0uN9zqwZCY INSTANCE = new $$Lambda$LoadedApk$jGn233eZatkUEuAq0uN9zqwZCY();

    private /* synthetic */ $$Lambda$LoadedApk$jGn233eZatkUEuAq0uN9zqwZCY() {
    }

    @Override // java.io.FilenameFilter
    public final boolean accept(File file, String str) {
        return str.endsWith(".hap");
    }
}
