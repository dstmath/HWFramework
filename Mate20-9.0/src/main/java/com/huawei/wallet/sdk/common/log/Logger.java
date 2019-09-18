package com.huawei.wallet.sdk.common.log;

import java.util.Map;

public class Logger {
    private String mTag;

    public static class Builder {
        /* access modifiers changed from: private */
        public String module;
        /* access modifiers changed from: private */
        public String tag;

        public Logger build() {
            return new Logger(this);
        }

        public Builder setTag(String tag2) {
            this.tag = tag2;
            return this;
        }

        public Builder setModule(String module2) {
            this.module = module2;
            return this;
        }

        public Builder depth(int depth) {
            return this;
        }
    }

    Logger(Builder builder) {
        String str;
        if (builder.module != null) {
            this.mTag = builder.module + ":";
        }
        if (builder.tag != null) {
            if (this.mTag == null) {
                str = builder.tag;
            } else {
                str = this.mTag + builder.tag;
            }
            this.mTag = str;
        }
    }

    public static Builder tag(String tag) {
        return new Builder().setTag(tag);
    }

    public static Builder module(String module) {
        return new Builder().setModule(module);
    }

    public static boolean isDebugLogEnable() {
        return LogUtil.isDebugLogEnable();
    }

    public static void d(String tag, String msg, boolean isNeedProguard) {
        LogUtil.d(tag, msg, isNeedProguard);
    }

    public static void i(String tag, String msg, boolean isNeedProguard) {
        LogUtil.i(tag, msg, isNeedProguard);
    }

    public static void i(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.i(tag, msg, e, isNeedProguard);
    }

    public static void w(String tag, String msg, boolean isNeedProguard) {
        LogUtil.e(tag, msg, null, isNeedProguard);
    }

    public static void w(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(tag, msg, e, isNeedProguard);
    }

    public static void e(String tag, String msg, boolean isNeedProguard) {
        LogUtil.e(tag, msg, null, isNeedProguard);
    }

    public static void e(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(tag, msg, e, isNeedProguard);
    }

    public static void e(String tag, String message, Throwable e, int errorCode, Map<String, String> map, boolean uploadLog, boolean isNeedProguard) {
        if (message != null) {
            LogUtil.e(tag, message, e, isNeedProguard);
        }
    }

    public void d(String msg, boolean isNeedProguard) {
        LogUtil.d(this.mTag, msg, isNeedProguard);
    }

    public void i(String msg, boolean isNeedProguard) {
        LogUtil.i(this.mTag, msg, isNeedProguard);
    }

    public void i(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.i(this.mTag, msg, e, isNeedProguard);
    }

    public void w(String msg, boolean isNeedProguard) {
        LogUtil.e(this.mTag, msg, null, isNeedProguard);
    }

    public void w(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(this.mTag, msg, e, isNeedProguard);
    }

    public void e(String msg, boolean isNeedProguard) {
        LogUtil.e(this.mTag, msg, null, isNeedProguard);
    }

    public void e(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(this.mTag, msg, e, isNeedProguard);
    }
}
