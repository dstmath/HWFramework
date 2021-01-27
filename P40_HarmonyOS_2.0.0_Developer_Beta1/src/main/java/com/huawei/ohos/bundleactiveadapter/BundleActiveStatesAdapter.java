package com.huawei.ohos.bundleactiveadapter;

import android.app.usage.UsageEvents;

public final class BundleActiveStatesAdapter {
    private UsageEvents mUsageEvents;

    public static final class StateAdapter {
        public static final int STATE_TYPE_ABILITY_ENDED = 7;
        public static final int STATE_TYPE_ABILITY_PAUSED = 0;
        public static final int STATE_TYPE_ABILITY_RESUMED = 1;
        public static final int STATE_TYPE_CALL_LINK = 8;
        public static final int STATE_TYPE_FOREGROUND_ABILITY_BEGIN = 5;
        public static final int STATE_TYPE_FOREGROUND_ABILITY_END = 6;
        public static final int STATE_TYPE_HAS_INTERACTED = 3;
        public static final int STATE_TYPE_PROFILE_MODIFIED = 2;
        public static final int STATE_TYPE_UNKNOW = 9;
        public static final int STATE_TYPE_USAGE_PRIORITY_GROUP_MODIFIED = 4;
        private UsageEvents.Event mUsageEvent = new UsageEvents.Event();

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setEvent(UsageEvents.Event event) {
            this.mUsageEvent = event;
        }

        public int queryAppUsagePriorityGroup() {
            int appStandbyBucket = this.mUsageEvent.getAppStandbyBucket();
            if (appStandbyBucket == 5) {
                return 0;
            }
            if (appStandbyBucket == 10) {
                return 1;
            }
            if (appStandbyBucket == 20) {
                return 2;
            }
            if (appStandbyBucket == 30) {
                return 3;
            }
            if (appStandbyBucket == 40) {
                return 4;
            }
            if (appStandbyBucket != 50) {
            }
            return 5;
        }

        public String queryNameOfClass() {
            return this.mUsageEvent.getClassName();
        }

        public int queryStateType() {
            int eventType = this.mUsageEvent.getEventType();
            if (eventType == 1) {
                return 1;
            }
            if (eventType == 2) {
                return 0;
            }
            if (eventType == 5) {
                return 2;
            }
            if (eventType == 11) {
                return 4;
            }
            if (eventType == 23) {
                return 7;
            }
            if (eventType == 7) {
                return 3;
            }
            if (eventType == 8) {
                return 8;
            }
            if (eventType != 19) {
                return eventType != 20 ? 9 : 6;
            }
            return 5;
        }

        public String queryBundleName() {
            return this.mUsageEvent.getPackageName();
        }

        public String queryIndexOfLink() {
            return this.mUsageEvent.getShortcutId();
        }

        public long queryStateOccurredMs() {
            return this.mUsageEvent.getTimeStamp();
        }
    }

    public BundleActiveStatesAdapter(UsageEvents usageEvents) {
        this.mUsageEvents = usageEvents;
    }

    public boolean hasNextStateAdapter() {
        UsageEvents usageEvents = this.mUsageEvents;
        if (usageEvents == null) {
            return false;
        }
        return usageEvents.hasNextEvent();
    }

    public boolean queryNextStateAdapter(StateAdapter stateAdapter) {
        if (this.mUsageEvents == null || stateAdapter == null) {
            return false;
        }
        UsageEvents.Event event = new UsageEvents.Event();
        boolean nextEvent = this.mUsageEvents.getNextEvent(event);
        if (nextEvent) {
            stateAdapter.setEvent(event);
        }
        return nextEvent;
    }
}
