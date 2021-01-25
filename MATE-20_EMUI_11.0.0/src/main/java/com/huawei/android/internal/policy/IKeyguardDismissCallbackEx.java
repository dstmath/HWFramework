package com.huawei.android.internal.policy;

import com.android.internal.policy.IKeyguardDismissCallback;

public class IKeyguardDismissCallbackEx {
    private IKeyguardDismissCallback mCallback = new IKeyguardDismissCallback.Stub() {
        /* class com.huawei.android.internal.policy.IKeyguardDismissCallbackEx.AnonymousClass1 */

        public void onDismissError() {
            IKeyguardDismissCallbackEx.this.onDismissError();
        }

        public void onDismissSucceeded() {
            IKeyguardDismissCallbackEx.this.onDismissSucceeded();
        }

        public void onDismissCancelled() {
            IKeyguardDismissCallbackEx.this.onDismissCancelled();
        }
    };

    public IKeyguardDismissCallback getIKeyguardDismissCallback() {
        return this.mCallback;
    }

    public void onDismissError() {
    }

    public void onDismissSucceeded() {
    }

    public void onDismissCancelled() {
    }
}
