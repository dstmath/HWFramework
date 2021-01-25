package com.android.server.firewall;

import android.content.ComponentName;
import android.content.Intent;

/* access modifiers changed from: package-private */
public interface Filter {
    boolean matches(IntentFirewall intentFirewall, ComponentName componentName, Intent intent, int i, int i2, String str, int i3);
}
