package com.huawei.android.internal.app;

import android.content.ComponentName;
import android.content.Context;
import com.android.internal.app.AssistUtils;

public class AssistUtilsEx {
    private AssistUtils mAssistUtils;

    public AssistUtilsEx(Context context) {
        this.mAssistUtils = new AssistUtils(context);
    }

    public ComponentName getAssistComponentForUser(int userId) {
        return this.mAssistUtils.getAssistComponentForUser(userId);
    }

    public ComponentName getActiveServiceComponentName() {
        return this.mAssistUtils.getActiveServiceComponentName();
    }
}
