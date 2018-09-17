package com.android.server.pm;

import android.content.IIntentReceiver;
import android.os.Bundle;

/* compiled from: PackageManagerService */
interface PackageSender {
    void sendPackageAddedForNewUsers(String str, boolean z, int i, int... iArr);

    void sendPackageBroadcast(String str, String str2, Bundle bundle, int i, String str3, IIntentReceiver iIntentReceiver, int[] iArr);
}
