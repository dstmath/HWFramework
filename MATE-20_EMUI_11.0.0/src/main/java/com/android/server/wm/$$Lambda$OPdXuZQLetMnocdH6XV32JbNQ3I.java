package com.android.server.wm;

import android.os.Environment;
import com.android.server.wm.TaskSnapshotPersister;
import java.io.File;

/* renamed from: com.android.server.wm.-$$Lambda$OPdXuZQLetMnocdH6XV32JbNQ3I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OPdXuZQLetMnocdH6XV32JbNQ3I implements TaskSnapshotPersister.DirectoryResolver {
    public static final /* synthetic */ $$Lambda$OPdXuZQLetMnocdH6XV32JbNQ3I INSTANCE = new $$Lambda$OPdXuZQLetMnocdH6XV32JbNQ3I();

    private /* synthetic */ $$Lambda$OPdXuZQLetMnocdH6XV32JbNQ3I() {
    }

    @Override // com.android.server.wm.TaskSnapshotPersister.DirectoryResolver
    public final File getSystemDirectoryForUser(int i) {
        return Environment.getDataSystemCeDirectory(i);
    }
}
