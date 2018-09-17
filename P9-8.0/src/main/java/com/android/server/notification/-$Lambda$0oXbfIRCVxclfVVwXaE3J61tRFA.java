package com.android.server.notification;

import android.os.VibrationEffect;
import com.android.server.notification.NotificationManagerService.AnonymousClass14;

final /* synthetic */ class -$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA implements FlagChecker {

    /* renamed from: com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Object -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0() {
            ((NotificationManagerService) this.-$f0).lambda$-com_android_server_notification_NotificationManagerService_218944((NotificationRecord) this.-$f1, (VibrationEffect) this.-$f2);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3) {
            this.-$f0 = obj;
            this.-$f1 = obj2;
            this.-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA$2 */
    final /* synthetic */ class AnonymousClass2 implements FlagChecker {
        private final /* synthetic */ int -$f0;
        private final /* synthetic */ int -$f1;

        private final /* synthetic */ boolean $m$0(int arg0) {
            return AnonymousClass14.lambda$-com_android_server_notification_NotificationManagerService$14_247583(this.-$f0, this.-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass2(int i, int i2) {
            this.-$f0 = i;
            this.-$f1 = i2;
        }

        public final boolean apply(int i) {
            return $m$0(i);
        }
    }

    public final boolean apply(int i) {
        return $m$0(i);
    }
}
