package com.android.server.wm;

import java.util.ArrayList;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$2llf7xFHC_eOUHiFBaZbS1vcCvs implements Consumer {
    private final /* synthetic */ Object -$f0;

    /* renamed from: com.android.server.wm.-$Lambda$2llf7xFHC_eOUHiFBaZbS1vcCvs$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((HwWindowManagerService) this.-$f0).lambda$-com_android_server_wm_HwWindowManagerService_75919((String) this.-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        HwWindowManagerService.lambda$-com_android_server_wm_HwWindowManagerService_60641((ArrayList) this.-$f0, (WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$2llf7xFHC_eOUHiFBaZbS1vcCvs(Object obj) {
        this.-$f0 = obj;
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}
