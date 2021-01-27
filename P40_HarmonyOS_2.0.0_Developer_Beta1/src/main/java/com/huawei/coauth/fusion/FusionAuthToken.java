package com.huawei.coauth.fusion;

public class FusionAuthToken {
    FusionAuthType authType;
    String templateId;

    FusionAuthToken(FusionAuthType authType2, String templateId2) {
        this.authType = authType2;
        this.templateId = templateId2;
    }

    public FusionAuthType getAuthType() {
        return this.authType;
    }

    public String getTemplateId() {
        return this.templateId;
    }
}
