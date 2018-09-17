package com.android.internal.widget;

public interface LockScreenWidgetInterface {
    boolean providesClock();

    void setCallback(LockScreenWidgetCallback lockScreenWidgetCallback);
}
