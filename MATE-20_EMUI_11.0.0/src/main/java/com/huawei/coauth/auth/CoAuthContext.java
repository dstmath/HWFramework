package com.huawei.coauth.auth;

import android.os.Bundle;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public class CoAuthContext {
    private byte[] addition;
    private byte[] authPara;
    private CoAuthType authType;
    private long challenge;
    private boolean coAuthBegin;
    private CoAuthGroup coAuthGroup;
    private boolean enableUi;
    private int execAbility;
    private CoAuthContext fallbackContext;
    private boolean identificationMode;
    private int retryCount;
    private String sensorDeviceId;
    private long sessionId;
    private byte[] templateId;
    private Bundle uiConfig;
    private String verifyDeviceId;

    private CoAuthContext() {
        this.identificationMode = false;
        this.challenge = 0;
        this.coAuthBegin = false;
    }

    public CoAuthGroup getCoAuthGroup() {
        return this.coAuthGroup;
    }

    public long getChallenge() {
        return this.challenge;
    }

    public CoAuthType getAuthType() {
        return this.authType;
    }

    public String getSensorDeviceId() {
        return this.sensorDeviceId;
    }

    public String getVerifyDeviceId() {
        return this.verifyDeviceId;
    }

    public int getExecAbility() {
        return this.execAbility;
    }

    /* access modifiers changed from: package-private */
    public long getSessionId() {
        return this.sessionId;
    }

    /* access modifiers changed from: package-private */
    public boolean isCoAuthBegin() {
        return this.coAuthBegin;
    }

    public Optional<byte[]> getAuthPara() {
        byte[] bArr = this.authPara;
        return bArr == null ? Optional.empty() : Optional.of(bArr.clone());
    }

    public Optional<byte[]> getAddition() {
        byte[] bArr = this.addition;
        return bArr == null ? Optional.empty() : Optional.of(bArr.clone());
    }

    public Optional<byte[]> getTemplateId() {
        byte[] bArr = this.templateId;
        return bArr == null ? Optional.empty() : Optional.of(bArr.clone());
    }

    public Bundle getUiConfig() {
        Bundle bundle = this.uiConfig;
        return bundle == null ? bundle : bundle.deepCopy();
    }

    public boolean isEnableUi() {
        return this.enableUi;
    }

    public boolean isIdentificationMode() {
        return this.identificationMode;
    }

    public CoAuthContext getFallbackContext() {
        return this.fallbackContext;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    /* access modifiers changed from: package-private */
    public void setCoAuthBegin(boolean coAuthBegin2) {
        this.coAuthBegin = coAuthBegin2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setSessionId(long sessionId2) {
        this.sessionId = sessionId2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setCoAuthGroup(CoAuthGroup coAuthGroup2) {
        this.coAuthGroup = coAuthGroup2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setEnableUi(boolean enableUi2) {
        this.enableUi = enableUi2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setChallenge(long challenge2) {
        this.challenge = challenge2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setAuthType(CoAuthType authType2) {
        this.authType = authType2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setSensorDeviceId(String sensorDeviceId2) {
        this.sensorDeviceId = sensorDeviceId2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setVerifyDeviceId(String verifyDeviceId2) {
        this.verifyDeviceId = verifyDeviceId2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setExecAbility(int execAbility2) {
        this.execAbility = execAbility2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setAuthPara(byte[] authPara2) {
        this.authPara = authPara2 == null ? null : Arrays.copyOf(authPara2, authPara2.length);
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setAddition(byte[] addition2) {
        this.addition = addition2 == null ? null : Arrays.copyOf(addition2, addition2.length);
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setTemplateId(byte[] templateId2) {
        this.templateId = templateId2 == null ? null : Arrays.copyOf(templateId2, templateId2.length);
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setIdentificationMode(boolean identificationMode2) {
        this.identificationMode = identificationMode2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setFallbackContext(CoAuthContext coAuthContext) {
        this.fallbackContext = coAuthContext;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setUiConfig(Bundle uiConfig2) {
        this.uiConfig = uiConfig2 == null ? null : uiConfig2.deepCopy();
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CoAuthContext setRetryCount(int retryCount2) {
        this.retryCount = retryCount2;
        return this;
    }

    public static class Builder {
        private byte[] addition;
        private byte[] authPara;
        private CoAuthType authType = CoAuthType.TYPE_IGNORE;
        private long challenge;
        private CoAuthGroup coAuthGroup;
        private boolean enableUi;
        private int execAbility = 0;
        private CoAuthContext fallbackContext;
        private boolean identificationMode = false;
        private int retryCount;
        private String sensorDeviceId;
        private byte[] templateId;
        private Bundle uiConfig;
        private String verifyDeviceId;

        public Builder setCoAuthGroup(CoAuthGroup coAuthGroup2) {
            this.coAuthGroup = coAuthGroup2;
            return this;
        }

        public Builder setCoAuthGroup(String groupId) {
            CoAuthUtil.hexStringToBytes(groupId).ifPresent(new Consumer<byte[]>() {
                /* class com.huawei.coauth.auth.CoAuthContext.Builder.AnonymousClass1 */

                public void accept(byte[] bytes) {
                    Builder.this.coAuthGroup = new CoAuthGroup(bytes);
                }
            });
            return this;
        }

        public Builder setAbilityFlag(int execAbility2) {
            this.execAbility = execAbility2;
            return this;
        }

        public Builder setAuthParam(byte[] para, byte[] addition2) {
            byte[] bArr = null;
            this.authPara = para == null ? null : Arrays.copyOf(para, para.length);
            if (addition2 != null) {
                bArr = Arrays.copyOf(addition2, addition2.length);
            }
            this.addition = bArr;
            return this;
        }

        public Builder setEnableUi(Bundle uiConfig2) {
            this.enableUi = true;
            this.uiConfig = uiConfig2 == null ? null : uiConfig2.deepCopy();
            return this;
        }

        public Builder setEnableRetry(int retryCount2) {
            this.retryCount = retryCount2;
            return this;
        }

        public Builder setIdentificationMode() {
            this.identificationMode = true;
            return this;
        }

        public Builder setTemplateId(byte[] template) {
            this.templateId = template == null ? null : Arrays.copyOf(template, template.length);
            return this;
        }

        public Builder setEnableFallback(CoAuthContext fallbackCtx, Bundle config) {
            this.fallbackContext = fallbackCtx;
            return this;
        }

        public Builder setChallenge(long challenge2) {
            this.challenge = challenge2;
            return this;
        }

        public Builder setAuthType(CoAuthType authType2) {
            this.authType = authType2 == null ? CoAuthType.TYPE_IGNORE : authType2;
            return this;
        }

        public Builder setSensorDeviceId(String sensorDeviceId2) {
            this.sensorDeviceId = sensorDeviceId2;
            return this;
        }

        public Builder setVerifyDeviceId(String verifyDeviceId2) {
            this.verifyDeviceId = verifyDeviceId2;
            return this;
        }

        public CoAuthContext build() {
            return new CoAuthContext().setAuthType(this.authType).setCoAuthGroup(this.coAuthGroup).setChallenge(this.challenge).setSensorDeviceId(this.sensorDeviceId).setVerifyDeviceId(this.verifyDeviceId).setSessionId(CoAuthUtil.getNewSessionId()).setTemplateId(this.templateId).setAuthPara(this.authPara).setAddition(this.addition).setUiConfig(this.uiConfig).setEnableUi(this.enableUi).setRetryCount(this.retryCount).setIdentificationMode(this.identificationMode).setFallbackContext(this.fallbackContext).setExecAbility(this.execAbility);
        }
    }
}
