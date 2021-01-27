package com.android.internal.os;

import android.os.IInstalld;

public interface HwZygoteInit {
    int[] getDexOptNeededForMapleSystemServer(IInstalld iInstalld, String[] strArr, String str);
}
