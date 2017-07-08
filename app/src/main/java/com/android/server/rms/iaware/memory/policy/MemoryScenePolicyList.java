package com.android.server.rms.iaware.memory.policy;

import android.rms.iaware.AwareLog;
import java.util.Map;

public class MemoryScenePolicyList {
    private static final String TAG = "AwareMem_MemPolicyList";
    private final Map<String, MemoryScenePolicy> mMemoryScenePolicies;

    public MemoryScenePolicyList(Map<String, MemoryScenePolicy> memoryScenePolicies) {
        this.mMemoryScenePolicies = memoryScenePolicies;
    }

    public MemoryScenePolicy getMemoryScenePolicy(String name) {
        return this.mMemoryScenePolicies != null ? (MemoryScenePolicy) this.mMemoryScenePolicies.get(name) : null;
    }

    public void reset() {
        if (this.mMemoryScenePolicies == null || this.mMemoryScenePolicies.size() < 1) {
            AwareLog.w(TAG, "empty memoryScenePolicies");
            return;
        }
        for (MemoryScenePolicy policy : this.mMemoryScenePolicies.values()) {
            policy.reset();
        }
    }

    public void clear() {
        if (this.mMemoryScenePolicies == null || this.mMemoryScenePolicies.size() < 1) {
            AwareLog.w(TAG, "empty memoryScenePolicies");
            return;
        }
        for (MemoryScenePolicy policy : this.mMemoryScenePolicies.values()) {
            policy.clear();
        }
    }
}
