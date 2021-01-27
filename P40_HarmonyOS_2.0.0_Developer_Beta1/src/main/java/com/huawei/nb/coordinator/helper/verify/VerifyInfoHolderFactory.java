package com.huawei.nb.coordinator.helper.verify;

public final class VerifyInfoHolderFactory {
    private VerifyInfoHolderFactory() {
    }

    public static VerifyInfoHolder getVerifyInfoHolder(int i) {
        return VerifyInfoHolder.getInstance(i);
    }
}
