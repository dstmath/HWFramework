package com.android.server.wm;

import android.util.SparseArray;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$VrxrRGaWeDA63X9yoVs2zDEaoRI implements Consumer {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.wm.-$Lambda$VrxrRGaWeDA63X9yoVs2zDEaoRI$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            WindowsForAccessibilityObserver.lambda$-com_android_server_wm_AccessibilityController$WindowsForAccessibilityObserver_62351((SparseArray) this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        MagnifiedViewport.lambda$-com_android_server_wm_AccessibilityController$DisplayMagnifier$MagnifiedViewport_30455((SparseArray) this.-$f0, (WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$VrxrRGaWeDA63X9yoVs2zDEaoRI(Object obj) {
        this.-$f0 = obj;
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
