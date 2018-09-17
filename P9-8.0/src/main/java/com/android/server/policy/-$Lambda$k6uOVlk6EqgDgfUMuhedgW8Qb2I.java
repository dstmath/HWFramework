package com.android.server.policy;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

final /* synthetic */ class -$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I implements OnCancelListener {
    private final /* synthetic */ int -$f0;
    private final /* synthetic */ Object -$f1;

    /* renamed from: com.android.server.policy.-$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I$1 */
    final /* synthetic */ class AnonymousClass1 implements OnClickListener {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(DialogInterface arg0, int arg1) {
            ((AccessibilityShortcutController) this.-$f1).lambda$-com_android_server_policy_AccessibilityShortcutController_9607(this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj) {
            this.-$f0 = i;
            this.-$f1 = obj;
        }

        public final void onClick(DialogInterface dialogInterface, int i) {
            $m$0(dialogInterface, i);
        }
    }

    private final /* synthetic */ void $m$0(DialogInterface arg0) {
        ((AccessibilityShortcutController) this.-$f1).lambda$-com_android_server_policy_AccessibilityShortcutController_9939(this.-$f0, arg0);
    }

    public /* synthetic */ -$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I(int i, Object obj) {
        this.-$f0 = i;
        this.-$f1 = obj;
    }

    public final void onCancel(DialogInterface dialogInterface) {
        $m$0(dialogInterface);
    }
}
