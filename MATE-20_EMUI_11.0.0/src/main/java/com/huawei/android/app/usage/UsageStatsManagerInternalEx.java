package com.huawei.android.app.usage;

import android.app.usage.UsageStatsManagerInternal;
import com.android.server.LocalServices;

public class UsageStatsManagerInternalEx {
    public static void addAppIdleStateChangeListener(AppIdleStateChangeListenerEx listenerEx) {
        ((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class)).addAppIdleStateChangeListener(listenerEx.getListener());
    }

    public static class AppIdleStateChangeListenerEx {
        private UsageStatsManagerInternal.AppIdleStateChangeListener mListener = new UsageStatsManagerInternal.AppIdleStateChangeListener() {
            /* class com.huawei.android.app.usage.UsageStatsManagerInternalEx.AppIdleStateChangeListenerEx.AnonymousClass1 */

            public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
                AppIdleStateChangeListenerEx.this.onAppIdleStateChanged(packageName, userId, idle, bucket, reason);
            }

            public void onParoleStateChanged(boolean isParoleOn) {
            }
        };

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
        }

        public UsageStatsManagerInternal.AppIdleStateChangeListener getListener() {
            return this.mListener;
        }
    }
}
