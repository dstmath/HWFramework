package com.huawei.coauth.fusion;

import android.os.Bundle;

public class UiConfig {
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String IS_BLUR = "IS_BLUR";
    private static final String IS_FULL_SCREEN_MODE = "IS_FULL_SCREEN_MODE";
    private static final String TASK_ID = "TASK_ID";
    private static final String TITLE = "TITLE";
    private String description;
    private boolean isBlur = false;
    private boolean isFullScreen = false;
    int taskId = 0;
    private String title;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UiConfig setTitle(String title2) {
        this.title = title2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UiConfig setDescription(String description2) {
        this.description = description2;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UiConfig setGaussianBlur(boolean blur) {
        this.isBlur = blur;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UiConfig setFullscreenMode(boolean fullScreen) {
        this.isFullScreen = fullScreen;
        return this;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UiConfig setLaunchedTaskId(int taskId2) {
        this.taskId = taskId2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public Bundle createUiBundle() {
        Bundle bundle = new Bundle();
        safePutString(bundle, TITLE, this.title);
        safePutString(bundle, DESCRIPTION, this.description);
        bundle.putBoolean(IS_BLUR, this.isBlur);
        bundle.putBoolean(IS_FULL_SCREEN_MODE, this.isFullScreen);
        bundle.putInt(TASK_ID, this.taskId);
        return bundle;
    }

    private void safePutString(Bundle bundle, String key, String value) {
        if (key != null && value != null) {
            bundle.putString(key, value);
        }
    }

    public static class Builder {
        String description;
        boolean isBlur = false;
        boolean isFullScreen = false;
        private int taskId = 0;
        String title;

        public Builder setTitle(String title2) {
            this.title = title2;
            return this;
        }

        public Builder setDescription(String description2) {
            this.description = description2;
            return this;
        }

        public Builder setEnableGaussianBlur() {
            this.isBlur = true;
            return this;
        }

        public Builder setEnableFullscreenMode() {
            this.isFullScreen = true;
            return this;
        }

        public Builder setLaunchedTaskId(int taskId2) {
            this.taskId = taskId2;
            return this;
        }

        public UiConfig build() {
            return new UiConfig().setTitle(this.title).setDescription(this.description).setGaussianBlur(this.isBlur).setFullscreenMode(this.isFullScreen).setLaunchedTaskId(this.taskId);
        }
    }
}
