package android.appwidget;

import android.util.ArraySet;

public abstract class AppWidgetManagerInternal {
    public abstract ArraySet<String> getHostedWidgetPackages(int i);

    public abstract void unlockUser(int i);
}
