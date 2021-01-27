package com.huawei.dalvik.system;

import dalvik.system.BlockGuard;

public class BlockGuardEx {
    public static final Policy LAX_POLICY = new Policy(BlockGuard.LAX_POLICY);

    public static Policy getThreadPolicy() {
        return new Policy(BlockGuard.getThreadPolicy());
    }

    public static void setThreadPolicy(Policy policy) {
        BlockGuard.setThreadPolicy(policy.mPolicy);
    }

    public static class Policy {
        private BlockGuard.Policy mPolicy;

        private Policy(BlockGuard.Policy policy) {
            this.mPolicy = policy;
        }
    }
}
