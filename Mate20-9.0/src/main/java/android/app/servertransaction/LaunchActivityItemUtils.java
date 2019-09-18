package android.app.servertransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;

public class LaunchActivityItemUtils {
    static Intent getIntent(Object launchActivityItem) {
        if (launchActivityItem instanceof LaunchActivityItem) {
            return ((LaunchActivityItem) launchActivityItem).getIntent();
        }
        return null;
    }

    static ActivityInfo getActivityInfo(Object launchActivityItem) {
        if (launchActivityItem instanceof LaunchActivityItem) {
            return ((LaunchActivityItem) launchActivityItem).getActivityInfo();
        }
        return null;
    }
}
