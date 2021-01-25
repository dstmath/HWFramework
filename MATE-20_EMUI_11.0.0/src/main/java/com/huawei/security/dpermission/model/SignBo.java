package com.huawei.security.dpermission.model;

import com.huawei.security.dpermission.utils.LogUtil;
import ohos.global.icu.impl.PatternTokenizer;

public class SignBo {
    private String sha256;

    public String getSha256() {
        return this.sha256;
    }

    public void setSha256(String str) {
        this.sha256 = str;
    }

    public String toString() {
        return "SignBo{sha256='" + LogUtil.mask(this.sha256) + PatternTokenizer.SINGLE_QUOTE + '}';
    }
}
