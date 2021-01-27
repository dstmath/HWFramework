package com.huawei.coauth.pool;

import android.os.Bundle;
import com.huawei.coauth.pool.types.AuthType;
import com.huawei.coauth.pool.types.ExecutorSecureLevel;
import com.huawei.coauth.pool.types.ExecutorType;

public class AuthExecutor {
    private static final String KEY_ABILITY = "ability";
    private static final String KEY_AUTH_TYPE = "authType";
    private static final String KEY_DEVICE_ID = "deviceId";
    private static final String KEY_EXECUTOR_TYPE = "executorType";
    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_IS_MULTIPLEMODE = "multipleMode";
    private static final String KEY_IS_VOLATILE = "volatile";
    private static final String KEY_MODULE_NAME = "moduleName";
    private static final String KEY_PUBLIC_KEY = "publicKey";
    private static final String KEY_SECURE_LEVEL = "secureLevel";
    private long ability;
    private AuthType authType;
    private ExecutorType executorType;
    private String gid;
    private boolean isMultipleMode;
    private boolean isVolatileMode;
    private String moduleName;
    private byte[] publicKey;
    private ExecutorSecureLevel secureLevel;
    private String udid;

    private AuthExecutor() {
    }

    public static class Builder {
        private long ability;
        private AuthType authType;
        private ExecutorType executorType;
        private String gid;
        private boolean isMultipleMode = false;
        private boolean isVolatileMode = true;
        private String moduleName;
        private byte[] publicKey;
        private ExecutorSecureLevel secureLevel;
        private String udid;

        public Builder setAuthType(AuthType type) {
            this.authType = type;
            return this;
        }

        public Builder setExecutorType(ExecutorType type) {
            this.executorType = type;
            return this;
        }

        public Builder setExecutorSecurityLevel(ExecutorSecureLevel level) {
            this.secureLevel = level;
            return this;
        }

        public Builder setExecutorAbility(long executorAbility) {
            this.ability = executorAbility;
            return this;
        }

        public Builder setExecutorPublicKey(int type, byte[] key) {
            this.publicKey = (byte[]) key.clone();
            return this;
        }

        public Builder setExecutorGroupId(String groupId) {
            this.gid = groupId;
            return this;
        }

        public Builder setExecutorDeviceId(String deviceId) {
            this.udid = deviceId;
            return this;
        }

        public Builder setVolatileMode(boolean mode) {
            this.isVolatileMode = mode;
            return this;
        }

        public Builder setExecutorModule(String module) {
            this.moduleName = module;
            return this;
        }

        public Builder setEnableMultipleSession(boolean mode) {
            this.isMultipleMode = mode;
            return this;
        }

        public AuthExecutor build() {
            AuthExecutor executor = new AuthExecutor();
            executor.authType = this.authType;
            executor.executorType = this.executorType;
            executor.secureLevel = this.secureLevel;
            executor.ability = this.ability;
            executor.publicKey = this.publicKey;
            executor.gid = this.gid;
            executor.udid = this.udid;
            executor.isVolatileMode = this.isVolatileMode;
            executor.moduleName = this.moduleName;
            executor.isMultipleMode = this.isMultipleMode;
            return executor;
        }
    }

    /* access modifiers changed from: protected */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_AUTH_TYPE, this.authType.getValue());
        bundle.putInt(KEY_EXECUTOR_TYPE, this.executorType.getValue());
        bundle.putInt(KEY_SECURE_LEVEL, this.secureLevel.getValue());
        bundle.putLong(KEY_ABILITY, this.ability);
        bundle.putByteArray(KEY_PUBLIC_KEY, this.publicKey);
        bundle.putString("groupId", this.gid);
        bundle.putString("deviceId", this.udid);
        bundle.putString(KEY_MODULE_NAME, this.moduleName);
        bundle.putBoolean(KEY_IS_VOLATILE, this.isVolatileMode);
        bundle.putBoolean(KEY_IS_MULTIPLEMODE, this.isMultipleMode);
        return bundle;
    }
}
