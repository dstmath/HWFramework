package com.android.server.wifi.HwUtil;

import android.content.ComponentName;
import android.os.Bundle;

public interface IHwDevicePolicyManagerEx {
    Bundle getPolicy(ComponentName componentName, String str);
}
