package com.android.server.wm;

import android.os.Environment;
import com.android.server.wm.TaskSnapshotPersister;
import java.io.File;

/* renamed from: com.android.server.wm.-$$Lambda$TaskSnapshotController$OPdXuZQLetMnocdH6XV32JbNQ3I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TaskSnapshotController$OPdXuZQLetMnocdH6XV32JbNQ3I implements TaskSnapshotPersister.DirectoryResolver {
    public static final /* synthetic */ $$Lambda$TaskSnapshotController$OPdXuZQLetMnocdH6XV32JbNQ3I INSTANCE = new $$Lambda$TaskSnapshotController$OPdXuZQLetMnocdH6XV32JbNQ3I();

    private /* synthetic */ $$Lambda$TaskSnapshotController$OPdXuZQLetMnocdH6XV32JbNQ3I() {
    }

    public final File getSystemDirectoryForUser(int i) {
        return Environment.getDataSystemCeDirectory(i);
    }
}
