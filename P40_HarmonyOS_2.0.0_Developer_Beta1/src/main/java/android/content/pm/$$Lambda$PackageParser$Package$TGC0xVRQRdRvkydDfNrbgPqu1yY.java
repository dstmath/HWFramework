package android.content.pm;

import java.io.File;
import java.io.FilenameFilter;

/* renamed from: android.content.pm.-$$Lambda$PackageParser$Package$TGC0xVRQRdRvkydDfNrbgPqu1yY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageParser$Package$TGC0xVRQRdRvkydDfNrbgPqu1yY implements FilenameFilter {
    public static final /* synthetic */ $$Lambda$PackageParser$Package$TGC0xVRQRdRvkydDfNrbgPqu1yY INSTANCE = new $$Lambda$PackageParser$Package$TGC0xVRQRdRvkydDfNrbgPqu1yY();

    private /* synthetic */ $$Lambda$PackageParser$Package$TGC0xVRQRdRvkydDfNrbgPqu1yY() {
    }

    @Override // java.io.FilenameFilter
    public final boolean accept(File file, String str) {
        return str.endsWith(".hap");
    }
}
