package com.huawei.nb.coordinator.helper.verify;

public class VerifyFactory {
    private VerifyFactory() {
    }

    public static IVerify getVerify(int i) {
        if (i == 0) {
            return new VerifyViaHWMember();
        }
        if (i == 1) {
            return new VerifyViaHWMember();
        }
        if (i == 2) {
            return new VerifySigature();
        }
        return new VerifyNone();
    }
}
