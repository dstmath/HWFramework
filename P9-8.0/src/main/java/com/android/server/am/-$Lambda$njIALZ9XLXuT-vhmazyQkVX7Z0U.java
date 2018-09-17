package com.android.server.am;

import android.app.PictureInPictureParams;
import com.android.internal.os.ProcessCpuTracker.FilterStats;
import com.android.internal.os.ProcessCpuTracker.Stats;

final /* synthetic */ class -$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U implements FilterStats {

    /* renamed from: com.android.server.am.-$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((ActivityManagerService) this.-$f0).lambda$-com_android_server_am_ActivityManagerService_391930((ActivityRecord) this.-$f1, (PictureInPictureParams) this.-$f2);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    public final boolean needed(Stats stats) {
        return $m$0(stats);
    }
}
