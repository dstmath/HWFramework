package com.huawei.nb.coordinator.helper.verify;

public class VerifyFactory {
    public static IVerify getVerify(int mode) {
        if (mode == 0) {
            return new VerifyViaHWMember();
        }
        if (mode == 1) {
            return new VerifyViaHWMember();
        }
        if (mode == 2) {
            return new VerifySigature();
        }
        return new VerifyNone();
    }
}
