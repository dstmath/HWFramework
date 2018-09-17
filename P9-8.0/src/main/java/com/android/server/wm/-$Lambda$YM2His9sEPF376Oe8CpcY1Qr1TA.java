package com.android.server.wm;

import com.android.internal.util.ToBooleanFunction;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$YM2His9sEPF376Oe8CpcY1Qr1TA implements ToBooleanFunction {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.wm.-$Lambda$YM2His9sEPF376Oe8CpcY1Qr1TA$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return WallpaperController.lambda$-com_android_server_wm_WallpaperController_23261((WindowState) this.-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    private final /* synthetic */ boolean $m$0(Object arg0) {
        return ((WallpaperController) this.-$f0).lambda$-com_android_server_wm_WallpaperController_4838((WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$YM2His9sEPF376Oe8CpcY1Qr1TA(Object obj) {
        this.-$f0 = obj;
    }

    public final boolean apply(Object obj) {
        return $m$0(obj);
    }
}
