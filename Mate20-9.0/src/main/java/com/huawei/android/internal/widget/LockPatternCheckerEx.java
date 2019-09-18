package com.huawei.android.internal.widget;

import android.os.AsyncTask;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;

public class LockPatternCheckerEx {

    public interface OnCheckCallback {
        void onChecked(boolean z, int i);
    }

    public static AsyncTask<?, ?, ?> checkPassword(LockPatternUtils utils, String password, int userId, final OnCheckCallback callback) {
        return LockPatternChecker.checkPassword(utils, password, userId, new LockPatternChecker.OnCheckCallback() {
            public void onChecked(boolean b, int i) {
                if (OnCheckCallback.this != null) {
                    OnCheckCallback.this.onChecked(b, i);
                }
            }
        });
    }
}
