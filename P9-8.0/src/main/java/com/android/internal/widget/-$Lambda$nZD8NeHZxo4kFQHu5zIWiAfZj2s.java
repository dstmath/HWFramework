package com.android.internal.widget;

import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass13;

final /* synthetic */ class -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s implements OnMenuItemClickListener {

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$1 */
    final /* synthetic */ class AnonymousClass1 implements OnComputeInternalInsetsListener {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(InternalInsetsInfo arg0) {
            ((FloatingToolbarPopup) this.-$f0).lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_16966(arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void onComputeInternalInsets(InternalInsetsInfo internalInsetsInfo) {
            $m$0(internalInsetsInfo);
        }
    }

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((AnonymousClass13) this.-$f0).lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup$13_75644();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$3 */
    final /* synthetic */ class AnonymousClass3 implements OnClickListener {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(View arg0) {
            ((FloatingToolbarPopup) this.-$f0).lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_72553((ImageButton) this.-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onClick(View view) {
            $m$0(view);
        }
    }

    /* renamed from: com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s$4 */
    final /* synthetic */ class AnonymousClass4 implements OnItemClickListener {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(AdapterView arg0, View arg1, int arg2, long arg3) {
            ((FloatingToolbarPopup) this.-$f0).lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_73918((OverflowPanel) this.-$f1, arg0, arg1, arg2, arg3);
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
            $m$0(adapterView, view, i, j);
        }
    }

    public final boolean onMenuItemClick(MenuItem menuItem) {
        return $m$0(menuItem);
    }
}
