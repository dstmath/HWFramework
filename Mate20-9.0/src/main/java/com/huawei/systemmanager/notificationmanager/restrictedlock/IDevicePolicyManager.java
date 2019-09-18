package com.huawei.systemmanager.notificationmanager.restrictedlock;

import android.content.ComponentName;
import android.content.Context;
import java.util.List;

public interface IDevicePolicyManager {
    List<ComponentName> getActiveAdminsAsUser(Context context, int i);
}
