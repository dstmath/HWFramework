package com.android.server.autofill.ui;

import android.service.autofill.FillResponse;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter.FilterListener;

final /* synthetic */ class -$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694 implements OnItemClickListener {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((AnchoredWindow) this.-$f0).-com_android_server_autofill_ui_FillUi$AutofillWindowPresenter-mthref-0();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694$2 */
    final /* synthetic */ class AnonymousClass2 implements OnClickListener {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(View arg0) {
            ((FillUi) this.-$f0).lambda$-com_android_server_autofill_ui_FillUi_5115((FillResponse) this.-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void onClick(View view) {
            $m$0(view);
        }
    }

    /* renamed from: com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0() {
            ((AutofillWindowPresenter) this.-$f0).lambda$-com_android_server_autofill_ui_FillUi$AutofillWindowPresenter_13158((LayoutParams) this.-$f1);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.autofill.ui.-$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694$4 */
    final /* synthetic */ class AnonymousClass4 implements FilterListener {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(int arg0) {
            ((FillUi) this.-$f1).lambda$-com_android_server_autofill_ui_FillUi_7794(this.-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass4(int i, Object obj) {
            this.-$f0 = i;
            this.-$f1 = obj;
        }

        public final void onFilterComplete(int i) {
            $m$0(i);
        }
    }

    private final /* synthetic */ void $m$0(AdapterView arg0, View arg1, int arg2, long arg3) {
        ((FillUi) this.-$f0).lambda$-com_android_server_autofill_ui_FillUi_7197(arg0, arg1, arg2, arg3);
    }

    public /* synthetic */ -$Lambda$DgpgpA67BLtfXEF5MAIi1KCU694(Object obj) {
        this.-$f0 = obj;
    }

    public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
        $m$0(adapterView, view, i, j);
    }
}
