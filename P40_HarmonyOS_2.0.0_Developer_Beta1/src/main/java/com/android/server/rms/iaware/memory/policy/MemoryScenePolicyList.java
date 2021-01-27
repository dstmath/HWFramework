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
        Map<String, MemoryScenePolicy> map = this.mMemoryScenePolicies;
        if (map != null) {
            return map.get(name);
        }
        return null;
    }

    public void reset() {
        Map<String, MemoryScenePolicy> map = this.mMemoryScenePolicies;
        if (map == null || map.size() < 1) {
            AwareLog.w(TAG, "empty memoryScenePolicies");
            return;
        }
        for (MemoryScenePolicy policy : this.mMemoryScenePolicies.values()) {
            policy.reset();
        }
    }

    public void clear() {
        Map<String, MemoryScenePolicy> map = this.mMemoryScenePolicies;
        if (map == null || map.size() < 1) {
            AwareLog.w(TAG, "empty memoryScenePolicies");
            return;
        }
        for (MemoryScenePolicy policy : this.mMemoryScenePolicies.values()) {
            policy.clear();
        }
    }
}
