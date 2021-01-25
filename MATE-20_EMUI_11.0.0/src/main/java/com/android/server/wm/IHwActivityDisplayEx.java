package com.android.server.wm;

public interface IHwActivityDisplayEx {
    boolean keepStackResumed(ActivityStack activityStack);

    boolean launchMagicOnSplitScreenDismissed(ActivityStack activityStack);
}
