package com.android.server.pm;

import java.io.File;
import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$PackageInstallerSession$W9UfdQnk8WsOPyckE_zXYvseVVs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageInstallerSession$W9UfdQnk8WsOPyckE_zXYvseVVs implements Consumer {
    public static final /* synthetic */ $$Lambda$PackageInstallerSession$W9UfdQnk8WsOPyckE_zXYvseVVs INSTANCE = new $$Lambda$PackageInstallerSession$W9UfdQnk8WsOPyckE_zXYvseVVs();

    private /* synthetic */ $$Lambda$PackageInstallerSession$W9UfdQnk8WsOPyckE_zXYvseVVs() {
    }

    public final void accept(Object obj) {
        ((File) obj).delete();
    }
}
