package com.android.server.pm;

import java.io.File;
import java.io.FileFilter;

/* renamed from: com.android.server.pm.-$$Lambda$ShortcutDumpFiles$v6wMz6MRa9pgSnEDM_9bjvrLaKY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShortcutDumpFiles$v6wMz6MRa9pgSnEDM_9bjvrLaKY implements FileFilter {
    public static final /* synthetic */ $$Lambda$ShortcutDumpFiles$v6wMz6MRa9pgSnEDM_9bjvrLaKY INSTANCE = new $$Lambda$ShortcutDumpFiles$v6wMz6MRa9pgSnEDM_9bjvrLaKY();

    private /* synthetic */ $$Lambda$ShortcutDumpFiles$v6wMz6MRa9pgSnEDM_9bjvrLaKY() {
    }

    @Override // java.io.FileFilter
    public final boolean accept(File file) {
        return file.isFile();
    }
}
