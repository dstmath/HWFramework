package com.huawei.android.feature.install;

import java.util.ArrayList;
import java.util.List;

public class InstallRequest {
    private final List mRequestNames;

    public class Builder<T> {
        /* access modifiers changed from: private */
        public final List<T> mNames;

        private Builder() {
            this.mNames = new ArrayList();
        }

        public Builder addModule(T t) {
            this.mNames.add(t);
            return this;
        }

        public InstallRequest build() {
            if (this.mNames.size() != 0) {
                return new InstallRequest(this);
            }
            throw new IllegalStateException("Request module names is null");
        }
    }

    private InstallRequest(Builder builder) {
        this.mRequestNames = new ArrayList();
        this.mRequestNames.addAll(builder.mNames);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public List getInstallRequestModules() {
        return this.mRequestNames;
    }
}
