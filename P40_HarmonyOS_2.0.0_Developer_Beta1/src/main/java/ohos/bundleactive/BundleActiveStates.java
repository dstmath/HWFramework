package ohos.bundleactive;

import com.huawei.ohos.bundleactiveadapter.BundleActiveStatesAdapter;

public final class BundleActiveStates {
    private BundleActiveStatesAdapter mStatesAdapter = null;

    public static final class State {
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
        private BundleActiveStatesAdapter.StateAdapter mStateAdapter;

        public State() {
            this.mStateAdapter = null;
            this.mStateAdapter = new BundleActiveStatesAdapter.StateAdapter();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setStateAdapter(BundleActiveStatesAdapter.StateAdapter stateAdapter) {
            this.mStateAdapter = stateAdapter;
        }

        public int queryAppUsagePriorityGroup() {
            return this.mStateAdapter.queryAppUsagePriorityGroup();
        }

        public String queryNameOfClass() {
            return this.mStateAdapter.queryNameOfClass();
        }

        public int queryStateType() {
            return this.mStateAdapter.queryStateType();
        }

        public String queryBundleName() {
            return this.mStateAdapter.queryBundleName();
        }

        public String queryIndexOfLink() {
            return this.mStateAdapter.queryIndexOfLink();
        }

        public long queryStateOccurredMs() {
            return this.mStateAdapter.queryStateOccurredMs();
        }
    }

    BundleActiveStates(Object obj) {
        if (obj instanceof BundleActiveStatesAdapter) {
            this.mStatesAdapter = (BundleActiveStatesAdapter) obj;
        } else {
            this.mStatesAdapter = new BundleActiveStatesAdapter(null);
        }
    }

    public boolean hasNextState() {
        return this.mStatesAdapter.hasNextStateAdapter();
    }

    public boolean queryNextState(State state) {
        if (state == null) {
            return false;
        }
        BundleActiveStatesAdapter.StateAdapter stateAdapter = new BundleActiveStatesAdapter.StateAdapter();
        boolean queryNextStateAdapter = this.mStatesAdapter.queryNextStateAdapter(stateAdapter);
        if (queryNextStateAdapter) {
            state.setStateAdapter(stateAdapter);
        }
        return queryNextStateAdapter;
    }
}
