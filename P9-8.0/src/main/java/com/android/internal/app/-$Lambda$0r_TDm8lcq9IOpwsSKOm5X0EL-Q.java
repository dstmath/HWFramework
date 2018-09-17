package com.android.internal.app;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.internal.widget.ResolverDrawerLayout.OnDismissedListener;

final /* synthetic */ class -$Lambda$0r_TDm8lcq9IOpwsSKOm5X0EL-Q implements OnItemClickListener {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.internal.app.-$Lambda$0r_TDm8lcq9IOpwsSKOm5X0EL-Q$1 */
    final /* synthetic */ class AnonymousClass1 implements OnDismissedListener {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((AccessibilityButtonChooserActivity) this.-$f0).-com_android_internal_app_AccessibilityButtonChooserActivity-mthref-0();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void onDismissed() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0(AdapterView arg0, View arg1, int arg2, long arg3) {
        ((AccessibilityButtonChooserActivity) this.-$f0).lambda$-com_android_internal_app_AccessibilityButtonChooserActivity_3326(arg0, arg1, arg2, arg3);
    }

    public /* synthetic */ -$Lambda$0r_TDm8lcq9IOpwsSKOm5X0EL-Q(Object obj) {
        this.-$f0 = obj;
    }

    public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
        $m$0(adapterView, view, i, j);
    }
}
