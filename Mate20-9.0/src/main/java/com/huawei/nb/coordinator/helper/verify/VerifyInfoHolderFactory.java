package com.huawei.nb.coordinator.helper.verify;

public class VerifyInfoHolderFactory {
    public static VerifyInfoHolder getVerifyInfoHolder(int mode) {
        return VerifyInfoHolder.getInstance(mode);
    }
}
