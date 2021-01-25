package com.huawei.coauth.fusion;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.coauth.fusion.CustomizedPinChecker;
import com.huawei.fusionauth.fusion.InnerCallback;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class FusionAuthContext {
    private static final String AUTH_ID = "AUTH_ID";
    private static final String AUTH_TYPES = "AUTH_TYPES";
    private static final String CUSTOMIZED_PIN = "CUSTOMIZED_PIN";
    private static final String PIN_CHECKER = "PIN_CHECKER";
    private static final String TASK_ID = "TASK_ID";
    private static final String UI_CONFIG = "UI_CONFIG";
    private static final String WINDOW_MODE = "WINDOW_MODE";
    Context appContext;
    long authId;
    FusionAuthToken authToken;
    FusionAuthType[] authTypes;
    CustomizedPinChecker customizedPinChecker;
    private Status status;
    int taskId;
    UiConfig uiConfig;
    int windowMode;

    private FusionAuthContext(Context appContext2) {
        this.taskId = 0;
        this.windowMode = 0;
        this.status = Status.PENDING;
        this.appContext = appContext2;
        this.authId = getNewAuthId();
    }

    /* access modifiers changed from: package-private */
    public FusionAuthContext setAuthTypes(List<FusionAuthType> authTypes2) {
        if (authTypes2 != null) {
            this.authTypes = new FusionAuthType[authTypes2.size()];
            authTypes2.toArray(this.authTypes);
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public FusionAuthContext setUiConfig(UiConfig uiConfig2) {
        this.uiConfig = uiConfig2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public FusionAuthContext setActivityTaskId(int taskId2) {
        this.taskId = taskId2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public FusionAuthContext setWindowMode(int windowMode2) {
        this.windowMode = windowMode2;
        return this;
    }

    /* access modifiers changed from: package-private */
    public FusionAuthContext setPinChecker(CustomizedPinChecker customizedPinChecker2) {
        this.customizedPinChecker = customizedPinChecker2;
        return this;
    }

    private static long getNewAuthId() {
        return new SecureRandom().nextLong();
    }

    public FusionAuthToken getAuthToken() {
        return this.authToken;
    }

    public Status getStatus() {
        Status status2;
        synchronized (this) {
            status2 = this.status;
        }
        return status2;
    }

    /* access modifiers changed from: package-private */
    public void setStatus(Status status2) {
        synchronized (this) {
            this.status = status2;
        }
    }

    public enum Status {
        PENDING(1),
        RUNNING(2),
        FINISHED(3);
        
        private final int value;

        public int getValue() {
            return this.value;
        }

        private Status(int value2) {
            this.value = value2;
        }
    }

    public static class Builder {
        private List<FusionAuthType> authTypeList;
        private Context context;
        private CustomizedPinChecker customizedPinChecker;
        private int taskId = 0;
        private UiConfig uiConfig;
        private int windowMode = 0;

        public Builder(Context context2) {
            this.context = context2;
            if (context2 instanceof Activity) {
                this.taskId = ((Activity) context2).getTaskId();
                this.windowMode = ActivityManagerEx.getActivityWindowMode((Activity) context2);
            }
            this.authTypeList = new ArrayList();
        }

        public Builder setAuthType(List<FusionAuthType> authTypes) {
            this.authTypeList.clear();
            if (authTypes != null) {
                this.authTypeList.addAll(authTypes);
            }
            return this;
        }

        public Builder setAuthType(FusionAuthType authType) {
            this.authTypeList.clear();
            if (authType != null) {
                this.authTypeList.add(authType);
            }
            return this;
        }

        public Builder setTakeOverPinChecker(CustomizedPinChecker checker) {
            this.customizedPinChecker = checker;
            return this;
        }

        public Builder setUiConfig(UiConfig uiConfig2) {
            this.uiConfig = uiConfig2;
            return this;
        }

        public FusionAuthContext build() {
            return new FusionAuthContext(this.context).setAuthTypes(this.authTypeList).setUiConfig(this.uiConfig).setActivityTaskId(this.taskId).setWindowMode(this.windowMode).setPinChecker(this.customizedPinChecker);
        }
    }

    /* access modifiers changed from: package-private */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong(AUTH_ID, this.authId);
        FusionAuthType[] fusionAuthTypeArr = this.authTypes;
        if (fusionAuthTypeArr != null) {
            int[] authValues = new int[fusionAuthTypeArr.length];
            int i = 0;
            while (true) {
                FusionAuthType[] fusionAuthTypeArr2 = this.authTypes;
                if (i >= fusionAuthTypeArr2.length) {
                    break;
                }
                authValues[i] = fusionAuthTypeArr2[i].getValue();
                bundle.putIntArray(AUTH_TYPES, authValues);
                i++;
            }
        }
        UiConfig uiConfig2 = this.uiConfig;
        if (uiConfig2 != null) {
            bundle.putBundle(UI_CONFIG, uiConfig2.createUiBundle());
        }
        CustomizedPinChecker customizedPinChecker2 = this.customizedPinChecker;
        if (customizedPinChecker2 != null) {
            bundle.putBundle(CUSTOMIZED_PIN, customizedPinChecker2.createBundle());
            bundle.putBinder(PIN_CHECKER, new IPinCheckCallback(this.customizedPinChecker.checker));
        }
        int i2 = this.taskId;
        if (i2 != 0) {
            bundle.putInt(TASK_ID, i2);
        }
        bundle.putInt(WINDOW_MODE, this.windowMode);
        return bundle;
    }

    static class IPinCheckCallback extends InnerCallback.Stub {
        private CustomizedPinChecker.Checker checker;

        IPinCheckCallback(CustomizedPinChecker.Checker checker2) {
            this.checker = checker2;
        }

        @Override // com.huawei.fusionauth.fusion.InnerCallback
        public boolean check(byte[] blob) throws RemoteException {
            CustomizedPinChecker.Checker checker2 = this.checker;
            if (checker2 == null) {
                return false;
            }
            return checker2.check(blob);
        }
    }
}
