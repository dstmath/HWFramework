package ohos.event.intentagent;

import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;

public final class TriggerInfo {
    private int code;
    private IntentParams extraInfo;
    private Intent intent;
    private String permission;

    public TriggerInfo() {
        this(null);
    }

    public TriggerInfo(String str, IntentParams intentParams, Intent intent2, int i) {
        this.permission = str;
        if (intentParams != null) {
            this.extraInfo = new IntentParams(intentParams);
        }
        if (intent2 != null) {
            this.intent = new Intent(intent2);
        }
        this.code = i;
    }

    public TriggerInfo(TriggerInfo triggerInfo) {
        if (triggerInfo != null) {
            this.permission = triggerInfo.permission;
            IntentParams intentParams = triggerInfo.extraInfo;
            if (intentParams != null) {
                this.extraInfo = new IntentParams(intentParams);
            }
            Intent intent2 = triggerInfo.intent;
            if (intent2 != null) {
                this.intent = new Intent(intent2);
            }
            this.code = triggerInfo.code;
        }
    }

    public String getPermission() {
        return this.permission;
    }

    public IntentParams getExtraInfo() {
        return this.extraInfo;
    }

    public Intent getIntent() {
        return this.intent;
    }

    public int getCode() {
        return this.code;
    }

    public static final class Builder {
        private int code;
        private Intent intent;
        private IntentParams params;
        private String permission;

        public Builder setPermission(String str) {
            this.permission = str;
            return this;
        }

        public Builder setIntentParams(IntentParams intentParams) {
            this.params = intentParams;
            return this;
        }

        public Builder setIntent(Intent intent2) {
            this.intent = intent2;
            return this;
        }

        public Builder setCode(int i) {
            this.code = i;
            return this;
        }

        public TriggerInfo build() {
            return new TriggerInfo(this.permission, this.params, this.intent, this.code);
        }
    }
}
