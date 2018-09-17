package com.android.server.wm;

import android.graphics.Rect;
import android.os.IBinder;
import android.util.MutableBoolean;
import com.android.internal.util.ToBooleanFunction;

final /* synthetic */ class -$Lambda$gdyonVWUyLAUEjsalC_69kEf8YE implements ScreenshoterForExternalDisplay {

    /* renamed from: com.android.server.wm.-$Lambda$gdyonVWUyLAUEjsalC_69kEf8YE$1 */
    final /* synthetic */ class AnonymousClass1 implements ToBooleanFunction {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ boolean -$f1;
        private final /* synthetic */ boolean -$f2;
        private final /* synthetic */ int -$f3;
        private final /* synthetic */ Object -$f4;
        private final /* synthetic */ Object -$f5;
        private final /* synthetic */ Object -$f6;
        private final /* synthetic */ Object -$f7;
        private final /* synthetic */ Object -$f8;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((HwDisplayContent) this.-$f4).lambda$-com_android_server_wm_HwDisplayContent_6900(this.-$f3, this.-$f0, this.-$f1, (IBinder) this.-$f5, (MutableBoolean) this.-$f6, this.-$f2, (Rect) this.-$f7, (Rect) this.-$f8, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(boolean z, boolean z2, boolean z3, int i, Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.-$f0 = z;
            this.-$f1 = z2;
            this.-$f2 = z3;
            this.-$f3 = i;
            this.-$f4 = obj;
            this.-$f5 = obj2;
            this.-$f6 = obj3;
            this.-$f7 = obj4;
            this.-$f8 = obj5;
        }

        public final boolean apply(Object obj) {
            return $m$0(obj);
        }
    }

    public final Object screenshotForExternalDisplay(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) {
        return $m$0(iBinder, rect, i, i2, i3, i4, z, i5);
    }
}
