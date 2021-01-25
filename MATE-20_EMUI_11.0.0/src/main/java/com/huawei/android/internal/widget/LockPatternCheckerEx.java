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
            /* class com.huawei.android.internal.widget.LockPatternCheckerEx.AnonymousClass1 */

            public void onChecked(boolean b, int i) {
                OnCheckCallback onCheckCallback = OnCheckCallback.this;
                if (onCheckCallback != null) {
                    onCheckCallback.onChecked(b, i);
                }
            }
        });
    }
}
